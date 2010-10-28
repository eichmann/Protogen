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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;




import edu.uiowa.webapp.ClassVariable.AttributeType;
import edu.uiowa.webapp.ClassVariable.RelationshipType;
import edu.uiowa.webapp.DomainClass.ClassType;


public class DomainCodeGenerator {

	private String projectPath = null;

	private String packageRoot = null;

	private String projectName = null;

	private String currentPackageDirectory = null;
	private String currentPackageName = null;

	private List<DomainClass> domainClassList = new ArrayList<DomainClass>();
	
	private Schema currentSchema;


	public List<DomainClass> getDomainClassList() {
		return domainClassList;
	}

	public void setDomainClassList(List<DomainClass> domainClassList) {
		this.domainClassList = domainClassList;
	}

	public DomainCodeGenerator(String projectPath, String packageRoot, String projectName) 
	{
		this.projectPath = projectPath;
		this.packageRoot = packageRoot;
		this.projectName = projectName;

	}

	public void generateDomainCodeForDatabase(Database theDatabase)
	throws IOException {
		
		Iterator<Schema> schemaIter = theDatabase.getSchemas().iterator();
		
		while(schemaIter.hasNext())
		{
			currentSchema = schemaIter.next();
			currentPackageName = packageRoot + "." + currentSchema.getUnqualifiedLabel() + "." + "domain";
			currentPackageDirectory = projectPath + "/"	+ currentPackageName.replaceAll("\\.", "/");
			(new File(currentPackageDirectory)).mkdirs();
			
			currentSchema.populateEntityAttributeForeignReference();
			generateDomainCodeForSchema(currentSchema);
	
			
		}
		
	

	}

	private void generateDomainCodeForSchema(Schema schema) throws IOException {

		for (int i = 0; i < schema.getEntities().size(); i++)
		{

			
			DomainClass ec = generateDomainCodeForEntity(schema.getEntities().elementAt(i));
			if(ec != null)
				domainClassList.add(ec);		
			System.out.println("check:"+i+" -"+schema.getEntities().elementAt(i).getLabel());
		}
		
		connectLinks();
		writeFiles();

	}
	


