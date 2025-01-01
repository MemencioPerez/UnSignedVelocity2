plugins {
    java
    alias(libs.plugins.blossom)
    alias(libs.plugins.runvelocity)
    alias(libs.plugins.shadow)
}

val packetEventsVersion = libs.versions.packetevents.get()

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.william278.net/velocity/")
    maven("https://repo.codemc.io/repository/maven-releases/")
}

dependencies {
    implementation(libs.bstats)
    compileOnly(libs.velocity.api)
    compileOnly(libs.velocity.proxy)
    annotationProcessor(libs.velocity.api)
    compileOnly(libs.packetevents)
}

sourceSets {
    main {
        blossom {
            javaSources {
                property("version", project.version.toString())
            }
        }
    }
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(17)
    }
    build {
        dependsOn(shadowJar)
    }
    clean {
        delete("run")
    }
    shadowJar {
        archiveBaseName.set(rootProject.name)
        archiveClassifier.set("")
        relocate("org.bstats", "io.github._4drian3d.unsignedvelocity.libs.bstats")
        minimize()
    }
    runVelocity {
        downloadPlugins {
            if (packetEventsVersion.contains("SNAPSHOT")) {
                url("https://ci.codemc.io/job/retrooper/job/packetevents/lastSuccessfulBuild/artifact/velocity/build/libs/packetevents-velocity-$packetEventsVersion.jar")
            } else {
                github("retrooper", "packetevents", "v$packetEventsVersion",
                    "packetevents-velocity-$packetEventsVersion.jar"
                )
            }
        }
        velocityVersion(libs.versions.velocity.get())
    }
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))
