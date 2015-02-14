VERSION 66
RULES "$Id: e3.cr 1720"

MAGELLAN
"magellan.library.gamebinding.e3a.E3ASpecificStuff";class
"ERESSEA";orderFileStartingString

ORDER "COMMENT"
"// Text1*";syntax
"//";locale_de
"//";locale_en

ORDER "PCOMMENT"
";";locale_de
";";locale_en
1;internal

ORDER "ALLIANCE"
"ALLIANZ (AUSSTOSSEN f1) | (VERLASSEN) | (KOMMANDO f1) | NEU | (EINLADEN f1) | (BEITRETEN a1)";syntax
"ALLIANZ";locale_de
"ALLIANCE";locale_en

ORDER "WORK"
"ARBEITE";syntax
"ARBEITE";locale_de
"ARBEITEN";locale_de
"WORK";locale_en

ORDER "ATTACK"
"ATTACKIERE u1";syntax
"ATTACKIERE";locale_de
"ATTACKIEREN";locale_de
"ATTACK";locale_en

ORDER "BANNER"
"BANNER Text";syntax
"BANNER";locale_de
"BANNER";locale_en

ORDER "CLAIM"
"BEANSPRUCHE [1] Ding";syntax
"BEANSPRUCHE";locale_de
"BEANSPRUCHEN";locale_de
"CLAIM";locale_en

ORDER "PROMOTION"
"BEF�RDERUNG";syntax
"BEF�RDERE";locale_de
"BEF�RDERUNG";locale_de
"PROMOTION";locale_en

ORDER "SIEGE"
"BELAGERE b1";syntax
"BELAGERE";locale_de
"SIEGE";locale_en

ORDER "NAME"
"BENENNE EINHEIT | PARTEI | GEB�UDE | BURG | SCHIFF | REGION | ALLIANZ | (FREMDE EINHEIT u1) | (FREMDES SCHIFF s1) | (FREMDES GEB�UDE b1) | (FREMDE BURG b1) | (FREMDE PARTEI f1) Text";syntax
"BENENNE";locale_de
"BENENNEN";locale_de
"NAME";locale_en

ORDER "USE"
"BENUTZE [1] Ding";syntax
"BENUTZE";locale_de
"BENUTZEN";locale_de
"USE";locale_en

ORDER "DESCRIBE"
"BESCHREIBE REGION | SCHIFF | GEB�UDE | BURG | EINHEIT | PRIVAT  Text";syntax
"BESCHREIBE";locale_de
"BESCHREIBEN";locale_de
"DESCRIBE";locale_en

ORDER "ENTER"
"BETRETE (BURG b1 | SCHIFF s1 | Ding x1)";syntax
"BETRETE";locale_de
"BETRETEN";locale_de
"ENTER";locale_en

ORDER "GUARD"
"BEWACHE [NICHT]";syntax
"BEWACHE";locale_de
"BEWACHEN";locale_de
"GUARD";locale_en

ORDER "PAY"
"BEZAHLE NICHT [b1]";syntax
"BEZAHLE";locale_de
"BEZAHLEN";locale_de
"PAY";locale_en

ORDER "MESSAGE"
"BOTSCHAFT REGION | (SCHIFF s1) | (GEB�UDE b2) | (BURG b3) | (EINHEIT u4) | (PARTEI f5)  Text";syntax
"BOTSCHAFT";locale_de
"MESSAGE";locale_en

ORDER "DEFAULT"
"DEFAULT Order";syntax
"DEFAULT";locale_de
"DEFAULT";locale_en

ORDER "UNIT"
"EINHEIT u1";syntax
"EINHEIT";locale_de
"UNIT";locale_en
1;internal

ORDER "EMAIL"
"EMAIL Email";syntax
"EMAIL";locale_de
"EMAIL";locale_en

ORDER "END"
"ENDE";syntax
"ENDE";locale_de
"END";locale_en

ORDER "RIDE"
"FAHRE u1";syntax
"FAHRE";locale_de
"FAHREN";locale_de
"RIDE";locale_en

ORDER "FOLLOW"
"FOLGE (EINHEIT u1) | (SCHIFF s2)";syntax
"FOLGE";locale_de
"FOLGEN";locale_de
"FOLLOW";locale_en

ORDER "GIVE"
"GIB (u1|0) ( 1 | (JE 2) | ALLES  Ding | PERSONEN ) | ALLES | KR�UTER | KOMMANDO | EINHEIT";syntax
"GIB";locale_de
"GIVE";locale_en

ORDER "GROUP"
"GRUPPE [Name]";syntax
"GRUPPE";locale_de
"GROUP";locale_en

