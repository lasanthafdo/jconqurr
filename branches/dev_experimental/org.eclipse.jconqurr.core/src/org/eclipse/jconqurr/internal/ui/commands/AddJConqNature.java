package org.eclipse.jconqurr.internal.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jconqurr.core.Activator;
import org.eclipse.jconqurr.core.JConqurrNature;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.osgi.framework.Bundle;

public class AddJConqNature extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// TODO Auto-generated method stub
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil
				.getActiveSite(event).getSelectionProvider().getSelection();
		Object firstElement = selection.getFirstElement();
		try {
			if (firstElement instanceof IJavaProject) {
				IJavaProject selectedProject = (IJavaProject) firstElement;
				// get the current class path entries
				IClasspathEntry[] classPaths = selectedProject
						.getResolvedClasspath(false);
				Bundle thisBundle = Activator.getDefault().getBundle();
				String[] locDetails = thisBundle.getLocation().split(":");
				String bundlePath = "";
				for(int i=0; i<locDetails.length; i++)
					if(locDetails[i].equals("file")) {
						bundlePath = locDetails[i+1];
						break;
					}
				
				// add our classpaths to a IClassPathEntry array and set it to the project
				IClasspathEntry[] modifiedClassPaths = null;
				IPath libPath = new Path(bundlePath + IPath.SEPARATOR + "lib").makeAbsolute();
				IPath annotationPath = libPath.append("org.eclipse.jconqurr.annotations.jar");
				IClasspathEntry aptCpEntry = JavaCore.newLibraryEntry(annotationPath, null, null);
				modifiedClassPaths = addClassPathEntry(classPaths, aptCpEntry);
				IPath directivesPath = libPath.append("org.eclipse.jconqurr.directives.jar");
				IClasspathEntry drctvsCpEntry = JavaCore.newLibraryEntry(directivesPath, null, null);
				modifiedClassPaths = addClassPathEntry(modifiedClassPaths, drctvsCpEntry);
				selectedProject.setRawClasspath(modifiedClassPaths, null);
				
				// add JConq nature
				IProjectDescription projDescription = selectedProject.getProject().getDescription();
				String[] natures = projDescription.getNatureIds();
				String[] modifiedNatures = new String[natures.length+1];
				System.arraycopy(natures, 0, modifiedNatures, 0, natures.length);
				modifiedNatures[natures.length] = JConqurrNature.JCONQ_NATURE;
				projDescription.setNatureIds(modifiedNatures);
				selectedProject.getProject().setDescription(projDescription, null);
			}
		} catch (JavaModelException e) {
			// TODO: handle exception
			e.printStackTrace();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private IClasspathEntry[] addClassPathEntry(IClasspathEntry[] currentList, IClasspathEntry classPathToAdd) {
		IClasspathEntry[] newClassPaths = new IClasspathEntry[currentList.length+1];
		for(int i=0; i<currentList.length; i++) {
			if(currentList[i].equals(classPathToAdd))
				return currentList;
			newClassPaths[i] = currentList[i];
		}
		newClassPaths[currentList.length] = classPathToAdd;
		return newClassPaths;
	}
}
