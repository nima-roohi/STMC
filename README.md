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

**_Note:_**
_The following steps are only tested on MacOS and Ubuntu._   


Prerequisites
-------------

1. Install Java (version 11 or higher) and 
    set the `JAVA_HOME` environment variable to the installation folder.
    After this step, entering `${JAVA_HOME}/bin/java -version` on terminal should 
    successfully print the current java version.
    
    For example:
    ```sh
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
    ```sh
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

**_Note:_**
_If you want to compile the source code, you are expected to have some experience working with IDEs and Java._
_Furthermore, most of STMC is written in Scala. So you need to know a little bit about this language as well._   

We use [IntelliJ IDEA (Community Edition)](https://www.jetbrains.com/idea/download/#section=mac)
for our project. 
Follow the next steps to compile and run the source code:

1. Clone the source code on your local computer.
    You can do this by executing `git clone https://github.com/nima-roohi/STMC` in your terminal.
1. Install the IDE and open/import STMC project folder in it.
1. Make sure your JDK is set properly (version 11 or above).
1. Make sure your Scala library is set property. 
    If don't have it already, IntelliJ can download it for you.
    It also has a great plugin for coding in Scala which you should install as well.
1. Change whatever you like in the source code.
1. Build the project.
    This can be done, for example, by selecting `Build Project` from the `Build` menu.
1. Rebuild the `stmc` artifact.
    This can be done, for example, by selecting `Build Artifacts...` and then `stmc > rebuild`
    from the `Build` menu.
    After this step, a Jar file called `stmc.jar` will be overwritten.
1. If you don't want to rebuild the artifact and just want to run the tool within the IDE, 
    so that you can debug your code, instead of the previous step, run 
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

Running the Benchmarks
----------------------

Running the benchmarks is fairly straightforward.
Open a terminal and enter a folder that you can write into.
For the rest of this section, we assume you are in your home folder and use `~` to denote that folder.
Follow the next steps:

1. Clone the source code on your local computer
    (if you don't have `git`, you can download the source code using your browser as well).
    ```sh
    ~$ git clone https://github.com/nima-roohi/STMC
    Cloning into 'STMC'...
    remote: Enumerating objects: 118, done.
    remote: Counting objects: 100% (118/118), done.
    remote: Compressing objects: 100% (71/71), done.
    remote: Total 2853 (delta 61), reused 88 (delta 36), pack-reused 2735
    Receiving objects: 100% (2853/2853), 33.16 MiB | 7.33 MiB/s, done.
    Resolving deltas: 100% (1358/1358), done.
    ~$ 
    ```
1. Enter STMC folder.
    ```sh
    ~$ cd ./STMC/
    ~/STMC$
    ```
1. Look at list of available examples and select one. For example, we choose `brp`.
    ```sh
    ~/STMC$ ls ./examples/
    brp	crowds	egl
    ~/STMC$ 
    ```
1. Run the benchmark.
    ```sh
    ~/STMC$ ./examples/brp/run.sh 
    ```
   This will run 68 statistical tests and attempts to run 20 symbolic tests
   (some of the symbolic ones might not survive the state space explosion).
   Furthermore, each of the statistical tests will be repeated 20 times using at 
   most 4 processes, so we can gather average times and number of samples.
   Therefore, be ready to give it quite a few hours before it finishes.
   
   The synchronization between different processes is carried out through ports.
   It is assumed port number `56437` is free. As a consequence, you cannot run 
   multiple benchmarks at the same time. There are two method for changing this 
   behavior.
   1. Disable multi-processes in the benchmark.
      For this method, simply remove all `-mt 4` from `~/STMC/examples/brp/run.sh`.
   1. Assign environment variable `STMC_PORT` to a free port of your choice. You 
      should also use this step when `56437` is taken by some other processes and 
      you want to have multi-processes feature anyway.