ORDER "HELP"
"HELFE f1 ALLES|GIB|BEWACHE|SILBER [NICHT] ";syntax
"HELFE";locale_de
"HELFEN";locale_de
"HELP";locale_en

ORDER "COMBAT"
"K�MPFE [AGGRESSIV|HINTEN|DEFENSIV|NICHT|FLIEHE|(HELFE [NICHT])]";syntax
"K�MPFE";locale_de
"K�MPFEN";locale_de
"COMBAT";locale_en

ORDER "COMBATSPELL"
"KAMPFZAUBER [STUFE 1] Zauber [NICHT]";syntax
"KAMPFZAUBER";locale_de
"COMBATSPELL";locale_en

ORDER "CONTACT"
"KONTAKTIERE u1";syntax
"KONTAKTIERE";locale_de
"KONTAKTIEREN";locale_de
"CONTACT";locale_en

ORDER "LEARN"
"LERNE Talent [1]";syntax
"LERNE";locale_de
"LERNEN";locale_de
"LEARN";locale_en

ORDER "LOCALE"
"LOCALE";syntax
"LOCALE";locale_de
"LOCALE";locale_en
1;internal

ORDER "MAKE"
"MACHE [(TEMP u1 [Name]) | ([1] Ding [s1|b1]) | ([1] STRA�E Richtung)]";syntax
"MACHE";locale_de
"MACHEN";locale_de
"MAKE";locale_en

ORDER "MOVE"
"NACH Richtung1+";syntax
"NACH";locale_de
"MOVE";locale_en

ORDER "NEXT"
"Invalid";syntax
"N�CHSTER";locale_de
"NEXT";locale_en
1;internal

ORDER "RESTART"
"NEUSTART Rasse Passwort";syntax
"NEUSTART";locale_de
"RESTART";locale_en
0;active

ORDER "NUMBER"
"NUMMER (EINHEIT) | (SCHIFF) | (GEB�UDE) | (BURG) | (PARTEI) [x1]";syntax
"NUMMER";locale_de
"NUMBER";locale_en

ORDER "OPTION"
"OPTION Option [Nicht]";syntax
"OPTION";locale_de
"OPTION";locale_en

ORDER "FACTION"
"PARTEI f1";syntax
"PARTEI";locale_de
"FACTION";locale_en
1;internal

ORDER "PASSWORD"
"PASSWORT passwort";syntax
"PASSWORT";locale_de
"PASSWORD";locale_en

ORDER "PLANT"
"PFLANZE [1] (KR�UTER) | (B�UME) | (SAMEN) | (MALLORNSAMEN)";syntax
"PFLANZE";locale_de
"PFLANZEN";locale_de
"PLANT";locale_en

ORDER "PIRACY"
"PIRATERIE f1*";syntax
"PIRATERIE";locale_de
"PIRACY";locale_en

ORDER "PREFIX"
"PR�FIX [Name]";syntax
"PR�FIX";locale_de
"PREFIX";locale_en

ORDER "REGION"
"REGION 1 , 2";syntax
"REGION";locale_de
"REGION";locale_en
1;internal

ORDER "RECRUIT"
"REKRUTIERE 1 [Race]";syntax
"REKRUTIERE";locale_de
"REKRUTIEREN";locale_de
"RECRUIT";locale_en

ORDER "RESERVE"
"RESERVIERE (1) | (ALLES) | (JE 2) Gegenstand";syntax
"RESERVIERE";locale_de
"RESERVIEREN";locale_de
"RESERVE";locale_en

ORDER "ROUTE"
"ROUTE Richtung1+";syntax
"ROUTE";locale_de
"ROUTE";locale_en

ORDER "SORT"
"SORTIERE (VOR) | (HINTER) u1";syntax
"SORTIERE";locale_de
"SORTIEREN";locale_de
"SORT";locale_en

ORDER "QUIT"
"STIRB Password";syntax
"STIRB";locale_de
"QUIT";locale_en

ORDER "HIDE"
"TARNE ([1]) | (PARTEI [NICHT])";syntax
"TARNE";locale_de
"TARNEN";locale_de
"HIDE";locale_en

ORDER "CARRY"
"TRANSPORTIERE u1";syntax
"TRANSPORTIERE";locale_de
"TRANSPORTIEREN";locale_de
"CARRY";locale_en

ORDER "ORIGIN"
"URSPRUNG 1 2";syntax
"URSPRUNG";locale_de
"ORIGIN";locale_en

