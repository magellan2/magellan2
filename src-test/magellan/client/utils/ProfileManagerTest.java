// class magellan.client.utils.ProfileManagerTest2
// created on Aug 1, 2022
//
// Copyright 2003-2022 by magellan project team
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
package magellan.client.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.zip.ZipOutputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import magellan.client.Client;
import magellan.client.utils.ProfileManager.ProfileException;
import magellan.library.utils.FileUtils;
import magellan.library.utils.FileUtils.FileException;
import magellan.library.utils.OrderedOutputProperties;
import magellan.test.MagellanTest;

public class ProfileManagerTest {

  private Path settingsDir;
  private Path[] oldProfiles;

  @Before
  public void setUp() throws Exception {
    settingsDir = Path.of("test/ProfileManager");
    MagellanTest.forceDelete(settingsDir);
    Files.createDirectory(settingsDir);
  }

  @After
  public void tearDown() throws IOException {
    MagellanTest.forceDelete(settingsDir);
  }

  @Test
  public void testInit() throws IOException {
    File settingsDir2 = new File("Test");
    String p = ProfileManager.init(settingsDir2);
    assertNull(p);

    p = ProfileManager.init(settingsDir.toFile());
    assertEquals("default", p);
    assertEquals(settingsDir, ProfileManager.getSettingsDirectory().toPath());
    p = ProfileManager.getCurrentProfile();
    assertTrue(p.startsWith("default"));
    File d = ProfileManager.getProfileDirectory();
    assertTrue(Files.isDirectory(d.toPath()));
  }

  @Test
  public void testInitLegacy() throws IOException {
    Files.writeString(settingsDir.resolve(Client.SETTINGS_FILENAME), "Hodor!");

    ProfileManager.init(settingsDir.toFile());

    assertEquals(1, ProfileManager.getProfiles().size());
    assertEquals("Hodor!", Files.lines(settingsDir.resolve("default").resolve(Client.SETTINGS_FILENAME))
        .collect(Collectors.joining()));
  }

  @Test
  public void testInit2() throws IOException {
    ProfileManager.init(settingsDir.toFile());

    String p = ProfileManager.init(settingsDir.toFile());
    assertEquals("default", p);
    assertEquals(1, ProfileManager.getProfiles().size());
    assertTrue(ProfileManager.getProfiles().contains("default"));
    assertFalse(ProfileManager.getProfiles().contains("missing"));
    assertNotEquals("default", ProfileManager.getProfileDirectory());
    assertTrue(ProfileManager.getProfileDirectory().getName().startsWith("default"));
    assertEquals("default", p);
  }

  @Test
  public void testInit3() throws IOException {
    makeDefault();

    String p = ProfileManager.init(settingsDir.toFile());
    assertEquals("default", p);
    assertEquals(1, ProfileManager.getProfiles().size());
    assertTrue(ProfileManager.getProfiles().contains("default"));
    assertFalse(ProfileManager.getProfiles().contains("missing"));
    assertEquals("default", p);
  }

  private void makeDefault() throws IOException {
    ProfileManager.init(settingsDir.toFile());
    ProfileManager.saveSettings();
  }

  @Test
  public void testCheckProfiles() throws IOException {
    makeDefault();

    MagellanTest.allow(settingsDir.resolve("default"));
    ProfileManager.init(settingsDir.toFile());
    MagellanTest.deny(settingsDir.resolve("default"));
    ProfileManager.checkProfiles();
    assertFalse(ProfileManager.getProfiles().contains("default"));
    assertNull(ProfileManager.getCurrentProfile());
    MagellanTest.allow(settingsDir.resolve("default"));
  }

