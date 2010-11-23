package edu.uiowa.webapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.lang.reflect.*;


import edu.uiowa.webapp.ClassVariable.AttributeType;
import edu.uiowa.webapp.ClassVariable.RelationshipType;

public class DomainClass
{
	
	public enum ClassType {
	    ENTITY, COMPOSITEID, PLAIN
	}
	private String modifier ="";
	private String identifier="";
	private String tableName="";
	private String comment="";
	private String packageName="";
	private List<ClassVariable> symTable = new ArrayList<ClassVariable>();
	private List<String> importList = new ArrayList<String>();
	private ClassType classType;
	private Entity entity=null;
	private Schema schema=null;
	private boolean usesCompositeKey;
	
	
	


	
	

	public boolean isUsesCompositeKey() {
		return usesCompositeKey;
	}
	public void setUsesCompositeKey(boolean usesCompositeKey) {
		this.usesCompositeKey = usesCompositeKey;
	}
	public Schema getSchema() {
		return schema;
	}
	public void setSchema(Schema schema) {
		this.schema = schema;
	}
	public Entity getEntity() {
		return entity;
	}
	public void setEntity(Entity entity) {
		this.entity = entity;
	}
	public ClassType getClassType() {
		return classType;
	}
	public void setClassType(ClassType classType) {
		this.classType = classType;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public List<ClassVariable> getSymTable() {
		return symTable;
	}
	public void setSymTable(List<ClassVariable> symTable) {
		this.symTable = symTable;
	}
	
	public String getModifier() {
		return modifier;
	}
	public void setModifier(String modifier) {
		this.modifier = modifier;
	}

	public String getIdentifier() {
		return identifier;
	}
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public String getPackageName() {
		return packageName;
	}
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	
	String getLowerIdentifier()
	{
		return identifier.substring(0, 1).toLowerCase() + identifier.substring(1);
	}

	public List<String> getImportList() {
		return importList;
	}
	public void setImportList(List<String> importList) {
		this.importList = importList;
	}
	public Iterator<ClassVariable> listAllIter()
	{
		List<ClassVariable> listAllSym = new ArrayList<ClassVariable>();
		
		listAllSym.addAll(getPrimaryKeys());
		listAllSym.addAll(getNonKeys());
	
		
		return listAllSym.iterator();
	}
	
	public String symbolsToString(String indent)
	{
		
		Iterator<ClassVariable> cvIter = listAllIter();
		String output = "";
		while(cvIter.hasNext())
		{
			ClassVariable cv = cvIter.next();
			output += indent + cv.toAnnotationDeclaration();
			output += indent + cv.toDeclaration();
		}


		return output;

	}
	
	public String getSelectBoxLabel()
	{
		
		Iterator<ClassVariable> cvIter = listAllIter();
		String output = "";
		while(cvIter.hasNext())
		{
			ClassVariable cv = cvIter.next();
			output += "${item."+cv.getIdentifier()+"}";
			if(cvIter.hasNext())
				output += " - ";
				
		}


		return output;

	}

	public String genGettersSetters(String indent)
	{
		Iterator<ClassVariable> cvIter = listAllIter();
		String output = "";
		while(cvIter.hasNext())
		{
			ClassVariable cv = cvIter.next();
			output += indent + "/*****" + cv.getIdentifier() + "*****/\n";
			output += cv.toGetter(indent) + "";
			output += cv.toSetter(indent) + "\n";
		}
		return output;
	}
	
	
	public String genConstructor(String indent)
	{
		Iterator<ClassVariable> cvIter = listAllIter();
		String output = indent + "public " + identifier + "()";

		output+=indent+"{\n";

		
		while(cvIter.hasNext())
		{
			ClassVariable cv = cvIter.next();
			String init = "";
			if(cv.getInitializer().isEmpty())
			{
				if(cv.getType().equals("String"))
					init = " = \"\"";
				else if (cv.getType().equals("int"))
					init = " = 0";
				else if (cv.getType().equals("long"))
					init = " = 0";
				else if (cv.getType().equals("float"))
					init = " = 0";
				else if (cv.getType().equals("double"))
					init = " = 0";

				else if (cv.getType().equals("boolean"))
					init = " = false";

				else
					init = ""; 
			}
			else 
				init = cv.getInitializer();



			if(!init.isEmpty())
			output += indent + indent + "this." + cv.getIdentifier()+ init+ ";";
			output+="\n";

		}
		output+=indent+"}\n";
		return output;
	}
	public String genConstructorWithId(String indent)
	{
		Iterator<ClassVariable> cvIter = getPrimaryKeys().iterator();
		String output = indent + "public " + identifier + "(";
		while(cvIter.hasNext())
		{
			ClassVariable cv = cvIter.next();
			output += cv.getType() + " " + cv.getIdentifier();
			if(cvIter.hasNext())
				output += ", ";
		}

		output+=")\n"+indent+"{\n";
		
		 cvIter = getPrimaryKeys().iterator();
		while(cvIter.hasNext())
		{
			ClassVariable cv = cvIter.next();
			output += indent + indent + "this." + cv.getIdentifier() + " = " + cv.getIdentifier() + ";";
			output+="\n";

		}
		output+=indent+"}\n";
		return output;
	}

	public String genConstructorWithArgs(String indent)
	{
		Iterator<ClassVariable> cvIter = listAllIter();
		String output = indent + "public " + identifier + "(";
		while(cvIter.hasNext())
		{
			ClassVariable cv = cvIter.next();
			output += cv.getType() + " " + cv.getIdentifier();
			if(cvIter.hasNext())
				output += ", ";
		}

		output+=")\n"+indent+"{\n";

		 cvIter = listAllIter();
		while(cvIter.hasNext())
		{
			ClassVariable cv = cvIter.next();
			output += indent + indent + "this." + cv.getIdentifier() + " = " + cv.getIdentifier() + ";";
			output+="\n";

		}
		output+=indent+"}\n";
		return output;
	}
	
	public String genImportList(String indent)
	{
		String output="";
		Iterator<String> ilI = importList.iterator();
		while(ilI.hasNext())
			output += indent + "import "+ ilI.next() + ";\n";
		return output;
		
	}
	
	public void populateClassVariableDomainClass()
	{

		Iterator<ClassVariable> cvIter = symTable.iterator();
		while(cvIter.hasNext())
			cvIter.next().setDomainClass(this);
	
	}


	public String toString()
	{
		String indent = "";
		String output = "";
		output += "package " + packageName + ";";
		output += "\n";
		output += "\n";
		output += genImportList(indent);
		output += "\n";
		output += "\n";
		output += genAnnotations(indent) + "\n";
		output += modifier + " class " + identifier + "\n";
		output += "{\n";
		indent = "    ";
		output += symbolsToString(indent);
		output += "\n";
//		output += genConstructorWithId(indent);
//		output += "\n";
		output += genConstructor(indent);
		output += "\n";
		output += genConstructorWithArgs(indent);

		output += "\n";
		output += genGettersSetters(indent);
		output += "\n";
		output += "}\n";

		return output;

	}
	private String genAnnotations(String indent) {
		if(classType == ClassType.ENTITY && entity != null)
		return "@Entity\n@Table(name = \""+entity.getSqlLabel()+"\", schema=\""+schema.getSqlLabel()+"\")";
		else
			return "";
	}
	public List<ClassVariable> getPrimaryKeys() {
		List<ClassVariable> cvList = new ArrayList<ClassVariable>();
		Iterator<ClassVariable> symIter = symTable.iterator();
		while(symIter.hasNext())
		{
			ClassVariable cv = symIter.next();
			if(cv.isPrimary() )//&& cv.getAttribType() != AttributeType.FOREIGNPRIMARYKEY)
				cvList.add(cv);
			
			
			
			
			
		}
		
		return cvList;
	}
	
	public List<ClassVariable> getForeignClassVariables() {
		List<ClassVariable> cvList = new ArrayList<ClassVariable>();
		Iterator<ClassVariable> symIter = symTable.iterator();
		while(symIter.hasNext())
		{
			ClassVariable cv = symIter.next();
			if(cv.getAttribType() == AttributeType.FOREIGNATTRIBUTE)
				cvList.add(cv);
			
		}
		
		return cvList;
	}
	
	public List<ClassVariable> getNonKeys() {
		List<ClassVariable> cvList = new ArrayList<ClassVariable>();
	
		
		
		for(ClassVariable cv: symTable)
		{
			if(!cv.isPrimary() )
				cvList.add(cv);
		
		}
		
		return cvList;
	}


}