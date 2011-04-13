// class magellan.library.tasks.MessageInspector
// created on Nov 22, 2010
//
// Copyright 2003-2010 by magellan project team
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
package magellan.library.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import magellan.library.Battle;
import magellan.library.Building;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.IntegerID;
import magellan.library.Message;
import magellan.library.Region;
import magellan.library.Ship;
import magellan.library.Unit;
import magellan.library.impl.MagellanMessageImpl;
import magellan.library.rules.MessageType;
import magellan.library.tasks.Problem.Severity;
import magellan.library.tasks.ProblemFactory.BattleProblem;
import magellan.library.tasks.ProblemFactory.MessageProblem;
import magellan.library.utils.Resources;

/**
 * An inspector that displays all or selected (faction, region unit) messages.
 * 
 * @author stm
 * @version 1.0, Nov 22, 2010
 */
public class MessageInspector extends AbstractInspector {

  // public static final ProblemType MESSAGE_PROBLEM = ProblemType.create("tasks.messageinspector",
  // "message");

  private static final MessageType FACTIONEFFECT = new MessageType(IntegerID.create(-42),
      "FACTION--EFFECT");
  private static final MessageType FACTIONERROR = new MessageType(IntegerID.create(-43),
      "REGION--ERROR");
  private static final MessageType REGIONEFFECT = new MessageType(IntegerID.create(-44),
      "REGION--EFFECT");
  private static final MessageType BUILDINGEFFECT = new MessageType(IntegerID.create(-45),
      "BUILDING--EFFECT");
  private static final MessageType SHIPEFFECT = new MessageType(IntegerID.create(-45),
      "SHIP--EFFECT");
  private static final MessageType UNITEFFECT = new MessageType(IntegerID.create(-46),
      "UNIT--EFFECT");
  protected static final MessageType BATTLE = new MessageType(IntegerID.create(-47), "BATTLE");

  private static final int C_FACTION = 1;
  private static final int C_REGION = 2;
  private static final int C_UNIT = 3;

  private Set<ProblemType> suppressedTypes = new HashSet<ProblemType>();
  private Set<Message> suppressedProblems = new HashSet<Message>();
  private Map<IntegerID, ProblemType> problemTypes = new HashMap<IntegerID, ProblemType>();
  private Map<String, ProblemType> effectTypes = new HashMap<String, ProblemType>();

  /**
   * @param data
   */
  public MessageInspector(GameData data) {
    super(data);
    initTypes();
  }

  private void initTypes() {
    for (MessageType mt : getData().msgTypes().values()) {
      problemTypes.put(mt.getID(), createProblemType(mt));
    }
    // add faction effects
    Set<String> effects = new HashSet<String>();
    for (Faction f : getData().getFactions()) {
      if (f.getEffects() != null) {
        for (String s : f.getEffects()) {
          effects.add(getPhrase(s));
        }
      }
      if (f.getErrors() != null) {
        for (String s : f.getErrors()) {
          effects.add(getPhrase(s));
        }
      }
    }
    for (String effect : effects) {
      effectTypes.put(effect, createProblemType(effect, C_FACTION));
    }

    // add region effects
    effects.clear();
    for (Region r : getData().getRegions()) {
      if (r.getEffects() != null) {
        for (String s : r.getEffects()) {
          effects.add(getPhrase(s));
        }
      }

      for (Building b : r.buildings()) {
        if (b.getEffects() != null) {
          for (String s : b.getEffects()) {
            effects.add(getPhrase(s));
          }
        }
      }

      for (Ship s : r.ships()) {
        if (s.getEffects() != null) {
          for (String eff : s.getEffects()) {
            effects.add(getPhrase(eff));
          }
        }
      }
    }
    for (String effect : effects) {
      effectTypes.put(effect, createProblemType(effect, C_REGION));
    }

    // add unit effects
    effects.clear();
    for (Unit u : getData().getUnits()) {
      if (u.getEffects() != null) {
        for (String s : u.getEffects()) {
          effects.add(getPhrase(s));
        }
      }
    }
    for (String effect : effects) {
      effectTypes.put(effect, createProblemType(effect, C_UNIT));
    }

    // problemTypes.put(FACTIONEFFECT.getID(), createProblemType(FACTIONEFFECT));
    // problemTypes.put(FACTIONERROR.getID(), createProblemType(FACTIONERROR));
    // problemTypes.put(REGIONEFFECT.getID(), createProblemType(REGIONEFFECT));
    // problemTypes.put(BUILDINGEFFECT.getID(), createProblemType(BUILDINGEFFECT));
    // problemTypes.put(SHIPEFFECT.getID(), createProblemType(SHIPEFFECT));
    // problemTypes.put(UNITEFFECT.getID(), createProblemType(UNITEFFECT));
    problemTypes.put(BATTLE.getID(), ProblemType.create("tasks.messageinspector", "battle"));
  }

  private String getPhrase(String s) {
    String result = s;
    if (s.indexOf('.') >= 0) {
      result = s.substring(0, s.indexOf('.'));
    }
    try {
      // hack to simplify messages like "2 Gehirmschmalz"
      StringTokenizer tok = new StringTokenizer(result);
      String number = tok.nextToken();
      int n = Integer.parseInt(number);
      if (n > 1) {
        result = "#" + result.substring(number.length());
      }
    } catch (Exception e) {
      // no number
    }
    return result.trim();
  }

