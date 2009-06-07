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

package magellan.client.swing.map;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import magellan.client.MagellanContext;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.library.CoordinateID;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.Sign;
import magellan.library.utils.Colors;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;


/**
 * Basic class to draw Signs: a string within a box
 * a first try
 *
 * @author Fiete
 * @version 1.0
 */
public class SignTextCellRenderer extends HexCellRenderer {
	protected Color fontColor = Color.black;
	protected Color backColor = Color.white;
	protected Color brighterColor = Color.black.brighter();
	protected Font unscaledFont = null;
	protected Font font = null;
//	protected FontMetrics fontMetrics = null;
	protected int minimumFontSize = 10;
//	protected int fontHeight = 0;
	protected boolean isScalingFont = false;
	protected int hAlign = SignTextCellRenderer.CENTER;
	protected String singleString[] = new String[1];
	String doubleString[] = new String[2];
	
	
	/** DOCUMENT-ME */
	public static final int LEFT = 0;

	/** DOCUMENT-ME */
	public static final int CENTER = 1;

	/** DOCUMENT-ME */
	public static final int RIGHT = 2;

	/**
	 * Creates new SignTextCellRenderer
	 *
	 * 
	 * 
	 */
	protected SignTextCellRenderer(CellGeometry geo, MagellanContext context) {
		super(geo, context);

		if(settings != null) {
			try {
				setFontColor(Colors.decode(settings.getProperty("SignTextCellRenderer.textColor",
																Colors.encode(fontColor))));
				setBackColor(Colors.decode(settings.getProperty("SignTextCellRenderer.backColor",
						Colors.encode(backColor))));
			} catch(NumberFormatException e) {
			}

			setScalingFont((Boolean.valueOf(settings.getProperty("SignTextCellRenderer.isScalingFont",
															 "false"))).booleanValue());
		}

		setFont(new Font(settings.getProperty("SignTextCellRenderer.fontName", "SansSerif"),
						 Integer.parseInt(settings.getProperty("SignTextCellRenderer.fontStyle",
															   Font.PLAIN + "")),
						 Integer.parseInt(settings.getProperty("SignTextCellRenderer.fontSize", 11 +
															   ""))));
		setMinimumFontSize(Integer.parseInt(settings.getProperty("SignTextCellRenderer.minimumFontSize",
																 "10")));
	}

	protected Color getFontColor() {
		return fontColor;
	}

	protected void setFontColor(Color col) {
		fontColor = col;
		settings.setProperty("SignTextCellRenderer.textColor", Colors.encode(fontColor));
	}

	
	
	
	protected Font getFont() {
		return unscaledFont;
	}

	protected void setFont(Font f) {
		unscaledFont = f;
		setFont(1f);
		settings.setProperty("SignTextCellRenderer.fontName", f.getName());
		settings.setProperty("SignTextCellRenderer.fontStyle", Integer.toString(f.getStyle()));
		settings.setProperty("SignTextCellRenderer.fontSize", Integer.toString(f.getSize()));
	}

	protected void setFont(float scaleFactor) {
		font = unscaledFont.deriveFont(Math.max(unscaledFont.getSize() * scaleFactor,
												minimumFontSize));

		// using deprecated getFontMetrics() to avoid Java2D methods
//		fontMetrics = Client.getDefaultFontMetrics(this.font);
//		fontHeight = this.fontMetrics.getHeight();
	}

	protected boolean isScalingFont() {
		return isScalingFont;
	}

	protected void setScalingFont(boolean b) {
		if(b != isScalingFont()) {
			isScalingFont = b;
			settings.setProperty("SignTextCellRenderer.isScalingFont",
								 String.valueOf(isScalingFont()));
		}
		
	}

	protected int getMinimumFontSize() {
		return minimumFontSize;
	}

	protected void setMinimumFontSize(int size) {
		if(getMinimumFontSize() != size) {
			setMinimumFontSize(size);
			settings.setProperty("SignTextCellRenderer.minimumFontSize", size + "");
		}
	}

