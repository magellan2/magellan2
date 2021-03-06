/*
 *  Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe,
 *                          Stefan Goetz, Sebastian Pappert,
 *                          Klaas Prause, Enno Rehling,
 *                          Sebastian Tusk, Ulrich Kuester,
 *                          Ilja Pavkovic
 *
 * This file is part of the Eressea Java Code Base, see the
 * file LICENSING for the licensing information applying to
 * this file.
 *
 */

package magellan.library.utils;

/**
 * DOCUMENT-ME
 * 
 * @author $Author: $
 * @version $Revision: 171 $
 */
public class Sizeof {
  /**
   * DOCUMENT-ME
   * 
   * @throws Exception DOCUMENT-ME
   */
  public static void main(String args[]) throws Exception {
    // Warm up all classes/methods we will use
    Sizeof.runGC();
    Sizeof.usedMemory();

    // Array to keep strong references to allocated objects
    final int count = 100000;
    Object objects[] = new Object[count];

    long heap1 = 0;

    // Allocate count+1 objects, discard the first one
    for (int i = -1; i < count; ++i) {
      Object object = null;

      // Instantiate your data here and assign it to object
      object = new Object();

      // object = new Integer (i);
      // object = new Long (i);
      // object = new String ();
      // object = new byte [128][1]
      if (i >= 0) {
        objects[i] = object;
      } else {
        object = null; // Discard the warm up object
        Sizeof.runGC();
        heap1 = Sizeof.usedMemory(); // Take a before heap snapshot
      }
    }

    Sizeof.runGC();

    long heap2 = Sizeof.usedMemory(); // Take an after heap snapshot:

    final int size = Math.round(((float) (heap2 - heap1)) / count);
    System.out.println("'before' heap: " + heap1 + ", 'after' heap: " + heap2);
    System.out.println("heap delta: " + (heap2 - heap1) + ", {" + objects[0].getClass()
        + "} size = " + size + " bytes");

    for (int i = 0; i < count; ++i) {
      objects[i] = null;
    }

    objects = null;
  }

  private static void runGC() throws Exception {
    // It helps to call Runtime.gc()
    // using several method calls:
    for (int r = 0; r < 4; ++r) {
      Sizeof._runGC();
    }
  }

  private static void _runGC() throws Exception {
    long usedMem1 = Sizeof.usedMemory();
    long usedMem2 = Long.MAX_VALUE;

    for (int i = 0; (usedMem1 < usedMem2) && (i < 500); ++i) {
      Sizeof.s_runtime.runFinalization();
      Sizeof.s_runtime.gc();
      Thread.yield();

      usedMem2 = usedMem1;
      usedMem1 = Sizeof.usedMemory();
    }
  }

  private static long usedMemory() {
    return Sizeof.s_runtime.totalMemory() - Sizeof.s_runtime.freeMemory();
  }

  private static final Runtime s_runtime = Runtime.getRuntime();
}

// End of class
