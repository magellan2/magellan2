/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.library.gamebinding;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import magellan.library.Building;
import magellan.library.EntityID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Order;
import magellan.library.Rules;
import magellan.library.Ship;
import magellan.library.StringID;
import magellan.library.Unit;
import magellan.library.UnitID;
import magellan.library.completion.Completion;
import magellan.library.completion.OrderParser;
import magellan.library.rules.ItemType;
import magellan.library.rules.OrderType;
import magellan.library.utils.Direction;
import magellan.library.utils.IDBaseConverter;
import magellan.library.utils.Locales;
import magellan.library.utils.OrderToken;
import magellan.library.utils.OrderTokenizer;
import magellan.library.utils.RadixTree;
import magellan.library.utils.RadixTreeImpl;
import magellan.library.utils.Resources;
import magellan.library.utils.Umlaut;
import magellan.library.utils.logging.Logger;

/**
 * A class for reading Eressea orders and checking their syntactical correctness. A
 * <tt>OrderParser</tt> object can register a <tt>OrderCompleter</tt> object. In such a case the
 * <tt>OrderParser</tt> will call the corresponding methods of the <tt>OrderCompleter</tt> if it
 * encounters an incomplete order.
 */
public abstract class AbstractOrderParser implements OrderParser {
  private static final Logger log = Logger.getInstance(AbstractOrderParser.class);

  // this is not entirely true with dynamic bases but it probably doesn't really hurt
  protected static final int MAX_UID = 1679615;

  /**
   * The set of allowed quotes in orders
   */
  protected char[] quotes = new char[] { '\'', '"' };

  private String errMsg;
  private TokenBucket tokenBucket;
  protected Iterator<OrderToken> tokensIterator;
  protected OrderToken lastToken;
  private int tokenIndex;

  private GameData data;

  private Map<Locale, RadixTree<OrderHandler>> commandTries;
  // private RadixTree<OrderHandler> commandTrie;
  private HashMap<StringID, OrderHandler> commandMap;

  private Locale locale;

  private OrderHandler emptyReader;

  private char defaultQuote = '"';

  private Map<Locale, RadixTree<Direction>> dirTranslations =
      new HashMap<Locale, RadixTree<Direction>>();

  private boolean prefixMatching = true;

  protected static enum Type {
    EMPTY, OPENING, CLOSING
  }

  /**
   * Reader for orders with unit argument.
   */
  public abstract class UnitOrderHandler extends OrderHandler {
    protected UnitOrderHandler(OrderParser orderParser) {
      super(orderParser);
    }

    protected UnitID target;

    @Override
    protected void init(OrderToken token, String text) {
      target = null;
      order = new UnitArgumentOrder(getTokens(), text, target);
    }

    @Override
    public UnitArgumentOrder getOrder() {
      UnitArgumentOrder uorder = (UnitArgumentOrder) super.getOrder();
      uorder.target = target;
      return uorder;
    }
  }

  /**
   * Reader for orders with unit container argument.
   */
  public abstract class UCOrderHandler extends OrderHandler {
    protected UCOrderHandler(OrderParser orderParser) {
      super(orderParser);
    }

    protected EntityID target;

    @Override
    protected void init(OrderToken token, String text) {
      target = null;
      order = new UCArgumentOrder(getTokens(), text, target, UCArgumentOrder.T_UNKNOWN);
    }

    @Override
    public UCArgumentOrder getOrder() {
      UCArgumentOrder uorder = (UCArgumentOrder) super.getOrder();
      uorder.container = target;
      return uorder;
    }
  }

  /**
   * Reader for orders with building argument.
   */
  public abstract class BuildingOrderHandler extends OrderHandler {
    protected BuildingOrderHandler(OrderParser orderParser) {
      super(orderParser);
    }

    protected EntityID target;

    @Override
    protected void init(OrderToken token, String text) {
      target = null;
      order = new UCArgumentOrder(getTokens(), text, target, UCArgumentOrder.T_BUILDING);
    }

    @Override
    public UCArgumentOrder getOrder() {
      UCArgumentOrder uorder = (UCArgumentOrder) super.getOrder();
      uorder.container = target;
      return uorder;
    }
  }

  /**
   * Reader for orders with building argument.
   */
  public abstract class ShipOrderHandler extends OrderHandler {
    protected ShipOrderHandler(AbstractOrderParser orderParser) {
      super(orderParser);
    }

    protected EntityID target;

    @Override
    protected void init(OrderToken token, String text) {
      target = null;
      order = new UCArgumentOrder(getTokens(), text, target, UCArgumentOrder.T_SHIP);
    }

    @Override
    public UCArgumentOrder getOrder() {
      UCArgumentOrder uorder = (UCArgumentOrder) super.getOrder();
      uorder.container = target;
      return uorder;
    }
  }

  /**
   * Creates a new <tt>AbstractOrderParser</tt> object.
   */
  public AbstractOrderParser(GameData data) {
    if (data == null)
      throw new NullPointerException();
    this.data = data;
    // rules = data.getGameSpecificRules();
    init();
    initCommands();
    emptyReader = new OrderHandler(this) {
      @Override
      protected void init(OrderToken token, String text) {
        order = new SimpleOrder(getTokens(), text);
      }

      @Override
      protected boolean readIt(OrderToken token) {
        boolean tokenValid;
        if (token.ttype == OrderToken.TT_COMMENT) {
          OrderToken t = getNextToken();
          tokenValid = checkFinal(t);
        } else {
          tokenValid = checkFinal(token) && order.getPrefix().length() == 0;
        }
        return tokenValid;
      }
    };
  }

