/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.library.gamebinding;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

import magellan.library.GameData;
import magellan.library.Spell;
import magellan.library.UnitID;
import magellan.library.completion.OrderParser;
import magellan.library.rules.BuildingType;
import magellan.library.rules.CastleType;
import magellan.library.rules.ItemCategory;
import magellan.library.rules.ItemType;
import magellan.library.rules.SkillType;
import magellan.library.utils.Direction;
import magellan.library.utils.IDBaseConverter;
import magellan.library.utils.OrderToken;
import magellan.library.utils.OrderTokenizer;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;
import ds.tree.RadixTree;
import ds.tree.RadixTreeImpl;

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

  public static final char[] QUOTES = new char[] { '\'', '"' };
  private String errMsg = null;
  private TokenBucket tokenBucket = null;
  private Iterator<OrderToken> tokensIterator = null;
  private EresseaOrderCompleter completer = null;
  private GameData data = null;

  private RadixTree<OrderHandler> commandTrie;

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
    abstract boolean read(OrderToken token);
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
    tokenBucket = new TokenBucket();
    setCompleter(cc);
    this.data = data;
    initCommands();
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

  /**
   * Fills the trie with all known orders. Subclasses may override this in order to set a different
   * set of orders.
   */
  protected void initCommands() {
    commandTrie = new RadixTreeImpl<OrderHandler>();

    commandTrie.insert("@", new AtReader());
    commandTrie.insert(Resources.getOrderTranslation(EresseaConstants.O_WORK), new WorkReader());
    commandTrie
        .insert(Resources.getOrderTranslation(EresseaConstants.O_ATTACK), new AttackReader());
    commandTrie
        .insert(Resources.getOrderTranslation(EresseaConstants.O_BANNER), new BannerReader());
    commandTrie.insert(Resources.getOrderTranslation(EresseaConstants.O_CLAIM),
        new BeansprucheReader());
    commandTrie.insert(Resources.getOrderTranslation(EresseaConstants.O_PROMOTION),
        new BefoerderungReader());
    commandTrie
        .insert(Resources.getOrderTranslation(EresseaConstants.O_STEAL), new BeklaueReader());
    commandTrie.insert(Resources.getOrderTranslation(EresseaConstants.O_SIEGE),
        new BelagereReader());
    commandTrie.insert(Resources.getOrderTranslation(EresseaConstants.O_NAME), new BenenneReader());
    commandTrie.insert(Resources.getOrderTranslation(EresseaConstants.O_USE), new BenutzeReader());
    commandTrie.insert(Resources.getOrderTranslation(EresseaConstants.O_DESCRIBE),
        new BeschreibeReader());
    commandTrie
        .insert(Resources.getOrderTranslation(EresseaConstants.O_ENTER), new BetreteReader());
    commandTrie
        .insert(Resources.getOrderTranslation(EresseaConstants.O_GUARD), new BewacheReader());
    commandTrie.insert(Resources.getOrderTranslation(EresseaConstants.O_MESSAGE),
        new BotschaftReader());
    commandTrie.insert(Resources.getOrderTranslation(EresseaConstants.O_DEFAULT),
        new DefaultReader());
    commandTrie.insert(Resources.getOrderTranslation(EresseaConstants.O_EMAIL), new EmailReader());
    commandTrie.insert(Resources.getOrderTranslation(EresseaConstants.O_END), new EndeReader());
    commandTrie.insert(Resources.getOrderTranslation(EresseaConstants.O_RIDE), new FahreReader());
    commandTrie.insert(Resources.getOrderTranslation(EresseaConstants.O_FOLLOW), new FolgeReader());
    commandTrie.insert(Resources.getOrderTranslation(EresseaConstants.O_RESEARCH),
        new ForscheReader());
    commandTrie.insert(Resources.getOrderTranslation(EresseaConstants.O_GIVE), new GibReader());
    commandTrie.insert(Resources.getOrderTranslation(EresseaConstants.O_GROUP), new GruppeReader());
    commandTrie.insert(Resources.getOrderTranslation(EresseaConstants.O_HELP), new HelfeReader());
    commandTrie.insert(Resources.getOrderTranslation(EresseaConstants.O_COMBAT),
        new KaempfeReader());
    commandTrie.insert(Resources.getOrderTranslation(EresseaConstants.O_COMBATSPELL),
        new KampfzauberReader());
    commandTrie.insert(Resources.getOrderTranslation(EresseaConstants.O_BUY), new KaufeReader());
    commandTrie.insert(Resources.getOrderTranslation(EresseaConstants.O_CONTACT),
        new KontaktiereReader());
    commandTrie.insert(Resources.getOrderTranslation(EresseaConstants.O_TEACH), new LehreReader());
    commandTrie.insert(Resources.getOrderTranslation(EresseaConstants.O_LEARN), new LerneReader());
    commandTrie.insert(Resources.getOrderTranslation(EresseaConstants.O_SUPPLY), new GibReader());
    commandTrie
        .insert(Resources.getOrderTranslation(EresseaConstants.O_LOCALE), new LocaleReader());
    commandTrie.insert(Resources.getOrderTranslation(EresseaConstants.O_MAKE), new MacheReader());
    commandTrie.insert(Resources.getOrderTranslation(EresseaConstants.O_MOVE), new NachReader());
    commandTrie.insert(Resources.getOrderTranslation(EresseaConstants.O_NEXT),
        new FinalKeywordReader());
    commandTrie
        .insert(Resources.getOrderTranslation(EresseaConstants.O_NUMBER), new NummerReader());
    commandTrie
        .insert(Resources.getOrderTranslation(EresseaConstants.O_OPTION), new OptionReader());
    commandTrie.insert(Resources.getOrderTranslation(EresseaConstants.O_FACTION),
        new ParteiReader());
    commandTrie.insert(Resources.getOrderTranslation(EresseaConstants.O_PASSWORD),
        new PasswortReader());
    commandTrie
        .insert(Resources.getOrderTranslation(EresseaConstants.O_PLANT), new PflanzeReader());
    commandTrie.insert(Resources.getOrderTranslation(EresseaConstants.O_PIRACY),
        new PiraterieReader());
    commandTrie.insert(Resources.getOrderTranslation(EresseaConstants.O_PREFIX),
        new PraefixReader());
    commandTrie
        .insert(Resources.getOrderTranslation(EresseaConstants.O_REGION), new RegionReader());
    commandTrie.insert(Resources.getOrderTranslation(EresseaConstants.O_RECRUIT),
        new RekrutiereReader());
    commandTrie.insert(Resources.getOrderTranslation(EresseaConstants.O_RESERVE),
        new ReserviereReader());
    commandTrie.insert(Resources.getOrderTranslation(EresseaConstants.O_ROUTE), new RouteReader());
    commandTrie
        .insert(Resources.getOrderTranslation(EresseaConstants.O_SORT), new SortiereReader());
    commandTrie
        .insert(Resources.getOrderTranslation(EresseaConstants.O_SPY), new SpioniereReader());
    commandTrie.insert(Resources.getOrderTranslation(EresseaConstants.O_QUIT), new StirbReader());
    commandTrie.insert(Resources.getOrderTranslation(EresseaConstants.O_HIDE), new TarneReader());
    commandTrie.insert(Resources.getOrderTranslation(EresseaConstants.O_CARRY),
        new TransportiereReader());
    commandTrie.insert(Resources.getOrderTranslation(EresseaConstants.O_TAX), new TreibeReader());
    commandTrie.insert(Resources.getOrderTranslation(EresseaConstants.O_ENTERTAIN),
        new UnterhalteReader());
    commandTrie.insert(Resources.getOrderTranslation(EresseaConstants.O_ORIGIN),
        new UrsprungReader());
    commandTrie.insert(Resources.getOrderTranslation(EresseaConstants.O_FORGET),
        new VergesseReader());
    commandTrie
        .insert(Resources.getOrderTranslation(EresseaConstants.O_SELL), new VerkaufeReader());
    commandTrie.insert(Resources.getOrderTranslation(EresseaConstants.O_LEAVE),
        new FinalKeywordReader());
    commandTrie.insert(Resources.getOrderTranslation(EresseaConstants.O_CAST), new ZaubereReader());
    commandTrie.insert(Resources.getOrderTranslation(EresseaConstants.O_SHOW), new ZeigeReader());
    commandTrie.insert(Resources.getOrderTranslation(EresseaConstants.O_DESTROY),
        new ZerstoereReader());
    commandTrie.insert(Resources.getOrderTranslation(EresseaConstants.O_GROW), new ZuechteReader());
    commandTrie.insert(Resources.getOrderTranslation(EresseaConstants.O_SABOTAGE),
        new SabotiereReader());
  }

  /**
   * Adds the specified handler for the specified command (and removes any previous handler for the
   * command).
   * 
   * @param prefix
   * @param handler
   */
  protected void addCommand(String prefix, OrderHandler handler) {
    if (commandTrie.contains(prefix))
      commandTrie.delete(prefix);
    commandTrie.insert(prefix, handler);
  }

  /**
   * Removes the handler for the specified command.
   * 
   * @param prefix
   */
  protected void removeCommand(String prefix) {
    if (commandTrie.contains(prefix))
      commandTrie.delete(prefix);
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
   * Returns the value of tokenBucket.
   * 
   * @return Returns tokenBucket.
   */
  public Iterator<OrderToken> getTokensIterator() {
    return tokensIterator;
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
   * Parses one line of text from the specified stream by tokenizing it and checking the syntax.
   * 
   * @param in the stream to read the order from.
   * @return <tt>true</tt> if the syntax of the order read is valid, <tt>false</tt> else.
   */
  public boolean read(Reader in) {
    setErrMsg(null);
    tokenBucket.read(in);
    tokenBucket.mergeTempTokens(getData().base);
    tokensIterator = tokenBucket.iterator();

    boolean retVal = true;

    while (getTokensIterator().hasNext() && retVal) {
      OrderToken token = getTokensIterator().next();

      if (token.ttype != OrderToken.TT_COMMENT) {
        retVal = readOrder(token);
      }
    }

    return retVal;
  }

  /**
   * Matches the token with the keys in {@link #getHandlers(OrderToken)}. If there is exactly one
   * handler, it is applied to the <code>t</code> and the result returned. Otherwise this method
   * returns <code>true</code> if <code>t</code> is the final token. Afterwards the completer is
   * applied.
   * 
   * @param t
   * @return
   */
  protected boolean readOrder(OrderToken t) {
    boolean retVal = false;

    ArrayList<OrderHandler> readers = getHandlers(t);
    if (readers.size() == 1) {
      OrderHandler r = readers.iterator().next();
      retVal = r.read(t);
    } else {
      retVal = checkFinal(t);
    }
    if (getCompleter() != null && !t.followedBySpace() && t.ttype != OrderToken.TT_PERSIST) {
      getCompleter().cmplt();
    }

    return retVal;
  }

  /**
   * Returns a set of handlers that match the specified token.
   * 
   * @param t
   * @return
   */
  protected ArrayList<OrderHandler> getHandlers(OrderToken t) {
    return commandTrie.searchPrefix(t.getText(), Integer.MAX_VALUE);
  }

  // protected boolean read([^(]*)\(([^)]*)\) \{
  // protected class \1Reader extends OrderReader { public boolean read(\2) {
  // ************* AT
  protected class AtReader extends OrderHandler {
    public boolean read(OrderToken token) {
      OrderToken t = getTokensIterator().next();

      return readOrder(t);
    }
  }

// ************* WORK (ARBEITE)
  protected class WorkReader extends OrderHandler {
    public boolean read(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;

      return checkNextFinal();
    }
  }

// ************* ATTACK (ATTACKIERE)
  protected class AttackReader extends OrderHandler {
    public boolean read(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (isID(t.getText()) == true) {
        retVal = readAttackUID(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltAttack();
      }

      return retVal;
    }

    protected boolean readAttackUID(OrderToken token) {
      token.ttype = OrderToken.TT_ID;
      return checkNextFinal();
    }
  }

// ************* BANNER
  protected class BannerReader extends OrderHandler {
    public boolean read(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (isQuoted(t.getText()) == true) {
        retVal = readFinalString(t);
      } else {
        unexpected(t);
      }

      return retVal;
    }
  }

// ************* BEFÖRDERUNG
  protected class BefoerderungReader extends OrderHandler {
    public boolean read(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;

      return checkNextFinal();
    }
  }

// ************* BEKLAUE
  protected class BeklaueReader extends OrderHandler {
    public boolean read(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (isID(t.getText()) == true) {
        retVal = readBeklaueUID(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltBeklaue();
      }

      return retVal;
    }

    protected boolean readBeklaueUID(OrderToken token) {
      token.ttype = OrderToken.TT_ID;

      return checkNextFinal();
    }
  }

// ************* BELAGERE
  protected class BelagereReader extends OrderHandler {
    public boolean read(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (isID(t.getText()) == true) {
        retVal = readBelagereBID(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltBelagere();
      }

      return retVal;
    }

    protected boolean readBelagereBID(OrderToken token) {
      token.ttype = OrderToken.TT_ID;

      return checkNextFinal();
    }
  }

// ************* BENENNE
  protected class BenenneReader extends OrderHandler {
    public boolean read(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_CASTLE))) {
        retVal = readBenenneBeschreibeTarget(t);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_UNIT))) {
        retVal = readBenenneBeschreibeTarget(t);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_FACTION))) {
        retVal = readBenenneBeschreibeTarget(t);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_REGION))) {
        retVal = readBenenneBeschreibeTarget(t);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_SHIP))) {
        retVal = readBenenneBeschreibeTarget(t);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_FOREIGN))) {
        retVal = readBenenneFremdes(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltBenenne();
      }

      return retVal;
    }

    protected boolean readBenenneBeschreibeTarget(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (isString(t.getText()) == true) {
        retVal = readFinalString(t);
      } else {
        unexpected(t);
      }

      return retVal;
    }

    protected boolean readBenenneFremdes(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_UNIT))) {
        retVal = readBenenneFremdeEinheit(t);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_CASTLE))) {
        retVal = readBenenneFremdesGebaeude(t);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_FACTION))) {
        retVal = readBenenneFremdePartei(t);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_SHIP))) {
        retVal = readBenenneFremdesSchiff(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltBenenneFremdes();
      }

      return retVal;
    }

    protected boolean readBenenneFremdeEinheit(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (isID(t.getText())) {
        retVal = readBenenneFremdesTargetID(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltBenenneFremdeEinheit();
      }

      return retVal;
    }

    protected boolean readBenenneFremdesGebaeude(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (isID(t.getText())) {
        retVal = readBenenneFremdesTargetID(t);
      } else {
        unexpected(t);
      }
      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltBenenneFremdesGebaeude();
      }

      return retVal;
    }

    protected boolean readBenenneFremdePartei(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (isID(t.getText())) {
        retVal = readBenenneFremdesTargetID(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltBenenneFremdePartei();
      }

      return retVal;
    }

    protected boolean readBenenneFremdesSchiff(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (isID(t.getText())) {
        retVal = readBenenneFremdesTargetID(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltBenenneFremdesSchiff();
      }
      return retVal;
    }

    protected boolean readBenenneFremdesTargetID(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_ID;

      OrderToken t = getTokensIterator().next();

      if (isString(t.getText())) {
        retVal = readFinalString(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltBenenneFremdesTargetID();
      }
      return retVal;
    }
  }

// ************* BENUTZE
  protected class BenutzeReader extends OrderHandler {
    public boolean read(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (isString(t.getText()) == true) {
        retVal = readFinalString(t);
      } else if (isNumeric(t.getText())) {
        retVal = readBenutzeAmount(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltBenutze(0);
      }
      return retVal;
    }

    protected boolean readBenutzeAmount(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_NUMBER;

      // anzahl feststellen?
      int minAmount = 0;
      try {
        minAmount = Integer.parseInt(token.getText());
      } catch (NumberFormatException e) {
        // not parsable Number !?
      }

      OrderToken t = getTokensIterator().next();

      if (isString(t.getText()) == true) {
        retVal = readFinalString(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltBenutze(minAmount);
      }
      return retVal;
    }
  }

// ************* BESCHREIBE
  protected class BeschreibeReader extends OrderHandler {
    public boolean read(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_CASTLE))) {
        retVal = readBenenneBeschreibeTarget(t);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_UNIT))) {
        retVal = readBenenneBeschreibeTarget(t);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_PRIVATE))) {
        retVal = readBenenneBeschreibeTarget(t);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_REGION))) {
        retVal = readBenenneBeschreibeTarget(t);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_SHIP))) {
        retVal = readBenenneBeschreibeTarget(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltBeschreibe();
      }
      return retVal;
    }

    protected boolean readBenenneBeschreibeTarget(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (isString(t.getText()) == true) {
        retVal = readFinalString(t);
      } else {
        unexpected(t);
      }

      return retVal;
    }

  }

