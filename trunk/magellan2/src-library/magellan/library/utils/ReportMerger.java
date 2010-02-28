/*
 *  Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe,
 *                          Stefan Goetz, Sebastian Pappert,
 *                          Klaas Prause, Enno Rehling,
 *                          Sebastian Tusk, Ulrich Kuester,
 *                          Ilja Pavkovic
 *
 * This file is part of the Eressea Java Code Base, see the
 * file LICENSING for the licensing information applying to
 * this file.
 *
 */

package magellan.library.utils;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import magellan.library.CoordinateID;
import magellan.library.EntityID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.GameDataMerger;
import magellan.library.Region;
import magellan.library.gamebinding.MapMergeEvaluator;
import magellan.library.rules.Date;
import magellan.library.utils.logging.Logger;
import magellan.library.utils.mapping.SavedTranslationsMapping;
import magellan.library.utils.transformation.BoxTransformer;
import magellan.library.utils.transformation.MapTransformer;
import magellan.library.utils.transformation.ReportTransformer;
import magellan.library.utils.transformation.TwoLevelTransformer;
import magellan.library.utils.transformation.MapTransformer.BBox;
import magellan.library.utils.transformation.MapTransformer.BBoxes;

/**
 * Helper class.
 */
public class ReportMerger extends Object {
  static final Logger log = Logger.getInstance(ReportMerger.class);

  /**
   * An interface representing classes that read a report from a file.
   */
  public interface Loader {
    /**
     * Read a report from file.
     */
    public GameData load(File file);
  }

  /**
   * An interface representing classes that receive a GameData object (possibly assigning it to the
   * Client).
   */
  public interface AssignData {
    /**
     * Do something with the given report
     */
    public void assign(GameData data);
  }

  /**
   * Manages a report and provides information relevant to ReportMerger.
   * 
   * @author stm
   * @version 1.0, Dec 17, 2007
   */
  private class ReportCache implements Comparable<ReportCache> {
    // data set
    private SoftReference<GameData> dataReference = null;

    // you can either set game data statically...
    private GameData staticData = null;

    // ...or load data from a file
    private File file = null;

    private boolean hasAstralRegions = false;

    // already merged with another report
    private boolean merged = false;

    private Date date = null;

    private boolean initialized = false;

    /**
     * Creates a new ReportCache which will read the report from <code>file</code> as needed using
     * this ReportMergers <code>Loader</code>.
     * 
     * @param file
     */
    public ReportCache(File file) {
      staticData = null;
      this.file = file;
    }

    /**
     * Creates a new ReportCache which uses the report <code>data</code>.
     * 
     * @param data
     */
    public ReportCache(GameData data) {
      staticData = data;
    }

    /**
     * @return The report
     */
    public GameData getData() {
      if (staticData != null)
        return staticData;

      GameData data = null;
      if (dataReference == null) {
        data = loadData();
      } else {
        data = dataReference.get();
        if (data == null) {
          data = loadData();
        }
      }

      return data;
    }

    /**
     * Load report via ReportMerger's <code>loader</code>.
     * 
     * @return The report
     */
    private GameData loadData() {
      GameData data = loader.load(file);
      dataReference = new SoftReference<GameData>(data);
      return data;
    }

    /**
     * Perform initializations if not already done.
     */
    private void init() {
      if (!initialized) {
        initRegionMaps();
      }
    }

    /**
     * Sets up regionMap and schemeMap und regionUIDMap.
     */
    private void initRegionMaps() {
      GameData data = getData();

      for (Region region : data.getRegions()) {
        if (region.getCoordinate().getZ() == 1) {
          hasAstralRegions = true;
        }
      }
    }

    /**
     * Returns <code>true</code> if the report has at least one astral region.
     */
    public boolean hasAstralRegions() {
      init();
      return hasAstralRegions;
    }

    /**
     * Ensures partial order by increasing date.
     * 
     * @return <code>1</code> iff this report's round is greater than <code>otherReport</code>'s
     *         round.
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(ReportCache otherReport) {
      if (otherReport == null)
        return 1;
      else
        return otherReport.getRound() > getRound() ? -1 : (otherReport.getRound() < getRound() ? 1
            : 0);
    }

    /**
     * Returns the data's round (== date).
     */
    private int getRound() {
      if (date == null) {
        date = getData().getDate();
      }
      if (date == null)
        return -1;
      else
        return date.getDate();
    }

    /**
     * Frees memory.
     */
    public void release() {
      staticData = null;
      if (dataReference != null) {
        dataReference.clear();
      }
      initialized = false;
      date = null;
    }

    public boolean isMerged() {
      return merged;
    }

    public void setMerged(boolean b) {
      merged = b;
    }

    public File getFile() {
      return file;
    }
  }

