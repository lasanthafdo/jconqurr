package org.eclipse.jconqurr.core.gpu;

import java.io.*;
import java.util.List;

import org.eclipse.jconqurr.core.ast.visitors.AssignmentVisitor;
import org.eclipse.jconqurr.core.ast.visitors.ExpressionStatementVisitor;
import org.eclipse.jconqurr.core.ast.visitors.ForLoopVisitor;
import org.eclipse.jconqurr.core.ast.visitors.InfixExpressionVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class GPUHandler implements IGPUHandler {

	private MethodDeclaration method;
	private String cubinFileName = "JCudaCubinSample_kernel.cu";
	private String cuMethodArguments;
	private String gloabalOutPut;

	private String getDriverInitializationCode() {

		String initializationCode = " JCudaDriver.cuInit(0);" + "\n"
				+ "CUcontext pctx = new CUcontext();" + "\n"
				+ "CUdevice dev = new CUdevice();" + "\n"
				+ " JCudaDriver.cuDeviceGet(dev, 0);" + "\n"
				+ " JCudaDriver.cuCtxCreate(pctx, 0, dev);" + "\n";
		return initializationCode;
	}

	private String loadCubinCode() {
		String loadCubin = "CUmodule module = new CUmodule();" + "\n"
				+ " JCudaDriver.cuModuleLoad(module, cubinFileName);" + "\n";
		return loadCubin;
	}

	private String getFunctionPointer() {
		String funcPointer = "CUfunction function = new CUfunction();"
				+ "\n"
				+ "JCudaDriver.cuModuleGetFunction(function, module, \"sampleKernel\");";
		return funcPointer;
	}

	private String getCubinFileDeclaration() {
		String cubinDeclaraion = "String cubinFileName = prepareCubinFile(\""
				+ cubinFileName + "\");";
		return cubinDeclaraion;
	}

	private String getToByteArrayCode() {
		String toByteArrayCode = "private static byte[] toByteArray(InputStream inputStream) throws IOException"
				+ "{"
				+ "\n"
				+ "ByteArrayOutputStream baos = new ByteArrayOutputStream();\n"
				+ " byte buffer[] = new byte[8192];\n"
				+ "while (true)\n"
				+ "{\n"
				+ "int read = inputStream.read(buffer);\n"
				+ "if (read == -1)\n"
				+ "{\n"
				+ "break;\n"
				+ "}\n"
				+ "baos.write(buffer, 0, read);\n"
				+ "}\n"
				+ "return baos.toByteArray();\n" + "}";
		return toByteArrayCode;
	}

	private String getPrepareCubinFileCode() {
		String prepareCubinFileCode = " private static String prepareCubinFile(String cuFileName) throws IOException"
				+ "\n"
				+ "{\n"
				+ "  int endIndex = cuFileName.lastIndexOf('.');"
				+ "\n"
				+ "  if (endIndex == -1)"
				+ "\n"
				+ " {\n"
				+ "endIndex = cuFileName.length()-1;"
				+ "}"
				+ "String cubinFileName = cuFileName.substring(0, endIndex+1)+\"cubin\";"
				+ " File cubinFile = new File(cubinFileName);"
				+ " if (cubinFile.exists())"
				+ " {"
				+ "return cubinFileName;"
				+ "}"
				+ "File cuFile = new File(cuFileName);"
				+ " if (!cuFile.exists())"
				+ "{"
				+ "throw new IOException(\"Input file not found: \"+cuFileName);"
				+ "}"
				+ "String modelString = \"-m\"+System.getProperty(\"sun.arch.data.model\");"
				+ " String command ="
				+ " \"nvcc \" + modelString +\" -arch sm_11 -cubin \"+"
				+ "cuFile.getPath()+\" -o \"+cubinFileName;"
				+ "System.out.println(\"Executing\n\"+command);"
				+ "Process process = Runtime.getRuntime().exec(command);"
				+ "String errorMessage = new String(toByteArray(process.getErrorStream()));"
				+ "String outputMessage = new String(toByteArray(process.getInputStream()));"
				+ "int exitValue = 0;"
				+ "try"
				+ "{"
				+ "exitValue = process.waitFor();"
				+ "}"
				+ "catch (InterruptedException e)"
				+ "{"
				+ "Thread.currentThread().interrupt();"
				+ "throw new IOException(\"Interrupted while waiting for nvcc output\", e);"
				+ "}"
				+ " System.out.println(\"nvcc process exitValue \"+exitValue);"
				+ "if (exitValue != 0)"
				+ "{"
				+ " System.out.println(\"errorMessage:\n\"+errorMessage);"
				+ "System.out.println(\"outputMessage:\n\"+outputMessage);"
				+ "throw new IOException(\"Could not create .cubin file: \"+errorMessage);"
				+ "}" + "return cubinFileName;" + "}";
		return prepareCubinFileCode;
	}

	@Override
	public void process() {
		System.out.println("Called the process.......................");

		ForLoopVisitor forLoopVisitor = new ForLoopVisitor();
		this.method.accept(forLoopVisitor);
		List<ForStatement> forLoops = forLoopVisitor.getForLoops();
		for (ForStatement s : forLoops) {
			List initialisers = s.initializers();
			Expression expression = s.getExpression();
			List updaters = s.updaters();

			for (Object obj : initialisers) {
				System.out.println("Initializers :"
						+ ((Expression) obj).toString());
			}
			for (Object obj : updaters) {
				System.out
						.println("Updaters: " + ((Expression) obj).toString());
			}
			System.out.println("Expression: " + expression.toString());
			InfixExpressionVisitor infixVisitor = new InfixExpressionVisitor();
			expression.accept(infixVisitor);
			System.out
					.println("Left operand:" + infixVisitor.getLeftHandSide());
			System.out.println("Condition operand: "
					+ infixVisitor.getOperator());
			System.out.println("Right operand: "
					+ infixVisitor.getRightHandSide());

			System.out.println("Body:" + s.getBody());
			s.getBody();
			ExpressionStatementVisitor expVisitior = new ExpressionStatementVisitor();
			s.getBody().accept(expVisitior);
			for (ExpressionStatement ex : expVisitior.getExpressionStatements()) {
				AssignmentVisitor asignVisitor = new AssignmentVisitor();
				ex.accept(asignVisitor);
				asignVisitor.getLeftHandSide();
				System.out.println("Left hand Side: "
						+ asignVisitor.getLeftHandSide());
				if (asignVisitor.getLeftHandSide().getNodeType() == Assignment.ARRAY_ACCESS) {
					System.out.println("array access");
					// String[]
					// array=asignVisitor.getLeftHandSide().toString().split("[");
					int count = 0;
					int lenght = asignVisitor.getLeftHandSide().toString()
							.length();
					String leftHandSide = asignVisitor.getLeftHandSide()
							.toString();
					for (int l = 0; l < lenght; l++) {
						if (leftHandSide.charAt(l) == '[') {
							count++;
						}
					}
					if(count>0){
						String pointer="";
						for(int h=0;h<count;h++){
							pointer=pointer+"*";
						}
						gloabalOutPut="float"+pointer+" globalOutputData";
						 System.out.println(gloabalOutPut);
					}
					 
					 
					// System.out.println(array[0]);
					// if(array.length==2){
					// gloabalOutPut="float* globalOutputData";
					// System.out.println(gloabalOutPut);
					// }
				}
				System.out.println("Right hand side: "
						+ asignVisitor.getRightHandSide());
				asignVisitor.getRightHandSide();
			}

			System.out.println(s.toString());
			// writeToCUFile(s);
		}
		System.out.println("end the process.......................");
	}

	@Override
	public void setMethod(MethodDeclaration method) {
		// TODO Auto-generated method stub
		this.method = method;
	}

	private void writeToCUFile(ForStatement s) {
		FileOutputStream fout;

		try {
			fout = new FileOutputStream("F:\\aaa77.cu");
			new PrintStream(fout).println(s.toString());
			fout.close();
		} catch (IOException e) {
			System.err.println("Unable to write to file: " + e.getMessage());
		}

		/*
		 * try { FileWriter fstream = new
		 * FileWriter("C:\\Users\\Lahiru\\Desktop\\abbb77.txt"); BufferedWriter
		 * out = new BufferedWriter(fstream); out.write("Hello Java");
		 * out.close(); } catch (Exception e) { System.err.println("Error: " +
		 * e.getMessage()); }
		 */

	}

}