  protected void init() {
    errMsg = null;
    tokenBucket = new TokenBucket();
    tokensIterator = null;
    lastToken = null;
    tokenIndex = 0;
  }

  /**
   * Returns true if prefixes of tokens are matched to the token, i.e., if abbreviations are possible.
   */
  public boolean isPrefixMatching() {
    return prefixMatching;
  }

  /**
   * Sets the value of prefixMatching.
   *
   * @param prefixMatching The value for prefixMatching.
   */
  public void setPrefixMatching(boolean prefixMatching) {
    this.prefixMatching = prefixMatching;
  }

  /**
   * Returns the value of data.
   *
   * @return Returns data.
   */
  public GameData getData() {
    return data;
  }

  /**
   * Sets the value of data.
   *
   * @param data The value for data.
   */
  protected void setData(GameData data) {
    this.data = data;
    translationsMap.clear();
  }

  protected Rules getRules() {
    return data.getRules();
  }

  /**
   * Returns the value of completer.
   *
   * @return Returns completer.
   */
  public abstract AbstractOrderCompleter getCompleter();

  /**
   * Return current locale.
   */
  public Locale getLocale() {
    return locale == null ? Locales.getOrderLocale() : locale;
  }

  /**
   * Returns the localized order in the current locale.
   *
   * @see Resources#getOrderTranslation(String, Locale)
   */
  protected String getOrderTranslation(StringID orderId) {
    Map<StringID, String> translations = translationsMap.get(getLocale());
    if (translations == null) {
      translations = new HashMap<StringID, String>();
      translationsMap.put(getLocale(), translations);
    }
    String token = translations.get(orderId);
    if (token == null) {
      token =
          getData().getGameSpecificStuff().getOrderChanger().getOrderO(getLocale(), orderId)
              .getText();
      translations.put(orderId, token);
    }
    return token;
  }

  /**
   * Returns the localized rule item (skill, race) in the current locale.
   *
   * @see Resources#getRuleItemTranslation(String, Locale)
   */
  protected String getRuleItemTranslation(String key) {
    return Resources.getRuleItemTranslation(key, getLocale());
  }

  /**
   * Tries to find the unit with the given ID in the data.
   *
   * @param id the target's ID (as text in the report's base)
   * @return The unit if it is in the data, otherwise <code>null</code>
   * @throws NumberFormatException if unit id is not parseable
   * @throws NullPointerException if unit id is null
   */
  public Unit getUnit(String id) {
    return getData().getUnit(UnitID.createUnitID(id, getData().base));
  }

  /**
   * Tries to find the building with the given ID in the data.
   *
   * @param id the target's ID (as text in the report's base)
   * @return The building if it is in the data, otherwise <code>null</code>
   * @throws NumberFormatException if unit id is not parseable
   * @throws NullPointerException if unit id is null
   */
  public Building getBuilding(String id) {
    return getData().getBuilding(EntityID.createEntityID(id, getData().base));
  }

  /**
   * Tries to find the ship with the given ID in the data.
   *
   * @param id the target's ID (as text in the report's base)
   * @return The ship if it is in the data, otherwise <code>null</code>
   * @throws NumberFormatException if unit id is not parseable
   * @throws NullPointerException if unit id is null
   */
  public Ship getShip(String id) {
    return getData().getShip(EntityID.createEntityID(id, getData().base));
  }

  /**
   * Tries to find the faction with the given ID in the data.
   *
   * @param id the target's ID (as text in the report's base)
   * @return The faction if it is in the data, otherwise <code>null</code>
   * @throws NumberFormatException if unit id is not parseable
   * @throws NullPointerException if unit id is null
   */
  public Faction getFaction(String id) {
    return getData().getFaction(EntityID.createEntityID(id, getData().base));
  }

  /**
   * Returns the value of commandMap.
   *
   * @return Returns commandMap.
   */
  protected HashMap<StringID, OrderHandler> getCommandMap() {
    return commandMap;
  }

  /**
   *
   */
  public void clearCommandMap() {
    commandMap = new HashMap<StringID, OrderHandler>();
    commandTries = new HashMap<Locale, RadixTree<OrderHandler>>();
    // RadixTreeImpl<OrderHandler> commandTrie =
    // new magellan.library.utils.RadixTreeImpl<OrderHandler>();
    // commandTries.put(Locales.getOrderLocale(), commandTrie);
  }

  protected abstract void initCommands();

  /**
   * Adds the specified handler for the specified command (and removes any previous handler for the
   * command). The caller must make sure that {@link #getOrderTranslation(StringID)}(prefix, locale)
   * returns the localized command for any order language.
   *
   * @param prefix A command ID
   * @param handler The handler for this command
   */
  protected void addCommand(StringID prefix, OrderHandler handler) {
    commandMap.put(prefix, handler);
    for (Locale loc : commandTries.keySet()) {
      RadixTree<OrderHandler> commandTrie = commandTries.get(loc);
      addCommand(prefix, commandTrie, loc, handler);
    }
  }

