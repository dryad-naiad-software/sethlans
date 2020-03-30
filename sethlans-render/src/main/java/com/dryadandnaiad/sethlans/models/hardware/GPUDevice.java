package com.dryadandnaiad.sethlans.models.hardware;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created Mario Estrella on 3/29/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@NoArgsConstructor
@Data
public class GPUDevice {
    private String model;
    private long memory; // in B
    private int rating;
    private boolean openCL;
    private boolean cuda;
    private String deviceID;
}
