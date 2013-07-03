VERSION 777
RULES "Allanon Regeln $Id: "

MAGELLAN
"magellan.library.gamebinding.AllanonSpecificStuff";class

ORDER "COMMENT"
"// Text1*";syntax
"//";locale_de

ORDER "PCOMMENT"
";";locale_de
";";locale_en
1;internal

ORDER "EMAIL"
"ADRESSE Email";syntax
"ADRESSE";locale_de

ORDER "WORK"
"ARBEITE";syntax
"ARBEITE";locale_de

ORDER "ATTACK"
"ATTACKIERE u1";syntax
"ATTACKIERE";locale_de

ORDER "CLAIM"
"BEANSPRUCHE [1] Ding";syntax
"BEANSPRUCHE";locale_de

ORDER "STEAL"
"BEKLAUE u1";syntax
"BEKLAUE";locale_de

ORDER "SIEGE"
"BELAGERE b1";syntax
"BELAGERE";locale_de

ORDER "NAME"
"BENENNE EINHEIT | PARTEI | GEBÄUDE | BURG | SCHIFF | REGION | (FREMDE EINHEIT u1) | (FREMDES SCHIFF s1) | (FREMDES GEBÄUDE b1) | (FREMDE BURG b1) | (FREMDE PARTEI f1) Text";syntax
"BENENNE";locale_de

ORDER "USE"
"BENUTZE [1] Ding";syntax
"BENUTZE";locale_de

ORDER "DESCRIBE"
"BESCHREIBE REGION | SCHIFF | GEBÄUDE | BURG | EINHEIT | PRIVAT  Text";syntax
"BESCHREIBE";locale_de

ORDER "ENTER"
"BETRETE (BURG b1 | SCHIFF s1 | Ding x1)";syntax
"BETRETE";locale_de

ORDER "GUARD"
"BEWACHE [NICHT]";syntax
"BEWACHE";locale_de

ORDER "MESSAGE"
"BOTSCHAFT REGION | (SCHIFF s1) | (GEBÄUDE b2) | (BURG b3) | (EINHEIT u4) | (PARTEI f5)  Text";syntax
"BOTSCHAFT";locale_de

ORDER "DEFAULT"
"DEFAULT Order";syntax
"DEFAULT";locale_de

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

ORDER "RESEARCH"
"FORSCHE KRÄUTER";syntax
"FORSCHE";locale_de

ORDER "GIVE"
"GIB (u1|0) ( 1 | (JE 2) | ALLES  Ding | PERSONEN ) | ALLES | KRÄUTER | KOMMANDO | EINHEIT";syntax
"GIB";locale_de

ORDER "GROUP"
"GRUPPE [Name]";syntax
"GRUPPE";locale_de

ORDER "HELP"
"HELFE f1 ALLES|GIB|KÄMPFE|BEWACHE|SILBER|PARTEITARNUNG [NICHT] ";syntax
"HELFE";locale_de

ORDER "COMBAT"
"KÄMPFE [AGGRESSIV|HINTEN|DEFENSIV|NICHT|FLIEHE|(HELFE [NICHT])]";syntax
"KÄMPFE";locale_de

ORDER "COMBATSPELL"
"KAMPFZAUBER [STUFE 1] Zauber [NICHT]";syntax
"KAMPFZAUBER";locale_de

ORDER "BUY"
"KAUFE 1 Luxus";syntax
"KAUFE";locale_de

ORDER "CONTACT"
"KONTAKTIERE u1";syntax
"KONTAKTIERE";locale_de

ORDER "TEACH"
"LEHRE u1+";syntax
"LEHRE";locale_de

ORDER "LEARN"
"LERNE Talent [1]";syntax
"LERNE";locale_de

ORDER "SUPPLY"
"LIEFERE (u1|0) ( 1 | (JE 2) | ALLES  Ding | PERSONEN ) | ALLES | KRÄUTER | KOMMANDO | EINHEIT";syntax
"LIEFERE";locale_de

ORDER "LOCALE"
"LOCALE";syntax
"LOCALE";locale_de
1;internal

ORDER "MAKE"
"MACHE [(TEMP u1 [Name]) | ([1] Ding [s1|b1]) | ([1] STRAßE Richtung)]";syntax
"MACHE";locale_de

ORDER "MOVE"
"NACH Richtung1+";syntax
"NACH";locale_de

ORDER "NEXT"
"Invalid";syntax
"NÄCHSTER";locale_de
1;internal

ORDER "NUMBER"
"NUMMER (EINHEIT) | (SCHIFF) | (GEBÄUDE) | (BURG) | (PARTEI) [x1]";syntax
"NUMMER";locale_de

ORDER "OPTION"
"OPTION Option [Nicht]";syntax
"OPTION";locale_de

ORDER "FACTION"
"PARTEI f1";syntax
"PARTEI";locale_de
1;internal

ORDER "PASSWORD"
"PASSWORT passwort";syntax
"PASSWORT";locale_de

ORDER "PREFIX"
"PRÄFIX [Name]";syntax
"PRÄFIX";locale_de

ORDER "REGION"
"REGION 1 , 2";syntax
"REGION";locale_de
1;internal

ORDER "RECRUIT"
"REKRUTIERE 1";syntax
"REKRUTIERE";locale_de

ORDER "RESERVE"
"SAMMEL (1) | (ALLES) | (JE 2) Gegenstand";syntax
"SAMMEL";locale_de

ORDER "ROUTE"
"ROUTE Richtung1+";syntax
"ROUTE";locale_de

ORDER "SABOTAGE"
"SABOTIERE SCHIFF";syntax
"SABOTIERE";locale_de

ORDER "SORT"
"SORTIERE (VOR) | (HINTER) u1";syntax
"SORTIERE";locale_de

ORDER "SPY"
"SPIONIERE u1";syntax
"SPIONIERE";locale_de

ORDER "QUIT"
"STIRB Password";syntax
"STIRB";locale_de

ORDER "HIDE"
"TARNE ([1]) | (Rasse) | (PARTEI [NICHT]) | (PARTEI NUMMER f1)";syntax
"TARNE";locale_de

ORDER "TAX"
"TREIBE [1]";syntax
"TREIBE";locale_de

ORDER "ENTERTAIN"
"UNTERHALTE [1]";syntax
"UNTERHALTE";locale_de

ORDER "ORIGIN"
"URSPRUNG 1 2";syntax
"URSPRUNG";locale_de

ORDER "FORGET"
"VERGESSE Talent";syntax
"VERGESSE";locale_de

