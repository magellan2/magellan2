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
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
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
																		  SelectionListener, ShortcutListener, GameDataListener, 
																		  PreferencesFactory, DockingWindowListener
{
	private static final Logger log = Logger.getInstance(TaskTablePanel.class);

  public static final String IDENTIFIER = "TASKS";

	protected JTable table;
	TableSorter sorter;
	TaskTableModel model;
	protected List<Inspector> inspectors;
  
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

  // shortcuts
  private List<KeyStroke> shortcuts;

	/**
	 * Creates a new TaskTablePanel object.
	 *
	 * @param d 
	 * @param initData 
	 * @param p 
	 */
	public TaskTablePanel(EventDispatcher d, GameData initData, Properties p) {
		super(d, initData, p);
		init(d);
	}

	private void init(EventDispatcher d) {
		d.addUnitOrdersListener(this);
		d.addGameDataListener(this);

		// TODO (stm): this is broken, so for now we don't care about selection
    //		d.addSelectionListener(this);

		initGUI();
		refreshProblems();
	}

	private void initGUI() {
		model = new TaskTableModel(getHeaderTitles());
		sorter = new TableSorter(model);
		table = new JTable(sorter);
//		sorter.addMouseListenerToHeaderInTable(table); // OLD
		sorter.setTableHeader(table.getTableHeader());   //NEW
		
		
		// allow reordering of headers
		table.getTableHeader().setReorderingAllowed(true);

		// set row selection to single selection
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// Row 0 ("I"): smallest possible, not resizeable
		table.getColumnModel().getColumn(TaskTableModel.IMAGE_POS).setResizable(false);
		table.getColumnModel().getColumn(TaskTableModel.IMAGE_POS).setMaxWidth(table.getColumnModel()
																					.getColumn(TaskTableModel.IMAGE_POS)
																					.getMinWidth()*3);

//		// Row 1 ("!"): smallest possible, not resizeable
//		table.getColumnModel().getColumn(TaskTableModel.UNKNOWN_POS).setResizable(false);
//		table.getColumnModel().getColumn(TaskTableModel.UNKNOWN_POS).setMaxWidth(table.getColumnModel()
//																					  .getColumn(TaskTableModel.UNKNOWN_POS)
//																					  .getMinWidth()*3);

		// Row 2 ("Line"): smallest possible, not resizeable
		table.getColumnModel().getColumn(TaskTableModel.LINE_POS).setResizable(true);
		table.getColumnModel().getColumn(TaskTableModel.LINE_POS).setMaxWidth(table.getColumnModel()
																				   .getColumn(TaskTableModel.LINE_POS)
																				   .getMinWidth()*2);
		// react on double clicks on a row
		table.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					if(e.getClickCount() == 2) {
						JTable target = (JTable) e.getSource();
						int row = target.getSelectedRow();

						if(log.isDebugEnabled()) {
							log.debug("TaskTablePanel: Double click on row " + row);
						}
						selectObjectOnRow(row);
					}
				}
			});

		// layout component
		this.setLayout(new BorderLayout());
		this.add(new JScrollPane(table), BorderLayout.CENTER);
    
    // initialize shortcuts
    shortcuts = new ArrayList<KeyStroke>(1);

    // 0: Focus
    shortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_MASK));

    // register for shortcuts
    DesktopEnvironment.registerShortcutListener(this);

	}

	private void selectObjectOnRow(int row) {
		Object obj = sorter.getValueAt(row, TaskTableModel.OBJECT_POS);
		dispatcher.fire(new SelectionEvent<Object>(this, null, obj));
	}

	private static final int RECALL_IN_MS = 10;
	// TODO make this configurable
	private static final boolean REGIONS_WITH_UNCONFIRMED_UNITS_ONLY = false;

