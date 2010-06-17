package org.eclipse.jconqurr.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

public class JConqurrNature implements IProjectNature {
	public static final String JCONQ_NATURE = "org.eclipse.jconqurr.parallelnature";
	
	private IProject project;

	@Override
	public void configure() throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deconfigure() throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public IProject getProject() {
		// TODO Auto-generated method stub
		return project;
	}

	@Override
	public void setProject(IProject project) {
		// TODO Auto-generated method stub
		this.project = project;
	}

}
