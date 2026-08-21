Flan 1.12.8
================
- Update to 26.2
  - Also add sulfur cube permission for hitting sulfur cubes with blocks inside
- Fix bluemap crash
- Fix projectile test crash
- Add journeymap integration
- Fix wrong enderpearl check [@ElectricSteve](https://github.com/electricsteve)
- Fix crash when using invalid id in allow screen [@SPGesus](https://github.com/SPGesus)
- Fix spear dealing knockback (so e.g. breaking item frames) in claims

Flan 1.12.7
================
- Fix claim display fake blocks position [@Wesley1808](https://github.com/Wesley1808)
- Update common protection api integration [@Wesley1808](https://github.com/Wesley1808)
- Add es_es lang [@cferreras](https://github.com/cferreras)
- Fix dimension-fix path being wrong.
  - For all people that already ran with 1.12.6 you need to move the `claims` folder under `dimensions/...`  
    to `data` so e.g. `dimensions/minecraft/overworld/claims` -> `dimensions/minecraft/overworld/data/claims`

Flan 1.12.6
================
- Update to 26.1
~~- Rewrite nbt config for claim and inspection tool to respect all components now~~
~~- Allow configuring map marker styles (border and fill color)~~
~~- Fix wrong claim boundaries on edges~~
~~- Internally rewrite currency integrations.~~
~~  - Add beconomy integration~~
~~- Update component removal logic for gui items. Should fix some resource pack issues~~
~~- Add tag cycling for guis (applicable to ignore lists)~~
~~- Add toggle for black/whitelist for ignore lists~~

Flan 1.12.5
================
- Update to 1.21.11  
- Money & Signs shops integration disabled till it updates    
~~- Update to 1.21.10 [@Wesley1808](https://github.com/Wesley1808)~~
~~- Fix fake blocks: Don't load chunks that aren't already loaded, or out of range [@JayPeaSize](https://github.com/JayPeaSize)~~
~~- Update bluemap integration~~
  ~~- Updates map on config changes~~
  ~~- Fix claims added to map despite config disabled~~

Flan 1.12.4
================
- Update vehicle pass check, fixes boat issues
- Remove pt_br translation as the file using older format
- Separate projectile handling from current system
  - Add `flan:projectile_block_interact` and `flan:projectile_entity_interact` to interaction overrides
  - `flan:projectile_entity_interact` takes precedence and only if this is not defined it checks `flan:projectile_block_interact`
  - Add `ignoredProjectileTypes` to config
- Rename `flan:mob_spawn` permission to `flan:disable_monster_spawn`
- Rename `flan:animal_spawn` permission to `flan:disable_mob_spawn`
- Fix some permission flags not checked for globalDefaultPerms config within claims causing e.g. flight permission being ignored

Flan 1.12.3
================
- Fix drop handler error when on client

Flan 1.12.2
================
- Projectile now vanish with no permission hits instead of phasing through blocks
- Fix anvil text screen item not visible
- Add pt-br lang
- Add another check to determine fake player. Fixes universal shop not accessible
- Rewrite fabric drop handling to match neos event
- Fix give command giving twice the amount if drop is not allowed

Flan 1.12.1
================
- Add `vehicle_pass` permission:
  - Blocks vehicles (boats, minecarts etc.) from crossing claim border
- Add config to disable webmap integrations
- Add `de_de` translation
- Add configurable potion duration.
  - As such format is now `effect;duration;amplifier` or `effect;amplifier`
  - Duration is in ticks
- Commands now respect the position of the executor
- Update language lib
- Fix subclaims not synced with parent height
- Fix player in groups always displayed as the owner
- Fix global config ignored
- Fix flight not properly removed on neoforge
- Fix no hunger for claim owner 
  - partially caused by global config ignored but now also requires explicit set to true to affect owner
- Fix allow list menu command checking wrong permission
- Fix list adminclaims only listing claims in current dimension
- Loosen constraint on commands making them respect different positions through /execute
- Fix command info permission using wrong translation key
- Remove a bundled unused dependency
- Remove an expected log with offline players

Flan 1.12.0
================
- Fix datapack directory not updated. Now have a prefix `flan`
  - `claim_permissions` -> `flan/claim_permission` (plural also removed)
  - `claim_interactions_override` -> `flan/claim_interactions_override`
  - Old datapacks also require update!
- Update confirm command info

Flan 1.11.15
================
- Update to 1.21.8
- Fix default groups config not working and always being empty
- Move config permission verification after datapack load
- Fix true/false translation key in command

Flan 1.11.14
================
- Update to 1.21.8
~~- Fix config global permission issue of outside being treated as in claim~~

Flan 1.11.13
================
- Fix level permission not defaulting to default value.
  - Fixes flight always on if not defined in config
- Reset player motion if player tries to fly with blocked flight

Flan 1.11.12
================
- Bump language lib:
  - Fixes issues with default translation being overriden causing problems
- Fix damage check reversed so entities were always damaged
- Increased priorities of various events which should help with mod compat (e.g. carry on)
- Remove the no permission message that showed up if e.g. block interact is allowed but items were not
- Permission now have a `require_explicit` field:
  - If true the claims permission needs to be set to true explicitly to apply to admin/owner
  - By default only `may_flight` has this so the permission needs to be true for flying
- Fix empty hand bypassing block interaction protection
- Update claim display handling fixing dangling fake blocks
- Extend allow lists with item drop/pickup list so claim users can decide what items are droppable/pickupable
- Move prevent/allow flight and hunger handling out of claims so now it respects global perms in config

Flan 1.11.11
================
- Update to 1.21.5!
- Gui items now have all components removed for cleaner tooltips
- Effect string can now use `-` instead of just `;` as delimiter
- Update yaml lib

Flan 1.11.10
================
- Hotfix: Fix permission gui page not flipping

Flan 1.11.9
================
- Fix indirect non living damage which for vanilla was really only with player tnt on itemframes etc.
- Split the wrench override into two files as non present items also causes the tag to error
- Rework gui backend and add pagination to all missing ones
- Replace double typing command system with a confirmation system
- Fix booleans in messages being 0 or 1 instead of translated

Flan 1.11.7.b
================
- Fabric: Fix PlayerAbilityLib integration

Flan 1.11.7
================
- Fix buy/sell handler now requiring server to parse and thus not working
- Fix datagen missing and thus permissions not working 
- Fix codecs requiring server access and thus some things permission not being registered 
- Added `may_flight` permission which if enabled allows creative flight in the claim. Disabled by default
- Fix double clicking in gui resulting in triple click
- Update translation lib, fixing some translation issues
- Fix swapped buy/sell permission node
- Add windcharge permission
- Add claim border crossing event
- Update es_mx translation

Flan 1.11.6
================
- Update buy/sell handler. Old configs will be auto converted. Check the wiki for new format
  - https://github.com/Flemmli97/Flan/wiki/Config#buysell-handler

Flan 1.11.5
================
- Claim permission fields now use snake_case:
  - Old permission json will still work but throw a warning
  - Removed description field as thats done via a language file
    - name key is `flan.permission.<id>`
    - description key is `flan.permission.<id>.desc`. see the mods language file for examples
- Removed `use3d` command. Its now merged with `switchMode`
- Show mode when holding tool
- Fix 3d subclaims not possible
- Update command formatting and translations
- Fix particle display not showing claim edges
- Claim tools now show nearby claims instead of only the one the player is currently in

Flan 1.11.4
================
- Fix claim downwards extension. Should fix disappearing claim issue
- Remove save path caching (notable in single player)
- Fix translatable component args

Flan 1.11.3
================
- Fix string input screen for real now
- Fix unable to remove non existing players
- Fix player remove command result error

Flan 1.11.2
================
- Fix string input screen not working (group perm screen etc.)
- Add option to disable claim depth. Set claimdepth to -1 in config
- Fix neoforge crash

Flan 1.11.1
================
- Fix player nullpointer with translation
- Fix some wrong formatting in translations
- Fix nullpointer with claimdisplay

Flan 1.11.0
================
- Reworked how language/translations are handled
  - Will now respect the players language on the client. At least if the server also has the language
- Add patrol and phantom to mob spawn check on fabric
- Added 3d claims (both main and subclaims)! woo
  - 3d and 2d claims cannot overlap regardless what height the 3d claim would be
  - Switch to 3d claim mode via `/flan use3d`
  - 3d claim height to NOT use up claimblocks
  - 3d claims will NOT auto extend down like 2d claims
  - All current claims are 2d and untouched by this change
- Added pointer indicator when holding claim tool making it more clear where you are aiming
- Moved custom permission config to a datapack system. Allows mods to change this without depending on flan
  - Affects following config options: `customItemPermission`, `customBlockPermission`, `customEntityPermission`, `leftClickBlockPermission`
  - To use define a file under `data/claim_interactions_override`. 
  - Define a mapping of entry - permission. Use `#`-prefix for tags. See the builtin overrides for examples
  - Supported types: 
    - `flan:block_left_click`: Left clicking blocks
    - `flan:block_interact`: Interacting with a block (not just right click but collision etc. too) 
    - `flan:item_use`, `flan:entity_attack`, `flan:entity_interact`
  - Non tag entries take priority over tags 
- Fix BlueMap claim indicator bounds

Flan 1.10.10
================
- Fix nullpointer with claim name and admin claims

Flan 1.10.9
================
- Move config init to earlier point. Fixing datapack fails when using flan commands in them
- Add required claimblocks to chat feedback when claiming and failing
- Fix usernames in default claim names
- Fix nullpointer with dynmap and worldedit (or "fake worlds" stuff)
- Add permission to prevent hurting named entities
- Fix arrows crashing the game
- Add decorated pots to projectile permission

Flan 1.10.8
================
- Fix claim message spam when claiming is disabled
- Update zh_tw
- Fix nullpointer with claim check api

Flan 1.10.7
================
- Fix recursive error with claim messages
- Added translations for:
  - Mexican Spanish
  - Italian
  - Korean
  - Traditional chinese
- Add config to disable claiming over vanilla spawn protection (disabled by default)
- Add config for claim cooldown. When claiming needs to wait x time to create another claim. (0 by default)
- Renamed some commands to a shorter version:
  - addClaim -> add
  - claimInfo -> info
  - adminMode -> bypass
  - buyBlocks -> buy
  - sellBlocks -> sell
- Change buy item config to allow multiple items
  - See the wiki for an example
  - The item with the highest value will be consumed first
- Add permission node required for admin mode editing admin claims
  - `flan.command.bypass.admin.mode`
  - Also admin mode permission node has been renamed to `flan.command.bypass.claim` to reflect command change
- Address neoforge breaking changes

Flan 1.10.6
================
- Fix admin claim with enter/leave text crashing

Flan 1.10.5
================
- Update to 1.21
- Removed frostwalker permission due to changes to the   
  enchantment system making it impossible to determine from what enchant modifications now come from.  
  It now uses the place permission.  
~~- Cache chunks for claim display and don't load unloaded chunks which should solve some performance issue  
  with large claims etc.~~  
~~- Fix placeholders not working for enter/leave messages~~  
~~- Add `entity attack` and `entity use` ignore list for claims similar to existing item, block ignore lists  
  Entitites in that list can be interacted/attacked in that claim regardless of permission~~  
~~- Add command for the ignore list:  
  `/ignoreList (add | remove) <type> <value>`~~

Flan 1.10.4
================
- Fix permission command

Flan 1.10.3
================
- Fix group screen
- Update neoforge and fix neoforge events

Flan 1.10.2
================
- Fabric: Fix a mixin crash in prod

Flan 1.10.1
================
- Neoforge: update neoforge version addressing TickEvent change

Flan 1.10.0
================
- Port to 1.20.5
- Fix wandering trader not being in trade permission
- Permissions are now done via datapack allowing adding more permissions easier.
  This can be used in conjunction with e.g. `itemPermission` config.  
  This is a **breaking change** for mods that depend on flan!!!  
  Notably `PermissionRegistry` has been renamed to `BuiltinPermission`
  which now contains ids for all default provided permissions. All permission checks now take a simple `ResourceLocation`  
  For users/server owners this should not cause problems (hopefully)

Flan 1.9.1
================
- Ignore missing registry entries for allowed items/block list

Flan 1.9.0
================
- Add command to modify base claim blocks:
  - `/flan giveClaimBlocks base`
- Add claim dependent item and block list:
  - Custom item list player can use in a claim
  - Custom block break list player can use in a claim
  - Custom block interact list player can use
  - Adding `#` defines it as the entries as a tag
- Add create minecart contraption permission:
  - If false will prevent contraptions from crossing into the claim
  - If a contraption goes out of the claim it will not be able to come back in!
- Fix fake player command requiring op
- Fix duplicating items using create wrench when on claim border
- Fix can take items out of storage drawer in creative mode
- Fix mobspawn blocking not working on forge

Flan 1.8.11
================
- Ignore creative player for start breaking event as they insta break anyway
- Change claim display from particle to blocks.  
  Can still be enabled in config via `particleDisplay`
- Impactor economy integration

Flan 1.8.10
================
- Fix some issues with breaking progress prevention

Flan 1.8.9
================
- Fix taterzen npc id being wrong in config
- Fix log spam with mismatching position when preventing block breaking
- Improve block breaking prevention:
  - Now stops mining at all if breaking is not possible
- Disable the claim tool held message when player can't claim anyway (blacklisted world, max claimblock = 0)
- Make sign commands always execute even without permission. 
  They need commands to setup anyway so servers purposefully put them there in the first place
- Disable double chests forming if player can't open the other chest
- Add new permission: INTERACTSIGN:
  If true players can edit signs, e.g. via dyeing them etc.

Flan 1.8.8
================
- Fix crash with modded flying and players login out in claims with no flying permission
- Make map claim markers 3d
- Add nbt support for claiming items. Any nbt key in the config has to match the one on the item
- Add command to create claims without players:  
  /flan addClaim x y z x2 y2 z2 <dimension> <player|+Admin>
- Update entity interact events for updated fabric api which fixes e.g. villager trading always disabled
- Fix crash with ftb ranks
- Add archeology permission. Used for brushing blocks

Flan 1.8.7
================
- Fix entityPermission wrongly parsed causing extreme large files on reload

Flan 1.8.6
================
- Port 1.20.1  
~~- Fix autoStructureClaim config not working~~

Flan 1.8.5
================
- Fabric: common protection api support
- Add default chinese lang file
- Taterzen npc now use TRADE permission
- Make spawn prevention only apply to natural spawns and not other like spawners
- Prevent entering/placing boats into claims if BOAT permission is disabled
- Add error message when player is in subclaim mode but trying to delete main claim
- Use forge player place events in addition to current checks.
- Add bonus claim block permission node: `flan.claim.blocks.bonus`
- Add mappings config `leftClickBlockIgnore` for left clicking blocks.  
  This is useful when blocks have custom behaviour when left clicked e.g. storage drawers
- Add universal shops to default ignored blocks
- Add config `autoClaimStructures` to auto claim generated structures as admin claims
- Rename `admin` option in tp command to `global`
- Fix thrown item triggering sculks when sculk permission is off
- Add config `customEntityPermission` that allows to change permission for an entity when interacted (right click). just like block and items

Flan 1.8.4.3
================
- Update forge version to 45.0.59

Flan 1.8.4.2
================
- Fabric: Fix mixin crash

Flan 1.8.4.1
================
- Update to 1.19.4
<i>
    - Fabric: Add harvest with ease compat
    </i>

Flan 1.8.4
================
- Fix /flan group ignoring subclaim mode
- Add lang key support for some things (like group name). Makes it possible to change their color
- Prevent dragon egg interaction near claims without permissions
- Fix invalid player data when editing offline player data
- Make owned allays not able to pickup locked items or if claim disables pickup items in general
- Add special list for handling fake players in claim. 
  Instead of adding fakeplayers to permission groups you can add fake players to this list instead.
  Command is: /flan fakePlayer <add|remove> uuid
  Note: Some mods dont have a fixed uuid for fake players meaning the uuid changes each server restart.
        This means you need to reconfig it every restart for those mods.
        Report it to them to have them make it persistent.
- Also add notification when fakeplayers cannot perform their action in a claim

Flan 1.8.3
================
- Fix claim display keep getting added for same claim causing lots of particles

Flan 1.8.2
================
- Some QOL changes
- Add claimblock cap permission "flan.claim.blocks.cap":  
  Claimblocks will still increase but are not usable until cap is increased or lifted
- Add support for bluemap mod
- Add config to display claim enter/leave messages as action bars instead of titles
- Fix record items not usable even with jukebox permission true
- Admin mode can transfer claims regardless of owner
- Add support for various other claiming mods: goml reserved, ftbchunks and also minecolonies
  You will not be able to claim over areas claimed by those mods.
  Can be disabled in the config
- Allow subclaim title to be set via command

Flan 1.8.1.1
================
- Check for version of fabric permission api

Flan 1.8.1
================
- Relocating shaded libs. Should solve some lib problems.  
  Note: If a mod crashed before cause of this also report the problem to the other mod  
  since this only occurs more than one mod messed it up
- Fabric: Permission api v2 support.  
  Things like "flan.claim.blocks.max" nodes are now usable
- Ignoring hostile mobs during interaction check

Flan 1.8.0
================
- Update to forge 41.1.0
- New command #167:  
  /flan expand <x>
  tries expanding the claim by x blocks into the direction the player is looking
- Display remaining claim blocks at various messages #170
- Increase night vision potion in claims so it stops flickering
- Diamon economy integration
- New permissions:  
  PLAYERMOBSPAWN: Permission for affected players to spawn mobs with interactions. E.g. wardens, or endermites with enderpearls  
  SCULK: Permission for sculk sensors. Shriekers which spawn warden are handled under PLAYERMOBSPAWN

Flan 1.7.10
================
- Check for keep inventory when deciding if drops should be locked or not  
  Stops unlock cmd message when keep inventory is on
- Fix addClaim cmd bypassing world blacklist
- Make admin mode ignore world blacklist
- Make claim info message look nicer
- Update to 1.19

Flan 1.7.9
================
- Move pickup check to a better place. Should fix some mod compat issues

Flan 1.7.8
================
- Add dynmap support for claims

Flan 1.7.7
================
- Prevent moss/grassblock/nylium bonemealing if too close to a claim without permission
- Make subclaims inherit some data of the parent claim upon creation.  
  E.g. player groups or set group permissions
- Better description when editing enter/leave text

Flan 1.7.6
================
- Fix delete command using wrong permission node
- Fabric: Support for EightsEconomyP
- Add waystones and yigd graves to default ignored block list

Flan 1.7.5
================
- Fix a typo in the default lang file
- Add CLAIMMESSAGE permission for specifically editing the  
  enter/leave message of a claim

Flan 1.7.4
================
- 1.18.2

Flan 1.7.3
================
- Internal changes
- Forge: Fix wither ignoring claims

Flan 1.7.2
================
- Internal changes
- Fabric: Fix Clumps incompability with xp permission

Flan 1.7.1
======================
- Fix non player explosions not doing damage to hostile/players
  Most noticable when creeper do nothing in claims
- Fix command feedback for enter/leave message
- Forge: Fix pistons across claimborder not working

Flan 1.7.0
======================
- FAKEPLAYER permission for fake players
- Ability to remove claim enter/leave messages.
  Simply use "$empty" instead to remove a message
- Rewritten the language system so its now possible to specify
  a language to use if a translation exist.  
  PR for translations go under "common/src/main/resources/data/flan/lang"
- defaultClaimName config:  
  If not an empty string sets a claims name on creation to this  
  2 arguments are passed to this string:  
  The claims owner, and the amount of claims the player has  
  Example: "%1$s's Claim #%2$s" would then e.g. result in the name "Player's Claim #1"  
- defaultEnterMessage and defaultLeaveMessage:  
  Automatically sets the enter/leave message to this  
  The claim name is passed on to this so "Entering %s" -> "Entering <Claim>"
- Rewritten buy/sell system.  
  Its now possible to specify money, items or xp points as a "currency" value  
  For more info see the https://github.com/Flemmli97/Flan/wiki/Config#buysell-handler
- maxBuyBlocks config:
  Specify the max amount of claim blocks a player can buy.  
  giveClaimBlocks command bypasses this still. -1 = no limit

Flan 1.6.9
======================
- Fix claiming tools bypassing permissions *cough*
- Also fix claiming tool still doing their normal actions (aka hoeing the ground)

Flan 1.6.8
======================
- Rewrite block interaction check again which should fix some issues with it

Flan 1.6.7
======================
- Yeet players off riding entities when teleporting
- Some mod integration fixes

Flan 1.6.6
======================
- Fix blockentity interaction not working
- Fix globalDefaultPerms defined in config not applying to claim on creation

Flan 1.6.5
======================
- Fix claims below 0 not working  
- Only kick players out of subclaim if the mainclaim allows them to stay
- New config "customItemPermission" and "customBlockPermission"  
  allows to specify a mapping of item/block to permission  
  example is endcrystals are mapped to the ENDCRYSTALPLACE permission internally  
  Syntax is <item/block>-\<permission>  
  Use @ to indicate a tag
- Moved some interaction checks to a different location so more mods should be covered by default

Flan 1.6.4
======================
- Add back all integrations with other mods  
  This assumes no breaking changes between the 1.17 version of those mods and 1.18
  FTBRanks integration for forge still missing

Flan 1.6.3
======================
- Port to 1.18

Flan 1.6.2
======================
- Fix tp sometimes tp into walls  
- Add bannedDeletionTime config:  
  After x amount of days since the ban the claims will be deleted.  
  Only for permanent bans (so temp bans are not affected).  
  Temp bans are still affected by the inactivity time though.  
  Default 30 days (same as inactivity days)  
- Add deletePlayerFile config:  
  Previously for inactive players only the claims where deleted.  
  If this is set to true their player data will also be deleted.  
  Default false  
- Add universal graves mods grave to the default ignored blocks  
- Another performance improvement  
- Add new config "offlineProtectActivation" to only protect claims when the player is offline.  
  Disabled by default  

Flan 1.6.1
======================
- Improve performance
- Support for Money and Sign Shops currency (forge)
- Fix unusable admin cmd with FTBRanks if its not defined in Ranks
- Change lockItems config to a permission
  Global default is ALLTRUE so its still the same as setting it to true in the past config

Flan 1.6.0
======================
- Separate some stuff to make it more api like and to make it into an api jar.
  Breaks compat with any potential mods using the previous system.
- Add ignoredEntities config to always ignore certain entities.
  Like grave mods that add the graves as an entity (e.g. corpse mod).
  Add the corpse entity from that mod to default list
- Add modid support for ignoredBlocks and ignoredEntities.
  Use a modid to affect all things from that mod
- Add FTBRanks support. Permission Nodes are the same

Flan 1.5.4
======================
- Safe read player data so it doesnt crash when the file is wrong
- Fix typo in lang file making listAdminClaims error.
  For already existing lang files replace %1 with %1$s for "listAdminClaims"

Flan 1.5.3
======================
- Add wiki link to help command
- Add shortcut /flan ? displaying the help command
- Make server gui inventory unmodifiable by vanilla methods.
  This inturn makes sure other mods cant take or put items into it
- Fix inspection tool and claiming tool bypassing permission
- Add LIGHTNING permission:
  Decides wether a lightning can set blocks on fire and affect non hostile mobs it hits
- Add scoreboard criteria for tracking various things:
  flan:total_claimblocks: Tracks the total claim blocks of players (normal and additional claimblocks)
  flan:used_claimblocks: Tracks the used claimblocks of players
  flan:free_claimblocks: Tracks the amount a player can use to claim
  flan:claim_number: Tracks the number of claims a player has in total
- Add option to only display part of a claims info with claimInfo

Flan 1.5.2
======================
- Add left click blocks check cause some blocks react to that (dragon egg)
- Fix out of memory error when updating from 1.4.x to 1.5+
- Add INTERACTBLOCK permission as a generic permission for block interaction.
  All blocks not covered by other permissions fall under this.
- Fix lilypad placing ignore permission
- Add FROSTWALKER permission

Flan 1.5.1
======================
- Invert ANIMALSPAWN in global perms if its not defined. fix animals not spawning outside claims
- Fix mobs ignoring permission and not spawning in claims

Flan 1.5.0
======================
- Fix some items checking for wrong position and thus are able to interact in claims
- Fix enderpearl phasing through blocks
- Add claim homes set via /flan setHome in a claim to the players position.
  The claim permission to allow others do that too is EDITCLAIM
- Add /flan teleport to tp to a claims home.
  Default global value is ALLFALSE so disabled.
- Add NOHUNGER permission: Disables hunger in claims
  Default global value is ALLFALSE so disabled.
- Fix resizing claims of other players not using their claim blocks
- Add ability to add potion effects to claims.
  That claim will then apply that effect to any player inside the claim
  Specify potions with <id;amplifier>
- Add EDITPOTIONS permission for that. There are no limits to potion effects so
  this should be admin reserved only.
  Default global value is ALLFALSE so disabled for that reason
- Add claim deletion for inactive players. Added config for that are:
  inactivityTimeDays: How long in days a player has to be offline for it to check the player
  inactivityBlocksMax: How much claimblocks the player can have at max for it to delete the claims

Flan 1.4.2
======================
- Fix a ticking player crash
- Fix anvil gui not updating client xp use
- Add ability to name claims.
  Names have to be unique for each player and claim.
  No additional feature for that (yet)
- Add blockEntityTagIgnore config
  Blockentities are checked against the strings here and if their tag
  matches one they will be ignored for a permission check.
  Useful for blockentities that should only be accessible under certain circumstances.
  Default values: ["IsDeathChest"] for vanilla death chest mod (only for version 2.0.0+)
- Add entityTagIgnore config same as blockEntityTagIgnore but it will only
  check against scoreboard tags. Default "graves.marker" is for vanilla tweaks graves.
  Keep in mind that the items dropped by graves are not player bound so if a claim disables
  item pickups you will not be able to pick up the items

Flan 1.4.1
======================
- Change globalDefaultPerms to use a version where you can specify if its modifiable or not
  Valid values are:
  ALLTRUE: Permission is true everywhere and players are unable to change it in claims
  ALLFALSE: Permission is false everywhere and players are unable to change it in claims
  TRUE: Permission is true in non claimed areas
  FALSE: Permission is false in non claimed areas
  Old configs will get auto converted: false -> ALLFALSE and true -> ALLTRUE
  Basically now a worldguard version
- Add locking items when the player dies so other players cant pick it up.
  Use /flan unlockItems to allow it.
- /flan help finished: command are clickable now and give additional info on the commands
- Log the file path when /flan readGriefPrevention can't find the files

Flan 1.4.0
======================
- Add FLIGHT permission. Prevents non creative flight in claims
- Add CANSTAY permission. Makes players unable to enter your claim
- Remove the mobspawn config.
- Add support for wildcard worlds in globalDefaultPerms. Use "*" instead of <dimension key>
  MOBSPAWN added to globalDefaultPerms with value false
  FLIGHT added to globalDefaultPerms with value true
- Add /flan trapped command. Using it teleports you out of a claim you don't own after 5 seconds

Flan 1.3.3
======================
- Some blocks permission changed from PROJECTILES to OPENCONTAINER on direct interaction
  Affected blocks: campfire, tnt, (chorus fruit), bells
  Direct interaction as in right clicking the block. For actual projectile hits this remains unchanged
- Fix several entities not protected on indirect non projectile player attacks (e.g. through player ignited tnt)-
  More noticable with other mods #52
- Add gunpowder currency support.
  Enables the purchase and selling of claim blocks. Price configurable. Disabled by default
  Only selling and purchase of additional claim blocks possible (not the ones you get with time).

Flan 1.3.2
======================
- Change gui item text to non italic (there are some very picky people)
- Add enderman grief permission
- Add snowman snowlayers permission
- Truly fix personal groups saving issues ._. (pls...)

Flan 1.3.1
======================
- Fix the nullpointer when creating claims. derp
- Fix personal groups not reading from file. derp x2
- Fix items in gui not updated clientside on shift click

Flan 1.3.0
======================
- Fix inventory desync when using the naming screen
- Add /flan help command. For now it only displays all available commands
- Add default groups. Set via the config file in defaultGroups.
  Every claim created will have those groups by default to make group creation easier
  and without having to make the same group for multiple claims again and again.
- Add personal groups. Set either via the command /flan permissions personal ...
  or via the gui /flan personalGroups.
  Personal groups are just like default groups except player bound. If a player has any
  personal groups that will be used instead of the global default group. That way the player
  can also specify what groups a claim should have upon creation.
- Localized various gui strings

Flan 1.2.6
======================
- Fix giveClaimBlocks not working for players that never joined the server
- Add chorus fruit eating permission
- Fix wither permission not toggable

Flan 1.2.5
======================
- Fix inventory desync after closing gui
- Sync itemstack when failing to place blocks
- Fix various thrown entitys to not abide by claim protection
- Add 2 new permissions:
  Item drop permission: If set to false prevents items being dropped (players only)
  Item pickup permission: If set to false prevents items from being picked up. Player thrown items
  (Death included) gets a special tag so that the one who threw them can always pick it up.
- Change some permissions to default true: Enderchest, Enchanting table, Item drop, Item pickup

Flan 1.2.4
======================
- Fix crash in void worlds #39
- Fix permission autocomplete #43
- Fix mod icon #42
- Check if the new owner has enough claim blocks when using transferClaim. Bypassed with admin mode

Flan 1.2.3
======================
- Fabric Permission API support for commands and claim creation. See wiki for permission nodes
- Add an ignore list for blocks to be ignored (For mods with gravestones etc.)
- Add a permission check event for mods
- Fix piercing arrows locking up the game in claims

Flan 1.2.2
======================
- Increase particle size #25
- Fix thrown items activating pressure plates #30
- Add option to have multiple lines for permission description in lang file #24
- Add enderchest, enchantment table use, piston and water across border permission #29
- Fix player claim data being reset upon death #26

Flan 1.2.1
======================
- Fix wrong claim size calculation #19
- Fix blacklisted worlds not saving #21
- Fix group command not able to edit some groups with illegal characters #20
- Fix double message for claim inspection when targeting a block
- Add toggable mobspawning for claims. Needs allowMobSpawnToggle to be true in the config

Flan 1.2.0
======================
- Claim display stays as long as you are holding either the inspection or the claiming tool
- Fix things getting logged despite enableLogs set to false
- Change the default time to get a claim block to 600 ticks instead of 1200
- Change Permission to a registry instead of hardcoded enums

Flan 1.1.5
======================
- Make tile entitys always at least be in the open container flag.
  Making them always be protected. This can be turned off to only include inventory type tile entities.
  #14

Flan 1.1.4
======================
- Directly check player class for item usage. So fake players get ignored.

Flan 1.1.3
======================
- Ignore item usage with non player (e.g. with modded machines). Fix #12
- Tell the player the amount of claimblocks etc. when switching to the claiming tool and creating/resizing claims
- Fix resizing claims not taking into account the minClaimSize value

Flan 1.1.2
======================
- Fix reload command being not restricted to admins

Flan 1.1.1
======================
- Fix a wrong check regarding block entitys that affected modded container blocks to not be protected

Flan 1.1.0
======================
- Add global per world perms (for more info visit https://github.com/Flemmli97/Flan/wiki/Config)
- Fail safe for config reading just in case the config contains "bad values"
- Some more logs

Flan 1.0.9
======================
- Add some logging stuff. Mostly with read/saving things
- Add option to disable the need for claim blocks by setting maxClaimBlocks to -1 in the configs

Flan 1.0.8
======================
- Changed using wrong tupel

Flan 1.0.7
======================
- Enable tamed pet interaction in claims for the owner

Flan 1.0.6
======================
- Update Fabric API to use the new PlayerBlockBreakEvent
  Should fix issues with other mods bypassing claim protection
  (If the mods dont directly break the blocks)
- Fix the lang configs being overwritten constantly
- Add description + translation for all permissions (used in the permission menu)
- Fix claim overlapping on edges not being detected
- Fix subclaims showing wrong display on conflict

Flan 1.0.5
======================
- Moved the config files to the run/server root folder. (The place where all other mods configs are too)

Flan 1.0.4
======================
- BisUmTo: Add addClaim command to create claims via commands
- Add transferClaim command to transfer owner of a claim to another player
- Add editPermission command to edit claim permissions via commands

Flan 1.0.3
======================
- Add permission to toggle firespreading in claims
- PvP, Explosion, Wither and Firespread are now global permission only
- Finished reading griefprevention data (previously set permissions were not read)
- /flan list now has ability to list claims of other players (requires op)
- Op level for op commands is now configurable

Flan 1.0.2
======================
- Fix Nullpointer with admin claims
- Removed unneccessary and wrong confirmation for admin single claim deletion
- Fix block placing sometimes using the wrong position

Flan 1.0.1
======================
- Fix data being deleted when there was a saving error
- Fix reading GriefPrevention data
- Add auto extend claims downwards
- Prevent double processing when right clicking a block with a usable item
- Add Raid permission flag
- Improved visual for claims dealing with liquids