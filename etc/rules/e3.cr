VERSION 65
RULES "$Id: e2k9.cr 001 2009-06-07 stm $"

MAGELLAN
"magellan.library.gamebinding.e3a.E3ASpecificStuff";class
"ERESSEA";orderFileStartingString

OPTIONCATEGORY "REPORT"
"REPORT";name
"true";order
1;bitmask

OPTIONCATEGORY "COMPUTER"
"COMPUTER";name
"true";order
2;bitmask

OPTIONCATEGORY "ZUGVORLAGE"
"ZUGVORLAGE";name
"true";order
4;bitmask

OPTIONCATEGORY "STATISTIK"
"STATISTIK";name
"true";order
16;bitmask

OPTIONCATEGORY "DEBUG"
"DEBUG";name
"false";order
32;bitmask

OPTIONCATEGORY "ZIPPED"
"ZIPPED";name
"true";order
64;bitmask

OPTIONCATEGORY "ZEITUNG"
"ZEITUNG";name
"false";order
128;bitmask

OPTIONCATEGORY "ADRESSEN"
"ADRESSEN";name
"true";order
512;bitmask

OPTIONCATEGORY "BZIP2"
"BZIP2";name
"true";order
1024;bitmask

OPTIONCATEGORY "PUNKTE"
"PUNKTE";name
"false";order
2048;bitmask

OPTIONCATEGORY "SHOWSKCHANGE"
"SHOWSKCHANGE";name
"false";order
4096;bitmask

ALLIANCECATEGORY "ALLES"
"ALL";name
27;bitmask

ALLIANCECATEGORY "SILBER"
"SILVER";name
"ALLES";parent
1;bitmask

ALLIANCECATEGORY "GIB"
"GIVE";name
"ALLES";parent
8;bitmask

ALLIANCECATEGORY "BEWACHEN"
"GUARD";name
"ALLES";parent
16;bitmask

ALLIANCECATEGORY "KÄMPFE"
"COMBAT";name
2;bitmask

ITEMCATEGORY "silver"
"Silber";name
0;naturalorder

ITEMCATEGORY "weapons"
"Waffen";name
0;naturalorder

ITEMCATEGORY "front weapons"
"Front-Waffen";name
0;naturalorder
"weapons";parent

ITEMCATEGORY "distance weapons"
"Distanz-Waffen";name
1;naturalorder
"weapons";parent

ITEMCATEGORY "ammunition"
"Munition";name
2;naturalorder
"weapons";parent

ITEMCATEGORY "armour"
"Rüstungen";name
1;naturalorder

ITEMCATEGORY "shield"
"Schilde";name
0;naturalorder
"armour";parent

ITEMCATEGORY "resources"
"Ressourcen";name
2;naturalorder

ITEMCATEGORY "luxuries"
"Luxusgüter";name
3;naturalorder

ITEMCATEGORY "herbs"
"Kräuter";name
"kraeuter";iconname
4;naturalorder

ITEMCATEGORY "potions"
"Tränke";name
5;naturalorder

ITEMCATEGORY "trophies"
"Trophäen";name
6;naturalorder

ITEMCATEGORY "misc"
"Sonstiges";name
7;naturalorder

SKILLCATEGORY "war"
"Kampf";name
0;naturalorder

SKILLCATEGORY "magic"
"Magie";name
1;naturalorder

SKILLCATEGORY "resource"
"Resourcen-Gewinnung";name
2;naturalorder

SKILLCATEGORY "silver"
"Silber-Gewinnung";name
0;naturalorder
"resource";parent

SKILLCATEGORY "build"
"Bauen";name
3;naturalorder

SKILLCATEGORY "movement"
"Bewegung";name
4;naturalorder

SKILLCATEGORY "trade"
"Handel";name
5;naturalorder

SKILLCATEGORY "misc"
"Sonstiges";name
6;naturalorder

HERB "Flachwurz"
"Flachwurz";name
"Ebene";region
"herbs";category
"flatroot";iconname

HERB "Würziger Wagemut"
"Würziger Wagemut";name
"Ebene";region
"herbs";category
"tangy temerity";iconname

HERB "Eulenauge"
"Eulenauge";name
"Ebene";region
"herbs";category
"owlsgaze";iconname

HERB "Grüner Spinnerich"
"Grüner Spinnerich";name
"Wald";region
"herbs";category
"spider ivy";iconname

HERB "Blauer Baumringel"
"Blauer Baumringel";name
"Wald";region
"herbs";category
"cobalt fungus";iconname

HERB "Elfenlieb"
"Elfenlieb";name
"Wald";region
"herbs";category
"elvendear";iconname

HERB "Gurgelkraut"
"Gurgelkraut";name
"Sumpf";region
"herbs";category
"bugleweed";iconname

HERB "Knotiger Saugwurz"
"Knotiger Saugwurz";name
"Sumpf";region
"herbs";category
"knotroot";iconname

HERB "Blasenmorchel"
"Blasenmorchel";name
"Sumpf";region
"herbs";category
"bubblemorel";iconname

HERB "Wasserfinder"
"Wasserfinder";name
"Wüste";region
"herbs";category
"waterfinder";iconname

HERB "Kakteenschwitz"
"Kakteenschwitz";name
"Wüste";region
"herbs";category
"peyote";iconname

