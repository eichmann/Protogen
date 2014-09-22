package edu.uiowa.icts.protogen.springhibernate.velocity;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.junit.Test;

import edu.uiowa.icts.protogen.springhibernate.DomainClass;
import edu.uiowa.webapp.Schema;

public class ControllerMvcTestGeneratorTest {
	@Test
	public void shouldGenerateJavaSourceCodeForSpringMVCTestFileWithSchemaNameInPackageName() {
		String packageRoot = "edu.uiowa.icts";
		
		Schema schema = new Schema();
		schema.setLabel("ictssysadmin");
		
		DomainClass domainClass = new DomainClass(null);
		domainClass.setSchema(schema);
		domainClass.setIdentifier("ClinicalDocument");
		
		Properties properties = new Properties();
	//	properties.setProperty( "include.schema.in.package.name", "false" );	
		
		ControllerMvcTestGenerator generator = new ControllerMvcTestGenerator(packageRoot,domainClass,properties);
		
		String sourceCode = generator.javaSourceCode();
		
		assertThat(sourceCode, containsString("package edu.uiowa.icts.ictssysadmin.controller;"));	
	}
	
	@Test
	public void shouldGenerateJavaSourceCodeForSpringMVCTestFileWithoutSchemaNameInPackageName() {
		String packageRoot = "edu.uiowa.icts";
		
		Schema schema = new Schema();
		schema.setLabel("ictssysadmin");
		
		DomainClass domainClass = new DomainClass(null);
		domainClass.setSchema(schema);
		domainClass.setIdentifier("ClinicalDocument");
		
		Properties properties = new Properties();
		properties.setProperty( "include.schema.in.package.name", "false" );	
		
		ControllerMvcTestGenerator generator = new ControllerMvcTestGenerator(packageRoot,domainClass,properties);
		
		String sourceCode = generator.javaSourceCode();
		
		assertThat(sourceCode, containsString("package edu.uiowa.icts.controller;"));	
	}
	
	@Test
	public void shouldGenerateJavaSourceCodeForSpringMVCTestFileWithSchemaNameInRequestMapping() {
		String packageRoot = "edu.uiowa.icts";
		
		Schema schema = new Schema();
		schema.setLabel("ictssysadmin");
		
		DomainClass domainClass = new DomainClass(null);
		domainClass.setSchema(schema);
		domainClass.setIdentifier("ClinicalDocument");
		
		Properties properties = new Properties();
		properties.setProperty( "datatables.generation", "2" );
	//	properties.setProperty( "include.schema.in.request.mapping", "false" );	
		
		ControllerMvcTestGenerator generator = new ControllerMvcTestGenerator(packageRoot,domainClass,properties);
		
		String sourceCode = generator.javaSourceCode();
		
		assertThat(sourceCode, containsString("package edu.uiowa.icts.ictssysadmin.controller;"));	
		assertThat(sourceCode, containsString("* Generated by Protogen"));
		
		SimpleDateFormat ft = new SimpleDateFormat ("EEE MMM dd");
		assertThat(sourceCode, containsString(ft.format(new Date())));
		assertThat(sourceCode, containsString("ClinicalDocumentControllerMvcTest extends AbstractControllerMVCTests"));
        
		// test list_alt
		assertThat(sourceCode, containsString("public void listAltShouldLoadListOfClinicalDocuments() throws Exception {"));
		assertThat(sourceCode, containsString("mockMvc.perform(get(\"/ictssysadmin/clinicaldocument/list_alt\"))"));
		assertThat(sourceCode, containsString(".andExpect(model().attributeExists(\"clinicalDocumentList\"))"));
		assertThat(sourceCode, containsString(".andExpect(view().name(\"/ictssysadmin/clinicaldocument/list_alt\"));"));
		
		// test list
		assertThat(sourceCode, containsString("public void listShouldSimplyLoadPage() throws Exception {"));
		assertThat(sourceCode, containsString("mockMvc.perform(get(\"/ictssysadmin/clinicaldocument/list\"))"));
		assertThat(sourceCode, containsString(".andExpect(view().name(\"/ictssysadmin/clinicaldocument/list\"));"));
		
		// test index
		assertThat(sourceCode, containsString("public void indexShouldDisplayListPage() throws Exception {"));
		assertThat(sourceCode, containsString("mockMvc.perform(get(\"/ictssysadmin/clinicaldocument/\"))"));
		assertThat(sourceCode, containsString(".andExpect(view().name(\"/ictssysadmin/clinicaldocument/list\"));"));
		
		// test add
		assertThat(sourceCode, containsString("public void addShouldDisplayNewClinicalDocumentForm() throws Exception {"));
		assertThat(sourceCode, containsString("mockMvc.perform(get(\"/ictssysadmin/clinicaldocument/add\"))"));
		assertThat(sourceCode, containsString(".andExpect(model().attributeExists(\"clinicalDocument\"))"));
		assertThat(sourceCode, containsString(".andExpect(view().name(\"/ictssysadmin/clinicaldocument/edit\"));"));
	}
	
	
	@Test
	public void shouldGenerateJavaSourceCodeForSpringMVCTestFileWithoutSchemaNameInRequestMapping() {
		String packageRoot = "edu.uiowa.icts";
		
		Schema schema = new Schema();
		schema.setLabel("ictssysadmin");
		
		DomainClass domainClass = new DomainClass(null);
		domainClass.setSchema(schema);
		domainClass.setIdentifier("ClinicalDocument");
		
		Properties properties = new Properties();
		properties.setProperty( "datatables.generation", "2" );
		properties.setProperty( "include.schema.in.request.mapping", "false" );	
		
		ControllerMvcTestGenerator generator = new ControllerMvcTestGenerator(packageRoot,domainClass,properties);
		
		String sourceCode = generator.javaSourceCode();
		
		assertThat(sourceCode, containsString("package edu.uiowa.icts.ictssysadmin.controller;"));	
		assertThat(sourceCode, containsString("* Generated by Protogen"));
		
		SimpleDateFormat ft = new SimpleDateFormat ("EEE MMM dd");
		assertThat(sourceCode, containsString(ft.format(new Date())));
		assertThat(sourceCode, containsString("ClinicalDocumentControllerMvcTest extends AbstractControllerMVCTests"));
        
		// test list_alt
		assertThat(sourceCode, containsString("public void listAltShouldLoadListOfClinicalDocuments() throws Exception {"));
		assertThat(sourceCode, containsString("mockMvc.perform(get(\"/clinicaldocument/list_alt\"))"));
		assertThat(sourceCode, containsString(".andExpect(model().attributeExists(\"clinicalDocumentList\"))"));
		assertThat(sourceCode, containsString(".andExpect(view().name(\"/ictssysadmin/clinicaldocument/list_alt\"));"));
		
		// test list
		assertThat(sourceCode, containsString("public void listShouldSimplyLoadPage() throws Exception {"));
		assertThat(sourceCode, containsString("mockMvc.perform(get(\"/clinicaldocument/list\"))"));
		assertThat(sourceCode, containsString(".andExpect(view().name(\"/ictssysadmin/clinicaldocument/list\"));"));
		
		// test index
		assertThat(sourceCode, containsString("public void indexShouldDisplayListPage() throws Exception {"));
		assertThat(sourceCode, containsString("mockMvc.perform(get(\"/clinicaldocument/\"))"));
		assertThat(sourceCode, containsString(".andExpect(view().name(\"/ictssysadmin/clinicaldocument/list\"));"));
		
		// test add
		assertThat(sourceCode, containsString("public void addShouldDisplayNewClinicalDocumentForm() throws Exception {"));
		assertThat(sourceCode, containsString("mockMvc.perform(get(\"/clinicaldocument/add\"))"));
		assertThat(sourceCode, containsString(".andExpect(model().attributeExists(\"clinicalDocument\"))"));
		assertThat(sourceCode, containsString(".andExpect(view().name(\"/ictssysadmin/clinicaldocument/edit\"));"));
	}
	
