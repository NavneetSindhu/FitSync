// Top-level build file
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
    alias(libs.plugins.firebase.crashlytics) apply false
}

// Automatically load all keys from local.properties into project properties
val localProps = java.util.Properties()
val localPropsFile = file("local.properties")
if (localPropsFile.exists()) {
    localPropsFile.inputStream().use { localProps.load(it) }
    localProps.forEach { name, value ->
        allprojects {
            extra.set(name.toString(), value.toString())
        }
    }
}