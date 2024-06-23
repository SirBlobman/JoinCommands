fun fetchProperty(propertyName: String, defaultValue: String): String {
    val found = findProperty(propertyName)
    if (found != null) {
        return found.toString()
    }

    return defaultValue
}

repositories {
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://libraries.minecraft.net/")
}

dependencies {
    compileOnly(project(":common"))
    compileOnly("net.md-5:bungeecord-api:1.21-R0.1-SNAPSHOT")
}

tasks {
    processResources {
        val pluginName = fetchProperty("plugin.name", "")
        val pluginPrefix = fetchProperty("plugin.prefix", "")
        val pluginDescription = fetchProperty("plugin.description", "")
        val pluginWebsite = fetchProperty("plugin.website", "")
        val pluginMainClass = fetchProperty("plugin.main", "")
        val calculatedVersion = rootProject.ext.get("calculatedVersion") as String

        filesMatching("bungee.yml") {
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