// ************* BETRETE
  protected class BetreteReader extends OrderHandler {
    public boolean read(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_CASTLE))) {
        retVal = readBetreteBurg(t);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_SHIP))) {
        retVal = readBetreteSchiff(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltBetrete();
      }
      return retVal;
    }

    protected boolean readBetreteBurg(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (isID(t.getText()) == true) {
        retVal = readBetreteBurgBID(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltBetreteBurg();
      }
      return retVal;
    }

    protected boolean readBetreteBurgBID(OrderToken token) {
      token.ttype = OrderToken.TT_ID;

      return checkNextFinal();
    }

    protected boolean readBetreteSchiff(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (isID(t.getText()) == true) {
        retVal = readBetreteSchiffSID(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltBetreteSchiff();
      }
      return retVal;
    }

    protected boolean readBetreteSchiffSID(OrderToken token) {
      token.ttype = OrderToken.TT_ID;

      return checkNextFinal();
    }
  }

// ************* BEWACHE
  protected class BewacheReader extends OrderHandler {
    public boolean read(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_NOT))) {
        retVal = readBewacheNicht(t);
      } else {
        retVal = checkFinal(t);
      }

      return retVal;
    }

    protected boolean readBewacheNicht(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;

      return checkNextFinal();
    }
  }