ORDER "SELL"
"VERKAUFE 1 | ALLES  Luxus";syntax
"VERKAUFE";locale_de

ORDER "LEAVE"
"VERLASSE";syntax
"VERLASSE";locale_de

ORDER "CAST"
"ZAUBERE [REGION 1 2] [STUFE 3] Zauber Parameter1*";syntax
"ZAUBERE";locale_de

ORDER "SHOW"
"ZEIGE (ding)|(ALLE [ZAUBER|TRÄNKE])";syntax
"ZEIGE";locale_de

ORDER "DESTROY"
"ZERSTÖRE [1] [STRASSE richtung]";syntax
"ZERSTÖRE";locale_de

ORDER "ANWERBEN"
"ANWERBEN";syntax
"ANWERBEN";locale_de

ORDER "ERKUNDEN"
"ERKUNDEN";syntax
"ERKUNDE";locale_de

ORDER "FINDE"
"FINDE (ALLES | f1)";syntax
"FINDE";locale_de

ORDER "MAGIEGEBIET"
"MAGIEGEBIET string";syntax
"MAGIEGEBIET";locale_de

ORDER "MEUCHELN"
"MEUCHELN u1+";syntax
"MEUCHELN";locale_de

ORDER "UNTERSTUTZE"
"UNTERSTUETZE u1";syntax
"UNTERSTUETZE";locale_de

ORDER "WEIHE"
"WEIHE string";syntax
"WEIHE";locale_de

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
"GEBÄUDE";locale_de
"BUILDING";locale_en
1;internal

ORDER "COMBAT_"
"KÄMPFE";locale_de
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

ORDER "COMBATSPELL"
"KAMPFZAUBER";locale_de
"COMBATSPELL";locale_en
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

ORDER "FACTION"
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

ORDER "HELP_COMBAT"
"KÄMPFE";locale_de
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
"KRÄUTER";locale_de
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

ORDER "NUMBER"
"NUMMER";locale_de
"NUMBER";locale_en
1;internal

ORDER "PAUSE"
"PAUSE";locale_de
"PAUSE";locale_en
1;internal

ORDER "POTIONS"
"TRÄNKE";locale_de
"POTIONS";locale_en
1;internal

ORDER "PRIVATE"
"PRIVAT";locale_de
"PRIVATE";locale_en
1;internal

ORDER "ROAD"
"STRAßE";locale_de
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
"BÄUME";locale_de
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
"SÜDOSTEN";locale_de
"SOUTHEAST";locale_en
"SO";locale_de
"SE";locale_en
1;internal

ORDER "SOUTHWEST"
"SÜDWESTEN";locale_de
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
"SÜDOSTEN";locale_de
"SOUTHEAST";locale_en
1;internal

ORDER "SW"
"SW";locale_de
"SW";locale_en
"SÜDWESTEN";locale_de
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


SKILLCATEGORY "war"
"Kampf";name
0;naturalorder

SKILLCATEGORY "magic"
"Magie";name
1;naturalorder

SKILLCATEGORY "expensive"
"Teure Talente";name
2;naturalorder

SKILLCATEGORY "resource"
"Ressourcen-Gewinnung";name
3;naturalorder

SKILLCATEGORY "silver"
"Silber-Gewinnung";name
4;naturalorder
"resource";parent

SKILLCATEGORY "build"
"Produktion";name
5;naturalorder

SKILLCATEGORY "move"
"Bewegung";name
6;naturalorder

SKILLCATEGORY "misc"
"Sonstiges";name
7;naturalorder

SKILL "Armbrustschießen"
"Armbrustschießen";name
"war";category
SKILL "Bogenschießen"
"Bogenschießen";name
"war";category
SKILL "Katapultbedienung"
"Katapultbedienung";name
"war";category
SKILL "Hiebwaffen"
"Hiebwaffen";name
"war";category
SKILL "Stangenwaffen"
"Stangenwaffen";name
"war";category
SKILL "Waffenbau"
"Waffenbau";name
"build";category
SKILL "Taktik"
"Taktik";name
"expensive";category
COSTS
50;1
100;2
150;3
200;4
250;5
300;6
350;7
400;8
450;9
500;10
550;11
600;12
650;13
700;14
750;15
800;16
850;17
900;18
950;19
1000;20
1050;21
1100;22
1150;23
1200;24
1250;25
1300;26
1350;27
1400;28
1450;29
1500;30
1550;31
1600;32
1650;33
1700;34
1750;35
1800;36
1850;37
1900;38
1950;39
2000;40
2050;41
2100;42
2150;43
2200;44
2250;45
2300;46
2350;47
2400;48
2450;49
2500;50
SKILL "Reiten"
"Reiten";name
"move";category
SKILL "Bergbau"
"Bergbau";name
"resource";category
SKILL "Burgenbau"
"Burgenbau";name
"build";category
SKILL "Handeln"
"Handeln";name
"silver";category
SKILL "Holzfällen"
"Holzfällen";name
"resource";category
SKILL "Magie"
"Magie";name
"magic";category
COSTS
50;1
200;2
450;3
800;4
1250;5
1800;6
2450;7
3200;8
4050;9
5000;10
6050;11
7200;12
8450;13
9800;14
11250;15
12800;16
14450;17
16200;18
18050;19
20000;20
22050;21
24200;22
26450;23
28800;24
31250;25
33800;26
36450;27
39200;28
42050;29
45000;30
48050;31
51200;32
54450;33
57800;34
61250;35
64800;36
68450;37
72200;38
76050;39
80000;40
84050;41
88200;42
92450;43
96800;44
101250;45
105800;46
110450;47
115200;48
120050;49
125000;50
SKILL "Pferdedressur"
"Pferdedressur";name
"resource";category
SKILL "Schiffbau"
"Schiffbau";name
"build";category
SKILL "Segeln"
"Segeln";name
"move";category
SKILL "Steinbau"
"Steinbau";name
"resource";category
SKILL "Straßenbau"
"Straßenbau";name
"build";category
SKILL "Tarnung"
"Tarnung";name
"misc";category
SKILL "Rüstungsbau"
"Rüstungsbau";name
"build";category
SKILL "Unterhaltung"
"Unterhaltung";name
"silver";category
SKILL "Wagenbau"
"Wagenbau";name
"build";category
SKILL "Bogenbau"
"Bogenbau";name
"build";category
SKILL "Wahrnehmung"
"Wahrnehmung";name
"misc";category
SKILL "Steuereintreiben"
"Steuereintreiben";name
"silver";category
SKILL "Meucheln"
"Meucheln";name
"expensive";category
COSTS
50;1
100;2
150;3
200;4
250;5
300;6
350;7
400;8
450;9
500;10
550;11
600;12
650;13
700;14
750;15
800;16
850;17
900;18
950;19
1000;20
1050;21
1100;22
1150;23
1200;24
1250;25
1300;26
1350;27
1400;28
1450;29
1500;30
1550;31
1600;32
1650;33
1700;34
1750;35
1800;36
1850;37
1900;38
1950;39
2000;40
2050;41
2100;42
2150;43
2200;44
2250;45
2300;46
2350;47
2400;48
2450;49
2500;50
SKILL "Kräuterkunde"
"Kräuterkunde";name
"expensive";category
COSTS
50;1
100;2
150;3
200;4
250;5
300;6
350;7
400;8
450;9
500;10
550;11
600;12
650;13
700;14
750;15
800;16
850;17
900;18
950;19
1000;20
1050;21
1100;22
1150;23
1200;24
1250;25
1300;26
1350;27
1400;28
1450;29
1500;30
1550;31
1600;32
1650;33
1700;34
1750;35
1800;36
1850;37
1900;38
1950;39
2000;40
2050;41
2100;42
2150;43
2200;44
2250;45
2300;46
2350;47
2400;48
2450;49
2500;50
SKILL "Alchimie"
"Alchimie";name
"expensive";category
COSTS
50;1
100;2
150;3
200;4
250;5
300;6
350;7
400;8
450;9
500;10
550;11
600;12
650;13
700;14
750;15
800;16
850;17
900;18
950;19
1000;20
1050;21
1100;22
1150;23
1200;24
1250;25
1300;26
1350;27
1400;28
1450;29
1500;30
1550;31
1600;32
1650;33
1700;34
1750;35
1800;36
1850;37
1900;38
1950;39
2000;40
2050;41
2100;42
2150;43
2200;44
2250;45
2300;46
2350;47
2400;48
2450;49
2500;50
SKILL "Anwerben"
"Anwerben";name
"misc";category

