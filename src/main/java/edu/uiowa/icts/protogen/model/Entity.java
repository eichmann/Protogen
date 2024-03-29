package edu.uiowa.icts.protogen.model;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.uiowa.icts.protogen.springhibernate.DomainClass;

public class Entity extends Element {
	
    Schema schema = null;
	public Vector<Attribute> attributes = new Vector<Attribute>();
	public Vector<Attribute> primaryKeyAttributes = new Vector<Attribute>();
    Vector<Attribute> parentKeyAttributes = new Vector<Attribute>();
    Vector<Attribute> subKeyAttributes = new Vector<Attribute>();
	
	Vector<Relationship> parents = new Vector<Relationship>();
	Vector<Relationship> children = new Vector<Relationship>();
	
	private DomainClass domainClass;

	static Logger log = LogManager.getLogger(Entity.class);
	
	public Entity() {
		
	}
	
	public Entity(String sqlLabel, String remarks) {
		this.sqlLabel = sqlLabel;
		this.label = sqlLabel;
		this.remarks = remarks;
	}
	
	public Schema getSchema() {
        return schema;
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }
    
    public void addAttribute(Attribute attribute) {
    	attributes.add(attribute);
    }

    public void addPrimaryKey(Attribute attribute) {
    	primaryKeyAttributes.add(attribute);
    }

    public Vector<Attribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(Vector<Attribute> attributes) {
		this.attributes = attributes;
	}

	public Vector<Attribute> getPrimaryKeyAttributes() {
		return primaryKeyAttributes;
	}

	public void setPrimaryKeyAttributes(Vector<Attribute> primaryKeyAttributes) {
		this.primaryKeyAttributes = primaryKeyAttributes;
	}

    public Vector<Attribute> getParentKeyAttributes() {
        return parentKeyAttributes;
    }

    public Vector<Attribute> getSubKeyAttributes() {
        return subKeyAttributes;
    }

	public Vector<Relationship> getParents() {
		return parents;
	}

	public void setParent(Relationship parent) {
		this.parents.add(parent);
	}
	
	public Vector<Entity> getAncestors() {
	    Vector<Entity> ancestors = new Vector<Entity>();

	    for (Relationship relationship : parents) {
	    	ancestors.add(relationship.getSourceEntity());
	    }
	    
	    return ancestors;
	}
	
	public Vector<Relationship> getChildren() {
		return children;
	}

	public void setChild(Relationship child) {
		this.children.add(child);
	}
	
	public Attribute getAttributeByLabel(String label) {
	    for (int i = 0; i < attributes.size(); i++)
	        if (attributes.elementAt(i).getLabel().equals(label))
	            return attributes.elementAt(i);
	    
	    return null;
	}

    public Attribute getAttributeBySQLLabel(String label) {
        for (int i = 0; i < attributes.size(); i++) {
            if (attributes.elementAt(i).getSqlLabel().equals(label))
                return attributes.elementAt(i);
        }
        
        return null;
    }

    public String getForeignReferencedAttribute(String referencedAttribute) {
        for (int i = 0; i < getParents().size(); i++) {
            String mappedAttribute = getParents().elementAt(i).getForeignReferencedAttribute(referencedAttribute);
            if (mappedAttribute != null)
                return mappedAttribute;
        }
        return null;
    }

    public Attribute getIntKeyAttribute() {
        for (int i = 0; i < attributes.size(); i++)
            if (attributes.elementAt(i).isPrimary() && attributes.elementAt(i).isInt())
                return attributes.elementAt(i);
        
        return null;
    }

    public void relabel() {
        relabel(true);
        for (int i = 0; i < attributes.size(); i++){
        	attributes.elementAt(i).relabel();
        }
        
        // breaks hibernate primary key columns in domain objects 
//        for (int i = 0; i < primaryKeyAttributes.size(); i++){
//        	primaryKeyAttributes.elementAt(i).relabel();
//        }
    }
    
    public boolean hasAncestor() {
    	return getParents().size() > 0;
    }
    
    public int ancestorCount() {
    	return getParents().size();
    }
    
    public boolean hasDateTime() {
    	for (Attribute attribute : attributes)
    		if (attribute.isDateTime())
                 return true;       
        return false;
    }

    public boolean hasPrimaryDateTime() {
    	for (Attribute attribute : attributes)
    		if (attribute.isDateTime() && attribute.isPrimary())
                 return true;       
        return false;
    }

    public boolean hasForeignDateTime() {
    	for (Attribute attribute : attributes)
    		if (attribute.isDateTime() && attribute.isForeign())
                 return true;       
        return false;
    }

    public boolean hasTimestamp() {
    	for (Attribute attribute : attributes)
    		if (attribute.isTimestamp())
                 return true;       
        return false;
    }

    public boolean hasPrimaryTimestamp() {
    	for (Attribute attribute : attributes)
    		if (attribute.isTimestamp() && attribute.isPrimary())
                 return true;       
        return false;
    }

    public boolean hasForeignTimestamp() {
    	for (Attribute attribute : attributes)
    		if (attribute.isTimestamp() && attribute.isForeign())
                 return true;       
        return false;
    }

    public boolean hasImage() {
        for (int i = 0; i < attributes.size(); i++)
            if (attributes.elementAt(i).getDomain() != null && attributes.elementAt(i).getDomain().getLabel().equals("Image"))
                return true;
        
        return false;
    }

