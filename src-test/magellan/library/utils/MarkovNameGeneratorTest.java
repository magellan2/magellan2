// class magellan.library.utils.MarkovNameGeneratorTest
// created on Jul 19, 2022
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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.stream.IntStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import magellan.library.utils.MarkovNameGenerator.CMarkov;
import magellan.library.utils.MarkovNameGenerator.MarkovGenerator;
import magellan.library.utils.MarkovNameGenerator.Profiler;

public class MarkovNameGeneratorTest {

  private Random rng;
  private String[] incomplete = new String[] { "a", "aa", "ba", "aaa" };
  private String[] completed = new String[] { "a", "aa", "ba", "aaa", "baa" };

  @Before
  public void setUp() throws Exception {
    rng = new Random();
    new File("test/names.txt").delete();
  }

  @After
  public void tearDown() {
    new File("test/names.txt").delete();
  }

  @Test
  public void testGenerate() {
    String[] words = getWords(10, 3);
    CMarkov markov = MarkovGenerator.create(words, 1);
    String word = markov.generate();
    assertTrue(word + " length < 3", word.length() >= 3);
  }

  @Test
  public void testWords() {
    for (int l = 0; l < 10; ++l) {
      String[] w = getWords(10, l);
      for (String word : w) {
        assertTrue(word + " < " + l, word.length() >= l);
      }
    }
  }

  private String[] getWords(int size, int length) {
    Set<String> words = new HashSet<>(size);
    int l2 = (int) (Math.log(size) + 1);
    while (words.size() < size) {
      words.add(getRandomWord(Math.max(l2, length)));
    }
    return words.toArray(new String[0]);
  }

  private String getRandomWord(int length) {
    int l = length + rng.nextInt(length);
    char[] cs = new char[l];
    for (int i = 0; i < l; ++i) {
      cs[i] = (char) ('a' + rng.nextInt(20));
    }
    return new String(cs);
  }

  @Test
  public void testGetNamesCount() throws IOException {
    String[] w = new String[] { "a" };
    writeWords(w, new File("test/names.txt"));

    MarkovNameGenerator namegen = new MarkovNameGenerator(new Properties(), new File("test"));
    assertEquals(1, namegen.getNamesCount());
    namegen.load("test/names.txt");
    assertEquals(1, namegen.getNamesCount());
    assertFalse(new File("test/nonames.txt").exists());
    namegen.load("test/nonames.txt");
    assertEquals(0, namegen.getNamesCount());

    w = new String[] { "a", "b" };
    writeWords(w, new File("test/names.txt"));
    namegen.load("test/names.txt");
    assertEquals(2, namegen.getNamesCount());

    w = incomplete;
    String[] w2 = completed;
    writeWords(w, new File("test/names.txt"));
    namegen.load("test/names.txt");
    int count = namegen.getNamesCount();
    String[] newords = IntStream.iterate(0, x -> x + 1).limit(count)
        .mapToObj(x -> namegen.getName())
        .toArray(n -> new String[n]);
    assertEquals(Arrays.toString(newords) + " too small", 5, count);
    assertArrayEquals(w2, newords);
  }

  @Test
  public void testManyNames() throws IOException {
    final int LOWER_LIMIT = 1000;
    String[] w = getWords(LOWER_LIMIT, 6);
    writeWords(w, new File("test/names.txt"));

    MarkovNameGenerator namegen = new MarkovNameGenerator(new Properties(), new File("test"));
    assertEquals(LOWER_LIMIT, namegen.getLowerLimit());
    assertEquals(namegen.getLowerLimit(), namegen.getNamesCount());

    for (String element : w) {
      assertEquals(element, namegen.getName());
    }

    w = getWords(LOWER_LIMIT / 2, 7);
    writeWords(w, new File("test/names.txt"));

    namegen.load("test/names.txt");
    assertEquals(namegen.getLowerLimit(), namegen.getNamesCount());

    for (String element : w) {
      assertEquals(element, namegen.getName());
    }
    String word = namegen.getName();
    assertNotNull(word);
    assertFalse(Arrays.asList(w).contains(word));
  }

  private long startTime;

  private void resetTime() {
    startTime = System.currentTimeMillis();
  }

  private void printTime() {
    System.err.println("Time " + (System.currentTimeMillis() - startTime));
  }

  private static enum PTags {
    INIT, CREATE, NEW, GENERATE, NULL, GEN1
  }

