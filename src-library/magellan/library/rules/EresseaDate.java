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

package magellan.library.rules;

import magellan.library.utils.Resources;

/**
 * DOCUMENT ME!
 * 
 * @author Sebastian
 * @version
 */
public class EresseaDate extends Date {
  private static final String months_old[] = { "Januar", "Februar", "März", "April", "Mai", "Juni", "Juli", "August", "September", "Oktober", "November", "Dezember" };
  private static final String months_new[] = { "Feldsegen", "Nebeltage", "Sturmmond", "Herdfeuer", "Eiswind", "Schneebann", "Blütenregen", "Mond_der_milden_Winde", "Sonnenfeuer" };

  // long
  private static final String week_long[] = { "Erste_Woche_des_Monats_", "Zweite_Woche_des_Monats_", "Dritte_Woche_des_Monats_" };
  private static final String year_long = "_im_Jahre_";
  private static final String age_long = "_des_zweiten_Zeitalters.";

  // long alt
  private static final String week_long_alt[] = { "Anfang_des_Monats_", "Mitte_des_Monats_", "Ende_des_Monats_" };
  private static final String year_long_alt = "_im_Jahre_";
  private static final String age_long_alt = "_des_zweiten_Zeitalters.";

  // long alt2
  private static final String week_long_alt2[] = { "Erste_Woche_des_Monats_", "Zweite_Woche_des_Monats_", "Letzte_Woche_des_Monats_" };
  private static final String year_long_alt2 = "_im_Jahre_";
  private static final String age_long_alt2 = "_des_zweiten_Zeitalters.";

  // short internationalized
  private static final String week_short[] = { "1.", "2.", "3." };

  // phrase
  private static final String begin_phrase = "Wir_schreiben_";
  private static final String week_phrase[] = { "die_erste_Woche_des_Monats_", "die_zweite_Woche_des_Monats_", "die_dritte_Woche_des_Monats_" };
  private static final String year_phrase = "_im_Jahre_";
  private static final String age_phrase = "_des_zweiten_Zeitalters.";

  // alternative phrase
  private static final String begin_phrase_alt = "Wir_haben_";
  private static final String week_phrase_alt[] = { "den_Anfang_des_Monats_", "die_Mitte_des_Monats_", "das_Ende_des_Monats_" };
  private static final String year_phrase_alt = "_im_Jahre_";
  private static final String age_phrase_alt = "_des_zweiten_Zeitalters.";

  // alternative phrase 2
  private static final String begin_phrase_alt2 = "Wir_schreiben_";
  private static final String week_phrase_alt2[] = { "die_erste_Woche_des_Monats_", "die_zweite_Woche_des_Monats_", "die_letzte_Woche_des_Monats_" };
  private static final String year_phrase_alt2 = "_im_Jahre_";
  private static final String age_phrase_alt2 = "_des_zweiten_Zeitalters.";

  // seasons
  private static final String seasonPhrases[] = { "Es_ist_Sommer", "Es_ist_Herbst", "Es_ist_Herbst", "Es_ist_Winter", "Es_ist_Winter", "Es_ist_Winter", "Es_ist_Frühling", "Es_ist_Frühling", "Es_ist_Sommer" };

  // private static int epochsBeginAt[] = {0, 1, 184};
  private int epoch = 1;

  /**
   * Creates new EresseaDate
   */
  public EresseaDate(int iInitDate) {
    super(iInitDate);
  }

  /**
   * DOCUMENT-ME
   */
  public int getEpoch() {
    return this.epoch;
  }

  /**
   * DOCUMENT-ME
   */
  public void setEpoch(int newEpoch) {
    this.epoch = newEpoch;

    /*
     * not such a bad idea, actually, but removed for vinyambar int round =
     * getDate(); if (newEpoch > 0 && newEpoch < epochsBeginAt.length) { if
     * (round < epochsBeginAt[newEpoch]) { setDate(epochsBeginAt[newEpoch]); }
     * else if (newEpoch < epochsBeginAt.length - 1 && round >
     * epochsBeginAt[newEpoch + 1]) { setDate(epochsBeginAt[newEpoch + 1]); } }
     */
  }

