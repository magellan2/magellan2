// class magellan.library.utilsut.MagellanTestWithResourcesTest
// created on Nov 16, 2010
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
package magellan.library.utils;

import static org.junit.Assert.assertEquals;
import magellan.library.utils.logging.Logger;
import magellan.test.MagellanTestWithResources;

import org.junit.Test;

/**
 * @author stm
 * @version 1.0, Nov 16, 2010
 */
public class ResourcesTest extends MagellanTestWithResources {

  /**
   * Tests with keys in all resources are the same.
   */
  @Test
  public void testLanguagesIdentical() {
    String result = Resources.getInstance().check().toString();
    if (result.length() > 0) {
      Logger.getInstance(this.getClass()).error(result);
    }
    assertEquals("", result);
  }

}
