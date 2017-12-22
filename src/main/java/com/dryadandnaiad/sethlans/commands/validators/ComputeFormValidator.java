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

package com.dryadandnaiad.sethlans.commands.validators;

import com.dryadandnaiad.sethlans.commands.ComputeForm;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Created Mario Estrella on 12/21/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Component
public class ComputeFormValidator implements Validator {
    @Override
    public boolean supports(Class<?> aClass) {
        return ComputeForm.class.equals(aClass);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ComputeForm computeForm = (ComputeForm) target;

        if (computeForm.getSelectedCompute().equals(ComputeType.GPU) || computeForm.getSelectedCompute().equals(ComputeType.CPU_GPU)) {
            if (computeForm.getSelectedGPUIds().isEmpty()) {
                errors.rejectValue("selectedGPUIds", "form.gpuErrors", "One GPU must be selected for rendering");
            }
        }

    }
}
