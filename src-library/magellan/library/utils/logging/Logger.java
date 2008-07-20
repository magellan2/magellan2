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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import magellan.library.utils.Utils;


//import org.apache.log4j.*;

/*
 * @author Ilja Pavkovic
 */
public class Logger {
	//	private Category ivTraceLog;

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

	/** Debug messages are printed for debugging purposes */
	public static final int DEBUG = 5;

	/** AWT messages are printed for debugging awt purposes */
	public static final int AWT = 6;

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
		if(aBase == null) {
			throw new NullPointerException();
		}
	}

	/**
	 * 
	 */
	public static Logger getInstance(Class aClass) {
		// be fail-fast
		if(aClass == null) {
			throw new NullPointerException();
		}

		return Logger.getInstance(aClass.getName());
	}

	/**
	 * 
	 */
	public static Logger getInstance(String aBase) {
		// be fail-fast
		if(aBase == null) {
			throw new NullPointerException();
		}

		// pavkovic 2004.02.04: we dont take any advantage 
		// of different loggers, so reduce memory footprint
		// of Magellan
		// return new Logger(aBase);
		return Logger.DEFAULT;
	}

	/**
   * Set the logging level. Currently supported levels are {@link Logger#FATAL},
   * {@link Logger#ERROR}, {@link Logger#WARN}, {@link Logger#INFO},
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

		if(level.startsWith("O")) {
			Logger.setLevel(Logger.OFF);
		}

		if(level.startsWith("F")) {
			Logger.setLevel(Logger.FATAL);
		}

		if(level.startsWith("E")) {
			Logger.setLevel(Logger.ERROR);
		}

		if(level.startsWith("W")) {
			Logger.setLevel(Logger.WARN);
		}

		if(level.startsWith("I")) {
			Logger.setLevel(Logger.INFO);
		}

		if(level.startsWith("D")) {
			Logger.setLevel(Logger.DEBUG);
		}

		if(level.startsWith("A")) {
			Logger.setLevel(Logger.AWT);
		}
	}

	/**
	 * @return The current verbosity level
	 */
	public static int getLevel(){
	  return Logger.verboseLevel;
	}
	
	/**
	 * 
	 * @return The string representation of <code>level</code> 
	 */
	public static String getLevel(int level) {
		if(level <= Logger.OFF) {
			return "OFF";
		}

		switch(level) {
		case FATAL:
			return "FATAL";

		case ERROR:
			return "ERROR";

		case WARN:
			return "WARN";

		case INFO:
			return "INFO";

		case DEBUG:
			return "DEBUG";

		default:
			return "ALL";
		}
	}

	/**
	 * 
	 */
	private void log(int aLevel, Object aObj, Throwable aThrowable) {
		if(Logger.verboseLevel >= aLevel) {
			if(Logger.logListeners.isEmpty()) {
				Logger.DEFAULTLOGLISTENER.log(aLevel, aObj, aThrowable);
			} else {
				for(LogListener l : Logger.logListeners ) {
					l.log(aLevel, aObj, aThrowable);
				}
				if (Logger.activateDefaultLogListener) {
				  Logger.DEFAULTLOGLISTENER.log(aLevel, aObj, aThrowable);
				}
			}
		}
	}
	
	public static void activateDefaultLogListener(boolean activate) {
	  Logger.activateDefaultLogListener = activate;
	}

	/**
	 * 
	 */
	public void fatal(Object aObj) {
		fatal(aObj, null);
	}

	/**
	 * 
	 */
	public void fatal(Object aObj, Throwable aThrowable) {
		log(Logger.FATAL, aObj, aThrowable);
	}

	/**
	 * 
	 */
	public boolean isFatalEnabled() {
		return Logger.verboseLevel >= Logger.FATAL;
	}

	/**
	 * 
	 */
	public void error(Object aObj) {
		error(aObj, null);
	}
  
  /**
   * 
   */
  public void errorOnce(Object aObj) {
    if (Logger.onceErrors == null){
      // create new list
      Logger.onceErrors = new HashSet<Object>();
    }
    if (Logger.onceErrors.contains(aObj)){
      // already processed error
      return;
    }
    // add to errors - list
    Logger.onceErrors.add(aObj);
    // normal call to Logger.error
    error(aObj);
  }
  

	/**
	 * 
	 */
	public void error(Object aObj, Throwable aThrowable) {
		log(Logger.ERROR, aObj, aThrowable);
	}

	/**
	 * 
	 */
	public boolean isErrorEnabled() {
		return Logger.verboseLevel >= Logger.ERROR;
	}

	/**
	 * 
	 */
	public void warn(Object aObj) {
		warn(aObj, null);
	}
  
  /**
   * processed all warnings only once
   * 
   */
  public void warnOnce(Object aObj) {
    if (Logger.onceWarnings == null){
      // create new list
      Logger.onceWarnings = new HashSet<Object>();
    }
    if (Logger.onceWarnings.contains(aObj)){
      // already processed warning
      return;
    }
    // add to warnings - list
    Logger.onceWarnings.add(aObj);
    // normal call to Logger.warn
    warn(aObj);
  }
  

	/**
	 * 
	 */
	public void warn(Object aObj, Throwable aThrowable) {
		log(Logger.WARN, aObj, aThrowable);
	}

	/**
	 * 
	 */
	public boolean isWarnEnabled() {
		return Logger.verboseLevel >= Logger.WARN;
	}

	/**
	 * 
	 */
	public void info(Object aObj) {
		info(aObj, null);
	}

	/**
	 * 
	 */
	public void info(Object aObj, Throwable aThrowable) {
		log(Logger.INFO, aObj, aThrowable);
	}

	/**
	 * 
	 */
	public boolean isInfoEnabled() {
		return Logger.verboseLevel >= Logger.INFO;
	}

	/**
	 * 
	 */
	public void debug(Object aObj) {
		debug(aObj, null);
	}

	/**
	 * 
	 */
	public void debug(Object aObj, Throwable aThrowable) {
		log(Logger.DEBUG, aObj, aThrowable);
	}

	/**
	 * 
	 */
	public boolean isDebugEnabled() {
		return Logger.verboseLevel >= Logger.DEBUG;
	}

	/**
	 * 
	 */
	public void awt(Object aObj) {
		awt(aObj, null);
	}

	/**
	 * 
	 */
	public void awt(Object aObj, Throwable aThrowable) {
		log(Logger.AWT, aObj, aThrowable);

		if(isAwtEnabled()) {
			if(Logger.searchAwtLogger) {
				Logger.searchAwtLogger = false;

				try {
					Logger.awtLogger = Class.forName("magellan.library.utils.logging.AWTLogger").newInstance();
				} catch(ClassNotFoundException e) {
					debug("AWTLogger not found", e);
				} catch(InstantiationException e) {
					debug("Cannot instanciate AWTLogger", e);
				} catch(IllegalAccessException e) {
					debug("Cannot access AWTLogger", e);
				}
			}
		}

		if(Logger.awtLogger != null) {
			try {
				Class parameterTypes[] = new Class[] { Object.class, Throwable.class };
				Object arguments[] = new Object[] { aObj, aThrowable };
				Method method = Logger.awtLogger.getClass().getMethod("log", parameterTypes);
				method.invoke(Logger.awtLogger, arguments);
			} catch(NoSuchMethodException e) {
				debug(e);
			} catch(InvocationTargetException e) {
				debug(e);
			} catch(IllegalAccessException e) {
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

	public static void addLogListener(LogListener l) {
		Logger.logListeners.add(l);
	}

	public static void removeLogListener(LogListener l) {
		Logger.logListeners.remove(l);
	}

	
	private static class DefaultLogListener implements LogListener {
	  private Calendar calendar = Calendar.getInstance();
	  
		public void log(int aLevel, Object aObj, Throwable aThrowable) {
			log(System.err, aLevel, aObj, aThrowable);
		} 
		
		private void log(PrintStream aOut, int aLevel, Object aObj, Throwable aThrowable) {
		  
		  ByteArrayOutputStream baos = new ByteArrayOutputStream();
		  PrintStream ps = new PrintStream(baos);
		  
		  calendar.setTimeInMillis(System.currentTimeMillis());

		  String prefix = "(--)";
		  switch (aLevel) {
      case FATAL:
        prefix = "(FF)";
        break;
      case ERROR:
        prefix = "(EE)";
        break;
      case WARN:
        prefix = "(WW)";
        break;
      case INFO:
        prefix = "(II)";
        break;
      case DEBUG:
        prefix = "(DD)";
        break;
      case AWT: 
        prefix = "(AA)";
        break;
      default:
        prefix = "(--)";
        break;
      }
		  ps.print(prefix+" ");
		  ps.print(Utils.toDayAndTime(calendar.getTime()));
		  ps.print(": ");
		  
			if(aObj != null) {
				if(aObj instanceof Throwable) {
					((Throwable) aObj).printStackTrace(ps);
				} else {
					ps.println(aObj);
				}
			}
			
			if(aThrowable != null) {
				aThrowable.printStackTrace(ps);
			} else {
				if((aObj != null) && !(aObj instanceof Throwable) && aObj.toString().endsWith("Error")) {
					new Exception("SELF GENERATED STACK TRACE").printStackTrace(ps);
				}
			}
			
			ps.close();
			
			aOut.print(baos.toString());
		}
	}
}
