package org.eclipse.jconqurr.core.build;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jconqurr.core.ast.CompilationUnitParser;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.handlers.HandlerUtil;

public class JConqurrProjectBuilder {
	public IJavaProject createProject(IJavaProject selectedProject, ExecutionEvent event) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject("Jq"
				+ selectedProject.getElementName());
		if (!project.exists()) {
			try {
				// Create a project
				project.create(null);
				project.open(null);
				// Set the java nature
				IProjectDescription description = selectedProject.getProject().getDescription();
				description.setNatureIds(new String[] { JavaCore.NATURE_ID });
				project.setDescription(description, null);
				// set the build path
				IJavaProject javaProject = JavaCore.create(project);
				IClasspathEntry[] buildPath = {
						JavaCore.newSourceEntry(project.getFullPath().append(
								"src")),
						JavaRuntime.getDefaultJREContainerEntry() };
				javaProject.setRawClasspath(buildPath, project.getFullPath()
						.append("bin"), null);
				// create source folder

				IFolder folder = project.getFolder("src");
				folder.create(true, true, null);
				
			} catch (CoreException e) {

				try {
					return (IJavaProject) project.getNature(JavaCore.NATURE_ID);
				} catch (CoreException e1) {
					// TODO Auto-generated catch block
					MessageDialog.openError(HandlerUtil.getActiveShell(event),
							"Error", "Error in getting parallel project");									
					e1.printStackTrace();
				}
			}
		}
		try {
			return (IJavaProject) project.getNature(JavaCore.NATURE_ID);
		} catch (CoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}
	}
	
	public void copySourceFile(IJavaProject parallel,ICompilationUnit unit) {
		IProject project = parallel.getProject();
		IFolder folder = project.getFolder("src");
		IPackageFragmentRoot srcFolder = parallel
				.getPackageFragmentRoot(folder);
		try {
			if (srcFolder.exists()) {
				IPackageDeclaration[] dec = unit.getPackageDeclarations();
				IPackageFragment fragment = srcFolder.createPackageFragment(
						dec[0].getElementName(), true, null);
				CompilationUnit cu = CompilationUnitParser.parse(unit);
				fragment.createCompilationUnit(unit.getElementName(), cu.toString(), true, null);
			}
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}				
	}
	
	public void copySources(IJavaProject original, IJavaProject parallel) {
		try {
			IPackageFragment[] pfs = original.getPackageFragments();
			for(IPackageFragment pf: pfs) {
				for(ICompilationUnit cu:pf.getCompilationUnits()) {
					copySourceFile(parallel, cu);
				}
			}
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
