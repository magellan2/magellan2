// class magellan.library.io.cr.AbstractReportParser
// created on Apr 15, 2013
//
// Copyright 2003-2013 by magellan project team
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
package magellan.library.io;

import java.io.IOException;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import magellan.library.Building;
import magellan.library.CoordinateID;
import magellan.library.EntityID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Rules;
import magellan.library.Ship;
import magellan.library.Unit;
import magellan.library.UnitID;
import magellan.library.io.file.FileType;
import magellan.library.utils.MagellanFactory;
import magellan.library.utils.Resources;
import magellan.library.utils.UserInterface;
import magellan.library.utils.logging.Logger;
import magellan.library.utils.transformation.ReportTransformer;

public abstract class AbstractReportParser implements ReportParser {
  protected static final Logger log = Logger.getInstance(AbstractReportParser.class);

  protected GameData world;
  protected String game;
  protected int version = 0;
  protected UserInterface ui = null;
  protected Faction firstFaction;
  protected ReportTransformer transformer;
  protected int errors;

  protected static final String number = "[\\+\\-]?\\d+";

  /**
   * Translates c by newOrigin if it's in the same z-level and returns it.
   */
  protected CoordinateID originTranslate(CoordinateID c) {
    return transformer.transform(c);
  }

  /**
   * Transform a coordinate translation: mirror at origin, translate, mirror again.
   */
  protected CoordinateID transformTranslation(CoordinateID oldTranslation) {
    CoordinateID zero = CoordinateID.create(0, 0, oldTranslation.getZ());
    return zero.inverseTranslateInLayer(transformer.transform(zero
        .inverseTranslateInLayer(oldTranslation)));
  }

  protected static String astral = Resources.get("rules.astralspacecoordinate");
  protected static String coordPattern;
  protected static String regionsPattern1;
  protected static String regionsPattern2;

  static {
    buildPattern();
  }

  protected static void buildPattern() {
    // check if locale has changed
    if (coordPattern == null || !astral.equals(Resources.get("rules.astralspacecoordinate"))) {
      astral = Resources.get("rules.astralspacecoordinate");
      // \((number)\,\ ?(number)(\,\ ?((number)|Astralraum))?\)
      // \(([\+\-]?\d+)\,\ ?([\+\-]?\d+)(\,\ ?(([\+\-]?\d+)|Astralraum))?\)
      coordPattern =
          "\\((" + number + ")\\,\\ ?(" + number + ")(\\,\\ ?((" + number + ")|" + astral
              + "))?\\)";
      // \(([\+\-]?\d+) ([\+\-]?\d+)( (([\+\-]?\d+)|Astralraum))?\)
      regionsPattern1 =
          "\\((" + number + ") (" + number + ")( ((" + number + ")|" + astral + "))?\\)";
      // \(([\+\-]?\d+) ([\+\-]?\d+)( (([\+\-]?\d+)|Astralraum))?\)*
      regionsPattern2 =
          "\\((" + number + ") (" + number + ")( ((" + number + ")|" + astral + "))?\\)*";
    }
  }

  public AbstractReportParser() {
    super();
  }

  /**
   * special sub to translate coords in ";regions" tags of messages expecting this form
   * <code>"x1 y1 z1, x2 y2 z2";regions</code>.<br />
   * There is also an older variant: <code>"der Sumpf von Rudros (-7,23)";regions</code>
   *
   * @param value
   */
  protected String originTranslateRegions(String value) {
    final StringBuffer result = new StringBuffer();
    buildPattern();
    String content = value.replace("\"", "");

    if (content.matches(regionsPattern2)) {
      final Matcher matcher = Pattern.compile(regionsPattern1).matcher(content);
      while (matcher.find()) {
        final String candi = matcher.group();
        // candi=candi.replaceAll(astral,
        // world.getGameSpecificRules().getAstralSpacePlane());
        CoordinateID coord = CoordinateID.parse(candi, " ");
        if (coord != null) {
          coord = originTranslate(coord);
          matcher.appendReplacement(result, "(" + coord.toString(" ") + ")");
        } else {
          matcher.appendReplacement(result, matcher.group());
        }
      }
      matcher.appendTail(result);
      return result.toString();
    } else
      return originTranslate(content);
  }

