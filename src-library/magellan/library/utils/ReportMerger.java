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

import java.awt.event.WindowEvent;
import java.io.File;
import java.lang.ref.SoftReference;
import java.util.Arrays;

import javax.swing.JOptionPane;

import magellan.library.CoordinateID;
import magellan.library.GameData;
import magellan.library.GameDataMerger;
import magellan.library.Region;
import magellan.library.rules.Date;
import magellan.library.tasks.GameDataInspector;
import magellan.library.tasks.Problem;
import magellan.library.utils.logging.Logger;
import magellan.library.utils.transformation.ReportTransformer;
import magellan.library.utils.transformation.TwoLevelTransformer;

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
      ui.setProgress(file.getName() + " - " + Resources.get("util.reportmerger.status.loading"), ui
          .getProgress());
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
     * Ensures partial order by increasing date. This violates the general contract of equals() and
     * hashCode()!
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
      ui.setMaximum(reports.length * 3); // three steps per report
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

  boolean error = false;
  Object lock = new Object();

  /**
   * Do the merging. This method can be executed in its own thread.
   */
  private synchronized GameData mergeThread() {
    error = false;

    if (ui != null) {
      ui.setTitle(Resources.get("util.reportmerger.window.title"));
      ui.setProgress(Resources.get("util.reportmerger.status.merge"), 0);
      ui.addClosingListener(new UserInterface.ClosingListener() {
        public boolean close(WindowEvent e) {
          error = true;
          ui.setProgress(Resources.get("util.reportmerger.status.aborting"), ui.getProgress());
          ui.setMaximum(-1);
          return false;
        }
      });
    }

    if (sort) {
      sortReports();
    }

    /**
     * We merge reports one by one. If merging of a report fails, we try to merge all other reports.
     * We only break, if all reports, subsequently, have failed to merge.
     */
    try {
      boolean cancel = false;
      int iFailedConnectivity = reports.length;
      do {
        for (int currentReport = 0; !error && !cancel && currentReport < reports.length; currentReport++) {
          if (!reports[currentReport].isMerged()) {
            if (mergeReport(reports[currentReport])) {
              // a report has been successfully merged
              // previously failed reports could now have a connection
              iFailedConnectivity--;
            }
          }
        }

        // prepare failure message
        if (!error && !cancel && iFailedConnectivity > 0) {
          cancel = askToCancel();
        }
      } while (iFailedConnectivity > 0 && !error && !cancel);
    } catch (Exception e) {
      ReportMerger.log.error(e);
      ui.showException("Exception while merging report", null, e);
      error = true;
    }

    // inform user about memory problems
    if (globalData.isOutOfMemory()) {
      ui.showDialog(Resources.get("client.msg.outofmemory.title"), Resources
          .get("client.msg.outofmemory.text"), JOptionPane.WARNING_MESSAGE,
          JOptionPane.DEFAULT_OPTION);
      ReportMerger.log.error(Resources.get("client.msg.outofmemory.text"));
      error = true;
    }
    if (!MemoryManagment.isFreeMemory(globalData.estimateSize())) {
      ui.showDialog(Resources.get("client.msg.outofmemory.title"), Resources
          .get("client.msg.outofmemory.text"), JOptionPane.WARNING_MESSAGE,
          JOptionPane.DEFAULT_OPTION);
      log.warn(Resources.get("client.msg.lowmem.text"));
    }
    int bE = 0, rE = 0, ruE = 0, sE = 0, uE = 0, mE = 0;
    for (Problem p : globalData.getErrors()) {
      if (p.getType() == GameDataInspector.GameDataProblemTypes.DUPLICATEBUILDINGID.type) {
        bE++;
      }
      if (p.getType() == GameDataInspector.GameDataProblemTypes.DUPLICATEREGIONID.type) {
        rE++;
        Region c = (Region) p.getObject();
        log.info("Problem: Duplicate Region ID: '" + p.getRegion().getName() + "' ("
            + p.getRegion().getID() + ") <> '" + c.getName() + "' (" + c.getID() + ")");
      }
      if (p.getType() == GameDataInspector.GameDataProblemTypes.DUPLICATEREGIONUID.type) {
        ruE++;
        Region c = (Region) p.getObject();
        log.info("Problem: Duplicate Region UID: '" + p.getRegion().getName() + "' ("
            + p.getRegion().getID() + ") <> '" + c.getName() + "' (" + c.getID() + ")");
      }
      if (p.getType() == GameDataInspector.GameDataProblemTypes.DUPLICATESHIPID.type) {
        sE++;
      }
      if (p.getType() == GameDataInspector.GameDataProblemTypes.DUPLICATEUNITID.type) {
        uE++;
      }
      if (p.getType() == GameDataInspector.GameDataProblemTypes.OUTOFMEMORY.type) {
        mE++;
      }
    }
    if (bE > 0 || rE > 0 || ruE > 0 || sE > 0 || uE > 0) {
      log.error("report with errors: " + (rE + ruE) + " " + uE + " " + bE + " " + sE);
      ui.showDialog(Resources.get("client.msg.reporterrors.title"),
          Resources.get("client.msg.reporterrors.text", globalData.getFileType().getName(), rE
              + ruE, uE, bE, sE), JOptionPane.WARNING_MESSAGE, JOptionPane.DEFAULT_OPTION);
    }

    if (ui != null) {
      ui.ready();
    }

    if (!error && assignData != null) {
      assignData.assign(globalData);
    }

    return globalData;
  }

  private boolean askToCancel() {
    StringBuilder strMessage =
        new StringBuilder(Resources.get("util.reportmerger.msg.noconnection.text.1")).append("\n");

    int notmerged = 0;
    String lastName = null;
    int listed = 0;
    for (ReportCache report : reports) {
      if (!report.isMerged()) {
        if (++notmerged > 4) {
          if (notmerged == 5) {
            strMessage.append(", ...");
          }
          lastName = report.getFile().getName();
        } else {
          if (listed > 0) {
            strMessage.append(", ");
          }
          strMessage.append(report.getFile().getName());
          lastName = null;
        }
        listed++;
      }
    }
    if (lastName != null) {
      strMessage.append(", ").append(lastName);
    }
    strMessage.append(".\n");
    if (strMessage.length() > 300) {
      strMessage =
          new StringBuilder(Resources.get("util.reportmerger.msg.noconnection.text.1"))
              .append("\n");
      strMessage.append(Resources.get("util.reportmerger.msg.noconnection.text.3", listed));
    }
    strMessage.append(Resources.get("util.reportmerger.msg.noconnection.text.4"));

    // ask to merge anyway
    return !(ui != null && !ui.confirm(strMessage.toString(), Resources
        .get("util.reportmerger.msg.confirmmerge.title")));
  }

  /**
   * Sort the reports according to their Comparator which currently means, according to their age.
   */
  private void sortReports() {
    ui.setProgress(Resources.get("util.reportmerger.status.sorting"), iProgress);
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

    ++iProgress;
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
    ++iProgress;
    if (ui != null) {
      ui.setProgress(newReport.getFile().getName() + " - "
          + Resources.get("util.reportmerger.status.connecting"), iProgress);
    }

    ReportTransformer transformers[] = getTransformers(newReport);
    if (transformers != null) {
      /***************************************************************************
       * Merge the reports, finally!
       */
      ++iProgress;
      if (ui != null) {
        ui.setProgress(newReport.getFile().getName() + " - "
            + Resources.get("util.reportmerger.status.merging"), iProgress);
      }
      // old: globalData = GameDataMerger.merge(globalData, clonedData);
      globalData =
          GameDataMerger.merge(globalData, newReport.getData(), transformers[0], transformers[1]);

      for (ReportTransformer t : transformers) {
        t.storeTranslations(globalData, newReport.getData());
      }

      newReport.setMerged(true);
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

  private ReportTransformer[] getTransformers(ReportCache newReport) {
    return dataReport.getData().getGameSpecificStuff().getTransformers(dataReport.getData(),
        newReport.getData(), ui, interactive);
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
        if (clonedData.isOutOfMemory()) {
          ui.showDialog(Resources.get("client.msg.outofmemory.title"), Resources
              .get("client.msg.outofmemory.text"), JOptionPane.WARNING_MESSAGE,
              JOptionPane.DEFAULT_OPTION);
          // ui.confirm(Resources.get("client.msg.outofmemory.text"), Resources
          // .get("client.msg.outofmemory.title"));
          ReportMerger.log.error(Resources.get("client.msg.outofmemory.text"));
        }
      } catch (CloneNotSupportedException e) {
        ReportMerger.log.error(e);
        throw new RuntimeException("problems while cloning", e);
      }
      if (!MemoryManagment.isFreeMemory(clonedData.estimateSize())) {
        ui.showDialog(Resources.get("client.msg.outofmemory.title"), Resources
            .get("client.msg.outofmemory.text"), JOptionPane.WARNING_MESSAGE,
            JOptionPane.DEFAULT_OPTION);
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
   * Check if newReport's game type matches that of global data.
   * 
   * @param newReport
   */
  private boolean checkGameType(ReportCache newReport) {
    boolean okay = true;
    if (newReport.getData() == null) {
      okay = false;
      ReportMerger.log.warn("ReportMerger.mergeReport(): got empty data.");
      if (ui != null) {
        ui.showMessageDialog(newReport.getFile().getName() + ": "
            + Resources.get("util.reportmerger.msg.emptydata"));
      }
    } else if (!globalData.getGameName().equalsIgnoreCase(newReport.getData().getGameName())) {
      ReportMerger.log.warn("ReportMerger.mergeReport(): game types don't match.");
      okay = false;
      if (ui != null) {
        // Fiete 20090105: displayed a yes/no box with error message - bug #348
        // ui.confirm(newReport.getFile().getName() + ": " +
        // Resources.get("util.reportmerger.msg.wronggametype"), newReport.getFile().getName());
        // FIXME (stm) this can be a redundant message
        okay =
            ui.confirm(newReport.getFile().getName() + ": "
                + Resources.get("util.reportmerger.msg.wronggametype"), "");
      }
    }
    if (!okay) {
      newReport.setMerged(true);
    }

    return okay;
  }
}
