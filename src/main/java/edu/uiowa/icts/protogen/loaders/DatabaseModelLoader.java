package edu.uiowa.icts.protogen.loaders;

import java.util.Properties;

import edu.uiowa.icts.protogen.model.Database;

public abstract interface DatabaseModelLoader {
    
    public abstract void run(String descriptor) throws Exception;
    
    public abstract void run(Properties props) throws Exception;
    
    public abstract Database getDatabase();
    
}