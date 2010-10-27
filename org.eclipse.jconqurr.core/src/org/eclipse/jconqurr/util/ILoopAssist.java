package org.eclipse.jconqurr.util;

import org.eclipse.jconqurr.core.data.loops.ILoopHandler;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public interface ILoopAssist extends INodeAssist {
	/**
	 * 
	 * @param ast
	 * @param className
	 * @param forStmt
	 * @param handler
	 * @return
	 */
	public TypeDeclaration getRunnableInnerClassFor(AST ast, String className,
			ForStatement forStmt,ILoopHandler handler);
	
	public Block getGenericThreadInitializationBlock(AST ast, ForStatement threadInitForLoop, String innerClassName,
			String instanceName, int originalLB, int originalUB, int numThreads);
}
