plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.dreamkeeper"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.dreamkeeper"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("com.github.jolenechong:androidWordCloud:1.0.0") {
        // exclude due to duplicate classes with the
        // edu.stanford.nlp:stanford-corenlp dependency for data processing
        exclude(group="com.sun.xml.bind", module="jaxb-core")
        exclude(group="com.sun.xml.bind", module="jaxb-impl")
    }
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.ui)
    implementation(libs.navigation.fragment)
    implementation(libs.legacy.support.v4)
    implementation(libs.recyclerview)
    implementation(libs.preference)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

}
