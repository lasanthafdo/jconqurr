package org.eclipse.jconqurr.core.ast.visitors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.SimpleName;

public class ModifiedSimpleNameVisitor extends ASTVisitor {
	List<SimpleName> simpleNames = new ArrayList<SimpleName>();

	@Override
	public boolean visit(SimpleName node) {
		simpleNames.add(node);
		return super.visit(node);
	}

	public List<SimpleName> getSimpleNames() {
		return simpleNames;
	}
	
	public List<String> getIdentifiers() {
		List<String> identifiers = new ArrayList<String>();
		for(SimpleName name: simpleNames)
			identifiers.add(name.getIdentifier());
		return identifiers;
	}
}