ORDER "FORGET"
"VERGESSE Talent";syntax
"VERGISS";locale_de
"VERGESSEN";locale_de
"FORGET";locale_en

ORDER "LEAVE"
"VERLASSE";syntax
"VERLASSE";locale_de
"VERLASSEN";locale_de
"LEAVE";locale_en

ORDER "CAST"
"ZAUBERE [REGION 1 2] [STUFE 3] Zauber Parameter1*";syntax
"ZAUBERE";locale_de
"CAST";locale_en

ORDER "SHOW"
"ZEIGE (ding)|(ALLE [ZAUBER|TR�NKE])";syntax
"ZEIGE";locale_de
"ZEIGEN";locale_de
"SHOW";locale_en

ORDER "DESTROY"
"ZERST�RE [1] [STRASSE richtung]";syntax
"ZERST�RE";locale_de
"ZERST�REN";locale_de
"DESTROY";locale_en

ORDER "GROW"
"(Z�CHTE PFERDE)|(Z�CHTE [1] KR�UTER)";syntax
"Z�CHTE";locale_de
"Z�CHTEN";locale_de
"GROW";locale_en

ORDER "PERSISTENT"
"";locale_de
"@";locale_en
1;internal

ORDER "ADDRESSES"
"ADRESSEN";locale_de
"ADDRESSES";locale_en
1;internal
0;active

ORDER "AFTER"
"HINTER";locale_de
"AFTER";locale_en
1;internal

ORDER "ALL"
"ALLES";locale_de
"ALL";locale_en
1;internal

ORDER "AURA"
"AURA";locale_de
"AURA";locale_en
1;internal

ORDER "BEFORE"
"VOR";locale_de
"BEFORE";locale_en
1;internal

ORDER "CASTLE"
"BURG";locale_de
"CASTLE";locale_en
1;internal

ORDER "BUILDING"
"GEB�UDE";locale_de
"BUILDING";locale_en
1;internal

ORDER "WATCH"
"WACHE";locale_de
"WATCH";locale_en
1;internal

ORDER "COMBAT_"
"K�MPFE";locale_de
"COMBAT";locale_en
1;internal

ORDER "COMBAT_AGGRESSIVE"
"AGGRESSIV";locale_de
"AGGRESSIVE";locale_en
1;internal

ORDER "COMBAT_DEFENSIVE"
"DEFENSIV";locale_de
"DEFENSIVE";locale_en
1;internal

ORDER "COMBAT_FLEE"
"FLIEHE";locale_de
"FLEE";locale_en
1;internal

ORDER "COMBAT_FRONT"
"VORNE";locale_de
"FRONT";locale_en
1;internal

ORDER "COMBAT_REAR"
"HINTEN";locale_de
"REAR";locale_en
1;internal

ORDER "COMBAT_NOT"
"NICHT";locale_de
"NOT";locale_en
1;internal

ORDER "CONTROL"
"KOMMANDO";locale_de
"CONTROL";locale_en
1;internal

ORDER "EACH"
"JE";locale_de
"EACH";locale_en
1;internal

ORDER "ERESSEA"
"ERESSEA";locale_de
"ERESSEA";locale_en
1;internal

ORDER "PARAMETER_FACTION"
"PARTEI";locale_de
"FACTION";locale_en
1;internal

ORDER "FOREIGNUNIT"
"FREMDE";locale_de
"FOREIGNUNIT";locale_en
1;internal

ORDER "FOREIGNFACTION"
"FREMDE";locale_de
"FOREIGN";locale_en
1;internal

ORDER "FOREIGNBUILDING"
"FREMDE";locale_de
"FOREIGN";locale_en
1;internal

ORDER "FOREIGNSHIP"
"FREMDES";locale_de
"FOREIGN";locale_en
1;internal

ORDER "HELP_ALL"
"ALLES";locale_de
"ALL";locale_en
1;internal

ORDER "HELP_COMBAT"
"K�MPFE";locale_de
"COMBAT";locale_en
1;internal

ORDER "HELP_FACTIONSTEALTH"
"PARTEITARNUNG";locale_de
"FACTIONSTEALTH";locale_en
1;internal

ORDER "HELP_GIVE"
"GIB";locale_de
"GIVE";locale_en
1;internal

ORDER "HELP_GUARD"
"BEWACHE";locale_de
"BEWACHEN";locale_de
"GUARD";locale_en
1;internal

ORDER "HELP_SILVER"
"SILBER";locale_de
"SILVER";locale_en
1;internal

ORDER "COMBAT_HELP"
"HELFE";locale_de
"HELP";locale_en
1;internal

