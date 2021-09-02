# Flan [![](http://cf.way2muchnoise.eu/full_404578_Fabric_%20.svg)![](http://cf.way2muchnoise.eu/versions/404578.svg)](https://www.curseforge.com/minecraft/mc-mods/flan) [![](http://cf.way2muchnoise.eu/full_493246_Forge_%20.svg)![](http://cf.way2muchnoise.eu/versions/493246.svg)](https://www.curseforge.com/minecraft/mc-mods/flan-forge) [![Discord](https://img.shields.io/discord/790631506313478155?color=0a48c4&label=discord)](https://discord.gg/K7G9GyER)

Server side land claiming mod for fabric

To use flan in your dependencies add the following snippet to your build.gradle

```gradle
repositories {
    maven {
        name = "Flemmli97"
        url "https://gitlab.com/api/v4/projects/21830712/packages/maven"
    }
}

dependencies {
    modImplementation "io.github.flemmli97:flan:${minecraft_version}-${flan_version}"
        
    //Use the following lines for 1.6.0+
    modCompileOnly("io.github.flemmli97:flan:${minecraft_version}-${flan_version}:api") {
		transitive = false //Remove this if you want to have all those optional dependencies
	}
    modRuntime("io.github.flemmli97:flan:${minecraft_version}-${flan_version}") {
		transitive = false //Remove this if you want to have all those optional dependencies
	}
}
```

