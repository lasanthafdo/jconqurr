package org.eclipse.jconqurr.core.gpu;

import org.eclipse.jdt.core.dom.MethodDeclaration;

public interface IGPUHandler {
	public void setMethod(MethodDeclaration method);

	public void process();
}
