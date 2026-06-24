# WallShift ProGuard rules

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }

# Keep Room entities
-keep class com.vynik.wallshift.data.model.** { *; }

# Keep WorkManager workers
-keep class com.vynik.wallshift.worker.** { *; }

# Keep data classes for serialization
-keepclassmembers class com.vynik.wallshift.** {
    public <init>(...);
}

# Coil
-dontwarn coil.**

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
