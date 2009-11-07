package org.eclipse.jconqurr.core.parallel;

import java.util.List;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ForStatement;

/**
 * 
 * @author lasantha
 *
 */
public interface IForLoopModifier {
	public void analyzeCode();
	public void modifyCode();
	public String getModifiedCode();
	public void setCompilationUnit(CompilationUnit cu);
	public CompilationUnit getCompilationUnit();
	public Block getModifiedBlock();
	public List<ForStatement> getForStatements();
}
