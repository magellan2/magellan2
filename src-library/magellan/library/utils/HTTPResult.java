// class magellan.library.utils.HTTPResult
// created on 26.02.2008
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
package magellan.library.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Map;

import magellan.library.utils.logging.Logger;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;

/**
 * This is a container for HTTP responses.  
 *
 * @author <a href="mailto:thoralf@m84.de">Thoralf Rickert</a>
 * @version 1.0, erstellt am 02.09.2007
 */
public class HTTPResult {
  /** Log-class */
  private static final Logger log = Logger.getInstance(HTTPResult.class);
  
  protected GetMethod method = null;
  protected int status = 0;
  protected byte[] result = null;
  
  protected Hashtable<String, String> header = new Hashtable<String, String>();
  
  public HTTPResult(GetMethod method, boolean async) {
    this.method = method;
    try {
      status = method.getStatusCode();
      
      if (!async) {
        result = setResult(method.getResponseBodyAsStream());
        Header[] headers = method.getResponseHeaders();
        
        for (Header header : headers) {
          this.header.put(header.getName(),header.getValue());
        }
      }
    } catch (Exception exception) {
      log.error("Konnte GET-Result nicht auslesen. "+exception.getMessage());
    }
  }

  public HTTPResult(PostMethod method) {
    try {
      
      result = setResult(method.getResponseBodyAsStream());
      status = method.getStatusCode();
      Header[] headers = method.getResponseHeaders();
      for (Header header : headers) {
        this.header.put(header.getName(),header.getValue());
      }
      
      
    } catch (Exception exception) {
      log.error("Konnte POST-Result nicht auslesen. "+exception.getMessage());
    }
  }

  /**
   * Liefert das Resultat des Webservers.
   */
  public byte[] getResult() {
    return result;
  }
  
  /**
   * Returns the result from the webserver as a String.
   * This method verifies the given encoding.
   */
  public String getResultAsString() {
    try {
      if (result == null) result = setResult(method.getResponseBodyAsStream());
    } catch (Exception exception) {}
    
    String encoding = getEncoding();
    if (encoding != null) {
      try {
        return new String(result,encoding);
      } catch (Exception exception) {}
    }
    return new String(result);
  }
  
  /**
   * Returns the encoding of the result content.
   */
  public String getEncoding() {
    String contentType = Utils.notNullString(getHeader("Content-Type")).trim();
    String encoding = null;
    if (contentType.contains("charset=")) {
      encoding = contentType.substring(contentType.toLowerCase().indexOf("charset=")+8).trim();
    }
    return encoding;
  }
  
  private byte[] setResult(InputStream stream) throws Exception {
    ByteArrayOutputStream outstream = new ByteArrayOutputStream(4096);
    byte[] buffer = new byte[4096];
    int len;
    while ((len = stream.read(buffer)) > 0) outstream.write(buffer, 0, len);
    outstream.close();
    return outstream.toByteArray();
  }
  
  /**
   * Returns the response body as stream.
   * This stream is only available, if async mode is enabled.
   */
  public InputStream getStream() throws IOException {
    return method.getResponseBodyAsStream();
  }

  /**
   * Liefert den vom Server übermittelten Statuscode.
   */
  public int getStatus() {
    return status;
  }
  
  /**
   * Liefert den Header der Response vom Server.
   */
  public Map<String, String> getHeader() {
    return header;
  }
  
  public String getHeader(String key) {
    if (header.containsKey(key)) return header.get(key);
    return null;
  }
  
  public String toString() {
    return getResultAsString();
  }
}

