package org.eclipse.jconqurr.core.data;

import java.util.List;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ForStatement;

public interface IForLoopModifier {
	public void analyzeCode();
	public String getModifiedCode();
	public void setCompilationUnit(CompilationUnit cu);
	public CompilationUnit getCompilationUnit();
	public List<ForStatement> getForStatements();
}
