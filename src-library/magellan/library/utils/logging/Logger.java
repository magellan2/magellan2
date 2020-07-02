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

package magellan.library.utils.logging;

import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

//import org.apache.log4j.*;

/**
 * Logs events in different levels.
 * 
 * @author Ilja Pavkovic
 */
public class Logger {
  // private Category ivTraceLog;

  /** This log level entirely stops all logging */
  public static final int OFF = 0;

  /** Fatal messages are messages which are printed before a fatal program exit */
  public static final int FATAL = 1;

  /** Error messages are printed if an error occurs but the application can continue */
  public static final int ERROR = 2;

  /** Warning messages are info messages with warning character */
  public static final int WARN = 3;

  /** Info messages are printed for informational purposes */
  public static final int INFO = 4;

  /** Fine messages are more fine grained than INFO messages, but not yet DEBUG */
  public static final int FINE = 5;

  /** Fine messages are more fine grained than INFO messages, but not yet DEBUG */
  public static final int FINER = 6;

  /** Fine messages are more fine grained than INFO messages, but not yet DEBUG */
  public static final int FINEST = 7;

  /** Debug messages are printed for debugging purposes */
  public static final int DEBUG = 8;

  /** AWT messages are printed for debugging awt purposes */
  public static final int AWT = 9;

  public static final int MAX_LEVEL = 9;

  private static Logger DEFAULT = new Logger("");
  private static int verboseLevel = Logger.INFO;
  private static Object awtLogger = null;
  private static boolean searchAwtLogger = true;
  private static LogListener DEFAULTLOGLISTENER = new DefaultLogListener();

  private static Set<Object> onceWarnings = null;
  private static Set<Object> onceErrors = null;

  private static boolean activateDefaultLogListener = false;

  private Logger(String aBase) {
    // be fail-fast
    if (aBase == null)
      throw new NullPointerException();
  }

  /**
   * Returns a logger "personalized" for some class.
   */
  public static Logger getInstance(Class<?> aClass) {
    // be fail-fast
    if (aClass == null)
      throw new NullPointerException();

    return Logger.getInstance(aClass.getName());
  }

  /**
   * Returns a logger with a specified name.
   */
  public static Logger getInstance(String aBase) {
    // be fail-fast
    if (aBase == null)
      throw new NullPointerException();

    // pavkovic 2004.02.04: we dont take any advantage
    // of different loggers, so reduce memory footprint
    // of Magellan
    // return new Logger(aBase);
    return Logger.DEFAULT;
  }

  /**
   * Set the logging level. Currently supported levels are {@link Logger#FATAL},
   * {@link Logger#ERROR}, {@link Logger#WARN}, {@link Logger#INFO}, {@link Logger#FINE},
   * {@link Logger#DEBUG}, {@link Logger#AWT}.
   * 
   * @param level The new log level.
   */
  public static void setLevel(int level) {
    Logger.verboseLevel = level;
  }

  /**
   * 
   */
  public static void setLevel(String aLevel) {
    String level = aLevel.toUpperCase();

    if (level.startsWith("O")) {
      Logger.setLevel(Logger.OFF);
    }

    if (level.startsWith("F")) {
      Logger.setLevel(Logger.FATAL);
    }

    if (level.startsWith("E")) {
      Logger.setLevel(Logger.ERROR);
    }

    if (level.startsWith("W")) {
      Logger.setLevel(Logger.WARN);
    }

    if (level.startsWith("I")) {
      Logger.setLevel(Logger.INFO);
    }

    if (level.startsWith("N")) {
      Logger.setLevel(Logger.FINE);
    }

    if (level.startsWith("R")) {
      Logger.setLevel(Logger.FINER);
    }

    if (level.startsWith("S")) {
      Logger.setLevel(Logger.FINEST);
    }

    if (level.startsWith("D")) {
      Logger.setLevel(Logger.DEBUG);
    }

    if (level.startsWith("A")) {
      Logger.setLevel(Logger.AWT);
    }
  }

  /**
   * @return The current verbosity level
   */
  public static int getLevel() {
    return Logger.verboseLevel;
  }

