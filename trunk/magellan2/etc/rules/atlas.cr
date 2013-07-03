VERSION 66
RULES "$Id: atlas.cr 1720"

MAGELLAN
"magellan.library.gamebinding.e3a.AtlasSpecificStuff";class
"FACTION";orderFileStartingString

ORDER "FORM"
"FORM u1";syntax

ORDER "ACCEPT"
"ACCEPT f1";syntax

ORDER "ADDRESS"
"ADDRESS Address";syntax

ORDER "ADMIT"
"ADMIT f1";syntax

ORDER "ALLY"
"ALLY f1 01";syntax

ORDER "BEHIND"
"BEHIND 01";syntax

ORDER "COMBAT"
"COMBAT spell";syntax

ORDER "DISPLAY"
"DISPLAY (UNIT | BUILDING SHIP) string";syntax

ORDER "GUARD"
"GUARD 01";syntax

ORDER "NAME"
"NAME  (FACTION | UNIT | BUILDING | SHIP) name";syntax

ORDER "PASSWORD"
"PASSWORD password";syntax
ORDER "RESHOW"
"RESHOW spell";syntax
ORDER "FIND"
"FIND f1";syntax

ORDER "BOARD"
"BOARD s1";syntax

ORDER "ENTER"
"ENTER b1";syntax

ORDER "LEAVE"
"LEAVE";syntax

ORDER "PROMOTE"
"PROMOTE u1";syntax

ORDER "ATTACK"
"ATTACK (u1 | PEASANTS)";syntax

ORDER "DEMOLISH"
"DEMOLISH";syntax

ORDER "GIVE"
"GIVE u1 1 item";syntax

ORDER "PAY"
"PAY u1 1";syntax

ORDER "SINK"
"SINK";syntax

ORDER "TRANSFER"
"TRANSFER (u1 | PEASANTS) 1";syntax

ORDER "TAX"
"TAX";syntax

ORDER "RECRUIT"
"RECRUIT 1";syntax

ORDER "QUIT"
"QUIT password";syntax

ORDER "MOVE"
"MOVE (N | W | M | S | W | Y)";syntax

ORDER "SAIL"
"SAIL  (N | W | M | S | W | Y)";syntax

ORDER "BUILD"
"BUILD (BUILDING [b1]) | (SHIP [s1|type])";syntax

ORDER "ENTERTAIN"
"ENTERTAIN";syntax

ORDER "PRODUCE"
"PRODUCE item";syntax

ORDER "RESEARCH"
"RESEARCH [1]";syntax

ORDER "STUDY"
"STUDY skill";syntax

ORDER "TEACH"
"TEACH u1+";syntax

ORDER "WORK"
"WORK";syntax

ORDER "CAST"
"CAST spell";syntax

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

SKILL "Lumberjack"
"Lumberjack";name
"resource";category

SKILL "Quarrying"
"Quarrying";name
"resource";category

SKILL "Horse Training"
"Horse Training";name
"resource";category

SKILL "Weaponsmith"
"Weaponsmith";name
"build";category

SKILL "Armorer"
"Armorer";name
"build";category

SKILL "Building"
"Building";name
"build";category

SKILL "Shipbuilding"
"Shipbuilding";name
"build";category

SKILL "Entertainment"
"Entertainment";name
"silver";category

SKILL "Stealth"
"Stealth";name
"misc";category

SKILL "Observation"
"Observation";name
"misc";category

SKILL "Tactics"
"Tactics";name
"war";category
200;cost

SKILL "Riding"
"Riding";name
"misc";category

SKILL "Sword"
"Sword";name
"war";category

SKILL "Crossbow"
"Crossbow";name
"war";category

SKILL "Longbow"
"Longbow";name
"war";category

SKILL "Magic"
"Magic";name
"magic";category
200;cost

RACE "Menschen"
"Menschen";name
50;recruitmentcosts
10;weight
5;capacity


ITEM "silver"
"silver";name
1;weight;
"silver";category

ITEM "wood"
"wood";name
1;weight;
"resources";category
"Lumberjack";makeskill

ITEM "stone"
"stone";name
50;weight;
"resources";category
"Quarrying";makeskill

ITEM "iron"
"iron";name
1;weight;
"resources";category
"Mining";makeskill

ITEM "horse"
"horse";name
1;weight;
"resources";category
"Horse training"; makeskill
1;ishorse

ITEM "sword"
"sword";name
1;weight;
"distance weapons";category
"Sword";useskill
1;makeskilllevel
RESOURCES
1;iron

ITEM "longbow"
"longbow";name
1;weight;
"distance weapons";category
"Longbow";useskill
1;makeskilllevel
RESOURCES
1;wood

ITEM "crossbow"
"crossbow";name
1;weight;
"weapons";category
"Crossbow";useskill
1;makeskilllevel
RESOURCES
1;wood

ITEM "chain mail"
"chain mail";name
1;weight;
"armour";category
"Armorer";makeskill
RESOURCES
3;Eisen

ITEM "plate mail"
"plate mail";name
1;weight;
"armour";category
"Armorer";makeskill
3;makeskilllevel
RESOURCES
1;Eisen




SHIPTYPE "Longboat"
"Longboat";name
100;size
1;level
1;range
200;capacity
0;captainlevel
0;sailorlevel
RAWMATERIALS
1;Holz

SHIPTYPE "Clipper"
"Clipper";name
200;size
1;level
1;range
800;capacity
0;captainlevel
0;sailorlevel
RAWMATERIALS
1;Holz

SHIPTYPE "Galleon"
"Galleon";name
300;size
1;level
1;range
1800;capacity
0;captainlevel
0;sailorlevel
RAWMATERIALS
1;Holz

CASTLETYPE "Building"
"Building";name
1;level
1;minsize
RAWMATERIALS
1;stone


REGIONTYPE "mountain"
"mountain";name
1666;maxworkers
"true";isLand

REGIONTYPE "plain"
"plain";name
6666;maxworkers
"true";isLand

REGIONTYPE "swamp"
"swamp";name
3333;maxworkers
"true";isLand

REGIONTYPE "forest"
"forest";name
6666;maxworkers
"true";isLand

REGIONTYPE "ocean"	
"ocean";name
0;maxworkers
"true";isOcean
