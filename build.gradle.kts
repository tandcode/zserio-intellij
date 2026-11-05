import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.4.21"
    id("org.jetbrains.intellij.platform") version "2.19.0"
    id("org.jetbrains.intellij.platform.grammarkit") version "2.19.0"
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")

    intellijPlatform {
        intellijIdea(providers.gradleProperty("platformVersion"))
        testFramework(TestFrameworkType.Platform)
    }
}

intellijPlatform {
    pluginConfiguration {
        name = providers.gradleProperty("pluginName")
        version = providers.gradleProperty("pluginVersion")
        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
            untilBuild = provider { null }
        }
        // Change notes shown on the Marketplace: the "## <version>" section of CHANGELOG.md
        changeNotes = provider {
            val changelog = file("CHANGELOG.md")
            val version = providers.gradleProperty("pluginVersion").get()
            if (!changelog.exists()) return@provider ""
            val lines = changelog.readLines()
            val start = lines.indexOfFirst { it.startsWith("## ") && it.contains(version) }
            if (start < 0) return@provider ""
            val body = lines.drop(start + 1).takeWhile { !it.startsWith("## ") }
            val items = body.map { it.trim() }.filter { it.startsWith("- ") }
            "<ul>" + items.joinToString("") { "<li>${it.removePrefix("- ")}</li>" } + "</ul>"
        }
    }
    signing {
        certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
        privateKey = providers.environmentVariable("PRIVATE_KEY")
        password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    }
    publishing {
        token = providers.environmentVariable("PUBLISH_TOKEN")
        // Versions like 0.2.0-beta.1 go to the "beta" channel, everything else to the default channel
        channels = providers.gradleProperty("pluginVersion").map {
            listOf(it.substringAfter('-', "default").substringBefore('.'))
        }
    }
    pluginVerification {
        ides {
            recommended()
        }
    }
}

tasks {
    generateLexer {
        sourceFile = file("src/main/grammar/Zserio.flex")
    }

    generateParser {
        sourceFile = file("src/main/grammar/Zserio.bnf")
    }

    test {
        // Optional test inputs, e.g. -Pzserio.corpus=/path/to/schemas or -Pzserio.jar=/path/to/zserio.jar
        // (see ZserioCorpusParsingTest and ZserioCompilerTest).
        project.properties.filterKeys { it.startsWith("zserio.") }.forEach { (key, value) ->
            systemProperty(key, value.toString())
        }
    }
}

sourceSets {
    main {
        java {
            srcDir(tasks.generateLexer)
            srcDir(tasks.generateParser)
        }
    }
}
