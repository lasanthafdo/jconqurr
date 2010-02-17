package org.eclipse.jconqurr.core.ast.visitors;


import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.SimpleName;

public class AssignmentVisitor extends ASTVisitor {
	
	public boolean visit(Assignment node) {
		if (node.getLeftHandSide() instanceof SimpleName) {
			System.out.println(node.toString());
			System.out.println(node.getLeftHandSide());
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
