<%--
  ~ Copyright (c) 2017 Dryad and Naiad Software LLC
  ~
  ~ This program is free software; you can redistribute it and/or
  ~ modify it under the terms of the GNU General Public License
  ~ as published by the Free Software Foundation; either version 2
  ~ of the License, or (at your option) any later version.
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with this program; if not, write to the Free Software
  ~ Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  ~
  --%>

<%--
  Created by IntelliJ IDEA.
  User: Mario Estrella mestrella@dryadandnaiad.com
  Company: Dryad and Naiad Software LLC
  Project: sethlans
  Date: 3/6/17
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<form action="/setup" method="post">
    <div class="form-group">
        <h3><u>Storage</u></h3>

        <label for="projectDirectory">Projects</label>
        <input type="text" class="form-control" id="projectDirectory" value="~/.sethlans/projects" data-toggle="tooltip"
               data-trigger="hover"
               title="Render projects will be stored here. Each project contains .blend files and finished renders in a subdirectory named after the project.">
        <label for="blenderDirectory">Blender Binaries</label>
        <input type="text" class="form-control" id="blenderDirectory" value="~/.sethlans/blenderzip"
               data-toggle="tooltip" data-trigger="hover"
               title="Storage for zip files containing blender binaries. These will be distributed to nodes if the node does not have the appropriate blender version available.">
        <label for="cacheDirectory">Tile Cache</label>
        <input type="text" class="form-control" id="cacheDirectory" value="~/.sethlans/cache" data-toggle="tooltip"
               data-trigger="hover"
               title="Running projects will store the tiles from nodes and place them here in a sub-directory named after the project.">
    </div>
    <div class="form-group">
        <h3><u>Ports</u></h3>
        <label for="serverPort">Sethlans HTTP Port</label>
        <input type="number" class="form-control" id="serverPort" value="7007" data-toggle="tooltip"
               data-trigger="hover"
               title="Default HTTP Port is 7007.  Nodes will communicate with the server on this port if HTTPS isn't enabled.">
        <div class="checkbox">
            <label data-toggle="tooltip" data-trigger="hover" data-placement="right" title="Turns on HTTPS">
                <input type="checkbox" id="useHTTPS" class="form-check-input" value="true" onClick="toggleHTTPS()">
                Enable HTTPS </label>
        </div>
        <div>
            <label id="https-label" for="secureServerPort" hidden> Sethlans HTTPS Port </label>
            <input type="hidden" class="form-control " id="secureServerPort" value="7443" data-toggle="tooltip"
                   data-trigger="hover"
                   title="Default HTTPS port is 7443. Nodes will communicate on the server on this port.">
        </div>
    </div>
    <div class="form-group">
        <h3><u>Options</u></h3>
        <div class="checkbox">
            <label data-toggle="tooltip" data-trigger="hover" data-placement="right"
                   title="When application is started, launch brower/open tab and navigate to Sethlans main page.">
                <input type="checkbox" id="launchBrowserOnStartup" class="form-check-input" value="true">
                Launch Browser on Startup </label>
        </div>
    </div>
    <input class="btn btn-primary btn-lg btn-block" type="submit" value="Continue">
</form>
</div>
<script>
    function toggleHTTPS() {
        if ($("#useHTTPS").is(":checked")) {
            document.getElementById("https-label").style.display = "block";
            document.getElementById("secureServerPort").type = "number";
        }
        else {
            document.getElementById("https-label").style.display = "none";
            document.getElementById("secureServerPort").type = "hidden";
        }

    }
</script>