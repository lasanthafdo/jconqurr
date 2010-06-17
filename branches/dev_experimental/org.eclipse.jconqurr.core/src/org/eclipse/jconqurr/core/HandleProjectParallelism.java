package org.eclipse.jconqurr.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jconqurr.core.ast.visitors.FieldDeclarationVisitor;
import org.eclipse.jconqurr.core.ast.visitors.TypeDeclarationVisitor;
import org.eclipse.jconqurr.core.data.ForLoop;
import org.eclipse.jconqurr.core.data.ForLoopHandler;
import org.eclipse.jconqurr.core.data.IForLoopHandler;
import org.eclipse.jconqurr.core.divideandconquer.DivideAndConquerHandler;
import org.eclipse.jconqurr.core.divideandconquer.IDivideAndConquerHandler;
import org.eclipse.jconqurr.core.gpu.GPUHandler;
import org.eclipse.jconqurr.core.gpu.IGPUHandler;
import org.eclipse.jconqurr.core.pipeline.IPipelineHandler;
import org.eclipse.jconqurr.core.pipeline.PipelineHandler;
import org.eclipse.jconqurr.core.task.ITaskMethod;
import org.eclipse.jconqurr.core.task.TaskMethod;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

public class HandleProjectParallelism implements IHandleProjectParallelism {
	private String classNameDeclaration;
	private String className;
	private String imports;
	private String packageName;
	private String taskParallelCode;
	private String loopParallelCode;
	private String otherMethods;
	private String otherInnerClasses;
	private String fieldDeclarations;
	private String divideAndConquerCode;
	private String gpuCode;
	private String pipelineClassFields = "";
	private String sharedFields = "";
	private String pipelineCode;
	private static String srcPath;

	public static String getSrcPath() {
		return srcPath;
	}

	/**
	 * @see HandleProjectParallelism#convert(IJavaProject, ICompilationUnit)
	 */
	public void convert(IJavaProject parallel, ICompilationUnit unit) {
		ForLoop.lockNo = 1;
		ForLoopHandler.counter = 0;
		sharedFields = "";
		IProject project = parallel.getProject();
		IFolder folder = project.getFolder("src");
		srcPath = folder.getLocation().toOSString();
		CompilationUnit cu = parse(unit);
		ICompilationUnitFilter filter = new CompilationUnitFilter();
		filter.setCompilationUnit(cu);
		filter.filter();
		
		setFieldsDeclaration(cu);
		setTaskParallelCode(filter.getAnnotatedParallelTaskMethods());
		setLoopParallelCode(filter.getAnnotatedParallelForMethods());
		setDivideAndConquerCode(filter.getAnnotatedDivideAndConquer());
		setGPUCode(filter.getAnnotatedGPUMethods());
		setOtherInnerClasses(filter.getOtherInnerClasses());
		setOtherMethods(filter.getNotAnnotatedMethods());
		setClassName(cu);
		setPipelineCode(filter.getPipelineMethods());
		setImports(unit);

		IPackageFragmentRoot srcFolder = parallel
				.getPackageFragmentRoot(folder);
		try {
			if (srcFolder.exists()) {
				IPackageDeclaration[] dec = unit.getPackageDeclarations();
				IPackageFragment fragment = srcFolder.createPackageFragment(
						dec[0].getElementName(), true, null);
				packageName = dec[0].getElementName();
				try {
					String src = formatCode(generateClass());

					ICompilationUnit cunit = fragment.createCompilationUnit(
							unit.getElementName(), src, true, null);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
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
					convert(parallel, unit);
				}
			}
		}
	}

