/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.library.tasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.gamebinding.GameSpecificStuff;
import magellan.library.tasks.Problem.Severity;
import magellan.library.utils.logging.Logger;

/**
 * This is an abstract implementation of an inspector. You can use this as a base for your own
 * implementation.
 * 
 * @author $Author: $
 * @version $Revision: 171 $
 */
public abstract class AbstractInspector implements Inspector {
  private static final Logger log = Logger.getInstance(AbstractInspector.class);

  public static final String SUPPRESS_LINE_PREFIX = SUPPRESS_PREFIX + "Line";

  private GameData data;

  private GameSpecificStuff gameSpecStuff;

  protected AbstractInspector(GameData data) {
    this.data = data;
  }

  /**
   * Calls reviewUnit(u,Problem.INFO), reviewUnit(u,Problem.WARNING)... etc. and returns the joint
   * list of problems.
   * 
   * @see magellan.library.tasks.Inspector#reviewUnit(magellan.library.Unit)
   */
  public List<Problem> reviewUnit(Unit u) {
    if (checkIgnoreUnit(u))
      return Collections.emptyList();

    List<Problem> li = reviewUnit(u, Severity.INFORMATION);
    List<Problem> lw = reviewUnit(u, Severity.WARNING);
    List<Problem> le = reviewUnit(u, Severity.ERROR);

    checkIgnore(li);
    checkIgnore(lw);
    checkIgnore(le);

    if (!li.isEmpty() || !lw.isEmpty() || !le.isEmpty()) {
      List<Problem> problems = new ArrayList<Problem>(10);
      problems.addAll(li);
      problems.addAll(lw);
      problems.addAll(le);
      return problems;
    }
    return Collections.emptyList();
  }

  private void checkIgnore(List<Problem> problems) {
    for (Iterator<Problem> it = problems.iterator(); it.hasNext();) {
      Problem p = it.next();
      Unit unit = p.getOwner();
      if (unit != null) {
        for (String line : unit.getOrders()) {
          if (isSuppressMarkerFor(line, p, false)) {
            it.remove();
            break;
          }
        }
        for (int l = p.getLine() - 2; l >= 0; --l) {
          if (l >= unit.getOrders().size())
            log.error("error in wrong line: " + unit + " " + l);
          String line = unit.getOrders().get(l);
          if (!line.startsWith(SUPPRESS_PREFIX))
            break;
          else if (isSuppressMarkerFor(line, p, true)) {
            it.remove();
            break;
          }
        }
      }
    }
  }

  protected boolean isSuppressMarkerFor(String line, Problem p, boolean lineMode) {
    if (lineMode)
      return line.equals(getSuppressLineComment(p.getType()));
    else
      return line.equals(getSuppressUnitComment(p.getType()));
  }

  /**
   * Returns <code>true</code> iff this unit's orders contain {@link #getSuppressComment()}.
   * Sub-classes should overwrite this to add more sophisticated ignoring of errors.
   * 
   * @param u
   * @return
   */
  protected boolean checkIgnoreUnit(Unit u) {
    for (ProblemType p : getTypes()) {
      boolean found = false;
      for (String order : u.getOrders())
        if (order.equals(getSuppressUnitComment(p)))
          found = true;
      if (!found)
        return false;
    }
    return false;
  }

  /**
   * Returns <code>true</code> iff this container's owner unit's orders contain
   * {@link #getSuppressComment()}. Sub-classes should overwrite this to add more sophisticated
   * ignoring of errors.
   * 
   * @param u
   * @return
   */
  protected boolean checkIgnoreUnitContainer(UnitContainer c) {
    // TODO should we use getModifiedOnwerUnit() here?
    if (c.getOwnerUnit() == null)
      return false;
    return checkIgnoreUnit(c.getOwnerUnit());
  }

  /**
   * Returns an empty list. Sub-classes should usually overwrite this method or
   * {@link #reviewRegion(Region, int)} (or both).
   * 
   * @see magellan.library.tasks.Inspector#reviewUnit(magellan.library.Unit, int)
   */
  public List<Problem> reviewUnit(Unit u, Severity severity) {
    return Collections.emptyList();
  }

  /**
   * Calls reviewUnit(r,Problem.INFO), reviewUnit(r,Problem.WARNING)... etc. and returns the joint
   * list of problems.
   * 
   * @see magellan.library.tasks.Inspector#reviewRegion(magellan.library.Region)
   */
  public List<Problem> reviewRegion(Region r) {
    if (checkIgnoreRegion(r))
      return Collections.emptyList();

    List<Problem> li = reviewRegion(r, Severity.INFORMATION);
    List<Problem> lw = reviewRegion(r, Severity.WARNING);
    List<Problem> le = reviewRegion(r, Severity.ERROR);

    checkIgnore(li);
    checkIgnore(lw);
    checkIgnore(le);

    if (!li.isEmpty() || !lw.isEmpty() || !le.isEmpty()) {
      List<Problem> problems = new ArrayList<Problem>(10);
      problems.addAll(li);
      problems.addAll(lw);
      problems.addAll(le);
      return problems;
    }
    return Collections.emptyList();
  }

  /**
   * Returns <code>false</code>. Sub-classes should overwrite this method to add more sophisticated
   * ignoring of errors.
   * 
   * @param u
   * @return <code>true</code> iff this region should be ignored for problems
   */
  protected boolean checkIgnoreRegion(Region r) {
    return false;
  }

  /**
   * Returns an empty list. Sub-classes should usually overwrite this method or
   * {@link #reviewUnit(Unit, int)} (or both).
   * 
   * @see magellan.library.tasks.Inspector#reviewRegion(magellan.library.Region, int)
   */
  public List<Problem> reviewRegion(Region r, Severity severity) {
    return Collections.emptyList();
  }

  /**
   * @see magellan.library.tasks.Inspector#suppress(magellan.library.tasks.Problem)
   */
  public Unit suppress(Problem p) {
    if (p.getOwner() == null)
      return null;
    if (p.getLine() >= 0)
      p.getOwner().addOrderAt(p.getLine() - 1, getSuppressLineComment(p.getType()), true);
    else
      p.getOwner().addOrderAt(0, getSuppressUnitComment(p.getType()));
    return p.getOwner();
  }

  protected String getSuppressUnitComment(ProblemType p) {
    StringBuffer sb = new StringBuffer(SUPPRESS_PREFIX);
    sb.append(" ");
    sb.append(p.getName());
    return sb.toString();
  }

  protected String getSuppressLineComment(ProblemType p) {
    StringBuffer sb = new StringBuffer(SUPPRESS_LINE_PREFIX);
    sb.append(" ");
    sb.append(p.getName());
    return sb.toString();
  }

  public void unSuppress(Unit u) {
    List<String> newOrders = new ArrayList<String>(u.getOrders().size());
    boolean changed = false;
    for (String o : u.getOrders()) {
      boolean match = false;
      for (ProblemType p : getTypes()) {
        if (o.equals(getSuppressLineComment(p)) || o.equals(getSuppressUnitComment(p)))
          match = true;
      }
      if (match)
        changed = true;
      else
        newOrders.add(o);
    }
    if (changed)
      u.setOrders(newOrders);
  }

  public void setGameData(GameData gameData) {
    this.data = gameData;
    this.gameSpecStuff = data.getGameSpecificStuff();
  }

  public GameData getData() {
    return data;
  }
  
  public GameSpecificStuff getGameSpecificStuff() {
    return gameSpecStuff;
  }
}
