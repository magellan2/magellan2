// class magellan.client.preferences.ClientMemory
// created on Aug 21, 2023
//
// Copyright 2003-2023 by magellan project team
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
package magellan.client.preferences;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import magellan.library.utils.logging.Logger;

public class ClientMemory {
  private static Logger log = Logger.getInstance(ClientMemory.class.getName());

  private Properties install4jProps;
  private int xmx = -1;
  private String unit = "M";
  private StringBuilder vmoptions;

  private File vmoptionsFile;

  public ClientMemory(File binaryDirectory, File settingsDirectory) {
    LineNumberReader reader2 = null;
    try {
      install4jProps = new Properties();
      File responseFile = new File(binaryDirectory, ".install4j");
      responseFile = new File(responseFile, "response.varfile");
      FileReader reader;
      install4jProps.load(reader = new FileReader(responseFile));
      reader.close();

      xmx = -1;
      matchXmX(install4jProps.getProperty("xmx"));

      vmoptions = new StringBuilder();
      vmoptionsFile = new File(settingsDirectory.getParentFile(), "my.vmoptions");
      reader2 = new LineNumberReader(new FileReader(vmoptionsFile));

      String line;
      while ((line = reader2.readLine()) != null) {
        matchXmX(line);
        vmoptions.append(line).append("\n");
      }
    } catch (IOException e) {
      Logger.getInstance(this.getClass().getName()).fine("io error", e);
    } finally {
      if (reader2 != null) {
        try {
          reader2.close();
        } catch (IOException e) {
          log.fine("io error", e);
        }
      }
    }
  }

  private void matchXmX(String line) {
    if (line == null)
      return;
    Matcher matcher = Pattern.compile("^ *-Xmx([0-9]+)([A-Za-z]*)").matcher(line);
    if (matcher.find()) {
      xmx = Integer.parseInt(matcher.group(1));
      unit = matcher.group(2).toUpperCase();
      convertToMB();
    }
  }

  private void convertToMB() {
    if (unit.isEmpty()) {
      xmx /= 1024 * 1024;
    } else {
      switch (unit.charAt(0)) {
      case 'K':
        xmx /= 1024;
        break;
      case 'M':
        // keep
        break;
      case 'G':
        xmx *= 1024;
        break;
      }
    }
    unit = "M";
  }

  public int getXmX() {
    return xmx;
  }

  public String getXmXUnit() {
    if (unit.isEmpty())
      return "b";
    switch (unit.charAt(0)) {
    case 'K':
      return "KB";
    case 'M':
      return "MB";
    case 'G':
      return "GB";
    default:
      return "MB";
    }
  }

  public void setXmX(int newXmx, String newXmxUnit) {
    String line = getLine(newXmx, newXmxUnit);
    if (!line.toUpperCase().equals(install4jProps.getProperty("xmx").toUpperCase())) {
      Matcher matcher = Pattern.compile("^ *-Xmx([0-9]+)([A-Za-z]*)", Pattern.MULTILINE).matcher(vmoptions.toString());
      String newOptions;
      if (matcher.find()) {
        matcher.reset();
        newOptions = matcher.replaceFirst(line);
      } else {
        newOptions = vmoptions.toString() + "\n" + line;
      }

      try {
        FileWriter writer = new FileWriter(vmoptionsFile);
        writer.write(newOptions);
        writer.close();
      } catch (IOException e) {
        log.warn("could not write vmoptions file " + vmoptionsFile.getAbsolutePath(), e);
      }
    }
  }

  private String getLine(int xmx, String unit) {
    if ("b".equals(unit)) {
      unit = "";
    } else if ("KB".equals(unit)) {
      unit = "K";
    } else if ("MB".equals(unit)) {
      unit = "M";
    } else if ("GB".equals(unit)) {
      unit = "G";
    } else {
      log.warn("invalid memory unit '" + unit + "'");
    }

    return "-Xmx" + xmx + unit;
  }
}
