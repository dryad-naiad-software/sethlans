package com.dryadandnaiad.sethlans.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created Mario Estrella on 2/21/18.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public class AMDGFXID {

    private static final Map<String, String> amdIds;

    static {
        amdIds = new HashMap<String, String>();
        amdIds.put("gfx804", "Radeon RX 550 Series");
    }

    public static String getDeviceName(String deviceId) {
        return amdIds.get(deviceId);
    }


}
