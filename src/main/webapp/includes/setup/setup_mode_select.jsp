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
<p class="lead">Mode for this installation</p>
<p id="buttonWorks"></p>
<p>
    <button type="button" class="btn btn-primary btn-lg custom_btn_width" data-toggle="tooltip" data-placement="right"
            data-trigger="hover"
            title="Sethlans will be configured as the farm server" onclick="serverClicked()"> Server
    </button>
</p>
<p>
    <button type="button" class="btn btn-primary btn-lg custom_btn_width" data-toggle="tooltip" data-placement="right"
            data-trigger="hover"
            title="Sethlans will be configured as a node in the farm only" onclick="nodeClicked()">Node
    </button>
</p>

<p>
    <button type="button" class="btn btn-primary btn-lg custom_btn_width" data-toggle="tooltip" data-placement="right"
            data-trigger="hover"
            title="Sethlans will act as the farm server and as a node in the farm" onclick="bothClicked()">Both
    </button>
</p>
<script>
    function serverClicked() {
        document.getElementById("buttonWorks").innerHTML = "Server Clicked";
    }
    function nodeClicked() {
        document.getElementById("buttonWorks").innerHTML = "Node Clicked";
    }

    function bothClicked() {
        document.getElementById("buttonWorks").innerHTML = "Both Clicked";
    }
</script>