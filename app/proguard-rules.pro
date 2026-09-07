# Preserve generic signatures and runtime annotations used by Retrofit and Gson.
-keepattributes Signature,InnerClasses,EnclosingMethod,Exceptions
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations,AnnotationDefault

# Gson reflects over API DTO fields. Some legacy DTOs do not annotate every field with
# @SerializedName, so their Java field names are part of the wire format. Keep those members and
# their names while still allowing R8 to optimize the containing classes.
-keepclassmembers,allowoptimization class Ir.co.tfs.farazaman.data.model.** {
    <fields>;
}

# Retrofit reads method annotations and Kotlin suspend generic signatures at runtime.
# Optimizing/shrinking these interfaces can turn Continuation<Response<T>> into a raw type,
# which crashes only in minified builds when Retrofit casts it to ParameterizedType.
-keep interface Ir.co.tfs.farazaman.service.remote.** { *; }
-keep interface Ir.co.tfs.farazaman.data.api.** { *; }

# R8 full mode otherwise treats Continuation as non-instantiable and removes the generic
# Continuation<? super Response<T>> signature that Retrofit uses for suspend methods.
-keep class kotlin.coroutines.Continuation { *; }

# A suspend Retrofit method encodes Response<T> inside Continuation's generic signature.
# In R8 full mode the Response type must remain instantiated, otherwise Retrofit sees a raw
# Response in Release and fails before creating the HTTP request.
-keep,allowoptimization,allowshrinking,allowobfuscation class retrofit2.Response

# Keep the response type reached only through Retrofit method signatures.
-if interface ** { @retrofit2.http.* public *** *(...); }
-keep,allowoptimization,allowshrinking,allowobfuscation class <3>

-keepclassmembers class Ir.co.tfs.farazaman.data.model.** {
    <init>(...);
}

# Authentication is security-sensitive and differs between password grant and AppAuth.
# Keep this small flow stable across debug and minified release builds.
-keep class Ir.co.tfs.farazaman.auth.** { *; }
-keep class Ir.co.tfs.farazaman.data.repository.AuthRepositoryImpl { *; }
-keep class Ir.co.tfs.farazaman.domain.repository.AuthRepository { *; }
-keep class Ir.co.tfs.farazaman.domain.usecase.LoginUseCase { *; }
-keep class Ir.co.tfs.farazaman.domain.model.LoginResult** { *; }
-keep class Ir.co.tfs.farazaman.data.model.res.LoginResponse { *; }
-keep class Ir.co.tfs.farazaman.util.TokenManager { *; }
-keep class Ir.co.tfs.farazaman.util.SecureTokenStorage { *; }
-keep class Ir.co.tfs.farazaman.util.AndroidKeyStoreCipher { *; }
-keep class net.openid.appauth.** { *; }

# SignalR/Gson constructs realtime payloads reflectively in minified Release builds.
# SignalR resolves collection payloads from anonymous TypeReference subclasses at runtime.
# R8 must keep both the subclass and its generic signature; otherwise Release crashes with
# "TypeReference must be instantiated with a type parameter" during app lifecycle callbacks.
-keep class com.microsoft.signalr.TypeReference { *; }
-keep class ** extends com.microsoft.signalr.TypeReference { *; }
-keep class Ir.co.tfs.farazaman.realtime.MobileSupervisorHubClient$* { *; }

# Apply the same protection to Gson TypeToken subclasses used by current or future features.
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class ** extends com.google.gson.reflect.TypeToken { *; }

-keep class Ir.co.tfs.farazaman.realtime.MobileChatMessage { *; }
-keep class Ir.co.tfs.farazaman.realtime.LocationRequest { *; }
-keep class Ir.co.tfs.farazaman.realtime.LocationResponse { *; }
-keep class Ir.co.tfs.farazaman.realtime.LocationRejected { *; }
-keep class Ir.co.tfs.farazaman.realtime.OnlineMobileUser { *; }
