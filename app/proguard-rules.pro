# Reflexion for test if service is running
-keepnames class org.neige.wakeyouinmusic.android.services.AlarmService { *; }

# Models
-keepclassmembers class org.neige.wakeyouinmusic.android.models.** { *; }

# Crashlytics
-keep class com.crashlytics.** { *; }
-keepattributes SourceFile,LineNumberTable

# Android Support
-keep class android.support.v4.app.** { *; }
-keep interface android.support.v4.app.** { *; }
-keep class android.support.v7.app.** { *; }
-keep interface android.support.v7.app.** { *; }

# Deezer
-keep class com.deezer.** { *; }
-keep interface com.deezer.** { *; }
-keep enum com.deezer.** { *; }

# Spotify
-keep class com.spotify.** { public protected private *; }
-keep class org.neige.wakeyouinmusic.android.spotify.models.** { *; }

# RetroLambda
-dontwarn java.lang.invoke.*

# RxAndroid
-dontwarn sun.misc.Unsafe

# OkHttp
-dontwarn com.squareup.okhttp.**
-dontwarn okio.**

# Retrofit
-dontwarn com.google.appengine.api.urlfetch.*
-keepattributes *Annotation*,Signature
-keep class retrofit.** { *; }
-keepclasseswithmembers class * { @retrofit.http.* <methods>; }

# Google Apis
-keepattributes Signature,RuntimeVisibleAnnotations,AnnotationDefault
-keepclassmembers class * { @com.google.api.client.util.Key <fields>; }
-dontwarn com.google.api.client.extensions.android.**
-dontwarn com.google.api.client.googleapis.extensions.android.**
-dontwarn com.google.android.gms.**