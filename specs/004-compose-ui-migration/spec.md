# Feature Specification: مهاجرت تدریجی UI و Design System فرازمان

**Feature Branch**: `004-compose-ui-migration`  
**Created**: 2026-09-07  
**Status**: Draft  
**Input**: مهاجرت تدریجی XML به Compose با قرارداد State/Event و Design System مرکزی فرازمان بر پایه Material Design 3.

## Clarifications

### Session 2026-09-07

- Q: معیار عددی دسترس‌پذیری Design System برای حداقل اندازه لمس و کنتراست رنگ چه باشد؟ → A: حداقل touch target برابر 48dp؛ کنتراست متن عادی حداقل 4.5:1 و متن بزرگ حداقل 3:1.
- Q: اختلاف‌های بصری intentional نسبت به XML را چه کسی باید پیش از Migrated شدن هر Screen تأیید کند؟ → A: عبور از checklist و تست‌ها کافی است؛ تأیید انسانی جداگانه لازم نیست.
- Q: برای امکان بازگشت از Screen جدید Compose، آیا باید runtime feature flag داخل نسخه منتشرشده وجود داشته باشد؟ → A: فقط Screenهای پرریسک نقشه، GPS، دوربین و Track تا پایان rollout runtime feature flag داشته باشند؛ سایر Screenها rollback کد/نسخه‌ای دارند.
- Q: Visual Intent و تغییرات Theme/Componentها با چه روشی به‌عنوان gate قابل تکرار بررسی شوند؟ → A: screenshot regression خودکار برای تمام Screenها، شامل Map و جریان‌های سیستمی، الزامی است.
- Q: runtime feature flag صفحات پرریسک در نسخه production چگونه تغییر کند؟ → A: از تنظیم محلی مخفی و محافظت‌شده diagnostics/support استفاده شود؛ پیش‌فرض Compose و قابل بازگشت به XML باشد.

## User Scenarios & Testing

### User Story 1 - Foundation رسمی Design System (Priority: P1)

به‌عنوان تیم محصول، می‌خواهیم branding و قواعد مشترک رابط فرازمان در یک Design System مرکزی قرار گیرد تا تغییر رنگ، typography، shape، spacing و ظاهر اجزای مشترک بدون ویرایش گسترده Feature Screenها ممکن باشد.

**Why this priority**: تمام Screenهای مهاجرتیافته به این foundation وابسته‌اند و ایجاد Screen پیش از آن styling موازی تولید می‌کند.

**Independent Test**: کاتالوگ مستقل Design System در matrix روشن/تاریک، RTL، فونت بزرگ و گوشی کوچک رندر شود؛ تغییر آزمایشی palette و shape پایه بدون تغییر Feature Screen در همه نمونه‌ها دیده شود.

**Acceptance Scenarios**:

1. **Given** یک component بنیادی، **When** variant معنایی آن انتخاب می‌شود، **Then** رنگ، typography، shape، elevation و اندازه از Theme/Tokens مرکزی دریافت می‌شود.
2. **Given** Theme روشن یا تاریک و متن فارسی، **When** کاتالوگ رندر می‌شود، **Then** contrast، touch target، RTL و font scaling معیارهای foundation را پاس می‌کنند.
3. **Given** نیاز به API آزمایشی Material، **When** استفاده تصویب می‌شود، **Then** دلیل، wrapper ایزوله و fallback ثبت شده و Feature مستقیماً به آن وابسته نیست.

---

### User Story 2 - قرارداد مشترک State/Event و پیمانکار MVP (Priority: P1)

به‌عنوان توسعه‌دهنده، می‌خواهم صفحات پیمانکار اولین مصرف‌کننده production از Design System و قرارداد State/Event باشند تا معماری مقصد end-to-end اثبات شود.

**Why this priority**: صفحات پیمانکار جدیدتر و کم‌وابستگی‌ترند و proving ground مناسبی برای State/Event، Material 3، Preview، RTL، Dark Mode و accessibility هستند.

