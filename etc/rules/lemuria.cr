VERSION 69
RULES "$Id: lemuria.cr 69"

MAGELLAN
"magellan.library.gamebinding.EresseaSpecificStuff";class
"LEMURIA";orderFileStartingString

ORDER "COMMENT"
"// Text1*";syntax
"//";locale_de
"KOMMENTAR";locale_de

ORDER "PCOMMENT"
";";locale_de
1;internal

ORDER "ATTACK"
"ATTACKIEREN u1";syntax
"ANGREIFEN";locale_de
"ANGRIFF";locale_de
"ATTACKE"; locale_de
"ATTACKIERE";locale_de
"ATTACKIEREN";locale_de

ORDER "BANNER"
"BANNER Text";syntax
"BANNER";locale_de

ORDER "STEAL"
"STEHLEN u1";syntax
"BEKLAUE"; locale_de
"BEKLAUEN"; locale_de
"BESTEHLE";locale_de
"BESTEHLEN";locale_de
"DIEBSTAHL"; locale_de
"STEHLE";locale_de
"STEHLEN";locale_de

ORDER "SIEGE"
"BELAGERN b1";syntax
"BELAGERE";locale_de
"BELAGERN";locale_de
"BELAGERUNG";locale_de

ORDER "NAME"
"NAME [EINHEIT] | PARTEI | GEBÄUDE | BURG | SCHIFF | REGION | REICH Text";syntax
"BENENNE";locale_de
"BENENNEN";locale_de
"NAME";locale_de

ORDER "USE"
"BENUTZEN [1] Ding";syntax
"BENUTZE";locale_de
"BENUTZEN";locale_de

ORDER "DESCRIBE"
"TEXT [EINHEIT] | PARTEI | GEBÄUDE | BURG | SCHIFF | REGION | REICH Text";syntax
"BESCHREIBE";locale_de
"BESCHREIBEN";locale_de
"TEXT";locale_de

ORDER "ENTER"
"BETRETEN (b1 | s1 | BURG b1 | GEBÄUDE b1 | SCHIFF s1)";syntax
"BETRETE";locale_de
"BETRETEN";locale_de
"BESTEIGE";locale_de
"BESTEIGEN";locale_de

ORDER "GUARD"
"BEWACHEN [NICHT]";syntax
"BEWACHEN";locale_de
"BEWACHE";locale_de

ORDER "MESSAGE"
"BOTSCHAFT REGION | (SCHIFF s1) | (GEBÄUDE b2) | (BURG b3) | (EINHEIT u4) | (PARTEI f5)  Text";syntax
"BOTSCHAFT";locale_de

ORDER "DEFAULT"
"DEFAULT Order";syntax
"DEFAULT";locale_de
"VORLAGE";locale_de

ORDER "ALTERNATIVE"
"ALTERNATIVE Order";syntax
"ALTERNATIVE";locale_de

ORDER "UNIT"
"EINHEIT u1";syntax
"EINHEIT";locale_de
1;internal

ORDER "END"
"ENDE";syntax
"ENDE";locale_de

ORDER "FOLLOW"
"FOLGE (EINHEIT u1) | (SCHIFF s2)";syntax
"FOLGE";locale_de
"FOLGEN";locale_de

ORDER "RESEARCH"
"ERFORSCHEN";syntax
"FORSCHE";locale_de
"FORSCHEN";locale_de
"ERFORSCHEN";locale_de

ORDER "GIVE"
"GIB (u1|0) ( 1 | (JE 2) | ALLES  Ding | PERSONEN ) | ALLES | KRÄUTER | KOMMANDO | EINHEIT | LUXUSGÜTER | LUXUSWAREN | WAFFEN | RÜSTUNGEN | SCHILDE | KRÄUTER | TRÄNKE | TIERE | TRANSPORTER";syntax
"GIB";locale_de
"GEBEN";locale_de

ORDER "HELP"
"HELFE f1 ALLES|GIB|KÄMPFE|BEWACHE|SILBER|PARTEITARNUNG [NICHT] ";syntax
"HELFE";locale_de
"HELFEN";locale_de

ORDER "COMBAT"
"KÄMPFEN [AGGRESSIV|HINTEN|DEFENSIV|NICHT|FLIEHE|(HELFE [NICHT])]";syntax
"KÄMPFEN";locale_de
"KÄMPFE";locale_de

ORDER "COMBATSPELL"
"KAMPFZAUBER [STUFE 1] Zauber [NICHT]";syntax
"KAMPFZAUBER";locale_de

ORDER "BUY"
"KAUFEN 1 Luxus";syntax
"KAUFE";locale_de
"KAUFEN";locale_de

ORDER "CONTACT"
"KONTAKTIEREN (EINHEIT)|(FACTION) u1";syntax
"KONTAKTIERE";locale_de
"KONTAKTIEREN";locale_de

ORDER "TEACH"
"LEHREN u1+";syntax
"LEHRE";locale_de
"LEHREN";locale_de

ORDER "LEARN"
"LERNEN Talent [1]";syntax
"LERNE";locale_de
"LERNEN";locale_de

ORDER "LOCALE"
"LOCALE Sprache";syntax
"LOCALE";locale_de
1;internal

ORDER "MAKE"
"MACHEN [(TEMP u1 [Name]) | ([1] Ding [s1|b1]) | ([1] STRAßE Richtung)]";syntax
"MACHE";locale_de
"MACHEN";locale_de

ORDER "MOVE"
"NACH Richtung1+";syntax
"NACH";locale_de
"REISE";locale_de
"REISEN";locale_de

ORDER "NEXT"
"Invalid";syntax
"NÄCHSTER";locale_de
1;internal

ORDER "NUMBER"
"NUMMER (EINHEIT) | (SCHIFF) | (GEBÄUDE) | (BURG) | (PARTEI) | (GEGENSTAND) | (REICH) [x1]";syntax
"ID";locale_de
"NUMMER";locale_de

ORDER "REICH"
"REICH x1";syntax
"REICH";locale_de

ORDER "FACTION"
"PARTEI f1";syntax
"PARTEI";locale_de
1;internal

ORDER "REGION"
"REGION 1 , 2";syntax
"REGION";locale_de
1;internal

ORDER "RECRUIT"
"REKRUTIEREN 1";syntax
"REKRUTIERE";locale_de
"REKRUTIEREN";locale_de

ORDER "RESERVE"
"RESERVIEREN (1) | (ALLES) | (JE 2) Gegenstand";syntax
"RESERVIERE";locale_de
"RESERVIEREN";locale_de

ORDER "GRENZE"
"GRENZE (1) Gegenstand";syntax
"GRENZE";locale_de

ORDER "ROUTE"
"ROUTE Richtung1+";syntax
"ROUTE";locale_de

ORDER "SORT"
"SORTIEREN (VOR) | (HINTER) u1";syntax
"SORTIERE";locale_de
"SORTIEREN";locale_de

ORDER "SPY"
"SPIONIEREN u1";syntax
"SPIONIERE";locale_de
"SPIONIEREN";locale_de

ORDER "HIDE"
"TARNEN ([1]) | (Rasse) | (PARTEI [NICHT]) | (PARTEI NUMMER f1)";syntax
"TARNE";locale_de
"TARNEN";locale_de

ORDER "TAX"
"TREIBEN [1]";syntax
"TREIBE";locale_de
"TREIBEN";locale_de

ORDER "ENTERTAIN"
"UNTERHALTEN [1]";syntax
"UNTERHALTE";locale_de
"UNTERHALTEN";locale_de