  /**
   * @return The string representation of <code>level</code>
   */
  public static String getLevel(int level) {
    if (level <= Logger.OFF)
      return "OFF";

    switch (level) {
    case FATAL:
      return "FATAL";

    case ERROR:
      return "ERROR";

    case WARN:
      return "WARN";

    case INFO:
      return "INFO";

    case FINE:
      return "FINE";

    case FINER:
      return "FINER";

    case FINEST:
      return "FINEST";

    case DEBUG:
      return "DEBUG";

    default:
      return "ALL";
    }
  }

  /**
   * 
   */
  public void log(int aLevel, Object aObj, Throwable aThrowable) {
    if (Logger.verboseLevel >= aLevel) {
      for (LogListener l : Logger.logListeners) {
        l.log(aLevel, aObj, aThrowable);
      }
      if (Logger.activateDefaultLogListener) {
        Logger.DEFAULTLOGLISTENER.log(aLevel, aObj, aThrowable);
      }
    }
  }

  /**
   * Activates or de-activates the default listener, which logs to {@link System#err}.
   */
  public static void activateDefaultLogListener(boolean activate) {
    Logger.activateDefaultLogListener = activate;
  }

  /**
   * Logs a message at the {@link #FATAL} level.
   */
  public void fatal(Object aObj) {
    fatal(aObj, null);
  }

  /**
   * Logs a message at the {@link #FATAL} level.
   */
  public void fatal(Object aObj, Throwable aThrowable) {
    log(Logger.FATAL, aObj, aThrowable);
  }

  /**
   * Returns true if the log level is at least {@link #FATAL}.
   */
  public boolean isFatalEnabled() {
    return Logger.verboseLevel >= Logger.FATAL;
  }

  /**
   * Logs a message at the {@link #ERROR} level.
   */
  public void error(Object aObj) {
    error(aObj, null);
  }

  /**
   * Logs a message at the {@link #ERROR} level, but only if no equal message has been logged
   * before.
   */
  public void errorOnce(Object aObj) {
    if (Logger.onceErrors == null) {
      // create new list
      Logger.onceErrors = new HashSet<Object>();
    }
    if (Logger.onceErrors.contains(aObj))
      // already processed error
      return;
    // add to errors - list
    Logger.onceErrors.add(aObj);
    // normal call to Logger.error
    error(aObj);
  }

  /**
   * Stops remembering an object for {@link #errorOnce(Object)}.
   */
  public void clearError(Object aObj) {
    if (Logger.onceErrors != null) {
      Logger.onceErrors.remove(aObj);
    }
  }

  /**
   * Logs a message at the {@link #ERROR} level.
   */
  public void error(Object aObj, Throwable aThrowable) {
    log(Logger.ERROR, aObj, aThrowable);
  }

  /**
   * Returns true if log level is at least {@link #ERROR}.
   */
  public boolean isErrorEnabled() {
    return Logger.verboseLevel >= Logger.ERROR;
  }

  /**
   * Logs a message at the {@link #WARN} level.
   */
  public void warn(Object aObj) {
    warn(aObj, null);
  }

  /**
   * Logs a message at the {@link #WARN} level, but only if no equals event has been logged before.
   */
  public void warnOnce(Object aObj) {
    if (Logger.onceWarnings == null) {
      // create new list
      Logger.onceWarnings = new HashSet<Object>();
    }
    if (Logger.onceWarnings.contains(aObj))
      // already processed warning
      return;
    // add to warnings - list
    Logger.onceWarnings.add(aObj);
    // normal call to Logger.warn
    warn(aObj);
  }

  /**
   * Logs a message at the {@link #WARN} level.
   */
  public void warn(Object aObj, Throwable aThrowable) {
    log(Logger.WARN, aObj, aThrowable);
  }

  /**
   * Returns true if log level is at least {@link #WARN}.
   */
  public boolean isWarnEnabled() {
    return Logger.verboseLevel >= Logger.WARN;
  }

  /**
   * Logs a message at the {@link #INFO} level.
   */
  public void info(Object aObj) {
    info(aObj, null);
  }

  /**
   * Logs a message at the {@link #INFO} level.
   */
  public void info(Object aObj, Throwable aThrowable) {
    log(Logger.INFO, aObj, aThrowable);
  }

  /**
   * Returns true if level is at least {@link #INFO}.
   */
  public boolean isInfoEnabled() {
    return Logger.verboseLevel >= Logger.INFO;
  }

  /**
   * Logs a message at the {@link #FINE} level.
   */
  public void fine(Object aObj) {
    fine(aObj, null);
  }

