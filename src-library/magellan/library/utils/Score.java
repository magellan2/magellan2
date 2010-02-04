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

/**
 * @author Ralf Duckstein
 * @version 1.0, 21.05.2008
 */

public class Score<K> implements Comparable<Score<K>> {
  private K key;
  private int score = 0;
  private String type;

  public Score(K key) {
    this(key, 0);
  }

  public Score(K key, int score) {
    this(key, score, null);
  }

  public Score(K key, int score, String type) {
    this.key = key;
    this.score = score;
    this.type = type;
  }

  public K getKey() {
    return this.key;
  }

  public int getScore() {
    return this.score;
  }

  public int addScore(int add) {
    return this.score += add;
  }

  public int setScore(int score) {
    return this.score = score;
  }

  public String getType() {
    return this.type;
  }

  public String setType(String type) {
    return this.type = type;
  }

  public int compareTo(Score<K> s) {
    return score > s.getScore() ? 1 : (score < s.getScore() ? -1 : 0);
  }

  @Override
  public String toString() {
    return type + ": " + key.toString() + " = " + score;
  }
}
