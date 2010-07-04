package org.eclipse.jconqurr.directives;

public final class Directives {
public static final void startTask(){};
public static final void endTask(){};
public static final void forLoop(){};
public static final void barrier(){};
public static final void pipelineInput(){};
public static final void pipelineInput(String input,String output){};
public static final void pipelineStage(){};
public static final void pipelineStage(String input,String output){};
public static final void pipelineSplitStage(String input,String output,String noOfSplitPaths){};
public static final void pipelineOutput(){};
public static final void pipelineStart(){};
public static final void pipelineEnd(){};
public static final void shared(String shared){};
}
 