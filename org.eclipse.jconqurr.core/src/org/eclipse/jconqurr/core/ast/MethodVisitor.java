package org.eclipse.jconqurr.core.ast;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;

/**
 * 
 * @author lasantha
 *
 */
public class MethodVisitor extends ASTVisitor {
	List<MethodDeclaration> methods = new ArrayList<MethodDeclaration>();

	@Override
	public boolean visit(MethodDeclaration node) {
		methods.add(node);
		return super.visit(node);
	}
	
	/**
	 * 
	 * @return
	 */
	public List<MethodDeclaration> getMethods() {
		return methods;
	}
	
}
