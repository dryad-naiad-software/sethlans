package com.dryadandnaiad.sethlans.services.config;

import com.dryadandnaiad.sethlans.enums.SethlansConfigKeys;
import com.dryadandnaiad.sethlans.forms.subclasses.SetupNode;
import org.springframework.stereotype.Service;

import static com.dryadandnaiad.sethlans.utils.SethlansUtils.getGPUDeviceString;
import static com.dryadandnaiad.sethlans.utils.SethlansUtils.writeProperty;

/**
 * Created Mario Estrella on 3/5/18.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class UpdateComputeServiceImpl implements UpdateComputeService {
    @Override
    public boolean saveComputeSettings(SetupNode setupNode) {
        writeProperty(SethlansConfigKeys.COMPUTE_METHOD, setupNode.getComputeMethod().toString());
        switch (setupNode.getComputeMethod()) {
            case CPU:
                writeProperty(SethlansConfigKeys.CPU_CORES, setupNode.getCores().toString());
                writeProperty(SethlansConfigKeys.TILE_SIZE_CPU, setupNode.getTileSizeCPU().toString());
                writeProperty(SethlansConfigKeys.GPU_DEVICE, "");
                return true;
            case GPU:
                writeProperty(SethlansConfigKeys.TILE_SIZE_GPU, setupNode.getTileSizeGPU().toString());
                writeProperty(SethlansConfigKeys.GPU_DEVICE, getGPUDeviceString(setupNode));
                writeProperty(SethlansConfigKeys.CPU_CORES, "");
                return true;

            case CPU_GPU:
                writeProperty(SethlansConfigKeys.CPU_CORES, setupNode.getCores().toString());
                writeProperty(SethlansConfigKeys.TILE_SIZE_CPU, setupNode.getTileSizeCPU().toString());
                writeProperty(SethlansConfigKeys.TILE_SIZE_GPU, setupNode.getTileSizeGPU().toString());
                writeProperty(SethlansConfigKeys.GPU_DEVICE, getGPUDeviceString(setupNode));
                return true;

        }
        return false;
    }


}
