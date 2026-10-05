"""Drive a separately packaged QA helper on a private WSL display.

Run only against copied QA profiles. Template installation libraries are read-only.
Arguments: staged source, loader, template profile, isolated wrapper, output directory.
"""
import hashlib
import json
import os
import pathlib
import shutil
import signal
import subprocess
import sys
import time

source, loader, template, wrapper, output = sys.argv[1:6]
source, template, output = map(pathlib.Path, (source, template, output))
assert loader in ('fabric', 'forge', 'neoforge'), 'Unsupported loader'
artifact_loader = loader
mc = __import__('tomllib').loads((source / 'gradle/libs.versions.toml').read_text())['versions']['minecraft']
menu_version = __import__('tomllib').loads((source / 'gradle/libs.versions.toml').read_text())['versions']['modmenu']
output.mkdir(parents=True, exist_ok=False)
client = output / 'client'
(client / 'mods').mkdir(parents=True)
(client / 'config').mkdir()
(client / 'config/fml.toml').write_text('earlyWindowControl=false\n')
saved_config = os.environ.get('BREATH_FOG_QA_CONFIG')
if saved_config:
    shutil.copy2(saved_config, client / 'config/breath_fog.json')
fixture = os.environ.get('BREATH_FOG_QA_WORLD')
if fixture:
    shutil.copytree(fixture, client / 'saves/dogs-world')
else:
    shutil.copytree(template / 'client/saves', client / 'saves')
options = (template / 'client/options.txt').read_text()
for key, value in {'renderDistance':'5','simulationDistance':'5','guiScale':'2','enableVsync':'false','maxFps':'60','soundCategory_master':'0.0','pauseOnLostFocus':'false'}.items():
    lines = [line for line in options.splitlines() if not line.startswith(key + ':')]
    options = '\n'.join(lines) + '\n' + key + ':' + value + '\n'
(client / 'options.txt').write_text(options)
missing_pack = client / 'resourcepacks/breath-fog-no-sprites'
(missing_pack / 'assets/minecraft/atlases').mkdir(parents=True)
(missing_pack / 'pack.mcmeta').write_text(json.dumps({'pack': {'pack_format': 34 if mc == '1.21.1' else 15, 'description': 'Owned missing-sprite QA fixture'}}))
(missing_pack / 'assets/minecraft/atlases/particles.json').write_text(json.dumps({'sources': [{'type': 'minecraft:filter', 'pattern': {'namespace': 'breath_fog'}}]}))

for jar in (source / artifact_loader / 'build/libs').glob('*.jar'):
    if jar.name.endswith(f'-{artifact_loader}.jar') and ('-sources' not in jar.name):
        shutil.copy2(jar, client / 'mods')
if artifact_loader == 'fabric':
    for jar in (template / 'client/mods').glob('fabric-api*.jar'):
        shutil.copy2(jar, client / 'mods')
if len(sys.argv) > 6:
    for jar in pathlib.Path(sys.argv[6]).glob(f'*-{artifact_loader}.jar'):
        shutil.copy2(jar, client / 'mods')
    if artifact_loader == 'fabric' and not os.environ.get('BREATH_FOG_QA_SKIP_MODMENU'):
        menu = list((pathlib.Path('/root/.gradle/caches/modules-2/files-2.1/com.terraformersmc/modmenu') / menu_version).glob(f'*/modmenu-{menu_version}.jar'))
        if menu:
            shutil.copy2(menu[0], client / 'mods')
cmd = json.loads((template / 'launch-command.json').read_text())
cmd = [arg for arg in cmd if not arg.startswith('-Dqa.')]
cmd[cmd.index('--gameDir') + 1] = str(client)
cmd[cmd.index('--username') + 1] = 'BreathQA'
if mc.startswith('26.'):
    if '--graphicsBackend' in cmd:
        cmd[cmd.index('--graphicsBackend') + 1] = os.environ.get('BREATH_FOG_QA_BACKEND', 'vulkan')
    else:
        cmd += ['--graphicsBackend', os.environ.get('BREATH_FOG_QA_BACKEND', 'vulkan')]