ORDER "ORIGIN"
"URSPRUNG 1 2";syntax
"URSPRUNG";locale_de

ORDER "SELL"
"VERKAUFEN 1 | ALLES  Luxus";syntax
"VERKAUFE";locale_de
"VERKAUFEN";locale_de

ORDER "LEAVE"
"VERLASSEN";syntax
"VERLASSE";locale_de
"VERLASSEN";locale_de

ORDER "CAST"
"ZAUBERN [REGION 1 2] [STUFE 3] Zauber Parameter1*";syntax
"ZAUBERE";locale_de
"ZAUBERN";locale_de

ORDER "DESTROY"
"ZERSTÖREN ([1] [STRASSE richtung]) | (BURG b1) | (GEBÄUDE b1)";syntax
"ZERSTÖRE";locale_de
"ZERSTÖREN";locale_de

ORDER "PERSISTENT"
"";locale_de
1;internal

ORDER "AFTER"
"HINTER";locale_de
1;internal

ORDER "ALL"
"ALLES";locale_de
1;internal

ORDER "AURA"
"AURA";locale_de
1;internal

ORDER "BEFORE"
"VOR";locale_de
1;internal

ORDER "CASTLE"
"BURG";locale_de
1;internal

ORDER "BUILDING"
"GEBÄUDE";locale_de
1;internal

ORDER "COMBAT_"
"KÄMPFE";locale_de
1;internal

ORDER "COMBAT_AGGRESSIVE"
"AGGRESSIV";locale_de
1;internal

ORDER "COMBAT_DEFENSIVE"
"DEFENSIV";locale_de
1;internal

ORDER "COMBAT_FLEE"
"FLIEHE";locale_de
1;internal

ORDER "COMBAT_FRONT"
"VORNE";locale_de
1;internal

ORDER "COMBAT_REAR"
"HINTEN";locale_de
1;internal

ORDER "COMBAT_NOT"
"NICHT";locale_de
1;internal

ORDER "CONTROL"
"KOMMANDO";locale_de
1;internal

ORDER "LEMURIA"
"LEMURIA";locale_de
1;internal

ORDER "PARAMETER_FACTION"
"PARTEI";locale_de
1;internal

ORDER "HELP_ALL"
"ALLES";locale_de
1;internal

ORDER "HELP_COMBAT"
"KÄMPFE";locale_de
1;internal

ORDER "HELP_FACTIONSTEALTH"
"PARTEITARNUNG";locale_de
1;internal

ORDER "HELP_GIVE"
"GIB";locale_de
1;internal

ORDER "HELP_GUARD"
"BEWACHE";locale_de
"BEWACHEN";locale_de
1;internal

ORDER "HELP_SILVER"
"SILBER";locale_de
1;internal

ORDER "COMBAT_HELP"
"HELFE";locale_de
1;internal

ORDER "HERBS"
"KRÄUTER";locale_de
1;internal

ORDER "HORSES"
"PFERDE";locale_de
1;internal

ORDER "LEVEL"
"STUFE";locale_de
1;internal

ORDER "MEN"
"PERSONEN";locale_de
1;internal

ORDER "NOT"
"NICHT";locale_de
1;internal

ORDER "STEALTH_NUMBER"
"NUMMER";locale_de
1;internal

ORDER "PAUSE"
"PAUSE";locale_de
1;internal

ORDER "ROAD"
"STRAßE";locale_de
1;internal

ORDER "SHIP"
"SCHIFF";locale_de
1;internal

ORDER "TEMP"
"TEMP";locale_de
1;internal

ORDER "UNIT"
"EINHEIT";locale_de
1;internal

ORDER "NORTHWEST"
"NORDWESTEN";locale_de
"NW";locale_de
1;internal

ORDER "NORTHEAST"
"NORDOSTEN";locale_de
1;internal

ORDER "EAST"
"OSTEN";locale_de
1;internal

ORDER "SOUTHEAST"
"SÜDOSTEN";locale_de
"SO";locale_de
1;internal

ORDER "SOUTHWEST"
"SÜDWESTEN";locale_de
"SW";locale_de
1;internal

ORDER "WEST"
"WESTEN";locale_de
1;internal

ORDER "NW"
"NW";locale_de
"NORDWESTEN";locale_de
1;internal

ORDER "NE"
"NO";locale_de
"NORDOSTEN";locale_de
1;internal

ORDER "E"
"O";locale_de
"OSTEN";locale_de
1;internal

ORDER "SE"
"SO";locale_de
"SÜDOSTEN";locale_de
1;internal

ORDER "SW"
"SW";locale_de
"SÜDWESTEN";locale_de
1;internal

ORDER "W"
"W";locale_de
"WESTEN";locale_de
1;internal

ALLIANCECATEGORY "ALLES"
"ALL";name
59;bitmask

ALLIANCECATEGORY "SILBER"
"SILVER";name
"ALLES";parent
1;bitmask

ALLIANCECATEGORY "KÄMPFE"
"COMBAT";name
"ALLES";parent
2;bitmask

ALLIANCECATEGORY "GIB"
"GIVE";name
"ALLES";parent
8;bitmask

ALLIANCECATEGORY "BEWACHEN"
"GUARD";name
"ALLES";parent
16;bitmask

ALLIANCECATEGORY "PARTEITARNUNG"
"FACTIONSTEALTH";name
"ALLES";parent
32;bitmask

ITEMCATEGORY "silver"
"Silber";name
0;naturalorder

ITEMCATEGORY "weapons"
"Waffen";name
1;naturalorder

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
2;naturalorder

ITEMCATEGORY "shield"
"Schilde";name
0;naturalorder
"armour";parent

ITEMCATEGORY "resources"
"Ressourcen";name
3;naturalorder

ITEMCATEGORY "luxuries"
"Luxusgüter";name
4;naturalorder

ITEMCATEGORY "herbs"
"Kräuter";name
"kraeuter";iconname
5;naturalorder

ITEMCATEGORY "potions"
"Tränke";name
6;naturalorder

ITEMCATEGORY "trophies"
"Trophäen";name
7;naturalorder

ITEMCATEGORY "misc"
"Sonstiges";name
8;naturalorder

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

HERB "Alraune"
"Alraune";name
"Hochland";region
"herbs";category
"mandrake";iconname

HERB "Blasenmorchel"
"Blasenmorchel";name
"Sumpf";region
"herbs";category
"bubblemorel";iconname

HERB "Blauer Baumringel"
"Blauer Baumringel";name
"Wald";region
"herbs";category
"cobalt fungus";iconname

HERB "Eisblume"
"Eisblume";name
"Gletscher";region
"herbs";category
"ice begonia";iconname

HERB "Elfenlieb"
"Elfenlieb";name
"Wald";region
"herbs";category
"elvendear";iconname

HERB "Eulenauge"
"Eulenauge";name
"Ebene";region
"herbs";category
"owlsgaze";iconname

HERB "Fjordwuchs"
"Fjordwuchs";name
"Hochland";region
"herbs";category
"fjord fungus";iconname

HERB "Flachwurz"
"Flachwurz";name
"Ebene";region
"herbs";category
"flatroot";iconname

HERB "Grüner Spinnerich"
"Grüner Spinnerich";name
"Wald";region
"herbs";category
"spider ivy";iconname

HERB "Gurgelkraut"
"Gurgelkraut";name
"Sumpf";region
"herbs";category
"bugleweed";iconname

