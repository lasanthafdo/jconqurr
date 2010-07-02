package org.eclipse.jconqurr.core.ast.visitors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;;

public class VariableDeclarationVisitor extends ASTVisitor {
	List<VariableDeclarationStatement> variableDeclarations = new ArrayList<VariableDeclarationStatement>();

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		variableDeclarations.add(node);
		return super.visit(node);
	}
	
	/**
	 * 
	 * @return
	 */
	public List<VariableDeclarationStatement> getVariablesDeclaraions() {
		return variableDeclarations;
	}
}
