package org.eclipse.jconqurr.core.parallel.loops;

/**
 * Defined to capture the loop body of the for loop.
 * The method runLoopBody() should implement the loop body 
 * @author lasantha
 *
 */
public interface IForLoopTask {
	/**
	 * This method should contain only the loop body of the for loop for
	 * proper execution
	 */
	public void runLoopBody(int loopVar);
}
