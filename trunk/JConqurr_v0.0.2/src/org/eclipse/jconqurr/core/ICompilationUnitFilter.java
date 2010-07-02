package org.eclipse.jconqurr.core;

import java.util.HashMap;
import java.util.List;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * 
 * @author prabu
 * 
 */
public interface ICompilationUnitFilter {
	
	/**
	 * filter the annotated methods from the compilation unit
	 */
	public void filter();
	
	/**
	 * set the compilation unit for filtering
	 * @param cu
	 */
	public void setCompilationUnit(CompilationUnit cu);
	
	/**
	 * returns methods which annotated with parallelTask
	 * @return List<MethodDeclaration>
	 */
	public List<MethodDeclaration> getAnnotatedParallelTaskMethods();
	
	/**
	 * returns methods which annotated with parallelFor
	 * @return List<MethodDeclaration>
	 */
	public List<MethodDeclaration> getAnnotatedParallelForMethods();

	/**
	 * methods which is not annotated with jconqurr annotations
	 * @return  List<MethodDeclaration> 
	 */
	public List<MethodDeclaration> getNotAnnotatedMethods();
	
	/**
	 * returns the list of methods which annotated with divideAndConquer annotation
	 * @return
	 */
	public List<HashMap<String,MethodDeclaration>> getAnnotatedDivideAndConquer();

	/**
	 * returns the methods which annotated with GPU annotation
	 * @return
	 */
	public List<MethodDeclaration> getAnnotatedGPUMethods();

	public List<MethodDeclaration> getPipelineMethods();

	public List<TypeDeclaration> getOtherInnerClasses();

	//public void removeUnwantedStuff();
}