// ************* BOTSCHAFT
  protected class BotschaftReader extends OrderHandler {
    public boolean read(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      // FIX
      if (t.equalsToken("AN")) {
        retVal = read(t);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_UNIT))) {
        retVal = readBotschaftEinheit(t);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_FACTION))) {
        retVal = readBotschaftPartei(t);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_REGION))) {
        retVal = readBotschaftRegion(t);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_CASTLE))) {
        retVal = readBotschaftGebaeude(t);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_SHIP))) {
        retVal = readBotschaftSchiff(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltBotschaft();
      }
      return retVal;
    }

    protected boolean readBotschaftEinheit(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (isID(t.getText()) == true) {
        retVal = readBotschaftEinheitUID(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltBotschaftEinheit();
      }
      return retVal;
    }

    protected boolean readBotschaftEinheitUID(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_ID;

      OrderToken t = getTokensIterator().next();

      if (isQuoted(t.getText()) == true) {
        retVal = readFinalString(t);
      } else {
        unexpected(t);
      }

      return retVal;
    }

    protected boolean readBotschaftPartei(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (isID(t.getText()) == true) {
        retVal = readBotschaftParteiFID(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltBotschaftPartei();
      }
      return retVal;
    }

    protected boolean readBotschaftParteiFID(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_ID;

      OrderToken t = getTokensIterator().next();

      if (isQuoted(t.getText()) == true) {
        retVal = readFinalString(t);
      } else {
        unexpected(t);
      }

      return retVal;
    }

    protected boolean readBotschaftRegion(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (isString(t.getText()) == true) {
        retVal = readFinalString(t);
      } else {
        unexpected(t);
      }

      return retVal;
    }

    protected boolean readBotschaftGebaeude(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (isID(t.getText()) == true) {
        retVal = readBotschaftGebaeudeID(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltBotschaftGebaeude();
      }
      return retVal;
    }

    protected boolean readBotschaftGebaeudeID(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_ID;

      OrderToken t = getTokensIterator().next();

      if (isQuoted(t.getText()) == true) {
        retVal = readFinalString(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltBotschaftGebaeudeID();
      }
      return retVal;
    }

    protected boolean readBotschaftSchiff(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (isID(t.getText()) == true) {
        retVal = readBotschaftSchiffID(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltBotschaftSchiff();
      }
      return retVal;
    }

    protected boolean readBotschaftSchiffID(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_ID;

      OrderToken t = getTokensIterator().next();

      if (isQuoted(t.getText()) == true) {
        retVal = readFinalString(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltBotschaftSchiffID();
      }
      return retVal;
    }
  }

// ************* DEFAULT
  protected class DefaultReader extends OrderHandler {
    public boolean read(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      // the following can be string literal, a string literal within quotes or with an opening
      // quote
      // find out which type we have
      Type tokenType = Type.EMPTY;
      String innerText = "";

      char quote = 0;

      if (t.ttype == OrderToken.TT_EOC || t.getText().length() < 1) {
        // empty
        innerText = "";
        tokenType = Type.EMPTY;
      } else {
        if (t.getText().charAt(0) == '\'') {
          // opening single quote
          quote = '\'';
        } else if (t.getText().charAt(0) == '"') {
          // opening double quote
          quote = '"';
        } else {
          // no quote
          innerText = t.getText();
          tokenType = Type.EMPTY;
        }
      }
      if (quote != 0) {
        // text starts with quote
        if (t.getText().length() >= 2 && t.getText().charAt(t.getText().length() - 1) == quote) {
          innerText = t.getText().substring(1, t.getText().length() - 1);
          tokenType = Type.CLOSING;
        } else {
          innerText = t.getText().substring(1, t.getText().length());
          tokenType = Type.OPENING;
        }
      }

      // parse the string inside the quote(s)
      boolean retVal =
          EresseaOrderParser.this.read(new StringReader(innerText)) && innerText.length() != 0;

      if (tokenType == Type.CLOSING) {
        // return true iff the innerText is an nonempty order
        if (getCompleter() != null) {
          getCompleter().clear();
        }
        return retVal && innerText.length() != 0;
      }

      if (getCompleter() != null) {
        if (tokenType == Type.EMPTY) {
          // nothing
          getCompleter().cmplOpeningQuote(null, quote == 0 ? '\'' : quote);
        } else if (tokenType == Type.OPENING) {
          // quote with following text:
          OrderTokenizer tokenizer = new OrderTokenizer(new StringReader(innerText));
          OrderToken firstToken = tokenizer.getNextToken(), lastToken = firstToken;
          int tokenCount = 0;
          for (OrderToken currentToken = firstToken; currentToken.ttype != OrderToken.TT_EOC; tokenCount++) {
            lastToken = currentToken;
            currentToken = tokenizer.getNextToken();
          }
          // add opening and closing quotes to value as fit, but not to name
          // this way, the completion list is filtered correctly, later
          if (retVal) {
            getCompleter().cmplFinalQuote(lastToken, quote);
          }
          if (tokenCount == 1 && !lastToken.followedBySpace()) {
            getCompleter().cmplOpeningQuote(null, quote);
          }

        }
      }
      return false;
    }
  }

// ************* EMAIL
  protected class EmailReader extends OrderHandler {
    public boolean read(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (isEmailAddress(t.getText()) == true) {
        retVal = readEmailAddress(t);
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
    public boolean read(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;

      return checkNextFinal();
    }
  }

// ************* FAHRE
  protected class FahreReader extends OrderHandler {
    public boolean read(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (isID(t.getText()) == true) {
        retVal = readFahreUID(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltFahre();
      }
      return retVal;
    }

    protected boolean readFahreUID(OrderToken token) {
      token.ttype = OrderToken.TT_ID;

      return checkNextFinal();
    }
  }

// ************* FOLGE
  protected class FolgeReader extends OrderHandler {
    public boolean read(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_UNIT)) == true) {
        retVal = readFolgeEinheit(t);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_SHIP)) == true) {
        retVal = readFolgeSchiff(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltFolge();
      }
      return retVal;
    }

    protected boolean readFolgeEinheit(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (isID(t.getText()) == true) {
        retVal = readFinalID(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltFolgeEinheit();
      }
      return retVal;
    }

    protected boolean readFolgeSchiff(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (isID(t.getText()) == true) {
        retVal = readFinalID(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltFolgeSchiff();
      }
      return retVal;
    }
  }

// ************* BEANSPRUCHE (Fiete)
  protected class BeansprucheReader extends OrderHandler {
    public boolean read(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (isNumeric(t.getText())) {
        retVal = readBeansprucheAmount(t);
      } else if (isString(t.getText())) {
        retVal = readFinalString(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltBeanspruche();
      }
      return retVal;
    }

    protected boolean readBeansprucheAmount(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_NUMBER;

      OrderToken t = getTokensIterator().next();

      if (isString(t.getText())) {
        retVal = readFinalString(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltBeanspruche();
      }
      return retVal;
    }
  }

// ************* FORSCHE
  protected class ForscheReader extends OrderHandler {
    public boolean read(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_HERBS))) {
        retVal = readFinalKeyword(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltForsche();
      }
      return retVal;
    }
  }

// ************* GIB
  protected class GibReader extends OrderHandler {
    public boolean read(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (isID(t.getText()) == true) {
        retVal = readGibUID(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltGib();
      }

      return retVal;
    }

    protected boolean readGibUID(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_ID;

      UnitID uid = UnitID.createUnitID(token.getText(), getData().base);
      OrderToken t = getTokensIterator().next();

      if (isNumeric(t.getText()) == true) {
        retVal = readGibUIDAmount(t, uid, Integer.parseInt(t.getText()), true);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_EACH))) {
        retVal = readGibJe(t, uid);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_ALL))) {
        retVal = readGibUIDAlles(t);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_UNIT))
          || t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_CONTROL))
          || t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_HERBS))) {
        retVal = readFinalKeyword(t);
// } else if(isString(t.getText()) == true) {
// this is not allowed
// retVal = readFinalString(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltGibUID();
      }
      return retVal;
    }

    protected boolean readGibJe(OrderToken token, UnitID uid) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (isNumeric(t.getText()) == true) {
        retVal = readGibUIDAmount(t, uid, Integer.parseInt(t.getText()), false); // GIB JE PERSONS
        // is illegal
      } else
      // // GIVE bla JE ALL ... does not make sense
      // if(t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_ALL))) {
      // retVal = readGibUIDAlles(t);
      // } else
      if (isString(t.getText()) == true) {
        retVal = readFinalString(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltGibJe();
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

      OrderToken t = getTokensIterator().next();

      if (isString(t.getText()) == true) {
        retVal = readFinalString(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltGibUIDAmount(uid, i, persons);
      }
      return retVal;
    }

    protected boolean readGibUIDAlles(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (isString(t.getText()) == true) {
        retVal = readFinalString(t);
      } else {
        retVal = checkFinal(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltGibUIDAlles();
      }
      return retVal;
    }
  }

// ************* GRUPPE
  protected class GruppeReader extends OrderHandler {
    public boolean read(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (isString(t.getText()) == true) {
        retVal = readFinalString(t);
      } else {
        if (getCompleter() != null) {
          getCompleter().cmpltGruppe();
        }

        // just "GRUPPE" without explicit group is valid
        retVal = checkFinal(t);
// unexpected(t);
      }

      return retVal;
    }
  }

// ************* HELFE
  protected class HelfeReader extends OrderHandler {
    public boolean read(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (isID(t.getText()) == true) {
        retVal = readHelfeFID(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltHelfe();
      }
      return retVal;
    }

    protected boolean readHelfeFID(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_ID;

      OrderToken t = getTokensIterator().next();

      if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_ALL))
          || t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_HELP_GUARD))
          || t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_HELP_GIVE))
          || t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_HELP_COMBAT))
          || t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_HELP_SILVER))
          || t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_HELP_FACTIONSTEALTH))) {
        retVal = readHelfeFIDModifier(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltHelfeFID();
      }
      return retVal;
    }

    protected boolean readHelfeFIDModifier(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_NOT))) {
        retVal = readHelfeFIDModifierNicht(t, token.getText());
      } else {
        retVal = checkFinal(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
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
    public boolean read(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_COMBAT_AGGRESSIVE))) {
        retVal = readFinalKeyword(t);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_COMBAT_REAR))) {
        retVal = readFinalKeyword(t);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_COMBAT_DEFENSIVE))) {
        retVal = readFinalKeyword(t);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_COMBAT_NOT))) {
        retVal = readFinalKeyword(t);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_COMBAT_FLEE))) {
        retVal = readFinalKeyword(t);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_COMBAT_HELP))) {
        retVal = readKaempfeHelfe(t);
      } else {
        retVal = checkFinal(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltKaempfe();
      }
      return retVal;
    }

    protected boolean readKaempfeHelfe(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_NOT))) {
        retVal = readFinalKeyword(t);
      } else {
        retVal = checkFinal(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltKaempfeHelfe();
      }
      return retVal;
    }
  }

