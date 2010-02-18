package org.eclipse.jconqurr.core.data;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jconqurr.core.ast.visitors.ExpressionStatementVisitor;
import org.eclipse.jconqurr.core.ast.visitors.ForLoopVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;

public class ForLoopHandler implements IForLoopHandler {
	private MethodDeclaration method;
	private List<String> tasks = new ArrayList();
	private String returnType;
	private String modifier;
	private String shedulerMethod;

	/**
	 * @see IForLoopHandler#getModifiedMethod()
	 */
	public String getModifiedMethod() {
		String taskCode = "";
		int i = 1;
		for (String s : tasks) {
			taskCode = taskCode + taskRunnerCode("task" + i, s);
			i++;
		}
		return sheduleTasks(tasks) + "\n" + taskCode;
	}

	/**
	 * @see IForLoopHandler#setTasks()
	 */
	public void setTasks() {
		ForLoopVisitor loopVisitor = new ForLoopVisitor();
		method.accept(loopVisitor);
		List<ForStatement> forStatements = loopVisitor.getForLoops();
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
		List<String> inerLoops = new ArrayList();
		for (ForStatement forLoop : forStatements) {
			if (!(forLoop.getParent() instanceof ForStatement)) {
				ExpressionStatementVisitor expressionStatementVisitor = new ExpressionStatementVisitor();
				forLoop.getExpression().accept(expressionStatementVisitor);
				conditionOprator = expressionStatementVisitor.getOprator();
				body = forLoop.getBody().toString();
				updater = forLoop.updaters().get(0).toString();
				Expression initializer = (Expression) forLoop.initializers()
						.get(0);
				String initializers[] = forLoop.initializers().get(0)
						.toString().split("=");

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
				start2 = end1 + 1;
				end2 = conditionInt;
				conditon1 = condition + conditionOprator + end1;
				conditon2 = condition + conditionOprator + end2;
				try {
					ExpressionStatementVisitor exprStmtVisitor = new ExpressionStatementVisitor();
					Block block = method.getBody();
					block.accept(exprStmtVisitor);
					for (ExpressionStatement exprStmt : exprStmtVisitor
							.getExpressionStatements()) {
					}
				} catch (Exception e) {

				}
			} else {
				String initializer = forLoop.initializers().get(0).toString();
				String expression = forLoop.getExpression().toString();
				String updaters = forLoop.updaters().get(0).toString();
				String inerForLoop = "for(" + initializer + ";" + expression
						+ ";" + updaters + ")";
				inerLoops.add(inerForLoop);
			}
		}
		String task1 = "for(" + lefthandSideAssign + "=" + start1 + ";"
				+ conditon1 + ";" + updater + ")" + body;
		String task2 = "for(" + lefthandSideAssign + "=" + start2 + ";"
				+ conditon2 + ";" + updater + ")" + body;
		tasks.add(task1);
		tasks.add(task2);

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

	/**
	 * @see IForLoopHandler#init()
	 */
	public void init() {
		returnType = method.getReturnType2().toString();
		modifier = "";
		List<IExtendedModifier> modifiers = method.modifiers();
		for (IExtendedModifier a : modifiers) {
			if (a.isModifier()) {
				modifier = modifier + " " + ((Modifier) a).toString();
			}
		}
		shedulerMethod = modifier + " " + returnType + " "
				+ method.getName().toString();
		setTasks();
	}

	/**
	 * @see IForLoopHandler#setMethod(MethodDeclaration)
	 */
	public void setMethod(MethodDeclaration method) {
		this.method = method;
	}

	/**
	 * creates the runnable code
	 * 
	 * @param task
	 * @param taskBody
	 * @return the runnable code
	 */
	private String taskRunnerCode(String task, String taskBody) {
		String taskRunnerCode = "\n" + "static class " + task
				+ " implements Runnable{" + "\n" + "public void run() {"
				+ taskBody + "}}";
		return taskRunnerCode;
	}

	/**
	 * creates the code relevant to task scheduling
	 * 
	 * @param tasks
	 * @return String
	 */
	private String sheduleTasks(List<String> tasks) {
		String exec = "static ExecutorService execData = Executors.newCachedThreadPool();"
				+ "\n";
		String sheduleTasksMethod = "\n" + shedulerMethod + "(){" + "try{"
				+ "\n" + "Future<?>[] future = (Future<?>[]) new Future["
				+ tasks.size() + "];" + "\n";
		String futureSubmit = "";
		for (int i = 0; i < tasks.size(); i++) {
			futureSubmit = futureSubmit + "future[" + i
					+ "]=execData.submit(new" + " task" + (i + 1) + "());"
					+ "\n";
		}
		String futureGet = "for(int i=0;i<" + tasks.size() + ";i++)" + "\n"
				+ "future[i].get();" + "\n" + "}" + "\n"
				+ "catch (Exception e) {e.printStackTrace();}}";
		return exec + sheduleTasksMethod + futureSubmit + futureGet;
	}

}