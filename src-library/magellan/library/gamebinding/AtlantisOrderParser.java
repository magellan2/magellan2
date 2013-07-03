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
package magellan.library.gamebinding;

import magellan.library.EntityID;
import magellan.library.GameData;
import magellan.library.completion.OrderParser;
import magellan.library.utils.OrderToken;

/**
 * @author stm
 */
public class AtlantisOrderParser extends AbstractOrderParser {

  protected AtlantisOrderCompleter completer;

  /**
   * @param data
   */
  public AtlantisOrderParser(GameData data) {
    super(data);
  }

  /**
   * @param data
   * @param cc
   */
  public AtlantisOrderParser(GameData data, AtlantisOrderCompleter cc) {
    super(data, cc);
  }

  /**
   * @see magellan.library.gamebinding.AbstractOrderParser#initCommands()
   */
  @Override
  protected void initCommands() {
    clearCommandMap();

    // WORK
    addCommand(AtlantisConstants.O_WORK, new WorkReader(this));
    // FORM u1
    addCommand(AtlantisConstants.O_FORM, new FormReader(this));
    // ACCEPT f1
    addCommand(AtlantisConstants.O_ACCEPT, new AcceptReader(this));
    // ADDRESS Address
    addCommand(AtlantisConstants.O_ADDRESS, new AddressReader(this));
    // ADMIT f1
    addCommand(AtlantisConstants.O_ADMIT, new AdmitReader(this));
    // ALLY f1 01
    addCommand(AtlantisConstants.O_ALLY, new AllyReader(this));
    // BEHIND 01
    addCommand(AtlantisConstants.O_BEHIND, new BehindReader(this));
    // COMBAT spell
    addCommand(AtlantisConstants.O_COMBAT, new CombatReader(this));
    // DISPLAY (UNIT | BUILDING SHIP) string
    addCommand(AtlantisConstants.O_DISPLAY, new DisplayReader(this));
    // GUARD 01
    addCommand(AtlantisConstants.O_GUARD, new GuardReader(this));
    // NAME (FACTION | UNIT | BUILDING | SHIP) name
    addCommand(AtlantisConstants.O_NAME, new NameReader(this));
    // PASSWORD password
    addCommand(AtlantisConstants.O_PASSWORD, new PasswordReader(this));
    // RESHOW spell
    addCommand(AtlantisConstants.O_RESHOW, new ReshowReader(this));
    // FIND f1
    addCommand(AtlantisConstants.O_FIND, new FindReader(this));
    // BOARD s1
    addCommand(AtlantisConstants.O_BOARD, new BoardReader(this));
    // ENTER b1
    addCommand(AtlantisConstants.O_ENTER, new EnterReader(this));
    // LEAVE
    addCommand(AtlantisConstants.O_LEAVE, new LeaveReader(this));
    // PROMOTE u1
    addCommand(AtlantisConstants.O_PROMOTE, new PromoteReader(this));
    // ATTACK (u1 | PEASANTS)
    addCommand(AtlantisConstants.O_ATTACK, new AttackReader(this));
    // DEMOLISH
    addCommand(AtlantisConstants.O_DEMOLISH, new DemolishReader(this));
    // GIVE u1 1 item
    addCommand(AtlantisConstants.O_GIVE, new GiveReader(this));
    // PAY u1 1
    addCommand(AtlantisConstants.O_PAY, new PayReader(this));
    // SINK
    addCommand(AtlantisConstants.O_SINK, new SinkReader(this));
    // TRANSFER (u1 | PEASANTS) 1
    addCommand(AtlantisConstants.O_TRANSFER, new TransferReader(this));
    // TAX
    addCommand(AtlantisConstants.O_TAX, new TaxReader(this));
    // RECRUIT 1
    addCommand(AtlantisConstants.O_RECRUIT, new RecruitReader(this));
    // QUIT password
    addCommand(AtlantisConstants.O_QUIT, new QuitReader(this));
    // MOVE (N | W | M | S | W | Y)
    addCommand(AtlantisConstants.O_MOVE, new MoveReader(this));
    // SAIL (N | W | M | S | W | Y)
    addCommand(AtlantisConstants.O_SAIL, new SailReader(this));
    // BUILD (BUILDING [b1]) | (SHIP [s1|type])
    addCommand(AtlantisConstants.O_BUILD, new BuildReader(this));
    // ENTERTAIN
    addCommand(AtlantisConstants.O_ENTERTAIN, new EntertainReader(this));
    // PRODUCE item
    addCommand(AtlantisConstants.O_PRODUCE, new ProduceReader(this));
    // RESEARCH [1]
    addCommand(AtlantisConstants.O_RESEARCH, new ResearchReader(this));
    // STUDY skill
    addCommand(AtlantisConstants.O_STUDY, new StudyReader(this));
    // TEACH u1+
    addCommand(AtlantisConstants.O_TEACH, new TeachReader(this));
    // WORK
    addCommand(AtlantisConstants.O_WORK, new WorkReader(this));
    // CAST spell
    addCommand(AtlantisConstants.O_CAST, new CastReader(this));

  }

  protected class IdHandler extends OrderHandler {

    protected EntityID id;

    protected IdHandler(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isID(t.getText(), false)) {
        retVal = readFinalID(t);
        id = EntityID.createEntityID(t.getText(), getData().base);
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        completeId();
      }

      return retVal;
    }

