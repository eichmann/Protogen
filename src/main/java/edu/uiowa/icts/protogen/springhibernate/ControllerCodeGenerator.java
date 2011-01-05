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
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.uiowa.webapp.Attribute;
import edu.uiowa.webapp.Schema;

public class ControllerCodeGenerator extends AbstractSpringHibernateCodeGenerator{

	
	protected static final Log log =LogFactory.getLog(ControllerCodeGenerator.class);
	
	private String interfaceSuffix="Service";
	public ControllerCodeGenerator(SpringHibernateModel model, String pathBase,String packageRoot) {
		super(model, pathBase, packageRoot);
		(new File(packageRootPath)).mkdirs();
	}


	private void generateController(DomainClass dc) throws IOException {

		String packageName = model.getPackageRoot() +"." + dc.getSchema().getLowerLabel() +".controller";
		String daoPackageName = model.getPackageRoot() +"." + dc.getSchema().getLowerLabel() +".dao";
	

		String packagePath = pathBase + "/"	+ packageName.replaceAll("\\.", "/") ;
		
		String className=""+dc.getIdentifier()+"Controller";
		String interfaceName=""+dc.getIdentifier()+interfaceSuffix;
		String accessor=""+dc.getSchema().getLowerLabel()+"DaoService.get"+dc.getIdentifier()+interfaceSuffix+"()";
		String jspPath="/"+dc.getSchema().getLowerLabel()+"/"+dc.getLowerIdentifier();
		
		List<String> importList = new ArrayList<String>();
		importList.add("import java.util.Date;");
		importList.add("import edu.uiowa.icts.spring.*;");
		importList.add("import "+dc.getPackageName()+".*;");
		importList.add("import "+daoPackageName+".*;");
		importList.add("import org.apache.commons.logging.LogFactory;");
		importList.add("import org.apache.commons.logging.Log;");
		importList.add("import org.springframework.stereotype.Controller;");
		importList.add("import org.springframework.ui.ModelMap;");
		importList.add("import org.springframework.web.bind.annotation.RequestMapping;");
		importList.add("import org.springframework.web.bind.annotation.RequestMethod;");
		importList.add("import org.springframework.web.bind.annotation.ModelAttribute;");
		importList.add("import org.springframework.web.bind.annotation.RequestParam;");
		importList.add("import org.springframework.web.servlet.ModelAndView;");
		
		(new File(packagePath)).mkdirs();
		
		/*
		 * If exists, exit
		 * 
		 */
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
		out.write(" *\n");
		out.write("*/\n");
		
		lines(out, 1);

		/*
		 * Print class header
		 * 
		 */
		out.write("@Controller\n");
		out.write("@RequestMapping(\"/"+dc.getSchema().getLowerLabel()+"/"+dc.getLowerIdentifier()+"/*\")\n");
		out.write("public class "+className+" extends Abstract"+dc.getSchema().getUpperLabel()+"Controller {\n");
		lines(out,2);

		spaces(out, 4);
		out.write("private static final Log log =LogFactory.getLog("+className+".class);");
		lines(out,2);
		out.write(generateListMethod(dc, accessor,jspPath, 4));
		lines(out,2);
		out.write( generateAddMethod(dc , accessor,jspPath, 4));
		lines(out,2);
		out.write( generateEditMethod(dc , accessor,jspPath, 4));
		lines(out,2);
		out.write( generateShowMethod(dc , accessor,jspPath, 4));
		lines(out,2);
		out.write( generateSaveMethod(dc , accessor,jspPath, 4));
		lines(out,2);
		out.write( generateDeleteMethod(dc , accessor,jspPath, 4));
		lines(out,2);
		out.write("}\n");
		out.close();
	}
	