HERB "Sandfäule"
"Sandfäule";name
"Wüste";region
"herbs";category
"sandreeker";iconname

HERB "Windbeutel"
"Windbeutel";name
"Hochland";region
"herbs";category
"windbag";iconname

HERB "Fjordwuchs"
"Fjordwuchs";name
"Hochland";region
"herbs";category
"fjord fungus";iconname

HERB "Alraune"
"Alraune";name
"Hochland";region
"herbs";category
"mandrake";iconname

HERB "Steinbeißer"
"Steinbeißer";name
"Gebirge";region
"herbs";category
"rockweed";iconname

HERB "Spaltwachs"
"Spaltwachs";name
"Gebirge";region
"herbs";category
"gapgrowth";iconname

HERB "Höhlenglimm"
"Höhlenglimm";name
"Gebirge";region
"herbs";category
"cavelichen";iconname

HERB "Eisblume"
"Eisblume";name
"Gletscher";region
"herbs";category
"ice begonia";iconname

HERB "Weißer Wüterich"
"Weißer Wüterich";name
"Gletscher";region
"herbs";category
"white hemlock";iconname

HERB "Schneekristall"
"Schneekristall";name
"Gletscher";region
"herbs";category
"snowcrystal petal";iconname



SKILL "Armbrustschießen"
"Armbrustschießen";name
"war";category
SKILL "Ausdauer"
"Ausdauer";name
"misc";category
SKILL "Bergbau"
"Bergbau";name
"resource";category
SKILL "Bogenschießen"
"Bogenschießen";name
"war";category
SKILL "Burgenbau"
"Burgenbau";name
"build";category
SKILL "Hiebwaffen"
"Hiebwaffen";name
"war";category
SKILL "Holzfällen"
"Holzfällen";name
"resource";category
SKILL "Katapultbedienung"
"Katapultbedienung";name
"war";category
SKILL "Magie"
"Magie";name
"magic";category
COSTS
100;1
200;2
350;3
550;4
800;5
1100;6
1450;7
1850;8
2300;9
2800;10
3350;11
3950;12
4600;13
5300;14
6050;15
6850;16
7700;17
8600;18
9550;19
10550;20
11600;21
12700;22
13850;23
15050;24
16300;25
17600;26
18950;27
20350;28
21800;29
23300;30
24850;31
26450;32
28100;33
29800;34
31550;35
33350;36
35200;37
37100;38
39050;39
41050;40
43100;41
45200;42
47350;43
49550;44
51800;45
54100;46
56450;47
58850;48
61300;49
63800;50
SKILL "Pferdedressur"
"Pferdedressur";name
"resource";category
SKILL "Reiten"
"Reiten";name
"movement";category
SKILL "Rüstungsbau"
"Rüstungsbau";name
"build";category
SKILL "Schiffbau"
"Schiffbau";name
"build";category
SKILL "Segeln"
"Segeln";name
"movement";category
SKILL "Stangenwaffen"
"Stangenwaffen";name
"war";category
SKILL "Steinbau"
"Steinbau";name
"resource";category
SKILL "Straßenbau"
"Straßenbau";name
"build";category
SKILL "Taktik"
"Taktik";name
"war";category
500;cost
SKILL "Waffenbau"
"Waffenbau";name
"build";category
SKILL "Waffenloser Kampf"
"Waffenloser Kampf";name
"war";category
SKILL "Wagenbau"
"Wagenbau";name
"build";category

RACE "Zwerge"
"Zwerge";name
240;recruitmentcosts
"Zwerg";recruitmentname
10;weight
5.4;capacity
TALENTBONI
2;Bergbau
-1;Bogenschießen
2;Burgenbau
1;Hiebwaffen
-1;Holzfällen
2;Katapultbedienung
0;Magie
-2;Pferdedressur
-2;Reiten
2;Rüstungsbau
-1;Schiffbau
-2;Segeln
2;Steinbau
2;Straßenbau
2;Waffenbau
TALENTBONI "Berge"
1;Taktik
TALENTBONI "Gletscher"
1;Taktik

RACE "Orks"
"Orks";name
100;recruitmentcosts
"Ork";recruitmentname
10;weight
5.4;capacity
TALENTBONI
1;Bergbau
1;Burgenbau
1;Holzfällen
0;Magie
-1;Pferdedressur
-1;Schiffbau
-1;Segeln
1;Steinbau
1;Taktik
2;Waffenbau
-1;Wagenbau

RACE "Elfen"
"Elfen";name
200;recruitmentcosts
"Elf";recruitmentname
10;weight
5.4;capacity
TALENTBONI
-2;Bergbau
2;Bogenschießen
-1;Burgenbau
-2;Katapultbedienung
0;Magie
2;Pferdedressur
-1;Rüstungsbau
-1;Schiffbau
-1;Segeln
-1;Steinbau
-1;Straßenbau
TALENTBONI "Wald"
2;Taktik

RACE "Dämonen"
"Dämonen";name
360;recruitmentcosts
"Dämon";recruitmentname
10;weight
5.4;capacity
TALENTBONI
1;Hiebwaffen
1;Holzfällen
-3;Pferdedressur
-1;Reiten
-1;Schiffbau
-1;Segeln
1;Stangenwaffen
-1;Taktik
1;Waffenbau
-2;Wagenbau

