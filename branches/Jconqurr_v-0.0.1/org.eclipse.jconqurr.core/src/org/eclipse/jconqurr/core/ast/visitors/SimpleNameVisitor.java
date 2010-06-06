package org.eclipse.jconqurr.core.ast.visitors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.SimpleName;

public class SimpleNameVisitor extends ASTVisitor {
	List<String> identifiers = new ArrayList<String>();
	List<SimpleName> simpleNames = new ArrayList<SimpleName>();
	List<SimpleName>variables=new ArrayList<SimpleName>();
	private String identifier;

	@Override
	public boolean visit(SimpleName node) {
		simpleNames.add(node);
		IBinding binding = node.resolveBinding();
		identifier = node.toString();
		identifiers.add(identifier);
		try {
			IVariableBinding vbinding = (IVariableBinding) node
					.resolveBinding();
			variables.add(node);
			if (vbinding != null) {
				type = vbinding.getType().getName();
			}
		} catch (ClassCastException e) {
			//e.printStackTrace();
		}

		return true;
	}

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

	public List<SimpleName> getSimpleNames() {
		return simpleNames;
	}
	
	public List<SimpleName> getVariables() {
		return variables;
	}

	public List<String> getIdentifiers() {
		return identifiers;
	}

	private String type;

}
