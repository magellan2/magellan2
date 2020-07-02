/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.library.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import magellan.library.GameData;
import magellan.library.Spell;
import magellan.library.StringID;
import magellan.library.gamebinding.EresseaConstants;
import magellan.library.rules.ItemType;
import magellan.library.utils.CollectionFactory;
import magellan.library.utils.Locales;
import magellan.library.utils.Resources;
import magellan.library.utils.SpellSyntax;
import magellan.library.utils.logging.Logger;

/**
 * Container class for a spell based on its representation in a cr version &ge; 42.
 */
public class MagellanSpellImpl extends MagellanDescribedImpl implements Spell {
  private static final Logger log = Logger.getInstance(MagellanSpellImpl.class);

  /** Standard spell component implementation. */
  public class Component implements Spell.Component {
    private String name;
    private int amount;
    private boolean leveldependant;

    public Component(String name, String ingredients) {
      this.name = name;
      try {
        amount = Integer.parseInt(ingredients.substring(0, ingredients.indexOf(" ")));
      } catch (Exception e) {
        MagellanSpellImpl.log.error(e.toString() + " unexpected spell component " + ingredients
            + ";" + name);
      }
      try {
        leveldependant = 1 == Integer.parseInt(ingredients.substring(ingredients.indexOf(" ") + 1));
      } catch (Exception e) {
        MagellanSpellImpl.log.error(e.toString() + " unexpected spell component " + ingredients
            + ";" + name);
      }
    }

    public int getAmount() {
      return amount;
    }

    public ItemType getItem() {
      if (name.equalsIgnoreCase(magellan.library.Spell.Component.AURA)
          || name.equalsIgnoreCase(magellan.library.Spell.Component.PERMANENT_AURA))
        return null;
      else
        return data.getRules().getItemType(name);
    }

    public String getName() {
      return name;
    }

    public boolean isLevelDependent() {
      return leveldependant;
    }

    @Override
    public String toString() {
      return amount + " " + name + (leveldependant ? "*Stufe" : "");
    }

  }

  private int blockID = -1; // this is the id of the ZAUBER block in the cr
  private int level = -1; // a mage's level has to be at least this value to be able to cast this
  // spell
  private int rank = -1;
  private String type = null; // represents the 'class' tag, can't be named like that, though
  private boolean onShip = false;
  private boolean onOcean = false;
  private boolean isFamiliar = false;
  private boolean isFar = false;

  private GameData data = null;

  private Map<String, String> components = null; // map of String objects
  private Locale locale = null;

  private String syntax = null; // FF 20070221 new CR tag syntax
  /**
   * the spellsytnax object
   */
  private SpellSyntax spellSyntax = null;
  private List<Component> parsedComponents;

  /**
   * Creates a new Spell object. CR looks as follows:
   *
   * <pre>
   * ZAUBER 1234567
   * "german name";name
   * "localized description";description
   * ...
   * ...
   * EINHEIT mage
   * ...
   * SPRUECHE
   * "german name"
   * ...
   * ...
   * TRANSLATION
   * "english name";german name
   * </pre>
   *
   * this means, spells are identified by their german names
   *
   * @param id This should currently be a {@link StringID}
   * @param _data
   */
  // FIXME(stm) I don't like this reference to GameData here
  public MagellanSpellImpl(StringID id, GameData _data) {
    super(id);
    data = _data;
  }

  /**
   * @see magellan.library.Unique#getID()
   */
  @Override
  public StringID getID() {
    return (StringID) super.getID(); // StringID.create(super.getName());
  }

  /**
   * Returns the integer serving as the block id in the cr.
   */
  public int getBlockID() {
    return blockID;
  }

  /**
   * Sets the integer serving as the block id in the cr.
   */
  public void setBlockID(int id) {
    blockID = id;
  }

  /**
   * Returns the level of this spell which indicates the lowest skill level a mage must have to be
   * able to cast this spell.
   */
  public int getLevel() {
    return level;
  }

  /**
   * Sets the level of this spell which indicates the lowest skill level a mage must have to be able
   * to cast this spell.
   */
  public void setLevel(int level) {
    this.level = level;
  }

  /**
   * @see magellan.library.Spell#getRank()
   */
  public int getRank() {
    return rank;
  }

  /**
   * @see magellan.library.Spell#setRank(int)
   */
  public void setRank(int rank) {
    this.rank = rank;
  }

  /**
   * Returns the class attribute of this spell.
   */
  public String getType() {
    return type;
  }

  /**
   * Sets the class attribute of this spell.
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * @see magellan.library.Spell#getOnOcean()
   */
  public boolean getOnOcean() {
    return onOcean;
  }

