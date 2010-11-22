// class magellan.library.gamebinding.TransformerFinder
// created on Mar 16, 2010
//
// Copyright 2003-2010 by magellan project team
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
package magellan.library.utils.transformation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import magellan.library.CoordinateID;
import magellan.library.EntityID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.gamebinding.EresseaSpecificStuff;
import magellan.library.gamebinding.MapMergeEvaluator;
import magellan.library.utils.Resources;
import magellan.library.utils.Score;
import magellan.library.utils.SetGirthDialog;
import magellan.library.utils.UserInterface;
import magellan.library.utils.logging.Logger;
import magellan.library.utils.mapping.SavedTranslationsMapping;
import magellan.library.utils.transformation.BoxTransformer.BBox;
import magellan.library.utils.transformation.BoxTransformer.BBoxes;

public class TransformerFinder {
  static final Logger log = Logger.getInstance(TransformerFinder.class);

  private GameData globalData;
  private GameData addedData;
  private UserInterface ui;
  private boolean interactive;
  private String filename;

  // private HashMap<GameData, Boolean> astralInfo;

  private boolean tryBoxes;

  public TransformerFinder(GameData globalData, GameData addedData, UserInterface ui,
      boolean interactive, boolean tryFindingBoxes) {
    this.globalData = globalData;
    this.addedData = addedData;
    this.ui = ui;
    this.interactive = interactive;
    try {
      filename = addedData.getFileType().getFile().getName();
    } catch (IOException e) {
      log.warn("could not get filetype", e);
      filename = "???";
    }
    tryBoxes = tryFindingBoxes;
  }

  /**
   * @return A pair of transformers, the first one for the original report, the second one for the
   *         added report. Or <code>null</code> if no good transformer can be found.
   */
  public ReportTransformer[] getTransformers() {
    // decide, which translation to choose
    Score<CoordinateID> bestTranslation = findRealSpaceTranslation();

    if (bestTranslation != null) {
      log.info("Using this (real) translation: " + bestTranslation.getKey());
      Score<CoordinateID> bestAstralTranslation = findAstralRSpaceTranslation(bestTranslation);

      if (bestAstralTranslation != null) {
        log.info("Using this astral translation: " + bestAstralTranslation.getKey());

        // compute dimension (of the output report)
        BBoxes boxes = new BBoxes();
        if (tryBoxes) {
          boxes = getBoundingBox(bestTranslation.getKey(), bestAstralTranslation.getKey());
        }
        if (boxes == null)
          return null;

        ReportTransformer transformer1 = getTransformer(globalData, boxes);
        ReportTransformer transformer2 =
            getTransformer(boxes, bestTranslation.getKey(), bestAstralTranslation.getKey());
        return new ReportTransformer[] { transformer1, transformer2 };
      }
    }
    return null;
  }

  /**
   * Find best translation in real space
   * 
   * @param addedData
   */
  private Score<CoordinateID> findRealSpaceTranslation() {
    MapMergeEvaluator mhelp = globalData.getGameSpecificStuff().getMapMergeEvaluator();
    Collection<Score<CoordinateID>> translationList =
        mhelp.getDataMappings(globalData, addedData, 0);

    // try to find a translation for the new report's owner faction in the
    // global data's owner faction
    Score<CoordinateID> savedTranslation = findSavedMapping(0);

    // decide, which translation to choose
    Score<CoordinateID> bestTranslation = null;

    if (interactive) {
      bestTranslation = askTranslation(translationList, savedTranslation, 0);
    } else {
      bestTranslation = decideTranslation(translationList, savedTranslation, 0);
    }
    return bestTranslation;
  }

