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
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

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
	public JSPCodeGenerator(SpringHibernateModel model, String pathBase, String packageRoot,Properties properties) {
		super(model, pathBase, packageRoot);
		this.properties = properties;
		jspRoot = pathBase;
	}

	private static final Log log = LogFactory.getLog(JSPCodeGenerator.class);
	public String jspRoot;
	private Properties properties;

	public void generate() throws IOException {
		generateAllJSP(model.getDomainClassList());
	}
	
	public void generateAllJSP(List<DomainClass> ecList) throws IOException {
		generateMenu(ecList);
		Iterator<DomainClass> ecIter = ecList.iterator();
		while(ecIter.hasNext()) {
			DomainClass ec = ecIter.next();
			generateShowJSP(ec);
			generateListJSP(ec);
			generateListAltJSP(ec);
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
	
		boolean deOb = Boolean.parseBoolean(properties.getProperty("deobfuscate.column.names", "false"));
		
		Iterator<ClassVariable> cvIter = ec.getPrimaryKeys().iterator();
		output += "<h2>"+ec.getIdentifier()+"</h2>";
		output += lines(1);
		while(cvIter.hasNext()) {
			ClassVariable cv = cvIter.next();
			log.debug("ClassVariable:"+cv.getIdentifier());
			if(ec.isUsesCompositeKey() && cv.isPrimary()) {
				output += "<h2>";
				for(Attribute a : ec.getEntity().getPrimaryKeyAttributes()) {
					output += "${"+cv.getLowerIdentifier()+"."+a.getLowerLabel()+"}";
				}
				output += "</h2>";
			}else{
				output += "<h2> ${"+ec.getLowerIdentifier() +"."+ cv.getIdentifier()+"} </h2>";
			}
			output += lines(1);
		}
		
		output += spaces(indent) + "";
		lines(1);
		
		cvIter = ec.listAllIter();
		output += lines(1);
		
		output += spaces(indent) + "<table class=\"table table-bordered table-hover\">";
		indent += 4;
		
		while(cvIter.hasNext()) {
			
			ClassVariable cv = cvIter.next();

			output += lines(1);
			output += spaces(indent) + "<tr>";
			indent += 4;
			
			
			String th_label = cv.getUpperIdentifier();
			if( deOb && cv.getAttribute() != null ){
				th_label = "${ "+ec.getSchema().getLowerLabel()+":deobfuscateColumn ( '"+ec.getTableName()+"', '"+cv.getAttribute().getSqlLabel()+"') }";
			}
			
			log.debug("-ClassVariable:"+cv.getIdentifier());
			output += lines(1);
			output += spaces(indent) + "<th>" + th_label + "</th>";
			output += lines(1);
			
			String pkString = "";
		
			output += spaces(indent) + "<td>";
			output += lines(1);
			indent += 4;
			
			if(cv.getAttribType() == AttributeType.CHILD && cv.getAttribute().getEntity().getDomainClass() != null) {
				log.debug("isChild");
				Iterator<ClassVariable> pkIter = cv.getAttribute().getEntity().getDomainClass().getPrimaryKeys().iterator();
				while(pkIter.hasNext()) {
					ClassVariable pk = pkIter.next();
					pkString += pk.getIdentifier() + "=${item." + pk.getIdentifier() + "}";
					while(pkIter.hasNext()){
						pkString += "&";
					}
				}
				
				if(cv.getAttribute().getEntity().getDomainClass().isUsesCompositeKey()) {
					output += spaces(indent) +"not implemented<br/><br/>";
				}else{
					output += spaces(indent) + "<ul>";
					output += lines(1);
					indent += 4;
					
					output += spaces(indent) + "<c:forEach items=\"${"+ec.getLowerIdentifier() +"."+ cv.getIdentifier()+"}\" var=\"item\" varStatus=\"itemStatus\" >";
					output += lines(1);
					indent += 4;
					
					output += spaces(indent) +"<li><a href=\"../"+ cv.getAttribute().getEntity().getDomainClass().getLowerIdentifier().toLowerCase() +  "/edit.html?"+pkString+"\" > ${item."+cv.getAttribute().getEntity().getDomainClass().getPrimaryKey().getLowerIdentifier() + "}</a></li>";
					output += lines(1);
					indent -= 4;
					
					output += spaces(indent) +"</c:forEach>";
					output += lines(1);
					indent -= 4;
					
					output += spaces(indent) +"</ul>";
				}
			} else if( cv.isPrimary() && cv.getDomainClass().isUsesCompositeKey()) {
				String params = "";
				String label = "";
				for(Attribute a : ec.getEntity().getPrimaryKeyAttributes()) {
					String pk_label = a.getLowerLabel();
					if( deOb ){
						pk_label = "${ "+ec.getSchema().getLowerLabel()+":deobfuscateColumn ( '"+ec.getTableName()+"', '"+a.getSqlLabel()+"') }";
					}
					label += "("+pk_label+", ${ "+ec.getLowerIdentifier()+".id."+a.getLowerLabel()+" })";
					params += a.getLowerLabel() + "=${ " + ec.getLowerIdentifier()+".id."+a.getLowerLabel() + " }&";
				}
				params = params.substring(0, params.length()-1);
				output += spaces(indent) + label;
			} else {
				output += spaces(indent) + "${"+ec.getLowerIdentifier() +"."+ cv.getIdentifier()+"}";
			}

			indent -= 4;
			output += lines(1);
			output += spaces(indent) + "<td>";
			
			indent -= 4;
			output += lines(1);
			output += spaces(indent) + "</tr>";
		}
		
		output += lines(1);
		indent -= 4;
		output += spaces(indent) + "</table>";
		
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

		output += "<h2>"+ec.getIdentifier()+" List</h2>";
		output += lines(1);
		
		output += lines(1);
		output += spaces(indent) + "<a href=\"add.html\" class=\"btn\">Add</a>";
		output += lines(2);
		output += spaces(indent) + "<div id=\"error_div\" class=\"alert alert-error\" style=\"display: none;\">";
		output += lines(1);
		indent += 4;
		output += spaces(indent) + "<%-- div for showing errors, see messager.js.showMessage --%>";
		output += lines(1);
		indent -= 4;
		output += spaces(indent) + "</div>";
		output += lines(2);
		
		output += spaces(indent) + "<table id=\""+ec.getIdentifier().toLowerCase()+"Table\" class=\"table table-bordered table-striped table-hover\">";
		output += lines(1);
		indent += 4;
		output += spaces(indent) + "<%-- table filled by setDataTable call below --%>";
		output += lines(1);
		indent -= 4;
		output += spaces(indent) + "</table>";
		output += lines(2);
		
		output += spaces(indent) + "<c:url value=\"/"+ ec.getSchema().getUnqualifiedLabel() + "/" + ec.getIdentifier().toLowerCase()+"/datatable.html\" var=\"datatableUrl\">";
		indent += 4;
		output += lines(1);
		
		output += spaces(indent) + "<c:param name=\"display\" value=\"list\"/>";
		indent -= 4;
		output += lines(1);
		
		output += spaces(indent) + "</c:url>";
		output += lines(1);
		
		output += spaces(indent) + "<script type=\"text/javascript\">";
		output += lines(1);
		indent += 4;
		
		output += spaces(indent) + "var cols = [];";
		output += lines(1);
		
		boolean deOb = Boolean.parseBoolean(properties.getProperty("deobfuscate.column.names", "false"));
		
		ClassVariable cv;
		Iterator<ClassVariable> cvIter = ec.listAllIter();
		while(cvIter.hasNext()) {
			cv = cvIter.next();
			if ( cv.isPrimary() && ec.isUsesCompositeKey() ) {
				for(Attribute a : ec.getEntity().getPrimaryKeyAttributes()) {
					output += spaces(indent) + "cols.push({ \"sName\": \"" + a.getLowerLabel() + "\", \"sTitle\":\"" +  ( deOb ? " ${ "+ec.getSchema().getLowerLabel()+":deobfuscateColumn ( '"+ec.getTableName()+"', '"+a.getSqlLabel()+"') } " : a.getLabel() ) + "\",	\"sClass\":\"\", \"bSortable\":true, \"bSearchable\": true });";
					output += lines(1);
				}
			} else {
				if( RelationshipType.NONE == cv.getRelationshipType() ){
					output += spaces(indent) + "cols.push({ \"sName\": \"" + cv.getLowerIdentifier() + "\", \"sTitle\":\"" +  ( deOb ? " ${ "+ec.getSchema().getLowerLabel()+":deobfuscateColumn ( '"+ec.getTableName()+"', '"+cv.getAttribute().getSqlLabel() +"') } " : cv.getUpperIdentifier() ) +  "\",	\"sClass\":\"\", \"bSortable\":true, \"bSearchable\": true });";
					output += lines(1);
				} else {
					output += spaces(indent) + "cols.push({ \"sName\": \"" + cv.getLowerIdentifier() + "\", \"sTitle\":\"" +  ( deOb ? " ${ "+ec.getSchema().getLowerLabel()+":deobfuscateColumn ( '"+ec.getTableName()+"', '"+cv.getAttribute().getSqlLabel() +"') } " : cv.getUpperIdentifier() ) + "\",	\"sClass\":\"\", \"bSortable\":false, \"bSearchable\": false });";
					output += lines(1);
				}
			}
		}
		output += spaces(indent) + "cols.push({ \"sName\": \"urls\", \"sTitle\":\"\", \"sClass\":\"\", \"bSortable\":false, \"bSearchable\": false });";
		output += lines(1);
		output += spaces(indent) + "setDataTable('"+ec.getIdentifier().toLowerCase()+"Table',10,0,'${datatableUrl}',cols,undefined,true);";
		indent -= 4;
		output += lines(1);
		output += spaces(indent) + "</script>";
		
//		output += spaces(indent) + "<table class=\"table table-bordered table-striped table-hover table-datatable\">";
//		output += lines(1);
//		indent += 4;
//		
//		output += spaces(indent) + "<thead>";
//		output += lines(1);
//		indent += 4;
//	
//		Iterator<ClassVariable> cvIter = ec.listAllIter();
//		output += spaces(indent) + "<tr>";
//		output += lines(1);
//		indent += 4;
//
//		while(cvIter.hasNext()) {
//			ClassVariable cv = cvIter.next();	
//			output += spaces(indent) +"<th>" +cv.getUpperIdentifier() + "</th>";
//			output += lines(1);
//		}
//		output += spaces(indent) + "<th></th>";
//		output += lines(1);
//		indent -= 4;
//		
//		output += spaces(indent) + "</tr>";
//		output += lines(1);
//		indent -=4;
//
//		output += spaces(indent) + "</thead>";
//		output += lines(1);
//		
//		cvIter = ec.listAllIter();
//		output += spaces(indent) + "<tbody>";
//		
//		output += lines(1);
//		indent += 4;
//		
//		output += spaces(indent) + "<c:forEach items=\"${"+ec.getLowerIdentifier()+"List}\" var=\""+ec.getLowerIdentifier()+"\"  varStatus=\"status\">";
//		output += lines(1);
//		indent += 4;
//		
//		output += spaces(indent) + "<tr>";
//		output += lines(1);
//		indent +=4;
//		
//		String links="";
//		while(cvIter.hasNext()) {
//			
//			ClassVariable cv = cvIter.next();
//			log.debug(cv.getLowerIdentifier()+" : "+cv.getRelationshipType()+" : "+cv.getAttribType());
//			
//			if (cv.isPrimary()) {
//				if(ec.isUsesCompositeKey() && cv.isPrimary()) {
//
//					List<String[]> compositeKeys = new ArrayList<String[]>();
//					for(Attribute a : ec.getEntity().getPrimaryKeyAttributes()) {
//						compositeKeys.add(new String[] {a.getLowerLabel(), ec.getLowerIdentifier()+".id."+a.getLowerLabel() });
//					}
//				
//					String params = "";
//					String label = "";
//					for(String[] starray :compositeKeys) {
//						params += starray[0]+"=${"+starray[1] + "}&";
//						label += "("+starray[0]+",${"+starray[1]+"})";
//					}
//					params = params.substring(0, params.length()-1);
//					links += "<td><a href=\"edit.html?"+params+"\">edit</a> ";
//					links += "<a href=\"show.html?"+params+"\">view</a>";
//					links += " <a href=\"delete.html?"+params+"\">delete</a></td>";
//					output += "<td><a href=\"edit.html?"+params+"\">"+label+"</a></td> ";
//				} else {
//					links += spaces(indent) + "<td>";
//					indent += 4;
//					links += lines(1);
//					links += spaces(indent) + "<a href=\"edit.html?"+cv.getIdentifier()+"=${" +ec.getLowerIdentifier()+"."+cv.getIdentifier() + "}\">edit</a> ";
//					links += lines(1);
//					links += spaces(indent) + "<a href=\"show.html?"+cv.getIdentifier()+"=${" +ec.getLowerIdentifier()+"."+cv.getIdentifier() + "}\">view</a>";
//					links += lines(1);
//					links += spaces(indent) + "<a href=\"delete.html?"+cv.getIdentifier()+"=${" +ec.getLowerIdentifier()+"."+cv.getIdentifier() + "}\">delete</a>";
//					links += lines(1);
//					indent -= 4;
//					links += spaces(indent) + "</td>";
//					
//					output += spaces(indent) +"<td><a href=\"edit.html?"+cv.getIdentifier()+"=${" +ec.getLowerIdentifier()+"."+cv.getIdentifier() + "}\">${" +ec.getLowerIdentifier()+"."+cv.getIdentifier() + "}</a></td>";
//				}
//			} else if( cv.getRelationshipType() == RelationshipType.ONETOMANY) {
//				output += spaces(indent) +"<td>"+cv.getLowerIdentifier() + "</td>";
//	//			output += spaces(indent) +"<td>${" +ec.getLowerIdentifier()+"."+cv.getDomainClass().getLowerIdentifier()+"."+cv.getDomainClass().getPrimaryKey().getIdentifier() + "}</td>";
//			} else if( cv.getRelationshipType() == RelationshipType.MANYTOMANY) {
//				//output += spaces(indent) +"<td>${" +ec.getLowerIdentifier()+"."+cv.getLowerIdentifier() + "}</td>";
//				output += spaces(indent) +"<td>"+cv.getLowerIdentifier() + "</td>";
//			} else if(cv.getRelationshipType() == RelationshipType.MANYTOONE){
//				output += spaces(indent) +"<td>${" +ec.getLowerIdentifier()+"."+cv.getDomainClass().getLowerIdentifier()+"."+cv.getDomainClass().getPrimaryKey().getLowerIdentifier() + "}</td>";
//			} else if(cv.getAttribType() == AttributeType.CHILD){
//				output += spaces(indent) +"<td>"+cv.getIdentifier() + "</td>";
//			}else{
//				output += spaces(indent) +"<td>${" +ec.getLowerIdentifier()+"."+cv.getIdentifier() + "}</td>";
//			}
//			output += lines(1);
//		}
//		
//		output += links;
//		indent -= 4;
//		output += lines(1);
//		
//		output += spaces(indent) + "</tr>";
//		output += lines(1);
//		indent -= 4;
//		
//		output += spaces(indent) + "</c:forEach>";
//		indent -= 4;
//		output += lines(1);
//		
//		output += spaces(indent) + "</tbody>";
//		indent -= 4;
//		output += lines(1);
//		
//		output += spaces(indent) + "</table>";
	
		File file = new File(jspFile);
		FileWriter fstream = new FileWriter(file);
		BufferedWriter out = new BufferedWriter(fstream);
		out.write(output);
		out.close();
		log.debug(".........done");
	}
	
	private void generateListAltJSP(DomainClass ec) throws IOException {
		
		boolean deOb = Boolean.parseBoolean(properties.getProperty("deobfuscate.column.names", "false"));
		
		log.debug("GeneratingListJSP:"+ec.getIdentifier());
		log.debug("...."+ ec.getIdentifier());
		String directory =  jspRoot + "/"+ ec.getSchema().getUnqualifiedLabel() + "/" + ec.getIdentifier().toLowerCase();
		
		(new File(directory )).mkdirs();
		
		String jspFile = directory+ "/list_alt.jsp";
		int indent = 0;
		
		String output= spaces(indent) + "<%@ include file=\"/WEB-INF/include.jsp\"  %>";
		output += lines(2);

		output += "<h2>"+ec.getIdentifier()+" List</h2>";
		output += lines(1);
		
		output += lines(1);
		output += spaces(indent) + "<a href=\"add.html\" class=\"btn\">Add</a>";
		output += lines(2);
		output += spaces(indent) + "<div id=\"error_div\" class=\"alert alert-error\" style=\"display: none;\">";
		output += lines(1);
		indent += 4;
		output += spaces(indent) + "<%-- div for showing errors, see messager.js.showMessage --%>";
		output += lines(1);
		indent -= 4;
		output += spaces(indent) + "</div>";
		output += lines(2);
		
		output += spaces(indent) + "<table class=\"table table-bordered table-striped table-hover\">";
		output += lines(1);
		indent += 4;
		
		output += spaces(indent) + "<thead>";
		output += lines(1);
		indent += 4;
	
		Iterator<ClassVariable> cvIter = ec.listAllIter();
		output += spaces(indent) + "<tr>";
		output += lines(1);
		indent += 4;
		while(cvIter.hasNext()) {
			ClassVariable cv = cvIter.next();
			if( deOb && cv.getAttribute() != null ){
				output += spaces(indent) + "<th>${ "+ec.getSchema().getLowerLabel()+":deobfuscateColumn ( '"+ec.getTableName()+"', '"+cv.getAttribute().getSqlLabel()+"') }</th>";
			}else{
				output += spaces(indent) +"<th>" +cv.getUpperIdentifier() + "</th>";
			}
			output += lines(1);
		}
		output += spaces(indent) + "<th></th>";
		output += lines(1);
		indent -= 4;
		
		output += spaces(indent) + "</tr>";
		output += lines(1);
		indent -=4;

		output += spaces(indent) + "</thead>";
		output += lines(1);
		
		cvIter = ec.listAllIter();
		output += spaces(indent) + "<tbody>";
		
		output += lines(1);
		indent += 4;
		
		output += spaces(indent) + "<c:forEach items=\"${"+ec.getLowerIdentifier()+"List}\" var=\""+ec.getLowerIdentifier()+"\"  varStatus=\"status\">";
		output += lines(1);
		indent += 4;
		
		output += spaces(indent) + "<tr>";
		output += lines(1);
		indent +=4;
		
		String links="";
		while(cvIter.hasNext()) {
			
			ClassVariable cv = cvIter.next();
			log.debug(cv.getLowerIdentifier()+" : "+cv.getRelationshipType()+" : "+cv.getAttribType());
			
			if (cv.isPrimary()) {
				if(ec.isUsesCompositeKey() && cv.isPrimary()) {

					String params = "";
					String label = "";
					for(Attribute a : ec.getEntity().getPrimaryKeyAttributes()) {
						String pk_label = a.getLowerLabel();
						if( deOb ){
							pk_label = "${ "+ec.getSchema().getLowerLabel()+":deobfuscateColumn ( '"+ec.getTableName()+"', '"+a.getSqlLabel()+"') }";
						}
						label += "("+pk_label+", ${ "+ec.getLowerIdentifier()+".id."+a.getLowerLabel()+" })";
						params += a.getLowerLabel() + "=${ " + ec.getLowerIdentifier()+".id."+a.getLowerLabel() + " }&";
					}
					
					params = params.substring(0, params.length()-1);
					
					output += spaces(indent) + "<td>";
					output += lines(1);
					indent += 4;
					output += spaces(indent) + "<a href=\"edit.html?"+params+"\">"+label+"</a>";
					indent -= 4;
					output += lines(1);
					output += spaces(indent) + "</td>";
					
					links += spaces(indent) + "<td>";
					indent += 4;
					links += lines(1);
					links += spaces(indent) + "<a href=\"edit.html?"+params+"\">edit</a> ";
					links += lines(1);
					links += spaces(indent) + "<a href=\"show.html?"+params+"\">view</a>";
					links += lines(1);
					links += spaces(indent) + "<a href=\"delete.html?"+params+"\">delete</a>";
					links += lines(1);
					indent -= 4;
					links += spaces(indent) + "</td>";
				} else {
					links += spaces(indent) + "<td>";
					indent += 4;
					links += lines(1);
					links += spaces(indent) + "<a href=\"edit.html?"+cv.getIdentifier()+"=${" +ec.getLowerIdentifier()+"."+cv.getIdentifier() + "}\">edit</a> ";
					links += lines(1);
					links += spaces(indent) + "<a href=\"show.html?"+cv.getIdentifier()+"=${" +ec.getLowerIdentifier()+"."+cv.getIdentifier() + "}\">view</a>";
					links += lines(1);
					links += spaces(indent) + "<a href=\"delete.html?"+cv.getIdentifier()+"=${" +ec.getLowerIdentifier()+"."+cv.getIdentifier() + "}\">delete</a>";
					links += lines(1);
					indent -= 4;
					links += spaces(indent) + "</td>";
					
					output += spaces(indent) +"<td><a href=\"edit.html?"+cv.getIdentifier()+"=${" +ec.getLowerIdentifier()+"."+cv.getIdentifier() + "}\">${" +ec.getLowerIdentifier()+"."+cv.getIdentifier() + "}</a></td>";
				}
			} else if( cv.getRelationshipType() == RelationshipType.ONETOMANY) {
				output += spaces(indent) +"<td>"+cv.getLowerIdentifier() + "</td>";
	//			output += spaces(indent) +"<td>${" +ec.getLowerIdentifier()+"."+cv.getDomainClass().getLowerIdentifier()+"."+cv.getDomainClass().getPrimaryKey().getIdentifier() + "}</td>";
			} else if( cv.getRelationshipType() == RelationshipType.MANYTOMANY) {
				//output += spaces(indent) +"<td>${" +ec.getLowerIdentifier()+"."+cv.getLowerIdentifier() + "}</td>";
				output += spaces(indent) +"<td>"+cv.getLowerIdentifier() + "</td>";
			} else if(cv.getRelationshipType() == RelationshipType.MANYTOONE){
				output += spaces(indent) +"<td>${" +ec.getLowerIdentifier()+"."+cv.getDomainClass().getLowerIdentifier()+"."+cv.getDomainClass().getPrimaryKey().getLowerIdentifier() + "}</td>";
			} else if(cv.getAttribType() == AttributeType.CHILD){
				output += spaces(indent) +"<td>"+cv.getIdentifier() + "</td>";
			}else{
				output += spaces(indent) +"<td>${" +ec.getLowerIdentifier()+"."+cv.getIdentifier() + "}</td>";
			}
			output += lines(1);
		}
		
		output += links;
		indent -= 4;
		output += lines(1);
		
		output += spaces(indent) + "</tr>";
		output += lines(1);
		indent -= 4;
		
		output += spaces(indent) + "</c:forEach>";
		indent -= 4;
		output += lines(1);
		
		output += spaces(indent) + "</tbody>";
		indent -= 4;
		output += lines(1);
		
		output += spaces(indent) + "</table>";
	
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
		
		Iterator<ClassVariable> cvIter = ec.getPrimaryKeys().iterator();
		output += lines(1);
		output += lines(1);
		while(cvIter.hasNext()) {
			ClassVariable cv = cvIter.next();
			if(ec.isUsesCompositeKey() && cv.isPrimary()) {
				output += "";
			} else {
				// output += "<h2>${"+ec.getLowerIdentifier() +"."+ cv.getIdentifier()+"}</h2>";
				output += "";
			}
			output += lines(1);
		}
		
		output += "<form:form method=\"post\" commandName=\""+ec.getLowerIdentifier()+"\" action=\"save.html\" >";
		output += lines(1);
		indent += 4;
		
		output += spaces(indent) + "<fieldset>";
		output += lines(1);
		indent += 4;
		
		output += spaces(indent) + "<legend>"+ec.getIdentifier()+"</legend>";
		
		boolean deOb = Boolean.parseBoolean(properties.getProperty("deobfuscate.column.names", "false"));
		
		
		cvIter = ec.listAllIter();;
		while(cvIter.hasNext()) {
			
			output += lines(1);
			ClassVariable cv = cvIter.next();
			if(ec.isUsesCompositeKey() && cv.isPrimary()) {
				for(Attribute a : ec.getEntity().getPrimaryKeyAttributes()) {
					if(!a.isForeign()){
						String label = a.getLowerLabel();
						if( deOb ){
							label = "${ "+ec.getSchema().getLowerLabel()+":deobfuscateColumn ( '"+ec.getTableName()+"', '"+a.getSqlLabel()+"') }";
						}
						output += lines(1);
						output += spaces(indent) + "<label for=\"id."+a.getLowerLabel()+"\">"+label+"</label>";
						output += lines(1);
						output += spaces(indent) + "<form:input path=\"id."+a.getLowerLabel()+"\" /><br/>";
						output += lines(1);
					}
				}
			} else if( cv.getAttribType() == AttributeType.CHILD ) {
				//output += spaces(indent) +"<th></th>";
				//output += lines(1);
				//output += spaces(indent) +"<td></td>";
			} else if(cv.getAttribType() == AttributeType.FOREIGNATTRIBUTE) {
				if(cv.isPrimary() && cv.getDomainClass().isUsesCompositeKey() ) {
					for(Attribute a : ec.getEntity().getPrimaryKeyAttributes()) {
						
						String label = cv.getUpperIdentifier();
						if( deOb ){
							label = "${ "+ec.getSchema().getLowerLabel()+":deobfuscateColumn ( '"+ec.getTableName()+"', '"+cv.getAttribute().getSqlLabel()+"') }";
						}
						
						String elementId = "id."+a.getLowerLabel();
						output += spaces(indent) +"<label for=\""+elementId+"\">" + label + "</label>";
						output += lines(1);
						output += "<form:select path=\""+elementId+"\" items=\"${"+cv.getDomainClass().getLowerIdentifier()+"List}\" itemValue=\""+cv.getDomainClass().getPrimaryKeys().iterator().next().getLowerIdentifier()+"\" itemLabel=\""+cv.getDomainClass().getPrimaryKeys().iterator().next().getLowerIdentifier() +"\"/>";
						output += lines(1);
						output += "<br/>";
					}
				} else {
					
					String label = cv.getUpperIdentifier();
					if( deOb ){
						label = "${ "+ec.getSchema().getLowerLabel()+":deobfuscateColumn ( '"+ec.getTableName()+"', '"+cv.getAttribute().getSqlLabel()+"') }";
					}
					
					String elementId = cv.getIdentifier()+"."+cv.getDomainClass().getPrimaryKeys().iterator().next().getLowerIdentifier();
					output += spaces(indent) +"<label for=\""+elementId+"\">" + label + "</label>";
					output += lines(1);
				//	output += spaces(indent) + "<td><select id=\""+cv.getIdentifier()+"\" name=\""+cv.getAttribute().getReferencedEntity().getDomainClass().getEntity().getLowerLabel()+"Id\" >";
					output += " <form:select path=\""+elementId+"\" items=\"${"+cv.getDomainClass().getLowerIdentifier()+"List}\" itemValue=\""+cv.getDomainClass().getPrimaryKeys().iterator().next().getLowerIdentifier()+"\" itemLabel=\""+cv.getDomainClass().getPrimaryKeys().iterator().next().getLowerIdentifier() +"\"/>";
					output += lines(1);
					output += "<br/>";
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
			} else {
				if(cv.isPrimary()) {
					output += spaces(indent) +"";
					output += lines(1);
					output += spaces(indent) +"<form:hidden path=\""+ cv.getIdentifier()+"\" />";
				} else {
					
					String label = cv.getUpperIdentifier();
					if( deOb ){
						label = "${ "+ec.getSchema().getLowerLabel()+":deobfuscateColumn ( '"+ec.getTableName()+"', '"+cv.getAttribute().getSqlLabel()+"') }";
					}
					
					String cssClass = null;
					if(cv.getType().equalsIgnoreCase("date")){
						cssClass="cssClass=\"dateInput\"";
					}
					output += spaces(indent) + "<label for=\""+cv.getIdentifier()+"\">" + label + "</label>";
					output += lines(1);
					output += spaces(indent) + "<form:input path=\""+ cv.getIdentifier()+"\" "+(cssClass!=null ? cssClass : "")+" />";
					output += lines(1);
					output += spaces(indent) + "<br/>";
				}
			}
			output += lines(1);
			output += spaces(indent) + "";
		}
		
		output += lines(1);
		output += spaces(indent) + "<input type=\"submit\" value=\"Save\" class=\"btn btn-primary\" />";
		output += lines(1);
		output += spaces(indent) + "<button type=\"button\" class=\"btn\" id=\"cancel_button\">Cancel</button>";
		output += lines(1);
		output += spaces(indent) + "<script type=\"text/javascript\">";
		output += lines(1);
		indent += 4;
		output += spaces(indent) + "$('#cancel_button').click(function( e ){ e.preventDefault(); window.location.href = 'list.html'; });";
		output += lines(1);
		indent -= 4;
		output += spaces(indent) + "</script>";
		output += lines(1);
		indent -= 4;
		
		output += spaces(indent) + "";
		output += lines(1);
		output += spaces(indent) + "</fieldset>";
		indent -= 4;
		output += lines(1);
		output += "</form:form>";
		
		
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
		for (int i = 0; i < num; i++){
			out += "\n";
		}
		return out;
	}

	private String spaces(int num) {
		String out = "";
		for (int i = 0; i < num; i++){
			out += " ";
		}
		return out;
	}
}