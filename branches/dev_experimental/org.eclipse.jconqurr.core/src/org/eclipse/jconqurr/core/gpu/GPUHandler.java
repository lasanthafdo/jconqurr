package org.eclipse.jconqurr.core.gpu;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jconqurr.core.HandleProjectParallelism;
import org.eclipse.jconqurr.core.ast.visitors.AssignmentVisitor;
import org.eclipse.jconqurr.core.ast.visitors.BlockVisitor;
import org.eclipse.jconqurr.core.ast.visitors.ExpressionStatementVisitor;
import org.eclipse.jconqurr.core.ast.visitors.ForLoopVisitor;
import org.eclipse.jconqurr.core.ast.visitors.InfixExpressionVisitor;
import org.eclipse.jconqurr.core.ast.visitors.SimpleNameVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Statement;

public class GPUHandler implements IGPUHandler {

	private MethodDeclaration method;

	private String gloabalOutPut;
	private String leftOperandOfBodyStatement;
	private String rightOperandOfBodyStatement;
	private String operatorOfBodyStatement;
	private String noThreds;
	private String updater;
	private String leftOperandForCondition;
	private String rightOperandForCondition;
	private String operatorForCondition;
	private String initializer;
	private final String tidx = "tidx";
	private final String cubinFileName = "simple.cu";
	private List<String> globalInputs = new ArrayList<String>();
	private List<String> identifiers = new ArrayList<String>();
	private String outPutIdentifier;
	private String leftOperator;
	private String returnType;
	private String modifier;
	private String newBody;

	private String getCubinFileDeclaration() {
		String cubinDeclaraion = "String cubinFileName = prepareCubinFile(\""
				+ cubinFileName + "\");";
		return cubinDeclaraion;
	}

	@Override
	public void process() {
		System.out.println("Called the process.......................");

		ForLoopVisitor forLoopVisitor = new ForLoopVisitor();
		this.method.accept(forLoopVisitor);
		List<ForStatement> forLoops = forLoopVisitor.getForLoops();
		for (ForStatement s : forLoops) {
			handleLoopDeclaration(s);
			handleLoopBody(s);
			writeToCUFile(s);
		}
		handleMethodBody();
		System.out.println("end the process.......................");
	}

	private String getGpuCode() {

		String prepareCubinFile = "String cubinFileName = prepareCubinFile(\""
				+ cubinFileName + "\");\n";
		String malloc = "";
		for (String s : identifiers) {
			malloc = malloc + JCUDABinding.getMemAllocFor2DHostVariablesCode(s);
		}
		String content = "";
		for (String s : identifiers) {
			content = content
					+ JCUDABinding.getCopyContentsFor2DHostVariablesCode(s);
		}
		String numTHreads = "int numThreads=" + noThreds + ";\n";

		String gpu = "try{"
				+ prepareCubinFile
				+ JCUDABinding.getDriverinitializationcode()
				+ JCUDABinding.getLoadcubincode()
				+ numTHreads
				+ JCUDABinding.getFunctionpointercode()
				+ malloc
				+ content
				+ JCUDABinding
						.getMemAllocDeviceOutputVariablesCode(outPutIdentifier)
				+ JCUDABinding.getExePrametersCode(identifiers.get(0),
						identifiers.get(1), outPutIdentifier)
				+ JCUDABinding.getMemAllocForHostOutputCode(outPutIdentifier)
				+ "\n" + "}" + "catch (IOException e) {}";
		return gpu;
	}

