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
        
        val QEV_apiKey = properties.getProperty("QEV_APIKEY") ?: ""
        buildConfigField("String", "QEV_APIKEY", "\"$QEV_apiKey\"")
        val DB_HOST = properties.getProperty("DB_HOST") ?: ""
        buildConfigField("String", "DB_HOST", "\"$DB_HOST\"")
        val DB_PORT = properties.getProperty("DB_PORT") ?: ""
        buildConfigField("String", "DB_PORT", "\"$DB_PORT\"")
        val DB_USER = properties.getProperty("DB_USER") ?: ""
        buildConfigField("String", "DB_USER", "\"$DB_USER\"")
        val DB_PASSWORD = properties.getProperty("DB_PASSWORD") ?: ""
        buildConfigField("String", "DB_PASSWORD", "\"$DB_PASSWORD\"")
        val DB_NAME = properties.getProperty("DB_NAME") ?: ""
        buildConfigField("String", "DB_NAME", "\"$DB_NAME\"")
        val SMTP_HOST = properties.getProperty("SMTP_HOST") ?: ""
        buildConfigField("String", "SMTP_HOST", "\"$SMTP_HOST\"")
        val SMTP_PORT = properties.getProperty("SMTP_PORT") ?: ""
        buildConfigField("String", "SMTP_PORT", "\"$SMTP_PORT\"")
        val SMTP_USER = properties.getProperty("SMTP_USER") ?: ""
        buildConfigField("String", "SMTP_USER", "\"$SMTP_USER\"")
        val SMTP_PASS = properties.getProperty("SMTP_PASS") ?: ""
        buildConfigField("String", "SMTP_PASS", "\"$SMTP_PASS\"")
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/NOTICE.md"
            excludes += "/META-INF/LICENSE.md"
        }
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
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.gson)
    implementation(libs.constraintlayout)
    implementation(libs.mail.android)
    implementation(libs.activation.android)
    implementation(libs.jtds)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.stripe.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.google.android.material:material:1.3.0-alpha03")}