// ************* KAMPFZAUBER
  protected class KampfzauberReader extends OrderHandler {
    public boolean read(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_LEVEL))) {
        retVal = readKampfzauberStufe(t);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_NOT))) {
        retVal = readFinalKeyword(t);
      } else if (isString(t.getText())) {
        retVal = readKampfzauberSpell(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltKampfzauber();
      }
      return retVal;
    }

    protected boolean readKampfzauberStufe(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (isNumeric(t.getText())) {
        t.ttype = OrderToken.TT_NUMBER;
        t = getTokensIterator().next();

        if (isString(t.getText())) {
          retVal = readFinalString(t);
        } else {
          unexpected(t);
        }

        if (getCompleter() != null && !t.followedBySpace()) {
          getCompleter().cmpltKampfzauberStufe();
        }
      }

      return retVal;
    }

    protected boolean readKampfzauberSpell(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_STRING;

      OrderToken t = getTokensIterator().next();

      if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_NOT))) {
        retVal = readFinalKeyword(t);
      } else {
        retVal = checkFinal(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltKampfzauberSpell();
      }
      return retVal;
    }
  }

// ************* KAUFE
  protected class KaufeReader extends OrderHandler {
    public boolean read(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (isNumeric(t.getText()) == true) {
        retVal = readKaufeAmount(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltKaufe();
      }
      return retVal;
    }

    protected boolean readKaufeAmount(OrderToken token) {
      boolean retVal = false;
      ItemType type = null;
      ItemCategory luxuryCategory =
          (getData() != null) ? getData().rules.getItemCategory(EresseaConstants.C_LUXURIES) : null;
      token.ttype = OrderToken.TT_NUMBER;

      OrderToken t = getTokensIterator().next();

      // 
      if ((getData().rules != null) && ((type = getData().rules.getItemType(t.getText())) != null)
          && (luxuryCategory != null) && luxuryCategory.equals(type.getCategory())) {
        retVal = readFinalString(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltKaufeAmount();
      }
      return retVal;
    }
  }

// ************* KONTAKTIERE
  protected class KontaktiereReader extends OrderHandler {
    public boolean read(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (isID(t.getText()) == true) {
        retVal = readKontaktiereUID(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
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
    public boolean read(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (isID(t.getText()) == true) {
        retVal = readLehreUID(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltLehre();
      }
      return retVal;
    }

    protected boolean readLehreUID(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_ID;

      OrderToken t = getTokensIterator().next();

      if (isID(t.getText()) == true) {
        retVal = readLehreUID(t);
      } else {
        retVal = checkFinal(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltLehre();
      }
      return retVal;
    }
  }

// ************* LERNE
  protected class LerneReader extends OrderHandler {
    public boolean read(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      // detect quoted strings
      SkillType skill = getData().rules.getSkillType(t.getStrippedText(EresseaOrderParser.QUOTES));
      if ((getData().rules != null) && (skill != null)) {
        retVal = readLerneTalent(t);
      } else {
        retVal = checkFinal(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltLerne();
      }
      return retVal;
    }

    protected boolean readLerneTalent(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_STRING;
      SkillType skill =
          getData().rules.getSkillType(token.getStrippedText(EresseaOrderParser.QUOTES));

      OrderToken t = getTokensIterator().next();

      if (isNumeric(t.getText()) == true) {
        retVal = readFinalNumber(t);
      } else if (t.ttype != OrderToken.TT_EOC
          && skill.equals(getData().rules.getSkillType(EresseaConstants.S_MAGIE))) {
        retVal = readFinalString(t);
      } else {
        retVal = checkFinal(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltLerneTalent(skill);
      }
      return retVal;
    }
  }

// ************* LOCALE
  protected class LocaleReader extends OrderHandler {
    public boolean read(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (isString(t.getText())) {
        retVal = readFinalString(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltLocale();
      }
      return retVal;
    }
  }

// ************* MACHE
  protected class MacheReader extends OrderHandler {
    public boolean read(OrderToken token) {
      boolean retVal = false;
      BuildingType type = null;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (isNumeric(t.getText()) == true) {
        retVal = readMacheAmount(t);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_TEMP))) {
        retVal = readMacheTemp(t);
      } else if (isTempID(t.getText()) == true) {
        retVal = readMacheTempID(t);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_CASTLE))) {
        retVal = readMacheBurg(t);
      } else if ((getData().rules != null)
          && ((type = getData().rules.getBuildingType(t.getText())) != null)
          && (!(type instanceof CastleType) || t.equalsToken(Resources
              .getOrderTranslation(EresseaConstants.O_CASTLE)))) {
        retVal = readMacheBuilding(t);
      } else if ((getData().rules != null) && (getData().rules.getShipType(t.getText()) != null)) {
        retVal = readMacheShip(t);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_SHIP))) {
        retVal = readMacheSchiff(t);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_ROAD))) {
        retVal = readMacheStrasse(t);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_SEED))) {
        retVal = readFinalKeyword(t);
      } else if (t.ttype == OrderToken.TT_EOC) {
        retVal = false;
      } else {
        retVal = readMacheAnything(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltMache();
      }

      return retVal;
    }

    protected boolean readMacheAmount(OrderToken token) {
      boolean retVal = false;
      BuildingType type = null;
      token.ttype = OrderToken.TT_NUMBER;

      OrderToken t = getTokensIterator().next();

      if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_CASTLE))) {
        retVal = readMacheBurg(t);
      } else if ((getData().rules != null)
          && ((type = getData().rules.getBuildingType(t.getText())) != null)
          && !(type instanceof CastleType)) {
        retVal = readMacheBuilding(t);
      } else if ((getData().rules != null) && (getData().rules.getShipType(t.getText()) != null)) {
        retVal = readMacheShip(t);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_SHIP))) {
        retVal = readMacheSchiff(t);
      } else {
        retVal = readMacheAnything(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltMacheAmount();
      }
      return retVal;
    }

    protected boolean readMacheTemp(OrderToken token) {
      token.ttype = OrderToken.TT_ID;

      OrderToken t = getTokensIterator().next();

      if (getCompleter() != null) {
        getCompleter().cmpltMacheTemp();
      }

      unexpected(t);

      return false; // there can't follow an id, else it would have been merged with TEMP
    }

    protected boolean readMacheTempID(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_ID;

      OrderToken t = getTokensIterator().next();

      if (isString(t.getText())) {
        retVal = readFinalString(t);
      } else {
        retVal = checkFinal(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltMacheTempID();
      }
      return retVal;
    }

    protected boolean readMacheBurg(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (isID(t.getText())) {
        retVal = readFinalID(t);
      } else {
        retVal = checkFinal(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltMacheBurg();
      }
      return retVal;
    }

    protected boolean readMacheBuilding(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_STRING;

      OrderToken t = getTokensIterator().next();

      if (isID(t.getText()) == true) {
        retVal = readFinalID(t);
      } else {
        retVal = checkFinal(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
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

      OrderToken t = getTokensIterator().next();

      if (isID(t.getText()) == true) {
        retVal = readFinalID(t);
      } else {
        retVal = checkFinal(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltMacheSchiff();
      }
      return retVal;
    }

    protected boolean readMacheStrasse(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_STRING;

      OrderToken t = getTokensIterator().next();

      if (Direction.toInt(t.getText()) != Direction.DIR_INVALID) {
        retVal = readFinalString(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltMacheStrasse();
      }
      return retVal;
    }

    protected boolean readMacheAnything(OrderToken token) {
      boolean retVal = true;

      if ((token.ttype != OrderToken.TT_EOC) && (token.ttype != OrderToken.TT_COMMENT)) {
        token.ttype = OrderToken.TT_STRING;
        retVal = checkNextFinal();
      }

      return retVal;
    }
  }

// ************* NACH
  protected class NachReader extends OrderHandler {
    public boolean read(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (Direction.toInt(t.getText()) != Direction.DIR_INVALID) {
        retVal = readNachDirection(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltNach();
      }
      return retVal;
    }

    protected boolean readNachDirection(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (Direction.toInt(t.getText()) != Direction.DIR_INVALID) {
        retVal = readNachDirection(t);
      } else {
        retVal = checkFinal(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltNach();
      }
      return retVal;
    }
  }

// ************* NUMMER
  protected class NummerReader extends OrderHandler {
    public boolean read(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_UNIT)) == true) {
        retVal = readNummerEinheit(t);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_SHIP)) == true) {
        retVal = readNummerSchiff(t);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_FACTION)) == true) {
        retVal = readNummerPartei(t);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_CASTLE)) == true) {
        retVal = readNummerBurg(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltNummer();
      }
      return retVal;
    }

    protected boolean readNummerEinheit(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (isID(t.getText()) == true) {
        retVal = readFinalID(t);
      } else if (t.ttype == OrderToken.TT_EOC) {
        retVal = true;
      } else {
        unexpected(t);
      }

      return retVal;
    }

    protected boolean readNummerPartei(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (isID(t.getText()) == true) {
        retVal = readFinalID(t);
      } else if (t.ttype == OrderToken.TT_EOC) {
        retVal = true;
      } else {
        unexpected(t);
      }

      return retVal;
    }

    protected boolean readNummerSchiff(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (isID(t.getText()) == true) {
        retVal = readFinalID(t);
      } else if (t.ttype == OrderToken.TT_EOC) {
        retVal = true;
      } else {
        unexpected(t);
      }

      return retVal;
    }

    protected boolean readNummerBurg(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (isID(t.getText()) == true) {
        retVal = readFinalID(t);
      } else if (t.ttype == OrderToken.TT_EOC) {
        retVal = true;
      } else {
        unexpected(t);
      }

      return retVal;
    }
  }

