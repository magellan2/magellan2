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

  public SimpleDate(String month, String year) {
    super(1);

    int m = 0;
    for (m = 0; m < months.length; ++m)
      if (months[m].equals(month)) {
        break;
      }
    setDate((Integer.parseInt(year) - 1) * 12 + m);
  }

  @Override
  public int getSeason() {
    // HIGHTODO Automatisch generierte Methode implementieren
    return 0;
  }

  @Override
  public String toString(int iDateType) {
    // HIGHTODO Automatisch generierte Methode implementieren
    return null;
  }

}
