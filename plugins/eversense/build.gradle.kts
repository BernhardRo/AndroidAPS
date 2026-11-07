plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    id("kotlin-android")
    id("android-module-dependencies")
    id("test-module-dependencies")
    id("jacoco-module-dependencies")
}

android {
    namespace = "com.nightscout.eversense"
}


dependencies {
    testImplementation(libs.androidx.work.testing)

    ksp(libs.com.google.dagger.compiler)
    ksp(libs.com.google.dagger.android.processor)
}