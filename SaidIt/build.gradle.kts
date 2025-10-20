import com.android.build.api.dsl.ManagedVirtualDevice

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    id("com.google.gms.google-services") version "4.4.1" apply false
}

// Apply Google Services plugin only if google-services.json is present
val hasGoogleServicesJson = file("google-services.json").exists() ||
        file("src/debug/google-services.json").exists() ||
        file("src/release/google-services.json").exists()
if (hasGoogleServicesJson) {
    apply(plugin = "com.google.gms.google-services")
} else {
    logger.lifecycle("google-services.json missing; skipping Google Services plugin")
}

android {
    namespace = "com.siya.epistemophile"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.siya.epistemophile"
        minSdk = 30
        targetSdk = 34
        versionCode = 15
        versionName = "2.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["clearPackageData"] = "true"
        testInstrumentationRunnerArguments["grantAllPermissions"] = "true"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                file("proguard.cfg")
            )
        }
        debug {}
    }

    lint { abortOnError = false }
    buildFeatures { buildConfig = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    testOptions {
        unitTests.isIncludeAndroidResources = true
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
        animationsDisabled = true
        managedDevices {
            devices {
                maybeCreate<ManagedVirtualDevice>("mediumApi30").apply {
                    device = "Medium Phone"
                    apiLevel = 30
                    systemImageSource = "google-atd"
                    require64Bit = false
                }
            }
            groups {
                maybeCreate("ci").apply {
                    targetDevices.add(devices["mediumApi30"])
                }
            }
        }
    }
}

// KSP configuration to keep Hilt fast-init while suppressing noisy validation in tests
ksp {
    arg("dagger.hilt.android.internal.disableAndroidSuperclassValidation", "true")
    arg("dagger.fastInit", "enabled")
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.tap.target.view)
    implementation(project(":core"))
    implementation(project(":domain"))
    implementation(project(":data"))
    implementation(project(":audio"))
    implementation(project(":features:recorder"))
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    
    // Coroutines for modern threading
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    // Unit testing
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.robolectric)
    testRuntimeOnly(libs.robolectric.android.all)
    testImplementation(libs.coroutines.test)
    // Hilt testing for Robolectric/JUnit
    testImplementation(libs.hilt.android.testing)
    kspTest(libs.hilt.compiler)

    // Android instrumentation tests
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test:core-ktx:1.5.0")
    androidTestImplementation("androidx.test:core:1.5.0")
    kspAndroidTest(libs.hilt.compiler)
    androidTestUtil("androidx.test:orchestrator:1.4.2")
}
