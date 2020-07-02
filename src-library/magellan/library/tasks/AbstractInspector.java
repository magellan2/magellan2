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
import magellan.library.Identifiable;
import magellan.library.Order;
import magellan.library.Region;
import magellan.library.Ship;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.gamebinding.GameSpecificStuff;
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

  private List<InspectorInterceptor> interceptors = new ArrayList<InspectorInterceptor>();

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

    List<Problem> problems = findProblems(u);

    checkIgnore(problems);

    if (problems.size() > 0)
      return problems;
    return Collections.emptyList();
  }

  /**
   * Returns an empty list. Sub-classes should usually overwrite this method or
   * {@link AbstractInspector#listProblems(magellan.library.Region)} (or both).
   *
   * @see magellan.library.tasks.Inspector#findProblems(magellan.library.Unit)
   */
  public List<Problem> findProblems(Unit u) {
    return Collections.emptyList();
  }

  private void checkIgnore(List<Problem> problems) {
    for (Iterator<Problem> it = problems.iterator(); it.hasNext();) {
      Problem p = it.next();
      Unit unit = getResponsibleUnit(p);
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
            if (!(line.getText().startsWith(Inspector.SUPPRESS_PREFIX) || line.getText().startsWith(
                Inspector.SUPPRESS_PREFIX_PERMANENT))) {
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

  protected Unit getResponsibleUnit(Problem p) {
    Unit responsible = null;
    if (p.getOwner() != null) {
      responsible = p.getOwner();
    } else if (p.getRegion() != null && !p.getRegion().units().isEmpty()) {
      // first unit of owner faction in region
      if (p.getFaction() != null) {
        for (Unit u : p.getRegion().units())
          if (p.getFaction().equals(u.getFaction())) {
            responsible = u;
            break;
          }
      } else {
        // first unit
        responsible = p.getRegion().units().iterator().next();
      }
    } else if (p.getFaction() != null) {
      // first unit of owner faction
      if (p.getFaction().units() != null) {
        responsible = p.getFaction().units().iterator().next();
      }
    } else if (getData().getOwnerFaction() != null && getData().getFaction(getData()
        .getOwnerFaction()) != null) {
      for (Unit u : getData().getFaction(getData().getOwnerFaction()).units()) {
        responsible = u;
        break;
      }
    }
    return responsible;
  }

  protected boolean isSuppressMarker(Order order) {
    return order.getText().startsWith(AbstractInspector.SUPPRESS_LINE_PREFIX_PERMANENT) || order
        .getText().startsWith(AbstractInspector.SUPPRESS_LINE_PREFIX) || order.getText().startsWith(
            Inspector.SUPPRESS_PREFIX_PERMANENT) || order.getText().startsWith(
                Inspector.SUPPRESS_PREFIX);
  }

  protected boolean isSuppressMarkerFor(Order line, Problem p, boolean lineMode) {
    if (lineMode)
      return line.getText().equals(getSuppressLineComment(p, false)) || line.getText().equals(
          getSuppressLineComment(p, true));
    else
      return line.getText().equals(getSuppressUnitComment(p, false)) || line.getText().equals(
          getSuppressUnitComment(p, true));
  }

  /**
   * Returns false
   */
  protected boolean checkIgnoreUnit(Unit u) {
    if (intercept(null, null, u, null))
      return true;
    return false;
  }

  /**
   * Returns <code>true</code> iff this unit's orders contain
   * {@link AbstractInspector#getSuppressUnitComment(Problem)}. Sub-classes should overwrite
   * this to add more sophisticated ignoring of errors.
   *
   * @param u
   */
  protected boolean checkIgnoreUnit(Unit u, Problem p) {
    boolean found = false;
    for (Order order : u.getOrders2()) {
      if (order.getText().equals(getSuppressUnitComment(p))) {
        found = true;
        break;
      }
    }
    return found;
  }

  /**
   * Calls {@link AbstractInspector#checkIgnoreUnit(Unit)} for the container's owner unit.
   * Sub-classes may overwrite this to add more sophisticated ignoring of errors.
   *
   * @param c
   */
  protected boolean checkIgnoreUnitContainer(UnitContainer c) {
    // TODO should we use getModifiedOnwerUnit() here?
    if (c.getOwnerUnit() == null)
      return false;
    return checkIgnoreUnit(c.getOwnerUnit());
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

    List<Problem> problems = listProblems(r);

    checkIgnore(problems);

    if (problems.size() > 0)
      return problems;
    return Collections.emptyList();
  }

  /**
   * Returns <code>false</code>. Sub-classes should overwrite this method to add more sophisticated
   * ignoring of errors.
   *
   * @param r
   * @return <code>true</code> iff this region should be ignored for problems
   */
  protected boolean checkIgnoreRegion(Region r) {
    if (intercept(null, r, null, null))
      return true;

    return false;
  }

  /**
   * Returns an empty list. Sub-classes should usually overwrite this method or
   * {@link Inspector#findProblems(magellan.library.Unit)} (or both).
   *
   * @see magellan.library.tasks.Inspector#listProblems(magellan.library.Region)
   */
  public List<Problem> listProblems(Region r) {
    return Collections.emptyList();
  }

  /**
   * @see Inspector#suppress(Problem)
   */
  public Unit suppress(Problem p) {
    Unit responsible = getResponsibleUnit(p);
    if (responsible == null)
      return null;

    if (p.getLine() >= 0) {
      responsible.addOrderAt(p.getLine() - 1, getSuppressLineComment(p), true);
    } else {
      responsible.addOrderAt(0, getSuppressUnitComment(p), true);
    }
    return responsible;
  }

  protected String getSuppressUnitComment(Problem p, boolean permanent) {
    StringBuffer sb = new StringBuffer(permanent ? Inspector.SUPPRESS_PREFIX_PERMANENT
        : Inspector.SUPPRESS_PREFIX);
    sb.append(" ").append(p.getType().getName());
    if (getResponsibleUnit(p) != null && getResponsibleUnit(p) != p.getObject() && (p
        .getObject() instanceof Identifiable)) {
      Identifiable object = (Identifiable) p.getObject();
      sb.append(" ").append(object.getID());
    }
    return sb.toString();
  }

  protected String getSuppressLineComment(Problem p, boolean permanent) {
    StringBuffer sb = new StringBuffer(permanent ? AbstractInspector.SUPPRESS_LINE_PREFIX_PERMANENT
        : AbstractInspector.SUPPRESS_LINE_PREFIX);
    sb.append(" ").append(p.getType().getName());
    if (getResponsibleUnit(p) != null && getResponsibleUnit(p) != p.getObject() && (p
        .getObject() instanceof Identifiable)) {
      Identifiable object = (Identifiable) p.getObject();
      sb.append(" ").append(object.getID());
    }

    return sb.toString();
  }

  protected String getSuppressUnitComment(Problem p) {
    return getSuppressUnitComment(p, false);
  }

  protected String getSuppressLineComment(Problem p) {
    return getSuppressLineComment(p, false);
  }

  /**
   * @see magellan.library.tasks.Inspector#unSuppress(magellan.library.Unit)
   */
  public void unSuppress(Unit u) {
    List<Order> newOrders = new ArrayList<Order>(u.getOrders2().size());
    boolean changed = false;
    for (Order o : u.getOrders2()) {
      if (isSuppressMarker(o)) {
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

  /**
   * @see magellan.library.tasks.Inspector#addInterceptor(magellan.library.tasks.InspectorInterceptor)
   */
  public void addInterceptor(InspectorInterceptor interceptor) {
    interceptors.add(interceptor);
  }

  /**
   * Calls all interceptors and find an interceptor that matches. If not, return false.
   */
  protected boolean intercept(Faction f, Region r, Unit u, Ship s) {
    for (InspectorInterceptor interceptor : interceptors) {
      if (interceptor.ignore(f, r, u, s))
        return true;
    }

    return false;
  }
}
