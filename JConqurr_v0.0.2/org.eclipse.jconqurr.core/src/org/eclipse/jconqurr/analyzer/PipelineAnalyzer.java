package org.eclipse.jconqurr.analyzer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jconqurr.core.ast.visitors.MethodInvocationVisitor;
import org.eclipse.jconqurr.core.ast.visitors.ModifiedAssignmentVisitor;
import org.eclipse.jconqurr.core.ast.visitors.ModifiedSimpleNameVisitor;
import org.eclipse.jconqurr.core.ast.visitors.VariableDeclarationFragmentVisitor;
import org.eclipse.jconqurr.core.ast.visitors.WhileStatementVisitor;
import org.eclipse.jconqurr.rewrite.PipelineRewriter;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

public class PipelineAnalyzer extends JQAnalyzer {
	private MethodDeclaration pipelineMethod;
	private List<StageMarker> pipelineStageMarkers;
	private List<CandidateStageMarker> stageMarkerCandidates;

	public PipelineAnalyzer(CompilationUnit cu) {
		super(cu);
	}

	public void setPipelineMethod(MethodDeclaration pipelineMethod) {
		this.pipelineMethod = pipelineMethod;
	}

	public MethodDeclaration getPipelineMethod() {
		return pipelineMethod;
	}

	@SuppressWarnings("unchecked")
	public void insertDirectives() {
		if (pipelineMethod == null)
			throw new NullPointerException();
		// Mark the while statements of the method being analyzed
		WhileStatementVisitor wsVisitor = new WhileStatementVisitor();
		pipelineMethod.accept(wsVisitor);
		AST ast = this.rootAST;
		ASTRewrite rewriter = ASTRewrite.create(ast);
		PipelineRewriter pipeRewriter = new PipelineRewriter(this.unit);
		pipeRewriter.setImportDeclarations(rewriter);
		for (WhileStatement ws : wsVisitor.getWhileStatements()) {
			pipeRewriter.markPipelineStart(rewriter, ws);
			pipeRewriter.markPipelineEnd(rewriter, ws);
			analyzeWhileLoop(ws);
			for(StageMarker stg: pipelineStageMarkers)
				pipeRewriter.markPipelineStage(rewriter, ws,
						stg.candidateStatement, getArgumentListForPSD(stg));
		}
		// Rewrite changes
		pipeRewriter.rewriteChanges(rewriter, false);
		// By definition of Block.statements() method, all elements in the
		// returned list should be
		// of type Statement. Otherwise an exception is triggered. Therefore the
		// following cast is
		// safe for a valid list of statements.
		List<Statement> methodStatements = (List<Statement>) pipelineMethod
				.getBody().statements();
		for (Statement stmt : methodStatements) {
			System.out.println(stmt.toString());
		}
	}

