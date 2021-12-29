package edu.uiowa.icts.protogen.loaders;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import edu.uiowa.icts.protogen.model.Attribute;
import edu.uiowa.icts.protogen.model.Database;
import edu.uiowa.icts.protogen.model.Domain;
import edu.uiowa.icts.protogen.model.Schema;
import edu.uiowa.icts.protogen.model.Entity;
import edu.uiowa.icts.protogen.model.Relationship;

public class ClayXpathLoader implements DatabaseModelLoader {
	static Logger logger = LogManager.getLogger(ClaySaxLoader.class);
	
	public static void main(String[] args) throws IOException, DocumentException {
		ClayXpathLoader loader = new ClayXpathLoader();
		Database database = loader.load("/Users/eichmann/Documents/Components/workspace/Protogen/src/test/resources/ryanlorentzen.clay");
		database.dump();
	}
	
	Element databaseNode = null;
	Database database = null;
	
	public Database load(String fileName) throws IOException, DocumentException {
		File input = new File(fileName);
		InputStream is = new FileInputStream(input);
		SAXReader reader = new SAXReader(false);
		Document document = reader.read(is);
		Element root = document.getRootElement();
		logger.info("document root: " + root.getName() + "\tversion: " + root.attributeValue("clay-version"));
		is.close();
		
		databaseNode = (Element)root.selectSingleNode("database-model");
		database = new Database(databaseNode.attributeValue("name"), databaseNode.attributeValue("remarks"));

		processSchemas(database, databaseNode);
		// now do the foreign keys to accommodate forward references in the XML
		processForeignKeys();
		
		for (Schema schema : database.getSchemas()) {
			for (Entity entity : schema.getEntities()) {
				entity.generateParentKeys();
				entity.generateSubKeys();
				entity.matchRemarks();
			}
		}
		
		database.relabel();

		return database;
	}
	
	@SuppressWarnings("unchecked")
	void processSchemas(Database database, Element dbnode) {
		logger.info("database: " + database.getSqlLabel());
		for (Element schemaNode : (List<Element>)dbnode.selectNodes("schema-list/schema")) {
			Schema schema = new Schema(schemaNode.attributeValue("name"), schemaNode.attributeValue("remarks"));
			logger.info("schema: " + schema.getSqlLabel());
			database.addSchema(schema);
			processDomains(schema, schemaNode);
			processEntities(schema, schemaNode);
		}
	}
	
	@SuppressWarnings("unchecked")
	void processDomains(Schema schema, Element schemaNode) {
		for (Element domainNode : (List<Element>)schemaNode.selectNodes("domain-list/domain")) {
			Domain domain = new Domain(domainNode.attributeValue("name"), domainNode.attributeValue("remarks"));
			schema.addDomain(domain);
			domain.setType(((Element)domainNode.selectSingleNode("data-type")).attributeValue("name"));
			logger.info("\tdomain: " + domain);
		}		
	}

	@SuppressWarnings("unchecked")
	void processEntities(Schema schema, Element schemaNode) {
		for (Element tableNode : (List<Element>)schemaNode.selectNodes("table-list/table")) {
			Entity entity = new Entity(tableNode.attributeValue("name"), tableNode.attributeValue("remarks"));
			logger.info("\tentity: " + entity.getSqlLabel());
			schema.addEntity(entity);
			entity.setSchema(schema);
			processAttributes(entity, tableNode);
			processPrimaryKeys(entity, tableNode);
		}
	}

	@SuppressWarnings("unchecked")
	void processAttributes(Entity entity, Element entityNode) {
		for (Element attributeNode : (List<Element>)entityNode.selectNodes("column-list/column")) {
			Attribute attribute = new Attribute(attributeNode.attributeValue("name"), attributeNode.attributeValue("remarks"));
			attribute.setMandatory(getBooleanAttributeValue(attributeNode, "mandatory"));
			attribute.setAutoIncrement(getBooleanAttributeValue(attributeNode, "auto-increment"));
			attribute.setDomain(entity.getSchema().getDomainByLabel(attributeNode.attributeValue("domain")));
			attribute.setType(((Element)attributeNode.selectSingleNode("data-type")).attributeValue("name"));
			logger.info("\t\t" + attribute);
			entity.addAttribute(attribute);
		}
	}
	
	@SuppressWarnings("unchecked")
	void processPrimaryKeys(Entity entity, Element entityNode) {
		for (Element keyNode : (List<Element>)entityNode.selectNodes("primary-key/primary-key-column")) {
			Attribute attribute = entity.getAttributeByLabel(keyNode.attributeValue("name"));
			attribute.setPrimary(true);
			entity.addPrimaryKey(attribute);
			logger.info("\t\tprimary key: " + attribute);
		}
	}
	
	@SuppressWarnings("unchecked")
	void processForeignKeys() {
		for (Element schemaNode : (List<Element>)databaseNode.selectNodes("schema-list/schema")) {
			Schema targetSchema = database.getSchemaByName(schemaNode.attributeValue("name"));
			for (Element entityNode : (List<Element>)schemaNode.selectNodes("table-list/table")) {
				Entity entity = targetSchema.getEntityByLabel(entityNode.attributeValue("name"));
				for (Element keyNode : (List<Element>)entityNode.selectNodes("foreign-key-list/foreign-key")) {
					Relationship relationship = new Relationship();
					Schema sourceSchema = database.getSchemaByName(keyNode.attributeValue("referenced-table-schema"));
					Entity sourceEntity = sourceSchema.getEntityByLabel(keyNode.attributeValue("referenced-table"));
					relationship.setSourceEntity(sourceEntity);
					relationship.setTargetEntity(entity);
					
					sourceEntity.setChild(relationship);
					entity.setParent(relationship);
					entity.getSchema().addRelationship(relationship);
					
					for (Element referencedNode : (List<Element>)keyNode.selectNodes("foreign-key-column")) {
						String sourceAttributeName = referencedNode.attributeValue("referenced-key-column-name");
						String targetAttributeName = referencedNode.attributeValue("column-name");
						relationship.setForeignReferencedAttributeMapping(targetAttributeName, sourceAttributeName);
			            entity.getAttributeByLabel(targetAttributeName).setForeign(true); 
			            entity.getAttributeByLabel(targetAttributeName).setReferencedEntityName(sourceEntity.getLabel());
						sourceEntity.getAttributeByLabel(sourceAttributeName).setForeignAttribute(entity.getAttributeByLabel(targetAttributeName));
						logger.info("\tforeign key: " + sourceSchema.getLabel() + "." + sourceEntity.getLabel() + "." + sourceAttributeName
										+ " -> " + targetSchema.getLabel() + "." + entity.getLabel() + "." + targetAttributeName);
					}
				}
			}
		}
	}
	
	boolean getBooleanAttributeValue(Element element, String name) {
		return Boolean.parseBoolean(element.attributeValue(name));
	}

	int getIntAttributeValue(Element element, String name) {
		return Integer.parseInt(element.attributeValue(name));
	}

	@Override
	public void run(String descriptor) throws Exception {
		load(descriptor);
	}

	@Override
	public void run(Properties props) throws Exception {
		logger.error("ClayXpathLoader launched with props");
	}

	@Override
	public Database getDatabase() {
		return database;
	}

}
