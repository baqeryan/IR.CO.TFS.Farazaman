package Ir.co.tfs.farazaman.activity

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.mutableIntStateOf
import Ir.co.tfs.farazaman.OSMTracker
import Ir.co.tfs.farazaman.R
import Ir.co.tfs.farazaman.presentation.compose.IntroPageUi
import Ir.co.tfs.farazaman.presentation.compose.IntroScreen
import Ir.co.tfs.farazaman.presentation.compose.IntroUiEvent
import Ir.co.tfs.farazaman.presentation.compose.IntroUiState
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanTheme

enum class TutorialRole(val id: String, val title: String) {
    Supervisor("supervisor", "ناظر"),
    Driver("driver", "راننده"),
    Contractor("contractor", "پیمانکار");

    val completedPreferenceKey: String get() = "role_tutorial_completed_$id"

    companion object {
        fun fromId(id: String?): TutorialRole? = entries.firstOrNull { it.id == id }
    }
}

object RoleTutorialLauncher {
    fun launch(context: Context, role: TutorialRole, destination: Intent) {
        val completed = PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(role.completedPreferenceKey, false)
        context.startActivity(
            if (completed) destination else Intro.createIntent(context, role, destination),
        )
    }
}

class Intro : AppCompatActivity() {
    private lateinit var role: TutorialRole
    private lateinit var destination: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        role = TutorialRole.fromId(intent.getStringExtra(EXTRA_ROLE)) ?: TutorialRole.Supervisor
        destination = readDestinationIntent()
            ?: Intent(this, RoleSelectionActivity::class.java)

        onBackPressedDispatcher.addCallback(this) {
            startActivity(Intent(this@Intro, RoleSelectionActivity::class.java))
            finish()
        }

