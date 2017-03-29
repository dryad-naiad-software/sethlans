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

package com.dryadandnaiad.sethlans.controllers;

import com.dryadandnaiad.sethlans.commands.ProjectForm;
import com.dryadandnaiad.sethlans.domains.blender.BlenderZipEntity;
import com.dryadandnaiad.sethlans.services.database.BlenderZipService;
import com.dryadandnaiad.sethlans.services.storage.WebUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

/**
 * Created Mario Estrella on 3/24/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Controller
@Profile({"SERVER", "BOTH"})
public class ProjectController {
    private static final Logger LOG = LoggerFactory.getLogger(ProjectController.class);
    private BlenderZipService blenderZipService;
    private List<BlenderZipEntity> availableBlenderBinaries;
    private WebUploadService webUploadService;

    @Autowired
    public void setWebUploadService(WebUploadService webUploadService) {
        this.webUploadService = webUploadService;
    }

    @Autowired
    public void setBlenderZipService(BlenderZipService blenderZipService) {
        this.blenderZipService = blenderZipService;
    }

    @RequestMapping("/project")
    public String getPage() {
        return "project/project_list";
    }

    @RequestMapping("/project/new")
    public String newProject(Model model) {
        model.addAttribute("projectForm", new ProjectForm());
        return "project/project_form";
    }

    @RequestMapping(value = "/project/new", method = RequestMethod.POST)
    public String newProjectDetails(final @Valid @ModelAttribute("projectForm") ProjectForm projectForm, BindingResult bindingResult, @RequestParam("projectFile") MultipartFile projectFile) {
        webUploadService.store(projectFile);
        availableBlenderBinaries = (List<BlenderZipEntity>) blenderZipService.listAll();
        projectForm.setAvailableBlenderBinaries(availableBlenderBinaries);
        LOG.debug(projectForm.toString());
        return "project/project_form";
    }
}
