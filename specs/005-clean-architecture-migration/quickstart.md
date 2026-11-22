# Quickstart: اعتبارسنجی هر برش معماری

1. architecture dependency tests و unit tests ماژول تغییرکرده را اجرا کنید.
2. `./gradlew :app:assembleDebug` و smoke گردش user story را اجرا کنید.
3. برای auth/network/DTO، `./gradlew :app:lintRelease :app:assembleRelease` و login + first authenticated API را روی APK minified تست کنید.
4. برای GPS/map/camera/database، روی دستگاه ARM permission denial، background/foreground، process recreation و داده uploadنشده را بررسی کنید.
5. selected municipality، role separation، Persian RTL/dark mode و عدم ثبت secret در log را تأیید کنید.
6. parity evidence و rollback point را قبل از حذف owner قدیمی در `specs/005-clean-architecture-migration/reviews/` ثبت کنید.
