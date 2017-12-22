/*
 * Copyright (c) 2017 Dryad and Naiad Software LLC.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

package com.dryadandnaiad.sethlans.commands;

import com.dryadandnaiad.sethlans.enums.ComputeType;

import java.util.List;

/**
 * Created Mario Estrella on 12/21/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public class ComputeForm {

    private ComputeType selectedCompute;
    private Integer selectedCores;
    private List<Integer> selectedGPUIds;

    public ComputeType getSelectedCompute() {
        return selectedCompute;
    }

    public void setSelectedCompute(ComputeType selectedCompute) {
        this.selectedCompute = selectedCompute;
    }

    public Integer getSelectedCores() {
        return selectedCores;
    }

    public void setSelectedCores(Integer selectedCores) {
        this.selectedCores = selectedCores;
    }

    public List<Integer> getSelectedGPUIds() {
        return selectedGPUIds;
    }

    public void setSelectedGPUIds(List<Integer> selectedGPUIds) {
        this.selectedGPUIds = selectedGPUIds;
    }

    @Override
    public String toString() {
        return "ComputeForm{" +
                "selectedCompute=" + selectedCompute +
                ", selectedCores=" + selectedCores +
                ", selectedGPUIds=" + selectedGPUIds +
                '}';
    }
}