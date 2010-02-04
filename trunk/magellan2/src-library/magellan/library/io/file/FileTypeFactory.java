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
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * A class to create FileTypes.
 */
public class FileTypeFactory {
  private FileTypeFactory() {
  }

  private static FileTypeFactory singleton = new FileTypeFactory();

  /**
   * Returns the sole FileTypeFactory (with respect to singleton pattern).
   * 
   * @return the singleton <code>FileTypeFactory</code>
   */
  public static FileTypeFactory singleton() {
    return FileTypeFactory.singleton;
  }

  /**
   * Creates an <code>InputStreamSourceFileType</code> of the given file name.
   * 
   * @param url the URL to the InputStream
   * @return an InputStreamSourceFileType pointing to the given URL.
   * @throws IOException
   */
  public FileType createInputStreamSourceFileType(File url) throws IOException {
    return new InputStreamSourceFileType(url).checkConnection();
  }

  /**
   * Creates an <code>InputStreamSourceFileType</code> of the given file name.
   * 
   * @param fileName the URL to the InputStream
   * @param readonly file shall be readonly
   * @return a FileType pointing to the given File.
   * @throws IOException
   */
  public FileType createFileType(File fileName, boolean readonly) throws IOException {
    return createFileType(fileName, readonly, null);
  }

  /**
   * Creates an <code>InputStreamSourceFileType</code> of the given file name.
   * 
   * @param fileName the URL to the InputStream
   * @param readonly file shall be readonly
   * @param ftc a FileTypeChooser used if entry in compressed file is not unique.
   * @return a FileType pointing to the given File.
   * @throws IOException
   */
  public FileType createFileType(File fileName, boolean readonly, FileTypeChooser ftc)
      throws IOException {
    return doCreateFileType(fileName, readonly, ftc).checkConnection();
  }

  private FileType doCreateFileType(File fileName, boolean readonly, FileTypeChooser ftc)
      throws IOException {
    if (fileName == null)
      throw new NullPointerException();

    String fileNameLC = fileName.getName().toLowerCase();

    if (fileNameLC.endsWith(FileType.GZIP))
      return new GZipFileType(fileName, readonly);

    if (fileNameLC.endsWith(FileType.BZIP2))
      return new BZip2FileType(fileName, readonly);

    if (fileNameLC.endsWith(FileType.ZIP))
      return createZipFileType(fileName, readonly, ftc);

    if (fileNameLC.endsWith(FileType.CR) || fileNameLC.endsWith(FileType.XML))
      return new FileType(fileName, readonly);

    return new UnknownFileType(fileName, readonly);
  }

  private static final String ENDINGS[] = new String[] { FileType.CR, FileType.XML };

  /**
   * This method either loads a Zipfile and checks it content for a CR file or it creates a new
   * Zipfile by using ZipOutputStream to create a new ZipFile with one entry. Actually this
   * behaviour works only "sometimes"...wuuuha
   */
  protected FileType createZipFileType(File fileName, boolean readonly, FileTypeChooser ftc)
      throws IOException {
    if (fileName != null && !fileName.exists() && !readonly) {
      // ok, the zipfile doesnt exist and the file mus be writeable
      // in this case we cannot use ZipFile because it reads first
      // the content of the file and returns a ZipFileType with one
      // of the entries in this file...
      String entryName =
          fileName.getName().substring(0, fileName.getName().length() - FileType.ZIP.length())
              + FileType.CR;
      return new ZipFileType(fileName, readonly, entryName);
    }

    ZipFile zFile = new ZipFile(fileName);

    ZipEntry entries[] = ZipFileType.getZipEntries(zFile, FileTypeFactory.ENDINGS);

    if (entries.length == 0)
      throw new NoValidEntryException();

    if (entries.length == 1)
      return new ZipFileType(fileName, readonly, entries[0]);

    // entries > 1, so we need to choose one
    if (ftc == null)
      throw new NotUniqueEntryException();

    ZipEntry chosenEntry = ftc.chooseZipEntry(entries);

    if (chosenEntry == null)
      throw new NotUniqueEntryException();

    return new ZipFileType(fileName, readonly, chosenEntry);
  }

  /**
   * A <code>FileTypeChooser</code> selects single entry if multiple valid entries are found in a
   * compressed file, e.g. multiple .cr files in a .zip file.
   */
  public static class FileTypeChooser {
    /**
     * Selects a <code>ZipEntry</code>.
     * 
     * @param entries an array of ZipEntry objects
     * @return the selected ZipEntry or <code>null</code> if none have been selected.
     */
    public ZipEntry chooseZipEntry(ZipEntry entries[]) {
      return null;
    }
  }

  /**
   * A <code>NotUniqueEntryException</code> shall be thrown if there are multiple valid entries in a
   * compressed file and none have been selected.
   */
  public static class NotUniqueEntryException extends IOException {
  }

  /**
   * A <code>NotValidEntryException</code> shall be thrown if there is no valid entry in a
   * compressed file.
   */
  public static class NoValidEntryException extends IOException {
  }
}
