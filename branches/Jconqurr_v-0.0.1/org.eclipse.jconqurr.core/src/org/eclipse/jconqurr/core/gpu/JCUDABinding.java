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
	
	protected String getMemAllocFor2DHostVariablesCode(String variableName) {
		String hostDevicePointer = "hostDevicePointersFor_" + variableName;
		String comment = "//Allocate arrays on device, one for each row." + "\n" +
						 "//The pointers to these arrays are stored in host memory." + "\n";
		String initCUdeviceptr = "CUdeviceptr " + hostDevicePointer + "[] = new CUdeviceptr[numThreads];" + "\n";
		String forLoop = "for(int i = 0; i < numThreads; i++) {" + "\n" +
						 hostDevicePointer + "[i] = new CUdeviceptr();" + "\n" +
						 "JCudaDriver.cuMemAlloc(" + hostDevicePointer + "[i], size * Sizeof.INT);" + "\n" + "}";	
		
		return null;
	}
	
	protected String getCopyContentsFor2DHostVariablesCode(String variableName) {
		String hostDevicePointer = "hostDevicePointersFor_" + variableName;
		String comment = "//Copy the content of the rows from the host input data" + "\n" +
						 "//to the device arrays that have just been allocated." + "\n";
		String forLoop = "for(int i = 0; i < numThreads; i++) {" + "\n" +
		 				 "JCudaDriver.cuMemcpyHtoD(" + hostDevicePointer + "[i], Pointer.to(variableName[i]), size * Sizeof.INT);" + "\n" + "}";
		return null;
	}
	
	protected String getMemAllocForPointersCode(String variableName, String pointerName) {
		String deviceInput = "deviceInputFor_" + variableName;
		String comment = "//Allocate device memory for the array pointers," + "\n" +
		 				 "//and copy the array pointers from the host to the device." + "\n";
		String initDeviceInput = "CUdeviceptr " + deviceInput + " = new CUdeviceptr();" + "\n";
		String call1 = "JCudaDriver.cuMemAlloc(" + deviceInput + ", numThreads * Sizeof.POINTER);" + "\n";
		String call2 = "JCudaDriver.cuMemcpyHtoD(" + deviceInput + ", Pointer.to(" + pointerName + "), numThreads * Sizeof.POINTER);" + "\n";
		
		return comment + initDeviceInput + call1 + call2;
	}
	
	protected String getMemAllocDeviceOutputVariablesCode(String variableName) {
		String deviceOutput = "deviceOutputFor_" + variableName;
		String comment = "//Allocate device output memory: of a single column" + "\n";
		String initdeviceOutput = "CUdeviceptr " + deviceOutput + " = new CUdeviceptr();" + "\n";
		String call1 = "JCudaDriver.cuMemAlloc(" + deviceOutput + ", numThreads * Sizeof.INT);" + "\n";	
		
		return comment + initdeviceOutput + call1;
	}
	
	protected String getExePrametersCode(String InVariableName1, String InVariableName2, String OutVariableName) {
		String exePrameters = "JCudaDriver.cuFuncSetBlockShape(function, numThreads, 1, 1);"
			+ "\n"
			+ "Pointer dIn1 = Pointer.to(deviceInputFor_" + InVariableName1 + ");"
			+ "\n"
			+ "Pointer dIn2 = Pointer.to(deviceInputFor_" + InVariableName2 + ");"
			+ "\n"
			+ "Pointer dOut = Pointer.to(deviceOutputFor_" + OutVariableName + ");"
			+ "\n"
			+ "Pointer pSize = Pointer.to(new int[]{size}):"
			+ "\n"
			+ "int offset = 0;"
			+ "\n"
			+ "offset = JCudaDriver.align(offset, Sizeof.POINTER);"
			+ "\n"
			+ "JCudaDriver.cuParamSetv(function, offset, dIn1, Sizeof.POINTER);"
			+ "\n"
			+ "offset += Sizeof.POINTER"
			+ "\n"
			+ "offset = JCudaDriver.align(offset, Sizeof.POINTER);"
			+ "\n"
			+ "JCudaDriver.cuParamSetv(function, offset, dIn2, Sizeof.POINTER);"
			+ "\n"
			+ "offset += Sizeof.POINTER"
			+ "\n"
			+ "offset = JCudaDriver.align(offset, Sizeof.INT);"
			+ "\n"
			+ "JCudaDriver.cuParamSetv(function, offset, pSize, Sizeof.INT);"
			+ "\n"
			+ "offset += Sizeof.INT"
			+ "\n"
			+ "offset = JCudaDriver.align(offset, Sizeof.POINTER);"
			+ "\n"
			+ "JCudaDriver.cuParamSetv(function, offset, dOut, Sizeof.POINTER);"
			+ "\n"
			+ "offset += Sizeof.POINTER"
			+ "\n"
			+ "JCudaDriver.cuParamSetv(function, offset);"
			+ "\n"
			+ "//Call the function"
			+ "\n"
			+ "JCudaDriver.cuLaunch(function);"
			+ "\n"
			+ "JCudaDriver.cuCtxSynchronize();"
			+ "\n";
		return exePrameters;
	}
	
	protected String getMemAllocForHostOutputCode(String outputVariableName) {
		
		String comment = "//Allocate host output memory and copy the device output to the host.(one dimentional)" + "\n";
		String call1 = "JCudaDriver.cuMemcpyDtoH(Pointer.to(" + outputVariableName + "), deviceOutputFor_" + outputVariableName + ", numThreads * Sizeof.INT);" + "\n";
		
		return comment + call1;
	}
	
}
