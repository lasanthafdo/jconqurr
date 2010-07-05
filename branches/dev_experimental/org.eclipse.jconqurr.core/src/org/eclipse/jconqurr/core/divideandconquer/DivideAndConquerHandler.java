package org.eclipse.jconqurr.core.divideandconquer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jconqurr.core.ast.visitors.BlockVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;

public class DivideAndConquerHandler implements IDivideAndConquerHandler {
	private MethodDeclaration recursiveMethod;
	private MethodDeclaration recursiveMethodCaller;
	private String divideAndConquerCallerSignature;
	private String piTaskConstructorArgument;
	private String callerStmt;
	private List<String> argumentList = new ArrayList<String>();
	private List<String> argumentIdentifiers = new ArrayList<String>();
	private String bodyOfRunMethod;
	String className;
	

	@Override
	public String getModifiedMethods() {
		return getCaller() + getPIClass();
	}

	private String getCaller() {
		return divideAndConquerCallerSignature
				+ "{"
				+ callerStmt
				+ "\n"
				+ "try{"
				+ "\n"
				+ " FJTaskRunnerGroup g = new FJTaskRunnerGroup(Runtime.getRuntime().availableProcessors());"
				+ "\n" + className + " task = new " + className
				+ piTaskConstructorArgument + "\n" + "g.invoke(task);" + "\n"
				+ "}catch(InterruptedException ie){" + "\n"
				+ "ie.printStackTrace();" + "\n" + "}" + "\n" + "}";
	}

	private String getPIClass() {
		String classDecl = "public class " + className + " extends FJTask{"
				+ "\n";
		String fields = "";
		for (String s : argumentList) {
			fields = fields + s + ";" + "\n";
		}
		String constructorArguments = "";
		for (int i = 0; i < argumentList.size(); i++) {
			String s = argumentList.get(i);
			if (i == argumentList.size() - 1) {
				constructorArguments = constructorArguments + s;
			} else {
				constructorArguments = constructorArguments + s + ",";
			}
		}

		String constructorInit = "";
		for (String s : argumentIdentifiers) {
			constructorInit = constructorInit + "this." + s + "=" + s + ";\n";
		}
		String constructor = "public " + className + "(" + constructorArguments
				+ "){" + constructorInit + "}";

		String runMethod = "public void run()" + bodyOfRunMethod;
		return classDecl + fields + constructor + runMethod + "}";

	}

	@Override
	public void init() {
		className = recursiveMethod.getName().toString() + "Task";
		/*List<NormalAnnotation> mod = recursiveMethodCaller.modifiers();
		for (NormalAnnotation a : mod) {
			if (a.getTypeName().toString().startsWith("DivideAndConquer")) {
				a.delete();
			}
		}*/
		int j = -1;
		for (int i = 0; i < recursiveMethodCaller.getBody().statements().size(); i++) {
			if ((((Statement) recursiveMethodCaller.getBody().statements().get(
					i)).toString().trim()).startsWith(recursiveMethod.getName()
					.toString().trim())) {
				j = i;
			}
		}
		if (j != -1) {
			String s = ((Statement) recursiveMethodCaller.getBody()
					.statements().get(j)).toString();
			int length = recursiveMethod.getName().toString().length();
			piTaskConstructorArgument = s.substring(length);
			recursiveMethodCaller.getBody().statements().remove(j);
		}
		String stmt = "";
		for (int i = 0; i < recursiveMethodCaller.getBody().statements().size(); i++) {
			stmt = stmt + recursiveMethodCaller.getBody().statements().get(i);
		}
		callerStmt = stmt;
		recursiveMethodCaller.getBody().delete();
		IMethodBinding binding = recursiveMethodCaller.resolveBinding();
		 
		if (recursiveMethodCaller.parameters().size() > 0) {
			String arguments = "";
			for (int i = 0; i < recursiveMethodCaller.parameters().size(); i++) {
				String d = recursiveMethodCaller.parameters().get(i).toString();
				if (i == (recursiveMethodCaller.parameters().size() - 1)) {
					arguments += d;
				} else {
					arguments += d + ",";
				}
			}
			String m = binding.getMethodDeclaration().toString().substring(0,
					binding.getMethodDeclaration().toString().indexOf("("));
			divideAndConquerCallerSignature = m + "(" + arguments + ")";
		} else {
			divideAndConquerCallerSignature = binding.getMethodDeclaration().toString();
		}
		recursiveMethod.parameters();
		for (int i = 0; i < recursiveMethod.parameters().size(); i++) {
			SingleVariableDeclaration sv = (SingleVariableDeclaration) recursiveMethod
					.parameters().get(i);
			IVariableBinding vBinding = sv.resolveBinding();
			System.out.println("Parameter:" + vBinding.getType().getName()
					+ " " + vBinding.getName());
			argumentList.add(vBinding.getType().getName() + " "
					+ vBinding.getName());
			argumentIdentifiers.add(vBinding.getName());
		}
		recursiveMethod.getBody().statements();
		BlockVisitor bVisitor = new BlockVisitor();
		int position = 0;
		int occurence = 0;
		String task = "";
		for (int i = 0; i < recursiveMethod.getBody().statements().size(); i++) {
			Statement st = (Statement) (recursiveMethod.getBody().statements()
					.get(i));
			if (st.getNodeType() == Statement.IF_STATEMENT) {
				st.accept(bVisitor);
				List<Block> blocks = bVisitor.getBlocks();
				for (Block b : blocks) {
					for (int a = 0; a < b.statements().size(); a++) {
						Statement t = (Statement) (b.statements().get(a));
						if (t.toString().trim().startsWith(
								recursiveMethod.getName().toString())) {
							task = task
									+ className
									+ " task"
									+ occurence
									+ "= new "
									+ className
									+ t.toString().substring(
											recursiveMethod.getName()
													.toString().length())
									+ "\n";
							position = recursiveMethod.getBody().toString()
									.indexOf(
											recursiveMethod.getName()
													.toString());
							b.statements().remove(a);
							occurence++;
							a = a - 1;
						}
					}
				}
			}
		}
		bodyOfRunMethod = recursiveMethod.getBody().toString().substring(0,
				position)
				+ task
				+ "coInvoke(task0, task1);\n"
				+ recursiveMethod.getBody().toString().substring(position,
						recursiveMethod.getBody().toString().length());
		System.out.println(bodyOfRunMethod);
	}

	@Override
	public void setRecursionCaller(MethodDeclaration method) {
		recursiveMethodCaller = method;
	}

	@Override
	public void setRecursiveMethod(MethodDeclaration method) {
		recursiveMethod = method;
	}

}
