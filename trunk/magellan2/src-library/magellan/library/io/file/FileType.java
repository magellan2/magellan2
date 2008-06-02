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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import magellan.library.io.BOMReader;
import magellan.library.utils.Encoding;
import magellan.library.utils.logging.Logger;


/**
 * A FileType represents a file for reading and writing data. Special care will be taken for
 * compressed files in the corresponding child objects.
 */
public class FileType {
  private final static Logger log = Logger.getInstance(FileType.class);

  /** A String representation of the default encoding. */
  public static final Encoding DEFAULT_ENCODING = Encoding.ISO;

    
  // basically identified file types
  public static final String CR = ".cr";
  public static final String XML = ".xml";

  // basically identified compression types with single entry
  static final String GZIP = ".gz";
  static final String BZIP2 = ".bz2";

  // basically identified compression types with multiple entries
  static final String ZIP = ".zip";

  /** The file this file type identifies. */
  protected File filename;

  /** true iff file is readonly. */
  protected boolean readonly = false;
  protected boolean createBackup = true;
  
  private BOMReader reader;

  FileType(File aFile, boolean readonly) throws IOException {
    if(aFile == null) {
      throw new IOException();
    }

    filename = aFile;

    this.readonly = readonly;
  }

  /**
   * Sets if file is readonly
   *
   * 
   */
  public void setReadonly(boolean readonly) {
    this.readonly = readonly;
  }

  /**
   * DOCUMENT-ME
   *
   * 
   */
  public void setCreateBackup(boolean aCreateBackup) {
    createBackup = aCreateBackup;
  }

  /**
   * Tests if an InputStream can be opened for this FileType.
   *
   * @return <code>this</code>
   *
   * @throws IOException
   */
  public FileType checkConnection() throws IOException {
    try {
//            if(readonly) {
                createInputStream().close();
//            } else {
//                createOutputStream().close();
//            }
    } catch(FileNotFoundException e) {
      // if file is readonly, this will be a problem
      // if not, it may be ok that the file does not exist 
      if(readonly) {
        throw e;
      }
    }

    return this;
  }

  /**
   * Returns the underlying file.
   *
   * @return a File object
   *
   * @throws IOException if file cannot be determined, e.g. for  an url pointing to an
   *        InputStream.
   */
  public File getFile() throws IOException {
    return new File(getName());
  }

  /**
   * Returns the most inner name of the FileType. Will be overwritten in ZipFileType
   *
   * @return the most inner name of a FileType.
   */
  public String getInnerName() {
    return getName();
  }


  /**
   * Returns the name of the FileType.
   *
   * @return the name of the FileType
   */
  public String getName() {
    return filename.getAbsolutePath();
  }

  /**
   * Returns a String representation of the FileType.
   *
   * @return a String representation of the FileType.
   */
  public String toString() {
    if(getInnerName() == null) {
      return getName();
    } else {
      return getName() + " (" + getInnerName() + ")";
    }
  }

  /**
   * Creates a Reader for this FileType.
   *
   * @return a Reader of the underlying File.
   *
   * @throws IOException
   */
  public Reader createReader() throws IOException {
    reader = null;
    String encoding = getEncoding();
    reader = FileType.createEncodingReader(createInputStream(),encoding);
    return reader;
  }

  /**
   * Creates a backup of the underlying file and returns  a Writer for this.
   *
   * @return a Writer of the underlying File.
   *
   * @throws ReadOnlyException If file is marked as readonly or cannot be opened
   * @throws IOException If another IOException occured
   */
  public Writer createWriter(String encoding) throws IOException {
    return createWriter(encoding, FileBackup.DEFAULT_BACKUP_LEVEL);
  }

  /**
   * Creates a backup of the underlying file and returns  a Writer for this.
   *
   * @return a Writer of the underlying File.
   *
   * @throws ReadOnlyException If file is marked as readonly or cannot be opened
   * @throws IOException If another IOException occured
   */
  public Writer createWriter(String encoding,int numberOfBackups) throws IOException {
    if(readonly) {
      throw new ReadOnlyException();
    }
    
    if(createBackup && filename.exists() && filename.canWrite()) {
      File backup = FileBackup.create(filename,numberOfBackups);
      log.info("Created backupfile " + backup +" (FileType.java)");
    }
    
    if (filename.exists() && !filename.canWrite())
      throw new IOException("cannot write "+filename);

    return new BufferedWriter(FileType.createEncodingWriter(createOutputStream(),encoding));
  }
  
  
  /**
   * Creates an InputStream for the underlying file.
   *
   * @return an InputStream of the underlying file.
   *
   * @throws IOException
   */
  protected InputStream createInputStream() throws IOException {
    return new FileInputStream(filename);
  }

