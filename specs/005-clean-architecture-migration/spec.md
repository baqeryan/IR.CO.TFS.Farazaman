# Feature Specification: مهاجرت تدریجی به معماری Clean چندماژوله

**Feature Branch**: `005-clean-architecture-migration`

**Created**: 2026-09-30

**Status**: Draft — implementation requires explicit architecture approval

**Input**: تبدیل معماری فعلی FaraZaman با الهام از `eslamfaisal/android-compose-clean-architecture-sample` و حفظ قیود عملیاتی پروژه

## User Scenarios & Testing

### User Story 1 - ورود و انتخاب نقش روی مرزهای Clean Architecture (Priority: P1)

کاربر باید بدون تغییر رفتار فعلی، شهر را انتخاب کند، با OIDC یا password وارد شود و پس از دریافت اطلاعات و نقش‌ها به مقصد درست هدایت شود؛ در حالی که UI، domain و data در ماژول‌های جدا قرار گرفته‌اند.

**Why this priority**: تمام گردش‌های عملیاتی به session معتبر و routing درست وابسته‌اند و این برش اولین اثبات عملی معماری مقصد است.

**Independent Test**: روی نصب تمیز، هر دو روش ورود با یک سرور انتخاب‌شده اجرا شود، user info/roles دریافت شود و role routing و logout کامل بدون وابستگی مستقیم presentation به Retrofit/SharedPreferences کار کند.

**Acceptance Scenarios**:

1. **Given** شهر انتخاب شده و OIDC فعال است، **When** ورود موفق می‌شود، **Then** token امن ذخیره، اطلاعات و نقش‌ها دریافت و صفحه انتخاب نقش یا مقصد تک‌نقشی نمایش داده می‌شود.
2. **Given** password mode فعال است، **When** ورود و اولین API احرازشده اجرا می‌شود، **Then** درخواست‌ها به `BASE_URL` همان شهرداری و از interceptor مشترک ارسال می‌شوند.
3. **Given** session نامعتبر یا logout است، **When** پاک‌سازی اجرا می‌شود، **Then** داده‌های session پاک و داده‌های track و فایل‌های پایدار حفظ می‌شوند.

---

### User Story 2 - مأموریت و رهگیری سرپرست به‌صورت vertical slice (Priority: P2)

سرپرست باید انتخاب محدوده، ایجاد/بازیابی مأموریت، برنامه روزانه، شروع/پایان track و history را با همان رفتار تولیدی، از طریق use case و repositoryهای مستقل از UI انجام دهد.

**Why this priority**: پیچیده‌ترین گردش تجاری برنامه است و مرز درست میان API، preference، ContentProvider، GPS service و Compose را اثبات می‌کند.

**Independent Test**: یک مأموریت سرپرست از انتخاب محدوده تا ثبت نقاط، rollover روز، پایان مأموریت و مشاهده history با fake repository و سپس روی دستگاه واقعی اجرا شود.

**Acceptance Scenarios**:

1. **Given** سرپرست مجاز است، **When** محدوده و قرارداد را تأیید می‌کند، **Then** مأموریت remote و track local به‌صورت اتمیک و قابل بازیابی ایجاد می‌شوند.
2. **Given** مأموریت uploadنشده از روز قبل وجود دارد، **When** روز عوض می‌شود، **Then** daily plan روی همان track refresh و ثبت تخلف تا موفقیت refresh مسدود می‌شود.
3. **Given** Activity متوقف یا نابود می‌شود، **When** lifecycle تغییر می‌کند، **Then** location updates/listeners آزاد و foreground tracking طبق قرارداد فعلی ادامه یا متوقف می‌شود.

---

### User Story 3 - گردش راننده و مدیریت track روی لایه‌های مستقل (Priority: P3)

راننده باید مأموریت خود را شروع/پایان دهد و track، waypoint، export و upload را بدون وابستگی UI به ContentResolver، فایل یا Retrofit مدیریت کند.

**Why this priority**: داده‌های میدانی و سازگاری دیتابیس legacy حیاتی‌اند و باید پیش از هر تعویض persistence ایمن شوند.

**Independent Test**: یک track راننده ثبت، در لیست/جزئیات باز، به GPX/ZIP صادر و به سرور انتخاب‌شده upload شود؛ track سرپرست در نتایج راننده دیده نشود.

**Acceptance Scenarios**:

