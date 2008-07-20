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

package magellan.client.utils;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import magellan.client.swing.map.CellGeometry;
import magellan.client.swing.map.ExternalMapCellRenderer;
import magellan.client.swing.map.MapCellRenderer;
import magellan.library.utils.Locales;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;


/**
 * DOCUMENT ME!
 *
 * @author Andreas
 * @version 1.0
 */
public class RendererLoader extends Object {
	private static final Logger log = Logger.getInstance(RendererLoader.class);
	private File directory;
	private ZipClassLoader loader;
	private Class paramClass[];
	private Object paramInst[];
	private Properties settings;
	private static final String RENDERER_CLASS_STRING = "magellan.client.swing.map.ExternalMapCellRenderer";
	private static Class RENDERER_CLASS;

	/**
	 * Creates new RendererLoader
	 *
   * @throws IllegalArgumentException If any problem was found
	 */
	public RendererLoader(File dir, String sDir, CellGeometry geom, Properties sett) {
		try {
			paramClass = new Class[2];
			paramClass[0] = geom.getClass();
			paramClass[1] = (new Properties()).getClass();

			paramInst = new Object[2];
			paramInst[0] = geom;
			paramInst[1] = sett;
			settings = sett;

			directory = new File(dir, sDir);
			loader = new ZipClassLoader();

			RendererLoader.RENDERER_CLASS = Class.forName(RendererLoader.RENDERER_CLASS_STRING);
		} catch(Exception exc) {
			throw new IllegalArgumentException(exc);
		}
	}

	/**
	 * This methods tries to find all kind of MapCellRenderer. It looks into
   * all libraries and subpackages in the application root-directory to 
   * find classes that ends with Renderer.class and implement. 
	 */
	public Collection<MapCellRenderer> loadRenderers() {
		RendererLoader.log.info("Searching for additional renderers...");

		if(settings.getProperty("RendererLoader.dontSearchAdditionalRenderers", "false").equals("true")) {
			RendererLoader.log.info("Searching for additional renderers disabled.");

			return null;
		}

		long start = System.currentTimeMillis();
    List<MapCellRenderer> list = new ArrayList<MapCellRenderer>();
    
		try {
			File[] files = directory.listFiles();

			for(File file : files) {
				boolean found = false;
				boolean error = false;
				StringBuffer msg = new StringBuffer();
        
				try {
					if(file.getName().endsWith(".jar") || file.getName().endsWith(".zip")) {
						msg.append("Checking " + file.getName() + "...");

						ZipFile jar = new ZipFile(file);
						loader.setToLoad(jar);

						Enumeration<? extends ZipEntry> e = jar.entries();

						while(e.hasMoreElements()) {
							ZipEntry next = e.nextElement();
              
							if(!next.isDirectory() && next.getName().endsWith("Renderer.class")) {
								String name = next.getName();
								name = name.substring(0, name.indexOf(".class")).replace('\\', '.').replace('/', '.');

								try {
									Class<?> rclass = loader.loadClass(name);

									if(isRenderer(rclass)) {
										try {
											try {
												Constructor constr = rclass.getConstructor(paramClass);
												Object obj = constr.newInstance(paramInst);
                        if (obj instanceof  MapCellRenderer) {
                          MapCellRenderer renderer = (MapCellRenderer)obj;
                          loadResourceBundle(jar, renderer);
                          list.add(renderer); // try with arguments
                          found = true;
                        }
											} catch(Exception parameterException) {
                        // okay, just a simple constructor.
												Object obj = rclass.newInstance();
                        if (obj instanceof  MapCellRenderer) {
                          MapCellRenderer renderer = (MapCellRenderer)obj;
  												loadResourceBundle(jar, renderer);
  												list.add(renderer); // try without arguments
  												found = true;
                        }
											}
										} catch(Exception loadException) {
											error = true;
											RendererLoader.log.info(msg);
											RendererLoader.log.info("Unable to load " + rclass.getName() + ':' +
													 loadException + '!');
										}
									}
								} catch(ClassNotFoundException cnfe) {
								}
							}
						}

						if(found) {
							msg.append("Successful!");
							RendererLoader.log.info(msg);
						} else if(!error) {
							msg.append("Nothing found!");
							RendererLoader.log.info(msg);
						}
					}
				} catch(Exception exc) {
				}
			}

			if(list.size() > 0) {
				Iterator it = list.iterator();
				StringBuffer msg = new StringBuffer();

				if(list.size() > 1) {
					msg.append("Additional renderers(" + list.size() + ") loaded: ");
				} else {
					msg.append("Additional renderer loaded: ");
				}

				while(it.hasNext()) {
					msg.append(((MapCellRenderer) it.next()).getName());

					if(it.hasNext()) {
						msg.append(';');
					}
				}

				RendererLoader.log.info(msg);

				long end = System.currentTimeMillis();
				RendererLoader.log.info("Searching for additional renderers done. Found " + list.size() +
						 " instances in " + String.valueOf((end - start)) + " msecs");

				return list;
			}
		} catch(Exception exc) {
		}

		long end = System.currentTimeMillis();
		RendererLoader.log.info("Searching for additional renderers done. Found 0 instances in " + String.valueOf((end - start)) + " msecs");

		return list;
	}

