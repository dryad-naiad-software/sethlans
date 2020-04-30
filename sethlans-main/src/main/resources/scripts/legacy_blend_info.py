#!/usr/bin/env python3
"""
This takes a blend file argument and prints out some of its details, eg:
  blend_info.py /path/to/test.blend

    Output:

    Scene name = String
    Engine = String
    Frame Start = Int
    Frame End = Int
    Frame Skip = Int
    Res Percent = Int
    Res X = String
    Res Y = String
    Camera Name = String
    Samples(Only for Cycles) = Int

"""
import json
import legacy_blendfile as blendfile
import sys
from pathlib import Path

if len(sys.argv) != 2:
    print(f'Usage: {sys.argv[0]} somefile.blend', file=sys.stderr)
    sys.exit(1)

filepath = Path(sys.argv[1])


def listbase_iter(data, struct, listbase):
    element = data.get_pointer((struct, listbase, b'first'))
    while element is not None:
        yield element
        element = element.get_pointer(b'next')


def idprop_group_iter(idprops, ):
    return listbase_iter(idprops, b'data', b'group')


def query_main_scene(filepath, callbacks):
    """Return the equivalent to bpy.context.scene"""
    with blendfile.open_blend(filepath) as blend:
        # There is no bpy.context.scene, we get it from the main window
        window_manager = [block for block in blend.blocks if block.code == b'WM'][0]
        window = window_manager.get_pointer(b'winactive')
        screen = window.get_pointer(b'screen')
        scene = screen.get_pointer(b'scene')

        output = []
        for callback in callbacks:
            output.append(callback(scene))
        return output


def get_name(scene):
    return scene.get((b'id', b'name'))


def get_resolution_percentage(scene):
    return scene.get((b'r', b'size'))


def get_engine(scene):
    return scene.get((b'r', b'engine'))


def get_frame_start(scene):
    return scene.get((b'r', b'sfra'))


def get_frame_end(scene):
    return scene.get((b'r', b'efra'))


def get_frame_skip(scene):
    return scene.get((b'r', b'frame_step'))


def get_resolution_x(scene):
    return scene.get((b'r', b'xsch'))


def get_resolution_y(scene):
    return scene.get((b'r', b'ysch'))


engine, = query_main_scene(filepath, [get_engine])
res_x, res_y, res_percent = query_main_scene(filepath, [
    get_resolution_x,
    get_resolution_y, get_resolution_percentage
])
frame_start, frame_end, frame_skip = query_main_scene(filepath, [
    get_frame_start,
    get_frame_end, get_frame_skip,
])

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
