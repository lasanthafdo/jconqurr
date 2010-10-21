package org.eclipse.jconqurr.core;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;

public interface ICompilationUnitHandler {
	/**
	 * Sets the compilation unit to be processed
	 * @param cu
	 * 		the compilation unit to be analyzed for the conversion process
	 * @param targetProject
	 * 		the target project that will have the converted compilation unit
	 */
	public void setCompilationUnit(ICompilationUnit cu, IJavaProject targetProject);
	
	/**
	 * Converts the currently set compilation unit to parallel executable code.
	 * Throws a {@code NullPointerException} if no compilation unit is set for
	 * analysis or if the set compilation unit is null
	 * @throws NullPointerException
	 * 		thrown if compilation unit is null
	 * @throws JavaModelException
	 * 		thrown if the java model is inconsistent or an exception occurs when
	 * 		accessing resources
	 */
	public void convertToParallel() throws NullPointerException, JavaModelException;
	
	/**
	 * Returns the converted parallel executable compilation unit or null if
	 * conversion has not yet being performed. Please note that this method call
	 * must be preceded by calls to {@link ICompilationUnitHandler#setCompilationUnit(ICompilationUnit)}
	 *  and {@code ICompilationUnitHandler#convertToParallel()} for this method invocation
	 * to be meaningful.
	 * @return
	 * 		the converted compilation unit or null if not yet converted
	 */
	public ICompilationUnit getConvertedCompilationUnit();
}
