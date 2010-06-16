package org.eclipse.jconqurr.analyzer;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class MethodAnalyzer extends JQAnalyzer {
	public static final int SIMPLE_STATEMENT_WEIGHT = 2;
	public static final int COMPLEX_STATEMENT_WEIGHT = 4;
	public static final int METHOD_CALL_WEIGHT = 10;
	public static final int LOOP_WEIGHT = 8;
	
	private MethodDeclaration method;
	private boolean assumptionBasedAnalysis;
	private double methodWeight;
	
	public MethodAnalyzer(CompilationUnit cu) {
		super(cu);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void insertDirectives() {
		// TODO Auto-generated method stub
	}
	
	public void analyze() {
		// For test purposes only
		this.methodWeight = 7.19;
	}

	public void setAssumptionBasedAnalysis(boolean assumptionBasedAnalysis) {
		this.assumptionBasedAnalysis = assumptionBasedAnalysis;
	}

	public boolean isAssumptionBasedAnalysis() {
		return assumptionBasedAnalysis;
	}

	public void setMethod(MethodDeclaration method) {
		this.method = method;
	}

	public MethodDeclaration getMethod() {
		return method;
	}
	
	public double getWeightForMethod(MethodDeclaration method, boolean assumptionBased) {
		this.method = method;
		this.assumptionBasedAnalysis = assumptionBased;
		analyze();
		return methodWeight;
	}
	
	public double getMethodWeight() {
		return methodWeight;
	}
}
