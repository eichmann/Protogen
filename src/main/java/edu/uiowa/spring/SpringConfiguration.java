package edu.uiowa.spring;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.uiowa.hibernate.FileContents;
import edu.uiowa.webapp.DomainClass;
import edu.uiowa.webapp.Entity;
import edu.uiowa.webapp.JSPGenerator;
import edu.uiowa.webapp.Relationship;
import edu.uiowa.webapp.Schema;

public class SpringConfiguration {

	
	private static final Log log =LogFactory.getLog(SpringConfiguration.class);

	private String templateFile = "";
	
	private String springComponentsToken = "@springcomponents@";
	private String springComponents = "";
	private String hibernatePackagesToken = "@hibernatepackages@";
	private String hibernatePackages = "";
	private String databaseNameToken = "@databasename@";
	private String databaseName = "";
	private String packageRoot="";

	
	

	
	private static String TEMPLATE_PATH="";
	private String suffix = "Controller";

	public SpringConfiguration() {
		this.templateFile=TEMPLATE_PATH + "dispatcher-servlet.tmplt";
	}

	

	public String get() throws Exception {
		
		String template = FileContents.getContents(templateFile);
		template = template.replaceAll(springComponentsToken, springComponents);
		template = template.replaceAll(hibernatePackagesToken, hibernatePackages);
		template = template.replaceAll(databaseNameToken, databaseName);
		
		return template;
	}
	
	public void genModelList(List<Schema> schemaList)
	{	
		log.debug("");
		log.debug("genMOdelList");
		
		Iterator<Schema> schemaIter =  schemaList.iterator();
		while(schemaIter.hasNext())
		{
			Schema schema = schemaIter.next();
			log.debug("schem:"+schema.getLabel());
			springComponents += "<context:component-scan base-package=\""+ packageRoot +"." +schema.getUnqualifiedLabel()+ "\" />\n     ";
			hibernatePackages += "<value>"+packageRoot +"." +schema.getUnqualifiedLabel()+"</value>\n     ";
		}
		log.debug("..done");
	}



	public String getTemplateFile() {
		return templateFile;
	}



	public void setTemplateFile(String templateFile) {
		this.templateFile = templateFile;
	}



	public String getSpringComponentsToken() {
		return springComponentsToken;
	}



	public void setSpringComponentsToken(String springComponentsToken) {
		this.springComponentsToken = springComponentsToken;
	}



	public String getSpringComponents() {
		return springComponents;
	}



	public void setSpringComponents(String springComponents) {
		this.springComponents = springComponents;
	}



	public String getHibernatePackagesToken() {
		return hibernatePackagesToken;
	}



	public void setHibernatePackagesToken(String hibernatePackagesToken) {
		this.hibernatePackagesToken = hibernatePackagesToken;
	}



	public String getHibernatePackages() {
		return hibernatePackages;
	}



	public void setHibernatePackages(String hibernatePackages) {
		this.hibernatePackages = hibernatePackages;
	}



	public String getPackageRoot() {
		return packageRoot;
	}



	public void setPackageRoot(String packageRoot) {
		this.packageRoot = packageRoot;
	}



	public static String getTEMPLATE_PATH() {
		return TEMPLATE_PATH;
	}



	public static void setTEMPLATE_PATH(String tEMPLATEPATH) {
		TEMPLATE_PATH = tEMPLATEPATH;
	}



	public String getSuffix() {
		return suffix;
	}



	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}



	public String getDatabaseNameToken() {
		return databaseNameToken;
	}



	public void setDatabaseNameToken(String databaseNameToken) {
		this.databaseNameToken = databaseNameToken;
	}



	public String getDatabaseName() {
		return databaseName;
	}



	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}





	
	
	

}
