package org.eclipse.jconqurr.internal.ui.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jconqurr.core.HandleProjectParallelism;
import org.eclipse.jconqurr.core.IHandleProjectParallelism;
import org.eclipse.jconqurr.core.build.BuildJconqurrProj;
import org.eclipse.jconqurr.core.build.IBuildJconqurrProjManager;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.actions.ActionDelegate;

public class ConvertParallelAction extends ActionDelegate {

	private IStructuredSelection selection = StructuredSelection.EMPTY;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.actions.ActionDelegate#selectionChanged(org.eclipse.jface
	 * .action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection sel) {
		if (sel instanceof IStructuredSelection)
			selection = (IStructuredSelection) sel;
		else
			selection = StructuredSelection.EMPTY;
	}

	/**
	 * Constructor for Action1.
	 */
	public ConvertParallelAction() {
		super();
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		Object firstElement = selection.getFirstElement();
		if (firstElement instanceof IProject) {
			IJavaProject selectedProject;
			try {
				selectedProject = (IJavaProject) (((IProject) firstElement)
						.getNature(JavaCore.NATURE_ID));
				//BuildJconqurrProj projBuilder = new BuildJconqurrProj();
				IBuildJconqurrProjManager projBuilderManager=new BuildJconqurrProj();
				IJavaProject parallelProject=projBuilderManager.createProject(selectedProject);
				IHandleProjectParallelism handler=new HandleProjectParallelism();
				handler.handleProject(parallelProject, selectedProject);
			//	IJavaProject proj = projBuilder.createProject(selectedProject);
				//projBuilder.convert(proj, selectedProject);
				 
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
