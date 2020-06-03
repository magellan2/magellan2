/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.library.rules;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Random;

import magellan.library.utils.Resources;
import magellan.library.utils.SelfCleaningProperties;
import magellan.library.utils.logging.Logger;

/**
 * This class represents an Eressea Date.
 * 
 * @author Sebastian
 */
public class EresseaDate extends Date {
  private static final Logger log = Logger.getInstance(EresseaDate.class);
  private static final String months_old[] = { "Januar", "Februar", "März", "April", "Mai", "Juni",
      "Juli", "August", "September", "Oktober", "November", "Dezember" };
  private static final String months_new[] = { "Feldsegen", "Nebeltage", "Sturmmond", "Herdfeuer",
      "Eiswind", "Schneebann", "Blütenregen", "Mond_der_milden_Winde", "Sonnenfeuer" };

  private static final int EPOCH1_START = 0;
  private static final int EPOCH2_START = 184;
  private static final int EPOCH3_START = 1;

  private int epoch = 1;
  private int startRound = 1;
  private Random r = new Random();

  /**
   * Creates new EresseaDate.
   */
  public EresseaDate(int iInitDate) {
    this(iInitDate, 0);

  }

  /**
   * Creates new EresseaDate with adjustment. For example, the second age of Eressea started after
   * the first week of the seventh month of the year 6, so it could be created with
   * <code>EresseaDate(185, 6, 7, 1)</code> (but also with <code>EresseaDate(185)</code>, epoch 2.
   * 
   * @param iInitDate The date (as read from the report).
   * @param yearOffset The year of round 1.
   * @param monthOffset The month of round 1.
   * @param weekOffset The week of round 1.
   */
  public EresseaDate(int iInitDate, int yearOffset, int monthOffset, int weekOffset) {
    super(iInitDate);

    startRound = -27 * (yearOffset - 1) - 3 * (monthOffset - 1) + (weekOffset);
  }

  /**
   * Creates new EresseaDate with adjustment.
   * 
   * @param iInitDate The date (or round, as read from the report)
   * @param startRound The round corresponding to year 1, month 1, week 1
   */
  public EresseaDate(int iInitDate, int startRound) {
    super(iInitDate);
    this.startRound = startRound;
  }

  /**
   * Returns the epoch ("Zeitalter").
   */
  public int getEpoch() {
    return epoch;
  }

  /**
   * Sets the epoch ("Zeitalter").
   */
  public void setEpoch(int newEpoch) {
    epoch = newEpoch;
    switch (epoch) {
    case 1:
      startRound = EPOCH1_START;
      break;
    case 2:
      startRound = EPOCH2_START;
      break;
    case 3:
      startRound = EPOCH3_START;
      break;

    default:
      startRound = 1;
      break;
    }
  }

  /**
   * Returns a (usually localized) string representation of the date.
   * 
   * @see magellan.library.rules.Date#toString(int) Either {@link Date#TYPE_LONG},
   *      {@link Date#TYPE_LONG}, or {@link Date#TYPE_PHRASE}.
   */
  @Override
  public String toString(int iDateType) {
    try {
      String strDate = "";

      if (getEpoch() == 1) {
        if (iDate < 1) {
          strDate = Resources.get("rules.eresseadate.invalid");
        } else {
          // first age
          switch (iDateType) {
          default:
          case TYPE_SHORT:
            strDate = EresseaDate.months_old[(iDate - 1) % 12] + " " + (((iDate - 1) / 12) + 1);
            break;

          case TYPE_LONG:
            strDate =
                Resources.get("rules.eresseadate.epoch1.type_long",
                    EresseaDate.months_old[(iDate - 1) % 12], (((iDate - 1) / 12) + 1));
            break;

          case TYPE_PHRASE:
            strDate =
                Resources.get("rules.eresseadate.epoch1.type_long",
                    EresseaDate.months_old[(iDate - 1) % 12], (((iDate - 1) / 12) + 1));
            break;
          }
        }
      } else if (getEpoch() >= 2) {
        // second and third age
        int iDate2 = getWeekFromStart();
        if (iDate2 < 0) {
          strDate = Resources.get("rules.eresseadate.invalid");
        } else {

          int iWeek = iDate2 % 3;
          String strWeek = Resources.get("rules.eresseadate.week_short." + (iWeek + 1));
          String strMonth =
              Resources.get("rules.eresseadate." + EresseaDate.months_new[(iDate2 / 3) % 9]);
          int iYear = (iDate2 / 27) + 1;

          switch (iDateType) {
          default:
          case Date.TYPE_SHORT: {
            strDate = Resources.get("rules.eresseadate.type_short", strWeek, strMonth, iYear);
          }
            break;

          case Date.TYPE_LONG: {
            // select one of three phrases at random
            int random = r.nextInt(3);
            String strWeekLong =
                Resources.get("rules.eresseadate.week_long." + (iWeek + 1) + "." + random);
            String strAge = Resources.get("rules.eresseadate.age_long." + getEpoch());
            strDate =
                Resources.get("rules.eresseadate.type_long." + random, strWeekLong, strMonth, iYear,
                    strAge);
          }
            break;

          case TYPE_PHRASE: {
            int random = r.nextInt(3);
            String strWeekLong =
                Resources.get("rules.eresseadate.week_phrase." + (iWeek + 1) + "." + random);
            String strAge = Resources.get("rules.eresseadate.age_phrase." + getEpoch());
            strDate =
                Resources.get("rules.eresseadate.type_phrase." + random, strWeekLong, strMonth,
                    iYear, strAge);
          }
            break;

          case TYPE_PHRASE_AND_SEASON: {
            int random = r.nextInt(3);

            String strWeekLong =
                Resources.get("rules.eresseadate.week_phrase." + (iWeek + 1) + "." + random);
            String strAge = Resources.get("rules.eresseadate.age_phrase." + getEpoch());
            String strSeason = " " + Resources.get("rules.eresseadate.season_phrase." + getSeason());
            strDate =
                Resources.get("rules.eresseadate.type_phrase_season." + random, strWeekLong,
                    strMonth, iYear, strAge, strSeason);
          }
            break;
          }
        }
      }
      strDate = strDate.replaceAll("  ", " ");
      return strDate;
    } catch (Throwable e) {
      log.error("illegal state", e);
      return iDate + "/" + epoch;
    }
  }

