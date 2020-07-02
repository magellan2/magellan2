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

package magellan.client;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import magellan.client.desktop.DesktopEnvironment;
import magellan.client.desktop.Initializable;
import magellan.client.desktop.MagellanDesktop;
import magellan.client.desktop.ShortcutListener;
import magellan.client.event.EventDispatcher;
import magellan.client.event.OrderConfirmEvent;
import magellan.client.event.OrderConfirmListener;
import magellan.client.event.SelectionEvent;
import magellan.client.event.SelectionListener;
import magellan.client.event.TempUnitEvent;
import magellan.client.event.TempUnitListener;
import magellan.client.preferences.RegionOverviewPreferences;
import magellan.client.swing.InternationalizedDataPanel;
import magellan.client.swing.MenuProvider;
import magellan.client.swing.context.UnitContainerContextFactory;
import magellan.client.swing.context.UnitContextFactory;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.client.swing.preferences.PreferencesFactory;
import magellan.client.swing.tree.BorderNodeWrapper;
import magellan.client.swing.tree.CellRenderer;
import magellan.client.swing.tree.ContextManager;
import magellan.client.swing.tree.CopyTree;
import magellan.client.swing.tree.FactionNodeWrapper;
import magellan.client.swing.tree.GroupNodeWrapper;
import magellan.client.swing.tree.IslandNodeWrapper;
import magellan.client.swing.tree.NodeWrapperFactory;
import magellan.client.swing.tree.RegionNodeWrapper;
import magellan.client.swing.tree.SimpleNodeWrapper;
import magellan.client.swing.tree.TreeHelper;
import magellan.client.swing.tree.TreeUpdate;
import magellan.client.swing.tree.UnitContainerNodeWrapper;
import magellan.client.swing.tree.UnitNodeWrapper;
import magellan.client.utils.SelectionHistory;
import magellan.client.utils.TreeBuilder;
import magellan.library.Alliance;
import magellan.library.AllianceGroup;
import magellan.library.Building;
import magellan.library.EntityID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Group;
import magellan.library.ID;
import magellan.library.Named;
import magellan.library.Region;
import magellan.library.Ship;
import magellan.library.TempUnit;
import magellan.library.Unique;
import magellan.library.Unit;
import magellan.library.ZeroUnit;
import magellan.library.event.GameDataEvent;
import magellan.library.event.UnitChangeEvent;
import magellan.library.event.UnitChangeListener;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.utils.PropertiesHelper;
import magellan.library.utils.Resources;
import magellan.library.utils.Units;
import magellan.library.utils.comparator.BestSkillComparator;
import magellan.library.utils.comparator.IDComparator;
import magellan.library.utils.comparator.NameComparator;
import magellan.library.utils.comparator.SkillComparator;
import magellan.library.utils.comparator.SkillRankComparator;
import magellan.library.utils.comparator.SortIndexComparator;
import magellan.library.utils.comparator.TopmostRankedSkillComparator;
import magellan.library.utils.comparator.UnitSkillComparator;
import magellan.library.utils.logging.Logger;

/**
 * A panel containing a tree with all regions units, ships, etc.
 *
 * @author $Author: $
 * @version $Revision: 382 $
 */
