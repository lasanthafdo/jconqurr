package org.eclipse.jconqurr.core;

import java.util.List;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jconqurr.core.data.loops.ILoopHandler;
import org.eclipse.jconqurr.core.data.loops.LoopHandler;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.UndoEdit;

public class CompilationUnitHandler implements ICompilationUnitHandler {
	private ICompilationUnit originalICU; 	// the original compilation unit from
											// the original project
	private ICompilationUnitFilter cuFilter; 	// this will filter everything from
												// the targetCU
	private CompilationUnit targetCU; 	// this is the target compilation unit
										// which will be the same as original initially
	private ILoopHandler loopHandler; // will handle loops

	@Override
	public void convertToParallel() throws NullPointerException,
			JavaModelException {
		try {
			cuFilter.filter();
			// handle for loops
			List<MethodDeclaration> forStatementMethods = cuFilter
					.getAnnotatedParallelForMethods();
			if (!cuFilter.getAnnotatedParallelForMethods().isEmpty())
				handleForLoops(forStatementMethods);
			for (int i = (this.targetCU.imports().size() - 1); i >= 0; i--) {
				ImportDeclaration importDecl = (ImportDeclaration) this.targetCU
						.imports().get(i);
				if (importDecl.getName().getFullyQualifiedName().startsWith(
						"org.eclipse.jconqurr")) {
					importDecl.delete();
				}
			}
			// handle other parallelizations
			// TODO: Write for other parallelizations
			// rewrite changes
			rewriteChanges();
		} catch (NullPointerException ne) {
			ne.printStackTrace();
			throw ne;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void handleForLoops(List<MethodDeclaration> forStatementMethods) {
		loopHandler = new LoopHandler(targetCU,
				(CompilationUnitFilter) cuFilter);
		loopHandler.processCompilationUnit();
	}

	@Override
	public ICompilationUnit getConvertedCompilationUnit() {
		return (ICompilationUnit) this.targetCU;
	}

	@Override
	public void setCompilationUnit(ICompilationUnit cu,
			IJavaProject targetProject) {
		IPackageFragment packageFragment = null;
		IPath targetPackagePath = null;
		try {
			this.originalICU = cu;
			if (cuFilter == null) {
				cuFilter = new CompilationUnitFilter();
			}

			if (this.originalICU.getParent().getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
				packageFragment = (IPackageFragment) this.originalICU
						.getParent();
				IPath relativePackagePath = packageFragment.getPath()
						.removeFirstSegments(1).makeRelative();
				targetPackagePath = targetProject.getPath().append(
						relativePackagePath);
			}
			IPackageFragment targetPackageFragment = targetProject
					.findPackageFragment(targetPackagePath);
			if (targetPackageFragment != null) {
				this.originalICU.copy(targetPackageFragment, null, null, true,
						null);
				this.targetCU = parse(targetPackageFragment
						.getCompilationUnit(this.originalICU.getElementName()));
				// start recording modifications to the AST of this CU
				this.targetCU.recordModifications();
			} else
				throw new JavaModelException(new CoreException(new Status(
						IStatus.ERROR, Activator.PLUGIN_ID,
						"Package not found!")));
			// set the compilation unit of the filter to the recently copied
			// unit
			// note that this copy will be modified
			cuFilter.setCompilationUnit(this.targetCU);
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
	}

	private void rewriteChanges() {
		ITextFileBufferManager bufferManager = FileBuffers
				.getTextFileBufferManager();
		IPath path = targetCU.getJavaElement().getPath();
		ITextFileBuffer textFileBuffer = null;
		IDocument document = null;
		TextEdit edit = null;
		@SuppressWarnings("unused")
		UndoEdit undo = null;

		try {
			bufferManager.connect(path, LocationKind.IFILE, null);
			textFileBuffer = bufferManager.getTextFileBuffer(path,
					LocationKind.IFILE);
			// retrieve the buffer
			document = textFileBuffer.getDocument();
			edit = targetCU.rewrite(document, null);
			undo = edit.apply(document);
			textFileBuffer.commit(null, true);
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

	/**
	 * Returns the compilation unit for a given ICompilationUnit reference
	 * 
	 * @param unit
	 *            the ICompilationUnit reference to be parsed
	 * @return CompilationUnit the CompilationUnit object that has been passed
	 *         successfully
	 */
	private static CompilationUnit parse(ICompilationUnit unit) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(unit);
		parser.setResolveBindings(true);
		return (CompilationUnit) parser.createAST(null);
	}

}
