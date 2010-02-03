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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import magellan.client.swing.context.ContextChangeable;
import magellan.client.swing.context.ContextObserver;
import magellan.library.Faction;
import magellan.library.Item;
import magellan.library.Skill;
import magellan.library.Unit;
import magellan.library.rules.Category;
import magellan.library.utils.Resources;
import magellan.library.utils.comparator.SkillComparator;
import magellan.library.utils.comparator.SkillRankComparator;


/**
 * A UnitNodeWrapper serves as an abstraction layer between a tree cell renderer and the unit to
 * render. It manages a list of icons and text(s) that are to be displayed. It maintains a 
 * {@link UnitNodeWrapperDrawPolicy} adapter which governs many of the object's properties.
 *
 * @author $Author: $
 * @version $Revision: 288 $
 */
public class UnitNodeWrapper extends EmphasizingImpl implements CellObject2, SupportsClipboard {
	private static final Comparator<Skill> skillComparator = new SkillComparator();
	private static Comparator<Skill> rankComparator = null;

	private static final String SKILL_CHANGE_STYLE_PREFIX = "Talent";
	private Unit unit = null;
	private int amount = -1;
  private int modified = -1;
  private String prfx = null;
	private String text = null;
	private boolean iconNamesCreated = false;
	private List<GraphicsElement> iconNames = null;
	private Boolean reverse;
	private String additionalIcon = null;
	private UnitNodeWrapperDrawPolicy adapter;

	/**
	 * Creates a new UnitNodeWrapper object. The text for the  unit is generated from the unit's name
	 * and ID and from the arguments.
	 *
	 * @param u The unit
	 * @param prfx A text that is displayed in front of the name
	 * @param num The number of persons
	 * @param mod The modified number of persons
	 */
	public UnitNodeWrapper(Unit u, String prfx, int num, int mod) {
		this.unit = u;
		this.amount = num;
        this.modified = mod;
        this.prfx = prfx;
		//this.text = getText(u, prfx, num, mod);
	}

	/**
	 * Creates a new UnitNodeWrapper with specified text.
	 *
	 * @param u The unit
	 * @param text The text that is displayed for the unit
	 */
	public UnitNodeWrapper(Unit u, String text) {
		unit = u;
		this.text = text;
	}

	/**
	 * Returns the unit represented by the wrapper. 
	 */
	public Unit getUnit() {
		return unit;
	}

	/**
	 * Returns a string representation of the object, which is either pre-set by the constructor or
	 * generated from the unit name, id, amount and modified amount (if applicable).
	 */
	@Override
  public String toString() {
		return text != null ? text : UnitNodeWrapper.getText(unit,prfx, amount, modified);
	}

	/**
	 * Specifies an additional icon that is displayed in front of the unit text.
	 * 
	 */
	public void setAdditionalIcon(String icon) {
		additionalIcon = icon;
	}

	/**
	 * Returns <code>null</code>.
	 * 
	 * @see magellan.client.swing.tree.CellObject#getIconNames()
	 */
	public List<String> getIconNames() {
		return null;
	}

	/**
	 * Returns <code>true</code> if the unit belongs to a priveleged faction and the unit's orders or 
	 * one of the subordinate element's orders are unconfirmed.
	 *
	 * @see magellan.client.swing.tree.CellObject#emphasized()
	 */
	@Override
  public boolean emphasized() {
		Faction f = unit.getFaction();

		if((f != null) && f.isPrivileged()) {
			if(!unit.isOrdersConfirmed()) {
				return true;
			}

			for(SupportsEmphasizing se : getSubordinatedElements()) {
			  if(se.emphasized()) {
			    return true;
			  }
			}
		}

		return false;
	}

	/**
   * Returns the accordant option of the draw policy.
	 */
	public boolean isShowingAdditional() {
		return adapter.properties[adapter.SHOW_ADDITIONAL];
	}

	/**
   * Returns the accordant option of the draw policy.
	 * 
	 */
	public boolean isShowingContainerIcons() {
		return isShowingAdditional() && adapter.properties[adapter.SHOW_CONTAINER];
	}

	/**
   * Returns the accordant option of the draw policy.
	 * 
	 */
	public boolean isShowingSkillIcons() {
		return isShowingAdditional() && adapter.properties[adapter.SHOW_SKILL];
	}

	/**
   * Returns the accordant option of the draw policy.
	 * 
	 */
	public boolean isShowingOtherIcons() {
		return isShowingAdditional() && adapter.properties[adapter.SHOW_OTHER];
	}

