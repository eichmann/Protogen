package edu.uiowa.webapp;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.xml.sax.Attributes;
import edu.uiowa.loaders.DBConnect;
import edu.uiowa.loaders.PropertyLoader;
import edu.uiowa.loaders.generic;

public class JDBCLoader implements DatabaseSchemaLoader {
 
	private Database database = null;
	private Connection conn = null;
	private String currentSchema=null;

	public JDBCLoader() {

	}


    public Database getDatabase() {
        return database;
    }

	
	public void run(String filename) throws Exception {
		

		
		
		Properties prop = PropertyLoader.loadProperties(filename);
		String schema= prop.getProperty("db.schema");
		

		
		
		connect(prop);
		DatabaseMetaData dbMeta = conn.getMetaData();
		
		System.out.println("Database: "+dbMeta.getDatabaseProductName()+" "+dbMeta.getDatabaseProductVersion());
		System.out.println("JDBC Version: "+dbMeta.getDriverName()+"."+dbMeta.getDriverVersion());
		
		
		setupDatabase(dbMeta);
		
		/*
		 * if schema is defined, use;otherwise run on all schemas
		 */
		if(schema!=null)
		{
			Schema s = createSchema(dbMeta, schema);
			database.getSchemas().add(s);
			
		}
		else
		{
		
		ResultSet schemasRS = dbMeta.getSchemas();
		System.out.println("All Schemas");
		while(schemasRS.next())
		{
		
			Schema s = createSchema(dbMeta, schemasRS.getString(1));
			database.getSchemas().add(s);
			
		}
		}
		
		updateForeignKeys(dbMeta);
	//	database.relabel();
		updateLabels();
	
	}
	
	private void updateForeignKeys(DatabaseMetaData dbMeta) throws SQLException
	{
		System.out.println("Updating foreign keys");
		int count =0;
		for(Schema s:database.getSchemas())
			for(Entity e:s.getEntities())
			{
				ResultSet rs = dbMeta.getExportedKeys(null, s.getLabel(), e.getLabel());
				if(count==0)
					printColumns(rs);
				while(rs.next())
				{
					
					String pkschema= rs.getString(2);
					String pktable= rs.getString(3);
					String pkcolumn = rs.getString(4);
					String fkschema= rs.getString(6);
					String fktable= rs.getString(7);
					String fkcolumn = rs.getString(8);
					System.out.println(""+pkcolumn+" -> "+fkcolumn);
					
					Schema pks = getSchema(pkschema);
					Entity pke = getEntity(pks, pktable);
					Attribute  pka = getAttribute(pke, pkcolumn);
					
					
				//	pka.set
					
					
					Schema fks = getSchema(fkschema);
					Entity fke = getEntity(fks, fktable);
					Attribute  fka = getAttribute(fke, fkcolumn);
					
					pka.setParentAttribute(fka);
					fka.getChildAttributes().add(pka);
					
					Relationship r = new Relationship();
					r.setSourceEntity(fke);
					r.setTargetEntity(pke);
					r.setForeignReferencedAttributeMapping(pkcolumn, fkcolumn);
					r.setLabel("foreign_key");
					s.getRelationships().add(r);
					
					
					
					
					
				
				}
			count++;
				
			}
	
		
	}
	
	
	private void updateLabels() throws SQLException
	{
		System.out.println("updating labels");

		for(Schema s:database.getSchemas())
		{
			System.out.print("Schema:"+s.getLabel());

			for(Entity e:s.getEntities())
			{
				
				System.out.print("...Entity:"+e.getLabel());
				
				for(Attribute a:e.getAttributes())
				{
					String lab = a.getLabel();
					System.out.print(".....Attribute:"+lab);

					
					//a.setType(a.getJavaTypeClass());
	
					
				}
				

				e.generateParentKeys();
	            e.generateSubKeys();
	            e.matchRemarks();

				
			}
			
			s.relabel();
		
		}
		for(Schema s:database.getSchemas())
		{
			System.out.print("Schema:"+s.getLabel());
			for(Entity e:s.getEntities())
			{
				
				System.out.print("...Entity:"+e.getLabel());
				
				for(Attribute a:e.getAttributes())
				{
					String lab = a.getLabel();
					System.out.print(".....Attribute:"+lab);

					
					//a.setType(a.getJavaTypeClass());
	
					
				}
			



				
			}
	}
		
	
		
	}
	
	

	
	private Schema getSchema(String schemaName)
	{
		for(Schema s:database.getSchemas())
		{
			if (s.getLabel().equalsIgnoreCase(schemaName))
				return s;
			
		}
		return null;
		
		
	}
	
