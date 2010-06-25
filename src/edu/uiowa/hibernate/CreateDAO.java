package edu.uiowa.hibernate;

public class CreateDAO {

	
	private String templateFile = "";

	private String TABLENAME = "@TABLENAME@";
	private String PROGNAME = "@PROGNAME@";
	private String tablename = "@tablename@";

	private String tableName;
	private String projectName;
	
	private static String TEMPLATE_PATH="";
	private String suffix;

	public CreateDAO() {
		this.templateFile=TEMPLATE_PATH + "TABLENAMEService.tmplt";
	}

	public CreateDAO(String type) {
		if (type.equals("Service")) {
			this.templateFile=TEMPLATE_PATH + "TABLENAMEService.tmplt";
			this.suffix=type;
			
		} else if (type.equals("Home")) {
			this.templateFile=TEMPLATE_PATH + "TABLENAMEHome.tmplt";
			this.suffix=type;
		} else if (type.equals("GenericDao")) {
			this.templateFile=TEMPLATE_PATH + "GenericDao.tmplt";
			this.suffix=type;
		} else if (type.equals("GenericDaoInterface")) {
			this.templateFile=TEMPLATE_PATH + "GenericDaoInterface.tmplt";
			this.suffix=type;
		}
		
	}
	
	public String packageName() {
		return "edu.uiowa." + projectName + "." + tableName + suffix;
	}
	
	public String fileName() {
		return tableName + suffix + ".java";
	}
	
//
//	public String fullPath() {
//		return "edu/uiowa/" + projectName + "/" + tableName + "Service.java";
//	}
//	
	
	public String get() throws Exception {
		
		String template = FileContents.getContents(templateFile);
		template = template.replaceAll(TABLENAME, tableName);
		System.out.println("projectName: " +  projectName);
		template = template.replaceAll(PROGNAME, projectName);
		template = template.replaceAll(tablename, tableName.toLowerCase());
		return template;
	}
	
	public String get(String templateFile, String projectName, String tableName) throws Exception {
		
		String template = FileContents.getContents(templateFile);
		template = template.replaceAll(TABLENAME, tableName);
		template = template.replaceAll(PROGNAME, projectName);
		template = template.replaceAll(tablename, tableName.toLowerCase());
		return template;
	}
	
	public String get(String projectName, String tableName) throws Exception {
		
		String template = FileContents.getContents(templateFile);
		template = template.replaceAll(TABLENAME, tableName);
		template = template.replaceAll(PROGNAME, projectName);
		template = template.replaceAll(tablename, tableName.toLowerCase());
		return template;
	}
	
	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}


}
