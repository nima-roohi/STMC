STMC - Statistical Model Checker
================================

STMC is a statistical model checker that uses _stratified_ or _antithetic_ sampling techniques to 
reduce sample variance and hence required number of samples to make a statistical decision.
It uses [PRISM](https://www.prismmodelchecker.org/) for loading probabilistic models and their simulation.
The simulation engine is improved by STMC to make stratified and antithetic sampling possible.
Please visit the [Tool Page](https://github.com/nima-roohi/STMC) for 
a short introduction on stratified and antithetic sampling techniques,
a list of related publications, and
our benchmarks. 
Here we explain how to install and run examples and benchmarks.


Prerequisites
-------------

1. Install Java (version 11 or higher) and 
    set the `JAVA_HOME` environment variable to the installation folder.
    After this step, entering `${JAVA_HOME}/bin/java -version` on terminal should 
    successfully print the current java version.
    
    For example:
    ```
    ~$ ${JAVA_HOME}/bin/java -version
    java version "11.0.2" 2019-01-15 LTS
    Java(TM) SE Runtime Environment 18.9 (build 11.0.2+9-LTS)
    Java HotSpot(TM) 64-Bit Server VM 18.9 (build 11.0.2+9-LTS, mixed mode)
    ~$ 
    ``` 
1. Install PRISM (version 4.5) and 
    set the `PRISM_HOME` environment variable to the installation folder.
    After this step, entering `${PRISM_HOME}/bin/prism -version` on terminal should 
    successfully print the current version of PRISM.
    
    For example:
    ```
    ~$ ${PRISM_HOME}/bin/prism -version
    PRISM version 4.5
    ~$ 
    ``` 

Compiling the Source Code (Optional)
------------------------------------

This repository already contains the binary version.
It only has Java bytecode which means no compilation is required to run 
the examples or benchmarks.
However, if one wishes to change the code then they must carry the compilation step as well.

__Note:__ 
_If you want to compile the source, you are expected to have some experience working with IDEs and JDK._
_Furthermore, most of STMC is written in Scala. So you need to know a little bit about this language as well._   

We use [IntelliJ IDEA (Community Edition)](https://www.jetbrains.com/idea/download/#section=mac)
for our project. 
Follow the following steps to compile and run the source code:

1. Install the IDE and open/import STMC project folder in it.
1. Make sure your JDK is set properly (version 11 or above).
1. Make sure your Scala library is set property. 
    If don't have it already, IntelliJ can download it for you.
    It also has a great plugin for coding in Scala which you should install as well.
1. Change whatever you like in the source code.
1. Build the project.
    This can be done, for example, by selecting `Build Project` from the `Build` menu.
1. Rebuild the `stmc` artifact.
    This can be done, for example, by selecting `Build Artifacts...` and then `stmc>rebuild`
    from the `Build` menu.
    After this step, a Jar file called `stmc.jar` will be overwritten.
1. If you don't want to rebuild the artifact and just want to run the tool within the IDE, 
    so that you can debug you code, instead of the previous step, run 
    `src/edu/stmc/Main.scala` file by, for example, right-clicking on the file in the Navigation Pane
    and selecting `Run 'Main.Main()'`.
    
    Since STMC uses PRISM and since PRISM uses native libraries, our first attempt to run the code
    will most likely end up with `UnsatisfiedLinkError`. However, we now have a lunch configuration 
    that we edit, by for example selecting `Edit Configurations...` from the `Run` menu, to fix this 
    problem as well. In the following steps, make sure to replace `/opt/prism-4.5` with the folder 
    you installed PRISM in.
    1. Enter `-Djava.library.path=/opt/prism-4.5/lib` for `VM options`.         
    1. If you are on Linux, enter `LD_LIBRARY_PATH=/opt/prism-4.5/lib` for `Environment variables`.
    1. If you are on Mac, enter `DYLD_LIBRARY_PATH=/opt/prism-4.5/lib` for `Environment variables`.
    
    We are now ready to run the source code, which is already explained at the beginning of this step.


STMC does not uses 


1. If you want to compile the code then install Scala 2.12.8.