  /**
   * find best astral translation
   */
  private Score<CoordinateID> findAstralRSpaceTranslation(Score<CoordinateID> bestTranslation) {
    MapMergeEvaluator mhelp = globalData.getGameSpecificStuff().getMapMergeEvaluator();
    Score<CoordinateID> bestAstralTranslation = null;
    if (!hasRegionsInLayer(EresseaSpecificStuff.ASTRAL_LAYER, addedData)) {
      bestAstralTranslation =
          new Score<CoordinateID>(CoordinateID.create(0, 0, EresseaSpecificStuff.ASTRAL_LAYER), -1);
    } else if (!hasRegionsInLayer(EresseaSpecificStuff.ASTRAL_LAYER, globalData)) {
      Collection<Score<CoordinateID>> empty = Collections.emptyList();
      if (interactive) {
        bestAstralTranslation = askTranslation(empty, null, EresseaSpecificStuff.ASTRAL_LAYER);
      } else {
        bestAstralTranslation = decideTranslation(empty, null, EresseaSpecificStuff.ASTRAL_LAYER);
      }
    } else {

      Collection<CoordinateID> otherLevels = new ArrayList<CoordinateID>(1);
      otherLevels.add(bestTranslation.getKey());
      Collection<Score<CoordinateID>> astralTranslationList =
          mhelp.getDataMappings(globalData, addedData, EresseaSpecificStuff.ASTRAL_LAYER,
              otherLevels);

      Score<CoordinateID> savedAstralTranslation =
          findSavedMapping(EresseaSpecificStuff.ASTRAL_LAYER);

      if (interactive) {
        bestAstralTranslation =
            askTranslation(astralTranslationList, savedAstralTranslation,
                EresseaSpecificStuff.ASTRAL_LAYER);
      } else {
        bestAstralTranslation =
            decideTranslation(astralTranslationList, savedAstralTranslation,
                EresseaSpecificStuff.ASTRAL_LAYER);
      }
    }
    return bestAstralTranslation;
  }

  private static boolean hasRegionsInLayer(int layer, GameData data) {
    // if (astralInfo == null) {
    // astralInfo = new HashMap<GameData, Boolean>();
    // }
    // if (astralInfo.get(data) == null) {
    boolean found = false;
    for (Region r : data.getRegions()) {
      if (r.getCoordinate().getZ() == layer) {
        found = true;
        break;
      }
    }
    // astralInfo.put(data, found);
    // }
    // return astralInfo.get(data);
    return found;
  }

  /***************************************************************************
   * Stores the translation between the owner faction of global data and added data implied by the
   * given translation.
   */
  public static void storeTranslations(int layer, CoordinateID bestTranslation,
      GameData globalData, GameData addedData) {
    // store found translations
    EntityID newReportOwner = addedData.getOwnerFaction();
    if (newReportOwner == null)
      return;

    if (globalData.getCoordinateTranslation(newReportOwner, layer) != null
        && !globalData.getCoordinateTranslation(newReportOwner, layer).equals(bestTranslation)) {
      log.warn("old translation " + globalData.getCoordinateTranslation(newReportOwner, layer)
          + " inconsistent with new translation " + bestTranslation);
    }
    CoordinateID correct = addedData.getCoordinateTranslation(newReportOwner, layer);
    if (correct != null) {
      correct =
          CoordinateID.create(bestTranslation.getX() + correct.getX(), bestTranslation.getY()
              + correct.getY(), layer);
      globalData.setCoordinateTranslation(newReportOwner, correct);
    }

  }

  private Score<CoordinateID> findSavedMapping(int layer) {
    EntityID newReportOwner = addedData.getOwnerFaction();
    // ask user if necessary
    if (newReportOwner == null && interactive) {
      if (!addedData.getFactions().isEmpty()) {
        Faction firstFaction = addedData.getFactions().iterator().next();

        Object result =
            ui.input(Resources.getFormatted("util.reportmerger.msg.inputowner.msg", filename),
                Resources.get("util.reportmerger.msg.inputowner.title"), addedData.getFactions()
                    .toArray(), firstFaction);
        if (result != null && result instanceof Faction) {
          addedData.setOwnerFaction(((Faction) result).getID());
        }
      }
    }

    CoordinateID translation =
        SavedTranslationsMapping.getSingleton().getMapping(globalData, addedData, layer);
    if (translation != null)
      return new Score<CoordinateID>(translation, 0);
    return null;
  }

