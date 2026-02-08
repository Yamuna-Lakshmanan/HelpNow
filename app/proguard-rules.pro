# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# aaptOptions setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# --- Rules from the 'main' branch ---
# Keep Gson classes
-keep class com.google.gson.** { *; }
-keep class com.helpnow.models.** { *; }

# Keep data classes that use Gson annotations
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# --- Rule from the 'venish' branch ---
# Keep all classes in the app's main package
-keep class com.helpnow.app.** { *; }
