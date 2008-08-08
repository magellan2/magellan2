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
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
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
import magellan.client.swing.tree.ContextManager;
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
																		  SelectionListener, ShortcutListener, GameDataListener, 
																		  PreferencesFactory, DockingWindowListener, MouseListener
{
	private static final Logger log = Logger.getInstance(TaskTablePanel.class);

  public static final String IDENTIFIER = "TASKS";

  private JTable table;
	private TableSorter sorter;
	private TaskTableModel model;
	
	private List<Inspector> inspectors;
  
  /**
   * Not easy to detect if this panel is shown / visible
   * switched OFF: if refreshProblems is detecting, that the JMenu
   *    entry in MagellanDeskTop.desktopMenu is not selected
   * switched ON: if paint(Graphics g) is detected and isShown=false
   *    in that case refreshProblems is called too.
   * adjusted by Events from DockingWindowFramework   
   */
  private boolean isShown = true; 
  
  /**
   * Indicator of a refresh is needed if Panel becomes 
   * visible again
   */
  private boolean needRefresh = true;

  /** list of registered keyboard shortcuts */
  private List<KeyStroke> shortcuts;

  private Collection<Region> lastSelection = Collections.emptyList();

  private Region lastActiveRegion;

  private Vector<String> headerTitles;

  private boolean secondThreadInvoked = false;

  protected static int threadCounter = 0;


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
		refreshProblems();
		
	}

	private void initGUI() {
		model = new TaskTableModel(getHeaderTitles());
		sorter = new TableSorter(model);
		table = new JTable(sorter);

		sorter.setTableHeader(table.getTableHeader());   //NEW
		
		// allow reordering of headers
		table.getTableHeader().setReorderingAllowed(true);

		// set row selection to single selection
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

//		// Col 0 ("I"): smallest possible, not resizeable
//		table.getColumnModel().getColumn(TaskTableModel.IMAGE_POS).setResizable(false);
//		table.getColumnModel().getColumn(TaskTableModel.IMAGE_POS).setMaxWidth(table.getColumnModel()
//																					.getColumn(TaskTableModel.IMAGE_POS)
//																					.getMinWidth()*3);
//
//		// Col 1 ("Line"): smallest possible, not resizeable
//		table.getColumnModel().getColumn(TaskTableModel.LINE_POS).setResizable(true);
//		table.getColumnModel().getColumn(TaskTableModel.LINE_POS).setMaxWidth(table.getColumnModel()
//																				   .getColumn(TaskTableModel.LINE_POS)
//																				   .getMinWidth()*2);
		
		// react on double clicks on a row
		table.addMouseListener(this);

		// layout component
		this.setLayout(new BorderLayout());
		this.add(new JScrollPane(table), BorderLayout.CENTER);
		
		this.setFocusable(false);
    
    // initialize shortcuts
    shortcuts = new ArrayList<KeyStroke>(1);

    // 0: Focus
    shortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK));

    // register for shortcuts
    DesktopEnvironment.registerShortcutListener(this);

	}

  /**
   * Create and show context menu.
   * 
   * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
   */
  public void mouseClicked(final MouseEvent e) {
    if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
      JTable target = (JTable) e.getSource();
      int row = target.getSelectedRow();

      if(TaskTablePanel.log.isDebugEnabled()) {
        TaskTablePanel.log.debug("TaskTablePanel: Double click on row " + row);
      }
      selectObjectOnRow(row);
    }

    if (e.getButton() == MouseEvent.BUTTON3) {
      JPopupMenu menu = new JPopupMenu();
      final int row = table.rowAtPoint(e.getPoint());
      int col = table.columnAtPoint(e.getPoint());
      
      Object o = sorter.getValueAt(row, TaskTableModel.OBJECT_POS);
      
      
      JMenuItem refreshMenu;
      JMenuItem acknowledgeMenu;
      JMenuItem unAcknowledgeMenu;
      
      refreshMenu = new JMenuItem(Resources.get("tasks.contextmenu.refresh.title"));
      acknowledgeMenu = new JMenuItem(Resources.get("tasks.contextmenu.acknowledge.title"));
      unAcknowledgeMenu = new JMenuItem(Resources.get("tasks.contextmenu.unacknowledge.title"));
      
      menu.add(refreshMenu);
      menu.add(acknowledgeMenu);
      menu.add(unAcknowledgeMenu);

      refreshMenu.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent menuEvent) {
           refreshProblems();
        }
      });
      
      
      acknowledgeMenu.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent menuEvent) {
          acknowledge(row);
        }
      });

      unAcknowledgeMenu.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent menuEvent) {
          unAcknowledge();
        }
      });
      
      if (menu != null) {
        ContextManager.showMenu(menu, this, e.getX(), e.getY());
      }

    }
  }

  public void mouseEntered(MouseEvent e) {
  }

  public void mouseExited(MouseEvent e) {
  }

  public void mousePressed(MouseEvent e) {
  }

  public void mouseReleased(MouseEvent e) {
  }

	protected void unAcknowledge() {
    Window w = SwingUtilities.getWindowAncestor(this);
    final ProgressBarUI progressUI = new ProgressBarUI((JFrame) (w instanceof JFrame?w:null));
    progressUI.setTitle(Resources.get("tasks.progressbar.unack.title"));

    progressUI.setMaximum(data.regions().size());

    new Thread(new Runnable() {
     public void run() {
       try {
         progressUI.show();
         int iProgress=0;
         for (Inspector i : getInspectors()){
           for (Region r : data.regions().values()){
             progressUI.setProgress(r.getName(), ++iProgress);
             for (Unit u : r.units()){
               i.unSuppress(u);
             }
           }
         }
         dispatcher.fire(new GameDataEvent(this, data));
       }finally{
         try {
           progressUI.ready();
         } catch (Exception e){
           e.printStackTrace();
         }
       }
     }

   }).start();

    
  }

  protected void acknowledge(int row) {
    Problem p = (Problem) sorter.getValueAt(row, TaskTableModel.PROBLEM_POS);
    
    Unit u = p.addSuppressComment();
    if (u!=null)
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

  
  /**
   * The Thread which refreshes the List of Problems
   * only one is allowed
   */
  protected static Thread refreshThread = null;
  
	/**
	 * Rebuild the complete table.
	 */
	public void refreshProblems() {
	  synchronized (lastSelection) {
	    clearProblems();
	    if (restrictToSelection() && lastSelection!=null && !lastSelection.isEmpty()){
	      refreshProblems(lastSelection, Collections.<Region>emptyList());
	    } else {
	      refreshProblems(data.regions().values(), Collections.<Region>emptyList());
	    }
    }
	}

	/**
   * First remove problems from <code>regionsToRemove</code>. Then add
   * problems from <code>regionsToRemove</code> to model. These sets may be identical.
   * 
   * <p>The necessary work is done in a separate background thread. Multiple threads are supported.
   * 
   * @param regionsToAdd
   * @param regionsToRemove
   */
	protected void refreshProblems(final Collection<Region> regionsToAdd, final Collection<Region> regionsToRemove) {
	  // Fiete: do nothing if Panel is hidden
	  this.needRefresh=true;
	  if (!this.isShown){
	    return;
	  }

	  if (TaskTablePanel.log.isDebugEnabled()){
	    log.debug("refreshProblems called by: ", (new Exception()));
	  }

	  // if another refresh thread is running, first wait for a short while
	  if (TaskTablePanel.refreshThread!=null && TaskTablePanel.refreshThread.isAlive()){
	    try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
      }
	    // if still running, notify it of this new request via secondThreadInvoked
	    if (TaskTablePanel.refreshThread!=null && TaskTablePanel.refreshThread.isAlive()){
	      synchronized (TaskTablePanel.this) {
	        this.secondThreadInvoked =true;
	        log.debug("new call to refreshProblems rejected, thread still running");
	        return;
	      }
	    }
	  }
    
    // check, if TaskTablePanel is visible anyway
    MagellanDesktop MD = MagellanDesktop.getInstance();
    JMenu desktopMenu = MD.getDesktopMenu();
    boolean menuSelected = true;
    if (desktopMenu!=null){
      for (int index=0; index<desktopMenu.getItemCount(); index++) {
        if (desktopMenu.getItem(index) instanceof JCheckBoxMenuItem) {
          JCheckBoxMenuItem menu = (JCheckBoxMenuItem)desktopMenu.getItem(index);
          if (menu.getActionCommand().equals("menu."+ TaskTablePanel.IDENTIFIER)) {
            menuSelected = menu.isSelected();
          }
        }
      }
    }
    if (!menuSelected){
      // Our Panel is not selected -> need no refresh
      this.isShown=false;
    }
    
    if (!this.isShown || !this.needRefresh) {
      log.debug("call to refreshProblems rejected  (panel not visible), reason: "+this.isShown);
      return;
    }

    // start work in a background thread
    final Window w = SwingUtilities.getWindowAncestor(this);

    // creating new Thread
    TaskTablePanel.refreshThread = new Thread(new Runnable() {
	    boolean closeRequest = false;  
      final int id = ++threadCounter ;
	    
      public void run() {
        
        // this listener  asks for confirmation if the user attempts to close the progress dialog
        final ProgressBarUI.ClosingListener listener = new ProgressBarUI.ClosingListener() {
        
          public boolean proceed(WindowEvent e) {
            if (JOptionPane.showConfirmDialog(TaskTablePanel.this, Resources
                .get("tasks.runthread.abort.message"), Resources.get("tasks.runthread.abort.title", new  Object[] { id }),
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
              closeRequest=true;
              try {
                Thread.sleep(500);
              } catch (InterruptedException e1) {
              }
              return true;
            }else
              return false;
          }
          
        };
        
        // init progress bar
        final ProgressBarUI progressUI = new ProgressBarUI((JFrame) (w instanceof JFrame?w:null), false, listener);
        progressUI.setTitle(Resources.get("tasks.progressBar.refresh.title", new  Object[] { id }));
        progressUI.setMaximum(regionsToAdd.size()+regionsToRemove.size());

        try {
          int restarts = 0;
          boolean doRestart=true;
          // restart 10 times if necessary 
          while (doRestart && restarts<10){
            doRestart = false;
            int iProgress=0;
            log.debug("started refresh Problems "+id+" "+restarts+" times.");
            progressUI.show();

            // remove problems of deleted regions
            for (Region r : regionsToRemove) {
              // if a refresh was invoked a second time: signal restart
              if (secondThreadInvoked && restarts<8){
                log.debug("restarted "+id);
                doRestart = true;
                restarts++;
                break;
              }
              // user requested abort
              if (closeRequest){
                progressUI.setMaximum(1);
                progressUI.setProgress("aborted", 1);
                TaskTablePanel.this.needRefresh=true;
                log.debug("aborted "+id);
                break;
              }
              progressUI.setProgress(r.getName(), ++iProgress);
              r.refreshUnitRelations();
              unReviewRegionAndUnits(r);
            }

            // add problems of added regions
            for (Region r : regionsToAdd) {
              // if a refresh was invoked a second time: signal restart
              if (secondThreadInvoked  && restarts<8){
                log.debug("restarted "+id);
                doRestart = true;
                restarts++;
                break;
              }
              // user requestet abort
              if (closeRequest){
                progressUI.setMaximum(1);
                progressUI.setProgress("aborted", 1);
                TaskTablePanel.this.needRefresh=true;
                log.debug("aborted "+id);
                break;
              }

              progressUI.setProgress(r.getName(), ++iProgress);
              reviewRegionAndUnits(r);
            }
            // clean up
            synchronized (TaskTablePanel.this) {
              secondThreadInvoked=false;
            }
          }
        }finally{
          try {
            // clean up
            log.debug("exited refresh Problems "+id);
            secondThreadInvoked=false;
            progressUI.ready();
          } catch (Exception e){
            e.printStackTrace();
          }
        }
      }

    });
    // starting the thread
    TaskTablePanel.refreshThread.start();
    this.needRefresh=false;
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
		if (PropertiesHelper.getBoolean(settings, PropertiesHelper.TASKTABLE_INSPECTORS_ORDER_SYNTAX, true)) {
      inspectors.add(OrderSyntaxInspector.getInstance());
    }
	}

	 private List<Inspector> getInspectors() {
	    return inspectors;
	  }


	/**
	 * clean up
	 */
	@Override
  public void quit() {
	  removeListeners();
	  
		super.quit();
	}

	private void removeListeners() {
	  if(this.dispatcher != null) {
	    this.dispatcher.removeUnitOrdersListener(this);
	    this.dispatcher.removeGameDataListener(this);
	    this.dispatcher.removeSelectionListener(this);
	  }
	}

	private void registerListeners() {
	  if (this.dispatcher!=null){
	    this.dispatcher.addUnitOrdersListener(this);
	    this.dispatcher.addGameDataListener(this);
	    this.dispatcher.addSelectionListener(this);
	  }
	}

  /**
	 * @see magellan.client.swing.InternationalizedDataPanel#gameDataChanged(magellan.library.event.GameDataEvent)
	 */
	@Override
  public void gameDataChanged(GameDataEvent e) {
		super.gameDataChanged(e);
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
    if (!this.isShown) {
      return;
    }

    synchronized (lastSelection) {
      // if selection has changed, refresh delta
      if (e.getSelectionType() == SelectionEvent.ST_REGIONS) {
        // no selection: refresh all
        if (e.getSelectedObjects() == null || e.getSelectedObjects().isEmpty()) {
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
          if(restrictToSelection()){
            if (delSelection.size()>newSelection.size()*2){
              // if there are very many regions to delete, rather refresh all...
              refreshProblems();
            } else
              refreshProblems(addSelection, delSelection);
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

    if (restrictToActiveRegion() && r != null && r != lastActiveRegion) {
      clearProblems();
      refreshProblems(Collections.singleton(r), Collections.<Region> emptyList());
    }
    lastActiveRegion = r;

}

  private void clearProblems() {
	  model.clearProblems();
	}

  /**
	 * Updates reviews when orders have changed.
	 *
	 * @param e 
	 * @see magellan.client.event.UnitOrdersListener#unitOrdersChanged(magellan.client.event.UnitOrdersEvent)
	 */
	public void unitOrdersChanged(UnitOrdersEvent e) {
		// rebuild warning list for given unit
    // this is not enough, also other units can be affected
		// reviewObjects(e.getUnit(), e.getUnit().getRegion());
    // try to recalc the region again
	  refreshProblems(Collections.singleton(e.getUnit().getRegion()), Collections.singleton(e.getUnit().getRegion()));
	}

	/**
	 * Reviews a region with all units within.
	 */
	private void reviewRegionAndUnits(Region r) {
		if(r == null) {
			return;
		}

		if(TaskTablePanel.log.isDebugEnabled()) {
			TaskTablePanel.log.debug("TaskTablePanel.reviewRegionAndUnits(" + r + ") called");
		}

		reviewRegion(r);

		if(r.units() == null) {
			return;
		}

		for(Iterator iter = r.units().iterator(); iter.hasNext();) {
			Unit u = (Unit) iter.next();
			reviewUnit(u);
		}
		
	}

  /**
   * Reviews a region with all units within.
   */
  private void unReviewRegionAndUnits(Region r) {
    if(r == null) {
      return;
    }

    if(TaskTablePanel.log.isDebugEnabled()) {
      TaskTablePanel.log.debug("TaskTablePanel.reviewRegionAndUnits(" + r + ") called");
    }

    removeRegion(r);

    if(r.units() == null) {
      return;
    }

    for(Iterator iter = r.units().iterator(); iter.hasNext();) {
      Unit u = (Unit) iter.next();
      removeUnit(u);
    }
  }

	private void removeUnit(Unit u) {
    for (Iterator iter = getInspectors().iterator(); iter.hasNext();) {
      Inspector c = (Inspector) iter.next();
      model.removeProblems(c, u);
    }    
  }

  private void removeRegion(Region r) {
    for (Iterator iter = getInspectors().iterator(); iter.hasNext();) {
      Inspector c = (Inspector) iter.next();
      model.removeProblems(c, r);
    }    
  }

  /**
	 * Reviews a the specified region and the specified unit.
	 */
	private void reviewRegion(Region r) {
	  removeRegion(r);
	  for (Iterator iter = getInspectors().iterator(); iter.hasNext();) {
	    Inspector c = (Inspector) iter.next();
	    if (r != null) {
	      // add new problems if found
	      List<Problem> problems = c.reviewRegion(r);

	      model.addProblems(filterProblems(problems));
	    }
	  }
	}

	/**
	 * Reviews a the specified region and the specified unit.
	 */
	private void reviewUnit(Unit u) {
	  removeUnit(u);
	  for (Iterator iter = getInspectors().iterator(); iter.hasNext();) {
	    Inspector c = (Inspector) iter.next();
	    if (u != null) {
	      // add new problems if found
	      List<Problem> problems = c.reviewUnit(u);
	      model.addProblems(filterProblems(problems));
	    }
	  }
	}
	
  /**
   * Filter list of problems according to {@link #restrictToOwner()} and
   * {@link #restrictToPassword()}.
   */
	private List<Problem> filterProblems(List<Problem> problems) {
	  if (!restrictToOwner()) {
      return problems;
    }

	  // TODO(stm) low performance here?
	  List<Problem> filteredList = new ArrayList<Problem>(problems.size());
	  for (Problem p: problems){
	    Faction f = p.getFaction();
	    if (data.getOwnerFaction()==null || !restrictToOwner() || f==null || data.getOwnerFaction().equals(f.getID())){
	      filteredList.add(p);
	    } else {
	      if (restrictToPassword() && f!=null && f.getPassword()!=null && f.getPassword().length()>0){
	        filteredList.add(p);
	      }
	    }

	  }
	  return filteredList;
	}

  private boolean restrictToOwner() {
    return PropertiesHelper.getBoolean(settings, PropertiesHelper.TASKTABLE_RESTRICT_TO_OWNER, true);
  }
  
  private boolean restrictToPassword() {
    return PropertiesHelper.getBoolean(settings, PropertiesHelper.TASKTABLE_RESTRICT_TO_PASSWORD, true);
  }

  // TODO make configurable
  private boolean restrictToSelection() {
    return PropertiesHelper.getBoolean(settings, PropertiesHelper.TASKTABLE_RESTRICT_TO_SELECTION, false);
  }

  // TODO make configurable
  private boolean restrictToActiveRegion() {
    return PropertiesHelper.getBoolean(settings, PropertiesHelper.TASKTABLE_RESTRICT_TO_ACTIVEREGION, false);
  }


  private Vector<String> getHeaderTitles() {
    if (headerTitles==null){
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
		  synchronized (getLock()) {
		    for(int i = getRowCount() - 1; i >= 0; i--) {
		      removeRow(i);
		    }
      }
		}

		/**
		 * Adds a list of problems one by one.
		 *
		 * @param p 
		 */
		public void addProblems(List<Problem> problems) {
		  synchronized (getLock()) {
		    for(Problem p: problems) {
		      addProblem(p);
		    }
		  }
		}

		private Object getLock(){
		  return this;
		}
		
		protected static final int IMAGE_POS = 0;
		protected static final int PROBLEM_POS = 1;
		protected static final int OBJECT_POS = 2;
//		private static final int REGION_POS = 3;
//		private static final int FACTION_POS = 4;
		protected static final int LINE_POS = 5;
		protected static final int NUMBEROF_POS = 6;

		/**
		 * Add a problem to this model.
		 *
		 * @param p 
		 */
		public void addProblem(Problem p) {
		  synchronized (getLock()) {
		    HasRegion hasR = p.getObject();
		    Faction faction = p.getFaction();

		    Object[] o = new Object[TaskTableModel.NUMBEROF_POS+1];
		    int i=0;
	      o[i++] = Resources.get("tasks.tasktablepanel.problemtype_" +  Integer.toString(p.getType()));
		    o[i++] = p;
		    o[i++] = hasR;
		    o[i++] = hasR.getRegion();
		    o[i++] = faction==null?"":faction;
		    o[i++] = (p.getLine() < 1) ? "" : Integer.toString(p.getLine());
		    o[i++] = null;
		    this.addRow(o);
      }
		}

		/**
		 * Remove all problems of the given inspector <i>and</i> the given source.
		 * 
		 * @param inspector
		 * @param source
		 */
		public void removeProblems(Inspector inspector, Object source) {
		  synchronized (getLock()) {

		    Vector dataVector = getDataVector();

		    for(int i = getRowCount() - 1; i >= 0; i--) {
		      if (i>=dataVector.size()){
		        TaskTablePanel.log.warn("TaskTablePanel: synchronization problem");
		        break;
		      }
		      Vector v = (Vector) dataVector.get(i);
		      Problem p = (Problem) v.get(TaskTableModel.PROBLEM_POS);

		      // Inspector and region: only non unit objects will be removed
		      if(p.getInspector().equals(inspector) && p.getSource().equals(source)) {
		        removeRow(i);
		      }
		    }
		  }
		}
	}
  
  /**
   * Should return all short cuts this class want to be informed. The elements
   * should be of type javax.swing.KeyStroke
   * 
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
  public void paint(Graphics g){
     if (!this.isShown){
       // Panel was deactivated earlier (or never opened)
       // and new we have a paint command...
       // need to rebuild the problems
       this.isShown=true;
       // log.info("TaskTablePanel shown after hide! -> refreshing.");
       this.refreshProblems();
     }
     super.paint(g);
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#viewFocusChanged(net.infonode.docking.View, net.infonode.docking.View)
   */
  public void viewFocusChanged(View arg0, View arg1) {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowAdded(net.infonode.docking.DockingWindow, net.infonode.docking.DockingWindow)
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
    this.isShown=false;
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
   * @see net.infonode.docking.DockingWindowListener#windowRemoved(net.infonode.docking.DockingWindow, net.infonode.docking.DockingWindow)
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
    this.isShown=true;
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