// ************* OPTION
  protected class OptionReader extends OrderHandler {
    public boolean read(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_ADDRESSES))
          || t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_REPORT))
          || t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_BZIP2))
          || t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_COMPUTER))
          || t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_ITEMPOOL))
          || t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_SILVERPOOL))
          || t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_STATISTICS))
          || t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_ZIPPED))
          || t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_SCORE))
          || t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_TEMPLATE))) {
        retVal = readOptionOption(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltOption();
      }
      return retVal;
    }

    protected boolean readOptionOption(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_NOT))) {
        retVal = readFinalKeyword(t);
      } else {
        retVal = checkFinal(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltOptionOption();
      }
      return retVal;
    }
  }

// ************* PARTEI
  protected class ParteiReader extends OrderHandler {
    public boolean read(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (isID(t.getText()) == true) {
        retVal = readParteiFID(t);
      } else {
        unexpected(t);
      }

      return retVal;
    }

    protected boolean readParteiFID(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_ID;

      OrderToken t = getTokensIterator().next();

      if (isQuoted(t.getText()) == true) {
        retVal = readFinalString(t);
      } else {
        unexpected(t);
      }

      return retVal;
    }
  }

// ************* PASSWORT
  protected class PasswortReader extends OrderHandler {
    public boolean read(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (t.ttype == OrderToken.TT_EOC) {
        retVal = true;
      } else if (isQuoted(t.getText()) == true) {
        retVal = readFinalString(t);
      } else {
        unexpected(t);
      }

      return retVal;
    }
  }

