import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// Параметры подписи берутся из переменных окружения (CI), а при их отсутствии —
// из локального keystore.properties. Оба источника не попадают в git.
val keystoreProperties = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}

fun signingValue(property: String, environment: String): String? =
    providers.environmentVariable(environment).orNull?.takeIf(String::isNotBlank)
        ?: keystoreProperties.getProperty(property)?.takeIf(String::isNotBlank)

val releaseStoreFile = signingValue("storeFile", "KEYSTORE_FILE")
val releaseStorePassword = signingValue("storePassword", "KEYSTORE_PASSWORD")
val releaseKeyAlias = signingValue("keyAlias", "KEY_ALIAS")
val releaseKeyPassword = signingValue("keyPassword", "KEY_PASSWORD")
val hasReleaseSigning = listOf(
    releaseStoreFile,
    releaseStorePassword,
    releaseKeyAlias,
    releaseKeyPassword,
).all { it != null }

// Версию можно передать из CI (-PversionName/-PversionCode). Без параметров берётся
// git-тег, указывающий ровно на текущий коммит (например, v1.3 → 1.3). Если коммит
// не отмечен тегом, используются значения по умолчанию.
fun versionCodeOf(name: String): Int {
    val parts = name.substringBefore('-').split('.')
    val major = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val minor = parts.getOrNull(1)?.toIntOrNull() ?: 0
    val patch = parts.getOrNull(2)?.toIntOrNull() ?: 0
    return major * 10000 + minor * 100 + patch
}

val gitTag = try {
    providers.exec {
        commandLine("git", "describe", "--tags", "--exact-match", "HEAD")
        workingDir = rootDir
        isIgnoreExitValue = true
    }.standardOutput.asText.get().trim()
} catch (_: Exception) {
    ""
}

val gitVersionName = gitTag
    .removePrefix("v")
    .takeIf { it.matches(Regex("""^[0-9]+(\.[0-9]+){0,2}(-[0-9A-Za-z.-]+)?$""")) }

val buildVersionName = providers.gradleProperty("versionName").orNull?.takeIf(String::isNotBlank)
    ?: gitVersionName
    ?: "1.2"
val buildVersionCode = providers.gradleProperty("versionCode").orNull?.toIntOrNull()
    ?: versionCodeOf(buildVersionName)

android {
    namespace = "com.smekhnyov.carts"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.smekhnyov.carts"
        minSdk = 31
        targetSdk = 37
        versionCode = buildVersionCode
        versionName = buildVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = rootProject.file(releaseStoreFile!!)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.mlkit.barcode.scanning)
    implementation(libs.zxing.core)

    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