  // merged data set
  protected GameData globalData = null;

  protected ReportCache dataReport = null;

  // reports to merge
  protected ReportCache reports[] = null;

  // loader interface
  protected Loader loader = null;

  // data assign interface
  protected AssignData assignData = null;

  protected UserInterface ui;
  protected int iProgress;

  private boolean sort = false;

  private boolean interactive = false;

  /**
   * Creates new ReportMerger
   */
  public ReportMerger(GameData _data, File files[], Loader _loader, AssignData _assignData) {
    globalData = _data;
    // globalData.removeTheVoid(); // removes void regions; STM: bad idea to change the report
    // here...
    reports = new ReportCache[files.length];

    for (int i = 0; i < files.length; i++) {
      reports[i] = new ReportCache(files[i]);
    }

    loader = _loader;
    assignData = _assignData;
  }

  /**
   * Creates a new ReportMerger object.
   */
  public ReportMerger(GameData _data, File file, Loader _loader, AssignData _assignData) {
    this(_data, new File[] { file }, _loader, _assignData);
  }

  /**
   * 
   */
  public GameData merge() {
    return merge(new NullUserInterface(), true, false, false);
  }

  /**
   * Start merging
   * 
   * @param aUI A user interface for indicating progress, displaying messages etc.
   * @param async If <code>true</code> the merging will be started in a new thread.
   * @param sort
   * @param interactive
   */
  public GameData merge(UserInterface aUI, boolean sort, boolean interactive, boolean async) {
    ui = aUI;
    this.sort = sort;
    this.interactive = interactive;

    if (ui != null) {
      ui.setMaximum(reports.length * 4); // four steps per report
    }
    ui.show();
    if (async) {
      new Thread(new Runnable() {
        public void run() {
          ReportMerger.this.mergeThread();
        }
      }).start();
      return null;
    } else
      return mergeThread();
  }

  /**
   * Do the merging. This method can be executed in its own thread.
   */
  private synchronized GameData mergeThread() {
    if (ui != null) {
      ui.setTitle(Resources.get("util.reportmerger.window.title"));
      ui.setProgress(Resources.get("util.reportmerger.status.merge"), 0);
    }

    if (sort) {
      sortReports();
    }

    boolean error = false;
    /**
     * We merge reports one by one. If merging of a report fails, we try to merge all other reports.
     * We only break, if all reports, subsequently, have failed to merge.
     */
    try {
      int iFailedConnectivity = tryAllReports();

      // prepare failure message
      if (iFailedConnectivity > 0) {
        if (askToMergeAnyway()) {
          for (int i = 0; i < reports.length; i++) {
            if (!reports[i].isMerged()) {
              iProgress += 2;
              ui.setProgress(reports[i].getFile().getName() + " - "
                  + Resources.get("util.reportmerger.status.merging"), iProgress);

              globalData = GameDataMerger.merge(globalData, reports[i].getData());

              reports[i].setMerged(true);
              reports[i].release();
            }
          }
        }
      }
    } catch (Exception e) {
      ReportMerger.log.error(e);
      ui.showException("Exception while merging report", null, e);
      error = true;
    }

    // inform user about memory problems
    if (globalData.outOfMemory) {
      ui.showDialog((new JOptionPane(Resources.get("client.msg.outofmemory.text"),
          JOptionPane.WARNING_MESSAGE, JOptionPane.DEFAULT_OPTION)).createDialog(Resources
          .get("client.msg.outofmemory.title")));
      ReportMerger.log.error(Resources.get("client.msg.outofmemory.text"));
      error = true;
    }
    if (!MemoryManagment.isFreeMemory(globalData.estimateSize())) {
      ui.showDialog((new JOptionPane(Resources.get("client.msg.lowmem.text"),
          JOptionPane.WARNING_MESSAGE, JOptionPane.DEFAULT_OPTION)).createDialog(Resources
          .get("client.msg.lowmem.title")));
      log.warn(Resources.get("client.msg.lowmem.text"));
    }

    if (ui != null) {
      ui.ready();
    }

    if (!error && assignData != null) {
      assignData.assign(globalData);
    }

    return globalData;
  }

  private boolean askToMergeAnyway() {
    StringBuilder strMessage =
        new StringBuilder(Resources.get("util.reportmerger.msg.noconnection.text.1")).append(" ");

    for (int i = 0; i < reports.length; i++) {
      if (!reports[i].isMerged()) {
        strMessage.append(reports[i].getFile().getName());

        if (i < reports.length - 1) {
          strMessage.append(", ");
        }
      }
    }
    strMessage.append(Resources.get("util.reportmerger.msg.noconnection.text.2"));

    // ask to merge anyway
    return ui != null
        && ui.confirm(strMessage.toString(), Resources
            .get("util.reportmerger.msg.confirmmerge.title"));
  }