RACE "Menschen"
"Menschen";name
75;recruitmentcosts
10;weight
7;capacity

RACE "Termiten"
"Termiten";name
60;recruitmentcosts
3;weight
7;capacity
TALENTBONI
-2;Katapultbedienung
1;Stangenwaffen
1;Waffenbau
1;Taktik
-2;Reiten
1;Bergbau
-1;Burgenbau
1;Holzfällen
-2;Pferdedressur
-1;Schiffbau
-1;Segeln
-2;Steinbau
-1;Straßenbau
1;Tarnung
-1;Rüstungsbau
-1;Wagenbau
-1;Bogenbau
1;Steuereintreiben
1;Kräuterkunde
TALENTBONI "Wald"
1;Wahrnehmung
1;Tarnung

RACE "Elfen"
"Elfen";name
110;recruitmentcosts
10;weight
7;capacity
TALENTBONI
-1;Armbrustschießen
2;Bogenschießen
-2;Katapultbedienung
-1;Hiebwaffen
-2;Bergbau
-1;Burgenbau
1;Magie
1;Pferdedressur
-1;Schiffbau
-1;Segeln
-1;Steinbau
-1;Straßenbau
1;Tarnung
-1;Rüstungsbau
2;Bogenbau
1;Wahrnehmung
2;Kräuterkunde
TALENTBONI "Wald"
1;Wahrnehmung
1;Tarnung

RACE "Aquaner"
"Aquaner";name
80;recruitmentcosts
10;weight
7;capacity
TALENTBONI
-2;Katapultbedienung
-1;Hiebwaffen
1;Stangenwaffen
-1;Reiten
-1;Bergbau
2;Handeln
1;Holzfällen
-1;Pferdedressur
3;Schiffbau
3;Segeln
-2;Straßenbau
-2;Rüstungsbau
-1;Wagenbau
-1;Alchimie
TALENTBONI "Ozean"
1;Armbrustschießen
1;Bogenschießen
1;Katapultbedienung
1;Hiebwaffen
1;Stangenwaffen
1;Waffenbau
1;Taktik
1;Reiten
1;Bergbau
1;Burgenbau
1;Handeln
1;Holzfällen
1;Magie
1;Pferdedressur
1;Schiffbau
1;Segeln
1;Steinbau
1;Straßenbau
1;Tarnung
1;Rüstungsbau
1;Unterhaltung
1;Wagenbau
1;Bogenbau
1;Wahrnehmung
1;Steuereintreiben
1;Meucheln
1;Kräuterkunde
1;Alchimie
1;Anwerben

RACE "Feen"
"Feen";name
110;recruitmentcosts
10;weight
7;capacity
TALENTBONI
-3;Katapultbedienung
-1;Hiebwaffen
-1;Stangenwaffen
-2;Waffenbau
-1;Taktik
-1;Bergbau
-1;Burgenbau
1;Handeln
1;Magie
1;Pferdedressur
-1;Schiffbau
-1;Segeln
-2;Steinbau
1;Tarnung
-1;Rüstungsbau
1;Unterhaltung
-1;Wagenbau
1;Bogenbau
1;Wahrnehmung
-1;Steuereintreiben
-2;Meucheln
1;Kräuterkunde

RACE "Riesen"
"Riesen";name
100;recruitmentcosts
20;weight
17;capacity
TALENTBONI
-2;Armbrustschießen
-2;Bogenschießen
1;Katapultbedienung
-2;Reiten
1;Bergbau
2;Burgenbau
1;Holzfällen
-2;Pferdedressur
-1;Schiffbau
-2;Segeln
3;Steinbau
2;Straßenbau
-2;Tarnung
1;Rüstungsbau
-1;Bogenbau
-1;Meucheln
-1;Kräuterkunde

