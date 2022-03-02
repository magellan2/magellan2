/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.client.swing.tasks;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import magellan.client.desktop.DesktopEnvironment;
import magellan.client.desktop.MagellanDesktop;
import magellan.client.desktop.ShortcutListener;
import magellan.client.event.EventDispatcher;
import magellan.client.event.SelectionEvent;
import magellan.client.event.SelectionListener;
import magellan.client.event.UnitOrdersEvent;
import magellan.client.preferences.TaskTablePreferences;
import magellan.client.swing.InternationalizedDataPanel;
import magellan.client.swing.ProgressBarUI;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.client.swing.preferences.PreferencesFactory;
import magellan.client.swing.table.TableSorter;
import magellan.client.utils.TextAreaDialog;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.HasRegion;
import magellan.library.Region;
import magellan.library.Unit;
import magellan.library.event.GameDataEvent;
import magellan.library.event.UnitChangeEvent;
import magellan.library.event.UnitChangeListener;
import magellan.library.tasks.AttackInspector;
import magellan.library.tasks.BuildingInspector;
import magellan.library.tasks.GameDataInspector;
import magellan.library.tasks.Inspector;
import magellan.library.tasks.InspectorInterceptor;
import magellan.library.tasks.MaintenanceInspector;
import magellan.library.tasks.MessageInspector;
import magellan.library.tasks.MovementInspector;
import magellan.library.tasks.OrderSyntaxInspector;
import magellan.library.tasks.Problem;
import magellan.library.tasks.Problem.Severity;
import magellan.library.tasks.ProblemFactory;
import magellan.library.tasks.ProblemType;
import magellan.library.tasks.ShipInspector;
import magellan.library.tasks.SkillInspector;
import magellan.library.tasks.TeachInspector;
import magellan.library.tasks.ToDoInspector;
import magellan.library.tasks.TransferInspector;
import magellan.library.utils.PropertiesHelper;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

/**
 * A panel for showing reviews about unit, region and/or gamedata.
 */
