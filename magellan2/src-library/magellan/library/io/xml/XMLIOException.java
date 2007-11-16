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

package magellan.library.io.xml;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * This class encapsulates some occuring exceptions as IOException.
 */
public class XMLIOException extends IOException {
	private Exception exception;
	private static final String EXCEPTION_SEPARATOR = "______________ORIGINAL EXCEPTION____________";

	/**
	 * Creates a new XMLIOException object.
	 *
	 * 
	 */
	public XMLIOException(String aMessage) {
		super(aMessage);
	}

	/**
	 * Creates a new XMLIOException object.
	 *
	 * 
	 */
	public XMLIOException(Exception exception) {
		super(exception.getMessage());
		this.exception = exception;
	}

	/**
	 * DOCUMENT-ME
	 */
	public void printStackTrace() {
		printStackTrace(System.err);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void printStackTrace(PrintStream aTarget) {
		synchronized(aTarget) {
			super.printStackTrace(aTarget);

			if(exception != null) {
				aTarget.println(EXCEPTION_SEPARATOR);
				exception.printStackTrace(aTarget);
			}
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void printStackTrace(PrintWriter aTarget) {
		synchronized(aTarget) {
			super.printStackTrace(aTarget);

			if(exception != null) {
				aTarget.println(EXCEPTION_SEPARATOR);
				exception.printStackTrace(aTarget);
			}
		}
	}
}
