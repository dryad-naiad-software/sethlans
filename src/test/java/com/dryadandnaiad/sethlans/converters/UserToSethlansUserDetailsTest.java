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

package com.dryadandnaiad.sethlans.converters;

import com.dryadandnaiad.sethlans.enums.Role;
import com.dryadandnaiad.sethlans.models.user.User;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * File created by Mario Estrella on 12/25/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
class UserToSethlansUserDetailsTest {
    UserToSethlansUserDetails userToSethlansUserDetails = new UserToSethlansUserDetails();

    @Test
    void convert() {
        var roles = new HashSet<Role>();

        roles.add(Role.SUPER_ADMINISTRATOR);

        var user = User.builder()
                .active(true)
                .id(12345L)
                .userID(UUID.randomUUID().toString())
                .username("testuser")
                .password("test1234")
                .roles(roles)
                .build();

        var userDetails = userToSethlansUserDetails.convert(user);
        assertThat(userDetails).isNotNull();

    }
}