/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.client.completion;

import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.CaretListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;

import magellan.client.event.EventDispatcher;
import magellan.client.event.SelectionEvent;
import magellan.client.event.SelectionListener;
import magellan.client.preferences.DetailsViewAutoCompletionPreferences;
import magellan.client.swing.completion.CompletionGUI;
import magellan.client.swing.completion.ListCompletionGUI;
import magellan.client.swing.completion.MarkedTextCompletionGUI;
import magellan.client.swing.completion.NoneCompletionGUI;
import magellan.client.swing.completion.OrderEditorList;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.library.completion.Completer;
import magellan.library.completion.CompleterSettingsProvider;
import magellan.library.completion.Completion;
import magellan.library.event.GameDataEvent;
import magellan.library.event.GameDataListener;
import magellan.library.gamebinding.AbstractOrderCompleter;
import magellan.library.gamebinding.EresseaOrderCompleter;
import magellan.library.utils.CollectionFactory;
import magellan.library.utils.OrderToken;
import magellan.library.utils.PropertiesHelper;
import magellan.library.utils.logging.Logger;

/**
 * DOCUMENT ME!
 *
 * @author Andreas Gampe, Ulrich Küster
 */
public class AutoCompletion implements SelectionListener, KeyListener, ActionListener,
    CaretListener, FocusListener, GameDataListener, CompleterSettingsProvider {
  private static final Logger log = Logger.getInstance(AutoCompletion.class);
  private OrderEditorList editors;
  private Vector<CompletionGUI> completionGUIs;
  private CompletionGUI currentGUI;
  private Completer completer;
  private int lastCaretPosition = 0;

  private static final AttributeSet SIMPLEATTRIBUTESET = new SimpleAttributeSet();

  /**
   * Keys for cycling, completing and breaking.
   */
  private KeyStroke completerKeys[];

  public static final int CK_FORWARD = 0;
  public static final int CK_BACKWARD = 1;
  public static final int CK_COMPLETE = 2;
  public static final int CK_BREAK = 3;
  public static final int CK_START = 4;
  /** number of completer keys (cycle forward, cycle backward, complete, cancel, start */
  public static final int numKeys = 5;

  private Timer timer;
  private List<Completion> completions = null;
  private int completionIndex = 0;
  private String lastStub = null;
  private boolean enableAutoCompletion = true;

  // limits the completion of the make-order to items
  // whose resources are available
  private boolean limitMakeCompletion = true;
  private boolean emptyStubMode = false;
  private boolean hotKeyMode = false;

  private String activeGUI = null;
  private int time = 150;

  // self defined completion objects (mapping a name (String) to a value
  // (String))
  private Map<String, String> selfDefinedCompletions = new LinkedHashMap<String, String>();

  protected Properties settings;
  private boolean limitCompletions = true;

  /**
   * Creates new AutoCompletion
   */
  public AutoCompletion(Properties settings, EventDispatcher dispatcher) {
    this.settings = settings;
    dispatcher.addSelectionListener(this);
    dispatcher.addGameDataListener(this);

    editors = null;
    completionGUIs = new Vector<CompletionGUI>();
    currentGUI = null;
    completer = null;

    loadSettings();
    timer = new Timer(time, this);

    addCompletionGUI(new ListCompletionGUI());
    addCompletionGUI(new MarkedTextCompletionGUI());
    addCompletionGUI(new NoneCompletionGUI());

    // let the AutoCompletion choose the current GUI
    loadComplete();
  }

  protected void loadSettings() {
    String autoCmp = settings.getProperty(PropertiesHelper.AUTOCOMPLETION_ENABLED);

    if (autoCmp != null) {
      enableAutoCompletion = autoCmp.equalsIgnoreCase("true");
    } else {
      autoCmp = settings.getProperty("OrderEditingPanel.useOrderCompletion");

      if (autoCmp != null) {
        enableAutoCompletion = Boolean.valueOf(autoCmp).booleanValue();
        settings.remove("OrderEditingPanel.useOrderCompletion");
        settings.setProperty(PropertiesHelper.AUTOCOMPLETION_ENABLED, enableAutoCompletion ? "true"
            : "false");
      } else {
        enableAutoCompletion = true;
      }
    }

    limitMakeCompletion =
        settings.getProperty(PropertiesHelper.AUTOCOMPLETION_LIMIT_MAKE_COMPLETION, "true")
            .equalsIgnoreCase("true");

    String stubMode = settings.getProperty(PropertiesHelper.AUTOCOMPLETION_EMPTY_STUB_MODE, "true");
    emptyStubMode = stubMode.equalsIgnoreCase("true");

    String keyMode = settings.getProperty(PropertiesHelper.AUTOCOMPLETION_HOTKEY_MODE, "false");
    hotKeyMode = keyMode.equalsIgnoreCase("true");

    activeGUI = settings.getProperty(PropertiesHelper.AUTOCOMPLETION_COMPLETION_GUI, "List");

    try {
      time = Integer.parseInt(settings.getProperty(PropertiesHelper.AUTOCOMPLETION_TIME, "150"));
    } catch (Exception exc) {
      time = 150;
    }

    completerKeys = new KeyStroke[AutoCompletion.numKeys];

    completerKeys[CK_FORWARD] = PropertiesHelper.getKeyStroke(settings,
        PropertiesHelper.AUTOCOMPLETION_KEYS_CYCLE_FORWARD,
        KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_DOWN_MASK));
    completerKeys[CK_BACKWARD] = PropertiesHelper.getKeyStroke(settings,
        PropertiesHelper.AUTOCOMPLETION_KEYS_CYCLE_BACKWARD,
        KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_DOWN_MASK));
    completerKeys[CK_COMPLETE] = PropertiesHelper.getKeyStroke(settings,
        PropertiesHelper.AUTOCOMPLETION_KEYS_COMPLETE,
        KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0));
    completerKeys[CK_BREAK] = PropertiesHelper.getKeyStroke(settings,
        PropertiesHelper.AUTOCOMPLETION_KEYS_BREAK,
        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
    completerKeys[CK_START] = PropertiesHelper.getKeyStroke(settings,
        PropertiesHelper.AUTOCOMPLETION_KEYS_START,
        KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.CTRL_DOWN_MASK));

    selfDefinedCompletions = getSelfDefinedCompletions(settings);
  }

  /**
   * read defined completions from given property file
   *
   * @return Map containing name to
   */
  private Map<String, String> getSelfDefinedCompletions(Properties aSettings) {
    Map<String, String> result = CollectionFactory.<String, String> createSyncOrderedMap();

    // load selfdefined completions
    List<String> completionNames =
        PropertiesHelper.getList(aSettings, "AutoCompletion.SelfDefinedCompletions.name");
    for (int i = 0, size = completionNames.size(); i < size; i++) {
      String name = completionNames.get(i);
      String value = (String) aSettings.get("AutoCompletion.SelfDefinedCompletions.value" + i);
      if (name != null && value != null) {
        result.put(name, value);
      }
    }

    return result;
  }

  /**
   * Tries to use the active GUI from the settings. If this is not available, some GUI from the list
   * of available GUIs is chosen.
   */
  protected void loadComplete() {
    if (completionGUIs.size() == 0)
      return;

    if (activeGUI == null) {
      setCurrentGUI(completionGUIs.get(0));

      return;
    }

    Iterator<CompletionGUI> it = completionGUIs.iterator();

    while (it.hasNext()) {
      CompletionGUI cGUI = it.next();

      if (cGUI.getTitle().equals(activeGUI)) {
        setCurrentGUI(cGUI);

        return;
      }
    }

    setCurrentGUI(completionGUIs.get(0));
  }

  /**
   * Registers a new OrderEditorList to be used by this AutoCompletion.
   */
  public void attachEditorManager(OrderEditorList cel) {
    if (editors != null) {
      editors.removeExternalKeyListener(this);
      editors.removeExternalCaretListener(this);
      editors.removeExternalFocusListener(this);
    }

    editors = cel;

    if (editors != null) {
      editors.addExternalKeyListener(this);
      editors.addExternalCaretListener(this);
      editors.addExternalFocusListener(this);
    }
  }

  /**
   * Adds a new GUI to the list of available GUIs.
   */
  public void addCompletionGUI(CompletionGUI cGUI) {
    cGUI.init(this);
    completionGUIs.add(cGUI);
  }

  /**
   * Returns the list of available GUIs.
   */
  public Vector<CompletionGUI> getCompletionGUIs() {
    return completionGUIs;
  }

  /**
   * Sets <code>cGUI</code> as active GUI.
   *
   * @param cGUI
   */
  public void setCurrentGUI(CompletionGUI cGUI) {
    if (AutoCompletion.log.isDebugEnabled()) {
      AutoCompletion.log.debug("AutoCompletion.setCurrentGUI called with " + cGUI);
    }

    if (!completionGUIs.contains(cGUI)) {
      addCompletionGUI(cGUI);
    }

    if (currentGUI != null) {
      currentGUI.stopOffer();
    }

    currentGUI = cGUI;
    settings.setProperty(PropertiesHelper.AUTOCOMPLETION_COMPLETION_GUI, cGUI.getTitle());
    timer.restart();
  }

  /**
   * Returns the currently used GUI.
   */
  public CompletionGUI getCurrentGUI() {
    return currentGUI;
  }

  /**
   * Registers a new Completer to use.
   */
  public void setCompleter(Completer c) {
    completer = c;
  }

  /**
   * @see magellan.client.event.SelectionListener#selectionChanged(magellan.client.event.SelectionEvent)
   */
  public void selectionChanged(SelectionEvent e) {
    if (AutoCompletion.log.isDebugEnabled()) {
      AutoCompletion.log.debug("AutoCompletion.selectionChanged called with " + e);
    }

    // stop offering a completion
    if (currentGUI != null) {
      currentGUI.stopOffer();
    }

    // let the editor update
    if (editors != null) {
      editors.selectionChanged(e);

      // search for new completion
      JTextComponent j = editors.getCurrentEditor();

      if ((j != null) && enableAutoCompletion) {
        timer.restart();
      }
    }
  }

  /**
   * Finds and displays a new list of completions according to the current editor and caret
   * position.
   */
  public void offerCompletion(JTextComponent j) {
    offerCompletion(j, true);
  }

  /**
   * Finds and displays a new list of completions according to the current editor and caret
   * position. If <code>manual</code> is <code>true</code>, the GUI is always shown. Otherwise, it
   * is only shown if appropriate (i.e. there is something to complete).
   */
  protected void offerCompletion(JTextComponent j, boolean manual) {
    if (!enableAutoCompletion || currentGUI == null || completer == null || j == null
        || !j.isVisible())
      return;

    // find the piece of text
    // TODO join with previous line if this one ends with "\\"
    String line = AutoCompletion.getCurrentLine(j);

    if (line == null)
      return;

    if ((line.length() == 0)
        || ((j.getCaretPosition() > 0) && (j.getText().charAt(j.getCaretPosition() - 1) == '\n'))
        || ((j.getCaretPosition() > 0) && (j.getText().charAt(j.getCaretPosition() - 1) == '\r'))) {
      // new line, delete old completions
      completions = null;
    } else if (lastCaretPosition + 1 != j.getCaretPosition()
        && lastCaretPosition != j.getCaretPosition()) {
      // Caret went backwards so line must be parsed again
      completions = null;
    } else if ((j.getCaretPosition() > 0) && (j.getText().charAt(j.getCaretPosition() - 1) == ' ')) {
      // if Space typed, delete old completions to enforce new parsing
      completions = null;
    }
    if (completions == null) {
      currentGUI.stopOffer();
    }

    // remember CaretPosition
    lastCaretPosition = j.getCaretPosition();

    boolean doOffer = false;
    String stub = "";
    // run completer engine
    if (editors.getCurrentUnit() != null) {
      completions = completer.getCompletions(editors.getCurrentUnit(), line, completions);
      // try to get stub from last token
      List<OrderToken> tokens = completer.getParserTokens();
      if (tokens != null) {
        stub = AutoCompletion.getStub(tokens);
      } else {
        stub = AutoCompletion.getStub(line);
      }
      addCommonCompletion(stub);
      lastStub = stub;

      // determine, whether the cursor is inside a word
      boolean inWord = true;

      if ((j.getText().length() == j.getCaretPosition()) || (j.getCaretPosition() == 0)) {
        inWord = false;
      } else {
        char c = j.getText().charAt(j.getCaretPosition());
        // TODO test for quotes?
        if ((c == ' ') || (c == '\n') || (c == '\t')) {
          inWord = false;
        } else {
          c = j.getText().charAt(j.getCaretPosition() - 1);

          if ((c == ' ') || (c == '\n') || (c == '\t')) {
            inWord = false;
          }
        }
      }

      // show completions

      // Fiete: try to detect, if we fully typed a offered completion
      // ennos feature wish: in that case do not offer the completion anymore
      // we check, if we have only one completion left AND if the last word typed is just
      // this last completion - in that case we do not offer the completion
      boolean completionCompleted = false;
      if ((completions != null) && (completions.size() == 1) && !inWord) {
        // we have just 1 completion avail and we are not in a word
        Completion c = completions.iterator().next();
        if (c.getValue().equalsIgnoreCase(stub) && !stub.equals("")) {
          // last completion equals last fully typed word
          completionCompleted = true;
        }
      }

      if (completions != null) {
        if (manual
            || (line.length() > 0 && (emptyStubMode || (stub.length() > 0))
                && (completions.size() > 0) && !inWord && !completionCompleted)) {
          doOffer = true;
        }
      }
    }
    if (doOffer) {
      currentGUI.offerCompletion(j, completions, stub);
      completionIndex = 0;
    } else if (currentGUI != null) {
      currentGUI.stopOffer();
    }
  }

  /**
   * If all completions share a common prefix, add the completion "prefix..."
   *
   * @param stub
   */
  private void addCommonCompletion(String stub) {
    if (completions.size() > 1) {
      Completion reference = completions.iterator().next();
      int common = 0;
      boolean isCommon = true;
      for (common = 0; common < reference.getName().length() && isCommon; ++common) {
        isCommon = true;
        char commonChar = reference.getName().charAt(common);
        if (commonChar == ' ' || commonChar == '\t') {
          isCommon = false;
          break;
        }
        for (Completion c : completions) {
          if (c.getName().length() <= common || c.getName().charAt(common) != commonChar) {
            isCommon = false;
            common--;
            break;
          }
        }
      }
      if (common > 0 && reference.getName().length() != common
          && reference.getName().charAt(common) == ' ') {
        common--;
      }
      if (common > 0) {
        String commonPart = reference.getName().substring(0, common);
        if (stub.length() < common && !contained(commonPart + "...")) {
          completions.add(0, new Completion(commonPart + "...", commonPart, "", 0));
        }
      }
    }
  }

  private boolean contained(String commonPart) {
    for (Completion c : completions) {
      if (commonPart.equals(c.getName()))
        return true;
    }
    return false;
  }

  /**
   * Selects the next completion in the list, if available.
   */
  public void cycleForward() {
    if ((completions == null) || (completions.size() == 1) || (editors == null))
      return;

    completionIndex++;
    completionIndex %= completions.size();

    if (currentGUI.isOfferingCompletion()) {
      currentGUI
          .cycleCompletion(editors.getCurrentEditor(), completions, lastStub, completionIndex);
    }
  }

  /**
   * Selects the previous completion in the list, if available.
   */
  public void cycleBackward() {
    if ((completions == null) || (completions.size() == 1) || (editors == null))
      return;

    completionIndex--;

    if (completionIndex < 0) {
      completionIndex += completions.size();
    }

    completionIndex %= completions.size();

    if (currentGUI.isOfferingCompletion()) {
      currentGUI
          .cycleCompletion(editors.getCurrentEditor(), completions, lastStub, completionIndex);
    }
  }

  /**
   * Inserts <code>completion</code> into the current editor at the current position.
   */
  public void insertCompletion(Completion completion) {
    if (!enableAutoCompletion)
      return;

    completions = null;

    if (currentGUI != null) {
      currentGUI.stopOffer();
    }
    if (editors == null)
      return;

    JTextComponent j = editors.getCurrentEditor();
    int caretPos = j.getCaretPosition();
    int lineBounds[] = AutoCompletion.getCurrentLineBounds(j.getText(), caretPos);
    String line = AutoCompletion.getCurrentLine(j).substring(0, caretPos - lineBounds[0]);
    String stub;
    completer.getCompletions(editors.getCurrentUnit(), line, null);
    List<OrderToken> tokens = completer.getParserTokens();
    if (tokens != null) {
      // if (completer.getParser() != null) {
      // completer.getParser().parse(line, editors.getCurrentUnit().getLocale()).getTokens();
      stub = AutoCompletion.getStub(tokens);
    } else {
      // try your best...
      stub = AutoCompletion.getStub(line);
    }
    String newLine = completion.replace(line, stub);
    if (caretPos != j.getText().length()) {
      // add additional blank if we are inside the line
      char c = j.getText().charAt(caretPos);
      if (!Character.isWhitespace(c)) {
        newLine += " ";
      }
    }
    try {
      j.getDocument().remove(lineBounds[0], caretPos - lineBounds[0]);
      j.getDocument().insertString(lineBounds[0], newLine, AutoCompletion.SIMPLEATTRIBUTESET);
      j.getCaret().setDot(lineBounds[0] + newLine.length() - completion.getCursorOffset());

      // pavkovic 2003.03.04: enforce focus request
      j.requestFocusInWindow();
    } catch (BadLocationException exc) {
      AutoCompletion.log.warn(exc);
    }
  }

  /**
   * Returns the current line in <code>j</code> according to the current caret position or
   * <code>null</code> if there is no valid line for some reason.
   */
  public static String getCurrentLine(JTextComponent j) {
    int offset = j.getCaretPosition();
    int lineBounds[] = AutoCompletion.getCurrentLineBounds(j.getText(), offset);

    if (lineBounds[0] < 0)
      return null;

    lineBounds[1] = Math.min(offset, lineBounds[1]) - lineBounds[0];

    try {
      return j.getText(lineBounds[0], lineBounds[1]);
    } catch (BadLocationException ble) {
      // bail out
    }

    return null;
  }

  /**
   * Returns the line in <code>text</code> that includes <code>offset</code>, that is, the first
   * position of a line break before offset (or 0) and the last position of a line break after
   * offset (or <code>text.length()</code>.
   *
   * @return an array of two ints, the first one is the start position, the second one is (one
   *         character after) the end position.
   */
  public static int[] getCurrentLineBounds(String text, int offset) {
    int ret[] = { -1, -1 };

    if ((offset > -1) && (offset <= text.length())) {
      for (ret[0] = offset - 1; ret[0] > -1; ret[0]--) {
        char c = text.charAt(ret[0]);

        if ((c == '\n') || (c == '\r')) {
          break;
        }
      }

      ret[0]++;
      ret[1] = text.indexOf('\n', offset);

      if (ret[1] == -1) {
        ret[1] = text.length();
      }
    }

    return ret;
  }

  /**
   * @see EresseaOrderCompleter#getStub(List)
   */
  public static String getStub(List<OrderToken> txt) {
    return AbstractOrderCompleter.getStub(txt);
  }

  /**
   * @see EresseaOrderCompleter#getStub(String)
   */
  public static String getStub(String txt) {
    return AbstractOrderCompleter.getStub(txt);
  }

  /**
   * @see java.awt.event.KeyListener#keyReleased(KeyEvent)
   */
  public void keyReleased(KeyEvent p1) {
    // dummy implementation
  }

  /**
   * Handles special keys to control the GUI, mainly.
   */
  public void keyPressed(KeyEvent e) {
    if (!enableAutoCompletion || (currentGUI == null) || !currentGUI.isOfferingCompletion())
      return;

    if (isCompleterKey(e, CK_FORWARD)) {
      cycleForward();
      e.consume();

      return;
    }

    if (isCompleterKey(e, CK_BACKWARD)) {
      cycleBackward();
      e.consume();

      return;
    }

    if (isCompleterKey(e, CK_COMPLETE)) {
      Completion cmp = currentGUI.getSelectedCompletion();

      if (cmp != null) {
        insertCompletion(cmp);
      }

      e.consume();

      return;
    }

    if (isCompleterKey(e, CK_BREAK)) {
      currentGUI.stopOffer();

      return;
    }

    if (isCompleterKey(e, CK_START))
      return;

    boolean plain = (e.getModifiersEx() == 0);

    if (!plain)
      return;
    if (!Character.isLetterOrDigit(e.getKeyChar())) {
      currentGUI.stopOffer();
    }

    int keys[] = currentGUI.getSpecialKeys();

    if (keys != null) {
      int code = e.getKeyCode();
      for (int key : keys) {
        if (code == key) {
          currentGUI.specialKeyPressed(code);
          e.consume();
        }
      }
    }
  }

  public boolean isCompleterKey(KeyEvent e, int i) {
    KeyStroke ks = KeyStroke.getKeyStrokeForEvent(e);
    return ks.equals(completerKeys[i]);
  }

  /**
   * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
   */
  public void keyTyped(java.awt.event.KeyEvent p1) {
    // dummy implementation
  }

  /**
   * Offer auto completion when timer has fired.
   *
   * @param e
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(java.awt.event.ActionEvent e) {
    if (AutoCompletion.log.isDebugEnabled()) {
      AutoCompletion.log.debug("AutoCompletion.actionPerformed called with " + e);
    }

    timer.stop();

    if (enableAutoCompletion && !hotKeyMode && editors != null) {
      offerCompletion(editors.getCurrentEditor(), false);
    }
  }

  /**
   * updates the completion gui when the user clicks inside editor or moves cursor.
   *
   * @param e
   */
  public void caretUpdate(javax.swing.event.CaretEvent e) {
    if (AutoCompletion.log.isDebugEnabled()) {
      AutoCompletion.log.debug("AutoCompletion.caretUpdate called with " + e);
    }

    if (enableAutoCompletion && (currentGUI != null) && !currentGUI.editorMayUpdateCaret()) {
      // currentGUI.stopOffer();
      if (currentGUI.isOfferingCompletion()) {
        SwingUtilities.invokeLater(new Runnable() {

          public void run() {
            if (editors != null) {
              offerCompletion(editors.getCurrentEditor(), false);
            }
          }
        });

      }
      timer.restart();
    }
  }

  /**
   * @see magellan.library.event.GameDataListener#gameDataChanged(magellan.library.event.GameDataEvent)
   */
  public void gameDataChanged(GameDataEvent e) {
    setCompleter(e.getGameData().getGameSpecificStuff().getCompleter(e.getGameData(), this));

    if (currentGUI != null) {
      currentGUI.stopOffer();
    }
  }

  /**
   * @see magellan.client.swing.completion.OrderEditorList#getPreferencesAdapter()
   */
  public PreferencesAdapter getPreferencesAdapter() {
    return new DetailsViewAutoCompletionPreferences(this);
  }

  /**
   * Possibly activates the GUI.
   *
   * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
   */
  public void focusGained(java.awt.event.FocusEvent e) {
    if (AutoCompletion.log.isDebugEnabled()) {
      AutoCompletion.log.debug("AutoCompletion.focusGained called with " + e);
    }

    if (enableAutoCompletion) {
      timer.restart();
    }
  }

  /**
   * Hides the GUI.
   *
   * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
   */
  public void focusLost(java.awt.event.FocusEvent p1) {
    if ((currentGUI != null) && (!currentGUI.editorMayLoseFocus())) {
      currentGUI.stopOffer();
    }
  }

  /**
   * Completely enables or disables auto completion.
   */
  public void setEnableAutoCompletion(boolean b) {
    enableAutoCompletion = b;
    settings.setProperty(PropertiesHelper.AUTOCOMPLETION_ENABLED, enableAutoCompletion ? "true"
        : "false");

    if (!b && (currentGUI != null)) {
      currentGUI.stopOffer();
    }
  }

  /**
   * Returns <code>true</code> if auto completion is enabled.
   */
  public boolean isEnableAutoCompletion() {
    return enableAutoCompletion;
  }

  /**
   * Control if completion of MACHE orders should be limited
   */
  public void setLimitMakeCompletion(boolean value) {
    limitMakeCompletion = value;
    settings.setProperty(PropertiesHelper.AUTOCOMPLETION_LIMIT_MAKE_COMPLETION, value ? "true"
        : "false");
  }

  /**
   * Returns if completion of MACHE orders is limited
   */
  public boolean getLimitMakeCompletion() {
    return limitMakeCompletion;
  }

  /**
   * Controls if completion should start on an empty word or requires at least one letter.
   */
  public void setEmptyStubMode(boolean b) {
    emptyStubMode = b;
    settings.setProperty(PropertiesHelper.AUTOCOMPLETION_EMPTY_STUB_MODE, emptyStubMode ? "true"
        : "false");
  }

  /**
   * Returns if completion should start on an empty word or requires at least one letter.
   */
  public boolean getEmptyStubMode() {
    return emptyStubMode;
  }

  /**
   * Setter for hot key mode
   *
   * @param b
   */
  public void setHotKeyMode(boolean b) {
    hotKeyMode = b;
    settings
        .setProperty(PropertiesHelper.AUTOCOMPLETION_HOTKEY_MODE, hotKeyMode ? "true" : "false");
  }

  /**
   * Getter for hot key mode
   */
  public boolean getHotKeyMode() {
    return hotKeyMode;
  }

  /**
   * Returns the time (in ms) after a key stroke until activating the GUI.
   */
  public int getActivationTime() {
    return time;
  }

  /**
   * Sets the time (in ms) after a key stroke until activating the GUI.
   */
  public void setActivationTime(int t) {
    time = t;

    if (time < 1) {
      time = 1;
    }

    settings.setProperty(PropertiesHelper.AUTOCOMPLETION_TIME, String.valueOf(time));
    timer.setDelay(time);
    timer.setInitialDelay(time);
  }

  /**
   * Returns keys for cycling, completing and breaking.
   *
   * <pre>
   * cycle forward
   * cycle backward
   * complete
   * break
   * start
   * </pre>
   */
  public KeyStroke[] getCompleterKeys() {
    return completerKeys;
  }

  /**
   * Sets keys for cycling, completing and breaking.
   *
   * <pre>
   * cycle forward
   * cycle backward
   * complete
   * break
   * start
   * </pre>
   */
  public void setCompleterKeys(KeyStroke ck[]) {
    completerKeys = ck;
    PropertiesHelper.setKeyStroke(settings, PropertiesHelper.AUTOCOMPLETION_KEYS_CYCLE_FORWARD, ck[CK_FORWARD]);
    PropertiesHelper.setKeyStroke(settings, PropertiesHelper.AUTOCOMPLETION_KEYS_CYCLE_BACKWARD, ck[CK_BACKWARD]);
    PropertiesHelper.setKeyStroke(settings, PropertiesHelper.AUTOCOMPLETION_KEYS_COMPLETE, ck[CK_COMPLETE]);
    PropertiesHelper.setKeyStroke(settings, PropertiesHelper.AUTOCOMPLETION_KEYS_BREAK, ck[CK_BREAK]);
    PropertiesHelper.setKeyStroke(settings, PropertiesHelper.AUTOCOMPLETION_KEYS_START, ck[CK_START]);
  }

  /**
   * Returns a list containing the self defined completions as Completion objects.
   */
  public List<Completion> getSelfDefinedCompletions() {
    List<Completion> retVal = new Vector<Completion>();

    for (String name : selfDefinedCompletions.keySet()) {
      String value = selfDefinedCompletions.get(name);
      Completion c = new Completion(name, value, "", 1);
      retVal.add(c);
    }

    return retVal;
  }

  /**
   * Sets the value of completionGUIs.
   *
   * @param completionGUIs The value for completionGUIs.
   */
  public void setCompletionGUIs(Vector<CompletionGUI> completionGUIs) {
    this.completionGUIs = completionGUIs;
  }

  /**
   * Returns the value of settings.
   *
   * @return Returns settings.
   */
  public Properties getSettings() {
    return settings;
  }

  /**
   * Sets the value of settings.
   *
   * @param settings The value for settings.
   */
  public void setSettings(Properties settings) {
    this.settings = settings;
  }

  /**
   * Sets the value of selfDefinedCompletions.
   *
   * @param selfDefinedCompletions The value for selfDefinedCompletions.
   */
  public void setSelfDefinedCompletions(Map<String, String> selfDefinedCompletions) {
    this.selfDefinedCompletions = selfDefinedCompletions;
  }

  /**
   * Returns the value of selfDefinedCompletions.
   */
  public Map<String, String> getSelfDefinedCompletionsMap() {
    return selfDefinedCompletions;
  }

  /**
   * Returns the value of limitCompletions.
   *
   * @return Returns limitCompletions.
   */
  public boolean isLimitCompletions() {
    return limitCompletions;
  }

  /**
   * Sets the value of limitCompletions.
   *
   * @param limitCompletions The value for limitCompletions.
   */
  public void setLimitCompletions(boolean limitCompletions) {
    this.limitCompletions = limitCompletions;
  }
}
