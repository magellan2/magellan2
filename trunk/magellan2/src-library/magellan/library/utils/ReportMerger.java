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

/**
 * Helper class.
 */
public class ReportMerger extends Object {
  private static final Logger log = Logger.getInstance(ReportMerger.class);

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
   * An interface for classes that transform coordinates. Possibly used by a report parser to
   * translate a report.
   */
  public static interface ReportTranslator {
    /**
     * Return a coordinate related to c. Should be the inverse of
     * {@link #inverseTransform(CoordinateID)}.
     */
    public CoordinateID transform(CoordinateID c);

    /**
     * Return a coordinate related to c. Should be the inverse of {@link #transform(CoordinateID)}.
     */
    public CoordinateID inverseTransform(CoordinateID c);
  }

  /**
   * Returns the coordinates unchanged.
   */
  public static class IdentityTranslator implements ReportTranslator {

    public CoordinateID inverseTransform(CoordinateID c) {
      return c;
    }

    public CoordinateID transform(CoordinateID c) {
      return c;
    }

  }

  /**
   * Translates coordinates in two levels by a given translation.
   */
  public static class TwoLevelTranslator implements ReportTranslator {

    private CoordinateID bestTranslation;
    private CoordinateID bestAstralTranslation;
    private CoordinateID translate2;
    private CoordinateID translate1;

    /**
     * @param bestTranslation
     * @param bestAstralTranslation
     */
    public TwoLevelTranslator(CoordinateID bestTranslation, CoordinateID bestAstralTranslation) {
      this.bestTranslation = bestTranslation;
      this.bestAstralTranslation = bestAstralTranslation;
      translate1 = CoordinateID.create(bestTranslation.getX(), bestTranslation.getY());
      translate2 = CoordinateID.create(bestAstralTranslation.getX(), bestAstralTranslation.getY());
    }

    /**
     * If c is in the layer of bestAstralTranslation, transform it by this value, if it's in
     * bestTranslation's level, transform it by this one.
     */
    public CoordinateID transform(CoordinateID c) {
      if (c.getZ() == bestTranslation.getZ())
        return c.subtract(translate1);
      if (c.getZ() == bestAstralTranslation.getZ())
        return c.subtract(translate2);
      return c;
    }

    /**
     * If c is in the layer of bestAstralTranslation, transform it by this value, if it's in
     * bestTranslation's level, transform it by this one.
     */
    public CoordinateID inverseTransform(CoordinateID c) {
      if (c.getZ() == bestTranslation.getZ())
        return c.translate(translate1);
      if (c.getZ() == bestAstralTranslation.getZ())
        return c.translate(translate2);
      return c;
    }

  }

  /**
   * Manages a report and provides information relevant to ReportMerger.
   * 
   * @author stm
   * @version 1.0, Dec 17, 2007
   */
  private class ReportCache implements Comparable<ReportCache> {
    // data set
    SoftReference<GameData> dataReference = null;

    // you can either set game data statically...
    GameData staticData = null;

    // ...or load data from a file
    File file = null;

    // maps region names to a set of region coordinates (of regions with that
    // name)
    Map<String, Collection<Region>> regionMap = null;

    // maps schemes (region names) to a Collection of astral regions
    // containing that scheme
    Map<String, Collection<Region>> schemeMap = null;

    // maps regionUIDs to the Region
    // Map<Long, Region> regionUIDMap = null;

    boolean hasAstralRegions = false;

    // already merged with another report
    boolean merged = false;

    private Date date = null;

    private boolean mergeError = false;

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
      // ui.setProgress(file.getName() + " - " + Resources.get("util.reportmerger.status.loading"),
      // iProgress);

      GameData data = loader.load(file);
      dataReference = new SoftReference<GameData>(data);
      return data;
    }

    /**
     * Perform initializations if not already done.
     */
    private void init() {
      if (schemeMap == null || regionMap == null) {
        initRegionMaps();
      }
    }

    /**
     * Sets up regionMap and schemeMap und regionUIDMap.
     */
    private void initRegionMaps() {
      GameData data = getData();

      regionMap = new HashMap<String, Collection<Region>>();
      schemeMap = new HashMap<String, Collection<Region>>();
      /*
       * regionUIDMap = new HashMap<Long, Region>();
       */

      for (Region region : data.getRegions()) {
        /*
         * if ((region.getName() != null) && (region.getName().length() > 0)) { Collection<Region>
         * regions = regionMap.get(region.getName()); if (regions == null) { regions = new
         * HashSet<Region>(); regionMap.put(region.getName(), regions); } regions.add(region); } if
         * (region.getUID()!=0){ regionUIDMap.put(region.getUID(), region); }
         */
        if (region.getCoordinate().getZ() == 1) {
          hasAstralRegions = true;
          /*
           * for (Scheme scheme : region.schemes()) { Collection<Region> astralRegions =
           * schemeMap.get(scheme.getName()); if (astralRegions == null) { astralRegions = new
           * HashSet<Region>(); schemeMap.put(scheme.getName(), astralRegions); }
           * astralRegions.add(region); }
           */
        }
      }
    }

    public boolean hasAstralRegions() {
      init();
      return hasAstralRegions;
    }