	protected int getHAlign() {
		return hAlign;
	}

	protected void setHAlign(int i) {
		hAlign = i;
	}

//	protected FontMetrics getFontMetrics() {
//		if(fontMetrics == null) {
//			fontMetrics = Client.getDefaultFontMetrics(new Font("TimesRoman",Font.PLAIN, 10));
//		}
//
//		return fontMetrics;
//	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	@Override
  public int getPlaneIndex() {
		return Mapper.PLANE_SIGNS;
	}



	/**
	 * Returns the Lines which should be on the sign
	 * max 2 Lines allowed
	 *
	 * @param r the region
	 * @param rect a rectangle of region-hex - not needed here
	 *
	 * @return Collection of Strings to put on the sign
	 */
	public Collection<Sign> getText(Region r, Rectangle rect){
		return r.getSigns();
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 * 
	 */
	@Override
  public void init(GameData data, Graphics g, Rectangle offset) {
		super.init(data, g, offset);

		if(isScalingFont) {
			setFont(cellGeo.getScaleFactor());
		} else {
			setFont(1.0f);
		}
		
		
		
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 * 
	 */
	@Override
  public void render(Object obj, boolean active, boolean selected) {
		if(obj instanceof Region) {
			Region r = (Region) obj;
			CoordinateID c = r.getCoordinate();
			Rectangle rect = cellGeo.getCellRect(c.x, c.y);

      Collection<Sign> signs = getText(r, rect);
      if (signs==null)
        return;
      
      String[] display = new String[signs.size()]; 
			int signCount=0;
			for (Sign s : signs){
			  display[signCount++]=s.getText();
			}

			if(display.length == 0) {
				return;
			}

			graphics.setFont(font);
			graphics.setColor(fontColor);

			int height = getHeight(display);
			int middleX = (rect.x + (rect.width / 2)) - offset.x;
			int middleY = (rect.y + (rect.height / 2) + 1) - offset.y;
			int upperY = middleY;

			if(display.length == 1) {
				upperY += (height / 4);				
			} else {
				upperY -= (((display.length - 1) * height) / 4);
			}

			// put the text above the cell...
			upperY -= (rect.height / 2);
			int i = 0;
			
			switch(hAlign) {
			// left
			case LEFT:

				int leftX = middleX - (getMaxWidth(display) / 2);
				
				i = 0;
				for (String s : display){
					drawString(graphics, s, leftX, upperY + (i * height));
					i++;
				}

				break;

			// center
			case CENTER:
				// height/X is not good...but near.
				// to have the border a bit away from text
				int lmax = (getMaxWidth(display) + (height/4));
				fillRectangle(middleX-(lmax/2), upperY - height, lmax, ((display.length) * height) + (height/4));
				
				i = 0;
				for (String s : display){
					int l = getWidth(s);
					drawString(graphics, s, middleX - (l / 2), upperY + (i * height));
					i++;
				}

				break;

			// right
			case RIGHT:

				int rightX = middleX + (getMaxWidth(display) / 2);

				i = 0;
				for (String s : display){
					i++;
					drawString(graphics, s, rightX - getWidth(s),
							   upperY + (i * height));
				}

				break;
			}
		}
	}

	 protected int getWidth(String string) {
	    return (int) font.getStringBounds(string, graphics.getFontRenderContext()).getWidth();
	  }

	  protected int getWidth(char [] chars, int begin, int limit) {
	    return (int) font.getStringBounds(chars, begin, limit, graphics.getFontRenderContext()).getWidth();
	  }


	private int getMaxWidth(String[] display) {
		int maxWidth = -1;
		for (String s : display){
			if(getWidth(s) > maxWidth) {
				maxWidth = getWidth(s);
			}
		}

		return maxWidth;
	}

  private int getHeight(String[] display) {
    float height = 1;
    for (String text : display){
      height=Math.max(height, font.getLineMetrics(text, graphics.getFontRenderContext()).getHeight());
    }
    return (int) (height * 0.9);
  }


	private void drawString(Graphics graphic, String text, int X, int Y) {
		graphics.setColor(fontColor);
		graphics.drawString(text, X, Y);
	}
	
	private void fillRectangle(int X,int Y,int width, int height){
		graphics.setColor(fontColor);
		graphics.drawRect(X-1, Y-1, width+1, height+1);
		graphics.fillRect(X+(width/2), Y+height, 2, height/2);
		graphics.setColor(backColor);
		graphics.fillRect(X, Y, width, height);
	}
	
	
	@Override
  public PreferencesAdapter getPreferencesAdapter() {
		return new Preferences(this);
	}
	
	protected class Preferences extends JPanel implements PreferencesAdapter {
		// The source component to configure
		protected SignTextCellRenderer source = null;
    private final Logger log = Logger.getInstance(Preferences.class);

		// GUI elements
		private JPanel pnlFontColor = null;
		private JPanel pnlBackColor = null;
		private JComboBox cmbFontName = null;
		private JCheckBox chkFontBold = null;
		private JComboBox cmbFontSize = null;
		private JComboBox cmbMinimumFontSize = null;
		private JCheckBox chkScaleFont = null;

		/**
		 * Creates a new Preferences object.
		 *
		 * 
		 */
		public Preferences(SignTextCellRenderer r) {
			this.source = r;
			init();
		}

		private void init() {
			pnlFontColor = new JPanel();
			pnlFontColor.setSize(50, 50);
			pnlFontColor.setBackground(source.getFontColor());
			pnlFontColor.addMouseListener(new MouseAdapter() {
					@Override
          public void mousePressed(MouseEvent e) {
						Color newColor = JColorChooser.showDialog(pnlFontColor.getTopLevelAncestor(),
																  Resources.get("map.signtextcellrenderer.textcolor"),
																  pnlFontColor.getBackground());

						if(newColor != null) {
							pnlFontColor.setBackground(newColor);
						}
					}
				});

			JLabel lblFontColor = new JLabel(Resources.get("map.signtextcellrenderer.fontcolor"));
			lblFontColor.setLabelFor(pnlFontColor);
			
			pnlBackColor = new JPanel();
			pnlBackColor.setSize(50, 50);
			pnlBackColor.setBackground(source.getBackColor());
			pnlBackColor.addMouseListener(new MouseAdapter() {
					@Override
          public void mousePressed(MouseEvent e) {
						Color newColor = JColorChooser.showDialog(pnlBackColor.getTopLevelAncestor(),
																  Resources.get("map.signtextcellrenderer.backcolor"),
																  pnlBackColor.getBackground());

						if(newColor != null) {
							pnlBackColor.setBackground(newColor);
						}
					}
				});

			JLabel lblBackColor = new JLabel(Resources.get("map.signtextcellrenderer.backcolor"));
			lblFontColor.setLabelFor(pnlBackColor);

			String fontNames[] = null;

			try {
				fontNames = GraphicsEnvironment.getLocalGraphicsEnvironment()
											   .getAvailableFontFamilyNames();
			} catch(NullPointerException e) {
				// FIXME(pavkovic) 2003.03.17: This is bad!
				log.error("Probably your are running jdk1.4.1 on Apple. Perhaps we can keep Magellan running. But don't count on it!");
				fontNames = new String[0];
			}

			cmbFontName = new JComboBox(fontNames);
			cmbFontName.setSelectedItem(source.getFont().getName());

			JLabel lblFontName = new JLabel(Resources.get("map.signtextcellrenderer.fonttype"));
			lblFontName.setLabelFor(cmbFontName);

			chkFontBold = new JCheckBox(Resources.get("map.signtextcellrenderer.usebold"),
										source.getFont().getStyle() != Font.PLAIN);

			Font font = source.getFont();
			List<String> fontSizes = new LinkedList<String>();
			fontSizes.add("8");
			fontSizes.add("9");
			fontSizes.add("10");
			fontSizes.add("11");
			fontSizes.add("12");
			fontSizes.add("14");
			fontSizes.add("16");
			fontSizes.add("18");
			fontSizes.add("22");
			cmbFontSize = new JComboBox(fontSizes.toArray());
			cmbFontSize.setEditable(true);
			cmbFontSize.setSelectedItem("" + font.getSize());

			JLabel lblFontSize = new JLabel(Resources.get("map.signtextcellrenderer.fontsize"));
			lblFontSize.setLabelFor(cmbFontSize);

			cmbMinimumFontSize = new JComboBox(fontSizes.toArray());
			cmbMinimumFontSize.setEditable(true);
			cmbMinimumFontSize.setSelectedItem("" + source.getMinimumFontSize());

			JLabel lblMinimumFontSize = new JLabel(Resources.get("map.signtextcellrenderer.minimumfontsize"));
			lblMinimumFontSize.setLabelFor(cmbMinimumFontSize);

			chkScaleFont = new JCheckBox(Resources.get("map.signtextcellrenderer.scalefontwithmapzoom"), source.isScalingFont());

			this.setLayout(new GridBagLayout());

			GridBagConstraints c = new GridBagConstraints();
			c.anchor = GridBagConstraints.WEST;
			c.gridx = 0;
			c.gridy = 0;
			this.add(lblFontColor, c);
			c.gridx = 1;
			c.gridy = 0;
			this.add(pnlFontColor, c);
			
			c.gridx = 0;
			c.gridy = 1;
			this.add(lblBackColor, c);
			c.gridx = 1;
			c.gridy = 1;
			this.add(pnlBackColor, c);

			c.gridx = 0;
			c.gridy = 2;
			this.add(lblFontName, c);
			c.gridx = 1;
			c.gridy = 2;
			this.add(cmbFontName, c);

			c.gridx = 0;
			c.gridy = 3;
			this.add(chkFontBold, c);

			c.gridx = 0;
			c.gridy = 4;
			this.add(lblFontSize, c);
			c.gridx = 1;
			c.gridy = 4;
			this.add(cmbFontSize, c);

			c.gridx = 0;
			c.gridy = 5;
			this.add(lblMinimumFontSize, c);
			c.gridx = 1;
			c.gridy = 5;
			this.add(cmbMinimumFontSize, c);

			c.gridx = 0;
			c.gridy = 6;
			c.gridwidth = 2;
			this.add(chkScaleFont, c);
		}

        public void initPreferences() {
            // TODO: implement it
        }

		/**
		 * DOCUMENT-ME
		 */
		public void applyPreferences() {
			source.setFontColor(pnlFontColor.getBackground());
			source.setBackColor(pnlBackColor.getBackground());

			String fontName = (String) cmbFontName.getSelectedItem();
			int fontStyle = chkFontBold.isSelected() ? Font.BOLD : Font.PLAIN;
			int fontSize = Integer.parseInt((String) cmbFontSize.getSelectedItem());
			source.setFont(new Font(fontName, fontStyle, fontSize));
			source.setMinimumFontSize(Integer.parseInt((String) cmbMinimumFontSize.getSelectedItem()));
			source.setScalingFont(chkScaleFont.isSelected());
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public Component getComponent() {
			return this;
		}

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public String getTitle() {
			return source.getName();
		}
	}

	/**
	 * @return the backColor
	 */
	public Color getBackColor() {
		return backColor;
	}

	/**
	 * @param backColor the backColor to set
	 */
	public void setBackColor(Color backColor) {
		this.backColor = backColor;
		settings.setProperty("SignTextCellRenderer.backColor", Colors.encode(backColor));
	}
	


  /**
   * @see magellan.client.swing.map.HexCellRenderer#getName()
   */
  @Override
  public String getName() {
    return Resources.get("map.signtextcellrenderer.name");
  }

	
}
