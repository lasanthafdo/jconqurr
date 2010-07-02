package org.eclipse.jconqurr.core.data.loops;

public class LoopTask implements Runnable {
	private LoopRange loopRange;
	private IForLoopTask loopTaskBody;
	
	public LoopTask(int start,int end, IForLoopTask body) {
		this.loopRange = new LoopRange();
		this.loopRange.setStart(start);
		this.loopRange.setEnd(end);
		this.loopTaskBody = body;
	}
	
	private void doLoopTask(LoopRange lr,IForLoopTask body) {
		for(int i=lr.getStart();i<lr.getEnd();i++) {
			body.runLoopBody(i);
		}
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		doLoopTask(loopRange, loopTaskBody);
	}

}
