package edu.uiowa.webapp;

import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.uiowa.icts.protogen.springhibernate.BaseTestCodeGenerator;
import edu.uiowa.icts.protogen.springhibernate.ConfigGenerator;
import edu.uiowa.icts.protogen.springhibernate.ControllerCodeGenerator;
import edu.uiowa.icts.protogen.springhibernate.DAOCodeGenerator;
import edu.uiowa.icts.protogen.springhibernate.JSPCodeGenerator;
import edu.uiowa.icts.protogen.springhibernate.DomainCodeGenerator;
import edu.uiowa.icts.protogen.springhibernate.ClassVariable.AttributeType;
import edu.uiowa.icts.protogen.springhibernate.SpringHibernateModel;

public class Generator {
	static String pathPrefix = "";
	//static String mode = "tags";
	//static String modelSource = "clay";
	//static String dburl="";
	//static String dbusername="" ;
	//static String dbpassword = "";
	///static String dbdriverclass = "";
	//static String dbschema = "";
	//static boolean dbssl = true;

	private static final Log log =LogFactory.getLog(Generator.class);
	/**
	 * @param args
	 * arg 0 = qualified package name (required)
	 * arg 1 = Eclipse project name (required)
	 * arg 2 = location of Eclipse workspace (can be relative, is optional)
	 *
	 * @throws Exception 
	 */
	
	public Generator()
	{
		pathPrefix = System.getProperty("user.dir") + "/..";
	}

	static Database theDatabase = null;