  /**
   * @see magellan.library.Spell#setOnOcean(boolean)
   */
  public void setOnOcean(boolean onOcean) {
    this.onOcean = onOcean;
  }

  /**
   * @see magellan.library.Spell#getOnShip()
   */
  public boolean getOnShip() {
    return onShip;
  }

  /**
   * @see magellan.library.Spell#setOnShip(boolean)
   */
  public void setOnShip(boolean onShip) {
    this.onShip = onShip;
  }

  /**
   * @see magellan.library.Spell#getIsFamiliar()
   */
  public boolean getIsFamiliar() {
    return isFamiliar;
  }

  /**
   * @see magellan.library.Spell#setIsFamiliar(boolean)
   */
  public void setIsFamiliar(boolean isFamiliar) {
    this.isFamiliar = isFamiliar;
  }

  /**
   * @see magellan.library.Spell#getIsFar()
   */
  public boolean getIsFar() {
    return isFar;
  }

  /**
   * @see magellan.library.Spell#setIsFar(boolean)
   */
  public void setIsFar(boolean _isFar) {
    isFar = _isFar;
  }

  /**
   * Returns the components of this spell as Strings.
   *
   * @see magellan.library.Spell#getComponents()
   */
  public Map<String, String> getComponents() {
    if (components == null) {
      components = CollectionFactory.<String, String> createSyncOrderedMap();
    }
    return components;
  }

  public List<? extends Spell.Component> getParsedComponents() {
    // if (parsedComponents==null){
    parsedComponents = new ArrayList<Component>(getComponents().size());
    for (String name : getComponents().keySet()) {
      parsedComponents.add(new Component(name, getComponents().get(name)));
    }
    // }
    return parsedComponents;
  }

  /**
   * @see magellan.library.Spell#setComponents(java.util.Map)
   */
  public void setComponents(Map<String, String> components) {
    this.components = components;
  }