	/**
   * Returns the accordant option of the draw policy.
	 * 
	 */
	public boolean isShowingIconText() {
		return isShowingAdditional() && adapter.properties[adapter.SHOW_TEXT];
	}

	/**
	 * Return <code>true</code> iff skills with level less than one should be shown.
	 * 
	 * @return <code>true</code> iff skills with level less than one should be shown
	 */
	public boolean isShowingSkillsLessThanOne() {
		return isShowingAdditional() && adapter.properties[adapter.SHOW_SKILL_LESS_ONE];
	}

  /**
   * Return <code>true</code> iff skills with level less than two should be shown.
   * 
   * @return <code>true</code> iff skills with level less than two should be shown
   */
  public boolean isShowingSkillsLessThanTwo() {
    return isShowingAdditional() && adapter.properties[adapter.SHOW_SKILL_LESS_TWO];
  }
  
  /**
   * Returns the maximum number of skill icons that should be shown.
   * 
   * @return The maximum number of skill icons that should be shown.
   */
  public int numberOfShownSkills() {
    if  (!adapter.properties[adapter.NUMBER_OF_SHOWN_SKILLS])
      return Integer.MAX_VALUE;
    
    for (int i=1; i<=5; ++i){
      if (!adapter.properties[adapter.NUMBER_OF_SHOWN_SKILLS+i])
        return i-1;
    }
    return 5;
  }
  
	/**
   * Returns the accordant option of the draw policy.
	 * 
	 */
	public boolean isShowingExpectedOnly() {
		return isShowingAdditional() && adapter.properties[adapter.SHOW_EXPECTED_ONLY];
	}

	/**
   * Returns the accordant option of the draw policy.
	 * 
	 */
	public boolean isShowingChanges() {
		return adapter.properties[adapter.SHOW_CHANGES];
	}

	/**
   * Returns the accordant option of the draw policy.
	 * 
	 */
	public boolean isShowingChangesStyled() {
		return isShowingChanges() && adapter.properties[adapter.SHOW_CHANGE_STYLED];
	}

	/**
   * Returns the accordant option of the draw policy.
	 * 
	 */
	public boolean isShowingChangesText() {
		return isShowingChanges() && adapter.properties[adapter.SHOW_CHANGE_TEXT];
	}

	/**
   * Returns the accordant option of the draw policy.
	 * 
	 */
	public boolean isShowingCategorized() {
		return adapter.properties[adapter.SHOW_CATEGORIZED];
	}

	/**
   * Returns the accordant option of the draw policy.
	 *
	 * 
	 */
	public boolean isShowingCatagorized(int type) {
		return adapter.properties[adapter.CATEGORIZE_START + type];
	}

	/**
	 * Generates a text which is composed of the prefix, the units name and id, and amount1 and 
	 * amount2.
	 * 
	 * @param u
	 * @param prefix
	 * @param amount1
	 * @param amount2
	 * @return
	 */
	private static String getText(Unit u, String prefix, int amount1, int amount2) {
		StringBuffer sb = new StringBuffer();

		if(prefix != null) {
			sb.append(prefix);
		}

		sb.append(u.toString());

		if(amount1 > -1) {
			sb.append(": ").append(amount1);

			if((amount2 > -1) && (amount2 != amount1)) {
				sb.append(" (").append(amount2).append(")");
			}
		}

		return sb.toString();
	}

