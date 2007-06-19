/*
 * Created on 17.11.2004
 *
 */
package magellan.client.utils.logging;

import java.awt.BorderLayout;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import magellan.client.event.EventDispatcher;
import magellan.client.event.UnitOrdersEvent;
import magellan.client.event.UnitOrdersListener;
import magellan.client.swing.InternationalizedPanel;


/**
 * @author pavkovic
 *
 */
public class EventDisplayPanel extends InternationalizedPanel implements UnitOrdersListener {
	JTextArea area;
	EventDispatcher dispatcher;
	
	public EventDisplayPanel(EventDispatcher d) {
		super();
		init(d);
	}

	private void init(EventDispatcher d) {	
		dispatcher = d;
		d.addUnitOrdersListener(this);
		initGUI();
	}
	
	private void initGUI() {
		this.setLayout(new BorderLayout());
		
		area = new JTextArea();
		area.setText("");
		this.add(new JScrollPane(area),BorderLayout.CENTER);
	}
	
	public void unitOrdersChanged(UnitOrdersEvent e) {
		area.setText(area.getText()+e.toString()+'\n');
	}


  /**
  protected String getString(String key) {
    return Resources.get("utils.logging.eventdisplaypanel.",key);
  }
  */
}
