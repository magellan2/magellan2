// class magellan.library.utils.MarkovNameGenerator
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MarkovNameGenerator extends AbstractNameGenerator implements NameGenerator {

  private static enum ProfileCreate {
    C_START, C_CREATE, C_LOOP, C_INCREASE, C_UPDATE, C_TERMINATE, C_MGET, C_NEWMAP, C_PUT, C_CLONE;
  }

  private static enum ProfileGenerate {
    INIT, INIT2, CREATE, CLEAR, LOOP2, GENERATE, ADD, CONTAIN, COPY, LOG,
    NORMALIZE, CREATE2, LOOP3, STEP, UPDATE2, APPEND, ENDG,
    GET3, SELECT3, GET3l,
    LOOP4, GENERATE4, ADD4, CONTAIN4;
  }

  public static class Profiler {

    private static final String formatMax = "**%10s %10.6f / %10d";
    private static final String formatMany = "*%11s %10.6f / %10d";
    private static final String formatNormal = "%12s %10.6f / %10d";
    private long startGlobal = System.nanoTime();
    private long startSplit = System.nanoTime();
    private long startFine = System.nanoTime();
    private long[] logs;
    private long[] counts;
    private Enum<?>[] tags;

    public void split() {
      startSplit = System.nanoTime();
      startFine = startSplit;
    }

    public void print(String string) {
      System.err.println(string);
    }
    // long t2 = System.nanoTime();
    // System.err.print(String.format(" %s %10.6f:%10.6f:%10.6f ", string,
    // (double) (t2 - startFine) / 1000000,
    // (double) (t2 - startSplit) / 1000000,
    // (double) (t2 - startGlobal) / 1000000));
    // startFine = t2;
    // }

    public void log(Enum<?> tag) {
      if (logs == null)
        return;
      long t2 = System.nanoTime();
      logs[tag.ordinal()] += t2 - startFine;
      ++counts[tag.ordinal()];
      startFine = t2;
    }

    public void register(Enum<?>... tags) {
      this.tags = tags;
      logs = new long[tags.length];
      counts = new long[tags.length];
    }

    public void printTags() {
      if (logs == null)
        return;
      long max = 0;
      for (long tt : logs)
        if (max < tt) {
          max = tt;
        }
      long total = 0;
      for (Enum<?> tag : tags) {
        long tt = logs[tag.ordinal()];
        total += tt;
        String f;
        if (tt >= max) {
          f = formatMax;
        } else if (tt * 10 >= max) {
          f = formatMany;
        } else {
          f = formatNormal;
        }
        System.err.println(String.format(f, tag.name(), (double) tt / 1000000, counts[tag.ordinal()]));
      }
      System.err.println(String.format(formatNormal, "TOTAL", (double) total / 1000000, 1));
      System.err.println();
    }

  }

  static Profiler profilerCreate = new Profiler();
  static Profiler profilerGenerate = new Profiler();

  protected static void resetProfilers() {
    profilerCreate.register(ProfileCreate.values());
    profilerGenerate.register(ProfileGenerate.values());
    profilerCreate.split();
    profilerGenerate.split();
  }

  /**
   * Thrown on integer overflow.
   */
  public static class OverflowException extends RuntimeException {
    /**
     * Constructs an exception.
     */
    public OverflowException() {
      super("integer overflow");
    }
  }

  /**
   * Records the current situation while scanning a word.
   */
  public static interface State<K> {

    /**
     * Change the state after c is read
     */
    void update(K c);

    /**
     * Creates an immutable copy of this state. If update is called on an immutable state, an
     * {@link IllegalStateException} is thrown.
     */
    State<K> getImmutable();

    // don't forget to overwrite these!
    @Override
    int hashCode();

    // don't forget to overwrite these!
    @Override
    boolean equals(Object obj);

  }

  /**
   * Creates a state
   */
  public static interface StateFactory<K> {
    /**
     * Creates a state
     */
    State<K> initial();

    /**
     * Returns the terminal outcome.
     */
    K terminator();
  }

  /**
   * Immutable version if CharacterState.
   */
  public static class ICState implements State<Character> {

    protected int hashCode;
    protected int length;
    protected Object[] c;

    public ICState(CharacterState cs) {
      c = Arrays.copyOf(cs.c, cs.c.length);
      hashCode = cs.hashCode;
      length = cs.length;
    }

    protected ICState() {
      //
    }

    public void update(Character c) {
      throw new UnsupportedOperationException("immutable class");
    }

    public State<Character> getImmutable() {
      return this;
    }

    @Override
    public int hashCode() {
      return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof ICState) {
        ICState s2 = (ICState) obj;
        return Arrays.equals(c, s2.c) && length == s2.length;
      }
      return false;
    }

    @Override
    public String toString() {
      return "S(" + Arrays.toString(c) + "," + length + ")";
    }

  }

  /**
   * A state based on the last x characters and the position in the word.
   */
  public static class CharacterState extends ICState implements State<Character>, Cloneable {

    // private Character[] c;
    // private int length;
    // private int hashCode;

    /**
     * Creates a state that looks back length characters.
     */
    public CharacterState(int length) {
      super();
      c = new Character[length];
      hashCode = 0;
    }

    @Override
    public void update(Character newC) {
      if (length < c.length) {
        c[length] = newC;
      } else {
        for (int i = 1; i < c.length; ++i) {
          c[i - 1] = c[i];
        }
        if (c.length > 0) {
          c[c.length - 1] = newC;
        }
      }
      length++;
      hashCode = Arrays.hashCode(c) + 1023 * length;
    }

    @Override
    public State<Character> getImmutable() {
      return new ICState(this);
      // try {
      // CharacterState s = (CharacterState) clone();
      // s.c = Arrays.copyOf(c, c.length);
      // s.immutable = true;
      // return s;
      // } catch (CloneNotSupportedException e) {
      // throw new IllegalStateException("this cannot happen");
      // }
    }

  }

  /**
   * Creates a {@link CharacterState}.
   */
  public static class SimpleFactory implements StateFactory<Character> {

    private int length;

    /**
     * Creates a factory that supplies states with lookback length.
     */
    public SimpleFactory(int length) {
      if (length < 0)
        throw new IllegalArgumentException("length must be non-negative");
      this.length = length;
    }

    public State<Character> initial() {
      return new CharacterState(length);
    }

    public Character terminator() {
      return Character.MAX_VALUE;
    }

  }

  /**
   * A class that represents a markov chain.
   */
  public static class Markov<K> {
    Map<State<K>, Map<K, Integer>> model;
    Map<State<K>, Long> weights;

    Random rng = new Random();

    private int warn;
    private String lastWarn;

    private boolean normalized;
    private StateFactory<K> factory;

    /**
     * Creates a new, empty model using the states from the factory.
     */
    public Markov(StateFactory<K> factory) {
      model = new HashMap<>();
      weights = new HashMap<>();
      normalized = false;
      this.factory = factory;
    }

    protected State<K> createState() {
      return factory.initial();
    }

    /**
     * Adds a 'word' by applying the outcomes one by one, starting from the initial state.
     */
    public void add(K[] w) {
      State<K> state = createState();
      for (K c : w) {
        increase(state, c);
        state.update(c);
      }
      increase(state, factory.terminator());
    }

    /**
     * Modifies the model assuming that the outcome (the character) c is read at the given state.
     */
    public void increase(State<K> state, K c) {
      Map<K, Integer> map2 = model.get(state);
      if (map2 == null) {
        map2 = new HashMap<K, Integer>();
      }
      increase(map2, c);
      model.put(state.getImmutable(), map2);
    }

    private void increase(Map<K, Integer> map, K key) {
      Integer v = map.get(key);
      if (v == null) {
        v = 0;
      }
      map.put(key, v + 1);
    }

    protected void normalize() {
      weights.clear();
      for (State<K> s : model.keySet()) {
        Map<K, Integer> map2 = model.get(s);
        weights.put(s, count(map2));
      }
      normalized = true;
    }

    private Long count(Map<K, Integer> map2) {
      long sum = 0;
      for (K c2 : map2.keySet()) {
        Integer v = map2.get(c2);
        if (Long.MAX_VALUE - v < v)
          throw new OverflowException();
        sum += v;
      }
      return sum;
    }

    /**
     * Generates a new 'word' by starting from the initial state and progressing until a terminator is reached.
     * 
     * @throws IllegalStateException if the factory does not support a terminal state.
     */
    public List<K> generate() {
      if (factory.terminator() == null)
        throw new IllegalStateException("no terminal state defined");
      if (!normalized) {
        normalize();
      }

      State<K> state = createState();
      List<K> word = new ArrayList<>();
      while (true) {
        K nextC = step(state);
        if (nextC == null) {
          ++warn;
          lastWarn = word.toString();
          break;
        }
        if (nextC.equals(factory.terminator())) {
          break;
        }
        state.update(nextC);
        word.add(nextC);
      }
      return word;
    }

    /**
     * Make a step from the given state.
     * 
     * @return the outcome of the step
     */
    public K step(State<K> state) {
      Map<K, Integer> map2 = model.get(state);
      return select(map2, weights.get(state));
    }

    private K select(Map<K, Integer> map, Long sum) {
      if (map == null)
        return null;
      long r = nextLong(sum);
      long s = 0;
      K last = null;
      for (K k : map.keySet()) {
        last = k;
        s += map.get(k);
        if (r < s)
          return k;
      }
      return last;
    }

    private long nextLong(Long bound) {
      if (bound <= 0)
        throw new IllegalArgumentException("bound most be positive");
      return ThreadLocalRandom.current().nextLong(bound);
    }

  }

  public static class CMap {
    private static final char DMAX = 1256;
    private static final char DMIN = 1256;
    private char MIN = DMIN;
    private char MAX = DMAX;
    int[] primitives = new int[MAX - MIN];
    int imax;
    Map<Character, Integer> complexs;
    long sum;
    long[] cache;
    private Character lastP;

    public void setMinMax(char min, char max) {
      if (min < 0 || min >= Character.MAX_VALUE - 1 || max < 1 || max >= Character.MAX_VALUE || max <= min)
        throw new IllegalArgumentException("min/max out of range");
      MIN = min;
      MAX = max;
      primitives = new int[MAX - MIN];
    }

    public void increase(char c) {
      if (c >= MIN && c < MAX) {
        ++primitives[c - MIN];
        cache = null;
      } else if (c == Character.MAX_VALUE) {
        ++imax;
      } else {
        if (complexs == null) {
          complexs = new HashMap<>();
        }
        Integer v = complexs.get(c);
        if (v == null) {
          v = 0;
        }
        complexs.put(c, v + 1);
      }
      ++sum;
    }

    public long count() {
      return sum;
    }

    public long get(char c) {
      if (c >= MIN && c < MAX)
        return primitives[c - MIN];
      if (c == Character.MAX_VALUE)
        return imax;
      if (complexs != null)
        return complexs.get(c);
      return 0;
    }

    public Character select0(long r) {
      long s = imax;
      Character last = Character.MAX_VALUE;
      if (r < s)
        return Character.MAX_VALUE;
      for (char i = MIN; i < MAX; ++i) {
        if (primitives[i - MIN] > 0) {
          last = i;
        }
        s += primitives[i - MIN];
        if (r < s)
          return Character.valueOf(i);
      }
      if (complexs != null) {
        for (Character c : complexs.keySet()) {
          last = c;
          s += complexs.get(c);
          if (r < s)
            return c;
        }
      }
      return last;
    }

    public Character select(long r) {
      long s = imax;
      Character last = Character.MAX_VALUE;
      if (r < s)
        return Character.MAX_VALUE;
      if (cache == null) {
        cache = new long[primitives.length];
        lastP = null;
        for (char i = MIN; i < MAX; ++i) {
          s += primitives[i - MIN];
          if (primitives[i - MIN] > 0) {
            lastP = i;
          }
          cache[i - MIN] = s;
        }
      } else {
        s = imax;
      }
      {
        int i = Arrays.binarySearch(cache, r);
        if (i < 0) {
          // i is -(insertion point) - 1
          i = -(i + 1);
        } else {
          // i is position of r
          i = i + 1;
          while (i < cache.length && primitives[i] == 0) {
            ++i;
          }
        }
        if (i < primitives.length)
          return Character.valueOf((char) (MIN + i));
      }
      if (lastP != null) {
        last = lastP;
      }

      if (complexs != null) {
        for (Character c : complexs.keySet()) {
          last = c;
          s += complexs.get(c);
          if (r < s)
            return c;
        }
      }
      return last;
    }
  }

  /**
   * A class that represents a markov chain.
   */
  public static class CMarkov {
    Map<State<Character>, CMap> model;
    Map<State<Character>, Long> weights;

    Random rng = new Random();

    private int warn;
    private String lastWarn;

    private boolean normalized;
    private StateFactory<Character> factory;

    /**
     * Creates a new, empty model using the states from the factory.
     */
    public CMarkov(StateFactory<Character> factory) {
      model = new HashMap<>();
      weights = new HashMap<>();
      normalized = false;
      this.factory = factory;
    }

    protected State<Character> createState() {
      return factory.initial();
    }

    /**
     * Adds a 'word' by applying the outcomes one by one, starting from the initial state.
     */
    public void add(String w) {
      profilerCreate.log(ProfileCreate.C_START);
      State<Character> state = createState();
      profilerCreate.log(ProfileCreate.C_CREATE);
      for (int i = 0, n = w.length(); i < n; i++) {
        char c = w.charAt(i);
        profilerCreate.log(ProfileCreate.C_LOOP);
        increase(state, c);
        profilerCreate.log(ProfileCreate.C_INCREASE);
        state.update(c);
        profilerCreate.log(ProfileCreate.C_UPDATE);
      }
      increase(state, factory.terminator());
      profilerCreate.log(ProfileCreate.C_TERMINATE);
    }

    /**
     * Modifies the model assuming that the outcome (the character) c is read at the given state.
     */
    public void increase(State<Character> state, char c) {
      CMap map2 = model.get(state);
      profilerCreate.log(ProfileCreate.C_MGET);
      if (map2 == null) {
        map2 = new CMap();
        map2.setMinMax((char) 32, (char) 256);
        profilerCreate.log(ProfileCreate.C_NEWMAP);
      }
      map2.increase(c);
      profilerCreate.log(ProfileCreate.C_INCREASE);

      State<Character> clone = state.getImmutable();
      profilerCreate.log(ProfileCreate.C_CLONE);
      model.put(clone, map2);
      profilerCreate.log(ProfileCreate.C_PUT);

    }

    protected void normalize() {
      weights.clear();
      for (State<Character> s : model.keySet()) {
        CMap map2 = model.get(s);
        weights.put(s, map2.count());
      }
      normalized = true;
    }

    /**
     * Generates a new 'word' by starting from the initial state and progressing until a terminator is reached.
     * 
     * @throws IllegalStateException if the factory does not support a terminal state.
     */
    public String generate() {
      if (factory.terminator() == null)
        throw new IllegalStateException("no terminal state defined");
      if (!normalized) {
        normalize();
        profilerGenerate.log(ProfileGenerate.NORMALIZE);
      }

      State<Character> state = createState();
      profilerGenerate.log(ProfileGenerate.CREATE2);
      StringBuilder word = new StringBuilder();
      while (true) {
        profilerGenerate.log(ProfileGenerate.LOOP3);
        Character nextC = step(state);
        profilerGenerate.log(ProfileGenerate.STEP);
        if (nextC == null) {
          ++warn;
          lastWarn = word.toString();
          break;
        }
        if (nextC.equals(factory.terminator())) {
          break;
        }
        state.update(nextC);
        profilerGenerate.log(ProfileGenerate.UPDATE2);
        word.append(nextC);
        profilerGenerate.log(ProfileGenerate.APPEND);
      }
      profilerGenerate.log(ProfileGenerate.ENDG);

      return word.toString();
    }

    /**
     * Make a step from the given state.
     * 
     * @return the outcome of the step
     */
    public Character step(State<Character> state) {
      CMap map2 = model.get(state);
      profilerGenerate.log(ProfileGenerate.GET3);
      Long w = weights.get(state);
      profilerGenerate.log(ProfileGenerate.GET3l);
      Character c = select(map2, w);
      profilerGenerate.log(ProfileGenerate.SELECT3);
      return c;
    }

    private Character select(CMap map, Long sum) {
      if (map == null)
        return null;
      long r = nextLong(sum);
      Character s = map.select0(r);
      // Character s = map.select(r);
      return s;
    }

    private long nextLong(Long bound) {
      if (bound <= 0)
        throw new IllegalArgumentException("bound most be positive");
      return ThreadLocalRandom.current().nextLong(bound);
    }

  }

  public static class MarkovGenerator {

    /**
     * Creates a model from a list of words with given lookback length.
     * 
     */
    public static CMarkov create(String[] words, int length) {
      CMarkov m = new CMarkov(new SimpleFactory(length));
      profilerCreate.register(ProfileCreate.values());

      profilerCreate.split();
      for (String w : words) {
        // Character[] xx = w.chars().mapToObj(c -> Character.valueOf((char) c)).toArray(i -> new Character[i]);
        // System.err.print(System.nanoTime() - t + " ");
        m.add(w);
        // profilerCreate.print();
      }
      System.err.println();
      profilerCreate.printTags();
      return m;
    }

  }

  public static final String ORIGINAL = "original";

  public static String listToString(List<Character> l) {
    return l.stream()
        .map(String::valueOf)
        .collect(Collectors.joining());
  }

  public static void main(String[] args) {
    int MAX = 5;
    List<String> names = readNames("etc/names/insect.txt");
    Collections.shuffle(names);
    System.out.println("Sample size " + names.size());
    log("Original", IntStream.iterate(0, (i) -> i <= MAX, (i) -> i + 1)
        .mapToObj((i) -> String.format("Generated (%d)", i)).toArray());
    @SuppressWarnings("unchecked")
    Markov<Character>[] markov = IntStream.iterate(0, (i) -> i <= MAX, (i) -> i + 1)
        .mapToObj((i) -> MarkovGenerator.create(names.toArray(new String[0]), i))
        .toArray(Markov[]::new);

    @SuppressWarnings("unchecked")
    Set<String>[] set = new HashSet[MAX + 1];
    for (int i = 0; i < 20 && i < names.size(); ++i) {
      log(names.get(i), Arrays.stream(markov)
          .map(Markov<Character>::generate)
          .map(MarkovNameGenerator::listToString)
          .toArray((n) -> new String[n]));
    }
    for (int x = 0; x <= MAX; ++x) {
      if (markov[x].warn > 0) {
        System.out.println(markov[x].warn + " incomplete: " + markov[x].lastWarn);
      }
      set[x] = new HashSet<>();
      for (int i = 0; i < 100000; ++i) {
        set[x].add(listToString(markov[x].generate()));
      }
    }
    {
      log("" + names.size(), IntStream.iterate(0, (i) -> i <= MAX, (i) -> i + 1)
          .mapToObj((i) -> String.format("%d unique", set[i].size())).toArray());
    }

    CMarkov mx = MarkovGenerator.create(names.toArray(new String[0]), 2);
    create(mx, names, 1000);
    // createInsect();
  }

  private static void create(CMarkov mx, List<String> names, int amount) {
    for (int x = 0; x < amount; ++x) {
      String n = mx.generate();
      if (!names.contains(n)) {
        System.out.println(n);
      } else {
        // System.out.println("!!!" + n);
      }
    }
  }

  private static void createInsect() {
    char[] alphabet = new char[] { 'a', 'c', 'e', 'f', 'h', 'i', 'j', 'k', 'n', 'p', 'q', 's', 't', 'x', 'z' };

    CMarkov insect = new CMarkov(new SimpleFactory(1));
    Scanner sc = new Scanner(System.in);
    for (Character c1 : alphabet) {
      State<Character> state = insect.createState();
      System.out.print("frequency for start with " + c1 + ": ");
      int occ = sc.nextInt();
      System.out.print("frequency for end with " + c1 + ": ");
      int end = sc.nextInt();
      for (int i = 0; i < occ; ++i) {
        insect.increase(state, c1);
      }
      state.update(c1);
      for (Character c2 : alphabet) {
        System.out.print("frequency for " + c1 + c2 + ": ");
        occ = sc.nextInt();
        state = insect.createState();
        for (int l = 1; l < 12; ++l) {
          for (int i = 0; i < occ; ++i) {
            insect.increase(state, c2);
          }
          if (l > 5) {
            for (int i = 0; i < end - Math.abs(8 - l); ++i) {
              insect.increase(state, new SimpleFactory(1).terminator());
            }
          }
          state.update(c1);
        }
      }
    }
    sc.close();
    create(insect, Collections.emptyList(), 1000);
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

  public int lowerLimit = 1000;

  public int getLowerLimit() {
    return lowerLimit;
  }

  public void setLowerLimit(int lowerLimit) {
    if (lowerLimit <= 0)
      throw new IllegalArgumentException("limit must be > 0");
    this.lowerLimit = lowerLimit;
  }

  private int original;
  private int maxSize;
  private CMarkov markov;

  public MarkovNameGenerator(Properties settings, File settingsDir) {
    super(settings, settingsDir);
    updateIfInitialized();
  }

  @Override
  public void load(String fileName) {
    clearVariables();
    super.load(fileName);
    original = 0;
    if (names != null) {
      original = names.length;
    }
    maxSize = -1;
    markov = null;

    updateIfInitialized();

    if (getVariable(ORIGINAL) == null) {
      setInteger(ORIGINAL, original);
    } else {
      original = getInteger(ORIGINAL);
    }
  }

  /**
   * @see magellan.library.utils.NameGenerator#getName()
   */
  @Override
  public String getName() {
    String name = super.getName();
    updateIfInitialized();
    return name;
  }

  protected void updateIfInitialized() {
    if (lowerLimit == 0)
      return;
    if (getNamesCount() >= lowerLimit)
      return;
    if (names == null)
      return;
    if (maxSize >= 0 && maxSize <= names.length)
      return;

    // try creating LOWER_LIMIT / 10 new names with highest possible quality (= length)
    int needed = Math.max(lowerLimit / 10, lowerLimit - getNamesCount());
    profilerGenerate.log(ProfileGenerate.INIT);
    Set<String> all = new HashSet<>(names.length + needed);
    all.addAll(Arrays.asList(names));
    Set<String> added = new HashSet<>(needed);

    if (markov == null) {
      int length = 5;
      for (; length > 0; --length) {
        profilerGenerate.log(ProfileGenerate.INIT2);
        markov = MarkovGenerator.create(names, length);
        profilerGenerate.log(ProfileGenerate.CREATE);
        added.clear();
        profilerGenerate.log(ProfileGenerate.CLEAR);
        for (int i = 0; i < lowerLimit && added.size() < needed; ++i) {
          profilerGenerate.log(ProfileGenerate.LOOP2);
          String name = markov.generate();
          profilerGenerate.log(ProfileGenerate.GENERATE);
          if (!all.contains(name)) {
            added.add(name);
            profilerGenerate.log(ProfileGenerate.ADD);
          }
          profilerGenerate.log(ProfileGenerate.CONTAIN);
        }
        // profilerGenerate.print("generated\n" + length);
        if (added.size() >= needed) {
          break;
        }
      }
    } else {
      for (int i = 0; i < lowerLimit && added.size() < needed; ++i) {
        profilerGenerate.log(ProfileGenerate.LOOP4);
        String name = markov.generate();
        profilerGenerate.log(ProfileGenerate.GENERATE4);
        if (!all.contains(name)) {
          added.add(name);
          profilerGenerate.log(ProfileGenerate.ADD4);
        }
        profilerGenerate.log(ProfileGenerate.CONTAIN4);
      }

    }

    // add to name list
    if (added.size() > 0) {
      int start = names.length;
      names = Arrays.copyOf(names, names.length + Math.min(added.size(), needed));
      for (String name : added) {
        names[start++] = name;
        if (start >= names.length) {
          break;
        }
      }
    }
    if (added.size() < needed) {
      maxSize = names.length;
    }

    profilerGenerate.log(ProfileGenerate.COPY);
    profilerGenerate.printTags();
    profilerGenerate.log(ProfileGenerate.LOG);
    profilerGenerate.print("\n");
  }
}
