<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<jsp:directive.page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" isErrorPage="true" />
    
<html>
<head>
<c:import url="/head.jsp" />
<script language="javascript"> 
function toggle() {
	var ele = document.getElementById("toggleText");
	var text = document.getElementById("displayText");
	if(ele.style.display == "block") {
    		ele.style.display = "none";
		text.innerHTML = "show";
  	}
	else {
		ele.style.display = "block";
		text.innerHTML = "hide";
	}
} 
</script>
</head>
<body >
<div id="roof">
<c:import url="/roof.jsp" />
</div>
<div id="content">
<c:import url="/header.jsp" />


<div id="centerColFull">
<br/>
<h3>Oops...</h3>
<br/>
<p>There has been an error when processing your request. We're sorry for the inconvenience.  <br/> <br/>
<a href="https://www.icts.uiowa.edu/jira/secure/CreateIssue!default.jspa">Click here if you like to report this issue.</a></p>
<br/>
<input type="button" name="c1" onclick=javascript:toggle(); value="Error Details">
<div id="toggleText" style="display: none">
<table>
<tr>
<td>
<font color="red">
<jsp:expression>exception.toString()</jsp:expression>
</font>
</td>
</tr>
</table>
</div> 	


<br/>

<c:import url="/footer.jsp" />
</div>
</div>
</body>
</html>
