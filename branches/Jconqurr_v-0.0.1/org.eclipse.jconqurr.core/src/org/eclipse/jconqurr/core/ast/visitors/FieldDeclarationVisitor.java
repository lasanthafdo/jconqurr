package org.eclipse.jconqurr.core.ast.visitors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;

public class FieldDeclarationVisitor extends ASTVisitor{
	private List<FieldDeclaration> fields=new ArrayList<FieldDeclaration>();
	public boolean visit(FieldDeclaration node) {
		fields.add(node);
		return true;
	}
	
	public List<FieldDeclaration> getFields(){
		return fields;
	}
}
