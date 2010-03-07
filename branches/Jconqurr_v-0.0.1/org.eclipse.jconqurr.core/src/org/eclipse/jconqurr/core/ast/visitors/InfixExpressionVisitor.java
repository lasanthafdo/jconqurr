package org.eclipse.jconqurr.core.ast.visitors;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.InfixExpression;

public class InfixExpressionVisitor extends ASTVisitor {
	private String operator;
	private String rightHandSide;
	private String leftHandSide;

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
		return super.visit(node);

	}

}
