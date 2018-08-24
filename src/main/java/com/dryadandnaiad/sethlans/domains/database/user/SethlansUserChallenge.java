package com.dryadandnaiad.sethlans.domains.database.user;

import lombok.Data;

import javax.persistence.Embeddable;
import javax.persistence.Transient;

/**
 * Created Mario Estrella on 8/24/18.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: Sethlans
 */
@Embeddable
@Data
public class SethlansUserChallenge {
    private String challenge;
    private String response;
    @Transient
    private boolean responseUpdated;

}
