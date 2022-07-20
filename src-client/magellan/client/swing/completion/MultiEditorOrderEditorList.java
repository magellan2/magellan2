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
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
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

import magellan.client.EMapOverviewPanel;
import magellan.client.completion.AutoCompletion;
import magellan.client.desktop.DesktopEnvironment;
import magellan.client.desktop.ShortcutListener;
import magellan.client.event.EventDispatcher;
import magellan.client.event.OrderConfirmEvent;
import magellan.client.event.OrderConfirmListener;
import magellan.client.event.SelectionEvent;
import magellan.client.event.SelectionListener;
import magellan.client.event.TempUnitEvent;
import magellan.client.event.TempUnitListener;
import magellan.client.event.UnitOrdersEvent;
import magellan.client.preferences.DetailsViewOrderEditorPreferences;
import magellan.client.swing.DialogProvider;
import magellan.client.swing.InternationalizedDataPanel;
import magellan.client.swing.TempUnitDialog;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.client.utils.Colors;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Group;
import magellan.library.Island;
import magellan.library.Region;
import magellan.library.TempUnit;
import magellan.library.Unit;
import magellan.library.UnitID;
import magellan.library.completion.OrderParser;
import magellan.library.event.GameDataEvent;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.utils.Cache;
import magellan.library.utils.CacheHandler;
import magellan.library.utils.IDBaseConverter;
import magellan.library.utils.MagellanImages;
import magellan.library.utils.PropertiesHelper;
import magellan.library.utils.Resources;
import magellan.library.utils.TrustLevels;
import magellan.library.utils.guiwrapper.CacheableOrderEditor;
import magellan.library.utils.logging.Logger;

/**
 * A panel holding one or more {@link OrderEditor}s.
 */
