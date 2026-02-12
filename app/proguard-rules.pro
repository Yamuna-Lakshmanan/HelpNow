proguard
# Keep the Application class to prevent ClassNotFoundException
-keep class com.helpnow.app.HelpNowApplication { *; }

# Keep Gson and Models
-keep class com.google.gson.** { *; }
-keep class com.helpnow.models.** { *; }

# Keep data classes that use Gson annotations
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep Coroutines for background initialization
-keep class kotlinx.coroutines.** { *; }