RACE "Halblinge"
"Halblinge";name
100;recruitmentcosts
"Halbling";recruitmentname
10;weight
5.4;capacity
TALENTBONI
2;Armbrustschießen
1;Bergbau
-1;Bogenschießen
1;Burgenbau
-1;Hiebwaffen
-1;Katapultbedienung
-1;Pferdedressur
-1;Reiten
-1;Schiffbau
-2;Segeln
-1;Stangenwaffen
1;Straßenbau
2;Wagenbau

RACE "Menschen"
"Menschen";name
100;recruitmentcosts
"Mensch";recruitmentname
10;weight
5.4;capacity
TALENTBONI
2;Reiten
2;Schiffbau
2;Segeln

RACE "Goblins"
"Goblins";name
60;recruitmentcosts
"Goblin";recruitmentname
6;maintenance
6;weight
4.4;capacity
TALENTBONI
1;Bergbau
1;Burgenbau
1;Katapultbedienung
0;Magie
-2;Schiffbau
-2;Segeln
-2;Straßenbau
-2;Taktik
-1;Wagenbau

RACE "Trolle"
"Trolle";name
260;recruitmentcosts
"Troll";recruitmentname
20;weight
10.8;capacity
TALENTBONI
1;Bergbau
-2;Bogenschießen
2;Burgenbau
1;Hiebwaffen
2;Katapultbedienung
-1;Pferdedressur
0;Reiten
2;Rüstungsbau
-1;Schiffbau
-1;Segeln
2;Steinbau
2;Straßenbau
-1;Taktik

RACE "Drachen"
"Drachen";name

RACE "Dracoide"
"Dracoide";name

RACE "Jungdrachen"
"Jungdrachen";name

RACE "Schattendämonen"
"Schattendämonen";name
0;maintenance
5;weight

RACE "Schattenmeister"
"Schattenmeister";name
0;maintenance
5;weight

RACE "Bauern"
"Bauern";name
10;weight

RACE "Ents"
"Ents";name
50;weight

RACE "Schneemänner"
"Schneemänner";name
5;weight

RACE "Untote"
"Untote";name
0;maintenance
10;weight

RACE "Zombies"
"Zombies";name
0;maintenance
10;weight

RACE "Skelette"
"Skelette";name
0;maintenance
5;weight

RACE "Ghoule"
"Ghoule";name
0;maintenance
10;weight

RACE "Kröten"
"Kröten";name
1;weight

RACE "Adler"
"Adler";name
0;maintenance
5;weight

RACE "Delphine"
"Delphine";name
0;maintenance
5;weight

RACE "Einhörner"
"Einhörner";name
0;maintenance
50;weight

RACE "Eulen"
"Eulen";name
0;maintenance
5;weight

RACE "Geister"
"Geister";name
5;weight

RACE "Luchse"
"Luchse";name
0;maintenance
5;weight

RACE "Nymphen"
"Nymphen";name
0;maintenance
10;weight

RACE "Ratten"
"Ratten";name
0;maintenance
1;weight

RACE "Riesenschildkröten"
"Riesenschildkröten";name
0;maintenance
16;weight

RACE "Teufelchen"
"Teufelchen";name
5;weight
2;capacity

RACE "Tunnelwürmer"
"Tunnelwürmer";name
0;maintenance
300;weight

RACE "Wölfe"
"Wölfe";name
5;weight

ITEM "Silber"
"Silber";name
0.01;weight
"silver";category
1;storeinbonw

ITEM "Juwel"
"Juwel";name
1;weight
"luxuries";category
1;storeinbonw
RESOURCES
7;Silber

ITEM "Weihrauch"
"Weihrauch";name
2;weight
"luxuries";category
1;storeinbonw

ITEM "Balsam"
"Balsam";name
2;weight
"luxuries";category
1;storeinbonw

ITEM "Gewürz"
"Gewürz";name
2;weight
"luxuries";category
1;storeinbonw

ITEM "Myrrhe"
"Myrrhe";name
2;weight
"luxuries";category
1;storeinbonw

ITEM "Öl"
"Öl";name
3;weight
"luxuries";category
1;storeinbonw

ITEM "Seide"
"Seide";name
3;weight
"luxuries";category
1;storeinbonw

ITEM "Eisen"
"Eisen";name
5;weight
"Bergbau";makeskill
"resources";category

ITEM "Laen"
"Laen";name
2;weight
"Bergbau";makeskill
"resources";category

ITEM "Adamantium"
"Adamantium";name
2;weight
"resources";category

ITEM "Holz"
"Holz";name
5;weight
"Holzfällen";makeskill
"resources";category

ITEM "Bäume"
"Bäume";name
5;weight
"Holzfällen";makeskill
"resources";category

ITEM "Schößlinge"
"Schößlinge";name
5;weight
"Holzfällen";makeskill
"resources";category

ITEM "Mallorn"
"Mallorn";name
5;weight
"Holzfällen";makeskill
"resources";category

ITEM "Mallornschößlinge"
"Mallornschößlinge";name
5;weight
"Holzfällen";makeskill
"resources";category

ITEM "Stein"
"Stein";name
60;weight
"Steinbau";makeskill
"resources";category