	/*
	 * generates abstract controllers
	 * 
	 * 
	 */
	private void generateAbstractControllers() throws IOException
	{
		for(Schema schema :model.getSchemaMap().keySet())
		{
			List<DomainClass> domainClassList = model.getSchemaMap().get(schema);
			if(domainClassList !=null && domainClassList.isEmpty()==false )
			{
				String packageName = model.getPackageRoot() +"." + schema.getLowerLabel() +".controller";

				String packagePath = pathBase + "/"	+ packageName.replaceAll("\\.", "/") ;

				generateAbstractController(schema,"Abstract"+schema.getUpperLabel()+"Controller", packageName,packagePath);
			}
			
			
		}
	
	}
	
	
	/*
	 * generates an abstract controller
	 * 
	 * 
	 */
	private void generateAbstractController(Schema schema, String className, String packageName,
			String packagePath) throws IOException {
		
		String daoPackageName = model.getPackageRoot() +"." + schema.getLowerLabel() +".dao";
		List<String> importList = new ArrayList<String>();
		importList.add("import edu.uiowa.icts.spring.*;");
		importList.add("import org.springframework.beans.factory.annotation.Autowired;");

		importList.add("import org.springframework.security.core.context.SecurityContextHolder;");
		importList.add("import "+daoPackageName+".*;");
		
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
		out.write(" *\n");
		out.write(" *\n");
		out.write("*/\n");
		
		lines(out, 1);
		/*
		 * Print  header
		 * 
		 */
		out.write("public abstract class "+className+" {\n");
		lines(out,2);
		
		
		String type= schema.getUpperLabel()+"DaoService";
		String variableName= schema.getLowerLabel()+"DaoService";
		lines(out, 3);
		spaces(out, 4);
		out.write("/*********** "+variableName+" ****************/\n");
		spaces(out, 4);
		out.write("protected "+type+" "+ variableName+"; \n");
		lines(out, 1);
		spaces(out, 4);
		out.write("@Autowired \n");
		out.write(createSetter(type, variableName,4));
		lines(out, 1);
		spaces(out, 4);
		out.write("public String getUsername() {\n");
		spaces(out, 8);
		out.write("return SecurityContextHolder.getContext().getAuthentication().getName();");
		lines(out, 1);
		spaces(out, 4);
		out.write("}");
		
		lines(out,1);
		out.write("}\n");
		out.close();
	}
	
	
	public String generateListMethod(DomainClass dc ,String accessor,String jspPath, int indent)
	{
		StringBuffer output = new StringBuffer();
		output.append(indent(indent)+"@RequestMapping(value = \"list.html\", method = RequestMethod.GET)\n");
		output.append(indent(indent)+"public ModelAndView list()\n");
		output.append(indent(indent)+"{\n");
		output.append(indent(indent*2)+"log.debug(\"in list method for "+dc.getIdentifier()+"\");\n");
		output.append(indent(indent*2)+"ModelMap model = new ModelMap();\n");
		output.append(indent(indent*2)+"model.addAttribute(\""+dc.getLowerIdentifier()+"List\","+accessor+".list());\n");
		output.append(indent(indent*2)+"return new ModelAndView(\""+jspPath+"/list\",model);\n");
	
		output.append(indent(indent)+"}");
		return output.toString();
	}
	
	public String generateAddMethod(DomainClass dc , String accessor,String jspPath, int indent)
	{
		
		StringBuffer output = new StringBuffer();
		output.append(indent(indent)+"@RequestMapping(value = \"add.html\", method = RequestMethod.GET)\n");
		output.append(indent(indent)+"public ModelAndView add()\n");
		output.append(indent(indent)+"{\n");
		output.append(indent(indent*2)+"log.debug(\"in add method for "+dc.getIdentifier()+"\");\n");
		output.append(indent(indent*2)+"ModelMap model = new ModelMap();\n");
		output.append(indent(indent*2)+dc.getIdentifier()+" "+dc.getLowerIdentifier()+" = new "+dc.getIdentifier()+"();\n");
		output.append(indent(indent*2)+"model.addAttribute(\""+dc.getLowerIdentifier()+"\","+dc.getLowerIdentifier()+");\n");
		for( ClassVariable cv :dc.getForeignClassVariables())
		{
			output.append(indent(indent*2)+"model.addAttribute(\""+cv.getDomainClass().getLowerIdentifier()+"List\","+cv.getDomainClass().getSchema().getLowerLabel()+"DaoService.get"+cv.getDomainClass().getIdentifier()+interfaceSuffix+"().list());\n");
		}
		output.append(indent(indent*2)+"return new ModelAndView(\""+jspPath+"/edit\",model);\n");
	
		output.append(indent(indent)+"}");
		return output.toString();
	}
	
