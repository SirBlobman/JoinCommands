fun fetchProperty(propertyName: String, defaultValue: String): String {
    val found = findProperty(propertyName)
    if (found != null) {
        return found.toString()
    }

    return defaultValue
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly(project(":common"))
    compileOnly("com.velocitypowered:velocity-api:3.1.1")
    annotationProcessor("com.velocitypowered:velocity-api:3.1.1")
}

tasks {
    processResources {
        val pluginId = fetchProperty("plugin.id", "")
        val pluginName = fetchProperty("plugin.name", "")
        val pluginPrefix = fetchProperty("plugin.prefix", "")
        val pluginDescription = fetchProperty("plugin.description", "")
        val pluginWebsite = fetchProperty("plugin.website", "")
        val pluginMainClass = fetchProperty("plugin.main", "")
        val calculatedVersion = rootProject.ext.get("calculatedVersion") as String

        filesMatching("velocity-plugin.json") {
            expand(
                mapOf(
                    "pluginId" to pluginId,
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