ORDER "HERBS"
"KR�UTER";locale_de
"HERBS";locale_en
1;internal

ORDER "HORSES"
"PFERDE";locale_de
"HORSES";locale_en
1;internal

ORDER "LEVEL"
"STUFE";locale_de
"LEVEL";locale_en
1;internal

ORDER "MALLORNSEED"
"MALLORNSAMEN";locale_de
"MALLORNSEED";locale_en
1;internal

ORDER "MEN"
"PERSONEN";locale_de
"MEN";locale_en
1;internal

ORDER "NOT"
"NICHT";locale_de
"NOT";locale_en
1;internal

ORDER "STEALTH_NUMBER"
"NUMMER";locale_de
"NUMBER";locale_en
1;internal

ORDER "PAUSE"
"PAUSE";locale_de
"PAUSE";locale_en
1;internal

ORDER "POTIONS"
"TR�NKE";locale_de
"POTIONS";locale_en
1;internal

ORDER "PRIVATE"
"PRIVAT";locale_de
"PRIVATE";locale_en
1;internal

ORDER "ROAD"
"STRA�E";locale_de
"ROAD";locale_en
1;internal

ORDER "SEED"
"SAMEN";locale_de
"SEED";locale_en
1;internal

ORDER "SHIP"
"SCHIFF";locale_de
"SHIP";locale_en
1;internal

ORDER "SPELLS"
"ZAUBER";locale_de
"SPELLS";locale_en
1;internal

ORDER "TEMP"
"TEMP";locale_de
"TEMP";locale_en
1;internal

ORDER "TREES"
"B�UME";locale_de
"TREES";locale_en
1;internal

ORDER "UNIT"
"EINHEIT";locale_de
"UNIT";locale_en
1;internal


ORDER "BZIP2"
"BZIP2";locale_de
"BZIP2";locale_en
1;internal

ORDER "COMPUTER"
"COMPUTER";locale_de
"COMPUTER";locale_en
1;internal

ORDER "PLAINTEXT"
"AUSWERTUNG";locale_de
"PLAINTEXT";locale_en
1;internal

ORDER "REPORT"
"AUSWERTUNG";locale_de
"PLAINTEXT";locale_en
1;internal

ORDER "SCORE"
"PUNKTE";locale_de
"SCORE";locale_en
1;internal

ORDER "SILVERPOOL"
"SILBERPOOL";locale_de
"SILVERPOOL";locale_en
1;internal

ORDER "STATISTICS"
"STATISTIK";locale_de
"STATISTICS";locale_en
1;internal

ORDER "TEMPLATE"
"ZUGVORLAGE";locale_de
"TEMPLATE";locale_en
1;internal

ORDER "ZIPPED"
"ZIPPED";locale_de
"ZIPPED";locale_en
1;internal

ORDER "NORTHWEST"
"NORDWESTEN";locale_de
"NORTHWEST";locale_en
"NW";locale_de
"NW";locale_en
1;internal

ORDER "NORTHEAST"
"NORDOSTEN";locale_de
"NORTHEAST";locale_en
1;internal

ORDER "EAST"
"OSTEN";locale_de
"EAST";locale_en
1;internal

ORDER "SOUTHEAST"
"S�DOSTEN";locale_de
"SOUTHEAST";locale_en
"SO";locale_de
"SE";locale_en
1;internal

ORDER "SOUTHWEST"
"S�DWESTEN";locale_de
"SOUTHWEST";locale_en
"SW";locale_de
"SW";locale_en
1;internal

ORDER "WEST"
"WESTEN";locale_de
"WEST";locale_en
1;internal

ORDER "NW"
"NW";locale_de
"NW";locale_en
"NORDWESTEN";locale_de
"NORTHWEST";locale_en
1;internal

ORDER "NE"
"NO";locale_de
"NE";locale_en
"NORDOSTEN";locale_de
"NORTHEAST";locale_en
1;internal

ORDER "E"
"O";locale_de
"E";locale_en
"OSTEN";locale_de
"EAST";locale_en
1;internal

ORDER "SE"
"SO";locale_de
"SE";locale_en
"S�DOSTEN";locale_de
"SOUTHEAST";locale_en
1;internal

ORDER "SW"
"SW";locale_de
"SW";locale_en
"S�DWESTEN";locale_de
"SOUTHWEST";locale_en
1;internal

ORDER "W"
"W";locale_de
"W";locale_en
"WESTEN";locale_de
"WEST";locale_en
1;internal

ORDER "ITEMPOOL"
"MATERIALPOOL";locale_de
"ITEMPOOL";locale_en
1;internal

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

