package org.eclipse.jconqurr.core.dependency;

import org.eclipse.jdt.core.dom.MethodDeclaration;

/**
 * 
 * @author Anonymous
 *
 */
public interface IDependencyAnalyzer {
	/**
	 * Filters the statements before analyzed
	 * @param method
	 * 	the method to be analyzed
	 */
	public void filterStatements(MethodDeclaration method);
}
