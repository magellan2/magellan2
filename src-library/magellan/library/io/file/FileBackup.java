/*
 *  Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe,
 *                          Stefan Goetz, Sebastian Pappert,
 *                          Klaas Prause, Enno Rehling,
 *                          Sebastian Tusk, Ulrich Kuester,
 *                          Ilja Pavkovic
 *
 * This file is part of the Eressea Java Code Base, see the
 * file LICENSING for the licensing information applying to
 * this file.
 *
 */

package magellan.library.io.file;

import java.io.File;
import java.io.IOException;

import magellan.library.utils.logging.Logger;

/**
 * Handles backup generation for files.
 */
public class FileBackup {

  private static final Logger log = Logger.getInstance(FileBackup.class);

  /** this is normally a bad idea, but we allow this global */
  public static int DEFAULT_BACKUP_LEVEL = 2;

  /**
   * Disable instantiation of class
   */
  private FileBackup() {
  }

  /**
   * Returns the new backup file for the given file. Uses a backup level of <code>2</code>.
   * 
   * @param file the file to back up. The directory is the parent directory of the given file.
   * @return the created backup file.
   * @throws IOException if the file cannot be created
   */
  public static synchronized File create(File file) throws IOException {
    return FileBackup.create(file, file.getCanonicalFile().getParentFile());
  }

  /**
   * Returns the new backup file for the given file and backup level
   * 
   * @param file the file to back up. The directory is the parent directory of the given file.
   * @param backuplevel number of revisions to hold, ignored for the SIMPLE backup type.
   * @return the created backup file.
   * @throws IOException if the file cannot be created
   */
  public static synchronized File create(File file, int backuplevel) throws IOException {
    return FileBackup.create(file, file.getParentFile(), backuplevel);
  }

  /**
   * Returns the new backup file for the given file. Uses a backup level of <code>2</code>.
   * 
   * @param file the file to back up.
   * @param directory the directory to copy the backup to. If the directory doesn't exist, it will
   *          be created.
   * @return the created backup file.
   * @throws IOException if the file cannot be created
   */
  public static synchronized File create(File file, File directory) throws IOException {
    return FileBackup.create(file, directory, FileBackup.DEFAULT_BACKUP_LEVEL);
  }

  /**
   * Returns the new backup file for the given file.
   * 
   * @param file the file to back up.
   * @param directory the directory to copy the backup to. If the directory doesn't exist, it will
   *          be created.
   * @param backupLevel number of revisions to hold, ignored for the SIMPLE backup type.
   * @return the created backup file or null if target file does not exist
   * @throws IOException if the file cannot be created
   */
  public static synchronized File create(File file, File directory, int backupLevel)
      throws IOException {
    // ensure that file exists. If not, stop execution to create backup file
    if (!(file.exists()))
      return null;

    if (directory == null) {
      FileBackup.log.error("error getting dir for file " + file.toString());
      FileBackup.log.error("the returned directory is null - failed to create backup!");
      return null;
    }

    // ensure that target directory exists
    if (!directory.exists()) {
      if (!directory.mkdirs())
        throw new IOException("Could not create directory " + directory);
    }

    File backup = null;

    int highestBackup = FileBackup.getLatestRevision(file.getName(), directory);
    backup =
        new File(directory + File.separator
            + FileBackup.getVersionName(file.getName(), highestBackup + 1));
    FileBackup.copy(file, backup);

    if (file.canWrite()) {
      FileBackup.removeObsoleteRevisions(highestBackup + 1, backupLevel, file.getName(), directory);
    }

    return backup;
  }

  /**
   * Returns the revision number of the latest revision found for the given filename.
   * 
   * @param filename filename to check for revision numbers.
   * @param dir directory to search.
   * @return revision number of the latest backup; returns <code>0</code> if no backup could be
   *         found.
   */
  private static int getLatestRevision(String filename, File dir) {
    if (dir == null)
      return 0;

    File files[] = dir.listFiles();
    int result = 0;

    for (File file : files) {
      String name = file.getName();

      if (name.startsWith(filename)) {
        int revision = FileBackup.getRevision(name);

        if (revision > result) {
          result = revision;
        }
      }
    }

    return result;
  }

  /**
   * Returns the revision number of the filename.
   * 
   * @param filename filename to check for a revision number.
   * @return the found revision number; returns <code>0</code> if no revision number could be found.
   */
  private static int getRevision(String filename) {
    int startOffset = filename.indexOf('~');
    int endOffset = filename.indexOf('~', startOffset + 1);

    while ((startOffset < endOffset) && (startOffset > -1) && (endOffset > -1)) {
      String result = filename.substring(startOffset + 1, endOffset);
      startOffset = filename.indexOf('~', endOffset);
      endOffset = filename.indexOf('~', startOffset + 1);

      try {
        int revision = Integer.parseInt(result);

        if (revision > 0)
          return revision;
      } catch (NumberFormatException nfe) {
      }
    }

    return 0;
  }

  /**
   * Concatenates the filename plus the revision string.
   * 
   * @param filename the filename to use.
   * @param revision the revision number.
   * @return the resulting string.
   */
  private static String getVersionName(String filename, int revision) {
    StringBuffer buf = new StringBuffer(15);
    buf.append(filename);
    buf.append('~');
    buf.append(revision);
    buf.append('~');

    return buf.toString();
  }

  /**
   * Copies the given source file to the given destination.
   * 
   * @param source source file.
   * @param target destination target file.
   * @throws IOException if an I/O error occured.
   */
  private static void copy(File source, File target) throws IOException {
    CopyFile.copy(source, target);
  }

  /**
   * Removes all obsolete revisions for the given filename in the given directory.
   * 
   * @param currentRevision revision number of the latest revision.
   * @param backupLevel number of revisions to hold.
   * @param filename filename to check for revision numbers.
   * @param directory directory which holds the backups.
   */
  private static void removeObsoleteRevisions(int currentRevision, int backupLevel,
      String filename, File directory) {
    File files[] = directory.listFiles();

    if (backupLevel > 0) {
      // delete old revisions
      for (File file : files) {
        String name = file.getName();

        if (name.startsWith(filename)) {
          int revision = FileBackup.getRevision(name);
          if (revision > 0 && revision <= (currentRevision - backupLevel)) {
            file.delete();
          }
        }
      }
    }
  }
}
