package com.dryadandnaiad.sethlans.forms.subclasses;

import com.dryadandnaiad.sethlans.domains.hardware.GPUDevice;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import lombok.Data;

import java.util.List;

/**
 * Created Mario Estrella on 2/23/18.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Data
public class SetupNode {
    private ComputeType computeMethod;
    private int cores;
    private boolean gpuEmpty;
    private List<GPUDevice> selectedGPUs;
}
