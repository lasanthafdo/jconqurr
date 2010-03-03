package org.eclipse.jconqurr.core.ast.visitors;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class CompilationUnitParser {
	public static CompilationUnit parse(ICompilationUnit cu) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(cu);
		parser.setResolveBindings(true);
		return (CompilationUnit) parser.createAST(null);		
	}
	
	
}
