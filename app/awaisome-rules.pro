-keepattributes Exceptions, InnerClasses, Signature, Deprecated, SourceFile, LineNumberTable, *Annotation*, EnclosingMethod

-keep class **.R
-keep class **.R { *; }
-keep class **.R$* { public static final int *; }
-keep class **$Properties

-keep,allowoptimization public class androidx.webkit.**
-keep,allowoptimization class androidx.webkit.**
-keep,allowoptimization public class * extends androidx.webkit.**
-keep,allowoptimization class * extends androidx.webkit.**
-keep,allowoptimization public class * extends androidx.multidex.**
-keep,allowoptimization class * extends androidx.multidex.**
-keep,allowoptimization public class androidx.appcompat.widget.** { *; }
-keep,allowoptimization class androidx.appcompat.widget.** { *; }

-keepclassmembernames,allowoptimization class * implements java.lang.annotation.Annotation
-keepclassmembernames,allowoptimization public class * extends android.database.ContentObserver
-keepnames public class android.webkit.*
-keep public class * extends android.app.Service
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.content.BroadcastReceiver
-keep public class * implements android.os.IInterface
-keep,allowobfuscation,allowoptimization interface <1>

-keep public class * implements android.os.Parcelable { public static final *; }
-keepclassmembers class * { @android.webkit.JavascriptInterface <methods>; }
-keepclassmembers public class * { @android.webkit.JavascriptInterface <methods>; }
-keepclassmembernames public class * extends android.view.View {
    *** get*();
    void set*(***);
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclassmembernames,allowoptimization public class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
-keepclassmembers,allowoptimization enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class com.google.android.gms.common.api.GoogleApiClient { public *; }
-keep class com.google.android.gms.common.api.GoogleApiClient { *; }
-keep class com.google.android.gms.common.api.GoogleApiClient$* { public *; }
-keep class com.google.android.gms.common.api.GoogleApiClient$* { *; }
-keep class com.google.android.gms.location.LocationServices { public *; }
-keep class com.google.android.gms.location.LocationServices { *; }
-keep class com.google.android.gms.location.FusedLocationProviderApi { public *; }
-keep class com.google.android.gms.location.FusedLocationProviderApi { *; }
-keep class com.google.android.gms.location.ActivityRecognition { public *; }
-keep class com.google.android.gms.location.ActivityRecognition { *; }
-keep class com.google.android.gms.location.ActivityRecognitionApi { public *; }
-keep class com.google.android.gms.location.ActivityRecognitionApi { *; }
-keep class com.google.android.gms.location.ActivityRecognitionResult { public *; }
-keep class com.google.android.gms.location.ActivityRecognitionResult { *; }
-keep class com.google.android.gms.location.DetectedActivity { public *; }
-keep class com.google.android.gms.location.DetectedActivity { *; }
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient { public *; }
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient { *; }
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info { public *; }
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info { *; }


#### start Ad Adapters ####
-keep class com.adcolony.sdk.**
-keep class com.adcolony.sdk.** { *; }
-keep class com.facebook.ads.**
-keep class com.facebook.ads.** { *; }
-keep class com.chartboost.**
-keep class com.chartboost.** { *; }
-keep class com.truenet.**
-keep class com.truenet.** { *; }
-keep class com.startapp.**
-keep class com.startapp.** { *; }
-keep class com.applovin.**
-keep class com.applovin.** { *; }
-keep class com.iab.omid.**
-keep class com.iab.omid.** { *; }
-keep class com.appodeal.**
-keep class com.appodeal.** { *; }
-keep class org.nexage.**
-keep class org.nexage.** { *; }
-keep class com.inmobi.**
-keep class com.inmobi.** { *; }
-keep class com.moat.**
-keep class com.moat.** { *; }
-keep class com.iab.**
-keep class com.iab.** { *; }

-keepattributes Exceptions, InnerClasses, Signature, Deprecated, SourceFile, LineNumberTable, *Annotation*, EnclosingMethod

-dontwarn com.adcolony.sdk.**
-dontwarn com.facebook.ads.**
-dontwarn com.chartboost.**
-dontwarn com.startapp.**
-dontwarn com.applovin.**
-dontwarn com.iab.omid.**
-dontwarn com.appodeal.**
-dontwarn org.nexage.**
-dontwarn com.inmobi.**
-dontwarn com.moat.**
-dontwarn com.iab.**
#### end Ad Adapters ####

################## GOOGLE ##################
-keep class com.google.ads.** { *; }
-keep class com.google.gson.** { *; }
-keep class com.google.android.** { *; }
-keep class com.google.firebase.** { *; }
-keep class com.google.googlesignin.** { *; }
-keep class com.google.android.gms.** { *; }
-keep public class com.google.android.gms.** { public protected *; }
-keep class com.google.common.** { *; }
-keep class com.crashlytics.** { *; }

-dontwarn com.google.ads.**
-dontwarn com.google.common.**
-dontwarn com.google.android.gms.**

#noinspection ShrinkerUnresolvedReference
################## GOOGLE SERVICES ##################
-keep class com.google.android.gms.dynamite.DynamiteModule {*;}
-keep class com.google.android.gms.common.GooglePlayServicesRepairableException {*;}
-keep class com.google.android.gms.common.GooglePlayServicesNotAvailableException {*;}
-keep class * extends java.util.ListResourceBundle { protected *[][] getContents(); }
-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable { public static final *** NULL; }

-keepclassmembernames class * { @com.google.android.gms.common.annotation.KeepName *; }
-keepclassmembernames class * { @com.google.android.gms.common.annotation.KeepForSdk *; }
-keepclasseswithmembers class * { @com.google.android.gms.common.annotation.KeepName <fields>; }
-keepclasseswithmembers class * { @com.google.android.gms.common.annotation.KeepName <methods>; }
-keepclasseswithmembers class * { @com.google.android.gms.common.annotation.KeepForSdk <fields>; }
-keepclasseswithmembers class * { @com.google.android.gms.common.annotation.KeepForSdk <methods>; }

################## OTHERS ##################
-keep class okhttp3.** { *; }
-keep class retrofit2.** { *; }
-keep class org.apache.** { *; }
-keep class com.shaded.fasterxml.jackson.** { *; }
-keepnames class com.fasterxml.jackson.** { *; }
-keepnames class javax.servlet.** { *; }
-keepnames class org.ietf.jgss.** { *; }
-if interface * { @retrofit2.http.* <methods>; }
-keepclasseswithmembers class * { @retrofit2.http.* <methods>; }
-keepclassmembernames interface * { @retrofit2.http.* <methods>; }

-dontwarn com.crashlytics.**
-dontwarn rx.*
-dontwarn okio.**
-dontwarn org.apache.**
-dontwarn com.squareup.okhttp3.**
-dontwarn retrofit.**
-dontwarn retrofit2.**
-dontwarn java.lang.invoke.*
-dontwarn org.w3c.dom.**
-dontwarn org.joda.time.**
-dontwarn org.shaded.apache.**
-dontwarn org.ietf.jgss.**
-dontwarn org.jetbrains.annotations.**
-dontwarn android.webkit.WebView
-dontwarn android.webkit.JavascriptInterface
-dontwarn android.security.NetworkSecurityPolicy
