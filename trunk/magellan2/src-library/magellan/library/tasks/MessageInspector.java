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

import magellan.library.Battle;
import magellan.library.Building;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.IntegerID;
import magellan.library.Message;
import magellan.library.Region;
import magellan.library.Unit;
import magellan.library.impl.MagellanMessageImpl;
import magellan.library.rules.MessageType;
import magellan.library.tasks.Problem.Severity;
import magellan.library.tasks.ProblemFactory.MessageProblem;
import magellan.library.utils.Resources;

/**
 * An inspector that displays all or selected (faction, region unit) messages.
 * 
 * @author stm
 * @version 1.0, Nov 22, 2010
 */
public class MessageInspector extends AbstractInspector {

  public static final ProblemType MESSAGE_PROBLEM = ProblemType.create("tasks.messageinspector",
      "message");

  private static final MessageType FACTIONEFFECT = new MessageType(IntegerID.create(-42),
      "FACTION--EFFECT");
  private static final MessageType FACTIONERROR = new MessageType(IntegerID.create(-43),
      "REGION--ERROR");
  private static final MessageType REGIONEFFECT = new MessageType(IntegerID.create(-44),
      "REGION--EFFECT");
  private static final MessageType BUILDINGEFFECT = new MessageType(IntegerID.create(-45),
      "BUILDING--EFFECT");
  private static final MessageType UNITEFFECT = new MessageType(IntegerID.create(-46),
      "UNIT--EFFECT");
  private static final MessageType BATTLE = new MessageType(IntegerID.create(-47), "BATTLE");

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
    // problemTypes.put(UNITEFFECT.getID(), createProblemType(UNITEFFECT));
    problemTypes.put(BATTLE.getID(), ProblemType.create("tasks.messageinspector", "battle"));
  }

  private String getPhrase(String s) {
    if (s.indexOf('.') < 0)
      return s;
    return s.substring(0, s.indexOf('.'));
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
      suppressedProblems.add(((MessageProblem) p).getReportMessage());
    }
    return null;
  }

  @Override
  public void unSuppress(Unit u) {
    suppressedProblems.clear();
  }

  @Override
  public List<Problem> reviewGlobal() {
    return super.reviewGlobal();
  }

  @Override
  public List<Problem> reviewFaction(Faction f) {
    List<Problem> problems = new LinkedList<Problem>();
    if (f.getMessages() != null) {
      for (Message m : f.getMessages()) {
        if (!suppressedProblems.contains(m))
          if (!suppressedTypes.contains(getProblemType(m))) {
            problems.add(ProblemFactory.createProblem(getData(), m, f, null, null, null, this));
          }
      }
    }
    if (f.getBattles() != null) {
      for (Battle b : f.getBattles()) {
        Region region = getData().getRegion(b.getID());
        Message m =
            new MagellanMessageImpl(Message.ambiguousID, Resources.get(
                "tasks.messageinspector.battle.message", region), BATTLE, null);
        if (!suppressedProblems.contains(m))
          if (!suppressedTypes.contains(getProblemType(m))) {
            problems.add(ProblemFactory.createProblem(getData(), m, f, region, null, null, this));
          }
      }
    }

    if (f.getEffects() != null) {
      for (String s : f.getEffects()) {
        MagellanMessageImpl m =
            new MagellanMessageImpl(Message.ambiguousID, s, FACTIONEFFECT, null);
        if (!suppressedProblems.contains(m))
          if (!suppressedTypes.contains(getProblemType(m))) {
            problems.add(ProblemFactory.createProblem(getData(), m, f, null, null, null, this));
          }
      }
    }

    if (f.getErrors() != null) {
      for (String s : f.getErrors()) {
        MagellanMessageImpl m = new MagellanMessageImpl(Message.ambiguousID, s, FACTIONERROR, null);
        if (!suppressedProblems.contains(m))
          if (!suppressedTypes.contains(getProblemType(m))) {
            problems.add(ProblemFactory.createProblem(getData(), m, f, null, null, null, this));
          }
      }

    }

    return problems;
  }

  @Override
  public List<Problem> reviewRegion(Region r, Severity severity) {
    if (!Severity.INFORMATION.equals(severity))
      return Collections.emptyList();

    List<Problem> problems = new LinkedList<Problem>();

    if (r.getMessages() != null) {
      for (Message m : r.getMessages()) {
        if (!suppressedProblems.contains(m))
          if (!suppressedTypes.contains(getProblemType(m))) {
            problems.add(ProblemFactory.createProblem(getData(), m, null, r, null, null, this));
          }
      }
    }
    if (r.getEvents() != null) {
      for (Message m : r.getEvents()) {
        if (!suppressedProblems.contains(m))
          if (!suppressedTypes.contains(getProblemType(m))) {
            problems.add(ProblemFactory.createProblem(getData(), m, null, r, null, null, this));
          }
      }
    }

    if (r.getPlayerMessages() != null) {
      for (Message m : r.getPlayerMessages()) {
        if (!suppressedProblems.contains(m))
          if (!suppressedTypes.contains(getProblemType(m))) {
            problems.add(ProblemFactory.createProblem(getData(), m, null, r, null, null, this));
          }
      }
    }

    if (r.getEffects() != null) {
      for (String s : r.getEffects()) {
        MagellanMessageImpl m = new MagellanMessageImpl(Message.ambiguousID, s, REGIONEFFECT, null);
        if (!suppressedProblems.contains(m))
          if (!suppressedTypes.contains(getProblemType(m))) {
            problems.add(ProblemFactory.createProblem(getData(), m, null, r, null, null, this));
          }
      }
    }

    for (Building b : r.buildings()) {
      if (b.getEffects() != null) {
        for (String s : b.getEffects()) {
          MagellanMessageImpl m =
              new MagellanMessageImpl(Message.ambiguousID, s, BUILDINGEFFECT, null);
          if (!suppressedProblems.contains(m))
            if (!suppressedTypes.contains(getProblemType(m))) {
              problems.add(ProblemFactory.createProblem(getData(), m, null, r, null, b, this));
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
        if (!suppressedProblems.contains(m))
          if (!suppressedTypes.contains(getProblemType(m))) {
            problems.add(ProblemFactory.createProblem(getData(), m, null, null, u, null, this));
          }
      }
    }

    if (u.getEffects() != null) {
      for (String s : u.getEffects()) {
        MagellanMessageImpl m = new MagellanMessageImpl(Message.ambiguousID, s, UNITEFFECT, null);
        if (!suppressedProblems.contains(m))
          if (!suppressedTypes.contains(getProblemType(m))) {
            problems.add(ProblemFactory.createProblem(getData(), m, null, null, u, null, this));
          }
      }
    }

    return problems;
  }

  public static Inspector getInstance(GameData gameData) {
    return new MessageInspector(gameData);
  }

}
