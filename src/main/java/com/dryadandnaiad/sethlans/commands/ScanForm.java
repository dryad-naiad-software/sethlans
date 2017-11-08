package com.dryadandnaiad.sethlans.commands;

import java.util.List;

/**
 * Created Mario Estrella on 11/7/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public class ScanForm {

    private List<Integer> sethlansNodeIdNum;


    public List<Integer> getSethlansNodeIdNum() {
        return sethlansNodeIdNum;
    }

    public void setSethlansNodeIdNum(List<Integer> sethlansNodeIdNum) {
        this.sethlansNodeIdNum = sethlansNodeIdNum;
    }

    @Override
    public String toString() {
        return "ScanForm{" +
                "sethlansNodeIdNum=" + sethlansNodeIdNum +
                '}';
    }
}
