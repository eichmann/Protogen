package edu.uiowa.icts.protogen.springhibernate.velocity;

import java.io.StringWriter;
import java.util.Date;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

public class ControllerMvcTestGenerator extends AbstractVelocityGenerator {

	private String className;
	private String jspPath;

	public ControllerMvcTestGenerator( String packageName, String className, String jspPath ) {
		// initialize Velocity
		super( packageName );
		this.className = className;
		this.jspPath = jspPath;
	}

	public String javaSourceCode() {
		/* lets make a Context and put data into it */
		VelocityContext context = new VelocityContext();
		context.put( "packageName", this.packageName );
		context.put( "date", new Date().toString() ); // can be done with Velocity tools but let's keep it simple to start
		context.put( "className", this.className );
		context.put( "jspPath", this.jspPath );

		/* lets render a template loaded from the classpath */
		StringWriter w = new StringWriter();
		Velocity.mergeTemplate( "/velocity-templates/ControllerMvcTest.java", Velocity.ENCODING_DEFAULT, context, w );
		return w.toString();

	}

}
