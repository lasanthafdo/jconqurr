package org.eclipse.jconqurr.core.parallel.loops;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 
 * @author lasantha
 *
 */
public class LoopHandler implements Runnable {
	protected class LoopRange {
		public int rangeStart, rangeEnd;
	}

	protected int loopStart, loopEnd, loopCurrent, numThreads;
	protected LoopRange range;
	
	public LoopHandler(int start, int end, int nThreads) {
		loopStart = loopCurrent = start;
		loopEnd = end;
		numThreads = nThreads;
	}
	
	protected synchronized LoopRange getLoopRange() {
		if(loopCurrent >= loopEnd)
			return null;
		range.rangeStart = loopCurrent;
		loopCurrent += (loopEnd-loopStart)/(numThreads+1);
		range.rangeEnd = loopCurrent < loopEnd ? loopCurrent: loopEnd;
		LoopRange range = new LoopRange();
		return range;
	}
	
	public void doLoopRange(int start, int end) {
		
	}
	
	public void processLoop() {
		ExecutorService executor = Executors.newFixedThreadPool(numThreads);
		for (int i = 0; i < numThreads; i++) {

			executor.execute(new Thread(this));
		}
		executor.shutdown();
	}
	
	@Override
	public void run() {
		while(getLoopRange() != null) {
			LoopRange loopRange = getLoopRange();
			doLoopRange(loopRange.rangeStart, loopRange.rangeEnd);
		}
	}


}
