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

package magellan.library.utils.comparator;

import java.util.Comparator;

import magellan.library.Message;
import magellan.library.rules.MessageType;

/**
 * A comparator imposing an ordering on Message objects by comparing their types.
 * <p>
 * Note: this comparator imposes orderings that are inconsistent with equals. This is the case when
 * neither of the messages has a valid type set.
 * </p>
 */
public class MessageTypeComparator implements Comparator<Message> {
  protected Comparator<? super MessageType> typeCmp = null;

  /**
   * Creates a new MessageTypeComparator object.
   * 
   * @param typeComparator the comparator applied to compare the message types.
   * @throws NullPointerException DOCUMENT-ME
   */
  public MessageTypeComparator(Comparator<? super MessageType> typeComparator) {
    if (typeComparator == null)
      throw new NullPointerException();

    typeCmp = typeComparator;
  }

  /**
   * Compares its two arguments for order according to their types
   * 
   * @return the result specified message type comparator.
   */
  public int compare(Message o1, Message o2) {
    MessageType t1 = o1.getMessageType();
    MessageType t2 = o2.getMessageType();

    if (t1 == null)
      return (t2 == null) ? 0 : 1;
    else
      return (t2 == null) ? (-1) : typeCmp.compare(t1, t2);
  }
}
