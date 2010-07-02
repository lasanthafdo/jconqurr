package org.eclipse.jconqurr.core.ast.visitors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class TypeDeclarationVisitor extends ASTVisitor {
	List<TypeDeclaration> typeDeclarations = new ArrayList<TypeDeclaration>();

	@Override
	public boolean visit(TypeDeclaration node) {
		// TODO Auto-generated method stub
		typeDeclarations.add(node);
		return super.visit(node);
	}

	public List<TypeDeclaration> getTypeDeclarations() {
		return typeDeclarations;
	}
}
