# Reglas de ProGuard/R8 para el build de release.
#
# El archivo no existia, pero app/build.gradle.kts lo referencia en
# buildTypes.release -> proguardFiles(...). Con isMinifyEnabled = false no se
# aplica, asi que hoy no rompe; en cuanto se active la minificacion, hara falta.

# Modelos que Firebase Realtime Database deserializa por reflexion. Sin esto,
# R8 renombra los campos y los productos vuelven vacios del servidor.
-keepclassmembers class com.example.crudprducts1.** {
    <init>();
    <fields>;
}

# Supabase-kt y Ktor usan kotlinx.serialization, que depende de metadatos.
-keepattributes *Annotation*, InnerClasses, Signature
-dontwarn kotlinx.serialization.**
-keep,includedescriptorclasses class kotlinx.serialization.** { *; }

# Ktor elige el motor HTTP en tiempo de ejecucion mediante ServiceLoader.
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**
-keep class org.slf4j.** { *; }
-dontwarn org.slf4j.**
