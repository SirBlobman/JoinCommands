repositories {
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly(project(":common"))
    compileOnly("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.3")
}

tasks {
    processResources {
        val calculatedVersion = rootProject.ext.get("calculatedVersion") as String
        val pluginName = (findProperty("plugin.name") ?: "") as String
        val pluginPrefix = (findProperty("plugin.prefix") ?: "") as String
        val pluginDescription = (findProperty("plugin.description") ?: "") as String
        val pluginWebsite = (findProperty("plugin.website") ?: "") as String
        val pluginMainClass = (findProperty("plugin.main") ?: "") as String

        filesMatching("plugin.yml") {
            filter {
                it.replace("\${plugin.name}", pluginName)
                    .replace("\${plugin.prefix}", pluginPrefix)
                    .replace("\${plugin.description}", pluginDescription)
                    .replace("\${plugin.website}", pluginWebsite)
                    .replace("\${plugin.main}", pluginMainClass)
                    .replace("\${plugin.version}", calculatedVersion)
            }
        }
    }
}