  /**
   * Chooses a translation among the translations in translationList and savedTranslation. If no
   * translation is acceptable, a default translation (0,0, layer) is chosen.
   * 
   * @param newReport The report to be merged
   * @param translationList
   * @param savedTranslation
   * @param layer
   * @return The best translation, never <code>null</code>
   */
  private Score<CoordinateID> decideTranslation(Collection<Score<CoordinateID>> translationList,
      Score<CoordinateID> savedTranslation, int layer) {
    Score<CoordinateID> bestTranslation =
        new Score<CoordinateID>(CoordinateID.create(0, 0, layer), -1);

    if (translationList != null && translationList.size() > 0) {
      bestTranslation = Collections.max(translationList);

      log.info("Found " + translationList.size() + " translations in layer " + layer + " for "
          + filename + " (best(maxScore):" + bestTranslation.toString() + ")");

      // Fiete: just see the other translations...
      /*
       * log.info("DEBUG: all found translations:"); for (Score<CoordinateID>myScore :
       * translationList){ log.info(myScore.toString()); }
       */

    } else {
      log.info("No translation in layer " + layer + " found for " + filename);
    }

    // choose the saved translation only if no good translation has been found
    if (savedTranslation != null) {
      if (!bestTranslation.getKey().equals(savedTranslation.getKey())) {
        log.info("Saved translation in layer " + layer + " " + savedTranslation.getKey() + " != "
            + bestTranslation.getKey() + " best translation found.");
        if (bestTranslation.getScore() > 0) {
          log.info("Preferring computed translation");
        }
      }
      if (bestTranslation.getScore() < 0) {
        log.info("Using saved translation in layer " + layer);
        bestTranslation = savedTranslation;
      }
    } else {
      log.info("no known translation (no translation saved in CR)");
    }
    if (bestTranslation.getScore() < 0) {
      log.warn("No good translation found in layer " + layer);
    }

    return bestTranslation;
  }

  /**
   * Chooses a translation among the translations in translationList and savedTranslation. If no
   * translation is acceptable the user is asked for input. Returns <code>null</code> if user
   * aborts.
   * 
   * @param newReport The report to be merged
   * @param translationList never <code>null</code>
   * @param savedTranslation may be <code>null</code>
   * @param layer
   * @return The best translation, <code>null</code> if user aborts
   */
  private Score<CoordinateID> askTranslation(Collection<Score<CoordinateID>> translationList,
      Score<CoordinateID> savedTranslation, int layer) {
    Score<CoordinateID> bestTranslation =
        decideTranslation(translationList, savedTranslation, layer);

    // sanity check: translation changed since last time?
    boolean isTranslationSame =
        savedTranslation != null && bestTranslation.getKey().equals(savedTranslation.getKey());
    boolean isTranslationChanged =
        savedTranslation != null && !bestTranslation.getKey().equals(savedTranslation.getKey());

    // sanity check: more than one good translation?
    Set<CoordinateID> distinct = new HashSet<CoordinateID>();
    distinct.add(bestTranslation.getKey());
    if (translationList != null) {
      for (Score<CoordinateID> t : translationList) {
        if (t.getScore() >= 0 && t.getScore() * 5 >= bestTranslation.getScore()) {
          // check if t is rated best for all DataMappings
          for (String type : t.getTypes()) {
            if (!bestTranslation.getTypes().contains(type)) {
              distinct.add(t.getKey());
            }
          }
        }
      }
    }
    if (savedTranslation != null) {
      // this leads to a user interaction if saved translation != best found translation
      distinct.add(savedTranslation.getKey());
    }

    if (bestTranslation.getScore() > 0 && !isTranslationChanged
        && (isTranslationSame || distinct.size() == 1)) {
      if (distinct.size() != 1 && isTranslationSame) {
        log.info("found " + distinct.size()
            + " translations, but best translation equals saved translation: okay.");
      }
      // all seems well, return
      return bestTranslation;
    } else {
      // ask user what to do

      // setup dialog
      String message =
          Resources.getFormatted("util.reportmerger.msg.method", filename, layer)
              + (isTranslationChanged ? Resources.get("util.reportmerger.msg.changed") : "");

      String bestMethod =
          Resources.getFormatted("util.reportmerger.msg.method.best", bestTranslation);
      String chooseMethod = Resources.get("util.reportmerger.msg.method.choose");
      String inputMethod = Resources.get("util.reportmerger.msg.method.input");
      String skipMethod = Resources.get("util.reportmerger.msg.method.skip");
      ArrayList<Object> choices = new ArrayList<Object>(4 + translationList.size());
      choices.add(bestMethod);
      // choices.add(chooseMethod);
      choices.add(inputMethod);
      choices.add(skipMethod);

      ArrayList<Score<CoordinateID>> sorted = new ArrayList<Score<CoordinateID>>(translationList);
      Collections.sort(sorted, Collections.reverseOrder());
      choices.addAll(sorted);

      // ask the question
      Object choice =
          ui.input(message, Resources.get("util.reportmerger.msg.method.title"), choices.toArray(),
              bestMethod);
      if (choice == null || choice.equals(skipMethod))
        return null;

      // select translation the user wanted
      if (choice.equals(bestMethod))
        // try anyway
        return bestTranslation;
      else if (choice.equals(chooseMethod)) {
        // choose among found translations, deprecated
        Collection<Score<CoordinateID>> tChoices =
            new ArrayList<Score<CoordinateID>>(translationList);
        if (savedTranslation != null) {
          tChoices.add(savedTranslation);
        }
        tChoices.add(new Score<CoordinateID>(CoordinateID.create(0, 0, layer), -1));

        Score<?> help =
            (Score<?>) ui.input(Resources.getFormatted(
                "util.reportmerger.msg.usertranslation.choose", filename, layer), Resources
                .get("util.reportmerger.msg.usertranslation.title"), tChoices.toArray(),
                bestTranslation);
        Score<CoordinateID> chosenTranslation = null;
        // workaround since we cannot cast Object to Score<CoordinateID>
        for (Score<CoordinateID> s : tChoices) {
          if (s.equals(help)) {
            chosenTranslation = s;
            break;
          }
        }
        if (chosenTranslation != null) {
          log.info("user choice: " + chosenTranslation);
          bestTranslation = chosenTranslation;
        } else {
          log.info("user abort");
          bestTranslation = inputMapping(layer);
        }
      } else if (choice.equals(inputMethod)) {
        bestTranslation = inputMapping(layer);
      } else if (choice instanceof Score<?>) {
        // workaround since we cannot cast Object to Score<CoordinateID>
        for (Score<CoordinateID> s : translationList) {
          if (s.equals(choice)) {
            bestTranslation = s;
            break;
          }
        }
      } else {
        log.error("Unexpected choice");
        return null;
      }

    }
    return bestTranslation;
  }

