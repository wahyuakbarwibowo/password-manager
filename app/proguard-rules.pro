# Aminmart Password Manager - ProGuard Rules
# Optimized for small APK size while maintaining functionality

# ===========================================
# General optimizations
# ===========================================
-dontoptimize
-dontobfuscate

# ===========================================
# Hilt (Dependency Injection)
# ===========================================
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class hilt_aggregated_deps.** { *; }
-keep class * extends dagger.hilt.android.AndroidEntryPoint { *; }
-keep class * extends dagger.hilt.android.internal.managers.ComponentSupplier { *; }
-keep class * extends dagger.hilt.android.internal.lifecycle.HiltViewModelFactory { *; }
-keep class dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-dontwarn dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper

# ===========================================
# Kotlin
# ===========================================
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepattributes *Annotation*, InnerClasses
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# ===========================================
# Biometric
# ===========================================
-keep class androidx.biometric.** { *; }

# ===========================================
# Keep model classes
# ===========================================
-keep class com.aminmart.passwordmanager.domain.model.** { *; }
-keep class com.aminmart.passwordmanager.data.local.** { *; }
-keep class com.aminmart.passwordmanager.data.security.** { *; }
-keep class com.aminmart.passwordmanager.data.repository.** { *; }

# ===========================================
# Compose
# ===========================================
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
-keepclassmembers,allowobfuscation class * extends androidx.compose.runtime.Composer {
    void <init>(java.lang.Object...);
}

# ===========================================
# Remove logging for release builds
# ===========================================
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
