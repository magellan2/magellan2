// class magellan.library.io.file.MemoryFileType
// created on Jan 15, 2008
//
// Copyright 2003-2008 by magellan project team
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
package magellan.library.io.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Reader;
import java.io.Writer;

import magellan.library.utils.Encoding;

/**
 * A FileType that directly pipes its input (from {@link #createReader()}) to an outputstream (
 * {@link #createOutputStream()}).
 * 
 * @author stm
 */
public class PipeFileType extends FileType {

  private static int SERIAL = 0;

  private PipedOutputStream outputStream;
  private PipedInputStream inputStream;

  private String encoding = Encoding.DEFAULT.toString();

  private final int serial = PipeFileType.SERIAL++;

  /**
   * Creates a pipe file. Everything written to the reader given by {@link #createReader()} will be
   * directly written to the writer given by {@link #createWriter(String)}. There is no actual file
   * involved.
   * 
   * @throws IOException if an I/O error occurs.
   */
  public PipeFileType() throws IOException {
    super(new File(""), false);
    outputStream = new PipedOutputStream();
    inputStream = new PipedInputStream(outputStream);
  }

  /**
   * @see magellan.library.io.file.FileType#createInputStream()
   */
  @Override
  protected InputStream createInputStream() throws IOException {
    return inputStream;
  }

  /**
   * @see magellan.library.io.file.FileType#createOutputStream()
   */
  @Override
  protected OutputStream createOutputStream() throws IOException {
    return outputStream;
  }

  @Override
  public FileType checkConnection() throws IOException {
    return this;
  }

  /**
   * A PipeFileType does not create backups.
   * 
   * @see magellan.library.io.file.FileType#createWriter(java.lang.String)
   */
  @Override
  public Writer createWriter(String encoding) throws IOException {
    return new BufferedWriter(new OutputStreamWriter(outputStream, encoding));
  }

  /**
   * A PipeFileType does not create backups.
   * 
   * @see magellan.library.io.file.FileType#createWriter(java.lang.String, int)
   */
  @Override
  public Writer createWriter(String encoding, int numberOfBackups) throws IOException {
    return new BufferedWriter(new OutputStreamWriter(outputStream, encoding));
  }

  @Override
  public Reader createReader() throws IOException {
    return new BufferedReader(new InputStreamReader(inputStream, encoding));
  }

  @Override
  public String getEncoding() {
    return encoding;
  }

  /**
   * Changes the encoding.
   * 
   * @param _encoding
   */
  public void setEncoding(String _encoding) {
    encoding = _encoding;
  }

  /**
   * Returns <code>null</code>.
   * 
   * @see magellan.library.io.file.FileType#getFile()
   */
  @Override
  public File getFile() throws IOException {
    return null;
  }

  /**
   * Returns an empty string.
   * 
   * @see magellan.library.io.file.FileType#getInnerName()
   */
  @Override
  public String getInnerName() {
    return "";
  }

  /**
   * Returns an empty string.
   * 
   * @see magellan.library.io.file.FileType#getName()
   */
  @Override
  public String getName() {
    return "";
  }

  @Override
  public boolean isBZIP2File() throws IOException {
    return false;
  }

  /**
   * @see magellan.library.io.file.FileType#isCRFile()
   * @deprecated we don't seem to need any of this any more
   */
  @Deprecated
  @Override
  public boolean isCRFile() throws IOException {
    return true;
  }

  @Override
  public boolean isGZIPFile() throws IOException {
    return false;
  }

  @Override
  public boolean isXMLFile() throws IOException {
    return false;
  }

  @Override
  public boolean isZIPFile() throws IOException {
    return false;
  }

  @Override
  public void setCreateBackup(boolean createBackup) {
    throw new UnsupportedOperationException("not available for PipeFileType");
  }

  @Override
  public void setReadonly(boolean readonly) {
    if (readonly)
      throw new UnsupportedOperationException("not available for PipeFileType");
  }

  @Override
  public String toString() {
    return "PIPE" + serial;
  }

}
