package edu.uiowa.icts.protogen.webapp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.uiowa.icts.protogen.loaders.ClayXpathLoader;
import edu.uiowa.icts.protogen.loaders.DatabaseModelLoader;
import edu.uiowa.icts.protogen.loaders.JDBCLoader;
import edu.uiowa.icts.protogen.model.Database;
import edu.uiowa.icts.protogen.springhibernate.BaseTestCodeGenerator;
import edu.uiowa.icts.protogen.springhibernate.ColumnDeobfuscationCodeGenerator;
import edu.uiowa.icts.protogen.springhibernate.ControllerCodeGenerator;
import edu.uiowa.icts.protogen.springhibernate.DAOCodeGenerator;
import edu.uiowa.icts.protogen.springhibernate.JSPCodeGenerator;
import edu.uiowa.icts.protogen.springhibernate.DomainCodeGenerator;
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

	static Logger log = LogManager.getLogger(Generator.class);
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
		pathPrefix = System.getProperty("user.dir") + "/../";
	}

	public static Database theDatabase = null;

	public int runGenerator(Properties props) throws IOException {
		
		int error = 0;
		String projectName = props.getProperty("project.name");
		String packageName = props.getProperty("package.name");
		String webAppName = props.getProperty("jsp.project.name");
		String suppressionList = props.getProperty("suppression list");
		Hashtable<String,String> suppressionHash = null;
		
		if (suppressionList != null) {
			suppressionHash = new Hashtable<String,String>();
			log.info("suppression list: " + suppressionList);
			for (String suppressed : suppressionList.split("[ \n\r\t]+")) {
				log.info("suppressing: " + suppressed);	
				suppressionHash.put(suppressed, suppressed);
			}
		}
		
		if(props.getProperty("pathPrefix") != null){
			pathPrefix = props.getProperty("path.prefix");
		}
		
		log.debug("Path Prefix:"+System.getProperty("user.dir"));
		

		String modelSource = props.getProperty("model.source", "clay");
		String mode = props.getProperty("mode", "tags");

		DatabaseModelLoader theLoader = null;
		if(modelSource.equalsIgnoreCase("clay")) {
			theLoader = new ClayXpathLoader();
			if (suppressionHash != null)
				((ClayXpathLoader)theLoader).setSuppressionHash(suppressionHash);
			String clayFile = "";
			try {
				clayFile = props.getProperty("clay.file", pathPrefix + projectName + "/WebContent/resources/" + projectName + ".clay");
				theLoader.run(clayFile);
			} catch (Exception e) {
				log.error("Could not parse clay file: " + clayFile, e);
				return 1;
			}

		} else if(modelSource.equalsIgnoreCase("jdbc")) {

			theLoader = new JDBCLoader();
			if (suppressionHash != null)
				((JDBCLoader)theLoader).setSuppressionHash(suppressionHash);
			try {
				theLoader.run(props);
			} catch (Exception e) {
				log.error("Could load JDBC", e);
				error = 1;
			}
			
			if( theLoader.getDatabase() != null ){
				theLoader.getDatabase().setLabel(projectName);
				theLoader.getDatabase().relabel();
			} else {
				log.error("the database is null, exiting");
				return 0;
			}
		}

        File dumpFile = new File(pathPrefix + projectName + "/src/non-packaged-resources/" + modelSource + ".txt");
        FileWriter fstream = new FileWriter(dumpFile);
        BufferedWriter out = new BufferedWriter(fstream);

		theDatabase = theLoader.getDatabase();
		theDatabase.dump(out);
		out.close();
		
		log.debug("PathPrefix:"+pathPrefix);

		try {
			switch (mode) {
			case "tags":
				String packageRoot = packageName;
				if (Boolean.parseBoolean(props.getProperty("generate.tags", "true"))) {
					String tagLocation = props.getProperty("tag.file.location", pathPrefix + "/" + projectName+ "/"  + "src");
					TagClassGenerator theGenerator;
					String databaseType = props.getProperty("database.type", "postgres");
					
					theGenerator = new TagClassGenerator(tagLocation, packageRoot, projectName, databaseType);
					
					try {
						theGenerator.generateTagClasses(theDatabase);
					} catch (IOException e2) {
						log.error("Could not generate Tag Classes: " + tagLocation, e2);
						error=1;
					}
				}
				
				if (Boolean.parseBoolean(props.getProperty("generate.tld", "true"))) {
					//TLDGenerator theTLDgenerator = new TLDGenerator(tldLocation, packageRoot, projectName);
					TLDGenerator theTLDgenerator = new TLDGenerator(props);
					try {
						theTLDgenerator.generateTLD(theDatabase);
					} catch (IOException e1) {
						log.error("Could not generate TLD File: " +  props.getProperty("tld.file.location"), e1);
						error = 1;
					}
				}
				
				if (Boolean.parseBoolean(props.getProperty("generate.jsps", "true"))) {
					String jspLocation = props.getProperty("jsp.file.location", pathPrefix + projectName + "/WebContent/");
					
					JSPGenerator theJSPgenerator;
					if(props.getProperty("jsp.taglibrary.prefix") != null){
						theJSPgenerator = new JSPGenerator(jspLocation, packageRoot, projectName, props.getProperty("jsp.taglibrary.prefix"));
					}else{
						theJSPgenerator = new JSPGenerator(jspLocation, packageRoot, projectName);
					}
					
					if (props.getProperty("jsp.resources.location") != null)
						theJSPgenerator.setResourcesPath(props.getProperty("jsp.resources.location"));
					
					try {
						theJSPgenerator.generateJSPs(webAppName, theDatabase);
					} catch (IOException e) {
						log.error("Could not generate JSP Files: " + jspLocation, e);
						error = 1;
					}
				}
				break;
			case "hibernate":
				SpringHibernateModel model = new SpringHibernateModel( theDatabase, packageName, props );

				/*
				 * generate.domain = true 
				 */
				if ( Boolean.parseBoolean( props.getProperty( "generate.domain", "true" ) ) ) {
					String domainPath = props.getProperty( "domain.file.location", pathPrefix + projectName + "/" + "src" );
					DomainCodeGenerator codeGen = new DomainCodeGenerator( model, domainPath, packageName );
					try {
						log.debug( "***********Writing domain code*****************" );
						codeGen.generate();
					} catch ( IOException e ) {
						log.debug( "Error writing domain code" );
						log.error( "Error writing domain code", e );
						error = 1;
					}
				} else {
					log.debug("Not generating domain code");
				}

				/*
				 * generate.dao = true 
				 */
				if (Boolean.parseBoolean(props.getProperty("generate.dao", "true"))) {
					String daoPath = props.getProperty("dao.file.location",	pathPrefix +projectName+ "/"  + "src");
					
					DAOCodeGenerator codeGen = new DAOCodeGenerator(model,daoPath,packageName,props);
					
					try {
						codeGen.generate();
					} catch (Exception e3) {
						log.error("Could not generate DAO Classes: " +daoPath, e3);
						error=1;
					}
				} else {
					log.debug("Not generating dao code");
				}

				/*
				 * generate.controller = true 
				 */
				if ( Boolean.parseBoolean( props.getProperty( "generate.controller", "true" ) ) ) {
					String controllerPath = props.getProperty( "controller.file.location", pathPrefix + projectName + "/" + "src" );
					ControllerCodeGenerator codeGen = new ControllerCodeGenerator( model, controllerPath, packageName, props );
					try {
						codeGen.generate();
					} catch ( Exception e3 ) {
						log.error( "Could not generate Controller Classes: " + controllerPath, e3 );
						error = 1;
					}
				} else {
					log.debug( "Not generating controller code" );
				}
				
				
				/*
				 * generate.jsp = true 
				 */
				if (Boolean.parseBoolean(props.getProperty("generate.jsp", "true"))) {
					String jspPath = props.getProperty("jsp.file.location",	pathPrefix +projectName+ "/"  + "src");
					JSPCodeGenerator codeGen = new JSPCodeGenerator(model,jspPath,packageName,props);
					try {
						codeGen.generate();
					} catch (Exception e3) {
						log.error("Could not generate JSP files: " +jspPath, e3);
						error=1;
					}
				} else {
					log.debug("Not generating jsp code");
				}
				
				/*
				 * generate.test = true 
				 */
				if ( Boolean.parseBoolean( props.getProperty( "generate.test", "true" ) ) ) {
					String testPath = props.getProperty( "test.file.location", pathPrefix + projectName + "/" + "src" );
					BaseTestCodeGenerator codeGen = new BaseTestCodeGenerator( model, testPath, packageName, props );
					try {
						codeGen.generate();
					} catch ( Exception e3 ) {
						log.error( "Could not generate Test Classes: " + testPath, e3 );
						error = 1;
					}
				} else {
					log.debug( "Not generating test code" );
				}
				
				/*
				 * deobfuscate.column.names = true
				 */
				if (Boolean.parseBoolean(props.getProperty("deobfuscate.column.names", "false"))) {
					String path = props.getProperty("dao.file.location", pathPrefix + projectName+ "/"  + "src");
					ColumnDeobfuscationCodeGenerator codeGen = new ColumnDeobfuscationCodeGenerator(model,path,packageName,props);
					try {
						codeGen.generate();
					} catch (Exception e3) {
						log.error("Could not generate Column Deobfuscation files: " + path, e3);
						error=1;
					}
				} else {
					log.debug("Not generating test code");
				}
				break;
			default:
				break;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return error;
	}
	
	public static void main(String[] args) throws Exception {
		Properties myProps = new Properties();


		if (args.length > 2)
		{
			pathPrefix = args[2];


		}
		else
		{
			log.error("Error: invalid number of arguments. needs 2.");
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

		log.debug("Generator mode: "+ myProps.getProperty("mode"));
	}

	public static Database getDatabase() {
		return theDatabase;
	}

}
