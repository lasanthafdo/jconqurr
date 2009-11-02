package org.eclipse.jconqurr.core.parallel;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.runtime.IPath;
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
		System.out.println("Inside Execute method.....");

		IStructuredSelection selection = (IStructuredSelection) HandlerUtil
		.getActiveMenuSelection(event);
		Object firstElement = selection.getFirstElement();

		if (firstElement instanceof ICompilationUnit) {
			ICompilationUnit cu = (ICompilationUnit) firstElement;
			CompilationUnit parsedAST = parse(cu);
			MethodVisitor methodVisitor = new MethodVisitor();
			ForLoopVisitor loopVisitor = new ForLoopVisitor();

			//vist the methods
			parsedAST.accept(methodVisitor);

			for(MethodDeclaration method: methodVisitor.getMethods()) {
				IAnnotationBinding[] annotationBinding =  method.resolveBinding().getAnnotations();
				if(annotationBinding.length>0) {
					for(int i=0; i<annotationBinding.length;i++) {
						if(annotationBinding[i].getName().equals("ParallelFor")) {
							System.out.println("Annotation:" + annotationBinding[i].getName());
							method.accept(loopVisitor);
							for(ForStatement forLoop: loopVisitor.getForLoops()) {
								System.out.println("METHOD BODY:" + method.getBody().toString());

								String strExpression = forLoop.getExpression().toString();
								String regex = "[<>[<=][>=]]";
								String result[] = {};
								result = strExpression.split(regex);
								Integer conditionInt = new Integer(0);

								System.out.println("MAX:" + Integer.MAX_VALUE);								
								for(int j=0; j<result.length; j++) {
									if(isParsableToInt(result[j].trim())) { 
										conditionInt = Integer.parseInt(result[j].trim());
										break;
									}
									//									conditionInt = Integer.parseInt(result[i].trim());
									System.out.println("Result[" + j + "]:" + result[j]);
								}
								int newCondition1 = (int)conditionInt/2;
								//								int newCondition2 = (int)conditionInt;

								try {

									String code = "\n" + 
									"public class TestClass {\n" +
									"public void myMethod() {\n" + 
									"for(int i=0;i<" + newCondition1 + ";i++)";
									code = code + forLoop.getBody().toString();
									code = code + "\n} + \n}";
									System.out.println(code); 

									ForStatement newForStmt = forLoop;

									/*
									 * 
									 */
									ASTParser tempParser = ASTParser.newParser(AST.JLS3); // Java 5.0 and up
									tempParser.setKind(ASTParser.K_COMPILATION_UNIT);
									tempParser.setSource(code.toCharArray());
									tempParser.setResolveBindings(true);
									tempParser.setBindingsRecovery(true);
									CompilationUnit tempUnit = (CompilationUnit) tempParser.createAST(null /* IProgrssMonitor*/);

									System.out.println("Unit:\n" + tempUnit.toString());
									ForLoopVisitor tempLoopVisitor = new ForLoopVisitor();
									tempUnit.accept(tempLoopVisitor);
									for(ForStatement forStatmnt:tempLoopVisitor.getForLoops()) {
										System.out.println("Mod Loop:\n" + forStatmnt.toString());
									}
									/*
									 * 
									 */

									//newForStmt.setStructuralProperty(ForStatement.EXPRESSION_PROPERTY, ("i<" + newCondition1));
									ASTRewrite rewrite = ASTRewrite.create(parsedAST.getAST());
									System.out.println("ForStatemntProps:\n" + forLoop.properties());
									//forLoop.setExpression(newExpr);
									Block block = method.getBody();
									//Statement loopBody = (Statement)rewrite.createStringPlaceholder(forLoop.getBody().toString(), ASTNode.BLOCK);
									//newForStmt.setBody(loopBody);
									System.out.println(forLoop.getExpression());

									Block testStatement = (Block)rewrite.createStringPlaceholder(code, ASTNode.BLOCK);
									ListRewrite listRewrite = rewrite.getListRewrite(block, Block.STATEMENTS_PROPERTY);

									System.out.println("For Statement:\n" + newForStmt.toString());
									System.out.println("Test Statement:\n" + testStatement.toString());
									System.out.println(ExpressionStatement.propertyDescriptors(AST.JLS3));
									System.out.println("ForStatemntProps:\n" + ForStatement.propertyDescriptors(AST.JLS3));									
									listRewrite.insertAfter(tempUnit,forLoop,null);

									ITextFileBufferManager bufferManager = FileBuffers.getTextFileBufferManager(); // get the buffer manager
									IPath path = parsedAST.getJavaElement().getPath(); // unit: instance of CompilationUnit
									try {
										bufferManager.connect(path, null); // (1)
										ITextFileBuffer textFileBuffer = bufferManager.getTextFileBuffer(path);
										// retrieve the buffer
										IDocument document = textFileBuffer.getDocument();
										// ... edit the document here ... 
										AST ast = parsedAST.getAST();
										ImportDeclaration id = ast.newImportDeclaration();
										id.setName(ast.newName(new String[] {"java", "util", "Set"}));
										ASTRewrite tempRewriter = ASTRewrite.create(ast);
										TypeDeclaration td = (TypeDeclaration) parsedAST.types().get(0);
										ITrackedNodePosition tdLocation = tempRewriter.track(td);
										ListRewrite lrw = tempRewriter.getListRewrite(parsedAST, CompilationUnit.IMPORTS_PROPERTY);
										lrw.insertLast(id, null);
										TextEdit edits = tempRewriter.rewriteAST(document, null);
										UndoEdit undo = edits.apply(document);

										// commit changes to underlying file
										textFileBuffer
										.commit(null /* ProgressMonitor */, false /* Overwrite */); // (3)

									} finally {
										bufferManager.disconnect(path, null); // (4)
									}
									
									
									//									Document document = new Document("import java.util.List;\nclass X {}\n");
									//									ASTParser anotherParser = ASTParser.newParser(AST.JLS3);
									//									anotherParser.setSource(document.get().toCharArray());
									//									CompilationUnit cu1 = (CompilationUnit) anotherParser.createAST(null);
									//									AST ast = cu1.getAST();
									//									ImportDeclaration id = ast.newImportDeclaration();
									//									id.setName(ast.newName(new String[] {"java", "util", "Set"}));
									//									ASTRewrite rewriter = ASTRewrite.create(ast);
									//									TypeDeclaration td = (TypeDeclaration) cu1.types().get(0);
									//									ITrackedNodePosition tdLocation = rewriter.track(td);
									//									ListRewrite lrw = rewriter.getListRewrite(cu1, CompilationUnit.IMPORTS_PROPERTY);
									//									lrw.insertLast(id, null);
									//									TextEdit edits = rewriter.rewriteAST(document, null);
									//									UndoEdit undo = edits.apply(document);
									//									System.out.println(document.get());
									//									assert "import java.util.List;\nimport java.util.Set;\nclass X {}".equals(document.get().toCharArray());


								} catch (Exception e) {
									e.printStackTrace();
								}

								System.out.println("METHOD BODY:" + method.getBody().toString());
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
	private static CompilationUnit parse(ICompilationUnit unit) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(unit);
		parser.setResolveBindings(true);
		return (CompilationUnit) parser.createAST(null);
	}

}

