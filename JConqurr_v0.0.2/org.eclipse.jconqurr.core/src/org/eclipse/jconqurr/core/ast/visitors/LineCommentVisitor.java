package org.eclipse.jconqurr.core.ast.visitors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.LineComment;

public class LineCommentVisitor extends ASTVisitor {
	List<LineComment> comments = new ArrayList<LineComment>();

	public boolean visit(LineComment node) {
		System.out.println("Called the comment visitor");
		comments.add(node);
		System.out.println(node.toString());
		return super.visit(node);
	}

	public List<LineComment> getComments() {
		return comments;
	}
}
