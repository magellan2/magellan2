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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import magellan.client.swing.context.ContextChangeable;
import magellan.client.swing.context.ContextObserver;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.client.swing.preferences.PreferencesFactory;
import magellan.library.Alliance;
import magellan.library.Border;
import magellan.library.CombatSpell;
import magellan.library.EntityID;
import magellan.library.Faction;
import magellan.library.Group;
import magellan.library.Island;
import magellan.library.Item;
import magellan.library.Potion;
import magellan.library.Region;
import magellan.library.Skill;
import magellan.library.Spell;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.impl.MagellanFactionImpl;
import magellan.library.relation.UnitRelation;
import magellan.library.rules.ItemCategory;
import magellan.library.utils.Resources;
import magellan.library.utils.Units;
import magellan.library.utils.logging.Logger;

/**
 * DOCUMENT ME!
 *
 * @author Andreas
 * @version 1.0
 */
public class NodeWrapperFactory extends JTabbedPane implements PreferencesFactory, ContextObserver {
  private static final Logger log = Logger.getInstance(NodeWrapperFactory.class);

  /** The index of the policy adapter for border nodes */
  public static final int BORDER = 0;

  /** The index of the policy adapter for faction nodes */
  public static final int FACTION = 1;

  /** The index of the policy adapter for island nodes */
  public static final int ISLAND = 2;

  /** The index of the policy adapter for region nodes */
  public static final int REGION = 3;

  /** The index of the policy adapter for unit container nodes */
  public static final int UNITCONTAINER = 4;

  /** The index of the policy adapter for unit nodes */
  public static final int UNIT = 5;

  /** The index of the policy adapter for potion nodes */
  public static final int POTION = 6;

  /** The index of the policy adapter for spell nodes */
  private static final int SPELL = 7;

  /** The index of the policy adapter for item nodes */
  public static final int ITEM = 8;

  /** The index of the policy adapter for skill nodes */
  public static final int SKILL = 9;

  /** The index of the policy adapter for group nodes */
  public static final int GROUP = 10;

  /** The index of the policy adapter for simple nodes */
  public static final int SIMPLE = 11;

  private static final int NUM_ADAPTERS = 12;

  protected NodeWrapperDrawPolicy adapters[];

  protected Properties settings;
  protected boolean initialized[];
  protected JTabbedPane tabs;
  protected String initString = null;
  protected String title = null;
  protected JMenu contextMenu;
  protected TreeUpdate source;

  /**
   * Creates new NodeWrapperFactory
   */
  public NodeWrapperFactory(Properties settings) {
    this(settings, null, null);
  }

  /**
   * Creates a new NodeWrapperFactory object.
   */
  public NodeWrapperFactory(Properties settings, String initString, String title) {
    super(SwingConstants.LEFT);

    this.initString = initString;

    if (title == null) {
      title = Resources.get("tree.nodewrapperfactory.title.unknown");
    }

    this.title = title;
    this.settings = settings;
    initialized = new boolean[NodeWrapperFactory.NUM_ADAPTERS];
    adapters = new NodeWrapperDrawPolicy[NodeWrapperFactory.NUM_ADAPTERS];

    for (int i = 0; i < NodeWrapperFactory.NUM_ADAPTERS; i++) {
      initialized[i] = false;
      adapters[i] = null;
    }

    // init our Pref-Adapter interface
    initUI();
  }

  /**
   * Set the object that should be notified on context changes.
   */
  public void setSource(TreeUpdate s) {
    source = s;
  }

  // Initializes the PreferencesAdapter GUI
  protected void initUI() {
    // (context) menu
    contextMenu = new JMenu(title);
    contextMenu.setEnabled(false);
    init(new BorderNodeWrapper(null), NodeWrapperFactory.BORDER);
    init(new FactionNodeWrapper(new MagellanFactionImpl(EntityID.createEntityID(0, 36), null),
        null, null), NodeWrapperFactory.FACTION);
    init(new GroupNodeWrapper(null), NodeWrapperFactory.GROUP);
    init(new IslandNodeWrapper(null), NodeWrapperFactory.ISLAND);
    init(new PotionNodeWrapper(null, "", ""), NodeWrapperFactory.POTION);
    init(new UnitContainerNodeWrapper(null), NodeWrapperFactory.UNITCONTAINER);
    init(new RegionNodeWrapper(null), NodeWrapperFactory.REGION);
    init(new ItemNodeWrapper(null), NodeWrapperFactory.ITEM);
    init(new UnitNodeWrapper(null, ""), NodeWrapperFactory.UNIT);
    init(new SkillNodeWrapper(null, new Skill(null, 0, 0, 0, true), null), NodeWrapperFactory.SKILL);
    init(new SimpleNodeWrapper(null, "null"), NodeWrapperFactory.SIMPLE);

  }

