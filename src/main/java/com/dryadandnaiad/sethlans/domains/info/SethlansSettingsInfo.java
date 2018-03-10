package com.dryadandnaiad.sethlans.domains.info;

import com.dryadandnaiad.sethlans.enums.SethlansMode;
import lombok.Data;

/**
 * Created Mario Estrella on 3/2/18.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Data
public class SethlansSettingsInfo {
    private String httpsPort;
    private String sethlansIP;
    private String logFile;
    private String projectDir;
    private String blenderDir;
    private String binDir;
    private String rootDir;
    private String benchmarkDir;
    private String tempDir;
    private String scriptsDir;
    private String cacheDir;
    private SethlansMode mode;
}