  protected void addCommand(StringID prefix, RadixTree<OrderHandler> commandTrie, Locale loc,
      OrderHandler handler) {
    List<String> orders = normalize(prefix, loc);
    for (String order : orders) {
      if (commandTrie.contains(order)) {
        commandTrie.delete(order);
      }
      commandTrie.insert(order, handler);
    }
  }

  private List<String> normalize(StringID prefix, Locale loc) {
    List<String> loNames = Collections.emptyList();
    if (getData() != null) {
      OrderType order = getData().getRules().getOrder(prefix);
      if (order != null) {
        List<String> names = order.getNames(loc);
        if (names != null) {
          loNames = new ArrayList<String>(names.size());
          for (String name : names) {
            loNames.add(normalize(name));
          }
        }
      } else {
        loNames = Collections.singletonList(normalize(prefix.toString()));
      }

    } else {
      loNames = Collections.singletonList(normalize(prefix.toString()));
    }
    return loNames;
  }

  /**
   * Removes the handler for the specified command.
   *
   * @param prefix
   */
  protected void removeCommand(StringID prefix) {
    commandMap.remove(prefix);
    for (Locale loc : commandTries.keySet()) {
      RadixTree<OrderHandler> commandTrie = commandTries.get(loc);
      List<String> orders = normalize(prefix, loc);
      for (String order : orders) {
        if (commandTrie.contains(order)) {
          commandTrie.delete(order);
        }
      }
    }
  }

  /**
   * Returns a collections of all command registered by {@link #addCommand(StringID, OrderHandler)}
   */
  protected Set<StringID> getCommands() {
    return commandMap.keySet();
  }

  /**
   * Returns a collection of all registered command handlers.
   */
  protected Collection<OrderHandler> getHandlers() {
    return commandMap.values();
  }

  /**
   * Returns the trie for the current locale ({@link #getLocale()}.
   */
  protected RadixTree<OrderHandler> getCommandTrie() {
    if (!commandTries.containsKey(getLocale())) {
      initCommands(getLocale());
    }
    return commandTries.get(getLocale());
  }

  /**
   * Ensures that the commands for the given locale are found by {@link #getHandlers()}.
   *
   * @param loc
   */
  protected void initCommands(Locale loc) {
    RadixTreeImpl<OrderHandler> commandTrie = new RadixTreeImpl<OrderHandler>();
    commandTries.put(loc, commandTrie);

    for (StringID command : commandMap.keySet()) {
      addCommand(command, commandTrie, loc, commandMap.get(command));
    }
  }

  /**
   * Returns the tokens read by the parser.
   *
   * @return all <tt>OrderToken</tt> object produced by the underlying <tt>OrderTokenizer</tt> by
   *         reading a order.
   */
  public List<OrderToken> getTokens() {
    return tokenBucket;
  }

  /**
   * Returns next token from the <code>tokenBucket</code>.
   */
  protected OrderToken getNextToken() {
    lastToken = tokensIterator.next();
    tokenIndex++;
    return lastToken;
  }

  /**
   * Returns the last token retrieved by {@link #getNextToken()} or <code>null</code> if there was
   * none.
   */
  protected OrderToken getLastToken() {
    return lastToken;
  }

  /**
   * Returns <code>true</code> if there is a next token
   */
  protected boolean hasNextToken() {
    return tokensIterator != null && tokensIterator.hasNext();
  }

  /**
   * @return the index of the last token retrieved by getLastToken() or -1 if no token was retrieved
   *         yet.
   */
  protected int getTokenIndex() {
    return tokenIndex;
  }

  /**
   * Returns the error messages produced by the last invocation of the <tt>read(Reader in)</tt>
   * method.
   *
   * @return an error message if the last <tt>read</tt> returned <tt>false</tt>, <tt>null</tt> else.
   */
  public String getErrorMessage() {
    return errMsg;
  }

  /**
   * Sets the value error message
   */
  protected void setErrMsg(String errMsg) {
    this.errMsg = errMsg;
  }

  /**
   * @see magellan.library.completion.OrderParser#parse(String, Locale)
   */
  public synchronized Order parse(String text, @SuppressWarnings("hiding") Locale locale) {
    setErrMsg(null);
    tokenBucket.read(new StringReader(text));
    tokenBucket.mergeTempTokens(getData().base);
    tokensIterator = tokenBucket.iterator();
    tokenIndex = 0;
    lastToken = null;
    this.locale = locale;

    OrderToken token = getNextToken();

    Order result = readOrder(token, text);

    this.locale = null;
    return result;
  }

  /**
   * Parses one line of text from the specified stream by tokenizing it and checking the syntax.
   *
   * @param in the stream to read the order from.
   * @return <tt>true</tt> if the syntax of the order read is valid, <tt>false</tt> else.
   * @deprecated Use {@link #parse(String, Locale)}
   */
  @Deprecated
  public boolean read(Reader in) {
    StringBuffer buffer = new StringBuffer();
    int ch;

    try {
      while ((ch = in.read()) != -1) {
        buffer.append((char) ch);
      }
    } catch (IOException e) {
      log.error("error reading order", e);
    }
    return parse(buffer.toString(), Locales.getOrderLocale()).isValid();

  }

