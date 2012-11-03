// class magellan.library.HasCache
// created on Aug 3, 2008
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
package magellan.library;

import magellan.library.utils.Cache;
import magellan.library.utils.CacheHandler;

public interface HasCache {

  /**
   * Returns <code>false</code> if this unit does not have a cache. The reverse is not always true:<br/>
   * If this method returns <code>false</code>, a subsequent call to getCache() will always create a
   * new cache.<br/>
   * If this method returns <code>true</code>, a subsequent call to getCache() may or may not create
   * a new cache.<br/>
   */
  public abstract boolean hasCache();

  /**
   * Returns the value of cache. Creates it if necessary.
   * 
   * @return Returns cache.
   */
  public abstract Cache getCache();

  /**
   * Sets the value of cache.
   * 
   * @param cache The value for cache.
   */
  public abstract void setCache(Cache cache);

  /**
   * Releases the memory for the cache.
   */
  public abstract void clearCache();

  /**
   * Register a {@link CacheHandler}.
   */
  public void addCacheHandler(CacheHandler handler);

}