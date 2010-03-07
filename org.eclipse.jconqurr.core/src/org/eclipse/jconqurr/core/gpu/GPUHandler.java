package org.eclipse.jconqurr.core.gpu;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jconqurr.core.HandleProjectParallelism;
import org.eclipse.jconqurr.core.ast.visitors.AssignmentVisitor;
import org.eclipse.jconqurr.core.ast.visitors.ExpressionStatementVisitor;
import org.eclipse.jconqurr.core.ast.visitors.ForLoopVisitor;
import org.eclipse.jconqurr.core.ast.visitors.InfixExpressionVisitor;
import org.eclipse.jconqurr.core.ast.visitors.SimpleNameVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class GPUHandler implements IGPUHandler {

	private MethodDeclaration method;
	private String cubinFileName = "JCudaCubinSample_kernel.cu";
	private String cuMethodArguments;
	private String gloabalOutPut;
	private String noThreds;
	private String updater;
	private String leftOperandForCondition;
	private String rightOperandForCondition;
	private String operatorForCondition;
	private String initializer;
	private List<String> globalInputs = new ArrayList<String>();

	
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
			List initialisers = s.initializers();
			Expression expression = s.getExpression();
			List updaters = s.updaters();
			initializer="";
			for (Object obj : initialisers) {
				initializer=initializer+((Expression) obj).toString();
				System.out.println("Initializers :"
						+ ((Expression) obj).toString());
			}
			updater="";
			for (Object obj : updaters) {
				System.out
						.println("Updaters: " + ((Expression) obj).toString());
				updater=updater+((Expression) obj).toString();
			}
			System.out.println("Expression: " + expression.toString());
			InfixExpressionVisitor infixVisitor = new InfixExpressionVisitor();
			expression.accept(infixVisitor);
			System.out
					.println("Left operand:" + infixVisitor.getLeftHandSide());
			System.out.println("Condition operand: "
					+ infixVisitor.getOperator());
			leftOperandForCondition=infixVisitor.getLeftHandSide();
			operatorForCondition=infixVisitor.getOperator();
			rightOperandForCondition=infixVisitor.getRightHandSide();
			System.out.println("Right operand & no of threads: "
					+ infixVisitor.getRightHandSide());
			noThreds = infixVisitor.getRightHandSide();
			 
			System.out.println("Body:" + s.getBody());
			s.getBody();
			ExpressionStatementVisitor expVisitior = new ExpressionStatementVisitor();
			s.getBody().accept(expVisitior);
			for (ExpressionStatement ex : expVisitior.getExpressionStatements()) {
				AssignmentVisitor asignVisitor = new AssignmentVisitor();
				ex.accept(asignVisitor);
				asignVisitor.getLeftHandSide();
				System.out.println("Left hand Side: "
						+ asignVisitor.getLeftHandSide());
				if (asignVisitor.getLeftHandSide().getNodeType() == Assignment.ARRAY_ACCESS) {
					System.out.println("array access");
					ArrayAccess arrayAccess = (ArrayAccess) asignVisitor
							.getLeftHandSide();
					gloabalOutPut = getVariablePointer(asignVisitor
							.getLeftHandSide().toString(), gloabalOutPut,
							arrayAccess.resolveTypeBinding().getName());
				}
				System.out.println("Right hand side: "
						+ asignVisitor.getRightHandSide());
				infixVisitor.getInfixExpressions().clear();
				asignVisitor.getRightHandSide().accept(infixVisitor);
				infixVisitor.getLeftHandSide();
				System.out.println(infixVisitor.getLeftHandSide());

				infixVisitor.getRightHandSide();
				System.out.println(infixVisitor.getRightHandSide());
				for (InfixExpression in : infixVisitor.getInfixExpressions()) {
					System.out.println(in.getLeftOperand());
					if (in.getLeftOperand().getNodeType() == Assignment.ARRAY_ACCESS) {
						in.getLeftOperand().resolveTypeBinding();
						ArrayAccess arrayAccess = (ArrayAccess) in
								.getLeftOperand();
						SimpleNameVisitor simpleNameVisitor = new SimpleNameVisitor();
						arrayAccess.accept(simpleNameVisitor);
						String identifier = simpleNameVisitor.getIdentifier();
						System.out.println(arrayAccess.resolveTypeBinding()
								.getName()
								+ "-------------");

						globalInputs.add(getVariablePointer(in.getLeftOperand()
								.toString(), identifier, arrayAccess
								.resolveTypeBinding().getName()));
						arrayAccess.getArray().accept(simpleNameVisitor);

					}
					if (in.getRightOperand().getNodeType() == Assignment.ARRAY_ACCESS) {
						in.getLeftOperand().resolveTypeBinding();
						ArrayAccess arrayAccess = (ArrayAccess) in
								.getRightOperand();
						SimpleNameVisitor simpleNameVisitor = new SimpleNameVisitor();
						arrayAccess.accept(simpleNameVisitor);
						System.out.println(arrayAccess.resolveTypeBinding()
								.toString());
						String identifier = simpleNameVisitor.getIdentifier();
						System.out.println(arrayAccess.resolveTypeBinding()
								.getName()
								+ "-------------");

						globalInputs.add(getVariablePointer(in
								.getRightOperand().toString(), identifier,
								arrayAccess.resolveTypeBinding().getName()));
						arrayAccess.getArray().accept(simpleNameVisitor);

					}
					System.out.println(in.getOperator());
					System.out.println(in.getRightOperand());
					System.out.println("gloabal inputs.......................");
					for (String g : globalInputs) {
						System.out.println(g);
					}
				}
				asignVisitor.getRightHandSide();
			}
			writeToCUFile(s);
		}
		System.out.println("end the process.......................");
	}

	private String getVariablePointer(String exp, String name, String type) {
		int count = 0;
		int lenght = exp.length();
		String leftHandSide = exp;
		String pointerVariable = "";
		String identifier = "";
		for (int l = 0; l < lenght; l++) {
			if (leftHandSide.charAt(l) == '[') {
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
			System.out.println(pointerVariable);
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
			String path = HandleProjectParallelism.getSrcPath() + "/simple.cu";
			fout = new FileOutputStream(path);
			System.out.println(path);
			new PrintStream(fout).println(cuFile);
			fout.close();
			System.out.println("No Exception Occured");
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
		String body = "C[tidX]=A[tidx]+B[tidx];\n";
		String sync = "__syncthreads();";
		return "{" + variabels + body + sync + "}";
	}
 
	public String getModifiedCode(){
		return JCUDABinding.getTobytearraycode()+JCUDABinding.getGetpreparecubinfilecode();
	}
}