  private Score<CoordinateID> inputMapping(int layer) {
    Score<CoordinateID> resultTranslation = null;
    boolean badInput = false;
    boolean cancelled = true;
    do {
      badInput = false;
      String message =
          Resources.getFormatted("util.reportmerger.msg.usertranslation.x", filename, layer);
      Object xS =
          ui.input(message, Resources.get("util.reportmerger.msg.usertranslation.title"), null, "0");
      if (xS != null) {
        message =
            Resources.getFormatted("util.reportmerger.msg.usertranslation.y", filename, layer);
        Object yS =
            ui.input(message, Resources.get("util.reportmerger.msg.usertranslation.title"), null,
                "0");
        if (yS != null) {
          cancelled = false;
          try {
            CoordinateID trans =
                CoordinateID.create(Integer.parseInt((String) xS), Integer.parseInt((String) yS),
                    layer);
            resultTranslation = new Score<CoordinateID>(trans, 1, "user");
            log.debug("using user translation: " + resultTranslation.getKey() + " in layer "
                + layer);
          } catch (NumberFormatException e) {
            badInput = true;
          }
        }
      }
    } while (badInput && !cancelled);
    if (cancelled) {
      log.info("user input cancelled");
      return null;
    }
    return resultTranslation;
  }

  private BBoxes getBoundingBox(CoordinateID bestTranslation, CoordinateID bestAstralTranslation) {
    // get existing box of old data
    BBoxes outBoxes = new BBoxes();
    boolean problem = !getBoundingBox(globalData, outBoxes);
    // get existing box of new data
    BBoxes inBoxes = new BBoxes();
    problem |= !getBoundingBox(addedData, inBoxes);
    // get new box of combination of both
    BBoxes idBoxes = new BBoxes();
    problem |=
        !getTransformBox(globalData, addedData, bestTranslation, bestAstralTranslation, idBoxes);

    Set<Integer> layers = new HashSet<Integer>(outBoxes.getLayers());
    layers.addAll(inBoxes.getLayers());
    layers.addAll(idBoxes.getLayers());

    BBoxes resultBoxes = new BBoxes();
    for (int layer : layers) {
      Set<BBox> ask = new HashSet<BoxTransformer.BBox>();

      BBox outBox = outBoxes.getBox(layer);
      BBox inBox = inBoxes.getBox(layer);
      BBox idBox = idBoxes.getBox(layer);

      // set default: box that was already known
      if (outBox != null) {
        resultBoxes.setBox(layer, outBox);
      }

      // added report has its own box: choose if none was known before, ask if it changed
      if (inBox != null) {
        if (outBox != null
            && (inBox.maxx - inBox.minx != outBox.maxx - outBox.minx || inBox.maxy - inBox.miny != outBox.maxy
                - outBox.miny)) {
          log.warn("bounding box changed from " + outBox + " to " + inBox);
          ask.add(inBox);
        }
        resultBoxes.setBox(layer, inBox);
      }

      // boxes from combination
      BBox rBox = resultBoxes.getBox(layer);

      if (idBox != null && idBox.maxx != Integer.MIN_VALUE) {
        if (rBox == null) {
          log.info("gone round the world (westward); the world's new girth is " + (idBox.maxx + 1));
          ask.add(idBox);
          resultBoxes.setBoxX(layer, (idBox.maxx + 1) / 2 - (idBox.maxx), (idBox.maxx + 1) / 2);
        } else if (rBox.maxx - rBox.minx != idBox.maxx) {
          // boxes do not match
          log.warn("new xbox: " + rBox + " -> " + idBox);
          // old box might be a multiple of new box...
          if (rBox.maxx != Integer.MIN_VALUE && (idBox.maxx) % (rBox.maxx - rBox.minx) != 0) {
            log.warn("cannot continue");
            ask.add(idBox);
            if (!interactive)
              return null;
          } else {
            log.info("the world has shrunken (westward); the world's new girth is "
                + (idBox.maxx + 1));
            ask.add(rBox);
            resultBoxes.setBoxX(layer, (idBox.maxx + 1) / 2 - (idBox.maxx), (idBox.maxx + 1) / 2);
          }
        }
      }
      // same thing for y-box
      if (idBox != null && idBox.maxy != Integer.MIN_VALUE) {
        if (rBox == null) {
          log.info("gone round the world (southward); the world's new girth is " + (idBox.maxy + 1));
          ask.add(idBox);
          resultBoxes.setBoxY(layer, (idBox.maxy + 1) / 2 - (idBox.maxy), (idBox.maxy + 1) / 2);
        } else if (rBox.maxy - rBox.miny != idBox.maxy) {
          // boxes do not match
          log.warn("new ybox: " + rBox + " -> " + idBox);
          if (rBox.maxy != Integer.MIN_VALUE && (idBox.maxy) % (rBox.maxy - rBox.miny) != 0) {
            log.warn("cannot continue");
            ask.add(idBox);
            if (!interactive)
              return null;
          } else {
            log.info("the world has shrunken (southward); the world's new girth is "
                + (idBox.maxy + 1));
            ask.add(rBox);
            resultBoxes.setBoxY(layer, (idBox.maxy + 1) / 2 - (idBox.maxy), (idBox.maxy + 1) / 2);
          }
        }
      }

      // ask user and return result
      if (interactive && (problem || !ask.isEmpty())) {
        rBox = askBox(layer, ask, resultBoxes.getBox(layer));
        if (rBox != null) {
          resultBoxes.setBox(layer, rBox);
        } else
          return null;
      }

    }
    return resultBoxes;
  }

