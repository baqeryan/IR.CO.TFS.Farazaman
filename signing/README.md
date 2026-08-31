# FaraZaman Release signing

`farazaman-release.jks` is the permanent Android Release signing identity.
Gradle reads its credentials from `release-signing.properties`.

Keep an offline backup of this entire directory. Losing the keystore or its
passwords prevents future APKs from updating installations signed with this key.
Changing the key also requires uninstalling previously installed builds unless
an app-store signing/key-rotation process is used.

Build the signed Release APK on Windows with:

```powershell
.\gradlew.bat :app:assembleRelease "-Dorg.gradle.jvmargs=-Xmx2g -XX:MaxMetaspaceSize=768m" --no-daemon --no-parallel --max-workers=1
```

The signed artifact is written to:

`app/build/outputs/apk/release/app-release.apk`

.\gradlew.bat :app:assembleRelease "-Dorg.gradle.jvmargs=-Xmx2g -XX:MaxMetaspaceSize=768m" --no-daemon --no-parallel --max-workers=1
