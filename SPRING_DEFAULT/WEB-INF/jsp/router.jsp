<%@page import="java.io.File"%>
<%@ include file="/WEB-INF/include.jsp"%>
<%         
			String tablename = (String)request.getAttribute("tablename");
			String myPage = (String)request.getAttribute("templatepagename");
			String schemaName = (String)request.getAttribute("schemaname") != null ?  (String)request.getAttribute("schemaname") : "";
			String root = request.getSession().getServletContext().getRealPath("/");
			File f = new File(root + "WEB-INF/jsp/"+ schemaName +"/main/" +tablename +"/" +myPage+".jsp");
			String filename;
			if (f.exists()) {
				filename = "/WEB-INF/jsp/"+schemaName+"/main/" +tablename +"/" +myPage+".jsp";
			} else {
				filename = "/WEB-INF/jsp/"+schemaName+"/generated/" +tablename +"/" +myPage+".jsp";
			}
 
            
%>
<c:import  url="<%=filename %>"/>