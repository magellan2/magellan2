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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import magellan.library.utils.MarkovNameGenerator.IMarkov;
import magellan.library.utils.MarkovNameGenerator.Markov;
import magellan.library.utils.MarkovNameGenerator.MarkovGenerator;
import magellan.library.utils.MarkovNameGenerator.SimpleFactory;
import magellan.library.utils.MarkovNameGenerator.State;
import magellan.library.utils.MarkovNameGenerator.StateFactory;

@SuppressWarnings("javadoc")
public class MarkovNameGeneratorTest {

  private static Random rng;
  private String[] incomplete = new String[] { "a", "aa", "ba", "aaa" };
  private String[] completed = new String[] { "a", "aa", "ba", "aaa", "baa" };

  @Before
  public void setUp() {
    rng = new Random();
    new File("test/names.txt").delete();
  }

  @After
  public void tearDown() {
    new File("test/names.txt").delete();
  }

  @SuppressWarnings("unused")
  @Test
  public void testEmpty() {
    try {
      new Markov<Integer>(new StateFactory<Integer>() {
        public State<Integer> initial() {
          return null;
        }

        public Integer terminator() {
          return null;
        }
      });
      fail();
    } catch (IllegalArgumentException e) {
      // pass
    }
  }

  @Test
  public void testGenerate() {
    String[] words = getWords(10, 3, aToZ);
    Markov<Character> markov = MarkovGenerator.createK(words, 1);
    String word = IMarkov.listToString(markov.generate());
    assertTrue(word + " length < 3", word.length() >= 3);
  }

  @Test
  public void testWords() {
    for (int l = 0; l < 10; ++l) {
      String[] w = getWords(10, l, aToZ);
      for (String word : w) {
        assertTrue(word + " < " + l, word.length() >= l);
      }
    }
  }

  private String[] getWords(int size, int length, char[] alphabet) {
    Set<String> words = new HashSet<>(size);
    int l2 = (int) (Math.log(size) + 1);
    while (words.size() < size) {
      words.add(getRandomWord(Math.max(l2, length), alphabet));
    }
    return words.toArray(new String[0]);
  }

