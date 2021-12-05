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