    public boolean hasDomainAttribute() {
        for (int i = 0; i < attributes.size(); i++)
            if (attributes.elementAt(i).getDomain() != null && !attributes.elementAt(i).getDomain().getLabel().equals("Image"))
                return true;
        
        return false;
    }
    
    public boolean hasBinaryDomainAttribute() {
        for (int i = 0; i < attributes.size(); i++)
            if (attributes.elementAt(i).getDomain() != null && attributes.elementAt(i).getDomain().getJavaType().equals("byte[]"))
                return true;
        
        return false;
    }

    public boolean hasInt() {
        for (int i = 0; i < attributes.size(); i++)
            if (attributes.elementAt(i).isInt())
                return true;
        
        return false;
    }

    public boolean hasCounter() {
        for (int i = 0; i < attributes.size(); i++)
            if (attributes.elementAt(i).isCounter())
                return true;
        
        return false;
    }

    public boolean hasForeign() {
        for (int i = 0; i < attributes.size(); i++)
            if (attributes.elementAt(i).isForeign())
                return true;
        
        return false;
    }

    public void generateParentKeys() {
        log.debug("\n" + this + " primary keys: " + getPrimaryKeyAttributes());
        for (int i = 0; i < getParents().size(); i++) {
            Entity theSourceEntity = getParents().elementAt(i).getSourceEntity();
            Vector<Attribute> parentKeys = theSourceEntity.getPrimaryKeyAttributes();
            log.debug("\t" + theSourceEntity + " primary keys: " + parentKeys);
            parentLoop : for (int j = 0; j < parentKeys.size(); j++) {
                Attribute parentKey = parentKeys.elementAt(j);
                if (parentKeyAttributes.size() == 0)
                    parentKeyAttributes.addElement(parentKey);
                for (int k = 0; k < parentKeyAttributes.size(); k++) {
                    log.debug("\t\tcomparing " + parentKey + " to " + parentKeyAttributes.elementAt(k));
                    if (parentKey.getLabel().equals(parentKeyAttributes.elementAt(k).getLabel()))
                        continue parentLoop;
                }
                log.debug("\t\tadding " + parentKey);
                parentKeyAttributes.addElement(parentKey);
            }
        }
        log.debug(this + " parent keys: " + parentKeyAttributes);
    }

    public void generateSubKeys() {
        parentLoop: for (int j = 0; j < getPrimaryKeyAttributes().size(); j++) {
            Attribute primaryKey = getPrimaryKeyAttributes().elementAt(j);
            for (int k = 0; k < parentKeyAttributes.size(); k++) {
            	
            	log.debug( this.label + " - " + primaryKey.getLabel() + " - " + parentKeyAttributes.elementAt(k).getLabel());
            	
                if (primaryKey.getLabel().equalsIgnoreCase(parentKeyAttributes.elementAt(k).getLabel()))
                    continue parentLoop;
            }
            
            log.debug(" adding "+primaryKey.getLabel()+" to "+this.label);
            subKeyAttributes.addElement(primaryKey);
        }

        log.debug(this + " sub keys: " + subKeyAttributes);
    }
    
    public boolean isPrimaryReference(Attribute theAttribute) {
        for (int i = 0; i < primaryKeyAttributes.size(); i++) {
            Attribute primaryAttribute = primaryKeyAttributes.elementAt(i);
            if (primaryAttribute.sqlLabel.equals(theAttribute.sqlLabel))
                return true;
        }
        return false;
    }
    
    public boolean isForeignReference(Attribute theAttribute) {
        for (int i = 0; i < parentKeyAttributes.size(); i++) {
            Attribute parentAttribute = parentKeyAttributes.elementAt(i);
            if (parentAttribute.sqlLabel.equals(theAttribute.sqlLabel))
                return true;
        }
        return false;
    }
    
    public Attribute getByForeignReference(Attribute theAttribute) {
        Attribute targetAttribute = null;
        
        for (int i = 0; i < attributes.size(); i++) {
            targetAttribute = attributes.elementAt(i);
            if (targetAttribute.sqlLabel.equals(theAttribute.sqlLabel))
                return targetAttribute;
        }
        
        return null;
    }
    
    public Entity getForeignReferenceEntity(Attribute theAttribute) {
        Entity theForeignEntity = null;
        
        for (int i = 0; i < parents.size(); i++) {
            Attribute sourceAttribute = parents.elementAt(i).getSourceEntity().getAttributeBySQLLabel(theAttribute.getSqlLabel());
            if (sourceAttribute != null)
                return parents.elementAt(i).getSourceEntity();
        }
        
        return theForeignEntity;
    }

    public void matchRemarks() {
        for (int i = 0; i < attributes.size(); i++)
            attributes.elementAt(i).matchRemarks();
    }

    public void dump(BufferedWriter out) throws IOException {
        out.write("\t\tentity: " + label + "\tuid: " + uid + "\n");
        for (int i = 0; i < children.size(); i++){
        	out.write("\t\t\tchild " + children.elementAt(i).targetEntity.getLabel() + "\n");
        }
        for (int i = 0; i < attributes.size(); i++){
        	attributes.elementAt(i).dump(out);
        }
    }

	public DomainClass getDomainClass() {
		return domainClass;
	}

	public void setDomainClass(DomainClass domainClass) {
		this.domainClass = domainClass;
	}


}