HERB "Höhlenglimm"
"Höhlenglimm";name
"Berge";region
"herbs";category
"cavelichen";iconname

HERB "Kakteenschwitz"
"Kakteenschwitz";name
"Wüste";region
"herbs";category
"peyote";iconname

HERB "Knotiger Saugwurz"
"Knotiger Saugwurz";name
"Sumpf";region
"herbs";category
"knotroot";iconname

HERB "Sandfäule"
"Sandfäule";name
"Wüste";region
"herbs";category
"sandreeker";iconname

HERB "Schneekristall"
"Schneekristall";name
"Gletscher";region
"herbs";category
"snowcrystal petal";iconname

HERB "Spaltwachs"
"Spaltwachs";name
"Berge";region
"herbs";category
"gapgrowth";iconname

HERB "Steinbeißer"
"Steinbeißer";name
"Berge";region
"herbs";category
"rockweed";iconname

HERB "Wasserfinder"
"Wasserfinder";name
"Wüste";region
"herbs";category
"waterfinder";iconname

HERB "Weißer Wüterich"
"Weißer Wüterich";name
"Gletscher";region
"herbs";category
"white hemlock";iconname

HERB "Windbeutel"
"Windbeutel";name
"Hochland";region
"herbs";category
"windbag";iconname

HERB "Würziger Wagemut"
"Würziger Wagemut";name
"Ebene";region
"herbs";category
"tangy temerity";iconname

SKILL "Alchemie"
"Alchemie";name
"build";category
250;cost
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
SKILL "Handeln"
"Handeln";name
"trade";category
SKILL "Hiebwaffen"
"Hiebwaffen";name
"war";category
SKILL "Holzfällen"
"Holzfällen";name
"resource";category
SKILL "Juwelierskunst"
"Juwelierskunst";name
"build";category
SKILL "Katapultbedienung"
"Katapultbedienung";name
"war";category
SKILL "Kräuterkunde"
"Kräuterkunde";name
"resource";category
100;cost
SKILL "Magie"
"Magie";name
"magic";category
COSTS
100;1
250;2
400;3
550;4
700;5
850;6
1000;7
1150;8
1300;9
1450;10
1600;11
1750;12
1900;13
2050;14
2200;15
2350;16
2500;17
2650;18
2800;19
2950;20
3100;21
3250;22
3400;23
3550;24
3700;25
3850;26
4000;27
4150;28
4300;29
4450;30
4600;31
4750;32
4900;33
5050;34
5200;35
5350;36
5500;37
5650;38
5800;39
5950;40
6100;41
6250;42
6400;43
6550;44
6700;45
6850;46
7000;47
7150;48
7300;49
7450;50
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
SKILL "Spionage"
"Spionage";name
"misc";category
150;cost
SKILL "Stangenwaffen"
"Stangenwaffen";name
"war";category
SKILL "Steinbau"
"Steinbau";name
"resource";category
SKILL "Steuereintreiben"
"Steuereintreiben";name
"silver";category
SKILL "Straßenbau"
"Straßenbau";name
"build";category
SKILL "Taktik"
"Taktik";name
"war";category
100;cost
SKILL "Tarnung"
"Tarnung";name
"misc";category
SKILL "Unterhaltung"
"Unterhaltung";name
"silver";category
SKILL "Waffenbau"
"Waffenbau";name
"build";category
SKILL "Wagenbau"
"Wagenbau";name
"build";category
SKILL "Wahrnehmung"
"Wahrnehmung";name
"misc";category

RACE "Zwerge"
"Zwerge";name
110;recruitmentcosts
10;weight
5.0;capacity
TALENTBONI
2;Alchemie
2;Bergbau
-1;Bogenschießen
2;Burgenbau
1;Handeln
1;Hiebwaffen
-1;Holzfällen
2;Katapultbedienung
-2;Kräuterkunde
-2;Magie
-2;Pferdedressur
-2;Reiten
2;Rüstungsbau
-1;Schiffbau
-2;Segeln
2;Steinbau
1;Steuereintreiben
2;Straßenbau
-1;Tarnung
-1;Unterhaltung
2;Waffenbau
TALENTBONI "Berge"
1;Taktik
TALENTBONI "Gletscher"
1;Taktik

RACE "Orks"
"Orks";name
70;recruitmentcosts
10;weight
5.0;capacity
2;recruitmentfactor
TALENTBONI
1;Alchemie
1;Bergbau
1;Burgenbau
-3;Handeln
1;Holzfällen
-2;Kräuterkunde
-1;Magie
-1;Pferdedressur
1;Rüstungsbau
-1;Schiffbau
-1;Segeln
1;Steinbau
1;Steuereintreiben
1;Taktik
-2;Unterhaltung
2;Waffenbau
-1;Wagenbau

RACE "Elfen"
"Elfen";name
130;recruitmentcosts
10;weight
5.0;capacity
TALENTBONI
-1;Alchemie
-2;Bergbau
2;Bogenbau
2;Bogenschießen
-1;Burgenbau
-2;Katapultbedienung
2;Kräuterkunde
1;Magie
1;Pferdedressur
-1;Rüstungsbau
-1;Schiffbau
-1;Segeln
-1;Steinbau
-1;Straßenbau
1;Tarnung
1;Wahrnehmung
TALENTBONI "Wald"
2;Taktik
1;Tarnung
1;Wahrnehmung

RACE "Halblinge"
"Halblinge";name
60;recruitmentcosts
8;weight
5.0;capacity
TALENTBONI
1;Armbrustschießen
1;Bergbau
-1;Bogenschießen
1;Burgenbau
2;Handeln
-1;Hiebwaffen
-1;Katapultbedienung
2;Kräuterkunde
-1;Pferdedressur
-1;Reiten
-1;Schiffbau
-2;Segeln
1;Stangenwaffen
-1;Steuereintreiben
1;Straßenbau
1;Tarnung
1;Unterhaltung
2;Wagenbau
1;Wahrnehmung

RACE "Menschen"
"Menschen";name
75;recruitmentcosts
10;weight
5.0;capacity
TALENTBONI

RACE "Trolle"
"Trolle";name
90;recruitmentcosts
20;weight
10.0;capacity
TALENTBONI
2;Bergbau
-2;Bogenschießen
2;Burgenbau
1;Hiebwaffen
2;Katapultbedienung
-1;Kräuterkunde
-1;Pferdedressur
-2;Reiten
2;Rüstungsbau
-1;Schiffbau
-1;Segeln
2;Steinbau
1;Steuereintreiben
2;Straßenbau
-1;Taktik
-3;Tarnung
-1;Unterhaltung
-1;Wahrnehmung

RACE "Aquaner"
"Aquaner";name
80;recruitmentcosts
10;weight
5.0;capacity
TALENTBONI
-1;Armbrustschießen
-1;Bergbau
2;Handeln
-1;Hiebwaffen
1;Holzfällen
-2;Katapultbedienung
-1;Pferdedressur
-1;Reiten
-2;Rüstungsbau
2;Schiffbau
2;Segeln
1;Stangenwaffen
-2;Straßenbau
-1;Wagenbau
SPECIALS
1;shiprange

RACE "Bauern"
"Bauern";name
10;weight

RACE "Bären"
"Bären";name
0;maintenance
40;weight

RACE "Ents"
"Ents";name
0;maintenance
240;weight

RACE "Ghoule"
"Ghoule";name
0;maintenance
5;weight

RACE "Kobolde"
"Kobolde";name
0;maintenance
6;weight

RACE "Kraken"
"Kraken";name
0;maintenance
320;weight

