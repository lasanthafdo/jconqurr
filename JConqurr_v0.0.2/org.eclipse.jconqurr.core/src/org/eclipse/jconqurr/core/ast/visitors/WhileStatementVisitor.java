package org.eclipse.jconqurr.core.ast.visitors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.WhileStatement;

public class WhileStatementVisitor extends ASTVisitor {
	private List<WhileStatement> whileStatements = new ArrayList<WhileStatement>();

	@Override
	public boolean visit(WhileStatement node) {
		whileStatements.add(node);
		return super.visit(node);
	}
	
	public List<WhileStatement> getWhileStatements() {
		return whileStatements;
	}
}
