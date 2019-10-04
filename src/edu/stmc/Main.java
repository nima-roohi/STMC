/*+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 + STMC - Statistical Model Checker                                                               +
 +                                                                                                +
 + Copyright (C) 2019                                                                             +
 + Authors:                                                                                       +
 +   Nima Roohi <nroohi@ucsd.edu> (University of California San Diego)                            +
 +                                                                                                +
 + This program is free software: you can redistribute it and/or modify it under the terms        +
 + of the GNU General Public License as published by the Free Software Foundation, either         +
 + version 3 of the License, or (at your option) any later version.                               +
 +                                                                                                +
 + This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;      +
 + without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.      +
 + See the GNU General Public License for more details.                                           +
 +                                                                                                +
 + You should have received a copy of the GNU General Public License along with this program.     +
 + If not, see <https://www.gnu.org/licenses/>.                                                   +
 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

package edu.stmc;

import prism.PrismCL;
import prism.PrismException;

import java.io.*;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
  static {
    Runtime.Version v = Runtime.version();
    if (v.feature() < 11)
      throw new Error("Java version is " + v + " (its feature value is " + v.feature() + "). However, it must be at least 11.");
  }

  private static int repeat       = 1;
  private static int processCount = 1;

  private static double timeAvg    = 0;
  private static double timeM2     = 0;
  private static double samplesAvg = 0;
  private static double samplesM2  = 0;
  private static int    iters      = 0;

  private static void actualUpdateTotal(final double time, final double samples) {
    iters++;
    {
      double delta = time - timeAvg;
      timeAvg += delta / iters;
      double delta2 = time - timeAvg;
      timeM2 += delta * delta2;
    }
    {
      double delta = samples - samplesAvg;
      samplesAvg += delta / iters;
      double delta2 = samples - samplesAvg;
      samplesM2 += delta * delta2;
    }
  }

  // When multiple instances are running, we use socket to synchronize
  private static int getPortNumber() {
    try {
      return Integer.parseInt(System.getenv().getOrDefault("STMC_PORT", "56437"));
    } catch (Exception e) {
      return 56437;
    }
  }
  private static final int    port    = getPortNumber();
  private static       Thread updater = new Thread(() -> {
    try {
      try (ServerSocket ss = new ServerSocket(port)) {
        while (true) {
          Socket          s       = ss.accept();
          DataInputStream in      = new DataInputStream(s.getInputStream());
          double          time    = in.readDouble();
          double          samples = in.readDouble();
          s.close();
          actualUpdateTotal(time, samples);
        }
      }
    } catch (IOException e) {
      // silent
    }
  });

  public static void updateTotal(final double time, final double samples) {
    try {
      Socket           s   = new Socket("localhost", port);
      DataOutputStream out = new DataOutputStream(s.getOutputStream());
      out.writeDouble(time);
      out.writeDouble(samples);
      out.flush();
      out.close();
    } catch (ConnectException e) {
      // Socket is not ready, so it is likely that we are dealing with multi-thread
      actualUpdateTotal(time, samples);
    } catch (IOException e) {
      throw new Error(e);
    }
  }

  /**
   * Remove the first character of the input string if it is equal to '-'. If the first character is removed then
   * remove the second character of the input string if it is also equal to '-'. Input string won't be trimmed.
   * @param sw input switch
   * @return null if nothing is removed. Otherwise, the new string.
   * @throws PrismException if after removing '-' the switch becomes empty.
   */
  private static String adjustSwitch(String sw) throws PrismException {
    if (sw.length() > 0 && sw.charAt(0) == '-') {
      sw = sw.substring(1); // Remove "-"
      if (sw.length() == 0)
        throw new PrismException("Invalid empty switch");
      // Remove optional second "-" (i.e. we allow switches of the form --sw too)
      if (sw.charAt(0) == '-')
        sw = sw.substring(1);
      if (sw.length() == 0)
        throw new PrismException("Invalid empty switch");
      return sw;
    }
    return null;
  }
  public static void main(String[] args) throws PrismException, IOException, InterruptedException {
    // args = new String[]{
    // // "/opt/prism-4.5/prism-examples/dice/dice.pm",
    // // "-pf", "P=? [ F s=7 & d=1 ]", // actual probability: 0.16666650772094727
    //
    // // "./examples/egl/egl.pm",
    // // "-const", "L=8",
    // // "-const", "N=12",
    // // "-pf", "P=?[F<100!kA&kB]",
    //
    // // "./examples/crowds/crowds.pm",
    // // "-const", "CrowdSize=20",
    // // "-const", "TotalRuns=11",
    // // "-pf", "P<0.15[F<100observe0>1]",
    // // "-sparse",
    //
    // // "./examples/brp/brp.pm",
    // // "-const", "MAX=15",
    // // "-const", "N=4096",
    // // "-pf", "P<0.39[F<100s=3]", // actual probability: 0.38371680610076186
    // // "-mtbdd",
    //
    // "./examples/fms/fms.sm",
    // "-const", "n=7",
    // "-pf", "P<0.5[F<4P1=0&P2=0&P3=0]",
    // // "-mtbdd",
    //
    // "-sim",
    // "-repeat", "10",
    // "-mt", "6",
    //
    // // "-simmethod","sprt",
    // // "-simconf","0.001",
    // // "-simwidth","0.001",
    //
    // "-stmc",
    // "-htm", "SSPRT",
    // "-sm", "stratified",
    // "-ss", "2,2,2,2,2,2,2,2,2,2,2,2",
    // "-alpha", "0.0001",
    // "-beta", "0.0001",
    // "-delta", "0.0001",
    // "-min_iter", "2",
    // // "-gamma", "0.001",
    // };

    /* We support two hidden arguments:
     * 1. -repeat <n> can be used to repeat the test multiple times. It is useful for testing statistical verification
     *    algorithms. Value of n must be at least 1.
     * 1. -multithread (or -mt) <n>, in which n optional, is used to run multiple instances at the same time (useful
     *    only when repeat is larger than one). Value of n, if present, must be at least 1. If it is not provided
     *    then the number of processors available to the Java virtual machine will be used, which is also the maximum
     *    number of processes that will be used (larger values will be silently lowered down to this number).
     *
     * Strictly speaking, multithread will create multiple processes and not threads. This to guarantee thread safety.
     * All of these parameters are removed from the set of parameters that will be given to PRISM.
     * If multithread is used then -mainlog (a hidden parameter in Prism) must not be set. */
    boolean      hasFileLog = false;
    List<String> params     = new ArrayList<>(Arrays.asList(args));
    for (int i = 0; i < params.size(); i++) {
      String sw = adjustSwitch(params.get(i));
      if (sw != null) {
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
                throw new PrismException("Value for -repeat switch ('" + params.get(i) + "') must be at least 1");
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

    final String SEP = "+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++";
    if (processCount == 1)
      for (int i = 0; i < repeat; i++) {
        if (i > 0)
          System.out.println(SEP);
        if (repeat > 1)
          System.out.println("Repeat Number: " + i);
        PrismCL.main(params.toArray(new String[0]));
      }
    else {
      updater.setDaemon(true);
      updater.start();
      String PRISM_HOME = System.getenv().getOrDefault("PRISM_HOME", "/opt/prism-4.5");
      String JAVA_HOME  = System.getenv().get("JAVA_HOME");
      String JAVA_CMD   = "java";
      if (JAVA_HOME != null)
        JAVA_CMD = new File(JAVA_HOME + File.separator + "bin" + File.separator + "java").getAbsolutePath();

      String prismCmd = System.getenv("PRIM_JAVA_COMMAND");
      // In development, this parameter is barely set. So we set it to exactly what my IDE use. Dear Hackers,
      // please do NOT use the absolute paths you see here to screw up with my system (or even better do not
      // screw up with my system at all).
      if (prismCmd == null)
        prismCmd =
        JAVA_CMD + " " +
        "-Djava.library.path=" + PRISM_HOME + "/lib " +
        "-Dfile.encoding=UTF-8 " +
        "-classpath " +
        "./out/production/stmc:./out/artifacts/stmc/stmc.jar:" + PRISM_HOME + "/lib/prism.jar:" + PRISM_HOME + "/lib/colt.jar:" + PRISM_HOME + "/lib/epsgraphics.jar:" + PRISM_HOME + "/lib/jas.jar:" + PRISM_HOME + "/lib/jcommon.jar:" + PRISM_HOME + "/lib/jfreechart.jar:" + PRISM_HOME + "/lib/log4j.jar:" + PRISM_HOME + "/lib/lpsolve55j.jar:" + PRISM_HOME + "/lib/nailgun-server.jar:" + PRISM_HOME + "/lib/jhoafparser.jar:/opt/java/libs/scalatest_2.12-3.0.5.jar:/opt/java/libs/scalactic_2.12-3.0.5.jar " +
        "prism.PrismCL";
      final String[] prismCmds = prismCmd.split("\\s+");
      for (int i = 0; i < prismCmds.length; i++)
        prismCmds[i] = prismCmds[i].trim();

      // Combining prism command and input arguments to create a new command (used to run different processors).
      List<String> new_args = new ArrayList<>(Arrays.asList(prismCmds));
      new_args.addAll(params);

      ProcessBuilder bld = new ProcessBuilder(new_args);
      bld.redirectErrorStream(true);
      File dir = new File("./ws");
      if (!dir.mkdir()) dir = null;
      else dir.deleteOnExit();

      // 1. Redirecting outputs to separate files so multithread won't mix the outputs
      // 2. Execute a process and waiting for it to be done
      final ExecutorService pool = Executors.newFixedThreadPool(processCount);
      for (int i = 0; i < repeat; i++) {
        final int  iter    = i;
        final File outFile = File.createTempFile("process-", ".out", dir);
        pool.execute(() -> {
          try {
            final Process process;
            synchronized (bld) {
              bld.redirectOutput(ProcessBuilder.Redirect.to(outFile));
              process = bld.start();
            }
            process.waitFor();
            // Printing content of a tmp file and deleting it afterwards
            synchronized (Math.class) {
              System.out.println(SEP);
              System.out.println("Repeat Number " + iter + ": " + outFile.getAbsolutePath());
              try (final BufferedReader buff = new BufferedReader(new InputStreamReader(new FileInputStream(outFile)))) {
                String line;
                while ((line = buff.readLine()) != null)
                  System.out.println(line);
              }
            }
            // Try to delete, only if no exception is raised so far (we won't delete these files even if timeout occurs)
            if (!outFile.delete())
              outFile.deleteOnExit();
          } catch (Exception e) {
            throw new Error(e);
          }
        });
      }
      pool.shutdown();
      pool.awaitTermination(10000, TimeUnit.DAYS);
    }

    if (repeat > 1) {
      System.out.print("Time: average=" + timeAvg + ", standard-error=" + Math.sqrt(timeM2 / (iters - 1) / repeat));
      System.out.println("\nSamples: average=" + samplesAvg + ", standard-error=" + Math.sqrt(samplesM2 / (iters - 1) / repeat));
    }

  }
}
