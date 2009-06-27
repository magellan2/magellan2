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
import magellan.client.desktop.ShortcutListener;
import magellan.client.event.EventDispatcher;
import magellan.client.event.OrderConfirmEvent;
import magellan.client.event.OrderConfirmListener;
import magellan.client.event.SelectionEvent;
import magellan.client.event.SelectionListener;
import magellan.client.event.TempUnitEvent;
import magellan.client.event.TempUnitListener;
import magellan.client.event.UnitOrdersEvent;
import magellan.client.event.UnitOrdersListener;
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
import magellan.library.Building;
import magellan.library.Faction;
import magellan.library.Group;
import magellan.library.ID;
import magellan.library.Named;
import magellan.library.Region;
import magellan.library.Ship;
import magellan.library.TempUnit;
import magellan.library.Unique;
import magellan.library.Unit;
import magellan.library.UnitID;
import magellan.library.ZeroUnit;
import magellan.library.event.GameDataEvent;
import magellan.library.relation.TransferRelation;
import magellan.library.relation.UnitRelation;
import magellan.library.utils.MagellanFactory;
import magellan.library.utils.PropertiesHelper;
import magellan.library.utils.Resources;
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
 * DOCUMENT-ME
 * 
 * @author $Author: $
 * @version $Revision: 382 $
 */
