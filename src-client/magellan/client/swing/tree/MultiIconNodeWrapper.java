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

package magellan.client.swing.tree;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import magellan.client.swing.context.ContextChangeable;
import magellan.client.swing.context.ContextObserver;
import magellan.library.Skill;
import magellan.library.utils.Resources;


/**
 * A NodeWrapper that shows a list of icons with associated text.
 *
 * @author $Author: $
 * @version $Revision: 288 $
 */
public class MultiIconNodeWrapper extends EmphasizingImpl implements CellObject2, SupportsClipboard {

//  private int modified = -1;
//	private String text = null;

	private boolean iconNamesCreated = false;
	private List<GraphicsElement> iconNames = null;

//	private String additionalIcon = null;
	private UnitNodeWrapperDrawPolicy adapter;

//  private Object object;

  private String[] texts;

//  private Object[] icons;
//
//  private String clipboardValue;

	public MultiIconNodeWrapper(Object obj, String [] texts, Object [] icons, String clipboardValue) {
//		this.object = obj;
		this.texts = texts;
//		this.icons = icons;
//		this.clipboardValue = clipboardValue;
	}

	@Override
  public String toString() {
	  boolean first = true;
	  StringBuffer buffer = new StringBuffer();
		for (String text : texts){
		  buffer.append(text);
		  if (first){
		    first = false;
		  } else {
		    buffer.append(" ");
		  }
		}
		return buffer.toString();
	}

//	public void setAdditionalIcon(String icon) {
//		additionalIcon = icon;
//	}

	// we just don't support old style
	public List<String> getIconNames() {
		return null;
	}

	public boolean isShowingAdditional() {
		return adapter.properties[adapter.SHOW_ADDITIONAL];
	}