  @Test
  public void testRemove() throws ProfileException, IOException, FileException {
    makeDefault();

    ProfileManager.init(settingsDir.toFile());
    Path oldSettings = settingsDir.resolve("default").resolve("magellan.ini");
    Files.createFile(oldSettings);
    ProfileManager.add("second", "default");
    assertEquals(1, (long) Files.list(settingsDir.resolve("second")).collect(Collectors.counting()));
    Path newSettings = settingsDir.resolve("second").resolve("magellan.ini");
    assertFileEquals(oldSettings, newSettings);
    ProfileManager.remove("second", true);
    assertFalse(Files.isDirectory(settingsDir.resolve("second")));

    ProfileManager.add("second", "default");
    assertEquals(1, (long) Files.list(settingsDir.resolve("second")).collect(Collectors.counting()));
    ProfileManager.remove("second", false);
    assertEquals(1, (long) Files.list(settingsDir.resolve("second")).collect(Collectors.counting()));

    // does not exist
    assertFalse(ProfileManager.remove("atlantis", true));

    // not empty
    try {
      ProfileManager.add("second", "default");
      fail("should get exception");
    } catch (ProfileException e) {
      assertEquals(FileException.ExceptionType.FileExists, ((FileException) e.getCause()).getType());
    }

    // access denied
    FileUtils.deleteDirectory(settingsDir.resolve("second"));
    ProfileManager.add("second", "default");
    MagellanTest.deny(settingsDir.resolve("second"));
    try {
      ProfileManager.remove("second", true);
      fail("should get exception");
    } catch (ProfileException e) {
      // okay
    }
  }

  private void assertFileEquals(Path p1, Path p2) {
    try {
      assertTrue(Files.isRegularFile(p1));
      assertTrue(Files.isRegularFile(p2));
      assertEquals(Files.lines(p1).collect(Collectors.joining()), Files.lines(p2).collect(Collectors.joining()));
    } catch (Throwable t) {
      fail(t.getMessage());
    }
  }

  @Test
  public void testAdd() throws IOException, ProfileException {
    makeDefault();

    ProfileManager.init(settingsDir.toFile());
    Files.createFile(settingsDir.resolve("default").resolve("magellan.ini"));

    // happy path
    ProfileManager.add("second", null);
    assertEquals("", // settingsDir.resolve("second"),
        Files.list(settingsDir.resolve("second")).map(Path::toString).collect(Collectors.joining()));
    assertEquals(new File(settingsDir.toFile(), "second"), ProfileManager.getProfileDirectory("second"));
    assertEquals(2, ProfileManager.getProfiles().size());
    assertEquals("default", ProfileManager.getCurrentProfile());

    // happy path copy
    ProfileManager.add("third", "default");
    assertEquals(settingsDir.resolve("third").resolve("magellan.ini").toString(),
        Files.list(settingsDir.resolve("third")).map(Path::toString).collect(Collectors.joining()));
    assertEquals("default", ProfileManager.getCurrentProfile());

    // existing dir, don't copy
    ProfileManager.add("second", "default");
    assertEquals("",
        Files.list(settingsDir.resolve("second")).map(Path::toString).collect(Collectors.joining()));
    assertEquals(3, ProfileManager.getProfiles().size());

    // null name
    ProfileManager.add(null, null);
    assertEquals(3, ProfileManager.getProfiles().size());

    // template does not exist
    ProfileManager.add("second", "atlantis");
    assertEquals("",
        Files.list(settingsDir.resolve("second")).map(Path::toString).collect(Collectors.joining()));
    assertEquals(3, ProfileManager.getProfiles().size());
  }

  @Test
  public void testSetProfile() throws ProfileException, IOException {
    makeDefault();

    ProfileManager.init(settingsDir.toFile());
    ProfileManager.add("second", "default");
    assertEquals("default", ProfileManager.getCurrentProfile());
    assertTrue(ProfileManager.setProfile("second"));
    assertEquals("second", ProfileManager.getCurrentProfile());

    assertFalse(ProfileManager.setProfile("atlantis"));
    assertEquals("second", ProfileManager.getCurrentProfile());
  }

  @Test
  public void testSaveSettings() throws IOException {
    ProfileManager.init(settingsDir.toFile());
    ProfileManager.saveSettings();

    Properties settings = new OrderedOutputProperties();
    settings.loadFromXML(new BufferedInputStream(Files.newInputStream(settingsDir.resolve(ProfileManager.INIFILE))));
    assertEquals("default", settings.get("profile.default.name"));
    assertEquals("default", settings.get("profile.default.directory"));
    assertEquals("default", settings.get("profile.current"));
  }

