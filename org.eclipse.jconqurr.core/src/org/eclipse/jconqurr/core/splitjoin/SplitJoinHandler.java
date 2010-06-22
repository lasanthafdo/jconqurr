package org.eclipse.jconqurr.core.splitjoin;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jconqurr.core.ast.visitors.MethodInvocationVisitor;
import org.eclipse.jconqurr.core.ast.visitors.SimpleNameVisitor; //import org.eclipse.jconqurr.core.ast.visitors.StatementVisitor;
import org.eclipse.jconqurr.core.ast.visitors.VariableDeclarationVisitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

public class SplitJoinHandler implements ISplitJoinHandler {
	private MethodDeclaration method;
	private List<VariableDeclarationStatement> variableDeclarationStatements = new ArrayList<VariableDeclarationStatement>();
	//private String pipelineOutPut = "";
	private List<SimpleName> variables = new ArrayList<SimpleName>();
	private String splitJoinMethod = "";
	private List<Statement> statementsBeforeSplitJoin = new ArrayList<Statement>();
	private String stmtBeforeSplitJoin = "";
	private String expressionOfWhileStatement = "";
	private List<SplitJoinStage> splitJoinStages = new ArrayList<SplitJoinStage>();
	private String className = "";
	List<String> queueFieldsList = new ArrayList<String>();
	private String queueFields="";
	List<String> commonVariablesList = new ArrayList<String>();
	private String commonVariables = "";
	List<String> barrierFieldsList = new ArrayList<String>();
	private String barrierFields = "";
	protected String jqSplitCounter = "";

	public String getClassName() {
		return className;
	}

	public List<SplitJoinStage> getSplitJoinStages() {
		return splitJoinStages;
	}

	private String stmtAfterSplitJoin = "";

	@Override
	public String getModifiedMethod(String className) {	
		
		this.className = className;
		String innerClasses = "";
		String threadStatements = "";
		for (SplitJoinStage p : splitJoinStages) {
			p.processStage(className);
			innerClasses += p.getThreadClass();
			threadStatements += p.getThreadDeclaration(className);

		}
		for (SplitJoinStage p : splitJoinStages) {
			p.setOtherVariables();
		}
		setSplitJoinMethod(threadStatements);
		
		return getFields(className) + splitJoinMethod + innerClasses + getBarrierClass();
	}

	public String getFields(String className) {	
		this.commonVariables = "";
		this.barrierFields = "";
		this.queueFields = "";
		
		setQueueFields();
		setCommonVariables();
		setBarrierFields();
		
		this.commonVariablesList.clear();
		this.barrierFieldsList.clear();
		this.queueFieldsList.clear();
		
		String fields = "\n";
		fields += this.commonVariables;
		fields += this.barrierFields;
		fields += this.queueFields;
		
		
		return fields;
	}
	
	private void setCommonVariables(){
		this.commonVariables += "\n";
		for(String s:commonVariablesList){
			commonVariables += s.toString() + "\n";
		}
	}
	
	private void setBarrierFields(){
		this.barrierFields += "\n";
		for(String s:barrierFieldsList){
			this.barrierFields += s.toString() + "\n";
		}
	}
	
	private void setQueueFields(){
		for(String s:queueFieldsList){
			queueFields += s.toString() + "\n";
		}
	} 

	@Override
	public List<String> getTasks() {
		return null;
	}

	private void setSplitJoinMethod(String threads) {
		IMethodBinding binding = method.resolveBinding();
		String decl = "";
		if (method.parameters().size() > 0) {
			String arguments = "";
			for (int i = 0; i < method.parameters().size(); i++) {
				String d = method.parameters().get(i).toString();
				if (i == (method.parameters().size() - 1)) {
					arguments += d;
				} else {
					arguments += d + ",";
				}
			}
			String m = binding.getMethodDeclaration().toString().substring(0,
					binding.getMethodDeclaration().toString().indexOf("("));
			ITypeBinding ibinding[] = binding.getExceptionTypes();
			String exceptions = "";
			for (int i = 0; i < ibinding.length; i++) {
				if (ibinding.length == 1) {
					exceptions = ibinding[i].getName();
				}
				if (ibinding.length > 1) {
					if (i != (ibinding.length - 1)) {
						exceptions += ibinding[i] + ",";
					} else {
						exceptions += ibinding[i];
					}
				}
			}
			if (!exceptions.equals("")) {
				exceptions = " throws " + exceptions;
			}
			decl = m + "(" + arguments + ")" + exceptions;
		} else {
			decl = binding.getMethodDeclaration().toString();
		}

		String barrierInvokation = "jqBarrier.process();";

		splitJoinMethod = decl + "{" + stmtBeforeSplitJoin + "\n" + threads
				+ barrierInvokation + stmtAfterSplitJoin + "}";
	}

