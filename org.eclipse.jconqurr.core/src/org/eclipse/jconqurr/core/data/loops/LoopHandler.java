package org.eclipse.jconqurr.core.data.loops;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jconqurr.core.CompilationUnitFilter;
import org.eclipse.jconqurr.core.ICompilationUnitFilter;
import org.eclipse.jconqurr.core.ast.visitors.BlockVisitor;
import org.eclipse.jconqurr.core.ast.visitors.ForLoopVisitor;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;

/**
 * 
 * @author Lasantha Fernando
 * 
 */
public class LoopHandler implements ILoopHandler {
	private CompilationUnit cu;
	private ICompilationUnitFilter cuFilter;
	private int numThreads;

	public LoopHandler() {
		cu = null;
		cuFilter = null;
		numThreads = 4;
	}

	public LoopHandler(CompilationUnit cu, CompilationUnitFilter cuFilter) {
		this.cu = cu;
		this.cuFilter = cuFilter;
	}

	@Override
	public void setCompilationUnitInfo(CompilationUnit cu,
			ICompilationUnitFilter cuFilter) throws NullPointerException {
		this.cu = cu;
		this.cuFilter = cuFilter;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void processCompilationUnit() {
		AST ast = cu.getAST();
		// add the import declaration for concurrent library of java.util
		ImportDeclaration concurrentImports = ast.newImportDeclaration();
		concurrentImports.setName(ast.newName(new String[] { "java", "util",
				"concurrent", "*" }));
		cu.imports().add(concurrentImports);
		BlockVisitor blockVisitor = new BlockVisitor();
		Block unitBlock = blockVisitor.getBlocks().get(0);
		
		// for each method declaration in the compilation unit
		for (MethodDeclaration methodDecl : cuFilter
				.getAnnotatedParallelForMethods()) {
			Block methodBlock = methodDecl.getBody();
			// create the necessary method invocation parameters and send them
			// as a list
			NumberLiteral nThreads = ast.newNumberLiteral(String
					.valueOf(numThreads));
			List<Expression> params = new ArrayList<Expression>();
			params.add(nThreads);
			// call the private method to get a standard assignment expression
			Assignment assgnExpression = getStandardAssignment(ast,
					"executorService", "ExecutorService", "newFixedThreadPool",
					"", params);
			methodBlock.statements().add(0,
					ast.newExpressionStatement(assgnExpression));
			ForLoopVisitor flVisitor = new ForLoopVisitor();
			methodDecl.accept(flVisitor);
			for(ForStatement forStmt: flVisitor.getForLoops()) {
				TypeDeclarationStatement threadClass = getRunnableInnerClassFor(ast, "JQ" + methodDecl.getName() + "LoopRunnable", forStmt);
				unitBlock.statements().add(threadClass);
			}
		} // end of for loop
	}

	@SuppressWarnings("unchecked")
	private TypeDeclarationStatement getRunnableInnerClassFor(AST ast, String className, ForStatement forStmt) {
		SimpleType runnableInterface = ast.newSimpleType(ast.newSimpleName("Runnable"));
		TypeDeclaration innerClassType = ast.newTypeDeclaration();
		innerClassType.setName(ast.newSimpleName(className));
		innerClassType.superInterfaceTypes().add(runnableInterface);
		List<IExtendedModifier> innerClassModifiers = innerClassType.modifiers();
		innerClassModifiers.add(ast.newModifier(ModifierKeyword.PRIVATE_KEYWORD));
		TypeDeclarationStatement loopBodyInnerClass = ast.newTypeDeclarationStatement(innerClassType);
		
		return loopBodyInnerClass;
	}
	@SuppressWarnings("unchecked")
	private Assignment getStandardAssignment(AST ast, String vdfVarName,
			String vdeVarType, String miName, String miExpr,
			List<Expression> params) {
		// set the assignment details correctly
		Assignment assgnExpr = ast.newAssignment();
		assgnExpr.setOperator(Assignment.Operator.ASSIGN);
		VariableDeclarationFragment vdfVarNamePart = ast
				.newVariableDeclarationFragment();
		vdfVarNamePart.setName(ast.newSimpleName(vdfVarName));
		VariableDeclarationExpression vde = ast
				.newVariableDeclarationExpression(vdfVarNamePart);
		vde.setType(ast.newSimpleType(ast.newSimpleName(vdeVarType)));
		MethodInvocation mi = ast.newMethodInvocation();
		mi.setExpression(ast.newSimpleName(miExpr));
		mi.setName(ast.newSimpleName(miName));
		for (Expression expr : params)
			mi.arguments().add(expr);
		assgnExpr.setLeftHandSide(vde);
		assgnExpr.setRightHandSide(mi);
		// return the assignment
		return assgnExpr;
	}
}
