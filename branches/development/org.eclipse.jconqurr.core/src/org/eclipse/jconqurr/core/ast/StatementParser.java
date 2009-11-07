package org.eclipse.jconqurr.core.ast;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;

public class StatementParser {
	public static Block parse(String code) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_STATEMENTS);
		parser.setSource(code.toCharArray());
		parser.setResolveBindings(true);
		Block block = (Block)parser.createAST(null);
		System.out.println(block.toString());
		return block;		
	}
}
