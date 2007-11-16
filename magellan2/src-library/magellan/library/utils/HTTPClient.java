// class magellan.library.utils.HTTPClient
// created on 02.09.2007
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
package magellan.library.utils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import magellan.library.utils.logging.Logger;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.ProxyHost;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.params.HttpMethodParams;

/**
 * This is a small wrapper for the commons http client
 * to initialize the proxy settings and so on...
 *
 * @author <a href="mailto:thoralf@m84.de">Thoralf Rickert</a>
 * @version 1.0, 02.09.2007
 */
public class HTTPClient {
  /** Log-class */
  private static final Logger log = Logger.getInstance(HTTPClient.class);
  
  /** The real client that makes everything */
  protected HttpClient client = null;
  
  /** The proxyserver */
  private ProxyHost proxy = null;

  /**
   * Creates a new HTTP Client to connect to remote HTTP servers.
   */
  public HTTPClient(Properties properties) {
    client = new HttpClient();
    
    setTimeOut(5);
    
    boolean proxyEnabled = new Boolean(properties.getProperty("http.proxy.enabled",String.valueOf(false)));
    String host = properties.getProperty("http.proxy.host");
    int port = new Integer(properties.getProperty("http.proxy.port",String.valueOf(0)));
    if (proxyEnabled && host!=null && host.length()!=0) setProxy(host,port);
    
    client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
    client.getParams().setParameter(HttpMethodParams.USER_AGENT,"magellan-http/1.0");
    client.getParams().setParameter(HttpMethodParams.COOKIE_POLICY, CookiePolicy.IGNORE_COOKIES);
  }
  

  /**
   * Sets the Proxy server.
   */
  public void setProxy(String host, int port) {
    HostConfiguration config = client.getHostConfiguration();
    if (config == null) config = new HostConfiguration();
    config.setProxy(host,port);
    
    proxy = new ProxyHost(host, port); 
  }
  
  /**
   * Sets the Proxy server.
   */
  public void setProxy(Proxy proxy) {
    switch (proxy.type()) {
      case DIRECT: {
        break;
      }
      case HTTP: {
        //...
        break;
      }
      case SOCKS: {
        // ...
        break;
      }
    }
  }
  
  /**
   * Macht einen GET Request.
   */
  public HTTPResult get(String uri) {
    try {
      return get(new URI(uri));
    } catch (Exception exception) {
      log.error("Die URI ist syntaktisch falsch",exception);
    }
    return null;
  }
  
  /**
   * Macht einen GET Request.
   */
  public HTTPResult get(URI uri) {
    GetMethod method = new GetMethod(uri.toString());
    try {
      // verify proxy settings
      ProxySelector selector = ProxySelector.getDefault();
      List<Proxy> proxies = selector.select(uri);
      if (proxies.size()>0) {
        setProxy(proxies.get(0));
      }
      
      client.executeMethod(method);
      return new HTTPResult(method);
    } catch (SocketTimeoutException exception) {
      log.error("Fehler beim Ausführen eines HTTP-GET auf '"+uri+"'. "+exception.getMessage());
    } catch (Exception exception) {
      log.error("Fehler beim Ausführen eines HTTP-GET auf '"+uri+"'",exception);
    }
    
    return null;
  }
  
  /**
   * Macht einen POST Request.
   */
  public HTTPResult post(URI uri, NameValuePair[] parameters) {
    
    PostMethod method = new PostMethod(uri.toString());
    method.setRequestBody(parameters);
    
    try {
      client.executeMethod(method);
      return new HTTPResult(method);
    } catch (SocketTimeoutException exception) {
      log.error("Fehler beim Ausführen eines HTTP-POST auf '"+uri+"'. "+exception.getMessage());
    } catch (Exception exception) {
      log.error("Fehler beim Ausführen eines HTTP-POST auf '"+uri+"'.",exception);
    }
    
    return null;
  }
 
  /**
   * Macht einen POST Request.
   */
  public HTTPResult post(URI uri, String content) {
    return post(uri, content, (Part[])null);
  }
  
