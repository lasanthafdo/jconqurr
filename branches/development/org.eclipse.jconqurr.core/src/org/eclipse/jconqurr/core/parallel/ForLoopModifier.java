package org.eclipse.jconqurr.core.parallel;

import java.util.List;

import org.eclipse.jconqurr.annotations.library.ParallelFor;
import org.eclipse.jconqurr.core.ast.MethodVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class ForLoopModifier {
	private String modifiedCode;
	private CompilationUnit compilationUnit;
	private List<ForStatement> forStatements;
	
	public ForLoopModifier() {
		modifiedCode = "";
		compilationUnit = null;
	}
	
	public void analyzeCode() throws NullPointerException {
		if(compilationUnit == null) throw new NullPointerException("Compilation Unit cannot be null");
		
		MethodVisitor methodVisitor = new MethodVisitor();
		compilationUnit.accept(methodVisitor);
		for(MethodDeclaration method: methodVisitor.getMethods()) {
			IAnnotationBinding[] ab = method.resolveBinding().getAnnotations();
			for(int i=0; i<ab.length; i++) {
				if(ab[i] instanceof ParallelFor) {
					
				}
			}
		}
	}
	
	public void setCompilationUnit(CompilationUnit cu) {
		compilationUnit = cu;
	}
	
	public String getModifiedCode() {
		return modifiedCode;
	}
	
	public List<ForStatement> getForStatements() {
		return forStatements;
	}
}
