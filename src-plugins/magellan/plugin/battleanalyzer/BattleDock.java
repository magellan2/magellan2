// class magellan.plugin.groupeditor.GroupEditorDock
// created on 25.09.2008
//
// Copyright 2003-2008 by magellan project team
//
// Author : $Author: $
// $Id: $
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc.,
// 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
package magellan.plugin.battleanalyzer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import javax.swing.BoxLayout;
import javax.swing.JEditorPane;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import magellan.client.Client;
import magellan.client.desktop.MagellanDesktop;
import magellan.client.event.SelectionEvent;
import magellan.client.swing.tree.CopyTree;
import magellan.client.swing.tree.SimpleNodeWrapper;
import magellan.library.Battle;
import magellan.library.CoordinateID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Selectable;
import magellan.library.event.GameDataEvent;
import magellan.library.event.GameDataListener;
import magellan.library.utils.PropertiesHelper;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

/**
 * Shows battle details
 * 
 * @author stm
 */
public class BattleDock extends JPanel implements ActionListener, GameDataListener {
  private static Logger log = Logger.getInstance(BattleDock.class);
  /** DOCK Identifier */
  public static final String IDENTIFIER = "BattleAnalyzer";
  private Client client = null;
  private GameData world = null;
  private Properties properties;
  private Map<CoordinateID, BattleInfo> battles;
  @SuppressWarnings("unused")
  private JEditorPane textArea;
  private DefaultMutableTreeNode root;
  private DefaultTreeModel treeModel;
  private CopyTree tree;

  /**
   * Created a new instance of this class
   * 
   * @param _properties
   */
  public BattleDock(Client client, Properties _properties) {
    setClient(client);
    setProperties(_properties);
    init();

    client.getDispatcher().addGameDataListener(this);
  }

  /**
   * Initializes the GUI
   */
  protected void init() {
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    // textArea = new JEditorPane();
    // textArea.setContentType("text/html");
    // textArea.setEditable(false);
    // textArea.setText("----");
    // textArea.setCaretPosition(0);
    // // textArea.setPreferredSize(new Dimension(400, 400));
    //
    // JScrollPane scrollPane = new JScrollPane(textArea);
    // scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    // // scrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);
    // scrollPane.setPreferredSize(new Dimension(400, 400));
    // add(scrollPane);

    root = new DefaultMutableTreeNode(null);
    treeModel = new DefaultTreeModel(root);
    tree = new CopyTree(treeModel);
    tree.setRootVisible(false);

    final TreePopupMenu popup = new TreePopupMenu();

    MouseListener ml = new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        maybeShowPopup(e);
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        maybeShowPopup(e);
      }

