package org.eclipse.jconqurr.core.ast.visitors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.InfixExpression;

public class InfixExpressionVisitor extends ASTVisitor {
	private String operator;
	private String rightHandSide;
	private String leftHandSide;
	private List<InfixExpression> infixExpressions = new ArrayList<InfixExpression>();

	/**
	 * @return the infixExpressions
	 */
	public List<InfixExpression> getInfixExpressions() {
		return infixExpressions;
	}

	/**
	 * @return the operator
	 */
	public String getOperator() {
		return operator;
	}

	/**
	 * @return the rightHandSide
	 */
	public String getRightHandSide() {
		return rightHandSide;
	}

	/**
	 * @return the leftHandSide
	 */
	public String getLeftHandSide() {
		return leftHandSide;
	}

	public boolean visit(InfixExpression node) {
		// System.out.println(node.getOperator().toString());
		operator = node.getOperator().toString();
		rightHandSide = node.getRightOperand().toString();
		leftHandSide = node.getLeftOperand().toString();
		infixExpressions.add(node);
		return super.visit(node);

	}

}