// ************* PFLANZEN
  protected class PflanzeReader extends OrderHandler {
    public boolean read(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (isNumeric(t.getText()) == true) {
        retVal = readPflanzeAmount(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
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

      OrderToken t = getTokensIterator().next();

      if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_HERBS))
          || t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_SEED))
          || t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_MALLORNSEED))
          || t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_TREES))) {
        t.ttype = OrderToken.TT_KEYWORD;
        retVal = checkNextFinal();
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltPflanze(minAmount);
      }
      return retVal;
    }

    protected boolean readPflanzeWas(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      // might want to treat these not as keywords but as items...?
      if (false) {
        retVal = checkFinal(t);
      } else {
        unexpected(t);
      }

      return retVal;
    }
  }

// ************* PIRATERIE
  protected class PiraterieReader extends OrderHandler {
    public boolean read(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (isID(t.getText()) == true) {
        retVal = readPiraterieFID(t);
      } else {
        retVal = checkFinal(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltPiraterie();
      }
      return retVal;
    }

    protected boolean readPiraterieFID(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_ID;

      OrderToken t = getTokensIterator().next();

      if (isID(t.getText()) == true) {
        retVal = readPiraterieFID(t);
      } else {
        retVal = checkFinal(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltPiraterieFID();
      }
      return retVal;
    }
  }

// ************* PRAEFIX
  protected class PraefixReader extends OrderHandler {
    public boolean read(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (isQuoted(t.getText()) == true) {
        retVal = readFinalString(t);
      } else {
        retVal = checkFinal(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltPraefix();
      }
      return retVal;
    }
  }

// ************* REGION
  protected class RegionReader extends OrderHandler {
    public boolean read(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

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
    public boolean read(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (isNumeric(t.getText()) == true) {
        retVal = readFinalNumber(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltRekrutiere();
      }

      return retVal;
    }
  }

// ************* RESERVIERE
  protected class ReserviereReader extends OrderHandler {
    public boolean read(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();
      if (isNumeric(t.getText()) == true) {
        retVal = readReserviereAmount(t);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_EACH))) {
        retVal = readReserviereJe(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltReserviere();
      }
      return retVal;
    }

    protected boolean readReserviereJe(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (isNumeric(t.getText()) == true) {
        retVal = readReserviereAmount(t);
      } else {
        retVal = checkFinal(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltReserviereJe();
      }
      return retVal;

    }

    protected boolean readReserviereAmount(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_NUMBER;

      OrderToken t = getTokensIterator().next();

      if (isString(t.getText()) == true) {
        retVal = readFinalString(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltReserviereAmount();
      }
      return retVal;
    }
  }

// ************* ROUTE
  protected class RouteReader extends OrderHandler {
    public boolean read(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (Direction.toInt(t.getText()) != Direction.DIR_INVALID) {
        retVal = readRouteDirection(t);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_PAUSE))) {
        retVal = readRouteDirection(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltRoute();
      }
      return retVal;
    }

    protected boolean readRouteDirection(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (Direction.toInt(t.getText()) != Direction.DIR_INVALID) {
        retVal = readRouteDirection(t);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_PAUSE))) {
        retVal = readRouteDirection(t);
      } else {
        retVal = checkFinal(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltRoute();
      }
      return retVal;
    }
  }

// ************* SABOTIERE
  protected class SabotiereReader extends OrderHandler {
    public boolean read(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_SHIP))) {
        retVal = readFinalKeyword(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltSabotiere();
      }
      return retVal;
    }
  }

// ************* SORTIERE
  protected class SortiereReader extends OrderHandler {
    public boolean read(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      // FIX
      if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_BEFORE))) {
        retVal = readSortiereVor(t);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_AFTER))) {
        retVal = readSortiereHinter(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltSortiere();
      }
      return retVal;
    }

    protected boolean readSortiereVor(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (isID(t.getText())) {
        retVal = readFinalID(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltSortiereVor();
      }
      return retVal;
    }

    protected boolean readSortiereHinter(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (isID(t.getText())) {
        retVal = readFinalID(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltSortiereHinter();
      }
      return retVal;
    }
  }

// ************* SPIONIERE
  protected class SpioniereReader extends OrderHandler {
    public boolean read(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (isID(t.getText()) == true) {
        retVal = readFinalID(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltSpioniere();
      }
      return retVal;
    }
  }

// ************* STIRB
  protected class StirbReader extends OrderHandler {
    public boolean read(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (isQuoted(t.getText()) == true) {
        retVal = readFinalString(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltStirb();
      }
      return retVal;
    }
  }

// ************* TARNE
  protected class TarneReader extends OrderHandler {
    public boolean read(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (isNumeric(t.getText()) == true) {
        retVal = readFinalNumber(t);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_FACTION))) {
        retVal = readTarnePartei(t);
      } else if ((getData().rules != null) && (getData().rules.getRace(t.getText()) != null)) {
        retVal = readFinalString(t);
      } else {
        retVal = checkFinal(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltTarne();
      }
      return retVal;
    }

    protected boolean readTarnePartei(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_NOT))) {
        retVal = readFinalKeyword(t);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_NUMBER))) {
        retVal = readTarneParteiNummer(t);
      } else {
        retVal = checkFinal(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltTarnePartei();
      }
      return retVal;
    }

    protected boolean readTarneParteiNummer(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (isID(t.getText())) {
        retVal = readFinalID(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltTarneParteiNummer();
      }
      return retVal;
    }
  }

