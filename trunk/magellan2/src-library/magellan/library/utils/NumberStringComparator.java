// class magellan.library.utils.NumberStringComparator
// created on Nov 2, 2012
//
// Copyright 2003-2012 by magellan project team
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

import java.util.Comparator;

/**
 * A comparator that sorts strings that may represent numbers. If both string represent numbers,
 * they are compared numerically. A string and a number are compared as if the number was smaller.
 * Two strings are compared alphabetically.
 */
public class NumberStringComparator implements Comparator<String> {

  private static Comparator<? super String> instance = new NumberStringComparator();

  private NumberStringComparator() {
  }

  /**
   * @see Comparator#compare(Object, Object)
   */
  public int compare(String o1, String o2) {
    double n1, n2;
    try {
      n1 = Double.parseDouble(o1);
    } catch (NumberFormatException e) {
      n1 = Double.NaN;
    }
    try {
      n2 = Double.parseDouble(o2);
    } catch (NumberFormatException e) {
      n2 = Double.NaN;
    }
    if (n1 != Double.NaN) {
      if (n2 != Double.NaN)
        return Double.compare(n1, n2);
      else
        return -1;
    } else if (n2 != Double.NaN)
      return 1;
    else
      return o1.compareTo(o2);

  }

  /**
   * Returns a singleton instance.
   */
  public static Comparator<? super String> getInstance() {
    return instance;
  }

}
