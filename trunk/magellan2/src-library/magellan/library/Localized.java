// class magellan.library.Localized
// created on 20.11.2007
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
package magellan.library;

import java.util.Locale;

/**
 * This interface should guarantee that the object can be localized. Furthermore it should make sure
 * that relocalized objects invalidate locale dependent information.
 * 
 * @author ...
 * @version 1.0, 20.11.2007
 */
public interface Localized {

  /**
   * Sets the current locale of the object and should invalidate/reset all locale dependent content
   * of the object if the locale changes.
   * 
   * @param locale
   */
  public void setLocale(Locale locale);

  /**
   * Return the current locale i.e. for comparisions
   * 
   * @return locale
   */
  public Locale getLocale();

}
