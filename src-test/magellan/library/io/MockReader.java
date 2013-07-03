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
package magellan.library.io;

import java.io.IOException;
import java.io.Reader;

public class MockReader extends Reader {

  private StringBuffer content;
  private int currentPos;

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
    // no action necessary
  }

  public MockReader add(String string) {
    content.append(string);
    return this;
  }

  public MockReader addLine(String string) {
    content.append(string).append("\n");
    return this;
  }

  public String getContent() {
    return content.toString();
  }

}
