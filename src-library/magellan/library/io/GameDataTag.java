// class magellan.library.io.GameDataTag
// created on Sep 25, 2009
//
// Copyright 2003-2009 by magellan project team
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
package magellan.library.io;

/**
 * An enum for all tags that appear in CR files.
 * 
 * @author stm
 * @version 1.0, Sep 25, 2009
 */
public enum GameDataTag {

  ERESSEA(true, null, "ERESSEA", TagType.integer), VERSION(true, null, "VERSION", TagType.integer),
  VERSION_charset(false, VERSION, "charset", TagType.string), VERSION_locale(false, VERSION,
      "locale", TagType.string), VERSION_noskillpoints(false, VERSION, "noskillpoints",
      TagType.integer), VERSION_date(false, VERSION, "date", TagType.integer), VERSION_Spiel(false,
      VERSION, "Spiel", TagType.string), VERSION_Konfiguration(false, VERSION, "Konfiguration",
      TagType.string), VERSION_Koordinaten(false, VERSION, "Koordinaten", TagType.string),
  VERSION_Basis(false, VERSION, "Basis", TagType.integer), VERSION_Runde(false, VERSION, "Runde",
      TagType.integer), VERSION_Zeitalter(false, VERSION, "Zeitalter", TagType.integer),
  VERSION_mailto(false, VERSION, "mailto", TagType.string), VERSION_mailcmd(false, VERSION,
      "mailcmd", TagType.string), PARTEI(true, VERSION, "PARTEI", TagType.id), PARTEI_locale(false,
      PARTEI, "locale", TagType.string), PARTEI_age(false, PARTEI, "age", TagType.integer);

  private boolean block;
  private GameDataTag parent;
  private String tag;
  private TagType type;

  GameDataTag(boolean block, GameDataTag parent, String tag, TagType type) {
    this.block = block;
    this.parent = parent;
    this.tag = tag;
    this.type = type;
  }

  @Override
  public String toString() {
    return (block ? "block " : "tag ") + tag
        + (parent != null ? (" sub " + parent.toString()) : "");
  }
}
