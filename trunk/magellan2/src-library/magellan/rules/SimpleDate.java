// class magellan.rules.SimpleDate
// created on Apr 15, 2013
//
// Copyright 2003-2013 by magellan project team
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
package magellan.rules;

import magellan.library.rules.Date;

public class SimpleDate extends Date {
  private static final String months[] = { "January", "February", "March", "April", "May", "June",
      "July", "August", "September", "October", "November", "December" };
  private int month;
  private int year;

  public SimpleDate(String aMonth, String year) {
    super(1);

    month = 0;
    for (month = 0; month < months.length; ++month)
      if (months[month].equals(aMonth)) {
        break;
      }
    try {
      this.year = Integer.parseInt(year);
    } catch (NumberFormatException e) {
      this.year = -1;
    }

    setDate((Integer.parseInt(year) - 1) * 12 + month);
  }

  @Override
  public int getSeason() {
    // HIGHTODO Automatisch generierte Methode implementieren
    return 0;
  }

  @Override
  public String toString(int iDateType) {
    return months[month] + ", " + year;
  }

}