    protected void completeId() {
      getCompleter().cmpltId();
    }
  }

  protected class FlagHandler extends OrderHandler {

    int flag;

    protected FlagHandler(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isNumeric(t.getText(), 10, 0, 1)) {
        retVal = readFinalID(t);
        flag = Integer.parseInt(t.getText());
      } else {
        unexpected(t);
      }

      if (shallComplete(token, t)) {
        completeFlag();
      }

      return retVal;
    }

    private void completeFlag() {
      getCompleter().cmpltFlag();
    }
  }

  protected class StringHandler extends OrderHandler {

    private String string;

    protected StringHandler(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      boolean retVal = false;
      token.ttype = OrderToken.TT_KEYWORD;

      OrderToken t = getNextToken();

      if (isString(t)) {
        StringChecker checker = new StringChecker(false, false, true, false);
        retVal = checker.read(t);
        string = checker.content;
      } else {
        unexpected(t);
      }

      return retVal;
    }
  }

  protected class FormReader extends IdHandler {
    public FormReader(OrderParser parser) {
      super(parser);
    }

  }

  protected class AcceptReader extends IdHandler {
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
        id = EntityID.createEntityID(t.getText(), getData().base);
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
        getCompleter().cmpltFlag();
      }

      return retVal;
    }
  }

  protected class BehindReader extends FlagHandler {
    // BEHIND 01
    public BehindReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      return checkNextFinal();
    }
  }

  protected class CombatReader extends StringHandler {
    // COMBAT spell
    public CombatReader(OrderParser parser) {
      super(parser);
    }
  }

  protected class DisplayReader extends OrderHandler {
    // DISPLAY (UNIT | BUILDING SHIP) string
    public DisplayReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      return checkNextFinal();
    }
  }

  protected class GuardReader extends OrderHandler {
    // GUARD 01
    public GuardReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      return checkNextFinal();
    }
  }

  protected class NameReader extends OrderHandler {
    // NAME (FACTION | UNIT | BUILDING | SHIP) name
    public NameReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      return checkNextFinal();
    }
  }

  protected class PasswordReader extends OrderHandler {
    // PASSWORD password
    public PasswordReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      return checkNextFinal();
    }
  }

  protected class ReshowReader extends OrderHandler {
    // RESHOW spell
    public ReshowReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      return checkNextFinal();
    }
  }

  protected class FindReader extends OrderHandler {
    // FIND f1
    public FindReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      return checkNextFinal();
    }
  }

  protected class BoardReader extends OrderHandler {
    // BOARD s1
    public BoardReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      return checkNextFinal();
    }
  }

  protected class EnterReader extends OrderHandler {
    // ENTER b1
    public EnterReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      return checkNextFinal();
    }
  }

  protected class LeaveReader extends OrderHandler {
    // LEAVE
    public LeaveReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      return checkNextFinal();
    }
  }

  protected class PromoteReader extends OrderHandler {
    // PROMOTE u1
    public PromoteReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      return checkNextFinal();
    }
  }

  protected class AttackReader extends OrderHandler {
    // ATTACK (u1 | PEASANTS)
    public AttackReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      return checkNextFinal();
    }
  }

  protected class DemolishReader extends OrderHandler {
    // DEMOLISH
    public DemolishReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      return checkNextFinal();
    }
  }

  protected class GiveReader extends OrderHandler {
    // GIVE u1 1 item
    public GiveReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      return checkNextFinal();
    }
  }

  protected class PayReader extends OrderHandler {
    // PAY u1 1
    public PayReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      return checkNextFinal();
    }
  }

  protected class SinkReader extends OrderHandler {
    // SINK
    public SinkReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      return checkNextFinal();
    }
  }

  protected class TransferReader extends OrderHandler {
    // TRANSFER (u1 | PEASANTS) 1
    public TransferReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      return checkNextFinal();
    }
  }

  protected class TaxReader extends OrderHandler {
    // TAX
    public TaxReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      return checkNextFinal();
    }
  }

  protected class RecruitReader extends OrderHandler {
    // RECRUIT 1
    public RecruitReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      return checkNextFinal();
    }
  }

  protected class QuitReader extends OrderHandler {
    // QUIT password
    public QuitReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      return checkNextFinal();
    }
  }

  protected class MoveReader extends OrderHandler {
    // MOVE (N | W | M | S | W | Y)
    public MoveReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      return checkNextFinal();
    }
  }

  protected class SailReader extends OrderHandler {
    // SAIL (N | W | M | S | W | Y)
    public SailReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      return checkNextFinal();
    }
  }

  protected class BuildReader extends OrderHandler {
    // BUILD (BUILDING [b1]) | (SHIP [s1|type])
    public BuildReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      return checkNextFinal();
    }
  }

  protected class EntertainReader extends OrderHandler {
    // ENTERTAIN
    public EntertainReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      return checkNextFinal();
    }
  }

  protected class ProduceReader extends OrderHandler {
    // PRODUCE item
    public ProduceReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      return checkNextFinal();
    }
  }

  protected class ResearchReader extends OrderHandler {
    // RESEARCH [1]
    public ResearchReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      return checkNextFinal();
    }
  }

  protected class StudyReader extends OrderHandler {
    // STUDY skill
    public StudyReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      return checkNextFinal();
    }
  }

  protected class TeachReader extends OrderHandler {
    // TEACH u1+
    public TeachReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      return checkNextFinal();
    }
  }

  protected class WorkReader extends OrderHandler {
    // WORK
    public WorkReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      return checkNextFinal();
    }
  }

  protected class CastReader extends OrderHandler {
    // CAST spell
    public CastReader(OrderParser parser) {
      super(parser);
    }

    @Override
    protected boolean readIt(OrderToken token) {
      token.ttype = OrderToken.TT_KEYWORD;

      getOrder().setLong(true);
      return checkNextFinal();
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
  @Override
  protected void setCompleter(AbstractOrderCompleter completer) {
    this.completer = (AtlantisOrderCompleter) completer;
  }
}
