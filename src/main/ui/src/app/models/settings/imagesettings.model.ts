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

import {ImageOutputFormat} from "../../enums/imageoutputformat.enum";

/**
 * File created by Mario Estrella on 4/13/2022
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans_ui
 */
export class ImageSettings {
  resolutionX: number;
  resolutionY: number;
  resPercentage: number;
  imageOutputFormat: ImageOutputFormat;
  imageZipFileLocation: string;

  constructor() {
    this.resolutionX = 0;
    this.resolutionY = 0;
    this.resPercentage = 0;
    this.imageOutputFormat = ImageOutputFormat.PNG;
    this.imageZipFileLocation = ''
  }

  setImageSettings(obj: any) {
    this.resolutionX = obj.resolutionX;
    this.resolutionY = obj.resolutionY;
    this.resPercentage = obj.resPercentage;
    this.imageOutputFormat = obj.imageOutputFormat;
    this.imageZipFileLocation = obj.imageZipFileLocation;
  }

}