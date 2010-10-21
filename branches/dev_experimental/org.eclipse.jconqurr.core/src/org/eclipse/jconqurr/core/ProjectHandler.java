package org.eclipse.jconqurr.core;

import org.eclipse.jconqurr.core.build.IProjectBuilder;
import org.eclipse.jconqurr.core.build.ProjectBuilder;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;

public class ProjectHandler implements IProjectHandler {
	private IJavaProject originalProject;
	private IJavaProject parallelProject;

	@Override
	public void convertToParallel() throws NullPointerException, JavaModelException {
		if(originalProject == null)
			throw new NullPointerException();
		IProjectBuilder builder = ProjectBuilder.getInstance();
		parallelProject = builder.createParallelProject(originalProject);
		IPackageFragment[] fragments = originalProject.getPackageFragments();
		for (IPackageFragment fragment : fragments) {
			if (fragment.getKind() == IPackageFragmentRoot.K_SOURCE) {
				ICompilationUnitHandler cuHandler = new CompilationUnitHandler();
				for (ICompilationUnit unit : fragment.getCompilationUnits()) {
					cuHandler.setCompilationUnit(unit, parallelProject);
					cuHandler.convertToParallel();
				}
			}
		}
	}

	@Override
	public IJavaProject getParallelProject() {
		return parallelProject;
	}

	@Override
	public void setSourceProject(IJavaProject srcProj) {
		this.originalProject = srcProj;
	}
}
