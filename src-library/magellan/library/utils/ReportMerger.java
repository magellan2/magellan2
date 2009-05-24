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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import magellan.library.CoordinateID;
import magellan.library.EntityID;
import magellan.library.Faction;
import magellan.library.GameData;
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
   * every match increases the score in the translationList
   * by the default value
   */
//  private static final int Translationlist_Score_Default = 1;
  /**
   * RegionUID is stronger than other procedures, so we adjust
   * the scrore quite a bit more
   */
//  private static final int Translationlist_Score_RegionUID = 10;
  
  /**
   * Astral1 will deliver onyl one translation and not increasing the
   * scrore with matches...makes sense only, if other procedures fail
   */
//  private static final int Translationlist_Score_Astral1 = 1;
  
  /**
   * Astral2 will deliver onyl one translation and not increasing the
   * scrore with matches...makes sense only, if other procedures fail
   */
//  private static final int Translationlist_Score_Astral2 = 1;

/*  
  enum Type {
    NONE, DEFAULT, SAVED, MATCHED, ASTRAL2, ASTRAL1, USER, REGIONUID;
    
    public String toString(){
      return Resources.get("util.reportmerger.translations.type."+super.toString());
    }
  };
*/  

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
//    Map<Long, Region> regionUIDMap = null;

    boolean hasAstralRegions = false;

    // already merged with another report
    boolean merged = false;

    private Date date = null;

    private boolean mergeError = false;

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
      if (staticData != null) {
        return staticData;
      }

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
      regionUIDMap = new HashMap<Long, Region>();
      */

      for (Region region : data.regions().values()) {
/*        if ((region.getName() != null) && (region.getName().length() > 0)) {
          Collection<Region> regions = regionMap.get(region.getName());
          if (regions == null) {
            regions = new HashSet<Region>();
            regionMap.put(region.getName(), regions);
          }
          regions.add(region);
        }
        
        if (region.getUID()!=0){
          regionUIDMap.put(region.getUID(), region);
        }
*/        
        if (region.getCoordinate().z == 1) {
          hasAstralRegions = true;
/*
          for (Scheme scheme : region.schemes()) {
            Collection<Region> astralRegions = schemeMap.get(scheme.getName());
            if (astralRegions == null) {
              astralRegions = new HashSet<Region>();
              schemeMap.put(scheme.getName(), astralRegions);
            }
            astralRegions.add(region);
          }
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
     * @return <code>1</code> iff this reports round is greater than
     *         <code>otherReport</code>'s round.
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(ReportCache otherReport) {
      if (otherReport == null) {
        return 1;
      } else {
        return otherReport.getRound() > this.getRound() ? -1 : (otherReport.getRound() < this.getRound() ? 1 : 0);
      }
    }

    private int getRound() {
      if (date == null) {
        date = getData().getDate();
      }
      if (date == null) {
        return -1;
      } else {
        return date.getDate();
      }
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
     * @return The regions with the specified name
    public Collection<Region> getRegionsByName(String name) {
      init();
      Collection<Region> result = regionMap.get(name);
      if (result == null)
        return Collections.emptySet();
      else
        return result;
    }
    
    public Region getRegionByUID(long regionUID){
      init();
      return regionUIDMap.get(regionUID);
    }
*/
    
    
    
/*
     * @param name
     * @return The set of astral regions having a scheme named <code>name</code>
    public Collection<Region> getAstralRegionBySchemeName(String name) {
      init();
      Collection<Region> result = schemeMap.get(name);
      if (result == null)
        return Collections.emptySet();
      else
        return result;
    }
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
    return merge(new NullUserInterface(), true, false, false);
  }

  /**
   * Start merging
   * 
   * @param aUI
   *          A user interface for indicating progress, displaying messages etc.
   * @param async
   *          If <code>true</code> the merging will be started in a new
   *          thread.
   * @param sort 
   * @param interactive 
   * 
   */
  public GameData merge(UserInterface aUI, boolean sort, boolean interactive, boolean async) {
    ui = aUI;
    this.sort  = sort;
    this.interactive  = interactive;
    
    if (ui != null) {
      ui.setMaximum(reports.length * 4); // four steps per report
    }
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
      ui.setTitle(Resources.get("util.reportmerger.window.title"));
      ui.show();
      ui.setProgress(Resources.get("util.reportmerger.status.merge"), 0);
    }

    if (sort){
      sortReports();
    }
    
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
            if (r.isMerged()) {
              iMerged++;
            }
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
      ReportMerger.log.error(e);
      ui.showException("Exception while merging report", null, e);
    }

    if (globalData!=null && globalData.outOfMemory) {
      ui.confirm(Resources.get("client.msg.outofmemory.text"), Resources.get("client.msg.outofmemory.title"));
      ReportMerger.log.error(Resources.get("client.msg.outofmemory.text"));
    }
    if (!MemoryManagment.isFreeMemory(globalData.estimateSize())){
      ui.confirm(Resources.get("client.msg.lowmem.text"), Resources.get("client.msg.lowmem.title"));
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
    dataReport = new ReportCache(globalData);

    iProgress += 1;
    if (ui != null) {
      ui.setProgress(newReport.getFile().getName() + " - " + Resources.get("util.reportmerger.status.processing"), iProgress);
    }

    // return if game type is wrong
    if (!checkGameType(newReport)) {
      return true;
    }

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

    MapMergeEvaluator mhelp = dataReport.getData().getGameSpecificStuff().getMapMergeEvaluator();
    Collection<Score<CoordinateID>> translationList = mhelp.getDataMappings(dataReport.getData(), newReport.getData(), 0);
    
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

    if (bestTranslation != null) {
      /*************************************************************************
       * find best astral translation
       */
      Score<CoordinateID> bestAstralTranslation = null;
      if (!newReport.hasAstralRegions()) {
        bestAstralTranslation = new Score<CoordinateID>(new CoordinateID(0, 0, 1), -1);
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
        Collection<Score<CoordinateID>> astralTranslationList = mhelp.getDataMappings(dataReport.getData(), newReport.getData(), 1, otherLevels);
        
        Score<CoordinateID> savedAstralTranslation = findSavedMapping(dataReport, newReport, 1);

        if (interactive) {
          bestAstralTranslation = askTranslation(newReport, astralTranslationList, savedAstralTranslation, 1);
        } else {
          bestAstralTranslation = decideTranslation(newReport, astralTranslationList, savedAstralTranslation, 1);
        }
      }
      /***************************************************************************
       * store translations
       */
      if (bestAstralTranslation != null) {
        iProgress += 1;
        if (ui != null) {
          ui.setProgress(newReport.getFile().getName() + " - "
                         + Resources.get("util.reportmerger.status.translating"), iProgress);
        }

        ReportMerger.log.info("Using this (real) translation: " + bestTranslation.getKey());
        // store found translations
        EntityID newReportOwner = newReport.getData().getOwnerFaction();
        if (newReportOwner != null) {
          if (globalData.getCoordinateTranslation(newReportOwner, 0) != null
              && !globalData.getCoordinateTranslation(newReportOwner, 0)
                            .equals(bestTranslation.getKey())) {
            ReportMerger.log.warn("old translation " + globalData.getCoordinateTranslation(newReportOwner, 0)
                     + " inconsistent with new translation " + bestTranslation.getKey());
          }
          if (bestTranslation.getScore() >= 0) {
            CoordinateID correct = newReport.getData().getCoordinateTranslation(newReportOwner, 0);
            if (correct != null) {
              correct =
                        new CoordinateID(bestTranslation.getKey().x + correct.x,
                                         bestTranslation.getKey().y + correct.y, 0);
              globalData.setCoordinateTranslation(newReportOwner, correct);
            }
          }
        }

        if (newReport.hasAstralRegions() && dataReport.hasAstralRegions()) {
          ReportMerger.log.info("Using this astral translation: " + bestAstralTranslation.getKey());

          if (newReportOwner != null) {
            if (globalData.getCoordinateTranslation(newReportOwner, 1) != null
                && !globalData.getCoordinateTranslation(newReportOwner, 1)
                              .equals(bestAstralTranslation.getKey())) {
              ReportMerger.log.warn("old astral translation "
                       + globalData.getCoordinateTranslation(newReportOwner, 1)
                       + " inconsistent with new translation "
                       + bestAstralTranslation.getKey());
            }
            if (bestAstralTranslation.getScore() >= 0) {
              CoordinateID correct =
                                     newReport.getData()
                                              .getCoordinateTranslation(newReportOwner, 1);
              if (correct != null) {
                correct =
                          new CoordinateID(bestAstralTranslation.getKey().x + correct.x,
                                           bestAstralTranslation.getKey().y + correct.y, 1);
                globalData.setCoordinateTranslation(newReportOwner, correct);
              }
            }
          }
        }
        
        /***************************************************************************
         * translate new report
         */
        GameData clonedData = newReport.getData();
        if (bestTranslation.getKey().x != 0 || bestTranslation.getKey().y != 0) {
          try {
            clonedData = (GameData) clonedData.clone(bestTranslation.getKey());
            if (clonedData!=null && clonedData.outOfMemory) {
              ui.confirm(Resources.get("client.msg.outofmemory.text"), Resources.get("client.msg.outofmemory.title"));
              ReportMerger.log.error(Resources.get("client.msg.outofmemory.text"));
            }
            if (!MemoryManagment.isFreeMemory(clonedData.estimateSize())){
              ui.confirm(Resources.get("client.msg.lowmem.text"), Resources.get("client.msg.lowmem.title"));
            }
          } catch (CloneNotSupportedException e) {
            ReportMerger.log.error(e);
          }
        } else {
          ReportMerger.log.info("Level 0 : using untranslated new report - same origin");
        }
        if (bestAstralTranslation.getKey().x != 0 || bestAstralTranslation.getKey().y != 0) {
          try {
            clonedData = (GameData) clonedData.clone(bestAstralTranslation.getKey());
            if (clonedData!=null && clonedData.outOfMemory) {
              ui.confirm(Resources.get("client.msg.outofmemory.text"), Resources.get("client.msg.outofmemory.title"));
              ReportMerger.log.error(Resources.get("client.msg.outofmemory.text"));
            }
            if (!MemoryManagment.isFreeMemory(clonedData.estimateSize())){
              ui.confirm(Resources.get("client.msg.lowmem.text"), Resources.get("client.msg.lowmem.title"));
            }
          } catch (CloneNotSupportedException e) {
            ReportMerger.log.error(e);
            // TODO if cloning the gamedata fails but translation is necessary we must stop merging!
          }
        } else {
          ReportMerger.log.info("Astral level : using untranslated new report - same origin or no Astral Regions");
        }
        
        /***************************************************************************
         * Merge the reports, finally!
         */
        iProgress += 1;
        if (ui != null) {
          ui.setProgress(newReport.getFile().getName() + " - " + Resources.get("util.reportmerger.status.merging"), iProgress);
        }
        globalData = GameData.merge(globalData, clonedData);
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

  private Score<CoordinateID> findSavedMapping(ReportCache dataReport2, ReportCache newReport, int layer) {
    EntityID newReportOwner = newReport.getData().getOwnerFaction();
    // ask user if necessary
    if (newReportOwner==null && interactive){
      if (!newReport.getData().factions().isEmpty()){
        Faction firstFaction = newReport.getData().factions().values().iterator().next();
        Object result = ui.input(Resources.getFormatted("util.reportmerger.msg.inputowner.msg", newReport.getFile().getName()), Resources.get("util.reportmerger.msg.inputowner.title"), newReport.getData().factions().values().toArray(), firstFaction);
        if (result!=null && result instanceof Faction) {
          newReport.getData().setOwnerFaction((EntityID) ((Faction)result).getID());
        }
      }
    }

    CoordinateID translation = SavedTranslationsMapping.getSingleton().getMapping(dataReport2.getData(), newReport.getData(), layer);
    if (translation != null) {
      return new Score<CoordinateID>(translation, 0);
    } return null;
  }

  /**
   * Chooses a translation among the translations in translationList and
   * savedTranslation. If no translation is acceptable, a default translation
   * (0,0, layer) is chosen.
   * 
   * @param newReport The report to be merged
   * @param translationList 
   * @param savedTranslation
   * @param layer
   * @return The best translation, never <code>null</code>
   */
  private Score<CoordinateID> decideTranslation(ReportCache newReport, Collection<Score<CoordinateID>> translationList, Score<CoordinateID> savedTranslation, int layer) {
    Score<CoordinateID> bestTranslation = new Score<CoordinateID>(new CoordinateID(0, 0, layer), -1);

    if (translationList!=null && translationList.size() > 0) {
      bestTranslation = Collections.max(translationList);

      ReportMerger.log.info("Found " + translationList.size() + " translations in layer " + layer + " for " + newReport.getFile().getName() + " (best(maxScore):" + bestTranslation.toString()+")");
      
      // Fiete: just see the other translations...
      /*
      ReportMerger.log.info("DEBUG: all found translations:");
      
      for (Score<CoordinateID>myScore : translationList){
        ReportMerger.log.info(myScore.toString());
      }
      **/
      
    } else {
      ReportMerger.log.info("No translation in layer "+ layer+" found for " + newReport.getFile().getName());
    }

    // choose the saved translation only if no good translation has been found 
    if (savedTranslation != null) {
      if (!bestTranslation.getKey().equals(savedTranslation.getKey())) {
        ReportMerger.log.info("Saved translation in layer "+ layer+" " + savedTranslation.getKey() + " != " + bestTranslation.getKey() + " best translation found.");
        if (bestTranslation.getScore() > 0) {
          ReportMerger.log.info("Preferring computed translation");
        }
      }
      if (bestTranslation.getScore() < 0) {
        ReportMerger.log.info("Using saved translation in layer "+layer);
        bestTranslation = savedTranslation;
      }
    } else {
      ReportMerger.log.info("no known translation (no translation saved in CR)");
    }
    if (bestTranslation.getScore()<0) {
      ReportMerger.log.warn("No good translation found in layer "+layer);
    }

    
    return bestTranslation;
  }

  /**
   * Chooses a translation among the translations in translationList and
   * savedTranslation. If no translation is acceptable the user is asked for input.
   * Returns <code>null</code> if user aborts.
   * 
   * @param newReport The report to be merged
   * @param translationList  never <code>null</code>
   * @param savedTranslation may be <code>null</code>
   * @param layer
   * @return The best translation, <code>null</code> if user aborts
   */
  private Score<CoordinateID> askTranslation(ReportCache newReport, Collection<Score<CoordinateID>> translationList, Score<CoordinateID> savedTranslation, int layer) {
    Score<CoordinateID> bestTranslation = decideTranslation(newReport, translationList, savedTranslation, layer);
    
    // sanity check: translation changed since last time?
    boolean changed = false;
    if (savedTranslation != null && !bestTranslation.getKey().equals(savedTranslation.getKey())) {
      changed = true;
    }
    // sanity check: more than one good translation?
    Set<CoordinateID> distinct = new HashSet<CoordinateID>();
    if (translationList!=null) {
      for (Score<CoordinateID> t : translationList){
        if (t.getScore()>=0) {
          distinct.add(t.getKey());
        }
      }
    }
    if (savedTranslation!=null) {
      distinct.add(savedTranslation.getKey());
    }
    
    if (!newReport.isMergeError() && !dataReport.isMergeError() && bestTranslation.getScore() >= 0 && !changed && distinct.size()==1) {
      // all seems well, return
      return bestTranslation;
    }else{
      // ask user what to do
      String message = Resources.getFormatted("util.reportmerger.msg.method", newReport.getFile().getName(), layer) 
      + (changed?Resources.get("util.reportmerger.msg.changed"):"");
      
      String bestMethod = Resources.getFormatted("util.reportmerger.msg.method.best", bestTranslation);
      String chooseMethod = Resources.get("util.reportmerger.msg.method.choose");
      String inputMethod = Resources.get("util.reportmerger.msg.method.input");
      String skipMethod = Resources.get("util.reportmerger.msg.method.skip");
      ArrayList<Object> choices = new ArrayList<Object>(4+translationList.size());
      choices.add(bestMethod);
//      choices.add(chooseMethod);
      choices.add(inputMethod);
      choices.add(skipMethod);
      
      choices.addAll(translationList);
      
      Object choice = ui.input(message, Resources.get("util.reportmerger.msg.method.title"), choices.toArray(), bestMethod);
      if (choice==null || choice.equals(skipMethod)) {
        return null;
      }
      
      if (choice.equals(bestMethod)){
        // try anyway
        return bestTranslation;
      }else if (choice.equals(chooseMethod)){
        // choose among found translations, deprecated
        Collection<Score<CoordinateID>> tChoices = new ArrayList<Score<CoordinateID>>(translationList);
        if (savedTranslation!=null) {
          tChoices.add(savedTranslation);
        }
        tChoices.add(new Score<CoordinateID>(new CoordinateID(0,0,layer), -1));
        
        Score help = (Score) ui.input(Resources.getFormatted("util.reportmerger.msg.usertranslation.choose", newReport.getFile().getName(), layer), 
            Resources.get("util.reportmerger.msg.usertranslation.title"), tChoices.toArray(), bestTranslation);
        Score<CoordinateID> chosenTranslation = null;
        // workaround since we cannot cast Object to Score<CoordinateID>
        for (Score<CoordinateID> s : tChoices) {
          if (s.equals(help)) {
            chosenTranslation = s;
            break;
          }
        }
        if (chosenTranslation!=null){
          ReportMerger.log.info("user choice: "+chosenTranslation);
          bestTranslation=chosenTranslation;
        }else{
          ReportMerger.log.info("user abort");
          bestTranslation = inputMapping(newReport, layer);
        }
      }else if (choice.equals(inputMethod)){
        bestTranslation = inputMapping(newReport, layer);
      }else if (choice instanceof Score){
        // workaround since we cannot cast Object to Score<CoordinateID>
        for (Score<CoordinateID> s : translationList) {
          if (s.equals(choice)) {
            bestTranslation = s; 
            break;
          }
        }
      }else{ 
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
      String message = Resources.getFormatted("util.reportmerger.msg.usertranslation.x", newReport.getFile().getName(), layer);
      Object xS = ui.input(message, Resources.get("util.reportmerger.msg.usertranslation.title"), null, "0");
      if (xS != null) {
        message = Resources.getFormatted("util.reportmerger.msg.usertranslation.y", newReport.getFile().getName(), layer);
        Object yS = ui.input(message, Resources.get("util.reportmerger.msg.usertranslation.title"), null, "0");
        if (yS != null) {
          cancelled = false;
          try {
            CoordinateID trans = new CoordinateID(Integer.parseInt((String) xS), Integer.parseInt((String) yS), layer);
            resultTranslation = new Score<CoordinateID>(trans, 1, "user");
            ReportMerger.log.debug("using user translation: " + resultTranslation.getKey()+" in layer "+layer);
          } catch (NumberFormatException e) {
            badInput = true;
          }
        }
      }
    } while (badInput && !cancelled);
    if (cancelled){
      ReportMerger.log.info("user input cancelled");
      return null;
    }
    return resultTranslation;
  }

  /**
   * Compare two sets of schemes. Two sets are considered equal if the sets of
   * names of the schemes are equal.
   * 
   * @param schemes1
   * @param schemes2
   * @return
   */
  
//  private boolean equals(Collection<Scheme> schemes1, Collection<Scheme> schemes2) {
//    if (schemes1 == null) {
//      if (schemes2 == null) {
//        return true;
//      } else {
//        return false;
//      }
//    }
//
//    if (schemes1.size() != schemes2.size()) {
//      return false;
//    }
//
//    Set<String> schemeNames1 = new HashSet<String>();
//    Set<String> schemeNames2 = new HashSet<String>();
//
//    for (Scheme s : schemes1) {
//      schemeNames1.add(s.getName());
//    }
//
//    for (Scheme s : schemes2) {
//      schemeNames2.add(s.getName());
//    }
//
//    return schemeNames1.containsAll(schemeNames2);
//  }

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
    if (newReport.getData() == null || !globalData.getGameName().equalsIgnoreCase(newReport.getData().getGameName())) {
      // no report loaded or
      // game types doesn't match. Make sure, it will not be tried again.

      if (ui != null) {
        // Fiete 20090105: displayed a yes/no box with error message - bug #348
        // ui.confirm(newReport.getFile().getName() + ": " + Resources.get("util.reportmerger.msg.wronggametype"), newReport.getFile().getName());
        ui.showMessageDialog(newReport.getFile().getName() + ": " + Resources.get("util.reportmerger.msg.wronggametype"));
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

  /**
   * Weiterer Versuch des Findens eines AR-RR Mappings Hierbei soll EINE AR
   * Region mit ausreichend Schemen reichen
   * 
   * @param data
   * @return
   */
/*  
  private CoordinateID getOneRegion_AR_RR_Translation(ReportCache data) {
    /**
     * Ansatz: Sind die Schemen g�nstig verteilt, reicht eine AR Region und ihre
     * Schemen zum Bestimmen des Mappings AR->RR Dazu wird versucht, die
     * RR-Region genau unter der AR-Region zu finden. Diese darf maximal 2
     * Regionen von allen Schemen entfernt sein. Die Entfernung dieser Region zu
     * benachbarten Regionen der Schemen, die NICHT selbst Schemen sind, muss
     * allerdings > 2 sein (da sonst auch diese Region in den Schemen enthalten
     * sein m�sste) Die endg�ltieg Region unter der AR-Region kann Ozean sein
     * und muss daher nicht in den Schemen sichtbar sein. Daher wird zuerst ein
     * Pool von m�glichen Regionen gebildet, indem alle Schemen, ihre Nachbarn
     * und wiederum deren Nachbarn erfasst werden. Dann werden nicht in Frage
     * kommende Region sukkzessive eliminiert
     * 
     * 
     */
    // Das Ergebnis des Vorganges...
/*    CoordinateID result = null;

    // regionen durchlaufen und AR Regionen bearbeiten
    for (Region actAR_Region : data.getData().regions().values()) {
      if (actAR_Region.getCoordinate().z == 1) {
        // Es ist eine AR Region
        CoordinateID translation = this.getOneRegion_processARRegion(data.getData(), actAR_Region);

        if (translation != null) {
          if (result != null && !result.equals(translation)){
            log.warn("two mappings for "+actAR_Region+": " + result + "; " + translation);
            data.setMergeError(true);
          }
          result = translation;
        }
      }
    }

    return result;
  }
*/
  /**
   * Bearbeitet eine AR-Region
   * 
   * @param data
   * @param astralRegion
   * @return
   */
/*
  private CoordinateID getOneRegion_processARRegion(GameData data, Region astralRegion) {
    CoordinateID translation = null;

    // wir brechen ab, wenn gar keine Schemen vorhanden sind...16 Felder Ozean!
    if (astralRegion.schemes() == null) {
      return null;
    }

    Collection<CoordinateID> possibleRR_Regions = new HashSet<CoordinateID>(0);
    // possibleRR_Regions sind erstmal alle Schemen
    for (Scheme actScheme : astralRegion.schemes()) {
      Region actSchemeRegion = data.getRegion(actScheme.getCoordinate());
      // fail safe...
      if (actSchemeRegion == null) {
        continue;
      }
      possibleRR_Regions.add(actSchemeRegion.getCoordinate());
    }
    // sollte die Liste jetzt leer sein (unwahrscheinlich), brechen wir ab
    if (possibleRR_Regions.size() == 0) {
      // log.warn?
      return null;
    }
    // die schemen erfahren eine sonderbehandlung, diese extra listen
    Collection<CoordinateID> actSchemeRegions = new HashSet<CoordinateID>(possibleRR_Regions);
    
    // die possible Regions mit Nachbarn f�llen, f�r den ung�nstigsten
    // Fall ist ein radius von 3 notwendig
    // nicht vorhandene Regionen werden trotzdem aufgef�hrt
    
    possibleRR_Regions = this.getOneRegion_explodeRegionList(data, possibleRR_Regions,false,3);
        
    // Ab jetzt versuchen, unm�gliche Regionen zu entfernen...
    // erste bedingung: alle regionen, die sich auch nur von einer
    // schemenRegionen
    // weiter entfernt befinden als 2 Regionen k�nnen raus.
    possibleRR_Regions = this.getOneRegion_deleteIfDist(data, actSchemeRegions, possibleRR_Regions, 2, true);

    // zweite bedingung: Randregionen von schemen (nicht ozean-Regionen...), die
    // nicht selbst schemen sind, d�rfen nicht weniger als 3
    // Regionen entfernt sein.
    // Dazu: Randregionen basteln
    Collection<CoordinateID> schemenRandRegionIDs = new HashSet<CoordinateID>(0);
    schemenRandRegionIDs = this.getOneRegion_explodeRegionList(data, actSchemeRegions,true,5);
 
    // schemen selbst abziehen
    schemenRandRegionIDs.removeAll(actSchemeRegions);
    // Ozeanfelder l�schen
    schemenRandRegionIDs = this.getOneRegion_deleteOceans(data,schemenRandRegionIDs);
    // alle l�schen, die weniger als 3 Regionen an den randregionen dranne sind
    possibleRR_Regions = this.getOneRegion_deleteIfDist(data, schemenRandRegionIDs, possibleRR_Regions, 3, false);
    // jetzt sollte im Idealfall nur noch eine Region vorhanden sein ;-))
    if (possibleRR_Regions.size() == 1) {
      // Treffer, wir k�nnen Translation bestimmen
      // Verst�ndnisfrage: ist gesichert, dass sich das einzige
      // Element einer ArrayList immer auf Index=0 befindet?
      // Region rrRegion = data.getRegion(possibleRR_Regions.iterator().next());
      CoordinateID rrRegionID = possibleRR_Regions.iterator().next();
      // translation = new CoordinateID(rrRegion.getCoordinate().x - 4 * astralRegion.getCoordinate().x, rrRegion.getCoordinate().y - 4 * astralRegion.getCoordinate().y);
      translation = new CoordinateID(rrRegionID.x - 4 * astralRegion.getCoordinate().x, rrRegionID.y - 4 * astralRegion.getCoordinate().y);
    }
    return translation;
  }
*/
  /**
   * Falls <code>innerhalb == true</code>: L�scht die Regionen aus
   * regionList, welche von mindestens einer Region in <code>schemen</code>
   * einen Abstand gr��er <code>radius</code> haben.
   * 
   * Falls <code>innerhalb == false</code>: L�scht die Regionen aus
   * regionList, welche von mindestens einer Region in <code>schemen</code>
   * einen Abstand kleiner <code>radius</code> haben.
   * 
   * @param schemen
   * @param regionen
   * @param radius
   * @return
   */
/*  
  private Collection<CoordinateID> getOneRegion_deleteIfDist(GameData data, Collection<CoordinateID> schemenIDs, Collection<CoordinateID> regionIDList, int radius, boolean innerhalb) {
    Collection<CoordinateID> regionsIDsToDel = new ArrayList<CoordinateID>(0);
    for (CoordinateID actRegionID : regionIDList) {
      // nur die betrachten, die nicht schon in del sind
      if (!regionsIDsToDel.contains(actRegionID)) {
        // Durch alle Schemen laufen und Abstand berechnen
        for (CoordinateID actSchemenID : schemenIDs) {
          // Abstand berechnen
          int dist = Regions.getRegionDist(actRegionID, actSchemenID);

          if ((dist > radius && innerhalb) || (dist < radius && !innerhalb)) {
            // actRegion ist weiter/n�her als radius von actSchemenregion
            // entfernt
            // muss gel�scht werden
            regionsIDsToDel.add(actRegionID);
            break;
          }
        }
      }
    }
    // L�schung durchf�hren
    Collection<CoordinateID> erg = new HashSet<CoordinateID>(0);
    erg.addAll(regionIDList);
    erg.removeAll(regionsIDsToDel);
    return erg;
  }
*/
  /**
   * L�scht die Regionen aus regionList, welche als Ozean deklariert sind.
   * 
   * @param regionen
   * @return
   */
/*  
  private Collection<CoordinateID> getOneRegion_deleteOceans(GameData data,Collection<CoordinateID> regionIDList) {
    Collection<CoordinateID> result = new HashSet<CoordinateID>();

    for (CoordinateID actRegionID : regionIDList) {
      Region actRegion = data.getRegion(actRegionID);
      if (actRegion!=null && actRegion.getRegionType().isAstralVisible())
        result.add(actRegionID);
    }
    return result;
  }
*/
  /**
   * Erweitert die Liste der Regionen um die Nachbarn der aktuellen Regionen
   * 
   * @param data
   * @param regionList
   * @return
   */
/*  
  private Collection<CoordinateID> getOneRegion_explodeRegionList(GameData data, Collection<CoordinateID> regionIDList,boolean GameDataRegionsOnly) {
    // Liste verl�ngern nach durchlauf
    return this.getOneRegion_explodeRegionList(data, regionIDList, GameDataRegionsOnly, 1);
  }
*/
  /**
   * Erweitert die Liste der Regionen um die Nachbarn der aktuellen Regionen
   * 
   * @param data
   * @param regionList
   * @return
   */
/*  
  private Collection<CoordinateID> getOneRegion_explodeRegionList(GameData data, Collection<CoordinateID> regionIDList,boolean GameDataRegionsOnly,int radius) {
    // Liste verl�ngern nach durchlauf
    Set<CoordinateID> neighborhood = new HashSet<CoordinateID>();
    for (CoordinateID actRegionID : regionIDList) {
      // liefert die IDs der Nachbarregionen
      // Collection<CoordinateID> neighbors = actRegion.getNeighbours();
      Collection<CoordinateID> neighbors = this.getOneRegion_getAllNeighbourIDs(actRegionID, radius);
      for (CoordinateID newRegionID : neighbors) {
        // hinzuf�gen, wenn noch nicht vorhanden
        if (!GameDataRegionsOnly || data.getRegion(newRegionID)!=null)
        neighborhood.add(newRegionID);
      }
    }
    return neighborhood;
  }
*/  
  
  /**
   * Retrieve the regions within radius around region center.
   * Creates new Regions if not already present
   * @param regions a map containing the existing regions.
   * @param center the region the neighbours of which are retrieved.
   * @param radius the maximum distance between center and any region to be regarded as a
   *      neighbour within radius.
   * 
   *
   * @return a map with all neighbours that were found, including     region center. The keys are
   *       instances of class Coordinate,     values are objects of class Region.
   */
/*  
  private ArrayList<CoordinateID> getOneRegion_getAllNeighbourIDs(CoordinateID center, int radius) {
    ArrayList<CoordinateID> neighbours = new ArrayList<CoordinateID>();
    

    for(int dx = -radius; dx <= radius; dx++) {
      for(int dy = (-radius + Math.abs(dx)) - ((dx > 0) ? dx : 0);
          dy <= ((radius - Math.abs(dx)) - ((dx < 0) ? dx : 0)); dy++) {
        CoordinateID c = new CoordinateID(0, 0, center.z);
        c.x = center.x + dx;
        c.y = center.y + dy;
        neighbours.add(c);
      }
    }
    return neighbours;
  }
*/  
  
}