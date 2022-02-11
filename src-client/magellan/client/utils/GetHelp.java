// class util.GetHelp
// created on Feb 9, 2022
//
// Copyright 2003-2022 by magellan project team
//
// Author : $Author: $
// $Id: $
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc.,
// 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
package magellan.client.utils;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import magellan.library.utils.logging.Logger;

/**
 * How to update the Eressea help:
 * <ul>
 * <li>run this class</li>
 * <li>load the page list, e.g, help/de/pagelist.txt</li>
 * <li>save the help files to a new directory, e.g., help/de.new</li>
 * <li>copy the files eressea.jhm, eresseaTOC.xml to help/de, all html files from help/de.new/eressea to
 * help/de/eressea</li>
 * <li>create the new magellan.jhm file by running</li>
 * <code>xmllint -xinclude --format magellan.template.jhm > magellan.jhm</code> in help/de
 * - create the new JavaHelp search index by running
 * <kbd>java --class-path ../lib/javahelp-2.0.05.jar com.sun.java.help.search.Indexer de/&lowast;/&lowast;html</kbd>
 * in the directory help
 *
 * @author stm
 * @version 1.0, Feb 10, 2022
 */
public class GetHelp extends JPanel {

  public static class PageInfo {

    private Map<String, Page> pageLookup;
    private List<Page> pages;
    private List<Page> redirects;

    public PageInfo() {
      pageLookup = new HashMap<String, Page>();
      pages = new ArrayList<Page>();
      redirects = new ArrayList<Page>();
    }

    private static String getHelpfilename(String page, boolean extension) {
      return page.replaceAll("[^A-Za-z0-9_-]", "_") + (extension ? ".html" : "");
    }

    private void clear() {
      pageLookup.clear();
      pages.clear();
      redirects.clear();
    }

    public void read(String pageFile) {
      try {
        readPages(pageFile);
      } catch (IOException e) {
        log("error reading pages from " + pageFile, e);
        return;
      }
      for (Page p : getPages()) {
        String pName = p.getFile();
        if (pageLookup.containsKey(pName)) {
          log("duplicate page " + p);
        }
        pageLookup.put(pName, p);
      }
      for (Page p : getRedirects()) {
        String pName = p.getFile();
        if (pageLookup.containsKey(pName)) {
          log("duplicate page " + p);
        }
        pageLookup.put(pName, p);
      }
    }

    private void readPages(String fileName) throws FileNotFoundException, IOException {
      clear();
      try (LineNumberReader reader = new LineNumberReader(new FileReader(fileName, Charset.forName(FILE_ENCODING)))) {
        String line;
        while ((line = reader.readLine()) != null) {
          if (line.startsWith("#") || line.isBlank()) {
            continue;
          }
          String[] fields = line.split("\t");
          if (fields.length < 2 || fields.length > 3)
            throw new IOException("unexpected file format in line " + reader.getLineNumber() + ": " + line);
          Page page = new Page();
          if (fields[0].equals("R")) {
            page.setTarget(null);
          } else {
            try {
              page.setLevel(Integer.parseInt(fields[0]));
            } catch (NumberFormatException ex) {
              throw new IOException("unexpected page type " + fields[0] + " in line " + reader.getLineNumber());
            }
          }
          page.setPage(fields[1]);
          if (fields.length > 2) {
            page.setName(fields[2]);
          }
          addPage(page);
        }
      }
    }

    private void addPage(Page page) {
      if (page.isRedirect()) {
        redirects.add(page);
      } else {
        pages.add(page);
      }
    }

    public Collection<? extends Page> getPages() {
      return Collections.unmodifiableList(pages);
    }

    public Collection<? extends Page> getRedirects() {
      return Collections.unmodifiableList(redirects);
    }

    public Page findPage(String pageName) {
      return pageLookup.get(pageName.split("#")[0]);
    }

    private static final String URL_ENCODING = "utf-8";

    public static String urlencode(String string) {
      try {
        return URLEncoder.encode(string, URL_ENCODING);
      } catch (UnsupportedEncodingException e) {
        log(e);
        return "###internal error###";
      }
    }