  /**
   * Returns a string representation which looks like this: <tt>[N1 F S See ]</tt> denoting the
   * type, the level, and if this is a far, a ship or a sea spell.
   *
   * @see magellan.library.impl.MagellanNamedImpl#toString()
   */
  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer(getName());
    sb.append(" [");
    switch (getType() == null ? '?' : getType().charAt(2)) {
    // spell.combat = Kampfzauber
    case 'm':
      sb.append(Resources.get("spell.combat.short"));
      break;
    // spell.normal = Normaler Zauber
    case 'r':
      sb.append(Resources.get("spell.normal.short"));
      break;
    // spell.postcombat = Postkampfzauber
    case 's':
      sb.append(Resources.get("spell.postcombat.short"));
      break;
    // spell.precombat = Präkampfzauber
    case 'e':
      sb.append(Resources.get("spell.precombat.short"));
      break;
    default:
      sb.append(Resources.get("spell.unspecified.short"));
    }
    sb.append(getLevel() > 0 ? getLevel() : "-").append(" ");
    sb.append(" ").append(getIsFar() ? Resources.get("spell.far.short") : "-");
    sb.append(" ").append(getOnShip() ? Resources.get("spell.ship.short") : "-");
    sb.append(" ").append(getOnOcean() ? Resources.get("spell.see.short") : "-");
    sb.append("]");
    return sb.toString();
  }

  /**
   * @deprecated this may change if the constructor is changed to not include a reference to the
   *             GameData any more.
   */
  @Deprecated
  protected String getUnTranslatedName() {
    return super.getName();
  }

  /**
   * @see magellan.library.impl.MagellanNamedImpl#getName()
   */
  @Override
  public String getName() {
    // FIXME(stm) I don't like this reference to GameData here
    if (data != null)
      return data.getTranslation(super.getName());
    else
      return super.getName();
  }

  /**
   * Returns a name for this spell's type.
   */
  public String getTypeName() {
    if (type != null)
      return Resources.get("spell." + type);
    else
      return Resources.get("spell.unspecified");
  }

  /**
   * A human readable string with information about the syntax of the spell (FF)
   *
   * @see magellan.library.Spell#getSyntaxString()
   */
  public String getSyntaxString() {
    StringBuffer retVal = new StringBuffer();

    // Region, if is far
    if (isFar) {
      retVal.append("[").append(getOrderTranslation(EresseaConstants.OC_REGION)).append(" X Y]");
    }

    // Level...allways possible, but not allways usefull
    // we have no info, how to decide here - we add it.
    if (retVal.length() > 0) {
      retVal.append(" ");
    }

    // FF 20080903 that was not quite correct
    // in mantis Pyanfar suggested:
    /**
     * Vorschlag: Der Magellan sollte das aus den im CR vermerktenAurakosten ablesen, "1 1;Aura"
     * heisst 1 Aura pro stufe =&gt; also variabel "50 0;Aura" heisst 50 Aura Festkosten =&gt; also nicht
     * variabel...
     */
    // and maybe we can use this kwowledge for open problems to we
    // built an little private function here
    if (isAuraLevelDependend()) {
      retVal.append("[").append(getOrderTranslation(EresseaConstants.OC_LEVEL)).append(" n]");
    }

    // name of spell in "
    if (retVal.length() > 0) {
      retVal.append(" ");
    }

    String spellName = getName();

    retVal.append("\"").append(spellName).append("\"");

    // Syntax
    if (getSpellSyntax() != null && getSpellSyntax().toString() != null) {
      if (retVal.length() > 0) {
        retVal.append(" ");
      }
      retVal.append(getSpellSyntax().toString());
    }

    // if nothing was added, return null
    if (retVal.length() == 0) {
      retVal = null;
    } else {
      // pr?fix:
      StringBuffer oldRetVal = retVal;
      retVal = new StringBuffer("Syntax: ");
      if (getType() == null) {
        retVal.append(getOrderTranslation(EresseaConstants.OC_COMBATSPELL)).append("?");
      } else if (getType().contains("combat")) {
        retVal.append(getOrderTranslation(EresseaConstants.OC_COMBATSPELL));
      } else {
        retVal.append(getOrderTranslation(EresseaConstants.OC_CAST));
      }

      retVal.append(" ").append(oldRetVal.toString());
    }
    return retVal == null ? null : retVal.toString();
  }

  private String getOrderTranslation(StringID orderId) {
    // FIXME use correct unit locale
    return data.getGameSpecificStuff().getOrderChanger()
        .getOrderO(Locales.getOrderLocale(), orderId).getText();
  }

  /**
   * checks, if we have knowledge about the components of the spell and if the auro counts per level
   * or absolute. if it counts per level we assume the aura-cost depends on level and we deliver
   * true, else false
   *
   * @return
   */
  private boolean isAuraLevelDependend() {
    boolean retval = false;
    if (components == null || components.size() == 0)
      return false;
    for (String key : components.keySet()) {
      String val = components.get(key);
      if (key.equalsIgnoreCase(magellan.library.Spell.Component.AURA)) {
        int blankPos = val.indexOf(" ");
        if ((blankPos > 0) && (blankPos < val.length())) {
          String getLevelAtDays = val.substring(blankPos + 1, val.length());
          if (getLevelAtDays.equals("1"))
            return true;
          else
            // return here, asuming, no other "aura" component will be found
            return false;
        }
      }
    }
    return retval;
  }

  /**
   * @see magellan.library.Spell#getSyntax()
   */
  public String getSyntax() {
    return syntax;
  }

  /**
   * @param syntax the syntax to set
   */
  public void setSyntax(String syntax) {
    this.syntax = syntax;
  }

  /**
   * returns the spellsyntax object of this spell
   *
   * @return a SpellSyntax object or <code>null</code>
   */
  public SpellSyntax getSpellSyntax() {
    if (syntax == null || syntax.length() == 0)
      return null;

    // creating a new one if it does not exists
    if (spellSyntax == null) {
      spellSyntax = new SpellSyntax(syntax);
    }

    return spellSyntax;
  }

  /**
   * Only returns the locale of the spell
   *
   * @see magellan.library.Localized#getLocale()
   * @return the locale of the spell or <code>null</code>
   */
  public Locale getLocale() {
    return locale;
  }

  /**
   * Sets the locale and invalidates the description if required.
   *
   * @see magellan.library.Localized#setLocale(java.util.Locale)
   */
  public void setLocale(Locale locale) {
    if ((locale != null) && (!locale.equals(this.locale))) {
      super.setDescription(null);
      this.locale = locale;
    }
  }

  /**
   * @see magellan.library.Addeable#addAttribute(java.lang.String, java.lang.String)
   */
  public void addAttribute(String key, String value) {
    throw new RuntimeException("this method is not implemented");
  }

  /**
   * @see magellan.library.Addeable#containsAttribute(java.lang.String)
   */
  public boolean containsAttribute(String key) {
    return false;
  }

  /**
   * @see magellan.library.Addeable#getAttribute(java.lang.String)
   */
  public String getAttribute(String key) {
    throw new RuntimeException("this method is not implemented");
  }

  /**
   * @see magellan.library.Addeable#getAttributeKeys()
   */
  public List<String> getAttributeKeys() {
    return new ArrayList<String>();
  }

  /**
   * @see magellan.library.Addeable#getAttributeSize()
   */
  public int getAttributeSize() {
    return 0;
  }

}
