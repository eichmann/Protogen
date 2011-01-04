package edu.uiowa.icts.protogen;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.uiowa.webapp.Generator;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class GeneratorTest  extends TestCase
{
	
	
	private static final Log log =LogFactory.getLog(GeneratorTest.class);
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public GeneratorTest( String testName )
    {
        super( testName );
        
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( GeneratorTest.class );
    }

    /**
     * Tests Hibernate domain code generation
     */
    public void testHibernateCodeGeneration()
    {
    	String projectName = "Protogen";
    	String pathPrefix = System.getProperty("user.dir");
    	log.debug("PathPrefix:"+pathPrefix);
    	Properties props = new Properties();
    	props.setProperty("package.name","protogen.test");
    	props.setProperty("project.name",projectName);
    	props.setProperty("mode", "spring");
    	props.setProperty("model.source", "clay");
    	props.setProperty("clay.file",pathPrefix+"/src/test/resources/Model.clay");
    	props.setProperty("generate.domain", "true");
    	props.setProperty("generate.dao", "true");
    	props.setProperty("generate.controller", "true");
    	props.setProperty("generate.jsp", "true");
    	props.setProperty("domain.file.location",pathPrefix + "/target/test/java"  + "src");
    	props.setProperty("dao.file.location",pathPrefix + "/target/test/java"  + "src");
    	props.setProperty("controller.file.location",pathPrefix + "/target/test/java"  + "src");
    	props.setProperty("jsp.file.location",pathPrefix + "/target/test/jsp"  + "src");
    	Generator gen = new Generator();
    	int result = gen.runGenerator(props);
        assertEquals("Error during domain code generation",0,result);
    	
    	
    }
}
