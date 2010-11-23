package edu.uiowa.webapp;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.uiowa.loaders.DBConnect;
import edu.uiowa.loaders.PropertyLoader;

public class JDBCLoader implements DatabaseSchemaLoader {

	private Database database = null;
	private Connection conn = null;
	private String currentSchema=null;

	public JDBCLoader() {

	}
	
	private static final Log log =LogFactory.getLog(JDBCLoader.class);



	public Database getDatabase() {
		return database;
	}


	public void run(String filename) throws Exception {




		Properties prop = PropertyLoader.loadProperties(filename);
		String schema= prop.getProperty("db.schema");




		connect(prop);
		DatabaseMetaData dbMeta = conn.getMetaData();

		log.debug("Database: "+dbMeta.getDatabaseProductName()+" "+dbMeta.getDatabaseProductVersion());
		log.debug("JDBC Version: "+dbMeta.getDriverName()+"."+dbMeta.getDriverVersion());


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
			log.debug("All Schemas");
			while(schemasRS.next())
			{

				Schema s = createSchema(dbMeta, schemasRS.getString(1));
				database.getSchemas().add(s);

			}
		}

		updateForeignKeys(dbMeta);
		
		for(Schema s:database.getSchemas())
		{
            for (int i = 0; i < s.getRelationships().size(); i++) {
                Relationship currentRelationship = s.getRelationships().elementAt(i);
                if (currentRelationship.sourceEntity == null) {
                	{
                     log.debug("source entity is null for " + currentRelationship.getSourceEntityName() + " -> " + s.getEntityByLabel(currentRelationship.getSourceEntityName()));
                    currentRelationship.setSourceEntity(s.getEntityByLabel(currentRelationship.getSourceEntityName()));
                	}
                }
                currentRelationship.getSourceEntity().setChild(currentRelationship);
            }
			for(Entity e:s.getEntities())
			{
				
				e.generateParentKeys();
				e.generateSubKeys();
				e.matchRemarks();
			}
		}

