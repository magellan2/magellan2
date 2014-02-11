/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.library.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import magellan.library.Building;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.rules.CastleType;
import magellan.library.tasks.Problem.Severity;

/**
 * This class inspects buildings.
 */
public class BuildingInspector extends AbstractInspector {

  enum BuildingProblemTypes {
    EMPTY_BUILDING, EMPTY_CASTLE, EMPTY_BUILDING_FUSSY, EMPTY_CASTLE_FUSSY, OVERLOADED, BESIEGED,
    ILLEGAL_REGION;

    private ProblemType type;

    BuildingProblemTypes() {
      String name = name().toLowerCase();
      type = ProblemType.create("tasks.buildinginspector", name);
    }

    ProblemType getType() {
      return type;
    }
  }

  /**
   * Returns an instance of BuildingInspector.
   * 
   * @return The singleton instance of BuildingInspector
   */
  public static BuildingInspector getInstance(GameData data) {
    return new BuildingInspector(data);
  }

  private Collection<ProblemType> types;

  protected BuildingInspector(GameData data) {
    super(data);
  }

  /**
   * Reviews the region for buildings with problems.
   * 
   * @see AbstractInspector#listProblems(magellan.library.Region)
   */
  @Override
  public List<Problem> listProblems(Region r) {
    if (r == null)
      return Collections.emptyList();

    // this inspector is only interested in buildings
    if ((r.buildings() == null) || r.buildings().isEmpty())
      return Collections.emptyList();

    List<Problem> problems = reviewBuildings(r);

    if (problems.isEmpty())
      return Collections.emptyList();
    else
      return problems;
  }

  private List<Problem> reviewBuildings(Region r) {
    List<Problem> problems = new ArrayList<Problem>(2);

    for (Building b : r.buildings()) {
      problems.addAll(reviewBuilding(b));
    }

    return problems;
  }

  private List<Problem> reviewBuilding(Building b) {
    if (checkIgnoreUnitContainer(b))
      return Collections.emptyList();

    List<Problem> problems = new ArrayList<Problem>();

    if (b.modifiedUnits().isEmpty()) {
      if (b.getRegion().units().isEmpty()) {
        if (b.getBuildingType() instanceof CastleType) {
          problems.add(ProblemFactory.createProblem(Severity.WARNING,
              BuildingProblemTypes.EMPTY_CASTLE_FUSSY.getType(), b, this));
        } else {
          problems.add(ProblemFactory.createProblem(Severity.WARNING,
              BuildingProblemTypes.EMPTY_BUILDING_FUSSY.getType(), b, this));
        }
      } else {
        if (b.getBuildingType() instanceof CastleType) {
          problems.add(ProblemFactory.createProblem(Severity.WARNING,
              BuildingProblemTypes.EMPTY_CASTLE.getType(), b, this));
        } else {
          problems.add(ProblemFactory.createProblem(Severity.WARNING,
              BuildingProblemTypes.EMPTY_BUILDING.getType(), b, this));
        }
      }
    }

    return problems;
  }

  /**
   * Reviews the region for buildings with problems.
   * 
   * @see magellan.library.tasks.AbstractInspector#findProblems(magellan.library.Unit)
   */
  @Override
  public List<Problem> findProblems(Unit unit) {
    UnitContainer uc = unit.getModifiedUnitContainer();
    if (uc == null)
      return Collections.emptyList();
    if (!(uc instanceof Building))
      return Collections.emptyList();
    if (uc.getModifiedOwnerUnit() == null || !uc.getModifiedOwnerUnit().equals(unit))
      return Collections.emptyList();

    Building b = (Building) uc;
    List<Problem> problems = new ArrayList<Problem>(2);

    // Unit owner = b.getOwnerUnit();
    // Unit newOwner = b.getModifiedOwnerUnit();
    int inmates = 0;
    for (Unit u : b.modifiedUnits()) {
      inmates += u.getModifiedPersons();
    }
    if (b.getSize() < inmates) {
      problems.add(ProblemFactory.createProblem(Severity.WARNING, BuildingProblemTypes.OVERLOADED
          .getType(), b, this));
    }

    if (!b.getBuildingType().containsRegionType(b.getRegion().getRegionType())) {
      problems.add(ProblemFactory.createProblem(Severity.ERROR, BuildingProblemTypes.ILLEGAL_REGION
          .getType(), b, this));
    }

    if (b.getBesiegers() > 0) {
      problems.add(ProblemFactory.createProblem(Severity.WARNING, BuildingProblemTypes.BESIEGED
          .getType(), b, this));
    }

    // building maintenance is checked by order processor

    return problems;
  }

  public Collection<ProblemType> getTypes() {
    if (types == null) {
      types = new LinkedList<ProblemType>();
      for (BuildingProblemTypes t : BuildingProblemTypes.values()) {
        types.add(t.getType());
      }
    }
    return types;
  }
}
