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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import magellan.library.CoordinateID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.Scheme;
import magellan.library.StringID;
import magellan.library.rules.Date;
import magellan.library.rules.RegionType;
import magellan.library.utils.logging.Logger;

/**
 * Helper class.
 */
public class ReportMerger extends Object {
  private static final Logger log = Logger.getInstance(ReportMerger.class);

  private static final int USER_SCORE = Integer.MAX_VALUE;
  private static final int SAVED_SCORE = Integer.MAX_VALUE - 2;
  private static final int ASTRALTOREAL_SCORE = Integer.MAX_VALUE - 6;
  private static final int ONEREGION_SCORE = Integer.MAX_VALUE - 4;

  /**
   * 
   */
  public interface Loader {
    /**
     * 
     */
    public GameData load(File file);
  }

  /**
   * 
   */
  public interface AssignData {
    /**
     * 
     */
    public void assign(GameData _data);
  }

  /**
   * Manages a report and provides information relevant to ReportMerger.
   * 
   * @author ...
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

    boolean hasAstralRegions = false;

    // already merged with another report
    boolean merged = false;

    private Date date = null;

    /**
     * Creates a new ReportCache which will read the report from
     * <code>file</code> as needed using this ReportMergers
     * <code>Loader</code>.
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
//      ui.setProgress(file.getName() + " - " + Resources.get("util.reportmerger.status.loading"), iProgress);

      GameData data = loader.load(file);
      dataReference = new SoftReference<GameData>(data);
      return data;
    }

    /**
     * Perform initializations if not already done.
     */
    private void init() {
      if (schemeMap == null || regionMap == null)
        initRegionMaps();
    }

    /**
     * Sets up regionMap and schemeMap.
     */
    private void initRegionMaps() {
      GameData data = getData();

      regionMap = new HashMap<String, Collection<Region>>();
      schemeMap = new HashMap<String, Collection<Region>>();

      for (Region region : data.regions().values()) {
        if ((region.getName() != null) && (region.getName().length() > 0)) {
          Collection<Region> regions = regionMap.get(region.getName());
          if (regions == null) {
            regions = new HashSet<Region>();
            regionMap.put(region.getName(), regions);
          }
          regions.add(region);
        }

        if (region.getCoordinate().z == 1) {
          hasAstralRegions = true;
          for (Scheme scheme : region.schemes()) {
            Collection<Region> astralRegions = schemeMap.get(scheme.getName());
            if (astralRegions == null) {
              astralRegions = new HashSet<Region>();
              schemeMap.put(scheme.getName(), astralRegions);
            }
            astralRegions.add(region);
          }
        }
      }
    }

    public boolean hasAstralRegions() {
      init();
      return hasAstralRegions;
    }

    /**
     * Astral: A...B | | Real: A---B
     * 
     * To merge two non-overlapping Astral Spaces we need: - an astral to real
     * mapping of report A - an overlapping real world between both reports - an
     * astral to real mapping of report B
     * 
     * Astral to real mapping can be done by two ways: 1. from two neighbour
     * astral spaces with schemes (there can be seen exactly one same scheme
     * from both astral regions) 2. from several astral regions with schemes,
     * calculating the "extend" of the schemes
     * 
     * ==> Having only one astral region with schemes will often not be enough
     * to calculate the mapping between astral and real space
     * 
     * Variant 2 not jet implemented!
     * 
     */
    private CoordinateID getReportAstralToReal() {
      GameData data = getData();

      CoordinateID reportAstralToReal = null;

      CoordinateID minExtend = null;
      CoordinateID maxExtend = null;

      init();

      for (Region region : data.regions().values()) {
        if (region.getCoordinate().z == 1) {
          for (Scheme scheme : region.schemes()) {
            Collection<Region> regionsForScheme = schemeMap.get(scheme.getName());
            if (regionsForScheme.size() > 1) {
              if (regionsForScheme.size() > 2) {
                log.error("Report corrupted: scheme visible from more than two regions: " + scheme);
              }
              /**
               * This is the second astral region showing the same scheme. From
               * this we can calculate an astral to real mapping for the new
               * report by variant 1
               */
              CoordinateID firstCoord = region.getCoordinate();
              CoordinateID secondCoord = null;
              for (Region secondScheme : regionsForScheme) {
                if (!secondScheme.equals(region)) {
                  secondCoord = secondScheme.getCoordinate();
                  break;
                }
              }
              CoordinateID schemeCoord = scheme.getCoordinate();
              CoordinateID newTranslation = new CoordinateID(schemeCoord.x - 2 * (firstCoord.x + secondCoord.x), schemeCoord.y - 2 * (firstCoord.y + secondCoord.y));
              if (reportAstralToReal != null && !reportAstralToReal.equals(newTranslation))
                log.warn("two astral to real translations found: " + reportAstralToReal + "; " + newTranslation);

              reportAstralToReal = newTranslation;
            }

            // we may not find any astral to real mapping by variant 1 above
            // therefore also do calculations for variant 2 here
            // we "normalize" all schemes to be in the area
            // only if not already found a mapping
            if (true) {
              int nx = scheme.getCoordinate().x - 4 * region.getCoordinate().x;
              int ny = scheme.getCoordinate().y - 4 * region.getCoordinate().y;
              // this is a virtual 3 axis diagonal to x and y in the same level,
              // but we store it in the z coordinate
              int nd = nx + ny;
              if (minExtend == null) {
                minExtend = new CoordinateID(nx, ny, nd);
                maxExtend = new CoordinateID(nx, ny, nd);
              } else {
                minExtend.x = Math.min(minExtend.x, nx);
                minExtend.y = Math.min(minExtend.y, ny);
                minExtend.z = Math.min(minExtend.z, nd);
                maxExtend.x = Math.max(maxExtend.x, nx);
                maxExtend.y = Math.max(maxExtend.y, ny);
                maxExtend.z = Math.max(maxExtend.z, nd);
              }
              // now check if we found an "extend of 4" in at least two
              // directions of the three directions
              boolean dx = maxExtend.x - minExtend.x == 4;
              boolean dy = maxExtend.y - minExtend.y == 4;
              boolean dd = maxExtend.z - minExtend.z == 4;
              CoordinateID newTranslation = null;
              if (dx && dy) {
                newTranslation = new CoordinateID(maxExtend.x - 2, maxExtend.y - 2);
              } else if (dx && dd) {
                newTranslation = new CoordinateID(maxExtend.x - 2, maxExtend.z - maxExtend.x);
              } else if (dy && dd) {
                newTranslation = new CoordinateID(maxExtend.z - maxExtend.y, maxExtend.y - 2);
              }
              if (newTranslation != null) {
                if (reportAstralToReal != null) {
                  if (!reportAstralToReal.equals(newTranslation))
                    log.warn("more than one astral to real translations found: " + reportAstralToReal + "; " + newTranslation);
                } else
                  reportAstralToReal = newTranslation;
              }
            }
          }
        }
      }

      return reportAstralToReal;
    }

