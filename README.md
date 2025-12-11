# Usage
This mod is currently for 1.21.11 with Fabric, but may work with Quilt (not tested!).
You can download the mod from the [modrinth page](https://modrinth.com/mod/elytra-infinite), but you also need to install these mods:
- [Fabric API](https://modrinth.com/mod/fabric-api)
- [YetAnotherConfigLib](https://modrinth.com/mod/yacl)
- [Mod Menu](https://modrinth.com/mod/modmenu)

# Building
Requirements:
- Git
- JDK 21

In a shell (on Linux) or in Git Bash (on Windows) run:
```sh
git clone https://github.com/meanwhile131/elytra-infinite.git
cd elytra-infinite
./gradlew build
```
The mod JAR will be in `build/libs` (the plain .jar is the one you want, -sources.jar is for other purposes).