  /**
   * Try to merge the reports one by one and return number of failed reports.
   */
  private int tryAllReports() {
    int iFailedConnectivity = 0;
    int currentReport = 0;
    int iMerged = 0;

    while (true) {
      if (!reports[currentReport].isMerged()) {
        if (!mergeReport(reports[currentReport])) {
          iFailedConnectivity++;
        } else {
          // a report has been successfully merged
          // previously failed reports could now have a connection
          iFailedConnectivity = 0;
          iMerged++;
        }
      }

      if ((iMerged + iFailedConnectivity) == reports.length) {
        // all reports have either been merged or subsequently tried to be merged without success
        break;
      }

      currentReport++;

      // cycle through reports
      if (currentReport >= reports.length) {
        currentReport = 0;
        iFailedConnectivity = 0;
        iMerged = 0;
        for (ReportCache r : reports) {
          if (r.isMerged()) {
            iMerged++;
          }
        }
      }
    }
    return iFailedConnectivity;
  }

  /**
   * Sort the reports according to their Comparator which currently means, according to their age.
   */
  private void sortReports() {
    Arrays.sort(reports);
  }

  /**
   * Merges a report to the current report. <code>newReport</code>.merged is set to true if it has
   * been successfully merged or if it can never be merged successfully.
   * 
   * @param newReport
   * @return true iff reports were merged or report data null or report types don't match
   */
  private boolean mergeReport(ReportCache newReport) {
    dataReport = new ReportCache(globalData);

    iProgress += 1;
    if (ui != null) {
      ui.setProgress(newReport.getFile().getName() + " - "
          + Resources.get("util.reportmerger.status.processing"), iProgress);
    }

    // return if game type is wrong
    if (!checkGameType(newReport))
      return true;

    // fixed (stm) moved to GameDataMerger
    // adjustTrustlevels(newReport);

    // fixed (stm) moved to GameDataMerger
    // setTempID(newReport);

    /***************************************************************************
     * Look for coordinate translation.
     * <p>
     * Important: A faction's coordinate system for astral space is independent of it's coordinate
     * system for normal space (in Eressea). It depends (as far as I know) on the astral space
     * region where the faction first enters the astral space (this region will have the coordinate
     * (0,0,1). Thus a special translation for the astral space (beside that one for normal space)
     * has to be found.
     * </p>
     */
    iProgress += 1;
    if (ui != null) {
      ui.setProgress(newReport.getFile().getName() + " - "
          + Resources.get("util.reportmerger.status.connecting"), iProgress);
    }

    // decide, which translation to choose
    Score<CoordinateID> bestTranslation = findRealSpaceTranslation(newReport);

    if (bestTranslation != null) {
      Score<CoordinateID> bestAstralTranslation =
          findAstralRSpaceTranslation(newReport, bestTranslation);

      if (bestAstralTranslation != null) {
        iProgress += 1;
        if (ui != null) {
          ui.setProgress(newReport.getFile().getName() + " - "
              + Resources.get("util.reportmerger.status.translating"), iProgress);
        }

        // TODO (stm) do this /after/ merging?
        storeTranslations(newReport, bestTranslation, bestAstralTranslation);

        BBoxes boxes =
            getBoundingBox(globalData, newReport.getData(), bestTranslation.getKey(),
                bestAstralTranslation.getKey());

        ReportTransformer transformer1 = getTransformer(globalData, boxes);
        ReportTransformer transformer2 =
            getTransformer(dataReport.getData(), newReport.getData(), boxes, bestTranslation
                .getKey(), bestAstralTranslation.getKey());

        // GameData clonedData = translateReport(newReport, bestTranslation, bestAstralTranslation);

        /***************************************************************************
         * Merge the reports, finally!
         */
        iProgress += 1;
        if (ui != null) {
          ui.setProgress(newReport.getFile().getName() + " - "
              + Resources.get("util.reportmerger.status.merging"), iProgress);
        }
        // old: globalData = GameDataMerger.merge(globalData, clonedData);
        globalData =
            GameDataMerger.merge(globalData, newReport.getData(), transformer1, transformer2);

        // try {
        // (new CRWriter(globalData, null, FileTypeFactory.singleton().createFileType(
        // new File("dummydummy1.cr"), false), globalData.getEncoding())).writeSynchronously();
        // (new CRWriter(newData, null, FileTypeFactory.singleton().createFileType(
        // new File("dummydummy2.cr"), false), newData.getEncoding())).writeSynchronously();
        // } catch (IOException e) {
        // e.printStackTrace();
        // }

        newReport.setMerged(true);
      } else {
        ReportMerger.log.info("aborting...");
        iProgress -= 1;
        if (ui != null) {
          ui.setProgress(newReport.getFile().getName(), iProgress);
        }
      }
    } else {
      ReportMerger.log.info("aborting...");
      iProgress -= 1;
      if (ui != null) {
        ui.setProgress(newReport.getFile().getName(), iProgress);
      }
    }

    newReport.release();
    return newReport.isMerged();
  }

