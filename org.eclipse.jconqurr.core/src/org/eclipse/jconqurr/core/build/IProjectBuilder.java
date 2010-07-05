package org.eclipse.jconqurr.core.build;

import org.eclipse.jdt.core.IJavaProject;

public interface IProjectBuilder {
	/**
	 * Creates a project with necessary class path and settings modifications for the conversion process.
	 * The project returned by this method will not be fully converted to parallel. However, library additions
	 * and other changes needed will be performed by this method to give a basic skeleton for the final converted
	 * project. 
	 * @param originalProject
	 * 		a reference to the original project that needs to be converted
	 * @return
	 * 		the parallel project that is to be used.
	 * @throws NullPointerException
	 * 		when the passed in argument for {@code IJavaProject} is null
	 */
	public IJavaProject createParallelProject(IJavaProject originalProject) throws NullPointerException;
}
