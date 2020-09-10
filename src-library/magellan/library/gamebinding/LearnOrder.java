// class magellan.library.gamebinding.LearnOrder
// created on Nov 13, 2010
//
// Copyright 2003-2010 by magellan project team
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

import java.util.List;

import magellan.library.utils.OrderToken;

/**
 * A LERNE order.
 */
public class LearnOrder extends SimpleOrder {

  /**
   * The name of the learned skill.
   */
  public String skillName;
  private boolean auto;

  /**
   * @param tokens
   * @param text
   */
  public LearnOrder(List<OrderToken> tokens, String text) {
    super(tokens, text);
  }

  /**
   * Set for LERNE AUTO
   */
  public void setAuto(boolean auto) {
    this.auto = auto;
  }

  /**
   * Returns true if this is a LERNE AUTO order
   */
  public boolean isAuto() {
    return auto;
  }
}
