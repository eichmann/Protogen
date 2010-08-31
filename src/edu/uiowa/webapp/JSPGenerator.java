/*
 * Created on May 20, 2008
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.uiowa.webapp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

public class JSPGenerator {

    String webAppPath = null;
    String packagePrefix = null;
    String projectName = null;

    File packagePrefixDirectory = null;
    File tagDirectory = null;

    public JSPGenerator(String webAppPath, String packagePrefix, String projectName) {
        this.webAppPath = webAppPath;
        this.packagePrefix = packagePrefix;
        this.projectName = projectName;
    }

    public void generateJSPs(Database theDatabase) throws IOException {
        generateEntityDirectories(theDatabase);

        generateIndex(theDatabase);
        generateHeader(theDatabase);
        generateFooter(theDatabase);
        generateDbtest(theDatabase);

        Enumeration<Schema> schemaEnum = theDatabase.getSchemas().elements();
        while (schemaEnum.hasMoreElements()) {
            Schema theSchema = schemaEnum.nextElement();
            Enumeration<Entity> entityEnum = theSchema.getEntities().elements();
            while (entityEnum.hasMoreElements()) {
                Entity theEntity = entityEnum.nextElement();
                generateEntityJSP(theSchema, theEntity);
                generateAddEntityJSP(theSchema, theEntity);
                if (theEntity.hasBinaryDomainAttribute()|| theEntity.hasImage())
                {      	
                    generateUploadEntityJSP(theSchema, theEntity);
                }
                generateEditEntityJSP(theSchema, theEntity);
            }
        }
    }

    private void generateEntityDirectories(Database theDatabase) throws IOException {
        Enumeration<Schema> schemaEnum = theDatabase.getSchemas().elements();
        while (schemaEnum.hasMoreElements()) {
            Schema theSchema = schemaEnum.nextElement();
            File schemaDir = new File(webAppPath + theSchema.getLowerLabel());
            if (schemaDir.exists()) {
                if (schemaDir.isFile())
                    throw new IOException("webapp directory " + schemaDir.getAbsolutePath() + " is a normal file");
            } else {
                schemaDir.mkdir();
            }
            Enumeration<Entity> entityEnum = theSchema.getEntities().elements();
            while (entityEnum.hasMoreElements()) {
                Entity theEntity = entityEnum.nextElement();
                File entityDir = new File(webAppPath + theSchema.getLowerLabel() + "/" + theEntity.getUnqualifiedLowerLabel());
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

        generateHeaderBlock(out, false);
        
        out.write("<ul>\n");
        Enumeration<Schema> schemaEnum = theDatabase.getSchemas().elements();
        while (schemaEnum.hasMoreElements()) {
            Schema theSchema = schemaEnum.nextElement();
            out.write("\t<li>" + theSchema.getLabel() + " list\n");
            out.write("\t<ul>\n");
            Enumeration<Entity> entityEnum = theSchema.getEntities().elements();
            while (entityEnum.hasMoreElements()) {
                Entity theEntity = entityEnum.nextElement();
                // skip over subordinate entities
                if (theEntity.getParents().size() > 0)
                    continue;
                out.write("\t\t<li><a href=\"" + theSchema.getLowerLabel() + "/" + theEntity.getUnqualifiedLowerLabel() + "/" + theEntity.getUnqualifiedLowerLabel() + "List.jsp\">" + theEntity.getUnqualifiedLabel() + " list</a></li>\n");
                generateEntityListJSP(theSchema, theEntity);
            }
            out.write("\t</ul></li>\n");
        }
        out.write("</ul>\n");
        generateFooterBlock(out, false);
        
        out.close();
    }
    
    public void generateEntityListJSP(Schema theSchema, Entity theEntity) throws IOException {
        File theIndexJSP = new File(webAppPath + theSchema.getLowerLabel() + "/" + theEntity.getUnqualifiedLowerLabel() + "/" + theEntity.getUnqualifiedLowerLabel() + "List.jsp");
        FileWriter fstream = new FileWriter(theIndexJSP);
        BufferedWriter out = new BufferedWriter(fstream);

        generateHeaderBlock(out, true);
        
        generateEntityListBlock(out, theSchema, theEntity);

        generateFooterBlock(out, true);

        out.close();
    }
    
    public void generateEntityJSP(Schema theSchema, Entity theEntity) throws IOException {
        File theIndexJSP = new File(webAppPath + theSchema.getLowerLabel() + "/" + theEntity.getUnqualifiedLowerLabel() + "/" + theEntity.getUnqualifiedLowerLabel() + ".jsp");
        FileWriter fstream = new FileWriter(theIndexJSP);
        BufferedWriter out = new BufferedWriter(fstream);

        Attribute keyAttribute = (theEntity.getSubKeyAttributes().size() == 0 ? theEntity.getPrimaryKeyAttributes().firstElement() : theEntity.getSubKeyAttributes().firstElement());
        generateHeaderBlock(out, true, theEntity.hasInt() || theEntity.hasDateTime());

        for (int i = 0; i < theEntity.getAttributes().size(); i++) {
            Attribute theAttribute = theEntity.getAttributes().elementAt(i);
            if (theAttribute.isPrimary() && theAttribute.isInt())
                out.write("<fmt:parseNumber var=\"" + theAttribute.getLabel() + "\" value=\"${param." + theAttribute.getLabel() + "}\" />\n");
            if (theAttribute.isPrimary() && theAttribute.isDateTime())
                out.write("<fmt:parseDate var=\"" + theAttribute.getLabel() + "\" value=\"${param." + theAttribute.getLabel() + "}\" pattern=\"yyyy-MM-dd HH:mm:ss.S\" />\n");
        }
        out.write("<" + packagePrefix.substring(packagePrefix.lastIndexOf('.')+1) + ":" + theEntity.getLowerLabel());
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
                out.write(" <" + packagePrefix.substring(packagePrefix.lastIndexOf('.')+1) + ":" + theEntity.getLowerLabel() + "" + theAttribute.getUpperLabel() + " />");
        }
        out.write("</h2>\n");

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
                    out.write(currentAttribute.getLabel() + "=<" + packagePrefix.substring(packagePrefix.lastIndexOf('.')+1) + ":" + theEntity.getUnqualifiedLowerLabel() + currentAttribute.getUpperLabel() + " />");
                }
                out.write("\"><" + packagePrefix.substring(packagePrefix.lastIndexOf('.')+1) + ":" + theEntity.getUnqualifiedLowerLabel() + theAttribute.getUpperLabel() + " /></a></td>\n");
            } else {
                out.write("\t\t\t\t<td>");
                generateAttributeTag(true, out, theEntity, theAttribute);
                out.write("</td>\n");
               //out.write("\t\t\t\t<td><" + packagePrefix.substring(packagePrefix.lastIndexOf('.')+1) + ":" + theEntity.getLowerLabel() + theAttribute.getUpperLabel() + " /></td>\n");
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
        
        out.write("</" + packagePrefix.substring(packagePrefix.lastIndexOf('.')+1) + ":" + theEntity.getLowerLabel() + ">\n");

        generateFooterBlock(out, true);

        out.close();
    }
    
    public void generateAttributeTag(Boolean renderForeignLink, BufferedWriter out, Entity theEntity, Attribute theAttribute) throws IOException {
        if (renderForeignLink && theEntity.isForeignReference(theAttribute)) {
            Entity parentEntity = theEntity.getForeignReferenceEntity(theAttribute);
            out.write("<a href=\"../" + parentEntity.getUnqualifiedLowerLabel() + "/" + parentEntity.getUnqualifiedLowerLabel() + ".jsp?");
            for (int j = 0; j < parentEntity.getPrimaryKeyAttributes().size(); j++) {
                Attribute currentAttribute = parentEntity.getPrimaryKeyAttributes().elementAt(j);
                if (j > 0)
                    out.write("&");
                out.write(currentAttribute.getLabel() + "=<" + packagePrefix.substring(packagePrefix.lastIndexOf('.') + 1) + ":"
                        + theEntity.getUnqualifiedLowerLabel() + theAttribute.getUpperLabel() + " />");
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
                out.write("<" + packagePrefix.substring(packagePrefix.lastIndexOf('.') + 1) + ":" + theEntity.getUnqualifiedLowerLabel() + ""
                        + currentAttribute.getUpperLabel() + " />");            
            }
            out.write("\">");
            out.write("<" + packagePrefix.substring(packagePrefix.lastIndexOf('.') + 1) + ":" + theEntity.getUnqualifiedLowerLabel() + ""
                    + theAttribute.getUpperLabel() + "Name />");        	
            out.write("</a>");
        } else if (theAttribute.isImage()){
            out.write("<img src=\"../" + theEntity.getUnqualifiedLowerLabel() + "/display" + theEntity.getUpperLabel()  + theAttribute.getUpperLabel() + ".jsp?&size=120");
            for (int j = 0; j < theEntity.getPrimaryKeyAttributes().size(); j++) {
                Attribute currentAttribute = theEntity.getPrimaryKeyAttributes().elementAt(j);
                out.write("&" + currentAttribute.getLabel() + "=<" + packagePrefix.substring(packagePrefix.lastIndexOf('.') + 1) + ":"
                        + theEntity.getUnqualifiedLowerLabel() + currentAttribute.getUpperLabel() + " />");
            }
            out.write("\">");
        } else {
            out.write("<" + packagePrefix.substring(packagePrefix.lastIndexOf('.') + 1) + ":" + theEntity.getUnqualifiedLowerLabel() + ""
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
        
        out.write("<%@ taglib prefix=\"" + packagePrefix.substring(packagePrefix.lastIndexOf('.')+1) + "\" uri=\"http://icts.uiowa.edu/" + packagePrefix.substring(packagePrefix.lastIndexOf('.')+1) + "\"%>\n");
        out.write("<" + packagePrefix.substring(packagePrefix.lastIndexOf('.') + 1) + ":" + theEntity.getUnqualifiedLowerLabel() + theAttribute.getUpperLabel() + " ");            
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
        
        out.write("<%@ taglib prefix=\"" + packagePrefix.substring(packagePrefix.lastIndexOf('.')+1) + "\" uri=\"http://icts.uiowa.edu/" + packagePrefix.substring(packagePrefix.lastIndexOf('.')+1) + "\"%>\n");
        out.write("<" + packagePrefix.substring(packagePrefix.lastIndexOf('.') + 1) + ":" + theEntity.getUnqualifiedLowerLabel() + theAttribute.getUpperLabel() + " ");            
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

        out.write("\n\t\t<h2>" + theEntity.getUnqualifiedLabel() + " List</h2>\n");

        out.write("\t\t<table border=1>\n");
        out.write("\t\t\t<tr>\n");
        for (int i = 0; i < theEntity.getAttributes().size(); i++) {
            Attribute theAttribute = theEntity.getAttributes().elementAt(i);
            out.write("\t\t\t\t<th>" + theAttribute.getUpperLabel() + "</th>\n");
        }
        out.write("\t\t\t</tr>\n");
        out.write("\t\t\t<" + packagePrefix.substring(packagePrefix.lastIndexOf('.')+1) + ":foreach" + theEntity.getUnqualifiedLabel() + " var=\"" + keyAttribute.getLowerLabel() + "Iter\">\n");
        out.write("\t\t\t\t<tr>\n");
        out.write("\t\t\t\t\t<" + packagePrefix.substring(packagePrefix.lastIndexOf('.')+1) + ":" + theEntity.getUnqualifiedLowerLabel() + " " + (keyAttribute.getLabel().equals("ID") ? keyAttribute.getLabel() : keyAttribute.getLowerLabel()) + "=\"${" + keyAttribute.getLowerLabel() + "Iter}\">\n");
        for (int i = 0; i < theEntity.getAttributes().size(); i++) {
            Attribute theAttribute = theEntity.getAttributes().elementAt(i);
            if (theAttribute == keyAttribute) {
                out.write("\t\t\t\t\t\t<td><a href=\"../../" + theEntity.getSchema().getUnqualifiedLowerLabel() + "/" + theEntity.getUnqualifiedLowerLabel() + "/" + theEntity.getUnqualifiedLowerLabel() + ".jsp?");
                for (int j = 0; j < theEntity.getPrimaryKeyAttributes().size(); j++) {
                    Attribute currentAttribute = theEntity.getPrimaryKeyAttributes().elementAt(j);
                    if (j > 0)
                        out.write("&");
                    out.write(currentAttribute.getLabel() + "=<" + packagePrefix.substring(packagePrefix.lastIndexOf('.')+1) + ":" + theEntity.getUnqualifiedLowerLabel() + currentAttribute.getUpperLabel() + " />");
                }
                out.write("\"><" + packagePrefix.substring(packagePrefix.lastIndexOf('.')+1) + ":" + theEntity.getUnqualifiedLowerLabel() + theAttribute.getUpperLabel() + " /></a></td>\n");
            } else {
                out.write("\t\t\t\t\t\t<td>");
                generateAttributeTag(true, out, theEntity, theAttribute);
                out.write("</td>\n");
               //out.write("\t\t\t\t<td><" + packagePrefix.substring(packagePrefix.lastIndexOf('.')+1) + ":" + theEntity.getLowerLabel() + theAttribute.getUpperLabel() + " /></td>\n");
            }
        }
        out.write("\t\t\t\t\t</" + packagePrefix.substring(packagePrefix.lastIndexOf('.')+1) + ":" + theEntity.getUnqualifiedLowerLabel() + ">\n");
        out.write("\t\t\t\t</tr>\n");
        out.write("\t\t\t</" + packagePrefix.substring(packagePrefix.lastIndexOf('.')+1) + ":foreach" + theEntity.getUnqualifiedLabel() + ">\n");
        out.write("\t\t</table>\n");
        out.write("\t\t<a href=\"../../" + theEntity.getSchema().getUnqualifiedLowerLabel() + "/" + theEntity.getUnqualifiedLowerLabel() + "/add" + theEntity.getUnqualifiedLabel() + ".jsp");
        if (theEntity.getParents().size() > 0) {
            Entity parent = theEntity.getParents().firstElement().getSourceEntity();
            for (int i = 0; i < parent.getPrimaryKeyAttributes().size(); i++) {
                Attribute parentKey = parent.getPrimaryKeyAttributes().elementAt(i);
                if (i == 0)
                    out.write("?");
                else
                    out.write("&");
                out.write(parentKey.getLabel() + "=<c:out value=\"${" + parentKey.getLabel() + "}\"/>");
            }
        }
        out.write("\">Add new " + theEntity.getUnqualifiedLabel() + "</a>\n");
        
    }

    public void generateAddEntityJSP(Schema theSchema, Entity theEntity) throws IOException {
        File theIndexJSP = new File(webAppPath + theSchema.getLowerLabel() + "/" + theEntity.getLowerLabel() + "/" + "add" + theEntity.getLabel() + ".jsp");
        FileWriter fstream = new FileWriter(theIndexJSP);
        BufferedWriter out = new BufferedWriter(fstream);

        generateHeaderBlock(out, true, theEntity.hasInt() || theEntity.hasDateTime());
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
            out.write(generateIndent(2) + "<" + packagePrefix.substring(packagePrefix.lastIndexOf('.')+1) + ":upload" + theEntity.getUpperLabel() + "> ");
            out.write("</" + packagePrefix.substring(packagePrefix.lastIndexOf('.')+1) + ":upload" + theEntity.getUpperLabel() + ">\n");
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
                System.out.println("entity: " + theEntity + "\tancestor: " + ancestors.elementAt(i) + "\tsubkeys: " + ancestors.elementAt(i).getSubKeyAttributes() + "\tparent keys: " + ancestors.elementAt(i).primaryKeyAttributes);
                Attribute ancestorKey = null;
                if (ancestors.elementAt(i).getSubKeyAttributes().size() > 0)
                    ancestorKey = ancestors.elementAt(i).getSubKeyAttributes().firstElement();
                else
                    ancestorKey = ancestors.elementAt(i).getPrimaryKeyAttributes().firstElement();
                out.write(generateIndent(i+2) + "<" + packagePrefix.substring(packagePrefix.lastIndexOf('.')+1) + ":" + ancestors.elementAt(i).getLowerLabel() + " " + ancestorKey.getLabel() + "=\"${" + (ancestorKey.isInt() || ancestorKey.isDateTime() ? "" : "param.") + ancestorKey.getLabel() + "}\" >\n");
            }
            out.write(generateIndent(ancestors.size()+2) + "<" + packagePrefix.substring(packagePrefix.lastIndexOf('.')+1) + ":" + theEntity.getLowerLabel() + ">\n");
            for (int i = 0; i < theEntity.getAttributes().size(); i++) {
                Attribute theAttribute = theEntity.getAttributes().elementAt(i);
                if (theAttribute.isPrimary()) {
                	out.write(generateIndent(ancestors.size()+3) + "<c:set var=\"" + theAttribute.getLabel() + "\" ><" + packagePrefix.substring(packagePrefix.lastIndexOf('.')+1) + ":" + theEntity.getLowerLabel() + theAttribute.getUpperLabel() + "/></c:set>\n");
                	if (theAttribute.isInt())
                		continue;
                }
                out.write(generateIndent(ancestors.size()+3) + "<" + packagePrefix.substring(packagePrefix.lastIndexOf('.')+1) + ":" + theEntity.getLowerLabel() + "" + theAttribute.getUpperLabel() + " " + theAttribute.getLabel() + " = \"${" + (theAttribute.isDateTime() ? "" : "param.") + theAttribute.getLabel() + "}\" />\n");
            }
            out.write(generateIndent(ancestors.size()+2) + "</" + packagePrefix.substring(packagePrefix.lastIndexOf('.')+1) + ":" + theEntity.getLowerLabel() + ">\n");
            for (int i = ancestors.size() - 1; i >= 0; i--) {
                out.write(generateIndent(i+2) + "</" + packagePrefix.substring(packagePrefix.lastIndexOf('.')+1) + ":" + ancestors.elementAt(i).getLowerLabel() + ">\n");
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
        File theIndexJSP = new File(webAppPath + theSchema.getLowerLabel() + "/" + theEntity.getLowerLabel() + "/" + "upload" + theEntity.getLabel() + ".jsp");
        FileWriter fstream = new FileWriter(theIndexJSP);
        BufferedWriter out = new BufferedWriter(fstream);

        generateHeaderPrefix(out, theEntity.hasInt() || theEntity.hasDateTime());

        out.write("<" + packagePrefix.substring(packagePrefix.lastIndexOf('.')+1) + ":upload" + theEntity.getUpperLabel() + "> ");
        out.write("</" + packagePrefix.substring(packagePrefix.lastIndexOf('.')+1) + ":upload" + theEntity.getUpperLabel() + ">\n");

        out.write("<c:redirect url=\"" + theEntity.getUnqualifiedLowerLabel() + "List.jsp \"/>\n");

        out.close();
    }
    
    
    public void generateEditEntityJSP(Schema theSchema, Entity theEntity) throws IOException {
        File theIndexJSP = new File(webAppPath + theSchema.getLowerLabel() + "/" + theEntity.getLowerLabel() + "/" + "edit" + theEntity.getLabel() + ".jsp");
        FileWriter fstream = new FileWriter(theIndexJSP);
        BufferedWriter out = new BufferedWriter(fstream);

        generateHeaderBlock(out, true, theEntity.hasInt() || theEntity.hasDateTime());
        out.write("<h2>Add " + theEntity.getUnqualifiedLabel() + ":</h2>\n");

        out.write("\n<c:choose>\n");
        out.write("\t<c:when test=\"${empty param.submit}\">\n");
        out.write("\t\t<form action=\"edit" + theEntity.getLabel() + ".jsp\" method=\"post\" >\n");
        
        for (int i = 0; i < theEntity.getAttributes().size(); i++) {
            Attribute theAttribute = theEntity.getAttributes().elementAt(i);
            if (theAttribute.isPrimary() && theAttribute.isInt())
                out.write("<fmt:parseNumber var=\"" + theAttribute.getLabel() + "\" value=\"${param." + theAttribute.getLabel() + "}\" />\n");
            if (theAttribute.isPrimary() && theAttribute.isDateTime())
                out.write("<fmt:parseDate var=\"" + theAttribute.getLabel() + "\" value=\"${param." + theAttribute.getLabel() + "}\" pattern=\"yyyy-MM-dd HH:mm:ss.S\" />\n");
        }
        out.write("<" + packagePrefix.substring(packagePrefix.lastIndexOf('.')+1) + ":" + theEntity.getLowerLabel());
        for (int i = 0; i < theEntity.getAttributes().size(); i++) {
            Attribute theAttribute = theEntity.getAttributes().elementAt(i);
            if (theAttribute.isPrimary())
                out.write(" " + theAttribute.getLabel() + "=\"${" + (theAttribute.isInt() || theAttribute.isDateTime() ? "" : "param.") + theAttribute.getLabel() + "}\"");
        }
        out.write(">\n");
        
        
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
            else {
                out.write("\t\t\t\t\t<tr>\n");
                out.write("\t\t\t\t\t\t<th align=left>" + theAttribute.getUpperLabel() + "</th>\n");
                out.write("\t\t\t\t\t\t<td><input type=\"text\" name=\"" + theAttribute.getLabel() + "\" size=\"40\" value=\"");
                generateAttributeTag(false, out, theEntity, theAttribute);
                out.write("\"></td>\n");
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
        out.write("\t\t</" + packagePrefix.substring(packagePrefix.lastIndexOf('.')+1) + ":" + theEntity.getLowerLabel() + ">\n");
        out.write("\t\t</form>\n");
        out.write("\t</c:when>\n");
        out.write("\t<c:when test=\"${param.submit == 'Cancel'}\">\n");
        out.write("\t\t<c:redirect url=\"../../index.jsp\" />\n");
        out.write("\t</c:when>\n");
        out.write("\t<c:when test=\"${param.submit == 'Save'}\">\n");
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
            System.out.println("entity: " + theEntity + "\tancestor: " + ancestors.elementAt(i) + "\tsubkeys: " + ancestors.elementAt(i).getSubKeyAttributes() + "\tparent keys: " + ancestors.elementAt(i).primaryKeyAttributes);
            Attribute ancestorKey = null;
            if (ancestors.elementAt(i).getSubKeyAttributes().size() > 0)
                ancestorKey = ancestors.elementAt(i).getSubKeyAttributes().firstElement();
            else
                ancestorKey = ancestors.elementAt(i).getPrimaryKeyAttributes().firstElement();
            out.write(generateIndent(i+2) + "<" + packagePrefix.substring(packagePrefix.lastIndexOf('.')+1) + ":" + ancestors.elementAt(i).getLowerLabel() + " " + ancestorKey.getLabel() + "=\"${" + (ancestorKey.isInt() || ancestorKey.isDateTime() ? "" : "param.") + ancestorKey.getLabel() + "}\" >\n");
        }
        out.write(generateIndent(ancestors.size()+2) + "<" + packagePrefix.substring(packagePrefix.lastIndexOf('.')+1) + ":" + theEntity.getLowerLabel());
        for (int i = 0; i < theEntity.getAttributes().size(); i++) {
            Attribute theAttribute = theEntity.getAttributes().elementAt(i);
            if (theAttribute.isPrimary())
                out.write(" " + theAttribute.getLabel() + "=\"${" + (theAttribute.isInt() || theAttribute.isDateTime() ? "" : "param.") + theAttribute.getLabel() + "}\"");
        }
        out.write(">\n");
        //out.write(generateIndent(ancestors.size()+2) + "<" + packagePrefix.substring(packagePrefix.lastIndexOf('.')+1) + ":" + theEntity.getLowerLabel() + ">\n");
        for (int i = 0; i < theEntity.getAttributes().size(); i++) {
            Attribute theAttribute = theEntity.getAttributes().elementAt(i);
            if ((theAttribute.isPrimary() && theAttribute.isInt()) || theAttribute.isDomain() || theAttribute.isImage())
                continue;
            out.write(generateIndent(ancestors.size()+3) + "<" + packagePrefix.substring(packagePrefix.lastIndexOf('.')+1) + ":" + theEntity.getLowerLabel() + "" + theAttribute.getUpperLabel() + " " + theAttribute.getLabel() + " = \"${" + (theAttribute.isDateTime() ? "" : "param.") + theAttribute.getLabel() + "}\" />\n");
        }
        out.write(generateIndent(ancestors.size()+2) + "</" + packagePrefix.substring(packagePrefix.lastIndexOf('.')+1) + ":" + theEntity.getLowerLabel() + ">\n");
        for (int i = ancestors.size() - 1; i >= 0; i--) {
            out.write(generateIndent(i+2) + "</" + packagePrefix.substring(packagePrefix.lastIndexOf('.')+1) + ":" + ancestors.elementAt(i).getLowerLabel() + ">\n");
        }

        out.write("\t\t<c:redirect url=\"" + theEntity.getLowerLabel() + ".jsp\" >\n");
        for (int i = 0; i < theEntity.getAttributes().size(); i++) {
            Attribute theAttribute = theEntity.getAttributes().elementAt(i);
            if (theAttribute.isPrimary() && theAttribute.isInt()) {
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
    
    public String generateIndent(int length) {
        StringBuffer theIndent = new StringBuffer();
        for (int i = 0; i < length; i++)
            theIndent.append("\t");
        return theIndent.toString();
    }

    public void generateHeader(Database theDatabase) throws IOException {
        File theHeaderJSP  = new File(webAppPath + "header.jsp");
        FileWriter fstream = new FileWriter(theHeaderJSP);
        BufferedWriter out = new BufferedWriter(fstream);
        out.write("<img src=\"/" + projectName + "/images/icts_logo2.jpg\"><br>\n");
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
    
    public void generateHeaderBlock(BufferedWriter out, boolean uplink) throws IOException {
        generateHeaderBlock(out, uplink, false);
    }
    
    public void generateHeaderBlock(BufferedWriter out, boolean uplink, boolean hasDateTime) throws IOException {
        generateHeaderPrefix(out, hasDateTime);

        out.write("\n<html>\n");
        out.write("<head>\n");
        out.write("</head>\n");
        out.write("<body>\n");
        out.write("<c:import url=\"" + (uplink ? "../../" : "") + "header.jsp\" />\n");
    }
    
    public void generateHeaderPrefix(BufferedWriter out, boolean hasDateTime) throws IOException {
        out.write("<%@ taglib prefix=\"sql\" uri=\"http://java.sun.com/jsp/jstl/sql\"%>\n");
        out.write("<%@ taglib prefix=\"c\" uri=\"http://java.sun.com/jsp/jstl/core\"%>\n");
        if (hasDateTime)
            out.write("<%@ taglib prefix=\"fmt\" uri=\"http://java.sun.com/jsp/jstl/fmt\"%>\n");
        out.write("<%@ taglib prefix=\"" + packagePrefix.substring(packagePrefix.lastIndexOf('.')+1) + "\" uri=\"http://icts.uiowa.edu/" + packagePrefix.substring(packagePrefix.lastIndexOf('.')+1) + "\"%>\n");
    }

    public void generateFooterBlock(BufferedWriter out, boolean uplink) throws IOException {
        out.write("\n<c:import url=\"" + (uplink ? "../../" : "") + "footer.jsp\" />\n");
        out.write("</body>\n");
        out.write("</html>\n");
    }
    
    public void generateDbtest(Database theDatabase) throws IOException {
        File theIndexJSP  = new File(webAppPath + "dbtest.jsp");
        FileWriter fstream = new FileWriter(theIndexJSP);
        BufferedWriter out = new BufferedWriter(fstream);
        out.write("<%@ taglib prefix=\"" + packagePrefix.substring(packagePrefix.lastIndexOf('.')+1) + "\" uri=\"http://icts.uiowa.edu/" + packagePrefix.substring(packagePrefix.lastIndexOf('.')+1) + "\"%>\n");
        out.write("    <%@ page  errorPage=\"/error/dberror.jsp\" %>" + "\n" 
        		+ "<" +  packagePrefix.substring(packagePrefix.lastIndexOf('.')+1) + ":dbtest/>");
  

        out.close();
    }
    
    

}
