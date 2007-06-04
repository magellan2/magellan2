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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;


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
	private static int verboseLevel = INFO;
	private static Object awtLogger = null;
	private static boolean searchAwtLogger = true;

	private Logger(String aBase) {
		// be fail-fast
		if(aBase == null) {
			throw new NullPointerException();
		}

		//ivTraceLog = Category.getInstance(aBase);
	}

	private static Logger DEFAULT = new Logger("");

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 *
	 * @throws NullPointerException DOCUMENT-ME
	 */
	public static Logger getInstance(Class aClass) {
		// be fail-fast
		if(aClass == null) {
			throw new NullPointerException();
		}

		return getInstance(aClass.getName());
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 *
	 * @throws NullPointerException DOCUMENT-ME
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
		return DEFAULT;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public static void setLevel(int level) {
		verboseLevel = level;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public static void setLevel(String aLevel) {
		String level = aLevel.toUpperCase();

		if(level.startsWith("O")) {
			setLevel(OFF);
		}

		if(level.startsWith("F")) {
			setLevel(FATAL);
		}

		if(level.startsWith("E")) {
			setLevel(ERROR);
		}

		if(level.startsWith("W")) {
			setLevel(WARN);
		}

		if(level.startsWith("I")) {
			setLevel(INFO);
		}

		if(level.startsWith("D")) {
			setLevel(DEBUG);
		}

		if(level.startsWith("A")) {
			setLevel(AWT);
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public static String getLevel(int level) {
		if(level <= OFF) {
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

	private void log(int aLevel, Object aObj, Throwable aThrowable) {
		if(verboseLevel >= aLevel) {
			if(logListeners.isEmpty()) {
				DEFAULTLOGLISTENER.log(aLevel, aObj, aThrowable);
			} else {
				for(Iterator iter = logListeners.iterator(); iter.hasNext(); ) {
					LogListener l = (LogListener) iter.next();
					l.log(aLevel, aObj, aThrowable);
				}
			}
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void fatal(Object aObj) {
		fatal(aObj, null);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 */
	public void fatal(Object aObj, Throwable aThrowable) {
		log(FATAL, aObj, aThrowable);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public boolean isFatalEnabled() {
		return verboseLevel >= FATAL;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void error(Object aObj) {
		error(aObj, null);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 */
	public void error(Object aObj, Throwable aThrowable) {
		log(ERROR, aObj, aThrowable);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public boolean isErrorEnabled() {
		return verboseLevel >= ERROR;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void warn(Object aObj) {
		warn(aObj, null);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 */
	public void warn(Object aObj, Throwable aThrowable) {
		log(WARN, aObj, aThrowable);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public boolean isWarnEnabled() {
		return verboseLevel >= WARN;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void info(Object aObj) {
		info(aObj, null);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 */
	public void info(Object aObj, Throwable aThrowable) {
		log(INFO, aObj, aThrowable);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public boolean isInfoEnabled() {
		return verboseLevel >= INFO;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void debug(Object aObj) {
		debug(aObj, null);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 */
	public void debug(Object aObj, Throwable aThrowable) {
		log(DEBUG, aObj, aThrowable);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public boolean isDebugEnabled() {
		return verboseLevel >= DEBUG;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void awt(Object aObj) {
		awt(aObj, null);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 */
	public void awt(Object aObj, Throwable aThrowable) {
		log(AWT, aObj, aThrowable);

		if(isAwtEnabled()) {
			if(searchAwtLogger) {
				searchAwtLogger = false;

				try {
					awtLogger = Class.forName("magellan.library.utils.logging.AWTLogger").newInstance();
				} catch(ClassNotFoundException e) {
					debug("AWTLogger not found", e);
				} catch(InstantiationException e) {
					debug("Cannot instanciate AWTLogger", e);
				} catch(IllegalAccessException e) {
					debug("Cannot access AWTLogger", e);
				}
			}
		}

		if(awtLogger != null) {
			try {
				Class parameterTypes[] = new Class[] { Object.class, Throwable.class };
				Object arguments[] = new Object[] { aObj, aThrowable };
				Method method = awtLogger.getClass().getMethod("log", parameterTypes);
				method.invoke(awtLogger, arguments);
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
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public boolean isAwtEnabled() {
		return verboseLevel >= AWT;
	}
	
	private static Collection<LogListener> logListeners = new ArrayList<LogListener>();

	public static void addLogListener(LogListener l) {
		logListeners.add(l);
	}

	public static void removeLogListener(LogListener l) {
		logListeners.remove(l);
	}

	private static LogListener DEFAULTLOGLISTENER = new DefaultLogListener();

	private static class DefaultLogListener implements LogListener {
		public void log(int aLevel, Object aObj, Throwable aThrowable) {
			log(System.err, aLevel, aObj, aThrowable);
		} 
		
		private void log(PrintStream aOut, int aLevel, Object aObj, Throwable aThrowable) {
			if(aObj != null) {
				if(aObj instanceof Throwable) {
					((Throwable) aObj).printStackTrace(aOut);
				} else {
					aOut.println(aObj);
				}
			}
			
			if(aThrowable != null) {
				aThrowable.printStackTrace(aOut);
			} else {
				if((aObj != null) && !(aObj instanceof Throwable) &&
				   aObj.toString().endsWith("Error")) {
					new Exception("SELF GENERATED STACK TRACE").printStackTrace(aOut);
				}
			}
		}
	}
}
