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

package magellan.client.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;

import javax.swing.ButtonGroup;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.filechooser.FileFilter;

import magellan.client.Client;
import magellan.library.utils.Resources;
import magellan.library.utils.UserInterface;
import magellan.library.utils.logging.Logger;

import com.sun.jimi.core.Jimi;
import com.sun.jimi.core.JimiWriter;
import com.sun.jimi.core.options.JPGOptions;

/**
 * DOCUMENT ME!
 *
 * @author Sebastian
 * @version 1.0
 */
public class MapSaverUI extends InternationalizedDialog {
	private static final Logger log = Logger.getInstance(MapSaverUI.class);

  private javax.swing.JComboBox cbFormat;
  private javax.swing.JButton btnCancel;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JRadioButton rbtnCount;
  private javax.swing.JTextField textX;
  private javax.swing.JRadioButton rbtnSize;
  private javax.swing.JTextField textY;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JButton btnSave;
  private JSlider qSlider;
  private ButtonGroup btnGroup;
  private boolean lastWasCount = true;
  private Component outComponent;

  /** DOCUMENT-ME */
  public static final int SAVEAS_IMAGETYPE_JPEG = 0;

  /** DOCUMENT-ME */
  public static final int SAVEAS_IMAGETYPE_PNG = 1;

  
	/**
	 * Creates a new MapSaverUI object.
	 */
	public MapSaverUI(java.awt.Frame parent, boolean modal, Component cmpSave) {
		super(parent, modal);
		initComponents();

		String strList[] = {  "JPEG","PNG" };

		cbFormat.setModel(new DefaultComboBoxModel(strList));
		cbFormat.setSelectedIndex(0);

		btnGroup = new ButtonGroup();
		btnGroup.add(rbtnCount);
		btnGroup.add(rbtnSize);

		rbtnCount.setSelected(true);

		pack();
    centerWindow();

		outComponent = cmpSave;
	}

