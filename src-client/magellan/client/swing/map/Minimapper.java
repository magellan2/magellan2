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

import java.util.Hashtable;
import java.util.Map;

import javax.swing.ToolTipManager;

import magellan.client.MagellanContext;
import magellan.library.utils.Resources;


/**
 * DOCUMENT ME!
 *
 * @author Andreas
 * @version
 */
public class Minimapper extends Mapper {
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
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 */
	public void setRenderer(MapCellRenderer renderer, int plane) {
		String old = settings.getProperty("Mapper.Planes." + plane);
		super.setRenderer(renderer, plane);
		settings.setProperty("Mapper.Planes." + plane, old);
	}

	protected RenderingPlane[] initRenderingPlanes() {
		RenderingPlane p[] = new RenderingPlane[1];
		p[PLANE_REGION] = new RenderingPlane(PLANE_REGION, Resources.get("magellan.map.mapper.plane.region.name"), 1);
		p[PLANE_REGION].setRenderer(myRenderer = new RegionShapeCellRenderer(getCellGeometry(),
																			 context,
																			 "Minimap.FactionColors",
																			 "Minimap.RegionColors",
																			 "Minimap.PoliticsMode"));

		return p;
	}

	/**
	 * DOCUMENT-ME
	 *
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

	// pavkovic 2003.01.28: this is a Map of the default Translations mapped to this class
	// it is called by reflection (we could force the implementation of an interface,
	// this way it is more flexible.)
	// Pls use this mechanism, so the translation files can be created automagically
	// by inspecting all classes.
	private static Map<String,String> defaultTranslations;

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public static synchronized Map<String,String> getDefaultTranslations() {
		if(defaultTranslations == null) {
			defaultTranslations = new Hashtable<String, String>();
			defaultTranslations.put("plane.region.name", "Regions");
		}

		return defaultTranslations;
	}
}