// ************* TRANSPORTIERE
  protected class TransportiereReader extends OrderHandler {
    public boolean read(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (isID(t.getText()) == true) {
        retVal = readFinalID(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltTransportiere();
      }
      return retVal;
    }
  }

// ************* TREIBE
  protected class TreibeReader extends OrderHandler {
    public boolean read(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

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
    public boolean read(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

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
    public boolean read(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

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

      OrderToken t = getTokensIterator().next();

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
    public boolean read(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if ((getData().rules != null) && (getData().rules.getSkillType(t.getText()) != null)) {
        retVal = readFinalString(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltVergesse();
      }
      return retVal;
    }
  }

// ************* VERKAUFE
  protected class VerkaufeReader extends OrderHandler {
    public boolean read(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (isNumeric(t.getText()) == true) {
        retVal = readVerkaufeAmount(t);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_ALL))) {
        retVal = readVerkaufeAlles(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltVerkaufe();
      }
      return retVal;
    }

    protected boolean readVerkaufeAmount(OrderToken token) {
      boolean retVal = false;
      ItemType type = null;
      ItemCategory luxuryCategory =
          (getData().rules != null) ? getData().rules.getItemCategory(EresseaConstants.C_LUXURIES)
              : null;
      token.ttype = OrderToken.TT_NUMBER;

      OrderToken t = getTokensIterator().next();

      if ((getData().rules != null) && ((type = getData().rules.getItemType(t.getText())) != null)
          && (luxuryCategory != null) && type.getCategory().equals(luxuryCategory)) {
        retVal = readFinalString(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltVerkaufeAmount();
      }
      return retVal;
    }

    protected boolean readVerkaufeAlles(OrderToken token) {
      boolean retVal = false;
      ItemType type = null;
      ItemCategory luxuryCategory =
          (getData().rules != null) ? getData().rules.getItemCategory(EresseaConstants.C_LUXURIES)
              : null;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if ((getData().rules != null) && ((type = getData().rules.getItemType(t.getText())) != null)
          && (type != null) && (luxuryCategory != null)
          && luxuryCategory.equals(type.getCategory())) {
        retVal = readFinalString(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltVerkaufeAlles();
      }
      return retVal;
    }
  }

// ************* ZAUBERE
  protected class ZaubereReader extends OrderHandler {
    public boolean read(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_REGION))) {
        retVal = readZaubereRegion(t);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_LEVEL))) {
        retVal = readZaubereStufe(t);
      } else if (isString(t.getText())) {
// Spell s = data.getSpell(t.getText());
// if(s != null) {
// retVal = readZaubereSpruch(t, s);
// } else {
        retVal = readZaubereSpruch(t);
// }
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltZaubere();
      }
      return retVal;
    }

    protected boolean readZaubereRegion(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (isNumeric(t.getText(), 10, Integer.MIN_VALUE, Integer.MAX_VALUE)) {
        retVal = readZaubereRegionCoor(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltZaubereRegion();
      }
      return retVal;
    }

    protected boolean readZaubereRegionCoor(OrderToken token) {
      boolean retVal = false;

      // x-coordinate
      token.ttype = OrderToken.TT_NUMBER;

      OrderToken t = getTokensIterator().next();

      if (isNumeric(t.getText(), 10, Integer.MIN_VALUE, Integer.MAX_VALUE)) {
        // y-coordinate
        t.ttype = OrderToken.TT_NUMBER;
        t = getTokensIterator().next();

        if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_LEVEL))) {
          retVal = readZaubereRegionStufe(t);
        } else if (isString(t.getText())) {
          retVal = readZaubereSpruch(t);
        } else {
          unexpected(t);
        }

        if (getCompleter() != null && !t.followedBySpace()) {
          getCompleter().cmpltZaubereRegionCoor();
        }
      }

      return retVal;
    }

    protected boolean readZaubereStufe(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (isNumeric(t.getText())) {
        t.ttype = OrderToken.TT_NUMBER;
        t = getTokensIterator().next();

        if (isString(t.getText())) {
          retVal = readZaubereSpruch(t);
        } else {
          unexpected(t);
        }

        if (getCompleter() != null && !t.followedBySpace()) {
          getCompleter().cmpltZaubereStufe();
        }
      }

      return retVal;
    }

    protected boolean readZaubereRegionStufe(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (isNumeric(t.getText())) {
        t.ttype = OrderToken.TT_NUMBER;
        t = getTokensIterator().next();

        if (isString(t.getText())) {
          retVal = readZaubereSpruch(t);
        } else {
          unexpected(t);
        }

        if (getCompleter() != null && !t.followedBySpace()) {
          getCompleter().cmpltZaubereRegionStufe();
        }
      }

      return retVal;
    }

    protected boolean readZaubereSpruch(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_STRING;
      // checken, ob der Zauberspruch bekannt ist
      // Problem: keine Referenz auf die Unit, wir können nicht die spells der unit durchgehen
      // wir müssen spells der GameData durchgehen
      if (getData().spells() == null || getData().spells().size() == 0) {
        return false;
      }
      for (Spell s : getData().spells().values()) {
        String test =
            token.getText().replaceAll("\"", "").replaceAll("~", " ").replaceAll("\'", "");
        if (test.equalsIgnoreCase(s.getName())) {
          // here we return just true
          // toDo: get Spell Syntax, check, if more tokens expected and
          // do next checks
          skipRestOfOrder();
          return true;
        }
      }
      return retVal;
    }

    /**
     * skips rest of Line
     */
    protected void skipRestOfOrder() {
      if (!getTokensIterator().hasNext())
        return;
      OrderToken t = getTokensIterator().next();
      while ((t.ttype != OrderToken.TT_EOC) && (t.ttype != OrderToken.TT_COMMENT)
          && getTokensIterator().hasNext()) {
        t = getTokensIterator().next();
      }
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
// if(token.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_CASTLE))) {
// token.ttype = OrderToken.TT_KEYWORD;
// token = tokens.next();
// token.ttype = OrderToken.TT_ID;
//          
// } else if(token.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_UNIT))) {
//          
// } else if(token.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_FACTION))) {
//          
// } else if(token.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_REGION))) {
//          
// } else if(token.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_SHIP))) {
//          
// }
// }
// }
// return false;
// }}

  }

