// class magellan.library.gamebinding.AtlantisOrderParser
// created on Apr 16, 2013
//
// Copyright 2003-2013 by magellan project team
//
// Author : $Author: $
// $Id: $
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc.,
// 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
package magellan.library.gamebinding.atlantis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import magellan.library.EntityID;
import magellan.library.GameData;
import magellan.library.StringID;
import magellan.library.UnitID;
import magellan.library.completion.OrderParser;
import magellan.library.gamebinding.AbstractOrderParser;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.gamebinding.GiveOrder;
import magellan.library.gamebinding.MovementOrder;
import magellan.library.gamebinding.OrderHandler;
import magellan.library.gamebinding.TeachOrder;
import magellan.library.rules.ItemType;
import magellan.library.utils.Direction;
import magellan.library.utils.OrderToken;

/**
 * @author stm
 */
public class AtlantisOrderParser extends AbstractOrderParser {

  protected AtlantisOrderCompleter completer;
  public final ItemType silverType;

  /**
   * @param data
   */
  public AtlantisOrderParser(GameData data) {
    this(data, null);
  }

  /**
   * @param data
   * @param cc
   */
  public AtlantisOrderParser(GameData data, AtlantisOrderCompleter cc) {
    super(data);
    setCompleter(cc);
    setQuotes(new char[] { '"' });
    silverType = data.getRules().getItemType(AtlantisConstants.I_USILVER);
  }

  /**
   * @see magellan.library.gamebinding.AbstractOrderParser#initCommands()
   */
  @Override
  protected void initCommands() {
    setPrefixMatching(false);
    clearCommandMap();

    // WORK
    addCommand(AtlantisConstants.OC_WORK, new WorkReader(this));
    // FORM u1
    addCommand(AtlantisConstants.OC_FORM, new FormReader(this));
    // END
    addCommand(AtlantisConstants.OC_END, new EndReader(this));
    // ACCEPT f1
    addCommand(AtlantisConstants.OC_ACCEPT, new AcceptReader(this));
    // ADDRESS Address
    addCommand(AtlantisConstants.OC_ADDRESS, new AddressReader(this));
    // ADMIT f1
    addCommand(AtlantisConstants.OC_ADMIT, new AdmitReader(this));
    // ALLY f1 01
    addCommand(AtlantisConstants.OC_ALLY, new AllyReader(this));
    // BEHIND 01
    addCommand(AtlantisConstants.OC_BEHIND, new BehindReader(this));
    // COMBAT spell
    addCommand(AtlantisConstants.OC_COMBAT, new CombatReader(this));
    // DISPLAY (UNIT | BUILDING SHIP) string
    addCommand(AtlantisConstants.OC_DISPLAY, new DisplayReader(this));
    // GUARD 01
    addCommand(AtlantisConstants.OC_GUARD, new GuardReader(this));
    // NAME (FACTION | UNIT | BUILDING | SHIP) name
    addCommand(AtlantisConstants.OC_NAME, new NameReader(this));
    // PASSWORD password
    addCommand(AtlantisConstants.OC_PASSWORD, new PasswordReader(this));
    // RESHOW spell
    addCommand(AtlantisConstants.OC_RESHOW, new ReshowReader(this));
    // FIND f1
    addCommand(AtlantisConstants.OC_FIND, new FindReader(this));
    // BOARD s1
    addCommand(AtlantisConstants.OC_BOARD, new BoardReader(this));
    // ENTER b1
    addCommand(AtlantisConstants.OC_ENTER, new EnterReader(this));
    // LEAVE
    addCommand(AtlantisConstants.OC_LEAVE, new LeaveReader(this));
    // PROMOTE u1
    addCommand(AtlantisConstants.OC_PROMOTE, new PromoteReader(this));
    // ATTACK (u1 | PEASANTS)
    addCommand(AtlantisConstants.OC_ATTACK, new AttackReader(this));
    // DEMOLISH
    addCommand(AtlantisConstants.OC_DEMOLISH, new DemolishReader(this));
    // GIVE u1 1 item
    addCommand(AtlantisConstants.OC_GIVE, new GiveReader(this, AtlantisConstants.OC_GIVE));
    // PAY u1 1
    addCommand(AtlantisConstants.OC_PAY, new GiveReader(this, AtlantisConstants.OC_PAY));
    // SINK
    addCommand(AtlantisConstants.OC_SINK, new SinkReader(this));
    // TRANSFER (u1 | PEASANTS) 1
    addCommand(AtlantisConstants.OC_TRANSFER, new GiveReader(this, AtlantisConstants.OC_TRANSFER));
    // TAX
    addCommand(AtlantisConstants.OC_TAX, new TaxReader(this));
    // RECRUIT 1
    addCommand(AtlantisConstants.OC_RECRUIT, new RecruitReader(this));
    // QUIT password
    addCommand(AtlantisConstants.OC_QUIT, new QuitReader(this));
    // MOVE (N | W | M | S | W | Y)
    addCommand(AtlantisConstants.OC_MOVE, new MoveReader(this));
    // SAIL (N | W | M | S | W | Y)
    addCommand(AtlantisConstants.OC_SAIL, new SailReader(this));
    // BUILD (BUILDING [b1]) | (SHIP [s1|type])
    addCommand(AtlantisConstants.OC_BUILD, new BuildReader(this));
    // ENTERTAIN
    addCommand(AtlantisConstants.OC_ENTERTAIN, new EntertainReader(this));
    // PRODUCE item
    addCommand(AtlantisConstants.OC_PRODUCE, new ProduceReader(this));
    // RESEARCH [1]
    addCommand(AtlantisConstants.OC_RESEARCH, new ResearchReader(this));
    // STUDY skill
    addCommand(AtlantisConstants.OC_STUDY, new StudyReader(this));
    // TEACH u1+
    addCommand(AtlantisConstants.OC_TEACH, new TeachReader(this));
    // WORK
    addCommand(AtlantisConstants.OC_WORK, new WorkReader(this));
    // CAST spell
    addCommand(AtlantisConstants.OC_CAST, new CastReader(this));

  }

