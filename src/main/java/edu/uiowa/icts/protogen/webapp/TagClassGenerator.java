/*
 * Created on May 12, 2008
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.uiowa.icts.protogen.webapp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.uiowa.icts.protogen.model.Attribute;
import edu.uiowa.icts.protogen.model.Database;
import edu.uiowa.icts.protogen.model.Domain;
import edu.uiowa.icts.protogen.model.Entity;
import edu.uiowa.icts.protogen.model.Relationship;
import edu.uiowa.icts.protogen.model.Schema;

public class TagClassGenerator {
	static Logger log = LogManager.getLogger(TagClassGenerator.class);

    String projectPath = null;

    String packagePrefix = null;
    
    String projectName = null;

    File sourceRootDirectory = null;

    File packagePrefixDirectory = null;

    File tagDirectory = null;
    
    Schema theSchema = null;
    
    private String databaseType = "postgres";

    /**
     * @param projectPath
     * @param packagePrefix
     * @param projectName
     * @param databaseType - postgres or sqlserver supported
     */
    public TagClassGenerator( String projectPath, String packagePrefix, String projectName, String databaseType ) {
        this.projectPath = projectPath;
        this.packagePrefix = packagePrefix;
        this.projectName = projectName;
        this.databaseType = databaseType;
    }
    
    /**
     * @param projectPath
     * @param packagePrefix
     * @param projectName
     */
    public TagClassGenerator(String projectPath, String packagePrefix, String projectName) {
        this.projectPath = projectPath;
        this.packagePrefix = packagePrefix;
        this.projectName = projectName;
    }

    /**
     * @param theDatabase
     * @throws IOException
     */
    public void generateTagClasses(Database theDatabase) throws IOException {
		log.debug("Creating Tag CLasses");
        generateSourceDirectoryRoot(projectPath);
        generatePackagePrefixDirectories(packagePrefix);

        for (int i = 0; i < theDatabase.getSchemas().size(); i++) {
            theSchema = theDatabase.getSchemas().elementAt(i);
            for (int j = 0; j < theDatabase.getSchemas().elementAt(i).getDomains().size(); j++)
                generateDomainClasses(theDatabase.getSchemas().elementAt(i).getDomains().elementAt(j));
            for (int j = 0; j < theDatabase.getSchemas().elementAt(i).getEntities().size(); j++)
                generateEntityTagClasses(theDatabase.getSchemas().elementAt(i).getEntities().elementAt(j));
        }

        generateBodyTagSupportClass();
        generateTagSupportClass();
        generateSequenceClass();
        generateDBTestClass();
        generateExportImport();
    }

    private void generateExportImport() {
    	
	}

	private void generateSourceDirectoryRoot(String projectPath) throws IOException {
        //sourceRootDirectory = new File((projectPath.endsWith("/src") ? projectPath : projectPath + "/src"));
    	sourceRootDirectory = new File(projectPath);
        if (sourceRootDirectory.exists()) {
            if (sourceRootDirectory.isFile())
                throw new IOException("project source directory a normal file");
        } else {
            sourceRootDirectory.mkdir();
        }
    }

    private void generatePackagePrefixDirectories(String packagePrefix) throws IOException {
        StringBuffer path = new StringBuffer(sourceRootDirectory.getPath());
        StringTokenizer theTokenizer = new StringTokenizer(packagePrefix, ".");
        while (theTokenizer.hasMoreTokens()) {
            path.append("/");
            path.append(theTokenizer.nextToken());
            packagePrefixDirectory = new File(path.toString());
            if (packagePrefixDirectory.exists()) {
                if (packagePrefixDirectory.isFile())
                    throw new IOException("project package directory " + path + " is a normal file");
            } else {
                packagePrefixDirectory.mkdir();
            }
        }
    }

    private void generateDomainClasses(Domain theDomain) throws IOException {
        if (theDomain.getLabel().equals("Image"))
            return;
        if (theDomain.getLabel().equals("trinary"))
            return;

        File domainFile = new File(packagePrefixDirectory, "/" + theDomain.getUnqualifiedLabel() + ".java");
        FileWriter fstream = new FileWriter(domainFile);
        BufferedWriter out = new BufferedWriter(fstream);

        out.write("package " + packagePrefix + ";\n\n");
        out.write("\npublic class " + theDomain.getUnqualifiedLabel() + " extends Object {\n\n");

        // close out class
        out.write("\n}\n");
        out.close();
}

    private void generateEntityTagClasses(Entity theEntity) throws IOException {
        tagDirectory = new File(packagePrefixDirectory, "/" + theEntity.getUnqualifiedLowerLabel());
        if (tagDirectory.exists()) {
            if (tagDirectory.isFile())
                throw new IOException("project tag directory " + tagDirectory.getPath() + " is a normal file");
        } else {
            tagDirectory.mkdir();
        }

        generateEntityBaseClass(theEntity);
        generateEntityIteratorClass(theEntity);
        generateEntityDeleterClass(theEntity);
        if (theEntity.hasCounter())
        	generateEntityShifterClass(theEntity);
        if (theEntity.hasImage() || theEntity.hasBinaryDomainAttribute())
            generateEntityUploadServlet(theEntity);

        for (int i = 0; i < theEntity.getAttributes().size(); i++) {
            generateEntityHelperClass(theEntity, theEntity.getAttributes().elementAt(i));
//            if (theEntity.hasImage() || theEntity.hasBinaryDomainAttribute())
//                generateEntityUploadHelperClass(theEntity, theEntity.getAttributes().elementAt(i));
            if (theEntity.getAttributes().elementAt(i).isDateTime() || theEntity.getAttributes().elementAt(i).isTimestamp())
                generateEntityToNowHelperClass(theEntity, theEntity.getAttributes().elementAt(i));
        }
    }
    
    private void generateParentEntityReferences(BufferedWriter out, Entity theEntity, String indentString) throws IOException {
    	ArrayList<String> entityList = new ArrayList<String>(0);
        StringBuffer parentBuffer = new StringBuffer();
        for (int i = 0; i < theEntity.getParents().size(); i++) {
            Relationship theRelationship = theEntity.getParents().elementAt(i);
            Entity theSourceEntity = theRelationship.getSourceEntity();
            if( !entityList.contains(theSourceEntity.getLabel()) ){
            	out.write(indentString + theSourceEntity.getLabel() + " the" + theSourceEntity.getLabel() + " = (" + theSourceEntity.getLabel() + ")findAncestorWithClass(this, " + theSourceEntity.getLabel() + ".class);\n");
            	out.write(indentString + "if (the" + theSourceEntity.getLabel() + "!= null)" + "\n" + indentString + "\tparentEntities.addElement(the" + theSourceEntity.getLabel() + ");\n");
            	parentBuffer.append(indentString + "if (the" + theSourceEntity.getLabel() + " == null) {\n");
            	// parentBuffer.append(indentString + "\tthrow new JspTagException(\"Error: no containing " + theSourceEntity.getLabel() + " for " + theEntity.getLabel() + "\");\n");
            	parentBuffer.append(indentString + "} else {\n");
            	for (int j = 0; j < theSourceEntity.getPrimaryKeyAttributes().size(); j++) {
            		parentBuffer.append(indentString + "\t" + theEntity.getAttributeBySQLLabel(theRelationship.getForeignReferencedAttribute(theSourceEntity.getPrimaryKeyAttributes().elementAt(j).getSqlLabel())).getLabel() + " = the" + theSourceEntity.getLabel() + ".get"
            				+ Character.toUpperCase(theSourceEntity.getPrimaryKeyAttributes().elementAt(j).getLabel().charAt(0))
            				+ theSourceEntity.getPrimaryKeyAttributes().elementAt(j).getLabel().substring(1) + "();\n");
            	}
            	parentBuffer.append(indentString + "}\n");
            	
            	entityList.add(theSourceEntity.getLabel());
            }
        }
        out.write("\n" + parentBuffer.toString() + "\n");
    }
    
    private void generateParentImports(BufferedWriter out, Entity theEntity) throws IOException {
    	ArrayList<String> entityList = new ArrayList<String>(0);
        for (int i = 0; i < theEntity.getParents().size(); i++) {
            Entity theSourceEntity = theEntity.getParents().elementAt(i).getSourceEntity();
            if( !entityList.contains(theSourceEntity.getLabel()) ){
            	out.write("import " + packagePrefix + "." + theSourceEntity.getLowerLabel() + "." + theSourceEntity.getLabel() + ";\n");
            	entityList.add(theSourceEntity.getLabel());
            }
        }        
    }

    private void generateEntityBaseClass(Entity theEntity) throws IOException {
        if ((theEntity.getPrimaryKeyAttributes() == null || theEntity.getPrimaryKeyAttributes().size() == 0) && (theEntity.getSubKeyAttributes() == null || theEntity.getSubKeyAttributes().size() == 0))
            return;

        File baseClassFile = new File(tagDirectory, "/" + theEntity.getUnqualifiedLabel() + ".java");
        FileWriter fstream = new FileWriter(baseClassFile);
        BufferedWriter out = new BufferedWriter(fstream);

        out.write("package " + packagePrefix + "." + theEntity.getUnqualifiedLowerLabel() + ";\n\n");
        out.write("import java.sql.PreparedStatement;\n");
        out.write("import java.sql.ResultSet;\n");
        out.write("import java.sql.SQLException;\n");
        out.write("import java.util.Vector;\n\n");
        
        // out.write("import org.json.JSONObject;\n");
        // out.write("import org.json.JSONException;\n\n");
        
        out.write("import org.apache.logging.log4j.LogManager;\n");
        out.write("import org.apache.logging.log4j.Logger;\n");
        
        if (theEntity.hasDateTime())
            out.write("import java.util.Date;\n");
          if (theEntity.hasTimestamp())
            out.write("import java.sql.Timestamp;\n");
//        if (theEntity.hasImage())
//            out.write("import java.awt.Image;\n");
        out.write("\n");
        out.write("import javax.servlet.jsp.JspException;\n");
        out.write("import javax.servlet.jsp.JspTagException;\n");
        out.write("import javax.servlet.jsp.tagext.Tag;\n\n");
        generateParentImports(out, theEntity);
        out.write("\n");
        out.write("import " + packagePrefix + "." + projectName + "TagSupport;\n");
        for (int i = 0; i < theEntity.getAttributes().size(); i++) {
            Attribute theAttribute = theEntity.getAttributes().elementAt(i);
            if (theAttribute.getDefaultValue().startsWith("Sequence") || (theAttribute.isPrimary() && !theAttribute.isForeign() && theAttribute.isInt())) {
                out.write("import " + packagePrefix + ".Sequence;\n");
                break;
            }
        }
        out.write("\n@SuppressWarnings(\"serial\")");
        out.write("\npublic class " + theEntity.getUnqualifiedLabel() + " extends " + projectName + "TagSupport {\n\n");
        out.write("\tstatic " + theEntity.getUnqualifiedLabel() + " currentInstance = null;\n");
        out.write("\tboolean commitNeeded = false;\n");
        out.write("\tboolean newRecord = false;\n\n");
        out.write("\tprivate static final Logger log = LogManager.getLogger("+theEntity.getUnqualifiedLabel() +".class);\n\n");
        out.write("\tVector<" + projectName + "TagSupport> parentEntities = new Vector<" + projectName + "TagSupport>();\n\n");

        // declare attributes
        for (int i = 0; i < theEntity.getAttributes().size(); i++) {
            Attribute theAttribute = theEntity.getAttributes().elementAt(i);
            out.write("\t" + (theAttribute.getDomain() == null ? theAttribute.getType() : theAttribute.getDomain().getJavaType()) + " " + theAttribute.getLabel() + " = " + theAttribute.getInitializer()
                            + ";\n");
        }

        out.write("\n\tprivate String var = null;\n");
        
        out.write("\n\tprivate "+theEntity.getLabel()+" cached"+theEntity.getLabel()+" = null;\n");
        
        generateDoStartTag(theEntity, out);

        generateDoEndTag(theEntity, out);
        
        // generateToJson(theEntity, out);
        
        generateInsertEntity(theEntity, out);

        generateSettersGetters(theEntity, out, false, true);
        
        generateSetterGetter("String", "var", false, out);
        
        generateFunctionGetters(theEntity, out);

        generateServiceStateReset(theEntity, out);

        // close out class
        out.write("\n}\n");
        out.close();
    }
    
    private void generateDoStartTag(Entity theEntity, BufferedWriter out) throws IOException {
        int attrSeq = 0;
        int keySeq = 0;

        Attribute keyAttribute = (theEntity.getSubKeyAttributes().size() == 0 ? theEntity.getPrimaryKeyAttributes().firstElement() : theEntity.getSubKeyAttributes().firstElement());

        StringBuffer resultBuffer = new StringBuffer();
        StringBuffer paramBuffer = new StringBuffer();
        StringBuffer queryBuffer = new StringBuffer();

        out.write("\n\tpublic int doStartTag() throws JspException {\n");
        out.write("\t\tcurrentInstance = this;\n");
        out.write("\t\ttry {\n");
        
        generateParentEntityReferences(out, theEntity, "\t\t\t");

        // generate context test for a containing iterator, if it's there pull primary key values from it preferentially
        out.write("\t\t\t" + theEntity.getLabel() + "Iterator the" + theEntity.getLabel() + "Iterator = ("
                + theEntity.getLabel() + "Iterator)findAncestorWithClass(this, " + theEntity.getLabel() + "Iterator.class);\n\n");
        out.write("\t\t\t" + "if (the" + theEntity.getLabel() + "Iterator != null) {\n");
           for (int j = 0; j < theEntity.getPrimaryKeyAttributes().size(); j++) {
               out.write("\t\t\t\t" + theEntity.getPrimaryKeyAttributes().elementAt(j).getLabel() + " = the" + theEntity.getLabel() + "Iterator.get"
                       + Character.toUpperCase(theEntity.getPrimaryKeyAttributes().elementAt(j).getLabel().charAt(0))
                       + theEntity.getPrimaryKeyAttributes().elementAt(j).getLabel().substring(1) + "();\n");
           }
        out.write("\t\t\t}\n\n");

        // Logic for instance context (i.e., what parents already exist in the run-time environment.  This needs to support
        // creation of new instances for roots of instance trees (the no active parents case), entry via a navigation path
        // (the one active parent case), or instantiation of a new instance somewhere other than a root (the all parents
        // active case).
        
        // case for no active parents
        out.write("\t\t\tif (the" + theEntity.getLabel() + "Iterator == null");
        for (int j = 0; j < theEntity.getParents().size(); j++) {
            Entity theParentEntity = theEntity.getParents().elementAt(j).getSourceEntity();
            out.write(" && the" + theParentEntity.getLabel() + " == null");
        }
        out.write(" && " + keyAttribute.getLabel() + " == " + keyAttribute.getInitializer() + ") {\n");
        out.write("\t\t\t\t// no " + keyAttribute.getLabel() + " was provided - the default is to assume that it is a new "
                + theEntity.getLabel() + " and to generate a new " + keyAttribute.getLabel() + "\n");
        out.write("\t\t\t\t" + keyAttribute.getLabel() + " = " + keyAttribute.getDefaultValue() + ";\n");
        // out.write("\t\t\t\tlog.debug(\"generating new " + theEntity.getLabel() + " \" + " + keyAttribute.getLabel()+ ");\n");
        out.write("\t\t\t\tinsertEntity();\n");
        
        //TODO need case for null iterator and all active parents
//        out.write("\t\t\t} else if (the" + theEntity.getLabel() + "Iterator == null");
//        for (int j = 0; j < theEntity.getParents().size(); j++) {
//            Entity theParentEntity = theEntity.getParents().elementAt(j).getSourceEntity();
//            out.write(" && the" + theParentEntity.getLabel() + " != null");
//        }
//        out.write(" && " + keyAttribute.getLabel() + " == " + keyAttribute.getInitializer() + ") {\n");
//        out.write("\t\t\t\tboolean found = false;\n");
//        out.write("\t\t\t\tPreparedStatement stmt = getConnection().prepareStatement(\"select");

        
        // case for one parent active among multiple parents - we iterate through the parents, using each of them in turn as the only non-null
        // ancestor
        if (theEntity.getParents().size() > 1) {
	        for (int i = 0; i < theEntity.getParents().size(); i++) {
	            Entity theNavigationEntity = null;
	            out.write("\t\t\t} else if (the" + theEntity.getLabel() + "Iterator == null && ");
	            for (int j = 0; j < theEntity.getParents().size(); j++) {
	                Entity theSourceEntity = theEntity.getParents().elementAt(j).getSourceEntity();
	                if (j > 0)
	                    out.write(" && ");
	                out.write("the" + theSourceEntity.getLabel() + " " + (i==j ? "!" : "=") + "= null");
	                if (i == j)
	                    theNavigationEntity = theSourceEntity;
	            }
	            out.write(") {\n");
	            
	            out.write("\t\t\t\t// an " + keyAttribute.getLabel() + " was provided as an attribute - we need to load a " + theEntity.getLabel() + " from the database\n");
	            out.write("\t\t\t\tboolean found = false;\n");
	            out.write("\t\t\t\tPreparedStatement stmt = getConnection().prepareStatement(\"select");
	            paramBuffer = new StringBuffer();
	            queryBuffer = new StringBuffer();
	            resultBuffer = new StringBuffer();
	            keySeq = 0;
	            attrSeq = 0;
	            boolean firstParam = true;
	            for (int k = 0; k < theEntity.getAttributes().size(); k++) {
	                Attribute theAttribute = theEntity.getAttributes().elementAt(k);
	                if (theAttribute.isPrimary() && theNavigationEntity.isPrimaryReference(theAttribute)) {
	                    paramBuffer.append((firstParam ? " " : " and ") + theAttribute.getSqlLabel() + " = ?");
	                    queryBuffer.append("\t\t\t\tstmt."
	                            + theAttribute.getSQLMethod(false)
	                            + "("
	                            + (keySeq + 1)
	                            + ","
	                            + theAttribute.getLabel()
	                            + (theAttribute.isDateTime() ? " == null ? null : new java.sql."
	                                    + (theAttribute.isTimestamp() ? "Timestamp" : "Date") + "(" + theAttribute.getLabel() + ".getTime())"
	                                    : "") + ");\n");
	                    keySeq++;
	                    firstParam = false;
	                }else if (theNavigationEntity.isPrimaryReference(theAttribute)) {
	                	paramBuffer.append((firstParam ? " " : " and ") + theAttribute.getSqlLabel() + " = ?");
	                	
	                	queryBuffer.append("\t\t\t\tstmt."
	                			+ theAttribute.getSQLMethod(false)
	                			+ "(" + (keySeq + 1) + ","
	                			+ theAttribute.getLabel()
	                			+ (theAttribute.isDateTime() ? " == null ? null : new java.sql."
	                			+ (theAttribute.isTimestamp() ? "Timestamp" : "Date") + "(" + theAttribute.getLabel() + ".getTime())" : "") + ");\n");
	                	keySeq++;
	                	firstParam = false;
	                } else {
	                	
                		out.write((attrSeq == 0 ? " " : ",") + theAttribute.getSqlLabel());
	                    resultBuffer.append("\t\t\t\t\tif (" + theAttribute.getLabel() + " == " + theAttribute.getInitializer() + ")\n");
	                    resultBuffer.append("\t\t\t\t\t\t" + theAttribute.getLabel() + " = "
	                    		+ "rs." + theAttribute.getSQLMethod(true) + "(" + (attrSeq + 1) + ");\n");
	                    attrSeq++;	                		
	                    
	                }
	            }
	            out.write(" from " + theSchema.getSqlLabel()+ "." + theEntity.getSqlLabel() + " where");
	            out.write(paramBuffer.toString());
	            out.write("\");\n");
	            out.write(queryBuffer.toString());
	            out.write("\t\t\t\tResultSet rs = stmt.executeQuery();\n");
	            out.write("\t\t\t\twhile (rs.next()) {\n");
	            out.write(resultBuffer.toString());
	            out.write("\t\t\t\t\tfound = true;\n");
	            out.write("\t\t\t\t}\n");
	            out.write("\t\t\t\trs.close();\n");
	            out.write("\t\t\t\tstmt.close();\n");
	            out.write("\n");
	            out.write("\t\t\t\tif (!found) {\n");
	            out.write("\t\t\t\t\tinsertEntity();\n");
	            out.write("\t\t\t\t}\n");
	        }
        }
        
        // case for all parents active - this just defaults to the presumption that if one of the single active parent
        // cases didn't trigger, we're in the all active parents case
        out.write("\t\t\t} else {\n");
        out.write("\t\t\t\t// an iterator or " + keyAttribute.getLabel() + " was provided as an attribute - we need to load a "
                + theEntity.getLabel() + " from the database\n");
        out.write("\t\t\t\tboolean found = false;\n");
        out.write("\t\t\t\tPreparedStatement stmt = getConnection().prepareStatement(\"select");
        paramBuffer = new StringBuffer();
        queryBuffer = new StringBuffer();
        resultBuffer = new StringBuffer();
        keySeq = 0;
        attrSeq = 0;
        for (int i = 0; i < theEntity.getAttributes().size(); i++) {
            Attribute theAttribute = theEntity.getAttributes().elementAt(i);
            if (theAttribute.isPrimary()) {
                paramBuffer.append((i == 0 ? " " : " and ") + theAttribute.getSqlLabel() + " = ?");
                queryBuffer.append("\t\t\t\tstmt."
                        + theAttribute.getSQLMethod(false)
                        + "("
                        + (keySeq + 1)
                        + ","
                        + theAttribute.getLabel()
                        + (theAttribute.isDateTime() ? " == null ? null : new java.sql."
                                + (theAttribute.isTimestamp() ? "Timestamp" : "Date") + "(" + theAttribute.getLabel() + ".getTime())"
                                : "") + ");\n");
                keySeq++;
            } else {
                out.write((attrSeq == 0 ? " " : ",") + theAttribute.getSqlLabel());
                resultBuffer.append("\t\t\t\t\tif (" + theAttribute.getLabel() + " == " + theAttribute.getInitializer() + ")\n");
                resultBuffer.append("\t\t\t\t\t\t" + theAttribute.getLabel() + " = "
//                        + (theAttribute.getDomain() == null ? "" : "(" + theAttribute.getDomain().getLabel() + ") (Object) ")
                        + "rs." + theAttribute.getSQLMethod(true) + "(" + (attrSeq + 1) + ");\n");
                attrSeq++;
            }
        }
        if (attrSeq == 0)
            out.write(" 1");
        out.write(" from " + theSchema.getSqlLabel() + "." + theEntity.getSqlLabel() + " where");
        out.write(paramBuffer.toString());
        out.write("\");\n");
        out.write(queryBuffer.toString());
        out.write("\t\t\t\tResultSet rs = stmt.executeQuery();\n");
        out.write("\t\t\t\twhile (rs.next()) {\n");
        out.write(resultBuffer.toString());
        out.write("\t\t\t\t\tfound = true;\n");
        out.write("\t\t\t\t}\n");
        out.write("\t\t\t\trs.close();\n");
        out.write("\t\t\t\tstmt.close();\n");
        out.write("\n");
        out.write("\t\t\t\tif (!found) {\n");
        out.write("\t\t\t\t\tinsertEntity();\n");
        out.write("\t\t\t\t}\n");
        out.write("\t\t\t}\n");
        
        // end of context cases
        
        out.write("\t\t} catch (SQLException e) {\n");
        
        out.write("\t\t\tlog.error(\"JDBC error retrieving " + keyAttribute.getLabel() + " \" + " + keyAttribute.getLabel() + ", e);\n\n");
        
        out.write("\t\t\tfreeConnection();\n");
        out.write("\t\t\tclearServiceState();\n\n");
        
        out.write("\t\t\tTag parent = getParent();\n");
        out.write("\t\t\tif(parent != null){\n");
        out.write("\t\t\t\tpageContext.setAttribute(\"tagError\", true);\n");
        out.write("\t\t\t\tpageContext.setAttribute(\"tagErrorException\", e);\n");
        out.write("\t\t\t\tpageContext.setAttribute(\"tagErrorMessage\", \"JDBC error retrieving " + keyAttribute.getLabel() + " \" + " + keyAttribute.getLabel() + ");\n");
        out.write("\t\t\t\treturn parent.doEndTag();\n");
        out.write("\t\t\t}else{\n");
        out.write("\t\t\t\tthrow new JspException(\"JDBC error retrieving " + keyAttribute.getLabel() + " \" + " + keyAttribute.getLabel() + ",e);\n");
        out.write("\t\t\t}\n\n");
        
        out.write("\t\t} finally {\n");
        out.write("\t\t\tfreeConnection();\n");
        out.write("\t\t}\n");
        
        out.write("\n");
        
        out.write("\t\tif(pageContext != null){\n");
        out.write("\t\t\t"+theEntity.getLabel()+" current"+theEntity.getLabel()+" = ("+theEntity.getLabel()+") pageContext.getAttribute(\"tag_"+theEntity.getLowerLabel()+"\");\n");
        out.write("\t\t\tif(current"+theEntity.getLabel()+" != null){\n");
        out.write("\t\t\t\tcached"+theEntity.getLabel()+" = current"+theEntity.getLabel()+";\n");
        out.write("\t\t\t}\n");
        out.write("\t\t\tcurrent"+theEntity.getLabel()+" = this;\n");
        out.write("\t\t\tpageContext.setAttribute((var == null ? \"tag_"+theEntity.getLowerLabel()+"\" : var), current"+theEntity.getLabel()+");\n");
        out.write("\t\t}\n\n");
        
        out.write("\t\treturn EVAL_PAGE;\n");
        out.write("\t}\n");
    }
    
    private void generateDoEndTag(Entity theEntity, BufferedWriter out) throws IOException {
        int attrSeq = 0;
        int keySeq = 0;

        StringBuffer paramBuffer = new StringBuffer();
        StringBuffer queryBuffer = new StringBuffer();

        out.write("\n\tpublic int doEndTag() throws JspException {\n");
        out.write("\t\tcurrentInstance = null;\n");
        
        out.write("\n");
        
        out.write("\t\tif(pageContext != null){\n");
        out.write("\t\t\tif(this.cached"+theEntity.getLabel()+" != null){\n");
        out.write("\t\t\t\tpageContext.setAttribute((var == null ? \"tag_"+theEntity.getLowerLabel()+"\" : var), this.cached"+theEntity.getLabel()+");\n");
        out.write("\t\t\t}else{\n");
        out.write("\t\t\t\tpageContext.removeAttribute((var == null ? \"tag_"+theEntity.getLowerLabel()+"\" : var));\n");
        out.write("\t\t\t\tthis.cached"+theEntity.getLabel()+" = null;\n");
        out.write("\t\t\t}\n");
        out.write("\t\t}\n\n");
        
        out.write("\t\ttry {\n");
        
        out.write("\t\t\tBoolean error = null; // (Boolean) pageContext.getAttribute(\"tagError\");\n");
        
        out.write("\t\t\tif(pageContext != null){\n");
        out.write("\t\t\t\terror = (Boolean) pageContext.getAttribute(\"tagError\");\n");
        out.write("\t\t\t}\n\n");
        
        out.write("\t\t\tif(error != null && error){\n\n");
        
        out.write("\t\t\t\tfreeConnection();\n");
        out.write("\t\t\t\tclearServiceState();\n\n");

        out.write("\t\t\t\tException e = (Exception) pageContext.getAttribute(\"tagErrorException\");\n");
        out.write("\t\t\t\tString message = (String) pageContext.getAttribute(\"tagErrorMessage\");\n\n");
        
        // out.write("\t\t\t\tif(pageContext != null){\n");
        // out.write("\t\t\t\t\te = (Exception) pageContext.getAttribute(\"tagErrorException\");\n");
        // out.write("\t\t\t\t\tmessage = (String) pageContext.getAttribute(\"tagErrorMessage\");\n");
        // out.write("\t\t\t\t}\n\n");
        
        out.write("\t\t\t\tTag parent = getParent();\n");
        out.write("\t\t\t\tif(parent != null){\n");
        out.write("\t\t\t\t\treturn parent.doEndTag();\n");
        
        out.write("\t\t\t\t}else if(e != null && message != null){\n");
        out.write("\t\t\t\t\tthrow new JspException(message,e);\n");
        out.write("\t\t\t\t}else if(parent == null){\n");
        out.write("\t\t\t\t\tpageContext.removeAttribute(\"tagError\");\n");
        out.write("\t\t\t\t\tpageContext.removeAttribute(\"tagErrorException\");\n");
        out.write("\t\t\t\t\tpageContext.removeAttribute(\"tagErrorMessage\");\n");
        out.write("\t\t\t\t}\n");
        out.write("\t\t\t}\n");
        
        out.write("\t\t\tif (commitNeeded) {\n");
        out.write("\t\t\t\tPreparedStatement stmt = getConnection().prepareStatement(\"update " + theSchema.getSqlLabel() + "." + theEntity.getSqlLabel() + " set");
        paramBuffer = new StringBuffer();
        queryBuffer = new StringBuffer();
        attrSeq = 0;
		for ( int i = 0; i < theEntity.getAttributes().size(); i++ ) {
			Attribute theAttribute = theEntity.getAttributes().elementAt( i );
			if ( theAttribute.isPrimary() ) {
				paramBuffer.append( ( keySeq == 0 ? " " : " and " ) + theAttribute.getSqlLabel() + " = ? " );
				keySeq++;
			
				
			} else if ( !theAttribute.isPrimary() && theAttribute.isForeign() ) {

				out.write( ( attrSeq == 0 ? " " : ", " ) + theAttribute.getSqlLabel() + " = ?" );

				queryBuffer.append( "\t\t\t\tif ( " + theAttribute.getLabel() + " == " + theAttribute.getInitializer() + " ) {\n" );
				
				if( theAttribute.isInt() ){
					queryBuffer.append( "\t\t\t\t\tstmt.setNull( " + ( attrSeq + 1 ) + ", java.sql.Types.INTEGER );\n" );
				} else if( theAttribute.isText() ){
					queryBuffer.append( "\t\t\t\t\tstmt.setNull( " + ( attrSeq + 1 ) + ", java.sql.Types.VARCHAR );\n" );
				} else {
					queryBuffer.append( "\t\t\t\t\tstmt.setNull( " + ( attrSeq + 1 ) + ", java.sql.Types.OTHER );\n" );
				}
				
				queryBuffer.append( "\t\t\t\t} else {\n" );
				queryBuffer.append( "\t\t\t\t\t" + "stmt." + theAttribute.getSQLMethod( false ) + "( " + ( attrSeq + 1 ) + ", " + theAttribute.getLabel() );
				if ( theAttribute.isDateTime() ) {
					queryBuffer.append( " == null ? null : new java.sql." + ( theAttribute.isTimestamp() ? "Timestamp" : "Date" ) + "( " + theAttribute.getLabel() + ".getTime() )" );
				}
				queryBuffer.append( " );\n" );
				
				queryBuffer.append( "\t\t\t\t}\n" );

				attrSeq++;

			} else {
				out.write( ( attrSeq == 0 ? " " : ", " ) + theAttribute.getSqlLabel() + " = ?" );
				queryBuffer.append( "\t\t\t\t" + "stmt." + theAttribute.getSQLMethod( false ) + "( " + ( attrSeq + 1 ) + ", " + theAttribute.getLabel() );
				if ( theAttribute.isDateTime() ) {
					queryBuffer.append( " == null ? null : new java.sql." + ( theAttribute.isTimestamp() ? "Timestamp" : "Date" ) + "( " + theAttribute.getLabel() + ".getTime() )" );
				}
				queryBuffer.append( " );\n" );
				attrSeq++;
			}
		}
        out.write(" where" + paramBuffer.toString() + "\");\n");
        out.write(queryBuffer.toString());
        keySeq = 0;
        for (int i = 0; i < theEntity.getAttributes().size(); i++) {
            Attribute theAttribute = theEntity.getAttributes().elementAt(i);
            if (theAttribute.isPrimary()) {
                out.write("\t\t\t\tstmt."
                        + theAttribute.getSQLMethod(false)
                        + "("
                        + (attrSeq + keySeq + 1)
                        + ","
                        + theAttribute.getLabel()
                        + (theAttribute.isDateTime() ? " == null ? null : new java.sql."
                                + (theAttribute.isTimestamp() ? "Timestamp" : "Date") + "(" + theAttribute.getLabel() + ".getTime())"
                                : "") + ");\n");
                keySeq++;
            }
        }
        out.write("\t\t\t\tstmt.executeUpdate();\n");
        out.write("\t\t\t\tstmt.close();\n");
        out.write("\t\t\t}\n");
        out.write("\t\t} catch (SQLException e) {\n");
        out.write("\t\t\tlog.error(\"Error: IOException while writing to the user\", e);\n\n");
        
        out.write("\t\t\tfreeConnection();\n");
        out.write("\t\t\tclearServiceState();\n\n");

        out.write("\t\t\tTag parent = getParent();\n");
        out.write("\t\t\tif(parent != null){\n");
        out.write("\t\t\t\tpageContext.setAttribute(\"tagError\", true);\n");
        out.write("\t\t\t\tpageContext.setAttribute(\"tagErrorException\", e);\n");
        out.write("\t\t\t\tpageContext.setAttribute(\"tagErrorMessage\", \"Error: IOException while writing to the user\");\n");
        out.write("\t\t\t\treturn parent.doEndTag();\n");
        out.write("\t\t\t}else{\n");
        out.write("\t\t\t\tthrow new JspTagException(\"Error: IOException while writing to the user\");\n");
        out.write("\t\t\t}\n\n");
        
        out.write("\t\t} finally {\n");
        out.write("\t\t\tclearServiceState();\n");
        out.write("\t\t\tfreeConnection();\n");
        out.write("\t\t}\n");
        out.write("\t\treturn super.doEndTag();\n");
        out.write("\t}\n");
    }
    
    @SuppressWarnings( "unused" )
	private void generateToJson(Entity theEntity, BufferedWriter out) throws IOException {
        out.write("\n\tpublic String toJson() {\n");
        out.write("\t\tJSONObject job = new JSONObject();\n");
        out.write("\t\ttry {\n");
        for (int i = 0; i < theEntity.getAttributes().size(); i++) {
            Attribute theAttribute = theEntity.getAttributes().elementAt(i);
            out.write("\t\t\tjob.put(\""+theAttribute.getLabel()+"\","+theAttribute.getLabel()+");\n");
        }   
        out.write("\t\t} catch (JSONException e) {\n");
        out.write("\t\t\tlog.error(\"error building json string\",e);\n");
        out.write("\t\t}\n");
        out.write("\t\treturn job.toString();\n");
        out.write("\t}\n");
    }
    
    private void generateInsertEntity(Entity theEntity, BufferedWriter out) throws IOException {
        int attrSeq = 0;

        StringBuffer paramBuffer = new StringBuffer();
        StringBuffer queryBuffer = new StringBuffer();
        StringBuffer keyBuffer = new StringBuffer();
        StringBuffer keyParamBuffer = new StringBuffer();

        out.write("\n\tpublic void insertEntity() throws JspException, SQLException {\n");
        for (int i = 0; i < theEntity.getAttributes().size(); i++) {
            Attribute theAttribute = theEntity.getAttributes().elementAt(i);
            if (!theAttribute.isPrimary() && theAttribute.isText()) {
                out.write("\t\tif (" + theAttribute.getLabel() + " == " + theAttribute.getInitializer() + "){\n");
                out.write("\t\t\t" + theAttribute.getLabel() + " = \"\";\n");
                out.write("\t\t}\n");
            } else if (theAttribute.isCounter()) {
                Entity dominantEntity = theAttribute.getDominantEntity();
                out.write("\t\t" + theAttribute.getLabel() + " = 1;\n");
                if (dominantEntity != theEntity) {
                    for (int j = 0; j < dominantEntity.getPrimaryKeyAttributes().size(); j++) {
                        Attribute dominantAttribute = dominantEntity.getAttributes().elementAt(j);
                        keyBuffer.append(" and " + dominantAttribute.getSqlLabel() + " = ?");
                        keyParamBuffer.append("\t\t\tcountStmt." + dominantAttribute.getSQLMethod(false) + "(" + (j+1) + ", " + dominantAttribute.getLabel() + ");\n");
                    }
                }
                out.write("\t\tPreparedStatement countStmt = getConnection().prepareStatement(\"select max(" + theAttribute.getSqlLabel() + ") from " + theSchema.getSqlLabel() + "." + theEntity.getSqlLabel()
                        + " where true " + keyBuffer.toString() + " having max(" + theAttribute.getSqlLabel() + ") is not null;\");\n");
                out.write(keyParamBuffer.toString());
                out.write("\t\tResultSet rs = countStmt.executeQuery();\n");
                out.write("\t\twhile (rs.next()) {\n");
                out.write("\t\t\t" + theAttribute.getLabel() + " = rs.getInt(1) + 1;\n");
                out.write("\t\t}\n");
                out.write("\t\trs.close();\n");
                out.write("\t\tcountStmt.close();\n");
                out.write("\n");
            } else if (theAttribute.isPrimary() && !theEntity.isForeignReference(theAttribute) && theAttribute.isInt()) {
                out.write("\t\tif (" + theAttribute.getLabel() + " == " + theAttribute.getInitializer() + ") {\n");
                out.write("\t\t\t" + theAttribute.getLabel() + " = " + theAttribute.getDefaultValue() + ";\n");
                out.write("\t\t\tlog.debug(\"generating new " + theEntity.getLabel() + " \" + " + theAttribute.getLabel() + ");\n");
                out.write("\t\t}\n\n");
            }
        }   
        out.write("\t\tPreparedStatement stmt = getConnection().prepareStatement(\"insert into " + theSchema.getSqlLabel() + "." + theEntity.getSqlLabel() + "(");
        Attribute autoIncrementAttribute = null;
        for (int i = 0; i < theEntity.getAttributes().size(); i++) {
            Attribute theAttribute = theEntity.getAttributes().elementAt(i);
            if (theAttribute.isAutoIncrement()) {
                autoIncrementAttribute = theAttribute;
                continue;
            }
            out.write((attrSeq == 0 ? "" : ",") + theAttribute.getSqlLabel());
            paramBuffer.append((attrSeq == 0 ? "?" : ",?"));
            if (!theAttribute.isPrimary() && theAttribute.isForeign()) {
                queryBuffer.append("\t\tif (" + theAttribute.getLabel() + " == " + theAttribute.getInitializer() + ") {\n");
                queryBuffer.append("\t\t\tstmt.setNull(" + (attrSeq + 1) + ", java.sql.Types.INTEGER"  + ");\n");
                queryBuffer.append("\t\t} else {\n");
                queryBuffer.append("\t\t\tstmt."
                        + theAttribute.getSQLMethod(false)
                        + "("
                        + (attrSeq + 1)
                        + ","
                        + theAttribute.getLabel()
                        + (theAttribute.isDateTime() ? " == null ? null : new java.sql."
                                + (theAttribute.isTimestamp() ? "Timestamp" : "Date") + "(" + theAttribute.getLabel() + ".getTime())" : "")
                        + ");\n");
                queryBuffer.append("\t\t}\n");
            } else {
                queryBuffer.append("\t\tstmt."
                        + theAttribute.getSQLMethod(false)
                        + "("
                        + (attrSeq + 1)
                        + ","
                        + theAttribute.getLabel()
                        + (theAttribute.isDateTime() ? " == null ? null : new java.sql."
                                + (theAttribute.isTimestamp() ? "Timestamp" : "Date") + "(" + theAttribute.getLabel() + ".getTime())" : "")
                        + ");\n");
            }
            attrSeq++;
        }
        out.write(") values (" + paramBuffer.toString() + ")\"" + (autoIncrementAttribute != null ? ", java.sql.Statement.RETURN_GENERATED_KEYS" : "") + ");\n");
        out.write(queryBuffer.toString());
        out.write("\t\tstmt.executeUpdate();\n");
        if (autoIncrementAttribute != null) {
            out.write("\n");
            out.write("\t\t// snag the new auto-increment value\n");            
            out.write("\t\tResultSet irs = stmt.getGeneratedKeys();\n");
            out.write("\t\twhile (irs.next()) {\n");
            out.write("\t\t\t" + autoIncrementAttribute.getLabel() + " = irs.getInt(1);\n");
            out.write("\t\t}\n");
            out.write("\n");
        }
        out.write("\t\tirs.close();\n");
        out.write("\t\tstmt.close();\n");
        
        if (autoIncrementAttribute != null) {
        	out.write("\n");
        	out.write("\t\tlog.debug(\"generating new " + theEntity.getLabel() + " \" + " + autoIncrementAttribute.getLabel()+ ");\n");
        	out.write("\n");
        }
        
        out.write("\t\tfreeConnection();\n");
        out.write("\t}\n");
    }
    
    private void generateSettersGetters(Entity theEntity, BufferedWriter out, boolean keysOnly, boolean parentTag) throws IOException {
        for (int i = 0; i < theEntity.getAttributes().size(); i++) {
            Attribute theAttribute = theEntity.getAttributes().elementAt(i);
            
            if (keysOnly && !theAttribute.isPrimary())
                continue;
            generateSetterGetter(theAttribute.getType(), theAttribute.getLabel(), theAttribute.getDomain(), theAttribute.isPrimary(), parentTag, out);
            if (theAttribute.isDateTime() || theAttribute.isTimestamp())
                generateSetterNow(theAttribute, out);
        }
    }
    
    private void generateSetterGetter(String type, String label, boolean parentTag, BufferedWriter out) throws IOException {
    	generateSetterGetter(type, label, null, true, parentTag, out);
    }
    
    private void generateSetterGetter(String type, String label, Domain domain, boolean primary, boolean parentTag, BufferedWriter out) throws IOException {
        // generate getter method
        out.write("\n\tpublic " + (domain == null ? type : domain.getJavaType()) + " get" + Character.toUpperCase(label.charAt(0))
                + label.substring(1) + " () {\n");
        if (parentTag && type.equals("String")) {
            out.write("\t\tif (commitNeeded)\n");
            out.write("\t\t\treturn \"\";\n");
            out.write("\t\telse\n");
            out.write("\t\t\treturn " + label + ";\n");
        } else
            out.write("\t\treturn " + label + ";\n");
        out.write("\t}\n");

        // generate setter method
        out.write("\n\tpublic void set" + Character.toUpperCase(label.charAt(0))
                + label.substring(1) + " ("+ (domain == null ? type : domain.getJavaType()) + " " + label
                + ") {\n");
        out.write("\t\tthis." + label + " = " + label + ";\n");
        if (!primary)
            out.write("\t\tcommitNeeded = true;\n");
        out.write("\t}\n");    	

        // generate look-aside method for actual value
        out.write("\n\tpublic " + (domain == null ? type : domain.getJavaType()) + " getActual" + Character.toUpperCase(label.charAt(0))
                + label.substring(1) + " () {\n");
        out.write("\t\treturn " + label + ";\n");
        out.write("\t}\n");
}
    
    private void generateSetterNow(Attribute attribute, BufferedWriter out) throws IOException {
        // generate setter method for current time
        out.write("\n\tpublic void set" + Character.toUpperCase(attribute.getLabel().charAt(0))
                + attribute.getLabel().substring(1) + "ToNow ( ) {\n");
        if (attribute.isDateTime())
        		out.write("\t\tthis." + attribute.getLabel() + " = new java.util.Date();\n");
        else
    		out.write("\t\tthis." + attribute.getLabel() + " = new java.sql.Timestamp(new java.util.Date().getTime());\n");
        if (!attribute.isPrimary())
            out.write("\t\tcommitNeeded = true;\n");
        out.write("\t}\n");     
    }
    
    private void generateFunctionGetters(Entity theEntity, BufferedWriter out) throws IOException {
        for (int i = 0; i < theEntity.getAttributes().size(); i++) {
            Attribute theAttribute = theEntity.getAttributes().elementAt(i);
            generateFunctionGetter(theEntity, theAttribute, out);
        }
    }
    
    private void generateFunctionGetter(Entity theEntity, Attribute theAttribute, BufferedWriter out) throws IOException {
        // generate getter method
        out.write("\n\tpublic static " + staticType((theAttribute.getDomain() == null ? theAttribute.getType() : theAttribute.getDomain().getJavaType())) + " " + theAttribute.getLabel() + "Value() throws JspException {\n");
        out.write("\t\ttry {\n");
        out.write("\t\t\treturn currentInstance.get" + Character.toUpperCase(theAttribute.getLabel().charAt(0)) + theAttribute.getLabel().substring(1) + "();\n");
        out.write("\t\t} catch (Exception e) {\n");
        out.write("\t\t\t throw new JspTagException(\"Error in tag function " + theAttribute.getLabel() + "Value()\");\n");
        out.write("\t\t}\n");
        out.write("\t}\n");
    }
    
    private String staticType(String type){
    	if("int".equalsIgnoreCase(type)){
    		return "Integer";
    	}else if("long".equalsIgnoreCase(type)){
    		return "Long";
    	}else if("boolean".equalsIgnoreCase(type)){
    		return "Boolean";
    	}else if("float".equalsIgnoreCase(type)){
    		return "Float";
    	}else if("double".equalsIgnoreCase(type)){
    		return "Double";
    	}
    	return type;
    }
    
    private void generateServiceStateReset(Entity theEntity, BufferedWriter out) throws IOException {
        generateServiceStateReset(theEntity, out, true, false);
    }
    
    private void generateServiceStateReset(Entity theEntity, BufferedWriter out, boolean isMainTag, boolean isUploadTag) throws IOException {
        out.write("\n\tprivate void clearServiceState () {\n");
        for (int i = 0; i < theEntity.getAttributes().size(); i++) {
            Attribute theAttribute = theEntity.getAttributes().elementAt(i);
            out.write("\t\t" + theAttribute.getLabel() + " = " + theAttribute.getInitializer() + ";\n");
        }
        
        if (isMainTag) {
            out.write("\t\tnewRecord = false;\n");
            out.write("\t\tcommitNeeded = false;\n");
            out.write("\t\tparentEntities = new Vector<" + projectName + "TagSupport>();\n");
            if(!isUploadTag){
            	out.write("\t\tthis.var = null;\n\n");
            }
        }
        out.write("\t}\n");
    }
    
    private void generateEntityIteratorClass(Entity theEntity) throws IOException {
        if ((theEntity.getPrimaryKeyAttributes() == null || theEntity.getPrimaryKeyAttributes().size() == 0) && (theEntity.getSubKeyAttributes() == null || theEntity.getSubKeyAttributes().size() == 0))
            return;

        Vector<Attribute> primaryKeys = theEntity.getPrimaryKeyAttributes();
        Vector<Attribute> subKeys = theEntity.getSubKeyAttributes();
        Attribute keyAttribute = (subKeys.size() == 0 ? theEntity.getPrimaryKeyAttributes().firstElement() : subKeys.firstElement());
        File baseClassFile = new File(tagDirectory, "/" + theEntity.getUnqualifiedLabel() + "Iterator.java");
        FileWriter fstream = new FileWriter(baseClassFile);
        BufferedWriter out = new BufferedWriter(fstream);
        out.write("package " + packagePrefix + "." + theEntity.getUnqualifiedLowerLabel() + ";\n"
                + "\n"
                + "\n"
                + "import java.sql.PreparedStatement;\n"
                + "import java.sql.ResultSet;\n"
                + "import java.sql.SQLException;\n"
                + "import java.util.Vector;\n");
        out.write("import org.apache.logging.log4j.Logger;\n");
        out.write("import org.apache.logging.log4j.LogManager;\n");
        if (theEntity.hasPrimaryDateTime() || theEntity.hasForeignDateTime())
            out.write("import java.util.Date;\n");
        if (theEntity.hasPrimaryTimestamp() || theEntity.hasForeignTimestamp())
            out.write("import java.sql.Timestamp;\n");
//        if (theEntity.hasImage())
//            out.write("import java.awt.Image;\n");
        out.write("\n"
                + "import javax.servlet.jsp.JspException;\n"
                + "import javax.servlet.jsp.JspTagException;\n"
                + "import javax.servlet.jsp.tagext.Tag;\n"
                + "\n"
                + "import " + packagePrefix + "." + projectName + "TagSupport;\n"
                + "import " + packagePrefix + "." + projectName + "BodyTagSupport;\n");
        generateParentImports(out, theEntity);
        out.write("\n"
                + "@SuppressWarnings(\"serial\")" 
        		+ "\n"
                + "public class " + theEntity.getUnqualifiedLabel() + "Iterator extends " + projectName + "BodyTagSupport {\n");
        for (Attribute theKey : primaryKeys) {
            out.write("    " + theKey.getType() + " " + theKey.getLabel() + " = " + theKey.getInitializer() + ";\n");
        }
        for (Attribute theKey : theEntity.attributes) {
            if (theKey.isForeign() && !theKey.isPrimary())
            	out.write("    " + theKey.getType() + " " + theKey.getLabel() + " = " + theKey.getInitializer() + ";\n");
        }
        out.write("\tVector<" + projectName + "TagSupport> parentEntities = new Vector<" + projectName + "TagSupport>();\n\n");
        
        out.write("\tprivate static final Logger log = LogManager.getLogger("+theEntity.getUnqualifiedLabel() +"Iterator.class);\n\n");
        
        out.write("\n    PreparedStatement stat = null;\n"
                + "    ResultSet rs = null;\n"
                + "    String fromList = null;\n"
                + "    String joinCriteria = null;\n"
                + "    String filterCriteria = null;\n"
                + "    String sortCriteria = null;\n"
                + "    int limitCriteria = 0;\n"
                + "    String var = null;\n"
                + "    int rsCount = 0;\n"
                + "\n");
        
        // string list to avoid duplicate variables and functions
        ArrayList<String> entityList = new ArrayList<String>();
        if (theEntity.getParents().size() > 1) {
            for (int i = 0; i < theEntity.getParents().size(); i++){
                Entity parent = theEntity.getParents().elementAt(i).getSourceEntity();
                if( !entityList.contains(parent.getUpperLabel()) ){
                	out.write("   boolean use" + parent.getUpperLabel() + " = false;\n");
                	entityList.add(parent.getUpperLabel());
                }
            }
            out.write("\n");
        }
        
        if (theEntity.getParents().size() == 0) {
            out.write("\tpublic static String " + theEntity.getLowerLabel() + "Count() throws JspTagException {\n");
            out.write("\t\tint count = 0;\n");
            out.write("\t\t" + theEntity.getUnqualifiedLabel() + "Iterator theIterator = new " + theEntity.getUnqualifiedLabel() + "Iterator();\n");
            out.write("\t\ttry {\n"
                    + "\t\t\tPreparedStatement stat = theIterator.getConnection().prepareStatement(\"SELECT count(*)");
            out.write(" from " + theSchema.getSqlLabel() + "." + theEntity.getSqlLabel() + "\"\n");
            out.write("\t\t\t\t\t\t);\n\n");
            out.write("\t\t\tResultSet crs = stat.executeQuery();\n"
                    + "\n"
                    + "\t\t\tif (crs.next()) {\n");
            out.write("\t\t\t\tcount = crs.getInt(1);\n"
                    + "\t\t\t}\n"
                    + "\t\t\tcrs.close();\n"
                    + "\t\t\tstat.close();\n"
                    + "\t\t} catch (SQLException e) {\n"
                    + "\t\t\tlog.error(\"JDBC error generating " + theEntity.getLabel() + " iterator\", e);\n"
                    + "\t\t\tthrow new JspTagException(\"Error: JDBC error generating " + theEntity.getLabel() + " iterator\");\n"
                    + "\t\t} finally {\n"
                    + "\t\t\ttheIterator.freeConnection();\n"
                    + "\t\t}\n");
            out.write("\t\treturn \"\" + count;\n");
            out.write("\t}\n\n");
        }
        
        // string list to avoid duplicate variables and functions
        entityList = new ArrayList<String>();
        
        for (Relationship relationship : theEntity.getParents()) {
            Entity parentEntity = relationship.getSourceEntity();
            StringBuffer keyBuffer = new StringBuffer();
            StringBuffer booleanBuffer = new StringBuffer();
            
            if( !entityList.contains(theEntity.getLowerLabel() + "CountBy" + parentEntity.getLabel()) ){
            	out.write("\tpublic static String " + theEntity.getLowerLabel() + "CountBy" + parentEntity.getLabel() + "(");
            	for (int j = 0; j < parentEntity.getPrimaryKeyAttributes().size(); j++) {
            		Attribute theAttribute = parentEntity.getPrimaryKeyAttributes().elementAt(j);
            		out.write((j > 0 ? ", " : "") + "String " + theAttribute.getLabel());
            		keyBuffer.append("\t\t\tstat." + theAttribute.getSQLMethod(false) + "(" + (j+1) + "," + theAttribute.parseValue() + ");\n");
            	}
            	out.write(") throws JspTagException {\n");
            	out.write("\t\tint count = 0;\n");
            	out.write("\t\t" + theEntity.getUnqualifiedLabel() + "Iterator theIterator = new " + theEntity.getUnqualifiedLabel() + "Iterator();\n");
            	out.write("\t\ttry {\n"
            			+ "\t\t\tPreparedStatement stat = theIterator.getConnection().prepareStatement(\"SELECT count(*)");
            	out.write(" from " + theSchema.getSqlLabel() + "." + theEntity.getSqlLabel() + " where 1=1\"\n");
            	for (String source : relationship.getSourceAttributes()) {
            		out.write("\t\t\t\t\t\t+ \" and " + relationship.getTargetAttribute(source) + " = ?\"\n");            		
            	}
            	out.write("\t\t\t\t\t\t);\n\n");
            	out.write(keyBuffer.toString());
            	out.write("\t\t\tResultSet crs = stat.executeQuery();\n"
            			+ "\n"
            			+ "\t\t\tif (crs.next()) {\n");
            	out.write("\t\t\t\tcount = crs.getInt(1);\n"
            			+ "\t\t\t}\n"
            			+ "\t\t\tcrs.close();\n"
            			+ "\t\t\tstat.close();\n"
            			+ "\t\t} catch (SQLException e) {\n"
            			+ "\t\t\tlog.error(\"JDBC error generating " + theEntity.getLabel() + " iterator\", e);\n"
            			+ "\t\t\tthrow new JspTagException(\"Error: JDBC error generating " + theEntity.getLabel() + " iterator\");\n"
            			+ "\t\t} finally {\n"
            			+ "\t\t\ttheIterator.freeConnection();\n"
            			+ "\t\t}\n");
            	out.write("\t\treturn \"\" + count;\n");
            	out.write("\t}\n\n");
            	
            	entityList.add(theEntity.getLowerLabel() + "CountBy" + parentEntity.getLabel());
            }
            
            if( !entityList.contains(parentEntity.getLowerLabel()+ "Has" + theEntity.getLabel()) ){
            	out.write("\tpublic static Boolean " + parentEntity.getLowerLabel()+ "Has" + theEntity.getLabel() + "(");
            	for (int j = 0; j < parentEntity.getPrimaryKeyAttributes().size(); j++) {
            		Attribute theAttribute = parentEntity.getPrimaryKeyAttributes().elementAt(j);
            		out.write((j > 0 ? ", " : "") + "String " + theAttribute.getLabel());
            		booleanBuffer.append((j > 0 ? ", " : "") + theAttribute.getLabel());
            	}
            	out.write(") throws JspTagException {\n");
            	out.write("\t\treturn ! " + theEntity.getLowerLabel() + "CountBy" + parentEntity.getLabel() + "(" + booleanBuffer.toString() + ").equals(\"0\");\n");
            	out.write("\t}\n\n");
            	entityList.add(parentEntity.getLowerLabel()+ "Has" + theEntity.getLabel());
            }
        }
        
            StringBuffer keyBuff = new StringBuffer();
            int attrCnt = 0;
            out.write("\tpublic static Boolean " + theEntity.getLowerLabel() + "Exists (");
            for (int j = 0; j < theEntity.getPrimaryKeyAttributes().size(); j++) {
                Attribute theAttribute = theEntity.getPrimaryKeyAttributes().elementAt(j);
                out.write((attrCnt++ > 0 ? ", " : "") + "String " + theAttribute.getLabel());
                keyBuff.append("\t\t\tstat." + theAttribute.getSQLMethod(false) + "(" + (attrCnt) + "," + theAttribute.parseValue() + ");\n");
            }
            out.write(") throws JspTagException {\n");
            out.write("\t\tint count = 0;\n");
            out.write("\t\t" + theEntity.getUnqualifiedLabel() + "Iterator theIterator = new " + theEntity.getUnqualifiedLabel() + "Iterator();\n");
            out.write("\t\ttry {\n"
                    + "\t\t\tPreparedStatement stat = theIterator.getConnection().prepareStatement(\"SELECT count(*)");
            out.write(" from " + theSchema.getSqlLabel() + "." + theEntity.getSqlLabel() + " where 1=1\"\n");
            for (int j = 0; j < theEntity.getPrimaryKeyAttributes().size(); j++) {
                Attribute theAttribute = theEntity.getPrimaryKeyAttributes().elementAt(j);
                out.write("\t\t\t\t\t\t+ \" and " + theAttribute.getSqlLabel() + " = ?\"\n");
            }
            out.write("\t\t\t\t\t\t);\n\n");
            out.write(keyBuff.toString());
            out.write("\t\t\tResultSet crs = stat.executeQuery();\n"
                    + "\n"
                    + "\t\t\tif (crs.next()) {\n");
            out.write("\t\t\t\tcount = crs.getInt(1);\n"
                    + "\t\t\t}\n"
                    + "\t\t\tcrs.close();\n"
                    + "\t\t\tstat.close();\n"
                    + "\t\t} catch (SQLException e) {\n"
                    + "\t\t\tlog.error(\"JDBC error generating " + theEntity.getLabel() + " iterator\", e);\n"
                    + "\t\t\tthrow new JspTagException(\"Error: JDBC error generating " + theEntity.getLabel() + " iterator\");\n"
                    + "\t\t} finally {\n"
                    + "\t\t\ttheIterator.freeConnection();\n"
                    + "\t\t}\n");
            out.write("\t\treturn count > 0;\n");
            out.write("\t}\n\n");
        if (theEntity.getParents().size() > 1) {
            for (int i = 0; i < theEntity.getParents().size() - 1; i++) {
                Entity parentEntityOne = theEntity.getParents().elementAt(i).getSourceEntity();
                Entity parentEntityTwo = theEntity.getParents().elementAt(i + 1).getSourceEntity();
                StringBuffer keyBuffer = new StringBuffer();
                int attrCount = 0;
                out.write("\tpublic static Boolean " + parentEntityOne.getLowerLabel() + parentEntityTwo.getLabel() + "Exists (");
                for (int j = 0; j < parentEntityOne.getPrimaryKeyAttributes().size(); j++) {
                    Attribute theAttribute = parentEntityOne.getPrimaryKeyAttributes().elementAt(j);
                    out.write((attrCount++ > 0 ? ", " : "") + "String " + theAttribute.getLabel());
                    keyBuffer.append("\t\t\tstat." + theAttribute.getSQLMethod(false) + "(" + (attrCount) + "," + theAttribute.parseValue() + ");\n");
                }
                for (int j = 0; j < parentEntityTwo.getPrimaryKeyAttributes().size(); j++) {
                    Attribute theAttribute = parentEntityTwo.getPrimaryKeyAttributes().elementAt(j);
                    if (parentEntityOne.getAttributeByLabel(theAttribute.getLabel()) != null)
                        continue;
                    out.write((attrCount++ > 0 ? ", " : "") + "String " + theAttribute.getLabel());
                    keyBuffer.append("\t\t\tstat." + theAttribute.getSQLMethod(false) + "(" + (attrCount) + "," + theAttribute.parseValue() + ");\n");
                }
                out.write(") throws JspTagException {\n");
                out.write("\t\tint count = 0;\n");
                out.write("\t\t" + theEntity.getUnqualifiedLabel() + "Iterator theIterator = new " + theEntity.getUnqualifiedLabel() + "Iterator();\n");
                out.write("\t\ttry {\n"
                        + "\t\t\tPreparedStatement stat = theIterator.getConnection().prepareStatement(\"SELECT count(*)");
                out.write(" from " + theSchema.getSqlLabel() + "." + theEntity.getSqlLabel() + " where 1=1\"\n");
                for (int j = 0; j < parentEntityOne.getPrimaryKeyAttributes().size(); j++) {
                    Attribute theAttribute = parentEntityOne.getPrimaryKeyAttributes().elementAt(j);
                    out.write("\t\t\t\t\t\t+ \" and " + theAttribute.getSqlLabel() + " = ?\"\n");
                }
                for (int j = 0; j < parentEntityTwo.getPrimaryKeyAttributes().size(); j++) {
                    Attribute theAttribute = parentEntityTwo.getPrimaryKeyAttributes().elementAt(j);
                    if (parentEntityOne.getAttributeByLabel(theAttribute.getLabel()) != null)
                        continue;
                    out.write("\t\t\t\t\t\t+ \" and " + theAttribute.getSqlLabel() + " = ?\"\n");
                }
                out.write("\t\t\t\t\t\t);\n\n");
                out.write(keyBuffer.toString());
                out.write("\t\t\tResultSet crs = stat.executeQuery();\n"
                        + "\n"
                        + "\t\t\tif (crs.next()) {\n");
                out.write("\t\t\t\tcount = crs.getInt(1);\n"
                        + "\t\t\t}\n"
                        + "\t\t\tcrs.close();\n"
                        + "\t\t\tstat.close();\n"
                        + "\t\t} catch (SQLException e) {\n"
                        + "\t\t\tlog.error(\"JDBC error generating " + theEntity.getLabel() + " iterator\", e);\n"
                        + "\t\t\tthrow new JspTagException(\"Error: JDBC error generating " + theEntity.getLabel() + " iterator\");\n"
                        + "\t\t} finally {\n"
                        + "\t\t\ttheIterator.freeConnection();\n"
                        + "\t\t}\n");
                out.write("\t\treturn count > 0;\n");
                out.write("\t}\n\n");
            }            
        }
        
        out.write("    public int doStartTag() throws JspException {\n");
        generateParentEntityReferences(out, theEntity, "\t\t");
        out.write("\n      try {\n");
        
        /*
         * Create count query
         * 
         */
        out.write("            //run count query  \n");
        if (theEntity.hasForeign())
        	out.write("            int webapp_keySeq = 1;\n");
        out.write("            stat = getConnection().prepareStatement(\"SELECT count(*)");
        out.write(" from \" + generateFromClause() + \" where 1=1\"\n");
        out.write("                                                        + generateJoinCriteria()\n");
        out.write("                                                        + generateFilterCriteria()\n");
        
        StringBuffer queryBuffer = new StringBuffer();
//        for (int i = 0; i < parentKeys.size(); i++) {
//            Attribute theAttribute = theEntity.getAttributeBySQLLabel(theEntity.getForeignReferencedAttribute(parentKeys.elementAt(i).getSqlLabel()));
//            out.write("                                                        + (" + theAttribute.getLabel() + " == " + theAttribute.getInitializer() + " ? \"\" : \" and " + theAttribute.getSqlLabel() + " = ?\")\n");
//            queryBuffer.append("            if (" + theAttribute.getLabel() + " != " + theAttribute.getInitializer() + ") stat."
//                    + theAttribute.getSQLMethod(false)
//                    + "(webapp_keySeq++, "
//                    + theAttribute.getLabel()
//                    + (theAttribute.isDateTime() ? " == null ? null : new java.sql."
//                            + (theAttribute.isTime() ? "Timestamp" : "Date") + "(" + theAttribute.getLabel() + ".getTime())"
//                            : "") + ");\n");
//        }
        
        for (int i = 0; i < theEntity.getParents().size(); i++) {
        	
            Relationship theRelationship = theEntity.getParents().elementAt(i);
            Entity theSourceEntity = theRelationship.getSourceEntity();
            
            for (int j = 0; j < theSourceEntity.getPrimaryKeyAttributes().size(); j++) {
            	
            	Attribute theAttribute = theEntity.getAttributeBySQLLabel(theRelationship.getForeignReferencedAttribute(theSourceEntity.getPrimaryKeyAttributes().elementAt(j).getSqlLabel()));
            	
            	out.write("                                                        + (" + theAttribute.getLabel() + " == " + theAttribute.getInitializer() + " ? \"\" : \" and " + theAttribute.getSqlLabel() + " = ?\")\n");
            	
            	queryBuffer.append("            if (" + theAttribute.getLabel() + " != " + theAttribute.getInitializer() + ") stat."
                        + theAttribute.getSQLMethod(false)
                        + "(webapp_keySeq++, "
                        + theAttribute.getLabel()
                        + (theAttribute.isDateTime() ? " == null ? null : new java.sql."
                                + (theAttribute.isTimestamp() ? "Timestamp" : "Date") + "(" + theAttribute.getLabel() + ".getTime())"
                                : "") + ");\n");
            }
            
        }
        
        if(!"sqlserver".equals(databaseType)){
        	out.write("                                                        + generateLimitCriteria());\n");
        }else{
        	out.write("															);\n");
        }
        
        out.write(queryBuffer.toString());
        out.write("            rs = stat.executeQuery();\n"
                + "\n"
                + "            if (rs.next()) {\n");
//        for (int i = 0; i < primaryKeys.size(); i++) {
//            Attribute theKey = primaryKeys.elementAt(i);
//            out.write("                " + theKey.getLabel() + " = rs." + theKey.getSQLMethod(true) + "(" + (i+1) + ");\n");
//        }
        out.write("                pageContext.setAttribute(var+\"Total\", rs.getInt(1));\n"
                + "            }\n\n\n");
        
        out.write("            //run select id query  \n");
        /*
         * Create select id query
         */
        if (theEntity.hasForeign())
        	out.write("            webapp_keySeq = 1;\n");
        out.write("            stat = getConnection().prepareStatement(\"SELECT ");
        
        if("sqlserver".equals(databaseType)){
        	out.write(" \" + generateLimitCriteria() + \" ");
        }
        
        for (int i = 0; i < primaryKeys.size(); i++) {
            Attribute theKey = primaryKeys.elementAt(i);
            out.write((i == 0 ? "" : ", ") + theSchema.getSqlLabel() + "." + theEntity.getSqlLabel() + "." + theKey.getSqlLabel());
        }
        out.write(" from \" + generateFromClause() + \" where 1=1\"\n");
        out.write("                                                        + generateJoinCriteria()\n");
        out.write("                                                        + generateFilterCriteria()\n");
        queryBuffer = new StringBuffer();
        
        for (int i = 0; i < theEntity.getParents().size(); i++) {
        	
            Relationship theRelationship = theEntity.getParents().elementAt(i);
            Entity theSourceEntity = theRelationship.getSourceEntity();
            
            for (int j = 0; j < theSourceEntity.getPrimaryKeyAttributes().size(); j++) {
            	
            	Attribute theAttribute = theEntity.getAttributeBySQLLabel(theRelationship.getForeignReferencedAttribute(theSourceEntity.getPrimaryKeyAttributes().elementAt(j).getSqlLabel()));
            	
            	out.write("                                                        + (" + theAttribute.getLabel() + " == " + theAttribute.getInitializer() + " ? \"\" : \" and " + theAttribute.getSqlLabel() + " = ?\")\n");
            	
            	queryBuffer.append("            if (" + theAttribute.getLabel() + " != " + theAttribute.getInitializer() + ") stat."
                        + theAttribute.getSQLMethod(false)
                        + "(webapp_keySeq++, "
                        + theAttribute.getLabel()
                        + (theAttribute.isDateTime() ? " == null ? null : new java.sql."
                                + (theAttribute.isTimestamp() ? "Timestamp" : "Date") + "(" + theAttribute.getLabel() + ".getTime())"
                                : "") + ");\n");
            }
            
        }
        
        out.write("                                                        + \" order by \" + generateSortCriteria() ");
        
        if(!"sqlserver".equals(databaseType)){
        	out.write(" +  generateLimitCriteria());\n");
        }else{
        	out.write(");\n");
        }
        
        out.write(queryBuffer.toString());
        out.write("            rs = stat.executeQuery();\n"
                + "\n"
                + "            if ( rs != null && rs.next() ) {\n");
        for (int i = 0; i < primaryKeys.size(); i++) {
            Attribute theKey = primaryKeys.elementAt(i);
            out.write("                " + theKey.getLabel() + " = rs." + theKey.getSQLMethod(true) + "(" + (i+1) + ");\n");
        }
        out.write("                if (var != null) {\n"
                + "                    pageContext.setAttribute(var, this);\n"
                + "                    pageContext.setAttribute(var+\"Count\", ++rsCount);\n"
                + "                }\n"
                + "                return EVAL_BODY_INCLUDE;\n"
                + "            }\n");
        
        out.write("        } catch (SQLException e) {\n");
        out.write("            log.error(\"JDBC error generating " + theEntity.getLabel() + " iterator: \" + stat, e);\n\n");
        
        out.write("\t\t\tfreeConnection();\n");
        out.write("\t\t\tclearServiceState();\n\n");
        
        out.write("\t\t\tTag parent = getParent();\n");
        out.write("\t\t\tif(parent != null){\n");
        out.write("\t\t\t\tpageContext.setAttribute(\"tagError\", true);\n");
        out.write("\t\t\t\tpageContext.setAttribute(\"tagErrorException\", e);\n");
        out.write("\t\t\t\tpageContext.setAttribute(\"tagErrorMessage\", \"Error: JDBC error generating " + theEntity.getLabel() + " iterator: \" + stat);\n");
        out.write("\t\t\t\treturn parent.doEndTag();\n");
        out.write("\t\t\t}else{\n");
        out.write("\t\t\t\tthrow new JspException(\"Error: JDBC error generating " + theEntity.getLabel() + " iterator: \" + stat,e);\n");
        out.write("\t\t\t}\n\n");
        out.write("        }\n");
        out.write("\n");
        out.write("        return SKIP_BODY;\n");
        out.write("    }\n");
        out.write("\n");

        out.write("    private String generateFromClause() {\n"
                + "       StringBuffer theBuffer = new StringBuffer(\"" + theSchema.getSqlLabel() + "." + theEntity.getSqlLabel() + "\");\n");
        if (theEntity.getParents().size() > 1) {
            for (int i = 0; i < theEntity.getParents().size(); i++){
                Entity parent = theEntity.getParents().elementAt(i).getSourceEntity();
                out.write("       if (use" + parent.getUpperLabel() + ")\n");
                out.write("          theBuffer.append(\", " + theSchema.getSqlLabel() + "." + parent.getSqlLabel() + "\");\n");
            }
            out.write("\n");
        }
        out.write("      return theBuffer.toString();\n"
                + "    }\n"
                + "\n");

        out.write("    private String generateJoinCriteria() {\n"
                + "       StringBuffer theBuffer = new StringBuffer();\n");
        if (theEntity.getParents().size() > 1) {
            for (Relationship relationship : theEntity.getParents()) {
            	for (String source : relationship.getSourceAttributes()) {
            		String target = relationship.getTargetAttribute(source);
                	out.write("       if (use" + relationship.getSourceEntity().getUpperLabel() + ")\n");
                    out.write("          theBuffer.append(\" and " + relationship.getSourceEntity().getSchema().getSqlLabel() + "." + relationship.getSourceEntity().getSqlLabel() + "." + source
                    										+ " = " + relationship.getTargetEntity().getSchema().getSqlLabel() + "." + relationship.getTargetEntity().getSqlLabel() + "." + target + "\");\n");
            	}
            }
            out.write("\n");
        }
        out.write("      return theBuffer.toString();\n"
                + "    }\n"
                + "\n");

        out.write("    private String generateFilterCriteria() {\n"
                + "        if (filterCriteria != null) {\n"
                + "            return \" and \" + filterCriteria;\n"
                + "        } else {\n"
                + "            return \"\";\n"
                + "        }\n"
                + "    }\n"
                + "\n");
        
        out.write("    private String generateSortCriteria() {\n"
                + "        if (sortCriteria != null) {\n"
                + "            return sortCriteria;\n"
                + "        } else {\n"
                + "            return \"");
        for (int i = 0; i < primaryKeys.size(); i++) {
            Attribute theKey = primaryKeys.elementAt(i);
            
            out.write((i > 0 ? "," : "") + theKey.getSqlLabel());
        }
        out.write("\";\n"
                + "        }\n"
                + "    }\n"
                + "\n");
        
        out.write("    private String generateLimitCriteria() {\n");
        out.write("        if (limitCriteria > 0) {\n");
        if( "sqlserver".equals(databaseType) ){
        	out.write("            return \" top(\" + limitCriteria + \")\";\n");
        }else{
        	out.write("            return \" limit \" + limitCriteria;\n");
        }
        out.write("        } else {\n");
        out.write("            return \"\";\n");
        out.write("        }\n");
        out.write("    }\n");
        out.write("\n");
                
        out.write("    public int doAfterBody() throws JspException {\n"
                + "        try {\n"
                + "            if ( rs != null && rs.next() ) {\n");
        for (int i = 0; i < primaryKeys.size(); i++) {
            Attribute theKey = primaryKeys.elementAt(i);
            out.write("                " + theKey.getLabel() + " = rs." + theKey.getSQLMethod(true) + "(" + (i+1) + ");\n");
        }
        out.write("                pageContext.setAttribute(var+\"Count\", ++rsCount);\n");
        out.write("                return EVAL_BODY_AGAIN;\n");
        out.write("            }\n");
        out.write("        } catch (SQLException e) {\n");
        out.write("            log.error(\"JDBC error iterating across " + theEntity.getLabel() + "\", e);\n\n");
        
        out.write("\t\t\tfreeConnection();\n");
        out.write("\t\t\tclearServiceState();\n\n");
        
        out.write("\t\t\tTag parent = getParent();\n");
        out.write("\t\t\tif(parent != null){\n");
        out.write("\t\t\t\tpageContext.setAttribute(\"tagError\", true);\n");
        out.write("\t\t\t\tpageContext.setAttribute(\"tagErrorException\", e);\n");
        out.write("\t\t\t\tpageContext.setAttribute(\"tagErrorMessage\", \"JDBC error iterating across " + theEntity.getLabel() + "\" + stat.toString());\n");
        out.write("\t\t\t\treturn parent.doEndTag();\n");
        out.write("\t\t\t}else{\n");
        out.write("\t\t\t\tthrow new JspException(\"JDBC error iterating across " + theEntity.getLabel() + "\",e);\n");
        out.write("\t\t\t}\n\n");
        
        out.write("        }\n");
        out.write("        return SKIP_BODY;\n");
        out.write("    }\n");
        out.write("\n");
        
        
        
        
        
        out.write("    public int doEndTag() throws JspTagException, JspException {\n");
        out.write("        try {\n");
        
        out.write("\t\t\tif( var != null )\n");
        out.write("\t\t\t\tpageContext.removeAttribute(var);\n");
        out.write("\t\t\tif( pageContext != null ){\n");
        out.write("\t\t\t\tBoolean error = (Boolean) pageContext.getAttribute(\"tagError\");\n");
        out.write("\t\t\t\tif( error != null && error ){\n\n");
        
        out.write("\t\t\t\t\tfreeConnection();\n");
        out.write("\t\t\t\t\tclearServiceState();\n\n");
        
        out.write("\t\t\t\t\tException e = null; // (Exception) pageContext.getAttribute(\"tagErrorException\");\n");
        out.write("\t\t\t\t\tString message = null; // (String) pageContext.getAttribute(\"tagErrorMessage\");\n\n");
        
        out.write("\t\t\t\t\tif(pageContext != null){\n");
        out.write("\t\t\t\t\t\te = (Exception) pageContext.getAttribute(\"tagErrorException\");\n");
        out.write("\t\t\t\t\t\tmessage = (String) pageContext.getAttribute(\"tagErrorMessage\");\n\n");
        out.write("\t\t\t\t\t}\n");
        
        out.write("\t\t\t\t\tTag parent = getParent();\n");
        out.write("\t\t\t\t\tif(parent != null){\n");
        out.write("\t\t\t\t\t\treturn parent.doEndTag();\n");
        
        out.write("\t\t\t\t\t}else if(e != null && message != null){\n");
        out.write("\t\t\t\t\t\tthrow new JspException(message,e);\n");
        out.write("\t\t\t\t\t}else if(parent == null && pageContext != null){\n");
        out.write("\t\t\t\t\t\tpageContext.removeAttribute(\"tagError\");\n");
        out.write("\t\t\t\t\t\tpageContext.removeAttribute(\"tagErrorException\");\n");
        out.write("\t\t\t\t\t\tpageContext.removeAttribute(\"tagErrorMessage\");\n");
        out.write("\t\t\t\t\t}\n");
        out.write("\t\t\t\t}\n");
        out.write("\t\t\t}\n\n");
        
        out.write("            if( rs != null ){\n");
        out.write("                rs.close();\n");
        out.write("            }\n\n");
        
        out.write("            if( stat != null ){\n");
        out.write("                stat.close();\n");
        out.write("            }\n\n");
        
        out.write("        } catch ( SQLException e ) {\n");
        
        out.write("            log.error(\"JDBC error ending " + theEntity.getLabel() + " iterator\",e);\n");
        
        out.write("\t\t\tfreeConnection();\n\n");
        out.write("\t\t\tTag parent = getParent();\n");
        out.write("\t\t\tif(parent != null){\n");
        out.write("\t\t\t\tpageContext.setAttribute(\"tagError\", true);\n");
        out.write("\t\t\t\tpageContext.setAttribute(\"tagErrorException\", e);\n");
        out.write("\t\t\t\tpageContext.setAttribute(\"tagErrorMessage\", \"JDBC error retrieving " + keyAttribute.getLabel() + " \" + " + keyAttribute.getLabel() + ");\n");
        out.write("\t\t\t\treturn parent.doEndTag();\n");
        out.write("\t\t\t}else{\n");
        out.write("\t\t\t\tthrow new JspException(\"Error: JDBC error ending " + theEntity.getLabel() + " iterator\",e);\n");
        out.write("\t\t\t}\n\n");
        
        
        out.write("        } finally {\n");
        out.write("            clearServiceState();\n");
        out.write("            freeConnection();\n");
        out.write("        }\n");
        out.write("        return super.doEndTag();\n");
        out.write("    }\n");
        out.write("\n");
        out.write("    private void clearServiceState() {\n");
        
        for (int i = 0; i < primaryKeys.size(); i++) {
            Attribute theKey = primaryKeys.elementAt(i);
            out.write("        " + theKey.getLabel() + " = " + theKey.getInitializer() + ";\n");
        }
        out.write("        parentEntities = new Vector<" + projectName + "TagSupport>();\n\n");
        out.write("        this.rs = null;\n"
                + "        this.stat = null;\n"
                + "        this.sortCriteria = null;\n"
                + "        this.var = null;\n"
                + "        this.rsCount = 0;\n"
                + "    }\n"
                + "\n"
                + "    public String getFromList() {\n"
                + "        return fromList;\n"
                + "    }\n"
                + "\n"
                + "    public void setFromList(String fromList) {\n"
                + "        this.fromList = fromList;\n"
                + "    }\n"
                + "\n"
                + "    public String getJoinCriteria() {\n"
                + "        return joinCriteria;\n"
                + "    }\n"
                + "\n"
                + "    public void setJoinCriteria(String joinCriteria) {\n"
                + "        this.joinCriteria = joinCriteria;\n"
                + "    }\n"
                + "\n"
                + "    public String getFilterCriteria() {\n"
                + "        return filterCriteria;\n"
                + "    }\n"
                + "\n"
                + "    public void setFilterCriteria(String filterCriteria) {\n"
                + "        this.filterCriteria = filterCriteria;\n"
                + "    }\n"
                + "\n"
                + "    public String getSortCriteria() {\n"
                + "        return sortCriteria;\n"
                + "    }\n"
                + "\n"
                + "    public void setSortCriteria(String sortCriteria) {\n"
                + "        this.sortCriteria = sortCriteria;\n"
                + "    }\n"
                + "\n"
                + "    public int getLimitCriteria() {\n"
                + "        return limitCriteria;\n"
                + "    }\n"
                + "\n"
                + "    public void setLimitCriteria(int limitCriteria) {\n"
                + "        this.limitCriteria = limitCriteria;\n"
                + "    }\n"
                + "\n"
                + "    public String getVar() {\n"
                + "        return var;\n"
                + "    }\n"
                + "\n"
                + "    public void setVar(String var) {\n"
                + "        this.var = var;\n"
                + "    }\n"
                + "\n"
                + "    public Boolean isFirst() throws SQLException {\n"
                + "        return rs.isFirst();\n"
                + "    }\n"
                + "\n"
                + "    public Boolean isLast() throws SQLException {\n"
                + "        return rs.isLast();\n"
                + "    }\n"
                + "\n"
                + "\n");
        
        entityList = new ArrayList<String>();
        if (theEntity.getParents().size() > 1) {
            for (int i = 0; i < theEntity.getParents().size(); i++){
                Entity parent = theEntity.getParents().elementAt(i).getSourceEntity();
                if( !entityList.contains(parent.getUpperLabel()) ){
                	out.write("   public boolean getUse" + parent.getUpperLabel() + "() {\n"
                			+ "        return use" + parent.getUpperLabel() + ";\n"
                			+ "    }\n"
                			+ "\n"
                			+ "    public void setUse" + parent.getUpperLabel() + "(boolean use" + parent.getUpperLabel() + ") {\n"
                			+ "        this.use" + parent.getUpperLabel() + " = use" + parent.getUpperLabel() + ";\n"
                			+ "    }\n"
                			+ "\n");
                	entityList.add(parent.getUpperLabel());
                }
            }
            out.write("\n");
        }

        generateSettersGetters(theEntity, out, true, false);
        out.write("}\n");
        out.close();
    }

    private void generateEntityDeleterClass(Entity theEntity) throws IOException {
        if ((theEntity.getPrimaryKeyAttributes() == null || theEntity.getPrimaryKeyAttributes().size() == 0) && (theEntity.getSubKeyAttributes() == null || theEntity.getSubKeyAttributes().size() == 0))
            return;

        Vector<Attribute> primaryKeys = theEntity.getPrimaryKeyAttributes();
        File baseClassFile = new File(tagDirectory, "/" + theEntity.getUnqualifiedLabel() + "Deleter.java");
        FileWriter fstream = new FileWriter(baseClassFile);
        BufferedWriter out = new BufferedWriter(fstream);
        out.write("package " + packagePrefix + "." + theEntity.getUnqualifiedLowerLabel() + ";\n"
                + "\n"
                + "\n"
                + "import java.sql.PreparedStatement;\n"
                + "import java.sql.ResultSet;\n"
                + "import java.sql.SQLException;\n"
                + "import java.util.Vector;\n");
        out.write("import org.apache.logging.log4j.Logger;\n");
        out.write("import org.apache.logging.log4j.LogManager;\n");
        
        if (theEntity.hasDateTime())
            out.write("import java.util.Date;\n");
        if (theEntity.hasTimestamp())
            out.write("import java.sql.Timestamp;\n");
//        if (theEntity.hasImage())
//            out.write("import java.awt.Image;\n");
        out.write("\n"
                + "import javax.servlet.jsp.JspException;\n"
                + "import javax.servlet.jsp.tagext.Tag;\n"
                + "\n"
                + "import " + packagePrefix + "." + projectName + "TagSupport;\n"
                + "import " + packagePrefix + "." + projectName + "BodyTagSupport;\n");
        generateParentImports(out, theEntity);
        out.write("\n"
                + "@SuppressWarnings(\"serial\")" 
        		+ "\n"
                + "public class " + theEntity.getUnqualifiedLabel() + "Deleter extends " + projectName + "BodyTagSupport {\n");
        for (int i = 0; i < primaryKeys.size(); i++) {
            Attribute theKey = primaryKeys.elementAt(i);
            out.write("    " + theKey.getType() + " " + theKey.getLabel() + " = " + theKey.getInitializer() + ";\n");
        }
        for (int i = 0; i < theEntity.attributes.size(); i++) {
            Attribute theAttribute = theEntity.attributes.elementAt(i);
            if (!theEntity.isPrimaryReference(theAttribute))
                out.write("    " + theAttribute.getType() + " " + theAttribute.getLabel() + " = " + theAttribute.getInitializer() + ";\n");
        }
        out.write("\tVector<" + projectName + "TagSupport> parentEntities = new Vector<" + projectName + "TagSupport>();\n\n");

        out.write("\tprivate static final Logger log = LogManager.getLogger("+theEntity.getUnqualifiedLabel() +"Deleter.class);\n\n");

        out.write("\n    ResultSet rs = null;\n"
                + "    String var = null;\n"
                + "    int rsCount = 0;\n"
                + "\n");
        
        out.write("    public int doStartTag() throws JspException {\n");
        generateParentEntityReferences(out, theEntity, "\t\t");
        
        // generate and execute the delete statement to remove this tuple.
        // TODO the initializer logic may be generically unsafe in that it might be possible to execute a statement that removes all entries in the table
        
        out.write("\n        PreparedStatement stat;\n"
                + "        try {\n"
                + "            int webapp_keySeq = 1;\n"
                + "            stat = getConnection().prepareStatement(\"DELETE from " + theSchema.getSqlLabel() + "." + theEntity.getSqlLabel() + " where 1=1\"");
        StringBuffer queryBuffer = new StringBuffer();
        for (int i = 0; i < primaryKeys.size(); i++) {
            Attribute theAttribute = primaryKeys.elementAt(i);
            out.write("\n                                                        + (" + theAttribute.getLabel() + " == " + theAttribute.getInitializer() + " ? \"\" : \" and " + theAttribute.getSqlLabel() + " = ? \")");
            queryBuffer.append("            if (" + theAttribute.getLabel() + " != " + theAttribute.getInitializer() + ") stat."
                    + theAttribute.getSQLMethod(false)
                    + "(webapp_keySeq++, " + theAttribute.getLabel() + (theAttribute.isDateTime() ? " == null ? null : new java.sql." 
                    + (theAttribute.isTimestamp() ? "Timestamp" : "Date") + "(" + theAttribute.getLabel() + ".getTime())" : "") 
                    + ");\n");
        }
        
        for (int i = 0; i < theEntity.getParents().size(); i++) {
            Relationship theRelationship = theEntity.getParents().elementAt(i);
            Entity theSourceEntity = theRelationship.getSourceEntity();
            
            /*
             out.write(indentString + theSourceEntity.getLabel() + " the" + theSourceEntity.getLabel() + " = (" 
             	+ theSourceEntity.getLabel() + ")findAncestorWithClass(this, " + theSourceEntity.getLabel() + ".class);\n");
            out.write(indentString + "if (the" + theSourceEntity.getLabel() + "!= null)" + "\n" + indentString + "\tparentEntities.addElement(the" + theSourceEntity.getLabel() + ");\n");
            parentBuffer.append(indentString + "if (the" + theSourceEntity.getLabel() + " == null) {\n");
            parentBuffer.append(indentString + "} else {\n");
            for (int j = 0; j < theSourceEntity.getPrimaryKeyAttributes().size(); j++) {
                parentBuffer.append(indentString + "\t" + theEntity.getAttributeBySQLLabel(theRelationship.getForeignReferencedAttribute(theSourceEntity.getPrimaryKeyAttributes().elementAt(j).getSqlLabel())).getLabel() + " = the" + theSourceEntity.getLabel() + ".get"
                        + Character.toUpperCase(theSourceEntity.getPrimaryKeyAttributes().elementAt(j).getLabel().charAt(0))
                        + theSourceEntity.getPrimaryKeyAttributes().elementAt(j).getLabel().substring(1) + "();\n");
            }
            parentBuffer.append(indentString + "}\n");
             */
            
            for (int j = 0; j < theSourceEntity.getPrimaryKeyAttributes().size(); j++) {
            	Attribute attribute = theEntity.getAttributeBySQLLabel(theRelationship.getForeignReferencedAttribute(theSourceEntity.getPrimaryKeyAttributes().elementAt(j).getSqlLabel()));
            	out.write("\n                                                        + (" + attribute.getLabel() + " == " + attribute.getInitializer() + " ? \"\" : \" and " + attribute.getSqlLabel() + " = ? \")");
                queryBuffer.append("\t\t\tif (" + attribute.getLabel() + " != " + attribute.getInitializer() + ") stat."+ attribute.getSQLMethod(false)
                        + "(webapp_keySeq++, " + attribute.getLabel() + (attribute.isDateTime() ? " == null ? null : new java.sql." 
                        + (attribute.isTimestamp() ? "Timestamp" : "Date") + "(" + attribute.getLabel() + ".getTime())" : "") 
                        + ");\n");
            }
        }
        
        out.write(");\n");
        out.write(queryBuffer.toString());
        out.write("            stat.execute();\n\n");
        
        out.write("\t\t\twebapp_keySeq = 1;\n");
        
        queryBuffer = new StringBuffer();
        
        StringBuffer keyBuf = new StringBuffer("");
        String orderBy = null;
        // if we have a counter attribute, we need to shift the record of interest to zero
        for (int i = 0; i < primaryKeys.size(); i++) {
            Attribute theKey = primaryKeys.elementAt(i);
            if (!theKey.isCounter())
            	continue;
             
            out.write("\t\t\tPreparedStatement stmt = getConnection().prepareStatement(\n");
            out.write("\t\t\t\t\t\"select ");
            out.write(theKey.getSqlLabel()+" from "+ theSchema.getSqlLabel() + "." + theEntity.getSqlLabel());
            out.write(" where 1=1 \"");
            for (int j = 0; j < primaryKeys.size(); j++) {
                Attribute theAttribute = primaryKeys.elementAt(j);
                if (theAttribute == theKey) {
                	orderBy = theKey.getSqlLabel();
                	keyBuf.append("\t\t\t\t"+theKey.getType() +" _keyVal = rs."+theKey.getSQLMethod(true)+"(1);\n");
                	
                	out.write("\n\t\t\t\t\t+\" and " + theKey.getSqlLabel() + " > ? \"");
                } else {
	                out.write("\n\t\t\t\t\t+(" + theAttribute.getLabel() + " == " + theAttribute.getInitializer() + " ? \"\" : \" and " + theAttribute.getSqlLabel() + " = ? \")");
                }
                
                queryBuffer.append("\n\t\t\tif (" + theAttribute.getLabel() + " != " + theAttribute.getInitializer() + ") stmt."
                        + theAttribute.getSQLMethod(false)
                        + "(webapp_keySeq++, "
                        + theAttribute.getLabel()
                        + (theAttribute.isDateTime() ? " == null ? null : new java.sql."
                                + (theAttribute.isTimestamp() ? "Timestamp" : "Date") + "(" + theAttribute.getLabel() + ".getTime())"
                                : ""));
                queryBuffer.append(");");
            }
            out.write(orderBy == null ? "" : "\n\t\t\t\t\t+\" order by "+orderBy+" asc\"");
            out.write(");\n");
            
            
            out.write(queryBuffer.toString());
            out.write("\n\t\t\trs = stmt.executeQuery();\n");
            out.write("\t\t\twhile(rs.next()){\n");
            out.write("\t\t\t\twebapp_keySeq = 1;\n");
            
            out.write(keyBuf.toString());
            
            queryBuffer = new StringBuffer();
            
            out.write("\t\t\t\tstat = getConnection().prepareStatement(");
            out.write("\"update " + theSchema.getSqlLabel() + "." + theEntity.getSqlLabel()+" set " + theKey.getSqlLabel() + " = " + theKey.getSqlLabel() + " - 1 where 1=1 \"");
            
            for (int j = 0; j < primaryKeys.size(); j++) {
                Attribute theAttribute = primaryKeys.elementAt(j);
                if (theAttribute == theKey) {
                	out.write("\n\t\t\t\t\t\t+ \" and " + theKey.getSqlLabel() + " = ? \"");
                } else {
	                out.write("\n\t\t\t\t\t\t+ (" + theAttribute.getLabel() + " == " + theAttribute.getInitializer() + " ? \"\" : \" and " + theAttribute.getSqlLabel() + " = ? \") ");
                }
                queryBuffer.append("\n\t\t\t\tif (" + theAttribute.getLabel() + " != " + theAttribute.getInitializer() + ") stat.");
                queryBuffer.append(theAttribute.getSQLMethod(false));
                queryBuffer.append("(webapp_keySeq++, ");
                queryBuffer.append((theAttribute == theKey ? "_keyVal" : theAttribute.getLabel())); 
                queryBuffer.append((theAttribute.isDateTime() ? " == null ? null : new java.sql."+ (theAttribute.isTimestamp() ? "Timestamp" : "Date") + "(" + theAttribute.getLabel() + ".getTime())" : ""));
                queryBuffer.append(");");
            }
            out.write(");\n");
            out.write(queryBuffer.toString());
            out.write("\n");
            out.write("\t\t\t\tstat.execute();\n");
            out.write("\t\t\t\tstat.close();\n");
            
            out.write("\t\t\t}\n");
            out.write("\t\t\trs.close();\n");
            out.write("\t\t\tstmt.close();\n\n");
            
        }
        
        out.write("        } catch (SQLException e) {\n");
        out.write("            log.error(\"JDBC error generating " + theEntity.getLabel() + " deleter\", e);\n\n");
        
        out.write("\t\t\tclearServiceState();\n");
        out.write("\t\t\tfreeConnection();\n\n");
        
        out.write("\t\t\tTag parent = getParent();\n");
        out.write("\t\t\tif(parent != null){\n");
        out.write("\t\t\t\tpageContext.setAttribute(\"tagError\", true);\n");
        out.write("\t\t\t\tpageContext.setAttribute(\"tagErrorException\", e);\n");
        out.write("\t\t\t\tpageContext.setAttribute(\"tagErrorMessage\", \"Error: JDBC error generating " + theEntity.getLabel() + " deleter\");\n");
        out.write("\t\t\t\treturn parent.doEndTag();\n");
        out.write("\t\t\t}else{\n");
        out.write("\t\t\t\tthrow new JspException(\"Error: JDBC error generating " + theEntity.getLabel() + " deleter\",e);\n");
        out.write("\t\t\t}\n\n");
        
        out.write("        } finally {\n");
        out.write("            freeConnection();\n");
        out.write("        }\n\n");
        out.write("        return SKIP_BODY;\n");
        out.write("    }\n");
        out.write("\n");
        
        
        
        out.write("\tpublic int doEndTag() throws JspException {\n\n");
        out.write("\t\tclearServiceState();\n");
        
        out.write("\t\tBoolean error = (Boolean) pageContext.getAttribute(\"tagError\");\n");
        out.write("\t\tif(error != null && error){\n\n");
        
        out.write("\t\t\tfreeConnection();\n\n");

        out.write("\t\t\tException e = (Exception) pageContext.getAttribute(\"tagErrorException\");\n");
        out.write("\t\t\tString message = (String) pageContext.getAttribute(\"tagErrorMessage\");\n\n");
        
        out.write("\t\t\tTag parent = getParent();\n");
        out.write("\t\t\tif(parent != null){\n");
        out.write("\t\t\t\treturn parent.doEndTag();\n");
        
        out.write("\t\t\t}else if(e != null && message != null){\n");
        out.write("\t\t\t\tthrow new JspException(message,e);\n");
        out.write("\t\t\t}else if(parent == null){\n");
        out.write("\t\t\t\tpageContext.removeAttribute(\"tagError\");\n");
        out.write("\t\t\t\tpageContext.removeAttribute(\"tagErrorException\");\n");
        out.write("\t\t\t\tpageContext.removeAttribute(\"tagErrorMessage\");\n");
        out.write("\t\t\t}\n");
        out.write("\t\t}\n");
        
        out.write("\t\treturn super.doEndTag();\n");
        out.write("\t}\n\n");
        out.write("    private void clearServiceState() {\n");
        for (int i = 0; i < primaryKeys.size(); i++) {
            Attribute theKey = primaryKeys.elementAt(i);
            out.write("        " + theKey.getLabel() + " = " + theKey.getInitializer() + ";\n");
        }
        out.write("        parentEntities = new Vector<" + projectName + "TagSupport>();\n\n");
        out.write("        this.rs = null;\n"
                + "        this.var = null;\n"
                + "        this.rsCount = 0;\n"
                + "    }\n"
                + "\n"
                + "    public String getVar() {\n"
                + "        return var;\n"
                + "    }\n"
                + "\n"
                + "    public void setVar(String var) {\n"
                + "        this.var = var;\n"
                + "    }\n"
                + "\n"
                + "\n");
        
        generateSettersGetters(theEntity, out, true, false);
        out.write("}\n");
        out.close();
    }

    private void generateEntityShifterClass(Entity theEntity) throws IOException {
        Vector<Attribute> primaryKeys = theEntity.getPrimaryKeyAttributes();
        File baseClassFile = new File(tagDirectory, "/" + theEntity.getUnqualifiedLabel() + "Shifter.java");
        FileWriter fstream = new FileWriter(baseClassFile);
        BufferedWriter out = new BufferedWriter(fstream);
        out.write("package " + packagePrefix + "." + theEntity.getUnqualifiedLowerLabel() + ";\n"
                + "\n"
                + "\n"
                + "import java.sql.PreparedStatement;\n"
                + "import java.sql.ResultSet;\n"
                + "import java.sql.SQLException;\n"
                + "import java.util.Vector;\n"
                + "import javax.servlet.jsp.tagext.Tag;\n"
                + "\n"
                + "import javax.servlet.jsp.JspException;\n"
                + "import javax.servlet.jsp.JspTagException;\n"
                + "\n"
                + "import " + packagePrefix + "." + projectName + "TagSupport;\n"
                + "import " + packagePrefix + "." + projectName + "BodyTagSupport;\n");
        generateParentImports(out, theEntity);
        out.write("import org.apache.logging.log4j.Logger;\n");
        out.write("import org.apache.logging.log4j.LogManager;\n");
        
        out.write("\n"
                + "@SuppressWarnings(\"serial\")" 
        		+ "\n"
                + "public class " + theEntity.getUnqualifiedLabel() + "Shifter extends " + projectName + "BodyTagSupport {\n");
        for (int i = 0; i < primaryKeys.size(); i++) {
            Attribute theKey = primaryKeys.elementAt(i);
            out.write("    " + theKey.getType() + " " + theKey.getLabel() + " = " + theKey.getInitializer() + ";\n");
        }
        out.write("\tint newNumber = 0;\n");
        out.write("\tVector<" + projectName + "TagSupport> parentEntities = new Vector<" + projectName + "TagSupport>();\n\n");
        out.write("\tprivate static final Logger log = LogManager.getLogger("+theEntity.getUnqualifiedLabel() +"Shifter.class);\n\n");

        out.write("\n    ResultSet rs = null;\n"
                + "    String var = null;\n"
                + "    int rsCount = 0;\n"
                + "\n");
        
        out.write("    public int doStartTag() throws JspException {\n");
        generateParentEntityReferences(out, theEntity, "\t\t");
        
        // if we have a counter attribute, we need to shift the record of interest to minus one
        for (int i = 0; i < primaryKeys.size(); i++) {
            Attribute theKey = primaryKeys.elementAt(i);
            if (!theKey.isCounter())
            	continue;
            out.write("\n        PreparedStatement stat;\n"
                    + "        try {\n"
                    + "            int webapp_keySeq = 1;\n");
            out.write("            stat = getConnection().prepareStatement(\"update " + theSchema.getSqlLabel() + "." + theEntity.getSqlLabel()
            		+ " set " + theKey.getSqlLabel() + " = -1 where 1=1\"\n");
            StringBuffer queryBuffer = new StringBuffer();
            for (int j = 0; j < primaryKeys.size(); j++) {
                Attribute theAttribute = primaryKeys.elementAt(j);
                if (theAttribute == theKey) {
                	out.write("                                                        + \" and " + theKey.getSqlLabel() + " = ?\"\n");
                } else {
	                out.write("                                                        + (" + theAttribute.getLabel() + " == " + theAttribute.getInitializer() + " ? \"\" : \" and " + theAttribute.getSqlLabel() + " = ?\")\n");
                }
                queryBuffer.append("            if (" + theAttribute.getLabel() + " != " + theAttribute.getInitializer() + ") stat."
                        + theAttribute.getSQLMethod(false)
                        + "(webapp_keySeq++, "
                        + theAttribute.getLabel()
                        + (theAttribute.isDateTime() ? " == null ? null : new java.sql."
                                + (theAttribute.isTimestamp() ? "Timestamp" : "Date") + "(" + theAttribute.getLabel() + ".getTime())"
                                : ""));
                queryBuffer.append(");\n");
            }
            out.write("                                                        );\n");
            out.write(queryBuffer.toString());
            out.write("            stat.execute();\n\n");
        }
        
        // if we have a counter attribute, we now shift the target record into the old slot
        for (int i = 0; i < primaryKeys.size(); i++) {
            Attribute theKey = primaryKeys.elementAt(i);
            if (!theKey.isCounter())
            	continue;
            out.write("            webapp_keySeq = 1;\n");
            out.write("            stat = getConnection().prepareStatement(\"update " + theSchema.getSqlLabel() + "." + theEntity.getSqlLabel()
            		+ " set " + theKey.getSqlLabel() + " = ? where 1=1\"\n");
            StringBuffer queryBuffer = new StringBuffer();
            StringBuffer targetBuffer = new StringBuffer();
            for (int j = 0; j < primaryKeys.size(); j++) {
                Attribute theAttribute = primaryKeys.elementAt(j);
                if (theAttribute == theKey) {
                	out.write("                                                        + \" and " + theKey.getSqlLabel() + " = ?\"\n");
                	targetBuffer.append("            stat.setInt(webapp_keySeq++, " + theKey.getLabel() + ");\n");
                	queryBuffer.append("            stat.setInt(webapp_keySeq++, newNumber);\n");
               } else {
	                out.write("                                                        + (" + theAttribute.getLabel() + " == " + theAttribute.getInitializer() + " ? \"\" : \" and " + theAttribute.getSqlLabel() + " = ?\")\n");
	                queryBuffer.append("            if (" + theAttribute.getLabel() + " != " + theAttribute.getInitializer() + ") stat."
	                        + theAttribute.getSQLMethod(false)
	                        + "(webapp_keySeq++, "
	                        + theAttribute.getLabel()
	                        + (theAttribute.isDateTime() ? " == null ? null : new java.sql."
	                                + (theAttribute.isTimestamp() ? "Timestamp" : "Date") + "(" + theAttribute.getLabel() + ".getTime())"
	                                : ""));
	                queryBuffer.append(");\n");
	            }
            }
            out.write("                                                        );\n");
            out.write(targetBuffer.toString());
            out.write(queryBuffer.toString());
            out.write("            stat.execute();\n\n");
        }
        
        // if we have a counter attribute, we now shift the target record into the vacated slot
        for (int i = 0; i < primaryKeys.size(); i++) {
            Attribute theKey = primaryKeys.elementAt(i);
            if (!theKey.isCounter())
            	continue;
            out.write("            webapp_keySeq = 1;\n");
            out.write("            stat = getConnection().prepareStatement(\"update " + theSchema.getSqlLabel() + "." + theEntity.getSqlLabel()
            		+ " set " + theKey.getSqlLabel() + " = ? where 1=1\"\n");
            StringBuffer queryBuffer = new StringBuffer();
            StringBuffer targetBuffer = new StringBuffer();
            for (int j = 0; j < primaryKeys.size(); j++) {
                Attribute theAttribute = primaryKeys.elementAt(j);
                if (theAttribute == theKey) {
                	targetBuffer.append("            stat.setInt(webapp_keySeq++, newNumber);\n");
                	out.write("                                                        + \" and " + theKey.getSqlLabel() + " = -1\"\n");
                } else {
	                out.write("                                                        + (" + theAttribute.getLabel() + " == " + theAttribute.getInitializer() + " ? \"\" : \" and " + theAttribute.getSqlLabel() + " = ?\")\n");
	                queryBuffer.append("            if (" + theAttribute.getLabel() + " != " + theAttribute.getInitializer() + ") stat."
	                        + theAttribute.getSQLMethod(false)
	                        + "(webapp_keySeq++, "
	                        + theAttribute.getLabel()
	                        + (theAttribute.isDateTime() ? " == null ? null : new java.sql."
	                                + (theAttribute.isTimestamp() ? "Timestamp" : "Date") + "(" + theAttribute.getLabel() + ".getTime())"
	                                : ""));
	                queryBuffer.append(");\n");
                }
            }
            out.write("                                                        );\n");
            out.write(targetBuffer.toString());
            out.write(queryBuffer.toString());
            out.write("            stat.execute();\n");
        }
        
        
        out.write("        } catch (SQLException e) {\n");
        out.write("            log.error(\"JDBC error generating " + theEntity.getLabel() + " deleter\", e);\n\n");
        
        out.write("\t\t\tclearServiceState();\n\n");
        out.write("\t\t\tfreeConnection();\n\n");
        out.write("\t\t\tTag parent = getParent();\n");
        out.write("\t\t\tif(parent != null){\n");
        out.write("\t\t\t\tpageContext.setAttribute(\"tagError\", true);\n");
        out.write("\t\t\t\tpageContext.setAttribute(\"tagErrorException\", e);\n");
        out.write("\t\t\t\tpageContext.setAttribute(\"tagErrorMessage\", \"JDBC error generating " + theEntity.getLabel() + " deleter\");\n");
        out.write("\t\t\t\treturn parent.doEndTag();\n");
        out.write("\t\t\t}else{\n");
        out.write("\t\t\t\tthrow new JspException(\"JDBC error generating " + theEntity.getLabel() + " deleter\",e);\n");
        out.write("\t\t\t}\n\n");
        
        out.write("        } finally {\n");
        out.write("            freeConnection();\n");
        out.write("        }\n");
        out.write("\n");
        out.write("        return SKIP_BODY;\n");
        out.write("    }\n");
        out.write("\n");
        
        out.write("\tpublic int doEndTag() throws JspException {\n\n");
        out.write("\t\tclearServiceState();\n");
        
        
        out.write("\t\tif(pageContext != null){\n");
        out.write("\t\t\tBoolean error = (Boolean) pageContext.getAttribute(\"tagError\");\n");
        out.write("\t\t\tif(error != null && error){\n\n");
        out.write("\t\t\t\tfreeConnection();\n\n");
        out.write("\t\t\t\tException e = (Exception) pageContext.getAttribute(\"tagErrorException\");\n");
        out.write("\t\t\t\tString message = (String) pageContext.getAttribute(\"tagErrorMessage\");\n\n");
        out.write("\t\t\t\tTag parent = getParent();\n");
        out.write("\t\t\t\tif(parent != null){\n");
        out.write("\t\t\t\t\treturn parent.doEndTag();\n");
        out.write("\t\t\t\t}else if(e != null && message != null){\n");
        out.write("\t\t\t\t\tthrow new JspException(message,e);\n");
        out.write("\t\t\t\t}else if(parent == null){\n");
        out.write("\t\t\t\t\tpageContext.removeAttribute(\"tagError\");\n");
        out.write("\t\t\t\t\tpageContext.removeAttribute(\"tagErrorException\");\n");
        out.write("\t\t\t\t\tpageContext.removeAttribute(\"tagErrorMessage\");\n");
        out.write("\t\t\t\t}\n");
        out.write("\t\t\t}\n");
        out.write("\t\t}\n");
        
        
        out.write("\t\treturn super.doEndTag();\n");
        out.write("\t}\n\n");
        out.write("    private void clearServiceState() {\n");
        for (int i = 0; i < primaryKeys.size(); i++) {
            Attribute theKey = primaryKeys.elementAt(i);
            out.write("        " + theKey.getLabel() + " = " + theKey.getInitializer() + ";\n");
        }
        out.write("        parentEntities = new Vector<" + projectName + "TagSupport>();\n\n");
        out.write("        this.rs = null;\n"
                + "        this.var = null;\n"
                + "        this.rsCount = 0;\n"
                + "    }\n"
                + "\n"
                + "    public String getVar() {\n"
                + "        return var;\n"
                + "    }\n"
                + "\n"
                + "    public void setVar(String var) {\n"
                + "        this.var = var;\n"
                + "    }\n"
                + "\n"
                + "\n");
        
        generateSettersGetters(theEntity, out, true, false);
        generateSetterGetter("int", "newNumber", false, out);
        out.write("}\n");
        out.close();
    }

    private void generateEntityHelperClass(Entity theEntity, Attribute theAttribute) throws IOException {
        generateEntityHelperClass(theEntity, "", theAttribute);
    }

    private void generateEntityUploadHelperClass(Entity theEntity, Attribute theAttribute) throws IOException {
        generateEntityHelperClass(theEntity, "Upload", theAttribute);
    }

    private void generateEntityHelperClass(Entity theEntity, String uploadString, Attribute theAttribute) throws IOException {
        File baseClassFile = new File(tagDirectory, "/" + theEntity.getUnqualifiedLabel() + uploadString
                + Character.toUpperCase(theAttribute.getLabel().charAt(0)) + theAttribute.getLabel().substring(1) + ".java");
        FileWriter fstream = new FileWriter(baseClassFile);
        BufferedWriter out = new BufferedWriter(fstream);
        out.write("package " + packagePrefix + "." + theEntity.getUnqualifiedLowerLabel() + ";\n");

        out.write("\nimport javax.servlet.jsp.JspException;\n");
        out.write("import javax.servlet.jsp.JspTagException;\n");
        out.write("import javax.servlet.jsp.tagext.Tag;\n\n");
        
        if (theAttribute.isBinaryDomain() || theAttribute.isImage()) {
            out.write("import java.sql.PreparedStatement;\n");
            out.write("import java.sql.ResultSet;\n");
            out.write("import javax.servlet.http.HttpServletResponse;\n");
            out.write("import java.io.ByteArrayInputStream;\n");
            out.write("import java.io.BufferedInputStream;\n");
            out.write("import java.io.BufferedOutputStream;\n");
            out.write("import java.io.InputStream;\n");
            out.write("import java.io.OutputStream;\n");
            out.write("import java.io.IOException;\n");

        }
        if (theAttribute.isImage()) {
            out.write("import javax.imageio.ImageIO;\n");
        }
        if (theAttribute.isDateTime() || (theAttribute.isBinaryDomain() && theEntity.hasDateTime())) {
            out.write("import java.util.Date;\n");
            out.write("import java.text.DateFormat;\n");
            out.write("import java.text.SimpleDateFormat;\n");
        }
        if (theAttribute.isTimestamp())
            out.write("import java.sql.Timestamp;\n");
        out.write("import org.apache.logging.log4j.Logger;\n");
        out.write("import org.apache.logging.log4j.LogManager;\n");

//        if (theAttribute.isImage() || (theAttribute.isDomain() && theEntity.hasImage()))
//            out.write("import java.awt.Image;\n");

        out.write("\nimport " + packagePrefix + "." + projectName + "TagSupport;\n");

        out.write("\n@SuppressWarnings(\"serial\")\n");
        out.write("public class " + theEntity.getUnqualifiedLabel() + uploadString + theAttribute.getUpperLabel() + " extends " + projectName + "TagSupport {\n\n");
        
        if (theAttribute.isDateTime()) {
        	out.write("\tString type = \"DATE\";\n");
        	out.write("\tString dateStyle = \"DEFAULT\";\n");
        	out.write("\tString timeStyle = \"DEFAULT\";\n");
        	out.write("\tString pattern = null;\n");
        }
        out.write("\tprivate static final Logger log = LogManager.getLogger("+theEntity.getUnqualifiedLabel()+ uploadString + theAttribute.getUpperLabel()+".class);\n");

        
        if (theAttribute.isBinaryDomain() || theAttribute.isImage()) {
            for (int i = 0; i < theEntity.getAttributes().size(); i++) {
                Attribute entityAttribute = theEntity.getAttributes().elementAt(i);
                out.write("\t" + (entityAttribute.getDomain() == null ? entityAttribute.getType() : entityAttribute.getDomain().getJavaType()) + " " + entityAttribute.getLabel() + " = " + entityAttribute.getInitializer() + ";\n");
            }
        }

        out.write("\n\tpublic int doStartTag() throws JspException {\n");
        out.write("\t\ttry {\n");
        if (theAttribute.isBinaryDomain() || theAttribute.isImage()) {
            out.write("\t\t\tPreparedStatement stmt = getConnection().prepareStatement(\"select");
            StringBuffer paramBuffer = new StringBuffer();
            StringBuffer queryBuffer = new StringBuffer();
            StringBuffer resultBuffer = new StringBuffer();
            int keySeq = 0;
            int attrSeq = 0;
            boolean firstParam = true;
            for (int k = 0; k < theEntity.getAttributes().size(); k++) {
                Attribute entityAttribute = theEntity.getAttributes().elementAt(k);
                if (entityAttribute.isPrimary()) {
                    paramBuffer.append((firstParam ? " " : " and ") + entityAttribute.getSqlLabel() + " = ?");
                    queryBuffer.append("\t\t\tstmt."
                            + entityAttribute.getSQLMethod(false)
                            + "("
                            + (keySeq + 1)
                            + ","
                            + entityAttribute.getLabel()
                            + (entityAttribute.isDateTime() ? " == null ? null : new java.sql."
                                    + (entityAttribute.isTimestamp() ? "Timestamp" : "Date") + "(" + entityAttribute.getLabel() + ".getTime())"
                                    : "") + ");\n");
                    keySeq++;
                    firstParam = false;
                } else {
                    out.write((attrSeq == 0 ? " " : ",") + entityAttribute.getSqlLabel());
                    resultBuffer.append("\t\t\t\tif (" + entityAttribute.getLabel() + " == " + entityAttribute.getInitializer() + ")\n");
                    resultBuffer.append("\t\t\t\t\t" + entityAttribute.getLabel() + " = "
//                            + (theAttribute.getDomain() == null ? "" : "(" + theAttribute.getDomain().getLabel() + ") (Object) ")
                            + "rs." + entityAttribute.getSQLMethod(true) + "(" + (attrSeq + 1) + ");\n");
                    attrSeq++;
                }
            }
            out.write(" from " + theSchema.getSqlLabel()+ "." + theEntity.getSqlLabel() + " where");
            out.write(paramBuffer.toString());
            out.write("\");\n");
            out.write(queryBuffer.toString());
            out.write("\t\t\tResultSet rs = stmt.executeQuery();\n");
            out.write("\t\t\twhile (rs.next()) {\n");
            out.write(resultBuffer.toString());
            out.write("\t\t\t}\n");
            out.write("\t\t\trs.close();\n");
            out.write("\t\t\tstmt.close();\n");
            out.write("\n");

            if (theAttribute.getDomain().getJavaType().equals("byte[]")) {
                out.write("\t\t\tHttpServletResponse response = (HttpServletResponse)pageContext.getResponse();\n");
                out.write("\t\t\tresponse.setHeader(\"Expires\", \"0\");\n");
                out.write("\t\t\tresponse.setHeader(\"Cache-Control\", \"must-revalidate, post-check=0, pre-check=0\");\n");
                out.write("\t\t\tresponse.setHeader(\"Pragma\", \"public\");\n");
                out.write("\t\t\tresponse.setHeader(\"Content-disposition\", \"attachment;filename=\\\"\" + " + theAttribute.getLowerLabel() + "Name + \"\\\"\");\n");
                out.write("\t\t\tif (" + theAttribute.getLowerLabel() + "Name.toLowerCase().endsWith(\".pdf\"))\n");
                    out.write("\t\t\t        response.setContentType(\"application/pdf\");\n");
                out.write("\t\t\telse if (" + theAttribute.getLowerLabel() + "Name.toLowerCase().endsWith(\".doc\"))\n");
                    out.write("\t\t\t    response.setContentType(\"application/msword\");\n");
                    out.write("\t\t\telse if (" + theAttribute.getLowerLabel() + "Name.toLowerCase().endsWith(\".docx\"))\n");
                    out.write("\t\t\t    response.setContentType(\"application/msword\");\n");
                    out.write("\t\t\telse if (" + theAttribute.getLowerLabel() + "Name.toLowerCase().endsWith(\".ppt\"))\n");
                    out.write("\t\t\t    response.setContentType(\"application/mspowerpoint\");\n");
                    out.write("\t\t\telse if (" + theAttribute.getLowerLabel() + "Name.toLowerCase().endsWith(\".pptx\"))\n");
                    out.write("\t\t\t    response.setContentType(\"application/mspowerpoint\");\n");
                out.write("\t\t\tresponse.setContentLength(" + theAttribute.getLowerLabel() + ".length);\n");
                out.write("\t\t\t\n");
                out.write("\t\t\t// write ByteArrayOutputStream to the response OutputStream\n");
                out.write("\t\t\tcopy(new ByteArrayInputStream(" + theAttribute.getLowerLabel() + "), response.getOutputStream());\n");
            } else if (theAttribute.isImage()) {
                // See ImageServlet and Database.loadThumbnail in Loki 
                out.write("\t\t\tHttpServletResponse response = (HttpServletResponse)pageContext.getResponse();\n");
                out.write("\t\t\tImageIO.write(ImageIO.read(new ByteArrayInputStream(" + theAttribute.getLowerLabel() + ")),\"JPEG\", response.getOutputStream());\n");
            }

        } else {
            out.write("\t\t\t" + theEntity.getUnqualifiedLabel() + uploadString + " the" + theEntity.getUnqualifiedLabel() + uploadString + " = (" + theEntity.getUnqualifiedLabel() + uploadString
                    + ")findAncestorWithClass(this, " + theEntity.getUnqualifiedLabel() + uploadString + ".class);\n");
            if (uploadString.length() == 0)
                out.write("\t\t\tif (!the" + theEntity.getUnqualifiedLabel() + uploadString + ".commitNeeded) {\n");
            if (theAttribute.isDateTime()) {
                out.write("\t\t\t\tString resultString = null;\n");
                out.write("\t\t\t\tif (the" + theEntity.getUnqualifiedLabel() + uploadString + ".get"
                    + Character.toUpperCase(theAttribute.getLabel().charAt(0)) + theAttribute.getLabel().substring(1) + "() == null) {\n");
                out.write("\t\t\t\t\tresultString = \"\";\n");
                out.write("\t\t\t\t} else {\n");
                out.write("\t\t\t\t\tif (pattern != null) {\n");
                out.write("\t\t\t\t\t\tresultString = (new SimpleDateFormat(pattern)).format(the" + theEntity.getUnqualifiedLabel() + uploadString + ".get"
                        + Character.toUpperCase(theAttribute.getLabel().charAt(0)) + theAttribute.getLabel().substring(1) + "());\n");
                out.write("\t\t\t\t\t} else if (type.equals(\"BOTH\")) {\n");
                out.write("\t\t\t\t\t\tresultString = DateFormat.getDateTimeInstance(formatConvert(dateStyle),formatConvert(timeStyle)).format(the" + theEntity.getUnqualifiedLabel() + uploadString + ".get"
                    + Character.toUpperCase(theAttribute.getLabel().charAt(0)) + theAttribute.getLabel().substring(1) + "());\n");
                out.write("\t\t\t\t\t} else if (type.equals(\"TIME\")) {\n");
                out.write("\t\t\t\t\t\tresultString = DateFormat.getTimeInstance(formatConvert(timeStyle)).format(the" + theEntity.getUnqualifiedLabel() + uploadString + ".get"
                        + Character.toUpperCase(theAttribute.getLabel().charAt(0)) + theAttribute.getLabel().substring(1) + "());\n");
                out.write("\t\t\t\t\t} else { // date\n");
                out.write("\t\t\t\t\t\tresultString = DateFormat.getDateInstance(formatConvert(dateStyle)).format(the" + theEntity.getUnqualifiedLabel() + uploadString + ".get"
                        + Character.toUpperCase(theAttribute.getLabel().charAt(0)) + theAttribute.getLabel().substring(1) + "());\n");
                out.write("\t\t\t\t\t}\n");
                out.write("\t\t\t\t}\n");
            	out.write("\t\t\t\tpageContext.getOut().print(resultString);\n");
            } else {
            	out.write("\t\t\t\tpageContext.getOut().print(the" + theEntity.getUnqualifiedLabel() + uploadString + ".get"
                    + Character.toUpperCase(theAttribute.getLabel().charAt(0)) + theAttribute.getLabel().substring(1) + "());\n");
            }
            if (uploadString.length() == 0)
                out.write("\t\t\t}\n");
        }
        out.write("\t\t} catch (Exception e) {\n");
        out.write("\t\t\tlog.error(\"Can't find enclosing " + theEntity.getUnqualifiedLabel() + " for " + theAttribute.getLabel() + " tag \", e);\n");
        
        out.write("\t\t\tfreeConnection();\n");

        out.write("\t\t\tTag parent = getParent();\n");
        out.write("\t\t\tif(parent != null){\n");
        out.write("\t\t\t\tpageContext.setAttribute(\"tagError\", true);\n");
        out.write("\t\t\t\tpageContext.setAttribute(\"tagErrorException\", e);\n");
        out.write("\t\t\t\tpageContext.setAttribute(\"tagErrorMessage\", \"Error: Can't find enclosing " + theEntity.getUnqualifiedLabel() + " for " + theAttribute.getLabel() + " tag \");\n");
        out.write("\t\t\t\treturn parent.doEndTag();\n");
        out.write("\t\t\t}else{\n");
        out.write("\t\t\t\tthrow new JspTagException(\"Error: Can't find enclosing " + theEntity.getUnqualifiedLabel() + " for " + theAttribute.getLabel() + " tag \");\n");
        out.write("\t\t\t}\n\n");
        
        
        out.write("\t\t}\n");
        out.write("\t\treturn SKIP_BODY;\n");
        out.write("\t}\n");

        if (theAttribute.isBinaryDomain() || theAttribute.isImage()) {
            out.write("\n\tpublic int doEndTag() throws JspException {\n");
            out.write("\t\tclearServiceState();\n");
            out.write("\t\treturn super.doEndTag();\n");
            out.write("\t}\n");

            generateSettersGetters(theEntity, out, true, false);
        } else {
	        out.write("\n\tpublic " + theAttribute.getType() + " get" + theAttribute.getUpperLabel() + "() throws JspException {\n");
	        out.write("\t\ttry {\n");
	        out.write("\t\t\t" + theEntity.getUnqualifiedLabel() + " the" + theEntity.getUnqualifiedLabel() + " = (" + theEntity.getUnqualifiedLabel() + ")findAncestorWithClass(this, " + theEntity.getUnqualifiedLabel() + ".class);\n");
	        out.write("\t\t\treturn the" + theEntity.getUnqualifiedLabel() + ".get" + theAttribute.getUpperLabel() + "();\n");
	        out.write("\t\t} catch (Exception e) {\n");
	        
	        out.write("\t\t\tlog.error(\"Can't find enclosing " + theEntity.getUnqualifiedLabel() + " for " + theAttribute.getLabel() + " tag \", e);\n");
	        out.write("\t\t\tfreeConnection();\n");
	        out.write("\t\t\tTag parent = getParent();\n");
	        out.write("\t\t\tif(parent != null){\n");
	        out.write("\t\t\t\tpageContext.setAttribute(\"tagError\", true);\n");
	        out.write("\t\t\t\tpageContext.setAttribute(\"tagErrorException\", e);\n");
	        out.write("\t\t\t\tpageContext.setAttribute(\"tagErrorMessage\", \"Error: Can't find enclosing " + theEntity.getUnqualifiedLabel() + " for " + theAttribute.getLabel() + " tag \");\n");
	        out.write("\t\t\t\tparent.doEndTag();\n");
	        out.write("\t\t\t\treturn "+theAttribute.getInitializer()+";\n");
	        out.write("\t\t\t}else{\n");
	        out.write("\t\t\t\tthrow new JspTagException(\"Error: Can't find enclosing " + theEntity.getUnqualifiedLabel() + " for " + theAttribute.getLabel() + " tag \");\n");
	        out.write("\t\t\t}\n");

	        out.write("\t\t}\n");
	        out.write("\t}\n");
	        
	        out.write("\n\tpublic void set" + theAttribute.getUpperLabel() + "(" + (theAttribute.getDomain() == null ? theAttribute.getType() : theAttribute.getDomain().getJavaType()) + " " + theAttribute.getLabel()
	                + ") throws JspException {\n");
	        out.write("\t\ttry {\n");
	        out.write("\t\t\t" + theEntity.getUnqualifiedLabel() + " the" + theEntity.getUnqualifiedLabel() + " = (" + theEntity.getUnqualifiedLabel()
	                + ")findAncestorWithClass(this, " + theEntity.getUnqualifiedLabel() + ".class);\n");
	        out.write("\t\t\tthe" + theEntity.getUnqualifiedLabel() + ".set" + theAttribute.getUpperLabel() + "(" + theAttribute.getLabel() + ");\n");
	        out.write("\t\t} catch (Exception e) {\n");

	        out.write("\t\t\tlog.error(\"Can't find enclosing " + theEntity.getUnqualifiedLabel() + " for " + theAttribute.getLabel() + " tag \", e);\n");
	        out.write("\t\t\tfreeConnection();\n");
	        out.write("\t\t\tTag parent = getParent();\n");
	        out.write("\t\t\tif(parent != null){\n");
	        out.write("\t\t\t\tpageContext.setAttribute(\"tagError\", true);\n");
	        out.write("\t\t\t\tpageContext.setAttribute(\"tagErrorException\", e);\n");
	        out.write("\t\t\t\tpageContext.setAttribute(\"tagErrorMessage\", \"Error: Can't find enclosing " + theEntity.getUnqualifiedLabel() + " for " + theAttribute.getLabel() + " tag \");\n");
	        out.write("\t\t\t\tparent.doEndTag();\n");
	        out.write("\t\t\t}else{\n");
	        out.write("\t\t\t\tthrow new JspTagException(\"Error: Can't find enclosing " + theEntity.getUnqualifiedLabel() + " for " + theAttribute.getLabel() + " tag \");\n");
	        out.write("\t\t\t}\n");
	        
	        out.write("\t\t}\n");
	        out.write("\t}\n");
        }
        
        if (theAttribute.isBinaryDomain() || theAttribute.isImage()) {
            out.write("\n");
            out.write("\tpublic static void copy(InputStream inStream, OutputStream outStream) throws IOException {\n");
            out.write("\t\tInputStream in = null;\n");
            out.write("\t\tOutputStream out = null;\n");
            out.write("\t\ttry {\n");
            out.write("\t\t\tin = new BufferedInputStream(inStream);\n");
            out.write("\t\t\tout = new BufferedOutputStream(outStream);\n");
            out.write("\t\t\twhile (true) {\n");
            out.write("\t\t\t\tint data = in.read();\n");
            out.write("\t\t\t\tif (data == -1) {\n");
            out.write("\t\t\t\t\tbreak;\n");
            out.write("\t\t\t\t}\n");
            out.write("\t \t\t\tout.write(data);\n");
            out.write("\t\t\t}\n");
            out.write("\t\t} finally {\n");
            out.write("\t\t\tif (in != null) {\n");
            out.write("\t\t\t\tin.close();\n");
            out.write("\t\t\t}\n");
            out.write("\t\t\tif (out != null) {\n");
            out.write("\t\t\t\tout.close();\n");
            out.write("\t\t\t}\n");
            out.write("\t\t}\n");
            out.write("\t}\n");

            generateServiceStateReset(theEntity, out, false, false);
        }
        
        if (theAttribute.isDateTime()) {
        	generateDateFormatGetterSetter(out, "pattern");
        	generateDateFormatGetterSetter(out, "type");
        	generateDateFormatGetterSetter(out, "dateStyle");
        	generateDateFormatGetterSetter(out, "timeStyle");
            out.write("\n");
            out.write("\tpublic static int formatConvert(String stringValue) {\n");
            out.write("\t\tif (stringValue.equals(\"SHORT\"))\n");
            out.write("\t\t\treturn DateFormat.SHORT;\n");
            out.write("\t\tif (stringValue.equals(\"MEDIUM\"))\n");
            out.write("\t\t\treturn DateFormat.MEDIUM;\n");
            out.write("\t\tif (stringValue.equals(\"LONG\"))\n");
            out.write("\t\t\treturn DateFormat.LONG;\n");
            out.write("\t\tif (stringValue.equals(\"FULL\"))\n");
            out.write("\t\t\treturn DateFormat.FULL;\n");
            out.write("\t\treturn DateFormat.DEFAULT;\n");
            out.write("\t}\n");
        }

        out.write("\n}\n");

        out.close();
    }

    private void generateDateFormatGetterSetter(BufferedWriter out, String property) throws IOException {
        out.write("\n");
        out.write("\tpublic String get" + Character.toUpperCase(property.charAt(0)) + property.substring(1) + "() {\n");
        out.write("\t\treturn " + property + ";\n");
        out.write("\t}\n");
        out.write("\n");
        out.write("\tpublic void set" + Character.toUpperCase(property.charAt(0)) + property.substring(1) + "(String " + property + ") {\n");
        if (property.equals("pattern"))
        	out.write("\t\tthis." + property + " = " + property + ";\n");
        else
        	out.write("\t\tthis." + property + " = " + property + ".toUpperCase();\n");
        out.write("\t}\n");
    }

    private void generateEntityToNowHelperClass(Entity theEntity, Attribute theAttribute) throws IOException {
        File baseClassFile = new File(tagDirectory, "/" + theEntity.getUnqualifiedLabel() + Character.toUpperCase(theAttribute.getLabel().charAt(0)) + theAttribute.getLabel().substring(1) + "ToNow.java");
        FileWriter fstream = new FileWriter(baseClassFile);
        BufferedWriter out = new BufferedWriter(fstream);
        out.write("package " + packagePrefix + "." + theEntity.getUnqualifiedLowerLabel() + ";\n");

        out.write("\nimport javax.servlet.jsp.JspException;\n");
        out.write("import javax.servlet.jsp.JspTagException;\n");
        out.write("import javax.servlet.jsp.tagext.Tag;\n\n");
        
        out.write("import org.apache.logging.log4j.Logger;\n");
        out.write("import org.apache.logging.log4j.LogManager;\n");
        
        if (theAttribute.isDateTime()){
        	out.write("import java.util.Date;\n");
        }
        if (theEntity.hasTimestamp())
            out.write("import java.sql.Timestamp;\n");
        
        out.write("\nimport " + packagePrefix + "." + projectName + "TagSupport;\n");

        out.write("\n@SuppressWarnings(\"serial\")\n");
        out.write("public class " + theEntity.getUnqualifiedLabel() + theAttribute.getUpperLabel() + "ToNow extends " + projectName + "TagSupport {\n\n");
        out.write("\tprivate static final Logger log = LogManager.getLogger("+theEntity.getUnqualifiedLabel()+ theAttribute.getUpperLabel() +"ToNow.class);\n\n");

        out.write("\n\tpublic int doStartTag() throws JspException {\n");
        out.write("\t\ttry {\n");
        out.write("\t\t\t" + theEntity.getUnqualifiedLabel() + " the" + theEntity.getUnqualifiedLabel() + " = (" + theEntity.getUnqualifiedLabel() + ")findAncestorWithClass(this, " + theEntity.getUnqualifiedLabel() + ".class);\n");
        out.write("\t\t\tthe" + theEntity.getUnqualifiedLabel() + ".set" + theAttribute.getUpperLabel() + "ToNow( );\n");
        out.write("\t\t} catch (Exception e) {\n");
        
        out.write("\t\t\tlog.error(\" Can't find enclosing " + theEntity.getUnqualifiedLabel() + " for " + theAttribute.getLabel() + " tag \", e);\n");
        
        out.write("\t\t\tfreeConnection();\n\n");

        out.write("\t\t\tTag parent = getParent();\n");
        out.write("\t\t\tif(parent != null){\n");
        out.write("\t\t\t\tpageContext.setAttribute(\"tagError\", true);\n");
        out.write("\t\t\t\tpageContext.setAttribute(\"tagErrorException\", e);\n");
        out.write("\t\t\t\tpageContext.setAttribute(\"tagErrorMessage\", \"Can't find enclosing " + theEntity.getUnqualifiedLabel() + " for " + theAttribute.getLabel() + " tag \");\n");
        out.write("\t\t\t\treturn parent.doEndTag();\n");
        out.write("\t\t\t}else{\n");
        out.write("\t\t\t\tthrow new JspTagException(\"Error: Can't find enclosing " + theEntity.getUnqualifiedLabel() + " for " + theAttribute.getLabel() + " tag \");\n");
        out.write("\t\t\t}\n\n");
        
        out.write("\t\t}\n");
        out.write("\t\treturn SKIP_BODY;\n");
        out.write("\t}\n");

        out.write("\n\tpublic " + theAttribute.getType() + " get" + theAttribute.getUpperLabel() + "() throws JspException {\n");
        out.write("\t\ttry {\n");
        out.write("\t\t\t" + theEntity.getUnqualifiedLabel() + " the" + theEntity.getUnqualifiedLabel() + " = (" + theEntity.getUnqualifiedLabel()
                + ")findAncestorWithClass(this, " + theEntity.getUnqualifiedLabel() + ".class);\n");
        out.write("\t\t\treturn the" + theEntity.getUnqualifiedLabel() + ".get" + theAttribute.getUpperLabel() + "();\n");
        out.write("\t\t} catch (Exception e) {\n\n");
        
        out.write("\t\t\tlog.error(\"Can't find enclosing " + theEntity.getLabel() + " for " + theAttribute.getLabel() + " tag \", e);\n\n");
        
        out.write("\t\t\tfreeConnection();\n\n");

        out.write("\t\t\tTag parent = getParent();\n");
        out.write("\t\t\tif(parent != null){\n");
        out.write("\t\t\t\tpageContext.setAttribute(\"tagError\", true);\n");
        out.write("\t\t\t\tpageContext.setAttribute(\"tagErrorException\", e);\n");
        out.write("\t\t\t\tpageContext.setAttribute(\"tagErrorMessage\", \"Can't find enclosing " + theEntity.getUnqualifiedLabel() + " for " + theAttribute.getLabel() + " tag \");\n");
        out.write("\t\t\t\tparent.doEndTag();\n");
        out.write("\t\t\t\treturn null;\n");
        out.write("\t\t\t}else{\n");
        out.write("\t\t\t\tthrow new JspTagException(\"Error: Can't find enclosing " + theEntity.getLabel() + " for " + theAttribute.getLabel() + " tag \");\n");
        out.write("\t\t\t}\n\n");
        
        out.write("\t\t}\n");
        out.write("\t}\n");

        out.write("\n\tpublic void set" + theAttribute.getUpperLabel() + "() throws JspException {\n");
        out.write("\t\ttry {\n");
        out.write("\t\t\t" + theEntity.getUnqualifiedLabel() + " the" + theEntity.getUnqualifiedLabel() + " = (" + theEntity.getUnqualifiedLabel()
                + ")findAncestorWithClass(this, " + theEntity.getUnqualifiedLabel() + ".class);\n");
        out.write("\t\t\tthe" + theEntity.getUnqualifiedLabel() + ".set" + theAttribute.getUpperLabel() + "ToNow( );\n");
        out.write("\t\t} catch (Exception e) {\n\n");
        
        out.write("\t\t\tlog.error(\"Can't find enclosing " + theEntity.getUnqualifiedLabel() + " for " + theAttribute.getLabel() + " tag \", e);\n\n");
        
        out.write("\t\t\tfreeConnection();\n\n");

        out.write("\t\t\tTag parent = getParent();\n");
        out.write("\t\t\tif(parent != null){\n");
        out.write("\t\t\t\tpageContext.setAttribute(\"tagError\", true);\n");
        out.write("\t\t\t\tpageContext.setAttribute(\"tagErrorException\", e);\n");
        out.write("\t\t\t\tpageContext.setAttribute(\"tagErrorMessage\", \"Can't find enclosing " + theEntity.getUnqualifiedLabel() + " for " + theAttribute.getLabel() + " tag \");\n");
        out.write("\t\t\t\tparent.doEndTag();\n");
        out.write("\t\t\t}else{\n");
        out.write("\t\t\t\tthrow new JspTagException(\"Error: Can't find enclosing " + theEntity.getUnqualifiedLabel() + " for " + theAttribute.getLabel() + " tag \");\n");
        out.write("\t\t\t}\n\n");
        
        out.write("\t\t}\n");
        out.write("\t}\n");
        out.write("}");

        out.close();
    }

    private void generateEntityUploadServlet(Entity theEntity) throws IOException {
        if ((theEntity.getPrimaryKeyAttributes() == null || theEntity.getPrimaryKeyAttributes().size() == 0) && (theEntity.getSubKeyAttributes() == null || theEntity.getSubKeyAttributes().size() == 0))
            return;

        File baseClassFile = new File(tagDirectory, "/" + theEntity.getUnqualifiedLabel() + "UploadServlet.java");
        FileWriter fstream = new FileWriter(baseClassFile);
        BufferedWriter out = new BufferedWriter(fstream);

        out.write("package " + packagePrefix + "." + theEntity.getUnqualifiedLowerLabel() + ";\n\n");
        out.write("import java.io.IOException;\n");
        out.write("import java.sql.Connection;\n");
        out.write("import java.sql.PreparedStatement;\n");
        out.write("import java.sql.ResultSet;\n");
        out.write("import java.sql.SQLException;\n\n");

        out.write("import javax.naming.InitialContext;\n");
        out.write("import javax.servlet.ServletException;\n");
        out.write("import javax.servlet.annotation.MultipartConfig;\n");
        out.write("import javax.servlet.annotation.WebServlet;\n");
        out.write("import javax.servlet.http.HttpServlet;\n");
        out.write("import javax.servlet.http.HttpServletRequest;\n");
        out.write("import javax.servlet.http.HttpServletResponse;\n");
        out.write("import javax.servlet.http.Part;\n");
        out.write("import javax.sql.DataSource;\n\n");

        out.write("import org.apache.logging.log4j.LogManager;\n");
        out.write("import org.apache.logging.log4j.Logger;\n");
        out.write("import " + packagePrefix + ".Sequence;\n");
        out.write("\n");
        
        out.write("@WebServlet(\"/" + theEntity.getUpperLabel() + "UploadServlet\")\n");
        out.write("@MultipartConfig(fileSizeThreshold=1024*1024*10, 	// 10 MB\n");
        out.write("                 maxFileSize=1024*1024*50,      	// 50 MB\n");
        out.write("                 maxRequestSize=1024*1024*100)   	// 100 MB\n");
        out.write("public class " + theEntity.getUnqualifiedLabel() + "UploadServlet extends HttpServlet {\n");
        out.write("\tprivate static final long serialVersionUID = 1L;\n");
        out.write("\tprivate static final Logger log = LogManager.getLogger(" + theEntity.getUnqualifiedLabel() + "UploadServlet.class);\n");
        out.write("\tprotected DataSource theDataSource = null;\n\n");
        
        out.write("\tpublic " + theEntity.getUnqualifiedLabel() + "UploadServlet() {\n");
        out.write("\t\tsuper();\n");
        out.write("\t}\n\n");

        out.write("\tprotected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {\n");
        out.write("\t\tdoPost(request, response);\n");
        out.write("\t}\n\n");

        out.write("\tprotected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {\n");
        /*
         * declare "simple" attributes
         */
        for (Attribute attribute : theEntity.getAttributes()) {
            if (attribute.isImage() || attribute.isBinaryDomain())
            	continue;
            if (attribute.isPrimary() && !attribute.isForeign()) {
                out.write("\t\t" + attribute.getType() + " " + attribute.getLabel() + " = " + attribute.getInitializer() + ";\n");
                out.write("\t\tif (request.getParameter(\"" + attribute.getLabel() + "\") != null)\n");
                out.write("\t\t\t" + attribute.getLabel() + " = " + attribute.parseUploadValue() + ";\n");
            	
            } else {
                out.write("\t\t" + (attribute.getDomain() == null ? attribute.getType() : attribute.getDomain().getJavaType()) + " " + attribute.getLabel() + " = " + attribute.parseUploadValue() + ";\n");            	
            }
        }
        out.write("\n");
        
        /*
         * declare streamed attributes
         */
        Attribute imageAttribute = null; //we'll need this multiple times later
        for (Attribute attribute : theEntity.getAttributes()) {
            if (attribute.isImage() || attribute.isBinaryDomain()) {
            	out.write("\t\tPart " + attribute.getLabel() + " = null;\n");
            	imageAttribute = attribute;
            }
        }
        out.write("\n");

        /*
         * snag the streamed attributes
         */
        out.write("\t\tfor (Part part : request.getParts()) {\n");
        out.write("\t\t\tPayload payload = getPayload(part);\n");
        out.write("\t\t\tif (payload == null)\n");
        out.write("\t\t\t\tcontinue;\n");
        out.write("\t\t\tlog.debug(payload.name + \" file: \" + payload.file);\n");
        out.write("\n");
        out.write("\t\t\tswitch(payload.name) {\n");
        for (Attribute attribute : theEntity.getAttributes()) {
            if (attribute.isImage() || attribute.isBinaryDomain()) {
            	out.write("\t\t\tcase \"" + attribute.getLabel() + "\":\n");            	
            	out.write("\t\t\t\t" + attribute.getLabel() + " = part;\n");            	
            	out.write("\t\t\t\t" + attribute.getLabel() + "Name = payload.file;\n");            	
            	out.write("\t\t\t\tbreak;\n");
            }
        }
        out.write("\t\t\t}\n");
        out.write("\t\t}\n");
        out.write("\n");
        
        /*
         * add/update the tuple
         */
        out.write("\t\ttry {\n");
        out.write("\t\t\tConnection conn = getConnection();\n");
        out.write("\t\t\tPreparedStatement stmt = null;\n");
        out.write("\t\t\tboolean recordExists = false;\n");
        out.write("\n");
        
        /*
         * check if this is an add or an update
         */
        out.write("\t\t\tstmt = conn.prepareStatement(\"select count(*) from " + theEntity.getSchema().getSqlLabel() + "." + theEntity.getSqlLabel() + " where ");
        String setBuffer = "";
        int attrCount = 0;
        for (Attribute attribute : theEntity.getPrimaryKeyAttributes()) {
        	setBuffer += "\t\t\tstmt." + attribute.getSQLMethod(false) + "(" + ++attrCount + "," + attribute.getLabel() + ");\n";
        	if (attrCount > 1) {
        		out.write(" and ");
        	}
        	out.write(attribute.getSqlLabel() + " = ?");
        }
        out.write("\");\n");
        out.write(setBuffer);
        out.write("\t\t\tResultSet rs = stmt.executeQuery();\n");
        out.write("\t\t\twhile (rs.next()) {\n");
        out.write("\t\t\t\tint count = rs.getInt(1);\n");
        out.write("\t\t\t\trecordExists = count == 1;\n");
        out.write("\t\t\t}\n");
        out.write("\t\t\trs.close();\n");
        out.write("\t\t\tstmt.close();\n");
        out.write("\n");
        
        out.write("\t\t\tif (recordExists) {\n");
        /*
         * doing an update
         */
        out.write("\t\t\t\tlog.debug(\"image upload: \" + " + imageAttribute.getLabel() + ");\n");
        out.write("\t\t\t\tint paramCount = 0;\n");
        out.write("\t\t\t\tstmt = conn.prepareStatement(\"update " + theEntity.getSchema().getSqlLabel() + "." + theEntity.getSqlLabel() + " set ");
        boolean firstKey = true;
        boolean firstNonKey = true;
        String nonKeySetBuffer = "";
        String keySetBuffer = "";
        String nonKeyAssignBuffer = "";
        String keyAssignBuffer = "";
        for (Attribute attribute : theEntity.getAttributes()) {
        	if (attribute.isImage() || attribute.isBinaryDomain())
        		continue;
        	if (attribute.isPrimary()) {
        		if (firstKey) {
        			firstKey = false;
        		} else {
        			keyAssignBuffer += " and ";
        		}
        		keyAssignBuffer += attribute.getSqlLabel() + " = ?";
        		keySetBuffer += "\t\t\t\tstmt." + attribute.getSQLMethod(false) + "(++paramCount," + attribute.getLabel() + ");\n";
        	} else {
           		if (firstNonKey) {
           			firstNonKey = false;
        		} else {
        			nonKeyAssignBuffer += ", ";
        		}
           		nonKeyAssignBuffer += attribute.getSqlLabel() + " = ?";
        		nonKeySetBuffer += "\t\t\t\tstmt." + attribute.getSQLMethod(false) + "(++paramCount," + attribute.getLabel() + ");\n";
        	}
        }
        out.write(nonKeyAssignBuffer + " where " + keyAssignBuffer + "\");\n");
        out.write(nonKeySetBuffer);
        out.write(keySetBuffer);
        out.write("\t\t\t\tstmt.execute();\n");
        out.write("\t\t\t\tstmt.close();\n");
        out.write("\t\t\t\tif (" + imageAttribute.getLabel() + " != null) {\n");
        out.write("\t\t\t\t\tparamCount = 0;\n");
        out.write("\t\t\t\t\tstmt = conn.prepareStatement(\"update " + theEntity.getSchema().getSqlLabel() + "." + theEntity.getSqlLabel() + " set ");
        out.write(imageAttribute.getLabel() + "=?," + imageAttribute.getLabel() + "_name=? where " + keyAssignBuffer + "\");\n");
        out.write("\t\t\t\t\tstmt.setBinaryStream(++paramCount, " + imageAttribute.getLabel() + ".getInputStream(), " + imageAttribute.getLabel() + ".getSize());\n");
        out.write("\t\t\t\t\tstmt.setString(++paramCount, " + imageAttribute.getLabel() + "Name);\n");
        out.write(keySetBuffer.replaceAll("stmt\\.", "\tstmt."));
        out.write("\t\t\t\t\tstmt.execute();\n");
        out.write("\t\t\t\t\tstmt.close();\n");
        out.write("\t\t\t\t}\n");
        out.write("\t\t\t} else {\n");
        /*
         * doing an add
         */
        out.write("\t\t\t\tint paramCount = 0;\n");
        out.write("\t\t\t\tstmt = conn.prepareStatement(\"insert into " + theEntity.getSchema().getSqlLabel() + "." + theEntity.getSqlLabel() + "(");
        boolean firstAttr = true;
        String attrList = "";
        String mapList = "";
        String assignList = "";
        for (Attribute attribute : theEntity.getAttributes()) {
        	if (attribute == imageAttribute) {
    			attrList += ",";
    			mapList += ",";
    			assignList += "\t\t\t\tstmt.setBinaryStream(++paramCount, " + imageAttribute.getLabel() + ".getInputStream(), " + imageAttribute.getLabel() + ".getSize());\n";
        	} else {
        		if (!firstAttr) {
        			attrList += ",";
        			mapList += ",";
        		} else {
        			firstAttr = false;
        		}
        		if (attribute.isPrimary() && !attribute.isForeign())
            		assignList += "\t\t\t\tstmt." + attribute.getSQLMethod(false) + "(++paramCount,(seqnum > 0 ? seqnum : Sequence.generateID()));\n";
        		else
        			assignList += "\t\t\t\tstmt." + attribute.getSQLMethod(false) + "(++paramCount," + attribute.getLabel() + ");\n";
        	}
        	attrList += attribute.getSqlLabel();
        	mapList += "?";
        }
        out.write(attrList +") values(" + mapList + ")\");\n");
        out.write(assignList);
        out.write("\t\t\t\tstmt.execute();\n");
        out.write("\t\t\t\tstmt.close();\n");
        out.write("\t\t\t}\n");
        out.write("\n");
        out.write("\t\t\tconn.close();\n");
        out.write("\t\t} catch (SQLException e) {\n");
        out.write("\t\t\tlog.error(\"Failed to make database connection: \" + e);\n");
        out.write("\t\t}\n");
        out.write("\n");
        
        /*
         * tidy up the post method
         */
        out.write("\t\tresponse.sendRedirect(request.getContextPath() + (request.getParameter(\"redirect\") != null ? request.getParameter(\"redirect\") : \"/" + theEntity.getLowerLabel() + "/list.jsp\"));\n");
        out.write("\t}\n\n");
        
        /*
         * Finally do all the helper elements
         */
 
        out.write("\tprivate Payload getPayload(Part part) {\n");
        out.write("\t\tString contentDisp = part.getHeader(\"content-disposition\");\n");
        out.write("\t\tString[] tokens = contentDisp.split(\";\");\n");
        out.write("\t\tString name = null;\n");
        out.write("\t\tString file = null;\n");
        out.write("\t\tfor (String token : tokens) {\n");
        out.write("\t\t\tif (token.trim().startsWith(\"name\")) {\n");
        out.write("\t\t\t\tname =  token.substring(token.indexOf(\"=\") + 2, token.length()-1);\n");
        out.write("\t\t\t}\n");
        out.write("\t\t\tif (token.trim().startsWith(\"filename\")) {\n");
        out.write("\t\t\t\tfile =  token.substring(token.indexOf(\"=\") + 2, token.length()-1);\n");
        out.write("\t\t\t}\n");
        out.write("\t\t}\n");
        out.write("\n");
        out.write("\t\tif (file == null || file.length() == 0)\n");
        out.write("\t\t\treturn null;\n");
        out.write("\n");
        out.write("\t\treturn new Payload(name, file);\n");
        out.write("\t}\n");
        out.write("\n");
        out.write("\tpublic DataSource getDataSource() {\n");
        out.write("\t\tif (theDataSource == null) try {\n");
        out.write("\t\t\ttheDataSource = (DataSource)new InitialContext().lookup(\"java:/comp/env/jdbc/" + projectName + "\");\n");
        out.write("\t\t} catch (Exception e) {\n");
        out.write("\t\t\tlog.error(\"Error in database initialization\", e);\n");
        out.write("\t\t}\n");
        out.write("\n");
        out.write("\treturn theDataSource;\n");
        out.write("\t}\n");
        out.write("\n");
        out.write("\tpublic Connection getConnection() throws SQLException {\n");
        out.write("\t\treturn getDataSource().getConnection();\n");
        out.write("\t}\n");
        out.write("\n");
        out.write("\tclass Payload {\n");
        out.write("\t\tString name = null;\n");
        out.write("\t\tString file = null;\n");
        out.write("\n");
        out.write("\t\tPayload(String name, String file) {\n");
        out.write("\t\t\tthis.name = name;\n");
        out.write("\t\t\tthis.file = file;\n");
        out.write("\t\t}\n");
        out.write("\t}\n");
        out.write("\n");
        
        // close out class
        out.write("\n}\n");
        out.close();
    }
    
    private void generateUploadDoStartTag(Entity theEntity, BufferedWriter out) throws IOException {
        int attrSeq = 0;
        int keySeq = 0;
        StringBuffer keyParamBuffer = new StringBuffer();

        Attribute keyAttribute = (theEntity.getSubKeyAttributes().size() == 0 ? theEntity.getPrimaryKeyAttributes().firstElement() : theEntity.getSubKeyAttributes().firstElement());

        StringBuffer resultBuffer = new StringBuffer();
        StringBuffer paramBuffer = new StringBuffer();
        StringBuffer queryBuffer = new StringBuffer();

        out.write("\n\tpublic int doStartTag() throws JspException {\n");
        out.write("\t\ttry {\n");
        
        generateParentEntityReferences(out, theEntity, "\t\t\t");

        // generate context test for a containing iterator, if it's there pull primary key values from it preferentially
        out.write("\t\t\t" + theEntity.getLabel() + "Iterator the" + theEntity.getLabel() + "Iterator = ("
                + theEntity.getLabel() + "Iterator)findAncestorWithClass(this, " + theEntity.getLabel() + "Iterator.class);\n\n");
        out.write("\t\t\t" + "if (the" + theEntity.getLabel() + "Iterator != null) {\n");
           for (int j = 0; j < theEntity.getPrimaryKeyAttributes().size(); j++) {
               out.write("\t\t\t\t" + theEntity.getPrimaryKeyAttributes().elementAt(j).getLabel() + " = the" + theEntity.getLabel() + "Iterator.get"
                       + Character.toUpperCase(theEntity.getPrimaryKeyAttributes().elementAt(j).getLabel().charAt(0))
                       + theEntity.getPrimaryKeyAttributes().elementAt(j).getLabel().substring(1) + "();\n");
           }
        out.write("\t\t\t}\n\n");

        // Logic for instance context (i.e., what parents already exist in the run-time environment.  This needs to support
        // creation of new instances for roots of instance trees (the no active parents case), entry via a navigation path
        // (the one active parent case), or instantiation of a new instance somewhere other than a root (the all parents
        // active case).
        
        // case for no active parents
        out.write("\t\t\tif (the" + theEntity.getLabel() + "Iterator == null");
        for (int j = 0; j < theEntity.getParents().size(); j++) {
            Entity theParentEntity = theEntity.getParents().elementAt(j).getSourceEntity();
            out.write(" && the" + theParentEntity.getLabel() + " == null");
        }
        out.write(" && " + keyAttribute.getLabel() + " == " + keyAttribute.getInitializer() + ") {\n");
//        out.write("\t\t\t\t// no " + keyAttribute.getLabel() + " was provided - the default is to assume that it is a new "
//                + theEntity.getLabel() + " and to generate a new " + keyAttribute.getLabel() + "\n");
//        out.write("\t\t\t\t" + keyAttribute.getLabel() + " = " + keyAttribute.getDefaultValue() + ";\n");
//        out.write("\t\t\t\tlog.debug(\"generating new " + theEntity.getLabel() + " \" + " + keyAttribute.getLabel()
//                + ");\n");
//        out.write("\t\t\t\tinsertEntity();\n");
        out.write("\t\t\t\tHttpServletRequest theRequest = (HttpServletRequest) pageContext.getRequest();\n");
        out.write("\t\t\t\tboolean isMultipart = ServletFileUpload.isMultipartContent(theRequest);\n");
        out.write("\t\t\t\t// Create a factory for disk-based file items\n");
        out.write("\t\t\t\tFileItemFactory factory = new DiskFileItemFactory();\n");
        out.write("\n");
        out.write("\t\t\t\t// Create a new file upload handler\n");
        out.write("\t\t\t\tServletFileUpload upload = new ServletFileUpload(factory);\n");
        out.write("\n");
        out.write("\t\t\t\t// Parse the request\n");
        out.write("\t\t\t\ttry {\n");
        out.write("\t\t\t\t\tString fileName = null;\n");
        out.write("\t\t\t\t\tList items = upload.parseRequest(theRequest);\n");
        out.write("\t\t\t\t\tInputStream uploadedStream = null;\n");
        out.write("\t\t\t\t\tInputStream uploadedImageStream = null;\n");
        out.write("\n");
        out.write("\t\t\t\t\t// Process the uploaded items\n");
        out.write("\t\t\t\t\tIterator iter = items.iterator();\n");
        out.write("\t\t\t\t\twhile (iter.hasNext()) {\n");
        out.write("\t\t\t\t\t\tFileItem item = (FileItem) iter.next();\n");
        out.write("\n");
        out.write("\t\t\t\t\t\tif (item.isFormField()) {\n");
        out.write("\t\t\t\t\t\t\tString name = item.getFieldName();\n");
        out.write("\t\t\t\t\t\t\tString value = item.getString();\n");
        out.write("\t\t\t\t\t\t\tlog.debug(\"field: \" + name + \"\tvalue: \" + value);\n");
        for (int i = 0; i < theEntity.getAttributes().size(); i++) {
            Attribute theAttribute = theEntity.getAttributes().elementAt(i);
            if (!theAttribute.isImage() && !theAttribute.isBinaryDomain()) {
              out.write("\t\t\t\t\t\t\tif (name.equals(\"" + theAttribute.getLabel() + "\")) {\n");
              // out.write("\t\t\t\t\t\t\t\tcommitNeeded = true;\n");
              out.write("\t\t\t\t\t\t\t\tif (value == null || value.length() == 0)\n");
              out.write("\t\t\t\t\t\t\t\t\t" + theAttribute.getLabel() + " = " + theAttribute.getDefaultValue() + ";\n");
              out.write("\t\t\t\t\t\t\t\telse\n");
              if (theAttribute.isBoolean())
                  out.write("\t\t\t\t\t\t\t\t\t" + theAttribute.getLabel() + " = value.equals(\"on\");\n");
              else
                  out.write("\t\t\t\t\t\t\t\t\t" + theAttribute.getLabel() + " = " + theAttribute.parseValue("value") + ";\n");
              out.write("\t\t\t\t\t\t\t}\n");
            }
        }
        out.write("\t\t\t\t\t\t\tif (name.equals(\"submit\"))\n");
        out.write("\t\t\t\t\t\t\t\tcommitNeeded = value.equals(\"Save\");\n");
        out.write("\t\t\t\t\t\t} else {\n");
        out.write("\t\t\t\t\t\t\tString fieldName = item.getFieldName();\n");
        out.write("\t\t\t\t\t\t\tString contentType = item.getContentType();\n");
        out.write("\t\t\t\t\t\t\tfileName = item.getName();\n");
        out.write("\t\t\t\t\t\t\tlog.debug(\"field: \" + fieldName + \"\tfile: \" + fileName + \"\tcontentType: \" + contentType);\n");
        for (int i = 0; i < theEntity.getAttributes().size(); i++) {
            Attribute theAttribute = theEntity.getAttributes().elementAt(i);
            if (theAttribute.isImage() ||theAttribute.isBinaryDomain()) {
              out.write("\t\t\t\t\t\t\tif (fieldName.equals(\"" + theAttribute.getLabel() + "\") && contentType != null) {\n");
              // out.write("\t\t\t\t\t\t\t\tcommitNeeded = true;\n");
              out.write("\t\t\t\t\t\t\t\t" + theAttribute.getLabel() + "Item = item;\n");
              out.write("\t\t\t\t\t\t\t\tif (fileName.contains(\"\\\\\"))\n");
              out.write("\t\t\t\t\t\t\t\t\t" + theAttribute.getLabel() + "Name = fileName.substring(fileName.lastIndexOf('\\\\')+1);\n");
              out.write("\t\t\t\t\t\t\t\telse\n");
              out.write("\t\t\t\t\t\t\t\t\t" + theAttribute.getLabel() + "Name = fileName;\n");
              if (theAttribute.isImage()) {
                  out.write("\t\t\t\t\t\t\t\tImage theImage = ImageIO.read(new BufferedInputStream(" + theAttribute.getLabel() + "Item.getInputStream()));\n");
                  out.write("\t\t\t\t\t\t\t\tByteArrayOutputStream baos = new ByteArrayOutputStream();\n");
                  out.write("\t\t\t\t\t\t\t\tImageIO.write((RenderedImage) theImage, \"jpg\", baos);\n");
                  out.write("\t\t\t\t\t\t\t\t" + theAttribute.getLabel() + " = baos.toByteArray();\n");
              }
              out.write("\t\t\t\t\t\t\t}\n");
            }
        }
        out.write("\t\t\t\t\t\t}\n");
        out.write("\t\t\t\t\t}\n");

        out.write("\t\t\t\t\tint count = 0;\n");
        out.write("\t\t\t\t\ttry {\n"
                + "\t\t\t\t\t\tPreparedStatement stat = getConnection().prepareStatement(\"SELECT count(*)");
        out.write(" from " + theSchema.getSqlLabel() + "." + theEntity.getSqlLabel() + " where 1=1\"\n");
        for (int j = 0; j < theEntity.getPrimaryKeyAttributes().size(); j++) {
            Attribute theAttribute = theEntity.getPrimaryKeyAttributes().elementAt(j);
            out.write("\t\t\t\t\t\t\t\t\t+ \" and " + theAttribute.getSqlLabel() + " = ?\"\n");
            keyParamBuffer.append("\t\t\t\t\t\tstat." + theAttribute.getSQLMethod(false) + "(" + (j+1) + ", " + theAttribute.getLabel() + ");\n");
        }
        out.write("\t\t\t\t\t\t\t\t\t);\n\n");
        out.write(keyParamBuffer.toString());
        out.write("\t\t\t\t\t\tResultSet crs = stat.executeQuery();\n"
                + "\n"
                + "\t\t\t\t\t\tif (crs.next()) {\n");
        
        out.write("\t\t\t\t\t\t\tcount = crs.getInt(1);\n");
        out.write("\t\t\t\t\t\t}\n");
        out.write("\t\t\t\t\t\tcrs.close();\n");
        out.write("\t\t\t\t\t\tstat.close();\n");
        out.write("\t\t\t\t\t} catch (SQLException e) {\n");
                
        out.write("\t\t\t\t\t\tlog.error(\"JDBC error generating " + theEntity.getLabel() + " object count\", e);\n");
        
        out.write("\t\t\t\t\t\tfreeConnection();\n");
        out.write("\t\t\t\t\t\tclearServiceState();\n\n");
        
        out.write("\t\t\t\t\t\tTag parent = getParent();\n");
        out.write("\t\t\t\t\t\tif(parent != null){\n");
        out.write("\t\t\t\t\t\t\tpageContext.setAttribute(\"tagError\", true);\n");
        out.write("\t\t\t\t\t\t\tpageContext.setAttribute(\"tagErrorException\", e);\n");
        out.write("\t\t\t\t\t\t\tpageContext.setAttribute(\"tagErrorMessage\", \"Error: JDBC error generating " + theEntity.getLabel() + " object count\");\n");
        out.write("\t\t\t\t\t\t\treturn parent.doEndTag();\n");
        out.write("\t\t\t\t\t\t}else{\n");
        out.write("\t\t\t\t\t\t\tthrow new JspTagException(\"Error: JDBC error generating " + theEntity.getLabel() + " object count\");\n");
        out.write("\t\t\t\t\t\t}\n\n");
        
        out.write("\t\t\t\t\t} finally {\n");
        out.write("\t\t\t\t\t\tfreeConnection();\n");
        out.write("\t\t\t\t\t}\n");

        out.write("\t\t\t\t\tif (count == 0 && commitNeeded)\n");
        out.write("\t\t\t\t\t\tinsertEntity();\n");
        out.write("\t\t\t\t} catch (FileUploadException e) {\n\n");
        
        out.write("\t\t\t\t\tlog.error(\"Upload Exception\", e);\n\n");
        
        out.write("\t\t\t\t\tfreeConnection();\n");
        out.write("\t\t\t\t\tclearServiceState();\n\n");
        
        out.write("\t\t\t\t\tTag parent = getParent();\n");
        out.write("\t\t\t\t\tif(parent != null){\n");
        out.write("\t\t\t\t\t\tpageContext.setAttribute(\"tagError\", true);\n");
        out.write("\t\t\t\t\t\tpageContext.setAttribute(\"tagErrorException\", e);\n");
        out.write("\t\t\t\t\t\tpageContext.setAttribute(\"tagErrorMessage\", \"Upload Exception\");\n");
        out.write("\t\t\t\t\t\treturn parent.doEndTag();\n");
        out.write("\t\t\t\t\t}else{\n");
        out.write("\t\t\t\t\t\tthrow new JspException(\"Upload Exception\",e);\n");
        out.write("\t\t\t\t\t}\n\n");
        
        if (theEntity.hasImage()) {
            out.write("\t\t\t\t} catch (IOException e) {\n\n");
            out.write("\t\t\t\t\tlog.error(\"HasImage\", e);\n\n");
            
            out.write("\t\t\t\t\tfreeConnection();\n");
            out.write("\t\t\t\t\tclearServiceState();\n\n");
            
            out.write("\t\t\t\t\tTag parent = getParent();\n");
            out.write("\t\t\t\t\tif(parent != null){\n");
            out.write("\t\t\t\t\t\tpageContext.setAttribute(\"tagError\", true);\n");
            out.write("\t\t\t\t\t\tpageContext.setAttribute(\"tagErrorException\", e);\n");
            out.write("\t\t\t\t\t\tpageContext.setAttribute(\"tagErrorMessage\", \"HasImage\");\n");
            out.write("\t\t\t\t\t\treturn parent.doEndTag();\n");
            out.write("\t\t\t\t\t}else{\n");
            out.write("\t\t\t\t\t\tthrow new JspException(\"HasImage\",e);\n");
            out.write("\t\t\t\t\t}\n\n");
            
        }
        out.write("\t\t\t\t}\n");
        
        // case for one parent active among multiple parents - we iterate
        // through the parents, using each of them in turn as the only non-null
        // ancestor
        if (theEntity.getParents().size() > 1) {
            for (int i = 0; i < theEntity.getParents().size(); i++) {
                Entity theNavigationEntity = null;
                out.write("\t\t\t} else if (the" + theEntity.getLabel() + "Iterator == null && ");
                for (int j = 0; j < theEntity.getParents().size(); j++) {
                    Entity theSourceEntity = theEntity.getParents().elementAt(j).getSourceEntity();
                    if (j > 0)
                        out.write(" && ");
                    out.write("the" + theSourceEntity.getLabel() + " " + (i==j ? "!" : "=") + "= null");
                    if (i == j)
                        theNavigationEntity = theSourceEntity;
                }
                out.write(") {\n");
                
                out.write("\t\t\t\t// an " + keyAttribute.getLabel() + " was provided as an attribute - we need to load a "
                        + theEntity.getLabel() + " from the database\n");
                out.write("\t\t\t\tboolean found = false;\n");
                out.write("\t\t\t\tPreparedStatement stmt = getConnection().prepareStatement(\"select");
                paramBuffer = new StringBuffer();
                queryBuffer = new StringBuffer();
                resultBuffer = new StringBuffer();
                keySeq = 0;
                attrSeq = 0;
                boolean firstParam = true;
                for (int k = 0; k < theEntity.getAttributes().size(); k++) {
                    Attribute theAttribute = theEntity.getAttributes().elementAt(k);
                    if (theAttribute.isPrimary() && theNavigationEntity.isPrimaryReference(theAttribute)) {
                        paramBuffer.append((firstParam ? " " : " and ") + theAttribute.getSqlLabel() + " = ?");
                        queryBuffer.append("\t\t\t\tstmt."
                                + theAttribute.getSQLMethod(false)
                                + "("
                                + (keySeq + 1)
                                + ","
                                + theAttribute.getLabel()
                                + (theAttribute.isDateTime() ? " == null ? null : new java.sql."
                                        + (theAttribute.isTimestamp() ? "Timestamp" : "Date") + "(" + theAttribute.getLabel() + ".getTime())"
                                        : "") + ");\n");
                        keySeq++;
                        firstParam = false;
                    } else {
                        out.write((attrSeq == 0 ? " " : ",") + theAttribute.getSqlLabel());
                        resultBuffer.append("\t\t\t\t\tif (" + theAttribute.getLabel() + " == " + theAttribute.getInitializer() + ")\n");
                        resultBuffer.append("\t\t\t\t\t\t" + theAttribute.getLabel() + " = "
//                                + (theAttribute.getDomain() == null ? "" : "(" + theAttribute.getDomain().getLabel() + ") (Object) ")
                                + "rs." + theAttribute.getSQLMethod(true) + "(" + (attrSeq + 1) + ");\n");
                        attrSeq++;
                    }
                }
                out.write(" from " + theSchema.getSqlLabel()+ "." + theEntity.getSqlLabel() + " where");
                out.write(paramBuffer.toString());
                out.write("\");\n");
                out.write(queryBuffer.toString());
                out.write("\t\t\t\tResultSet rs = stmt.executeQuery();\n");
                out.write("\t\t\t\twhile (rs.next()) {\n");
                out.write(resultBuffer.toString());
                out.write("\t\t\t\t\tfound = true;\n");
                out.write("\t\t\t\t}\n");
                out.write("\t\t\t\trs.close();\n");
                out.write("\t\t\t\tstmt.close();\n");
                out.write("\n");
                out.write("\t\t\t\tif (!found) {\n");
                out.write("\t\t\t\t\tinsertEntity();\n");
                out.write("\t\t\t\t}\n");
            }
        }
        
        // case for all parents active - this just defaults to the presumption that if one of the single active parent
        // cases didn't trigger, we're in the all active parents case
        out.write("\t\t\t} else {\n");
        out.write("\t\t\t\t// an iterator or " + keyAttribute.getLabel() + " was provided as an attribute - we need to load a "
                + theEntity.getLabel() + " from the database\n");
        out.write("\t\t\t\tboolean found = false;\n");
        out.write("\t\t\t\tPreparedStatement stmt = getConnection().prepareStatement(\"select");
        paramBuffer = new StringBuffer();
        queryBuffer = new StringBuffer();
        resultBuffer = new StringBuffer();
        keySeq = 0;
        attrSeq = 0;
        for (int i = 0; i < theEntity.getAttributes().size(); i++) {
            Attribute theAttribute = theEntity.getAttributes().elementAt(i);
            if (theAttribute.isPrimary()) {
                paramBuffer.append((i == 0 ? " " : " and ") + theAttribute.getSqlLabel() + " = ?");
                queryBuffer.append("\t\t\t\tstmt."
                        + theAttribute.getSQLMethod(false)
                        + "("
                        + (keySeq + 1)
                        + ","
                        + theAttribute.getLabel()
                        + (theAttribute.isDateTime() ? " == null ? null : new java.sql."
                                + (theAttribute.isTimestamp() ? "Timestamp" : "Date") + "(" + theAttribute.getLabel() + ".getTime())"
                                : "") + ");\n");
                keySeq++;
            } else {
                out.write((attrSeq == 0 ? " " : ",") + theAttribute.getSqlLabel());
                resultBuffer.append("\t\t\t\t\tif (" + theAttribute.getLabel() + " == " + theAttribute.getInitializer() + ")\n");
                resultBuffer.append("\t\t\t\t\t\t" + theAttribute.getLabel() + " = "
//                        + (theAttribute.getDomain() == null ? "" : "(" + theAttribute.getDomain().getLabel() + ") (Object) ")
                        + "rs." + theAttribute.getSQLMethod(true) + "(" + (attrSeq + 1) + ");\n");
                attrSeq++;
            }
        }
        if (attrSeq == 0)
            out.write(" 1");
        out.write(" from " + theSchema.getSqlLabel() + "." + theEntity.getSqlLabel() + " where");
        out.write(paramBuffer.toString());
        out.write("\");\n");
        out.write(queryBuffer.toString());
        out.write("\t\t\t\tResultSet rs = stmt.executeQuery();\n");
        out.write("\t\t\t\twhile (rs.next()) {\n");
        out.write(resultBuffer.toString());
        out.write("\t\t\t\t\tfound = true;\n");
        out.write("\t\t\t\t}\n");
        out.write("\t\t\t\trs.close();\n");
        out.write("\t\t\t\tstmt.close();\n");
        out.write("\n");
        out.write("\t\t\t\tif (!found) {\n");
        out.write("\t\t\t\t\tinsertEntity();\n");
        out.write("\t\t\t\t}\n");
        out.write("\t\t\t}\n");
        
        // end of context cases
        
        out.write("\t\t} catch (SQLException e) {\n");
        
        out.write("\t\t\tlog.error(\"JDBC error retrieving " + keyAttribute.getLabel() + " \" + " + keyAttribute.getLabel() + ",e);\n");
        
        out.write("\t\t\tfreeConnection();\n");
        out.write("\t\t\tclearServiceState();\n\n");
        
        out.write("\t\t\tTag parent = getParent();\n");
        out.write("\t\t\tif(parent != null){\n");
        out.write("\t\t\t\tpageContext.setAttribute(\"tagError\", true);\n");
        out.write("\t\t\t\tpageContext.setAttribute(\"tagErrorException\", e);\n");
        out.write("\t\t\t\tpageContext.setAttribute(\"tagErrorMessage\", \"Error: JDBC error retrieving " + keyAttribute.getLabel() + " \" + " + keyAttribute.getLabel() + ");\n");
        out.write("\t\t\t\treturn parent.doEndTag();\n");
        out.write("\t\t\t}else{\n");
        out.write("\t\t\t\tthrow new JspTagException(\"Error: JDBC error retrieving " + keyAttribute.getLabel() + " \" + " + keyAttribute.getLabel() + ");\n");
        out.write("\t\t\t}\n\n");
        
        out.write("\t\t} finally {\n");
        out.write("\t\t\tfreeConnection();\n");
        out.write("\t\t}\n");
        out.write("\t\treturn EVAL_PAGE;\n");
        out.write("\t}\n");
    }
    
    private void generateUploadDoEndTag(Entity theEntity, BufferedWriter out) throws IOException {
        int attrSeq = 0;
        int keySeq = 0;

        StringBuffer paramBuffer = new StringBuffer();
        StringBuffer queryBuffer = new StringBuffer();

        out.write("\n\tpublic int doEndTag() throws JspException {\n");
        out.write("\t\ttry {\n");
        
        out.write("\t\t\tif(pageContext != null){\n");
        out.write("\t\t\t\tBoolean error = (Boolean) pageContext.getAttribute(\"tagError\");\n");
        out.write("\t\t\t\tif(error != null && error){\n\n");
        
        out.write("\t\t\t\t\tfreeConnection();\n");
        out.write("\t\t\t\t\tclearServiceState();\n\n");

        out.write("\t\t\t\t\tException e = (Exception) pageContext.getAttribute(\"tagErrorException\");\n");
        out.write("\t\t\t\t\tString message = (String) pageContext.getAttribute(\"tagErrorMessage\");\n\n");
        
        out.write("\t\t\t\t\tTag parent = getParent();\n");
        out.write("\t\t\t\t\tif(parent != null){\n");
        out.write("\t\t\t\t\t\treturn parent.doEndTag();\n");
        
        out.write("\t\t\t\t\t}else if(e != null && message != null){\n");
        out.write("\t\t\t\t\t\tthrow new JspException(message,e);\n");
        out.write("\t\t\t\t\t}else if(parent == null){\n");
        out.write("\t\t\t\t\t\tpageContext.removeAttribute(\"tagError\");\n");
        out.write("\t\t\t\t\t\tpageContext.removeAttribute(\"tagErrorException\");\n");
        out.write("\t\t\t\t\t\tpageContext.removeAttribute(\"tagErrorMessage\");\n");
        out.write("\t\t\t\t\t}\n");
        out.write("\t\t\t\t}\n");
        out.write("\t\t\t}\n");
        
        out.write("\t\t\tif (commitNeeded) {\n");
        out.write("\t\t\t\tPreparedStatement stmt = getConnection().prepareStatement(\"update " + theSchema.getSqlLabel() + "." + theEntity.getSqlLabel() + " set");
        paramBuffer = new StringBuffer();
        queryBuffer = new StringBuffer();
        attrSeq = 0;
        for (int i = 0; i < theEntity.getAttributes().size(); i++) {
            Attribute theAttribute = theEntity.getAttributes().elementAt(i);
            if (theAttribute.isPrimary()) {
                paramBuffer.append((keySeq == 0 ? " " : " and ") + theAttribute.getSqlLabel() + " = ?");
                keySeq++;
            } else {
                out.write((attrSeq == 0 ? " " : ", ") + theAttribute.getSqlLabel() + " = ?");
                if (theAttribute.isBinaryDomain()) {
                	queryBuffer.append("\t\t\t\tstmt.setBinaryStream(" + (attrSeq + 1) + ", " + theAttribute.getLabel() + "Item.getInputStream(), (int) " + theAttribute.getLabel() + "Item.getSize());\n");
                } else if (theAttribute.isImage()) {
                    queryBuffer.append("\t\t\t\tstmt.setBytes(" + (attrSeq + 1) + ", " + theAttribute.getLabel() + ");\n");
                } else {
	                queryBuffer.append("\t\t\t\tstmt."
	                        + theAttribute.getSQLMethod(false)
	                        + "("
	                        + (attrSeq + 1)
	                        + ","
	                        + (theAttribute.isByteA() ? "getBytes(" : "") + theAttribute.getLabel() + (theAttribute.isByteA() ? ")" : "")
	                        + (theAttribute.isDateTime() ? " == null ? null : new java.sql."
	                                + (theAttribute.isTimestamp() ? "Timestamp" : "Date") + "(" + theAttribute.getLabel() + ".getTime())"
	                                : "") + ");\n");
                }
                attrSeq++;
            }
        }
        out.write(" where" + paramBuffer.toString() + "\");\n");
        out.write(queryBuffer.toString());
        keySeq = 0;
        for (int i = 0; i < theEntity.getAttributes().size(); i++) {
            Attribute theAttribute = theEntity.getAttributes().elementAt(i);
            if (theAttribute.isPrimary()) {
                out.write("\t\t\t\tstmt."
                        + theAttribute.getSQLMethod(false)
                        + "("
                        + (attrSeq + keySeq + 1)
                        + ","
                        + theAttribute.getLabel()
                        + (theAttribute.isDateTime() ? " == null ? null : new java.sql."
                                + (theAttribute.isTimestamp() ? "Timestamp" : "Date") + "(" + theAttribute.getLabel() + ".getTime())"
                                : "") + ");\n");
                keySeq++;
            }
        }
        out.write("\t\t\t\tstmt.executeUpdate();\n");
        out.write("\t\t\t\tstmt.close();\n");
        out.write("\t\t\t}\n");
        out.write("\t\t} catch (SQLException e) {\n");

        out.write("\t\t\tlog.error(\"SQLException while writing to the user\", e);\n\n");
        
        out.write("\t\t\tfreeConnection();\n");
        out.write("\t\t\tclearServiceState();\n\n");
        
        out.write("\t\t\tTag parent = getParent();\n");
        out.write("\t\t\tif(parent != null){\n");
        out.write("\t\t\t\tpageContext.setAttribute(\"tagError\", true);\n");
        out.write("\t\t\t\tpageContext.setAttribute(\"tagErrorException\", e);\n");
        out.write("\t\t\t\tpageContext.setAttribute(\"tagErrorMessage\", \"SQLException while writing to the user\");\n");
        out.write("\t\t\t\treturn parent.doEndTag();\n");
        out.write("\t\t\t}else{\n");
        out.write("\t\t\t\tthrow new JspTagException(\"Error: SQLException while writing to the user\");\n");
        out.write("\t\t\t}\n\n");
        
        if (theEntity.hasBinaryDomainAttribute()) {
            out.write("\t\t} catch (IOException e) {\n");
            out.write("\t\t\tlog.error(\"IOException while writing to the user\", e);\n");
            
            out.write("\t\t\tfreeConnection();\n");
            out.write("\t\t\tclearServiceState();\n\n");
            
            out.write("\t\t\tTag parent = getParent();\n");
            out.write("\t\t\tif(parent != null){\n");
            out.write("\t\t\t\tpageContext.setAttribute(\"tagError\", true);\n");
            out.write("\t\t\t\tpageContext.setAttribute(\"tagErrorException\", e);\n");
            out.write("\t\t\t\tpageContext.setAttribute(\"tagErrorMessage\", \"Error: IOException while writing to the user\");\n");
            out.write("\t\t\t\treturn parent.doEndTag();\n");
            out.write("\t\t\t}else{\n");
            out.write("\t\t\tthrow new JspTagException(\"Error: IOException while writing to the user\");\n");
            out.write("\t\t\t}\n\n");
            
        }
        out.write("\t\t} finally {\n");
        out.write("\t\t\tfreeConnection();\n");
        out.write("\t\t}\n");
        out.write("\t\tclearServiceState();\n");
        out.write("\t\treturn super.doEndTag();\n");
        out.write("\t}\n");
    }
    
    public void generateBodyTagSupportClass() throws IOException {
        File baseClassFile = new File(packagePrefixDirectory, "/" + projectName + "BodyTagSupport.java");
        FileWriter fstream = new FileWriter(baseClassFile);
        BufferedWriter out = new BufferedWriter(fstream);
        out.write("package " + packagePrefix + ";\n" 
                + "\n" 
                + "import java.sql.Connection;\n" 
                + "import java.sql.SQLException;\n" 
                + "\n"
                + "import javax.naming.InitialContext;\n" 
                + "import javax.servlet.jsp.JspException;\n"
                + "import javax.servlet.jsp.JspTagException;\n" 
                + "import javax.servlet.jsp.tagext.BodyTagSupport;\n"
                + "import javax.sql.DataSource;\n"
                + "import org.apache.logging.log4j.Logger;\n"
                + "import org.apache.logging.log4j.LogManager;\n\n"
                //+ "import org.codehaus.jackson.annotate.JsonIgnore;\n"
                + "\n" 
                + "@SuppressWarnings(\"serial\")" 
                + "\n"
                + "public class " + projectName + "BodyTagSupport extends BodyTagSupport {\n" 
                + "\n"
                + "    protected DataSource theDataSource = null;\n" 
                + "    protected Connection theConnection = null;\n" 
                + "\n"
                + "    public " + projectName + "BodyTagSupport() {\n" 
                + "        super();\n" 
                + "    }\n" 
                + "\n" 
                + "    private static final Logger log = LogManager.getLogger("+ projectName + "BodyTagSupport.class);\n"
                + "\n"
                + "    @Override\n"
                + "    public int doEndTag() throws JspException {\n" 
                + "    	freeConnection();\n"
                + "    	return super.doEndTag();\n" 
                + "    }\n" 
                + "    \n"
                //+ "    @JsonIgnore\n"
                + "    public DataSource getDataSource() {\n"
                + "        if (theDataSource == null) try {\n"
                + "            theDataSource = (DataSource)new InitialContext().lookup(\"java:/comp/env/jdbc/" + projectName + "\");\n"
                + "        } catch (Exception e) {\n"
                + "            log.error(\"Error in database initialization\", e);\n" 
                + "        }\n" 
                + " \n"
                + "        return theDataSource;\n" 
                + "    }\n" 
                + "    \n"
                //+ "    @JsonIgnore\n"
                + "    public Connection getConnection() throws SQLException {\n" 
                + "        if (theConnection == null)\n"
                + "        	theConnection = getDataSource().getConnection();\n" 
                + "        return theConnection;\n" + "    }\n"
                + "    \n" 
                + "    public void freeConnection() throws JspTagException {\n" 
                + "     try {\n" 
                + "        if (theConnection != null)\n"
                + "        	theConnection.close();\n" 
                + "        theConnection = null;\n" 
                + "     } catch (SQLException e) {\n" 
                + "         log.error(\"JDBC error freeing connection\", e);\n"
                + "        theConnection = null;\n" 
                + "         throw new JspTagException(\"Error: JDBC error freeing connection\");\n" 
                + "     }\n"
                + "    }\n" 
                + "\n" 
                + "}");

        out.close();
    }

    public void generateTagSupportClass() throws IOException {
        File baseClassFile = new File(packagePrefixDirectory, "/" + projectName + "TagSupport.java");
        FileWriter fstream = new FileWriter(baseClassFile);
        BufferedWriter out = new BufferedWriter(fstream);
        out.write("package " + packagePrefix + ";\n" + "\n" + "import java.sql.Connection;\n" + "import java.sql.SQLException;\n" + "\n"
                + "import javax.naming.InitialContext;\n" + "import javax.servlet.jsp.JspException;\n"
                + "import javax.servlet.jsp.JspTagException;\n" 
                + "import javax.servlet.jsp.tagext.TagSupport;\n"
                + "import javax.sql.DataSource;\n" 
                + "import org.apache.logging.log4j.Logger;\n"
                + "import org.apache.logging.log4j.LogManager;\n\n"
                //+ "import org.codehaus.jackson.annotate.JsonIgnore;\n"
                + "\n" 
                + "@SuppressWarnings(\"serial\")"
                + "\n" 
                + "public class " + projectName + "TagSupport extends TagSupport {\n" 
                + "\n" 
                + "    protected DataSource theDataSource = null;\n"
                + "    protected Connection theConnection = null;\n"
                + "    private static final Logger log = LogManager.getLogger("+ projectName + "TagSupport.class);\n"
                + "\n" 
                + "    public " + projectName + "TagSupport() {\n"
                + "        super();\n" 
                + "    }\n" 
                + "\n" 
                + "    @Override\n" 
                + "    public int doEndTag() throws JspException {\n"
                + "		freeConnection();\n"
                + "    	return super.doEndTag();\n" 
                + "    }\n\n" 
                // + "    @JsonIgnore\n"
                + "    public DataSource getDataSource() {\n"
                + "        if (theDataSource == null) try {\n"
                + "            theDataSource = (DataSource)new InitialContext().lookup(\"java:/comp/env/jdbc/" + projectName + "\");\n"
                + "        } catch (Exception e) {\n"
                + "            log.error(\"Error in database initialization: \" + e);\n" 
                + "        }\n\n" 
                + "        return theDataSource;\n" 
                + "    }\n" 
                + "    \n"
                // + "    @JsonIgnore\n"
                + "    public Connection getConnection() throws SQLException {\n" 
                + "        if (theConnection == null)\n"
                + "        	theConnection = getDataSource().getConnection();\n" 
                + "        return theConnection;\n" + "    }\n\n"
                + "    public void freeConnection() throws JspTagException {\n" 
                + "     try {\n" 
                + "        if (theConnection != null)\n"
                + "         theConnection.close();\n" 
                + "        theConnection = null;\n" 
                + "     } catch (SQLException e) {\n" 
                + "         log.error(\"JDBC error freeing connection\", e);\n"
                + "        theConnection = null;\n" 
                + "         throw new JspTagException(\"Error: JDBC error freeing connection\");\n" 
                + "     }\n"
                + "    }\n" 
                + "\n" + "}\n");

        out.close();
    }

    public void generateSequenceClass() throws IOException {
        File baseClassFile = new File(packagePrefixDirectory, "/Sequence.java");
        FileWriter fstream = new FileWriter(baseClassFile);
        BufferedWriter out = new BufferedWriter(fstream);
        out.write("package " + packagePrefix + ";\n");
        out.write("\n");
        out.write("import java.sql.Connection;\n");
        out.write("import java.sql.PreparedStatement;\n");
        out.write("import java.sql.ResultSet;\n");
        out.write("import java.sql.SQLException;\n");
        out.write("import java.util.Hashtable;\n");
        out.write("import org.apache.logging.log4j.Logger;\n");
        out.write("import org.apache.logging.log4j.LogManager;\n");
        
        out.write("\n");
        out.write("import javax.servlet.jsp.JspException;\n");
        out.write("\n");
        out.write("@SuppressWarnings(\"serial\")\n");
        out.write("public class Sequence extends " + projectName + "TagSupport {\n");
        out.write("\tpublic static enum Driver {POSTGRESQL, ORACLE, MYSQL, SQLSERVER, UNKNOWN};\n");
        out.write("\tpublic static Driver theDriver = Driver.UNKNOWN;\n");
        out.write("\tstatic String theDriverString = null;\n");
        out.write("\tstatic Hashtable<String, Driver> theDriverHash = new Hashtable<String, Driver>();\n");
        out.write("\tString var = null;\n");
        out.write("\n");
        out.write("\tstatic {\n");
        out.write("\t\ttheDriverHash.put(\"PostgreSQL\", Driver.POSTGRESQL);\n");
        out.write("\t\ttheDriverHash.put(\"Microsoft SQL Server\", Driver.SQLSERVER);\n");
        out.write("\t}\n");
        
        out.write("\tprivate static final Logger log = LogManager.getLogger(Sequence.class);\n\n");

        out.write("\tpublic int doStartTag() throws JspException {\n");
        out.write("\t\tpageContext.setAttribute(var, generateID());\n");
        out.write("\t\treturn SKIP_BODY;\n");
        out.write("\t}\n");
        out.write("\n");
        out.write("\tpublic String getVar() {\n");
        out.write("\t\treturn var;\n");
        out.write("\t}\n");
        out.write("\n");
        out.write("\tpublic void setVar(String var) {\n");
        out.write("\t\tthis.var = var;\n");
        out.write("\t}\n");
        out.write("\n");
        out.write("\tstatic public int generateID() {\n");
        out.write("\t\tint nextInt = 0;\n");
        out.write("\t\tConnection conn = null;\n");
        out.write("\n");
        out.write("\t\ttry {\n");
        out.write("\t\t\tconn = (new Sequence()).getConnection();\n");
        out.write("\t\t\tif (theDriver == Driver.UNKNOWN) {\n");
        out.write("\t\t\t\ttheDriverString = conn.getMetaData().getDatabaseProductName();\n");
        out.write("\t\t\t\tlog.debug(\"'\" + theDriverString + \"'\");\n");
        out.write("\t\t\t\ttheDriver = theDriverHash.get(theDriverString);\n");
        out.write("\t\t\t}\n");
        out.write("\n");
        out.write("\t\t\tswitch (theDriver) {\n");
        out.write("\t\t\t\tcase POSTGRESQL:\n");
        out.write("\t\t\t\t\tPreparedStatement stat = conn.prepareStatement(\"SELECT nextval ('" + theSchema.getSqlLabel() + ".seqnum')\");\n");
        out.write("\n");
        out.write("\t\t\t\t\tResultSet rs = stat.executeQuery();\n");
        out.write("\n");
        out.write("\t\t\t\t\twhile (rs.next()) {\n");
        out.write("\t\t\t\t\t\tnextInt = rs.getInt(1);\n");
        out.write("\t\t\t\t\t}\n");
        out.write("\t\t\t\t\trs.close();\n");
        out.write("\t\t\t\t\tstat.close();\n");
        out.write("\t\t\t\t\tbreak;\n");
        out.write("\t\t\t\tcase SQLSERVER:\n");
        out.write("\t\t\t\t\tnextInt = 1;\n");
        out.write("\t\t\t\t\tbreak;\n");
        out.write("\t\t\t\tdefault:\n");
        out.write("\t\t\t\t\tnextInt = 1;\n");
        out.write("\t\t\t}\n");
        out.write("\t\t} catch (Exception e) {\n");
        out.write("\t\t\tlog.error(\"SeqNum Exception\", e);\n");
        out.write("\t\t} finally {\n");
        out.write("\t\t\ttry {\n");
        out.write("\t\t\t\tif (conn != null)\n");
        out.write("\t\t\t\t\tconn.close();\n");
        out.write("\t\t\t} catch (SQLException e) {\n");
        out.write("\t\t\t\tlog.error(\"Problem closing Connection\",e);\n");
        out.write("\t\t\t}\n");
        out.write("\t\t}\n");
        out.write("\t\treturn nextInt;\n");
        out.write("\t}\n");
        out.write("}");

        out.close();
    }
    
	public void generateDBTestClass() throws IOException {
		File baseClassFile = new File( packagePrefixDirectory, "/DBTest.java" );
		FileWriter fstream = new FileWriter( baseClassFile );
		BufferedWriter out = new BufferedWriter( fstream );
		out.write( "package " + packagePrefix + ";\n\n" );
		out.write( "import javax.servlet.jsp.JspWriter;" + "\n" );
		out.write( "import java.sql.Statement;" + "\n" );
		out.write( "import javax.servlet.jsp.JspException;" + "\n" );
		out.write( "import javax.servlet.jsp.JspTagException;" + "\n" );
		out.write( "import org.apache.logging.log4j.Logger;\n" );
		out.write( "import org.apache.logging.log4j.LogManager;\n\n\n" );
		out.write( "@SuppressWarnings(\"serial\")" );
		out.write( "\n" );
		out.write( "public class DBTest extends " + this.projectName + "BodyTagSupport {\n\n" );
		out.write( "\tprivate static final Logger log = LogManager.getLogger( DBTest.class );\n\n" );
		out.write( "\tpublic int doStartTag() throws JspException {\n" );
		out.write( "\t\ttry { \n" );
		out.write( "\t\t\tJspWriter out = pageContext.getOut();\n" );
		out.write( "\t\t\tStatement statement = getConnection().createStatement();\n" );
		if ( "sqlserver".equals( databaseType ) ) {
			out.write( "\t\t\tboolean rs = statement.execute(\"select CURRENT_TIMESTAMP\");\n" );
		} else {
			out.write( "\t\t\tboolean rs = statement.execute(\"select now()\");\n" );
		}
		out.write( "\t\t\tif (rs) { \n" );
		out.write( "\t\t\t\tout.print(\"SUCCESS\");\n" );
		out.write( "\t\t\t} else {\n" );
		out.write( "\t\t\t\tout.print(\"FAILED\");\n" );
		out.write( "\t\t\t}\n" );
		out.write( "\t\t} catch ( Exception e ) {" + "\n" );
		out.write( "\t\t\tlog.error( \"Connection Failed\", e );\n" );
		out.write( "\t\t\tthrow new JspTagException(\"Connection Failed: \" + e);\n" );
		out.write( "\t\t} finally {\n" );
		out.write( "\t\t\tfreeConnection();\n" );
		out.write( "\t\t}\n" );
		out.write( "\t\treturn SKIP_BODY;\n" );
		out.write( "\t}\n" );
		out.write( "}" );
		out.close();
	}

	/**
	 * @return the projectPath
	 */
	public String getProjectPath() {
		return projectPath;
	}

	/**
	 * @param projectPath the projectPath to set
	 */
	public void setProjectPath(String projectPath) {
		this.projectPath = projectPath;
	}

	/**
	 * @return the packagePrefix
	 */
	public String getPackagePrefix() {
		return packagePrefix;
	}

	/**
	 * @param packagePrefix the packagePrefix to set
	 */
	public void setPackagePrefix(String packagePrefix) {
		this.packagePrefix = packagePrefix;
	}

	/**
	 * @return the projectName
	 */
	public String getProjectName() {
		return projectName;
	}

	/**
	 * @param projectName the projectName to set
	 */
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	/**
	 * @return the sourceRootDirectory
	 */
	public File getSourceRootDirectory() {
		return sourceRootDirectory;
	}

	/**
	 * @param sourceRootDirectory the sourceRootDirectory to set
	 */
	public void setSourceRootDirectory(File sourceRootDirectory) {
		this.sourceRootDirectory = sourceRootDirectory;
	}

	/**
	 * @return the packagePrefixDirectory
	 */
	public File getPackagePrefixDirectory() {
		return packagePrefixDirectory;
	}

	/**
	 * @param packagePrefixDirectory the packagePrefixDirectory to set
	 */
	public void setPackagePrefixDirectory(File packagePrefixDirectory) {
		this.packagePrefixDirectory = packagePrefixDirectory;
	}

	/**
	 * @return the tagDirectory
	 */
	public File getTagDirectory() {
		return tagDirectory;
	}

	/**
	 * @param tagDirectory the tagDirectory to set
	 */
	public void setTagDirectory(File tagDirectory) {
		this.tagDirectory = tagDirectory;
	}

	/**
	 * @return the theSchema
	 */
	public Schema getTheSchema() {
		return theSchema;
	}

	/**
	 * @param theSchema the theSchema to set
	 */
	public void setTheSchema(Schema theSchema) {
		this.theSchema = theSchema;
	}

	/**
	 * @return the databaseType
	 */
	public String getDatabaseType() {
		return databaseType;
	}

	/**
	 * @param databaseType the databaseType to set
	 */
	public void setDatabaseType(String databaseType) {
		this.databaseType = databaseType;
	}
}

