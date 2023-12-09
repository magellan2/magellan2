// class magellan.test.MagellanTest
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
package magellan.test;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Set;

public class MagellanTestUtil {

  public static void forceDelete(Path dir) throws IOException {
    if (Files.exists(dir)) {
      allow(dir);
      Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
          try {
            allow(dir);
            Files.list(dir).forEach(t -> {
              try {
                MagellanTestUtil.allow(t);
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            });
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
          return FileVisitResult.CONTINUE;
        }
      });
      Files.walk(dir)
          .sorted(Comparator.reverseOrder())
          .forEach(MagellanTestUtil::del);
    }
  }

  public static void del(Path p) {
    try {
      if (Files.exists(p)) {
        Files.delete(p);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static Set<PosixFilePermission> permsDeny = Collections.EMPTY_SET;
  private static Set<PosixFilePermission> permsAllow =
      EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE);

  public static void allow(Path dir) throws IOException {
    Files.setPosixFilePermissions(dir, permsAllow);
  }

  public static void deny(Path dir) throws IOException {
    Files.setPosixFilePermissions(dir, permsDeny);
  }

  public static boolean isInternalTesting() {
    return "INTERNAL".equals(System.getenv("MAGELLAN2_TESTING"));
  }

}
