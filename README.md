# Flan  
[![](http://cf.way2muchnoise.eu/full_404578_Fabric_%20.svg)![](http://cf.way2muchnoise.eu/versions/404578.svg)](https://www.curseforge.com/minecraft/mc-mods/flan)  
[![](http://cf.way2muchnoise.eu/full_493246_Forge_%20.svg)![](http://cf.way2muchnoise.eu/versions/493246.svg)](https://www.curseforge.com/minecraft/mc-mods/flan-forge)  
[![](https://img.shields.io/modrinth/dt/Si383TIH?logo=modrinth&label=Modrinth)![](https://img.shields.io/modrinth/game-versions/Si383TIH?logo=modrinth&label=Latest%20for)](https://modrinth.com/mod/flan)  
[![Discord](https://img.shields.io/discord/790631506313478155?color=0a48c4&label=discord)](https://discord.gg/8Cx26tfWNs)

Server side land claiming mod for fabric.

### Translations

Translations for Flan depends on the community. Submitting a translation to the mod is simple:
1. Fork this repository
2. Create a translation `.json`-file under `common/src/main/resources/data/flan/lang`.  
   Use the english translation under `common/src/generated/resources/data/flan/lang` as reference.
3. Create a PR to submit your translation

### Devs

If you want to add integration to flan first add the following snippet to your build.gradle
```groovy
repositories {
    maven {
        name = "Flemmli97"
        url "https://maven.blazing-coop.net/releases"
    }
}

dependencies {    
    //Fabric==========    
    modCompileOnly("io.github.flemmli97:flan:${minecraft_version}-${flan_version}-${mod_loader}:api") {
		transitive = false //Remove this if you want to have all those optional dependencies
	}
    modRuntime("io.github.flemmli97:flan:${minecraft_version}-${flan_version}-${mod_loader}") {
		transitive = false //Remove this if you want to have all those optional dependencies
	}
    
    //NeoForge==========    
    compileOnly "io.github.flemmli97:flan:${minecraft_version}-${flan_version}-${mod_loader}:api"
    runtimeOnly "io.github.flemmli97:flan:${minecraft_version}-${flan_version}-${mod_loader}"
}
```

To check if an action can be done simply call `ClaimHandler#canInteract(ServerPlayer player, BlockPos pos, Identifier permission)`
