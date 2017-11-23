/*
 * Copyright (c) 2017 Dryad and Naiad Software LLC.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

package com.dryadandnaiad.sethlans.domains.blender;

import com.dryadandnaiad.sethlans.enums.BlenderEngine;

/**
 * Created Mario Estrella on 3/30/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public class BlendFile {
    private String sceneName;
    private BlenderEngine engine;
    private int frameStart;
    private int frameEnd;
    private int frameStep;
    private int resPercent;
    private int resolutionX;
    private int resolutionY;
    private String cameraName;
    private int cyclesSamples;

    public BlendFile(String sceneName, BlenderEngine engine, int frameStart, int frameEnd, int frameStep, int resPercent, int resolutionX, int resolutionY, String cameraName, int cyclesSamples) {
        this.sceneName = sceneName;
        this.engine = engine;
        this.frameStart = frameStart;
        this.frameEnd = frameEnd;
        this.frameStep = frameStep;
        this.resPercent = resPercent;
        this.resolutionX = resolutionX;
        this.resolutionY = resolutionY;
        this.cameraName = cameraName;
        this.cyclesSamples = cyclesSamples;
    }

    public String getSceneName() {
        return sceneName;
    }

    public void setSceneName(String sceneName) {
        this.sceneName = sceneName;
    }

    public BlenderEngine getEngine() {
        return engine;
    }

    public void setEngine(BlenderEngine engine) {
        this.engine = engine;
    }

    public int getFrameStart() {
        return frameStart;
    }

    public void setFrameStart(int frameStart) {
        this.frameStart = frameStart;
    }

    public int getFrameEnd() {
        return frameEnd;
    }

    public void setFrameEnd(int frameEnd) {
        this.frameEnd = frameEnd;
    }

    public int getFrameStep() {
        return frameStep;
    }

    public void setFrameStep(int frameStep) {
        this.frameStep = frameStep;
    }

    public int getResPercent() {
        return resPercent;
    }

    public void setResPercent(int resPercent) {
        this.resPercent = resPercent;
    }

    public int getResolutionX() {
        return resolutionX;
    }

    public void setResolutionX(int resolutionX) {
        this.resolutionX = resolutionX;
    }

    public int getResolutionY() {
        return resolutionY;
    }

    public void setResolutionY(int resolutionY) {
        this.resolutionY = resolutionY;
    }

    public String getCameraName() {
        return cameraName;
    }

    public void setCameraName(String cameraName) {
        this.cameraName = cameraName;
    }

    public int getCyclesSamples() {
        return cyclesSamples;
    }

    public void setCyclesSamples(int cyclesSamples) {
        this.cyclesSamples = cyclesSamples;
    }

    @Override
    public String toString() {
        return "BlendFile{" +
                "sceneName='" + sceneName + '\'' +
                ", engine=" + engine +
                ", frameStart=" + frameStart +
                ", frameEnd=" + frameEnd +
                ", frameStep=" + frameStep +
                ", resPercent=" + resPercent +
                ", resolutionX=" + resolutionX +
                ", resolutionY=" + resolutionY +
                ", cameraName='" + cameraName + '\'' +
                ", cyclesSamples=" + cyclesSamples +
                '}';
    }
}
