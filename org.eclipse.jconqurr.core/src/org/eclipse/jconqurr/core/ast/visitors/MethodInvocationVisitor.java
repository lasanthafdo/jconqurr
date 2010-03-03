package org.eclipse.jconqurr.core.ast.visitors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class MethodInvocationVisitor extends ASTVisitor {
	 
	List<MethodInvocation> methods = new ArrayList<MethodInvocation>();

	@Override
	public boolean visit(MethodInvocation node) {
		methods.add(node);
		return super.visit(node);
	}
	
	public List<MethodInvocation> getMethods() {
		return methods;
	}
}
