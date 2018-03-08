package com.dryadandnaiad.sethlans.services.imagemagick;

import org.springframework.stereotype.Service;

/**
 * Created Mario Estrella on 3/7/18.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class ImageMagickSetupServiceImpl implements ImageMagickSetupService {
    @Override
    public boolean installImageMagick(String binaryDir) {
        return false;
    }
}
