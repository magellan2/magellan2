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
	public void setShowTooltip(boolean b) {
		// never show tooltips
	}
  
  /**
   * @see magellan.client.swing.map.Mapper#setRenderer(magellan.client.swing.map.MapCellRenderer)
   */
  public void setRenderer(MapCellRenderer renderer) {
    log.info("Minimapper.setRenderer()"+renderer);
    super.setRenderer(renderer);
    settings.setProperty("Minimap.Renderer",renderer.getClass().getName());
  }

	/**
	 * 
	 */
	public void setRenderer(MapCellRenderer renderer, int plane) {
		String old = settings.getProperty("Mapper.Planes." + plane);
		super.setRenderer(renderer, plane);
		settings.setProperty("Mapper.Planes." + plane, old);
	}

  /**
   * @see magellan.client.swing.map.Mapper#initRenderingPlanes()
   */
	protected RenderingPlane[] initRenderingPlanes() {
		RenderingPlane p[] = new RenderingPlane[1];
		p[PLANE_REGION] = new RenderingPlane(PLANE_REGION, Resources.get("map.mapper.plane.region.name"), 1);
		p[PLANE_REGION].setRenderer(myRenderer = new RegionShapeCellRenderer(getCellGeometry(),
																			 context,
																			 "Minimap.FactionColors",
																			 "Minimap.RegionColors",
																			 "Minimap.PoliticsMode"));

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
	 *
	 * 
	 */
	public void setPaintMode(int mode) {
		myRenderer.setPaintMode(mode);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
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

	protected void setLastRegionRenderingType(int l) {
		minimapLastType = l;
	}

	protected int getLastRegionRenderingType() {
		return minimapLastType;
	}
}