ITEM "Steine"
"Steine";name
60;weight
"Steinbau";makeskill
"resources";category

ITEM "Pferd"
"Pferd";name
50;weight
"Pferdedressur";makeskill
1;makeskilllevel
"resources";category

ITEM "Pferde"
"Pferde";name
50;weight
"Pferdedressur";makeskill
1;makeskilllevel
"resources";category

ITEM "Streitross"
"Streitross";name
50;weight
"Pferdedressur";makeskill
4;makeskilllevel
"resources";category
RESOURCES
1;Pferd
1;Eisen
200;Silber

ITEM "Same"
"Same";name
0.1;weight
"Kräuterkunde";makeskill
3;makeskilllevel
"resources";category

ITEM "Mallornsame"
"Mallornsame";name
0.1;weight
"Kräuterkunde";makeskill
4;makeskilllevel
"resources";category

ITEM "Wagen"
"Wagen";name
40;weight
"Wagenbau";makeskill
1;makeskilllevel
"resources";category
RESOURCES
5;Holz

ITEM "Katapult"
"Katapult";name
100;weight
"Wagenbau";makeskill
"Katapultbedienung";useskill
5;makeskilllevel
"distance weapons";category
RESOURCES
10;Holz

ITEM "Katapultmunition"
"Katapultmunition";name
10;weight
"Steinbau";makeskill
"Katapultbedienung";useskill
3;makeskilllevel
"ammunition";category
1;storeinbonw
RESOURCES
1;Stein

ITEM "Schwert"
"Schwert";name
1;weight
"Waffenbau";makeskill
"Hiebwaffen";useskill
3;makeskilllevel
"weapons";category
1;storeinbonw
RESOURCES
1;Eisen

ITEM "Schartiges Schwert"
"Schartiges Schwert";name
1;weight
"Hiebwaffen";useskill
"weapons";category
1;storeinbonw

ITEM "Runenschwert"
"Runenschwert";name
1;weight
"Hiebwaffen";useskill
"weapons";category
RESOURCES
3000;Silber

ITEM "Laenschwert"
"Laenschwert";name
1;weight
"Waffenbau";makeskill
"Hiebwaffen";useskill
8;makeskilllevel
"weapons";category
1;storeinbonw
RESOURCES
1;Laen

ITEM "Flammenschwert"
"Flammenschwert";name
1;weight
"Hiebwaffen";useskill
"weapons";category
1;storeinbonw

ITEM "Bihänder"
"Bihänder";name
2;weight
"Waffenbau";makeskill
"Hiebwaffen";useskill
4;makeskilllevel
"weapons";category
1;storeinbonw
RESOURCES
2;Eisen

ITEM "Rostiger Zweihänder"
"Rostiger Zweihänder";name
2;weight
"Hiebwaffen";useskill
"weapons";category
1;storeinbonw

ITEM "Speer"
"Speer";name
1;weight
"Waffenbau";makeskill
"Stangenwaffen";useskill
2;makeskilllevel
"weapons";category
1;storeinbonw
RESOURCES
1;Holz

ITEM "Lanze"
"Lanze";name
2;weight
"Waffenbau";makeskill
"Stangenwaffen";useskill
2;makeskilllevel
"weapons";category
1;storeinbonw
RESOURCES
2;Holz

ITEM "Hellebarde"
"Hellebarde";name
2;weight
"Waffenbau";makeskill
"Stangenwaffen";useskill
3;makeskilllevel
"weapons";category
1;storeinbonw
RESOURCES
1;Eisen
2;Holz

ITEM "Rostige Hellebarde"
"Rostige Hellebarde";name
2;weight
"Stangenwaffen";useskill
"weapons";category
1;storeinbonw

ITEM "Kriegsaxt"
"Kriegsaxt";name
2;weight
"Waffenbau";makeskill
"Hiebwaffen";useskill
3;makeskilllevel
"weapons";category
1;storeinbonw
RESOURCES
1;Eisen
1;Holz

ITEM "Adamantiumaxt"
"Adamantiumaxt";name
1;weight
"weapons";category

ITEM "Rostige Kriegsaxt"
"Rostige Kriegsaxt";name
2;weight
"Hiebwaffen";useskill
"weapons";category
1;storeinbonw

ITEM "Armbrust"
"Armbrust";name
1;weight
"Waffenbau";makeskill
"Armbrustschießen";useskill
3;makeskilllevel
"distance weapons";category
1;storeinbonw
RESOURCES
1;Holz

ITEM "Repetierarmbrust"
"Repetierarmbrust";name
1;weight
"Waffenbau";makeskill
"Armbrustschießen";useskill
6;makeskilllevel
"distance weapons";category
1;storeinbonw
RESOURCES
1;Holz
1;Eisen

ITEM "Bogen"
"Bogen";name
1;weight
"Waffenbau";makeskill
"Bogenschießen";useskill
2;makeskilllevel
"distance weapons";category
1;storeinbonw
RESOURCES
1;Holz

ITEM "Elfenbogen"
"Elfenbogen";name
1;weight
"Waffenbau";makeskill
"Bogenschießen";useskill
5;makeskilllevel
"distance weapons";category
1;storeinbonw
RESOURCES
2;Mallorn

