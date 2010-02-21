/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.library.rules;

import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

/**
 * This class represents an Eressea Date.
 * 
 * @author Sebastian
 */
public class EresseaDate extends Date {
  private static final Logger log = Logger.getInstance(EresseaDate.class);
  private static final String months_old[] =
      { "Januar", "Februar", "März", "April", "Mai", "Juni", "Juli", "August", "September",
          "Oktober", "November", "Dezember" };
  private static final String months_new[] =
      { "Feldsegen", "Nebeltage", "Sturmmond", "Herdfeuer", "Eiswind", "Schneebann", "Blütenregen",
          "Mond_der_milden_Winde", "Sonnenfeuer" };

  private int epoch = 1;
  private int yearOffset = -1;
  private int monthOffset = -1;
  private int weekOffset = -1;

  /**
   * Creates new EresseaDate.
   */
  public EresseaDate(int iInitDate) {
    super(iInitDate);
  }

  /**
   * Creates new EresseaDate with adjustment. For example, the second age of Eressea started after
   * the first week of the seventh month of the year 6, so it could be created with
   * <code>EresseaDate(185, 6, 7, 1)</code> (but also with <code>EresseaDate(185)</code>, epoch 2.
   * 
   * @param iInitDate The date (as read from the report).
   * @param yearOffset The year of week 1.
   * @param monthOffset The month of week 1.
   * @param weekOffset The week of week 1.
   */
  public EresseaDate(int iInitDate, int yearOffset, int monthOffset, int weekOffset) {
    super(iInitDate);
    this.yearOffset = yearOffset;
    this.monthOffset = monthOffset;
    this.weekOffset = weekOffset;
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
  }

  /**
   * Returns a (usually localized) string representation of the date.
   * 
   * @see magellan.library.rules.Date#toString(int) Either {@link Date#TYPE_LONG},
   *      {@link Date#TYPE_LONG}, or {@link Date#TYPE_PHRASE}.
   */
  @Override
  public String toString(int iDateType) {
    String strDate = "";

    if (getEpoch() == 1) {
      // first age
      switch (iDateType) {
      default:
      case TYPE_SHORT:
        strDate = EresseaDate.months_old[(iDate - 1) % 12] + " " + (((iDate - 1) / 12) + 1);

        break;

      case TYPE_LONG:
        strDate =
            EresseaDate.months_old[(iDate - 1) % 12] + " des Jahres " + (((iDate - 1) / 12) + 1)
                + " im ersten Zeitalter";

        break;

      case TYPE_PHRASE:
        strDate =
            "Wir schreiben den " + EresseaDate.months_old[(iDate - 1) % 12] + " des Jahres "
                + (((iDate - 1) / 12) + 1) + " im ersten Zeitalter.";

        break;
      }
    } else if (getEpoch() >= 2) {
      // second and third age
      int iDate2 = getWeekFromStart();

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
        int random = ((int) (java.lang.Math.random() * 3)) % 3;
        String strWeekLong =
            Resources.get("rules.eresseadate.week_long." + (iWeek + 1) + "." + random);
        String strAge = Resources.get("rules.eresseadate.age_long." + getEpoch());
        strDate =
            Resources.get("rules.eresseadate.type_long." + random, strWeekLong, strMonth, iYear,
                strAge);
      }
        break;

      case TYPE_PHRASE: {
        int random = ((int) (java.lang.Math.random() * 3)) % 3;
        String strWeekLong =
            Resources.get("rules.eresseadate.week_phrase." + (iWeek + 1) + "." + random);
        String strAge = Resources.get("rules.eresseadate.age_phrase." + getEpoch());
        strDate =
            Resources.get("rules.eresseadate.type_phrase." + random, strWeekLong, strMonth, iYear,
                strAge);
      }
        break;

      case TYPE_PHRASE_AND_SEASON: {
        int random = ((int) (java.lang.Math.random() * 3)) % 3;
        String strWeekLong =
            Resources.get("rules.eresseadate.week_phrase." + (iWeek + 1) + "." + random);
        String strAge = Resources.get("rules.eresseadate.age_phrase." + getEpoch());
        String strSeason = " " + Resources.get("rules.eresseadate.season_phrase." + getSeason());
        strDate =
            Resources.get("rules.eresseadate.type_phrase_season." + random, strWeekLong, strMonth,
                iYear, strAge, strSeason);
      }
        break;
      }
    }
    strDate = strDate.replaceAll("  ", " ");
    return strDate;
  }

  /**
   * Returns the difference to (year 0, week 27).
   */
  public int getWeekFromStart() {

    int iDate2 = iDate;
    if (yearOffset != -1) {
      iDate2 -= 27 * (yearOffset);
      iDate2 -= (monthOffset) * 3;
      iDate2 -= weekOffset + 1;
    } else {
      if (getEpoch() == 1)
        return iDate;

      if (getEpoch() == 2) {
        if (iDate2 >= 184) {
          iDate2 -= 184;
        }
      } else if (getEpoch() == 3) {
        iDate2 -= 1;
      }

      if (getEpoch() > 3) {
        log.error("unknown epoch, we'll try our best...");
      }
    }

    if (iDate2 < 0) {
      log.error("invalid date " + iDate);
      iDate2 = Math.max(0, iDate2);
    }

    return iDate2;
  }

  /**
   * Creates a clone.
   */
  public EresseaDate copy() {
    EresseaDate date = new EresseaDate(iDate, yearOffset, monthOffset, weekOffset);
    date.setEpoch(getEpoch());
    return date;
  }

  /**
   * @see magellan.library.rules.Date#getSeason()
   */
  @Override
  public int getSeason() {
    if (getEpoch() < 2)
      return Date.UNKNOWN;

    int time = getWeekFromStart();

    switch ((time / 3) % 9) {
    case 0:
      return Date.SUMMER;
    case 1:
      return Date.AUTUMN;
    case 2:
      return Date.AUTUMN;
    case 3:
      return Date.WINTER;
    case 4:
      return Date.WINTER;
    case 5:
      return Date.WINTER;
    case 6:
      return Date.SPRING;
    case 7:
      return Date.SPRING;
    case 8:
      return Date.SUMMER;
    default:
      return Date.UNKNOWN;
    }
  }

}
