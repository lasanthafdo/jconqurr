package org.eclipse.jconqurr.core.data.loops;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jconqurr.core.CompilationUnitFilter;
import org.eclipse.jconqurr.core.ICompilationUnitFilter;
import org.eclipse.jconqurr.core.ast.visitors.ForLoopVisitor;
import org.eclipse.jconqurr.core.ast.visitors.TypeDeclarationVisitor;
import org.eclipse.jconqurr.util.ILoopAssist;
import org.eclipse.jconqurr.util.LoopAssist;
import org.eclipse.jconqurr.util.StringUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

/**
 * 
 * @author Lasantha Fernando
 * 
 */
public class LoopHandler implements ILoopHandler {
	private CompilationUnit cu;
	private ICompilationUnitFilter cuFilter;
	private int numThreads;

	private int originalLowerBound;
	private int originalUpperBound;

	public LoopHandler() {
		cu = null;
		cuFilter = null;
		numThreads = 4;
		originalLowerBound = originalUpperBound = 0;
	}

	public LoopHandler(CompilationUnit cu, CompilationUnitFilter cuFilter) {
		this.cu = cu;
		this.cuFilter = cuFilter;
		numThreads = Runtime.getRuntime().availableProcessors();
	}

	@Override
	public void setCompilationUnitInfo(CompilationUnit cu,
			ICompilationUnitFilter cuFilter) throws NullPointerException {
		this.cu = cu;
		this.cuFilter = cuFilter;
	}

