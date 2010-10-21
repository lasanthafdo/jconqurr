package org.eclipse.jconqurr.core.data.loops;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jconqurr.core.CompilationUnitFilter;
import org.eclipse.jconqurr.core.ICompilationUnitFilter;
import org.eclipse.jconqurr.core.ast.visitors.ForLoopVisitor;
import org.eclipse.jconqurr.core.ast.visitors.ModifiedSimpleNameVisitor;
import org.eclipse.jconqurr.core.ast.visitors.TypeDeclarationVisitor;
import org.eclipse.jconqurr.core.ast.visitors.VariableDeclarationFragmentVisitor;
import org.eclipse.jconqurr.util.INodeAssist;
import org.eclipse.jconqurr.util.NodeAssist;
import org.eclipse.jconqurr.util.StringUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
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
		INodeAssist nodeAssist = new NodeAssist();
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
			// create the necessary method invocation parameters and send them
			// as a list
			NumberLiteral nThreads = ast.newNumberLiteral(String
					.valueOf(numThreads));
			List<Expression> params = new ArrayList<Expression>();
			params.add(nThreads);
			// Get the help of a NodeAssist object to get standard assignment
			// expression
			Assignment assgnExpression = nodeAssist.getStandardAssignment(ast,
					"executorService", "ExecutorService", "newFixedThreadPool",
					"Executors", params);
			Statement assgnStmt = ast.newExpressionStatement(assgnExpression);
			methodBlock.statements().add(0, assgnStmt);

			ForLoopVisitor flVisitor = new ForLoopVisitor();
			methodDecl.accept(flVisitor);
			String innerClassName = "JQ"
					+ StringUtils.toProperCase(methodDecl.getName().toString())
					+ "LoopRunnable";
			String mcInstanceName = typeDecl.getName().toString().substring(0,
					1).toLowerCase()
					+ typeDecl.getName().toString().substring(1) + "Obj";

			ClassInstanceCreation mainClassInstantiation = ast
					.newClassInstanceCreation();
			mainClassInstantiation.setType(ast.newSimpleType(ast
					.newName(typeDecl.getName().toString())));
			VariableDeclarationFragment mcInstanceFragment = ast
					.newVariableDeclarationFragment();
			mcInstanceFragment.setInitializer(mainClassInstantiation);
			mcInstanceFragment.setName(ast.newSimpleName(mcInstanceName));
			VariableDeclarationStatement mcInstanceDecl = ast
					.newVariableDeclarationStatement(mcInstanceFragment);
			mcInstanceDecl.setType(ast.newSimpleType(ast.newName(typeDecl
					.getName().toString())));
			methodBlock.statements().add(1, mcInstanceDecl);

			ArrayCreation runnableArray = ast.newArrayCreation();
			ArrayType arrayType = ast.newArrayType(ast.newSimpleType(ast
					.newSimpleName(innerClassName)), 1);
			runnableArray.setType(arrayType);
			runnableArray.dimensions().add(
					ast.newNumberLiteral(String.valueOf(numThreads)));
			VariableDeclarationFragment runnableArrayVdf = ast
					.newVariableDeclarationFragment();
			runnableArrayVdf.setInitializer(runnableArray);
			runnableArrayVdf.setName(ast.newSimpleName("runnableObj"));

			VariableDeclarationStatement runnableArrayDecl = ast
					.newVariableDeclarationStatement(runnableArrayVdf);
			runnableArrayDecl.setType(ast.newArrayType(ast.newQualifiedType(ast
					.newSimpleType(ast.newSimpleName(typeDecl.getName()
							.toString())), ast.newSimpleName(innerClassName)),
					1));
			methodBlock.statements().add(2, runnableArrayDecl);

			for (ForStatement forStmt : flVisitor.getForLoops()) {
				int forStmtIndex = methodBlock.statements().indexOf(forStmt);
				Statement stmtAboveFor = (Statement) methodBlock.statements()
						.get(forStmtIndex - 1);
				if (stmtAboveFor.getNodeType() == ASTNode.EXPRESSION_STATEMENT
						&& ((ExpressionStatement) stmtAboveFor).getExpression()
								.getNodeType() == ASTNode.METHOD_INVOCATION) {

					MethodInvocation candidateDirectiveStmt = (MethodInvocation) ((ExpressionStatement) stmtAboveFor)
							.getExpression();

					if (candidateDirectiveStmt.getExpression().toString()
							.equals("Directives")
							&& candidateDirectiveStmt.getName().getIdentifier()
									.equals("forLoop")) {
						// TODO:name should be unique for each 'for' loop
						TypeDeclaration threadClass = getRunnableInnerClassFor(
								ast, innerClassName, forStmt);
						bodyDeclList.add(threadClass);
						cleanUpCode(forStmt, stmtAboveFor);
					} // end if(MethodInvocation is 'Directives.forLoop')
				} // end if(Statement is a MethodInvocation)
			} // end of inner for loop (for..each ForStatement in
				// flVisitor.getForLoops())

			ForStatement threadInitForLoop = nodeAssist
					.getStandardForStatement(ast, 0, numThreads, 1);
			ClassInstanceCreation runnableInstantiation = ast
					.newClassInstanceCreation();
			runnableInstantiation.setType(ast.newSimpleType(ast
					.newName(innerClassName)));
			runnableInstantiation.setExpression(ast
					.newSimpleName(mcInstanceName));
			// these two arguments must be of the form (i*partitionSize + originalLowerBound,
			// and Math.min(((i+1)*partitionSize + originalLowerBound),originalUpperBound)
			
			int partitionSize = (int)Math.ceil((originalUpperBound-originalLowerBound)/numThreads);
			InfixExpression lbArgMult = ast.newInfixExpression();
			lbArgMult.setLeftOperand(ast.newSimpleName("i"));
			lbArgMult.setRightOperand(ast.newNumberLiteral(String.valueOf(partitionSize)));
			lbArgMult.setOperator(InfixExpression.Operator.TIMES);
			InfixExpression lbArgPlus = ast.newInfixExpression();
			lbArgPlus.setLeftOperand(lbArgMult);
			lbArgPlus.setRightOperand(ast.newNumberLiteral(String.valueOf(originalLowerBound)));
			lbArgPlus.setOperator(InfixExpression.Operator.PLUS);

			InfixExpression ubArgInnerPlus = ast.newInfixExpression();
			ubArgInnerPlus.setLeftOperand(ast.newSimpleName("i"));
			ubArgInnerPlus.setRightOperand(ast.newNumberLiteral(String.valueOf(1)));
			ubArgInnerPlus.setOperator(InfixExpression.Operator.PLUS);
			ParenthesizedExpression parenthesizedPlusExpr = ast.newParenthesizedExpression();
			parenthesizedPlusExpr.setExpression(ubArgInnerPlus);
			InfixExpression ubArgMult = ast.newInfixExpression();
			ubArgMult.setLeftOperand(parenthesizedPlusExpr);
			ubArgMult.setRightOperand(ast.newNumberLiteral(String.valueOf(partitionSize)));
			ubArgMult.setOperator(InfixExpression.Operator.TIMES);
			InfixExpression ubArgOuterPlus = ast.newInfixExpression();
			ubArgOuterPlus.setLeftOperand(ubArgMult);
			ubArgOuterPlus.setRightOperand(ast.newNumberLiteral(String.valueOf(originalLowerBound)));
			ubArgOuterPlus.setOperator(InfixExpression.Operator.PLUS);
			
			MethodInvocation miMathMin = ast.newMethodInvocation();
			miMathMin.setName(ast.newSimpleName("min"));
			miMathMin.setExpression(ast.newSimpleName("Math"));
			miMathMin.arguments().add(ubArgOuterPlus);
			miMathMin.arguments().add(ast.newNumberLiteral(String.valueOf(originalUpperBound)));
			
			runnableInstantiation.arguments().add(lbArgPlus);
			runnableInstantiation.arguments().add(miMathMin);

			Assignment newRunnableObjCreation = ast.newAssignment();
			ArrayAccess runnableArrayAccess = ast.newArrayAccess();
			runnableArrayAccess.setArray(ast.newSimpleName("runnableObj"));
			runnableArrayAccess.setIndex(ast.newSimpleName("i"));
			newRunnableObjCreation.setLeftHandSide(runnableArrayAccess);
			newRunnableObjCreation.setRightHandSide(runnableInstantiation);
			newRunnableObjCreation.setOperator(Assignment.Operator.ASSIGN);
			
			MethodInvocation miSubmitToExecutor = ast.newMethodInvocation();
			miSubmitToExecutor.setExpression(ast.newSimpleName("executorService"));
			miSubmitToExecutor.setName(ast.newSimpleName("submit"));
			ArrayAccess runnableObjArg = ast.newArrayAccess();
			runnableObjArg.setArray(ast.newSimpleName("runnableObj"));
			runnableObjArg.setIndex(ast.newSimpleName("i"));
			miSubmitToExecutor.arguments().add(runnableObjArg);

			Block threadInitForBlock = ast.newBlock();
			threadInitForBlock.statements().add(
					ast.newExpressionStatement(newRunnableObjCreation));
			threadInitForBlock.statements().add(ast.newExpressionStatement(miSubmitToExecutor));
			threadInitForLoop.setBody(threadInitForBlock);
			methodBlock.statements().add(threadInitForLoop);
			
			MethodInvocation miShutdownExecutor = ast.newMethodInvocation();
			miShutdownExecutor.setExpression(ast.newSimpleName("executorService"));
			miShutdownExecutor.setName(ast.newSimpleName("shutdown"));
			methodBlock.statements().add(ast.newExpressionStatement(miShutdownExecutor));
		} // end of outer for loop (for...each MethodDeclaration in
			// cuFilter.getAnnotatedMethods())

	}

	@SuppressWarnings("unchecked")
	private TypeDeclaration getRunnableInnerClassFor(AST ast, String className,
			ForStatement forStmt) {
		// declare the needed AST types
		SimpleType runnableInterface = ast.newSimpleType(ast
				.newSimpleName("Runnable"));
		INodeAssist nodeAssist = new NodeAssist();
		TypeDeclaration innerClassType = ast.newTypeDeclaration();
		innerClassType.setName(ast.newSimpleName(className));
		innerClassType.superInterfaceTypes().add(runnableInterface);
		List<IExtendedModifier> innerClassModifiers = innerClassType
				.modifiers();
		innerClassModifiers.add(ast
				.newModifier(ModifierKeyword.PRIVATE_KEYWORD));

		// add the required field declarations
		nodeAssist.addFieldDeclarationTo(innerClassType, ast, "upper", "int",
				"private");
		nodeAssist.addFieldDeclarationTo(innerClassType, ast, "lower", "int",
				"private");

		// new for statement is created while the body is just copied
		ForStatement newForStmt = ast.newForStatement();
		newForStmt.setBody((Statement) ASTNode.copySubtree(ast, forStmt
				.getBody()));

		List<Expression> initializerList = forStmt.initializers();
		VariableDeclarationFragmentVisitor vdfVisitor = new VariableDeclarationFragmentVisitor();
		ModifiedSimpleNameVisitor snVisitor = new ModifiedSimpleNameVisitor();

		// get info about initializers
		for (Expression expr : initializerList) {
			expr.accept(vdfVisitor);
		}
		for (VariableDeclarationFragment vdf : vdfVisitor
				.getVariableDeclarationFragments()) {
			vdf.accept(snVisitor);
		}

		// get info about condition expression
		Expression conditionExpr = forStmt.getExpression();

		// deal with infix expression type conditions
		if (conditionExpr.getNodeType() == ASTNode.INFIX_EXPRESSION) {
			InfixExpression infixExpr = (InfixExpression) conditionExpr;
			// check if there is variable in initializer list that is same as
			// that used in condition, if so...
			if (infixExpr.getLeftOperand().getNodeType() == ASTNode.SIMPLE_NAME
					&& snVisitor.getIdentifiers().contains(
							((SimpleName) infixExpr.getLeftOperand())
									.getIdentifier())) {
				if (infixExpr.getRightOperand().getNodeType() == ASTNode.NUMBER_LITERAL) {
					NumberLiteral uppperBoundLiteral = (NumberLiteral) infixExpr
							.getRightOperand();
					this.originalUpperBound = Integer
							.parseInt(uppperBoundLiteral.getToken());
				}
				// set the right operand to 'upper' instead of a number literal
				// (lots of assumptions done here that must be fixed)
				InfixExpression newInfixExpr = ast.newInfixExpression();
				newInfixExpr.setOperator(infixExpr.getOperator());
				newInfixExpr.setLeftOperand((Expression) ASTNode.copySubtree(
						ast, infixExpr.getLeftOperand()));
				newInfixExpr.setRightOperand(ast.newSimpleName("upper"));

				newForStmt.setExpression(newInfixExpr);

				// get the corresponding variable at the initializer
				SimpleName varNameObjAtInit = null;
				for (SimpleName sName : snVisitor.getSimpleNames()) {
					if (sName.getIdentifier().equals(
							((SimpleName) infixExpr.getLeftOperand())
									.getIdentifier())) {
						varNameObjAtInit = sName;
					}
				}
				// set the initialization of the variable to 'lower'
				if (varNameObjAtInit.getParent().getNodeType() == ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
					VariableDeclarationFragment vdfOfInitExpr = (VariableDeclarationFragment) varNameObjAtInit
							.getParent();
					VariableDeclarationExpression vdeOfInit = (VariableDeclarationExpression) vdfOfInitExpr
							.getParent();
					VariableDeclarationFragment newVdfOfInit = ast
							.newVariableDeclarationFragment();

					if (vdfOfInitExpr.getInitializer().getNodeType() == ASTNode.NUMBER_LITERAL) {
						NumberLiteral lowerBoundLiteral = (NumberLiteral) vdfOfInitExpr
								.getInitializer();
						this.originalLowerBound = Integer
								.parseInt(lowerBoundLiteral.getToken());
					}

					newVdfOfInit.setInitializer(ast.newSimpleName("lower"));
					newVdfOfInit.setName((SimpleName) ASTNode.copySubtree(ast,
							vdfOfInitExpr.getName()));
					VariableDeclarationExpression newVdeOfInit = ast
							.newVariableDeclarationExpression(newVdfOfInit);
					newVdeOfInit.setType((Type) ASTNode.copySubtree(ast,
							vdeOfInit.getType()));
					newForStmt.initializers().add(newVdeOfInit);
				}

				for (Expression updaterExpr : (List<Expression>) forStmt
						.updaters()) {
					newForStmt.updaters().add(
							(Expression) ASTNode.copySubtree(ast, updaterExpr));
				}
			}
		}

		// setup the method statements and other work that needs to be done
		// before creating the method
		List<Statement> runMethodStmts = new ArrayList<Statement>();
		runMethodStmts.add(newForStmt);
		List<IExtendedModifier> runMethodModifiers = new ArrayList<IExtendedModifier>();
		runMethodModifiers.add(ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));
		// now call the INodeAssist.getMethodDeclarationFor to get the method
		// declaration prim & proper
		MethodDeclaration runMethod = nodeAssist.getMethodDeclarationFor(ast,
				"run", (Type) ast.newPrimitiveType(PrimitiveType.VOID),
				runMethodStmts, runMethodModifiers, null);
		// finally add the run method to the inner class
		innerClassType.bodyDeclarations().add(runMethod);

		// setup the constructor that takes 2 int parameters
		List<Statement> constructorStmts = new ArrayList<Statement>();
		List<IExtendedModifier> constructorModifiers = new ArrayList<IExtendedModifier>();
		List<SingleVariableDeclaration> constructorParams = new ArrayList<SingleVariableDeclaration>();
		constructorModifiers.add(ast
				.newModifier(ModifierKeyword.PRIVATE_KEYWORD));
		// set the parameter list correctly
		SingleVariableDeclaration svdLower = ast.newSingleVariableDeclaration();
		SingleVariableDeclaration svdUpper = ast.newSingleVariableDeclaration();
		svdLower.setName(ast.newSimpleName("lower"));
		svdLower.setType(ast.newPrimitiveType(PrimitiveType.INT));
		svdUpper.setName(ast.newSimpleName("upper"));
		svdUpper.setType(ast.newPrimitiveType(PrimitiveType.INT));
		constructorParams.add(svdLower);
		constructorParams.add(svdUpper);
		// setup the statements inside the constructor
		// first, the 'this.<fieldName>' type of expression needs to be set.
		FieldAccess lowerExpr = ast.newFieldAccess();
		FieldAccess upperExpr = ast.newFieldAccess();
		ThisExpression lowerThisExpr = ast.newThisExpression();
		ThisExpression upperThisExpr = ast.newThisExpression();
		lowerExpr.setExpression(lowerThisExpr);
		lowerExpr.setName(ast.newSimpleName("lower"));
		upperExpr.setExpression(upperThisExpr);
		upperExpr.setName(ast.newSimpleName("upper"));
		// now assign the values correctly
		Assignment assignLower = ast.newAssignment();
		Assignment assignUpper = ast.newAssignment();
		assignLower.setLeftHandSide(lowerExpr);
		assignLower.setRightHandSide(ast.newSimpleName("lower"));
		assignLower.setOperator(Assignment.Operator.ASSIGN);
		assignUpper.setLeftHandSide(upperExpr);
		assignUpper.setRightHandSide(ast.newSimpleName("upper"));
		assignUpper.setOperator(Assignment.Operator.ASSIGN);
		// create statements out of expressions and add to the statement list
		ExpressionStatement exprLower = ast.newExpressionStatement(assignLower);
		ExpressionStatement exprUpper = ast.newExpressionStatement(assignUpper);
		constructorStmts.add(exprLower);
		constructorStmts.add(exprUpper);
		// call nodeAssist to get the method
		MethodDeclaration constructorMethod = nodeAssist
				.getMethodDeclarationFor(ast, className, null,
						constructorStmts, constructorModifiers,
						constructorParams);
		innerClassType.bodyDeclarations().add(constructorMethod);

		return innerClassType;
	}

	private void cleanUpCode(ForStatement forStmt, Statement directiveStmt) {
		if (forStmt.getParent() != null)
			forStmt.delete();
		directiveStmt.delete();
	}
}
