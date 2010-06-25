package edu.uiowa.webapp;

import java.util.Vector;

public class Database extends ClayElement {

    Vector<Schema> schemas = new Vector<Schema>();

    public Vector<Schema> getSchemas() {
        return schemas;
    }

    public void setSchemas(Vector<Schema> schemas) {
        this.schemas = schemas;
    }

    public Schema getSchemaByName(String label) {
        Entity target = null;

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

    public void dump() {
        System.out.println("database: " + label + "\tuid: " + uid);
        for (int i = 0; i < schemas.size(); i++)
            schemas.elementAt(i).dump();
    }

}
