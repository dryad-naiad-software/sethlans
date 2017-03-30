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
import sys

import blendfile

filepath = sys.argv[-1]


def listbase_iter(data, struct, listbase):
    element = data.get_pointer((struct, listbase, b'first'))
    while element is not None:
        yield element
        element = element.get_pointer(b'next')


def idprop_group_iter(idprops, ):
    return listbase_iter(idprops, b'data', b'group')


def views_iter(scene):
    """Return an iterator for all views of scene"""
    return listbase_iter(scene, b'r', b'views')


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


def get_frame_current(scene):
    return scene.get((b'r', b'cfra'))


def get_resolution_x(scene):
    return scene.get((b'r', b'xsch'))


def get_resolution_y(scene):
    return scene.get((b'r', b'ysch'))


def get_camera_name(scene):
    camera = scene.get_pointer(b'camera')
    assert camera
    return camera.get((b'id', b'name'))


def get_camera_lens(scene):
    camera = scene.get_pointer(b'camera')
    assert camera
    camera_data = camera.get_pointer(b'data')
    return camera_data.get(b'lens')


def get_views_name_status(scene):
    name_status = []
    for view in views_iter(scene):
        name_status.append((
            view.get(b'name'),
            view.get(b'viewflag'),
        ))
    return name_status


def get_samples(scene):
    # get custom properties
    properties = scene.get_pointer((b'id', b'properties'))

    if properties is None:
        return -1

    # iterate through all the property groups
    for itor in idprop_group_iter(properties):
        if itor.get(b'name') == "cycles":
            for itor2 in idprop_group_iter(itor):
                if itor2.get(b'name') == "samples":
                    return itor2.get((b'data', b'val'))


engine, = query_main_scene(filepath, [get_engine])
resolution_x, resolution_y, res_percent = query_main_scene(filepath, [
    get_resolution_x,
    get_resolution_y, get_resolution_percentage
])
scene_name, = query_main_scene(filepath, [get_name])
camera_name, = query_main_scene(filepath, [
    get_camera_name
])
frame_start, frame_end, frame_current, frame_skip = query_main_scene(filepath, [
    get_frame_start,
    get_frame_end,
    get_frame_current, get_frame_skip,
])
views_data, = query_main_scene(filepath, [get_views_name_status])
samples, = query_main_scene(filepath, [get_samples])

print(scene_name, ",", engine, ",", frame_start, ",", frame_end, ",", frame_skip, ",", res_percent, ",", resolution_x,
      ",", resolution_y,
      ",", camera_name, ",", samples)
