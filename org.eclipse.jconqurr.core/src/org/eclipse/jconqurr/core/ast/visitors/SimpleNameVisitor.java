package org.eclipse.jconqurr.core.ast.visitors;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;

public class SimpleNameVisitor extends ASTVisitor {

	@Override
	public boolean visit(SimpleName node) {
		IBinding binding=node.resolveBinding();
		if(node.getParent() instanceof ForStatement){
			System.out.println("Parent is a foor loop");
		}
		System.out.println("Binding name:"+binding.getName());
		System.out.println("Binding kind:"+binding.getKind());
		System.out.println("Binding modifiers:"+binding.getModifiers());
		System.out.println("Binding key:"+binding.getKey());
		System.out.println(node.getParent().toString());
		
		System.out.println("Simple Name:"+node.toString());
		return super.visit(node);
	}
}
