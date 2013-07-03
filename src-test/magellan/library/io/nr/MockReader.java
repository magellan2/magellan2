// class magellan.library.io.nr.MockReader
// created on Apr 19, 2013
//
// Copyright 2003-2013 by magellan project team
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
package magellan.library.io.nr;

import java.io.IOException;
import java.io.Reader;

public class MockReader extends Reader {

  private StringBuffer content;
  private int currentPos;
  private int messages;
  private boolean status;

  public MockReader() {
    content = new StringBuffer();
  }

  @Override
  public int read(char[] cbuf, int off, int len) throws IOException {
    if (currentPos >= content.length())
      return -1;
    int i;
    for (i = 0; i < len; ++i) {
      if (currentPos < content.length()) {
        cbuf[off + i] = content.charAt(currentPos++);
      } else {
        break;
      }
    }
    return i;
  }

  @Override
  public void close() throws IOException {
  }

  public void add(String string) {
    content.append(string);
  }

  public void addLine(String string) {
    content.append(string).append("\n");
  }

  protected void addMessage(String message) {
    if (messages == 0) {
      addLine("");
      addLine("                                   Messages");
      addLine("");
    }
    addLine(message);
  }

  public void addHeader(int fnum, String month, int year) {
    addLine("                             Atlantis Turn Report");
    addLine("                                   Faction 1 (" + fnum + ")");
    addLine("                               " + month + ", Year " + year);

  }

  protected void addHeader(int fnum, String date) {
    addLine("                             Atlantis Turn Report");
    addLine("                                   Faction 1 (" + fnum + ")");
    addLine("                               " + date);
  }

  public void addRegion(String name, int x, int y, String terrain, String exits, int peasants,
      int money) {
    if (!status) {
      addLine("");
      addLine("                                Current Status");
    }
    addLine("");
    addLine(String.format("%s (%d,%d), %s, exits: %s. peasants: %d, $%d.", name, x, y, terrain,
        exits, peasants, money));
  }

  public void addUnit(String name, int uid, String faction, int fid, int money, String order) {
    addUnit(name, uid, faction, fid, money, order, 1, null, null);
  }

  public void addUnit(String name, int uid, String faction, int fid, int money, String order,
      int number, Object[] skills, Object[] items) {

    if (faction != null) {
      add(String.format("  * %s (%d), faction %s (%d)", name, uid, faction, fid));
    } else {
      add(String.format("  * %s (%d)", name, uid));
    }
    if (money > 0) {
      add(String.format(", $%d", money));
    }
    if (number > 1) {
      add(", number: " + number);
    }
    if (skills != null) {
      for (int i = 0; i < skills.length;) {
        if (i == 0) {
          add(String.format(", skills: %s %d [%d]", skills[i++], skills[i++], skills[i++]));
        } else {
          add(String.format(", %s %d [%d]", skills[i++], skills[i++], skills[i++]));
        }
      }
    }
    if (items != null) {
      for (int i = 0; i < items.length;) {
        if (i == 0) {
          add(String.format(", has: %d %s", items[i++], items[i++]));
        } else {
          add(String.format(", %d %s", items[i++], items[i++]));
        }
      }
    }
    if (order != null) {
      add(", default: \"" + order + "\"");
    }

    addLine(".");

  }

  public void addShip(String name, int sid, String type, String description) {
    add(String.format("    %s (%d), %s", name, sid, type));
    if (description != null) {
      addLine("; " + description + ".");
    } else {
      addLine(".");
    }
  }

  public void addBuilding(String name, int bid, int size, String description) {
    add(String.format("    %s (%d), size %d", name, bid, size));
    if (description != null) {
      addLine("; " + description + ".");
    } else {
      addLine(".");
    }
  }

}
