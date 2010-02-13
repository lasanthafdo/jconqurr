package org.eclipse.jconqurr.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jconqurr.core.ast.visitors.MethodVisitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

/**
 * 
 * @author prabu
 * 
 */
public class CompilationUnitFilter implements ICompilationUnitFilter {
	private CompilationUnit compilationUnit;
	private List<MethodDeclaration> annotatedTaskMethods = new ArrayList();
	private List<MethodDeclaration> annotatedLoopMethods = new ArrayList();
	private List<MethodDeclaration> notAnnotatedMethods = new ArrayList();

	/**
	 * @see ICompilationUnitFilter#getAnnotatedParallelForMethods
	 */
	@Override
	public List<MethodDeclaration> getAnnotatedParallelForMethods() {
		// TODO Auto-generated method stub
		return annotatedLoopMethods;
	}

	/**
	 * @see ICompilationUnitFilter#getAnnotatedParallelTaskMethods()
	 */
	@Override
	public List<MethodDeclaration> getAnnotatedParallelTaskMethods() {
		// TODO Auto-generated method stub
		return annotatedTaskMethods;
	}

	/**
	 * @see ICompilationUnitFilter#getNotAnnotatedMethods()
	 */
	@Override
	public List<MethodDeclaration> getNotAnnotatedMethods() {
		// TODO Auto-generated method stub
		return notAnnotatedMethods;
	}

	/**
	 * @see ICompilationUnitFilter#setCompilationUnit(CompilationUnit)
	 */
	@Override
	public void setCompilationUnit(CompilationUnit cu) {
		// TODO Auto-generated method stub
		compilationUnit = cu;

	}

	/**
	 * @see ICompilationUnitFilter#filter()
	 */
	@Override
	public void filter() {
		if (compilationUnit == null)
			throw new NullPointerException("Compilation Unit cannot be null");
		// List<MethodDeclaration> methods = new ArrayList();
		MethodVisitor methodVisitor = new MethodVisitor();
		compilationUnit.accept(methodVisitor);
		for (MethodDeclaration method : methodVisitor.getMethods()) {
			IMethodBinding mb = method.resolveBinding();
			if (mb != null) {
				IAnnotationBinding[] ab = mb.getAnnotations();
				if (ab.length > 0) {
					for (int i = 0; i < ab.length; i++) {
						if (ab[i] != null) {
							if (ab[i].getAnnotationType().getName().trim()
									.equals("ParallelTasks")) {
								annotatedTaskMethods.add(method);
							} else if (ab[i].getAnnotationType().getName()
									.trim().equals("ParallelFor")) {
								annotatedLoopMethods.add(method);
							} else {

								notAnnotatedMethods.add(method);
							}
						}
					}
				} else
					notAnnotatedMethods.add(method);
				System.out.println("added a not annotated method");
				System.out.println(method.toString());
			}
		}

	}

}
