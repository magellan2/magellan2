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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.border.AbstractBorder;
import javax.swing.border.TitledBorder;
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
import magellan.client.swing.InternationalizedDataPanel;
import magellan.client.swing.MenuProvider;
import magellan.client.swing.context.UnitContainerContextFactory;
import magellan.client.swing.context.UnitContextFactory;
import magellan.client.swing.preferences.ExtendedPreferencesAdapter;
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
import magellan.client.utils.ImageFactory;
import magellan.client.utils.SelectionHistory;
import magellan.library.Alliance;
import magellan.library.Building;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Group;
import magellan.library.ID;
import magellan.library.Island;
import magellan.library.Named;
import magellan.library.Region;
import magellan.library.Ship;
import magellan.library.TempUnit;
import magellan.library.Unique;
import magellan.library.Unit;
import magellan.library.UnitID;
import magellan.library.ZeroUnit;
import magellan.library.event.GameDataEvent;
import magellan.library.event.GameDataListener;
import magellan.library.relation.TransferRelation;
import magellan.library.relation.UnitRelation;
import magellan.library.rules.SkillType;
import magellan.library.utils.MagellanFactory;
import magellan.library.utils.PropertiesHelper;
import magellan.library.utils.Resources;
import magellan.library.utils.Umlaut;
import magellan.library.utils.comparator.BestSkillComparator;
import magellan.library.utils.comparator.IDComparator;
import magellan.library.utils.comparator.NameComparator;
import magellan.library.utils.comparator.RegionIslandComparator;
import magellan.library.utils.comparator.SkillComparator;
import magellan.library.utils.comparator.SkillTypeComparator;
import magellan.library.utils.comparator.SkillTypeRankComparator;
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
public class EMapOverviewPanel extends InternationalizedDataPanel implements TreeSelectionListener, TreeExpansionListener, SelectionListener, OrderConfirmListener, PreferencesFactory, TempUnitListener, ShortcutListener, ChangeListener, TreeUpdate, MenuProvider, Initializable {
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
  private Object activeObject = null;
  private List<Unique> selectedObjects = new LinkedList<Unique>();

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
  private int expandMode = EXPAND_FLAG | (3 << 2);
  private int expandTrustlevel = Faction.TL_PRIVILEGED;

  /** DOCUMENT-ME */
  public static final int COLLAPSE_FLAG = 1;

