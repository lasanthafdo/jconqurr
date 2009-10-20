package org.eclipse.jconqurr.internal.ui.commands;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
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
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

public class ConvertParallelCommand extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// TODO Auto-generated method stub
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil
				.getActiveSite(event).getSelectionProvider().getSelection();
		Object firstElement = selection.getFirstElement();
		if (firstElement instanceof IJavaProject) {
			IJavaProject selectedProject = (IJavaProject) firstElement;
			ConvertParallelCommand.this.creatProject(selectedProject);
		}
		return null;
	}

	public void creatProject(IJavaProject selectedProject) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject("Jconq"
				+ selectedProject.getElementName());
		if (!project.exists()) {
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
				System.arraycopy(oldEntries, 0, newEntries, 0,
						oldEntries.length);
				newEntries[oldEntries.length] = JavaCore.newSourceEntry(src
						.getPath());
				javaProject.setRawClasspath(newEntries, javaProject
						.getOutputLocation(), null);
				IPackageFragment pack = src.createPackageFragment("x.y", true,
						null);
				String str = "package x.y;" + "\n" + "public class E{" + "\n"
						+ "String first;" + "\n" + "}";
				ICompilationUnit cu = pack.createCompilationUnit("E.java", str,
						false, null);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
