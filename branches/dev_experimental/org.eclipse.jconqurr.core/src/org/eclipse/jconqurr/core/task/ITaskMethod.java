package org.eclipse.jconqurr.core.task;

import java.util.List;

import org.eclipse.jdt.core.dom.MethodDeclaration;

public interface ITaskMethod {

	/**
	 * Returns the modified method as a string. Note that this string would have to be inserted
	 * to a proper compilation unit and compiled before integrating the converted project.
	 * @return
	 * 	returns the modified method as a string.
	 */
	public String getModifiedMethod();

	/**
	 * Gets the list of tasks
	 * @return
	 *  A list of tasks returned as {@link List} of type {@link String}
	 */
	public List<String> getTasks();

	/**
	 * Sets the method that needs to be converted to parallel.
	 * @param method
	 * 	the method to be parallelized passed as  {@link MethodDeclaration} object
	 */
	public void setMethod(MethodDeclaration method);

	/**
	 * Initializes the TaskMethod object for processing.
	 */
	public void init();

}
