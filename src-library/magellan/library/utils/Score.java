// class magellan.library.utils.Score
// created on 19.05.2008
//
// Copyright 2003-2008 by magellan project team
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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

/**
 * A class that maintains and integer score and type for a key.
 * 
 * @param <K> The type of the key.
 * @author Ralf Duckstein
 * @version 1.0, 21.05.2008
 */
public class Score<K> implements Comparable<Score<K>> {
  private K key;
  private int score = 0;
  private String type;
  private OrderedHashtable<String, String> types = new OrderedHashtable<String, String>(2);

  /**
   * Creates a score without type and value 0.
   */
  public Score(K key) {
    this(key, 0);
  }

  /**
   * Creates a score without type.
   */
  public Score(K key, int score) {
    this(key, score, null);
  }

  /**
   * Creates a score with one type.
   */
  public Score(K key, int score, String type) {
    this.key = key;
    this.score = score;
    this.type = type;
  }

  /**
   * Returns the key.
   */
  public K getKey() {
    return this.key;
  }

  /**
   * Returns the score.
   */
  public int getScore() {
    return this.score;
  }

  /**
   * Increases the score by <code>add</code>.
   */
  public int addScore(int add) {
    return this.score += add;
  }

  /**
   * Changes the score to <code>score</code>.
   */
  public int setScore(int score) {
    return this.score = score;
  }

  /**
   * Returns the (first) type.
   */
  public String getType() {
    return this.type;
  }

  /**
   * Sets the type.
   */
  public String setType(String type) {
    HashMap<String, String> oldTypes = new HashMap<String, String>(types);
    this.types.clear();
    this.types.put(type, type);
    for (String old : oldTypes.values()) {
      this.types.put(old, old);
    }
    return this.type = type;

  }

  /**
   * Adds an additional type.
   */
  public void addType(String type) {
    if (this.types.isEmpty()) {
      this.type = type;
    }
    this.types.put(type, type);
  }

  /**
   * Returns a collection of all types. The first value will always be {@link #getType()}.
   */
  public Collection<String> getTypes() {
    return Collections.unmodifiableCollection(types.values());
  }

  /**
   * Compares the score to another score. Partial order, so it violates contracts of compareTo(),
   * equals() and hashCode()!
   * 
   * @return 1 if this score is greater than <code>s</code>
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Score<K> s) {
    return score > s.getScore() ? 1 : (score < s.getScore() ? -1 : 0);
  }

  @Override
  public String toString() {
    StringBuilder b = new StringBuilder();
    for (String t : types.values()) {
      if (b.length() > 0) {
        b.append(", ");
      }
      b.append(t);
    }
    b.append(": ").append(key.toString()).append(" = ").append(score);
    return b.toString();
  }

}
