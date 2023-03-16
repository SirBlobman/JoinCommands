repositories {
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    compileOnly(project(":common"))
    compileOnly("net.md-5:bungeecord-api:1.19-R0.1-SNAPSHOT")
}

tasks {
    processResources {
        val calculatedVersion = rootProject.ext.get("calculatedVersion") as String
        val pluginName = (findProperty("plugin.name") ?: "") as String
        val pluginPrefix = (findProperty("plugin.prefix") ?: "") as String
        val pluginDescription = (findProperty("plugin.description") ?: "") as String
        val pluginWebsite = (findProperty("plugin.website") ?: "") as String
        val pluginMainClass = (findProperty("plugin.main") ?: "") as String

        filesMatching("bungee.yml") {
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
