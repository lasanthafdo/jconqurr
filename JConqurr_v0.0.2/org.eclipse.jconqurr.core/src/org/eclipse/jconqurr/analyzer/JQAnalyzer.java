package org.eclipse.jconqurr.analyzer;

import org.eclipse.jconqurr.core.ast.visitors.MethodVisitor;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public abstract class JQAnalyzer {
	protected CompilationUnit unit;
	protected AST rootAST;
	protected ASTNode rootSrcNode;
	
	public JQAnalyzer(CompilationUnit cu) {
		this.unit = cu;
		this.rootSrcNode = cu.getRoot();
		this.rootAST = cu.getAST();
	}

	public ASTNode getRootSrcNode() {
		return rootSrcNode;
	}

	public void setRootSrcNode(ASTNode rootSrcNode) {
		this.rootSrcNode = rootSrcNode;
	}
	
	public CompilationUnit getCompilationUnit() {
		return unit;
	}
	
	public AST getRootAST() {
		return rootAST;
	}
	
	protected MethodDeclaration getMethodDeclaration(IMethodBinding binding,boolean searchOnlyCurrentClass) {
		if(!searchOnlyCurrentClass)
			throw new IllegalArgumentException("Searching outside of current class not yet supported.");
		// Search for the method inside the class of the current compilation unit
		TypeDeclaration classDecl = (TypeDeclaration)this.unit.types().get(0);
		MethodVisitor methodVisitor = new MethodVisitor();
		classDecl.accept(methodVisitor);
		MethodDeclaration candidateMd = null;
		for(MethodDeclaration md: methodVisitor.getMethods()) {
			if(md.resolveBinding().equals(binding)) {
				candidateMd = md;
				break;
			}
		}
		
		return candidateMd;
	}
	
	public abstract void insertDirectives();
}
