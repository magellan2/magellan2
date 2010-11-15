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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import magellan.library.Alliance;
import magellan.library.Building;
import magellan.library.EntityID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Order;
import magellan.library.Rules;
import magellan.library.Ship;
import magellan.library.Spell;
import magellan.library.StringID;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.UnitID;
import magellan.library.completion.Completion;
import magellan.library.completion.OrderParser;
import magellan.library.rules.AllianceCategory;
import magellan.library.rules.BuildingType;
import magellan.library.rules.ItemCategory;
import magellan.library.rules.ItemType;
import magellan.library.rules.SkillType;
import magellan.library.utils.Direction;
import magellan.library.utils.IDBaseConverter;
import magellan.library.utils.Locales;
import magellan.library.utils.OrderToken;
import magellan.library.utils.OrderTokenizer;
import magellan.library.utils.RadixTreeImpl;
import magellan.library.utils.Resources;
import magellan.library.utils.Umlaut;
import magellan.library.utils.logging.Logger;
import ds.tree.RadixTree;

/**
 * A class for reading Eressea orders and checking their syntactical correctness. A
 * <tt>OrderParser</tt> object can register a <tt>OrderCompleter</tt> object. In such a case the
 * <tt>OrderParser</tt> will call the corresponding methods of the <tt>OrderCompleter</tt> if it
 * encounters an incomplete order.
 */
public class EresseaOrderParser implements OrderParser {
  private static final Logger log = Logger.getInstance(EresseaOrderParser.class);

  // this is not entirely true with dynamic bases but it probably doesn't really hurt
  protected static final int MAX_UID = 1679615;

  /**
   * The set of allowed quotes in orders
   */
  public static final char[] QUOTES = new char[] { '\'', '"' };

  private String errMsg;
  private TokenBucket tokenBucket;
  private Iterator<OrderToken> tokensIterator;
  private OrderToken lastToken;
  private int tokenIndex;
  private EresseaOrderCompleter completer;
  private GameData data;

  private Map<Locale, RadixTree<OrderHandler>> commandTries;
  // private RadixTree<OrderHandler> commandTrie;
  private HashMap<String, OrderHandler> commandMap;

  private Locale locale;

  private OrderHandler emptyReader;

  protected static enum Type {
    EMPTY, OPENING, CLOSING
  }

  /**
   * An OrderHandler tries to match the {@link #tokensIterator}
   * 
   * @author stm
   * @version 1.0, Jun 11, 2009
   */
  public abstract class OrderHandler {

    protected SimpleOrder order;
    protected boolean valid;

    /**
     * Reads an order.
     * 
     * @param token First token of the order ("GIB", "LERNE" or the like) that must match the
     *          concrete OrderHandler class.
     * @return true if the order is valid
     */
    public boolean read(OrderToken token, String text) {
      init(token, text);
      valid = readIt(token);
      getOrder().setValid(valid);
      postProcess();
      return getOrder().isValid();
    }

    protected void init(@SuppressWarnings("unused") OrderToken token, String text) {
      order = new SimpleOrder(getTokens(), text);
    }

    protected void postProcess() {
      // nothing to do, but can be overwritten
    }

    /**
     * Reads an order.
     * 
     * @param token First token of the order ("GIB", "LERNE" or the like) that must match the
     *          concrete OrderHandler class.
     * @return true if the order is valid
     */
    protected abstract boolean readIt(OrderToken token);

    public SimpleOrder getOrder() {
      return order;
    }

  }

  /**
   * Reader for orders with unit argument.
   */
  public abstract class UnitOrderHandler extends OrderHandler {
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
    protected EntityID target;

