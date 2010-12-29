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
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.uiowa.webapp.Schema;

public class ControllerCodeGenerator {



	private String pathBase = null;
	private String packageRoot = null;

	private String currentPackageDirectory = null;
	private String currentPackageName = null;


	Schema theSchema = null;

	private static final Log log =LogFactory.getLog(ControllerCodeGenerator.class);

	
	public ControllerCodeGenerator(String pathBase, String packageRoot) {
		this.pathBase = pathBase;
		this.packageRoot = packageRoot;
	}

	public void generateControllerClasses(List<DomainClass> dcList) throws Exception {

		
		Iterator<DomainClass> dcIter = dcList.iterator();
		
		while(dcIter.hasNext())
		{
			DomainClass dc = dcIter.next();
			currentPackageName = packageRoot + "." + dc.getSchema().getLabel() + "";
			currentPackageDirectory = pathBase + "/"	+ currentPackageName.replaceAll("\\.", "/") + "/controller";
			(new File(currentPackageDirectory)).mkdirs();

			generateControllerClass(dc);
			
		}
	

	}
	





	private void generateControllerClass(DomainClass ent) throws Exception {


		CreateController controller = new CreateController();
		controller.setProjectName(currentPackageName);
		controller.setTableName(ent.getLowerIdentifier());
		controller.setClassIdentifier(ent.getIdentifier());
		controller.setSchemaName(ent.getSchema().getLabel());
		controller.genModelList(ent);
		
		File domainFile = new File(currentPackageDirectory, "/" +controller.getFileName());
		if(!domainFile.exists())
		{
		FileWriter fstream = new FileWriter(domainFile);
		BufferedWriter out = new BufferedWriter(fstream);

		out.write(controller.get());
		out.close();
		}
		else
			log.debug("" + domainFile.getCanonicalPath() + " Exists. Not Overwriting");




	}

}
