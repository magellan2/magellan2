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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Properties;

import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import magellan.client.swing.MessagePanel;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.client.utils.SwingUtils;
import magellan.library.utils.PropertiesHelper;
import magellan.library.utils.Resources;

/**
 * The preferences dialog for the message panel.
 * 
 * @author ...
 * @version 1.0, 15.11.2007
 */
public class ClientMessagePreferences extends AbstractPreferencesAdapter implements
    PreferencesAdapter {
  protected MessagePanel src;
  protected JCheckBox lineWrap;

  JPanel[] panelColors;
  private String[] colorProperties;
  private String[] colorKeys;
  private Properties settings;

  /**
   * Creates a new Pref object.
   * 
   * @param settings
   */
  public ClientMessagePreferences(MessagePanel src, Properties settings) {
    this.src = src;
    this.settings = settings;

    JPanel help =
        addPanel(Resources.get("messagepanel.prefs.border.title"), new FlowLayout(
            FlowLayout.LEADING));

    lineWrap = new JCheckBox(Resources.get("messagepanel.prefs.linewrap"), src.isLineWrap());
    help.add(lineWrap);

    colorProperties = new String[] {
        PropertiesHelper.MESSAGETYPE_SECTION_EVENTS_COLOR,
        PropertiesHelper.MESSAGETYPE_SECTION_MOVEMENTS_COLOR,
        PropertiesHelper.MESSAGETYPE_SECTION_ECONOMY_COLOR,
        PropertiesHelper.MESSAGETYPE_SECTION_MAGIC_COLOR,
        PropertiesHelper.MESSAGETYPE_SECTION_STUDY_COLOR,
        PropertiesHelper.MESSAGETYPE_SECTION_PRODUCTION_COLOR,
        PropertiesHelper.MESSAGETYPE_SECTION_ERRORS_COLOR,
        PropertiesHelper.MESSAGETYPE_SECTION_BATTLE_COLOR,
        PropertiesHelper.MESSAGETYPE_SECTION_UNKNOWN_COLOR
    };
    colorKeys = new String[] {
        "clientpreferences.messagecolors.events",
        "clientpreferences.messagecolors.movements",
        "clientpreferences.messagecolors.economy",
        "clientpreferences.messagecolors.magic",
        "clientpreferences.messagecolors.study",
        "clientpreferences.messagecolors.production",
        "clientpreferences.messagecolors.errors",
        "clientpreferences.messagecolors.battle",
        "clientpreferences.messagecolors.unknown" };
    setPanelColors();
  }

  private void setPanelColors() {
    Dimension prefDim = SwingUtils.getDimension(1.5, 1.5, true);
    GridBagConstraints c = new GridBagConstraints();
    JPanel colors =
        addPanel(Resources.get("clientpreferences.border.messagecolors"), new GridBagLayout());
    panelColors = new JPanel[colorProperties.length];

    c.anchor = GridBagConstraints.NORTHWEST;
    c.weightx = 1.0;

    for (int i = 0; i < colorProperties.length; ++i) {
      JLabel label = new JLabel(Resources.get(colorKeys[i]));
      c.gridy = i;
      c.gridwidth = GridBagConstraints.RELATIVE;
      c.fill = GridBagConstraints.HORIZONTAL;
      colors.add(label, c);

      panelColors[i] = new JPanel();
      panelColors[i].setBorder(new LineBorder(Color.black));
      panelColors[i].setPreferredSize(prefDim);
      panelColors[i].setBackground(PropertiesHelper.getColor(settings, colorProperties[i], Color.BLACK));
      panelColors[i].addMouseListener(new ColorPanelMouseAdapter());
      c.fill = GridBagConstraints.NONE;
      colors.add(panelColors[i], c);
    }
  }

  /**
   * @see magellan.client.swing.preferences.PreferencesAdapter#initPreferences()
   */
  public void initPreferences() {
    lineWrap.setSelected(src.isLineWrap());

    for (int i = 0; i < colorProperties.length; ++i) {
      panelColors[i].setBackground(PropertiesHelper.getColor(settings, colorProperties[i], Color.BLACK));
      PropertiesHelper.setColor(settings, colorProperties[i], panelColors[i].getBackground());
    }
  }

  /**
   * 
   */
  public void applyPreferences() {
    src.setLineWrap(lineWrap.isSelected());

    for (int i = 0; i < colorProperties.length; ++i) {
      PropertiesHelper.setColor(settings, colorProperties[i], panelColors[i].getBackground());
    }
    src.setPreferencesChanged();
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

  static class ColorPanelMouseAdapter extends MouseAdapter {
    @Override
    public void mousePressed(MouseEvent e) {
      Color newColor =
          JColorChooser.showDialog(((JComponent) e.getSource()).getTopLevelAncestor(), Resources
              .get("clientpreferences.messagecolors.dialog.title"), ((Component) e.getSource())
                  .getBackground());

      if (newColor != null) {
        ((Component) e.getSource()).setBackground(newColor);
      }
    }
  }
}
