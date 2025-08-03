import java.util.Properties
import org.gradle.api.GradleException

// Chargement de local.properties
val keystoreProps = Properties().apply {
    val propFile = rootProject.file("local.properties")
    if (propFile.exists()) {
        propFile.inputStream().use { load(it) }
    }
}
fun prop(key: String) = keystoreProps.getProperty(key, "")

plugins {
    // Pour tester, remplace temporairement vos alias par les IDs directs :
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    alias(libs.plugins.kotlin.compose)
    // (Vous pouvez remettre alias(libs.plugins.kotlin.compose) si vous le souhaitez)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.gap.dmcgap" // ou ton namespace réel
    compileSdk = 35 // adapte à ton projet

    defaultConfig {
        applicationId = "com.gap.dmcgap"
        minSdk = 25
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // Pour Kotlin : jvmTarget à 17
    kotlinOptions {
        jvmTarget = "17"
    }
    signingConfigs {
        create("releaseConfig") {
            val storeFilePath = prop("RELEASE_STORE_FILE")
            if (storeFilePath.isBlank()) {
                // Pas de keystore renseigné : on ne configure pas la signature
                println("⚠️ RELEASE_STORE_FILE non défini : release non signé")
            } else {
                val store = file(storeFilePath)
                if (!store.exists()) {
                    throw GradleException("Keystore introuvable à l'emplacement spécifié : $storeFilePath")
                }
                storeFile = store
                storePassword = prop("RELEASE_STORE_PASSWORD")
                keyAlias = prop("RELEASE_KEY_ALIAS")
                keyPassword = prop("RELEASE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        getByName("release") {
            if (prop("RELEASE_STORE_FILE").isNotBlank()) {
                signingConfig = signingConfigs.getByName("releaseConfig")
            }
            isMinifyEnabled = false
        }
        getByName("debug") {
            // debug utilise la signature automatique d'Android (pas besoin de keystore custom)
        }
    }
}


dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.foundation)
    implementation(platform("com.google.firebase:firebase-bom:33.13.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation (libs.androidx.compose.material)
    implementation(libs.firebase.config.ktx)

}