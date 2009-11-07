package org.eclipse.jconqurr.core.parallel.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jconqurr.core.ast.ForLoopVisitor;
import org.eclipse.jconqurr.core.parallel.ForLoopModifier;
import org.eclipse.jconqurr.core.parallel.IForLoopModifier;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ForStatement;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class TestForLoopModifier {
	private CompilationUnit unit;
	private IForLoopModifier flModifier;
	
	@Before
	public void initializeCU() {
		unit = getTestCompilationUnit();
		flModifier = new ForLoopModifier();
		flModifier.setCompilationUnit(unit);
	}
	
	@Ignore
	public void testAnalyzeCode() {
		assertTrue(unit.getProblems().length == 0);

		flModifier.setCompilationUnit(unit);
		assertNotNull(flModifier.getCompilationUnit());

		flModifier.analyzeCode();
	}
	
	@Ignore
	public void testSetCompilationUnit() {
		assertNotNull(unit);
		
		try {
			flModifier.analyzeCode();
		} catch (Exception e){
			assertTrue(e instanceof NullPointerException);
		}
		
		flModifier.setCompilationUnit(unit);
		assertEquals(unit, flModifier.getCompilationUnit());
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
	
	@Test
	public void testModifyCode() {
		flModifier.analyzeCode();
		flModifier.modifyCode();
	}
	
	private CompilationUnit getTestCompilationUnit() {
		String code = "\n" + 
		"import org.eclipse.jconqurr.annotations.library.ParallelFor;\n\n" +
		"public class MyClass {\n" +
		"@ParallelFor\n" +
		"public void myMethod() {\n" +
		"\t@ParallelFor {\n" +
		"\tfor(int i=0;i<5000;i++) {\n" +
		"\t\tint calc=i*900+12/45;\n" +
		"\t}\n" +
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
		return (CompilationUnit) tempParser.createAST(null /* IProgrssMonitor*/);
	}
	
	@Ignore 
	public void testParseTree() {
		CompilationUnit unit = getTestCompilationUnit();
		ForLoopVisitor flVisitor = new ForLoopVisitor();
		unit.accept(flVisitor);
		
		for(ForStatement forStatement: flVisitor.getForLoops()) {
			ASTNode node = forStatement;
			while(node != null) {
				System.out.println(node.getNodeType());
				System.out.println(node.toString() + "\n");
				node = node.getParent();
			}
			
		}
	}

}
