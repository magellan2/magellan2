// class magellan.library.utils.ResourcePathClassLoader
// created on 19.05.2007
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import magellan.library.utils.logging.Logger;


/**
 * Loads classes and resources from a configurable set of places. Well-known local directories
 * and/or contents of the executed jar file are used as fall-back resourcePaths.
 */
public class ResourcePathClassLoader extends ClassLoader {
  private static final Logger log = Logger.getInstance(ResourcePathClassLoader.class);
  private List<URL> resourcePaths = new ArrayList<URL>();
  
  /**
   * Creates a new class loader initializing itself with the specified settings.
   */
  public ResourcePathClassLoader(Properties settings) {
    this.resourcePaths = loadResourcePaths(settings);
  }

  /**
   * Loads the resource paths from the specified settings.
   */
  private static List<URL> loadResourcePaths(Properties settings) {
    Collection<String> properties = PropertiesHelper.getList(settings, "Resources.preferredPathList");
    List<URL> resourcePaths = new ArrayList<URL>(properties.size());

    for(Iterator<String> iter = properties.iterator(); iter.hasNext();) {
      String location = iter.next();

      try {
        resourcePaths.add(new URL(location));
      } catch(MalformedURLException e) {
        log.error(e);
      } 
    }
    
    return resourcePaths;
  }

  /**
   * Returns the resource paths this loader operates on.
   */
  public Collection<URL> getPaths() {
    return Collections.unmodifiableCollection(this.resourcePaths);
  }

  /**
   * Finds the resource with the given name.
   *
   * @param name the resource name
   *
   * @return a URL for reading the resource, or <code>null</code> if the resource could not be
   *       found
   * @see ClassLoader#findResource(String)
   */
  protected URL findResource(String name) {
    return ResourcePathClassLoader.getResource(name, this.resourcePaths);
  }
  

  /**
   * Finds the resource with the given name in the given resource paths.
   *
   * @param aName The resource name, a relative path!
   * @param resourcePaths Additional resource paths to find the resource in
   * 
   * @return A URL for reading the resource, or <code>null</code> if the resource could not be
   *       found
   */
  protected static URL getResource(String aName, Collection<URL> resourcePaths) {
    URL url = null;
    log.debug("trying to find \""+aName+"\"");

    //String name = Umlaut.replace(aName," ","\\ ");
    String name=aName;
    
    url = getResourceFromPaths(name, resourcePaths);

    if(url == null) {
      url = getResourceFromCurrentDir(name);
    }

    // try to get the resource from the class path
    if(url == null) {
      url = getSystemClassLoader().getResource(name);
    }

    // try to get the resource from the jar
    if(url == null) {
      // FFTest...do not use \ but / (Fiete)
      String myName = name.replace("\\".charAt(0), "/".charAt(0));
      url = getSystemClassLoader().getResource(myName);
    }

    if(url != null) {
      // do some ugly tests here because of jvm specification bugs with spaces
      // in filenames inside OR outside a jar file
      if(canOpenURLResource(url) == null) {
        try {
          URL decoded = canOpenURLResource(URLDecoder.decode(url.toString(),"UTF-8"));
          if(decoded != null) {
            url = decoded;
          } else {
            URL encoded = canOpenURLResource(URLEncoder.encode(url.toString(),"UTF-8"));
            if(encoded != null) {
              url = encoded;
            }
          }
        } catch (UnsupportedEncodingException usee) {
          
        }
      }
    }
    return url;
  }

  /**
   * Searches the available resource paths for a resource with the specified name and returns the
   * first match.
   *
   * @param name The resource name, a relative path!
   * @param resourcePaths The collection of resource paths.
   *
   * @return a URL for reading the resource, or <code>null</code> if the resource could not be
   *       found
   */
  private static URL getResourceFromPaths(String name, Collection<URL> resourcePaths) {
    URL url = null;

    for(Iterator<URL> iter = resourcePaths.iterator(); iter.hasNext() && (url == null);) {
      url = verifyResource(iter.next(), name);
    }

    return url;
  }


  /**
   * Checks if the object specified by the base location and the name exists.
   * If it does, a valid URL pointing to is returned else null.
   */
  private static URL verifyResource(URL location, String name) {
    URL url = null;

    try {
      url = new URL(location, name);
      InputStream istream = url.openStream();
      istream.close();
    } catch(Exception ex) {
      url = null;
    }

    return url;
  }


  /**
   * Searches the current directory for a resource with the specified name.
   *
   * @param name The resource name, a relative path!
   *
   * @return a URL for reading the resource, or <code>null</code> if 
   *         the resource could not be found
   */
  private static URL getResourceFromCurrentDir(String name) {
    URL url = null;

    try {
      File currentDirectory = new File(".");
      URL baseLocation = currentDirectory.toURI().toURL();
      url = verifyResource(baseLocation, name);
    } catch(Exception e) {
      log.error(e);
    }

    return url;
  }

  /**
   * 
   */
  private static URL canOpenURLResource(String aUrl) {
    try {
      return canOpenURLResource(new URL(aUrl));
    }  catch (MalformedURLException e) {
      return null;
    }
  }

  /**
   * 
   */
  private static URL canOpenURLResource(URL url) {
    if(url == null) return null;
    try {
      url.openStream().close();
      return url;
    } catch (IOException e) {
      return null;
    } 
  }

  /**
   * @see java.lang.ClassLoader#findClass(java.lang.String)
   */
  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    String fileName = name.replace('.', '/').concat(".class");
    
    URL url = getResourceFromPaths(fileName, resourcePaths);
    if (url == null) url = getResourceFromCurrentDir(fileName);
    if (url == null) return findSystemClass(name);
    
    try {
      InputStream stream = url.openStream();
      List<Byte> buffer = new LinkedList<Byte>();
      int read;
      
      while ((read = stream.read()) != -1) {
        buffer.add(new Byte((byte) read));
      }
      
      stream.close();

      byte buf[] = new byte[buffer.size()];

      for(int i = 0; i < buffer.size(); i++) {
        buf[i] = buffer.get(i).byteValue();
      }
      return defineClass(name, buf, 0, buf.length);
    } catch (Exception exception) {
      throw new ClassNotFoundException(exception.getMessage());
    }
  }
  
  
}
