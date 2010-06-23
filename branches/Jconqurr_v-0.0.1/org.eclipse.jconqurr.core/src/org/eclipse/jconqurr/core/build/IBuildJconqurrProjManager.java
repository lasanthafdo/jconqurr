package org.eclipse.jconqurr.core.build;

import org.eclipse.jdt.core.IJavaProject;

public interface IBuildJconqurrProjManager {

	/**
	 * Creates the project for the parallelization
	 * 
	 * @param selectedProject
	 * @return IJavaProject
	 */
	public IJavaProject createProject(IJavaProject selectedProject);

}