public class TaskTablePanel extends InternationalizedDataPanel implements UnitChangeListener,
    SelectionListener, PreferencesFactory {
  private static final Logger log = Logger.getInstance(TaskTablePanel.class);

  /** @deprecated Use {@link MagellanDesktop#TASKS_IDENTIFIER} instead */
  @Deprecated
  public static final String IDENTIFIER = MagellanDesktop.TASKS_IDENTIFIER;

  private static final ProblemType INTERNAL = ProblemType.create("tasks.tasktablepanel",
      "internal");

  private JTable table;
  private TableSorter sorter;
  private TaskTableModel model;

  private JProgressBar progressbar;

  private JButton refreshButton;

  private JCheckBox selectionLabel;

  private JCheckBox activeRegionLabel;

  private JCheckBox globalLabel;

  private List<Inspector> inspectors;

  /**
   * Not easy to detect if this panel is shown / visible switched OFF: if refreshProblems is
   * detecting, that the JMenu entry in MagellanDeskTop.desktopMenu is not selected switched ON: if
   * paint(Graphics g) is detected and isShown=false in that case refreshProblems is called too.
   * adjusted by Events from DockingWindowFramework
   */
  private boolean shown = true;

  /** list of registered keyboard shortcuts */
  private List<KeyStroke> shortcuts;

  private Collection<Region> lastSelection = Collections.emptyList();

  private Region lastActiveRegion;

  private Vector<String> headerTitles;

  private magellan.client.swing.tasks.TaskTablePanel.UpdateEventDispatcher updateDispatcher;

  private Set<ProblemType> ignoredProblems;

  private Map<String, ProblemType> pMap;

  private boolean runInThisThread = false;

  /**
   * Creates a new TaskTablePanel object.
   *
   * @param d
   * @param initData
   * @param p
   */
  public TaskTablePanel(EventDispatcher d, GameData initData, Properties p) {
    super(d, initData, p);
    init();
  }

  private void init() {
    registerListeners();

    initGUI();
    initInspectors(getGameData());
    initUpdateThread();
    refreshProblems();
  }

  private void initGUI() {
    model = new TaskTableModel(getHeaderTitles());
    sorter = new TableSorter(model);
    table = new JTable(sorter);

    sorter.setTableHeader(table.getTableHeader()); // NEW

    // HACK: narrower columns for problem type and line numbers
    table.getColumn(table.getColumnName(TaskTableModel.PROBLEM_POS)).setPreferredWidth(200);
    table.getColumn(table.getColumnName(TaskTableModel.OBJECT_POS)).setPreferredWidth(50);
    table.getColumn(table.getColumnName(TaskTableModel.REGION_POS)).setPreferredWidth(50);
    table.getColumn(table.getColumnName(TaskTableModel.FACTION_POS)).setPreferredWidth(50);
    table.getColumn(table.getColumnName(TaskTableModel.LINE_POS)).setPreferredWidth(6);
    table.getColumn(table.getColumnName(TaskTableModel.TYPE_POS)).setPreferredWidth(6);

    // allow reordering of headers
    table.getTableHeader().setReorderingAllowed(true);

    // set row selection to single selection
    table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

    PopupListener popupListener = new PopupListener();
    // react on double clicks on a row
    table.addMouseListener(popupListener);
    table.addKeyListener(popupListener);
    table.setToolTipText(Resources.get("tasks.tooltip", false));

    // layout component
    setLayout(new BorderLayout());
    JScrollPane tablePane = new JScrollPane(table);
    tablePane.getViewport().addMouseListener(popupListener);
    tablePane.getViewport().addKeyListener(popupListener);
    this.add(tablePane, BorderLayout.CENTER);

    JPanel statusBar = new JPanel(new GridBagLayout());

    refreshButton = new JButton(Resources.get("tasks.contextmenu.refresh.title"));
    refreshButton.addActionListener(popupListener);
    refreshButton.setMargin(new Insets(1, 1, 1, 1));
    selectionLabel = new JCheckBox(Resources.get("tasks.selectionlabel.title"));
    selectionLabel.setToolTipText(Resources.get("tasks.selectionlabel.tooltip"));
    selectionLabel.setSelected(restrictToSelection());
    selectionLabel.addMouseListener(popupListener);
    selectionLabel.addItemListener(popupListener);
    activeRegionLabel = new JCheckBox(Resources.get("tasks.activeregionlabel.title"));
    activeRegionLabel.setToolTipText(Resources.get("tasks.activeregionlabel.tooltip"));
    activeRegionLabel.setSelected(restrictToActiveRegion());
    activeRegionLabel.addMouseListener(popupListener);
    activeRegionLabel.addItemListener(popupListener);
    globalLabel = new JCheckBox(Resources.get("tasks.globallabel.title"));
    globalLabel.setToolTipText(Resources.get("tasks.globallabel.tooltip"));
    globalLabel.setSelected(showGlobal());
    globalLabel.addMouseListener(popupListener);
    globalLabel.addItemListener(popupListener);

    progressbar = new JProgressBar();
    GridBagConstraints c = new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 3, 0);
    statusBar.add(selectionLabel, c);
    c.gridx++;
    statusBar.add(activeRegionLabel, c);
    c.gridx++;
    statusBar.add(globalLabel, c);
    c.gridx++;
    c.weightx = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    statusBar.add(progressbar, c);
    c.gridx++;
    c.weightx = 0;
    c.fill = GridBagConstraints.NONE;
    statusBar.add(refreshButton);

    this.add(statusBar, BorderLayout.PAGE_END);

    // initialize shortcuts
    shortcuts = new ArrayList<KeyStroke>(1);

    // 0: Focus
    shortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK));

    // register for shortcuts
    DesktopEnvironment.registerShortcutListener(new TTShortcutListener());
  }

  class PopupListener extends MouseAdapter implements KeyListener, ActionListener, ItemListener {
    JPopupMenu tableMenu;
    JPopupMenu busyMenu;
    JPopupMenu activeRegionMenu;
    JPopupMenu globalMenu;
    JPopupMenu selectionMenu;

    JMenuItem selectMenu;
    JMenuItem refreshMenu;
    JMenuItem acknowledgeMenu;
    JMenuItem unAcknowledgeMenu;
    JMenuItem removeTypeMenu;
    JMenuItem showMenu;

    PopupListener() {
      createPopupMenus();
    }

    /**
     * Create and show context menu.
     *
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseClicked(final MouseEvent e) {
      maybeSelect(e);
      maybeShowPopup(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
      maybeShowPopup(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
      maybeShowPopup(e);
    }

    private void maybeSelect(MouseEvent e) {
      if (updateDispatcher.isBusy())
        return;
      if (e.getSource() == table) {
        if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
          if (e.isControlDown()) {
            int row = table.getSelectedRow();
            if (row >= 0 && row < sorter.getRowCount()) {
              showObjectOnRow(row);
            }
          } else {
            JTable target = (JTable) e.getSource();

            int row = target.rowAtPoint(e.getPoint());
            int col = target.columnAtPoint(e.getPoint());
            if (TaskTablePanel.log.isDebugEnabled()) {
              TaskTablePanel.log.debug("TaskTablePanel: Double click on row " + row);
            }
            selectObjectOnRow(row, col);
          }
        }
      }
    }

    private void maybeShowPopup(MouseEvent mouseEvent) {
      Object source = mouseEvent.getSource();
      Component comp = mouseEvent.getComponent();
      int x = mouseEvent.getX();
      int y = mouseEvent.getY();
      int row = table.rowAtPoint(mouseEvent.getPoint());
      boolean popup = mouseEvent.isPopupTrigger();
      maybeShowPopup(source, comp, x, y, row, popup);
    }

    private void maybeShowPopup(KeyEvent keyEvent) {
      Object source = keyEvent.getSource();

      Component comp = keyEvent.getComponent();

      int x = table.getX();
      int y = table.getY();
      int row = table.getSelectedRow();
      if (row >= 0) {
        int col = table.getSelectedColumn();
        if (col < 0) {
          col = 0;
        }
        Rectangle rect = table.getCellRect(row, col, false);
        x = rect.x;
        y = rect.y;
      }
      boolean popup = keyEvent.getKeyCode() == KeyEvent.VK_CONTEXT_MENU;
      maybeShowPopup(source, comp, x, y, row, popup);
    }

    private void maybeShowPopup(Object source, Component comp, int x, int y, int row, boolean popup) {
      if (source == activeRegionLabel) {
        // activeRegionMenu.show(e.getComponent(), e.getX(), e.getY());
      } else if (source == selectionLabel) {
        // selectionMenu.show(e.getComponent(), e.getX(), e.getY());
      } else if (source == globalLabel) {
        // globalMenu.show(e.getComponent(), e.getX(), e.getY());
      } else if (source == refreshButton) {
        //
      } else {
        if (updateDispatcher.isBusy()) {
          busyMenu.show(comp, x, y);
          return;
        }
        // unselected rows if user clicks another row

        if (row >= 0) {
          boolean clickedOnSelection = false;
          for (int i : table.getSelectedRows()) {
            if (row == i) {
              clickedOnSelection = true;
              break;
            }
          }
          if (!clickedOnSelection) {
            table.clearSelection();
            table.addRowSelectionInterval(row, row);
          }
        }
        if (popup) {
          if (row >= 0 && table.getSelectedRowCount() > 0) {
            if (table.getSelectedRowCount() > 1) {
              acknowledgeMenu.setText(Resources.get("tasks.contextmenu.acknowledge.title"));
            } else {
              acknowledgeMenu.setText(Resources.get("tasks.contextmenu.acknowledge1.title"));
            }
            acknowledgeMenu.setEnabled(true);
            removeTypeMenu.setEnabled(true);
            selectMenu.setEnabled(true);
          } else {
            acknowledgeMenu.setText(Resources.get("tasks.contextmenu.acknowledge.title"));
            acknowledgeMenu.setEnabled(false);
            removeTypeMenu.setEnabled(false);
            selectMenu.setEnabled(false);
          }
          tableMenu.show(comp, x, y);
        }
      }
    }

    private void createPopupMenus() {
      tableMenu = new JPopupMenu();
      selectMenu = new JMenuItem(Resources.get("tasks.contextmenu.select.title"));
      showMenu = new JMenuItem(Resources.get("tasks.contextmenu.showfull.title"));
      refreshMenu = new JMenuItem(Resources.get("tasks.contextmenu.refresh.title"));
      acknowledgeMenu = new JMenuItem(Resources.get("tasks.contextmenu.acknowledge.title"));
      removeTypeMenu = new JMenuItem(Resources.get("tasks.contextmenu.removetype.title"));
      unAcknowledgeMenu = new JMenuItem(Resources.get("tasks.contextmenu.unacknowledge.title"));

      tableMenu.add(showMenu);
      tableMenu.add(selectMenu);
      tableMenu.add(new JSeparator());
      tableMenu.add(refreshMenu);
      tableMenu.add(acknowledgeMenu);
      tableMenu.add(unAcknowledgeMenu);
      tableMenu.add(new JSeparator());
      tableMenu.add(removeTypeMenu);

      selectMenu.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent menuEvent) {
          int row = table.getSelectedRow();
          int col = table.getSelectedColumn();
          if (row >= 0 && row < sorter.getRowCount() && col >= 0 && col < sorter.getColumnCount()) {
            selectObjectOnRow(row, col);
          }
        }
      });

      showMenu.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent menuEvent) {
          int row = table.getSelectedRow();
          if (row >= 0 && row < sorter.getRowCount()) {
            showObjectOnRow(row);
          }
        }
      });

      refreshMenu.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent menuEvent) {
          refreshProblems();
        }
      });

      acknowledgeMenu.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent menuEvent) {
          acknowledge(table.getSelectedRows(), false);
        }
      });

      unAcknowledgeMenu.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent menuEvent) {
          unAcknowledge();
        }
      });

      removeTypeMenu.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent menuEvent) {
          removeType(table.getSelectedRows());
        }
      });

      busyMenu = new JPopupMenu();
      JMenuItem busyMenuItem = new JMenuItem(Resources.get("tasks.contextmenu.busy.title"));
      busyMenuItem.setEnabled(false);
      busyMenu.add(busyMenuItem);

      JMenuItem resetMenuItem = new JMenuItem(Resources.get("tasks.contextmenu.reset.title"));
      resetMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent menuEvent) {
          updateDispatcher.reset();
          refreshProblems();
        }
      });
      busyMenu.add(resetMenuItem);

      activeRegionMenu = new JPopupMenu();
      JMenuItem arMenuItem = new JMenuItem(Resources.get(
          "tasks.contextmenu.restricttoactiveregion.title"));
      activeRegionMenu.add(arMenuItem);

      arMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          setRestrictToActiveRegion(!restrictToActiveRegion());
          refreshProblems();
        }
      });

      globalMenu = new JPopupMenu();
      JMenuItem glMenuItem = new JMenuItem(Resources.get("tasks.contextmenu.showglobal.title"));
      globalMenu.add(glMenuItem);

      glMenuItem.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          setShowGlobal(!showGlobal());
          refreshProblems();
        }
      });

      selectionMenu = new JPopupMenu();
      JMenuItem sMenuItem = new JMenuItem(Resources.get(
          "tasks.contextmenu.restricttoselection.title"));
      selectionMenu.add(sMenuItem);

      sMenuItem.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          setRestrictToSelection(!restrictToSelection());
          refreshProblems();
        }
      });

    }

    public void actionPerformed(ActionEvent e) {
      if (e.getSource() == refreshButton) {
        refreshProblems();
      }
    }

    public void itemStateChanged(ItemEvent e) {
      Object source = e.getItemSelectable();
      if (source == selectionLabel) {
        setRestrictToSelection(selectionLabel.isSelected());
        refreshProblems();
      }
      if (source == activeRegionLabel) {
        setRestrictToActiveRegion(activeRegionLabel.isSelected());
        refreshProblems();
      }
      if (source == globalLabel) {
        setShowGlobal(globalLabel.isSelected());
        refreshProblems();
      }
    }

    public void keyTyped(KeyEvent e) {
      //
    }

    public void keyPressed(KeyEvent e) {
      switch (e.getKeyCode()) {
      case KeyEvent.VK_CONTEXT_MENU:
        maybeShowPopup(e);
        break;
      case KeyEvent.VK_ENTER:
        if (updateDispatcher.isBusy())
          return;
        if (e.getSource() == table) {
          int row = table.getSelectedRow();
          if (row >= 0 && row < sorter.getRowCount()) {
            showObjectOnRow(row);
            e.consume();
          }
        }
        break;
      case KeyEvent.VK_SPACE:
        if (updateDispatcher.isBusy())
          return;
        int row = table.getSelectedRow();
        if (row >= 0 && row < sorter.getRowCount()) {
          int col = table.getSelectedColumn();
          if (col < 0) {
            col = 0;
          }
          selectObjectOnRow(row, col);
        }
        e.consume();
        break;
      case KeyEvent.VK_DELETE:
        if (updateDispatcher.isBusy())
          return;
        if (e.isControlDown()) {
          removeType(table.getSelectedRows());
        } else {
          acknowledge(table.getSelectedRows(), true);
        }
        e.consume();
        break;
      }
    }

    public void keyReleased(KeyEvent e) {
      //
    }
  }

  protected void unAcknowledge() {
    if ((JOptionPane.showConfirmDialog(this, Resources.get("tasks.confirmunacknowledge.message"), null,
        JOptionPane.YES_NO_OPTION)) != JOptionPane.YES_OPTION)
      // cancel
      return;

    Window w = SwingUtilities.getWindowAncestor(this);
    final ProgressBarUI progressUI = new ProgressBarUI((JFrame) (w instanceof JFrame ? w : null));
    progressUI.setTitle(Resources.get("tasks.progressbar.unack.title"));

    progressUI.setMaximum(getGameData().getRegions().size());

    progressUI.show();
    new Thread(new Runnable() {
      public void run() {
        try {
          int iProgress = 0;
          for (Inspector i : getInspectors()) {
            i.unSuppressGlobal();
            for (Faction f : getGameData().getFactions()) {
              i.unSuppress(f);
            }
            for (Region r : getGameData().getRegions()) {
              i.unSuppress(r);
              progressUI.setProgress(r.getName(), ++iProgress);
              for (Unit u : r.units()) {
                i.unSuppress(u);
              }
            }
          }
          dispatcher.fire(new GameDataEvent(this, getGameData()));
        } finally {
          try {
            progressUI.ready();
          } catch (Throwable e) {
            e.printStackTrace();
          }
        }
      }

    }).start();

  }

  protected void acknowledge(int selectedRows[], boolean confirm) {
    if (selectedRows.length == 0)
      return;

    if (confirm)
      if (JOptionPane.showConfirmDialog(this, Resources.get("tasks.confirmdelete.message", selectedRows.length), null,
          JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
        return;

    // sort problems by line
    List<Problem> problems = new ArrayList<Problem>();
    for (int row : selectedRows) {
      if (row < 0 || row >= sorter.getRowCount())
        throw new IndexOutOfBoundsException();
      Problem p = (Problem) sorter.getValueAt(row, TaskTableModel.PROBLEM_POS);
      problems.add(p);
    }

    int[] sortedRows = new int[selectedRows.length];
    int i = 0;
    for (int row : selectedRows) {
      sortedRows[i++] = sorter.modelIndex(row);
    }
    Arrays.sort(sortedRows);
    for (i = sortedRows.length - 1; i >= 0; --i) {
      model.removeRow(sortedRows[i]);
    }
    model.fireTableRowsDeleted(sortedRows[0], sortedRows[sortedRows.length - 1]);

    Collections.sort(problems, new Comparator<Problem>() {

      public int compare(Problem p1, Problem p2) {
        return p2.getLine() - p1.getLine();
      }
    });

    for (Problem p : problems) {
      Unit u = p.addSuppressComment();
      if (u != null) {
        dispatcher.fire(new UnitOrdersEvent(this, u));
      }
    }

  }

  private void removeType(int[] rows) {
    // sort problems by line
    for (int row : rows) {
      if (row < 0 || row >= sorter.getRowCount())
        throw new IndexOutOfBoundsException();
      Problem p = (Problem) sorter.getValueAt(row, TaskTableModel.PROBLEM_POS);
      int option;
      String desc = p.getType().getDescription();
      if (desc == null) {
        desc = p.getMessage();
      }
      if (desc != null && desc.length() > 120) {
        desc = desc.substring(0, 70) + " ... " + desc.substring(desc.length() - 40, desc.length()
            - 1);
      }
      if ((option = JOptionPane.showConfirmDialog(this,
          Resources.get("tasks.confirmremovetype.message", p.getType(), desc))) == JOptionPane.YES_OPTION) {
        addIgnoredProblem(p);
      }
      if (option == JOptionPane.CANCEL_OPTION) {
        break;
      }
    }
    refreshProblems();
  }

  /**
   * Fire selection event for object associated with row
   *
   * @param row row index of the underlying table
   * @param col column index of the underlying table
   */
  protected void selectObjectOnRow(int row, int col) {
    if (row < 0 || row >= sorter.getRowCount() || col < 0 || col >= sorter.getColumnCount())
      throw new IndexOutOfBoundsException();

    Object obj = sorter.getValueAt(row, TaskTableModel.OBJECT_POS);
    if (table.convertColumnIndexToModel(col) == TaskTableModel.REGION_POS) {
      obj = sorter.getValueAt(row, table.convertColumnIndexToModel(col));
    }
    if (obj != null) {
      dispatcher.fire(SelectionEvent.create(this, obj, SelectionEvent.ST_DEFAULT));
    }

  }

  protected void showObjectOnRow(int row) {
    if (row < 0 || row >= sorter.getRowCount())
      throw new IndexOutOfBoundsException();
    Problem p = (Problem) sorter.getValueAt(row, TaskTableModel.PROBLEM_POS);
    StringBuilder text = new StringBuilder();
    String desc = " (" + p.getType().getDescription() + ")";
    text.append(Resources.get("tasks.showfull.message", p.getMessage(), p.getObject(), p
        .getRegion(), p.getFaction(), p.getLine(), p.getType(), desc));
    final TextAreaDialog d = (new TextAreaDialog((JFrame) SwingUtilities.getWindowAncestor(this), Resources.get(
        "tasks.showfull.dialog.title"), text.toString()));
    d.setPreferredSize(new Dimension(400, 200));
    d.pack();
    d.setVisible(true);
  }

  private void initUpdateThread() {
    updateDispatcher = new UpdateEventDispatcher();
  }

  /**
   * Rebuild the complete table.
   */
  public void refreshProblems() {
    checkShown();
    if (!isShown()) {
      TaskTablePanel.log.debug("call to refreshProblems rejected  (panel not visible), reason: "
          + isShown());
      return;
    }

    updateDispatcher.clear();
    if (showGlobal()) {
      updateDispatcher.addGlobal();
    }
    if (restrictToActiveRegion()) {
      if (lastActiveRegion != null) {
        updateDispatcher.addRegion(lastActiveRegion);
      }
    } else if (restrictToSelection() && lastSelection != null && !lastSelection.isEmpty()) {
      updateDispatcher.addRegions(lastSelection);
    } else {
      updateDispatcher.addRegions(getGameData().getRegions());
    }
  }

  /**
   * Holds information about a necessary update on the task panel.
   *
   * @author stm
   * @version 1.0, Aug 23, 2008
   */
  protected static class UpdateEvent {

    private static final Object CLEAR = "EMPTY";
    private static final Object DATA = "DATA";
    private Region region;
    private Unit unit;
    private boolean add;

    /**
     * Creates a region update event.
     *
     * @param r This update concerns a region
     * @param add <code>true</code> if this region is added to the watched set, <code>false</code> if
     *          the corresponding problems should be removed.
     */
    public UpdateEvent(Region r, boolean add) {
      region = r;
      this.add = add;
    }

    /**
     * Creates a unit update event.
     *
     * @param u This update concerns this unit
     * @param add <code>true</code> if this unit is added to the watched set.
     */
    public UpdateEvent(Unit u, boolean add) {
      unit = u;
      this.add = add;
    }

    /**
     * Creates a "clear" or "global" event.
     *
     * @param b If <code>false</code> , clear all problems. If <code>true</code> update global problems.
     */
    public UpdateEvent(boolean b) {
      region = null;
      unit = null;
      add = b;
    }

    /**
     * Returns the value of region.
     *
     * @return Returns region.
     */
    public Region getRegion() {
      return region;
    }

    /**
     * Returns the value of unit.
     *
     * @return Returns unit.
     */
    public Unit getUnit() {
      return unit;
    }

    /**
     * Returns the value of add.
     *
     * @return Returns add.
     */
    public boolean isAdd() {
      return add;
    }

    /**
     * Returns either the region or the unit for this event whichever is non- <code>null</code>
     *
     * @return Return the object.
     */
    public Object getObject() {
      if (region != null)
        return region;
      if (unit != null)
        return unit;
      if (add)
        return DATA;
      else
        return CLEAR;
    }

  }

  /**
   * Enqueues and polls update events.
   *
   * @author stm
   * @version 1.0, Aug 23, 2008
   */
  private static class EQueue {
    private List<UpdateEvent> events = new LinkedList<UpdateEvent>();
    /** Takes track of number of insertions of objects to add. */
    private Map<Object, Integer> addObjects = new HashMap<Object, Integer>();
    /** Takes track of number of insertions of objects to delete. */
    private Map<Object, Integer> delObjects = new HashMap<Object, Integer>();
    private boolean clear = false;

    public EQueue() {
      //
    }

    /**
     * Take an object out of the queue. Returns the first event that was not inserted again later in the
     * queue.
     */
    public synchronized UpdateEvent poll() {
      UpdateEvent event = events.remove(0);
      Integer rank = event.isAdd() ? addObjects.get(event.getObject()) : delObjects.get(event
          .getObject());
      // remove obsolete events
      while (rank > 1 || clear) {
        if (rank > 1 || (clear && event.getObject() != UpdateEvent.CLEAR)) {
          // this is not the last event for the object or we are in clear mode
          // and this is not the last clear event
          if (TaskTablePanel.log.isDebugEnabled()) {
            TaskTablePanel.log.debug("skip " + event.getObject() + " " + event.isAdd() + " "
                + rank);
          }
          if (event.isAdd()) {
            addObjects.put(event.getObject(), rank - 1);
          } else {
            delObjects.put(event.getObject(), rank - 1);
          }
          event = events.remove(0);
          rank = event.isAdd() ? addObjects.get(event.getObject()) : delObjects.get(event
              .getObject());
        } else {
          // this is the last clear event in the queue
          clear = false;
        }
      }
      if (TaskTablePanel.log.isDebugEnabled()) {
        TaskTablePanel.log.debug("poll " + event.getObject() + " " + event.isAdd() + " " + rank);
      }
      if (event.isAdd()) {
        addObjects.remove(event.getObject());
      } else {
        delObjects.remove(event.getObject());
      }
      return event;
    }

    /**
     * Wait until an event is enqueued, then return it.
     *
     * @throws InterruptedException
     */
    public synchronized UpdateEvent waitFor() throws InterruptedException {
      while (events.size() == 0) {
        this.wait();
      }

      return poll();
    }

    /**
     * Enqueue an event.
     */
    public synchronized void push(UpdateEvent e) {
      if (e.getObject() == UpdateEvent.CLEAR) {
        clear = true;
      }
      if (e.isAdd()) {
        Integer rank = addObjects.get(e.getObject());
        if (rank == null) {
          addObjects.put(e.getObject(), 1);
          if (TaskTablePanel.log.isDebugEnabled()) {
            TaskTablePanel.log.debug("afirst " + e.getObject());
          }
        } else {
          addObjects.put(e.getObject(), rank + 1);
          if (TaskTablePanel.log.isDebugEnabled()) {
            TaskTablePanel.log.debug("adouble " + e.getObject() + " " + (rank + 1));
          }
        }
      } else {
        Integer rank = delObjects.get(e.getObject());
        if (rank == null) {
          delObjects.put(e.getObject(), 1);
          if (TaskTablePanel.log.isDebugEnabled()) {
            TaskTablePanel.log.debug("dfirst " + e.getObject());
          }
        } else {
          delObjects.put(e.getObject(), rank + 1);
          if (TaskTablePanel.log.isDebugEnabled()) {
            TaskTablePanel.log.debug("ddouble " + e.getObject() + " " + (rank + 1));
          }
        }
      }
      events.add(e);
      notifyAll();
    }

    public synchronized boolean isEmpty() {
      return addObjects.isEmpty() && delObjects.isEmpty();
    }

    public synchronized int size() {
      return addObjects.size() + delObjects.size();
    }

    public synchronized void clear() {
      clear = true;
      events.clear();
      addObjects.clear();
      delObjects.clear();
      clear = false;
    }
  }

  /**
   * This class registers and handles events in a separate thread. Events are first enqueued here and
   * than handled one by one in the refreshThread.
   *
   * @author stm
   * @version 1.0, Aug 23, 2008
   */
  // NOTE The constructor starts a thread. This is likely to be wrong if the class is ever
  // extended, since the thread will be started before the subclass constructor is
  // started.
  protected final class UpdateEventDispatcher {

    /**
     * The Thread which handles update events.
     */
    protected Thread refreshThread = null;

    /** The events are enqued here */
    protected EQueue queue;

    private boolean stop = false;

    /**
     * Creates a dispatcher and starts the dispatch thread.
     */
    public UpdateEventDispatcher() {
      queue = new EQueue();
      refreshThread = new Thread(new TaskSearchRunner(), "TaskTableRefresher");
      refreshThread.start();
    }

    /**
     * Return <code>true</code> if there are events in the queue. This is not really thread safe so
     * don't rely too much on it!
     *
     * @return <code>true</code> if there are events in the queue.
     */
    public synchronized boolean isBusy() {
      return !queue.isEmpty();
    }

    /**
     * The event queue.
     *
     * @author stm
     * @version 1.0, Aug 23, 2008
     */
    protected class TaskSearchRunner implements Runnable {
      public void run() {
        int progress = 1;
        while (!stop) {
          UpdateEvent event = null;
          try {
            // wait for next event
            while (queue.size() < 1) {
              Thread.sleep(200);
            }
            event = queue.waitFor();

            // update progress bar as good as possible...
            if (progressbar.getMaximum() < queue.size() + 1) {
              progressbar.setMaximum(queue.size() + 1);
              progress = 1;
            } else {
              progress++;
            }
            progressbar.setValue(progress);
            progressbar.repaint();

            // handle event
            Object object = event.getObject();
            if (UpdateEvent.CLEAR.equals(object)) {
              clearProblems();
            } else if (UpdateEvent.DATA.equals(object)) {
              reviewGlobal();
            } else {
              Region region = null;
              if (object instanceof Region) {
                region = event.getRegion();
              } else if (object instanceof Unit) {
                region = event.getUnit().getRegion();
              }
              if (region == null) {
                // regionless unit
                if (event.isAdd()) {
                  reviewUnit(event.getUnit());
                } else {
                  unReviewUnit(event.getUnit());
                }
              } else {
                if (event.isAdd()) {
                  reviewRegionAndUnits(region);
                } else {
                  unReviewRegionAndUnits(region);
                }
              }
            }

            if (queue.size() == 0) {
              progressbar.setMaximum(1);
              progress = 1;
              progressbar.setValue(progress);
              progressbar.repaint();
            }
          } catch (Throwable t) {
            // try to handle errors gracefully
            TaskTablePanel.log.error("Exception in TaskTable update thread:", t);
            try {
              error(event);
            } catch (Throwable t2) {
              TaskTablePanel.log.error("Error in TaskTable update thread:", t2);
            }
          }
        }
        stop = true;
      }
    }

    /**
     * Call removeRegion for all regions.
     *
     * @param regions
     */
    public void removeRegions(Collection<Region> regions) {
      for (Region r : regions) {
        removeRegion(r);
      }
    }

    /**
     * Enqueue an event for removing all problems corresponding to r
     *
     * @param r
     */
    public void removeRegion(Region r) {
      if (!isShown())
        return;
      queue.push(new UpdateEvent(r, false));
    }

    /**
     * Call addRegion for all regions.
     *
     * @param regions
     */
    public void addRegions(Collection<? extends Region> regions) {
      for (Region region : regions) {
        addRegion(region);
      }
    }

    /**
     * Enqueue an event for adding events corresponding to region.
     *
     * @param region
     */
    public void addRegion(Region region) {
      if (!isShown())
        return;
      queue.push(new UpdateEvent(region, true));
    }

    public void addUnits(Collection<Unit> units) {
      for (Unit u : units) {
        addUnit(u);
      }
    }

    /**
     * Enqueue an event for adding events corresponding to unit. Currently this is pretty much
     * equivalent to calling addRegion(u.getRegion()).
     *
     * @param u
     */
    public void addUnit(Unit u) {
      if (!isShown())
        return;
      queue.push(new UpdateEvent(u, true));
    }

    public void removeUnits(Collection<Unit> units) {
      for (Unit u : units) {
        removeUnit(u);
      }
    }

    /**
     * Enqueue an event for removing all events corresponding to unit. Currently this is pretty much
     * equivalent to calling removeRegion(u.getRegion()).
     *
     * @param u
     */
    public void removeUnit(Unit u) {
      if (!isShown())
        return;
      queue.push(new UpdateEvent(u, false));
    }

    public void addGlobal() {
      queue.push(new UpdateEvent(true));
    }

    public void clear() {
      queue.push(new UpdateEvent(false));
    }

    private void reset() {
      queue.clear();
    }

    public void quit() {
      stop = true;
    }

  }

  /**
   * Register all inspectors we know of.
   *
   * @param gameData
   */
  private void initInspectors(GameData gameData) {
    inspectors = new ArrayList<Inspector>();

    if (PropertiesHelper.getBoolean(settings, PropertiesHelper.TASKTABLE_INSPECTORS_ATTACK, true)) {
      inspectors.add(AttackInspector.getInstance(gameData));
    }
    if (PropertiesHelper.getBoolean(settings, PropertiesHelper.TASKTABLE_INSPECTORS_TODO, true)) {
      inspectors.add(ToDoInspector.getInstance(gameData));
    }
    if (PropertiesHelper.getBoolean(settings, PropertiesHelper.TASKTABLE_INSPECTORS_MOVEMENT,
        true)) {
      inspectors.add(MovementInspector.getInstance(gameData));
    }
    if (PropertiesHelper.getBoolean(settings, PropertiesHelper.TASKTABLE_INSPECTORS_SHIP, true)) {
      inspectors.add(ShipInspector.getInstance(gameData));
    }
    if (PropertiesHelper.getBoolean(settings, PropertiesHelper.TASKTABLE_INSPECTORS_BUILDING,
        true)) {
      inspectors.add(BuildingInspector.getInstance(gameData));
    }
    if (PropertiesHelper.getBoolean(settings, PropertiesHelper.TASKTABLE_INSPECTORS_SKILL, true)) {
      inspectors.add(SkillInspector.getInstance(gameData));
    }
    if (PropertiesHelper.getBoolean(settings, PropertiesHelper.TASKTABLE_INSPECTORS_TEACH, true)) {
      inspectors.add(TeachInspector.getInstance(gameData));
    }
    if (PropertiesHelper.getBoolean(settings, PropertiesHelper.TASKTABLE_INSPECTORS_GAMEDATA,
        true)) {
      inspectors.add(GameDataInspector.getInstance(gameData));
    }
    if (PropertiesHelper.getBoolean(settings, PropertiesHelper.TASKTABLE_INSPECTORS_MESSAGE,
        true)) {
      inspectors.add(MessageInspector.getInstance(gameData));
    }
    if (PropertiesHelper.getBoolean(settings, PropertiesHelper.TASKTABLE_INSPECTORS_MAINTENANCE,
        true)) {
      inspectors.add(MaintenanceInspector.getInstance(gameData));
    }
    if (PropertiesHelper.getBoolean(settings, PropertiesHelper.TASKTABLE_INSPECTORS_TRANSFER,
        true)) {
      inspectors.add(TransferInspector.getInstance(gameData));
    }
    // let's add this last to catch problems inserted by other inspectors
    if (PropertiesHelper.getBoolean(settings, PropertiesHelper.TASKTABLE_INSPECTORS_ORDER_SYNTAX,
        true)) {
      inspectors.add(OrderSyntaxInspector.getInstance(gameData));
    }

    for (Inspector i : inspectors) {
      i.setGameData(gameData);
    }

    getIgnoredProblems();
  }

  /**
   * Adds a new inspector to the list of insprectors
   */
  public void addInspector(Inspector inspector) {
    inspectors.add(inspector);
  }

  /**
   * Adds a new inspector interceptor to the list of inspectors
   */
  public void addInspectorInterceptor(InspectorInterceptor interceptor) {
    for (Inspector i : inspectors) {
      i.addInterceptor(interceptor);
    }
  }

  /**
   * Returns a list of all registered inspectors.
   */
  public List<Inspector> getInspectors() {
    return Collections.unmodifiableList(inspectors);
  }

  /**
   * Returns a list of all ProblemTypes of all registered inspectors.
   */
  public List<ProblemType> getAllProblemTypes() {
    List<ProblemType> types = null;
    for (Inspector i : getInspectors()) {
      for (ProblemType p : i.getTypes()) {
        if (types == null) {
          types = new ArrayList<ProblemType>(getInspectors().size());
        }
        types.add(p);
      }
    }
    if (types == null)
      return Collections.emptyList();
    else
      return Collections.unmodifiableList(types);
  }

  /**
   * clean up
   *
   * @see magellan.client.swing.InternationalizedDataPanel#quit()
   */
  @Override
  public void quit() {
    removeListeners();
    updateDispatcher.quit();

    super.quit();
  }

  private void removeListeners() {
    if (dispatcher != null) {
      // dispatcher.removeUnitOrdersListener(this);
      dispatcher.removeGameDataListener(this);
      dispatcher.removeSelectionListener(this);
    }
  }

  private void registerListeners() {
    if (dispatcher != null) {
      // dispatcher.addUnitOrdersListener(this);
      // unnecessary
      // dispatcher.addGameDataListener(this);
      dispatcher.addSelectionListener(this);
    }
  }

  /**
   * Clear table and refresh all problems.
   *
   * @see magellan.client.swing.InternationalizedDataPanel#gameDataChanged(magellan.library.event.GameDataEvent)
   */
  @Override
  public void gameDataChanged(GameDataEvent e) {
    super.gameDataChanged(e);
    updateDispatcher.reset();
    initInspectors(e.getGameData());
    lastActiveRegion = null;
    lastSelection = new HashSet<Region>();
    // refresh to include unknown problems
    getIgnoredProblems();

    // do nothing if Panel is hidden
    if (!isShown())
      return;
    // rebuild warning list
    refreshProblems();
  }

  protected void error(UpdateEvent event) {
    model.addProblem(ProblemFactory.createProblem(Severity.INFORMATION, INTERNAL, event != null
        ? event.region : null, null, null, event != null ? event.unit : null, null, INTERNAL
            .getMessage(), -1));
  }

  /**
   * @see magellan.client.event.SelectionListener#selectionChanged(magellan.client.event.SelectionEvent)
   */
  public void selectionChanged(SelectionEvent e) {
    if ((e.getSource() == this))
      return;

    // do nothing if Panel is hidden
    if (!isShown())
      return;

    synchronized (lastSelection) {
      // if selection has changed, refresh delta
      if (e.getSelectionType() == SelectionEvent.ST_REGIONS && e.getSelectedObjects() != null) {
        // no selection: refresh all
        if (e.getSelectedObjects().isEmpty()) {
          // copy to Hashset to improve performance!
          lastSelection = new HashSet<Region>(getGameData().getRegions());
          refreshProblems();
        } else {
          // copy to Hashset to improve performance!
          HashSet<?> eventSelection = new HashSet<Object>(e.getSelectedObjects());
          Collection<Region> newSelection = new HashSet<Region>();
          Collection<Region> addSelection = new HashSet<Region>();
          Collection<Region> delSelection = new HashSet<Region>();

          // remove problems of regions that are not selected any more
          for (Region region : lastSelection) {
            Object o = region;
            if (o instanceof Region) {
              if (!eventSelection.contains(o)) {
                delSelection.add((Region) o);
              }
            }
          }

          // add problems of newly selected region
          for (Object o : e.getSelectedObjects()) {
            if (o instanceof Region) {
              newSelection.add((Region) o);
              if (!lastSelection.contains(o)) {
                addSelection.add((Region) o);
              }
            }
          }
          lastSelection = newSelection;
          if (restrictToSelection()) {
            if (delSelection.size() > newSelection.size() * 2) {
              // if there are very many regions to delete, rather refresh all...
              refreshProblems();
            } else {
              updateDispatcher.addRegions(addSelection);
            }
            updateDispatcher.removeRegions(delSelection);
          }
        }
      }
    }

    // if we are in active region mode, check if active region has changed and
    // if so, refresh
    Region r = null;
    if (e.getActiveObject() instanceof Unit) {
      Unit u = (Unit) e.getActiveObject();
      if (u != null) {
        r = u.getRegion();
      }
    }
    if (e.getActiveObject() instanceof HasRegion) {
      r = ((HasRegion) e.getActiveObject()).getRegion();
    }
    if (e.getActiveObject() instanceof Region) {
      r = (Region) e.getActiveObject();
    }

    synchronized (this) {
      if (restrictToActiveRegion() && r != null && r.getData() == getGameData()
          && r != lastActiveRegion) {
        lastActiveRegion = r;
        refreshProblems();
      }
      lastActiveRegion = r;
    }

  }

  private void clearProblems() {
    if (runInThisThread) {
      model.clearProblems();
    } else {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          model.clearProblems();
        }
      });
    }
  }

  /**
   * Updates reviews when orders have changed.
   *
   * @param e
   * @see magellan.client.event.UnitOrdersListener#unitOrdersChanged(magellan.client.event.UnitOrdersEvent)
   */
  public void unitOrdersChanged(UnitOrdersEvent e) {
    update(e.getUnit());
  }

  protected void update(Unit unit) {
    Region r1 = unit.getRegion();
    Region r2 = unit.getNewRegion() == null ? null : unit.getData().getRegion(unit.getNewRegion());

    if (r1 != null) {
      updateDispatcher.removeRegion(r1);
    }
    if (r2 != null) {
      updateDispatcher.removeRegion(r2);
    }
    if (r1 != null) {
      updateDispatcher.addRegion(r1);
    }
    if (r2 != null) {
      updateDispatcher.addRegion(r2);
    }
  }

  private void reviewGlobal() {
    if (TaskTablePanel.log.isDebugEnabled()) {
      TaskTablePanel.log.debug("TaskTablePanel.reviewGlobal() called");
    }

    for (Inspector c : getInspectors()) {
      final List<Problem> gproblems = c.reviewGlobal();
      // add problems in the AWT event dispatching thread to avoid
      // synchronization issues!
      if (!gproblems.isEmpty()) {
        if (runInThisThread) {
          addProblems(gproblems);
        } else {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              addProblems(gproblems);
            }
          });
        }
      }
      for (Faction f : getGameData().getFactions())
        if (isValidFaction(f)) {
          final List<Problem> fproblems = c.reviewFaction(f);
          // add problems in the AWT event dispatching thread to avoid
          // synchronization issues!
          if (!fproblems.isEmpty()) {
            if (runInThisThread) {
              addProblems(fproblems);
            } else {
              SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                  addProblems(fproblems);
                }
              });
            }
          }
        }
    }

  }

  /**
   * Reviews a region with all units within.
   */
  private void reviewRegionAndUnits(Region r) {
    if (r == null)
      return;

    if (TaskTablePanel.log.isDebugEnabled()) {
      TaskTablePanel.log.debug("TaskTablePanel.reviewRegionAndUnits(" + r + ") called");
    }

    reviewRegion(r);

    if (r.units() == null)
      return;

    for (Unit u : r.units()) {
      reviewUnit(u);
    }

  }

  /**
   * Reviews a region with all units within.
   */
  private void unReviewRegionAndUnits(Region r) {
    if (r == null)
      return;

    if (TaskTablePanel.log.isDebugEnabled()) {
      TaskTablePanel.log.debug("TaskTablePanel.reviewRegionAndUnits(" + r + ") called");
    }

    removeRegion(r);

    if (r.units() == null)
      return;

    for (Unit u : r.units()) {
      unReviewUnit(u);
    }
  }

  private void removeRegion(final Region r) {
    for (final Inspector c : getInspectors()) {
      if (runInThisThread) {
        model.removeProblems(c, r);
      } else {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            // remove problems in the AWT event dispatching thread to avoid
            // synchronization issues!
            model.removeProblems(c, r);
          }
        });
      }
    }
  }

  private void unReviewUnit(final Unit u) {
    for (final Inspector c : getInspectors()) {
      if (runInThisThread) {
        model.removeProblems(c, u);
      } else {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            // remove problems in the AWT event dispatching thread to avoid
            // synchronization issues!
            model.removeProblems(c, u);
          }
        });
      }
    }
  }

  /**
   * Reviews a the specified region and the specified unit.
   */
  private void reviewRegion(Region r) {
    for (Inspector c : getInspectors()) {
      if (r != null) {
        final List<Problem> problems = c.reviewRegion(r);
        if (runInThisThread) {
          addProblems(problems);
        } else
        // add problems in the AWT event dispatching thread to avoid
        // synchronization issues!
        if (!problems.isEmpty()) {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              addProblems(problems);
            }
          });
        }
      }
    }
  }

  protected void addProblems(List<Problem> problems) {
    for (Problem p : problems) {
      if (checkActive(p)) {
        model.addProblem(p);
      }
    }
  }

  /**
   * Reviews a the specified region and the specified unit.
   */
  private void reviewUnit(Unit u) {
    if (!isValidUnitByFaction(u))
      return;
    for (Inspector c : getInspectors()) {
      if (u != null) {
        final List<Problem> problems = c.reviewUnit(u);
        if (runInThisThread) {
          addProblems(problems);
        } else {
          // add problems in the AWT event dispatching thread to avoid
          // synchronization issues!
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              addProblems(problems);
            }
          });
        }
      }
    }
  }

  /**
   * checks, if the specified unit is valid according to the settings restrictToOwner and
   * restrictToPassword
   *
   * @param u
   * @return
   */
  private boolean isValidUnitByFaction(Unit u) {
    return isValidFaction(u.getFaction());
  }

  private boolean isValidFaction(Faction f) {
    // maybe it better to ignore the "restrictToOwner" setting, if there is no
    // faction owner.
    if (restrictToOwner() && !restrictToPassword() && (getGameData().getOwnerFaction() == null
        || f == null || !getGameData().getOwnerFaction().equals(f.getID())))
      return false;
    if (restrictToPassword() && f != null && (f.getPassword() == null || f.getPassword()
        .length() == 0))
      return false;

    return true;
  }

  /**
   * Specifies which problems to use. Only problems whose <code>ProblemType.getName()</code> matches
   * one in result will be displayed.
   *
   * @param result A list of name. If <code>null</code>, all problems will be displayed.
   * @deprecated We use a negative list now
   * @see #setIgnoredProblems(Set)
   */
  @Deprecated
  public void setActiveProblems(Set<ProblemType> result) {
    // activeProblems = result;
  }

  /**
   * Specifies which problems to ignore. Only problems whose <code>ProblemType.getName()</code> does
   * not match one in this set will be displayed.
   *
   * @param set A list of names. If <code>null</code>, all problems will be displayed.
   */
  public void setIgnoredProblems(Set<ProblemType> set) {
    // refresh map to include unknown problems
    getAllProblemTypes();

    // make sure that unknown problems are replaced by their now existing problem (e.g. after game
    // data change)
    ignoredProblems = new HashSet<ProblemType>();
    if (set != null) {
      for (ProblemType newP : set)
        if (pMap.containsKey(newP.getName())) {
          ignoredProblems.add(pMap.get(newP.getName()));
        } else {
          ignoredProblems.add(newP);
        }
    }

    for (Inspector i : getInspectors()) {
      for (ProblemType type : i.getTypes()) {
        i.setIgnore(type, ignoredProblems.contains(type));
      }
    }

    setIgnoredProperty();
  }

  private void setIgnoredProperty() {
    StringBuffer definition = new StringBuffer("");

    for (ProblemType p : ignoredProblems) {
      if (definition.length() > 0) {
        definition.append(";");
      }
      definition.append(p.getName());
    }

    settings.setProperty(PropertiesHelper.TASKTABLE_INSPECTORS_IGNORE_LIST, definition.toString());
  }

  private void addIgnoredProblem(Problem p) {
    ignoredProblems.add(p.getType());
    if (p.getInspector() != null) {
      p.getInspector().setIgnore(p.getType(), true);
    }
    setIgnoredProperty();
  }

  /**
   * Returns a list of all ignored problem types (read from the settings if necessary).
   */
  @SuppressWarnings("deprecation")
  public Set<ProblemType> getIgnoredProblems() {
    ignoredProblems = new HashSet<ProblemType>();

    String criteria = settings.getProperty(PropertiesHelper.TASKTABLE_INSPECTORS_LIST);
    if (criteria != null) {
      Logger.getInstance(this.getClass()).warn("deprecated property "
          + PropertiesHelper.TASKTABLE_INSPECTORS_LIST);
      settings.remove(PropertiesHelper.TASKTABLE_INSPECTORS_LIST);
    }

    criteria = settings.getProperty(PropertiesHelper.TASKTABLE_INSPECTORS_IGNORE_LIST);

    if (criteria != null) {
      pMap = new HashMap<String, ProblemType>();
      for (ProblemType p : getAllProblemTypes()) {
        pMap.put(p.getName(), p);
      }
      for (StringTokenizer tokenizer = new StringTokenizer(criteria, ";"); tokenizer
          .hasMoreTokens();) {
        String s = tokenizer.nextToken();
        ProblemType p;
        if (!pMap.containsKey(s)) {
          pMap.put(s, new ProblemType(s, Resources.get("tasks.unknowntype.group"), Resources.get(
              "tasks.unknowntype.description"), null));
        }
        p = pMap.get(s);
        ignoredProblems.add(p);
      }
    }

    for (Inspector i : getInspectors()) {
      for (ProblemType type : i.getTypes()) {
        i.setIgnore(type, ignoredProblems.contains(type));
      }
    }

    return Collections.unmodifiableSet(ignoredProblems);
  }

  private boolean checkActive(Problem p) {
    if (ignoredProblems != null && ignoredProblems.contains(p.getType()))
      return false;
    if (!isValidFaction(p.getFaction()))
      return false;
    if (restrictToActiveRegion()) {
      if (lastActiveRegion != null)
        return p.getRegion() == null || p.getRegion() == lastActiveRegion;
    } else if (restrictToSelection())
      return p.getRegion() == null || lastSelection == null || lastSelection.contains(p
          .getRegion());

    return true;
  }

  /**
   * Returns <code>true</code> if problems will be restricted to the owner faction.
   */
  public boolean restrictToOwner() {
    return PropertiesHelper.getBoolean(settings, PropertiesHelper.TASKTABLE_RESTRICT_TO_OWNER,
        true);
  }

  /**
   * Returns <code>true</code> if problems will be restricted to factions with known password.
   */
  public boolean restrictToPassword() {
    return PropertiesHelper.getBoolean(settings, PropertiesHelper.TASKTABLE_RESTRICT_TO_PASSWORD,
        true);
  }

  /**
   * Sets whether problems will be restricted to the owner faction.
   */
  public void setRestrictToOwner(boolean value) {
    settings.put(PropertiesHelper.TASKTABLE_RESTRICT_TO_OWNER, String.valueOf(value));
  }

  /**
   * Sets whether problems will be restricted to factions with known password.
   */
  public void setRestrictToPassword(boolean value) {
    settings.put(PropertiesHelper.TASKTABLE_RESTRICT_TO_PASSWORD, String.valueOf(value));
  }

  /**
   * Returns <code>true</code> if problems will be restricted to current region selection.
   */
  public boolean restrictToSelection() {
    return PropertiesHelper.getBoolean(settings, PropertiesHelper.TASKTABLE_RESTRICT_TO_SELECTION,
        false);
  }

  /**
   * Returns <code>true</code> if problems will be restricted to current region.
   */
  public boolean restrictToActiveRegion() {
    return PropertiesHelper.getBoolean(settings,
        PropertiesHelper.TASKTABLE_RESTRICT_TO_ACTIVEREGION, false);
  }

  /**
   * Sets whether problems will be restricted to current selection.
   */
  public void setRestrictToSelection(boolean value) {
    settings.put(PropertiesHelper.TASKTABLE_RESTRICT_TO_SELECTION, String.valueOf(value));
    // selectionLabel.setEnabled(value);
  }

  /**
   * Sets whether problems will be restricted to current region.
   */
  public void setRestrictToActiveRegion(boolean value) {
    settings.put(PropertiesHelper.TASKTABLE_RESTRICT_TO_ACTIVEREGION, String.valueOf(value));
    // activeRegionLabel.setEnabled(value);
  }

  /**
   * Returns <code>true</code> if global problems will be shown.
   */
  public boolean showGlobal() {
    return PropertiesHelper.getBoolean(settings, PropertiesHelper.TASKTABLE_SHOW_GLOBAL, true);
  }

  /**
   * Sets whether global problems will be shown.
   */
  public void setShowGlobal(boolean value) {
    settings.put(PropertiesHelper.TASKTABLE_SHOW_GLOBAL, String.valueOf(value));
    // globalLabel.setEnabled(value);
  }

  private Vector<String> getHeaderTitles() {
    if (headerTitles == null) {
      headerTitles = new Vector<String>(7);
      headerTitles.add(Resources.get("tasks.tasktablepanel.header.description"));
      headerTitles.add(Resources.get("tasks.tasktablepanel.header.object"));
      headerTitles.add(Resources.get("tasks.tasktablepanel.header.region"));
      headerTitles.add(Resources.get("tasks.tasktablepanel.header.faction"));
      headerTitles.add(Resources.get("tasks.tasktablepanel.header.line"));
      headerTitles.add(Resources.get("tasks.tasktablepanel.header.type"));
    }
    return headerTitles;
  }

  private static class TaskTableModel extends DefaultTableModel {

    /**
     * Creates a new TaskTableModel object.
     *
     * @param header
     * @see javax.swing.table.DefaultTableModel#DefaultTableModel(Vector, int)
     */
    public TaskTableModel(Vector<?> header) {
      super(header, 0);
    }

    /**
     * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
     */
    @Override
    public boolean isCellEditable(int row, int column) {
      return false;
    }

    public void clearProblems() {
      for (int i = getRowCount() - 1; i >= 0; i--) {
        removeRow(i);
      }
    }

    /**
     * Adds a list of problems one by one. Should be called in the AWT event dispatch thread!
     */
    @SuppressWarnings("unused")
    public void addProblems(List<Problem> problems) {
      for (Problem p : problems) {
        addProblem(p);
      }
    }

    protected static final int PROBLEM_POS = 0;
    protected static final int OBJECT_POS = 1;
    protected static final int REGION_POS = 2;
    protected static final int FACTION_POS = 3;
    protected static final int LINE_POS = 4;
    protected static final int TYPE_POS = 5;
    protected static final int NUMBEROF_POS = 6;

    /**
     * Add a problem to this model. Should be called in the AWT event dispatch thread!
     *
     * @param p
     */
    public void addProblem(final Problem p) {
      for (int r = 0; r < getRowCount(); ++r)
        if (getValueAt(r, 0).equals(p))
          return;

      Faction faction = p.getFaction();

      Object[] newRow = new Object[TaskTableModel.NUMBEROF_POS + 1];
      int i = 0;
      newRow[i++] = p;
      newRow[i++] = p.getObject();
      newRow[i++] = p.getRegion();
      newRow[i++] = faction == null ? "" : faction;
      newRow[i++] = (p.getLine() < 1) ? "" : Integer.toString(p.getLine());
      newRow[i++] = p.getType().getName();// Resources.get("tasks.tasktablepanel.problemtype_" +
      // p.getSeverity().toString());
      newRow[i++] = null;
      addRow(newRow);
    }

    /**
     * Remove all problems of the given inspector <i>and</i> the given source. Should be called in the
     * AWT event dispatch thread!
     *
     * @param inspector
     * @param source
     */
    public void removeProblems(final Inspector inspector, final Object source) {
      Vector<?> dataVector = getDataVector();

      for (int i = getRowCount() - 1; i >= 0; i--) {
        if (i >= dataVector.size()) {
          TaskTablePanel.log.warn("TaskTablePanel: synchronization problem");
          break;
        }
        Vector<?> v = (Vector<?>) dataVector.get(i);
        Problem p = (Problem) v.get(TaskTableModel.PROBLEM_POS);

        // Inspector and region: only non unit objects will be removed
        // FIXME (stm)
        if (source instanceof Region && p.getRegion() != null) {
          if (inspector.equals(p.getInspector()) && p.getRegion().equals(source)) {
            removeRow(i);
          }
        } else if (source instanceof Unit && p.getOwner() != null) {
          if (inspector.equals(p.getInspector()) && p.getOwner().equals(source)) {
            removeRow(i);
          }
        }
      }
    }
  }

  public class TTShortcutListener implements ShortcutListener {

    /**
     * Should return all short cuts this class want to be informed. The elements should be of type
     * javax.swing.KeyStroke
     */
    public Iterator<KeyStroke> getShortCuts() {
      return shortcuts.iterator();
    }

    /**
     * This method is called when a shortcut from getShortCuts() is recognized.
     *
     * @param shortcut
     */
    public void shortCut(javax.swing.KeyStroke shortcut) {
      int index = shortcuts.indexOf(shortcut);

      switch (index) {
      case -1:
        break; // unknown shortcut

      case 0:
        DesktopEnvironment.requestFocus(MagellanDesktop.TASKS_IDENTIFIER);
        table.requestFocusInWindow();
        break;
      }
    }

    /**
     * @see magellan.client.desktop.ShortcutListener#getShortcutDescription(KeyStroke)
     */
    public String getShortcutDescription(KeyStroke stroke) {
      int index = shortcuts.indexOf(stroke);

      return Resources.get("tasks.shortcut.description." + String.valueOf(index));
    }

    /**
     * @see magellan.client.desktop.ShortcutListener#getListenerDescription()
     */
    public String getListenerDescription() {
      return Resources.get("tasks.shortcut.title");
    }

  }

  /**
   * @see magellan.client.swing.preferences.PreferencesFactory#createPreferencesAdapter()
   */
  public PreferencesAdapter createPreferencesAdapter() {
    return new TaskTablePreferences(this, settings, getGameData());
  }

  @Override
  public void paint(Graphics g) {
    if (!isShown()) {
      // Panel was deactivated earlier (or never opened)
      // and new we have a paint command...
      // need to rebuild the problems
      setShown(true);
      // log.info("TaskTablePanel shown after hide! -> refreshing.");
      refreshProblems();
    }
    super.paint(g);
  }

  /**
   * Sets the value of isShown.
   *
   * @param isShown The value for isShown.
   */
  private void setShown(boolean isShown) {
    shown = isShown;
  }

  private boolean checkShown() {
    // check, if TaskTablePanel is visible anyway
    MagellanDesktop MD = MagellanDesktop.getInstance();
    JMenu desktopMenu = MD.getDesktopMenu();
    boolean menuSelected = true;
    if (desktopMenu != null) {
      for (int index = 0; index < desktopMenu.getItemCount(); index++) {
        if (desktopMenu.getItem(index) instanceof JCheckBoxMenuItem) {
          JCheckBoxMenuItem menu = (JCheckBoxMenuItem) desktopMenu.getItem(index);
          if (menu.getActionCommand().equals("menu." + MagellanDesktop.TASKS_IDENTIFIER)) {
            menuSelected = menu.isSelected();
          }
        }
      }
    }
    if (!menuSelected) {
      // Our Panel is not selected -> need no refresh
      setShown(false);
    }
    return isShown();
  }

  /**
   * Returns the value of isShown.
   *
   * @return Returns isShown.
   */
  private boolean isShown() {
    return shown;
  }

  @Override
  public void setGameData(GameData data) {
    getGameData().removeUnitChangeListener(this);
    super.setGameData(data);
    data.addUnitChangeListener(this);
  }

  public void unitChanged(UnitChangeEvent event) {
    update(event.getUnit());
  }
}
