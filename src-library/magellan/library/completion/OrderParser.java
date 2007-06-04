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

package magellan.library.completion;

import java.io.Reader;
import java.util.List;

/**
 * DOCUMENT-ME
 *
 * @author $Author: $
 * @version $Revision: 171 $
 */
public interface OrderParser {
	/**
	 * Parses one line of text from the specified stream by tokenizing it and checking the syntax.
	 *
	 * @param in the stream to read the order from.
	 *
	 * @return <tt>true</tt> if the syntax of the order read is valid, <tt>false</tt> else.
	 */
	public boolean read(Reader in);

	/**
	 * Returns the tokens read by the parser.
	 *
	 * @return all <tt>OrderToken</tt> object produced by the underlying <tt>OrderTokenizer</tt> by
	 * 		   reading a order.
	 */
	public List getTokens();
}