(output / 'launch-command.json').write_text(json.dumps(cmd, indent=2))
(output / 'hashes.json').write_text(json.dumps({p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in (client/'mods').glob('*.jar')}, indent=2))
log = (output / 'console.log').open('w')
environment = dict(os.environ, MESA_GL_VERSION_OVERRIDE='4.6', MESA_GLSL_VERSION_OVERRIDE='460')
process = subprocess.Popen(['bash', wrapper] + cmd, cwd=client, stdout=log, stderr=subprocess.STDOUT, start_new_session=True, env=environment)
qa = client / 'qa'
counter = 0
results = []
last_complete_status = {}

def state():
    global last_complete_status
    try:
        last_complete_status = json.loads((qa / 'status.json').read_text())
    except (FileNotFoundError, json.JSONDecodeError):
        pass
    return last_complete_status

def until(predicate, seconds=90):
    end = time.monotonic() + seconds
    while time.monotonic() < end:
        if process.poll() is not None:
            raise RuntimeError(f'Client exited {process.returncode}; see {output}/console.log')
        current = state()
        if current.get('screen') in ('LoadingErrorScreen','ModLoadingErrorScreen'):
            raise RuntimeError(f'Loader blocked startup on {current["screen"]}; inspect {output}/console.log')
        if predicate(current):
            return current
        time.sleep(0.2)
    raise TimeoutError(f'Condition timed out; last state {state()}')

def send(**values):
    global counter
    counter += 1
    command = dict(id=str(counter), **values)
    temp = qa / 'control.tmp'
    temp.write_text(json.dumps(command))
    temp.replace(qa / 'control.json')
    if values.get('stop'):
        return state()
    current = until(lambda s:s.get('id') == str(counter), 30)
    if current.get('error'):
        raise RuntimeError(current['error'])
    return current

def record(name, current):
    (output / (name + '.json')).write_text(json.dumps(current, indent=2))
    results.append(name)
    print(f'PASS {loader} {name}', flush=True)

def shot(name, minimum=0, **values):
    send(shot=name, minimumParticles=minimum, **values)
    until(lambda s:(qa / (name + '.png')).exists(), 40)
    time.sleep(0.5)
    current = until(lambda s:s.get('capturedShot') == name, 8)
    assert current['capturedParticles'] >= minimum, f'{name}: required particles were absent at capture'
    shutil.copy2(qa / (name + '.png'), output)
    record(name, current)