ALLIANCECATEGORY "K�MPFE"
"COMBAT";name
2;bitmask

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
"R�stungen";name
2;naturalorder

ITEMCATEGORY "shield"
"Schilde";name
0;naturalorder
"armour";parent

ITEMCATEGORY "resources"
"Ressourcen";name
3;naturalorder

ITEMCATEGORY "luxuries"
"Luxusg�ter";name
4;naturalorder

ITEMCATEGORY "herbs"
"Kr�uter";name
"kraeuter";iconname
5;naturalorder

ITEMCATEGORY "potions"
"Tr�nke";name
6;naturalorder

ITEMCATEGORY "trophies"
"Troph�en";name
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

HERB "Flachwurz"
"Flachwurz";name
"Ebene";region
"herbs";category
"flatroot";iconname

HERB "W�rziger Wagemut"
"W�rziger Wagemut";name
"Ebene";region
"herbs";category
"tangy temerity";iconname

HERB "Eulenauge"
"Eulenauge";name
"Ebene";region
"herbs";category
"owlsgaze";iconname

HERB "Gr�ner Spinnerich"
"Gr�ner Spinnerich";name
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
"W�ste";region
"herbs";category
"waterfinder";iconname

HERB "Kakteenschwitz"
"Kakteenschwitz";name
"W�ste";region
"herbs";category
"peyote";iconname

HERB "Sandf�ule"
"Sandf�ule";name
"W�ste";region
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

HERB "Steinbei�er"
"Steinbei�er";name
"Berge";region
"herbs";category
"rockweed";iconname

HERB "Spaltwachs"
"Spaltwachs";name
"Berge";region
"herbs";category
"gapgrowth";iconname

HERB "H�hlenglimm"
"H�hlenglimm";name
"Berge";region
"herbs";category
"cavelichen";iconname

HERB "Eisblume"
"Eisblume";name
"Gletscher";region
"herbs";category
"ice begonia";iconname

HERB "Wei�er W�terich"
"Wei�er W�terich";name
"Gletscher";region
"herbs";category
"white hemlock";iconname

HERB "Schneekristall"
"Schneekristall";name
"Gletscher";region
"herbs";category
"snowcrystal petal";iconname



SKILL "Armbrustschie�en"
"Armbrustschie�en";name
"war";category
SKILL "Ausdauer"
"Ausdauer";name
"misc";category
SKILL "Bergbau"
"Bergbau";name
"resource";category
SKILL "Bogenschie�en"
"Bogenschie�en";name
"war";category
SKILL "Burgenbau"
"Burgenbau";name
"build";category
SKILL "Hiebwaffen"
"Hiebwaffen";name
"war";category
SKILL "Holzf�llen"
"Holzf�llen";name
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
SKILL "R�stungsbau"
"R�stungsbau";name
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
SKILL "Stra�enbau"
"Stra�enbau";name
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
10;weight
5.4;capacity
TALENTBONI
2;Bergbau
-1;Bogenschie�en
2;Burgenbau
1;Hiebwaffen
-1;Holzf�llen
2;Katapultbedienung
0;Magie
-2;Pferdedressur
-2;Reiten
2;R�stungsbau
-1;Schiffbau
-2;Segeln
2;Steinbau
2;Stra�enbau
2;Waffenbau
TALENTBONI "Berge"
1;Taktik
TALENTBONI "Gletscher"
1;Taktik

RACE "Orks"
"Orks";name
100;recruitmentcosts
10;weight
5.4;capacity
TALENTBONI
1;Bergbau
1;Burgenbau
1;Holzf�llen
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
10;weight
5.4;capacity
TALENTBONI
-2;Bergbau
2;Bogenschie�en
-1;Burgenbau
-2;Katapultbedienung
0;Magie
2;Pferdedressur
-1;R�stungsbau
-1;Schiffbau
-1;Segeln
-1;Steinbau
-1;Stra�enbau
TALENTBONI "Wald"
2;Taktik

RACE "D�monen"
"D�monen";name
360;recruitmentcosts
10;weight
5.4;capacity
TALENTBONI
1;Hiebwaffen
1;Holzf�llen
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
10;weight
5.4;capacity
TALENTBONI
2;Armbrustschie�en
1;Bergbau
-1;Bogenschie�en
1;Burgenbau
-1;Hiebwaffen
-1;Katapultbedienung
-1;Pferdedressur
-1;Reiten
-1;Schiffbau
-2;Segeln
-1;Stangenwaffen
1;Stra�enbau
2;Wagenbau

