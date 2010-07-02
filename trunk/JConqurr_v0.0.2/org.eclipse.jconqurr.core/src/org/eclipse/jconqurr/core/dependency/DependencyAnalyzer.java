package org.eclipse.jconqurr.core.dependency;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jconqurr.core.ast.visitors.BlockVisitor;
import org.eclipse.jconqurr.core.ast.visitors.ForLoopVisitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;

public class DependencyAnalyzer implements IDependencyAnalyzer {
	ICompilationUnit cu;
	Map<Statement, Statement> dependency = new HashMap<Statement, Statement>();
	Map<MethodDeclaration, List<Statement>> statements = new HashMap<MethodDeclaration, List<Statement>>();

	@Override
	public void filterStatements(MethodDeclaration method) {
		
		BlockVisitor blockVisitor = new BlockVisitor();
		method.accept(blockVisitor);
		List<Block> blocks = blockVisitor.getBlocks();
		List<Statement> statementList = new ArrayList<Statement>();
		for (Block b : blocks) {
			List stmts = b.statements();
			for (int i = 0; i < stmts.size(); i++) {
				if(((Statement) stmts.get(i)).getNodeType()==Statement.FOR_STATEMENT){
					ForStatement s=(ForStatement)((Statement) stmts.get(i));
					ForLoopVisitor forStatmentVisitor=new ForLoopVisitor();
					s.getBody().accept(forStatmentVisitor);
					List<ForStatement> inerForStatements=forStatmentVisitor.getForLoops();
				}
				statementList.add((Statement) stmts.get(i));
			}
		}
		statements.put(method, statementList);
		// List stmts=method.getBody().statements();
		printStatements();
	}

	public void setCompilationUnit(ICompilationUnit unit) {
		this.cu = unit;
	}

	private void printStatements() {
		// System.out.println(statements);
		Set<MethodDeclaration> key = statements.keySet();
		for (MethodDeclaration m : key) {
			List<Statement> statementList = statements.get(m);
			for (Statement s : statementList) {
				//System.out.println(s.toString());
			}
		}
	}

}
