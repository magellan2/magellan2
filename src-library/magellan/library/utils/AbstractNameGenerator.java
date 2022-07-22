// class magellan.library.utils.AbstractNameGenerator
// created on Jul 20, 2022
//
// Copyright 2003-2022 by magellan project team
//
// Author : $Author: $
// $Id: $
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc.,
// 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
package magellan.library.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import magellan.library.utils.logging.Logger;

public abstract class AbstractNameGenerator implements NameGenerator {
  protected static Logger log = Logger.getInstance(AbstractNameGenerator.class);

  protected static final String USED = "used";
  protected static final String NAMEGEN = "NameGenerator.";

  protected boolean available = false;
  protected String[] names;

  protected Properties settings;
  protected File settingsDir;

  private Map<String, String> vars;

  public AbstractNameGenerator(Properties settings, File settingsDir) {
    super();
    this.settings = settings;
    this.settingsDir = settingsDir;
    available = PropertiesHelper.getBoolean(settings, "NameGenerator.active", true);
    vars = new HashMap<>();
    if (getNameFile() != null) {
      load(getNameFile().getAbsolutePath());
    }
  }

  /**
   * @see magellan.library.utils.NameGenerator#getName()
   */
  @Override
  public String getName() {
    int currentName = getInteger(USED);
    if (names != null && currentName < names.length) {
      String name = names[currentName++];
      setInteger(USED, currentName);
      return name;
    }
    return null;
  }

  /**
   * Returns the file where the remaining names are stored locally, <code>null</code> if undefined.
   */
  public String getCache() {
    File file = getNameFile();
    if (file != null)
      return file.getAbsolutePath();
    return null;
  }

  /**
   * Loads names from a file.
   * 
   * @param fileName If this is a file with at least one name, names are replaced with the contents. Otherwise, names
   *          are cleared.
   */
  public void load(String fileName) {
    if (vars != null) {
      initVars();
    }
    List<String> nameList = new LinkedList<>();
    if (!Utils.isEmpty(fileName)) {
      File file = new File(fileName);

      // we read the file only if it exists.
      if (file.exists() && file.canRead()) {
        try {
          BufferedReader in = new BufferedReader(new FileReader(file));
          String name = null;

          while ((name = in.readLine()) != null) {
            name = readLine(name);
            if (name != null) {
              nameList.add(name);
            }
          }

          in.close();
        } catch (IOException ioe) {
          log.warn(ioe);
        }
      }
    }
    if (nameList.isEmpty()) {
      names = null;
    } else {
      names = nameList.toArray(new String[0]);
    }
  }

  protected String readLine(String name) {
    name = name.trim();
    if (name.startsWith("#")) {
      // lines starting with # are comments,
      // unless they start with two ##, in which case the first # is deleted
      if (name.startsWith("##")) {
        name = name.substring(1);
      } else {
        readComment(name);
        name = "";
      }
    }
    if (!name.isEmpty())
      return name.trim();
    return null;
  }

  protected void readComment(String line) {
    readVariable(line);
  }

  protected void readVariable(String line) {
    line = line.substring(1);
    int split = line.indexOf("=");
    if (split >= 1) {
      String name = line.substring(0, split).trim();
      String value = line.substring(split + 1).trim();
      if (name.startsWith(NAMEGEN) && !value.isEmpty()) {
        vars.put(name.substring(NAMEGEN.length()), value);
      }
    }
  }

  protected void writeVariable(PrintWriter out, String name, String string) {
    out.println("# " + NAMEGEN + name + "=" + vars.get(name));
  }

  protected void initVars() {
    vars.clear();
    vars.put(USED, "0");
  }

  protected void clearVariables() {
    vars.clear();
  }

  protected void setVariable(String name, String val) {
    vars.put(name, val);
  }

  protected String getVariable(String name) {
    return vars.get(name);
  }

  protected void setInteger(String name, int val) {
    vars.put(name, String.valueOf(val));
  }

  protected int getInteger(String name) {
    return Integer.parseInt(vars.get(name));
  }

  /**
   * Writes the remaining names to {@link #getNameFile()}.
   */
  protected void close() {
    File file = getNameFile();

    if (file != null) {
      try {
        if (names != null) {
          PrintWriter out = new PrintWriter(new FileWriter(file));
          for (String name : vars.keySet()) {
            writeVariable(out, name, vars.get(name));
          }
          for (String name : names) {
            out.println(name);
          }
          out.close();
        } else {
          file.delete();
        }
      } catch (IOException exc) {
        Logger.getInstance(getClass()).warn(exc);
      }
    }
  }

  /**
   * Returns the file to hold the local copy of the remaining names or <code>null</code> if this is not defined.
   */
  protected File getNameFile() {
    if (settingsDir != null)
      return new File(settingsDir, "names.txt");
    return null;
  }

  /**
   * @see magellan.library.utils.NameGenerator#setEnabled(boolean)
   */
  @Override
  public void setEnabled(boolean available) {
    this.available = available;
  }

  /**
   * @see magellan.library.utils.NameGenerator#isActive()
   */
  public boolean isActive() {
    return available;
  }

  /**
   * @see magellan.library.utils.NameGenerator#isAvailable()
   */
  @Override
  public boolean isAvailable() {
    return (available && (getNamesCount() > 0));
  }

  /**
   * @see magellan.library.utils.NameGenerator#getNamesCount()
   */
  @Override
  public int getNamesCount() {
    if (names == null)
      return 0;
    return names.length - getInteger(USED);
  }

  public void quit() {
    close();
  }

}