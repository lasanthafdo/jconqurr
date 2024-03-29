package org.eclipse.jconqurr.core.ast.visitors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.InfixExpression;

public class ExpressionStatementVisitor extends ASTVisitor {
	List<ExpressionStatement> expressionStatements = new ArrayList<ExpressionStatement>();
	
	private String operator;
	@Override
	public boolean visit(ExpressionStatement node) {
		//System.out.println(node.getLeadingComment());
		
		expressionStatements.add(node);
		return super.visit(node);
	}
	public boolean visit(InfixExpression node){
		//System.out.println(node.getOperator().toString());
		operator=node.getOperator().toString();
		
		return super.visit(node);
		
	}
	public List<ExpressionStatement> getExpressionStatements() {
		return expressionStatements;
	}
	
	public String  getOprator(){
		return operator;
	}
}
