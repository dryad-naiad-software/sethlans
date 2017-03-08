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
  Date: 3/7/17
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="sql" uri="http://java.sun.com/jsp/jstl/sql" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<form action="/upload" method="post" enctype="multipart/form-data">
    <div class="form-group">
        <h3><u>Blender Upload</u></h3>
        <input type="file" id="files" name="files" multiple/>
        <div style="text-align: right; margin-top: 10px;">
            <input type="submit" value="Upload Files" class="btn btn-lg btn-primary"/>
        </div>
    </div>
</form>
<form action="/setup" method="post">
    <div class="form-group">
        <input class="btn btn-primary btn-lg btn-block" type="submit" value="Setup Summary">
    </div>
</form>
</div>
<script>

</script>