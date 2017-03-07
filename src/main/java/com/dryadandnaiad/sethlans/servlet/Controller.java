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

package com.dryadandnaiad.sethlans.servlet;

import com.dryadandnaiad.sethlans.config.SethlansConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created Mario Estrella on 3/5/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@WebServlet(name = "Controller", urlPatterns = {""})
public class Controller extends HttpServlet {
    private static final Logger LOG = LogManager.getLogger(Controller.class);
    private static SethlansConfiguration config = SethlansConfiguration.getInstance();
    private boolean firstTime = config.isFirstTime();
    private Map<String, String> actionMap = new HashMap<>();

    public Controller() {
    }

    private void doForward(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String setup = request.getParameter("setup");
        if (setup == null && firstTime) {
            request.setAttribute("setup", "mode");
        }


        try {
            // Forward to the requested page.
            request.getRequestDispatcher("/setup.jsp").forward(request, response);
        } catch (ServletException ex) {
            LOG.debug(ex.getMessage());
            throw ex;
        } catch (IOException ex) {
            LOG.debug(ex.getMessage());
            throw ex;
        }
    }


    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doForward(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doForward(request, response);
    }
}
