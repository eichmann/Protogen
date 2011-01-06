/*
 * @author bkusenda
 * 
 * Generates dao files
 * 
 */
package edu.uiowa.icts.protogen.springhibernate;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.uiowa.webapp.Schema;

public class BaseTestCodeGenerator extends AbstractSpringHibernateCodeGenerator{

	protected static final Log log =LogFactory.getLog(BaseTestCodeGenerator.class);
	
	public BaseTestCodeGenerator(SpringHibernateModel model, String pathBase,String packageRoot) {
		super(model, pathBase, packageRoot);
		(new File(packageRootPath)).mkdirs();
	}



	private void generateTest(DomainClass dc) throws IOException {

		String packageName = model.getPackageRoot() +"." + dc.getSchema().getLowerLabel() +".dao";

		String packagePath = pathBase + "/"	+ packageName.replaceAll("\\.", "/") ;
		
		String className=""+dc.getIdentifier()+"Test";

		List<String> importList = new ArrayList<String>();
		importList.add("import edu.uiowa.icts.spring.*;");
		importList.add("import edu.uiowa.icts.spring.AbstractSpringTestCase;");
		importList.add("import org.junit.After;");
		importList.add("import org.junit.Before;");
		importList.add("import org.junit.Test;");
		importList.add("import org.springframework.beans.factory.annotation.Autowired;");
		importList.add("import "+dc.getPackageName()+".*;");
		
		(new File(packagePath)).mkdirs();
		
	
		File file = new File(packagePath, className	+ ".java");
		if(file.exists() && overwrite==false)
		{
			log.debug("File Exists");
			return;
		}
		
		if(file.exists())
			log.debug("Overwriting file....");
			
		FileWriter fstream = new FileWriter(file);
		BufferedWriter out = new BufferedWriter(fstream);
		
		/*
		 * Print Package
		 */
		out.write("package "+ packageName+";\n");
		lines(out, 1);
		
		/*
		 * Print imports
		 */
		for(String importSt: importList)
			out.write(importSt+"\n");
		
		lines(out, 1);
		out.write("/* Generated by Protogen\n");
		out.write(" *\n");
		out.write(" * Unit test Template\n");
		out.write("*/\n");
		
		lines(out, 2);
		/*
		 * Print interface header
		 * 
		 */
		out.write("public class "+className+" extends AbstractSpringTestCase {\n");
		lines(out,2);
		spaces(out, 4);
		out.write("@Autowired\n");
		spaces(out, 4);
		out.write("private "+dc.getSchema().getUpperLabel()+"DaoService daoService;\n");
		lines(out,1);
		out.write(createGetter(dc.getSchema().getUpperLabel()+"DaoService", "daoService", 4));
		lines(out,1);
		out.write(createSetter(dc.getSchema().getUpperLabel()+"DaoService", "daoService", 4));
		
		lines(out,1);
		String methodTemplates = ""
			+ indent(4)+"@Test\n"
			+ indent(4)+"public void testServiceName() {\n"
			+ indent(4)+"	assertEquals(true,true);\n"
			+ indent(4)+"	       \n"
			+ indent(4)+"}\n"
			+ indent(4)+"	   \n"
			+ indent(4)+"@Before\n"
			+ indent(4)+"public void setUp() throws Exception {\n"
			+ indent(4)+"	super.setUp();\n"
			+ indent(4)+"		   \n"
			+ indent(4)+"}\n"
			+ indent(4)+"	   \n"
			+ indent(4)+"@After\n"
			+ indent(4)+"public void tearDown() throws Exception {\n"
			+ indent(4)+"	super.tearDown();\n"
			+ indent(4)+"}\n";
		out.write(methodTemplates);
		
		lines(out,1);
		out.write("}\n");
		out.close();
	}	

	

	/*
	 * Public Function to generate java domain code
	 * 
	 */
	public void generate() throws IOException 
	{
		
	
		
		for(DomainClass dc: model.getDomainClassList())
			generateTest(dc);
		

		
	}

}
