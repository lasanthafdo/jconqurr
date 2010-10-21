package org.eclipse.jconqurr.util.test;

import static org.junit.Assert.*;

import org.eclipse.jconqurr.util.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class StringUtilsTest {

	private static String testString = "";
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		testString = "main";
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testToProperCase() {
		String result = StringUtils.toProperCase(testString);
		assertEquals("main", testString);
		assertEquals("Main", result);
	}

}
