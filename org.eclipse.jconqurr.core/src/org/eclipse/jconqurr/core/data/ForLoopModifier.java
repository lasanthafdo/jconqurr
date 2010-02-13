package org.eclipse.jconqurr.core.data;

import java.util.List;

import org.eclipse.jconqurr.core.ast.visitors.ForLoopVisitor;
import org.eclipse.jconqurr.core.ast.visitors.MethodVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class ForLoopModifier implements IForLoopModifier {
	private String modifiedCode;
	private CompilationUnit compilationUnit;
	private List<ForStatement> forStatements;
	
	public ForLoopModifier() {
		modifiedCode = "";
		compilationUnit = null;
	}
	
	@Override
	public void analyzeCode() {
		if(compilationUnit == null) throw new NullPointerException("Compilation Unit cannot be null");

		MethodVisitor methodVisitor = new MethodVisitor();
		compilationUnit.accept(methodVisitor);
		for(MethodDeclaration method: methodVisitor.getMethods()) {
			IMethodBinding mb = method.resolveBinding();
			if(mb != null) {
				IAnnotationBinding[] ab = mb.getAnnotations();
				for(int i=0; i<ab.length; i++) {
					if(ab[i] != null) {
						if(ab[i].getAnnotationType().getName().trim().equals("ParallelFor")) {
							ForLoopVisitor loopVisitor = new ForLoopVisitor();
							method.accept(loopVisitor);
							forStatements = loopVisitor.getForLoops();
						}
					}
				}
			}
		}
	}
	
	@Override
	public void setCompilationUnit(CompilationUnit cu) {
		compilationUnit = cu;
	}
	
	@Override
	public String getModifiedCode() {
		return modifiedCode;
	}
	
	@Override
	public List<ForStatement> getForStatements() {
		return forStatements;
	}

	@Override
	public CompilationUnit getCompilationUnit() {
		// TODO Auto-generated method stub
		return compilationUnit;
	}
}
