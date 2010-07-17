package org.eclipse.jconqurr.core.task;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jconqurr.core.ast.visitors.SimpleNameVisitor;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public class Task {
	private List<Statement> statements;
	private List<Statement> statementsBeforeTasks;
	private List<VariableDeclarationStatement> varibaleDeclarations = new ArrayList<VariableDeclarationStatement>();
	private List<SimpleName> variables = new ArrayList<SimpleName>();
	private List<String> variableNames = new ArrayList<String>();
	private List<VariableDeclarationStatement> threadClassFields = new ArrayList<VariableDeclarationStatement>();
	private String runMethod;
	private boolean barrierExists;
	private MethodDeclaration method;
	private String objectCreationArguments = "";
	private String threadClass = "";
	private int taskId;

	public Task(MethodDeclaration method, List<Statement> statements,
			List<Statement> statementsBeforeTasks, boolean barrierExists,
			int taskId) {
		this.method = method;
		this.statements = statements;
		this.statementsBeforeTasks = statementsBeforeTasks;
		this.barrierExists = barrierExists;
		this.taskId = taskId;
		for (Statement stm : statementsBeforeTasks) {
			if (stm instanceof VariableDeclarationStatement) {
				varibaleDeclarations.add((VariableDeclarationStatement) stm);
			}
		}
		for (Statement stm : statements) {
			SimpleNameVisitor nameVisitor = new SimpleNameVisitor();
			stm.accept(nameVisitor);
			List<SimpleName> names = nameVisitor.getVariables();
			if (names != null) {
				for (SimpleName name : names) {
					boolean contains = false;
					for (SimpleName n : variables) {
						if (n.toString().equals(name.toString())) {
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
		setRunMethod();
		setThreadClass();
		// System.out.println(getThreadClass());
		// System.out.println(getObject());
	}

	private void setRunMethod() {
		String taskBody = "";
		String method = "";
		for (Statement stm : statements) {
			taskBody += stm.toString();
		}
		String barrierCaller = "try {" + "\n" + "barrier.await();" + "\n" + "}"
				+ "\n" + " catch (InterruptedException e) {" + "\n"
				+ "// TODO Auto-generated catch block" + "\n"
				+ "e.printStackTrace();" + "\n" + "}" + "\n"
				+ " catch (BrokenBarrierException e) {" + "\n"
				+ "e.printStackTrace();" + "\n" + "}";
		if (barrierExists) {
			method = "public void run(){" + taskBody + barrierCaller + "}";
		} else {
			method = "public void run(){" + taskBody + "}";
		}
		runMethod = method;
	}

	private void filterDependentVariables() {
		for (VariableDeclarationStatement vdecl : varibaleDeclarations) {
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
						// System.out.println("equals");
						threadClassFields.add(vdecl);
					}
				}
			}
		}
	}

	private void setThreadClass() {

		String threadFields = "";
		List<String> constructorArguments = new ArrayList<String>();
		List<String> constructorInitialization = new ArrayList<String>();
		for (int i = 0; i < method.parameters().size(); i++) {
			if (method.parameters().get(i) instanceof SingleVariableDeclaration) {
				SingleVariableDeclaration var = (SingleVariableDeclaration) method
						.parameters().get(i);
				IVariableBinding vbind = var.resolveBinding();
				for (int j = 0; j < variables.size(); j++) {
					if (vbind.getName().equals(variables.get(j).toString())) {
						objectCreationArguments += vbind.getName() + ",";
						threadFields += method.parameters().get(i) + ";" + "\n";
						constructorArguments.add(method.parameters().get(i)
								.toString());
						String s = "this." + vbind.getName() + "="
								+ vbind.getName();
						constructorInitialization.add(s);
					}
				}
			}
		}
		if (objectCreationArguments.length() >= 1) {
			objectCreationArguments = objectCreationArguments.substring(0,
					(objectCreationArguments.length() - 1));
		}
		for (int i = 0; i < threadClassFields.size(); i++) {
			List<VariableDeclarationFragment> fragments = threadClassFields
					.get(i).fragments();
			for (int j = 0; j < fragments.size(); j++) {
				if ((i == (threadClassFields.size() - 1))
						&& (j == (fragments.size() - 1))) {
					objectCreationArguments += fragments.get(j).getName();
				} else {
					objectCreationArguments += fragments.get(j).getName() + ",";
				}
				IVariableBinding typeBinding = fragments.get(j)
						.resolveBinding();
				ITypeBinding tbinding = fragments.get(j).getName()
						.resolveTypeBinding();
				threadFields += tbinding.getName() + " "
						+ typeBinding.getName() + ";" + "\n";
				constructorArguments.add(tbinding.getName() + " "
						+ typeBinding.getName());
				String s = "this." + typeBinding.getName() + "="
						+ typeBinding.getName();
				constructorInitialization.add(s);
			}
		}
		if(barrierExists){
			if(objectCreationArguments.length()>0){
			objectCreationArguments+=",barrier";
			}else{
				objectCreationArguments+="barrier";
			}
			constructorArguments.add("CyclicBarrier barrier");
			threadFields +="CyclicBarrier barrier;";
			constructorInitialization.add("this.barrier=barrier");
		}
		String constructorBody = "";
		String constructorParameters = "";
		for (int i = 0; i < constructorArguments.size(); i++) {
			if (i < (constructorArguments.size() - 1)) {
				constructorParameters += constructorArguments.get(i) + ",";

			} else {
				constructorParameters += constructorArguments.get(i);
			}
		}
		for (int i = 0; i < constructorInitialization.size(); i++) {
			constructorBody += constructorInitialization.get(i) + ";" + "\n";
		}
		// System.out.println("constructorBody="+constructorBody);
		// System.out.println("constructorParameters="+constructorParameters);
		String constructor = "task" + taskId + "(" + constructorParameters
				+ ")" + "{"  + constructorBody + "}";
		System.out.println(threadFields);
		threadClass = "\n" + "public class " + "task" + taskId
				+ " implements Runnable{" + "\n" +threadFields+ constructor + "\n"
				+ getRunMethod() + "}";
	}

	public String getObject() {
		String object = "new" + " task" + taskId + "("
				+ objectCreationArguments + ")";
		return object;
	}

	public String getThreadClass() {
		return threadClass;
	}

	public String getRunMethod() {
		return runMethod;
	}
}
