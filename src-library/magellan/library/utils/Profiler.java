// class magellan.library.utils.Profiler
// created on Jul 28, 2022
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

public class Profiler {

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