  /**
   * Update the context menu, which governs certain display properties.
   */
  protected void updateContextMenu() {
    contextMenu.removeAll();

    for (NodeWrapperDrawPolicy adapter : adapters) {
      if ((adapter != null) && (adapter instanceof ContextChangeable)) {
        ContextChangeable cc = (ContextChangeable) adapter;
        JMenuItem item = cc.getContextAdapter();

        if (item instanceof JMenu) {
          contextMenu.add(item);
        } else {
          JMenu help = new JMenu(adapter.getTitle());
          help.add(item);
          contextMenu.add(help);
        }

        cc.setContextObserver(this);
      }
    }

    contextMenu.setEnabled(true);
  }

  /**
   * Returns the menu that can change display properties of the nodes created by this factory.
   */
  public JMenu getContextMenu() {
    return contextMenu;
  }

  /**
   * Initializes preference adapter for region nodes
   */
  public void initRegionNodeWrappers() {
    // init(new RegionNodeWrapper(null), NodeWrapperFactory.REGION);
  }

  /**
   * Initializes preference adapter for item nodes
   */
  public void initItemNodeWrappers() {
    // init(new ItemNodeWrapper(null), NodeWrapperFactory.ITEM);
  }

  /**
   * Initializes preference adapter for unit nodes
   */
  public void initUnitNodeWrappers() {
    // init(new UnitNodeWrapper(null, ""), NodeWrapperFactory.UNIT);
  }

  /**
   * Initializes preference adapter for skill nodes
   */
  public void initSkillNodeWrappers() {
    // init(new SkillNodeWrapper(null, new Skill(null, 0, 0, 0, true), null),
    // NodeWrapperFactory.UNIT);
  }

  /**
   * Initializes preference adapter for simple nodes
   */
  public void initSimpleNodeWrappers() {
    // init(new SimpleNodeWrapper(null, "null"), NodeWrapperFactory.SIMPLE);
  }

  /**
   * Initialize a CellObject by category index. This makes sure that the CellObject type is
   * registered with the preferences adapter and initializes the context menu.
   *
   * @param co
   * @param policy
   */
  protected void init(CellObject co, int policy) {
    NodeWrapperDrawPolicy o = null;

    if (initString == null) {
      o = co.init(settings, adapters[policy]);
    } else {
      o = co.init(settings, initString, adapters[policy]);
    }

    if (!initialized[policy] && (o != null)) {
      adapters[policy] = o;
      initialized[policy] = true;

      if (o instanceof ContextChangeable) {
        updateContextMenu();
      }
    }
  }

  /**
   * Create a node for a border (street, wall, etc.)
   */
  public BorderNodeWrapper createBorderNodeWrapper(Border b) {
    BorderNodeWrapper bnw = new BorderNodeWrapper(b);
    init(bnw, NodeWrapperFactory.BORDER);

    return bnw;
  }

  /**
   * Create a node for a faction.
   */
  public FactionNodeWrapper createFactionNodeWrapper(Faction f, Region r,
      Map<EntityID, Alliance> alliances) {
    FactionNodeWrapper fnw = new FactionNodeWrapper(f, r, alliances);
    init(fnw, NodeWrapperFactory.FACTION);

    return fnw;
  }

  /**
   * Create a node for an island.
   */
  public IslandNodeWrapper createIslandNodeWrapper(Island i) {
    IslandNodeWrapper inw = new IslandNodeWrapper(i);
    init(inw, NodeWrapperFactory.ISLAND);

    return inw;
  }

  /**
   * Create a node for a region.
   */
  public RegionNodeWrapper createRegionNodeWrapper(Region r) {
    RegionNodeWrapper rnw = new RegionNodeWrapper(r);
    init(rnw, NodeWrapperFactory.REGION);

    return rnw;
  }

