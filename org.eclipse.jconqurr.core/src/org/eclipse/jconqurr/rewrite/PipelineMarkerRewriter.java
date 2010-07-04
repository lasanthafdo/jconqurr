package org.eclipse.jconqurr.rewrite;

import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

public class PipelineMarkerRewriter extends SimpleRewriter {

	public PipelineMarkerRewriter(CompilationUnit cu) {
		super(cu);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Marks the start of a pipeline convertible code segment by placing the
	 * directive
	 * 
	 * <pre>
	 * @code Directives.pipelineStart();}
	 * </pre>
	 * 
	 * on top of the code segment<br>
	 * 
	 * @param rewriter
	 *            the {@link ASTRewrite} object that should be passed in
	 * @param ws
	 *            the {@link WhileStatement} that has been identified as a
	 *            candidate for pipelining
	 */
	public void markPipelineStart(ASTRewrite rewriter, WhileStatement ws) {
		// Check that all arguments are valid before proceeding with rewriting
		if (rewriter == null)
			throw new NullPointerException("Argument of type "
					+ ASTRewrite.class.getCanonicalName() + " cannot be null");
		AST ast = rewriter.getAST();
		CompilationUnit cu = this.compilationUnit;
		if (!cu.getAST().equals(ast))
			throw new IllegalArgumentException(
					"Rewriter passed in is not set to modify the CompilationUnit object\n"
							+ "referenced by the current object of type "
							+ this.getClass().getCanonicalName());
		// Change the AST as necessary
		MethodInvocation mi = ast.newMethodInvocation();
		SimpleName qualifierName = ast.newSimpleName("Directives");
		SimpleName methodName = ast.newSimpleName("pipelineStart");
		mi.setExpression(qualifierName);
		mi.setName(methodName);
		ExpressionStatement methodInvocationStatement = ast
				.newExpressionStatement(mi);
		ASTNode parentNode = ws.getParent();
		if (parentNode instanceof Block) {
			ListRewrite lrw = rewriter.getListRewrite((Block) parentNode,
					Block.STATEMENTS_PROPERTY);
			lrw.insertBefore(methodInvocationStatement, ws, null);
		} else {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Marks the end of a pipeline convertible code segment by placing the
	 * directive
	 * 
	 * <pre>
	 * @code Directives.pipelineEnd();}
	 * </pre>
	 * 
	 * after the last line of the identified code segment<br>
	 * 
	 * @param rewriter
	 *            the {@link ASTRewrite} object that should be passed in
	 * @param pipelineMethod
	 *            the {@link MethodDeclaration} that has been annotated for
	 *            pipeline parallelism
	 * @param ws
	 *            the {@link WhileStatement} that has been identified as a
	 *            candidate for pipelining
	 */
	public void markPipelineEnd(ASTRewrite rewriter, WhileStatement ws) {
		// Check that all arguments are valid before proceeding with rewriting
		ensureValidArguments(rewriter);
		// Create simple local variables for easier handling
		AST ast = rewriter.getAST();
		// Change the AST as necessary
		MethodInvocation mi = ast.newMethodInvocation();
		SimpleName qualifierName = ast.newSimpleName("Directives");
		SimpleName methodName = ast.newSimpleName("pipelineEnd");
		mi.setExpression(qualifierName);
		mi.setName(methodName);
		ExpressionStatement methodInvocationStatement = ast
				.newExpressionStatement(mi);
		ASTNode parentNode = ws.getParent();
		if (parentNode instanceof Block) {
			ListRewrite lrw = rewriter.getListRewrite((Block) parentNode,
					Block.STATEMENTS_PROPERTY);
			lrw.insertAfter(methodInvocationStatement, ws, null);
		} else {
			throw new UnsupportedOperationException();
		}
	}

	@SuppressWarnings("unchecked")
	public void markPipelineStage(ASTRewrite rewriter, WhileStatement ws, Statement stmt, List<String> argList) {
		// Check that all arguments are valid before proceeding with rewriting
		ensureValidArguments(rewriter);
		// Create simple local variables for easier handling
		AST ast = rewriter.getAST();
		// Change the AST as necessary
		MethodInvocation mi = ast.newMethodInvocation();
		SimpleName qualifierName = ast.newSimpleName("Directives");
		SimpleName methodName = ast.newSimpleName("pipelineStage");
		for(String arg: argList) {
			StringLiteral strLiteralArg = ast.newStringLiteral();
			strLiteralArg.setLiteralValue(arg);
			mi.arguments().add(strLiteralArg);
		}
		mi.setExpression(qualifierName);
		mi.setName(methodName);
		ExpressionStatement methodInvocationStatement = ast
				.newExpressionStatement(mi);
		ListRewrite lrw = rewriter.getListRewrite((Block)ws.getBody(),
					Block.STATEMENTS_PROPERTY);
		lrw.insertBefore(methodInvocationStatement, stmt, null);
	}
	
	public void setImportDeclarations(ASTRewrite rewriter) {
		// Check validity of arguments. This method call will throw exceptions
		// on invalid arguments
		ensureValidArguments(rewriter);
		// Do the changes
		CompilationUnit cu = this.compilationUnit;
		AST ast = rewriter.getAST();
		ImportDeclaration id = ast.newImportDeclaration();
		id.setName(ast.newName(new String[] { "org", "eclipse", "jconqurr",
				"directives", "Directives" }));
		ListRewrite declRewriter = rewriter.getListRewrite(cu,
				CompilationUnit.IMPORTS_PROPERTY);
		declRewriter.insertLast(id, null);
	}

	public void ensureValidArguments(ASTRewrite rewriter) {
		if (rewriter == null)
			throw new NullPointerException("Argument of type "
					+ ASTRewrite.class.getCanonicalName() + " cannot be null");
		AST ast = rewriter.getAST();
		CompilationUnit cu = this.compilationUnit;
		if (!cu.getAST().equals(ast))
			throw new IllegalArgumentException(
					"Rewriter passed in is not set to modify the CompilationUnit object\n"
							+ "referenced by the current object of type "
							+ this.getClass().getCanonicalName());

	}
}
