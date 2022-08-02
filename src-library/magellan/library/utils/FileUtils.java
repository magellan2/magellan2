// class magellan.client.utils.FileUtils
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
package magellan.library.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.CopyOption;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import magellan.library.utils.FileUtils.FileException.ExceptionType;
import magellan.library.utils.logging.Logger;

public class FileUtils {
  public static class FileException extends Exception {

    public enum ExceptionType {
      IOError, FileExists, Unknown, AccessDenied, WriteDenied, ZipAccessError
    }

    private Path[] context;
    private ExceptionType type;

    public FileException(ExceptionType type, String message, Path[] context, Throwable cause) {
      super(message, cause);
      this.type = type;
      this.context = context;
    }

    public Object[] getContext() {
      return context;
    }

    public ExceptionType getType() {
      return type;
    }

  }

  private static final Logger log = Logger.getInstance(FileUtils.class);

  /**
   * Size of the buffer to read/write data
   */
  public static final int BUFFER_SIZE = 4096;

  public static void mvFile(String filename, Path sourceDir, Path destDir) throws IOException {
    Path dest = destDir.resolve(filename);
    Path src = sourceDir.resolve(filename);
    if (Files.exists(src) && Files.isRegularFile(src)) {
      Files.move(src, dest);
    }
  }

  public static void copyDirectory(Path source, Path target, CopyOption... options) throws FileException {
    Path[][] context = new Path[1][];
    try {
      Files.walkFileTree(source, new SimpleFileVisitor<Path>() {

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
            throws IOException {
          Path newdir = target.resolve(source.relativize(dir));
          context[0] = new Path[] { source, newdir };
          Files.createDirectories(newdir);
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
            throws IOException {
          Path newfile = target.resolve(source.relativize(file));
          context[0] = new Path[] { source, newfile };
          Files.copy(file, newfile, options);
          return FileVisitResult.CONTINUE;
        }

      });
    } catch (FileAlreadyExistsException e) {
      throw new FileException(ExceptionType.AccessDenied, "file already exists", context[0], e);
    } catch (AccessDeniedException e) {
      throw new FileException(ExceptionType.AccessDenied, "access denied", context[0], e);
    } catch (IOException e) {
      throw new FileException(ExceptionType.IOError, "could not copy", context[0], e);
    }

  }

  private static Path context;

  public static void deleteDirectory(Path dir) throws FileException {
    try {
      Files.walk(dir)
          .sorted(Comparator.reverseOrder())
          .sequential()
          .map(FileUtils::mark)
          .forEach(t -> {
            try {
              Files.deleteIfExists(t);
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          });
    } catch (RuntimeException e) {
      throw new FileException(ExceptionType.IOError, "could not delete", new Path[] { dir, context }, e.getCause());
    } catch (IOException e) {
      throw new FileException(ExceptionType.IOError, "could not delete", new Path[] { dir, context }, e);
    }
  }

  private static Path mark(Path p) {
    context = p;
    return p;
  }

  /**
   * Extracts a zip file specified by the zipFile to a directory specified by
   * destDirectory (will be created if does not exists). If filename is null, all files are extracted. Otherwise only
   * filename is extracted. If this is a directory, it is extracted recursively iff recursive is <code>true</code>.
   * 
   * @throws IOException
   */
  public static void unzip(Path zipFile, Path destDirectory, String filename, boolean recursive) throws FileException {
    if (!Files.exists(destDirectory) || !Files.isDirectory(destDirectory) || !Files.isWritable(destDirectory))
      throw new FileException(ExceptionType.WriteDenied,
          "destination not writable",
          new Path[] { destDirectory, destDirectory }, null);

    try {
      ZipInputStream zipIn = new ZipInputStream(Files.newInputStream(zipFile));
      ZipEntry entry = zipIn.getNextEntry();
      // iterates over entries in the zip file
      while (entry != null) {
        if (filename == null || filename.equals(entry.getName()) || (recursive && FileUtils.getTopLevel(entry).equals(
            filename))) {
          Path filePath = destDirectory.resolve(entry.getName());
          if (!entry.isDirectory()) {
            // if the entry is a file, extracts it
            try {
              FileUtils.extractFile(zipIn, filePath);
            } catch (IOException e) {
              throw new FileException(ExceptionType.ZipAccessError, "could not extract file ", new Path[] { zipFile,
                  filePath }, e);
            }

          } else {
            // if the entry is a directory, make the directory
            try {
              Files.createDirectory(filePath);
            } catch (IOException e) {
              throw new FileException(ExceptionType.ZipAccessError, "could not create directory ", new Path[] { zipFile,
                  filePath }, e);
            }
          }
        }
        zipIn.closeEntry();
        entry = zipIn.getNextEntry();
      }
      zipIn.close();
    } catch (IOException e) {
      throw new FileException(ExceptionType.ZipAccessError, "cannot open zip for reading", new Path[] { zipFile }, e);
    }

  }

  protected static String getTopLevel(ZipEntry entry) {
    String name = entry.getName();
    int pos = name.indexOf(File.separatorChar);
    if (pos >= 0)
      return name.substring(0, pos);
    return name;
  }

  /**
   * Extracts a zip entry (file entry)
   * 
   * @param zipIn
   * @param destPath
   * @throws IOException
   */
  protected static void extractFile(ZipInputStream zipIn, Path destPath) throws IOException {
    try {
      Files.createDirectories(destPath.getParent());// new ZipInputStream(new FileInputStream(zipFile)))
    } catch (FileAlreadyExistsException e) {
      // okay
    }
    BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(destPath));
    byte[] bytesIn = new byte[BUFFER_SIZE];
    int read = 0;
    while ((read = zipIn.read(bytesIn)) != -1) {
      bos.write(bytesIn, 0, read);
    }
    bos.close();
  }

  /**
   * Adds srcDirectory and its content to the archive. Paths in the archive are relative to parentDirectory, if
   * parentDirectory is not <code>null</code>. Filter is applied to every file encountered. It is not applied
   * recursively. Empty directories are omitted.
   * 
   * @param outputStream
   * @param srcDirectory path to the directory to add
   * @param parentDirectory the path in the archive should be the relative path of srcDirectory to parentDirectory
   * @param filter applied to the files encountered below srcDirectory
   * @throws IOException
   */
  public static void addDirectory(ZipOutputStream outputStream, Path srcDirectory, Path parentDirectory,
      java.util.function.Predicate<Path> filter)
      throws IOException {
    Files.walkFileTree(srcDirectory, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
        if (filter == null || filter.test(file)) {
          FileUtils.addFile(outputStream, parentDirectory, file);
        }
        return FileVisitResult.CONTINUE;
      }

    });
  }

  /**
   * @param outputStream
   * @param rootDirectory
   * @param file
   * @throws IOException
   */
  public static void addFile(ZipOutputStream outputStream, Path rootDirectory, Path file) throws IOException {
    Path entryFile = rootDirectory.relativize(file);
    outputStream.putNextEntry(new ZipEntry(entryFile.toString()));
    byte[] bytes = Files.readAllBytes(file);
    outputStream.write(bytes, 0, bytes.length);
    outputStream.closeEntry();
  }

}
