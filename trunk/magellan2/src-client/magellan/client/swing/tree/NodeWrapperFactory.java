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

package magellan.client.swing.tree;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;

import magellan.client.swing.context.ContextChangeable;
import magellan.client.swing.context.ContextObserver;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.client.swing.preferences.PreferencesFactory;
import magellan.library.Border;
import magellan.library.Faction;
import magellan.library.Group;
import magellan.library.Island;
import magellan.library.Item;
import magellan.library.Potion;
import magellan.library.Region;
import magellan.library.Skill;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;


/**
 * DOCUMENT ME!
 *
 * @author Andreas
 * @version
 */
public class NodeWrapperFactory extends JTabbedPane implements PreferencesFactory, ContextObserver {
	private static final Logger log = Logger.getInstance(NodeWrapperFactory.class);

	/** DOCUMENT-ME */
	public static final int BORDER = 0;

	/** DOCUMENT-ME */
	public static final int FACTION = 1;

	/** DOCUMENT-ME */
	public static final int ISLAND = 2;

	/** DOCUMENT-ME */
	public static final int REGION = 3;

	/** DOCUMENT-ME */
	public static final int UNITCONTAINER = 4;

	/** DOCUMENT-ME */
	public static final int UNIT = 5;

	/** DOCUMENT-ME */
	public static final int POTION = 6;

	/** DOCUMENT-ME */
	public static final int ITEM = 7;

	/** DOCUMENT-ME */
	public static final int SKILL = 8;

	/** DOCUMENT-ME */
	public static final int GROUP = 9;

	/** DOCUMENT-ME */
	public static final int SIMPLE = 10;
	protected Properties settings;
	protected boolean initialized[];
	protected NodeWrapperDrawPolicy adapters[];
	protected JTabbedPane tabs;
	protected String initString = null;
	protected String title = null;
	protected JMenu contextMenu;
	protected TreeUpdate source;

	/**
	 * Creates new NodeWrapperFactory
	 *
	 * 
	 */
	public NodeWrapperFactory(Properties settings) {
		this(settings, null, null);
	}

