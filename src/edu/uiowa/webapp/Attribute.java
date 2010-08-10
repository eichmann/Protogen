package edu.uiowa.webapp;

public class Attribute extends ClayElement {

    Domain domain = null;
    String sqlType = null;
    String type = null;
    String remarks = null;
    boolean mandatory = false;
    boolean primary = false;
    boolean autoIncrement = false;
    boolean foreign = false;
    boolean sequence = false;
    boolean counter = false;
    String sequenceName = null;
    Entity dominantEntity = null;
    String referencedEntityName = null;
    Entity referencedEntity = null;
    Entity entity = null;
    

	public Entity getEntity() {
		return entity;
	}

	public void setEntity(Entity entity) {
		this.entity = entity;
	}

	Attribute foreignAttribute = null;
    




	public Entity getReferencedEntity() {
		return referencedEntity;
	}

	public void setReferencedEntity(Entity referencedEntity) {
		this.referencedEntity = referencedEntity;
	}

	public String getReferencedEntityName() {
		return referencedEntityName;
	}

	public void setReferencedEntityName(String referencedEntityName) {
		this.referencedEntityName = referencedEntityName;
	}

	public Domain getDomain() {
        return domain;
    }

    public void setDomain(Domain domain) {
        this.domain = domain;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks.trim();
    }
    
    public void matchRemarks() {
        if (remarks == null)
            return;
        else if (remarks.startsWith("counter ")) {
            counter = true;
            String schemaName = remarks.substring(remarks.indexOf(' ') + 1, remarks.indexOf('.'));
            String entityName = remarks.substring(remarks.indexOf('.') + 1);
            System.out.println("schema: " + schemaName + " - " + Generator.getDatabase().getSchemaByName(schemaName));
            System.out.println("entity: " + entityName + " - " + Generator.getDatabase().getSchemaByName(schemaName).getEntityByLabel(entityName));
            dominantEntity = Generator.getDatabase().getSchemaByName(schemaName).getEntityByLabel(entityName);
        }
        else if (remarks.startsWith("sequence ")) {
            sequence = true;
            sequenceName = remarks.substring(remarks.indexOf(' ') + 1);
        }
    }
    
    public String getInitializer() {
        if (isInt())
            return "0";
        else if (isDouble())
            return "0.0";
        else if (isBoolean())
            return "false";
        else
            return "null";
    }

    public String getDefaultValue() {
        if (isInt())
            return "Sequence.generateID()";
        else if (isDouble())
            return "0.0";
        else if (isDateTime())
            return "new Date()";
        else if (isBoolean())
            return "false";
        else
            return "null";
    }

    public boolean isCounter() {
        return counter;
    }
    
    public Entity getDominantEntity() {
        return dominantEntity;
    }

    public boolean isSequence() {
        return sequence;
    }
    