    /**
     * Ensures order by increasing date.
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
      if (regionMap != null) {
        regionMap.clear();
      }
      if (schemeMap != null) {
        schemeMap.clear();
      }
      regionMap = null;
      schemeMap = null;
      date = null;
    }

    public boolean isMergeError() {
      return mergeError;
    }

    public void setMergeError(boolean mergeError) {
      this.mergeError = mergeError;
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

    /*
     * @param name
     * @return The regions with the specified name public Collection<Region> getRegionsByName(String
     * name) { init(); Collection<Region> result = regionMap.get(name); if (result == null) return
     * Collections.emptySet(); else return result; } public Region getRegionByUID(long regionUID){
     * init(); return regionUIDMap.get(regionUID); }
     */

    /*
     * @param name
     * @return The set of astral regions having a scheme named <code>name</code> public
     * Collection<Region> getAstralRegionBySchemeName(String name) { init(); Collection<Region>
     * result = schemeMap.get(name); if (result == null) return Collections.emptySet(); else return
     * result; }
     */
  }

  // merged data set
  GameData globalData = null;

  ReportCache dataReport = null;

  // reports to merge
  ReportCache reports[] = null;

  // loader interface
  Loader loader = null;

  // data assign interface
  AssignData assignData = null;

  UserInterface ui;
  int iProgress;

  private boolean sort = false;

  private boolean interactive = false;

  /**
   * Creates new ReportMerger
   */
  public ReportMerger(GameData _data, File files[], Loader _loader, AssignData _assignData) {
    globalData = _data;
    globalData.removeTheVoid(); // removes void regions
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
    }

    // inform user about memory problems
    if (globalData.outOfMemory) {
      ui.showDialog((new JOptionPane(Resources.get("client.msg.outofmemory.text"),
          JOptionPane.WARNING_MESSAGE, JOptionPane.DEFAULT_OPTION)).createDialog(Resources
          .get("client.msg.outofmemory.title")));
      ReportMerger.log.error(Resources.get("client.msg.outofmemory.text"));
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

    if (assignData != null) {
      assignData.assign(globalData);
    }

    return globalData;
  }

  private boolean askToMergeAnyway() {
    StringBuilder strMessage =
        new StringBuilder(Resources.get("util.reportmerger.msg.noconnection.text.1"));

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

    adjustTrustlevels(newReport);

    setTempID(newReport);

    /***************************************************************************
     * Look for coordinate translation Important: A faction's coordinate system for astral space is
     * independent of it's coordinate system for normal space. It depends (as far as I know) on the
     * astral space region where the faction first enters the astral space (this region will have
     * the coordinate (0,0,1). Thus a special translation for the astral space (beside that one for
     * normal space) has to be found.
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

        storeTranslations(newReport, bestTranslation, bestAstralTranslation);

        GameData clonedData = translateReport(newReport, bestTranslation, bestAstralTranslation);

        /***************************************************************************
         * Merge the reports, finally!
         */
        iProgress += 1;
        if (ui != null) {
          ui.setProgress(newReport.getFile().getName() + " - "
              + Resources.get("util.reportmerger.status.merging"), iProgress);
        }
        globalData = GameDataMerger.merge(globalData, clonedData);
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

  /***************************************************************************
   * translate new report
   */
  private GameData translateReport(ReportCache newReport, Score<CoordinateID> bestTranslation,
      Score<CoordinateID> bestAstralTranslation) {
    GameData clonedData = newReport.getData();
    if (bestTranslation.getKey().getX() != 0 || bestTranslation.getKey().getY() != 0
        || bestAstralTranslation.getKey().getX() != 0 || bestAstralTranslation.getKey().getY() != 0) {
      try {
        clonedData =
            (GameData) clonedData.clone(new TwoLevelTranslator(bestTranslation.getKey(),
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

    if (!newReport.isMergeError() && !dataReport.isMergeError() && bestTranslation.getScore() >= 0
        && !changed && distinct.size() == 1)
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
   * Prepare curTempID-Value for merging. If reports are of the same age, keep existing by setting
   * the new one to default value. Otherwise set the existing to default value.
   */
  private void setTempID(ReportCache newReport) {
    if ((globalData.getDate() != null) && (newReport.getData().getDate() != null)
        && (globalData.getDate().getDate() < newReport.getData().getDate().getDate())) {
      globalData.setCurTempID(-1);
    } else {
      newReport.getData().setCurTempID(-1);
    }
  }

  /**
   * prepare faction trustlevel for merging: - to be added CR is older or of same age -> hold
   * existing trust levels - to be added CR is newer and contains trust level that were set by the
   * user explicitly (or read from CR what means the same) -> take the trust levels out of the new
   * CR otherwise -> hold existing trust levels This means: set those trust levels, that will not be
   * retained to default values
   */
  private void adjustTrustlevels(ReportCache newReport) {
    if ((globalData.getDate() != null) && (newReport.getData().getDate() != null)
        && (globalData.getDate().getDate() < newReport.getData().getDate().getDate())
        && TrustLevels.containsTrustLevelsSetByUser(newReport.getData())) {
      // take the trust levels out of the to be added data
      // set those in the existing data to default-values
      for (Faction f : globalData.getFactions()) {
        f.setTrustLevel(Faction.TL_DEFAULT);
        f.setTrustLevelSetByUser(false);
      }
    } else {
      // take the trust levels out of the existing data
      // set those in the to be added data to default-values
      for (Faction f : newReport.getData().getFactions()) {
        f.setTrustLevel(Faction.TL_DEFAULT);
        f.setTrustLevelSetByUser(false);
      }
    }

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
