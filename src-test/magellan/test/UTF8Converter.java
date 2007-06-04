/*
 * class magellan.testUTF8Converter
 * created on 26.07.2006
 *
 * $Revision: $
 * $Date: $
 */
package magellan.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 * Diese Klasse bietet einige Funktionen für die Konvertierung von Code
 * in und nach UTF8. Diese Klasse wurde benutzt, um das gesamte
 * Projekt auf UTF8-Codierung zu hiefen.  
 *
 * @author Thoralf Rickert
 * @version 1.0, erstellt am 26.07.2006
 */
public class UTF8Converter {
  private static final boolean verifyUTF8 = false;
  
  public static void main(String[] args) {
    if (args.length == 0) {
      args = new String[1];
      args[0] = "D:\\Eclipse Workspace\\Magellan2";
    }
    
    File file = new File(args[0]);
    if (!file.exists()) {
      System.err.println("Das Verzeichnis/die Datei "+file+" existiert nicht. Bitte geben Sie es beim Start an.");
      System.exit(1);
    }
    System.out.println("Verify "+file);
    
    UTF8Converter converter = new UTF8Converter();
    converter.check(file);
    //converter.convert(file);
    
  }
  
  /**
   * Mit Hilfe dieser Methode kann man prüfen, ob die Datei oder der Inhalt
   * des Verzeichnisses Probleme mit der Kodierung hat.
   */
  public void check(File file) {
    convert(file,file,true);
  }
  
  /**
   * Mit Hilfe dieser Methode kann man prüfen, ob die Datei oder der Inhalt
   * des Verzeichnisses Probleme mit der Kodierung hat. Wenn ja, dann wird 
   * die Datei ersetzt.
   */
  public void convert(File file) {
    convert(file,file,false);
  }
  
  private void convert(File root, File file, boolean convert) {
    if (isIgnoreFile(file)) return;
    try {
      if (file.isDirectory()) {
        File[] files = file.listFiles();
        for (File f : files) {
          convert(root,f,convert);
        }
      } else {
        byte[] cache = loadFile(file);
        boolean utf8 = checkUTF8(file.getAbsolutePath().substring(root.getAbsolutePath().length()), cache);
        boolean iso8859 = checkISO8859(file.getAbsolutePath().substring(root.getAbsolutePath().length()), cache);
        /*
        if (utf8) {
          System.out.println("[utf-8]    "+file);
        } else if (iso8859) {
          System.out.println("[iso-8859] "+file);
        } else {
          System.out.println("[unknown]  "+file);
        }
        */
        if (convert) {
          String data = null;
          if (utf8) {
            data = convertUTF8(file, cache);
          } else if (iso8859) {
            data = convertISO8859(file, cache);
          }
          
          if (data != null) {
            saveFile(file, data);
          }
        }
      }
    } catch (Exception exception) {
      exception.printStackTrace(System.err);
    }
  }
  
  /**
   * Diese Methode konvertiert doppelt kodierte
   * UTF-8 Zeichen wieder zurück. Dreifachcodierung
   * verursacht irreperable Schäden (3-byte)
   */
  public String convertUTF8(File file, byte[] cache) throws Exception {
    String data = new String(cache,"UTF-8");
    data = data.replaceAll("Ã¼","ü");
    data = data.replaceAll("Ã¤","ä");
    data = data.replaceAll("Ã¶","ö");
    data = data.replaceAll("ÃŸ","ß");
    data = data.replaceAll("Ãœ","ü");
    
    return data;
  }
  
  /**
   * Diese Methode konvertiert noch nicht
   * in UTF-8 kodierte Daten.
   */
  public String convertISO8859(File file, byte[] cache) throws Exception {
    String data = new String(cache,"ISO-8859-1");
    data = data.replaceAll("0xE4","ä");
    data = data.replaceAll("0xF6","ö");
    data = data.replaceAll("0xFC","ü");
    data = data.replaceAll("0xC4","Ä");
    data = data.replaceAll("0xD6","Ö");
    data = data.replaceAll("0xDC","Ü");
    data = data.replaceAll("0xDF","ß");
    
    return data;
  }
  