	private DomainClass generateDomainCodeForEntity(Entity entity) throws IOException {
		
		if(isManyToMany(entity))
		{
			System.out.println("Entity is manyToMany.  Not Creating Class");
			return null;
		}
	
		HashMap<String,Attribute> foreignAndPrimaryKeysAttributes = getHashFromAttributesFtPt(entity.getAttributes().iterator());	
		HashMap<String,Attribute> foreignAndNotPrimaryKeysAttributes = getHashFromAttributesFtPf(entity.getAttributes().iterator());
		HashMap<String,Attribute> notForeignAndPrimaryKeysAttributes = getHashFromAttributesFfPt(entity.getAttributes().iterator());
		HashMap<String,Attribute> notForeignAndNotPrimaryKeysAttributes = getHashFromAttributesFfPf(entity.getAttributes().iterator());
	
		System.out.println("******** Entity = " + entity.getSqlLabel());


		/***Domain Class components***/
		HashSet<String> symTableHash = new HashSet<String>();
		List<ClassVariable> symTable = new ArrayList<ClassVariable>();
		List<String> importList = new ArrayList<String>();

		importList.add("java.util.Set");
		importList.add("java.util.*");
		importList.add("java.text.DateFormat");
		importList.add("java.text.SimpleDateFormat");
		importList.add("java.text.ParseException");
		importList.add("java.util.Date");

		
		importList.add("javax.persistence.*");
		importList.add("javax.persistence.Entity");
		importList.add("javax.persistence.Table");
		importList.add("javax.persistence.Table");
		importList.add("javax.persistence.Table");
		importList.add("javax.persistence.Column");
		importList.add("javax.persistence.FetchType");
		importList.add("javax.persistence.JoinColumn");
		importList.add("javax.persistence.ManyToOne");
		
		
		importList.add("org.springframework.format.annotation.DateTimeFormat");
		importList.add("org.hibernate.annotations.*");
		importList.add("javax.persistence.CascadeType");
		importList.add(currentPackageName + ".*");


		//If entity's primary key's are composite, create class and attribute for composite ID
		if (entity.getPrimaryKeyAttributes().size()>1)
		{
			generateCompositeIdClassForEntity(entity);

			ClassVariable v =new ClassVariable("private", entity.getUnqualifiedLabel()+"Id", entity.getUnqualifiedLowerLabel() + "Id");

			v.setAttribType(AttributeType.COMPOSITEKEY);
			v.setComment("Composite Key");
			v.getGetterAnnotations().add("@EmbeddedId");
			v.getGetterAnnotations().add("@AttributeOverrides( {\n");
			Iterator<Attribute> at = entity.getPrimaryKeyAttributes().iterator();
			while (at.hasNext())
			{
				Attribute a = at.next();
				v.getGetterAnnotations().add("@AttributeOverride(name = \""+a.getUnqualifiedLabel()+"\", column = @Column(name = \""+a.getSqlLabel()+"\", nullable = false))");
				if(at.hasNext())
					v.getGetterAnnotations().add(",");;
					
			}
			v.getGetterAnnotations().add("})");
			symTable.add(v);
			symTableHash.add(v.getIdentifier());

		}

		//If only primary key
		if (entity.getPrimaryKeyAttributes().size() ==1)
		{

			Iterator<Attribute> attribIter1 = entity.getPrimaryKeyAttributes().iterator();
			if(attribIter1.hasNext())
			{

				Attribute attrib = attribIter1.next();
				ClassVariable v = new ClassVariable("private", attrib.getType(), attrib.getUnqualifiedLowerLabel());
				if(attrib.isForeign())
				{
//					attrib = entity.getAttributeByLabel(attrib.getUnqualifiedLabel());
					
					Entity parent = attrib.getReferencedEntity();//entity.getForeignReferenceEntity(attrib);
					
					v.setAttribType(AttributeType.FOREIGNPRIMARYKEY);
					v.setRelationshipType(RelationshipType.ONETOONE);

					v.setAttribute(attrib);
					v.setType(attrib.getType());	
					v.setComment("Foreign-Primary Key");
					v.getGetterAnnotations().add("@GenericGenerator(name = \"generator\", strategy = \"foreign\", parameters = @Parameter(name = \"property\", value = \""+parent.getUnqualifiedLowerLabel()+"\"))");
					v.getGetterAnnotations().add("@Id");
					v.getGetterAnnotations().add("@GeneratedValue(generator=\"generator\")");
					v.getGetterAnnotations().add("@Column(name = \""+attrib.getSqlLabel()+"\", unique = true, nullable = false)");		
				}
				else
				{
					v.setAttribType(AttributeType.PRIMARYKEY);
					v.setRelationshipType(RelationshipType.NONE);
					
					v.setAttribute(attrib);
					v.setComment("Primary key");
					v.getGetterAnnotations().add("@javax.persistence.SequenceGenerator(  name=\"gen\",  sequenceName=\""+currentSchema.getUnqualifiedLabel()+".seqnum\",allocationSize=1)");
					v.getGetterAnnotations().add("@Id");
					v.getGetterAnnotations().add("@GeneratedValue( strategy=GenerationType.SEQUENCE,generator=\"gen\")");
					v.getGetterAnnotations().add("@Column(name = \""+attrib.getSqlLabel()+"\", unique = true, nullable = false)");		
				}
				symTable.add(v);
				symTableHash.add(v.getIdentifier());
			}

		}


		//Local Column Attributes
		Iterator<Attribute> attribIter = entity.getAttributes().iterator();

		while(attribIter.hasNext())
		{	

			Attribute attrib = attribIter.next();
			if(!attrib.isForeign())
			{
			ClassVariable v = new ClassVariable("private", attrib.getType(), attrib.getUnqualifiedLowerLabel());

			v.setAttribType(AttributeType.LOCALATTRIBUTE);
			v.setRelationshipType(RelationshipType.NONE);
			
			v.setAttribute(attrib);
			v.setComment("Local Attribute");
			v.getGetterAnnotations().add("@Column(name = \""+attrib.getSqlLabel()+"\")");

			if(!symTableHash.contains(v.getIdentifier()))
				symTable.add(v);
			}

		}

		//Find Child Variables
		System.out.println("*************Entity:"+ entity.getSqlLabel() + " ---Count:" + entity.getChildren().size() );
		Iterator<Relationship> iter = entity.getChildren().iterator();
		HashMap<String, Integer> checkExists = new HashMap<String,Integer>();
		while (iter.hasNext()) {
			Relationship r = iter.next();
			Entity e = r.getTargetEntity();
			Iterator<Attribute> attribIter3 = getHashOfAttributesToEntity(entity,e).iterator();
			
			while(attribIter3.hasNext())
			{

				Attribute attrib = attribIter3.next();
				System.out.println("*************Entity:"+ entity.getSqlLabel() + " ---Attrib:" + attrib.getUnqualifiedLabel() );
				String postfix="";
				if(checkExists.containsKey(e.getUnqualifiedLabel()))
				{
					int counter = (checkExists.get(e.getUnqualifiedLabel()));
					postfix = "For" + attrib.getUnqualifiedLabel().substring(0, attrib.getUnqualifiedLabel().length()-2) + ""+ (counter>0?counter:"");
					checkExists.put(e.getUnqualifiedLabel(),counter+1);
				}
				else
				{	checkExists.put(e.getUnqualifiedLabel(),0);
				
				}

				ClassVariable v=null;
				if(isManyToMany(e))
				{
					 v=getManyToManyVariable(entity, e);
					 System.out.println("************** " + e.getUnqualifiedLabel() + " is ManyToMany Table" );

				}
				else	
				{
					String variableName= plural(e.getUnqualifiedLowerLabel()) + postfix;
					v = new ClassVariable("private", "Set<"+e.getUnqualifiedLabel()+">", variableName, " = new HashSet<"+e.getUnqualifiedLabel()+">(0)");
					//v = new ClassVariable("private", "Set", variableName, " = new HashSet(0)");
					if(e.getUnqualifiedLowerLabel().equalsIgnoreCase(entity.getUnqualifiedLowerLabel()))
						v.getGetterAnnotations().add("@OneToMany(fetch = FetchType.LAZY,   mappedBy = \""+entity.getUnqualifiedLowerLabel()+"\", targetEntity="+e.getUnqualifiedLabel()+".class, cascade=CascadeType.REMOVE)");
					else
						v.getGetterAnnotations().add("@OneToMany(fetch = FetchType.LAZY,   mappedBy = \""+entity.getUnqualifiedLowerLabel()+"\", targetEntity="+e.getUnqualifiedLabel()+".class, cascade=CascadeType.REMOVE)");
				}
				v.setAttribType(AttributeType.CHILD);
				v.setAttribute(attrib);
				symTable.add(v);
				symTableHash.add(v.getIdentifier());
			}
		}


		//Find Parent Variables
		List<Attribute> attribList = new ArrayList<Attribute>();

		attribList.addAll(foreignAndNotPrimaryKeysAttributes.values());
		attribList.addAll(foreignAndPrimaryKeysAttributes.values());
		System.out.println("***CREATING ForeignRefCode on Entity:"+ entity.getSqlLabel() + " ---Count:" + attribList.size() );
		Iterator<Attribute> iter2 = attribList.iterator();
		checkExists = new HashMap<String,Integer>();
		while (iter2.hasNext()) {
	
			Attribute at = iter2.next();
			System.out.println("***Attribute:"+at.getUnqualifiedLabel());
			Entity e = at.getReferencedEntity();//entity.getForeignReferenceEntity(at);
			
			if(e != null)
			{
				
				System.out.println("***Current:"+entity.getUnqualifiedLabel()+" **PARENT ENTITY = "+e.getUnqualifiedLabel()+" for "+ at.getUnqualifiedLabel());

					String postfix="";

					if(checkExists.containsKey(e.getUnqualifiedLabel()))
					{

						int counter = (checkExists.get(e.getUnqualifiedLabel()));
						postfix = "By" + at.getUnqualifiedLabel().substring(0, at.getUnqualifiedLabel().length()-2)+ ""+ (counter>0?counter:"");
						checkExists.put(e.getUnqualifiedLabel(),counter+1);
					}
					else
					{
						
						System.out.println("************EXISTS = "+e.getUnqualifiedLabel()+" for "+ at.getUnqualifiedLabel());

						checkExists.put(e.getUnqualifiedLabel(),0);
						
					
					}

					String variableName= e.getUnqualifiedLowerLabel()+"" + postfix;
					ClassVariable v = new ClassVariable("private", ""+e.getUnqualifiedLabel()+"", variableName);
					
					if(at.isPrimary() && entity.getPrimaryKeyAttributes().size()==1)
					{
						v.setRelationshipType(RelationshipType.ONETOONE);
						v.getGetterAnnotations().add("@OneToOne(fetch = FetchType.LAZY,  targetEntity="+e.getUnqualifiedLabel()+".class)");
						v.getGetterAnnotations().add("@PrimaryKeyJoinColumn");
					}
					else
					{
					v.setRelationshipType(RelationshipType.MANYTOONE);
					v.getGetterAnnotations().add("@ManyToOne(fetch = FetchType.LAZY,  targetEntity="+e.getUnqualifiedLabel()+".class )");
					if(at.isPrimary() && entity.getPrimaryKeyAttributes().size() >1)
						v.getGetterAnnotations().add("@JoinColumn(name = \""+at.getSqlLabel()+"\",nullable = false, insertable = false, updatable = false)");
					else
						v.getGetterAnnotations().add("@JoinColumn(name = \""+at.getSqlLabel()+"\",nullable = false)");//, insertable = false, updatable = false)");
					}
					v.setAttribute(at);
					v.setAttribType(AttributeType.FOREIGNATTRIBUTE);
					
					symTable.add(v);
					symTableHash.add(v.getIdentifier());

					
				
			}
			else
			{
				System.out.println("************PARENT ENTITY = NULL for "+ at.getUnqualifiedLabel());
			}
			
		}

		DomainClass domainClass = new DomainClass();
		domainClass.setSchema(currentSchema);
		domainClass.setClassType(ClassType.ENTITY);
		domainClass.setPackageName(currentPackageName);
		domainClass.setModifier("public");
		domainClass.setIdentifier(entity.getUnqualifiedLabel());
		domainClass.setTableName(entity.getSqlLabel());
		domainClass.setImportList(importList);
		domainClass.setEntity(entity);
		domainClass.setSymTable(symTable);
		domainClass.populateClassVariableDomainClass();
		entity.setDomainClass(domainClass);
	

//		File file = new File(packagePrefixDirectory, entity.getUnqualifiedLabel()	+ ".java");
//		FileWriter fstream = new FileWriter(file);
//		BufferedWriter out = new BufferedWriter(fstream);
//		out.write(domainClass.toString());
//		out.close();
//		
		return domainClass;

	}
	
	
	
	

	
	public String plural(String st)
	{
		return st+"s";
//		if (st.charAt(st.length()) == 's')
//			return st;
//		else if (st.charAt(st.length()-1) == 'y')
//			return st.substring(0, st.length()) + "ies";
//		else
//			return st + "s";
		
		
	}