  /**
   * Create a node for a region including a number (of persons).
   */
  public RegionNodeWrapper createRegionNodeWrapper(Region r, long amount) {
    RegionNodeWrapper rnw = new RegionNodeWrapper(r, amount);
    init(rnw, NodeWrapperFactory.REGION);

    return rnw;
  }

  /**
   * Creates a wrapper node for a unit relation.
   */
  public UnitRelationNodeWrapper createRelationNodeWrapper(Unit owner, UnitRelation rel,
      CellObject2 innerNode) {
    UnitRelationNodeWrapper rnw = new UnitRelationNodeWrapper2(owner, rel, innerNode);
    // FIXME which policy?
    init(rnw, NodeWrapperFactory.SIMPLE);
    return rnw;
  }

  /**
   * Creates a wrapper node for a unit relation.
   *
   * @param owner
   */
  public UnitRelationNodeWrapper createRelationNodeWrapper(Unit owner, UnitRelation rel,
      CellObject innerNode) {
    UnitRelationNodeWrapper rnw = new UnitRelationNodeWrapper(owner, rel, innerNode);
    // FIXME which policy?
    init(rnw, NodeWrapperFactory.SIMPLE);
    return rnw;
  }

  /**
   * Creates a wrapper node for a unit container.
   *
   * @param uc
   * @return The NodeWrapper
   */
  public UnitContainerNodeWrapper createUnitContainerNodeWrapper(UnitContainer uc) {
    return createUnitContainerNodeWrapper(uc, true, false, null);
  }

  /**
   * Creates a wrapper node for a unit container.
   *
   * @param uc
   * @return The NodeWrapper
   */
  public UnitContainerNodeWrapper createUnitContainerNodeWrapper(UnitContainer uc, String prefix) {
    return createUnitContainerNodeWrapper(uc, true, false, prefix);
  }

  /**
   * Creates a wrapper node for a unit container with extended options.
   *
   * @param uc
   * @param showFreeLoad Specifies if the free load should be displayed
   * @param hasCommand If <code>true</code>, it is indicated the the unit has the command
   * @return The NodeWrapper
   */
  public UnitContainerNodeWrapper createUnitContainerNodeWrapper(UnitContainer uc,
      boolean showFreeLoad, boolean hasCommand) {
    return createUnitContainerNodeWrapper(uc, showFreeLoad, hasCommand, null);
  }

  /**
   * Creates a wrapper node for a unit container with extended options.
   *
   * @param uc
   * @param showFreeLoad Specifies if the free load should be displayed
   * @param hasCommand If <code>true</code>, it is indicated the the unit has the command
   * @return The NodeWrapper
   */
  public UnitContainerNodeWrapper createUnitContainerNodeWrapper(UnitContainer uc,
      boolean showFreeLoad, boolean hasCommand, String prefix) {
    UnitContainerNodeWrapper ucnw =
        new UnitContainerNodeWrapper(uc, showFreeLoad, hasCommand, prefix);
    init(ucnw, NodeWrapperFactory.UNITCONTAINER);

    return ucnw;
  }

  /**
   * Creates a wrapper node for a unit. The text will consist only of the unit's name and id
   *
   * @param unit The unit
   * @return The created node wrapper.
   */
  public UnitNodeWrapper createUnitNodeWrapper(Unit unit) {
    return createUnitNodeWrapper(unit, null, -1, -1);
  }

  /**
   * Creates a wrapper node for a unit. The text is generated from the unit's name and ID, and num
   * without mod or prefix.
   *
   * @param unit The unit
   * @param num The number of persons
   * @return The created node wrapper.
   */
  public UnitNodeWrapper createUnitNodeWrapper(Unit unit, long num) {
    return createUnitNodeWrapper(unit, null, num, -1);
  }

  /**
   * Creates a wrapper node for a unit. The text is generated from the unit's name and ID, and num
   * and mod without prefix.
   *
   * @param unit The unit
   * @param num The number of persons
   * @param mod The modified number of persons.
   * @return The created node wrapper.
   */
  public UnitNodeWrapper createUnitNodeWrapper(Unit unit, long num, int mod) {
    return createUnitNodeWrapper(unit, null, num, mod);
  }

