package org.eclipse.jconqurr.core.parallel.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jconqurr.core.data.ForLoopModifier;
import org.eclipse.jconqurr.core.data.IForLoopModifier;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.Test;

public class TestForLoopModifier {

	@Test
	public void testAnalyzeCode() {
		CompilationUnit cu = getTestCompilationUnit();
		assertTrue(cu.getProblems().length == 0);

		IForLoopModifier flModifier = new ForLoopModifier();

		flModifier.setCompilationUnit(cu);
		assertNotNull(flModifier.getCompilationUnit());

		flModifier.analyzeCode();
	}
	
	@Test
	public void testSetCompilationUnit() {
		CompilationUnit cu = getTestCompilationUnit();
		assertNotNull(cu);
		IForLoopModifier flModifier = new ForLoopModifier();
		
		try {
			flModifier.analyzeCode();
		} catch (Exception e){
			assertTrue(e instanceof NullPointerException);
		}
		
		flModifier.setCompilationUnit(cu);
		assertEquals(cu, flModifier.getCompilationUnit());
		assertNotNull(flModifier.getCompilationUnit());
	}

	@Test
	public void testGetModifiedCode() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetForStatements() {
		fail("Not yet implemented");
	}
	
	private CompilationUnit getTestCompilationUnit() {
		String code = "\n" + 
		"import org.eclipse.jconqurr.annotations.library.ParallelFor;\n\n" +
		"public class MyClass {\n" +
		"@ParallelFor\n" +
		"public void myMethod() {\n" + 
		"\tfor(int i=0;i<5000;i++) {\n" +
		"\t\tint calc=i*900+12/45;\n" +
		"\t}\n" +
		"}\n" +
		"\n" +
		"public void myNextMethod() {\n" + 
		"\tfor(int i=0;i<5000;i++) {\n" +
		"\t\tint calc=i*900+12/45;\n" +
		"\t}\n" +
		"}\n" +
		"}\n";

		Map<String, String> options = new HashMap<String, String>();
		options.put("org.eclipse.jdt.core.compiler.source", "1.6");
		ASTParser tempParser = ASTParser.newParser(AST.JLS3); // Java 5.0 and up
		tempParser.setKind(ASTParser.K_COMPILATION_UNIT);
		tempParser.setCompilerOptions(options);
		tempParser.setSource(code.toCharArray());
		tempParser.setResolveBindings(true);
		CompilationUnit tempUnit = (CompilationUnit) tempParser.createAST(null /* IProgrssMonitor*/);
		
		return tempUnit;
	}

}