RACE "Riesenfrösche"
"Riesenfrösche";name
0;maintenance
7;weight

RACE "Sandwürmer"
"Sandwürmer";name
0;maintenance
600;weight

RACE "Skelette"
"Skelette";name
0;maintenance
5;weight

RACE "Wölfe"
"Wölfe";name
0;maintenance
10;weight

RACE "Zombies"
"Zombies";name
0;maintenance
5;weight

ITEM "Silber"
"Silber";name
0.01;weight
"silver";category
1;storeinbonw

ITEM "Öl"
"Öl";name
1;weight
"luxuries";category
1;storeinbonw
RESOURCES
3;Silber

ITEM "Öle"
"Öle";name
1;weight
"luxuries";category
1;storeinbonw
RESOURCES
3;Silber

ITEM "Balsam"
"Balsam";name
1;weight
"luxuries";category
1;storeinbonw
RESOURCES
4;Silber

ITEM "Balsame"
"Balsame";name
1;weight
"luxuries";category
1;storeinbonw
RESOURCES
4;Silber

ITEM "Weihrauch"
"Weihrauch";name
1;weight
"luxuries";category
1;storeinbonw
RESOURCES
5;Silber

ITEM "Weihrauche"
"Weihrauche";name
1;weight
"luxuries";category
1;storeinbonw
RESOURCES
5;Silber

ITEM "Myrrhe"
"Myrrhe";name
1;weight
"luxuries";category
1;storeinbonw
RESOURCES
5;Silber

ITEM "Myrrhen"
"Myrrhen";name
1;weight
"luxuries";category
1;storeinbonw
RESOURCES
5;Silber

ITEM "Gewürz"
"Gewürz";name
1;weight
"luxuries";category
1;storeinbonw
RESOURCES
6;Silber

ITEM "Gewürze"
"Gewürze";name
1;weight
"luxuries";category
1;storeinbonw
RESOURCES
6;Silber

ITEM "Seide"
"Seide";name
1;weight
"luxuries";category
1;storeinbonw
RESOURCES
6;Silber

ITEM "Seiden"
"Seiden";name
1;weight
"luxuries";category
1;storeinbonw
RESOURCES
6;Silber

ITEM "Pelz"
"Pelz";name
1;weight
"luxuries";category
1;storeinbonw
RESOURCES
7;Silber

ITEM "Pelze"
"Pelze";name
1;weight
"luxuries";category
1;storeinbonw
RESOURCES
7;Silber

ITEM "Juwel"
"Juwel";name
1;weight
"luxuries";category
1;storeinbonw
RESOURCES
8;Silber

ITEM "Juwelen"
"Juwelen";name
1;weight
"luxuries";category
1;storeinbonw
RESOURCES
8;Silber

ITEM "Holz"
"Holz";name
5;weight
"Holzfällen";makeskill
"resources";category
1;storeinbonw

ITEM "Stein"
"Stein";name
60;weight
"Steinbau";makeskill
"resources";category
0;storeinbonw

ITEM "Steine"
"Steine";name
60;weight
"Steinbau";makeskill
"resources";category
0;storeinbonw

ITEM "Eisen"
"Eisen";name
5;weight
"Bergbau";makeskill
"resources";category
1;storeinbonw

ITEM "Gold"
"Gold";name
5;weight
"Bergbau";makeskill
"resources";category
0;storeinbonw

ITEM "Goldring"
"Goldring";name
0.1;weight
"Juwelierskunst";makeskill
3;makeskilllevel
"resources";category
1;storeinbonw

ITEM "Pferd"
"Pferd";name
50;weight
"Pferdedressur";makeskill
1;makeskilllevel
"resources";category
0;storeinbonw
1;ishorse

ITEM "Pferde"
"Pferde";name
50;weight
"Pferdedressur";makeskill
1;makeskilllevel
"resources";category
0;storeinbonw
1;ishorse

ITEM "Kamel"
"Kamel";name
50;weight
"Pferdedressur";makeskill
1;makeskilllevel
"resources";category
0;storeinbonw
1;ishorse

ITEM "Kamele"
"Kamele";name
50;weight
"Pferdedressur";makeskill
1;makeskilllevel
"resources";category
0;storeinbonw
1;ishorse

ITEM "Elefant"
"Elefant";name
240;weight
"Pferdedressur";makeskill
1;makeskilllevel
"resources";category
0;storeinbonw
1;ishorse

ITEM "Elefanten"
"Elefanten";name
240;weight
"Pferdedressur";makeskill
1;makeskilllevel
"resources";category
0;storeinbonw
1;ishorse

ITEM "Pegasus"
"Pegasus";name
50;weight
"Pferdedressur";makeskill
1;makeskilllevel
"resources";category
0;storeinbonw
1;ishorse

ITEM "Pegasi"
"Pegasi";name
50;weight
"Pferdedressur";makeskill
1;makeskilllevel
"resources";category
0;storeinbonw
1;ishorse

ITEM "Greif"
"Greif";name
120;weight
"Pferdedressur";makeskill
1;makeskilllevel
"resources";category
0;storeinbonw
1;ishorse

ITEM "Greife"
"Greife";name
120;weight
"Pferdedressur";makeskill
1;makeskilllevel
"resources";category
0;storeinbonw
1;ishorse

ITEM "Wagen"
"Wagen";name
40;weight
"Wagenbau";makeskill
1;makeskilllevel
"resources";category
0;storeinbonw
RESOURCES
5;Holz

ITEM "Wagenwrack"
"Wagenwrack";name
40;weight
"Wagenbau";makeskill
1;makeskilllevel
"resources";category
0;storeinbonw
RESOURCES
1;Holz

ITEM "Wagenwracks"
"Wagenwracks";name
40;weight
"Wagenbau";makeskill
1;makeskilllevel
"resources";category
0;storeinbonw
RESOURCES
1;

ITEM "Speer"
"Speer";name
1;weight
"Waffenbau";makeskill
"Stangenwaffen";useskill
1;makeskilllevel
"weapons";category
1;storeinbonw
RESOURCES
1;Holz

ITEM "Speere"
"Speere";name
1;weight
"Waffenbau";makeskill
"Stangenwaffen";useskill
1;makeskilllevel
"weapons";category
1;storeinbonw
RESOURCES
1;Holz

ITEM "Stumpfer Speer"
"Stumpfer Speer";name
1;weight
"Waffenbau";makeskill
"Stangenwaffen";useskill
1;makeskilllevel
"weapons";category
1;storeinbonw
RESOURCES
1;Stumpfer Speer

ITEM "Stumpfe Speere"
"Stumpfe Speere";name
1;weight
"Waffenbau";makeskill
"Stangenwaffen";useskill
1;makeskilllevel
"weapons";category
1;storeinbonw
RESOURCES
1;Stumpfer Speer

ITEM "Schwert"
"Schwert";name
1;weight
"Waffenbau";makeskill
"Hiebwaffen";useskill
2;makeskilllevel
"weapons";category
1;storeinbonw
RESOURCES
1;Eisen

ITEM "Schwerter"
"Schwerter";name
1;weight
"Waffenbau";makeskill
"Hiebwaffen";useskill
2;makeskilllevel
"weapons";category
1;storeinbonw
RESOURCES
1;Eisen

ITEM "Rostiges Schwert"
"Rostiges Schwert";name
1;weight
"Waffenbau";makeskill
"Hiebwaffen";useskill
1;makeskilllevel
"weapons";category
1;storeinbonw
RESOURCES
1;Rostiges Schwert

