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

package com.dryadandnaiad.sethlans.bootstrap;

import com.dryadandnaiad.sethlans.enums.Role;
import com.dryadandnaiad.sethlans.models.system.AccessKey;
import com.dryadandnaiad.sethlans.models.user.User;
import com.dryadandnaiad.sethlans.repositories.AccessKeyRepository;
import com.dryadandnaiad.sethlans.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * File created by Mario Estrella on 4/11/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Component
public class Bootstrap implements CommandLineRunner {

    private final AccessKeyRepository accessKeyRepository;
    private final UserRepository userRepository;

    public Bootstrap(AccessKeyRepository accessKeyRepository, UserRepository userRepository) {
        this.accessKeyRepository = accessKeyRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        if (accessKeyRepository.count().block() == 0) {
            // load data
            System.out.println("#### Populating Access Key Repository ####");

            accessKeyRepository.save(AccessKey.builder().accessKey(UUID.randomUUID().toString()).build()).block();

            System.out.println("Loaded Access Keys: " + accessKeyRepository.count().block());
        }
        if (userRepository.count().block() == 0) {
            Set<Role> admin = new HashSet<>();
            Set<Role> user = new HashSet<>();
            Set<Role> superUser = new HashSet<>();
            admin.add(Role.ADMINISTRATOR);
            superUser.add(Role.SUPER_ADMINISTRATOR);
            user.add(Role.USER);
            // load data
            System.out.println("#### Populating User Repository ####");
            userRepository.save(User.builder().username("bubba").password("test1234").email("test@test.com").
                    active(true).roles(admin).build()).block();
            userRepository.save(User.builder().username("bubba2").password("test123442").email("test2@test.com").
                    active(true).roles(user).build()).block();
            userRepository.save(User.builder().username("bubba3").password("test123442").email("test2@test.com").
                    active(true).roles(superUser).build()).block();

            System.out.println("Loaded Users: " + userRepository.count().block());

        }

    }
}
