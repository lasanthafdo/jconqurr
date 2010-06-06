package org.eclipse.jconqurr.core.ast.visitors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.SimpleName;

public class NameVisitor extends ASTVisitor {
	List<String> identifiers = new ArrayList<String>();
	List<SimpleName> simpleNames = new ArrayList<SimpleName>();

	@Override
	public boolean visit(SimpleName node) {
		simpleNames.add(node);
		return true;
	}

	public List<SimpleName> getSimpleNames() {
		return simpleNames;
	}
}