  /**
   * Creates a wrapper node for a unit. The text is generated from the prefix, the unit's name and
   * ID, and num and mod.
   *
   * @param unit The unit
   * @param prfx A prefix which is displayed in front of the unit's name.
   * @param num The number of persons
   * @param mod The modified number of persons.
   * @return The created node wrapper.
   */
  public UnitNodeWrapper createUnitNodeWrapper(Unit unit, String prfx, long num, int mod) {
    UnitNodeWrapper unw = new UnitNodeWrapper(unit, prfx, num, mod);
    init(unw, NodeWrapperFactory.UNIT);

    return unw;
  }

  /**
   * Creates a wrapper node for a unit. The text will not be generated but taken from the argument.
   *
   * @param unit The unit
   * @param text The node will use this text.
   * @return The created node wrapper.
   */
  public UnitNodeWrapper createUnitNodeWrapper(Unit unit, String text) {
    UnitNodeWrapper unw = new UnitNodeWrapper(unit, text);
    init(unw, NodeWrapperFactory.UNIT);

    return unw;
  }

  public UnitListNodeWrapper createUnitListNodeWrapper(String text, String clipBoardValue,
      Collection<Unit> units, String icon) {
    UnitListNodeWrapper wrapper = new UnitListNodeWrapper(text, clipBoardValue, units, icon);
    init(wrapper, NodeWrapperFactory.SIMPLE);

    return wrapper;
  }

  /**
   * Create a node for a potion.
   */
  public PotionNodeWrapper createPotionNodeWrapper(Potion potion) {
    PotionNodeWrapper pnw = new PotionNodeWrapper(potion);
    init(pnw, NodeWrapperFactory.POTION);

    return pnw;
  }

  /**
   * Create a node for a potion with the given name and appendix to the name.
   */
  public PotionNodeWrapper createPotionNodeWrapper(Potion potion, String name, String postfix) {
    PotionNodeWrapper pnw = new PotionNodeWrapper(potion, name, postfix);
    init(pnw, NodeWrapperFactory.POTION);

    return pnw;
  }

  /**
   * Create a node for a spell.
   */
  public DefaultNodeWrapper createSpellNodeWrapper(Spell spell) {
    SpellNodeWrapper snw = new SpellNodeWrapper(spell);
    init(snw, NodeWrapperFactory.SPELL);

    return snw;
  }

  /**
   * Create a node for a combat spell.
   */
  public DefaultNodeWrapper createSpellNodeWrapper(CombatSpell spell) {
    SpellNodeWrapper snw = new SpellNodeWrapper(spell);
    init(snw, NodeWrapperFactory.SPELL);

    return snw;
  }

  /**
   * Create a node for an item without unit.
   */
  public ItemNodeWrapper createItemNodeWrapper(Item item) {
    return createItemNodeWrapper(null, createStatItem(item));
  }

  /**
   * Create a StatItem matching an items type and amount
   */
  public Units.StatItem createStatItem(Item item) {
    return new Units.StatItem(item.getItemType(), item.getAmount());
  }

  /**
   * Create a node for an item. Unmodified amount is taken from a unit
   */
  public ItemNodeWrapper createItemNodeWrapper(Unit unit, Units.StatItem modItem) {
    ItemNodeWrapper inw = new ItemNodeWrapper(unit, modItem, -1);
    init(inw, NodeWrapperFactory.ITEM);

    return inw;
  }

  /**
   * Create a node for an item. The unmodified amount is not taken from the unit but is given
   * explicitly.
   */
  public ItemNodeWrapper
      createItemNodeWrapper(Unit unit, Units.StatItem item, long unmodifiedAmount) {
    ItemNodeWrapper inw = new ItemNodeWrapper(unit, item, unmodifiedAmount);
    init(inw, NodeWrapperFactory.ITEM);

    return inw;
  }

  /**
   * Create a node for an item category with explicit name.
   * 
   * @param category
   * @param amount
   * @param catName
   */
  public ItemCategoryNodeWrapper createItemNodeWrapper(ItemCategory category, int amount,
      String catName) {
    return new ItemCategoryNodeWrapper(category, amount, catName);
  }

  /**
   * Create a node for a skill.
   */
  public SkillNodeWrapper createSkillNodeWrapper(Unit unit, Skill skill, Skill modSkill) {
    SkillNodeWrapper snw = new SkillNodeWrapper(unit, skill, modSkill);
    init(snw, NodeWrapperFactory.SKILL);

    return snw;
  }

  /**
   * Create a node for a group.
   */
  public GroupNodeWrapper createGroupNodeWrapper(Group group) {
    GroupNodeWrapper gnw = new GroupNodeWrapper(group);
    init(gnw, NodeWrapperFactory.GROUP);

    return gnw;
  }

