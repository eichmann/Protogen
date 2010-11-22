/*
 * Created on May 12, 2008
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.uiowa.webapp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import edu.uiowa.hibernate.CreateDAO;

public class DAOGenerator {

	private String projectPath = null;
	private String packageRoot = null;

	private String currentPackageDirectory = null;
	private String currentPackageName = null;

	String projectName = null;

	DomainClass currentDomainClass = null;


	public DAOGenerator(String projectPath, String packageRoot, String projectName) {
		this.projectPath = projectPath;
		this.packageRoot = packageRoot;
		this.projectName = projectName;
	}

	public void generateDaoClasses(List<DomainClass> dcList) throws Exception {

		Iterator<DomainClass> dcIter = dcList.iterator();
		
		while(dcIter.hasNext())
		{
			currentDomainClass = dcIter.next();
			currentPackageName = packageRoot + "." + currentDomainClass.getSchema().getUnqualifiedLabel() + "";
			currentPackageDirectory = projectPath + "/"	+ currentPackageName.replaceAll("\\.", "/") + "/dao";
			(new File(currentPackageDirectory)).mkdirs();
			try {
				copyGenericDaoInterface();
				copyGenericDao();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			generateDaoClass(currentDomainClass);
			
		}
		
		


	}


	private void copyGenericDaoInterface() throws Exception {
	    CreateDAO genDao = new CreateDAO("GenericDaoInterface");
	    genDao.setProjectName(currentPackageName);
		genDao.setTableName("");
		File domainFile = new File(currentPackageDirectory, "/" +genDao.fileName());
		if (!domainFile.exists())
		{
		FileWriter fstream = new FileWriter(domainFile);
		BufferedWriter out = new BufferedWriter(fstream);

		out.write(genDao.get());
		out.close();
		} else 
			System.out.println("" + domainFile.getCanonicalPath() + " Exists. Not Overwriting");
	}
	
	private void copyGenericDao() throws Exception {
	    CreateDAO genDao = new CreateDAO("GenericDao");
	    genDao.setProjectName(currentPackageName);
		genDao.setTableName("");
		File domainFile = new File(currentPackageDirectory, "/" +genDao.fileName());
		if (!domainFile.exists())
		{
		FileWriter fstream = new FileWriter(domainFile);
		BufferedWriter out = new BufferedWriter(fstream);

		out.write(genDao.get());
		out.close();
		} else 
			System.out.println("" + domainFile.getCanonicalPath() + " Exists. Not Overwriting");
	}

	private void generateDaoClass(DomainClass dc) throws Exception {


		Entity ent = dc.getEntity();

		CreateDAO homeDao = new CreateDAO("Home");
		homeDao.setProjectName(currentPackageName);
		homeDao.setTableName(ent.getUnqualifiedLabel());

		File domainFile = new File(currentPackageDirectory, "/" +homeDao.fileName());
		if (!domainFile.exists())
		{
		FileWriter fstream = new FileWriter(domainFile);
		BufferedWriter out = new BufferedWriter(fstream);

		out.write(homeDao.get());
		out.close();
		} else System.out.println("" + domainFile.getCanonicalPath() + " Exists. Not Overwriting");
		
		CreateDAO serviceDao = new CreateDAO("Service");
		serviceDao.setProjectName(currentPackageName);
		serviceDao.setTableName(ent.getUnqualifiedLabel());
		File domainFileService = new File(currentPackageDirectory, "/" +serviceDao.fileName());
		if (!domainFileService.exists())
		{
		FileWriter fstreamServcice = new FileWriter(domainFileService);
		BufferedWriter outService = new BufferedWriter(fstreamServcice);
		outService.write(serviceDao.get());
		outService.close();
		}
		else
			System.out.println("" + domainFileService.getCanonicalPath() + " Exists. Not Overwriting");


	}

}
