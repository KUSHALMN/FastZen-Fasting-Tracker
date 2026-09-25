plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.aistudio.fastzen.kpmxlo"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            manifestPlaceholders["admobAppId"] = "ca-app-pub-3940256099942544~3347511713"
            buildConfigField("String", "ADMOB_BANNER_AD_UNIT_ID", "\"ca-app-pub-3940256099942544/6300978111\"")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Production AdMob configuration: can be supplied via gradle property or environment variable
            // Defaults to placeholder to ensure Google test IDs NEVER ship in production builds
            val prodAppId = (project.findProperty("FASTZEN_ADMOB_APP_ID") as? String)
                ?: System.getenv("FASTZEN_ADMOB_APP_ID")
                ?: "ca-app-pub-0000000000000000~0000000000"
            val prodBannerId = (project.findProperty("FASTZEN_ADMOB_BANNER_ID") as? String)
                ?: System.getenv("FASTZEN_ADMOB_BANNER_ID")
                ?: ""
            manifestPlaceholders["admobAppId"] = prodAppId
            buildConfigField("String", "ADMOB_BANNER_AD_UNIT_ID", "\"$prodBannerId\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.play.services.ads)
}
