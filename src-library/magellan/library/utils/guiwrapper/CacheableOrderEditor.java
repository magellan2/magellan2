// class magellan.library.utils.CacheableOrderEditor
// created on 27.04.2007
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
package magellan.library.utils.guiwrapper;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.FocusListener;
import java.awt.event.KeyListener;

import javax.swing.border.Border;
import javax.swing.event.CaretListener;

/**
 * This is a wrapper for the OrderEditor that is part of the Client package.
 * 
 * @author ...
 * @version 1.0, 27.04.2007
 */
public interface CacheableOrderEditor {
  public void formatTokens();

  public void setUseSyntaxHighlighting(boolean bool);

  public void setTokenColor(String styleName, Color color);

  /**
   * Refresh the editor's contents.
   */
  public void reloadOrders();

  //
  // Swing specific methods
  //
  public void requestFocus();

  public void setBackground(Color activeBgColorConfirmed);

  public boolean hasFocus();

  public void setBorder(Border border);

  public void setKeepVisible(boolean b);

  public boolean isModified();

  public void removeCaretListener(CaretListener caretAdapter);

  public void removeFocusListener(FocusListener focusAdapter);

  public void removeKeyListener(KeyListener keyAdapter);

  public void setFont(Font font);

  public void setCursor(Cursor cursor);

  public Rectangle getBounds();

  public void fireOrdersChangedEvent();
}
