package org.eclipse.jconqurr.core.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jconqurr.core.ast.visitors.ExpressionStatementVisitor;
import org.eclipse.jconqurr.core.ast.visitors.MethodInvocationVisitor;
import org.eclipse.jconqurr.core.ast.visitors.SimpleNameVisitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public class ForLoop {
	private Block parent;
	private int position;
	private ForStatement forLoop;
	private List<Statement> statementsBeforeForLoop;
	private List<Statement> statementsAfterForLoop;
	private List<String> tasks = new ArrayList<String>();
	private Set<String> identifiers = new HashSet<String>();
	private List<VariableDeclarationStatement> variableDeclarationsBeforLoop = new ArrayList<VariableDeclarationStatement>();
	private List<ASTNode> parentsOfParents = new ArrayList<ASTNode>();
	private List<VariableDeclarationStatement> variableDeclarationStatements = new ArrayList<VariableDeclarationStatement>();
	private List<SimpleName> variables = new ArrayList<SimpleName>();
	private List<VariableDeclarationStatement> threadClassFields = new ArrayList<VariableDeclarationStatement>();
	public static int lockNo = 1;

	public List<SimpleName> getVariables() {
		return variables;
	}

	public List<Statement> getStatementBeforForLoop() {
		return statementsBeforeForLoop;
	}

	public List<Statement> getStatementsAfterForLoop() {
		return statementsAfterForLoop;
	}

	public ForLoop(Block parent, int position) {
		ASTNode root = parent;
		if (parent.getParent() instanceof MethodDeclaration) {
		} else {
			while (!(root.getParent() instanceof MethodDeclaration)) {
				parentsOfParents.add(root.getParent());
				root = root.getParent();
			}
		}
		this.parent = parent;
		this.position = position;
		// System.out.println(parentsOfParents.size());
	}

	public List<String> getTasks() {
		return tasks;
	}

	public List<VariableDeclarationStatement> getThreadClassFields() {
		return threadClassFields;
	}

	@SuppressWarnings("unchecked")
	public void init() {
		if (this.parent != null) {
			List<Statement> statements = parent.statements();
			statementsBeforeForLoop = statements.subList(0, position - 1);
			for (int i = 0; i < statementsBeforeForLoop.size(); i++) {

				if (statementsBeforeForLoop.get(i).toString().trim()
						.startsWith("Directives.forLoop()")) {
					statementsBeforeForLoop = statementsBeforeForLoop.subList(
							i + 2, statementsBeforeForLoop.size());
					statementsBeforeForLoop = statementsBeforeForLoop.subList(
							0, 0);
				}
			}
			for (int i = 0; i < statementsBeforeForLoop.size(); i++) {
				if (statementsBeforeForLoop.get(i) instanceof VariableDeclarationStatement) {
					variableDeclarationStatements
							.add((VariableDeclarationStatement) statementsBeforeForLoop
									.get(i));
				}
			}
			statementsAfterForLoop = statements.subList(position + 1,
					statements.size());
			for (int i = 0; i < statementsAfterForLoop.size(); i++) {
				if (statementsAfterForLoop.get(i).toString().trim().startsWith(
						"Directives.forLoop()")) {

					statementsAfterForLoop = statementsAfterForLoop.subList(0,
							i);
				}
			}
			if (statements.get(position) instanceof ForStatement) {
				forLoop = (ForStatement) statements.get(position);
				SimpleNameVisitor visitor = new SimpleNameVisitor();
				forLoop.accept(visitor);
				for (String s : visitor.getIdentifiers()) {
					identifiers.add(s);
				}
			}
			addTasks();
			if (statementsBeforeForLoop.size() > 0) {
				setVariableDeclarationsBeforeLoop(statementsBeforeForLoop);
			}
			SimpleNameVisitor nameVisitor = new SimpleNameVisitor();
			forLoop.accept(nameVisitor);
			// System.out.println(nameVisitor.getVariables());
			for (SimpleName name : nameVisitor.getVariables()) {
				if (variables.size() == 0) {
					variables.add(name);
				} else {
					boolean contains = false;
					for (SimpleName d : variables) {
						if (d.toString().equals(name.toString())) {
							contains = true;
						}
					}
					if (!contains) {
						variables.add(name);
					}
				}
			}
		}
		filterDependentVariables();
	}

	@SuppressWarnings("unchecked")
	private void filterDependentVariables() {
		for (VariableDeclarationStatement vdecl : variableDeclarationStatements) {
			vdecl.fragments();

			for (VariableDeclarationFragment fragment : (List<VariableDeclarationFragment>) vdecl
					.fragments()) {
				IVariableBinding vbinding = (IVariableBinding) fragment
						.resolveBinding();

				vbinding.getName();
				String variableName = vbinding.getName();
				for (SimpleName name : variables) {
					if (variableName.equals(name.toString())) {
						System.out.println("equals");
						threadClassFields.add(vdecl);
					}
				}
			}
		}
	}

	private void setVariableDeclarationsBeforeLoop(List<Statement> statements) {
		for (Statement stmt : statements) {
			if (stmt instanceof VariableDeclarationStatement) {
				VariableDeclarationStatement exp = (VariableDeclarationStatement) stmt;
				variableDeclarationsBeforLoop.add(exp);
			}
		}
	}

	/**
	 * Checks the string is convertible to a integer
	 * 
	 * @param s
	 * @return boolean
	 */
	private boolean isParsableToInt(String s) {
		try {
			Integer.parseInt(s);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public void addTasks() {
		int start = 0;
		int start1 = 0;
		int start2 = 0;
		int end1 = 0;
		int end2 = 0;
		String lefthandSideAssign = "";
		String conditionOprator = "";
		String conditon1 = "";
		String conditon2 = "";
		String updater = "";
		String body = "";
		ExpressionStatementVisitor expressionStatementVisitor = new ExpressionStatementVisitor();
		forLoop.getExpression().accept(expressionStatementVisitor);
		conditionOprator = expressionStatementVisitor.getOprator();

		body = forLoop.getBody().toString();
		body = analyzeBody(forLoop.getBody());
		updater = forLoop.updaters().get(0).toString();
		String initializers[] = forLoop.initializers().get(0).toString().split(
				"=");
		for (int k = 0; k < initializers.length; k++) {
			if (isParsableToInt(initializers[k].trim())) {
				start = Integer.parseInt(initializers[k].trim());
				break;
			} else
				lefthandSideAssign = initializers[k];
		}
		String condition = "";
		String strExpression = forLoop.getExpression().toString();
		String regex = "[<>[<=][>=]]";
		String result[] = {};
		result = strExpression.split(regex);
		Integer conditionInt = new Integer(0);
		for (int j = 0; j < result.length; j++) {
			if (isParsableToInt(result[j].trim())) {
				conditionInt = Integer.parseInt(result[j].trim());
				break;
			} else {
				condition = condition + result[j].trim();
			}
		}
		int newCondition1 = (int) conditionInt / 2;
		start1 = start;
		end1 = newCondition1;
		start2 = end1;
		end2 = conditionInt;
		conditon1 = condition + conditionOprator + end1;
		conditon2 = condition + conditionOprator + end2;
		InfixExpression exp = (InfixExpression) forLoop.getExpression();
		String task1 = "for(" + lefthandSideAssign + "=" + start1 + ";"
				+ exp.getLeftOperand() + exp.getOperator()
				+ exp.getRightOperand() + "/2" + ";" + updater + ")" + body;
		String task2 = "for(" + lefthandSideAssign + "="
				+ exp.getRightOperand() + "/2" + ";" + exp.getLeftOperand()
				+ exp.getOperator() + exp.getRightOperand() + ";" + updater
				+ ")" + body;
		tasks.add(task1);
		tasks.add(task2);
	}

	@SuppressWarnings("unchecked")
	public String analyzeBody(Statement stmt) {
		System.out.println("substring" + stmt.toString().substring(0, 10));
		MethodInvocationVisitor visitor = new MethodInvocationVisitor();
		stmt.accept(visitor);
		visitor.getMethods();
		List<Statement> syncStatments = new ArrayList<Statement>();
		List<Statement> beforeSync = new ArrayList<Statement>();
		List<Statement> afterSync = new ArrayList<Statement>();
		List<String> forDecl = new ArrayList<String>();
		String body = "";
		for (MethodInvocation m : visitor.getMethods()) {
			if (m.toString().startsWith("Directives.shared")) {
				System.out.println("shared detected");
				System.out.print(m.getParent());
				ASTNode node = m.getParent();

				while (!(node instanceof Block)) {
					node = node.getParent();
				}
				ASTNode parent = m.getParent();
				while (!(stmt.toString().equals(parent.toString()))) {
					parent = parent.getParent();
					if (parent instanceof ForStatement) {
						String initializer = "";
						String expression = "";
						String updater = "";
						String forStatement = "";
						initializer = ((ForStatement) parent).initializers()
								.get(0).toString();
						expression = ((ForStatement) parent).getExpression()
								.toString();
						updater = ((ForStatement) parent).updaters().get(0)
								.toString();
						forStatement = "for(" + initializer + ";" + expression
								+ ";" + updater + ")";
						forDecl.add(forStatement);
						body += forStatement;
					}
				}
				if (node instanceof Block) {
					int position = 0;
					String blockbody = "";
					String sharedstmt = "";
					List<Statement> stmts = ((Block) node).statements();
					for (int i = 0; i < stmts.size(); i++) {
						if (stmts.get(i).toString().startsWith(
								"Directives.shared")) {
							position = i;
							syncStatments.add(stmts.get(i + 1));

						}
					}
					beforeSync = stmts.subList(0, position);
					if ((position + 2) != stmts.size()) {
						afterSync = stmts.subList(position + 2, stmts.size());
					}
					for (Statement t : syncStatments) {

						sharedstmt += t.toString();
					}
					sharedstmt = "synchronized(" + "lock" + lockNo + ")" + "{"
							+ sharedstmt + "}";
					lockNo++;
					for (Statement t : beforeSync) {
						System.out.println("-->" + t);
						blockbody += t;
					}
					blockbody = blockbody + sharedstmt;
					for (Statement t : afterSync) {
						System.out.println("-->" + t);
						blockbody += t;
					}

					body = body + "{" + blockbody + "}";
					System.out.println(body);
					return body;

				}
			}
		}
		return stmt.toString();
	}
}
