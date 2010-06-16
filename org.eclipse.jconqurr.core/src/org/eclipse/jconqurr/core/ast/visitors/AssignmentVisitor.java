package org.eclipse.jconqurr.core.ast.visitors;


import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Assignment.Operator;

public class AssignmentVisitor extends ASTVisitor {
	 
	private Expression leftHandSide;
	private Operator operator;
	private Expression rightHandSide;
	/**
	 * @return the leftHandSide
	 */
	public Expression getLeftHandSide() {
		return leftHandSide;
	}
	public Operator getOperator(){
		return operator;
	}
	/**
	 * @return the rightHandSide
	 */
	public Expression getRightHandSide() {
		return rightHandSide;
	}
	
	public boolean visit(Assignment node) {
		leftHandSide=node.getLeftHandSide();
		rightHandSide=node.getRightHandSide();
		operator=node.getOperator();
		return super.visit(node);
	}
}