    public String getSequence() {
        return sequenceName;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    public void setAutoIncrement(boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
    }

    public boolean isForeign() {
        return foreign;
    }

    public void setForeign(boolean foreign) {
        this.foreign = foreign;
    }

    public void setForeignAttribute(Attribute foreignAttribute) {
        setForeign(true);
        this.foreignAttribute = foreignAttribute;
    }

    public void relabel() {
        sqlLabel = label;
        if (label.toLowerCase().equals("id"))
                label = "ID";
        else
            relabel(false);
        
        sqlType = Character.toUpperCase(type.charAt(0)) + type.toLowerCase().substring(1);
        if (type.toLowerCase().equals("int"))
            type = "int";
        else if (type.toLowerCase().equals("smallint"))
            type = "int";
        else if (type.toLowerCase().equals("integer"))
            type = "int";
        else if (type.toLowerCase().equals("int4"))
            type = "int";
        else if (type.toLowerCase().equals("numeric"))
            type = "int";
        else if (type.toLowerCase().equals("decimal"))
            type = "int";
        else if (type.toLowerCase().equals("text"))
            type = "String";
        else if (type.toLowerCase().equals("char"))
            type = "String";
        else if (type.toLowerCase().equals("varchar"))
            type = "String";
        else if (type.toLowerCase().equals("date"))
            type = "Date";
        else if (type.toLowerCase().equals("timestamp"))
            type = "Date";
        else if (type.toLowerCase().equals("double precision"))
            type = "double";
        else if (type.toLowerCase().equals("float"))
            type = "double";
        else if (type.toLowerCase().equals("boolean"))
            type = "boolean";
        else if (type.toLowerCase().equals("real"))
            type = "float";
        else
            type = "Object";
    }
    
    public boolean isInt() {
        return type.equals("int");
    }
    
    public boolean isText() {
        return type.equals("String");
    }
    
    public boolean isBoolean() {
        return type.equals("boolean");
    }
    
    public boolean isDouble() {
        return type.equals("double");
    }
    
    public boolean isDateTime() {
        return type.equals("Date");
    }
    
    public boolean isTime() {
        return sqlType.equals("Timestamp");
    }
    
    public boolean isImage() {
        return domain != null && domain.getLabel().equals("Image");
    }
    
    public boolean isDomain() {
        return domain != null && !domain.getLabel().equals("Image");
    }
    public boolean isBinaryDomain() {
        return domain != null && domain.getJavaType().equals("byte[]");
    }
    
    public boolean isByteA() {
    	return sqlType.toLowerCase().equals("bytea");
    }
    
    public String getJavaTypeClass() {
        if (isInt())
            return "Integer";
        if (isText())
            return "String";
        if (isBoolean())
            return "Boolean";
        if (isDouble())
            return "Double";
        if (isDateTime())
            return "Date";
        return type;
    }
    
    public String getSQLMethod(boolean get) {
        if (type.equals("double") || type.equals("float"))
            return (get ? "get" : "set") + "Double";
        else if (sqlType.equals("Numeric"))
            return (get ? "get" : "set") + "Int";
        else if (sqlType.equals("Text") || sqlType.toLowerCase().equals("char") || sqlType.toLowerCase().equals("varchar"))
            return (get ? "get" : "set") + "String";
        else if (sqlType.toLowerCase().equals("int4") || sqlType.toLowerCase().equals("integer") || sqlType.toLowerCase().equals("smallint"))
            return (get ? "get" : "set") + "Int";
        else if (sqlType.toLowerCase().equals("decimal"))
            return (get ? "get" : "set") + "Int";
        else if (sqlType.toLowerCase().equals("bytea"))
            return (get ? "get" : "set") + "Bytes";
        else
            return (get ? "get" : "set") + sqlType;
    }

    public String parseValue() {
    	return parseValue(getLabel());
    }

    public String parseValue(String label) {
        if (type.equals("double") || type.equals("float"))
            return "Double.parseDouble(" + label + ")";
        else if (sqlType.equals("Numeric"))
            return "Integer.parseInt(" + label + ")";
        else if (sqlType.equals("Text") || sqlType.toLowerCase().equals("char") || sqlType.toLowerCase().equals("varchar"))
            return label;
        else if (sqlType.toLowerCase().equals("int4") || sqlType.toLowerCase().equals("integer") || sqlType.toLowerCase().equals("int") || sqlType.toLowerCase().equals("smallint"))
            return "Integer.parseInt(" + label + ")";
        else if (sqlType.toLowerCase().equals("decimal"))
            return "Integer.parseInt(" + label + ")";
        else if (sqlType.toLowerCase().equals("boolean"))        	
            return "Boolean.parseBoolean(" + label + ")";
        else if (sqlType.toLowerCase().equals("timestamp"))
            return "new java.util.Date(Integer.parseInt(" + label + "))";
        else if (sqlType.toLowerCase().equals("bytea"))
            return label;
        else
            return label;
    }

    public void dump() {
        System.out.println("\t\t\tattribute: " + label + "\tuid: " + uid + "\ttype: " + type + "\tmandatory: " + mandatory + "\tprimary: " + primary + "\tauto-increment: " + autoIncrement + "\tremarks: " + remarks + "\tcounter: " + counter + "\tsequence: " + sequence + "\tsequence name: " + sequenceName);
    }


}