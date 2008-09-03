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

package magellan.client.swing.tasks;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
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
import magellan.client.event.UnitOrdersListener;
import magellan.client.preferences.TaskTablePreferences;
import magellan.client.swing.InternationalizedDataPanel;
import magellan.client.swing.ProgressBarUI;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.client.swing.preferences.PreferencesFactory;
import magellan.client.swing.table.TableSorter;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.HasRegion;
import magellan.library.Region;
import magellan.library.Unit;
import magellan.library.event.GameDataEvent;
import magellan.library.event.GameDataListener;
import magellan.library.tasks.AttackInspector;
import magellan.library.tasks.Inspector;
import magellan.library.tasks.MovementInspector;
import magellan.library.tasks.OrderSyntaxInspector;
import magellan.library.tasks.Problem;
import magellan.library.tasks.ShipInspector;
import magellan.library.tasks.ToDoInspector;
import magellan.library.utils.PropertiesHelper;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;
import net.infonode.docking.DockingWindow;
import net.infonode.docking.DockingWindowListener;
import net.infonode.docking.OperationAbortedException;
import net.infonode.docking.View;

/**
 * A panel for showing reviews about unit, region and/or gamedata.
 */
public class TaskTablePanel extends InternationalizedDataPanel implements UnitOrdersListener,
    SelectionListener, ShortcutListener, GameDataListener, PreferencesFactory,
    DockingWindowListener {
  private static final Logger log = Logger.getInstance(TaskTablePanel.class);

  public static final String IDENTIFIER = "TASKS";

  private JTable table;
  private TableSorter sorter;
  private TaskTableModel model;

  private JProgressBar progressbar;

  private JLabel selectionLabel;

  private JLabel activeRegionLabel;

  private List<Inspector> inspectors;

  /**
   * Not easy to detect if this panel is shown / visible switched OFF: if
   * refreshProblems is detecting, that the JMenu entry in
   * MagellanDeskTop.desktopMenu is not selected switched ON: if paint(Graphics
   * g) is detected and isShown=false in that case refreshProblems is called
   * too. adjusted by Events from DockingWindowFramework
   */
  private boolean shown = true;

  /**
   * Indicator of a refresh is needed if Panel becomes visible again
   */
  private boolean needRefresh = true;

  /** list of registered keyboard shortcuts */
  private List<KeyStroke> shortcuts;

  private Collection<Region> lastSelection = Collections.emptyList();

  private Region lastActiveRegion;

  private Vector<String> headerTitles;

  private magellan.client.swing.tasks.TaskTablePanel.UpdateEventDispatcher updateDispatcher;

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
    initInspectors();
    initUpdateThread();
    refreshProblems();

  }

  private void initGUI() {
    model = new TaskTableModel(getHeaderTitles());
    sorter = new TableSorter(model);
    table = new JTable(sorter);

    sorter.setTableHeader(table.getTableHeader()); // NEW

    // allow reordering of headers
    table.getTableHeader().setReorderingAllowed(true);

    // set row selection to single selection
    table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

    PopupListener popupListener = new PopupListener();
    // react on double clicks on a row
    table.addMouseListener(popupListener);

    // layout component
    this.setLayout(new BorderLayout());
    this.add(new JScrollPane(table), BorderLayout.CENTER);

    JPanel statusBar = new JPanel(new GridBagLayout());

    selectionLabel = new JLabel(Resources.get("tasks.selectionlabel.title"));
    selectionLabel.setToolTipText(Resources.get("tasks.selectionlabel.tooltip"));
    selectionLabel.setEnabled(restrictToSelection());
    selectionLabel.addMouseListener(popupListener);
    activeRegionLabel = new JLabel(Resources.get("tasks.activeregionlabel.title"));
    activeRegionLabel.setToolTipText(Resources.get("tasks.activeregionlabel.tooltip"));
    activeRegionLabel.setEnabled(restrictToActiveRegion());
    activeRegionLabel.addMouseListener(popupListener);

    progressbar = new JProgressBar();
    GridBagConstraints c =
        new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START,
            GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 3, 0);
    statusBar.add(selectionLabel, c);
    c.gridx++;
    statusBar.add(activeRegionLabel, c);
    c.gridx++;
    c.weightx = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    statusBar.add(progressbar, c);

    this.add(statusBar, BorderLayout.PAGE_END);

    this.setFocusable(false);

    // initialize shortcuts
    shortcuts = new ArrayList<KeyStroke>(1);

    // 0: Focus
    shortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK));

    // register for shortcuts
    DesktopEnvironment.registerShortcutListener(this);

  }

  class PopupListener extends MouseAdapter {
    JPopupMenu tableMenu;
    JPopupMenu busyMenu;
    JPopupMenu activeRegionMenu;
    JPopupMenu selectionMenu;

    JMenuItem refreshMenu;
    JMenuItem acknowledgeMenu;
    JMenuItem unAcknowledgeMenu;

    PopupListener() {
      createPopupMenus();
    }

    /**
     * Create and show context menu.
     * 
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    public void mouseClicked(final MouseEvent e) {
      maybeSelect(e);
      maybeShowPopup(e);
    }

    public void mousePressed(MouseEvent e) {
      maybeShowPopup(e);
    }

    public void mouseReleased(MouseEvent e) {
      maybeShowPopup(e);
    }

    private void maybeSelect(MouseEvent e) {
      if (updateDispatcher.isBusy()) {
        return;
      }
      if (e.getSource() == table) {
        if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
          JTable target = (JTable) e.getSource();
          int row = target.getSelectedRow();

          if (TaskTablePanel.log.isDebugEnabled()) {
            TaskTablePanel.log.debug("TaskTablePanel: Double click on row " + row);
          }
          selectObjectOnRow(row);
        }
      }
    }

    private void maybeShowPopup(MouseEvent e) {
      if (updateDispatcher.isBusy()) {
        busyMenu.show(e.getComponent(), e.getX(), e.getY());
        return;
      }

      if (e.getSource() == table) {
        // unselected rows if user clicke another row
        int rowClicked = table.rowAtPoint(e.getPoint());
        boolean clickedOnSelection = false;
        for (int i : table.getSelectedRows()) {
          if (rowClicked == i) {
            clickedOnSelection = true;
            break;
          }
        }
        if (!clickedOnSelection) {
          table.clearSelection();
          table.addRowSelectionInterval(rowClicked, rowClicked);
        }

        if (e.isPopupTrigger()) {
          if (table.getSelectedRowCount() > 0)
            acknowledgeMenu.setEnabled(true);
          else
            acknowledgeMenu.setEnabled(false);
          tableMenu.show(e.getComponent(), e.getX(), e.getY());
        }
      } else if (e.getSource() == activeRegionLabel) {
        activeRegionMenu.show(e.getComponent(), e.getX(), e.getY());
      } else if (e.getSource() == selectionLabel) {
        selectionMenu.show(e.getComponent(), e.getX(), e.getY());
      }
    }

    private void createPopupMenus() {
      tableMenu = new JPopupMenu();
      refreshMenu = new JMenuItem(Resources.get("tasks.contextmenu.refresh.title"));
      acknowledgeMenu = new JMenuItem(Resources.get("tasks.contextmenu.acknowledge.title"));
      unAcknowledgeMenu = new JMenuItem(Resources.get("tasks.contextmenu.unacknowledge.title"));

      tableMenu.add(refreshMenu);
      tableMenu.add(acknowledgeMenu);
      tableMenu.add(unAcknowledgeMenu);

      refreshMenu.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent menuEvent) {
          refreshProblems();
        }
      });

      acknowledgeMenu.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent menuEvent) {
          int row = table.getSelectedRow();
          acknowledge(row);
        }
      });

      unAcknowledgeMenu.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent menuEvent) {
          unAcknowledge();
        }
      });

      busyMenu = new JPopupMenu();
      JMenuItem busyMenuItem = new JMenuItem(Resources.get("tasks.contextmenu.busy.title"));
      busyMenuItem.setEnabled(false);
      busyMenu.add(busyMenuItem);

      activeRegionMenu = new JPopupMenu();
      JMenuItem arMenuItem =
          new JMenuItem(Resources.get("tasks.contextmenu.restricttoactiveregion.title"));
      activeRegionMenu.add(arMenuItem);

      arMenuItem.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          setRestrictToActiveRegion(!restrictToActiveRegion());
          refreshProblems();
        }
      });

      selectionMenu = new JPopupMenu();
      JMenuItem sMenuItem =
          new JMenuItem(Resources.get("tasks.contextmenu.restricttoselection.title"));
      selectionMenu.add(sMenuItem);

      sMenuItem.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          setRestrictToSelection(!restrictToSelection());
          refreshProblems();
        }
      });

    }
  }

  protected void unAcknowledge() {
    Window w = SwingUtilities.getWindowAncestor(this);
    final ProgressBarUI progressUI = new ProgressBarUI((JFrame) (w instanceof JFrame ? w : null));
    progressUI.setTitle(Resources.get("tasks.progressbar.unack.title"));

    progressUI.setMaximum(data.regions().size());

    new Thread(new Runnable() {
      public void run() {
        try {
          progressUI.show();
          int iProgress = 0;
          for (Inspector i : getInspectors()) {
            for (Region r : data.regions().values()) {
              progressUI.setProgress(r.getName(), ++iProgress);
              for (Unit u : r.units()) {
                i.unSuppress(u);
              }
            }
          }
          dispatcher.fire(new GameDataEvent(this, data));
        } finally {
          try {
            progressUI.ready();
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }

    }).start();

  }

  protected void acknowledge(int row) {
    Problem p = (Problem) sorter.getValueAt(row, TaskTableModel.PROBLEM_POS);
    selectObjectOnRow(row);
    log.info("ack " + row + " " + p.getObject());

    Unit u = p.addSuppressComment();
    if (u != null)
      dispatcher.fire(new UnitOrdersEvent(this, u));
  }

  /**
   * Fire selection event for object associated with row
   * 
   * @param row
   */
  private void selectObjectOnRow(int row) {
    Object obj = sorter.getValueAt(row, TaskTableModel.OBJECT_POS);
    dispatcher.fire(new SelectionEvent<Object>(this, null, obj));
  }

  private void initUpdateThread() {
    updateDispatcher = new UpdateEventDispatcher();
  }

  /**
   * Rebuild the complete table.
   */
  public void refreshProblems() {
    checkShown();
    if (!this.isShown() || !this.needRefresh) {
      log.debug("call to refreshProblems rejected  (panel not visible), reason: " + this.isShown());
      return;
    }

    updateDispatcher.clear();
    if (restrictToActiveRegion()) {
      if (lastActiveRegion != null)
        updateDispatcher.addRegion(lastActiveRegion);
    } else if (restrictToSelection() && lastSelection != null && !lastSelection.isEmpty()) {
      updateDispatcher.addRegions(lastSelection);
    } else {
      updateDispatcher.addRegions(data.regions().values());
    }
  }

  /**
   * Holds information about a necessary update on the task panel.
   * 
   * @author stm
   * @version 1.0, Aug 23, 2008
   */
  protected class UpdateEvent {
    
    private final Object CLEAR = "EMPTY";
    private Region region;
    private Unit unit;
    private boolean add;

    /**
     * @param r
     *          This update concerns a region
     * @param add
     *          <code>true</code> if this region is added to the watched set,
     *          <code>false</code> if the corresponding problems should be
     *          removed.
     */
    public UpdateEvent(Region r, boolean add) {
      this.region = r;
      this.add = add;
    }

    /**
     * @param u
     *          This update concerns this unit
     * @param add
     *          <code>true</code> if this unit is added to the watched set.
     */
    public UpdateEvent(Unit u, boolean add) {
      this.unit = u;
      this.add = add;
    }

    /**
     * If <code>false</code> , clear all problems.
     * 
     * @param b
     */
    public UpdateEvent(boolean b) {
      this.region = null;
      this.unit = null;
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
     * Returns either the region or the unit for this event whichever is non-
     * <code>null</code>
     * 
     * @return Return the object.
     */
    public Object getObject() {
      if (region != null)
        return region;
      if (unit != null)
        return unit;
      return CLEAR;
    }

  }

  /**
   * Enqueues and polls update events.
   * 
   * @author stm
   * @version 1.0, Aug 23, 2008
   */
  private class EQueue {
    private List<UpdateEvent> events = new LinkedList<UpdateEvent>();
    /** Takes track of number of insertions of objects to add. */
    private Map<Object, Integer> addObjects = new HashMap<Object, Integer>();
    /** Takes track of number of insertions of objects to delete. */
    private Map<Object, Integer> delObjects = new HashMap<Object, Integer>();
    private boolean clear = false;

    /**
     * Take an object out of the queue. Returns the first event that was not
     * inserted again later in the queue.
     */
    public synchronized UpdateEvent poll() {
      UpdateEvent event = events.remove(0);
      Integer rank =
          event.isAdd() ? addObjects.get(event.getObject()) : delObjects.get(event.getObject());
      // remove obsolete events
      while (rank > 1 || clear) {
        if (rank > 1 || (clear && event.getObject() != event.CLEAR)) {
          // this is not the last event for the object or we are in clear mode
          // and this is not the last clear event
          if (log.isDebugEnabled())
            log.debug("skip " + event.getObject() + " " + event.isAdd() + " " + rank);
          if (event.isAdd())
            addObjects.put(event.getObject(), rank - 1);
          else
            delObjects.put(event.getObject(), rank - 1);
          event = events.remove(0);
          rank =
              event.isAdd() ? addObjects.get(event.getObject()) : delObjects.get(event.getObject());
        } else {
          // this is the last clear event in the queue
          clear = false;
        }
      }
      if (log.isDebugEnabled())
        log.debug("poll " + event.getObject() + " " + event.isAdd() + " " + rank);
      if (event.isAdd())
        addObjects.remove(event.getObject());
      else
        delObjects.remove(event.getObject());
      return event;
    }

    /**
     * Wait until an event is enqueued, then return it.
     * 
     * @throws InterruptedException
     */
    public synchronized UpdateEvent waitFor() throws InterruptedException {
      if (events.size() == 0) {
        this.wait();
      }

      return poll();
    }

    /**
     * Enqueue an event.
     */
    public synchronized void push(UpdateEvent e) {
      if (e.getObject() == e.CLEAR)
        clear = true;
      if (e.isAdd()) {
        Integer rank = addObjects.get(e.getObject());
        if (rank == null) {
          addObjects.put(e.getObject(), 1);
          if (log.isDebugEnabled())
            log.debug("afirst " + e.getObject());
        } else {
          addObjects.put(e.getObject(), rank + 1);
          if (log.isDebugEnabled())
            log.debug("adouble " + e.getObject() + " " + (rank + 1));
        }
      } else {
        Integer rank = delObjects.get(e.getObject());
        if (rank == null) {
          delObjects.put(e.getObject(), 1);
          if (log.isDebugEnabled())
            log.debug("dfirst " + e.getObject());
        } else {
          delObjects.put(e.getObject(), rank + 1);
          if (log.isDebugEnabled())
            log.debug("ddouble " + e.getObject() + " " + (rank + 1));
        }
      }
      events.add(e);
      this.notifyAll();
    }

    public synchronized boolean isEmpty() {
      return addObjects.isEmpty() && delObjects.isEmpty();
    }

    public synchronized int size() {
      return addObjects.size() + delObjects.size();
    }

    // public synchronized void clear() {
    // events.clear();
    // addObjects.clear();
    // delObjects.clear();
    // }
  }

  /**
   * This class registers and handles events in a separate thread. Events are
   * first enqueued here and than handled one by one in the refreshThread.
   * 
   * @author stm
   * @version 1.0, Aug 23, 2008
   */
  protected class UpdateEventDispatcher {

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
      refreshThread = new Thread(new Runner());
      refreshThread.start();
    }

    /**
     * Return <code>true</code> if there are events in the queue. This is not
     * really thread safe so don't rely too much on it!
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
    protected class Runner implements Runnable {
      public void run() {
        int progress = 1;
        while (!stop) {
          try {
            UpdateEvent event;
            // wait for next event
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
            if (object.equals(event.CLEAR)) {
              clearProblems();
            } else {
              Region region = null;
              if (object instanceof Region) {
                region = event.getRegion();
              } else if (object instanceof Unit) {
                region = event.getUnit().getRegion();
              }
              if (region == null) {
                // regionless unit
                if (event.isAdd())
                  reviewUnit(event.getUnit());
                else
                  unReviewUnit(event.getUnit());
              } else {
                if (event.isAdd())
                  reviewRegionAndUnits(region);
                else
                  unReviewRegionAndUnits(region);
              }
            }
            if (queue.size() == 0)
              progressbar.setMaximum(0);
          } catch (Throwable t) {
            log.error("Exception in TaskTable update thread:" + t);
            t.printStackTrace();
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
      for (Region r : regions)
        removeRegion(r);
    }

    /**
     * Enqueue an event for removing all problems corresponding to r
     * 
     * @param r
     */
    public void removeRegion(Region r) {
      if (!isShown()) {
        return;
      }
      queue.push(new UpdateEvent(r, false));
    }

    /**
     * Call addRegion for all regions.
     * 
     * @param regions
     */
    public void addRegions(Collection<Region> regions) {
      for (Region region : regions)
        addRegion(region);
    }

    /**
     * Enqueue an event for adding events corresponding to region.
     * 
     * @param region
     */
    public void addRegion(Region region) {
      if (!isShown()) {
        return;
      }
      queue.push(new UpdateEvent(region, true));
    }

    public void addUnits(Collection<Unit> units) {
      for (Unit u : units)
        addUnit(u);
    }

    /**
     * Enqueue an event for adding events corresponding to unit. Currently this
     * is pretty much equivalent to calling addRegion(u.getRegion()).
     * 
     * @param u
     */
    public void addUnit(Unit u) {
      if (!isShown()) {
        return;
      }
      queue.push(new UpdateEvent(u, true));
    }

    public void removeUnits(Collection<Unit> units) {
      for (Unit u : units)
        removeUnit(u);
    }

    /**
     * Enqueue an event for removing all events corresponding to unit. Currently
     * this is pretty much equivalent to calling removeRegion(u.getRegion()).
     * 
     * @param u
     */
    public void removeUnit(Unit u) {
      if (!isShown()) {
        return;
      }
      queue.push(new UpdateEvent(u, false));
    }

    public void clear() {
      queue.push(new UpdateEvent(false));
    }

    public void quit() {
      stop = true;
    }

  }



  /**
   * Register all inspectors we know of.
   */
  private void initInspectors() {
    inspectors = new ArrayList<Inspector>();

    if (PropertiesHelper.getBoolean(settings, PropertiesHelper.TASKTABLE_INSPECTORS_ATTACK, true)) {
      inspectors.add(AttackInspector.getInstance());
    }
    if (PropertiesHelper.getBoolean(settings, PropertiesHelper.TASKTABLE_INSPECTORS_TODO, true)) {
      inspectors.add(ToDoInspector.getInstance());
    }
    if (PropertiesHelper.getBoolean(settings, PropertiesHelper.TASKTABLE_INSPECTORS_MOVEMENT, true)) {
      inspectors.add(MovementInspector.getInstance());
    }
    if (PropertiesHelper.getBoolean(settings, PropertiesHelper.TASKTABLE_INSPECTORS_SHIP, true)) {
      inspectors.add(ShipInspector.getInstance());
    }
    if (PropertiesHelper.getBoolean(settings, PropertiesHelper.TASKTABLE_INSPECTORS_ORDER_SYNTAX,
        true)) {
      inspectors.add(OrderSyntaxInspector.getInstance());
    }
  }

  private List<Inspector> getInspectors() {
    return inspectors;
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
    if (this.dispatcher != null) {
      this.dispatcher.removeUnitOrdersListener(this);
      this.dispatcher.removeGameDataListener(this);
      this.dispatcher.removeSelectionListener(this);
    }
  }

  private void registerListeners() {
    if (this.dispatcher != null) {
      this.dispatcher.addUnitOrdersListener(this);
      this.dispatcher.addGameDataListener(this);
      this.dispatcher.addSelectionListener(this);
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
    // do nothing if Panel is hidden
    if (!this.isShown()) {
      return;
    }
    // rebuild warning list
    refreshProblems();
  }

  /**
   * @see SelectionListener#selectionChanged(com.eressea.event.SelectionEvent)
   */
  public void selectionChanged(SelectionEvent e) {
    if ((e.getSource() == this)) {
      return;
    }

    // do nothing if Panel is hidden
    if (!this.isShown()) {
      return;
    }

    synchronized (lastSelection) {
      // if selection has changed, refresh delta
      if (e.getSelectionType() == SelectionEvent.ST_REGIONS && e.getSelectedObjects() != null) {
        // no selection: refresh all
        if (e.getSelectedObjects().isEmpty()) {
          lastSelection = data.regions().values();
          refreshProblems();
        } else {
          Collection<Region> newSelection = new HashSet<Region>();
          Collection<Region> addSelection = new HashSet<Region>();
          Collection<Region> delSelection = new HashSet<Region>();

          // remove problems of regions that are not selected any more
          for (Iterator<Region> it = lastSelection.iterator(); it.hasNext();) {
            Object o = it.next();
            if (o instanceof Region) {
              if (!e.getSelectedObjects().contains(o)) {
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
            } else
              updateDispatcher.addRegions(addSelection);
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
      if (u != null)
        r = u.getRegion();
    }
    if (e.getActiveObject() instanceof HasRegion) {
      r = ((HasRegion) e.getActiveObject()).getRegion();
    }
    if (e.getActiveObject() instanceof Region) {
      r = (Region) e.getActiveObject();
    }

    synchronized (this) {
      if (restrictToActiveRegion() && r != null && r != lastActiveRegion) {
        lastActiveRegion = r;
        refreshProblems();
      }
      lastActiveRegion = r;
    }

  }

  private void clearProblems() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        model.clearProblems();
      }
    });
  }

  /**
   * Updates reviews when orders have changed.
   * 
   * @param e
   * @see magellan.client.event.UnitOrdersListener#unitOrdersChanged(magellan.client.event.UnitOrdersEvent)
   */
  public void unitOrdersChanged(UnitOrdersEvent e) {
    updateDispatcher.removeRegion(e.getUnit().getRegion());
    updateDispatcher.addRegion(e.getUnit().getRegion());
  }

  /**
   * Reviews a region with all units within.
   */
  private void reviewRegionAndUnits(Region r) {
    if (r == null) {
      return;
    }

    if (TaskTablePanel.log.isDebugEnabled()) {
      TaskTablePanel.log.debug("TaskTablePanel.reviewRegionAndUnits(" + r + ") called");
    }

    reviewRegion(r);

    if (r.units() == null) {
      return;
    }

    for (Iterator iter = r.units().iterator(); iter.hasNext();) {
      Unit u = (Unit) iter.next();
      reviewUnit(u);
    }

  }

  /**
   * Reviews a region with all units within.
   */
  private void unReviewRegionAndUnits(Region r) {
    if (r == null) {
      return;
    }

    if (TaskTablePanel.log.isDebugEnabled()) {
      TaskTablePanel.log.debug("TaskTablePanel.reviewRegionAndUnits(" + r + ") called");
    }

    removeRegion(r);

    if (r.units() == null) {
      return;
    }

    for (Iterator iter = r.units().iterator(); iter.hasNext();) {
      Unit u = (Unit) iter.next();
      unReviewUnit(u);
    }
  }

  private void removeRegion(final Region r) {
    for (Iterator iter = getInspectors().iterator(); iter.hasNext();) {
      final Inspector c = (Inspector) iter.next();
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          // remove problems in the AWT event dispatching thread to avoid
          // synchronization issues!
          model.removeProblems(c, r);
        }
      });
    }
  }

  private void unReviewUnit(final Unit u) {
    for (Iterator iter = getInspectors().iterator(); iter.hasNext();) {
      final Inspector c = (Inspector) iter.next();
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          // remove problems in the AWT event dispatching thread to avoid
          // synchronization issues!
          model.removeProblems(c, u);
        }
      });
    }
  }

  /**
   * Reviews a the specified region and the specified unit.
   */
  private void reviewRegion(Region r) {
    for (Iterator iter = getInspectors().iterator(); iter.hasNext();) {
      Inspector c = (Inspector) iter.next();
      if (r != null) {
        final List<Problem> problems = c.reviewRegion(r);
        // add problems in the AWT event dispatching thread to avoid
        // synchronization issues!
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            model.addProblems(problems);
          }
        });
      }
    }
  }

  /**
   * Reviews a the specified region and the specified unit.
   */
  private void reviewUnit(Unit u) {
    if (!isValidUnitByFaction(u)){return;}
    for (Iterator iter = getInspectors().iterator(); iter.hasNext();) {
      Inspector c = (Inspector) iter.next();
      if (u != null) {
        final List<Problem> problems = c.reviewUnit(u);
        // add problems in the AWT event dispatching thread to avoid
        // synchronization issues!
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            model.addProblems(problems);
          }
        });
      }
    }
  }

  /**
   * checks, if the specified unit is valid according to the settings
   * restrictToOwner and restrictToPassword
   * @param u
   * @return
   */
  private boolean isValidUnitByFaction(Unit u){
    
    if (this.restrictToOwner() && (data.getOwnerFaction()==null || u.getFaction()==null || !data.getOwnerFaction().equals(u.getFaction().getID()))){
      return false;
    }
    if (this.restrictToPassword() && (u.getFaction()==null || u.getFaction().getPassword()==null || u.getFaction().getPassword().length()==0)){
      return false;
    }
    
    return true;
  }

  public boolean restrictToOwner() {
    return PropertiesHelper
        .getBoolean(settings, PropertiesHelper.TASKTABLE_RESTRICT_TO_OWNER, true);
  }

  public boolean restrictToPassword() {
    return PropertiesHelper.getBoolean(settings, PropertiesHelper.TASKTABLE_RESTRICT_TO_PASSWORD,
        true);
  }

  public void setRestrictToOwner(boolean value) {
    settings.put(PropertiesHelper.TASKTABLE_RESTRICT_TO_OWNER, String.valueOf(value));
  }

  public void setRestrictToPassword(boolean value) {
    settings.put(PropertiesHelper.TASKTABLE_RESTRICT_TO_PASSWORD, String.valueOf(value));
  }

  public boolean restrictToSelection() {
    return PropertiesHelper.getBoolean(settings, PropertiesHelper.TASKTABLE_RESTRICT_TO_SELECTION,
        false);
  }

  public boolean restrictToActiveRegion() {
    return PropertiesHelper.getBoolean(settings,
        PropertiesHelper.TASKTABLE_RESTRICT_TO_ACTIVEREGION, false);
  }

  public void setRestrictToSelection(boolean value) {
    settings.put(PropertiesHelper.TASKTABLE_RESTRICT_TO_SELECTION, String.valueOf(value));
    selectionLabel.setEnabled(value);
  }

  public void setRestrictToActiveRegion(boolean value) {
    settings.put(PropertiesHelper.TASKTABLE_RESTRICT_TO_ACTIVEREGION, String.valueOf(value));
    activeRegionLabel.setEnabled(value);
  }

  private Vector<String> getHeaderTitles() {
    if (headerTitles == null) {
      headerTitles = new Vector<String>(7);
      headerTitles.add(Resources.get("tasks.tasktablepanel.header.type"));
      headerTitles.add(Resources.get("tasks.tasktablepanel.header.description"));
      headerTitles.add(Resources.get("tasks.tasktablepanel.header.object"));
      headerTitles.add(Resources.get("tasks.tasktablepanel.header.region"));
      headerTitles.add(Resources.get("tasks.tasktablepanel.header.faction"));
      headerTitles.add(Resources.get("tasks.tasktablepanel.header.line"));
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
    public TaskTableModel(Vector header) {
      super(header, 0);
      init();
    }

    private void init() {
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
     * Adds a list of problems one by one. Should be called in the AWT event
     * dispatch thread!
     * 
     * @param p
     */
    public void addProblems(List<Problem> problems) {
      for (Problem p : problems) {
        addProblem(p);
      }
    }

    protected static final int IMAGE_POS = 0;
    protected static final int PROBLEM_POS = 1;
    protected static final int OBJECT_POS = 2;
    // private static final int REGION_POS = 3;
    // private static final int FACTION_POS = 4;
    protected static final int LINE_POS = 5;
    protected static final int NUMBEROF_POS = 6;

    /**
     * Add a problem to this model. Should be called in the AWT event dispatch
     * thread!
     * 
     * @param p
     */
    public void addProblem(final Problem p) {
      HasRegion hasR = p.getObject();
      Faction faction = p.getFaction();

      Object[] o = new Object[TaskTableModel.NUMBEROF_POS + 1];
      int i = 0;
      o[i++] = Resources.get("tasks.tasktablepanel.problemtype_" + Integer.toString(p.getType()));
      o[i++] = p;
      o[i++] = hasR;
      o[i++] = hasR.getRegion();
      o[i++] = faction == null ? "" : faction;
      o[i++] = (p.getLine() < 1) ? "" : Integer.toString(p.getLine());
      o[i++] = null;
      addRow(o);
    }

    /**
     * Remove all problems of the given inspector <i>and</i> the given source.
     * Should be called in the AWT event dispatch thread!
     * 
     * @param inspector
     * @param source
     */
    public void removeProblems(final Inspector inspector, final Object source) {
      Vector dataVector = getDataVector();

      for (int i = getRowCount() - 1; i >= 0; i--) {
        if (i >= dataVector.size()) {
          TaskTablePanel.log.warn("TaskTablePanel: synchronization problem");
          break;
        }
        Vector v = (Vector) dataVector.get(i);
        Problem p = (Problem) v.get(TaskTableModel.PROBLEM_POS);

        // Inspector and region: only non unit objects will be removed
        if (p.getInspector().equals(inspector) && p.getSource().equals(source)) {
          removeRow(i);
        }
      }
    }
  }

  /**
   * Should return all short cuts this class want to be informed. The elements
   * should be of type javax.swing.KeyStroke
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
      DesktopEnvironment.requestFocus(TaskTablePanel.IDENTIFIER);
      break;
    }
  }

  /**
   * @see magellan.client.desktop.ShortcutListener#getShortcutDescription(java.lang.Object)
   */
  public String getShortcutDescription(Object stroke) {
    int index = shortcuts.indexOf(stroke);

    return Resources.get("tasks.shortcut.description." + String.valueOf(index));
  }

  /**
   * @see magellan.client.desktop.ShortcutListener#getListenerDescription()
   */
  public String getListenerDescription() {
    return Resources.get("tasks.shortcut.title");
  }

  /**
   * @see magellan.client.swing.preferences.PreferencesFactory#createPreferencesAdapter()
   */
  public PreferencesAdapter createPreferencesAdapter() {
    return new TaskTablePreferences(this, settings, data);
  }

  @Override
  public void paint(Graphics g) {
    if (!this.isShown()) {
      // Panel was deactivated earlier (or never opened)
      // and new we have a paint command...
      // need to rebuild the problems
      this.setShown(true);
      // log.info("TaskTablePanel shown after hide! -> refreshing.");
      this.refreshProblems();
    }
    super.paint(g);
  }

  /**
   * Sets the value of isShown.
   * 
   * @param isShown
   *          The value for isShown.
   */
  private void setShown(boolean isShown) {
    this.shown = isShown;
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
          if (menu.getActionCommand().equals("menu." + TaskTablePanel.IDENTIFIER)) {
            menuSelected = menu.isSelected();
          }
        }
      }
    }
    if (!menuSelected) {
      // Our Panel is not selected -> need no refresh
      this.setShown(false);
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

  /**
   * @see net.infonode.docking.DockingWindowListener#viewFocusChanged(net.infonode.docking.View,
   *      net.infonode.docking.View)
   */
  public void viewFocusChanged(View arg0, View arg1) {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowAdded(net.infonode.docking.DockingWindow,
   *      net.infonode.docking.DockingWindow)
   */
  public void windowAdded(DockingWindow arg0, DockingWindow arg1) {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowClosed(net.infonode.docking.DockingWindow)
   */
  public void windowClosed(DockingWindow arg0) {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowClosing(net.infonode.docking.DockingWindow)
   */
  public void windowClosing(DockingWindow arg0) throws OperationAbortedException {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowDocked(net.infonode.docking.DockingWindow)
   */
  public void windowDocked(DockingWindow arg0) {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowDocking(net.infonode.docking.DockingWindow)
   */
  public void windowDocking(DockingWindow arg0) throws OperationAbortedException {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowHidden(net.infonode.docking.DockingWindow)
   */
  public void windowHidden(DockingWindow arg0) {
    this.setShown(false);
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowMaximized(net.infonode.docking.DockingWindow)
   */
  public void windowMaximized(DockingWindow arg0) {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowMaximizing(net.infonode.docking.DockingWindow)
   */
  public void windowMaximizing(DockingWindow arg0) throws OperationAbortedException {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowMinimized(net.infonode.docking.DockingWindow)
   */
  public void windowMinimized(DockingWindow arg0) {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowMinimizing(net.infonode.docking.DockingWindow)
   */
  public void windowMinimizing(DockingWindow arg0) throws OperationAbortedException {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowRemoved(net.infonode.docking.DockingWindow,
   *      net.infonode.docking.DockingWindow)
   */
  public void windowRemoved(DockingWindow arg0, DockingWindow arg1) {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowRestored(net.infonode.docking.DockingWindow)
   */
  public void windowRestored(DockingWindow arg0) {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowRestoring(net.infonode.docking.DockingWindow)
   */
  public void windowRestoring(DockingWindow arg0) throws OperationAbortedException {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowShown(net.infonode.docking.DockingWindow)
   */
  public void windowShown(DockingWindow arg0) {
    this.setShown(true);
    this.refreshProblems();
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowUndocked(net.infonode.docking.DockingWindow)
   */
  public void windowUndocked(DockingWindow arg0) {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowUndocking(net.infonode.docking.DockingWindow)
   */
  public void windowUndocking(DockingWindow arg0) throws OperationAbortedException {
  }

}
