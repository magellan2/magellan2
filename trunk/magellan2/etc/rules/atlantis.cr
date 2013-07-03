VERSION 66
RULES "$Id: atlantis.cr 1720"

MAGELLAN
"magellan.library.gamebinding.atlantis.AtlantisSpecificStuff";class
"FACTION";orderFileStartingString

ORDER "FORM"
"FORM u1";syntax
"FORM";locale_en

ORDER "END"
"END";syntax
"END";locale_en

ORDER "ACCEPT"
"ACCEPT f1";syntax
"ACCEPT";locale_en

ORDER "ADDRESS"
"ADDRESS Address";syntax
"ADDRESS";locale_en

ORDER "ADMIT"
"ADMIT f1";syntax
"ADMIT";locale_en

ORDER "ALLY"
"ALLY f1 01";syntax
"ALLY";locale_en

ORDER "BEHIND"
"BEHIND 01";syntax
"BEHIND";locale_en

ORDER "COMBAT"
"COMBAT spell";syntax
"COMBAT";locale_en

ORDER "DISPLAY"
"DISPLAY (UNIT | BUILDING | SHIP) string";syntax
"DISPLAY";locale_en

ORDER "GUARD"
"GUARD 01";syntax
"GUARD";locale_en

ORDER "NAME"
"NAME  (FACTION | UNIT | BUILDING | SHIP) name";syntax
"NAME";locale_en

ORDER "PASSWORD"
"PASSWORD password";syntax
"PASSWORD";locale_en

ORDER "RESHOW"
"RESHOW spell";syntax
"RESHOW";locale_en

ORDER "FIND"
"FIND f1";syntax
"FIND";locale_en

ORDER "BOARD"
"BOARD s1";syntax
"BOARD";locale_en

ORDER "ENTER"
"ENTER b1";syntax
"ENTER";locale_en

ORDER "LEAVE"
"LEAVE";syntax
"LEAVE";locale_en

ORDER "PROMOTE"
"PROMOTE u1";syntax
"PROMOTE";locale_en

ORDER "ATTACK"
"ATTACK (u1 | PEASANTS)";syntax
"ATTACK";locale_en

ORDER "DEMOLISH"
"DEMOLISH";syntax
"DEMOLISH";locale_en

ORDER "GIVE"
"GIVE u1 1 item";syntax
"GIVE";locale_en

ORDER "PAY"
"PAY u1 1";syntax
"PAY";locale_en

ORDER "SINK"
"SINK";syntax
"SINK";locale_en

ORDER "TRANSFER"
"TRANSFER (u1 | PEASANTS) 1";syntax
"TRANSFER";locale_en

ORDER "TAX"
"TAX";syntax
"TAX";locale_en

ORDER "RECRUIT"
"RECRUIT 1";syntax
"RECRUIT";locale_en

ORDER "QUIT"
"QUIT password";syntax
"QUIT";locale_en

ORDER "MOVE"
"MOVE (N | W | M | S | W | Y)";syntax
"MOVE";locale_en

ORDER "SAIL"
"SAIL  (N | W | M | S | W | Y)";syntax
"SAIL";locale_en

ORDER "BUILD"
"BUILD (BUILDING [b1]) | (SHIP s1) | (shiptype)";syntax
"BUILD";locale_en

ORDER "ENTERTAIN"
"ENTERTAIN";syntax
"ENTERTAIN";locale_en

ORDER "PRODUCE"
"PRODUCE item";syntax
"PRODUCE";locale_en

ORDER "RESEARCH"
"RESEARCH [1]";syntax
"RESEARCH";locale_en

ORDER "STUDY"
"STUDY skill";syntax
"STUDY";locale_en

ORDER "TEACH"
"TEACH u1+";syntax
"TEACH";locale_en

ORDER "WORK"
"WORK";syntax
"WORK";locale_en

ORDER "CAST"
"CAST spell";syntax
"CAST";locale_en

ORDER "NEW"
1;internal
"NEW";locale_en

ORDER "PEASANTS"
1;internal
"PEASANTS";locale_en

ORDER "SHIP"
1;internal
"SHIP";locale_en

ORDER "BUILDING"
1;internal
"BUILDING";locale_en

ORDER "UNIT"
1;internal
"UNIT";locale_en

ORDER "FACTION"
1;internal
"FACTION";locale_en


ORDER "NORTHWEST"
"Mir";locale_en
1;internal

ORDER "NORTHEAST"
"NORTH";locale_en
1;internal

ORDER "EAST"
"EAST";locale_en
1;internal

ORDER "SOUTHEAST"
"Ydd";locale_en
1;internal

ORDER "SOUTHWEST"
"SOUTH";locale_en
1;internal

ORDER "WEST"
"WEST";locale_en
1;internal

ORDER "NW"
"M";locale_en
"Mir";locale_en
1;internal

ORDER "NE"
"N";locale_en
"NORTH";locale_en
1;internal

ORDER "E"
"E";locale_en
"EAST";locale_en
1;internal

ORDER "SE"
"Y";locale_en
"Ydd";locale_en
1;internal

ORDER "SW"
"S";locale_en
"SOUTH";locale_en
1;internal

ORDER "W"
"W";locale_en
"WEST";locale_en
1;internal