  /**
   * Matches the token with the keys in {@link #getHandlers(OrderToken)}. If there is exactly one
   * handler, it is applied to the <code>t</code> and the result returned. Otherwise this method
   * returns <code>true</code> if <code>t</code> is the final token. Afterwards the completer is
   * applied.
   *
   * @param firstToken
   */
  protected Order readOrder(OrderToken firstToken, String text) {
    OrderToken t = firstToken;
    if (t.ttype == OrderToken.TT_EXCLAM) {
      t = getNextToken();
      t.setStart(firstToken.getStart());
    }
    if (t.ttype == OrderToken.TT_PERSIST) {
      t = getNextToken();
      t.setStart(firstToken.getStart());
    }

    List<OrderHandler> readers = getHandlers(t);
    OrderHandler reader;
    if (readers.size() == 1) {
      reader = readers.get(0);
    } else {
      // more than one possible completion
      reader = emptyReader;
      // exception: if there is an exact match, accept this reader
      if (readers.size() > 1)
        if (getCommandTrie().contains(t.getText().toLowerCase())) {
          reader = getCommandTrie().find(t.getText().toLowerCase());
        }
    }

    reader.read(t, text);
    if (getCompleter() != null && !t.followedBySpace() && t.ttype != OrderToken.TT_PERSIST
        && t.ttype != OrderToken.TT_EXCLAM) {
      getCompleter().cmplt();
    }

    SimpleOrder result = reader.getOrder();

    if (firstToken.ttype == OrderToken.TT_PERSIST) {
      if (t.getText().trim().startsWith(EresseaConstants.O_COMMENT) && reader == emptyReader) {
        result.setValid(false);
      }
    }

    return result;
  }

  /**
   * Returns a set of handlers that match the specified token.
   *
   * @param t
   */
  protected List<OrderHandler> getHandlers(OrderToken t) {
    ArrayList<OrderHandler> handlers;
    if (!isPrefixMatching()) {
      OrderHandler reader = getCommandTrie().find(t.getText().toLowerCase());
      return reader != null ? Collections.singletonList(reader) : Collections
          .<OrderHandler> emptyList();
    } else {
      handlers = getCommandTrie().searchPrefix(normalize(t.getText()), Integer.MAX_VALUE);
      // remove duplicates
      Set<OrderHandler> handlerSet = new HashSet<OrderHandler>(handlers);
      return new ArrayList<OrderHandler>(handlerSet);
    }
  }

  // ************* general use

  /** The StringChecker class may serve as a utility template for checking string tokens */
  protected class StringChecker {
    protected char innerDefaultQuote = defaultQuote;
    private boolean forceQuotes;
    private boolean allowQuotes;
    private boolean allowEmpty;
    protected boolean valid;
    public String content;
    private boolean nextValid;
    private boolean followedBySpace;

    protected OrderToken openingToken;
    protected OrderToken innerToken;
    protected OrderToken closingToken;
    protected OrderToken nextToken;
    private boolean preferQuotes;

    /**
     * Creates a StringChecker with default behaviour.
     *
     * @param forceQuotes if this is <code>true</code>, the string must be enclosed in (single or
     *          double) quotes.
     * @param preferQuotes if this is <code>true</code>, quotes are inserted by order completers.
     * @param allowQuotes if this is <code>true</code>, the string may be enclosed in (single or double)
     *          quotes.
     * @param allowEmpty if this is <code>true</code>, the content may be empty.
     * @throws IllegalArgumentException if <code>forceQuotes</code> but not <code>allowQuote</code> or
     *           if <code>allowEmpty</code> but not <code>allowQuotes</code>.
     */
    public StringChecker(boolean forceQuotes, boolean preferQuotes, boolean allowQuotes,
        boolean allowEmpty) {
      this.forceQuotes = forceQuotes;
      this.allowQuotes = allowQuotes;
      this.preferQuotes = preferQuotes;
      this.allowEmpty = allowEmpty;
      if (forceQuotes && !allowQuotes)
        throw new IllegalArgumentException();
      if (allowEmpty && !allowQuotes)
        throw new IllegalArgumentException();
      if (preferQuotes && !allowQuotes)
        throw new IllegalArgumentException();
      if (forceQuotes && !allowQuotes)
        throw new IllegalArgumentException();
    }

    /**
     * Tries to parse a string beginning with <code>token</code>. It first splits the following string
     * tokens into {@link #openingToken}, {@link #innerToken}, {@link #closingToken}, and
     * {@link #nextToken} (token after the string). Then it checks them with {@link #checkInner()},
     * clears completions, calls {@link #checkNext()}. If there is a completer and the last token of the
     * string is followed by space, it calls {@link #complete()}. The completions are then garnished
     * with quotes as specified by the do... methods.
     *
     * @return <code>{@link #checkInner()} &amp;&amp; {@link #checkNext()}</code>
     * @see AbstractOrderParser#getString(OrderToken)
     * @throws IllegalArgumentException If <code>!isString(token)</code>
     */
    public boolean read(OrderToken token) {
      if (!isString(token))
        throw new IllegalArgumentException(token.toString());

      OrderToken[] tokens = getString(token);
      openingToken = tokens[0];
      innerToken = tokens[1];
      closingToken = tokens[2];
      nextToken = tokens[3];

      content = "";
      if (innerToken != null) {
        content = innerToken.getText();
      }

      followedBySpace = false;
      if (closingToken != null) {
        followedBySpace = closingToken.followedBySpace();
      } else {
        followedBySpace =
            openingToken == null && innerToken != null && innerToken.followedBySpace();
      }

      valid = checkInner();
      if (getCompleter() != null) {
        getCompleter().clear();
      }
      nextValid = checkNext();

      if (getCompleter() != null && isComplete()) {
        complete();

        getCompleter().fixQuotes(openingToken, innerToken, closingToken, preferQuotes, forceQuotes,
            valid, innerDefaultQuote);
      }
      return valid && isQuotesValid() && nextValid;
    }

