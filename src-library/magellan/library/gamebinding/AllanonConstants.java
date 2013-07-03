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
  /** order ANWERBEN */
  public static final String OS_ANWERBEN = "ANWERBEN";
  /** Order constant ANWERBEN */
  public static final StringID OC_ANWERBEN = StringID.create("ANWERBEN");
  /** order BEANSPRUCHE */
  public static final String OS_BEANSPRUCHE = "BEANSPRUCHE";
  /** Order constant BEANSPRUCHE */
  public static final StringID OC_BEANSPRUCHE = StringID.create("BEANSPRUCHE");
  /** order ERKUNDEN */
  public static final String OS_ERKUNDEN = "ERKUNDEN";
  /** Order constant ERKUNDEN */
  public static final StringID OC_ERKUNDEN = StringID.create("ERKUNDEN");
  /** order KARAWANE */
  public static final String OS_KARAWANE = "KARAWANE";
  /** Order constant KARAWANE */
  public static final StringID OC_KARAWANE = StringID.create("KARAWANE");
  /** order MEUCHELN */
  public static final String OS_MEUCHELN = "MEUCHELN";
  /** Order constant MEUCHELN */
  public static final StringID OC_MEUCHELN = StringID.create("MEUCHELN");

  /** skill ALCHIMIE */
  public static final StringID S_ALCHIMIE = StringID.create("ALCHIMIE");
  /** skill ANWERBEN */
  public static final StringID S_ANWERBEN = StringID.create("ANWERBEN");
  /** skill BEANSPRUCHE */
  public static final StringID S_BEANSPRUCHE = StringID.create("BEANSPRUCHE");
  /** skill MEUCHELN */
  public static final StringID S_MEUCHELN = StringID.create("MEUCHELN");
  /** skill MECHANIK */
  public static final StringID S_MECHANIK = StringID.create("MECHANIK");

  public static final ID ST_KARAWANE = StringID.create("KARAWANE");
}
