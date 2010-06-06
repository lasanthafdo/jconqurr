package org.eclipse.jconqurr.core.build;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.JavaRuntime;

public class BuildJconqurrProj implements IBuildJconqurrProjManager {

	/**
	 * @see IBuildJconqurrProjManager#createProject(IJavaProject)
	 */
	public IJavaProject createProject(IJavaProject selectedProject) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject("Jq"
				+ selectedProject.getElementName());
		List<IPath>libraryPath=new ArrayList<IPath>();
		try {
			IPackageFragmentRoot[] roots = selectedProject
					.getPackageFragmentRoots();
			for (IPackageFragmentRoot r : roots) {
				if(!(r.getElementName().equals("src"))){
					libraryPath.add(r.getPath());
				}
			}
		} catch (JavaModelException e2) {
			e2.printStackTrace();
		}
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
				IClasspathEntry[] libPaths=new IClasspathEntry[libraryPath.size()+1];
				for(int i=0;i<libraryPath.size();i++){
					libPaths[i+1]=JavaCore.newLibraryEntry((libraryPath.get(i)),
							null, null, true);
				}
				libPaths[0]=JavaCore.newSourceEntry(project.getFullPath()
						.append("src"));
				buildPath=libPaths;
				javaProject.setRawClasspath(buildPath, project.getFullPath()
						.append("bin"), null);
				// create source folder
				IFolder folder = project.getFolder("src");
				folder.create(true, true, null);
				IPackageFragmentRoot srcFolder = javaProject
						.getPackageFragmentRoot(folder);
			} catch (CoreException e) {
				try {
					return (IJavaProject) project.getNature(JavaCore.NATURE_ID);
				} catch (CoreException e1) {
					e1.printStackTrace();
				}
			}
		}
		try {
			return (IJavaProject) project.getNature(JavaCore.NATURE_ID);
		} catch (CoreException e1) {
			e1.printStackTrace();
			return null;
		}
	}

}
