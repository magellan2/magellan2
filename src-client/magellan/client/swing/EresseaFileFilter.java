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

package magellan.client.swing;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import magellan.library.utils.Resources;

/**
 * A FileFilter extension for Eressea file types
 */
public class EresseaFileFilter extends javax.swing.filechooser.FileFilter {
  /** Selects .cr-files */
  public static final int CR_FILTER = 0;

  /** Selects .txt-files */
  public static final int TXT_FILTER = 1;

  /** Selects .zip-files */
  public static final int ZIP_FILTER = 2;

  /** Selects .gz-files */
  public static final int GZ_FILTER = 3;

  /** Selects .bz2-files */
  public static final int BZ2_FILTER = 4;

  /** Selects .cr, .zip, .gz, and .bz2-files */
  public static final int ALLCR_FILTER = 5;

  /** Selects .zip, .gz, and .bz2-files (but not .cr) */
  public static final int ALLCR_COMPRESSED_FILTER = 6;

  /** Selects .nr files */
  public static final int NR_FILTER = 7;

  /** Selects .r files */
  public static final int ATLANTIS_FILTER = 8;

  private List<String> extensions;
  protected String description = "";

  /**
   * Creates a new EresseaFileFilter object.
   * 
   * @param flag One of the <code>FILTER</code>-flags
   */
  public EresseaFileFilter(int flag) {
    extensions = new LinkedList<String>();
    switch (flag) {
    case CR_FILTER:
    case TXT_FILTER:
    case ZIP_FILTER:
    case GZ_FILTER:
    case BZ2_FILTER:
    case ATLANTIS_FILTER:
    case NR_FILTER:
      extensions.add(getExtension(flag));
      break;
    case ALLCR_FILTER:
      extensions.add(getExtension(EresseaFileFilter.CR_FILTER));
      extensions.add(getExtension(EresseaFileFilter.ZIP_FILTER));
      extensions.add(getExtension(EresseaFileFilter.GZ_FILTER));
      extensions.add(getExtension(EresseaFileFilter.BZ2_FILTER));
      break;
    case ALLCR_COMPRESSED_FILTER:
      extensions.add(getExtension(EresseaFileFilter.ZIP_FILTER));
      extensions.add(getExtension(EresseaFileFilter.GZ_FILTER));
      extensions.add(getExtension(EresseaFileFilter.BZ2_FILTER));
      break;
    default:
      throw new IllegalArgumentException("Unsupported filter type");
    }
    description = getDescription(flag);
  }

  /**
   * Creates a new EresseaFileFilter object.
   * 
   * @param ext Only files with this extension will be accepted by this filter
   * @param desc A description to identify for this filter
   */
  public EresseaFileFilter(String ext, String desc) {
    extensions = new LinkedList<String>();
    extensions.add(ext);
    description = desc;
  }

  /**
   * Creates a new EresseaFileFilter object.
   * 
   * @param ext A List of Strings. Only files with these extensions will be accepted by this filter
   * @param desc A description to identify for this filter
   */
  public EresseaFileFilter(List<String> ext, String desc) {
    extensions = new LinkedList<String>(ext);
    description = desc;
  }

  /**
   * Append an appropriate extension to a file.
   * 
   * @param aFile
   * @return A File with the filename extended by the current extension
   */
  public File addExtension(File aFile) {
    return accept(aFile) ? aFile : new File(aFile.getPath() + getExtension());
  }

  /**
   * Returns <code>true</code> iff this file is accepted by this filter.
   * 
   * @param f Any File
   * @return <code>true</code> iff this file is accepted
   */
  @Override
  public boolean accept(File f) {
    if (f.isDirectory())
      return true;
    for (String ext : extensions) {
      if (f.getName().toLowerCase().endsWith(ext))
        return true;
    }
    return false;
  }

  private String getExtension(int flag) {
    return "." + Resources.get("eresseafilefilter.defaults.extension." + flag).toLowerCase();
  }

  private String getExtension() {
    if (extensions.isEmpty())
      return null;
    return extensions.get(0);
  }

  /**
   * Returns the current description.
   * 
   * @return The description of this filter
   */
  @Override
  public String getDescription() {
    return description;
  }

  /**
   * Returns the description for the flag.
   * 
   * @param flag
   * @return The appropriate description
   */
  protected String getDescription(int flag) {
    String retVal = "";
    retVal = Resources.get("eresseafilefilter.defaults.description." + flag);
    if (retVal == null) {
      retVal = "unknown";
    }
    if (!extensions.isEmpty()) {
      retVal += " (";
    }
    for (Iterator<String> it = extensions.iterator(); it.hasNext();) {
      retVal += "*" + it.next();
      if (it.hasNext()) {
        retVal += ", ";
      } else {
        retVal += ")";
      }
    }
    return retVal;
  }

}
