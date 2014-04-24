// class magellan.library.Bookmark
// created on Apr 22, 2014
//
// Copyright 2003-2014 by magellan project team
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

public interface Bookmark {

  public enum BookmarkType {
    REGION, UNIT, BUILDING, ISLAND, SHIP, SPELL, UNKNOWN;

    public static BookmarkType get(Selectable object) {
      Class<?> clazz = object.getClass();
      BookmarkType type = null;
      try {
        type = BookmarkType.valueOf(clazz.getSimpleName().toUpperCase());
      } catch (IllegalArgumentException ex) {
        for (Class<?> iface : clazz.getInterfaces()) {
          try {
            type = BookmarkType.valueOf(iface.getSimpleName().toUpperCase());
            return type;
          } catch (IllegalArgumentException ex2) {
            // CONTINUE
          }
        }
      }
      return type == null ? UNKNOWN : type;
    }
  }

  String getName();

  Selectable getObject();

  BookmarkType getType();

}