**Independent Test**: انتخاب محدوده، عملیات، اقدام، اصلاحیه و مکان پیمانکار با Behavioral Parity اجرا شوند؛ Screenها بدون dependency واقعی Preview شوند و styling موازی Design System نداشته باشند.

**Acceptance Scenarios**:

1. **Given** State نمونه، **When** Preview اجرا می‌شود، **Then** Screen بدون Activity، ViewModel، شبکه، مکان یا دوربین واقعی و زیر `FaraZamanTheme` رندر می‌شود.
2. **Given** کنش کاربر، **When** Screen واکنش می‌دهد، **Then** فقط Event نوع‌دار منتشر و side effect توسط Route/container اجرا می‌شود.
3. **Given** صفحات Contractor، **When** architecture review اجرا می‌شود، **Then** فقط tokens/components مجاز را مصرف کرده و hard-coded branding یا Material 2 جدید ندارند.

---

### User Story 3 - صفحات فهرستی و پشتیبان (Priority: P2)

به‌عنوان کاربر، می‌خواهم اعلان‌ها، پروفایل و فهرست‌های کم‌ریسک پس از مهاجرت همان داده، pagination، empty/error/loading و عملیات فعلی را با زبان بصری یکپارچه ارائه دهند.

**Independent Test**: پاسخ موفق، خالی، خطا، آفلاین، refreshing و صفحه بعد مستقل آزموده و Behavioral Parity با نسخه XML تأیید شود.

**Acceptance Scenarios**:

1. **Given** اعلان‌های خوانده/خوانده‌نشده، **When** list/detail/Visit/Delete اجرا می‌شود، **Then** قرارداد سرور و badge حفظ می‌شود.
2. **Given** business status، **When** Feature Component آن را نمایش می‌دهد، **Then** status به semantic variant نگاشت و styling توسط `FzStatusChip` انجام می‌شود.

---

### User Story 4 - پوسته و صفحات ناظر (Priority: P2)

به‌عنوان ناظر، می‌خواهم انتخاب محدوده، خانه، امروز و تاریخچه پس از مهاجرت، hierarchy اطلاعات، منوی شیشه‌ای، scroll insets، badge، وضعیت محدوده و navigation فعلی را حفظ کنند.

**Independent Test**: مسیر ناظر روی اندازه‌های مختلف، RTL و تغییر زنده Dark Mode اجرا شود و آخرین آیتم زیر منو پنهان نماند.

**Acceptance Scenarios**:

1. **Given** محتوای بلند، **When** تا انتها scroll می‌شود، **Then** content زیر منوی شیشه‌ای قرار نمی‌گیرد.
2. **Given** تغییر badge یا محدوده، **When** State تغییر می‌کند، **Then** Screen بدون دسترسی مستقیم به preferences/location/broadcast به‌روز می‌شود.

---

### User Story 5 - فرم‌ها و قابلیت‌های سیستمی (Priority: P3)

به‌عنوان کاربر میدانی، می‌خواهم فرم، دوربین، فایل، Map و GPS پس از مهاجرت پایدار بمانند و Activity recreation داده قابل نگهداری را از بین نبرد.

**Independent Test**: رد permission، cancel، rotation/process recreation، قطع شبکه، camera return و foreground/background آزموده شود.

**Acceptance Scenarios**:

1. **Given** درخواست camera/file/location، **When** رد یا لغو می‌شود، **Then** نتیجه به State/Effect قابل فهم تبدیل می‌شود.
2. **Given** MapLibre یا GPS فعال، **When** مقصد خارج می‌شود، **Then** منابع با lifecycle آزاد می‌شوند.
3. **Given** validation یا submit فعال، **When** لمس تکراری رخ می‌دهد، **Then** business action دوباره اجرا نمی‌شود.

---

### User Story 6 - خروج کنترل‌شده Legacy (Priority: P3)

به‌عنوان نگهدارنده، می‌خواهم XML، bridge Material 2 و binding قدیمی فقط پس از تأیید Behavioral Parity، Visual Intent Parity و gateهای Debug/Release حذف شوند.

