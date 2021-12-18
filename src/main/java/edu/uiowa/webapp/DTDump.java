package edu.uiowa.webapp;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.Node;

public class DTDump {
	static Logger logger = LogManager.getLogger(DTDump.class);
	static Hashtable<String,Attribute> attributeHash = new Hashtable<String,Attribute>();
	static Hashtable<String,Entity> entityHash = new Hashtable<String,Entity>();
	static Hashtable<String,Relationship> relationHash = new Hashtable<String,Relationship>();

    public static void main(String[] argv) throws Exception {
		SAXReader reader = new SAXReader(false);
		Document document = reader.read(System.in);
//		System.out.println(document.asXML());
		print(document.getRootElement(), "");
		attributeWalk(document.getRootElement());
		entityWalk(document.getRootElement());
		relationWalk(document.getRootElement());
		attributeAssignment(document.getRootElement());
		
		logger.debug("attributeHash: " + attributeHash);
		logger.debug("entityHash: " + entityHash);
		logger.debug("relationHash: " + relationHash);
		
		if (logger.isDebugEnabled()) {
			Enumeration<Entity> entityEnum = entityHash.elements();
			while (entityEnum.hasMoreElements()) {
				entityEnum.nextElement().dump();
			}
			Enumeration<Relationship> relationEnum = relationHash.elements();
			while (relationEnum.hasMoreElements()) {
				relationEnum.nextElement().dump();
			}
		}
	}

	public static void print(Node node, String indent) {
		logger.debug(indent + node.getName()
				+ " : name=" + ((Element)node).attributeValue("name")
				+ " : ref=" + ((Element)node).attributeValue("ref")
				+ " : value=" + ((Element)node).attributeValue("value")
				+ " : mixed=" + ((Element)node).attributeValue("mixed")
				+ " : min=" + ((Element)node).attributeValue("minOccurs")
				+ " : max=" + ((Element)node).attributeValue("maxOccurs")
				+ " : base=" + ((Element)node).attributeValue("base")
				);
		List nodes = node.selectNodes("*");
		Iterator nodeIterator = nodes.iterator();
		while (nodeIterator.hasNext()) {
			Node child = (Node) nodeIterator.next();
			print(child, indent + "   ");
		}
	}
	
	public static void attributeWalk(Node node) {
		logger.info("Scanning for free-standing attributes");
		List nodes = node.selectNodes("*");
		Iterator nodeIterator = nodes.iterator();
		while (nodeIterator.hasNext()) {
			Element child = (Element) nodeIterator.next();
			String childName = child.attributeValue("name");
			
			List subNodes = child.selectNodes("*");
			if (subNodes.size() != 1 || ((Element)subNodes.get(0)).attributeValue("mixed") == null)
				continue;
			
			logger.debug("\t\tattribute : " + childName);
			Attribute theAttribute = new Attribute();
			theAttribute.setLabel(childName);
			attributeHash.put(childName, theAttribute);
		}		
	}
	
	public static void entityWalk(Node node) {
		logger.info("Scanning for entities");
		List nodes = node.selectNodes("*");
		Iterator nodeIterator = nodes.iterator();
		while (nodeIterator.hasNext()) {
			Element child = (Element) nodeIterator.next();
			String childName = child.attributeValue("name");
			
			// toss the attributes
			List subNodes = child.selectNodes("*");
			if (subNodes.size() != 1 || ((Element)subNodes.get(0)).attributeValue("mixed") != null)
				continue;
			logger.trace(((Node)subNodes.get(0)).selectSingleNode("*").getName());
			// toss the referentials
			if ("choice".equals(((Node)subNodes.get(0)).selectSingleNode("*").getName()))
				continue;
			
			logger.debug("\t\tentity : " + childName);
			Entity theEntity = new Entity();
			theEntity.setLabel(childName);
			entityHash.put(childName, theEntity);
		}		
	}