  @Test
  public void testProfile() throws IOException {
    final int LOWER_LIMIT = 1000;
    long x = System.currentTimeMillis();
    Profiler p = new MarkovNameGenerator.Profiler();
    p.register(PTags.values());
    String[] w = getWords(LOWER_LIMIT, 6);
    writeWords(w, new File("test/names.txt"));
    p.log(PTags.INIT);
    for (int i = 0; i < 10; ++i) {
      CMarkov markov = MarkovGenerator.create(w, 1);
    }
    p.log(PTags.CREATE);
    MarkovNameGenerator namegen = new MarkovNameGenerator(new Properties(), new File("test"));
    p.log(PTags.NEW);
    MarkovNameGenerator.resetProfilers();
    for (int i = 0; i < 10000; ++i) {
      String s = namegen.getName();
      if (s == null) {
        p.log(PTags.NULL);
      } else {
        p.log(PTags.GEN1);
      }
    }
    p.log(PTags.GENERATE);
    p.printTags();
    System.err.println(String.format("total %10.6f", (double) (System.currentTimeMillis() - x)));
  }

  @Test
  public void testTable() {
    for (char i = 0; i < 255; ++i) {
      System.out.print(String.format("%3d %s  ", (int) i, String.valueOf(i)));
      if (i % 16 == 0) {
        System.out.println();
      }
    }
  }

  private void writeWords(String[] w, File file) throws IOException {
    try (PrintWriter out = new PrintWriter(new FileWriter(file))) {
      for (String name : w) {
        out.println(name);
      }
    } catch (IOException e) {
      throw e;
    }
  }

  private void readWords(File file, List<String> words, Map<String, String> vars) throws IOException {
    words.clear();
    vars.clear();
    try (BufferedReader in = new BufferedReader(new FileReader(file))) {
      String name = null;

      while ((name = in.readLine()) != null) {
        name = readLine(name, vars);
        if (name != null && name.length() > 0) {
          words.add(name);
        }
      }
    }
  }

  protected String readLine(String name, Map<String, String> vars) {
    name = name.trim();
    if (name.startsWith("#")) {
      readVar(name, vars);
      name = "";
    }
    if (!name.isEmpty())
      return name.trim();
    return null;
  }

  protected void readVar(String line, Map<String, String> vars) {
    line = line.substring(1);
    int split = line.indexOf("=");
    if (split >= 1) {
      String name = line.substring(0, split).trim();
      String value = line.substring(split + 1).trim();
      vars.put(name, value);
    }
  }

  @Test
  public void testWrite() throws IOException {
    String[] w = new String[] { "a", "b" };
    writeWords(w, new File("test/names.txt"));
    MarkovNameGenerator namegen = new MarkovNameGenerator(new Properties(), new File("test"));
    assertEquals(2, namegen.getNamesCount());
    namegen.close();
    List<String> words = new ArrayList<String>();
    Map<String, String> vars = new HashMap<String, String>();
    readWords(new File("test/names.txt"), words, vars);
    assertEquals(2, words.size());
    assertArrayEquals(words.toArray(), w);
    assertEquals("0", vars.get(AbstractNameGenerator.NAMEGEN + AbstractNameGenerator.USED));

    namegen.getName();
    namegen.close();
    readWords(new File("test/names.txt"), words, vars);
    assertEquals(2, words.size());
    assertArrayEquals(words.toArray(), w);
    assertEquals("1", vars.get(AbstractNameGenerator.NAMEGEN + AbstractNameGenerator.USED));
  }

  @Test
  public void testWriteGenerated() throws IOException {
    writeWords(incomplete, new File("test/names.txt"));
    MarkovNameGenerator namegen = new MarkovNameGenerator(new Properties(), new File("test"));
    assertEquals(5, namegen.getNamesCount());
    assertEquals(4, namegen.getInteger(MarkovNameGenerator.ORIGINAL));

    namegen.close();
    List<String> words = new ArrayList<String>();
    Map<String, String> vars = new HashMap<String, String>();
    readWords(new File("test/names.txt"), words, vars);
    assertEquals(5, words.size());
    assertArrayEquals(completed, words.toArray());
    assertEquals("0", vars.get(AbstractNameGenerator.NAMEGEN + AbstractNameGenerator.USED));
    assertEquals("" + incomplete.length, vars.get(AbstractNameGenerator.NAMEGEN + MarkovNameGenerator.ORIGINAL));

    namegen = new MarkovNameGenerator(new Properties(), new File("test"));
    assertEquals(5, namegen.getNamesCount());
    assertEquals(4, namegen.getInteger(MarkovNameGenerator.ORIGINAL));

  }
}
