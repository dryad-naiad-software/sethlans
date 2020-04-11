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

package com.dryadandnaiad.sethlans.db;

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.*;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;

/**
 * Created by Mario Estrella on 4/11/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 * <p>
 * Taken from https://stackoverflow.com/questions/47185840/how-to-make-embedded-mongodb-keep-the-data-on-application-shutdown/49449394#49449394
 */
@Slf4j
public class MongoInMemory {
    private int port;
    private String host;
    private MongodProcess process = null;
    private final File embedMongo = new File(System.getProperty("user.home") + "/.embedmongo");

    public MongoInMemory(int port, String host) {
        this.port = port;
        this.host = host;
    }

    @PostConstruct
    public void init() throws IOException {
        Storage storage = new Storage(
                System.getProperty("user.home") + "/.sethlans/data/mongodb", null, 0);

        IRuntimeConfig runtimeConfig;

        if (!embedMongo.exists()) {
            runtimeConfig = new RuntimeConfigBuilder()
                    .defaults(Command.MongoD)
                    .artifactStore(new ExtractedArtifactStoreBuilder()
                            .defaults(Command.MongoD)
                            .download(new DownloadConfigBuilder()
                                    .defaultsForCommand(Command.MongoD).build()))
                    .build();
        } else {
            runtimeConfig = new RuntimeConfigBuilder()
                    .defaults(Command.MongoD).build();
        }


        IMongodConfig mongodConfig = new MongodConfigBuilder()
                .version(Version.Main.PRODUCTION)
                .net(new Net(host, port, false))
                .replication(storage)
                .build();


        MongodStarter runtime = MongodStarter.getInstance(runtimeConfig);
        process = runtime.prepare(mongodConfig).start();
    }

    @PreDestroy
    public void stop() {
        process.stop();
    }
}