    public static String urldecode(String string) {
      try {
        return URLDecoder.decode(string, URL_ENCODING);
      } catch (UnsupportedEncodingException e) {
        log(e);
        return "###internal error###";
      }

    }

    public static String normalize(String string) {
      return string.replace(" ", "_");
    }

    public static String denormalize(String string) {
      return string.replace("_", " ");
    }

  }

  public static class Page {

    private boolean redirect;
    private int level;
    private String page;
    private String name;
    private String text;
    private HTMLDocument document;
    private String target;
    private boolean empty;
    private String file;

    public void setTarget(String target) {
      redirect = true;
      this.target = target;
    }

    public String getTarget() {
      return target;
    }

    public boolean isRedirect() {
      return redirect;
    }

    public void setLevel(int level) {
      this.level = level;
    }

    public int getLevel() {
      return level;
    }

    @Override
    public String toString() {
      if (redirect)
        return "R-" + page + "->" + target;
      else
        return level + "-\"" + page + (name != null ? ("\"-\"" + name) : "") + "\"";
    }

    public void setPage(String string) {
      page = PageInfo.denormalize(string);// PageInfo.normalize(string);
      file = PageInfo.getHelpfilename(page, false);
    }

    public void setName(String string) {
      name = string;
    }

    public String getName() {
      return name == null ? page : name;
    }

    public String getText() {
      return text;
    }

    private void setText(String text) {
      this.text = text;
    }

    public String getPage() {
      return page;
    }

    public String getFile() {
      return file;
    }

    public HTMLDocument getDocument() {
      return document;
    }

    public void setDocument(HTMLDocument doc) {
      try {
        setText(doc.getText(0, doc.getEndPosition().getOffset()));
        document = doc;
        try {
          document.setBase(new URL("file:"));
        } catch (MalformedURLException e) {
          // TODO Auto-generated catch block
          log(e);
        }
      } catch (BadLocationException e) {
        setText(e.getMessage());
      }
    }

    @Override
    public int hashCode() {
      return page.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof Page)
        return page.equals(((Page) obj).page);
      return false;
    }

    public void setEmpty(boolean b) {
      empty = b;
    }