public class EMapOverviewPanel extends InternationalizedDataPanel implements TreeSelectionListener,
    TreeExpansionListener, SelectionListener, OrderConfirmListener, PreferencesFactory,
    TempUnitListener, ShortcutListener, ChangeListener, TreeUpdate, MenuProvider, Initializable {
  private static final Logger log = Logger.getInstance(EMapOverviewPanel.class);

  // GUI elements
  private JSplitPane sppTreeHistory;
  private CopyTree tree;
  private DefaultTreeModel treeModel;
  private DefaultMutableTreeNode rootNode;
  private JScrollPane scpTree;
  private JList lstHistory;
  private JScrollPane scpHistory;

  // node maps, mapping the object ids to their tree nodes
  private Map<ID, TreeNode> regionNodes = new Hashtable<ID, TreeNode>();
  private Map<ID, TreeNode> unitNodes = new Hashtable<ID, TreeNode>();
  private Map<ID, TreeNode> buildingNodes = new Hashtable<ID, TreeNode>();
  private Map<ID, TreeNode> shipNodes = new Hashtable<ID, TreeNode>();

  // /**
  // * relation map, mapping unit's id to its relations; used to store the last known relations and
  // * update related units when relations change
  // */
  // private Map<ID, Set<TransferRelation>> lastUnitRelations =
  // new HashMap<ID, Set<TransferRelation>>();
  private boolean ignoreTreeSelections = false;

  // region with previously selected item
  private Unique activeObject;
  private List<Object> selectedObjects = new ArrayList<Object>();
  private List<List<Object>> contexts = new ArrayList<List<Object>>();

  // needed by FactionNodeWrapper to determine the active alliances
  // keys: FactionIDs, values: Alliance-objects
  private Map<EntityID, Alliance> activeAlliances = new Hashtable<EntityID, Alliance>();

  // This flag is added for performance reasons
  // to avoid unnecessary updates to the activeAlliances map
  private boolean activeAlliancesAreDefault = false;

  // shortcuts
  private List<KeyStroke> shortcuts;

  // factory for tree NodeWrapper
  private NodeWrapperFactory nodeWrapperFactory;

  // the context menu manager
  private ContextManager contextManager;

  // tree expand/collapse properties

  /** DOCUMENT-ME */
  public static final int EXPAND_FLAG = 1;

  /** DOCUMENT-ME */
  public static final int EXPAND_IFINSIDE_FLAG = 2;
  private int expandMode = EMapOverviewPanel.EXPAND_FLAG | (3 << 2);
  private int expandTrustlevel = Faction.TL_PRIVILEGED;

  /** DOCUMENT-ME */
  public static final int COLLAPSE_FLAG = 1;

  /** DOCUMENT-ME */
  public static final int COLLAPSE_ONLY_EXPANDED = 2;
  private int collapseMode = EMapOverviewPanel.COLLAPSE_FLAG
      | EMapOverviewPanel.COLLAPSE_ONLY_EXPANDED | (3 << 2);
  private List<TreeNode> lastExpanded = new LinkedList<TreeNode>();
  private Set<TreeNode> collapsedNodes = new HashSet<TreeNode>();
  private Set<TreeNode> collapseInfo = new HashSet<TreeNode>();
  private Set<TreeNode> expandInfo = new HashSet<TreeNode>();
  private Set<TreePath> selectionTransfer = new HashSet<TreePath>();

  private UnitChangeListener unitChangeListener;

  protected Object lastCause;
  protected Region lastRegion;
  private static final Comparator<Unique> idCmp = IDComparator.DEFAULT;
  private static final Comparator<Named> nameCmp = new NameComparator(EMapOverviewPanel.idCmp);

  /** @deprecated Use {@link MagellanDesktop#OVERVIEW_IDENTIFIER} instead */
  @Deprecated
  public static final String IDENTIFIER = MagellanDesktop.OVERVIEW_IDENTIFIER;

  /**
   * Creates a new EMapOverviewPanel object.
   *
   * @param d
   * @param p
   */
  public EMapOverviewPanel(EventDispatcher d, GameData data, Properties p) {
    super(d, data, p);

    unitChangeListener = new UnitChangeListener() {
      public void unitChanged(UnitChangeEvent event) {
        if (lastCause != event.getCause() || lastRegion != event.getUnit().getRegion()) {
          lastCause = event.getCause();
          lastRegion = event.getUnit().getRegion();
          update(event.getUnit());
        }
      }
    };

    loadExpandProperties();
    loadCollapseProperty();

    nodeWrapperFactory =
        new NodeWrapperFactory(settings, "EMapOverviewPanel", Resources
            .get("emapoverviewpanel.wrapperfactory.title"));
    nodeWrapperFactory.setSource(this);

    // to get the pref-adapter
    // nodeWrapperFactory
    // .createUnitNodeWrapper(MagellanFactory.createUnit(UnitID.createUnitID(0, 10)));

    SelectionHistory.addListener(this);
    d.addSelectionListener(this);
    d.addOrderConfirmListener(this);
    d.addTempUnitListener(this);

    // create tree and add it to this panel within a scroll pane
    rootNode = new DefaultMutableTreeNode(null);
    treeModel = new DefaultTreeModel(rootNode);
    tree = new CopyTree(treeModel);
    tree.setRootVisible(false);

    // pavkovic 2005.05.28: enable handle for root nodes
    tree.setShowsRootHandles(true);
    tree.setScrollsOnExpand(false);
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

    // set the tree to have a large model (almost always much data...)
    tree.setLargeModel(true);

    tree.addTreeSelectionListener(this);
    tree.addTreeExpansionListener(this);

    ToolTipManager.sharedInstance().registerComponent(tree);
    scpTree = new JScrollPane(tree);

    // ClearLook suggests to remove border
    scpTree.setBorder(null);

    // tree uses different cell renderer
    tree.setCellRenderer(new CellRenderer(getMagellanContext()));

    // init context manager with different node wrappers
    contextManager = new ContextManager(tree, dispatcher);
    contextManager.putSimpleObject(UnitNodeWrapper.class, new UnitContextFactory());

    UnitContainerContextFactory conMenu = new UnitContainerContextFactory(settings);
    contextManager.putSimpleObject(FactionNodeWrapper.class, conMenu);
    contextManager.putSimpleObject(RegionNodeWrapper.class, conMenu);
    contextManager.putSimpleObject(UnitContainerNodeWrapper.class, conMenu);
    contextManager.putSimpleObject(IslandNodeWrapper.class, conMenu);

    // history list
    lstHistory = new JList();
    lstHistory.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
          Object val = lstHistory.getSelectedValue();
          if (val != null) {
            dispatcher.fire(SelectionEvent.create(lstHistory, SelectionHistory
                .getHistory(lstHistory.getSelectedIndex()).event));
          }
        }
      }
    });
    SelectionHistory.ignoreSource(lstHistory);
    SelectionHistory.setMaxSize(SelectionHistory.HISTORY_SIZE);
    scpHistory = new JScrollPane(lstHistory);

    // ClearLook suggests to remove border
    scpHistory.setBorder(null);

    // split pane
    sppTreeHistory = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scpTree, scpHistory);
    sppTreeHistory.setOneTouchExpandable(true);
    sppTreeHistory.setDividerLocation(Integer.parseInt(settings.getProperty(
        "EMapOverviewPanel.treeHistorySplit", "100")));

    // ClearLook suggests to remove border
    sppTreeHistory.setBorder(null);

    // add components to this panel
    setLayout(new GridLayout(1, 0));
    this.add(sppTreeHistory);

    // initialize shortcuts
    shortcuts = new ArrayList<KeyStroke>(8);

    // 0-1: Focus
    shortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.CTRL_MASK));
    shortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.ALT_MASK));

    // 2-4: Other CTRL shortcuts
    // shortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_N,KeyEvent.CTRL_MASK));
    // shortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_B,KeyEvent.CTRL_MASK));
    shortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, InputEvent.CTRL_MASK));
    shortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, InputEvent.CTRL_MASK));

    // 5-6: Other ALT shortcuts
    shortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.CTRL_MASK));
    shortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.CTRL_MASK));

    // register for shortcuts
    DesktopEnvironment.registerShortcutListener(this);

    // (stm) we listen to UnitChangeEvents now
    // // register order change listener
    // d.addUnitOrdersListener(new UnitWrapperUpdater());
  }

  /**
   * Returns the factory that creates node wrappers.
   */
  public NodeWrapperFactory getNodeWrapperFactory() {
    return nodeWrapperFactory;
  }

  /**
   * Returns the component displaying the overview tree.
   */
  public Component getOverviewComponent() {
    return scpTree;
  }

  /**
   * Returns the component displaying the history.
   */
  public Component getHistoryComponent() {
    return scpHistory;
  }

  /**
   * GameDataChanged event handler routine updating the tree. Note: It is significant for the
   * valueChanged() method, how deep the different node types are nested.
   *
   * @see magellan.client.swing.InternationalizedDataPanel#gameDataChanged(magellan.library.event.GameDataEvent)
   */
  @Override
  public void gameDataChanged(GameDataEvent e) {
    GameData oldData = getGameData();
    setGameData(e.getGameData());

    // activeObject is from the old report. Convert it to the corresponding object in the new report
    if (activeObject instanceof Unit) {
      activeObject = getGameData().getUnit(activeObject.getID());
    } else if (activeObject instanceof Building) {
      activeObject = getGameData().getBuilding(activeObject.getID());
    } else if (activeObject instanceof Ship) {
      activeObject = getGameData().getShip(activeObject.getID());
    } else if (activeObject instanceof Region) {
      activeObject = getGameData().getRegion(((Region) activeObject).getCoordinate());
    } else {
      activeObject = null;
    }

    if (oldData != getGameData()) {
      // clear the history
      SelectionHistory.clear();
      setLstHistory();
    }

    rebuildTree();

    // initialize activeAlliances-Map
    setDefaultAlliances();
  }

  @Override
  public void setGameData(GameData data) {
    getGameData().removeUnitChangeListener(unitChangeListener);
    super.setGameData(data);
    data.addUnitChangeListener(unitChangeListener);
  }

  /**
   * Rebuild the region tree from scratch.
   */
  public void rebuildTree() {
    Unique oldActiveObject = activeObject;

    // clear node maps
    regionNodes.clear();
    unitNodes.clear();
    buildingNodes.clear();
    shipNodes.clear();
    rootNode.removeAllChildren();

    // // clear relation map
    // lastUnitRelations.clear();

    // clear other buffers
    selectedObjects.clear();
    contexts.clear();
    activeObject = null;
    lastExpanded.clear();

    // if((data != null) && (data.getRegions() != null)) {
    // preprocess regions to have relations
    // pavkovic 2003.12.21: moved to RegionNodeWrapper, perhaps this helps
    // for(Iterator iter = data.getRegions().iterator();
    // iter.hasNext();) {
    // ((Region) iter.next()).refreshUnitRelations();
    // }
    // }
    // initialize variables used in while loop

    // TODO: this needs explanations
    boolean createIslandNodes =
        PropertiesHelper.getBoolean(settings, "EMapOverviewPanel.displayIslands", true)
            && PropertiesHelper.getBoolean(settings, "EMapOverviewPanel.sortRegions", true)
            && settings.getProperty("EMapOverviewPanel.sortRegionsCriteria", "coordinates")
                .equalsIgnoreCase("islands");

    boolean sortShipUnderUnitParent =
        PropertiesHelper.getBoolean(settings, "EMapOverviewPanel.sortShipUnderUnitParent", true);
    boolean showHomeless =
        PropertiesHelper.getBoolean(settings, "EMapOverviewPanel.showHomeless", false);

    TreeBuilder treeBuilder = getTreeBuilder();
    treeBuilder.setSortShipUnderUnitParent(sortShipUnderUnitParent);

    int displayMode = TreeBuilder.UNITS;
    if (PropertiesHelper.getBoolean(settings, "EMapOverviewPanel.treeBuilderWithBuildings", true)) {
      displayMode = displayMode | TreeBuilder.BUILDINGS;
    }
    if (PropertiesHelper.getBoolean(settings, "EMapOverviewPanel.treeBuilderWithShips", true)) {
      displayMode = displayMode | TreeBuilder.SHIPS;
    }
    if (PropertiesHelper.getBoolean(settings, "EMapOverviewPanel.treeBuilderWithComments", true)) {
      displayMode = displayMode | TreeBuilder.COMMENTS;
    }

    if (createIslandNodes) {
      displayMode = displayMode | TreeBuilder.CREATE_ISLANDS;
    }

    if (showHomeless) {
      displayMode = displayMode | TreeBuilder.SHOW_HOMELESS;
    }

    treeBuilder.setDisplayMode(displayMode);

    // creation of Comparator outsourced to Comparator
    // getUnitSorting(java.util.Properties)
    treeBuilder.setUnitComparator(EMapOverviewPanel.getUnitSorting(settings));
    treeBuilder.setTreeStructure(EMapOverviewPanel.getTreeStructure(settings));

    treeBuilder.buildTree(rootNode, getGameData());

    // for (Unit u : data.getUnits()) {
    // List<TransferRelation> relations = u.getRelations(TransferRelation.class);
    // if (relations != null && !relations.isEmpty()) {
    // lastUnitRelations.put(u.getID(), new HashSet<TransferRelation>(relations));
    // }
    // }

    tree.setShowsRootHandles(PropertiesHelper.getBoolean(settings,
        "EMapOverviewPanel.treeRootHandles", true));

    treeModel.reload();

    if (oldActiveObject != null) {
      dispatcher.fire(SelectionEvent.create(treeModel, oldActiveObject, SelectionEvent.ST_DEFAULT));
    } else {
      dispatcher.fire(SelectionEvent.create(this));
    }
  }

  private void setLstHistory() {
    // Object[] historyItems = new Object[SelectionHistory.getHistory().size()];
    // int i = 0;
    // for (SelectionEntry se : SelectionHistory.getHistory()){
    // historyItems[i++]=se.getActiveObject();
    // }
    //
    // lstHistory.setListData(historyItems);
    lstHistory.setListData(SelectionHistory.getHistory().toArray());
    // if (lstHistory.getModel().getSize()>0)
    // lstHistory.setSelectedIndex(0);
  }

  private TreeBuilder myTreeBuilder;

  private boolean supressSelections = false;

  private TreeBuilder getTreeBuilder() {
    if (myTreeBuilder == null) {
      myTreeBuilder = createTreeBuilder();
    }
    return myTreeBuilder;
  }

  /**
   * Retrieve a Comparator to sort the units according to the settings.
   *
   * @param settings
   */
  public static Comparator<? super Unit> getUnitSorting(Properties settings) {
    // create Comparator used for unit sorting
    String criteria = settings.getProperty("EMapOverviewPanel.sortUnitsCriteria", "skills");

    // used as the comparator on the lowest structure level
    Comparator<? super Unit> cmp = null;

    if (criteria.equals("skills")) {
      if (settings.getProperty("EMapOverviewPanel.useBestSkill", "true").equalsIgnoreCase("true")) {
        // ToStringComparator<Skill> nameComparator = new ToStringComparator<Skill>(null);
        SkillRankComparator skillTypeRankComparator = new SkillRankComparator(null, settings);
        // SkillTypeComparator<Skill> skillTypeComparator = new
        // SkillTypeComparator<Skill>(skillTypeRankComparator, null);
        BestSkillComparator bestSkillComparator =
            new BestSkillComparator(SkillComparator.skillCmp, skillTypeRankComparator,
                SkillComparator.skillCmp);
        cmp = new UnitSkillComparator(bestSkillComparator, EMapOverviewPanel.idCmp);
      } else {
        cmp =
            new UnitSkillComparator(new TopmostRankedSkillComparator(null, settings),
                EMapOverviewPanel.idCmp);
      }
    } else if (criteria.equals("names")) {
      cmp = EMapOverviewPanel.nameCmp;
    } else {
      cmp = new SortIndexComparator<Unit>(EMapOverviewPanel.idCmp);
    }

    // get an array of ints out of the definition string:
    int treeStructure[] = EMapOverviewPanel.getTreeStructure(settings);

    return TreeHelper.buildComparator(cmp, treeStructure);
  }

  /**
   * Retrieve the structure of the unit tree according to the settings
   *
   * @param settings
   */
  private static int[] getTreeStructure(Properties settings) {
    String criteria =
        settings.getProperty("EMapOverviewPanel.treeStructure", " " + TreeHelper.FACTION + " "
            + TreeHelper.GROUP);
    StringTokenizer tokenizer = new StringTokenizer(criteria);
    int treeStructure[] = new int[tokenizer.countTokens()];
    int i = 0;

    while (tokenizer.hasMoreTokens()) {
      try {
        String s = tokenizer.nextToken();
        treeStructure[i] = Integer.parseInt(s);
        i++;
      } catch (NumberFormatException e) {
        throw new IllegalStateException("properties corrupt", e);
      }
    }

    // System.out.print("treeStructure: ");
    // for (i = 0; i < treeStructure.length; i++) {
    // System.out.print(treeStructure[i] + " ");
    // }
    // System.out.println("");
    return treeStructure;
  }

  /**
   * TreeSelection event handler, notifies event listeners.
   *
   * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
   */
  public void valueChanged(TreeSelectionEvent tse) {
    if (ignoreTreeSelections)
      return;

    DefaultMutableTreeNode firstNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
    TreePath paths[] = tse.getPaths();

    // search for nodes which have been selected/deselected because of collapse
    DefaultMutableTreeNode node = null;
    boolean removed = false;

    for (TreePath path : paths) {
      if (selectionTransfer.contains(path)) {
        removed = true;

        break;
      }
    }

    if (removed) { // some selection transfer detected
      selectionTransfer.clear();

      /**
       * Do not return, since tree selections will not be cleared after a user collapses a node that
       * contains selected subelements.
       */

      // return;
      /*
       * Note: This is perhaps not the best way because there may be other nodes than that from
       * collapse. But "normally" this shouldn't happen.
       */
    } else if (!selectionTransfer.isEmpty()) {
      // debug
      removed = !(!removed);
    }

    /**
     * DETERMINE ACTIVE OBJECT :
     */
    node = firstNode;

    if (node == null) {
      // return; // there may be an inconsistent state with active object
      // We may not return, when firstNode is null
      // This also happens, when the selection is deleted.
      // But in this case we have to continue to do that deletion.
    } else {
      Object o = node.getUserObject();

      // region node selected?
      if (o instanceof RegionNodeWrapper) {
        activeObject = ((RegionNodeWrapper) o).getRegion();

        if (!activeAlliancesAreDefault) {
          setDefaultAlliances();
          tree.repaint();
        }
      } else if (o instanceof FactionNodeWrapper) {
        // faction node selected?
        activeObject = ((FactionNodeWrapper) o).getFaction();

        Faction f = (Faction) activeObject;

        setAlliances(f.getAllies(), f);
      } else if (o instanceof GroupNodeWrapper) {
        // group node selected?
        Group g = ((GroupNodeWrapper) o).getGroup();
        activeObject = g;

        setAlliances(g.allies(), g.getFaction());
      } else if (o instanceof UnitNodeWrapper) {
        // unit node selected?
        activeObject = ((UnitNodeWrapper) o).getUnit();

        Group g = ((Unit) activeObject).getGroup();
        if (g == null) {
          Faction f = ((Unit) activeObject).getFaction();
          if (f != null) {
            setAlliances(f.getAllies(), f);
          }
        } else {
          setAlliances(g.allies(), g.getFaction());
        }
      } else if (o instanceof UnitContainerNodeWrapper) {
        // building node selected?
        if (((UnitContainerNodeWrapper) o).getUnitContainer() instanceof Building
            || ((UnitContainerNodeWrapper) o).getUnitContainer() instanceof Ship) {
          activeObject = ((UnitContainerNodeWrapper) o).getUnitContainer();

        }

        if (!activeAlliancesAreDefault) {
          setDefaultAlliances();
          tree.repaint();
        }
      } else if (o instanceof BorderNodeWrapper) {
        // border node selected?
        activeObject = ((BorderNodeWrapper) o).getBorder();

        if (!activeAlliancesAreDefault) {
          setDefaultAlliances();
          tree.repaint();
        }
      } else if (o instanceof IslandNodeWrapper) {
        // island node selected?
        activeObject = ((IslandNodeWrapper) o).getIsland();

        if (!activeAlliancesAreDefault) {
          setDefaultAlliances();
          tree.repaint();
        }
      } else if (o instanceof SimpleNodeWrapper) {
        activeObject = null;
      }
    }

    /**
     * UPDATE SELECTED OBJECTS :
     */
    for (TreePath path : paths) {
      if (path == null) {
        continue;
      }

      node = (DefaultMutableTreeNode) path.getLastPathComponent();

      Object o = getNodeSubject(node);

      if (o instanceof Unique) {
        if (tse.isAddedPath(path)) {
          selectedObjects.add(o);
          contexts.add(translate(path));
        } else {
          Iterator<Object> it = selectedObjects.iterator();
          Iterator<?> it2 = contexts.iterator();
          for (; it.hasNext();) {
            it2.next();
            if (it.next().equals(o)) {
              it.remove();
              it2.remove();
            }
          }
        }
      }
    }

    if (!supressSelections) {
      ArrayList<List<Object>> selectionContexts =
          new ArrayList<List<Object>>(selectedObjects.size());
      if (tree != null && tree.getSelectionPaths() != null) {
        for (TreePath path : tree.getSelectionPaths()) {
          selectionContexts.add(translate(path));
        }
      }

      dispatcher.fire(SelectionEvent.create(this, selectionContexts));
    }
  }

  private List<Object> translate(TreePath path) {
    ArrayList<Object> context = new ArrayList<Object>(path.getPathCount());
    for (Object o : path.getPath()) {
      context.add(getNodeSubject((DefaultMutableTreeNode) o));
    }
    return context;
  }

  /**
   * Returns the (Report) object that belongs to this node, for example a region, a unit or a group.
   *
   * @param node
   * @return
   */
  private Object getNodeSubject(DefaultMutableTreeNode node) {
    Object o = node.getUserObject();
    if (o == null)
      return null;

    if (o instanceof RegionNodeWrapper) {
      o = ((RegionNodeWrapper) o).getRegion();
    } else if (o instanceof FactionNodeWrapper) {
      o = ((FactionNodeWrapper) o).getFaction();
    } else if (o instanceof GroupNodeWrapper) {
      o = ((GroupNodeWrapper) o).getGroup();
    } else if (o instanceof UnitNodeWrapper) {
      o = ((UnitNodeWrapper) o).getUnit();
    } else if (o instanceof UnitContainerNodeWrapper) {
      o = ((UnitContainerNodeWrapper) o).getUnitContainer();
    } else if (o instanceof BorderNodeWrapper) {
      o = ((BorderNodeWrapper) o).getBorder();
    } else if (o instanceof IslandNodeWrapper) {
      o = ((IslandNodeWrapper) o).getIsland();
    } else if (o instanceof SimpleNodeWrapper) {
      o = ((SimpleNodeWrapper) o).getObject();
    } else {
      EMapOverviewPanel.log
          .warn("EMapOverviewPanel.valueChanged() : Type of the user object of a selected node is unknown:"
              + o);
    }
    return o;
  }

  /**
   * Sets the active alliance status that are used to paint the faction or group nodes in the tree.
   *
   * @param allies The alliances map to be used, my be <code>null</code>
   * @param f The faction whose alliances are used
   */
  private void setAlliances(Map<EntityID, Alliance> allies, Faction f) {
    if ((allies == null) && (activeAlliances.size() > 0)) {
      // can't determine new specific alliances
      // set default alliances
      activeAlliances.clear();
      activeAlliancesAreDefault = false;
    } else if ((allies != null) && !activeAlliances.equals(allies)) {
      // set new active alliances
      activeAlliances.clear();
      /* this is a hack, E3 alliance implies combat state */
      if (f.getAlliance() != null) {
        activeAlliances.putAll(allies);
      } else {
        for (Entry<EntityID, Alliance> entry : allies.entrySet()) {
          boolean helpCombat =
              Units.isAllied(f, entry.getValue().getFaction(), EresseaConstants.A_COMBAT, false);
          Alliance all =
              new Alliance(entry.getValue().getFaction(), entry.getValue().getState()
                  | (helpCombat ? EresseaConstants.A_COMBAT : 0));
          activeAlliances.put(entry.getKey(), all);
        }
      }
      activeAlliancesAreDefault = false;

      // add the selected group or faction to be able to show, on whose
      // alliances
      // the current colors of the icons of a faction node depend
      activeAlliances.put(f.getID(), new Alliance(f, Integer.MAX_VALUE));
    }

    tree.repaint();
  }

  // private Region getSelectedRegion(DefaultMutableTreeNode node) {
  // while ((node != null) && !(node.getUserObject() instanceof RegionNodeWrapper)) {
  // node = (DefaultMutableTreeNode) node.getParent();
  // }
  //
  // if ((node != null) && node.getUserObject() instanceof RegionNodeWrapper) {
  // return ((RegionNodeWrapper) node.getUserObject()).getRegion();
  // } else {
  // return null;
  // }
  // }
  //
  /**
   * Initialize interface
   */
  public void initComponent(String params) {
    boolean changed = false;
    for (StringTokenizer st = new StringTokenizer(params.replace('_', ' '), "|"); st
        .hasMoreTokens();) {
      String curParam = st.nextToken();
      int pos = curParam.indexOf('=');
      if (pos > 0) {
        String key = curParam.substring(0, pos);
        String value = curParam.substring(pos + 1);
        if ("EMapOverviewPanel.treeStructure".equals(key)) {
          settings.setProperty(key, value);
          changed = true;
        }
      }
    }
    if (changed) {
      rebuildTree();
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see com.eressea.demo.desktop.Initializable#getComponentConfiguration()
   */
  public String getComponentConfiguration() {
    StringBuffer sb = new StringBuffer();
    sb.append("EMapOverviewPanel.treeStructure");
    sb.append("=");
    sb.append(settings.getProperty("EMapOverviewPanel.treeStructure", " " + TreeHelper.FACTION
        + " " + TreeHelper.GROUP));
    return sb.toString().replace(' ', '_');
  }

  /**
   * Event handler for TreeExpansionEvents (recenters the tree if necessary)
   *
   * @see javax.swing.event.TreeExpansionListener#treeExpanded(javax.swing.event.TreeExpansionEvent)
   */
  public void treeExpanded(TreeExpansionEvent e) {
    DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
    Rectangle rec = tree.getPathBounds(new TreePath(node.getPath()));

    if (node.getChildCount() > 0) {
      TreePath path = new TreePath(((DefaultMutableTreeNode) node.getLastChild()).getPath());

      if (tree.getPathBounds(path) != null) {
        rec.add(tree.getPathBounds(path));
      }
    }

    SwingUtilities.invokeLater(new ScrollerRunnable(rec));
  }

  /**
   * Event handler for TreeCollapsedEvents
   *
   * @see javax.swing.event.TreeExpansionListener#treeCollapsed(javax.swing.event.TreeExpansionEvent)
   */
  public void treeCollapsed(TreeExpansionEvent e) {
    // nothing to do
  }

  /**
   * Change event handler, change the display status of the tree if an unit orderConfimation Status
   * changed
   *
   * @see magellan.client.event.OrderConfirmListener#orderConfirmationChanged(magellan.client.event.OrderConfirmEvent)
   */
  public void orderConfirmationChanged(OrderConfirmEvent e) {
    for (Unit unit : e.getUnits()) {
      treeModel.nodeChanged(unitNodes.get(unit.getID()));
    }

    // TODO(stm) I doubt that this is necessary
    // /**
    // * BUG JTree UI see OrderConfimEvent
    // *
    // * @author Fiete
    // */
    // if (e.changedToUnConfirmed()) {
    // tree.updateUI();
    // tree.treeDidChange();
    // }

    tree.validate();
    tree.repaint();
  }

  /**
   * Collections all information about nodes to be collapsed.
   */
  protected void collectCollapseInfo() {
    // only clear when not expanded-only, here use also old collapse info
    if ((collapseMode & EMapOverviewPanel.COLLAPSE_ONLY_EXPANDED) == 0) {
      collapseInfo.clear();
    }

    if ((collapseMode & EMapOverviewPanel.COLLAPSE_FLAG) != 0) {
      int depth = collapseMode >> 2;

      if ((collapseMode & EMapOverviewPanel.COLLAPSE_ONLY_EXPANDED) != 0) {
        if (lastExpanded.size() > 0) {
          collapseInfo.addAll(lastExpanded);
          lastExpanded.clear();
        }
      } else {
        Enumeration<?> e = rootNode.children();

        while (e.hasMoreElements()) {
          DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
          Object obj = node.getUserObject();

          if (obj instanceof IslandNodeWrapper) {
            collapseImpl(node, depth + 1, true, collapseInfo);
          } else {
            collapseImpl(node, depth, true, collapseInfo);
          }
        }
      }
    }
  }

  private void collapseImpl(DefaultMutableTreeNode node, int depth, boolean add,
      Set<TreeNode> infoSet) {
    if ((depth > 0) && (node.getChildCount() > 0)) {
      depth--;

      for (int i = 0; i < node.getChildCount(); i++) {
        collapseImpl((DefaultMutableTreeNode) node.getChildAt(i), depth, true, infoSet);
      }
    }

    // important to do this here because children look on parent's expand state
    TreePath path = new TreePath(node.getPath());

    if (tree.isExpanded(path)) {
      infoSet.add(node);
    }
  }

  protected void resetExpandInfo() {
    expandInfo.clear();
  }

  protected void addExpandInfo(DefaultMutableTreeNode node) {
    expandInfo.add(node);
  }

  protected void collectExpandInfo(Region region, DefaultMutableTreeNode node, TreePath path) {
    if ((expandMode & EMapOverviewPanel.EXPAND_FLAG) != 0) {
      if ((expandMode & EMapOverviewPanel.EXPAND_IFINSIDE_FLAG) != 0) {
        // search for privileged faction
        // note: Node searching is not nice, but the fastest way
        boolean found = false;
        Enumeration<?> enumeration = node.children();

        while (!found && enumeration.hasMoreElements()) {
          DefaultMutableTreeNode child = (DefaultMutableTreeNode) enumeration.nextElement();
          Object obj = child.getUserObject();

          if (obj instanceof FactionNodeWrapper) {
            Faction fac = ((FactionNodeWrapper) obj).getFaction();

            if (fac.getTrustLevel() >= expandTrustlevel) {
              found = true;
            }
          }
        }

        if (!found)
          return;
      }

      expandInfo.add(node);

      int eDepth = expandMode >> 2;

      if (eDepth > 0) {
        eDepth--;

        Enumeration<?> enumeration = node.children();
        boolean open;

        while (enumeration.hasMoreElements()) {
          open = false;

          DefaultMutableTreeNode child = (DefaultMutableTreeNode) enumeration.nextElement();
          Object obj = child.getUserObject();

          if (obj instanceof FactionNodeWrapper) {
            Faction fac = ((FactionNodeWrapper) obj).getFaction();

            if (fac.getTrustLevel() >= expandTrustlevel) {
              open = true;
            }
          }

          if (open) {
            expandImpl(child, eDepth, expandInfo);
          }
        }
      }
    }
  }

  private void expandImpl(DefaultMutableTreeNode node, int depth, Set<TreeNode> infoSet) {
    infoSet.add(node);

    if ((depth > 0) && (node.getChildCount() > 0)) {
      depth--;

      for (int i = 0; i < node.getChildCount(); i++) {
        expandImpl((DefaultMutableTreeNode) node.getChildAt(i), depth, infoSet);
      }
    }
  }

  protected void doExpandAndCollapse(Collection<TreePath> newSelection) {
    // filter collapse info that would be re-expanded
    Iterator<TreeNode> cIt = collapseInfo.iterator();
    Map<TreeNode, TreePath> pathsToCollapse = new HashMap<TreeNode, TreePath>();
    Map<TreeNode, TreePath> pathsToExpand = new HashMap<TreeNode, TreePath>();

    while (cIt.hasNext()) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) cIt.next();
      TreePath path = new TreePath(node.getPath());
      pathsToCollapse.put(node, path);

      Iterator<TreeNode> it2 = expandInfo.iterator();

      while (it2.hasNext()) {
        DefaultMutableTreeNode node2 = (DefaultMutableTreeNode) it2.next();

        if (!pathsToExpand.containsKey(node2)) {
          pathsToExpand.put(node2, new TreePath(node2.getPath()));
        }

        TreePath path2 = pathsToExpand.get(node2);

        if (path.isDescendant(path2)) {
          cIt.remove();

          // but save it for last expand on expand-only mode
          if ((collapseMode & EMapOverviewPanel.COLLAPSE_ONLY_EXPANDED) != 0) {
            lastExpanded.add((TreeNode) path.getLastPathComponent());
          }

          break;
        }
      }
    }

    // now expand
    Iterator<TreeNode> eIt = expandInfo.iterator();

    while (eIt.hasNext()) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) eIt.next();
      TreePath path = null;

      if (pathsToExpand.containsKey(node)) {
        path = pathsToExpand.get(node);
      } else {
        path = new TreePath(node.getPath());
      }

      checkPathToExpand(path);
    }

    // prepare the selection transfer
    selectionTransfer.clear();

    // sort out already selected objects
    Iterator<TreePath> sIt = newSelection.iterator();

    while (sIt.hasNext()) {
      TreePath path = sIt.next();

      if (tree.isPathSelected(path)) {
        sIt.remove();
      }
    }

    selectionTransfer.addAll(newSelection);

    // transfer the selection
    TreePath paths[] = selectionTransfer.toArray(new TreePath[0]);

    tree.setSelectionPaths(paths);

    // now collapse
    // (this should'nt trigger selection events any more)
    cIt = collapseInfo.iterator();

    while (cIt.hasNext()) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) cIt.next();
      TreePath path = pathsToCollapse.get(node);

      if (tree.isExpanded(path)) {
        tree.collapsePath(path);
        cIt.remove();
      }
    }
  }

  /**
   * Looks a path down from root to the leaf if to expand any segment
   */
  protected void checkPathToExpand(TreePath path) {
    if (path.getPathCount() > 1) {
      checkPathToExpand(path.getParentPath());
    }

    if (!tree.isExpanded(path)) {
      lastExpanded.add((TreeNode) path.getLastPathComponent());
      tree.expandPath(path);
    }
  }

  /**
   * Selection event handler, update the selection status of the tree. First the active object is
   * considered and selected in the tree if contained. After that selected objects are considered,
   * but only selection type is different from SelectionEvent.ST_REGIONS. In this case the tree
   * selection is set to the selected objects (as long as they are contained in the tree anyway).
   * Keep in mind, that this will produce the active object _NOT_ to be selected, if <code>selectedObjects
   * != null && !selectedObjects.contains(activeObject)</code> !!!
   *
   * @param se
   * @see magellan.client.event.SelectionListener#selectionChanged(magellan.client.event.SelectionEvent)
   */
  public void selectionChanged(SelectionEvent se) {
    // update the selection in the context manager
    if (se.getSelectionType() != SelectionEvent.ST_REGIONS) {
      if (se.getSelectedObjects() != null) {
        contextManager.setSelection(se);
      }
    }

    // try to prevent notification loops, i.e. that calling this
    // procedure results in a notification of the registered
    // listeners of this object
    if (se.getSource() == this)
      return;

    // clear current selection to avoid intermediate selections through
    // expand and collapse
    ignoreTreeSelections = true;
    tree.clearSelection();
    // ignoreTreeSelections = false;

    collectCollapseInfo();
    resetExpandInfo();

    Collection<TreePath> newSel = new LinkedList<TreePath>();

    /** HANDLE activeObject : change alliance settings and expand info */
    if (se.getActiveObject() instanceof Unique) {
      activeObject = (Unique) se.getActiveObject();

      // The path of the selected object (if contained in the tree)
      TreePath path = null;

      // region selected?
      if (activeObject instanceof ZeroUnit) {
        activeObject = ((ZeroUnit) activeObject).getRegion();
      }

      if (activeObject instanceof Region) {
        if (!activeAlliancesAreDefault) {
          setDefaultAlliances();
          tree.repaint();
        }

        Region i = (Region) activeObject;
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) regionNodes.get(i.getID());

        if (node != null) {
          path = new TreePath(node.getPath());
          newSel.add(path);
          collectExpandInfo(i, node, path);
        }
      } else
      // unit selected?
      if (activeObject instanceof Unit) {
        Unit i = (Unit) activeObject;
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) unitNodes.get(i.getID());

        if (node != null) {
          path = new TreePath(node.getPath());
          addExpandInfo(node);
        }

        Group g = i.getGroup();

        if (g == null) {
          setAlliances(i.getFaction().getAllies(), i.getFaction());
        } else {
          setAlliances(g.allies(), g.getFaction());
        }
      } else
      // ship selected?
      if (activeObject instanceof Ship) {
        Ship i = (Ship) activeObject;
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) shipNodes.get(i.getID());

        if (node != null) {
          path = new TreePath(node.getPath());
          addExpandInfo(node);
        }

        if (!activeAlliancesAreDefault) {
          setDefaultAlliances();
          tree.repaint();
        }
      } else
      // building selected?
      if (activeObject instanceof Building) {
        Building i = (Building) activeObject;
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) buildingNodes.get(i.getID());

        if (node != null) {
          path = new TreePath(node.getPath());
          addExpandInfo(node);
        }

        if (!activeAlliancesAreDefault) {
          setDefaultAlliances();
          tree.repaint();
        }
      }

      // the active object has to be selected in the tree :
      selectedObjects.clear();
      contexts.clear();

      // selectedObjects.add(activeObject);

      /**
       * The active object will be added to the selectedObjects automatically when it will be
       * selected in the tree This is done in doExpandAndCollapse()
       */
      if (path != null) {
        newSel.add(path);
      }

      supressSelections = true;
      doExpandAndCollapse(newSel);
      supressSelections = false;

      if (path != null) {
        // center on the selected item if it is outside the view port
        SwingUtilities.invokeLater(new ScrollerRunnable(tree.getPathBounds(path)));
      }
    }

    /**
     * HANDLE selectedObjects. Don't change anything, if selectedObjects == null or selection event
     * type is ST_REGIONS (which means that this is a selection of regions on the map or of regions
     * to be selected on the map. Keep in mind that selections on the map don't have anything to do
     * with selections in the tree).
     */
    if ((se.getSelectedObjects() != null) && (se.getSelectionType() != SelectionEvent.ST_REGIONS)) {
      selectedObjects.clear();
      selectedObjects.addAll(se.getSelectedObjects());
      contexts.clear();
      contexts.addAll(se.getContexts());
      ignoreTreeSelections = true;
      tree.clearSelection();

      for (Object o : selectedObjects) {
        DefaultMutableTreeNode node = null;

        if (o instanceof Region) {
          node = (DefaultMutableTreeNode) regionNodes.get(((Region) o).getID());
        } else if (o instanceof Ship) {
          node = (DefaultMutableTreeNode) shipNodes.get(((Ship) o).getID());
        } else if (o instanceof Building) {
          node = (DefaultMutableTreeNode) buildingNodes.get(((Building) o).getID());
        } else if (o instanceof Unit) {
          node = (DefaultMutableTreeNode) unitNodes.get(((Unit) o).getID());
        }

        if (node != null) {
          tree.addSelectionPath(new TreePath(node.getPath()));
        }
      }

    }
    ignoreTreeSelections = false;
    tree.repaint();
  }

  protected void expandSelected(Region region, DefaultMutableTreeNode node, TreePath path) {
    if ((expandMode & EMapOverviewPanel.EXPAND_FLAG) != 0) {
      if ((expandMode & EMapOverviewPanel.EXPAND_IFINSIDE_FLAG) != 0) {
        // search for privileged faction
        // note: Node searching is not nice, but the fastest way
        boolean found = false;
        Enumeration<?> enumeration = node.children();

        while (!found && enumeration.hasMoreElements()) {
          DefaultMutableTreeNode child = (DefaultMutableTreeNode) enumeration.nextElement();
          Object obj = child.getUserObject();

          if (obj instanceof FactionNodeWrapper) {
            Faction fac = ((FactionNodeWrapper) obj).getFaction();

            if (fac.getTrustLevel() >= expandTrustlevel) {
              found = true;
            }
          }
        }

        if (!found)
          return;
      }

      collapsedNodes.remove(node);

      if (!tree.isExpanded(path)) {
        lastExpanded.add(node);
        tree.expandPath(path);
      }

      int eDepth = expandMode >> 2;

      if (eDepth > 0) {
        Enumeration<?> enumeration = node.children();
        while (enumeration.hasMoreElements()) {

          DefaultMutableTreeNode child = (DefaultMutableTreeNode) enumeration.nextElement();
          Object obj = child.getUserObject();

          if (obj instanceof FactionNodeWrapper) {
            Faction fac = ((FactionNodeWrapper) obj).getFaction();

            if (fac.getTrustLevel() >= expandTrustlevel) {
              expandImpl(child, eDepth - 1);
            }
          }
        }
      }
    }
  }

  private void expandImpl(DefaultMutableTreeNode node, int depth) {
    TreePath path = new TreePath(node.getPath());

    if (!tree.isExpanded(path)) {
      tree.expandPath(path);
      lastExpanded.add(0, node);
    }

    collapsedNodes.remove(node);

    if ((depth > 0) && (node.getChildCount() > 0)) {
      depth--;

      for (int i = 0; i < node.getChildCount(); i++) {
        expandImpl((DefaultMutableTreeNode) node.getChildAt(i), depth);
      }
    }
  }

  protected void collapseSelected() {
    if ((collapseMode & EMapOverviewPanel.COLLAPSE_FLAG) != 0) {
      int depth = collapseMode >> 2;

      if ((collapseMode & EMapOverviewPanel.COLLAPSE_ONLY_EXPANDED) != 0) {
        if (lastExpanded.size() > 0) {
          List<TreeNode> copy = new LinkedList<TreeNode>(lastExpanded);
          Iterator<TreeNode> it = copy.iterator();

          while (it.hasNext()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) it.next();

            Collection<TreeNode> col = childSelected(node, false);

            if (col != null) {
              collapsedNodes.addAll(col);
            }

            collapsedNodes.add(node);
            collapseImpl(node, 0, false);
            lastExpanded.remove(node);
          }
        }
      } else {
        Enumeration<?> e = rootNode.children();

        while (e.hasMoreElements()) {
          DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
          Object obj = node.getUserObject();

          if (obj instanceof IslandNodeWrapper) {
            collapseImpl(node, depth + 1, true);
          } else {
            collapseImpl(node, depth, true);
          }
        }
      }
    }
  }

  private void collapseImpl(DefaultMutableTreeNode node, int depth, boolean add) {
    if ((depth > 0) && (node.getChildCount() > 0)) {
      depth--;

      for (int i = 0; i < node.getChildCount(); i++) {
        collapseImpl((DefaultMutableTreeNode) node.getChildAt(i), depth, true);
      }
    }

    // important to do this here because children look on parent's expand state
    TreePath path = new TreePath(node.getPath());

    if (tree.isExpanded(path)) {
      if (add && tree.isPathSelected(path)) {
        collapsedNodes.add(node);
      }

      tree.collapsePath(path);
    }
  }

  private Collection<TreeNode> childSelected(DefaultMutableTreeNode node, boolean include) {
    if (tree.isSelectionEmpty())
      return null;

    List<TreeNode> list = new LinkedList<TreeNode>();

    if (include && tree.isPathSelected(new TreePath(node.getPath()))) {
      list.add(node);
    }

    Enumeration<?> e = node.children();

    while (e.hasMoreElements()) {
      Collection<TreeNode> col = childSelected((DefaultMutableTreeNode) e.nextElement(), true);

      if (col != null) {
        list.addAll(col);
      }
    }

    if (list.size() == 0) {
      list = null;
    }

    return list;
  }

  /**
   * Inserts a new ship into the tree.
   */
  public void tempShipCreated(Ship parent, Ship newShip) {
    DefaultMutableTreeNode parentNode =
        (DefaultMutableTreeNode) shipNodes.get(parent.getID());
    if (parentNode != null) {

      if (newShip == null) {
        if (parentNode.getChildCount() > 0) {
          parentNode.removeAllChildren();
          treeModel.reload(parentNode);
        }
        return;
      }

      Comparator<Unique> idComp = IDComparator.DEFAULT;
      List<Ship> siblings = new LinkedList<Ship>(parent.getTempShips());
      Collections.sort(siblings, idComp);

      int index = Collections.binarySearch(siblings, newShip, idComp);

      // bugfixing if creating more than one temp unit at once:
      // lower maximum to parentNode.getChildCount()
      index = Math.min(index, parentNode.getChildCount());
      if (index >= 0) {
        insertShip(parentNode, newShip, index);
      } else {
        EMapOverviewPanel.log
            .info(
                "EMapOverviewPanel.tempUnitCreated(): new temp ship is not a child of its parent!");
      }
    } else {
      EMapOverviewPanel.log
          .info("EMapOverviewPanel.tempUnitCreated(): cannot determine parent node of temp ship!");
    }
  }

  /**
   * Inserts the temp unit into the tree.
   *
   * @see magellan.client.event.TempUnitListener#tempUnitCreated(magellan.client.event.TempUnitEvent)
   */
  public void tempUnitCreated(TempUnitEvent e) {
    DefaultMutableTreeNode parentNode =
        (DefaultMutableTreeNode) unitNodes.get(e.getTempUnit().getParent().getID());

    if (parentNode != null) {
      Comparator<Unique> idComp = IDComparator.DEFAULT;
      List<TempUnit> siblings = new LinkedList<TempUnit>(e.getTempUnit().getParent().tempUnits());
      Collections.sort(siblings, idComp);

      int index = Collections.binarySearch(siblings, e.getTempUnit(), idComp);

      // bugfixing if creating more than one temp unit at once:
      // lower maximum to parentNode.getChildCount()
      index = Math.min(index, parentNode.getChildCount());
      if (index >= 0) {
        insertUnit(parentNode, e.getTempUnit(), index);
      } else {
        EMapOverviewPanel.log
            .info("EMapOverviewPanel.tempUnitCreated(): new temp unit is not a child of its parent!");
      }
    } else {
      EMapOverviewPanel.log
          .info("EMapOverviewPanel.tempUnitCreated(): cannot determine parent node of temp unit!");
    }
  }

  /**
   * Removes the temp unit from the tree.
   *
   * @see magellan.client.event.TempUnitListener#tempUnitDeleting(magellan.client.event.TempUnitEvent)
   */
  public void tempUnitDeleting(TempUnitEvent e) {
    TempUnit t = e.getTempUnit();
    DefaultMutableTreeNode unitNode = (DefaultMutableTreeNode) unitNodes.get(t.getID());
    unitNodes.remove(t.getID());

    // DefaultMutableTreeNode parentNode =
    // (DefaultMutableTreeNode)unitNode.getParent();
    Object o = tree.getLastSelectedPathComponent();

    if ((o != null) && (o == unitNode)) {
      tree.getSelectionModel().removeSelectionPath(new TreePath(treeModel.getPathToRoot(unitNode)));
    }

    treeModel.removeNodeFromParent(unitNode);

    /*
     * parentNode.remove(unitNode); treeModel.reload(parentNode);
     */
  }

  private void insertShip(DefaultMutableTreeNode parentNode, Ship newShip, int index) {
    UnitContainerNodeWrapper w = nodeWrapperFactory.createUnitContainerNodeWrapper(newShip);
    DefaultMutableTreeNode shipNode = new DefaultMutableTreeNode(w);
    parentNode.insert(shipNode, index);
    treeModel.reload(parentNode);
  }

  private void insertUnit(DefaultMutableTreeNode parentNode, Unit u, int index) {
    UnitNodeWrapper w = nodeWrapperFactory.createUnitNodeWrapper(u);
    DefaultMutableTreeNode unitNode = new DefaultMutableTreeNode(w);
    parentNode.insert(unitNode, index);
    unitNodes.put(u.getID(), unitNode);
    treeModel.reload(parentNode);
  }

  // ///////////////
  // Key handler //
  // ///////////////

  /**
   * Should return all short cuts this class want to be informed. The elements should be of type
   * javax.swing.KeyStroke
   *
   * @see magellan.client.desktop.ShortcutListener#getShortCuts()
   */
  public Iterator<KeyStroke> getShortCuts() {
    return shortcuts.iterator();
  }

  /**
   * This method is called when a shortcut from getShortCuts() is recognized.
   *
   * @see magellan.client.desktop.ShortcutListener#shortCut(javax.swing.KeyStroke)
   */
  public void shortCut(javax.swing.KeyStroke shortcut) {
    int index = shortcuts.indexOf(shortcut);

    switch (index) {
    case -1:
      break; // unknown shortcut

    case 0:
    case 1:
      DesktopEnvironment.requestFocus(MagellanDesktop.OVERVIEW_IDENTIFIER);
      tree.requestFocus(); // activate the tree, not the scrollpane

      break;

    // case 2: shortCut_N();break;
    // case 2: toggleOrderConfirmation();break;
    case 2:
    case 4:
      jumpInSelectionHistory(1);

      break;

    case 3:
    case 5:
      jumpInSelectionHistory(-1);

      break;
    }
  }

  /**
   * Confirms/unconfirms a unit.
   */
  public void shortCut_B() {
    toggleOrderConfirmation();
  }

  /**
   * Selects the next unit.
   */
  public void shortCut_N() {
    shortCut_N(true);
  }

  /**
   * Selects the previous unit.
   */
  public void shortCut_Reverse_N() {
    shortCut_N(false);
  }

  /**
   * Selects the next or previous unit.
   */
  protected void shortCut_N(boolean traverseDown) {
    if ((tree == null) || (rootNode == null) || (tree.getSelectionPath() == null))
      // fail fast
      return;

    DefaultMutableTreeNode actNode =
        (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
    Unit u = null;

    if (traverseDown) {
      // iterate over objects after actNode
      u =
          getUnitInTree(iterateToNode(rootNode.preorderEnumeration(), actNode), actNode,
              traverseDown);

      if (u == null) {
        // iterate over objects before actNode
        u = getUnitInTree(rootNode.preorderEnumeration(), actNode, traverseDown);
      }
    } else {
      // iterate over objects before actNode
      u = getUnitInTree(rootNode.preorderEnumeration(), actNode, traverseDown);

      if (u == null) {
        // iterate over objects after actNode
        u =
            getUnitInTree(iterateToNode(rootNode.preorderEnumeration(), actNode), actNode,
                traverseDown);
      }
    }

    if (u != null) {
      if (EMapOverviewPanel.log.isDebugEnabled()) {
        EMapOverviewPanel.log
            .debug("EMapOverviewPanel.shortCut_N(): firing Selection Event with Unit " + u + " ("
                + u.isOrdersConfirmed() + ")");
      }

      // event source u? yeah, just provide some
      // stupid value here, null is prohibited, else
      // the selectionChanged() method would reject
      // the event since it originates from this.
      dispatcher.fire(SelectionEvent.create(u, u));
      // pavkovic 2004.04.28: we dont need to fire an event here as
      // treeValueChanged already does this for us
      // dispatcher.fire(new SelectionEvent(this, null, u));

      // final Unit u2 = u;
      // SwingUtilities.invokeLater(new Runnable() {
      // public void run() {
      // dispatcher.fire(new SelectionEvent(EMapOverviewPanel.this, null, u2));
      // }});
    }
  }

  private Enumeration<?> iterateToNode(Enumeration<?> nodes, Object actNode) {
    while (nodes.hasMoreElements()) {
      if (nodes.nextElement().equals(actNode)) {
        break;
      }
    }

    return nodes;
  }

  private Unit getUnitInTree(Enumeration<?> nodes, Object actNode, boolean first) {
    Unit ret = null;

    while (nodes.hasMoreElements()) {
      DefaultMutableTreeNode nextNode = (DefaultMutableTreeNode) nodes.nextElement();

      if (nextNode.equals(actNode))
        // return latest found
        return ret;

      if (nextNode.getUserObject() instanceof UnitNodeWrapper) {
        UnitNodeWrapper uw = (UnitNodeWrapper) nextNode.getUserObject();
        Unit u = uw.getUnit();

        // pavkovic 2003.06.17: foreign units have their ordersConfirmed set to
        // false!
        // we use uw.emphasized() to find a *candidate* for selecting.
        // The candidate needs to have ordersConfirmed set to false.
        if (uw.emphasized() && !u.isOrdersConfirmed()) {
          ret = u;

          // now we found a candidate
          if (first)
            // ... and we are interested in the *first* hit
            return ret;
        }
      }
    }

    // ... here we are interested in the *last* hit (e.q. to find a predecessor
    // of a unit)
    return ret;
  }

  /**
   * @see magellan.client.swing.preferences.PreferencesFactory#createPreferencesAdapter()
   */
  public PreferencesAdapter createPreferencesAdapter() {
    return new RegionOverviewPreferences(this, settings, getGameData());
  }

  /**
   * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
   */
  public void stateChanged(javax.swing.event.ChangeEvent p1) {
    // update the history list
    setLstHistory();
  }

  /**
   * Repaints this component. This method also explicitly repaints it's managed components because
   * the may be separated by the Magellan Desktop.
   */
  @Override
  public void repaint() {
    super.repaint();

    if (scpTree != null) {
      scpTree.repaint();
    }

    if (scpHistory != null) {
      scpHistory.repaint();
    }
  }

  private void toggleOrderConfirmation() {
    if (activeObject == null)
      return;

    if (activeObject instanceof Region) {
      Region r = (Region) activeObject;
      boolean first = true;
      boolean confirm = false;

      for (Unit u : r.units()) {
        // if (EMapDetailsPanel.isPrivilegedAndNoSpy(u)) {
        if (first) {
          confirm = !u.isOrdersConfirmed();
          first = false;
        }

        u.setOrdersConfirmed(confirm);
        // }
      }

      dispatcher.fire(new OrderConfirmEvent(this, r.units()));
    } else if (activeObject instanceof Unit) {
      Unit u = (Unit) activeObject;

      // if (EMapDetailsPanel.isPrivilegedAndNoSpy(u)) {
      u.setOrdersConfirmed(!u.isOrdersConfirmed());

      dispatcher.fire(new OrderConfirmEvent(this, Collections.singletonList(u)));
      // }
    }
  }

  /**
   * Do clean-up on quitting.
   */
  @Override
  public void quit() {
    settings.setProperty("EMapOverviewPanel.treeHistorySplit", Integer.toString(sppTreeHistory
        .getDividerLocation()));
  }

  /**
   * Selects and entry in the selection history by a relative offset.
   *
   * @param i
   */
  private void jumpInSelectionHistory(int i) {
    ListModel model = lstHistory.getModel();

    if (model.getSize() > 0) {
      int selectedIndex = Math.max(lstHistory.getSelectedIndex(), 0);
      int newIndex = Math.min(Math.max(selectedIndex + i, 0), model.getSize() - 1);

      if (selectedIndex != newIndex) {
        lstHistory.setSelectedIndex(newIndex);
        // dispatcher.fire(SelectionEvent.create(lstHistory,
        // SelectionHistory.getHistory(newIndex).event));
      }
    }
  }

  /**
   * This is a helper method to set this.activeAlliances to a useful value, if no faction or group
   * is active. The idea is to take all alliances of all privileged factions and combine their
   * states by & (or in other words to take the intersection over all alliances of all privileged
   * factions)
   */
  private void setDefaultAlliances() {
    activeAlliancesAreDefault = true;

    List<Faction> privilegedFactions = new LinkedList<Faction>();
    activeAlliances.clear();

    boolean privilegedWithoutAllies = false;

    for (Faction f : getGameData().getFactions()) {
      if (f.isPrivileged()) {
        privilegedFactions.add(f);

        if ((f.getAllies() == null || f.getAllies().values().size() <= 0)
            && f.getAlliance() == null) {
          // remember that one privileged faction had no allies
          // so it is not necessary to do further calculations
          privilegedWithoutAllies = true;
        }
      }
    }

    if (!privilegedWithoutAllies && !privilegedFactions.isEmpty()
        && (privilegedFactions.get(0)).getAllies() != null) {
      // take the alliances of the first found privileged faction as
      // activeAlliances
      activeAlliances.putAll((privilegedFactions.get(0)).getAllies());

      // now check whether they are contained in the alliances-Maps of the other
      // privileged factions and adjust their states if necessary
      boolean delEntry = false;

      for (Iterator<EntityID> iter = activeAlliances.keySet().iterator(); iter.hasNext();) {
        EntityID id = iter.next();

        for (int factionCount = 1; factionCount < privilegedFactions.size(); factionCount++) {
          Faction f = privilegedFactions.get(factionCount);

          if (f.getAllies() == null || !f.getAllies().containsKey(id)) {
            // mark this alliances as to be deleted out of activeAlliances
            delEntry = true;
            break;
          } else {
            Alliance a1 = activeAlliances.get(id);
            Alliance a2 = f.getAllies().get(id);
            activeAlliances.put(id, new Alliance(a1.getFaction(), a1.getState() & a2.getState()));
          }
        }

        if (delEntry) {
          delEntry = false;
          iter.remove();
        }
      }
    }

    if (!privilegedFactions.isEmpty()) {
      // if all factions are in one E3 type alliance,
      AllianceGroup commonAlliance = privilegedFactions.get(0).getAlliance();
      for (Faction f : privilegedFactions) {
        if (f.getAlliance() == null || f.getAlliance() != commonAlliance) {
          commonAlliance = null;
          break;
        }
      }

      // ... add all alliance factions with HELP Combat
      for (Faction f : privilegedFactions) {
        if (commonAlliance != null) {
          for (ID allyId : commonAlliance.getFactions()) {
            Alliance a1 = activeAlliances.get(allyId);
            Faction f2 = f.getData().getFaction(allyId);
            if (f2 != null) {
              if (a1 == null) {
                activeAlliances.put(f2.getID(), new Alliance(f2, EresseaConstants.A_COMBAT));
              } else {
                activeAlliances.put(f2.getID(), new Alliance(f2, a1.getState()
                    | EresseaConstants.A_COMBAT));
              }
            }
          }
        }
      }
    }

    // now add all privileged factions with alliance state Integer.MAX_VALUE
    for (Faction f : privilegedFactions) {
      activeAlliances.put(f.getID(), new Alliance(f, Integer.MAX_VALUE));
    }
  }

  protected void loadExpandProperties() {
    try {
      expandMode = Integer.parseInt(settings.getProperty("EMapOverviewPanel.ExpandMode"));
    } catch (Exception exc) {
      expandMode = EMapOverviewPanel.EXPAND_FLAG | (3 << 2);
    }

    try {
      expandTrustlevel =
          Integer.parseInt(settings.getProperty("EMapOverviewPanel.ExpandTrustlevel"));
    } catch (Exception exc) {
      expandTrustlevel = Faction.TL_PRIVILEGED;
    }
  }

  protected void loadCollapseProperty() {
    try {
      collapseMode = Integer.parseInt(settings.getProperty("EMapOverviewPanel.CollapseMode"));
    } catch (Exception exc) {
      expandMode = 3 << 2;
    }
  }

  /**
   * @see magellan.client.swing.tree.TreeUpdate#updateTree(java.lang.Object)
   */
  public void updateTree(Object src) {
    tree.treeDidChange();

    // (stm 09-2012) I don't think this is needed any more and it hurts performance
    // // Use the UI Fix
    // javax.swing.plaf.TreeUI treeUI = tree.getUI();
    //
    // if (treeUI instanceof javax.swing.plaf.basic.BasicTreeUI) {
    // javax.swing.plaf.basic.BasicTreeUI ui2 = (javax.swing.plaf.basic.BasicTreeUI) treeUI;
    // int i = ui2.getLeftChildIndent();
    // ui2.setLeftChildIndent(100);
    // ui2.setLeftChildIndent(i);
    // }
    // tree.revalidate();
    // tree.repaint();
  }

  /**
   * @see magellan.client.desktop.ShortcutListener#getShortcutDescription(KeyStroke)
   */
  public String getShortcutDescription(KeyStroke stroke) {
    int index = shortcuts.indexOf(stroke);

    return Resources.get("emapoverviewpanel.shortcut.description." + String.valueOf(index));
  }

  /**
   * @see magellan.client.desktop.ShortcutListener#getListenerDescription()
   */
  public String getListenerDescription() {
    return Resources.get("emapoverviewpanel.shortcut.title");
  }

  private class ScrollerRunnable implements Runnable {
    private Rectangle centerRect;

    /**
     * Creates a new ScrollerRunnable object.
     *
     * @param r
     */
    public ScrollerRunnable(Rectangle r) {
      centerRect = r;
    }

    /**
     * Scrolls tree to correct viewport.
     */
    public void run() {
      if (centerRect != null) {
        Rectangle viewRect = scpTree.getViewport().getViewRect();
        centerRect.x = viewRect.x;
        centerRect.width = viewRect.width;

        if (viewRect.contains(centerRect) == false) {
          Point viewPos = new Point(0, centerRect.y);

          if (centerRect.height < viewRect.height) {
            viewPos.y =
                Math.min(tree.getHeight() - viewRect.height, centerRect.y
                    - ((viewRect.height - centerRect.height) / 2));
          }

          scpTree.getViewport().setViewPosition(viewPos);
        }
      }
    }
  }

  // /**
  // * Updates UnitNodeWrappers on changes of orders
  // */
  // private class UnitWrapperUpdater implements UnitOrdersListener {
  //
  // Set<Unit> dirtyUnits = new HashSet<Unit>();
  //
  // /**
  // * Invoked when the orders of a unit are modified.
  // *
  // * @see
  // magellan.client.event.UnitOrdersListener#unitOrdersChanged(magellan.client.event.UnitOrdersEvent)
  // */
  // public void unitOrdersChanged(UnitOrdersEvent e) {
  // synchronized (dirtyUnits) {
  // if (dirtyUnits == null) {
  // dirtyUnits = new HashSet<Unit>();
  // }
  // if (e.getUnit() != null) {
  // // a changed order can change the unit's own node, all nodes that are related to the unit,
  // // and
  // // also all units that are related by two degrees;
  // // FIXME order interpreter should do this for us!
  // markDirty(e.getUnit(), 2, dirtyUnits);
  // }
  // if (!e.isChanging()) {
  // int i = dirtyUnits.size();
  // while (!dirtyUnits.isEmpty() && i-- >= 0) {
  // Unit u = dirtyUnits.iterator().next();
  // update(u);
  // dirtyUnits.remove(u);
  // }
  // if (!dirtyUnits.isEmpty()) {
  // log.error("dirty units remain!");
  // }
  // dirtyUnits.clear();
  // }
  // }
  // }
  //
  // /**
  // * Updates the nodes of this unit and all nodes that are related to this unit indirectly by no
  // * more than <code>updateRelationPartersDistance</code>.
  // *
  // * @param u The updated unit
  // * @param updateRelationPartnersDistance maximum distance to this unit of units that should be
  // * updated as well
  // */
  // protected synchronized void markDirty(Unit u, int updateRelationPartnersDistance,
  // Set<Unit> visited) {
  // if (u != null && unitNodes.containsKey(u.getID())) {
  // visited.add(u);
  //
  // if (updateRelationPartnersDistance > 0) {
  // for (UnitRelation rel : u.getRelations()) {
  // if (rel.origin != u) {
  // markDirty(rel.origin, updateRelationPartnersDistance - 1, visited);
  // }
  // if (rel.source != rel.origin) {
  // markDirty(rel.source, updateRelationPartnersDistance - 1, visited);
  // }
  // if (rel instanceof InterUnitRelation) {
  // InterUnitRelation iuRel = (InterUnitRelation) rel;
  // if (iuRel.target != u && iuRel.target != rel.source) {
  // markDirty(iuRel.target, updateRelationPartnersDistance - 1, visited);
  // }
  // }
  // }
  // }
  // }
  // }
  //
  // }

  /**
   * Updates the nodes of this unit and all nodes that are related to this unit indirectly by no
   * more than <code>updateRelationPartersDistance</code>.
   *
   * @param u The updated unit
   */
  protected synchronized void update(Unit u) {
    // log.finest(u);
    // long time = System.currentTimeMillis();
    TreeNode regionNode = regionNodes.get(u.getRegion().getID());
    // Set<TreeNode> parents = new HashSet<TreeNode>();
    // parents.add(regionNode);
    //
    for (Unit u2 : u.getRegion().units()) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) unitNodes.get(u2.getID());
      // TreeNode[] path = treeModel.getPathToRoot(node);
      // TreeNode parent = path[path.length - 2];
      if (node != null) {
        treeModel.nodeChanged(node);
        UnitNodeWrapper unw = (UnitNodeWrapper) node.getUserObject();
        unw.clearBuffer();
        // parents.add(parent);
      }

    }
    for (Ship parent : u.getRegion().ships()) {
      tempShipCreated(parent, null);
      for (Ship tempShip : parent.getTempShips()) {
        tempShipCreated(parent, tempShip);
      }
    }
    updateTree(this);

    // log.finest(System.currentTimeMillis() - time);
    // for (TreeNode parent : parents) {
    // int[] childIndices = new int[parent.getChildCount()];
    // for (int i = 0; i < childIndices.length; ++i) {
    // childIndices[i] = i;
    // }
    // treeModel.nodesChanged(parent, childIndices);
    // log.finest(System.currentTimeMillis() - time);
    // }

    // if (unitNodes.containsKey(u.getID())) {
    // DefaultMutableTreeNode node = (DefaultMutableTreeNode) unitNodes.get(u.getID());
    // UnitNodeWrapper unw = (UnitNodeWrapper) node.getUserObject();
    // unw.clearBuffer();
    // treeModel.nodeChanged(node);
    //
    // // update building or ship nodes, which may have been modified, too
    // Ship ship = u.getShip();
    // if (ship != null && shipNodes.containsKey(ship.getID())) {
    // treeModel.nodeChanged(shipNodes.get(ship.getID()));
    // }
    // ship = u.getModifiedShip();
    // if (ship != null && shipNodes.containsKey(ship.getID())) {
    // treeModel.nodeChanged(shipNodes.get(ship.getID()));
    // }
    //
    // UnitContainer container = u.getBuilding();
    // if (container != null && buildingNodes.containsKey(container.getID())) {
    // treeModel.nodeChanged(buildingNodes.get(container.getID()));
    // }
    // container = u.getModifiedBuilding();
    // if (container != null && buildingNodes.containsKey(container.getID())) {
    // treeModel.nodeChanged(buildingNodes.get(container.getID()));
    // }
    //
    // // tree.validate();
    // // tree.repaint();
    // }
  }

  protected TreeBuilder createTreeBuilder() {
    TreeBuilder treeBuilder = new TreeBuilder(settings, nodeWrapperFactory);
    treeBuilder.setRegionNodes(regionNodes);
    treeBuilder.setUnitNodes(unitNodes);
    treeBuilder.setShipNodes(shipNodes);
    treeBuilder.setBuildingNodes(buildingNodes);
    treeBuilder.setActiveAlliances(activeAlliances);
    /**
     * Fiete Default fuer den Modus auf UNITS | SHIPS | BUILDINGS |COMMENTS setzen nach Vorgabe stm
     */
    treeBuilder.setDisplayMode(Integer.parseInt(settings.getProperty("EMapOverviewPanel.filters",
        Integer.valueOf(
            TreeBuilder.UNITS | TreeBuilder.BUILDINGS | TreeBuilder.SHIPS | TreeBuilder.COMMENTS)
            .toString())));
    return treeBuilder;
  }

  /**
   * Class encapsulating the menu for the Overview.
   *
   * @deprecated Currently unused. (stm)
   */
  @Deprecated
  class OverviewMenu extends JMenu implements ActionListener {
    JCheckBoxMenuItem items[];

    /**
     * Creates a new OverviewMenu object.
     *
     * @param label
     * @param mnemonic
     * @see #setMnemonic(char)
     * @see JMenu#JMenu(String)
     */
    public OverviewMenu(String label, char mnemonic) {
      super(label);
      this.setMnemonic(mnemonic);

      this.add(nodeWrapperFactory.getContextMenu());
      addSeparator();

      JMenuItem item = this.add(Resources.get("emapoverviewpanel.menu.filter"));
      item.setEnabled(false);

      int mode = getTreeBuilder().getDisplayMode();

      items = new JCheckBoxMenuItem[4];

      for (int i = 0; i < 4; i++) {
        items[i] =
            new JCheckBoxMenuItem(Resources.get("emapoverviewpanel.menu.filter."
                + String.valueOf(i)));
        items[i].setSelected((mode & (1 << i)) != 0);
        items[i].addActionListener(this);
        this.add(items[i]);
      }
    }

    /**
     */
    public void actionPerformed(ActionEvent actionEvent) {
      updateState();
    }

    protected void updateState() {
      TreeBuilder treeBuilder = getTreeBuilder();

      int mode = treeBuilder.getDisplayMode() & TreeBuilder.CREATE_ISLANDS;

      for (int i = 0; i < items.length; i++) {
        mode |= ((items[i].isSelected() ? 1 : 0) << i);
      }

      if (mode != treeBuilder.getDisplayMode()) {
        treeBuilder.setDisplayMode(mode);
        rebuildTree();
        settings.setProperty("EMapOverviewPanel.filters", String.valueOf(mode
            ^ TreeBuilder.CREATE_ISLANDS));
      }
    }
  }

  /**
   * Returns the menu for the overview panel that is used by the client.
   *
   * @deprecated I think this is needless.
   */
  @Deprecated
  public JMenu getMenu() {
    // We will create it every time the method is invoked since that should be
    // only once...
    return new OverviewMenu(Resources.get("emapoverviewpanel.menu.caption"), Resources.get(
        "emapoverviewpanel.menu.mnemonic").charAt(0));
  }

  /**
   * @see magellan.client.swing.MenuProvider#getSuperMenu()
   */
  public String getSuperMenu() {
    return "tree";
  }

  /**
   * @see magellan.client.swing.MenuProvider#getSuperMenuTitle()
   */
  public String getSuperMenuTitle() {
    return Resources.get("emapoverviewpanel.menu.supertitle");
  }

  /**
   *
   */
  public SelectionEvent getSelectedObjects() {
    return contextManager.getSelection();
  }

  /**
   * Returns the event dispatcher of this panel.
   */
  public EventDispatcher getEventDispatcher() {
    return dispatcher;
  }

  /**
   * Returns the value of expandMode.
   */
  public int getExpandMode() {
    return expandMode;
  }

  /**
   * Returns the value of expandTrustLevel.
   */
  public int getExpandTrustLevel() {
    return expandTrustlevel;
  }

  /**
   * Returns the value of collapseMode.
   */
  public int getCollapseMode() {
    return collapseMode;
  }

  /**
   * Sets the value of expandMode.
   */
  public void setExpandMode(int expandMode) {
    this.expandMode = expandMode;
  }

  /**
   * Sets the value of expandTrustlevel.
   */
  public void setExpandTrustLevel(int expandTrustlevel) {
    this.expandTrustlevel = expandTrustlevel;
  }

  /**
   * Sets the value of collapseMode.
   */
  public void setCollapseMode(int collapseMode) {
    this.collapseMode = collapseMode;
  }
}
