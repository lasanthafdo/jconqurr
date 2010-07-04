package org.eclipse.jconqurr.core;

import org.eclipse.jdt.core.IJavaProject;

public interface IProjectHandler {
	/**
	 * Sets the source project that is to be converted. All classes implementing
	 * this interface will have to keep a reference for the source project
	 * passed in through this method. Otherwise working on that project through
	 * the provided {@link IProjectHandler#processSourceProjectInfo()} and
	 * {@link IProjectHandler#convertToParallel()} methods will be near
	 * impossible.
	 * 
	 * @param srcProj
	 *            the source project that is to be analyzed for conversion
	 */
	public void setSourceProject(IJavaProject srcProj);

	/**
	 * Converts the project to parallel. Analyzes the original project and makes
	 * copy that will have parallel optimized code segments at identified
	 * markers. This copy will be kept separately and no changes are done to the
	 * original project.
	 * <p>
	 * Please note that {@link IProjectHandler#setSourceProject(IJavaProject)}
	 * must be called before calling this method.
	 * {@link IProjectHandler#getParallelProject()} can be called to get a
	 * reference to the created paralell project.
	 * <p>
	 * Throws a null pointer exception if the source project has not been set or
	 * is null.
	 * 
	 * @throws NullPointerException
	 */
	public void convertToParallel() throws NullPointerException;

	/**
	 * Returns the already created parallel project if the successful is
	 * conversion or null if conversion has not yet being performed or was
	 * unsuccessful. Please note that calls to
	 * {@link IProjectHandler#setSourceProject(IJavaProject)} and
	 * {@link IProjectHandler#convertToParallel()} must be preceded before any
	 * call to this method, for this method call to be meaningful.
	 * 
	 * @return the converted parallel project or {@code null} if no converted
	 *         project exists.
	 */
	public IJavaProject getParallelProject();
}
