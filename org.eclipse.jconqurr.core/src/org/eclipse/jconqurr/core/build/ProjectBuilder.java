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
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.JavaRuntime;

public class ProjectBuilder implements IProjectBuilder {
	private static ProjectBuilder currentInstance = null;
	
	private ProjectBuilder() {

	}
	
	public static ProjectBuilder getInstance() {
		if(currentInstance == null)
			currentInstance = new ProjectBuilder();
		
		return currentInstance;
	}
	
	@Override
	public IJavaProject createParallelProject(IJavaProject originalProject)
			throws NullPointerException {
		IJavaProject parallelProject = null;
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject("Jq"
				+ originalProject.getElementName());
		List<IPath> libraryPath = new ArrayList<IPath>();
		// get the library paths
		try {
			IPackageFragmentRoot[] roots = originalProject
					.getPackageFragmentRoots();
			for (IPackageFragmentRoot r : roots) {
				if (!(r.getElementName().equals("src"))) {
					libraryPath.add(r.getPath());
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		try {
			if (!project.exists()) {

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
				IClasspathEntry[] libPaths = new IClasspathEntry[libraryPath
						.size() + 1];
				for (int i = 0; i < libraryPath.size(); i++) {
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
				// add package fragments to the target project
				addPackageFragments(originalProject, javaProject);
				
				// following segment copies all non-java resources which are
				// files and folders of the project directly.
				Object[] nonJavaRes = null;
				nonJavaRes = originalProject.getNonJavaResources();
				for (Object resource : nonJavaRes) {
					if (resource instanceof IFile) {
						IFile srcFile = (IFile) resource;
						IFile destFile = project.getFile(srcFile.getName());
						if (!destFile.exists() && !srcFile.isHidden()) {
							destFile.create(srcFile.getContents(),
									IResource.NONE, null);
						}
					}
				} // end of for loop for non-java resource addition
			}// end if(!project.exists())
			parallelProject = (IJavaProject) project
					.getNature(JavaCore.NATURE_ID);
		} catch (CoreException e) {
			e.printStackTrace();
		}

		return parallelProject;
	}
	
	private void addPackageFragments(IJavaProject originalProj, IJavaProject targetProj) throws JavaModelException {
		IFolder targetSrc = targetProj.getProject().getFolder("src");
		IFolder originalSrc = originalProj.getProject().getFolder("src");
		if(originalSrc.exists() && targetSrc.exists()) {
			IPackageFragmentRoot srcRoot = originalProj.findPackageFragmentRoot(originalSrc.getFullPath());
			IPackageFragmentRoot targetRoot = targetProj.findPackageFragmentRoot(targetSrc.getFullPath());
			for(IJavaElement fragment:srcRoot.getChildren()) {
				if(fragment.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
					targetRoot.createPackageFragment(fragment.getElementName(), true, null);
				}
			}
		}
	}

}
