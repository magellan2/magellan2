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

package magellan.client.completion;

import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.Timer;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;

import magellan.client.MagellanContext;
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
import magellan.library.utils.OrderedHashtable;
import magellan.library.utils.PropertiesHelper;
import magellan.library.utils.logging.Logger;

/**
 * DOCUMENT ME!
 * 
 * @author Andreas Gampe, Ulrich Küster
 */
public class AutoCompletion implements SelectionListener, KeyListener, ActionListener, CaretListener, FocusListener, GameDataListener, CompleterSettingsProvider {
  private static final Logger log = Logger.getInstance(AutoCompletion.class);
  private OrderEditorList editors;
  private Vector<CompletionGUI> completionGUIs;
  private CompletionGUI currentGUI;
  private Completer completer;
  private int lastCaretPosition = 0;

  /**
   * Keys for cycling, completing and breaking. completerKeys[]
   * completerKeys[][0] completerKeys[1] cycle forward modifier key cycle
   * backward modifier key complete modifier key break modifier key
   */
  private int completerKeys[][];
  private Timer timer;
  private List<Completion> completions = null;
  private int completionIndex = 0;
  private String lastStub = null;
  private boolean enableAutoCompletion = true;

  // limits the completion of the make-order to items
  // whose resources are available
  private boolean limitMakeCompletion = true;
  private boolean emptyStubMode = false;
  private String activeGUI = null;
  private int time = 150;

  // self defined completion objects (mapping a name (String) to a value
  // (String))
  private Map<String, String> selfDefinedCompletions = new Hashtable<String, String>();
  private Map<String, String> selfDefinedCompletions2 = new Hashtable<String, String>();

  protected Properties settings;
  private Properties completionSettings;

