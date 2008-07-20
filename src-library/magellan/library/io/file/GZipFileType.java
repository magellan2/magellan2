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

package magellan.library.io.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * A <code>GZipFileType</code> represents a file compressed with gzip.
 *
 * @author $Author: $
 * @version $Revision: 305 $
 */
public class GZipFileType extends FileType {
	GZipFileType(File aFile, boolean readonly) throws IOException {
		super(aFile, readonly);
	}

	@Override
  protected InputStream createInputStream() throws IOException {
		return new GZIPInputStream(new FileInputStream(filename));
	}

	@Override
  protected OutputStream createOutputStream() throws IOException {
		return new GZIPOutputStream(new FileOutputStream(filename));
	}
    
    /**
     * @see FileType#getInnerName()
     */
    @Override
    public String getInnerName() {
        return getName().substring(0,getName().indexOf(FileType.GZIP));
    }
}
