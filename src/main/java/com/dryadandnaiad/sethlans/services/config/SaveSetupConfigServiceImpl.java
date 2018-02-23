package com.dryadandnaiad.sethlans.services.config;

import com.dryadandnaiad.sethlans.forms.SetupForm;
import com.dryadandnaiad.sethlans.services.database.BlenderBinaryDatabaseService;
import org.springframework.stereotype.Service;

/**
 * Created Mario Estrella on 2/23/18.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class SaveSetupConfigServiceImpl implements SaveSetupConfigService {
    private BlenderBinaryDatabaseService blenderBinaryDatabaseService;

    @Override
    public void saveSettings(SetupForm setupForm) {

    }
}