ITEM "Mallornbogen"
"Mallornbogen";name
1;weight
"Waffenbau";makeskill
"Bogenschießen";useskill
5;makeskilllevel
"distance weapons";category
1;storeinbonw
RESOURCES
1;Mallorn

ITEM "Mallornarmbrust"
"Mallornarmbrust";name
1;weight
"Waffenbau";makeskill
"Armbrustschießen";useskill
5;makeskilllevel
"distance weapons";category
1;storeinbonw
RESOURCES
1;Mallorn

ITEM "Mallornlanze"
"Mallornlanze";name
2;weight
"Waffenbau";makeskill
"Stangenwaffen";useskill
5;makeskilllevel
"weapons";category
1;storeinbonw
RESOURCES
2;Mallorn

ITEM "Mallornspeer"
"Mallornspeer";name
1;weight
"Waffenbau";makeskill
"Stangenwaffen";useskill
5;makeskilllevel
"weapons";category
1;storeinbonw
RESOURCES
1;Mallorn

ITEM "Kettenhemd"
"Kettenhemd";name
2;weight
"Rüstungsbau";makeskill
3;makeskilllevel
"armour";category
1;storeinbonw
RESOURCES
3;Eisen

ITEM "Rostiges Kettenhemd"
"Rostiges Kettenhemd";name
"armour";category
2;weight
1;storeinbonw

ITEM "Laenkettenhemd"
"Laenkettenhemd";name
1;weight
"Rüstungsbau";makeskill
9;makeskilllevel
"armour";category
1;storeinbonw
RESOURCES
3;Laen

ITEM "Plattenpanzer"
"Plattenpanzer";name
4;weight
"Rüstungsbau";makeskill
4;makeskilllevel
"armour";category
1;storeinbonw
RESOURCES
5;Eisen

ITEM "Adamantiumrüstung"
"Adamantiumrüstung";name
1;weight
"armour";category

ITEM "Rostiger Plattenpanzer"
"Rostiger Plattenpanzer";name
4;weight
"armour";category
1;storeinbonw

ITEM "Schild"
"Schild";name
1;weight
"Rüstungsbau";makeskill
2;makeskilllevel
"shield";category
1;storeinbonw
RESOURCES
1;Eisen

ITEM "Turmschild"
"Turmschild";name
2;weight
"Rüstungsbau";makeskill
6;makeskilllevel
"shield";category
1;storeinbonw
RESOURCES
2;Eisen

ITEM "Rostiger Schild"
"Rostiger Schild";name
"shield";category
1;weight
1;storeinbonw

ITEM "Rostiges Schild"
"Rostiger Schild";name
"shield";category
1;weight
1;storeinbonw

ITEM "Laenschild"
"Laenschild";name
"Rüstungsbau";makeskill
7;makeskilllevel
"shield";category
1;storeinbonw
RESOURCES
1;Laen

ITEM "Mantel der Unverletzlichkeit"
"Mantel der Unverletzlichkeit";name
"armour";category
RESOURCES
3000;Silber

ITEM "Siebenmeilentee"
"Siebenmeilentee";name
"Alchemie";makeskill
"potions";category
2;makeskilllevel
RESOURCES
1;Blauer Baumringel
1;Windbeutel

ITEM "Goliathwasser"
"Goliathwasser";name
"Alchemie";makeskill
"potions";category
2;makeskilllevel
RESOURCES
1;Gurgelkraut
1;Fjordwuchs


ITEM "Wasser des Lebens"
"Wasser des Lebens";name
"Alchemie";makeskill
"potions";category
2;makeskilllevel
RESOURCES
1;Elfenlieb
1;Knotiger Saugwurz


ITEM "Trank der Wahrheit"
"Trank der Wahrheit";name
"Alchemie";makeskill
"potions";category
2;makeskilllevel
RESOURCES
1;Flachwurz
1;Fjordwuchs


ITEM "Schaffenstrunk"
"Schaffenstrunk";name
"Alchemie";makeskill
4;makeskilllevel
"potions";category
RESOURCES
1;Alraune
1;Spaltwachs
1;Würziger Wagemut


ITEM "Wundsalbe"
"Wundsalbe";name
"Alchemie";makeskill
4;makeskilllevel
"potions";category
RESOURCES
1;Weißer Wüterich
1;Blauer Baumringel
1;Würziger Wagemut

ITEM "Bauernblut"
"Bauernblut";name
"Alchemie";makeskill
4;makeskilllevel
"potions";category
RESOURCES
1;Höhlenglimm
1;Fjordwuchs
1;Blauer Baumringel
1;Bauer

ITEM "Gehirnschmalz"
"Gehirnschmalz";name
"Alchemie";makeskill
6;makeskilllevel
"potions";category
RESOURCES
1;Wasserfinder
1;Steinbeißer
1;Windbeutel
1;Gurgelkraut

ITEM "Dumpfbackenbrot"
"Dumpfbackenbrot";name
"Alchemie";makeskill
6;makeskilllevel
"potions";category
RESOURCES
1;Eulenauge
1;Grüner Spinnerich
1;Höhlenglimm
1;Fjordwuchs

