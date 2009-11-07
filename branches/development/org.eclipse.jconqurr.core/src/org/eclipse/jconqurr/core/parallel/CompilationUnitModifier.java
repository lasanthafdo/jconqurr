package org.eclipse.jconqurr.core.parallel;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jconqurr.core.ast.ForLoopVisitor;
import org.eclipse.jconqurr.core.ast.MethodVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class CompilationUnitModifier implements ICompilationUnitModifier {
	private CompilationUnit compilationUnit;
	private List<IForLoopModifier> forLoopModifiers;
	
	public CompilationUnitModifier() {
		compilationUnit = null;
		forLoopModifiers = new ArrayList<IForLoopModifier>();
	}
	
	public CompilationUnitModifier(CompilationUnit cu) {
		compilationUnit = cu;
		forLoopModifiers = new ArrayList<IForLoopModifier>();
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
							for(ForStatement forStatement: loopVisitor.getForLoops()) {
								IForLoopModifier modifier = new ForLoopModifier(forStatement);
								forLoopModifiers.add(modifier);
							}
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
	public void modifyCode() {
		for(IForLoopModifier modifier: forLoopModifiers) {
			modifier.modifyCode();
		}
	}
	
	@Override
	public CompilationUnit getCompilationUnit() {
		// TODO Auto-generated method stub
		return compilationUnit;
	}
	
	@Override
	public IForLoopModifier getForLoopModifier(int index) {
		return forLoopModifiers.get(index);
	}
}
