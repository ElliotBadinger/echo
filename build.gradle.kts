import org.gradle.api.tasks.testing.Test

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.ksp) apply false
}

subprojects {
    plugins.withType(org.gradle.api.plugins.JavaPlugin::class.java) {
        apply(plugin = "jacoco")
        extensions.configure(org.gradle.testing.jacoco.plugins.JacocoPluginExtension::class.java) {
            toolVersion = "0.8.11"
        }
        tasks.withType(Test::class.java).configureEach {
            useJUnit()
            finalizedBy("jacocoTestReport")
            val maxForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
            maxParallelForks = maxForks
            forkEvery = 50
            jvmArgs("-Xmx1024m")
            systemProperty("junit.jupiter.execution.parallel.enabled", "true")
            testLogging {
                events("failed", "skipped")
                exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.SHORT
                showStandardStreams = false
            }
        }
        // Configure any existing JacocoReport tasks (e.g., the default 'jacocoTestReport' on JVM modules)
        tasks.withType(org.gradle.testing.jacoco.tasks.JacocoReport::class.java).configureEach {
            reports {
                xml.required.set(true)
                html.required.set(true)
            }
            executionData.setFrom(fileTree(buildDir).include("**/jacoco/test.exec", "**/jacoco/*.exec", "**/jacoco/*.ec"))
            val javaSrc = fileTree("src/main/java")
            val kotlinSrc = fileTree("src/main/kotlin")
            sourceDirectories.setFrom(files(javaSrc, kotlinSrc))
            classDirectories.setFrom(fileTree(buildDir).include("**/classes/**"))
        }
        // Ensure test tasks finalize by any JacocoReport tasks that exist in the project
        tasks.withType(Test::class.java).configureEach {
            finalizedBy("jacocoTestReport")
        }
    }
}

val fastTests = tasks.register("fastTests")
val fullTests = tasks.register("fullTests")

gradle.projectsEvaluated {
    val includeProperty = gradle.startParameter.projectProperties["includeTestTasks"]
        ?.split(',')
        ?.map { it.trim() }
        ?.filter { it.isNotEmpty() }
        ?.toSet()
        ?: emptySet()
    val skipInstrumentation = gradle.startParameter.projectProperties["skipInstrumentation"] == "true"

    val fastTargets = listOf(
        project(":domain") to "test",
        project(":data") to "test",
        project(":features:recorder") to "test",
        project(":audio") to "testDebugUnitTest",
        project(":SaidIt") to "testDebugUnitTest"
    )

    val fastProviders = fastTargets
        .filter { (proj, taskName) -> taskName in proj.tasks.names }
        .associate { (proj, taskName) ->
            val path = "${proj.path}:$taskName"
            path to proj.tasks.named(taskName)
        }

    val selectedFast = if (includeProperty.isEmpty()) {
        fastProviders.keys
    } else {
        fastProviders.keys.filter { it in includeProperty }.ifEmpty { fastProviders.keys }
    }

    fastTests.configure {
        setDependsOn(emptyList<Any>())
        selectedFast.forEach { dependsOn(fastProviders.getValue(it)) }
    }

    val saidItProject = project(":SaidIt")
    val instrumentationTaskName = "mediumApi30DebugAndroidTest"
    val instrumentationProvider = if (instrumentationTaskName in saidItProject.tasks.names) {
        saidItProject.tasks.named(instrumentationTaskName)
    } else {
        null
    }

    fullTests.configure {
        setDependsOn(emptyList<Any>())
        dependsOn(fastTests)
        if (!skipInstrumentation && instrumentationProvider != null) {
            dependsOn(instrumentationProvider)
        }
    }
}

tasks.register("jacocoAll") {
    subprojects.forEach { p ->
        dependsOn(p.tasks.withType(org.gradle.testing.jacoco.tasks.JacocoReport::class.java))
    }
}
