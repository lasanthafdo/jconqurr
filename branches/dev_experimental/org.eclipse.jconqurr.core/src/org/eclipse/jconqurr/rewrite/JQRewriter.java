package org.eclipse.jconqurr.rewrite;

import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.UndoEdit;

public abstract class JQRewriter {
	protected IDocument document;
	protected IPath path;
	protected ITextFileBufferManager bufferManager;
	protected ITextFileBuffer textFileBuffer;
	protected TextEdit edit;
	protected UndoEdit undo;
	protected CompilationUnit compilationUnit;	
	
	public CompilationUnit getCompilationUnit() {
		return compilationUnit;
	}

	public void setCompilationUnit(CompilationUnit compilationUnit) {
		this.compilationUnit = compilationUnit;
	}

	public JQRewriter(CompilationUnit cu) {
		this.compilationUnit = cu;
	}
	
	public abstract void rewriteChanges(ASTRewrite rewriter, boolean overwrite);
}