public class MultiEditorOrderEditorList extends InternationalizedDataPanel implements
    OrderEditorList, SelectionListener, TempUnitListener, MouseListener, CacheHandler {
  private static final Logger log = Logger.getInstance(MultiEditorOrderEditorList.class);

  private boolean multiEditorLayout = false;
  private boolean hideButtons = false;
  private boolean editAllFactions = false;
  private SortedSet<Unit> units;

  // currently selected entities
  private Unit currentUnit = null;

  private Color errorBgColor = null;

  private Color standardBgColor = null;
  private Color activeBgColor = null;
  private Color standardBgColorConfirmed = null;
  private Color activeBgColorConfirmed = null;
  private OrderEditor editorSingelton = null;
  private static final Border standardBorder = new LineBorder(Color.lightGray, 1);
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
  protected ScrollPanel content; // a Panel implementing the scrollable
  // interface

  // protected JPanel content; // a Panel implementing the scrollable interface
  protected JScrollPane scpContent = null;

  // the buttons for temp units etc.
  protected ButtonPanel buttons;

  // editor list generation mode
  protected int listMode = 1 << MultiEditorOrderEditorList.LIST_REGION;
  /** List only editor for current unit */
  public static final int LIST_UNIT = 0;
  /** List editors for currently selected element's faction (if applicable) */
  public static final int LIST_FACTION = 1;
  /** List editors for currently selected element's region (if applicable) */
  public static final int LIST_REGION = 2;
  /** List editors for currently selected element's island (if applicable) */
  public static final int LIST_ISLAND = 3;

  private AutoCompletion completion;

  private OrderParser parser;

  private boolean guiInitialized = false;

  public DialogProvider dialogProvider;

  /**
   * Creates a new MultiEditorOrderEditorList object.
   */
  public MultiEditorOrderEditorList(EventDispatcher dispatcher, GameData data, Properties settings,
      UndoManager undoMgr, DialogProvider dp) {
    super(dispatcher, data, settings);

    // beware: an OrderParser is likely not thread-safe. so we better call it only from the
    // EventDispatchThread...
    parser = data.getOrderParser();

    loadListProperty();

    dialogProvider = dp;
    dispatcher.addTempUnitListener(this);

    keyAdapter = new MEKeyAdapter(this);
    caretAdapter = new MECaretAdapter(this);
    focusAdapter = new MEFocusAdapter(this);

    this.undoMgr = undoMgr;
    initGUI();
    initContent();
    // startTimer();
  }

  private void initGUI() {
    readSettings();

    units = new TreeSet<Unit>(EMapOverviewPanel.getUnitSorting(settings)); // new
    // ArrayList<Unit>();
    content = new ScrollPanel();

    // content = new JPanel();
    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
    scpContent = new JScrollPane(content);

    // ClearLook suggests to remove the border
    scpContent.setBorder(null);

    addFocusListener(new FocusListener() {
      public void focusLost(FocusEvent e) {
        //
      }

      public void focusGained(FocusEvent e) {
        requestEditorFocus();
      }
    });

    redrawPane();

    if (!multiEditorLayout) {
      initSingleEditor();
    }

    // update the color of the editor when the order confirmation status changes
    dispatcher.addOrderConfirmListener(new OrderConfirmListener() {
      public void orderConfirmationChanged(OrderConfirmEvent e) {
        if (!equals(e.getSource())) {
          for (Unit u : e.getUnits()) {
            if (getEditor(u) != null) {
              if (u.equals(currentUnit)) {
                // u is active unit
                if (u.isOrdersConfirmed()) {
                  getEditor(u).setBackground(activeBgColorConfirmed);
                } else {
                  getEditor(u).setBackground(activeBgColor);
                }
              } else {
                if (u.isOrdersConfirmed()) {
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
    guiInitialized = true;
  }

  /**
   * @see magellan.client.swing.InternationalizedDataPanel#gameDataChanged(magellan.library.event.GameDataEvent)
   */
  @Override
  public void gameDataChanged(GameDataEvent e) {
    setGameData(e.getGameData());
  }

  @Override
  public void setGameData(GameData data) {
    super.setGameData(data);
    if (guiInitialized) { // might be called from constructor
      parser = data.getOrderParser();
      initContent();
      if (!multiEditorLayout) {
        initSingleEditor();
      }
    }
  }

  /**
   *
   */
  private void initContent() {
    clearUnits();
    currentUnit = null;
  }

  private void readSettings() {
    multiEditorLayout =
        Boolean.valueOf(
            settings.getProperty(PropertiesHelper.ORDEREDITOR_MULTIEDITORLAYOUT, Boolean.TRUE
                .toString())).booleanValue();
    hideButtons =
        Boolean.valueOf(
            settings
                .getProperty(PropertiesHelper.ORDEREDITOR_HIDEBUTTONS, Boolean.FALSE.toString()))
            .booleanValue();
    editAllFactions =
        Boolean.valueOf(
            settings.getProperty(PropertiesHelper.ORDEREDITOR_EDITALLFACTIONS, Boolean.FALSE
                .toString())).booleanValue();
    activeBgColor =
        Colors.decode(settings.getProperty("OrderEditor.activeBackgroundColor", "255,255,255"));
    standardBgColor =
        Colors.decode(settings.getProperty("OrderEditor.standardBackgroundColor", "228,228,228"));
    activeBgColorConfirmed =
        Colors.decode(settings.getProperty("OrderEditor.activeBackgroundColorConfirmed",
            "255,255,102"));
    standardBgColorConfirmed =
        Colors.decode(settings.getProperty("OrderEditor.standardBackgroundColorConfirmed",
            "255,204,0"));
    errorBgColor = Colors.decode(settings.getProperty("OrderEditor.errorBgColor", "255,128,0"));
  }

  // private boolean swingGlitch =false;

  /**
   * @see javax.swing.JComponent#paint(java.awt.Graphics)
   */
  @Override
  public void paint(Graphics g) {
    super.paint(g);

    // stm: this does not make sense any more
    // if (MultiEditorOrderEditorList.log.isDebugEnabled()) {
    // MultiEditorOrderEditorList.log.debug("paint! [" + swingGlitch + "]");
    // }
    //
    // // we are in a situation AFTER painting (hopefully!)
    // if (swingGlitch) {
    // swingGlitch = false;
    // SwingUtilities.invokeLater(swingGlitchThread);
    // }
  }

  /**
   * Updates the editors after a new object has been selected.
   */
  public void selectionChanged(SelectionEvent se) {
    if (MultiEditorOrderEditorList.log.isDebugEnabled()) {
      MultiEditorOrderEditorList.log.debug("MultiEditorOrderEditorList.selectionChanged: "
          + se.getActiveObject());

      if (se.getActiveObject() != null) {
        MultiEditorOrderEditorList.log.debug("MultiEditorOrderEditorList.selectionChanged: "
            + se.getActiveObject().getClass());
      }

      MultiEditorOrderEditorList.log.debug("MultiEditorOrderEditorList.selectionChanged: "
          + (se.getSource() == this));
    }

    // remember if we want to have the focus (see below)
    boolean restoreFocus =
        (getEditor(currentUnit) != null && getEditor(currentUnit).hasFocus()) || content.hasFocus()
            || hasFocus();
    // if WE triggered the selection change, the new unit DOES get the focus
    restoreFocus =
        restoreFocus || (se.getSource() == this)
            || (se.getSource().getClass().equals(OrderEditor.class));

    if (multiEditorLayout) {
      deselectEditor(currentUnit);
      loadEditors(se);

    } else {
      // single editor mode
      Object activeObject = se.getActiveObject();
      if (activeObject instanceof Unit
          && (magellan.library.utils.Units.isPrivilegedAndNoSpy((Unit) activeObject)
              || isEditAllFactions())) {
        // update editor
        currentUnit = (Unit) activeObject;
        attachOrderEditor(currentUnit, editorSingelton);
        selectEditor(currentUnit, editorSingelton);
      } else {
        // no unit selected --> no editor active
        if (currentUnit != null) {
          currentUnit.setOrderEditor(null);
          deselectEditor(editorSingelton);
          editorSingelton.setBorder(getBorder(null, false));
          currentUnit = null;
          editorSingelton.setUnit(null);
          editorSingelton.setEditable(false);
        }
      }
    }

    // Restore focus. If we had the focus before, we want to ensure that the new
    // current editor
    // gets the focus.
    if (restoreFocus) {
      requestEditorFocus();
    }

    // update button state
    getButtonPanel().currentUnitChanged();
  }

  /**
   * Ensures that the correct editors are loaded and selected for the active object.
   *
   * @param activeObject
   */
  private void loadEditors(SelectionEvent se) {
    List<Object> context = null;
    if (se.getActiveObject() != null) {
      context = se.getContexts().iterator().next();
    }

    Island newIsland = null;
    Region newRegion = null;
    Faction newFaction = null;
    Object activeObject = null;
    if (context != null) {
      for (Object o : context) {
        if (o instanceof Island) {
          newIsland = (Island) o;
        }
        if (o instanceof Region) {
          newRegion = (Region) o;
        }
        if (o instanceof Faction) {
          newFaction = (Faction) o;
        }

        activeObject = o;
      }
    }

    Unit newUnit = null;
    if (activeObject != null) {
      if (activeObject instanceof Unit
          && (magellan.library.utils.Units.isPrivilegedAndNoSpy((Unit) activeObject)
              || isEditAllFactions())) {
        newUnit = (Unit) activeObject;
        newRegion = newUnit.getRegion();
        newIsland = newRegion == null ? null : newRegion.getIsland();
        newFaction = newUnit.getFaction();
      } else if (activeObject instanceof Region) {
        newRegion = (Region) activeObject;
        newIsland = newRegion.getIsland();
      } else if (activeObject instanceof Faction) {
        newFaction = (Faction) activeObject;
      } else if (activeObject instanceof Group) {
        newFaction = ((Group) activeObject).getFaction();
      } else if (activeObject instanceof Island) {
        newIsland = (Island) activeObject;
      } else { // some other node or non privileged unit selected
        // newUnit = null;
        newIsland = null;
        newRegion = null;
        newFaction = null;
      }
    }

    if ((newUnit == currentUnit && newUnit != null)
        || (newUnit != null && currentUnit != null
            && newUnit.getRegion() == currentUnit.getRegion() && newUnit.getFaction() == currentUnit
                .getFaction())) {
      // no change necessary
    } else {
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
   */
  private void selectEditor(Unit newUnit) {
    if (getEditor(currentUnit) != null) {
      deselectEditor(currentUnit);
    }
    if (newUnit != null) {
      selectEditor(newUnit, getEditor(newUnit));
    }
    currentUnit = newUnit;
    if (newUnit != null) {
      // this.currentFaction=newUnit.getFaction();
      // this.currentIsland=newUnit.getRegion().getIsland();
      // this.currentRegion=newUnit.getRegion();
    }
  }

  /**
   * Sets appearance of an editor acording to the state of a unit.
   *
   * @param newUnit Must not be <code>null</code>.
   * @param editor
   */
  private void selectEditor(Unit newUnit, OrderEditor editor) {
    if (newUnit == null)
      throw new NullPointerException();
    if (editor != null) {
      editor.setBorder(getBorder(newUnit, true));

      if (newUnit.isOrdersConfirmed()) {
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
      editor.formatTokens();
    }
  }

  /**
   * Reset a unit's editor border to normal.
   *
   * @param selectedUnit
   */
  private void deselectEditor(Unit selectedUnit) {
    if (getEditor(selectedUnit) != null) {
      deselectEditor(getEditor(selectedUnit));
    }
  }

  /**
   * Reset editor border to normal.
   *
   * @param editor Must not be <code>null</code>.
   */
  private void deselectEditor(OrderEditor editor) {
    editor.setBorder(getBorder(currentUnit, false));

    if (currentUnit.isOrdersConfirmed()) {
      editor.setBackground(standardBgColorConfirmed);
    } else {
      editor.setBackground(standardBgColor);
    }

    // deactivate visibility call
    editor.setKeepVisible(false);

    // make the old editor do the syntax highlighting again later
    if (editor.isModified()) {
      updateThread.e = editor;
      SwingUtilities.invokeLater(updateThread);
    }
  }

  /**
   * Ensures that the correct editors are loaded if a temp unit was created.
   */
  public void tempUnitCreated(TempUnitEvent e) {
    if (MultiEditorOrderEditorList.log.isDebugEnabled()) {
      MultiEditorOrderEditorList.log.debug("MultiEditorOrderEditorList.tempUnitCreated: "
          + e.getTempUnit());
    }

    if ((currentUnit != null) && multiEditorLayout) {
      addUnit(e.getTempUnit());
      // loadEditors(currentUnit.getRegion().getIsland(),
      // currentUnit.getRegion(), currentUnit.getFaction(), currentUnit);

      revalidate();

      if (MultiEditorOrderEditorList.log.isDebugEnabled()) {
        MultiEditorOrderEditorList.log.debug("MultiEditorOrderEditorList.tempUnitCreated: "
            + e.getTempUnit());
        MultiEditorOrderEditorList.log.debug("MultiEditorOrderEditorList.tempUnitCreated: "
            + getEditor(e.getTempUnit()));
      }

      // if(getEditor(e.getTempUnit()) != null) {
      // // pavkovic 2002.02.15: here we don't request focus in an invokeLater
      // runnable
      // // because this would lead to intense focus change between parent and
      // temp unit.
      // getEditor(e.getTempUnit()).requestFocus();
      // //requestFocus(e.getTempUnit().cache.orderEditor);
      // }
    }
  }

  /**
   * Ensures that the right editors are loaded if a temp unit was created.
   */
  public void tempUnitDeleting(TempUnitEvent e) {
    if (multiEditorLayout) {
      removeUnit(e.getTempUnit(), null);
      revalidate();
    }
  }

  /**
   * Processes CTRL+UP/DOWN keys.
   */
  public void keyPressed(KeyEvent e) {
    if (e.isControlDown()) {
      if (e.getKeyCode() == KeyEvent.VK_SPACE) {
        getCompleter().offerCompletion(getCurrentEditor());
      }
      if (multiEditorLayout) {
        switch (e.getKeyCode()) {
        case KeyEvent.VK_DOWN:
          Unit u = getNextUnit();
          if (u != null) {
            /*
             * refreshing the relations here is necessary because this is the only place where a
             * different unit is selected and the focusLost event in the order editor does not occur
             * before the selection event
             */
            if (getEditor(currentUnit) != null && getEditor(currentUnit).isModified()) {
              currentUnit.refreshRelations();
            }

            dispatcher.fire(SelectionEvent.create(this, u));
          }

          break;

        case KeyEvent.VK_UP:
          u = getPreviousUnit();
          if (u != null) {
            /*
             * refreshing the relations here is necessary because this is the only place where a
             * different unit is selected and the focusLost event in the order editor does not occur
             * before the selection event
             */
            if (getEditor(currentUnit) != null && getEditor(currentUnit).isModified()) {
              currentUnit.refreshRelations();
            }

            dispatcher.fire(SelectionEvent.create(this, u));
          }

          break;
        }
      }
    }
  }

  /**
   * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
   */
  public void keyReleased(KeyEvent e) {
    // do not react
  }

  /**
   * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
   */
  public void keyTyped(KeyEvent e) {
    // do not react
  }

  /**
   * Show a context menu on right-click.
   *
   * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
   */
  public void mouseClicked(MouseEvent e) {
    // maybeShowPopup(e);
  }

  /**
   * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
   */
  public void mouseEntered(MouseEvent e) {
    // do not react
  }

  /**
   * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
   */
  public void mouseExited(MouseEvent e) {
    // do not react
  }

  /**
   * Selects the editor in multi editor layout.
   *
   * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
   */
  public void mousePressed(MouseEvent e) {
    if (multiEditorLayout) {
      OrderEditor editor = (OrderEditor) e.getSource();
      // rightclick does select too(Fiete)
      dispatcher.fire(SelectionEvent.create(e.getSource(), editor.getUnit()));
    }
    maybeShowPopup(e);
  }

  /**
   * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
   */
  public void mouseReleased(MouseEvent e) {
    maybeShowPopup(e);
  }

  private void maybeShowPopup(MouseEvent e) {
    if (e.getSource() instanceof OrderEditor) {
      OrderEditor editor = (OrderEditor) e.getSource();
      if (e.isPopupTrigger()) {
        Unit u = editor.getUnit();
        if (currentUnit != null) {
          if (u != null) {
            JPopupMenu unitContextMenu =
                editor.getContextFactory().createContextMenu(dispatcher, getGameData(), u, null,
                    null);
            if (unitContextMenu != null) {
              unitContextMenu.show(this, e.getX(), e.getY());
            }
          }
        }
      }
    }
  }

  /**
   * @see magellan.library.utils.CacheHandler#clearCache(magellan.library.utils.Cache)
   */
  public void clearCache(Cache c) {
    if (c.orderEditor != null) {
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
   */
  public boolean getUseSyntaxHighlighting() {
    return PropertiesHelper.getBoolean(settings, "OrderEditor.highlightSyntax", true);
  }

  /**
   * Enable or disable syntax highlighting in the editor.
   */
  public void setUseSyntaxHighlighting(boolean bool) {
    settings.setProperty("OrderEditor.highlightSyntax", String.valueOf(bool));

    if (multiEditorLayout) {
      if (getGameData().getUnits() != null) {
        for (Unit u : getGameData().getUnits()) {
          if (getEditor(u) != null) {
            getEditor(u).setUseSyntaxHighlighting(bool);
          }
        }
      }
    } else {
      editorSingelton.setUseSyntaxHighlighting(bool);
    }
  }

  /**
   * Return the color of the specified token style used for syntax highlighting in the editor.
   */
  public Color getTokenColor(String styleName) {
    return Colors.decode(settings.getProperty("OrderEditor.styles." + styleName + ".color",
        OrderEditor.getDefaultColor(styleName)));
  }

  /**
   * Set the color of the specified token style used for syntax highlighting in the editor.
   */
  public void setTokenColor(String styleName, Color color) {
    settings.setProperty("OrderEditor.styles." + styleName + ".color", Colors.encode(color));

    if (multiEditorLayout) {

      if (getGameData().getUnits() != null) {
        for (Unit u : getGameData().getUnits()) {
          if (getEditor(u) != null) {
            getEditor(u).setTokenColor(styleName, color);
          }
        }
      }

    } else {
      editorSingelton.setTokenColor(styleName, color);
    }
  }

  /**
   * @return Background color of orders with errors
   */
  public Color getErrorBackground() {
    return errorBgColor;
  }

  /**
   * Set Background color of orders with errors.
   *
   * @param c
   */
  public void setErrorBackground(Color c) {
    if ((errorBgColor != c) && (c != null)) {
      errorBgColor = c;
      settings.setProperty("OrderEditor.errorBgColor", Colors.encode(c));
    }

    if (multiEditorLayout) {
      if (getGameData().getUnits() != null) {
        for (Unit u : getGameData().getUnits()) {
          if (getEditor(u) != null) {
            getEditor(u).setErrorBackround(c);
          }
        }
      }

    } else {
      editorSingelton.setErrorBackround(c);
    }
  }

  /**
   * Returns the background color for editors.
   */
  public Color getStandardBackgroundColor() {
    return standardBgColor;
  }

  /**
   * Changes the background color for all editors.
   */
  public void setStandardBackgroundColor(Color c) {
    if ((standardBgColor != c) && (c != null)) {
      standardBgColor = c;
      settings.setProperty("OrderEditor.standardBackgroundColor", Colors.encode(c));
    }

    if (multiEditorLayout) {
      if (getGameData().getUnits() != null) {
        for (Unit u : getGameData().getUnits()) {
          if (getEditor(u) != null) {
            if (!u.isOrdersConfirmed() && u != currentUnit) {
              getEditor(u).setBackground(c);
            }
          }
        }
      }
    }
  }

  /**
   * Returns the background color for confirmed units' editors.
   */
  public Color getStandardBackgroundColorConfirmed() {
    return standardBgColorConfirmed;
  }

  /**
   * Sets and applies the background color for confirmed units' editors for all editors.
   */
  public void setStandardBackgroundColorConfirmed(Color c) {
    if ((standardBgColorConfirmed != c) && (c != null)) {
      standardBgColorConfirmed = c;
      settings.setProperty("OrderEditor.standardBackgroundColorConfirmed", Colors.encode(c));
    }

    if (multiEditorLayout) {
      if (getGameData().getUnits() != null) {
        for (Unit u : getGameData().getUnits()) {
          OrderEditor area = getEditor(u);
          if (area != null) {
            if (u.isOrdersConfirmed() && u != currentUnit) {
              area.setBackground(c);
            }
          }
        }
      }
    }
  }

  /**
   * Returns the color for the active unconfirmed unit's editor.
   */
  public Color getActiveBackgroundColor() {
    return activeBgColor;
  }

  /**
   * Sets and applies the color for the active unconfirmed unit's editor.
   */
  public void setActiveBackgroundColor(Color c) {
    if ((activeBgColor != c) && (c != null)) {
      activeBgColor = c;
      settings.setProperty("OrderEditor.activeBackgroundColor", Colors.encode(c));
    }
    if (currentUnit != null && getEditor(currentUnit) != null && !currentUnit.isOrdersConfirmed()) {
      getEditor(currentUnit).setBackground(c);
    }
  }

  /**
   * Returns the color for the active confirmed unit's editor.
   */
  public Color getActiveBackgroundColorConfirmed() {
    return activeBgColorConfirmed;
  }

  /**
   * Sets and applies the color for the active confirmed unit's editor.
   */
  public void setActiveBackgroundColorConfirmed(Color c) {
    if ((activeBgColorConfirmed != c) && (c != null)) {
      activeBgColorConfirmed = c;
      settings.setProperty("OrderEditor.activeBackgroundColorConfirmed", Colors.encode(c));
    }
    if (currentUnit != null && getEditor(currentUnit) != null && currentUnit.isOrdersConfirmed()) {
      getEditor(currentUnit).setBackground(c);
    }
  }

  /**
   * @return <code>true</code> iff currently in multi editor layout
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
    if (multi != multiEditorLayout) {
      settings.setProperty(PropertiesHelper.ORDEREDITOR_MULTIEDITORLAYOUT, String.valueOf(multi));
      clearUnits();
      synchronized (content) {
        if (multi && (editorSingelton != null)) {
          // if before there was only one editor and now we
          // switch to multi editor layout
          content.removeAll();
          removeListeners(editorSingelton);
          editorSingelton = null;
        } else {
          initSingleEditor();
        }
      }

      multiEditorLayout = multi;
      repaint();

    }
  }

  private void initSingleEditor() {
    editorSingelton = new OrderEditor(getGameData(), settings, undoMgr, dispatcher, parser);
    editorSingelton.setCursor(new Cursor(Cursor.TEXT_CURSOR));

    // add listeners
    addListeners(editorSingelton);
    content.removeAll();
    content.add(editorSingelton);
  }

  /**
   * Returns <code>true</code> if temp/confirmation buttons should be shown.
   */
  public boolean isHideButtons() {
    return hideButtons;
  }

  /**
   * Sets and applies if temp/confirmation buttons should be shown.
   */
  public void setHideButtons(boolean bool) {
    if (bool != hideButtons) {
      settings.setProperty(PropertiesHelper.ORDEREDITOR_HIDEBUTTONS, String.valueOf(bool));
      hideButtons = bool;
      redrawPane();
    }
  }

  private void redrawPane() {
    remove(scpContent);
    remove(getButtonPanel());
    setLayout(new BorderLayout());
    add(scpContent, BorderLayout.CENTER);

    if (!hideButtons) {
      add(getButtonPanel(), BorderLayout.SOUTH);
    }

    repaint();
  }

  /**
   * Removes the Adapters for Key-, Caret- & Focusevents from the given JTextComponents.
   */
  private void removeListeners(JTextComponent j) {
    j.removeFocusListener(focusAdapter);
    j.removeKeyListener(keyAdapter);
    j.removeCaretListener(caretAdapter);
    j.removeMouseListener(this);
    if (j instanceof OrderEditor) {
      OrderEditor editor = (OrderEditor) j;
      editor.releaseListener();
    }
  }

  /**
   * Removes the Adapters for Key-, Caret- & Focusevents from all sub-components that are
   * JTextComponents.
   */
  private void removeListenersFromAll() {
    Component c[] = content.getComponents();

    if ((c != null) && (c.length > 0)) {
      for (Component element : c) {
        if (element instanceof JTextComponent) {
          removeListeners((JTextComponent) element);
        }
      }
    }
  }

  /**
   * Adds the Adapters for Key-, Caret- & Focusevents to the given JTextComponents.
   */
  private void addListeners(OrderEditor j) {
    j.addFocusListener(focusAdapter);
    j.addKeyListener(keyAdapter);
    j.addCaretListener(caretAdapter);
    j.addMouseListener(this);
    j.registerListener();
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
    List<Unit> unitList = new LinkedList<Unit>();
    if (!isListMode(MultiEditorOrderEditorList.LIST_FACTION)) {
      f = null;
    }
    if (isListMode(MultiEditorOrderEditorList.LIST_ISLAND)) {
      // list units of specified Island
      for (Region r : i.regions()) {
        for (Unit u : r.units()) {
          if ((isEditAllFactions() || magellan.library.utils.Units.isPrivilegedAndNoSpy(u))
              && (f == null || u.getFaction().equals(f))) {
            unitList.add(u);
          }
        }
      }
    }

    loadEditors(unitList);
  }

  /**
   * Load editors belonging to the specified region and faction depending on listMode.
   *
   * @param r
   * @param f
   */
  private void loadEditors(Region r, Faction f) {
    if (!isListMode(MultiEditorOrderEditorList.LIST_REGION) && r.getIsland() != null) {
      if (isListMode(MultiEditorOrderEditorList.LIST_ISLAND)) {
        loadEditors(r.getIsland(), f);
      } else {
        loadEditors(Collections.<Unit> emptyList());
      }
      return;
    }
    if (!isListMode(MultiEditorOrderEditorList.LIST_FACTION)) {
      f = null;
    }

    boolean filter = f != null || !isEditAllFactions();
    List<Unit> l = new ArrayList<Unit>(filter ? 0 : r.units().size());

    for (Unit u : r.units()) {
      if ((isEditAllFactions() || magellan.library.utils.Units.isPrivilegedAndNoSpy(u))
          && (f == null || f.equals(u.getFaction()))) {
        l.add(u);
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
    if (!(isEditAllFactions() || magellan.library.utils.Units.isPrivilegedAndNoSpy(u))) {
      loadEditors(Collections.<Unit> emptyList());
      return;
    }

    if (isListMode(MultiEditorOrderEditorList.LIST_REGION)) {
      if (isListMode(MultiEditorOrderEditorList.LIST_FACTION)) {
        loadEditors(u.getRegion(), u.getFaction());
      } else {
        loadEditors(u.getRegion(), (Faction) null);
      }
    } else if (isListMode(MultiEditorOrderEditorList.LIST_ISLAND)) {
      if (isListMode(MultiEditorOrderEditorList.LIST_FACTION)) {
        loadEditors(u.getRegion().getIsland(), u.getFaction());
      } else {
        loadEditors(u.getRegion().getIsland(), (Faction) null);
      }
    } else {
      loadEditors(Collections.singletonList(u));
    }
  }

  /**
   * Loads and displays the editors that best correspond to the selected entities. This depends on
   * the current {@link MultiEditorOrderEditorList#listMode}. <code>
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
    if (newUnit != null) {
      loadEditors(newUnit);
    } else if (newRegion != null && newFaction != null) {
      loadEditors(newRegion, newFaction);
    } else if (newRegion != null) { // newFaction == null
      loadEditors(newRegion, (Faction) null);
    } else if (newIsland != null) {
      loadEditors(newIsland, newFaction);
    } else {
      loadEditors(Collections.<Unit> emptyList());
    }
    // currentIsland = newIsland;
    // currentRegion = newRegion;
    // currentFaction = newFaction;
    currentUnit = newUnit;
  }

  /**
   * Adds editors for all units in privileged factions that are in the specified list to this
   * component and removes unused ones. OrderEditors are created if necessary, else the cached
   * version are used.
   */
  private void loadEditors(List<Unit> unitsToShow) {
    clearUnits();
    for (Unit u : unitsToShow) {
      units.add(u);
    }
    for (Unit u : units) {
      OrderEditor editor = buildOrderEditor(u);
      content.add(editor);
      addListeners(editor);
    }

    invalidate();
    validate();
  }

  /**
   * Returns the cached editor for a unit.
   *
   * @param u A unit, may be <code>null</code>.
   * @return The cached editor for <code>u</code> or null if none exists
   */
  private OrderEditor getEditor(Unit u) {
    if (u != null) {
      CacheableOrderEditor ce = u.getOrderEditor();
      if (ce != null) {
        if (ce instanceof OrderEditor)
          return (OrderEditor) ce;
        else {
          MultiEditorOrderEditorList.log.error("wrong type of editor!");
        }
      }
    }
    return null;
  }

  // /**
  // * Sets the cached editor of the specified unit.
  // *
  // * @param u
  // * The unit, not <code>null</code>.
  // * @param editor
  // * The new editor or <code>null</code> to dispose the currently
  // * cached editor of <code>u</code>.
  // */
  // private void setEditor(Unit u, CacheableOrderEditor editor) {
  // if(u.hasCache() || editor==null)
  // u.setOrderEditor(editor);
  // }

  /**
   * Removes the specified unit from the content. This includes removing editor and listeners for it
   * and the unit itself from <code>units</code>.
   *
   * @param u
   * @param it An iterator on <code>units</code>. If not <code>null</code>, the unit is removed via
   *          this iterator, if <code>null</code> it is removed via <code>units.remove</code>.
   */
  private void removeUnit(Unit u, Iterator<Unit> it) {
    if (MultiEditorOrderEditorList.log.isDebugEnabled()) {
      MultiEditorOrderEditorList.log.debug("MultiEditor.removeUnit  " + u);
    }
    JTextComponent c = getEditor(u);
    synchronized (content) {
      if (c == null) {
        MultiEditorOrderEditorList.log.error(this.getClass() + ": unit already removed; " + u);
      } else {
        content.remove(c);
        removeListeners(c);
      }
      if (it != null) {
        it.remove();
      } else {
        units.remove(u);
      }
      if (content.getComponentCount() == 0 && content.hasFocus()) {
        this.requestFocus();
      }
    }
  }

  /**
   * Adds the specified unit to the content. This includes creating and adding a new order editor
   * and updating the internal data structures. If possible, the new editor is inserted at the right
   * position in the unit order, else it is inserted at the end.
   *
   * @param u The unit for the new editor.
   */
  private void addUnit(Unit u) {
    if (!units.contains(u)) {
      if (MultiEditorOrderEditorList.log.isDebugEnabled()) {
        MultiEditorOrderEditorList.log.debug("MultiEditor.add: " + u);
      }
      buildOrderEditor(u);

      units.add(u);
      if (units.first() == u) {
        content.add(getEditor(u), 0);
      } else {
        Unit predUnit = getPreviousUnit(u);
        if (predUnit == null) {
          content.add(getEditor(u));
        } else {
          content.add(getEditor(u), content.getComponentZOrder(getEditor(predUnit)) + 1);
        }
      }
      addListeners(getEditor(u));
      // getEditor(u).setOrders(u.getOrders());
    } else {
      if (MultiEditorOrderEditorList.log.isDebugEnabled()) {
        MultiEditorOrderEditorList.log.debug("MultiEditor.not add: " + u);
      }
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
    // return ( units.get(units.indexOf(currentUnit) + 1));
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
   */
  private OrderEditor buildOrderEditor(Unit u) {
    if (u == null)
      return null;
    CacheableOrderEditor cEditor = getEditor(u);
    if (cEditor == null || !(cEditor instanceof OrderEditor)) {
      OrderEditor editor = new OrderEditor(getGameData(), settings, undoMgr, dispatcher, parser);
      u.addCacheHandler(this);
      attachOrderEditor(u, editor);
      return editor;
    } else {
      OrderEditor editor = (OrderEditor) cEditor;
      attachOrderEditor(u, editor);
      return editor;
    }
  }

  /**
   * Attaches a unit to an editor and vice versa.
   *
   * @param u
   * @param editor
   */
  private void attachOrderEditor(Unit u, OrderEditor editor) {
    editor.setBorder(getBorder(u, false));

    if (u.isOrdersConfirmed()) {
      editor.setBackground(standardBgColorConfirmed);
    } else {
      editor.setBackground(standardBgColor);
    }

    // deactivate visibility call
    editor.setKeepVisible(false);
    editor.setFont(new Font("Monospaced", Font.PLAIN, editor.getFont().getSize()));
    editor.setCursor(new Cursor(Cursor.TEXT_CURSOR));

    editor.setUnit(u);

    u.setOrderEditor(editor);
  }

  protected Border getBorder(Unit u, boolean active) {
    // return new TitledBorder(new MatteBorder(1, 0, 0, 1, Color.BLUE), u.toString());

    return new TitledBorder(active ? MultiEditorOrderEditorList.activeBorder : new LineBorder(
        Color.lightGray, 1), u == null ? "" : (u.toString() + ": " + u.getPersons()));
  }

  /**
   * Performs the clean-up necessary to put the editor list into a state without units and editors
   */
  private void clearUnits() {
    MultiEditorOrderEditorList.log.debug("clearUnits called");
    if (multiEditorLayout) {
      synchronized (content) {
        if (units != null) {
          for (Unit u : units) {
            u.setOrderEditor(null);
          }
          units.clear();
          units = new TreeSet<Unit>(EMapOverviewPanel.getUnitSorting(settings));
        }

        removeListenersFromAll();
        content.removeAll();
      }
    } else {
      if (currentUnit != null) {
        if (getEditor(currentUnit) != null) {
          removeListeners(getEditor(currentUnit));
          currentUnit.setOrderEditor(null);
        }
      }

      if (editorSingelton.getUnit() != null) {
        editorSingelton.getUnit().setOrderEditor(null);
      }
      editorSingelton.setUnit(null);
      editorSingelton.setEditable(false);
      removeListeners(editorSingelton);
    }

    currentUnit = null;
  }

  private void requestFocus(final CacheableOrderEditor editor) {
    // stm 2007.08.06 hack removed, still works
    // pavkovic 2004.02.14
    // THIS IS A HACK: I don't know why the focus
    // gets lost somehow so put it at the end of the
    // event dispatching thread.
    // It may be EMapOverviewPanel (SwingUtilities.invokeLater(new
    // ScrollerRunnable()))).
    // SwingUtilities.invokeLater(new Runnable() {
    // public void run() {
    editor.requestFocus();
    // }
    // });
  }

  private boolean requestEditorFocus() {
    if (multiEditorLayout) {
      if (getEditor(currentUnit) != null) {
        requestFocus(getEditor(currentUnit));
        return true;
      } else
        return false;
    } else {
      editorSingelton.requestFocus();
      return true;
    }
  }

  protected void loadListProperty() {
    try {
      listMode = Integer.parseInt(settings.getProperty("OrderEditorList.listMode"));
    } catch (Exception exc) {
      listMode = 1 << MultiEditorOrderEditorList.LIST_REGION;
    }
  }

  public void saveListProperty() {
    if (listMode == (1 << MultiEditorOrderEditorList.LIST_REGION)) {
      settings.remove("OrderEditorList.listMode");
    } else {
      settings.setProperty("OrderEditorList.listMode", String.valueOf(listMode));
    }
  }

  /**
   * Returns the variable listMode.
   */
  public int getListMode() {
    return listMode;
  }

  /**
   * Sets the variable listMode.
   */
  public void setListMode(int listMode) {
    this.listMode = listMode;
  }

  /**
   * @see magellan.client.swing.completion.OrderEditorList#getCurrentEditor()
   */
  public JTextComponent getCurrentEditor() {
    if (multiEditorLayout) {
      if (getEditor(currentUnit) != null)
        return getEditor(currentUnit);
      else
        return null;
    } else
      return editorSingelton;
  }

  /**
   * @see magellan.client.swing.completion.OrderEditorList#getCurrentUnit()
   */
  public Unit getCurrentUnit() {
    return currentUnit;
  }

  /**
   * @see magellan.client.swing.completion.OrderEditorList#getPreferencesAdapter()
   */
  public PreferencesAdapter getPreferencesAdapter() {
    return new DetailsViewOrderEditorPreferences(this);
  }

  /**
   * @see magellan.client.swing.completion.OrderEditorList#addExternalKeyListener(java.awt.event.KeyListener)
   */
  public void addExternalKeyListener(KeyListener k) {
    keyListeners.add(k);
  }

  /**
   * @see magellan.client.swing.completion.OrderEditorList#removeExternalKeyListener(java.awt.event.KeyListener)
   */
  public void removeExternalKeyListener(KeyListener k) {
    keyListeners.remove(k);
  }

  /**
   * @see magellan.client.swing.completion.OrderEditorList#addExternalCaretListener(javax.swing.event.CaretListener)
   */
  public void addExternalCaretListener(CaretListener k) {
    caretListeners.add(k);
  }

  /**
   * @see magellan.client.swing.completion.OrderEditorList#removeExternalCaretListener(javax.swing.event.CaretListener)
   */
  public void removeExternalCaretListener(CaretListener k) {
    caretListeners.remove(k);
  }

  /**
   * @see magellan.client.swing.completion.OrderEditorList#addExternalFocusListener(java.awt.event.FocusListener)
   */
  public void addExternalFocusListener(FocusListener k) {
    focusListeners.add(k);
  }

  /**
   * @see magellan.client.swing.completion.OrderEditorList#removeExternalFocusListener(java.awt.event.FocusListener)
   */
  public void removeExternalFocusListener(FocusListener k) {
    focusListeners.remove(k);
  }

  private static class MEKeyAdapter extends KeyAdapter {
    protected MultiEditorOrderEditorList source;

    /**
     * Creates a new MEKeyAdapter object.
     */
    public MEKeyAdapter(MultiEditorOrderEditorList s) {
      source = s;
    }

    /**
     * @see java.awt.event.KeyAdapter#keyPressed(java.awt.event.KeyEvent)
     */
    @Override
    public void keyPressed(KeyEvent e) {
      // first external listeners
      for (KeyListener keyListener2 : source.keyListeners) {
        (keyListener2).keyPressed(e);

        if (e.isConsumed())
          return;
      }

      // now the source
      source.keyPressed(e);
    }

    /**
     * @see java.awt.event.KeyAdapter#keyReleased(java.awt.event.KeyEvent)
     */
    @Override
    public void keyReleased(KeyEvent e) {
      // first external listeners
      for (KeyListener keyListener2 : source.keyListeners) {
        (keyListener2).keyReleased(e);

        if (e.isConsumed())
          return;
      }

      // now the source
      source.keyReleased(e);
    }

    /**
     * @see java.awt.event.KeyAdapter#keyTyped(java.awt.event.KeyEvent)
     */
    @Override
    public void keyTyped(KeyEvent e) {
      // first external listeners
      for (KeyListener keyListener2 : source.keyListeners) {
        (keyListener2).keyTyped(e);

        if (e.isConsumed())
          return;
      }

      // now the source
      source.keyTyped(e);
    }
  }

  private static class MEFocusAdapter extends FocusAdapter {
    protected MultiEditorOrderEditorList source;

    /**
     * Creates a new MEFocusAdapter object.
     */
    public MEFocusAdapter(MultiEditorOrderEditorList s) {
      source = s;
    }

    /**
     * @see java.awt.event.FocusAdapter#focusGained(java.awt.event.FocusEvent)
     */
    @Override
    public void focusGained(FocusEvent e) {
      // first the source (maybe selection event!)
      // source.focusGained(e);

      // then external listeners
      for (FocusListener focusListener2 : source.focusListeners) {
        (focusListener2).focusGained(e);
      }
    }

    /**
     * @see java.awt.event.FocusAdapter#focusLost(java.awt.event.FocusEvent)
     */
    @Override
    public void focusLost(FocusEvent e) {
      // first external listeners
      for (FocusListener focusListener2 : source.focusListeners) {
        (focusListener2).focusLost(e);
      }

      // now the source
      // source.focusLost(e);
    }
  }

  private static class MECaretAdapter implements CaretListener {
    protected MultiEditorOrderEditorList source;

    /**
     * Creates a new MECaretAdapter object.
     */
    public MECaretAdapter(MultiEditorOrderEditorList s) {
      source = s;
    }

    /**
     * @see javax.swing.event.CaretListener#caretUpdate(javax.swing.event.CaretEvent)
     */
    public void caretUpdate(CaretEvent e) {
      for (CaretListener caretListener : source.caretListeners) {
        (caretListener).caretUpdate(e);
      }
    }
  }

  // this is a very special thread to position the actual orderEditor into the
  // viewport of the
  // scrollpane
  // In very ugly situations it is called twice, but that does not really hurt
  private class SwingGlitchThread implements Runnable {

    private int loopCounter = 0;

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
      // waiting for isValid() to become true
      // does not work, because when we really
      // need it, i.e. editors are shown for the
      // first time they stay invalid.
      Rectangle viewRect = scpContent.getViewport().getViewRect();

      if (MultiEditorOrderEditorList.log.isDebugEnabled()) {
        MultiEditorOrderEditorList.log
            .debug("MultiEditorOrderEditorList.selectionChanged.runnable: viewRect:" + viewRect);
      }

      if (getEditor(currentUnit) != null) {
        OrderEditor editor = getEditor(currentUnit);
        Rectangle bounds = editor.getBounds();
        MultiEditorOrderEditorList.log
            .debug("MultiEditorOrderEditorList.selectionChanged.runnable: Bounds:" + bounds);

        while (!viewRect.contains(viewRect.x, bounds.y, viewRect.width, Math.min(viewRect.height,
            bounds.height))) {
          Point newPos = null;

          if (bounds.height < viewRect.height) {
            newPos = new Point(0, bounds.y - ((viewRect.height - bounds.height) / 2));
          } else {
            newPos = new Point(0, bounds.y);
          }

          newPos.y = Math.max(0, newPos.y);

          if (MultiEditorOrderEditorList.log.isDebugEnabled()) {
            MultiEditorOrderEditorList.log
                .debug("MultiEditorOrderEditorList.selectionChanged.runnable: newPos : " + newPos);
          }

          Rectangle newRect = new Rectangle(viewRect);
          newRect.setLocation(newPos);

          // currentUnit.cache.orderEditor.scrollRectToVisible(newRect);
          int pos = editor.getCaretPosition();
          content.scrollRectToVisible(newRect);
          editor.setCaretPosition(0);
          editor.setCaretPosition(pos);

          // scpContent.getViewport().setViewPosition(newPos);
          viewRect = scpContent.getViewport().getViewRect();
          bounds = getEditor(currentUnit).getBounds();

          if (++loopCounter > 3) {
            loopCounter = 0;

            break;
          }
        }

        MultiEditorOrderEditorList.log
            .debug("MultiEditorOrderEditorList.selectionChanged.runnable: viewRect after:"
                + viewRect);
        MultiEditorOrderEditorList.log
            .debug("MultiEditorOrderEditorList.selectionChanged.runnable: Bounds after:" + bounds);
      }

      content.validate();
      scpContent.getViewport().invalidate();
      repaint();
    }
  }

  /**
   * Does the syntax highlighting for an editor.
   */
  private static class UpdateThread implements Runnable {
    public CacheableOrderEditor e = null;

    /**
     * Format the editor
     */
    public void run() {
      e.formatTokens();
      // this is not allowed as it may prevent storing of changed orders of a
      // unit!
      // e.setModified(false);
    }
  }

  protected ButtonPanel getButtonPanel() {
    if (buttons == null) {
      buttons = new ButtonPanel();
      DesktopEnvironment.registerShortcutListener(buttons.getShortCutListener());
    }
    return buttons;
  }

  // for "check orders", "create temp unit" and "delete temp unit"
  protected class ButtonPanel extends JPanel implements ActionListener {
    private JCheckBox checkOrderConfirm = null;
    private JButton btnCreateTempUnit = null;
    private JButton btnDeleteTempUnit = null;
    private TempUnitDialog dialog;
    private ShortcutListener shortCutListener;

    /**
     * Returns the value of shortCutListener.
     *
     * @return Returns shortCutListener.
     */
    public ShortcutListener getShortCutListener() {
      return shortCutListener;
    }

    /**
     * Creates a new ButtonPanel object.
     */
    public ButtonPanel() {

      shortCutListener = new magellan.client.desktop.ShortcutListener() {
        List<KeyStroke> shortcuts = null;

        public Iterator<KeyStroke> getShortCuts() {
          if (shortcuts == null) {
            shortcuts = new LinkedList<KeyStroke>();
            shortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK));
            shortcuts.add(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK
                | InputEvent.SHIFT_DOWN_MASK));
          }

          return shortcuts.iterator();
        }

        public void shortCut(KeyStroke s) {
          int index = shortcuts.indexOf(s);

          switch (index) {
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

        public String getShortcutDescription(KeyStroke stroke) {
          int index = shortcuts.indexOf(stroke);

          return Resources.get("completion.multieditorordereditorlist.shortcuts.description."
              + String.valueOf(index));
        }
      };

      checkOrderConfirm =
          new JCheckBox(Resources
              .get("completion.multieditorordereditorlist.chk.orderconfirmation"), false);
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

      setLayout(new GridBagLayout());
      setBorder(new EmptyBorder(4, 4, 4, 4));

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
          if (!equals(e.getSource())) {
            for (Unit u : e.getUnits()) {
              if (u.equals(currentUnit)) {
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
          if ((e.getActiveObject() != null) && e.getActiveObject() instanceof Unit) {
            Unit u = (Unit) e.getActiveObject();

            checkOrderConfirm.setSelected(u.isOrdersConfirmed());
          }
        }
      });
    }

    /**
     * Performs the button actions.
     *
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(java.awt.event.ActionEvent p1) {
      if (p1.getSource() == checkOrderConfirm) {
        toggleOrderConfirmation();
      }

      if (p1.getSource() == btnCreateTempUnit) {
        createTempUnit();
      }

      if (p1.getSource() == btnDeleteTempUnit) {
        deleteTempUnit();
      }
    }

    protected void toggleOrderConfirmation() {
      if (currentUnit != null) {
        currentUnit.setOrdersConfirmed(!currentUnit.isOrdersConfirmed());
        checkOrderConfirm.setSelected(currentUnit.isOrdersConfirmed());

        dispatcher.fire(new OrderConfirmEvent(this, Collections.singletonList(currentUnit)));
      }
    }

    protected void createTempUnit() {
      if ((currentUnit != null) && TrustLevels.isPrivileged(currentUnit.getFaction())) {
        // Use the current unit as the parent or its parent if it
        // is itself a temp unit
        Unit parentUnit = currentUnit;

        if (currentUnit instanceof TempUnit) {
          parentUnit = ((TempUnit) currentUnit).getParent();
        }

        createTempImpl(parentUnit, parentUnit.getRegion());
      }
    }

    private void createTempImpl(Unit parentUnit, Region parentRegion) {
      UnitID id = UnitID.createTempID(getGameData(), settings, parentUnit);

      if (dialogProvider == null || !settings
          .getProperty("MultiEditorOrderEditorList.ButtonPanel.ShowTempUnitDialog", "true")
          .equalsIgnoreCase("true")) {
        // don't show any dialogs, simply create the tempunit and finish.
        TempUnit tempUnit = parentUnit.createTemp(getGameData(), id);
        dispatcher.fire(new TempUnitEvent(this, tempUnit, TempUnitEvent.CREATED), true);
        selectEditor(tempUnit);
        if (getEditor(tempUnit) != null) {
          getEditor(tempUnit).requestFocus();
        }
        dispatcher.fire(SelectionEvent.create(this, tempUnit));
      } else {
        // do all the tempunit-dialog-stuff

        if (dialog == null) {
          dialog = dialogProvider.create();
        }

        // ask the user for a valid id repeatedly
        boolean first = true;

        dialog.setFaction(parentUnit.getFaction());
        dialog.setParentGroup(parentUnit.getGroup());
        dialog.setCombatState(parentUnit.getCombatStatus());

        while (true) {
          if (first) { // reset if it's the first dialog for this temp unit
            // unit id is non-negative on views
            UnitID newID = UnitID.createUnitID(-id.intValue(), getGameData().base);
            dialog.show(newID.toString(), parentUnit.getName());
          } else { // do not reset if we had formerly wrong data
            dialog.show(parentUnit.getName());
          }

          first = false;

          if (dialog.isApproved()) {
            String tempID = dialog.getID();

            if ((tempID == null) || tempID.trim().equals(""))
              // JOptionPane.show...
              return;

            try {
              int realNewIDInt = IDBaseConverter.parse(tempID, getGameData().base);
              UnitID checkID = UnitID.createUnitID(-realNewIDInt, getGameData().base);

              if (getGameData().getTempUnit(checkID) == null) {
                TempUnit tempUnit = parentUnit.createTemp(getGameData(), checkID);

                // Name
                String name = dialog.getName();

                if ((name != null) && !name.trim().equals("")) {
                  tempUnit.setName(name);
                  getGameData().getGameSpecificStuff().getOrderChanger().addNamingOrder(tempUnit,
                      name);
                }

                // extended features
                if (dialog.wasExtendedDialog()) {
                  // Recruiting
                  try {
                    int recruits =
                        Math.max(0,
                            (dialog.getRecruit() != null && dialog.getRecruit().length() > 0)
                                ? Integer.parseInt(dialog.getRecruit()) : 0);
                    int transfers =
                        Math.max(0,
                            (dialog.getTransfer() != null && dialog.getTransfer().length() > 0)
                                ? Integer.parseInt(dialog.getTransfer()) : 0);

                    if (recruits > 0) {
                      getGameData().getGameSpecificStuff().getOrderChanger().addRecruitOrder(
                          tempUnit, recruits);
                    }
                    if (transfers > 0) {
                      getGameData().getGameSpecificStuff().getOrderChanger().addGiveOrder(
                          parentUnit, tempUnit, transfers, EresseaConstants.I_MEN, null);
                    }
                    if (dialog.isGiveRecruitCost() && recruits > 0) {
                      getGameData().getGameSpecificStuff().getOrderChanger().addGiveOrder(
                          parentUnit,
                          tempUnit,
                          recruits * parentUnit.getRace().getRecruitmentCosts(),
                          EresseaConstants.I_USILVER,
                          Resources
                              .get("completion.multieditorordereditorlist.tempunit.recruitCost"));
                    }
                    if (dialog.isGiveMaintainCost() && transfers + recruits > 0) {
                      getGameData().getGameSpecificStuff().getOrderChanger().addGiveOrder(
                          parentUnit,
                          tempUnit,
                          parentUnit.getRace().getMaintenance() * (transfers + recruits),
                          EresseaConstants.I_USILVER,
                          Resources
                              .get("completion.multieditorordereditorlist.tempunit.maintainCost"));
                    }

                    dispatcher.fire(new UnitOrdersEvent(this, parentUnit));
                  } catch (NumberFormatException nfe) {
                    // user input error, ignore
                  }
                }

                // simple order
                String order = dialog.getOrder();

                if ((order != null) && !order.trim().equals("")) {
                  tempUnit.addOrder(order);
                }

                String group = dialog.getGroup();
                if (group != null) {
                  getGameData().getGameSpecificStuff().getOrderChanger().addGroupOrder(tempUnit,
                      group);
                }

                int combatState = dialog.getCombatState();
                if (combatState >= 0) {
                  getGameData().getGameSpecificStuff().getOrderChanger().addCombatOrder(tempUnit,
                      combatState);
                }

                // description
                String descript = dialog.getDescript();

                if ((descript != null) && !descript.trim().equals("")) {
                  descript = descript.replace('\n', ' ');
                  tempUnit.setDescription(descript);
                  getGameData().getGameSpecificStuff().getOrderChanger().addDescribeUnitOrder(
                      tempUnit, descript);
                }

                parentUnit.refreshRelations();
                // data update
                dispatcher.fire(new TempUnitEvent(this, tempUnit, TempUnitEvent.CREATED), true);
                selectEditor(tempUnit);
                if (getEditor(tempUnit) != null) {
                  getEditor(tempUnit).requestFocus();
                }
                dispatcher.fire(SelectionEvent.create(this, tempUnit));
                return;
              } else {
                JOptionPane.showMessageDialog(this, Resources
                    .get("completion.multieditorordereditorlist.msg.duplicatetempid.text"),
                    Resources
                        .get("completion.multieditorordereditorlist.msg.duplicatetempid.title"),
                    JOptionPane.ERROR_MESSAGE);
              }
            } catch (NumberFormatException nfe) {
              JOptionPane.showMessageDialog(this, Resources
                  .get("completion.multieditorordereditorlist.msg.invalidtempid.text"), Resources
                      .get("completion.multieditorordereditorlist.msg.invalidtempid.title"),
                  JOptionPane.ERROR_MESSAGE);
            }
          } else
            // dialog canceled
            return;
        }
      }
    }

    protected void deleteTempUnit() {
      if ((currentUnit != null) && currentUnit instanceof TempUnit) {
        TempUnit tempUnit = (TempUnit) currentUnit;
        Unit parentUnit = tempUnit.getParent();
        selectEditor(parentUnit);
        if (getEditor(currentUnit) != null) {
          getEditor(currentUnit).requestFocus();
        }
        dispatcher.fire(SelectionEvent.create(this, parentUnit), true);
        dispatcher.fire(new TempUnitEvent(this, tempUnit, TempUnitEvent.DELETING), true);
        parentUnit.deleteTemp(tempUnit.getID(), getGameData());
      }
    }

    /**
     * Enables the confirm box.
     */
    public void setConfirmEnabled(boolean enabled) {
      checkOrderConfirm.setEnabled(enabled);
    }

    /**
     * Enables the create temp button.
     */
    public void setCreationEnabled(boolean enabled) {
      btnCreateTempUnit.setEnabled(enabled);
    }

    /**
     * Enables the delete temp button
     */
    public void setDeletionEnabled(boolean enabled) {
      btnDeleteTempUnit.setEnabled(enabled);
    }

    /**
     * Updates the buttons according to the current unit.
     */
    public void currentUnitChanged() {
      boolean enabled =
          (currentUnit != null) && currentUnit.getFaction() != null
              && currentUnit.getFaction().isPrivileged();

      setConfirmEnabled(enabled);
      setCreationEnabled(enabled);
      setDeletionEnabled(enabled && (currentUnit instanceof TempUnit));
    }
  }

  /**
   * A simple JPanel that implements the Scrollable interface and is used to hold the order editors.
   */
  private class ScrollPanel extends JPanel implements Scrollable {
    /**
     * DOCUMENT-ME
     *
     * @see javax.swing.Scrollable#getPreferredScrollableViewportSize()
     */
    public Dimension getPreferredScrollableViewportSize() {
      return getPreferredSize();
    }

    /**
     * If the parent of this component is an instance of JViewport this method returns the maximum
     * of the original preferred size and the viewport size.
     */
    @Override
    public Dimension getPreferredSize() {
      Container parent = getParent();

      // this special case has become necessary with the
      // implementation of the Scrollable interface
      if (parent instanceof JViewport) {
        JViewport viewport = (JViewport) parent;
        Dimension psize = super.getPreferredSize();
        psize.width = Math.max(psize.width, viewport.getWidth());
        psize.height = Math.max(psize.height, viewport.getHeight());

        return psize;
      } else
        return super.getPreferredSize();
    }

    /**
     * DOCUMENT-ME
     */
    public int getScrollableUnitIncrement(java.awt.Rectangle visibleRect, int orientation,
        int direction) {
      if (orientation == SwingConstants.HORIZONTAL)
        return visibleRect.width / 5;
      else {
        if (!isMultiEditorLayout())
          return visibleRect.height / 10;
        Component lastVisibleComponent = null;
        Component nextComponent = null;

        if (direction < 0) { // up
          lastVisibleComponent = this.getComponentAt(visibleRect.x, visibleRect.y);

          if (lastVisibleComponent != null) {
            if (visibleRect.y > lastVisibleComponent.getY())
              // component is not fully visible
              return visibleRect.y - lastVisibleComponent.getY();
            else {
              // component is fully visible, get next component
              List<Component> components = Arrays.asList(getComponents());
              int count = components.indexOf(lastVisibleComponent) - 1;

              if ((count >= 0) && (count < getComponentCount())) {
                nextComponent = getComponent(count);

                return nextComponent.getHeight();
              }
            }
          }
        } else { // down
          lastVisibleComponent =
              this.getComponentAt(visibleRect.x, visibleRect.y + visibleRect.height);

          if (lastVisibleComponent != null) {
            if ((visibleRect.y + visibleRect.height) < (lastVisibleComponent.getY()
                + lastVisibleComponent
                    .getHeight()))
              // component is not fully visible
              return (lastVisibleComponent.getY() + lastVisibleComponent.getHeight())
                  - visibleRect.y - visibleRect.height;
            else {
              // component is fully visible, get next component
              List<Component> components = Arrays.asList(getComponents());
              int count = components.indexOf(lastVisibleComponent) + 1;

              if ((count >= 0) && (count < getComponentCount())) {
                nextComponent = getComponent(count);

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
     */
    public int getScrollableBlockIncrement(java.awt.Rectangle visibleRect, int orientation,
        int direction) {
      if (orientation == SwingConstants.HORIZONTAL)
        return visibleRect.width;
      else
        return visibleRect.height;
    }

    /**
     * DOCUMENT-ME
     */
    public boolean getScrollableTracksViewportWidth() {
      return false;
    }

    /**
     * DOCUMENT-ME
     */
    public boolean getScrollableTracksViewportHeight() {
      return false;
    }
  }

  public boolean isEditAllFactions() {
    return editAllFactions;
  }

  public void setEditAllFactions(boolean edit) {
    settings.setProperty(PropertiesHelper.ORDEREDITOR_EDITALLFACTIONS, String.valueOf(edit));
    editAllFactions = edit;
    redrawPane();
  }

  /**
   * Sets completion as the completion for order auto completion.
   *
   * @param completion
   */
  public void setCompleter(AutoCompletion completion) {
    this.completion = completion;
  }

  /**
   * @return The current order completer
   */
  public AutoCompletion getCompleter() {
    return completion;
  }

  /**
   * Return whether syntax highlighting is enabled or disabled.
   */
  public static boolean getUseSyntaxHighlighting(Properties settings) {
    return (Boolean.valueOf(settings.getProperty("OrderEditor.highlightSyntax", "true"))
        .booleanValue());
  }

}
