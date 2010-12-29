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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.uiowa.webapp.Database;
import edu.uiowa.webapp.Schema;

public class ConfigGenerator {



	private String projectPath = null;
	private String packageRoot = null;

	private String currentPackageDirectory = null;
	private String currentPackageName = null;

	private static final Log log =LogFactory.getLog(ConfigGenerator.class);

	
	String projectName = null;

	Schema theSchema = null;

	public ConfigGenerator(String projectPath, String packageRoot, String projectName) {
		this.projectPath = projectPath;
		this.packageRoot = packageRoot;
		this.projectName = projectName;
	}




	public void generateDispatcher(Database db) throws Exception {


		SpringConfiguration config = new SpringConfiguration();
		config.setPackageRoot(packageRoot);
		config.genModelList(db.getSchemas());
		config.setDatabaseName(db.getSqlLabel());
		
		log.debug("Here:");
		
		File file = new File(projectPath, "..//WebContent/WEB-INF/dispatcher-servlet.xml");
		
	
		if(!file.exists())
		{
		FileWriter fstream = new FileWriter(file);
		BufferedWriter out = new BufferedWriter(fstream);

		out.write(config.get());
		out.close();
		}
		else
			log.debug("" + file.getCanonicalPath() + " Exists. Not Overwriting");




	}

}
