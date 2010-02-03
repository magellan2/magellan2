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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

import org.apache.tools.bzip2.CBZip2InputStream;
import org.apache.tools.bzip2.CBZip2OutputStream;
/**
 * A <code>BZip2FileType</code> represents file compressed with bzip2.
 *
 * @author $Author: $
 * @version $Revision: 305 $
 */
public class BZip2FileType extends FileType {
	BZip2FileType(File aFile, boolean readonly) throws IOException {
		super(aFile, readonly);
	}

	/** 
	 * the tmpfile used for reading the BZIP2 content. Note that it will be nullified 
	 * if createOutputStream is called or if the garbage collector decides to. */
	private WeakReference<File> tmpfileRef;

	@Override
  protected InputStream createInputStream() throws IOException {
		// normally the following lines would be ok. But somehow it does not work, so we copy the content of the 
		// bzip2file into a tmpfile for reading with deleteonexit set.
		//return new CBZip2InputStream(new FileInputStream(fileName));
		File tmpfile = tmpfileRef != null ? (File) tmpfileRef.get() : null;
		if(tmpfile == null) {
			tmpfile = CopyFile.createTempFile();
			tmpfileRef = new WeakReference<File>(tmpfile);
			InputStream fis = new FileInputStream(filename);
			int magic3 = fis.read();
			int magic4 = fis.read();

			if((magic3 != 'B') || (magic4 != 'Z')) {
				throw new IOException("File " + filename + " is missing bzip2 header BZ.");
			}

			CopyFile.copyStreams(new CBZip2InputStream(new BufferedInputStream(fis)), new FileOutputStream(tmpfile));
		}

		return new FileInputStream(tmpfile);
	}

	@Override
  protected OutputStream createOutputStream() throws IOException {
		tmpfileRef = null;
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filename));
		bos.write('B');
		bos.write('Z');

		return new CBZip2OutputStream(bos);
	}
    
    /**
     * @see FileType#getInnerName()
     */
    @Override
    public String getInnerName() {
        return getName().substring(0,getName().lastIndexOf(FileType.BZIP2));
    }

}
