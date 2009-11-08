package org.eclipse.jconqurr.core.parallel;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jconqurr.core.ast.CompilationUnitParser;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.UndoEdit;

public class CompilationUnitParallelizer {
	
	public void parallelize(ICompilationUnit cu) {
		CompilationUnit unit = CompilationUnitParser.parse(cu);

		ICompilationUnitModifier cuModifier = new CompilationUnitModifier(unit);
		
		cuModifier.analyzeCode();
		cuModifier.modifyCode();
		IForLoopModifier flModifier = cuModifier.getForLoopModifier(0);

		ITextFileBufferManager bufferManager = FileBuffers.getTextFileBufferManager(); // get the buffer manager
		IPath path = unit.getJavaElement().getPath(); // unit: instance of CompilationUnit
		try {
			bufferManager.connect(path,LocationKind.IFILE, null); // (1)
			ITextFileBuffer textFileBuffer = bufferManager.getTextFileBuffer(path, LocationKind.IFILE);
			// retrieve the buffer
			IDocument document = textFileBuffer.getDocument();
			// ... edit the document here ... 
			AST ast = unit.getAST();
			ASTRewrite tempRewriter = ASTRewrite.create(ast);

			ImportDeclaration lhDeclaration = ast.newImportDeclaration();
			lhDeclaration.setName(ast.newName(new String[] {"org", "eclipse", "jconqurr","core","parallel","loops","LoopHandler"}));
			ImportDeclaration iftDeclaration = ast.newImportDeclaration();
			iftDeclaration.setName(ast.newName(new String[] {"org", "eclipse", "jconqurr","core","parallel","loops","IForLoopTask"}));


			TypeDeclaration td = (TypeDeclaration) unit.types().get(0);
			ITrackedNodePosition tdLocation = tempRewriter.track(td);
			tempRewriter.replace(flModifier.getForStatement(), flModifier.getModifiedBlock(), null);
			ListRewrite lrw = tempRewriter.getListRewrite(unit, CompilationUnit.IMPORTS_PROPERTY);
			lrw.insertLast(lhDeclaration, null);
			lrw.insertLast(iftDeclaration, null);
			TextEdit edits = tempRewriter.rewriteAST(document, null);
			UndoEdit undo = edits.apply(document);

			// commit changes to underlying file
			textFileBuffer
			.commit(null /* ProgressMonitor */, false /* Overwrite */); 
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				bufferManager.disconnect(path, LocationKind.IFILE, null);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