  /**
   * Creates new AutoCompletion
   * 
   * @param context
   *          The magellan context holding Client-Global informations
   */
  public AutoCompletion(MagellanContext context) {
    this.settings = context.getProperties();
    this.completionSettings = context.getCompletionProperties();
    context.getEventDispatcher().addSelectionListener(this);
    context.getEventDispatcher().addGameDataListener(this);

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
        settings.setProperty(PropertiesHelper.AUTOCOMPLETION_ENABLED, enableAutoCompletion ? "true" : "false");
      } else {
        enableAutoCompletion = true;
      }
    }

    limitMakeCompletion = settings.getProperty(PropertiesHelper.AUTOCOMPLETION_LIMIT_MAKE_COMPLETION, "true").equalsIgnoreCase("true");

    String stubMode = settings.getProperty(PropertiesHelper.AUTOCOMPLETION_EMPTY_STUB_MODE, "true");
    emptyStubMode = stubMode.equalsIgnoreCase("true");

    activeGUI = settings.getProperty(PropertiesHelper.AUTOCOMPLETION_COMPLETION_GUI, "List");

    try {
      time = Integer.parseInt(settings.getProperty(PropertiesHelper.AUTOCOMPLETION_TIME, "150"));
    } catch (Exception exc) {
      time = 150;
    }

    completerKeys = new int[4][2]; // cycle forward, cycle backward, complete,
                                    // break

    String cycleForward = settings.getProperty(PropertiesHelper.AUTOCOMPLETION_KEYS_CYCLE_FORWARD);

    try {
      StringTokenizer st = new StringTokenizer(cycleForward, ",");
      completerKeys[0][0] = Integer.parseInt(st.nextToken());
      completerKeys[0][1] = Integer.parseInt(st.nextToken());
    } catch (Exception exc) {
      completerKeys[0][0] = InputEvent.CTRL_MASK;
      completerKeys[0][1] = KeyEvent.VK_DOWN;
    }

    String cycleBackward = settings.getProperty(PropertiesHelper.AUTOCOMPLETION_KEYS_CYCLE_BACKWARD);

    try {
      StringTokenizer st = new StringTokenizer(cycleBackward, ",");
      completerKeys[1][0] = Integer.parseInt(st.nextToken());
      completerKeys[1][1] = Integer.parseInt(st.nextToken());
    } catch (Exception exc) {
      completerKeys[1][0] = InputEvent.CTRL_MASK;
      completerKeys[1][1] = KeyEvent.VK_UP;
    }

    String complete = settings.getProperty(PropertiesHelper.AUTOCOMPLETION_KEYS_COMPLETE);

    try {
      StringTokenizer st = new StringTokenizer(complete, ",");
      completerKeys[2][0] = Integer.parseInt(st.nextToken());
      completerKeys[2][1] = Integer.parseInt(st.nextToken());
    } catch (Exception exc) {
      completerKeys[2][0] = 0;
      completerKeys[2][1] = KeyEvent.VK_TAB;
    }

    String breakKey = settings.getProperty(PropertiesHelper.AUTOCOMPLETION_KEYS_BREAK);

    try {
      StringTokenizer st = new StringTokenizer(breakKey, ",");
      completerKeys[3][0] = Integer.parseInt(st.nextToken());
      completerKeys[3][1] = Integer.parseInt(st.nextToken());
    } catch (Exception exc) {
      completerKeys[3][0] = 0;
      completerKeys[3][1] = KeyEvent.VK_ESCAPE;
    }

    selfDefinedCompletions = getSelfDefinedCompletions(settings);
    selfDefinedCompletions2 = getSelfDefinedCompletions(completionSettings);

  }

  /**
   * read defined completions from given property file
   * 
   * @return Map containing name to
   */
  private Map<String,String> getSelfDefinedCompletions(Properties aSettings) {
    Map<String, String> result = new OrderedHashtable<String, String>();

    // load selfdefined completions
    List<String> completionNames = PropertiesHelper.getList(aSettings, "AutoCompletion.SelfDefinedCompletions.name");
    for (int i = 0, size = completionNames.size(); i < size; i++) {
      String name = completionNames.get(i);
      String value = (String)aSettings.get("AutoCompletion.SelfDefinedCompletions.value" + i);
      if (name != null && value != null) {
        result.put(name, value);
      }
    }

    return result;
  }

  /**
   * to choose the current GUI DOCUMENT-ME
   */
  public void loadComplete() {
    if (completionGUIs.size() == 0) {
      return;
    }

    if (activeGUI == null) {
      setCurrentGUI(completionGUIs.get(0));

      return;
    }

    Iterator it = completionGUIs.iterator();

    while (it.hasNext()) {
      CompletionGUI cGUI = (CompletionGUI) it.next();

      if (cGUI.getTitle().equals(activeGUI)) {
        setCurrentGUI(cGUI);

        return;
      }
    }

    setCurrentGUI(completionGUIs.get(0));
  }

  /**
   * DOCUMENT-ME
   * 
   * @param cel
   *          DOCUMENT-ME
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
   * DOCUMENT-ME
   * 
   * @param cGUI
   *          DOCUMENT-ME
   */
  public void addCompletionGUI(CompletionGUI cGUI) {
    cGUI.init(this);
    completionGUIs.add(cGUI);
  }

  /**
   * DOCUMENT-ME
   * 
   * 
   */
  public Vector<CompletionGUI> getCompletionGUIs() {
    return completionGUIs;
  }

  /**
   * DOCUMENT-ME
   * 
   * @param cGUI
   *          DOCUMENT-ME
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
   * DOCUMENT-ME
   * 
   * 
   */
  public CompletionGUI getCurrentGUI() {
    return currentGUI;
  }

  /**
   * DOCUMENT-ME
   * 
   * @param c
   *          DOCUMENT-ME
   */
  public void setCompleter(Completer c) {
    completer = c;
  }

  /**
   * DOCUMENT-ME
   * 
   * @param e
   *          DOCUMENT-ME
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
    }

    // search for new completion
    JTextComponent j = editors.getCurrentEditor();

    if ((j != null) && enableAutoCompletion) {
      timer.restart();
    }
  }

  protected void offerCompletion(JTextComponent j) {
    if (!enableAutoCompletion || (currentGUI == null) || (completer == null) || (j == null) || (completer == null) || !j.isVisible()) {
      return;
    }

    // find the piece of text
    String line = AutoCompletion.getCurrentLine(j);

    if (line == null) {
      return;
    }

    if (false) {
      completions = null;
    }
    if ((line.length() == 0) || ((j.getCaretPosition() > 0) && (j.getText().charAt(j.getCaretPosition() - 1) == '\n')) || ((j.getCaretPosition() > 0) && (j.getText().charAt(j.getCaretPosition() - 1) == '\r'))) {
      // new line, delete old completions
      completions = null;
    } else if (lastCaretPosition > j.getCaretPosition()) {
      // Caret went backwards so line must be parsed again
      completions = null;
    } else if ((j.getCaretPosition() > 0) && (j.getText().charAt(j.getCaretPosition() - 1) == ' ')) {
      // if Space typed, delete old completions to enforce new parsing
      completions = null;
    }

    // remember CaretPosition
    lastCaretPosition = j.getCaretPosition();

    // run completer engine
    if (editors.getCurrentUnit() != null) {
      completions = completer.getCompletions(editors.getCurrentUnit(), line, completions);

      addCommonCompletion(AutoCompletion.getStub(line));
      
      // determine, whether the cursor is inside a word
      boolean inWord = true;

      if ((j.getText().length() == j.getCaretPosition()) || (j.getCaretPosition() == 0)) {
        inWord = false;
      } else {
        char c = j.getText().charAt(j.getCaretPosition());

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
      if (line.length() > 0) {
        String stub = AutoCompletion.getStub(line);
        lastStub = stub;
        
        // Fiete: try to detect, if we fully typed a offered completion
        // ennos feature wish: in that case do not offer the completion anymore
        // we check, if we have only one completion left AND if the last word typed is just
        // this last completion - in that case we do not offer the completion
        boolean completionCompleted = false;
        if ((completions != null) && (completions.size() == 1) && !inWord){
          // we have just 1 completion avail and we are not in a word
          Completion c = completions.iterator().next();
          if (c.getValue().equalsIgnoreCase(stub)){
            // last completion equals last fully typed word
            completionCompleted=true;
          }
        }

        if ((emptyStubMode || (stub.length() > 0)) && (completions != null) && (completions.size() > 0) && !inWord && !completionCompleted) {
          currentGUI.offerCompletion(j, completions, stub);
          completionIndex = 0;
        }
      }
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
      if (common > 0 && reference.getName().length()!=common && reference.getName().charAt(common)==' ') {
        common--;
      }
      if (common > 0) {
        String commonPart = reference.getName().substring(0, common);
        if (stub.length()<common) {
          completions.add(0, new Completion(commonPart + "...", commonPart, "", 0));
        }
      }
    }
  }

  /**
   * DOCUMENT-ME
   */
  public void cycleForward() {
    if ((completions == null) || (completions.size() == 1) || (editors == null)) {
      return;
    }

    completionIndex++;
    completionIndex %= completions.size();

    if (currentGUI.isOfferingCompletion()) {
      currentGUI.cycleCompletion(editors.getCurrentEditor(), completions, lastStub, completionIndex);
    }
  }

  /**
   * DOCUMENT-ME
   */
  public void cycleBackward() {
    if ((completions == null) || (completions.size() == 1) || (editors == null)) {
      return;
    }

    completionIndex--;

    if (completionIndex < 0) {
      completionIndex += completions.size();
    }

    completionIndex %= completions.size();

    if (currentGUI.isOfferingCompletion()) {
      currentGUI.cycleCompletion(editors.getCurrentEditor(), completions, lastStub, completionIndex);
    }
  }

  /**
   * DOCUMENT-ME
   * 
   * @param completion
   *          DOCUMENT-ME
   */
  public void insertCompletion(Completion completion) {
    if (!enableAutoCompletion) {
      return;
    }

    completions = null;

    if (currentGUI != null) {
      currentGUI.stopOffer();
    }

    JTextComponent j = editors.getCurrentEditor();
    int caretPos = j.getCaretPosition();
    String line = AutoCompletion.getCurrentLine(j);
    String stub = AutoCompletion.getStub(line);
    int stubLen = stub.length();
    int stubBeg = caretPos - stubLen;
    String text = j.getText();
    
    if (stubLen>0){
      int temp1 = text.indexOf('\n', caretPos);

      if (temp1 == -1) {
        temp1 = Integer.MAX_VALUE;
      }

      int temp2 = text.indexOf('\r', caretPos);

      if (temp2 == -1) {
        temp2 = Integer.MAX_VALUE;
      }

      int temp3 = text.indexOf('\t', caretPos);

      if (temp3 == -1) {
        temp3 = Integer.MAX_VALUE;
      }

      int temp4 = text.indexOf(' ', caretPos);

      if (temp4 == -1) {
        temp4 = Integer.MAX_VALUE;
      }

      temp1 = Math.min(temp1, temp2);
      temp3 = Math.min(temp3, temp4);

      int stubEnd = Math.min(temp1, temp3);

      if (stubEnd == Integer.MAX_VALUE) {
        stubEnd = text.length();
      } else if (text.charAt(stubEnd) == ' ') {
        stubEnd++;
      }

      stubLen = stubEnd - stubBeg;
    }
    
    try {
      j.getDocument().remove(stubBeg, stubLen);
      // add additional blank if we are inside the line
      char c = text.length()>stubBeg+stubLen?text.charAt(stubBeg+stubLen):0;
      if (c=='\n' || c==0) {
        j.getDocument().insertString(stubBeg, completion.getValue(), new SimpleAttributeSet());
      } else if (c==' ') {
        j.getDocument().insertString(stubBeg, completion.getValue(), new SimpleAttributeSet());
      } else {
        j.getDocument().insertString(stubBeg, completion.getValue()+" ", new SimpleAttributeSet());
      }
      j.getCaret().setDot((stubBeg + completion.getValue().length()) - completion.getCursorOffset());

      // pavkovic 2003.03.04: enforce focus request
      j.requestFocus();
    } catch (BadLocationException exc) {
      AutoCompletion.log.info(exc);
    }
  }

  /**
   * DOCUMENT-ME
   * 
   * @param j
   *          DOCUMENT-ME
   * 
   */
  public static String getCurrentLine(JTextComponent j) {
    int offset = j.getCaretPosition();
    int lineBounds[] = AutoCompletion.getCurrentLineBounds(j.getText(), offset);

    if (lineBounds[0] < 0) {
      return null;
    }

    lineBounds[1] = Math.min(offset, lineBounds[1]) - lineBounds[0];

    try {
      return j.getText(lineBounds[0], lineBounds[1]);
    } catch (BadLocationException ble) {
    }

    return null;
  }

  /**
   * DOCUMENT-ME
   * 
   * @param text
   *          DOCUMENT-ME
   * @param offset
   *          DOCUMENT-ME
   * 
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
   * DOCUMENT-ME
   * 
   * @param txt
   *          DOCUMENT-ME
   * 
   */
  public static String getStub(String txt) {
    StringBuffer retVal = new StringBuffer();

    for (int i = txt.length() - 1; i >= 0; i--) {
      char c = txt.charAt(i);

//      if ((c == '"') || (c == '\'') || (c == '_') || (c == '-') || (Character.isLetterOrDigit(c) == true)) {
      if ((!Character.isWhitespace(c) && c!='\'' && c!='"' && c!='@') ) {
//      if ((!Character.isWhitespace(c))) {
        retVal.append(c);
      } else {
        break;
      }
    }

    return retVal.reverse().toString();
  }

  /**
   * DOCUMENT-ME
   * 
   * @param p1
   *          DOCUMENT-ME
   */
  public void keyReleased(java.awt.event.KeyEvent p1) {
  }

  /**
   * DOCUMENT-ME
   * 
   * @param e
   *          DOCUMENT-ME
   */
  public void keyPressed(java.awt.event.KeyEvent e) {
    if (!enableAutoCompletion || (currentGUI == null) || !currentGUI.isOfferingCompletion()) {
      return;
    }

    int code = e.getKeyCode();
    int modifiers = e.getModifiers();
    boolean plain = (e.getModifiers() == 0);

    if ((completerKeys[0][0] == modifiers) && (completerKeys[0][1] == code)) {
      cycleForward();
      e.consume();

      return;
    }

    if ((completerKeys[1][0] == modifiers) && (completerKeys[1][1] == code)) {
      cycleBackward();
      e.consume();

      return;
    }

    if ((completerKeys[2][0] == modifiers) && (completerKeys[2][1] == code)) {
      Completion cmp = currentGUI.getSelectedCompletion();

      if (cmp != null) {
        insertCompletion(cmp);
      }

      e.consume();

      return;
    }

    if ((completerKeys[3][0] == modifiers) && (completerKeys[3][1] == code)) {
      currentGUI.stopOffer();

      return;
    }

    if (!plain) {
      return;
    }

    int keys[] = currentGUI.getSpecialKeys();

    if (keys != null) {
      for (int i = 0; i < keys.length; i++) {
        if (code == keys[i]) {
          currentGUI.specialKeyPressed(code);
          e.consume();
        }
      }
    }
  }

  /**
   * DOCUMENT-ME
   * 
   * @param p1
   *          DOCUMENT-ME
   */
  public void keyTyped(java.awt.event.KeyEvent p1) {
  }

  /**
   * DOCUMENT-ME
   * 
   * @param e
   *          DOCUMENT-ME
   */
  public void actionPerformed(java.awt.event.ActionEvent e) {
    if (AutoCompletion.log.isDebugEnabled()) {
      AutoCompletion.log.debug("AutoCompletion.actionPerformed called with " + e);
    }

    timer.stop();

    if (enableAutoCompletion) {
      offerCompletion(editors.getCurrentEditor());
    }
  }

  /**
   * DOCUMENT-ME
   * 
   * @param e
   *          DOCUMENT-ME
   */
  public void caretUpdate(javax.swing.event.CaretEvent e) {
    if (AutoCompletion.log.isDebugEnabled()) {
      AutoCompletion.log.debug("AutoCompletion.caretUpdate called with " + e);
    }

    if (enableAutoCompletion && (currentGUI != null) && !currentGUI.editorMayUpdateCaret()) {
      currentGUI.stopOffer();
      timer.restart();
    }
  }

  /**
   * DOCUMENT-ME
   * 
   * @param e
   *          DOCUMENT-ME
   */
  public void gameDataChanged(GameDataEvent e) {
    setCompleter(e.getGameData().getGameSpecificStuff().getCompleter(e.getGameData(), this));

    if (currentGUI != null) {
      currentGUI.stopOffer();
    }
  }

  /**
   * DOCUMENT-ME
   * 
   * 
   */
  public PreferencesAdapter getPreferencesAdapter() {
    return new DetailsViewAutoCompletionPreferences(this);
  }

  /**
   * DOCUMENT-ME
   * 
   * @param e
   *          DOCUMENT-ME
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
   * DOCUMENT-ME
   * 
   * @param p1
   *          DOCUMENT-ME
   */
  public void focusLost(java.awt.event.FocusEvent p1) {
    if ((currentGUI != null) && !currentGUI.editorMayLoseFocus()) {
      currentGUI.stopOffer();
    }
  }

  /**
   * DOCUMENT-ME
   * 
   * @param b
   *          DOCUMENT-ME
   */
  public void setEnableAutoCompletion(boolean b) {
    enableAutoCompletion = b;
    settings.setProperty(PropertiesHelper.AUTOCOMPLETION_ENABLED, enableAutoCompletion ? "true" : "false");

    if (!b && (currentGUI != null)) {
      currentGUI.stopOffer();
    }
  }

  /**
   * DOCUMENT-ME
   * 
   * 
   */
  public boolean isEnableAutoCompletion() {
    return enableAutoCompletion;
  }

  /**
   * DOCUMENT-ME
   * 
   * @param value
   *          DOCUMENT-ME
   */
  public void setLimitMakeCompletion(boolean value) {
    limitMakeCompletion = value;
    settings.setProperty(PropertiesHelper.AUTOCOMPLETION_LIMIT_MAKE_COMPLETION, value ? "true" : "false");
  }

  /**
   * DOCUMENT-ME
   * 
   * 
   */
  public boolean getLimitMakeCompletion() {
    return limitMakeCompletion;
  }

  /**
   * DOCUMENT-ME
   * 
   * @param b
   *          DOCUMENT-ME
   */
  public void setEmptyStubMode(boolean b) {
    emptyStubMode = b;
    settings.setProperty(PropertiesHelper.AUTOCOMPLETION_EMPTY_STUB_MODE, emptyStubMode ? "true" : "false");
  }

  /**
   * DOCUMENT-ME
   * 
   * 
   */
  public boolean getEmptyStubMode() {
    return emptyStubMode;
  }

  /**
   * DOCUMENT-ME
   * 
   * 
   */
  public int getActivationTime() {
    return time;
  }

  /**
   * DOCUMENT-ME
   * 
   * @param t
   *          DOCUMENT-ME
   */
  public void setActivationTime(int t) {
    time = t;

    if (time < 1) {
      time = 1;
    }

    settings.setProperty(PropertiesHelper.AUTOCOMPLETION_TIME, String.valueOf(time));
    timer.setDelay(time);
  }

  /**
   * DOCUMENT-ME
   * 
   * 
   */
  public int[][] getCompleterKeys() {
    return completerKeys;
  }

  /**
   * DOCUMENT-ME
   * 
   * @param ck
   *          DOCUMENT-ME
   */
  public void setCompleterKeys(int ck[][]) {
    completerKeys = ck;
    settings.setProperty(PropertiesHelper.AUTOCOMPLETION_KEYS_CYCLE_FORWARD, String.valueOf(ck[0][0]) + ',' + String.valueOf(ck[0][1]));
    settings.setProperty(PropertiesHelper.AUTOCOMPLETION_KEYS_CYCLE_BACKWARD, String.valueOf(ck[1][0]) + ',' + String.valueOf(ck[1][1]));
    settings.setProperty(PropertiesHelper.AUTOCOMPLETION_KEYS_COMPLETE, String.valueOf(ck[2][0]) + ',' + String.valueOf(ck[2][1]));
    settings.setProperty(PropertiesHelper.AUTOCOMPLETION_KEYS_BREAK, String.valueOf(ck[3][0]) + ',' + String.valueOf(ck[3][1]));
  }

  /**
   * Returns a list containing the self defined completions as Completion
   * objects.
   */
  public List<Completion> getSelfDefinedCompletions() {
    List<Completion> retVal = new Vector<Completion>();

    for (Iterator<String> iter = selfDefinedCompletions.keySet().iterator(); iter.hasNext();) {
      String name = iter.next();
      String value = selfDefinedCompletions.get(name);
      Completion c = new Completion(name, value, "", 0);
      retVal.add(c);
    }

    for (Iterator<String> iter = selfDefinedCompletions2.keySet().iterator(); iter.hasNext();) {
      String name = iter.next();
      String value = selfDefinedCompletions2.get(name);
      Completion c = new Completion(name, value, "", 0);
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
}