	private void generateCompositeIdClassForEntity(Entity entity) throws IOException {
		

		File file = new File(currentPackageDirectory, entity.getUnqualifiedLabel()	+ "Id.java");
		
		if(file.exists())
		{
			System.out.println("" + file.getCanonicalPath() + " Exists. Not Overwriting");
			return;
		}
		
		FileWriter fstream = new FileWriter(file);
		BufferedWriter out = new BufferedWriter(fstream);

		out.write("package " + currentPackageName + ";\n");
		List<String> importList = new ArrayList<String>();
		importList.add("import java.util.Set;");
		importList.add("import java.util.*;");
		importList.add("import " + currentPackageName + ".*;");
		importList.add("import javax.persistence.*;");
		importList.add("import java.io.Serializable;");

		Iterator<String> importIter = importList.iterator();

		lines(out, 1);

		while (importIter.hasNext())
			out.write(importIter.next() + "\n");
		lines(out, 2);
		out.write("@Embeddable\n");
		out.write("public class " + entity.getUnqualifiedLabel() + "Id implements Serializable\n");
		out.write("{\n");
		lines(out, 2);
		spaces(out, 4);
		out.write("//Table attribute definitions\n");
		Iterator<Attribute> attribIter0 = entity.getPrimaryKeyAttributes().iterator();
		while(attribIter0.hasNext())
		{
			Attribute attrib = attribIter0.next();
			String field = "";
			field = "private " + attrib.getType() + " "	+ attrib.getUnqualifiedLowerLabel() + ";\n";
			spaces(out, 4);
			out.write(field);

		}
		lines(out, 4);
		spaces(out, 4);
		out.write("//Table attribute definitions\n");
		attribIter0 = entity.getPrimaryKeyAttributes().iterator();
		while(attribIter0.hasNext())
		{

			Attribute attrib = attribIter0.next();
			generateGetter(out, attrib, false);
			lines(out, 1);
			generateSetter(out, attrib);
			lines(out, 1);


		}

		out.write("}");
		out.close();
	}