    /**
     * If this returns <code>true</code>, the completer is called. In this implementation this is the
     * case if the last string token is not followed by space and the completer is not
     * <code>null</code>.
     */
    protected boolean isComplete() {
      return getCompleter() != null && !followedBySpace;
    }

    /**
     * Checks the quotes. According to constructor parameters. Subclasses usually don't need to
     * overwrite this method.
     *
     * @return <code>false</code> if there are no quotes but quotes are forced or if the closing quote
     *         doesn't match the opening quote.
     */
    protected boolean isQuotesValid() {
      if (forceQuotes && openingToken == null)
        return false;
      if ((openingToken == null && closingToken == null)
          || (openingToken != null && closingToken != null))
        return true;
      return false;
    }

    /**
     * Subclasses may override this method if they want to check the content, which is in
     * <code>innerToken</code>.
     *
     * @see AbstractOrderParser#getString(OrderToken)
     */
    protected boolean checkInner() {
      return allowEmpty || content.trim().length() > 0;
    }

    /**
     * Subclasses should call an appropriate method of <code>getCompleter()</code> here.
     */
    protected void complete() {
      if (valid || openingToken == null && content.length() == 0) {
        getCompleter().addCompletion(
            new Completion(content, "", Completion.DEFAULT_PRIORITY + 2, allowEmpty
                && content.length() == 0 && openingToken == null ? 1 : 0));
      }
    }

    /**
     * Returns {@link AbstractOrderParser#checkFinal(OrderToken)}. Subclasses should overwrite to check
     * the rest of the order.
     */
    protected boolean checkNext() {
      boolean retVal = true;
      if (forceQuotes) {
        retVal = openingToken != null && closingToken != null;
      }
      if (!allowQuotes) {
        retVal &= openingToken == null && closingToken == null;
      }
      return retVal && checkFinal(nextToken);
    }

  }

  protected class FinalKeywordReader extends OrderHandler {
    FinalKeywordReader(AbstractOrderParser orderParser) {
      super(orderParser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      return readFinalKeyword(token);
    }
  }

  /**
   * Tests if the next token is a quoted (possibly empty) string at the end of the order.
   *
   * @return The string (without quotes) if a valid description was found, otherwise <code>null</code>
   */
  protected String readDescription() {
    return readDescription(true);
  }

  /**
   * Change default quote character
   */
  public void setDefaultQuote(char c) {
    defaultQuote = c;
  }

  /**
   * Tests if the next token is a quoted string at the end of the order.
   *
   * @return The string (without quotes) if a valid description was found, otherwise <code>null</code>
   */
  protected String readDescription(boolean allowEmpty) {
    OrderToken t = getNextToken();
    return readDescription(t, allowEmpty);
  }

  /**
   * Tests if t is a quoted (possibly empty) string at the end of the order.
   *
   * @return The string (without quotes) if a valid description was found, otherwise <code>null</code>
   */
  protected String readDescription(OrderToken t) {
    return readDescription(t, true);
  }

  /**
   * If this a quoted string at the end of the order, it is returned. If <code>allowEmpty</code>, it
   * may also be an empty string.
   *
   * @return The string (without quotes) if a valid description was found, otherwise <code>null</code>
   */
  protected String readDescription(OrderToken t, boolean allowEmpty) {
    if (isString(t)) {
      StringChecker checker = new StringChecker(true, true, true, allowEmpty);
      if (checker.read(t))
        return checker.content;
      else
        return null;
    } else {
      unexpected(t);
      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltDescription();
      }
    }

    return null;
  }

  protected boolean readFinalKeyword(OrderToken token) {
    token.ttype = OrderToken.TT_KEYWORD;

    return checkNextFinal();
  }

  /**
   * Returns true if the <code>token</code> is a nonempty string.
   */
  protected boolean readFinalString(OrderToken token) {
    return new StringChecker(false, false, true, false).read(token);
  }

  protected boolean readFinalID(OrderToken token) {
    return readFinalID(token, false);
  }

  protected boolean readFinalID(OrderToken token, boolean tempAllowed) {
    token.ttype = OrderToken.TT_ID;

    OrderToken t = getNextToken();

    // TODO why test for TEMP?
    if (shallComplete(token, t) && isTemp(token.getText()) && tempAllowed) {
      getCompleter().addRegionUnits("", true);
    }

    return (tempAllowed || !token.getText().toLowerCase().startsWith(
        getOrderTranslation(EresseaConstants.OC_TEMP).toLowerCase()))
        && checkFinal(t);
  }

  protected boolean readFinalNumber(OrderToken token) {
    token.ttype = OrderToken.TT_NUMBER;

    return checkNextFinal();
  }

