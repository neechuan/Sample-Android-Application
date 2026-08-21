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

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Preserve AppDynamics Agent SDK classes and interfaces
-keep class com.appdynamics.eumagent.runtime.** { *; }
-keep interface com.appdynamics.eumagent.runtime.** { *; }

# Preserve custom classes, interfaces, and members annotated with @DontObfuscate
-keep @com.appdynamics.eumagent.runtime.DontObfuscate class * { *; }
-keep @com.appdynamics.eumagent.runtime.DontObfuscate interface * { *; }
-keepclassmembers class * {
    @com.appdynamics.eumagent.runtime.DontObfuscate *;
}

# Preserve classes and methods targeted by AppDynamics Info Points
-keep class com.appdynamics.sampleandroidapplication.data.TodoRepository {
    public *;
}
-keep class com.appdynamics.sampleandroidapplication.MainActivity {
    public *;
    protected *;
}
-keep class com.appdynamics.sampleandroidapplication.model.TodoItem {
    public *;
}

# Preserve stack trace line numbers and annotations
-keepattributes SourceFile,LineNumberTable,*Annotation*,Signature
