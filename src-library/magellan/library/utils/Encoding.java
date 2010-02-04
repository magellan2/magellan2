// class magellan.library.utils.Encoding
// created on 13.08.2007
//
// Copyright 2003-2007 by magellan project team
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
 * This is a wrapper for all possible encodings in the system.
 * 
 * @author Thoralf Rickert
 * @version 1.0, 13.08.2007
 */
public enum Encoding {
  /** Default Encoding */
  DEFAULT("UTF-8"),
  /** UTF-8 Encoding */
  UTF8("UTF-8"),
  /** ISO-8859 Encoding */
  ISO("ISO-8859-1");

  private String encoding;

  private Encoding(String encoding) {
    this.encoding = encoding;
  }

  /**
   * @see java.lang.Enum#toString()
   */
  @Override
  public String toString() {
    return encoding;
  }

  /**
   * Tries to find the corresponding Encoding enum to it's name representation.
   */
  public static Encoding getEncoding(String encoding) {
    if (encoding == null)
      return null;
    Encoding[] values = Encoding.values();
    for (Encoding e : values) {
      if (e.toString().equalsIgnoreCase(encoding))
        return e;
    }
    return null;
  }
}
