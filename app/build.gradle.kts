plugins {
    // Pour tester, remplace temporairement vos alias par les IDs directs :
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    alias(libs.plugins.kotlin.compose)
    // (Vous pouvez remettre alias(libs.plugins.kotlin.compose) si vous le souhaitez)
    id("com.google.gms.google-services")
}

android {
        namespace = "com.gap.dmcgap"
        compileSdk = 35
        buildFeatures {
            buildConfig = true // Active la génération
        }
        defaultConfig {
            applicationId = "com.gap.dmcgap"
            minSdk = 25
            targetSdk = 35
            versionCode = 1
            versionName = "1.0"

        }
        signingConfigs {
            create("releaseConfig") {
                // Chemin vers ton keystore (relatif au dossier du module app)
                storeFile = file("D:/DepDocs/Keystores/dmcgap-release.jks")
                // Mot de passe du keystore
                storePassword = "dmcgap2025"
                // Alias que tu as choisi
                keyAlias = "dmcgap-key"
                // Mot de passe de l’alias (si différent)
                keyPassword = "dmcgap2025"
            }
        }
        buildTypes {
            release {
                isMinifyEnabled = false
                // signingConfig défini précédemment
                signingConfig = signingConfigs.getByName("releaseConfig")
            }
            debug { }
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
        }
        kotlinOptions {
            jvmTarget = "11"
        }
        buildFeatures {
            compose = true
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