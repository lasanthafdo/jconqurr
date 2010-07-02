package org.eclipse.jconqurr.core.ast.visitors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;

public class ModifiedAssignmentVisitor extends ASTVisitor {
	private List<Assignment> assignments = new ArrayList<Assignment>();
	
	@Override
	public boolean visit(Assignment node) {
		assignments.add(node);
		return super.visit(node);
	}
	
	public List<Assignment> getAssignments() {
		return assignments;
	}
	
	public List<Expression> getAssignmentsAsExpressions() {
		List<Expression> expressions = new ArrayList<Expression>();
		expressions.addAll(assignments);
		return expressions;
	}
	
	public List<Expression> getLeftHandSideExpressions() {
		List<Expression> lhExpressions = new ArrayList<Expression>();
		for(Assignment exp: assignments) {
			lhExpressions.add(exp.getLeftHandSide());
		}
		return lhExpressions;
	}
}