ITEM "Nestwärme"
"Nestwärme";name
"Alchemie";makeskill
6;makeskilllevel
"potions";category
RESOURCES
1;Eisblume;
1;Grüner Spinnerich
1;Spaltwachs
1;Kakteenschwitz

ITEM "Pferdeglück"
"Pferdeglück";name
"Alchemie";makeskill
6;makeskilllevel
"potions";category
RESOURCES
1;Blauer Baumringel
1;Sandfäule
1;Kakteenschwitz
1;Knotiger Saugwurz

ITEM "Berserkerblut"
"Berserkerblut";name
"Alchemie";makeskill
6;makeskilllevel
"potions";category
RESOURCES
1;Weißer Wüterich
1;Alraune
1;Flachwurz
1;Sandfäule

ITEM "Bauernlieb"
"Bauernlieb";name
"Alchemie";makeskill
8;makeskilllevel
"potions";category
RESOURCES
1;Alraune
1;Schneekristall
1;Steinbeißer
1;Blasenmorchel
1;Elfenlieb

ITEM "Elixier der Macht"
"Elixier der Macht";name
"Alchemie";makeskill
8;makeskilllevel
"potions";category
RESOURCES
1;Elfenlieb
1;Wasserfinder
1;Windbeutel
1;Grüner Spinnerich
1;Blasenmorchel
1;Drachenblut

ITEM "Heiltrank"
"Heiltrank";name
"Alchemie";makeskill
8;makeskilllevel
"potions";category
RESOURCES
1;Gurgelkraut
1;Windbeutel
1;Eisblume
1;Elfenlieb
1;Spaltwachs

ITEM "Phiole"
"Phiole";name
"potions";category
"traenke";iconname

ITEM "Kraeuterbeutel"
"Kräuterbeutel";name
"herbs";category
"kraeuterbeutel";iconname

ITEM "Silberbeutel"
"Silberbeutel";name
"silver";category

ITEM "Silberkassette"
"Silberkassette";name
"silver";category

ITEM "Trollhorn"
"Trollhorn";name
"trophies";category
0.01;weight
1;storeinbonw

ITEM "Zwergenbart"
"Zwergenbart";name
"trophies";category
0.01;weight
1;storeinbonw

ITEM "Halblingfuß"
"Halblingfuß";name
"trophies";category
0.01;weight
1;storeinbonw

ITEM "Elfenohr"
"Elfenohr";name
"trophies";category
0.01;weight
1;storeinbonw

ITEM "Dämonenblut"
"Dämonenblut";name
"trophies";category
0.01;weight
1;storeinbonw

ITEM "Goblinkopf"
"Goblinkopf";name
"trophies";category
0.01;weight
1;storeinbonw

ITEM "Menschenskalp"
"Menschenskalp";name
"trophies";category
0.01;weight
1;storeinbonw

ITEM "Orkhauer"
"Orkhauer";name
"trophies";category
0.01;weight
1;storeinbonw

ITEM "Zauberbeutel"
"Zauberbeutel";name
"misc";category
1;weight
0;storeinbonw

ITEM "Sphäre der Unsichtbarkeit"
"Sphäre der Unsichtbarkeit";name
"misc";category
1;weight
1;storeinbonw

ITEM "Tiegel mit Krötenschleim"
"Tiegel mit Krötenschleim";name
"misc";category
1;weight
1;storeinbonw

ITEM "Magischer Kräuterbeutel"
"Magischer Kräuterbeutel";name
1;weight
"misc";category
1;storeinbonw

ITEM "Seeschlangenkopf"
"Seeschlangenkopf";name
5;weight
"misc";category
1;storeinbonw

ITEM "Drachenkopf"
"Drachenkopf";name
5;weight
"misc";category
1;storeinbonw

ITEM "Ring der flinken Finger"
"Ring der flinken Finger";name
0;weight
"misc";category
1;storeinbonw

ITEM "Drachenblut"
"Drachenblut";name
"misc";category
1;weight
0;storeinbonw

SHIPTYPE "Einbaum"
"Einbaum";name
3;size
1;level
3;range
20;capacity
2;persons
1;captainlevel
2;sailorlevel
RAWMATERIALS
1;Holz

SHIPTYPE "Floß"
"Floß";name
10;size
1;level
1;range
500;capacity
5;persons
1;captainlevel
5;sailorlevel

SHIPTYPE "Kutter"
"Kutter";name
10;size
2;level
2;range
50;capacity
5;persons
2;captainlevel
5;sailorlevel

SHIPTYPE "Barke"
"Barke";name
10;size
2;level
3;range
50;capacity
10;persons
2;captainlevel
5;sailorlevel

SHIPTYPE "Kogge"
"Kogge";name
100;size
4;level
4;range
2000;capacity
50;persons
4;captainlevel
20;sailorlevel

SHIPTYPE "Karavelle"
"Karavelle";name
300;size
6;level
4;range
6000;capacity
150;persons
6;captainlevel
30;sailorlevel

SHIPTYPE "Fregatte"
"Fregatte";name
100;size
5;level
4;range
1000;capacity
110;persons
5;captainlevel
40;sailorlevel
RAWMATERIALS
1;Holz
10;Silber

SHIPTYPE "Galeone"
"Galeone";name
300;size
7;level
4;range
3000;capacity
310;persons
7;captainlevel
60;sailorlevel
RAWMATERIALS
1;Holz
10;Silber

