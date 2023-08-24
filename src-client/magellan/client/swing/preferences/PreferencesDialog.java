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

package magellan.client.swing.preferences;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import magellan.client.swing.CenterLayout;
import magellan.client.swing.InternationalizedDialog;
import magellan.client.utils.SwingUtils;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

/**
 * A dialog allowing the user to set preferences or similar data.
 * <p>
 * This class has a kind of plug-in mechanism, that allows displaying a dynamic set of panels as tab
 * panes without changing the implementation.
 * </p>
 * <p>
 * In order to use this feature, these are the preconditions:
 * </p>
 * <ul>
 * <li>Make all preferences of your actual UI component (a subclass of DataPanel or something
 * similar, e.g. the EMapDetailsPanel) publicly accessible (usually via get/set methods).</li>
 * <li>Write a container that makes these preferences editable (nice checkboxes and so on).</li>
 * <li>That container must, upon creation, reflect the current preferences of your UI
 * component.</li>
 * <li>That container also has to provide a means of applying the changes made by the user to your
 * UI component.</li>
 * <li>Create a class implementing the PreferencesAdapter interface, which holds a reference to the
 * container. It is responsible for passing the your container to this PreferencesDialog and making
 * it 'apply' the changes made by the user to your UI component.</li>
 * <li>Finally, plug in your container into this PreferencesDialog object by calling the addTab()
 * method.</li>
 * </ul>
 * <p>
 * Note that such a preferences container has to resize with its parent component, i.e. the
 * preferences dialog
 * </p>
 */
public class PreferencesDialog extends InternationalizedDialog {
  /**
   * list of all PreferencesAdapters connected (also the children of ExtendedPreferencesAdapters)
   */
  private Collection<PreferencesAdapter> adapters = null;
  private DialogTree dialogtree;
  private Properties settings = null;

  /**
   * Creates a modal or non-modal dialog with the specified title and the specified owner frame.
   * 
   * @param owner the frame from which the dialog is displayed.
   * @param modal true for a modal dialog, false for one that allows others windows to be active at
   *          the same time.
   * @param settings the String to display in the dialog's title bar.
   */
  public PreferencesDialog(Frame owner, boolean modal, Properties settings) {
    super(owner, modal);
    this.settings = settings;
    setTitle(Resources.get("preferences.preferencesdialog.window.title"));

    adapters = new LinkedList<PreferencesAdapter>();

    setContentPane(getMainPane());

    int width = Integer.parseInt(settings.getProperty("PreferencesDialog.width", "800"));
    int height = Integer.parseInt(settings.getProperty("PreferencesDialog.height", "700"));
    setSize(width, height);

    SwingUtils.setLocation(this, settings, "PreferencesDialog.x", "PreferencesDialog.y");
  }

  /**
   * Creates a new PreferencesDialog object.
   */
  public PreferencesDialog(Frame owner, boolean modal, Properties settings,
      Collection<PreferencesFactory> prefAdapters) {
    this(owner, modal, settings);

    for (PreferencesFactory factory : prefAdapters) {
      try {
        addTab(factory.createPreferencesAdapter());
      } catch (Throwable e) {
        Logger.getInstance(this.getClass()).error("preferences dialog error", e);
      }
    }
  }

  /**
   * DOCUMENT-ME
   */
  public void updateLaF() {
    if (dialogtree != null) {
      dialogtree.updateUI();
    }
  }

  /**
   * Do last initialization and show.
   * 
   * @see java.awt.Dialog#setVisible(boolean)
   */
  @Override
  public void setVisible(boolean isVisible) {
    if (isVisible) {
      try {
        initPreferences();
      } catch (Throwable e) {
        Logger.getInstance(this.getClass()).error("preferences dialog error", e);
      }
      dialogtree.showFirst();
    }
    super.setVisible(isVisible);
  }