        val currentPage = mutableIntStateOf(0)
        val pages = pagesFor(role)
        setContent {
            FaraZamanTheme {
                IntroScreen(
                    state = IntroUiState(
                        roleTitle = role.title,
                        pages = pages,
                        currentPage = currentPage.intValue,
                    ),
                    onEvent = { event ->
                        when (event) {
                            IntroUiEvent.Skip -> finishIntro()
                            IntroUiEvent.Previous -> if (currentPage.intValue > 0) {
                                currentPage.intValue--
                            }
                            IntroUiEvent.Next -> if (currentPage.intValue < pages.lastIndex) {
                                currentPage.intValue++
                            } else {
                                finishIntro()
                            }
                        }
                    },
                )
            }
        }
    }

    private fun finishIntro() {
        PreferenceManager.getDefaultSharedPreferences(this).edit()
            .putBoolean(role.completedPreferenceKey, true)
            .putBoolean(OSMTracker.Preferences.KEY_DISPLAY_APP_INTRO, false)
            .apply()
        startActivity(destination)
        finish()
    }

    @Suppress("DEPRECATION")
    private fun readDestinationIntent(): Intent? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        intent.getParcelableExtra(EXTRA_DESTINATION, Intent::class.java)
    } else {
        intent.getParcelableExtra(EXTRA_DESTINATION)
    }

    private fun pagesFor(role: TutorialRole): List<IntroPageUi> = when (role) {
        TutorialRole.Supervisor -> supervisorPages()
        TutorialRole.Driver -> driverPages()
        TutorialRole.Contractor -> contractorPages()
    }

    private fun supervisorPages() = listOf(
        IntroPageUi(
            "انتخاب محدوده کاری",
            "ابتدا کارفرما و قرارداد را انتخاب کنید تا برنامه روزانه، محدوده فعالیت و اطلاعات مأموریت دریافت شود.",
            R.drawable.ic_daily_tasks,
            listOf("محدوده درست را پیش از شروع بازدید کنترل کنید.", "در صورت وجود مأموریت ناتمام، برنامه همان مأموریت را بازیابی می‌کند."),
        ),
        IntroPageUi(
            "شروع بازدید و ثبت مسیر",
            "با شروع بازدید، سرویس مکان‌یابی فعال می‌شود و مسیر حرکت را حتی هنگام قرارگرفتن برنامه در پس‌زمینه ثبت می‌کند.",
            R.drawable.intro_hands_phone,
            listOf("مجوز موقعیت دقیق و دسترسی پس‌زمینه را فعال کنید.", "اعلان ثبت مسیر را نبندید و GPS دستگاه را روشن نگه دارید."),
        ),
        IntroPageUi(
            "برنامه امروز و اطلاعات معبر",
            "در صفحه امروز، نواقص و برنامه‌های کاری را ببینید. نام، نشانی و عرض معبر بر اساس موقعیت فعلی به‌روزرسانی می‌شود.",
            R.drawable.ic_map,
            listOf("برای دریافت دقیق نام معبر چند لحظه در فضای باز منتظر بمانید.", "خطای موقت اطلاعات جاده مانع ثبت مسیر نمی‌شود."),
        ),
        IntroPageUi(
            "نقشه، لایه‌ها و نقاط ثبت‌شده",
            "نقشه مسیر GPS، محدوده کاری، نقاط لایه و نواقص را نمایش می‌دهد و امکان کنترل نقاط آماده خروجی GPX را فراهم می‌کند.",
            R.drawable.intro_map_pins,
            listOf("نقطه نقص قرمز و نقطه لایه سبز نمایش داده می‌شود.", "از دکمه موقعیت برای بازگشت دوربین به محل فعلی استفاده کنید."),
        ),
        IntroPageUi(
            "نقص، تصویر و پایان بازدید",
            "در زمان بازدید می‌توانید نقص را همراه موقعیت و تصاویر ثبت کنید. در پایان، بازدید را متوقف و مسیر را برای سرور ارسال کنید.",
            R.drawable.intro_violation_finish,
            listOf("پیش از ثبت نقص، بازدید باید فعال باشد.", "تصاویر را پیش از ارسال بازبینی کنید.", "سوابق و وضعیت ارسال از صفحه تاریخچه قابل پیگیری است."),
        ),
    )

    private fun driverPages() = listOf(
        IntroPageUi(
            "انتخاب برنامه و محدوده",
            "برنامه کاری و محدوده‌ای را که برای خودرو تعیین شده بررسی کنید و سپس مأموریت را آغاز کنید.",
            R.drawable.ic_daily_tasks,
            listOf("پیش از حرکت، عنوان مأموریت و محدوده انتخاب‌شده را کنترل کنید.", "برای تغییر محدوده، ابتدا مأموریت جاری را به‌درستی پایان دهید."),
        ),
        IntroPageUi(
            "آماده‌سازی موقعیت مکانی",
            "برای ثبت قابل اعتماد مسیر، GPS، موقعیت دقیق و دسترسی مکان در پس‌زمینه باید فعال باشند.",
            R.drawable.ic_location,
            listOf("در فضای باز منتظر تثبیت موقعیت بمانید.", "محدودیت باتری ممکن است ثبت مسیر در پس‌زمینه را متوقف کند."),
        ),
        IntroPageUi(
            "ثبت پیوسته مسیر",
            "پس از شروع مأموریت، نقاط حرکت به‌صورت پیوسته ذخیره می‌شوند و توقف‌های خودرو نیز در سابقه مسیر باقی می‌مانند.",
            R.drawable.intro_hands_phone,
            listOf("اعلان سرویس ثبت مسیر نشان می‌دهد عملیات فعال است.", "بستن صفحه یا خاموش‌کردن نمایشگر نباید مأموریت را متوقف کند."),
        ),
        IntroPageUi(
            "کنترل مسیر روی نقشه",
            "موقعیت زنده، جهت حرکت و مسیر طی‌شده را روی نقشه بررسی کنید و در صورت نیاز نقاط میدانی ثبت کنید.",
            R.drawable.ic_map,
            listOf("دقت GPS و جهت حرکت با جابه‌جایی واقعی بهتر می‌شود.", "پیش از پایان مأموریت، نقاط GPX را روی نقشه کنترل کنید."),
        ),
        IntroPageUi(
            "پایان و ارسال مأموریت",
            "پس از پایان مسیر، ثبت موقعیت را متوقف و فایل مأموریت را ارسال کنید. تا تأیید ارسال، اطلاعات محلی را حذف نکنید.",
            R.drawable.intro_illustration_history,
            listOf("اتصال اینترنت پایدار برای آپلود لازم است.", "در صورت خطا، مأموریت ذخیره‌شده را دوباره ارسال کنید."),
        ),
    )

    private fun contractorPages() = listOf(
        IntroPageUi(
            "انتخاب محدوده فعالیت",
            "کارفرما، قرارداد و محدوده‌ای را که عملیات پیمانکار در آن انجام می‌شود انتخاب کنید.",
            R.drawable.ic_daily_tasks,
            listOf("انتخاب محدوده، زمینه تمام اقدام‌ها و اصلاحیه‌های بعدی است.", "پیش از ادامه، اطلاعات قرارداد را کنترل کنید."),
        ),
        IntroPageUi(
            "ثبت اقدام اجرایی",
            "شرح اقدام انجام‌شده را روشن و دقیق ثبت کنید تا سوابق عملیات برای پیگیری بعدی قابل استفاده باشد.",
            R.drawable.intro_clipboard,
            listOf("شرح کوتاه اما کامل بنویسید.", "اطلاعات ثبت‌شده باید با محدوده کاری انتخاب‌شده مرتبط باشد."),
        ),
        IntroPageUi(
            "تصاویر و مستندات",
            "برای مستندسازی اقدام یا اصلاح نقص از دوربین استفاده کنید، تصاویر را بازبینی و موارد نامناسب را حذف کنید.",
            R.drawable.ic_camera,
            listOf("تصویر واضح و مرتبط ثبت کنید.", "قبل از ثبت نهایی، فهرست تصاویر را کنترل کنید."),
        ),
        IntroPageUi(
            "موقعیت و اصلاح نواقص",
            "موقعیت نقص را روی نقشه بررسی کنید و اصلاحات موردنیاز را همراه توضیح و مدرک ثبت کنید.",
            R.drawable.ic_location,
            listOf("برای موقعیت دقیق، GPS را روشن نگه دارید.", "مختصات نامعتبر یا خارج از محدوده را پیش از ارسال اصلاح کنید."),
        ),
        IntroPageUi(
            "پیگیری عملیات",
            "اقدام‌ها و اصلاحیه‌ها را از محیط عملیات دنبال کنید و هنگام تغییر پروژه، محدوده کاری مناسب را دوباره انتخاب کنید.",
            R.drawable.intro_illustration_history,
            listOf("ارسال موفق را پیش از خروج از فرم بررسی کنید.", "از بخش پروفایل می‌توانید نقش یا محدوده کاری را تغییر دهید."),
        ),
    )

    companion object {
        private const val EXTRA_ROLE = "tutorial_role"
        private const val EXTRA_DESTINATION = "tutorial_destination"

        fun createIntent(context: Context, role: TutorialRole, destination: Intent): Intent =
            Intent(context, Intro::class.java).apply {
                putExtra(EXTRA_ROLE, role.id)
                putExtra(EXTRA_DESTINATION, destination)
            }
    }
}