	private List<GraphicsElement> createGraphicsElements(Unit u) {
		List<GraphicsElement> names  = new ArrayList<GraphicsElement>();
		List<Skill> skills = new LinkedList<Skill>();

		if(isShowingSkillIcons() && (u.getSkills() != null)) {
			boolean bool = isShowingSkillsLessThanOne();
      boolean bool2 = isShowingSkillsLessThanTwo();
			for(Iterator<Skill> iter = u.getSkills().iterator(); iter.hasNext();) {
				Skill s = iter.next();
				boolean addSkill=true;
        if (s.getLevel()<1 && !bool){
          addSkill=false;
        }
        if (s.getLevel()<2 && !bool2){
          addSkill=false;
        }
				if(addSkill) {
					skills.add(s);
				}
			}

			if(adapter.getSettings().getProperty("EMapOverviewPanel.useBestSkill", "true")
						  .equalsIgnoreCase("true")) {
				// use best skill to sort icons
				Collections.sort(skills, UnitNodeWrapper.skillComparator);
			} else {
				// use skilltyperanking to sort icons
				Collections.sort(skills, UnitNodeWrapper.rankComparator);
			}
		}

		Collection<Item> others = null;

		if(isShowingOtherIcons()) {
			others = new ArrayList<Item>(u.getModifiedItems());
		}

		// main
		Tag2Element.start(u);

		GraphicsElement start = new UnitGraphicsElement(toString());
		start.setType(GraphicsElement.MAIN);

		if(additionalIcon != null) {
			start.setImageName(additionalIcon);
		}

		Tag2Element.apply(start);

		GraphicsElement ge = null;

		// Fiete Test: temps, die gefaellt werden..haben geburtstag
		// Fiete removed 20060911 (creator wishes to use the icon for the item only
		/**
		if (u.getPersons()==0 && u.getModifiedPersons()>0){
			names.add(new GraphicsElement(null, null, "geburtstag"));
		}
		*/
		
		if(isShowingContainerIcons()) {
			if(unit.getBuilding() != null && unit.getBuilding().getType() != null) {
				ge = new GraphicsElement(null, null, unit.getBuilding().getType().getID().toString());
				ge.setTooltip(unit.getBuilding().getName());
				ge.setType(GraphicsElement.ADDITIONAL);
				names.add(ge);
			}

			if(unit.getShip() != null) {
			  if (unit.getShip().getType()==null) {
          ge = new GraphicsElement(null, null, "ERROR");
        } else{
			    ge = new GraphicsElement(null, null, unit.getShip().getType().getID().toString());
			    ge.setTooltip(unit.getShip().getName());
			    ge.setType(GraphicsElement.ADDITIONAL);
			  }
				names.add(ge);
			}
		}

    // Heldenanzeige
    if(u.isHero()) {
      names.add(new GraphicsElement(null, null, "hero"));
    }

    // skills
    int skillCounter = 0;
		for(Iterator<Skill> iter = skills.iterator(); iter.hasNext() && skillCounter < numberOfShownSkills();) {
			Skill s = iter.next();
			skillCounter++;
			ge = null;

			if(isShowingIconText()) {
				ge = new GraphicsElement(new Integer(s.getLevel()), null, null,
										 s.getSkillType().getID().toString());

				if(s.isLostSkill()) {
					ge.setObject("-");
				}
			} else {
				ge = new GraphicsElement(null, null, s.getSkillType().getID().toString());
			}

			ge.setTooltip(s.getSkillType().getName());
			ge.setType(GraphicsElement.ADDITIONAL);

			if(isShowingChanges() && s.isLevelChanged()) {
				if(isShowingChangesStyled()) {
					ge.setStyleset(UnitNodeWrapper.SKILL_CHANGE_STYLE_PREFIX +
								   ((s.getChangeLevel() >= 0) ? ">." : "<.") +
								   UnitNodeWrapper.SKILL_CHANGE_STYLE_PREFIX + String.valueOf(s.getChangeLevel()));
				}

				if(isShowingChangesText() && isShowingIconText()) {
					ge.setObject(ge.getObject().toString() + "(" +
								 ((s.getChangeLevel() >= 0) ? "+" : "") +
								 String.valueOf(s.getChangeLevel()) + ")");
				}
			} else {
				Tag2Element.apply(ge);
			}

			names.add(ge);
		}

		// items
		if(others != null) {
			if(isShowingCategorized()) {
				ArrayList<List<Item>> categories = new ArrayList<List<Item>>(adapter.NUMBER_OF_CATEGORIES); 
				boolean anything = false;

				for(int i = 0; i < adapter.NUMBER_OF_CATEGORIES; i++) {
					if(isShowingCatagorized(i)) {
	          categories.add(new LinkedList<Item>());
						anything = true;
					} else {
					  categories.add(null);
					}
				}

				if(anything) {
					for(Iterator<Item> it = others.iterator(); it.hasNext();) {
						Item item = it.next();

						try {
							String cat = item.getItemType().getCategory().getID().toString();

							int j = -1;

							for(int i = 0; i < adapter.NUMBER_OF_CATEGORIES; i++) {
								if(adapter.categories[i].equals(cat) && isShowingCatagorized(i)) {
									j = i;
								}
							}

							if(j != -1) {
								it.remove();
								categories.get(j).add(item);
							}
						} catch(Exception exc) {
						}
					}

					StringBuffer buffer = new StringBuffer();

					for(int i = 0; i < adapter.NUMBER_OF_CATEGORIES; i++) {
						if(categories.get(i) != null) {

						  int count = 0;
							buffer.setLength(0);
							Item item = null;

							for(Iterator<Item> it  = categories.get(i).iterator(); it.hasNext();) {
								item = it.next();
								buffer.append(item.getAmount());
								buffer.append(' ');
								buffer.append(item.getName());

								if(it.hasNext()) {
									buffer.append(',');
								}

								count += item.getAmount();
							}

							if(item !=null && (count > 0 || !isShowingExpectedOnly())) {
							  Category catP = item.getItemType().getCategory();
							  while (catP.getParent()!=null){
							    catP = catP.getParent();
							  }
							  String iconName = magellan.library.utils.Umlaut
                   .convertUmlauts(catP.getName());
							  
							  if (categories.get(i).size()==1){
							    iconName = "items/" + item.getItemType().getIconName();
							  }
							  /**
								if(isShowingIconText()) {
									ge = new GraphicsElement(new Integer(count), null, null,
															 "items/" +
															 item.getItemType().getIconName());
								} else {
									ge = new GraphicsElement(null, null,
															 "items/" +
															 item.getItemType().getIconName());
								}
								*/
								if(isShowingIconText()) {
                  ge = new GraphicsElement(new Integer(count), null, null,
                      iconName);
                } else {
                  ge = new GraphicsElement(null, null,
                      iconName);
                }
								ge.setTooltip(buffer.toString());
								ge.setType(GraphicsElement.ADDITIONAL);
								Tag2Element.apply(ge);
								names.add(ge);
							}

							categories.set(i, null);
						}
					}

					buffer = null;
				}

				categories = null;
			}

			for(Iterator<Item> iter = others.iterator(); iter.hasNext();) {
				Item s = iter.next();

				if(isShowingExpectedOnly()) {
					if(s.getAmount() <= 0) {
						// skip "empty" items
						continue;
					}
				}

				ge = null;

				if(isShowingIconText()) {
					ge = new GraphicsElement(null, null, null,
											 "items/" + s.getItemType().getIconName());

					Item oldItem = u.getItem(s.getItemType());
					int oldAmount = 0;

					if(oldItem != null) {
						oldAmount = oldItem.getAmount();
					}

					if(oldAmount != s.getAmount()) {
						if(isShowingExpectedOnly()) {
							// only show expected future value
							ge.setObject(String.valueOf(s.getAmount()));
						} else {
							ge.setObject(String.valueOf(oldAmount) + "(" +
										 String.valueOf(s.getAmount()) + ")");
						}
					} else {
						if(oldAmount == 0) {
							continue;
						}

						ge.setObject(new Integer(oldAmount));
					}
				} else {
					ge = new GraphicsElement(null, null, "items/" + s.getItemType().getIconName());
				}

				ge.setTooltip(s.getName());
				ge.setType(GraphicsElement.ADDITIONAL);
				Tag2Element.apply(ge);
				names.add(ge);
			}
		}

		if(reverseOrder()) {
			Collections.reverse(names);
		}

		names.add(0, start);

		// ease garbage collection
		skills.clear();

		//others.clear();
		return names;
	}