  /**
   * Logs a message at the {@link #FINE} level.
   */
  public void fine(Object aObj, Throwable aThrowable) {
    log(Logger.FINE, aObj, aThrowable);
  }

  /**
   * Logs a message at the {@link #FINER} level.
   */
  public void finer(Object aObj) {
    finer(aObj, null);
  }

  /**
   * Logs a message at the {@link #FINER} level.
   */
  public void finer(Object aObj, Throwable aThrowable) {
    log(Logger.FINER, aObj, aThrowable);
  }

  /**
   * Logs a message at the {@link #FINEST} level.
   */
  public void finest(Object aObj) {
    finest(aObj, null);
  }

  /**
   * Logs a message at the {@link #FINEST} level.
   */
  public void finest(Object aObj, Throwable aThrowable) {
    log(Logger.FINEST, aObj, aThrowable);
  }

  /**
   * Returns true if the level is at least {@link #FINE}.
   */
  public boolean isFineEnabled() {
    return Logger.verboseLevel >= Logger.FINE;
  }

  /**
   * Logs a message at the {@link #DEBUG} level.
   */
  public void debug(Object aObj) {
    debug(aObj, null);
  }

  /**
   * Logs a message at the {@link #DEBUG} level.
   */
  public void debug(Object aObj, Throwable aThrowable) {
    log(Logger.DEBUG, aObj, aThrowable);
  }

  /**
   * Returns true if the level is at least {@link #DEBUG}.
   */
  public boolean isDebugEnabled() {
    return Logger.verboseLevel >= Logger.DEBUG;
  }

  /**
   * Logs an {@link #AWT} level message.
   */
  public void awt(Object aObj) {
    awt(aObj, null);
  }

  /**
   * Logs an {@link #AWT} level message.
   */
  public void awt(Object aObj, Throwable aThrowable) {
    log(Logger.AWT, aObj, aThrowable);

    if (isAwtEnabled()) {
      if (Logger.searchAwtLogger) {
        Logger.searchAwtLogger = false;

        try {
          Constructor<?> constructor = Class.forName("magellan.library.utils.logging.AWTLogger")
              .getConstructor();
          Logger.awtLogger = constructor.newInstance();
        } catch (ClassNotFoundException e) {
          debug("AWTLogger not found", e);
        } catch (InstantiationException e) {
          debug("Cannot instanciate AWTLogger", e);
        } catch (IllegalAccessException e) {
          debug("Cannot access AWTLogger", e);
        } catch (NoSuchMethodException e) {
          debug("AWTLogger does not have constructor");
        } catch (SecurityException e) {
          debug("Security exception", e);
        } catch (IllegalArgumentException e) {
          debug("illegal argument", e);
        } catch (InvocationTargetException e) {
          debug("Exception in AWTLogger constructor", e);
        }
      }
    }

    if (Logger.awtLogger != null) {
      try {
        Class<?> parameterTypes[] = new Class[] { Object.class, Throwable.class };
        Object arguments[] = new Object[] { aObj, aThrowable };
        Method method = Logger.awtLogger.getClass().getMethod("log", parameterTypes);
        method.invoke(Logger.awtLogger, arguments);
      } catch (NoSuchMethodException e) {
        debug(e);
      } catch (InvocationTargetException e) {
        debug(e);
      } catch (IllegalAccessException e) {
        debug(e);
      }
    }
  }

  /**
   * 
   */
  public boolean isAwtEnabled() {
    return Logger.verboseLevel >= Logger.AWT;
  }

  private static Collection<LogListener> logListeners = new ArrayList<LogListener>();

  /**
   * Adds a listener to the list of notified listeners.
   */
  public static void addLogListener(LogListener l) {
    Logger.logListeners.add(l);
  }

  /**
   * Removes l from the list of notified listeners.
   */
  public static void removeLogListener(LogListener l) {
    Logger.logListeners.remove(l);
  }

  private static class DefaultLogListener extends AbstractLogListener implements LogListener {

    /**
     * Logs to {@link System#err}.
     * 
     * @see magellan.library.utils.logging.LogListener#log(int, java.lang.Object,
     *      java.lang.Throwable)
     */
    public void log(int level, Object aObj, Throwable aThrowable) {
      log(System.err, getMessage(level, aObj, aThrowable));
    }

    private void log(PrintStream aOut, String message) {
      aOut.print(message);
    }
  }

}