	/**
	 * Generate the source code for the compilation unit
	 * 
	 * @return String
	 */
	private String generateClass() {

		String execImports;
		String gpuImports;
		String pipelineImports;
		String exec;
		if (!(this.gpuCode.equals(""))) {
			gpuImports = "import java.io.*;" + "\n" + "import jcuda.*;" + "\n"
					+ "import jcuda.driver.*;";
		} else {
			gpuImports = "";
		}
		if (!(this.pipelineCode.equals(""))) {
			pipelineImports = "import java.util.concurrent.*;" + "\n";

		} else {
			pipelineImports = "";
		}
		if (!(this.loopParallelCode.equals(""))
				|| !(this.taskParallelCode.equals(""))) {
			execImports = "import java.util.concurrent.ExecutorService;" + "\n"
					+ "import java.util.concurrent.Executors;" + "\n"
					+ "import java.util.concurrent.Future;"
					+ "import java.util.concurrent.BrokenBarrierException;"
					+ "import java.util.concurrent.CyclicBarrier;";

		} else {
			execImports = "";
		}
		String divideAndConqerImports;
		if (!(this.divideAndConquerCode.equals(""))) {
			divideAndConqerImports = "import EDU.oswego.cs.dl.util.concurrent.FJTask;"
					+ "\n"
					+ "import EDU.oswego.cs.dl.util.concurrent.FJTaskRunnerGroup;\n";
		} else {
			divideAndConqerImports = "";
		}
		if (!(this.loopParallelCode.equals(""))
				|| !(this.taskParallelCode.equals(""))) {
			exec = "static ExecutorService exec = Executors.newCachedThreadPool();"
					+ "\n";
		} else {
			exec = "";
		}

		String src = "package " + packageName + ";" + "\n" + imports
				+ execImports + divideAndConqerImports + gpuImports
				+ pipelineImports + "\n " + classNameDeclaration + "{"
				+ fieldDeclarations + sharedFields + pipelineClassFields + exec
				+ taskParallelCode + loopParallelCode + divideAndConquerCode
				+ gpuCode + pipelineCode + otherMethods + otherInnerClasses
				+ "}";

		return src;

	}

	/**
	 * sets the task parallel code
	 * 
	 * @param taskParallelMethods
	 */
	private void setTaskParallelCode(List<MethodDeclaration> taskParallelMethods) {
		this.taskParallelCode = "";
		for (MethodDeclaration method : taskParallelMethods) {
			ITaskMethod taskMethod = new TaskMethod();
			taskMethod.setMethod(method);
			taskMethod.init();
			this.taskParallelCode = this.taskParallelCode
					+ taskMethod.getModifiedMethod();
		}
	}

	private void setGPUCode(List<MethodDeclaration> gpuMethods) {
		this.gpuCode = "";
		for (MethodDeclaration method : gpuMethods) {
			IGPUHandler gpuHandler = new GPUHandler();
			gpuHandler.setMethod(method);
			gpuHandler.process();
			this.gpuCode = this.gpuCode + gpuHandler.getModifiedCode();
		}
	}

	private void setPipelineCode(List<MethodDeclaration> pipelineMethods) {
		this.pipelineCode = "";
		for (MethodDeclaration method : pipelineMethods) {
			IPipelineHandler pipelineHandler = new PipelineHandler();
			pipelineHandler.setMethod(method);
			pipelineHandler.init();
			this.pipelineCode = this.pipelineCode
					+ pipelineHandler.getModifiedMethod(className);
			pipelineClassFields = pipelineHandler.getFields(className);
		}

	}

	/**
	 * sets the loop parallel code
	 * 
	 * @param loopParallelMethods
	 */
	private void setLoopParallelCode(List<MethodDeclaration> loopParallelMethods) {
		this.loopParallelCode = "";
		this.sharedFields = "";
		for (MethodDeclaration method : loopParallelMethods) {

			IForLoopHandler forLoopHandler = new ForLoopHandler();
			forLoopHandler.setMethod(method);
			forLoopHandler.init();
			this.loopParallelCode = this.loopParallelCode
					+ forLoopHandler.getModifiedMethod();
		}
		int i = ForLoop.lockNo;
		for (int j = 1; j < i; j++) {
			sharedFields += "private static Object lock" + j + "=new Object();";
		}

	}

	/**
	 * sets the modified parallel divide and conquer code
	 * 
	 * @param divideAndConquerMethods
	 */
	private void setDivideAndConquerCode(List<HashMap<String, MethodDeclaration>> divideAndConquerMethods) {
		this.divideAndConquerCode = "";
		for (HashMap<String, MethodDeclaration> map : divideAndConquerMethods) {
			System.out.println(map.get("caller"));
			System.out.println(map.get("recursive"));
			IDivideAndConquerHandler divideAndConquerHandler = new DivideAndConquerHandler();
			divideAndConquerHandler.setRecursionCaller(map.get("caller"));// recursive
			divideAndConquerHandler.setRecursiveMethod(map.get("recursive"));

			divideAndConquerHandler.init();
			divideAndConquerCode = divideAndConquerHandler.getModifiedMethods();
		}
	}

	/**
	 * sets the other methods which are not annotated by Jconqurr annotations
	 * 
	 * @param otherMethods
	 */
	private void setOtherMethods(List<MethodDeclaration> otherMethods) {
		this.otherMethods = "";
		for (MethodDeclaration method : otherMethods) {
			this.otherMethods = this.otherMethods + method.toString();
		}
	}