public class EMapOverviewPanel extends InternationalizedDataPanel implements TreeSelectionListener,
    TreeExpansionListener, SelectionListener, OrderConfirmListener, PreferencesFactory,
    TempUnitListener, ShortcutListener, ChangeListener, TreeUpdate, MenuProvider, Initializable {
  private static final Logger log = Logger.getInstance(EMapOverviewPanel.class);

  // GUI elements
  private JSplitPane sppTreeHistory = null;
  private CopyTree tree = null;
  private DefaultTreeModel treeModel = null;
  private DefaultMutableTreeNode rootNode = null;
  private JScrollPane scpTree = null;
  private JList lstHistory = null;
  private JScrollPane scpHistory;

  // node maps, mapping the object ids to their tree nodes
  private Map<ID, TreeNode> regionNodes = new Hashtable<ID, TreeNode>();
  private Map<ID, TreeNode> unitNodes = new Hashtable<ID, TreeNode>();
  private Map<ID, TreeNode> buildingNodes = new Hashtable<ID, TreeNode>();
  private Map<ID, TreeNode> shipNodes = new Hashtable<ID, TreeNode>();

  // relation map, mapping unit's id to its relations
  private Map<ID, List<UnitRelation>> unitRelations = new HashMap<ID, List<UnitRelation>>();
  private boolean ignoreTreeSelections = false;

  // region with previously selected item
  private Unique activeObject = null;
  private List<Object> selectedObjects = new LinkedList<Object>();

  // needed by FactionNodeWrapper to determine the active alliances
  // keys: FactionIDs, values: Alliance-objects
  private Map<ID, Alliance> activeAlliances = new Hashtable<ID, Alliance>();

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
  private int collapseMode = EMapOverviewPanel.COLLAPSE_FLAG | EMapOverviewPanel.COLLAPSE_ONLY_EXPANDED | (3 << 2);
  private List<TreeNode> lastExpanded = new LinkedList<TreeNode>();
  private Set<TreeNode> collapsedNodes = new HashSet<TreeNode>();
  private Set<TreeNode> collapseInfo = new HashSet<TreeNode>();
  private Set<TreeNode> expandInfo = new HashSet<TreeNode>();
  private Set<TreePath> selectionTransfer = new HashSet<TreePath>();
  private static final Comparator<Unique> idCmp = IDComparator.DEFAULT;
  private static final Comparator<Named> nameCmp = new NameComparator(EMapOverviewPanel.idCmp);

  public static final String IDENTIFIER = "OVERVIEW";

  /**
   * Creates a new EMapOverviewPanel object.
   * 
   * @param d
   *          DOCUMENT-ME
   * @param p
   *          DOCUMENT-ME
   */
  public EMapOverviewPanel(EventDispatcher d, Properties p) {
    super(d, p);
    loadExpandProperties();
    loadCollapseProperty();

    nodeWrapperFactory = new NodeWrapperFactory(settings, "EMapOverviewPanel", Resources.get("emapoverviewpanel.wrapperfactory.title"));
    nodeWrapperFactory.setSource(this);

    // to get the pref-adapter
    nodeWrapperFactory.createUnitNodeWrapper(MagellanFactory.createUnit(UnitID.createUnitID(0, 10)));

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
          if (lstHistory.getSelectedValue()!=null){
            dispatcher.fire(new SelectionEvent(lstHistory, null, lstHistory.getSelectedValue()));
          }
        }
      }
    });
    SelectionHistory.ignoreSource(lstHistory);
    scpHistory = new JScrollPane(lstHistory);

    // ClearLook suggests to remove border
    scpHistory.setBorder(null);

    // split pane
    sppTreeHistory = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scpTree, scpHistory);
    sppTreeHistory.setOneTouchExpandable(true);
    sppTreeHistory.setDividerLocation(Integer.parseInt(settings.getProperty("EMapOverviewPanel.treeHistorySplit", "100")));

    // ClearLook suggests to remove border
    sppTreeHistory.setBorder(null);

    // add components to this panel
    this.setLayout(new GridLayout(1, 0));
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

    // register order change listener
    d.addUnitOrdersListener(new UnitWrapperUpdater());
  }

  /**
   * DOCUMENT-ME
   * 
   * 
   */
  public NodeWrapperFactory getNodeWrapperFactory() {
    return nodeWrapperFactory;
  }

  /**
   * Returns the component displaying the overview tree.
   * 
   * 
   */
  public Component getOverviewComponent() {
    return scpTree;
  }

  /**
   * Returns the component displaying the history.
   * 
   * 
   */
  public Component getHistoryComponent() {
    return scpHistory;
  }

  /**
   * GameDataChanged event handler routine updating the tree. Note: It is
   * significant for the valueChanged() method, how deep the different node
   * types are nested.
   * 
   * @param e
   *          DOCUMENT-ME
   */
  @Override
  public void gameDataChanged(GameDataEvent e) {
    this.data = e.getGameData();

    contextManager.setGameData(this.data);

    rebuildTree();

    // initialize activeAlliances-Map
    setDefaultAlliances();
  }

  public void rebuildTree() {
    Unique oldActiveObject = activeObject;
    Collection<Object> oldSelectedObjects = new LinkedList<Object>(selectedObjects);

    // clear the history
    SelectionHistory.clear();
    lstHistory.setListData(SelectionHistory.getHistory().toArray());

    // clear node maps
    regionNodes.clear();
    unitNodes.clear();
    buildingNodes.clear();
    shipNodes.clear();
    rootNode.removeAllChildren();

    // clear relation map
    unitRelations.clear();

    // clear other buffers
    selectedObjects.clear();
    activeObject = null;
    lastExpanded.clear();

    // if((data != null) && (data.regions() != null)) {
    // preprocess regions to have relations
    // pavkovic 2003.12.21: moved to RegionNodeWrapper, perhaps this helps
    // for(Iterator iter = data.regions().values().iterator();
    // iter.hasNext();) {
    // ((Region) iter.next()).refreshUnitRelations();
    // }
    // }
    // initialize variables used in while loop

    // TODO: this needs explanations
    boolean createIslandNodes = PropertiesHelper.getBoolean(settings, "EMapOverviewPanel.displayIslands", true) && PropertiesHelper.getBoolean(settings, "EMapOverviewPanel.sortRegions", true) && settings.getProperty("EMapOverviewPanel.sortRegionsCriteria", "coordinates").equalsIgnoreCase("islands");

    boolean sortShipUnderUnitParent = PropertiesHelper.getBoolean(settings, "EMapOverviewPanel.sortShipUnderUnitParent", true);

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
    treeBuilder.setDisplayMode(displayMode | (createIslandNodes ? TreeBuilder.CREATE_ISLANDS : 0));

    // creation of Comparator outsourced to Comparator
    // getUnitSorting(java.util.Properties)
    treeBuilder.setUnitComparator(EMapOverviewPanel.getUnitSorting(settings));
    treeBuilder.setTreeStructure(EMapOverviewPanel.getTreeStructure(settings));

    treeBuilder.buildTree(rootNode, data);

    tree.setShowsRootHandles(PropertiesHelper.getBoolean(settings, "EMapOverviewPanel.treeRootHandles", true));

    treeModel.reload();
    this.selectionChanged(new SelectionEvent(treeModel, oldSelectedObjects, oldActiveObject));

  }

  private TreeBuilder myTreeBuilder;

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
   *          DOCUMENT-ME
   * 
   */
  public static Comparator<? super Unit> getUnitSorting(Properties settings) {
    // create Comparator used for unit sorting
    String criteria = settings.getProperty("EMapOverviewPanel.sortUnitsCriteria", "skills");

    // used as the comparator on the lowest structure level
    Comparator<? super Unit> cmp = null;

    if (criteria.equals("skills")) {
      if (settings.getProperty("EMapOverviewPanel.useBestSkill", "true").equalsIgnoreCase("true")) {
//        ToStringComparator<Skill> nameComparator = new ToStringComparator<Skill>(null);
        SkillRankComparator skillTypeRankComparator = new SkillRankComparator(null, settings);
//        SkillTypeComparator<Skill> skillTypeComparator = new SkillTypeComparator<Skill>(skillTypeRankComparator, null);
        BestSkillComparator bestSkillComparator = new BestSkillComparator(SkillComparator.skillCmp,skillTypeRankComparator,SkillComparator.skillCmp);
        cmp = new UnitSkillComparator(bestSkillComparator,EMapOverviewPanel.idCmp);
      } else {
        cmp = new UnitSkillComparator(new TopmostRankedSkillComparator(null, settings), EMapOverviewPanel.idCmp);
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
   *          DOCUMENT-ME
   * 
   */
  private static int[] getTreeStructure(Properties settings) {
    String criteria = settings.getProperty("EMapOverviewPanel.treeStructure", " " + TreeHelper.FACTION + " " + TreeHelper.GROUP);
    StringTokenizer tokenizer = new StringTokenizer(criteria);
    int treeStructure[] = new int[tokenizer.countTokens()];
    int i = 0;

    while (tokenizer.hasMoreTokens()) {
      try {
        String s = tokenizer.nextToken();
        treeStructure[i] = Integer.parseInt(s);
        i++;
      } catch (NumberFormatException e) {
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
   * @param tse
   *          DOCUMENT-ME
   */
  public void valueChanged(TreeSelectionEvent tse) {
    if (ignoreTreeSelections) {
      return;
    }
    
    DefaultMutableTreeNode firstNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
    TreePath paths[] = tse.getPaths();

    // search for nodes which have been selected/deselected because of collapse
    DefaultMutableTreeNode node = null;
    boolean removed = false;

    for (int i = 0; i < paths.length; i++) {
      if (selectionTransfer.contains(paths[i])) {
        removed = true;

        break;
      }
    }

    if (removed) { // some selection transfer detected
      selectionTransfer.clear();

      /**
       * Do not return, since tree selections will not be cleared after a user
       * collapses a node that contains selected subelements.
       */

      // return;
      /*
       * Note: This is perhaps not the best way because there may be other nodes
       * than that from collapse. But "normally" this shouldn't happen.
       */
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
          setAlliances(f.getAllies(), f);
        } else {
          setAlliances(g.allies(), g.getFaction());
        }
      } else if (o instanceof UnitContainerNodeWrapper) {
        // building node selected?
        if (((UnitContainerNodeWrapper) o).getUnitContainer() instanceof Building || ((UnitContainerNodeWrapper) o).getUnitContainer() instanceof Ship) {
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
    for (int i = 0; i < paths.length; i++) {
      if (paths[i] == null) {
        continue;
      }

      TreePath path = paths[i];
      node = (DefaultMutableTreeNode) path.getLastPathComponent();

      Object o = getNodeSubject(node); 

      if (o instanceof Unique){
        if (tse.isAddedPath(path)) {
          selectedObjects.add(o);
        } else {
          selectedObjects.remove(o);
        }
      }
    }
    
    Collection<Object> selectionPath = new ArrayList<Object>();
    if (tree!=null && tree.getSelectionPath()!=null) {
      for (Object o : tree.getSelectionPath().getPath()){
        selectionPath.add(getNodeSubject((DefaultMutableTreeNode)o));
      }
    }
    dispatcher.fire(new SelectionEvent(this, selectedObjects, activeObject, selectionPath));
  }

  /**
   * Returns the (Report) object that belongs to this node, for example a region, a unit or a group.
   * 
   * @param node
   * @return
   */
  private Object getNodeSubject(DefaultMutableTreeNode node) {
    Object o = node.getUserObject();
    if (o==null) {
      return null;
    }

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
      EMapOverviewPanel.log.warn("EMapOverviewPanel.valueChanged() : Type of the user object of a selected node is unknown:" + o);
    }
    return o;
  }

  /**
   * Sets the active alliance status that are used to paint the faction or group
   * nodes in the tree.
   * 
   * @param allies
   *          The alliances map to be used, my be <code>null</code>
   * @param f
   *          The faction whose alliances are used
   */
  private void setAlliances(Map<ID,Alliance> allies, Faction f) {
    if ((allies == null) && (activeAlliances.size() > 0)) {
      // can't determine new specific alliances
      // set default alliances
      activeAlliances.clear();
      activeAlliancesAreDefault = false;
    } else if ((allies != null) && !activeAlliances.equals(allies)) {
      // set new active alliances
      activeAlliances.clear();
      activeAlliances.putAll(allies);
      activeAlliancesAreDefault = false;

      // add the selected group or faction to be able to show, on whose
      // alliances
      // the current colors of the icons of a faction node depend
      activeAlliances.put(f.getID(), new Alliance(f, Integer.MAX_VALUE));
    }

    tree.repaint();
  }

//  private Region getSelectedRegion(DefaultMutableTreeNode node) {
//    while ((node != null) && !(node.getUserObject() instanceof RegionNodeWrapper)) {
//      node = (DefaultMutableTreeNode) node.getParent();
//    }
//
//    if ((node != null) && node.getUserObject() instanceof RegionNodeWrapper) {
//      return ((RegionNodeWrapper) node.getUserObject()).getRegion();
//    } else {
//      return null;
//    }
//  }
//
  /**
   * Initialize interface
   */
  public void initComponent(String params) {
    boolean changed = false;
    for (StringTokenizer st = new StringTokenizer(params.replace('_', ' '), "|"); st.hasMoreTokens();) {
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
    sb.append(settings.getProperty("EMapOverviewPanel.treeStructure", " " + TreeHelper.FACTION + " " + TreeHelper.GROUP));
    return sb.toString().replace(' ', '_');
    /*
     * sb.append("EMapOverviewPanel.treeHistorySplit"); sb.append("=");
     * sb.append(settings.getProperty("EMapOverviewPanel.treeHistorySplit",
     * "100")); sb.append("_"); sb.append("EMapOverviewPanel.displayIslands");
     * sb.append("=");
     * sb.append(settings.getProperty("EMapOverviewPanel.displayIslands",
     * "true")); sb.append("_"); sb.append("EMapOverviewPanel.sortRegions");
     * sb.append("=");
     * sb.append(settings.getProperty("EMapOverviewPanel.sortRegions", "true"));
     * sb.append("_"); sb.append("EMapOverviewPanel.sortRegionsCriteria");
     * sb.append("=");
     * sb.append(settings.getProperty("EMapOverviewPanel.sortRegionsCriteria",
     * "coordinates")); sb.append("_");
     * sb.append("EMapOverviewPanel.sortUnitsCriteria"); sb.append("=");
     * sb.append(settings.getProperty("EMapOverviewPanel.sortUnitsCriteria","skills"));
     * sb.append("_"); sb.append("EMapOverviewPanel.useBestSkill");
     * sb.append("=");
     * sb.append(settings.getProperty("EMapOverviewPanel.useBestSkill",
     * "true")); sb.append("_");
     */

  }

  /**
   * Event handler for TreeExpansionEvents (recenters the tree if necessary)
   * 
   * @param e
   *          DOCUMENT-ME
   */
  public void treeExpanded(TreeExpansionEvent e) {
    DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
    Rectangle rec = tree.getPathBounds(new TreePath(node.getPath()));

    if (node.getChildCount() > 0) {
      TreePath path = new TreePath(((DefaultMutableTreeNode) node.getLastChild()).getPath());

      if (tree.getPathBounds(path)!=null) {
        rec.add(tree.getPathBounds(path));
      }
    }

    SwingUtilities.invokeLater(new ScrollerRunnable(rec));
  }

  /**
   * Event handler for TreeCollapsedEvents
   * 
   * @param e
   *          DOCUMENT-ME
   */
  public void treeCollapsed(TreeExpansionEvent e) {
  }

  /**
   * Change event handler, change the display status of the tree if an unit
   * orderConfimation Status changed
   * 
   * @param e
   *          DOCUMENT-ME
   */
  public void orderConfirmationChanged(OrderConfirmEvent e) {
    if ((e.getSource() == this) || (e.getUnits() == null) || (e.getUnits().size() == 0)) {
      return;
    }
    /**
     * BUG JTree UI see OrderConfimEvent
     * 
     * @author Fiete
     */
    if (e.changedToUnConfirmed()) {
      tree.updateUI();
    }
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
        Enumeration e = rootNode.children();

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

  private void collapseImpl(DefaultMutableTreeNode node, int depth, boolean add, Set<TreeNode> infoSet) {
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
        Enumeration enumeration = node.children();

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

        if (!found) {
          return;
        }
      }

      expandInfo.add(node);

      int eDepth = expandMode >> 2;

      if (eDepth > 0) {
        eDepth--;

        Enumeration enumeration = node.children();
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
    Iterator it = collapseInfo.iterator();
    Map<TreeNode, TreePath> pathsToCollapse = new HashMap<TreeNode, TreePath>();
    Map<TreeNode, TreePath> pathsToExpand = new HashMap<TreeNode, TreePath>();

    while (it.hasNext()) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) it.next();
      TreePath path = new TreePath(node.getPath());
      pathsToCollapse.put(node, path);

      Iterator it2 = expandInfo.iterator();

      while (it2.hasNext()) {
        DefaultMutableTreeNode node2 = (DefaultMutableTreeNode) it2.next();

        if (!pathsToExpand.containsKey(node2)) {
          pathsToExpand.put(node2, new TreePath(node2.getPath()));
        }

        TreePath path2 = pathsToExpand.get(node2);

        if (path.isDescendant(path2)) {
          it.remove();

          // but save it for last expand on expand-only mode
          if ((collapseMode & EMapOverviewPanel.COLLAPSE_ONLY_EXPANDED) != 0) {
            lastExpanded.add((TreeNode)path.getLastPathComponent());
          }

          break;
        }
      }
    }

    // now expand
    it = expandInfo.iterator();

    while (it.hasNext()) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) it.next();
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
    it = newSelection.iterator();

    while (it.hasNext()) {
      TreePath path = (TreePath) it.next();

      if (tree.isPathSelected(path)) {
        it.remove();
      }
    }

    selectionTransfer.addAll(newSelection);
    
    
    // transfer the selection
    TreePath paths[] = selectionTransfer.toArray(new TreePath[0]);

    tree.setSelectionPaths(paths);

    // now collapse
    // (this should'nt trigger selection events any more)
    it = collapseInfo.iterator();

    while (it.hasNext()) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) it.next();
      TreePath path = pathsToCollapse.get(node);

      if (tree.isExpanded(path)) {
        tree.collapsePath(path);
        it.remove();
      }
    }
  }

  /**
   * Looks a path down from root to the leaf if to expand any segment
   * 
   * @param path
   *          DOCUMENT-ME
   */
  protected void checkPathToExpand(TreePath path) {
    if (path.getPathCount() > 1) {
      checkPathToExpand(path.getParentPath());
    }

    if (!tree.isExpanded(path)) {
      lastExpanded.add((TreeNode)path.getLastPathComponent());
      tree.expandPath(path);
    }
  }

  /**
   * Selection event handler, update the selection status of the tree. First the
   * active object is considered and selected in the tree if contained. After
   * that selected objects are considered, but only selection
   * type is different from SelectionEvent.ST_REGIONS. In this case the tree
   * selection is set to the selected objects (as long as they are contained in
   * the tree anyway). Keep in mind, that this will produce the active object
   * _NOT_ to be selected, if selectedObjects != null &&
   * !selectedObjects.contains(activeObject) !!!
   * 
   * @param se
   * @see magellan.client.event.SelectionListener#selectionChanged(magellan.client.event.SelectionEvent)
   */
  public void selectionChanged(SelectionEvent se) {
    // update the selection in the context manager
    if (se.getSelectionType() != SelectionEvent.ST_REGIONS) {
      if (se.getSelectedObjects()!=null) {
        contextManager.setSelection(se.getSelectedObjects());
      }
    }

    // try to prevent notification loops, i.e. that calling this
    // procedure results in a notification of the registered
    // listeners of this object
    if (se.getSource() == this) {
      return;
    }
    
    // clear current selection to avoid intermediate selections through
    // expand and collapse
    ignoreTreeSelections = true;
    tree.clearSelection();
    ignoreTreeSelections = false;

    collectCollapseInfo();
    resetExpandInfo();

    Collection<TreePath> newSel = new LinkedList<TreePath>();

    /** HANDLE activeObject : change alliance settings and expand info*/
    if (se.getActiveObject() instanceof Unique) {
      activeObject = (Unique)se.getActiveObject();

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

      // selectedObjects.add(activeObject);

      /**
       * The active object will be added to the selectedObjects automatically
       * when it will be selected in the tree This is done in
       * doExpandAndCollapse()
       */
      if (path != null) {
        newSel.add(path);
      }

      doExpandAndCollapse(newSel);
        
      if (path != null) {
        // center on the selected item if it is outside the view port
        SwingUtilities.invokeLater(new ScrollerRunnable(tree.getPathBounds(path)));
      }
    }

    /**
     * HANDLE selectedObjects Don't change anything, if selectedObjects == null
     * or selection event type is ST_REGIONS (which means that this is a
     * selection of regions on the map or of regions to be selected on the map.
     * Keep in mind that selections on the map don't have anything to do with
     * selections in the tree).
     */
    if ((se.getSelectedObjects() != null) && (se.getSelectionType() != SelectionEvent.ST_REGIONS)) {
      selectedObjects.clear();
      selectedObjects.addAll(se.getSelectedObjects());
      ignoreTreeSelections = true;
      tree.clearSelection();

      for (Iterator iter = selectedObjects.iterator(); iter.hasNext();) {
        Object o = iter.next();

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

      ignoreTreeSelections = false;
    }
  }

  protected void expandSelected(Region region, DefaultMutableTreeNode node, TreePath path) {
    if ((expandMode & EMapOverviewPanel.EXPAND_FLAG) != 0) {
      if ((expandMode & EMapOverviewPanel.EXPAND_IFINSIDE_FLAG) != 0) {
        // search for privileged faction
        // note: Node searching is not nice, but the fastest way
        boolean found = false;
        Enumeration enumeration = node.children();

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

        if (!found) {
          return;
        }
      }

      collapsedNodes.remove(node);

      if (!tree.isExpanded(path)) {
        lastExpanded.add(node);
        tree.expandPath(path);
      }

      int eDepth = expandMode >> 2;

      if (eDepth > 0) {
        Enumeration enumeration = node.children();
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
          List<Object> copy = new LinkedList<Object>(lastExpanded);
          Iterator it = copy.iterator();

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
        Enumeration e = rootNode.children();

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
    if (tree.isSelectionEmpty()) {
      return null;
    }

    List<TreeNode> list = new LinkedList<TreeNode>();

    if (include && tree.isPathSelected(new TreePath(node.getPath()))) {
      list.add(node);
    }

    Enumeration e = node.children();

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
   * DOCUMENT-ME
   * 
   * @param e
   *          DOCUMENT-ME
   */
  public void tempUnitCreated(TempUnitEvent e) {
    DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) unitNodes.get(e.getTempUnit().getParent().getID());

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
        EMapOverviewPanel.log.info("EMapOverviewPanel.tempUnitCreated(): new temp unit is not a child of its parent!");
      }
    } else {
      EMapOverviewPanel.log.info("EMapOverviewPanel.tempUnitCreated(): cannot determine parent node of temp unit!");
    }
  }

  /**
   * DOCUMENT-ME
   * 
   * @param e
   *          DOCUMENT-ME
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
   * Should return all short cuts this class want to be informed. The elements
   * should be of type javax.swing.KeyStroke
   * 
   * 
   */
  public Iterator<KeyStroke> getShortCuts() {
    return shortcuts.iterator();
  }

  /**
   * This method is called when a shortcut from getShortCuts() is recognized.
   * 
   * @param shortcut
   *          DOCUMENT-ME
   */
  public void shortCut(javax.swing.KeyStroke shortcut) {
    int index = shortcuts.indexOf(shortcut);

    switch (index) {
    case -1:
      break; // unknown shortcut

    case 0:
    case 1:
      DesktopEnvironment.requestFocus(EMapOverviewPanel.IDENTIFIER);
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
   * DOCUMENT-ME
   */
  public void shortCut_B() {
    toggleOrderConfirmation();
  }

  /**
   * DOCUMENT-ME
   */
  public void shortCut_N() {
    shortCut_N(true);
  }

  /**
   * DOCUMENT-ME
   */
  public void shortCut_Reverse_N() {
    shortCut_N(false);
  }

  protected void shortCut_N(boolean traverseDown) {
    if ((tree == null) || (rootNode == null) || (tree.getSelectionPath() == null)) {
      // fail fast
      return;
    }

    DefaultMutableTreeNode actNode = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
    Unit u = null;

    if (traverseDown) {
      // iterate over objects after actNode
      u = getUnitInTree(iterateToNode(rootNode.preorderEnumeration(), actNode), actNode, traverseDown);

      if (u == null) {
        // iterate over objects before actNode
        u = getUnitInTree(rootNode.preorderEnumeration(), actNode, traverseDown);
      }
    } else {
      // iterate over objects before actNode
      u = getUnitInTree(rootNode.preorderEnumeration(), actNode, traverseDown);

      if (u == null) {
        // iterate over objects after actNode
        u = getUnitInTree(iterateToNode(rootNode.preorderEnumeration(), actNode), actNode, traverseDown);
      }
    }

    if (u != null) {
      if (EMapOverviewPanel.log.isDebugEnabled()) {
        EMapOverviewPanel.log.debug("EMapOverviewPanel.shortCut_N(): firing Selection Event with Unit " + u + " (" + u.isOrdersConfirmed() + ")");
      }


      // event source u? yeah, just provide some
      // stupid value here, null is prohibited, else
      // the selectionChanged() method would reject
      // the event since it originates from this.
      selectionChanged(new SelectionEvent(u, null, u));
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

  private Enumeration<?> iterateToNode(Enumeration nodes, Object actNode) {
    while (nodes.hasMoreElements()) {
      if (nodes.nextElement().equals(actNode)) {
        break;
      }
    }

    return nodes;
  }

  private Unit getUnitInTree(Enumeration nodes, Object actNode, boolean first) {
    Unit ret = null;

    while (nodes.hasMoreElements()) {
      DefaultMutableTreeNode nextNode = (DefaultMutableTreeNode) nodes.nextElement();

      if (nextNode.equals(actNode)) {
        // return latest found
        return ret;
      }

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
          if (first) {
            // ... and we are interested in the *first* hit
            return ret;
          }
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
    return new RegionOverviewPreferences(this, settings, data);
  }

  /**
   * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
   */
  public void stateChanged(javax.swing.event.ChangeEvent p1) {
    // update the history list
    lstHistory.setListData(SelectionHistory.getHistory().toArray());
  }

  /**
   * Repaints this component. This method also explicitly repaints it's managed
   * components because the may be separated by the Magellan Desktop.
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
    if (activeObject == null) {
      return;
    }

    if (activeObject instanceof Region) {
      Region r = (Region) activeObject;
      boolean first = true;
      boolean confirm = false;

      for (Iterator iter = r.units().iterator(); iter.hasNext();) {
        Unit u = (Unit) iter.next();

//        if (EMapDetailsPanel.isPrivilegedAndNoSpy(u)) {
          if (first) {
            confirm = !u.isOrdersConfirmed();
            first = false;
          }

          u.setOrdersConfirmed(confirm);
//        }
      }

      dispatcher.fire(new OrderConfirmEvent(this, r.units()));
    } else if (activeObject instanceof Unit) {
      Unit u = (Unit) activeObject;

//      if (EMapDetailsPanel.isPrivilegedAndNoSpy(u)) {
        u.setOrdersConfirmed(!u.isOrdersConfirmed());

        List<Unit> units = new LinkedList<Unit>();
        units.add(u);
        dispatcher.fire(new OrderConfirmEvent(this, units));
//      }
    }

    tree.invalidate();
    tree.repaint();
  }

  /**
   * Do clean-up on quitting.
   */
  @Override
  public void quit() {
    settings.setProperty("EMapOverviewPanel.treeHistorySplit", Integer.toString(sppTreeHistory.getDividerLocation()));
  }

  /**
   * Selects and entry in the selection history by a relative offset.
   * 
   * @param i
   *          DOCUMENT-ME
   */
  private void jumpInSelectionHistory(int i) {
    ListModel model = lstHistory.getModel();

    if (model.getSize() > 0) {
      int selectedIndex = Math.max(lstHistory.getSelectedIndex(), 0);
      int newIndex = Math.min(Math.max(selectedIndex + i, 0), model.getSize() - 1);

      if (selectedIndex != newIndex) {
        lstHistory.setSelectedIndex(newIndex);
        dispatcher.fire(new SelectionEvent(lstHistory, null, lstHistory.getModel().getElementAt(newIndex)));
      }
    }
  }

  /**
   * This is a helper method to set this.activeAlliances to a usefull value, if
   * no faction or group is active. The idea is to take all alliances of all
   * privileged factions and combine their states by & (or in other words to
   * take the intersection over all alliances of all privileged factions)
   */
  private void setDefaultAlliances() {
    activeAlliancesAreDefault = true;

    List<Faction> privilegedFactions = new LinkedList<Faction>();
    activeAlliances.clear();

    boolean privilegedWithoutAllies = false;

    for (Iterator iter = data.factions().values().iterator(); iter.hasNext();) {
      Faction f = (Faction) iter.next();

      if (f.isPrivileged()) {
        privilegedFactions.add(f);

        if ((f.getAllies() == null) || (f.getAllies().values().size() <= 0)) {
          // remember that one privileged faction had no allies
          // so it is not necessary to do further calculations
          privilegedWithoutAllies = true;
        }
      }
    }

    if (!privilegedWithoutAllies) {
      // take the alliances of the first found privileged faction as
      // activeAlliances
      if (privilegedFactions.size() > 0) {
        activeAlliances.putAll((privilegedFactions.get(0)).getAllies());
      }

      // now check whether they are contained in the alliances-Maps of the other
      // privileged factions and adjust their states if necessary
      boolean delEntry = false;

      for (Iterator iter = activeAlliances.keySet().iterator(); iter.hasNext();) {
        ID id = (ID) iter.next();

        for (int factionCount = 1; factionCount < privilegedFactions.size(); factionCount++) {
          Faction f = privilegedFactions.get(factionCount);

          if (!f.getAllies().containsKey(id)) {
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

    // now add all privileged factions with alliance state Integer.MAX_VALUE
    for (Iterator iter = privilegedFactions.iterator(); iter.hasNext();) {
      Faction f = (Faction) iter.next();
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
      expandTrustlevel = Integer.parseInt(settings.getProperty("EMapOverviewPanel.ExpandTrustlevel"));
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
   * DOCUMENT-ME
   * 
   * @param src
   *          DOCUMENT-ME
   */
  public void updateTree(Object src) {
    tree.treeDidChange();

    // Use the UI Fix
    javax.swing.plaf.TreeUI treeUI = tree.getUI();

    if (treeUI instanceof javax.swing.plaf.basic.BasicTreeUI) {
      javax.swing.plaf.basic.BasicTreeUI ui2 = (javax.swing.plaf.basic.BasicTreeUI) treeUI;
      int i = ui2.getLeftChildIndent();
      ui2.setLeftChildIndent(100);
      ui2.setLeftChildIndent(i);
    }
    tree.revalidate();
    tree.repaint();
  }

  /**
   * DOCUMENT-ME
   * 
   * @param stroke
   *          DOCUMENT-ME
   * 
   */
  public String getShortcutDescription(Object stroke) {
    int index = shortcuts.indexOf(stroke);

    return Resources.get("emapoverviewpanel.shortcut.description." + String.valueOf(index));
  }

  /**
   * DOCUMENT-ME
   * 
   * 
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
     *          DOCUMENT-ME
     */
    public ScrollerRunnable(Rectangle r) {
      centerRect = r;
    }

    /**
     * DOCUMENT-ME
     */
    public void run() {
      if (centerRect != null) {
        Rectangle viewRect = scpTree.getViewport().getViewRect();
        centerRect.x = viewRect.x;
        centerRect.width = viewRect.width;

        if (viewRect.contains(centerRect) == false) {
          Point viewPos = new Point(0, centerRect.y);

          if (centerRect.height < viewRect.height) {
            viewPos.y = Math.min(tree.getHeight() - viewRect.height, centerRect.y - ((viewRect.height - centerRect.height) / 2));
          }

          scpTree.getViewport().setViewPosition(viewPos);
        }
      }
    }
  }

  /**
   * Updates UnitNodeWrappers on changes of orders
   */
  private class UnitWrapperUpdater implements UnitOrdersListener {
    Collection<UnitRelation> buff1 = new LinkedList<UnitRelation>();
    Collection<UnitRelation> buff2 = new LinkedList<UnitRelation>();
    
    /**
     * Invoked when the orders of a unit are modified.
     * 
     * @param e
     *          DOCUMENT-ME
     */
    public void unitOrdersChanged(UnitOrdersEvent e) {
      update(e.getUnit(), true);
    }

    protected synchronized void update(Unit u, boolean updateRelationPartners) {
      if (unitNodes.containsKey(u.getID())) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) unitNodes.get(u.getID());
        UnitNodeWrapper unw = (UnitNodeWrapper) node.getUserObject();
        unw.clearBuffer();
        treeModel.nodeChanged(node);

        Collection<UnitRelation> relations = u.getRelations(TransferRelation.class);

        // ensure that unitRelations has an entry for u
        if (!unitRelations.containsKey(u.getID())) {
          if (relations.size() == 0) {
            return;
          }

          unitRelations.put(u.getID(), new LinkedList<UnitRelation>());
        }

        
        List<UnitRelation> oldRelations = unitRelations.get(u.getID());
        Collection<UnitRelation> buffer = updateRelationPartners ? buff1 : buff2;

        if (!relations.equals(oldRelations)) {
          buffer.clear();
          buffer.addAll(oldRelations);

          for (UnitRelation o : relations){
            if (oldRelations.contains(o)) {
              buffer.remove(o);
            } else {
              oldRelations.add(o);

              if (updateRelationPartners) {
                update(((TransferRelation) o).target, false);
              }
            }
          }

          for (Object o : buffer){
            oldRelations.remove(o);

            if (updateRelationPartners) {
              update(((TransferRelation) o).target, false);
            }
          }
        }
      }
    }
  }


  protected TreeBuilder createTreeBuilder() {
    TreeBuilder treeBuilder = new TreeBuilder(settings,nodeWrapperFactory);
    treeBuilder.setRegionNodes(regionNodes);
    treeBuilder.setUnitNodes(unitNodes);
    treeBuilder.setShipNodes(shipNodes);
    treeBuilder.setBuildingNodes(buildingNodes);
    treeBuilder.setActiveAlliances(activeAlliances);
    /**
     * Fiete Default fuer den Modus auf UNITS | SHIPS | BUILDINGS |COMMENTS
     * setzen nach Vorgabe stm
     */
    treeBuilder.setDisplayMode(Integer.parseInt(settings.getProperty("EMapOverviewPanel.filters", new Integer(TreeBuilder.UNITS | TreeBuilder.BUILDINGS | TreeBuilder.SHIPS | TreeBuilder.COMMENTS).toString())));
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
     *          DOCUMENT-ME
     * @param mnemonic
     *          DOCUMENT-ME
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
        items[i] = new JCheckBoxMenuItem(Resources.get("emapoverviewpanel.menu.filter." + String.valueOf(i)));
        items[i].setSelected((mode & (1 << i)) != 0);
        items[i].addActionListener(this);
        this.add(items[i]);
      }
    }

    /**
     * DOCUMENT-ME
     * 
     * @param actionEvent
     *          DOCUMENT-ME
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
        settings.setProperty("EMapOverviewPanel.filters", String.valueOf(mode ^ TreeBuilder.CREATE_ISLANDS));
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
    return new OverviewMenu(Resources.get("emapoverviewpanel.menu.caption"), Resources.get("emapoverviewpanel.menu.mnemonic").charAt(0));
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
  public Collection<?> getSelectedObjects() {
    return this.contextManager.getSelection();
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