  private ProblemType createProblemType(String effect, int category) {
    switch (category) {
    case C_FACTION:
      return new ProblemType(Resources.get("tasks.messageinspector.factioneffect.name", effect),
          Resources.get("tasks.messageinspector.message.group"), Resources.get(
              "tasks.messageinspector.factioneffect.description", effect), null);
    case C_REGION:
      return new ProblemType(Resources.get("tasks.messageinspector.regioneffect.name", effect),
          Resources.get("tasks.messageinspector.message.group"), Resources.get(
              "tasks.messageinspector.regioneffect.description", effect), null);
    case C_UNIT:
      return new ProblemType(Resources.get("tasks.messageinspector.uniteffect.name", effect),
          Resources.get("tasks.messageinspector.message.group"), Resources.get(
              "tasks.messageinspector.uniteffect.description", effect), null);
    default:
      throw new IllegalStateException("unknown effect type");
    }
  }

  private ProblemType createProblemType(MessageType mt) {
    return new ProblemType(Resources.get("tasks.messageinspector.message.name", mt.getID()),
        Resources.get("tasks.messageinspector.message.group"), Resources.get(
            "tasks.messageinspector.message.description", mt.getPattern()), null);
  }

  public ProblemType getProblemType(Battle battle) {
    return problemTypes.get(BATTLE.getID());
  }

  public ProblemType getProblemType(Message message) {
    if (message.getMessageType().getID().intValue() > 0)
      return problemTypes.get(message.getMessageType().getID());
    else if (message.getMessageType().getID() == BATTLE.getID())
      return problemTypes.get(BATTLE.getID());
    else
      return effectTypes.get(getPhrase(message.getText()));
  }

  /**
   * @see magellan.library.tasks.Inspector#getTypes()
   */
  public Collection<ProblemType> getTypes() {
    ArrayList<ProblemType> result =
        new ArrayList<ProblemType>(problemTypes.size() + effectTypes.size());
    result.addAll(problemTypes.values());
    result.addAll(effectTypes.values());
    return result;
  }

  @Override
  public void setIgnore(ProblemType type, boolean ignore) {
    if (ignore) {
      suppressedTypes.add(type);
    } else {
      suppressedTypes.remove(type);
    }
  }

  /**
   * @see magellan.library.tasks.AbstractInspector#suppress(magellan.library.tasks.Problem)
   */
  @Override
  public Unit suppress(Problem p) {
    if (p instanceof MessageProblem) {
      ((MessageProblem) p).getReportMessage().setAcknowledged(true);
      suppressedProblems.add(((MessageProblem) p).getReportMessage());
    } else if (p instanceof BattleProblem) {
      ((BattleProblem) p).getReportMessage().setAcknowledged(true);
      suppressedProblems.add(((BattleProblem) p).getReportMessage());
    }

    return null;
  }

  @Override
  public void unSuppress(Unit u) {
    for (Message m : suppressedProblems) {
      m.setAcknowledged(false);
    }
    suppressedProblems.clear();
  }

  @Override
  public void unSuppress(Region r) {
    if (r.getMessages() != null) {
      for (Message m : r.getMessages()) {
        m.setAcknowledged(false);
      }
    }
    if (r.getEvents() != null) {
      for (Message m : r.getEvents()) {
        m.setAcknowledged(false);
      }
    }

    if (r.getPlayerMessages() != null) {
      for (Message m : r.getPlayerMessages()) {
        m.setAcknowledged(false);
      }
    }

    if (r.getEffects() != null) {
      // FIXME do something
    }

    for (Building b : r.buildings()) {
      if (b.getEffects() != null) {
        // FIXME
      }
    }

    for (Ship s : r.ships()) {
      if (s.getEffects() != null) {
        // FIXME
      }
    }
  }

  @Override
  public void unSuppress(Faction f) {
    if (f.getMessages() != null) {
      for (Message m : f.getMessages()) {
        m.setAcknowledged(false);
      }
    }
    if (f.getBattles() != null) {
      for (Battle b : f.getBattles()) {
        setAcknowledged(b, false);
      }
    }

    if (f.getEffects() != null) {
      // FIXME do something
    }

    if (f.getErrors() != null) {
      // FIXME do something
    }
  }