**Independent Test**: هر Migration Record شواهد کامل داشته باشد و پس از حذف legacy هیچ reference شکسته باقی نماند.

**Acceptance Scenarios**:

1. **Given** checklist ناقص، **When** cleanup بررسی می‌شود، **Then** مسیر legacy حذف نمی‌شود.
2. **Given** اختلاف بصری عمدی، **When** صفحه پذیرفته می‌شود، **Then** دلیل اختلاف و انطباق آن با Material 3/branding در Migration Record ثبت شده است.

### Edge Cases

- تغییر زنده Theme mode، locale، RTL یا font scale.
- داده قبلی همراه refresh، خطا همراه cache و پایان pagination.
- Event/effect تکراری بر اثر recomposition، لمس سریع یا recreation.
- بازگشت از camera/file picker/settings و process death.
- نبود Play Services، خاموشی GPS، شکست style نقشه یا قطع شبکه.
- componentی که فقط یک بار مصرف می‌شود ولی branding/accessibility policy مشترک دارد.
- API آزمایشی که در نسخه فعلی dependencies موجود نیست یا fallback هم‌ارز ندارد.
- SDUI variant ناشناخته؛ client باید fallback معنایی امن داشته باشد و styling خام نپذیرد.
- دسترسی غیرمجاز یا تغییر تصادفی flagهای diagnostics؛ کنترل‌ها باید برای کاربر عادی غیرقابل مشاهده و غیرقابل تغییر باشند.

## Requirements

### Existing Migration Requirements

- **FR-001**: مهاجرت MUST صفحه‌به‌صفحه، تدریجی و در پایان هر دسته قابل انتشار باشد.
- **FR-002**: هر Screen MUST immutable State ورودی و یک callback یگانه typed Event خروجی داشته باشد.
- **FR-003**: Screen MUST فراخوانی مستقیم Retrofit، ContentResolver، SharedPreferences، Intent، navigation، permission، GPS، camera یا service نداشته باشد.
- **FR-004**: Route، Activity، Fragment، ViewModel یا coordinator MUST مالک dependency و side effect باشد.
- **FR-005**: State MUST loading، content، empty، error، refreshing، submitting، enabled و validation را حسب نیاز مدل کند.
- **FR-006**: Event MUST تمام intentهای کاربر را صریح پوشش دهد.
- **FR-007**: State collection MUST lifecycle-aware و effectها MUST دارای ownership، کلید پایدار و cleanup باشند.
- **FR-008**: هر Screen MUST Preview مستقل برای stateهای قابل اعمال داشته باشد.
- **FR-009**: فارسی، RTL، Dark Mode زنده، hierarchy اطلاعات و branding فرازمان MUST حفظ شوند.
- **FR-010**: business logic، API، DTO، runtime BASE_URL، auth و persistence MUST بدون requirement مستقل تغییر نکنند.
- **FR-011**: پیش از مهاجرت MUST inventory شامل input/output، side effect، dependency، state، baseline و rollback ثبت شود.
- **FR-012**: ترتیب MUST Foundation، Contractor، Support، Supervisor، System flows و Cleanup باشد.
- **FR-013**: Componentها MUST به دو دسته متمایز تقسیم شوند: `Design System Components` فاقد business concept مانند `FzButton/FzCard/FzTextField/FzStatusChip/FzScaffold` که می‌توانند از ابتدا و آگاهانه ساخته شوند؛ و `Feature Shared Components` مانند `ViolationCard/NotificationCard/ContractorHeader` که MUST فقط پس از اثبات تکرار یا نیاز مشترک واقعی استخراج شوند.
- **FR-014**: Form State MUST input، validation و submit را تفکیک و submit تکراری را کنترل کند.
- **FR-015**: navigation، snackbar و permission request MUST با recomposition/recreation تکرار نشوند.
- **FR-016**: MapView، camera، file picker و location MUST پشت adapter/coordinator lifecycle-aware بمانند.
- **FR-017**: legacy XML/binding/resource MUST فقط پس از gateهای parity و بررسی عدم استفاده حذف شود.
- **FR-018**: هر دسته MUST unit، UI و smoke tests متناسب با ریسک داشته باشد.
- **FR-019**: actionها و stateهای مشترک MUST semantics/test tag پایدار داشته باشند.
- **FR-020**: inventory MUST وضعیت Not Started، In Progress، Parity Review، Migrated یا Blocked را نگه دارد.

