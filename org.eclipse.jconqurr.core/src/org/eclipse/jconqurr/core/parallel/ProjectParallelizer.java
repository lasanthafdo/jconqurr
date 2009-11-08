package org.eclipse.jconqurr.core.parallel;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;

public class ProjectParallelizer {
	public void parallelize(IJavaProject project) {
		try {
			IPackageFragment[] pfs = project.getPackageFragments();
			if(pfs.length>0) {
				CompilationUnitParallelizer cuParallelizer = new CompilationUnitParallelizer();
				for(IPackageFragment pf: pfs) {
					for(ICompilationUnit cu:pf.getCompilationUnits()) {
						cuParallelizer.parallelize(cu);
					}
				}
			}
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
}