	private void generateSetter(BufferedWriter out, Attribute attrib)
	throws IOException {
		spaces(out, 4);
		out.write("public void set" + attrib.getUpperLabel() + "("	+ attrib.getType() + " " + attrib.getUnqualifiedLowerLabel()	+ ")\n");
		spaces(out, 4);
		out.write("{\n");
		spaces(out, 8);
		out.write("this." + attrib.getUnqualifiedLowerLabel() + " = "	+ attrib.getUnqualifiedLowerLabel() + ";\n");
		spaces(out, 4);
		out.write("}\n");

	}

	private void generateGetter(BufferedWriter out, Attribute attrib, boolean primaryAnnotations)
	throws IOException {
		
		spaces(out, 4);
		out.write("@Column(name = \""+attrib.getSqlLabel()+"\""+ (attrib.isPrimary() ? ", nullable = false":"")+ ")\n" );
		spaces(out, 4);
		out.write("public "+attrib.getType()+" get" + attrib.getUpperLabel() + "()\n");
		spaces(out, 4);
		out.write("{\n");
		spaces(out, 8);
		out.write("return " + attrib.getUnqualifiedLowerLabel() + ";\n");
		spaces(out, 4);
		out.write("}\n");

	}

	private HashMap<String,Attribute> getHashFromAttributesFtPt(Iterator<Attribute> attribIter)
	{
		HashMap<String,Attribute> hash = new HashMap<String,Attribute>();
		while(attribIter.hasNext())
		{
			Attribute attribute = attribIter.next();
			if(attribute.isForeign() && attribute.isPrimary() )
				hash.put(attribute.getUnqualifiedLabel(), attribute);
		}
		return hash;
	}

