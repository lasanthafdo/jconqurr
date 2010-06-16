package org.eclipse.jconqurr.core;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;

public interface IHandleProjectParallelism {
	
	/**
	 * convert and writes the compilation unit to the new parallel project
	 * 
	 * @param parallel
	 * @param unit
	 */
	public void convert(IJavaProject parallel, ICompilationUnit unit);

	/**
	 * Handles the project parallelism
	 * 
	 * @param parallelProject
	 * @param selectedProject
	 * @throws JavaModelException
	 */
	public void handleProject(IJavaProject parallelProject,
			IJavaProject selectedProject) throws JavaModelException;
}
