package org.eclipse.jconqurr.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//import org.eclipse.jconqurr.core.ast.visitors.AnnotationVisitor;
import org.eclipse.jconqurr.core.ast.visitors.ExpressionStatementVisitor;
//import org.eclipse.jconqurr.core.ast.visitors.LineCommentVisitor;
import org.eclipse.jconqurr.core.ast.visitors.MethodInvocationVisitor;
import org.eclipse.jconqurr.core.ast.visitors.MethodVisitor;
import org.eclipse.jconqurr.core.ast.visitors.TypeDeclarationVisitor;
//import org.eclipse.jconqurr.core.dependency.DependencyAnalyser;
//import org.eclipse.jconqurr.core.dependency.IDependencyAnalyser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
//import org.eclipse.jdt.core.dom.TypeDeclaration;
//import org.eclipse.ui.internal.themes.IThemeDescriptor;

import com.sun.org.apache.bcel.internal.classfile.InnerClass;
import com.sun.org.apache.bcel.internal.classfile.InnerClasses;

/**
 * 
 * @author prabu
 * 
 */
public class CompilationUnitFilter implements ICompilationUnitFilter {
	private CompilationUnit compilationUnit;
	private List<MethodDeclaration> annotatedTaskMethods = new ArrayList<MethodDeclaration>();
	private List<MethodDeclaration> annotatedLoopMethods = new ArrayList<MethodDeclaration>();
	private List<MethodDeclaration> listOfMethodsAndConstructors = new ArrayList<MethodDeclaration>();
	private List<MethodDeclaration> notAnnotatedMethods = new ArrayList<MethodDeclaration>();
	private List<HashMap<String, MethodDeclaration>> annotatedDivideAndConquer = new ArrayList<HashMap<String, MethodDeclaration>>();
	private List<MethodDeclaration> annotatedGPUMethods=new ArrayList<MethodDeclaration>();
	private List<MethodDeclaration> annotatedPipelineMethods=new ArrayList<MethodDeclaration>();
	private List<TypeDeclaration> otherInnerclasses = new ArrayList<TypeDeclaration>();
	private List<MethodDeclaration> annotatedSplitJoinMethods=new ArrayList<MethodDeclaration>();
	
	public List<MethodDeclaration> getAnnotatedGPUMethods(){
		return annotatedGPUMethods;
	}
	
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
		return listOfMethodsAndConstructors;
	}
	
	/**
	 * @see ICompilationUnitFilter#getOtherInnerClasses()
	 */
	@Override
	public List<TypeDeclaration> getOtherInnerClasses() {
		// TODO Auto-generated method stub
		return otherInnerclasses;
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
		MethodVisitor methodVisitor = new MethodVisitor();
		ExpressionStatementVisitor exVisitor=new ExpressionStatementVisitor();
		TypeDeclarationVisitor typeVisitor=new TypeDeclarationVisitor();
		
		compilationUnit.accept(typeVisitor);
		for(TypeDeclaration t:typeVisitor.getTypeDeclarations()){
			ITypeBinding binding=t.resolveBinding();
			if(t.isMemberTypeDeclaration()){
				otherInnerclasses.add(t);
			}			
		} 
		//System.out.println(otherInnerclasses.size());
		
		compilationUnit.accept(methodVisitor);
		//IDependencyAnalyser analyser=new DependencyAnalyser();
		
		
		for (MethodDeclaration method : methodVisitor.getMethods()) {
			//if(!method.getParent().equals(typeVisitor)){
				method.accept(exVisitor);
				//analyser.filterStatements(method);
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
								}  else if (ab[i].getAnnotationType().getName()
										.trim().equals("GPU")) {
									annotatedGPUMethods.add(method);
								} 
								 else if (ab[i].getAnnotationType().getName()
											.trim().equals("Pipeline")) {
										 annotatedPipelineMethods.add(method);
									} 
								
								 else if (ab[i].getAnnotationType().getName()
											.trim().equals("SplitJoin")) {
										 annotatedSplitJoinMethods.add(method);
									} 
								
								else if (ab[i].getAnnotationType().getName()
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
									listOfMethodsAndConstructors.add(method);
								}
							}
						}
					} else {
						listOfMethodsAndConstructors.add(method);
					}
					
					for(int i=0;i<listOfMethodsAndConstructors.size();i++){
						if(otherInnerclasses.contains(listOfMethodsAndConstructors.get(i).getParent())){
							listOfMethodsAndConstructors.remove(listOfMethodsAndConstructors.get(i));
						}
					}
					
				}
			}
			
			
		for (HashMap<String, MethodDeclaration> d : annotatedDivideAndConquer)
			listOfMethodsAndConstructors.remove(d.get("recursive"));
		

	}

	
	/*public void removeUnwantedStuff() {
		for(MethodDeclaration m:listOfMethodsAndConstructors){
			System.out.println(otherInnerclasses.size());
			if(otherInnerclasses.contains(m.getParent())){
				m.delete();
			}
		}
	}*/

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

	@Override
	public List<MethodDeclaration> getPipelineMethods() {
		return annotatedPipelineMethods;
	}
	
	@Override
	public List<MethodDeclaration> getSplitJoinMethods() {
		return annotatedSplitJoinMethods;
	}

}
