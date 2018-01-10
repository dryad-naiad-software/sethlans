/*
 * Copyright (c) 2018 Dryad and Naiad Software LLC.
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
import com.dryadandnaiad.sethlans.enums.*;
import com.dryadandnaiad.sethlans.services.blender.BlenderParseBlendFileService;
import com.dryadandnaiad.sethlans.services.blender.BlenderProjectService;
import com.dryadandnaiad.sethlans.services.database.BlenderBinaryDatabaseService;
import com.dryadandnaiad.sethlans.services.database.BlenderProjectDatabaseService;
import com.dryadandnaiad.sethlans.services.database.SethlansNodeDatabaseService;
import com.dryadandnaiad.sethlans.services.storage.WebUploadService;
import com.dryadandnaiad.sethlans.utils.SethlansUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.*;
import java.util.*;

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
    public String listProjects(Model model) {
        List<SethlansNode> sethlansNodeList = sethlansNodeDatabaseService.listAll();
        List<SethlansNode> activeNodes = new ArrayList<>();
        Set<ComputeType> activeTypes = new HashSet<>();
        for (SethlansNode sethlansNode : sethlansNodeList) {
            if (sethlansNode.isActive() && sethlansNode.isBenchmarkComplete()) {
                activeNodes.add(sethlansNode);
            }
        }
        for (SethlansNode activeNode : activeNodes) {
            activeTypes.add(activeNode.getComputeType());
        }

        if (activeNodes.size() > 0 && activeTypes.contains(ComputeType.CPU_GPU)) {
            activeTypes.add(ComputeType.CPU);
            activeTypes.add(ComputeType.GPU);
        }

        if (activeNodes.size() > 0 && !activeTypes.contains(ComputeType.CPU_GPU)) {
            activeTypes.add(ComputeType.CPU_GPU);
        }
        LOG.debug("activetypes" + activeTypes.toString());
        getAvailableBlenderBinaries();
        model.addAttribute("project_option", "list");
        model.addAttribute("projects", blenderProjectDatabaseService.listAllReverse());
        model.addAttribute("active_nodes", activeNodes);
        model.addAttribute("active_compute_types", activeTypes);
        return "project/projects";
    }

    @RequestMapping("/project/new")
    public String newProject(Model model) {
        model.addAttribute("projectForm", new ProjectForm());
        model.addAttribute("project_option", "project_form");
        return "project/projects";
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
        return "redirect:/project";
    }

    @RequestMapping("/project/start/{id}")
    public String startProject(@PathVariable Integer id) {
        BlenderProject blenderProject = blenderProjectDatabaseService.getById(id);
        blenderProject.setStarted(true);
        blenderProject = blenderProjectDatabaseService.saveOrUpdate(blenderProject);
        blenderProjectService.startProject(blenderProject);
        return "redirect:/project";
    }

    @RequestMapping("/project/pause/{id}")
    public String pauseProject(@PathVariable Integer id) {
        BlenderProject blenderProject = blenderProjectDatabaseService.getById(id);
        blenderProject.setPaused(true);
        blenderProject = blenderProjectDatabaseService.saveOrUpdate(blenderProject);
        blenderProjectService.pauseProject(blenderProject);
        return "redirect:/project";
    }

    @RequestMapping("/project/edit/{id}")
    public String editProject(@PathVariable Integer id, Model model) {
        getAvailableBlenderBinaries();
        BlenderProject project = blenderProjectDatabaseService.getById(id);
        ProjectForm projectForm = blenderProjectToProjectForm.convert(project);
        projectForm.setAvailableBlenderBinaries(getAvailableBlenderBinaries());
        projectForm.setAvailableBlenderVersions();
        if (projectForm.getProjectType().equals(ProjectType.STILL_IMAGE)) {
            projectForm.setEndFrame(200);
        }
        model.addAttribute("projectForm", projectForm);
        model.addAttribute("project_option", "project_form");
        LOG.debug(projectForm.toString());
        return "project/projects";
    }

    @RequestMapping(value = "/project/download/{id}")
    public void downloadProject(@PathVariable Integer id, HttpServletResponse response) {
        BlenderProject project = blenderProjectDatabaseService.getById(id);
        if (project.getProjectType().equals(ProjectType.STILL_IMAGE)) {
            File image = new File(project.getFrameFileNames().get(0));
            SethlansUtils.serveFile(image, response);
        }
        if (project.getProjectType().equals(ProjectType.ANIMATION)) {
            File zipFile = SethlansUtils.createArchive(project.getFrameFileNames(), project.getProjectRootDir(), project.getProjectName().toLowerCase());
            if (zipFile != null) {
                SethlansUtils.serveFile(zipFile, response);
            }
        }
    }

    @RequestMapping("/project/details/{id}")
    public String projectDetails(@PathVariable Integer id, Model model) {
        model.addAttribute("project", blenderProjectDatabaseService.getById(id));
        return "project/project_details";
    }

    @RequestMapping(value = "/project/new", method = RequestMethod.POST)
    public String newProjectDetails(final @Valid @ModelAttribute("projectForm") ProjectForm projectForm, BindingResult bindingResult,
                                    @RequestParam("projectFile") MultipartFile projectFile, Model model) {

        if (bindingResult != null) {
            LOG.debug("New project with binding result.");
        }
        String uploadTag = SethlansUtils.getShortUUID();
        webUploadService.store(projectFile, uploadTag);
        getAvailableBlenderBinaries();
        projectForm.setUploadedFile(projectFile.getOriginalFilename());
        projectForm.setFileLocation(temp + uploadTag + "-" + projectFile.getOriginalFilename());
        projectForm.setBlendFile(blenderParseBlendFileService.parseBlendFile(projectForm.getFileLocation()));
        projectForm.setAvailableBlenderBinaries(getAvailableBlenderBinaries());
        projectForm.populateForm();
        projectForm.setAvailableBlenderVersions();
        LOG.debug(projectForm.toString());
        model.addAttribute("project_option", "project_form");
        return "project/projects";
    }

    @RequestMapping(value = "/project/new/details", method = RequestMethod.POST)
    public String projectSummary(final @Valid @ModelAttribute("projectForm") ProjectForm projectForm, BindingResult bindingResult, Model model) {
        projectFormValidator.validate(projectForm, bindingResult);

        if (bindingResult.hasErrors()) {
            LOG.debug(bindingResult.toString());
            projectForm.setProgress(ProjectFormProgress.DETAILS);
            projectForm.setAvailableBlenderBinaries(getAvailableBlenderBinaries());
            projectForm.setAvailableBlenderVersions();
            LOG.debug(projectForm.toString());
            model.addAttribute("project_option", "project_form");
            return "project/projects";
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


    @RequestMapping("/project/thumbnail/{id}")
    public ResponseEntity<byte[]> getThumbnailImage(@PathVariable Integer id) {
        BlenderProject blenderProject = blenderProjectDatabaseService.getById(id);
        File image = new File(blenderProject.getCurrentFrameThumbnail());
        try {
            InputStream in = new BufferedInputStream(new FileInputStream(image));
            byte[] imageToSend = IOUtils.toByteArray(in);
            if (blenderProject.getCurrentFrameThumbnail().contains("png")) {
                return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(imageToSend);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @ModelAttribute("availableBlenderBinaries")
    public List<BlenderBinary> getAvailableBlenderBinaries() {
        List<BlenderBinary> availableBlenderBinaries = new ArrayList<>();
        List<BlenderBinary> databaseList = blenderBinaryDatabaseService.listAll();
        for (BlenderBinary blenderBinary : databaseList) {
            if (blenderBinary.isDownloaded()) {
                availableBlenderBinaries.add(blenderBinary);
            }
        }
        return availableBlenderBinaries;
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
