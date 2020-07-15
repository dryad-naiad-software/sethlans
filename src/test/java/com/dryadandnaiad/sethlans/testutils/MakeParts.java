/*
 * Copyright (c) 2020. Dryad and Naiad Software LLC.
 *   This program is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU General Public License
 *   as published by the Free Software Foundation; either version 2
 *   of the License, or (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.dryadandnaiad.sethlans.testutils;

/**
 * File created by Mario Estrella on 5/20/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public class MakeParts {
//    static File BIN_DIRECTORY = new File(SystemUtils.USER_HOME + File.separator + "test-binaries");
//    static File TEST_DIRECTORY = new File(SystemUtils.USER_HOME + File.separator + "testing");
//
//    @BeforeEach
//    void setUp() {
//        TEST_DIRECTORY.mkdirs();
//    }
//
//    @AfterEach
//    void tearDown() {
//        //FileSystemUtils.deleteRecursively(TEST_DIRECTORY);
//    }
//
//    @BeforeAll
//    static void beforeAll() {
//        BIN_DIRECTORY.mkdirs();
//        var version = "2.83.2";
//        var os = QueryUtils.getOS();
//        var download = BlenderUtils.downloadBlenderToServer(version,
//                "resource",
//                BIN_DIRECTORY.toString(),
//                os);
//        BlenderUtils.extractBlender(BIN_DIRECTORY.toString(),
//                os, download.toString(), version);
//    }
//
//    @AfterAll
//    static void afterAll() {
//        FileSystemUtils.deleteRecursively(BIN_DIRECTORY);
//    }
//
//    @Test
//    void createParts() {
//        var file1 = "pavillon_barcelone_v1.2_textures_animation.blend";
//        TestFileUtils.copyTestArchiveToDisk(TEST_DIRECTORY.toString(), "blend_files/" + file1, file1);
//        var version = "2.83.2";
//        var blenderExecutable = BlenderUtils.getBlenderExecutable(BIN_DIRECTORY.toString(), version);
//        var renderTask = makeRenderTask(version, TEST_DIRECTORY + File.separator + file1,
//                ComputeOn.HYBRID, BlenderEngine.CYCLES, blenderExecutable, ImageOutputFormat.PNG);
//        renderTask.setUseParts(true);
//        var coordinates = ImageUtils.configurePartCoordinates(9);
//        var frameInfo = renderTask.getFrameInfo();
//        frameInfo.setPartNumber(1);
//        frameInfo.setPartMinX(coordinates.get(0).getMinX());
//        frameInfo.setPartMaxX(coordinates.get(0).getMaxX());
//        frameInfo.setPartMinY(coordinates.get(0).getMinY());
//        frameInfo.setPartMaxY(coordinates.get(0).getMaxY());
//        renderTask.setFrameInfo(frameInfo);
//        assertThat(BlenderScript.writeRenderScript(renderTask)).isTrue();
//        var result = BlenderUtils.executeRenderTask(renderTask, true);
//        assertThat(result).isNotNull();
//        frameInfo = renderTask.getFrameInfo();
//        frameInfo.setPartMinX(coordinates.get(1).getMinX());
//        frameInfo.setPartMaxX(coordinates.get(1).getMaxX());
//        frameInfo.setPartMinY(coordinates.get(1).getMinY());
//        frameInfo.setPartMaxY(coordinates.get(1).getMaxY());
//        frameInfo.setPartNumber(2);
//        renderTask.setFrameInfo(frameInfo);
//        assertThat(BlenderScript.writeRenderScript(renderTask)).isTrue();
//        result = BlenderUtils.executeRenderTask(renderTask, true);
//        assertThat(result).isNotNull();
//        frameInfo = renderTask.getFrameInfo();
//        frameInfo.setPartMinX(coordinates.get(2).getMinX());
//        frameInfo.setPartMaxX(coordinates.get(2).getMaxX());
//        frameInfo.setPartMinY(coordinates.get(2).getMinY());
//        frameInfo.setPartMaxY(coordinates.get(2).getMaxY());
//        frameInfo.setPartNumber(3);
//        renderTask.setFrameInfo(frameInfo);
//        assertThat(BlenderScript.writeRenderScript(renderTask)).isTrue();
//        result = BlenderUtils.executeRenderTask(renderTask, true);
//        assertThat(result).isNotNull();
//        frameInfo = renderTask.getFrameInfo();
//        frameInfo.setPartMinX(coordinates.get(3).getMinX());
//        frameInfo.setPartMaxX(coordinates.get(3).getMaxX());
//        frameInfo.setPartMinY(coordinates.get(3).getMinY());
//        frameInfo.setPartMaxY(coordinates.get(3).getMaxY());
//        frameInfo.setPartNumber(4);
//        renderTask.setFrameInfo(frameInfo);
//        assertThat(BlenderScript.writeRenderScript(renderTask)).isTrue();
//        result = BlenderUtils.executeRenderTask(renderTask, true);
//        assertThat(result).isNotNull();
//        frameInfo = renderTask.getFrameInfo();
//        frameInfo.setPartMinX(coordinates.get(4).getMinX());
//        frameInfo.setPartMaxX(coordinates.get(4).getMaxX());
//        frameInfo.setPartMinY(coordinates.get(4).getMinY());
//        frameInfo.setPartMaxY(coordinates.get(4).getMaxY());
//        frameInfo.setPartNumber(5);
//        renderTask.setFrameInfo(frameInfo);
//        assertThat(BlenderScript.writeRenderScript(renderTask)).isTrue();
//        result = BlenderUtils.executeRenderTask(renderTask, true);
//        assertThat(result).isNotNull();
//        frameInfo = renderTask.getFrameInfo();
//        frameInfo.setPartMinX(coordinates.get(5).getMinX());
//        frameInfo.setPartMaxX(coordinates.get(5).getMaxX());
//        frameInfo.setPartMinY(coordinates.get(5).getMinY());
//        frameInfo.setPartMaxY(coordinates.get(5).getMaxY());
//        frameInfo.setPartNumber(6);
//        renderTask.setFrameInfo(frameInfo);
//        assertThat(BlenderScript.writeRenderScript(renderTask)).isTrue();
//        result = BlenderUtils.executeRenderTask(renderTask, true);
//        assertThat(result).isNotNull();
//        frameInfo = renderTask.getFrameInfo();
//        frameInfo.setPartMinX(coordinates.get(6).getMinX());
//        frameInfo.setPartMaxX(coordinates.get(6).getMaxX());
//        frameInfo.setPartMinY(coordinates.get(6).getMinY());
//        frameInfo.setPartMaxY(coordinates.get(6).getMaxY());
//        frameInfo.setPartNumber(7);
//        renderTask.setFrameInfo(frameInfo);
//        assertThat(BlenderScript.writeRenderScript(renderTask)).isTrue();
//        result = BlenderUtils.executeRenderTask(renderTask, true);
//        assertThat(result).isNotNull();
//        frameInfo = renderTask.getFrameInfo();
//        frameInfo.setPartMinX(coordinates.get(7).getMinX());
//        frameInfo.setPartMaxX(coordinates.get(7).getMaxX());
//        frameInfo.setPartMinY(coordinates.get(7).getMinY());
//        frameInfo.setPartMaxY(coordinates.get(7).getMaxY());
//        frameInfo.setPartNumber(8);
//        renderTask.setFrameInfo(frameInfo);
//        assertThat(BlenderScript.writeRenderScript(renderTask)).isTrue();
//        result = BlenderUtils.executeRenderTask(renderTask, true);
//        assertThat(result).isNotNull();
//        frameInfo = renderTask.getFrameInfo();
//        frameInfo.setPartMinX(coordinates.get(8).getMinX());
//        frameInfo.setPartMaxX(coordinates.get(8).getMaxX());
//        frameInfo.setPartMinY(coordinates.get(8).getMinY());
//        frameInfo.setPartMaxY(coordinates.get(8).getMaxY());
//        frameInfo.setPartNumber(9);
//        renderTask.setFrameInfo(frameInfo);
//        assertThat(BlenderScript.writeRenderScript(renderTask)).isTrue();
//        result = BlenderUtils.executeRenderTask(renderTask, true);
//        assertThat(result).isNotNull();
//
//
//    }
//
//    private RenderTask makeRenderTask(String version, String blendFile,
//                                      ComputeOn computeOn, BlenderEngine engine, String blenderExecutable, ImageOutputFormat imageOutputFormat) {
//        var taskDir = new File(TEST_DIRECTORY + File.separator + "render");
//        var tileSize = 256;
//        taskDir.mkdirs();
//        var deviceIDs = new ArrayList<String>();
//        deviceIDs.add("CUDA_0");
//        deviceIDs.add("CUDA_1");
//        deviceIDs.add("CUDA_2");
//
//        var scriptInfo = TaskScriptInfo.builder()
//                .blenderEngine(engine)
//                .computeOn(computeOn)
//                .deviceIDs(deviceIDs)
//                .taskResolutionX(1920)
//                .taskResolutionY(1080)
//                .taskResPercentage(100)
//                .taskTileSize(tileSize)
//                .cores(7)
//                .deviceType(DeviceType.CUDA)
//                .samples(1500)
//                .imageOutputFormat(imageOutputFormat)
//                .build();
//        var frameInfo = TaskFrameInfo.builder()
//                .frameNumber(1)
//                .build();
//        return RenderTask.builder()
//                .projectID("2a7f2507-523s-4c82-aab2-64708f833da4")
//                .taskID(UUID.randomUUID().toString())
//                .taskDir(taskDir.toString())
//                .blenderVersion(version)
//                .blenderExecutable(blenderExecutable)
//                .scriptInfo(scriptInfo)
//                .frameInfo(frameInfo)
//                .isBenchmark(false)
//                .taskBlendFile(blendFile)
//                .projectName("A Sample Project")
//                .build();
//    }
}
