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

package com.dryadandnaiad.sethlans.domains.blender;

import com.dryadandnaiad.sethlans.enums.PixelFormat;
import com.dryadandnaiad.sethlans.enums.VideoCodec;
import com.dryadandnaiad.sethlans.enums.VideoOutputFormat;
import com.dryadandnaiad.sethlans.enums.VideoQuality;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Embeddable;

/**
 * Created Mario Estrella on 4/8/2019.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Embeddable
@Data
@EqualsAndHashCode
public class VideoSettings {
    private String frameRate;
    private VideoCodec codec;
    private PixelFormat pixelFormat;
    private VideoOutputFormat videoOutputFormat;
    private VideoQuality videoQuality;
}
