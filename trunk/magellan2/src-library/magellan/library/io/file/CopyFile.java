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

/**
 * Handles backup generation for files.
 */
public class CopyFile {
	/**
	 * Disable instantiation of class
	 */
	private CopyFile() {
	}

	/**
	 * Copies the given source file to a temporary file.
	 *
	 * @param source source file.
	 *
	 * 
	 *
	 * @throws IOException if an I/O error occured.
	 */
	public static synchronized File copy(File source) throws IOException {
		return copy(source, createTempFile());
	}

	/**
	 * Copies the given source file to the given destination.
	 *
	 * @param source source file.
	 * @param target destination target file.
	 *
	 * 
	 *
	 * @throws IOException if an I/O error occured.
	 */
	public static synchronized File copy(File source, File target) throws IOException {
		copyStreams(new FileInputStream(source), new FileOutputStream(target));
		target.setLastModified(source.lastModified());

		return target;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	public static synchronized File createCrTempFile() throws IOException {
		return createTempFile("magellan", ".tmp.cr");
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	public static synchronized File createTempFile() throws IOException {
		return createTempFile("magellan");
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	public static synchronized File createTempFile(String prefix) throws IOException {
		return createTempFile(prefix, null);
	}

	private static File createTempFile(String prefix, String suffix) throws IOException {
		File tempfile = File.createTempFile(prefix, suffix);
		tempfile.deleteOnExit();

		return tempfile;
	}

	/** Copies the given source inputstream to the given destination outputstream. */
	private static final int BUFF_SIZE = 100000;


	public static synchronized void copyStreams(InputStream source, OutputStream target) throws IOException {
		copyStreams(source, target, true);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * @throws IOException DOCUMENT-ME
	 */
	public static synchronized void copyStreams(InputStream source, OutputStream target, boolean closeStreams)
										 throws IOException
	{
		InputStream in = null;
		OutputStream out = null;
		byte buffer[] = new byte[BUFF_SIZE];

		try {
			// encapsulate into BufferedInputStream if necessary
			try {
				in = (BufferedInputStream) source;
			} catch(ClassCastException e) {
				in = new BufferedInputStream(source);
			}

			// encapsulate into BufferedInputStream if necessary
			try {
				out = (BufferedOutputStream) target;
			} catch(ClassCastException e) {
				out = new BufferedOutputStream(target);
			}

			int count = 0;

			do {
				out.write(buffer, 0, count);
				count = in.read(buffer, 0, buffer.length);
			} while(count != -1);
			
		} finally {
			if(closeStreams) {
				if(in != null) {
					try {
						in.close();
					} catch(IOException e) {
					}
				}
				
				if(out != null) {
					try {
						out.close();
					} catch(IOException e) {
					}
				}
			} else {
				out.flush();
			}
		}
	}
}
