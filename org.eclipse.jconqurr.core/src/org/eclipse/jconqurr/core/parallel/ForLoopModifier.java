package org.eclipse.jconqurr.core.parallel;

import org.eclipse.jconqurr.core.ast.StatementParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ForStatement;

public class ForLoopModifier implements IForLoopModifier {
	private String modifiedCode;
	private ForStatement forStatement;
	private Block modifiedBlock;
	
	public ForLoopModifier() {
		modifiedCode = "";
		forStatement = null;
	}
	
	public ForLoopModifier(ForStatement fs) {
		modifiedCode = "";
		forStatement = fs;
	}
	
	@Override
	public void analyzeCode() {
		
	}
	

	
	@Override
	public String getModifiedCode() {
		return modifiedCode;
	}
	
	@Override
	public ForStatement getForStatement() {
		return forStatement;
	}

	@Override
	public Block getModifiedBlock() {
		return modifiedBlock;
	}
	
	@Override
	public void modifyCode() {
		if(forStatement != null) {
			String code = "";
			String initializer = forStatement.initializers().get(0).toString();
			String initResult[] = initializer.split("[=]");
			int start = Integer.parseInt(initResult[1]);

			String expression = forStatement.getExpression().toString();
			String regex = "[<>[<=][>=]]";
			String exprResult[] = {};
			exprResult = expression.split(regex);
			int end = Integer.parseInt(exprResult[1].trim());
			String loopVar = exprResult[0].trim();
			
			code = "int start = " + start + ", end = " + end + ", nThreads = " + Runtime.getRuntime().availableProcessors() + ";\n" +
				"LoopHandler loopHandler = new LoopHandler(start, end, nThreads);\n" +
				"loopHandler.setLoopBody(new IForLoopTask() {\n" +
				"\n" +
				"@Override\n" +
				"public void runLoopBody(int " + loopVar + ")" +
				forStatement.getBody().toString() +
				"});\n" +
				"Thread loopHandlerThread = new Thread(loopHandler);\n" + 
				"loopHandlerThread.start();\n" +
				"try {\n" +
				"\tloopHandlerThread.join();\n" +
				"} catch (InterruptedException e) {\n" +
				"\te.printStackTrace();\n" +
				"}\n";			
			Block block = StatementParser.parse(code);
			modifiedCode = code;
			modifiedBlock = block;
		}
	}
}