		conn.close();
	}

	private void updateForeignKeys(DatabaseMetaData dbMeta) throws SQLException
	{

		log.debug("Updating foreign keys");
		if(dbMeta.getDatabaseProductName().equalsIgnoreCase("oracle"))
		{

			log.debug("...using custom oracle lookup");
			Schema schema=null;
			if(database.getSchemas().size()==1)
				schema = database.getSchemas().iterator().next();
			log.debug("schemaname:"+schema.getLabel());

			String query = ""
				+ " select"
				+ "    col.table_name, col.column_name,"
				+ "    rel.table_name,rel.column_name"
				+ " from "
				+ "    all_tab_columns col"
				+ "    join all_cons_columns con "
				+ "      on col.table_name = con.table_name "
				+ "     and col.column_name = con.column_name"
				+ "    join all_constraints cc "
				+ "      on con.constraint_name = cc.constraint_name"
				+ "    join all_cons_columns rel "
				+ "      on cc.r_constraint_name = rel.constraint_name "
				+ "     and con.position = rel.position"
				+ " where "
				+ "    cc.constraint_type = 'R'";

			log.debug(".......executing query");
			Statement statement = conn.createStatement();
			statement.execute(query);
			log.debug(".......query complete");

			int count =0;
			ResultSet rs= statement.getResultSet();

			if(count==0)
				printColumns(rs);
			while(rs.next())
			{
				log.debug(""+rs);

				String pkschema= schema.getLabel();
				String pktable= rs.getString(1);
				String pkcolumn = rs.getString(2);
				String fkschema=  schema.getLabel();
				String fktable= rs.getString(3);
				String fkcolumn = rs.getString(4);
				log.debug(".........."+pktable+"."+pkcolumn+" -> "+fktable+"."+fkcolumn);

				if(pktable.equalsIgnoreCase(fktable) && pkcolumn.equalsIgnoreCase(fkcolumn))
					continue;
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
				r.setSourceEntityName(fke.getLabel());
				r.setTargetEntity(pke);
				r.setForeignReferencedAttributeMapping(pkcolumn, fkcolumn);
				r.setLabel("foreign_key");
				pka.setReferencedEntityName(fktable);
			//	r.setRelationshipCardinality(CardinalityEnum.ONE_TO_MANY);
				log.debug("Relationship:"+r);
				schema.getRelationships().add(r);

			}
			count++;
			rs.close();
			return;
		}

		log.debug("Updating foreign keys");
		int count =0;
		for(Schema s:database.getSchemas())
			for(Entity e:s.getEntities())
			{
				ResultSet rs= dbMeta.getExportedKeys(null, s.getLabel(), e.getLabel());

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
					log.debug("...."+pkcolumn+" -> "+fkcolumn);

					Schema pks = getSchema(pkschema);
					Entity pke = getEntity(pks, pktable);
					Attribute  pka = getAttribute(pke, pkcolumn);


					//	pka.set


					Schema fks = getSchema(fkschema);
					Entity fke = getEntity(fks, fktable);
					Attribute  fka = getAttribute(fke, fkcolumn);
					fka.setForeign(true);
					fka.setForeignAttribute(pka);
					fka.setParentAttribute(pka);
					


					pka.setParentAttribute(fka);
				//	pka.setReferencedEntityName(fka.getLabel());
					fka.getChildAttributes().add(pka);
					

					Relationship r = new Relationship();
					r.setSourceEntity(fke);
					r.setTargetEntity(pke);
					r.setForeignReferencedAttributeMapping(pkcolumn, fkcolumn);
					r.setLabel("foreign_key");
					
					pka.setReferencedEntityName(fktable);
					pka.setParentAttribute(fka);
					
					fke.setChild(r);
					s.getRelationships().add(r);
					






				}
				count++;

				rs.close();
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
		log.debug("------------");
		log.debug("Database");
		while(rs.next())
		{
			log.debug(""+rs.getString(1));
			database.setLabel(rs.getString(1));
		}
		rs.close();


	}

	private Schema createSchema(DatabaseMetaData dbMeta, String label) throws SQLException
	{
		Schema schema = new Schema();
		schema.setLabel(label);
		String[] types = {"TABLE"};
		ResultSet rs = dbMeta.getTables(null, label, "%",types);
		log.debug("------------");
		log.debug("Tables in "+schema);
		while(rs.next())
		{

			Entity e = createEntity(dbMeta,rs.getString(3));
			e.setSchema(schema);

			schema.getEntities().add(e);

		}

		rs.close();
		return schema;
	}

	private Entity createEntity(DatabaseMetaData dbMeta, String label) throws SQLException
	{
		log.debug("...Column:"+label);
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
			//					log.debug("");
			//					
			//			}


			String aLabel = rs.getString(4);
			String type = rs.getString(6);
			log.debug("......"+aLabel+" type="+type);
			if(getAttribute(e, aLabel)==null)
			{
				a.setLabel(aLabel);
				a.setType(type);
				a.setEntity(e);
				e.getAttributes().add(a);
			}

		}
		rs.close();
		log.debug("......Primary Keys:");
		ResultSet rs2 = dbMeta.getPrimaryKeys(null, currentSchema,label );
		printColumns(rs2);

		while(rs2.next())
		{
			String aLabel = rs2.getString(4);
			log.debug("......"+aLabel);
			for(Attribute a :e.getAttributes())
			{
				if(aLabel.equalsIgnoreCase(a.getLabel()))
				{
					a.setPrimary(true);
					log.debug("..........is primary");
					boolean exists = false;
					for(Attribute aa : e.getPrimaryKeyAttributes())
					{
						
						if(aa.getLabel().equalsIgnoreCase(aLabel))
						{
							
							exists=true;
							continue;
						}


					}
					if(!exists)
					{
						log.debug("................adding");
						e.getPrimaryKeyAttributes().add(a);
					}
					
						

				}

			}

		}
		rs2.close();

		return e;
	}

	private void connect(Properties prop) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		String connection_url= prop.getProperty("db.url");
		String username= prop.getProperty("db.username");
		String password= prop.getProperty("db.password");
		boolean ssl= Boolean.parseBoolean(prop.getProperty("db.ssl"));
		String driver= prop.getProperty("db.driver");
		log.debug(prop.toString());
		DBConnect dbConn = new DBConnect(driver,connection_url,username,password,ssl);
		dbConn.connect();
		conn = dbConn.getConn();



	}

	private void printColumns(ResultSet rs) throws SQLException
	{
		int count = rs.getMetaData().getColumnCount();
	
		if(log.isDebugEnabled()) {
			StringBuffer  temp = new StringBuffer("......Columns:");
			for (int i =0;i<count;i++)
			{
				temp.append(""+i+"-"+rs.getMetaData().getColumnName(i+1));
				if(i<count -1)
					temp.append(",");
				else
					temp.append("");
				
			}
		log.debug(temp);
		}

	}
}
