package org.eclipse.jconqurr.core.pipeline;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jconqurr.core.ast.visitors.AssignmentVisitor;
import org.eclipse.jconqurr.core.ast.visitors.SimpleNameVisitor;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public class PipelineStage {
	MethodInvocation directiveStage;
	Statement statement;
	int stageNumber;
	String threadConstructor;
	String runMethod;
	String classDeclaration;
	String classFields;
	String expressionOfWhileStatement;
	List<Statement> statementBeforePipelin;
	List<String> threadInputFields = new ArrayList<String>();
	public static int queCounter = 0;
	public static int numberOfStages = 0;
	public static List<String> barrierFields = new ArrayList<String>();
	String inArg[];
	public static String queField = "";
	public static String booleanFields = "";
	String pipelineInput;
	String pipelineOutput;
	private String[] mainInput = { "" };
	private List<String> argumentSimpleName = new ArrayList<String>();
	private String threadDeclaration = "";

	public PipelineStage(MethodInvocation directiveStage, Statement statement,
			List<Statement> statementBeforePipelin,
			String expressionOfWhileStatement, int stageNumber) {
		this.directiveStage = directiveStage;
		this.statement = statement;
		this.statementBeforePipelin = statementBeforePipelin;
		this.expressionOfWhileStatement = expressionOfWhileStatement;
		this.stageNumber = stageNumber;
	}

	public String getThreadDeclaration() {
		int limit = argumentSimpleName.size() - 1;
		String s = "";
		for (int i = 0; i < argumentSimpleName.size(); i++) {
			if (i < limit) {
				s += argumentSimpleName.get(i) + ",";
			} else {
				s += argumentSimpleName.get(i);
			}
		}
		threadDeclaration = "Thread t" + stageNumber + " = new JPThread"
				+ stageNumber + "(" + s + ");" + "\n" + "t" + stageNumber
				+ ".start();";
		return threadDeclaration;
	}

	public void process() {
		MethodInvocation m = directiveStage;
		StringLiteral input = (StringLiteral) m.arguments().get(0);
		StringLiteral output = (StringLiteral) m.arguments().get(1);
		String inArg[] = input.getLiteralValue().split(",");
		String outArg[] = output.getLiteralValue().split(",");
		classDeclaration = "private class" + " JPThread" + stageNumber
				+ " extends Thread";
		if (stageNumber == 1 || stageNumber == numberOfStages) {
			mainInput = inArg;
		}
		AssignmentVisitor assignVisitor = new AssignmentVisitor();
		statement.accept(assignVisitor);
		if (stageNumber == 1) {
			pipelineInput = assignVisitor.getRightHandSide().toString();
		}
		if (stageNumber == numberOfStages) {
			pipelineOutput = assignVisitor.getLeftHandSide().toString();
		}
		SimpleNameVisitor svisitor = new SimpleNameVisitor();
		assignVisitor.getRightHandSide().accept(svisitor);
		if (assignVisitor.getRightHandSide() instanceof MethodInvocation) {
			MethodInvocation mi = ((MethodInvocation) assignVisitor
					.getRightHandSide());
			mi.getName().getIdentifier();
			if (stageNumber > 1) {
				pipelineInput = mi.getName().getIdentifier() + "("
						+ "queue_out" + (stageNumber - 1) + ".take()" + ")";
			}
		}
		String inputFieldType = "";
		String outputType = "";
		for (Statement s : statementBeforePipelin) {
			if (s instanceof VariableDeclarationStatement) {
				SimpleNameVisitor visit = new SimpleNameVisitor();
				s.accept(visit);
				for (int i = 0; i < inArg.length; i++) {
					if (visit.getIdentifiers().contains(inArg[i])) {
						inputFieldType = ((VariableDeclarationStatement) s)
								.getType().toString();
						String field = inputFieldType + " " + inArg[i];
						threadInputFields.add(field);
						argumentSimpleName.add(inArg[i]);
					}
				}
				for (int i = 0; i < outArg.length; i++) {
					if (visit.getIdentifiers().contains(outArg[i])) {
						outputType = ((VariableDeclarationStatement) s)
								.getType().toString();
						queCounter++;
						addQueField(outputType, "queue_out" + queCounter);
						// String field = inputFieldType + " " + inArg[i];
					}
				}
			}
		}
	}

	public String getInputThreadClass() {
		booleanFields += "static boolean isJPThread" + stageNumber
				+ "Done = false;";
		barrierFields.add("static boolean isJPThread" + stageNumber
				+ "Done = false;");
		String threadClass = " private class" + " JPThread" + stageNumber
				+ " extends Thread {" + "\n";
		String inputField = "";
		String constructorArg = "";
		String constructorInit = "";
		for (int j = 0; j < threadInputFields.size(); j++) {
			inputField += threadInputFields.get(j) + ";\n";
			if (j == (threadInputFields.size() - 1)) {
				constructorArg += threadInputFields.get(j);
			} else {
				constructorArg += threadInputFields.get(j) + ",";
			}
		}
		if (stageNumber == 1 || stageNumber == numberOfStages) {
			for (String s : mainInput) {
				String t = "this." + s + "=" + s + ";" + "\n";
				constructorInit += t;
			}
		}
		String threadConstructor = "JPThread" + stageNumber + "("
				+ constructorArg + "){" + "\n" + constructorInit + "}";
		if (stageNumber > 1) {
			expressionOfWhileStatement = "!(isJPThread" + (stageNumber - 1)
					+ "Done && (queue_out" + (queCounter - 1) + ".isEmpty()))";
		}
		String statement = "";
		String barrierInvoke = "";
		if (stageNumber == numberOfStages) {
			barrierInvoke = "b.notifyBarrier();";
		}
		statement = "queue_out" + queCounter + ".put(" + pipelineInput + ");";
		String body = "try{" + "while(" + expressionOfWhileStatement + "){"
				+ "try{" + statement + "} catch (InterruptedException e) {"
				+ "e.printStackTrace();} }}catch (Exception e) {"
				+ "e.printStackTrace();}" + "isJPThread" + stageNumber
				+ "Done = true;" + barrierInvoke;
		String runMethod = "public void run() {" + body + "\n" + "}";
		String inputThread = threadClass + inputField + threadConstructor
				+ runMethod + "}";
		return inputThread;

	}

	public String getThreadClass() {
		return null;

	}

	private String addQueField(String type, String name) {
		String s = "";
		if (type.equals("double")) {
			s = "static BlockingQueue<Double> " + name
					+ "= new LinkedBlockingQueue<Double>();";
		} else if (type.equals("float")) {
			s = "static BlockingQueue<Float> " + name
					+ "= new LinkedBlockingQueue<Float>();";
		} else if (type.equals("int")) {
			s = "static BlockingQueue<Integer> " + name
					+ "= new LinkedBlockingQueue<Integer>();";
		} else if (type.equals("short")) {
			s = "static BlockingQueue<Short> " + name
					+ "= new LinkedBlockingQueue<Short>();";

		} else if (type.equals("byte")) {
			s = "static BlockingQueue<Byte> " + name
					+ "= new LinkedBlockingQueue<Byte>();";
		} else {
			s = "static BlockingQueue<" + type + ">" + name
					+ "= new LinkedBlockingQueue<" + type + ">();";
		}
		queField += s;
		return s;
	}
}
