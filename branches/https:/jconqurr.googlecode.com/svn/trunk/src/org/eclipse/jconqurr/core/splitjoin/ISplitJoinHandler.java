package org.eclipse.jconqurr.core.splitjoin;

import java.util.List;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public interface ISplitJoinHandler {
	public String getModifiedMethod(String className);

	public List<String> getTasks();

	public void setMethod(MethodDeclaration method);

	public void init();

	public String getFields(String className);
}
