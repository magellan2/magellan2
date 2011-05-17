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

import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Order;
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

  /**
   * This prefix is prepended to suppress markers.
   * 
   * @see #suppress(Problem)
   */
  public static final String SUPPRESS_LINE_PREFIX = Inspector.SUPPRESS_PREFIX + "Line";

  /**
   * This prefix is prepended to suppress markers. It is a permanent comment
   * 
   * @see #suppress(Problem)
   */
  public static final String SUPPRESS_LINE_PREFIX_PERMANENT = Inspector.SUPPRESS_PREFIX_PERMANENT
      + "Line";

  private GameData data;

  private GameSpecificStuff gameSpecStuff;

  protected AbstractInspector(GameData data) {
    this.data = data;
    gameSpecStuff = data.getGameSpecificStuff();
  }

  /**
   * Returns the empty list. Inspectors should overwrite this if they need to add problems that
   * don't depend on a certain unit or region.
   * 
   * @see magellan.library.tasks.Inspector#reviewGlobal()
   */
  public List<Problem> reviewGlobal() {
    return Collections.emptyList();
  }

  /**
   * Returns the empty list. Inspectors should overwrite this if they need to add problems that only
   * depend on a certain unit or region.
   * 
   * @see magellan.library.tasks.Inspector#reviewFaction(Faction)
   */
  public List<Problem> reviewFaction(Faction f) {
    return Collections.emptyList();
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
        for (Order line : unit.getOrders2()) {
          if (isSuppressMarkerFor(line, p, false)) {
            it.remove();
            break;
          }
        }
        if (p.getLine() - 2 >= unit.getOrders2().size()) {
          AbstractInspector.log.error("error in wrong line: " + unit + " " + p.getLine(),
              new Exception());
        } else {
          for (int l = p.getLine() - 2; l >= 0; --l) {
            Order line = unit.getOrders2().get(l);
            if (!(line.getText().startsWith(Inspector.SUPPRESS_PREFIX) || line.getText()
                .startsWith(Inspector.SUPPRESS_PREFIX_PERMANENT))) {
              break;
            } else if (isSuppressMarkerFor(line, p, true)) {
              it.remove();
              break;
            }
          }
        }
      }
    }
  }

  protected boolean isSuppressMarkerFor(Order line, Problem p, boolean lineMode) {
    if (lineMode)
      return line.getText().equals(getSuppressLineComment(p.getType(), false))
          || line.getText().equals(getSuppressLineComment(p.getType(), true));
    else
      return line.getText().equals(getSuppressUnitComment(p.getType()))
          || line.getText().equals(getSuppressUnitComment(p.getType(), true));
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
      for (Order order : u.getOrders2()) {
        if (order.getText().equals(getSuppressUnitComment(p))) {
          found = true;
        }
      }
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
   * {@link #reviewRegion(Region, Severity)} (or both).
   * 
   * @see magellan.library.tasks.Inspector#reviewUnit(magellan.library.Unit,
   *      magellan.library.tasks.Problem.Severity)
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

    int size = li.size() + lw.size() + le.size();
    if (size > 0) {
      List<Problem> problems = new ArrayList<Problem>(size);
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
   * {@link #reviewUnit(Unit, Severity)} (or both).
   * 
   * @see magellan.library.tasks.Inspector#reviewRegion(magellan.library.Region, Severity)
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
    if (p.getLine() >= 0) {
      p.getOwner().addOrderAt(p.getLine() - 1, getSuppressLineComment(p.getType()), true);
    } else {
      p.getOwner().addOrderAt(0, getSuppressUnitComment(p.getType()), true);
    }
    return p.getOwner();
  }

  protected String getSuppressUnitComment(ProblemType p, boolean permanent) {
    StringBuffer sb =
        new StringBuffer(permanent ? Inspector.SUPPRESS_PREFIX_PERMANENT
            : Inspector.SUPPRESS_PREFIX);
    sb.append(" ");
    sb.append(p.getName());
    return sb.toString();
  }

  protected String getSuppressLineComment(ProblemType p, boolean permanent) {
    StringBuffer sb =
        new StringBuffer(permanent ? AbstractInspector.SUPPRESS_LINE_PREFIX_PERMANENT
            : AbstractInspector.SUPPRESS_LINE_PREFIX);
    sb.append(" ");
    sb.append(p.getName());
    return sb.toString();
  }

  protected String getSuppressUnitComment(ProblemType p) {
    StringBuffer sb = new StringBuffer(Inspector.SUPPRESS_PREFIX);
    sb.append(" ");
    sb.append(p.getName());
    return sb.toString();
  }

  protected String getSuppressLineComment(ProblemType p) {
    StringBuffer sb = new StringBuffer(AbstractInspector.SUPPRESS_LINE_PREFIX);
    sb.append(" ");
    sb.append(p.getName());
    return sb.toString();
  }

  /**
   * @see magellan.library.tasks.Inspector#unSuppress(magellan.library.Unit)
   */
  public void unSuppress(Unit u) {
    List<Order> newOrders = new ArrayList<Order>(u.getOrders2().size());
    boolean changed = false;
    for (Order o : u.getOrders2()) {
      boolean match = false;
      for (ProblemType p : getTypes()) {
        if (o.getText().equals(getSuppressLineComment(p))
            || o.getText().equals(getSuppressUnitComment(p))) {
          match = true;
        }
      }
      if (match) {
        changed = true;
      } else {
        newOrders.add(o);
      }
    }
    if (changed) {
      u.setOrders2(newOrders);
    }
  }

  /**
   * Does nothing.
   * 
   * @see magellan.library.tasks.Inspector#unSuppress(magellan.library.Region)
   */
  public void unSuppress(Region r) {
    // nothing to do for inspectors that suppress problems by changing unit orders
  }

  /**
   * Does nothing.
   * 
   * @see magellan.library.tasks.Inspector#unSuppress(magellan.library.Faction)
   */
  public void unSuppress(Faction f) {
    // nothing to do for inspectors that suppress problems by changing unit orders
  }

  /**
   * Does nothing.
   * 
   * @see magellan.library.tasks.Inspector#unSuppressGlobal()
   */
  public void unSuppressGlobal() {
    // nothing to do for inspectors that suppress problems by changing unit orders
  }

  public void setGameData(GameData gameData) {
    data = gameData;
    gameSpecStuff = data.getGameSpecificStuff();
  }

  /**
   * Returns the current GameData
   */
  public GameData getData() {
    return data;
  }

  /**
   * Returns the current gameSpecificStuff.
   */
  public GameSpecificStuff getGameSpecificStuff() {
    return gameSpecStuff;
  }

  /**
   * Does not really ignore any problems. Subclasses may want to overwrite this to be more
   * efficient.
   * 
   * @see magellan.library.tasks.Inspector#setIgnore(magellan.library.tasks.ProblemType, boolean)
   */
  public void setIgnore(ProblemType type, boolean ignore) {
    // by default don't ignore any
  }

}