  /**
   * Ask the user about boxes. Use the argument as proposal.
   * 
   * @param best
   * @param ask
   * @return user choice or <code>null</code> for no choice
   */
  private BBox askBox(int layer, Set<BBox> boxes, BBox best) {
    String message = Resources.getFormatted("util.reportmerger.msg.box", filename, layer);
    if (boxes.size() == 0) {
      Resources.getFormatted("util.reportmerger.msg.box.null", filename, layer);
    }

    String inputMethod = Resources.get("util.reportmerger.msg.box.input");
    String skipMethod = Resources.get("util.reportmerger.msg.box.skip");
    ArrayList<Object> choices = new ArrayList<Object>(3);
    if (best != null) {
      choices.add(Resources.getFormatted("util.reportmerger.msg.box.found", best));
    }
    for (BBox box : boxes) {
      choices.add(Resources.getFormatted("util.reportmerger.msg.box.found", box));
    }

    choices.add(inputMethod);
    choices.add(skipMethod);

    Object choice =
        ui.input(message, Resources.get("util.reportmerger.msg.box.title"), choices.toArray(),
            choices.get(0));
    if (choice == null || choice.equals(skipMethod))
      return null;

    if (choice.equals(inputMethod)) {
      SetGirthDialog girthDialog;
      ui.showDialog(girthDialog = new SetGirthDialog(null, best, layer));
      if (girthDialog.approved())
        return girthDialog.getNewBorders().getBox(layer);
      else
        return null;
    } else if (choice.equals(skipMethod))
      return null;
    else {
      for (int i = 0; i < choices.size(); i++)
        if (choices.get(i).equals(choice)) {
          if (i == 0 && best != null)
            return best;
          int j = best == null ? 0 : 1;
          for (BBox box : boxes)
            if (j++ == i)
              return box;
        }
    }
    log.error("Unexpected choice");
    return null;
  }