  /**
   * Tries to replace coordinates in string by the translated version. The string is searched for
   * occurrences of the form "(123,123)" or "(123,123,123)" or "(123,123,Astralraum)", transforms them
   * and replaces them. This is not completely fool-proof!
   *
   * @param value Usually a message text which might contain coordinates
   * @see magellan.library.utils.transformation.ReportTransformer#transform(CoordinateID)
   */
  protected String originTranslate(String value) {
    final StringBuffer result = new StringBuffer();
    buildPattern();
    final Matcher matcher = Pattern.compile(coordPattern).matcher(value);
    while (matcher.find()) {
      final String candi = matcher.group();
      // candi=candi.replaceAll(astral,
      // world.getGameSpecificRules().getAstralSpacePlane());
      CoordinateID coord = CoordinateID.parse(candi.substring(1, candi.length() - 1), ",");
      if (coord != null) {
        coord = transformer.transform(coord);
        matcher.appendReplacement(result, "(" + coord.toString() + ")");
      } else {
        matcher.appendReplacement(result, matcher.group());
      }
    }
    matcher.appendTail(result);
    return result.toString();
  }

  /**
   * Helper function: Find a faction in world. If not found, create one and insert it.
   */
  protected Faction getAddFaction(EntityID id) {
    Faction faction = world.getFaction(id);

    if (faction == null) {
      faction = MagellanFactory.createFaction(id, world);
      world.addFaction(faction);
    }

    return faction;
  }

  /**
   * Helper function: Find a unit in world. If not found, create one and insert it.
   */
  protected Unit getAddUnit(UnitID id, boolean old) {
    if (old) {
      Unit unit = world.getOldUnit(id);

      if (unit == null) {
        unit = MagellanFactory.createUnit(id, world);
        world.addOldUnit(unit);
      }

      return unit;
    } else {
      Unit unit = world.getUnit(id);

      if (unit == null) {
        unit = MagellanFactory.createUnit(id, world);
        world.addUnit(unit);
      }

      return unit;
    }
  }

  /**
   * Helper function: Find a building in world. If not found, create one and insert it.
   */
  protected Building getAddBuilding(EntityID id) {
    Building building = world.getBuilding(id);

    if (building == null) {
      building = MagellanFactory.createBuilding(id, world);
      world.addBuilding(building);
    }

    return building;
  }

  /**
   * Helper function: Find a ship in world. If not found, create one and insert it.
   */
  protected Ship getAddShip(EntityID id) {
    Ship ship = world.getShip(id);

    if (ship == null) {
      ship = MagellanFactory.createShip(id, world);
      world.addShip(ship);
    }

    return ship;
  }

  /**
   * @return The first faction encountered while parsing, <code>null</code> if not applicable
   */
  public Faction getFirstFaction() {
    return firstFaction;
  }

  public int getErrors() {
    return errors;
  }

  public void setUI(UserInterface aui) {
    ui = aui;
  }

  public void setTransformer(ReportTransformer coordinateTransformer) {
    transformer = coordinateTransformer;
  }

  /**
   * @see magellan.library.io.ReportParser#read(magellan.library.io.file.FileType,
   *      magellan.library.Rules)
   */
  public GameData read(FileType aFileType, Rules rules) throws IOException {
    GameData newData =
        rules.getGameSpecificStuff().createGameData(rules.getGameSpecificStuff().getName());
    newData.setFileType(aFileType);
    Reader reader = aFileType.createReader();

    try {
      log.info("Loading report " + aFileType.getName());
      return read(reader, newData);
    } finally {
      try {
        reader.close();
      } catch (IOException e) {
        log.error(e);
        // can't do much here
      }
    }
  }

}