### Design System Requirements

- **FR-021**: تمام UI جدید Compose MUST بر foundation رسمی `androidx.compose.material3` و APIهای Stable آن ساخته شود.
- **FR-022**: پروژه MUST یک Farazaman Design System مرکزی بین Material 3 و Featureها داشته باشد و source of truth دوم برای Theme ایجاد نکند.
- **FR-023**: `FaraZamanTheme` موجود MUST در صورت امکان evolve شود و ColorScheme، Typography، Shapes، tokens اختصاصی، Light/Dark، RTL و typography فارسی را فراهم کند.
- **FR-024**: Dynamic Color MUST به‌صورت پیش‌فرض خاموش و palette پیش‌فرض brand-controlled باشد.
- **FR-025**: tokenهای مرکزی MUST colors، typography، shapes، spacing، elevation، motion و component sizes را پوشش دهند.
- **FR-026**: semantic colors MUST حداقل primary/onPrimary، secondary/onSecondary، background، surface/surfaceVariant/onSurface، error/onError و extensionهای success/warning/info را پوشش دهند.
- **FR-027**: Feature Screen MUST از hard-coded branding color، global typography، reusable shape، spacing، elevation یا component size خودداری کند.
- **FR-028**: component پرتکرار یا دارای branding/behavior/semantics/accessibility/variant/change-risk MUST پشت wrapper فرازمان قرار گیرد؛ wrapper بی‌دلیل ممنوع است.
- **FR-029**: Fz component MUST semantic variant بپذیرد و Feature نباید styling خامی مانند color/radius/elevation به آن تزریق کند، مگر API واقعاً low-level و مستند باشد.
- **FR-030**: Design System MUST هیچ concept کسب‌وکاری Contractor، Supervisor، Violation یا Notification را نشناسد.
- **FR-031**: Feature Component MUST business status را به semantic variant Design System نگاشت کند؛ مانند `ViolationStatusChip → FzStatusChip`.
- **FR-032**: Screen MUST فرض کند زیر `FaraZamanTheme` است و مالک Theme یا global styling نباشد.
- **FR-033**: ساختار مقصد MUST `Route/Container/ViewModel → Pure Screen → Feature Component → Fz Design System → Material 3` را رعایت کند.
- **FR-034**: Material 2 جدید MUST در Screen مهاجرت‌یافته اضافه نشود؛ bridge موقت MUST در Migration Record دارای removal condition باشد.
- **FR-035**: Experimental/Alpha Material API MUST فقط با نیاز ثبت‌شده، wrapper ایزوله Design System و fallback مصرف شود و Feature Screen MUST مستقیم به آن وابسته نباشد.
- **FR-036**: Design System MUST Preview مستقل tokens و تمام foundational component/variant/stateهای خود را داشته باشد.
- **FR-037**: Preview strategy MUST سه سطح Design System، Feature Component و Screen را پوشش دهد.
- **FR-038**: Previewهای قابل اعمال MUST Persian/RTL، Light، Dark، Large Font و Small Phone را پوشش دهند؛ shell responsive همچنین Large Phone/Tablet را پوشش دهد.
- **FR-039**: foundation accessibility policy MUST حداقل touch target برابر `48dp`، contentDescription، semantic role، disabled state، font scaling، error announcement و test semantics را تعریف کند؛ نسبت کنتراست MUST برای متن عادی حداقل `4.5:1` و برای متن بزرگ حداقل `3:1` باشد.
- **FR-040**: layout logic MUST به‌صورت پیش‌فرض start/end و RTL-first باشد؛ left/right فقط با دلیل فنی ثبت‌شده مجاز است.
- **FR-041**: SDUI/client models MUST semantic component/variant را بپذیرند و styling خام backend مانند hex color، font size یا radius را به Design System وارد نکنند.
- **FR-042**: semantic variant ناشناخته MUST به fallback امن و مستند نگاشت شود.
- **FR-043**: Theme configuration MUST قابل جایگزینی باشد تا palette/shape/typography آینده بدون تغییر Screen ممکن باشد؛ ساخت presetهای اضافی در Scope نیست.
- **FR-044**: architecture enforcement SHOULD با false-positive کنترل‌شده، color literal، arbitrary typography/shape و direct Material styling خارج از design package را شناسایی کند.
- **FR-045**: dependency upgrade MUST فقط پس از اثبات نیاز و بررسی سازگاری Kotlin/Compose compiler/AGP، به‌صورت تغییر جدا و با Debug/Release gate انجام شود.
- **FR-046**: parity MUST به Behavioral Parity اجباری و Visual Intent Parity تفکیک شود؛ pixel parity با XML الزام مطلق نیست.
- **FR-047**: اختلاف بصری intentional MUST branding، conventions، information hierarchy و UX را حفظ و در Migration Record ثبت کند؛ قبولی Visual Intent checklist و تست‌های مرتبط برای پذیرش کافی است و تأیید انسانی جداگانه الزام نیست.
- **FR-048**: تا قبولی architecture review صفحات Contractor، مهاجرت production دسته Support و Supervisor MUST آغاز نشود.
- **FR-049**: Screenهای system-critical نقشه، GPS، دوربین و Track MUST تا پایان rollout تأییدشده دارای runtime feature flag محلی برای بازگشت به مسیر legacy باشند؛ flag MUST از بخش مخفی و محافظت‌شده diagnostics/support تغییر کند، برای کاربر عادی نمایش داده نشود و مقدار پیش‌فرض آن Compose باشد. سایر Screenها MUST از rollback کد/نسخه‌ای استفاده کنند و نیازمند runtime flag نیستند.
- **FR-050**: تمام Screenهای مهاجرت‌یافته، شامل Map، Camera، GPS و Track، MUST screenshot regression خودکار داشته باشند؛ وابستگی native/system در screenshot test MUST با fixture یا fake قطعی جایگزین شود و smoke test واقعی دستگاه همچنان جداگانه اجرا شود.

