package org.eclipse.jconqurr.util;

import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;

public class NodeAssist implements INodeAssist {

	@SuppressWarnings("unchecked")
	@Override
	public MethodDeclaration getMethodDeclarationFor(AST ast,
			String methodName, Type returnType, List<Statement> statementList,
			List<IExtendedModifier> modifiers,
			List<SingleVariableDeclaration> params) {
		// set up the initialization for method declaration
		MethodDeclaration runMethod = ast.newMethodDeclaration();
		if(returnType == null) {
			runMethod.setConstructor(true);
		}
		else {
			runMethod.setReturnType2(ast.newPrimitiveType(PrimitiveType.VOID));			
		}
		runMethod.setName(ast.newSimpleName(methodName));
		runMethod.modifiers().add(
				(IExtendedModifier) ast
						.newModifier(ModifierKeyword.PUBLIC_KEYWORD));
		
		// set up the method body
		Block newMethodBody = ast.newBlock();
		for (int i = statementList.size() - 1; i >= 0; i--) {
			Statement stmt = statementList.get(i);
			if(stmt.getParent() != null)
				stmt.delete();
			newMethodBody.statements().add(stmt);
		}
		runMethod.setBody(newMethodBody);
		
		//if there are any parameters, add them
		if (params != null) {
			runMethod.parameters().addAll(params);
		}
		
		return runMethod;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Assignment getStandardAssignment(AST ast, String vdfVarName,
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
	
	@SuppressWarnings("unchecked")
	@Override
	public void addFieldDeclarationTo(TypeDeclaration classType, AST ast, String vdfVarName, String fieldVarType, String accessModifier) {
		VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
		fragment.setName(ast.newSimpleName(vdfVarName));
		FieldDeclaration classField = ast.newFieldDeclaration(fragment);
		PrimitiveType varType = ast.newPrimitiveType(PrimitiveType.toCode(fieldVarType));
		classField.setType(varType);
		IExtendedModifier modifier = ast.newModifier(ModifierKeyword.toKeyword(accessModifier));
		classField.modifiers().add(modifier);
		
		classType.bodyDeclarations().add(classField);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public ForStatement getStandardForStatement(AST ast, int lowerbound,
			int upperbound, int incrementSize) {
		ForStatement forStmt = ast.newForStatement();

		// set up the initializer
		VariableDeclarationFragment vdf = ast.newVariableDeclarationFragment();
		vdf.setName(ast.newSimpleName("i"));
		vdf.setInitializer(ast.newNumberLiteral(Integer.toString(lowerbound)));
		VariableDeclarationExpression initExprVde = ast.newVariableDeclarationExpression(vdf);
		initExprVde.setType(ast.newPrimitiveType(PrimitiveType.INT));
		forStmt.initializers().add(initExprVde);
		
		// set up the condition
		InfixExpression conditionExpr = ast.newInfixExpression();
		conditionExpr.setOperator(InfixExpression.Operator.LESS);
		conditionExpr.setLeftOperand(ast.newSimpleName("i"));
		conditionExpr.setRightOperand(ast.newNumberLiteral(Integer.toString(upperbound)));
		forStmt.setExpression(conditionExpr);
		
		// set up the increment
		if(incrementSize == 1) {
			PostfixExpression pfExpr = ast.newPostfixExpression();
			pfExpr.setOperand(ast.newSimpleName("i"));
			pfExpr.setOperator(PostfixExpression.Operator.INCREMENT);
			forStmt.updaters().add(pfExpr);
		}
		else {
			Assignment assgn = ast.newAssignment();
			assgn.setOperator(Assignment.Operator.PLUS_ASSIGN);
			assgn.setLeftHandSide(ast.newSimpleName("i"));
			assgn.setRightHandSide(ast.newSimpleName(Integer.toString(incrementSize)));
			forStmt.updaters().add(assgn);
		}
		
		return forStmt;
	}
}