SHIPTYPE "Drachenschiff"
"Drachenschiff";name
100;size
5;level
6;range
500;capacity
110;persons
5;captainlevel
60;sailorlevel
RAWMATERIALS
1;Holz
10;Silber

SHIPTYPE "Trireme"
"Trireme";name
300;size
7;level
6;range
1500;capacity
310;persons
7;captainlevel
90;sailorlevel
RAWMATERIALS
1;Holz
10;Silber

CASTLETYPE "Grundmauern"
"Grundmauern";name
1;level
1;minsize
1;maxsize
11;wage
0;tradetax
RAWMATERIALS
1;Stein

CASTLETYPE "Handelsposten"
"Handelsposten";name
1;level
2;minsize
9;maxsize
11;wage
0;tradetax
RAWMATERIALS
1;Stein

CASTLETYPE "Befestigung"
"Befestigung";name
2;level
10;minsize
49;maxsize
12;wage
6;tradetax
RAWMATERIALS
1;Stein

CASTLETYPE "Turm"
"Turm";name
3;level
50;minsize
249;maxsize
13;wage
12;tradetax
RAWMATERIALS
1;Stein

CASTLETYPE "Burg"
"Burg";name
4;level
250;minsize
1249;maxsize
14;wage
18;tradetax
RAWMATERIALS
1;Stein

CASTLETYPE "Festung"
"Festung";name
5;level
1250;minsize
6249;maxsize
15;wage
24;tradetax
RAWMATERIALS
1;Stein

CASTLETYPE "Zitadelle"
"Zitadelle";name
6;level
6250;minsize
16;wage
30;tradetax
RAWMATERIALS
1;Stein

CASTLETYPE "Gerüst"
"Gerüst";name
1;level
1;minsize
4;maxsize
RAWMATERIALS
1;Holz
REGIONTYPES
"Berge"
"Ebene"
"Gletscher"
"Hochland"
"Sumpf"
"Wüste"
"Wald"
"Vulkan"
"Aktiver Vulkan"

CASTLETYPE "Wachstube"
"Wachstube";name
2;level
5;minsize
9;maxsize
RAWMATERIALS
1;Holz
REGIONTYPES
"Berge"
"Ebene"
"Gletscher"
"Hochland"
"Sumpf"
"Wüste"
"Wald"
"Vulkan"
"Aktiver Vulkan"

CASTLETYPE "Wachturm"
"Wachturm";name
2;level
10;minsize
10;maxsize
RAWMATERIALS
1;Holz
REGIONTYPES
"Berge"
"Ebene"
"Gletscher"
"Hochland"
"Sumpf"
"Wüste"
"Wald"
"Vulkan"
"Aktiver Vulkan"

BUILDINGTYPE "Leuchtturm"
"Leuchtturm";name
3;level
MAINTENANCE
100;Silber
RAWMATERIALS
2;Stein
1;Holz
1;Eisen
100;Silber
REGIONTYPES
"Berge"
"Ebene"
"Gletscher"
"Hochland"
"Sumpf"
"Wüste"
"Wald"
"Vulkan"
"Aktiver Vulkan"

BUILDINGTYPE "Bergwerk"
"Bergwerk";name
4;level
MAINTENANCE
500;Silber
RAWMATERIALS
5;Stein
10;Holz
1;Eisen
250;Silber
TALENTBONI
1;Bergbau
REGIONTYPES
"Berge"
"Ebene"
"Gletscher"
"Hochland"
"Sumpf"
"Wüste"
"Wald"
"Vulkan"
"Aktiver Vulkan"

BUILDINGTYPE "Steinbruch"
"Steinbruch";name
2;level
MAINTENANCE
250;Silber
RAWMATERIALS
1;Stein
5;Holz
1;Eisen
250;Silber
TALENTBONI
1;Steinbau
REGIONTYPES
"Berge"
"Ebene"
"Gletscher"
"Hochland"
"Sumpf"
"Wüste"
"Wald"
"Vulkan"
"Aktiver Vulkan"

BUILDINGTYPE "Sägewerk"
"Sägewerk";name
3;level
MAINTENANCE
250;Silber
RAWMATERIALS
5;Stein
5;Holz
3;Eisen
200;Silber
TALENTBONI
1;Holzfällen
REGIONTYPES
"Berge"
"Ebene"
"Gletscher"
"Hochland"
"Sumpf"
"Wüste"
"Wald"
"Vulkan"
"Aktiver Vulkan"

BUILDINGTYPE "Schmiede"
"Schmiede";name
3;level
MAINTENANCE
300;Silber
MAINTENANCE
1;Holz
TALENTBONI
1;Rüstungsbau
1;Waffenbau
RAWMATERIALS
5;Stein
5;Holz
2;Eisen
200;Silber
REGIONTYPES
"Berge"
"Ebene"
"Gletscher"
"Hochland"
"Sumpf"
"Wüste"
"Wald"
"Vulkan"
"Aktiver Vulkan"

BUILDINGTYPE "Pferdezucht"
"Pferdezucht";name
2;level
MAINTENANCE
150;Silber
RAWMATERIALS
2;Stein
4;Holz
1;Eisen
100;Silber
REGIONTYPES
"Berge"
"Ebene"
"Gletscher"
"Hochland"
"Sumpf"
"Wüste"
"Wald"
"Vulkan"
"Aktiver Vulkan"

