// class magellan.client.preferences.Install4JTest
// created on Apr 19, 2022
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
package magellan.client.preferences;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;

import org.junit.Before;
import org.junit.Test;

public class Install4JTest {

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testEscape() throws IOException {
    assertEquals("\\\\ a\\#b$c\\=d", "\\ a#b$c=d".replaceAll("([#\\\\=])", "\\\\$1"));
    assertEquals("\\\\ a\\#b$c\\=d\\:e\\!f", new Install4J(null).escape("\\ a#b$c=d:e!f"));
  }

  String input = "a\\b#c$d:f=g\u2a69h\nij";

  @Test
  public void testStore() throws Exception {
    File dir = new File("./test/.install4j");
    File response = new File(dir, "response.varfile");
    dir.mkdir();
    response.delete();
    response.createNewFile();
    Install4J i4 = new Install4J(new File("./test"));
    assertEquals(null, i4.setVariable("x=y", input));
    i4.setVariable("a", "aa");
    i4.setVariable("z", "zz");
    i4.store("no comment");
    assertEquals("aa", i4.getVariable("a"));
    assertEquals(input, i4.getVariable("x=y"));

    Install4J i4b = new Install4J(new File("./test"));
    assertEquals("aa", i4b.getVariable("a"));
    assertEquals(input, i4b.getVariable("x=y"));
  }

  @Test
  public void testStoreSorted() throws Exception {
    File[] files = createDir();
    Install4J i4 = new Install4J(new File("./test"));

    ByteArrayOutputStream buf = new ByteArrayOutputStream();
    buf.write("# 123\n".getBytes());
    buf.write("abc\\\\\\\n  def\\\nghi\n".getBytes());
    buf.write("#789\\\\\\\n  xxx\n".getBytes());
    buf.write("xyz\n  fgh\n".getBytes());
    buf.write("#456\n".getBytes());
    buf.write("#678\n".getBytes());
    buf.write("def\n".getBytes());
    buf.write("aaa\\\\\\\n  bbb\\\n ccc\n".getBytes());
    i4.storeSorted(buf);
    LineNumberReader r = new LineNumberReader(new FileReader(files[1]));
    assertEquals("  fgh", r.readLine());
    assertEquals("#789\\\\\\", r.readLine());
    assertEquals("  xxx", r.readLine());
    assertEquals("aaa\\\\bbbccc", r.readLine());
    assertEquals("# 123", r.readLine());
    assertEquals("abc\\\\defghi", r.readLine());
    assertEquals("#456", r.readLine());
    assertEquals("#678", r.readLine());
    assertEquals("def", r.readLine());
    assertEquals("xyz", r.readLine());
    assertEquals(null, r.readLine());
    r.close();
  }

  @Test
  public void testRead() throws Exception {
    File response = createDir()[1];
    FileWriter writer = new FileWriter(response);
    writer.write("\n");
    writer.write("x\\=y=a\\\\b\\#c$d\\:f\\=g\\u2a69h\\ni\\\n  j\n");
    writer.write("#x=y\n");
    writer.write("a=aa\n");
    writer.write("\n");
    writer.close();

    Install4J i4 = new Install4J(new File("./test"));

    assertEquals("aa", i4.getVariable("a"));
    assertEquals(input, i4.getVariable("x=y"));
    assertEquals(null, i4.getVariable("x"));

  }

  private File[] createDir() throws IOException {
    File dir = new File("./test/.install4j");
    File response = new File(dir, "response.varfile");
    dir.mkdir();
    response.delete();
    response.createNewFile();
    return new File[] { dir, response };
  }

}