	private HashMap<String,Attribute> getHashFromAttributesFfPt(Iterator<Attribute> attribIter)
	{
		HashMap<String,Attribute> hash = new HashMap<String,Attribute>();
		while(attribIter.hasNext())
		{
			Attribute attribute = attribIter.next();
			if(!attribute.isForeign() && attribute.isPrimary() )
				hash.put(attribute.getUnqualifiedLabel(), attribute);
		}
		return hash;
	}

	private HashMap<String,Attribute> getHashFromAttributesFtPf(Iterator<Attribute> attribIter)
	{
		HashMap<String,Attribute> hash = new HashMap<String,Attribute>();
		while(attribIter.hasNext())
		{
			Attribute attribute = attribIter.next();
			if(attribute.isForeign() && !attribute.isPrimary() )
				hash.put(attribute.getUnqualifiedLabel(), attribute);
		}
		return hash;
	}

	private HashMap<String,Attribute> getHashFromAttributesFfPf(Iterator<Attribute> attribIter)
	{
		HashMap<String,Attribute> hash = new HashMap<String,Attribute>();
		while(attribIter.hasNext())
		{
			Attribute attribute = attribIter.next();
			if(!attribute.isForeign() && !attribute.isPrimary() )
				hash.put(attribute.getUnqualifiedLabel(), attribute);
		}
		return hash;
	}