	/**
	 * Specifies if the icons should be displayed in reverse order. This overrides the settings
	 * if the {@link UnitNodeWrapperDrawPolicy}.
	 * 
	 * @param bool
	 */
	public void setReverseOrder(boolean bool) {
		reverse = bool ? Boolean.TRUE : Boolean.FALSE;
	}

	/**
	 * Returns <code>true</code> if the icons should be displayed in reverse order. 
	 */
	public boolean reverseOrder() {
		if(reverse != null) {
			return reverse.booleanValue();
		}

		return adapter.properties[adapter.SHOW_NAMEFIRST];
	}

	/**
	 * Clears the collection of icons and indicates that it should be re-created.
	 */
	public void clearBuffer() {
		if(iconNames != null) {
			iconNames.clear();
			iconNames = null;
		}

		iconNamesCreated = false;
	}

	/**
	 * Indicates that the list icons should be re-created 
	 * 
	 * @see magellan.client.swing.tree.CellObject#propertiesChanged()
	 */
	public void propertiesChanged() {
	  clearBuffer();
	}

	/**
   * DOCUMENT-ME
	 *  
	 * @see magellan.client.swing.tree.CellObject#init(java.util.Properties, magellan.client.swing.tree.NodeWrapperDrawPolicy)
	 */
	public NodeWrapperDrawPolicy init(Properties settings, NodeWrapperDrawPolicy newAdapter) {
		return init(settings, "UnitNodeWrapper", newAdapter);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * @see magellan.client.swing.tree.CellObject#init(java.util.Properties, java.lang.String, magellan.client.swing.tree.NodeWrapperDrawPolicy)
	 */
	public NodeWrapperDrawPolicy init(Properties settings, String prefix,
									  NodeWrapperDrawPolicy newAdapter) {
		// return the adapter
		if(newAdapter == null) {
			newAdapter = new UnitNodeWrapperDrawPolicy(settings, prefix);
		}

		newAdapter.addCellObject(this);
		this.adapter = (UnitNodeWrapperDrawPolicy) newAdapter;

		if(UnitNodeWrapper.rankComparator == null) {
			UnitNodeWrapper.rankComparator = new SkillRankComparator(null, settings);
		}

		return newAdapter;
	}

	/**
	 * Returns the list of of {@link GraphicsElement}s that should be displayed. 
	 *
	 * 
	 * @see magellan.client.swing.tree.CellObject2#getGraphicsElements()
	 */
	public List<GraphicsElement> getGraphicsElements() {
		if(!iconNamesCreated) {
			this.iconNames = createGraphicsElements(this.unit);
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

		/** option for showing only first x skills */
		public final int NUMBER_OF_SHOWN_SKILLS = 9;
    
    public final int NUMBER_OF_SHOWN_SKILLS_START = 10;
    
    public final int NUMBER_OF_SHOWN_SKILLS_END = 14;
    
		/** DOCUMENT-ME */
		public final int SHOW_CHANGES = 15;

		/** DOCUMENT-ME */
		public final int SHOW_CHANGE_STYLED = 16;

		/** DOCUMENT-ME */
		public final int SHOW_CHANGE_TEXT = 17;

		/** DOCUMENT-ME */
		public final int SHOW_CATEGORIZED = 18;

    /** DOCUMENT-ME */
		public final int CATEGORIZE_START = 19;

    public final int NUMBER_OF_CATEGORIES = 7;

		
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
			super(4, new int[] { 8, 5, 2, 7}, settings, prefix,
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
					  
            { "showHighest", "false" },
            { "showHighest.1", "true" },
            { "showHighest.2", "true" },
            { "showHighest.3", "false" },
            { "showHighest.4", "false" },
            { "showHighest.5", "false" },

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
					  
//			{ "showWarnings", "false" }
				  },
				  new String[] {
					  "prefs.additional.text", "prefs.container.text", "prefs.skill.text",
					  "prefs.skilllessthanone.text", "prefs.skilllessthantwo.text", 
					  "prefs.other.text", "prefs.icontext.text",
					  "prefs.nfirst.text", "prefs.showExpectedOnly",
					  
            "prefs.showhighest.text", "prefs.showhighest.1", "prefs.showhighest.2",
              "prefs.showhighest.3", "prefs.showhighest.4", "prefs.showhighest.5",

            "prefs.changes.text", "prefs.changes.mode0.text", "prefs.changes.mode1.text",
					  
            "prefs.categorized.text", "prefs.categorized.0", "prefs.categorized.1",
					  "prefs.categorized.2", "prefs.categorized.3", "prefs.categorized.4",
					  "prefs.categorized.5", "prefs.categorized.6",
					  
					  "prefs.showWarnings"
				  }, 4, "tree.unitnodewrapper.");

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
		 * 
		 * @see magellan.client.swing.tree.AbstractNodeWrapperDrawPolicy#applyPreferences()
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
		 * @see magellan.client.swing.context.ContextChangeable#getContextAdapter()
		 */
		public JMenuItem getContextAdapter() {
			return contextMenu;
		}

		/**
		 * DOCUMENT-ME
		 *
		 * @see magellan.client.swing.context.ContextChangeable#setContextObserver(magellan.client.swing.context.ContextObserver)
		 */
		public void setContextObserver(ContextObserver co) {
			obs = co;
		}

		/**
		 * DOCUMENT-ME
		 *
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
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
		 * @return
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
	 * 
	 *
	 * @see magellan.client.swing.tree.SupportsClipboard#getClipboardValue()
	 */
	public String getClipboardValue() {
		if(unit != null) {
			return unit.toString();
		} else {
			return toString();
		}
	}
}
