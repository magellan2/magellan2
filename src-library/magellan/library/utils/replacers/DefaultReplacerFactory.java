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

/*
 * DefaultReplacerFactory.java
 *
 * Created on 20. Mai 2002, 15:49
 */
package magellan.library.utils.replacers;

import java.util.HashMap;
import java.util.Map;

import magellan.library.utils.logging.Logger;

/**
 * DOCUMENT ME!
 * 
 * @author Andreas
 * @version 1.0
 */
public class DefaultReplacerFactory implements ReplacerFactory {
  private static final Logger log = Logger.getInstance(DefaultReplacerFactory.class);
  protected Map<String, ReplacerInfo> replacers;

  /**
   * Creates new DefaultReplacerFactory
   */
  public DefaultReplacerFactory() {
    replacers = new HashMap<String, ReplacerInfo>();
  }

  /**
   * Registers a replacer for the given name and arguments.
   */
  public void putReplacer(String name, Class<?> repClass, Object args[]) {
    putReplacer(name, repClass);

    if (args != null) {
      setArguments(name, args);
    }
  }

  /**
   * Registers a replacer for the given name and arguments.
   */
  public void putReplacer(String name, Class<?> repClass, Object arg) {
    putReplacer(name, repClass);

    if (arg != null) {
      setArguments(name, arg);
    }
  }

  /**
   * Registers a replacer for the given name without arguments.
   */
  public void putReplacer(String name, Class<?> repClass) {
    replacers.put(name, new ReplacerInfo(repClass));
  }

  /**
   * Sets arguments for the given replacer.
   */
  public void setArguments(String name, Object arg) {
    Object args[] = new Object[1];
    args[0] = arg;
    setArguments(name, args);
  }

  /**
   * Sets arguments for the given replacer.
   */
  public void setArguments(String name, Object args[]) {
    Object argCopy[] = new Object[args.length];
    System.arraycopy(args, 0, argCopy, 0, argCopy.length);
    (replacers.get(name)).setArgs(argCopy);
  }

  /**
   * Looks up and instanciates a replacer.
   * 
   * @see magellan.library.utils.replacers.ReplacerFactory#createReplacer(java.lang.String)
   */
  public Replacer createReplacer(String name) {
    ReplacerInfo repInfo = replacers.get(name);

    try {
      if (repInfo.args == null)
        return (Replacer) repInfo.replacerClass.newInstance();

      return (Replacer) repInfo.replacerClass.getConstructor(repInfo.argClasses).newInstance(
          repInfo.args);
    } catch (Exception exc) {
      DefaultReplacerFactory.log.warn(exc);
    }

    return null;
  }

  /**
   * @see magellan.library.utils.replacers.ReplacerFactory#isReplacer(java.lang.String)
   */
  public boolean isReplacer(String name) {
    return replacers.containsKey(name);
  }

  /**
   * @see magellan.library.utils.replacers.ReplacerFactory#getReplacers()
   */
  public java.util.Set<String> getReplacers() {
    return replacers.keySet();
  }

  protected static class ReplacerInfo {
    Class<?> replacerClass;
    Object args[] = null;
    Class<?> argClasses[] = null;

    /**
     * Creates a new ReplacerInfo object.
     */
    public ReplacerInfo(Class<?> repClass) {
      replacerClass = repClass;
    }

    /**
     * Sets the constructor arguments.
     */
    public void setArgs(Object arg[]) {
      args = arg;
      argClasses = new Class[args.length];

      for (int i = 0; i < argClasses.length; i++) {
        if (args[i] instanceof Integer) {
          argClasses[i] = int.class;
        } else if (args[i] instanceof Boolean) {
          argClasses[i] = boolean.class;
        } else {
          argClasses[i] = args[i].getClass();
        }
      }
    }
  }
}
