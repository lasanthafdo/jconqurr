package org.eclipse.jconqurr.core.test.loops;


import org.eclipse.jconqurr.core.CompilationUnitFilter;
import org.eclipse.jconqurr.core.data.loops.LoopHandler;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestLoops {
	private static LoopHandler lHandler;
	private static CompilationUnit cu;
	private static CompilationUnitFilter cuFilter;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		lHandler = new LoopHandler();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testLooping() {
		lHandler.setCompilationUnitInfo(cu, cuFilter);
	}
}
