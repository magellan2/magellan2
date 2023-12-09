/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.client.swing.completion;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIDefaults;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;
import javax.swing.undo.UndoManager;

import magellan.client.event.EventDispatcher;
import magellan.client.event.SelectionEvent;
import magellan.client.event.SelectionListener;
import magellan.client.event.UnitOrdersEvent;
import magellan.client.event.UnitOrdersListener;
import magellan.client.swing.context.ContextFactory;
import magellan.client.swing.context.UnitContextFactory;
import magellan.client.swing.tree.Changeable;
import magellan.client.utils.Colors;
import magellan.library.GameData;
import magellan.library.Order;
import magellan.library.Unit;
import magellan.library.completion.OrderParser;
import magellan.library.utils.MergeLineReader;
import magellan.library.utils.OrderToken;
import magellan.library.utils.guiwrapper.CacheableOrderEditor;
import magellan.library.utils.logging.Logger;

/**
 * A text pane for convenient editing and handling of Eressea orders.
 */
public class OrderEditor extends JTextPane implements DocumentListener, KeyListener,
    SelectionListener, FocusListener, CacheableOrderEditor, Changeable {
  private static final Logger log = Logger.getInstance(OrderEditor.class);

  // Style name constants

  /** Text style REGULAR */
  public static final String S_REGULAR = "regular";
  /** Text style REGULAR inverted */
  public static final String S_REGULAR_INV = "regular_inv";

  /** Text style KEYWORD */
  public static final String S_KEYWORD = "keyword";
  /** Text style KEYWORD inverted */
  public static final String S_KEYWORD_INV = "keyword_inv";

  /** Text style STRING */
  public static final String S_STRING = "string";
  /** Text style STRING inverted */
  public static final String S_STRING_INV = "string_inv";

  /** Text style NUMBER */
  public static final String S_NUMBER = "number";
  /** Text style NUMBER inverted */
  public static final String S_NUMBER_INV = "number_inv";

  /** Text style ID */
  public static final String S_ID = "id";
  /** Text style ID inverted */
  public static final String S_ID_INV = "id_inv";

  /** Text style COMMENT */
  public static final String S_COMMENT = "comment";
  /** Text style COMMENT inverted */
  public static final String S_COMMENT_INV = "comment_inv";

  /** Text style LONGORDER */
  public static final String S_LONGORDER = "longorder";
  /** Text style LONGORDER inverted */
  public static final String S_LONGORDER_INV = "longorder_inv";

  private Color errorColor;

  private Unit unit = null;
  private boolean modified = false;
  private List<String> orders = new LinkedList<String>();
  private OrderParser parser = null;
  private Properties settings = null;
  private boolean highlightSyntax = true;
  private boolean ignoreModifications = false;
  private EventDispatcher dispatcher = null;

  // private GameData data = null;
  // TODO undoing disabled
  // private UndoManager undoMgr = null;

  // keep this udpate runnable instead of re-creating it over and over again
  private DocumentUpdateRunnable docUpdateThread = new DocumentUpdateRunnable(null);
  private OrderEditorCaret myCaret = null;

  private UnitOrdersListener orderListener;

  /**
   * Creates a new OrderEditor object.
   */
  public OrderEditor(GameData data, Properties settings, UndoManager _undoMgr, EventDispatcher d,
      OrderParser parser) {
    super();

    // pavkovic 2002.11.11: use own caret for more logical refreshing
    myCaret = new OrderEditorCaret();
    setCaret(myCaret);

    this.settings = settings;

    this.parser = parser; // (data != null) ? data.getGameSpecificStuff().getOrderParser(data) :
    // null;

    dispatcher = d;

    // this.dispatcher.addGameDataListener(this);
    highlightSyntax =
        (Boolean.valueOf(settings.getProperty("OrderEditor.highlightSyntax", "true"))
            .booleanValue());

    getDocument().addDocumentListener(this);

    // TODO stm undo is deactivated
    // undoMgr = _undoMgr;
    // SignificantUndos sigUndos = new SignificantUndos();
    // getDocument().addUndoableEditListener(sigUndos);
    // getDocument().addDocumentListener(sigUndos);

    addKeyListener(this);
    addFocusListener(this);

    orderListener = new UnitOrdersListener() {
      public void unitOrdersChanged(UnitOrdersEvent e) {
        // refresh local copy of orders in editor
        if (e.getUnit() != null && e.getSource() != OrderEditor.this && e.getUnit().equals(unit)) {
          setOrders(unit.getOrders2());
        }
      }
    };

    errorColor = Colors.decode(settings.getProperty("OrderEditor.errorBgColor", "255,128,0"));

    initStyles();
  }

  /**
   * @param textPane
   * @param charactersPerTab
   */
  public static void setTabs(final JTextPane textPane, int charactersPerTab) {
    FontMetrics fm = textPane.getFontMetrics(textPane.getFont());
    int charWidth = fm.charWidth(' ');
    int tabWidth = charWidth * charactersPerTab;

    TabStop[] tabs = new TabStop[6];

    for (int j = 0; j < tabs.length; j++) {
      int tab = j + 1;
      tabs[j] = new TabStop(tab * tabWidth);
    }

    TabSet tabSet = new TabSet(tabs);
    SimpleAttributeSet attributes = new SimpleAttributeSet();
    StyleConstants.setTabSet(attributes, tabSet);
    int length = textPane.getDocument().getLength();
    textPane.getStyledDocument().setParagraphAttributes(0, length, attributes, false);
  }

  /**
   * registers an OrderListener with the event dispatcher
   */
  public void registerListener() {
    dispatcher.addUnitOrdersListener(orderListener);
  }

  /**
   * release an OrderListener with the event dispatcher
   */
  public boolean releaseListener() {
    return dispatcher.removeUnitOrdersListener(orderListener);
  }

  @Override
  protected void finalize() throws Throwable {
    if (releaseListener())
      if (OrderEditor.log != null) {
        OrderEditor.log.error("listener wasn't removed for OrderEditor " + unit + " " + this);
      } else {
        System.err.println("listener wasn't removed for OrderEditor " + unit + " " + this);
      }

    super.finalize();
  }

  // private boolean swingInspected;
  public void changedUpdate(DocumentEvent e) {
  }

  /**
   * DOCUMENT-ME
   *
   * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
   */
  public void insertUpdate(DocumentEvent e) {
    removeUpdate(e);
  }

  /**
   * DOCUMENT-ME
   *
   * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
   */
  public void removeUpdate(DocumentEvent e) {
    if (ignoreModifications)
      return;

    setModified(true);
    fireDelayedOrdersEvent();
    docUpdateThread.setEvent(e);
    SwingUtilities.invokeLater(docUpdateThread);
  }

  Timer timer = null;

  /*
   * Timer support routine for informing other objects about changed orders.
   */
  private void fireDelayedOrdersEvent() {
    if (timer == null) {
      timer = new Timer(700, new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          SwingUtilities.invokeLater(new Runnable() {

            public void run() {
              setOrdersAndFireEvent();
            }
          });
          // dispatcher.fire(new UnitOrdersEvent(OrderEditor.this,unit));
        }
      });
      timer.setRepeats(false);
    }

    // always restart to prevent refreshing while moving around
    timer.restart();
  }

  /**
   * Indicates whether the contents of this editor have been modified.
   */
  public boolean isModified() {
    return modified;
  }

  /**
   * If the active object in the selection event is a unit it is registered with this editor.
   */
  public void selectionChanged(SelectionEvent e) {
    if (OrderEditor.log.isDebugEnabled()) {
      OrderEditor.log.debug("OrderEditor.selectionChanged: " + e.getActiveObject());
      OrderEditor.log.debug("OrderEditor.selectionChanged: " + e.getActiveObject().getClass());
    }

    Object activeObject = e.getActiveObject();

    if (activeObject instanceof Unit) {
      setUnit((Unit) activeObject);
    }
  }

  /**
   * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
   */
  public void keyPressed(KeyEvent e) {
    if ((unit != null)
        && ((e.getKeyCode() == KeyEvent.VK_ENTER) || (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) || (e
            .getKeyCode() == KeyEvent.VK_DELETE))) {
      setOrdersAndFireEvent();
    }
  }

  /**
   * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
   */
  public void keyReleased(KeyEvent e) {
    // ignored
  }

  /**
   * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
   */
  public void keyTyped(KeyEvent e) {
    // ignored
  }

  /**
   * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
   */
  public void focusGained(FocusEvent e) {
    // ignored
  }

  /**
   * Update orders.
   *
   * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
   */
  public void focusLost(FocusEvent e) {
    setOrdersAndFireEvent();
  }

  private void setOrdersAndFireEvent() {
    if (OrderEditor.log.isDebugEnabled()) {
      OrderEditor.log.debug("OrderEditor.setOrdersAndFireEvent(" + isModified() + "," + unit + ")");
    }

    if (isModified() && (unit != null)) {
      // this is done on purpose: in init of UnitOrdersEvent the list of related units is built
      // so we need to create it before changing orders of the unit
      UnitOrdersEvent e = new UnitOrdersEvent(this, unit);
      unit.setOrders(getOrders());
      // we also need to notify that the unit orders are now unmodified
      setModified(false);
      dispatcher.fire(e);
    }
  }

  public void fireOrdersChangedEvent() {
    if (unit != null) {
      UnitOrdersEvent e = new UnitOrdersEvent(this, unit);
      unit.setOrders(getOrders());
      // we also need to notify that the unit orders are now unmodified
      setModified(false);
      dispatcher.fire(e);
    }
  }

  /**
   * Returns the unit currently registered with this editor.
   */
  public Unit getUnit() {
    return unit;
  }

  /**
   * Sets the unit for this editor component. If the orders have been modified and there is a previous
   * unit registered with this component, its orders are updated.
   */
  public void setUnit(Unit u) {
    setOrdersAndFireEvent();

    ignoreModifications = true;
    unit = u;

    if (unit != null) {
      setOrders(unit.getOrders2());
    } else {
      setText("");
      // undoMgr.discardAllEdits();
    }

    ignoreModifications = false;
  }

  /**
   * Returns a list of the orders this text pane is currently containing. A order extending over more
   * than one line is stripped of the trailing backslashes, concatenated and returned as one order.
   */
  public List<String> getOrders() {
    if (!isModified())
      return orders;

    orders = new LinkedList<String>();

    try {
      String text = getText();
      String line = null;
      LineNumberReader stream = new LineNumberReader(new MergeLineReader(new StringReader(text)));

      // note: stream.ready() is not false at end of string
      while (stream.ready()) {
        if ((line = stream.readLine()) != null) {
          orders.add(line);
        } else {
          break;
        }
      }

      stream.close();
    } catch (IOException ioe) {
      OrderEditor.log.warn(ioe);
    }

    return orders;
  }

  /**
   * Puts the list elements in <kbd>c</kbd> into this text pane, one at a time.
   */
  public void setOrders(Collection<Order> c) {
    orders = new LinkedList<String>();
    for (Order o : c) {
      orders.add(o.toString());
    }

    boolean oldMod = ignoreModifications;
    ignoreModifications = true;
    setText("");

    for (String string : orders) {
      addOrder(string);
    }

    setCaretPosition(0);
    setModified(false);
    ignoreModifications = oldMod;
    // undoMgr.discardAllEdits();
  }

  /**
   * Adds a order to this text pane.
   */
  public void addOrder(String cmd) {
    boolean oldMod = ignoreModifications;
    ignoreModifications = true;

    Document doc = getDocument();

    try {
      // if((doc.getLength() > 0) && !doc.getText(doc.getLength(), 1).equals("\n")) {
      if ((doc.getLength() > 0)) {
        doc.insertString(doc.getLength(), "\n", null);
      }

      int insertPos = doc.getLength();
      doc.insertString(insertPos, cmd, null);

      if (highlightSyntax) {
        formatTokens(insertPos);
      }
    } catch (BadLocationException e) {
      OrderEditor.log.warn("OrderEditor.addOrder(): " + e.toString());
    }

    ignoreModifications = oldMod;
  }

  /**
   * Allows to change the modified state of this text pane. Setting it to <kbd>true</kbd> currently does
   * not automatically result in an event being fired.
   */
  private void setModified(boolean isModified) {
    modified = isModified;
  }

  /**
   * Return whether syntax highlighting is enabled or disabled.
   */
  public boolean getUseSyntaxHighlighting() {
    return highlightSyntax;
  }

  /**
   * Enable or disable syntax highlighting.
   */
  public void setUseSyntaxHighlighting(boolean bool) {
    if (highlightSyntax != bool) {
      highlightSyntax = bool;

      if (bool) {
        errorColor = Colors.decode(settings.getProperty("OrderEditor.errorBgColor", "255,128,0"));
      } else {
        errorColor = getBackground();
      }
      formatTokens();
    }
  }

  /**
   * Set the color of the specified token style used for syntax highlighting.
   */
  public void setTokenColor(String styleName, Color color) {
    Style s = ((StyledDocument) getDocument()).getStyle(styleName);

    if (s != null) {
      StyleConstants.setForeground(s, color);
    }
  }

  /**
   * Refresh the editor's contents.
   */
  public void reloadOrders() {
    if (unit != null) {
      setOrders(unit.getOrders2());
    }
  }

  /**
   * Sets the background color for orders with errors.
   *
   * @param c
   */
  public void setErrorBackround(Color c) {
    errorColor = c;
    initStyles();
  }

  private UIDefaults getUiDefaults() {
    Object o = getClientProperty("Nimbus.Overrides");
    UIDefaults defaults;
    if (o instanceof UIDefaults) {
      defaults = (UIDefaults) o;
    } else {
      defaults = new UIDefaults();
    }
    return defaults;
  }

  @Override
  public void setBackground(Color bg) {
    UIDefaults defaults = getUiDefaults();
    defaults.put("TextPane[Enabled].backgroundPainter", bg);
    putClientProperty("Nimbus.Overrides", defaults);
    putClientProperty("Nimbus.Overrides.InheritDefaults", true);

    super.setBackground(bg);
  }

  /**
   * Adds styles to this component with one style for each token type.
   */
  private void initStyles() {
    Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
    Style regular = addStyle(OrderEditor.S_REGULAR, def);
    Style s = addStyle(OrderEditor.S_REGULAR_INV, regular);
    StyleConstants.setBackground(s, errorColor);

    s = addStyle(OrderEditor.S_KEYWORD, regular);
    StyleConstants.setForeground(s, Colors.decode(settings.getProperty("OrderEditor.styles."
        + OrderEditor.S_KEYWORD + ".color", OrderEditor.getDefaultColor(OrderEditor.S_KEYWORD))));
    s = addStyle(OrderEditor.S_KEYWORD_INV, s);
    StyleConstants.setBackground(s, errorColor);

    s = addStyle(OrderEditor.S_STRING, regular);
    StyleConstants.setForeground(s, Colors.decode(settings.getProperty("OrderEditor.styles."
        + OrderEditor.S_STRING + ".color", OrderEditor.getDefaultColor(OrderEditor.S_STRING))));
    s = addStyle(OrderEditor.S_STRING_INV, s);
    StyleConstants.setBackground(s, errorColor);

    s = addStyle(OrderEditor.S_NUMBER, regular);
    StyleConstants.setForeground(s, Colors.decode(settings.getProperty("OrderEditor.styles."
        + OrderEditor.S_NUMBER + ".color", OrderEditor.getDefaultColor(OrderEditor.S_NUMBER))));
    s = addStyle(OrderEditor.S_NUMBER_INV, s);
    StyleConstants.setBackground(s, errorColor);

    s = addStyle(OrderEditor.S_ID, regular);
    StyleConstants.setForeground(s, Colors.decode(settings.getProperty("OrderEditor.styles."
        + OrderEditor.S_ID + ".color", OrderEditor.getDefaultColor(OrderEditor.S_ID))));
    s = addStyle(OrderEditor.S_ID_INV, s);
    StyleConstants.setBackground(s, errorColor);

    s = addStyle(OrderEditor.S_COMMENT, regular);
    StyleConstants.setForeground(s, Colors.decode(settings.getProperty("OrderEditor.styles."
        + OrderEditor.S_COMMENT + ".color", OrderEditor.getDefaultColor(OrderEditor.S_COMMENT))));
    s = addStyle(OrderEditor.S_COMMENT_INV, s);
    StyleConstants.setBackground(s, errorColor);

    s = addStyle(OrderEditor.S_LONGORDER, regular);
    StyleConstants.setBackground(s, Color.yellow);
    s = addStyle(OrderEditor.S_LONGORDER_INV, s);
    StyleConstants.setBackground(s, errorColor);

  }

  /**
   * Return the default color as a string for the specified token style.
   */
  public static String getDefaultColor(String styleName) {
    String retVal = "0,0,0";

    if (styleName.equals(OrderEditor.S_KEYWORD)) {
      retVal = "0,0,255";
    } else if (styleName.equals(OrderEditor.S_STRING)) {
      retVal = "192,0,0";
    } else if (styleName.equals(OrderEditor.S_NUMBER)) {
      retVal = "0,0,0";
    } else if (styleName.equals(OrderEditor.S_ID)) {
      retVal = "168,103,0";
    } else if (styleName.equals(OrderEditor.S_COMMENT)) {
      retVal = "0,128,0";
    }

    return retVal;
  }

  /**
   * Returns a style corresponding to a token type
   */
  private Style getTokenStyle(OrderToken t, boolean valid) {
    StyledDocument doc = (StyledDocument) getDocument();
    Style retVal = doc.getStyle(OrderEditor.S_REGULAR);

    switch (t.ttype) {
    case OrderToken.TT_UNDEF:
      if (valid) {
        retVal = doc.getStyle(OrderEditor.S_REGULAR);
      } else {
        retVal = doc.getStyle(OrderEditor.S_REGULAR_INV);
      }

      break;

    case OrderToken.TT_PERSIST:
    case OrderToken.TT_EXCLAM:
    case OrderToken.TT_KEYWORD:
      if (valid) {
        retVal = doc.getStyle(OrderEditor.S_KEYWORD);
      } else {
        retVal = doc.getStyle(OrderEditor.S_KEYWORD_INV);
      }

      break;

    case OrderToken.TT_STRING:
    case OrderToken.TT_OPENING_QUOTE:
    case OrderToken.TT_CLOSING_QUOTE:
      if (valid) {
        retVal = doc.getStyle(OrderEditor.S_STRING);
      } else {
        retVal = doc.getStyle(OrderEditor.S_STRING_INV);
      }

      break;

    case OrderToken.TT_NUMBER:
      if (valid) {
        retVal = doc.getStyle(OrderEditor.S_NUMBER);
      } else {
        retVal = doc.getStyle(OrderEditor.S_NUMBER_INV);
      }

      break;

    case OrderToken.TT_ID:
      if (valid) {
        retVal = doc.getStyle(OrderEditor.S_ID);
      } else {
        retVal = doc.getStyle(OrderEditor.S_ID_INV);
      }

      break;

    case OrderToken.TT_COMMENT:
      if (valid) {
        retVal = doc.getStyle(OrderEditor.S_COMMENT);
      } else {
        retVal = doc.getStyle(OrderEditor.S_COMMENT_INV);
      }

      break;
    default:
      if (valid) {
        retVal = doc.getStyle(OrderEditor.S_REGULAR);
      } else {
        retVal = doc.getStyle(OrderEditor.S_REGULAR_INV);
      }
    }

    return retVal;
  }

  /**
   * Gives back the text in the text pane. Additionally \r is replaced by space. This is a so-called
   * "minor incompatibility" introduced by jdk 1.4.
   */
  @Override
  public String getText() {
    String text = super.getText();

    if ((text == null) || (text.indexOf('\r') == -1))
      return text;

    StringBuffer sb = new StringBuffer(text.length());
    char chars[] = text.toCharArray();

    for (char c : chars) {
      if (c != '\r') {
        sb.append(c);
      }
    }

    return sb.toString();
  }

  private static OrderToken atToken = new OrderToken("@");
  static {
    atToken.ttype = OrderToken.TT_PERSIST;
  }

  /**
   * Format the tokens in the line starting in the document at insertPos, with regard to the token
   * type colors.
   */
  private void formatTokens(int startPos) {
    StyledDocument doc = (StyledDocument) getDocument();
    setTabs(this, 4);

    String text = getText();

    if ((text == null) || text.equals(""))
      return;

    int pos[] = getLineBorders(text, startPos);
    if (pos[0] == -1)
      return;

    if (OrderEditor.log.isDebugEnabled()) {
      OrderEditor.log.debug("OrderEditor.formatTokens(" + startPos + "): (" + text + "," + pos[0]
          + "," + pos[1] + ")");
    }

    text = text.substring(pos[0], pos[1]);

    if (OrderEditor.log.isDebugEnabled()) {
      OrderEditor.log.debug("OrderEditor.formatTokens(" + startPos + "): (" + text + ")");
    }

    Order order = parser.parse(text, getUnit().getLocale());
    boolean valid = order.getProblem() == null; // parser.read(new StringReader(text));

    // doc.setCharacterAttributes(pos[0]+(pos[1]-pos[0])/4, pos[1]-(pos[1]-pos[0])/4,
    // doc.getStyle(S_REGULAR), true);

    OrderToken prevToken = null;
    for (OrderToken token : order.getTokens()) {
      if (OrderEditor.log.isDebugEnabled()) {
        OrderEditor.log.debug("OrderEditor.formatTokens: token " + token);
      }

      if (token.ttype == OrderToken.TT_EOC || token.getEnd() < 0) {
        // invalid tokens for our purpose
        break;
      }

      Style style = getTokenStyle(token, valid);

      if (style != null) {
        if (OrderEditor.log.isDebugEnabled()) {
          OrderEditor.log.debug("OrderEditor.formatTokens setting style from "
              + (pos[0] + token.getStart()) + " length " + (token.getEnd() - token.getStart()));
        }
        // format from the (whitespace after the) end of the previous token till the end of the
        // current token
        int start = (prevToken != null ? prevToken.getEnd() : token.getStart());
        doc.setCharacterAttributes(pos[0] + start, token.getEnd() - start, style, true);
      }
      prevToken = token;
    }

    if (prevToken != null) {
      doc.setCharacterAttributes(pos[0] + prevToken.getEnd(), pos[1] - pos[0] - prevToken.getEnd(),
          doc.getStyle(OrderEditor.S_REGULAR), true);
    }
    // @-token is not explicitly styled any more as it doesn't occur in getTokens()
  }

  /**
   * Format all tokens with regard to the token type colors.
   */
  public void formatTokens() {
    if (!highlightSyntax)
      return;

    String text = getText();

    if ((text == null) || text.equals(""))
      return;

    int pos = 0;

    while (pos != -1) {
      if (OrderEditor.log.isDebugEnabled()) {
        OrderEditor.log.debug("OrderEditor.formatTokens(): formatting pos " + pos);
      }

      formatTokens(pos);
      pos = text.indexOf("\n", pos);

      if (pos != -1) {
        pos++;
      }
    }

    if (OrderEditor.log.isDebugEnabled()) {
      OrderEditor.log.debug("OrderEditor.formatTokens(): formatting done");
    }
  }

  /**
   * Returns the start and end of the line denoted by offset.
   *
   * @param text the string to search.
   * @param offset the position from where to look for linebreaks.
   * @return start of line at array index 0, end of line at array index 1.
   */
  private int[] getLineBorders(String text, int offset) {
    int ret[] = new int[2];

    // 2002.02.20 pavkovic: if offset is greater than text size, we won't find any position
    // -> we can return -1 very fast
    if (offset > text.length()) {
      ret[0] = -1;

      return ret;
    }

    try {
      // pavkovic 2003.01.20: line ending may be \r\n!
      // prior to jdk 1.4 this only may have been \n OR \r
      // if current position IS \n or \r move to the right
      // if not, move to the left
      text = text.replace('\r', ' ');

      for (ret[0] = offset - 1; ret[0] > -1; ret[0]--) {
        char c = text.charAt(ret[0]);

        if ((c == '\n') || (c == '\r')) {
          break;
        }
      }

      ret[0]++;
      ret[1] = text.indexOf('\n', offset);

      // ret[1] = text.indexOf('\n', ret[0]);
      if (ret[1] == -1) {
        ret[1] = text.length();
      }
    } catch (StringIndexOutOfBoundsException e) {
      OrderEditor.log.error("OrderEditor.getLineBorders(), text: " + text + ", offset: " + offset
          + ", ret: (" + ret[0] + ", " + ret[1] + ")", e);
    }

    return ret;
  }

  /**
   * DOCUMENT-ME
   *
   * @see magellan.library.utils.guiwrapper.CacheableOrderEditor#setKeepVisible(boolean)
   */
  public void setKeepVisible(boolean b) {
    myCaret.setKeepVisible(b);
  }

  /**
   * DOCUMENT-ME
   */
  public boolean keepVisible() {
    return myCaret.keepVisible();
  }

  // pavkovic 2002.11.12
  // this is a dumb caret that does NOT track JScrollpane!
  // try to search for "JScrollPane tracks JTextComponent" (grr, took me days to find it!)
  // we use it in a MultiEditor-situation for the order editors
  // but this also means we need to handle setText
  private class OrderEditorCaret extends DefaultCaret {
    private boolean keepVis = true;

    @Override
    protected void adjustVisibility(Rectangle nloc) {
      if (OrderEditor.log.isDebugEnabled()) {
        OrderEditor.log.debug("OrderEditor(" + unit + "): adjustVisibility(" + keepVis + "," + nloc
            + ")");
      }

      if (keepVisible()) {
        super.adjustVisibility(nloc);
      }
    }

    /**
     * DOCUMENT-ME
     */
    public void setKeepVisible(boolean mode) {
      if (OrderEditor.log.isDebugEnabled()) {
        OrderEditor.log.debug("OrderEditor: setting caret.setKeepVisible(" + mode + ")");
      }

      keepVis = mode;
    }

    /**
     * DOCUMENT-ME
     */
    public boolean keepVisible() {
      return keepVis;
    }
  }

  private class DocumentUpdateRunnable implements Runnable {
    private DocumentEvent e;

    /**
     * Creates a new DocumentUpdateRunnable object.
     */
    public DocumentUpdateRunnable(DocumentEvent e) {
      this.e = e;
    }

    /**
     * DOCUMENT-ME
     */
    public void setEvent(DocumentEvent e) {
      this.e = e;
    }

    /**
     * Formats changed tokens.
     */
    public void run() {
      int offset = e.getOffset();
      String text = getText();
      int pos = getLineBorders(text, offset)[0];

      if ((pos < 0) || (pos > getDocument().getLength()))
        return;

      if (e.getType().equals(DocumentEvent.EventType.REMOVE)) {
        formatTokens(pos);
      } else if (e.getType().equals(DocumentEvent.EventType.INSERT)) {
        if ((text.length() >= (offset + e.getLength()))
            && (text.substring(offset, offset + e.getLength()).indexOf("\n") > -1)) {
          // multiple-line-insert happened
          // try {
          if (text.equals("") == false) {
            int p = pos;

            while (p > -1) {
              formatTokens(p);
              p = text.indexOf("\n", p);

              if (p != -1) {
                p++;
              }
            }
          }

          // } catch (Exception e) {
          // log.error(e);
          // }
        } else {
          // single-line-insert happened
          formatTokens(pos);
        }
      }
    }
  }

  private static class SignificantUndos implements UndoableEditListener, DocumentListener {
    boolean bSignificant = true;
    boolean bMoreThanOne = false;
    javax.swing.undo.CompoundEdit compoundEdit = new javax.swing.undo.CompoundEdit();

    /**
     * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
     */
    public void changedUpdate(DocumentEvent e) {
      documentEvent(e);
    }

    /**
     * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
     */
    public void insertUpdate(DocumentEvent e) {
      documentEvent(e);
    }

    /**
     * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
     */
    public void removeUpdate(DocumentEvent e) {
      documentEvent(e);
    }

    /**
     * All events except change events are significant.
     */
    private void documentEvent(DocumentEvent e) {
      bSignificant = (!DocumentEvent.EventType.CHANGE.equals(e.getType()));
    }

    /**
     * @see javax.swing.event.UndoableEditListener#undoableEditHappened(javax.swing.event.UndoableEditEvent)
     */
    public void undoableEditHappened(UndoableEditEvent e) {
      if (bSignificant) {
        if (bMoreThanOne) {
          compoundEdit.addEdit(e.getEdit());
          compoundEdit.end();
          // undoMgr.undoableEditHappened(new UndoableEditEvent(e.getSource(), compoundEdit));
          compoundEdit = new javax.swing.undo.CompoundEdit();
          bMoreThanOne = false;
        } else {
          // undoMgr.undoableEditHappened(e);
        }
      } else {
        compoundEdit.addEdit(e.getEdit());
        bMoreThanOne = true;
      }
    }
  }

  /**
   * Returns the unit for this editor.
   *
   * @see magellan.client.swing.tree.Changeable#getArgument()
   */
  public Object getArgument() {
    return getUnit();
  }

  /**
   * Returns {@link magellan.client.swing.tree.Changeable#CONTEXT_MENU}.
   *
   * @see magellan.client.swing.tree.Changeable#getChangeModes()
   */
  public int getChangeModes() {
    return Changeable.CONTEXT_MENU;
  }

  /**
   * @see magellan.client.swing.tree.Changeable#getContextFactory()
   */
  public ContextFactory getContextFactory() {
    return new UnitContextFactory();
  }

}
