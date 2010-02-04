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

package magellan.library.io.xml;

import java.io.IOException;
import java.io.Reader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import magellan.library.utils.logging.Logger;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * DOCUMENT-ME
 * 
 * @author $Author: $
 * @version $Revision: 213 $
 */
public class XMLIO {
  /** DOCUMENT-ME */
  public static final Logger log = Logger.getInstance(XMLIO.class);

  /**
   * DOCUMENT-ME
   * 
   * @throws IOException DOCUMENT-ME
   * @throws XMLIOException DOCUMENT-ME
   */
  public Document getDocument(Reader reader) throws IOException {
    try {
      DocumentBuilderFactory dbf = null;
      dbf = DocumentBuilderFactory.newInstance();

      // This makes ID/IDREF attributes to have a meaning.
      // dbf.setValidating(true);
      DocumentBuilder db = dbf.newDocumentBuilder();

      InputSource is = new InputSource(reader);

      // FIXME: take care of errors via org.xml.sax.ErrorHandler !!!
      // URL dtd = ResourcePathClassLoader.getResourceStatically("rules/rules.dtd");
      // URL dtd = null;

      // if(dtd == null) {
      // log.warn("Could not find a dtd.");
      // } else {
      // is.setSystemId(dtd.toString());
      // }

      return db.parse(is);
    } catch (FactoryConfigurationError fce) {
      throw new XMLIOException(fce.getException());
    } catch (ParserConfigurationException e) {
      throw new XMLIOException(e);
    } catch (SAXException e) {
      throw new XMLIOException(e);
    }
  }
}
