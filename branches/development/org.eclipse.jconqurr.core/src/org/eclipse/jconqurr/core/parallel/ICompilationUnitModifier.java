package org.eclipse.jconqurr.core.parallel;

import org.eclipse.jdt.core.dom.CompilationUnit;

public interface ICompilationUnitModifier {
	public void setCompilationUnit(CompilationUnit cu);
	public CompilationUnit getCompilationUnit();
	public void analyzeCode();
	public void modifyCode();
	public IForLoopModifier getForLoopModifier(int index);
}