	@Test
	public void shouldGenerateJavaSourceCodeForSpringMVCTestFileWithDotHTMLInRequestMapping() {
		String packageRoot = "edu.uiowa.icts";
		
		Schema schema = new Schema();
		schema.setLabel("ictssysadmin");
		
		DomainClass domainClass = new DomainClass(null);
		domainClass.setSchema(schema);
		domainClass.setIdentifier("ClinicalDocument");
		
		Properties properties = new Properties();
		properties.setProperty( "controller.request.mapping.extension", ".html" );	
		properties.setProperty( "include.schema.in.request.mapping", "false" );	

		ControllerMvcTestGenerator generator = new ControllerMvcTestGenerator(packageRoot,domainClass,properties);
		
		String sourceCode = generator.javaSourceCode();
		
		assertThat(sourceCode, containsString("mockMvc.perform(get(\"/clinicaldocument/list_alt.html\"))"));
		assertThat(sourceCode, containsString("mockMvc.perform(get(\"/clinicaldocument/list.html\"))"));
		assertThat(sourceCode, containsString("mockMvc.perform(get(\"/clinicaldocument/\"))"));
		assertThat(sourceCode, containsString("mockMvc.perform(get(\"/clinicaldocument/add.html\"))"));
	}
	
	@Test
	public void shouldGenerateJavaSourceCodeForSpringMVCTestFileWithoutDotHTMLInRequestMapping() {
		String packageRoot = "edu.uiowa.icts";
		
		Schema schema = new Schema();
		schema.setLabel("ictssysadmin");
		
		DomainClass domainClass = new DomainClass(null);
		domainClass.setSchema(schema);
		domainClass.setIdentifier("ClinicalDocument");
		
		Properties properties = new Properties();
		properties.setProperty( "include.schema.in.request.mapping", "false" );	

		ControllerMvcTestGenerator generator = new ControllerMvcTestGenerator(packageRoot,domainClass,properties);
		
		String sourceCode = generator.javaSourceCode();
		
		assertThat(sourceCode, containsString("mockMvc.perform(get(\"/clinicaldocument/list_alt\"))"));
		assertThat(sourceCode, containsString("mockMvc.perform(get(\"/clinicaldocument/list\"))"));
		assertThat(sourceCode, containsString("mockMvc.perform(get(\"/clinicaldocument/\"))"));
		assertThat(sourceCode, containsString("mockMvc.perform(get(\"/clinicaldocument/add\"))"));
	}


}