      private void maybeShowPopup(MouseEvent e) {
        TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
        tree.setSelectionPath(selPath);
        if (selPath == null)
          return;
        else {
          tree.setSelectionPath(selPath);
          if (e.isPopupTrigger()) {
            popup.setContext(null);
            Object uo = getPathObject(selPath);
            popup.setContext(uo);
            popup.show(tree, e.getX(), e.getY());
          }
        }
      }
    };
    tree.addMouseListener(ml);

    tree.addKeyListener(new KeyAdapter() {

      @Override
      public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
        case KeyEvent.VK_ENTER:
          Object target = getPathObject(tree.getSelectionPath());
          if (target instanceof Selectable) {
            client.getDispatcher().fire(SelectionEvent.create(BattleDock.this, target, SelectionEvent.ST_DEFAULT));
          }
          e.consume();
          break;
        }
      }
    });

    JScrollPane treeScrollPane = new JScrollPane(tree);
    treeScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    // treeScrollPane.setPreferredSize(new Dimension(400, 400));
    add(treeScrollPane);
  }

  protected Object getPathObject(TreePath selPath) {
    Object c = selPath.getLastPathComponent();
    if (c instanceof DefaultMutableTreeNode) {
      Object o = ((DefaultMutableTreeNode) c).getUserObject();
      if (o instanceof SimpleNodeWrapper)
        return ((SimpleNodeWrapper) o).getObject();
    }
    return null;
  }

  public static class TreeUtils {
    /**
     * Expands/Collapse specified tree to a certain level.
     * 
     * @param jTree jtree to expand to a certain level
     * @param level the level of expansion
     */
    public static void expandOrCollapseToLevel(JTree jTree, TreePath treePath, int level,
        boolean expand) {
      expandOrCollapsePath(jTree, treePath, level, 0, expand);
    }

    public static void expandOrCollapsePath(JTree jTree, TreePath treePath, int level,
        int currentLevel, boolean expand) {
      if (expand && level <= currentLevel && level > 0)
        return;

      TreeNode treeNode = (TreeNode) treePath.getLastPathComponent();
      TreeModel tModel = jTree.getModel();
      if (tModel.getChildCount(treeNode) >= 0) {
        for (int i = 0; i < tModel.getChildCount(treeNode); i++) {
          TreeNode n = (TreeNode) tModel.getChild(treeNode, i);
          TreePath path = treePath.pathByAddingChild(n);
          expandOrCollapsePath(jTree, path, level, currentLevel + 1, expand);
        }
        if (!expand && currentLevel < level)
          return;
      }
      if (expand) {
        jTree.expandPath(treePath);
      } else {
        jTree.collapsePath(treePath);
      }
    }
  }

  /**
   * This method updates the list of factions inside the faction choose box.
   */
  protected void update() {

    // reset components
    // StringBuilder text = new StringBuilder();
    int heroFactor = PropertiesHelper.getInteger(getProperties(), "plugins.battledock.herofactor", 5);

    battles = new LinkedHashMap<CoordinateID, BattleInfo>();

    for (Faction f : world.getFactions()) {
      if (f.getBattles() != null) {
        for (Battle b : f.getBattles()) {
          BattleInfo info = battles.get(b.getID());
          if (info == null) {
            info = new BattleInfo(b.getID(), world);
            info.setHeroFactor(heroFactor);
          }
          info.parse(b, f.getLocale());

          battles.put(b.getID(), info);
        }
      }
    }
    // for (BattleInfo info : battles.values()) {
    // text.append(info.toHtml());
    // }

    // if (text.length() == 0) {
    // textArea.setText("<h2>" + Resources.get("plugin.battle.nobattles") + "</h2");
    // } else {
    // textArea.setText(text.toString());
    // }

    root.removeAllChildren();

    for (BattleInfo info : battles.values()) {
      info.toTree(root);
    }

    treeModel.reload();

    tree.revalidate();
    tree.repaint();
  }

  /**
   * Makes the dock visible on the desktop.
   * 
   * @see MagellanDesktop#restoreView(String)
   */
  public void restoreView() {
    client.getDesktop().restoreView(IDENTIFIER);
  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent event) {
    BattleDock.log.info("ActionEvent '" + event.getActionCommand() + "' retrieved");

  }

  /**
   * Returns the value of client.
   * 
   * @return Returns client.
   */
  public Client getClient() {
    return client;
  }

  /**
   * Sets the value of client.
   * 
   * @param client The value for client.
   */
  public void setClient(Client client) {
    this.client = client;
  }

  /**
   * Returns the value of world.
   * 
   * @return Returns world.
   */
  public GameData getWorld() {
    return world;
  }

  /**
   * Sets the value of world.
   * 
   * @param world The value for world.
   */
  public void setWorld(GameData world) {
    if (this.world != world) {
      this.world = world;
      update();
    }
    this.world = world;
  }

  /**
   * Returns the value of properties.
   * 
   * @return Returns properties.
   */
  public Properties getProperties() {
    return properties;
  }

  /**
   * Sets the value of properties.
   * 
   * @param properties The value for properties.
   */
  public void setProperties(Properties properties) {
    this.properties = properties;
  }

  /**
   * @see magellan.library.event.GameDataListener#gameDataChanged(magellan.library.event.GameDataEvent)
   */
  public void gameDataChanged(GameDataEvent e) {
    setWorld(e.getGameData());
  }

  public class TreePopupMenu extends JPopupMenu {

    private Object context;
    private JMenuItem expandMenu;
    private JMenuItem collapseMenu;
    private JMenuItem collapse1Menu;
    private JMenuItem titleMenu;

    public TreePopupMenu() {
      add(titleMenu = new JMenuItem("foo"));
      add(expandMenu = new JMenuItem(Resources.get("plugin.battle.popup.expand.title")));
      add(collapseMenu = new JMenuItem(Resources.get("plugin.battle.popup.collapse.title")));
      add(collapse1Menu = new JMenuItem(Resources.get("plugin.battle.popup.collapse1.title")));

      titleMenu.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          client.getDispatcher().fire(
              SelectionEvent.create(BattleDock.this, context, SelectionEvent.ST_DEFAULT));
        }
      });

      expandMenu.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          TreeUtils.expandOrCollapseToLevel(tree, tree.getSelectionPath(), Integer.MAX_VALUE, true);
        }
      });

      collapseMenu.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          tree.collapsePath(tree.getSelectionPath());
        }
      });

      collapse1Menu.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          TreeUtils.expandOrCollapseToLevel(tree, tree.getSelectionPath(), 1, false);
        }
      });

    }

    public void setContext(Object userObject) {
      if (userObject == null) {
        titleMenu.setText("---");
        titleMenu.setEnabled(false);
      } else {
        titleMenu.setText(Resources.get("plugin.battle.popup.goto.title", userObject.toString()));
        titleMenu.setEnabled(true);
      }
      context = userObject;
    }

  }

}
