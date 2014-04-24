// class magellan.library.impl.MagellanBookmarkBuilder
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
package magellan.library.impl;

import magellan.library.Bookmark;
import magellan.library.Bookmark.BookmarkType;
import magellan.library.BookmarkBuilder;
import magellan.library.ID;
import magellan.library.IntegerID;
import magellan.library.Selectable;
import magellan.library.utils.logging.Logger;

public class MagellanBookmarkBuilder implements BookmarkBuilder {
  private static Logger log = Logger.getInstance(MagellanBookmarkBuilder.class);

  // protected static Map<Class<?>, String> types = new HashMap<Class<?>, String>();
  //
  // static {
  // types.put(Region.class, "region");
  // types.put(Unit.class, "unit");
  // }

  public static class BookmarkImpl implements Bookmark {

    private static final Selectable NO_OBJECT = new Selectable() {

      public ID getID() {
        return IntegerID.create(0);
      }

      public int compareTo(Object o) {
        if (o == NO_OBJECT)
          return 0;
        else
          return -1;
      }

      public void setName(String name) {
        // NOP
      }

      public String getName() {
        return "---";
      }

      public String getModifiedName() {
        return "---";
      }

      @Override
      public Object clone() throws CloneNotSupportedException {
        return NO_OBJECT;
      }
    };

    private BookmarkType type;

    public BookmarkImpl(BookmarkType type) {
      if (type == null)
        throw new NullPointerException();
      this.type = type;
      object = NO_OBJECT;
    }

    public BookmarkImpl(Selectable object, String name) {
      if (object == null)
        throw new NullPointerException();
      type = BookmarkType.get(object);
      this.object = object;
      if (name != null) {
        this.name = name;
      }
    }

    private Selectable object;
    public String name;

    public String getName() {
      return name;
    }

    public Selectable getObject() {
      return object;
    }

    @Override
    public String toString() {
      return name != null ? name : object.toString();
    }

    public BookmarkType getType() {
      return type;
    }

  }

  BookmarkImpl bookmark = new BookmarkImpl(BookmarkType.UNKNOWN);

  public void setObject(Selectable object) {
    bookmark.object = object;
    bookmark.type = BookmarkType.get(object);
  }

  public Bookmark getBookmark() {
    return bookmark;
  }

  public void setName(String name) {
    bookmark.name = name;
  }

}
