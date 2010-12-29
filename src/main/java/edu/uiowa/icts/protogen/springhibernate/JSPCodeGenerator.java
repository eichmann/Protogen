/*
 * Created on May 12, 2008
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.uiowa.icts.protogen.springhibernate;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.uiowa.icts.protogen.springhibernate.ClassVariable.AttributeType;


public class JSPCodeGenerator {
	
	private static final Log log =LogFactory.getLog(JSPCodeGenerator.class);
	public String jspRoot;

	
	public JSPCodeGenerator(String jspRoot)
	{
		this.jspRoot = jspRoot;

		
	}
	
	public void generateAllJSP(List<DomainClass> ecList)
		throws IOException {
		
	


		generateMenu(ecList);

		Iterator<DomainClass> ecIter = ecList.iterator();
		while(ecIter.hasNext())
		{
			
			DomainClass ec = ecIter.next();
			generateShowJSP(ec);
			generateListJSP(ec);
			generateEditJSP(ec);
		}
		
		
	}

	private void generateShowJSP(DomainClass ec) throws IOException {
		String directory =  jspRoot + "/"+ ec.getSchema().getUnqualifiedLabel() + "/generated/" + ec.getIdentifier().toLowerCase();
		(new File(directory )).mkdirs();
		String jspFile = directory +"/show.jsp";
		int indent = 0;
		
		String output= spaces(indent) + "<%@ include file=\"/WEB-INF/include.jsp\"  %>";
		output += lines(2);
	
		
		Iterator<ClassVariable> cvIter = ec.getPrimaryKeys().iterator();
		output += "<h2>"+ec.getIdentifier()+"</h2>";
		output += lines(1);
		while(cvIter.hasNext())
		{
			ClassVariable cv = cvIter.next();
			log.debug("ClassVariable:"+cv.getIdentifier());
			output += "<p>"+cv.getUpperIdentifier() + ":${"+ec.getLowerIdentifier() +"."+ cv.getIdentifier()+"}</p>";
			output += lines(1);
			
			
		}
		
		output += spaces(indent) + "<table class=\"tableData1\">";
		lines(1);
		
		cvIter = ec.listAllIter();
		output += lines(1);
		indent +=4;
		while(cvIter.hasNext())
		{
			output += spaces(indent) + "<tr>";
			output += lines(1);
			ClassVariable cv = cvIter.next();
			log.debug("-ClassVariable:"+cv.getIdentifier());
			indent +=4;
			output += spaces(indent) +"<th>" +cv.getUpperIdentifier() + "</th>";
			output += lines(1);
			
			String pkString="";

		
			if(cv.getAttribType() == AttributeType.CHILD && cv.getAttribute().getEntity().getDomainClass() !=null)
			{
				log.debug("isChild");
				Iterator<ClassVariable> pkIter = cv.getAttribute().getEntity().getDomainClass().getPrimaryKeys().iterator();
				while(pkIter.hasNext())
				{
					ClassVariable pk = pkIter.next();
					pkString += pk.getIdentifier() + "=${item." + pk.getIdentifier() + "}";
					while(pkIter.hasNext())
						pkString += "&";
					
					
					
				}
				
				
				output += spaces(indent) +"<td><ul><c:forEach items=\"${"+ec.getLowerIdentifier() +"."+ cv.getIdentifier()+"}\" var=\"item\" varStatus=\"itemStatus\" >";
				output += spaces(indent) +"<li><a href=\"../"+ cv.getAttribute().getEntity().getDomainClass().getLowerIdentifier() +  "/edit.html?"+pkString+"\" > "+ cv.getAttribute().getEntity().getDomainClass().getSelectBoxLabel() + "</a></li>";
				output += spaces(indent) +"</c:forEach></li></td>";
			}
			else
			output += spaces(indent) +"<td>${"+ec.getLowerIdentifier() +"."+ cv.getIdentifier()+"}</td>";
			
			output += lines(1);
			indent -=4;
			output += spaces(indent) + "</tr>";
			output += lines(1);
		}
		indent -=4;
		output += spaces(indent) + "</table>";
		output += lines(1);
		
		
		
		
		File file = new File(jspFile);
		FileWriter fstream = new FileWriter(file);
		BufferedWriter out = new BufferedWriter(fstream);
		out.write(output);
		out.close();
		log.debug("Log here");

	}
	
	
	private void generateListJSP(DomainClass ec) throws IOException {
	
		log.debug("GeneratingListJSP:"+ec.getIdentifier());
		log.debug("...."+ ec.getIdentifier());
		String directory =  jspRoot + "/"+ ec.getSchema().getUnqualifiedLabel() + "/generated/" + ec.getIdentifier().toLowerCase();
		
		(new File(directory )).mkdirs();
		
		String jspFile = directory+ "/list.jsp";
		int indent = 0;
		
		String output= spaces(indent) + "<%@ include file=\"/WEB-INF/include.jsp\"  %>";
		output += lines(2);

		output += "<h2>"+ec.getIdentifier()+" List</h2>";
		output += lines(1);
		output += spaces(indent) + "<a href=\"add.html\">Add</a>";
		output += lines(1);
		
		output += spaces(indent) + "<table class=\"tableData1\">\n<thead>";
		lines(1);
	
		Iterator<ClassVariable> cvIter = ec.listAllIter();
		output += lines(1);
		output += spaces(indent) + "<tr>";
		output += lines(1);
		indent +=4;
		while(cvIter.hasNext())
		{
			
			ClassVariable cv = cvIter.next();	
			output += spaces(indent) +"<th>" +cv.getUpperIdentifier() + "</th>";
			output += lines(1);
			
			
		}
		indent -=4;
		output += spaces(indent) + "</tr>\n</thead>";
		output += lines(1);
		indent -=4;
		
		cvIter = ec.listAllIter();
		output += spaces(indent) + "<tbody>\n<c:forEach items=\"${"+ec.getLowerIdentifier()+"List}\" var=\""+ec.getLowerIdentifier()+"\"  varStatus=\"status\">";
		output += spaces(indent) + "<tr>";
		output += lines(1);
		indent +=4;

		while(cvIter.hasNext())
		{
			
			ClassVariable cv = cvIter.next();
			if (cv.isPrimary())
			{
				output += spaces(indent) +"<td><a href=\"edit.html?"+ec.getLowerIdentifier()+"Id=${" +ec.getLowerIdentifier()+"."+cv.getIdentifier() + "}\">${" +ec.getLowerIdentifier()+"."+cv.getIdentifier() + "}</a>";
				output += spaces(indent) +"  <a href=\"show.html?"+ec.getLowerIdentifier()+"Id=${" +ec.getLowerIdentifier()+"."+cv.getIdentifier() + "}\">(view)</a></td>";
			}
			else
				output += spaces(indent) +"<td>${" +ec.getLowerIdentifier()+"."+cv.getIdentifier() + "}</td>";
			output += lines(1);
			
			
		}
		indent -=4;
		output += spaces(indent) + "</tr>";
		output += lines(1);
		output += spaces(indent) + "</c:forEach>";
		indent -=4;
		
		output += spaces(indent) + "\n</tbody>\n</table>";
		output += lines(1);
		
	
		File file = new File(jspFile);
		FileWriter fstream = new FileWriter(file);
		BufferedWriter out = new BufferedWriter(fstream);
		out.write(output);
		out.close();
		log.debug(".........done");
	}
	
	
	

	private void generateEditJSP(DomainClass ec) throws IOException {
		String directory =  jspRoot + "/" +ec.getSchema().getUnqualifiedLabel() + "/generated/" + ec.getIdentifier().toLowerCase();
		(new File(directory )).mkdirs();
		
		String jspFile = directory + "/edit.jsp";
		int indent = 0;
		
		String output= spaces(indent) + "<%@ include file=\"/WEB-INF/include.jsp\"  %>";
		output += lines(2);
		
		Iterator<ClassVariable> cvIter = ec.getPrimaryKeys().iterator();
		output += "<h2>"+ec.getIdentifier()+"</h2>";
		output += lines(1);
		while(cvIter.hasNext())
		{
			ClassVariable cv = cvIter.next();
			output += "<p>"+cv.getUpperIdentifier() + ":${"+ec.getLowerIdentifier() +"."+ cv.getIdentifier()+"}</p>";
			output += lines(1);
			
		}
		
		output += spaces(indent) + "<form:form method=\"get\" commandName=\""+ec.getLowerIdentifier()+"\" action=\"save.html\" >";
		output += spaces(indent) + "<table class=\"tableData1\">";
		lines(1);
		
		cvIter = ec.listAllIter();;
		output += lines(1);
		indent +=4;
		while(cvIter.hasNext())
		{
			
			output += spaces(indent) + "<tr>";
			output += lines(1);
			ClassVariable cv = cvIter.next();
			indent +=4;
			
			if( cv.getAttribType() ==AttributeType.CHILD )
			{

				output += spaces(indent) +"<th></th>";
				output += lines(1);
				output += spaces(indent) +"<td></td>";
			}
			else if(cv.getAttribType() == AttributeType.FOREIGNATTRIBUTE )
			{

				
				output += spaces(indent) +"<th>" +cv.getUpperIdentifier() + "</th>";
				output += lines(1);
				output += spaces(indent) + "<td><select id=\""+cv.getIdentifier()+"\" name=\""+cv.getAttribute().getReferencedEntity().getDomainClass().getEntity().getLowerLabel()+"Id\" >";
				output += lines(1);
				output += spaces(indent) + "<c:forEach items=\"${" + cv.getDomainClass().getLowerIdentifier()+ "List}\" var=\"item\" >";
				String selected="";
				if( cv.getDomainClass().getPrimaryKeys().size()>0 && cv.getAttribute().getReferencedEntity().getDomainClass().getPrimaryKeys().size()>0)
				{
					log.debug("creating select");
				selected += "<c:if test=\"${";
				selected += ec.getLowerIdentifier()+"."+cv.getIdentifier()+"." + cv.getDomainClass().getPrimaryKeys().iterator().next().getIdentifier(); 
				selected += "== item."+ cv.getDomainClass().getPrimaryKeys().iterator().next().getIdentifier() +"}\">selected=\"true\"</c:if>";
				output += spaces(indent) + "<option "+selected+" value=\"${item."+cv.getAttribute().getReferencedEntity().getDomainClass().getPrimaryKeys().iterator().next().getIdentifier() +"}\" >"+ cv.getAttribute().getReferencedEntity().getDomainClass().getSelectBoxLabel()+"</option>";
				}
				output += spaces(indent) + "</c:forEach>";
				output += lines(1);
				output += spaces(indent) + "</select></td>";

				output += lines(1);

			}
			else 
			{
				if(cv.isPrimary())
				{
				output += spaces(indent) +"<th></th>";
				output += lines(1);
				output += spaces(indent) +"<td><form:hidden path=\""+ cv.getIdentifier()+"\" /></td>";
				}
				else 
				{
					output += spaces(indent) +"<th>" +cv.getUpperIdentifier() + "</th>";
					output += lines(1);
					output += spaces(indent) +"<td><form:input path=\""+ cv.getIdentifier()+"\" /></td>";
				}
			}
			
			output += lines(1);
			indent -=4;
			output += spaces(indent) + "</tr>";
			output += lines(1);
		}
		
		output += spaces(indent) + "<tr>";
		output += lines(1);
		indent +=4;
		output += spaces(indent) +"<th></th>";
		output += lines(1);
		output += spaces(indent) +"<td><input type=\"submit\" value=\"Save\" /></td>";
		output += lines(1);
		indent -=4;
		output += spaces(indent) + "</tr>";
		output += lines(1);
		indent -=4;
		output += spaces(indent) + "</table>";
		output += lines(1);
		output += spaces(indent) + "</form:form>";
		output += lines(1);
		
		File file = new File(jspFile);
		FileWriter fstream = new FileWriter(file);
		BufferedWriter out = new BufferedWriter(fstream);
		out.write(output);
		out.close();
	}
	
	
	private void generateIndex(List<DomainClass> ecList) throws IOException {
		(new File(jspRoot)).mkdirs();
		String jspFile = jspRoot + "/index.jsp";
		int indent = 0;
		
		String output= spaces(indent) + "<%@ include file=\"/WEB-INF/include.jsp\"  %>";
		output += lines(2);
		
		Iterator<DomainClass> dcIter =ecList.iterator();
		output += "<h2>Menu</h2>";
		output += lines(1);
		while(dcIter.hasNext())
		{
			DomainClass dc = dcIter.next();
			output += "<a href=\"<c:url value=\"/"+dc.getSchema().getUnqualifiedLabel() +"/" +dc.getLowerIdentifier().toLowerCase() + "/list.html\" />\" >"+dc.getIdentifier()+" List</a><br/>";
			output += lines(1);
			
		}
		lines(1);
	
		File file = new File(jspFile);
		FileWriter fstream = new FileWriter(file);
		BufferedWriter out = new BufferedWriter(fstream);
		out.write(output);
		out.close();
	}
	
	
	private void generateMenu(List<DomainClass> ecList) throws IOException {
		(new File(jspRoot)).mkdirs();
		String jspFile = jspRoot + "/menu.jsp";
		int indent = 0;
		
		String output= spaces(indent) + "<%@ include file=\"/WEB-INF/include.jsp\"  %>";
		output += lines(2);
		
		Iterator<DomainClass> dcIter =ecList.iterator();
		output += lines(1);
		while(dcIter.hasNext())
		{
			DomainClass dc = dcIter.next();
			output += "<a href=\"<c:url value=\"/"+dc.getSchema().getUnqualifiedLabel() +"/" +dc.getLowerIdentifier().toLowerCase() + "/list.html\" />\" >"+dc.getIdentifier()+" List</a>";
			output += lines(1);
			
		}
		lines(1);
		
	
		File file = new File(jspFile);
		FileWriter fstream = new FileWriter(file);
		BufferedWriter out = new BufferedWriter(fstream);
		out.write(output);
		out.close();
	}
	
	
	private String lines(int num) {
		String out = "";
		for (int i = 0; i < num; i++)
			out = "\n";
		return out;
	}

	private String spaces(int num) {
		String out = "";
		for (int i = 0; i < num; i++)
			out = " ";
		return out;
	}
	
	








}