// ************* ZEIGE
  protected class ZeigeReader extends OrderHandler {
    public boolean read(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_ALL))) {
        retVal = readZeigeAlle(t);
      } else if (isString(t.getText()) == true) {
        retVal = readFinalString(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltZeige();
      }
      return retVal;
    }

    protected boolean readZeigeAlle(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_POTIONS))
          || t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_SPELLS))) {
        retVal = readFinalKeyword(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltZeigeAlle();
      }
      return retVal;
    }
  }

// ************* ZERSTOERE
  protected class ZerstoereReader extends OrderHandler {
    public boolean read(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (isNumeric(t.getText())) {
        retVal = readZerstoereAmount(t);
      } else if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_ROAD))) {
        retVal = readZerstoereStrasse(t);
      } else {
        retVal = checkFinal(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltZerstoere();
      }
      return retVal;
    }

    protected boolean readZerstoereAmount(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_NUMBER;

      OrderToken t = getTokensIterator().next();

      if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_ROAD))) {
        retVal = readZerstoereStrasse(t);
      } else {
        retVal = checkFinal(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltZerstoere();
      }
      return retVal;
    }

    protected boolean readZerstoereStrasse(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_STRING;

      OrderToken t = getTokensIterator().next();

      if (Direction.toInt(t.getText()) != Direction.DIR_INVALID) {
        retVal = readFinalString(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltZerstoereStrasse();
      }
      return retVal;
    }
  }

// ************* ZUECHTE
  protected class ZuechteReader extends OrderHandler {
    public boolean read(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getTokensIterator().next();

      if (t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_HERBS))
          || t.equalsToken(Resources.getOrderTranslation(EresseaConstants.O_HORSES))) {
        retVal = readFinalKeyword(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltZuechte();
      }
      return retVal;
    }
  }

  protected class FinalKeywordReader extends OrderHandler {
    public boolean read(OrderToken token) {
      return readFinalKeyword(token);
    }
  }

  // ************* general use
  protected boolean readFinalKeyword(OrderToken token) {
    token.ttype = OrderToken.TT_KEYWORD;

    return checkNextFinal();
  }

  protected boolean readFinalString(OrderToken token) {
    token.ttype = OrderToken.TT_STRING;

    return checkNextFinal();
  }

  protected boolean readFinalID(OrderToken token) {
    token.ttype = OrderToken.TT_ID;

    return checkNextFinal();
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
    if (getTokensIterator().hasNext()) {
      OrderToken t = getTokensIterator().next();

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
    boolean retVal = ((t.ttype == OrderToken.TT_EOC) || (t.ttype == OrderToken.TT_COMMENT));

    if (retVal == false) {
      unexpected(t);
    }

    return retVal;
  }

  protected void unexpected(OrderToken t) {
    setErrMsg("Unexpected token " + t.toString());
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

  protected boolean isNumeric(String txt) {
    return isNumeric(txt, 10, 0, Integer.MAX_VALUE);
  }

  /**
   * Tests if <code>txt</code> represents a valid ID (or TEMP ID) given the <code>data.base</code>
   * and {@link #MAX_UID}.
   * 
   * @param txt
   * @return
   */
  protected boolean isID(String txt) {
    boolean retVal = isNumeric(txt, getData().base, 0, EresseaOrderParser.MAX_UID);

    if (retVal == false) {
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
      retVal = (temp.equalsIgnoreCase("TEMP"));
      retVal = retVal && isNumeric(nr, getData().base, 0, EresseaOrderParser.MAX_UID);
    }

    return retVal;
  }

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
          EresseaOrderParser.log.warn("OrderEditor.getColor()", e);
        }
      } else {
        try {
          Integer.parseInt(txt.substring(0, firstCommaPos));
          Integer.parseInt(txt.substring(firstCommaPos + 1, txt.length()));
          retVal = true;
        } catch (NumberFormatException e) {
          EresseaOrderParser.log.warn("OrderEditor.getColor()", e);
        }
      }
    }

    return retVal;
  }

  /**
   * Tests if <code>txt</code> is surrounded by double quotes.
   * 
   * @param txt
   * @return
   */
  protected boolean isQuoted(String txt) {
    return (txt.startsWith("\"") && txt.endsWith("\"") && txt.length() >= 2);
  }

  /**
   * Tests if <code>txt</code> is surrounded by single quotes.
   * 
   * @param txt
   * @return
   */
  protected boolean isSingleQuoted(String txt) {
    return (txt.startsWith("\'") && txt.endsWith("\'") && txt.length() >= 2);
  }

  /**
   * Tests if <code>txt</code> is a nonempty string which is either surrounded by quotes or by
   * double quotes, or is composed solely by the characters [A-Za-zÄÖÜäöüß~,._:].
   * 
   * @param txt
   * @return
   */
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
        if (!(((c >= 'A') && (c <= 'Z')) || ((c >= 'a') && (c <= 'z')) || (c == 'Ä') || (c == 'Ö')
            || (c == 'Ü') || (c == 'ä') || (c == 'ö') || (c == 'ü') || (c == '~') || (c == 'ß')
            || (c == ',') || (c == '.') || (c == '_') || (c == ':') || ((!isNumeric) && (c >= '0') && (c <= '9')))) {
          retVal = false;
          break;
        }
      }
    }

    if (retVal != Pattern.matches(
        "(\".*\")|(\'.*\')|([A-Za-zÄÖÜäöüß~,._:][A-Za-zÄÖÜäöüß~,._:0-9]*)", txt))
      throw new AssertionError("isString \"" + txt + "\"");

    return retVal;
  }

  /**
   * Tests if <code>txt</code> is (syntactically) a valid email address.
   * 
   * @param txt
   * @return
   */
  protected boolean isEmailAddress(String txt) {
    boolean retVal = true;
    int atIndex = txt.indexOf("@");

    if ((atIndex > -1) && (atIndex == txt.lastIndexOf("@"))) {
      for (int i = 0; i < txt.length(); i++) {
        char c = txt.charAt(i);

        if (!(((c >= '0') && (c <= '9')) || ((c >= 'A') && (c <= 'Z'))
            || ((c >= 'a') && (c <= 'z')) || (c == '-') || (c == '_') || (c == '.') || (c == '@'))) {
          retVal = false;

          break;
        }
      }
    } else {
      retVal = false;
    }

    return retVal;
  }

}

/**
 * A class for collecting and preprocessing order tokens
 */
class TokenBucket extends Vector<OrderToken> {
  private static final int MAX_TEMP_NR = 1679615; // = (36 ^ 4) - 1;

  /**
   * Creates a new TokenBucket object.
   */
  public TokenBucket() {
  }

  /**
   * Creates a new TokenBucket object.
   */
  public TokenBucket(Reader in) {
    read(in);
  }

  /**
   * @see java.util.Vector#add(java.lang.Object)
   */
  @Override
  public boolean add(OrderToken o) {
    super.add(o);

    return true;
  }

  /**
   * Reads the tokens from <code>in</code> with an {@link OrderTokenizer} and adds them to
   * <code>this</code>.
   * 
   * @param in
   * @return
   */
  public int read(Reader in) {
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

        if (tempText.equalsIgnoreCase("TEMP")) {
          try {
            OrderToken nrToken = tokenAt(i + 1);
            String nrText = nrToken.getText();
            int nr = IDBaseConverter.parse(nrText, base);

            if ((nr >= 0) && (nr <= TokenBucket.MAX_TEMP_NR)) {
              tempToken.setText("TEMP " + nrText);

              if ((tempToken.getEnd() > -1) && (nrToken.getEnd() > -1)) {
                tempToken.setEnd(nrToken.getEnd());
              }

              remove(i + 1);
            }
          } catch (NumberFormatException e) {
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