BUILDINGTYPE "Hafen"
"Hafen";name
3;level
25;maxsize
MAINTENANCE
250;Silber
RAWMATERIALS
5;Stein
5;Holz
250;Silber
REGIONTYPES
"Berge"
"Ebene"
"Gletscher"
"Hochland"
"Sumpf"
"Wüste"
"Wald"
"Vulkan"
"Aktiver Vulkan"

BUILDINGTYPE "Taverne"
"Taverne";name
2;level
MAINTENANCE
5;Silber pro Größenpunkt
RAWMATERIALS
1;Eisen
4;Stein
3;Holz
200;Silber
REGIONTYPES
"Berge"
"Ebene"
"Gletscher"
"Hochland"
"Sumpf"
"Wüste"
"Wald"
"Vulkan"
"Aktiver Vulkan"

BUILDINGTYPE "Akademie"
"Akademie";name
3;level
25;maxsize
MAINTENANCE
1000;Silber
RAWMATERIALS
5;Stein
5;Holz
1;Eisen
500;Silber
REGIONTYPES
"Berge"
"Ebene"
"Gletscher"
"Hochland"
"Sumpf"
"Wüste"
"Wald"
"Vulkan"
"Aktiver Vulkan"

BUILDINGTYPE "Magierturm"
"Magierturm";name
5;level
50;maxsize
MAINTENANCE
1000;Silber
RAWMATERIALS
5;Stein
3;Holz
2;Mallorn
3;Eisen
2;Laen
500;Silber
TALENTBONI
1;Magie
REGIONTYPES
"Berge"
"Ebene"
"Gletscher"
"Hochland"
"Sumpf"
"Wüste"
"Wald"
"Vulkan"
"Aktiver Vulkan"

BUILDINGTYPE "Karawanserei"
"Karawanserei";name
2;level
10;maxsize
MAINTENANCE
3000;Silber
2;Pferd
RAWMATERIALS
1;Stein
5;Holz
1;Eisen
500;Silber
REGIONTYPES
"Wüste"

BUILDINGTYPE "Damm"
"Damm";name
4;level
50;maxsize
MAINTENANCE
1000;Silber
3;Holz
RAWMATERIALS
5;Stein
10;Holz
1;Eisen
500;Silber
REGIONTYPES
"Sumpf"

BUILDINGTYPE "Tunnel"
"Tunnel";name
6;level
100;maxsize
MAINTENANCE
100;Silber
2;Stein
RAWMATERIALS
10;Stein
5;Holz
1;Eisen
300;Silber
REGIONTYPES
"Gletscher"

BUILDINGTYPE "Monument"
"Monument";name
4;level
RAWMATERIALS
1;Stein
1;Holz
1;Eisen
400;Silber
REGIONTYPES
"Berge"
"Ebene"
"Gletscher"
"Hochland"
"Sumpf"
"Wüste"
"Wald"
"Vulkan"
"Aktiver Vulkan"

BUILDINGTYPE "Markt"
"Markt";name
1;level
10;maxsize
MAINTENANCE
200;Silber
RAWMATERIALS
1;Stein
1;Holz
REGIONTYPES
"Berge"
"Ebene"
"Gletscher"
"Hochland"
"Sumpf"
"Wüste"
"Wald"
"Vulkan"
"Aktiver Vulkan"

REGIONTYPE "Berge"
"Berge";name
1000;maxworkers
250;roadstones
"true";isAstralVisible

REGIONTYPE "Ebene"
"Ebene";name
10000;maxworkers
50;roadstones
"true";isAstralVisible

REGIONTYPE "Gletscher"
"Gletscher";name
100;maxworkers
250;roadstones
"Tunnel";roadsupportbuilding
"true";isAstralVisible

REGIONTYPE "Eisberg"
"Eisberg";name
0;maxworkers
"false";isAstralVisible

REGIONTYPE "Hochland"
"Hochland";name
4000;maxworkers
100;roadstones
"true";isAstralVisible

REGIONTYPE "Sumpf"
"Sumpf";name
2000;maxworkers
75;roadstones
"Damm";roadsupportbuilding
"true";isAstralVisible

REGIONTYPE "Wüste"
"Wüste";name
500;maxworkers
100;roadstones
"Karawanserei";roadsupportbuilding
"true";isAstralVisible

REGIONTYPE "Wald"
"Wald";name
10000;maxworkers
50;roadstones
"true";isAstralVisible

REGIONTYPE "Ozean"	
"Ozean";name
0;maxworkers
"true";isOcean
"false";isAstralVisible

REGIONTYPE "Mahlstrom"	
"Mahlstrom";name
0;maxworkers
"true";isOcean
"false";isAstralVisible

REGIONTYPE "Feuerwand"
"Feuerwand";name
0;maxworkers
"false";isAstralVisible

REGIONTYPE "Vulkan"
"Vulkan";name
500;maxworkers
250;roadstones
"true";isAstralVisible

REGIONTYPE "Aktiver Vulkan"
"Aktiver Vulkan";name
500;maxworkers
250;roadstones
"true";isAstralVisible
