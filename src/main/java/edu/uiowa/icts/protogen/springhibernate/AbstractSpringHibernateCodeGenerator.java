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
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.uiowa.webapp.Entity;

public abstract class AbstractSpringHibernateCodeGenerator {

	protected String pathBase = null; 
	protected String packageRoot = null; 
	protected String packageRootPath = null; 
	protected SpringHibernateModel model=null;
	protected boolean overwrite=false;


	protected static final Log log =LogFactory.getLog(AbstractSpringHibernateCodeGenerator.class);


	public AbstractSpringHibernateCodeGenerator(SpringHibernateModel model, String pathBase,String packageRoot) {
		this.model = model;
		this.pathBase = pathBase;
		this.packageRoot = packageRoot;
		packageRootPath = pathBase + "/"	+ packageRoot.replaceAll("\\.", "/");
		(new File(packageRootPath)).mkdirs();
		

	}
	
	/*
	 *  this method is used to generate the code
	 * 
	 * 
	 */
	public abstract void generate() throws IOException ;


	public String getPathBase() {
		return pathBase;
	}


	public void setPathBase(String pathBase) {
		this.pathBase = pathBase;
	}

	protected void lines(BufferedWriter out, int num) throws IOException {
		for (int i = 0; i < num; i++)
			out.write("\n");
	}

	protected void spaces(BufferedWriter out, int num) throws IOException {
		for (int i = 0; i < num; i++)
			out.write(" ");
	}

	public boolean isOverwrite() {
		return overwrite;
	}

	public void setOverwrite(boolean overwrite) {
		this.overwrite = overwrite;
	}
	

	


}
