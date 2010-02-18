package org.eclipse.jconqurr.core;

import java.util.List;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jconqurr.core.ast.visitors.FieldDeclarationVisitor;
import org.eclipse.jconqurr.core.data.ForLoopHandler;
import org.eclipse.jconqurr.core.data.IForLoopHandler;
import org.eclipse.jconqurr.core.task.ITaskMethod;
import org.eclipse.jconqurr.core.task.TaskMethod;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class HandleProjectParallelism implements IHandleProjectParallelism {
	private String className;
	private String imports;
	private String packageName;
	private String taskParallelCode;
	private String loopParallelCode;
	private String otherMethods;
	private String fieldDeclarations;

	/**
	 * @see HandleProjectParallelism#convert(IJavaProject, ICompilationUnit)
	 */
	public void convert(IJavaProject parallel, ICompilationUnit unit) {
		CompilationUnit cu = parse(unit);
		ICompilationUnitFilter filter = new CompilationUnitFilter();
		filter.setCompilationUnit(cu);
		filter.filter();
		List<MethodDeclaration> taskParallelMethods = filter
				.getAnnotatedParallelTaskMethods();
		List<MethodDeclaration> otherMethods = filter.getNotAnnotatedMethods();
		List<MethodDeclaration> loopParallelMethods=filter.getAnnotatedParallelForMethods();
		FieldDeclarationVisitor fieldVisitor=new FieldDeclarationVisitor();
		cu.accept(fieldVisitor);
		fieldDeclarations="";
		for(FieldDeclaration f:fieldVisitor.getFields()){
			fieldDeclarations=fieldDeclarations+f.toString();
		}
		this.taskParallelCode = "";
		for (MethodDeclaration method : taskParallelMethods) {
			ITaskMethod taskMethod = new TaskMethod();
			taskMethod.setMethod(method);
			taskMethod.init();
			this.taskParallelCode = this.taskParallelCode
					+ taskMethod.getModifiedMethod();
		}
		this.loopParallelCode = "";
		for (MethodDeclaration method :loopParallelMethods ) {
			IForLoopHandler forLoopHandler = new ForLoopHandler();
			forLoopHandler.setMethod(method);
			forLoopHandler.init();
			this.loopParallelCode = this.loopParallelCode
					+ forLoopHandler.getModifiedMethod();
		}
		System.out.println(loopParallelCode);
		this.otherMethods = "";
		for (MethodDeclaration method : otherMethods) {
			this.otherMethods = this.otherMethods + method.toString();
		}

		className = unit.getElementName();
		imports="";
		try {
			IImportDeclaration[] classImports=unit.getImports();
			for(IImportDeclaration im:classImports){
				if(!(im.getElementName().startsWith("org.eclipse.jconqurr.")))
				imports=imports+"import "+im.getElementName()+";\n";
			}
		} catch (JavaModelException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		IProject project = parallel.getProject();
		IFolder folder = project.getFolder("src");
		IPackageFragmentRoot srcFolder = parallel
				.getPackageFragmentRoot(folder);
		try {
			if (srcFolder.exists()) {
				IPackageDeclaration[] dec = unit.getPackageDeclarations();
				IPackageFragment fragment = srcFolder.createPackageFragment(
						dec[0].getElementName(), true, null);
				packageName = dec[0].getElementName();
				String src = generateClass();
				ICompilationUnit cunit = fragment.createCompilationUnit(
						className, src, true, null);
			}
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Generate the source code for the compilation unit
	 * 
	 * @return String
	 */
	private String generateClass() {
		char[] name = new char[className.length() - 5];
		className.getChars(0, className.length() - 5, name, 0);
		String n = "";
		for (char c : name) {
			n = n + c;
		}
		String execImports = "import java.util.concurrent.ExecutorService;" + "\n"
				+ "import java.util.concurrent.Executors;" + "\n"
				+ "import java.util.concurrent.Future;";
		String src = "package " + packageName + ";" + "\n" + imports +execImports+ "\n "
				+ "public class " + n + "{"+fieldDeclarations + taskParallelCode+loopParallelCode+otherMethods
				+ "}";
		return src;

	}

	/**
	 * @see HandleProjectParallelism#handleProject(IJavaProject, IJavaProject)
	 */
	public void handleProject(IJavaProject parallel, IJavaProject sequential)
			throws JavaModelException {
		IPackageFragment[] fragments = sequential.getPackageFragments();
		for (IPackageFragment fr : fragments) {
			if (fr.getKind() == IPackageFragmentRoot.K_SOURCE) {
				for (ICompilationUnit unit : fr.getCompilationUnits()) {
					//System.out.println(unit.getElementName());
					// convert each classes in to parallel
					convert(parallel, unit);
				}
			}
		}
	}

	/**
	 * returns the compilation unit for a given ICompilationUnit reference
	 * 
	 * @param unit
	 * @return CompilationUnit
	 */
	private static CompilationUnit parse(ICompilationUnit unit) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(unit);
		parser.setResolveBindings(true);
		return (CompilationUnit) parser.createAST(null);
	}

}

