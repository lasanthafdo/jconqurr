# Introduction #

JConqurr is a tool kit for Java that enables the developer to automatically convert their code to multi-threaded code. The first stage of the project comprises of developing a framework based on marker-annotations and directives that will convert the specified code segment suitable for parallel processing. In this stage, the tool kit would rely on the developer to place the markers correctly.
In the second stage, the tool kit would attempt to automatically identify parallel convertible code segments and insert the markers automatically.

# Details #

JConqurr is developed as a plug-in project for Eclipse IDE and is licensed under Eclipse Public License v1.0 . Currently the plug-in supports following parallel techniques.

## Parallel execution for Loops ##

Loops can be easily optimized for parallel execution. To convert a loop to parallel, place a directive above the loop statement.

## Pipeline Pattern ##

Uses the pipeline pattern to execute the program in parallel. The stages to be taken for the pipeline should be specified using the directives as well as the beginning and end of the code segment that need to be parallel-optimized. The ability to select the stages automatically is also available. However this feature is currently available as an experimental feature and available only in development builds.

## Split-join pattern ##

Uses split-join or fork-join pattern to execute code in parallel.

## Divide and Conquer ##

Uses divide and conquer technique to achieve parallelism. Directives should be used to mark the beginning and end of the code segment in focus.

## Parallel execution for Tasks ##

This pattern can be used to submit individual tasks to be run in parallel. Currently, it is the programmers responsibility to ensure that no dependencies exist between any of the tasks. However, we hope to integrate automatic dependency analysis to JConqurr in the near future.

## nVidia GPU utilization for parallel processing ##

GPU based parallel processing currently one of the hot topics for achieving performance and the CUDA platform seems to be becoming increasingly popular as means to achieve massive parallelism. In fact, current GeForce chip-sets use highly parallel and complex architectures for graphics processing that can be used for non-graphic-related applications as well. However, this feature is in its incubation stage as a lot of research and study is needed to effectively harness the power of these rapidly changing graphics chip-set architectures.