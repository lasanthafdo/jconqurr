package org.eclipse.jconqurr.core.ast.visitors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ForStatement;

/**
 * 
 * @author lasantha
 *
 */
public class ForLoopVisitor extends ASTVisitor {
	List<ForStatement> forLoops = new ArrayList<ForStatement>();
	
	@Override
	public boolean visit(ForStatement node) {
		
		forLoops.add(node);
		return super.visit(node);
		
	}
	
	/**
	 * 
	 * @return
	 */
	public List<ForStatement> getForLoops() {
		return forLoops;
	}
}

