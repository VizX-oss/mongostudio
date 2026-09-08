# ProGuard configuration for MongoStudio
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.naming.**
-dontwarn javax.security.sasl.**
-keep class javax.security.sasl.** { *; }
-dontwarn com.mongodb.**
-dontwarn org.bson.**
-keep class com.mongodb.** { *; }
-keep class org.bson.** { *; }