	/**
	 * Creates a new NodeWrapperFactory object.
	 *
	 * 
	 * 
	 * 
	 */
	public NodeWrapperFactory(Properties settings, String initString, String title) {
		super(JTabbedPane.LEFT);

		this.initString = initString;

		if(title == null) {
			title = Resources.get("tree.nodewrapperfactory.title.unknown");
		}

		this.title = title;
		this.settings = settings;
		initialized = new boolean[11];
		adapters = new NodeWrapperDrawPolicy[11];

		for(int i = 0; i < 11; i++) {
			initialized[i] = false;
			adapters[i] = null;
		}

		// init our Pref-Adapter interface
		initUI();
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setSource(TreeUpdate s) {
		source = s;
	}

	// Initializes the PreferencesAdapter GUI
	protected void initUI() {
		// (context) menu
		contextMenu = new JMenu(title);
		contextMenu.setEnabled(false);
	}

	//update the context menu
	protected void updateContextMenu() {
		contextMenu.removeAll();

		for(int i = 0; i < adapters.length; i++) {
			if((adapters[i] != null) && (adapters[i] instanceof ContextChangeable)) {
				ContextChangeable cc = (ContextChangeable) adapters[i];
				JMenuItem item = cc.getContextAdapter();

				if(item instanceof JMenu) {
					contextMenu.add(item);
				} else {
					JMenu help = new JMenu(adapters[i].getTitle());
					help.add(item);
					contextMenu.add(help);
				}

				cc.setContextObserver(this);
			}
		}

		contextMenu.setEnabled(true);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public JMenu getContextMenu() {
		return contextMenu;
	}

	// initialize a CellObject by category index
	protected void init(CellObject co, int index) {
		NodeWrapperDrawPolicy o = null;

		if(initString == null) {
			o = co.init(settings, adapters[index]);
		} else {
			o = co.init(settings, initString, adapters[index]);
		}

		if(!initialized[index] && (o != null)) {
			adapters[index] = o;
			initialized[index] = true;

			if(o instanceof ContextChangeable) {
				updateContextMenu();
			}
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public BorderNodeWrapper createBorderNodeWrapper(Border b) {
		BorderNodeWrapper bnw = new BorderNodeWrapper(b);
		init(bnw, BORDER);

		return bnw;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 * 
	 *
	 * 
	 */
	public FactionNodeWrapper createFactionNodeWrapper(Faction f, Region r, Map alliances) {
		FactionNodeWrapper fnw = new FactionNodeWrapper(f, r, alliances);
		init(fnw, FACTION);

		return fnw;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public IslandNodeWrapper createIslandNodeWrapper(Island i) {
		IslandNodeWrapper inw = new IslandNodeWrapper(i);
		init(inw, ISLAND);

		return inw;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public RegionNodeWrapper createRegionNodeWrapper(Region r) {
		RegionNodeWrapper rnw = new RegionNodeWrapper(r);
		init(rnw, REGION);

		return rnw;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
	 */
	public RegionNodeWrapper createRegionNodeWrapper(Region r, int amount) {
		RegionNodeWrapper rnw = new RegionNodeWrapper(r, amount);
		init(rnw, REGION);

		return rnw;
	}

	/**
	 * Creates a wrapper node for a unit container.
	 *
	 * @param uc 
	 *
	 * @return The NodeWrapper
	 */
	public UnitContainerNodeWrapper createUnitContainerNodeWrapper(UnitContainer uc) {
		return createUnitContainerNodeWrapper(uc, true, false);
	}

	/**
	 * Creates a wrapper node for a unit container with extended options.
	 *
	 * @param uc
	 * @param showFreeLoad Specifies if the free load should be displayed
	 * @param hasCommand If <code>true</code>, it is indicated the the unit has the command
	 * @return The NodeWrapper
	 */
	public UnitContainerNodeWrapper createUnitContainerNodeWrapper(UnitContainer uc, boolean showFreeLoad, boolean hasCommand) {
		UnitContainerNodeWrapper ucnw = new UnitContainerNodeWrapper(uc, showFreeLoad, hasCommand);
		init(ucnw, UNITCONTAINER);

		return ucnw;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public UnitNodeWrapper createUnitNodeWrapper(Unit unit) {
		return createUnitNodeWrapper(unit, null, -1, -1);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
	 */
	public UnitNodeWrapper createUnitNodeWrapper(Unit unit, int num) {
		return createUnitNodeWrapper(unit, null, num, -1);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 * 
	 *
	 * 
	 */
	public UnitNodeWrapper createUnitNodeWrapper(Unit unit, int num, int mod) {
		return createUnitNodeWrapper(unit, null, num, mod);
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
	 */
	public UnitNodeWrapper createUnitNodeWrapper(Unit unit, String prfx, int num, int mod) {
		UnitNodeWrapper unw = new UnitNodeWrapper(unit, prfx, num, mod);
		init(unw, UNIT);

		return unw;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
	 */
	public UnitNodeWrapper createUnitNodeWrapper(Unit unit, String text) {
		UnitNodeWrapper unw = new UnitNodeWrapper(unit, text);
		init(unw, UNIT);

		return unw;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public PotionNodeWrapper createPotionNodeWrapper(Potion potion) {
		PotionNodeWrapper pnw = new PotionNodeWrapper(potion);
		init(pnw, POTION);

		return pnw;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
	 */
	public PotionNodeWrapper createPotionNodeWrapper(Potion potion, String name, String postfix) {
		PotionNodeWrapper pnw = new PotionNodeWrapper(potion, name, postfix);
		init(pnw, POTION);

		return pnw;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public ItemNodeWrapper createItemNodeWrapper(Item item) {
		return createItemNodeWrapper(null, item);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
	 */
	public ItemNodeWrapper createItemNodeWrapper(Unit unit, Item item) {
		ItemNodeWrapper inw = new ItemNodeWrapper(unit, item);
		init(inw, ITEM);

		return inw;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 * 
	 *
	 * 
	 */
	public SkillNodeWrapper createSkillNodeWrapper(Unit unit, Skill skill, Skill modSkill) {
		SkillNodeWrapper snw = new SkillNodeWrapper(unit, skill, modSkill);
		init(snw, SKILL);

		return snw;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public GroupNodeWrapper createGroupNodeWrapper(Group group) {
		GroupNodeWrapper gnw = new GroupNodeWrapper(group);
		init(gnw, GROUP);

		return gnw;
	}

    
    public SimpleNodeWrapper createSimpleNodeWrapper(Object obj, String text, Object icons) {
        SimpleNodeWrapper snw = new SimpleNodeWrapper(obj, text, icons);
        init(snw, SIMPLE);

        return snw;
        
    }
    
	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
	 */
	public SimpleNodeWrapper createSimpleNodeWrapper(Object obj, Object icons) {
		SimpleNodeWrapper snw = new SimpleNodeWrapper(obj, icons);
		init(snw, SIMPLE);

		return snw;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 * 
	 *
	 * 
	 */
	public SimpleNodeWrapper createSimpleNodeWrapper(Object text, Object icons, String clipValue) {
		SimpleNodeWrapper snw = new SimpleNodeWrapper(text, icons, clipValue);
		init(snw, SIMPLE);

		return snw;
	}

	/**
	 * DOCUMENT-ME
	 */
	public void contextDataChanged() {
		log.info("Context data changed. " + source);

		if(source != null) {
			source.updateTree(this);
		}
	}

	/////////////////////////////
	// PreferencesAdapter code //
	/////////////////////////////
	public PreferencesAdapter createPreferencesAdapter() {
		return new NodeWrapperFactoryPreferences(adapters);
	}

	class NodeWrapperFactoryPreferences extends JTabbedPane implements PreferencesAdapter {
		List<PreferencesAdapter> myAdapters;

		protected NodeWrapperFactoryPreferences(NodeWrapperDrawPolicy adapters[]) {
			myAdapters = new LinkedList<PreferencesAdapter>();

			for(int i = 0; i < adapters.length; i++) {
				if(adapters[i] != null) {
					PreferencesAdapter pref = adapters[i].createPreferencesAdapter();

					if(pref != null) {
						addTab(pref.getTitle(), pref.getComponent());
						myAdapters.add(pref);
					}
				}
			}

			// try to enforce only one column
			setPreferredSize(null);

			java.awt.Dimension dim = getPreferredSize();
			int tabHeight = getTabCount() * 30; // just approximate since there are no public functions :-(

			if(dim.height < tabHeight) {
				dim.height = tabHeight;
				setPreferredSize(dim);
			}
		}

        public void initPreferences() {
            // TODO: implement it
        }

		/**
		 * DOCUMENT-ME
		 */
		public void applyPreferences() {
			Iterator it = myAdapters.iterator();

			while(it.hasNext()) {
				PreferencesAdapter pref = (PreferencesAdapter) it.next();
				pref.applyPreferences();
			}
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public java.awt.Component getComponent() {
			return this;
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public String getTitle() {
			return title;
		}
	}
}
