plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

fun readLocalEnv(name: String): String {
    val projectProperty = project.findProperty(name) as? String
    if (!projectProperty.isNullOrBlank()) return projectProperty

    val systemEnv = System.getenv(name)
    if (!systemEnv.isNullOrBlank()) return systemEnv

    val envFiles = listOf(rootProject.file(".env"), rootProject.file("../.env"))
    for (envFile in envFiles) {
        if (!envFile.exists()) continue
        val match = envFile.readLines()
            .map { it.trim() }
            .firstOrNull { it.startsWith("$name=") && !it.startsWith("#") }
        if (match != null) {
            return match.substringAfter("=")
                .trim()
                .trim('"')
                .trim('\'')
        }
    }

    return ""
}

fun buildConfigString(value: String): String =
    "\"${value.replace("\\", "\\\\").replace("\"", "\\\"")}\""

android {
    namespace = "com.example.p2p"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.p2p"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    val baseUrlDebug: String = project.findProperty("BASE_URL_DEBUG") as? String
        ?: "http://157.137.189.178/api/v1/"
    val baseUrlRelease: String = project.findProperty("BASE_URL_RELEASE") as? String
        ?: "http://157.137.189.178/api/v1/"
    val groqApiKey: String = readLocalEnv("GROQ_API_KEY")

    buildTypes {
        debug {
            buildConfigField("String", "BASE_URL", "\"$baseUrlDebug\"")
            buildConfigField("String", "GROQ_API_KEY", buildConfigString(groqApiKey))
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "BASE_URL", "\"$baseUrlRelease\"")
            buildConfigField("String", "GROQ_API_KEY", buildConfigString(groqApiKey))
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

tasks.whenTaskAdded {
    if (name == "assembleDebug") {
        doLast {
            val apkDir = file("build/outputs/apk/debug")
            apkDir.listFiles()?.filter { it.extension == "apk" }?.forEach { apk ->
                apk.renameTo(File(apkDir, "P2PFINAL.apk"))
            }
        }
    }
    if (name == "assembleRelease") {
        doLast {
            val apkDir = file("build/outputs/apk/release")
            apkDir.listFiles()?.filter { it.extension == "apk" }?.forEach { apk ->
                apk.renameTo(File(apkDir, "p2p-release.apk"))
            }
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui.text.google.fonts)
    implementation(libs.navigation.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging)
    implementation(libs.coroutines.android)
    implementation(libs.datastore.preferences)
    implementation(libs.coil.compose)
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("com.google.firebase:firebase-analytics:22.4.0")
    implementation("com.google.firebase:firebase-messaging-ktx:24.1.1")
    implementation("com.google.firebase:firebase-auth-ktx:23.2.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.2")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
