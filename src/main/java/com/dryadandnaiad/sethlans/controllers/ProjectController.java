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

import com.dryadandnaiad.sethlans.commands.ProjectForm;
import com.dryadandnaiad.sethlans.converters.BlenderProjectToProjectForm;
import com.dryadandnaiad.sethlans.domains.database.blender.BlenderBinary;
import com.dryadandnaiad.sethlans.domains.database.blender.BlenderProject;
import com.dryadandnaiad.sethlans.domains.database.node.SethlansNode;
import com.dryadandnaiad.sethlans.enums.BlenderEngine;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.enums.ProjectFormProgress;
import com.dryadandnaiad.sethlans.enums.RenderOutputFormat;
import com.dryadandnaiad.sethlans.services.blender.BlenderParseBlendFileService;
import com.dryadandnaiad.sethlans.services.blender.BlenderProjectService;
import com.dryadandnaiad.sethlans.services.database.BlenderBinaryDatabaseService;
import com.dryadandnaiad.sethlans.services.database.BlenderProjectDatabaseService;
import com.dryadandnaiad.sethlans.services.database.SethlansNodeDatabaseService;
import com.dryadandnaiad.sethlans.services.storage.WebUploadService;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Arrays;
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
public class ProjectController extends AbstractSethlansController {
    private static final Logger LOG = LoggerFactory.getLogger(ProjectController.class);
    private BlenderBinaryDatabaseService blenderBinaryDatabaseService;
    private List<BlenderBinary> availableBlenderBinaries;
    private WebUploadService webUploadService;
    private BlenderParseBlendFileService blenderParseBlendFileService;
    private BlenderProjectDatabaseService blenderProjectDatabaseService;
    private Validator projectFormValidator;
    private BlenderProjectToProjectForm blenderProjectToProjectForm;
    private SethlansNodeDatabaseService sethlansNodeDatabaseService;
    private BlenderProjectService blenderProjectService;

    @Value("${sethlans.tempDir}")
    private String temp;


    @RequestMapping("/project")
    public String getPage(Model model) {
        List<SethlansNode> sethlansNodeList = sethlansNodeDatabaseService.listAll();
        List<SethlansNode> activeNodes = new ArrayList<>();
        for (SethlansNode sethlansNode : sethlansNodeList) {
            if (sethlansNode.isActive()) {
                activeNodes.add(sethlansNode);
            }
        }
        getAvailableBlenderBinaries();
        model.addAttribute("availableBlenderBinaries", availableBlenderBinaries);
        model.addAttribute("projects", blenderProjectDatabaseService.listAll());
        model.addAttribute("active_nodes", activeNodes);
        return "project/project_list";
    }

    @RequestMapping("/project/new")
    public String newProject(Model model) {
        model.addAttribute("projectForm", new ProjectForm());
        return "project/project_form";
    }

    @RequestMapping("/project/view/{id}")
    public String getProject(@PathVariable Integer id, Model model) {
        model.addAttribute("project", blenderProjectDatabaseService.getById(id));
        return "project/project_view";
    }

    @RequestMapping("/project/delete/{id}")
    public String deleteProject(@PathVariable Integer id) {
        BlenderProject blenderProject = blenderProjectDatabaseService.getById(id);
        blenderProjectService.stopProject(blenderProject);
        blenderProjectDatabaseService.delete(id);
        return "redirect:/project";
    }

    @RequestMapping("/project/start/{id}")
    public String startProject(@PathVariable Integer id) {
        BlenderProject blenderProject = blenderProjectDatabaseService.getById(id);
        blenderProjectService.startProject(blenderProject);
        return "redirect:/project";
    }

    @RequestMapping("/project/pause/{id}")
    public String pauseProject(@PathVariable Integer id) {
        BlenderProject blenderProject = blenderProjectDatabaseService.getById(id);
        blenderProjectService.pauseProject(blenderProject);
        return "redirect:/project";
    }

    @RequestMapping("/project/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        getAvailableBlenderBinaries();
        BlenderProject project = blenderProjectDatabaseService.getById(id);
        ProjectForm projectForm = blenderProjectToProjectForm.convert(project);
        projectForm.setAvailableBlenderBinaries(availableBlenderBinaries);
        projectForm.setAvailableBlenderVersions();
        model.addAttribute("projectForm", projectForm);
        LOG.debug(projectForm.toString());
        return "project/project_form";
    }

