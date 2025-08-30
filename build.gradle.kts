plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.hilt.android) apply false
    id("jacoco") apply false
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
        }
        tasks.register("jacocoTestReport", org.gradle.testing.jacoco.tasks.JacocoReport::class.java) {
            reports {
                xml.required.set(true)
                html.required.set(true)
            }
            val testTask = tasks.findByName("test") as? Test
            executionData.setFrom(fileTree(buildDir).include("**/jacoco/test.exec", "**/jacoco/*.exec", "**/jacoco/*.ec"))
            val javaSrc = fileTree("src/main/java")
            val kotlinSrc = fileTree("src/main/kotlin")
            sourceDirectories.setFrom(files(javaSrc, kotlinSrc))
            classDirectories.setFrom(fileTree(buildDir).include("**/classes/**"))
            dependsOn(testTask)
        }
    }
}
