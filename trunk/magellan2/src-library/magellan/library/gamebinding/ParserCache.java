// class magellan.library.gamebinding.ParserCache
// created on Nov 12, 2017
//
// Copyright 2003-2017 by magellan project team
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
package magellan.library.gamebinding;

import magellan.library.GameData;
import magellan.library.completion.Completer;

/**
 * Caches an OrderParser based on data and completer.
 */
public class ParserCache<T> {

  /**
   * Creates an OderParser
   */
  public interface Factory<T> {

    T create();

  }

  private GameData cachedData;
  private T nullParser;
  private Completer cachedCompleter;
  private T completerParser;

  public T getOrderParser(GameData data, Completer completer, Factory<T> factory) {
    if (completer == null) {
      if (cachedData != data || nullParser == null) {
        nullParser = factory.create();
        cachedData = data;
      }
      return nullParser;
    } else {
      if (cachedCompleter != completer || cachedData != data || completerParser == null) {
        completerParser = factory.create();
        cachedCompleter = completer;
        cachedData = data;
      }
      return completerParser;
    }
  }

}
