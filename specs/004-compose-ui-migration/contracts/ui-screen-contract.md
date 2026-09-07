# Pure Screen Contract

```kotlin
@Composable
fun XxxScreen(
    state: XxxUiState,
    onEvent: (XxxUiEvent) -> Unit,
    modifier: Modifier = Modifier,
)
```

## Dependency direction

`Route/Container/ViewModel → Screen → Feature Components → Fz Components → Material 3`

## Rules

1. State immutable و Event sealed/type-safe است.
2. Screen ViewModel resolve، network/storage/system API یا navigation مستقیم اجرا نمی‌کند.
3. Route/container StateFlow را lifecycle-aware جمع و one-off effect را یک‌بار مصرف می‌کند.
4. Screen `FaraZamanTheme` را نصب نمی‌کند؛ host/Preview آن را فراهم می‌کند.
5. Screen branding color، typography، reusable shape/spacing/elevation و component styling خام تعریف نمی‌کند.
6. Feature Component business model را به semantic variant نگاشت می‌کند؛ Design System business model نمی‌گیرد. Feature Shared Component فقط پس از اثبات تکرار استخراج و در package همان Feature نگهداری می‌شود.
7. direct Material primitive فقط برای primitive فاقد policy مجاز است؛ component policyدار از `Fz*` مصرف می‌شود.
8. Preview به Hilt، Retrofit، database، Activity، real ViewModel/navigation/location/camera وابسته نیست.
9. semantics/test tag پایدار، RTL و start/end رعایت می‌شود.
10. effectهای Compose ownership، key و cleanup مستند دارند.
11. Screen باید با fixture قطعی قابل screenshot باشد؛ native/system content از طریق ورودی نمایشی یا adapter قابل fake شدن است.

## Definition of Done per Screen

- State/Event و side effect بیرون Screen
- Material 3 زیر FarazamanTheme
- بدون hard-code قابل‌توجه یا styling موازی
- component policy رعایت‌شده
- Preview مستقل
- RTL، Light/Dark، font scale و accessibility قبول
- Behavioral Parity قبول
- intentional visual differences ثبت‌شده
- automated screenshot baseline و verification قبول
- tests/build قبول
- legacy resource فقط پس از parity حذف‌شده