ITEM "Rostige Schwerter"
"Rostige Schwerter";name
1;weight
"Waffenbau";makeskill
"Hiebwaffen";useskill
1;makeskilllevel
"weapons";category
1;storeinbonw
RESOURCES
1;Rostiges Schwert

ITEM "Kriegshammer"
"Kriegshammer";name
2;weight
"Waffenbau";makeskill
"Hiebwaffen";useskill
4;makeskilllevel
"weapons";category
1;storeinbonw
RESOURCES
1;Holz
1;Stein
2;Eisen

ITEM "Kriegshämmer"
"Kriegshämmer";name
2;weight
"Waffenbau";makeskill
"Hiebwaffen";useskill
4;makeskilllevel
"weapons";category
1;storeinbonw
RESOURCES
1;Holz
1;Stein
2;Eisen

ITEM "Lockerer Kriegshammer"
"Lockerer Kriegshammer";name
2;weight
"Waffenbau";makeskill
"Hiebwaffen";useskill
1;makeskilllevel
"weapons";category
1;storeinbonw
RESOURCES
1;Lockerer Kriegshammer

ITEM "Lockere Kriegshämmer"
"Lockere Kriegshämmer";name
2;weight
"Waffenbau";makeskill
"Hiebwaffen";useskill
1;makeskilllevel
"weapons";category
1;storeinbonw
RESOURCES
1;Lockerer Kriegshammer

ITEM "Streitaxt"
"Streitaxt";name
2;weight
"Waffenbau";makeskill
"Hiebwaffen";useskill
5;makeskilllevel
"weapons";category
1;storeinbonw
RESOURCES
2;Holz
3;Eisen

ITEM "Streitäxte"
"Streitäxte";name
2;weight
"Waffenbau";makeskill
"Hiebwaffen";useskill
5;makeskilllevel
"weapons";category
1;storeinbonw
RESOURCES
2;Holz
3;Eisen

ITEM "Rostige Streitaxt"
"Rostige Streitaxt";name
2;weight
"Waffenbau";makeskill
"Hiebwaffen";useskill
1;makeskilllevel
"weapons";category
1;storeinbonw
RESOURCES
1;Rostige Streitaxt

ITEM "Rostige Streitäxte"
"Rostige Streitäxte";name
2;weight
"Waffenbau";makeskill
"Hiebwaffen";useskill
1;makeskilllevel
"weapons";category
1;storeinbonw
RESOURCES
1;Rostige Streitaxt

ITEM "Hellebarde"
"Hellebarde";name
2;weight
"Waffenbau";makeskill
"Stangenwaffen";useskill
4;makeskilllevel
"weapons";category
1;storeinbonw
RESOURCES
1;Eisen
1;Holz

ITEM "Hellebarden"
"Hellebarden";name
2;weight
"Waffenbau";makeskill
"Stangenwaffen";useskill
4;makeskilllevel
"weapons";category
1;storeinbonw
RESOURCES
1;Eisen
1;Holz

ITEM "Verbogene Hellebarde"
"Verbogene Hellebarde";name
2;weight
"Stangenwaffen";useskill
"weapons";category
1;storeinbonw

ITEM "Verbogene Hellebarden"
"Verbogene Hellebarden";name
2;weight
"Stangenwaffen";useskill
"weapons";category
1;storeinbonw

ITEM "Zweihänder"
"Zweihänder";name
2;weight
"Waffenbau";makeskill
"Hiebwaffen";useskill
5;makeskilllevel
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

ITEM "Rostige Zweihänder"
"Rostige Zweihänder";name
2;weight
"Hiebwaffen";useskill
"weapons";category
1;storeinbonw

ITEM "Kriegselefant"
"Kriegselefant";name
300;weight
"Waffenbau";makeskill
"Reiten";useskill
5;makeskilllevel
"weapons";category
1;storeinbonw
RESOURCES
2;Holz
1;Elefantenpanzer
1;Elefant

ITEM "Kriegselefanten"
"Kriegselefanten";name
300;weight
"Waffenbau";makeskill
"Reiten";useskill
5;makeskilllevel
"weapons";category
1;storeinbonw
RESOURCES
2;Holz
1;Elefantenpanzer
1;Elefant

ITEM "Lahmender Kriegselefant"
"Lahmender Kriegselefant";name
300;weight
"Waffenbau";makeskill
"Reiten";useskill
1;makeskilllevel
"weapons";category
1;storeinbonw
RESOURCES
1;Elefant

ITEM "Lahmendee Kriegselefanten"
"Lahmende Kriegselefanten";name
300;weight
"Waffenbau";makeskill
"Reiten";useskill
1;makeskilllevel
"weapons";category
1;storeinbonw
RESOURCES
1;Elefant

ITEM "Bogen"
"Bogen";name
1;weight
"Bogenbau";makeskill
"Bogenschießen";useskill
2;makeskilllevel
"distance weapons";category
1;storeinbonw
RESOURCES
1;Holz

ITEM "Bögen"
"Bögen";name
1;weight
"Bogenbau";makeskill
"Bogenschießen";useskill
2;makeskilllevel
"distance weapons";category
1;storeinbonw
RESOURCES
1;Holz

ITEM "Schlaffer Bogen"
"Schlaffer Bogen";name
1;weight
"Bogenbau";makeskill
"Bogenschießen";useskill
1;makeskilllevel
"distance weapons";category
1;storeinbonw
RESOURCES
1;Schlaffer Bogen

ITEM "Schlaffe Bögen"
"Schlaffe Bögen";name
1;weight
"Bogenbau";makeskill
"Bogenschießen";useskill
1;makeskilllevel
"distance weapons";category
1;storeinbonw
RESOURCES
1;Schlaffer Bogen

ITEM "Armbrust"
"Armbrust";name
2;weight
"Bogenbau";makeskill
"Armbrustschießen";useskill
3;makeskilllevel
"distance weapons";category
1;storeinbonw
RESOURCES
1;Holz

ITEM "Armbrüste"
"Armbrüste";name
2;weight
"Bogenbau";makeskill
"Armbrustschießen";useskill
3;makeskilllevel
"distance weapons";category
1;storeinbonw
RESOURCES
1;Holz

ITEM "Schlaffe Armbrust"
"Schlaffe Armbrust";name
2;weight
"Bogenbau";makeskill
"Armbrustschießen";useskill
1;makeskilllevel
"distance weapons";category
1;storeinbonw
RESOURCES
1;Schlaffe Armbrust

ITEM "Schlaffe Armbrüste"
"Schlaffe Armbrüste";name
2;weight
"Bogenbau";makeskill
"Armbrustschießen";useskill
1;makeskilllevel
"distance weapons";category
1;storeinbonw
RESOURCES
1;Schlaffe Armbrust

ITEM "Katapult"
"Katapult";name
100;weight
"Wagenbau";makeskill
"Katapultbedienung";useskill
3;makeskilllevel
"distance weapons";category
0;storeinbonw
RESOURCES
10;Holz

ITEM "Katapulte"
"Katapulte";name
100;weight
"Wagenbau";makeskill
"Katapultbedienung";useskill
3;makeskilllevel
"distance weapons";category
0;storeinbonw
RESOURCES
10;Holz

ITEM "Marodes Katapult"
"Marodes Katapult";name
100;weight
"Wagenbau";makeskill
"Katapultbedienung";useskill
1;makeskilllevel
"distance weapons";category
0;storeinbonw
RESOURCES
1;Marodes Katapult