	private Entity getEntity(Schema s, String name)
	{
		for(Entity e: s.getEntities())
		{
			if (e.getLabel().equalsIgnoreCase(name))
				return e;
			
		}
		return null;
		
		
	}
	
	private Attribute getAttribute(Entity e,String columnName)
	{
		for(Attribute a:e.getAttributes())
		{
			if ( a.getLabel().equalsIgnoreCase(columnName))
				return a;
			
		}
		return null;
		
		
	}
	
	private void setupDatabase(DatabaseMetaData dbMeta) throws SQLException
	{
	
		ResultSet rs = dbMeta.getCatalogs();
		database = new Database();
		System.out.println("------------");
		System.out.println("Database");
		while(rs.next())
		{
			database.setLabel(rs.getString(1));
		}
		

	}
	
	private Schema createSchema(DatabaseMetaData dbMeta, String label) throws SQLException
	{
		Schema schema = new Schema();
		schema.setLabel(label);
		String[] types = {"TABLE"};
		ResultSet rs = dbMeta.getTables(null, label, "%",types);
		System.out.println("------------");
		System.out.println("Tables in "+schema);
		while(rs.next())
		{
			
			Entity e = createEntity(dbMeta,rs.getString(3));
			e.setSchema(schema);

			schema.getEntities().add(e);
			
		}
		
		return schema;
	}
	
	private Entity createEntity(DatabaseMetaData dbMeta, String label) throws SQLException
	{
		System.out.println("...Column:"+label);
		Entity e = new Entity();
		e.setLabel(label);
		
		ResultSet rs= dbMeta.getColumns(null,currentSchema, label, "%");
		printColumns(rs);

		while(rs.next())
		{
			Attribute a = new Attribute();
			
//			for (int i =0;i<count;i++)
//			{
//				System.out.print(""+i+"-"+rs.getString(i+1));
//				if(i<count -1)
//					System.out.print(",");
//				else
//					System.out.println("");
//					
//			}
			
			
			String aLabel = rs.getString(4);
			String type = rs.getString(6);
			System.out.println("......"+aLabel+" type="+type);
			if(getAttribute(e, aLabel)==null)
			{
				a.setLabel(aLabel);
				a.setType(type);
				a.setEntity(e);
				e.getAttributes().add(a);
			}
			
		}
		System.out.println("......Primary Keys:");
		ResultSet rs2 = dbMeta.getPrimaryKeys(null, currentSchema,label );
		printColumns(rs2);

		while(rs2.next())
		{
			String aLabel = rs2.getString(4);
			System.out.println("......"+aLabel);
			for(Attribute a :e.getAttributes())
			{
				if(aLabel.equalsIgnoreCase(a.getLabel()))
				{
						a.setPrimary(true);
						boolean exists = false;
						for(Attribute aa : e.getPrimaryKeyAttributes())
						{
							if(aa.label.equalsIgnoreCase(aa.getLabel()))
							{
								exists = true;
								continue;
							}
								
							
						}
						if(!exists)
							e.getPrimaryKeyAttributes().add(a);
						
				}
				
			}
			
		}
		
		return e;
	}
	
	private void connect(Properties prop) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		String connection_url= prop.getProperty("db.url");
		String username= prop.getProperty("db.username");
		String password= prop.getProperty("db.password");
		boolean ssl= Boolean.parseBoolean(prop.getProperty("db.ssl"));
		String driver= prop.getProperty("db.driver");
		System.out.println(prop.toString());
		DBConnect dbConn = new DBConnect(driver,connection_url,username,password,ssl);
		dbConn.connect();
		conn = dbConn.getConn();
		
		
		
	}
	
	private void printColumns(ResultSet rs) throws SQLException
	{
		int count = rs.getMetaData().getColumnCount();
		System.out.print("......Columns:");
		for (int i =0;i<count;i++)
		{
			System.out.print(""+i+"-"+rs.getMetaData().getColumnName(i+1));
			if(i<count -1)
				System.out.print(",");
			else
				System.out.println("");
				
		}
		
	}
}

