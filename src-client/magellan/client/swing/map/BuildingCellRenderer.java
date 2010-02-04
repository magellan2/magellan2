/*
 *  Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe,
 *                          Stefan Goetz, Sebastian Pappert,
 *                          Klaas Prause, Enno Rehling,
 *                          Sebastian Tusk, Ulrich Kuester,
 *                          Ilja Pavkovic
 *
 * This file is part of the Eressea Java Code Base, see the
 * file LICENSING for the licensing information applying to
 * this file.
 *
 */

package magellan.client.swing.map;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import magellan.client.Client;
import magellan.client.MagellanContext;
import magellan.client.swing.basics.SpringUtilities;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.library.Building;
import magellan.library.CoordinateID;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.rules.BuildingType;
import magellan.library.rules.UnitContainerType;
import magellan.library.utils.PropertiesHelper;
import magellan.library.utils.Resources;

/**
 * A renderer for Building objects.
 */
public class BuildingCellRenderer extends ImageCellRenderer {

  /**
   * Creates a new BuildingCellRenderer object.
   */
  public BuildingCellRenderer(CellGeometry geo, MagellanContext context) {
    super(geo, context);
  }

  /**
   * @see magellan.client.swing.map.HexCellRenderer#render(java.lang.Object, boolean, boolean)
   */
  @Override
  public void render(Object obj, boolean active, boolean selected) {
    if (obj instanceof Region) {
      Region region = (Region) obj;

      Iterator<Building> iter = region.buildings().iterator();

      if (iter.hasNext()) {
        CoordinateID coordinate = region.getCoordinate();
        Point pos = new Point(cellGeo.getImagePosition(coordinate.x, coordinate.y));
        pos.translate(-offset.x, -offset.y);

        Dimension size = cellGeo.getImageSize();

        while (iter.hasNext()) {
          Building b = iter.next();
          UnitContainerType type = b.getType();

          if (type != null && isEnabled(region, type)) {
            Image img = getImage(type.getID().toString());

            if (img != null) {
              graphics.drawImage(img, pos.x, pos.y, size.width, size.height, null);
            }
          }
        }
      }
    }
  }

  /**
   * This method checks, if the given containtertype is in the rules and if it has a properties
   * settings to not render it (default:true)
   */
  protected boolean isEnabled(Region region, UnitContainerType containerType) {
    // that should be enough...
    return PropertiesHelper.getBoolean(Client.INSTANCE.getProperties(),
        PropertiesHelper.BUILDINGRENDERER_RENDER + containerType.getID(), true);
  }

  /**
   * @see magellan.client.swing.map.HexCellRenderer#getPreferencesAdapter()
   */
  @Override
  public PreferencesAdapter getPreferencesAdapter() {
    return new BuildingCellRendererPreferences(this, settings);
  }

  /**
   * @see magellan.client.swing.map.HexCellRenderer#getPlaneIndex()
   */
  @Override
  public int getPlaneIndex() {
    return Mapper.PLANE_BUILDING;
  }

  /**
   * @see magellan.client.swing.map.HexCellRenderer#getName()
   */
  @Override
  public String getName() {
    return Resources.get("map.buildingcellrenderer.name");
  }

  /**
   * This is the inner class used for the preferences
   * 
   * @author Thoralf Rickert
   * @version 1.0, 28.02.2008
   */

  protected class BuildingCellRendererPreferences extends JPanel implements PreferencesAdapter {
    protected BuildingCellRenderer source = null;
    protected GameData data = null;
    protected List<JCheckBox> buildings;
    private Properties settings;

    /**
     * Creates a new BuildingCellRendererPreferences object.
     */
    protected BuildingCellRendererPreferences(MapCellRenderer source, Properties settings) {
      super(new BorderLayout());
      this.source = (BuildingCellRenderer) source;
      this.settings = settings;
    }

    protected void initGUI() {
      JPanel panel = new JPanel(new SpringLayout());
      panel.setBorder(new TitledBorder(new EtchedBorder(), Resources
          .get("building.renderer.show.caption")));

      buildings = new ArrayList<JCheckBox>();

      // arrange one box for each building type in a grid
      if (data != null) {
        int i = 0;
        for (BuildingType type : data.rules.getBuildingTypes()) {

          boolean selected =
              PropertiesHelper.getBoolean(Client.INSTANCE.getProperties(),
                  PropertiesHelper.BUILDINGRENDERER_RENDER + type.getID(), true);

          JCheckBox box =
              new JCheckBox(Resources.get("building.renderer.show", type.getName()), selected);
          box.setActionCommand(type.getID().toString());
          buildings.add(box);
          panel.add(box);

          // add slack components
          if (++i % 5 == 0) {
            panel.add(new JPanel());
          }
        }
      }
      SpringUtilities.makeCompactGrid(panel, 0, 6, 3, 3, 3, 3);

      this.add(panel, BorderLayout.CENTER);
    }

    /**
     * @see magellan.client.swing.preferences.PreferencesAdapter#applyPreferences()
     */
    public void applyPreferences() {
      for (JCheckBox box : buildings) {
        boolean selected = box.isSelected();
        String id = box.getActionCommand();
        settings.setProperty(PropertiesHelper.BUILDINGRENDERER_RENDER + id, Boolean
            .toString(selected));
      }
    }

    /**
     * @see magellan.client.swing.preferences.PreferencesAdapter#getComponent()
     */
    public Component getComponent() {
      return this;
    }

    /**
     * @see magellan.client.swing.preferences.PreferencesAdapter#getTitle()
     */
    public String getTitle() {
      return getName();
    }

    /**
     * @see magellan.client.swing.preferences.PreferencesAdapter#initPreferences()
     */
    public void initPreferences() {
      removeAll();
      data = Client.INSTANCE.getData();
      initGUI();
    }
  }
}
