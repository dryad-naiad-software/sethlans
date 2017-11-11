package com.dryadandnaiad.sethlans.commands;

import java.util.List;

/**
 * Created Mario Estrella on 11/7/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public class ScanForm {

    private List<Integer> sethlansNodeId;


    public List<Integer> getSethlansNodeId() {
        return sethlansNodeId;
    }

    public void setSethlansNodeId(List<Integer> sethlansNodeId) {
        this.sethlansNodeId = sethlansNodeId;
    }

    @Override
    public String toString() {
        return "ScanForm{" +
                "sethlansNodeId=" + sethlansNodeId +
                '}';
    }
}
