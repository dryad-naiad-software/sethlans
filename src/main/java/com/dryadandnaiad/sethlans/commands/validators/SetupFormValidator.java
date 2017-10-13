/*
 * Copyright (c) 2017 Dryad and Naiad Software LLC
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

import com.dryadandnaiad.sethlans.commands.SetupForm;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.enums.SetupProgress;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Created Mario Estrella on 3/17/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Component
public class SetupFormValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return SetupForm.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {

        SetupForm setupForm = (SetupForm) target;

        if (StringUtils.containsAny(setupForm.getUsername(), "~`!@#$%^&*()-_=+[{]}\\|;:\'\",<.>/?")) {
            errors.rejectValue("username", "form.usernameInvalidChar", "The following symbols are not supported. ~`!@#$%^&*()-_=+[{]}\\|;:\'\",<.>/?");
        }



        if (!setupForm.getPassword().equals(setupForm.getPasswordConfirm())) {
            errors.rejectValue("password", "form.passwordsDontMatch", "Passwords Don't Match");
            errors.rejectValue("passwordConfirm", "form.passwordsDontMatch", "Passwords Don't Match");
        }

        if (setupForm.getPassword().length() < 8) {
            errors.rejectValue("password", "form.passwordTooShort", "Password Too Short");
        }

        if (setupForm.getSelectedMethod().equals(ComputeType.GPU) || setupForm.getSelectedMethod().equals(ComputeType.CPU_GPU)) {
            if (setupForm.getSelectedGPUId().isEmpty() && setupForm.getProgress().equals(SetupProgress.SUMMARY)) {
                errors.rejectValue("selectedGPUId", "form.gpuErrors", "One GPU must be selected for rendering");
            }
        }


    }
}
