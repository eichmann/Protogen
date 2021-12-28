package edu.uiowa.icts.protogen.springhibernate;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Generates DAO test classes
 * @author bkusenda
 */
public class BaseTestCodeGenerator extends AbstractSpringHibernateCodeGenerator {
	static Logger log = LogManager.getLogger(BaseTestCodeGenerator.class);

	private Properties properties;

	public BaseTestCodeGenerator( SpringHibernateModel model, String pathBase, String packageRoot, Properties properties ) {
		super( model, pathBase, packageRoot );
		this.properties = properties;
		( new File( packageRootPath ) ).mkdirs();
	}

	/**
	 * @param {@link DomainClass}
	 * @throws IOException
	 */
	private void generateTest( DomainClass dc ) throws IOException {

		String packageName = model.getPackageRoot() + ( Boolean.valueOf( properties.getProperty( "include.schema.in.package.name", "true" ) ) ? "." + dc.getSchema().getLowerLabel() : "" ) + ".dao";

		String packagePath = pathBase + "/" + packageName.replaceAll( "\\.", "/" );

		String className = "" + dc.getIdentifier() + "Test";

		List<String> importList = new ArrayList<String>();
		importList.add( "import edu.uiowa.icts.spring.*;" );
		importList.add( "import edu.uiowa.icts.spring.AbstractSpringTestCase;" );
		importList.add( "import org.junit.After;" );
		importList.add( "import org.junit.Before;" );
		importList.add( "import org.junit.Test;" );
		importList.add( "import org.springframework.beans.factory.annotation.Autowired;" );
		importList.add( "import org.springframework.test.context.web.WebAppConfiguration;" );
	//	importList.add( "import " + dc.getPackageName() + ".*;" );

		BufferedWriter out = createFileInSrcElseTarget( packagePath, className + ".java" );

		/*
		 * Print Package
		 */
		out.write( "package " + packageName + ";\n" );
		lines( out, 1 );

		/*
		 * Print imports
		 */
		for ( String importSt : importList ) {
			out.write( importSt + "\n" );
		}

		String daoClassName = properties.getProperty( dc.getSchema().getUpperLabel().toLowerCase() + ".master.dao.service.name" );
		if ( daoClassName == null || "".equals( className.trim() ) ) {
			daoClassName = dc.getSchema().getUpperLabel() + "DaoService";
		}
		
		String daoServiceName = StringUtils.substring( daoClassName, 0, 1 ).toLowerCase() + StringUtils.substring( daoClassName, 1, daoClassName.length() );

		lines( out, 1 );

		SimpleDateFormat sdf = new SimpleDateFormat( "MM/dd/yyyy HH:mm:ss z", Locale.US );
		sdf.setTimeZone( TimeZone.getDefault() );

		out.write( "/**\n" );
		out.write( " * Unit test Template\n" );
		out.write( " * Generated by Protogen\n" );
		out.write( " * @since " + sdf.format( new Date() ) + "\n" );
		out.write( " */\n" );

		/*
		 * Print interface header
		 */
		out.write( "@WebAppConfiguration\n" );
		out.write( "public class " + className + " extends AbstractSpringTestCase {\n\n" );
		
		spaces( out, 4 );
		out.write( "@Autowired\n" );
		spaces( out, 4 );
		out.write( "private " + daoClassName + " " + daoServiceName + ";\n" );
		lines( out, 1 );
		out.write( createGetter( daoClassName, daoServiceName, 4 ) );
		lines( out, 1 );
		out.write( createSetter( daoClassName, daoServiceName, 4 ) );

		lines( out, 1 );
		String methodTemplates = ""
			+ indent( 4 ) + "@Test\n"
			+ indent( 4 ) + "public void testServiceName() {\n"
			+ indent( 4 ) + "	assertEquals( true, true );\n"
			+ indent( 4 ) + "}\n\n"
			
			+ indent( 4 ) + "@Before\n"
			+ indent( 4 ) + "public void setUp() throws Exception {\n"
			+ indent( 4 ) + "	super.setUp();\n"
			+ indent( 4 ) + "}\n\n"
			
			+ indent( 4 ) + "@After\n"
			+ indent( 4 ) + "public void tearDown() throws Exception {\n"
			+ indent( 4 ) + "	super.tearDown();\n"
			+ indent( 4 ) + "}\n\n";
		
		out.write( methodTemplates );

		out.write( "}" );
		
		out.close();
	}

	/**
	 * public method to generate java domain test code
	 */
	public void generate() throws IOException {
		for ( DomainClass dc : model.getDomainClassList() ){
			generateTest( dc );
		}
	}

}