  /**
   * Macht einen POST Request.
   */
  public HTTPResult post(String uri, String content) {
    try {
      return post(new URI(uri),content, (Part[])null);
    } catch (Exception exception) {
      log.error("Die URI ist syntaktisch falsch",exception);
    }
    return null;
  }
  
  
  /**
   * Macht einen POST Request.
   */
  public HTTPResult post(URI uri, Part[] parts) {
    
    PostMethod method = new PostMethod(uri.toString());
    
    
    if(parts != null) method.setRequestEntity(new MultipartRequestEntity(parts, method.getParams()));
    
    try {
      client.executeMethod(method);
      return new HTTPResult(method);
    } catch (SocketTimeoutException exception) {
      log.error("Fehler beim Ausführen eines HTTP-POST auf '"+uri+"'. "+exception.getMessage());
    } catch (Exception exception) {
      log.error("Fehler beim Ausführen eines HTTP-POST auf '"+uri+"'.",exception);
    }
    
    return null;
    
  }
  
  /**
   * Macht einen POST Request.
   */
  protected HTTPResult post(URI uri, String content, Part[] parts) {
    PostMethod method = new PostMethod(uri.toString());
    
    method.setRequestEntity(new StringRequestEntity(content));
    method.setRequestHeader("Content-type", "text/xml; charset="+Encoding.DEFAULT);
    
    
    if(parts != null) method.setRequestEntity(new MultipartRequestEntity(parts, method.getParams()));
    
    try {
      client.executeMethod(method);
      return new HTTPResult(method);
    } catch (SocketTimeoutException exception) {
      log.error("Fehler beim Ausführen eines HTTP-POST auf '"+uri+"'. "+exception.getMessage());
    } catch (Exception exception) {
      log.error("Fehler beim Ausführen eines HTTP-POST auf '"+uri+"'.",exception);
    }
    
    return null;
  }

  /**
   * Macht einen POST Request.
   */
  public HTTPResult post(String uri, String key, String content) {
    try {
      return post(new URI(uri),key,content);
    } catch (Exception exception) {
      log.error("Die URI ist syntaktisch falsch",exception);
    }
    return null;
  }
  
  /**
   * Macht einen POST Request.
   */
  public HTTPResult post(URI uri, String key, String content) {
    PostMethod method = new PostMethod(uri.toString());
    method.addParameter(key,content);
    method.setRequestHeader("Content-type", "text/xml; charset=UTF-8");
    
    
    try {
      client.executeMethod(method);
      return new HTTPResult(method);
    } catch (SocketTimeoutException exception) {
      log.error("Fehler beim Ausführen eines HTTP-GET auf '"+uri+"'. "+exception.getMessage());
    } catch (Exception exception) {
      log.error("Fehler beim Ausführen eines HTTP-POST.",exception);
    }
    
    return null;
  }
  
  /**
   * Setzt den Timeout des Clients auf einen sinnvollen Wert.
   */
  public void setTimeOut(int seconds) {
    client.getHttpConnectionManager().getParams().setConnectionTimeout(seconds*1000);
    client.getHttpConnectionManager().getParams().setSoTimeout(seconds*1000);
  }
  
  /**
   * Setzt die Cookie Policy
   */
  public void setCookiePolicy(boolean accept) {
    
    if(accept)
      client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
    else 
      client.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
  }

  /**
   * Liefert die Cookies 
   */
  public Cookie[] getCookies() {
    return client.getState().getCookies();
  }
}



/**
 * This is a container for HTTP responses.  
 *
 * @author <a href="mailto:thoralf@m84.de">Thoralf Rickert</a>
 * @version 1.0, erstellt am 02.09.2007
 */
class HTTPResult {
  /** Log-class */
  private static final Logger log = Logger.getInstance(HTTPResult.class);
  
  protected int status = 0;
  protected byte[] result = null;
  
  protected Hashtable<String, String> header = new Hashtable<String, String>();
  
  public HTTPResult(GetMethod method) {
    try {
      result = setResult(method.getResponseBodyAsStream());
      status = method.getStatusCode();
      Header[] headers = method.getResponseHeaders();
      
      for (Header header : headers) {
        this.header.put(header.getName(),header.getValue());
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

