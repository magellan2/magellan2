// class magellan.library.gamebinding.EresseaMessageRenderer
// created on 28.11.2007
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
package magellan.library.gamebinding.e4;

import magellan.library.GameData;
import magellan.library.gamebinding.e3a.E3AMessageRenderer;

/**
 * A Renderer for Eressea Messages Messages in Eressea look like this: MESSAGE 350568592
 * 5281483;type "Whio (whio) übergibt 10 Silber an Darin Jerekop (djer).";rendered 1515696;unit
 * 10;amount "Silber";resource 631683;target The rendered tag can be rendered by this Renderer using
 * the Message, the Messagetype and Translations Messagetypes look like this: MESSAGETYPE 5281483
 * "\"$unit($unit) übergibt $int($amount) $resource($resource,$amount) an $unit($target).\"";text
 * "economy";section
 * 
 * @author ...
 * @version 1.0, 28.11.2007
 */
public class E4MessageRenderer extends E3AMessageRenderer {

  /**
   * @param gd
   */
  public E4MessageRenderer(GameData gd) {
    super(gd);
  }

}
