plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.crudprducts1"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.crudprducts1"
        minSdk = 26
        targetSdk = 34

        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
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

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

}

dependencies {
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Asegúrate de tener estas versiones base actualizadas y compatibles con Compose
    implementation("androidx.core:core-ktx:1.12.0") // o superior
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    implementation(libs.androidx.navigation.compose)

    // Habia un bloque `dependencies { }` anidado dentro de este, con un SEGUNDO BOM de
    // Firebase (33.9.0) compitiendo con el del catalogo (33.1.0). Aplanado a un solo BOM.
    implementation(platform(libs.firebase.bom))
    implementation("com.google.firebase:firebase-database")
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)

    implementation(libs.coil.compose)

    // Supabase Storage. `util/SupabaseClient.kt` y `ProductRepository` lo usan para
    // subir, publicar y borrar las imagenes de los productos, pero no habia ninguna
    // dependencia declarada: cinco referencias sin resolver al compilar.
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.storage)
    implementation(libs.ktor.client.android)

    // `Task.await()`: deja esperar las operaciones de Firebase dentro de una corrutina
    // en vez de encadenar callbacks. Lo usa deleteProduct para borrar las imagenes del
    // bucket ANTES de eliminar el registro.
    implementation(libs.coroutines.play.services)

    debugImplementation(libs.androidx.compose.ui.tooling)
}