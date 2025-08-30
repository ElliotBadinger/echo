plugins {
    alias(libs.plugins.kotlin.jvm)
}

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

dependencies {
    implementation(libs.coroutines.core)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}
