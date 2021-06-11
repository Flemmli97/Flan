# Flan

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
    modImplementation "io.github.flemmli97:flan:${flan_version}-${minecraft_version}"
}
```
