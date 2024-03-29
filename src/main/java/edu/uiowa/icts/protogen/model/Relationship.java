package edu.uiowa.icts.protogen.model;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Relationship extends Element {

	public Entity sourceEntity = null;
	String sourceEntityName = null; // necessary for foreign key references occurring prior to target table declaration
	public Entity targetEntity = null;
	Hashtable<String,String> attributeMap = new Hashtable<String,String>();

	enum CardinalityEnum {
		ONE_TO_ONE, ONE_TO_MANY, ONE, MANY
	};

	static Logger log = LogManager.getLogger(Relationship.class);

	CardinalityEnum relationshipCardinality = CardinalityEnum.ONE_TO_MANY;
	CardinalityEnum sourceEntityCardinality = CardinalityEnum.ONE;
	CardinalityEnum targetEntityCardinality = CardinalityEnum.MANY;

	public Entity getSourceEntity() {
		return sourceEntity;
	}

	public void setSourceEntity(Entity sourceEntity) {
        log.debug("setting source entity: " + sourceEntity);
		this.sourceEntity = sourceEntity;
	}

	public String getSourceEntityName() {
        return sourceEntityName;
    }

    public void setSourceEntityName(String sourceEntityName) {
        this.sourceEntityName = sourceEntityName;
    }

    public Entity getTargetEntity() {
		return targetEntity;
	}

	public void setTargetEntity(Entity targetEntity) {
		this.targetEntity = targetEntity;
	}
	
	public Set<String> getSourceAttributes() {
		return attributeMap.keySet();
	}
	
	public String getTargetAttribute(String source) {
		return attributeMap.get(source);
	}

	public CardinalityEnum getRelationshipCardinality() {
		return relationshipCardinality;
	}

	public void setRelationshipCardinality(
			CardinalityEnum relationshipCardinality) {
		this.relationshipCardinality = relationshipCardinality;
	}

	public CardinalityEnum getSourceEntityCardinality() {
		return sourceEntityCardinality;
	}

	public void setSourceEntityCardinality(CardinalityEnum sourceEntityCardinality) {
		this.sourceEntityCardinality = sourceEntityCardinality;
	}

	public CardinalityEnum getTargetEntityCardinality() {
		return targetEntityCardinality;
	}

	public void setTargetEntityCardinality(CardinalityEnum targetEntityCardinality) {
		this.targetEntityCardinality = targetEntityCardinality;
	}
	
	public void setForeignReferencedAttributeMapping(String foreignAttribute, String referencedAttribute) {
	    log.debug(sourceEntity + " " + referencedAttribute + " --> " + targetEntity + " " + foreignAttribute);
	    attributeMap.put(referencedAttribute, foreignAttribute);
	//TODO    if (!referencedAttribute.equals(foreignAttribute)) targetEntity.getAttributeByLabel(foreignAttribute).setForeignAttribute(sourceEntity.getAttributeByLabel(referencedAttribute));
	}
	
	public String getForeignReferencedAttribute(String referencedAttribute) {
	    return attributeMap.get(referencedAttribute);
	}

    public void dump(BufferedWriter out) throws IOException {
        out.write("\t\tsource entity: " + sourceEntity + "\ttarget entity: " + targetEntity + "\tuid: " + uid + "\n");
        log.debug("\t\tsource entity: " + (sourceEntity == null ? null : sourceEntity.getLabel()) + "\ttarget entity: " + (targetEntity == null ? null : targetEntity.getLabel()) + "\tuid: " + uid);
    }

	@Override
	public String toString() {
		return "Relationship ["
				+ "sourceEntity=" + sourceEntity
				+ ", sourceEntityName=" + sourceEntityName
				+ ", targetEntity="	+ targetEntity
				+ ", attributeMap="	+ attributeMap
				+ "]";
	}
    
}
