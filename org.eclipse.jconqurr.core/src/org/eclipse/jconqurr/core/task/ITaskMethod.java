package org.eclipse.jconqurr.core.task;

import java.util.List;

import org.eclipse.jdt.core.dom.MethodDeclaration;

public interface ITaskMethod {

	public String getModifiedMethod();

	public List<String> getTasks();

	public void setMethod(MethodDeclaration method);

	public void init();

}
