// class magellan.library.gamebinding.AllanonConstants
// created on 26.02.2009
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
package magellan.library.gamebinding;

import magellan.library.ID;
import magellan.library.StringID;

/**
 * This class contains some Allanon specific constants.
 * 
 * @author Thoralf Rickert
 * @version 1.0, 26.02.2009
 */
public class AllanonConstants extends EresseaConstants {
  public static final String O_ANWERBEN = "ANWERBEN";
  public static final String O_BEANSPRUCHE = "BEANSPRUCHE";
  public static final String O_KARAWANE = "KARAWANE";
  public static final String O_MEUCHELN = "MEUCHELN";

  public static final ID S_ALCHIMIE = StringID.create("ALCHIMIE");
  public static final ID S_ANWERBEN = StringID.create("ANWERBEN");
  public static final ID S_BEANSPRUCHE = StringID.create("BEANSPRUCHE");
  public static final ID S_MEUCHELN = StringID.create("MEUCHELN");
  public static final ID S_MECHANIK = StringID.create("MECHANIK");

  public static final ID ST_KARAWANE = StringID.create("KARAWANE");
}