	protected void loadResourceBundle(ZipFile jar, MapCellRenderer obj) {
    ResourceBundle rb = Resources.getResourceBundle(Locales.getGUILocale());
		if(rb == null) {
			throw new RuntimeException("ResourceBundle not found.");
		}

		((ExternalMapCellRenderer) obj).setResourceBundle(rb);
	}

	protected boolean isRenderer(Object cur) {
		if(RendererLoader.RENDERER_CLASS != null) {
			return RendererLoader.RENDERER_CLASS.isInstance(cur);
		}

		return isRenderer(cur.getClass());
	}

	protected boolean isRenderer(Class cur) {
		Class inf[] = cur.getInterfaces();

		if((inf != null) && (inf.length > 0)) {
			for(int i = 0; i < inf.length; i++) {
				if(inf[i].getName().equals(RendererLoader.RENDERER_CLASS_STRING)) {
					return true;
				}
			}
		}

		Class parent = cur.getSuperclass();

		return (parent == null) ? false : isRenderer(parent);
	}

	protected class ZipClassLoader extends ClassLoader {
		protected ZipFile jar = null;

		/**
		 * DOCUMENT-ME
		 *
		 * 
		 */
		public void setToLoad(ZipFile jar) {
			this.jar = jar;
		}

		@Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
			try {
				//find the according entry
				ZipEntry entry = jar.getEntry(name.replace('.', '\\') + ".class");

				if(entry == null) {
					entry = jar.getEntry(name.replace('.', '/') + ".class");
				}

				//allocate buffer
				long size = entry.getSize();
				byte buf[] = new byte[(int) size];

				//open connection
				InputStream in = jar.getInputStream(entry);

				int curSize = 0;
				int i = 0;

				do {
					i = in.read(buf, curSize, (int) (size - curSize));

					if(i != -1) {
						curSize += i;
					}
				} while((curSize < size) && (i != -1));

				in.close();

				if(i == -1) {
					throw new RuntimeException("IO Error.");
				}

				return defineClass(name, buf, 0, buf.length);
			} catch(Exception exc) {
				throw new ClassNotFoundException(exc.toString());
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
    public InputStream getResourceAsStream(String name) {
			try {
				ZipEntry zip = jar.getEntry(name);

				if(zip == null) {
					zip = jar.getEntry(name.replace('/', '\\'));
				}

				if(zip == null) {
					zip = jar.getEntry(name.replace('\\', '/'));
				}

				return jar.getInputStream(zip);
			} catch(Exception exc) {
				// FIXME(pavkovic): it is generally a bad idea to catch java.lang.Exception
			}

			return null;
		}
	}
}
