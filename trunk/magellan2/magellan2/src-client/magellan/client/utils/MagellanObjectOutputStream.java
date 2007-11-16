// class magellan.client.utils.MagellanObjectOutputStream
// created on 06.07.2007
//
// Copyright 2003-2007 by magellan project team
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

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.io.PrintStream;

import org.apache.commons.codec.binary.Base64;

import magellan.library.utils.logging.Logger;

/**
 * This is a wrapper of the object output stream to write
 * the object data as Strings. We need this for the
 * docking framework that write all data into an ObjectOutputStream
 * instead of XML or ini's.
 * 
 * It's a little bit dirty but we need a human readable version
 * for exchange layouts.
 *
 * @author Thoralf Rickert
 * @version 1.0, 06.07.2007
 */
public class MagellanObjectOutputStream extends ObjectOutputStream {
  private static final Logger log = Logger.getInstance(MagellanObjectOutputStream.class);
  private PrintStream out;
  private static final Base64 base64 = new Base64();

  /**
   * @see ObjectOutputStream
   */
  public MagellanObjectOutputStream(OutputStream out) throws IOException {
    //super(out);
    this.out = new PrintStream(out,true,"UTF-8");
  }
  
  

  /**
   * @see java.io.ObjectOutputStream#write(byte[], int, int)
   */
  @Override
  public void write(byte[] buf, int off, int len) throws IOException {
    byte[] copy = new byte[len];
    System.arraycopy(buf, off, copy, 0, Math.min(buf.length - off, len));
    out.println("byte[]:"+new String(base64.encode(copy)));
  }



  /**
   * @see java.io.ObjectOutputStream#write(byte[])
   */
  @Override
  public void write(byte[] buf) throws IOException {
    throw new UnsupportedOperationException("This write operation isn't wrapped yet.");
  }

  /**
   * @see java.io.ObjectOutputStream#write(int)
   */
  @Override
  public void write(int val) throws IOException {
    throw new UnsupportedOperationException("This write operation isn't wrapped yet.");
  }

  /**
   * @see java.io.ObjectOutputStream#writeBoolean(boolean)
   */
  @Override
  public void writeBoolean(boolean val) throws IOException {
    out.println("boolean:"+val);
  }

  /**
   * @see java.io.ObjectOutputStream#writeByte(int)
   */
  @Override
  public void writeByte(int val) throws IOException {
    throw new UnsupportedOperationException("This write operation isn't wrapped yet.");
  }

  /**
   * @see java.io.ObjectOutputStream#writeBytes(java.lang.String)
   */
  @Override
  public void writeBytes(String str) throws IOException {
    throw new UnsupportedOperationException("This write operation isn't wrapped yet.");
  }

  /**
   * @see java.io.ObjectOutputStream#writeChar(int)
   */
  @Override
  public void writeChar(int val) throws IOException {
    throw new UnsupportedOperationException("This write operation isn't wrapped yet.");
  }

  /**
   * @see java.io.ObjectOutputStream#writeChars(java.lang.String)
   */
  @Override
  public void writeChars(String str) throws IOException {
    throw new UnsupportedOperationException("This write operation isn't wrapped yet.");
  }

  /**
   * @see java.io.ObjectOutputStream#writeClassDescriptor(java.io.ObjectStreamClass)
   */
  @Override
  protected void writeClassDescriptor(ObjectStreamClass desc) throws IOException {
    throw new UnsupportedOperationException("This write operation isn't wrapped yet.");
  }

  /**
   * @see java.io.ObjectOutputStream#writeDouble(double)
   */
  @Override
  public void writeDouble(double val) throws IOException {
    throw new UnsupportedOperationException("This write operation isn't wrapped yet.");
  }

  /**
   * @see java.io.ObjectOutputStream#writeFloat(float)
   */
  @Override
  public void writeFloat(float val) throws IOException {
    out.println("float:"+val);
  }

  /**
   * @see java.io.ObjectOutputStream#writeInt(int)
   */
  @Override
  public void writeInt(int val) throws IOException {
    out.println("int:"+val);
  }

  /**
   * @see java.io.ObjectOutputStream#writeLong(long)
   */
  @Override
  public void writeLong(long val) throws IOException {
    throw new UnsupportedOperationException("This write operation isn't wrapped yet.");
  }

  /**
   * @see java.io.ObjectOutputStream#writeShort(int)
   */
  @Override
  public void writeShort(int val) throws IOException {
    throw new UnsupportedOperationException("This write operation isn't wrapped yet.");
  }

  /**
   * @see java.io.ObjectOutputStream#writeUnshared(java.lang.Object)
   */
  @Override
  public void writeUnshared(Object obj) throws IOException {
    throw new UnsupportedOperationException("This write operation isn't wrapped yet.");
  }

  /**
   * @see java.io.ObjectOutputStream#writeUTF(java.lang.String)
   */
  @Override
  public void writeUTF(String str) throws IOException {
    out.println("string:"+str);
  }


  /**
   * @see java.io.ObjectOutputStream#close()
   */
  @Override
  public void close() throws IOException {
    out.close();
  }


  /**
   * @see java.io.ObjectOutputStream#flush()
   */
  @Override
  public void flush() throws IOException {
    out.flush();
  }

  
}
