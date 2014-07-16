package edu.uiowa.icts.protogen.springhibernate.velocity;

import org.apache.velocity.app.Velocity;
import org.apache.velocity.VelocityContext;

import java.io.StringWriter;
import java.util.Date;

public class AbstractControllerMVCTestsGenerator extends AbstractVelocityGenerator {
	
	
	public AbstractControllerMVCTestsGenerator(String packageName) {
		// initialize Velocity
		super(packageName);
	}

	public String javaSourceCode() {
        /* lets make a Context and put data into it */
        VelocityContext context = new VelocityContext();
        context.put("packageName", this.packageName);
        context.put("date", new Date().toString()); // can be done with Velocity tools but let's keep it simple to start

        /* lets render a template loaded from the classpath */
        StringWriter w = new StringWriter();
        Velocity.mergeTemplate("/velocity-templates/AbstractControllerMVCTests.java", Velocity.ENCODING_DEFAULT, context, w );
        
        return w.toString();
	}

}
