// class magellan.library.utils.MarkovNameGenerator
// created on Jul 19, 2022
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
package magellan.library.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MarkovNameGenerator implements NameGenerator {
  public static class Markov {
    private static final int LEN = 2;
    private static final int MAX_PARTS = 2;

    private Map<Integer, Integer> parts;
    private int partSum;
    Map<Integer, Integer>[] lengths;
    private int[] lengthSum;
    Map<Character, Integer>[] starters;
    private int[] startSum;
    Map<String, Map<Character, Integer>>[] frequencies;
    Map<String, Integer> sums[];

    Random rng = new Random();

    private int warn;
    private String lastWarn;

    @SuppressWarnings("unchecked")
    protected Markov() {
      parts = new HashMap<>();
      lengths = new HashMap[MAX_PARTS + 1];
      lengthSum = new int[MAX_PARTS + 1];
      starters = new HashMap[MAX_PARTS + 1];
      startSum = new int[MAX_PARTS + 1];
      frequencies = new HashMap[MAX_PARTS + 1];
      sums = new HashMap[MAX_PARTS + 1];
      for (int p = 0; p < MAX_PARTS + 1; ++p) {
        lengths[p] = new HashMap<>();
        starters[p] = new HashMap<>();
        frequencies[p] = new HashMap<>();
        sums[p] = new HashMap<>();
      }
    }

    private static Markov create(String[] words) {
      Markov m = new Markov();
      for (String w : words) {
        m.add(w);
      }
      m.normalize();
      return m;
    }

    private void add(String word) {
      String[] wx = word.trim().split("[-\\s]+");
      increase(parts, wx.length);
      for (int p = 0; p < wx.length; ++p) {
        int pnum = Math.min(p, MAX_PARTS - 1);
        String w = wx[p];
        increase(lengths[pnum], w.length());
        String pre = "" + w.charAt(0);
        increase(starters[pnum], w.charAt(0));
        Map<String, Map<Character, Integer>> frecs = frequencies[pnum];
        if (frecs == null) {
          frecs = new HashMap<String, Map<Character, Integer>>();
        }
        for (int i = 1; i < w.length(); ++i) {
          char c = w.charAt(i);
          increase(frecs, pre, c);
          pre = pre + c;
          if (pre.length() > LEN) {
            pre = pre.substring(1);
          }
        }
        frequencies[pnum] = frecs;
      }
    }

    private <K, L> void increase(Map<K, Map<L, Integer>> map, K k1, L k2) {
      Map<L, Integer> map2 = map.get(k1);
      if (map2 == null) {
        map2 = new HashMap<L, Integer>();
      }
      increase(map2, k2);
      map.put(k1, map2);
    }

    private <K> void increase(Map<K, Integer> map, K key) {
      Integer v = map.get(key);
      if (v == null) {
        v = 0;
      }
      map.put(key, v + 1);
    }

    private void normalize() {
      for (int pnum = 0; pnum < MAX_PARTS + 1; ++pnum) {
        lengthSum[pnum] = count(lengths[pnum]);
        partSum = count(parts);
        startSum[pnum] = count(starters[pnum]);
        sums[pnum] = new HashMap<>();
        Map<String, Map<Character, Integer>> frecs = frequencies[pnum];
        for (String c : frecs.keySet()) {
          Map<Character, Integer> map2 = frecs.get(c);
          sums[pnum].put(c, count(map2));
        }
      }
    }

    private <K> Integer count(Map<K, Integer> map2) {
      int sum = 0;
      for (K c2 : map2.keySet()) {
        sum += map2.get(c2);
      }
      return sum;
    }

    public String generate() {
      int px = select(parts, partSum);
      String name = null;
      for (int p = 0; p < px; ++p) {
        int pnum = Math.min(p, MAX_PARTS - 1);
        int length = select(lengths[pnum], lengthSum[pnum]);
        StringBuilder word = new StringBuilder(length);
        Map<String, Map<Character, Integer>> frecs = frequencies[pnum];
        word.append(select(starters[pnum], startSum[pnum]));
        while (word.length() < length) {
          String pre = word.substring(word.length() - Math.min(word.length(), LEN), word.length());
          Character nextC = select(frecs.get(pre), sums[pnum].get(pre));
          if (nextC == null) {
            ++warn;
            lastWarn = word.toString();
            break;
          }
          word.append(nextC);
        }
        name = name == null ? word.toString() : name + " " + word.toString();
      }
      return name;
    }

    private <K> K select(Map<K, Integer> map, Integer sum) {
      if (map == null)
        return null;
      int r = rng.nextInt(sum);
      int s = 0;
      K last = null;
      for (K k : map.keySet()) {
        last = k;
        s += map.get(k);
        if (r < s)
          return k;
      }
      return last;
    }
  }

  public static void main(String[] args) {
    String[] words = new String[] { // "Hans", "Paul", "Peter", "Hermann" };
        "Joanna Seidel",
        "Almuth Hanisch",
        "Yvonne Esser",
        "Karina Grabowski",
        "Luise Borchert",
        "Thekla Kastner",
        "Anke Maas",
        "Nadja Wilke",
        "Henriette H�usler",
        "Eugenie Dreher",
        "Heinz-J�rgen H�fer",
        "Almut Eckardt",
        "Georgios Holst",
        "Hans-Christian Schr�der",
        "Reinhard Oswald",
        "Waltraut Reinhard",
        "Hans-Georg Kraus",
        "Kai Jacobi",
        "Ali R�mer",
        "Enno Jost",
        "Margarethe Peters",
        "Pamela Habermann",
        "Willibald Vo�",
        "Marlies Kern",
        "Natascha Albers",
        "Friederike Paulsen",
        "Karl Sch�rmann",
        "Isabel Metzner",
        "Ingolf Janssen",
        "Denis Faust",
        "Kornelia Gabriel",
        "Wilma Endres",
        "Corina Weiser",
        "Sylvia Br�ck",
        "Ursel Mende",
        "Dominik Noll",
        "Frieda Mai",
        "Sabina Brandl",
        "Fritz H�bner",
        "Sven Eggers",
        "Birgit Enders",
        "Wally Hausmann",
        "Norbert Sch�tte",
        "Christiane Winter",
        "Nathalie Otten",
        "Vitali Martin",
        "Markus Hartung",
        "Kay Busse",
        "Jean Baumgart",
        "Andreas Fritsche",
        "Henny Schroeder",
        "Ernestine Gebhardt",
        "Natalia Wendel",
        "Maximilian Ehrlich",
        "Fred Straub",
        "Edmund Pietsch",
        "Helmuth Tietz",
        "Markus Wulff",
        "Valeri Geisler",
        "Marietta Heider",
        "Hans-Walter Kranz",
        "Eleonore Mahler",
        "Mandy Ernst",
        "Otto B�hm",
        "Albrecht Kilian",
        "Bertram Stoll",
        "Bruno Kugler",
        "Gertraude B�hler",
        "Inna Gr�newald",
        "Helgard Morgenstern",
        "Gertraud Kohler",
        "Bert Reis",
        "Brunhild Wilke",
        "Klaus-Peter Vogler",
        "Gerti D�rr",
        "Peggy Budde",
        "Bernd H�hne",
        "Michele Stein",
        "Irmhild Brinkmann",
        "Benedikt Schuster",
        "Gertrude G�bel",
        "Christof Paul",
        "J�rg Thomas",
        "Ulla Seiler",
        "Franziska Sander",
        "Gerta Kleinert",
        "Ronny Raabe",
        "Rosalinde Stahl",
        "Jeanette Adam",
        "Marita Helm",
        "Eckard Jan�en",
        "Hedwig Ulbrich",
        "Hans-Werner Buchner",
        "Margareta B�hme",
        "Carolin B�chner",
        "August Kopp",
        "Carmen Funk",
        "Myriam Hohmann",
        "Susanne Volkmann",
        "Gesa Scherer",
        "Ekkehard Ebner",
        "Roswita Dreier",
        "Hanne Bolz",
        "Denis K�hl",
        "Egbert Ortmann",
        "Leopold Sch�nfeld",
        "Lisbeth Wichmann",
        "Simon Winkelmann",
        "Theresa Rahn",
        "Dorothee Schr�er",
        "Anika Fink",
        "Dimitrios Hartl",
        "Sandro Strobel",
        "Doreen Lampe",
        "Sigrid Grimm",
        "Klaudia Ahlers",
        "Eleonore Grote",
        "Georg Barth",
        "H�seyin Falk",
        "Heinz-Peter Wilke",
        "Salvatore Heise",
        "John Mohr",
        "Ricarda Otten",
        "Lore Funk",
        "Erich Schwab",
        "Waltraud Springer",
        "Philipp Klug",
        "Lieselotte Fuhrmann",
        "Hanno Kremer",
        "Rafael Volz",
        "Saskia Siegel",
        "Doris Schl�ter",
        "Katja Schweitzer",
        "Hatice Gerlach",
        "Volker Reitz",
        "Julia R�mer",
        "Christiana Petersen",
        "Ulli Maa�",
        "Marita Reichel",
        "Gertrude Baier",
        "Annerose Eggert",
        "Henning Siebert",
        "Barbara Reinhardt",
        "Sonja Grunwald",
        "Stefanie Mann",
        "Wanda Wegner",
        "Arnold Augustin",
        "Gottfried Gruber",
        "Irma Janssen",
        "Marie Kunze",
        "Carina Herrmann",
        "Jessica Gerlach",
        "Hilda Seidler",
        "Klaudia Riedel",
        "Isabel Schilling",
        "Annett Harder",
        "Alexandra Geisler",
        "Henrik Uhlig",
        "Gerold Forster",
        "Edgar Lerch",
        "Patricia Sch�ller",
        "Heino Dietze",
        "Marcel Zink",
        "Reinhardt Otto",
        "Pamela Kleinert",
        "Jonas Wiesner",
        "Sophia Fiebig",
        "Friederike Albert",
        "Monika Wilkens",
        "Guido Stahl",
        "Magdalena Eder",
        "Sven Haag",
        "Eduard Fr�hlich",
        "Reinhard Rupp",
        "Konstanze Gabriel",
        "Heinz-Peter Hildebrandt",
        "Anja Hopf",
        "Wally Burger",
        "Manja Jonas",
        "Ortwin Oppermann",
        "Antonius Seibel",
        "Christiane Kuhn",
        "Elisabeth Dittmann",
        "Marlies Ebel",
        "Meike Sperling",
        "Leonid Scheel",
        "Eveline Schramm",
        "Hinrich Grau",
        "Jana Eckardt",
        "Marie Teichmann",
        "Mareike Sch�ne",
        "Conny Heinen",
        "Erhard Mielke",
        "Jenny Simon",
        "Beatrice Balzer",
        "Pauline Hager",
        "Volkmar Hinrichs",
        "Larissa Hinze",
        "Leo Ehlert",
        "Alina Schmidt",
        "Axel Widmann",
        "Dennis Wolff",
        "G�nter Burger",
        "Sophia Otto",
        "Alexander Eberle",
        "Nina Cremer",
        "Insa Schneider",
        "Roberto Klemm",
        "Swetlana Bruhn",
        "Walter Armbruster",
        "Henning Scherer",
        "Klaus-J�rgen Buck",
        "Engelbert Weigel",
        "Christof Sp�th",
        "Ernst Oswald",
        "Margit Schwarze",
        "Imke Harms",
        "Emine Seibert",
        "Myriam Berger",
        "Murat Vetter",
        "Waldemar Huth",
        "Alois Zeidler",
        "Detlef Seidel",
        "Zita Endres",
        "Felicitas Winkler",
        "Franziska Ortmann",
        "Renate Herzog",
        "Hans-G�nter Reinke",
        "Gabriele Petri",
        "Eleonore B�hler",
        "Almuth Behr",
        "Saskia Ebner",
        "Alwin Dreyer",
        "Andy Becher",
        "Ines Fr�hlich",
        "Edith May",
        "Karen Orth",
        "Helga Menzel",
        "Thilo Daniel",
        "Isa Sperling",
        "Edward Graf",
        "Edwin Stoll",
        "Ivonne Kohler",
        "Kai-Uwe Kempf",
        "Siegbert Heckmann",
        "Ottmar Opitz",
        "Christine Wille",
        "Rosa Jacobi",
        "Irina Schilling",
        "Jeannette K�hnel",
        "J�rgen Mai",
        "Martina Klug",
        "Rico G�tze",
        "Johann Ostermann",
        "Hans-Walter Rudolph",
        "Christian Walther",
        "Dieter Busse",
        "Marius Bader",
        "Friedhelm Reitz",
        "Ria Beier",
        "Friederike Kunert",
        "Georgios Kirchhoff",
        "Wilma Balzer",
        "Inge Herold",
        "Heike Menzel",
        "Sascha Jacobs",
        "Hanno Ewald",
        "Heiner Sch�ne",
        "Leopold Schwarze",
        "Cornelia Heckmann",
        "Gerald Rudolf",
        "Rosi K�bler",
        "Marija Oswald",
        "Berthold Peter",
        "Andrew Sch�tz",
        "Wally Rademacher",
        "Antje Wessels",
        "Rene K�hn",
        "Heinz Sieber",
        "Karoline Grau",
        "Lina Steinbach",
        "Jolanta Petermann",
        "Heinz-Werner Schirmer",
        "Franz Just",
        "Heidemarie Schindler",
        "Brunhilde Rau",
        "Giesela Weise",
        "Volkmar Heim",
        "Marie Schwab",
        "Elfriede Hildebrand",
        "Carla Dittmann",
        "Hans-Peter Scholz",
        "Monica Gross",
        "Horst-Dieter Hoffmann",
        "Klaus-J�rgen Steinert",
        "R�diger Dreyer",
        "Mohammad Stern",
        "Simona Jansen",
        "Marliese Berthold",
        "Falk Rehm",
        "Lukas Eberle",
        "Gregor Fleischmann",
        "Heidi Gr�n",
        "Kathrin Bachmann",
        "Stephanie Grundmann",
        "Mike Noack",
        "Michael Hopp",
        "Gabriele B�hler",
        "Christl Hornung",
        "Konrad Reichelt",
        "Moritz Schulze",
        "Anke Westermann",
        "Axel Diehl",
        "Grete Zeidler",
        "Claus-Peter Franke",
        "Bianka Hamann",
        "Yusuf Leonhardt",
        "Evi Heitmann",
        "Ernst Breuer",
        "Marija Hempel",
        "Irmhild D�rr",
        "Heiner Lehnert",
        "Georg Fritsch",
        "Conny Thiele",
        "Hedi Weller",
        "Eike R�hl",
        "Pia Wegner",
        "Marianne Spindler",
        "Roswitha Metzner",
        "J�rg Will",
        "Karl-Friedrich Weiss",
        "Jens-Peter Knauer",
        "Rudi Thiemann",
        "Frauke Engels",
        "Kathleen Widmann",
        "Heinz-Werner Herrmann",
        "Oswald Walter",
        "Maritta Peter",
        "Guenter Bode",
        "Harri L�ck",
        "Liane Frisch",
        "Eberhard Ostermann",
        "Winfried Fischer",
        "Adam Heller",
        "Tina Kolb",
        "Carola Stein",
        "Yilmaz Krug",
        "Marga Jacobs",
        "Annett H�bner",
        "Eveline Dietz",

    };
    Markov markov = Markov.create(words);
    for (int i = 0; i < 100; ++i) {
      System.out.println(markov.generate());
    }
    if (markov.warn > 0) {
      System.out.println(markov.warn + " incomplete: " + markov.lastWarn);
    }
  }

  public boolean isActive() {
    // HIGHTODO Automatisch generierte Methode implementieren
    return false;
  }

  public boolean isAvailable() {
    // HIGHTODO Automatisch generierte Methode implementieren
    return false;
  }

  public void setEnabled(boolean available) {
    // HIGHTODO Automatisch generierte Methode implementieren

  }

  public String getName() {
    // HIGHTODO Automatisch generierte Methode implementieren
    return null;
  }

  public int getNamesCount() {
    // HIGHTODO Automatisch generierte Methode implementieren
    return 0;
  }

}