try:
    until(lambda s:s.get('world'), int(os.environ.get('BREATH_FOG_QA_STARTUP_SECONDS', '180')))
    if saved_config:
        assert state()['pixelated'], 'Saved pixel style did not survive a fresh client process'
        record('restart-preserves-pixel-setting', state())
    else:
        assert state()['pixelated'], 'Fresh installation did not select pixelated breath'
        assert json.loads((client/'config/breath_fog.json').read_text())['pixelated']
        record('fresh-default-is-pixelated', state())
    send(pixelated=False)
    send(closeScreen=True, enabled=True, commands=['difficulty peaceful','gamemode creative BreathQA','tp BreathQA 0 200 0','time set day','weather clear'])
    until(lambda s:s.get('fixtureChunks'), 30)
    send(commands=['fill -8 199 -8 8 199 8 minecraft:gray_concrete','tp BreathQA 0 200 0'])
    until(lambda s:'minecraft:gray_concrete' in s.get('fixtureFloor', ''), 15)
    time.sleep(2)
    send(commands=['fillbiome -12 196 -12 12 220 12 minecraft:snowy_plains'], yaw=180)
    time.sleep(2)
    if artifact_loader != 'fabric' or any((client / 'mods').glob('modmenu-*.jar')):
        send(nativeConfig=True)
        assert state()['screen'] == 'BreathFogConfigScreen', 'Native loader config factory failed'
        shot('native-mods-config')
        send(closeScreen=True)
    shot('soft-first', 5, preview=True, camera='FIRST_PERSON', actors=7)
    shot('soft-third-front', 5, preview=True, camera='THIRD_PERSON_FRONT')
    # Exercise each visibility toggle through its real visible button and Save.
    for label, camera in [('Nearby players', 'FIRST_PERSON'), ('First person', 'FIRST_PERSON'), ('Third person', 'THIRD_PERSON_FRONT')]:
        send(actors=0, camera=camera, config=True)
        send(uiToggle=label)
        send(uiClick='Save')
        if label == 'Nearby players':
            send(actors=7, preview=True)
            time.sleep(2)
            assert state()['emitters'] == 1
        else:
            send(preview=True)
            time.sleep(2)
            assert state()['particles'] == 0
        record(label.lower().replace(' ', '-') + '-visibility-toggle', state())
        send(config=True)
        send(uiToggle=label)
        send(uiClick='Save')
    send(actors=7)
    send(clientCommand='breathfog config')
    until(lambda s:s.get('screen') == 'BreathFogConfigScreen', 15)
    record('local-config-command', state())
    shot('settings-visibility')
    send(uiClick='>')
    shot('settings-pixel-toggle')
    send(uiClick='Disabled')
    send(uiClick='Cancel')
    assert not state()['pixelated'], 'Cancel applied draft'
    record('cancel-preserves-soft', state())
    send(config=True)
    send(uiClick='Reset defaults')
    send(uiClick='Save')
    assert state()['pixelated'], 'Reset defaults did not select pixelated breath'
    assert json.loads((client/'config/breath_fog.json').read_text())['pixelated']
    record('reset-default-is-pixelated', state())
    send(config=True)
    send(uiClick='>')
    shot('settings-pixel-default')
    send(closeScreen=True, pixelated=False)
    send(config=True)
    send(uiClick='>')
    send(uiText='NaN')
    send(uiClick='Save')
    assert state()['screen'] == 'BreathFogConfigScreen', 'Invalid number was accepted'
    record('invalid-number-rejected', state())
    send(uiText='1.00')
    send(uiClick='Disabled')
    send(guiScale=3)
    send(guiScale=2)
    send(uiClick='Save')
    assert state()['pixelated'], 'Save did not apply pixelated'
    assert json.loads((client/'config/breath_fog.json').read_text())['pixelated']
    record('pixel-setting-saved', state())
    record('draft-survives-resizing', state())
    shot('pixel-first', 5, preview=True, camera='FIRST_PERSON')
    shot('pixel-third-front', 5, preview=True, camera='THIRD_PERSON_FRONT')
    shot('pixel-third-back', 5, preview=True, camera='THIRD_PERSON_BACK', drive='sprint')
    send(drive='none', reload=True)
    until(lambda s:not s.get('reloading') and not s.get('paused'), 90)
    shot('pixel-after-resource-reload', 5, preview=True, camera='THIRD_PERSON_FRONT')

    if (source / 'focus-default-icon-update').exists():
        address = os.environ.get('BREATH_FOG_QA_SERVER')
        if address:
            send(connect=address)
            until(lambda s:s.get('world') and s.get('biome') == 'minecraft:snowy_plains', 90)
            shot('unmodded-server-breath', 2, preview=True, camera='THIRD_PERSON_FRONT')
        send(disconnect=True)
        until(lambda s:not s.get('world') and s.get('particles') == 0, 15)
        record('disconnect-cleanup', state())
        send(stop=True)
        process.wait(timeout=40)
        assert process.returncode == 0, f'Nonzero client exit {process.returncode}'
        backend = next((line.strip() for line in (output/'console.log').read_text().splitlines() if 'Using graphics backend' in line or 'OpenGL renderer' in line or 'OpenGL version' in line), 'Backend unknown')
        (output/'PASS.json').write_text(json.dumps({'loader':loader,'scenarios':results,'scope':'default/icon update revalidation','driver':backend,'graphicsRenderer':state().get('graphicsRenderer'),'graphicsVersion':state().get('graphicsVersion'),'display':'private WSL Xvfb','mods':[p.name for p in (client/'mods').glob('*.jar')]},indent=2))
        sys.exit(0)
    send(resourcePack='file/breath-fog-no-sprites', reload=True)
    until(lambda s:not s.get('reloading') and not s.get('paused'), 90)
    send(preview=True)
    time.sleep(3)
    assert state()['particles'] == 0, 'Missing sprites must suspend emission'
    record('missing-sprites-suspend-emission', state())
    send(resourcePack='', reload=True)
    until(lambda s:not s.get('reloading') and not s.get('paused'), 90)
    shot('resource-pack-removal-restores-breath', 5, preview=True)
    send(actors=0, camera='THIRD_PERSON_FRONT', commands=['tp BreathQA 0 200 0 180 0','fill -8 200 1 8 205 1 minecraft:glass']
    )
    time.sleep(1)
    until(lambda s:'minecraft:glass' in s.get('glassFixture', ''), 10)
    send(yaw=180, collisionProbe=True)
    until(lambda s:s.get('collidedParticles', 0) > 0, 12)
    record('glass-collision-fades-wisps', state())
    send(commands=['fill -8 200 1 8 205 1 minecraft:air'])

    send(actors=48, motion=.07, preview=True)
    until(lambda s:s.get('emitters') == 24 and not s.get('paused'), 20)
    time.sleep(6)
    shot('crowded-player-budget', 5)
    assert state()['emitters'] == 24 and state()['peak'] <= 256
    send(actors=0, motion=0, drive='none', cancelPreview=True)
    starts = []
    previous_particles = state().get('particles', 0)
    previous_age = state().get('ownClockexhaleAge', -1)
    clock_instrumented = 'ownClockexhaleAge' in state()
    end = time.monotonic() + (30 if clock_instrumented else 12)
    while time.monotonic() < end:
        current = state()
        age = current.get('ownClockexhaleAge', -1)
        onset = previous_age < 0 and age >= 0 if clock_instrumented else current.get('particles', 0) > 0 and previous_particles == 0
        if onset:
            starts.append({'controllerTicks':current['controllerTicks'], 'gameTime':current['gameTime'], 'particles':current['particles'], 'sprinting':current['sprinting'], 'biome':current['biome'], 'ownClockAge':age, 'previewRemaining':current.get('previewRemaining')})
        previous_particles = current.get('particles', 0)
        previous_age = age
        time.sleep(.1)
    assert len(starts) >= (4 if clock_instrumented else 2), 'Normal cold breath did not cycle without preview'
    periods = [b['controllerTicks']-a['controllerTicks'] for a,b in zip(starts, starts[1:])]
    game_periods = [b['gameTime']-a['gameTime'] for a,b in zip(starts, starts[1:])]
    (output / 'natural-cadence-trace.json').write_text(json.dumps({'clockInstrumented':clock_instrumented,'starts':starts,'controllerPeriods':periods,'gameTimePeriods':game_periods},indent=2))
    assert all(start['previewRemaining'] is None or start['previewRemaining'] <= 0 for start in starts), 'Preview contaminated the natural cadence trace'
    assert all(54 <= period <= 106 for period in periods), f'Idle cadence outside allowed interval: {periods}'
    record('natural-cold-breath-cadence', dict(state(), plumeStarts=starts, periods=periods, gameTimePeriods=game_periods))
    send(actors=0, motion=0, cancelPreview=True, commands=['fillbiome -12 196 -12 12 220 12 minecraft:plains'])
    start_tick = state()['ticks']
    until(lambda s:s.get('ticks',0) >= start_tick+75 and s.get('particles') == 0, 25)
    record('warm-biome-suppressed', state())
    shot('warm-biome-preview', 2, preview=True)
    send(commands=['fillbiome -12 196 -12 12 220 12 minecraft:snowy_plains'])
    if loader == 'forge':
        start = send(fps=10)
        end = until(lambda s:s.get('gameTime',0) >= start['gameTime']+100, 20)
        actual = end['controllerTicks']-start['controllerTicks']
        expected = end['gameTime']-start['gameTime']
        assert abs(actual-expected) <= 3, f'Low FPS altered simulation cadence: {actual}/{expected}'
        record('low-fps-preserves-game-ticks', end)
        send(fps=60)
    send(enabled=False)
    assert until(lambda s:s.get('particles') == 0, 8)['particles'] == 0
    record('disable-cleans-owned-particles', state())
    send(enabled=True, actors=0, commands=['gamemode spectator BreathQA'])
    send(preview=True)
    time.sleep(2)
    assert state()['particles'] == 0
    record('spectator-suppressed', state())
    send(commands=['gamemode creative BreathQA'])
    send(commands=['effect give BreathQA minecraft:invisibility 20 0 true'], preview=True)
    time.sleep(3)
    assert state()['particles'] == 0
    record('invisibility-suppressed', state())
    send(commands=['effect clear BreathQA','gamemode survival BreathQA','time set night','setblock 0 200 0 minecraft:red_bed[facing=south,part=foot]','setblock 0 200 1 minecraft:red_bed[facing=south,part=head]','tp BreathQA 0.5 200 -1.5'])
    time.sleep(1)
    send(useBlock=[0,200,0], preview=True)
    until(lambda s:s.get('sleeping'), 10)
    time.sleep(2)
    assert state()['particles'] == 0
    record('sleeping-suppressed', state())
    send(wake=True, closeScreen=True, commands=['gamemode creative BreathQA','setblock 0 200 0 minecraft:air','setblock 0 200 1 minecraft:air'])
    send(commands=['effect clear BreathQA','tp BreathQA 0 200 0','fill -2 200 -2 2 203 2 minecraft:water'], preview=True)
    time.sleep(3)
    assert state()['underWater'] and state()['particles'] == 0
    record('underwater-suppressed', state())
    send(commands=['fill -2 200 -2 2 203 2 minecraft:air','tp BreathQA 0 200 0'], drive='sneak', preview=True)
    shot('sneaking-breath', 2, camera='THIRD_PERSON_FRONT')
    send(drive='none')
    until(lambda s:s.get('pose') == 'STANDING', 5)
    time.sleep(1)
    send(commands=['summon minecraft:boat 0 200 0 {Type:oak}','ride BreathQA mount @e[type=minecraft:boat,limit=1,sort=nearest]'], preview=True)
    until(lambda s:s.get('riding'), 10)
    shot('riding-breath', 2)
    send(commands=['ride BreathQA dismount','kill @e[type=minecraft:boat]'])
    send(drive='none', commands=['gamemode survival BreathQA','kill BreathQA'])
    time.sleep(3)
    assert not state()['alive'] and state()['particles'] == 0
    record('death-suppressed', state())
    send(respawn=True)
    until(lambda s:s.get('alive'), 20)
    send(commands=['gamemode creative BreathQA','tp BreathQA 0 200 0'], closeScreen=True)
    shot('respawn-restores-breath', 2, preview=True)
    send(config=True, guiScale=3)
    shot('settings-scale3')
    send(guiScale=2, closeScreen=True)
    address = os.environ.get('BREATH_FOG_QA_SERVER')
    if address:
        send(connect=address)
        until(lambda s:s.get('world') and s.get('biome') == 'minecraft:snowy_plains', 90)
        shot('unmodded-server-breath', 2, preview=True, camera='THIRD_PERSON_FRONT')
    send(disconnect=True)
    until(lambda s:not s.get('world') and s.get('particles') == 0, 15)
    record('disconnect-cleanup', state())
    send(stop=True)
    process.wait(timeout=40)
    if process.returncode != 0:
        raise RuntimeError(f'Nonzero exit {process.returncode}')
    backend = next((line.strip() for line in (output/'console.log').read_text().splitlines() if 'Using graphics backend' in line or 'OpenGL renderer' in line or 'OpenGL version' in line), 'Backend unknown')
    (output/'PASS.json').write_text(json.dumps({'loader':loader,'scenarios':results,'driver':backend,'graphicsRenderer':state().get('graphicsRenderer'),'graphicsVersion':state().get('graphicsVersion'),'display':'private WSL Xvfb','mods':[p.name for p in (client/'mods').glob('*.jar')]}, indent=2))
finally:
    if process.poll() is None:
        os.killpg(process.pid, signal.SIGTERM)
        try:
            process.wait(timeout=25)
        except subprocess.TimeoutExpired:
            os.killpg(process.pid, signal.SIGKILL)
    log.close()
