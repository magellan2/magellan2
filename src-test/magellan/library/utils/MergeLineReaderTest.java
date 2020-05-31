// class magellan.library.utils.MergeLineReaderTest
// created on Jan 30, 2020
//
// Copyright 2003-2020 by magellan project team
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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;

public class MergeLineReaderTest {

  @Test
  public void testRead() throws IOException {
    MergeLineReader reader = getReader("");
    assertEquals(-1, reader.read());

    assertEquals('a', getReader("a").read());
    assertEquals(-1, reader.read());

    reader = getReader("a\nb");
    assertEquals('a', reader.read());
    assertEquals('\n', reader.read());
    assertEquals('b', reader.read());
    assertEquals(-1, reader.read());
  }

  @Test
  public void testMerge() throws IOException {
    MergeLineReader reader = getReader("a\\\nb");
    assertEquals('a', reader.read());
    assertEquals('b', reader.read());
    assertEquals(-1, reader.read());

    reader = getReader("a\\b");
    assertEquals('a', reader.read());
    assertEquals('\\', reader.read());
    assertEquals('b', reader.read());
    assertEquals(-1, reader.read());

    reader = getReader("a \\\n b");
    assertEquals('a', reader.read());
    assertEquals(' ', reader.read());
    assertEquals(' ', reader.read());
    assertEquals('b', reader.read());
    assertEquals(-1, reader.read());
  }

  private MergeLineReader getReader(String string) {
    return new MergeLineReader(new StringReader(string));
  }

}
