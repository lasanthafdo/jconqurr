package org.eclipse.jconqurr.core.data.loops;

import org.eclipse.jconqurr.core.ICompilationUnitFilter;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * 
 * @author lasantha
 *
 */
public interface ILoopHandler {
	/**
	 * Sets the compilation unit information for the loop handler.
	 * @param cu
	 * 		the compilation unit that will transformed to be parallel executable. Please
	 * 		note that this compilation unit will be modified by the method and only a suitable
	 * 		copy should be passed in if the original is not to be modified
	 * @param cuFilter
	 * 		filtered data for the compilation unit. Loop information will be extracted from this filter.
	 * @throws NullPointerException
	 * 		thrown if arguments contain any null references
	 */
	public void setCompilationUnitInfo(CompilationUnit cu, ICompilationUnitFilter cuFilter) throws NullPointerException;
	
	/**
	 * Processes the compilation unit and converts the code to parallel accordingly
	 */
	public void processCompilationUnit();
}
