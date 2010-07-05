package org.eclipse.jconqurr.core.pipeline;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public class PiplineMethod {
	private MethodDeclaration method;
	private List<VariableDeclarationStatement> variableDeclarations=new ArrayList<VariableDeclarationStatement>();
	
	
	
	public void setMethodDeclaration(MethodDeclaration method){
		this.method=method;
	}
	
	@SuppressWarnings("unchecked")
	public void filterStatements(){
		List<Statement> stmts=method.getBody().statements();
		for(Statement stm:stmts){
			if(stm instanceof VariableDeclarationStatement){
				variableDeclarations.add((VariableDeclarationStatement)stm);
			}
		}
	}
}