	@Override
	public void init() {
		VariableDeclarationVisitor vVisitor = new VariableDeclarationVisitor();
		MethodInvocationVisitor mVisitor = new MethodInvocationVisitor();
		this.method.accept(mVisitor);
		this.method.accept(vVisitor);
		this.variableDeclarationStatements = vVisitor.getVariablesDeclaraions();
		int stageCounter = 0;
		//SplitJoinStage.barrierFields = "";
		SplitJoinStage.numberOfStages = 0;
		
		List<Statement> stmts = method.getBody().statements();
		List<Statement> whileStmts = null;
		for (MethodInvocation m : mVisitor.getMethods()) {
			if (m.toString().trim().startsWith("Directives.pipelineStart")) {
				int i = 0;
				while (!(stmts.get(i).toString().startsWith(m.toString()))) {
					statementsBeforeSplitJoin.add(stmts.get(i));
					stmtBeforeSplitJoin += stmts.get(i);
					i++;
				}
				try {
					WhileStatement wstmt = (WhileStatement) getNextStatement(m)
							.get(0);
					expressionOfWhileStatement = wstmt.getExpression()
							.toString();
					whileStmts = ((Block) wstmt.getBody()).statements();
					System.out.println(whileStmts);

				} catch (ClassCastException e) {

				}
			}
			else if (m.toString().trim().startsWith("Directives.pipelineEnd")) {
				m.getStartPosition();

				boolean detected = false;
				for (int j = 0; j < stmts.size(); j++) {
					if (detected) {
						stmtAfterSplitJoin += stmts.get(j);
					}
					if (stmts.get(j).toString().startsWith(m.toString())) {
						detected = true;
					}
				}
			}

			else if ((m.toString().trim().startsWith("Directives.pipelineStage"))||(m.toString().trim().startsWith("Directives.pipelineSplitStage"))) {
				List<Statement> statments = getNextStageStatement(m);
				stageCounter++;
				if(m.toString().trim().startsWith("Directives.pipelineStage")){
					splitJoinStages.add(new SplitJoinStage(m, statments,
							statementsBeforeSplitJoin, expressionOfWhileStatement,
							stageCounter, this, false));
				}
				else{
					splitJoinStages.add(new SplitJoinStage(m, statments,
							statementsBeforeSplitJoin, expressionOfWhileStatement,
							stageCounter, this, true));
				}
				
			}
			
			/*else if (m.toString().trim().startsWith("Directives.pipelineSplitStage")) {
				List<Statement> statments = getNextStageStatement(m);
				stageCounter++;
				
			}*/
			/*if (m.toString().trim().startsWith("Directives.pipelineOutput()")) {
				pipelineOutPut = getNextStatement(m).get(0).toString();
				SimpleNameVisitor visitor = new SimpleNameVisitor();
				getNextStatement(m).get(0).accept(visitor);
				addVariables(visitor.getVariables());
			}*/
		}
		SplitJoinStage.numberOfStages = splitJoinStages.size();
	}

	private String getBarrierClass() {
		String classDecl = "class JqBarrier";
		String fields = "boolean isCompleted = false;";
		String processMethod = "synchronized void process() {"
				+ "while (!isCompleted) {"
				+ "try {Thread.sleep(1);} catch (InterruptedException e) {e.printStackTrace();}}}";
		String notifyMethod = "void notifyBarrier() {	isCompleted = true;	}";
		return classDecl + "{" + fields + processMethod + notifyMethod + "}";
	}

	private void addVariables(List<SimpleName> names) {
		for (SimpleName name : names) {
			if (variables.size() > 0) {
				boolean alreadyInserted = false;
				for (SimpleName variable : variables) {
					if (name.toString().equals(variable.toString())) {
						alreadyInserted = true;
					}
				}
				if (!alreadyInserted) {
					variables.add(name);
				}
			} else {
				variables.add(name);
			}
		}
	}

	private List<ASTNode> getNextStatement(ASTNode node) {
		// List<Statement> stmts = new ArrayList<Statement>();
		// StatementVisitor stmtVisitor = new StatementVisitor();
		// node.getParent().getParent().accept(stmtVisitor);
		// for(Statement stmt:stmtVisitor.getStatements()){
		// stmts.add(stmt);
		// if(stmt.toString().trim().startsWith("Directives.pipelineStage"))
		// break;
		// }

		ASTNode parent = node.getParent().getParent();
		List<ASTNode> nextStatement = new ArrayList<ASTNode>();
		ASTNode blockParent;
		if (parent instanceof Block) {
			Block block = (Block) parent;
			blockParent = block.getParent();
			List<Statement> statements = block.statements();
			for (int i = 0; i < statements.size(); i++) {
				if (statements.get(i).getStartPosition() == node
						.getStartPosition()
						&& (i + 1 < statements.size())) {
					nextStatement.add(statements.get(i + 1));
				}
			}
		}
		return nextStatement;

	}

	private List<Statement> getNextStageStatement(ASTNode node) {
		// List<Statement> stmts = new ArrayList<Statement>();
		// StatementVisitor stmtVisitor = new StatementVisitor();
		// node.getParent().getParent().accept(stmtVisitor);
		// for(Statement stmt:stmtVisitor.getStatements()){
		// stmts.add(stmt);
		// if(stmt.toString().trim().startsWith("Directives.pipelineStage"))
		// break;
		// }

		ASTNode parent = node.getParent().getParent();
		List<Statement> nextStatement = new ArrayList<Statement>();
		ASTNode blockParent;
		if (parent instanceof Block) {
			Block block = (Block) parent;
			blockParent = block.getParent();
			List<Statement> statements = block.statements();
			for (int i = 0; i < statements.size(); i++) {
				if (statements.get(i).getStartPosition() == node.getStartPosition()	&& (i + 1 < statements.size())) {
					while (((i + 1) < statements.size()) && ((!statements.get(i + 1).toString().trim().startsWith("Directives.pipelineStage"))&&(!statements.get(i + 1).toString().trim().startsWith("Directives.pipelineSplitStage")))) {
						nextStatement.add(statements.get(i + 1));
						i++;
					}

				}
			}
		}
		return nextStatement;

	}

	@Override
	public void setMethod(MethodDeclaration method) {
		this.method = method;
	}

}