	private void initComponents() {
		cbFormat = new javax.swing.JComboBox();
		btnCancel = new javax.swing.JButton(Resources.get("mapsaverui.btn.cancel.caption"));
		jPanel1 = new javax.swing.JPanel();
		rbtnSize = new javax.swing.JRadioButton(Resources.get("mapsaverui.radio.size.caption"));
		textX = new javax.swing.JTextField();
		rbtnCount = new javax.swing.JRadioButton(Resources.get("mapsaverui.radio.amount.caption"));
		textY = new javax.swing.JTextField();
		jLabel1 = new javax.swing.JLabel();
		jLabel2 = new javax.swing.JLabel();
		btnSave = new javax.swing.JButton(Resources.get("mapsaverui.btn.save.caption"));
		getContentPane().setLayout(new java.awt.GridBagLayout());

		java.awt.GridBagConstraints gridBagConstraints1;

		setTitle(Resources.get("mapsaverui.window.title"));
		addWindowListener(new java.awt.event.WindowAdapter() {
				@Override
        public void windowClosing(java.awt.event.WindowEvent evt) {
					closeDialog(evt);
				}
			});

		DefaultBoundedRangeModel dbrm = new DefaultBoundedRangeModel(7, 0, 1, 10);

		qSlider = new JSlider(dbrm);
		qSlider.setMajorTickSpacing(1);
		qSlider.setPaintTicks(true);
		qSlider.setPaintLabels(true);
		qSlider.createStandardLabels(1);
		qSlider.setSnapToTicks(true);

		JLabel qLabel = new JLabel(Resources.get("mapsaverui.quality.label") + ":");

		gridBagConstraints1 = new java.awt.GridBagConstraints();
		gridBagConstraints1.gridx = 0;
		gridBagConstraints1.gridy = 1;
		gridBagConstraints1.insets = new java.awt.Insets(0, 5, 5, 5);
		getContentPane().add(cbFormat, gridBagConstraints1);

		btnCancel.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					btnCancelAction(evt);
				}
			});

		gridBagConstraints1 = new java.awt.GridBagConstraints();
		gridBagConstraints1.gridx = 2;
		gridBagConstraints1.gridy = 1;
		gridBagConstraints1.insets = new java.awt.Insets(0, 0, 5, 5);
		getContentPane().add(btnCancel, gridBagConstraints1);

		jPanel1.setLayout(new java.awt.GridBagLayout());

		java.awt.GridBagConstraints gridBagConstraints2;

		jPanel1.setBorder(new javax.swing.border.TitledBorder(Resources.get("mapsaverui.border.imageoptions")));

		rbtnSize.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					if(lastWasCount) {
						rbtnSizeAction(evt);
						lastWasCount = false;
					}
				}
			});

		gridBagConstraints2 = new java.awt.GridBagConstraints();
		gridBagConstraints2.gridx = 0;
		gridBagConstraints2.gridy = 1;
		gridBagConstraints2.gridwidth = 4;
		gridBagConstraints2.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints2.insets = new java.awt.Insets(0, 5, 5, 5);
		jPanel1.add(rbtnSize, gridBagConstraints2);

		textX.setText("1");

		gridBagConstraints2 = new java.awt.GridBagConstraints();
		gridBagConstraints2.gridx = 1;
		gridBagConstraints2.gridy = 2;
		gridBagConstraints2.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints2.insets = new java.awt.Insets(0, 0, 5, 5);
		gridBagConstraints2.weightx = 0.5;
		jPanel1.add(textX, gridBagConstraints2);

		rbtnCount.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					if(!lastWasCount) {
						rbtnCountAction(evt);
						lastWasCount = true;
					}
				}
			});

		gridBagConstraints2 = new java.awt.GridBagConstraints();
		gridBagConstraints2.gridx = 0;
		gridBagConstraints2.gridy = 0;
		gridBagConstraints2.gridwidth = 4;
		gridBagConstraints2.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints2.insets = new java.awt.Insets(5, 5, 0, 5);
		jPanel1.add(rbtnCount, gridBagConstraints2);

		textY.setText("1");

		gridBagConstraints2 = new java.awt.GridBagConstraints();
		gridBagConstraints2.gridx = 3;
		gridBagConstraints2.gridy = 2;
		gridBagConstraints2.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints2.insets = new java.awt.Insets(0, 0, 5, 5);
		gridBagConstraints2.weightx = 0.5;
		jPanel1.add(textY, gridBagConstraints2);

		jLabel1.setText("X:");
		jLabel1.setLabelFor(textX);

		gridBagConstraints2 = new java.awt.GridBagConstraints();
		gridBagConstraints2.gridx = 0;
		gridBagConstraints2.gridy = 2;
		gridBagConstraints2.insets = new java.awt.Insets(0, 5, 5, 5);
		jPanel1.add(jLabel1, gridBagConstraints2);

		jLabel2.setText("Y:");
		jLabel2.setLabelFor(textY);

		gridBagConstraints2 = new java.awt.GridBagConstraints();
		gridBagConstraints2.gridx = 2;
		gridBagConstraints2.gridy = 2;
		gridBagConstraints2.insets = new java.awt.Insets(0, 0, 5, 5);
		jPanel1.add(jLabel2, gridBagConstraints2);

		gridBagConstraints2.gridx = 0;
		gridBagConstraints2.gridy = 3;
		jPanel1.add(qLabel, gridBagConstraints2);

		gridBagConstraints2.gridx = 1;
		gridBagConstraints2.gridwidth = 3;
		jPanel1.add(qSlider, gridBagConstraints2);

		gridBagConstraints1 = new java.awt.GridBagConstraints();
		gridBagConstraints1.gridx = 0;
		gridBagConstraints1.gridy = 0;
		gridBagConstraints1.gridwidth = 3;
		gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints1.insets = new java.awt.Insets(5, 5, 5, 5);
		gridBagConstraints1.weightx = 1.0;
		gridBagConstraints1.weighty = 1.0;
		getContentPane().add(jPanel1, gridBagConstraints1);

		btnSave.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					btnSaveAction(evt);
				}
			});

		gridBagConstraints1 = new java.awt.GridBagConstraints();
		gridBagConstraints1.gridx = 1;
		gridBagConstraints1.gridy = 1;
		gridBagConstraints1.insets = new java.awt.Insets(0, 0, 5, 5);
		getContentPane().add(btnSave, gridBagConstraints1);
	}

  /**
   * This method sets the window dimension and positions the window to the
   * center of the screen.
   */

  public void centerWindow() {
    int xSize = (int)getBounds().getWidth();
    int ySize = (int)getBounds().getHeight();
    if (xSize > 0 && ySize > 0) {
      int x = getToolkit().getScreenSize().width;
      int y = getToolkit().getScreenSize().height;
      setLocation(new Point((x / 2 - xSize / 2), (y / 2 - ySize / 2)));
    }
  }


	private void btnCancelAction(java.awt.event.ActionEvent evt) {
		setVisible(false);
		dispose();
	}

	private void btnSaveAction(java.awt.event.ActionEvent evt) {
		try {
			int iType = MapSaverUI.SAVEAS_IMAGETYPE_JPEG;
			String strBase;

			switch(cbFormat.getSelectedIndex()) {
  			case 0:
  				iType = MapSaverUI.SAVEAS_IMAGETYPE_JPEG;
  				break;
  
  			case 1:
  				iType = MapSaverUI.SAVEAS_IMAGETYPE_PNG;
  				break;
			}
      
			javax.swing.JFileChooser fc = new javax.swing.JFileChooser();

      FileFilter ff = new MapSaverFileFilter(iType);
			fc.addChoosableFileFilter(ff);
			fc.setFileFilter(ff);

			if(fc.showSaveDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
				strBase = fc.getSelectedFile().toString();

        final String fileName = strBase;
				final int quality = qSlider.getValue() * 10;
        final int imageType = iType;
        final UserInterface ui = new ProgressBarUI(this);
        ui.setTitle(Resources.get("progressdialog.map.title"));
        ui.show();
        ui.setProgress("", 1);
        
        new Thread(new Runnable() {
          public void run() {
            
            try {
      				if(rbtnCount.isSelected()) {
      					saveAs_SC(ui, fileName, Integer.parseInt(textX.getText()),
      							  Integer.parseInt(textY.getText()), quality, imageType);
      				} else {
      					saveAs(ui, fileName, Integer.parseInt(textX.getText()),
      						   Integer.parseInt(textY.getText()), quality, imageType);
      				}
            } catch(OutOfMemoryError oomError) {
              MapSaverUI.log.error(oomError);
              javax.swing.JOptionPane.showMessageDialog(Client.INSTANCE, Resources.get("mapsaverui.msg.outofmem.text"),
                                    Resources.get("mapsaverui.msg.outofmem.title"),
                                    javax.swing.JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
              ui.ready();
              setVisible(false);
              System.gc();
              dispose();
              throw new RuntimeException(ex);
            }
    
            ui.ready();
    				System.gc();
    				setVisible(false);
    				dispose();
            
          }
        }).start();
			}
		} catch(Exception ex) {
			MapSaverUI.log.error(ex);
			javax.swing.JOptionPane.showMessageDialog(this,
													  Resources.get("mapsaverui.msg.erroronsave.text") +
													  ex.toString(),
													  Resources.get("mapsaverui.msg.erroronsave.title"),
													  javax.swing.JOptionPane.ERROR_MESSAGE);
		} catch(OutOfMemoryError oomError) {
			MapSaverUI.log.error(oomError);
			javax.swing.JOptionPane.showMessageDialog(this, Resources.get("mapsaverui.msg.outofmem.text"),
													  Resources.get("mapsaverui.msg.outofmem.title"),
													  javax.swing.JOptionPane.ERROR_MESSAGE);
		}
	}

	private void rbtnSizeAction(java.awt.event.ActionEvent evt) {
		int iX = Integer.parseInt(textX.getText());
		int iY = Integer.parseInt(textY.getText());
		int iWidth = outComponent.getBounds().width;
		int iHeight = outComponent.getBounds().height;

		textX.setText(Integer.toString(iWidth / iX));
		textY.setText(Integer.toString(iHeight / iY));
	}

	private void rbtnCountAction(java.awt.event.ActionEvent evt) {
		int iX = Integer.parseInt(textX.getText());
		int iY = Integer.parseInt(textY.getText());
		int iWidth = outComponent.getBounds().width;
		int iHeight = outComponent.getBounds().height;

		textX.setText(Integer.toString(iWidth / iX));
		textY.setText(Integer.toString(iHeight / iY));
	}

	/**
	 * Closes the dialog
	 *
	 * 
	 */
	private void closeDialog(java.awt.event.WindowEvent evt) {
		setVisible(false);
		dispose();
	}

	public void saveAs(UserInterface ui, String strOut, int iWidth, int iHeight, int fQuality, int iSaveType) throws Exception {
		int iX = 0;
		int iY = 0;
		BufferedImage bimg = new BufferedImage(iWidth, iHeight, BufferedImage.TYPE_INT_RGB);
		Dimension dim = new Dimension(outComponent.getBounds().width, outComponent.getBounds().height);
		Graphics2D g2 = null;

		iX = (int) (dim.getWidth() / iWidth);

		if((((int) dim.getWidth()) % iWidth) > 0) {
			iX++;
		}

		iY = (int) (dim.getHeight() / iHeight);

		if((((int) dim.getHeight()) % iHeight) > 0) {
			iY++;
		}

		dim = null;
    
    //int size = iX*iY;

		try {
			for(int y = 0; y < iY; y++) {
				for(int x = 0; x < iX; x++) {
          ui.setProgress(Resources.get("progressdialog.map.step01"), x*y);
					g2 = bimg.createGraphics();
					g2.setClip(0, 0, iWidth, iHeight);

					java.awt.geom.AffineTransform transform = new java.awt.geom.AffineTransform();
					transform.setToIdentity();
					transform.translate(-x * iWidth, -y * iHeight);
					g2.transform(transform);

					outComponent.paint(g2);

					transform = null;
					g2.dispose();
					g2 = null;

					SaveAs(strOut, bimg, x, y, x + (y * iX) + 1, iX * iY, iSaveType, fQuality);
				}
			}

			bimg.flush();
			bimg = null;
		} catch(OutOfMemoryError e) {
			if(g2 != null) {
				g2.dispose();
				g2 = null;
			}

			bimg.flush();
			bimg = null;

			throw e;
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * @throws Exception DOCUMENT-ME
	 */
	public void saveAs_SC(UserInterface ui, String strOut, int iCountX, int iCountY, int fQuality, int iSaveType) throws Exception {
		Dimension dim = new Dimension(outComponent.getBounds().width, outComponent.getBounds().height);

		int iWidth = ((int) dim.getWidth()) / iCountX;

		if(((int) dim.getWidth()) > (iWidth * iCountX)) {
			iWidth++;
		}

		int iHeight = ((int) dim.getHeight()) / iCountY;

		if(((int) dim.getHeight()) > (iHeight * iCountY)) {
			iHeight++;
		}

		saveAs(ui, strOut, iWidth, iHeight, fQuality, iSaveType);
	}

  /**
   * This method saves the image in bimg to a file.
   * Currently JPG and PNG is supported.
   */
	private void SaveAs(String strOut, BufferedImage bimg, int x, int y, int iOf, int iMax,
						int iSaveType, int fQuality) throws Exception
	{
		switch(iSaveType) {
  		case SAVEAS_IMAGETYPE_JPEG: {
  			String strOutput;
        String extension = ".jpg";
        
        if (strOut.toLowerCase().endsWith(extension)) {
          strOut = strOut.substring(0,strOut.length()-extension.length());
        }
        
        String coords = "_" + x + "_" + y;
        if (strOut.endsWith(coords)) {
          strOut = strOut.substring(0,strOut.length()-coords.length());
        }
        
        strOutput = strOut + coords + extension;
  
  			MapSaverUI.log.info(strOutput + " " + iOf + " of " + iMax);
        FileOutputStream fos = new FileOutputStream(strOutput);
        
        JimiWriter writer = Jimi.createJimiWriter("image/jpeg",fos);
        JPGOptions options = new JPGOptions();
        options.setQuality(fQuality);
        writer.setOptions(options);
        
        writer.setSource(bimg.getSource());
        writer.putImage(fos);
        
        fos.close();
        
        break;
  		}
  
  		case SAVEAS_IMAGETYPE_PNG: {
  
  			String strOutput;
        String extension = ".png";
        
        if (strOut.toLowerCase().endsWith(extension)) {
          strOut = strOut.substring(0,strOut.length()-extension.length());
        }
  
        String coords = "_" + x + "_" + y;
        if (strOut.endsWith(coords)) {
          strOut = strOut.substring(0,strOut.length()-coords.length());
        }
        
  			strOutput = strOut + coords + extension;
  
        Jimi.putImage("image/png",bimg.getSource(), strOutput);

        break;
      }
		}
	}
}


class MapSaverFileFilter extends FileFilter {
  private int iType;
  
  public MapSaverFileFilter(int iType) {
    this.iType = iType;
  }
  
  @Override
  public boolean accept(File f) {
    String fileName = f.getName().toLowerCase();
    switch (iType) {
      case MapSaverUI.SAVEAS_IMAGETYPE_PNG: {
        return f.isDirectory() || fileName.endsWith(".png");
      }
      case MapSaverUI.SAVEAS_IMAGETYPE_JPEG: {
        return f.isDirectory() || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg");
      }
    }
    return false;
  }

  @Override
  public String getDescription() {
    switch (iType) {
      case MapSaverUI.SAVEAS_IMAGETYPE_PNG: {
        return Resources.get("mapsaverui.filter.png.description");
      }
      case MapSaverUI.SAVEAS_IMAGETYPE_JPEG: {
        return Resources.get("mapsaverui.filter.jpg.description");
      }
    }
    return "*.*";
  }
};