	private void setOtherInnerClasses(List<TypeDeclaration> innerClasses) {
		this.otherInnerClasses = "";
		for (TypeDeclaration innerClass : innerClasses) {
			this.otherInnerClasses += innerClass.toString() + "\n";
		}
	}

	/**
	 * sets the fields declarations in the class
	 * 
	 * @param cu
	 */
	private void setFieldsDeclaration(CompilationUnit cu) {
		FieldDeclarationVisitor fieldVisitor = new FieldDeclarationVisitor();
		cu.accept(fieldVisitor);
		fieldDeclarations = "";
		for (FieldDeclaration f : fieldVisitor.getFields()) {
			fieldDeclarations = fieldDeclarations + f.toString();
		}
	}

	/**
	 * sets the class name
	 * 
	 * @param className
	 */
	private void setClassName(CompilationUnit compilationUnit) {
		TypeDeclarationVisitor typeVisitor = new TypeDeclarationVisitor();
		compilationUnit.accept(typeVisitor);
		String modifiers = "";
		String name = "";
		String superType = "";
		String interfaceTypes = "";
		if (typeVisitor.getTypeDeclarations().size() >= 1) {
			TypeDeclaration t = typeVisitor.getTypeDeclarations().get(0);
			for (int i = 0; i < t.modifiers().size(); i++) {
				modifiers += t.modifiers().get(i) + " ";
			}
			name = t.getName().toString();
			if (t.getSuperclassType() != null) {
				superType = " extends " + t.getSuperclassType() + " ";
			}
			if (t.superInterfaceTypes().size() > 0) {
				if (t.superInterfaceTypes().size() == 1) {
					interfaceTypes = " implements "
							+ t.superInterfaceTypes().get(0);
				} else {
					for (int i = 0; i < t.superInterfaceTypes().size(); i++) {
						if (i != t.superInterfaceTypes().size() - 1) {
							interfaceTypes += " "
									+ t.superInterfaceTypes().get(i) + ",";
						} else {
							interfaceTypes = interfaceTypes + " "
									+ t.superInterfaceTypes().get(i);
						}
					}
					interfaceTypes = " implements " + interfaceTypes;
				}
			}
		}

		if (name != null)
			this.className = name;
		this.classNameDeclaration = modifiers + " class " + name + superType
				+ interfaceTypes;
	}

	/**
	 * sets the imports
	 * 
	 * @param unit
	 */
	private void setImports(ICompilationUnit unit) {
		imports = "";
		try {
			IImportDeclaration[] classImports = unit.getImports();
			for (IImportDeclaration im : classImports) {
				if (!(im.getElementName().startsWith("org.eclipse.jconqurr.")))
					imports = imports + "import " + im.getElementName() + ";\n";
			}
		} catch (JavaModelException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	/**
	 * Formats the source code
	 * 
	 * @param src
	 * @return
	 * @throws org.eclipse.jface.text.BadLocationException
	 * @throws IOException
	 */
	private String formatCode(String src)
			throws org.eclipse.jface.text.BadLocationException, IOException {
		// take default Eclipse formatting options

		Map options = DefaultCodeFormatterConstants.getEclipseDefaultSettings();
		// initialize the compiler settings to be able to format 1.5 code
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM,
				JavaCore.VERSION_1_5);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);

		// change the option to wrap each enum constant on a new line
		options
				.put(
						DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_ENUM_CONSTANTS,
						DefaultCodeFormatterConstants
								.createAlignmentValue(
										true,
										DefaultCodeFormatterConstants.WRAP_ONE_PER_LINE,
										DefaultCodeFormatterConstants.INDENT_ON_COLUMN));

		// instantiate the default code formatter with the given options
		final CodeFormatter codeFormatter = ToolFactory
				.createCodeFormatter(options);
		// retrieve the source to format
		String source = src;
		source = src; // retrieve the source
		final TextEdit edit = codeFormatter.format(
				CodeFormatter.K_COMPILATION_UNIT, // format a compilation unit
				source, // source to format
				0, // starting position
				source.length(), // length
				0, // initial indentation
				System.getProperty("line.separator") // line separator
				);
		IDocument document = new Document(source);
		try {
			edit.apply(document);
		} catch (MalformedTreeException e) {
			e.printStackTrace();
		}
		return document.get();
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
