package org.eclipse.jconqurr.core.task;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jconqurr.core.ast.visitors.LineCommentVisitor;
import org.eclipse.jconqurr.core.ast.visitors.SimpleNameVisitor;
import org.eclipse.jconqurr.core.ast.visitors.VariableDeclarationVisitor;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public class TaskMethod implements ITaskMethod {
	private MethodDeclaration method;
	private List<String> tasks = new ArrayList();
	private List<Statement> statmentsBeforeTasks = new ArrayList();
	private String returnType;
	private String modifier;
	private String shedulerMethod;
	private List<String> codeAfterBarrier = new ArrayList();
	private static int barrierCounter = 1;
	private static int taskCounter = 1;
	private static int taskNo = 1;
	private List<VariableDeclarationStatement> variableDeclarationStatements = new ArrayList<VariableDeclarationStatement>();
	private List<VariableDeclarationStatement> threadClassFields = new ArrayList<VariableDeclarationStatement>();
	private List<SimpleName> variables = new ArrayList<SimpleName>();

	private String sheduleTasks(List<String> tasks,
			List<String> codeAfterBarrier) {
		String otherStm = "";
		for (Statement stm : statmentsBeforeTasks) {
			otherStm += stm.toString() + "\n";
		}
		String sheduleTasksMethod = "\n" + shedulerMethod + "{" + "try{"
				+ otherStm + "\n"
				+ "Future<?>[] future = (Future<?>[]) new Future["
				+ tasks.size() + "];" + "\n";

		String futureSubmit = "";
		String cyclicBarrier = "";
		if (codeAfterBarrier.size() != 0) {
			cyclicBarrier = "CyclicBarrier barrier = new CyclicBarrier("
					+ tasks.size() + "," + "new barrier" + taskCounter + "()"
					+ ");";
		}
		if (codeAfterBarrier.size() != 0) {
			for (int i = 0; i < tasks.size(); i++) {
				futureSubmit = futureSubmit + "future[" + i
						+ "]=exec.submit(new" + " task" + taskNo
						+ "(barrier));" + "\n";
				taskNo++;
			}
		} else {
			for (int i = 0; i < tasks.size(); i++) {
				futureSubmit = futureSubmit + "future[" + i
						+ "]=exec.submit(new" + " task" + taskNo + "());"
						+ "\n";
				taskNo++;
			}
		}
		String futureGet = "for(int i=0;i<" + tasks.size() + ";i++)" + "\n"
				+ "future[i].get();" + "\n" + "}" + "\n"
				+ "catch (Exception e) {e.printStackTrace();}}";
		return sheduleTasksMethod + cyclicBarrier + futureSubmit + futureGet;
	}

	public String getModifiedMethod() {
		String taskCode = "";
		String barrierCode = "";
		for (String s : tasks) {
			taskCode = taskCode + taskRunnerCode("task" + taskCounter, s);
			taskCounter++;
		}
		if (codeAfterBarrier.size() != 0) {
			for (String s : codeAfterBarrier) {
				barrierCode = barrierCode
						+ codeAfterBarrier("barrier" + taskCounter, s);
				barrierCounter++;
			}
		}
		return sheduleTasks(tasks, codeAfterBarrier) + "\n" + taskCode
				+ barrierCode;
	}

	public List<String> getTasks() {
		int directiveStart = -1;
		List<Statement> stmts = method.getBody().statements();
		VariableDeclarationVisitor variableDeclarationVisitor = new VariableDeclarationVisitor();
		method.accept(variableDeclarationVisitor);
		variableDeclarationVisitor.getVariablesDeclaraions();
		List<Integer> taskStartPositions = new ArrayList<Integer>();
		List<Integer> taskEndPositions = new ArrayList<Integer>();
		List<Integer> barrierPositions = new ArrayList<Integer>();
		for (Statement s : stmts) {
			if (s instanceof VariableDeclarationStatement) {
				variableDeclarationStatements
						.add((VariableDeclarationStatement) s);
			}
			if (s.toString().startsWith("Directives.startTask();")) {
				directiveStart = stmts.indexOf(s);
				taskStartPositions.add(stmts.indexOf(s));
			}
			if (s.toString().startsWith("Directives.endTask();")) {
				taskEndPositions.add(stmts.indexOf(s));
			}
			if (s.toString().startsWith("Directives.barrier();")) {
				barrierPositions.add(stmts.indexOf(s));
			}
		}
		if (taskStartPositions.size() > 0) {
			List<Statement> subStatmentBeforeTask = stmts.subList(0,
					(taskStartPositions.get(0)));
			for (Statement stm : subStatmentBeforeTask) {
				statmentsBeforeTasks.add(stm);
			}
			for (int m = 0; m < taskStartPositions.size(); m++) {
				String task = "";
				List<Statement> subStatement;
				List<Statement> otherStatement;
				subStatement = stmts.subList((taskStartPositions.get(m) + 1),
						(taskEndPositions.get(m)));
				if (m + 1 < taskStartPositions.size()) {
					otherStatement = stmts.subList(taskEndPositions.get(m) + 1,
							taskStartPositions.get(m + 1));
					for (Statement stm : otherStatement) {
						statmentsBeforeTasks.add(stm);
					}
				}

				if (subStatement.size() > 0) {
					for (Statement s : subStatement) {
						task = task + s.toString();
						SimpleNameVisitor nameVisitor = new SimpleNameVisitor();
						s.accept(nameVisitor);
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
					tasks.add(task);
				}
			}

			if (barrierPositions.size() > 0) {
				String rest = "";
				List<Statement> subStatement = stmts.subList((barrierPositions
						.get(0) + 1), stmts.size());
				for (Statement s : subStatement) {
					rest = rest + s.toString();
				}
				codeAfterBarrier.add(rest);
			}
		}
		filterDependentVariables();
		return null;
	}

	public void setMethod(MethodDeclaration method) {
		this.method = method;
	}

	private String taskRunnerCode(String task, String taskBody) {
		String taskRunnerCode = "";
		String barrierCaller = "try {" + "\n" + "barrier.await();" + "\n" + "}"
				+ "\n" + " catch (InterruptedException e) {" + "\n"
				+ "// TODO Auto-generated catch block" + "\n"
				+ "e.printStackTrace();" + "\n" + "}" + "\n"
				+ " catch (BrokenBarrierException e) {" + "\n"
				+ "e.printStackTrace();" + "\n" + "}";
		if (codeAfterBarrier.size() != 0) {
			taskRunnerCode = "\n" + "public class " + task
					+ " implements Runnable{" + "\n" + "CyclicBarrier barrier;"
					+ "\n" + task + "(CyclicBarrier barrier){" + "\n"
					+ "this.barrier = barrier;" + "\n" + "}"
					+ "public void run() {" + taskBody + barrierCaller + "}}";
		} else {
			taskRunnerCode = "\n" + "public class " + task
					+ " implements Runnable{" + "\n" + "\n"
					+ "public void run() {" + taskBody + "}}";
		}
		return taskRunnerCode;
	}

	private String codeAfterBarrier(String barrier, String barrierBody) {
		String barrierRunnerCode = "\n" + "public class " + barrier
				+ " implements Runnable{" + "\n" + "public void run() {"
				+ barrierBody + "}}";
		return barrierRunnerCode;
	}

	public void init() {

		IMethodBinding binding = method.resolveBinding();

		ITypeBinding[] parameterBinding = binding.getMethodDeclaration()
				.getParameterTypes();

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

			shedulerMethod = m + "(" + arguments + ")";
		} else {
			shedulerMethod = binding.getMethodDeclaration().toString();
		}

		getTasks();
	}

	private void filterDependentVariables() {
		for (VariableDeclarationStatement vdecl : variableDeclarationStatements) {
			vdecl.fragments();
			for (VariableDeclarationFragment fragment : (List<VariableDeclarationFragment>) vdecl
					.fragments()) {
				IVariableBinding vbinding = (IVariableBinding) fragment
						.resolveBinding();
				vbinding.getName();
				ITypeBinding tbinding = vbinding.getType();
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
}
