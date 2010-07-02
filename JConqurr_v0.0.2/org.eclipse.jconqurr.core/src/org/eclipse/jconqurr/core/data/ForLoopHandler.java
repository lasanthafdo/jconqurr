package org.eclipse.jconqurr.core.data;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jconqurr.core.ast.visitors.ForLoopVisitor;
import org.eclipse.jconqurr.core.ast.visitors.MethodInvocationVisitor;
import org.eclipse.jconqurr.core.ast.visitors.VariableDeclarationVisitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;


public class ForLoopHandler implements IForLoopHandler {
	private MethodDeclaration method;
	private String shedulerMethod;
	private List<ForLoop> forLoops = new ArrayList<ForLoop>();
	public static int counter = 0;
	private String taskRunnerCode = "";
	private boolean isStatic = false;

	/**
	 * @see IForLoopHandler#getModifiedMethod()
	 */
	public String getModifiedMethod() {
		return sheduleTasks() + "\n" + taskRunnerCode;
	}

	/**
	 * @see IForLoopHandler#setTasks()
	 */
	public void setTasks() {
		ForLoopVisitor loopVisitor = new ForLoopVisitor();
		method.accept(loopVisitor);
		method.getBody().statements();
		MethodInvocationVisitor methodInvocationVisitor = new MethodInvocationVisitor();
		method.accept(methodInvocationVisitor);
		List<MethodInvocation> directivesForLoops = new ArrayList<MethodInvocation>();

		for (MethodInvocation m : methodInvocationVisitor.getMethods()) {
			if (m.toString().startsWith("Directives.forLoop()")) {
				directivesForLoops.add(m);
			}
		}
		
		String blockContent = "";
		for (MethodInvocation m : directivesForLoops) {
			ASTNode parent = m.getParent();
			while (parent.getNodeType() != 8) {
				parent = parent.getParent();
			}
			if (parent.getNodeType() == 8) {
				Block block = (Block) parent;
				List<Statement> stmt = block.statements();
				if (!(blockContent.equals(block.toString()))) {
					blockContent = block.toString();
					for (int i = 0; i < stmt.size(); i++) {
						stmt.get(i).toString();
						if (stmt.get(i).toString().trim().startsWith(
								"Directives.forLoop()")) {
							ForLoop fLoop = new ForLoop(block, i + 1);
							fLoop.init();
							forLoops.add(fLoop);
						}
					}
				}
			}
		}
	}

	/**
	 * @see IForLoopHandler#init()
	 */
	public void init() {
		//counter = 0;
		
		for (int i = 0; i < method.modifiers().size(); i++) {
			if (method.modifiers().get(i).toString().equals("static")) {
				isStatic = true;
			}
		}
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
			//System.out.println(m + "(" + arguments + ")");
			shedulerMethod = m + "(" + arguments + ")";
		} else {
			shedulerMethod = binding.getMethodDeclaration().toString();
		}
		VariableDeclarationVisitor visitor = new VariableDeclarationVisitor();
		method.accept(visitor);
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
	private String taskRunnerCode(String task, String taskBody,
			String threadFields, String constructor) {
		String taskRunnerCode = "";
		if (isStatic) {
			taskRunnerCode = "\n" + "static class " + task
					+ " implements Runnable{" + threadFields + "\n"
					+ constructor + "\n" + "public void run() {" + taskBody
					+ "}}";
		} else {
			taskRunnerCode = "\n" + "class " + task + " implements Runnable{"
					+ threadFields + "\n" + constructor + "\n"
					+ "public void run() {" + taskBody + "}}";
		}
		return taskRunnerCode;
	}

