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

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import magellan.library.CoordinateID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.Scheme;
import magellan.library.StringID;
import magellan.library.rules.RegionType;
import magellan.library.utils.logging.Logger;


/**
 * Helper class.
 */
public class ReportMerger extends Object {
	private static final Logger log = Logger.getInstance(ReportMerger.class);

	/**
	 * DOCUMENT-ME
	 *
	 * @author $Author: $
	 * @version $Revision: 344 $
	 */
	public interface Loader {
		/**
		 * DOCUMENT-ME
		 *
		 * 
		 *
		 * 
		 */
		public GameData load(File file);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * @author $Author: $
	 * @version $Revision: 344 $
	 */
	public interface AssignData {
		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public void assign(GameData _data);
	}

	private class Report {
		// data set
		GameData data = null;

		// load data from
		File file = null;

		// maps region names to region coordinate
		Map<String,Region> regionMap = null;

		// maps schemes (region names) to a Collection of astral regions
		// which contain that scheme
		Map<String,Collection<Region>> schemeMap = null;

		// already merged with another report
		boolean merged = false;
	}

	// merged data set
	GameData data = null;

	// reports to merge
	Report reports[] = null;

	// loader interface
	Loader loader = null;

	// data assign interface
	AssignData assignData = null;

	public interface UserInterface {
		public void ready();
		public void show();
		public void setProgress(String strMessage, int iProgress);
		public boolean confirm(String strMessage, String strTitle);
	}

	public class NullUserInterface implements UserInterface {
		public void ready() {}
		public void show() {}
		public void setProgress(String strMessage, int iProgress) {}
		public boolean confirm(String strMessage, String strTitle) {
			return true;
		}
	}

	public class SwingUserInterface implements UserInterface {
		// user interface
		ProgressDlg dlg = null;
    boolean showing;