RACE "Zwerge"
"Zwerge";name
90;recruitmentcosts
10;weight
7;capacity
TALENTBONI
-1;Bogenschießen
2;Katapultbedienung
1;Hiebwaffen
2;Waffenbau
-2;Reiten
2;Bergbau
2;Burgenbau
1;Handeln
-1;Holzfällen
-2;Magie
-1;Schiffbau
1;Steinbau
2;Straßenbau
-1;Tarnung
2;Rüstungsbau
-1;Unterhaltung
1;Steuereintreiben
-1;Meucheln
-1;Kräuterkunde
TALENTBONI "Berge"
1;Armbrustschießen
1;Bogenschießen
1;Katapultbedienung
1;Hiebwaffen
1;Stangenwaffen
1;Waffenbau
1;Taktik
1;Reiten
1;Bergbau
1;Burgenbau
1;Handeln
1;Holzfällen
1;Magie
1;Pferdedressur
1;Schiffbau
1;Segeln
1;Steinbau
1;Straßenbau
1;Tarnung
1;Rüstungsbau
1;Unterhaltung
1;Wagenbau
1;Bogenbau
1;Wahrnehmung
1;Steuereintreiben
1;Meucheln
1;Kräuterkunde
1;Alchimie
1;Anwerben
TALENTBONI "Gletscher"
1;Armbrustschießen
1;Bogenschießen
1;Katapultbedienung
1;Hiebwaffen
1;Stangenwaffen
1;Waffenbau
1;Taktik
1;Reiten
1;Bergbau
1;Burgenbau
1;Handeln
1;Holzfällen
1;Magie
1;Pferdedressur
1;Schiffbau
1;Segeln
1;Steinbau
1;Straßenbau
1;Tarnung
1;Rüstungsbau
1;Unterhaltung
1;Wagenbau
1;Bogenbau
1;Wahrnehmung
1;Steuereintreiben
1;Meucheln
1;Kräuterkunde
1;Alchimie
1;Anwerben

RACE "Vampire"
"Vampire";name
200;recruitmentcosts
0;maintenance
10;weight
7;capacity
TALENTBONI
-1;Armbrustschießen
-1;Bogenschießen
2;Hiebwaffen
-1;Stangenwaffen
-1;Reiten
1;Burgenbau
-2;Handeln
-1;Holzfällen
1;Magie
-2;Segeln
1;Steinbau
1;Tarnung
-1;Bogenbau
1;Wahrnehmung
1;Steuereintreiben
-1;Kräuterkunde
1;Alchimie

RACE "Goblins"
"Goblins";name
45;recruitmentcosts
8;maintenance
7;weight
5;capacity
TALENTBONI
1;Katapultbedienung
-2;Taktik
1;Bergbau
1;Burgenbau
-1;Handeln
-1;Magie
-1;Pferdedressur
-2;Schiffbau
-1;Segeln
-2;Straßenbau
2;Tarnung
-2;Unterhaltung
-1;Wagenbau
1;Bogenbau
2;Meucheln
1;Alchimie

RACE "Orks"
"Orks";name
50;recruitmentcosts
10;weight
7;capacity
TALENTBONI
2;Waffenbau
1;Taktik
1;Bergbau
1;Burgenbau
-3;Handeln
1;Holzfällen
-1;Magie
-1;Pferdedressur
-1;Schiffbau
-3;Segeln
1;Steinbau
1;Rüstungsbau
-2;Unterhaltung
-1;Wagenbau
1;Steuereintreiben
-2;Kräuterkunde

RACE "Zentauren"
"Zentauren";name
100;recruitmentcosts
15;weight
7;capacity
TALENTBONI
1;Armbrustschießen
-1;Katapultbedienung
-1;Hiebwaffen
2;Reiten
-1;Bergbau
-2;Burgenbau
1;Handeln
1;Pferdedressur
-2;Schiffbau
-1;Segeln
-1;Steinbau
-1;Straßenbau
1;Tarnung
-1;Rüstungsbau
-1;Wagenbau
1;Bogenbau
2;Wahrnehmung
1;Kräuterkunde

RACE "Hydren"
"Hydren";name
90;recruitmentcosts
10;weight
7;capacity
TALENTBONI
1;Stangenwaffen
1;Waffenbau
1;Taktik
1;Handeln
-1;Holzfällen
-1;Pferdedressur
-1;Steinbau
1;Rüstungsbau
-1;Bogenbau
1;Steuereintreiben
1;Meucheln
1;Kräuterkunde
1;Alchimie
TALENTBONI "Wüste"
1;Armbrustschießen
1;Bogenschießen
1;Katapultbedienung
1;Hiebwaffen
1;Stangenwaffen
1;Waffenbau
1;Taktik
1;Reiten
1;Bergbau
1;Burgenbau
1;Handeln
1;Holzfällen
1;Magie
1;Pferdedressur
1;Schiffbau
1;Segeln
1;Steinbau
1;Straßenbau
1;Tarnung
1;Rüstungsbau
1;Unterhaltung
1;Wagenbau
1;Bogenbau
1;Wahrnehmung
1;Steuereintreiben
1;Meucheln
1;Kräuterkunde
1;Alchimie
1;Anwerben
TALENTBONI "Sumpf"
1;Armbrustschießen
1;Bogenschießen
1;Katapultbedienung
1;Hiebwaffen
1;Stangenwaffen
1;Waffenbau
1;Taktik
1;Reiten
1;Bergbau
1;Burgenbau
1;Handeln
1;Holzfällen
1;Magie
1;Pferdedressur
1;Schiffbau
1;Segeln
1;Steinbau
1;Straßenbau
1;Tarnung
1;Rüstungsbau
1;Unterhaltung
1;Wagenbau
1;Bogenbau
1;Wahrnehmung
1;Steuereintreiben
1;Meucheln
1;Kräuterkunde
1;Alchimie
1;Anwerben
TALENTBONI "Gletscher"
-1;Armbrustschießen
-1;Bogenschießen
-1;Katapultbedienung
-1;Hiebwaffen
-1;Stangenwaffen
-1;Waffenbau
-1;Taktik
-1;Reiten
-1;Bergbau
-1;Burgenbau
-1;Handeln
-1;Holzfällen
-1;Magie
-1;Pferdedressur
-1;Schiffbau
-1;Segeln
-1;Steinbau
-1;Straßenbau
-1;Tarnung
-1;Rüstungsbau
-1;Unterhaltung
-1;Wagenbau
-1;Bogenbau
-1;Wahrnehmung
-1;Steuereintreiben
-1;Meucheln
-1;Kräuterkunde
-1;Alchimie
-1;Anwerben
TALENTBONI "Eisscholle"
-1;Armbrustschießen
-1;Bogenschießen
-1;Katapultbedienung
-1;Hiebwaffen
-1;Stangenwaffen
-1;Waffenbau
-1;Taktik
-1;Reiten
-1;Bergbau
-1;Burgenbau
-1;Handeln
-1;Holzfällen
-1;Magie
-1;Pferdedressur
-1;Schiffbau
-1;Segeln
-1;Steinbau
-1;Straßenbau
-1;Tarnung
-1;Rüstungsbau
-1;Unterhaltung
-1;Wagenbau
-1;Bogenbau
-1;Wahrnehmung
-1;Steuereintreiben
-1;Meucheln
-1;Kräuterkunde
-1;Alchimie
-1;Anwerben
TALENTBONI "Berge"
-1;Armbrustschießen
-1;Bogenschießen
-1;Katapultbedienung
-1;Hiebwaffen
-1;Stangenwaffen
-1;Waffenbau
-1;Taktik
-1;Reiten
-1;Bergbau
-1;Burgenbau
-1;Handeln
-1;Holzfällen
-1;Magie
-1;Pferdedressur
-1;Schiffbau
-1;Segeln
-1;Steinbau
-1;Straßenbau
-1;Tarnung
-1;Rüstungsbau
-1;Unterhaltung
-1;Wagenbau
-1;Bogenbau
-1;Wahrnehmung
-1;Steuereintreiben
-1;Meucheln
-1;Kräuterkunde
-1;Alchimie
-1;Anwerben