  private BBoxes getBoundingBox(GameData oldData, GameData newData, CoordinateID bestTranslation,
      CoordinateID bestAstralTranslation) {
    // get existing box of old data
    BBoxes outBoxes = getBoundingBox(oldData);
    // get existing box of new data
    BBoxes inBoxes = getBoundingBox(newData);
    // get new box of combination of both
    BBoxes idBoxes = getTransformBox(oldData, newData, bestTranslation, bestAstralTranslation);

    BBoxes resultBoxes = new BBoxes();
    for (int layer : outBoxes.getLayers()) {
      BBox outBox = outBoxes.getBox(layer);
      BBox inBox = inBoxes.getBox(layer);
      if (inBox != null
          && (inBox.maxx - inBox.minx != outBox.maxx - outBox.minx || inBox.maxy - inBox.miny != outBox.maxy
              - outBox.miny)) {
        log.warn("bounding box changed from " + outBox + " to " + inBox);
        resultBoxes.setBox(layer, inBox);
      } else {
        resultBoxes.setBox(layer, outBox);
      }
    }
    for (int layer : inBoxes.getLayers()) {
      BBox outBox = outBoxes.getBox(layer);
      if (outBox == null) {
        resultBoxes.setBox(layer, inBoxes.getBox(layer));
      }
    }

    // check if a new box has been found
    for (int layer : idBoxes.getLayers()) {
      BBox idBox = idBoxes.getBox(layer);
      BBox rBox = resultBoxes.getBox(layer);
      if (idBox.maxx != Integer.MIN_VALUE) {
        if (rBox == null) {
          log.info("gone round the world (westward); the world's new girth is " + (idBox.maxx + 1));
          resultBoxes.setBoxX(layer, (idBox.maxx + 1) / 2 - (idBox.maxx), (idBox.maxx + 1) / 2);
        } else if (rBox.maxx - rBox.minx != idBox.maxx) {
          log.warn("new xbox: " + rBox + " -> " + idBox);
          // old box might be a multiple of new box...
          if (rBox.maxx != Integer.MIN_VALUE && idBox.maxx + 1 % rBox.maxx - rBox.minx != 0) {
            log.warn("cannot continue");
            return null;
          } else {
            log.info("the world has shrunken (westward); the world's new girth is "
                + (idBox.maxx + 1));
            resultBoxes.setBoxX(layer, (idBox.maxx + 1) / 2 - (idBox.maxx), (idBox.maxx + 1) / 2);
          }
        }
      }
      if (idBox.maxy != Integer.MIN_VALUE) {
        if (rBox == null) {
          log
              .info("gone round the world (southward); the world's new girth is "
                  + (idBox.maxy + 1));
          resultBoxes.setBoxY(layer, (idBox.maxy + 1) / 2 - (idBox.maxy), (idBox.maxy + 1) / 2);
        } else if (rBox.maxy - rBox.miny != idBox.maxy) {
          log.warn("new ybox: " + rBox + " -> " + idBox);
          if (rBox.maxy != Integer.MIN_VALUE && idBox.maxy % (rBox.maxy - rBox.miny) != 0) {
            log.warn("cannot continue");
            return null;
          } else {
            log.info("the world has shrunken (southward); the world's new girth is "
                + (idBox.maxy + 1));
            resultBoxes.setBoxY(layer, (idBox.maxy + 1) / 2 - (idBox.maxy), (idBox.maxy + 1) / 2);
          }
        }
      }
    }
    return resultBoxes;
  }