RACE "Menschen"
"Menschen";name
100;recruitmentcosts
10;weight
5.4;capacity
TALENTBONI
2;Reiten
2;Schiffbau
2;Segeln

RACE "Goblins"
"Goblins";name
60;recruitmentcosts
2;recruitmentfactor
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
-2;Stra�enbau
-2;Taktik
-1;Wagenbau
-1;Waffenbau

RACE "Trolle"
"Trolle";name
260;recruitmentcosts
20;weight
10.8;capacity
TALENTBONI
1;Bergbau
-2;Bogenschie�en
2;Burgenbau
1;Hiebwaffen
2;Katapultbedienung
-1;Pferdedressur
0;Reiten
2;R�stungsbau
-1;Schiffbau
-1;Segeln
2;Steinbau
2;Stra�enbau
-1;Taktik

RACE "Drachen"
"Drachen";name

RACE "Dracoide"
"Dracoide";name

RACE "Jungdrachen"
"Jungdrachen";name

RACE "Schattend�monen"
"Schattend�monen";name
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

RACE "Schneem�nner"
"Schneem�nner";name
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

RACE "Kr�ten"
"Kr�ten";name
1;weight

RACE "Adler"
"Adler";name
0;maintenance
5;weight

RACE "Delphine"
"Delphine";name
0;maintenance
5;weight

RACE "Einh�rner"
"Einh�rner";name
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

RACE "Riesenschildkr�ten"
"Riesenschildkr�ten";name
0;maintenance
16;weight

RACE "Teufelchen"
"Teufelchen";name
5;weight
2;capacity

RACE "Tunnelw�rmer"
"Tunnelw�rmer";name
0;maintenance
300;weight

RACE "W�lfe"
"W�lfe";name
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

ITEM "Gew�rz"
"Gew�rz";name
2;weight
"luxuries";category
1;storeinbonw

ITEM "Myrrhe"
"Myrrhe";name
2;weight
"luxuries";category
1;storeinbonw

ITEM "�l"
"�l";name
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
"Holzf�llen";makeskill
"resources";category

ITEM "B�ume"
"B�ume";name
5;weight
"Holzf�llen";makeskill
"resources";category

ITEM "Sch��linge"
"Sch��linge";name
5;weight
"Holzf�llen";makeskill
"resources";category

ITEM "Mallorn"
"Mallorn";name
5;weight
"Holzf�llen";makeskill
"resources";category

ITEM "Mallornsch��linge"
"Mallornsch��linge";name
5;weight
"Holzf�llen";makeskill
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
1;ishorse

ITEM "Pferde"
"Pferde";name
50;weight
"Pferdedressur";makeskill
1;makeskilllevel
"resources";category
1;ishorse

ITEM "Streitross"
"Streitross";name
50;weight
"Pferdedressur";makeskill
4;makeskilllevel
"resources";category
1;ishorse
RESOURCES
1;Pferd
1;Eisen
200;Silber

ITEM "Same"
"Same";name
0.1;weight
"Kr�uterkunde";makeskill
3;makeskilllevel
"resources";category

ITEM "Mallornsame"
"Mallornsame";name
0.1;weight
"Kr�uterkunde";makeskill
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

ITEM "Bih�nder"
"Bih�nder";name
2;weight
"Waffenbau";makeskill
"Hiebwaffen";useskill
4;makeskilllevel
"weapons";category
1;storeinbonw
RESOURCES
2;Eisen

ITEM "Rostiger Zweih�nder"
"Rostiger Zweih�nder";name
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
"Armbrustschie�en";useskill
3;makeskilllevel
"distance weapons";category
1;storeinbonw
RESOURCES
1;Holz

ITEM "Repetierarmbrust"
"Repetierarmbrust";name
1;weight
"Waffenbau";makeskill
"Armbrustschie�en";useskill
5;makeskilllevel
"distance weapons";category
1;storeinbonw
RESOURCES
1;Holz
1;Eisen

ITEM "Bogen"
"Bogen";name
1;weight
"Waffenbau";makeskill
"Bogenschie�en";useskill
2;makeskilllevel
"distance weapons";category
1;storeinbonw
RESOURCES
1;Holz

ITEM "Elfenbogen"
"Elfenbogen";name
1;weight
"Waffenbau";makeskill
"Bogenschie�en";useskill
5;makeskilllevel
"distance weapons";category
1;storeinbonw
RESOURCES
2;Mallorn

ITEM "Mallornbogen"
"Mallornbogen";name
1;weight
"Waffenbau";makeskill
"Bogenschie�en";useskill
5;makeskilllevel
"distance weapons";category
1;storeinbonw
RESOURCES
1;Mallorn