  /**
   * DOCUMENT-ME
   */
  public String toString(int iDateType) {
    String strDate = "";

    if (getEpoch() == 1) {
      // first age
      switch (iDateType) {
      default:
      case TYPE_SHORT:
        strDate = months_old[(iDate - 1) % 12] + " " + (((iDate - 1) / 12) + 1);

        break;

      case TYPE_LONG:
        strDate = months_old[(iDate - 1) % 12] + " des Jahres " + (((iDate - 1) / 12) + 1) + " im ersten Zeitalter";

        break;

      case TYPE_PHRASE:
        strDate = "Wir schreiben den " + months_old[(iDate - 1) % 12] + " des Jahres " + (((iDate - 1) / 12) + 1) + " im ersten Zeitalter.";

        break;
      }
    } else if (getEpoch() == 2) {
      // second age
      int iDate2 = iDate;

      if (iDate2 >= 184) {
        iDate2 -= 184;
      }

      switch (iDateType) {
      default:
      case Date.TYPE_SHORT: {
        int iWeek = (iDate2 % 3) + 1;
        String strWeek = Resources.get("rules.eresseadate."+week_short[iWeek - 1]);
        String strMonth = Resources.get("rules.eresseadate."+months_new[(iDate2 / 3) % 9]);
        int iYear = (iDate2 / 27) + 1;

        // strDate = iWeek + " " + getString("._Woche_") +" " + strMonth +
        // iYear;
        strDate = strWeek + " " + Resources.get("rules.eresseadate.Woche") + " " + strMonth + " " + Resources.get("rules.eresseadate.Jahr") + " " + iYear;
      }

        break;

      case Date.TYPE_LONG: {
        int iWeek = iDate2 % 3;
        String strMonth = Resources.get("rules.eresseadate."+months_new[(iDate2 / 3) % 9]);
        int iYear = (iDate2 / 27) + 1;

        switch (((int) (java.lang.Math.random() * 3)) % 3) {
        default:
        case 0:
          strDate = Resources.get("rules.eresseadate."+week_long[iWeek]) + " " + strMonth + " " + Resources.get("rules.eresseadate."+year_long) + " " + iYear + " " + Resources.get("rules.eresseadate."+age_long);

          break;

        case 1:
          strDate = Resources.get("rules.eresseadate."+week_long_alt[iWeek]) + " " + strMonth + " " + Resources.get("rules.eresseadate."+year_long_alt) + " " + iYear + " " + Resources.get("rules.eresseadate."+age_long_alt);

          break;

        case 2:
          strDate = Resources.get("rules.eresseadate."+week_long_alt2[iWeek]) + " " + strMonth + " " + Resources.get("rules.eresseadate."+year_long_alt2) + " " + iYear + " " + Resources.get("rules.eresseadate."+age_long_alt2);

          break;
        }
      }

        break;

      case TYPE_PHRASE: {
        int iWeek = iDate2 % 3;
        String strMonth = Resources.get("rules.eresseadate."+months_new[(iDate2 / 3) % 9]);
        int iYear = (iDate2 / 27) + 1;

        switch (((int) (java.lang.Math.random() * 3)) % 3) {
        default:
        case 0:
          strDate = Resources.get("rules.eresseadate."+begin_phrase) + " " + Resources.get("rules.eresseadate."+week_phrase[iWeek]) + " " + strMonth + " " + Resources.get("rules.eresseadate."+year_phrase) + " " + iYear + " " + Resources.get("rules.eresseadate."+age_phrase);

          break;

        case 1:
          strDate = Resources.get("rules.eresseadate."+begin_phrase_alt) + " " + Resources.get("rules.eresseadate."+week_phrase_alt[iWeek]) + " " + strMonth + " " + Resources.get("rules.eresseadate."+year_phrase_alt) + " " + iYear + " " + Resources.get("rules.eresseadate."+age_phrase_alt);

          break;

        case 2:
          strDate = Resources.get("rules.eresseadate."+begin_phrase_alt2) + " " + Resources.get("rules.eresseadate."+week_phrase_alt2[iWeek]) + " " + strMonth + " " + Resources.get("rules.eresseadate."+year_phrase_alt2) + " " + iYear + " " + Resources.get("rules.eresseadate."+age_phrase_alt2);

          break;
        }
      }

        break;

      case TYPE_PHRASE_AND_SEASON: {
        int iWeek = iDate2 % 3;
        String strMonth = Resources.get("rules.eresseadate."+months_new[(iDate2 / 3) % 9]);
        String season = " " + Resources.get("rules.eresseadate."+seasonPhrases[(iDate2 / 3) % 9]);
        int iYear = (iDate2 / 27) + 1;

        switch (((int) (java.lang.Math.random() * 3)) % 3) {
        default:
        case 0:
          strDate = Resources.get("rules.eresseadate."+begin_phrase) + " " + Resources.get("rules.eresseadate."+week_phrase[iWeek]) + " " + strMonth + " " + Resources.get("rules.eresseadate."+year_phrase) + " " + iYear + " " + Resources.get("rules.eresseadate."+age_phrase) + season;

          break;

        case 1:
          strDate = Resources.get("rules.eresseadate."+begin_phrase_alt) + " " + Resources.get("rules.eresseadate."+week_phrase_alt[iWeek]) + " " + strMonth + " " + Resources.get("rules.eresseadate."+year_phrase_alt) + " " + iYear + " " + Resources.get("rules.eresseadate."+age_phrase_alt) + " " + season;

          break;

        case 2:
          strDate = Resources.get("rules.eresseadate."+begin_phrase_alt2) + " " + Resources.get("rules.eresseadate."+week_phrase_alt2[iWeek]) + " " + strMonth + " " + Resources.get("rules.eresseadate."+year_phrase_alt2) + " " + iYear + " " + Resources.get("rules.eresseadate."+age_phrase_alt2) + season;

          break;
        }
      }

        break;
      }
    }
    strDate = strDate.replaceAll("  ", " ");
    return strDate;
  }

  /**
   * DOCUMENT-ME
   */
  public magellan.library.ID copy() {
    return new EresseaDate(this.iDate);
  }

  // /////////////////////////////
  // INTERNATIONALIZATION Code //
  // /////////////////////////////
}