	public String generateEditMethod(DomainClass dc , String accessor,String jspPath, int indent)
	{
		List<String[]> compositeKey = new ArrayList<String[]>();
		String sig="";
		if(dc.isUsesCompositeKey())
		{
			
			for(Attribute a : dc.getEntity().getPrimaryKeyAttributes())
			{
				String type = a.getType();
				if(type.equalsIgnoreCase("date"))
					type="String";
				sig += "@RequestParam(\""+a.getLowerLabel()+"\") "+type+" "+a.getLowerLabel()+", ";
				compositeKey.add(new String[]{a.getType(),a.getLowerLabel()});
			}
				
			sig = sig.substring(0, sig.length()-2);
			
		}
		else
		{
			for(ClassVariable cv :dc.getPrimaryKeys())
			{
				
				sig += "@RequestParam(\""+cv.getLowerIdentifier()+"\") "+cv.getType()+" "+dc.getLowerIdentifier()+"Id, ";
			}
			sig = sig.substring(0, sig.length()-2);
		}
			
		StringBuffer output = new StringBuffer();
		output.append(indent(indent)+"@RequestMapping(value = \"edit.html\", method = RequestMethod.GET)\n");
		output.append(indent(indent)+"public ModelAndView edit("+sig+")\n");
		output.append(indent(indent)+"{\n");
		output.append(indent(indent*2)+"log.debug(\"in edit method for "+dc.getIdentifier()+"\");\n");
		
		output.append(indent(indent*2)+"ModelMap model = new ModelMap();\n");
		
		/*
		 * set composite key values
		 */
		if(dc.isUsesCompositeKey())
		{
			output.append(indent(indent*2)+dc.getIdentifier()+"Id "+dc.getLowerIdentifier()+"Id = new "+dc.getIdentifier()+"Id();\n");
			for(String[] starray : compositeKey)
			{
				String setter = "set"+starray[1].substring(0, 1).toUpperCase() + starray[1].substring(1, starray[1].length());
				output.append(indent(indent*2)+dc.getLowerIdentifier()+"Id."+setter+"("+starray[1]+");\n");
			}
	
		}
	
		for( ClassVariable cv :dc.getForeignClassVariables())
		{
			output.append(indent(indent*2)+"model.addAttribute(\""+cv.getDomainClass().getLowerIdentifier()+"List\","+cv.getDomainClass().getSchema().getLowerLabel()+"DaoService.get"+cv.getDomainClass().getIdentifier()+interfaceSuffix+"().list());\n");
		}

		
		output.append(indent(indent*2)+dc.getIdentifier()+" "+dc.getLowerIdentifier()+" = "+accessor+".findById("+dc.getLowerIdentifier()+"Id);\n");
		output.append(indent(indent*2)+"model.addAttribute(\""+dc.getLowerIdentifier()+"\","+dc.getLowerIdentifier()+");\n");
		output.append(indent(indent*2)+"return new ModelAndView(\""+jspPath+"/edit\",model);\n");
	
		output.append(indent(indent)+"}");
		return output.toString();
	}
	
	
	public String generateShowMethod(DomainClass dc , String accessor,String jspPath, int indent)
	{
		List<String[]> compositeKey = new ArrayList<String[]>();
		String sig="";
		if(dc.isUsesCompositeKey())
		{
			
			for(Attribute a : dc.getEntity().getPrimaryKeyAttributes())
			{
				sig += "@RequestParam(\""+a.getLowerLabel()+"\") "+a.getType()+" "+a.getLowerLabel()+", ";
				compositeKey.add(new String[]{a.getType(),a.getLowerLabel()});
			}
				
			sig = sig.substring(0, sig.length()-2);
			
		}
		else
		{
			for(ClassVariable cv :dc.getPrimaryKeys())
			{
				
				sig += "@RequestParam(\""+cv.getLowerIdentifier()+"\") "+cv.getType()+" "+dc.getLowerIdentifier()+"Id, ";
			}
			sig = sig.substring(0, sig.length()-2);
		}
			
		StringBuffer output = new StringBuffer();
		output.append(indent(indent)+"@RequestMapping(value = \"show.html\", method = RequestMethod.GET)\n");
		output.append(indent(indent)+"public ModelAndView show("+sig+")\n");
		output.append(indent(indent)+"{\n");
		output.append(indent(indent*2)+"log.debug(\"in show method for "+dc.getIdentifier()+"\");\n");
		
		output.append(indent(indent*2)+"ModelMap model = new ModelMap();\n");
		
		/*
		 * set composite key values
		 */
		if(dc.isUsesCompositeKey())
		{
			output.append(indent(indent*2)+dc.getIdentifier()+"Id "+dc.getLowerIdentifier()+"Id = new "+dc.getIdentifier()+"Id();\n");
			for(String[] starray : compositeKey)
			{
				String setter = "set"+starray[1].substring(0, 1).toUpperCase() + starray[1].substring(1, starray[1].length());
				output.append(indent(indent*2)+dc.getLowerIdentifier()+"Id."+setter+"("+starray[1]+");\n");
			}
	
		}

		
		output.append(indent(indent*2)+dc.getIdentifier()+" "+dc.getLowerIdentifier()+" = "+accessor+".findById("+dc.getLowerIdentifier()+"Id);\n");
		output.append(indent(indent*2)+"model.addAttribute(\""+dc.getLowerIdentifier()+"\","+dc.getLowerIdentifier()+");\n");
		output.append(indent(indent*2)+"return new ModelAndView(\""+jspPath+"/show\",model);\n");
	
		output.append(indent(indent)+"}");
		return output.toString();
	}

