STMC - Statistical Model Checker
================================

STMC is a statistical model checker that uses _stratified_ or _antithetic_ sampling techniques to 
reduce sample variance and hence required number of samples to make a statistical decision.
It uses [PRISM](https://www.prismmodelchecker.org/) for loading probabilistic model and simulation.
However, it simulation engine is improved by STMC to make stratified and antithetic sampling possible.
Please visit the [Tool Page](https://github.com/nima-roohi/STMC) for 
a short introduction on stratified and antithetic samplings,
a list of related publications, and
our benchmarks. 
Here we explain how to install and run examples and benchmarks.


Prerequisites:
--------------

1. Install Java (version 11 or higher), and 
    set the `JAVA_HOME` environment variable to installation folder.
    `${JAVA_HOME}/bin/java`.
1. Install PRISM (version 4.5).




1. If you want to compile the code then install Scala 2.12.8.