  protected class BareHandler extends OrderHandler {

    protected BareHandler(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;

      return checkNextFinal();
    }

    protected void completeId() {
      getCompleter().cmpltId();
    }
  }

  protected class IdHandler extends OrderHandler {

    private boolean multi;

    protected List<EntityID> ids = new ArrayList<EntityID>();

    private boolean temp;

    protected IdHandler(OrderParser parser) {
      super(parser);
    }

    @Override
    protected void init(OrderToken token, String text) {
      super.init(token, text);
      ids.clear();
    }

    public IdHandler(OrderParser parser, boolean multi) {
      super(parser);
      this.multi = multi;
    }

    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isID(t.getText(), isTemp())) {
        addId(t);
        if (multi) {
          retVal = readIt(t);
        } else {
          retVal = readFinalID(t);
        }
      } else if (multi && ids.size() > 0) {
        retVal = checkFinal(t);
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        completeId();
      }

      return retVal;
    }

    protected void addId(OrderToken t) {
      ids.add(EntityID.createEntityID(t.getText(), getData().base));
    }

    /**
     * Returns the value of temp.
     *
     * @return Returns temp.
     */
    public boolean isTemp() {
      return temp;
    }

    /**
     * Sets the value of temp.
     *
     * @param temp The value for temp.
     */
    public void setTemp(boolean temp) {
      this.temp = temp;
    }

    protected void completeId() {
      getCompleter().cmpltId();
    }
  }

  protected class NumberHandler extends OrderHandler {

    int number;

    private final int min;
    private final int max;
    private boolean optional;

    protected NumberHandler(OrderParser parser, int min, int max) {
      this(parser, min, max, false);
    }

    @Override
    protected void init(OrderToken token, String text) {
      super.init(token, text);
      number = -1;
    }

    public NumberHandler(OrderParser parser, int min, int max, boolean optional) {
      super(parser);
      this.min = min;
      this.max = max;
      this.optional = optional;
    }

    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isNumeric(t.getText(), 10, min, max)) {
        retVal = readFinalID(t);
        number = Integer.parseInt(t.getText());
      } else if (optional && checkFinal(t))
        return true;
      else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        completeNumber();
      }

      return retVal;
    }

    private void completeNumber() {
      getCompleter().cmpltNumber(min, max);
    }
  }

  protected class FlagHandler extends NumberHandler {

    protected FlagHandler(OrderParser parser) {
      super(parser, 0, 1);
    }

  }

  protected class StringHandler extends OrderHandler {

    private String string;

    @Override
    protected void init(OrderToken token, String text) {
      super.init(token, text);
      string = null;
    }

    protected StringHandler(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isString(t)) {
        AbstractOrderParser.StringChecker checker =
            new AbstractOrderParser.StringChecker(false, false, true, false);
        retVal = checker.read(t);
        string = checker.content;
      } else {
        unexpected(t);
      }

      completeString(token, t);

      return retVal;
    }

    protected void completeString(OrderToken lastToken, OrderToken currentToken) {
      // may overwrite
    }
  }

  protected class DirectionHandler extends OrderHandler {
    protected DirectionHandler(OrderParser parser) {
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

      OrderToken t = getNextToken();
      Direction dir;
      if ((dir = toDirection(t.getText(), getLocale())) != Direction.INVALID) {
        t.ttype = OrderToken.TT_KEYWORD;
        getOrder().addDirection(dir);
        retVal = checkNextFinal();
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltDirection();
      }
      return retVal;

    }
  }

  protected class FormReader extends IdHandler {
    // FORM id
    public FormReader(OrderParser parser) {
      super(parser);
    }

  }

  protected class EndReader extends BareHandler {
    // END
    public EndReader(OrderParser parser) {
      super(parser);
    }
  }

  protected class AcceptReader extends IdHandler {
    // ACCEPT f1
    public AcceptReader(OrderParser parser) {
      super(parser);
    }
  }

  protected class AddressReader extends StringHandler {
    // syntax: ADDRESS Address
    public AddressReader(OrderParser parser) {
      super(parser);
    }

  }

  protected class AdmitReader extends IdHandler {
    // ADMIT f1
    public AdmitReader(OrderParser parser) {
      super(parser);
    }
  }

  protected class AllyReader extends IdHandler {
    // ALLY f1 01
    public AllyReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isID(t.getText(), false)) {
        retVal = readAlly(t);
        ids.add(EntityID.createEntityID(t.getText(), getData().base));
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        completeId();
      }

      return retVal;
    }

    private boolean readAlly(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_ID;

      OrderToken t = getNextToken();

      if (isNumeric(t.getText(), 10, 0, 1)) {
        retVal = readFinalNumber(t);
        // flag = Integer.parserInt(t.getText());
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltNumber(0, 1);
      }

      return retVal;
    }
  }

  protected class BehindReader extends FlagHandler {
    // BEHIND 01
    public BehindReader(OrderParser parser) {
      super(parser);
    }
  }

  protected class CombatReader extends StringHandler {
    // COMBAT spell
    public CombatReader(OrderParser parser) {
      super(parser);
    }
  }

  protected class KeywordStringReader extends OrderHandler {
    private StringID[] keywords;

    // XYZ (kw1 | kw2 | ...) string
    public KeywordStringReader(OrderParser parser, StringID... keywords) {
      super(parser);
      this.keywords = keywords;
    }

    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();
      String name;
      boolean kwFound = false;
      for (StringID key : keywords)
        if (t.equalsToken(getOrderTranslation(key))) {
          kwFound = true;
        }
      if (kwFound) {
        t.ttype = OrderToken.TT_KEYWORD;
        name = readDescription(false);
        retVal = name != null;
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltKeyword(keywords);
      }

      return retVal;
    }
  }

  protected class DisplayReader extends KeywordStringReader {
    // DISPLAY (UNIT | BUILDING SHIP) string
    public DisplayReader(OrderParser parser) {
      super(parser, AtlantisConstants.OC_UNIT, AtlantisConstants.OC_BUILDING,
          AtlantisConstants.OC_SHIP);
    }

  }

  protected class GuardReader extends FlagHandler {
    // GUARD 01
    public GuardReader(OrderParser parser) {
      super(parser);
    }
  }

  protected class NameReader extends KeywordStringReader {
    // NAME (FACTION | UNIT | BUILDING | SHIP) name
    public NameReader(OrderParser parser) {
      super(parser, AtlantisConstants.OC_PARAMETER_FACTION, AtlantisConstants.OC_UNIT,
          AtlantisConstants.OC_BUILDING, AtlantisConstants.OC_SHIP);
    }

  }

  protected class PasswordReader extends StringHandler {
    // PASSWORD password
    public PasswordReader(OrderParser parser) {
      super(parser);
    }
  }

  protected class ReshowReader extends StringHandler {
    // RESHOW spell
    public ReshowReader(OrderParser parser) {
      super(parser);
    }
  }

  protected class FindReader extends IdHandler {
    // FIND f1
    public FindReader(OrderParser parser) {
      super(parser);
    }
  }

  protected class BoardReader extends IdHandler {
    // BOARD s1
    public BoardReader(OrderParser parser) {
      super(parser);
    }
  }

  protected class EnterReader extends IdHandler {
    // ENTER b1
    public EnterReader(OrderParser parser) {
      super(parser);
    }

  }

  protected class LeaveReader extends BareHandler {
    // LEAVE
    public LeaveReader(OrderParser parser) {
      super(parser);
    }
  }

  protected class PromoteReader extends IdHandler {
    // PROMOTE u1
    public PromoteReader(OrderParser parser) {
      super(parser);
      setTemp(true);
    }

    @Override
    protected void addId(OrderToken t) {
      ids.add(UnitID.createUnitID(t.getText(), getData().base));
    }

  }

  protected class AttackReader extends UnitOrderHandler {
    // ATTACK (u1 | PEASANTS)
    public AttackReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isID(t.getText(), false)) {
        target = UnitID.createUnitID(t.getText(), getData().base);
        retVal = readFinalID(t);
      } else if (t.equalsToken(getOrderTranslation(AtlantisConstants.OC_PEASANTS))) {
        target = UnitID.createUnitID(0, getData().base);
        t.ttype = OrderToken.TT_KEYWORD;
        retVal = checkNextFinal();
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        getCompleter().cmpltAttack();
      }

      return retVal;
    }

  }

  protected class DemolishReader extends BareHandler {
    // DEMOLISH
    public DemolishReader(OrderParser parser) {
      super(parser);
    }
  }

  protected class GiveReader extends UnitOrderHandler {
    private int number;
    private StringID type;

    // GIVE u1 1 item
    // PAY (u1 | PEASANTS) 1
    // TRANSFER (u1 | PEASANTS) 1
    public GiveReader(OrderParser parser, StringID type) {
      super(parser);
      this.type = type;
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
      uorder.amount = number;
      uorder.type = AtlantisConstants.OC_GIVE;
      if (type == AtlantisConstants.OC_PAY) {
        uorder.itemType = silverType;
      } else if (type == AtlantisConstants.OC_TRANSFER) {
        uorder.type = EresseaConstants.OC_MEN;
      }
      return uorder;
    }

    @Override
    protected boolean readIt(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      boolean retVal = false;
      if (isID(t.getText(), true)) {
        t.ttype = OrderToken.TT_ID;
        target = UnitID.createUnitID(t.getText(), getData().base);

        retVal = readId(t);
      } else if (type != AtlantisConstants.OC_GIVE
          && t.equalsToken(getOrderTranslation(AtlantisConstants.OC_PEASANTS))) {
        // only transfer and pay allow PEASANTS as argument
        target = UnitID.createUnitID(0, getData().base);
        t.ttype = OrderToken.TT_KEYWORD;

        retVal = readId(t);
      } else {
        unexpected(t);
        if (shallComplete(token, t)) {
          getCompleter().addRegionUnits(" ", false);
        }
      }

      return retVal;

    }

    protected boolean readId(OrderToken token) {
      OrderToken t = getNextToken();
      boolean retVal = false;
      if (isNumeric(t.getText())) {
        t.ttype = OrderToken.TT_NUMBER;
        number = Integer.parseInt(t.getText());

        t = getNextToken();
        if (type == AtlantisConstants.OC_GIVE) {
          if (isString(t)) {
            getOrder().itemType = checkItem(t);
            if (getOrder().itemType != null) {
              if (getOrder().itemType.getID().equals(AtlantisConstants.I_USILVER)) {
                getOrder().itemType = null;
              } else {
                retVal = checkFinal(getLastToken());
              }
            } else if (shallComplete(token, t)) {
              getCompleter().cmpltItem(AtlantisConstants.I_USILVER);
            }
          } else {
            unexpected(t);
            if (shallComplete(token, t)) {
              getCompleter().cmpltItem();
            }
          }
        } else {
          retVal = checkFinal(t);
        }
      } else {
        unexpected(t);
        if (shallComplete(token, t)) {
          getCompleter().cmpltNumber(1, Integer.MAX_VALUE);
        }
      }
      return retVal;

    }
  }

  protected class SinkReader extends BareHandler {
    // SINK
    public SinkReader(OrderParser parser) {
      super(parser);
    }
  }

  protected class TaxReader extends BareHandler {
    // TAX
    public TaxReader(OrderParser parser) {
      super(parser);
    }
  }

  protected class RecruitReader extends NumberHandler {
    // RECRUIT 1
    public RecruitReader(OrderParser parser) {
      super(parser, 1, Integer.MAX_VALUE);
    }
  }

  protected class QuitReader extends StringHandler {
    // QUIT password
    public QuitReader(OrderParser parser) {
      super(parser);
    }
  }

  protected class MoveReader extends DirectionHandler {
    // MOVE (N | W | M | S | W | Y)
    public MoveReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      return super.readIt(token);
    }
  }

  protected class SailReader extends DirectionHandler {
    // SAIL (N | W | M | S | W | Y)
    public SailReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      return super.readIt(token);
    }
  }

  protected class BuildReader extends OrderHandler {
    // BUILD (BUILDING [b1]) | (SHIP s1) | (shiptype)
    public BuildReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();
      if (t.equalsToken(getOrderTranslation(AtlantisConstants.OC_BUILDING))
          || t.equalsToken(getOrderTranslation(AtlantisConstants.OC_SHIP))) {
        t.ttype = OrderToken.TT_KEYWORD;

        token = t;
        t = getNextToken();
        if (isID(t.getText(), false)) {
          retVal = readFinalID(t, false);
        } else {
          unexpected(t);
          if (shallComplete(token, t)) {
            getCompleter().cmpltId();
          }
        }
      } else if ((getRules() != null) && (getRules().getShipType(t.getText()) != null)) {
        retVal = checkNextFinal();
      } else {
        unexpected(t);
        if (shallComplete(token, t)) {
          getCompleter().cmpltKeyword(AtlantisConstants.OC_BUILDING, AtlantisConstants.OC_SHIP);
          getCompleter().addShiptypes();
        }
      }

      getOrder().setLong(true);
      return retVal;
    }
  }

  protected class EntertainReader extends BareHandler {
    // ENTERTAIN
    public EntertainReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      getOrder().setLong(true);
      return super.readIt(token);
    }
  }

  protected class ProduceReader extends OrderHandler {
    // PRODUCE item
    public ProduceReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      getOrder().setLong(true);
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isString(t)) {
        ItemType item = checkItem(t);
        retVal = item != null && checkFinal(getLastToken());
      } else {
        unexpected(t);
      }
      if (shallComplete(token, t)) {
        getCompleter().cmpltMache();
      }

      return retVal;
    }

  }

  protected class ResearchReader extends NumberHandler {
    // RESEARCH [1]
    public ResearchReader(OrderParser parser) {
      super(parser, 1, Integer.MAX_VALUE, true);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      getOrder().setLong(true);
      return super.readIt(token);
    }
  }

  protected class StudyReader extends StringHandler {
    // STUDY skill
    public StudyReader(OrderParser parser) {
      super(parser);
    }

    // FIXME check skill
    @Override
    protected boolean readIt(OrderToken token) {
      getOrder().setLong(true);
      return super.readIt(token);
    }

    @Override
    protected void completeString(OrderToken token, OrderToken t) {
      if (shallComplete(token, t)) {
        getCompleter().addSkills();
      }
    }
  }

  protected class TeachReader extends IdHandler {
    // TEACH u1+
    public TeachReader(OrderParser parser) {
      super(parser, true);
      setTemp(true);
    }

    @Override
    protected void init(OrderToken token, String text) {
      super.init(token, text);
      order = new TeachOrder(getTokens(), text);
    }

    @Override
    public TeachOrder getOrder() {
      TeachOrder uorder = (TeachOrder) super.getOrder();
      return uorder;
    }

    @Override
    protected void postProcess() {
      getOrder().setLong(true);
      for (EntityID id : ids) {
        getOrder().addUnit(UnitID.createUnitID(id.intValue(), getData().base));
      }
    }

    @Override
    protected void completeId() {
      getCompleter().addRegionUnits(" ", false);
    }

    @Override
    protected void addId(OrderToken t) {
      ids.add(UnitID.createUnitID(t.getText(), getData().base));
    }

  }

  protected class WorkReader extends BareHandler {
    // WORK
    public WorkReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      getOrder().setLong(true);
      return super.readIt(token);
    }

  }

  protected class CastReader extends StringHandler {
    // CAST spell
    public CastReader(OrderParser parser) {
      super(parser);
    }

    // FIXME check spell
    @Override
    protected boolean readIt(OrderToken token) {
      getOrder().setLong(true);
      return super.readIt(token);
    }
  }

  /**
   * Returns the value of completer.
   *
   * @return Returns completer.
   */
  @Override
  public AtlantisOrderCompleter getCompleter() {
    return completer;
  }

  /**
   * Sets the value of completer.
   *
   * @param completer The value for completer.
   */
  protected void setCompleter(AtlantisOrderCompleter completer) {
    this.completer = completer;
  }

  @Override
  protected StringID getTemp() {
    return AtlantisConstants.OC_NEW;
  }

  @Override
  protected Set<StringID> getCommands() {
    return super.getCommands();
  }

  @Override
  protected Collection<OrderHandler> getHandlers() {
    return super.getHandlers();
  }

  @Override
  protected List<OrderHandler> getHandlers(OrderToken t) {
    return super.getHandlers(t);
  }

  @Override
  protected void setErrMsg(String errMsg) {
    super.setErrMsg(errMsg);
  }

  @Override
  protected boolean isID(String txt) {
    return super.isID(txt);
  }

  @Override
  protected boolean isID(String txt, boolean allowTemp) {
    return super.isID(txt, allowTemp);
  }

  @Override
  protected boolean isTempID(String txt) {
    return super.isTempID(txt);
  }
}
