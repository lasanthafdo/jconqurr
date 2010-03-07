package org.eclipse.jconqurr.core.gpu;

public final class JCUDABinding {
	private final static String DriverInitializationCode = " JCudaDriver.cuInit(0);"
			+ "\n"
			+ "CUcontext pctx = new CUcontext();"
			+ "\n"
			+ "CUdevice dev = new CUdevice();"
			+ "\n"
			+ " JCudaDriver.cuDeviceGet(dev, 0);"
			+ "\n"
			+ " JCudaDriver.cuCtxCreate(pctx, 0, dev);" + "\n";
	
	private final static String loadCubinCode = "CUmodule module = new CUmodule();"
			+ "\n" + " JCudaDriver.cuModuleLoad(module, cubinFileName);" + "\n";
	
	private final static String functionPointerCode = "CUfunction function = new CUfunction();"
			+ "\n"
			+ "JCudaDriver.cuModuleGetFunction(function, module, \"sampleKernel\");";

	private final static String ToByteArrayCode = "private static byte[] toByteArray(InputStream inputStream) throws IOException"
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
			+ "}\n" + "return baos.toByteArray();\n" + "}";
	
	private final static String getPrepareCubinFileCode = " private static String prepareCubinFile(String cuFileName) throws IOException"
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

	/**
	 * @return the driverinitializationcode
	 */
	protected static String getDriverinitializationcode() {
		return DriverInitializationCode;
	}

	/**
	 * @return the loadcubincode
	 */
	protected static String getLoadcubincode() {
		return loadCubinCode;
	}

	/**
	 * @return the functionpointercode
	 */
	protected static String getFunctionpointercode() {
		return functionPointerCode;
	}

	/**
	 * @return the tobytearraycode
	 */
	protected static String getTobytearraycode() {
		return ToByteArrayCode;
	}

	/**
	 * @return the getpreparecubinfilecode
	 */
	protected static String getGetpreparecubinfilecode() {
		return getPrepareCubinFileCode;
	}
}
