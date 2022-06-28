/*
 * Copyright (c) 2020. Dryad and Naiad Software LLC.
 *   This program is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU General Public License
 *   as published by the Free Software Foundation; either version 2
 *   of the License, or (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.dryadandnaiad.sethlans.unit.converters;

import com.dryadandnaiad.sethlans.converters.ProjectToProjectView;
import com.dryadandnaiad.sethlans.enums.Role;
import com.dryadandnaiad.sethlans.tools.TestUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * File created by Mario Estrella on 12/25/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Slf4j
class ProjectToProjectViewTest {

    ProjectToProjectView projectToProjectView = new ProjectToProjectView();

    @Test
    void convert() {
        var roles = new HashSet<Role>();
        roles.add(Role.USER);
        var project = TestUtils.getProject();
        project.setSethlansUser(TestUtils.getUser(roles, "testuser1234", "test1234"));

        var projectView = projectToProjectView.convert(project);
        for (Field f : projectView.getClass().getDeclaredFields()) {
            assertThat(f).isNotNull();
        }

    }
}
