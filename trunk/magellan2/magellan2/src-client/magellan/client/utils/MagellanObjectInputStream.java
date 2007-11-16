// class magellan.client.utils.MagellanObjectInputStream
// created on 08.07.2007
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.ObjectInputStream;

import org.apache.commons.codec.binary.Base64;

/**
 * This is a wrapper of the object input stream to reads
 * the object data as Strings. We need this for the
 * docking framework that reads all data from an ObjectInputStream
 * instead of XML or ini's.
 * 
 * It's a little bit dirty but we need a human readable version
 * for exchange layouts.
 *
 * @author ...
 * @version 1.0, 08.07.2007
 */
public class MagellanObjectInputStream extends ObjectInputStream {
  private LineNumberReader in;
  private static final Base64 base64 = new Base64();

  /**
   * 
   */
  public MagellanObjectInputStream(InputStream in) throws IOException {
    this.in = new LineNumberReader(new InputStreamReader(in,"UTF-8"));
  }

  /**
   * @see java.io.ObjectInputStream#read(byte[], int, int)
   */
  @Override
  public int read(byte[] buf, int off, int len) throws IOException {
    String line = in.readLine();
    if (line == null || !line.startsWith("byte[]:")) throw new IOException("Could not read byte[] from input stream");
    String data = line.substring("byte[]:".length());
    byte[] abuf = base64.decode(data.getBytes());
    int x=0;
    for (int i=off; i<(off+len); i++) {
      buf[i] = abuf[x];
      x++;
    }
    return abuf.length;
  }

  /**
   * @see java.io.ObjectInputStream#read(byte[])
   */
  @Override
  public int read(byte[] buf) throws IOException {
    String line = in.readLine();
    if (line == null || !line.startsWith("byte[]:")) throw new IOException("Could not read byte[] from input stream");
    String data = line.substring("byte[]:".length());
    byte[] abuf = base64.decode(data.getBytes());
    for (int i=0; i<abuf.length; i++) {
      buf[i] = abuf[i];
    }
    return abuf.length;
  }

  /**
   * @see java.io.ObjectInputStream#readBoolean()
   */
  @Override
  public boolean readBoolean() throws IOException {
    String line = in.readLine();
    if (line == null || !line.startsWith("boolean:")) throw new IOException("Could not read boolean from input stream");
    String data = line.substring("boolean:".length());
    return Boolean.parseBoolean(data);
  }

  /**
   * @see java.io.ObjectInputStream#readFloat()
   */
  @Override
  public float readFloat() throws IOException {
    String line = in.readLine();
    if (line == null || !line.startsWith("float:")) throw new IOException("Could not read float from input stream");
    String data = line.substring("float:".length());
    return Float.parseFloat(data);
  }

  /**
   * @see java.io.ObjectInputStream#readInt()
   */
  @Override
  public int readInt() throws IOException {
    String line = in.readLine();
    if (line == null || !line.startsWith("int:")) throw new IOException("Could not read int from input stream");
    String data = line.substring("int:".length());
    return Integer.parseInt(data);
  }
  
  
  /**
   * @see java.io.ObjectInputStream#readUTF()
   */  
  @Override
  public String readUTF() throws IOException {
    String line = in.readLine();
    if (line == null || !line.startsWith("string:")) throw new IOException("Could not read string from input stream");
    String data = line.substring("string:".length());
    return data;
  }

  /**
   * @see java.io.ObjectInputStream#close()
   */
  @Override
  public void close() throws IOException {
    in.close();
  }

  
}
