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
import com.dryadandnaiad.sethlans.converters.BlenderProjectToProjectForm;
import com.dryadandnaiad.sethlans.domains.blender.BlenderBinary;
import com.dryadandnaiad.sethlans.domains.blender.BlenderProject;
import com.dryadandnaiad.sethlans.enums.ProjectFormProgress;
import com.dryadandnaiad.sethlans.enums.SethlansMode;
import com.dryadandnaiad.sethlans.services.blender.BlenderParseBlendFileService;
import com.dryadandnaiad.sethlans.services.database.BlenderBinaryService;
import com.dryadandnaiad.sethlans.services.database.BlenderProjectService;
import com.dryadandnaiad.sethlans.services.database.UserService;
import com.dryadandnaiad.sethlans.services.storage.WebUploadService;
import com.dryadandnaiad.sethlans.utils.SethlansUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created Mario Estrella on 3/24/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Controller
@Profile({"SERVER", "DUAL"})
public class ProjectController {
    private static final Logger LOG = LoggerFactory.getLogger(ProjectController.class);
    private BlenderBinaryService blenderBinaryService;
    private List<BlenderBinary> availableBlenderBinaries;
    private WebUploadService webUploadService;
    private BlenderParseBlendFileService blenderParseBlendFileService;
    private BlenderProjectService blenderProjectService;

    private BlenderProjectToProjectForm blenderProjectToProjectForm;

    @Value("${sethlans.tempDir}")
    private String temp;

    @Value("${sethlans.mode}")
    private SethlansMode mode;
    private UserService userService;

    @RequestMapping("/project")
    public String getPage(Model model) {
        getAvailableBlenderBinaries();
        model.addAttribute("availableBlenderBinaries", availableBlenderBinaries);
        model.addAttribute("projects", blenderProjectService.listAll());
        return "project/project_list";
    }

    @RequestMapping("/project/new")
    public String newProject(Model model) {
        model.addAttribute("projectForm", new ProjectForm());
        return "project/project_form";
    }

    @RequestMapping("/project/view/{id}")
    public String getProject(@PathVariable Integer id, Model model) {
        model.addAttribute("project", blenderProjectService.getById(id));
        return "project/project_view";
    }

    @RequestMapping("/project/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        getAvailableBlenderBinaries();
        BlenderProject project = blenderProjectService.getById(id);
        ProjectForm projectForm = blenderProjectToProjectForm.convert(project);
        projectForm.setAvailableBlenderBinaries(availableBlenderBinaries);
        model.addAttribute("projectForm", projectForm);
        LOG.debug(projectForm.toString());
        return "project/project_form";
    }

    @RequestMapping(value = "/project/new", method = RequestMethod.POST)
    public String newProjectDetails(final @Valid @ModelAttribute("projectForm") ProjectForm projectForm, BindingResult bindingResult, @RequestParam("projectFile") MultipartFile projectFile) {
        UUID uploadTag = UUID.randomUUID();
        webUploadService.store(projectFile, uploadTag.toString());
        getAvailableBlenderBinaries();
        projectForm.setUploadedFile(projectFile.getOriginalFilename());
        projectForm.setFileLocation(temp + uploadTag + "-" + projectFile.getOriginalFilename());
        projectForm.setBlendFile(blenderParseBlendFileService.parseBlendFile(projectForm.getFileLocation()));
        projectForm.setAvailableBlenderBinaries(availableBlenderBinaries);
        projectForm.populateForm();
        LOG.debug(projectForm.toString());
        return "project/project_form";
    }

    @RequestMapping(value = "/project/summary", method = RequestMethod.POST)
    public String projectSummary(final @Valid @ModelAttribute("projectForm") ProjectForm projectForm, BindingResult bindingResult) {
        LOG.debug(projectForm.toString());
        if (projectForm.getProgress() == ProjectFormProgress.FINISHED) {
            LOG.debug("FINISHED");
            BlenderProject savedProject = blenderProjectService.saveOrUpdateProjectForm(projectForm);

            return "redirect:/project/view/" + savedProject.getId();
        }
        return "project/project_view";

    }

    private void getAvailableBlenderBinaries() {
        availableBlenderBinaries = new ArrayList<>();
        List<BlenderBinary> databaseList = (List<BlenderBinary>) blenderBinaryService.listAll();
        for (BlenderBinary blenderBinary : databaseList) {
            if (blenderBinary.isDownloaded()) {
                availableBlenderBinaries.add(blenderBinary);
            }
        }
    }

    @ModelAttribute("version")
    public String getVersion() {
        return SethlansUtils.getVersion();
    }

    @ModelAttribute("sethlansmode")
    public String getMode() {
        return mode.toString();
    }

    @ModelAttribute("username")
    public String getUserName() {
        return userService.getById(1).getUsername();
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setWebUploadService(WebUploadService webUploadService) {
        this.webUploadService = webUploadService;
    }

    @Autowired
    public void setBlenderBinaryService(BlenderBinaryService blenderBinaryService) {
        this.blenderBinaryService = blenderBinaryService;
    }

    @Autowired
    public void setBlenderParseBlendFileService(BlenderParseBlendFileService blenderParseBlendFileService) {
        this.blenderParseBlendFileService = blenderParseBlendFileService;
    }

    @Autowired
    public void setBlenderProjectService(BlenderProjectService blenderProjectService) {
        this.blenderProjectService = blenderProjectService;
    }

    @Autowired
    public void setBlenderProjectToProjectForm(BlenderProjectToProjectForm blenderProjectToProjectForm) {
        this.blenderProjectToProjectForm = blenderProjectToProjectForm;
    }
}
