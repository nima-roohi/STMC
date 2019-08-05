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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
  private static int repeat       = 1;
  private static int processCount = 1;

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
    args = new String[]{
    // "/opt/prism-4.5/prism-examples/dice/dice.pm",
    // "-pf", "P>0.1 [ F s=7 & d=1 ]",
    "/Users/nima/Dropbox/Research/Yu/codes/prism-4.4/examples/egl/egl.pm",
    "-const", "L=8",
    "-const", "N=12",
    "-pf", "P<0.51[F<100!kA&kB]",
    // "-mt", "8",
    // "-repeat", "40",
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

      String prismCmd = System.getenv("PRIM_JAVA_COMMAND");
      // In development, this parameter is barely set. So we set it to exactly what my IDE use. Dear Hackers,
      // please do NOT use the absolute paths you see here to screw up with my system (or even better do not
      // screw up with my system at all).
      if (prismCmd == null)
        prismCmd =
        "/opt/java/jdk-11.0.2/bin/java " +
        "-Djava.library.path=/opt/prism-4.5/lib " +
        "-Dfile.encoding=UTF-8 " +
        "-classpath " +
        "/Users/nima/Git/Codes/Java/stmc/out/production/stmc:/opt/java/scala-2.12.8/lib/scala-library.jar:/opt/java/scala-2.12.8/lib/scala-swing_2.12-2.0.3.jar:/opt/java/scala-2.12.8/lib/scala-reflect.jar:/opt/java/scala-2.12.8/lib/scala-parser-combinators_2.12-1.0.7.jar:/opt/java/scala-2.12.8/lib/scala-xml_2.12-1.0.6.jar:/opt/prism-4.5/lib/prism.jar:/opt/prism-4.5/lib/colt.jar:/opt/prism-4.5/lib/epsgraphics.jar:/opt/prism-4.5/lib/jas.jar:/opt/prism-4.5/lib/jcommon.jar:/opt/prism-4.5/lib/jfreechart.jar:/opt/prism-4.5/lib/log4j.jar:/opt/prism-4.5/lib/lpsolve55j.jar:/opt/prism-4.5/lib/nailgun-server.jar:/opt/prism-4.5/lib/jhoafparser.jar:/opt/java/libs/scalatest_2.12-3.0.5.jar:/opt/java/libs/scalactic_2.12-3.0.5.jar " +
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
    }
  }
}
