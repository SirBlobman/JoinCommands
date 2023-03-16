val jenkinsBuildNumber = System.getenv("BUILD_NUMBER") ?: "Unknown"
val baseVersion = findProperty("version.base") as String
val betaVersionString = (findProperty("version.beta") ?: "false") as String
val betaVersion = betaVersionString.toBoolean()
val betaVersionPart = if (betaVersion) "Beta-" else ""

val calculatedVersion = "$baseVersion.$betaVersionPart$jenkinsBuildNumber"
rootProject.ext.set("calculatedVersion", calculatedVersion)

plugins {
    id("java")
}

allprojects {
    apply(plugin = "java")

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    repositories {
        mavenCentral()
        maven("https://nexus.sirblobman.xyz/public/")
    }

    dependencies {
        compileOnly("org.jetbrains:annotations:24.0.1")
    }

    tasks {
        withType<JavaCompile> {
            options.encoding = "UTF-8"
        }
    }
}
