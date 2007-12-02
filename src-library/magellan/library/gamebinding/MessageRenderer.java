// class magellan.library.gamebinding.MessageRenderer
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
package magellan.library.gamebinding;

import magellan.library.Message;

/**
 * An interface defining the render Method each MessageRenderer should have.
 * A Message Renderer should only be used to render ONE Message. This allows
 * to store parsing information in an implementing class.
 * A constructor should get all general background information. This would be 
 * generally a GameData object.  
 *
 * @author Ralf Duckstein
 * @version 1.0, 02.12.2007
 */
public interface MessageRenderer {
  
  /**
   * Renders a Message
   * 
   * @param msg The Message to render
   * 
   * @return the rendered String. This should be stored in the msg.text from 
   * the calling method.
   */
  public String renderMessage(Message msg);
}
