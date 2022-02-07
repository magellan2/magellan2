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

package magellan.client.swing;

import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.function.Predicate;

import javax.swing.JMenu;
import javax.swing.JScrollPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import magellan.client.event.EventDispatcher;
import magellan.client.event.SelectionEvent;
import magellan.client.event.SelectionListener;
import magellan.client.swing.tree.CellRenderer;
import magellan.client.swing.tree.CopyTree;
import magellan.client.swing.tree.LineWrapCellRenderer;
import magellan.client.swing.tree.MixedTreeCellRenderer;
import magellan.client.swing.tree.NodeWrapperFactory;
import magellan.client.swing.tree.RegionNodeWrapper;
import magellan.client.swing.tree.UnitNodeWrapper;
import magellan.library.Battle;
import magellan.library.Building;
import magellan.library.CoordinateID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.ID;
import magellan.library.IntegerID;
import magellan.library.Island;
import magellan.library.Message;
import magellan.library.Region;
import magellan.library.Ship;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.UnitID;
import magellan.library.event.GameDataEvent;
import magellan.library.utils.MagellanImages;
import magellan.library.utils.Resources;
import magellan.library.utils.UnionCollection;
import magellan.library.utils.comparator.IDComparator;
import magellan.library.utils.comparator.MessageTypeComparator;

/**
 * A class for displaying Eressea messages for regions or factions.
 * 
 * @author ...
 * @version 1.0
 */