1. **Given** مأموریت راننده فعال است، **When** GPS point یا waypoint ثبت می‌شود، **Then** داده با role صحیح در ContentProvider فعلی ذخیره می‌شود.
2. **Given** track کامل شده، **When** export/upload اجرا می‌شود، **Then** file I/O خارج main thread و URL بر پایه شهرداری انتخاب‌شده است.
3. **Given** خطای شبکه یا process recreation رخ می‌دهد، **When** UI دوباره ساخته می‌شود، **Then** state قابل‌بازیابی است و track محلی حذف نمی‌شود.

---

### User Story 4 - نقشه و تخلف با state/event و adapters چرخه‌عمر (Priority: P4)

کاربر سرپرست یا راننده باید نقشه، اطلاعات راه، ثبت تخلف، دوربین و موقعیت را با همان validation و metadata واقعی استفاده کند، بدون آنکه Composable یا ViewModel به implementationهای Android و transport متصل باشد.

**Why this priority**: MapLibre، دوربین و location پرریسک‌ترین integrationهای lifecycle هستند و پس از تثبیت tracking مهاجرت می‌شوند.

**Independent Test**: مسیر و fence روی MapLibre نمایش داده شود، تصویر دوربین و آخرین مختصات معتبر به تخلف متصل شود و خطا/لغو بدون leak یا ثبت موفقیت کاذب مدیریت شود.

**Acceptance Scenarios**:

1. **Given** مختصات معتبر وجود دارد، **When** تخلف ارسال می‌شود، **Then** metadata واقعی point و تصاویر تأییدشده map و submit می‌شوند.
2. **Given** مختصات نامعتبر یا stale mission است، **When** submit زده می‌شود، **Then** use case ارسال را رد و UI پیام فارسی قابل‌فهم نشان می‌دهد.
3. **Given** map style آنلاین در دسترس نیست، **When** نقشه باز می‌شود، **Then** cache/fallback فعلی بدون تغییر قرارداد کار می‌کند.

---

### User Story 5 - اعلان‌ها، realtime، پیمانکار و حذف بدهی legacy (Priority: P5)

کاربران باید اعلان، realtime، گردش‌های پیمانکار و تنظیمات را از feature moduleهای مستقل دریافت کنند و در پایان app فقط composition root و navigation را نگه دارد.

**Why this priority**: این برش وابستگی‌های باقی‌مانده را جمع می‌کند و امکان حذف packageهای orchestration قدیمی را پس از اثبات parity می‌دهد.

**Independent Test**: FCM و SignalR refresh اعلان را تحریک کنند، پیمانکار فقط عملیات دارای contract را انجام دهد، settings/profile کار کنند و architecture checks هیچ dependency ممنوعی گزارش نکنند.

**Acceptance Scenarios**:

1. **Given** notification یا SignalR event می‌رسد، **When** app foreground/background می‌شود، **Then** lifecycle ownership و source-of-truth سرور حفظ می‌شود.
2. **Given** قرارداد backend پیمانکار ناشناخته است، **When** کاربر اقدام شبکه‌ای می‌کند، **Then** UI موفقیت کاذب نشان نمی‌دهد و implementation فرضی ساخته نمی‌شود.
3. **Given** همه migration gateها پاس شده‌اند، **When** cleanup اجرا می‌شود، **Then** app module فقط wiring/navigation/platform entry points را نگه می‌دارد و کد legacy فقط با مدرک parity حذف می‌شود.

### Edge Cases

- تغییر `BASE_URL` بین دو session باید Retrofit وابسته به شهر را recreate کند و client مرکزی update/registry bearer token نگیرد.
- minified Release نباید generic metadata مربوط به Retrofit/Gson/AppAuth را حذف کند.
- process death در میانه OIDC، مأموریت فعال، ثبت تصویر یا upload نباید session/track را خراب کند.
- module split نباید authority ثابت ContentProvider، intent actionها، redirect URI یا application ID را تغییر دهد.
- تغییر روز، قطع شبکه، refresh token هم‌زمان، permission denial و Android background restrictions باید همان semantics فعلی را حفظ کنند.

## Requirements

### Functional Requirements