RACE "EINHEIT"
"EINHEIT";name

RACE "Zombies"
"Zombies";name
7;capacity
10;weight

RACE "Skelette"
"Skelette";name
7;capacity
10;weight

RACE "Schattendämonen"
"Schattendämonen";name
30;capacity
10;weight

RACE "Geflügelte Schrecken"
"Geflügelte Schrecken";name
50;capacity
10;weight

RACE "Gehörnte Fürsten der Nacht"
"Gehörnte Fürsten der Nacht";name
100;capacity
10;weight

RACE "Skelettlords"
"Skelettlords";name
7;capacity
10;weight

RACE "Geister"
"Geister";name
1;capacity
10;weight

RACE "Eidechsen"
"Eidechsen";name

RACE "Baumdrachen"
"Baumdrachen";name

RACE "Purpurdrachen"
"Purpurdrachen";name

RACE "Lindwürmer"
"Lindwürmer";name

RACE "Feuerdrachen"
"Feuerdrachen";name

RACE "Karfunkeldrachen"
"Karfunkeldrachen";name

RACE "Große Wyrme"
"Große Wyrme";name

RACE "Wölfe"
"Wölfe";name

RACE "Nashörner"
"Nashörner";name

RACE "Bären"
"Bären";name

RACE "Adler"
"Adler";name

RACE "Mamuts"
"Mamuts";name

RACE "Wale"
"Wale";name

RACE "Walrosse"
"Walrosse";name

RACE "Büffel"
"Büffel";name

RACE "Säbelzahntiger"
"Säbelzahntiger";name

RACE "Raben"
"Raben";name

RACE "Riesenspinnen"
"Riesenspinnen";name

RACE "Schildkröten"
"Schildkröten";name

ITEMCATEGORY "weapons"
"Waffen";name
0;naturalorder

ITEMCATEGORY "front weapons"
"Front-Waffen";name
"weapons";parent
0;naturalorder

ITEMCATEGORY "distance weapons"
"Distanzwaffen";name
"weapons";parent
1;naturalorder

ITEMCATEGORY "magic"
"Artefakte";name
1;naturalorder

ITEMCATEGORY "armour"
"Rüstungen";name
2;naturalorder

ITEMCATEGORY "shield"
"Schilder";name
0;naturalorder
"armour";parent

ITEMCATEGORY "resources"
"Rohstoffe";name
3;naturalorder

ITEMCATEGORY "herb"
"Kräuter";name
0;naturalorder
"resources";parent

ITEMCATEGORY "potions"
"Tränke";name
4;naturalorder

ITEMCATEGORY "luxuries"
"Handelsgüter";name
5;naturalorder

ITEMCATEGORY "misc"
"Sonstiges";name
6;naturalorder

ITEMCATEGORY "trophies"
"Trphaeen";name
0;naturalorder
"misc";parent


ITEM "Silber"
"Silber";name
0.01;weight
"misc";category

ITEM "Eisen"
"Eisen";name
5;weight
"resources";category
"Bergbau";makeskill

ITEM "Holz"
"Holz";name
5;weight
"resources";category
"Holzfällen";makeskill

ITEM "Mithril"
"Mithril";name
5;weight
"resources";category
"Bergbau";makeskill
5;makeskilllevel

ITEM "Stein"
"Stein";name
60;weight
"resources";category
"Steinbau";makeskill

ITEM "Pferd"
"Pferd";name
50;weight
"resources";category
"Pferdedressur";makeskill
1;makeskilllevel
1;ishorse

ITEM "Wagen"
"Wagen";name
40;weight
"misc";category
"Wagenbau";makeskill
RESOURCES
5;Holz

ITEM "Giftdolch"
"Giftdolch";name
1;weight
"front weapons";category
"Waffenbau";makeskill
7;makeskilllevel
RESOURCES
1;Eisen
1;Schwarzer Lotus

ITEM "Basiliskenzunge"
"Basiliskenzunge";name
1;weight
"front weapons";category
"Waffenbau";makeskill
6;makeskilllevel
RESOURCES
1;Eisen

ITEM "Harpune"
"Harpune";name
1;weight
"distance weapons";category
"Waffenbau";makeskill
6;makeskilllevel
RESOURCES
1;Eisen

ITEM "Katapult"
"Katapult";name
60;weight
"distance weapons";category
"Katapultbedienung";useskill
"Wagenbau";makeskill
3;makeskilllevel
RESOURCES
10;Holz
2;Eisen

ITEM "Balliste"
"Balliste";name
30;weight
"distance weapons";category
"Katapultbedienung";useskill
"Wagenbau";makeskill
6;makeskilllevel
RESOURCES
5;Holz
1;Eisen

ITEM "Schwert"
"Schwert";name
1;weight
"Waffenbau";makeskill
"Hiebwaffen";useskill
"weapons";category
RESOURCES
1;Eisen

ITEM "Speer"
"Speer";name
1;weight
"front weapons";category
"Stangenwaffen";useskill
"Waffenbau";makeskill
RESOURCES
1;Holz

ITEM "Streitkolben"
"Streitkolben";name
2;weight
"front weapons";category
"Hiebwaffen";useskill
"Waffenbau";makeskill
3;makeskilllevel
RESOURCES
2;Eisen

ITEM "Kriegsaxt"
"Kriegsaxt";name
2;weight
"front weapons";category
"Hiebwaffen";useskill
"Waffenbau";makeskill
4;makeskilllevel
RESOURCES
1;Eisen
1;Holz

