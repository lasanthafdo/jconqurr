package org.eclipse.jconqurr.ui.popup.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class Actions implements IObjectActionDelegate {

	private IStructuredSelection selection;

	/**
	 * Constructor for Action1.
	 */
	public Actions() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {

	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		ISelection selection = window.getSelectionService().getSelection();
		if (selection instanceof IStructuredSelection) {
			Actions.this.selection = (IStructuredSelection) selection;
			Object firstElement = Actions.this.selection.getFirstElement();
			if (firstElement instanceof IProject) {
				IProject selectedProject = (IProject) firstElement;
				Actions.this.creatProject(selectedProject);
			}
		}
	}

	public void creatProject(IProject selectedProject) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject("Jconq" + selectedProject.getName());
		try {
			project.create(null);
			project.open(null);
			IProjectDescription description = project.getDescription();
			description.setNatureIds(new String[] { JavaCore.NATURE_ID });
			project.setDescription(description, null);
			IJavaProject javaProject = JavaCore.create(project);
			IFolder binFolder = project.getFolder("bin");
			binFolder.create(false, true, null);
			List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();
			IVMInstall vmInstall = JavaRuntime.getDefaultVMInstall();
			LibraryLocation[] locations = JavaRuntime
					.getLibraryLocations(vmInstall);

			for (LibraryLocation element : locations) {
				entries.add(JavaCore.newLibraryEntry(element
						.getSystemLibraryPath(), null, null));
			}
			// add libs to project class path
			javaProject.setRawClasspath(entries
					.toArray(new IClasspathEntry[entries.size()]), null);
			IFolder sourceFolder = project.getFolder("src");
			sourceFolder.create(true, true, null);
			IPackageFragmentRoot src = javaProject
					.getPackageFragmentRoot(sourceFolder);
			IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
			IClasspathEntry[] newEntries = new IClasspathEntry[oldEntries.length + 1];
			System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
			newEntries[oldEntries.length] = JavaCore.newSourceEntry(src
					.getPath());
			javaProject.setRawClasspath(newEntries, javaProject
					.getOutputLocation(), null);
			IPackageFragment pack = src
					.createPackageFragment("x.y", true, null);
			String str = "package x.y;" + "\n" + "public class E{" + "\n"
					+ "String first;" + "\n" + "}";
			ICompilationUnit cu = pack.createCompilationUnit("E.java", str,
					false, null);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {

	}

}