  /**
   * Creates an OutputStream for the underlying file.
   *
   * @return an OutputStream of the underlying file.
   *
   * @throws IOException
   */
  protected OutputStream createOutputStream() throws IOException {
    return new FileOutputStream(filename);
  }

  
  /**
   * Creates a Reader.
      *
   * Tries to determine encoding by looking at the BOM. If it cannot determine the encoding, the
   * given <code>encoding</code> is used.
   *
   * @param is the InputStream
   *
   * @return a Reader for the given InputStream
   *
   * @throws IOException
   */
  public static BOMReader createEncodingReader(InputStream is, String encoding) throws IOException {
    return new BOMReader(is, encoding);
  }

  /**
   * Creates a Writer with the specified encoding.
   *
   * @param os the OutputStream
   * @param encoding
   *
   * @return a Writer for the given OutputStream
   *
   * @throws IOException
   */
  public static OutputStreamWriter createEncodingWriter(OutputStream os, String encoding) throws IOException {
    return new OutputStreamWriter(os, encoding);
  }

  /**
   * Determines, whether a file is of XML filetype, moved from
   * com.eressea.io.GameDataReader
   * by Jonathan 20060917 (Fiete)
   *
   * @return true, if the file is of XML type
   *
   * @throws IOException
   */
  public boolean isXMLFile() throws IOException {
        return getInnerName().endsWith(FileType.XML);
  }

  /**
   * Determines, whether a file is of CR filetype, moved from
   * com.eressea.io.GameDataReader
   * by Jonathan 20060917 (Fiete)
   *
   * @return true, if the file is of CR type or of unknown type
   *
   * @throws IOException
   */
  public boolean isCRFile() throws IOException {
        /* Unknown files are treated like CR files
         */
        return getInnerName().endsWith(FileType.CR) || this instanceof UnknownFileType;
  }

  /**
   * Determines, whether a file is a ZIP filetype
   * by Jonathan 20060917 (Fiete)
   * @return true, if the file is of ZIP type
   *
   * @throws IOException
   */
  public boolean isZIPFile() throws IOException {
        return this instanceof ZipFileType;
  }

  /**
   * Determines, whether a file is a GZIP filetype
   * by Jonathan 20060917 (Fiete)
   * @return true, if the file is of GZIP type
   *
   * @throws IOException
   */
  public boolean isGZIPFile() throws IOException {
        return this instanceof GZipFileType;
  }

  /**
   * Determines, whether a file is a BZIP2 filetype
   * by Jonathan 20060917 (Fiete)
   * @return true, if the file is of BZIP2 type
   *
   * @throws IOException
   */
  public boolean isBZIP2File() throws IOException {
        return this instanceof BZip2FileType;
  }  
  
  /**
   * TODO: DOCUMENT ME!
   *
   * @author $author$
   * @version $Revision: 476 $
   */
  public static class ReadOnlyException extends IOException {
  }

  /**
   * This method tries to find the encoding tag in
   * the CR file.
   */
  public String getEncoding() {
    if (reader!=null) return reader.getEncoding();
    try {
      
      // use UnicodeReader to determine encoding
      InputStream stream = createInputStream();
      BOMReader localReader = new BOMReader(stream, DEFAULT_ENCODING.toString());
      
      String encoding = findCharset(localReader);

      if (encoding==null){
        // maybe UnicodeReader was wrong, try reading in default encoding
        stream.close();
        stream = createInputStream();
        InputStreamReader fallbackReader = new InputStreamReader(stream, DEFAULT_ENCODING.toString());
        encoding = findCharset(fallbackReader);
      }
      
      stream.close();

      if (encoding == null ){
        log.info("no charset tag found in "+getName());
        encoding=localReader.getEncoding();
      } else if (localReader.hasBOM()!=null && localReader.hasBOM()) {
        String bomEncoding = localReader.getEncoding();
        // okay, here is a problem with the given encoding from the BOM Reader
        // It returns the Encoding without any "-" (UTF8 vs. UTF-8)
        // so we remove all "-" and compare the encodings
        String bomEncoding2 = bomEncoding.replaceAll("-", "");
        String fileEncoding2 = encoding.replaceAll("-", "");
        
        if (!bomEncoding2.equalsIgnoreCase(fileEncoding2)) {
          // UnicodeReader found an encoding different from the encoding of the charset tag
          log.warn("given encoding of "+getName()+" does not match encoding given by BOM. ;charset says "+encoding+" but using "+localReader.getEncoding());
          encoding=localReader.getEncoding();
        }
      }

      return encoding;
      
    } catch (Exception exception) {
      log.error(exception);
    }
    return null;
  }
  
  /**
   * Look for and evaluate the ";charset" tag. 
   *
   * @param localReader
   * @return The value of the tag, if found, else <code>null</code> 
   * @throws IOException 
   */
  private String findCharset(Reader localReader) throws IOException {
    LineNumberReader reader = new LineNumberReader(localReader);

    // read at least 5 lines
    String line;
    String encoding = DEFAULT_ENCODING.toString();
    int counter = 0;
    while ((line = reader.readLine()) != null) {
      if (line.lastIndexOf(";charset") > 0) {
        // found line with charset. Format is "<encoding>";charset
        return encoding = line.substring(1, line.indexOf(";charset") - 1);
      }
      counter++;
      if (counter >= 5) break;
    }
    return encoding;
  }
}
