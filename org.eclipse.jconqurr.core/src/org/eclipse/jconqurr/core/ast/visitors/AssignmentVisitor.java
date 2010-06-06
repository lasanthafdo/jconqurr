package org.eclipse.jconqurr.core.ast.visitors;


import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.SimpleName;
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
		if (node.getLeftHandSide() instanceof SimpleName) {
			//System.out.println(node.toString());
			//System.out.println(node.getLeftHandSide());
			/*IBinding binding = ((SimpleName) node.getLeftHandSide())
					.resolveBinding();
			if (localVariableManagers.containsKey(binding)) {
				// contains key -> it is an assignment ot a local variable

				VariableBindingManager manager = localVariableManagers
						.get(binding);

				manager.variableInitialized(node.getRightHandSide());
			}*/
		}
		// prevent that simplename is interpreted as reference
		return super.visit(node);
	}
}
