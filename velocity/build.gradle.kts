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
        val calculatedVersion = rootProject.ext.get("calculatedVersion") as String
        val pluginId = (findProperty("plugin.id") ?: "") as String
        val pluginName = (findProperty("plugin.name") ?: "") as String
        val pluginPrefix = (findProperty("plugin.prefix") ?: "") as String
        val pluginDescription = (findProperty("plugin.description") ?: "") as String
        val pluginWebsite = (findProperty("plugin.website") ?: "") as String
        val pluginMainClass = (findProperty("plugin.main") ?: "") as String

        filesMatching("velocity-plugin.json") {
            filter {
                it.replace("\${plugin.id}", pluginId)
                    .replace("\${plugin.name}", pluginName)
                    .replace("\${plugin.prefix}", pluginPrefix)
                    .replace("\${plugin.description}", pluginDescription)
                    .replace("\${plugin.website}", pluginWebsite)
                    .replace("\${plugin.main}", pluginMainClass)
                    .replace("\${plugin.version}", calculatedVersion)
            }
        }
    }
}
