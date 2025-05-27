// class magellan.library.utils.JsonAdapterTest
// created on May 30, 2020
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonAdapterTest {

  @Test
  public void testParsePropertiesMap() throws IOException {
    Properties props = JsonAdapter.parsePropertiesMap("{ \"key1\" : \"hodor\", \"key2\" : \"frodo\" }".getBytes());

    assertEquals("hodor", props.getProperty("key1"));
  }

  @Test
  public void testFindMapEntryByteArrayStringArray() throws IOException {
    Properties props = JsonAdapter.parsePropertiesMap("{ \"key1\" : { \"subkey\" : \"hodor\"}, \"key2\" : \"frodo\" }"
        .getBytes(), true);
    assertEquals("hodor", props.get("key1.subkey"));
    try {
      JsonAdapter.parsePropertiesMap("{ \"key1\" : { \"subkey\" : \"hodor\"}, \"key2\" : \"frodo\" }"
          .getBytes(), false);
      fail();
    } catch (IOException e) {
      // good
    }
  }

  @SuppressWarnings("rawtypes")
  @Test
  public void testJackson() throws JsonMappingException, JsonProcessingException {
    String locations = "{\n" +
        "\"www.root\" : \"http://magellan2.github.io\",\n" +
        "\"www.bugtracker\" : \"http://magellan2.github.io/bugs/\",\n" +
        "\"www.download\" : \"https://magellan2.github.io/de/download/\",\n" +
        "\"www.download.de\" : \"https://magellan2.github.io/de/download/\",\n" +
        "\"www.download.en\" : \"https://magellan2.github.io/en/download/\",\n" +
        "\"www.files\" : \"http://sourceforge.net/projects/magellan-client/files/\",\n" +
        "\"www.fernando\" : \"http://en.wikipedia.org/wiki/Ferdinand_Magellan\",\n" +
        "\"version.release\" : \"http://magellan2.github.io/release/VERSION\",\n" +
        "\"version.nightly\" : \"http://magellan2.github.io/latest/VERSION\"\n" +
        "}";
    String versions = "{\n" +
        "    \"versions\": {\n" +
        "        \"latest\": {\n" +
        "            \"raw\": \"v1.0.3-7.latest\",\n" +
        "            \"major\": \"1\",\n" +
        "            \"minor\": \"0\",\n" +
        "            \"revision\": \"3\",\n" +
        "            \"pre\": \"7.latest\",\n" +
        "            \"build\": \"7\",\n" +
        "            \"type\": \"latest\"\n" +
        "        },\n" +
        "        \"stable\": {\n" +
        "            \"raw\": \"v1.0.3-2\",\n" +
        "            \"major\": \"1\",\n" +
        "            \"minor\": \"0\",\n" +
        "            \"revision\": \"3\",\n" +
        "            \"pre\": \"2\",\n" +
        "            \"build\": \"2\",\n" +
        "            \"type\": \"\"\n" +
        "        }\n" +
        "    }\n" +
        "}";
    ObjectMapper mapper = new ObjectMapper();

    Map mapl = mapper.readValue(locations, Map.class);
    Map mapv = mapper.readValue(versions, Map.class);
    assertEquals("http://magellan2.github.io/latest/VERSION", mapl.get("version.nightly"));
    assertEquals("7.latest", ((Map) ((Map) mapv.get("versions")).get("latest")).get("pre"));

    JsonNode root = mapper.readTree(versions);
    assertEquals("7.latest", root.get("versions").get("latest").get("pre").asText());
    assertNotNull(root.path("foo").path("bar"));
    assertEquals("", root.path("foo").path("bar").asText());
  }

}
