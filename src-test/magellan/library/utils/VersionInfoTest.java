// class magellan.library.utils.VersionTestTest
// created on May 17, 2020
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

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import magellan.test.MagellanTestWithResources;

public class VersionInfoTest extends MagellanTestWithResources {
  @Test
  public void testIsNewer() {
    File rDir = new File(".");
    System.out.println(rDir.getAbsolutePath());
    String currentVersion = VersionInfo.getVersion(Resources.getResourceDirectory());
    String newVersion = "2.1.0-0.beta";
    Version a = new Version(currentVersion);
    Version b = new Version(newVersion);
    assertTrue(a.isNewer(b));
    assertTrue(VersionInfo.isNewer(currentVersion, newVersion));
  }

  @Test
  public void testReleaseVersions() {
    assertTrue(VersionInfo.isNewer("2.1.0-123.latest", "2.1.0-100.latest"));
    assertTrue(VersionInfo.isNewer("2.1.0-123.latest", "2.1.0-100.stable"));
    assertTrue(VersionInfo.isNewer("2.1.0-123.stable", "2.1.0-100.latest"));
    assertTrue(VersionInfo.isNewer("2.1.0-123.latest", "2.1.0-100"));
    assertTrue(VersionInfo.isNewer("2.1.0-144", "2.1.0-123.latest"));
    assertTrue(VersionInfo.isNewer("2.1.1", "2.1.0-123.latest"));
  }

}