	public static void relationWalk(Node node) {
		logger.info("Scanning for relations");
		List nodes = node.selectNodes("*");
		Iterator nodeIterator = nodes.iterator();
		while (nodeIterator.hasNext()) {
			Element child = (Element) nodeIterator.next();
			String childName = child.attributeValue("name");
			
			// toss the attributes
			List subNodes = child.selectNodes("*");
			if (subNodes.size() != 1 || ((Element)subNodes.get(0)).attributeValue("mixed") != null)
				continue;
			logger.trace(((Node)subNodes.get(0)).selectSingleNode("*").getName());
			// toss the referentials
			if (!"choice".equals(((Node)subNodes.get(0)).selectSingleNode("*").getName())
					|| ((Element)((Node)subNodes.get(0)).selectSingleNode("*/*")).attribute("ref") == null
					|| attributeHash.containsKey(((Element)((Node)subNodes.get(0)).selectSingleNode("*/*")).attribute("ref").getValue())
					|| !entityHash.containsKey(((Element)((Node)subNodes.get(0)).selectSingleNode("*/*")).attribute("ref").getValue())
					)
				continue;
			
			String ref = ((Element)((Node)subNodes.get(0)).selectSingleNode("*/*")).attribute("ref").getValue();
			logger.debug("\trelation : " + childName);
			logger.debug("\t\tref: " + ref);
			Relationship theRelationship = new Relationship();
			theRelationship.setTargetEntity(entityHash.get(ref));
			relationHash.put(childName, theRelationship);
		}		
	}
	
	public static void attributeAssignment(Node root) {
		logger.info("Assigning attributes to entities");
		List nodes = root.selectNodes("*");
		Iterator nodeIterator = nodes.iterator();
		while (nodeIterator.hasNext()) {
			Element candidate = (Element) nodeIterator.next();
			String candidateName = candidate.attributeValue("name");
			Entity theEntity = entityHash.get(candidateName);

			if (theEntity == null)
				continue;

			logger.debug("\t" + candidateName);
			List subnodes = candidate.selectNodes("*/*");
			Iterator subnodeIterator = subnodes.iterator();
			while (subnodeIterator.hasNext()) {
				Element subnode = (Element) subnodeIterator.next();
				String subNodeType = subnode.getName();
				logger.debug("\t\t" + subNodeType);
				if (subNodeType.equals("attribute")) {
					String subNodeName = subnode.attributeValue("name");
					logger.debug("\t\t\t" + subNodeName);
					
					Attribute theAttribute = attributeHash.get(subNodeName);
					
					if (theAttribute == null) {
						theAttribute = new Attribute();
						theAttribute.setLabel(subNodeName);
						attributeHash.put(subNodeName, theAttribute);
					}
					
					theEntity.getAttributes().add(theAttribute);
				} else if (subNodeType.equals("sequence")) {
					walkSequence(theEntity, subnode);
				}
			}
//			logger.debug(root.asXML());
		}
	}
	
	public static void walkSequence(Entity theEntity, Element node) {
		List subNodes = node.selectNodes("*");
		Iterator subNodeIterator = subNodes.iterator();
		while (subNodeIterator.hasNext()) {
			Element subNode = (Element) subNodeIterator.next();
			String subNodeType = subNode.getName();
			
			if (subNodeType.equals("sequence")) {
				walkSequence(theEntity, subNode);
			} else {
				String ref = subNode.attributeValue("ref");
				Relationship theRelationship = relationHash.get(ref);
				
				if (theRelationship != null) {
					theRelationship.setSourceEntity(theEntity);
					continue;
				}

				logger.debug("\t\t\t" + ref);				
				Attribute theAttribute = attributeHash.get(ref);
				
				if (theAttribute == null) {
					theAttribute = new Attribute();
					theAttribute.setLabel(ref);
					attributeHash.put(ref, theAttribute);
				}
				
				theEntity.getAttributes().add(theAttribute);
			}
		}
		
	}
}