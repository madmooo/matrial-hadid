"""Run once for each completed app change: python tools/bump-version.py [patch|minor|major]."""
from pathlib import Path
import sys

path = Path(__file__).resolve().parent.parent / 'version.properties'
props = dict(line.split('=', 1) for line in path.read_text().splitlines() if '=' in line)
major, minor, patch = map(int, props['versionName'].split('.'))
kind = sys.argv[1] if len(sys.argv) > 1 else 'patch'
if kind == 'patch': patch += 1
elif kind == 'minor': minor, patch = minor + 1, 0
elif kind == 'major': major, minor, patch = major + 1, 0, 0
else: raise SystemExit('Choose patch, minor or major')
name = f'{major}.{minor}.{patch}'
path.write_text(f'versionName={name}\nversionCode={int(props["versionCode"]) + 1}\n', encoding='utf-8')
print(name)
