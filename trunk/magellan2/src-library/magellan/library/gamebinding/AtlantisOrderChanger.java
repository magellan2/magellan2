// class magellan.library.gamebinding.AtlantisOrderChanger
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

import magellan.library.Order;
import magellan.library.Orders;
import magellan.library.StringID;
import magellan.library.Unit;
import magellan.library.UnitContainer;

public class AtlantisOrderChanger implements OrderChanger {

  public Order createOrder(Unit unit, String string) {
    // HIGHTODO Automatisch generierte Methode implementieren
    return null;
  }

  public void addCombatOrder(Unit unit, int newstate) {
    // HIGHTODO Automatisch generierte Methode implementieren

  }

  public void addDescribeUnitContainerOrder(Unit unit, UnitContainer uc, String descr) {
    // HIGHTODO Automatisch generierte Methode implementieren

  }

  public void addDescribeUnitOrder(Unit unit, String descr) {
    // HIGHTODO Automatisch generierte Methode implementieren

  }

  public void addDescribeUnitPrivateOrder(Unit unit, String descr) {
    // HIGHTODO Automatisch generierte Methode implementieren

  }

  public void addHideOrder(Unit unit, String level) {
    // HIGHTODO Automatisch generierte Methode implementieren

  }

  public void addGroupOrder(Unit unit, String name) {
    // HIGHTODO Automatisch generierte Methode implementieren

  }

  public void addNamingOrder(Unit unit, String name) {
    // HIGHTODO Automatisch generierte Methode implementieren

  }

  public void addNamingOrder(Unit unit, UnitContainer uc, String name) {
    // HIGHTODO Automatisch generierte Methode implementieren

  }

  public void addRecruitOrder(Unit u, int amount) {
    // HIGHTODO Automatisch generierte Methode implementieren

  }

  public void addGiveOrder(Unit source, Unit target, int amount, StringID item, String comment) {
    // HIGHTODO Automatisch generierte Methode implementieren

  }

  public void addMultipleHideOrder(Unit u) {
    // HIGHTODO Automatisch generierte Methode implementieren

  }

  public void disableLongOrders(Unit u) {
    // HIGHTODO Automatisch generierte Methode implementieren

  }

  public boolean isLongOrder(String order) {
    // HIGHTODO Automatisch generierte Methode implementieren
    return false;
  }

  public boolean isLongOrder(Order order) {
    // HIGHTODO Automatisch generierte Methode implementieren
    return false;
  }

  public int areCompatibleLongOrders(Orders orders) {
    // HIGHTODO Automatisch generierte Methode implementieren
    return 0;
  }

}
