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
  Date: 3/5/17
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
    <meta name="description" content="">
    <meta name="author" content="">
    <link rel="icon" href="../images/favicon.png">

    <title>Sethlans Initial Setup</title>

    <c:import url="../includes/css.html"/>
</head>

<body>
<c:import url="../includes/setup/setup_nav.html"/>
<c:import url="../includes/setup/setup_breadcrumb.jsp"/>


<div class="container">
    <h1 class="text-center">Sethlans Setup</h1>

        <c:choose>
            <c:when test="${param.setup == null}">
                <c:import url="../includes/setup/setup_mode_select.jsp"/>
            </c:when>
            <c:when test="${param.setup == 'sethlans_mode'}">
                <c:import url="../includes/setup/setup_mode_select.jsp"/>
            </c:when>
            <c:when test="${param.setup == 'setup_server'}">
                <c:import url="../includes/setup/setup_server.jsp"/>
            </c:when>
            <c:when test="${param.setup == 'setup_node'}">
                <c:import url="../includes/setup/setup_node.jsp"/>
            </c:when>
            <c:when test="${param.setup == 'setup_both'}">
                <c:import url="../includes/setup/setup_dual.jsp"/>
            </c:when>
        </c:choose>


</div><!-- /.container -->
<c:import url="../includes/scripts.html"/>
</body>
</html>

