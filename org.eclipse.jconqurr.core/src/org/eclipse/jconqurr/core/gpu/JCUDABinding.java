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
			+ "\n"
			+ "{"
			+ "\n"
			+ "ByteArrayOutputStream baos = new ByteArrayOutputStream();"
			+ "\n"
			+ " byte buffer[] = new byte[8192];"
			+ "\n"
			+ "while (true)"
			+ "\n"
			+ "{"
			+ "\n"
			+ "int read = inputStream.read(buffer);"
			+ "\n"
			+ "if (read == -1)"
			+ "\n"
			+ "{"
			+ "\n"
			+ "break;"
			+ "\n"
			+ "}"
			+ "\n"
			+ "baos.write(buffer, 0, read);"
			+ "\n"
			+ "}"
			+ "\n"
			+ "return baos.toByteArray();" + "\n" + "}";

	private final static String getPrepareCubinFileCode = " private static String prepareCubinFile(String cuFileName) throws IOException"
			+ "\n"
			+ "{"
			+ "\n"
			+ "int endIndex = cuFileName.lastIndexOf('.');"
			+ "\n"
			+ "if (endIndex == -1)"
			+ "\n"
			+ "{"
			+ "\n"
			+ "endIndex = cuFileName.length()-1;"
			+ "}"
			+ "\n"
			+ "String cubinFileName = cuFileName.substring(0, endIndex+1)+\"cubin\";"
			+ "\n"
			+ "File cubinFile = new File(cubinFileName);"
			+ "\n"
			+ "if (cubinFile.exists())"
			+ "\n"
			+ "{"
			+ "\n"
			+ "return cubinFileName;"
			+ "\n"
			+ "}"
			+ "\n"
			+ "File cuFile = new File(cuFileName);"
			+ "\n"
			+ " if (!cuFile.exists())"
			+ "\n"
			+ "{"
			+ "\n"
			+ "throw new IOException(\"Input file not found: \"+cuFileName);"
			+ "\n"
			+ "}"
			+ "\n"
			+ "String modelString = \"-m\"+System.getProperty(\"sun.arch.data.model\");"
			+ "\n"
			+ " String command = \"nvcc \" + modelString +\" -arch sm_11 -cubin \"+cuFile.getPath()+\" -o \"+cubinFileName;"
			+ "\n"
			+ "System.out.println(\"Executing\"+command);"
			+ "\n"
			+ "Process process = Runtime.getRuntime().exec(command);"
			+ "\n"
			+ "String errorMessage = new String(toByteArray(process.getErrorStream()));"
			+ "\n"
			+ "String outputMessage = new String(toByteArray(process.getInputStream()));"
			+ "\n"
			+ "int exitValue = 0;"
			+ "\n"
			+ "try"
			+ "\n"
			+ "{"
			+ "\n"
			+ "exitValue = process.waitFor();"
			+ "\n"
			+ "}"
			+ "\n"
			+ "catch (InterruptedException e)"
			+ "\n"
			+ "{"
			+ "\n"
			+ "Thread.currentThread().interrupt();"
			+ "\n"
			+ "throw new IOException(\"Interrupted while waiting for nvcc output\", e);"
			+ "\n"
			+ "}"
			+ "\n"
			+ " System.out.println(\"nvcc process exitValue \"+exitValue);"
			+ "\n"
			+ "if (exitValue != 0)"
			+ "\n"
			+ "{"
			+ "\n"
			+ " System.out.println(\"errorMessage:\"+errorMessage);"
			+ "\n"
			+ "System.out.println(\"outputMessage:\"+outputMessage);"
			+ "\n"
			+ "throw new IOException(\"Could not create .cubin file: \"+errorMessage);"
			+ "\n" + "}" + "\n" + "return cubinFileName;" + "\n" + "}";

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
