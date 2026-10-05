"""Owned offline loopback vanilla server for isolated client compatibility checks.

Arguments: vanilla server JAR, previously accepted EULA file, fresh output folder.
Send 'stop' on stdin when finished. No mod is installed on the server.
"""
import json
import pathlib
import shutil
import socket
import subprocess
import sys

jar, eula, output = map(pathlib.Path, sys.argv[1:])
if 'eula=true' not in eula.read_text():
    raise SystemExit('The existing test EULA is not accepted')
output.mkdir(parents=True, exist_ok=False)
shutil.copy2(jar, output/'server.jar')
shutil.copy2(eula, output/'eula.txt')
with socket.socket() as probe:
    probe.bind(('127.0.0.1', 0))
    port=probe.getsockname()[1]
generator={'biome':'minecraft:snowy_plains','layers':[{'block':'minecraft:bedrock','height':1},{'block':'minecraft:stone','height':2},{'block':'minecraft:snow_block','height':1}], 'structures':{}}
(output/'server.properties').write_text('\n'.join(['server-ip=127.0.0.1',f'server-port={port}','online-mode=false','enforce-secure-profile=false','white-list=false','enforce-whitelist=false','gamemode=creative','difficulty=peaceful','max-players=4','view-distance=5','simulation-distance=5','level-type=minecraft:flat','generator-settings='+json.dumps(generator)]))
(output/'address.txt').write_text(f'127.0.0.1:{port}')
print(f'Vanilla QA server: 127.0.0.1:{port}',flush=True)
with (output/'console.log').open('w') as log:
    subprocess.run(['nice','-n','10','taskset','-c','2,3','java','-Xms256M','-Xmx1G','-XX:ActiveProcessorCount=2','-jar','server.jar','nogui'],cwd=output,stdin=sys.stdin,stdout=log,stderr=subprocess.STDOUT,check=True)
