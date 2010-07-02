package org.eclipse.jconqurr.core.data;

import org.eclipse.jdt.core.dom.MethodDeclaration;

public interface IForLoopHandler {

	/**
	 * 
	 * @return the modified parallel code
	 */
	public String getModifiedMethod();

	/**
	 * 
	 * set the tasks that need to be parallelize using loop parallelism
	 */
	public void setTasks();

	/**
	 * 
	 * @param method
	 *            sets the sequential method that need to be parallelize
	 */
	public void setMethod(MethodDeclaration method);

	/**
	 * initialize the properties of the method
	 */
	public void init();

}