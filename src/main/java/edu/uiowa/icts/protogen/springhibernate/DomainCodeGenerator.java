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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;





import edu.uiowa.icts.protogen.springhibernate.ClassVariable.AttributeType;
import edu.uiowa.icts.protogen.springhibernate.ClassVariable.RelationshipType;
import edu.uiowa.icts.protogen.springhibernate.DomainClass.ClassType;
import edu.uiowa.webapp.Attribute;
import edu.uiowa.webapp.Database;
import edu.uiowa.webapp.Entity;
import edu.uiowa.webapp.Relationship;
import edu.uiowa.webapp.Schema;


public class DomainCodeGenerator extends AbstractSpringHibernateCodeGenerator {


	
	protected static final Log log =LogFactory.getLog(DomainCodeGenerator.class);

	/**
	 * @param model
	 * @param pathBase
	 * @param packageRoot
	 */
	public DomainCodeGenerator(SpringHibernateModel model, String pathBase,
			String packageRoot) {
		super(model, pathBase, packageRoot);
		(new File(packageRootPath)).mkdirs();
	}


	/*
	 * Generate Class for Composite Key
	 * Example TableNameId.java
	 * 
	 */
	private void generateDomainCompositeKeyClass(DomainClass dc,String packagePath) throws IOException {
		

		if(dc.isUsesCompositeKey()==false)
		{
			log.debug("Does not have composite key");
			return;
		}
		Entity entity = dc.getEntity();

		File file = new File(packagePath, entity.getUnqualifiedLabel()	+ "Id.java");

		if(file.exists())
		{
			log.debug("" + file.getCanonicalPath() + " Exists. Not Overwriting");
			return;
		}

		FileWriter fstream = new FileWriter(file);
		BufferedWriter out = new BufferedWriter(fstream);

		out.write("package " + dc.getPackageName() + ";\n");
		List<String> importList = new ArrayList<String>();
		importList.add("import java.util.Set;");
		importList.add("import java.util.*;");
		importList.add("import " +  model.getPackageRoot()+ ".*;");
		importList.add("import javax.persistence.*;");
		importList.add("import java.io.Serializable;");
		importList.add("import org.springframework.format.annotation.DateTimeFormat;");
		importList.add("import java.text.DateFormat;");
		importList.add("import java.text.SimpleDateFormat;");
		importList.add("import java.text.ParseException;");

		Iterator<String> importIter = importList.iterator();
		

		lines(out, 1);
		out.write(dc.getComment());
		lines(out, 1);
	

		while (importIter.hasNext())
			out.write(importIter.next() + "\n");
		lines(out, 2);
		out.write("@Embeddable\n");
		out.write("public class " + entity.getUnqualifiedLabel() + "Id implements Serializable\n");
		out.write("{\n");
		lines(out, 2);
		spaces(out, 4);
		out.write("//Table attribute definitions\n");
		Iterator<Attribute> attribIter0 = entity.getPrimaryKeyAttributes().iterator();
		while(attribIter0.hasNext())
		{
			Attribute attrib = attribIter0.next();
			String field = "";
			if(attrib.getJavaTypeClass().equalsIgnoreCase("date"))
			{
				spaces(out,4);
				out.write("@DateTimeFormat(pattern = \"yyyy-MM-dd\")\n");
				
			}
			
			
			field = "private " + attrib.getJavaTypeClass() + " "	+ attrib.getUnqualifiedLowerLabel() + ";\n";
			spaces(out, 4);
			out.write(field);

		}
		lines(out, 4);
		spaces(out, 4);
		out.write("//Table attribute definitions\n");
		attribIter0 = entity.getPrimaryKeyAttributes().iterator();
		while(attribIter0.hasNext())
		{

			Attribute attrib = attribIter0.next();
			generateGetter(out, attrib, false);
			lines(out, 1);
			if(attrib.getType().equalsIgnoreCase("date"))
				generateDateStringSetter(out, attrib);
			generateSetter(out, attrib);
			lines(out, 1);


		}

		out.write("}");
		out.close();
	}
	

	private void generateDateStringSetter(BufferedWriter out, Attribute attrib)
	throws IOException {
		spaces(out, 4);
		out.write("public void set" + attrib.getUpperLabel() + "( String " + attrib.getUnqualifiedLowerLabel()	+ ")\n");
		spaces(out, 4);
		out.write("{\n");
		spaces(out, 8);
		out.write("try{\n");
		spaces(out, 12);
		out.write("DateFormat formatter = new SimpleDateFormat(\"MM/dd/yyyy\");\n");
		spaces(out, 12);
		out.write("formatter.setLenient(true);\n");
		spaces(out, 12);
		out.write("this." + attrib.getUnqualifiedLowerLabel() + " = formatter.parse(" + attrib.getUnqualifiedLowerLabel() + ");\n");
		out.write("");
		out.write("} catch (ParseException e) {e.printStackTrace();}\n");
		spaces(out, 8);
		
		spaces(out, 4);
		out.write("}\n");

	}

	private void generateSetter(BufferedWriter out, Attribute attrib)
	throws IOException {
		spaces(out, 4);
		out.write("public void set" + attrib.getUpperLabel() + "("	+ attrib.getJavaTypeClass() + " " + attrib.getUnqualifiedLowerLabel()	+ ")\n");
		spaces(out, 4);
		out.write("{\n");
		spaces(out, 8);
		out.write("this." + attrib.getUnqualifiedLowerLabel() + " = "	+ attrib.getUnqualifiedLowerLabel() + ";\n");
		spaces(out, 4);
		out.write("}\n");

	}

	private void generateGetter(BufferedWriter out, Attribute attrib, boolean primaryAnnotations)
	throws IOException {

		spaces(out, 4);
		out.write("@Column(name = \""+attrib.getSqlLabel()+"\""+ (attrib.isPrimary() ? ", nullable = false":"")+ ")\n" );
		spaces(out, 4);
		out.write("public "+attrib.getJavaTypeClass()+" get" + attrib.getUpperLabel() + "()\n");
		spaces(out, 4);
		out.write("{\n");
		spaces(out, 8);
		out.write("return " + attrib.getUnqualifiedLowerLabel() + ";\n");
		spaces(out, 4);
		out.write("}\n");

	}
	/*
	 * Generate Domain Class for Table
	 *  Example TableName.java
	 */
	private void generateDomainClassCode(DomainClass domainClass,String packagePath) throws IOException {

		 DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	        Date date = new Date();
		String comment = "/* Generated by Protogen - www.icts.uiowa.edu/protogen\n";
		comment += " * @date "+ dateFormat.format(date)+"\n";
		comment += "*/ \n";
		domainClass.setComment(comment);
		
		if (domainClass.isUsesCompositeKey())
			generateDomainCompositeKeyClass(domainClass,packagePath);

		File file = new File(packagePath, domainClass.getIdentifier()	+ ".java");
		if(!file.exists())
		{
			FileWriter fstream = new FileWriter(file);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(domainClass.toString());
			out.close();
		}
		else
			log.debug("" + file.getCanonicalPath() + " Exists. Not Overwriting");


	}

	/*
	 * Public Function to generate java domain code
	 * 
	 */
	public void generate() throws IOException 
	{
		
		for(DomainClass dc: model.getDomainClassList())
		{
			String packagePath = pathBase + "/"	+ dc.getPackageName().replaceAll("\\.", "/");
			(new File(packagePath)).mkdirs();
			generateDomainClassCode(dc,packagePath);

		}

	}
	

}