  /** DOCUMENT-ME */
  public static final int COLLAPSE_ONLY_EXPANDED = 2;
  private int collapseMode = COLLAPSE_FLAG | COLLAPSE_ONLY_EXPANDED | (3 << 2);
  private List<TreeNode> lastExpanded = new LinkedList<TreeNode>();
  private Set<TreeNode> collapsedNodes = new HashSet<TreeNode>();
  private Set<TreeNode> collapseInfo = new HashSet<TreeNode>();
  private Set<TreeNode> expandInfo = new HashSet<TreeNode>();
  private Set<TreePath> selectionTransfer = new HashSet<TreePath>();
  private static final Comparator<Unique> idCmp = IDComparator.DEFAULT;
  private static final Comparator<Named> nameCmp = new NameComparator<Unique>(idCmp);

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
    shortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_1, KeyEvent.CTRL_MASK));
    shortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_1, KeyEvent.ALT_MASK));

    // 2-4: Other CTRL shortcuts
    // shortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_N,KeyEvent.CTRL_MASK));
    // shortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_B,KeyEvent.CTRL_MASK));
    shortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, KeyEvent.CTRL_MASK));
    shortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, KeyEvent.CTRL_MASK));

    // 5-6: Other ALT shortcuts
    shortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.CTRL_MASK));
    shortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.CTRL_MASK));

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
  public void gameDataChanged(GameDataEvent e) {
    this.data = e.getGameData();

    contextManager.setGameData(this.data);

    rebuildTree();

    // initialize activeAlliances-Map
    setDefaultAlliances();
  }

  protected void rebuildTree() {
    Object oldActiveObject = activeObject;
    Collection<Unique> oldSelectedObjects = new LinkedList<Unique>(selectedObjects);

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
    boolean createIslandNodes = PropertiesHelper.getboolean(settings, "EMapOverviewPanel.displayIslands", true) && PropertiesHelper.getboolean(settings, "EMapOverviewPanel.sortRegions", true) && settings.getProperty("EMapOverviewPanel.sortRegionsCriteria", "coordinates").equalsIgnoreCase("islands");

    boolean sortShipUnderUnitParent = PropertiesHelper.getboolean(settings, "EMapOverviewPanel.sortShipUnderUnitParent", true);

    TreeBuilder treeBuilder = getTreeBuilder();
    treeBuilder.setSortShipUnderUnitParent(sortShipUnderUnitParent);

    int displayMode = TreeBuilder.UNITS;
    if (PropertiesHelper.getboolean(settings, "EMapOverviewPanel.treeBuilderWithBuildings", true)) {
      displayMode = displayMode | TreeBuilder.BUILDINGS;
    }
    if (PropertiesHelper.getboolean(settings, "EMapOverviewPanel.treeBuilderWithShips", true)) {
      displayMode = displayMode | TreeBuilder.SHIPS;
    }
    if (PropertiesHelper.getboolean(settings, "EMapOverviewPanel.treeBuilderWithComments", true)) {
      displayMode = displayMode | TreeBuilder.COMMENTS;
    }
    treeBuilder.setDisplayMode(displayMode | (createIslandNodes ? TreeBuilder.CREATE_ISLANDS : 0));

    // creation of Comparator outsourced to Comparator
    // getUnitSorting(java.util.Properties)
    treeBuilder.setUnitComparator(getUnitSorting(settings));
    treeBuilder.setTreeStructure(getTreeStructure(settings));

    treeBuilder.buildTree(rootNode, data);

    tree.setShowsRootHandles(PropertiesHelper.getboolean(settings, "EMapOverviewPanel.treeRootHandles", true));

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
  public static Comparator getUnitSorting(Properties settings) {
    // create Comparator used for unit sorting
    String criteria = settings.getProperty("EMapOverviewPanel.sortUnitsCriteria", "skills");

    // used as the comparator on the lowest structure level
    Comparator cmp = null;

    if (criteria.equals("skills")) {
      if (settings.getProperty("EMapOverviewPanel.useBestSkill", "true").equalsIgnoreCase("true")) {
        cmp = new UnitSkillComparator(new BestSkillComparator(SkillComparator.skillCmp, new SkillTypeComparator(new SkillTypeRankComparator<Named>(new NameComparator<Unique>(null), settings), SkillComparator.skillCmp), null), idCmp);
      } else {
        cmp = new UnitSkillComparator(new TopmostRankedSkillComparator<Object>(null, settings), idCmp);
      }
    } else if (criteria.equals("names")) {
      cmp = nameCmp;
    } else {
      cmp = new SortIndexComparator<Unique>(idCmp);
    }

    // get an array of ints out of the definition string:
    int treeStructure[] = getTreeStructure(settings);

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
          selectedObjects.add((Unique) o);
        } else {
          selectedObjects.remove(o);
        }
      }
    }
    
    Collection<Object> selectionPath = new ArrayList<Object>();
    if (tree!=null && tree.getSelectionPath()!=null)
    for (Object o : tree.getSelectionPath().getPath()){
      selectionPath.add(getNodeSubject((DefaultMutableTreeNode)o));
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
    if (o==null)
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
      log.warn("EMapOverviewPanel.valueChanged() : Type of the user object of a selected node is unknown:" + o);
    }
    return o;
  }

  /**
   * Sets the active alliance status that are used to paint the faction or group
   * nodes in the tree.
   * 
   * @param allies
   *          The alliances map to be used
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

  private Region getSelectedRegion(DefaultMutableTreeNode node) {
    while ((node != null) && !(node.getUserObject() instanceof RegionNodeWrapper)) {
      node = (DefaultMutableTreeNode) node.getParent();
    }

    if ((node != null) && node.getUserObject() instanceof RegionNodeWrapper) {
      return ((RegionNodeWrapper) node.getUserObject()).getRegion();
    } else {
      return null;
    }
  }

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

      if (tree.getPathBounds(path)!=null)
        rec.add(tree.getPathBounds(path));
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
    if ((collapseMode & COLLAPSE_ONLY_EXPANDED) == 0) {
      collapseInfo.clear();
    }

    if ((collapseMode & COLLAPSE_FLAG) != 0) {
      int depth = collapseMode >> 2;

      if ((collapseMode & COLLAPSE_ONLY_EXPANDED) != 0) {
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
    if ((expandMode & EXPAND_FLAG) != 0) {
      if ((expandMode & EXPAND_IFINSIDE_FLAG) != 0) {
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

        TreePath path2 = (TreePath) pathsToExpand.get(node2);

        if (path.isDescendant(path2)) {
          it.remove();

          // but save it for last expand on expand-only mode
          if ((collapseMode & COLLAPSE_ONLY_EXPANDED) != 0) {
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
        path = (TreePath) pathsToExpand.get(node);
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
    TreePath paths[] = (TreePath []) selectionTransfer.toArray(new TreePath[0]);

    tree.setSelectionPaths(paths);

    // now collapse
    // (this should'nt trigger selection events any more)
    it = collapseInfo.iterator();

    while (it.hasNext()) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) it.next();
      TreePath path = (TreePath) pathsToCollapse.get(node);

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
   * that selected objects are considered, but only if != null and selection
   * type is different to SelectionEvent.ST_REGIONS. In this case the tree
   * selection is set to the selected objects (as long as they are contained in
   * the tree anyway). Keep in mind, thtat this will produce the active object
   * _NOT_ to be selected, if selectedObjects != null &&
   * !selectedObjects.contains(activeObject) !!!
   * 
   * @param se
   *          DOCUMENT-ME
   */
  public void selectionChanged(SelectionEvent se) {
    // update the selection in the context manager
    if (se.getSelectionType() != SelectionEvent.ST_REGIONS) {
      if (se.getSelectedObjects()!=null)
        contextManager.setSelection(se.getSelectedObjects());
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
    if (se.getActiveObject() != null) {
      activeObject = se.getActiveObject();

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
    if ((expandMode & EXPAND_FLAG) != 0) {
      if ((expandMode & EXPAND_IFINSIDE_FLAG) != 0) {
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
    if ((collapseMode & COLLAPSE_FLAG) != 0) {
      int depth = collapseMode >> 2;

      if ((collapseMode & COLLAPSE_ONLY_EXPANDED) != 0) {
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
        log.info("EMapOverviewPanel.tempUnitCreated(): new temp unit is not a child of its parent!");
      }
    } else {
      log.info("EMapOverviewPanel.tempUnitCreated(): cannot determine parent node of temp unit!");
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

  /**
   * Sort a collection of regions in a specific order.
   * 
   * @param regions
   *          DOCUMENT-ME
   * 
   */
  private Collection sortRegions(Collection<Region> regions) {
    if ((Boolean.valueOf(settings.getProperty("EMapOverviewPanel.sortRegions", "true"))).booleanValue()) {
      if (settings.getProperty("EMapOverviewPanel.sortRegionsCriteria", "coordinates").equals("coordinates")) {
        List<Region> sortedRegions = new LinkedList<Region>(regions);
        Collections.sort(sortedRegions, IDComparator.DEFAULT);

        return sortedRegions;
      } else if (settings.getProperty("EMapOverviewPanel.sortRegionsCriteria", "coordinates").equals("islands")) {
        List<Region> sortedRegions = new LinkedList<Region>(regions);
        Comparator<Unique> idCmp = IDComparator.DEFAULT;
        Collections.sort(sortedRegions, new RegionIslandComparator(new NameComparator<Unique>(idCmp), idCmp, idCmp));

        return sortedRegions;
      } else {
        return regions;
      }
    } else {
      return regions;
    }
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
      DesktopEnvironment.requestFocus(IDENTIFIER);
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
      if (log.isDebugEnabled()) {
        log.debug("EMapOverviewPanel.shortCut_N(): firing Selection Event with Unit " + u + " (" + u.isOrdersConfirmed() + ")");
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

  private Enumeration iterateToNode(Enumeration nodes, Object actNode) {
    while (nodes.hasMoreElements()) {
      if (nodes.nextElement().equals(actNode)) {
        break;
      }
    }

    return nodes;
  }

  private Unit getUnitInTree(Enumeration<TreeNode> nodes, Object actNode, boolean first) {
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
   * DOCUMENT-ME
   * 
   * 
   */
  public PreferencesAdapter createPreferencesAdapter() {
    return new EMapOverviewPreferences(this, settings);
  }

  /**
   * DOCUMENT-ME
   * 
   * @param p1
   *          DOCUMENT-ME
   */
  public void stateChanged(javax.swing.event.ChangeEvent p1) {
    // update the history list
    lstHistory.setListData(SelectionHistory.getHistory().toArray());
  }

  /**
   * Repaints this component. This method also explicitly repaints it's managed
   * components because the may be separated by the Magellan Desktop.
   */
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
        activeAlliances.putAll(((Faction) privilegedFactions.get(0)).getAllies());
      }

      // now check whether they are contained in the alliances-Maps of the other
      // privileged factions and adjust their states if necessary
      boolean delEntry = false;

      for (Iterator iter = activeAlliances.keySet().iterator(); iter.hasNext();) {
        ID id = (ID) iter.next();

        for (int factionCount = 1; factionCount < privilegedFactions.size(); factionCount++) {
          Faction f = (Faction) privilegedFactions.get(factionCount);

          if (!f.getAllies().containsKey(id)) {
            // mark this alliances as to be deleted out of activeAlliances
            delEntry = true;

            break;
          } else {
            Alliance a1 = (Alliance) activeAlliances.get(id);
            Alliance a2 = (Alliance) f.getAllies().get(id);
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
      expandMode = EXPAND_FLAG | (3 << 2);
    }

    try {
      expandTrustlevel = Integer.parseInt(settings.getProperty("EMapOverviewPanel.ExpandTrustlevel"));
    } catch (Exception exc) {
      expandTrustlevel = Faction.TL_PRIVILEGED;
    }
  }

  protected void saveExpandProperties() {
    settings.setProperty("EMapOverviewPanel.ExpandMode", String.valueOf(expandMode));
    settings.setProperty("EMapOverviewPanel.ExpandTrustlevel", String.valueOf(expandTrustlevel));
  }

  protected void loadCollapseProperty() {
    try {
      collapseMode = Integer.parseInt(settings.getProperty("EMapOverviewPanel.CollapseMode"));
    } catch (Exception exc) {
      expandMode = 3 << 2;
    }
  }

  protected void saveCollapseProperty() {
    settings.setProperty("EMapOverviewPanel.CollapseMode", String.valueOf(collapseMode));
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

  /**
   * 
   * Provides Panel (Extended Preferences Adapter) for Preferences
   *
   * @author ...
   * @version 1.0, 20.11.2007
   */
  private class EMapOverviewPreferences extends JPanel implements ExtendedPreferencesAdapter {
    
    /**
     * 
     * Panel for maintainig the SkillTypeList for sorting after 
     * skillType
     *
     * @author ...
     * @version 1.0, 20.11.2007
     */
    private class SkillPreferences extends JPanel implements PreferencesAdapter, GameDataListener {
      
        /**
         * 
         * An extra cell renderer to display the skills in the list
         *
         * @author ...
         * @version 1.0, 20.11.2007
         */
        private class MyCellRenderer extends JLabel implements ListCellRenderer {
          // we need a reference to the translations
          private GameData data=null;
          // we need a reference to the ImageFactory
          private ImageFactory imageFactory=null;
          
          /**
           * Constructs a new extra cell renderer for our skill list
           * @param _data
           * @param _imageFactory
           */
           
          public MyCellRenderer(GameData _data, ImageFactory _imageFactory){
            this.data = _data;
            this.imageFactory = _imageFactory;
          }
          
          /**
           * returns the JLabel to display in our skill list
           * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
           */
          public Component getListCellRendererComponent(
            JList list,
            Object value,            // value to display
            int index,               // cell index
            boolean isSelected,      // is the cell selected
            boolean cellHasFocus)    // the list and the cell have the focus
          {
              String s = value.toString();
              String normalizedIconName = Umlaut.convertUmlauts(s).toLowerCase();
              s = this.data.getTranslation(s);
              setText(s);
              setIcon(this.imageFactory.loadImageIcon(normalizedIconName));
              if (isSelected) {
                setBackground(list.getSelectionBackground());
                  setForeground(list.getSelectionForeground());
              } else {
                    setBackground(list.getBackground());
                    setForeground(list.getForeground());
              }
              setEnabled(list.isEnabled());
              setFont(list.getFont());
              setOpaque(true);
              return this;
          }
      }
      
      /**
       * 
       * a small comparator to compare translated skillNames
       *
       * @author ...
       * @version 1.0, 20.11.2007
       */  
      private class SkillTypeComparator implements Comparator<SkillType> {
        
        // Reference to Translations
        private GameData data=null;
        
        /**
         * constructs new Comparator
         * @param _data
         */
        public SkillTypeComparator(GameData _data){
          this.data = _data;
        }
        
        public int compare(SkillType o1,SkillType o2){
          String s1 = data.getTranslation(o1.getName());
          String s2 = data.getTranslation(o2.getName());
          return s1.compareToIgnoreCase(s2);
        }
      }
        
      
      /** DOCUMENT-ME */
      public JList skillList = null;
      private JButton upButton = null;
      private JButton downButton = null;
      private JButton refreshListButton = null;
      
      private SkillTypeComparator skillTypeComparator = null;

      /**
       * Creates a new SkillPreferences object.
       */
      public SkillPreferences(EventDispatcher d, ImageFactory imageFactory) {
        this.setLayout(new BorderLayout());
        this.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Resources.get("emapoverviewpanel.prefs.skillorder")));
        
        d.addGameDataListener(this);
        
        skillList = new JList();
        skillList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        skillList.setCellRenderer(new MyCellRenderer(data,imageFactory));
        // entries for List are updated in initPreferences
        
        this.add(new JScrollPane(skillList), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 1, 2, 1), 0, 0);

        upButton = new JButton(Resources.get("emapoverviewpanel.prefs.upbutton.caption"));
        upButton.setPreferredSize(new Dimension(110, 40));
        upButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if ((skillList.getModel() == null) || (skillList.getModel().getSize() == 0)) {
              return;
            }

            int selIndices[] = skillList.getSelectedIndices();

            if (selIndices.length == 0) {
              return;
            }

            List newData = new LinkedList();
            ListModel oldData = skillList.getModel();
            List<Integer> newSelectedIndices = new LinkedList<Integer>();

            for (int i = 0; i < oldData.getSize(); i++) {
              Object o = oldData.getElementAt(i);

              if (skillList.isSelectedIndex(i)) {
                int newPos;

                if ((i > 0) && !newSelectedIndices.contains(new Integer(i - 1))) {
                  newPos = i - 1;
                } else {
                  newPos = i;
                }

                newData.add(newPos, o);
                newSelectedIndices.add(new Integer(newPos));
              } else {
                newData.add(o);
              }
            }

            skillList.setListData(newData.toArray());

            int selection[] = new int[newSelectedIndices.size()];
            int i = 0;

            for (Iterator iter = newSelectedIndices.iterator(); iter.hasNext(); i++) {
              selection[i] = ((Integer) iter.next()).intValue();
            }

            skillList.setSelectedIndices(selection);
            skillList.ensureIndexIsVisible(selection[0]);
          }
        });
        buttons.add(upButton, c);

        c.gridy++;

        downButton = new JButton(Resources.get("emapoverviewpanel.prefs.downbutton.caption"));
        downButton.setPreferredSize(new Dimension(110, 40));
        downButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if ((skillList.getModel() == null) || (skillList.getModel().getSize() == 0)) {
              return;
            }

            int selIndices[] = skillList.getSelectedIndices();

            if (selIndices.length == 0) {
              return;
            }

            List<Object> newData = new LinkedList<Object>();
            ListModel oldData = skillList.getModel();
            List<Integer> newSelectedIndices = new LinkedList<Integer>();

            for (int i = oldData.getSize() - 1; i >= 0; i--) {
              Object o = oldData.getElementAt(i);

              if (skillList.isSelectedIndex(i)) {
                int newPos;

                if ((i < (oldData.getSize() - 1)) && !newSelectedIndices.contains(new Integer(i + 1))) {
                  newPos = i + 1;
                  newData.add(1, o);
                } else {
                  newPos = i;
                  newData.add(0, o);
                }

                newSelectedIndices.add(new Integer(newPos));
              } else {
                newData.add(0, o);
              }
            }

            skillList.setListData(newData.toArray());

            int selection[] = new int[newSelectedIndices.size()];
            int i = 0;

            for (Iterator iter = newSelectedIndices.iterator(); iter.hasNext(); i++) {
              selection[i] = ((Integer) iter.next()).intValue();
            }

            skillList.setSelectedIndices(selection);
            skillList.ensureIndexIsVisible(selection[0]);
          }
        });
        buttons.add(downButton, c);

        // add a filler
        c.gridy++;
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        buttons.add(new JPanel(), c);

        c.anchor = GridBagConstraints.SOUTHWEST;
        c.gridy++;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weighty = 0;
        c.insets.bottom = 0;

        refreshListButton = new JButton(Resources.get("emapoverviewpanel.prefs.refreshlistbutton.caption"));
        refreshListButton.setPreferredSize(new Dimension(110, 40));
        refreshListButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if ((skillList.getModel() == null) || (skillList.getModel().getSize() == 0)) {
              return;
            }

            ListModel listData = skillList.getModel();
            List<SkillType> v = new LinkedList<SkillType>();

            for (int index = 0; index < listData.getSize(); index++) {
              v.add((SkillType)listData.getElementAt(index));
            }
            
            if (skillTypeComparator==null){
              skillTypeComparator = new SkillTypeComparator(data);
            }
            
            Collections.sort(v,skillTypeComparator);
            skillList.setListData(v.toArray());
          }
        });
        buttons.add(refreshListButton, c);

        this.add(buttons, BorderLayout.EAST);
        this.initPreferences();
      }

      /**
       * DOCUMENT-ME
       * 
       * @param enable
       *          DOCUMENT-ME
       */
      public void setEnabled(boolean enable) {
        super.setEnabled(enable);
        skillList.setEnabled(enable);
        upButton.setEnabled(enable);
        downButton.setEnabled(enable);
        refreshListButton.setEnabled(enable);
      }

      // Game Data has changed
      public void gameDataChanged(GameDataEvent e){
        data = e.getGameData();
        this.initPreferences();
      }
      
      /**
       * fills the values
       */
      public void initPreferences() {
        if (data != null) {
          List<SkillType> v = new LinkedList<SkillType>();

          for (Iterator iter = data.rules.getSkillTypeIterator(); iter.hasNext();) {
            SkillType type = (SkillType) iter.next();
            v.add(type);
          }

          Collections.sort(v, new SkillTypeRankComparator<Named>(new NameComparator<Unique>(IDComparator.DEFAULT), EMapOverviewPanel.this.settings));
          skillList.setListData(v.toArray());
          
          if (v.size()>0){
            setEnabled(true);
          } else {
            setEnabled(false);
          }
        } else {
          setEnabled(false);
        }
      }

      /**
       * DOCUMENT-ME
       * 
       * 
       */
      public Component getComponent() {
        return this;
      }

      /**
       * DOCUMENT-ME
       * 
       * 
       */
      public String getTitle() {
        return Resources.get("emapoverviewpanel.prefs.skillorder");
      }

      /**
       * DOCUMENT-ME
       */
      public void applyPreferences() {
        ListModel listData = skillList.getModel();

        for (int index = 0; index < listData.getSize(); index++) {
          SkillType s = (SkillType) listData.getElementAt(index);
          settings.setProperty("ClientPreferences.compareValue." + s.getID(), String.valueOf(index));

        }
      }
    }

    private EMapOverviewPanel overviewPanel = null;

    /** DOCUMENT-ME */
    public JCheckBox chkSortRegions = null;

    /**
     * TODO DOCUMENT ME! Comment for <code>chkSortShipUnderUnitParent</code>.
     */
    public JCheckBox chkSortShipUnderUnitParent = null;

    /** DOCUMENT-ME */
    public JRadioButton rdbSortRegionsCoordinates = null;

    /** DOCUMENT-ME */
    public JRadioButton rdbSortRegionsIslands = null;

    /** DOCUMENT-ME */
    public JCheckBox chkDisplayIslands = null;

    /** DOCUMENT-ME */
    public JRadioButton rdbSortUnitsUnsorted = null;

    /** DOCUMENT-ME */
    public JRadioButton rdbSortUnitsSkills = null;

    // use the best skill of the unit to sort it

    /** DOCUMENT-ME */
    public JRadioButton useBestSkill = null;

    /**
     * if true, regiontree will contain regions without own units but with
     * buildings known in it
     */
    public JCheckBox chkRegionTreeBuilder_withBuildings = null;

    /**
     * if true, regiontree will contain regions without own units but with Ships
     * known in it
     */
    public JCheckBox chkRegionTreeBuilder_withShips = null;

    /**
     * if true, regiontree will contain regions without own units but with
     * Comments known in it
     */
    public JCheckBox chkRegionTreeBuilder_withComments = null;

    /** if true, region tree's top nodes will have handles */
    public JCheckBox chkRootHandles = null;
    
    // use the topmost skill in (selfdefined) skilltype-list to sort it

    /** DOCUMENT-ME */
    public JRadioButton useTopmostSkill = null;

    /** DOCUMENT-ME */
    public JRadioButton rdbSortUnitsNames = null;
    protected ExpandPanel ePanel;
    protected CollapsePanel cPanel;
    private SkillPreferences skillSort;
    private List<SkillPreferences> subAdapter;
    private JList useList;
    private JList elementsList;

    /**
     * Creates a new EMapOverviewPreferences object.
     * 
     * @param settings
     *          DOCUMENT-ME
     */
    public EMapOverviewPreferences(EMapOverviewPanel parent, Properties settings) {
      overviewPanel = parent;
      chkSortRegions = new JCheckBox(Resources.get("emapoverviewpanel.prefs.sortregions"));

      chkSortShipUnderUnitParent = new JCheckBox(Resources.get("emapoverviewpanel.prefs.sortShipUnderUnitParent"));

      rdbSortRegionsCoordinates = new JRadioButton(Resources.get("emapoverviewpanel.prefs.sortbycoordinates"));

      rdbSortRegionsIslands = new JRadioButton(Resources.get("emapoverviewpanel.prefs.sortbyislands"));

      ButtonGroup regionSortButtons = new ButtonGroup();
      regionSortButtons.add(rdbSortRegionsCoordinates);
      regionSortButtons.add(rdbSortRegionsIslands);

      JPanel pnlRegionSortButtons = new JPanel();
      pnlRegionSortButtons.setLayout(new BoxLayout(pnlRegionSortButtons, BoxLayout.Y_AXIS));
      pnlRegionSortButtons.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Resources.get("emapoverviewpanel.prefs.regionsorting")));
      pnlRegionSortButtons.add(chkSortRegions);
      pnlRegionSortButtons.add(rdbSortRegionsCoordinates);
      pnlRegionSortButtons.add(rdbSortRegionsIslands);

      chkDisplayIslands = new JCheckBox(Resources.get("emapoverviewpanel.prefs.showislands"));

      chkRegionTreeBuilder_withBuildings = new JCheckBox(Resources.get("emapoverviewpanel.prefs.treebuildings"));
      chkRegionTreeBuilder_withShips = new JCheckBox(Resources.get("emapoverviewpanel.prefs.treeships"));
      chkRegionTreeBuilder_withComments = new JCheckBox(Resources.get("emapoverviewpanel.prefs.treecomments"));

      chkRootHandles = new JCheckBox(Resources.get("emapoverviewpanel.prefs.roothandles"));

      JPanel pnlTreeStructure = new JPanel();
      pnlTreeStructure.setLayout(new GridBagLayout());

      GridBagConstraints c = new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0);
      pnlTreeStructure.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Resources.get("emapoverviewpanel.prefs.treeStructure")));

      JPanel elementsPanel = new JPanel();
      elementsPanel.setLayout(new BorderLayout(0, 0));
      elementsPanel.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Resources.get("emapoverviewpanel.prefs.treeStructure.available")));

      DefaultListModel elementsListModel = new DefaultListModel();
      elementsListModel.add(TreeHelper.FACTION, Resources.get("emapoverviewpanel.prefs.treeStructure.element.faction"));
      elementsListModel.add(TreeHelper.GROUP, Resources.get("emapoverviewpanel.prefs.treeStructure.element.group"));
      elementsListModel.add(TreeHelper.COMBAT_STATUS, Resources.get("emapoverviewpanel.prefs.treeStructure.element.combat"));
      elementsListModel.add(TreeHelper.HEALTH, Resources.get("emapoverviewpanel.prefs.treeStructure.element.health"));
      elementsListModel.add(TreeHelper.FACTION_DISGUISE_STATUS, Resources.get("emapoverviewpanel.prefs.treeStructure.element.factiondisguise"));
      elementsListModel.add(TreeHelper.TRUSTLEVEL, Resources.get("emapoverviewpanel.prefs.treeStructure.element.trustlevel"));
      elementsListModel.add(TreeHelper.TAGGABLE, Resources.get("emapoverviewpanel.prefs.treeStructure.element.taggable", new Object[] { TreeHelper.TAGGABLE_STRING }));
      elementsListModel.add(TreeHelper.TAGGABLE2, Resources.get("emapoverviewpanel.prefs.treeStructure.element.taggable", new Object[] { TreeHelper.TAGGABLE_STRING2 }));
      elementsListModel.add(TreeHelper.TAGGABLE3, Resources.get("emapoverviewpanel.prefs.treeStructure.element.taggable", new Object[] { TreeHelper.TAGGABLE_STRING3 }));
      elementsListModel.add(TreeHelper.TAGGABLE4, Resources.get("emapoverviewpanel.prefs.treeStructure.element.taggable", new Object[] { TreeHelper.TAGGABLE_STRING4 }));
      elementsListModel.add(TreeHelper.TAGGABLE5, Resources.get("emapoverviewpanel.prefs.treeStructure.element.taggable", new Object[] { TreeHelper.TAGGABLE_STRING5 }));

      elementsList = new JList(elementsListModel);

      JScrollPane pane = new JScrollPane(elementsList);
      elementsPanel.add(pane, BorderLayout.CENTER);

      JPanel usePanel = new JPanel();
      usePanel.setLayout(new GridBagLayout());
      usePanel.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Resources.get("emapoverviewpanel.prefs.treeStructure.use")));

      useList = new JList();
      useList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      pane = new JScrollPane(useList);
      c.gridheight = 4;
      usePanel.add(pane, c);

      c.gridheight = 1;
      c.gridx = 1;
      c.weightx = 0;
      usePanel.add(new JPanel(), c);

      c.gridy++;
      c.weighty = 0;

      JButton up = new JButton(Resources.get("emapoverviewpanel.prefs.treeStructure.up"));
      up.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          int pos = useList.getSelectedIndex();
          DefaultListModel model = (DefaultListModel) useList.getModel();

          if (pos == 0) {
            return;
          }

          Object o = model.elementAt(pos);
          model.remove(pos);
          model.insertElementAt(o, pos - 1);
          useList.setSelectedIndex(pos - 1);
        }
      });
      usePanel.add(up, c);

      c.gridy++;

      JButton down = new JButton(Resources.get("emapoverviewpanel.prefs.treeStructure.down"));
      down.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          int pos = useList.getSelectedIndex();
          DefaultListModel model = (DefaultListModel) useList.getModel();

          if (pos == (model.getSize() - 1)) {
            return;
          }

          Object o = model.elementAt(pos);
          model.remove(pos);
          model.insertElementAt(o, pos + 1);
          useList.setSelectedIndex(pos + 1);
        }
      });
      usePanel.add(down, c);

      c.gridy++;
      c.weighty = 1.0;
      usePanel.add(new JPanel(), c);

      c.gridx = 0;
      c.gridy = 0;
      c.gridheight = 4;
      c.weightx = 0.5;
      c.weighty = 0.5;
      pnlTreeStructure.add(elementsPanel, c);

      c.gridx = 2;
      pnlTreeStructure.add(usePanel, c);

      c.gridx = 1;
      c.gridheight = 1;
      c.weightx = 0;
      c.weighty = 1.0;
      pnlTreeStructure.add(new JPanel(), c);

      c.gridy++;
      c.weighty = 0;

      JButton right = new JButton("  -->  ");
      right.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          Object selection[] = elementsList.getSelectedValues();
          DefaultListModel model = (DefaultListModel) useList.getModel();

          for (int i = 0; i < selection.length; i++) {
            if (!model.contains(selection[i])) {
              model.add(model.getSize(), selection[i]);
            }
          }
        }
      });
      pnlTreeStructure.add(right, c);

      c.gridy++;

      JButton left = new JButton("  <--  ");
      left.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          DefaultListModel model = (DefaultListModel) useList.getModel();
          Object selection[] = useList.getSelectedValues();

          for (int i = 0; i < selection.length; i++) {
            model.removeElement(selection[i]);
          }
        }
      });
      pnlTreeStructure.add(left, c);

      c.gridy++;
      c.weighty = 1;
      pnlTreeStructure.add(new JPanel(), c);

      // Unit sorting
      rdbSortUnitsUnsorted = new JRadioButton(Resources.get("emapoverviewpanel.prefs.reportorder"));
      rdbSortUnitsUnsorted.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          skillSort.setEnabled(false);
          useBestSkill.setEnabled(false);
          useTopmostSkill.setEnabled(false);
        }
      });

      rdbSortUnitsSkills = new JRadioButton(Resources.get("emapoverviewpanel.prefs.sortbyskills"));
      rdbSortUnitsSkills.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          skillSort.setEnabled(true);
          useBestSkill.setEnabled(true);
          useTopmostSkill.setEnabled(true);
        }
      });

      useBestSkill = new JRadioButton(Resources.get("emapoverviewpanel.prefs.usebestskill"));
      useTopmostSkill = new JRadioButton(Resources.get("emapoverviewpanel.prefs.usetopmostskill"));

      ButtonGroup whichSkillToUse = new ButtonGroup();
      whichSkillToUse.add(useBestSkill);
      whichSkillToUse.add(useTopmostSkill);
      rdbSortUnitsNames = new JRadioButton(Resources.get("emapoverviewpanel.prefs.sortbynames"));
      rdbSortUnitsNames.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          skillSort.setEnabled(false);
          useBestSkill.setEnabled(false);
          useTopmostSkill.setEnabled(false);
        }
      });

      ButtonGroup unitsSortButtons = new ButtonGroup();
      unitsSortButtons.add(rdbSortUnitsUnsorted);
      unitsSortButtons.add(rdbSortUnitsSkills);
      unitsSortButtons.add(rdbSortUnitsNames);

      JPanel pnlUnitSort = new JPanel();
      pnlUnitSort.setLayout(new GridBagLayout());
      c = new GridBagConstraints(0, 0, 1, 1, 0.1, 0.1, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0);
      pnlUnitSort.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Resources.get("emapoverviewpanel.prefs.unitsorting")));
      pnlUnitSort.add(rdbSortUnitsUnsorted, c);
      c.gridy = 1;
      pnlUnitSort.add(rdbSortUnitsSkills, c);
      c.gridy = 2;
      c.insets = new Insets(0, 30, 0, 0);
      pnlUnitSort.add(useBestSkill, c);
      c.gridy = 3;
      pnlUnitSort.add(useTopmostSkill, c);
      c.gridy = 4;
      c.ipadx = 0;
      c.insets = new Insets(0, 0, 0, 0);
      pnlUnitSort.add(rdbSortUnitsNames, c);

      this.setLayout(new GridBagLayout());
      c.anchor = GridBagConstraints.CENTER;
      c.gridx = 0;
      c.gridy = 0;
      c.gridwidth = 1;
      c.gridheight = 1;
      c.fill = GridBagConstraints.HORIZONTAL;
      c.weightx = 1.0;
      c.weighty = 0.0;
      this.add(pnlRegionSortButtons, c);

      c.anchor = GridBagConstraints.WEST;
      c.gridy++;
      c.insets.left = 10;
      c.fill = GridBagConstraints.NONE;
      c.weightx = 0.0;
      this.add(chkDisplayIslands, c);

      c.anchor = GridBagConstraints.WEST;
      c.gridy++;
      c.insets.left = 10;
      c.fill = GridBagConstraints.NONE;
      c.weightx = 0.0;
      this.add(chkSortShipUnderUnitParent, c);

      c.anchor = GridBagConstraints.WEST;
      c.gridy++;
      c.insets.left = 10;
      c.fill = GridBagConstraints.NONE;
      c.weightx = 0.0;
      this.add(chkRegionTreeBuilder_withBuildings, c);

      c.anchor = GridBagConstraints.WEST;
      c.gridy++;
      c.insets.left = 10;
      c.fill = GridBagConstraints.NONE;
      c.weightx = 0.0;
      this.add(chkRegionTreeBuilder_withShips, c);

      c.anchor = GridBagConstraints.WEST;
      c.gridy++;
      c.insets.left = 10;
      c.fill = GridBagConstraints.NONE;
      c.weightx = 0.0;
      this.add(chkRegionTreeBuilder_withComments, c);

      c.anchor = GridBagConstraints.WEST;
      c.gridy++;
      c.insets.left = 10;
      c.fill = GridBagConstraints.NONE;
      c.weightx = 0.0;
      this.add(chkRootHandles, c);

      c.insets.left = 0;
      c.anchor = GridBagConstraints.CENTER;
      c.gridy++;
      c.fill = GridBagConstraints.HORIZONTAL;
      c.weightx = 1.0;
      this.add(pnlTreeStructure, c);

      c.gridy++;
      this.add(pnlUnitSort, c);

      JPanel help = new JPanel(new GridLayout(1, 2));
      help.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), Resources.get("emapoverviewpanel.prefs.expand.title")));
      help.add(ePanel = new ExpandPanel()); // , BorderLayout.WEST);
      help.add(cPanel = new CollapsePanel()); // , BorderLayout.EAST);
      c.gridy++;
      this.add(help, c);

      subAdapter = new ArrayList<SkillPreferences>(1);
      subAdapter.add(skillSort = new SkillPreferences(parent.dispatcher,parent.dispatcher.getMagellanContext().getImageFactory()));
    }

    
    
    
    /*
     * (non-Javadoc)
     * 
     * @see com.eressea.swing.preferences.PreferencesAdapter#initPreferences()
     */
    public void initPreferences() {
      chkSortRegions.setSelected(PropertiesHelper.getboolean(settings, "EMapOverviewPanel.sortRegions", true));
      chkSortShipUnderUnitParent.setSelected(PropertiesHelper.getboolean(settings, "EMapOverviewPanel.sortShipUnderUnitParent", true));

      chkRegionTreeBuilder_withBuildings.setSelected(PropertiesHelper.getboolean(settings, "EMapOverviewPanel.treeBuilderWithBuildings", true));
      chkRegionTreeBuilder_withShips.setSelected(PropertiesHelper.getboolean(settings, "EMapOverviewPanel.treeBuilderWithShips", true));
      chkRegionTreeBuilder_withComments.setSelected(PropertiesHelper.getboolean(settings, "EMapOverviewPanel.treeBuilderWithComments", true));

      chkRootHandles.setSelected(PropertiesHelper.getboolean(settings, "EMapOverviewPanel.treeRootHandles", true));

      rdbSortRegionsCoordinates.setSelected(settings.getProperty("EMapOverviewPanel.sortRegionsCriteria", "coordinates").equals("coordinates"));
      rdbSortRegionsIslands.setSelected(settings.getProperty("EMapOverviewPanel.sortRegionsCriteria", "coordinates").equals("islands"));
      chkDisplayIslands.setSelected(PropertiesHelper.getboolean(settings, "EMapOverviewPanel.displayIslands", true));

      String criteria = settings.getProperty("EMapOverviewPanel.treeStructure", " " + TreeHelper.FACTION + " " + TreeHelper.GROUP);

      DefaultListModel model2 = new DefaultListModel();

      for (StringTokenizer tokenizer = new StringTokenizer(criteria); tokenizer.hasMoreTokens();) {
        String s = tokenizer.nextToken();
        try {
          int i = Integer.parseInt(s);

          try {
            model2.add(model2.size(), elementsList.getModel().getElementAt(i));
          } catch (ArrayIndexOutOfBoundsException e) {
            model2.add(model2.size(), "unknown");
          }
        } catch (NumberFormatException e) {
        }
      }
      useList.setModel(model2);

      rdbSortUnitsUnsorted.setSelected(settings.getProperty("EMapOverviewPanel.sortUnitsCriteria", "skills").equals("unsorted"));
      rdbSortUnitsSkills.setSelected(settings.getProperty("EMapOverviewPanel.sortUnitsCriteria", "skills").equals("skills"));
      useBestSkill.setSelected(PropertiesHelper.getboolean(settings, "EMapOverviewPanel.useBestSkill", true));
      useTopmostSkill.setSelected(!useBestSkill.isSelected());

      rdbSortUnitsNames.setSelected(settings.getProperty("EMapOverviewPanel.sortUnitsCriteria", "skills").equals("names"));

      skillSort.initPreferences();
    }

    /**
     * DOCUMENT-ME
     */
    public void applyPreferences() {
      settings.setProperty("EMapOverviewPanel.sortRegions", String.valueOf(chkSortRegions.isSelected()));

      settings.setProperty("EMapOverviewPanel.sortShipUnderUnitParent", String.valueOf(chkSortShipUnderUnitParent.isSelected()));

      settings.setProperty("EMapOverviewPanel.treeBuilderWithBuildings", String.valueOf(chkRegionTreeBuilder_withBuildings.isSelected()));

      settings.setProperty("EMapOverviewPanel.treeBuilderWithShips", String.valueOf(chkRegionTreeBuilder_withShips.isSelected()));

      settings.setProperty("EMapOverviewPanel.treeBuilderWithComments", String.valueOf(chkRegionTreeBuilder_withComments.isSelected()));

      settings.setProperty("EMapOverviewPanel.treeRootHandles", String.valueOf(chkRootHandles.isSelected()));

      // workaround to support EMapOverviewPanel.filters
      int newFilter = TreeBuilder.UNITS;
      if (chkRegionTreeBuilder_withBuildings.isSelected())
        newFilter = newFilter | TreeBuilder.BUILDINGS;
      if (chkRegionTreeBuilder_withShips.isSelected())
        newFilter = newFilter | TreeBuilder.SHIPS;
      if (chkRegionTreeBuilder_withComments.isSelected())
        newFilter = newFilter | TreeBuilder.COMMENTS;

      settings.setProperty("EMapOverviewPanel.filters", String.valueOf(newFilter));

      if (rdbSortRegionsCoordinates.isSelected()) {
        settings.setProperty("EMapOverviewPanel.sortRegionsCriteria", "coordinates");
      } else if (rdbSortRegionsIslands.isSelected()) {
        settings.setProperty("EMapOverviewPanel.sortRegionsCriteria", "islands");
      }

      settings.setProperty("EMapOverviewPanel.displayIslands", String.valueOf(chkDisplayIslands.isSelected()));

      if (rdbSortUnitsUnsorted.isSelected()) {
        settings.setProperty("EMapOverviewPanel.sortUnitsCriteria", "unsorted");
      } else if (rdbSortUnitsSkills.isSelected()) {
        settings.setProperty("EMapOverviewPanel.sortUnitsCriteria", "skills");
      } else if (rdbSortUnitsNames.isSelected()) {
        settings.setProperty("EMapOverviewPanel.sortUnitsCriteria", "names");
      }

      settings.setProperty("EMapOverviewPanel.useBestSkill", String.valueOf(useBestSkill.isSelected()));

      DefaultListModel useListModel = (DefaultListModel) useList.getModel();
      StringBuffer definition = new StringBuffer("");

      DefaultListModel elementsListModel = (DefaultListModel) elementsList.getModel();
      for (int i = 0; i < useListModel.getSize(); i++) {
        String s = (String) useListModel.getElementAt(i);

        int pos = elementsListModel.indexOf(s);
        definition.append(pos).append(" ");
      }

      settings.setProperty("EMapOverviewPanel.treeStructure", definition.toString());

      ePanel.apply();
      cPanel.apply();

      // We have to assure, that SkillPreferences.applyPreferences is called
      // before we rebuild the tree, i.e. before we call gameDataChanged().
      skillSort.applyPreferences();

      overviewPanel.rebuildTree();
    }

    /**
     * DOCUMENT-ME
     * 
     * 
     */
    public Component getComponent() {
      return this;
    }

    /**
     * DOCUMENT-ME
     * 
     * 
     */
    public String getTitle() {
      return Resources.get("emapoverviewpanel.prefs.title");
    }

    /**
     * DOCUMENT-ME
     * 
     * 
     */
    public List getChildren() {
      return subAdapter;
    }

    protected class ExpandPanel extends JPanel implements ActionListener {
      protected JRadioButton radioButtons[];
      protected JCheckBox checkBox;
      protected JTextField trustlevel;

      /**
       * Creates a new ExpandPanel object.
       */
      public ExpandPanel() {
        super(new GridBagLayout());

        GridBagConstraints con = new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0);
        radioButtons = new JRadioButton[3];

        ButtonGroup group = new ButtonGroup();

        boolean expanded = (expandMode & EXPAND_FLAG) != 0;

        radioButtons[0] = new JRadioButton(Resources.get("emapoverviewpanel.prefs.expand.none"), !expanded);
        group.add(radioButtons[0]);
        this.add(radioButtons[0], con);

        con.gridy++;
        con.fill = GridBagConstraints.HORIZONTAL;
        this.add(new JSeparator(JSeparator.HORIZONTAL), con);
        con.fill = GridBagConstraints.NONE;

        radioButtons[1] = new JRadioButton(Resources.get("emapoverviewpanel.prefs.expand.faction"), (expanded && ((expandMode >> 2) == 0)));
        group.add(radioButtons[1]);
        con.gridy++;
        this.add(radioButtons[1], con);

        radioButtons[2] = new JRadioButton(Resources.get("emapoverviewpanel.prefs.expand.full"), (expanded && ((expandMode >> 2) == 3)));
        group.add(radioButtons[2]);
        con.gridy++;
        this.add(radioButtons[2], con);

        trustlevel = new JTextField(String.valueOf(expandTrustlevel), 3);
        trustlevel.setEnabled(radioButtons[2].isSelected());

        JPanel help = new JPanel();
        help.add(Box.createRigidArea(new Dimension(20, 5)));

        String s = Resources.get("emapoverviewpanel.prefs.expand.trustlevel");
        int index = s.indexOf("#T");

        if (index == 0) {
          help.add(trustlevel);
          help.add(new JLabel(s.substring(2)));
        } else if ((index == -1) || (index == (s.length() - 2))) {
          if (index != -1) {
            s = s.substring(0, index);
          }

          help.add(new JLabel(s));
          help.add(trustlevel);
        } else {
          help.add(new JLabel(s.substring(0, index)));
          help.add(trustlevel);
          help.add(new JLabel(s.substring(index + 2)));
        }

        con.gridy++;
        this.add(help, con);

        con.gridy++;
        checkBox = new JCheckBox(Resources.get("emapoverviewpanel.prefs.expand.ifinside"), (expandMode & EXPAND_IFINSIDE_FLAG) != 0);
        checkBox.setEnabled(expanded);
        this.add(checkBox, con);

        registerListener();
      }

      protected void registerListener() {
        for (int i = 0; i < radioButtons.length; i++) {
          radioButtons[i].addActionListener(this);
        }
      }

      /**
       * DOCUMENT-ME
       */
      public void apply() {
        if (radioButtons[0].isSelected()) {
          expandMode = expandMode & (0xFFFFFFFF ^ EXPAND_FLAG);
        } else {
          expandMode = expandMode | EXPAND_FLAG;
        }

        if (checkBox.isSelected()) {
          expandMode = expandMode | EXPAND_IFINSIDE_FLAG;
        } else {
          expandMode = expandMode & (0xFFFFFFFF ^ EXPAND_IFINSIDE_FLAG);
        }

        int i = expandMode >> 2;

        if (radioButtons[1].isSelected()) {
          i = 0;
        } else if (radioButtons[2].isSelected()) {
          i = 3;
        }

        expandMode = (expandMode & (EXPAND_FLAG | EXPAND_IFINSIDE_FLAG)) | (i << 2);

        try {
          expandTrustlevel = Integer.parseInt(trustlevel.getText());
        } catch (NumberFormatException nfe) {
        }

        saveExpandProperties();
      }

      /**
       * DOCUMENT-ME
       * 
       * @param actionEvent
       *          DOCUMENT-ME
       */
      public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
        checkBox.setEnabled(actionEvent.getSource() != radioButtons[0]);
        trustlevel.setEnabled(actionEvent.getSource() == radioButtons[2]);
      }
    }

    protected class CollapsePanel extends JPanel implements ActionListener {
      protected JRadioButton radioButtons[];
      protected JCheckBox checkBox;

      /**
       * Creates a new CollapsePanel object.
       */
      public CollapsePanel() {
        super(new GridBagLayout());

        this.setBorder(new LeftBorder());

        GridBagConstraints con = new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 3, 0, 0), 0, 0);
        radioButtons = new JRadioButton[3];

        ButtonGroup group = new ButtonGroup();

        boolean collapse = (collapseMode & COLLAPSE_FLAG) != 0;

        radioButtons[0] = new JRadioButton(Resources.get("emapoverviewpanel.prefs.collapse.none"), !collapse);
        group.add(radioButtons[0]);
        this.add(radioButtons[0], con);

        con.gridy++;
        con.fill = GridBagConstraints.HORIZONTAL;
        con.insets.left = 0;
        this.add(new JSeparator(JSeparator.HORIZONTAL), con);
        con.insets.left = 3;
        con.fill = GridBagConstraints.NONE;

        radioButtons[1] = new JRadioButton(Resources.get("emapoverviewpanel.prefs.collapse.faction"), (collapse && ((collapseMode >> 2) == 0)));
        group.add(radioButtons[1]);
        con.gridy++;
        this.add(radioButtons[1], con);

        radioButtons[2] = new JRadioButton(Resources.get("emapoverviewpanel.prefs.collapse.full"), (collapse && ((collapseMode >> 2) == 3)));
        group.add(radioButtons[2]);
        con.gridy++;
        this.add(radioButtons[2], con);

        con.gridy++;
        checkBox = new JCheckBox(Resources.get("emapoverviewpanel.prefs.collapse.onlyautoexpanded"), (collapseMode & COLLAPSE_ONLY_EXPANDED) != 0);
        this.add(checkBox, con);

        // to make it equally high to ePanel
        con.gridy++;
        this.add(Box.createVerticalStrut(checkBox.getPreferredSize().height + 5), con);

        /*
         * con.gridx = 0; con.gridheight = con.gridy + 1; con.gridy = 0;
         * con.fill = GridBagConstraints.VERTICAL; JComponent c = new
         * JSeparator(JSeparator.VERTICAL); c.setMaximumSize(new Dimension(3,
         * 1000)); this.add(c, con);
         */
        registerListener();
      }

      protected void registerListener() {
        for (int i = 0; i < radioButtons.length; i++) {
          radioButtons[i].addActionListener(this);
        }
      }

      /**
       * DOCUMENT-ME
       * 
       * @param actionEvent
       *          DOCUMENT-ME
       */
      public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
        checkBox.setEnabled(actionEvent.getSource() != radioButtons[0]);
      }

      /**
       * DOCUMENT-ME
       */
      public void apply() {
        if (radioButtons[0].isSelected()) {
          collapseMode &= (0xFFFFFFFF ^ COLLAPSE_FLAG);
        } else {
          collapseMode |= COLLAPSE_FLAG;
        }

        if (checkBox.isSelected()) {
          collapseMode |= COLLAPSE_ONLY_EXPANDED;
        } else {
          collapseMode &= (0xFFFFFFFF ^ COLLAPSE_ONLY_EXPANDED);
        }

        int i = collapseMode >> 2;

        if (radioButtons[1].isSelected()) {
          i = 0;
        } else if (radioButtons[2].isSelected()) {
          i = 3;
        }

        collapseMode = (collapseMode & (COLLAPSE_FLAG | COLLAPSE_ONLY_EXPANDED)) | (i << 2);

        saveCollapseProperty();
      }

      protected class LeftBorder extends AbstractBorder {
        protected JSeparator sep;

        /**
         * Creates a new LeftBorder object.
         */
        public LeftBorder() {
          sep = new JSeparator(JSeparator.VERTICAL);
        }

        /**
         * DOCUMENT-ME
         * 
         * @param c
         *          DOCUMENT-ME
         * 
         */
        public Insets getBorderInsets(Component c) {
          return getBorderInsets(c, null);
        }

        /**
         * DOCUMENT-ME
         * 
         * @param c
         *          DOCUMENT-ME
         * @param in
         *          DOCUMENT-ME
         * 
         */
        public Insets getBorderInsets(Component c, Insets in) {
          if (in == null) {
            in = new Insets(0, 0, 0, 0);
          }

          in.top = 0;
          in.bottom = 0;
          in.right = 0;
          in.left = sep.getPreferredSize().width;

          return in;
        }

        /**
         * DOCUMENT-ME
         * 
         * @param c
         *          DOCUMENT-ME
         * @param g
         *          DOCUMENT-ME
         * @param x
         *          DOCUMENT-ME
         * @param y
         *          DOCUMENT-ME
         * @param width
         *          DOCUMENT-ME
         * @param height
         *          DOCUMENT-ME
         */
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
          sep.setBounds(x, y, width, height);
          SwingUtilities.paintComponent(g, sep, new JPanel(), x, y, width, height);
        }
      }
    }
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
    Collection buf[] = { new LinkedList(), new LinkedList() };

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

        if (!unitRelations.containsKey(u.getID())) {
          if (relations.size() == 0) {
            return;
          }

          unitRelations.put(u.getID(), new LinkedList());
        }

        List<UnitRelation> oldRelations = unitRelations.get(u.getID());
        Collection<UnitRelation> buffer = buf[updateRelationPartners ? 0 : 1];

        if (!relations.equals(oldRelations)) {
          buffer.clear();
          buffer.addAll(oldRelations);

          Iterator<UnitRelation> it = relations.iterator();

          while (it.hasNext()) {
            UnitRelation o = it.next();

            if (oldRelations.contains(o)) {
              buffer.remove(o);
            } else {
              oldRelations.add(o);

              if (updateRelationPartners) {
                update(((TransferRelation) o).target, false);
              }
            }
          }

          if (buffer.size() > 0) {
            it = buffer.iterator();

            while (it.hasNext()) {
              Object o = it.next();
              oldRelations.remove(o);

              if (updateRelationPartners) {
                update(((TransferRelation) o).target, false);
              }
            }
          }
        }
      }
    }
  }

  class TreeBuilder {
    /** Units are interesting. */
    public static final int UNITS = 1;

    /** Buildings are interesting. */
    public static final int BUILDINGS = 2;

    /** Ships are interesting. */
    public static final int SHIPS = 4;

    /** Comments are interesting. */
    public static final int COMMENTS = 8;

    /** Islands should be displayed. */
    public static final int CREATE_ISLANDS = 16384;

    /** the mode controls which elements are displayed */
    private int displayMode = UNITS | BUILDINGS | SHIPS | COMMENTS;

    // TODO hides fields form EmapOverviewPanel! */
    private Map<ID,TreeNode> regionNodes;
    private Map<ID,TreeNode> unitNodes;
    private Map<ID,TreeNode> buildingNodes;
    private Map<ID,TreeNode> shipNodes;
    private Map<ID, Alliance> activeAlliances;
    private Comparator unitComparator;
    private int treeStructure[];
    private boolean sortShipUnderUnitParent = true;

    /**
     * Sets the display mode, which controls what elements to display.
     * 
     * @param mode
     */
    public void setDisplayMode(int mode) {
      this.displayMode = mode;
    }

    /**
     * Controls if ships nodes should be sorted under their parents node.
     * 
     * @param b
     */
    public void setSortShipUnderUnitParent(boolean b) {
      sortShipUnderUnitParent = b;
    }

    /**
     * Return the display mode, which controls what elements to display.
     * 
     * @return The current display mode.
     */
    public int getDisplayMode() {
      return displayMode;
    }

    /**
     * DOCUMENT-ME
     * 
     * @param regions
     *          DOCUMENT-ME
     */
    public void setRegionNodes(Map<ID,TreeNode> regions) {
      regionNodes = regions;
    }

    /**
     * DOCUMENT-ME
     * 
     * @param units
     *          DOCUMENT-ME
     */
    public void setUnitNodes(Map<ID,TreeNode> units) {
      unitNodes = units;
    }

    /**
     * DOCUMENT-ME
     * 
     * @param buildings
     *          DOCUMENT-ME
     */
    public void setBuildingNodes(Map<ID,TreeNode> buildings) {
      buildingNodes = buildings;
    }

    /**
     * DOCUMENT-ME
     * 
     * @param ships
     *          DOCUMENT-ME
     */
    public void setShipNodes(Map<ID,TreeNode> ships) {
      shipNodes = ships;
    }

    /**
     * DOCUMENT-ME
     * 
     * @param alliances
     *          DOCUMENT-ME
     */
    public void setActiveAlliances(Map alliances) {
      activeAlliances = alliances;
    }

    /**
     * DOCUMENT-ME
     * 
     * @param compare
     *          DOCUMENT-ME
     */
    public void setUnitComparator(Comparator compare) {
      unitComparator = compare;
    }

    /**
     * DOCUMENT-ME
     * 
     * @param structure
     *          DOCUMENT-ME
     */
    public void setTreeStructure(int structure[]) {
      treeStructure = structure;
    }

    /**
     * DOCUMENT-ME
     */
    public void buildTree(DefaultMutableTreeNode rootNode, GameData data) {
      if (data == null) {
        return;
      }

      buildTree(rootNode, sortRegions(data.regions().values()), data.units().values(), regionNodes, unitNodes, buildingNodes, shipNodes, unitComparator, activeAlliances, treeStructure, data);
    }

    /**
     * DOCUMENT-ME
     */
    public void buildTree(DefaultMutableTreeNode rootNode, Collection regionCollection, Collection<Unit> units, Map<ID,TreeNode> regionNodes, Map<ID,TreeNode> unitNodes, Map<ID,TreeNode> buildingNodes, Map<ID,TreeNode> shipNodes, Comparator unitSorting, Map<ID, Alliance> activeAlliances, int treeStructure[], GameData data) {
      boolean unitInteresting = (getDisplayMode() & UNITS) != 0;
      boolean buildingInteresting = (getDisplayMode() & BUILDINGS) != 0;
      boolean shipInteresting = (getDisplayMode() & SHIPS) != 0;
      boolean commentInteresting = (getDisplayMode() & COMMENTS) != 0;
      boolean createIslandNodes = (getDisplayMode() & CREATE_ISLANDS) != 0;

      DefaultMutableTreeNode islandNode = null;
      DefaultMutableTreeNode regionNode = null;
      Island curIsland = null;

      TreeHelper treehelper = new TreeHelper();
      for (Iterator regions = regionCollection.iterator(); regions.hasNext();) {
        Region r = (Region) regions.next();

        if (!((unitInteresting && !r.units().isEmpty()) || (buildingInteresting && !r.buildings().isEmpty()) || (shipInteresting && !r.ships().isEmpty()) || (commentInteresting && !((r.getComments() == null) || (r.getComments().size() == 0))))) {
          continue;
        }

        // add region node to tree an node map
        regionNode = (DefaultMutableTreeNode) treehelper.createRegionNode(r, nodeWrapperFactory, activeAlliances, unitNodes, buildingNodes, shipNodes, unitSorting, treeStructure, data, sortShipUnderUnitParent);

        if (regionNode == null) {
          continue;
        }

        // update island node
        if (createIslandNodes) {
          if (r.getIsland() != null) {
            if (!r.getIsland().equals(curIsland)) {
              curIsland = r.getIsland();
              islandNode = new DefaultMutableTreeNode(nodeWrapperFactory.createIslandNodeWrapper(curIsland));
              rootNode.add(islandNode);
            }
          } else {
            islandNode = null;
          }
        }

        if (islandNode != null) {
          islandNode.add(regionNode);
        } else {
          rootNode.add(regionNode);
        }

        regionNodes.put(r.getID(), regionNode);
      }

      // add the homeless
      DefaultMutableTreeNode n = new DefaultMutableTreeNode(Resources.get("emapoverviewpanel.node.regionlessunits"));

      for (Iterator<Unit> iter = units.iterator(); iter.hasNext();) {
        Unit un = iter.next();

        if (un.getRegion() == null) {
          n.add(new DefaultMutableTreeNode(nodeWrapperFactory.createUnitNodeWrapper(un)));
        }
      }

      if (n.getChildCount() > 0) {
        rootNode.add(n);
      }
    }
  }

  protected TreeBuilder createTreeBuilder() {
    TreeBuilder treeBuilder = new TreeBuilder();
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
   * @deprecated I think this is needless. (stm)
   */
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
  public JMenu getMenu() {
    // We will create it every time the method is invoked since that should be
    // only once...
    return new OverviewMenu(Resources.get("emapoverviewpanel.menu.caption"), Resources.get("emapoverviewpanel.menu.mnemonic").charAt(0));
  }

  /**
   * DOCUMENT-ME
   * 
   * 
   */
  public String getSuperMenu() {
    return "tree";
  }

  /**
   * DOCUMENT-ME
   * 
   * 
   */
  public String getSuperMenuTitle() {
    return Resources.get("emapoverviewpanel.menu.supertitle");
  }

  public Collection getSelectedObjects() {
    return this.contextManager.getSelection();
  }

  // pavkovic 2003.01.28: this is a Map of the default Translations mapped to
  // this class
  // it is called by reflection (we could force the implementation of an
  // interface,
  // this way it is more flexible.)
  // Pls use this mechanism, so the translation files can be created
  // automagically
  // by inspecting all classes.
  private static Map<String, String> defaultTranslations;

  /**
   * DOCUMENT-ME
   * 
   * 
   */
  public static synchronized Map<String, String> getDefaultTranslations() {
    if (defaultTranslations == null) {
      defaultTranslations = new Hashtable<String, String>();
      defaultTranslations.put("prefs.title", "Regions");
      defaultTranslations.put("prefs.sortregions", "Sort regions");
      defaultTranslations.put("prefs.sortShipUnderUnitParent", "Sort ships under unit parent node");
      defaultTranslations.put("prefs.sortbycoordinates", "By coordinate");
      defaultTranslations.put("prefs.sortbyislands", "By island");
      defaultTranslations.put("prefs.regionsorting", "Region sorting");
      defaultTranslations.put("prefs.showislands", "Show islands");
      defaultTranslations.put("prefs.reportorder", "Use report order");
      defaultTranslations.put("prefs.sortbyskills", "By skill");
      defaultTranslations.put("prefs.sortbynames", "By name");
      defaultTranslations.put("prefs.unitsorting", "Unit sorting");

      defaultTranslations.put("prefs.treeStructure", "Hierarchical tree structure");
      defaultTranslations.put("prefs.treeStructure.available", "Available structure elements");
      defaultTranslations.put("prefs.treeStructure.element.faction", "Faction");
      defaultTranslations.put("prefs.treeStructure.element.group", "Group");
      defaultTranslations.put("prefs.treeStructure.element.combat", "Combat status");
      defaultTranslations.put("prefs.treeStructure.element.health", "Health status");
      defaultTranslations.put("prefs.treeStructure.element.taggable", "Tag \"{0}\"");
      defaultTranslations.put("prefs.treeStructure.element.factiondisguise", "Faction disguised");
      defaultTranslations.put("prefs.treeStructure.element.trustlevel", "Trustlevel");
      defaultTranslations.put("prefs.treeStructure.use", "Use");
      defaultTranslations.put("prefs.treeStructure.up", "Up");
      defaultTranslations.put("prefs.treeStructure.down", "Down");

      defaultTranslations.put("prefs.showskillicons", "Show skill icons");
      defaultTranslations.put("prefs.showcontainericons", "Show building and ship icons");
      defaultTranslations.put("prefs.uniticons", "Unit icons");
      defaultTranslations.put("prefs.icontextcolor", "Color of icon text");
      defaultTranslations.put("prefs.showskilllevel", "Show skill level");
      defaultTranslations.put("prefs.skillorder", "Order of skills");
      defaultTranslations.put("prefs.upbutton.caption", "Up");
      defaultTranslations.put("prefs.downbutton.caption", "Down");
      defaultTranslations.put("prefs.refreshlistbutton.caption", "Alphabetical");
      defaultTranslations.put("prefs.usebestskill", "According to best skill");
      defaultTranslations.put("prefs.usetopmostskill", "According topmost skill in list");

      defaultTranslations.put("icontextdialog.title", "Tree icons labels");
      defaultTranslations.put("icontextdialog.txt.info.text", "On the left, you can select the position of the icon labels (the center box indicates the position of the icon itself).");
      defaultTranslations.put("icontextdialog.btn.ok.caption", "OK");
      defaultTranslations.put("icontextdialog.btn.cancel.caption", "Cancel");
      defaultTranslations.put("icontextdialog.lbl.font.caption", "Font");
      defaultTranslations.put("icontextdialog.chk.bold.caption", "Bold");
      defaultTranslations.put("icontextdialog.chk.italic.caption", "Italic");
      defaultTranslations.put("icontextdialog.btn.color.caption", "Font color");
      defaultTranslations.put("icontextdialog.colorchooser.title", "Font color of icon labels");
      defaultTranslations.put("wrapperfactory.title", "Region Tree Entries");
      defaultTranslations.put("prefs.collapse.onlyautoexpanded", "Collapse only auto-expanded elements");
      defaultTranslations.put("prefs.collapse.full", "Full");
      defaultTranslations.put("prefs.collapse.faction", "First level");
      defaultTranslations.put("prefs.collapse.none", "No auto-collapse");
      defaultTranslations.put("prefs.expand.ifinside", "Only if own units in region");
      defaultTranslations.put("prefs.expand.trustlevel", "Only with trustlevel #T or higher");
      defaultTranslations.put("prefs.expand.full", "Full");
      defaultTranslations.put("prefs.expand.faction", "First level");
      defaultTranslations.put("prefs.expand.title", "Tree-Expansion");
      defaultTranslations.put("prefs.expand.none", "No expansion");
      defaultTranslations.put("shortcut.description.5", "Backward through history");
      defaultTranslations.put("shortcut.description.3", "Backward through history");
      defaultTranslations.put("shortcut.description.4", "Forward through history");
      defaultTranslations.put("shortcut.description.2", "Forward through history");
      defaultTranslations.put("shortcut.description.1", "Request Focus");
      defaultTranslations.put("shortcut.description.0", "Request Focus");
      defaultTranslations.put("shortcut.title", "Overview");

      defaultTranslations.put("menu.caption", "Region Overview");
      defaultTranslations.put("menu.mnemonic", "R");
      defaultTranslations.put("menu.filter", "Filters");
      defaultTranslations.put("menu.filter.0", "Units");
      defaultTranslations.put("menu.filter.1", "Buildings");
      defaultTranslations.put("menu.filter.2", "Ships");
      defaultTranslations.put("menu.filter.3", "Comments");
      defaultTranslations.put("menu.supertitle", "Tree");

      defaultTranslations.put("prefs.treebuildings", "Additional include regions with information of buildings");
      defaultTranslations.put("prefs.treeships", "Additional include regions with information of ships");
      defaultTranslations.put("prefs.treecomments", "Additional include regions with known comments");
      defaultTranslations.put("prefs.roothandles", "Show handles of topmost nodes");

    }

    return defaultTranslations;
  }
}