	// This code needs to be reviewed from top to bottom including private
	// methods that are invoked
	// within this method
	@SuppressWarnings("unchecked")
	@Override
	public void processCompilationUnit() {
		// create the service objects necessary for modification
		ILoopAssist loopAssist = new LoopAssist();
		AST ast = cu.getAST();
		TypeDeclaration typeDecl = null;
		// add the import declaration for concurrent library of java.util
		ImportDeclaration concurrentImports = ast.newImportDeclaration();
		concurrentImports.setName(ast.newName(new String[] { "java", "util",
				"concurrent" }));
		concurrentImports.setOnDemand(true);
		cu.imports().add(concurrentImports);
		TypeDeclarationVisitor tdVisitor = new TypeDeclarationVisitor();
		cu.accept(tdVisitor);
		for (TypeDeclaration td : tdVisitor.getTypeDeclarations()) {
			if (cu.getTypeRoot().getElementName().contains(
					td.getName().getIdentifier())) {
				typeDecl = td;
				break;
			}
		}
		
		List<BodyDeclaration> bodyDeclList = typeDecl.bodyDeclarations();
		// for each method declaration in the compilation unit
		for (MethodDeclaration methodDecl : cuFilter
				.getAnnotatedParallelForMethods()) {
			Block methodBlock = methodDecl.getBody();

			ForLoopVisitor flVisitor = new ForLoopVisitor();
			methodDecl.accept(flVisitor);			// visit the method and get all the for loops

			for (ForStatement forStmt : flVisitor.getForLoops()) {			// for each for loop in the current method
				int forStmtIndex = methodBlock.statements().indexOf(forStmt);
				Statement stmtAboveFor = (Statement) methodBlock.statements()
						.get(forStmtIndex - 1);
				if (stmtAboveFor.getNodeType() == ASTNode.EXPRESSION_STATEMENT
						&& ((ExpressionStatement) stmtAboveFor).getExpression()
								.getNodeType() == ASTNode.METHOD_INVOCATION) {
					MethodInvocation candidateDirectiveStmt = (MethodInvocation) ((ExpressionStatement) stmtAboveFor)
							.getExpression();		// get the statement above the for statement as a candidate for a directive

					if (candidateDirectiveStmt.getExpression().toString()
							.equals("Directives")
							&& candidateDirectiveStmt.getName().getIdentifier()
									.equals("forLoop")) {			// if it is a for loop directive
						// TODO:name should be unique for each 'for' loop
						String innerClassName = "JQ"
								+ StringUtils.toProperCase(methodDecl.getName()
										.toString()) + "LoopRunnable";
						TypeDeclaration threadClass = loopAssist
								.getRunnableInnerClassFor(ast, innerClassName,
										forStmt, this);
						bodyDeclList.add(threadClass);
						// create the necessary method invocation parameters and
						// send them as a list
						NumberLiteral nThreads = ast.newNumberLiteral(String
								.valueOf(numThreads));
						List<Expression> params = new ArrayList<Expression>();
						params.add(nThreads);
						// Get the help of a NodeAssist object to get standard
						// assignment expression
						Assignment assgnExpression = loopAssist
								.getStandardAssignment(ast, "executorService",
										"ExecutorService",
										"newFixedThreadPool", "Executors",
										params);
						Statement assgnStmt = ast
								.newExpressionStatement(assgnExpression);
						methodBlock.statements().add(forStmtIndex++, assgnStmt);

						String mcInstanceName = typeDecl.getName().toString().substring(0, 1).toLowerCase()
						+ typeDecl.getName().toString().substring(1) + "Obj";
						ClassInstanceCreation mainClassInstantiation = ast
								.newClassInstanceCreation();
						mainClassInstantiation.setType(ast.newSimpleType(ast
								.newName(typeDecl.getName().toString())));
						VariableDeclarationFragment mcInstanceFragment = ast
								.newVariableDeclarationFragment();
						mcInstanceFragment
								.setInitializer(mainClassInstantiation);
						mcInstanceFragment.setName(ast
								.newSimpleName(mcInstanceName));
						VariableDeclarationStatement mcInstanceDecl = ast
								.newVariableDeclarationStatement(mcInstanceFragment);
						mcInstanceDecl.setType(ast.newSimpleType(ast
								.newName(typeDecl.getName().toString())));
						// adds a statement of the form
						// '<Class> <ClassNameInCamel>Obj = new <Class>();'
						methodBlock.statements().add(forStmtIndex++, mcInstanceDecl);

						ArrayCreation runnableArray = ast.newArrayCreation();
						ArrayType arrayType = ast.newArrayType(ast
								.newSimpleType(ast
										.newSimpleName(innerClassName)), 1);
						runnableArray.setType(arrayType);
						runnableArray.dimensions().add(
								ast.newNumberLiteral(String.valueOf(numThreads)));
						VariableDeclarationFragment runnableArrayVdf = ast
								.newVariableDeclarationFragment();
						runnableArrayVdf.setInitializer(runnableArray);
						runnableArrayVdf.setName(ast
								.newSimpleName("runnableObj"));
						VariableDeclarationStatement runnableArrayDecl = ast
								.newVariableDeclarationStatement(runnableArrayVdf);
						runnableArrayDecl.setType(ast.newArrayType(ast
								.newQualifiedType(ast.newSimpleType(ast
										.newSimpleName(typeDecl.getName()
												.toString())), ast
										.newSimpleName(innerClassName)), 1));
						// adds a statment of the form
						// '<Class>.<InnerRunnableClass>[] runnableObj = new <InnerRunnableClass>[<noOfThreads>];' 
						methodBlock.statements().add(forStmtIndex++, runnableArrayDecl);

						ForStatement threadInitForLoop = loopAssist
								.getStandardForStatement(ast, 0, numThreads, 1);
						Block threadInitForBlock = loopAssist
								.getGenericThreadInitializationBlock(ast,
										threadInitForLoop, innerClassName,
										mcInstanceName, originalLowerBound,
										originalUpperBound, numThreads);
						threadInitForLoop.setBody(threadInitForBlock);
						// adds the new for loop at the appropriate place
						methodBlock.statements().add(forStmtIndex++, threadInitForLoop);

						MethodInvocation miShutdownExecutor = ast
								.newMethodInvocation();
						miShutdownExecutor.setExpression(ast
								.newSimpleName("executorService"));
						miShutdownExecutor.setName(ast
								.newSimpleName("shutdown"));
						// adds the statement 'executorService.shutdown();'
						methodBlock.statements().add(forStmtIndex,
								ast.newExpressionStatement(miShutdownExecutor));

						// delete the old for loop and the directive statement
						cleanUpCode(forStmt, stmtAboveFor);
					} // end if(MethodInvocation is 'Directives.forLoop')
				} // end if(Statement is a MethodInvocation)
			} // end of inner for loop (for..each ForStatement in
		} // end of outer for loop (for...each MethodDeclaration in
	}

	private void cleanUpCode(ForStatement forStmt, Statement directiveStmt) {
		if (forStmt.getParent() != null)
			forStmt.delete();
		directiveStmt.delete();
	}

	@Override
	public void setOriginalBounds(int lb, int ub) {
		this.originalLowerBound = lb;
		this.originalUpperBound = ub;
	}
}
