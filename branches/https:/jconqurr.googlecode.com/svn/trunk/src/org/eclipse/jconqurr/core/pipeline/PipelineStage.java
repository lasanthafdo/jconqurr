package org.eclipse.jconqurr.core.pipeline;

//import java.util.ArrayList;
import java.util.List;
import org.eclipse.jconqurr.core.ast.visitors.SimpleNameVisitor;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public class PipelineStage {
	MethodInvocation directiveStage;
	List<Statement> statement;
	int stageNumber;
	String threadClassDeclaration = "";
	String threadVariablesDeclaration = "";
	String threadConstructor = "";
	String runMethod = "";
	String threadClass = "";

	String expressionOfWhileStatement;
	List<Statement> statementsBeforePipeline;
	public static int numberOfStages = 0;
	public static String barrierFields = "";
	String inArg[];
	String outArg[];
	String inputFields = "";
	String outputFields = "";

	private String threadDeclaration = "";
	PipelineHandler pipelineHandler;
	boolean hasInClass;
	boolean hasOutClass;
	String outClass = "";

	// static String queueFields = "";
	private String otherVariableDeclarations = "";
	static String className = "";
	String classObjectName = "";
	String queueType = "";

	public PipelineStage(MethodInvocation directiveStage,
			List<Statement> statement, List<Statement> statementBeforePipeline,
			String expressionOfWhileStatement, int stageNumber,
			PipelineHandler handler) {
		this.directiveStage = directiveStage;
		this.statement = statement;
		this.statementsBeforePipeline = statementBeforePipeline;
		this.expressionOfWhileStatement = expressionOfWhileStatement;
		this.stageNumber = stageNumber;
		this.pipelineHandler = handler;
	}

	public void processStage(String name) {
		if (classObjectName.isEmpty()) {
			classObjectName = "jq" + name;
		}

		MethodInvocation m = directiveStage;
		StringLiteral input = (StringLiteral) m.arguments().get(0);
		StringLiteral output = (StringLiteral) m.arguments().get(1);

		this.inputFields = input.getLiteralValue();
		if (!output.getLiteralValue().equals("null")) {
			this.outputFields = output.getLiteralValue();
		} else {
			this.outputFields = "";
		}

		this.inArg = input.getLiteralValue().split(",");
		this.outArg = output.getLiteralValue().split(",");

		if (this.outArg.length > 1) {
			this.hasOutClass = true;
			setOutClass();
		}

		if (stageNumber > 1) {
			if (pipelineHandler.getPipelineStages().get(stageNumber - 2).outArg.length > 1) {
				hasInClass = true;
			}
		}
		//setOtherVariables();
		setThreadClassDeclaration();
		setThreadVariablesDeclaration();
		setThreadConstructor();
		setRunMethod();
		setThreadDeclaration();
		setBarrierFields();
		addQueueFields();

		setThreadClass();
	}

	private void setThreadClassDeclaration() {
		threadClassDeclaration = "private class JPThread" + stageNumber
				+ " extends Thread";
	}

	private void setThreadVariablesDeclaration() {
		for (String s : inArg) {
			if (!inArg[0].toString().equals("null")) {
				threadVariablesDeclaration += getVariableType(s) + " " + s
						+ ";";
			}
		}
		for (String s : outArg) {
			if (!outArg[0].toString().equals("null")) {
				threadVariablesDeclaration += getVariableType(s) + " " + s
						+ ";";
			}
		}
	}

	private void setThreadConstructor() {
		String variableList = "";
		String variableInitialization = "";
		// Set Variable List
		for (String s : inArg) {
			if (inArg.length != 0) {
				if (!variableList.isEmpty()) {
					variableList += ", ";
				}
				variableList += getVariableType(s) + " " + s;
			}
		}
		for (String s : outArg) {
			if (!outArg[0].toString().equals("null")) {
				if (!variableList.isEmpty()) {
					variableList += ", ";
				}
				variableList += getVariableType(s) + " " + s;
			}
		}

		// Set Variable Initialization
		for (String s : inArg) {
			variableInitialization += "this." + s + "= " + s + ";";
		}
		for (String s : outArg) {
			if (!outArg[0].toString().equals("null")) {
				variableInitialization += "this." + s + "= " + s + ";";
			}
		}
		threadConstructor = "public JPThread" + (stageNumber) + "("
				+ variableList + ") {" + variableInitialization + "}";
	}

	private void setRunMethod() {
		String assignments = "";
		String threadInput = "";
		String threadOutput = "";
		String loopCondition = "";
		String function = "";
		String constrainingCondition = "";

		// Set Thread Input
		if (this.hasInClass) {
			for (String s : this.pipelineHandler.getPipelineStages().get(
					this.stageNumber - 2).outArg) {
				assignments += s + " = " + "jq." + s + ";";
			}
			threadInput += "JqOut" + (stageNumber - 1) + " jq = queueOut"
					+ (stageNumber - 1) + ".take();" + assignments;
		} else {
			if (this.stageNumber > 1) {
				threadInput += "this."
						+ this.pipelineHandler.getPipelineStages().get(
								this.stageNumber - 2).outArg[0] + " = queueOut"
						+ (this.stageNumber - 1) + ".take();";
			}
		}

		// Set Thread Output
		if (this.hasOutClass) {
			threadOutput += "JqOut" + (stageNumber) + " jq = new JqOut"
					+ (stageNumber) + "(" + outputFields + ");";
			for (String s : outArg) {
				if (!(isPrimitive(getVariableType(s)))) {
					threadOutput += "jq." + s + "=" + s + ".clone();";
				} else {
					threadOutput += "jq." + s + "=" + s + ";";
				}

			}
			threadOutput += "queueOut" + stageNumber + ".put((JqOut"
					+ this.stageNumber + ")(jq)); \n";
		} else {
			if ((stageNumber == numberOfStages)
					&& (this.outArg[0].toString().equals("null"))) {
				threadOutput += "";
			} else {
				threadOutput += "queueOut" + stageNumber + ".put("
						+ outArg[0].toString() + "); \n";
			}

		}

		// Set Loop Condition
		if (this.stageNumber == 1) {
			loopCondition = this.expressionOfWhileStatement;
		} else {
			loopCondition = "!((isPThread" + (stageNumber - 1)
					+ "Done ) && (queueOut" + (stageNumber - 1)
					+ ".isEmpty()))";
		}

		// Set Function
		for (Statement s : statement) {
			function += s.toString();
		}

		// set constraining condition
		if (this.stageNumber != 1) {
			constrainingCondition = "!queueOut" + (this.stageNumber - 1)
					+ ".isEmpty()";
		}

		// Set Run Method
		if (constrainingCondition.isEmpty()) {
			this.runMethod = "public void run() {" + "try { while ("
					+ loopCondition + ") {" + threadInput + function
					+ threadOutput
					+ "} } catch (Exception e) { e.printStackTrace();}"
					+ "isPThread" + (stageNumber) + "Done = true; ";
			if (stageNumber == numberOfStages) {
				runMethod += "jqBarrier.notifyBarrier(); }";
			} else {
				runMethod += "}";
			}
		} else {
			this.runMethod = "public void run() {" + "try { while ("
					+ loopCondition + ") { if(" + constrainingCondition + "){\n" + threadInput + function
					+ threadOutput
					+ "} } } catch (Exception e) { e.printStackTrace();}"
					+ "isPThread" + (stageNumber) + "Done = true; ";
			if (stageNumber == numberOfStages) {
				runMethod += "jqBarrier.notifyBarrier(); }";
			} else {
				runMethod += "}";
			}
		}

	}

	private void setThreadDeclaration() {
		String innerVariableList = "";
		innerVariableList += inputFields;
		if ((!innerVariableList.isEmpty()) && (!outputFields.isEmpty())) {
			innerVariableList += ",";
		}
		if (!outputFields.isEmpty()) {
			innerVariableList += outputFields + "\n";
		}
		this.threadDeclaration += "Thread threadAtStage" + stageNumber
				+ " = "+ this.classObjectName + ".new JPThread" + stageNumber + "(" + innerVariableList
				+ "); \n threadAtStage" + stageNumber + ".start();";
	}

	private void addQueueFields() {
		if (!((this.stageNumber == numberOfStages) && (this.outArg[0]
				.toString().equals("null")))) {

			if (this.stageNumber == numberOfStages) {
				if (this.hasOutClass) {
					this.queueType += "JqOut" + this.stageNumber;
					this.pipelineHandler.queueFileds
							.add("static BlockingQueue<JqOut" + stageNumber
									+ "> queueOut" + stageNumber
									+ " = new LinkedBlockingQueue<JqOut"
									+ stageNumber + ">();");

				} else {
					this.queueType = getQueueType(getVariableType(this.outArg[0]));
					this.pipelineHandler.queueFileds
							.add("static BlockingQueue<" + this.queueType
									+ "> queueOut" + stageNumber
									+ " = new LinkedBlockingQueue<"
									+ this.queueType + ">();");
				}
			} else {
				if (this.hasOutClass) {
					this.queueType += "JqOut" + this.stageNumber;
					this.pipelineHandler.queueFileds
							.add("static BlockingQueue<JqOut" + stageNumber
									+ "> queueOut" + stageNumber
									+ " = new ArrayBlockingQueue<JqOut"
									+ stageNumber + ">(10);");
				} else {
					this.queueType = getQueueType(getVariableType(this.outArg[0]));
					this.pipelineHandler.queueFileds
							.add("static BlockingQueue<" + this.queueType
									+ "> queueOut" + stageNumber
									+ " = new ArrayBlockingQueue<"
									+ this.queueType + ">(10);");
				}
			}
		} else {
			this.pipelineHandler.queueFileds.remove(this.stageNumber - 2);
			System.out.println(this.pipelineHandler.getPipelineStages().get(
					this.stageNumber - 2).queueType);
			this.pipelineHandler.queueFileds.add("static BlockingQueue<"
					+ this.pipelineHandler.getPipelineStages().get(
							this.stageNumber - 2).queueType
					+ "> queueOut"
					+ (stageNumber - 1)
					+ " = new LinkedBlockingQueue<"
					+ this.pipelineHandler.getPipelineStages().get(
							this.stageNumber - 2).queueType + ">();");
		}

	}

	private void setOutClass() {
		String variableDeclaration = "";
		String variableList = "";
		String variableInitialization = "";
		String constructor = "";

		// Set Variable Declaration
		for (String s : this.outArg) {
			variableDeclaration += getVariableType(s) + " " + s + ";";
		}

		// Set Variable List
		for (String s : this.outArg) {
			if (!variableList.isEmpty()) {
				variableList += ",";
			}
			variableList += getVariableType(s) + " " + s;
		}

		// Set Variable Initialization
		for (String s : this.outArg) {
			variableInitialization += "this." + s + " = " + s + ";";
		}
		// Set Constructor
		constructor = "JqOut" + stageNumber + "(" + variableList + ") {"
				+ variableInitialization + "}";

		// Set OutClass
		this.outClass = "class JqOut" + stageNumber + " {"
				+ variableDeclaration + constructor + "\n}";
	}

	private void setBarrierFields() {
		barrierFields += "boolean isPThread" + stageNumber
				+ "Done = false; \n";
	}

//	private void setOtherVariables() {
//		if (!otherVariableDeclarations.isEmpty()) {
//			otherVariableDeclarations += "static " + className
//					+ classObjectName + " = new " + className + "();";
//			otherVariableDeclarations += "static JqBarrier jqBarrier = "
//					+ classObjectName + ".new JqBarrier();";
//		}
//	}

	public String getVariableType(String variable) {
		String typeOfVariable = "";
		for (Statement s : statementsBeforePipeline) {
			if (s instanceof VariableDeclarationStatement) {
				SimpleNameVisitor visit = new SimpleNameVisitor();
				s.accept(visit);
				if (visit.getIdentifiers().contains(variable.trim())) {
					typeOfVariable = ((VariableDeclarationStatement) s)
							.getType().toString();
				}
			}
		}
		return typeOfVariable;
	}

	private void setThreadClass() {
		this.threadClass += this.threadClassDeclaration + " { \n "
				+ this.threadVariablesDeclaration + "\n"
				+ this.threadConstructor + "\n" + this.runMethod + "\n" + "}"
				+ "\n" + this.outClass;
	}

	public String getThreadClass() {
		return this.threadClass + "\n";
	}

	public String getThreadDeclaration(String className) {
		return this.threadDeclaration;
	}

	public String getQueueType(String type) {
		if (type.equals("byte")) {
			return "Byte";
		} else if (type.equals("short")) {
			return "Short";
		} else if (type.equals("int")) {
			return "Integer";
		} else if (type.equals("long")) {
			return "long";
		} else if (type.equals("float")) {
			return "Float";
		} else if (type.equals("double")) {
			return "Double";
		} else if (type.equals("char")) {
			return "Character";
		} else if (type.equals("boolean")) {
			return "Boolean";
		} else {
			return type;
		}
	}

	public boolean isPrimitive(String str) {
		boolean isPrimitive = false;
		String[] primitives = new String[8];
		primitives[0] = "byte";
		primitives[1] = "short";
		primitives[2] = "int";
		primitives[3] = "long";
		primitives[4] = "float";
		primitives[5] = "double";
		primitives[6] = "char";
		primitives[7] = "boolean";
		for (String s : primitives) {
			if (s.equals(str)) {
				isPrimitive = true;
				break;
			}
		}
		return isPrimitive;
	}
}