ITEM "Lanze"
"Lanze";name
2;weight
"front weapons";category
"Stangenwaffen";useskill
"Waffenbau";makeskill
3;makeskilllevel
RESOURCES
2;Holz

ITEM "Hellebarde"
"Hellebarde";name
2;weight
"front weapons";category
"Stangenwaffen";useskill
"Waffenbau";makeskill
4;makeskilllevel
RESOURCES
1;Eisen
1;Holz

ITEM "Armbrust"
"Armbrust";name
1;weight
"distance weapons";category
"Bogenbau";makeskill
3;makeskilllevel
RESOURCES
1;Holz

ITEM "Bogen"
"Bogen";name
1;weight
"distance weapons";category
"Bogenbau";makeskill
2;makeskilllevel
RESOURCES
1;Holz

ITEM "Elfenbogen"
"Elfenbogen";name
1;weight
"distance weapons";category
"Bogenbau";makeskill
6;makeskilllevel
RESOURCES
2;Holz

ITEM "Steinkeule"
"Steinkeule";name
6;weight
"front weapons";category
"Hiebwaffen";useskill
"Waffenbau";makeskill
5;makeskilllevel
RESOURCES
2;Stein

ITEM "Kriegshammer"
"Kriegshammer";name
2;weight
"front weapons";category
"Hiebwaffen";useskill
"Waffenbau";makeskill
8;makeskilllevel
RESOURCES
3;Mithril

ITEM "Kurzspeer"
"Kurzspeer";name
1;weight
"front weapons";category
"Stangenwaffen";useskill
"Waffenbau";makeskill
6;makeskilllevel
RESOURCES
1;Holz
1;Eisen

ITEM "Parierdolch"
"Parierdolch";name
1;weight
"front weapons";category
"Hiebwaffen";useskill
"Waffenbau";makeskill
6;makeskilllevel
RESOURCES
1;Eisen

ITEM "Lederhemd"
"Lederhemd";name
0;weight
"armour";category
"Rüstungsbau";makeskill
3;makeskilllevel
RESOURCES
1;Pferd

ITEM "Schattenpanzer"
"Schattenpanzer";name
2;weight
"armour";category
"Rüstungsbau";makeskill
8;makeskilllevel
RESOURCES
1;Mithrilplattenpanzer

ITEM "Stachelschild"
"Stachelschild";name
3;weight
"shield";category
"Rüstungsbau";makeskill
6;makeskilllevel
RESOURCES
7;Eisen

ITEM "Mithrilplattenpanzer"
"Mithrilplattenpanzer";name
2;weight
"armour";category
"Rüstungsbau";makeskill
8;makeskilllevel
RESOURCES
5;Mithril

ITEM "Kettenhemd"
"Kettenhemd";name
2;weight
"armour";category
"Rüstungsbau";makeskill
RESOURCES
3;Eisen

ITEM "Rundschild"
"Rundschild";name
1;weight
"shield";category
"Rüstungsbau";makeskill
2;makeskilllevel
RESOURCES
2;Holz

ITEM "Turmschild"
"Turmschild";name
3;weight
"shield";category
"Rüstungsbau";makeskill
5;makeskilllevel
RESOURCES
5;Eisen

ITEM "Mithrilschild"
"Mithrilschild";name
3;weight
"shield";category
"Rüstungsbau";makeskill
8;makeskilllevel
RESOURCES
5;Mithril
2;Eisen

ITEM "Plattenpanzer"
"Plattenpanzer";name
3;weight
"armour";category
"Rüstungsbau";makeskill
3;makeskilllevel
RESOURCES
5;Eisen

ITEM "Drachenpanzer"
"Drachenpanzer";name
2;weight
"armour";category

ITEM "Schattenklinge"
"Schattenklinge";name
1;weight
"magic";category
"Hiebwaffen";useskill

ITEM "KRÄUTER"
"KRÄUTER";name
"Kräuterkunde";makeskill
2;makeskilllevel

ITEM "Chitinpanzer"
"Chitinpanzer";name
1;weight
"armour";category

ITEM "Drachenkopf"
"Drachenkopf";name
2;weight
"msic";category

ITEM "Drachenblut"
"Drachenblut";name
1;weight
"misc";category

ITEM "Dämonenblut"
"Dämonenblut";name
0;weight
"misc";category

ITEM "Mistelzweig"
"Mistelzweig";name
0;weight
"misc";category

ITEM "Bauernblut"
"Bauernblut";name
0;weight
"misc";category

ITEM "Schädel"
"Schädel";name
1;weight
"misc";category

ITEM "Horn eines Einhorns"
"Horn eines Einhorns";name
1;weight
"misc";category

ITEM "Horn"
"Horn";name
1;weight
"trophy";category

ITEM "Echsenei"
"Echsenei";name
1;weight
"misc";category

ITEM "Spinnennetz"
"Spinnennetz";name
0;weight
"trophy";category

ITEM "Tierherz"
"Tierherz";name
1;weight
"trophy";category

ITEM "Wolke"
"Wolke";name
0;weight
"misc";category

ITEM "Schildkrötenpanzer"
"Schildkrötenpanzer";name
2;weight
"trophy";category

ITEM "Zaubersand"
"Zaubersand";name
1;weight
"misc";category

ITEM "Feder"
"Feder";name
0;weight
"trophy";category

ITEM "Rippe"
"Rippe";name
1;weight
"misc";category

ITEM "Flosse"
"Flosse";name
2;weight
"trophy";category

ITEM "Reißzahn"
"Reißzahn";name
0;weight
"trophy";category

ITEM "Stoßzahn"
"Stoßzahn";name
4;weight
"trophy";category

ITEM "Einhorn"
"Einhorn";name
50;weight
"magic";category

ITEM "Hippogryff"
"Hippogryff";name
50;weight
"magic";category

ITEM "Balsam"
"Balsam";name
2;weight
"luxuries";category
"balsam";iconname

ITEM "Gewürz"
"Gewürz";name
1;weight
"luxuries";category
"gewuerz";iconname

ITEM "Juwel"
"Juwel";name
1;weight
"luxuries";category
"juwel";iconname

ITEM "Myrrhe"
"Myrrhe";name
1;weight
"luxuries";category
"myrrhe";iconname

ITEM "Öl"
"Öl";name
2;weight
"luxuries";category
"oel";iconname

ITEM "Seide"
"Seide";name
2;weight
"luxuries";category
"seide";iconname

ITEM "Perle"
"Perle";name
1;weight
"luxuries";category