public class MessagePanel extends InternationalizedDataPanel implements SelectionListener,
    MenuProvider {
  // tree elements
  private CopyTree tree = null;
  private DefaultTreeModel treeModel = null;
  private DefaultMutableTreeNode rootNode = null;
  protected NodeWrapperFactory nodeFactory;
  protected LineWrapCellRenderer lineRenderer;

  /**
   * Creates a new <tt>MessagePanel</tt> object.
   * 
   * @param d the central event dispatcher.
   * @param gd the game data this message panel initiates with.
   * @param p settings
   */
  public MessagePanel(EventDispatcher d, GameData gd, Properties p) {
    super(d, gd, p);

    nodeFactory =
        new NodeWrapperFactory(p, "MessagePanel.Nodes", Resources
            .get("messagepanel.nodeFactory.title"));

    // create dummies to have a valid pref adapter
    // nodeFactory.createUnitNodeWrapper(null, null);
    // nodeFactory.createRegionNodeWrapper(null);

    d.addSelectionListener(this);

    initTree();
  }

  /**
   * Handles changes on game data.
   */
  @Override
  public void gameDataChanged(GameDataEvent e) {
    setGameData(e.getGameData());
    rootNode.removeAllChildren();
    treeModel.reload();
  }

  /**
   * Handles the selection of a new object to display messages for.
   */
  public void selectionChanged(SelectionEvent se) {
    // TODO (stm) only single selection?
    Object activeObject = se.getActiveObject();

    if (se.getSource() == this)
      return;

    // clear tree nodes
    rootNode.removeAllChildren();

    if (activeObject instanceof Island) {
      show((Island) activeObject, rootNode);
    } else if (activeObject instanceof Region) {
      show((Region) activeObject, rootNode);
    } else if (activeObject instanceof Faction) {
      show((Faction) activeObject, rootNode);
    } else if (activeObject instanceof Building || activeObject instanceof Ship) {
      show((UnitContainer) activeObject, rootNode);
    } else if (activeObject instanceof Unit) {
      show((Unit) activeObject, rootNode);
    }

    treeModel.reload();

    Enumeration<?> enumeration = rootNode.children();

    while (enumeration.hasMoreElements()) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) enumeration.nextElement();

      if (node.getChildCount() > 0) {
        DefaultMutableTreeNode firstChild = (DefaultMutableTreeNode) node.getFirstChild();
        tree.makeVisible(new TreePath(firstChild.getPath()));
      }
    }
  }

  /**
   * Shows the messages for the specified island.
   */
  public void show(Island i, DefaultMutableTreeNode parent) {
    if (getGameData().getFactions() == null)
      return;

    showBattles(parent, (b) -> i.getRegion(b.getID()) != null);

    showFactionMessages(parent, getIslandPredicate(i));
  }

  private Predicate<Message> getIslandPredicate(Island island) {
    return getMessagePredicate((c) -> island.getRegion(c) != null);
  }

  private Predicate<Message> getRegionPredicate(Region r) {
    return getMessagePredicate((c) -> r.getID().equals(c));
  }

  private Predicate<Message> getIdPredicate(ID id) {
    int idValue;
    if (id instanceof IntegerID) {
      idValue = ((IntegerID) id).intValue();
      return (msg) -> {
        for (String key : msg.getAttributeKeys()) {
          try {
            int i = Integer.parseInt(msg.getAttribute(key));

            // it would be cleaner to compare UnitID
            // objects here but that's too expensive
            if (idValue == i)
              return true;
          } catch (NumberFormatException e) {
            // ignore msg
          }
        }
        return false;
      };
    } else
      return (msg) -> false;
  }

  private Predicate<Message> getMessagePredicate(Predicate<CoordinateID> cFilter) {
    return (msg) -> {
      for (String key : msg.getAttributeKeys()) {
        String attribute = msg.getAttribute(key);
        CoordinateID c = CoordinateID.parse(attribute, ",");

        if (c == null) {
          c = CoordinateID.parse(attribute, " ");
        }

        if ((c != null) && (cFilter.test(c)))
          return true;
      }
      return false;
    };
  }

  protected void showFactionMessages(DefaultMutableTreeNode parent, Predicate<Message> filter) {
    List<Message> allMessages = new LinkedList<Message>();

    for (Faction f : getGameData().getFactions()) {
      if (f.getMessages() != null) {
        for (Message msg : f.getMessages()) {
          if (filter.test(msg)) {
            allMessages.add(msg);
          }
        }
      }
    }

    showFactionMessages(parent, allMessages);
  }

  protected void showFactionMessages(DefaultMutableTreeNode parent, Collection<Message> messages) {
    if (messages != null && !messages.isEmpty()) {
      DefaultMutableTreeNode node =
          new DefaultMutableTreeNode(Resources.get("messagepanel.node.factionmessages"));
      parent.add(node);
      addCategorizedMessages(messages, node);
    }
  }

  /**
   * Shows the messages for the specified region.
   */
  private void show(Region r, DefaultMutableTreeNode parent) {
    if (r == null)
      return;

    showBattles(parent, (b) -> b.getID().equals(r.getCoordinate()));

    showMessages(parent, r.getMessages(), Resources.get("messagepanel.node.messages"));

    showStrings(parent, r.getEffects(), Resources.get("messagepanel.node.effects"));

    showMessages(parent, r.getPlayerMessages(), Resources.get("messagepanel.node.dispatches"));

    showMessages(parent, r.getEvents(), Resources.get("messagepanel.node.events"));

    showMessages(parent, r.getSurroundings(), Resources.get("messagepanel.node.surroundings"));

    showMessages(parent, UnionCollection.union(r.getTravelThru(), r.getTravelThruShips()),
        Resources.get("messagepanel.node.travelthru"));

    showFactionMessages(parent, getRegionPredicate(r));
  }

  private void showStrings(DefaultMutableTreeNode parent, Collection<String> effects, String name) {
    if (effects != null && effects.size() > 0) {
      DefaultMutableTreeNode node = new DefaultMutableTreeNode(name);
      parent.add(node);

      for (String str : effects) {
        show(str, node);
      }
    }
  }

  private void showMessages(DefaultMutableTreeNode parent, Collection<Message> messages, String name) {
    showMessages(parent, messages, name, null);
  }

  /**
   * Add all messages that pass a filter. If <code>name == null</code>, add directly under parent, otherwise create a
   * sub node with the given name.
   */
  private void showMessages(DefaultMutableTreeNode parent, Collection<Message> messages, String name,
      Predicate<Message> filter) {
    if (messages != null && messages.size() > 0) {

      DefaultMutableTreeNode node;
      if (name != null) {
        node = new DefaultMutableTreeNode(name);
        parent.add(node);
      } else {
        node = parent;
      }

      for (Message msg : messages) {
        if (filter == null || filter.test(msg)) {
          show(msg, node);
        }
      }
    }
  }

  /**
   * Shows the messages for the specified Faction.
   */
  private void show(Faction f, DefaultMutableTreeNode parent) {
    if (f == null)
      return;

    showStrings(parent, f.getErrors(), Resources.get("messagepanel.node.errors"));

    showBattles(parent, f, (b) -> true);

    showFactionMessages(parent, f.getMessages());
  }

  /**
   * Show a unit's messages.
   */
  private void show(Unit u, DefaultMutableTreeNode parent) {
    if (u == null)
      return;

    showStrings(parent, u.getEffects(), Resources.get("messagepanel.node.effects"));

    showMessages(parent, u.getUnitMessages(), Resources.get("messagepanel.node.unitdispatches"));

    Faction f = u.getFaction();

    if (f != null) {
      showMessages(parent, f.getMessages(), null, getIdPredicate(u.getID()));
    }
  }

  /**
   * Show the messages related to the specified Building or Ship.
   */
  private void show(UnitContainer uc, DefaultMutableTreeNode parent) {
    if (uc == null)
      return;

    showStrings(parent, uc.getEffects(), Resources.get("messagepanel.node.effects"));

    // this is sort of slow
    showFactionMessages(parent, getIdPredicate(uc.getID()));
  }

  private void showBattles(DefaultMutableTreeNode parent, Predicate<Battle> battleFilter) {
    Collection<Battle> battles = new HashSet<Battle>();
    for (Faction f : getGameData().getFactions()) {
      getBattles(f, battleFilter, battles);
    }
    showBattles(parent, battles);
  }

  private void showBattles(DefaultMutableTreeNode parent, Faction f, Predicate<Battle> battleFilter) {
    Collection<Battle> battles = new HashSet<Battle>();
    getBattles(f, battleFilter, battles);
    showBattles(parent, battles);
  }

  private void getBattles(Faction f, Predicate<Battle> battleFilter, Collection<Battle> battles) {
    if (f.getBattles() != null) {
      for (Battle b : f.getBattles()) {
        if (battleFilter.test(b)) {
          battles.add(b);
        }
      }
    }
  }

  private void showBattles(DefaultMutableTreeNode parent, Collection<Battle> battles) {
    if (!battles.isEmpty()) {
      DefaultMutableTreeNode node = new DefaultMutableTreeNode(Resources.get("messagepanel.node.battles"));
      parent.add(node);

      for (Battle b : battles) {
        show(b, node);
      }
    }
  }

  /**
   * Show the messages of a battle.
   */
  private void show(Battle b, DefaultMutableTreeNode parent) {
    if (b == null)
      return;

    DefaultMutableTreeNode node = null;
    DefaultMutableTreeNode subNode = null;

    CoordinateID c = b.getID();
    Region r = getGameData().getRegion(c);

    if (r != null) {
      node = new DefaultMutableTreeNode(nodeFactory.createRegionNodeWrapper(r));
    } else {
      node = new DefaultMutableTreeNode(c.toString());
    }

    parent.add(node);

    Iterator<Message> msgs = b.messages().iterator();

    while (msgs.hasNext()) {
      Message msg = msgs.next();
      subNode = new DefaultMutableTreeNode(msg);
      node.add(subNode);
    }
  }

  /**
   * Display a <tt>Message</tt> object under a parent node.
   */
  private void show(Message m, DefaultMutableTreeNode parent) {
    DefaultMutableTreeNode node = null;
    DefaultMutableTreeNode subNode = null;

    node = new DefaultMutableTreeNode(m);
    parent.add(node);

    String value = m.getAttribute("unit");

    if (value != null) {
      try {
        int i = Integer.parseInt(value);
        Unit u = getGameData().getUnit(UnitID.createUnitID(i, getGameData().base));

        if (u != null) {
          subNode = new DefaultMutableTreeNode(nodeFactory.createUnitNodeWrapper(u));
          node.add(subNode);
        }
      } catch (NumberFormatException e) {
        // ignore message
      }
    }

    for (String key : m.getAttributeKeys()) {
      String val = m.getAttribute(key);
      CoordinateID c = CoordinateID.parse(val, ",");

      if (c == null) {
        c = CoordinateID.parse(val, " ");
      }

      if (c != null) {
        Region r = getGameData().getRegion(c);

        if (r != null) {
          subNode = new DefaultMutableTreeNode(nodeFactory.createRegionNodeWrapper(r));
          node.add(subNode);
        }
      }
    }

  }

  /**
   * Display a <tt>String</tt> object under a parent node.
   */
  private void show(String s, DefaultMutableTreeNode parent) {
    DefaultMutableTreeNode node = null;

    node = new DefaultMutableTreeNode(s);
    parent.add(node);
  }

  /**
   * 
   */
  private void handleValueChange() {
    DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

    if (node == null)
      return;

    Object o = node.getUserObject();

    if (o instanceof UnitNodeWrapper) {
      o = ((UnitNodeWrapper) o).getUnit();
    } else if (o instanceof RegionNodeWrapper) {
      o = ((RegionNodeWrapper) o).getRegion();
    }

    if (o instanceof Unit) {
      dispatcher.fire(SelectionEvent.create(this, (Unit) o));
    }
    if (o instanceof Region) {
      dispatcher.fire(SelectionEvent.create(this, (Region) o));
    }
  }

  /**
   *
   */
  private void initTree() {
    rootNode = new DefaultMutableTreeNode("Rootnode");
    treeModel = new DefaultTreeModel(rootNode);
    tree = new CopyTree(treeModel);
    tree.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
          handleValueChange();
        }
      }
    });

    tree.setRootVisible(false);
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    tree.setRowHeight(0);

    JScrollPane treeScrollPane = new JScrollPane(tree);

    // ClearLook suggests to remove this border
    treeScrollPane.setBorder(null);

    setLayout(new GridLayout(1, 0));
    add(treeScrollPane);

    ((DefaultTreeCellRenderer) tree.getCellRenderer()).setLeafIcon(MagellanImages.BULLETS_LEAF);
    ((DefaultTreeCellRenderer) tree.getCellRenderer()).setOpenIcon(MagellanImages.BULLETS_OPEN);
    ((DefaultTreeCellRenderer) tree.getCellRenderer()).setClosedIcon(MagellanImages.BULLETS_CLOSED);

    lineRenderer = new LineWrapCellRenderer((DefaultTreeCellRenderer) tree.getCellRenderer());
    lineRenderer.setLineWrap(settings.getProperty("MessagePanel.LineWrap", "true").equals("true"));
    lineRenderer.registerTree(tree);

    CellRenderer cr = new CellRenderer(getMagellanContext());

    MixedTreeCellRenderer mixed = new MixedTreeCellRenderer(lineRenderer);
    mixed.putRenderer(UnitNodeWrapper.class, cr);
    mixed.putRenderer(RegionNodeWrapper.class, cr);
    tree.setCellRenderer(mixed);
  }

  /**
   * 
   */
  public boolean isLineWrap() {
    return lineRenderer.isLineWrap();
  }

  /**
   * 
   */
  public void setLineWrap(boolean bool) {
    lineRenderer.setLineWrap(bool);
    settings.setProperty("MessagePanel.LineWrap", (bool ? "true" : "false"));
  }

  /**
   * 
   */
  public NodeWrapperFactory getNodeWrapperFactory() {
    return nodeFactory;
  }

  /**
   * Adds the given messages to the parent node, categorized by the section of the messagetypes and
   * then sorted by messagetypes.
   */
  private void addCategorizedMessages(Collection<Message> messages, DefaultMutableTreeNode parent) {
    Map<String, List<Message>> categories = new Hashtable<String, List<Message>>();
    // categorize messages
    for (Message message : messages) {
      String section = null;

      if (message.getMessageType() != null) {
        section = message.getMessageType().getSection();
      }

      if (section == null) {
        section = "others"; // Resources.get("messagepanel.node.others");
      }

      List<Message> l = categories.get(section);

      if (l == null) {
        l = new LinkedList<Message>();
        categories.put(section, l);
      }

      l.add(message);
    }

    // sort messages in the single categories
    // and add them as nodes
    Comparator<Message> comp = new MessageTypeComparator(IDComparator.DEFAULT);

    for (String category : categories.keySet()) {
      String s = null;

      try {
        s = Resources.get("messagepanel.section." + category);
      } catch (MissingResourceException e) {
        s = category;
      }

      DefaultMutableTreeNode n = new DefaultMutableTreeNode(s);
      parent.add(n);

      List<Message> l = categories.get(category);
      Collections.sort(l, comp);

      for (Message message : l) {
        show(message, n);
      }
    }
  }

  /**
   * 
   */
  public JMenu getMenu() {
    JMenu menu = new JMenu(Resources.get("messagepanel.menu.caption"));
    menu.setMnemonic(Resources.get("messagepanel.menu.mnemonic").charAt(0));
    menu.add(nodeFactory.getContextMenu());

    return menu;
  }

  /**
   * 
   */
  public String getSuperMenu() {
    return "tree";
  }

  /**
   * 
   */
  public String getSuperMenuTitle() {
    return Resources.get("messagepanel.menu.supertitle");
  }
}
