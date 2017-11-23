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

package com.dryadandnaiad.sethlans.controllers;

import com.dryadandnaiad.sethlans.commands.SetupForm;
import com.dryadandnaiad.sethlans.enums.SetupProgress;
import com.dryadandnaiad.sethlans.services.config.SaveSetupConfigService;
import com.dryadandnaiad.sethlans.services.system.PythonSetupService;
import com.dryadandnaiad.sethlans.services.system.SethlansManagerService;
import com.dryadandnaiad.sethlans.utils.BlenderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import javax.validation.Valid;
import java.util.List;

/**
 * Created Mario Estrella on 3/17/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Controller
@Profile("SETUP")
@RequestMapping("/setup")
@SessionAttributes("setupForm")
public class SetupController extends AbstractSethlansController {
    @Value("${sethlans.firsttime}")
    private boolean firstTime;

    private String modeName = "SETUP";

    private static final Logger LOG = LoggerFactory.getLogger(SetupController.class);
    private Validator setupFormValidator;
    private SethlansManagerService sethlansManagerService;
    private SaveSetupConfigService saveSetupConfigService;
    private PythonSetupService pythonSetupService;

    @RequestMapping
    public String getStartPage(final Model model) {
        if (firstTime) {
            model.addAttribute("setupForm", new SetupForm());
            return "setup";
        }
        return "redirect:/";

    }

    @Override
    public String getUserName() {
        return "username";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String processPage(final @Valid @ModelAttribute("setupForm") SetupForm setupForm, BindingResult bindingResult) throws InterruptedException {
        LOG.debug("Current progress \n" + setupForm.toString());
        setupFormValidator.validate(setupForm, bindingResult);

        if (bindingResult.hasErrors()) {
            LOG.debug(bindingResult.toString());
            setupForm.setProgress(setupForm.getPrevious());
        }

        if (setupForm.getProgress() == SetupProgress.FINISHED) {
            switch (setupForm.getMode()) {
                case SERVER:
                    saveSetupConfigService.saveServerSettings(setupForm);
                    break;
                case DUAL:
                    saveSetupConfigService.saveDualSettings(setupForm);
                    break;
                case NODE:
                    saveSetupConfigService.saveNodeSettings(setupForm);
                    break;
                default:
                    System.exit(1);
            }
            saveSetupConfigService.saveSethlansSettings(setupForm);
            saveSetupConfigService.wizardCompleted(setupForm);
            LOG.debug("Downloading and Installing Python");
            pythonSetupService.installPython(setupForm.getBinDirectory());
            pythonSetupService.setupScripts(setupForm.getScriptsDirectory());
            LOG.info("Setup complete complete. Restarting Sethlans");
            sethlansManagerService.restart();
            return "setup_finished";
        }

        return "setup";
    }

    @ModelAttribute("blender_versions")
    public List<String> getBlenderVersions() {
        return BlenderUtils.listVersions();
    }

    @Autowired
    public void setSaveSetupConfigService(SaveSetupConfigService saveSetupConfigService) {
        this.saveSetupConfigService = saveSetupConfigService;
    }

    @Autowired
    public void setSethlansManagerService(SethlansManagerService sethlansManagerService) {
        this.sethlansManagerService = sethlansManagerService;
    }

    @Autowired
    @Qualifier("setupFormValidator")
    public void setSetupFormValidator(Validator setupFormValidator) {
        this.setupFormValidator = setupFormValidator;
    }

    @Autowired
    public void setPythonSetupService(PythonSetupService pythonSetupService) {
        this.pythonSetupService = pythonSetupService;
    }

    @Override
    @ModelAttribute("sethlansmode")
    public String getMode() {
        return modeName;
    }
}
