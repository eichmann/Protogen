package edu.uiowa.webapp;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Generator {
	static String pathPrefix = "/Users/eichmann/Documents/Components/workspace/";
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

	static Database theDatabase = null;

	
	
	public void runGenerator(Properties props) {
		String projectName=props.getProperty("project.name");
		String packageName=props.getProperty("package.name");
		
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
				}
			}
			if (Boolean.parseBoolean(props.getProperty("generate.tld", "true"))) {
				String tldLocation = props.getProperty("tld.file.location", 
						pathPrefix + projectName + "/WebContent/WEB-INF/" + projectName +".tld");
				
				TLDGenerator theTLDgenerator = new TLDGenerator(tldLocation, packageRoot, projectName);
				try {
					theTLDgenerator.generateTLD(theDatabase);
				} catch (IOException e1) {
					log.error("Could not generate TLD File: " + tldLocation, e1);
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
				}
			}
		}
		else
		{

			String domainPath = props.getProperty("domain.file.location",
					pathPrefix + projectName+ "/"  + "src")	;
			DomainCodeGenerator theDomainGenerator = new DomainCodeGenerator(domainPath, packageName, projectName);
			try {
				theDomainGenerator.generateDomainCodeForDatabase(theDatabase);
			} catch (IOException e4) {
				log.error("Could not generate Code for Database: " + domainPath, e4);
			}

			String daoPath = props.getProperty("dao.file.location",
					pathPrefix +projectName+ "/"  + "src");
			DAOGenerator theDaoGenerator = new DAOGenerator(daoPath,packageName, projectName);
			try {
				theDaoGenerator.generateDaoClasses(theDomainGenerator.getDomainClassList());
			} catch (Exception e3) {
				log.error("Could not generate DAO Classes: " +daoPath, e3);
			}

			String controllerPath = props.getProperty("controller.file.location", 
					pathPrefix + projectName+ "/"  + "src");
			ControllerGenerator theControllerGenerator = new ControllerGenerator(controllerPath, packageName, projectName);
			try {
				theControllerGenerator.generateControllerClasses(theDomainGenerator.getDomainClassList());
			} catch (Exception e2) {
				log.error("Could not generate Controller Classes: " +controllerPath, e2);
			}

			String jspLocation = props.getProperty("jsp.file.location", 
					pathPrefix + projectName+ "/"  + "WebContent/WEB-INF/jsp/");
			JSPCodeGenerator theJSPGenerator = new JSPCodeGenerator(jspLocation);
			try {
				theJSPGenerator.generateAllJSP(theDomainGenerator.getDomainClassList());
			} catch (IOException e1) {
				log.error("Could not generate All JSP Files: " + jspLocation, e1);

			}

			String configLocation = props.getProperty("config.file.location",
					pathPrefix + projectName+ "/"  + "src");
			ConfigGenerator configGen = new ConfigGenerator(configLocation,packageName, projectName);
			try {
				configGen.generateDispatcher(theDatabase);
			} catch (Exception e) {
				log.error("Could not generate Dispatcher: " + configLocation, e);

			}

		}



		
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