### Key Entities

- **Design Token Set**: semantic colors، typography، shapes، spacing، elevation، motion و sizes برای یک Theme configuration.
- **Semantic Variant**: intent ظاهری مستقل از مقدار خام؛ مانند Primary، Danger، Success یا Warning.
- **Fz Foundation Component**: wrapper فاقد business concept با branding، behavior، semantics یا accessibility مشترک.
- **Feature Component**: نمایش business-specific که domain state را به semantic variant نگاشت می‌کند.
- **Screen Contract**: State، Event، Effect، owner و system dependencies یک مقصد.
- **Migration Record**: legacy/Compose paths، Material bridge، status، behavioral evidence، visual intent differences و rollback.
- **Migration Flag**: انتخاب محلی و موقت مسیر Compose یا legacy برای Screenهای system-critical، با owner، default، removal condition و تاریخ پایان rollout.
- **Parity Record**: Behavioral Parity اجباری و Visual Intent Parity با اختلافات عمدی ثبت‌شده.

## Success Criteria

### Measurable Outcomes

- **SC-001**: 100٪ Screenهای مهاجرت‌یافته فقط State/Event داشته و architecture check دسترسی ممنوع پیدا نکند.
- **SC-002**: 100٪ Screenهای Compose جدید زیر `FaraZamanTheme` رندر شوند.
- **SC-003**: 100٪ foundational componentها Preview مستقل برای تمام variant/stateهای خود داشته باشند.
- **SC-004**: 100٪ Screenهای مهاجرت‌یافته بدون hard-coded branding color، typography، reusable shape و styling موازی قبول شوند.
- **SC-005**: matrix Light+RTL، Dark+RTL، Large Font، Small Phone و Normal Phone برای foundation پاس شود؛ shellها Large Phone/Tablet را نیز پاس کنند.
- **SC-006**: تغییر آزمایشی palette اصلی بدون تغییر هیچ Feature Screen در 100٪ Previewهای منتخب منعکس شود.
- **SC-007**: تغییر آزمایشی shape پایه Card/Button بدون تغییر Feature Screen در 100٪ مصرف‌کنندگان منتخب منعکس شود.
- **SC-008**: هیچ Material 2 component جدید و هیچ Experimental Material API مستقیم در Feature Screenهای مهاجرت‌یافته یافت نشود.
- **SC-009**: 100٪ business statusهای مهاجرت‌یافته از mapping business→semantic استفاده کنند و raw status color نداشته باشند.
- **SC-010**: تمام Behavioral Parity scenarios بدون regression بحرانی/شدید قبول شوند و تمام اختلاف‌های Visual Intent ثبت شوند.
- **SC-011**: هر دسته unit tests، lintDebug و assembleDebug؛ دسته حساس lintRelease، assembleRelease و smoke عملیاتی را پاس کند.
- **SC-012**: هیچ legacy resource پیش از تکمیل Definition of Done صفحه حذف نشود.
- **SC-013**: render/scroll بیش از 10٪ نسبت به baseline افت نکند و system flow پس از 20 چرخه resource بدون owner نداشته باشد.
- **SC-014**: inventory صددرصد مقصدهای user-facing CODEMAP/Manifest و تمام Composeهای موجود را پوشش دهد.
- **SC-015**: صددرصد componentهای تعاملی Foundation حداقل touch target برابر 48dp و صددرصد ترکیب‌های متنی کاتالوگ نسبت کنتراست حداقل 4.5:1 برای متن عادی یا 3:1 برای متن بزرگ را پاس کنند.
- **SC-016**: صددرصد Screenهای Migrated دارای baseline screenshot خودکار پایدار باشند و هیچ اختلاف تأییدنشده‌ای در matrix تعریف‌شده باقی نماند؛ Screenهای system-critical علاوه بر آن smoke test واقعی دستگاه را پاس کنند.
- **SC-017**: صددرصد flagهای system-critical در نصب production به‌صورت پیش‌فرض مسیر Compose را انتخاب کنند، از UI عادی قابل دسترسی نباشند و در آزمون diagnostics بتوانند بدون تغییر داده یا انتشار APK جدید، مقصد بعدی را به مسیر XML برگردانند.

