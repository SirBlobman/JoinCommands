fun fetchProperty(propertyName: String, defaultValue: String): String {
    val found = findProperty(propertyName)
    if (found != null) {
        return found.toString()
    }

    return defaultValue
}

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
        val pluginName = fetchProperty("plugin.name", "")
        val pluginPrefix = fetchProperty("plugin.prefix", "")
        val pluginDescription = fetchProperty("plugin.description", "")
        val pluginWebsite = fetchProperty("plugin.website", "")
        val pluginMainClass = fetchProperty("plugin.main", "")
        val calculatedVersion = rootProject.ext.get("calculatedVersion") as String

        filesMatching("plugin.yml") {
            expand(
                mapOf(
                    "pluginName" to pluginName,
                    "pluginPrefix" to pluginPrefix,
                    "pluginDescription" to pluginDescription,
                    "pluginWebsite" to pluginWebsite,
                    "pluginMain" to pluginMainClass,
                    "pluginVersion" to calculatedVersion
                )
            )
        }
    }
}
