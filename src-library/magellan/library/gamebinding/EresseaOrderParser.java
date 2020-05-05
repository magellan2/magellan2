/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.library.gamebinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import magellan.library.Building;
import magellan.library.EntityID;
import magellan.library.GameData;
import magellan.library.Order;
import magellan.library.Ship;
import magellan.library.Spell;
import magellan.library.StringID;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.UnitID;
import magellan.library.completion.Completion;
import magellan.library.completion.OrderParser;
import magellan.library.gamebinding.RenameOrder.RenameObject;
import magellan.library.gamebinding.e3a.MaintainOrder;
import magellan.library.rules.BuildingType;
import magellan.library.rules.ItemCategory;
import magellan.library.rules.ItemType;
import magellan.library.rules.Race;
import magellan.library.rules.SkillType;
import magellan.library.utils.Direction;
import magellan.library.utils.OrderToken;
import magellan.library.utils.logging.Logger;

/**
 * A class for reading Eressea orders and checking their syntactical correctness. A
 * <tt>OrderParser</tt> object can register a <tt>OrderCompleter</tt> object. In such a case the
 * <tt>OrderParser</tt> will call the corresponding methods of the <tt>OrderCompleter</tt> if it
 * encounters an incomplete order.
 */
public class EresseaOrderParser extends AbstractOrderParser {
  private static final Logger log = Logger.getInstance(EresseaOrderParser.class);
  private EresseaOrderCompleter completer;

  /**
   * @param data
   * @param completer
   */
  public EresseaOrderParser(GameData data, EresseaOrderCompleter completer) {
    super(data);
    setCompleter(completer);
  }

  public EresseaOrderParser(GameData data) {
    super(data);
  }

  /**
   * Fills the trie with all known orders. Subclasses may override this in order to set a different
   * set of orders.
   */
  @Override
  protected void initCommands() {
    clearCommandMap();

    // addCheckedCommand("@", new AtReader());
    addCheckedCommand(EresseaConstants.OC_WORK, new ArbeiteReader(this));

    addCheckedCommand(EresseaConstants.OC_ATTACK, new AttackReader(this));

    addCheckedCommand(EresseaConstants.OC_BANNER, new BannerReader(this));
    addCheckedCommand(EresseaConstants.OC_CLAIM, new BeansprucheReader(this));
    addCheckedCommand(EresseaConstants.OC_PROMOTION, new BefoerderungReader(this));

    addCheckedCommand(EresseaConstants.OC_STEAL, new BeklaueReader(this));
    addCheckedCommand(EresseaConstants.OC_SIEGE, new BelagereReader(this));
    addCheckedCommand(EresseaConstants.OC_NAME, new BenenneReader(this));
    addCheckedCommand(EresseaConstants.OC_USE, new BenutzeReader(this));
    addCheckedCommand(EresseaConstants.OC_DESCRIBE, new BeschreibeReader(this));

    addCheckedCommand(EresseaConstants.OC_ENTER, new BetreteReader(this));

    addCheckedCommand(EresseaConstants.OC_GUARD, new BewacheReader(this));
    addCheckedCommand(EresseaConstants.OC_PAY, new BezahleReader(this));
    addCheckedCommand(EresseaConstants.OC_MESSAGE, new BotschaftReader(this));
    addCheckedCommand(EresseaConstants.OC_DEFAULT, new DefaultReader(this));
    addCheckedCommand(EresseaConstants.OC_EMAIL, new EmailReader(this));
    addCheckedCommand(EresseaConstants.OC_END, new EndeReader(this));
    addCheckedCommand(EresseaConstants.OC_RIDE, new FahreReader(this));
    addCheckedCommand(EresseaConstants.OC_FOLLOW, new FolgeReader(this));
    addCheckedCommand(EresseaConstants.OC_RESEARCH, new ForscheReader(this));
    addCheckedCommand(EresseaConstants.OC_GIVE, new GibReader(this));
    addCheckedCommand(EresseaConstants.OC_GROUP, new GruppeReader(this));
    addCheckedCommand(EresseaConstants.OC_HELP, new HelfeReader(this));
    addCheckedCommand(EresseaConstants.OC_COMBAT, new KaempfeReader(this));
    addCheckedCommand(EresseaConstants.OC_COMBATSPELL, new KampfzauberReader(this));
    addCheckedCommand(EresseaConstants.OC_BUY, new KaufeReader(this));
    addCheckedCommand(EresseaConstants.OC_CONTACT, new KontaktiereReader(this));
    addCheckedCommand(EresseaConstants.OC_TEACH, new LehreReader(this));
    addCheckedCommand(EresseaConstants.OC_LEARN, new LerneReader(this));
    addCheckedCommand(EresseaConstants.OC_SUPPLY, new GibReader(this));

    addCheckedCommand(EresseaConstants.OC_LOCALE, new LocaleReader(this));
    addCheckedCommand(EresseaConstants.OC_MAKE, new MacheReader(this));
    addCheckedCommand(EresseaConstants.OC_MOVE, new NachReader(this));
    // normalerweise nicht erlaubt...
    addCheckedCommand(EresseaConstants.OC_NEXT, new InvalidReader(this));
    addCheckedCommand(EresseaConstants.OC_RESTART, new NeustartReader(this));

    addCheckedCommand(EresseaConstants.OC_NUMBER, new NummerReader(this));

    addCheckedCommand(EresseaConstants.OC_OPTION, new OptionReader(this));
    addCheckedCommand(EresseaConstants.OC_FACTION, new ParteiReader(this));
    addCheckedCommand(EresseaConstants.OC_PASSWORD, new PasswortReader(this));

    addCheckedCommand(EresseaConstants.OC_PLANT, new PflanzeReader(this));
    addCheckedCommand(EresseaConstants.OC_PIRACY, new PiraterieReader(this));
    addCheckedCommand(EresseaConstants.OC_PREFIX, new PraefixReader(this));
    // normalerweise nicht erlaubt...
    addCheckedCommand(EresseaConstants.OC_REGION, new InvalidReader(this));
    addCheckedCommand(EresseaConstants.OC_RECRUIT, new RekrutiereReader(this));
    addCheckedCommand(EresseaConstants.OC_RESERVE, new ReserviereReader(this));
    addCheckedCommand(EresseaConstants.OC_ROUTE, new RouteReader(this));

    addCheckedCommand(EresseaConstants.OC_SORT, new SortiereReader(this));

    addCheckedCommand(EresseaConstants.OC_SPY, new SpioniereReader(this));
    addCheckedCommand(EresseaConstants.OC_QUIT, new StirbReader(this));
    addCheckedCommand(EresseaConstants.OC_HIDE, new TarneReader(this));
    addCheckedCommand(EresseaConstants.OC_CARRY, new TransportiereReader(this));
    addCheckedCommand(EresseaConstants.OC_TAX, new TreibeReader(this));
    addCheckedCommand(EresseaConstants.OC_ENTERTAIN, new UnterhalteReader(this));
    addCheckedCommand(EresseaConstants.OC_ORIGIN, new UrsprungReader(this));
    addCheckedCommand(EresseaConstants.OC_FORGET, new VergesseReader(this));

    addCheckedCommand(EresseaConstants.OC_SELL, new VerkaufeReader(this));
    addCheckedCommand(EresseaConstants.OC_LEAVE, new VerlasseReader(this));
    addCheckedCommand(EresseaConstants.OC_CAST, new ZaubereReader(this));
    addCheckedCommand(EresseaConstants.OC_SHOW, new ZeigeReader(this));
    addCheckedCommand(EresseaConstants.OC_DESTROY, new ZerstoereReader(this));
    addCheckedCommand(EresseaConstants.OC_GROW, new ZuechteReader(this));
    addCheckedCommand(EresseaConstants.OC_SABOTAGE, new SabotiereReader(this));
  }