	/**
	 * creates the code relevant to task scheduling
	 * 
	 * @param tasks
	 * @return String
	 */
	private String sheduleTasks() {
		String methodDeclaration = "\n" + shedulerMethod + "{";
		String methodBody = "";
		if (forLoops.size() > 0) {
			for (ForLoop f : forLoops) {
				String beforeLoop = "";
				String futurSheduler = "";
				String futureSubmit = "";
				String afterLoop = "";
				String objectCreationArguments = "";
				String threadFields = "";
				List<String> constructorArguments = new ArrayList<String>();
				List<String> constructorInitialization = new ArrayList<String>();
				List<VariableDeclarationStatement> threadClassFields = f
						.getThreadClassFields();
				//System.out.println(threadClassFields);
				for (int i = 0; i < method.parameters().size(); i++) {
					if (method.parameters().get(i) instanceof SingleVariableDeclaration) {
						/*System.out.println("instanceof singlevariabledec..");
						System.out.println("parameter:"
								+ method.parameters().get(i));*/
						SingleVariableDeclaration var = (SingleVariableDeclaration) method
								.parameters().get(i);
						IVariableBinding vbind = var.resolveBinding();
						//System.out.println(vbind.getName());
						for (int j = 0; j < f.getVariables().size(); j++) {
							if (vbind.getName().equals(
									f.getVariables().get(j).toString())) {
								objectCreationArguments += vbind.getName()
										+ ",";
								threadFields += method.parameters().get(i)
										+ ";" + "\n";
								constructorArguments.add(method.parameters()
										.get(i).toString());
								String s = "this." + vbind.getName() + "="
										+ vbind.getName();
								constructorInitialization.add(s);
							}
						}
					}
				}
				if(objectCreationArguments.length()>=1){
				objectCreationArguments=objectCreationArguments.substring(0, (objectCreationArguments.length()-1));
				}
				for (int i = 0; i < threadClassFields.size(); i++) {
					List<VariableDeclarationFragment> fragments = threadClassFields
							.get(i).fragments();
					for (int j = 0; j < fragments.size(); j++) {
						if ((i == (threadClassFields.size() - 1))
								&& (j == (fragments.size() - 1))) {
							objectCreationArguments += fragments.get(j)
									.getName();
						} else {
							objectCreationArguments += fragments.get(j)
									.getName()
									+ ",";
						}
						IVariableBinding typeBinding = fragments.get(j)
								.resolveBinding();
						ITypeBinding tbinding = fragments.get(j).getName()
								.resolveTypeBinding();
						/*System.out.println(tbinding.getName() + " "
								+ typeBinding.getName() + ";");*/
						threadFields += tbinding.getName() + " "
								+ typeBinding.getName() + ";" + "\n";
						constructorArguments.add(tbinding.getName() + " "
								+ typeBinding.getName());
						String s = "this." + typeBinding.getName() + "="
								+ typeBinding.getName();
						constructorInitialization.add(s);
					}
				}
				String constructorBody = "";
				String constructorParameters = "";
				for (int i = 0; i < constructorArguments.size(); i++) {
					if (i < (constructorArguments.size() - 1)) {
						constructorParameters += constructorArguments.get(i)
						+ ",";
						
					} else {
						constructorParameters += constructorArguments.get(i);
					}
				}
				for (int i = 0; i < constructorInitialization.size(); i++) {
					constructorBody += constructorInitialization.get(i) + ";"
							+ "\n";
				}

				System.out.println(objectCreationArguments);
				if (f.getStatementBeforForLoop().size() > 0) {
					for (Statement s : f.getStatementBeforForLoop()) {
						beforeLoop = beforeLoop + s.toString() + "\n";
					}
				}

				if (f.getStatementsAfterForLoop().size() > 0) {
					for (Statement s : f.getStatementsAfterForLoop()) {
						afterLoop = afterLoop + s.toString() + "\n";
					}
				}
				f.getTasks();
				futurSheduler = "try{" + "\n"
						+ "Future<?>[] future = (Future<?>[]) new Future["
						+ f.getTasks().size() + "];" + "\n";

				for (int i = 0; i < f.getTasks().size(); i++) {
					futureSubmit = futureSubmit + "future[" + i
							+ "]=exec.submit(new" + " forLooptask"
							+ (counter + 1) + "(" + objectCreationArguments
							+ "));" + "\n";
					String constructor = "";
					constructor = "forLooptask" + (counter + 1) + "("
							+ constructorParameters + ")" + "{"
							+ constructorBody + "}";

					taskRunnerCode = taskRunnerCode
							+ taskRunnerCode("forLooptask" + (counter + 1), f
									.getTasks().get(i), threadFields,
									constructor);
					counter++;
				}
				String futureGet = "for(int i=0;i<" + f.getTasks().size()
						+ ";i++)" + "\n" + "future[i].get();" + "\n" + "}"
						+ "\n" + "catch (Exception e) {e.printStackTrace();}";
				String executorCode = futurSheduler + futureSubmit + futureGet;
				methodBody = methodBody + beforeLoop + executorCode + afterLoop
						+ "\n";
			}
		}
		return methodDeclaration + methodBody + "}";
	}
}