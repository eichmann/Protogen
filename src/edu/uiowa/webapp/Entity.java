package edu.uiowa.webapp;

import java.util.Vector;

public class Entity extends ClayElement {
	
    Schema schema = null;
	Vector<Attribute> attributes = new Vector<Attribute>();
	Vector<Attribute> primaryKeyAttributes = new Vector<Attribute>();
    Vector<Attribute> parentKeyAttributes = new Vector<Attribute>();
    Vector<Attribute> subKeyAttributes = new Vector<Attribute>();
	
	Vector<Relationship> parents = new Vector<Relationship>();
	Vector<Relationship> children = new Vector<Relationship>();
	
	private DomainClass domainClass;

	public Schema getSchema() {
        return schema;
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
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
	    Vector<Relationship> currentParents = parents;
	    System.out.println("starting ancestor chain for " + this);
	    while (currentParents.size() > 0) {
	        //TODO for now, just pick the first parent in the vector - we'll worry about multipaths later
	        System.out.println("\tancestor: "  + currentParents.firstElement().getSourceEntity());
	        ancestors.insertElementAt(currentParents.firstElement().getSourceEntity(), 0);
	        currentParents = ancestors.firstElement().getParents();
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
	    Attribute target = null;
	    
	    for (int i = 0; i < attributes.size(); i++)
	        if (attributes.elementAt(i).getLabel().equals(label))
	            return attributes.elementAt(i);
	    
	    return null;
	}

    public Attribute getAttributeBySQLLabel(String label) {
        Attribute target = null;
        
        for (int i = 0; i < attributes.size(); i++) {
            System.out.println(attributes.elementAt(i).getLabel());
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
        Attribute target = null;
        
        for (int i = 0; i < attributes.size(); i++)
            if (attributes.elementAt(i).isPrimary() && attributes.elementAt(i).isInt())
                return attributes.elementAt(i);
        
        return null;
    }

    public void relabel() {
        relabel(true);
        for (int i = 0; i < attributes.size(); i++)
            attributes.elementAt(i).relabel();
    }
    
    public boolean hasDateTime() {
        for (int i = 0; i < attributes.size(); i++)
            if (attributes.elementAt(i).isDateTime())
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

    protected void generateParentKeys() {
        System.out.println("\n" + this + " primary keys: " + getPrimaryKeyAttributes());
        for (int i = 0; i < getParents().size(); i++) {
            Entity theSourceEntity = getParents().elementAt(i).getSourceEntity();
            Vector<Attribute> parentKeys = theSourceEntity.getPrimaryKeyAttributes();
            System.out.println("\t" + theSourceEntity + " primary keys: " + parentKeys);
            parentLoop : for (int j = 0; j < parentKeys.size(); j++) {
                Attribute parentKey = parentKeys.elementAt(j);
                if (parentKeyAttributes.size() == 0)
                    parentKeyAttributes.addElement(parentKey);
                for (int k = 0; k < parentKeyAttributes.size(); k++) {
                    System.out.println("\t\tcomparing " + parentKey + " to " + parentKeyAttributes.elementAt(k));
                    if (parentKey.getLabel().equals(parentKeyAttributes.elementAt(k).getLabel()))
                        continue parentLoop;
                }
                System.out.println("\t\tadding " + parentKey);
                parentKeyAttributes.addElement(parentKey);
            }
        }
        System.out.println(this + " parent keys: " + parentKeyAttributes);
    }

    protected void generateSubKeys() {
        parentLoop: for (int j = 0; j < getPrimaryKeyAttributes().size(); j++) {
            Attribute primaryKey = getPrimaryKeyAttributes().elementAt(j);
            for (int k = 0; k < parentKeyAttributes.size(); k++) {
                if (primaryKey.getLabel().equals(parentKeyAttributes.elementAt(k).getLabel()))
                    continue parentLoop;
            }
            subKeyAttributes.addElement(primaryKey);
        }

        System.out.println(this + " sub keys: " + subKeyAttributes);
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

    public void dump() {
        System.out.println("\t\tentity: " + label + "\tuid: " + uid);
        for (int i = 0; i < children.size(); i++)
            System.out.println("\t\t\tchild " + children.elementAt(i).targetEntity.getLabel());
        for (int i = 0; i < attributes.size(); i++)
            attributes.elementAt(i).dump();
    }

	public DomainClass getDomainClass() {
		return domainClass;
	}

	public void setDomainClass(DomainClass domainClass) {
		this.domainClass = domainClass;
	}


}