	private List<GraphicsElement> createGraphicsElements() {
		List<GraphicsElement> names  = new LinkedList<GraphicsElement>();
		List<Skill> skills = new LinkedList<Skill>();

//		for(Iterator iter = skills.iterator(); iter.hasNext();) {
//			Skill s = (Skill) iter.next();
//			ge = null;
//
//			if(isShowingIconText()) {
//				ge = new GraphicsElement(new Integer(s.getLevel()), null, null,
//										 s.getSkillType().getID().toString());
//
//				if(s.isLostSkill()) {
//					ge.setObject("-");
//				}
//			} else {
//				ge = new GraphicsElement(null, null, s.getSkillType().getID().toString());
//			}
//
//			ge.setTooltip(s.getSkillType().getName());
//			ge.setType(GraphicsElement.ADDITIONAL);
//
//			if(isShowingChanges() && s.isLevelChanged()) {
//				if(isShowingChangesStyled()) {
//					ge.setStyleset(MultiIconNodeWrapper.SKILL_CHANGE_STYLE_PREFIX +
//								   ((s.getChangeLevel() >= 0) ? ">." : "<.") +
//								   MultiIconNodeWrapper.SKILL_CHANGE_STYLE_PREFIX + String.valueOf(s.getChangeLevel()));
//				}
//
//				if(isShowingChangesText() && isShowingIconText()) {
//					ge.setObject(ge.getObject().toString() + "(" +
//								 ((s.getChangeLevel() >= 0) ? "+" : "") +
//								 String.valueOf(s.getChangeLevel()) + ")");
//				}
//			} else {
//				Tag2Element.apply(ge);
//			}
//
//			names.add(ge);
//		}
//
//
//		if(reverseOrder()) {
//			Collections.reverse(names);
//		}
//
//		names.add(0, start);

		// ease garbage collection
		skills.clear();

		return names;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setReverseOrder(boolean bool) {
//		reverse = bool ? Boolean.TRUE : Boolean.FALSE;
	  
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public boolean reverseOrder() {
//		if(reverse != null) {
//			return reverse.booleanValue();
//		}
//
//		return adapter.properties[adapter.SHOW_NAMEFIRST];
	  return false;
	}

	/**
	 * DOCUMENT-ME
	 */
	public void clearBuffer() {
		if(iconNames != null) {
			iconNames.clear();
			iconNames = null;
		}

		iconNamesCreated = false;
	}

	/**
	 * DOCUMENT-ME
	 */
	public void propertiesChanged() {
		if(iconNames != null) {
			iconNames.clear();
			iconNames = null;
		}

		iconNamesCreated = false;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
	 */
	public NodeWrapperDrawPolicy init(Properties settings, NodeWrapperDrawPolicy adapter) {
		return init(settings, "UnitNodeWrapper", adapter);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 * 
	 *
	 * 
	 */
	public NodeWrapperDrawPolicy init(Properties settings, String prefix,
									  NodeWrapperDrawPolicy adapter) {
		// return the adapter
		if(adapter == null) {
			adapter = new UnitNodeWrapperDrawPolicy(settings, prefix);
		}

		adapter.addCellObject(this);
		this.adapter = (UnitNodeWrapperDrawPolicy) adapter;


		return adapter;
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public List<GraphicsElement> getGraphicsElements() {
		if(!iconNamesCreated) {
			this.iconNames = createGraphicsElements();
			iconNamesCreated = true;
		}

		return iconNames;
	}

	private class UnitNodeWrapperDrawPolicy extends DetailsNodeWrapperDrawPolicy
		implements ContextChangeable, ActionListener
	{
		/** DOCUMENT-ME */
		public final int SHOW_ADDITIONAL = 0;

		/** DOCUMENT-ME */
		public final int SHOW_CONTAINER = 1;

		/** DOCUMENT-ME */
		public final int SHOW_SKILL = 2;

		/** DOCUMENT-ME */
		public final int SHOW_SKILL_LESS_ONE = 3;

    /** DOCUMENT-ME */
    public final int SHOW_SKILL_LESS_TWO = 4;
    
		/** DOCUMENT-ME */
		public final int SHOW_OTHER = 5;

		/** DOCUMENT-ME */
		public final int SHOW_TEXT = 6;

		/** DOCUMENT-ME */
		public final int SHOW_NAMEFIRST = 7;

		/** DOCUMENT-ME */
		public final int SHOW_EXPECTED_ONLY = 8;

		/** DOCUMENT-ME */
		public final int SHOW_CHANGES = 9;

		/** DOCUMENT-ME */
		public final int SHOW_CHANGE_STYLED = 10;

		/** DOCUMENT-ME */
		public final int SHOW_CHANGE_TEXT = 11;

		/** DOCUMENT-ME */
		public final int SHOW_CATEGORIZED = 12;

		/** DOCUMENT-ME */
		public final int CATEGORIZE_START = 13;

		/** DOCUMENT-ME */
		public final int SHOW_WARNINGS = 20;
		
		protected String categories[] = {
											"weapons", "armour", "resources", "luxuries", "herbs",
											"potions", "misc"
										};

		// for menu use
		protected ContextObserver obs;
		protected JMenu contextMenu;
		protected JCheckBoxMenuItem itemItem;
		protected JCheckBoxMenuItem skillItem;

		/**
		 * Creates a new UnitNodeWrapperDrawPolicy object.
		 *
		 * 
		 * 
		 * 
		 */
		public UnitNodeWrapperDrawPolicy(Properties settings, String prefix) {
			// super(5, new int[] {6, 2, 7, -1, -1}, settings, prefix,new String[][] {
			super(4, new int[] { 8, 2, 7, 0 }, settings, prefix,
				  new String[][] {
					  { "showAdditional", "true" },
					  { "showContainerIcons", "true" },
					  { "showSkillIcons", "true" },
					  { "showSkillLessThanOneIcons", "false" },
            { "showSkillLessThanTwoIcons", "true" },
					  { "showOtherIcons", "true" },
					  { "showIconText", "true" },
					  { "showNamesFirst", "false" },
					  { "showExpectedOnly", "false" },
					  
			{ "showChanges", "true" },
					  { "showChangesStyled", "true" },
					  { "showChangesText", "false" },
					  
			{ "showCategorized", "false" },
					  { "showCategorized.0", "false" },
					  { "showCategorized.1", "false" },
					  { "showCategorized.2", "false" },
					  { "showCategorized.3", "false" },
					  { "showCategorized.4", "false" },
					  { "showCategorized.5", "false" },
					  { "showCategorized.6", "false" },
					  
			{ "showWarnings", "false" }
				  },
				  new String[] {
					  "prefs.additional.text", "prefs.container.text", "prefs.skill.text",
					  "prefs.skilllessthanone.text", "prefs.skilllessthantwo.text", "prefs.other.text", "prefs.icontext.text",
					  "prefs.nfirst.text", "prefs.showExpectedOnly",
					  
			"prefs.changes.text", "prefs.changes.mode0.text", "prefs.changes.mode1.text",
					  
			"prefs.categorized.text", "prefs.categorized.0", "prefs.categorized.1",
					  "prefs.categorized.2", "prefs.categorized.3", "prefs.categorized.4",
					  "prefs.categorized.5", "prefs.categorized.6",
					  
			"prefs.showWarnings"
				  }, 0, "tree.unitnodewrapper.");

			// context menu
			contextMenu = new JMenu(Resources.get("tree.unitnodewrapper.prefs.title"));
			itemItem = new JCheckBoxMenuItem(Resources.get("tree.unitnodewrapper.prefs.other.text"), properties[SHOW_OTHER]);
			itemItem.addActionListener(this);
			contextMenu.add(itemItem);
			skillItem = new JCheckBoxMenuItem(Resources.get("tree.unitnodewrapper.prefs.skill.text"), properties[SHOW_SKILL]);
			skillItem.addActionListener(this);
			contextMenu.add(skillItem);
		}

		/**
		 * DOCUMENT-ME
		 */
		@Override
		public void applyPreferences() {
			// check warner
			//uWarning.getAdapter(null, null).applyPreferences();
			// update all wrappers
			super.applyPreferences();

			skillItem.setSelected(properties[SHOW_SKILL]);
			itemItem.setSelected(properties[SHOW_OTHER]);
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public JMenuItem getContextAdapter() {
			return contextMenu;
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public void setContextObserver(ContextObserver co) {
			obs = co;
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public void actionPerformed(ActionEvent e) {
			boolean changed = false;

			if(properties[SHOW_SKILL] != skillItem.isSelected()) {
				properties[SHOW_SKILL] = skillItem.isSelected();

				if((properties[SHOW_SKILL] && sK[SHOW_SKILL][1].equals("true")) ||
					   (!properties[SHOW_SKILL] && sK[SHOW_SKILL][1].equals("false"))) {
					settings.remove(prefix + "." + sK[SHOW_SKILL][0]);
				} else {
					settings.setProperty(prefix + "." + sK[SHOW_SKILL][0],
										 properties[SHOW_OTHER] ? "true" : "false");
				}

				changed = true;
			}

			if(properties[SHOW_OTHER] != itemItem.isSelected()) {
				properties[SHOW_OTHER] = skillItem.isSelected();

				if((properties[SHOW_OTHER] && sK[SHOW_OTHER][1].equals("true")) ||
					   (!properties[SHOW_OTHER] && sK[SHOW_OTHER][1].equals("false"))) {
					settings.remove(prefix + "." + sK[SHOW_OTHER][0]);
				} else {
					settings.setProperty(prefix + "." + sK[SHOW_OTHER][0],
										 properties[SHOW_OTHER] ? "true" : "false");
				}

				changed = true;
			}

			if(changed) {
				// update all wrappers
				applyPreferences();

				if(obs != null) {
					obs.contextDataChanged();
				}
			}
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public Properties getSettings() {
			return settings;
		}
	}

	protected class UnitGraphicsElement extends GraphicsElement {
		/**
		 * Creates a new UnitGraphicsElement object.
		 *
		 * 
		 * 
		 * 
		 * 
		 */
		public UnitGraphicsElement(Object o, Icon i, Image im, String s) {
			super(o, i, im, s);
		}

		/**
		 * Creates a new UnitGraphicsElement object.
		 *
		 * 
		 * 
		 * 
		 */
		public UnitGraphicsElement(Icon i, Image im, String s) {
			super(i, im, s);
		}

		/**
		 * Creates a new UnitGraphicsElement object.
		 *
		 * 
		 */
		public UnitGraphicsElement(Object o) {
			super(o);
		}

		/**
		 * 
		 */
    @Override
		public boolean isEmphasized() {
			return emphasized();
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public String getClipboardValue() {
	  return toString();
	}

}
