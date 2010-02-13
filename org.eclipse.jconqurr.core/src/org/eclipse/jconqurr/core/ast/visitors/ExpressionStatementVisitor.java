package org.eclipse.jconqurr.core.ast.visitors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ExpressionStatement;

public class ExpressionStatementVisitor extends ASTVisitor {
	List<ExpressionStatement> expressionStatements = new ArrayList<ExpressionStatement>();
	
	@Override
	public boolean visit(ExpressionStatement node) {
		expressionStatements.add(node);
		return super.visit(node);
	}
	
	public List<ExpressionStatement> getExpressionStatements() {
		return expressionStatements;
	}
}
