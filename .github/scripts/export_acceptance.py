"""Copy lightweight QA evidence, without worlds, game libraries or helper JARs.

Arguments: owned Linux staging directory, output evidence directory, final build log.
Existing evidence is preserved; matching files from this stage are refreshed.
"""
import pathlib
import shutil
import sys

stage, output = map(pathlib.Path, sys.argv[1:3])
output.mkdir(parents=True, exist_ok=True)

def copy_files(source, destination, suffixes):
    if not source.exists():
        return
    destination.mkdir(parents=True, exist_ok=True)
    for file in source.iterdir():
        if file.is_file() and file.suffix in suffixes:
            shutil.copy2(file, destination / file.name)

copy_files(stage, output / 'build/history', {'.log'})
shutil.copy2(stage / sys.argv[3], output / 'build/build-log.txt')
shutil.copytree(stage / 'common/build/test-results/test', output / 'build/test-results', dirs_exist_ok=True)
shutil.copytree(stage / 'common/build/reports/tests/test', output / 'build/test-report', dirs_exist_ok=True)
copy_files(stage / 'playtest', output / 'playtest', {'.log'})
for profile in (stage / 'playtest').iterdir():
    if profile.is_dir():
        target = output / 'playtest' / profile.name
        copy_files(profile, target, {'.log', '.json', '.png'})
        copy_files(profile / 'client/qa', target / 'qa', {'.json', '.png'})
        copy_files(profile / 'client/crash-reports', target / 'crash-reports', {'.txt'})
copy_files(stage / 'vanilla-server-1', output / 'vanilla-server', {'.log', '.txt', '.properties'})
print(f'Exported build, client and vanilla-server evidence to {output}')