ITEM "Mallornarmbrust"
"Mallornarmbrust";name
1;weight
"Waffenbau";makeskill
"Armbrustschie�en";useskill
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
"R�stungsbau";makeskill
3;makeskilllevel
"armour";category
1;storeinbonw
RESOURCES
2;Eisen

ITEM "Rostiges Kettenhemd"
"Rostiges Kettenhemd";name
"armour";category
2;weight
1;storeinbonw

ITEM "Laenkettenhemd"
"Laenkettenhemd";name
1;weight
"R�stungsbau";makeskill
9;makeskilllevel
"armour";category
1;storeinbonw
RESOURCES
2;Laen

ITEM "Plattenpanzer"
"Plattenpanzer";name
4;weight
"R�stungsbau";makeskill
4;makeskilllevel
"armour";category
1;storeinbonw
RESOURCES
4;Eisen

ITEM "Schuppenpanzer"
"Schuppenpanzer";name
3;weight
"R�stungsbau";makeskill
5;makeskilllevel
"armour";category
1;storeinbonw
RESOURCES
2;Eisen

ITEM "Adamantiumr�stung"
"Adamantiumr�stung";name
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
"R�stungsbau";makeskill
2;makeskilllevel
"shield";category
1;storeinbonw
RESOURCES
1;Eisen

ITEM "Turmschild"
"Turmschild";name
2;weight
"R�stungsbau";makeskill
4;makeskilllevel
"shield";category
1;storeinbonw
RESOURCES
1;Eisen

ITEM "Rostiger Schild"
"Rostiger Schild";name
"shield";category
1;weight
1;storeinbonw

ITEM "Laenschild"
"Laenschild";name
"R�stungsbau";makeskill
7;makeskilllevel
"shield";category
0;weight
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

ITEM "Goliathwasser"
"Goliathwasser";name
"Alchemie";makeskill
"potions";category
2;makeskilllevel


ITEM "Wasser des Lebens"
"Wasser des Lebens";name
"Alchemie";makeskill
"potions";category
2;makeskilllevel


ITEM "Trank der Wahrheit"
"Trank der Wahrheit";name
"Alchemie";makeskill
"potions";category
2;makeskilllevel


ITEM "Schaffenstrunk"
"Schaffenstrunk";name
"Alchemie";makeskill
4;makeskilllevel
"potions";category


ITEM "Wundsalbe"
"Wundsalbe";name
"Alchemie";makeskill
4;makeskilllevel
"potions";category

ITEM "Bauernblut"
"Bauernblut";name
"Alchemie";makeskill
4;makeskilllevel
"potions";category

ITEM "Gehirnschmalz"
"Gehirnschmalz";name
"Alchemie";makeskill
6;makeskilllevel
"potions";category

ITEM "Dumpfbackenbrot"
"Dumpfbackenbrot";name
"Alchemie";makeskill
6;makeskilllevel
"potions";category

ITEM "Nestw�rme"
"Nestw�rme";name
"Alchemie";makeskill
6;makeskilllevel
"potions";category

ITEM "Pferdegl�ck"
"Pferdegl�ck";name
"Alchemie";makeskill
6;makeskilllevel
"potions";category

ITEM "Berserkerblut"
"Berserkerblut";name
"Alchemie";makeskill
6;makeskilllevel
"potions";category

ITEM "Bauernlieb"
"Bauernlieb";name
"Alchemie";makeskill
8;makeskilllevel
"potions";category

ITEM "Elixier der Macht"
"Elixier der Macht";name
"Alchemie";makeskill
8;makeskilllevel
"potions";category

ITEM "Heiltrank"
"Heiltrank";name
"Alchemie";makeskill
8;makeskilllevel
"potions";category

ITEM "Phiole"
"Phiole";name
"potions";category
"traenke";iconname

ITEM "Kraeuterbeutel"
"Kr�uterbeutel";name
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

ITEM "Halblingfu�"
"Halblingfu�";name
"trophies";category
0.01;weight
1;storeinbonw

ITEM "Elfenohr"
"Elfenohr";name
"trophies";category
0.01;weight
1;storeinbonw

ITEM "D�monenblut"
"D�monenblut";name
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

ITEM "Sph�re der Unsichtbarkeit"
"Sph�re der Unsichtbarkeit";name
"misc";category
1;weight
1;storeinbonw

ITEM "Tiegel mit Kr�tenschleim"
"Tiegel mit Kr�tenschleim";name
"misc";category
1;weight
1;storeinbonw

ITEM "Magischer Kr�uterbeutel"
"Magischer Kr�uterbeutel";name
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

