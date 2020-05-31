// class magellan.client.preferences.MapPreferences
// created on 16.02.2008
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

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import magellan.client.swing.MapperPanel;
import magellan.client.swing.preferences.ExtendedPreferencesAdapter;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.library.utils.PropertiesHelper;
import magellan.library.utils.Resources;

/**
 * Preferences adapter for Mapper and Minimapper.
 * 
 * @author ...
 * @version 1.0, Oct 5, 2012
 */
public class MapPreferences extends AbstractPreferencesAdapter implements
    ExtendedPreferencesAdapter {

  // The source component to configure
  private MapperPanel source = null;

  // GUI elements
  private PreferencesAdapter prefMapper = null;
  private List<PreferencesAdapter> subAdapter;

  private JCheckBox showNavigation;
  private JCheckBox useSeasonImages;

  /**
   * Creates a new MapperPanelPreferences object.
   * 
   * @param m DOCUMENT-ME
   */
  public MapPreferences(MapperPanel m) {
    source = m;
    prefMapper = source.getMapper().getPreferencesAdapter();

    subAdapter = new ArrayList<PreferencesAdapter>(2);
    subAdapter.add(new MinimapPreferences(source));
  }

  /**
   * DOCUMENT-ME
   */
  public List<PreferencesAdapter> getChildren() {
    return subAdapter;
  }

  /**
   * @see magellan.client.swing.preferences.PreferencesAdapter#getComponent()
   */
  public Component getComponent() {
    JPanel erg =
        addPanel(Resources.get("map.mapperpanelpreferences.border.caption"), new BorderLayout());

    showNavigation =
        new JCheckBox(Resources.get("mapperpanel.prefs.details.chk.shownavigation"), source
            .getContext().getProperties()
            .getProperty("MapperPannel.Details.showNavigation", "true").equals("true"));
    useSeasonImages =
        new JCheckBox(Resources.get("map.bordercellrenderer.useseasonimages"), source.getContext()
            .getProperties().getProperty(PropertiesHelper.BORDERCELLRENDERER_USE_SEASON_IMAGES,
                "true").equals("true"));
    erg.add(showNavigation, BorderLayout.WEST);
    erg.add(useSeasonImages, BorderLayout.SOUTH);

    addComponent(prefMapper.getComponent());
    return this;
  }

  public void initPreferences() {
    prefMapper.initPreferences();
  }

  /**
   * @see magellan.client.swing.preferences.PreferencesAdapter#applyPreferences()
   */
  public void applyPreferences() {
    prefMapper.applyPreferences();

    source.setShowNavigation(showNavigation.isSelected());
    source.setUseSeasonImages(useSeasonImages.isSelected());

    source.getMapper().repaint(100);

  }

  /**
   * @see magellan.client.swing.preferences.PreferencesAdapter#getTitle()
   */
  public String getTitle() {
    return Resources.get("mapperpanel.prefs.title");
  }
}