	public String generateSaveMethod(DomainClass dc , String accessor,String jspPath, int indent)
	{
		List<String[]> compositeKey = new ArrayList<String[]>();
		String sig="";
		if(dc.isUsesCompositeKey())
		{
		
			for(Attribute a : dc.getEntity().getPrimaryKeyAttributes())
			{
				if(a.isForeign()==false)
				{
					String type = a.getType();
					if(type.equalsIgnoreCase("date"))
						type="String";
					sig += "@RequestParam(\"id."+a.getLowerLabel()+"\") "+type+" "+a.getLowerLabel()+", ";
					compositeKey.add(new String[]{a.getType(),a.getLowerLabel()});
				}
			}
				
			sig = sig.substring(0, sig.length());
			
		}
		
		for( ClassVariable cv :dc.getForeignClassVariables())
		{
			sig += "@RequestParam(\""+cv.getDomainClass().getLowerIdentifier()+"."+cv.getDomainClass().getPrimaryKey().getIdentifier()+"\") "+cv.getDomainClass().getPrimaryKey().getType()+" "+cv.getDomainClass().getPrimaryKey().getIdentifier()+", "; 
				//cv.getDomainClass().getLowerIdentifier()+"List\","+cv.getDomainClass().getSchema().getLowerLabel()+"DaoService.get"+cv.getDomainClass().getIdentifier()+interfaceSuffix+"().list());\n";
		}
				
			sig += "@ModelAttribute(\""+dc.getLowerIdentifier()+"\") "+dc.getIdentifier()+" "+dc.getLowerIdentifier()+"";
		
		
			
		StringBuffer output = new StringBuffer();
		output.append(indent(indent)+"@RequestMapping(value = \"save.html\", method = RequestMethod.POST)\n");
		output.append(indent(indent)+"public String save("+sig+")\n");
		output.append(indent(indent)+"{\n");
		output.append(indent(indent*2)+"log.debug(\"in save method for "+dc.getIdentifier()+"\");\n");
		

		/*
		 * set composite key values
		 */
		if(dc.isUsesCompositeKey())
		{
			output.append(indent(indent*2)+dc.getIdentifier()+"Id "+dc.getLowerIdentifier()+"Id = new "+dc.getIdentifier()+"Id();\n");
			for(String[] starray : compositeKey)
			{
				String setter = "set"+starray[1].substring(0, 1).toUpperCase() + starray[1].substring(1, starray[1].length());
				output.append(indent(indent*2)+dc.getLowerIdentifier()+"Id."+setter+"("+starray[1]+");\n");
			}
	
		}
		
		
		for( ClassVariable cv :dc.getForeignClassVariables())
		{
			if(cv.isPrimary()==false && dc.isUsesCompositeKey()==false)
			{
				String getter = ""+dc.getSchema().getLowerLabel()+"DaoService.get"+cv.getDomainClass().getIdentifier()+interfaceSuffix+"().findById("+cv.getDomainClass().getPrimaryKey().getIdentifier()+")"; 
				output.append(indent(indent*2)+""+dc.getLowerIdentifier()+".set"+cv.getUpperIdentifier()+"("+getter+");\n");
			}
			else
			{
				 
				output.append(indent(indent*2)+""+dc.getLowerIdentifier()+"Id.set"+cv.getAttribute().getUpperLabel()+"("+cv.getDomainClass().getPrimaryKey().getIdentifier()+");\n");
				
			}
			
				//cv.getDomainClass().getLowerIdentifier()+"List\","+cv.getDomainClass().getSchema().getLowerLabel()+"DaoService.get"+cv.getDomainClass().getIdentifier()+interfaceSuffix+"().list());\n";
		}
		if(dc.isUsesCompositeKey())
		{
		//output.append(indent(indent*2)+dc.getIdentifier()+" "+dc.getLowerIdentifier()+" = "+accessor+".findById("+dc.getLowerIdentifier()+"Id);\n");
		//output.append(indent(indent*2)+"if ("+dc.getLowerIdentifier()+" == null) {\n");
		//output.append(indent(indent*3)+""+dc.getLowerIdentifier()+" = new "+dc.getIdentifier()+"();\n");
		output.append(indent(indent*3)+""+dc.getLowerIdentifier()+".setId( "+dc.getLowerIdentifier()+"Id);\n");
		//output.append(indent(indent*3)+"}\n");
		}
		output.append(indent(indent*2)+accessor+".saveOrUpdate("+dc.getLowerIdentifier()+");\n");
		output.append(indent(indent*2)+"return \"redirect:"+jspPath+"/list.html\";\n");
	
		output.append(indent(indent)+"}");
		return output.toString();
	}
	
