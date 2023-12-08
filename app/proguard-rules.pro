# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-keepattributes SourceFile,LineNumberTable
# Uncomment te generate mapping file
#-printmapping outputfile.txt

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# With R8 full mode generic signatures are stripped for classes that are not
# kept. Suspend functions are wrapped in continuations where the type argument
# is used.
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-keepclassmembers class fr.labomg.biophonie.data.source.remote.NetworkUser {
  @com.squareup.moshi.FromJson *;
  @com.squareup.moshi.ToJson *;
}

-keep,allowobfuscation,allowshrinking public class fr.labomg.biophonie.data.source.ResultCall {*;}
-keep,allowobfuscation,allowshrinking public class fr.labomg.biophonie.data.source.ResultCallAdapterFactory {*;}
-keep,allowobfuscation,allowshrinking public class kotlin.Result {*;}

# solves issue with xml inflation with `app:` parameters
-dontwarn org.xmlpull.v1.**
-dontwarn org.kxml2.io.**
-dontwarn android.content.res.**
-keep class org.xmlpull.** { *; }
-keepclassmembers class org.xmlpull.** { *; }

# solves issue with xml inflation with navigator (TODO: update might fix)
-keepattributes RuntimeVisibleAnnotations
-keep class * extends androidx.navigation.Navigator