/*
 * Copyright (C) 2017 Dryad and Naiad Software LLC
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
 */
package com.dryadandnaiad.sethlans.application;

import com.dryadandnaiad.sethlans.enums.UIType;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.OptionHandlerFilter;

/**
 *
 * @author Mario Estrella <mestrella@dryadandnaiad.com>
 */
public class Sethlans {

    private static final Logger logger = LogManager.getLogger(Sethlans.class);

    @Option(name = "-compute-method", usage = "CPU: only use cpu, "
            + "GPU: only use gpu, CPU_GPU: can use cpu and gpu "
            + "(not at the same time) if -gpu is not use it will not "
            + "use the gpu", metaVar = "CPU", required = false)
    private String method = null;

    @Option(name = "-cores", usage = "Number of cores/threads to use for the "
            + "render", metaVar = "3", required = false)
    private int nb_cores = -1;

    @Option(name = "-cache-dir", usage = "Cache/Working directory. "
            + "will be emptied on execution",
            metaVar = "/tmp/cache", required = false)
    private String cache_dir = null;

    @Option(name = "--verbose", usage = "Display log", required = false)
    private boolean print_log = false;

    @Option(name = "-ui", usage = "Specify the user interface to use, default '"
            + UIType.Constants.GUI_VALUE + "', available '"
            + UIType.Constants.CLI_VALUE + "', '"
            + UIType.Constants.CLI_ONELINE_VALUE + "', '"
            + UIType.Constants.GUI_VALUE + "' (graphical)", required = false)
    private String ui_type = null;

    @Option(name = "-mode", usage = "Specify whether to operate as a server, node, or both", required = false)

    public static void main(String[] args) {
        new Sethlans().doMain(args);
    }

    public void doMain(String[] args) {
        CmdLineParser cmdParser = new CmdLineParser(this);

        try {
            cmdParser.parseArgument(args);

        } catch (CmdLineException e) {
            logger.error(e.getMessage());
            System.err.println(e.getMessage());
            System.err.println("Usage: ");
            cmdParser.printUsage(System.err);
            System.err.println();
            System.err.println("Example: java " + this.getClass().getName() + " " + cmdParser.printExample(OptionHandlerFilter.REQUIRED));
            return;

        }

    }
}
