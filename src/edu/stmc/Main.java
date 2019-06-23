package edu.stmc;

import prism.PrismCL;
import prism.PrismException;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
  private static int repeat       = 1;
  private static int processCount = 1;

  public static void main(String[] args) throws PrismException, IOException, InterruptedException {
    args = new String[]{
    // "/opt/prism-4.5/prism-examples/dice/dice.pm",
    // "-pf", "P>0.1 [ F s=7 & d=1 ]",
    "/Users/nima/Dropbox/Research/Yu/codes/prism-4.4/examples/egl/egl.pm",
    "-const", "L=8",
    "-const", "N=12",
    "-pf", "P<0.51[F<100!kA&kB]",
    "-mt", "8",
    "-repeat", "40",
    "-sim",
    "-stmc",
    "-htm", "SPRT",
    "-alpha", "0.01",
    "-beta", "0.01",
    "-delta", "0.05",
    // "-gamma", "0.1",
    // "-miniter", "10",
    };

    /* We support two hidden arguments:
     * 1. -repeat <n> can be used to repeat the test multiple times. It is useful for testing statistical verification
     *    algorithms. Value of n must be at least 1.
     * 1. -multithread (or -mt) <n>, in which providing the value is optional, is used to run multiple instances at the
     *    same time (useful only when repeat is larger than one). Value of n must be at least 1. If it is not provided
     *    then the number of processors available to the Java virtual machine will be used, which is also the maximum
     *    number of processes that will be used (larger values will be silently lowered down to this number).
     *
     * All of these parameters are removed from the set of parameters that will be given to PRISM. */
    boolean      hasFileLog = false;
    List<String> params     = new ArrayList<>(Arrays.asList(args));
    for (int i = 0; i < params.size(); i++) {
      String sw = params.get(i);
      if (sw.length() > 0 && sw.charAt(0) == '-') {
        sw = sw.substring(1); // Remove "-"
        if (sw.length() == 0)
          throw new PrismException("Invalid empty switch");
        // Remove optional second "-" (i.e. we allow switches of the form --sw too)
        if (sw.charAt(0) == '-')
          sw = sw.substring(1);
        switch (sw) {
          case "mainlog":
            hasFileLog = true;
            break;
          case "multithread":
          case "mt":
            params.remove(i);
            processCount = Runtime.getRuntime().availableProcessors();
            if (i < params.size())
              try {
                processCount = Math.min(processCount, Integer.parseInt(params.get(i)));
                if (processCount < 1)
                  throw new PrismException("Number of threads (" + processCount + ") cannot be less than 1");
                params.remove(i);
                i--;
              } catch (NumberFormatException e) { /* no-op */ }
            i--;
            break;
          case "repeat":
            params.remove(i);
            if (i >= params.size())
              throw new PrismException("Missing value for -repeat switch");
            try {
              repeat = Integer.parseInt(params.get(i));
              if (repeat < 1)
                throw new PrismException("Value for -repeat switch ('" + params.get(i) + "') is too small (must be at least one)");
              params.remove(i);
              i--;
            } catch (NumberFormatException e) {
              throw new PrismException("Could not parse value of -repeat switch ('" + params.get(i) + "') as an integer");
            }
            i--;
            break;
        } // switch
      }
    } // for loop
    processCount = Math.min(processCount, repeat);
    if (hasFileLog && processCount > 1)
      throw new PrismException("Option mainlog is not supported when number of processes (" + processCount + ") is larger than 1");

    if (processCount == 1)
      for (int i = 0; i < repeat; i++) {
        System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        System.out.println("Iteration " + i);
        PrismCL.main(params.toArray(new String[0]));
      }
    else {

      String prismCmd = System.getenv("PRIM_JAVA_COMMAND");
      if (prismCmd == null)
        prismCmd =
        "/opt/java/jdk-11.0.2/bin/java " +
        "-Djava.library.path=/opt/prism-4.5/lib " +
        "-Dfile.encoding=UTF-8 " +
        "-classpath " +
        "/Users/nima/Git/Codes/Java/stmc/out/production/stmc:/opt/java/scala-2.12.8/lib/scala-library.jar:/opt/java/scala-2.12.8/lib/scala-swing_2.12-2.0.3.jar:/opt/java/scala-2.12.8/lib/scala-reflect.jar:/opt/java/scala-2.12.8/lib/scala-parser-combinators_2.12-1.0.7.jar:/opt/java/scala-2.12.8/lib/scala-xml_2.12-1.0.6.jar:/opt/prism-4.5/lib/prism.jar:/opt/prism-4.5/lib/colt.jar:/opt/prism-4.5/lib/epsgraphics.jar:/opt/prism-4.5/lib/jas.jar:/opt/prism-4.5/lib/jcommon.jar:/opt/prism-4.5/lib/jfreechart.jar:/opt/prism-4.5/lib/log4j.jar:/opt/prism-4.5/lib/lpsolve55j.jar:/opt/prism-4.5/lib/nailgun-server.jar:/opt/prism-4.5/lib/jhoafparser.jar:/opt/java/libs/scalatest_2.12-3.0.5.jar:/opt/java/libs/scalactic_2.12-3.0.5.jar " +
        "prism.PrismCL";
      final String[] prismCmds = prismCmd.split("\\s");
      for (int i = 0; i < prismCmds.length; i++)
        prismCmds[i] = prismCmds[i].trim();

      List<String> new_args = new ArrayList<>(Arrays.asList(prismCmds));
      new_args.addAll(params);

      ProcessBuilder bld = new ProcessBuilder(new_args);
      bld.redirectErrorStream(true);
      final File dir = new File("./ws");
      dir.mkdir();

      // Redirecting outputs to separate files so multithread won't mix the outputs
      // Execute a process and waiting for it to be done
      final ExecutorService pool = Executors.newFixedThreadPool(processCount);
      for (int i = 0; i < repeat; i++) {
        final int  iter    = i;
        final File outFile = File.createTempFile("process-", ".out", dir);
        outFile.deleteOnExit();
        pool.execute(() -> {
          try {
            final Process process;
            synchronized (bld) {
              bld.redirectOutput(ProcessBuilder.Redirect.to(outFile));
              process = bld.start();
            }
            process.waitFor();
            // Printing content of all those tmp files and deleting them afterwards
            synchronized (Math.class) {
              System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
              System.out.println("Iteration " + iter + ": " + outFile.getName());
              if (outFile.exists())
                try (final BufferedReader buff = new BufferedReader(new InputStreamReader(new FileInputStream(outFile)))) {
                  String line;
                  while ((line = buff.readLine()) != null)
                    System.out.println(line);
                }
            }
            outFile.delete();
          } catch (Exception e) {
            throw new Error(e);
          }
        });
      }
      pool.shutdown();
    }
  }
}
