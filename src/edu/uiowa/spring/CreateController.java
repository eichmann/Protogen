package edu.uiowa.spring;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import edu.uiowa.hibernate.FileContents;
import edu.uiowa.webapp.DomainClass;
import edu.uiowa.webapp.Entity;
import edu.uiowa.webapp.Relationship;

public class CreateController {

	
	private String templateFile = "";

	private String TABLENAME = "@TABLENAME@";
	private String PROGNAME = "@PROGNAME@";
	private String tablename = "@tablename@";
	private String tableName = "@tableName@";
	private String parentlist = "@parentlist@";
	private String parentservices = "@parentservices@";
	private String onsaveparams = "@onsaveparams@";
	private String onsavebody = "@onsavebody@";
	private String schemaNameToken = "@schemaname@";
	private String schemaName = "";
	
	
	private String inputTableName;
	private String classIdentifier;
	
	private String projectName;
	private String parentlistcode;
	private String parentservicescode;
	private String onsaveparamscode;
	private String onsavebodycode;
	
	private static String TEMPLATE_PATH="";
	private String suffix = "Controller";

	public CreateController() {
		this.templateFile=TEMPLATE_PATH + "TABLENAMEController.tmplt";
	}

	
	public String getPackageName() {
		return "edu.uiowa." + projectName + "." + inputTableName + suffix;
	}
	
	public String getFileName() {
		return classIdentifier + suffix + ".java";
	}
	
	
	public String get() throws Exception {
		
		String template = FileContents.getContents(templateFile);
		template = template.replaceAll(TABLENAME, classIdentifier);
		template = template.replaceAll(schemaNameToken, schemaName);
		template = template.replaceAll(PROGNAME, projectName);
		template = template.replaceAll(tableName, inputTableName);
		template = template.replaceAll(tablename, inputTableName.toLowerCase());
		template = template.replaceAll(parentlist, parentlistcode);
		template = template.replaceAll(parentservices, parentservicescode);
		template = template.replaceAll(onsaveparams, onsaveparamscode);
		template = template.replaceAll(onsavebody, onsavebodycode);
		return template;
	}
	
	public void genModelList(DomainClass dc)
	{
		String output ="";
		String output2 = "";
		String onsaveparams ="";
		String onsavecode ="";
		
		Iterator<Relationship> rIter = dc.getEntity().getParents().iterator();
		HashSet<String> hashSet = new HashSet<String>();
		while(rIter.hasNext())
		{
			Relationship r = rIter.next();
			Entity e = r.getSourceEntity();
			if(!hashSet.contains(e.getLabel()) && !e.getUnqualifiedLabel().equals(classIdentifier))
			{
			
			
			output += "        model.addAttribute(\""+e.getUnqualifiedLowerLabel()+"List\"," + e.getUnqualifiedLowerLabel() + "Service.list());\n";
			output2 += "    private " + e.getUnqualifiedLabel() + "Service " + e.getUnqualifiedLowerLabel() + "Service;\n\n";
			output2 += "    @Autowired\n";
			output2 += "    public void set" + e.getUnqualifiedLabel() + "Service("+e.getUnqualifiedLabel() + "Service " + e.getUnqualifiedLowerLabel() +"Service)\n";
			output2 += "    { this." + e.getUnqualifiedLowerLabel() + "Service = " + e.getUnqualifiedLowerLabel() + "Service;}\n\n\n";
			onsaveparams += " @RequestParam(\""+e.getUnqualifiedLowerLabel()+"Id\") int "+e.getUnqualifiedLowerLabel()+", ";
			onsavecode += dc.getEntity().getUnqualifiedLowerLabel()+".set"+e.getUnqualifiedLabel()+"("+e.getUnqualifiedLowerLabel()+"Service.findById("+e.getUnqualifiedLowerLabel()+"));\n";
			
				
			hashSet.add(e.getLabel());
			}
		}
		this.onsavebodycode=onsavecode;
		this.onsaveparamscode=onsaveparams;
		parentlistcode=output;
		parentservicescode=output2;
		
	}
	
	/**************
	private ActionService actionService;
	
	@Autowired
	public void setActionService(ActionService actionService) {
		this.actionService = actionService;
	}
	
	
	/*******************/
	
	public String getTableName() {
		return inputTableName;
	}

	public void setTableName(String tableName) {
		this.inputTableName = tableName;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}


	public String getClassIdentifier() {
		return classIdentifier;
	}


	public void setClassIdentifier(String classIdentifier) {
		this.classIdentifier = classIdentifier;
	}


	public String getSchemaName() {
		return schemaName;
	}


	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}
	
	

}
