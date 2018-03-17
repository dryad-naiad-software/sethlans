package com.dryadandnaiad.sethlans.converters;

import com.dryadandnaiad.sethlans.domains.database.blender.BlenderProject;
import com.dryadandnaiad.sethlans.forms.ProjectForm;
import com.dryadandnaiad.sethlans.services.database.SethlansUserDatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ProjectFormToBlenderProject implements Converter<ProjectForm, BlenderProject> {
    private SethlansUserDatabaseService sethlansUserDatabaseService;

    @Override
    public BlenderProject convert(ProjectForm projectForm) {
        BlenderProject blenderProject = new BlenderProject();
        blenderProject.setBlenderEngine(projectForm.getBlenderEngine());
        blenderProject.setRenderOn(projectForm.getRenderOn());
        blenderProject.setProject_uuid(projectForm.getUuid());
        blenderProject.setProjectName(projectForm.getProjectName());
        blenderProject.setBlenderVersion(projectForm.getSelectedBlenderversion());
        blenderProject.setBlendFileLocation(projectForm.getFileLocation());
        blenderProject.setSethlansUser(sethlansUserDatabaseService.findByUserName(projectForm.getUsername()));
        blenderProject.setProjectType(projectForm.getProjectType());
        blenderProject.setStartFrame(projectForm.getStartFrame());
        blenderProject.setEndFrame(projectForm.getEndFrame());
        blenderProject.setStepFrame(projectForm.getStepFrame());
        blenderProject.setResolutionX(projectForm.getResolutionX());
        blenderProject.setResolutionY(projectForm.getResolutionY());
        blenderProject.setResPercentage(projectForm.getResPercentage());
        blenderProject.setBlendFilename(projectForm.getUploadedFile());
        blenderProject.setPartsPerFrame(projectForm.getPartsPerFrame());
        return blenderProject;
    }

    @Autowired
    public void setSethlansUserDatabaseService(SethlansUserDatabaseService sethlansUserDatabaseService) {
        this.sethlansUserDatabaseService = sethlansUserDatabaseService;
    }
}