  /**
   * Checks whether the next token is the end of line or a comment, i.e. the indicating a valid end of
   * the order. Reports an unexpected token if that is not the case.
   */
  protected boolean checkNextFinal() {
    if (hasNextToken()) {
      OrderToken t = getNextToken();

      return checkFinal(t);
    } else {
      setErrMsg("Missing token");

      return false;
    }
  }

  /**
   * Checks whether the token t is the end of line or a comment, i.e. the indicating a valid end of
   * the order. Reports an unexpected token if that is not the case.
   */
  protected boolean checkFinal(OrderToken t) {
    boolean retVal = ((isEoC(t)) || (t.ttype == OrderToken.TT_COMMENT));

    if (retVal == false) {
      unexpected(t);
    }

    return retVal;
  }

  private final class ItemChecker extends StringChecker {
    protected ItemType itype;

    private ItemChecker(boolean forceQuotes, boolean preferQuotes, boolean allowQuotes,
        boolean allowEmpty) {
      super(forceQuotes, preferQuotes, allowQuotes, allowEmpty);
    }

    @Override
    protected boolean checkInner() {
      for (ItemType type : getRules().getItemTypes()) {
        if (equalsNormalized(type.getName(), content)) {
          itype = type;
          return true;
        }
      }
      return false;
    }

    @Override
    protected void complete() {
      // do not complete here
      // super.complete();
    }

  }

  /**
   * Returns <code>true</code> if token represents an item.
   */
  protected ItemType checkItem(OrderToken token) {
    ItemChecker checker = new ItemChecker(false, false, true, false);
    checker.read(token);
    return checker.itype;
  }

  protected void unexpected(OrderToken t) {
    setErrMsg("Unexpected token " + t.toString());
  }

  protected static final boolean isEoC(OrderToken t) {
    return t.ttype == OrderToken.TT_EOC;
  }

  protected static final boolean isFinal(OrderToken t) {
    return t.ttype == OrderToken.TT_EOC || (t.ttype == OrderToken.TT_COMMENT);
  }

  /**
   * Tests if <code>txt</code> represents an integer with the given radix between min and max
   * (inclusively).
   *
   * @param txt
   * @param radix
   * @param min
   * @param max
   */
  protected boolean isNumeric(String txt, int radix, int min, int max) {
    boolean retVal = false;

    try {
      int i = Integer.parseInt(txt, radix);
      retVal = ((i >= min) && (i <= max));
    } catch (NumberFormatException e) {
      return false;
    }

    return retVal;
  }

  /**
   * Tests if txt represents a non-negative decimal integer number.
   */
  protected boolean isNumeric(String txt) {
    return isNumeric(txt, 10, 0, Integer.MAX_VALUE);
  }

  protected boolean isID(String txt) {
    return isID(txt, true);
  }

  /**
   * Tests if <code>txt</code> represents a valid ID (or TEMP ID) given the <code>data.base</code> and
   * {@link #MAX_UID}.
   *
   * @param txt
   */
  protected boolean isID(String txt, boolean allowTemp) {
    boolean retVal = isNumeric(txt, getData().base, 0, AbstractOrderParser.MAX_UID);

    if (!retVal && allowTemp) {
      retVal = isTempID(txt);
    }

    return retVal;
  }

  /**
   * Tests if <code>txt</code> represents a valid TEMP id.
   *
   * @param txt
   */
  protected boolean isTempID(String txt) {
    boolean retVal = false;
    int blankPos = txt.indexOf(" ");

    if (blankPos == -1) {
      blankPos = txt.indexOf("\t");
    }

    if (blankPos > -1) {
      String temp = txt.substring(0, blankPos);
      String nr = txt.substring(blankPos + 1);
      retVal = (isTemp(temp)) && isNumeric(nr, getData().base, 0, AbstractOrderParser.MAX_UID);
    }

    return retVal;
  }

  protected boolean isTemp(String temp) {
    return temp.equalsIgnoreCase(getOrderTranslation(getTemp()));
  }

  private Map<Locale, Map<StringID, String>> translationsMap =
      new HashMap<Locale, Map<StringID, String>>(3);

  protected abstract StringID getTemp();

  /**
   * tests if <code>txt</code> is a region id (coordinates).
   */
  protected boolean isRID(String txt) {
    boolean retVal = false;
    int firstCommaPos = txt.indexOf(",");
    int secondCommaPos = txt.lastIndexOf(",");

    if (firstCommaPos > -1) {
      if (secondCommaPos > firstCommaPos) {
        try {
          Integer.parseInt(txt.substring(0, firstCommaPos));
          Integer.parseInt(txt.substring(firstCommaPos + 1, secondCommaPos));
          Integer.parseInt(txt.substring(secondCommaPos + 1, txt.length()));
          retVal = true;
        } catch (NumberFormatException e) {
          AbstractOrderParser.log.warn("isRID()", e);
        }
      } else {
        try {
          Integer.parseInt(txt.substring(0, firstCommaPos));
          Integer.parseInt(txt.substring(firstCommaPos + 1, txt.length()));
          retVal = true;
        } catch (NumberFormatException e) {
          AbstractOrderParser.log.debug("isRID()", e);
        }
      }
    }

    return retVal;
  }