  @Override
  public List<Problem> reviewFaction(Faction f) {
    List<Problem> problems = new LinkedList<Problem>();
    if (f.getMessages() != null) {
      for (Message m : f.getMessages()) {
        if (!m.isAcknowledged())
          if (!suppressedTypes.contains(getProblemType(m))) {
            problems.add(ProblemFactory.createProblem(getData(), m, f, null, null, null, this));
          }
      }
    }
    if (f.getBattles() != null) {
      for (Battle b : f.getBattles()) {
        Region region = getData().getRegion(b.getID());
        // Message m =
        // new MagellanMessageImpl(Message.ambiguousID, Resources.get(
        // "tasks.messageinspector.battle.message", region), BATTLE, null);
        // m.setAcknowledged(b.isAcknowledged());
        if (isAcknowledged(b))
          if (!suppressedTypes.contains(getProblemType(b))) {
            problems.add(ProblemFactory.createProblem(getData(), b, f, region, null, null, this));
          }
      }
    }

    if (f.getEffects() != null) {
      for (String s : f.getEffects()) {
        MagellanMessageImpl m =
            new MagellanMessageImpl(Message.ambiguousID, s, FACTIONEFFECT, null);
        if (!m.isAcknowledged())
          if (!suppressedTypes.contains(getProblemType(m))) {
            problems.add(ProblemFactory.createProblem(getData(), m, f, null, null, null, this));
          }
      }
    }

    if (f.getErrors() != null) {
      for (String s : f.getErrors()) {
        MagellanMessageImpl m = new MagellanMessageImpl(Message.ambiguousID, s, FACTIONERROR, null);
        if (!m.isAcknowledged())
          if (!suppressedTypes.contains(getProblemType(m))) {
            problems.add(ProblemFactory.createProblem(getData(), m, f, null, null, null, this));
          }
      }

    }

    return problems;
  }

  protected static void setAcknowledged(Battle b, boolean value) {
    for (Message m : b.messages()) {
      m.setAcknowledged(value);
    }
  }

  protected static boolean isAcknowledged(Battle b) {
    for (Message m : b.messages())
      return m.isAcknowledged();
    return false;
  }

  @Override
  public List<Problem> reviewRegion(Region r, Severity severity) {
    if (!Severity.INFORMATION.equals(severity))
      return Collections.emptyList();

    List<Problem> problems = new LinkedList<Problem>();

    if (r.getMessages() != null) {
      for (Message m : r.getMessages()) {
        if (!m.isAcknowledged())
          if (!suppressedTypes.contains(getProblemType(m))) {
            problems.add(ProblemFactory.createProblem(getData(), m, null, r, null, null, this));
          }
      }
    }
    if (r.getEvents() != null) {
      for (Message m : r.getEvents()) {
        if (!m.isAcknowledged())
          if (!suppressedTypes.contains(getProblemType(m))) {
            problems.add(ProblemFactory.createProblem(getData(), m, null, r, null, null, this));
          }
      }
    }

    if (r.getPlayerMessages() != null) {
      for (Message m : r.getPlayerMessages()) {
        if (!m.isAcknowledged())
          if (!suppressedTypes.contains(getProblemType(m))) {
            problems.add(ProblemFactory.createProblem(getData(), m, null, r, null, null, this));
          }
      }
    }

    if (r.getEffects() != null) {
      for (String s : r.getEffects()) {
        MagellanMessageImpl m = new MagellanMessageImpl(Message.ambiguousID, s, REGIONEFFECT, null);
        if (!m.isAcknowledged())
          if (!suppressedTypes.contains(getProblemType(m))) {
            problems.add(ProblemFactory.createProblem(getData(), m, null, r, null, null, this));
          }
      }
    }

    for (Building b : r.buildings()) {
      if (b.getEffects() != null) {
        for (String eff : b.getEffects()) {
          MagellanMessageImpl m =
              new MagellanMessageImpl(Message.ambiguousID, eff, BUILDINGEFFECT, null);
          if (!m.isAcknowledged())
            if (!suppressedTypes.contains(getProblemType(m))) {
              problems.add(ProblemFactory.createProblem(getData(), m, null, r, null, b, this));
            }
        }
      }
    }

    for (Ship s : r.ships()) {
      if (s.getEffects() != null) {
        for (String eff : s.getEffects()) {
          MagellanMessageImpl m =
              new MagellanMessageImpl(Message.ambiguousID, eff, SHIPEFFECT, null);
          if (!m.isAcknowledged())
            if (!suppressedTypes.contains(getProblemType(m))) {
              problems.add(ProblemFactory.createProblem(getData(), m, null, r, null, s, this));
            }
        }
      }
    }

    return problems;
  }

  @Override
  public List<Problem> reviewUnit(Unit u, Severity severity) {
    if (!Severity.INFORMATION.equals(severity))
      return Collections.emptyList();

    List<Problem> problems = new LinkedList<Problem>();

    if (u.getUnitMessages() != null) {
      for (Message m : u.getUnitMessages()) {
        if (!m.isAcknowledged())
          if (!suppressedTypes.contains(getProblemType(m))) {
            problems.add(ProblemFactory.createProblem(getData(), m, null, null, u, null, this));
          }
      }
    }

    if (u.getEffects() != null) {
      for (String s : u.getEffects()) {
        MagellanMessageImpl m = new MagellanMessageImpl(Message.ambiguousID, s, UNITEFFECT, null);
        if (!m.isAcknowledged())
          if (!suppressedTypes.contains(getProblemType(m))) {
            problems.add(ProblemFactory.createProblem(getData(), m, null, null, u, null, this));
          }
      }
    }

    return problems;
  }

  /**
   * Returns an instance for the data.
   */
  public static Inspector getInstance(GameData gameData) {
    return new MessageInspector(gameData);
  }

}