	@SuppressWarnings("unchecked")
	public void analyzeWhileLoop(WhileStatement ws) {
		// These variables need to be re-instantiated each time this method
		// calls,
		// since they relate to a single loop
		stageMarkerCandidates = new ArrayList<CandidateStageMarker>();
		pipelineStageMarkers = new ArrayList<StageMarker>();
		// Initialize local variables
		MethodInvocationVisitor miVisitor = new MethodInvocationVisitor();
		MethodAnalyzer methodAnalyzer = new MethodAnalyzer(this.unit);
		List<Statement> loopStatements = null;
		IMethodBinding methodBinding = null;
		MethodDeclaration md = null;
		CandidateStageMarker csMarker = null;
		if (ws.getBody().getNodeType() == ASTNode.BLOCK) {
			loopStatements = ((Block) ws.getBody()).statements();
		}
		for (Statement stmt : loopStatements) {
			stmt.accept(miVisitor);
			for (MethodInvocation mi : miVisitor.getMethods()) {
				methodBinding = mi.resolveMethodBinding();
				md = getMethodDeclaration(methodBinding, true);
				if (md != null
						&& methodAnalyzer.getWeightForMethod(md, true) > 5.0) {
					csMarker = new CandidateStageMarker();
					csMarker.statement = stmt;
					csMarker.methodInvocation = mi;
					stageMarkerCandidates.add(csMarker);
				}
			}
			miVisitor.getMethods().clear();
		}
		// The first statement is automatically added since it is a certainty
		// however the pipeline stages are broken.
		// (The loop condition is the exit condition for the first stage of the
		// pipeline)
		CandidateStageMarker currentCSMarker = new CandidateStageMarker();
		StageMarker currentCandidate = new FirstStageMarker();
		currentCSMarker.statement = currentCandidate.candidateStatement = loopStatements.get(0);
		currentCSMarker.methodInvocation = null;
		StageMarker prevStgMkr = null;
		MethodInvocation mi = null;
		List<Expression> argumentList = new ArrayList<Expression>();
		int candidateIndex;
		Iterator<CandidateStageMarker> candidateStgMkrItr = stageMarkerCandidates.iterator();
		// The following sections needs further accuracy and fine-tuning
		while (currentCandidate != null) {
			Statement candidateStmt = currentCandidate.candidateStatement;
			candidateIndex = loopStatements.indexOf(candidateStmt);
			mi = currentCSMarker.methodInvocation;
			// initialize the argument list
			argumentList.clear();
			ModifiedAssignmentVisitor assgnVisitor = new ModifiedAssignmentVisitor();
			candidateStmt.accept(assgnVisitor);
			argumentList = assgnVisitor.getLeftHandSideExpressions();
			// 'mi == null' signifies the first statement.
			// Only output argument candidates will be added to list.
			// Input argument candidates for the first marker will be added
			// by the 'else' clause of the
			// 'if(prevStgMkr != null)' statement.
			if (mi != null) { 
				for (Expression arg : (List<Expression>) mi.arguments())
					argumentList.add(arg);
			}
			// Outputs of previous stage are always inputs of this stage, except
			// for first stage
			if (prevStgMkr != null) {
				currentCandidate.inputArgCandidates
						.addAll(prevStgMkr.outputArgCandidates);
				for (Expression arg : currentCandidate.inputArgCandidates) {
					int removeIndex = -1;
					for (Expression arg0 : argumentList) {
						if (arg0.toString().equals(arg.toString()))
							removeIndex = argumentList.indexOf(arg0);
					}
					try {
						argumentList.remove(removeIndex);
					} catch (ArrayIndexOutOfBoundsException e) {
						e.printStackTrace();
					}
				}
			} else { // prevStgMkr is null for the first stage
				VariableDeclarationFragmentVisitor vdfVisitor = new VariableDeclarationFragmentVisitor();
				MethodInvocationVisitor tempMIVisitor = new MethodInvocationVisitor();
				ws.getExpression().accept(vdfVisitor);
				for (VariableDeclarationFragment vdf : vdfVisitor
						.getVariableDeclarationFragments()) {
					((FirstStageMarker) currentCandidate).inputArgsFrmLoop
							.add(vdf.getName());
				}
				ws.getExpression().accept(tempMIVisitor);
				for (MethodInvocation tempMI : tempMIVisitor.getMethods()) {
					if (tempMI.getExpression() != null) {
						ModifiedSimpleNameVisitor tempVisitor = new ModifiedSimpleNameVisitor();
						tempMI.accept(tempVisitor);
						((FirstStageMarker) currentCandidate).inputArgsFrmLoop
								.addAll(tempVisitor.getSimpleNames());
					}
				}
			}
			// Check for references of any of the arguments below. If references
			// exist,
			// flag it as a candidate for output.
			ModifiedSimpleNameVisitor snVisitor = new ModifiedSimpleNameVisitor();
			for (Statement currentStmt : loopStatements.subList(candidateIndex,
					loopStatements.size() - 1)) {
				List<String> stmtIdentifierList = getSelectedIdentifiers(currentStmt);
				for (Expression argument : argumentList) {
					snVisitor.getSimpleNames().clear();
					argument.accept(snVisitor);
					// This loop only checks whether the argument name/ variable
					// name
					// is present in the statement. It checks for only one
					// argument
					// inside a single statement
					for (String identifier : snVisitor.getIdentifiers()) {
						if (stmtIdentifierList.contains(identifier)) {
							currentCandidate.outputArgCandidates.add(argument);
							break;
						}
					}
				}
				for (Expression arg : currentCandidate.outputArgCandidates)
					argumentList.remove(arg);
				// If the current candidate still does not have any output
				// arguments
				// and if we have reached the next candidate stage marker, then
				// that stage
				// marker cannot be kept further, since the stage above it needs
				// to access output variables
				// beyond the next candidate marker.
				Statement nextCandidate = getNextCandidateStatement(
						currentStmt, loopStatements);
				if (nextCandidate != null) {
					if (currentCandidate.outputArgCandidates.isEmpty()
							&& nextCandidate.equals(currentStmt))
						stageMarkerCandidates.remove(nextCandidate);
				}
			}
			// If any arguments are left out after sorting ouputs and inputs
			// from previous
			// add the remaining as direct inputs that are not coming through
			// the pipeline
			for (Expression arg : argumentList) {
				currentCandidate.inputArgCandidates.add(arg);
				argumentList.remove(arg);
			}
			// add as a marker, and prepare for the next iteration
			pipelineStageMarkers.add(currentCandidate);
			try {
				prevStgMkr = (StageMarker)currentCandidate.clone();
			} catch (CloneNotSupportedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (candidateStgMkrItr.hasNext()) {
				currentCSMarker = candidateStgMkrItr.next();
				currentCandidate = new StageMarker();
				currentCandidate.candidateStatement = currentCSMarker.statement;
			} else {
				currentCandidate = null;
			}
		}
		// prune unsuitable candidates
		for(StageMarker stageMarker: pipelineStageMarkers) {
			// Prune from input args
			List<Expression> argsToDump = new ArrayList<Expression>();
			ModifiedSimpleNameVisitor snVisitor = new ModifiedSimpleNameVisitor();
			for(Expression exp: stageMarker.inputArgCandidates) {
				exp.accept(snVisitor);
				for(SimpleName sn: snVisitor.getSimpleNames())
					if(sn.resolveBinding().getKind() != IBinding.VARIABLE)
						argsToDump.add(exp);
			}
			stageMarker.inputArgCandidates.removeAll(argsToDump);
			argsToDump.clear();
			snVisitor.getSimpleNames().clear();
			// Prune from output args
			for(Expression exp: stageMarker.outputArgCandidates) {
				exp.accept(snVisitor);
				for(SimpleName sn: snVisitor.getSimpleNames())
					if(sn.resolveBinding().getKind() != IBinding.VARIABLE)
						argsToDump.add(exp);
			}
			stageMarker.outputArgCandidates.removeAll(argsToDump);
			// If tis a FirstStageMarker, prune from its inputs
			if(stageMarker instanceof FirstStageMarker) {
				List<SimpleName> argsFrmLoopToDump = new ArrayList<SimpleName>();				
				for(SimpleName sn: ((FirstStageMarker)stageMarker).inputArgsFrmLoop) {
					if(sn.resolveBinding().getKind() != IBinding.VARIABLE)
						argsFrmLoopToDump.add(sn);
				}
				((FirstStageMarker)stageMarker).inputArgsFrmLoop.removeAll(argsFrmLoopToDump);
			}
		}
		// clear the class variables which are no longer needed and relevant,
		// to avoid accidents
		stageMarkerCandidates = null;
	}

	private Statement getNextCandidateStatement(Statement stmt,
			List<Statement> loopStatements) {
		int currentIndex = loopStatements.indexOf(stmt);
		if (currentIndex < 0)
			throw new IllegalArgumentException();
		while (++currentIndex < loopStatements.size()) {
			for(CandidateStageMarker csMarker: stageMarkerCandidates) {
				if(csMarker.statement.equals(loopStatements.get(currentIndex)))
					return loopStatements.get(currentIndex);
			}
		}

		return null;
	}

	private List<Expression> getAssigmentLHS(Statement stmt) {
		ModifiedAssignmentVisitor assgnVisitor = new ModifiedAssignmentVisitor();
		stmt.accept(assgnVisitor);
		return assgnVisitor.getLeftHandSideExpressions();
	}

	@SuppressWarnings("unchecked")
	private List<Expression> getMethodInvocationArguments(Statement stmt) {
		List<Expression> argumentList = new ArrayList<Expression>();
		MethodInvocationVisitor miVisitor = new MethodInvocationVisitor();
		stmt.accept(miVisitor);
		for (MethodInvocation mi : miVisitor.getMethods())
			argumentList.addAll(mi.arguments());
		return argumentList;
	}

	private List<String> getSelectedIdentifiers(Statement stmt) {
		ModifiedSimpleNameVisitor snVisitor = new ModifiedSimpleNameVisitor();
		for (Expression lhs : getAssigmentLHS(stmt)) {
			lhs.accept(snVisitor);
		}
		for (Expression argument : getMethodInvocationArguments(stmt)) {
			argument.accept(snVisitor);
		}
		return snVisitor.getIdentifiers();
	}

	private List<String> getArgumentListForPSD(StageMarker stageMarker) {
		List<String> argList = new ArrayList<String>(2);
		// prepare the input string
		String strInputArg = "";
		for(SimpleName sn: stageMarker.getInputSimpleNames()) {
			if(!strInputArg.isEmpty())
				strInputArg += ",";
			strInputArg += sn.getIdentifier();
		}
		argList.add(strInputArg);
		// prepare the output string
		String strOutputArg = "";
		for(SimpleName sn: stageMarker.getOutputSimpleNames()) {
			if(!strOutputArg.isEmpty())
				strOutputArg += ",";
			strOutputArg += sn.getIdentifier();
		}
		argList.add(strOutputArg);
		return argList;
	}
	
	private class StageMarker implements Cloneable {
		Statement candidateStatement;
		List<Expression> inputArgCandidates;
		List<Expression> outputArgCandidates;

		public StageMarker() {
			// TODO Auto-generated constructor stub
			this.inputArgCandidates = new ArrayList<Expression>();
			this.outputArgCandidates = new ArrayList<Expression>();
		}
		
		@Override
		protected Object clone() throws CloneNotSupportedException {
			// TODO Auto-generated method stub
			StageMarker copy = new StageMarker();
			copy.candidateStatement = this.candidateStatement;
			for(Expression inArg: this.inputArgCandidates)
				copy.inputArgCandidates.add(inArg);
			for(Expression outArg: this.outputArgCandidates)
				copy.outputArgCandidates.add(outArg);
			return copy;
		}
		
		protected List<SimpleName> getInputSimpleNames() {
			ModifiedSimpleNameVisitor snVisitor = new ModifiedSimpleNameVisitor();
			for(Expression exp: this.inputArgCandidates) {
				exp.accept(snVisitor);
			}
			return snVisitor.getSimpleNames();
		}
		
		protected List<SimpleName> getOutputSimpleNames() {
			ModifiedSimpleNameVisitor snVisitor = new ModifiedSimpleNameVisitor();
			for(Expression exp: this.outputArgCandidates) {
				exp.accept(snVisitor);
			}
			return snVisitor.getSimpleNames();
		}
	}

	private class FirstStageMarker extends StageMarker {
		List<SimpleName> inputArgsFrmLoop;

		public FirstStageMarker() {
			// TODO Auto-generated constructor stub
			super();
			inputArgsFrmLoop = new ArrayList<SimpleName>();
		}
		
		@Override
		protected List<SimpleName> getInputSimpleNames() {
			// TODO Auto-generated method stub
			List<SimpleName> simpleNames = super.getInputSimpleNames();
			simpleNames.addAll(this.inputArgsFrmLoop);
			return simpleNames;
		}
	}
	
	private class CandidateStageMarker {
		Statement statement;
		MethodInvocation methodInvocation;
	}
}
