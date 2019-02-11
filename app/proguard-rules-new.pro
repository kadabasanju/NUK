-keepattributes *Annotation*, Exceptions, Signature, Deprecated, SourceFile, SourceDir, LineNumberTable, LocalVariableTable, LocalVariableTypeTable, Synthetic, EnclosingMethod, RuntimeVisibleAnnotations, RuntimeInvisibleAnnotations, RuntimeVisibleParameterAnnotations, RuntimeInvisibleParameterAnnotations, AnnotationDefault, InnerClasses

-ignorewarnings

-dontwarn io.realm.processor.**
-dontwarn org.apache.commons.**

-dontshrink
-dontoptimize
-dontpreverify
-verbose
-dontskipnonpubliclibraryclassmembers
-dontskipnonpubliclibraryclasses

-dontwarn javax.management.**
-dontwarn java.lang.management.**
-dontwarn org.apache.log4j.**
-dontwarn org.apache.commons.logging.**
-dontwarn org.slf4j.**
-dontwarn org.json.**
-dontwarn org.apache.http.**
-dontwarn android.net.http.AndroidHttpClient
-dontwarn com.google.android.gms.**
-dontwarn com.android.volley.toolbox.**




-keep,allowobfuscation public class * extends android.app.Activity
-keep, allowobfuscation public class * extends android.app.Application
-keep, allowobfuscation public class * extends android.app.Service
-keep, allowobfuscation public class * extends android.content.BroadcastReceiver
-keep, allowobfuscation public class * extends android.content.ContentProvider
-keep, allowobfuscation public class * extends android.app.backup.BackupAgentHelper
-keep, allowobfuscation public class * extends android.preference.Preference
-keep, allowobfuscation public class com.android.vending.licensing.ILicensingService
-keep class javax.** { *; }
-keep class org.** { *; }
-keep class com.google.** {*;}
-keep class org.apache.http.**{*;}
-keep class org.json.**{*;}
-keep class models.** { *; }
-keepclassmembers class models.** { *; }
-keepclassmembers class dev.sutd.hdb.**{*;}
-keep class com.google.android.gms.maps.** { *; }
-keep interface com.google.android.gms.maps.** { *; }

-keepclassmembers class * implements android.os.Parcelable {
    static *** CREATOR;
}

# The Maps API uses serialization.
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-keep @io.realm.annotations.RealmModule class *
-dontwarn javax.**
-dontwarn io.realm.**

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}


-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
