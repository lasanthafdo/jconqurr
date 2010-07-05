package org.eclipse.jconqurr.core.pipeline;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jconqurr.core.ast.visitors.MethodInvocationVisitor;
import org.eclipse.jconqurr.core.ast.visitors.SimpleNameVisitor;
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

public class PipelineHandler implements IPipelineHandler {
	private MethodDeclaration method;
	private List<VariableDeclarationStatement> variableDeclarationStatements = new ArrayList<VariableDeclarationStatement>();
	private String pipelineOutPut = "";
	private List<SimpleName> variables = new ArrayList<SimpleName>();
	private String pipelineMethod = "";
	private List<Statement> statementBeforePipeline = new ArrayList<Statement>();
	private String stmtBeforePipeline = "";
	private String expressionOfWhileStatement = "";
	private List<PipelineStage> pipelineStages = new ArrayList<PipelineStage>();
	private String className = "";

	public String getClassName() {
		return className;
	}

	public List<PipelineStage> getPipelineStages() {
		return pipelineStages;
	}

	private String stmtAfterPipeline = "";

	@Override
	public String getModifiedMethod(String className) {
		String innerClasses = "";
		String threadStatements = "";
		for (PipelineStage p : pipelineStages) {
			p.processStage(className);
			innerClasses += p.getThreadClass();
			threadStatements += p.getThreadDeclaration(className);

		}
		setPipelineMethod(threadStatements);
		System.out.println(pipelineMethod + innerClasses + getBarrierClass());
		return pipelineMethod + innerClasses + getBarrierClass();
	}

	public String getFields(String className) {
		String fields = "\n";
		fields += PipelineStage.barrierFields;
		fields += PipelineStage.queueFields;
		fields += "static " + className + " jq" + className + "= new "
				+ className + "();";
		fields += "static Barrier jqBarrier = jq"+ className + ".new Barrier();";
		
		return fields;
	}

	@Override
	public List<String> getTasks() {
		return null;
	}

	private void setPipelineMethod(String threads) {
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

		pipelineMethod = decl + "{" + stmtBeforePipeline + "\n" + threads
				+ barrierInvokation + stmtAfterPipeline + "}";
	}

	@SuppressWarnings("unchecked")
	@Override
	public void init() {
		VariableDeclarationVisitor vVisitor = new VariableDeclarationVisitor();
		MethodInvocationVisitor mVisitor = new MethodInvocationVisitor();
		this.method.accept(mVisitor);
		this.method.accept(vVisitor);
		this.variableDeclarationStatements = vVisitor.getVariablesDeclaraions();
		int stageCounter = 0;
		PipelineStage.barrierFields = "";
		PipelineStage.queueFields = "";
		PipelineStage.numberOfStages = 0;
		
		List<Statement> stmts = method.getBody().statements();
		List<Statement> whileStmts = null;
		for (MethodInvocation m : mVisitor.getMethods()) {
			if (m.toString().trim().startsWith("Directives.pipelineStart")) {
				int i = 0;
				while (!(stmts.get(i).toString().startsWith(m.toString()))) {
					statementBeforePipeline.add(stmts.get(i));
					stmtBeforePipeline += stmts.get(i);
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
			if (m.toString().trim().startsWith("Directives.pipelineEnd")) {
				m.getStartPosition();

				boolean detected = false;
				for (int j = 0; j < stmts.size(); j++) {
					if (detected) {
						stmtAfterPipeline += stmts.get(j);
					}
					if (stmts.get(j).toString().startsWith(m.toString())) {
						detected = true;
					}
				}
			}

			if (m.toString().trim().startsWith("Directives.pipelineStage")) {
				List<Statement> statments = getNextStageStatement(m);
				stageCounter++;
				pipelineStages.add(new PipelineStage(m, statments,
						statementBeforePipeline, expressionOfWhileStatement,
						stageCounter, this));
			}
			if (m.toString().trim().startsWith("Directives.pipelineOutput()")) {
				pipelineOutPut = getNextStatement(m).get(0).toString();
				SimpleNameVisitor visitor = new SimpleNameVisitor();
				getNextStatement(m).get(0).accept(visitor);
				addVariables(visitor.getVariables());
			}
		}
		PipelineStage.numberOfStages = pipelineStages.size();
	}

	private String getBarrierClass() {
		String classDecl = "class Barrier";
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

	@SuppressWarnings("unchecked")
	private List<ASTNode> getNextStatement(ASTNode node) {
		ASTNode parent = node.getParent().getParent();
		List<ASTNode> nextStatement = new ArrayList<ASTNode>();
		if (parent instanceof Block) {
			Block block = (Block) parent;
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

	@SuppressWarnings("unchecked")
	private List<Statement> getNextStageStatement(ASTNode node) {
		ASTNode parent = node.getParent().getParent();
		List<Statement> nextStatement = new ArrayList<Statement>();
		if (parent instanceof Block) {
			Block block = (Block) parent;
			List<Statement> statements = block.statements();
			for (int i = 0; i < statements.size(); i++) {
				if (statements.get(i).getStartPosition() == node
						.getStartPosition()
						&& (i + 1 < statements.size())) {
					while (((i + 1) < statements.size())
							&& (!statements.get(i + 1).toString().trim()
									.startsWith("Directives.pipelineStage"))) {
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
