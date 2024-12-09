-keep,allowobfuscation,allowshrinking public class fr.labomg.biophonie.core.network.ResultCall {*;}
-keep,allowobfuscation,allowshrinking public class fr.labomg.biophonie.core.network.ResultCallAdapterFactory {*;}
-keep,allowobfuscation,allowshrinking public class kotlin.Result {*;}

-keepclassmembers class fr.labomg.biophonie.core.network.model.NetworkUser {
  @com.squareup.moshi.FromJson *;
  @com.squareup.moshi.ToJson *;
}