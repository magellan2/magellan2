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
 * Copyright (c) 2003 Sun Microsystems, Inc. All  Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * -Redistributions of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *
 * -Redistribution in binary form must reproduct the above copyright
 *  notice, this list of conditions and the following disclaimer in
 *  the documentation and/or other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT
 * BE LIABLE FOR ANY DAMAGES OR LIABILITIES SUFFERED BY LICENSEE AS A RESULT
 * OF OR RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THE SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE SOFTWARE, EVEN
 * IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that Software is not designed, licensed or intended for
 * use in the design, construction, operation or maintenance of any nuclear
 * facility.
 */
/*
 * @(#)TableMap.java    1.11 03/01/23
 */

/**
 * In a chain of data manipulators some behaviour is common. TableMap provides most of this
 * behavour and can be subclassed by filters that only need to override a handful of specific
 * methods. TableMap  implements TableModel by routing all requests to its model, and
 * TableModelListener by routing all events to its listeners. Inserting  a TableMap which has not
 * been subclassed into a chain of table filters  should have no effect.
 */
package magellan.client.swing.table;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

/**
 * DOCUMENT-ME
 *
 * @author $Author: $
 * @version $Revision: 171 $
 */
public class TableMap extends AbstractTableModel implements TableModelListener {
	protected TableModel model;

	private TableMap(){
	  super();
	}
	
	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public TableModel getModel() {
		return model;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setModel(TableModel model) {
		this.model = model;
		model.addTableModelListener(this);
	}

	// By default, Implement TableModel by forwarding all messages 
	// to the model. 
	public Object getValueAt(int aRow, int aColumn) {
		return model.getValueAt(aRow, aColumn);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 * 
	 */
	public void setValueAt(Object aValue, int aRow, int aColumn) {
		model.setValueAt(aValue, aRow, aColumn);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public int getRowCount() {
		return (model == null) ? 0 : model.getRowCount();
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public int getColumnCount() {
		return (model == null) ? 0 : model.getColumnCount();
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public String getColumnName(int aColumn) {
		return model.getColumnName(aColumn);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public Class<?> getColumnClass(int aColumn) {
		return model.getColumnClass(aColumn);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
	 */
	public boolean isCellEditable(int row, int column) {
		return model.isCellEditable(row, column);
	}

	//
	// Implementation of the TableModelListener interface, 
	//
	// By default forward all events to all the listeners. 
	public void tableChanged(TableModelEvent e) {
		fireTableChanged(e);
	}
}
