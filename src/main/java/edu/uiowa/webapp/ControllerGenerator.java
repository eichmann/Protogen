/*
 * Created on May 12, 2008
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.uiowa.webapp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import edu.uiowa.spring.CreateController;

public class ControllerGenerator {



	private String projectPath = null;
	private String packageRoot = null;

	private String currentPackageDirectory = null;
	private String currentPackageName = null;

	String projectName = null;

	Schema theSchema = null;

	public ControllerGenerator(String projectPath, String packageRoot, String projectName) {
		this.projectPath = projectPath;
		this.packageRoot = packageRoot;
		this.projectName = projectName;
	}

	public void generateControllerClasses(List<DomainClass> dcList) throws Exception {

		
		Iterator<DomainClass> dcIter = dcList.iterator();
		
		while(dcIter.hasNext())
		{
			DomainClass dc = dcIter.next();
			currentPackageName = packageRoot + "." + dc.getSchema().getLabel() + "";
			currentPackageDirectory = projectPath + "/"	+ currentPackageName.replaceAll("\\.", "/") + "/controller";
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
			System.out.println("" + domainFile.getCanonicalPath() + " Exists. Not Overwriting");




	}

}
