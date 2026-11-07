plugins {
    id("kmp-android-library")
    id("kotlinx-serialization")
}

kotlin {
    android {
        namespace = "app.aaps.plugins.eversense"
        withHostTest { isIncludeAndroidResources = true; isReturnDefaultValues = true }
    }
    sourceSets {
        androidMain.dependencies {
            api(libs.androidx.core)
            api(project.dependencies.platform(libs.kotlinx.serialization.bom))
            api(libs.kotlinx.serialization.json)
            api(libs.org.slf4j.api)
            api(libs.com.github.tony19.logback.android)
            implementation("org.bouncycastle:bcpkix-jdk18on:1.81")
            implementation("org.bouncycastle:bcprov-jdk18on:1.81")
        }
        getByName("androidHostTest").dependencies {
            implementation(libs.org.junit.jupiter)
            implementation(libs.org.junit.jupiter.api)
            runtimeOnly(libs.org.junit.platform.launcher)
            implementation(libs.org.mockito.kotlin)
            implementation(libs.org.mockito.junit.jupiter)
            implementation("com.squareup.okhttp3:mockwebserver:4.12.0")
        }
    }
}