  /**
   * Diese Methode liefert true zurück, wenn die Daten in cache 
   * Zeichen enthalten, die zweimal in UTF8 konvertiert wurden.
   * Geprüft wird momentan nur auf deutsche Umlaute.
   */
  public boolean checkUTF8(String fileName, byte[] cache) throws Exception {
    if (true) return false;
    boolean foundProblem = false;
    String data = new String(cache,"UTF-8");
    if (data.contains("Ã¼")) {
      System.out.println("Ã¼ "+fileName);
      foundProblem = true;
    } else if (data.contains("Ã¶")) {
      System.out.println("Ã¶ "+fileName);
      foundProblem = true;
    } else if (data.contains("Ã¤")) {
      System.out.println("Ã¤ "+fileName);
      foundProblem = true;
    } else if (data.contains("ÃŸ")) {
      System.out.println("ÃŸ "+fileName);
      foundProblem = true;
    } else if (data.contains("Ãœ")) {
      System.out.println("Ãœ "+fileName);
      foundProblem = true;
    } else if (data.contains("ï¿½")) {
      System.out.println("ï¿½ "+fileName);
      foundProblem = true;
    } else if (data.contains("ï¿½")) {
      System.out.println("ï¿½ "+fileName);
      foundProblem = true;
    } else if (data.indexOf((char)0xEF+""+(char)0xBF+""+(char)0xBD)>=0) {
      System.out.println((char)0xEF+""+(char)0xBF+""+(char)0xBD+" "+fileName);
      foundProblem = true;
    }
    
    return foundProblem;
  }
  
  /**
   * Diese Methode liefert true zurück, wenn die Daten in cache 
   * Zeichen enthalten, die im ISO8859-1 Zeichensatz verwendet
   * werden.
   * Geprüft wird momentan nur auf deutsche Umlaute.
   */
  public boolean checkISO8859(String fileName, byte[] cache) throws Exception {
    boolean foundProblem = false;
    
    String data = new String(cache,"ISO-8859-1");
    if (data.indexOf(0xE4)>=0) {
      System.out.println("ä "+fileName);
      foundProblem = true;
    } else if (data.indexOf(0xF6)>=0) {
      System.out.println("ö "+fileName);
      foundProblem = true;
    } else if (data.indexOf(0xFC)>=0) {
      System.out.println("ü "+fileName);
      foundProblem = true;
    } else if (data.indexOf(0xC4)>=0) {
      System.out.println("Ä "+fileName);
      foundProblem = true;
    } else if (data.indexOf(0xD6)>=0) {
      System.out.println("Ö "+fileName);
      foundProblem = true;
    } else if (data.indexOf(0xDC)>=0) {
      System.out.println("Ü "+fileName);
      foundProblem = true;
    } else if (data.indexOf(0xDF)>=0) {
      System.out.println("ß "+fileName);
      foundProblem = true;
//    } else if (data.indexOf((char)0xEF+""+(char)0xBF+""+(char)0xBD)>=0) {
//      System.out.println((char)0xEF+""+(char)0xBF+""+(char)0xBD+" "+fileName+" ????");
//      foundProblem = true;
    }
    
    return foundProblem;
  }
  
  
  /**
   * Diese Methode liefert true zurück, wenn eine
   * Datei nicht benutzt werden soll.
   */
  private boolean isIgnoreFile(File file) {
    if (file == null) return true;
    if (!file.exists()) return true;
    if (file.getName().equals(".svn")) return true;
    if (file.getName().equals(".settings")) return true;
    if (file.getName().equals(".classpath")) return true;
    if (file.getName().equals(".project")) return true;
    if (file.getName().equals("UTF8Converter.java")) return true;
    
    if (file.getName().toLowerCase().endsWith(".java")) return true;
    //if (file.getName().toLowerCase().endsWith(".properties")) return true;
    
    if (file.getName().toLowerCase().endsWith(".gif")) return true;
    if (file.getName().toLowerCase().endsWith(".jpg")) return true;
    if (file.getName().toLowerCase().endsWith(".png")) return true;
    if (file.getName().toLowerCase().endsWith(".ico")) return true;
    if (file.getName().toLowerCase().endsWith(".zip")) return true;
    if (file.getName().toLowerCase().endsWith(".jar")) return true;
    if (file.getName().toLowerCase().endsWith(".class")) return true;
    if (file.getName().toLowerCase().endsWith(".utf8")) return true;
    if (file.getName().toLowerCase().endsWith(".doc")) return true;
    if (file.getName().toLowerCase().endsWith(".bz2")) return true;
    if (file.getName().toLowerCase().endsWith(".gz")) return true;

    
    return false;
  }
  
  /**
   * Lädt eine Datei in ein Bytearray.
   */
  private byte[] loadFile(File file) throws Exception {
    byte[] cache = new byte[(int)file.length()];
    FileInputStream fr = new FileInputStream(file);
    fr.read(cache);
    fr.close();
    
    return cache;
  }
  
  /**
   * Diese Methode speichert die im String untergebrachten
   * Daten in einer UTF-8 Datei.
   */
  private void saveFile(File file, String data) throws Exception {
    FileOutputStream fos = new FileOutputStream(file);
    PrintStream ps = new PrintStream(fos,true,"UTF-8");
    ps.print(data);
    ps.close();
    fos.close();
  }
}
