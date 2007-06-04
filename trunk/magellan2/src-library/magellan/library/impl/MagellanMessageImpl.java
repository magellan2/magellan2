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

package magellan.library.impl;

import java.util.Map;
import java.util.StringTokenizer;

import magellan.library.CoordinateID;
import magellan.library.GameData;
import magellan.library.ID;
import magellan.library.IntegerID;
import magellan.library.Message;
import magellan.library.Unit;
import magellan.library.UnitID;
import magellan.library.rules.MessageType;
import magellan.library.utils.OrderedHashtable;


/**
 * A class for representing a message.
 * 
 * <p>
 * The new format of messages in Eressea CR versions >= 41 made it necessary to reconsider this
 * class. Mainly, messages can now have an id and attributes.
 * </p>
 * 
 * <p>
 * Two special attributes are available directly via the corresping get/set methods.
 * </p>
 * 
 * <p>
 * First, this is the type attribte (tag ;type in the cr)  denoting the type of the message. It is
 * transformed into a  <tt>MessageType</tt> object.
 * </p>
 * 
 * <p>
 * Second, there is the text attribute (tag ;rendered in the cr). By design this attribute should
 * actually be created by rendering the message according to the message type's pattern and the
 * other attributes. This class does contain a <tt>render()</tt> method, still, it is too
 * primitive to yield acceptable results, so it is preferrable to take the rendered message text
 * directly from the cr.
 * </p>
 * 
 * <p>
 * Note, that for historic reasons, a <tt>Message</tt> object might have no type or attributes and
 * an invalid id of -1.
 * </p>
 */
public class MagellanMessageImpl extends MagellanIdentifiableImpl implements Message {
	private String text = null;
	private MessageType type = null;

	/**
	 * The attributes of this message. The keys are the keys of the attribute, the values object
	 * pairs of the attributes' keys and values.
	 */
	public Map<String,String> attributes = null;

	/*
	 * this is a helper static variable so we only have one reference (tricky, eh?)
	 */
	private static final IntegerID ambiguousID = IntegerID.create(-1);

	/**
	 * Creates a new Message object.
	 *
	 * 
	 */
	public MagellanMessageImpl(String text) {
		this(ambiguousID, text, null, null);
	}

	/**
	 * Creates a new Message object.
	 *
	 * 
	 */
	public MagellanMessageImpl(ID id) {
		this(id, null, null, null);
	}

	/**
	 * Creates a new Message object.
	 *
	 * 
	 * 
	 */
	public MagellanMessageImpl(ID id, String text) {
		this(id, text, null, null);
	}

	/**
	 * Creates a new Message object.
	 *
	 * 
	 * 
	 * 
	 */
	public MagellanMessageImpl(ID id, MessageType type, Map<String,String> attributes) {
		this(id, null, type, attributes);
	}

	/**
	 * Creates a new Message object.
	 *
	 * 
	 */
	public MagellanMessageImpl(Message msg) {
		this(msg.getID(), msg.getText(), msg.getMessageType(), msg.getAttributes());
	}

	/**
	 * Creates a new Message object.
	 * 
	 * If <code>text</code> is not <code>null</code> the text is set directly; if it is null, it
	 * is rendered from the type and attributes.
	 * 
	 * @param id
	 *            id ID of the Message
	 * @param text
	 *            The message text
	 * @param type
	 *            The message type
	 * @param attributes
	 *            The attributes
	 */
	public MagellanMessageImpl(ID id, String text, MessageType type, Map<String,String> attributes) {
		super(id);

		if((text == null) && (type != null)) {
			this.text = render(null, type.getPattern(), attributes);
		} else {
			this.text = text;
		}

		this.type = type;

		if(attributes != null) {
			this.attributes = new OrderedHashtable<String, String>(attributes);
		}
	}

  /**
   * The attributes of this message. The keys are the keys of the attribute, the values object
   * pairs of the attributes' keys and values.
   */
  public Map<String,String> getAttributes() {
    return attributes;
  }
  /**
   * The attributes of this message. The keys are the keys of the attribute, the values object
   * pairs of the attributes' keys and values.
   */
  public void setAttributes(Map<String,String> attributes) {
    this.attributes = attributes;
  }
  
	/**
	 * Gets the rendered message text.
	 *
	 * @return The message text
	 */
	public String getText() {
		return text;
	}

	/**
	 * Sets the text of this message to <code>text</code>.
	 *
	 * @param text The new text
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * Returns the <code>MessageType</code> of this message.
	 *
	 * @return The message type
	 */
	public MessageType getMessageType() {
		return type;
	}

	/**
	 * Sets the <code>MessageType</code> of this message.
	 *
	 * @param type The new message type
	 */
	public void setType(MessageType type) {
		this.type = type;
	}

