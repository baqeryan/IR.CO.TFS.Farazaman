# Quickstart Validation

1. Build with `./gradlew :app:assembleDebug`.
2. Sign in, select Contractor, and verify work-area selection appears before the Contractor home.
3. Verify Contractor surfaces use «شروع عملیات», «ثبت اقدام», and «اصلاحیه» and do not expose «ثبت تخلف».
4. Open ثبت اقدام, enter a description, capture images, preview/remove them, and verify submission reports that the service is not connected rather than success.
5. Open اصلاحیه and verify the authoritative empty/not-connected state; no sample violation is displayed.
6. Switch light/dark mode and verify Persian RTL rendering.
7. Run `./gradlew :app:testDebugUnitTest` and a Release build before publishing.
