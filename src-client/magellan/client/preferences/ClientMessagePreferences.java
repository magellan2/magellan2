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
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Properties;

import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import magellan.client.Client;
import magellan.client.swing.MessagePanel;
import magellan.client.swing.layout.GridBagHelper;
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

  protected JPanel panelColorEvents = null;
  protected JPanel panelColorBattle = null;
  protected JPanel panelColorErrors = null;
  protected JPanel panelColorProduction = null;
  protected JPanel panelColorStudy = null;
  protected JPanel panelColorMagic = null;
  protected JPanel panelColorEconomy = null;
  protected JPanel panelColorMovements = null;

  /**
   * Creates a new Pref object.
   */
  public ClientMessagePreferences(MessagePanel src) {
    Dimension prefDim = SwingUtils.getDimension(1.5, 1.5, true);
    JPanel help =
        addPanel(Resources.get("messagepanel.prefs.border.title"), new FlowLayout(
            FlowLayout.LEADING));
    this.src = src;

    lineWrap = new JCheckBox(Resources.get("messagepanel.prefs.linewrap"), src.isLineWrap());
    help.add(lineWrap);

    JPanel colors =
        addPanel(Resources.get("clientpreferences.border.messagecolors"), new GridBagLayout());

    Properties settings = Client.INSTANCE.getProperties();

    JLabel label = new JLabel(Resources.get("clientpreferences.messagecolors.events"));
    GridBagConstraints c =
        new GridBagConstraints(0, 0, GridBagConstraints.RELATIVE, 1, 1.0, 1.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(3, 3, 3, 3), 0,
            0);

    GridBagHelper.setConstraints(c, 0, 0, GridBagConstraints.RELATIVE, 1, 1.0, 1.0,
        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
    colors.add(label, c);

    panelColorEvents = new JPanel();
    panelColorEvents.setBorder(new LineBorder(Color.black));
    panelColorEvents.setPreferredSize(prefDim);
    panelColorEvents.setBackground(PropertiesHelper.getColor(settings,
        PropertiesHelper.MESSAGETYPE_SECTION_EVENTS_COLOR, Color.BLACK));
    panelColorEvents.addMouseListener(new ColorPanelMouseAdapter());
    GridBagHelper.setConstraints(c, 1, 0, GridBagConstraints.REMAINDER, 1, 1.0, 1.0,
        GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, c.insets, 0, 0);
    colors.add(panelColorEvents, c);

    label = new JLabel(Resources.get("clientpreferences.messagecolors.movements"));
    GridBagHelper.setConstraints(c, 0, 1, GridBagConstraints.RELATIVE, 1, 1.0, 1.0,
        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
    colors.add(label, c);

    panelColorMovements = new JPanel();
    panelColorMovements.setBorder(new LineBorder(Color.black));
    panelColorMovements.setPreferredSize(prefDim);
    panelColorMovements.setBackground(PropertiesHelper.getColor(settings,
        PropertiesHelper.MESSAGETYPE_SECTION_MOVEMENTS_COLOR, Color.BLACK));
    panelColorMovements.addMouseListener(new ColorPanelMouseAdapter());
    GridBagHelper.setConstraints(c, 1, 1, GridBagConstraints.REMAINDER, 1, 1.0, 1.0,
        GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, c.insets, 0, 0);
    colors.add(panelColorMovements, c);

    label = new JLabel(Resources.get("clientpreferences.messagecolors.economy"));
    GridBagHelper.setConstraints(c, 0, 2, GridBagConstraints.RELATIVE, 1, 1.0, 1.0,
        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
    colors.add(label, c);

    panelColorEconomy = new JPanel();
    panelColorEconomy.setBorder(new LineBorder(Color.black));
    panelColorEconomy.setPreferredSize(prefDim);
    panelColorEconomy.setBackground(PropertiesHelper.getColor(settings,
        PropertiesHelper.MESSAGETYPE_SECTION_ECONOMY_COLOR, Color.BLACK));
    panelColorEconomy.addMouseListener(new ColorPanelMouseAdapter());
    GridBagHelper.setConstraints(c, 1, 2, GridBagConstraints.REMAINDER, 1, 1.0, 1.0,
        GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, c.insets, 0, 0);
    colors.add(panelColorEconomy, c);

    label = new JLabel(Resources.get("clientpreferences.messagecolors.magic"));
    GridBagHelper.setConstraints(c, 0, 3, GridBagConstraints.RELATIVE, 1, 1.0, 1.0,
        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
    colors.add(label, c);

    panelColorMagic = new JPanel();
    panelColorMagic.setBorder(new LineBorder(Color.black));
    panelColorMagic.setPreferredSize(prefDim);
    panelColorMagic.setBackground(PropertiesHelper.getColor(settings,
        PropertiesHelper.MESSAGETYPE_SECTION_MAGIC_COLOR, Color.BLACK));
    panelColorMagic.addMouseListener(new ColorPanelMouseAdapter());
    GridBagHelper.setConstraints(c, 1, 3, GridBagConstraints.REMAINDER, 1, 1.0, 1.0,
        GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, c.insets, 0, 0);
    colors.add(panelColorMagic, c);

    label = new JLabel(Resources.get("clientpreferences.messagecolors.study"));
    GridBagHelper.setConstraints(c, 0, 4, GridBagConstraints.RELATIVE, 1, 1.0, 1.0,
        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
    colors.add(label, c);

    panelColorStudy = new JPanel();
    panelColorStudy.setBorder(new LineBorder(Color.black));
    panelColorStudy.setPreferredSize(prefDim);
    panelColorStudy.setBackground(PropertiesHelper.getColor(settings,
        PropertiesHelper.MESSAGETYPE_SECTION_STUDY_COLOR, Color.BLACK));
    panelColorStudy.addMouseListener(new ColorPanelMouseAdapter());
    GridBagHelper.setConstraints(c, 1, 4, GridBagConstraints.REMAINDER, 1, 1.0, 1.0,
        GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, c.insets, 0, 0);
    colors.add(panelColorStudy, c);

    label = new JLabel(Resources.get("clientpreferences.messagecolors.production"));
    GridBagHelper.setConstraints(c, 0, 5, GridBagConstraints.RELATIVE, 1, 1.0, 1.0,
        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
    colors.add(label, c);

    panelColorProduction = new JPanel();
    panelColorProduction.setBorder(new LineBorder(Color.black));
    panelColorProduction.setPreferredSize(prefDim);
    panelColorProduction.setBackground(PropertiesHelper.getColor(settings,
        PropertiesHelper.MESSAGETYPE_SECTION_PRODUCTION_COLOR, Color.BLACK));
    panelColorProduction.addMouseListener(new ColorPanelMouseAdapter());
    GridBagHelper.setConstraints(c, 1, 5, GridBagConstraints.REMAINDER, 1, 1.0, 1.0,
        GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, c.insets, 0, 0);
    colors.add(panelColorProduction, c);

    label = new JLabel(Resources.get("clientpreferences.messagecolors.errors"));
    GridBagHelper.setConstraints(c, 0, 6, GridBagConstraints.RELATIVE, 1, 1.0, 1.0,
        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
    colors.add(label, c);

    panelColorErrors = new JPanel();
    panelColorErrors.setBorder(new LineBorder(Color.black));
    panelColorErrors.setPreferredSize(prefDim);
    panelColorErrors.setBackground(PropertiesHelper.getColor(settings,
        PropertiesHelper.MESSAGETYPE_SECTION_ERRORS_COLOR, Color.BLACK));
    panelColorErrors.addMouseListener(new ColorPanelMouseAdapter());
    GridBagHelper.setConstraints(c, 1, 6, GridBagConstraints.REMAINDER, 1, 1.0, 1.0,
        GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, c.insets, 0, 0);
    colors.add(panelColorErrors, c);

    label = new JLabel(Resources.get("clientpreferences.messagecolors.battle"));
    GridBagHelper.setConstraints(c, 0, 7, GridBagConstraints.RELATIVE, 1, 1.0, 1.0,
        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, c.insets, 0, 0);
    colors.add(label, c);

    panelColorBattle = new JPanel();
    panelColorBattle.setBorder(new LineBorder(Color.black));
    panelColorBattle.setPreferredSize(prefDim);
    panelColorBattle.setBackground(PropertiesHelper.getColor(settings,
        PropertiesHelper.MESSAGETYPE_SECTION_BATTLE_COLOR, Color.BLACK));
    panelColorBattle.addMouseListener(new ColorPanelMouseAdapter());
    GridBagHelper.setConstraints(c, 1, 7, GridBagConstraints.REMAINDER, 1, 1.0, 1.0,
        GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, c.insets, 0, 0);
    colors.add(panelColorBattle, c);

  }

  /**
   * @see magellan.client.swing.preferences.PreferencesAdapter#initPreferences()
   */
  public void initPreferences() {
  }

  /**
   * 
   */
  public void applyPreferences() {
    src.setLineWrap(lineWrap.isSelected());

    Properties settings = Client.INSTANCE.getProperties();

    PropertiesHelper.setColor(settings, PropertiesHelper.MESSAGETYPE_SECTION_EVENTS_COLOR,
        panelColorEvents.getBackground());
    PropertiesHelper.setColor(settings, PropertiesHelper.MESSAGETYPE_SECTION_BATTLE_COLOR,
        panelColorBattle.getBackground());
    PropertiesHelper.setColor(settings, PropertiesHelper.MESSAGETYPE_SECTION_ERRORS_COLOR,
        panelColorErrors.getBackground());
    PropertiesHelper.setColor(settings, PropertiesHelper.MESSAGETYPE_SECTION_PRODUCTION_COLOR,
        panelColorProduction.getBackground());
    PropertiesHelper.setColor(settings, PropertiesHelper.MESSAGETYPE_SECTION_STUDY_COLOR,
        panelColorStudy.getBackground());
    PropertiesHelper.setColor(settings, PropertiesHelper.MESSAGETYPE_SECTION_MAGIC_COLOR,
        panelColorMagic.getBackground());
    PropertiesHelper.setColor(settings, PropertiesHelper.MESSAGETYPE_SECTION_ECONOMY_COLOR,
        panelColorEconomy.getBackground());
    PropertiesHelper.setColor(settings, PropertiesHelper.MESSAGETYPE_SECTION_MOVEMENTS_COLOR,
        panelColorMovements.getBackground());
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
