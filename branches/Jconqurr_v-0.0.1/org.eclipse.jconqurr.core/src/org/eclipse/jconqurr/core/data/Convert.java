package org.eclipse.jconqurr.core.data;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jconqurr.core.ast.visitors.AnnotationVisitor;
import org.eclipse.jconqurr.core.ast.visitors.CompilationUnitParser;
import org.eclipse.jconqurr.core.ast.visitors.ExpressionStatementVisitor;
import org.eclipse.jconqurr.core.ast.visitors.ForLoopVisitor;
import org.eclipse.jconqurr.core.ast.visitors.MethodVisitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.UndoEdit;
import org.eclipse.ui.handlers.HandlerUtil;

public class Convert extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// TODO Auto-generated method stub
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil
		.getActiveMenuSelection(event);
		Object firstElement = selection.getFirstElement();

		if (firstElement instanceof ICompilationUnit) {
			ICompilationUnit cu = (ICompilationUnit) firstElement;
			CompilationUnit unit = CompilationUnitParser.parse(cu);
			MethodVisitor methodVisitor = new MethodVisitor();
			ForLoopVisitor loopVisitor = new ForLoopVisitor();

			ForLoopModifier flModifier = new ForLoopModifier();
			flModifier.setCompilationUnit(unit);
			flModifier.analyzeCode();
			//vist the methods
			unit.accept(methodVisitor);

			for(MethodDeclaration method: methodVisitor.getMethods()) {
				IAnnotationBinding[] annotationBinding =  method.resolveBinding().getAnnotations();
				if(annotationBinding.length>0) {
					for(int i=0; i<annotationBinding.length;i++) {
						if(annotationBinding[i].getName().equals("ParallelFor")) {
							method.accept(loopVisitor);
							for(ForStatement forLoop: loopVisitor.getForLoops()) {
								String strExpression = forLoop.getExpression().toString();
								String regex = "[<>[<=][>=]]";
								String result[] = {};
								result = strExpression.split(regex);
								Integer conditionInt = new Integer(0);

								for(int j=0; j<result.length; j++) {
									if(isParsableToInt(result[j].trim())) { 
										conditionInt = Integer.parseInt(result[j].trim());
										break;
									}
								}
								int newCondition1 = (int)conditionInt/2;
								//int newCondition2 = (int)conditionInt;

								try {
									ExpressionStatementVisitor exprStmtVisitor = new ExpressionStatementVisitor();
									Block block = method.getBody();
									block.accept(exprStmtVisitor);
									for(ExpressionStatement exprStmt: exprStmtVisitor.getExpressionStatements()) {
										System.out.println(exprStmt.toString());
									}

									ITextFileBufferManager bufferManager = FileBuffers.getTextFileBufferManager(); // get the buffer manager
									IPath path = unit.getJavaElement().getPath(); // unit: instance of CompilationUnit
									try {
										bufferManager.connect(path,LocationKind.IFILE, null); // (1)
										ITextFileBuffer textFileBuffer = bufferManager.getTextFileBuffer(path, LocationKind.IFILE);
										// retrieve the buffer
										IDocument document = textFileBuffer.getDocument();
										// ... edit the document here ... 
										AST ast = unit.getAST();
										ImportDeclaration id = ast.newImportDeclaration();
										id.setName(ast.newName(new String[] {"java", "util", "Set"}));
										ASTRewrite tempRewriter = ASTRewrite.create(ast);
										TypeDeclaration td = (TypeDeclaration) unit.types().get(0);
										ITrackedNodePosition tdLocation = tempRewriter.track(td);
										ListRewrite lrw = tempRewriter.getListRewrite(unit, CompilationUnit.IMPORTS_PROPERTY);
										lrw.insertLast(id, null);
										TextEdit edits = tempRewriter.rewriteAST(document, null);
										UndoEdit undo = edits.apply(document);

										// commit changes to underlying file
										textFileBuffer
										.commit(null /* ProgressMonitor */, false /* Overwrite */); // (3)

									} finally {
										bufferManager.disconnect(path, LocationKind.IFILE, null); // (4)
									}

								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}
					}
				}
			}

		} else {
			MessageDialog.openInformation(HandlerUtil.getActiveShell(event),
					"Information", "Please select a java source file");
		}

		return null;
	}

	private boolean isParsableToInt(String s) {
		try {
			Integer.parseInt(s);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
}

