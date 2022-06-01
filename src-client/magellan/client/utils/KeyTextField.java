// class magellan.client.preferences.KeyTextField
// created on Mar 2, 2022
//
// Copyright 2003-2022 by magellan project team
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
package magellan.client.utils;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JTextField;
import javax.swing.KeyStroke;

/**
 * A textfield that manages KeyStrokes.
 *
 */
public class KeyTextField extends JTextField {
  public static final KeyStroke UNDEFINED = KeyStroke.getKeyStroke(KeyEvent.VK_UNDEFINED, KeyEvent.VK_UNDEFINED);

  protected KeyStroke keyStroke = UNDEFINED;
  private boolean ignoreEnter;

  /**
   * Creates a new KeyTextField object.
   */
  public KeyTextField(int columns) {
    super(columns);
    addKeyListener(new KTListener());
  }

  /**
   * Returns the KeyStroke represented by the content.
   */
  public KeyStroke getKeyStroke() {
    if (verify())
      return keyStroke;
    else
      return UNDEFINED;
  }

  /**
   * Sets the represented and displayed value to stroke.
   * 
   * @param stroke If null, sets the value to {@link #UNDEFINED}.
   */
  public void init(KeyStroke stroke) {
    if (stroke == null) {
      keyStroke = UNDEFINED;
    } else {
      keyStroke = stroke;
    }
    setText(stroke);
  }

  /**
   * @param stroke
   */
  protected void setText(KeyStroke stroke) {
    setText(SwingUtils.getKeyStroke(stroke));
  }

  /**
   * To allow "tab" as a key.
   */
  @Override
  public boolean isManagingFocus() {
    return true;
  }

  class KTListener implements KeyListener {
    /**
     * 
     */
    public void keyPressed(KeyEvent p1) {
      KeyStroke old = keyStroke;
      keyStroke = KeyStroke.getKeyStrokeForEvent(p1);
      if (!verifyEnter()) {
        // ignore ENTER
        keyStroke = old;
        return;
      }
      setText(keyStroke);
      p1.consume();
    }

    /**
    * 
    */
    public void keyReleased(KeyEvent p1) {
      // delete any input if there's no "stable"(non-modifying) key
      if (!verify()) {
        keyStroke = UNDEFINED;
        setText(keyStroke);
      }
    }

    /**
     * 
     */
    public void keyTyped(KeyEvent p1) {
      p1.consume();
    }
  }

  /**
   * If set to true, this text field does not accept ENTER and ESCAPE as inputs.
   * 
   * @param ignoreEnter
   */
  public void setIgnoreEnter(boolean ignoreEnter) {
    this.ignoreEnter = ignoreEnter;
  }

  /**
   * If true, this text field does not accept ENTER and ESCAPE as inputs.
   */
  public boolean isIgnoreEnter() {
    return ignoreEnter;
  }

  /**
   * 
   */
  protected boolean verifyEnter() {
    return !ignoreEnter || (keyStroke.getModifiers() != 0 ||
        (keyStroke.getKeyCode() != KeyEvent.VK_ENTER && keyStroke.getKeyCode() != KeyEvent.VK_ESCAPE));
  }

  /**
   * keyStroke can temporarily contain values that do not pass verify(). {@link #getKeyStroke()} will, however, never
   * return values that do not verify (except UNDEFINED). The standard application does not accept modifiers and
   * respects verifyEnter.
   */
  protected boolean verify() {
    return keyStroke != UNDEFINED && verifyEnter() && !SwingUtils.isModifier(keyStroke.getKeyCode());
  }

}