  protected void addCheckedCommand(StringID prefix, OrderHandler reader) {
    if (getRules().getOrder(prefix) != null) {
      addCommand(prefix, reader);
    }
  }

  // ************* WORK (ARBEITE)
  protected class ArbeiteReader extends OrderHandler {
    public ArbeiteReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      return checkNextFinal();
    }
  }

  // ************* ATTACK (ATTACKIERE)
  protected class AttackReader extends UnitOrderHandler {
    public AttackReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected void init(OrderToken token, String text) {
      super.init(token, text);
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
    public BannerReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;

      return readDescription() != null;
    }
  }

  // ************* BEFÖRDERUNG
  protected class BefoerderungReader extends OrderHandler {
    public BefoerderungReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected void init(OrderToken token, String text) {
      order = new PromoteOrder(getTokens(), text);
    }

    @Override
    public PromoteOrder getOrder() {
      PromoteOrder uorder = (PromoteOrder) super.getOrder();
      return uorder;
    }

    @Override
    protected boolean readIt(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;

      return checkNextFinal();
    }
  }

  // ************* BEKLAUE
  protected class BeklaueReader extends UnitOrderHandler {
    public BeklaueReader(OrderParser parser) {
      super(parser);
    }

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
    public BelagereReader(OrderParser parser) {
      super(parser);
    }

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
          && getData().getRules().getCastleType(tBuilding.getType().getID()) != null
          && checkNextFinal();
    }
  }

  // ************* BENENNE
  protected class BenenneReader extends OrderHandler {
    public BenenneReader(OrderParser parser) {
      super(parser);
    }

    private EntityID target;

    @Override
    protected void init(OrderToken token, String text) {
      target = null;
      order = new RenameOrder(getTokens(), text, RenameObject.T_UNKNOWN, target, null);
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
      if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_CASTLE))) {
        getOrder().type = RenameObject.T_BUILDING;
        getOrder().name = readDescription(false);
        retVal = getOrder().name != null;
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_BUILDING))) {
        getOrder().type = RenameObject.T_BUILDING;
        getOrder().name = readDescription(false);
        retVal = getOrder().name != null;
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_UNIT))) {
        getOrder().type = RenameObject.T_UNIT;
        getOrder().name = readDescription(false);
        retVal = getOrder().name != null;
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_PARAMETER_FACTION))) {
        getOrder().type = RenameObject.T_FACTION;
        getOrder().name = readDescription(false);
        retVal = getOrder().name != null;
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_REGION))) {
        getOrder().type = RenameObject.T_REGION;
        getOrder().name = readDescription(false);
        retVal = getOrder().name != null;
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_SHIP))) {
        getOrder().type = RenameObject.T_SHIP;
        getOrder().name = readDescription(false);
        retVal = getOrder().name != null;
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_FOREIGNBUILDING))) {
        retVal = readBenenneFremdes(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_FOREIGNFACTION))) {
        retVal = readBenenneFremdes(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_FOREIGNSHIP))) {
        retVal = readBenenneFremdes(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_FOREIGNUNIT))) {
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

      if (token.equalsToken(getOrderTranslation(EresseaConstants.OC_FOREIGNUNIT))
          && t.equalsToken(getOrderTranslation(EresseaConstants.OC_UNIT))) {
        retVal = readBenenneFremdeEinheit(t);
      } else if (token.equalsToken(getOrderTranslation(EresseaConstants.OC_FOREIGNBUILDING))
          && t.equalsToken(getOrderTranslation(EresseaConstants.OC_CASTLE))) {
        retVal = readBenenneFremdesGebaeude(t);
      } else if (token.equalsToken(getOrderTranslation(EresseaConstants.OC_FOREIGNBUILDING))
          && t.equalsToken(getOrderTranslation(EresseaConstants.OC_BUILDING))) {
        retVal = readBenenneFremdesGebaeude(t);
      } else if (token.equalsToken(getOrderTranslation(EresseaConstants.OC_FOREIGNFACTION))
          && t.equalsToken(getOrderTranslation(EresseaConstants.OC_PARAMETER_FACTION))) {
        retVal = readBenenneFremdePartei(t);
      } else if (token.equalsToken(getOrderTranslation(EresseaConstants.OC_FOREIGNSHIP))
          && t.equalsToken(getOrderTranslation(EresseaConstants.OC_SHIP))) {
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
      getOrder().type = RenameObject.T_UNIT;

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
      getOrder().type = RenameObject.T_BUILDING;

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
      getOrder().type = RenameObject.T_FACTION;

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
      getOrder().type = RenameObject.T_SHIP;

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
    public BenutzeReader(OrderParser parser) {
      super(parser);
    }

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
    public BeschreibeReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().type = RenameObject.T_DESCRIBE_UNKNOWN;

      OrderToken t = getNextToken();
      t.ttype = OrderToken.TT_KEYWORD;
      if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_CASTLE))) {
        getOrder().type = RenameObject.T_DESCRIBE_BUILDING;
        retVal = readDescription() != null;
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_UNIT))) {
        getOrder().type = RenameObject.T_DESCRIBE_UNIT;
        retVal = readDescription() != null;
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_PRIVATE))) {
        getOrder().type = RenameObject.T_DESCRIBE_UNIT;
        retVal = readDescription() != null;
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_REGION))) {
        getOrder().type = RenameObject.T_DESCRIBE_REGION;
        retVal = readDescription() != null;
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_SHIP))) {
        getOrder().type = RenameObject.T_DESCRIBE_SHIP;
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
    public BetreteReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected void init(OrderToken token, String text) {
      super.init(token, text);
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

      if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_CASTLE))) {
        getOrder().type = UCArgumentOrder.T_BUILDING;
        retVal = readBetreteBurg(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_SHIP))) {
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
    public BewacheReader(OrderParser parser) {
      super(parser);
    }

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

      if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_NOT))) {
        retVal = readBewacheNicht(t);
      } else {
        getOrder().setNot(false);
        retVal = checkFinal(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltBewache();
      }

      return retVal;
    }

    protected boolean readBewacheNicht(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setNot(true);

      return checkNextFinal();
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

      if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_NOT))) {
        getOrder().setNot(true);
        retVal = readBezahleNicht(t);
      } else {
        getOrder().setNot(false);
        retVal = false;
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltBezahle();
      }
      return retVal;
    }

    protected boolean readBezahleNicht(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isID(t.getText(), false)) {
        t.ttype = OrderToken.TT_ID;
        EntityID target = EntityID.createEntityID(t.getText(), getData().base);
        getOrder().setBuilding(target);
        retVal = readFinalID(t);
      } else {
        retVal = checkFinal(t);
      }

      if (getCompleter() != null && !t.followedBySpace()) {
        getCompleter().cmpltBezahleNicht();
      }
      return retVal;
    }
  }

  // ************* BOTSCHAFT
  protected class BotschaftReader extends OrderHandler {
    public BotschaftReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_UNIT))) {
        retVal = readBotschaftEinheit(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_PARAMETER_FACTION))) {
        retVal = readBotschaftPartei(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_REGION))) {
        retVal = readBotschaftRegion(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_CASTLE))) {
        retVal = readBotschaftGebaeude(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_SHIP))) {
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
          && token.getText().equalsIgnoreCase(getOrderTranslation(EresseaConstants.OC_TEMP))) {
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
    private DefaultChecker defaultChecker;

    public DefaultReader(OrderParser parser) {
      super(parser);
      defaultChecker = new DefaultChecker();
    }

    @Override
    protected boolean readIt(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;

      if (!token.followedBySpace())
        return false;

      OrderToken t = getNextToken();

      if (isString(t))
        return getDefaultChecker().read(t);
      else {
        unexpected(t);
      }
      return false;
    }

    private DefaultChecker getDefaultChecker() {
      return defaultChecker;
    }

    protected class DefaultChecker extends StringChecker {

      private EresseaOrderParser innerParser;
      private ArrayList<Completion> oldList;

      public DefaultChecker() {
        super(false, true, true, false);
        innerDefaultQuote = '"';
      }

      protected EresseaOrderParser getInnerParser() {
        if (innerParser == null) {
          innerParser = new EresseaOrderParser(getData(), getCompleter());
        }
        return innerParser;
      }

      @Override
      protected boolean checkInner() {
        // parse the string inside the quote(s); this has side-effects on the completer!
        getInnerParser().setDefaultQuote('\'');
        if (getCompleter() != null) {
          getCompleter().setQuote('\'');
        }
        boolean ok = getInnerParser().parse(content, getLocale()).isValid();
        if (getCompleter() != null) {
          getCompleter().setQuote('"');
          oldList = new ArrayList<Completion>(getCompleter().getCompletions());
        }
        return ok && content.length() != 0 && super.checkInner();
      }

      @Override
      protected void complete() {
        if (nextToken.ttype != OrderToken.TT_EOC)
          return;

        if (getCompleter() != null && getInnerParser().getTokens().size() > 0) {
          // OrderToken lastToken = innerParser.getTokens().get(innerParser.getTokens().size() - 1);
          if (getInnerParser().getTokens().size() > 1) {
            lastToken = getInnerParser().getTokens().get(getInnerParser().getTokens().size() - 2);
            String lastW = "";
            if (!lastToken.followedBySpace() && lastToken.ttype != OrderToken.TT_PERSIST
                && lastToken.ttype != OrderToken.TT_EXCLAM) {
              if (lastToken.ttype == OrderToken.TT_CLOSING_QUOTE) {
                lastW =
                    getLastToken(4).getText() + getLastToken(3).getText()
                        + getLastToken(2).getText();
              } else if (getInnerParser().getTokens().size() > 2
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
        return getInnerParser().getTokens().get(getInnerParser().getTokens().size() - i);
      }
    }
  }

  // ************* EMAIL
  protected class EmailReader extends OrderHandler {
    public EmailReader(OrderParser parser) {
      super(parser);
    }

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
    public EndeReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;

      return checkNextFinal();
    }
  }

  // ************* FAHRE
  protected class FahreReader extends UnitOrderHandler {
    public FahreReader(OrderParser parser) {
      super(parser);
    }

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
          && token.getText().equalsIgnoreCase(getOrderTranslation(EresseaConstants.OC_TEMP))) {
        getCompleter().cmpltFahre(true);
      }

      return checkFinal(t);
    }
  }

  // ************* FOLGE
  protected class FolgeReader extends OrderHandler {
    public FolgeReader(OrderParser parser) {
      super(parser);
    }

    private String text;

    @Override
    protected void init(OrderToken token, String orderText) {
      order = new SimpleOrder(getTokens(), orderText);
      text = orderText;
    }

    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_UNIT)) == true) {
        retVal = readFolgeEinheit(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_SHIP)) == true) {
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

      OrderToken t = getNextToken();

      if (isID(t.getText(), false) == true) {
        EntityID target = EntityID.createEntityID(t.getText(), getData().base);
        UnitContainer tContainer = getData().getShip(target);
        order = new UCArgumentOrder(getTokens(), text, target, UCArgumentOrder.T_SHIP);
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
    public BeansprucheReader(OrderParser parser) {
      super(parser);
    }

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
    public ForscheReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);

      OrderToken t = getNextToken();

      if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_HERBS))) {
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
    public GibReader(OrderParser parser) {
      super(parser);
    }

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
      if (isTempID(token.getText())) {
        target = UnitID.createUnitID(token.getText(), getData().base);
      } else {
        target = UnitID.createUnitID(token.getText(), getData().base);
      }
      // do not test for existence -- unit may be invisible

      OrderToken t = getNextToken();

      if (isNumeric(t.getText()) == true) {
        getOrder().type = EresseaConstants.OC_GIVE;
        retVal = readGibUIDAmount(t, target, Integer.parseInt(t.getText()), true);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_EACH))) {
        getOrder().type = EresseaConstants.OC_GIVE;
        retVal = readGibJe(t, target);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_ALL))) {
        getOrder().type = EresseaConstants.OC_GIVE;
        retVal = readGibUIDAlles(t);
      } else {
        if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_UNIT))) {
          getOrder().type = EresseaConstants.OC_UNIT;
        }
        if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_CONTROL))) {
          getOrder().type = EresseaConstants.OC_CONTROL;
        }
        if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_HERBS))) {
          getOrder().type = EresseaConstants.OC_HERBS;
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
            token.getText().equalsIgnoreCase(getOrderTranslation(EresseaConstants.OC_TEMP)));
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
      // if(t.equalsToken(getOrderTranslation(EresseaConstants.OC_ALL))) {
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

      if (isFinal(t)) {
        retVal = checkFinal(t);
      } else if (isString(t)) {
        getOrder().itemType = checkItem(t);
        if (getOrder().itemType != null) {
          retVal = true;
        } else if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_MEN))) {
          getOrder().type = EresseaConstants.OC_MEN;
          retVal = true;
        } else {
          getOrder().setValid(false);
        }
        retVal = retVal && checkFinal(getLastToken());
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
        } else if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_MEN))) {
          getOrder().type = EresseaConstants.OC_MEN;
          retVal = true;
        } else if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_SHIP))) {
          getOrder().type = EresseaConstants.OC_SHIP;
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
    public GruppeReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isFinal(t)) {
        // just "GRUPPE" without explicit group is valid
        retVal = checkFinal(t);
        if (shallComplete(token, t)) {
          getCompleter().cmpltGruppe();
        }
      } else if (isString(t)) {
        retVal = new StringChecker(false, true, true, false) {
          @Override
          protected void complete() {
            getCompleter().cmpltGruppe();
          }
        }.read(t);
      }

      return retVal;
    }
  }

  // ************* HELFE
  protected class HelfeReader extends OrderHandler {
    public HelfeReader(OrderParser parser) {
      super(parser);
    }

    private Collection<StringID> categories;

    protected Collection<StringID> getCategories() {
      if (categories == null) {
        categories =
            Arrays.asList(EresseaConstants.OC_ALL, EresseaConstants.OC_HELP_COMBAT,
                EresseaConstants.OC_HELP_GIVE, EresseaConstants.OC_HELP_GUARD,
                EresseaConstants.OC_HELP_SILVER, EresseaConstants.OC_HELP_FACTIONSTEALTH,
                EresseaConstants.OC_COMBAT_HELP);

      }
      return categories;
    }

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

      for (StringID key : getCategories()) {
        if (t.equalsToken(getOrderTranslation(key))) {
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

      if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_NOT))) {
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
    public KaempfeReader(OrderParser parser) {
      super(parser);
    }

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

      if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_COMBAT_AGGRESSIVE))) {
        retVal = readFinalKeyword(t);
        getOrder().setStatus(EresseaConstants.CS_AGGRESSIVE);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_COMBAT_REAR))) {
        retVal = readFinalKeyword(t);
        getOrder().setStatus(EresseaConstants.CS_REAR);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_COMBAT_DEFENSIVE))) {
        retVal = readFinalKeyword(t);
        getOrder().setStatus(EresseaConstants.CS_DEFENSIVE);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_COMBAT_NOT))) {
        retVal = readFinalKeyword(t);
        getOrder().setStatus(EresseaConstants.CS_NOT);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_COMBAT_FLEE))) {
        retVal = readFinalKeyword(t);
        getOrder().setStatus(EresseaConstants.CS_FLEE);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_COMBAT_HELP))) {
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

      if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_NOT))) {
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
    public KampfzauberReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_LEVEL))) {
        retVal = readZaubereStufe(t, true);
        // } else if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_NOT))) {
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
      if (token.equalsToken(getOrderTranslation(EresseaConstants.OC_NOT))) {
        token.ttype = OrderToken.TT_KEYWORD;
        return checkNextFinal();
      } else if (isFinal(token)) {
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
    public KaufeReader(OrderParser parser) {
      super(parser);
    }

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
  protected class KontaktiereReader extends UnitOrderHandler {
    public KontaktiereReader(OrderParser parser) {
      super(parser);
    }

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
      target = UnitID.createUnitID(token.getText(), getData().base);

      return checkNextFinal();
    }
  }

  // ************* LEHRE
  protected class LehreReader extends OrderHandler {
    public LehreReader(OrderParser parser) {
      super(parser);
    }

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
            token.getText().equalsIgnoreCase(getOrderTranslation(EresseaConstants.OC_TEMP)));
      }
      return retVal;
    }
  }

  // ************* LERNE
  protected class LerneReader extends OrderHandler {
    public LerneReader(OrderParser parser) {
      super(parser);
    }

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
    public LocaleReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isString(t)) {
        retVal = readDescription(t, false) != null;
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
    public MacheReader(OrderParser parser) {
      super(parser);
    }

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
      } else if (!hasAmount && t.equalsToken(getOrderTranslation(EresseaConstants.OC_TEMP))) {
        getOrder().setLong(false);
        retVal = readMacheTemp(t);
      } else if (!hasAmount && isTempID(t.getText()) == true) {
        getOrder().setLong(false);
        retVal = readMacheTempID(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_CASTLE))) {
        retVal = readMacheBurg(t);
      } else if (isBuilding(t) != null || isCastle(t) != null) {
        retVal = readMacheBuilding(t);
      } else if ((getRules() != null) && (getRules().getShipType(t.getText()) != null)) {
        retVal = readMacheShip(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_SHIP))) {
        retVal = readMacheSchiff(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_ROAD))) {
        retVal = readMacheStrasse(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_SEED))) {
        retVal = readFinalKeyword(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_MALLORNSEED))) {
        retVal = readFinalKeyword(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_HERBS))) {
        retVal = readFinalKeyword(t);
      } else if (isFinal(t)) {
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

      if (isFinal(t)) {
        retVal = checkFinal(t);
        if (shallComplete(token, t)) {
          getCompleter().cmpltMacheTempID();
        }
      } else if (isString(t)) {
        retVal = readDescription(t, false) != null;
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
      // TODO if MACHE new building => implicit leave
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

      if (isString(t) && toDirection(t.getText(), getLocale()) != Direction.INVALID) {
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
    public NachReader(OrderParser parser) {
      super(parser);
    }

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

      if (toDirection(t.getText(), getLocale()) != Direction.INVALID) {
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
      getOrder().addDirection(toDirection(token.getText(), getLocale()));

      OrderToken t = getNextToken();

      if (toDirection(t.getText(), getLocale()) != Direction.INVALID) {
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

  // ************* NEXT (NÄCHSTER)
  protected class InvalidReader extends OrderHandler {
    public InvalidReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;
      return false;
    }
  }

  // ************* NEUSTART
  protected class NeustartReader extends OrderHandler {
    public NeustartReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isString(t)) {
        retVal = new StringChecker(false, false, true, false) {
          @Override
          protected boolean checkInner() {
            if (super.checkInner() && (getRules() != null)) {
              for (Race race : getRules().getRaces()) {
                if (equalsRace(race, content))
                  return true;
              }
            }
            return false;
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
    public NummerReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_UNIT)) == true) {
        retVal = readNummerEinheit(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_SHIP)) == true) {
        retVal = readNummerSchiff(t);
      } else if (t.equalsToken(getOrderTranslation(
          EresseaConstants.OC_PARAMETER_FACTION)) == true) {
        retVal = readNummerPartei(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_CASTLE)) == true) {
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
      } else {
        retVal = checkFinal(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltNummerId();
      }

      return retVal;
    }

    protected boolean readNummerPartei(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isID(t.getText(), false)) {
        retVal = readFinalID(t);
      } else {
        retVal = checkFinal(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltNummerId();
      }

      return retVal;
    }

    protected boolean readNummerSchiff(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isID(t.getText(), false)) {
        retVal = readFinalID(t);
      } else {
        retVal = checkFinal(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltNummerId();
      }

      return retVal;
    }

    protected boolean readNummerBurg(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isID(t.getText(), false)) {
        retVal = readFinalID(t);
      } else {
        retVal = checkFinal(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltNummerId();
      }

      return retVal;
    }
  }

  // ************* OPTION
  protected class OptionReader extends OrderHandler {
    public OptionReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_ADDRESSES))
          || t.equalsToken(getOrderTranslation(EresseaConstants.OC_REPORT))
          || t.equalsToken(getOrderTranslation(EresseaConstants.OC_BZIP2))
          || t.equalsToken(getOrderTranslation(EresseaConstants.OC_COMPUTER))
          || t.equalsToken(getOrderTranslation(EresseaConstants.OC_ITEMPOOL))
          || t.equalsToken(getOrderTranslation(EresseaConstants.OC_SILVERPOOL))
          || t.equalsToken(getOrderTranslation(EresseaConstants.OC_STATISTICS))
          || t.equalsToken(getOrderTranslation(EresseaConstants.OC_ZIPPED))
          || t.equalsToken(getOrderTranslation(EresseaConstants.OC_SCORE))
          || t.equalsToken(getOrderTranslation(EresseaConstants.OC_TEMPLATE))) {
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

      if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_NOT))) {
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
    public ParteiReader(OrderParser parser) {
      super(parser);
    }

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
    public PasswortReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isFinal(t)) {
        // PASSWORT without parameters is allowed
        retVal = checkFinal(t);
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
    public PflanzeReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      OrderToken t = getNextToken();

      if (isNumeric(t.getText()) == true) {
        retVal = readPflanzeAmount(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_HERBS))
          || t.equalsToken(getOrderTranslation(EresseaConstants.OC_SEED))
          || t.equalsToken(getOrderTranslation(EresseaConstants.OC_MALLORNSEED))
          || t.equalsToken(getOrderTranslation(EresseaConstants.OC_TREES))) {
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

      if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_HERBS))
          || t.equalsToken(getOrderTranslation(EresseaConstants.OC_SEED))
          || t.equalsToken(getOrderTranslation(EresseaConstants.OC_MALLORNSEED))
          || t.equalsToken(getOrderTranslation(EresseaConstants.OC_TREES))) {
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
    public PiraterieReader(OrderParser parser) {
      super(parser);
    }

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
    public PraefixReader(OrderParser parser) {
      super(parser);
    }

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
    public RegionReader(OrderParser parser) {
      super(parser);
    }

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
    public RekrutiereReader(OrderParser parser) {
      super(parser);
    }

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
        int val = Integer.parseInt(t.getText());
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
    public ReserviereReader(OrderParser parser) {
      super(parser);
    }

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
          || t.equalsToken(getOrderTranslation(EresseaConstants.OC_ALL))) {
        retVal = readReserviereAmount(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_EACH))) {
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
      boolean retVal = false;
      if (token.equalsToken(getOrderTranslation(EresseaConstants.OC_ALL))) {
        token.ttype = OrderToken.TT_KEYWORD;
        getOrder().amount = Order.ALL;
      } else {
        token.ttype = OrderToken.TT_NUMBER;
        getOrder().amount = Integer.parseInt(token.getText());
      }

      OrderToken t = getNextToken();
      if (!isEoC(t) && isString(t) && !t.equalsToken(getOrderTranslation(
          EresseaConstants.OC_ALL))) {
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
    public RouteReader(OrderParser parser) {
      super(parser);
    }

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

      if (toDirection(t.getText(), getLocale()) != Direction.INVALID) {
        retVal = readRouteDirection(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_PAUSE))) {
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
      getOrder().addDirection(toDirection(token.getText(), getLocale()));

      OrderToken t = getNextToken();

      if (toDirection(t.getText(), getLocale()) != Direction.INVALID) {
        retVal = readRouteDirection(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_PAUSE))) {
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
    public SabotiereReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      OrderToken t = getNextToken();

      if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_SHIP))) {
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
  protected class SortiereReader extends UnitOrderHandler {
    public SortiereReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      // FIX
      if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_BEFORE))) {
        retVal = readSortiereVor(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_AFTER))) {
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
        if (retVal) {
          target = UnitID.createUnitID(t.getText(), getData().base);
        }
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
        if (retVal) {
          target = UnitID.createUnitID(t.getText(), getData().base);
        }
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
  protected class SpioniereReader extends UnitOrderHandler {
    public SpioniereReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      OrderToken t = getNextToken();

      if (isID(t.getText()) == true) {
        retVal = readFinalID(t, true);
        if (retVal) {
          target = UnitID.createUnitID(t.getText(), getData().base);
        }
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
    public StirbReader(OrderParser parser) {
      super(parser);
    }

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
    public TarneReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isNumeric(t.getText()) == true) {
        retVal = readFinalNumber(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_PARAMETER_FACTION))) {
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

      if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_NOT))) {
        retVal = readFinalKeyword(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_STEALTH_NUMBER))) {
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
    public TransportiereReader(OrderParser parser) {
      super(parser);
    }

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
        getOrder().target = UnitID.createUnitID(t.getText(), getData().base);

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
    public TreibeReader(OrderParser parser) {
      super(parser);
    }

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
    public UnterhalteReader(OrderParser parser) {
      super(parser);
    }

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
    public UrsprungReader(OrderParser parser) {
      super(parser);
    }

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
    public VergesseReader(OrderParser parser) {
      super(parser);
    }

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
    public VerkaufeReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      OrderToken t = getNextToken();

      if (isNumeric(t.getText()) == true) {
        retVal = readVerkaufeAmount(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_ALL))) {
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
          && luxuryCategory.equals(type.getCategory())) {
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
    public VerlasseReader(OrderParser parser) {
      super(parser);
    }

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
    public ZaubereReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      OrderToken t = getNextToken();

      if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_REGION))) {
        retVal = readZaubereRegion(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_LEVEL))) {
        retVal = readZaubereStufe(t, false);
      } else if (isString(t)) {
        retVal = new ZaubereSpruchChecker(false, false, true, true).read(t);
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

        if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_LEVEL))) {
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

      public ZaubereSpruchChecker(boolean far, boolean combat, boolean addRegion,
          boolean addLevel) {
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
        // Problem: keine Referenz auf die Unit, wir können nicht die spells der unit durchgehen
        // wir müssen spells der GameData durchgehen
        foundSpell = null;
        for (Spell s : getData().getSpells()) {
          if (content.equalsIgnoreCase(s.getName())) {
            // here we return just true
            // toDo: get Spell Syntax, check, if more tokens expected and
            // do next checks
            if (s.getType() == null || (!combat ^ s.getType().toLowerCase().indexOf(
                "combat") > -1)) {
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
        return false;
      return !isFinal(t) ^ s.getSyntax().length() == 0;
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
    // if(token.equalsToken(getOrderTranslation(EresseaConstants.OC_CASTLE))) {
    // token.ttype = OrderToken.TT_KEYWORD;
    // token = tokens.next();
    // token.ttype = OrderToken.TT_ID;
    //
    // } else if(token.equalsToken(getOrderTranslation(EresseaConstants.OC_UNIT))) {
    //
    // } else if(token.equalsToken(getOrderTranslation(EresseaConstants.OC_PARAMETER_FACTION))) {
    //
    // } else if(token.equalsToken(getOrderTranslation(EresseaConstants.OC_REGION))) {
    //
    // } else if(token.equalsToken(getOrderTranslation(EresseaConstants.OC_SHIP))) {
    //
    // }
    // }
    // }
    // return false;
    // }}

  }

  // ************* ZEIGE
  protected class ZeigeReader extends OrderHandler {
    public ZeigeReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_ALL))) {
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

      if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_POTIONS))
          || t.equalsToken(getOrderTranslation(EresseaConstants.OC_SPELLS))) {
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
    public ZerstoereReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      OrderToken t = getNextToken();

      if (isNumeric(t.getText())) {
        retVal = readZerstoereAmount(t);
      } else if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_ROAD))) {
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

      if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_ROAD))) {
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

      if (isString(t) && toDirection(t.getText(), getLocale()) != Direction.INVALID) {
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
    public ZuechteReader(OrderParser parser) {
      super(parser);
    }

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

      if (t.equalsToken(getOrderTranslation(EresseaConstants.OC_HERBS))
          || (!number && t.equalsToken(getOrderTranslation(EresseaConstants.OC_HORSES))))
      // this is illegal (now?)
      // || t.equalsToken(getOrderTranslation(EresseaConstants.OC_TREES))
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

  /**
   * Returns the value of completer.
   *
   * @return Returns completer.
   */
  @Override
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
   * @see magellan.library.gamebinding.AbstractOrderParser#getTemp()
   */
  @Override
  protected StringID getTemp() {
    return EresseaConstants.OC_TEMP;
  }

  protected boolean equalsRace(Race race, String content) {
    return equalsNormalized(getRuleItemTranslation("race." + race.getID()), content) ||
        equalsNormalized(getRuleItemTranslation("race.1." + race.getID()), content);
  }

  // private static Map<Locale, String[]> shortNamess = new HashMap<Locale, String[]>();
  // private static Map<Locale, String[]> longNamess = new HashMap<Locale, String[]>();
  // private static Map<Locale, String[]> normalizedLongNamess = new HashMap<Locale, String[]>();
  //
  // private String getLongDirectionString(int key, Locale locale) {
  // if (locale == null) {
  // locale = Locales.getOrderLocale();
  // }
  // switch (key) {
  // case Direction.DIR_NW:
  // return getOrderTranslation(EresseaConstants.OC_NORTHWEST, locale);
  //
  // case Direction.DIR_NE:
  // return getOrderTranslation(EresseaConstants.OC_NORTHEAST, locale);
  //
  // case Direction.DIR_E:
  // return getOrderTranslation(EresseaConstants.OC_EAST, locale);
  //
  // case Direction.DIR_SE:
  // return getOrderTranslation(EresseaConstants.OC_SOUTHEAST, locale);
  //
  // case Direction.DIR_SW:
  // return getOrderTranslation(EresseaConstants.OC_SOUTHWEST, locale);
  //
  // case Direction.DIR_W:
  // return getOrderTranslation(EresseaConstants.OC_WEST, locale);
  // }
  //
  // return get("util.direction.name.long.invalid", locale);
  // }
  //
  // private static String getShortDirectionString(int key, Locale locale) {
  // if (locale == null) {
  // locale = Locales.getOrderLocale();
  // }
  // switch (key) {
  // case Direction.DIR_NW:
  // return getOrderTranslation(EresseaConstants.OC_NW, locale);
  //
  // case Direction.DIR_NE:
  // return getOrderTranslation(EresseaConstants.OC_NE, locale);
  //
  // case Direction.DIR_E:
  // return getOrderTranslation(EresseaConstants.OC_E, locale);
  //
  // case Direction.DIR_SE:
  // return getOrderTranslation(EresseaConstants.OC_SE, locale);
  //
  // case Direction.DIR_SW:
  // return getOrderTranslation(EresseaConstants.OC_SW, locale);
  //
  // case Direction.DIR_W:
  // return getOrderTranslation(EresseaConstants.OC_W, locale);
  // }
  //
  // return Resources.get("util.direction.name.short.invalid", locale);
  // }
  //
  // // /**
  // // * @param shortForm
  // // * @return f true, a short form of the direction's string representation is returned.
  // // */
  // // public String directionToString(boolean shortForm) {
  // // if (shortForm)
  // // return getShortDirectionString(dir, null);
  // // else
  // // return getLongDirectionString(dir, null);
  // // }
  //
  // /**
  // * Returns a String representation of the specified direction. <b>Note:</b> Please prefer using
  // * {@link #toString()}.
  // */
  // public String directionToString(int dir) {
  // return directionToString(dir, false);
  // }
  //
  // /**
  // * Returns a String representation of the specified direction. <b>Note:</b> Please prefer using
  // * {@link #toString(boolean)}.
  // *
  // * @param dir if true, a short form of the direction's string representation is returned.
  // */
  // public String directionToString(int dir, boolean shortForm) {
  // if (shortForm)
  // return getShortDirectionString(dir, null);
  // else
  // return getLongDirectionString(dir, null);
  // }
  //
  // /**
  // * Returns a String representation of the specified direction.
  // */
  // public static String directionToString(CoordinateID c) {
  // return Direction.toDirection(c).toString();
  // }
  //
  // // protected static void initNames(Locale locale) {
  // // if (locale == null) {
  // // locale = Locales.getOrderLocale();
  // // }
  // // if (shortNamess.containsKey(locale))
  // // return;
  // //
  // // String[] shorty = new String[6];
  // // String[] longy = new String[6];
  // // String[] nlongy = new String[6];
  // // Direction.shortNamess.put(locale, shorty);
  // // Direction.longNamess.put(locale, longy);
  // // Direction.normalizedLongNamess.put(locale, nlongy);
  // //
  // // for (int i = 0; i < 6; i++) {
  // // shorty[i] = Direction.getShortDirectionString(i, locale).toLowerCase();
  // // longy[i] = Direction.getLongDirectionString(i, locale).toLowerCase();
  // // nlongy[i] = Umlaut.convertUmlauts(Direction.getLongDirectionString(i,
  // locale)).toLowerCase();
  // // }
  // // }
  //
  // /**
  // * Converts a string (in the default order locale) to a direction.
  // */
  // public static Direction toDirection(String str) {
  // return toDirection(str, null);
  // }
  //
  // /**
  // * Converts a string (in the specified locale) to a direction.
  // */
  // public static Direction toDirection(String str, Locale locale) {
  // int dir = Direction.DIR_INVALID;
  // String s = Umlaut.normalize(str).toLowerCase();
  //
  // initNames(locale);
  // dir = Direction.find(s, SHORT, locale);
  //
  // if (dir == Direction.DIR_INVALID) {
  // dir = Direction.find(s, NORMLONG, locale);
  // }
  //
  // return toDirection(dir);
  // }
  //
  // /**
  // * Converts a string to an integer representation of the direction.
  // *
  // * @deprecated Prefer using {@link #toDirection(String)}.
  // */
  // @Deprecated
  // public static int toInt(String str) {
  // return toDirection(str).getDir();
  // }
  //
  // /**
  // * Returns the names of all valid directions in an all-lowercase short form.
  // */
  // public static List<String> getShortNames() {
  // return getShortNames(null);
  // }
  //
  // /**
  // * Returns the names of all valid directions in an all-lowercase short form.
  // */
  // public static List<String> getShortNames(Locale locale) {
  // initNames(locale);
  //
  // return Arrays.asList(Direction.shortNamess.get(locale == null ? Locales.getOrderLocale()
  // : locale));
  // }
  //
  // /**
  // * Returns the names of all valid directions in an all-lowercase long form.
  // */
  // public static List<String> getLongNames() {
  // return getLongNames(null);
  // }
  //
  // /**
  // * Returns the names of all valid directions in an all-lowercase long form.
  // */
  // public static List<String> getLongNames(Locale locale) {
  // initNames(locale);
  //
  // return Arrays.asList(Direction.longNamess.get(locale == null ? Locales.getOrderLocale()
  // : locale));
  // }
  //
  // /**
  // * Returns the names of all valid directions in an all-lowercase long form. The names are also
  // * normalized (umlauts converted etc.)
  // */
  // public static List<String> getNormalizedLongNames() {
  // return getNormalizedLongNames(null);
  // }
  //
  // /**
  // * Returns the names of all valid directions in an all-lowercase long form. The names are also
  // * normalized (umlauts converted etc.)
  // */
  // public static List<String> getNormalizedLongNames(Locale locale) {
  // initNames(locale);
  //
  // return Arrays.asList(Direction.normalizedLongNamess.get(locale == null ? Locales
  // .getOrderLocale() : locale));
  // }
  //
  // /**
  // * Finds pattern in the set of matches (case-sensitively) and returns the index of the hit.
  // * Pattern may be an abbreviation of any of the matches. If pattern is ambiguous or cannot be
  // * found among the matches, -1 is returned
  // *
  // * @param locale
  // */
  // private static int find(String pattern, int mode, Locale locale) {
  // if (locale == null) {
  // locale = Locales.getOrderLocale();
  // }
  //
  // int hits = 0;
  // int hitIndex = -1;
  // String[] strings;
  // switch (mode) {
  // case SHORT:
  // strings = shortNamess.get(locale);
  // break;
  // case LONG:
  // strings = longNamess.get(locale);
  // break;
  // case NORMLONG:
  // strings = normalizedLongNamess.get(locale);
  // break;
  // default:
  // throw new IllegalStateException();
  // }
  //
  // for (int i = 0; i < strings.length; ++i) {
  // if (strings[i].startsWith(pattern)) {
  // hits++;
  // hitIndex = i;
  // }
  // }
  //
  // if (hits == 1)
  // return hitIndex;
  // else
  // return -1;
  // }

}