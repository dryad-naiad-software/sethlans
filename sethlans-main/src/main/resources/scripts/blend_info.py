#!/usr/bin/env python3
import json
import sys
from blender_asset_tracer import blendfile
from blender_asset_tracer.blendfile import iterators
from pathlib import Path

if len(sys.argv) != 2:
    print(f'Usage: {sys.argv[0]} somefile.blend', file=sys.stderr)
    sys.exit(1)

bf_path = Path(sys.argv[1])
bf = blendfile.open_cached(bf_path)

# Get the first window manager (there is probably exactly one).
window_managers = bf.find_blocks_from_code(b'WM')
assert window_managers, 'The Blend file has no window manager'
window_manager = window_managers[0]

# Get the scene from the first window.
windows = window_manager.get_pointer((b'windows', b'first'))
for window in iterators.listbase(windows):
    scene = window.get_pointer(b'scene')
    break

# BAT can only return simple values, so it can't return the embedded
# struct 'r'. 'r.engine' is a simple string, though.
engine = scene[b'r', b'engine'].decode('utf8')
frame_start = scene[b'r', b'sfra']
frame_end = scene[b'r', b'efra']
frame_skip = scene[b'r', b'frame_step']
res_x = scene[b'r', b'xsch']
res_y = scene[b'r', b'ysch']
res_percent = scene[b'r', b'size']

render_info = {
    'engine': engine,
    'frame_start': frame_start,
    'frame_end': frame_end,
    'frame_skip': frame_skip,
    'res_percent': res_percent,
    'resolution_x': res_x,
    'resolution_y': res_y,
}

json.dump(render_info, sys.stdout, indent=4, sort_keys=False)
print()
