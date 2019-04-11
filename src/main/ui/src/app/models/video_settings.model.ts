/*
 * Copyright (c) 2019 Dryad and Naiad Software LLC
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

import {RenderOutputFormat} from '../enums/render_output_format.enum';
import {VideoCodec} from '../enums/video_codec.enum';
import {PixelFormat} from '../enums/pixel_format.enum';
import {VideoQuality} from '../enums/video_quality.enum';

export class VideoSettings {
  frameRate: string;
  codec: VideoCodec;
  pixelFormat: PixelFormat;
  videoOutputFormat: RenderOutputFormat;
  videoQuality: VideoQuality;
}