//	private Timer timer;
//	private Iterator regionsIterator;

  /**
   * counts Threads - to identify different Threads
   * (not needed anymore)
   */
  protected static int threadRunning=0;
  
  /**
   * The Thread which refreshes the List of Problems
   * only one is allowed
   */
  protected static Thread refreshThread = null;
  
	public void refreshProblems() {
    if (refreshThread!=null && refreshThread.isAlive()){
      // do not refresh if another refreshThread is still running
      // log.info("new call to refreshProblems rejected, thread still running");
      return;
    }
    
    // Fiete: check, if open Problems is open anyway
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
      // log.info("call to refreshProblems rejected  (not visisble Panel)");
      return;
    }
    
    initInspectors();
	  
	  Window w = SwingUtilities.getWindowAncestor(this);
	  final ProgressBarUI ui = new ProgressBarUI((JFrame) (w instanceof JFrame?w:null));
	  ui.setMaximum(data.regions().size());
	  ui.setTitle(Resources.get("dock.TASKS.progressBar.title"));
	  // creating new Thread
    refreshThread = new Thread(new Runnable() {
	    final int myThread = ++threadRunning;
	      
      public void run() {
        try {
          int iProgress=0;
          // log.info("started refresh Problems "+myThread);
          ui.show();
          synchronized (model) {
            model.clearProblems();
          }
          for (Region r  : data.regions().values()){
            if (threadRunning>myThread)
              break;
            ui.setProgress(r.getName(), ++iProgress);
            r.refreshUnitRelations();
            reviewRegionAndUnits(r);
          }
          if (threadRunning>myThread){
            ui.setMaximum(1);
            ui.setProgress("aborted", 1);
            // log.info("aborted "+myThread);
          }
          // log.info("finished refreshed Problems "+myThread);
        }finally{
          try {
            ui.ready();
          } catch (Exception e){
            e.printStackTrace();
          }
        }
      }  
    });
    // starting the thread
    refreshThread.start();
    this.needRefresh=false;
	}

	

	private void initInspectors() {
		inspectors = new ArrayList<Inspector>();
		
		if (PropertiesHelper.getBoolean(settings, PropertiesHelper.TASKTABLE_INSPECTORS_ATTACK, true))
		  inspectors.add(AttackInspector.getInstance());
		if (PropertiesHelper.getBoolean(settings, PropertiesHelper.TASKTABLE_INSPECTORS_TODO, true))
		  inspectors.add(ToDoInspector.getInstance());
		if (PropertiesHelper.getBoolean(settings, PropertiesHelper.TASKTABLE_INSPECTORS_MOVEMENT, true))
		  inspectors.add(MovementInspector.getInstance());
		if (PropertiesHelper.getBoolean(settings, PropertiesHelper.TASKTABLE_INSPECTORS_SHIP, true))
		  inspectors.add(ShipInspector.getInstance());
		if (PropertiesHelper.getBoolean(settings, PropertiesHelper.TASKTABLE_INSPECTORS_ORDER_SYNTAX, true))
		  inspectors.add(OrderSyntaxInspector.getInstance());
	}

	/**
	 * clean up
	 */
	public void quit() {
		if(this.dispatcher != null) {
			dispatcher.removeUnitOrdersListener(this);
			dispatcher.removeSelectionListener(this);
		}

		super.quit();
	}

	/**
	 * @see magellan.client.swing.InternationalizedDataPanel#gameDataChanged(magellan.library.event.GameDataEvent)
	 */
	public void gameDataChanged(GameDataEvent e) {
		super.gameDataChanged(e);
		// rebuild warning list
		this.needRefresh=true;
		refreshProblems();
	}

	/**
	 * @see SelectionListener#selectionChanged(com.eressea.event.SelectionEvent)
	 */
	public void selectionChanged(SelectionEvent e) {
		// (stm) this is pretty broken
		if((e.getSource() == this) || (e.getSelectionType() == SelectionEvent.ST_REGIONS)) {
			// ignore multiple region selections
			return;
		}
		
		// Fiete: do nothing if Panel is hidden 
		this.needRefresh=true;
		if (!this.isShown){
		  return;
		}
		
		Unit u = null;

		try {
			u = (Unit) e.getActiveObject();
		} catch(ClassCastException cce) {
		}

		Region r = null;

		try {
			r = ((HasRegion) e.getActiveObject()).getRegion();
		} catch(ClassCastException cce) {
		}

		if(r == null) {
			try {
				r = (Region) e.getActiveObject();
			} catch(ClassCastException cce) {
			}
		}

		reviewRegionAndUnits(r);
		reviewObjects(u, null);
	}

	/**
	 * Updates reviews when orders have changed.
	 *
	 * @param e 
	 * @see magellan.client.event.UnitOrdersListener#unitOrdersChanged(magellan.client.event.UnitOrdersEvent)
	 */
	public void unitOrdersChanged(UnitOrdersEvent e) {
	  // Fiete: do nothing if Panel is hidden
	  this.needRefresh=true;
	  if (!this.isShown){
	    
	    return;
	  }
		// rebuild warning list for given unit
    // this is not enough, also other units can be affected
		// reviewObjects(e.getUnit(), e.getUnit().getRegion());
    // try to recalc the region again
    reviewRegionAndUnits(e.getUnit().getRegion());
	}

	/**
	 * 
	 */
	private void reviewRegionAndUnits(Region r) {
		if(r == null) {
			return;
		}

		if(log.isDebugEnabled()) {
			log.debug("TaskTablePanel.reviewRegionAndUnits(" + r + ") called");
		}

		reviewObjects(null, r);

		if(r.units() == null) {
			return;
		}

		for(Iterator iter = r.units().iterator(); iter.hasNext();) {
			Unit u = (Unit) iter.next();
			reviewObjects(u, null);
		}
	}

	/**
	 * 
	 */
	private void reviewObjects(Unit u, Region r) {
	  synchronized (model) {
      for (Iterator iter = inspectors.iterator(); iter.hasNext();) {
        Inspector c = (Inspector) iter.next();
        if (r != null) {
          // remove previous problems of this unit AND the given inspector
          model.removeProblems(c, r);

          // add new problems if found
          List<Problem> problems = c.reviewRegion(r);
          
          model.addProblems(filterProblems(problems));
        }

        if (u != null) {
          // remove previous problems of this unit AND the given inspector
          model.removeProblems(c, u);

          // add new problems if found
          if (data.getOwnerFaction()==null || !restrictToOwner() || data.getOwnerFaction().equals(u.getFaction().getID())){
            List<Problem> problems = c.reviewUnit(u);
            model.addProblems(problems);
          }
        }
      }
    }
	}

	/**
	 * 
	 */
	private List<Problem> filterProblems(List<Problem> problems) {
	  if (!restrictToOwner())
	    return problems;
	  
     List<Problem> filteredList = new ArrayList<Problem>(problems.size());
     for (Problem p: problems){
       Faction f = p.getFaction();
       if (data.getOwnerFaction()==null || !restrictToOwner() || f==null || data.getOwnerFaction().equals(f.getID())){
         filteredList.add(p);
       }
     }
     return filteredList;
  }

  private boolean restrictToOwner() {
    return PropertiesHelper.getBoolean(settings, PropertiesHelper.TASKTABLE_RESTRICT_TO_OWNER, true);
  }

  private Vector<String> getHeaderTitles() {
		Vector<String> v = new Vector<String>(7);
		v.add(Resources.get("tasks.tasktablepanel.header.type"));
		v.add(Resources.get("tasks.tasktablepanel.header.description"));
		v.add(Resources.get("tasks.tasktablepanel.header.object"));
		v.add(Resources.get("tasks.tasktablepanel.header.region"));
		v.add(Resources.get("tasks.tasktablepanel.header.faction"));
		v.add(Resources.get("tasks.tasktablepanel.header.line"));
//		v.add(Resources.get("tasks.tasktablepanel.header.unknown"));

		return v;
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
		public boolean isCellEditable(int row, int column) {
			return false;
		}

		// TODO : find better solution!
		public void clearProblems() {
			for(int i = getRowCount() - 1; i >= 0; i--) {
				removeRow(i);
			}
		}

		/**
		 * Adds a list of problems one by one.
		 *
		 * @param p 
		 */
		public void addProblems(List<Problem> problems) {
			for(Problem p: problems) {
				addProblem(p);
			}
		}

		private static final int IMAGE_POS = 0;
		private static final int PROBLEM_POS = 1;
		private static final int OBJECT_POS = 2;
		private static final int REGION_POS = 3;
		private static final int FACTION_POS = 4;
		private static final int LINE_POS = 5;
		private static final int NUMBEROF_POS = 6;

		/**
		 * Add a problem to this model.
		 *
		 * @param p 
		 */
		public void addProblem(Problem p) {
      HasRegion hasR = p.getObject();
      Faction faction = p.getFaction();

      Object[] o = new Object[NUMBEROF_POS+1];
      int i=0;
      o[i++] = Integer.toString(p.getType());
      o[i++] = p;
      o[i++] = hasR;
      o[i++] = hasR.getRegion();
      o[i++] = faction==null?"":faction;
      o[i++] = (p.getLine() < 1) ? "" : Integer.toString(p.getLine());
      o[i++] = null;
      this.addRow(o);
      
		}

    // TODO : find better solution!
		public void removeProblems(Inspector inspector, Object source) {
			Vector dataVector = getDataVector();

			for(int i = getRowCount() - 1; i >= 0; i--) {
        if (i>=dataVector.size()){
          log.warn("TaskTablePanel: synchronization problem");
          break;
        }
				Vector v = (Vector) dataVector.get(i);
				Problem p = (Problem) v.get(PROBLEM_POS);

				// Inspector and region: only non unit objects will be removed
				if(p.getInspector().equals(inspector) && p.getSource().equals(source)) {
					removeRow(i);
				}
			}
		}
	}
  
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
      DesktopEnvironment.requestFocus(IDENTIFIER);
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
  
  public void paint(Graphics g){
     if (!this.isShown){
       // Panel was deactivated eralier (or never opened)
       // and new we have a paint command...
       // need to rebuild the prolems
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
