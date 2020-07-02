// class magellan.library.utils.OrderReaderEnTest
// created on May 6, 2020
//
// Copyright 2003-2020 by magellan project team
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

import org.junit.BeforeClass;

import magellan.library.utils.logging.Logger;

public class OrderReaderEnTest extends OrderReaderTest {

  @BeforeClass
  public static void setUpBeforeClass() {
    defaultLocale = EN_LOCALE;
    setDefaultLocale();

    Logger.setLevel(Logger.WARN);
    initResources();
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    setLocale(EN_LOCALE);
  }
}