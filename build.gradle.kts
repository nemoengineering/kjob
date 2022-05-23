import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    base
    `maven-publish`
    kotlin("jvm") version "1.6.21"
    id("com.adarshr.test-logger") version "3.2.0"

    // Linter
    id("org.jmailen.kotlinter") version "3.10.0"
}

allprojects {
    repositories {
        mavenCentral()
    }
    group = "sh.nemo"
    version = "0.1.0"
}

subprojects {
    apply {
        plugin("kotlin")
        plugin("org.gradle.maven-publish")
        plugin("com.adarshr.test-logger")
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

    configurations.create("testArtifacts") {
        extendsFrom(configurations["testRuntimeOnly"])
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

project(":kjob-core") {
    dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
        api("org.slf4j:slf4j-api:1.7.36")

        testImplementation("io.kotest:kotest-runner-junit5:4.3.1")
        testImplementation("io.kotest:kotest-assertions-core:4.3.1")
        testImplementation("io.mockk:mockk:1.12.4")
        testRuntimeOnly("ch.qos.logback:logback-classic:1.2.11")
    }
}

project(":kjob-example") {
    dependencies {
        implementation(project(":kjob-core"))
        implementation(project(":kjob-kron"))
        implementation(project(":kjob-mongo"))
        implementation(project(":kjob-inmem"))

        implementation("com.cronutils:cron-utils:9.1.6") {
            exclude(group = "org.slf4j", module = "slf4j-simple")
        }

        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
        implementation("ch.qos.logback:logback-classic:1.2.11")
    }
}

project(":kjob-mongo") {
    dependencies {
        implementation(project(":kjob-core"))
        implementation("org.mongodb:mongodb-driver-reactivestreams:4.6.0")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.6.1")

        testImplementation("io.reactivex.rxjava2:rxkotlin:2.4.0")
        testImplementation("io.kotest:kotest-runner-junit5:4.3.1")
        testImplementation("io.kotest:kotest-assertions-core:4.3.1")
        testImplementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo:3.4.6")
        testImplementation("io.mockk:mockk:1.12.4")
        testImplementation(project(path = ":kjob-core", configuration = "testArtifacts"))

        testRuntimeOnly("ch.qos.logback:logback-classic:1.2.11")
    }
}

project(":kjob-inmem") {
    dependencies {
        implementation(project(":kjob-core"))
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")

        testImplementation("io.kotest:kotest-runner-junit5:4.3.1")
        testImplementation("io.kotest:kotest-assertions-core:4.3.1")
        testImplementation("io.mockk:mockk:1.12.4")
        testImplementation(project(path = ":kjob-core", configuration = "testArtifacts"))

        testRuntimeOnly("ch.qos.logback:logback-classic:1.2.11")
    }
}

project(":kjob-kron") {
    dependencies {
        implementation(project(":kjob-core"))
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
        implementation("com.cronutils:cron-utils:9.1.6") {
            exclude(group = "org.slf4j", module = "slf4j-simple")
        }
        api("org.slf4j:slf4j-api:1.7.30")

        testImplementation(project(":kjob-inmem"))
        testImplementation("io.kotest:kotest-runner-junit5:4.3.1")
        testImplementation("io.kotest:kotest-assertions-core:4.3.1")
        testImplementation("io.mockk:mockk:1.12.4")
        testImplementation(project(path = ":kjob-core", configuration = "testArtifacts"))

        testRuntimeOnly("ch.qos.logback:logback-classic:1.2.11")
    }
}
