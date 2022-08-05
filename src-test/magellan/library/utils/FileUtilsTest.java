// class magellan.library.utils.FileUtilsTest
// created on Aug 2, 2022
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
package magellan.library.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import magellan.library.utils.FileUtils.FileException;
import magellan.test.MagellanTest;

public class FileUtilsTest {

  private static Path srcDir;
  private static Path destDir;

  @Before
  public void setUp() throws Exception {
    srcDir = Path.of("test/FileUtils/src");
    destDir = Path.of("test/FileUtils/dest");
    MagellanTest.forceDelete(srcDir);
    MagellanTest.forceDelete(destDir);
    Files.createDirectories(srcDir);
    Files.createDirectories(destDir);
    Path xyz = srcDir.resolve("xyz");
    if (!Files.exists(xyz)) {
      Path f = Files.createFile(xyz);
      Files.writeString(f, "zyx");
    }
    Files.walk(destDir)
        .sorted(Comparator.reverseOrder())
        .filter((p) -> {
          return !p.equals(destDir);
        })
        .forEach(MagellanTest::del);
  }

  @After
  public void after() throws IOException {
    MagellanTest.forceDelete(srcDir);
    MagellanTest.forceDelete(destDir);
  }

  @Test
  public void testMvFile() throws IOException {
    FileUtils.mvFile("xyz", srcDir, destDir);
    FileUtils.mvFile("xyz", srcDir, destDir);
    FileUtils.mvFile("zyx", srcDir, destDir);
    assertTrue(Files.isRegularFile(destDir.resolve("xyz")));
    assertFalse(Files.isRegularFile(srcDir.resolve("xyz")));
    assertFalse(Files.isRegularFile(destDir.resolve("zyx")));
  }

  @Test
  public void testCpFile() throws IOException {
    FileUtils.cpFile("xyz", srcDir, destDir);
    try {
      FileUtils.cpFile("xyz", srcDir, destDir);
      fail();
    } catch (FileAlreadyExistsException e) {
      // okay
    }
    FileUtils.cpFile("zyx", srcDir, destDir);
    assertTrue(Files.isRegularFile(destDir.resolve("xyz")));
    assertTrue(Files.isRegularFile(srcDir.resolve("xyz")));
    assertFalse(Files.isRegularFile(destDir.resolve("zyx")));
  }

  @Test
  public void testCopyDirectory() throws FileException {
    FileUtils.copyDirectory(srcDir, destDir);
    assertTrue(Files.isRegularFile(destDir.resolve("xyz")));
    try {
      FileUtils.copyDirectory(srcDir, destDir);
      fail();
    } catch (FileException exc) {
      assertEquals(FileException.ExceptionType.FileExists, exc.getType());
      assertEquals(destDir.resolve("xyz"), exc.getContext()[1]);
    }
  }

  @Test
  public void testCopyDirectoryPerm() throws FileException, IOException {
    MagellanTest.deny(destDir);
    try {
      FileUtils.copyDirectory(srcDir, destDir);
      fail();
    } catch (FileException exc) {
      assertEquals(FileException.ExceptionType.AccessDenied, exc.getType());
      assertEquals(destDir.resolve("xyz"), exc.getContext()[1]);
    }
  }

  @Test
  public void testDeleteDirectory() throws FileException {
    assertTrue(Files.exists(srcDir));
    FileUtils.deleteDirectory(srcDir);
    assertFalse(Files.exists(srcDir));
  }

  @Test
  public void testDeleteDirectoryPerm() throws FileException, IOException {
    MagellanTest.deny(srcDir.resolve("xyz"));
    assertTrue(Files.exists(srcDir));
    FileUtils.deleteDirectory(srcDir);
    assertFalse(Files.exists(srcDir));
  }

