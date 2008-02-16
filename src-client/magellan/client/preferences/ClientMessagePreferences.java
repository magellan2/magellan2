// class magellan.client.preferences.MessagePreferences
// created on 15.02.2008
//
// Copyright 2003-2008 by magellan project team
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
package magellan.client.preferences;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import magellan.client.swing.MessagePanel;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.library.utils.Resources;


/**
 * The preferences dialog for the message panel.
 *
 * @author ...
 * @version 1.0, 15.11.2007
 */
public class ClientMessagePreferences extends JPanel implements PreferencesAdapter {
  protected MessagePanel src;
  protected JCheckBox lineWrap;

  /**
   * Creates a new Pref object.
   */
  public ClientMessagePreferences(MessagePanel src) {
    super(new GridBagLayout());
    this.src = src;

    JPanel help = new JPanel(new FlowLayout(FlowLayout.LEADING));
    help.setBorder(BorderFactory.createTitledBorder(Resources.get("messagepanel.prefs.border.title")));

    lineWrap = new JCheckBox(Resources.get("messagepanel.prefs.linewrap"), src.isLineWrap());
    help.add(lineWrap);

    GridBagConstraints c = new GridBagConstraints(0, 0, 1, 1, 1, 1,
                            GridBagConstraints.WEST,
                            GridBagConstraints.BOTH,
                            new Insets(1, 1, 1, 1), 0, 0);
    this.add(new JPanel(), c);
    c.gridy = 2;
    this.add(new JPanel(), c);

    c.gridy = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    this.add(help, c);
  }
     
  /**
   * @see magellan.client.swing.preferences.PreferencesAdapter#initPreferences()
   */
  public void initPreferences() {
    // TODO implement MessagePanel preference initializer
  }

  /**
   * 
   */
  public void applyPreferences() {
    src.setLineWrap(lineWrap.isSelected());
  }

  /**
   * 
   */
  public Component getComponent() {
    return this;
  }

  /**
   * 
   */
  public String getTitle() {
    return Resources.get("messagepanel.prefs.title");
  }
}