ITEM "Marode Katapulte"
"Marode Katapulte";name
100;weight
"Wagenbau";makeskill
"Katapultbedienung";useskill
1;makeskilllevel
"distance weapons";category
0;storeinbonw
RESOURCES
1;Marodes Katapult

ITEM "Lederrüstung"
"Lederrüstung";name
1;weight
"Rüstungsbau";makeskill
3;makeskilllevel
"armour";category
1;storeinbonw
RESOURCES
1;Elefant

ITEM "Lederrüstungen"
"Lederrüstungen";name
1;weight
"Rüstungsbau";makeskill
3;makeskilllevel
"armour";category
1;storeinbonw
RESOURCES
1;Elefant

ITEM "Zerrissene Lederrüstung"
"Zerrissene Lederrüstung";name
1;weight
"Rüstungsbau";makeskill
1;makeskilllevel
"armour";category
1;storeinbonw
RESOURCES
1;Zerrissene Lederrüstung

ITEM "Zerrissene Lederrüstungen"
"Zerrissene Lederrüstungen";name
1;weight
"Rüstungsbau";makeskill
1;makeskilllevel
"armour";category
1;storeinbonw
RESOURCES
1;Zerrissene Lederrüstung

ITEM "Kettenhemd"
"Kettenhemd";name
2;weight
"Rüstungsbau";makeskill
3;makeskilllevel
"armour";category
1;storeinbonw
RESOURCES
3;Eisen

ITEM "Kettenhemden"
"Kettenhemden";name
2;weight
"Rüstungsbau";makeskill
3;makeskilllevel
"armour";category
1;storeinbonw
RESOURCES
3;Eisen

ITEM "Rostiges Kettenhemd"
"Rostiges Kettenhemd";name
2;weight
"Rüstungsbau";makeskill
1;makeskilllevel
"armour";category
1;storeinbonw
RESOURCES
1;Rostiges Kettenhemd

ITEM "Rostige Kettenhemden"
"Rostige Kettenhemden";name
2;weight
"Rüstungsbau";makeskill
1;makeskilllevel
"armour";category
1;storeinbonw
RESOURCES
1;Rostiges Kettenhemd

ITEM "Plattenpanzer"
"Plattenpanzer";name
4;weight
"Rüstungsbau";makeskill
4;makeskilllevel
"armour";category
1;storeinbonw
RESOURCES
5;Eisen

ITEM "Verbeulte Rüstung"
"Verbeulte Rüstung";name
4;weight
"Rüstungsbau";makeskill
1;makeskilllevel
"armour";category
1;storeinbonw
RESOURCES
1;Verbeulte Rüstung

ITEM "Verbeulte Rüstungen"
"Verbeulte Rüstungen";name
4;weight
"Rüstungsbau";makeskill
1;makeskilllevel
"armour";category
1;storeinbonw
RESOURCES
1;Verbeulte Rüstung

ITEM "Elefantenpanzer"
"Elefantenpanzer";name
50;weight
"Rüstungsbau";makeskill
5;makeskilllevel
"armour";category
1;storeinbonw
RESOURCES
10;Eisen

ITEM "Holzschild"
"Holzschild";name
2;weight
"Rüstungsbau";makeskill
2;makeskilllevel
"shield";category
1;storeinbonw
RESOURCES
1;Holz

ITEM "Holzschilde"
"Holzschilde";name
2;weight
"Rüstungsbau";makeskill
2;makeskilllevel
"shield";category
1;storeinbonw
RESOURCES
1;Holz

ITEM "Gespaltener Holzschild"
"Gespaltener Holzschild";name
2;weight
"Rüstungsbau";makeskill
1;makeskilllevel
"shield";category
1;storeinbonw
RESOURCES
1;Gespaltener Holzschild

ITEM "Gespaltene Holzschilde"
"Gespaltene Holzschilde";name
2;weight
"Rüstungsbau";makeskill
1;makeskilllevel
"shield";category
1;storeinbonw
RESOURCES
1;Gespaltener Holzschild

ITEM "Eisenschild"
"Eisenschild";name
2;weight
"Rüstungsbau";makeskill
3;makeskilllevel
"shield";category
1;storeinbonw
RESOURCES
1;Eisen

ITEM "Eisenschilde"
"Eisenschilde";name
2;weight
"Rüstungsbau";makeskill
3;makeskilllevel
"shield";category
1;storeinbonw
RESOURCES
1;Eisen

ITEM "Verbeulter Eisenschild"
"Verbeulter Eisenschild";name
2;weight
"Rüstungsbau";makeskill
3;makeskilllevel
"shield";category
1;storeinbonw
RESOURCES
1;Verbeulter Eisenschild

ITEM "Verbeulte Eisenschilde"
"Verbeulte Eisenschilde";name
2;weight
"Rüstungsbau";makeskill
3;makeskilllevel
"shield";category
1;storeinbonw
RESOURCES
1;Verbeulter Eisenschild

ITEM "Kräuter"
"Kräuter";name
0.01;weight
"Kräuterkunde";makeskill
1;makeskilllevel
"resources";category

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
1;Blasenmorchel
1;Fjordwuchs

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
1;Eisblume
1;Sandfäule
1;Spaltwachs

ITEM "Gehirnschmalz"
"Gehirnschmalz";name
"Alchemie";makeskill
4;makeskilllevel
"potions";category
RESOURCES
1;Steinbeißer
1;Wasserfinder
1;Windbeutel

ITEM "Wasser des Lebens"
"Wasser des Lebens";name
"Alchemie";makeskill
"potions";category
4;makeskilllevel
RESOURCES
1;Grüner Spinnerich
1;Knotiger Saugwurz
1;Steinbeißer

ITEM "Wundsalbe"
"Wundsalbe";name
"Alchemie";makeskill
6;makeskilllevel
"potions";category
RESOURCES
1;Höhlenglimm
1;Kakteenschwitz
1;Weißer Wüterich
1;Würziger Wagemut

ITEM "Bauernlieb"
"Bauernlieb";name
"Alchemie";makeskill
6;makeskilllevel
"potions";category
RESOURCES
1;Alraune
1;Blasenmorchel
1;Elfenlieb
1;Schneekristall

ITEM "Pferdeglück"
"Pferdeglück";name
"Alchemie";makeskill
6;makeskilllevel
"potions";category
RESOURCES
1;Blauer Baumringel
1;Kakteenschwitz
1;Knotiger Saugwurz
1;Würziger Wagemut

ITEM "Berserkerblut"
"Berserkerblut";name
"Alchemie";makeskill
8;makeskilllevel
"potions";category
RESOURCES
1;Alraune
1;Eulenauge
1;Gurgelkraut
1;Sandfäule
1;Weißer Wüterich

ITEM "Elixier der Macht"
"Elixier der Macht";name
"Alchemie";makeskill
8;makeskilllevel
"potions";category
RESOURCES
1;Eulenauge
1;Grüner Spinnerich
1;Schneekristall
1;Spaltwachs
1;Wasserfinder

ITEM "Heiltrank"
"Heiltrank";name
"Alchemie";makeskill
8;makeskilllevel
"potions";category
RESOURCES
1;Eisblume
1;Elfenlieb
1;Flachwurz
1;Gurgelkraut
1;Höhlenglimm

ITEM "Kadaver"
"Kadaver";name
"misc";category
10;weight
0;storeinbonw

ITEM "Ring der Unsichtbarkeit"
"Ring der Unsichtbarkeit";name
"misc";category
0.1;weight
0;storeinbonw

