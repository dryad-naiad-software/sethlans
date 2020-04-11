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

import com.dryadandnaiad.sethlans.models.system.AccessKey;
import com.dryadandnaiad.sethlans.repositories.AccessKeyRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Created by Mario Estrella on 4/11/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Component
public class Bootstrap implements CommandLineRunner {

    private final AccessKeyRepository accessKeyRepository;

    public Bootstrap(AccessKeyRepository accessKeyRepository) {
        this.accessKeyRepository = accessKeyRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (accessKeyRepository.count().block() == 0) {
            // load data
            System.out.println("#### Populating Access Key Repository ####");

            accessKeyRepository.save(AccessKey.builder().accessKey(UUID.randomUUID().toString()).build()).block();

            System.out.println("Loaded Access Keys: " + accessKeyRepository.count().block());
        }

    }
}
