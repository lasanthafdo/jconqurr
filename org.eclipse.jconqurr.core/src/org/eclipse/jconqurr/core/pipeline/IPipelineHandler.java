package org.eclipse.jconqurr.core.pipeline;

import java.util.List;

import org.eclipse.jdt.core.dom.MethodDeclaration;

public interface IPipelineHandler {
	public String getModifiedMethod();

	public List<String> getTasks();

	public void setMethod(MethodDeclaration method);

	public void init();

	public String getFields();
}
