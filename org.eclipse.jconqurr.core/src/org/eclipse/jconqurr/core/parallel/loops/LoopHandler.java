package org.eclipse.jconqurr.core.parallel.loops;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 
 * @author Lasantha Fernando
 *
 */
public class LoopHandler implements Runnable {
	private ExecutorService executor;
	private LoopRange range;
	private IForLoopTask loopTaskBody;
	private int currentBoundary, nThreads;
	
	/**
	 * Constructs a LoopHandler that sets the start and end parameters to the range and sets
	 * the number of threads. The constructor will also initialize the loop range, set IForLoopTask loopBody as
	 * null and created a fixed thread pool with the given number of threads
	 * 
	 * @param start
	 * 		the start value of the loop range
	 * @param end
	 * 		the end value of the loop range
	 * @param numThreads
	 * 		number of threads to create for the thread pool
	 */
	public LoopHandler(int start,int end,int numThreads) {
		this.range = new LoopRange();
		this.range.setStart(start);
		this.range.setEnd(end);
		this.nThreads = numThreads;
		currentBoundary = this.range.getStart();
		loopTaskBody = null;
		executor = Executors.newFixedThreadPool(nThreads);
	}
	
	/**
	 * 
	 * @param body
	 */
	public void setLoopBody(IForLoopTask body) {
		loopTaskBody = body;
	}
	
	public IForLoopTask getLoopBody() {
		return loopTaskBody;
	}
	
	private synchronized LoopRange getTaskRange() {
		if (currentBoundary >= range.getEnd())
			return null;
		LoopRange result = new LoopRange();
		result.setStart(currentBoundary);
		currentBoundary += range.getRangeDifference()/nThreads;
		result.setEnd((currentBoundary<range.getEnd())?currentBoundary:range.getEnd());
		return result;
	}
	
	private void processLoop() {
		LoopRange currentTaskRange;
		while((currentTaskRange = this.getTaskRange())!=null) {
			LoopTask loopTask = new LoopTask(currentTaskRange.getStart(),currentTaskRange.getEnd(),loopTaskBody);
			executor.execute(new Thread(loopTask));
		}
		executor.shutdown();
		while(!executor.isTerminated()) {}
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		processLoop();
	}
}
