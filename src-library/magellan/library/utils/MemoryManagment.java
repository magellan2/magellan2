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

import magellan.library.utils.logging.Logger;

/**
 * Some functions for handling memory - control etc
 * 
 * @author Fiete
 * @version $326$
 */
public class MemoryManagment {
  public static Logger log = Logger.getInstance(MemoryManagment.class);

  /**
   * minimal amount of free memory after calling gc and sleeping for 3 seconds
   */
  static long minMemory = 20000;

  /**
   * after calling gc in case of low memory we wait this amount of millisecs
   */
  static long waitingMillis = 2000;

  private static Runtime r = java.lang.Runtime.getRuntime();

  public MemoryManagment() {

  }

  public static Runtime getRuntime() {
    return MemoryManagment.r;
  }

  /**
   * Ändert die Priotität des Finalizer-Threads.
   * 
   * @param newPriority Die Priorität, die der Finalizer-Thread bekommen soll. from Helge Stieghahn
   *          (Fiete)
   */
  public static final void setFinalizerPriority(int newPriority) {
    new FinalizerChanger(newPriority);
    java.lang.System.gc();
    java.lang.System.runFinalization();
  }

  /**
   * A class to make the finalizer thread max priority. from Helge Stieghahn (Fiete)
   */
  private final static class FinalizerChanger {
    final int m_priority;

    private FinalizerChanger(int priority) {
      m_priority = priority;
    }

    @Override
    public void finalize() {
      Thread.currentThread().setPriority(m_priority);
    }
  }

  public static boolean isFreeMemory() {
    return MemoryManagment.isFreeMemory(MemoryManagment.minMemory);
  }

  /**
   * checks, if there is enough free memory for the JVM if not, invokes the garbage collector if not
   * successful returns false, otherwise true
   * 
   * @return true, if enough memory available
   */
  public static boolean isFreeMemory(long min) {
    // Runtime r = java.lang.Runtime.getRuntime();
    if (MemoryManagment.checkFreeMemory(min))
      return true;
    MemoryManagment.r.runFinalization();
    MemoryManagment.r.gc();
    try {
      MemoryManagment.log.warn("waiting for garbage collection");
      Thread.sleep(MemoryManagment.waitingMillis);
    } catch (InterruptedException e) {
      // do nothing...
    }
    if (MemoryManagment.checkFreeMemory(min))
      return true;
    return false;
  }

  private static boolean checkFreeMemory(long min) {
    long free = MemoryManagment.r.freeMemory();
    long tot = MemoryManagment.r.totalMemory();
    long max = MemoryManagment.r.maxMemory();
    if (free > min)
      return true;

    MemoryManagment.log.warn("memory free: " + free + " needed: " + max + " total: " + tot);
    return false;
  }

  public static Runtime getR() {
    return MemoryManagment.r;
  }

}
