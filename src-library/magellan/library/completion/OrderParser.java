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
import java.util.Locale;

import magellan.library.Order;
import magellan.library.utils.OrderToken;
import magellan.library.utils.OrderTokenizer;

/**
 * A class that parses and validates orders.
 */
public interface OrderParser {
  /**
   * Parses one line of text from the specified stream by tokenizing it and checking the syntax.
   * 
   * @param in the stream to read the order from.
   * @return <kbd>true</kbd> if the syntax of the order read is valid, <kbd>false</kbd> else.
   * @deprecated Should use {@link #parse(String, Locale)} instead
   */
  @Deprecated
  public boolean read(Reader in);

  /**
   * Returns the tokens read by the parser.
   * 
   * @return all <kbd>OrderToken</kbd> object produced by the underlying <kbd>OrderTokenizer</kbd> by
   *         reading a order.
   * @deprecated Should use {@link #parse(String, Locale)} instead
   */
  @Deprecated
  public List<OrderToken> getTokens();

  /**
   * Parses an order line and converts it into an Order object.
   * 
   * @param text One line of text
   * @param orderLocale The locale of the orders
   * @return An order object with line as text and the parsed token.
   */
  public Order parse(String text, Locale orderLocale);

  public OrderTokenizer getOrderTokenizer(Reader reader);

}
