# VersionController [![CodeFactor](https://www.codefactor.io/repository/github/rogermiranda1000/spigot-versioncontroller/badge)](https://www.codefactor.io/repository/github/rogermiranda1000/spigot-versioncontroller)
A helper for spigot plugins

### Dependencies
- spigot 1.16.5
- [Residence](https://zrips.net/Residence/)
- [WorldGuard 7.0.0](https://dev.bukkit.org/projects/worldguard/files/2723606)
- [WorldGuard 6.2](https://dev.bukkit.org/projects/worldguard/files/956770)
- [WorldEdit 7.0.0](https://dev.bukkit.org/projects/worldedit/files/2723275)
- [ConfigLib 2.2.0](https://github.com/Exlll/ConfigLib/releases/tag/v2.2.0) (extract with .jar shaded)
- (already shaded in code) [ConfigLib; tomwmth's fork](https://github.com/tomwmth/ConfigLib/tree/alpine) (extract with .jar shaded)
- Maven's `com.google.code.gson:gson:LATEST` (also, if the server is using <1.9 they'll need the [Gson plugin](https://www.spigotmc.org/resources/gson-for-1-8-3-or-older.30852/))
- Maven's `org.jetbrains:annotations:LATEST`
- Maven's `com.github.davidmoten:rtree-multi:LATEST`, and its dependency `com.github.davidmoten:guava-mini:LATEST` (extract both with .jar)
- Maven's `io.sentry:sentry:LATEST` (extract with .jar)
- Maven's `org.bstats:bstats-bukkit:LATEST` (extract with .jar shaded)

### Compile
- Run Maven's `clean install`. Remember to use Java **8** (already managed by Maven).
