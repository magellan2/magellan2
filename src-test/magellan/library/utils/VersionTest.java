// class magellan.library.utils.VersionTest
// created on May 11, 2020
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * Tests for {@link Version}.
 */
public class VersionTest {

  @Test
  public void testVersionInteger() {
    Version v = new Version("2");
    assertEquals("2", v.getMajor());
    assertEquals("0", v.getMinor());
    assertEquals("0", v.getRevision());

    v = new Version("2.3.4");
    assertEquals("2", v.getMajor());
    assertEquals("3", v.getMinor());
    assertEquals("4", v.getRevision());
  }

  @SuppressWarnings("unused")
  private void assertExceptionInteger(String version) {
    try {
      new Version(version);
      fail();
    } catch (Exception e) {
      // expected
    }
  }

  @SuppressWarnings("unused")
  private void assertExceptionDeprecated(String version) {
    try {
      new Version(version);
      fail();
    } catch (Exception e) {
      // expected
    }
  }

  private void assertExceptionSemantic(String version) {
    Version v = new Version(version);
    if (!v.isError()) {
      fail();
    }
  }

  @Test
  public void testVersionNonInt() {
    assertExceptionSemantic("2");
    assertExceptionSemantic("2.1");
    assertExceptionSemantic("2.3.0.4");
    assertExceptionSemantic("2.3.0 (build x)");

    Version v = new Version("2.3.4");
    assertEquals("2", v.getMajor());
    assertEquals("3", v.getMinor());
    assertEquals("4", v.getRevision());

    // this is not a valid semantic version, but we allow it for historic reasons
    v = new Version("2.3.x");
    assertEquals("2", v.getMajor());
    assertEquals("3", v.getMinor());
    assertEquals("0", v.getRevision());

    v = new Version("2.3.4-bla");
    assertEquals("2", v.getMajor());
    assertEquals("3", v.getMinor());
    assertEquals("4", v.getRevision());
    assertEquals("bla", v.getIdentifiers());

    v = new Version("2.3.4-1bla.2.foo");
    assertEquals("2", v.getMajor());
    assertEquals("3", v.getMinor());
    assertEquals("4", v.getRevision());
    assertEquals("1bla.2.foo", v.getIdentifiers());

    v = new Version("2.3.4 (build 789)");
    assertEquals("2", v.getMajor());
    assertEquals("3", v.getMinor());
    assertEquals("4", v.getRevision());
    assertEquals("", v.getIdentifiers());
    assertEquals("789", v.getBuild());

    v = new Version("2.3.4-1bla.2.foo (build 789)");
    assertEquals("2", v.getMajor());
    assertEquals("3", v.getMinor());
    assertEquals("4", v.getRevision());
    assertEquals("1bla.2.foo", v.getIdentifiers());
    assertEquals("789", v.getBuild());
  }

  @Test
  public void testVersionDeprecated() {
    Version v = new Version("2.3.4");
    assertEquals("2", v.getMajor());
    assertEquals("3", v.getMinor());
    assertEquals("4", v.getRevision());

    // this is not a valid semantic version, but we allow it for historic reasons
    v = new Version("2.3.x");
    assertEquals("2", v.getMajor());
    assertEquals("3", v.getMinor());
    assertEquals("0", v.getRevision());

    v = new Version("2.3.4-bla");
    assertEquals("2", v.getMajor());
    assertEquals("3", v.getMinor());
    assertEquals("4", v.getRevision());
    assertEquals("bla", v.getIdentifiers());

    v = new Version("2.3.4 (build 789)");
    assertEquals("2", v.getMajor());
    assertEquals("3", v.getMinor());
    assertEquals("4", v.getRevision());
    assertEquals("", v.getIdentifiers());
    assertEquals("789", v.getBuild());
  }

  @Test
  public void testCompareTo() {
    compare("1.0.0", "1.0.0", 0);
    compare("1.0.1", "1.0.0", 1);
    compare("1.1.0", "1.0.1", 1);
    compare("3.2.4", "2.3.4", 1);

    compare("3.20.4", "3.3.4", 1);

    compare("1.0.0-devel5.3 (build 123)", "1.0.0", 1);

    compare("1.0.0-devel5.3 (build 123)", "1.0.0-devel5.3 (build 122)", 1);
    compare("1.0.0-devel5.3 (build 123)", "1.0.0-devel5.3 (build 22)", 1);

    compare("1.0.0-devel5.3 (build 123)", "1.0.0 (build 122)", 1); // build no precedence
    compare("1.0.0-devel5.3", "1.0.0", -1);

    compare("2.0.0-rc1", "2.0.0", -1);
    compare("2.0.0-ab", "2.0.0-c", -1);
    compare("2.0.0-a", "2.0.0-ab", -1);
    compare("2.0.0-a", "2.0.0-ba", -1);
    compare("2.0.0-c", "2.0.0-a", 1);
    compare("2.0.0-2", "2.0.0-123", -1);
    compare("2.0.0-2", "2.0.0-xyz", -1);
    compare("2.0.0-23", "2.0.0-2y", -1);

    compare("2.0.1-a.99.z", "2.0.1-a.123.z", -1);
    compare("2.0.1-a.123", "2.0.1-a.123.1", -1);
    compare("2.0.1-a.123.1", "2.0.2", -1);

    compare("2.0.rc1", "2.0.0", -1);
    compare("2.0.rc1", "2.0.3", -1);
    compare("2.0.ab", "2.0.c", -1);

  }

  @Test
  public void testIsNewer() {
    Version v1 = new Version("1.2.3"),
        v2 = new Version("2.1.0");
    assertTrue(v2.isNewer(v1));
    assertTrue(!v1.isNewer(v2));
    assertTrue(!v1.isNewer(v1));
  }

  @Test
  public void testIsNewerDeprecated() {
    // 2.0.6 compatibility
    assertTrue(VersionInfo.isNewer("2.1.3-latest.123", "2.0.6"));
  }

  private void compare(String version1, String version2, int comparison) {
    Version v1 = new Version(version1),
        v2 = new Version(version2);
    int cmp = v1.compareTo(v2);
    assertEquals(comparison, cmp > 0 ? 1 : cmp < 0 ? -1 : 0);
    cmp = v2.compareTo(v1);
    assertEquals(-comparison, cmp > 0 ? 1 : cmp < 0 ? -1 : 0);
  }

}
