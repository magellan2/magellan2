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

package magellan.client.swing.completion;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.JTextComponent;
import javax.swing.undo.UndoManager;

import magellan.client.EMapDetailsPanel;
import magellan.client.EMapOverviewPanel;
import magellan.client.desktop.DesktopEnvironment;
import magellan.client.event.EventDispatcher;
import magellan.client.event.OrderConfirmEvent;
import magellan.client.event.OrderConfirmListener;
import magellan.client.event.SelectionEvent;
import magellan.client.event.SelectionListener;
import magellan.client.event.TempUnitEvent;
import magellan.client.event.TempUnitListener;
import magellan.client.event.UnitOrdersEvent;
import magellan.client.swing.InternationalizedDataPanel;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Group;
import magellan.library.Island;
import magellan.library.Region;
import magellan.library.StringID;
import magellan.library.TempUnit;
import magellan.library.Unit;
import magellan.library.UnitID;
import magellan.library.event.GameDataEvent;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.rules.ItemType;
import magellan.library.utils.Cache;
import magellan.library.utils.CacheHandler;
import magellan.library.utils.Colors;
import magellan.library.utils.IDBaseConverter;
import magellan.library.utils.MagellanImages;
import magellan.library.utils.Resources;
import magellan.library.utils.guiwrapper.CacheableOrderEditor;
import magellan.library.utils.logging.Logger;


/**
 * DOCUMENT-ME
 *
 * @author $Author: $
 * @version $Revision: 269 $
 */