	private void handleLoopBody(ForStatement s) {
		InfixExpressionVisitor infixVisitor = new InfixExpressionVisitor();
		ExpressionStatementVisitor expVisitior = new ExpressionStatementVisitor();
		s.getBody().accept(expVisitior);
		for (ExpressionStatement ex : expVisitior.getExpressionStatements()) {
			AssignmentVisitor assignVisitor = new AssignmentVisitor();
			ex.accept(assignVisitor);
			if (!assignVisitor.getAssignments().isEmpty()) {
				assignVisitor.getLeftHandSide();
				operatorOfBodyStatement = assignVisitor.getOperator()
						.toString();
				if (assignVisitor.getLeftHandSide().getNodeType() == Assignment.ARRAY_ACCESS) {
					ArrayAccess arrayAccess = (ArrayAccess) assignVisitor
							.getLeftHandSide();
					gloabalOutPut = getVariablePointer(assignVisitor
							.getLeftHandSide().toString(), gloabalOutPut,
							arrayAccess.resolveTypeBinding().getName());
					leftOperator = assignVisitor.getLeftHandSide().toString();
					leftOperandOfBodyStatement = getModifiedArray(assignVisitor
							.getLeftHandSide().toString());
				}
				infixVisitor.getInfixExpressions().clear();
				assignVisitor.getRightHandSide().accept(infixVisitor);
				for (InfixExpression in : infixVisitor.getInfixExpressions()) {
					System.out.println(in.getLeftOperand());
					if (in.getLeftOperand().getNodeType() == Assignment.ARRAY_ACCESS) {
						in.getLeftOperand().resolveTypeBinding();
						rightOperandOfBodyStatement = getModifiedArray(in
								.getLeftOperand().toString());
						ArrayAccess arrayAccess = (ArrayAccess) in
								.getLeftOperand();
						SimpleNameVisitor simpleNameVisitor = new SimpleNameVisitor();
						arrayAccess.accept(simpleNameVisitor);
						String identifier = simpleNameVisitor.getIdentifier();
						globalInputs.add(getVariablePointer(in.getLeftOperand()
								.toString(), identifier, arrayAccess
								.resolveTypeBinding().getName()));
						arrayAccess.getArray().accept(simpleNameVisitor);
					}
					rightOperandOfBodyStatement = rightOperandOfBodyStatement
							+ in.getOperator();
					if (in.getRightOperand().getNodeType() == Assignment.ARRAY_ACCESS) {
						in.getLeftOperand().resolveTypeBinding();
						ArrayAccess arrayAccess = (ArrayAccess) in
								.getRightOperand();
						SimpleNameVisitor simpleNameVisitor = new SimpleNameVisitor();
						arrayAccess.accept(simpleNameVisitor);
						String identifier = simpleNameVisitor.getIdentifier();
						rightOperandOfBodyStatement = rightOperandOfBodyStatement
								+ getModifiedArray(in.getRightOperand()
										.toString());
						globalInputs.add(getVariablePointer(in
								.getRightOperand().toString(), identifier,
								arrayAccess.resolveTypeBinding().getName()));
						arrayAccess.getArray().accept(simpleNameVisitor);
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void handleMethodBody() {
		String body = method.getBody().toString();
		int d = body.indexOf("for");
		System.out.println("a=" + d);
		returnType = method.getReturnType2().toString();
		modifier = "";
		List<IExtendedModifier> modifiers = method.modifiers();
		for (IExtendedModifier a : modifiers) {
			if (a.isModifier()) {
				modifier = modifier + " " + ((Modifier) a).toString();
			}
		}
		BlockVisitor blockVisitor = new BlockVisitor();
		method.accept(blockVisitor);
		List<Block> blocks = blockVisitor.getBlocks();
		int position = -1;
		for (Block b : blocks) {
			List<Statement> stmtList = b.statements();
			for (int i = 0; i < stmtList.size(); i++) {
				Statement stmt = stmtList.get(i);
				if (stmt.getNodeType() == Statement.FOR_STATEMENT) {
					position = method.getBody().toString().indexOf(stmt.toString().substring(0, 5));
					System.out.println("Position:" + position);
					//stmtList.remove(i);
				}

			}

		}
		if (method.getBody().toString().length() == position) {
			newBody = "{" + getGpuCode() + "}";
		} else {
			if (position < method.getBody().toString().length()
					&& position >= 0)
				newBody = method.getBody().toString().substring(0, position)
						+ getGpuCode()
						+ method.getBody().toString().substring(position,
								method.getBody().toString().length());

		}
		// System.out.println(method.getBody().toString().substring(0,
		// position));
	}

	private void handleLoopDeclaration(ForStatement s) {
		List<?> initialisers = s.initializers();
		Expression expression = s.getExpression();
		List<?> updaters = s.updaters();
		initializer = "";
		for (Object obj : initialisers) {
			initializer = initializer + ((Expression) obj).toString();
		}
		updater = "";
		for (Object obj : updaters) {
			updater = updater + ((Expression) obj).toString();
		}
		InfixExpressionVisitor infixVisitor = new InfixExpressionVisitor();
		expression.accept(infixVisitor);
		leftOperandForCondition = infixVisitor.getLeftHandSide();
		operatorForCondition = infixVisitor.getOperator();
		rightOperandForCondition = infixVisitor.getRightHandSide();
		noThreds = infixVisitor.getRightHandSide();
	}

	private String getModifiedArray(String exp) {
		int count = 0;
		String identifier = "";
		for (int l = 0; l < exp.length(); l++) {
			if (exp.charAt(l) == '[') {
				if (count > 0) {
					count++;
				} else {
					identifier = exp.substring(0, l);
					if (leftOperator.startsWith(identifier)) {
						outPutIdentifier = identifier;
					} else {
						identifiers.add(identifier);
					}
					count++;
				}
			}
		}
		if (count == 1) {
			return identifier + "[" + tidx + "]";
		} else
			return "";
	}

	private String getVariablePointer(String exp, String name, String type) {
		int count = 0;
		String pointerVariable = "";
		String identifier = "";
		for (int l = 0; l < exp.length(); l++) {
			if (exp.charAt(l) == '[') {
				if (count > 0) {
					count++;
				} else {
					identifier = exp.substring(0, l);
					count++;
				}
			}
		}
		if (count > 0) {
			String pointer = "";
			for (int h = 0; h < count; h++) {
				pointer = pointer + "*";
			}
			pointerVariable = type + pointer + " " + identifier;
		}
		return pointerVariable;
	}

	@Override
	public void setMethod(MethodDeclaration method) {
		this.method = method;
	}

	private void writeToCUFile(ForStatement s) {
		FileOutputStream fout;
		System.out.println(HandleProjectParallelism.getSrcPath());
		String cuFile = getCuDeclaration() + getCuFileBody();
		try {
			String path = HandleProjectParallelism.getSrcPath()
					+ IPath.SEPARATOR + "simple.cu";
			fout = new FileOutputStream(path);
			System.out.println(path);
			new PrintStream(fout).println(cuFile);
			fout.close();
		} catch (IOException e) {
			System.err.println("Unable to write to file: " + e.getMessage());
		}
	}

	private String getCuDeclaration() {
		String decl = "extern \"C\"\n" + "__global__ void sampleKernel(";
		String inputArg = "";
		for (String s : globalInputs) {
			inputArg = inputArg + "," + s;
		}
		String arg = gloabalOutPut + inputArg;
		return decl + arg + ")";

	}

	private String getCuFileBody() {
		String variabels = "const unsigned int tidX = threadIdx.x;" + "\n"
				+ "globalOutputData[tidX] = 0;" + "\n";
		String body = leftOperandOfBodyStatement + operatorOfBodyStatement
				+ rightOperandOfBodyStatement + ";\n";
		String sync = "__syncthreads();\n";
		return "{\n" + variabels + body + sync + "}";
	}

	public String getModifiedCode() {

		String method = modifier + " " + returnType + " "
				+ this.method.getName().toString() + "()" + newBody;
		return method + JCUDABinding.getTobytearraycode()
				+ JCUDABinding.getGetpreparecubinfilecode();
	}
}