## Assumptions

- Kotlin `1.9.22`، Compose Compiler `1.5.10`، Compose BOM `2024.02.02` و Material 3 موجود نقطه شروع‌اند؛ upgrade بدون نیاز اثبات‌شده انجام نمی‌شود.
- `FaraZamanTheme` موجود source of truth اولیه است و به package Design System منتقل/evolve می‌شود؛ Theme موازی ساخته نمی‌شود.
- Material Components Views فعلی برای XML تا مهاجرت همان صفحه مجاز است و Material 2 Compose جدید مجاز نیست.
- رفتار فعلی مرجع **Behavioral Parity** است.
- ظاهر XML baseline مقایسه است، اما مقصد Compose از **Farazaman Material 3 Design System** پیروی می‌کند.
- اختلاف بصری intentional مجاز است و MUST در Migration Record ثبت شود.
- Activity/Fragment می‌تواند موقتاً container باقی بماند و Navigation Compose سراسری لازم نیست.
- SDUI backend در این Feature تغییر نمی‌کند؛ فقط client contract برای semantic mapping آماده می‌شود.

## Out of Scope

- تغییر backend، endpoint، database schema یا جایگزینی auth/persistence/MapLibre/FCM/SignalR.
- بازطراحی بصری کامل تمام صفحات.
- چند Theme preset تولیدی، rollout کردن Dynamic Color یا Full Material 3 Expressive.
- جایگزینی schema فعلی backend SDUI.
- Global Navigation Compose migration.
- انتقال Design System به Gradle module جدا.
