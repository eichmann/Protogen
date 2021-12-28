package edu.uiowa.icts.protogen.springhibernate.velocity;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

public class AbstractControllerMVCTestsGeneratorTest {

	@Test
	public void shouldGenerateJavaSourceCodeForAbstractSpringMVCTestFile() {
		String packageName = "edu.uiowa.icts.ictssysadmin.controller";
		AbstractControllerMVCTestsGenerator generator = new AbstractControllerMVCTestsGenerator(packageName);
		String sourceCode = generator.javaSourceCode();
		
		assertThat(sourceCode, containsString("package edu.uiowa.icts.ictssysadmin.controller;"));	
		assertThat(sourceCode, containsString("* Generated by Protogen"));
		
		SimpleDateFormat ft = new SimpleDateFormat ("EEE MMM dd");
		assertThat(sourceCode, containsString(ft.format(new Date())));	
		
	}

}