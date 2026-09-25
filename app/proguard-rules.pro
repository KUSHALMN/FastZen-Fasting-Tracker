# Proguard and R8 optimization rules for FastZen release builds

# Google Mobile Ads (AdMob)
-keep public class com.google.android.gms.ads.** {
   public *;
}
-keepclassmembers class * extends com.google.android.gms.ads.AdView {
   *;
}
-keep public class com.google.ads.** {
   public *;
}

# Keep FastZen data and JSON persistence models
-keep class com.example.model.** { *; }
-keepclassmembers class com.example.model.** {
    <fields>;
    <init>(...);
}

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
