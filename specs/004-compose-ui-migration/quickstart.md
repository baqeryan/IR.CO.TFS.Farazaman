# Quickstart: Validation Guide

## Foundation

1. کاتالوگ Design System را برای Light+RTL، Dark+RTL، large font، small و normal phone باز کنید.
2. تمام variant/stateهای foundational را بررسی کنید.
3. palette آزمایشی Theme را عوض کنید؛ Feature Preview نباید تغییر source بخواهد.
4. shape پایه Button/Card را عوض کنید؛ مصرف‌کننده نباید تغییر source بخواهد.
5. architecture tests را برای raw styling، Material 2، Experimental imports و dependency direction اجرا کنید.
6. Roborazzi goldens را record و سپس verify کنید؛ تغییر golden باید Migration Record متناظر داشته باشد.

## One Screen

1. رفتار فعلی را به‌عنوان Behavioral Parity و ظاهر XML را به‌عنوان baseline مقایسه ثبت کنید؛ مقصد Compose باید از Farazaman Material 3 Design System پیروی کند.
2. UiState/UiEvent/UiEffect و reducer tests را ایجاد کنید.
3. Feature mapping و Screen خالص را فقط با Fz policy بسازید؛ Feature Shared Component را تنها پس از اثبات تکرار و داخل Feature استخراج کنید.
4. Screen preview matrix و UI tests را اجرا کنید.
5. container را متصل و lifecycle/effect را بررسی کنید.
6. اختلاف بصری intentional را ثبت کنید؛ pixel parity الزام نیست و پس از DoD legacy را حذف کنید.
7. برای Map/Camera/GPS/Track، مسیر Compose و Legacy را با diagnostics flag تست و عدم نمایش کنترل به کاربر عادی را تأیید کنید.

## Commands

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:recordRoborazziDebug
.\gradlew.bat :app:verifyRoborazziDebug
.\gradlew.bat :app:lintDebug
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:lintRelease
.\gradlew.bat :app:assembleRelease
```

## Expected Outcome

Foundation matrix سبز، Screen فقط State/Event، styling مرکزی، Behavioral Parity کامل، visual delta ثبت‌شده و legacy صرفاً پس از gate حذف شده باشد.
