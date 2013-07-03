/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.library.gamebinding.e3a;

import java.util.Arrays;
import java.util.Collection;

import magellan.library.EntityID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.completion.OrderParser;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.gamebinding.EresseaOrderParser;
import magellan.library.gamebinding.OrderHandler;
import magellan.library.gamebinding.RenameOrder.RenameObject;
import magellan.library.rules.BuildingType;
import magellan.library.rules.Race;
import magellan.library.utils.OrderToken;

/**
 */
public class E3AOrderParser extends EresseaOrderParser {

  private E3AOrderCompleter completer;

  /**
   * Creates a new <tt>EresseaOrderParser</tt> object.
   */
  public E3AOrderParser(GameData data) {
    super(data, null);
  }

  /**
   * Creates a new <tt>EresseaOrderParser</tt> object and registers the specified
   * <tt>OrderCompleter</tt> object. This constructor should be used only by the
   * <tt>OrderCompleter</tt> class itself.
   */
  public E3AOrderParser(GameData data, E3AOrderCompleter cc) {
    super(data, cc);
    completer = cc;
  }

  @Override
  protected void initCommands() {
    super.initCommands();

    removeCommand(EresseaConstants.O_HELP);

    removeCommand(EresseaConstants.O_RESEARCH);
    // removeCommand(EresseaConstants.O_FACTION);
    // removeCommand(EresseaConstants.O_REGION);
    // removeCommand(EresseaConstants.O_GROW);

    removeCommand(EresseaConstants.O_SPY);

    removeCommand(EresseaConstants.O_STEAL);

    // removeCommand(EresseaConstants.O_WORK);
    removeCommand(EresseaConstants.O_TAX);
    removeCommand(EresseaConstants.O_ENTERTAIN);

    removeCommand(EresseaConstants.O_BUY); // ?
    removeCommand(EresseaConstants.O_SELL);

    removeCommand(EresseaConstants.O_TEACH);

    removeCommand(EresseaConstants.O_SUPPLY);

    removeCommand(EresseaConstants.O_SABOTAGE);

    // removeCommand(EresseaConstants.O_SIEGE);

    addCommand(E3AConstants.O_ALLIANCE, new AllianzReader(this));
    addCommand(EresseaConstants.O_HELP, new HelfeReader(this));
    addCommand(E3AConstants.O_PAY, new BezahleReader(this));
    // addCommand(E3AConstants.O_GIVE, new GibReader(this));
    addCommand(EresseaConstants.O_MAKE, new E3MacheReader(this));
    addCommand(EresseaConstants.O_RECRUIT, new RekrutiereReader(this));

    // only TARNE PARTEI!
    removeCommand(EresseaConstants.O_HIDE);
    addCommand(EresseaConstants.O_HIDE, new TarneReader(this));
  }

  @Override
  public E3AOrderCompleter getCompleter() {
    return completer;
  }

  // ************* ALLIANZ
  protected class AllianzReader extends OrderHandler {
    AllianzReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isEoC(t)) {
        unexpected(t);
      } else if (t.equalsToken(getOrderTranslation(E3AConstants.O_ALLIANCE_KICK))) {
        retVal = readAllianzFactionID(t);
      } else if (t.equalsToken(getOrderTranslation(E3AConstants.O_ALLIANCE_LEAVE))) {
        t.ttype = OrderToken.TT_KEYWORD;
        retVal = checkNextFinal();
      } else if (t.equalsToken(getOrderTranslation(E3AConstants.O_ALLIANCE_COMMAND))) {
        retVal = readAllianzFactionID(t);
      } else if (t.equalsToken(getOrderTranslation(E3AConstants.O_ALLIANCE_NEW))) {
        t.ttype = OrderToken.TT_KEYWORD;
        retVal = checkNextFinal();
      } else if (t.equalsToken(getOrderTranslation(E3AConstants.O_ALLIANCE_INVITE))) {
        retVal = readAllianzFactionID(t);
      } else if (t.equalsToken(getOrderTranslation(E3AConstants.O_ALLIANCE_JOIN))) {
        retVal = readAllianzBeitreten(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltAllianz();
      }

      return retVal;
    }

    protected boolean readAllianzBeitreten(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isID(t.getText(), false))
        return readFinalID(t);
      else {
        unexpected(t);
      }

