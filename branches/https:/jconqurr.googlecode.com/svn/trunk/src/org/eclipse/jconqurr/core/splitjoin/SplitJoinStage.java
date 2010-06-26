package org.eclipse.jconqurr.core.splitjoin;

import java.util.List;
import org.eclipse.jconqurr.core.ast.visitors.SimpleNameVisitor;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public class SplitJoinStage {
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
	// public static String barrierFields = "";
	String inArg[];
	String outArg[];
	String inputFields = "";
	String outputFields = "";

	private String threadDeclaration = "";
	SplitJoinHandler splitJoinHandler;
	boolean hasInClass;
	boolean hasOutClass;
	String outClass = "";

	// static String queueFields = "";
	private String otherVariableDeclarations = "";
	static String className = "";
	String classObjectName = "";
	String queueType = "";
	private boolean isSplitStage;
	private String jqSplitCounter = "";

	public SplitJoinStage(MethodInvocation directiveStage,
			List<Statement> statement, List<Statement> statementBeforePipeline,
			String expressionOfWhileStatement, int stageNumber,
			SplitJoinHandler handler, boolean isSplitStage) {
		this.directiveStage = directiveStage;
		this.statement = statement;
		this.statementsBeforePipeline = statementBeforePipeline;
		this.expressionOfWhileStatement = expressionOfWhileStatement;
		this.stageNumber = stageNumber;
		this.splitJoinHandler = handler;
		this.isSplitStage = isSplitStage;
	}

	public void processStage(String name) {
		if (classObjectName.isEmpty()) {
			classObjectName = "jq" + name;
		}

		MethodInvocation m = directiveStage;
		StringLiteral input = (StringLiteral) m.arguments().get(0);
		StringLiteral output = (StringLiteral) m.arguments().get(1);
		if (m.arguments().size() == 3) {
			StringLiteral jqspltcnt = (StringLiteral) m.arguments().get(2);
			this.jqSplitCounter = jqspltcnt.getLiteralValue();
			this.splitJoinHandler.jqSplitCounter = this.jqSplitCounter;
		}

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
			this.queueType = "JQOut" + this.stageNumber;
			setOutClass();
		}
		
		else{
			this.queueType = getQueueType(getVariableType(this.outArg[0]));
		}

		if ((this.stageNumber != 1)
				&& (splitJoinHandler.getSplitJoinStages().get(stageNumber - 2).outArg.length > 1)) {
			hasInClass = true;
		}

		// setOtherVariables();
		setThreadClassDeclaration();
		setThreadVariablesDeclaration();
		setThreadConstructor();
		addQueueFields();

		setRunMethod();
		setThreadDeclaration();
		setBarrierFields();

		setThreadClass();
	}

	private void setThreadClassDeclaration() {
		threadClassDeclaration = "private class JPThread" + stageNumber
				+ " extends Thread";
	}

	private void setThreadVariablesDeclaration() {
		for (String s : inArg) {
			if (!inArg[0].toString().equals("null")) {
				this.threadVariablesDeclaration += getVariableType(s) + " " + s
						+ ";";
			}
		}
		for (String s : outArg) {
			if (!outArg[0].toString().equals("null")) {
				this.threadVariablesDeclaration += getVariableType(s) + " " + s
						+ ";";
			}
		}
		if (this.isSplitStage) {
			this.threadVariablesDeclaration += "int threadId;";
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

		if (this.isSplitStage) {
			if (!variableList.isEmpty()) {
				variableList += ", ";
			}
			variableList += "int id";
		}

		// Set Variable Initialization
		for (String s : inArg) {
			variableInitialization += "this." + s.trim() + "= " + s + ";";
		}
		for (String s : outArg) {
			if (!outArg[0].toString().equals("null")) {
				variableInitialization += "this." + s.trim() + "= " + s + ";";
			}
		}

		if (this.isSplitStage) {
			variableInitialization += "this.threadId = id;";
		}
		threadConstructor = "public JPThread" + (stageNumber) + "("
				+ variableList + ") {" + variableInitialization + "}";
	}

	private void setRunMethod() {
		//String assignments = "";
		String threadInput = "";
		String threadOutput = "";
		String loopCondition = "";
		String constrainingCondition = "";
		String function = "";
		String barrierCondition = "";

		// Set Loop Condition
		if (this.stageNumber == 1) {
			loopCondition = this.expressionOfWhileStatement;
		} else {
			loopCondition = "!(isPThread" + (stageNumber - 1) + "Done )";
		}

		// Set Function
		for (Statement s : statement) {
			function += s.toString();
		}

		if ((this.stageNumber != numberOfStages)
				&& (this.splitJoinHandler.getSplitJoinStages().get(
						this.stageNumber).isSplitStage)) {
			threadInput += setThrdIpForStageBeforeSplit();
			threadOutput += setThrdOpForStageBeforeSplit();
			constrainingCondition += setConstrainingConditionForStageBeforeSplit();
			barrierCondition += setBarrierConditionForStageBeforeSplit();
		}

		else if (this.isSplitStage) {
			threadInput += setThrdIpForSplitStage();
			threadOutput += setThrdOpForSplitStage();
			constrainingCondition += setConstrainingConditionForSplitStage();
			barrierCondition += setBarrierConditionForSplitStage();
		}

		else if ((this.stageNumber != 1)
				&& (this.splitJoinHandler.getSplitJoinStages().get(
						this.stageNumber - 2).isSplitStage)) {
			threadInput += setThrdIpForStageAfterSplit();
			threadOutput += setThrdOpForStageAfterSplit();
			constrainingCondition += setConstrainingConditionForStageAfterSplit();
			barrierCondition += setBarrierConditionForStageAfterSplit();
		}

		else {
			threadInput += setThrdIpForOtherStages();
			threadOutput += setThrdOpForOtherStages();
			constrainingCondition += setConstrainingConditionForOtherStages();
			barrierCondition += setBarrierConditionForOtherStages();
		}

		if (!constrainingCondition.isEmpty()) {
			this.runMethod += "public void run() {\n";
			
			if ((this.stageNumber != numberOfStages)
					&& (this.splitJoinHandler.getSplitJoinStages().get(
							this.stageNumber).isSplitStage)) {
				this.runMethod += "boolean allQueuesEmpty = false;\nint jqCounter = jqSplitCounter - 1;";
			} 
			else if (this.isSplitStage) {
				this.runMethod += "boolean allQueuesEmpty = false;\n";
			} 
			else if ((this.stageNumber != 1)
					&& (this.splitJoinHandler.getSplitJoinStages().get(
							this.stageNumber - 2).isSplitStage)) {
				this.runMethod += "boolean allQueuesEmpty = false;\n";
				this.runMethod += "int jqCounter = 0;\n";
			}
			
			this.runMethod += "try {\nwhile (" + loopCondition + ") {\nif ("
					+ constrainingCondition + ") {\n";
			this.runMethod += threadInput + function + "\n" + threadOutput;
			if ((this.stageNumber != 1)
					&& (this.splitJoinHandler.getSplitJoinStages().get(
							this.stageNumber - 2).isSplitStage)) {
				this.runMethod += "jqCounter++;\n";
			}
			this.runMethod += "}\n}\n} catch (Exception e) {\ne.printStackTrace();\n}\n"
					+ barrierCondition
					+ "isPThread"
					+ this.stageNumber
					+ "Done = true;";
			if (this.stageNumber == numberOfStages) {
				this.runMethod += "jqBarrier.notifyBarrier();\n";
			}
			this.runMethod += "}\n";
		} else {
			this.runMethod += "public void run() {\n";
			if ((this.stageNumber != numberOfStages)
					&& (this.splitJoinHandler.getSplitJoinStages().get(
							this.stageNumber).isSplitStage)) {
				this.runMethod += "boolean allQueuesEmpty = false;\nint jqCounter = jqSplitCounter - 1;";
			} else if (this.isSplitStage) {
				this.runMethod += "boolean allQueuesEmpty = false;\n";
			} else if ((this.stageNumber != 1)
					&& (this.splitJoinHandler.getSplitJoinStages().get(
							this.stageNumber - 2).isSplitStage)) {
				this.runMethod += "int jqCounter = 0;\n";
			}
			this.runMethod += "try {\nwhile (" + loopCondition + ") {\n"
					+ threadInput + function + threadOutput
					+ "\n}\n} catch (Exception e) {\ne.printStackTrace();\n}\n"
					+ barrierCondition + "isPThread" + this.stageNumber
					+ "Done = true;";
			if (this.stageNumber == numberOfStages) {
				this.runMethod += "jqBarrier.notifyBarrier();\n";
			}
			this.runMethod += "}\n";
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

		if (this.isSplitStage) {
			this.threadDeclaration += "Thread [] threadAtStage" + stageNumber
					+ " = new Thread[jqSplitCounter];";
			this.threadDeclaration += "\n for(int jc=0; jc<jqSplitCounter; jc++){\nthreadAtStage"
					+ this.stageNumber
					+ "[jc] = "
					+ this.classObjectName
					+ ".new JPThread"
					+ this.stageNumber
					+ "("
					+ innerVariableList + ", jc); \n}";
			this.threadDeclaration += "\nfor(int jc=0; jc<jqSplitCounter; jc++){\nthreadAtStage"
					+ this.stageNumber + "[jc].start();\n}\n";
		} else {
			this.threadDeclaration += "Thread threadAtStage" + stageNumber
					+ " = " + classObjectName + ".new JPThread" + stageNumber
					+ "(" + innerVariableList + "); \n threadAtStage"
					+ stageNumber + ".start();";
		}
	}

	private void addQueueFields() {
		if ((this.stageNumber != numberOfStages) && (this.splitJoinHandler.getSplitJoinStages().get(this.stageNumber).isSplitStage)){
			addQueueFieldsForStageBeforeSplit();
		}else if(this.isSplitStage){
			addQueueFieldsForSplitStage();
		}else{
			addQueueFieldsForOtherStages();
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
			variableInitialization += "this." + s.trim() + " = " + s + ";";
		}
		// Set Constructor
		constructor = "JqOut" + stageNumber + "(" + variableList + ") {"
				+ variableInitialization + "}";

		// Set OutClass
		this.outClass = "class JqOut" + stageNumber + " {"
				+ variableDeclaration + constructor + "\n}";
	}

	private void setBarrierFields() {
		this.splitJoinHandler.barrierFieldsList.add("static boolean isPThread"
				+ stageNumber + "Done = false;");
		/*
		 * barrierFields += "static boolean isPThread" + stageNumber +
		 * "Done = false; \n";
		 */
	}

	protected void setOtherVariables() {
		if (this.splitJoinHandler.commonVariablesList.isEmpty()) {
			this.splitJoinHandler.commonVariablesList.add("static "
					+ this.splitJoinHandler.getClassName() + " "
					+ classObjectName + " = new "
					+ this.splitJoinHandler.getClassName() + "();\n");
			this.splitJoinHandler.commonVariablesList
					.add("static JqBarrier jqBarrier = " + classObjectName
							+ ".new JqBarrier();");
			this.splitJoinHandler.commonVariablesList
					.add("static final int jqSplitCounter = "
							+ this.splitJoinHandler.jqSplitCounter + ";");
		}
	}

	private String setThrdIpForStageBeforeSplit() {
		String assignments = "";
		String threadInput = "";

		threadInput += "jqCounter++; \n";

		if (this.hasInClass) {
			for (String s : this.splitJoinHandler.getSplitJoinStages().get(
					this.stageNumber - 2).outArg) {
				assignments += s + " = " + "jq." + s + ";";
			}
			threadInput += "JqOut" + (stageNumber - 1) + " jq = queueOut"
					+ (stageNumber - 1) + ".take();" + assignments;
		} else {
			if (this.stageNumber > 1) {
				threadInput += "this."
						+ this.splitJoinHandler.getSplitJoinStages().get(
								this.stageNumber - 2).outArg[0].trim()
						+ " = queueOut" + (this.stageNumber - 1) + ".take();";
			}
		}
		return threadInput;
	}

	private String setThrdIpForSplitStage() {
		String assignments = "";
		String threadInput = "";

		if (this.hasInClass) {
			for (String s : this.splitJoinHandler.getSplitJoinStages().get(
					this.stageNumber - 2).outArg) {
				assignments += s + " = " + "jq." + s + ";";
			}
			threadInput += "JqOut" + (stageNumber - 1) + " jq = queueOut"
					+ (stageNumber - 1) + "[this.threadId].take();" + assignments;
		} else {
			if (this.stageNumber > 1) {
				threadInput += "this."
						+ this.splitJoinHandler.getSplitJoinStages().get(
								this.stageNumber - 2).outArg[0].trim()
						+ " = queueOut" + (this.stageNumber - 1) + "[this.threadId].take();";
			}
		}
		return threadInput;
	}

	private String setThrdIpForStageAfterSplit() {
		String assignments = "";
		String threadInput = "";

		//setOtherVariables();

		if (this.hasInClass) {
			for (String s : this.splitJoinHandler.getSplitJoinStages().get(
					this.stageNumber - 2).outArg) {
				assignments += s + " = " + "jq." + s + ";";
			}
			threadInput += "JqOut" + (stageNumber - 1) + " jq = queueOut"
					+ (stageNumber - 1) + "[jqCounter % jqSplitCounter].take();" + assignments;
		} else {
			if (this.stageNumber > 1) {
				threadInput += "this."
						+ this.splitJoinHandler.getSplitJoinStages().get(
								this.stageNumber - 2).outArg[0].trim()
						+ " = queueOut" + (this.stageNumber - 1) + "[jqCounter % jqSplitCounter].take();";
			}
		}
		return threadInput;
	}

	private String setThrdIpForOtherStages() {
		String assignments = "";
		String threadInput = "";

		if (this.hasInClass) {
			for (String s : this.splitJoinHandler.getSplitJoinStages().get(
					this.stageNumber - 2).outArg) {
				assignments += s + " = " + "jq." + s + ";";
			}
			threadInput += "JqOut" + (stageNumber - 1) + " jq = queueOut"
					+ (stageNumber - 1) + ".take();" + assignments;
		} else {
			if (this.stageNumber > 1) {
				threadInput += "this."
						+ this.splitJoinHandler.getSplitJoinStages().get(
								this.stageNumber - 2).outArg[0].trim()
						+ " = queueOut" + (this.stageNumber - 1) + ".take();";
			}
		}
		return threadInput;
	}

	private String setThrdOpForStageBeforeSplit() {
		String threadOutput = "";

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
			threadOutput += "queueOut" + stageNumber
					+ "[jqCounter % jqSplitCounter].put((JqOut"
					+ this.stageNumber + ")(jq)); \n";
		} else {
			if ((stageNumber == numberOfStages)
					&& (this.outArg[0].toString().equals("null"))) {
				threadOutput += "";
			} else {
				threadOutput += "queueOut" + stageNumber
						+ "[jqCounter % jqSplitCounter].put("
						+ outArg[0].toString() + "); \n";
			}

		}
		return threadOutput;
	}

	private String setThrdOpForSplitStage() {
		String threadOutput = "";

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
			threadOutput += "queueOut" + stageNumber
					+ "[this.threadId].put((JqOut" + this.stageNumber
					+ ")(jq)); \n";
		} else {
			if ((stageNumber == numberOfStages)
					&& (this.outArg[0].toString().equals("null"))) {
				threadOutput += "";
			} else {
				threadOutput += "queueOut" + stageNumber
						+ "[this.threadId].put(" + outArg[0].toString()
						+ "); \n";
			}
		}
		return threadOutput;
	}

	private String setThrdOpForStageAfterSplit() {
		String threadOutput = "";

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
					+ this.stageNumber + ")(jq)); \n jqCounter++; \n";
		} else {
			if ((stageNumber == numberOfStages)
					&& (this.outArg[0].toString().equals("null"))) {
				threadOutput += "";
			} else {
				threadOutput += "queueOut" + stageNumber + ".put("
						+ outArg[0].toString() + "); \n";
			}
		}
		return threadOutput;
	}

	private String setThrdOpForOtherStages() {
		String threadOutput = "";

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

		return threadOutput;
	}

	private String setConstrainingConditionForStageBeforeSplit() {
		String constrainingCondition = "";

		if (this.stageNumber != 1) {
			constrainingCondition = "!(queueOut" + (this.stageNumber - 1)
					+ ".isEmpty())";
		}
		return constrainingCondition;
	}

	private String setConstrainingConditionForSplitStage() {
		String constrainingCondition = "";

		constrainingCondition = "!(queueOut" + (this.stageNumber - 1)
				+ "[this.threadId].isEmpty())";
		return constrainingCondition;
	}

	private String setConstrainingConditionForStageAfterSplit() {
		String constrainingCondition = "";

		constrainingCondition = "!(queueOut" + (this.stageNumber - 1)
				+ "[jqCounter % jqSplitCounter].isEmpty())";
		return constrainingCondition;
	}

	private String setConstrainingConditionForOtherStages() {
		String constrainingCondition = "";

		if (this.stageNumber != 1) {
			constrainingCondition = "!(queueOut" + (this.stageNumber - 1)
					+ ".isEmpty())";
		}

		return constrainingCondition;
	}

	private String setBarrierConditionForStageBeforeSplit() {
		String barrierCondition = "";

		barrierCondition += "while (!allQueuesEmpty) {\nallQueuesEmpty = true;\nfor (BlockingQueue<"
				+ this.queueType
				+ "> queue : queueOut"
				+ this.stageNumber
				+ ")\nif (!queue.isEmpty()) {\nallQueuesEmpty = false;\nbreak;\n}\nThread.yield();\n}\n";
		return barrierCondition;
	}

	private String setBarrierConditionForSplitStage() {
		String barrierCondition = "";

		barrierCondition += "while (!allQueuesEmpty) {\nallQueuesEmpty = true;\nfor (BlockingQueue<"
				+ this.queueType
				+ "> queue : queueOut"
				+ this.stageNumber
				+ ")\nif (!queue.isEmpty()) {\nallQueuesEmpty = false;\nbreak;\n}\nThread.yield();\n}\n";
		return barrierCondition;
	}

	private String setBarrierConditionForStageAfterSplit() {
		String barrierCondition = "";

		barrierCondition += "while (!allQueuesEmpty) {\nallQueuesEmpty = true;\nfor (BlockingQueue<"
			+ this.splitJoinHandler.getSplitJoinStages().get(this.stageNumber-2).queueType
			+ "> queue : queueOut"
			+ (this.stageNumber-1)
			+ ")\nif (!queue.isEmpty()) {\nallQueuesEmpty = false;\nbreak;\n}\nThread.yield();\n}\n";
		
		if (this.stageNumber != numberOfStages) {
			barrierCondition += "while(!queueOut" + this.stageNumber
					+ ".isEmpty()) {\nThread.yield();\n}\n";
		}
	return barrierCondition;
	}

	private String setBarrierConditionForOtherStages() {
		String barrierCondition = "";

		if (this.stageNumber != numberOfStages) {
			barrierCondition += "while(!queueOut" + this.stageNumber
					+ ".isEmpty()) {\nThread.yield();\n}\n";
		}
		return barrierCondition;
	}

	private void addQueueFieldsForStageBeforeSplit() {
		String queueFields = "";
		
			
			queueFields += "static BlockingQueue<" + this.queueType + "> [] queueOut" + this.stageNumber
					+ " = new BlockingQueue[jqSplitCounter];";

			queueFields += "\nstatic { \n for(int jc = 0; jc < jqSplitCounter; jc++) {\n queueOut"
					+ this.stageNumber
					+ " [jc] = new ArrayBlockingQueue<" + this.queueType + ">(10); \n} \n }";

		
		this.splitJoinHandler.queueFieldsList.add(queueFields);
	}

	private void addQueueFieldsForSplitStage() {
		String queueFields = "";
		
			queueFields += "static BlockingQueue<" + this.queueType + "> [] queueOut" + this.stageNumber
					+ " = new BlockingQueue[jqSplitCounter];";

			queueFields += "\nstatic { \n for(int jc = 0; jc < jqSplitCounter; jc++) {\n queueOut"
					+ this.stageNumber
					+ " [jc] = new ArrayBlockingQueue<" + this.queueType + ">(10); \n} \n }";

		
		this.splitJoinHandler.queueFieldsList.add(queueFields);
	}

	private void addQueueFieldsForOtherStages() {
		String queueFields = "";

		if (!((this.stageNumber == numberOfStages) && (this.outArg[0].toString().equals("null")))) {
			if(this.stageNumber == numberOfStages){
				queueFields += "static BlockingQueue<" + this.queueType + "> queueOut" + this.stageNumber
				+ " = new LinkedBlockingQueue<" + this.queueType + ">(); \n";	
			}
			else{
				queueFields += "static BlockingQueue<" + this.queueType + "> queueOut" + this.stageNumber
				+ " = new ArrayBlockingQueue<" + this.queueType + ">(10); \n";	
			}
						
		} else {
			this.splitJoinHandler.queueFieldsList.remove(this.stageNumber - 2);
			if ((this.stageNumber != 1) && (this.splitJoinHandler.getSplitJoinStages().get(this.stageNumber - 2).isSplitStage)) {				
				queueFields += "static BlockingQueue<" + this.splitJoinHandler.getSplitJoinStages().get(this.stageNumber-2).queueType + "> [] queueOut"
						+ (this.stageNumber-1)
						+ " = new BlockingQueue[jqSplitCounter];";

				queueFields += "static { \n for(int jc = 0; jc < jqSplitCounter; jc++) {\n queueOut"
						+ (this.stageNumber-1)
						+ " [jc] = new LinkedBlockingQueue<" + this.splitJoinHandler.getSplitJoinStages().get(this.stageNumber-2).queueType
						+ ">(10); \n} \n }";

			} else {
				queueFields += "static BlockingQueue<"
						+ this.splitJoinHandler.getSplitJoinStages().get(
								this.stageNumber - 2).queueType
						+ "> queueOut"
						+ (stageNumber - 1)
						+ " = new LinkedBlockingQueue<"
						+ this.splitJoinHandler.getSplitJoinStages().get(
								this.stageNumber - 2).queueType + ">();";

			}
		}
		this.splitJoinHandler.queueFieldsList.add(queueFields);
	}

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