ITEM "Schriftrolle"
"Schriftrolle";name
"misc";category
1;weight
0;storeinbonw

ITEM "Zauberbuch"
"Zauberbuch";name
"misc";category
1;weight
0;storeinbonw

ITEM "Goblinohr"
"Goblinohr";name
"trophies";category
0.01;weight
1;storeinbonw

ITEM "Goblinohren"
"Goblinohren";name
"trophies";category
0.01;weight
1;storeinbonw

ITEM "Greifenfeder"
"Greifenfeder";name
"trophies";category
0.01;weight
1;storeinbonw

ITEM "Greifenfedern"
"Greifenfedern";name
"trophies";category
0.01;weight
1;storeinbonw

ITEM "Pegasusfeder"
"Pegasusfeder";name
"trophies";category
0.01;weight
1;storeinbonw

ITEM "Pegasusfedern"
"Pegasusfedern";name
"trophies";category
0.01;weight
1;storeinbonw

ITEM "Totenschädel"
"Totenschädel";name
"trophies";category
0.5;weight
1;storeinbonw

ITEM "Reißzahn"
"Reißzahn";name
"trophies";category
0.01;weight
1;storeinbonw

ITEM "Reißzähne"
"Reißzähne";name
"trophies";category
0.01;weight
1;storeinbonw

ITEM "Wolfsfell"
"Wolfsfell";name
"trophies";category
0.5;weight
1;storeinbonw

ITEM "Wolfsfelle"
"Wolfsfelle";name
"trophies";category
0.5;weight
1;storeinbonw

SHIPTYPE "Boot"
"Boot";name
5;size
1;level
2;range
50;capacity
1;captainlevel
2;sailorlevel

SHIPTYPE "Langboot"
"Langboot";name
50;size
2;level
4;range
500;capacity
2;captainlevel
10;sailorlevel

SHIPTYPE "Drachenschiff"
"Drachenschiff";name
100;size
3;level
5;range
1000;capacity
3;captainlevel
50;sailorlevel

SHIPTYPE "Karavelle"
"Karavelle";name
250;size
4;level
6;range
3000;capacity
4;captainlevel
30;sailorlevel

SHIPTYPE "Trireme"
"Trireme";name
200;size
5;level
8;range
2000;capacity
5;captainlevel
120;sailorlevel

SHIPTYPE "Galeone"
"Galeone";name
350;size
6;level
4;range
6000;capacity
6;captainlevel
80;sailorlevel

CASTLETYPE "Baustelle"
"Baustelle";name
1;level
1;minsize
1;maxsize
11;wage
0;tradetax
RAWMATERIALS
1;Stein

CASTLETYPE "Befestigung"
"Befestigung";name
2;level
2;minsize
9;maxsize
12;wage
0;tradetax
RAWMATERIALS
1;Stein

CASTLETYPE "Turm"
"Turm";name
3;level
10;minsize
49;maxsize
13;wage
0;tradetax
RAWMATERIALS
1;Stein

CASTLETYPE "Schloss"
"Schloss";name
4;level
50;minsize
249;maxsize
14;wage
0;tradetax
RAWMATERIALS
1;Stein

CASTLETYPE "Festung"
"Festung";name
5;level
250;minsize
1249;maxsize
15;wage
0;tradetax
RAWMATERIALS
1;Stein

CASTLETYPE "Zitadelle"
"Zitadelle";name
7;level
1250;minsize
6249;maxsize
16;wage
0;tradetax
RAWMATERIALS
1;Stein

CASTLETYPE "Akropolis"
"Akropolis";name
10;level
6250;minsize
31249;maxsize
17;wage
0;tradetax
RAWMATERIALS
1;Stein

CASTLETYPE "Megapolis"
"Megapolis";name
15;level
31250;minsize
18;wage
0;tradetax
RAWMATERIALS
1;Stein

BUILDINGTYPE "Jagdhaus"
"Jagdhaus";name
3;level
MAINTENANCE
100;Silber
RAWMATERIALS
5;Holz
3;Stein
1;Eisen
50;Silber
REGIONTYPES
"Ebene"
"Wald"

BUILDINGTYPE "Oase"
"Oase";name
3;level
MAINTENANCE
100;Silber
RAWMATERIALS
3;Holz
5;Stein
1;Eisen
50;Silber
REGIONTYPES
"Wüste"

BUILDINGTYPE "Pilzhöhle"
"Pilzhöhle";name
2;level
MAINTENANCE
100;Silber
RAWMATERIALS
1;Holz
2;Stein
1;Eisen
20;Silber
REGIONTYPES
"Berge"
"Gletscher"

BUILDINGTYPE "Plantage"
"Plantage";name
2;level
MAINTENANCE
100;Silber
RAWMATERIALS
2;Holz
1;Stein
1;Eisen
20;Silber
REGIONTYPES
"Sumpf"

BUILDINGTYPE "Holzfällerhütte"
"Holzfällerhütte";name
3;level
RAWMATERIALS
5;Holz
2;Stein
1;Eisen
100;Silber
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

BUILDINGTYPE "Sägewerk"
"Sägewerk";name
5;level
MAINTENANCE
100;Silber
RAWMATERIALS
6;Holz
3;Stein
5;Eisen
250;Silber
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

BUILDINGTYPE "Steingrube"
"Steingrube";name
3;level
RAWMATERIALS
2;Holz
3;Stein
2;Eisen
100;Silber
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

BUILDINGTYPE "Steinbruch"
"Steinbruch";name
5;level
MAINTENANCE
100;Silber
RAWMATERIALS
5;Holz
4;Stein
4;Eisen
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

BUILDINGTYPE "Mine"
"Mine";name
3;level
RAWMATERIALS
3;Holz
3;Stein
1;Eisen
100;Silber
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

BUILDINGTYPE "Bergwerk"
"Bergwerk";name
5;level
MAINTENANCE
100;Silber
RAWMATERIALS
6;Holz
3;Stein
4;Eisen
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

BUILDINGTYPE "Kräuterhütte"
"Kräuterhütte";name
3;level
RAWMATERIALS
5;Holz
2;Stein
1;Eisen
100;Silber
TALENTBONI
1;Kräuterkunde
REGIONTYPES
"Berge"
"Ebene"
"Gletscher"
"Hochland"
"Sumpf"
"Wüste"
"Wald"

BUILDINGTYPE "Werkstatt"
"Werkstatt";name
4;level
MAINTENANCE
100;Silber
TALENTBONI
1;Wagenbau
RAWMATERIALS
6;Holz
2;Stein
2;Eisen
250;Silber
REGIONTYPES
"Berge"
"Ebene"
"Gletscher"
"Hochland"
"Sumpf"
"Wüste"
"Wald"

BUILDINGTYPE "Schmiede"
"Schmiede";name
4;level
MAINTENANCE
100;Silber
TALENTBONI
1;Waffenbau
1;Bogenbau
RAWMATERIALS
2;Holz
6;Stein
4;Eisen
250;Silber
REGIONTYPES
"Berge"
"Ebene"
"Gletscher"
"Hochland"
"Sumpf"
"Wüste"
"Wald"

BUILDINGTYPE "Sattlerei"
"Sattlerei";name
4;level
MAINTENANCE
100;Silber
TALENTBONI
1;Rüstungsbau
RAWMATERIALS
4;Holz
2;Stein
6;Eisen
250;Silber
REGIONTYPES
"Berge"
"Ebene"
"Gletscher"
"Hochland"
"Sumpf"
"Wüste"
"Wald"

