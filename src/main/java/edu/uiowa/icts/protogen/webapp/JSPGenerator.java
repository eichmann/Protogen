/*
 * Created on May 20, 2008
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.uiowa.icts.protogen.webapp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Enumeration;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.uiowa.icts.protogen.model.Attribute;
import edu.uiowa.icts.protogen.model.Database;
import edu.uiowa.icts.protogen.model.Entity;
import edu.uiowa.icts.protogen.model.Relationship;
import edu.uiowa.icts.protogen.model.Schema;

public class JSPGenerator {
	static Logger log = LogManager.getLogger(JSPGenerator.class);

	String tagLibraryPrefix = null;
    String webAppPath = null;
    String webAppName = null;
    String packagePrefix = null;
    String projectName = null;

    boolean multiSchema = false;
    File packagePrefixDirectory = null;
    File tagDirectory = null;
    Path sourceResourcePath = null;

    public JSPGenerator(String webAppPath, String packagePrefix, String projectName) {
        this.webAppPath = webAppPath;
        this.packagePrefix = packagePrefix;
        this.projectName = projectName;
        this.tagLibraryPrefix = packagePrefix.substring(packagePrefix.lastIndexOf('.') + 1);
    }
    
    public JSPGenerator(String webAppPath, String packagePrefix, String projectName, String tagLibraryPrefix) {
        this.webAppPath = webAppPath;
        this.packagePrefix = packagePrefix;
        this.projectName = projectName;
        this.tagLibraryPrefix = tagLibraryPrefix;
    }
    
    public void setResourcesPath(String path) {
    	sourceResourcePath = Paths.get(path);
    }

    public void generateJSPs(Database theDatabase) throws IOException {
    	multiSchema = theDatabase.getSchemas().size() > 1;
    	webAppName = webAppPath.substring(webAppPath.indexOf("..")+3);
    	webAppName = webAppName.substring(0, webAppName.indexOf('/'));
    	log.info("webApp name: " + webAppName);
    	
        generateEntityDirectories(theDatabase);
        
        if (sourceResourcePath != null)
        	copyFolder(sourceResourcePath, Paths.get(webAppPath + "resources"));

        generateNav(theDatabase);
        generateBranding(theDatabase);
        
        generateMenu(theDatabase);
        generateIndex(theDatabase);
        generateInclude(theDatabase);
        generateHead(theDatabase);
        generateHeader(theDatabase);
        generateFooter(theDatabase);
        generateDbtest(theDatabase);

        Enumeration<Schema> schemaEnum = theDatabase.getSchemas().elements();
        while (schemaEnum.hasMoreElements()) {
            Schema theSchema = schemaEnum.nextElement();
            Enumeration<Entity> entityEnum = theSchema.getEntities().elements();
            while (entityEnum.hasMoreElements()) {
                Entity theEntity = entityEnum.nextElement();
                
                generateAddEditEntityJSP(theSchema, theEntity);
                generateAddEditEntityJSP(theSchema, theEntity, false);
                generateDeleteEntityJSP(theSchema, theEntity);
                generateEntityJSP(theSchema, theEntity);
                generateEntityListJSP(theSchema, theEntity);
                
                // generateAddEntityJSP(theSchema, theEntity);
                
                if (theEntity.hasBinaryDomainAttribute()|| theEntity.hasImage()) {
                    generateUploadEntityJSP(theSchema, theEntity);
                }
            }
        }
    }

	private String entityPathPrefix(Schema theSchema) {
		if (multiSchema)
			return webAppPath + theSchema.getLowerLabel() + "/";
		else
			return webAppPath;
	}

    /**
	 * @param theSchema
	 * @param theEntity
     * @throws IOException 
	 */
	private void generateDeleteEntityJSP(Schema theSchema, Entity theEntity) throws IOException {
		File f = new File(entityPathPrefix(theSchema) + theEntity.getUnqualifiedLowerLabel() + "/delete.jsp");
        FileWriter fstream = new FileWriter(f);
        BufferedWriter out = new BufferedWriter(fstream);
        out.write("<%@ include file=\"/_include.jsp\"  %>\n\n");
        
        for (int i = 0; i < theEntity.getAttributes().size(); i++) {
            Attribute theAttribute = theEntity.getAttributes().elementAt(i);
            if (theAttribute.isPrimary()){
            	out.write("<c:if test=\"${ empty param." + theAttribute.getLabel() + " }\">\n");
            	out.write("\t<c:redirect url=\"list.jsp\"/>\n");
            	out.write("</c:if>\n\n");
            }
        }
        
        for (int i = 0; i < theEntity.getAttributes().size(); i++) {
            Attribute theAttribute = theEntity.getAttributes().elementAt(i);
            if (theAttribute.isPrimary() && theAttribute.isInt())
                out.write("<fmt:parseNumber var=\"" + theAttribute.getLabel() + "\" value=\"${param." + theAttribute.getLabel() + "}\" />\n");
            if (theAttribute.isPrimary() && theAttribute.isDateTime())
                out.write("<fmt:parseDate var=\"" + theAttribute.getLabel() + "\" value=\"${param." + theAttribute.getLabel() + "}\" pattern=\"yyyy-MM-dd HH:mm:ss.S\" />\n");
        }
        
        out.write("\n<" + tagLibraryPrefix + ":delete" + theEntity.getUpperLabel());
        for (int i = 0; i < theEntity.getAttributes().size(); i++) {
            Attribute theAttribute = theEntity.getAttributes().elementAt(i);
            if (theAttribute.isPrimary()){
            	out.write(" " + theAttribute.getLabel() + "=\"${" + (theAttribute.isInt() || theAttribute.isDateTime() ? "" : "param.") + theAttribute.getLabel() + "}\"");
            }
        }
        out.write("/>\n\n");
        
        out.write("<c:redirect url=\"list.jsp\"/>");
        out.flush();
        out.close();
	}

	/**
	 * @param theDatabase
     * @throws IOException 
	 */
	private void generateBranding(Database theDatabase) throws IOException {
		File f  = new File(webAppPath + "branding.jsp");
        FileWriter fstream = new FileWriter(f);
        BufferedWriter out = new BufferedWriter(fstream);
        out.write("<%@ include file=\"/_include.jsp\"  %>\n");
        out.write("<img src=\"<c:url value=\"/resources/images/logo.png\" />\" alt=\"logo\">\n");
        out.close();
	}

	/**
	 * @param theDatabase
	 * @throws IOException 
	 */
	private void generateNav(Database theDatabase) throws IOException {
		File f  = new File(webAppPath + "nav.jsp");
        FileWriter fstream = new FileWriter(f);
        BufferedWriter out = new BufferedWriter(fstream);
        out.write("\n");
        out.close();
	}

	/**
	 * @param theDatabase
     * @throws IOException 
	 */
	private void generateInclude(Database theDatabase) throws IOException {
		File f  = new File(webAppPath + "_include.jsp");
        FileWriter fstream = new FileWriter(f);
        BufferedWriter out = new BufferedWriter(fstream);
		out.write("<%@ taglib prefix=\"sql\" uri=\"http://java.sun.com/jsp/jstl/sql\"%>\n");
        out.write("<%@ taglib prefix=\"c\" uri=\"http://java.sun.com/jsp/jstl/core\"%>\n");
        out.write("<%@ taglib prefix=\"fmt\" uri=\"http://java.sun.com/jsp/jstl/fmt\"%>\n");
        out.write("<%@ taglib prefix=\"" + tagLibraryPrefix + "\" uri=\"http://icts.uiowa.edu/" + packagePrefix.substring(packagePrefix.lastIndexOf('.')+1) + "\"%>\n");
        out.flush();
        out.close();
	}

	/**
	 * @param theDatabase
     * @throws IOException 
	 */
	private void generateMenu(Database theDatabase) throws IOException {
		File theIndexJSP  = new File(webAppPath + "menu.jsp");
        FileWriter fstream = new FileWriter(theIndexJSP);
        BufferedWriter out = new BufferedWriter(fstream);
        
        generateHeaderPrefix(out);
        
        out.write("<ul>\n");
        out.write(spaces(4)+"<li><a href=\"<c:url value=\"/index.jsp\" />\">Home</a></li>");
        Enumeration<Schema> schemaEnum = theDatabase.getSchemas().elements();
        while (schemaEnum.hasMoreElements()) {
            Schema theSchema = schemaEnum.nextElement();
            Enumeration<Entity> entityEnum = theSchema.getEntities().elements();
            while (entityEnum.hasMoreElements()) {
                Entity theEntity = entityEnum.nextElement();
                // skip over subordinate entities
                // if (theEntity.getParents().size() > 0)
                //   continue;
                out.write(spaces(4)+"<li><a href=\"<c:url value=\"/" + theEntity.getUnqualifiedLowerLabel() + "/list.jsp\" /> \">" + theEntity.getUnqualifiedLabel() + " list</a></li>\n");
            }
        }
        out.write("</ul>\n");
        
        out.close();
		
	}
	
	private void generateEntityDirectories(Database theDatabase) throws IOException {
        Enumeration<Schema> schemaEnum = theDatabase.getSchemas().elements();
        while (schemaEnum.hasMoreElements()) {
            Schema theSchema = schemaEnum.nextElement();
            if (multiSchema) {
				File schemaDir = new File(webAppPath + theSchema.getLowerLabel());
				if (schemaDir.exists()) {
					if (schemaDir.isFile())
						throw new IOException("webapp directory " + schemaDir.getAbsolutePath() + " is a normal file");
				} else {
					schemaDir.mkdir();
				} 
			}
			Enumeration<Entity> entityEnum = theSchema.getEntities().elements();
            while (entityEnum.hasMoreElements()) {
                Entity theEntity = entityEnum.nextElement();
                File entityDir = new File(entityPathPrefix(theSchema) + theEntity.getUnqualifiedLowerLabel());
                if (entityDir.exists()) {
                    if (entityDir.isFile())
                        throw new IOException("webapp directory " + entityDir.getAbsolutePath() + " is a normal file");
                } else {
                    entityDir.mkdir();
                }
            }
        }
    }
    
    public void generateIndex(Database theDatabase) throws IOException {
        File theIndexJSP  = new File(webAppPath + "index.jsp");
        FileWriter fstream = new FileWriter(theIndexJSP);
        BufferedWriter out = new BufferedWriter(fstream);

        generateHeaderPrefix(out);
        generateHeaderBlock(out, false, false, true);
        
        out.write(spaces(24)+"<h1>Hello World.</h1>\n");
        out.write(spaces(24)+"<p>This interface was synthesized from the " + projectName + " clay diagram using Protogen\n");
        
        generateFooterBlock(out, false);
        
        out.close();
    }
    
    public void generateEntityListJSP(Schema theSchema, Entity theEntity) throws IOException {
        File theIndexJSP = new File(entityPathPrefix(theSchema) + theEntity.getUnqualifiedLowerLabel() + "/list.jsp");
        FileWriter fstream = new FileWriter(theIndexJSP);
        BufferedWriter out = new BufferedWriter(fstream);

        generateHeaderPrefix(out);
        
        generateHeaderBlock(out, false, false, true);
        
        generateEntityListBlock(out, theSchema, theEntity);

        generateFooterBlock(out, true);

        out.close();
    }
    
    public void generateEntityJSP(Schema theSchema, Entity theEntity) throws IOException {
        File theIndexJSP = new File(entityPathPrefix(theSchema) + theEntity.getUnqualifiedLowerLabel() + "/show.jsp");
        FileWriter fstream = new FileWriter(theIndexJSP);
        BufferedWriter out = new BufferedWriter(fstream);

        generateHeaderPrefix(out);
        
        Attribute keyAttribute = (theEntity.getSubKeyAttributes().size() == 0 ? theEntity.getPrimaryKeyAttributes().firstElement() : theEntity.getSubKeyAttributes().firstElement());
        generateHeaderBlock(out, true, theEntity.hasInt() || theEntity.hasDateTime(), true, 8);

        for (int i = 0; i < theEntity.getAttributes().size(); i++) {
            Attribute theAttribute = theEntity.getAttributes().elementAt(i);
            if (theAttribute.isPrimary() && theAttribute.isInt())
                out.write("<fmt:parseNumber var=\"" + theAttribute.getLabel() + "\" value=\"${param." + theAttribute.getLabel() + "}\" />\n");
            if (theAttribute.isPrimary() && theAttribute.isDateTime())
                out.write("<fmt:parseDate var=\"" + theAttribute.getLabel() + "\" value=\"${param." + theAttribute.getLabel() + "}\" pattern=\"yyyy-MM-dd HH:mm:ss.S\" />\n");
        }
        out.write("<" + tagLibraryPrefix + ":" + theEntity.getLowerLabel());
        for (int i = 0; i < theEntity.getAttributes().size(); i++) {
            Attribute theAttribute = theEntity.getAttributes().elementAt(i);
            if (theAttribute.isPrimary())
                out.write(" " + theAttribute.getLabel() + "=\"${" + (theAttribute.isInt() || theAttribute.isDateTime() ? "" : "param.") + theAttribute.getLabel() + "}\"");
        }
        out.write(">\n");

        out.write("\t<h2>" + theEntity.getLabel() + ":");
        for (int i = 0; i < theEntity.getAttributes().size(); i++) {
            Attribute theAttribute = theEntity.getAttributes().elementAt(i);
            if (theAttribute.isPrimary())
                out.write(" <" + tagLibraryPrefix + ":" + theEntity.getLowerLabel() + "" + theAttribute.getUpperLabel() + " />");
        }
        out.write("</h2>\n");
        
        if (theEntity.getParents().size() > 0) {
            out.write("<ul>\n");
        	for (Relationship relationship : theEntity.getParents()) {
        		Entity parent = relationship.getSourceEntity();
                out.write("\t<li><a href=\"../" + parent.getUnqualifiedLowerLabel() + "/show.jsp");
                boolean first = true;
                for (String parentLabel : relationship.getSourceAttributes()) {
                	String effectiveLabel = parentLabel.equals("id") ? "ID" : parentLabel;
					out.write((first ? "?" : "&") + effectiveLabel + "=<" + tagLibraryPrefix + ":" + theEntity.getUnqualifiedLowerLabel()
					        + theEntity.getAttributeBySQLLabel(relationship.getForeignReferencedAttribute(parentLabel)).getUpperLabel() + " />");
               }
                out.write("\">" + parent.getLabel() + "</a>\n");
        	}
            out.write("</ul>\n");
        }

        out.write("\t\t<table border=1>\n");
        out.write("\t\t\t<tr>\n");
        for (int i = 0; i < theEntity.getAttributes().size(); i++) {
            Attribute theAttribute = theEntity.getAttributes().elementAt(i);
            out.write("\t\t\t<th>" + theAttribute.getUpperLabel() + "</th>\n");
        }
        out.write("\t\t\t</tr>\n");
        out.write("\t\t\t<tr>\n");
        for (int i = 0; i < theEntity.getAttributes().size(); i++) {
        	
        	
            Attribute theAttribute = theEntity.getAttributes().elementAt(i);
            if (theAttribute == keyAttribute) {
                out.write("\t\t\t\t<td><a href=\"../../" + theEntity.getSchema().getUnqualifiedLowerLabel() + "/" + theEntity.getUnqualifiedLowerLabel() + "/edit" + theEntity.getUnqualifiedLabel() + ".jsp?");
                for (int j = 0; j < theEntity.getPrimaryKeyAttributes().size(); j++) {
                    Attribute currentAttribute = theEntity.getPrimaryKeyAttributes().elementAt(j);
                    if (j > 0)
                        out.write("&");
                    out.write(currentAttribute.getLabel() + "=<" + tagLibraryPrefix + ":" + theEntity.getUnqualifiedLowerLabel() + currentAttribute.getUpperLabel() + " />");
                }
                out.write("\"><" + tagLibraryPrefix + ":" + theEntity.getUnqualifiedLowerLabel() + theAttribute.getUpperLabel() + " /></a></td>\n");
            } else {
                out.write("\t\t\t\t<td>");
                generateAttributeTag(true, out, theEntity, theAttribute);
                out.write("</td>\n");
               //out.write("\t\t\t\t<td><" + tagLibrayPrefix + ":" + theEntity.getLowerLabel() + theAttribute.getUpperLabel() + " /></td>\n");
            }
        	
        	if (theAttribute.isDomain()) {
        	    generateDisplayJSP(theEntity, theAttribute);
        	} else if (theAttribute.isImage())
                generateImageJSP(theEntity, theAttribute);
        	
            //Attribute theAttribute = theEntity.getAttributes().elementAt(i);
            //out.write("\t\t\t\t<td>");
            //generateAttributeTag(out, theEntity, theAttribute);
            //out.write("</td>\n");
        }
        out.write("\t\t\t</tr>\n");
        out.write("\t\t</table>\n");

        for (int i = 0; i < theEntity.getChildren().size(); i++) {
            Entity childEntity = theEntity.getChildren().elementAt(i).getTargetEntity();
            generateEntityListBlock(out, theSchema, childEntity);
        }
        
        out.write("</" + tagLibraryPrefix + ":" + theEntity.getLowerLabel() + ">\n");

        generateFooterBlock(out, true);

        out.close();
    }
    
    public void generateAttributeTag(Boolean renderForeignLink, BufferedWriter out, Entity theEntity, Attribute theAttribute) throws IOException {
        if (renderForeignLink && theEntity.isForeignReference(theAttribute)) {
            Entity parentEntity = theEntity.getForeignReferenceEntity(theAttribute);
            out.write("<a href=\"../" + parentEntity.getUnqualifiedLowerLabel() + "/" + parentEntity.getUnqualifiedLowerLabel() + ".jsp?");
            for (int j = 0; j < parentEntity.getPrimaryKeyAttributes().size(); j++) {
                Attribute currentAttribute = parentEntity.getPrimaryKeyAttributes().elementAt(j);
                if (j > 0){
                	out.write("&");
                }
                out.write(currentAttribute.getLabel() + "=<" + tagLibraryPrefix + ":" + theEntity.getUnqualifiedLowerLabel() + theAttribute.getUpperLabel() + " />");
            }
            out.write("\">");
        }

        if (theAttribute.isDomain()) {
            out.write("<a href=\"../" + theEntity.getUnqualifiedLowerLabel() + "/display" + theEntity.getUpperLabel()  + theAttribute.getUpperLabel() + ".jsp?");
            for (int j = 0; j < theEntity.getPrimaryKeyAttributes().size(); j++) {
                Attribute currentAttribute = theEntity.getPrimaryKeyAttributes().elementAt(j);
                if (j > 0)
                    out.write("&");
                out.write(currentAttribute.getLabel() + "=" );
                out.write("<" + tagLibraryPrefix + ":" + theEntity.getUnqualifiedLowerLabel() + "" + currentAttribute.getUpperLabel() + " />");            
            }
            out.write("\">");
            out.write("<" + tagLibraryPrefix + ":" + theEntity.getUnqualifiedLowerLabel() + ""
                    + theAttribute.getUpperLabel() + "Name />");        	
            out.write("</a>");
        } else if (theAttribute.isImage()){
            out.write("<img src=\"../" + theEntity.getUnqualifiedLowerLabel() + "/display" + theEntity.getUpperLabel()  + theAttribute.getUpperLabel() + ".jsp?&size=120");
            for (int j = 0; j < theEntity.getPrimaryKeyAttributes().size(); j++) {
                Attribute currentAttribute = theEntity.getPrimaryKeyAttributes().elementAt(j);
                out.write("&" + currentAttribute.getLabel() + "=<" + tagLibraryPrefix + ":"
                        + theEntity.getUnqualifiedLowerLabel() + currentAttribute.getUpperLabel() + " />");
            }
            out.write("\">");
        } else {
            out.write("<" + tagLibraryPrefix + ":" + theEntity.getUnqualifiedLowerLabel() + ""
                    + theAttribute.getUpperLabel() + " />");        	
        }

        if (renderForeignLink && theEntity.isForeignReference(theAttribute)) {
            out.write("</a>");
        }
    }
    
    public void generateDisplayJSP(Entity theEntity, Attribute theAttribute) throws IOException {
        File theIndexJSP = new File(webAppPath + theEntity.getSchema().getLowerLabel() + "/" + theEntity.getUnqualifiedLowerLabel() + "/display" + theEntity.getUpperLabel() + theAttribute.getUpperLabel() + ".jsp");
        FileWriter fstream = new FileWriter(theIndexJSP);
        BufferedWriter out = new BufferedWriter(fstream);

        generateHeaderPrefix(out);
        
        out.write("<" + tagLibraryPrefix + ":" + theEntity.getUnqualifiedLowerLabel() + theAttribute.getUpperLabel() + " ");            
        for (int j = 0; j < theEntity.getPrimaryKeyAttributes().size(); j++) {
            Attribute currentAttribute = theEntity.getPrimaryKeyAttributes().elementAt(j);
            out.write(" " + currentAttribute.getLabel() + "=\"${param." + currentAttribute.getLabel() + "}\"");
        }
        out.write(" />\n");

        out.close();
    }
    
    public void generateImageJSP(Entity theEntity, Attribute theAttribute) throws IOException {
        File theIndexJSP = new File(webAppPath + theEntity.getSchema().getLowerLabel() + "/" + theEntity.getUnqualifiedLowerLabel() + "/display" + theEntity.getUpperLabel() + theAttribute.getUpperLabel() + ".jsp");
        FileWriter fstream = new FileWriter(theIndexJSP);
        BufferedWriter out = new BufferedWriter(fstream);
        
        generateHeaderPrefix(out);
        
        out.write("<" + tagLibraryPrefix + ":" + theEntity.getUnqualifiedLowerLabel() + theAttribute.getUpperLabel() + " ");            
        for (int j = 0; j < theEntity.getPrimaryKeyAttributes().size(); j++) {
            Attribute currentAttribute = theEntity.getPrimaryKeyAttributes().elementAt(j);
            out.write(" " + currentAttribute.getLabel() + "=\"${param." + currentAttribute.getLabel() + "}\"");
        }
        out.write(" />\n");

        out.close();
    }
    
    public void generateEntityListBlock(BufferedWriter out, Schema theSchema, Entity theEntity) throws IOException {
        if ((theEntity.getPrimaryKeyAttributes() == null || theEntity.getPrimaryKeyAttributes().size() == 0) && (theEntity.getSubKeyAttributes() == null || theEntity.getSubKeyAttributes().size() == 0))
            return;

        Attribute keyAttribute = (theEntity.getSubKeyAttributes().size() == 0 ? theEntity.getPrimaryKeyAttributes().firstElement() : theEntity.getSubKeyAttributes().firstElement());

        int tabs = 6;
        out.write("\n");
        out.write(tabs(tabs)+"<h2>" + theEntity.getUnqualifiedLabel() + " List</h2>\n");

        out.write(tabs(tabs)+"<table class=\"table table-bordered table-striped table-hover table-datatable\">\n");
        tabs++;
        out.write(tabs(tabs)+"<thead>\n");
        tabs++;
        out.write(tabs(tabs)+"<tr>\n");
        tabs++;
        for (int i = 0; i < theEntity.getAttributes().size(); i++) {
            Attribute theAttribute = theEntity.getAttributes().elementAt(i);
            out.write(tabs(tabs)+"<th>" + theAttribute.getUpperLabel() + "</th>\n");
        }
        out.write(tabs(tabs)+"<th></th>\n");
        tabs--;
        out.write(tabs(tabs)+"</tr>\n");
        tabs--;
        out.write(tabs(tabs)+"</thead>\n");
        
        
        out.write(tabs(tabs)+"<tbody>\n");
        tabs++;
        out.write(tabs(tabs)+"<" + tagLibraryPrefix + ":foreach" + theEntity.getUnqualifiedLabel() + " var=\"" + keyAttribute.getLowerLabel() + "Iter\">\n");
        tabs++;
        out.write(tabs(tabs)+"<" + tagLibraryPrefix + ":" + theEntity.getUnqualifiedLowerLabel() + ">\n");
        tabs++;
        out.write(tabs(tabs)+"<tr>\n");
        tabs++;
        String editUrl = "";
        String deleteUrl = "";
        for (int i = 0; i < theEntity.getAttributes().size(); i++) {
            Attribute theAttribute = theEntity.getAttributes().elementAt(i);
            if (theAttribute == keyAttribute) {
            	editUrl = "edit.jsp?";
            	deleteUrl = "delete.jsp?";
                out.write(tabs(tabs)+"<td><a href=\"../" + theEntity.getUnqualifiedLowerLabel() + "/show.jsp?");
                for (int j = 0; j < theEntity.getPrimaryKeyAttributes().size(); j++) {
                    Attribute currentAttribute = theEntity.getPrimaryKeyAttributes().elementAt(j);
                    if (j > 0){
                    	out.write("&");
                    	editUrl += "&";
                    	deleteUrl += "&";
                    }
                    out.write(currentAttribute.getLabel() + "=<" + tagLibraryPrefix + ":" + theEntity.getUnqualifiedLowerLabel() + currentAttribute.getUpperLabel() + " />");
                    editUrl += currentAttribute.getLabel() + "=<" + tagLibraryPrefix + ":" + theEntity.getUnqualifiedLowerLabel() + currentAttribute.getUpperLabel() + " />";
                    deleteUrl += currentAttribute.getLabel() + "=<" + tagLibraryPrefix + ":" + theEntity.getUnqualifiedLowerLabel() + currentAttribute.getUpperLabel() + " />";
                    
                }
                out.write("\"><" + tagLibraryPrefix + ":" + theEntity.getUnqualifiedLowerLabel() + theAttribute.getUpperLabel() + " /></a></td>\n");
                
            } else {
                out.write(tabs(tabs)+"<td>");
                generateAttributeTag(true, out, theEntity, theAttribute);
                out.write("</td>\n");
            }
        }
        out.write(tabs(tabs)+"<td><a href=\""+editUrl+"\">edit</a> <a href=\""+ deleteUrl +"\">delete</a></td>\n");
        tabs--;
        out.write(tabs(tabs)+"</tr>\n");
        tabs--;
        out.write(tabs(tabs)+"</" + tagLibraryPrefix + ":" + theEntity.getUnqualifiedLowerLabel() + ">\n");
        tabs--;
        out.write(tabs(tabs)+"</" + tagLibraryPrefix + ":foreach" + theEntity.getUnqualifiedLabel() + ">\n");
        tabs--;
        out.write(tabs(tabs)+"</tbody>\n");
        
        
        out.write(tabs(tabs)+"<tfoot>\n");
        out.write(tabs(tabs)+"</tfoot>\n");
        
        tabs--;
        out.write(tabs(tabs)+"</table>\n\n");
        out.write(tabs(tabs)+"<br/>\n\n");

        //create add link
        out.write(tabs(tabs)+"<a class=\"btn\" href=\"add.jsp\">add</a>\n");
        
        out.write(tabs(tabs)+"<br/><br/>\n\n");
        
        //create list
        out.write("\t\t<" + tagLibraryPrefix + ":foreach" + theEntity.getUnqualifiedLabel() + " var=\"" + keyAttribute.getLowerLabel() + "Iter\">\n");
  
        out.write("\t\t\t<" + tagLibraryPrefix + ":" + theEntity.getUnqualifiedLowerLabel() + ">\n");
        for (int i = 0; i < theEntity.getAttributes().size(); i++) {
            Attribute theAttribute = theEntity.getAttributes().elementAt(i);
            if (theAttribute == keyAttribute) {
                out.write("\t\t\t\t\t\t<a href=\"../../" + theEntity.getSchema().getUnqualifiedLowerLabel() + "/" + theEntity.getUnqualifiedLowerLabel() + "/" + theEntity.getUnqualifiedLowerLabel() + ".jsp?");
                for (int j = 0; j < theEntity.getPrimaryKeyAttributes().size(); j++) {
                    Attribute currentAttribute = theEntity.getPrimaryKeyAttributes().elementAt(j);
                    if (j > 0)
                        out.write("&");
                    out.write(currentAttribute.getLabel() + "=<" + tagLibraryPrefix + ":" + theEntity.getUnqualifiedLowerLabel() + currentAttribute.getUpperLabel() + " />");
                }
                out.write("\"><" + tagLibraryPrefix + ":" + theEntity.getUnqualifiedLowerLabel() + theAttribute.getUpperLabel() + " /></a>\n");
            } else {
                out.write("\t\t");
                generateAttributeTag(true, out, theEntity, theAttribute);
                out.write("\n");
               //out.write("\t\t\t\t<td><" + tagLibrayPrefix + ":" + theEntity.getLowerLabel() + theAttribute.getUpperLabel() + " /></td>\n");
            }
        }
        out.write("\t\t\t<c:if test=\"${"+ keyAttribute.getLowerLabel() + "Iter != "+ keyAttribute.getLowerLabel() + "IterTotal}\" >, </c:if>" );
        
        out.write("\t\t\t\t\t</" + tagLibraryPrefix + ":" + theEntity.getUnqualifiedLowerLabel() + ">\n");
        out.write("\t\t\t</" + tagLibraryPrefix + ":foreach" + theEntity.getUnqualifiedLabel() + ">\n");
        
        
    }

    public void generateAddEntityJSP(Schema theSchema, Entity theEntity) throws IOException {
        File theIndexJSP = new File(entityPathPrefix(theSchema) + theEntity.getLowerLabel() + "/" + "add" + theEntity.getLabel() + ".jsp");
        FileWriter fstream = new FileWriter(theIndexJSP);
        BufferedWriter out = new BufferedWriter(fstream);

        generateHeaderBlock(out, true, theEntity.hasInt() || theEntity.hasDateTime(), true);
        out.write("<h2>Add " + theEntity.getUnqualifiedLabel() + ":</h2>\n");

        out.write("\n<c:choose>\n");
        out.write("\t<c:when test=\"${empty param.submit}\">\n");
        if (theEntity.hasDomainAttribute() || theEntity.hasImage()) {
            out.write("\t\t<form action=\"upload" + theEntity.getLabel() + ".jsp\" method=\"post\" enctype=\"multipart/form-data\">\n");
        } else {
            out.write("\t\t<form action=\"add" + theEntity.getLabel() + ".jsp\" method=\"post\" >\n");
        }
        for (int i = 0; i < theEntity.getAttributes().size(); i++) {
            Attribute theAttribute = theEntity.getAttributes().elementAt(i);
            if (theAttribute.isPrimary() && theAttribute.isInt()) {
                out.write("\t\t<input type=\"hidden\" name=\"" + theAttribute.getLabel() + "\" value=\"${param." + theAttribute.getLabel() + "}\">\n");
            }
        }
        out.write("\t\t<table>\n");
        out.write("\t\t\t<tr>\n");
        out.write("\t\t\t\t<td>\n");
        out.write("\t\t\t\t<table border=1 align=left>\n");
        for (int i = 0; i < theEntity.getAttributes().size(); i++) {
            Attribute theAttribute = theEntity.getAttributes().elementAt(i);
            if (theAttribute.isPrimary() && theAttribute.isInt())
                continue;
            else if (theAttribute.isDomain()) {
                out.write("\t\t\t\t\t<tr>\n");
                out.write("\t\t\t\t\t\t<th align=left>" + theAttribute.getUpperLabel() + "</th>\n");
                out.write("\t\t\t\t\t\t<td><input type=\"file\" name=\"" + theAttribute.getLabel() + "\" size=\"40\"></td>\n");
                out.write("\t\t\t\t\t</tr>\n");
            } else if (theAttribute.isImage()){
                out.write("\t\t\t\t\t<tr>\n");
                out.write("\t\t\t\t\t\t<th align=left>" + theAttribute.getUpperLabel() + "</th>\n");
                out.write("\t\t\t\t\t\t<td><input type=\"file\" name=\"" + theAttribute.getLabel() + "\" size=\"40\"></td>\n");
                out.write("\t\t\t\t\t</tr>\n");
            } else {
                out.write("\t\t\t\t\t<tr>\n");
                out.write("\t\t\t\t\t\t<th align=left>" + theAttribute.getUpperLabel() + "</th>\n");
                out.write("\t\t\t\t\t\t<td><input type=\"text\" name=\"" + theAttribute.getLabel() + "\" size=\"40\" value=\"\"></td>\n");
                out.write("\t\t\t\t\t</tr>\n");
            }
        }
        out.write("\t\t\t\t</table>\n");
        out.write("\t\t\t\t</td>\n");
        out.write("\t\t\t</tr>\n");
        out.write("\t\t\t<tr>\n");
        out.write("\t\t\t\t<td><input type=\"submit\" name=\"submit\" value=\"Save\"> <input type=\"submit\" name=\"submit\" value=\"Cancel\"></td>\n");
        out.write("\t\t\t</tr>\n");
        out.write("\t\t</table>\n");
        out.write("\t\t</form>\n");
        out.write("\t</c:when>\n");
        out.write("\t<c:when test=\"${param.submit == 'Cancel'}\">\n");
        out.write("\t\t<c:redirect url=\"../../index.jsp\" />\n");
        out.write("\t</c:when>\n");
        out.write("\t<c:when test=\"${param.submit == 'Save'}\">\n");
        if (theEntity.hasDomainAttribute() || theEntity.hasImage()) {
            out.write(generateIndent(2) + "<" + tagLibraryPrefix + ":upload" + theEntity.getUpperLabel() + "> ");
            out.write("</" + tagLibraryPrefix + ":upload" + theEntity.getUpperLabel() + ">\n");
        } else {
            for (int i = 0; i < theEntity.getAttributes().size(); i++) {
                Attribute theAttribute = theEntity.getAttributes().elementAt(i);
                if (theAttribute.isInt()) {
                    out.write("\t\t<fmt:parseNumber var=\"" + theAttribute.getLabel() + "\" value=\"${param." + theAttribute.getLabel() + "}\" />\n");
                } else if (theAttribute.isDateTime()) {
                    //out.write("\t\t<%-- We have a bean info instance and a property editor defined, but not yet successfully bound, hence... --%>\n");
                    out.write("\t\t<fmt:parseDate var=\"" + theAttribute.getLabel() + "\" value=\"${param." + theAttribute.getLabel() + "}\" pattern=\"yyyy-MM-dd\" />\n");
                }
            }
            
            Vector<Entity> ancestors = theEntity.getAncestors();
            for (int i = 0; i < ancestors.size(); i++) {
                log.debug("entity: " + theEntity + "\tancestor: " + ancestors.elementAt(i) + "\tsubkeys: " + ancestors.elementAt(i).getSubKeyAttributes() + "\tparent keys: " + ancestors.elementAt(i).primaryKeyAttributes);
                Attribute ancestorKey = null;
                if (ancestors.elementAt(i).getSubKeyAttributes().size() > 0)
                    ancestorKey = ancestors.elementAt(i).getSubKeyAttributes().firstElement();
                else
                    ancestorKey = ancestors.elementAt(i).getPrimaryKeyAttributes().firstElement();
                out.write(generateIndent(i+2) + "<" + tagLibraryPrefix + ":" + ancestors.elementAt(i).getLowerLabel() + " " + ancestorKey.getLabel() + "=\"${" + (ancestorKey.isInt() || ancestorKey.isDateTime() ? "" : "param.") + ancestorKey.getLabel() + "}\" >\n");
            }
            out.write(generateIndent(ancestors.size()+2) + "<" + tagLibraryPrefix + ":" + theEntity.getLowerLabel() + ">\n");
            for (int i = 0; i < theEntity.getAttributes().size(); i++) {
                Attribute theAttribute = theEntity.getAttributes().elementAt(i);
                if (theAttribute.isPrimary()) {
                	out.write(generateIndent(ancestors.size()+3) + "<c:set var=\"" + theAttribute.getLabel() + "\" ><" + tagLibraryPrefix + ":" + theEntity.getLowerLabel() + theAttribute.getUpperLabel() + "/></c:set>\n");
                	if (theAttribute.isInt())
                		continue;
                }
                out.write(generateIndent(ancestors.size()+3) + "<" + tagLibraryPrefix + ":" + theEntity.getLowerLabel() + "" + theAttribute.getUpperLabel() + " " + theAttribute.getLabel() + " = \"${" + (theAttribute.isDateTime() ? "" : "param.") + theAttribute.getLabel() + "}\" />\n");
            }
            out.write(generateIndent(ancestors.size()+2) + "</" + tagLibraryPrefix + ":" + theEntity.getLowerLabel() + ">\n");
            for (int i = ancestors.size() - 1; i >= 0; i--) {
                out.write(generateIndent(i+2) + "</" + tagLibraryPrefix + ":" + ancestors.elementAt(i).getLowerLabel() + ">\n");
            }
        }

        out.write("\t\t<c:redirect url=\"" + theEntity.getLowerLabel() + ".jsp\" >\n");
        for (int i = 0; i < theEntity.getAttributes().size(); i++) {
            Attribute theAttribute = theEntity.getAttributes().elementAt(i);
            if (theAttribute.isPrimary()) {
                out.write("\t\t\t<c:param name=\"" + theAttribute.getLabel() + "\" value=\"${" + theAttribute.getLabel() + "}\"/>\n");
            }
        }
        out.write("\t\t</c:redirect>\n");
         out.write("\t</c:when>\n");
        out.write("\t<c:otherwise>\n");
        out.write("\t\tA task is required for this function.\n");
        out.write("\t</c:otherwise>\n");
        out.write("</c:choose>\n");

        generateFooterBlock(out, true);

        out.close();
    }
    
    
    public void generateUploadEntityJSP(Schema theSchema, Entity theEntity) throws IOException {
        File theIndexJSP = new File(entityPathPrefix(theSchema) + theEntity.getLowerLabel() + "/" + "upload" + theEntity.getLabel() + ".jsp");
        FileWriter fstream = new FileWriter(theIndexJSP);
        BufferedWriter out = new BufferedWriter(fstream);

        generateHeaderPrefix(out);

        out.write("<" + tagLibraryPrefix + ":upload" + theEntity.getUpperLabel() + "> ");
        out.write("</" + tagLibraryPrefix + ":upload" + theEntity.getUpperLabel() + ">\n");

        out.write("<c:redirect url=\"" + theEntity.getUnqualifiedLowerLabel() + "List.jsp \"/>\n");

        out.close();
    }
    
    public void generateAddEditEntityJSP(Schema theSchema, Entity theEntity) throws IOException {
    	generateAddEditEntityJSP(theSchema, theEntity, true);
    }
    
    public void generateAddEditEntityJSP(Schema theSchema, Entity theEntity, Boolean isEdit) throws IOException {
        File theIndexJSP = new File(entityPathPrefix(theSchema) + theEntity.getLowerLabel() + "/"+ ( isEdit ? "edit" : "add" ) +".jsp");
        FileWriter fstream = new FileWriter(theIndexJSP);
        BufferedWriter out = new BufferedWriter(fstream);

        generateHeaderPrefix(out);
        
        int tabs = 1;
        
        out.write("\n<c:choose>\n");
        out.write(tabs(tabs) + "<c:when test=\"${empty param.submit}\">\n");
        
        tabs++;
        
        generateHeaderBlock(out, true, (theEntity.hasInt() || theEntity.hasDateTime()), true, tabs*4);
        
        tabs = 8;
        if(isEdit){
        	for (int i = 0; i < theEntity.getAttributes().size(); i++) {
        		Attribute theAttribute = theEntity.getAttributes().elementAt(i);
        		if (theAttribute.isPrimary() && theAttribute.isInt()){
        			out.write(tabs(tabs) + "<fmt:parseNumber var=\"" + theAttribute.getLabel() + "\" value=\"${param." + theAttribute.getLabel() + "}\" />\n");
        		}
        		if (theAttribute.isPrimary() && theAttribute.isDateTime()){
        			out.write(tabs(tabs) + "<fmt:parseDate var=\"" + theAttribute.getLabel() + "\" value=\"${param." + theAttribute.getLabel() + "}\" pattern=\"yyyy-MM-dd HH:mm:ss.S\" />\n");
        		}
        	}
        	out.write(tabs(tabs) + "<" + tagLibraryPrefix + ":" + theEntity.getLowerLabel());
        	for (int i = 0; i < theEntity.getAttributes().size(); i++) {
        		Attribute theAttribute = theEntity.getAttributes().elementAt(i);
        		if (theAttribute.isPrimary()){
        			out.write(" " + theAttribute.getLabel() + "=\"${" + (theAttribute.isInt() || theAttribute.isDateTime() ? "" : "param.") + theAttribute.getLabel() + "}\"");
        		}
        	}
        	out.write(">\n");
        	tabs++;
        }
        
        out.write(tabs(tabs) + "<form action=\""+ ( isEdit ? "edit.jsp" : "add.jsp" ) +"\" method=\"post\" >\n");
        tabs++;
        out.write(tabs(tabs) + "<fieldset>\n");
        tabs++;
        out.write(tabs(tabs) + "<legend>"+ theEntity.getUnqualifiedLabel() + "</legend>\n");
        
        
        for (int i = 0; i < theEntity.getAttributes().size(); i++) {
            Attribute theAttribute = theEntity.getAttributes().elementAt(i);
            if (theAttribute.isPrimary() && theAttribute.isInt()){
            	continue;
            } else {
                out.write(tabs(tabs) + "<label for=\""+theAttribute.getLabel()+"\">" + theAttribute.getUpperLabel() + "</label>\n");
                out.write(tabs(tabs) + "<input type=\"text\" id=\""+theAttribute.getLabel()+"\" name=\"" + theAttribute.getLabel() + "\" size=\"40\" value=\"");
                if(isEdit){
                	generateAttributeTag(false, out, theEntity, theAttribute);
                }
                out.write("\">\n\n");
            }
        }
        out.write(tabs(tabs) + "<input type=\"submit\" name=\"submit\" value=\"Save\">\n");
        out.write(tabs(tabs) + "<input type=\"submit\" name=\"submit\" value=\"Cancel\">\n");
        if(isEdit){
	        for (int i = 0; i < theEntity.getAttributes().size(); i++) {
	            Attribute theAttribute = theEntity.getAttributes().elementAt(i);
	            if (theAttribute.isPrimary() && theAttribute.isInt()) {
	                out.write(tabs(tabs) + "<input type=\"hidden\" name=\"" + theAttribute.getLabel() + "\" value=\"${param." + theAttribute.getLabel() + "}\">\n");
	            }
	        }
        }
        tabs--;
        out.write(tabs(tabs) + "</fieldset>\n");
        tabs--;
        out.write(tabs(tabs) + "</form>\n");
        tabs--;
        if(isEdit){
        	out.write(tabs(tabs) + "</" + tagLibraryPrefix + ":" + theEntity.getLowerLabel() + ">\n");
        }
        
        tabs = 2;
        generateFooterBlock(out, true,tabs*4);
        
        out.write("\n");
        
        out.write("\t</c:when>\n");
        out.write("\t<c:when test=\"${param.submit eq 'Cancel'}\">\n");
        out.write("\t\t<c:redirect url=\"/" + theEntity.getLowerLabel() + "/list.jsp\" />\n");
        out.write("\t</c:when>\n");
        out.write("\t<c:when test=\"${param.submit eq 'Save'}\">\n");
        
        if(isEdit){
	        for (int i = 0; i < theEntity.getAttributes().size(); i++) {
	            Attribute theAttribute = theEntity.getAttributes().elementAt(i);
	            if (theAttribute.isInt()) {
	                out.write("\t\t<fmt:parseNumber var=\"" + theAttribute.getLabel() + "\" value=\"${param." + theAttribute.getLabel() + "}\" />\n");
	            } else if (theAttribute.isDateTime()) {
	                out.write("\t\t<fmt:parseDate var=\"" + theAttribute.getLabel() + "\" value=\"${param." + theAttribute.getLabel() + "}\" pattern=\"yyyy-MM-dd\" />\n");
	            }
	        }
        }
        
//        Vector<Entity> ancestors = theEntity.getAncestors();
//        for (int i = 0; i < ancestors.size(); i++) {
//            log.debug("entity: " + theEntity + "\tancestor: " + ancestors.elementAt(i) + "\tsubkeys: " + ancestors.elementAt(i).getSubKeyAttributes() + "\tparent keys: " + ancestors.elementAt(i).primaryKeyAttributes);
//            Attribute ancestorKey = null;
//            if (ancestors.elementAt(i).getSubKeyAttributes().size() > 0){
//            	ancestorKey = ancestors.elementAt(i).getSubKeyAttributes().firstElement();
//            } else {
//            	ancestorKey = ancestors.elementAt(i).getPrimaryKeyAttributes().firstElement();
//            }
//            out.write(generateIndent(i+2) + "<" + tagLibrayPrefix + ":" + ancestors.elementAt(i).getLowerLabel() + " " + ancestorKey.getLabel() + "=\"${" + (ancestorKey.isInt() || ancestorKey.isDateTime() ? "" : "param.") + ancestorKey.getLabel() + "}\" >\n");
//        }
        out.write(tabs(2) + "<" + tagLibraryPrefix + ":" + theEntity.getLowerLabel());
        if(isEdit){
        	for (int i = 0; i < theEntity.getAttributes().size(); i++) {
        		Attribute theAttribute = theEntity.getAttributes().elementAt(i);
        		if (theAttribute.isPrimary()){
        			out.write(" " + theAttribute.getLabel() + "=\"${" + (theAttribute.isInt() || theAttribute.isDateTime() ? "" : "param.") + theAttribute.getLabel() + "}\"");
        		}
        	}
        }
        out.write(">\n");
        
        for (int i = 0; i < theEntity.getAttributes().size(); i++) {
            Attribute theAttribute = theEntity.getAttributes().elementAt(i);
            if ((theAttribute.isPrimary() && theAttribute.isInt()) || theAttribute.isDomain() || theAttribute.isImage()){
            	continue;
            }
            out.write(tabs(3) + "<" + tagLibraryPrefix + ":" + theEntity.getLowerLabel() + "" + theAttribute.getUpperLabel() + " " + theAttribute.getLabel() + " = \"${" + (theAttribute.isDateTime() ? "" : "param.") + theAttribute.getLabel() + "}\" />\n");
        }
        out.write(tabs(2) + "</" + tagLibraryPrefix + ":" + theEntity.getLowerLabel() + ">\n");
//        for (int i = ancestors.size() - 1; i >= 0; i--) {
//            out.write(generateIndent(i+2) + "</" + tagLibrayPrefix + ":" + ancestors.elementAt(i).getLowerLabel() + ">\n");
//        }

        out.write("\t\t<c:redirect url=\"/" + theEntity.getLowerLabel() + "/list.jsp\" />\n");
//        for (int i = 0; i < theEntity.getAttributes().size(); i++) {
//            Attribute theAttribute = theEntity.getAttributes().elementAt(i);
//            if (theAttribute.isPrimary() && theAttribute.isInt()) {
//                out.write("\t\t\t<c:param name=\"" + theAttribute.getLabel() + "\" value=\"${" + theAttribute.getLabel() + "}\"/>\n");
//            }
//        }
        //out.write("\t\t</c:redirect>\n");
        out.write("\t</c:when>\n");
        out.write("\t<c:otherwise>\n");
        out.write("\t\tA task is required for this function.\n");
        out.write("\t</c:otherwise>\n");
        out.write("</c:choose>");
        out.close();
    }
    
    public String generateIndent(int length) {
        StringBuffer theIndent = new StringBuffer();
        for (int i = 0; i < length; i++)
            theIndent.append("\t");
        return theIndent.toString();
    }

    public void generateHead(Database theDatabase) throws IOException {
        File theHeaderJSP  = new File(webAppPath + "head.jsp");
        FileWriter fstream = new FileWriter(theHeaderJSP);
        BufferedWriter out = new BufferedWriter(fstream);
        int spaces = 4;
        out.write(spaces(spaces) + "<head>\n");
        spaces += 4;
        out.write(spaces(spaces) + "<title>" + webAppName + "</title>\n");
        out.write(spaces(spaces) + "<meta charset=\"utf-8\">\n");
        out.write(spaces(spaces) + "<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n");
        out.write(spaces(spaces) + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n");
        out.write(spaces(spaces) + "<script	src=\"https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js\"></script>\n");
        out.write(spaces(spaces) + "<link rel=\"stylesheet\" href=\"https://stackpath.bootstrapcdn.com/bootstrap/4.5.0/css/bootstrap.min.css\" integrity=\"sha384-9aIt2nRpC12Uk9gS9baDl411NQApFmC26EwAOH8WgZl5MYYxFfc+NcPb1dKGj7Sk\" crossorigin=\"anonymous\">\n");
        out.write(spaces(spaces) + "<link rel=\"stylesheet\" id='font-awesome' href=\"https://use.fontawesome.com/releases/v5.7.2/css/all.css\" integrity=\"sha384-fnmOCqbTlWIlj8LyTjo7mOUStjsKC4pOpQbqyi7RrhN7udi9RwhKkMHpvLbHG9Sr\" crossorigin=\"anonymous\">\n");
        out.write(spaces(spaces) + "<script src=\"https://stackpath.bootstrapcdn.com/bootstrap/3.4.1/js/bootstrap.min.js\" integrity=\"sha384-aJ21OjlMXNL5UyIl/XNwTMqvzeRMZH2w8c5cRVpzpU8Y5bApTppSuUkhZXN0VxHd\" crossorigin=\"anonymous\"></script>\n");
        out.write(spaces(spaces) + "<link rel=\"stylesheet\" type=\"text/css\" href=\"<util:applicationRoot/>/resources/DataTables/css/jquery.dataTables.min.css\">\n");
        out.write(spaces(spaces) + "<script type=\"text/javascript\" charset=\"utf8\" src=\"" + webAppName + "/resources/DataTables/js/jquery.dataTables.min.js\"></script>\n");
        out.write(spaces(spaces) + "<style type=\"text/css\" media=\"all\">\n");
        out.write(spaces(spaces) + "@import \"/" + webAppName + "/resources/n3c_login_style.css\";\n");
        out.write(spaces(spaces) + "@import \"/" + webAppName + "/resources/main_style.css\";\n");
        out.write(spaces(spaces) + "</style>\n");
        out.write(spaces(spaces) + "<style type=\"text/css\">\n");
        out.write(spaces(spaces) + "table.dataTable thead .sorting_asc {\n");
        spaces += 4;
        out.write(spaces(spaces) + "background-image: none !important;\n");
        spaces -= 4;
        out.write(spaces(spaces) + "}\n");
        out.write(spaces(spaces) + "</style>\n");
        spaces -= 4;
        out.write(spaces(spaces) + "</head>\n");
        out.close();
    }
    
    public void generateHeader(Database theDatabase) throws IOException {
        File theHeaderJSP  = new File(webAppPath + "header.jsp");
        FileWriter fstream = new FileWriter(theHeaderJSP);
        BufferedWriter out = new BufferedWriter(fstream);
        out.write("<img src=\"/" + webAppName + "/resources/images/logo.png\" height=50px ><br>\n");
        out.write("<h1><a href=\"/" + projectName + "/index.jsp\">" + projectName + " scaffolding</a></h1>\n");
        out.close();
    }
    
    public void generateFooter(Database theDatabase) throws IOException {
        File theFooterJSP = new File(webAppPath + "footer.jsp");
        FileWriter fstream = new FileWriter(theFooterJSP);
        BufferedWriter out = new BufferedWriter(fstream);

        out.write("<br><br>\n");
        out.write("<hr>\n");
        out.write("<p>Supported in part by NIH grants R18 HS017034 and UL1 RR024979</p>\n");

        out.close();
    }
    
    public void generateHeaderBlock(BufferedWriter out, boolean uplink, boolean hasDateTime, boolean menu) throws IOException {
    	generateHeaderBlock(out, uplink, hasDateTime, menu, 0);
    }
    
    public void generateHeaderBlock(BufferedWriter out, boolean uplink, boolean hasDateTime, boolean menu, int spaces) throws IOException {
        out.write(spaces(spaces) + "<html>\n");
        spaces += 4;
        out.write(spaces(spaces) + "<c:import url=\"/head.jsp\" />\n");
        out.write(spaces(spaces) + "<body>\n");
        spaces += 4;
        out.write(spaces(spaces) + "<div class=\"container-fluid\">\n");
        spaces += 4;
        out.write(spaces(spaces) + "<c:import url=\"/header.jsp\" />\n");
        out.write(spaces(spaces) + "<div class=\"row flex-nowrap\">\n");
        spaces += 4;
        out.write(spaces(spaces) + "<div class=\"col-xs-3\">\n");
        spaces += 4;
        out.write(spaces(spaces) + "<c:import url=\"/menu.jsp\" />\n");
        spaces -= 4;
        out.write(spaces(spaces) + "</div>\n");
        out.write(spaces(spaces) + "<div class=\"col-xs-8\">\n");
    }
    
    public String spaces(int count){
    	String out = "";
    	for(int i=0;i<count;i++){
    		out += " ";
    	}
    	return out;
    }
    
    public String tabs(int count){
    	String out = "";
    	for(int i=0;i<count;i++){
    		out += "\t";
    	}
    	return out;
    }
    
    public void generateHeaderPrefix(BufferedWriter out) throws IOException {
        out.write("<%@ include file=\"/_include.jsp\" %>\n");
    }

    public void generateFooterBlock(BufferedWriter out, boolean uplink) throws IOException {
    	generateFooterBlock(out, uplink, 0);
    }
    
    public void generateFooterBlock(BufferedWriter out, boolean uplink, int spaces) throws IOException {
        out.write(spaces(spaces + 16)+"</div>\n");
        out.write(spaces(spaces + 12)+"</div>\n");
        out.write(spaces(spaces + 8)+"</div>\n");
        out.write(spaces(spaces + 8)+"<c:import url=\"/footer.jsp\" />\n");
        out.write(spaces(spaces + 4)+"</body>\n");
        out.write(spaces(spaces)+"</html>");
    }
    
    public void generateDbtest(Database theDatabase) throws IOException {
        File theIndexJSP  = new File(webAppPath + "dbtest.jsp");
        FileWriter fstream = new FileWriter(theIndexJSP);
        BufferedWriter out = new BufferedWriter(fstream);
        generateHeaderPrefix(out);
        out.write("<%@ taglib prefix=\"" + packagePrefix.substring(packagePrefix.lastIndexOf('.')+1) + "\" uri=\"http://icts.uiowa.edu/" + packagePrefix.substring(packagePrefix.lastIndexOf('.')+1) + "\"%>\n");
        out.write("<%@ page  errorPage=\"/error/dberror.jsp\" %>" + "\n" + "<" +  tagLibraryPrefix + ":dbtest/>");
        out.close();
    }
    
    public void copyFolder(Path source, Path target) throws IOException {
    	copyFolder(source, target, StandardCopyOption.REPLACE_EXISTING);
    }
    
	public void copyFolder(Path source, Path target, CopyOption... options) throws IOException {
		Files.walkFileTree(source, new SimpleFileVisitor<Path>() {

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				Files.createDirectories(target.resolve(source.relativize(dir)));
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.copy(file, target.resolve(source.relativize(file)), options);
				return FileVisitResult.CONTINUE;
			}
		});
	}
}