  @Test
  public void testExportProfiles() throws IOException, FileException {
    makeDefault();

    Path zipFile = Path.of("test/ProfileManager/export.zip");
    Path unpack = Path.of("test/ProfileManager/unpack");

    ProfileManager.init(settingsDir.toFile());
    Path iniFile = settingsDir.resolve("default").resolve("magellan.ini");
    Files.writeString(iniFile, "Hodor!");
    Files.createFile(settingsDir.resolve("default").resolve("magellan.ini~"));
    ProfileManager.exportProfiles(zipFile);

    Files.createDirectories(unpack);
    FileUtils.unzip(zipFile, unpack, null, true);

    assertEquals(
        "test/ProfileManager/unpack,test/ProfileManager/unpack/default,test/ProfileManager/unpack/default/magellan.ini,test/ProfileManager/unpack/profiles.ini",
        Files.walk(unpack).map(Path::toString).collect(Collectors.joining(",")));
    assertFileEquals(iniFile, unpack.resolve("default").resolve("magellan.ini"));
  }

  @Test
  public void testImportProfiles() throws IOException, ProfileException {
    makeDefault();

    Path zipFile = Path.of("test/ProfileManager/export.zip");
    try {
      ProfileManager.init(settingsDir.toFile());
      Files.writeString(settingsDir.resolve("default").resolve("magellan.ini"), "Hodor!");
      ProfileManager.add("second", "default");
      ProfileManager.add("third", "default");
      ProfileManager.saveSettings();
      ProfileManager.exportProfiles(zipFile);
      ProfileManager.remove("third", true);

      Collection<String> imported = ProfileManager.importProfilesFromZip(zipFile);
      assertEquals(3, imported.size());
      assertEquals(5, ProfileManager.getProfiles().size());
      assertEquals("default_1", ProfileManager.getProfileDirectory("default1").getName());
      assertEquals("second_1", ProfileManager.getProfileDirectory("second1").getName());
      assertEquals("third", ProfileManager.getProfileDirectory("third").getName());
    } finally {
      ProfileManager.remove("default1", true);
      ProfileManager.remove("second1", true);
      ProfileManager.remove("third", true);
    }
  }

  @Test
  public void testImportLegacy() throws IOException, FileException, ProfileException {
    makeDefault();

    Path zipFile = Path.of("test/ProfileManager/export.zip");
    Path defaultDir = settingsDir.resolve("default");

    ProfileManager.init(settingsDir.toFile());
    Files.writeString(defaultDir.resolve("magellan.ini"), "Hodor!");

    try (ZipOutputStream outputStream = new ZipOutputStream(Files.newOutputStream(zipFile))) {
      FileUtils.addFile(outputStream, defaultDir, defaultDir.resolve("magellan.ini"));
    }
    Files.writeString(defaultDir.resolve("magellan.ini"), "D'oh");

    Collection<String> imported = ProfileManager.importProfilesFromZip(zipFile);
    assertEquals(1, imported.size());
    assertEquals(2, ProfileManager.getProfiles().size());
    assertEquals("magellan.ini",
        Files.list(ProfileManager.getProfileDirectory("profile").toPath())
            .map(Path::getFileName).map(Path::toString)
            .collect(Collectors.joining()));
    assertEquals("Hodor!", Files.lines(ProfileManager.getProfileDirectory("profile").toPath().resolve("magellan.ini"))
        .collect(Collectors.joining()));
  }

  @Test
  public void testImportFromFile() throws IOException, ProfileException {
    makeDefault();
    Path settingsDir2 = Path.of("test/ProfileManager2");
    Files.createDirectories(settingsDir2);
    try {
      ProfileManager.init(settingsDir2.toFile());
      Files.writeString(settingsDir2.resolve("default").resolve("magellan.ini"), "Hodor!");
      ProfileManager.add("second", "default");
      ProfileManager.add("third", "default");
      ProfileManager.add("fourth", "default");
      ProfileManager.saveSettings();
      MagellanTest.forceDelete(settingsDir2.resolve("fourth"));

      ProfileManager.init(settingsDir.toFile());
      ProfileManager.add("second", "default");

      Collection<String> imported = ProfileManager.importProfilesFromDir(settingsDir2);

      assertEquals(3, imported.size());
      assertEquals(5, ProfileManager.getProfiles().size());
      assertEquals("default_1", ProfileManager.getProfileDirectory("default1").getName());
      assertEquals("second_1", ProfileManager.getProfileDirectory("second1").getName());
      assertEquals("third", ProfileManager.getProfileDirectory("third").getName());
    } finally {
      MagellanTest.forceDelete(settingsDir2);
    }
  }

}
