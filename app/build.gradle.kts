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

        val STRIPE_SK = properties.getProperty("STRIPE_SK") ?: ""
        buildConfigField("String", "STRIPE_SK", "\"$STRIPE_SK\"")
        val STRIPE_PK = properties.getProperty("STRIPE_PK") ?: ""
        buildConfigField("String", "STRIPE_PK", "\"$STRIPE_PK\"")
        val STRIPE_RK = properties.getProperty("STRIPE_RK") ?: ""
        buildConfigField("String", "STRIPE_RK", "\"$STRIPE_RK\"")
        val BREVO_apiKey = properties.getProperty("BREVO_APIKEY") ?: ""
        buildConfigField("String", "BREVO_APIKEY", "\"$BREVO_apiKey\"")
        val QEV_apiKey = properties.getProperty("QEV_APIKEY") ?: ""
        buildConfigField("String", "QEV_APIKEY", "\"$QEV_apiKey\"")
        val DB_HOST = properties.getProperty("DB_HOST") ?: ""
        buildConfigField("String", "DB_HOST", "\"$DB_HOST\"")
        val DB_PORT = properties.getProperty("DB_PORT") ?: ""
        buildConfigField("int", "DB_PORT", "\"$DB_PORT\"")
        val DB_USER = properties.getProperty("DB_USER") ?: ""
        buildConfigField("String", "DB_USER", "\"$DB_USER\"")
        val DB_PASSWORD = properties.getProperty("DB_PASSWORD") ?: ""
        buildConfigField("String", "DB_PASSWORD", "\"$DB_PASSWORD\"")
        val DB_NAME = properties.getProperty("DB_NAME") ?: ""
        buildConfigField("String", "DB_NAME", "\"$DB_NAME\"")


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
    implementation(libs.gson)
    implementation(libs.constraintlayout)
    implementation(libs.brevo)
    implementation(libs.jtds)
    implementation(libs.stripe.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}