	public int runGenerator(Properties props) {
		String projectName=props.getProperty("project.name");
		String packageName=props.getProperty("package.name");
		
		
		
		if(props.getProperty("pathPrefix")!=null)
			pathPrefix = props.getProperty("path.prefix");
		
		
		log.debug("Path Prefix:"+System.getProperty("user.dir"));
		
			
		

		String modelSource=props.getProperty("model.source", "clay");
		String mode=props.getProperty("mode", "tags");

		DatabaseSchemaLoader theLoader = null;
		if(modelSource.equalsIgnoreCase("clay"))
		{
			theLoader = new ClayLoader();
			String clayFile = "";
			try {
				clayFile = props.getProperty("clay.file",
						pathPrefix + projectName + "/WebContent/resources/" + projectName + ".clay");
				theLoader.run(clayFile);
			} catch (Exception e) {
				log.error("Could not parse clay file: " + clayFile, e);
				return 1;
			}

		}
		else if(modelSource.equalsIgnoreCase("jdbc"))
		{

			theLoader = new JDBCLoader();
			try {
				theLoader.run("database.properties");
			} catch (Exception e) {
				log.error("Could load JDBC", e);
			}
			theLoader.getDatabase().setLabel(projectName);
			theLoader.getDatabase().relabel();
		}

		theDatabase = theLoader.getDatabase();
		theDatabase.dump();

		log.debug("PathPrefix:"+pathPrefix);


		if(mode.equalsIgnoreCase("tags"))
		{
			String packageRoot = packageName;
			if (Boolean.parseBoolean(props.getProperty("generate.tags", "true"))) {
				String tagLocation = props.getProperty("tag.file.location",
						pathPrefix + "/" + projectName+ "/"  + "src");
				TagClassGenerator theGenerator = new TagClassGenerator(tagLocation, packageRoot, projectName);
				try {
					theGenerator.generateTagClasses(theDatabase);
				} catch (IOException e2) {
					log.error("Could not generate Tag Classes: " + tagLocation, e2);
						return 1;
				}
			}
			if (Boolean.parseBoolean(props.getProperty("generate.tld", "true"))) {


				//TLDGenerator theTLDgenerator = new TLDGenerator(tldLocation, packageRoot, projectName);
				TLDGenerator theTLDgenerator = new TLDGenerator(props);
				try {
					theTLDgenerator.generateTLD(theDatabase);
				} catch (IOException e1) {
					log.error("Could not generate TLD File: " +  props.getProperty("tld.file.location"), e1);
						return 1;
				}
			}
			if (Boolean.parseBoolean(props.getProperty("generate.jsps", "true"))) {
				String jspLocation = props.getProperty("jsp.file.location", 
						pathPrefix + projectName + "/WebContent/");
				JSPGenerator theJSPgenerator = new JSPGenerator(jspLocation, packageRoot, projectName);
				try {
					theJSPgenerator.generateJSPs(theDatabase);
				} catch (IOException e) {
					log.error("Could not generate JSP Files: " + jspLocation, e);
						return 1;
				}
			}
		}
		else
		{

			SpringHibernateModel model = new SpringHibernateModel(theDatabase, packageName);

			/*
			 * generate.domain = true 
			 * 
			 */
			if (Boolean.parseBoolean(props.getProperty("generate.domain", "true"))) {
				String domainPath = props.getProperty("domain.file.location",pathPrefix + projectName+ "/"  + "src");
				DomainCodeGenerator codeGen = new DomainCodeGenerator(model,domainPath,packageName);
					try {
						log.debug("***********Writing domain code*****************");
						codeGen.generate();
					} catch (IOException e) {
						log.debug("Error writing domain code");
						e.printStackTrace();
						return 1;
					}
			}

			/*
			 * generate.dao = true 
			 * 
			 */
			if (Boolean.parseBoolean(props.getProperty("generate.dao", "true"))) {
				String daoPath = props.getProperty("dao.file.location",	pathPrefix +projectName+ "/"  + "src");
				DAOCodeGenerator codeGen = new DAOCodeGenerator(model,daoPath,packageName);
				try {
					
					codeGen.generate();
				} catch (Exception e3) {
					log.error("Could not generate DAO Classes: " +daoPath, e3);
				}
			}

			

			/*
			 * generate.controller = true 
			 * 
			 */
			if (Boolean.parseBoolean(props.getProperty("generate.controller", "true"))) {
				String controllerPath = props.getProperty("controller.file.location",	pathPrefix +projectName+ "/"  + "src");
				ControllerCodeGenerator codeGen = new ControllerCodeGenerator(model,controllerPath,packageName);
				try {
					
					codeGen.generate();
				} catch (Exception e3) {
					log.error("Could not generate Controller Classes: " +controllerPath, e3);
				}
			}
			
			
			/*
			 * generate.jsps = true 
			 * 
			 */
			if (Boolean.parseBoolean(props.getProperty("generate.jsp", "true"))) {
				String jspPath = props.getProperty("jsp.file.location",	pathPrefix +projectName+ "/"  + "src");
				JSPCodeGenerator codeGen = new JSPCodeGenerator(model,jspPath,packageName);
				try {
					
					codeGen.generate();
				} catch (Exception e3) {
					log.error("Could not generate JSP files: " +jspPath, e3);
				}
			}
			
			
			/*
			 * generate.tests = true 
			 * 
			 */
			if (Boolean.parseBoolean(props.getProperty("generate.test", "true"))) {
				String testPath = props.getProperty("test.file.location",	pathPrefix +projectName+ "/"  + "src");
				BaseTestCodeGenerator codeGen = new BaseTestCodeGenerator(model,testPath,packageName);
				try {
					
					codeGen.generate();
				} catch (Exception e3) {
					log.error("Could not generate Test Classes: " + testPath, e3);
				}
			}
			
			

		}


		return 0;


	}
	public static void main(String[] args) throws Exception {
		Properties myProps = new Properties();


		if (args.length > 2)
		{
			pathPrefix = args[2];


		}
		else
		{
			System.out.println("Error: invalid number of arguments. needs 2.");
			System.exit(1);
		}
		if (!pathPrefix.endsWith("/"))
			pathPrefix += "/";
		myProps.setProperty("path.prefix", pathPrefix);

		if (args.length > 3)
		{
			//mode = args[3];
			myProps.setProperty("mode", args[3]);
			if (args.length > 4)
			{
				//modelSource = args[4];
				myProps.setProperty("model.source", args[4]);
				if(args.length>8)
				{
					//dburl=args[5];
					myProps.setProperty("db.url", args[5]);
					//dbusername=args[6];
					myProps.setProperty("db.username", args[6]);

					//dbpassword=args[7];
					myProps.setProperty("db.password", args[7]);
					//dbssl=Boolean.parseBoolean(args[8]);
					myProps.setProperty("db.ssl", args[8]);
					//dbdriverclass=args[9];
					myProps.setProperty("db.driver.class", args[9]);
					if(args.length>9)
						myProps.setProperty("db.schema", args[10]);
					//dbschema=args[10];


				}

			}

		}
		Generator gen = new Generator();
		gen.runGenerator(myProps);

		System.out.println("Generator mode:"+ myProps.getProperty("mode"));
	}

	static Database getDatabase() {
		return theDatabase;
	}

}
