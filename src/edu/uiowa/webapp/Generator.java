package edu.uiowa.webapp;

public class Generator {
	static String pathPrefix = "/Users/eichmann/Documents/Components/workspace/";
	static String mode = "tags";
	static String modelSource = "clay";
	static String dburl="";
	static String dbusername="" ;
	static String dbpassword = "";
	static String dbdriverclass = "";
	static String dbschema = "";
	static boolean dbssl = true;

	/**
	 * @param args
	 * arg 0 = qualified package name (required)
	 * arg 1 = Eclipse project name (required)
	 * arg 2 = location of Eclipse workspace (can be relative, is optional)
	 *
	 * @throws Exception 
	 */

	static Database theDatabase = null;

	public static void main(String[] args) throws Exception {

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

		if (args.length > 3)
		{
			mode = args[3];
			if (args.length > 4)
			{
				modelSource = args[4];
				if(args.length>8)
				{
					dburl=args[5];
					dbusername=args[6];
					dbpassword=args[7];
					dbssl=Boolean.parseBoolean(args[8]);
					dbdriverclass=args[9];
					if(args.length>9)
						dbschema=args[10];
					
					
				}

			}

		}
		
		System.out.println("Generator mode:"+ mode);

		DatabaseSchemaLoader theLoader = null;
		if(modelSource.equalsIgnoreCase("clay"))
		{
			theLoader = new ClayLoader();
			theLoader.run(pathPrefix + args[1] + "/WebContent/resources/" + args[1] + ".clay");
			
		}
		else if(modelSource.equalsIgnoreCase("jdbc"))
		{
		
			theLoader = new JDBCLoader();
			theLoader.run("database.properties");
			theLoader.getDatabase().setLabel(args[1]);
			theLoader.getDatabase().relabel();
		}

		theDatabase = theLoader.getDatabase();
		theDatabase.dump();

		System.out.println("PathPrefix:"+pathPrefix);


		if(mode.equalsIgnoreCase("tags"))
		{
			String packageRoot = args[0];
			TagClassGenerator theGenerator = new TagClassGenerator(pathPrefix + "/" + args[1]+ "/"  + "src", packageRoot, args[1]);
			theGenerator.generateTagClasses(theDatabase);

			TLDGenerator theTLDgenerator = new TLDGenerator(pathPrefix + args[1] + "/WebContent/", packageRoot, args[1]);
			theTLDgenerator.generateTLD(theDatabase);

			JSPGenerator theJSPgenerator = new JSPGenerator(pathPrefix + args[1] + "/WebContent/", packageRoot, args[1]);
			theJSPgenerator.generateJSPs(theDatabase);
		}
		else
		{

			DomainCodeGenerator theDomainGenerator = new DomainCodeGenerator(pathPrefix + args[1]+ "/"  + "src", args[0], args[1]);
			theDomainGenerator.generateDomainCodeForDatabase(theDatabase);

			DAOGenerator theDaoGenerator = new DAOGenerator(pathPrefix + args[1]+ "/"  + "src", args[0], args[1]);
			theDaoGenerator.generateDaoClasses(theDomainGenerator.getDomainClassList());

			ControllerGenerator theControllerGenerator = new ControllerGenerator(pathPrefix + args[1]+ "/"  + "src", args[0], args[1]);
			theControllerGenerator.generateControllerClasses(theDomainGenerator.getDomainClassList());

			JSPCodeGenerator theJSPGenerator = new JSPCodeGenerator(pathPrefix + args[1]+ "/"  + "WebContent/WEB-INF/jsp/");
			theJSPGenerator.generateAllJSP(theDomainGenerator.getDomainClassList());

			ConfigGenerator configGen = new ConfigGenerator(pathPrefix + args[1]+ "/"  + "src", args[0], args[1]);
			configGen.generateDispatcher(theDatabase);

		}



	}

	static Database getDatabase() {
		return theDatabase;
	}

}