  /**
   * Register a new container with this dialog. It will be put into its own tab and notified via the
   * apply() method, if the user confirmed the presumably modified preferences.
   */
  public void addTab(PreferencesAdapter adapter) {
    // tabs.add(adapter.getTitle(), adapter.getComponent());
    dialogtree.addAdapter(adapter);
  }

  private void applyPreferences() {
    for (PreferencesAdapter pA : adapters) {
      pA.applyPreferences();
    }
  }

  private void initPreferences() {
    initPreferences(adapters);
  }

  private void initPreferences(Collection<PreferencesAdapter> children) {
    for (PreferencesAdapter adapter : children) {
      initPreferences(adapter);
      if (adapter instanceof ExtendedPreferencesAdapter) {
        initPreferences(((ExtendedPreferencesAdapter) adapter).getChildren());
      }
    }
  }

  private void initPreferences(PreferencesAdapter adapter) {
    adapter.initPreferences();
  }

  private Container getMainPane() {
    JButton okButton = new JButton(Resources.get("preferences.preferencesdialog.btn.ok.caption"));
    okButton.setMnemonic(Resources.get("preferences.preferencesdialog.btn.ok.caption.mnemonic")
        .charAt(0));
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        applyPreferences();
        quit();
      }
    });

    JButton cancelButton =
        new JButton(Resources.get("preferences.preferencesdialog.btn.cancel.caption"));
    cancelButton.setMnemonic(Resources.get(
        "preferences.preferencesdialog.btn.cancel.caption.mnemonic").charAt(0));
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // reloadPreferences();
        quit();
      }
    });

    JButton applyButton =
        new JButton(Resources.get("preferences.preferencesdialog.btn.apply.caption"));
    applyButton.setMnemonic(Resources.get(
        "preferences.preferencesdialog.btn.apply.caption.mnemonic").charAt(0));
    applyButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        applyPreferences();
      }
    });

    JPanel buttonPanel = new JPanel(new FlowLayout());
    buttonPanel.setBorder(new EmptyBorder(5, 0, 0, 0));
    buttonPanel.add(okButton);
    buttonPanel.add(cancelButton);

    dialogtree = new DialogTree();

    JPanel main = new JPanel(new BorderLayout());
    main.setBorder(new EmptyBorder(5, 5, 5, 5));
    main.add(dialogtree, BorderLayout.CENTER);
    main.add(buttonPanel, BorderLayout.SOUTH);

    return main;
  }

  /**
   * @see magellan.client.swing.InternationalizedDialog#quit()
   */
  @Override
  public void quit() {
    settings.setProperty("PreferencesDialog.width", getWidth() + "");
    settings.setProperty("PreferencesDialog.height", getHeight() + "");
    settings.setProperty("PreferencesDialog.x", getX() + "");
    settings.setProperty("PreferencesDialog.y", getY() + "");
    settings = null;
    adapters.clear();
    adapters = null;

    super.quit();
  }

  protected class DialogTree extends JPanel implements TreeSelectionListener {
    protected JTree tree;
    protected DefaultMutableTreeNode root;
    protected DefaultTreeModel model;
    protected TreePath firstAdapter;
    protected JPanel content;
    protected CardLayout cardLayout;

    /**
     * Creates a new DialogTree object.
     */
    public DialogTree() {
      super(new BorderLayout(10, 0));

      root = new DefaultMutableTreeNode();
      tree = new JTree(model = new DefaultTreeModel(root));
      tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
      tree.setEditable(false);
      tree.setRootVisible(false);
      tree.addTreeSelectionListener(this);

      // tree.setMinimumSize(new Dimension(100,100));
      // tree.setPreferredSize(new Dimension(200, 300));
      // tree.setSize(200, 300);
      JScrollPane treePane = new JScrollPane(tree);

      // maybe some code to center the tree and make it smaller
      JPanel help = new JPanel(CenterLayout.SPAN_BOTH_LAYOUT);

      // help.add(new JTextfield(Resources.get("preferences.preferencesdialog.tree.title")));
      help.add(treePane);

      // this.add(help, BorderLayout.WEST);
      this.add(help, BorderLayout.WEST);

      content = new JPanel(cardLayout = new CardLayout(0, 0));

      // this.add(content, BorderLayout.CENTER);
      this.add(content, BorderLayout.CENTER);
    }

    /**
     * DOCUMENT-ME
     */
    public void addAdapter(PreferencesAdapter a) {
      DefaultMutableTreeNode node = addAdapterImpl(a);
      model.insertNodeInto(node, root, root.getChildCount());
      tree.setModel(null);
      tree.setModel(model);

      if (firstAdapter == null) {
        firstAdapter = new TreePath(model.getPathToRoot(node));
      }

      /*
       * Dimension dim = tree.getMaximumSize(); dim.width += 5; treeContainer.setPreferredSize(dim);
       */
    }

    protected DefaultMutableTreeNode addAdapterImpl(PreferencesAdapter pa) {
      // add adapter to adapter list
      adapters.add(pa);

      PreferencesInfo pref = new PreferencesInfo(pa);
      DefaultMutableTreeNode node = new DefaultMutableTreeNode(pref);

      Component c = pa.getComponent();
      Dimension dim = c.getPreferredSize();
      dim.width += 5;
      dim.height += 5;

      JScrollPane pane = new JScrollPane(c);
      pane.setBorder(null);
      pane.setPreferredSize(dim);

      JPanel help = new JPanel(CenterLayout.SPAN_X_LAYOUT);
      help.add(pane);

      content.add(pane, pa.getTitle());

      if (pa instanceof ExtendedPreferencesAdapter) {
        for (PreferencesAdapter preferencesAdapter : ((ExtendedPreferencesAdapter) pa)
            .getChildren()) {
          DefaultMutableTreeNode subNode = addAdapterImpl(preferencesAdapter);

          if (subNode != null) {
            node.add(subNode);
          }
        }
      }

      return node;
    }

    /**
     * DOCUMENT-ME
     */
    public void showFirst() {
      // expand all nodes
      if (root.getChildCount() > 0) {
        for (int i = 0; i < root.getChildCount(); i++) {
          openNode(root.getChildAt(i));
        }
      }

      String s = settings.getProperty("PreferencesDialog.DialogTree.SelectedRow", "-1");

      if (!s.equals("-1")) {
        tree.setSelectionRow(Integer.parseInt(s));
      } else {
        tree.setSelectionPath(firstAdapter);
      }
    }

    protected void openNode(TreeNode node) {
      tree.expandPath(new TreePath(model.getPathToRoot(node)));

      if (node.getChildCount() > 0) {
        for (int i = 0; i < node.getChildCount(); i++) {
          openNode(node.getChildAt(i));
        }
      }
    }

    /**
     * Show adapter corresponding to selected node.
     * 
     * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
     */
    public void valueChanged(javax.swing.event.TreeSelectionEvent treeSelectionEvent) {
      // store the selected row (we know, that it can be only one)
      int selection[] = tree.getSelectionRows();

      if (selection.length > 0) {
        settings.setProperty("PreferencesDialog.DialogTree.SelectedRow", String
            .valueOf(selection[0]));
      }

      PreferencesInfo pref =
          (PreferencesInfo) ((DefaultMutableTreeNode) tree.getSelectionPath()
              .getLastPathComponent()).getUserObject();

      if (pref != null) {
        cardLayout.show(content, pref.toString());
      }
    }

    /**
     * a wrapper class to display a PreferencesAdapter node with its title.
     */
    protected class PreferencesInfo {
      PreferencesAdapter adapter;

      /**
       * Creates a new PreferencesInfo object.
       */
      public PreferencesInfo(PreferencesAdapter pa) {
        adapter = pa;
      }

      /**
       * @see java.lang.Object#toString()
       */
      @Override
      public String toString() {
        return adapter.getTitle();
      }
    }
  }
}
