plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.hilt.android) apply false
}

subprojects {
    plugins.withType(org.gradle.api.plugins.JavaPlugin::class.java) {
        apply(plugin = "jacoco")
        extensions.configure(org.gradle.testing.jacoco.plugins.JacocoPluginExtension::class.java) {
            toolVersion = "0.8.11"
        }
        tasks.withType(Test::class.java).configureEach {
            useJUnit()
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
        tasks.withType(org.gradle.testing.jacoco.tasks.JacocoReport::class.java).configureEach {
            val reportTask = this
            tasks.withType(Test::class.java).configureEach {
                finalizedBy(reportTask)
            }
        }
    }
}

tasks.register("jacocoAll") {
    subprojects.forEach { p ->
        dependsOn(p.tasks.withType(org.gradle.testing.jacoco.tasks.JacocoReport::class.java))
    }
}
