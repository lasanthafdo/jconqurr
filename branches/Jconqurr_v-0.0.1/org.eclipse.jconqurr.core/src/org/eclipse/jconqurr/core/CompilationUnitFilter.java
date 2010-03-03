package org.eclipse.jconqurr.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jconqurr.core.ast.visitors.AnnotationVisitor;
import org.eclipse.jconqurr.core.ast.visitors.MethodInvocationVisitor;
import org.eclipse.jconqurr.core.ast.visitors.MethodVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

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
	private List<HashMap<String, MethodDeclaration>> annotatedDivideAndConquer = new ArrayList<HashMap<String, MethodDeclaration>>();

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
		List<MethodDeclaration> sortMethods = new ArrayList();
		MethodVisitor methodVisitor = new MethodVisitor();
		AnnotationVisitor annotationVisitor = new AnnotationVisitor();
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
							} else if (ab[i].getAnnotationType().getName()
									.trim().startsWith("DivideAndConquer")) {
								HashMap<String, MethodDeclaration> divideAndConquer = new HashMap<String, MethodDeclaration>();
								divideAndConquer.put("caller", method);
								MethodInvocationVisitor methodInvocationVisitor = new MethodInvocationVisitor();
								method.getBody()
										.accept(methodInvocationVisitor);
								for (MethodInvocation mi : methodInvocationVisitor
										.getMethods()) {
									IMethodBinding binding = mi
											.resolveMethodBinding();
									MethodDeclaration recursiveMethod = fillterRecursiveMethod(
											binding.getName(), binding
													.getParameterTypes());
									if (recursiveMethod != null) {
										divideAndConquer.put("recursive",
												recursiveMethod);
									}
								}
								annotatedDivideAndConquer.add(divideAndConquer);
							} else {
								notAnnotatedMethods.add(method);
							}
						}
					}
				} else {
					notAnnotatedMethods.add(method);
				}
			}
		}
		for (HashMap<String, MethodDeclaration> d : annotatedDivideAndConquer)
			notAnnotatedMethods.remove(d.get("recursive"));

	}

	private MethodDeclaration fillterRecursiveMethod(String name,
			ITypeBinding[] typeBinding) {
		MethodVisitor methodVisitor = new MethodVisitor();
		compilationUnit.accept(methodVisitor);
		for (MethodDeclaration method : methodVisitor.getMethods()) {
			IMethodBinding mb = method.resolveBinding();
			if (mb.getName().equals(name)
					&& mb.getParameterTypes().length == typeBinding.length) {
				MethodInvocationVisitor methodInvocationVisitor = new MethodInvocationVisitor();
				method.getBody().accept(methodInvocationVisitor);
				for (MethodInvocation mi : methodInvocationVisitor.getMethods()) {
					IMethodBinding binding = mi.resolveMethodBinding();
					if (binding.getName().equals(name)
							&& binding.getParameterTypes().length == typeBinding.length) {
						return method;
					}
				}
			}
		}
		return null;
	}

	@Override
	public List<HashMap<String, MethodDeclaration>> getAnnotatedDivideAndConquer() {
		return annotatedDivideAndConquer;
	}

}