		public SwingUserInterface(JFrame parent) {
			init(parent);
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		private void init(JFrame parent) {
      log.info("init");
			dlg = new ProgressDlg(parent, true);
			dlg.labelText.setText(Resources.get("util.reportmerger.status.merge"));
			dlg.progressBar.setMinimum(0);
			dlg.progressBar.setMaximum(reports.length * 4);
		}

		/**
		 * Shows the dialog.
		 */
		public void show() {
      showing=true;
      SwingUtilities.invokeLater((new Runnable() {public void run() {
				SwingUserInterface.this.dlg.setVisible(true);
			}})); 
		}

		private class Confirm implements Runnable {
			String strMessage;
			String strTitle;
			boolean bResult = false;

			/**
			 * DOCUMENT-ME
			 */
			public void run() {
				if(JOptionPane.showConfirmDialog(dlg, strMessage,
            Resources.get("util.reportmerger.msg.confirmmerge.title"),
													 JOptionPane.YES_NO_OPTION,
													 JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
					bResult = true;
				} else {
					bResult = false;
				}
			}
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 * 
		 *
		 * 
		 */
		public boolean confirm(String strMessage, String strTitle) {
			Confirm conf = new Confirm();
			conf.strMessage = strMessage;
			conf.strTitle = strTitle;

			try {
				SwingUtilities.invokeAndWait(conf);
			} catch(Exception e) {
				log.error(e);
			}

			return conf.bResult;
		}

		private class Progress implements Runnable {
			String strMessage;
			int iProgress;

			/**
			 * DOCUMENT-ME
			 */
			public void run() {
				dlg.labelText.setText(strMessage);
				dlg.progressBar.setValue(iProgress);
			}
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 * 
		 */
		public void setProgress(String strMessage, int iProgress) {
			Progress progress = new Progress();
			progress.strMessage = strMessage;
			progress.iProgress = iProgress;

			SwingUtilities.invokeLater(progress);
		}

		/**
		 * Notifies the userface that the task is done. Destroys progress dialog.
		 */
		public void ready() {
      if (showing) {
        // wait for dialog to come up in the first place
        while (!SwingUserInterface.this.dlg.isShowing()) {
          log.debug("ready " + SwingUserInterface.this.dlg.isShowing());
          try {
            Thread.sleep(100);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
      showing = false;
      log.debug("ready " + SwingUserInterface.this.dlg.isShowing());
      SwingUserInterface.this.dlg.setVisible(false);
      SwingUserInterface.this.dlg.dispose();
    }
	}

	UserInterface ui;
	int iProgress;

	/**
	 * Creates new ReportMerger
	 *
	 * 
	 * 
	 * 
	 * 
	 */
	public ReportMerger(GameData _data, File files[], Loader _loader, AssignData _assignData) {
		data = _data;
		data.removeTheVoid(); // removes void regions
		reports = new Report[files.length];

		for(int i = 0; i < files.length; i++) {
			reports[i] = new Report();
			reports[i].file = files[i];
		}

		loader = _loader;
		assignData = _assignData;
	}

	/**
	 * Creates a new ReportMerger object.
	 *
	 * 
	 * 
	 * 
	 * 
	 */
	public ReportMerger(GameData _data, File file, Loader _loader, AssignData _assignData) {
		data = _data;
		data.removeTheVoid(); // removes void regions
		reports = new Report[1];
		reports[0] = new Report();
		reports[0].file = file;

		loader = _loader;
		assignData = _assignData;
	}

	public GameData merge() {
		return merge(new NullUserInterface(), false);
	}

	public GameData merge(UserInterface aUI, boolean async) {
		ui = aUI;
		if(async) {
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
	 * Starts merging. Parent is used as parent for the userinterface.
	 */
	public GameData merge(JFrame parent) {
		return merge(new SwingUserInterface(parent),true);
	}

	private GameData mergeThread() {
		if(ui != null) {
			ui.show();
		}
		try {
			int iPosition = 0;
			int iFailedConnectivity = 0;
			int iMerged = 0;

			while(true) {
				if(!reports[iPosition].merged) {
					if(!mergeReport(reports[iPosition])) {
						iFailedConnectivity++;
					} else {
						iFailedConnectivity = 0;
						iMerged++;
					}
				}

				if((iMerged + iFailedConnectivity) == reports.length) {
					// some reports with out connection to central report
					break;
				}

				iPosition++;

				if(iPosition >= reports.length) {
					iPosition = 0;
					iFailedConnectivity = 0;
				}
			}

			if(iFailedConnectivity > 0) {
				String strMessage = Resources.get("util.reportmerger.msg.noconnection.text.1");

				for(int i = 0; i < reports.length; i++) {
					if(!reports[i].merged) {
						strMessage += reports[i].file.getName();

						if((i + 1) < reports.length) {
							strMessage += ", ";
						}
					}
				}

				strMessage += Resources.get("util.reportmerger.msg.noconnection.text.2");

				if(ui != null && ui.confirm(strMessage, Resources.get("util.reportmerger.msg.confirmmerge.title"))) {
					for(int i = 0; i < reports.length; i++) {
						if(!reports[i].merged) {
							iProgress += 2;
							ui.setProgress(reports[i].file.getName() + " - " +
                  Resources.get("util.reportmerger.status.merging"), iProgress);

							//data.mergeWith( reports[i].data );
							data = GameData.merge(data, reports[i].data);
							reports[i].merged = true;

							reports[i].data = null;
							reports[i].regionMap = null;

						}
					}
				}
			}
		} catch(Exception e) {
			log.error(e);
		}

		if(ui != null) { 
			ui.ready();
		}

		if(assignData != null) {
			assignData.assign(data);
		}

		return data;
	}

	
	/**
	 * Merges a report to the current report.
	 * 
	 * @param report
	 * @return true iff reports were merged or report data null or report types don't match 
	 */
	// TODO: We need to break this monster method up, urgently!!!
	private boolean mergeReport(Report report) {
		if(report.data == null) {
			iProgress += 1;
			if(ui != null) { 
				ui.setProgress(report.file.getName() + " - " + Resources.get("util.reportmerger.status.loading"), iProgress);
			}

			report.data = loader.load(report.file);
		}

		if(report.data == null || !data.name.equalsIgnoreCase(report.data.name)) {
			// no report loaded or 
			// game types doesn't match. Make sure, it will not be tried again.
			// TODO: maybe issue a message here.
			if (report.data == null)
				log.warn("ReportMerger.mergeReport(): got empty data.");
			else
				log.warn("ReportMerger.mergeReport(): game types don't match.");

			report.merged = true;

			return true;
		}

		/**
		 * prepare faction trustlevel for merging: - to be added CR is older or of same age -> hold
		 * existing trust levels - to be added CR is newer and contains trust level that were set
		 * by the user explicitly (or read from CR what means the same) -> take the trust levels
		 * out of the new CR otherwise -> hold existing trust levels This means: set those trust
		 * levels, that will not be retained to default values
		 */
		if((data.getDate() != null) && (report.data.getDate() != null) &&
			   (data.getDate().getDate() < report.data.getDate().getDate()) &&
			   TrustLevels.containsTrustLevelsSetByUser(report.data)) {
			// take the trust levels out of the to be added data
			// set those in the existing data to default-values
			for(Iterator<Faction> iterator = data.factions().values().iterator(); iterator.hasNext();) {
				Faction f = iterator.next();
				f.setTrustLevel(Faction.TL_DEFAULT);
				f.setTrustLevelSetByUser(false);
			}
		} else {
			// take the trust levels out of the existing data
			// set those in the to be added data to default-values
			for(Iterator<Faction> iterator = report.data.factions().values().iterator(); iterator.hasNext();) {
				Faction f = iterator.next();
				f.setTrustLevel(Faction.TL_DEFAULT);
				f.setTrustLevelSetByUser(false);
			}
		}

		/**
		 * Prepare curTempID-Value for merging. If reports are of the same age, keep existing by
		 * setting the new one to default value. Otherwise set the existing to default value.
		 */
		if((data.getDate() != null) && (report.data.getDate() != null) &&
			   (data.getDate().getDate() < report.data.getDate().getDate())) {
			data.setCurTempID(-1);
		} else {
			report.data.setCurTempID(-1);
		}

		boolean reportHasAstralRegions=false;
		boolean dataHasAstralRegions=false;

		// it is safe to assume, that when regionMap is null, schemeMap is null, too
		if(report.regionMap == null) {
			iProgress += 1;
			if(ui != null) {				
				ui.setProgress(report.file.getName() + " - " + Resources.get("util.reportmerger.status.processing"), iProgress);
			}
			report.regionMap = new HashMap<String, Region>();
			report.schemeMap = new HashMap<String, Collection<Region>>();

			for(Iterator iter = report.data.regions().values().iterator(); iter.hasNext();) {
				Region region = (Region) iter.next();

				if((region.getName() != null) && (region.getName().length() > 0)) {
					/*if (report.regionMap.containsKey(region.getName())) {
					    report.regionMap.put(region.getName(), null);
					}else{*/
					report.regionMap.put(region.getName(), region);

					//}
				}

				if(region.getCoordinate().z == 1) {
					reportHasAstralRegions=true;
					for(Iterator schemes = region.schemes().iterator(); schemes.hasNext();) {
						Scheme scheme = (Scheme) schemes.next();
						Collection<Region> col = report.schemeMap.get(scheme.getName());

						if(col == null) {
							col = new LinkedList<Region>();
							report.schemeMap.put(scheme.getName(), col);
						}

						col.add(region);
					}
				}
			}
		}

		// determine translation of coordinate system
		/**
		 * Important: A faction's coordinate system for astral space is indepent of it's coordinate
		 * system for normal space. It depends (as far as I know) on the astral space region where
		 * the faction first enters the astral space (this region will have the coordinate
		 * (0,0,1). Thus a special translation for the astral space (beside that one for normal
		 * space) has to be found.
		 */
		iProgress += 1;
		if(ui != null) {				
			ui.setProgress(report.file.getName() + " - " + Resources.get("util.reportmerger.status.connecting"), iProgress);
		}

		// maps translation (Coordinate) to match count (Integer)
		Map<CoordinateID,Integer> translationMap = new Hashtable<CoordinateID, Integer>();
		Map<CoordinateID,Integer> astralTranslationMap = new Hashtable<CoordinateID, Integer>();

		for(Iterator iter = data.regions().values().iterator(); iter.hasNext();) {
			Region region = (Region) iter.next();

			CoordinateID coord = region.getCoordinate();

			if(coord.z == 0) {
				if((region.getName() != null) && (region.getName().length() > 0)) {
					Region foundRegion = report.regionMap.get(region.getName());

					if(foundRegion != null) {
						CoordinateID foundCoord = foundRegion.getCoordinate();

						CoordinateID translation = new CoordinateID(foundCoord.x - coord.x,
																foundCoord.y - coord.y);

						Integer count = (Integer) translationMap.get(translation);

						if(count == null) {
							count = new Integer(1);
						} else {
							count = new Integer(count.intValue() + 1);
						}

						translationMap.put(translation, count);
					}
				}
			} else if(coord.z == 1) {
				// Now try to find an astral space region that matches this region
				// We can't use region name for this, since all astral space
				// regions are named "Nebel". We use the schemes instead.
				// Since all schemes have to match it's sufficient to look at the
				// first one to find a possible match. To check whether that
				// match really is one, we have to look at all schemes.
				dataHasAstralRegions=true;
				if(!region.schemes().isEmpty()) {
					Scheme scheme = (Scheme) region.schemes().iterator().next();
					Object o = report.schemeMap.get(scheme.getName());

					if(o != null) {
						// we found some astral region that shares at least
						// one scheme with the actual region. However, this
						// doesn't mean a lot, since schemes belong to several
						// astral regions.
						// check whether any of those regions shares all schemes
						for(Iterator regIter = ((Collection) o).iterator(); regIter.hasNext();) {
							Region foundRegion = (Region) regIter.next();

							if(foundRegion.schemes().size() == region.schemes().size()) {
								// at least the size fits
								boolean mismatch = false;

								for(Iterator schemes1 = region.schemes().iterator();
										schemes1.hasNext() && !mismatch;) {
									Scheme s1 = (Scheme) schemes1.next();
									boolean found = false;

									for(Iterator schemes2 = foundRegion.schemes().iterator();
											schemes2.hasNext() && !found;) {
										Scheme s2 = (Scheme) schemes2.next();

										if(s1.getName().equals(s2.getName())) {
											found = true; // found a scheme match
										}
									}

									if(!found) {
										mismatch = true;
									}
								}

								if(!mismatch) {
									// allright, seems we found a valid translation
									CoordinateID foundCoord = foundRegion.getCoordinate();
									CoordinateID translation = new CoordinateID(foundCoord.x - coord.x,
																			foundCoord.y - coord.y,
																			1);
									Integer count = (Integer) astralTranslationMap.get(translation);

									if(count == null) {
										count = new Integer(1);
									} else {
										count = new Integer(count.intValue() + 1);
									}

									astralTranslationMap.put(translation, count);
								}
							}
						}
					}
				}
			}
		}

		// end of search for translations, now check the found ones

		/* check whether any of the normal space translations is impossible by
		   comparing the terrains */
		int maxTerrainMismatches = (int) (Math.max(data.regions().size(),
												   report.data.regions().size()) * 0.02);
		CoordinateID loopCoord = new CoordinateID(0, 0, 0);
		RegionType forestTerrain = data.rules.getRegionType(StringID.create("Wald"));
		RegionType plainTerrain = data.rules.getRegionType(StringID.create("Ebene"));
		RegionType oceanTerrain = data.rules.getRegionType(StringID.create("Ozean"));
		RegionType glacierTerrain = data.rules.getRegionType(StringID.create("Gletscher"));
		RegionType activeVolcanoTerrain = data.rules.getRegionType(StringID.create("Aktiver Vulkan"));
		RegionType volcanoTerrain = data.rules.getRegionType(StringID.create("Vulkan"));

		for(Iterator iter = translationMap.keySet().iterator(); iter.hasNext();) {
			CoordinateID translation = (CoordinateID) iter.next();
			int mismatches = 0; // the number of regions not having the same region type at the current translations

			/* for each traslations we have to compare the regions'
			   terrains */
			for(Iterator regionIter = data.regions().values().iterator(); regionIter.hasNext();) {
				Region r = (Region) regionIter.next();

				if((r.getType() == null) || r.getType().equals(RegionType.unknown)) {
					continue;
				}

				CoordinateID c = r.getCoordinate();

				/* do the translation and find the corresponding
				   region in the report data */
				if(c.z == 0) {
					loopCoord.x = c.x;
					loopCoord.y = c.y;
					loopCoord.translate(translation);

					Region reportDataRegion = (Region) report.data.regions().get(loopCoord);

					/* the hit count for the current translation must
					   only be modified, if there actually are regions
					   to be compared and their terrains are valid */
					if((reportDataRegion != null) && (reportDataRegion.getType() != null) &&
						   !(reportDataRegion.getType().equals(RegionType.unknown))) {
						if(!r.getType().equals(reportDataRegion.getType())) {
							/* now we have a mismatch. If the reports
							   are from the same turn, terrains may
							   not differ at all. If the reports are
							   from different turns, some terrains
							   can be transformed. */
							if((data.getDate() != null) && (report.data.getDate() != null) &&
								   data.getDate().equals(report.data.getDate())) {
								mismatches++;
							} else {
								if(!(((forestTerrain != null) && (plainTerrain != null) &&
									   ((forestTerrain.equals(r.getType()) &&
									   plainTerrain.equals(reportDataRegion.getType())) ||
									   (plainTerrain.equals(r.getType()) &&
									   forestTerrain.equals(reportDataRegion.getType())))) ||
									   ((oceanTerrain != null) && (glacierTerrain != null) &&
									   ((oceanTerrain.equals(r.getType()) &&
									   glacierTerrain.equals(reportDataRegion.getType())) ||
									   (glacierTerrain.equals(r.getType()) &&
									   oceanTerrain.equals(reportDataRegion.getType())))) ||
									   ((activeVolcanoTerrain != null) && (volcanoTerrain != null) &&
									   ((activeVolcanoTerrain.equals(r.getType()) &&
									   volcanoTerrain.equals(reportDataRegion.getType())) ||
									   (volcanoTerrain.equals(r.getType()) &&
									   activeVolcanoTerrain.equals(reportDataRegion.getType())))))) {
									mismatches++;
								}
							}

							if(mismatches > maxTerrainMismatches) {
								translationMap.put(translation, new Integer(-1));

								break;
							}
						}
					}
				}
			}
		}

		/**
		 * Check the astral space translation map by comparing the schemes. Heuristic: If both
		 * space regions have schemes, they shouldn't differ. If they do, somethink is probably
		 * wrong!
		 */
		for(Iterator iter = astralTranslationMap.keySet().iterator(); iter.hasNext();) {
			CoordinateID translation = (CoordinateID) iter.next();

			// the number of astral space region where a scheme mismatch was found.
			int mismatches = 0;

			/* for each traslations we have to compare the regions' schemes */
			for(Iterator regionIter = data.regions().values().iterator(); regionIter.hasNext();) {
				Region r = (Region) regionIter.next();

				if(r.getCoordinate().z != 1) {
					continue;
				}

				CoordinateID c = r.getCoordinate();

				/* do the translation and find the corresponding
				   region in the report data */
				loopCoord.x = c.x + translation.x;
				loopCoord.y = c.y + translation.y;

				Region reportDataRegion = (Region) report.data.regions().get(loopCoord);

				if((reportDataRegion != null) && !reportDataRegion.schemes().isEmpty() &&
					   !r.schemes().isEmpty()) {
					// number of schemes the same?
					boolean mismatch = reportDataRegion.schemes().size() != r.schemes().size();

					// if number is ok, use nested loop to compare scheme names
					for(Iterator schemes1 = reportDataRegion.schemes().iterator();
							schemes1.hasNext() && !mismatch;) {
						Scheme s1 = (Scheme) schemes1.next();
						boolean foundname = false;

						for(Iterator schemes2 = r.schemes().iterator(); schemes2.hasNext();) {
							Scheme s2 = (Scheme) schemes2.next();

							if(s1.getName().equals(s2.getName())) {
								foundname = true; // found a scheme match

								break;
							}
						}

						if(!foundname) {
							mismatch = true;
						}
					}

					if(mismatch) {
						mismatches++;
					}
				}
			}

			// decrease hit count of this translation for each mismatch
			Integer i = (Integer) astralTranslationMap.get(translation);
			Integer i2 = new Integer(i.intValue() - mismatches);
			astralTranslationMap.put(translation, i2);
		}

		int iDX = 0;
		int iDY = 0;
		int iCount = 0;
		boolean bEqual = false;

		// search highest hit count
		Iterator iter = translationMap.entrySet().iterator();

		while(iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();

			CoordinateID translation = (CoordinateID) entry.getKey();
			int count = ((Integer) entry.getValue()).intValue();

			/*System.out.println( "Translation X:" + translation.x + " Y:" + translation.y +
			    " Hits:" + count );*/
			if(count > iCount) {
				iDX = translation.x;
				iDY = translation.y;
				iCount = count;
				bEqual = false;
			} else {
				if(count == iCount) {
					bEqual = true;
				}
			}
		}

		// search for best astral translation
		CoordinateID bestAstralTranslation = new CoordinateID(0, 0, 1);
		int bestHitCount = -1;

		for(iter = astralTranslationMap.keySet().iterator(); iter.hasNext();) {
			CoordinateID translation = (CoordinateID) iter.next();
			int count = ((Integer) astralTranslationMap.get(translation)).intValue();

			if(count > bestHitCount) {
				bestHitCount = count;
				bestAstralTranslation = translation;
			}
		}

		if(reportHasAstralRegions && dataHasAstralRegions && bestHitCount <= 0) {
			log.warn("Warning: ReportMerger: Couldn't find a good translation for astral space coordinate systems. " + 
					 "Merge results on level 1 may be poor and undefined!");
		} else if(astralTranslationMap.size() > 0) {
			log.info("ReportMerger: Found " + astralTranslationMap.size() +
							   " possible translations for astral space. Using this one: " +
							   bestAstralTranslation);
		}

		CoordinateID usedAstralTranslation = null;
		
		// use astral space translation anyway
		if((data.getDate() == null) || (report.data.getDate() == null)) {
			usedAstralTranslation = bestAstralTranslation;
//			report.data.placeOrigin(bestAstralTranslation);
		} else {
			// TODO: figure out if it is "<" or ">" here
			if(data.getDate().getDate() > report.data.getDate().getDate()) {
				usedAstralTranslation = new CoordinateID(-bestAstralTranslation.x, -bestAstralTranslation.y,
						bestAstralTranslation.z);
//				data.placeOrigin(new Coordinate(-bestAstralTranslation.x, -bestAstralTranslation.y,
//												bestAstralTranslation.z));
			} else {
				usedAstralTranslation = bestAstralTranslation;
//				report.data.placeOrigin(bestAstralTranslation);
			}
		}

		// TODO: manual translation
//		if (newAstralOrigin!= null && forceAstralOrigin)
//			usedAstralTranslation= newAstralOrigin;
		
		if (usedAstralTranslation != null && usedAstralTranslation != new CoordinateID(0,0,1)){
			log.info("ReportMerger: Using this astral translation: " + usedAstralTranslation.toString());
//			report.data.placeOrigin(usedAstralTranslation);
			try {
				if (usedAstralTranslation.x != 0 || usedAstralTranslation.y != 0)
					report.data = (GameData) report.data.clone(usedAstralTranslation);
			} catch (CloneNotSupportedException e) {
				log.error(e);
			}

		}
		

		CoordinateID usedTranslation = null;

		if ((data.getDate() == null) || (report.data.getDate() == null)) {
			usedTranslation = new CoordinateID(iDX, iDY);
			// report.data.placeOrigin(new Coordinate(iDX, iDY));
		} else {
			if (data.getDate().getDate() > report.data.getDate().getDate()) {
				usedTranslation = new CoordinateID(-iDX, -iDY);
				// data.placeOrigin(new Coordinate(-iDX, -iDY));
			} else {
				usedTranslation = new CoordinateID(iDX, iDY);
				// report.data.placeOrigin(new Coordinate(iDX, iDY));
			}
		}

		// TODO: manual translation
//		if (newOrigin != null && forceOrigin)
//			usedTranslation = newOrigin;

		if (usedTranslation != null
				&& usedTranslation != new CoordinateID(0, 0, 0)) {
			log.info("ReportMerger: Using this translation: "
					+ usedTranslation.toString());
//			report.data.placeOrigin(usedTranslation);
			try {				
				if (usedTranslation.x != 0 || usedTranslation.y != 0)
					report.data = (GameData) report.data.clone(usedTranslation);
			} catch (CloneNotSupportedException e) {
				log.error(e);
			}
		}

		// valid translation?
		if ((iCount > 0) && (!bEqual)) {
			iProgress += 1;
			if (ui != null) {
				ui.setProgress(report.file.getName() + " - "
						+ Resources.get("util.reportmerger.status.merging"), iProgress);
			}
			
			
			///////////////////////////////////////////////////
			// Merge the reports, finally!
			
			// data.mergeWith( report.data );
			data = GameData.merge(data, report.data);
			report.merged = true;

			report.data = null;
			report.regionMap = null;
		} else {
			iProgress -= 1;
			if(ui != null) {				
				ui.setProgress(report.file.getName(), iProgress);
			}
		}

		return report.merged;
	}

	private class ProgressDlg extends JDialog {
		/**
		 * Creates new form ProgressDlg
		 *
		 * 
		 * 
		 */
		public ProgressDlg(Frame parent, boolean modal) {
			super(parent, modal);
			initComponents();
			pack();
		}

		private void initComponents() {
			labelText = new JLabel();
			progressBar = new JProgressBar();
			getContentPane().setLayout(new GridBagLayout());

			GridBagConstraints gridBagConstraints1;
			setTitle(Resources.get("util.reportmerger.window.title"));

			/*addWindowListener(new java.awt.event.WindowAdapter() {
			    public void windowClosing(java.awt.event.WindowEvent evt) {
			        closeDialog(evt);
			    }
			}
			);*/
			labelText.setPreferredSize(new Dimension(250, 16));
			labelText.setMinimumSize(new Dimension(250, 16));
			labelText.setText("jLabel1");
			labelText.setHorizontalAlignment(SwingConstants.CENTER);
			labelText.setMaximumSize(new Dimension(32767, 16));

			gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.gridy = 1;
			gridBagConstraints1.fill = GridBagConstraints.BOTH;
			gridBagConstraints1.insets = new Insets(0, 5, 5, 5);
			gridBagConstraints1.weightx = 1.0;
			gridBagConstraints1.weighty = 0.5;
			getContentPane().add(labelText, gridBagConstraints1);

			progressBar.setPreferredSize(new Dimension(250, 14));
			progressBar.setMinimumSize(new Dimension(250, 14));

			gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.fill = GridBagConstraints.BOTH;
			gridBagConstraints1.insets = new Insets(5, 5, 5, 5);
			gridBagConstraints1.weightx = 1.0;
			gridBagConstraints1.weighty = 0.5;
			getContentPane().add(progressBar, gridBagConstraints1);
		}

		/** Closes the dialog */

		/*private void closeDialog(java.awt.event.WindowEvent evt) {
		    setVisible (false);
		    dispose ();
		}*/
		public JLabel labelText;

		/** DOCUMENT-ME */
		public JProgressBar progressBar;
	}
}