ITEM "Wein"
"Wein";name
2;weight
"luxuries";category

ITEM "Samt"
"Samt";name
2;weight
"luxuries";category

ITEM "Weihrauch"
"Weihrauch";name
2;weight
"luxuries";category

ITEM "Wanderslust"
"Wanderslust";name
0;weight
"potions";category
"Alchimie";makeskill
2;makeskilllevel

ITEM "Wasser des Lebens"
"Wasser des Lebens";name
0;weight
"potions";category
"Alchimie";makeskill
2;makeskilllevel


ITEM "Heilsalbe"
"Heilsalbe";name
0;weight
"potions";category
"Alchimie";makeskill
2;makeskilllevel


ITEM "Pferdesegen"
"Pferdesegen";name
0;weight
"potions";category
"Alchimie";makeskill
4;makeskilllevel

ITEM "Rachenlodern"
"Rachenlodern";name
0;weight
"potions";category
"Alchimie";makeskill
4;makeskilllevel

ITEM "Doppelsicht"
"Doppelsicht";name
0;weight
"potions";category
"Alchimie";makeskill
4;makeskilllevel

ITEM "Hirtenblut"
"Hirtenblut";name
0;weight
"potions";category
"Alchimie";makeskill
6;makeskilllevel

ITEM "Wissenstrunk"
"Wissenstrunk";name
0;weight
"potions";category
"Alchimie";makeskill
6;makeskilllevel

ITEM "Berserkerswut"
"Berserkerswut";name
0;weight
"potions";category
"Alchimie";makeskill
6;makeskilllevel

ITEM "Kinderreich"
"Kinderreich";name
0;weight
"potions";category
"Alchimie";makeskill
8;makeskilllevel

ITEM "Heiltrank"
"Heiltrank";name
0;weight
"potions";category
"Alchimie";makeskill
8;makeskilllevel

ITEM "Drachenhaut"
"Drachenhaut";name
0;weight
"potions";category
"Alchimie";makeskill
8;makeskilllevel

ITEM "Astraler Bann"
"Astraler Bann";name
0;weight
"potions";category
"Alchimie";makeskill
10;makeskilllevel

ITEM "Arcanas Segen"
"Arcanas Segen";name
0;weight
"potions";category
"Alchimie";makeskill
10;makeskilllevel

ITEM "Ewiges Leben"
"Ewiges Leben";name
0;weight
"potions";category
"Alchimie";makeskill
10;makeskilllevel

ITEM "Amulett der Heilung"
"Amulett der Heilung";name
0;weight
"magic";category

ITEM "Amulett des wahren Sehens"
"Amulett des wahren Sehens";name
0;weight
"magic";category

ITEM "Mantel der Unverletzlichkeit"
"Mantel der Unverletzlichkeit";name
0;weight
"magic";category

ITEM "Ring der Unsichtbarkeit"
"Ring der Unsichtbarkeit";name
0;weight
"magic";category

ITEM "Ring der Macht"
"Ring der Macht";name
0;weight
"magic";category

ITEM "Antimagiekristall"
"Antimagiekristall";name
0;weight
"magic";category

ITEM "Runenschwert"
"Runenschwert";name
0;weight
"magic";category
"Hiebwaffen";useskill

ITEM "Schildstein"
"Schildstein";name
0;weight
"magic";category

ITEM "Helm der Sieben Winde"
"Helm der Sieben Winde";name
0;weight
"magic";category

ITEM "Seelenstein"
"Seelenstein";name
0;weight
"magic";category

ITEM "Siebenmeilenstiefel"
"Siebenmeilenstiefel";name
0;weight
"magic";category

ITEM "Runenpanzer"
"Runenpanzer";name
0;weight
"magic";category

ITEM "Knochenamulett"
"Knochenamulett";name
0;weight
"magic";category


HERB "Hexenfinger"
"Hexenfinger";name
"Sumpf";region
"herb";category
"hexenfinger";iconname
HERB "Jaborose"
"Jaborose";name
"Sumpf";region
"herb";category
"snowcrystal petal";iconname
HERB "Schwarzer Lotus"
"Schwarzer Lotus";name
"Sumpf";region
"herb";category
"schwarzer lotus";iconname
HERB "Krötenpilz"
"Krötenpilz";name
"Sumpf";region
"herb";category
"fjord fungus";iconname
HERB "Alkanna"
"Alkanna";name
"Ebene";region
"herb";category
"gapgrowth";iconname
HERB "Vernonia"
"Vernonia";name
"Ebene";region
"herb";category
"bugleweed";iconname
HERB "Wurmfarn"
"Wurmfarn";name
"Wald";region
"herb";category
"tangy temerity";iconname
HERB "Schleierkraut"
"Schleierkraut";name
"Wald";region
"herb";category
"white hemlock";iconname
HERB "Yamswurzel"
"Yamswurzel";name
"Wüste";region
"herb";category
"mandrake";iconname
HERB "Dornenpalme"
"Dornenpalme";name
"Wüste";region
"herb";category
"dornenpalme";iconname
HERB "Trockener Wanderich"
"Trockener Wanderich";name
"Wüste";region
"herb";category
"sandreeker";iconname
HERB "Fenchelkaktus"
"Fenchelkaktus";name
"Wüste";region
"herb";category
"peyote";iconname
HERB "Perilla"
"Perilla";name
"Hochland";region
"herb";category
"elvendear";iconname
HERB "Gamander"
"Gamander";name
"Hochland";region
"herb";category
"gamander";iconname
HERB "Kaskarilla"
"Kaskarilla";name
"Hochland";region
"herb";category
"windbag";iconname
HERB "Ololiuqui"
"Ololiuqui";name
"Hochland";region
"herb";category
"ice begonia";iconname
HERB "Felberich"
"Felberich";name
"Berge";region
"herb";category
"felberich";iconname
HERB "Wandermoos"
"Wandermoos";name
"Berge";region
"herb";category
"cave lichen";iconname
HERB "Schlingflechte"
"Schlingflechte";name
"Gletscher";region
"herb";category
"bubblemorel";iconname
HERB "Schneeglüh"
"Schneeglüh";name
"Gletscher";region
"herb";category
"schneeglueh";iconname



SHIPTYPE "Boot"
"Boot";name
5;size
1;level
3;range
50;capacity
1;captainlevel
2;sailorlevel

SHIPTYPE "Langboot"
"Langboot";name
50;size
1;level
4;range
500;capacity
1;captainlevel
10;sailorlevel
SHIPTYPE "Drachenschiff"
"Drachenschiff";name
100;size
2;level
6;range
1000;capacity
2;captainlevel
50;sailorlevel

