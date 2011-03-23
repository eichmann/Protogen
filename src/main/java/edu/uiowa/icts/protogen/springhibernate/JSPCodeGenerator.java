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
import edu.uiowa.icts.protogen.springhibernate.ClassVariable.RelationshipType;
import edu.uiowa.webapp.Attribute;


public class JSPCodeGenerator extends AbstractSpringHibernateCodeGenerator{
	
	/**
	 * @param model
	 * @param pathBase
	 * @param packageRoot
	 */
	public JSPCodeGenerator(SpringHibernateModel model, String pathBase,
			String packageRoot) {
		super(model, pathBase, packageRoot);
		jspRoot = pathBase;
		// TODO Auto-generated constructor stub
	}

	private static final Log log =LogFactory.getLog(JSPCodeGenerator.class);
	public String jspRoot;


	public void generate() throws IOException
	{
		
		generateAllJSP(model.getDomainClassList());
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
		String directory =  jspRoot + "/"+ ec.getSchema().getUnqualifiedLabel() + "/" + ec.getIdentifier().toLowerCase();
		(new File(directory )).mkdirs();
		String jspFile = directory +"/show.jsp";
		int indent = 0;
		
		String output= spaces(indent) + "<%@ include file=\"/WEB-INF/include.jsp\"  %>";
		output += lines(2);
	
		
		Iterator<ClassVariable> cvIter = ec.getPrimaryKeys().iterator();
		output += "<h1>"+ec.getIdentifier()+"</h1>";
		output += lines(1);
		output += "<div class=\"box\">\n";
		output += lines(1);
		while(cvIter.hasNext())
		{
			ClassVariable cv = cvIter.next();
			log.debug("ClassVariable:"+cv.getIdentifier());
			if(ec.isUsesCompositeKey() && cv.isPrimary())
			{
				
				output += "<h2>";
				for(Attribute a : ec.getEntity().getPrimaryKeyAttributes())
				{
					output += " ${"+cv.getLowerIdentifier()+"."+a.getLowerLabel()+"} ";
			
				}
				output += "</h2>";
				

		
			}
			else 
			{
			output += "<h2> ${"+ec.getLowerIdentifier() +"."+ cv.getIdentifier()+"} </h2>";
			}
			output += lines(1);
			
			
		}
		
		output += spaces(indent) + "";
		lines(1);
		
		cvIter = ec.listAllIter();
		output += lines(1);
		indent +=4;
		while(cvIter.hasNext())
		{
			output += spaces(indent) + "";
			output += lines(1);
			ClassVariable cv = cvIter.next();
			log.debug("-ClassVariable:"+cv.getIdentifier());
			indent +=4;
			output += spaces(indent) +"" +cv.getUpperIdentifier() + ": ";
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
				
				
				if(cv.getAttribute().getEntity().getDomainClass().isUsesCompositeKey())
				{

					output += spaces(indent) +"not implemented<br/><br/>";
					
				}
				else
				{
				output += spaces(indent) +"<ul><c:forEach items=\"${"+ec.getLowerIdentifier() +"."+ cv.getIdentifier()+"}\" var=\"item\" varStatus=\"itemStatus\" >";
				output += spaces(indent) +"<li><a href=\"../"+ cv.getAttribute().getEntity().getDomainClass().getLowerIdentifier().toLowerCase() +  "/edit.html?"+pkString+"\" > ${item."+cv.getAttribute().getEntity().getDomainClass().getPrimaryKey().getLowerIdentifier() + "}</a></li>";
				output += spaces(indent) +"</c:forEach></ul><br/><br/>";
				}
			}
			else if( cv.isPrimary() && cv.getDomainClass().isUsesCompositeKey())
				
			{
				List<String[]> compositeKeys = new ArrayList<String[]>();
				
				
				for(Attribute a : ec.getEntity().getPrimaryKeyAttributes())
				{
					compositeKeys.add(new String[] {a.getLowerLabel(), ec.getLowerIdentifier()+".id."+a.getLowerLabel() });
								
				}

			
				String params="";
				String label="";
				for(String[] starray :compositeKeys)
				{
					params += starray[0]+"=${"+starray[1] + "}&";
					label += "("+starray[0]+",${"+starray[1]+"})";
					
				}
				params = params.substring(0, params.length()-1);
				output += ""+label+" ";
					
			}
			else
			{
			output += spaces(indent) +"${"+ec.getLowerIdentifier() +"."+ cv.getIdentifier()+"}<br/><br/>";
			}
			
			output += lines(1);
			indent -=4;
			output += spaces(indent) + "";
			output += lines(1);
		}
		indent -=4;
		output += spaces(indent) + "</div>";
		output += lines(1);
		
		
		
		
		File file = new File(jspFile);
		FileWriter fstream = new FileWriter(file);
		BufferedWriter out = new BufferedWriter(fstream);
		out.write(output);
		out.close();
		log.debug("done creating show");

	}
	
	
	private void generateListJSP(DomainClass ec) throws IOException {
	
		log.debug("GeneratingListJSP:"+ec.getIdentifier());
		log.debug("...."+ ec.getIdentifier());
		String directory =  jspRoot + "/"+ ec.getSchema().getUnqualifiedLabel() + "/" + ec.getIdentifier().toLowerCase();
		
		(new File(directory )).mkdirs();
		
		String jspFile = directory+ "/list.jsp";
		int indent = 0;
		
		String output= spaces(indent) + "<%@ include file=\"/WEB-INF/include.jsp\"  %>";
		output += lines(2);

		output += "<h1>"+ec.getIdentifier()+" List</h1>";
		output += lines(1);
		
		output += lines(1);
		output += spaces(indent) + "<div class=\"button\"><a href=\"add.html\">Add</a></div>";
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
		output += spaces(indent) + "<th></th></tr>\n</thead>";
		output += lines(1);
		indent -=4;
		
		cvIter = ec.listAllIter();
		output += spaces(indent) + "<tbody>\n<c:forEach items=\"${"+ec.getLowerIdentifier()+"List}\" var=\""+ec.getLowerIdentifier()+"\"  varStatus=\"status\">";
		output += spaces(indent) + "<tr>";
		output += lines(1);
		indent +=4;
		String links="";
		while(cvIter.hasNext())
		{
			
			ClassVariable cv = cvIter.next();

			
			if (cv.isPrimary())
			{
				if(ec.isUsesCompositeKey() && cv.isPrimary())
				{
					List<String[]> compositeKeys = new ArrayList<String[]>();
					
					
					for(Attribute a : ec.getEntity().getPrimaryKeyAttributes())
					{
						compositeKeys.add(new String[] {a.getLowerLabel(), ec.getLowerIdentifier()+".id."+a.getLowerLabel() });
									
					}
	
				
					String params="";
					String label="";
					for(String[] starray :compositeKeys)
					{
						params += starray[0]+"=${"+starray[1] + "}&";
						label += "("+starray[0]+",${"+starray[1]+"})";
						
					}
					params = params.substring(0, params.length()-1);
					links += "<td><a href=\"edit.html?"+params+"\">edit</a> ";
					links += "<a href=\"show.html?"+params+"\">view</a>";
					links += " <a href=\"delete.html?"+params+"\">delete</a></td>";
					output += "<td><a href=\"edit.html?"+params+"\">"+label+"</a></td> ";
					

				
			
				}
				else
				{
					links += spaces(indent) +"<td><a href=\"edit.html?"+cv.getIdentifier()+"Id=${" +ec.getLowerIdentifier()+"."+cv.getIdentifier() + "}\">edit</a> ";
					links += spaces(indent) +"<a href=\"show.html?"+cv.getIdentifier()+"Id=${" +ec.getLowerIdentifier()+"."+cv.getIdentifier() + "}\">view</a>";
					links += spaces(indent) +" <a href=\"delete.html?"+cv.getIdentifier()+"Id=${" +ec.getLowerIdentifier()+"."+cv.getIdentifier() + "}\">delete</a></td> ";
				output += spaces(indent) +"<td><a href=\"edit.html?"+cv.getIdentifier()+"Id=${" +ec.getLowerIdentifier()+"."+cv.getIdentifier() + "}\">${" +ec.getLowerIdentifier()+"."+cv.getIdentifier() + "}</a></td>";
				}
			}
			else if( cv.getRelationshipType() == RelationshipType.ONETOMANY)
			{
				output += spaces(indent) +"<td>"+cv.getLowerIdentifier() + "</td>";
	//			output += spaces(indent) +"<td>${" +ec.getLowerIdentifier()+"."+cv.getDomainClass().getLowerIdentifier()+"."+cv.getDomainClass().getPrimaryKey().getIdentifier() + "}</td>";
			}
			else if( cv.getRelationshipType() == RelationshipType.MANYTOMANY)
			{
				//output += spaces(indent) +"<td>${" +ec.getLowerIdentifier()+"."+cv.getLowerIdentifier() + "}</td>";
				output += spaces(indent) +"<td>"+cv.getLowerIdentifier() + "</td>";
			}
			else if(cv.getRelationshipType() == RelationshipType.MANYTOONE)
	
				output += spaces(indent) +"<td>${" +ec.getLowerIdentifier()+"."+cv.getDomainClass().getLowerIdentifier()+"."+cv.getDomainClass().getPrimaryKey().getLowerIdentifier() + "}</td>";
			else if(cv.getAttribType() == AttributeType.CHILD)
				
				output += spaces(indent) +"<td>"+cv.getIdentifier() + "</td>";
			else
			
				output += spaces(indent) +"<td>${" +ec.getLowerIdentifier()+"."+cv.getIdentifier() + "}</td>";
			output += lines(1);
			
			
		}
		
		output += spaces(indent) + links;
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
		String directory =  jspRoot + "/" +ec.getSchema().getUnqualifiedLabel() + "/" + ec.getIdentifier().toLowerCase();
		(new File(directory )).mkdirs();
		
		String jspFile = directory + "/edit.jsp";
		int indent = 0;
		
		String output= spaces(indent) + "<%@ include file=\"/WEB-INF/include.jsp\"  %>";
		output += lines(2);
		
		Iterator<ClassVariable> cvIter = ec.getPrimaryKeys().iterator();
		output += "<h1>"+ec.getIdentifier()+"</h1>";
		output += lines(1);
		output += "<div class=\"box\">";
		output += lines(1);
		while(cvIter.hasNext())
		{
			ClassVariable cv = cvIter.next();
			if(ec.isUsesCompositeKey() && cv.isPrimary())
			{
				output += "";
				
			}
			else
				output += "<h2>${"+ec.getLowerIdentifier() +"."+ cv.getIdentifier()+"}</h2>";
			
			output += lines(1);
			
		}
		
		output += spaces(indent) + "<form:form method=\"post\" commandName=\""+ec.getLowerIdentifier().toLowerCase()+"\" action=\"save.html\" >";
		output += spaces(indent) + "<fieldset>";
		lines(1);
		
		cvIter = ec.listAllIter();;
		output += lines(1);
		indent +=4;
		while(cvIter.hasNext())
		{
			
			output += spaces(indent) + "";
			output += lines(1);
			ClassVariable cv = cvIter.next();
			indent +=4;
			if(ec.isUsesCompositeKey() && cv.isPrimary()    )
			{
				
				
				for(Attribute a : ec.getEntity().getPrimaryKeyAttributes())
				{
					
					if(!a.isForeign())
						output += "<label for=\"id."+a.getLowerLabel()+"\">"+a.getLowerLabel()+"</label><form:input path=\"id."+a.getLowerLabel()+"\" /><br/>\n ";
			
				}
				

		
			}
			else if( cv.getAttribType() ==AttributeType.CHILD )
			{

				//output += spaces(indent) +"<th></th>";
				//output += lines(1);
				//output += spaces(indent) +"<td></td>";
			}
			else if(cv.getAttribType() == AttributeType.FOREIGNATTRIBUTE)
			{

				if(cv.isPrimary() && cv.getDomainClass().isUsesCompositeKey() )
				{
				
					for(Attribute a : ec.getEntity().getPrimaryKeyAttributes())
					{
					
						String elementId = "id."+a.getLowerLabel();
						output += spaces(indent) +"<label for=\""+elementId+"\">" +cv.getUpperIdentifier() + "</label>";
						output += lines(1);
					
						output += " <form:select path=\""+elementId+"\" items=\"${"+cv.getDomainClass().getLowerIdentifier()+"List}\" itemValue=\""+cv.getDomainClass().getPrimaryKeys().iterator().next().getLowerIdentifier()+"\" itemLabel=\""+cv.getDomainClass().getPrimaryKeys().iterator().next().getLowerIdentifier() +"\"/><br/>";
						output += lines(1);
						
					
					}
				}
				else
				{
				
				String elementId = cv.getIdentifier()+"."+cv.getDomainClass().getPrimaryKeys().iterator().next().getLowerIdentifier();
				output += spaces(indent) +"<label for=\""+elementId+"\">" +cv.getUpperIdentifier() + "</label>";
				output += lines(1);
			//	output += spaces(indent) + "<td><select id=\""+cv.getIdentifier()+"\" name=\""+cv.getAttribute().getReferencedEntity().getDomainClass().getEntity().getLowerLabel()+"Id\" >";
				output += " <form:select path=\""+elementId+"\" items=\"${"+cv.getDomainClass().getLowerIdentifier()+"List}\" itemValue=\""+cv.getDomainClass().getPrimaryKeys().iterator().next().getLowerIdentifier()+"\" itemLabel=\""+cv.getDomainClass().getPrimaryKeys().iterator().next().getLowerIdentifier() +"\"/><br/>";
				output += lines(1);
//				output += spaces(indent) + "<c:forEach items=\"${" + cv.getDomainClass().getLowerIdentifier()+ "List}\" var=\"item\" >";
//				String selected="";
//				if( cv.getDomainClass().getPrimaryKeys().size()>0 && cv.getAttribute().getReferencedEntity().getDomainClass().getPrimaryKeys().size()>0)
//				{
//					log.debug("creating select");
//				selected += "<c:if test=\"${";
//				selected += ec.getLowerIdentifier()+"."+cv.getIdentifier()+"." + cv.getDomainClass().getPrimaryKeys().iterator().next().getIdentifier(); 
//				selected += "== item."+ cv.getDomainClass().getPrimaryKeys().iterator().next().getIdentifier() +"}\">selected=\"true\"</c:if>";
//				output += spaces(indent) + "<option "+selected+" value=\"${item."+cv.getAttribute().getReferencedEntity().getDomainClass().getPrimaryKeys().iterator().next().getIdentifier() +"}\" >"+ cv.getAttribute().getReferencedEntity().getDomainClass().getSelectBoxLabel()+"</option>";
//				}
//				output += spaces(indent) + "</c:forEach>";
//				output += lines(1);
//				output += spaces(indent) + "</select></td>";

				output += lines(1);

				}
			}
			else 
			{
				if(cv.isPrimary())
				{
				output += spaces(indent) +"";
				output += lines(1);
				output += spaces(indent) +"<form:hidden path=\""+ cv.getIdentifier()+"\" />";
				}
				else 
				{
				
					String cssClass=null;
					if(cv.getType().equalsIgnoreCase("date"))
						cssClass="cssClass=\"dateInput\"";
					output += spaces(indent) +"<label for=\""+cv.getIdentifier()+"\">" +cv.getUpperIdentifier() + "</label>";
					output += lines(1);
					output += spaces(indent) +"<form:input path=\""+ cv.getIdentifier()+"\" "+(cssClass!=null ? cssClass : "")+"  /><br/>";
				}
			}
			
			output += lines(1);
			indent -=4;
			output += spaces(indent) + "";
			output += lines(1);
		}
		
		output += spaces(indent) + "";
		output += lines(1);
		indent +=4;
		output += spaces(indent) +"<label></label>";
		output += lines(1);
		output += spaces(indent) +"<input type=\"submit\" value=\"Save\" /><br/>";
		output += lines(1);
		indent -=4;
		output += spaces(indent) + "";
		output += lines(1);
		indent -=4;
		output += spaces(indent) + "</fieldset>";
		output += lines(1);
		output += spaces(indent) + "</form:form></div>";
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
		
		output += "<ul id=\"mainmenu\" >\n";
		output += "<li><a>Main</a><ul>\n";
		while(dcIter.hasNext())
		{
			DomainClass dc = dcIter.next();
			output += "<li><a href=\"<c:url value=\"/"+dc.getSchema().getUnqualifiedLabel() +"/" +dc.getLowerIdentifier().toLowerCase() + "/list.html\" />\" >"+dc.getIdentifier()+" List</a></li>";
			output += lines(1);
			
		}
		lines(1);
		output += "</ul></li></ul>\n";
		
	
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