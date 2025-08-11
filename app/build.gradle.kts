import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.serialization)
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { stream ->
        localProperties.load(stream)
    }
}

android {
    namespace = "com.example.myentrance"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.myentrance"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "com.example.myentrance.HiltTestRunner"

        buildConfigField("String", "SUPABASE_URL", "\"${localProperties.getProperty("SUPABASE_URL") ?: ""}\"")
        buildConfigField("String", "SUPABASE_KEY", "\"${localProperties.getProperty("SUPABASE_KEY") ?: ""}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    // Конфигурация для тестирования
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
    packaging {
        resources.excludes.addAll(
            listOf(
                "META-INF/LICENSE.md",
                "META-INF/LICENSE-notice.md",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt"
            )
        )
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    implementation(libs.play.services.fido)

    // Базовые тесты
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.firebase.firestore)

    // Glide
    implementation(libs.glide)
    ksp(libs.glide.ksp)

    // Supabase с BOM
    implementation(platform("io.github.jan-tennert.supabase:bom:3.2.2"))
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:storage-kt")

    implementation(libs.ktor.client.android)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // Coil
    implementation(libs.coil)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Hilt зависимости
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.fragment)


    implementation(libs.mockk)
    // Для Unit test'ов
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.turbine)
    testImplementation(libs.hilt.android.testing)

    // Для тестирования Navigation
    androidTestImplementation(libs.androidx.navigation.testing)

    // Основная зависимость для HiltTestApplication
    androidTestImplementation(libs.hilt.android.testing)

    // Компилятор для обработки аннотаций
    kspAndroidTest(libs.hilt.compiler)

}