  @Test
  public void testUnzip() throws IOException, FileException {
    Path dest = Path.of("test/FileUtils/unzip");
    try {
      try {
        FileUtils.unzip(Path.of("test/FileUtils/packed.zip"), dest, "alien.txt", true);
        fail();
      } catch (FileException e) {
        assertEquals(FileException.ExceptionType.WriteDenied, e.getType());
      }
      Files.createDirectory(dest);
      FileUtils.unzip(Path.of("test/FileUtils/packed.zip"), dest, "alien.txt", true);
      Path destFile = dest.resolve("alien.txt");
      assertEquals("Dear Earthling,",
          Files.lines(destFile).collect(Collectors.joining()).substring(0, 15));
      assertEquals(1, (long) Files.list(dest).map(Path::toString).collect(Collectors.counting()));
      assertEquals(destFile.toString(), Files.list(dest).map(Path::toString).collect(Collectors.joining()));

      FileUtils.unzip(Path.of("test/FileUtils/packed.zip"), dest, null, true);
      assertEquals(3, (long) Files.list(dest).map(Path::toString).collect(Collectors.counting()));
    } finally {
      MagellanTest.forceDelete(dest);
    }
  }

  @Test
  public void testGetTopLevel() throws IOException {
    ZipInputStream zipIn = readZip("test/FileUtils/packed.zip");
    ZipEntry entry = zipIn.getNextEntry();
    String top = FileUtils.getTopLevel(entry);
    assertEquals("alien.txt", top);

    entry = zipIn.getNextEntry();
    entry = zipIn.getNextEntry();
    top = FileUtils.getTopLevel(entry);
    assertEquals("one", top);

    entry = zipIn.getNextEntry();
    top = FileUtils.getTopLevel(entry);
    assertEquals("one", top);

  }

  @Test
  public void testExtractFile() throws IOException {
    ZipInputStream zipIn = readZip("test/FileUtils/packed.zip");
    Path dest = Path.of("test/FileUtils/unpack");
    assertFalse(Files.isDirectory(dest));
    ZipEntry entry = zipIn.getNextEntry();
    FileUtils.extractFile(zipIn, dest);
    assertTrue(Files.isRegularFile(dest));
    assertEquals("Dear Earthling,", Files.lines(dest).collect(Collectors.joining()).substring(0, 15));

    entry = zipIn.getNextEntry();
    entry = zipIn.getNextEntry();
    entry = zipIn.getNextEntry();
    FileUtils.extractFile(zipIn, dest);
    assertTrue(Files.isRegularFile(dest));
    assertEquals("", Files.lines(dest).collect(Collectors.joining()));
    zipIn.close();
    MagellanTest.del(dest);
  }

  private ZipInputStream readZip(String zipFile) throws FileNotFoundException {
    return new ZipInputStream(new FileInputStream(zipFile));
  }

  @Test
  public void testAddDirectory() throws IOException, FileException {
    Path dest = Path.of("test/FileUtils/result.zip");
    try {
      // writeZip
      final ZipOutputStream outputStream = new ZipOutputStream(Files.newOutputStream(dest));
      FileUtils.addDirectory(outputStream, srcDir, srcDir.getParent(), null);
      outputStream.close();
      FileUtils.unzip(dest, destDir, null, true);
      assertEquals("test/FileUtils/dest,test/FileUtils/dest/src,test/FileUtils/dest/src/xyz",
          Files.walk(destDir).map(Path::toString).collect(Collectors.joining(",")));
    } finally {
      MagellanTest.del(dest);
    }
  }

  @Test
  public void testAddFile() throws IOException, FileException {
    Path dest = Path.of("test/FileUtils/result.zip");
    Path src = Path.of("test/FileUtils/src");
    // writeZip
    final ZipOutputStream outputStream = new ZipOutputStream(Files.newOutputStream(dest));
    FileUtils.addFile(outputStream, src, src.resolve("xyz"));
    outputStream.close();
    FileUtils.unzip(dest, destDir, null, true);
    assertEquals("zyx",
        Files.lines(destDir.resolve("xyz")).collect(Collectors.joining()));
    assertEquals("test/FileUtils/dest/xyz", Files.list(destDir).map(Path::toString).collect(Collectors.joining()));
    MagellanTest.del(dest);
  }

}