  /**
   * Tests if <code>txt</code> is surrounded by double quotes.
   *
   * @deprecated nobody needs us
   */
  @Deprecated
  protected boolean isQuoted(String txt) {
    return (txt.startsWith("\"") && txt.endsWith("\"") && txt.length() >= 2);
  }

  /**
   * Tests if <code>txt</code> is surrounded by single quotes.
   *
   * @deprecated nobody needs us
   */
  @Deprecated
  protected boolean isSingleQuoted(String txt) {
    return (txt.startsWith("\'") && txt.endsWith("\'") && txt.length() >= 2);
  }

  protected boolean isAnyString(String txt) {
    return Pattern.matches("[^\\p{Cntrl}]*", txt);
  }

  /**
   * Returns <code>true</code> if <code>txt</code> is a nonempty string composed solely by the
   * characters [A-Za-zƒ÷‹‰ˆ¸ﬂ~,._:0-9] that does not start with a number. TODO allow unicode
   * characters
   */
  protected boolean isSimpleString(String txt) {
    return Pattern.matches("[A-Za-zƒ÷‹‰ˆ¸ﬂ~,._:-][A-Za-zƒ÷‹‰ˆ¸ﬂ~,._:0-9-]*", txt);
  }

  /**
   * Tests if <code>txt</code> is a nonempty string which is either surrounded by quotes or by double
   * quotes, or is composed solely by the characters [A-Za-zƒ÷‹‰ˆ¸ﬂ~,._:].
   *
   * @param txt
   * @deprecated you should prefer {@link #isString(OrderToken)} and {@link StringChecker}
   */
  @Deprecated
  protected boolean isString(String txt) {
    boolean retVal = isQuoted(txt);

    if (retVal == false) {
      retVal = isSingleQuoted(txt);
    }
    // we only allow numbers within text
    // otherwise 1234 would also match to isString
    boolean isNumeric = isNumeric(txt);
    if ((retVal == false) && (txt.length() > 0)) {
      retVal = true;
      for (int i = 0; i < txt.length(); i++) {
        char c = txt.charAt(i);
        // we allow numbers if txt is not numeric
        if (!(((c >= 'A') && (c <= 'Z')) || ((c >= 'a') && (c <= 'z')) || (c == 'ƒ') || (c == '÷')
            || (c == '‹') || (c == '‰') || (c == 'ˆ') || (c == '¸') || (c == '~') || (c == 'ﬂ')
            || (c == ',') || (c == '.') || (c == '_') || (c == ':') || ((!isNumeric) && (c >= '0')
                && (c <= '9')))) {
          retVal = false;
          break;
        }
      }
    }

    if (retVal != Pattern.matches(
        "(\".*\")|(\'.*\')|([A-Za-zƒ÷‹‰ˆ¸ﬂ~,._:][A-Za-zƒ÷‹‰ˆ¸ﬂ~,._:0-9]*)", txt))
      throw new AssertionError("isString \"" + txt + "\"");

    return retVal;
  }

  /**
   * Same as {@link #isString(OrderToken, boolean, boolean) isString(OrderToken, false, true)}
   */
  protected boolean isString(OrderToken token) {
    return isString(token, false, true);
  }

  /**
   * Returns <code>true</code> if token starts a string. That is, it must be either a
   * {@link OrderToken#TT_OPENING_QUOTE} or a string token.
   *
   * @param token the current token
   * @param forceQuotes if this is <code>true</code>, token must be a TT_OPENING_QUOTE
   */
  protected boolean isString(OrderToken token, boolean forceQuotes, boolean identifier) {
    if (token.ttype == OrderToken.TT_OPENING_QUOTE)
      return true;
    if (identifier)
      return (!forceQuotes && (isSimpleString(token.getText())) || isEoC(token));
    else
      return (!forceQuotes && (isAnyString(token.getText())) || isEoC(token));
  }

  /**
   * Parses token and the following tokens to identify a string.
   *
   * @param token The current token which <em>must</em> be either a TT_OPENING_Quote or a simple
   *          string. You may use {@link #isString(OrderToken)} to test validity of your token.
   * @return <code>result[0]</code> contains the opening quote, <code>result[1]</code> the content,
   *         <code>result[2]</code> the closing quote, and <code>result[3]</code> the token that
   *         follows the string. All except <code>result[3]</code> may be <code>null</code>.
   */
  protected OrderToken[] getString(OrderToken token) {
    OrderToken[] result = new OrderToken[4];
    if (token.ttype == OrderToken.TT_OPENING_QUOTE) {
      result[0] = token;
      OrderToken t = getNextToken();
      if (isEoC(t)) {
        result[1] = null;
        result[2] = null;
        result[3] = t;
      } else {
        result[1] = t;
        t = getNextToken();
        if (t.ttype == OrderToken.TT_CLOSING_QUOTE) {
          result[2] = t;
          result[3] = getNextToken();
        } else {
          result[2] = null;
          result[3] = t;
        }
      }
    } else if (token.ttype == OrderToken.TT_STRING || isSimpleString(token.getText())) {
      token.ttype = OrderToken.TT_STRING;
      result[0] = null;
      result[1] = token;
      result[2] = null;
      result[3] = getNextToken();
    } else if (isEoC(token)) {
      result[0] = null;
      result[1] = null;
      result[2] = null;
      result[3] = token;
    } else
      throw new IllegalArgumentException(token.toString());
    return result;
  }

