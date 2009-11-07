package org.eclipse.jconqurr.core.parallel;

import java.util.List;

import org.eclipse.jconqurr.core.ast.ForLoopVisitor;
import org.eclipse.jconqurr.core.ast.MethodVisitor;
import org.eclipse.jconqurr.core.ast.StatementParser;
import org.eclipse.jconqurr.core.parallel.loops.IForLoopTask;
import org.eclipse.jconqurr.core.parallel.loops.LoopHandler;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class ForLoopModifier implements IForLoopModifier {
	private String modifiedCode;
	private CompilationUnit compilationUnit;
	private List<ForStatement> forStatements;
	private List<ForStatement> newForStatements;
	private Block modifiedBlock;
	
	public ForLoopModifier() {
		modifiedCode = "";
		compilationUnit = null;
	}
	
	@Override
	public void analyzeCode() {
		if(compilationUnit == null) throw new NullPointerException("Compilation Unit cannot be null");

		MethodVisitor methodVisitor = new MethodVisitor();
		compilationUnit.accept(methodVisitor);
		for(MethodDeclaration method: methodVisitor.getMethods()) {
			IMethodBinding mb = method.resolveBinding();
			if(mb != null) {
				IAnnotationBinding[] ab = mb.getAnnotations();
				for(int i=0; i<ab.length; i++) {
					if(ab[i] != null) {
						if(ab[i].getAnnotationType().getName().trim().equals("ParallelFor")) {
							ForLoopVisitor loopVisitor = new ForLoopVisitor();
							method.accept(loopVisitor);
							forStatements = loopVisitor.getForLoops();
						}
					}
				}
			}
		}
	}
	
	@Override
	public void setCompilationUnit(CompilationUnit cu) {
		compilationUnit = cu;
	}
	
	@Override
	public String getModifiedCode() {
		return modifiedCode;
	}
	
	@Override
	public List<ForStatement> getForStatements() {
		return forStatements;
	}

	@Override
	public Block getModifiedBlock() {
		return modifiedBlock;
	}
	
	@Override
	public CompilationUnit getCompilationUnit() {
		// TODO Auto-generated method stub
		return compilationUnit;
	}
	
	@Override
	public void modifyCode() {
		if(forStatements == null) {
			System.out.println("NULL");
			return;
		}
		for(ForStatement forStatement: forStatements) {
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
				"loopHandlerThread.start();\n";
			Block block = StatementParser.parse(code);
			modifiedBlock = block;
			System.out.println(code);
		}
	}
}