    public boolean isEmpty() {
      return empty;
    }

  }

  private static final String BASE_URL = "https://wiki.eressea.de/index.php?title=";
  private static final String FILE_ENCODING = "utf-8";
  private static final String RAW_ACTION = "&redirect=no&action=raw";
  private static final String HTML_ACTION = "&redirect=no";
  private static final String HTML_ENCODING = "iso-8859-1";
  private static final String HTML_LANGUAGE = "de";

  private static final String RULES_HTML_TEMPLATE =
      "<!DOCTYPE html>\n" +
          "<html lang=\"%s\">\n" +
          "<head>\n" +
          "<meta charset=\"%s\" />\n" +
          "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />\n" +
          "<meta name=\"keywords\" content=\"Magellan, Dokumentation, Eressea, PbeM, JavaClient\" />\n" +
          "<meta name=\"generator\" content=\"magellan.client.utils.GetHelp\">" +
          "<meta name=\"description\" content=\"Magellan Dokumentation\" />\n" +
          "\n" +
          "<title>%s</title>\n" +
          "\n" +
          "<link rel=\"stylesheet\" href=\"./default.css\" type=\"text/css\" />\n" +
          "</head>\n" +
          "<body><div id='mh-content'>\n" +
          "<h1>%s</h1>\n" +
          "<div id='gh-content'></div>\n" +
          "</div>\n" +
          "</body>\n" +
          "</html>";
  private JList<Page> pageList;
  private JEditorPane pageArea;
  private DefaultListModel<Page> pageModel;
  private boolean raw;
  private PageInfo pInfo;
  private static magellan.library.utils.logging.Logger log;

  // public static void main(String[] args) throws FileNotFoundException, IOException {
  // new GetHelp().run();
  // }

  GetHelp() throws FileNotFoundException, IOException {
    initGUI();
    run();
  }

  private void initGUI() {
    setLayout(new BorderLayout());
    pageArea = new JEditorPane("text/plain", "Load a help pagelist file");
    // pageArea.setWrapStyleWord(true);
    // pageArea.setLineWrap(true);
    pageArea.setContentType("text/plain");
    pageArea.setEditable(false);
    pageArea.addHyperlinkListener((e) -> hyperlinkUpdate(e));

    JScrollPane pageScroller = new JScrollPane(pageArea);
    pageScroller.setPreferredSize(new Dimension(500, 500));
    add(pageScroller, BorderLayout.CENTER);

    pageList = new JList<Page>(pageModel = new DefaultListModel<Page>());
    pageList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    pageList.setLayoutOrientation(JList.VERTICAL);
    JScrollPane listScroller = new JScrollPane(pageList);
    listScroller.setPreferredSize(new Dimension(250, 80));
    add(listScroller, BorderLayout.WEST);

    JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
    buttonBar.add(new JButton(new AbstractAction("Load") {
      public void actionPerformed(ActionEvent e) {
        loadList();
      }
    }));
    buttonBar.add(new JButton(new AbstractAction("Save") {
      public void actionPerformed(ActionEvent e) {
        writeHelp();
      }
    }));
    add(buttonBar, BorderLayout.SOUTH);

    pageList.addListSelectionListener((e) -> selected(e));
  }

  private void hyperlinkUpdate(HyperlinkEvent e) {
    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
      if (e.getURL() != null) {
        String pageName = e.getURL().getPath();
        if (pageName != null) {
          Page page = findPage(pageName.replaceFirst(".html", ""));
          if (page != null) {
            showPage(page);
          } else {
            log("no page " + pageName);
          }
        } else {
          log("did not find " + e.getURL());
        }
      }
    }
  }

  private String getTargetFromUrl(String path) {
    String[] parts = path.split("/index.php/");
    if (parts.length == 2) {
      String pageName = null;
      pageName = PageInfo.getHelpfilename(PageInfo.urldecode(parts[1]), false);
      return pageName;
    }
    return null;
  }

  {
    log = magellan.library.utils.logging.Logger.getInstance("GetHelp");
    Logger.setLevel(Logger.INFO);
    Logger.activateDefaultLogListener(true);
  }

  private static void log(Throwable t) {
    log(null, t);
  }

  private static void log(Object message) {
    log(message, null);
  }

  private static void log(Object message, Throwable t) {
    if (message != null) {
      log.info(message.toString());
    } else if (t != null) {
      StringWriter s;
      t.printStackTrace(new PrintWriter(s = new StringWriter()));
      log.info(s.toString());
    } else {
      log.info("null");
    }
  }

  private void selected(ListSelectionEvent e) {
    if (!e.getValueIsAdjusting()) {
      showPage(pageList.getSelectedValue());
    }
  }

  private Page findPage(String pageName) {
    Page p = pInfo.findPage(pageName);
    if (p == null)
      return null;
    if (p.getTarget() == null) {
      getPage(p);
    }
    while (p != null && p.isRedirect()) {
      p = pInfo.findPage(p.getTarget());
    }
    return p;
  }

  private void showPage(Page page) {
    if (page.getDocument() == null) {
      getPage(page);
    }
    if (page.getDocument() == null) {
      if (page.getText() == null) {
        pageArea.setText("page not found");
      } else {
        pageArea.setText(page.getText());
      }
      pageArea.setContentType("text/plain");
    } else {
      pageArea.setDocument(page.getDocument());
      // pageArea.setText(page.getText());
      pageArea.setContentType(getContentType());
    }
  }

  private String getContentType() {
    return raw ? "text/plain" : "text/html";
  }

  private void run() throws FileNotFoundException, IOException {
    pInfo = new PageInfo();
    // pInfo.read(PAGE_FILE);
    pageModel.addAll(pInfo.getPages());
  }

  Set<Page> getPageStack = new HashSet<Page>();

  private void getPage(Page page) {
    DocumentBuilder builder;
    try {
      if (getPageStack.contains(page)) {
        log("circular page link from/to page " + page);
        return;
      }
      getPageStack.add(page);
      builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document parsed = builder.parse(getPageUrl(page));

      Element content = getContent(parsed);
      if (content == null) {
        page.setText("error getting page");
        return;
      }

      clearLinks(content, page);

      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      transformerFactory = TransformerFactory.newDefaultInstance();
      Transformer transformer = transformerFactory.newTransformer();
      DOMSource source = new DOMSource(content);
      StringWriter strWriter = new StringWriter();
      StreamResult result = new StreamResult(strWriter);
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      transformer.transform(source, result);

      HTMLEditorKit kit = new HTMLEditorKit();
      HTMLDocument doc = (HTMLDocument) kit.createDefaultDocument();
      kit.read(new StringReader(
          String.format(RULES_HTML_TEMPLATE, HTML_LANGUAGE, HTML_ENCODING, page.getName(), page.getName())), doc, 0);
      doc.setInnerHTML(doc.getElement("gh-content"), strWriter.toString());
      page.setDocument(doc);
    } catch (ParserConfigurationException e) {
      page.setText(e.getMessage());
      log(e);
    } catch (SAXException e) {
      page.setText(e.getMessage());
      log(e);
    } catch (FileNotFoundException e) {
      log("page not found: " + page);
      page.setEmpty(true);
    } catch (IOException e) {
      page.setText(e.toString());
      page.setDocument(getEmptyDoc());
      log(e);
    } catch (BadLocationException e) {
      page.setText(e.getMessage());
      log(e);
    } catch (TransformerConfigurationException e) {
      page.setText(e.getMessage());
      log(e);
    } catch (TransformerException e) {
      page.setText(e.getMessage());
      log(e);
    } finally {
      getPageStack.remove(page);
    }
  }

  private HTMLDocument getEmptyDoc() {
    HTMLEditorKit kit = new HTMLEditorKit();
    HTMLDocument doc = (HTMLDocument) kit.createDefaultDocument();
    try {
      doc.setInnerHTML(doc.getDefaultRootElement(), "<body></body>");
    } catch (BadLocationException | IOException e) {
      log(e);

    }
    return doc;
  }

  private void clearLinks(Element e, Page page) {
    // replace wiki links with Target.html#Ref
    NodeList aList = e.getElementsByTagName("a");
    for (int i = 0; i < aList.getLength(); ++i) {
      Node div = aList.item(i);
      Node href = div.getAttributes().getNamedItem("href");
      if (href == null) {
        log("empty href " + div + " on page " + page);
      } else {
        URL link = null;
        try {
          link = new URL("file", "", href.getNodeValue());
        } catch (MalformedURLException | DOMException e1) {
          log("messed up href " + href + "in page " + page);
          return;
        }
        String target = getTargetFromUrl(link.getFile());
        String ref = link.getRef();
        if (target == null) {
          if (link.getFile().isEmpty() && !link.getRef().isEmpty()) {
            // internal link
          } else {
            try {
              link = new URL(href.getNodeValue());
              log("external link " + href.getNodeValue() + " in page " + page);
            } catch (MalformedURLException | DOMException e1) {
              if (!link.getFile().isEmpty() || link.getRef().isEmpty()) {
                log("empty link " + link + " in page " + page);
              }
            }
          }
        } else {
          Page targetPage = pInfo.findPage(target);
          while (targetPage != null && targetPage.isRedirect()) {
            if (targetPage.getDocument() == null) {
              getPage(targetPage);
            }
            target = targetPage.getTarget();
            if (target == null) {
              log("invalid redirect target in page " + targetPage);
              break;
            }
            try {
              link = new URL("file", "", target);
              target = link.getFile();
              ref = link.getRef();
              targetPage = pInfo.findPage(target);
            } catch (MalformedURLException | DOMException e1) {
              log("messed up href " + href + "in page " + page);
              return;
            }
          }
          if (targetPage == null) {
            log("dead link " + link + " in page " + page);
          } else {
            if (target != null) {
              href.setNodeValue(PageInfo.getHelpfilename(PageInfo.normalize(target), true) +
                  (ref == null ? "" : ("#" + (ref))));
            } else {
              log("bad redirect " + href.getNodeValue() + " in page " + page);
            }
          }
        }
      }
    }

    // <div id="mw-content-text" lang="de" dir="ltr" class="mw-content-ltr"><div class="mw-parser-output"><div
    // class="redirectMsg"><p>Weiterleitung nach:</p><ul class="redirectText"><li><a href="/index.php/Schiff"
    // title="Schiff">Schiff</a></li></ul></div>

    // set redirect targets
    if (aList.getLength() == 1) {
      String redirect = aList.item(0).getAttributes().getNamedItem("href").getNodeValue();
      if (redirect != null) {
        if (!page.isRedirect()) {
          log("found redirect for non-redirect page " + page);
        } else {
          page.setTarget(redirect.replaceFirst(".html", ""));
        }
      }
    }
  }

  private Element getContent(Document parsed) {
    Node content = null;
    NodeList nl = parsed.getElementsByTagName("div");
    for (int i = 0; i < nl.getLength(); ++i) {
      Node div = nl.item(i);
      Node id = div.getAttributes().getNamedItem("id");
      if (id != null && id.getNodeValue().equals("mw-content-text")) {
        content = div;
        break;
      }
    }
    return (Element) content;
  }

  private String getPageUrl(Page page) {
    return BASE_URL + PageInfo.urlencode(page.getPage()) + (raw ? RAW_ACTION : HTML_ACTION);
  }

  private void loadList() {
    File dir = new File("help/de");
    JFileChooser fc = new JFileChooser(dir);
    fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
    fc.setMultiSelectionEnabled(false);
    fc.setAcceptAllFileFilterUsed(true);
    fc.setSelectedFile(new File(dir, "pagelist.txt"));
    fc.setDialogTitle("Load page list file");
    if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
      File file = fc.getSelectedFile();
      if (!file.exists() || !file.isFile() || !file.canRead()) {
        JOptionPane.showMessageDialog(this, "File not found: " + file);
      } else {
        log("opening " + file);
        pInfo.read(file.getAbsolutePath());
        pageModel.clear();
        pageModel.addAll(pInfo.getPages());
      }
    }

  }

  private static final String TOC_HEADER = "<?xml version='1.0' encoding='ISO-8859-1' ?>\n" +
      "<!DOCTYPE toc\n" +
      "  PUBLIC \"-//Sun Microsystems Inc.//DTD JavaHelp TOC Version 1.0//EN\"\n" +
      "         \"http://java.sun.com/products/javahelp/toc_1_0.dtd\">\n" +
      " \n" +
      "<toc version=\"1.0\">\n\n";

  private static final String TOC_FOOTER = "\n</toc>\n";
  private static final String TOC_ITEM = "<tocitem text=\"%s\" image=\"%s\" target=\"%s\"%s>\n";
  private static final String TOC_CLOSE = "</tocitem>\n";
  private static final String JHM_HEADER = "<?xml version='1.0' encoding='ISO-8859-1' ?>\n" +
      "<!DOCTYPE map\n" +
      "  PUBLIC \"-//Sun Microsystems Inc.//DTD JavaHelp Map Version 1.0//EN\"\n" +
      "         \"http://java.sun.com/products/javahelp/map_1_0.dtd\">\n" +
      "\n" +
      "<map version=\"1.0\">\n";
  private static final String JHM_FOOTER = "</map>\n";
  private static final String JHM_ITEM = "<mapID target=\"%s\" url=\"eressea/%s\" />\n";

  private void writeHelp() {
    File dir = new File(".", "help");
    JFileChooser fc = new JFileChooser(dir);
    fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    fc.setMultiSelectionEnabled(false);
    fc.setAcceptAllFileFilterUsed(false);
    fc.setDialogTitle("Select an empty directory for the new help files");
    if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
      File[] content = fc.getSelectedFile().listFiles();
      if (content == null) {
        log("cannot list directory");
      } else if (content.length > 0) {
        log("directory not empty");
      } else {
        log("start writing ...");
        for (Page p : pInfo.getPages())
          if (!p.isEmpty() && p.getDocument() == null) {
            getPage(p);
          }

        writeToc(fc.getSelectedFile());
        writeMap(fc.getSelectedFile());
        writePages(fc.getSelectedFile());
        log("...done");
      }
    }

  }

  private void writePages(File dir) {
    File eressea = new File(dir, "eressea");
    eressea.mkdir();
    for (Page p : pInfo.getPages()) {
      File helpFile = new File(eressea, PageInfo.getHelpfilename(p.isEmpty() ? p.getName() : p.getPage(), true));
      if (helpFile.exists()) {
        log("duplicate help file name " + helpFile.getPath());
      }
      if (p.isEmpty()) {
        log("skipping empty page " + p);
      } else {
        try (FileWriter writer = new FileWriter(helpFile);) {
          HTMLEditorKit kit = new HTMLEditorKit();
          HTMLDocument doc = p.getDocument();
          if (doc == null) {
            log("no document in page " + p);
          } else {

            kit.write(writer, doc, doc.getStartPosition().getOffset(), doc.getLength());
          }
        } catch (IOException e) {
          log(e);
        } catch (BadLocationException e) {
          log(e);
        }
      }
    }
  }

  private void writeMap(File dir) {
    File toc = new File(dir, "eressea.jhm");
    try (FileWriter writer = new FileWriter(toc);) {
      writer.write(JHM_HEADER);
      for (Page p : pInfo.getPages()) {
        writeMapItem(writer, p);
      }
      writer.write(JHM_FOOTER);
    } catch (IOException e) {
      log(e);
    }
  }

  private void writeMapItem(FileWriter writer, Page item) throws IOException {
    writer.write(String.format(indent(item.getLevel()) + JHM_ITEM,
        "",
        PageInfo.getHelpfilename(item.isEmpty() ? item.getName() : item.getPage(), false),
        PageInfo.getHelpfilename(item.isEmpty() ? item.getName() : item.getPage(), true)));
  }

  private void writeToc(File dir) {
    File toc = new File(dir, "eresseaTOC.xml");
    try (FileWriter writer = new FileWriter(toc);) {
      Page previousItem = null, firstItem = null;
      writer.write(TOC_HEADER);
      for (Page p : pInfo.getPages()) {
        if (previousItem != null) {
          if (p.getLevel() > previousItem.getLevel()) {
            writeTocItem(writer, previousItem, false);
          } else if (p.getLevel() < previousItem.getLevel()) {
            writeTocItem(writer, previousItem, true);
            for (int l = previousItem.getLevel(); l > p.getLevel(); --l) {
              closeTocItem(writer, l);
            }
          } else {
            writeTocItem(writer, previousItem, true);
          }
        }
        previousItem = p;
        if (firstItem == null) {
          firstItem = p;
        }
      }
      if (previousItem != null && firstItem != null) {
        writeTocItem(writer, previousItem, true);
        for (int l = previousItem.getLevel(); l > firstItem.getLevel(); --l) {
          closeTocItem(writer, l);
        }
      }
      writer.write(TOC_FOOTER);
    } catch (IOException e) {
      log(e);
    }
  }

  private void closeTocItem(Writer writer, int level) throws IOException {
    writer.write(String.format(indent(level - 1) + TOC_CLOSE, ""));
  }

  private void writeTocItem(Writer writer, Page item, boolean closeTag) throws IOException {
    // <tocitem text=\"Eressea Kurzreferenz\" image=\"openbook\" target=\"eressea_intro\">\n"
    String icon;
    if (item.getLevel() == 0) {
      icon = "openbook";
    } else {
      icon = closeTag ? "topic" : "chapter";
    }
    writer.write(String.format(indent(item.getLevel()) + TOC_ITEM,
        "",
        item.getName(),
        icon,
        PageInfo.getHelpfilename(item.isEmpty() ? item.getName() : item.getPage(), false),
        closeTag ? "/" : ""));
  }

  private String indent(int level) {
    return "%" + (level * 2 + 2) + "s";
  }

  private static void createAndShowGUI() {
    JFrame frame = new JFrame("Help Parser");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    try {
      frame.add(new GetHelp());
    } catch (Throwable t) {
      log(t);
      JOptionPane.showMessageDialog(null, t.toString());
      return;
    }
    frame.setLocationByPlatform(true);
    frame.pack();
    frame.setVisible(true);
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        createAndShowGUI();
      }
    });
  }

}
