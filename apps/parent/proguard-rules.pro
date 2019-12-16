-keepattributes SourceFile,LineNumberTable # Needed to avoid mangled Route patterns

# Instructure
-keep public class com.instructure.** { *; }
-keep public class instructure.** { *; }
-dontwarn com.instructure.**

# androidsvg
-dontwarn com.caverock.androidsvg.**
-keep public class com.caverock.androidsvg.** { *; }

# Retrofit, OkHttp, Gson
-keep class retrofit.** { *; }
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-dontwarn com.squareup.okhttp.**
-keep class sun.misc.Unsafe { *; }
-dontwarn retrofit.**
-dontwarn java.nio.file.*
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn okhttp3.**

# Simple XML
-dontwarn org.simpleframework.xml.stream.**
-keep public class org.simpleframework.** { *; }
-keep class org.simpleframework.xml.** { *; }
-keep class org.simpleframework.xml.core.** { *; }
-keep class org.simpleframework.xml.util.** { *; }
-keep class javax.xml.stream.** { *; }

# Eventbus
-keepattributes *Annotation*
-keepclassmembers class * {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# Kotlin
-dontwarn kotlinx.atomicfu.AtomicBoolean

# CameraKit
-dontwarn com.google.android.gms.**