SHIPTYPE "Flo�"
"Flo�";name
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
11;persons
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

SHIPTYPE "K�nigsbarke"
"K�nigsbarke";name
10;size
5;level
5;range
50;capacity
11;persons
5;captainlevel
10;sailorlevel
RAWMATERIALS
1;Mallorn
100;Silber

SHIPTYPE "Katamaran"
"Katamaran";name
30;size
7;level
7;range
100;capacity
22;persons
7;captainlevel
20;sailorlevel
RAWMATERIALS
1;Mallorn
100;Silber

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
11;wage
6;tradetax
RAWMATERIALS
1;Stein

CASTLETYPE "Turm"
"Turm";name
3;level
50;minsize
249;maxsize
12;wage
12;tradetax
RAWMATERIALS
1;Stein

CASTLETYPE "Burg"
"Burg";name
4;level
250;minsize
1249;maxsize
13;wage
18;tradetax
RAWMATERIALS
1;Stein

CASTLETYPE "Festung"
"Festung";name
5;level
1250;minsize
6249;maxsize
14;wage
24;tradetax
RAWMATERIALS
1;Stein

CASTLETYPE "Zitadelle"
"Zitadelle";name
6;level
6250;minsize
15;wage
30;tradetax
RAWMATERIALS
1;Stein

CASTLETYPE "Ger�st"
"Ger�st";name
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
"W�ste"
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
"W�ste"
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
"W�ste"
"Wald"
"Vulkan"
"Aktiver Vulkan"

BUILDINGTYPE "Leuchtturm"
"Leuchtturm";name
3;level
1;maintainedbyregionowner
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
"W�ste"
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
"W�ste"
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
"W�ste"
"Wald"
"Vulkan"
"Aktiver Vulkan"

BUILDINGTYPE "S�gewerk"
"S�gewerk";name
3;level
MAINTENANCE
250;Silber
RAWMATERIALS
5;Stein
5;Holz
3;Eisen
200;Silber
TALENTBONI
1;Holzf�llen
REGIONTYPES
"Berge"
"Ebene"
"Gletscher"
"Hochland"
"Sumpf"
"W�ste"
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
1;R�stungsbau
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
"W�ste"
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
"W�ste"
"Wald"
"Vulkan"
"Aktiver Vulkan"

BUILDINGTYPE "Hafen"
"Hafen";name
3;level
25;maxsize
1;maintainedbyregionowner
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
"W�ste"
"Wald"
"Vulkan"
"Aktiver Vulkan"

BUILDINGTYPE "Taverne"
"Taverne";name
2;level
MAINTENANCE
5;Silber pro Gr��enpunkt
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
"W�ste"
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
"W�ste"
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
"W�ste"
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
"W�ste"

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
"W�ste"
"Wald"
"Vulkan"
"Aktiver Vulkan"

BUILDINGTYPE "Marktplatz"
"Marktplatz";name
3;level
10;maxsize
1;maintainedbyregionowner
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
"W�ste"
"Wald"
"Vulkan"
"Aktiver Vulkan"

REGIONTYPE "Berge"
"Berge";name
600;maxworkers
250;roadstones
"true";isAstralVisible
"true";isLand

REGIONTYPE "Ebene"
"Ebene";name
4000;maxworkers
50;roadstones
"true";isAstralVisible
"true";isLand

REGIONTYPE "Gletscher"
"Gletscher";name
150;maxworkers
250;roadstones
"Tunnel";roadsupportbuilding
"true";isAstralVisible
"true";isLand

REGIONTYPE "Eisberg"
"Eisberg";name
0;maxworkers
"false";isAstralVisible

REGIONTYPE "Hochland"
"Hochland";name
2300;maxworkers
100;roadstones
"true";isAstralVisible
"true";isLand

REGIONTYPE "Sumpf"
"Sumpf";name
1200;maxworkers
75;roadstones
"Damm";roadsupportbuilding
"true";isAstralVisible
"true";isLand

REGIONTYPE "W�ste"
"W�ste";name
400;maxworkers
100;roadstones
"Karawanserei";roadsupportbuilding
"true";isAstralVisible
"true";isLand

REGIONTYPE "Wald"
"Wald";name
4000;maxworkers
50;roadstones
"true";isAstralVisible
"true";isLand

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
400;maxworkers
250;roadstones
"true";isAstralVisible
"true";isLand

REGIONTYPE "Aktiver Vulkan"
"Aktiver Vulkan";name
400;maxworkers
250;roadstones
"true";isAstralVisible
"true";isLand
