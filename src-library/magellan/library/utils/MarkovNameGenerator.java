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
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MarkovNameGenerator implements NameGenerator {
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
   * A state based on the last x characters and the position in the word.
   */
  public static class CharacterState implements State<Character>, Cloneable {

    private Character[] c;
    private boolean immutable;
    private int length;
    private int hashCode;

    /**
     * Creates a state that looks back length characters.
     */
    public CharacterState(int length) {
      c = new Character[length];
      hashCode = 0;
    }

    public void update(Character newC) {
      if (immutable)
        throw new IllegalStateException("cannot change immutable");
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
      hashCode = Arrays.hashCode(c) + 31 * length;
    }

    public State<Character> getImmutable() {
      try {
        CharacterState s = (CharacterState) clone();
        s.c = Arrays.copyOf(c, c.length);
        s.immutable = true;
        return s;
      } catch (CloneNotSupportedException e) {
        throw new IllegalStateException("this cannot happen");
      }
    }

    @Override
    public int hashCode() {
      return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof CharacterState) {
        CharacterState s2 = (CharacterState) obj;
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

  public static class MarkovGenerator {

    /**
     * Creates a model from a list of words with given lookback length.
     * 
     */
    public static Markov<Character> create(String[] words, int length) {
      Markov<Character> m = new Markov<Character>(new SimpleFactory(length));
      for (String w : words) {
        m.add(w.chars().mapToObj(c -> Character.valueOf((char) c)).toArray(i -> new Character[i]));
      }
      return m;
    }

  }

  private static String listToString(List<Character> l) {
    return l.stream()
        .map(String::valueOf)
        .collect(Collectors.joining());
  }

  public static void main(String[] args) {
    int MAX = 5;
    List<String> names = readNames("etc/names/human.txt");
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
        if (name.startsWith("#")) {
          // lines starting with # are comments,
          // unless they start with two ##, in which case the first # is deleted
          if (name.startsWith("##")) {
            name = name.substring(1);
          } else {
            name = "";
          }
        }
        if (!name.isEmpty()) {
          names.add(name.trim());
        }
      }

      in.close();
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
    return names;
  }

  public boolean isActive() {
    // HIGHTODO Automatisch generierte Methode implementieren
    return false;
  }

  public boolean isAvailable() {
    // HIGHTODO Automatisch generierte Methode implementieren
    return false;
  }

  public void setEnabled(boolean available) {
    // HIGHTODO Automatisch generierte Methode implementieren

  }

  public String getName() {
    // HIGHTODO Automatisch generierte Methode implementieren
    return null;
  }

  public int getNamesCount() {
    // HIGHTODO Automatisch generierte Methode implementieren
    return 0;
  }

}