	private List<Attribute> getHashOfAttributesToEntity(Entity parent, Entity child)
	{
		System.out.println("*******parent:"+ parent.getUnqualifiedLowerLabel() + " ****child:" + child.getUnqualifiedLowerLabel());
		
		List<Attribute> hash = new ArrayList<Attribute>();
		Iterator<Attribute> attribIter = child.getAttributes().iterator();
		while(attribIter.hasNext())
		{
			Attribute a = attribIter.next();
			System.out.println("*******atter:"+ a.getUnqualifiedLowerLabel());
			if(a.isForeign())
			{
				
				
				
				Entity e2 = a.getReferencedEntity();
				
				System.out.println("*******foreign:"+ a.getUnqualifiedLowerLabel() + " ****e2label" + (e2!=null?e2.getUnqualifiedLabel():"none"));


				if(e2 != null && parent.getUnqualifiedLabel().equalsIgnoreCase(e2.getUnqualifiedLabel()))
				{
					System.out.println("*******foreign:DINGDINGDING");
					hash.add(a);
					
				}

			}


		}
		return hash;
	}


	private ClassVariable getManyToManyVariable(Entity parent, Entity child)
	{
		ClassVariable v = new ClassVariable();
		Iterator<Attribute> attribIter = child.getAttributes().iterator();
		String thisKey="";
		String thatKey="";
		String targetEntity="";
		while(attribIter.hasNext())
		{
			Attribute a = attribIter.next();

			//Entity e2 = child.getForeignReferenceEntity(a);
			Entity e2 = a.getReferencedEntity();

			if(e2 != null && parent.getUnqualifiedLabel().equals(e2.getUnqualifiedLabel()))
				thisKey = a.getSqlLabel();
			else
			{
				if(e2 !=null) 
				{
					targetEntity = e2.getUpperLabel();
					System.out.println("HEREEE0");
					thatKey = a.getSqlLabel();
					v.setIdentifier(plural(e2.getUnqualifiedLowerLabel()));
					v.setType("Set<"+targetEntity+">");
					v.setInitializer(" = new HashSet<"+targetEntity+">(0)");
//					v.setType("Set<"+e2.getUnqualifiedLabel()+">");
//					v.setInitializer(" = new HashSet<"+e2.getUnqualifiedLabel()+">(0)");
				}
			}
		}
		v.setAttribType(AttributeType.FOREIGNATTRIBUTE);
		
		v.setRelationshipType(RelationshipType.MANYTOMANY);
		
		v.setModifier("private");
		v.getGetterAnnotations().add("@ManyToMany(cascade = CascadeType.ALL,targetEntity="+targetEntity+".class)");
		v.getGetterAnnotations().add("@JoinTable(name = \""+currentSchema.getSqlLabel()+"."+child.getSqlLabel()+"\", joinColumns = { @JoinColumn(name = \""+thisKey+"\")}, inverseJoinColumns = { @ JoinColumn(name = \""+thatKey+"\")})");

		return v;

	}

	private boolean isManyToMany(Entity child)
	{		
		System.out.println("**************Checking if child table is ManyToMany " + child.getUnqualifiedLabel());
		System.out.println( "* total attributes:" + child.getAttributes().size());
		System.out.println( "* primary keys:" + child.getPrimaryKeyAttributes().size());
		System.out.println( "* parent keys:" + child.getParentKeyAttributes().size());
		System.out.println( "* childern:" + child.getChildren().size());
	
		if(child.getAttributes().size()==2 && child.getPrimaryKeyAttributes().size() == 2 && child.getParentKeyAttributes().size()==2 && child.getChildren().size() ==0)
			return true;
		return false;
	}