  /**
   * Compare coordinates of translated regions from new report to regions from global data and
   * deduce boxes from the difference.
   * 
   * @param boxes The result boxes are stored in this parameter.
   * @return <code>false</code> if ambiguous boxes where found
   */
  public static boolean getTransformBox(GameData globalData, GameData addedData,
      CoordinateID bestTranslation, CoordinateID bestAstralTranslation, BBoxes boxes) {

    boolean result = true;

    Map<Long, Region> idMap = new HashMap<Long, Region>(addedData.getRegions().size() * 9 / 6);
    for (Region r : globalData.getRegions()) {
      if (r.hasUID() && r.getUID() >= 0) {
        idMap.put(r.getUID(), r);
      }
    }

    if (idMap.size() > 0) {
      for (Region rNew : addedData.getRegions()) {
        if (rNew.hasUID()) {
          Region rOld = idMap.get(rNew.getUID());
          if (rOld != null) {
            int layer = rNew.getCoordinate().getZ();
            CoordinateID translation =
                rNew.getCoordinate().inverseTranslateInLayer(rOld.getCoordinate());
            if (layer == bestTranslation.getZ()) {
              translation = translation.inverseTranslateInLayer(bestTranslation);
            } else if (layer == bestAstralTranslation.getZ()) {
              translation = translation.inverseTranslateInLayer(bestAstralTranslation);
            }
            translation =
                CoordinateID.create(Math.abs(translation.getX()), Math.abs(translation.getY()),
                    layer);
            if (translation.getX() != 0 || translation.getY() != 0) {
              log.debug("translation: " + translation);

              if ((translation.getX() != 0 && translation.getX() <= 2)
                  || (translation.getY() != 0 && translation.getY() <= 2)) {
                log.warn("dubious translation: " + rNew + " --> " + rOld);
              } else {
                if (translation.getX() != 0) {
                  if (boxes.getBox(layer) != null) {
                    if (boxes.getBox(layer).maxx != Integer.MAX_VALUE
                        && boxes.getBox(layer).maxx != translation.getX() - 1) {
                      log.warn("box mismatch: " + (translation.getX() - 1) + " vs. "
                          + boxes.getBox(layer).maxx);
                      result = false;
                    }
                  } else {
                    log.finest("translation: " + translation);
                  }

                  boxes.setBoxX(layer, 0, translation.getX() - 1);
                }
                if (translation.getY() != 0) {
                  if (boxes.getBox(layer) != null) {
                    if (boxes.getBox(layer).maxy != Integer.MAX_VALUE
                        && boxes.getBox(layer).maxy != translation.getY() - 1) {
                      log.warn("box mismatch: " + (translation.getY() - 1) + " vs. "
                          + boxes.getBox(layer).maxy);
                      result = false;
                    }
                  } else {
                    log.finest("translation: " + translation);
                  }
                  boxes.setBoxY(layer, 0, translation.getY() - 1);
                }
              }
            }
          }
        }
      }
    }
    return result;
  }

