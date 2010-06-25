package edu.uiowa.hibernate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

public class FileContents {

	
	  static public String getContents(File aFile) {
	    StringBuilder contents = new StringBuilder();
	    
	    try {
	      BufferedReader input =  new BufferedReader(new FileReader(aFile));
	      try {
	        String line = null; 
	        while (( line = input.readLine()) != null){
	          contents.append(line);
	          contents.append(System.getProperty("line.separator"));
	        }
	      }
	      finally {
	        input.close();
	      }
	    }
	    catch (IOException ex){
	      ex.printStackTrace();
	    }
	    
	    return contents.toString();
	  }
	  
	 static public String getContents(String file) throws Exception {
		  URL rsrcUrl = Thread.currentThread().getContextClassLoader().getResource(file);
		  
	      InputStream in = rsrcUrl.openStream();
	      return convertStreamToString(in);
		  
		  
	  }
	  
	  
	  public static String convertStreamToString(InputStream is) throws  IOException {
        /*
         * To convert the InputStream to String we use the BufferedReader.readLine()
         * method. We iterate until the BufferedReader return null which means
        * there's no more data to read. Each line will appended to a StringBuilder
         * and returned as String.
         */
     if (is != null) {
           StringBuilder sb = new StringBuilder();
           String line;
 
            try {
               BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
               while ((line = reader.readLine()) != null) {
                   sb.append(line).append("\n");
                }
            } finally {
                is.close();
            }
            return sb.toString();
        } else {       
           return "";
        }
    }

	  
	  


}
