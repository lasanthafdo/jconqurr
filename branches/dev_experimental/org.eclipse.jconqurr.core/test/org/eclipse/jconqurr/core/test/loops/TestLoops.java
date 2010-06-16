package org.eclipse.jconqurr.core.test.loops;


import org.eclipse.jconqurr.core.data.loops.IForLoopTask;
import org.eclipse.jconqurr.core.data.loops.LoopHandler;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestLoops {
	private static LoopHandler lHandler;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		lHandler = new LoopHandler(0, 100000, 4);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testLooping() {
		IForLoopTask flTask = new IForLoopTask() {
			
			@Override
			public void runLoopBody(int loopVar) {
				// TODO Auto-generated method stub
				double calc = Math.sqrt((loopVar + 1) * 233 / 231.223333);
				System.out.println("Value is :" + calc);
			}
		};
		lHandler.setLoopBody(flTask);
		assertEquals(flTask, lHandler.getLoopBody());
		
		Thread loopStartThread = new Thread(lHandler);
		loopStartThread.start();
	}
}
