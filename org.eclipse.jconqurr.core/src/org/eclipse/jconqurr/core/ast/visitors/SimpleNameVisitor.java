package org.eclipse.jconqurr.core.ast.visitors;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;

public class SimpleNameVisitor extends ASTVisitor {
	private String identifier;

	/**
	 * @return the identifier
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	private String type;

	@Override
	public boolean visit(SimpleName node) {

		IBinding binding = node.resolveBinding();
		IVariableBinding vbinding = (IVariableBinding) node.resolveBinding();
		identifier = node.toString();
		type = vbinding.getType().getName();
		if (node.getParent() instanceof ForStatement) {
			System.out.println("Parent is a foor loop");
		}
		System.out.println("Identifier :" + identifier);
		System.out.println("type :" + type);
		System.out.println("Binding name:" + binding.getName());
		System.out.println("Binding kind:" + binding.getKind());
		System.out.println("Binding modifiers:" + binding.getModifiers());
		System.out.println("Binding key:" + binding.getKey());
		System.out.println(node.getParent().toString());

		System.out.println("Simple Name:" + node.toString());
		return false;
	}
}
