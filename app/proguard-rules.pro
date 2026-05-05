# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# ============== Kahavanu Security Rules ==============

# Preserve line number information for debugging stack traces
-keepattributes SourceFile,LineNumberTable

# Hide the original source file name for obfuscation
-renamesourcefileattribute SourceFile

# ============== Firebase Auth ==============
-keep public class * extends com.google.firebase.**
-keepnames class com.google.firebase.auth.** { *; }
-keepclassmembers class com.google.firebase.auth.** { *; }

# ============== Kotlin ==============
-keepnames class kotlin.** { *; }
-keep class kotlin.** { *; }
-keepclassmembers class kotlin.** { *; }

# Kotlin lambda
-keep class kotlin.jvm.functions.** { *; }

# ============== Jetpack Compose ==============
-keep class androidx.compose.** { *; }
-keepnames class androidx.compose.** { *; }

# ============== Google Auth ==============
-keepnames class com.google.android.gms.auth.** { *; }
-keep class com.google.android.gms.auth.** { *; }

# ============== Google Play Services ==============
-keep class com.google.android.gms.** { *; }
-keepnames class com.google.android.gms.** { *; }

# ============== Google Identity ==============
-keep class com.google.android.libraries.identity.** { *; }
-keepnames class com.google.android.libraries.identity.** { *; }

# ============== Kahavanu App ==============
-keep class com.kahavanu.** { *; }
-keepnames class com.kahavanu.** { *; }

# Keep all public members of our screens, viewmodels, repositories
-keepclassmembers public class com.kahavanu.** {
    public *;
}

# ============== Coroutines ==============
-keepnames class kotlinx.coroutines.** { *; }
-keep class kotlinx.coroutines.** { *; }

# ============== Other Android Libraries ==============
-keep class androidx.** { *; }
-keepnames class androidx.** { *; }

# ============== Security: Remove Debug Logging ==============
# Remove verbose logging in release builds
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Optimize by removing PrintStream calls
-assumenosideeffects class java.io.PrintStream {
    public void println(*);
}