public class MultiEditorOrderEditorList extends InternationalizedDataPanel
	implements OrderEditorList, KeyListener, SelectionListener, TempUnitListener, FocusListener, MouseListener, 
			   CacheHandler
{
	private static final Logger log = Logger.getInstance(MultiEditorOrderEditorList.class);

  private boolean multiEditorLayout = false;
	private boolean hideButtons = false;
	private SortedSet<Unit> units;

  // currently selected entities
  private Island currentIsland = null;
  private Region currentRegion = null;
  private Faction currentFaction = null;
	private Unit currentUnit = null;
	
  // for remembering transitional selections
  private Region transitionalRegion=null;
  private Island transitionalIsland = null;
  private Faction transitionalFaction = null;

  private Color standardBgColor = null;
	private Color activeBgColor = null;
	private Color standardBgColorConfirmed = null;
	private Color activeBgColorConfirmed = null;
	private OrderEditor editor = null;
	private static final Border standardBorder = new LineBorder(Color.lightGray, 2);
	private static final Border activeBorder = new LineBorder(Color.darkGray, 2);
	private UpdateThread updateThread = new UpdateThread();
	private SwingGlitchThread swingGlitchThread = new SwingGlitchThread();

	// undo listener
	private UndoManager undoMgr = null;
	protected List<KeyListener> keyListeners = new LinkedList<KeyListener>();
	protected MEKeyAdapter keyAdapter;
	protected List<CaretListener> caretListeners = new LinkedList<CaretListener>();
	protected MECaretAdapter caretAdapter;
	protected List<FocusListener> focusListeners = new LinkedList<FocusListener>();
	protected MEFocusAdapter focusAdapter;

	// we have no longer any container like OrderEditingPanel
	protected ScrollPanel content; // a Panel implementing the scrollable interface

	// protected JPanel content; // a Panel implementing the scrollable interface
	protected JScrollPane scpContent = null;

	// the buttons for temp units etc.
	protected ButtonPanel buttons;

	// editor list generation mode
	protected int listMode = 1 << LIST_REGION;
	private static final int LIST_UNIT = 0;
	private static final int LIST_FACTION = 1;
	private static final int LIST_REGION = 2;
	private static final int LIST_ISLAND = 3;

	/**
	 * Creates a new MultiEditorOrderEditorList object.
	 */
	public MultiEditorOrderEditorList(EventDispatcher d, GameData initData, Properties settings,
									  UndoManager _undoMgr) {
		super(d, initData, settings);
    
		loadListProperty();

		d.addTempUnitListener(this);

		keyAdapter = new MEKeyAdapter(this);
		caretAdapter = new MECaretAdapter(this);
		focusAdapter = new MEFocusAdapter(this);

		undoMgr = _undoMgr;
		initGUI();
		initContent();
		//startTimer();
	}

	private void initGUI() {
		readSettings();
    
    units = new TreeSet<Unit>(EMapOverviewPanel.getUnitSorting(settings)); //new ArrayList<Unit>();
		content = new ScrollPanel();
    
		// content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		scpContent = new JScrollPane(content);

		// ClearLook suggests to remove the border
		scpContent.setBorder(null);

		buttons = new ButtonPanel();

		redrawPane();

		if(!multiEditorLayout) {
			editor = new OrderEditor(data, settings, undoMgr, dispatcher);
			editor.setCursor(new Cursor(Cursor.TEXT_CURSOR));
			addListeners(editor);
			content.add(editor);
		}

		// update the color of the editor when the order confirmation status changes
		dispatcher.addOrderConfirmListener(new OrderConfirmListener() {
				public void orderConfirmationChanged(OrderConfirmEvent e) {
					if(!this.equals(e.getSource())) {
						for(Iterator iter = e.getUnits().iterator(); iter.hasNext();) {
							Unit u = (Unit) iter.next();

							if(getEditor(u)!=null) {
								if(u.equals(currentUnit)) {
									// u is active unit
									if(u.isOrdersConfirmed()) {
                    getEditor(u).setBackground(activeBgColorConfirmed);
									} else {
                    getEditor(u).setBackground(activeBgColor);
									}
								} else {
									if(u.isOrdersConfirmed()) {
                    getEditor(u).setBackground(standardBgColorConfirmed);
									} else {
                    getEditor(u).setBackground(standardBgColor);
									}
								}
							}
						}
					}
				}
			});
	}

	/**
	 * @see magellan.client.swing.InternationalizedDataPanel#gameDataChanged(magellan.library.event.GameDataEvent)
	 */
	public void gameDataChanged(GameDataEvent e) {
		super.gameDataChanged(e);
    initContent();
	}

	/**
   * 
   */
  private void initContent() {
    clearUnits();
    currentIsland = null;
    currentRegion = null;
    currentFaction = null;
    currentUnit = null;
    
    transitionalRegion=null;
    transitionalIsland = null;
    transitionalFaction = null;

    // startTimer();
  }

  private void readSettings() {
		multiEditorLayout = Boolean.valueOf(settings.getProperty("OrderEditor.multiEditorLayout",
																 Boolean.TRUE.toString()))
								   .booleanValue();
		hideButtons = Boolean.valueOf(settings.getProperty("OrderEditor.hideButtons",
														   Boolean.FALSE.toString())).booleanValue();
		activeBgColor = Colors.decode(settings.getProperty("OrderEditor.activeBackgroundColor",
														   "255,255,255"));
		standardBgColor = Colors.decode(settings.getProperty("OrderEditor.standardBackgroundColor",
															 "228,228,228"));
		activeBgColorConfirmed = Colors.decode(settings.getProperty("OrderEditor.activeBackgroundColorConfirmed",
																	"255,255,102"));
		standardBgColorConfirmed = Colors.decode(settings.getProperty("OrderEditor.standardBackgroundColorConfirmed",
																	  "255,204,0"));
	}

	private boolean swingGlitch = false;



	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void paint(Graphics g) {
		super.paint(g);

		if(log.isDebugEnabled()) {
			log.debug("paint! [" + swingGlitch + "]");
		}

		// we are in a situation AFTER painting (hopefully!)
		if(swingGlitch) {
			swingGlitch = false;
			SwingUtilities.invokeLater(swingGlitchThread);
		}
	}

	/**
	 * Updates the editors after a new object has been selected. 
	 *
	 * 
	 */
	public void selectionChanged(SelectionEvent se) {
	  if (log.isDebugEnabled()) {
      log.debug("MultiEditorOrderEditorList.selectionChanged: " + se.getActiveObject());

      if (se.getActiveObject() != null) {
        log.debug("MultiEditorOrderEditorList.selectionChanged: " + se.getActiveObject().getClass());
      }

      log.debug("MultiEditorOrderEditorList.selectionChanged: " + (se.getSource() == this));
    }

    // remember if we want to have the focus (see below)
		boolean restoreFocus = (getEditor(currentUnit) != null && getEditor(currentUnit).hasFocus()) || content.hasFocus() || hasFocus();
		// if WE triggered the selection change, the new unit DOES get the focus
		restoreFocus = restoreFocus || (se.getSource() == this);

		if(multiEditorLayout) {
      deselectEditor(currentUnit);
      loadEditors(se.getActiveObject(), se.getPath());
      
		} else {
			// single editor mode
			Object activeObject = se.getActiveObject();

			if(activeObject instanceof Unit &&
				   EMapDetailsPanel.isPrivilegedAndNoSpy((Unit) activeObject)) {
				// update editor
        currentUnit = (Unit) activeObject;
        attachOrderEditor(currentUnit, editor);
        selectEditor(currentUnit, editor);
			} else {
        // no unit selected --> no editor active
				if(currentUnit != null) {
          setEditor(currentUnit, null);
          deselectEditor(editor);
          editor.setBorder(new TitledBorder(standardBorder,""));
					currentUnit = null;
					currentRegion = null;
          currentFaction=null;
          currentIsland=null;
					editor.setUnit(null);
					editor.setEditable(false);
				}
			}
		}

    // Restore focus. If we had the focus before, we want to ensure that the new current editor
    // gets the focus.
    if (((DesktopEnvironment.getMode() == DesktopEnvironment.FRAME) || restoreFocus)){
      requestFocus();
    }

    // update button state
    buttons.currentUnitChanged();
	}

	/**
   * Ensures that the correct editors are loaded and selected for the active object.
   * 
   * @param activeObject
   */
  private void loadEditors(Object activeObject, Collection path) {
    if (path!=null){
      for (Object o: path){
        if (o instanceof Island){
          transitionalIsland = (Island) o;
        }
        if (o instanceof Region){
          transitionalRegion = (Region) o;
          transitionalIsland = transitionalRegion.getIsland();
        }
        if (o instanceof Faction){
          transitionalFaction = (Faction) o;
        }
      }
    }
    Island newIsland = transitionalIsland;
    Region newRegion = transitionalRegion;
    Faction newFaction = transitionalFaction;
    Unit newUnit = null;
    if(activeObject != null) {
      if(activeObject instanceof Unit &&
           EMapDetailsPanel.isPrivilegedAndNoSpy((Unit) activeObject)) {
        newUnit = (Unit) activeObject;
        newRegion = newUnit.getRegion();
        newIsland = newRegion.getIsland();
        newFaction = newUnit.getFaction();
      } else if(activeObject instanceof Region) {
        newRegion = (Region) activeObject;
        newIsland = newRegion.getIsland();
      } else if(activeObject instanceof Faction) {
        newFaction = (Faction) activeObject;
      } else if(activeObject instanceof Group) {
        newFaction = ((Group) activeObject).getFaction();
      } else if(activeObject instanceof Island) {
        newIsland = (Island) activeObject;
      } else{ // some other node or non privileged unit selected
        transitionalIsland=null;
        transitionalRegion=null;
        transitionalFaction=null;
        newIsland=null;
        newRegion=null;
        newFaction=null;
        newUnit=null;
      }
    } 

    if ((newUnit == currentUnit && newUnit!=null) || 
        (newUnit!=null && currentUnit!=null && 
            newUnit.getRegion()==currentUnit.getRegion() && 
            newUnit.getFaction() == currentUnit.getFaction()))
      ; // no change necessary
    else{
      loadEditors(newIsland, newRegion, newFaction, newUnit);
    }
    
    selectEditor(newUnit);

    repaint();
    // make the UI component refresh itself - necessary at least under Windows
    revalidate();

  }

  /**
   * Sets appearance for editor corresponding to <code>newUnit</code> in multieditor mode.
   * 
   * @param newUnit If <code>null</code>, no editor is selected.
   * 
   */
  private void selectEditor(Unit newUnit) {
    if (getEditor(currentUnit)!=null)
      deselectEditor(currentUnit);
    if (newUnit!=null)
      selectEditor(newUnit, getEditor(newUnit));
    this.currentUnit=newUnit;
    if (newUnit!=null){
      this.currentFaction=newUnit.getFaction();
      this.currentIsland=newUnit.getRegion().getIsland();
      this.currentRegion=newUnit.getRegion();
    }
  }    
  
  /**
   * Sets appearance of an editor acording to the state of a unit.
   * 
   * @param newUnit Must not be <code>null</code>.
   * @param editor 
   */
  private void selectEditor(Unit newUnit, OrderEditor editor){
    if (newUnit==null)
      throw new NullPointerException();
    if(editor != null) {
      editor.setBorder(new TitledBorder(activeBorder,
                                   newUnit.toString() +
                                   ": " +
                                   newUnit.getPersons()));

      if(newUnit.isOrdersConfirmed()) {
        editor.setBackground(activeBgColorConfirmed);
      } else {
        editor.setBackground(activeBgColor);
      }

      // activate visibility call
      editor.setKeepVisible(true);
      editor.setEditable(true);

      // we use a call to repaint to force the execution of paint()
      // so we call SwingGlitchThread.run() indirectly AFTER painting
      // tricky and dirty, but it works...
      SwingUtilities.invokeLater(swingGlitchThread);
    }
  }

  /**
   * Reset a unit's editor border to normal.
   * 
   * @param currentUnit
   */
  private void deselectEditor(Unit currentUnit) {
    if(getEditor(currentUnit)!=null) {
      deselectEditor(getEditor(currentUnit));
    }
  }
  
  /**
   * Reset editor border to normal.
   * 
   * @param editor Must not be <code>null</code>.
   */
  private void deselectEditor(OrderEditor editor){
      editor.setBorder(new TitledBorder(standardBorder,currentUnit.toString() + ": " + currentUnit.getPersons()));

      if(currentUnit.isOrdersConfirmed()) {
        editor.setBackground(standardBgColorConfirmed);
      } else {
        editor.setBackground(standardBgColor);
      }

      // deactivate visibility call
      editor.setKeepVisible(false);

      // make the old editor do the syntax highlighting again later
      if(editor.isModified()) {
        updateThread.e = editor;
        SwingUtilities.invokeLater(updateThread);
      }
  }

  /**
	 * Ensures that the correct editors are loaded if a temp unit was created.
	 *
	 * 
	 */
	public void tempUnitCreated(TempUnitEvent e) {
		if(log.isDebugEnabled()) {
			log.debug("MultiEditorOrderEditorList.tempUnitCreated: " + e.getTempUnit());
		}

		if((currentUnit != null) && multiEditorLayout) {
			addUnit(e.getTempUnit());
//      loadEditors(currentUnit.getRegion().getIsland(), currentUnit.getRegion(), currentUnit.getFaction(), currentUnit);
      
			this.revalidate();

			if(log.isDebugEnabled()) {
				log.debug("MultiEditorOrderEditorList.tempUnitCreated: " + e.getTempUnit().getCache());
				log.debug("MultiEditorOrderEditorList.tempUnitCreated: " + getEditor(e.getTempUnit()));
			}

//			if(getEditor(e.getTempUnit()) != null) {
//				// pavkovic 2002.02.15: here we don't request focus in an invokeLater runnable
//				// because this would lead to intense focus change between parent and temp unit.
//				getEditor(e.getTempUnit()).requestFocus();
//				//requestFocus(e.getTempUnit().cache.orderEditor);
//			}
		}
	}

	/**
	 * Ensures that the right editors are loaded if a temp unit was created.
	 *
	 * 
	 */
	public void tempUnitDeleting(TempUnitEvent e) {
    if (multiEditorLayout){
      removeUnit(e.getTempUnit(), null);
      this.revalidate();
		}
	}

  
  /**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void keyPressed(KeyEvent e) {
		if((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) {
			if(multiEditorLayout) {
				switch(e.getKeyCode()) {
				case KeyEvent.VK_DOWN:
				  Unit u = getNextUnit();
					if(u!=null) {
						/* refreshing the relations here is
						 necessary because this is the only place
						 where a different unit is selected and the
						 focusLost event in the order editor does
						 not occur before the selection event */
						if (getEditor(currentUnit) != null && getEditor(currentUnit).isModified()) {
							currentUnit.refreshRelations();
						}
						
						dispatcher.fire(new SelectionEvent(this, null, u));
					}

					break;

				case KeyEvent.VK_UP:
				  u = getPreviousUnit();
					if(u!=null) {
						/* refreshing the relations here is
						 necessary because this is the only place
						 where a different unit is selected and the
						 focusLost event in the order editor does
						 not occur before the selection event */
						if (getEditor(currentUnit) != null && getEditor(currentUnit).isModified()) {
							currentUnit.refreshRelations();
						}

						

						dispatcher.fire(new SelectionEvent(this, null, u));
					}

					break;
				}
			}
		}
	}

  /**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void keyReleased(KeyEvent e) {
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void keyTyped(KeyEvent e) {
	}

	/**
	 * Fires an SelectionChanged event if a different editor than the current one is selected.
	 *
	 * 
	 */
	public void focusGained(FocusEvent e) {
//    log.info("fcg: "+((OrderEditor)e.getSource()).getUnit());
//		if(multiEditorLayout && e.getSource() instanceof OrderEditor) {
//			if((currentUnit == null) || (getEditor(currentUnit) != e.getSource())) {
//				dispatcher.fire(new SelectionEvent(this, null,
//												   ((OrderEditor) e.getSource()).getUnit()));
//			}
//		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void focusLost(FocusEvent e) {
	}

  /**
   * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
   */
  public void mouseClicked(MouseEvent e) {
    if (log.isDebugEnabled()){
      log.debug("mouseClicked "+e.getSource());
    }
    if (e.getSource() instanceof OrderEditor){
      if (multiEditorLayout)
        dispatcher.fire(new SelectionEvent(e.getSource(), null, ((OrderEditor) e.getSource()).getUnit()));
    }
  }

  /**
   * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
   */
  public void mouseEntered(MouseEvent e) {
  }

  /**
   * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
   */
  public void mouseExited(MouseEvent e) {
  }

  /**
   * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
   */
  public void mousePressed(MouseEvent e) {
  }

  /**
   * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
   */
  public void mouseReleased(MouseEvent e) {
  }

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void clearCache(Cache c) {
		if(c.orderEditor != null) {
			c.orderEditor.removeCaretListener(caretAdapter);
			c.orderEditor.removeFocusListener(focusAdapter);
			c.orderEditor.removeKeyListener(keyAdapter);
			c.orderEditor.setBorder(null);
			c.orderEditor.setBackground(null);
			c.orderEditor.setFont(null);
			c.orderEditor.setCursor(null);
		}
	}

	/**
	 * Return whether syntax highlighting in the editor is enabled or disabled.
	 *
	 * 
	 */
	public boolean getUseSyntaxHighlighting() {
		if(multiEditorLayout) {
			return (new OrderEditor(data, settings, undoMgr, dispatcher)).getUseSyntaxHighlighting();
		} else {
			return editor.getUseSyntaxHighlighting();
		}
	}

	/**
	 * Enable or disable syntax highlighting in the editor.
	 *
	 * 
	 */
	public void setUseSyntaxHighlighting(boolean bool) {
		if(multiEditorLayout) {
			boolean foundEditor = false;

			if(data.units() != null) {
				for(Iterator iter = data.units().values().iterator(); iter.hasNext();) {
					Unit u = (Unit) iter.next();

					if(getEditor(u) != null) {
						foundEditor = true;
            getEditor(u).setUseSyntaxHighlighting(bool);
					}
				}
			}

			if(!foundEditor) {
				(new OrderEditor(data, settings, undoMgr, dispatcher)).setUseSyntaxHighlighting(bool);
			}
		} else {
			editor.setUseSyntaxHighlighting(bool);
		}
	}

	/**
	 * Return the color of the specified token style used for syntax highlighting in the editor.
	 *
	 * 
	 *
	 * 
	 */
	public Color getTokenColor(String styleName) {
		return (new OrderEditor(data, settings, undoMgr, dispatcher)).getTokenColor(styleName);
	}

	/**
	 * Set the color of the specified token style used for syntax highlighting in the editor.
	 *
	 * 
	 * 
	 */
	public void setTokenColor(String styleName, Color color) {
		if(multiEditorLayout) {
			boolean foundEditor = false;

			if(data.units() != null) {
				for(Iterator iter = data.units().values().iterator(); iter.hasNext();) {
					Unit u = (Unit) iter.next();

					if(getEditor(u) != null) {
						foundEditor = true;
						getEditor(u).setTokenColor(styleName, color);
					}
				}
			}

			if(!foundEditor) {
				(new OrderEditor(data, settings, undoMgr, dispatcher)).setTokenColor(styleName,
																					 color);
			}
		} else {
			editor.setTokenColor(styleName, color);
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public Color getStandardBackgroundColor() {
		return standardBgColor;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setStandardBackgroundColor(Color c) {
		if((standardBgColor != c) && (c != null)) {
			standardBgColor = c;
			settings.setProperty("OrderEditor.standardBackgroundColor", Colors.encode(c));
		}

		if(multiEditorLayout) {
			if(data.units() != null) {
				for(Iterator iter = data.units().values().iterator(); iter.hasNext();) {
					Unit u = (Unit) iter.next();

					if(getEditor(u) != null) {
            getEditor(u).setBackground(c);
					}
				}
			}
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public Color getStandardBackgroundColorConfirmed() {
		return standardBgColorConfirmed;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setStandardBackgroundColorConfirmed(Color c) {
		if((standardBgColorConfirmed != c) && (c != null)) {
			standardBgColorConfirmed = c;
			settings.setProperty("OrderEditor.standardBackgroundColorConfirmed", Colors.encode(c));
		}

		if(multiEditorLayout) {
			if(data.units() != null) {
				for(Iterator iter = data.units().values().iterator(); iter.hasNext();) {
					Unit u = (Unit) iter.next();

					if(getEditor(u) != null) {
            getEditor(u).setBackground(c);
					}
				}
			}
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public Color getActiveBackgroundColor() {
		return activeBgColor;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setActiveBackgroundColor(Color c) {
		if((activeBgColor != c) && (c != null)) {
			activeBgColor = c;
			settings.setProperty("OrderEditor.activeBackgroundColor", Colors.encode(c));
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public Color getActiveBackgroundColorConfirmed() {
		return activeBgColorConfirmed;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setActiveBackgroundColorConfirmed(Color c) {
		if((activeBgColorConfirmed != c) && (c != null)) {
			activeBgColorConfirmed = c;
			settings.setProperty("OrderEditor.activeBackgroundColorConfirmed", Colors.encode(c));
		}
	}

	/**
	 * 
	 * @return <code>true</code> iff currently in multi editor layout
	 * 
	 */
	public boolean isMultiEditorLayout() {
		return multiEditorLayout;
	}

	/**
	 * Does initialization (of content resp. editor) for the specified mode.
	 *
	 * @param multi If <code>true</code> multi editor layout is initialized, else single editor mode.
	 */
	public void setMultiEditorLayout(boolean multi) {
		if(multi != multiEditorLayout) {
			settings.setProperty("OrderEditor.multiEditorLayout", String.valueOf(multi));
			clearUnits();
			synchronized (content) {
			  if(multi && (editor != null)) {
			    // if before there was only one editor and now we
			    // switch to multi editor layout
			    content.removeAll();
			    removeListeners(editor);
			    editor = null;
			  } else {
			    editor = new OrderEditor(data, settings, undoMgr, dispatcher);
			    editor.setCursor(new Cursor(Cursor.TEXT_CURSOR));

			    // add listeners
			    addListeners(editor);
			    content.add(editor);
			  }
			}

			multiEditorLayout = multi;
			repaint();
      
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public boolean isHideButtons() {
		return hideButtons;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setHideButtons(boolean bool) {
		if(bool != hideButtons) {
			settings.setProperty("OrderEditor.hideButtons", String.valueOf(bool));
			hideButtons = bool;
			redrawPane();
		}
	}

	private void redrawPane() {
		remove(scpContent);
		remove(buttons);
		setLayout(new BorderLayout());
		add(scpContent, BorderLayout.CENTER);

		if(!hideButtons) {
			add(buttons, BorderLayout.SOUTH);
		}

		repaint();
	}

	/**
	 * Removes the Adapters for Key-, Caret- & Focusevents from the given JTextComponents.
	 *
	 * 
	 */
	private void removeListeners(JTextComponent j) {
		j.removeFocusListener(focusAdapter);
		j.removeKeyListener(keyAdapter);
		j.removeCaretListener(caretAdapter);
    j.removeMouseListener(this);

	}

	/**
	 * Removes the Adapters for Key-, Caret- & Focusevents from all sub-components that are
	 * JTextComponents.
	 */
	private void removeListenersFromAll() {
		Component c[] = content.getComponents();

		if((c != null) && (c.length > 0)) {
			for(int i = 0; i < c.length; i++) {
				if(c[i] instanceof JTextComponent) {
					removeListeners((JTextComponent) c[i]);
				}
			}
		}
	}

	/**
	 * Adds the Adapters for Key-, Caret- & Focusevents to the given JTextComponents.
	 *
	 * 
	 */
	private void addListeners(JTextComponent j) {
		j.addFocusListener(focusAdapter);
		j.addKeyListener(keyAdapter);
		j.addCaretListener(caretAdapter);
    j.addMouseListener(this);
	}

	/**
	 * @param type A mode constant
	 * @return True iff current listMode is of <code>type</code>
	 */
	private boolean isListMode(int type) {
	  return ((listMode >> type) & 1) != 0;
	}

  /**
   * Load editors belonging to the specified island and faction depending on listMode.
   * 
   * @param r
   * @param f
   */
	private void loadEditors(Island i, Faction f) {
	  List<Unit> l = new LinkedList<Unit>();
	  if (!isListMode(LIST_FACTION))
	    f = null;
	  if (isListMode(LIST_ISLAND)) {
	    // list units of specified Island
	    for (Region r : i.regions()) {
	      for (Unit u : r.units())
	        if (EMapDetailsPanel.isPrivileged(f) && (f == null || u.getFaction().equals(f)))
	          l.add(u);
	    }
	  }

	  loadEditors(l);
	}

	/**
   * Load editors belonging to the specified region and faction depending on listMode.
   * 
	 * @param r
	 * @param f
	 */
	private void loadEditors(Region r, Faction f) {
	  if (!isListMode(LIST_REGION) && r.getIsland()!=null){
	    if (isListMode(LIST_ISLAND))
	      loadEditors(r.getIsland(), f);
	    else
	      loadEditors(Collections.<Unit>emptyList());
	    return;
	  }
	  if (!isListMode(LIST_FACTION))
	    f = null;

	  List<Unit> l = new LinkedList<Unit>(r.units());

	  for(Iterator<Unit> iter = l.iterator(); iter.hasNext();) {
	    Unit u = iter.next();

	    if(!EMapDetailsPanel.isPrivileged(u.getFaction()) || (f!=null &&  !f.equals(u.getFaction()))) {
	      iter.remove();
	    }
	  }
	  loadEditors(l);
	}


	/**
	 * Loads editors for the specified unit depending on <code>listMode</code>.
	 * 
	 * @param u
	 */
	private void loadEditors(Unit u) {
    if (!EMapDetailsPanel.isPrivilegedAndNoSpy(u)){
      loadEditors(Collections.<Unit>emptyList());
      return;
    }
    
	  if (isListMode(LIST_REGION))
	    if (isListMode(LIST_FACTION))
	      loadEditors(u.getRegion(), u.getFaction());
	    else 
	      loadEditors(u.getRegion(), (Faction) null);
	  else if (isListMode(LIST_ISLAND))
	    if (isListMode(LIST_FACTION)) 
	      loadEditors(u.getRegion().getIsland(), u.getFaction());
	    else
	      loadEditors(u.getRegion().getIsland(), (Faction) null);
	  else 
	    loadEditors(Collections.singletonList(u));
	}


	/**
   * Loads and displays the editors that best correspond to the selected
   * entities. This depends on the current
   * {@link MultiEditorOrderEditorList#listMode}. 
   * 
   *  <code>
   *                   listMode (ISLAND/REGION/FACTION)
   *  selected:        000    100     010     001    110      101             011            111
   *  island           empty  island  empty   empty  island  island          empty           island
   *  region           empty  island  region  empty  region  island          region/faction  region
   *  faction/group    empty  island  region  empty  region  faction/island  region/faction  faction/region
   *  unit             unit   island  region  unit   region  faction/island  region/faction  faction/region
   *  other             ===  like parent  ===
   * </code>
   * 
   * @param newIsland
   * @param newRegion
   * @param newFaction
   * @param newUnit
   */
	private void loadEditors(Island newIsland, Region newRegion, Faction newFaction, Unit newUnit) {
    if (newUnit!=null){
      loadEditors(newUnit);
    }else if (newRegion!=null && newFaction!=null){
	    loadEditors(newRegion, newFaction);
	  }else if (newRegion!=null){ // newFaction == null
	    loadEditors(newRegion, (Faction) null);
	  }else if (newIsland!=null){
	    loadEditors(newIsland, newFaction);
	  }else {
	    loadEditors(Collections.<Unit>emptyList());
	  }
    currentIsland = newIsland;
    currentRegion  = newRegion;
    currentFaction = newFaction;
    currentUnit = newUnit;
    transitionalIsland=null;
    transitionalRegion=null;
    transitionalFaction=null;
	}

	/**
   * Adds editors for all units in privileged factions that are in the specified
   * list to this component and removes unused ones. OrderEditors are created if
   * necessary, else the cached version are used.
   */
	private void loadEditors(List<Unit> unitsToShow) {
    clearUnits();
    for (Unit u: unitsToShow){
      units.add(u);
    }
    for (Unit u: units){
      OrderEditor editor = buildOrderEditor(u);
      content.add(editor);
      addListeners(editor);
    }
    
	  this.invalidate();
	  this.validate();
	}

	/**
   * Returns the cached editor for a unit.
   * 
   * @param u A unit, may be <code>null</code>.
   * @return The cached editor for <code>u</code> or null if none exists 
   */
  private OrderEditor getEditor(Unit u) {
    if(u!=null && u.getCache() != null && u.getCache().orderEditor != null) 
      return (OrderEditor) u.getCache().orderEditor;
    else
      return null;
  }

  /**
   * Sets the cached editor of the specified unit.
   * 
   * @param u
   *          The unit, not <code>null</code>.
   * @param editor
   *          The new editor or <code>null</code> to dispose the currently
   *          cached editor of <code>u</code>.
   */
  private void setEditor(Unit u, CacheableOrderEditor editor) {
    if(u.getCache() == null) {
      if (editor==null)
        return;
      u.setCache(new Cache());
    }
    u.getCache().orderEditor=editor;
  }

  
  /**
   * Removes the specified unit from the content. This includes removing editor
   * and listeners for it and the unit itself from <code>units</code>.
   * 
   * @param u
   * @param it
   *          An iterator on <code>units</code>. If not <code>null</code>,
   *          the unit is removed via this iterator, if <code>null</code> it is removed via
   *          <code>units.remove</code>.
   */
	private void removeUnit(Unit u, Iterator<Unit> it) {
    if (log.isDebugEnabled())
      log.debug("MultiEditor.removeUnit  "+u);
	  JTextComponent c = getEditor(u);
	  synchronized (content) {
	    if (c==null)
	      log.error(this.getClass()+": unit already removed; "+u);
	    else{
	      content.remove(c);
	      removeListeners(c);
	    }
	    if (it!=null)
	      it.remove();
	    else
	      units.remove(u);
	    if (content.getComponentCount()==0 && this.content.hasFocus())
	      this.requestFocus();
	  }
	}

	/**
   * Adds the specified unit to the content. This includes creating and adding a
   * new order editor and updating the internal data structures. If possible,
   * the new editor is inserted at the right position in the unit order, else it
   * is inserted at the end.
   * 
   * @param u
   *          The unit for the new editor.
   */
	private void addUnit(Unit u) {
	  if (!units.contains(u)){
      if (log.isDebugEnabled())
        log.debug("MultiEditor.add: "+u);
	    buildOrderEditor(u);

      units.add(u);
      if (units.first()==u)
        content.add(getEditor(u), 0);
      else{
        Unit predUnit = getPreviousUnit(u);
        if (predUnit==null){
          content.add(getEditor(u));
        }else{ 
          content.add(getEditor(u), content.getComponentZOrder(getEditor(predUnit))+1);
        }
      }
	    addListeners(getEditor(u));
	  }else{
      if (log.isDebugEnabled())
        log.debug("MultiEditor.not add: "+u);
    }
	}

	/**
	 * @return The unit following on currentUnit in the unit sorting of {@link EMapOverviewPanel}
	 */
	protected Unit getNextUnit() {
	  return getNextUnit(currentUnit);
  }

    
  /**
   * @return The unit following on u in the unit sorting of {@link EMapOverviewPanel}
   * 
   */
    protected Unit getNextUnit(Unit u) {
      Iterator<Unit> it = units.tailSet(u).iterator();
      if (!it.hasNext())
        return null;
      it.next(); // skip the currentUnit
      if (it.hasNext())
        return it.next();
      else
        return null;
//	  return ( units.get(units.indexOf(currentUnit) + 1));
	}

  /**
   * @return The unit preceding the currentUnit in the unit sorting of {@link EMapOverviewPanel}
   */
	private Unit getPreviousUnit() {
   return getPreviousUnit(currentUnit);
  }

    
    /**
     * @return The unit preceding on <code>u</code> in the unit sorting of {@link EMapOverviewPanel}
     */
    private Unit getPreviousUnit(Unit u) {
      SortedSet<Unit> pre = units.headSet(u);
      if (pre.isEmpty())
        return null;
      else
        return pre.last();
	}

  /**
	 * Builds and attaches the order editor for and to the given unit.
	 * 
	 */
	private OrderEditor buildOrderEditor(Unit u) {
    if (u==null){
      return null;
    }
    OrderEditor editor = getEditor(u);
    boolean created = false;
		if(editor == null) {
			editor = new OrderEditor(data, settings, undoMgr, dispatcher);
      created = true;
    }
		attachOrderEditor(u, editor);
    if (created){
      u.getCache().addHandler(this);
    }
    return editor;
  }
  
  /**
   * Attaches a unit to an editor and vice versa.
   * 
   * @param u
   * @param editor
   */
  private void attachOrderEditor(Unit u, OrderEditor editor){
		editor.setBorder(new TitledBorder(standardBorder, u.toString() + ": " + u.getPersons()));

		if(u.isOrdersConfirmed()) {
		  editor.setBackground(standardBgColorConfirmed);
		} else {
		  editor.setBackground(standardBgColor);
		}

		// deactivate visibility call
		editor.setKeepVisible(false);
		editor.setFont(new Font("Monospaced", Font.PLAIN, editor.getFont().getSize()));
		editor.setCursor(new Cursor(Cursor.TEXT_CURSOR));

		editor.setUnit(u);

		setEditor(u, editor);
	}

	/**
	 * Performs the clean-up necessary to put the editor list into a state without units and
	 * editors
	 */
	private void clearUnits() {
    log.debug("clearUnits called");
		if(multiEditorLayout) {
		  synchronized(content) {
		    if(units != null) {
		      units.clear();
		    }

		    removeListenersFromAll();
		    content.removeAll();
		  }
		} else {
			if(currentUnit != null) {
				if(getEditor(currentUnit) != null) {
					removeListeners(getEditor(currentUnit));
					setEditor(currentUnit, null);
				}

				editor.setUnit(null);
				editor.setEditable(false);
				removeListeners(editor);
			}
		}

		currentUnit = null;
	}

	private void requestFocus(final CacheableOrderEditor editor) {
    // stm 2007.08.06 hack removed, still works
		// pavkovic 2004.02.14
		// THIS IS A HACK: I don't know why the focus 
		// gets lost somehow so put it at the end of the 
		// event dispatching thread.
		// It may be EMapOverviewPanel (SwingUtilities.invokeLater(new ScrollerRunnable()))).
//		SwingUtilities.invokeLater(new Runnable() {
//				public void run() {
					editor.requestFocus();
//				}
//			});
	}

	/**
	 * Ensures that the right order editor gets the focus.
	 */
	public void requestFocus() {
	  if (multiEditorLayout){
	    if(getEditor(currentUnit) != null) {
	      requestFocus(getEditor(currentUnit));
	    }else
	      super.requestFocus();
	  }else{
	    editor.requestFocus();
	  }
	}

	protected void loadListProperty() {
		try {
			listMode = Integer.parseInt(settings.getProperty("OrderEditorList.listMode"));
		} catch(Exception exc) {
			listMode = 1 << LIST_REGION;
		}
	}

	protected void saveListProperty() {
		if(listMode == (1 << LIST_REGION)) {
			settings.remove("OrderEditorList.listMode");
		} else {
			settings.setProperty("OrderEditorList.listMode", String.valueOf(listMode));
		}
	}

	// pavkovic 2003.01.28: this is a Map of the default Translations mapped to this class
	// it is called by reflection (we could force the implementation of an interface,
	// this way it is more flexible.)
	// Pls use this mechanism, so the translation files can be created automagically
	// by inspecting all classes.
	private static Map<String,String> defaultTranslations;

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public static synchronized Map<String,String> getDefaultTranslations() {
		if(defaultTranslations == null) {
			defaultTranslations = new Hashtable<String, String>();
			defaultTranslations.put("chk.orderconfirmation", "Orders confirmed");

			defaultTranslations.put("msg.newtempid.text",
									"Please enter an id for the new temporary unit:");
			defaultTranslations.put("msg.newtempid.title", "New temp id");
			defaultTranslations.put("msg.duplicatetempid.text",
									"The specified temp id is already assigned to a unit. Please enter a different id.");
			defaultTranslations.put("msg.duplicatetempid.title", "Temp id used");
			defaultTranslations.put("msg.invalidtempid.text",
									"The temp id you specified is invalid.");
			defaultTranslations.put("prefs.listMode.text",
									"This settings affects the units to be shown while selecting a game object. Only useful in Multieditor-Mode.");
			defaultTranslations.put("prefs.listMode", "Editor Listing");
			defaultTranslations.put("shortcuts.description.1", "Delete temp unit");
			defaultTranslations.put("msg.invalidtempid.title", "Invalid temp id");

			defaultTranslations.put("prefs.multieditorlayout", "Multi-editor layout");
			defaultTranslations.put("prefs.hidebuttons", "Hide buttons");
			defaultTranslations.put("prefs.syntaxhighlighting", "Syntax highlighting");
			defaultTranslations.put("prefs.colors", "Colors");
			defaultTranslations.put("prefs.orderautocompletion", "Auto-complete orders");
			defaultTranslations.put("prefs.title", "Order editor");
			defaultTranslations.put("prefs.colors.standard", "Default");
			defaultTranslations.put("prefs.colors.keywords", "Keywords");
			defaultTranslations.put("prefs.colors.strings", "Strings");
			defaultTranslations.put("prefs.listMode.2.text",
									"Shows all privileged units of the current Faction");
			defaultTranslations.put("prefs.listMode.1.text",
									"Shows all privileged units of the current region");
			defaultTranslations.put("prefs.listMode.0.text",
									"Shows all privileged units of the current island");
			defaultTranslations.put("prefs.listMode.2", "Factions");
			defaultTranslations.put("prefs.listMode.1", "Regions");
			defaultTranslations.put("prefs.listMode.0", "Islands");
			defaultTranslations.put("shortcuts.description.0", "Create temp unit");
			defaultTranslations.put("shortcuts.title", "Temp units");
			defaultTranslations.put("prefs.syntaxhighlighting.caption",
									"Activate Syntax highlighting");
			defaultTranslations.put("prefs.layout", "Layout");
			defaultTranslations.put("prefs.colors.numbers", "Numbers");
			defaultTranslations.put("prefs.colors.ids", "IDs");
			defaultTranslations.put("prefs.colors.comments", "Comments");
			defaultTranslations.put("prefs.inactivebackground", "Color of inactive editors");
			defaultTranslations.put("prefs.inactivebackground.confirmed",
									"Color of inactive and confirmed editors");
			defaultTranslations.put("prefs.backgroundcolor", "Background color");
			defaultTranslations.put("prefs.activebackground", "Color of active editor");
			defaultTranslations.put("prefs.activebackground.confirmed",
									"Color of active and confirmed editor");

			defaultTranslations.put("tempunit.recruitCost", "recruitment costs");
			defaultTranslations.put("tempunit.maintainCost", "maintainance costs");
		}

		return defaultTranslations;
	}

	class OrderEditorListPreferences extends JPanel implements PreferencesAdapter {
		private MultiEditorOrderEditorList source = null;
		private JPanel pnlStandardColor = null;
		private JPanel pnlStandardColorConfirmed = null;
		private JPanel pnlActiveColor = null;
		private JPanel pnlActiveColorConfirmed = null;
		private JPanel pnlStylesColor = null;
		private JCheckBox chkMultiEditorLayout;
		private JCheckBox chkHideButtons;
		private JCheckBox chkSyntaxHighlighting;
		private JComboBox comboSHColors = null;
		private Dimension prefDim = new Dimension(20, 20);
		private JCheckBox listModes[];

		/**
		 * Creates a new OrderEditorListPreferences object.
		 *
		 * 
		 */
		public OrderEditorListPreferences(MultiEditorOrderEditorList source) {
			this.source = source;

			this.setLayout(new GridBagLayout());

			GridBagConstraints c = new GridBagConstraints(0, 0, 1, 1, 1, 0,
														  GridBagConstraints.WEST,
														  GridBagConstraints.HORIZONTAL,
														  new Insets(0, 0, 0, 0), 0, 0);

			this.add(getLayoutPanel(), c);

			c.gridy++;
			this.add(getColorPanel(), c);

			c.gridy++;
			this.add(getHighlightPanel(), c);

			c.gridy++;
			this.add(getListModePanel(), c);
		}

		protected Container getLayoutPanel() {
			JPanel content = new JPanel(new FlowLayout(FlowLayout.LEADING, 2, 0));
			content.setBorder(new TitledBorder(Resources.get("completion.multieditorordereditorlist.prefs.layout")));

			chkMultiEditorLayout = new JCheckBox(Resources.get("completion.multieditorordereditorlist.prefs.multieditorlayout"),
												 source.isMultiEditorLayout());
			content.add(chkMultiEditorLayout);

			chkHideButtons = new JCheckBox(Resources.get("completion.multieditorordereditorlist.prefs.hidebuttons"), source.isHideButtons());
			content.add(chkHideButtons);

			return content;
		}

		protected Container getColorPanel() {
			JPanel content = new JPanel(new GridBagLayout());
			content.setBorder(new TitledBorder(Resources.get("completion.multieditorordereditorlist.prefs.colors")));

			GridBagConstraints c = new GridBagConstraints(0, 0, 1, 1, 1, 0,
														  GridBagConstraints.WEST,
														  GridBagConstraints.NONE,
														  new Insets(0, 2, 1, 1), 0, 0);

			JLabel lblStandardColor = new JLabel(Resources.get("completion.multieditorordereditorlist.prefs.inactivebackground") + ": ");

			pnlStandardColor = new JPanel();
			pnlStandardColor.setBorder(new LineBorder(Color.black));
			pnlStandardColor.setPreferredSize(prefDim);
			pnlStandardColor.setBackground(source.getStandardBackgroundColor());
			pnlStandardColor.addMouseListener(new MouseAdapter() {
					public void mousePressed(MouseEvent e) {
						Color newColor = JColorChooser.showDialog(((JComponent) e.getSource()).getTopLevelAncestor(),
																  Resources.get("completion.multieditorordereditorlist.prefs.backgroundcolor"),
																  ((Component) e.getSource()).getBackground());

						if(newColor != null) {
							((Component) e.getSource()).setBackground(newColor);
						}
					}
				});

			JLabel lblStandardColorConfirmed = new JLabel(Resources.get("completion.multieditorordereditorlist.prefs.inactivebackground.confirmed") +
														  ": ");

			pnlStandardColorConfirmed = new JPanel();
			pnlStandardColorConfirmed.setBorder(new LineBorder(Color.black));
			pnlStandardColorConfirmed.setPreferredSize(prefDim);
			pnlStandardColorConfirmed.setBackground(source.getStandardBackgroundColorConfirmed());
			pnlStandardColorConfirmed.addMouseListener(new MouseAdapter() {
					public void mousePressed(MouseEvent e) {
						Color newColor = JColorChooser.showDialog(((JComponent) e.getSource()).getTopLevelAncestor(),
																  Resources.get("completion.multieditorordereditorlist.prefs.backgroundcolor"),
																  ((Component) e.getSource()).getBackground());

						if(newColor != null) {
							((Component) e.getSource()).setBackground(newColor);
						}
					}
				});

			JLabel lblActiveColor = new JLabel(Resources.get("completion.multieditorordereditorlist.prefs.activebackground") + ": ");

			pnlActiveColor = new JPanel();
			pnlActiveColor.setBorder(new LineBorder(Color.black));
			pnlActiveColor.setPreferredSize(prefDim);
			pnlActiveColor.setBackground(source.getActiveBackgroundColor());
			pnlActiveColor.addMouseListener(new MouseAdapter() {
					public void mousePressed(MouseEvent e) {
						Color newColor = JColorChooser.showDialog(((JComponent) e.getSource()).getTopLevelAncestor(),
																  Resources.get("completion.multieditorordereditorlist.prefs.backgroundcolor"),
																  ((Component) e.getSource()).getBackground());

						if(newColor != null) {
							((Component) e.getSource()).setBackground(newColor);
						}
					}
				});

			JLabel lblActiveColorConfirmed = new JLabel(Resources.get("completion.multieditorordereditorlist.prefs.activebackground.confirmed") +
														": ");

			pnlActiveColorConfirmed = new JPanel();
			pnlActiveColorConfirmed.setBorder(new LineBorder(Color.black));
			pnlActiveColorConfirmed.setPreferredSize(prefDim);
			pnlActiveColorConfirmed.setBackground(source.getActiveBackgroundColorConfirmed());
			pnlActiveColorConfirmed.addMouseListener(new MouseAdapter() {
					public void mousePressed(MouseEvent e) {
						Color newColor = JColorChooser.showDialog(((JComponent) e.getSource()).getTopLevelAncestor(),
																  Resources.get("completion.multieditorordereditorlist.prefs.backgroundcolor"),
																  ((Component) e.getSource()).getBackground());

						if(newColor != null) {
							((Component) e.getSource()).setBackground(newColor);
						}
					}
				});

			content.add(lblActiveColor, c);
			c.gridx++;
			content.add(pnlActiveColor, c);

			c.gridx = 0;
			c.gridy++;

			content.add(lblActiveColorConfirmed, c);
			c.gridx++;
			content.add(pnlActiveColorConfirmed, c);

			c.gridx = 0;
			c.gridy++;

			content.add(lblStandardColor, c);
			c.gridx++;
			content.add(pnlStandardColor, c);

			c.gridx = 0;
			c.gridy++;

			content.add(lblStandardColorConfirmed, c);
			c.gridx++;
			content.add(pnlStandardColorConfirmed, c);

			return content;
		}

		protected Container getHighlightPanel() {
			JPanel content = new JPanel(new GridBagLayout());
			content.setBorder(new TitledBorder(Resources.get("completion.multieditorordereditorlist.prefs.syntaxhighlighting")));

			GridBagConstraints c = new GridBagConstraints(0, 0, 1, 1, 1, 0,
														  GridBagConstraints.WEST,
														  GridBagConstraints.NONE,
														  new Insets(0, 2, 1, 1), 0, 0);

			chkSyntaxHighlighting = new JCheckBox(Resources.get("completion.multieditorordereditorlist.prefs.syntaxhighlighting.caption"),
												  source.getUseSyntaxHighlighting());
			content.add(chkSyntaxHighlighting, c);

			c.gridy++;

			Container styles = createStylesContainer();
			content.add(styles, c);

			return content;
		}

		protected Container getListModePanel() {
			JPanel content = new JPanel(new BorderLayout(2, 2));
			content.setBorder(new TitledBorder(Resources.get("completion.multieditorordereditorlist.prefs.listMode")));

			JTextArea text = new JTextArea(Resources.get("completion.multieditorordereditorlist.prefs.listMode.text"));
			text.setBackground(content.getBackground());
			text.setEditable(false);
			text.setLineWrap(true);
			text.setWrapStyleWord(true);
			content.add(text, BorderLayout.NORTH);

			JPanel help = new JPanel(new GridBagLayout());
			GridBagConstraints con = new GridBagConstraints(0, 0, 1, 1, 0.5, 0,
															GridBagConstraints.WEST,
															GridBagConstraints.NONE,
															new Insets(1, 1, 1, 1), 0, 0);
			listModes = new JCheckBox[3];

			for(int i = 0; i < 3; i++) {
				listModes[i] = new JCheckBox(Resources.get("completion.multieditorordereditorlist.prefs.listMode." + i));
				listModes[i].setSelected(((listMode >> (3 - i)) & 1) != 0);
				help.add(listModes[i], con);
				con.gridy++;
			}

			con.gridy = 0;
			con.gridx = 1;
			con.fill = GridBagConstraints.HORIZONTAL;

			for(int i = 0; i < 3; i++) {
				text = new JTextArea(Resources.get("completion.multieditorordereditorlist.prefs.listMode." + i + ".text"));
				text.setBackground(content.getBackground());
				text.setEditable(false);
				text.setLineWrap(true);
				text.setWrapStyleWord(true);
				help.add(text, con);
				con.gridy++;
			}

			content.add(help, BorderLayout.CENTER);

			help = new JPanel();
			help.setPreferredSize(new Dimension(15, 5));
			content.add(help, BorderLayout.WEST);

			return content;
		}

		protected Container createStylesContainer() {
			JPanel content = new JPanel();

			comboSHColors = new JComboBox(getStyles());
			comboSHColors.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						StyleContainer c = (StyleContainer) comboSHColors.getSelectedItem();
						pnlStylesColor.setBackground(c.color);
					}
				});
			content.add(comboSHColors);

			pnlStylesColor = new JPanel();
			pnlStylesColor.setBorder(new LineBorder(Color.black));
			pnlStylesColor.setPreferredSize(prefDim);
			pnlStylesColor.setBackground(((StyleContainer) comboSHColors.getItemAt(0)).color);
			pnlStylesColor.addMouseListener(new MouseAdapter() {
					public void mousePressed(MouseEvent me) {
						StyleContainer sc = (StyleContainer) comboSHColors.getSelectedItem();
						Color newColor = JColorChooser.showDialog(((JComponent) me.getSource()).getTopLevelAncestor(),
																  sc.description + " Farbe",
																  sc.color);

						if(newColor != null) {
							sc.color = newColor;
							pnlStylesColor.setBackground(newColor);
						}
					}
				});
			content.add(pnlStylesColor);

			return content;
		}

		private Object[] getStyles() {
			Object styles[] = new Object[6];

			styles[0] = new StyleContainer(Resources.get("completion.multieditorordereditorlist.prefs.colors.standard"),
										   OrderEditor.S_REGULAR,
										   source.getTokenColor(OrderEditor.S_REGULAR));
			styles[1] = new StyleContainer(Resources.get("completion.multieditorordereditorlist.prefs.colors.keywords"),
										   OrderEditor.S_KEYWORD,
										   source.getTokenColor(OrderEditor.S_KEYWORD));
			styles[2] = new StyleContainer(Resources.get("completion.multieditorordereditorlist.prefs.colors.strings"), OrderEditor.S_STRING,
										   source.getTokenColor(OrderEditor.S_STRING));
			styles[3] = new StyleContainer(Resources.get("completion.multieditorordereditorlist.prefs.colors.numbers"), OrderEditor.S_NUMBER,
										   source.getTokenColor(OrderEditor.S_NUMBER));
			styles[4] = new StyleContainer(Resources.get("completion.multieditorordereditorlist.prefs.colors.ids"), OrderEditor.S_ID,
										   source.getTokenColor(OrderEditor.S_ID));
			styles[5] = new StyleContainer(Resources.get("completion.multieditorordereditorlist.prefs.colors.comments"),
										   OrderEditor.S_COMMENT,
										   source.getTokenColor(OrderEditor.S_COMMENT));

			return styles;
		}

        public void initPreferences() {
            // TODO: implement it
        }
		/**
		 * DOCUMENT-ME
		 */
		public void applyPreferences() {
			source.setActiveBackgroundColor(pnlActiveColor.getBackground());
			source.setActiveBackgroundColorConfirmed(pnlActiveColorConfirmed.getBackground());
			source.setStandardBackgroundColor(pnlStandardColor.getBackground());
			source.setStandardBackgroundColorConfirmed(pnlStandardColorConfirmed.getBackground());
			source.setMultiEditorLayout(chkMultiEditorLayout.isSelected());
			source.setHideButtons(chkHideButtons.isSelected());
			source.setUseSyntaxHighlighting(chkSyntaxHighlighting.isSelected());

			for(int i = 0; i < comboSHColors.getItemCount(); i++) {
				StyleContainer sc = (StyleContainer) comboSHColors.getItemAt(i);
				source.setTokenColor(sc.name, sc.color);
			}

			listMode = 0;

			for(int i = 0; i < 3; i++) {
				if(listModes[i].isSelected()) {
					listMode |= (1 << (3 - i));
				}
			}

			if(listMode == 0) {
				listMode = (1 << LIST_REGION);
			}

			saveListProperty();
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
			return Resources.get("completion.multieditorordereditorlist.prefs.title");
		}

		private class StyleContainer {
			/** DOCUMENT-ME */
			public String description;

			/** DOCUMENT-ME */
			public String name;

			/** DOCUMENT-ME */
			public Color color;

			/**
			 * Creates a new StyleContainer object.
			 *
			 * 
			 * 
			 * 
			 */
			public StyleContainer(String description, String name, Color color) {
				this.description = description;
				this.name = name;
				this.color = color;
			}

			/**
			 * DOCUMENT-ME
			 *
			 * 
			 */
			public String toString() {
				return description;
			}
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public JTextComponent getCurrentEditor() {
		if(multiEditorLayout) {
			if(getEditor(currentUnit) != null) {
				return getEditor(currentUnit);
			} else {
				return null;
			}
		} else {
			return editor;
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public Unit getCurrentUnit() {
		return currentUnit;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public PreferencesAdapter getPreferencesAdapter() {
		return new OrderEditorListPreferences(this);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void addExternalKeyListener(KeyListener k) {
		keyListeners.add(k);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void removeExternalKeyListener(KeyListener k) {
		keyListeners.remove(k);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void addExternalCaretListener(CaretListener k) {
		caretListeners.add(k);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void removeExternalCaretListener(CaretListener k) {
		caretListeners.remove(k);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void addExternalFocusListener(FocusListener k) {
		focusListeners.add(k);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void removeExternalFocusListener(FocusListener k) {
		focusListeners.remove(k);
	}

	private class MEKeyAdapter extends KeyAdapter {
		protected MultiEditorOrderEditorList source;

		/**
		 * Creates a new MEKeyAdapter object.
		 *
		 * 
		 */
		public MEKeyAdapter(MultiEditorOrderEditorList s) {
			source = s;
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public void keyPressed(KeyEvent e) {
			// first external listeners
			for(Iterator iter = source.keyListeners.iterator(); iter.hasNext();) {
				((KeyListener) iter.next()).keyPressed(e);

				if(e.isConsumed()) {
					return;
				}
			}

			// now the source
			source.keyPressed(e);
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public void keyReleased(KeyEvent e) {
			// first external listeners
			for(Iterator iter = source.keyListeners.iterator(); iter.hasNext();) {
				((KeyListener) iter.next()).keyReleased(e);

				if(e.isConsumed()) {
					return;
				}
			}

			// now the source
			source.keyReleased(e);
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public void keyTyped(KeyEvent e) {
			// first external listeners
			for(Iterator iter = source.keyListeners.iterator(); iter.hasNext();) {
				((KeyListener) iter.next()).keyTyped(e);

				if(e.isConsumed()) {
					return;
				}
			}

			// now the source
			source.keyTyped(e);
		}
	}

	private class MEFocusAdapter extends FocusAdapter {
		protected MultiEditorOrderEditorList source;

		/**
		 * Creates a new MEFocusAdapter object.
		 *
		 * 
		 */
		public MEFocusAdapter(MultiEditorOrderEditorList s) {
			source = s;
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public void focusGained(FocusEvent e) {
			// first the source (maybe selection event!)
			source.focusGained(e);

			// then external listeners
			for(Iterator iter = source.focusListeners.iterator(); iter.hasNext();) {
				((FocusListener) iter.next()).focusGained(e);
			}
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public void focusLost(FocusEvent e) {
			// first external listeners
			for(Iterator iter = source.focusListeners.iterator(); iter.hasNext();) {
				((FocusListener) iter.next()).focusLost(e);
			}

			// now the source
			source.focusLost(e);
		}
	}

	private class MECaretAdapter implements CaretListener {
		protected MultiEditorOrderEditorList source;

		/**
		 * Creates a new MECaretAdapter object.
		 *
		 * 
		 */
		public MECaretAdapter(MultiEditorOrderEditorList s) {
			source = s;
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public void caretUpdate(CaretEvent e) {
			for(Iterator iter = source.caretListeners.iterator(); iter.hasNext();) {
				((CaretListener) iter.next()).caretUpdate(e);
			}
		}
	}

	// this is a very special thread to position the actual orderEditor into the viewport of the
	// scrollpane
	// In very ugly situations it is called twice, but that does not really hurt
	private class SwingGlitchThread implements Runnable {
		SwingGlitchThread() {
		}

		private int loopCounter = 0;

		/**
		 * DOCUMENT-ME
		 */
		public void run() {
			// waiting for isValid() to become true
			// does not work, because when we really
			// need it, i.e. editors are shown for the
			// first time they stay invalid.
			Rectangle viewRect = scpContent.getViewport().getViewRect();

			if(log.isDebugEnabled()) {
				log.debug("MultiEditorOrderEditorList.selectionChanged.runnable: viewRect:" +
						  viewRect);
			}

			if(getEditor(currentUnit) != null) {
				Rectangle bounds = getEditor(currentUnit).getBounds();
				log.debug("MultiEditorOrderEditorList.selectionChanged.runnable: Bounds:" + bounds);

				while(!viewRect.contains(viewRect.x, bounds.y, viewRect.width,
											 Math.min(viewRect.height, bounds.height))) {
					Point newPos = null;

					if(bounds.height < viewRect.height) {
						newPos = new Point(0, bounds.y - ((viewRect.height - bounds.height) / 2));
					} else {
						newPos = new Point(0, bounds.y);
					}

					newPos.y = Math.max(0, newPos.y);

					if(log.isDebugEnabled()) {
						log.debug("MultiEditorOrderEditorList.selectionChanged.runnable: newPos : " +
								  newPos);
					}

					Rectangle newRect = new Rectangle(viewRect);
					newRect.setLocation(newPos);

					//currentUnit.cache.orderEditor.scrollRectToVisible(newRect);
					content.scrollRectToVisible(newRect);

					// scpContent.getViewport().setViewPosition(newPos);
					viewRect = scpContent.getViewport().getViewRect();
					bounds = getEditor(currentUnit).getBounds();

					if(++loopCounter > 3) {
						loopCounter = 0;

						break;
					}
				}

				log.debug("MultiEditorOrderEditorList.selectionChanged.runnable: viewRect after:" +
						  viewRect);
				log.debug("MultiEditorOrderEditorList.selectionChanged.runnable: Bounds after:" +
						  bounds);
			}

			content.validate();
			scpContent.getViewport().invalidate();
			repaint();
		}
	}

	private class UpdateThread implements Runnable {
		/** DOCUMENT-ME */
		public CacheableOrderEditor e = null;

		/**
		 * DOCUMENT-ME
		 */
		public void run() {
			e.formatTokens();
			// this is not allowed as it may prevent storing of changed orders of a unit!
			// e.setModified(false);
		}
	}

	// for "check orders", "create temp unit" and "delete temp unit"
	protected class ButtonPanel extends JPanel implements ActionListener {
		private JCheckBox checkOrderConfirm = null;
		private JButton btnCreateTempUnit = null;
		private JButton btnDeleteTempUnit = null;
		private TempUnitDialog dialog;

		/**
		 * Creates a new ButtonPanel object.
		 */
		public ButtonPanel() {
			DesktopEnvironment.registerShortcutListener(new magellan.client.desktop.ShortcutListener() {
					List<KeyStroke> shortcuts = null;

					public Iterator<KeyStroke> getShortCuts() {
						if(shortcuts == null) {
							shortcuts = new LinkedList<KeyStroke>();
							shortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.CTRL_MASK));
							shortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_T,
																 KeyEvent.CTRL_MASK |
																 KeyEvent.SHIFT_MASK));
						}

						return shortcuts.iterator();
					}

					public void shortCut(KeyStroke s) {
						int index = shortcuts.indexOf(s);

						switch(index) {
						case 0:
							createTempUnit();

							break;

						case 1:
							deleteTempUnit();

							break;
						}
					}

					public String getListenerDescription() {
						return Resources.get("completion.multieditorordereditorlist.shortcuts.title");
					}

					public String getShortcutDescription(Object stroke) {
						int index = shortcuts.indexOf(stroke);

						return Resources.get("completion.multieditorordereditorlist.shortcuts.description." + String.valueOf(index));
					}
				});

			checkOrderConfirm = new JCheckBox(Resources.get("completion.multieditorordereditorlist.chk.orderconfirmation"), false);
			checkOrderConfirm.addActionListener(this);
			checkOrderConfirm.setEnabled(false);

			Icon icon = MagellanImages.GUI_CREATETEMPUNIT;
			btnCreateTempUnit = new JButton(icon);
			btnCreateTempUnit.addActionListener(this);
			btnCreateTempUnit.setEnabled(false);

			icon = MagellanImages.GUI_DELETETEMPUNIT;
			btnDeleteTempUnit = new JButton(icon);
			btnDeleteTempUnit.addActionListener(this);
			btnDeleteTempUnit.setEnabled(false);

			this.setLayout(new GridBagLayout());
			this.setBorder(new EmptyBorder(4, 4, 4, 4));

			GridBagConstraints c = new GridBagConstraints();

			c.anchor = GridBagConstraints.WEST;
			this.add(checkOrderConfirm, c);

			c.anchor = GridBagConstraints.CENTER;
			c.gridx = 1;
			c.weightx = 1.0;
			c.fill = GridBagConstraints.HORIZONTAL;
			this.add(new JPanel(), c);

			c.anchor = GridBagConstraints.CENTER;
			c.gridx = 2;
			this.add(btnCreateTempUnit, c);

			c.gridx = 3;
			this.add(btnDeleteTempUnit, c);

			// update the check box when the order confirmation status changes
			dispatcher.addOrderConfirmListener(new OrderConfirmListener() {
					public void orderConfirmationChanged(OrderConfirmEvent e) {
						if(!this.equals(e.getSource())) {
							for(Iterator iter = e.getUnits().iterator(); iter.hasNext();) {
								Unit u = (Unit) iter.next();

								if(u.equals(currentUnit)) {
									checkOrderConfirm.setSelected(u.isOrdersConfirmed());

									return;
								}
							}
						}
					}
				});

			// update the check box when a different unit is selected
			dispatcher.addSelectionListener(new SelectionListener() {
					public void selectionChanged(SelectionEvent e) {
						if((e.getActiveObject() != null) && e.getActiveObject() instanceof Unit) {
							Unit u = (Unit) e.getActiveObject();

							checkOrderConfirm.setSelected(u.isOrdersConfirmed());
						}
					}
				});
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public void actionPerformed(java.awt.event.ActionEvent p1) {
			if(p1.getSource() == checkOrderConfirm) {
				toggleOrderConfirmation();
			}

			if(p1.getSource() == btnCreateTempUnit) {
				createTempUnit();
			}

			if(p1.getSource() == btnDeleteTempUnit) {
				deleteTempUnit();
			}
		}

		protected void toggleOrderConfirmation() {
			if(currentUnit != null) {
				currentUnit.setOrdersConfirmed(!currentUnit.isOrdersConfirmed());
				checkOrderConfirm.setSelected(currentUnit.isOrdersConfirmed());

				List<Unit> units = new LinkedList<Unit>();

				units.add(currentUnit);
				dispatcher.fire(new OrderConfirmEvent(this, units));
			}
		}

		protected void createTempUnit() {
			if((currentUnit != null) && currentUnit.getFaction().isPrivileged()) {
				// Use the current unit as the parent or its parent if it
				// is itself a temp unit
				Unit parentUnit = currentUnit;

				if(currentUnit instanceof TempUnit) {
					parentUnit = ((TempUnit) currentUnit).getParent();
				}

				createTempImpl(parentUnit, parentUnit.getRegion());
			}
		}

		private void createTempImpl(Unit parentUnit, Region parentRegion) {
            UnitID id = UnitID.createTempID(data, settings, parentUnit);

			if(!settings.getProperty("MultiEditorOrderEditorList.ButtonPanel.ShowTempUnitDialog",
										 "true").equalsIgnoreCase("true")) {
				// don't show any dialogs, simply create the tempunit and finish.
				TempUnit tempUnit = parentUnit.createTemp(id);
				dispatcher.fire(new TempUnitEvent(this, tempUnit, TempUnitEvent.CREATED));
				dispatcher.fire(new SelectionEvent(this, null, tempUnit));
			} else {
				// do all the tempunit-dialog-stuff
				UnitID newID = UnitID.createUnitID(-id.intValue(),data.base); // unit id is non-negative on views

				if(dialog == null) {
					dialog = new TempUnitDialog((Frame) this.getTopLevelAncestor(), this, settings);
				}

				// ask the user for a valid id repeatedly
				boolean first = true;

				while(true) {
					if(first) { // reset if it's the first dialog for this temp unit
						dialog.show(newID.toString(), parentUnit.getName());
					} else { // do not reset if we had formerly wrong data
						dialog.show(parentUnit.getName());
					}

					first = false;

					if(dialog.isApproved()) {
						String tempID = dialog.getID();

						if((tempID == null) || tempID.trim().equals("")) {
							//JOptionPane.show...
							return;
						}

						try {
							int realNewIDInt = IDBaseConverter.parse(tempID,data.base);
							UnitID checkID = UnitID.createUnitID(-realNewIDInt,data.base);

							if(data.tempUnits().get(checkID) == null) {
								TempUnit tempUnit = parentUnit.createTemp(checkID);

								// Name
								String name = dialog.getName();

								if((name != null) && !name.trim().equals("")) {
									tempUnit.setName(name);
									data.getGameSpecificStuff().getOrderChanger().addNamingOrder(tempUnit,
																								 name);
								}

								// extended features
								if(dialog.wasExtendedDialog()) {
									// Recruiting
									String recruit = dialog.getRecruit();

									if(recruit != null) {
										try {
											int i = Integer.parseInt(recruit);

											if(i > 0) {
												data.getGameSpecificStuff().getOrderChanger()
													.addRecruitOrder(tempUnit, i);

												if(dialog.isGiveMaintainCost() ||
													   dialog.isGiveRecruitCost()) {
													ItemType silverType = data.rules.getItemType(StringID.create("Silber"),
																								 false);
													String silver = null;

													if(silverType != null) {
														silver = silverType.getName();
													} else {
														silver = "Silver";
													}

													if(dialog.isGiveRecruitCost()) {
														int recCost = 0;

														if(parentUnit.getRealRace() != null) {
															recCost = parentUnit.getRealRace().getRecruitmentCosts();
														} else {
															recCost = parentUnit.getRace().getRecruitmentCosts();
														}

														recCost = i * recCost;

														// TODO(pavkovic) extract to EresseaOrderChanger
														String tmpOrders = Resources.getOrderTranslation(EresseaConstants.O_GIVE)+ " "+
                                                                            tempUnit.toString(false)+
																		   " " + recCost + " " +
																		   silver + "; " +
																		   Resources.get("completion.multieditorordereditorlist.tempunit.recruitCost");
														parentUnit.addOrders(tmpOrders);
													}

													if(dialog.isGiveMaintainCost()) {
														String tmpOrders = Resources.getOrderTranslation(EresseaConstants.O_GIVE)+ " "+
														                    tempUnit.toString(false)+
																		   " " +
																		   String.valueOf(10 * i) +
																		   " " + silver + "; " +
																		   Resources.get("completion.multieditorordereditorlist.tempunit.maintainCost");
														parentUnit.addOrders(tmpOrders);
													}

													// TODO(pavkovic) extract to EresseaOrderChanger
													dispatcher.fire(new UnitOrdersEvent(this,
																						parentUnit));
												}
											}
										} catch(NumberFormatException nfe) {
										} catch(MissingResourceException mre) {
										}
									}

									// simple order
									String order = dialog.getOrder();

									if((order != null) && !order.trim().equals("")) {
										tempUnit.addOrders(order);
									}

									// description
									String descript = dialog.getDescript();

									if((descript != null) && !descript.trim().equals("")) {
										descript = descript.replace('\n', ' ');
										tempUnit.setDescription(descript);
										data.getGameSpecificStuff().getOrderChanger()
											.addDescribeUnitOrder(tempUnit, descript);
									}
								}

								// data update
								dispatcher.fire(new TempUnitEvent(this, tempUnit,
																  TempUnitEvent.CREATED), true);
                selectEditor(tempUnit);
                if (getEditor(tempUnit)!=null)
                  getEditor(tempUnit).requestFocus();
                dispatcher.fire(new SelectionEvent(this, null, tempUnit));
								return;
							} else {
								JOptionPane.showMessageDialog(this,
															  Resources.get("completion.multieditorordereditorlist.msg.duplicatetempid.text"),
															  Resources.get("completion.multieditorordereditorlist.msg.duplicatetempid.title"),
															  JOptionPane.ERROR_MESSAGE);
							}
						} catch(NumberFormatException nfe) {
							JOptionPane.showMessageDialog(this,
														  Resources.get("completion.multieditorordereditorlist.msg.invalidtempid.text"),
														  Resources.get("completion.multieditorordereditorlist.msg.invalidtempid.title"),
														  JOptionPane.ERROR_MESSAGE);
						}
					} else {
						// dialog canceled
						return;
					}
				}
			}
		}

		protected void deleteTempUnit() {
			if((currentUnit != null) && currentUnit instanceof TempUnit) {
        TempUnit tempUnit = (TempUnit) currentUnit;
				Unit parentUnit = tempUnit.getParent();
				selectEditor(parentUnit);
        if (getEditor(currentUnit)!=null)
          getEditor(currentUnit).requestFocus();
        dispatcher.fire(new SelectionEvent(this, null, parentUnit), true);
        dispatcher.fire(new TempUnitEvent(this, tempUnit,
            TempUnitEvent.DELETING), true);
				parentUnit.deleteTemp(tempUnit.getID(), data);
			}
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public void setConfirmEnabled(boolean enabled) {
			checkOrderConfirm.setEnabled(enabled);
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public void setCreationEnabled(boolean enabled) {
			btnCreateTempUnit.setEnabled(enabled);
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public void setDeletionEnabled(boolean enabled) {
			btnDeleteTempUnit.setEnabled(enabled);
		}

		/**
		 * DOCUMENT-ME
		 */
		public void currentUnitChanged() {
			boolean enabled = (currentUnit != null) && currentUnit.getFaction().isPrivileged();

			setConfirmEnabled(enabled);
			setCreationEnabled(enabled);
			setDeletionEnabled(enabled && (currentUnit instanceof TempUnit));
		}
	}

	/**
	 * A simple JPanel that implements the Scrollable interface and is used to hold the order
	 * editors.
	 */
	private class ScrollPanel extends JPanel implements Scrollable {
		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public Dimension getPreferredScrollableViewportSize() {
			return this.getPreferredSize();
		}

		/**
		 * If the parent of this component is an instance of JViewport this method returns the
		 * maximum of the original preferred size and the viewport size.
		 *
		 * 
		 */
		public Dimension getPreferredSize() {
			Container parent = this.getParent();

			// this special case has become necessary with the
			// implementation of the Scrollable interface
			if(parent instanceof JViewport) {
				JViewport viewport = (JViewport) parent;
				Dimension psize = super.getPreferredSize();
				psize.width = Math.max(psize.width, viewport.getWidth());
				psize.height = Math.max(psize.height, viewport.getHeight());

				return psize;
			} else {
				return super.getPreferredSize();
			}
		}


      
    
		/**
		 * DOCUMENT-ME
		 *
		 * 
		 * 
		 * 
		 *
		 * 
		 */
		public int getScrollableUnitIncrement(java.awt.Rectangle visibleRect, int orientation,
											  int direction) {
			if(orientation == SwingConstants.HORIZONTAL) {
				return visibleRect.width / 5;
			} else {
				Component lastVisibleComponent = null;
				Component nextComponent = null;

				if(direction < 0) { // up
					lastVisibleComponent = this.getComponentAt(visibleRect.x, visibleRect.y);

					if(lastVisibleComponent != null) {
						if(visibleRect.y > lastVisibleComponent.getY()) {
							// component is not fully visible
							return visibleRect.y - lastVisibleComponent.getY();
						} else {
							// component is fully visible, get next component
							List components = Arrays.asList(this.getComponents());
							int count = components.indexOf(lastVisibleComponent) - 1;

							if((count >= 0) && (count < this.getComponentCount())) {
								nextComponent = this.getComponent(count);

								return nextComponent.getHeight();
							}
						}
					}
				} else { // down
					lastVisibleComponent = this.getComponentAt(visibleRect.x,
															   visibleRect.y + visibleRect.height);

					if(lastVisibleComponent != null) {
						if((visibleRect.y + visibleRect.height) < (lastVisibleComponent.getY() +
							   lastVisibleComponent.getHeight())) {
							// component is not fully visible
							return (lastVisibleComponent.getY() + lastVisibleComponent.getHeight()) -
								   visibleRect.y - visibleRect.height;
						} else {
							// component is fully visible, get next component
							List components = Arrays.asList(this.getComponents());
							int count = components.indexOf(lastVisibleComponent) + 1;

							if((count >= 0) && (count < this.getComponentCount())) {
								nextComponent = this.getComponent(count);

								return nextComponent.getHeight();
							}
						}
					}
				}

				// fallback:
				return visibleRect.height / 5;
			}
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 * 
		 * 
		 *
		 * 
		 */
		public int getScrollableBlockIncrement(java.awt.Rectangle visibleRect, int orientation,
											   int direction) {
			if(orientation == SwingConstants.HORIZONTAL) {
				return visibleRect.width;
			} else {
				return visibleRect.height;
			}
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public boolean getScrollableTracksViewportWidth() {
			return false;
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public boolean getScrollableTracksViewportHeight() {
			return false;
		}
	}

}