- **FR-001**: مهاجرت MUST تدریجی، vertical-slice و قابل rollback باشد؛ big-bang rewrite مجاز نیست.
- **FR-002**: ساختار مقصد MUST شامل composition root در `:app`، ماژول‌های مشترک `:core:*` و ماژول‌های `:feature:*` با لایه‌های presentation/domain/data باشد.
- **FR-003**: dependency direction MUST از presentation به domain و از data به domain باشد؛ domain نباید Android، Retrofit، Room یا Compose import کند.
- **FR-004**: feature moduleها MUST مستقیماً به feature module دیگر وابسته نباشند؛ مدل/قرارداد مشترک باید به core contract مناسب منتقل شود.
- **FR-005**: هر migration slice MUST قبل و بعد از انتقال characterization/contract test و یک rollback point داشته باشد.
- **FR-006**: runtime `BASE_URL` MUST منبع authoritative تمام API/uploadهای شهرداری باقی بماند.
- **FR-007**: OIDC/password auth، TokenManager/Keystore، refresh serialization و logout semantics MUST حفظ شوند.
- **FR-008**: ContentProvider/SQLite فعلی MUST تا یک feature جداگانه و migration تأییدشده، implementation authoritative track storage باقی بماند؛ Room نمونه نباید کورکورانه جایگزین آن شود.
- **FR-009**: UI MUST از immutable state، typed event و one-shot effect استفاده کند و platform effects در route/container یا adapter چرخه‌عمر قرار گیرند.
- **FR-010**: عملیات network/database/file MUST خارج main thread و resourceها MUST lifecycle-owned و بسته شوند.
- **FR-011**: Persian locale، RTL، dark mode، accessibility و Compose design system فعلی MUST حفظ شوند.
- **FR-012**: R8 rules، signing identity، package prefix `Ir`، provider authority و Release behavior MUST حفظ شوند.
- **FR-013**: API/DTOهای ناشناخته پیمانکار MUST پیاده‌سازی حدسی نشوند.
- **FR-014**: architecture dependency tests MUST dependencyهای ممنوع، Android import در domain و دسترسی مستقیم UI به transport/storage را fail کنند.
- **FR-015**: هر برش MUST unit، integration/contract و در مسیرهای system-critical device smoke test داشته باشد.
- **FR-016**: حذف کد legacy MUST فقط پس از ثبت behavioral parity و پاس شدن Debug/Release gates انجام شود.

### Key Entities

- **Architecture Boundary**: ماژول، API عمومی، dependencyهای مجاز و owner آن.
- **Migration Slice**: یک گردش قابل انتشار با baseline، target files، tests، rollout و rollback.
- **Session Configuration**: شهرداری انتخاب‌شده، login mode، OIDC configuration و token state بدون افشای secret.
- **Mission/Track**: مدل‌های domain مستقل از DTO/ContentProvider که role و lifecycle مأموریت را حفظ می‌کنند.
- **UI Contract**: state immutable، event و effect برای هر screen/route.

## Success Criteria

- **SC-001**: پنج برش مهاجرت به‌ترتیب مستقل build/test و قابل انتشار باشند.
- **SC-002**: صددرصد source setهای domain بدون Android/Retrofit/Compose dependency compile شوند.
- **SC-003**: architecture tests تمام dependencyهای ممنوع تعریف‌شده را شناسایی کنند و baseline بدون violation جدید باشد.
- **SC-004**: ورود OIDC/password، user info، roles و اولین API احرازشده در Debug و minified Release پاس شوند.
- **SC-005**: هیچ regression در selected-city routing، track role separation، مأموریت rollover یا logout retention مشاهده نشود.
- **SC-006**: همه flowهای migrated تست reducer/use-case داشته و system-critical flowها smoke test واقعی دستگاه را پاس کنند.
- **SC-007**: app module در پایان فقط composition root، manifest entry points و navigation wiring نگه دارد؛ business rule مستقیم در Activity/Fragment/Service جدید اضافه نشود.
- **SC-008**: هیچ token، authorization header، password، client secret یا signing credential در log/artifact جدید ظاهر نشود.

## Assumptions

- معماری نمونه یک reference است، نه template قابل کپی؛ Gson، ContentProvider، AppAuth، MapLibre و toolchain فعلی تا تصمیم مستقل حفظ می‌شوند.
- نام‌گذاری ماژول‌ها با Gradle Groovy فعلی سازگار می‌ماند و تبدیل build scripts به Kotlin DSL خارج scope است.
- Navigation Compose سراسری شرط این مهاجرت نیست؛ Activity/Serviceهای Android می‌توانند adapter/entry point باقی بمانند.
- ساخت ماژول‌ها نیازمند تأیید کتبی migration impact طبق Constitution است؛ این سند و plan همان رکورد پیشنهادی‌اند.

## Out of Scope

- بازنویسی backend یا تغییر endpointها.
- تعویض فوری ContentProvider با Room، Gson با Moshi یا AppAuth با راهکار دیگر صرفاً برای شباهت به نمونه.
- تغییر بصری گسترده یا تکرار کار feature `004-compose-ui-migration`.
- تعویض signing key، application ID، provider authority یا package prefix.
