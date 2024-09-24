plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    kotlin("android") // Adicionando o plugin kotlin-android
    kotlin("kapt") // Adicionando o plugin kotlin-kapt
}

val kotlin_version = "1.5.31"

android {
    namespace = "com.example.diariodofiscal"
    compileSdk = 34
    buildFeatures {
        dataBinding = true
    }
    defaultConfig {
        applicationId = "com.example.diariodofiscal"
        minSdk = 21
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true // Habilitando o View Binding
    }
    packagingOptions {
        exclude ("META-INF/LICENSE.md")
        exclude ("META-INF/NOTICE.md")
    }
}


dependencies {
    androidTestImplementation("androidx.test:runner:1.3.0")
    androidTestImplementation("androidx.test:rules:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.3.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    implementation ("com.sun.mail:android-mail:1.6.7")
    implementation ("com.sun.mail:android-activation:1.6.7")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.itextpdf:itext7-core:7.1.15")
    implementation("com.github.bumptech.glide:glide:4.12.0")
    implementation("com.firebaseui:firebase-ui-firestore:7.2.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")
    implementation("com.google.firebase:firebase-messaging:23.0.0")
    implementation("com.squareup.picasso:picasso:2.71828")
    implementation("androidx.room:room-runtime:2.4.0")
    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("com.google.android.material:material:1.4.0")
    annotationProcessor("androidx.room:room-compiler:2.4.0")
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    kapt("androidx.room:room-compiler:2.4.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version")
    implementation("androidx.core:core-ktx:1.6.0")
    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("com.github.bumptech.glide:glide:4.12.0")
    kapt("com.github.bumptech.glide:compiler:4.12.0")
    implementation("com.google.firebase:firebase-core:21.1.1")
    implementation("com.google.firebase:firebase-storage:20.0.0")
    implementation("com.google.firebase:firebase-firestore:23.0.3")
    implementation(platform("com.google.firebase:firebase-bom:32.7.1"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.core:core:1.6.0")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-storage-ktx:20.3.0")
    testImplementation("junit:junit:4.13.2")
}
