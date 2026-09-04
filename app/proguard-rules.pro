# ─── FITSYNC R8 & PROGUARD OPTIMIZATION RULES ───────────────────────────────

# ── General Optimizations & Line Number Retention ──
-keepattributes SourceFile,LineNumberTable,Signature,InnerClasses,EnclosingMethod,*Annotation*
-repackageclasses ''
-allowaccessmodification

# ── Kotlin Coroutines & Flow ──
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ── AndroidX Compose & Runtime ──
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.material3.** { *; }
-dontwarn androidx.compose.**

# ── Room Database ──
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Dao interface * { *; }
-keep class * implements androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keepclassmembers class * {
    @androidx.room.TypeConverter *;
}
-dontwarn androidx.room.paging.**

# ── Dagger / Hilt Dependency Injection ──
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel
-keep class * extends androidx.lifecycle.ViewModel
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keep @dagger.Module class * { *; }
-keep class dagger.hilt.** { *; }
-keep class * implements dagger.hilt.internal.GeneratedComponent { *; }
-keep class **_Factory { *; }
-keep class **_MembersInjector { *; }

# ── Domain & Data Models (Gson & Kotlinx Serialization) ──
-keep class com.minimize.maximus.domain.model.** { *; }
-keepclassmembers class com.minimize.maximus.domain.model.** { <fields>; }
-keep @kotlinx.serialization.Serializable class * { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.google.gson.** { *; }

# ── Google Gemini Generative AI SDK ──
-keep class com.google.ai.client.generativeai.** { *; }
-dontwarn com.google.ai.client.generativeai.**

# ── Coil Image Loading ──
-keep class coil.** { *; }
-dontwarn coil.**

# ── Airbnb Lottie Animations ──
-keep class com.airbnb.lottie.** { *; }
-dontwarn com.airbnb.lottie.**

# ── Kizitonwose Calendar ──
-keep class com.kizitonwose.calendar.** { *; }
-dontwarn com.kizitonwose.calendar.**

# ── YCharts ──
-keep class co.yml.charts.** { *; }
-dontwarn co.yml.charts.**

# ── Ktor & OkHttp / Retrofit Networking ──
-keep class io.ktor.** { *; }
-keep class okhttp3.** { *; }
-keep class retrofit2.** { *; }
-dontwarn io.ktor.**
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**

# ── Firebase Crashlytics & Remote Config ──
-keep class com.google.firebase.crashlytics.** { *; }
-dontwarn com.google.firebase.crashlytics.**
-keep class com.google.firebase.remoteconfig.** { *; }
-dontwarn com.google.firebase.remoteconfig.**