	public String generateDeleteMethod(DomainClass dc , String accessor,String jspPath, int indent)
	{
		List<String[]> compositeKey = new ArrayList<String[]>();
		String sig="";
		if(dc.isUsesCompositeKey())
		{
			
			for(Attribute a : dc.getEntity().getPrimaryKeyAttributes())
			{
				String type = a.getType();
				if(type.equalsIgnoreCase("date"))
					type="String";
				sig += "@RequestParam(\""+a.getLowerLabel()+"\") "+type+" "+a.getLowerLabel()+", ";
				compositeKey.add(new String[]{a.getType(),a.getLowerLabel()});
			}
				
			sig = sig.substring(0, sig.length()-2);
			
		}
		else
		{
			for(ClassVariable cv :dc.getPrimaryKeys())
			{
				
				sig += "@RequestParam(\""+cv.getLowerIdentifier()+"\") "+cv.getType()+" "+dc.getLowerIdentifier()+"Id, ";
			}
			sig = sig.substring(0, sig.length()-2);
		}
			
		StringBuffer output = new StringBuffer();
		output.append(indent(indent)+"@RequestMapping(value = \"delete.html\", method = RequestMethod.GET)\n");
		output.append(indent(indent)+"public String delete("+sig+")\n");
		output.append(indent(indent)+"{\n");
		output.append(indent(indent*2)+"log.debug(\"in delete method for "+dc.getIdentifier()+"\");\n");
		

		/*
		 * set composite key values
		 */
		if(dc.isUsesCompositeKey())
		{
			output.append(indent(indent*2)+dc.getIdentifier()+"Id "+dc.getLowerIdentifier()+"Id = new "+dc.getIdentifier()+"Id();\n");
			for(String[] starray : compositeKey)
			{
				String setter = "set"+starray[1].substring(0, 1).toUpperCase() + starray[1].substring(1, starray[1].length());
				output.append(indent(indent*2)+dc.getLowerIdentifier()+"Id."+setter+"("+starray[1]+");\n");
			}
	
		}

		
		output.append(indent(indent*2)+dc.getIdentifier()+" "+dc.getLowerIdentifier()+" = "+accessor+".findById("+dc.getLowerIdentifier()+"Id);\n");
		output.append(indent(indent*2)+accessor+".delete("+dc.getLowerIdentifier()+");\n");
		output.append(indent(indent*2)+"return \"redirect:"+jspPath+"/list.html\";\n");
	
		output.append(indent(indent)+"}");
		return output.toString();
	}


	/*
	 * Public Function to generate java domain code
	 * 
	 */
	public void generate() throws IOException 
	{
		
		for(DomainClass dc: model.getDomainClassList())
			generateController(dc);
	
		generateAbstractControllers();

		
	}

}
