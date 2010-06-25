package edu.uiowa.webapp;

public abstract interface DatabaseSchemaLoader {
    
    public abstract void run(String descriptor) throws Exception;
    
    public abstract Database getDatabase();
    
}