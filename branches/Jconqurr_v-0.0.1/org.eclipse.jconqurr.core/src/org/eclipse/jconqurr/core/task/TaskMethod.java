package org.eclipse.jconqurr.core.task;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Statement;

public class TaskMethod implements ITaskMethod {
	private MethodDeclaration method;
	private List<String> tasks = new ArrayList();
	private String returnType;
	private String modifier;
	private String shedulerMethod;

	 
	private String sheduleTasks(List<String> tasks) {
		String exec = "static ExecutorService exec = Executors.newCachedThreadPool();"
				+ "\n";
		String sheduleTasksMethod = "\n" + shedulerMethod + "(){" + "try{"
				+ "\n" + "Future<?>[] future = (Future<?>[]) new Future["
				+ tasks.size() + "];" + "\n";
		String futureSubmit = "";
		for (int i = 0; i < tasks.size(); i++) {
			futureSubmit = futureSubmit + "future[" + i + "]=exec.submit(new"
					+ " task" + (i + 1) + "());" + "\n";
		}
		String futureGet = "for(int i=0;i<" + tasks.size() + ";i++)" + "\n"
				+ "future[i].get();" + "\n" + "}" + "\n"
				+ "catch (Exception e) {e.printStackTrace();}}";
		return exec + sheduleTasksMethod + futureSubmit + futureGet;
	}

	 
	public String getModifiedMethod() {
		String taskCode = "";
		int i = 1;
		for (String s : tasks) {
			taskCode = taskCode + taskRunnerCode("task" + i, s);
			i++;
		}
		return sheduleTasks(tasks)+"\n"+taskCode;
	}

	 
	public List<String> getTasks() {
		int directiveStart = -1;
		List<Statement> stmts = method.getBody().statements();
		List<Integer> positions = new ArrayList<Integer>();
		for (Statement s : stmts) {
			if (s.toString().startsWith("Directives.task();")) {
				directiveStart = stmts.indexOf(s);
				positions.add(stmts.indexOf(s));
			}
		}
		if (positions.size() > 0) {
			for (int m = 0; m < positions.size(); m++) {
				String task = "";
				List<Statement> subStatement;
				if (m + 1 < positions.size()) {
					subStatement = stmts.subList((positions.get(m) + 1),
							(positions.get(m + 1)));
				} else {
					subStatement = stmts.subList((positions.get(m) + 1), stmts
							.size());
				}
				if (subStatement.size() > 0) {
					for (Statement s : subStatement) {
						task = task + s.toString();
					}
					tasks.add(task);
				}
			}
		}
		return null;
	}

	 
	public void setMethod(MethodDeclaration method) {
		this.method = method;
		// TODO Auto-generated method stub

	}

	private String taskRunnerCode(String task, String taskBody) {
		String taskRunnerCode = "\n" + "static class " + task
				+ " implements Runnable{" + "\n" + "public void run() {"
				+ taskBody + "}}";
		return taskRunnerCode;
	}

	public void init() {
		// TODO Auto-generated method stub
		System.out.println("Simple Method Name"+method.getName());
		System.out.println(method.modifiers().toString());
		returnType = method.getReturnType2().toString();
		
		modifier = "";
		List<IExtendedModifier> modifiers = method.modifiers();
		for (IExtendedModifier a : modifiers) {
			if (a.isModifier()) {
				// System.out.println(((Modifier)
				// a).toString());
				modifier = modifier + " " + ((Modifier) a).toString();
			}
		}
		shedulerMethod =modifier + " " + returnType + " "
		+ method.getName().toString();
		getTasks();
		
	}

}
