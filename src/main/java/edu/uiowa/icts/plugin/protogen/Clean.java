package edu.uiowa.icts.plugin.protogen;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * @author rrlorent
 * @since Jul 3, 2014
 * @goal clean
 * @requiresDependencyResolution test
 */
public class Clean extends AbstractMojo {
	static Logger log = LogManager.getLogger(Clean.class);

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		log.debug( "cleaning" );
	}
	
}