	/**
	 * Helper method for render. Translates attributes into user strings.
	 *
	 * 
	 * 
	 * 
	 *
	 * 
	 */
	private static String renderAttribute(GameData data, String id, String attribute) {
		if(id.equalsIgnoreCase("region")) {
			CoordinateID coord = CoordinateID.parse(attribute, ",");

			if(coord == null) {
				coord = CoordinateID.parse(attribute, " ");
			}

			return data.getRegion(coord).toString();
		}

		if(id.equalsIgnoreCase("unit") || id.equalsIgnoreCase("target")) {
			Unit unit = null;

			if(attribute != null) {
				unit = data.getUnit(UnitID.createUnitID(attribute, 10));
			}

			return (unit == null) ? null : unit.toString();
		}

		return attribute;
	}

	/**
	 * Renderes a message text from the given <code>pattern</code> and <code>attributes</code>.
	 * 
	 * <p>Expects the tokens of the form {name1 name2} and replaces them by the attribute values for name1, name2 etc. 
	 * If these values are unit names or region coordinates, their names are taken from the provided GameData.</p>
	 * 
	 * <p>WARNING! This does not work for the current cr format (41) which expects tokens of the form $unit($unit).</p>
	 *
	 * @param data The game for replacing unit IDs and region coordinates
	 * @param pattern The pattern to render
	 * @param attributes A map of (String,Value)-pairs for replacing tokens in the pattern
	 *
	 * @return The rendered text as string
	 */
	public static String render(GameData data, String pattern, Map<String,String> attributes) {
		if((pattern == null) || (attributes == null)) {
			return null;
		}

		StringBuffer sb = new StringBuffer();
		String token = "";
		String lastToken = "";
		StringTokenizer st = new StringTokenizer(pattern, "{}", true);

		while(st.hasMoreTokens()) {
			lastToken = token;
			token = st.nextToken();

			if(lastToken.equals("{")) {
				StringTokenizer tt = new StringTokenizer(token);

				while(tt.hasMoreTokens()) {
					String strToken = tt.nextToken();
					String value = attributes.get(strToken);
					String strAttribute = renderAttribute(data, strToken, value);
					sb.append(strAttribute);
				}
			} else {
				if(!token.equals("{") && !token.equals("}")) {
					sb.append(token);
				}
			}
		}

		return sb.toString();
	}

	/**
	 * Renders the message and updates the message text.
	 *
	 * @param data The GameData for replacing unit IDs and region coordinates
	 */
	public void render(GameData data) {
		setText(MagellanMessageImpl.render(data, type.getPattern(), attributes));
	}

	/**
	 * 
	 *
	 * @return A hash code for this message
	 */
	public int hashCode() {
		// identify Message by message text
		return (text == null) ? superHashCode() : text.hashCode();
	}

	/**
	 * Indicates whether this Message object is equal to another object. Returns true only if o is
	 * not null and an instance of class Message and o's id is equal to the id of this  Message
	 * object. 2002.02.21 pavkovic: Also the message text has to be the same for Messages  with
	 * ambiguous IntegerID(-1)
	 *
	 * 
	 *
	 * 
	 */
	public boolean equals(Object o) {
		try {
			boolean ret = this.getID().equals(ambiguousID) ? isPrimitiveEquals((MagellanMessageImpl) o)
														   : isComplexEquals((MagellanMessageImpl) o);

			/*
			if(ret && log.isDebugEnabled()) {
			    log.debug("Messages: "+this+", "+o);
			    log.debug("this.getID() == ambiguousID:" +(this.getID().equals(ambiguousID)));
			    log.debug("isPrimitiveEquals:"+isPrimitiveEquals((Message) o));
			    log.debug("isComplexEquals:"+isComplexEquals((Message) o));
			}
			*/
			return ret;
		} catch(ClassCastException e) {
			return false;
		}
	}

	/**
	 * This checks if Messages are of old style without id. In such a situation we have to compare
	 * the text
	 *
	 * 
	 *
	 * 
	 */
	private boolean isPrimitiveEquals(MagellanMessageImpl o) {
		// we use == for ambiguousID as it is singleton
		// this is true iff this.ID == o.ID == ambiguousID && this.text == o.text
		return (this.getID() == o.getID()) && equalObjects(this.getText(), o.getText());
	}

	/**
	 * This checks if Messages are of new style (CR version >= 41 with id). But the id is not an
	 * identifying characteristica.
	 *
	 * 
	 *
	 * 
	 */
	private boolean isComplexEquals(MagellanMessageImpl o) {
		// this means: this.ID == o.ID ( != ambiguousID ) || (<IDs are not equal> this.text == o.text && this.messageType == o.messageType)
		return !this.getID().equals(ambiguousID) &&
			   (this.getID().equals(o.getID()) ||
			   (equalObjects(this.getText(), o.getText()) &&
			   equalObjects(this.getMessageType(), o.getMessageType())));
	}

	private static final boolean equalObjects(Object a, Object b) {
		return (a == null) ? (b == null) : a.equals(b);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public String toString() {
		return "Message[" + "id=" + id + "," + "type=" +
			   ((type == null) ? "null" : (type.getID() + "")) + "," + "text=\"" + text + "\"," +
			   "attributes=" + attributes + "]";
	}
}
