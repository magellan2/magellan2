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

package magellan.library.utils;

import java.io.File;
import java.util.StringTokenizer;
import java.util.zip.ZipFile;

import magellan.library.utils.logging.Logger;


/**
 * Small class for start-up help. Two search functions: The Magellan directory and the settings
 * file.
 *
 * @author Andreas
 * @version
 */
public class MagellanFinder {
	private static final Logger log = Logger.getInstance(MagellanFinder.class);

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 * 
	 *
	 * 
	 */
	public static File findSettingsDirectory(File magDirectory, File settDir) {
		File settFileDir = settDir;
		File magFile = null;

		if((settFileDir != null) && !settFileDir.equals(magDirectory)) {
			magFile = new File(settFileDir, "magellan.ini");

			if(magFile.exists() && !magFile.isDirectory() && magFile.canWrite()) {
				return settFileDir;
			}
		}

		settFileDir = magDirectory;
		magFile = new File(settFileDir, "magellan.ini");
		log.info("Searching for Magellan configuration:");

		StringBuffer msg = new StringBuffer();
		msg.append(settFileDir + "...");

		if(!settFileDir.exists() || !settFileDir.canWrite() || !magFile.exists()) {
			msg.append("Not found.");
			log.info(msg);
			msg = new StringBuffer();
			settFileDir = new File(System.getProperty("user.home"));
			msg.append(settFileDir + "...");
			magFile = new File(settFileDir, "magellan.ini");

			if(!magFile.exists()) {
				msg.append("Not found.");
				log.info(msg);
				msg = new StringBuffer();
				settFileDir = new File(".");

				if(!settFileDir.isDirectory()) {
					settFileDir.getParentFile();
				}

				msg.append(settFileDir + "...");
				magFile = new File(settFileDir, "magellan.ini");

				if(magFile.exists()) {
					msg.append("Found");
					log.info(msg);
					msg = new StringBuffer();
				}
			} else {
				msg.append("Found");
				log.info(msg);
				msg = new StringBuffer();
			}
		} else {
			msg.append("Found");
			log.info(msg);
			msg = new StringBuffer();
		}

		if(!magFile.exists()) {
			msg.append("Not found.\nUsing default directory " + magDirectory.getAbsolutePath() +
					   ".");
			log.info(msg);
			settFileDir = magDirectory;
		} else {
			log.info("Using directory " + settFileDir.getAbsolutePath() + ".");
			log.info(msg);
		}

		return settFileDir;
	}

	/**
	 * Searches for Magellan. This method scans the CLASSPATH and searches for JARs containing
	 * "magellan.client.Client" or corresponding directory structures.
	 *
	 * 
	 */
	public static File findMagellanDirectory() {
		String classPath = System.getProperty("java.class.path", ".");
		StringTokenizer st = new StringTokenizer(classPath, ";,");

		while(st.hasMoreTokens()) {
			String token = st.nextToken();

			// search for a jar
			try {
				if(token.endsWith(".jar") && checkJar(token)) {
					File file = new File(extractDir(token));
					log.info("Magellan directory: " + file + "(found JAR)");

					return file;
				} else {
					File file = new File(token);

					if(!file.isDirectory()) {
						file = file.getParentFile();
					}

					if(file.isDirectory()) {
						File list[] = file.listFiles();

						if(list.length > 0) {
							for(int i = 0; i < list.length; i++) {
								if(checkJar(list[i])) {
									log.info("Magellan directory: " + file + "(found JAR)");

									return file;
								}
							}
						}
					}
				}
			} catch(Exception exc) {
			}

			// search for the class
			try {
				File file = new File(token);

				if(!file.isDirectory()) {
					file = file.getParentFile();
				}

				File dir = new File(file, "magellan");

				if(dir.isDirectory()) {
					dir = new File(dir, "client");

					if(dir.isDirectory()) {
						String list[] = dir.list();

						if(list.length > 0) {
							for(int i = 0; i < list.length; i++) {
								if(list[i].equals("Client.class")) {
									log.info("Magellan directory: " + file +
											 "(found magellan.client.Client class)");

									return file;
								}
							}
						}
					}
				}
			} catch(Exception exc2) {
			}
		}

		return new File(".");
	}

	/**
	 * Extracts the directory out of the given file. If any error occurs, the current
	 * directory(".") is returned.
	 *
	 * 
	 *
	 * 
	 */
	protected static String extractDir(String file) {
		try {
			File f = new File(file);

			if(!f.isDirectory()) {
				f = f.getParentFile();
			}

			return f.toString();
		} catch(Exception exc) {
		}

		try {
			return new File(".").getAbsoluteFile().toString();
		} catch(Exception exc2) {
		}

		return ".";
	}

	/**
	 * Checks if the given file is a zip and contains a "com/eressea/demo/Client.class". These are
	 * the conditions for the file to be a valid magellan Java Archive (JAR).
	 *
	 * 
	 *
	 * 
	 */
	protected static boolean checkJar(String file) {
		return checkJar(new File(file));
	}

	/**
	 * Checks if the given file is a zip and contains a "com/eressea/demo/Client.class". These are
	 * the conditions for the file to be a valid magellan Java Archive (JAR).
	 *
	 * 
	 *
	 * 
	 */
	protected static boolean checkJar(File file) {
		try {
			ZipFile zipped = new ZipFile(file);

			if(zipped.getInputStream(zipped.getEntry("com/eressea/demo/Client.class")) != null) {
				zipped.close();

				return true;
			}

			zipped.close();
		} catch(Exception inner) {
		}

		return false;
	}
}