  /**
   * Returns the difference to (year 0, week 27).
   */
  protected int getWeekFromStart() {
    int iDate2 = iDate - startRound;

    if (iDate2 < 0) {
      log.errorOnce("invalid date " + iDate);
      // iDate2 = Math.max(0, iDate2);
    }

    return iDate2;
  }

  /**
   * Creates a copy of this Date object.
   */
  @Override
  public EresseaDate clone() {
    EresseaDate date = (EresseaDate) super.clone();

    date.epoch = epoch;
    date.startRound = startRound;

    return date;
  }

  /**
   * Creates a clone.
   */
  public EresseaDate copy() {
    return clone();
  }

  /**
   * @see magellan.library.rules.Date#getSeason()
   */
  @Override
  public int getSeason() {
    if (getEpoch() < 2)
      return Date.UNKNOWN;

    int time = getWeekFromStart();
    while (time < 0) {
      time += 27;
    }

    switch ((time / 3) % 9) {
    case 8:
    case 0:
      return Date.SUMMER;
    case 1:
    case 2:
      return Date.AUTUMN;
    case 3:
    case 4:
    case 5:
      return Date.WINTER;
    case 6:
    case 7:
      return Date.SPRING;
    default:
      return Date.UNKNOWN;
    }
  }

  private static Properties loadSettings(File directory, String fileName) {
    Properties settings = new SelfCleaningProperties();
    // settings = new OrderedOutputProperties();
    // settings = new AgingProperties();

    settings.clear();

    File settingsFile = new File(directory, fileName);

    // load settings from file
    if (settingsFile.exists()) {
      try {
        BufferedInputStream stream = null;
        try {
          settings.load(stream = new BufferedInputStream(new FileInputStream(settingsFile)));
          log.info("successfully loaded " + settingsFile);
        } finally {
          if (stream != null) {
            stream.close();
          }
        }
      } catch (IOException e) {
        log.error("Error while loading " + settingsFile, e);
        return null;
      }
    } else
      return null;
    return settings;
  }

  /**
   * Test method.
   * 
   * @param args
   */
  public static void main(String[] args) {
    Resources.initStaticPaths(loadSettings(new File(".", "sandbox/de"), "magellan.ini"));
    Resources.getInstance().initialize(new File("."), "");

    for (int i = 1; i < 24; ++i) {
      EresseaDate date = new EresseaDate(i);
      System.out.println(date.toString(TYPE_SHORT));
      System.out.println(date.toString(TYPE_LONG));
      System.out.println(date.toString(TYPE_PHRASE));
      System.out.println(date.toString(TYPE_PHRASE_AND_SEASON));
    }
    System.out.println();
    EresseaDate date = new EresseaDate(1);
    date.setEpoch(2);
    System.out.println(date.toString(TYPE_SHORT));
    System.out.println();
    for (int i = 1; i < 50; ++i) {
      date = new EresseaDate(182 + i);
      date.setEpoch(2);
      System.out.println(date.toString(TYPE_SHORT));
      System.out.println(date.toString(TYPE_LONG));
      System.out.println(date.toString(TYPE_PHRASE));
      System.out.println(date.toString(TYPE_PHRASE_AND_SEASON));
    }
  }
}
