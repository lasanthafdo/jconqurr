package org.eclipse.jconqurr.core.parallel;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

public class Convert extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// TODO Auto-generated method stub
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil
		.getActiveMenuSelection(event);
		Object firstElement = selection.getFirstElement();
		if(firstElement instanceof ICompilationUnit) {
			ICompilationUnit cu = (ICompilationUnit) firstElement;
			CompilationUnitParallelizer cuParallelizer = new CompilationUnitParallelizer();
			cuParallelizer.parallelize(cu);
		}

/*		
		if (firstElement instanceof IJavaProject) {
			IJavaProject selectedProject = (IJavaProject) firstElement;
			JConqurrProjectBuilder jqBuilder = new JConqurrProjectBuilder();
			IJavaProject paralellProject = jqBuilder.createProject(selectedProject, event);
			jqBuilder.copySources(selectedProject, paralellProject);
			ProjectParallelizer parallelizer = new ProjectParallelizer();
			parallelizer.parallelize(paralellProject);
		} else {
			MessageDialog.openInformation(HandlerUtil.getActiveShell(event),
					"Information", "Please select a java project");
		}
*/
		return null;
	}
}