BUILDINGTYPE "Schiffswerft"
"Schiffswerft";name
5;level
MAINTENANCE
100;Silber
TALENTBONI
1;Schiffbau
RAWMATERIALS
10;Holz
15;Stein
10;Eisen
500;Silber
REGIONTYPES
"Berge"
"Ebene"
"Gletscher"
"Hochland"
"Sumpf"
"Wüste"
"Wald"

BUILDINGTYPE "Alchemistenküche"
"Alchemistenküche";name
6;level
MAINTENANCE
300;Silber
TALENTBONI
1;Alchemie
RAWMATERIALS
3;Holz
5;Stein
3;Eisen
1;Gold
300;Silber
REGIONTYPES
"Berge"
"Ebene"
"Gletscher"
"Hochland"
"Sumpf"
"Wüste"
"Wald"

BUILDINGTYPE "Pferdezucht"
"Pferdezucht";name
3;level
MAINTENANCE
100;Silber
RAWMATERIALS
5;Holz
3;Stein
2;Eisen
100;Silber
REGIONTYPES
"Berge"
"Ebene"
"Gletscher"
"Hochland"
"Sumpf"
"Wüste"
"Wald"

BUILDINGTYPE "Kamelzucht"
"Kamelzucht";name
3;level
MAINTENANCE
100;Silber
RAWMATERIALS
5;Holz
3;Stein
2;Eisen
100;Silber
REGIONTYPES
"Berge"
"Ebene"
"Gletscher"
"Hochland"
"Sumpf"
"Wüste"
"Wald"

BUILDINGTYPE "Forsthaus"
"Forsthaus";name
3;level
RAWMATERIALS
5;Holz
2;Stein
2;Eisen
50;Silber
REGIONTYPES
"Berge"
"Ebene"
"Gletscher"
"Hochland"
"Sumpf"
"Wüste"
"Wald"

BUILDINGTYPE "Gewächshaus"
"Gewächshaus";name
5;level
MAINTENANCE
100;Silber
RAWMATERIALS
3;Holz
3;Stein
6;Eisen
250;Silber
REGIONTYPES
"Berge"
"Ebene"
"Gletscher"
"Hochland"
"Sumpf"
"Wüste"
"Wald"

BUILDINGTYPE "Markt"
"Markt";name
2;level
MAINTENANCE
100;Silber
RAWMATERIALS
2;Holz
8;Stein
2;Eisen
100;Silber
REGIONTYPES
"Berge"
"Ebene"
"Gletscher"
"Hochland"
"Sumpf"
"Wüste"
"Wald"

BUILDINGTYPE "Taverne"
"Taverne";name
3;level
MAINTENANCE
50;Silber
RAWMATERIALS
3;Holz
4;Stein
1;Eisen
200;Silber
REGIONTYPES
"Berge"
"Ebene"
"Gletscher"
"Hochland"
"Sumpf"
"Wüste"
"Wald"

BUILDINGTYPE "Magierturm"
"Magierturm";name
5;level
MAINTENANCE
500;Silber
RAWMATERIALS
3;Holz
5;Stein
3;Eisen
500;Silber
REGIONTYPES
"Berge"
"Ebene"
"Gletscher"
"Hochland"
"Sumpf"
"Wüste"
"Wald"

BUILDINGTYPE "Steg"
"Steg";name
2;level
MAINTENANCE
30;Silber
RAWMATERIALS
2;Holz
1;Eisen
50;Silber
REGIONTYPES
"Berge"
"Ebene"
"Gletscher"
"Hochland"
"Sumpf"
"Wüste"
"Wald"

BUILDINGTYPE "Hafen"
"Hafen";name
5;level
MAINTENANCE
200;Silber
RAWMATERIALS
5;Holz
10;Stein
4;Eisen
300;Silber
REGIONTYPES
"Berge"
"Ebene"
"Gletscher"
"Hochland"
"Sumpf"
"Wüste"
"Wald"

BUILDINGTYPE "Kanal"
"Kanal";name
3;level
MAINTENANCE
100;Silber
RAWMATERIALS
1;Holz
3;Stein
1;Eisen
250;Silber
REGIONTYPES
"Berge"
"Ebene"
"Gletscher"
"Hochland"
"Sumpf"
"Wüste"
"Wald"

BUILDINGTYPE "Leuchtturm"
"Leuchtturm";name
3;level
MAINTENANCE
100;Silber
RAWMATERIALS
1;Holz
2;Stein
2;Eisen
100;Silber
REGIONTYPES
"Berge"
"Ebene"
"Gletscher"
"Hochland"
"Sumpf"
"Wüste"
"Wald"

BUILDINGTYPE "Wegweiser"
"Wegweiser";name
1;level
RAWMATERIALS
1;Holz
1;Stein
1;Eisen
50;Silber
REGIONTYPES
"Berge"
"Ebene"
"Gletscher"
"Hochland"
"Sumpf"
"Wüste"
"Wald"

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

BUILDINGTYPE "Monument"
"Monument";name
4;level
RAWMATERIALS
1;Stein
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

BUILDINGTYPE "Steinkreis"
"Steinkreis";name
2;level
100;maxsize
RAWMATERIALS
5;Stein
5;Holz
REGIONTYPES
"Berge"
"Ebene"
"Gletscher"
"Hochland"
"Sumpf"
"Wüste"
"Wald"

BUILDINGTYPE "Geschäft"
"Geschäft";name
0;level
0;maxsize
RAWMATERIALS
REGIONTYPES
"Berge"
"Ebene"
"Gletscher"
"Hochland"
"Sumpf"
"Wüste"
"Wald"

BUILDINGTYPE "Höhle"
"Höhle";name
0;level
0;maxsize
RAWMATERIALS
REGIONTYPES
"Berge"
"Ebene"
"Gletscher"
"Hochland"
"Sumpf"
"Wüste"
"Wald"

BUILDINGTYPE "Ruine"
"Ruine";name
0;level
0;maxsize
RAWMATERIALS
REGIONTYPES
"Berge"
"Ebene"
"Gletscher"
"Hochland"
"Sumpf"
"Wüste"
"Wald"

REGIONTYPE "Berge"
"Berge";name
1000;maxworkers
200;roadstones
"true";isAstralVisible
"true";isLand

REGIONTYPE "Ebene"
"Ebene";name
10000;maxworkers
50;roadstones
"true";isAstralVisible
"true";isLand

REGIONTYPE "Gletscher"
"Gletscher";name
100;maxworkers
350;roadstones
"true";isAstralVisible
"true";isLand

REGIONTYPE "Hochland"
"Hochland";name
4000;maxworkers
100;roadstones
"true";isAstralVisible
"true";isLand

REGIONTYPE "Sumpf"
"Sumpf";name
2000;maxworkers
250;roadstones
"true";isAstralVisible
"true";isLand

REGIONTYPE "Wüste"
"Wüste";name
500;maxworkers
150;roadstones
"true";isAstralVisible
"true";isLand

REGIONTYPE "Wald"
"Wald";name
10000;maxworkers
50;roadstones
"true";isAstralVisible
"true";isLand

REGIONTYPE "Ozean"
"Ozean";name
0;maxworkers
"true";isOcean
"false";isAstralVisible

REGIONTYPE "See"
"See";name
0;maxworkers
"true";isOcean
"false";isAstralVisible

FACTION "22"
22;id
"true";isMonster

FACTION "35"
22;id
"true";isMonster