	private void lines(BufferedWriter out, int num) throws IOException {
		for (int i = 0; i < num; i++)
			out.write("\n");
	}

	private void spaces(BufferedWriter out, int num) throws IOException {
		for (int i = 0; i < num; i++)
			out.write(" ");
	}
	
	private void connectLinks()
	{	
		System.out.println("***Connecting Links***");
		Iterator<DomainClass> domainIter = domainClassList.iterator();
		while(domainIter.hasNext() )
		{
			
			
			DomainClass dc = domainIter.next();
			System.out.println("   "+dc.getIdentifier());
			Iterator<ClassVariable> cvIter =  dc.getSymTable().iterator();
			while (cvIter.hasNext())
			{
				
				ClassVariable cv = cvIter.next();
				System.out.println("      "+cv.getIdentifier());
				if(cv.getAttribType() == AttributeType.FOREIGNATTRIBUTE)
				{
					System.out.println("         -attribute is foreign");
					Entity e = cv.getAttribute().getReferencedEntity();//dc.getEntity().getForeignReferenceEntity(cv.getAttribute());
					if(e != null)
					{
					DomainClass c = findDomainByIdentifier(e.getUnqualifiedLabel());
					if(c == null)
						System.out.println("************** cannot find DomainClass:"+cv.getType());
					cv.setDomainClass(c);
					}
					else
						System.out.println("************** cannot getEntity from attribute:"+cv.getIdentifier());
					

				}
				else if(cv.getAttribType() == AttributeType.CHILD)
				{
					System.out.println("         -attribute is child");
					ClassVariable cvRef = findReferencedClassVariable(cv);
					if(cvRef == null)
						{
						System.out.println("************** ERROR CONNECTING CLASSVARIABLE");
						cv.setReferenedClassVariable(cvRef);
						}
						
				}
	
			}
			
			
			
		}
		
	}
	
	/************************************/
	private void writeToFile(DomainClass domainClass) throws IOException {
		
		String packageDirectory = 	projectPath + "/"	+ domainClass.getPackageName().replaceAll("\\.", "/");


		File file = new File(packageDirectory, domainClass.getIdentifier()	+ ".java");
		if(!file.exists())
		{
		FileWriter fstream = new FileWriter(file);
		BufferedWriter out = new BufferedWriter(fstream);
		out.write(domainClass.toString());
		out.close();
		}
		else
			System.out.println("" + file.getCanonicalPath() + " Exists. Not Overwriting");


	}
	/**
	 * @throws IOException **********************************/
	
	private void writeFiles() throws IOException
	{
		Iterator<DomainClass> domainIter = domainClassList.iterator();
		while(domainIter.hasNext() )
		{
			DomainClass dc = domainIter.next();
			writeToFile(dc);
				
		}
		
	}
	
	private ClassVariable findReferencedClassVariable(ClassVariable cv)
	{

			DomainClass dc = findDomainByIdentifier(cv.getAttribute().getReferencedEntity().getUnqualifiedLabel());
		
			
			Iterator<ClassVariable> cvIter = dc.getSymTable().iterator();
			
			while(cvIter.hasNext())
			{
				ClassVariable cv1 = cvIter.next();

				if(cv1.getAttribute()!= null && cv1.getAttribType() !=  AttributeType.CHILD && cv1.getAttribute().getUnqualifiedLabel().equals(cv.getAttribute().getUnqualifiedLabel()))
						return cv1;
//				else
//					System.out.println("Error on "+ cv.getIdentifier() +" with "+cv.getIdentifier());
			}

		return null;
		
	}
	
	private DomainClass findDomainByIdentifier(String ident)
	{
		Iterator<DomainClass> domainIter = domainClassList.iterator();
		
		while(domainIter.hasNext() )
		{
			DomainClass dc = domainIter.next();
			if(dc.getIdentifier().equalsIgnoreCase(ident))
				return dc;
	
		}
		return null;
		
	}






}