ALLIANCECATEGORY "ALLES"
"ALL";name
26;bitmask

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
1;naturalorder

ITEMCATEGORY "front weapons"
"Front-Waffen";name
0;naturalorder
"weapons";parent

ITEMCATEGORY "distance weapons"
"Distanz-Waffen";name
1;naturalorder
"weapons";parent

ITEMCATEGORY "armour"
"Rüstungen";name
2;naturalorder

ITEMCATEGORY "resources"
"Ressourcen";name
3;naturalorder

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

SKILLCATEGORY "misc"
"Sonstiges";name
6;naturalorder


SKILL "Mining"
"Mining";name
"resource";category
"Bergbau";iconname

SKILL "Lumberjack"
"Lumberjack";name
"resource";category
"Holzfällen";iconname

SKILL "Quarrying"
"Quarrying";name
"resource";category
"Steinbau";iconname

SKILL "Horse Training"
"Horse Training";name
"resource";category
"Pferdedressur";iconname

SKILL "Weaponsmith"
"Weaponsmith";name
"build";category
"Waffenbau";iconname

SKILL "Armorer"
"Armorer";name
"build";category
"Ruestungsbau";iconname

SKILL "Building"
"Building";name
"build";category
"Burgenbau";iconname

SKILL "Shipbuilding"
"Shipbuilding";name
"build";category
"Schiffbau";iconname

SKILL "Entertainment"
"Entertainment";name
"silver";category
"Unterhaltung";iconname

SKILL "Stealth"
"Stealth";name
"misc";category
"Tarnung";iconname

SKILL "Observation"
"Observation";name
"misc";category
"Wahrnehmung";iconname

SKILL "Tactics"
"Tactics";name
"war";category
200;cost
"Taktik";iconname

SKILL "Riding"
"Riding";name
"misc";category
"Reiten";iconname

SKILL "Sword"
"Sword";name
"war";category
"Hiebwaffen";iconname

SKILL "Crossbow"
"Crossbow";name
"war";category
"Armbrustschiessen";iconname

SKILL "Longbow"
"Longbow";name
"war";category
"Bogenschiessen";iconname

SKILL "Magic"
"Magic";name
"magic";category
200;cost
"Magie";iconname

RACE "Menschen"
"Person";name
50;recruitmentcosts
10;weight
5;capacity

ITEM "Silber"
"silver";name
0;weight;
"silver";category

ITEM "wood"
"wood";name
1;weight;
"resources";category
"Lumberjack";makeskill
"Holz";iconname

ITEM "stone"
"stone";name
50;weight;
"resources";category
"Quarrying";makeskill
"Stein";iconname

ITEM "iron"
"iron";name
1;weight;
"resources";category
"Mining";makeskill
"Eisen";iconname

ITEM "horse"
"horse";name
1;weight;
"resources";category
"Horse Training";makeskill
1;ishorse
"Pferd";iconname

ITEM "sword"
"sword";name
1;weight;
"distance weapons";category
"Sword";useskill
"Weaponsmith";makeskill
1;makeskilllevel
"Schwert";iconname
RESOURCES
1;iron

ITEM "longbow"
"longbow";name
1;weight;
"distance weapons";category
"Longbow";useskill
"Weaponsmith";makeskill
1;makeskilllevel
"Bogen";iconname
RESOURCES
1;wood

ITEM "crossbow"
"crossbow";name
1;weight;
"weapons";category
"Crossbow";useskill
"Weaponsmith";makeskill
1;makeskilllevel
"Armbrust";iconname
RESOURCES
1;wood

ITEM "chain mail"
"chain mail";name
1;weight;
"armour";category
"Armorer";makeskill
"Kettenhemd";iconname
RESOURCES
1;iron

ITEM "plate mail"
"plate mail";name
1;weight;
"armour";category
"Armorer";makeskill
3;makeskilllevel
"Plattenpanzer";iconname
RESOURCES
1;iron




SHIPTYPE "Longboat"
"Longboat";name
100;size
1;level
1;range
200;capacity
0;captainlevel
0;sailorlevel
"langboot";iconname
RAWMATERIALS
1;wood

SHIPTYPE "Clipper"
"Clipper";name
200;size
1;level
1;range
800;capacity
0;captainlevel
0;sailorlevel
"schoner";iconname
RAWMATERIALS
1;wood

SHIPTYPE "Galleon"
"Galleon";name
300;size
1;level
1;range
1800;capacity
0;captainlevel
0;sailorlevel
"galeone";iconname
RAWMATERIALS
1;wood

CASTLETYPE "Building"
"Building";name
1;level
1;minsize
"Burg";iconname
RAWMATERIALS
1;stone


REGIONTYPE "mountain"
"mountain";name
1666;maxworkers
12;peasantWage
"true";isLand
"Berge";iconname

REGIONTYPE "plain"
"plain";name
6666;maxworkers
15;peasantWage
"true";isLand
"Ebene";iconname

REGIONTYPE "swamp"
"swamp";name
3333;maxworkers
12;peasantWage
"true";isLand
"Sumpf";iconname

REGIONTYPE "forest"
"forest";name
6666;maxworkers
12;peasantWage
"true";isLand
"Wald";iconname

REGIONTYPE "ocean"	
"ocean";name
0;maxworkers
"true";isOcean
"Ozean";iconname
