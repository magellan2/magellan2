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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Creates names by expanding a list of names using a Markov model.
 *
 * @author stm
 * @version 1.0, Jul 26, 2022
 */
public class MarkovNameGenerator extends AbstractNameGenerator implements NameGenerator {

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
    protected Object[] context;

    /**
     * Create as copy of cs
     */
    public ICState(CharacterState cs) {
      context = Arrays.copyOf(cs.context, cs.context.length);
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
        return Arrays.equals(context, s2.context) && length == s2.length;
      }
      return false;
    }

    @Override
    public String toString() {
      return "S(" + Arrays.toString(context) + "," + length + ")";
    }

  }

  /**
   * A state based on the last x characters and the position in the word.
   */
  public static class CharacterState extends ICState implements State<Character>, Cloneable {

    /**
     * Creates a state that looks back length characters.
     */
    public CharacterState(int length) {
      super();
      context = new Character[length];
      hashCode = 0;
    }

    @Override
    public void update(Character newC) {
      if (length < context.length) {
        context[length] = newC;
      } else {
        for (int i = 1; i < context.length; ++i) {
          context[i - 1] = context[i];
        }
        if (context.length > 0) {
          context[context.length - 1] = newC;
        }
      }
      length++;
      hashCode = Arrays.hashCode(context) + 1023 * length;
    }

    @Override
    public State<Character> getImmutable() {
      return new ICState(this);
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
  public static interface IMarkov<K> {
    /**
     * Adds a 'word' by applying the outcomes one by one, starting from the initial state.
     */
    public void add(K[] w);

    /**
     * Modifies the model assuming that the outcome (the character) c is read at the given state.
     */
    public void increase(State<K> state, K c);

    /**
     * Generates a new 'word' by starting from the initial state and progressing until a terminator is reached or there
     * is no transition defined.
     * 
     * @throws IllegalStateException if the factory does not support a terminal state.
     */
    public List<K> generate();

    /**
     * Convert a list of characters to a string.
     */
    public static String listToString(List<Character> l) {
      return l.stream()
          .map(String::valueOf)
          .collect(Collectors.joining());
    }

    /**
     * Returns the last result of {@link #generate()} before a state without transition was reached or <code>null</code>
     * if no such event occured.
     */
    public List<K> getLastWarning();
  }

  /**
   * A special map to integers.
   */
  public static class IMap<K> {

    private Map<K, int[]> map = new HashMap<>();
    private int[] counts;
    private Object[] chars;
    int countAll;

    /**
     * Increase the value associated with key by 1.
     */
    public void increase(K key) {
      if (countAll > Integer.MAX_VALUE - 1)
        throw new OverflowException();
      int[] v = map.get(key);
      if (v == null) {
        v = new int[1];
        map.put(key, v);
      }
      ++v[0];
      ++countAll;
      counts = null;
    }

    /**
     * Return the sum of all values.
     */
    public int count() {
      return countAll;
    }

    /**
     * Returns the value associated with c.
     */
    public int get(char c) {
      int[] v = map.get(c);
      if (v == null)
        return 0;
      return v[0];
    }

    /**
     * Selects the smallest value of k such that sum(c<=k){value(c)} <= r.
     */
    public K select(int r) {
      K s = select2(r);
      return s;
    }

    protected K select0(int r) {
      if (map == null)
        return null;
      long s = 0;
      K last = null;
      for (K k : map.keySet()) {
        last = k;
        s += map.get(k)[0];
        if (r < s)
          return k;
      }
      return last;

    }

    @SuppressWarnings("unchecked")
    protected K select2(int r) {
      if (counts == null) {
        int count = map.size(), sum = 0, j = 0;

        counts = new int[count];
        chars = new Object[count];
        for (K c : map.keySet()) {
          sum += map.get(c)[0];
          counts[j] = sum;
          chars[j++] = c;
        }
      }

      int i = Arrays.binarySearch(counts, r);
      if (i < 0) {
        // i is -(insertion point) - 1
        i = -i - 1;
      } else {
        // i is position of r
        i = i + 1;
      }
      if (i >= counts.length) {
        i = counts.length - 1;
      }
      return (K) chars[i];
    }

  }

  /**
   * An implementation using nested maps.
   */
  public static class Markov<K> implements IMarkov<K> {
    Map<State<K>, IMap<K>> model;

    protected int warn;
    protected List<K> lastWarn;

    private StateFactory<K> factory;

    /**
     * Creates a new, empty model using the states from the factory.
     * 
     * @throws IllegalArgumentException if the factory does not support a terminal state.
     */
    public Markov(StateFactory<K> factory) {
      if (factory.terminator() == null)
        throw new IllegalArgumentException("no terminal state defined");
      model = new HashMap<>();
      this.factory = factory;
    }

    protected State<K> createState() {
      return factory.initial();
    }

    public void add(K[] w) {
      State<K> state = createState();
      for (K c : w) {
        increase(state, c);
        state.update(c);
      }
      increase(state, factory.terminator());
    }

    public void increase(State<K> state, K c) {
      IMap<K> map2 = model.get(state);
      if (map2 == null) {
        map2 = new IMap<K>();
        State<K> clone = state.getImmutable();
        model.put(clone, map2);
      }
      map2.increase(c);
    }

    public List<K> generate() {
      State<K> state = createState();
      List<K> word = new ArrayList<>();
      while (true) {
        K nextC = step(state);
        if (nextC == null) {
          ++warn;
          lastWarn = word;
          return null;
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
    protected K step(State<K> state) {
      IMap<K> map2 = model.get(state);
      if (map2 == null)
        return null;
      return map2.select2(nextInt(map2.count()));
    }

    protected int nextInt(int bound) {
      if (bound <= 0)
        throw new IllegalArgumentException("bound most be positive");
      return ThreadLocalRandom.current().nextInt(bound);
    }

    /**
     * Returns the number of warnings
     */
    public int getWarnings() {
      return warn;
    }

    public List<K> getLastWarning() {
      return lastWarn;
    }

  }

  /**
   * Generates a Markov class instance.
   */
  public static class MarkovGenerator {

    /**
     * Creates a model from a list of words with given lookback length.
     * 
     */
    public static Markov<Character> createK(String[] words, int length) {
      Markov<Character> m = new Markov<Character>(new SimpleFactory(length));

      for (String w : words) {
        Character[] xx = w.chars().mapToObj(c -> Character.valueOf((char) c)).toArray(i -> new Character[i]);
        m.add(xx);
      }
      return m;
    }

  }

  /**
   * Variable name for the number of initially loaded names.
   */
  public static final String ORIGINAL = "original";

  private int lowerLimit = 1000;

  /**
   * @return The current lower limit
   */
  public int getLowerLimit() {
    return lowerLimit;
  }

  /**
   * Sets the lower limit of names (original + generated) that should be available.
   */
  public void setLowerLimit(int lowerLimit) {
    if (lowerLimit <= 0)
      throw new IllegalArgumentException("limit must be > 0");
    this.lowerLimit = lowerLimit;
  }

  private int original;
  private int maxSize;
  private IMarkov<Character> markov;

  /**
   * Initialize and load list of files, if defined.
   * 
   * @param settings The client settings
   * @param settingsDir The directory where a configuration file can be stored
   */
  public MarkovNameGenerator(Properties settings, File settingsDir) {
    super(settings, settingsDir);
  }

  /**
   * @see magellan.library.utils.AbstractNameGenerator#load(java.lang.String)
   */
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
    update();
    String name = super.getName();
    return name;
  }

  @Override
  public int getNamesCount() {
    update();
    return super.getNamesCount();
  }

  /**
   * Resets the number of used names to 0.
   */
  public void reset() {
    names = Arrays.copyOf(names, getInteger(ORIGINAL));
    setInteger(USED, 0);
    maxSize = -1;
  }

  protected void update() {
    if (lowerLimit == 0)
      return;
    if (names == null) // not initialized
      return;
    int count = names.length - getInteger(USED);
    if (count >= lowerLimit)
      return;
    if (maxSize >= 0 && maxSize <= names.length) // we tried what we could
      return;

    // try creating LOWER_LIMIT / 10 new names with highest possible quality (= length)
    int needed = Math.max(lowerLimit / 10, lowerLimit - count);
    Set<String> all = new HashSet<>(names.length + needed);
    all.addAll(Arrays.asList(names));
    Set<String> added = new HashSet<>(needed);

    if (markov == null) {
      int length = 5;
      for (; length > 0; --length) {
        markov = MarkovGenerator.createK(names, length);
        added.clear();
        for (int i = 0; i < lowerLimit && added.size() < needed; ++i) {
          String name = generateName();
          if (name != null && !all.contains(name)) {
            added.add(name);
          }
        }
        if (added.size() >= needed) {
          break;
        }
      }
    } else {
      for (int i = 0; i < lowerLimit && added.size() < needed; ++i) {
        String name = generateName();
        if (name != null && !all.contains(name)) {
          added.add(name);
        }
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

  }

  protected String generateName() {
    return IMarkov.listToString(markov.generate());
  }
}
