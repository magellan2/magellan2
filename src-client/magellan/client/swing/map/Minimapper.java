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

import javax.swing.ToolTipManager;

import magellan.client.MagellanContext;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

/**
 * DOCUMENT ME!
 * 
 * @author Andreas
 * @version 1.0
 */
public class Minimapper extends Mapper {
  private static final Logger log = Logger.getInstance(Minimapper.class);
  private RegionShapeCellRenderer myRenderer;
  protected int minimapLastType = -1;

  /**
   * Creates new Minimapper.
   * 
   * @param context
   */
  public Minimapper(MagellanContext context) {
    super(context, null, new CellGeometry("cellgeometry.txt"));

    // if Mapper has registered us, we don't want this
    ToolTipManager.sharedInstance().unregisterComponent(this);
  }

  /**
   * Never shows tooltips.
   * 
   * @param b ignored
   * @see magellan.client.swing.map.Mapper#setShowTooltip(boolean)
   */
  @Override
  public void setShowTooltip(boolean b) {
    // never show tooltips
  }

  /**
   * @see magellan.client.swing.map.Mapper#setRenderer(magellan.client.swing.map.MapCellRenderer)
   */
  @Override
  public void setRenderer(MapCellRenderer renderer) {
    if (renderer.getPlaneIndex() == Mapper.PLANE_REGION) {
      Minimapper.log.debug("Minimapper.setRenderer(" + renderer.getClass().getName() + ")");
      super.setRenderer(renderer);
    }
  }

  /**
	 * 
	 */
  @Override
  public void setRenderer(MapCellRenderer renderer, int plane) {
    if (plane == Mapper.PLANE_REGION) {
      Minimapper.log.debug("Minimapper.setRenderer(" + renderer.getClass().getName() + ")");
      super.setRenderer(renderer, plane);
    }
  }

  @Override
  protected String getPropertyName(int plane) {
    return "Minimap.Renderer";
  }

  /**
   * @see magellan.client.swing.map.Mapper#initRenderingPlanes()
   */
  @Override
  protected RenderingPlane[] initRenderingPlanes() {
    RenderingPlane p[] = new RenderingPlane[Mapper.PLANE_REGION + 1];
    p[Mapper.PLANE_REGION] =
        new RenderingPlane(Mapper.PLANE_REGION, Resources.get("map.mapper.plane.region.name"),
            RenderingPlane.VISIBLE_REGIONS);
    p[Mapper.PLANE_REGION].setRenderer(myRenderer =
        new RegionShapeCellRenderer(getCellGeometry(), context, "Minimap.FactionColors",
            "Minimap.RegionColors", "Minimap.PoliticsMode"));
    return p;
  }

  /**
	 * 
	 */
  public MapCellRenderer getMinimapRenderer() {
    return myRenderer;
  }

  /**
   * DOCUMENT-ME
   */
  public void setPaintMode(int mode) {
    myRenderer.setPaintMode(mode);
  }

  /**
   * DOCUMENT-ME
   */
  public int getPaintMode() {
    return myRenderer.getPaintMode();
  }

  /**
   * DOCUMENT-ME
   */
  public void synchronizeColors() {
    // synchronize factions
    myRenderer.loadFactionColors(RegionShapeCellRenderer.DEFAULT_FACTION_KEY, false);
    myRenderer.saveFactionColors();

    // synchronize regions
    myRenderer.loadRegionColors(RegionShapeCellRenderer.DEFAULT_REGION_KEY, false);
    myRenderer.saveRegionColors();

    // load unknown/ocean
    myRenderer.loadOceanColor();
    myRenderer.loadUnknownColor();

    repaint();
  }

  @Override
  protected void setLastRegionRenderingType(int l) {
    minimapLastType = l;
  }

  @Override
  protected int getLastRegionRenderingType() {
    return minimapLastType;
  }
}
