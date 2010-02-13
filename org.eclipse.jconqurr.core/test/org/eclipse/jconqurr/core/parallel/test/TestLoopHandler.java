package org.eclipse.jconqurr.core.parallel.test;

import static org.junit.Assert.*;

import org.eclipse.jconqurr.core.data.loops.IForLoopTask;
import org.eclipse.jconqurr.core.data.loops.LoopHandler;
import org.junit.Before;
import org.junit.Test;

public class TestLoopHandler {
	private static double[] calcArray = new double[50000];
	private static double[] expectedCalc = new double[50000];
	
	private static int count = 0;
	
	@Before
	public void initializeArrays() {
		for(int i=0;i<50000;i++) {
			expectedCalc[i] = Math.sqrt((i*52)/32 + 1.2/(i+3.2));
		}
	}
	
	@Test
	public void testSetLoopBody() {
		LoopHandler loopHandler = new LoopHandler(0,1000,2);
		IForLoopTask loopBody = new IForLoopTask() {
			
			@Override
			public void runLoopBody(int loopVar) {
				// TODO Auto-generated method stub
				int testCalc = loopVar*100+950;
				testCalc++;
			}
		};
		loopHandler.setLoopBody(loopBody);
		assertEquals(loopHandler.getLoopBody().getClass(),loopBody.getClass());
	}

	@Test
	public void testRun() {
		int start = 0, end = 50000, nThreads = 7;
		LoopHandler loopHandler = new LoopHandler(start, end, nThreads);
		loopHandler.setLoopBody(new IForLoopTask() {
			
			@Override
			public void runLoopBody(int loopVar) {
				// TODO Auto-generated method stub
				synchronized(this){
					count++;
				}
				double calc = Math.sqrt((loopVar*52)/32 + 1.2/(loopVar+3.2));
				calcArray[loopVar] = calc;
			}
		});
		
		for(int i=start; i<end; i++) {
			assertTrue(calcArray[i]==0.0);
		}

		Thread loopHandlerThread = new Thread(loopHandler);
		loopHandlerThread.start();		
		try {
			loopHandlerThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			System.out.println("Thread Interrupted...");
			e.printStackTrace();
		}

		for(int i=start; i<end; i++) {
			assertEquals((Double)expectedCalc[i],(Double)calcArray[i]);
		}

		System.out.println("Test Method: testRun(), Count:" + count);
	}
	
/*
	@Test
	public void testGetTaskRange() {
		LoopHandler testHandler = new LoopHandler(0, 5000, 4);
		
		// First Range
		LoopRange testRange = testHandler.getTaskRange();
		assertEquals(0, testRange.getStart());
		assertEquals(5000/4, testRange.getEnd());
		assertEquals(5000/4, testRange.getRangeDifference());
		
		//Next Range
		testRange = testHandler.getTaskRange();
		assertEquals(1250, testRange.getStart());
		assertEquals(2500, testRange.getEnd());
		assertEquals(1250, testRange.getRangeDifference());
		
		//Next Range
		testRange = testHandler.getTaskRange();
		assertEquals(2500, testRange.getStart());
		assertEquals(3750, testRange.getEnd());
		assertEquals(1250, testRange.getRangeDifference());

		//Final Range
		testRange = testHandler.getTaskRange();
		assertEquals(3750, testRange.getStart());
		assertEquals(5000, testRange.getEnd());
		assertEquals(1250, testRange.getRangeDifference());

	}
*/
}
