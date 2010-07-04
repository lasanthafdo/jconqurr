package org.eclipse.jconqurr.directives;

public final class Directives {
	/**
	 * Marks the beginning of a method or code segment that can be parallelized using task parallelization
	 */
	public static final void startTask() {}

	/**
	 * Marks the end of a code segment that can be parallelized with task parallelization techniques.
	 * Note that this should be preceded by a {@link Directives#startTask()} directive.
	 */
	public static final void endTask() {}

	/**
	 * Marks the beginning of a for loop. The loop that is directly below this will be converted to parallel
	 * using loop parallelization techniques.
	 */
	public static final void forLoop() {}
	
	/**
	 * Marks the beginning of a for loop. The loop that is directly below will be converted to parallel.
	 * The specified number of threads will be created to handle the loop.
	 * @param nThreads
	 * 		the number of threads to be created to handle this loop
	 */
	public static final void forLoop(int nThreads) {}
	
	/**
	 * Marks where a barrier should be placed for the threads that have been created above this marker/directive
	 */
	public static final void barrier() {}

	/**
	 * Specifies input parameters for code that can be converted for parallel execution using
	 * pipelines
	 */
	public static final void pipelineInput() {}

	/**
	 * Marks the input parameters for a pipeline parallel convertible code
	 * @param input
	 * 		the input variables for this particular pipeline. The variable names should be specified
	 * 		as strings enclosed in quotation marks.
	 * @param output
	 * 		the output variables for this particular pipeline. The variable names should be specified as
	 * 		string literals enclosed in quotation marks. 
	 */
	public static final void pipelineInput(String input, String output){}

	/**
	 * Marks the beginning of a pipeline stage. If another pipeline stage is above this marker, this will be
	 * also considered as the end marker for the previous pipeline stage.
	 */
	public static final void pipelineStage() {}

	/**
	 * Marks the beginning of a pipeline stage. Provides parameters to specify input and output variables
	 * to the pipeline stage in addition to {@code pipelineStage()}.
	 * @param input
	 * 		the input variables for this pipeline stage to be passed in as string literals enclosed
	 * 		in quotation marks. Multiple input variables can be specified by comma separation inside
	 * 		the string literal. e.g.<p> {@code Directives.pipelineStage("inputVar1,inputVar2","outputVar")}
	 * @param output
	 * 		the output variables for this pipeline stage to be passed in as string literals enclosed
	 * 		in quotation marks.
	 */
	public static final void pipelineStage(String input, String output) {}

	/**
	 * Specifies the parameters for a pipeline split stage
	 * @param input
	 * 		the input for the pipeline split stage
	 * @param output
	 * 		the output for the pipeline split stage
	 * @param noOfSplitPaths
	 * 		the number of split paths to be considered
	 * @see Directives#pipelineStage()
	 */
	public static final void pipelineSplitStage(String input, String output,
			String noOfSplitPaths) {}

	/**
	 * Specifies the pipeline output of a pipeline
	 */
	public static final void pipelineOutput() {}

	/**
	 * Specifies the beginning of a pipeline convertible code segment
	 */
	public static final void pipelineStart() {}

	/**
	 * Marks the end of a pipeline convertible code. This marker should be preceded by a
	 * {@link Directives#pipelineStart()} directive.
	 */
	public static final void pipelineEnd() {}

	/**
	 * Marks a code segment that can be be processed using shared data parallelization
	 * @param shared
	 */
	public static final void shared(String shared) {}
}
