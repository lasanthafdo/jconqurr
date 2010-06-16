package org.eclipse.jconqurr.core.divideandconquer;

import org.eclipse.jdt.core.dom.MethodDeclaration;

public interface IDivideAndConquerHandler {

	/**
	 * 
	 * @return modified code.
	 */
	public String getModifiedMethods();

	/**
	 * sets the recursive method
	 * 
	 * @param method
	 */
	public void setRecursiveMethod(MethodDeclaration method);

	/**
	 * sets the recursive method caller
	 * 
	 * @param method
	 */
	public void setRecursionCaller(MethodDeclaration method);

	/**
	 * initialize the parameters for the parallelism
	 */
	public void init();
}
