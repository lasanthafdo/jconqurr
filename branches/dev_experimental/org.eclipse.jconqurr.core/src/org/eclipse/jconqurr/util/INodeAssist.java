package org.eclipse.jconqurr.util;

import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * 
 * @author lasantha
 * 
 */
public interface INodeAssist {
	/**
	 * 
	 * @param ast
	 * @param methodName
	 * @param returnType
	 * @param statementList
	 * @param modifiers
	 * @param params
	 * @return
	 */
	public MethodDeclaration getMethodDeclarationFor(AST ast,
			String methodName, Type returnType, List<Statement> statementList,
			List<IExtendedModifier> modifiers,
			List<SingleVariableDeclaration> params);

	/**
	 * 
	 * @param ast
	 * @param vdfVarName
	 * @param vdeVarType
	 * @param miName
	 * @param miExpr
	 * @param params
	 * @return
	 */
	public Assignment getStandardAssignment(AST ast, String vdfVarName,
			String vdeVarType, String miName, String miExpr,
			List<Expression> params);

	/**
	 * 
	 * @param classType
	 * @param ast
	 * @param vdfVarName
	 * @param fieldVarType
	 */
	public void addFieldDeclarationTo(TypeDeclaration classType, AST ast,
			String vdfVarName, String fieldVarType, String accessModifier);
	
	/**
	 * 
	 * @param ast
	 * @param lowerbound
	 * @param upperbound
	 * @param incrementSize
	 * @return
	 */
	public ForStatement getStandardForStatement(AST ast, int lowerbound,int upperbound,int incrementSize);

}
