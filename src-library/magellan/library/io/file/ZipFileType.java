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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * TODO: ZipEntry may also be a "normal" FileType
 * 
 * @author $Author: $
 * @version $Revision: 305 $
 */
public class ZipFileType extends FileType {
  protected ZipEntry zipentry = null;

  protected String entryName = null;

  ZipFileType(File aFile, boolean readonly, ZipEntry aEntry) throws IOException {
    super(aFile, readonly);

    if (aEntry == null)
      throw new FileTypeFactory.NoValidEntryException();

    zipentry = new ZipEntry(aEntry);
  }

  /**
   * This constructor is used for Zip files that must be created. Attention: The file aFile doesn't
   * really exist.
   */
  ZipFileType(File aFile, boolean readonly, String entryName) throws IOException {
    super(aFile, readonly);

    this.entryName = entryName;
  }

  /**
   * Returns the most inner name of the FileType. Will be overwritten in ZipFileType
   * 
   * @return the most inner name of a FileType.
   */
  @Override
  public String getInnerName() {
    if (entryName != null)
      return entryName;
    return zipentry.getName();
  }

  /**
   * Returns all files inside the zip ending with one of the given endings case insensitive
   * 
   * @param zip a ZipFile to inspect
   * @param endings an array of valid file name endings.
   * @return an array of valid zip file entries
   */
  public static ZipEntry[] getZipEntries(ZipFile zip, String endings[]) {
    Collection<ZipEntry> ret = new ArrayList<ZipEntry>();

    for (Enumeration<?> iter = zip.entries(); iter.hasMoreElements();) {
      ZipEntry entry = (ZipEntry) iter.nextElement();
      String entryName = entry.getName();

      for (String ending : endings) {
        if (entryName.toLowerCase().endsWith(ending)) {
          ret.add(entry);

          break;
        }
      }
    }

    return ret.toArray(new ZipEntry[] {});
  }

  /**
   * @see magellan.library.io.file.FileType#createInputStream()
   */
  @Override
  protected InputStream createInputStream() throws IOException {
    if (entryName != null)
      // this means we cannot handle an input stream - this file doesn't exist yet
      return null;

    InputStream is = new ZipFile(filename).getInputStream(zipentry);

    if (is == null)
      throw new IOException("Cannot read zip entry '" + zipentry + "' in file '" + filename + "',");

    return new BufferedInputStream(is);
  }

  @Override
  protected OutputStream createOutputStream() throws IOException {
    if (entryName != null) {
      // let us create a new file
      ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(filename));
      zos.putNextEntry(new ZipEntry(entryName));

      // TODO is ZipOutputStream already buffered?
      return new BufferedOutputStream(zos);
    }

    // here we need to do something special: all entries are copied expect the named zipentry, which
    // will be overwritten
    File tmpfile = CopyFile.copy(filename);
    try {
      ZipFile zfile = new ZipFile(tmpfile);
      // ZipOutputStream is already buffered (?)
      ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(filename));

      for (Enumeration<?> e = zfile.entries(); e.hasMoreElements();) {
        ZipEntry oldEntry = (ZipEntry) e.nextElement();

        if (!oldEntry.getName().equals(zipentry.getName())) {
          // do not reuse oldEntry but create a new ZipEntry
          zos.putNextEntry(new ZipEntry(oldEntry.getName()));

          InputStream currIn = zfile.getInputStream(oldEntry);
          CopyFile.copyStreams(currIn, zos, false);
          currIn.close();
        }
      }
      zfile.close();

      // do not reuse oldEntry but create a new ZipEntry
      zos.putNextEntry(new ZipEntry(zipentry.getName()));

      return new BufferedOutputStream(zos);
    } catch (IOException exc) {
      CopyFile.copy(tmpfile, filename);
      throw exc;
    }
  }

  /**
   * @see magellan.library.io.file.FileType#checkConnection()
   */
  @Override
  public FileType checkConnection() throws IOException {
    if (entryName != null)
      return this;
    return super.checkConnection();
  }

}