  /**
   * Tests if <code>txt</code> is (syntactically) a valid email address.
   *
   * @param txt
   */
  protected boolean isEmailAddress(String txt) {
    try {
      @SuppressWarnings("unused")
      InternetAddress foo = new InternetAddress(txt, true);
    } catch (AddressException e) {
      return false;
    }
    return true;

    // alternative implementation
    // return Pattern.matches("[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}", txt);
  }

  /**
   * Return true if a completions should be added to the completer for the current token. The standard
   * version returns true if there is a OrderCompleter and <code>token</code> is followed by a space
   * and <code>t</code> is not.
   *
   * @param token The last token read
   * @param t The current token.
   */
  protected boolean shallComplete(OrderToken token, OrderToken t) {
    return (getCompleter() != null && token.followedBySpace() && !t.followedBySpace());
  }

  /**
   * Returns name with '~' replaced by spaces and Umlauts replaced, too.
   */
  protected static String normalizeName(String name) {
    return Umlaut.convertUmlauts(name.replace('~', ' '));
  }

  protected String normalize(String token) {
    return Umlaut.normalize(token.trim()).toLowerCase();
  }

  protected boolean equalsNormalized(String rulesItem, String content) {
    return normalizeName(rulesItem).equalsIgnoreCase(normalizeName(content));
  }

  /**
   * Interpret token as direction.
   */
  public Direction toDirection(String text, Locale aLocale) {
    RadixTree<Direction> directions = dirTranslations.get(aLocale);
    if (directions == null) {
      directions = new RadixTreeImpl<Direction>();
      dirTranslations.put(aLocale, directions);
      for (Direction dir : getData().getGameSpecificStuff().getMapMetric().getDirections()) {
        for (String ld : getOrderTranslation(dir)) {
          directions.insert(normalize(ld.toLowerCase()), dir);
        }
      }
    }
    ArrayList<Direction> dirs = directions.searchPrefix(normalize(text), 2);
    Set<Direction> dirSet = new HashSet<Direction>(dirs);
    // anomaly: Eressea server accepts "NACH no" although it is a prefix of nordosten and nordwesten
    if (dirSet.size() == 1)
      return dirs.get(0);
    if (dirSet.size() > 0) {
      for (Direction dir : dirSet) {
        for (String ld : getOrderTranslation(dir))
          if (normalize(ld).equals(normalize(text)))
            return dir;
      }
    }
    return Direction.INVALID;
  }

  private List<String> getOrderTranslation(Direction dir) {
    return getData().getRules().getOrder(dir.getId()).getNames(getLocale());
  }

  /**
   * A class for collecting and preprocessing order tokens
   */
  class TokenBucket extends Vector<OrderToken> {
    private static final int MAX_TEMP_NR = 1679615; // = (36 ^ 4) - 1;

    /**
     * @see java.util.Vector#add(java.lang.Object)
     */
    @Override
    public synchronized boolean add(OrderToken o) {
      super.add(o);

      return true;
    }

    /**
     * Reads the tokens from <code>in</code> with an {@link OrderTokenizer} and adds them to
     * <code>this</code>.
     *
     * @param in
     * @return The number of tokens <i>after</i> reading
     */
    public int read(Reader in) {
      // TODO reduce object creation
      OrderTokenizer tokenizer = getOrderTokenizer(in);
      OrderToken token = null;
      clear();

      do {
        token = tokenizer.getNextToken();
        add(token);
      } while (token.ttype != OrderToken.TT_EOC);

      return size();
    }

    /**
     * Merges two tokens if the first one contains the string TEMP the second one contains an id.
     *
     * @return the number of remaining tokens.
     */
    public int mergeTempTokens(int base) {
      if (size() > 1) {
        for (int i = 0; i < (size() - 1); i++) {
          OrderToken tempToken = tokenAt(i);
          String tempText = tempToken.getText();

          if (isTemp(tempText)) {
            try {
              OrderToken nrToken = tokenAt(i + 1);
              String nrText = nrToken.getText();
              int nr = IDBaseConverter.parse(nrText, base);

              if ((nr >= 0) && (nr <= TokenBucket.MAX_TEMP_NR)) {
                OrderToken mergedToken =
                    new OrderToken(getOrderTranslation(getTemp()) + " " + nrText, tempToken
                        .getStart(), nrToken.getEnd(), OrderToken.TT_ID, nrToken.followedBySpace());
                remove(i);
                remove(i);
                add(i, mergedToken);
              }
            } catch (NumberFormatException e) {
              // wrong format -- don't merge
            }
          }
        }
      }

      return size();
    }

    /**
     * Returns the same value as {@link #elementAt(int)} but <code>null</code> if
     * <code>index >= size</code>.
     */
    public OrderToken tokenAt(int index) {
      OrderToken retVal = null;

      if (index < size()) {
        retVal = elementAt(index);
      }

      return retVal;
    }

  }

  public OrderTokenizer getOrderTokenizer(Reader in) {
    OrderTokenizer tokenizer = new OrderTokenizer(in);
    tokenizer.setQuotes(getQuotes());
    return tokenizer;
  }

  protected void setQuotes(char[] quotes) {
    this.quotes = Arrays.copyOf(quotes, quotes.length);
  }

  protected char[] getQuotes() {
    return quotes;
  }
}