  /**
   * Creates new SimpleNodeWrapper with one icon and given text.
   *
   * @param obj
   * @param text
   * @param icon may be <code>null</code>
   */
  public SimpleNodeWrapper createSimpleNodeWrapper(Object obj, String text, String icon) {
    SimpleNodeWrapper snw = new SimpleNodeWrapper(obj, text, icon);
    init(snw, NodeWrapperFactory.SIMPLE);

    return snw;
  }

  /**
   * Creates new SimpleNodeWrapper with several icons and given text.
   *
   * @param obj
   * @param text
   * @param icons may be <code>null</code>
   */
  public SimpleNodeWrapper
      createSimpleNodeWrapper(Object obj, String text, Collection<String> icons) {
    SimpleNodeWrapper snw = new SimpleNodeWrapper(obj, text, icons);
    init(snw, NodeWrapperFactory.SIMPLE);

    return snw;
  }

  /**
   * Creates new SimpleNodeWrapper with one icon and generated text.
   *
   * @param obj
   * @param icons may be <code>null</code>
   */
  public SimpleNodeWrapper createSimpleNodeWrapper(Object obj, String icons) {
    SimpleNodeWrapper snw = new SimpleNodeWrapper(obj, icons);
    init(snw, NodeWrapperFactory.SIMPLE);

    return snw;
  }

  /**
   * Creates new SimpleNodeWrapper with multiple icons and generated text.
   *
   * @param obj
   * @param icons may be <code>null</code>
   */
  public SimpleNodeWrapper createSimpleNodeWrapper(Object obj, Collection<String> icons) {
    SimpleNodeWrapper snw = new SimpleNodeWrapper(obj, icons);
    init(snw, NodeWrapperFactory.SIMPLE);

    return snw;
  }

  // /**
  // * Creates new SimpleNodeWrapper
  // */
  // public SimpleNodeWrapper createSimpleNodeWrapper(Object text, String icons, String clipValue) {
  // SimpleNodeWrapper snw = new SimpleNodeWrapper(text, icons, clipValue);
  // init(snw, NodeWrapperFactory.SIMPLE);
  // return snw;
  // }

  /**
   * DOCUMENT-ME
   *
   * @see magellan.client.swing.context.ContextObserver#contextDataChanged()
   */
  public void contextDataChanged() {
    NodeWrapperFactory.log.info("Context data changed. " + source);

    if (source != null) {
      source.updateTree(this);
    }
  }

  // ///////////////////////////
  // PreferencesAdapter code //
  // ///////////////////////////
  public PreferencesAdapter createPreferencesAdapter() {
    return new NodeWrapperFactoryPreferences(adapters);
  }

  class NodeWrapperFactoryPreferences extends JTabbedPane implements PreferencesAdapter {
    List<PreferencesAdapter> myAdapters;

    protected NodeWrapperFactoryPreferences(NodeWrapperDrawPolicy adapters[]) {
      myAdapters = new LinkedList<PreferencesAdapter>();

      for (NodeWrapperDrawPolicy adapter : adapters) {
        if (adapter != null) {
          PreferencesAdapter pref = adapter.createPreferencesAdapter();

          if (pref != null) {
            addTab(pref.getTitle(), pref.getComponent());
            myAdapters.add(pref);
          }
        }
      }

      // try to enforce only one column
      // setPreferredSize(null);
      //
      // java.awt.Dimension dim = getPreferredSize();
      // int tabHeight = getTabCount() * 30; // just approximate since there are no
      // // public functions :-(
      //
      // if (dim.height < tabHeight) {
      // dim.height = tabHeight;
      // setPreferredSize(dim);
      // }
    }

    public void initPreferences() {
      // TODO: implement it
    }

    /**
     * @see magellan.client.swing.preferences.PreferencesAdapter#applyPreferences()
     */
    public void applyPreferences() {
      for (PreferencesAdapter pref : myAdapters) {
        pref.applyPreferences();
      }
    }

    /**
     * @see magellan.client.swing.preferences.PreferencesAdapter#getComponent()
     */
    public java.awt.Component getComponent() {
      return this;
    }

    /**
     * @see magellan.client.swing.preferences.PreferencesAdapter#getTitle()
     */
    public String getTitle() {
      return title;
    }
  }

}
