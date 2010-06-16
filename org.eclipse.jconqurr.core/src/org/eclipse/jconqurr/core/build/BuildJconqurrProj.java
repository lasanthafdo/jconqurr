package org.eclipse.jconqurr.core.build;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
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
		IJavaProject projectToReturn = null;
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject("Jq"
				+ selectedProject.getElementName());
		List<IPath> JCUDALibraryPath = new ArrayList<IPath>();
		List<IPath> libraryPath = new ArrayList<IPath>();
		// get the library paths
		try {
			IPackageFragmentRoot[] roots = selectedProject
					.getPackageFragmentRoots();
			for (IPackageFragmentRoot r : roots) {
				if (!(r.getElementName().equals("src"))) {
					libraryPath.add(r.getPath());
				}
				if (r.getElementName().equals("jcublas-0.2.3.jar")) {
					JCUDALibraryPath.add(r.getPath());
				} else if (r.getElementName().equals("jcuda-0.2.3.jar")) {
					JCUDALibraryPath.add(r.getPath());
				} else if (r.getElementName().equals("jcudpp-0.2.3.jar")) {
					JCUDALibraryPath.add(r.getPath());
				} else if (r.getElementName().equals("jcufft-0.2.3.jar")) {
					JCUDALibraryPath.add(r.getPath());
				}

			}
		} catch (JavaModelException e2) {
			// TODO Auto-generated catch block
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
				if (JCUDALibraryPath.size() == 0) {
					IClasspathEntry[] paths = {
							JavaCore.newSourceEntry(project.getFullPath()
									.append("src")),
							JavaRuntime.getDefaultJREContainerEntry() };
					buildPath = paths;

				} else {
					IClasspathEntry[] paths = {
							JavaCore.newSourceEntry(project.getFullPath()
									.append("src")),
							JavaRuntime.getDefaultJREContainerEntry(),
							JavaCore.newLibraryEntry((JCUDALibraryPath.get(0)),
									null, null, true),
							JavaCore.newLibraryEntry((JCUDALibraryPath.get(1)),
									null, null, true),
							JavaCore.newLibraryEntry((JCUDALibraryPath.get(2)),
									null, null, true),
							JavaCore.newLibraryEntry((JCUDALibraryPath.get(3)),
									null, null, true) };
					buildPath = paths;
				}
				IClasspathEntry[] libPaths = new IClasspathEntry[libraryPath
						.size() + 1];
				for (int i = 0; i < libraryPath.size(); i++) {
					System.out.println(libraryPath.get(i).toOSString());
					libPaths[i + 1] = JavaCore.newLibraryEntry((libraryPath
							.get(i)), null, null, true);
				}
				libPaths[0] = JavaCore.newSourceEntry(project.getFullPath()
						.append("src"));
				buildPath = libPaths;
				javaProject.setRawClasspath(buildPath, project.getFullPath()
						.append("bin"), null);
				// create source folder
				IFolder folder = project.getFolder("src");
				folder.create(true, true, null);
				// following segment copies all non-java resources which are
				// files and folders of the project directly.
				Object[] nonJavaRes = null;
				nonJavaRes = selectedProject.getNonJavaResources();
				for (Object resource : nonJavaRes) {
					if (resource instanceof IFile) {
						IFile srcFile = (IFile) resource;
						IFile destFile = project.getFile(srcFile.getName());
						if (!destFile.exists() && !srcFile.isHidden()) {
							destFile.create(srcFile.getContents(), IResource.NONE, null);
						}
					}
				}

			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		
		try {
			projectToReturn = (IJavaProject) project.getNature(JavaCore.NATURE_ID);
		} catch (CoreException e1) {
			e1.printStackTrace();
		}
		
		return projectToReturn;
	}

}
 
