package org.eclipse.jconqurr.core.ast.visitors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MarkerAnnotation;;

public class AnnotationVisitor extends ASTVisitor {
	List<MarkerAnnotation> annotations = new ArrayList<MarkerAnnotation>();
	
	@Override
	public boolean visit(MarkerAnnotation node) {
		annotations.add(node);
		return super.visit(node);
	}
	
	/**
	 * 
	 * @return
	 */
	public List<MarkerAnnotation> getAnnotations() {
		return annotations;
	}
}