SHIPTYPE "Karawane"
"Karawane";name
0;size
0;level
2;range
0;captainlevel
0;sailorlevel

SHIPTYPE "Karavelle"
"Karavelle";name
250;size
3;level
6;range
3000;capacity
3;captainlevel
30;sailorlevel

SHIPTYPE "Trireme"
"Trireme";name
200;size
4;level
8;range
2000;capacity
4;captainlevel
120;sailorlevel

SHIPTYPE "Galeone"
"Galeone";name
300;size
5;level
10;range
2000;capacity
5;captainlevel
180;sailorlevel

CASTLETYPE "Grundmauern"
"Grundmauern";name
1;level
1;minsize
9;maxsize
11;wage
0;tradetax
RAWMATERIALS
1;Stein
CASTLETYPE "Handelsposten"
"Handelsposten";name
2;level
10;minsize
59;maxsize
12;wage
0;tradetax
RAWMATERIALS
1;Stein
CASTLETYPE "Befestigung"
"Befestigung";name
3;level
60;minsize
349;maxsize
13;wage
0;tradetax
RAWMATERIALS
1;Stein
CASTLETYPE "Turm"
"Turm";name
4;level
350;minsize
999;maxsize
14;wage
0;tradetax
RAWMATERIALS
1;Stein
CASTLETYPE "Schloss"
"Schloss";name
5;level
1000;minsize
2999;maxsize
15;wage
0;tradetax
RAWMATERIALS
1;Stein
CASTLETYPE "Festung"
"Festung";name
6;level
3000;minsize
11999;maxsize
16;wage
0;tradetax
RAWMATERIALS
1;Stein
CASTLETYPE "Zitadelle"
"Zitadelle";name
7;level
12000;minsize
17;wage
0;tradetax
RAWMATERIALS
1;Stein

REGIONTYPE "Ozean"
"Ozean";name
"true";isOcean
REGIONTYPE "Ebene"
"Ebene";name
10000;maxworkers
25;roadStones
REGIONTYPE "Wald"
"Wald";name
10000;maxworkers
25;roadStones
REGIONTYPE "Sumpf"
"Sumpf";name
2000;maxworkers
100;roadStones
REGIONTYPE "Wüste"
"Wüste";name
500;maxworkers
50;roadStones
REGIONTYPE "Hochland"
"Hochland";name
4000;maxworkers
50;roadStones
REGIONTYPE "Berge"
"Berge";name
1000;maxworkers
100;roadStones
REGIONTYPE "Gletscher"
"Gletscher";name
100;maxworkers
100;roadStones
REGIONTYPE "Feuerwand"
"Feuerwand";name
REGIONTYPE "Eisscholle"
"Eisscholle";name
"true";isOcean
REGIONTYPE "aktiver Vulkan"
"aktiver Vulkan";name
100;roadStones
REGIONTYPE "Vulkan"
"Vulkan";name
10000;maxworkers
100;roadStones


BUILDINGTYPE "Monument"
"Monument";name
2;level
RAWMATERIALS
1;Stein
1;Eisen
100;Silber
REGIONTYPES
"Ebene"
"Wald"
"Sumpf"
"Wüste"
"Hochland"
"Berge"
"Gletscher"
"Eisscholle"
"aktiver Vulkan"

BUILDINGTYPE "Bergwerk"
"Bergwerk";name
5;level
MAINTENANCE
500;Silber
RAWMATERIALS
1;Stein
1;Eisen
100;Silber
REGIONTYPES
"Ebene"
"Wald"
"Sumpf"
"Wüste"
"Hochland"
"Berge"
"Gletscher"
"Eisscholle"
"aktiver Vulkan"

BUILDINGTYPE "Schmiede"
"Schmiede";name
5;level
MAINTENANCE
500;Silber
RAWMATERIALS
1;Stein
1;Holz
100;Silber
REGIONTYPES
"Ebene"
"Wald"
"Sumpf"
"Wüste"
"Hochland"
"Berge"
"Gletscher"
"Eisscholle"
"aktiver Vulkan"

BUILDINGTYPE "Werft"
"Werft";name
5;level
100;maxsize
MAINTENANCE
500;Silber
RAWMATERIALS
1;Stein
1;Holz
100;Silber
REGIONTYPES
"Ebene"
"Wald"
"Sumpf"
"Wüste"
"Hochland"
"Berge"
"Gletscher"
"Eisscholle"
"aktiver Vulkan"

BUILDINGTYPE "Marktplatz"
"Marktplatz";name
5;level
100;maxsize
MAINTENANCE
500;Silber
RAWMATERIALS
1;Stein
1;Holz
100;Silber
REGIONTYPES
"Ebene"
"Wald"
"Sumpf"
"Wüste"
"Hochland"
"Berge"
"Gletscher"
"Eisscholle"
"aktiver Vulkan"

BUILDINGTYPE "Leuchtturm"
"Leuchtturm";name
2;level
MAINTENANCE
500;Silber
RAWMATERIALS
1;Stein
1;Holz
100;Silber
REGIONTYPES
"Ebene"
"Wald"
"Sumpf"
"Wüste"
"Hochland"
"Berge"
"Gletscher"
"Eisscholle"
"aktiver Vulkan"

BUILDINGTYPE "Baumschule"
"Baumschule";name
5;level
100;maxsize
MAINTENANCE
500;Silber
RAWMATERIALS
1;Stein
1;Holz
100;Silber
REGIONTYPES
"Ebene"
"Wald"
"Sumpf"
"Wüste"
"Hochland"
"Berge"
"Gletscher"
"Eisscholle"
"aktiver Vulkan"

BUILDINGTYPE "Magierturm"
"Magierturm";name
9;level
100;maxsize
MAINTENANCE
500;Silber
RAWMATERIALS
1;Stein
1;Holz
100;Silber
REGIONTYPES
"Ebene"
"Wald"
"Sumpf"
"Wüste"
"Hochland"
"Berge"
"Gletscher"
"Eisscholle"
"aktiver Vulkan"

BUILDINGTYPE "Universität"
"Universität";name
9;level
100;maxsize
MAINTENANCE
500;Silber
RAWMATERIALS
1;Stein
1;Holz
100;Silber
REGIONTYPES
"Ebene"
"Wald"
"Sumpf"
"Wüste"
"Hochland"
"Berge"
"Gletscher"
"Eisscholle"
"aktiver Vulkan"