/*

out.write("\t\t\tfreeConnection();\n");
out.write("\t\t\tclearServiceState();\n\n");

out.write("\t\t\tTag parent = getParent();\n");
out.write("\t\t\tif(parent != null){\n");
out.write("\t\t\t\tpageContext.setAttribute(\"tagError\", true);\n");
out.write("\t\t\t\tpageContext.setAttribute(\"tagErrorException\", e);\n");
out.write("\t\t\t\tpageContext.setAttribute(\"tagErrorMessage\", \"Error: JDBC error generating " + theEntity.getLabel() + " iterator: \" + stat.toString());\n");
out.write("\t\t\t\treturn parent.doEndTag();\n");
out.write("\t\t\t}else{\n");
out.write("\t\t\t\tthrow new JspException(\"Error: JDBC error iterating across " + theEntity.getLabel() + "\",e);\n");
out.write("\t\t\t}\n\n");

 out.write("\t\t\tBoolean error = (Boolean) pageContext.getAttribute(\"tagError\");\n");
        out.write("\t\t\tif(error != null && error){\n\n");
        
        out.write("\t\t\t\tfreeConnection();\n");
        out.write("\t\t\t\tclearServiceState();\n\n");

        out.write("\t\t\t\tException e = (Exception) pageContext.getAttribute(\"tagErrorException\");\n");
        out.write("\t\t\t\tString message = (String) pageContext.getAttribute(\"tagErrorMessage\");\n\n");
        
        out.write("\t\t\t\tTag parent = getParent();\n");
        out.write("\t\t\t\tif(parent != null){\n");
        out.write("\t\t\t\t\treturn parent.doEndTag();\n");
        
        out.write("\t\t\t\t}else if(e != null && message != null){\n");
        out.write("\t\t\t\t\tthrow new JspException(message,e);\n");
        out.write("\t\t\t\t}else if(parent == null){\n");
        out.write("\t\t\t\t\tpageContext.removeAttribute(\"tagError\");\n");
        out.write("\t\t\t\t\tpageContext.removeAttribute(\"tagErrorException\");\n");
        out.write("\t\t\t\t\tpageContext.removeAttribute(\"tagErrorMessage\");\n");
        out.write("\t\t\t\t}\n");
        out.write("\t\t\t}\n");

+ "import javax.servlet.jsp.tagext.Tag;\n"

*/