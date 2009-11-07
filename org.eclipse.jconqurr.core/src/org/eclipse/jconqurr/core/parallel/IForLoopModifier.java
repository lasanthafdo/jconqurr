package org.eclipse.jconqurr.core.parallel;

import org.eclipse.jdt.core.dom.Block;
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
	public Block getModifiedBlock();
	public ForStatement getForStatement();
}
