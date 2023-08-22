package edu.uiowa.icts.protogen.model;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Database extends Element {
	static Logger log = LogManager.getLogger(Database.class);

    Vector<Schema> schemas = new Vector<Schema>();
    
    public Database() {
    	
    }
    
    public Database(String sqlLabel, String remarks) {
    	this.sqlLabel = sqlLabel;
    	this.label = sqlLabel;
    	this.remarks = remarks;
    }

    public void addSchema(Schema schema) {
    	schemas.add(schema);
    }
    
    public Vector<Schema> getSchemas() {
        return schemas;
    }

    public void setSchemas(Vector<Schema> schemas) {
        this.schemas = schemas;
    }

    public Schema getSchemaByName(String label) {
        for (int i = 0; i < schemas.size(); i++)
            if (schemas.elementAt(i).getLabel().equals(label))
                return schemas.elementAt(i);

        return null;
    }

    public void relabel() {
        relabel(false);
        for (int i = 0; i < schemas.size(); i++)
            schemas.elementAt(i).relabel();
    }

    public void dump(BufferedWriter out) throws IOException {
        out.write("database: " + label + "\tuid: " + uid + "\n");
        for (int i = 0; i < schemas.size(); i++)
            schemas.elementAt(i).dump(out);
    }

}
