/*
 * Copyright (c) 2022 Dryad and Naiad Software LLC
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
 */

import {VideoCodec} from "../../enums/videocodec.enum";
import {PixelFormat} from "../../enums/pixelformat.enum";
import {VideoOutputFormat} from "../../enums/videooutputformat.enum";
import {VideoQuality} from "../../enums/videoquality.enum";

/**
 * File created by Mario Estrella on 4/13/2022
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans_ui
 */
export class VideoSettings {
  frameRate: number;
  codec: VideoCodec;
  pixelFormat: PixelFormat;
  videoOutputFormat: VideoOutputFormat;
  videoQuality: VideoQuality;
  videoFileLocation: string;

  constructor() {
    this.frameRate = 30;
    this.codec = VideoCodec.LIBX264;
    this.pixelFormat = PixelFormat.YUV420P;
    this.videoOutputFormat = VideoOutputFormat.MP4;
    this.videoQuality = VideoQuality.LOW_X264;
    this.videoFileLocation = '';
  }

  setVideoSettings(obj: any) {
    this.frameRate = obj.frameRate;
    this.codec = obj.codec;
    this.pixelFormat = obj.pixelFormat;
    this.videoOutputFormat = obj.videoOutputFormat;
    this.videoQuality = obj.videoQuality;
    this.videoFileLocation = obj.videoFileLocation;
  }
}
