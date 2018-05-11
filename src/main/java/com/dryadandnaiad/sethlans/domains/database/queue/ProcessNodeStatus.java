package com.dryadandnaiad.sethlans.domains.database.queue;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created Mario Estrella on 5/11/2018.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: Sethlans
 */
@Data
@AllArgsConstructor
public class ProcessNodeStatus {
    private String queueUUID;
    private boolean accepted;
}
