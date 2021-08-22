// class magellan.library.utils.JsonAdapter
// created on May 29, 2020
//
// Copyright 2003-2020 by magellan project team
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

import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import magellan.library.utils.logging.Logger;

public class JsonAdapter {
  private static final Logger log = Logger.getInstance(JsonAdapter.class);

  public static Properties parsePropertiesMap(byte[] jsonString) throws IOException {
    return parsePropertiesMap(jsonString, false);
  }

  public static Properties parsePropertiesMap(byte[] jsonString, boolean hierarchic) throws IOException {
    Properties bundle = new Properties();
    try {
      ObjectMapper mapper = new ObjectMapper();
      JsonNode root;
      root = mapper.readTree(jsonString);

      iterateProperties(bundle, "", root, 0, hierarchic);
    } catch (IOException e) {
      String jsonS = new String(jsonString);
      log.error("invalid resource file " + jsonS.substring(0, Math.min(100, jsonS.length())), e);
      throw e;
    }
    return bundle;

  }

  private static void iterateProperties(Properties properties, String prefix, JsonNode node, int depth,
      boolean hierarchic) throws IOException {
    if (node.isValueNode()) {
      if (!hierarchic && depth > 1)
        throw new IOException("hierarchic json in non-hierarchic mode found");
      properties.setProperty(prefix, node.asText());
      return;
    }

    for (Iterator<String> it = node.fieldNames(); it.hasNext();) {
      String child = it.next();
      String label = prefix.isEmpty() ? child : (prefix + "." + child);
      iterateProperties(properties, label, node.get(child), depth + 1, hierarchic);
    }

  }

}