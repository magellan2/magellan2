// class magellan.library.tasks.OrderSyntaxInspector
// created on 02.03.2008
//
// Copyright 2003-2008 by magellan project team
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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import magellan.library.GameData;
import magellan.library.Unit;
import magellan.library.completion.OrderParser;
import magellan.library.utils.Resources;
import magellan.library.utils.Utils;

/**
 * This inspectors checks all syntax.
 *
 * @author Thoralf Rickert
 * @version 1.0, 02.03.2008
 */
public class OrderSyntaxInspector extends AbstractInspector implements Inspector {

  /** The singleton instance of the OrderSyntaxInspector */
  public static final OrderSyntaxInspector INSPECTOR = new OrderSyntaxInspector();

  protected OrderSyntaxInspector() {
  }
  
  /**
   * Returns an instance of OrderSyntaxInspector.
   * 
   * @return The singleton instance of OrderSyntaxInspector
   */
  public static OrderSyntaxInspector getInstance() {
    return OrderSyntaxInspector.INSPECTOR;
  }

  /**
   * @see magellan.library.tasks.AbstractInspector#reviewUnit(magellan.library.Unit, int)
   */
  @Override
  public List<Problem> reviewUnit(Unit unit, int type) {
    
    
    Collection<String> orders = unit.getOrders();
    List<Problem> errors = new ArrayList<Problem>();
    
    if (Utils.isEmpty(orders) && type==Problem.ERROR) {
      // no orders...that could be a problem.
      if (Utils.isEmpty(unit.getFaction().getPassword())) {
        // okay, that isnt our unit... forget it
        return Collections.emptyList();
      } else {
        errors.add(new SyntaxError(this,unit,"tasks.ordersyntaxinspector.no_orders"));
      }
      
    }
    
    // be carefull with the order parser. Some orders may be correct but will not get
    // an OK from the parser: ZAUBERE und Benutze Trank ...
    // so I change that from error to warning
    
    if (type==Problem.WARNING) {
      GameData data = unit.getRegion().getData();
      OrderParser parser = data.getGameSpecificStuff().getOrderParser(data);
      
      Integer line = 1;
      for (String order : orders) {
        StringReader reader = new StringReader(order);
        // Debug
        if (order.toLowerCase().startsWith("zaubere 'geister")){
          int i = 1;
        }
        boolean ok = parser.read(reader);
        if (!ok) {
          errors.add(new SyntaxWarning(this,unit,"tasks.ordersyntaxinspector.parse_warning",order,line));
        }
        line++;
      }
    } 
    return errors;
  }
  
  class SyntaxError extends AbstractProblem implements Problem {
    public SyntaxError(Inspector inspector, Unit unit, String resourceKey, Object...args) {
      super(unit, unit, inspector, Resources.get(resourceKey,args));
    }
    
    /**
     * returns the type of the problem
     * 
     * @see magellan.library.tasks.AbstractProblem#getType()
     */
    @Override
    public int getType() {
      return Problem.ERROR;
    }
  }
  
  class SyntaxWarning extends AbstractProblem implements Problem {
    public SyntaxWarning(Inspector inspector, Unit unit, String resourceKey, Object...args) {
      super(unit, unit, inspector, Resources.get(resourceKey,args));
    }
    
    /**
     * returns the type of the problem
     * 
     * @see magellan.library.tasks.AbstractProblem#getType()
     */
    @Override
    public int getType() {
      return Problem.WARNING;
    }
  }
}