  private String getRandomWord(int length, char[] alphabet) {
    int l = length + (rng.nextInt(length) + rng.nextInt(length)) / 2;
    char[] cs = new char[l];
    for (int i = 0; i < l; ++i) {
      cs[i] = alphabet[rng.nextInt(alphabet.length)];
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
  public void testIncrease() {
    StateFactory<Integer> f = new MarkovNameGenerator.StateFactory<Integer>() {

      public State<Integer> initial() {
        return new State<Integer>() {

          public void update(Integer c) {
            //
          }

          public State<Integer> getImmutable() {
            return this;
          }
        };
      }

      public Integer terminator() {
        return Integer.MAX_VALUE;
      }

    };
    int[] force = new int[1];
    Markov<Integer> m = new MarkovNameGenerator.Markov<Integer>(f) {
      @Override
      public int nextInt(int bound) {
        return force[0];
      }

    };
    State<Integer> state = f.initial();
    m.increase(state, 1);
    m.increase(state, 1);
    m.increase(state, 42);
    m.increase(state, f.terminator());
    force[0] = 0;
    assertEquals(f.terminator(), m.step(state));
    force[0] = 1;
    assertEquals((Integer) 1, m.step(state));
    force[0] = 2;
    assertEquals((Integer) 1, m.step(state));
    force[0] = 3;
    assertEquals((Integer) 42, m.step(state));
  }

  @Test
  public void testIncreaseC() {
    SimpleFactory f;
    int[] force = new int[1];
    Markov<Character> m = new MarkovNameGenerator.Markov<>(f = new SimpleFactory(0)) {
      @Override
      public int nextInt(int bound) {
        return force[0];
      }
    };
    State<Character> state = f.initial();
    m.increase(state, 'x');
    m.increase(state, 'x');
    m.increase(state, 'y');
    m.increase(state, (char) f.terminator());

    int countx = 0, county = 0, countT = 0;
    for (int i = 0; i < 4; ++i) {
      force[0] = i;
      Character c = m.step(state);
      switch (c) {
      case 'x':
        ++countx;
        break;
      case 'y':
        ++county;
        break;
      default:
        if (c.equals(f.terminator())) {
          ++countT;
        } else {
          fail();
        }
      }
    }
    assertEquals(2, countx);
    assertEquals(1, county);
    assertEquals(1, countT);

  }

  @Test
  public void testBadStateG() {
    SimpleFactory f;
    Markov<Character> m = new MarkovNameGenerator.Markov<>(f = new SimpleFactory(0));
    State<Character> state = f.initial();
    m.increase(state, 'x');
    assertNull(m.generate());
    List<Character> w = m.getLastWarning();
    assertEquals(1, w.size());
    assertEquals((Character) 'x', w.get(0));
    assertEquals(1, m.getWarnings());
  }

  private static char[] aToZ;
  private static char[] international;

  static {
    aToZ = new char[26];
    international = new char[26 * 5];
    for (int i = 0; i < 26; ++i) {
      aToZ[i] = (char) ('a' + i);
      international[i] = (char) ('A' + i);
      international[i + 26] = (char) ('a' + i);
      international[i + 26 * 2] = (char) ('Ä' + i);
      international[i + 26 * 3] = (char) ('Ä' + 26 + i);
      international[i + 26 * 4] = (char) ('\uac00' + i);
    }
    // for(int i=0)
  }

  @Test
  public void testManyNames() throws IOException {
    final int LOWER_LIMIT = 1000;
    String[] w = getWords(LOWER_LIMIT, 6, aToZ);
    writeWords(w, new File("test/names.txt"));

    MarkovNameGenerator namegen = new MarkovNameGenerator(new Properties(), new File("test"));
    assertEquals(LOWER_LIMIT, namegen.getLowerLimit());
    assertEquals(namegen.getLowerLimit(), namegen.getNamesCount());

    for (String element : w) {
      assertEquals(element, namegen.getName());
    }

    w = getWords(LOWER_LIMIT / 2, 7, aToZ);
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

  private void writeWords(String[] w, File file) throws IOException {
    try (PrintWriter out = new PrintWriter(new FileWriter(file))) {
      for (String name : w) {
        out.println(name);
      }
    } catch (IOException e) {
      fail();
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

  // private long startTime;
  //
  // private void resetTime() {
  // startTime = System.currentTimeMillis();
  // }
  //
  // private void printTime() {
  // System.err.println("Time " + (System.currentTimeMillis() - startTime));
  // }

  private static enum PTags {
    INIT, CREATE, CREATEC, NEW, GENERATEK, GENERATEC, NULL, GEN1
  }

  @Test
  public void testProfile() throws IOException {
    final int LOWER_LIMIT = 1000;
    final int REPETITIONS = 1;
    long tStart = System.currentTimeMillis();
    Profiler p = new Profiler();
    p.register(PTags.values());
    String[] w = getWords(LOWER_LIMIT * 4, 10, international);
    writeWords(w, new File("test/names.txt"));

    p.log(PTags.INIT);
    for (int i = 0; i < REPETITIONS; ++i) {
      MarkovGenerator.createK(w, 1);
      p.log(PTags.CREATE);
    }
    MarkovNameGenerator namegenK = new MarkovNameGenerator(new Properties(), new File("test"));
    namegenK.setLowerLimit(LOWER_LIMIT);

    p.log(PTags.NEW);
    // MarkovNameGenerator.resetProfilers();
    int MAX = 1000;
    for (int i = 0; i < 20 * REPETITIONS; ++i) {
      namegenK.reset();

      generateX(namegenK, MAX);
      p.log(PTags.GENERATEK);
      generateX(namegenK, MAX);
      p.log(PTags.GENERATEK);
    }
    p.printTags();
    System.err.println(String.format("total %10.6f ms", (double) (System.currentTimeMillis() - tStart)));
    System.err.println(namegenK.getInteger(AbstractNameGenerator.USED) * REPETITIONS + " names generated");
  }

  private void generateX(MarkovNameGenerator namegen, int n) {
    for (int i = 0; i < n; ++i) {
      if (namegen.getName() == null) {
        System.err.println("empty");
        return;
      }
    }
  }

  @Test
  public void testTable() {
    for (char c = 0; c < 255; ++c) {
      System.out.print(String.format("%3d %c  ", (int) c, c));
      if (c % 16 == 0) {
        System.out.println();
      }
    }

    System.out.println();
    System.out.println();
    StringBuilder b = new StringBuilder();
    int l = 1;
    int LINELEN = 80;
    for (char c : aToZ) {
      b.append(String.format("%3d %c  ", (int) c, c));
      if (b.length() - l * LINELEN > 0) {
        b.append("\n");
        ++l;
      }
    }
    System.out.println(b.toString());
    System.out.println();

    b = new StringBuilder();
    l = 1;
    for (char c : international) {
      b.append(String.format("%4x %c  ", (int) c, c));
      if (b.length() - l * LINELEN > 0) {
        b.append("\n");
        ++l;
      }
    }
    System.out.println(b.toString());
    System.out.println();
  }

  /**
   * Read a name file and play around with it.
   *
   */
  public static void main(String[] args) {
    int MAX = 5;
    List<String> names = readNames("etc/names/german.txt");
    Collections.shuffle(names);
    System.out.println("Sample size " + names.size());
    log("Original", IntStream.iterate(0, (i) -> i <= MAX, (i) -> i + 1)
        .mapToObj((i) -> String.format("Generated (%d)", i)).toArray());
    @SuppressWarnings("unchecked")
    Markov<Character>[] markov = IntStream.iterate(0, (i) -> i <= MAX, (i) -> i + 1)
        .mapToObj((i) -> MarkovGenerator.createK(names.toArray(new String[0]), i))
        .toArray(Markov[]::new);

    @SuppressWarnings("unchecked")
    Set<String>[] set = new HashSet[MAX + 1];
    for (int i = 0; i < 20 && i < names.size(); ++i) {
      log(names.get(i), Arrays.stream(markov)
          .map(Markov<Character>::generate)
          .map(IMarkov::listToString)
          .toArray((n) -> new String[n]));
    }
    int NUM = 100000;
    for (int x = 0; x <= MAX; ++x) {
      if (markov[x].warn > 0) {
        System.out.println(markov[x].warn + " incomplete: " + markov[x].lastWarn);
      }
      set[x] = new HashSet<>();
      for (int i = 0; i < NUM; ++i) {
        set[x].add(IMarkov.listToString(markov[x].generate()));
      }
    }
    {
      log("" + names.size(), IntStream.iterate(0, (i) -> i <= MAX, (i) -> i + 1)
          .mapToObj((i) -> String.format("%.2f%% unique", (double) set[i].size() / NUM * 100)).toArray());
    }

    // Markov<Character> mx = MarkovGenerator.createK(names.toArray(new String[0]), 2);
    // create(mx, names, 1000);

    createInsects();
  }

  private static String[] create(Markov<Character> insect, List<String> names, int amount) {
    List<String> words = new ArrayList<String>(amount);
    for (int x = 0; x < amount; ++x) {
      String n = IMarkov.listToString(insect.generate());
      if (!names.contains(n)) {
        words.add(n);
      } else {
        // System.out.println("!!!" + n);
      }
    }
    return words.toArray(new String[] {});
  }

  private static void createInsects() {
    char[] alphabet = new char[] { 'a', 'c', 'e', 'f', 'h', 'i', 'j', 'k', 'n', 'p', 'q', 's', 't', 'x', 'z' };

    int[][] values = {
        // { a, c, e, f, h, i, j, k, n, p, q, s, t, x, z };
        // start
        { 2, 5, 1, 6, 2, 6, 1, 10, 0, 8, 0, 10, 8, 4, 5 },
        // end
        { 1, 5, 1, 3, 4, 0, 1, 4, 2, 5, 1, 10, 4, 6, 8 },
        // a
        { 0, 5, 0, 10, 5, 0, 4, 5, 1, 5, 2, 5, 5, 8, 3 },
        { 3, 2, 1, 1, 10, 1, 0, 5, 0, 1, 0, 5, 1, 1, 1 },
        { 0, 5, 0, 10, 5, 0, 4, 5, 1, 5, 2, 5, 5, 8, 3 },
        { 2, 2, 1, 5, 1, 6, 1, 4, 3, 3, 1, 7, 5, 3, 5 },
        { 2, 2, 1, 5, 0, 6, 1, 4, 3, 3, 1, 7, 5, 3, 5 },
        { 0, 5, 0, 10, 5, 0, 4, 5, 1, 5, 2, 5, 5, 8, 3 },
        { 2, 2, 1, 5, 6, 6, 2, 4, 3, 3, 1, 7, 5, 3, 5 },
        // k
        { 3, 2, 1, 1, 10, 1, 0, 5, 0, 1, 0, 5, 1, 1, 1 },
        { 2, 5, 1, 6, 2, 6, 1, 10, 3, 8, 0, 10, 8, 4, 5 },
        { 2, 1, 1, 6, 6, 6, 2, 10, 0, 3, 0, 10, 6, 4, 5 },
        { 4, 1, 2, 1, 0, 4, 2, 0, 0, 0, 0, 8, 3, 4, 4 },
        { 2, 5, 1, 6, 1, 5, 1, 8, 1, 8, 0, 4, 10, 3, 6 },
        { 2, 3, 1, 6, 2, 6, 1, 6, 0, 3, 1, 10, 4, 4, 5 },
        { 2, 1, 1, 6, 0, 6, 3, 4, 0, 8, 2, 6, 6, 2, 1 },
        { 2, 2, 1, 6, 2, 6, 1, 6, 2, 1, 1, 6, 6, 5, 3 },
        // { a, c, e, f, h, i, j, k, n, p, q, s, t, x, z };
    };

    Markov<Character> insect = new Markov<Character>(new SimpleFactory(1));
    // scan(insect, alphabet);
    apply(insect, alphabet, values, 5, 11);

    String[] names = create(insect, Collections.emptyList(), 100);
    Arrays.sort(names);
    String line = null;
    int LINELEN = 80;
    System.out.println();
    for (String n : names) {
      n = n.substring(0, 1).toUpperCase() + n.substring(1);
      if (line == null) {
        line = n;
      } else {
        line += ", " + n;
      }
      if (line.length() > LINELEN) {
        System.out.println(line);
        line = null;
      }
    }
    if (line != null) {
      System.out.println(line);
    }

  }

  private static void apply(Markov<Character> insect, char[] alphabet, int[][] values, int minlength, int maxlength) {
    for (int i1 = 0; i1 < alphabet.length; ++i1) {
      char c1 = alphabet[i1];
      State<Character> state = insect.createState();
      int occ = values[0][i1];
      int end = values[1][i1];
      System.out.print(String.format("frequency for %c: start %-2d end %-2d ", c1, occ, end));
      for (int i = 0; i < occ; ++i) {
        insect.increase(state, c1);
      }
      for (int i2 = 0; i2 < alphabet.length; ++i2) {
        char c2 = alphabet[i2];
        occ = values[i1 + 2][i2];
        System.out.print(String.format("%c %-2d ", c2, occ));
        state = insect.createState();
        state.update(c1);

        for (int l = 1; l <= maxlength; ++l) {
          if (l < maxlength) {
            for (int i = 0; i < occ - Math.max(0, l - (maxlength + minlength) / 2); ++i) {
              insect.increase(state, c2);
            }
          }
          if (l >= minlength) {
            for (int i = 0; i < end - ((maxlength + minlength) / 2 - l); ++i) {
              insect.increase(state, new SimpleFactory(1).terminator());
            }
          }
          state.update(c1);
        }
      }
      System.out.println();
    }
  }

  private static void log(String name1, Object[] output) {
    String format = "%-30s";
    System.out.println(String.format(format, name1) +
        Arrays.stream(output)
            .map((s) -> String.format(format, s))
            .collect(Collectors.joining("\t")));
  }

  private static List<String> readNames(String filename) {
    List<String> names = new ArrayList<>();
    try {
      BufferedReader in = new BufferedReader(new FileReader(filename, Charset.forName("UTF8")));
      String name = null;

      while ((name = in.readLine()) != null) {
        name = name.trim();
        if (!name.startsWith("#") && !name.isEmpty()) {
          names.add(name.trim());
        }
      }

      in.close();
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
    return names;
  }

}
