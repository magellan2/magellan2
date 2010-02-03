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

/*
 * ListReplacer.java
 *
 * Created on 19. Mai 2002, 11:39
 */
package magellan.library.utils.replacers;

import java.text.NumberFormat;
import java.util.Iterator;
import java.util.List;

/**
 * DOCUMENT ME!
 *
 * @author Andreas
 * @version 1.0
 */
public class ListReplacer implements Replacer {
	protected StringBuffer buffer;
	protected List<?> list;
	protected String unknown;
	protected String evolved = null;
	protected static NumberFormat numberFormat;

	/**
	 * Creates new ListReplacer
	 *
	 * 
	 * 
	 */
	public ListReplacer(List<?> list, String unknown) {
		buffer = new StringBuffer();
		this.list = list;
		this.unknown = unknown;

		if(ListReplacer.numberFormat == null) {
			try {
				ListReplacer.numberFormat = NumberFormat.getInstance(magellan.library.utils.Locales.getGUILocale());
			} catch(IllegalStateException ise) {
				ListReplacer.numberFormat = NumberFormat.getInstance();
			}

			ListReplacer.numberFormat.setMaximumFractionDigits(2);
			ListReplacer.numberFormat.setMinimumFractionDigits(0);
		}

		if(list == null) {
			evolved = "";
		} else {
			Iterator it = list.iterator();
			boolean canEvolve = true;

			while(canEvolve && it.hasNext()) {
				canEvolve = !(it.next() instanceof Replacer);
			}

			if(canEvolve) {
				evolved = (String) getReplacement(null);
			}
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public String getDescription() {
		return "list replacer";
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public Object getReplacement(Object o) {
		if(evolved != null) {
			return evolved;
		}

		//try{
		if(list == null) {
			return null;
		}

		buffer.setLength(0);

		Iterator it = list.iterator();

		while(it.hasNext()) {
			Object obj = it.next();

			if(obj instanceof Replacer) {
				obj = ((Replacer) obj).getReplacement(o);
			}

			if(obj == null) {
				buffer.append(unknown);
			} else {
				if(obj instanceof Number) {
					buffer.append(ListReplacer.numberFormat.format(obj));
				} else {
					buffer.append(obj.toString());
				}
			}
		}

		String str = buffer.toString();
		buffer.setLength(0);

		return str;

		//}catch(Exception exc) {}
		//return null;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	@Override
  public String toString() {
		StringBuffer buf = new StringBuffer();
		Iterator it = list.iterator();

		while(it.hasNext()) {
			buf.append(it.next());
		}

		if(evolved != null) {
			buf.append("(Evolved to: ");
			buf.append(evolved);
			buf.append(")");
		}

		return buf.toString();
	}
}
