package edu.uiowa.icts.protogen.model;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Schema extends Element {
	static Logger log = LogManager.getLogger(Schema.class);

    Vector<Domain> domains = new Vector<Domain>();
    Vector<Entity> entities = new Vector<Entity>();
    Vector<Relationship> relationships = new Vector<Relationship>();
    
    public Schema() {
    	
    }
    
    public Schema(String sqlLabel, String remarks) {
    	this.sqlLabel = sqlLabel;
    	this.label = sqlLabel;
    	this.remarks = remarks;
    }
    
	public boolean containsRelationship( Relationship relationship ){
		boolean contains = false;
		for(Relationship r : this.getRelationships()){
			if( r.sourceEntity != null && relationship.sourceEntity != null && r.targetEntity != null && relationship.targetEntity != null ){
				if( r.sourceEntity.lowerLabel != null && relationship.sourceEntity.lowerLabel != null && r.targetEntity.lowerLabel != null && relationship.targetEntity.lowerLabel != null ){
					if( r.sourceEntity.lowerLabel.equals(relationship.sourceEntity.lowerLabel) && r.targetEntity.lowerLabel.equals(relationship.targetEntity.lowerLabel) ){
						contains = true;
					}
				}
			}
		}
		return contains;
	}
	
    
    public Vector<Domain> getDomains() {
        return domains;
    }

    public void setDomains(Vector<Domain> domains) {
        this.domains = domains;
    }

    public Domain getDomainByLabel(String label) {
        for (int i = 0; i < domains.size(); i++)
            if (domains.elementAt(i).getLabel().equals(label))
                return domains.elementAt(i);

        return null;
    }
    
    public void addEntity(Entity entity) {
    	entities.add(entity);
    }
    
    public void addRelationship(Relationship relationship) {
    	relationships.add(relationship);
    }

    public void addDomain(Domain domain) {
    	domains.add(domain);
    }

    public Vector<Entity> getEntities() {
        return entities;
    }

    public void setEntities(Vector<Entity> entities) {
        this.entities = entities;
    }

    public Entity getEntityByLabel(String label) {
        for (int i = 0; i < entities.size(); i++)
            if (entities.elementAt(i).getLabel().equals(label))
                return entities.elementAt(i);

        return null;
    }
    
    public Entity getEntityBySQLLabel(String label) {
        for (int i = 0; i < entities.size(); i++)
            if (entities.elementAt(i).getSqlLabel().equals(label))
                return entities.elementAt(i);

        return null;
    }
    
    public  void populateEntityAttributeForeignReference()
    {
    	
    	
    	
    	Iterator<Entity> eIter = entities.iterator();
    	while(eIter.hasNext())
    	{
    		Entity e = eIter.next();
    		Iterator<Attribute> aIter = e.getAttributes().iterator();
    		while(aIter.hasNext())
    		{
    			Attribute a = aIter.next();
    			if(a.getReferencedEntityName()!=null)
    				a.setReferencedEntity(getEntityBySQLLabel(a.getReferencedEntityName()));
    			else
    				a.setReferencedEntity(e);
    			
    			a.setEntity(e);
    	
    			
    		}
    		
    		
    	}
	
    	
    	
    }

    public Vector<Relationship> getRelationships() {
        return relationships;
    }

    public void setRelationships(Vector<Relationship> relationships) {
        this.relationships = relationships;
    }

    public void relabel() {
        relabel(false);
        for (int i = 0; i < entities.size(); i++)
            entities.elementAt(i).relabel();
    }

    public void dump(BufferedWriter out) throws IOException {
        out.write("\tschema: " + label + "\tuid: " + uid + "\n");
        for (int i = 0; i < entities.size(); i++)
            entities.elementAt(i).dump(out);
        for (int i = 0; i < relationships.size(); i++)
            relationships.elementAt(i).dump(out);
    }

}
