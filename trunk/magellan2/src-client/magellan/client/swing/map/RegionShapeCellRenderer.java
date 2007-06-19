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
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;

import magellan.client.MagellanContext;
import magellan.client.desktop.Initializable;
import magellan.client.swing.context.ContextChangeable;
import magellan.client.swing.context.ContextObserver;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.ID;
import magellan.library.Region;
import magellan.library.StringID;
import magellan.library.Unit;
import magellan.library.event.GameDataEvent;
import magellan.library.event.GameDataListener;
import magellan.library.rules.RegionType;
import magellan.library.utils.Colors;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;


/**
 * This renderer draws plain one-color hex fields. It supports three modes - the Regiontype-Mode,
 * the Politics-Mode and the All-Factions-Mode. In Regiontype-Mode a region is rendered with a
 * color based on its type while in Politics Mode the color is based on the faction reigning over
 * the region. In All-Factions-Mode the colors of all factions in this region are used.
 */
public class RegionShapeCellRenderer extends AbstractRegionShapeCellRenderer
	implements Initializable, ContextChangeable, GameDataListener
{
	private static final Logger log = Logger.getInstance(RegionShapeCellRenderer.class);

	/** DOCUMENT-ME */
	public static final String MAP_TAG = "mapcolor";

	/** The default value for factionKey. */
	public static final String DEFAULT_FACTION_KEY = "GeomRenderer.FactionColors";

	/** The default value for regionKey. */
	public static final String DEFAULT_REGION_KEY = "GeomRenderer.RegionColors";

	/** The default value for paintKey. */
	public static final String DEFAULT_PAINTMODE_KEY = "GeomRenderer.paintMode";

	/** Defines the RegionType paint mode. */
	public static final int PAINT_REGIONTYPE = 0;

	/** Defines the Politics paint mode. */
	public static final int PAINT_POLITICS = 1;

	/** Defines the All Factions paint mode. */
	public static final int PAINT_ALLFACTIONS = 2;

	/** Defines the Trust Level OVERALL paint mode. */
	public static final int PAINT_TRUST_OVERALL = 3;

	/** Defines the Trust Level GUARD paint mode. */
	public static final int PAINT_TRUST_GUARD = 4;

	/** Stores the current painting mode. */
	protected int paintMode;

	/** Used for storing all found factions. */
	private List<String> factions = new LinkedList<String>();

	/** Stores the faction - color paires. The key is the faction name (Faction.getName()) */
	protected Map<String,Color> factionColors;

	/** The ocean is rendered with this color in Politics Mode. */
	protected Color oceanColor;

	/** All regions which can't be assigned to a faction are rendered with this color. */
	protected Color unknownColor;

	/** Stores the region - color paires. The key is the region type name (RegionType.getName()). */
	protected Map<ID,Color> regionColors;

	/** The PreferencesAdapter used for changing colors. */
	protected GeomRendererAdapter adapter;

	/** The default key for settings operations with the faction-color table. */
	protected String factionKey;

	/** The default key for settings operations with the region-color table. */
	protected String regionKey;

	/** The default key for settings operations with the Politics Mode variable. */
	protected String paintKey;

	/** A default color for regions without type. */
	public static Color typelessColor = new Color(128, 128, 128);
	protected JMenu contextMenu;
	protected ContextObserver obs;
	protected Color singleColorArray[] = new Color[1];

	/**
	 * Constructs a new RegionShapeCellRenderer object using the given geometry and settings. The
	 * default settings-operations keys are initialized to:
	 * 
	 * <ul>
	 * <li>
	 * factionKey="GeomRenderer.FactionColors"
	 * </li>
	 * <li>
	 * regionKey="GeomRenderer.RegionColors"
	 * </li>
	 * <li>
	 * paintKey="GeomRenderer.paintMode"
	 * </li>
	 * </ul>
	 * 
	 *
	 * @param geo The CellGeometry to be used
	 * @param settings The Properties to be used
	 */
	public RegionShapeCellRenderer(CellGeometry geo, MagellanContext context) {
		this(geo, context, DEFAULT_FACTION_KEY, DEFAULT_REGION_KEY, DEFAULT_PAINTMODE_KEY);
	}

	/**
	 * Constructs a new RegionShapeCellRenderer object using the given geometry and settings. The
	 * given Strings fKey, rKey and pKey are used to initialize the default settings-operations
	 * keys.
	 *
	 * @param geo The CellGeometry to be used
	 * @param settings The Properties to be used
	 * @param fKey The factionKey value for settings operations
	 * @param rKey The regionKey value for settings operations
	 * @param pKey The paintKey value for settings operations
	 */
	public RegionShapeCellRenderer(CellGeometry geo, MagellanContext context, String fKey, String rKey,
								   String pKey) {
		super(geo, context);

		factionColors = new HashMap<String,Color>();
		regionColors = new HashMap<ID, Color>();

		// use this keys for default load/save operations
		factionKey = fKey;
		regionKey = rKey;
		paintKey = pKey;

		// initialize faction colors
		loadFactionColors(false);
		loadOceanColor();
		loadUnknownColor();

		// initialize region colors
		initDefaultRegionTypeColors(); // load default as base
		loadRegionColors(false); // add the settings-colors

		try {
			setPaintMode(Integer.parseInt(settings.getProperty(paintKey)));
		} catch(Exception exc) {
			setPaintMode(0);
		}

		initContextMenu();

		context.getEventDispatcher().addGameDataListener(this);
	}

	/**
	 * Puts default values for some region-types to the region-color table. Currently the following
	 * region-types are implemented:
	 * 
	 * <ul>
	 * <li>
	 * Ebene
	 * </li>
	 * <li>
	 * Sumpf
	 * </li>
	 * <li>
	 * Wald
	 * </li>
	 * <li>
	 * W??ste
	 * </li>
	 * <li>
	 * Ozean
	 * </li>
	 * <li>
	 * Feuerwand
	 * </li>
	 * <li>
	 * Hochebene
	 * </li>
	 * <li>
	 * Berge
	 * </li>
	 * <li>
	 * Gletscher
	 * </li>
	 * </ul>
	 */
	protected void initDefaultRegionTypeColors() {
		regionColors.put(StringID.create("Ebene"), new Color(49, 230, 0));
		regionColors.put(StringID.create("Sumpf"), new Color(0, 180, 160));
		regionColors.put(StringID.create("Wald"), new Color(36, 154, 0));
		regionColors.put(StringID.create("W??ste"), new Color(244, 230, 113));
		regionColors.put(StringID.create("Ozean"), new Color(128, 128, 255));
		regionColors.put(StringID.create("Feuerwand"), new Color(255, 79, 0));
		regionColors.put(StringID.create("Hochebene"), new Color(166, 97, 56));
		regionColors.put(StringID.create("Berge"), new Color(155, 145, 141));
		regionColors.put(StringID.create("Gletscher"), new Color(222, 226, 244));
	}

	////////////////////
	// Access methods //
	////////////////////

	/**
	 * Returns the current Politics Mode.
	 *
	 * @return the current mode
	 */
	public int getPaintMode() {
		return paintMode;
	}

	/**
	 * Sets the Paint Mode of this renderer to pm. The new setting is stored with the paintKey in
	 * the global settings.
	 *
	 * @param mode The new mode
	 */
	public void setPaintMode(int mode) {
		if(paintMode == mode) {
			return;
		}

		paintMode = mode;
		settings.setProperty(paintKey, String.valueOf(paintMode));

		//adapter.showCard(paintMode);
	}

	/**
	 * Changes the color of faction f to color c.
	 *
	 * @param f The faction the color is for
	 * @param c The (new) color
	 */
	public void setFactionColor(Faction f, Color c) {
		setFactionColor(f.getName(), c);
	}

	/**
	 * Changes the color of the faction with title name to color c.
	 *
	 * @param name The faction name the color is for
	 * @param c The (new) color
	 */
	public void setFactionColor(String name, Color c) {
		Color oldC = (Color) factionColors.get(name);

		if(!c.equals(oldC)) {
			factionColors.put(name, c);
			saveFactionColors();
		}
	}

	/**
	 * Puts a whole map to the faction-colors Table. Keys of this map should be factions or
	 * strings.
	 *
	 * @param map A map containing Faction/String - Color pairs
	 */
	public void setFactionColors(Map<Object,Color> map) {
		Iterator it = map.keySet().iterator();

		while(it.hasNext()) {
			Object o = it.next();

			if(o instanceof Faction) {
				factionColors.put(((Faction) o).getName(), map.get(o));
			}

			if(o instanceof String) {
				factionColors.put((String)o, map.get(o));
			}
		}

		saveFactionColors();
	}

	/**
	 * Changes the color of region-type r to color c.
	 *
	 * @param r The RegionType the color is for
	 * @param c The (new) color
	 */
	public void setRegionColor(RegionType r, Color c) {
		setRegionColor(r.getID(), c);
	}

	/**
	 * Changes the color of the region-type with title name to color c.
	 *
	 * @param name The RegionType name the color is for
	 * @param c The (new) color
	 */
	public void setRegionColor(ID name, Color c) {
		Color oldC = (Color) regionColors.get(name);

		if(!c.equals(oldC)) {
			regionColors.put(name, c);
			saveRegionColors();
		}
	}

	/**
	 * Puts a whole map to the region-colors Table. Keys should be region-types or strings.
	 *
	 * @param map A map containig RegionType/String - Color pairs
	 */
	public void setRegionColors(Map<Object,Color> map) {
		for(Iterator iter = map.keySet().iterator(); iter.hasNext();) {
			Object o = iter.next();

			if(o instanceof RegionType) {
				regionColors.put(((RegionType) o).getID(), map.get(o));
			}

			if(o instanceof String) {
				regionColors.put(StringID.create((String) o), map.get(o));
			}
		}

		saveRegionColors();
	}

	/**
	 * Returns the colors used for ocean regions in Politics Mode.
	 *
	 * @return the ocean color
	 */
	public Color getOceanColor() {
		return oceanColor;
	}

	/**
	 * Sets the color used for ocean fields in Politics Mode and stores it in the global settings.
	 *
	 * @param c The new ocean color
	 */
	public void setOceanColor(Color c) {
		oceanColor = c;
		saveOceanColor();
	}

	/**
	 * Returns the colors used for unassigned regions in Politics Mode.
	 *
	 * @return The color for unassigned regions
	 */
	public Color getUnknownColor() {
		return unknownColor;
	}

	/**
	 * Sets the color used for unassigned fields in Politics Mode and stores it in the global
	 * settings.
	 *
	 * @param c The new color for unassigned regions
	 */
	public void setUnknownColor(Color c) {
		unknownColor = c;
		saveUnknownColor();
	}

	/**
	 * Returns the default key for settings operations concerning the faction-colors table.
	 *
	 * @return the faction-color table key
	 */
	public String getFactionKey() {
		return factionKey;
	}

	/**
	 * Sets the key used for deafult settings operations concerning the faction colors.
	 *
	 * @param fKey the new key
	 */
	public void setFactionKey(String fKey) {
		factionKey = fKey;
	}

	/**
	 * Returns the default key for settings operations concerning the region-colors table.
	 *
	 * @return the region-color table key
	 */
	public String getRegionKey() {
		return regionKey;
	}

	/**
	 * Sets the key used for deafult settings operations concerning the region colors.
	 *
	 * @param rKey the new key
	 */
	public void setRegionKey(String rKey) {
		regionKey = rKey;
	}

	/**
	 * Returns the default key for settings operations concerning the mode of this renderer.
	 *
	 * @return the Politics Mode key
	 */
	public String getPaintKey() {
		return paintKey;
	}

	/**
	 * Sets the key used for deafult settings operations concerning the Politics Mode.
	 *
	 * @param pKey the new key
	 */
	public void setPaintKey(String pKey) {
		paintKey = pKey;
	}

	////////////////////////////////
	// Global settings operations //
	////////////////////////////////
	protected void load(Map dest, String key, boolean reset) {
		load(dest, key, reset, false);
	}

	/**
	 * Parses the global settings value with the given key into the hashtable dest. If the reset
	 * flag is set, the hashtable is cleared before parsing.
	 *
	 * @param dest The destination hashtable
	 * @param key the settings key to use
	 * @param reset true if the hashtable should be cleared
	 * 
	 */
	protected void load(Map dest, String key, boolean reset, boolean makeID) {
		if(reset) {
			dest.clear();
		}

		String colors = settings.getProperty(key);

		if(colors == null) {
			return;
		}

		StringTokenizer st = new StringTokenizer(colors, ";");

		try {
			while(st.hasMoreTokens()) {
				String name = st.nextToken();
				String red = st.nextToken();
				String green = st.nextToken();
				String blue = st.nextToken();

				// i.pavkovic: stay conform with Colors.decode
				Object mkey = name;

				if(makeID) {
					mkey = StringID.create(name);
				}

				dest.put(mkey, Colors.decode(red + "," + green + "," + blue));
			}
		} catch(Exception exc) {
		}

		// maybe not the right count
	}

	/**
	 * Writes the given hashtable into the global settings using the given key.
	 *
	 * @param source The source hashtable with name-Color pairs
	 * @param key The key for the property to set
	 */
	protected void save(Map source, String key) {
		StringBuffer buffer = new StringBuffer();
		Iterator it = source.keySet().iterator();

		while(it.hasNext()) {
			Object name = it.next();
			Color col = (Color) source.get(name);
			buffer.append(name);
			buffer.append(';');
			buffer.append(Colors.encode(col).replace(',', ';'));

			if(it.hasNext()) {
				buffer.append(';');
			}
		}

		settings.setProperty(key, buffer.toString());
	}

	/**
	 * Loads the faction colors saved with the default key factionKey. If the reset flag is set,
	 * the color-table is cleared before loading.
	 *
	 * @param reset true if the faction-colors should be resetted
	 */
	public void loadFactionColors(boolean reset) {
		loadFactionColors(factionKey, reset);
	}

	/**
	 * Loads the faction colors saved with the given key. If the reset flag is set, the color-table
	 * is cleared before loading.
	 *
	 * @param key The key for the settings to load from
	 * @param reset true if the faction colors should be resetted
	 */
	public void loadFactionColors(String key, boolean reset) {
		load(factionColors, key, reset);
	}

	/**
	 * Saves the faction colors into the global settings using the default key factionKey.
	 */
	public void saveFactionColors() {
		saveFactionColors(factionKey);
	}

	/**
	 * Saves the faction colors into the global settings using the given key.
	 *
	 * @param key The property to save to
	 */
	public void saveFactionColors(String key) {
		save(factionColors, key);
	}

	/**
	 * Loads the region colors saved with the default key regionKey. If the reset flag is set, the
	 * color-table is cleared before loading.
	 *
	 * @param reset true if the region colors should be resetted
	 */
	public void loadRegionColors(boolean reset) {
		loadRegionColors(regionKey, reset);
	}

	/**
	 * Loads the region colors saved with the given key. If the reset flag is set, the color-table
	 * is cleared before loading.
	 *
	 * @param key the property to load from
	 * @param reset true if the region colors should be resetted
	 */
	public void loadRegionColors(String key, boolean reset) {
		load(regionColors, key, reset, true);
	}

	/**
	 * Saves the faction colors into the global settings using the default key regionKey.
	 */
	public void saveRegionColors() {
		saveRegionColors(regionKey);
	}

	/**
	 * Saves the region colors into the global settings using the given key.
	 *
	 * @param key the property to save to
	 */
	public void saveRegionColors(String key) {
		save(regionColors, key);
	}

	/**
	 * Loads the ocean color used for ocean fields in Politics Mode. The key for this is
	 * "GeomRenderer.OceanColor".
	 */
	public void loadOceanColor() {
		String color = settings.getProperty("GeomRenderer.OceanColor", "128,128,255");

		// replace old ";" with ","
		oceanColor = Colors.decode(color.replace(';', ','));
	}

	/**
	 * Saves the ocean color using the key "GeomRenderer.OceanColor".
	 */
	public void saveOceanColor() {
		settings.setProperty("GeomRenderer.OceanColor", Colors.encode(oceanColor));
	}

	/**
	 * Loads the unknown color used for unassigned fields in Politics Mode. The key for this is
	 * "GeomRenderer.UnknownColor".
	 */
	public void loadUnknownColor() {
		String color = settings.getProperty("GeomRenderer.UnknownColor", "128,128,128");

		// replace old ";" with ","
		unknownColor = Colors.decode(color.replace(';', ','));
	}

	/**
	 * Saves the unknown color using the key "GeomRenderer.UnknownColor".
	 */
	public void saveUnknownColor() {
		settings.setProperty("GeomRenderer.UnknownColor", Colors.encode(unknownColor));
	}

	//////////////////////////////////////
	// Rendering color decision methods //
	//////////////////////////////////////
	protected Color[] getColor(Region r) {
		if(paintMode != PAINT_ALLFACTIONS) {
			return null;
		}

		Color col = getTagColor(r);

		if(col != null) {
			singleColorArray[0] = col;

			return singleColorArray;
		}

		factions.clear();

		Iterator it = r.units().iterator();

		while(it.hasNext()) {
			String f = ((Unit) it.next()).getFaction().getName();

			if(!factions.contains(f)) {
				factions.add(f);
			}
		}

		if(factions.size() == 0) {
			singleColorArray[0] = getUnknownColor();

			if(r.getRegionType().isOcean()) {
				singleColorArray[0] = getOceanColor();
			}

			return singleColorArray;
		}

		if(factions.size() == 1) {
			singleColorArray[0] = getFactionColor((String) factions.get(0));

			return singleColorArray;
		}

		Color cols[] = new Color[factions.size()];

		for(int i = 0; i < cols.length; i++) {
			cols[i] = (Color) getFactionColor((String) factions.get(i));
		}

		return cols;
	}

	protected Color getSingleColor(Region r) {
		Color col = getTagColor(r);

		if(col != null) {
			return col;
		}

		switch(paintMode) {
		case PAINT_REGIONTYPE:
			return getRegionTypeColor(r.getRegionType());

		case PAINT_POLITICS:
			return getPoliticsColor(r);

		case PAINT_ALLFACTIONS:
			return getUnknownColor(); // this is an error !!!

		case PAINT_TRUST_OVERALL:
			return getTrustColor(r, false);

		case PAINT_TRUST_GUARD:
			return getTrustColor(r, true);

		default:
			return null; // error
		}
	}

	protected Color getTagColor(Region r) {
		if(r.containsTag(MAP_TAG)) {
			String s = r.getTag(MAP_TAG);

			if(s.length() > 0) {
				if(s.charAt(0) == '#') {
					try {
						return Color.decode(s);
					} catch(NumberFormatException nfe) {
					}
				}

				return Colors.decode(s);
			}
		}

		return null;
	}

	/**
	 * Fills the cell polygon if in TrustLevel mode. Fallback is Politics mode. After that left
	 * side is filled with lowest over all trustlevel, right side with lowest trustlevel of
	 * guarding units.
	 *
	 * 
	 * 
	 *
	 * 
	 */
	protected Color getTrustColor(Region r, boolean guard) {
		Color col = null;

		if(guard) {
			// find lowest guarding trust
			col = getLowestGuardingTrustColor(r);
		} else {
			// find lowest over all trust
			col = getLowestTrustColor(r);
		}

		// fallback
		if(col == null) {
			col = getPoliticsColor(r);
		}

		return col;
	}

	protected Color getLowestTrustColor(Region r) {
		int minLevel = Integer.MAX_VALUE;
		Faction minFaction = null;
		Iterator it = r.units().iterator();

		while(it.hasNext()) {
			Unit u = (Unit) it.next();

			if(u.getFaction().getTrustLevel() < minLevel) {
				minFaction = u.getFaction();
				minLevel = minFaction.getTrustLevel();
			}
		}

		if(minFaction == null) {
			return null;
		}

		return getFactionColor(minFaction, Color.red);
	}

	protected Color getLowestGuardingTrustColor(Region r) {
		int minLevel = Integer.MAX_VALUE;
		Faction minFaction = null;
		Iterator it = r.units().iterator();

		while(it.hasNext()) {
			Unit u = (Unit) it.next();

			if((u.getGuard() != 0) && (u.getFaction().getTrustLevel() < minLevel)) {
				minFaction = u.getFaction();
				minLevel = minFaction.getTrustLevel();
			}
		}

		if(minFaction == null) {
			return null;
		}

		return getFactionColor(minFaction, Color.red);
	}

	/**
	 * Returns the color of a region type. If the type can't be found in the region-color table, a
	 * new random color entry is put.
	 *
	 * @param type the region type for which a color is needed
	 *
	 * @return the color for the given type
	 */
	protected Color getRegionTypeColor(RegionType type) {
		if(type == null) {
			return typelessColor;
		}

		if(!regionColors.containsKey(type.getID())) {
			Color c = new Color((int) (Math.random() * 128) + 128,
								(int) (Math.random() * 128) + 128, (int) (Math.random() * 128) +
								128);
			setRegionColor(type.getID(), c);

			//adapter.addColor(0,type,c);
		}

		return (Color) regionColors.get(type.getID());
	}

	/**
	 * Returns the color of a region depending on the faction which own this region. The current
	 * implementation first looks if the type of this region is "Ozean" in which case the ocean
	 * color is used. Then it checks for an explicitly set owner unit. Normally the owner of the
	 * biggest "Burg" is returned. If there's no "Burg", the people of the different factions are
	 * counted and the faction with most people "wins". If this algorithm does not supply a
	 * faction, the unknown color is used.
	 *
	 * @param r The region for which a color is needed
	 *
	 * @return the color for the given region
	 */
	protected Color getPoliticsColor(Region r) {
		Unit unit = r.getOwnerUnit();

		if(unit != null) {
			return getFactionColor(unit.getFaction());
		}

		Faction f = getMaxPeopleFaction(r);

		if(f != null) {
			return getFactionColor(f);
		}

		if(r.getRegionType().isOcean()) {
			return oceanColor;
		}

		return unknownColor;
	}

	/**
	 * Returns the color for faction f. If there's no entry in the faction-color table a new
	 * random-color entry is created.
	 *
	 * @param f the faction for which a color is needed
	 *
	 * @return the color for the given faction
	 */
	protected Color getFactionColor(Faction f) {
		return getFactionColor(f.getName());
	}

	/**
	 * Returns the color for faction with name f. If there's no entry in the faction-color table a
	 * new random-color entry is created.
	 *
	 * @param f the faction for which a color is needed
	 *
	 * @return the color for the given faction
	 */
	protected Color getFactionColor(String f) {
		if(f == null) {
			f = "null";
		}

		if(!factionColors.containsKey(f)) {
			Color c = new Color((int) (Math.random() * 128) + 128,
								(int) (Math.random() * 128) + 128, (int) (Math.random() * 128) +
								128);
			setFactionColor(f, c);

			//adapter.addColor(1,f,c);
		}

		return (Color) factionColors.get(f);
	}

	/**
	 * Returns the color for faction f. If there's no entry in the faction-color table the entry is
	 * created with given colr.
	 *
	 * @param f the faction for which a color is needed
	 * @param c the default color
	 *
	 * @return the color for the given faction
	 */
	protected Color getFactionColor(Faction f, Color c) {
		return getFactionColor(f.getName(), c);
	}

	/**
	 * Returns the color for faction with name f. If there's no entry in the faction-color table
	 * the entry is created with given color.
	 *
	 * @param f the faction for which a color is needed
	 * @param c the default color
	 *
	 * @return the color for the given faction
	 */
	protected Color getFactionColor(String f, Color c) {
		if(!factionColors.containsKey(f)) {
			setFactionColor(f, c);

			//adapter.addColor(1,f,c);
		}

		return (Color) factionColors.get(f);
	}

	/**
	 * Counts the people of all faction in the given region and returns the faction with most of
	 * them, or null if there are no units.
	 *
	 * @param r A region to count
	 *
	 * @return the faction with most people or null if there are no units
	 */
	public static Faction getMaxPeopleFaction(Region r) {
		Faction maxFaction = null;
		int maxPeople = -1;
		Iterator it = r.units().iterator();

		while(it.hasNext()) {
			Unit unit = (Unit) it.next();
			Faction curFaction = unit.getFaction();
			Iterator intern = r.units().iterator();
			int curPeople = 0;

			while(intern.hasNext()) {
				Unit unit2 = (Unit) intern.next();

				if(unit2.getFaction().equals(curFaction)) {
					curPeople += unit2.getPersons();
				}
			}

			if(curPeople > maxPeople) {
				maxPeople = curPeople;
				maxFaction = curFaction;
			}
		}

		return maxFaction;
	}

	/**
	 * Just returns the current mode number.
	 *
	 * 
	 */
	public java.lang.String getComponentConfiguration() {
		return String.valueOf(paintMode);
	}

	/**
	 * Trys to convert the string to a number and set the corresponding paint mode.
	 *
	 * 
	 */
	public void initComponent(java.lang.String p1) {
		try {
			setPaintMode(Integer.parseInt(p1));
		} catch(NumberFormatException nfe) {
		}
	}

	protected void initContextMenu() {
		contextMenu = new JMenu(getName());

		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setPaintMode(Integer.parseInt(e.getActionCommand()));

				if(obs != null) {
					obs.contextDataChanged();
				}
			}
		};

		JMenuItem item = new JMenuItem(Resources.get("map.regionshapecellrenderer.cmb.mode.terrain"));
		item.addActionListener(al);
		item.setActionCommand("0");
		contextMenu.add(item);

		item = new JMenuItem(Resources.get("map.regionshapecellrenderer.cmb.mode.byfaction"));
		item.addActionListener(al);
		item.setActionCommand("1");
		contextMenu.add(item);

		item = new JMenuItem(Resources.get("map.regionshapecellrenderer.cmb.mode.allfactions"));
		item.addActionListener(al);
		item.setActionCommand("2");
		contextMenu.add(item);

		item = new JMenuItem(Resources.get("map.regionshapecellrenderer.cmb.mode.trustlevel"));
		item.addActionListener(al);
		item.setActionCommand("3");
		contextMenu.add(item);

		item = new JMenuItem(Resources.get("map.regionshapecellrenderer.cmb.mode.trustlevelandguard"));
		item.addActionListener(al);
		item.setActionCommand("4");
		contextMenu.add(item);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public JMenuItem getContextAdapter() {
		return contextMenu;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setContextObserver(ContextObserver co) {
		obs = co;
	}

  /**
   * @see magellan.client.swing.map.HexCellRenderer#getName()
   */
  @Override
  public String getName() {
    return Resources.get("map.regionshapecellrenderer.name");
  }

	/**
	 * Invoked when the current game data object becomes invalid.
	 *
	 * 
	 */
	public void gameDataChanged(GameDataEvent e) {
		GameData data = e.getGameData();

		//check for new factions
		Iterator it = data.factions().values().iterator();

		while(it.hasNext()) {
			Faction f = (Faction) it.next();

			// generates a new one if not present
			if(f.getTrustLevel() <= Faction.TL_DEFAULT) {
				getFactionColor(f, Color.red);
			} else {
				getFactionColor(f);
			}
		}

		it = data.regions().values().iterator();

		while(it.hasNext()) {
			Region r = (Region) it.next();
			getRegionTypeColor(r.getRegionType());
		}

		this.data = data;
	}

	/**
	 * Returns the PreferencesAdapter this renderer uses.
	 *
	 * @return A GeomRendererAdapter instance
	 */
	public PreferencesAdapter getPreferencesAdapter() {
		/*if (adapter==null)
		    adapter=new GeomRendererAdapter();
		return adapter;*/
		return new GeomRendererAdapter();
	}

	/**
	 * A PreferencesAdapter for use with RegionShapeCellRenderer objects. It supplies two cards for
	 * both RegionType and Politics Mode. The cards are initialized at creation time. Updates are
	 * performed through calls of addColor(). Changes are applied directly.
	 */
	protected class GeomRendererAdapter extends JPanel implements PreferencesAdapter,
																  ActionListener
	{
		private class ModePanel extends JPanel implements ActionListener {
			/**
			 * The element for showing in the list. Encapsulates name and color plus an id(not
			 * necessary)
			 */
			private class ListElement {
				String name;
				ID id;
				Color color;

				ListElement(String n, ID i, Color c) {
					this.name = n;
					this.id = i;
					this.color = c;
				}
			}

			///////////////////
			// Some comparators for list elements
			///////////////////
			private class NameComparator implements Comparator {
				/**
				 * DOCUMENT-ME
				 *
				 * 
				 * 
				 *
				 * 
				 */
				public int compare(Object o1, Object o2) {
					return ((ListElement) o1).name.compareTo(((ListElement) o2).name);
				}

				/**
				 * DOCUMENT-ME
				 *
				 * 
				 *
				 * 
				 */
				public boolean equals(Object o) {
					return false;
				}
			}

			private class ListElementFactionTrustComparator implements Comparator<ListElement> {
				/**
				 * DOCUMENT-ME
				 *
				 * 
				 * 
				 *
				 * 
				 */
				public int compare(ListElement o1, ListElement o2) {
					String name1 = o1.name;
					String name2 = o2.name;

					if(name1 == null) {
						name1 = "null";
					}

					if(name2 == null) {
						name2 = "null";
					}

					// try to find the factions in a data object
					if(data != null) {
						Faction f1 = null;
						Faction f2 = null;
						Iterator it = data.factions().values().iterator();

						while(it.hasNext() && ((f1 == null) || (f2 == null))) {
							Faction f = (Faction) it.next();

							if(name1.equals(f.getName())) {
								f1 = f;
							}

							if(name2.equals(f.getName())) {
								f2 = f;
							}
						}

						if(f1 == null) {
							if(f2 == null) {
								return nameComp.compare(o1, o2);
							}

							return 1;
						} else {
							if(f2 == null) {
								return -1;
							}

							if(f1.getTrustLevel() == f2.getTrustLevel()) {
								return nameComp.compare(o1, o2);
							}

							return f2.getTrustLevel() - f1.getTrustLevel();
						}
					}

					return nameComp.compare(o1, o2);
				}

				/**
				 * DOCUMENT-ME
				 *
				 * 
				 *
				 * 
				 */
				public boolean equals(Object o) {
					return false;
				}
			}

			/**
			 * Renders the list. Prefers ListElement values were the color is used for a color
			 * icon.
			 */
			private class ModePanelCellRenderer extends JLabel implements ListCellRenderer {
				/**
				 * Simple colored icon of given size(uses prefDim of ModePanel)
				 */
				private class ColorIcon extends java.lang.Object implements javax.swing.Icon {
					private Color myColor;

					/**
					 * Creates a new ColorIcon object.
					 *
					 * 
					 */
					public ColorIcon(Color c) {
						myColor = c;
					}

					/**
					 * DOCUMENT-ME
					 *
					 * 
					 */
					public int getIconHeight() {
						return prefDim.height;
					}

					/**
					 * DOCUMENT-ME
					 *
					 * 
					 */
					public int getIconWidth() {
						return prefDim.width;
					}

					/**
					 * DOCUMENT-ME
					 *
					 * 
					 * 
					 * 
					 * 
					 */
					public void paintIcon(Component c, Graphics g, int x, int y) {
						g.setColor(myColor);
						g.fillRect(x, y, prefDim.width, prefDim.height);
					}

					/**
					 * DOCUMENT-ME
					 *
					 * 
					 */
					public void setColor(Color c) {
						myColor = c;
					}
				}

				ColorIcon icon;

				/**
				 * Creates a new ModePanelCellRenderer object.
				 */
				public ModePanelCellRenderer() {
					icon = new ColorIcon(Color.white);
					this.setIcon(icon);
					this.setOpaque(true);
				}

				/**
				 * DOCUMENT-ME
				 *
				 * 
				 * 
				 * 
				 * 
				 * 
				 *
				 * 
				 */
				public Component getListCellRendererComponent(JList list, Object value, int index,
															  boolean isSelected,
															  boolean cellHasFocus) {
					if(value instanceof ListElement) {
						ListElement elem = (ListElement) value;
						this.setText(elem.name);
						icon.setColor(elem.color);
					} else {
						icon.setColor(Color.white);
						this.setText("---");
					}

					this.setBackground(isSelected ? list.getSelectionBackground()
												  : list.getBackground());
					this.setForeground(isSelected ? list.getSelectionForeground()
												  : list.getForeground());

					return this;
				}
			}

			/**
			 * A small ListModel implementation that allows ordering through a Comparator.
			 */
			private class SortableListModel extends AbstractListModel {
				protected List data;
				protected Comparator comp = null;
				protected int offset = -1;

				/**
				 * Creates a new SortableListModel object.
				 *
				 * 
				 */
				public SortableListModel(Collection initData) {
					data = new LinkedList();

					if(initData != null) {
						data.addAll(initData);
					}
				}

				/**
				 * DOCUMENT-ME
				 *
				 * 
				 *
				 * 
				 */
				public Object getElementAt(int index) {
					return data.get(index);
				}

				/**
				 * DOCUMENT-ME
				 *
				 * 
				 */
				public int getSize() {
					return data.size();
				}

				/**
				 * DOCUMENT-ME
				 *
				 * 
				 */
				public void add(Object o) {
					data.add(o);
					fireIntervalAdded(this, data.size() - 1, data.size() - 1);

					if(comp != null) {
						sort(comp, offset);
					}
				}

				/**
				 * DOCUMENT-ME
				 */
				public void removeAll() {
					int old = data.size();
					data.clear();
					fireIntervalRemoved(this, 0, old - 1);
				}

				/**
				 * DOCUMENT-ME
				 *
				 * 
				 */
				public void contentChanged(int index) {
					fireContentsChanged(this, index, index);
				}

				/**
				 * DOCUMENT-ME
				 *
				 * 
				 * 
				 */
				public void sort(Comparator comp, int offset) {
					this.comp = comp;
					this.offset = offset;

					try {
						Collection front = null;

						if(offset > 0) {
							front = new LinkedList();

							Iterator it = data.iterator();

							for(int i = 0; i < offset; i++) {
								front.add(it.next());
								it.remove();
							}
						}

						Collections.sort(data, comp);

						if(offset > 0) {
							data.addAll(0, front);
						}

						fireContentsChanged(this, Math.max(0, offset), data.size() - 1);
					} catch(Exception exc) {
					}
				}
			}

      private final Logger log = Logger.getInstance(ModePanel.class);
			protected boolean mode;
			private Collection<Object> myColors;
			private Dimension prefDim;
			private JList list;
			private SortableListModel listModel;
			private String oceanLabel;
			private String unknownLabel;
			private Comparator nameComp;
			private Comparator trustComp;
			private JCheckBox nameBox;

			/**
			 * Creates a new ModePanel object.
			 *
			 * 
			 */
			public ModePanel(boolean politics) {
				mode = politics;

				oceanLabel = Resources.get("map.regionshapecellrenderer.lbl.ocean.caption");
				unknownLabel = Resources.get("map.regionshapecellrenderer.lbl.unassignedregion.caption");

				nameComp = new NameComparator();
				trustComp = new ListElementFactionTrustComparator();

				listModel = new SortableListModel(null);
				myColors = new LinkedList<Object>();
				loadElements(mode ? factionColors : regionColors);

				listModel.sort(nameComp, mode ? 2 : 0);

				prefDim = new Dimension(100, 10);

				list = new JList(listModel);
				list.setCellRenderer(new ModePanelCellRenderer());

				final JList mList = list;
				list.addMouseListener(new MouseAdapter() {
						public void mouseClicked(MouseEvent e) {
							if(e.getClickCount() == 2) {
								int index = mList.locationToIndex(e.getPoint());

								if(index >= 0) {
									actionPerformed(index);
								}
							}
						}
					});

				this.setLayout(new BorderLayout());
				this.add(new JScrollPane(list), BorderLayout.CENTER);

				if(mode) {
					JPanel left = new JPanel(magellan.client.swing.CenterLayout.SPAN_X_LAYOUT);
					Box box = Box.createVerticalBox();
					ButtonGroup group = new ButtonGroup();
					nameBox = new JCheckBox(Resources.get("map.regionshapecellrenderer.chk.compare.name"), true);
					nameBox.addActionListener(this);
					group.add(nameBox);
					box.add(nameBox);

					JCheckBox trustBox = new JCheckBox(Resources.get("map.regionshapecellrenderer.chk.compare.trust"), false);
					trustBox.addActionListener(this);
					group.add(trustBox);
					box.add(trustBox);
					left.add(box);
					this.add(left, BorderLayout.WEST);
				}
			}

			protected void addGUIPair(Object obj, Color col) {
				String name = null;
				ID id = null;

				if(obj instanceof RegionType) {
					RegionType rt = (RegionType) obj;
					name = rt.getName();
					id = rt.getID();
				} else if(obj instanceof ID) {
					id = (ID) obj;
					name = id.toString();
				} else {
					if(obj != null) {
						name = obj.toString();
					} else {
						name = "null";
					}
				}

				if(name == null) {
					log.info(obj);
				}

				ListElement element = new ListElement(name, id, col);
				listModel.add(element);
			}

			protected void loadElements(Map source) {
				if(mode) { // add ocean and unknown
					addGUIPair(oceanLabel, oceanColor);
					addGUIPair(unknownLabel, unknownColor);
				}

				// if we get IDs we have to get the object behind
				Iterator it = source.keySet().iterator();

				while(it.hasNext()) {
					Object name = null;
					ID id = null;
					Object key = it.next();

					if(mode) {
						name = key;
					} else {
						id = (ID) key;
						name = id;

						if((data != null) && (data.rules != null)) {
							RegionType rt = data.rules.getRegionType(id);

							if(rt != null) {
								name = rt;
							}
						}
					}

					Color c = (Color) source.get(key);
					addGUIPair(name, c);
					myColors.add(name);
				}
			}

			/**
			 * DOCUMENT-ME
			 *
			 * 
			 * 
			 */
			public void addColor(String name, Color c) {
				if(name == null) {
					name = "null";
				}

				if(!myColors.contains(name)) {
					addGUIPair(name, c);
					myColors.add(name);
				}
			}

			/**
			 * DOCUMENT-ME
			 *
			 * 
			 * 
			 */
			public void addColor(RegionType name, Color c) {
				if(!myColors.contains(name)) {
					addGUIPair(name, c);
					myColors.add(name);
				}
			}

			protected void actionPerformed(int index) {
				ListElement element = (ListElement) listModel.getElementAt(index);

				String title = null;
				int colMode = 0;

				if(oceanLabel.equals(element.name)) {
					title = Resources.get("map.regionshapecellrenderer.dialog.oceancolor.title");
					colMode = 1;
				} else if(unknownLabel.equals(element.name)) {
					title = Resources.get("map.regionshapecellrenderer.dialog.unassignedregioncolor.title");
					colMode = 2;
				} else {
					Object msgArgs[] = { element.name };
					title = (new java.text.MessageFormat(Resources.get("map.regionshapecellrenderer.dialog.color.title"))).format(msgArgs);
				}

				Color c = JColorChooser.showDialog(parent, title, element.color);

				if(c != null) {
					element.color = c;
					listModel.contentChanged(index);

					switch(colMode) {
					case 0:

						if(mode) {
							setFactionColor(element.name, c);
						} else {
							if(element.id != null) {
								setRegionColor(element.id, c);
							} else {
								setRegionColor(StringID.create(element.name), c);
							}
						}

						break;

					case 1:
						setOceanColor(c);

						break;

					case 2:
						setUnknownColor(c);

						break;
					}
				}
			}

			/**
			 * DOCUMENT-ME
			 *
			 * 
			 */
			public void actionPerformed(ActionEvent e) {
				// sort from third because of ocean und unknown
				if(e.getSource() == nameBox) {
					listModel.sort(nameComp, 2);
					list.repaint();
				} else {
					listModel.sort(trustComp, 2);
					list.repaint();
				}
			}
		}

		/** A dummy frame for Dialogs */
		private JFrame parent; // there should be another way

		/** The panel which holds the cards */
		protected JPanel inner;

		/** The layout which manages the mode cards */
		protected CardLayout card;

		/** The box for the two modes */
		private JComboBox modeBox;

		/** The two ModePanels for RegionType and Politics Mode */
		private ModePanel content[];

		/**
		 * Creates a new GeomRendererAdapter for the super class.
		 */
		public GeomRendererAdapter() {
			parent = new JFrame();

			inner = new JPanel();
			inner.setLayout(card = new CardLayout());
			inner.setBorder(new javax.swing.border.TitledBorder(BorderFactory.createEtchedBorder(),
																Resources.get("map.regionshapecellrenderer.border.colortable")));
			content = new ModePanel[2];
			content[0] = new ModePanel(false);
			content[1] = new ModePanel(true);

			JScrollPane scroller = new JScrollPane(content[0]);
			scroller.getVerticalScrollBar().setUnitIncrement(16);
			inner.add(scroller, "0");
			scroller = new JScrollPane(content[1]);
			scroller.getVerticalScrollBar().setUnitIncrement(16);
			inner.add(scroller, "1");

			String items[] = new String[5];
			items[0] = Resources.get("map.regionshapecellrenderer.cmb.mode.terrain");
			items[1] = Resources.get("map.regionshapecellrenderer.cmb.mode.byfaction");
			items[2] = Resources.get("map.regionshapecellrenderer.cmb.mode.allfactions");
			items[3] = Resources.get("map.regionshapecellrenderer.cmb.mode.trustlevel");
			items[4] = Resources.get("map.regionshapecellrenderer.cmb.mode.trustlevelandguard");
			modeBox = new JComboBox(items);
			modeBox.setEditable(false);
			modeBox.addActionListener(this);

			JPanel p = new JPanel();
			p.add(new JLabel(Resources.get("map.regionshapecellrenderer.lbl.rendermode.caption")));
			p.add(modeBox);

			setLayout(new BorderLayout());
			add(p, BorderLayout.NORTH);
			add(inner, BorderLayout.CENTER);

			//initColors();
			showCard(getPaintMode());
		}

		/**
		 * Adds a new pair to the indexed ModePanel.
		 *
		 * @param index the ModePanel to use - 0 for RegionType, 1 for Politics
		 * @param name the name of the new pair
		 * @param c the color of the new pair
		 */
		public void addColor(int index, String name, Color c) {
			content[index].addColor(name, c);
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 * 
		 * 
		 */
		public void addColor(int index, RegionType name, Color c) {
			content[index].addColor(name, c);
		}

        public void initPreferences() {
            // TODO: implement it
        }

		/**
		 * An empty implementation of the PreferencesAdapter interface. Changes are applied
		 * directly.
		 */
		public void applyPreferences() {
		}

		/**
		 * Returns the component for graphical display.
		 *
		 * @return the GUI component
		 */
		public Component getComponent() {
			return this;
		}

		/**
		 * Returns the title of this adapter.
		 *
		 * @return the adapter title
		 */
		public String getTitle() {
			return Resources.get("map.regionshapecellrenderer.name");
		}

		/**
		 * Called when the mode box changes its selection. Shows the corresponding card.
		 *
		 * @param p1 An action event
		 */
		public void actionPerformed(java.awt.event.ActionEvent p1) {
			int s = modeBox.getSelectedIndex();

			switch(s) {
			case 0:
				card.first(inner);

				break;

			case 1:
			case 2:
			case 3:
			case 4:
				card.last(inner);

				break;
			}

			setPaintMode(s);
		}

		/**
		 * Shows the card with index c.
		 *
		 * @param c 0 for regiontype, 1 for politics, 2 for all faction
		 */
		public void showCard(int c) {
			modeBox.setSelectedIndex(c);
		}
	}
}
