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
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import magellan.client.desktop.DesktopEnvironment;
import magellan.client.desktop.ShortcutListener;
import magellan.client.event.EventDispatcher;
import magellan.client.event.SelectionEvent;
import magellan.client.event.SelectionListener;
import magellan.client.event.UnitOrdersEvent;
import magellan.client.event.UnitOrdersListener;
import magellan.client.swing.InternationalizedDataPanel;
import magellan.client.swing.ProgressBarUI;
import magellan.client.swing.table.TableSorter;
import magellan.library.GameData;
import magellan.library.HasRegion;
import magellan.library.Region;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.event.GameDataEvent;
import magellan.library.event.GameDataListener;
import magellan.library.tasks.AttackInspector;
import magellan.library.tasks.Inspector;
import magellan.library.tasks.MovementInspector;
import magellan.library.tasks.OrderSyntaxInspector;
import magellan.library.tasks.Problem;
import magellan.library.tasks.ShipInspector;
import magellan.library.tasks.ToDoInspector;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;


/**
 * A panel for showing reviews about unit, region and/or gamedata.
 */
public class TaskTablePanel extends InternationalizedDataPanel implements UnitOrdersListener,
																		  SelectionListener, ShortcutListener, GameDataListener
{
	private static final Logger log = Logger.getInstance(TaskTablePanel.class);

  public static final String IDENTIFIER = "TASKS";

	protected JTable table;
	TableSorter sorter;
	TaskTableModel model;
	protected List<Inspector> inspectors;

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
		initInspectors();
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
		dispatcher.fire(new SelectionEvent(this, null, obj));
	}

	private static final int RECALL_IN_MS = 10;
	// TODO make this configurable
	private static final boolean REGIONS_WITH_UNCONFIRMED_UNITS_ONLY = false;

//	private Timer timer;
//	private Iterator regionsIterator;

  protected static int threadRunning=0;

	private void refreshProblems() {
	  
	  Window w = SwingUtilities.getWindowAncestor(this);
	  final ProgressBarUI ui = new ProgressBarUI((JFrame) (w instanceof JFrame?w:null));
	  ui.setMaximum(data.regions().size());

	  new Thread(new Runnable() {
	    final int myThread = ++threadRunning;
	      
      public void run() {
        try {
          int iProgress=0;
//        log.info("started "+myThread);
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
//          log.info("aborted "+myThread);
          }
        }finally{
          try {
            ui.ready();
          } catch (Exception e){
            e.printStackTrace();
          }
        }
      }
    
    }).start();

	  
//    if(timer != null) {
//      timer.cancel();
//      timer = null;
//    }
//		if(model != null) {
//	    synchronized (model) {
//	      model.clearProblems();
//	    }
//		}
//
//		if((data != null) && (data.regions() != null)) {
//			regionsIterator = data.regions().values().iterator();
//
//			if(timer == null) {
//				timer = new Timer(true);
//				timer.scheduleAtFixedRate(new TimerTask() {
//						public void run() {
//						  inspectNextRegion();
//						}
//					}, RECALL_IN_MS, RECALL_IN_MS);
//			}
//		} else {
//			regionsIterator = null;
//		}
	}

	private boolean inspectNextRegion() {
		Region r = getNextRegion();

		if(r != null) {
			r.refreshUnitRelations();
			reviewRegionAndUnits(r);
		}

		return r != null;
	}

	private Region getNextRegion() {
//		if(regionsIterator == null) {
//			return null;
//		}
//
//		// find next interesting region
//		while(regionsIterator.hasNext()) {
//			Region r = (Region) regionsIterator.next();
//
//			if((r.units() != null) && !r.units().isEmpty()) {
//				if(REGIONS_WITH_UNCONFIRMED_UNITS_ONLY) {
//					// only show regions with unconfirmed units
//					for(Iterator iter = r.units().iterator(); iter.hasNext();) {
//						Unit u = (Unit) iter.next();
//
//						if(!u.isOrdersConfirmed()) {
//							return r;
//						}
//					}
//				} else {
//					return r;
//				}
//			}
//		}

		return null;
	}

	private void initInspectors() {
		inspectors = new ArrayList<Inspector>();
		inspectors.add(ToDoInspector.getInstance());
		inspectors.add(MovementInspector.getInstance());
		inspectors.add(ShipInspector.getInstance());
    inspectors.add(AttackInspector.getInstance());
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
		refreshProblems();
	}

	/* (non-Javadoc)
	 * @see com.eressea.event.SelectionListener#selectionChanged(com.eressea.event.SelectionEvent)
	 */
	public void selectionChanged(SelectionEvent e) {
		// (stm) this is pretty broken
		if((e.getSource() == this) || (e.getSelectionType() == SelectionEvent.ST_REGIONS)) {
			// ignore multiple region selections
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
		// rebuild warning list for given unit
    // this is not enough, also other units can be affected
		// reviewObjects(e.getUnit(), e.getUnit().getRegion());
    // try to recalc the region again
    reviewRegionAndUnits(e.getUnit().getRegion());
	}

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

	private void reviewObjects(Unit u, Region r) {
	  synchronized (model) {
      for (Iterator iter = inspectors.iterator(); iter.hasNext();) {
        Inspector c = (Inspector) iter.next();
        if (r != null) {
          // remove previous problems of this unit AND the given inspector
          model.removeProblems(c, r);

          // add new problems if found
          List problems = c.reviewRegion(r);
          model.addProblems(problems);
        }

        if (u != null) {
          // remove previous problems of this unit AND the given inspector
          model.removeProblems(c, u);

          // add new problems if found
          List problems = c.reviewUnit(u);
          model.addProblems(problems);
        }
      }
    }
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

		/* (non-Javadoc)
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
		public void addProblems(List p) {
			for(Iterator iter = p.iterator(); iter.hasNext();) {
				addProblem((Problem) iter.next());
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
      Object[] o = new Object[NUMBEROF_POS+1];
      int i=0;
			o[i++] = Integer.toString(p.getType());
			o[i++] = p;
      HasRegion hasR = p.getObject();
      o[i++] = hasR;
      o[i++] = hasR.getRegion();
      if (hasR instanceof Unit)
      o[i++] = ((Unit) hasR).getFaction();
      else if(hasR instanceof UnitContainer){
        Unit owner = ((UnitContainer) hasR).getOwner();
        o[i++]=owner!=null?owner.getFaction():"";
      }else
        o[i++]="";
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
   * DOCUMENT-ME
   * 
   * 
   */
  public String getListenerDescription() {
    return Resources.get("tasks.shortcut.title");
  }

}