    @Override
    protected void init(OrderToken token, String text) {
      target = null;
      order = new UCArgumentOrder(getTokens(), text, target);
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
    protected EntityID target;

    @Override
    protected void init(OrderToken token, String text) {
      target = null;
      order = new UCArgumentOrder(getTokens(), text, target);
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
    protected EntityID target;

    @Override
    protected void init(OrderToken token, String text) {
      target = null;
      order = new UCArgumentOrder(getTokens(), text, target);
    }

    @Override
    public UCArgumentOrder getOrder() {
      UCArgumentOrder uorder = (UCArgumentOrder) super.getOrder();
      uorder.container = target;
      return uorder;
    }
  }

  /**
   * Creates a new <tt>EresseaOrderParser</tt> object.
   */
  public EresseaOrderParser(GameData data) {
    this(data, null);
  }

  /**
   * Creates a new <tt>EresseaOrderParser</tt> object and registers the specified
   * <tt>OrderCompleter</tt> object. This constructor should be used only by the
   * <tt>OrderCompleter</tt> class itself.
   */
  public EresseaOrderParser(GameData data, EresseaOrderCompleter cc) {
    if (data == null)
      throw new NullPointerException();
    this.data = data;
    // rules = data.getGameSpecificRules();
    setCompleter(cc);
    init();
    initCommands();
    emptyReader = new OrderHandler() {
      @Override
      protected void init(OrderToken token, String text) {
        order = new SimpleOrder(getTokens(), text);
      }

      @Override
      protected boolean readIt(OrderToken token) {
        boolean valid;
        if (token.ttype == OrderToken.TT_COMMENT) {
          OrderToken t = getNextToken();
          valid = checkFinal(t);
        } else {
          valid = checkFinal(token) && !order.getText().startsWith("@");
        }
        return valid;
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
    // rules = data.getGameSpecificRules();
  }

  protected Rules getRules() {
    return data.rules;
  }

  /**
   * Returns the value of completer.
   * 
   * @return Returns completer.
   */
  public EresseaOrderCompleter getCompleter() {
    return completer;
  }

  /**
   * Sets the value of completer.
   * 
   * @param completer The value for completer.
   */
  protected void setCompleter(EresseaOrderCompleter completer) {
    this.completer = completer;
  }

  public Locale getLocale() {
    return locale == null ? Locales.getOrderLocale() : locale;
  }

  /**
   * Returns the localized order in the current locale.
   * 
   * @see Resources#getOrderTranslation(String, Locale)
   */
  protected String getOrderTranslation(String key) {
    return Resources.getOrderTranslation(key, getLocale());
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

  public int getNumber(String text) {
    return Integer.parseInt(text);
  }

  /**
   * Fills the trie with all known orders. Subclasses may override this in order to set a different
   * set of orders.
   */
  protected void initCommands() {
    commandMap = new HashMap<String, OrderHandler>();
    commandTries = new HashMap<Locale, RadixTree<OrderHandler>>();
    RadixTreeImpl<OrderHandler> commandTrie =
        new magellan.library.utils.RadixTreeImpl<OrderHandler>();
    commandTries.put(Locales.getOrderLocale(), commandTrie);

    // addCommand("@", new AtReader());
    addCommand(EresseaConstants.O_WORK, new ArbeiteReader());

    addCommand(EresseaConstants.O_ATTACK, new AttackReader());

    addCommand(EresseaConstants.O_BANNER, new BannerReader());
    addCommand(EresseaConstants.O_CLAIM, new BeansprucheReader());
    addCommand(EresseaConstants.O_PROMOTION, new BefoerderungReader());

    addCommand(EresseaConstants.O_STEAL, new BeklaueReader());
    addCommand(EresseaConstants.O_SIEGE, new BelagereReader());
    addCommand(EresseaConstants.O_NAME, new BenenneReader());
    addCommand(EresseaConstants.O_USE, new BenutzeReader());
    addCommand(EresseaConstants.O_DESCRIBE, new BeschreibeReader());

    addCommand(EresseaConstants.O_ENTER, new BetreteReader());

    addCommand(EresseaConstants.O_GUARD, new BewacheReader());
    addCommand(EresseaConstants.O_MESSAGE, new BotschaftReader());
    addCommand(EresseaConstants.O_DEFAULT, new DefaultReader());
    addCommand(EresseaConstants.O_EMAIL, new EmailReader());
    addCommand(EresseaConstants.O_END, new EndeReader());
    addCommand(EresseaConstants.O_RIDE, new FahreReader());
    addCommand(EresseaConstants.O_FOLLOW, new FolgeReader());
    addCommand(EresseaConstants.O_RESEARCH, new ForscheReader());
    addCommand(EresseaConstants.O_GIVE, new GibReader());
    addCommand(EresseaConstants.O_GROUP, new GruppeReader());
    addCommand(EresseaConstants.O_HELP, new HelfeReader());
    addCommand(EresseaConstants.O_COMBAT, new KaempfeReader());
    addCommand(EresseaConstants.O_COMBATSPELL, new KampfzauberReader());
    addCommand(EresseaConstants.O_BUY, new KaufeReader());
    addCommand(EresseaConstants.O_CONTACT, new KontaktiereReader());
    addCommand(EresseaConstants.O_TEACH, new LehreReader());
    addCommand(EresseaConstants.O_LEARN, new LerneReader());
    addCommand(EresseaConstants.O_SUPPLY, new GibReader());

    addCommand(EresseaConstants.O_LOCALE, new LocaleReader());
    addCommand(EresseaConstants.O_MAKE, new MacheReader());
    addCommand(EresseaConstants.O_MOVE, new NachReader());
    // normalerweise nicht erlaubt...
    addCommand(EresseaConstants.O_NEXT, new InvalidReader());
    addCommand(EresseaConstants.O_RESTART, new NeustartReader());

    addCommand(EresseaConstants.O_NUMBER, new NummerReader());

    addCommand(EresseaConstants.O_OPTION, new OptionReader());
    addCommand(EresseaConstants.O_FACTION, new ParteiReader());
    addCommand(EresseaConstants.O_PASSWORD, new PasswortReader());

    addCommand(EresseaConstants.O_PLANT, new PflanzeReader());
    addCommand(EresseaConstants.O_PIRACY, new PiraterieReader());
    addCommand(EresseaConstants.O_PREFIX, new PraefixReader());
    // normalerweise nicht erlaubt...
    addCommand(EresseaConstants.O_REGION, new InvalidReader());
    addCommand(EresseaConstants.O_RECRUIT, new RekrutiereReader());
    addCommand(EresseaConstants.O_RESERVE, new ReserviereReader());
    addCommand(EresseaConstants.O_ROUTE, new RouteReader());

    addCommand(EresseaConstants.O_SORT, new SortiereReader());

    addCommand(EresseaConstants.O_SPY, new SpioniereReader());
    addCommand(EresseaConstants.O_QUIT, new StirbReader());
    addCommand(EresseaConstants.O_HIDE, new TarneReader());
    addCommand(EresseaConstants.O_CARRY, new TransportiereReader());
    addCommand(EresseaConstants.O_TAX, new TreibeReader());
    addCommand(EresseaConstants.O_ENTERTAIN, new UnterhalteReader());
    addCommand(EresseaConstants.O_ORIGIN, new UrsprungReader());
    addCommand(EresseaConstants.O_FORGET, new VergesseReader());

    addCommand(EresseaConstants.O_SELL, new VerkaufeReader());
    addCommand(EresseaConstants.O_LEAVE, new VerlasseReader());
    addCommand(EresseaConstants.O_CAST, new ZaubereReader());
    addCommand(EresseaConstants.O_SHOW, new ZeigeReader());
    addCommand(EresseaConstants.O_DESTROY, new ZerstoereReader());
    addCommand(EresseaConstants.O_GROW, new ZuechteReader());
    addCommand(EresseaConstants.O_SABOTAGE, new SabotiereReader());
  }

  /**
   * Adds the specified handler for the specified command (and removes any previous handler for the
   * command). The caller must make sure that {@link getOrderTranslation(String)}(prefix, locale)
   * returns the localized command for any order language.
   * 
   * @param prefix A command ID
   * @param handler The handler for this command
   */
  protected void addCommand(String prefix, OrderHandler handler) {
    commandMap.put(prefix, handler);
    for (Locale loc : commandTries.keySet()) {
      RadixTree<OrderHandler> commandTrie = commandTries.get(loc);
      String order = Resources.getOrderTranslation(prefix, loc).toLowerCase();
      if (commandTrie.contains(order)) {
        commandTrie.delete(order);
      }
      commandTrie.insert(order, handler);
    }
  }

  /**
   * Removes the handler for the specified command.
   * 
   * @param prefix
   */
  protected void removeCommand(String prefix) {
    commandMap.remove(prefix);
    for (Locale loc : commandTries.keySet()) {
      RadixTree<OrderHandler> commandTrie = commandTries.get(loc);
      String order = Resources.getOrderTranslation(prefix, loc).toLowerCase();
      if (commandTrie.contains(order)) {
        commandTrie.delete(order);
      }
    }
  }

  /**
   * Returns a collections of all command registered by {@link #addCommand(String, OrderHandler)}.
   */
  protected Set<String> getCommands() {
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
    if (commandTries.containsKey(loc))
      return;
    RadixTree<OrderHandler> commandTrie = new RadixTreeImpl<EresseaOrderParser.OrderHandler>();
    commandTries.put(loc, commandTrie);

    for (String command : commandMap.keySet()) {
      String order = Resources.getOrderTranslation(command, loc).toLowerCase();
      if (commandTrie.contains(order)) {
        commandTrie.delete(order);
      }
      commandTrie.insert(order, commandMap.get(command));
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
      log.error("error reding order", e);
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
   * @return
   */
  protected Order readOrder(OrderToken firstToken, String text) {
    OrderToken t = firstToken;
    if (t.ttype == OrderToken.TT_PERSIST) {
      t = getNextToken();
      t.setStart(firstToken.getStart());
    }

    ArrayList<OrderHandler> readers = getHandlers(t);
    OrderHandler reader;
    if (readers.size() == 1) {
      reader = readers.iterator().next();
    } else {
      reader = emptyReader;
    }

    reader.read(t, text);
    if (getCompleter() != null && !t.followedBySpace() && t.ttype != OrderToken.TT_PERSIST) {
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
   * @return
   */
  protected ArrayList<OrderHandler> getHandlers(OrderToken t) {
    return getCommandTrie().searchPrefix(t.getText().toLowerCase(), Integer.MAX_VALUE);
  }

  // ************* WORK (ARBEITE)
  protected class ArbeiteReader extends OrderHandler {
    @Override
    protected boolean readIt(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      return checkNextFinal();
    }
  }

  // ************* ATTACK (ATTACKIERE)
  protected class AttackReader extends UnitOrderHandler {

    @Override
    protected void init(OrderToken token, String text) {
      target = null;
      order = new AttackOrder(getTokens(), text, target);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      OrderToken t = getNextToken();

      if (isID(t.getText()) == true) {
        retVal = readAttackUID(t);
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltAttack();
      }

      return retVal;
    }

    protected boolean readAttackUID(OrderToken token) {
      token.ttype = OrderToken.TT_ID;
      target = UnitID.createUnitID(token.getText(), getData().base);

      return checkNextFinal();
    }

  }

  // ************* BANNER
  protected class BannerReader extends OrderHandler {
    @Override
    protected boolean readIt(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;

      return readDescription() != null;
    }
  }

  // ************* BEF÷RDERUNG
  protected class BefoerderungReader extends OrderHandler {
    @Override
    protected boolean readIt(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;

      return checkNextFinal();
    }
  }

  // ************* BEKLAUE
  protected class BeklaueReader extends UnitOrderHandler {

    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      OrderToken t = getNextToken();

      if (isID(t.getText()) == true) {
        retVal = readBeklaueUID(t);
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltBeklaue();
      }

      return retVal;
    }

    protected boolean readBeklaueUID(OrderToken token) {
      token.ttype = OrderToken.TT_ID;
      target = UnitID.createUnitID(token.getText(), getData().base);

      return checkNextFinal();
    }
  }

  // ************* BELAGERE
  protected class BelagereReader extends BuildingOrderHandler {

    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      OrderToken t = getNextToken();

      if (isID(t.getText(), false) == true) {
        retVal = readBelagereBID(t);
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltBelagere();
      }

      return retVal;
    }

    protected boolean readBelagereBID(OrderToken token) {
      token.ttype = OrderToken.TT_ID;
      target = EntityID.createEntityID(token.getText(), getData().base);
      Building tBuilding = getData().getBuilding(target);

      return tBuilding != null
          && getData().rules.getCastleType(tBuilding.getType().getID()) != null && checkNextFinal();
    }
  }

  // ************* BENENNE
  protected class BenenneReader extends OrderHandler {

    private EntityID target;

    @Override
    protected void init(OrderToken token, String text) {
      target = null;
      order = new RenameOrder(getTokens(), text, RenameOrder.T_UNKNOWN, target, null);
    }

    @Override
    public RenameOrder getOrder() {
      RenameOrder uorder = (RenameOrder) super.getOrder();
      uorder.target = target;
      return uorder;
    }

    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();
      t.ttype = OrderToken.TT_KEYWORD;
      if (t.equalsToken(getOrderTranslation(EresseaConstants.O_CASTLE))) {
        getOrder().type = RenameOrder.T_BUILDING;
        getOrder().name = readDescription(false);
        retVal = getOrder().name != null;
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_BUILDING))) {
        getOrder().type = RenameOrder.T_BUILDING;
        getOrder().name = readDescription(false);
        retVal = getOrder().name != null;
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_UNIT))) {
        getOrder().type = RenameOrder.T_UNIT;
        getOrder().name = readDescription(false);
        retVal = getOrder().name != null;
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_FACTION))) {
        getOrder().type = RenameOrder.T_FACTION;
        getOrder().name = readDescription(false);
        retVal = getOrder().name != null;
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_REGION))) {
        getOrder().type = RenameOrder.T_REGION;
        getOrder().name = readDescription(false);
        retVal = getOrder().name != null;
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_SHIP))) {
        getOrder().type = RenameOrder.T_SHIP;
        getOrder().name = readDescription(false);
        retVal = getOrder().name != null;
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_FOREIGNBUILDING))) {
        retVal = readBenenneFremdes(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_FOREIGNFACTION))) {
        retVal = readBenenneFremdes(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_FOREIGNSHIP))) {
        retVal = readBenenneFremdes(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_FOREIGNUNIT))) {
        retVal = readBenenneFremdes(t);
      } else {
        BuildingType found = null;
        for (BuildingType type : getRules().getBuildingTypes()) {
          if (t.equalsToken(type.getName())) {
            if (found == null) {
              found = type;
            } else {
              // ambigous
              found = null;
              break;
            }
          }
        }

        if (found == null) {
          t.ttype = OrderToken.TT_UNDEF;
          unexpected(t);
        } else {
          getOrder().name = readDescription(false);
          retVal = getOrder().name != null;
        }
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltBenenne();
      }

      return retVal;
    }

    protected boolean readBenenneFremdes(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (token.equalsToken(getOrderTranslation(EresseaConstants.O_FOREIGNUNIT))
          && t.equalsToken(getOrderTranslation(EresseaConstants.O_UNIT))) {
        retVal = readBenenneFremdeEinheit(t);
      } else if (token.equalsToken(getOrderTranslation(EresseaConstants.O_FOREIGNBUILDING))
          && t.equalsToken(getOrderTranslation(EresseaConstants.O_CASTLE))) {
        retVal = readBenenneFremdesGebaeude(t);
      } else if (token.equalsToken(getOrderTranslation(EresseaConstants.O_FOREIGNBUILDING))
          && t.equalsToken(getOrderTranslation(EresseaConstants.O_BUILDING))) {
        retVal = readBenenneFremdesGebaeude(t);
      } else if (token.equalsToken(getOrderTranslation(EresseaConstants.O_FOREIGNFACTION))
          && t.equalsToken(getOrderTranslation(EresseaConstants.O_FACTION))) {
        retVal = readBenenneFremdePartei(t);
      } else if (token.equalsToken(getOrderTranslation(EresseaConstants.O_FOREIGNSHIP))
          && t.equalsToken(getOrderTranslation(EresseaConstants.O_SHIP))) {
        retVal = readBenenneFremdesSchiff(t);
      } else {
        BuildingType found = null;
        for (BuildingType type : getRules().getBuildingTypes()) {
          if (t.equalsToken(type.getName())) {
            found = type;
            retVal = readBenenneFremdesGebaeude(t);
            break;
          }
        }
        if (found == null) {
          unexpected(t);
        }
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltBenenneFremdes(token);
      }

      return retVal;
    }

    protected boolean readBenenneFremdeEinheit(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;
      getOrder().type = RenameOrder.T_UNIT;

      OrderToken t = getNextToken();

      if (isID(t.getText())) {
        t.ttype = OrderToken.TT_ID;
        target = UnitID.createUnitID(t.getText(), getData().base);
        // Unit tUnit = getData().getUnit(target);
        getOrder().name = readDescription(false);
        retVal = getOrder().name != null;
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltBenenneFremdeEinheit();
      }

      return retVal;
    }

    protected boolean readBenenneFremdesGebaeude(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;
      getOrder().type = RenameOrder.T_BUILDING;

      OrderToken t = getNextToken();

      if (isID(t.getText())) {
        t.ttype = OrderToken.TT_ID;
        target = EntityID.createEntityID(t.getText(), getData().base);
        // Building tBuilding = getData().getBuilding(target);
        getOrder().name = readDescription(false);
        retVal = getOrder().name != null;
      } else {
        unexpected(t);
      }
      if (shallComplete(token, t)) {
        getCompleter().cmpltBenenneFremdesGebaeude();
      }

      return retVal;
    }

    protected boolean readBenenneFremdePartei(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;
      getOrder().type = RenameOrder.T_FACTION;

      OrderToken t = getNextToken();

      if (isID(t.getText())) {
        t.ttype = OrderToken.TT_ID;
        target = EntityID.createEntityID(t.getText(), getData().base);
        // Faction tFaction = getData().getFaction(target);
        getOrder().name = readDescription(false);
        retVal = getOrder().name != null;
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltBenenneFremdePartei();
      }

      return retVal;
    }

    protected boolean readBenenneFremdesSchiff(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;
      getOrder().type = RenameOrder.T_SHIP;

      OrderToken t = getNextToken();

      if (isID(t.getText())) {
        t.ttype = OrderToken.TT_ID;
        target = EntityID.createEntityID(t.getText(), getData().base);
        // Ship tShip = getData().getShip(target);
        getOrder().name = readDescription(false);
        retVal = getOrder().name != null;
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltBenenneFremdesSchiff();
      }
      return retVal;
    }

  }

  // ************* BENUTZE
  protected class BenutzeReader extends OrderHandler {
    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isString(t)) {
        retVal = readFinalString(t);
      } else if (isNumeric(t.getText())) {
        retVal = readBenutzeAmount(t);
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltBenutze(0);
      }
      return retVal;
    }

    protected boolean readBenutzeAmount(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_NUMBER;

      try {
        // anzahl feststellen?
        final int minAmount = Integer.parseInt(token.getText());

        OrderToken t = getNextToken();

        if (isString(t)) {
          retVal = new StringChecker(false, false, true, false) {
            @Override
            protected void complete() {
              getCompleter().cmpltBenutze(minAmount);
            }
          }.read(t);
        } else {
          unexpected(t);
          if (shallComplete(token, t)) {
            getCompleter().cmpltBenutze(minAmount);
          }
        }
      } catch (NumberFormatException e) {
        // not parsable Number !?
      }

      return retVal;
    }
  }

  // ************* BESCHREIBE
  protected class BeschreibeReader extends BenenneReader {
    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();
      t.ttype = OrderToken.TT_KEYWORD;
      if (t.equalsToken(getOrderTranslation(EresseaConstants.O_CASTLE))) {
        retVal = readDescription() != null;
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_UNIT))) {
        retVal = readDescription() != null;
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_PRIVATE))) {
        retVal = readDescription() != null;
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_REGION))) {
        retVal = readDescription() != null;
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_SHIP))) {
        retVal = readDescription() != null;
      } else {
        BuildingType found = null;
        for (BuildingType type : getRules().getBuildingTypes()) {
          if (t.equalsToken(type.getName())) {
            found = type;
            retVal = readDescription() != null;
            break;
          }
        }
        if (found == null) {
          t.ttype = OrderToken.TT_UNDEF;
          unexpected(t);
        }
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltBeschreibe();
      }
      return retVal;
    }

  }

  // ************* BETRETE
  protected class BetreteReader extends UCOrderHandler {

    @Override
    protected void init(OrderToken token, String text) {
      order = new EnterOrder(getTokens(), text, UCArgumentOrder.T_UNKNOWN);
    }

    @Override
    public EnterOrder getOrder() {
      return (EnterOrder) super.getOrder();
    }

    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (t.equalsToken(getOrderTranslation(EresseaConstants.O_CASTLE))) {
        getOrder().type = UCArgumentOrder.T_BUILDING;
        retVal = readBetreteBurg(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_SHIP))) {
        getOrder().type = UCArgumentOrder.T_SHIP;
        retVal = readBetreteSchiff(t);
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltBetrete();
      }
      return retVal;
    }

    protected boolean readBetreteBurg(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isID(t.getText(), false) == true) {
        retVal = readBetreteBurgBID(t);
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltBetreteBurg();
      }
      return retVal;
    }

    protected boolean readBetreteBurgBID(OrderToken token) {
      token.ttype = OrderToken.TT_ID;
      target = EntityID.createEntityID(token.getText(), getData().base);
      // Building tBuilding = getData().getBuilding(target);

      getOrder().container = target;

      return checkNextFinal();
    }

    protected boolean readBetreteSchiff(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isID(t.getText(), false) == true) {
        retVal = readBetreteSchiffSID(t);
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltBetreteSchiff();
      }
      return retVal;
    }

    protected boolean readBetreteSchiffSID(OrderToken token) {
      token.ttype = OrderToken.TT_ID;
      target = EntityID.createEntityID(token.getText(), getData().base);
      Ship tBuilding = getData().getShip(target);

      getOrder().container = target;

      return tBuilding != null && checkNextFinal();
    }
  }

  // ************* BEWACHE
  protected class BewacheReader extends OrderHandler {

    @Override
    protected void init(OrderToken token, String text) {
      order = new GuardOrder(getTokens(), text);
    }

    @Override
    public GuardOrder getOrder() {
      GuardOrder corder = (GuardOrder) super.getOrder();
      return corder;
    }

    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (t.equalsToken(getOrderTranslation(EresseaConstants.O_NOT))) {
        retVal = readBewacheNicht(t);
      } else {
        getOrder().setNot(false);
        retVal = checkFinal(t);
      }

      return retVal;
    }

    protected boolean readBewacheNicht(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setNot(true);

      return checkNextFinal();
    }
  }

  // ************* BOTSCHAFT
  protected class BotschaftReader extends OrderHandler {
    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (t.equalsToken("AN")) {
        // FIXME whats this?
        retVal = readIt(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_UNIT))) {
        retVal = readBotschaftEinheit(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_FACTION))) {
        retVal = readBotschaftPartei(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_REGION))) {
        retVal = readBotschaftRegion(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_CASTLE))) {
        retVal = readBotschaftGebaeude(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_SHIP))) {
        retVal = readBotschaftSchiff(t);
      } else {
        BuildingType found = null;
        for (BuildingType type : getRules().getBuildingTypes()) {
          if (t.equalsToken(type.getName())) {
            found = type;
            retVal = readBotschaftGebaeude(t);
            break;
          }
        }
        if (found == null) {
          unexpected(t);
        }
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltBotschaft();
      }
      return retVal;
    }

    protected boolean readBotschaftEinheit(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isID(t.getText()) == true) {
        retVal = readBotschaftID(t);
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltBotschaftEinheit(false);
      }
      return retVal;
    }

    protected boolean readBotschaftID(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_ID;

      OrderToken t = getNextToken();

      retVal = readDescription(t, false) != null;

      if (shallComplete(token, t)
          && token.getText().equalsIgnoreCase(getOrderTranslation(EresseaConstants.O_TEMP))) {
        getCompleter().cmpltBotschaftEinheit(true);
      }

      return retVal;
    }

    protected boolean readBotschaftPartei(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isID(t.getText(), false) == true) {
        retVal = readBotschaftID(t);
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltBotschaftPartei();
      }
      return retVal;
    }

    protected boolean readBotschaftRegion(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      retVal = readDescription(false) != null;

      return retVal;
    }

    protected boolean readBotschaftGebaeude(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isID(t.getText(), false) == true) {
        retVal = readBotschaftID(t);
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltBotschaftGebaeude();
      }
      return retVal;
    }

    protected boolean readBotschaftSchiff(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isID(t.getText(), false) == true) {
        retVal = readBotschaftID(t);
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltBotschaftSchiff();
      }
      return retVal;
    }

  }

  // ************* DEFAULT
  protected class DefaultReader extends OrderHandler {
    @Override
    protected boolean readIt(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;

      if (!token.followedBySpace())
        return false;

      OrderToken t = getNextToken();

      if (isString(t))
        return new DefaultChecker().read(t);
      else if (!isEoC(t)) {
        unexpected(t);
      }
      return false;
    }

    protected class DefaultChecker extends StringChecker {

      private EresseaOrderParser innerParser;
      private ArrayList<Completion> oldList;

      public DefaultChecker() {
        super(false, true, true, false);
        defaultQuote = '\'';
      }

      @Override
      protected boolean checkInner() {
        // parse the string inside the quote(s); this has side-effects on the completer!
        innerParser = new EresseaOrderParser(getData(), getCompleter());
        boolean ok = innerParser.parse(content, getLocale()).isValid();
        if (getCompleter() != null) {
          oldList = new ArrayList<Completion>(getCompleter().getCompletions());
        }
        return ok && content.length() != 0 && super.checkInner();
      }

      @Override
      protected void complete() {
        if (nextToken.ttype != OrderToken.TT_EOC)
          return;
        if (getCompleter() != null && innerParser.getTokens().size() > 0) {
          // OrderToken lastToken = innerParser.getTokens().get(innerParser.getTokens().size() - 1);
          if (innerParser.getTokens().size() > 1) {
            lastToken = innerParser.getTokens().get(innerParser.getTokens().size() - 2);
            String lastW = "";
            if (!lastToken.followedBySpace() && lastToken.ttype != OrderToken.TT_PERSIST) {
              if (lastToken.ttype == OrderToken.TT_CLOSING_QUOTE) {
                lastW =
                    getLastToken(4).getText() + getLastToken(3).getText()
                        + getLastToken(2).getText();
              } else if (innerParser.getTokens().size() > 2
                  && getLastToken(3).ttype == OrderToken.TT_OPENING_QUOTE) {
                lastW = getLastToken(3).getText() + getLastToken(2).getText();
              } else {
                lastW = getLastToken(2).getText();
              }
            }
            getCompleter().clear();

            // add the beginning of the content to the completions, so the AutoCompletion sees the
            // right stub
            for (Completion c : oldList) {
              if (content.lastIndexOf(lastW) >= 0) {
                Completion c2 = new Completion(c.getName(), c.getValue(), "", 0, 0);
                String newName = content.substring(0, content.lastIndexOf(lastW)) + c.getName();
                String newValue = c2.replace(content, lastW);
                getCompleter().addCompletion(
                    new Completion(newName, newValue, c.getPostfix(), c.getPriority(), c
                        .getCursorOffset()));
              }
            }
            // this is a dirty fix for some special cases like DEFAULT 'BANNER ""|
            if (valid && lastToken.ttype == OrderToken.TT_CLOSING_QUOTE) {
              getCompleter().addCompletion(new Completion(content));
            }
          } else {
            for (Completion completion : oldList) {
              getCompleter().addCompletion(completion);
            }
          }
        }
      }

      private OrderToken getLastToken(int i) {
        return innerParser.getTokens().get(innerParser.getTokens().size() - i);
      }
    }
  }

  // ************* EMAIL
  protected class EmailReader extends OrderHandler {
    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isString(t)) {
        retVal = new StringChecker(true, true, true, false) {
          @Override
          protected boolean checkInner() {
            return super.checkInner() && isEmailAddress(content);
          }
        }.read(t);
      } else {
        unexpected(t);
      }

      return retVal;
    }

    protected boolean readEmailAddress(OrderToken token) {
      token.ttype = OrderToken.TT_STRING;

      return checkNextFinal();
    }
  }

  // ************* ENDE
  protected class EndeReader extends OrderHandler {
    @Override
    protected boolean readIt(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;

      return checkNextFinal();
    }
  }

  // ************* FAHRE
  protected class FahreReader extends UnitOrderHandler {

    @Override
    protected void init(OrderToken token, String text) {
      order = new RideOrder(getTokens(), text, null);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      OrderToken t = getNextToken();

      if (isID(t.getText()) == true) {
        retVal = readFahreUID(t);
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltFahre(false);
      }
      return retVal;
    }

    protected boolean readFahreUID(OrderToken token) {
      token.ttype = OrderToken.TT_ID;

      OrderToken t = getNextToken();
      target = UnitID.createUnitID(token.getText(), getData().base);

      if (shallComplete(token, t)
          && token.getText().equalsIgnoreCase(getOrderTranslation(EresseaConstants.O_TEMP))) {
        getCompleter().cmpltFahre(true);
      }

      return checkFinal(t);
    }
  }

  // ************* FOLGE
  protected class FolgeReader extends OrderHandler {

    private String text;

    @Override
    protected void init(OrderToken token, String text) {
      order = new SimpleOrder(getTokens(), text);
      this.text = text;
    }

    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (t.equalsToken(getOrderTranslation(EresseaConstants.O_UNIT)) == true) {
        retVal = readFolgeEinheit(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_SHIP)) == true) {
        retVal = readFolgeSchiff(t);
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltFolge();
      }
      return retVal;
    }

    protected boolean readFolgeEinheit(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isID(t.getText()) == true) {
        UnitID target = UnitID.createUnitID(t.getText(), getData().base);
        Unit tUnit = getData().getUnit(target);
        order = new FollowUnitOrder(getTokens(), text, target);
        retVal = tUnit != null && readFinalID(t, true);
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltFolgeEinheit(false);
      }
      return retVal;
    }

    protected boolean readFolgeSchiff(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      OrderToken t = getNextToken();

      if (isID(t.getText(), false) == true) {
        EntityID target = EntityID.createEntityID(t.getText(), getData().base);
        UnitContainer tContainer = getData().getShip(target);
        order = new UCArgumentOrder(getTokens(), text, target);
        getOrder().setLong(true);
        retVal = tContainer != null && readFinalID(t);
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltFolgeSchiff();
      }
      return retVal;
    }
  }

  // ************* BEANSPRUCHE (Fiete)
  protected class BeansprucheReader extends OrderHandler {
    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isNumeric(t.getText())) {
        retVal = readBeansprucheAmount(t);
      } else if (isString(t)) {
        retVal = new StringChecker(false, false, true, false) {
          @Override
          protected void complete() {
            getCompleter().cmpltBeanspruche();
          }
        }.read(t);
      } else {
        unexpected(t);
      }

      return retVal;
    }

    protected boolean readBeansprucheAmount(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_NUMBER;

      OrderToken t = getNextToken();

      if (isString(t)) {
        retVal = new StringChecker(false, false, true, false) {
          @Override
          protected void complete() {
            getCompleter().cmpltBeanspruche();
          }
        }.read(t);
      } else {
        unexpected(t);
      }

      return retVal;
    }
  }

  // ************* FORSCHE
  protected class ForscheReader extends OrderHandler {
    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);

      OrderToken t = getNextToken();

      if (t.equalsToken(getOrderTranslation(EresseaConstants.O_HERBS))) {
        retVal = readFinalKeyword(t);
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltForsche();
      }
      return retVal;
    }
  }

  // ************* GIB
  protected class GibReader extends UnitOrderHandler {

    @Override
    protected void init(OrderToken token, String text) {
      target = null;
      order = new GiveOrder(getTokens(), text, null, null);
    }

    @Override
    public GiveOrder getOrder() {
      GiveOrder uorder = (GiveOrder) super.getOrder();
      uorder.target = target;
      return uorder;
    }

    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isID(t.getText()) == true) {
        retVal = readGibUID(t);
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltGib();
      }

      return retVal;
    }

    protected boolean readGibUID(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_ID;
      Unit tUnit;
      if (isTempID(token.getText())) {
        target = UnitID.createUnitID(token.getText(), getData().base);
        tUnit = getData().getTempUnit(target);
      } else {
        target = UnitID.createUnitID(token.getText(), getData().base);
        tUnit = getData().getUnit(target);
      }
      // do not test for existence -- unit may be invisible

      OrderToken t = getNextToken();

      if (isNumeric(t.getText()) == true) {
        getOrder().type = EresseaConstants.O_GIVE;
        retVal = readGibUIDAmount(t, target, Integer.parseInt(t.getText()), true);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_EACH))) {
        getOrder().type = EresseaConstants.O_GIVE;
        retVal = readGibJe(t, target);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_ALL))) {
        getOrder().type = EresseaConstants.O_GIVE;
        retVal = readGibUIDAlles(t);
      } else {
        if (t.equalsToken(getOrderTranslation(EresseaConstants.O_UNIT))) {
          getOrder().type = EresseaConstants.O_UNIT;
        }
        if (t.equalsToken(getOrderTranslation(EresseaConstants.O_CONTROL))) {
          getOrder().type = EresseaConstants.O_CONTROL;
        }
        if (t.equalsToken(getOrderTranslation(EresseaConstants.O_HERBS))) {
          getOrder().type = EresseaConstants.O_HERBS;
        }
        if (getOrder().type != null) {
          // id 0 not allowed
          retVal = target.intValue() != 0 && readFinalKeyword(t);
        } else {
          unexpected(t);
        }
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltGibUID(
            token.getText().equalsIgnoreCase(getOrderTranslation(EresseaConstants.O_TEMP)));
      }
      return retVal;
    }

    protected boolean readGibJe(OrderToken token, UnitID uid) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;
      getOrder().each = true;

      OrderToken t = getNextToken();

      if (isNumeric(t.getText()) == true) {
        retVal = readGibUIDAmount(t, uid, Integer.parseInt(t.getText()), false); // GIB JE PERSONS
        // is illegal
      } else
      // // GIVE bla JE ALL ... does not make sense
      // if(t.equalsToken(getOrderTranslation(EresseaConstants.O_ALL))) {
      // retVal = readGibUIDAlles(t);
      // } else
      if (isString(t)) {
        retVal = readFinalString(t);
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltGibJe();
      }
      return retVal;
    }

    protected boolean readGibUIDAlles(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;
      getOrder().all = true;

      OrderToken t = getNextToken();

      if (isEoC(t)) {
        retVal = checkFinal(t);
      } else if (isString(t)) {
        getOrder().itemType = checkItem(t);
        if (getOrder().itemType != null) {
          retVal = true;
        } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_MEN))) {
          getOrder().type = EresseaConstants.O_MEN;
          retVal = true;
        } else {
          getOrder().setValid(false);
        }
        retVal = retVal && checkFinal(getLastToken());
      } else {
        retVal = checkFinal(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltGibUIDAlles();
      }
      return retVal;
    }

    /**
     * For multiple-line-completion like the creation of give-orders for the resources of an item in
     * OrderCompleter.cmpltGibUIDAmount it is necessary to save the unit's id and the amount to be
     * given. This is done by this method.
     * 
     * @param uid the unit's id
     * @param i the amount
     */
    protected boolean readGibUIDAmount(OrderToken token, UnitID uid, int i, boolean persons) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_NUMBER;
      getOrder().amount = Integer.parseInt(token.getText());

      OrderToken t = getNextToken();

      if (isString(t)) {
        getOrder().itemType = checkItem(t);
        if (getOrder().itemType != null) {
          retVal = true;
        } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_MEN))) {
          getOrder().type = EresseaConstants.O_MEN;
          retVal = true;
        } else {
          getOrder().setValid(false);
        }
        retVal = retVal && checkFinal(getLastToken());

      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltGibUIDAmount(uid, i, persons);
      }
      return retVal;
    }

  }

  // ************* GRUPPE
  protected class GruppeReader extends OrderHandler {
    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isEoC(t)) {
        // just "GRUPPE" without explicit group is valid
        retVal = checkFinal(t);
      } else if (isString(t)) {
        retVal = new StringChecker(false, true, true, false) {
          @Override
          protected void complete() {
            getCompleter().cmpltGruppe();
          }
        }.read(t);
      } else {
        retVal = checkFinal(t);
      }

      if (getCompleter() != null) {
        getCompleter().cmpltGruppe();
      }

      return retVal;
    }
  }

  // ************* HELFE
  protected class HelfeReader extends OrderHandler {
    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isID(t.getText(), false) == true) {
        retVal = readHelfeFID(t);
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltHelfe();
      }
      return retVal;
    }

    protected boolean readHelfeFID(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_ID;

      OrderToken t = getNextToken();

      for (Iterator<AllianceCategory> it = getRules().getAllianceCategoryIterator(); it.hasNext();) {
        AllianceCategory all = it.next();
        if (t.equalsToken(getOrderTranslation(Alliance.ORDER_KEY_PREFIX + all.getName()))) {
          retVal = readHelfeFIDModifier(t);
          break;
        }
      }
      if (!retVal) {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltHelfeFID();
      }
      return retVal;
    }

    protected boolean readHelfeFIDModifier(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (t.equalsToken(getOrderTranslation(EresseaConstants.O_NOT))) {
        retVal = readHelfeFIDModifierNicht(t, token.getText());
      } else {
        retVal = checkFinal(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltHelfeFIDModifier();
      }
      return retVal;
    }

    protected boolean readHelfeFIDModifierNicht(OrderToken token, String modifier) {
      token.ttype = OrderToken.TT_KEYWORD;

      return checkNextFinal();
    }
  }

  // ************* KAEMPFE
  protected class KaempfeReader extends OrderHandler {

    @Override
    protected void init(OrderToken token, String text) {
      order = new CombatOrder(getTokens(), text);
    }

    @Override
    public CombatOrder getOrder() {
      CombatOrder corder = (CombatOrder) super.getOrder();
      return corder;
    }

    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (t.equalsToken(getOrderTranslation(EresseaConstants.O_COMBAT_AGGRESSIVE))) {
        retVal = readFinalKeyword(t);
        getOrder().setStatus(EresseaConstants.CS_AGGRESSIVE);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_COMBAT_REAR))) {
        retVal = readFinalKeyword(t);
        getOrder().setStatus(EresseaConstants.CS_REAR);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_COMBAT_DEFENSIVE))) {
        retVal = readFinalKeyword(t);
        getOrder().setStatus(EresseaConstants.CS_DEFENSIVE);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_COMBAT_NOT))) {
        retVal = readFinalKeyword(t);
        getOrder().setStatus(EresseaConstants.CS_NOT);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_COMBAT_FLEE))) {
        retVal = readFinalKeyword(t);
        getOrder().setStatus(EresseaConstants.CS_FLEE);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_COMBAT_HELP))) {
        retVal = readKaempfeHelfe(t);
      } else {
        getOrder().setStatus(EresseaConstants.CS_FRONT);
        retVal = checkFinal(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltKaempfe();
      }
      return retVal;
    }

    protected boolean readKaempfeHelfe(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (t.equalsToken(getOrderTranslation(EresseaConstants.O_NOT))) {
        retVal = readFinalKeyword(t);
        getOrder().setStatus(EresseaConstants.CS_HELPNOT);
      } else {
        getOrder().setStatus(EresseaConstants.CS_HELPYES);
        retVal = checkFinal(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltKaempfeHelfe();
      }
      return retVal;
    }
  }

  // ************* KAMPFZAUBER
  protected class KampfzauberReader extends ZaubereReader {
    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (t.equalsToken(getOrderTranslation(EresseaConstants.O_LEVEL))) {
        retVal = readZaubereStufe(t, true);
        // } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_NOT))) {
        // retVal = readFinalKeyword(t);
      } else if (isString(t)) {
        retVal = new ZaubereSpruchChecker(false, true, false, true) {
          @Override
          protected void complete() {
            getCompleter().cmpltKampfzauber(openingToken == null, "", "");
          }
        }.read(t);
      } else {
        unexpected(t);
        if (shallComplete(token, t)) {
          getCompleter().cmpltKampfzauber(true, "\"", "\"");
        }
      }

      return retVal;
    }

    /**
     * @see magellan.library.gamebinding.EresseaOrderParser.ZaubereReader#readZaubereEnde(magellan.library.utils.OrderToken,
     *      magellan.library.Spell)
     */
    @Override
    protected boolean readZaubereEnde(OrderToken token, Spell s) {
      if (token.equalsToken(getOrderTranslation(EresseaConstants.O_NOT))) {
        token.ttype = OrderToken.TT_KEYWORD;
        return checkNextFinal();
      } else if (isEoC(token)) {
        if (getCompleter() != null) {
          getCompleter().cmpltKampfzauberSpell();
        }
        return true;
      } else {
        unexpected(token);
        return false;
      }
    }
  }

  // ************* KAUFE
  protected class KaufeReader extends OrderHandler {
    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      OrderToken t = getNextToken();

      if (isNumeric(t.getText()) == true) {
        retVal = readKaufeAmount(t);
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltKaufe();
      }
      return retVal;
    }

    protected boolean readKaufeAmount(OrderToken token) {
      boolean retVal = false;
      ItemType type = null;
      ItemCategory luxuryCategory =
          (getData() != null) ? getRules().getItemCategory(EresseaConstants.C_LUXURIES) : null;
      token.ttype = OrderToken.TT_NUMBER;

      OrderToken t = getNextToken();

      //
      if (isString(t) && (getRules() != null)
          && ((type = getRules().getItemType(t.getText())) != null) && (luxuryCategory != null)
          && luxuryCategory.equals(type.getCategory())) {
        retVal = readFinalString(t);
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltKaufeAmount();
      }
      return retVal;
    }
  }

  // ************* KONTAKTIERE
  protected class KontaktiereReader extends OrderHandler {
    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isID(t.getText()) == true) {
        retVal = readKontaktiereUID(t);
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltKontaktiere();
      }
      return retVal;
    }

    protected boolean readKontaktiereUID(OrderToken token) {
      token.ttype = OrderToken.TT_ID;

      return checkNextFinal();
    }
  }

  // ************* LEHRE
  protected class LehreReader extends OrderHandler {

    @Override
    protected void init(OrderToken token, String text) {
      order = new TeachOrder(getTokens(), text);
    }

    @Override
    public TeachOrder getOrder() {
      TeachOrder uorder = (TeachOrder) super.getOrder();
      return uorder;
    }

    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      OrderToken t = getNextToken();

      if (isID(t.getText()) == true) {
        retVal = readLehreUID(t);
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltLehre(false);
      }
      return retVal;
    }

    protected boolean readLehreUID(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_ID;
      UnitID unit = UnitID.createUnitID(token.getText(), getData().base);
      getOrder().addUnit(unit);

      OrderToken t = getNextToken();

      if (isID(t.getText()) == true) {
        retVal = readLehreUID(t);
      } else {
        retVal = checkFinal(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltLehre(
            token.getText().equalsIgnoreCase(getOrderTranslation(EresseaConstants.O_TEMP)));
      }
      return retVal;
    }
  }

  // ************* LERNE
  protected class LerneReader extends OrderHandler {

    @Override
    protected void init(OrderToken token, String text) {
      order = new LearnOrder(getTokens(), text);
    }

    @Override
    public LearnOrder getOrder() {
      LearnOrder uorder = (LearnOrder) super.getOrder();
      return uorder;
    }

    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      OrderToken t = getNextToken();

      if (isString(t) && token.followedBySpace()) {
        retVal = new StringChecker(false, false, true, false) {
          SkillType skill = null;

          @Override
          protected boolean checkInner() {
            if (innerToken == null)
              return false;
            if (getRules() == null)
              return openingToken.getText().length() > 0;
            // TODO localize
            skill = getRules().getSkillType(content.replace('~', ' '));
            getOrder().skillName = content;
            return skill != null;
          }

          @Override
          protected void complete() {
            getCompleter().cmpltLerne();
          }

          @Override
          protected boolean checkNext() {
            return skill != null && readLerneTalent(nextToken, skill);
          }

        }.read(t);
      } else {
        unexpected(t);
      }

      return retVal;
    }

    protected boolean readLerneTalent(OrderToken token, SkillType skill) {
      boolean retVal = false;

      OrderToken t = token;

      if (isNumeric(t.getText()) == true) {
        retVal = readFinalNumber(t);
      } else if (isString(t) && !isEoC(t)
          && skill.equals(getRules().getSkillType(EresseaConstants.S_MAGIE))) {
        retVal = readFinalString(t);
      } else {
        retVal = checkFinal(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltLerneTalent(skill);
      }
      return retVal;
    }
  }

  // ************* LOCALE
  protected class LocaleReader extends OrderHandler {
    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isString(t)) {
        retVal = readDescription(false) != null;
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltLocale();
      }
      return retVal;
    }
  }

  // ************* MACHE
  protected class MacheReader extends OrderHandler {
    @Override
    protected boolean readIt(OrderToken token) {
      return read(token, false);
    }

    public boolean read(OrderToken token, boolean hasAmount) {
      boolean retVal = false;
      if (hasAmount) {
        token.ttype = OrderToken.TT_NUMBER;
      } else {
        token.ttype = OrderToken.TT_KEYWORD;
      }

      getOrder().setLong(true);
      OrderToken t = getNextToken();

      if (!hasAmount && isNumeric(t.getText())) {
        retVal = read(t, true);
      } else if (!hasAmount && t.equalsToken(getOrderTranslation(EresseaConstants.O_TEMP))) {
        getOrder().setLong(false);
        retVal = readMacheTemp(t);
      } else if (!hasAmount && isTempID(t.getText()) == true) {
        retVal = readMacheTempID(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_CASTLE))) {
        retVal = readMacheBurg(t);
      } else if (isBuilding(t) != null || isCastle(t) != null) {
        retVal = readMacheBuilding(t);
      } else if ((getRules() != null) && (getRules().getShipType(t.getText()) != null)) {
        retVal = readMacheShip(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_SHIP))) {
        retVal = readMacheSchiff(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_ROAD))) {
        retVal = readMacheStrasse(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_SEED))) {
        retVal = readFinalKeyword(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_MALLORNSEED))) {
        retVal = readFinalKeyword(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_HERBS))) {
        retVal = readFinalKeyword(t);
      } else if (isEoC(t)) {
        // this is actually allowed, but may be a bit dangerous...
        retVal = false;
      } else {
        retVal = readMacheAnything(t);
      }

      if (shallComplete(token, t)) {
        if (hasAmount) {
          getCompleter().cmpltMacheAmount();
        } else {
          getCompleter().cmpltMache();
        }
      }

      return retVal;
    }

    protected Object isCastle(OrderToken t) {
      return null;
    }

    protected BuildingType isBuilding(OrderToken t) {
      if (getRules() != null)
        return getRules().getBuildingType(t.getText());
      return null;
    }

    protected boolean readMacheTemp(OrderToken token) {
      token.ttype = OrderToken.TT_ID;

      OrderToken t = getNextToken();

      if (getCompleter() != null) {
        getCompleter().cmpltMacheTemp();
      }

      unexpected(t);

      return false; // there can't follow an id, else it would have been merged with TEMP
    }

    protected boolean readMacheTempID(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_ID;

      OrderToken t = getNextToken();

      if (isString(t)) {
        retVal = readDescription(t, false) != null;
      }
      if (isEoC(t)) {
        retVal = true;
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltMacheTempID();
      }

      return retVal;
    }

    protected boolean readMacheBurg(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isID(t.getText(), false)) {
        retVal = readFinalID(t);
      } else {
        retVal = checkFinal(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltMacheBurg();
      }
      return retVal;
    }

    protected boolean readMacheBuilding(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_STRING;

      OrderToken t = getNextToken();

      if (isID(t.getText(), false)) {
        retVal = readFinalID(t);
      } else {
        retVal = checkFinal(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltMacheBuilding(token.getText());
      }
      return retVal;
    }

    protected boolean readMacheShip(OrderToken token) {
      token.ttype = OrderToken.TT_STRING;

      return checkNextFinal();
    }

    protected boolean readMacheSchiff(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_STRING;

      OrderToken t = getNextToken();

      if (isID(t.getText(), false) == true) {
        retVal = readFinalID(t);
      } else {
        retVal = checkFinal(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltMacheSchiff();
      }
      return retVal;
    }

    protected boolean readMacheStrasse(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_STRING;

      OrderToken t = getNextToken();

      if (isString(t) && Direction.toDirection(t.getText(), getLocale()) != Direction.INVALID) {
        retVal = readFinalString(t);
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltMacheStrasse();
      }
      return retVal;
    }

    protected boolean readMacheAnything(OrderToken token) {
      boolean retVal = false;

      if (isString(token)) {
        retVal = checkItem(token) != null;
      }

      return retVal && checkFinal(getLastToken());
    }

  }

  // ************* NACH
  protected class NachReader extends OrderHandler {

    @Override
    protected void init(OrderToken token, String text) {
      order = new MovementOrder(getTokens(), text, false);
    }

    @Override
    public MovementOrder getOrder() {
      MovementOrder uorder = (MovementOrder) super.getOrder();
      return uorder;
    }

    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      OrderToken t = getNextToken();

      if (Direction.toDirection(t.getText(), getLocale()) != Direction.INVALID) {
        retVal = readNachDirection(t);
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltNach();
      }
      return retVal;
    }

    protected boolean readNachDirection(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (Direction.toDirection(t.getText(), getLocale()) != Direction.INVALID) {
        retVal = readNachDirection(t);
      } else {
        retVal = checkFinal(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltNachDirection();
      }
      return retVal;
    }
  }

  // ************* NEXT (NƒCHSTER)
  protected class InvalidReader extends OrderHandler {
    @Override
    protected boolean readIt(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;
      return false;
    }
  }

  // ************* NEUSTART
  protected class NeustartReader extends OrderHandler {
    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isString(t)) {
        retVal = new StringChecker(false, false, true, false) {
          @Override
          protected boolean checkInner() {
            return super.checkInner() && (getRules() != null)
                && (getRules().getRace(content) != null);
          }

          @Override
          protected void complete() {
            getCompleter().cmpltNeustart();
          }

          @Override
          protected boolean checkNext() {
            if (isString(nextToken))
              // password
              return readDescription(nextToken, false) != null;
            else {
              unexpected(nextToken);
              return false;
            }
          }
        }.read(t);
      } else {
        unexpected(t);
        if (shallComplete(token, t)) {
          getCompleter().cmpltStirb();
        }
      }

      return retVal;
    }
  }

  // ************* NUMMER
  protected class NummerReader extends OrderHandler {
    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (t.equalsToken(getOrderTranslation(EresseaConstants.O_UNIT)) == true) {
        retVal = readNummerEinheit(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_SHIP)) == true) {
        retVal = readNummerSchiff(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_FACTION)) == true) {
        retVal = readNummerPartei(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_CASTLE)) == true) {
        retVal = readNummerBurg(t);
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltNummer();
      }
      return retVal;
    }

    protected boolean readNummerEinheit(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isID(t.getText(), false)) {
        retVal = readFinalID(t);
      } else if (isEoC(t)) {
        retVal = true;
      } else {
        unexpected(t);
      }

      // if (shallComplet(token, t)){
      // getCompleter().cmpltNummerEinheit();
      // }

      return retVal;
    }

    protected boolean readNummerPartei(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isID(t.getText(), false)) {
        retVal = readFinalID(t);
      } else if (isEoC(t)) {
        retVal = true;
      } else {
        unexpected(t);
      }

      return retVal;
    }

    protected boolean readNummerSchiff(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isID(t.getText(), false)) {
        retVal = readFinalID(t);
      } else if (isEoC(t)) {
        retVal = true;
      } else {
        unexpected(t);
      }

      return retVal;
    }

    protected boolean readNummerBurg(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isID(t.getText(), false)) {
        retVal = readFinalID(t);
      } else if (isEoC(t)) {
        retVal = true;
      } else {
        unexpected(t);
      }

      return retVal;
    }
  }

  // ************* OPTION
  protected class OptionReader extends OrderHandler {
    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (t.equalsToken(getOrderTranslation(EresseaConstants.O_ADDRESSES))
          || t.equalsToken(getOrderTranslation(EresseaConstants.O_REPORT))
          || t.equalsToken(getOrderTranslation(EresseaConstants.O_BZIP2))
          || t.equalsToken(getOrderTranslation(EresseaConstants.O_COMPUTER))
          || t.equalsToken(getOrderTranslation(EresseaConstants.O_ITEMPOOL))
          || t.equalsToken(getOrderTranslation(EresseaConstants.O_SILVERPOOL))
          || t.equalsToken(getOrderTranslation(EresseaConstants.O_STATISTICS))
          || t.equalsToken(getOrderTranslation(EresseaConstants.O_ZIPPED))
          || t.equalsToken(getOrderTranslation(EresseaConstants.O_SCORE))
          || t.equalsToken(getOrderTranslation(EresseaConstants.O_TEMPLATE))) {
        retVal = readOptionOption(t);
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltOption();
      }
      return retVal;
    }

    protected boolean readOptionOption(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (t.equalsToken(getOrderTranslation(EresseaConstants.O_NOT))) {
        retVal = readFinalKeyword(t);
      } else {
        retVal = checkFinal(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltOptionOption();
      }
      return retVal;
    }
  }

  // ************* PARTEI
  protected class ParteiReader extends OrderHandler {
    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isID(t.getText(), false) == true) {
        retVal = readParteiFID(t);
      } else {
        unexpected(t);
      }

      return retVal;
    }

    protected boolean readParteiFID(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_ID;

      OrderToken t = getNextToken();

      if (isString(t)) {
        retVal = readFinalString(t);
      } else {
        unexpected(t);
      }

      return retVal;
    }
  }

  // ************* PASSWORT
  protected class PasswortReader extends OrderHandler {
    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isEoC(t)) {
        // PASSWORT without parameters is allowed
        retVal = true;
      } else if (isString(t)) {
        retVal = readDescription(t, false) != null;
      } else {
        unexpected(t);
      }

      return retVal;
    }
  }

  // ************* PFLANZEN
  protected class PflanzeReader extends OrderHandler {
    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      OrderToken t = getNextToken();

      if (isNumeric(t.getText()) == true) {
        retVal = readPflanzeAmount(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_HERBS))
          || t.equalsToken(getOrderTranslation(EresseaConstants.O_SEED))
          || t.equalsToken(getOrderTranslation(EresseaConstants.O_MALLORNSEED))
          || t.equalsToken(getOrderTranslation(EresseaConstants.O_TREES))) {
        t.ttype = OrderToken.TT_KEYWORD;
        retVal = checkNextFinal();
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltPflanze();
      }
      return retVal;
    }

    protected boolean readPflanzeAmount(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_NUMBER;

      // anzahl feststellen?
      int minAmount = 0;
      try {
        minAmount = Integer.parseInt(token.getText());
      } catch (NumberFormatException e) {
        // not parsable Number !?
      }

      OrderToken t = getNextToken();

      if (t.equalsToken(getOrderTranslation(EresseaConstants.O_HERBS))
          || t.equalsToken(getOrderTranslation(EresseaConstants.O_SEED))
          || t.equalsToken(getOrderTranslation(EresseaConstants.O_MALLORNSEED))
          || t.equalsToken(getOrderTranslation(EresseaConstants.O_TREES))) {
        t.ttype = OrderToken.TT_KEYWORD;
        retVal = checkNextFinal();
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltPflanze(minAmount);
      }
      return retVal;
    }

  }

  // ************* PIRATERIE
  protected class PiraterieReader extends OrderHandler {
    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      OrderToken t = getNextToken();

      if (isID(t.getText(), false)) {
        retVal = readPiraterieFID(t);
      } else {
        retVal = checkFinal(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltPiraterie();
      }
      return retVal;
    }

    protected boolean readPiraterieFID(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_ID;

      OrderToken t = getNextToken();

      if (isID(t.getText(), false)) {
        retVal = readPiraterieFID(t);
      } else {
        retVal = checkFinal(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltPiraterieFID();
      }
      return retVal;
    }
  }

  // ************* PRAEFIX
  protected class PraefixReader extends OrderHandler {
    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (!isEoC(t) && isString(t)) {
        retVal = new StringChecker(false, false, false, false).read(t);
      } else {
        retVal = checkFinal(t);
        if (shallComplete(token, t)) {
          getCompleter().cmpltPraefix();
        }
      }

      return retVal;
    }
  }

  // ************* REGION
  protected class RegionReader extends OrderHandler {
    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isRID(t.getText()) == true) {
        retVal = readFinalID(t);
      } else {
        unexpected(t);
      }

      return retVal;
    }
  }

  // ************* REKRUTIERE
  protected class RekrutiereReader extends OrderHandler {

    @Override
    protected void init(OrderToken token, String text) {
      order = new RecruitmentOrder(getTokens(), text);
    }

    @Override
    public RecruitmentOrder getOrder() {
      RecruitmentOrder uorder = (RecruitmentOrder) super.getOrder();
      return uorder;
    }

    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isNumeric(t.getText()) == true) {
        int val = getNumber(t.getText());
        getOrder().setAmount(val);

        retVal = readFinalNumber(t);
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltRekrutiere();
      }

      return retVal;
    }
  }

  // ************* RESERVIERE
  protected class ReserviereReader extends OrderHandler {

    @Override
    protected void init(OrderToken token, String text) {
      order = new ReserveOrder(getTokens(), text);
    }

    @Override
    public ReserveOrder getOrder() {
      ReserveOrder uorder = (ReserveOrder) super.getOrder();
      return uorder;
    }

    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();
      if (isNumeric(t.getText()) == true
          || t.equalsToken(getOrderTranslation(EresseaConstants.O_ALL))) {
        retVal = readReserviereAmount(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_EACH))) {
        retVal = readReserviereJe(t);
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltReserviere();
      }
      return retVal;
    }

    protected boolean readReserviereJe(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;
      getOrder().each = true;

      OrderToken t = getNextToken();

      if (isNumeric(t.getText()) == true) {
        retVal = readReserviereAmount(t);
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltReserviereJe();
      }
      return retVal;

    }

    protected boolean readReserviereAmount(OrderToken token) {
      boolean retVal = false; // FIXME use locale!!
      if (token.equalsToken(getOrderTranslation(EresseaConstants.O_ALL))) {
        token.ttype = OrderToken.TT_KEYWORD;
        getOrder().amount = Order.ALL;
      } else {
        token.ttype = OrderToken.TT_NUMBER;
        getOrder().amount = Integer.parseInt(token.getText());
      }

      OrderToken t = getNextToken();
      if (!isEoC(t) && isString(t) && !t.equalsToken(getOrderTranslation(EresseaConstants.O_ALL))) {
        // retVal = checkItem(t) != null && checkFinal(getLastToken());
        OrderToken[] result = getString(t);
        if (result[1] != null) {
          getOrder().itemID = StringID.create(result[1].getText());
        }
        retVal = result[1] != null && checkFinal(result[3]);
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltReserviereAmount();
      }
      return retVal;
    }
  }

  // ************* ROUTE
  protected class RouteReader extends OrderHandler {

    @Override
    protected void init(OrderToken token, String text) {
      order = new MovementOrder(getTokens(), text, true);
    }

    @Override
    public MovementOrder getOrder() {
      MovementOrder uorder = (MovementOrder) super.getOrder();
      return uorder;
    }

    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      OrderToken t = getNextToken();

      if (Direction.toDirection(t.getText(), getLocale()) != Direction.INVALID) {
        retVal = readRouteDirection(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_PAUSE))) {
        retVal = readRouteDirection(t);
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltRoute();
      }
      return retVal;
    }

    protected boolean readRouteDirection(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (Direction.toDirection(t.getText(), getLocale()) != Direction.INVALID) {
        retVal = readRouteDirection(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_PAUSE))) {
        retVal = readRouteDirection(t);
      } else {
        retVal = checkFinal(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltRouteDirection();
      }
      return retVal;
    }

  }

  // ************* SABOTIERE
  protected class SabotiereReader extends OrderHandler {
    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      OrderToken t = getNextToken();

      if (t.equalsToken(getOrderTranslation(EresseaConstants.O_SHIP))) {
        retVal = readFinalKeyword(t);
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltSabotiere();
      }
      return retVal;
    }
  }

  // ************* SORTIERE
  protected class SortiereReader extends OrderHandler {
    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      // FIX
      if (t.equalsToken(getOrderTranslation(EresseaConstants.O_BEFORE))) {
        retVal = readSortiereVor(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_AFTER))) {
        retVal = readSortiereHinter(t);
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltSortiere();
      }
      return retVal;
    }

    protected boolean readSortiereVor(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isID(t.getText())) {
        retVal = readFinalID(t);
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltSortiereVor();
      }
      return retVal;
    }

    protected boolean readSortiereHinter(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isID(t.getText())) {
        retVal = readFinalID(t);
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltSortiereHinter();
      }
      return retVal;
    }
  }

  // ************* SPIONIERE
  protected class SpioniereReader extends OrderHandler {
    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      OrderToken t = getNextToken();

      if (isID(t.getText()) == true) {
        retVal = readFinalID(t, true);
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltSpioniere();
      }
      return retVal;
    }
  }

  // ************* STIRB
  protected class StirbReader extends OrderHandler {
    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isString(t)) {
        // password
        retVal = new StringChecker(true, true, true, false) {
          @Override
          protected void complete() {
            getCompleter().cmpltStirb();
          }
        }.read(t);
      } else {
        unexpected(t);
        if (shallComplete(token, t)) {
          getCompleter().cmpltStirb();
        }
      }

      return retVal;
    }
  }

  // ************* TARNE
  protected class TarneReader extends OrderHandler {
    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isNumeric(t.getText()) == true) {
        retVal = readFinalNumber(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_FACTION))) {
        retVal = readTarnePartei(t);
      } else if (isString(t) && !checkFinal(t)) {
        retVal = new StringChecker(false, false, true, false) {
          @Override
          protected boolean checkInner() {
            return super.checkInner() && (getRules() != null)
                && (getRules().getRace(content) != null);
          }

          @Override
          protected void complete() {
            getCompleter().cmpltTarne(openingToken != null);
          }
        }.read(t);
      } else {
        retVal = checkFinal(t);
        if (shallComplete(token, t)) {
          getCompleter().cmpltTarne(false);
        }
      }

      return retVal;
    }

    protected boolean readTarnePartei(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (t.equalsToken(getOrderTranslation(EresseaConstants.O_NOT))) {
        retVal = readFinalKeyword(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_NUMBER))) {
        retVal = readTarneParteiNummer(t);
      } else {
        retVal = checkFinal(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltTarnePartei();
      }
      return retVal;
    }

    protected boolean readTarneParteiNummer(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isID(t.getText(), false)) {
        retVal = readFinalID(t);
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltTarneParteiNummer();
      }
      return retVal;
    }
  }

  // ************* TRANSPORTIERE
  protected class TransportiereReader extends OrderHandler {
    @Override
    protected void init(OrderToken token, String text) {
      order = new TransportOrder(getTokens(), text, null);
    }

    @Override
    public TransportOrder getOrder() {
      TransportOrder corder = (TransportOrder) super.getOrder();
      return corder;
    }

    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isID(t.getText()) == true) {
        // Unit target = getUnit(t.getText());
        getOrder().target = UnitID.createUnitID(t.getText(), data.base);

        retVal = readFinalID(t, true);
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltTransportiere(false);
      }
      return retVal;
    }
  }

  // ************* TREIBE
  protected class TreibeReader extends OrderHandler {
    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      OrderToken t = getNextToken();

      if (isNumeric(t.getText()) == true) {
        retVal = readFinalNumber(t);
      } else {
        retVal = checkFinal(t);
      }

      return retVal;
    }
  }

  // ************* UNTERHALTE
  protected class UnterhalteReader extends OrderHandler {
    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      OrderToken t = getNextToken();

      if (isNumeric(t.getText()) == true) {
        retVal = readFinalNumber(t);
      } else {
        retVal = checkFinal(t);
      }

      return retVal;
    }
  }

  // ************* URSPRUNG
  protected class UrsprungReader extends OrderHandler {
    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isNumeric(t.getText(), 10, Integer.MIN_VALUE, Integer.MAX_VALUE) == true) {
        retVal = readUrsprungX(t);
      } else {
        unexpected(t);
      }

      return retVal;
    }

    protected boolean readUrsprungX(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_ID;

      OrderToken t = getNextToken();

      if (isNumeric(t.getText(), 10, Integer.MIN_VALUE, Integer.MAX_VALUE) == true) {
        retVal = readFinalID(t);
      } else {
        unexpected(t);
      }

      return retVal;
    }
  }

  // ************* VERGESSE
  protected class VergesseReader extends OrderHandler {
    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isString(t) && (getRules() != null) && (getRules().getSkillType(t.getText()) != null)) {
        retVal = readFinalString(t);
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltVergesse();
      }
      return retVal;
    }
  }

  // ************* VERKAUFE
  protected class VerkaufeReader extends OrderHandler {
    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      OrderToken t = getNextToken();

      if (isNumeric(t.getText()) == true) {
        retVal = readVerkaufeAmount(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_ALL))) {
        retVal = readVerkaufeAlles(t);
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltVerkaufe();
      }
      return retVal;
    }

    protected boolean readVerkaufeAmount(OrderToken token) {
      boolean retVal = false;
      ItemType type = null;
      ItemCategory luxuryCategory =
          (getRules() != null) ? getRules().getItemCategory(EresseaConstants.C_LUXURIES) : null;
      token.ttype = OrderToken.TT_NUMBER;

      OrderToken t = getNextToken();

      if (isString(t) && (getRules() != null)
          && ((type = getRules().getItemType(t.getText())) != null) && (luxuryCategory != null)
          && type.getCategory().equals(luxuryCategory)) {
        retVal = readFinalString(t);
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltVerkaufeAmount();
      }
      return retVal;
    }

    protected boolean readVerkaufeAlles(OrderToken token) {
      boolean retVal = false;
      ItemType type = null;
      ItemCategory luxuryCategory =
          (getRules() != null) ? getRules().getItemCategory(EresseaConstants.C_LUXURIES) : null;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isString(t) && (getRules() != null)
          && ((type = getRules().getItemType(t.getText())) != null) && (luxuryCategory != null)
          && luxuryCategory.equals(type.getCategory())) {
        retVal = readFinalString(t);
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltVerkaufeAlles();
      }
      return retVal;
    }
  }

  // ************ VERLASSE
  protected class VerlasseReader extends OrderHandler {

    @Override
    protected void init(OrderToken token, String text) {
      order = new LeaveOrder(getTokens(), text);
    }

    @Override
    public LeaveOrder getOrder() {
      LeaveOrder uorder = (LeaveOrder) super.getOrder();
      return uorder;
    }

    @Override
    protected boolean readIt(OrderToken token) {
      return readFinalKeyword(token);
    }
  }

  // ************* ZAUBERE
  protected class ZaubereReader extends OrderHandler {
    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      OrderToken t = getNextToken();

      if (t.equalsToken(getOrderTranslation(EresseaConstants.O_REGION))) {
        retVal = readZaubereRegion(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_LEVEL))) {
        retVal = readZaubereStufe(t, false);
      } else if (isString(t)) {
        retVal = new ZaubereSpruchChecker(false, false, true, true) {
        }.read(t);
      } else {
        unexpected(t);
        if (shallComplete(token, t)) {
          getCompleter().cmpltZaubere(false, false, true, true, "\"", "\"");
        }
      }

      return retVal;
    }

    protected boolean readZaubereRegion(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isNumeric(t.getText(), 10, Integer.MIN_VALUE, Integer.MAX_VALUE)) {
        retVal = readZaubereRegionCoor(t);
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltZaubereRegion();
      }
      return retVal;
    }

    protected boolean readZaubereRegionCoor(OrderToken token) {
      boolean retVal = false;

      // x-coordinate
      token.ttype = OrderToken.TT_NUMBER;

      OrderToken t = getNextToken();

      if (isNumeric(t.getText(), 10, Integer.MIN_VALUE, Integer.MAX_VALUE)) {
        // y-coordinate
        t.ttype = OrderToken.TT_NUMBER;
        t = getNextToken();

        if (t.equalsToken(getOrderTranslation(EresseaConstants.O_LEVEL))) {
          retVal = readZaubereRegionStufe(t);
        } else if (isString(t)) {
          retVal = new ZaubereSpruchChecker(true, false, false, true).read(t);
        } else {
          unexpected(t);
          if (shallComplete(token, t)) {
            getCompleter().cmpltZaubere(true, false, false, true, "\"", "\"");
          }
        }

      }

      return retVal;
    }

    protected boolean readZaubereStufe(OrderToken token, boolean combat) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isNumeric(t.getText())) {
        t.ttype = OrderToken.TT_NUMBER;
        t = getNextToken();
        if (isString(t)) {
          retVal = new ZaubereSpruchChecker(false, combat, false, false).read(t);
        } else {
          unexpected(t);
          if (shallComplete(token, t)) {
            getCompleter().cmpltZaubere(false, combat, false, false, "\"", "\"");
          }
        }
      } else {
        unexpected(t);
        if (shallComplete(token, t)) {
          getCompleter().cmpltZaubereStufe();
        }
      }
      return retVal;
    }

    protected boolean readZaubereRegionStufe(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isNumeric(t.getText())) {
        t.ttype = OrderToken.TT_NUMBER;
        t = getNextToken();
        if (isString(t)) {
          retVal = new ZaubereSpruchChecker(true, false, false, false).read(t);
        } else {
          unexpected(t);
          if (shallComplete(token, t)) {
            getCompleter().cmpltZaubere(true, false, false, false, "\"", "\"");
          }
        }
      } else {
        unexpected(t);
        if (shallComplete(token, t)) {
          getCompleter().cmpltZaubereRegionStufe();
        }
      }

      return retVal;
    }

    protected class ZaubereSpruchChecker extends StringChecker {

      private boolean combat;
      private Spell foundSpell;
      private boolean far;
      private boolean addRegion;
      private boolean addLevel;

      public ZaubereSpruchChecker(boolean far, boolean combat, boolean addRegion, boolean addLevel) {
        super(false, true, true, false);
        this.far = far;
        this.combat = combat;
        this.addRegion = addRegion;
        this.addLevel = addLevel;
      }

      @Override
      protected boolean checkInner() {
        if (!super.checkInner())
          return false;

        // checken, ob der Zauberspruch bekannt ist
        // Problem: keine Referenz auf die Unit, wir kˆnnen nicht die spells der unit durchgehen
        // wir m¸ssen spells der GameData durchgehen
        if (getData().spells() == null || getData().spells().size() == 0)
          return false;
        foundSpell = null;
        for (Spell s : getData().spells().values()) {
          if (content.equalsIgnoreCase(s.getName())) {
            // here we return just true
            // toDo: get Spell Syntax, check, if more tokens expected and
            // do next checks
            if (s.getType() == null || (!combat ^ s.getType().toLowerCase().indexOf("combat") > -1)) {
              foundSpell = s;
              break;
            }
          }
        }
        // FIXME this is not necessarily /syntactically/ incorrect...
        return foundSpell != null;
      }

      @Override
      protected void complete() {
        getCompleter().cmpltZaubere(far, combat, addRegion && openingToken == null,
            addLevel && openingToken == null, "", "");
      }

      @Override
      protected boolean checkNext() {
        if (content.length() > 0)
          return readZaubereEnde(nextToken, foundSpell);
        return false;
      }
    }

    protected boolean readZaubereEnde(OrderToken t, Spell s) {
      // if (!isEoC(t)) {
      // t = skipRestOfOrder();
      // }
      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltZaubereSpruch(s);
      }
      if (s == null || s.getSyntax() == null)
        // FIXME this is not /syntactically/ incorrect...
        return false;
      return !isEoC(t) ^ s.getSyntax().length() == 0;
    }

    /**
     * skips rest of Line
     */
    protected OrderToken skipRestOfOrder() {
      if (!hasNextToken())
        return null;
      OrderToken t = getNextToken();
      while ((!isEoC(t)) && (t.ttype != OrderToken.TT_COMMENT) && hasNextToken()) {
        t = getNextToken();
      }
      return t;
    }

    // protected boolean readZaubereSpruch(OrderToken token, Spell s) {
    // boolean retVal = false;
    // token.ttype = OrderToken.TT_STRING;
    //
    // OrderToken t = tokens.next();
    //
    // SpellSyntax ss = s.getSpellSyntax();
    // ss.reset();
    // SpellSyntaxToken sst = ss.getNextToken();
    //
    // retVal = readZaubereSyntax(t, sst);
    //
    // return retVal;
    // }

    // protected boolean readZaubereSyntax(OrderToken token, SpellSyntaxToken sst) {
    // switch (sst.getTokenType()) {
    // case SpellSyntaxToken.SST_KeyWord: {
    // if(token.equalsToken(getOrderTranslation(EresseaConstants.O_CASTLE))) {
    // token.ttype = OrderToken.TT_KEYWORD;
    // token = tokens.next();
    // token.ttype = OrderToken.TT_ID;
    //
    // } else if(token.equalsToken(getOrderTranslation(EresseaConstants.O_UNIT))) {
    //
    // } else if(token.equalsToken(getOrderTranslation(EresseaConstants.O_FACTION))) {
    //
    // } else if(token.equalsToken(getOrderTranslation(EresseaConstants.O_REGION))) {
    //
    // } else if(token.equalsToken(getOrderTranslation(EresseaConstants.O_SHIP))) {
    //
    // }
    // }
    // }
    // return false;
    // }}

  }

  // ************* ZEIGE
  protected class ZeigeReader extends OrderHandler {
    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (t.equalsToken(getOrderTranslation(EresseaConstants.O_ALL))) {
        retVal = readZeigeAlle(t);
      } else if (isString(t)) {
        retVal = new StringChecker(false, false, true, false) {
          @Override
          protected void complete() {
            getCompleter().cmpltZeige();
          }
        }.read(t);
      } else {
        unexpected(t);
        if (shallComplete(token, t)) {
          getCompleter().cmpltZeige();
        }
      }

      return retVal;
    }

    protected boolean readZeigeAlle(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (t.equalsToken(getOrderTranslation(EresseaConstants.O_POTIONS))
          || t.equalsToken(getOrderTranslation(EresseaConstants.O_SPELLS))) {
        retVal = readFinalKeyword(t);
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltZeigeAlle();
      }
      return retVal;
    }
  }

  // ************* ZERSTOERE
  protected class ZerstoereReader extends OrderHandler {
    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isNumeric(t.getText())) {
        retVal = readZerstoereAmount(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_ROAD))) {
        retVal = readZerstoereStrasse(t);
      } else {
        retVal = checkFinal(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltZerstoere();
      }
      return retVal;
    }

    protected boolean readZerstoereAmount(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_NUMBER;

      OrderToken t = getNextToken();

      if (t.equalsToken(getOrderTranslation(EresseaConstants.O_ROAD))) {
        retVal = readZerstoereStrasse(t);
      } else {
        retVal = checkFinal(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltZerstoere();
      }
      return retVal;
    }

    protected boolean readZerstoereStrasse(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_STRING;

      OrderToken t = getNextToken();

      if (isString(t) && Direction.toDirection(t.getText(), getLocale()) != Direction.INVALID) {
        retVal = readFinalString(t);
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltZerstoereStrasse();
      }
      return retVal;
    }
  }

  // ************* ZUECHTE
  protected class ZuechteReader extends OrderHandler {
    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      OrderToken t = getNextToken();

      boolean number = false;
      if (isNumeric(t.getText())) {
        t.ttype = OrderToken.TT_NUMBER;
        try {
          Integer.parseInt(t.getText());
          number = true;
        } catch (NumberFormatException e) {
          return false;
        }
        t = getNextToken();
      }

      if (t.equalsToken(getOrderTranslation(EresseaConstants.O_HERBS))
          || (!number && t.equalsToken(getOrderTranslation(EresseaConstants.O_HORSES))))
      // this is illegal (now?)
      // || t.equalsToken(getOrderTranslation(EresseaConstants.O_TREES))
      {
        retVal = readFinalKeyword(t);
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltZuechte();
      }
      return retVal;
    }
  }

  // ************* general use

  /** The StringChecker class may serve as a utility template for checking string tokens */
  protected class StringChecker {
    protected char defaultQuote = '"';
    private boolean forceQuotes;
    private boolean allowQuotes;
    private boolean allowEmpty;
    protected boolean valid;
    protected String content;
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
     * @param allowQuotes if this is <code>true</code>, the string may be enclosed in (single or
     *          double) quotes.
     * @param allowEmpty if this is <code>true</code>, the content may be empty.
     * @throws IllegalArgumentException if <code>forceQuotes</code> but not <code>allowQuote</code>
     *           or if <code>allowEmpty</code> but not <code>allowQuotes</code>.
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
     * Tries to parse a string beginning with <code>token</code>. It first splits the following
     * string tokens into {@link #openingToken}, {@link #innerToken}, {@link #closingToken}, and
     * {@link #nextToken} (token after the string). Then it checks them with {@link #checkInner()},
     * clears completions, calls {@link #checkNext()}. If there is a completer and the last token of
     * the string is followed by space, it calls {@link #complete()}. The completions are then
     * garnished with quotes as specified by the do... methods.
     * 
     * @return {@link #checkInner()} && {@link #checkNext()}
     * @see EresseaOrderParser#getString(OrderToken)
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
            valid, defaultQuote);
      }
      return valid && isQuotesValid() && nextValid;
    }

    /**
     * If this returns <code>true</code>, the completer is called. In this implementation this is
     * the case if the last string token is not followed by space and the completer is not
     * <code>null</code>.
     */
    protected boolean isComplete() {
      return getCompleter() != null && !followedBySpace;
    }

    /**
     * Checks the quotes. According to constructor parameters. Subclasses usually don't need to
     * overwrite this method.
     * 
     * @return <code>false</code> if there are no quotes but quotes are forced or if the closing
     *         quote doesn't match the opening quote.
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
     * @see EresseaOrderParser#getString(OrderToken)
     * @param tokens
     * @return
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
     * Returns {@link EresseaOrderParser#checkFinal(OrderToken)}. Subclasses should overwrite to
     * check the rest of the order.
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
    @Override
    protected boolean readIt(OrderToken token) {
      return readFinalKeyword(token);
    }
  }

  /**
   * Tests if the next token is a quoted (possibly empty) string at the end of the order.
   * 
   * @return The string (without quotes) if a valid description was found, otherwise
   *         <code>null</code>
   */
  protected String readDescription() {
    return readDescription(true);
  }

  /**
   * Tests if the next token is a quoted string at the end of the order.
   * 
   * @return The string (without quotes) if a valid description was found, otherwise
   *         <code>null</code>
   */
  protected String readDescription(boolean allowEmpty) {
    OrderToken t = getNextToken();
    return readDescription(t, allowEmpty);
  }

  /**
   * Tests if t is a quoted (possibly empty) string at the end of the order.
   * 
   * @return The string (without quotes) if a valid description was found, otherwise
   *         <code>null</code>
   */
  protected String readDescription(OrderToken t) {
    return readDescription(t, true);
  }

  /**
   * Returns <code>true</code> if t is a quoted string at the end of the order. If
   * <code>allowEmpty</code>, it may also be an empty string.
   * 
   * @return The string (without quotes) if a valid description was found, otherwise
   *         <code>null</code>
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

    if (shallComplete(token, t)
        && token.getText().equalsIgnoreCase(getOrderTranslation(EresseaConstants.O_TEMP))
        && tempAllowed) {
      getCompleter().addRegionUnits("", true);
    }

    return (tempAllowed || !token.getText().toLowerCase().startsWith(
        getOrderTranslation(EresseaConstants.O_TEMP).toLowerCase()))
        && checkFinal(t);
  }

  protected boolean readFinalNumber(OrderToken token) {
    token.ttype = OrderToken.TT_NUMBER;

    return checkNextFinal();
  }

  /**
   * Checks whether the next token is the end of line or a comment, i.e. the indicating a valid end
   * of the order. Reports an unexpected token if that is not the case.
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
        if (normalizeName(type.getName()).equalsIgnoreCase(normalizeName(content))) {
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

  /**
   * Tests if <code>txt</code> represents an integer with the given radix between min and max
   * (inclusively).
   * 
   * @param txt
   * @param radix
   * @param min
   * @param max
   * @return
   */
  protected boolean isNumeric(String txt, int radix, int min, int max) {
    boolean retVal = false;

    try {
      int i = Integer.parseInt(txt, radix);
      retVal = ((i >= min) && (i <= max));
    } catch (NumberFormatException e) {
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
   * Tests if <code>txt</code> represents a valid ID (or TEMP ID) given the <code>data.base</code>
   * and {@link #MAX_UID}.
   * 
   * @param txt
   * @return
   */
  protected boolean isID(String txt, boolean allowTemp) {
    boolean retVal = isNumeric(txt, getData().base, 0, EresseaOrderParser.MAX_UID);

    if (!retVal && allowTemp) {
      retVal = isTempID(txt);
    }

    return retVal;
  }

  /**
   * Tests if <code>txt</code> represents a valid TEMP id.
   * 
   * @param txt
   * @return
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
      retVal = (temp.equalsIgnoreCase(getOrderTranslation(EresseaConstants.O_TEMP)));
      retVal = retVal && isNumeric(nr, getData().base, 0, EresseaOrderParser.MAX_UID);
    }

    return retVal;
  }

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
          EresseaOrderParser.log.warn("isRID()", e);
        }
      } else {
        try {
          Integer.parseInt(txt.substring(0, firstCommaPos));
          Integer.parseInt(txt.substring(firstCommaPos + 1, txt.length()));
          retVal = true;
        } catch (NumberFormatException e) {
          EresseaOrderParser.log.debug("isRID()", e);
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

  /**
   * Returns <code>true</code> if <code>txt</code> is a nonempty string composed solely by the
   * characters [A-Za-zƒ÷‹‰ˆ¸ﬂ~,._:0-9] that does not start with a number.
   */
  protected boolean isSimpleString(String txt) {
    return Pattern.matches("[A-Za-zƒ÷‹‰ˆ¸ﬂ~,._:][A-Za-zƒ÷‹‰ˆ¸ﬂ~,._:0-9]*", txt);
  }

  /**
   * Tests if <code>txt</code> is a nonempty string which is either surrounded by quotes or by
   * double quotes, or is composed solely by the characters [A-Za-zƒ÷‹‰ˆ¸ﬂ~,._:].
   * 
   * @param txt
   * @return
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
            || (c == ',') || (c == '.') || (c == '_') || (c == ':') || ((!isNumeric) && (c >= '0') && (c <= '9')))) {
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
   * Same as {@link #isString(OrderToken, boolean) isString(OrderToken, false)}
   */
  protected boolean isString(OrderToken token) {
    return isString(token, false);
  }

  /**
   * Returns <code>true</code> if token starts a string. That is, it must be either a
   * {@link OrderToken#TT_OPENING_QUOTE} or a string token.
   * 
   * @param token the current token
   * @param forceQuotes if this is <code>true</code>, token must be a TT_OPENING_QUOTE
   */
  protected boolean isString(OrderToken token, boolean forceQuotes) {
    return token.ttype == OrderToken.TT_OPENING_QUOTE
        || (!forceQuotes && (isSimpleString(token.getText())) || isEoC(token));
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
   * @return
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
   * Return true if a completions should be added to the completer for the current token. The
   * standard version returns true if there is a OrderCompleter and <code>token</code> is followed
   * by a space and <code>t</code> is not.
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
      OrderTokenizer tokenizer = new OrderTokenizer(in);
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

          if (tempText.equalsIgnoreCase(getOrderTranslation(EresseaConstants.O_TEMP))) {
            try {
              OrderToken nrToken = tokenAt(i + 1);
              String nrText = nrToken.getText();
              int nr = IDBaseConverter.parse(nrText, base);

              if ((nr >= 0) && (nr <= TokenBucket.MAX_TEMP_NR)) {
                OrderToken mergedToken =
                    new OrderToken(getOrderTranslation(EresseaConstants.O_TEMP) + " " + nrText,
                        tempToken.getStart(), nrToken.getEnd(), OrderToken.TT_ID, nrToken
                            .followedBySpace());
                remove(i);
                remove(i);
                add(i++, mergedToken);
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
}