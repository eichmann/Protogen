package edu.uiowa.icts.protogen.springhibernate.velocity;

import java.util.Properties;
import org.apache.velocity.app.Velocity;

public class AbstractVelocityGenerator {

	public AbstractVelocityGenerator() {
		/* 
		 * init the runtime engine, which only takes affect with first call to .init(p) 
		 * subsequent calls to init are ignored.
		 * */
		Properties p = new Properties();
	    p.setProperty("resource.loader", "class");
	    p.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
	    p.setProperty("class.resource.loader.cache", "true");
	    p.setProperty("runtime.log.logsystem.log4j.logger","Apache Velocity");
	    Velocity.init( p );
	}
	
}