    @RequestMapping(value = "/project/new", method = RequestMethod.POST)
    public String newProjectDetails(final @Valid @ModelAttribute("projectForm") ProjectForm projectForm,BindingResult bindingResult, @RequestParam("projectFile")
            MultipartFile projectFile) {

        if (bindingResult != null) {
            LOG.debug("New project with binding result.");
        }
        UUID uploadTag = UUID.randomUUID();
        webUploadService.store(projectFile, uploadTag.toString());
        getAvailableBlenderBinaries();
        projectForm.setUploadedFile(projectFile.getOriginalFilename());
        projectForm.setFileLocation(temp + uploadTag + "-" + projectFile.getOriginalFilename());
        projectForm.setBlendFile(blenderParseBlendFileService.parseBlendFile(projectForm.getFileLocation()));
        projectForm.setAvailableBlenderBinaries(availableBlenderBinaries);
        projectForm.populateForm();
        projectForm.setAvailableBlenderVersions();
        LOG.debug(projectForm.toString());
        return "project/project_form";
    }

    @RequestMapping(value = "/project/new/details", method = RequestMethod.POST)
    public String projectSummary(final @Valid @ModelAttribute("projectForm") ProjectForm projectForm, BindingResult bindingResult) {
        projectFormValidator.validate(projectForm, bindingResult);

        if (bindingResult.hasErrors()) {
            LOG.debug(bindingResult.toString());
            projectForm.setProgress(ProjectFormProgress.DETAILS);
            projectForm.setAvailableBlenderBinaries(availableBlenderBinaries);
            projectForm.setAvailableBlenderVersions();
            LOG.debug(projectForm.toString());
            return "project/project_form";
        }
        LOG.debug(projectForm.toString());
        if (projectForm.getProgress() == ProjectFormProgress.FINISHED) {
            LOG.debug("FINISHED");
            BlenderProject savedProject = blenderProjectDatabaseService.saveOrUpdateProjectForm(projectForm);
            LOG.debug(projectForm.toString());
            return "redirect:/project/view/" + savedProject.getId();
        }


        return "project/project_view";

    }

    private void getAvailableBlenderBinaries() {
        availableBlenderBinaries = new ArrayList<>();
        List<BlenderBinary> databaseList = blenderBinaryDatabaseService.listAll();
        for (BlenderBinary blenderBinary : databaseList) {
            if (blenderBinary.isDownloaded()) {
                availableBlenderBinaries.add(blenderBinary);
            }
        }
    }


    @ModelAttribute("compute_types")
    public List<ComputeType> computeTypeArray() {
        return Arrays.asList(ComputeType.values());
    }

    @ModelAttribute("engines")
    public List<BlenderEngine> blenderEngineArray() {
        return Arrays.asList(BlenderEngine.values());
    }

    @ModelAttribute("formats")
    public List<RenderOutputFormat> renderOutputFormats() {
        return Arrays.asList(RenderOutputFormat.values());
    }

    @Autowired
    public void setWebUploadService(WebUploadService webUploadService) {
        this.webUploadService = webUploadService;
    }

    @Autowired
    public void setBlenderBinaryDatabaseService(BlenderBinaryDatabaseService blenderBinaryDatabaseService) {
        this.blenderBinaryDatabaseService = blenderBinaryDatabaseService;
    }

    @Autowired
    public void setBlenderParseBlendFileService(BlenderParseBlendFileService blenderParseBlendFileService) {
        this.blenderParseBlendFileService = blenderParseBlendFileService;
    }

    @Autowired
    public void setBlenderProjectDatabaseService(BlenderProjectDatabaseService blenderProjectDatabaseService) {
        this.blenderProjectDatabaseService = blenderProjectDatabaseService;
    }

    @Autowired
    public void setBlenderProjectToProjectForm(BlenderProjectToProjectForm blenderProjectToProjectForm) {
        this.blenderProjectToProjectForm = blenderProjectToProjectForm;
    }

    @Autowired
    public void setSethlansNodeDatabaseService(SethlansNodeDatabaseService sethlansNodeDatabaseService) {
        this.sethlansNodeDatabaseService = sethlansNodeDatabaseService;
    }

    @Autowired
    public void setBlenderProjectService(BlenderProjectService blenderProjectService) {
        this.blenderProjectService = blenderProjectService;
    }

    @Autowired
    @Qualifier("projectFormValidator")
    public void setProjectFormValidator(Validator projectFormValidator) {
        this.projectFormValidator = projectFormValidator;
    }
}
