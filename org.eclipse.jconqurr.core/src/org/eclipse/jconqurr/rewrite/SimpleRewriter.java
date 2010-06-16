package org.eclipse.jconqurr.rewrite;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.text.edits.MalformedTreeException;

public class SimpleRewriter extends JQRewriter {

	public SimpleRewriter(CompilationUnit cu) {
		super(cu);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void rewriteChanges(ASTRewrite rewriter, boolean overwrite) {
		// TODO Auto-generated method stub
		CompilationUnit unit = this.compilationUnit;
		bufferManager = FileBuffers.getTextFileBufferManager();
		path = unit.getJavaElement().getPath();
		
		try {
			bufferManager.connect(path, LocationKind.IFILE, null);
			textFileBuffer = bufferManager.getTextFileBuffer(path, LocationKind.IFILE);
			//retrieve the buffer
			document = textFileBuffer.getDocument();
			edit = rewriter.rewriteAST(document, null);
			undo = edit.apply(document);
			textFileBuffer.commit(null, false);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedTreeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
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
