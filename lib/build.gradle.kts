plugins {
    kotlin("multiplatform")
}

group = "tw.poj.kesi"
version = "0.6.0"

base {
    archivesName.set("konvert-to-poj")
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17)

    jvm()
    iosArm64()
    iosSimulatorArm64()

    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().configureEach {
        binaries.framework {
            baseName = "KonvertToPOJ"
        }
    }

    js {
        browser()
        nodejs()
        useEsModules()
        binaries.library()
        generateTypeScriptDefinitions()
    }

    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        nodejs()
    }

    sourceSets {
        commonMain {}
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

// --- Static GitHub Pages converter site -------------------------------------
// Bundles web/ together with the Kotlin/JS ES-module build into build/site,
// a fully static directory (no server code) ready for GitHub Pages.

val assembleWebSite by tasks.registering(Sync::class) {
    group = "distribution"
    description = "Assembles the static converter site into lib/build/site"

    dependsOn(tasks.named("jsBrowserProductionLibraryDistribution"))

    into(layout.buildDirectory.dir("site"))

    from(rootProject.layout.projectDirectory.dir("web"))
    from(layout.buildDirectory.dir("dist/js/productionLibrary")) {
        into("lib")
        include("*.mjs")
    }

    doLast {
        // Tell GitHub Pages to serve the files as-is instead of running Jekyll.
        layout.buildDirectory.file("site/.nojekyll").get().asFile.writeText("")
    }
}
