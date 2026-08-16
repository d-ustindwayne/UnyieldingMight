import java.util.Properties;

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.unyieldingmight"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.unyieldingmight"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        val properties = Properties().apply {
            val propertiesFile = project.rootProject.file("local.properties")
            if (propertiesFile.exists()) {
                load(propertiesFile.inputStream())
            }
        }

        val BREVO_apiKey = properties.getProperty("BREVO_APIKEY") ?: ""
        buildConfigField("String", "BREVO_APIKEY", "\"$BREVO_apiKey\"")
        val QEV_apiKey = properties.getProperty("QEV_APIKEY") ?: ""
        buildConfigField("String", "QEV_APIKEY", "\"$QEV_apiKey\"")

        // Optional: If you need it in AndroidManifest.xml
        manifestPlaceholders["QEV_APIKEY"] = QEV_apiKey
    }

    buildFeatures {
        buildConfig = true
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
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.brevo)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}