  private BBoxes getTransformBox(GameData oldData, GameData newData, CoordinateID bestTranslation,
      CoordinateID bestAstralTranslation) {

    BBoxes boxes = new BBoxes();

    Map<Long, Region> idMap = new HashMap<Long, Region>(newData.getRegions().size() * 9 / 6);
    for (Region r : oldData.getRegions()) {
      if (r.hasUID()) {
        idMap.put(r.getUID(), r);
      }
    }

    if (idMap.size() > 0) {
      for (Region rNew : newData.getRegions()) {
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
              log.finest("translation: " + translation);
            }
            if ((translation.getX() != 0 && translation.getX() <= 2)
                || (translation.getY() != 0 && translation.getY() <= 2)) {
              log.warn("unclear translation: " + rNew + " --> " + rOld);
            } else {
              if (translation.getX() != 0) {
                if (boxes.getBox(layer) != null) {
                  if (boxes.getBox(layer).maxx != Integer.MAX_VALUE
                      && boxes.getBox(layer).maxx != translation.getX() - 1) {
                    log.warn("box mismatch: " + (translation.getX() - 1) + " vs. "
                        + boxes.getBox(layer).maxx);
                  }
                }
                boxes.setBoxX(layer, 0, translation.getX() - 1);
              }
              if (translation.getY() != 0) {
                if (boxes.getBox(layer) != null) {
                  if (boxes.getBox(layer).maxy != Integer.MAX_VALUE
                      && boxes.getBox(layer).maxy != translation.getY() - 1) {
                    log.warn("box mismatch: " + (translation.getY() - 1) + " vs. "
                        + boxes.getBox(layer).maxy);
                  }
                }
                boxes.setBoxY(layer, 0, translation.getY() - 1);
              }
            }
          }
        }
      }
    }
    return boxes;
  }

  /**
   * Try to find a translation based on region ids.
   */
  protected static MapTransformer.BBoxes getBoundingBox(GameData data) {

    BBoxes boxes = new BBoxes();
    for (Region w : data.wrappers().values()) {
      Region original = data.getOriginal(w);
      if (original != null) {
        if (w.getCoordinate().getZ() != original.getCoordinate().getZ())
          throw new IllegalArgumentException("Report wraps between different layers not supported.");

        if (original.getCoordX() - w.getCoordX() > 0) {
          changeBoxX(boxes, w.getCoordinate().getZ(), w.getCoordX() + 1, original.getCoordX());
        }
        if (original.getCoordX() - w.getCoordX() < 0) {
          changeBoxX(boxes, w.getCoordinate().getZ(), original.getCoordX(), w.getCoordX() - 1);
        }
        if (original.getCoordY() - w.getCoordY() > 0) {
          changeBoxY(boxes, w.getCoordinate().getZ(), w.getCoordY() + 1, original.getCoordY());
        }
        if (original.getCoordY() - w.getCoordY() < 0) {
          changeBoxY(boxes, w.getCoordinate().getZ(), original.getCoordY(), w.getCoordY() - 1);
        }
      }
    }
    return boxes;
  }

  private static void changeBoxX(BBoxes boxes, int layer, int newmin, int newmax) {
    BBox oldBox = boxes.getBox(layer);
    if (oldBox != null
        && ((oldBox.minx != Integer.MAX_VALUE && oldBox.minx != newmin) || (oldBox.maxx != Integer.MAX_VALUE && oldBox.maxx != newmax))) {
      ReportMerger.log.warn("box changed");
    }
    boxes.setBoxX(layer, newmin, newmax);
  }

  private static void changeBoxY(BBoxes boxes, int layer, int newmin, int newmax) {
    BBox oldBox = boxes.getBox(layer);
    if (oldBox != null
        && ((oldBox.miny != Integer.MAX_VALUE && oldBox.miny != newmin) || (oldBox.maxy != Integer.MAX_VALUE && oldBox.maxy != newmax))) {
      ReportMerger.log.warn("box changed");
    }
    boxes.setBoxY(layer, newmin, newmax);
  }

  protected ReportTransformer getTransformer(GameData data, BBoxes boxes) {
    BoxTransformer transformer = new BoxTransformer(boxes);
    return transformer;
  }

  protected ReportTransformer getTransformer(GameData oldData, GameData newData, BBoxes outBoxes,
      CoordinateID bestTranslation, CoordinateID bestAstralTranslation) {
    Map<Long, Region> idMap = new HashMap<Long, Region>(newData.getRegions().size() * 9 / 6);
    for (Region r : oldData.getRegions()) {
      if (r.hasUID()) {
        idMap.put(r.getUID(), r);
      }
    }

    TwoLevelTransformer transformer2L =
        new TwoLevelTransformer(bestTranslation, bestAstralTranslation);

    try {
      MapTransformer transformerUID = new MapTransformer(transformer2L);
      transformerUID.setBoxes(outBoxes);

      if (idMap.size() > 0) {
        for (Region rNew : newData.getRegions()) {
          if (rNew.hasUID()) {
            Region rOld = idMap.get(rNew.getUID());
            if (rOld != null) {
              transformerUID.addMapping(rNew.getID(), outBoxes.putInBox(rOld.getCoordinate()));
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

  /***************************************************************************
   * Translate the report in two layers.
   */
  protected GameData translateReport(ReportCache newReport, Score<CoordinateID> bestTranslation,
      Score<CoordinateID> bestAstralTranslation) {
    GameData clonedData = newReport.getData();
    if (bestTranslation.getKey().getX() != 0 || bestTranslation.getKey().getY() != 0
        || bestAstralTranslation.getKey().getX() != 0 || bestAstralTranslation.getKey().getY() != 0) {
      try {
        clonedData =
            clonedData.clone(new TwoLevelTransformer(bestTranslation.getKey(),
                bestAstralTranslation.getKey()));
        if (clonedData == null)
          throw new RuntimeException("problems during cloning");
        if (clonedData.outOfMemory) {
          ui.showDialog((new JOptionPane(Resources.get("client.msg.outofmemory.text"),
              JOptionPane.WARNING_MESSAGE, JOptionPane.DEFAULT_OPTION)).createDialog(Resources
              .get("client.msg.outofmemory.title")));
          // ui.confirm(Resources.get("client.msg.outofmemory.text"), Resources
          // .get("client.msg.outofmemory.title"));
          ReportMerger.log.error(Resources.get("client.msg.outofmemory.text"));
        }
      } catch (CloneNotSupportedException e) {
        ReportMerger.log.error(e);
        throw new RuntimeException("problems while cloning", e);
      }
      if (!MemoryManagment.isFreeMemory(clonedData.estimateSize())) {
        ui.showDialog((new JOptionPane(Resources.get("client.msg.lowmem.text"),
            JOptionPane.WARNING_MESSAGE, JOptionPane.DEFAULT_OPTION)).createDialog(Resources
            .get("client.msg.lowmem.title")));
        log.warn(Resources.get("client.msg.lowmem.text"));
        // ui.confirm(Resources.get("client.msg.lowmem.text"), Resources
        // .get("client.msg.lowmem.title"));
      }
    } else {
      ReportMerger.log.info("Using untranslated new report - same origin");
    }
    return clonedData;
  }

  /**
   * Find best translation in real space
   */
  private Score<CoordinateID> findRealSpaceTranslation(ReportCache newReport) {
    MapMergeEvaluator mhelp = dataReport.getData().getGameSpecificStuff().getMapMergeEvaluator();
    Collection<Score<CoordinateID>> translationList =
        mhelp.getDataMappings(dataReport.getData(), newReport.getData(), 0);

    // try to find a translation for the new report's owner faction in the
    // global data's owner faction
    Score<CoordinateID> savedTranslation = findSavedMapping(dataReport, newReport, 0);

    // decide, which translation to choose
    Score<CoordinateID> bestTranslation = null;

    if (interactive) {
      bestTranslation = askTranslation(newReport, translationList, savedTranslation, 0);
    } else {
      bestTranslation = decideTranslation(newReport, translationList, savedTranslation, 0);
    }
    return bestTranslation;
  }

  /**
   * find best astral translation
   */
  private Score<CoordinateID> findAstralRSpaceTranslation(ReportCache newReport,
      Score<CoordinateID> bestTranslation) {
    MapMergeEvaluator mhelp = dataReport.getData().getGameSpecificStuff().getMapMergeEvaluator();
    Score<CoordinateID> bestAstralTranslation = null;
    if (!newReport.hasAstralRegions()) {
      bestAstralTranslation = new Score<CoordinateID>(CoordinateID.create(0, 0, 1), -1);
    } else if (!dataReport.hasAstralRegions()) {
      Collection<Score<CoordinateID>> empty = Collections.emptyList();
      if (interactive) {
        bestAstralTranslation = askTranslation(newReport, empty, null, 1);
      } else {
        bestAstralTranslation = decideTranslation(newReport, empty, null, 1);
      }
    } else {

      Collection<CoordinateID> otherLevels = new ArrayList<CoordinateID>(1);
      otherLevels.add(bestTranslation.getKey());
      Collection<Score<CoordinateID>> astralTranslationList =
          mhelp.getDataMappings(dataReport.getData(), newReport.getData(), 1, otherLevels);

      Score<CoordinateID> savedAstralTranslation = findSavedMapping(dataReport, newReport, 1);

      if (interactive) {
        bestAstralTranslation =
            askTranslation(newReport, astralTranslationList, savedAstralTranslation, 1);
      } else {
        bestAstralTranslation =
            decideTranslation(newReport, astralTranslationList, savedAstralTranslation, 1);
      }
    }
    return bestAstralTranslation;
  }

  /***************************************************************************
   * store translations
   */
  private void storeTranslations(ReportCache newReport, Score<CoordinateID> bestTranslation,
      Score<CoordinateID> bestAstralTranslation) {
    ReportMerger.log.info("Using this (real) translation: " + bestTranslation.getKey());
    // store found translations
    EntityID newReportOwner = newReport.getData().getOwnerFaction();
    if (newReportOwner != null) {
      if (globalData.getCoordinateTranslation(newReportOwner, 0) != null
          && !globalData.getCoordinateTranslation(newReportOwner, 0).equals(
              bestTranslation.getKey())) {
        ReportMerger.log.warn("old translation "
            + globalData.getCoordinateTranslation(newReportOwner, 0)
            + " inconsistent with new translation " + bestTranslation.getKey());
      }
      if (bestTranslation.getScore() >= 0) {
        CoordinateID correct = newReport.getData().getCoordinateTranslation(newReportOwner, 0);
        if (correct != null) {
          correct =
              CoordinateID.create(bestTranslation.getKey().getX() + correct.getX(), bestTranslation
                  .getKey().getY()
                  + correct.getY(), 0);
          globalData.setCoordinateTranslation(newReportOwner, correct);
        }
      }
    }

    if (newReport.hasAstralRegions() && dataReport.hasAstralRegions()) {
      ReportMerger.log.info("Using this astral translation: " + bestAstralTranslation.getKey());

      if (newReportOwner != null) {
        if (globalData.getCoordinateTranslation(newReportOwner, 1) != null
            && !globalData.getCoordinateTranslation(newReportOwner, 1).equals(
                bestAstralTranslation.getKey())) {
          ReportMerger.log.warn("old astral translation "
              + globalData.getCoordinateTranslation(newReportOwner, 1)
              + " inconsistent with new translation " + bestAstralTranslation.getKey());
        }
        if (bestAstralTranslation.getScore() >= 0) {
          CoordinateID correct = newReport.getData().getCoordinateTranslation(newReportOwner, 1);
          if (correct != null) {
            correct =
                CoordinateID.create(bestAstralTranslation.getKey().getX() + correct.getX(),
                    bestAstralTranslation.getKey().getY() + correct.getY(), 1);
            globalData.setCoordinateTranslation(newReportOwner, correct);
          }
        }
      }
    }
  }

  private Score<CoordinateID> findSavedMapping(ReportCache dataReport2, ReportCache newReport,
      int layer) {
    EntityID newReportOwner = newReport.getData().getOwnerFaction();
    // ask user if necessary
    if (newReportOwner == null && interactive) {
      if (!newReport.getData().getFactions().isEmpty()) {
        Faction firstFaction = newReport.getData().getFactions().iterator().next();
        Object result =
            ui.input(Resources.getFormatted("util.reportmerger.msg.inputowner.msg", newReport
                .getFile().getName()), Resources.get("util.reportmerger.msg.inputowner.title"),
                newReport.getData().getFactions().toArray(), firstFaction);
        if (result != null && result instanceof Faction) {
          newReport.getData().setOwnerFaction(((Faction) result).getID());
        }
      }
    }

    CoordinateID translation =
        SavedTranslationsMapping.getSingleton().getMapping(dataReport2.getData(),
            newReport.getData(), layer);
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
  private Score<CoordinateID> decideTranslation(ReportCache newReport,
      Collection<Score<CoordinateID>> translationList, Score<CoordinateID> savedTranslation,
      int layer) {
    Score<CoordinateID> bestTranslation =
        new Score<CoordinateID>(CoordinateID.create(0, 0, layer), -1);

    if (translationList != null && translationList.size() > 0) {
      bestTranslation = Collections.max(translationList);

      ReportMerger.log.info("Found " + translationList.size() + " translations in layer " + layer
          + " for " + newReport.getFile().getName() + " (best(maxScore):"
          + bestTranslation.toString() + ")");

      // Fiete: just see the other translations...
      /*
       * ReportMerger.log.info("DEBUG: all found translations:"); for (Score<CoordinateID>myScore :
       * translationList){ ReportMerger.log.info(myScore.toString()); }
       */

    } else {
      ReportMerger.log.info("No translation in layer " + layer + " found for "
          + newReport.getFile().getName());
    }

    // choose the saved translation only if no good translation has been found
    if (savedTranslation != null) {
      if (!bestTranslation.getKey().equals(savedTranslation.getKey())) {
        ReportMerger.log.info("Saved translation in layer " + layer + " "
            + savedTranslation.getKey() + " != " + bestTranslation.getKey()
            + " best translation found.");
        if (bestTranslation.getScore() > 0) {
          ReportMerger.log.info("Preferring computed translation");
        }
      }
      if (bestTranslation.getScore() < 0) {
        ReportMerger.log.info("Using saved translation in layer " + layer);
        bestTranslation = savedTranslation;
      }
    } else {
      ReportMerger.log.info("no known translation (no translation saved in CR)");
    }
    if (bestTranslation.getScore() < 0) {
      ReportMerger.log.warn("No good translation found in layer " + layer);
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
  private Score<CoordinateID> askTranslation(ReportCache newReport,
      Collection<Score<CoordinateID>> translationList, Score<CoordinateID> savedTranslation,
      int layer) {
    Score<CoordinateID> bestTranslation =
        decideTranslation(newReport, translationList, savedTranslation, layer);

    // sanity check: translation changed since last time?
    boolean changed = false;
    if (savedTranslation != null && !bestTranslation.getKey().equals(savedTranslation.getKey())) {
      changed = true;
    }
    // sanity check: more than one good translation?
    Set<CoordinateID> distinct = new HashSet<CoordinateID>();
    if (translationList != null) {
      for (Score<CoordinateID> t : translationList) {
        if (t.getScore() >= 0) {
          distinct.add(t.getKey());
        }
      }
    }
    if (savedTranslation != null) {
      distinct.add(savedTranslation.getKey());
    }

    if (bestTranslation.getScore() >= 0 && !changed && distinct.size() == 1)
      // all seems well, return
      return bestTranslation;
    else {
      // ask user what to do
      String message =
          Resources.getFormatted("util.reportmerger.msg.method", newReport.getFile().getName(),
              layer)
              + (changed ? Resources.get("util.reportmerger.msg.changed") : "");

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

      choices.addAll(translationList);

      Object choice =
          ui.input(message, Resources.get("util.reportmerger.msg.method.title"), choices.toArray(),
              bestMethod);
      if (choice == null || choice.equals(skipMethod))
        return null;

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
                "util.reportmerger.msg.usertranslation.choose", newReport.getFile().getName(),
                layer), Resources.get("util.reportmerger.msg.usertranslation.title"), tChoices
                .toArray(), bestTranslation);
        Score<CoordinateID> chosenTranslation = null;
        // workaround since we cannot cast Object to Score<CoordinateID>
        for (Score<CoordinateID> s : tChoices) {
          if (s.equals(help)) {
            chosenTranslation = s;
            break;
          }
        }
        if (chosenTranslation != null) {
          ReportMerger.log.info("user choice: " + chosenTranslation);
          bestTranslation = chosenTranslation;
        } else {
          ReportMerger.log.info("user abort");
          bestTranslation = inputMapping(newReport, layer);
        }
      } else if (choice.equals(inputMethod)) {
        bestTranslation = inputMapping(newReport, layer);
      } else if (choice instanceof Score<?>) {
        // workaround since we cannot cast Object to Score<CoordinateID>
        for (Score<CoordinateID> s : translationList) {
          if (s.equals(choice)) {
            bestTranslation = s;
            break;
          }
        }
      } else {
        ReportMerger.log.error("Unexpected choice");
        return null;
      }

    }
    return bestTranslation;
  }

  private Score<CoordinateID> inputMapping(ReportCache newReport, int layer) {
    Score<CoordinateID> resultTranslation = null;
    boolean badInput = false;
    boolean cancelled = true;
    do {
      badInput = false;
      String message =
          Resources.getFormatted("util.reportmerger.msg.usertranslation.x", newReport.getFile()
              .getName(), layer);
      Object xS =
          ui
              .input(message, Resources.get("util.reportmerger.msg.usertranslation.title"), null,
                  "0");
      if (xS != null) {
        message =
            Resources.getFormatted("util.reportmerger.msg.usertranslation.y", newReport.getFile()
                .getName(), layer);
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
            ReportMerger.log.debug("using user translation: " + resultTranslation.getKey()
                + " in layer " + layer);
          } catch (NumberFormatException e) {
            badInput = true;
          }
        }
      }
    } while (badInput && !cancelled);
    if (cancelled) {
      ReportMerger.log.info("user input cancelled");
      return null;
    }
    return resultTranslation;
  }

  /**
   * Check if newReport's game type matches that of global data.
   * 
   * @param newReport
   * @return
   */
  private boolean checkGameType(ReportCache newReport) {
    if (newReport.getData() == null
        || !globalData.getGameName().equalsIgnoreCase(newReport.getData().getGameName())) {
      // no report loaded or
      // game types doesn't match. Make sure, it will not be tried again.

      if (ui != null) {
        // Fiete 20090105: displayed a yes/no box with error message - bug #348
        // ui.confirm(newReport.getFile().getName() + ": " +
        // Resources.get("util.reportmerger.msg.wronggametype"), newReport.getFile().getName());
        // FIXME (stm) this can be a redundant message
        ui.showMessageDialog(newReport.getFile().getName() + ": "
            + Resources.get("util.reportmerger.msg.wronggametype"));
      }
      if (newReport.getData() == null) {
        ReportMerger.log.warn("ReportMerger.mergeReport(): got empty data.");
      } else {
        ReportMerger.log.warn("ReportMerger.mergeReport(): game types don't match.");
      }

      newReport.setMerged(true);

      return false;
    }
    return true;
  }

}
