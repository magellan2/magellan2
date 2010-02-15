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

package magellan.library.io.cr;

import java.io.File;
import java.io.IOException;

import magellan.library.CoordinateID;
import magellan.library.GameData;
import magellan.library.io.GameDataReader;
import magellan.library.io.file.CopyFile;
import magellan.library.io.file.FileType;
import magellan.library.io.file.FileTypeFactory;
import magellan.library.io.file.PipeFileType;
import magellan.library.utils.ReportMerger;
import magellan.library.utils.ReportMerger.ReportTranslator;
import magellan.library.utils.logging.Logger;

/**
 * This used to be the Loader class. Now it only supports cloning via cr writing/reading
 * 
 * @author $Author: $
 * @version $Revision: 299 $
 */
public class Loader {
  private static final Logger log = Logger.getInstance(Loader.class);

  /**
   * Creates a clone of the GameData using CRWriter/CRParser
   * 
   * @param data the given GameData
   * @return a clone of the given GameData
   * @throws CloneNotSupportedException if cloning failed
   */
  public GameData cloneGameData(GameData data) throws CloneNotSupportedException {
    return cloneGameData(data, new ReportMerger.IdentityTranslator());
  }

  /**
   * @deprecated Use {@link #cloneGameData(GameData, ReportTranslator)}
   */
  @Deprecated
  synchronized public GameData cloneGameDataInMemory(final GameData data,
      final CoordinateID newOrigin) throws CloneNotSupportedException {
    return cloneGameDataInMemory(data, new ReportMerger.TwoLevelTranslator(newOrigin, CoordinateID
        .create(0, 0)));
  }

  /**
   * Clones the data and translates coordinates.
   * 
   * @param data
   * @param coordinateTranslator
   * @return A copy of data, translated by the translator.
   * @throws CloneNotSupportedException if cloning failed
   */
  synchronized public GameData cloneGameDataInMemory(final GameData data,
      final ReportTranslator coordinateTranslator) throws CloneNotSupportedException {
    try {
      final PipeFileType filetype = new PipeFileType();
      filetype.setEncoding(data.getEncoding());
      final CRWriter crw = new CRWriter(data, null, filetype, data.getEncoding());
      GameDataReader crReader = new GameDataReader(null);

      class ReadRunner implements Runnable {
        boolean done = false;
        GameDataReader r;
        GameData d[];

        ReadRunner(GameDataReader r, GameData d[]) {
          this.r = r;
          this.d = d;
        }

        public boolean finished() {
          return done;
        }

        public void run() {
          try {
            d[0] = r.readGameData(filetype, coordinateTranslator, data.getGameName());
            done = true;
          } catch (IOException e1) {
            e1.printStackTrace();
          }
        }
      }

      GameData newData[] = new GameData[1];
      ReadRunner runner = new ReadRunner(crReader, newData);
      new Thread(runner).start();

      crw.writeSynchronously();
      crw.close();

      while (!runner.finished()) {
        notifyAll();
        try {
          wait(500);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }

      newData[0].setFileType(data.getFileType());

      return newData[0];
    } catch (IOException ioe) {
      Loader.log.error("Loader.cloneGameData failed!", ioe);
      throw new CloneNotSupportedException(ioe.toString());
    }

  }

  /**
   * Creates a clone of the GameData using CRWriter/CRParser
   * 
   * @param data the given GameData
   * @return a clone of the given GameData
   * @throws CloneNotSupportedException if cloning failed
   */
  public GameData cloneGameData(GameData data, ReportTranslator coordinateTranslator)
      throws CloneNotSupportedException {
    try {
      File tempFile = CopyFile.createCrTempFile();
      tempFile.deleteOnExit();

      FileType filetype = FileTypeFactory.singleton().createFileType(tempFile, false);
      filetype.setCreateBackup(false);

      // write cr to file
      CRWriter crw = new CRWriter(data, null, filetype, data.getEncoding());

      try {
        crw.writeSynchronously();
      } finally {
        crw.close();
      }

      GameData newData = new GameDataReader(null).readGameData(filetype, coordinateTranslator);
      newData.setFileType(data.getFileType());
      tempFile.delete();

      return newData;
    } catch (IOException ioe) {
      Loader.log.error("Loader.cloneGameData failed!", ioe);
      throw new CloneNotSupportedException(ioe.toString());
    }
  }

  /**
   * This method loads a report file from the given file.
   */
  public static GameData load(File file) throws IOException {
    FileType fileType = FileTypeFactory.singleton().createFileType(file, true);
    GameData data = new GameDataReader(null).readGameData(fileType);
    return data;
  }
}
