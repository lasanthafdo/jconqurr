# JConqurr #

**JConqurr** is a Eclipse plug-in which provide multi-core programming support for Java developers. It provides developers a framework to mark code segments that can be converted to parallel executable code so that multi-core environments can be more effectively utilised. The main approach to marking code segments is through custom Java annotations and predefined directives that can be placed to mark the code segments.

The tool supports following parallel processing techniques.

  * Parallel processing of loops
  * Parallel processing of tasks
  * Divide and Conquer
  * Pipeline pattern
  * Split-join pattern
  * GPU utilization for parallel execution (supports the JCuda platform)

As the next step of the project, it is planned to develop automatic identification of code segments that can be converted to parallel. Development for this phase is still under way.

Further information can be found in our wiki pages and our blog.

[JConqurr Blog](http://jconqurr.blogspot.com/)
<br><a href='http://code.google.com/p/jconqurr/wiki/IntroductionToJConqurr'>Introduction</a>