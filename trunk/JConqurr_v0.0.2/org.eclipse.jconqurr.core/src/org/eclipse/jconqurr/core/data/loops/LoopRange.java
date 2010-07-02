package org.eclipse.jconqurr.core.data.loops;

/**
 * Simple class that keeps the range to perform the loop on
 *
 * @author lasantha
 *
 */
public class LoopRange {
	private int start, end;
	
	public int getStart() {
		return start;
	}
	public void setStart(int start) {
		this.start = start;
	}
	public int getEnd() {
		return end;
	}
	public void setEnd(int end) {
		this.end = end;
	}
	
	public int getRangeDifference() {
		return (start-end)>0?(start-end):(end-start);
	}

}