    /**
     * Ensures order by increasing date.
     * 
     * @return <code>1</code> iff this reports round is greater than
     *         <code>otherReport</code>'s round.
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(ReportCache otherReport) {
      if (otherReport == null)
        return 1;
      else
        return otherReport.getRound() > this.getRound() ? -1 : (otherReport.getRound() < this.getRound() ? 1 : 0);
    }

    private int getRound() {
      if (date == null)
        date = getData().getDate();
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
      if (regionMap != null)
        regionMap.clear();
      if (schemeMap != null)
        schemeMap.clear();
      regionMap = null;
      schemeMap = null;
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

    /**
     * @param name
     * @return The regions with the specified name
     */
    public Collection<Region> getRegionsByName(String name) {
      init();
      Collection<Region> result = regionMap.get(name);
      if (result == null)
        return Collections.emptySet();
      else
        return result;
    }

    /**
     * @param name
     * @return The set of astral regions having a scheme named <code>name</code>
     */
    public Collection<Region> getAstralRegionBySchemeName(String name) {
      init();
      Collection<Region> result = schemeMap.get(name);
      if (result == null)
        return Collections.emptySet();
      else
        return result;
    }

  }

  /**
   * A class holding a coordinate translation and its score.
   * 
   */
  protected class DefaultTranslation implements Comparable<DefaultTranslation>{
    int score = -1;
    CoordinateID translation = null;

    public DefaultTranslation(CoordinateID t, int s) {
      translation = t;
      score = s;
    }

    public int getScore() {
      return score;
    }

    public void setScore(int score) {
      this.score = score;
    }

    public CoordinateID getTranslation() {
      return translation;
    }

    public int compareTo(DefaultTranslation o) {
      return score > o.getScore() ? 1 : (score < o.getScore() ? -1 : 0);
    }
  }

  // merged data set
  GameData globalData = null;

  // reports to merge
  ReportCache reports[] = null;

  // loader interface
  Loader loader = null;

  // data assign interface
  AssignData assignData = null;

  UserInterface ui;
  int iProgress;

  /**
   * Creates new ReportMerger
   * 
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
   * 
   */
  public ReportMerger(GameData _data, File file, Loader _loader, AssignData _assignData) {
    this(_data, new File[] { file }, _loader, _assignData);
  }

  /**
   * 
   */
  public GameData merge() {
    return merge(new NullUserInterface(), false);
  }

  /**
   * Start merging
   * 
   * @param aUI
   *          A user interface for indicating progress, displaying messages etc.
   * @param async
   *          If <code>true</code> the merging will be started in a new
   *          thread.
   * 
   */
  public GameData merge(UserInterface aUI, boolean async) {
    ui = aUI;
    if (ui != null)
      ui.setMaximum(reports.length * 4); // four steps per report
    if (async) {
      new Thread(new Runnable() {
        public void run() {
          ReportMerger.this.mergeThread();
        }
      }).start();
      return null;
    } else {
      return this.mergeThread();
    }
  }

  /**
   * Do the merging. This method can be executed in its own thread.
   */
  private synchronized GameData mergeThread() {
    if (ui != null) {
      ui.show();
      ui.setProgress(Resources.get("util.reportmerger.status.sorting"), 0);
    }
    sortReports();

    /**
     * We merge reports one by one. If merging of a report fails, we try to
     * merge all other reports. We only break, if all reports, subsequently,
     * have failed to merge.
     */
    try {
      int currentReport = 0;
      int iFailedConnectivity = 0;
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
            if (r.isMerged())
              iMerged++;
          }
        }
      }

      // prepare failure message
      if (iFailedConnectivity > 0) {
        String strMessage = Resources.get("util.reportmerger.msg.noconnection.text.1");

        for (int i = 0; i < reports.length; i++) {
          if (!reports[i].isMerged()) {
            strMessage += reports[i].getFile().getName();

            if ((i + 1) < reports.length) {
              strMessage += ", ";
            }
          }
        }

        strMessage += Resources.get("util.reportmerger.msg.noconnection.text.2");

        // ask to merge anyway
        if (ui != null && ui.confirm(strMessage, Resources.get("util.reportmerger.msg.confirmmerge.title"))) {
          for (int i = 0; i < reports.length; i++) {
            if (!reports[i].isMerged()) {
              iProgress += 2;
              ui.setProgress(reports[i].getFile().getName() + " - " + Resources.get("util.reportmerger.status.merging"), iProgress);

              // data.mergeWith( reports[i].getData() );
              globalData = GameData.merge(globalData, reports[i].getData());

              reports[i].setMerged(true);

              reports[i].release();
            }
          }
        }
      }
    } catch (Exception e) {
      log.error(e);
      ui.showException(Resources.get("reportmerger.exception"), null, e);
    }

    if (ui != null) {
      ui.ready();
    }

    if (assignData != null) {
      assignData.assign(globalData);
    }

    return globalData;
  }

  /**
   * Sort the reports according to their Comparator which currently means,
   * according to their age.
   */
  private void sortReports() {
    Arrays.sort(reports);
  }

  /**
   * Merges a report to the current report. <code>newReport</code>.merged is
   * set to true if it has been successfully merged or if it can never be merged
   * successfully.
   * 
   * @param newReport
   * @return true iff reports were merged or report data null or report types
   *         don't match
   */
  private boolean mergeReport(ReportCache newReport) {
    ReportCache dataReport = new ReportCache(globalData);

    iProgress += 1;
    if (ui != null) {
      ui.setProgress(newReport.getFile().getName() + " - " + Resources.get("util.reportmerger.status.processing"), iProgress);
    }

    // return if gametype is wrong
    if (!checkGameType(newReport))
      return true;

    adjustTrustlevels(newReport);

    setTempID(newReport);

    /***************************************************************************
     * Look for coordinate translation
     * 
     * Important: A faction's coordinate system for astral space is indepent of
     * it's coordinate system for normal space. It depends (as far as I know) on
     * the astral space region where the faction first enters the astral space
     * (this region will have the coordinate (0,0,1). Thus a special translation
     * for the astral space (beside that one for normal space) has to be found.
     */
    iProgress += 1;
    if (ui != null) {
      ui.setProgress(newReport.getFile().getName() + " - " + Resources.get("util.reportmerger.status.connecting"), iProgress);
    }

    /***************************************************************************
     * find best translation
     */
    Map<CoordinateID, DefaultTranslation> translationMap = addTranslationCandidates(newReport);
    adjustTranslationScore(newReport, translationMap);

    // try to find a translation for the new report's owner faction in the
    // global data's owner faction
    DefaultTranslation savedTranslation = null;
    if (globalData.getOwnerFaction() != null && globalData.getFaction(globalData.getOwnerFaction()) != null && newReport.getData().getOwnerFaction() != null) {
      CoordinateID trans = globalData.getCoordinateTranslation(newReport.getData().getOwnerFaction(), 0);
      if (trans != null) {
        savedTranslation = new DefaultTranslation(trans, SAVED_SCORE);
      }
    }

    DefaultTranslation bestTranslation = decideTranslation(newReport, translationMap, savedTranslation, false);

    if (bestTranslation != null) {
      /*************************************************************************
       * find best astral translation
       */
      DefaultTranslation bestAstralTranslation = null;
      if (!newReport.hasAstralRegions() || !dataReport.hasAstralRegions()) {
        bestAstralTranslation = new DefaultTranslation(new CoordinateID(0, 0, 1), -1);
      } else {
        Map<CoordinateID, DefaultTranslation> astralTranslationMap = addAstralTranslationCandidates(newReport);
        adjustAstralTranslationScore(newReport, astralTranslationMap);

        addAstralTranslationCandidates(dataReport, newReport, astralTranslationMap, bestTranslation.getTranslation());
        addAstralTranslationCandidates2(dataReport, newReport, astralTranslationMap, bestTranslation.getTranslation());

        DefaultTranslation savedAstralTranslation = null;
        if (globalData.getOwnerFaction() != null && globalData.getFaction(globalData.getOwnerFaction()) != null && newReport.getData().getOwnerFaction() != null) {
          CoordinateID trans = globalData.getCoordinateTranslation(newReport.getData().getOwnerFaction(), 1);
          if (trans != null)
            savedAstralTranslation = new DefaultTranslation(trans, SAVED_SCORE);
        }

        bestAstralTranslation = decideTranslation(newReport, astralTranslationMap, savedAstralTranslation, true);
      }
//      if (bestAstralTranslation == null && ui!=null){
//         String choice = (String) ui.input(Resources.get("util.reportmerger.msg.noastraltranslation"), null, new String [] { Resources.get("util.reportmerger.msg.mergeanyway"), Resources.get("util.reportmerger.msg.removeastral"), Resources.get("util.reportmerger.msg.dontmerge")}, Resources.get("util.reportmerger.msg.dontmerge"));
//         if (choice !=null && choice==Resources.get("util.reportmerger.msg.removeastral"))
//           newReport = new ReportCache(RegionFilter.getInstance().filter(newReport.getData()));
//      }
      if (bestAstralTranslation != null) {
        log.info("Using this translation: " + bestTranslation.getTranslation());
        if (newReport.hasAstralRegions() && dataReport.hasAstralRegions()) 
          log.info("Using this astral translation: " + bestAstralTranslation.getTranslation());
        GameData clonedData = newReport.getData();

        if (bestTranslation.getTranslation().x != 0 || bestTranslation.getTranslation().y != 0) {
          try {
            clonedData = (GameData) clonedData.clone(bestTranslation.getTranslation());
          } catch (CloneNotSupportedException e) {
            log.error(e);
          }
        }
        if (bestAstralTranslation.getTranslation().x != 0 || bestAstralTranslation.getTranslation().y != 0) {
          try {
            clonedData = (GameData) clonedData.clone(bestAstralTranslation.getTranslation());
          } catch (CloneNotSupportedException e) {
            log.error(e);
          }
        }

        // /////////////////////////////////////////////////
        // Merge the reports, finally!

        iProgress += 1;
        if (ui != null) {
          ui.setProgress(newReport.getFile().getName() + " - " + Resources.get("util.reportmerger.status.merging"), iProgress);
        }
        globalData = GameData.merge(globalData, clonedData);
        newReport.setMerged(true);

        if (newReport.getData().getOwnerFaction()!=null && !newReport.getData().getOwnerFaction().equals(globalData.getOwnerFaction())){
          // FIXME: it could be possible that there are no normal space regions in
          // the reports what happens to bestTranslation in this case?
          globalData.setCoordinateTranslation(newReport.getData().getOwnerFaction(), 0, bestTranslation.getTranslation());
          if (dataReport.hasAstralRegions() && newReport.hasAstralRegions())
            globalData.setCoordinateTranslation(newReport.getData().getOwnerFaction(), 1, bestAstralTranslation.getTranslation());
        }
      } else {
        log.info("aborting...");
        iProgress -= 1;
        if (ui != null) {
          ui.setProgress(newReport.getFile().getName(), iProgress);
        }
      }
    } else {
      log.info("aborting...");
      iProgress -= 1;
      if (ui != null) {
        ui.setProgress(newReport.getFile().getName(), iProgress);
      }
    }

    newReport.release();
    return newReport.isMerged();
  }

  /**
   * Chooses a translation among the translations in translationMap and
   * savedTranslation. If no translation is acceptable, a default translation
   * (0,0) or (0,0,1) resp. is chosen.
   * 
   * @param newReport The report to be merged
   * @param translationMap 
   * @param savedTranslation
   * @param astral If <code>true</code>, an astral translation will be returned
   * @return The best translation
   */
  private DefaultTranslation decideTranslation(ReportCache newReport, Map<CoordinateID, DefaultTranslation> translationMap, DefaultTranslation savedTranslation, boolean astral) {
    DefaultTranslation bestTranslation = new DefaultTranslation(new CoordinateID(0, 0, astral ? 1 : 0), -1);

    if (translationMap.size() > 0) {
      log.info("Found " + translationMap.size() + " " + (astral ? "astral " : "") + "translations for " + newReport.getFile().getName());
      bestTranslation = Collections.max(translationMap.values());
    } else {
      log.info("No " + (astral ? "astral " : "") + "translation found for " + newReport.getFile().getName());
    }
    // choose the saved translation only if no good translation has been found 
    if (savedTranslation != null) {
      if (!bestTranslation.getTranslation().equals(savedTranslation.getTranslation())) {
        log.info("Saved " + (astral ? "astral " : "") + "translation " + savedTranslation.getTranslation() + " != " + bestTranslation.getTranslation() + " best translation found.");
      }
      if (bestTranslation.getScore() < 0) {
        log.info("Using saved " + (astral ? "astral " : "") + "translation.");
        bestTranslation = savedTranslation;
      } else {
        log.info("Preferring computed translation");
      }
    }

    if (bestTranslation.getScore() < 0) {
      log.info("No good " + (astral ? "astral " : "") + "translation found.");
      boolean badInput = false;
      boolean cancelled = true;
      do {
        badInput = false;
        String message = astral?Resources.get("util.reportmerger.msg.usertranslation.astral.x"):Resources.get("util.reportmerger.msg.usertranslation.x");
        Object xS = ui.input((new java.text.MessageFormat(message)).format(new Object[] { newReport.getData().getOwnerFaction(), globalData.getOwnerFaction() }), Resources.get("util.reportmerger.msg.usertranslation.title"), null, "0");
        if (xS != null) {
          message = astral?Resources.get("util.reportmerger.msg.usertranslation.astral.y"):Resources.get("util.reportmerger.msg.usertranslation.y");
          Object yS = ui.input((new java.text.MessageFormat(message)).format(new Object[] { newReport.getData().getOwnerFaction(), globalData.getOwnerFaction() }), Resources.get("util.reportmerger.msg.usertranslation.title"), null, "0");
          if (yS != null) {
            cancelled = false;
            try {
              bestTranslation = new DefaultTranslation(new CoordinateID(Integer.parseInt((String) xS), Integer.parseInt((String) yS), astral ? 1 : 0), USER_SCORE);
              log.debug("using user " + (astral ? "astral " : "") + "translation: " + bestTranslation.getTranslation());
            } catch (NumberFormatException e) {
              badInput = true;
            }
          }
        }
      } while (badInput && !cancelled);
      if (cancelled){
        log.info("user input cancelled");
        return null;
      }
    }

    return bestTranslation;
  }

  /**
   * Tries to find matching regions and adds translations to the map accordingly.
   * 
   * @param newReport
   * @return
   */
  private Map<CoordinateID, DefaultTranslation> addTranslationCandidates(ReportCache newReport) {
    Map<CoordinateID, DefaultTranslation> translationMap = new Hashtable<CoordinateID, DefaultTranslation>();
    for (Region region : globalData.regions().values()) {
      CoordinateID coord = region.getCoordinate();
  
      if (coord.z == 0 && (region.getName() != null) && (region.getName().length() > 0)) {
        for (Region foundRegion : newReport.getRegionsByName(region.getName())) {
          if (foundRegion != null) {
            CoordinateID foundCoord = foundRegion.getCoordinate();
            CoordinateID translation = new CoordinateID(foundCoord.x - coord.x, foundCoord.y - coord.y);
            addTranslation(translationMap, translation);
          }
        }
      }
    }
    return translationMap;
  }

  /**
   * Add translation to map and increase score by 1.
   * 
   * @param translationMap
   * @param translation
   */
  private void addTranslation(Map<CoordinateID, DefaultTranslation> translationMap, CoordinateID translation) {
    DefaultTranslation translationCandidate = translationMap.get(translation);
  
    if (translationCandidate == null) {
      translationCandidate = new DefaultTranslation(translation, 1);
    } else {
      translationCandidate.setScore(translationCandidate.getScore() + 1);
    }
    translationMap.put(translation, translationCandidate);
  }

  /**
   * check whether any of the normal space translations is impossible by
   * comparing the terrains. Adjust score according to number of mismatches.
   */
  private void adjustTranslationScore(ReportCache newReport, Map<CoordinateID, DefaultTranslation> translationMap) {
    int maxTerrainMismatches = (int) (Math.max(globalData.regions().size(), newReport.getData().regions().size()) * 0.02);
    RegionType forestTerrain = globalData.rules.getRegionType(StringID.create("Wald"));
    RegionType plainTerrain = globalData.rules.getRegionType(StringID.create("Ebene"));
    RegionType oceanTerrain = globalData.rules.getRegionType(StringID.create("Ozean"));
    RegionType glacierTerrain = globalData.rules.getRegionType(StringID.create("Gletscher"));
    RegionType activeVolcanoTerrain = globalData.rules.getRegionType(StringID.create("Aktiver Vulkan"));
    RegionType volcanoTerrain = globalData.rules.getRegionType(StringID.create("Vulkan"));
  
    for (DefaultTranslation translationCandidate : translationMap.values()) {
      /*
       * the number of regions not having the same region type at the current
       * translations
       */
      int mismatches = 0; 
  
      /* for each translation we have to compare the regions' terrains */
      for (Iterator<Region> regionIter = globalData.regions().values().iterator(); regionIter.hasNext();) {
        Region r = regionIter.next();
  
        if ((r.getType() == null) || r.getType().equals(RegionType.unknown)) {
          continue;
        }
  
        CoordinateID c = r.getCoordinate();
  
        /*
         * do the translation and find the corresponding region in the report
         * data
         */
        if (c.z == 0) {
          CoordinateID translatedCoord = new CoordinateID(0, 0, 0);
          translatedCoord.x = c.x;
          translatedCoord.y = c.y;
          translatedCoord.translate(translationCandidate.getTranslation());
  
          Region reportDataRegion = newReport.getData().regions().get(translatedCoord);
  
          /*
           * the hit count for the current translation must only be modified, if
           * there actually are regions to be compared and their terrains are
           * valid
           */
          if ((reportDataRegion != null) && (reportDataRegion.getType() != null) && !(reportDataRegion.getType().equals(RegionType.unknown))) {
            if (!r.getType().equals(reportDataRegion.getType())) {
              /*
               * now we have a mismatch. If the reports are from the same turn,
               * terrains may not differ at all. If the reports are from
               * different turns, some terrains can be transformed.
               */
              if ((globalData.getDate() != null) && (newReport.getData().getDate() != null) && globalData.getDate().equals(newReport.getData().getDate())) {
                mismatches++;
              } else {
                if (!(((forestTerrain != null) && (plainTerrain != null) && 
                    ((forestTerrain.equals(r.getType()) && plainTerrain.equals(reportDataRegion.getType())) || 
                        (plainTerrain.equals(r.getType()) && forestTerrain.equals(reportDataRegion.getType())))) || 
                        ((oceanTerrain != null) && (glacierTerrain != null) && 
                            ((oceanTerrain.equals(r.getType()) && glacierTerrain.equals(reportDataRegion.getType())) || 
                                (glacierTerrain.equals(r.getType()) && oceanTerrain.equals(reportDataRegion.getType())))) || 
                                ((activeVolcanoTerrain != null) && (volcanoTerrain != null) && 
                                    ((activeVolcanoTerrain.equals(r.getType()) && volcanoTerrain.equals(reportDataRegion.getType())) || 
                                        (volcanoTerrain.equals(r.getType()) && activeVolcanoTerrain.equals(reportDataRegion.getType())))))) {
                  mismatches++;
                }
              }
  
              if (mismatches > maxTerrainMismatches) {
                mismatches = translationCandidate.getScore() + 1;
                break;
              }
            }
          }
        }
      }
      translationCandidate.setScore(translationCandidate.getScore() - mismatches);
    }
  
  }

  /**
   * Try to find astral translations by simply comparing regions by schemes.
   * 
   * @param newReport
   * @return
   */
  private Map<CoordinateID, DefaultTranslation> addAstralTranslationCandidates(ReportCache newReport) {
    Map<CoordinateID, DefaultTranslation> astralTranslationMap = new Hashtable<CoordinateID, DefaultTranslation>();
    for (Region region : globalData.regions().values()) {
      CoordinateID coord = region.getCoordinate();
      if (coord.z == 1) {
        // Now try to find an astral space region that matches this region
        // We can't use region name for this, since all astral space
        // regions are named "Nebel". We use the schemes instead.
        // Since all schemes have to match it's sufficient to look at the
        // first one to find a possible match. To check whether that
        // match really is one, we have to look at all schemes.
        if (!region.schemes().isEmpty()) {
          Scheme scheme = region.schemes().iterator().next();
          Collection<Region> o = newReport.getAstralRegionBySchemeName(scheme.getName());
  
          if (o != null) {
            // we found some astral region that shares at least
            // one scheme with the actual region. However, this
            // doesn't mean a lot, since schemes belong to several
            // astral regions.
            // check whether any of those regions shares all schemes
            for (Region foundRegion : o) {
              if (equals(foundRegion.schemes(), region.schemes())) {
                // all right, seems we found a valid translation
                CoordinateID foundCoord = foundRegion.getCoordinate();
                CoordinateID translation = new CoordinateID(foundCoord.x - coord.x, foundCoord.y - coord.y, 1);
                addTranslation(astralTranslationMap, translation);
              }
            }
          }
        }
      }
    }
    return astralTranslationMap;
  }

  /**
   * Try to find astral translation by astral-to-real mapping. Add found translations to astralTranslationMap.
   * 
   * @param dataReport
   * @param newReport
   * @param astralTranslationMap
   * @param realTranslation
   */
  private void addAstralTranslationCandidates(ReportCache dataReport, ReportCache newReport, Map<CoordinateID, DefaultTranslation> astralTranslationMap, CoordinateID realTranslation) {
    boolean reportHasAstralRegions = newReport.hasAstralRegions();
    boolean dataHasAstralRegions = dataReport.hasAstralRegions();

    CoordinateID reportAstralToReal = newReport.getReportAstralToReal();
    CoordinateID dataAstralToReal = dataReport.getReportAstralToReal();

    if (!dataHasAstralRegions)
      log.info("no astral regions in data");
    if (!reportHasAstralRegions)
      log.info("no astral regions in report");
    // Put AstralTranslation via Real Space Translation
    if ((dataAstralToReal != null) && (reportAstralToReal != null)) {
      log.info("ReportMerger: Data   AR-Real: " + dataAstralToReal);
      log.info("ReportMerger: Report AR-Real: " + reportAstralToReal);
      log.info("ReportMerger: Real Data-Report: " + realTranslation.x + ", " + realTranslation.y);

      CoordinateID astralTrans = new CoordinateID((new Integer((dataAstralToReal.x - reportAstralToReal.x + realTranslation.x) / 4)).intValue(), (new Integer((dataAstralToReal.y - reportAstralToReal.y + realTranslation.y) / 4)).intValue(), 1);
      astralTranslationMap.put(astralTrans, new DefaultTranslation(astralTrans, ASTRALTOREAL_SCORE));
      log.info("ReportMerger: Real-Space-trans, Resulting Trans: " + astralTrans);
    } else {
      if (dataHasAstralRegions && reportHasAstralRegions)
        log.info("ReportMerger: no valid astral translation found (Real Space Translation)");
    }

  }

  /**
   * Try to find astral translation by one-region astral-to-real mapping. Add found translations to astralTranslationMap.
   * 
   * @param dataReport
   * @param newReport
   * @param astralTranslationMap
   * @param realTranslation
   */
  private void addAstralTranslationCandidates2(ReportCache dataReport, ReportCache newReport, Map<CoordinateID, DefaultTranslation> astralTranslationMap, CoordinateID realTranslation) {
    boolean reportHasAstralRegions = newReport.hasAstralRegions();
    boolean dataHasAstralRegions = dataReport.hasAstralRegions();
  
    // Fiete - added OneRegion Astral-Real-Mapping
    CoordinateID dataAstralToReal_OneRegion = this.getOneRegion_AR_RR_Translation(dataReport.getData());
    CoordinateID reportAstralToReal_OneRegion = this.getOneRegion_AR_RR_Translation(newReport.getData());
    if ((dataAstralToReal_OneRegion != null) && (reportAstralToReal_OneRegion != null)) {
      log.info("ReportMerger (OneRegion): Data   AR-Real: " + dataAstralToReal_OneRegion);
      log.info("ReportMerger (OneRegion): Report AR-Real: " + reportAstralToReal_OneRegion);
      CoordinateID astralTrans = new CoordinateID((new Integer((dataAstralToReal_OneRegion.x - reportAstralToReal_OneRegion.x + realTranslation.x) / 4)).intValue(), (new Integer((dataAstralToReal_OneRegion.y - reportAstralToReal_OneRegion.y + realTranslation.y) / 4)).intValue(), 1);
      log.info("ReportMerger (OneRegion): Resulting Trans: " + astralTrans);
      astralTranslationMap.put(astralTrans, new DefaultTranslation(astralTrans, ONEREGION_SCORE));
    } else {
      if (dataHasAstralRegions && reportHasAstralRegions)
        log.info("ReportMerger: no valid astral translation found (One-Region-Translation)");
    }
  
  }

  /**
   * Check the astral space translation map by comparing the schemes.
   * Heuristic: If both space regions have schemes, they shouldn't differ. If
   * they do, something is probably wrong!
   */
  private void adjustAstralTranslationScore(ReportCache newReport, Map<CoordinateID, DefaultTranslation> astralTranslationMap) {
    for (CoordinateID translation : astralTranslationMap.keySet()) {

      // the number of astral space region where a scheme mismatch was found.
      int mismatches = 0;

      /* for each translation we have to compare the regions' schemes */
      for (Region r : globalData.regions().values()) {
        if (r.getCoordinate().z != 1)
          continue;

        CoordinateID c = r.getCoordinate();

        /*
         * do the translation and find the corresponding region in the report
         * data
         */
        CoordinateID translatedCoord = new CoordinateID(0, 0, 0);
        translatedCoord.x = c.x + translation.x;
        translatedCoord.y = c.y + translation.y;

        Region reportDataRegion = newReport.getData().regions().get(translatedCoord);

        if ((reportDataRegion != null) && !reportDataRegion.schemes().isEmpty() && !r.schemes().isEmpty()) {
          if (!equals(reportDataRegion.schemes(), r.schemes())) {
            mismatches++;
          }
        }
      }

      // decrease hit count of this translation for each mismatch
      DefaultTranslation t = astralTranslationMap.get(translation);
      t.setScore(t.getScore() - mismatches);
    }

  }

  /**
   * Compare two sets of schemes. Two sets are considered equal if the sets of
   * names of the schemes are equal.
   * 
   * @param schemes1
   * @param schemes2
   * @return
   */
  private boolean equals(Collection<Scheme> schemes1, Collection<Scheme> schemes2) {
    if (schemes1 == null)
      if (schemes2 == null)
        return true;
      else
        return false;

    if (schemes1.size() != schemes2.size())
      return false;

    Set<String> schemeNames1 = new HashSet<String>();
    Set<String> schemeNames2 = new HashSet<String>();

    for (Scheme s : schemes1) {
      schemeNames1.add(s.getName());
    }

    for (Scheme s : schemes2) {
      schemeNames2.add(s.getName());
    }

    return schemeNames1.containsAll(schemeNames2);
  }

  /**
   * Prepare curTempID-Value for merging. If reports are of the same age, keep
   * existing by setting the new one to default value. Otherwise set the
   * existing to default value.
   */
  private void setTempID(ReportCache newReport) {
    if ((globalData.getDate() != null) && (newReport.getData().getDate() != null) && (globalData.getDate().getDate() < newReport.getData().getDate().getDate())) {
      globalData.setCurTempID(-1);
    } else {
      newReport.getData().setCurTempID(-1);
    }
  }

  /**
   * prepare faction trustlevel for merging: - to be added CR is older or of
   * same age -> hold existing trust levels - to be added CR is newer and
   * contains trust level that were set by the user explicitly (or read from CR
   * what means the same) -> take the trust levels out of the new CR otherwise ->
   * hold existing trust levels This means: set those trust levels, that will
   * not be retained to default values
   */
  private void adjustTrustlevels(ReportCache newReport) {
    if ((globalData.getDate() != null) && (newReport.getData().getDate() != null) && (globalData.getDate().getDate() < newReport.getData().getDate().getDate()) && TrustLevels.containsTrustLevelsSetByUser(newReport.getData())) {
      // take the trust levels out of the to be added data
      // set those in the existing data to default-values
      for (Iterator<Faction> iterator = globalData.factions().values().iterator(); iterator.hasNext();) {
        Faction f = iterator.next();
        f.setTrustLevel(Faction.TL_DEFAULT);
        f.setTrustLevelSetByUser(false);
      }
    } else {
      // take the trust levels out of the existing data
      // set those in the to be added data to default-values
      for (Iterator<Faction> iterator = newReport.getData().factions().values().iterator(); iterator.hasNext();) {
        Faction f = iterator.next();
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
    if (newReport.getData() == null || !globalData.name.equalsIgnoreCase(newReport.getData().name)) {
      // no report loaded or
      // game types doesn't match. Make sure, it will not be tried again.

      if (ui != null) {
        ui.confirm(newReport.getFile().getName() + Resources.get("util.reportmerger.wronggametype"), newReport.getFile().getName());
      }
      if (newReport.getData() == null)
        log.warn("ReportMerger.mergeReport(): got empty data.");
      else
        log.warn("ReportMerger.mergeReport(): game types don't match.");

      newReport.setMerged(true);

      return false;
    }
    return true;
  }

  /**
   * Weiterer Versuch des Findens eines AR-RR Mappings Hierbei soll EINE AR
   * Region mit ausreichend Schemen reichen
   * 
   * @param data
   * @return
   */
  private CoordinateID getOneRegion_AR_RR_Translation(GameData data) {
    /**
     * Ansatz: Sind die Schemen günstig verteilt, reicht eine AR Region und ihre
     * Schemen zum Bestimmen des Mappings AR->RR Dazu wird versucht, die
     * RR-Region genau unter der AR-Region zu finden. Diese darf maximal 2
     * Regionen von allen Schemen entfernt sein. Die Entfernung dieser Region zu
     * benachbarten Regionen der Schemen, die NICHT selbst Schemen sind, muss
     * allerdings > 2 sein (da sonst auch diese Region in den Schemen enthalten
     * sein müsste) Die endgültieg Region unter der AR-Region kann Ozean sein
     * und muss daher nicht in den Schemen sichtbar sein. Daher wird zuerst ein
     * Pool von möglichen Regionen gebildet, indem alle Schemen, ihre Nachbarn
     * und wiederum deren Nachbarn erfasst werden. Dann werden nicht in Frage
     * kommende Region sukkzessive eliminiert
     * 
     * 
     */
    // Das Ergebnis des Vorganges...
    CoordinateID translation = null;

    // regionen durchlaufen und AR Regionen bearbeiten
    for (Region actAR_Region : data.regions().values()) {
      if (translation != null)
        break;
      if (actAR_Region.getCoordinate().z == 1) {
        // Es ist eine AR Region
        translation = this.getOneRegion_processARRegion(data, actAR_Region);
        // wenn ergebnis !=null, abbruch
        if (translation != null) {
          return translation;
        }
      }
    }

    return translation;
  }

  /**
   * Bearbeitet eine AR-Region
   * 
   * @param data
   * @param astralRegion
   * @return
   */
  private CoordinateID getOneRegion_processARRegion(GameData data, Region astralRegion) {
    CoordinateID translation = null;

    // wir brechen ab, wenn gar keine Schemen vorhanden sind...16 Felder Ozean!
    if (astralRegion.schemes() == null) {
      return null;
    }

    Collection<Region> possibleRR_Regions = new HashSet<Region>(0);
    // possibleRR_Regions sind erstmal alle Schemen
    for (Scheme actScheme : astralRegion.schemes()) {
      Region actSchemeRegion = data.getRegion(actScheme.getCoordinate());
      // fail safe...
      if (actSchemeRegion == null) {
        continue;
      }
      possibleRR_Regions.add(actSchemeRegion);
    }
    // sollte die Liste jetzt leer sein (unwahrscheinlich), brechen wir ab
    if (possibleRR_Regions.size() == 0) {
      // log.warn?
      return null;
    }
    // die schemen erfahren eine sonderbehandlung, diese extra listen
    Collection<Region> actSchemeRegions = new HashSet<Region>(possibleRR_Regions);
    // die possible Regions mit Nachbarn füllen, für den ungünstigsten
    // Fall sind 4 Läufe notwendig
    for (int i = 0; i < 4; i++) {
      possibleRR_Regions = this.getOneRegion_explodeRegionList(data, possibleRR_Regions);
    }

    // Ab jetzt versuchen, unmögliche Regionen zu entfernen...
    // erste bedingung: alle regionen, die sich auch nur von einer
    // schemenRegionen
    // weiter entfernt befinden als 2 Regionen können raus.
    possibleRR_Regions = this.getOneRegion_deleteIfDist(data, actSchemeRegions, possibleRR_Regions, 2, true);

    // zweite bedingung: Randregionen von schemen (nicht ozean-Regionen...), die
    // nicht selbst schemen sind, dürfen nicht weniger als 3
    // Regionen entfernt sein.
    // Dazu: Randregionen basteln
    Collection<Region> schemenRandRegionen = new HashSet<Region>(0);
    schemenRandRegionen = this.getOneRegion_explodeRegionList(data, actSchemeRegions);
    // schemen selbst abziehen
    schemenRandRegionen.removeAll(actSchemeRegions);
    // Ozeanfelder löschen
    schemenRandRegionen = this.getOneRegion_deleteOceans(schemenRandRegionen);
    // alle löschen, die weniger als 3 Regionen an den randregionen dranne sind
    possibleRR_Regions = this.getOneRegion_deleteIfDist(data, schemenRandRegionen, possibleRR_Regions, 3, false);
    // jetzt sollte im Idealfall nur noch eine Region vorhanden sein ;-))
    if (possibleRR_Regions.size() == 1) {
      // Treffer, wir können Translation bestimmen
      // Verständnisfrage: ist gesichert, dass sich das einzige
      // Element einer ArrayList immer auf Index=0 befindet?
      Region rrRegion = possibleRR_Regions.iterator().next();
      translation = new CoordinateID(rrRegion.getCoordinate().x - 4 * astralRegion.getCoordinate().x, rrRegion.getCoordinate().y - 4 * astralRegion.getCoordinate().y);
    }
    return translation;
  }

  /**
   * Falls <code>innerhalb == true</code>: Löscht die Regionen aus
   * regionList, welche von mindestens einer Region in <code>schemen</code>
   * einen Abstand größer <code>radius</code> haben.
   * 
   * Falls <code>innerhalb == false</code>: Löscht die Regionen aus
   * regionList, welche von mindestens einer Region in <code>schemen</code>
   * einen Abstand kleiner <code>radius</code> haben.
   * 
   * @param schemen
   * @param regionen
   * @param radius
   * @return
   */
  private Collection<Region> getOneRegion_deleteIfDist(GameData data, Collection<Region> schemen, Collection<Region> regionList, int radius, boolean innerhalb) {
    Collection<Region> regionsToDel = new ArrayList<Region>(0);
    for (Region actRegion : regionList) {
      // nur die betrachten, die nicht schon in del sind
      if (!regionsToDel.contains(actRegion)) {
        // Durch alle Schemen laufen und Abstand berechnen
        for (Region actSchemenRegion : schemen) {
          // Abstand berechnen
          int dist = Regions.getRegionDist(actRegion.getCoordinate(), actSchemenRegion.getCoordinate());

          if ((dist > radius && innerhalb) || (dist < radius && !innerhalb)) {
            // actRegion ist weiter/näher als radius von actSchemenregion
            // entfernt
            // muss gelöscht werden
            regionsToDel.add(actRegion);
            break;
          }
        }
      }
    }
    // Löschung durchführen
    Collection<Region> erg = new HashSet<Region>(0);
    erg.addAll(regionList);
    erg.removeAll(regionsToDel);
    return erg;
  }

  /**
   * Löscht die Regionen aus regionList, welche als Ozean deklariert sind.
   * 
   * @param regionen
   * @return
   */
  private Collection<Region> getOneRegion_deleteOceans(Collection<Region> regionList) {
    Collection<Region> result = new HashSet<Region>();

    for (Region actRegion : regionList) {
      if (!actRegion.getRegionType().isOcean())
        result.add(actRegion);
    }

    return result;
  }

  /**
   * Erweitert die Liste der Regionen um die Nachbarn der aktuellen Regionen
   * 
   * @param data
   * @param regionList
   * @return
   */
  private Collection<Region> getOneRegion_explodeRegionList(GameData data, Collection<Region> regionList) {
    // Liste verlängern nach durchlauf
    Set<Region> neighborhood = new HashSet<Region>();
    for (Region actRegion : regionList) {
      // liefert die IDs der Nachbarregionen
      Collection<CoordinateID> neighbors = actRegion.getNeighbours();
      for (CoordinateID newRegionID : neighbors) {
        Region newRegion = data.getRegion(newRegionID);
        // hinzufügen, wenn noch nicht vorhanden
        neighborhood.add(newRegion);
      }
    }
    return neighborhood;
  }

}
