import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    base
    `maven-publish`
    kotlin("jvm") version "1.6.21"
    id("com.adarshr.test-logger") version "2.1.1"
    id("com.github.ben-manes.versions") version "0.34.0"
    id("nebula.release") version "15.0.1"
    id("jacoco")
    id("com.github.kt3k.coveralls") version "2.10.2"
}

allprojects {
    repositories {
        jcenter()
    }
    group = "it.justwrote"
}

subprojects {
    apply {
        plugin("kotlin")
        plugin("org.gradle.maven-publish")
        plugin("com.adarshr.test-logger")
        plugin("com.github.kt3k.coveralls")
        plugin("jacoco")
    }

    val compileKotlin: KotlinCompile by tasks
    compileKotlin.kotlinOptions {
        jvmTarget = "1.8"
    }

    val compileTestKotlin: KotlinCompile by tasks
    compileTestKotlin.kotlinOptions {
        jvmTarget = "1.8"
    }

    tasks.test {
        useJUnitPlatform()
        systemProperties(mapOf("user.language" to "en"))
        testLogging {
            outputs.upToDateWhen { false }
            showStandardStreams = true
        }
    }

    testlogger {
        theme = com.adarshr.gradle.testlogger.theme.ThemeType.MOCHA_PARALLEL
        showStackTraces = true
        showFullStackTraces = true
        showCauses = true
        slowThreshold = 2000
        showSimpleNames = true
        showStandardStreams = true
    }

    jacoco {
        toolVersion = "0.8.5"
    }

    tasks.jacocoTestReport {
        reports {
            xml.isEnabled = true
            html.isEnabled = true
            csv.isEnabled = false
            html.destination = File("${buildDir}/reports/jacoco/test/html")
        }
    }

    tasks.jacocoTestCoverageVerification {
        violationRules {
            rule {
                limit {
                    minimum = BigDecimal(0.8)
                }
            }
        }
    }

    tasks.getByName("check") { dependsOn("jacocoTestCoverageVerification") }
    tasks.test { finalizedBy("jacocoTestReport") }

    configurations.create("testArtifacts") {
        extendsFrom(configurations["testRuntime"])
    }

    tasks.register("testJar", Jar::class.java) {
        dependsOn("testClasses")
        archiveClassifier.set("test")
        from(sourceSets["test"].output)
    }

    artifacts {
        add("testArtifacts", tasks.named<Jar>("testJar") )
    }

    val sourcesJar by tasks.registering(Jar::class) {
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource)
    }

    val project = this
    publishing {
        publications {
            register("mavenJava", MavenPublication::class) {
                from(components["java"])
                artifact(sourcesJar.get())
                groupId = project.group.toString()
                artifactId = project.name
                version = project.version.toString()
            }
        }
    }
}

//coveralls {
//    sourceDirs = files(subprojects.sourceSets.main.allSource.srcDirs).files.absolutePath
//    jacocoReportPath = file("${buildDir}/reports/jacoco/report.xml")
//}
//
//task codeCoverageReport(type: JacocoReport) {
//    executionData fileTree(project.rootDir.absolutePath).include("**/build/jacoco/*.exec")
//
//    subprojects.each {
//        if(!it.sourceSets.test.kotlin.srcDirs.every { !it.exists() }) {
//            sourceSets it.sourceSets.main
//        }
//    }
//
//    reports {
//        xml.enabled true
//        xml.destination file("${buildDir}/reports/jacoco/report.xml")
//        html.enabled false
//        csv.enabled false
//    }
//}
//

project(":kjob-core") {
    dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.0")
        api("org.slf4j:slf4j-api:1.7.30")

        testImplementation("io.kotest:kotest-runner-junit5:4.3.1")
        testImplementation("io.kotest:kotest-assertions-core:4.3.1")
        testImplementation("io.mockk:mockk:1.10.2")
        testRuntimeOnly("ch.qos.logback:logback-classic:1.2.3")
    }
}

project(":kjob-example") {
    dependencies {
        implementation(project(":kjob-core"))
        implementation(project(":kjob-kron"))
        implementation(project(":kjob-mongo"))
        implementation(project(":kjob-inmem"))

        implementation("com.cronutils:cron-utils:9.1.1") {
            exclude(group = "org.slf4j", module = "slf4j-simple")
        }

        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.0")
        implementation("ch.qos.logback:logback-classic:1.2.3")
    }
}

project(":kjob-mongo") {
    dependencies {
        implementation(project(":kjob-core"))
        implementation("org.mongodb:mongodb-driver-reactivestreams:4.1.1")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.4.0")

        testImplementation("io.reactivex.rxjava2:rxkotlin:2.4.0")
        testImplementation("io.kotest:kotest-runner-junit5:4.3.1")
        testImplementation("io.kotest:kotest-assertions-core:4.3.1")
        testImplementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo:3.0.0")
        testImplementation("io.mockk:mockk:1.10.2")
        testImplementation(project(path = ":kjob-core", configuration = "testArtifacts"))

        testRuntimeOnly("ch.qos.logback:logback-classic:1.2.3")
    }
}

project(":kjob-inmem") {
    dependencies {
        implementation(project(":kjob-core"))
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.0")

        testImplementation("io.kotest:kotest-runner-junit5:4.3.1")
        testImplementation("io.kotest:kotest-assertions-core:4.3.1")
        testImplementation("io.mockk:mockk:1.10.2")
        testImplementation(project(path = ":kjob-core", configuration = "testArtifacts"))

        testRuntimeOnly("ch.qos.logback:logback-classic:1.2.3")
    }
}

project(":kjob-kron") {
    dependencies {
        implementation(project(":kjob-core"))
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.0")
        implementation("com.cronutils:cron-utils:9.1.1") {
            exclude(group = "org.slf4j", module = "slf4j-simple")
        }
        api("org.slf4j:slf4j-api:1.7.30")

        testImplementation(project(":kjob-inmem"))
        testImplementation("io.kotest:kotest-runner-junit5:4.3.1")
        testImplementation("io.kotest:kotest-assertions-core:4.3.1")
        testImplementation("io.mockk:mockk:1.10.2")
        testImplementation(project(path = ":kjob-core", configuration = "testArtifacts"))

        testRuntimeOnly("ch.qos.logback:logback-classic:1.2.3")
    }
}
