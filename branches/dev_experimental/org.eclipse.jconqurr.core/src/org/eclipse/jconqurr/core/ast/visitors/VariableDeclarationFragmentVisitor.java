package org.eclipse.jconqurr.core.ast.visitors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class VariableDeclarationFragmentVisitor extends ASTVisitor {
	private List<VariableDeclarationFragment> vdFragments = new ArrayList<VariableDeclarationFragment>();
	
	@Override
	public boolean visit(VariableDeclarationFragment node) {
		vdFragments.add(node);
		return super.visit(node);
	}
	
	public List<VariableDeclarationFragment> getVariableDeclarationFragments() {
		return vdFragments;
	}
}
