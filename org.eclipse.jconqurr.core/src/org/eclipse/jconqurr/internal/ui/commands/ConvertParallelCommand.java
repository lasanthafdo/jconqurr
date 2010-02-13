package org.eclipse.jconqurr.internal.ui.commands;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jconqurr.core.HandleProjectParallelism;
import org.eclipse.jconqurr.core.IHandleProjectParallelism;
import org.eclipse.jconqurr.core.build.BuildJconqurrProj;
import org.eclipse.jconqurr.core.build.IBuildJconqurrProjManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

public class ConvertParallelCommand extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil
				.getActiveSite(event).getSelectionProvider().getSelection();
		Object firstElement = selection.getFirstElement();
		if (firstElement instanceof IJavaProject) {
			IJavaProject selectedProject = (IJavaProject) firstElement;
			IBuildJconqurrProjManager projBuilderManager=new BuildJconqurrProj();
			IJavaProject parallelProject=projBuilderManager.createProject(selectedProject);
			IHandleProjectParallelism handler=new HandleProjectParallelism();
			try {
				handler.handleProject(parallelProject, selectedProject);
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

}