  /**
   * Try to find a translation based on region ids. Compares positions of wrappers in data with
   * their original region and deduces box diameter from their difference.
   * 
   * @param boxes The result boxes are stored in this parameter.
   * @return <code>false</code> if ambiguous boxes where found
   */
  public static boolean getBoundingBox(GameData data, BBoxes boxes) {
    boolean result = true;
    for (Region w : data.wrappers().values()) {
      Region original = data.getOriginal(w);
      if (original != null) {
        if (w.getCoordinate().getZ() != original.getCoordinate().getZ())
          throw new IllegalArgumentException("Report wraps between different layers not supported.");

        if (original.getCoordY() == w.getCoordY()) {
          if (original.getCoordX() - w.getCoordX() > 0) {
            result &=
                changeBoxX(boxes, w.getCoordinate().getZ(), w.getCoordX() + 1, original.getCoordX());
          }
          if (original.getCoordX() - w.getCoordX() < 0) {
            result &=
                changeBoxX(boxes, w.getCoordinate().getZ(), original.getCoordX(), w.getCoordX() - 1);
          }
        } else {
          if (original.getCoordY() - w.getCoordY() > 0) {
            result &=
                changeBoxY(boxes, w.getCoordinate().getZ(), w.getCoordY() + 1, original.getCoordY());
          }
          if (original.getCoordY() - w.getCoordY() < 0) {
            result &=
                changeBoxY(boxes, w.getCoordinate().getZ(), original.getCoordY(), w.getCoordY() - 1);
          }
        }
      }
    }
    return result;
  }

  /**
   * @return <code>false</code> if the old box value was not infinite and did not match
   */
  private static boolean changeBoxX(BBoxes boxes, int layer, int newmin, int newmax) {
    BBox oldBox = boxes.getBox(layer);
    boolean result = true;
    if (oldBox != null
        && ((oldBox.minx != Integer.MAX_VALUE && oldBox.minx != newmin) || (oldBox.maxx != Integer.MAX_VALUE && oldBox.maxx != newmax))) {
      log.warn("box changed: " + oldBox + "->" + newmin + "/" + newmax);
      if (oldBox.maxx - oldBox.minx != newmax - newmin) {
        result = false;
      }
    }
    boxes.setBoxX(layer, newmin, newmax);
    return result;
  }

  /**
   * @return <code>false</code> if the old box value was not infinite and did not match
   */
  private static boolean changeBoxY(BBoxes boxes, int layer, int newmin, int newmax) {
    boolean result = true;
    BBox oldBox = boxes.getBox(layer);
    if (oldBox != null
        && ((oldBox.miny != Integer.MAX_VALUE && oldBox.miny != newmin) || (oldBox.maxy != Integer.MAX_VALUE && oldBox.maxy != newmax))) {
      log.warn("box changed: " + oldBox + "->" + newmin + "/" + newmax);
      if (oldBox.maxy - oldBox.miny != newmax - newmin) {
        result = false;
      }
    }
    boxes.setBoxY(layer, newmin, newmax);
    return result;
  }

  protected ReportTransformer getTransformer(GameData data, BBoxes boxes) {
    BoxTransformer transformer = new BoxTransformer(boxes);
    return transformer;
  }

  protected ReportTransformer getTransformer(BBoxes resultBoxes, CoordinateID bestTranslation,
      CoordinateID bestAstralTranslation) {
    Map<Long, Region> idMap = new HashMap<Long, Region>(addedData.getRegions().size() * 9 / 6);
    for (Region r : globalData.getRegions()) {
      if (r.hasUID() && r.getUID() >= 0) {
        idMap.put(r.getUID(), r);
      }
    }

    TwoLevelTransformer transformer2L =
        new TwoLevelTransformer(bestTranslation, bestAstralTranslation);

    try {
      MapTransformer transformerUID = new MapTransformer(transformer2L);
      transformerUID.setBoxes(resultBoxes);

      if (idMap.size() > 0) {
        for (Region rNew : addedData.getRegions()) {
          if (rNew.hasUID()) {
            Region rOld = idMap.get(rNew.getUID());
            if (rOld != null) {
              transformerUID.addMapping(rNew.getID(), resultBoxes.putInBox(rOld.getCoordinate()));
            }
          }
        }
      }
      return transformerUID;
    } catch (Exception e) {
      log.error("could not compute translation", e);
      return transformer2L;
    }
  }

}