      return false;
    }

    protected boolean readAllianzFactionID(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isID(t.getText(), false)) {
        t.ttype = OrderToken.TT_ID;
        Faction faction =
            getData().getFaction(EntityID.createEntityID(t.getText(), getData().base));
        retVal = faction != null && checkNextFinal();
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltFactions("");
      }

      return retVal;
    }
  }

  protected class HelfeReader extends EresseaOrderParser.HelfeReader {
    public HelfeReader(OrderParser parser) {
      super(parser);
    }

    private Collection<String> categories;

    /**
     * Returns all categories except KÄMPFE, which is implicit in E3 and cannot be set with the
     * HELFE order, and PARTEITARNUNG.
     * 
     * @see magellan.library.gamebinding.EresseaOrderParser.HelfeReader#getCategories()
     */
    @Override
    protected Collection<String> getCategories() {
      if (categories == null) {
        categories =
            Arrays.asList(EresseaConstants.O_ALL,
                // EresseaConstants.O_HELP_COMBAT,
                EresseaConstants.O_HELP_GIVE, EresseaConstants.O_HELP_GUARD,
                EresseaConstants.O_HELP_SILVER
            // , EresseaConstants.O_HELP_FACTIONSTEALTH
                );
      }
      return categories;
    }
  }

  // ************* BENENNE
  protected class BenenneReader extends EresseaOrderParser.BenenneReader {

    public BenenneReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();
      t.ttype = OrderToken.TT_KEYWORD;
      if (t.equalsToken(getOrderTranslation(E3AConstants.O_ALLIANCE))) {
        getOrder().type = RenameObject.T_ALLIANCE;
        getOrder().name = readDescription(false);
        retVal = readDescription(false) != null;
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_CASTLE))) {
        getOrder().type = RenameObject.T_BUILDING;
        getOrder().name = readDescription(false);
        retVal = readDescription(false) != null;
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_UNIT))) {
        getOrder().type = RenameObject.T_UNIT;
        getOrder().name = readDescription(false);
        retVal = readDescription(false) != null;
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_FACTION))) {
        getOrder().type = RenameObject.T_FACTION;
        getOrder().name = readDescription(false);
        retVal = readDescription(false) != null;
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_REGION))) {
        getOrder().type = RenameObject.T_REGION;
        getOrder().name = readDescription(false);
        retVal = readDescription(false) != null;
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_SHIP))) {
        getOrder().type = RenameObject.T_SHIP;
        getOrder().name = readDescription(false);
        retVal = readDescription(false) != null;
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_FOREIGNBUILDING))) {
        retVal = readBenenneFremdes(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_FOREIGNFACTION))) {
        retVal = readBenenneFremdes(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_FOREIGNSHIP))) {
        retVal = readBenenneFremdes(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.O_FOREIGNUNIT))) {
        retVal = readBenenneFremdes(t);
      } else {
        t.ttype = OrderToken.TT_UNDEF;
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltBenenne();
      }

      return retVal;
    }
  }

  // ************* BEZAHLE
  protected class BezahleReader extends OrderHandler {

    BezahleReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected void init(OrderToken token, String text) {
      order = new MaintainOrder(getTokens(), text);
    }

    @Override
    public MaintainOrder getOrder() {
      MaintainOrder corder = (MaintainOrder) super.getOrder();
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
        retVal = false;

      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltBezahle();
      }
      return retVal;
    }

    protected boolean readBewacheNicht(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setNot(true);

      return checkNextFinal();
    }
  }

  // ************* MACHE
  protected class E3MacheReader extends MacheReader {
    public E3MacheReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected BuildingType isCastle(OrderToken t) {
      if (t.equalsToken(getOrderTranslation(E3AConstants.O_WATCH)))
        if (getRules() != null)
          return getRules().getCastleType(E3AConstants.B_GUARDTOWER);
      return null;
    }
  }

  // ************* REKRUTIERE
  protected class RekrutiereReader extends EresseaOrderParser.RekrutiereReader {
    public RekrutiereReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isNumeric(t.getText()) == true) {
        retVal = readRekrutiereAmount(t);
      } else {
        unexpected(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltRekrutiere();
      }

      return retVal;
    }

    protected boolean readRekrutiereAmount(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_NUMBER;
      int val = Integer.parseInt(token.getText());
      getOrder().setAmount(val);

      OrderToken t = getNextToken();

      if (isString(t)) {
        retVal = new StringChecker(false, false, false, false) {
          @Override
          protected boolean checkInner() {
            if (innerToken == null || getData() == null || getRules() == null)
              return true;
            Race race = getRace(content);
            getOrder().setRace(race);
            return race != null;
          }

        }.read(t);
      } else {
        retVal = checkFinal(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltRekrutiereAmount();
      }

      return retVal;
    }
  }

  protected Race getRace(String content) {
    for (Race r : getRules().getRaces())
      if (r.getRecruitmentCosts() > 0)
        if (content.equalsIgnoreCase(getRuleItemTranslation("race." + r.getID()))
            || content.equalsIgnoreCase(getRuleItemTranslation("race.1." + r.getID())))
          return r;
    return null;
  }

  // ************* TARNE
  /**
   * Only TARNE PARTEI [NICHT] is allowed in E3.
   */
  protected class TarneReader extends EresseaOrderParser.TarneReader {
    public TarneReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (t.equalsToken(getOrderTranslation(EresseaConstants.O_FACTION))) {
        retVal = readTarnePartei(t);
      } else {
        retVal = checkFinal(t);
        if (shallComplete(token, t)) {
          getCompleter().cmpltTarne(false);
        }
      }

      return retVal;
    }

    /**
     * TARNE PARTEI NUMMER not allowed in E3!
     * 
     * @see magellan.library.gamebinding.EresseaOrderParser.TarneReader#readTarnePartei(magellan.library.utils.OrderToken)
     */
    @Override
    protected boolean readTarnePartei(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (t.equalsToken(getOrderTranslation(EresseaConstants.O_NOT))) {
        retVal = readFinalKeyword(t);
      } else {
        retVal = checkFinal(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltTarnePartei();
      }
      return retVal;
    }

    /**
     * This is not allowed in E3!
     * 
     * @see magellan.library.gamebinding.EresseaOrderParser.TarneReader#readTarneParteiNummer(magellan.library.utils.OrderToken)
     * @throws IllegalStateException always!
     */
    @Override
    protected boolean readTarneParteiNummer(OrderToken token) {
      throw new IllegalStateException();
    }
  }

}
