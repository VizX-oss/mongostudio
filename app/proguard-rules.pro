# ───────────────────────────────────────────────
# MongoStudio ProGuard / R8 Rules
# ───────────────────────────────────────────────

# Preserve annotations, generics, and stack traces
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod, SourceFile, LineNumberTable
-renamesourcefileattribute SourceFile

# ── GSON ──────────────────────────────────────
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# ── MongoDB Java Driver ────────────────────────
-dontwarn com.mongodb.**
-dontwarn org.bson.**
-keep class com.mongodb.** { *; }
-keep class org.bson.** { *; }
-keep class com.mongodb.client.model.** { *; }

# ── OkHttp (used internally by driver) ────────
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# ── Java security / SASL (needed by MongoDB TLS) ──
-dontwarn javax.naming.**
-dontwarn javax.security.sasl.**
-dontwarn javax.management.**
-keep class javax.security.sasl.** { *; }
-keep class javax.net.ssl.** { *; }
-keep class java.security.** { *; }

# ── Kotlin Coroutines ──────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** { volatile <fields>; }

# ── App Model Classes (serialized via GSON) ───
-keep class com.mongostudio.app.data.model.** { *; }
-keep class com.mongostudio.app.data.preferences.** { *; }

# ── Compose / AndroidX ────────────────────────
-dontwarn androidx.compose.**
