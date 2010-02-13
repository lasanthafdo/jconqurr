package org.eclipse.jconqurr.core.build;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;

public class BuildJconqurrProj implements IBuildJconqurrProjManager {

	 /**
	  * @see IBuildJconqurrProjManager#createProject(IJavaProject)
	  */
	public IJavaProject createProject(IJavaProject selectedProject) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject("Jq"
				+ selectedProject.getElementName());
		if (!project.exists()) {
			try {
				// Create a project
				project.create(null);
				project.open(null);
				// Set the java nature
				IProjectDescription description = project.getDescription();
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
				IPackageFragmentRoot srcFolder = javaProject
						.getPackageFragmentRoot(folder);
			} catch (CoreException e) {
				System.out.println("Project exist");
				try {
					return (IJavaProject) project.getNature(JavaCore.NATURE_ID);
				} catch (CoreException e1) {
					// TODO Auto-generated catch block
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
 
}
