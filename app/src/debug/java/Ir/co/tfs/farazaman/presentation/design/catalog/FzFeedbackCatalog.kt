package Ir.co.tfs.farazaman.presentation.design.catalog

import Ir.co.tfs.farazaman.presentation.design.components.FzAlert
import Ir.co.tfs.farazaman.presentation.design.components.FzButton
import Ir.co.tfs.farazaman.presentation.design.components.FzDialog
import Ir.co.tfs.farazaman.presentation.design.components.FzEmptyState
import Ir.co.tfs.farazaman.presentation.design.components.FzErrorState
import Ir.co.tfs.farazaman.presentation.design.components.FzLinearProgress
import Ir.co.tfs.farazaman.presentation.design.components.FzSkeleton
import Ir.co.tfs.farazaman.presentation.design.theme.FzSemanticIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FzFeedbackCatalog(modifier: Modifier = Modifier) {
    var dialog by remember { mutableStateOf(false) }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FzAlert("اطلاعات", message = "موقعیت مکانی دریافت شد")
        FzAlert("هشدار", message = "دقت GPS پایین است", intent = FzSemanticIntent.Warning)
        FzAlert("خطا", message = "ارسال اطلاعات انجام نشد", intent = FzSemanticIntent.Error)
        FzLinearProgress(0.6f)
        FzSkeleton()
        FzEmptyState("موردی وجود ندارد")
        FzErrorState("خطا در دریافت", retryLabel = "تلاش مجدد", onRetry = {})
        FzButton("نمایش گفتگو", { dialog = true })
    }
    FzDialog(dialog, "تأیید عملیات", "تأیید", { dialog = false }, { dialog = false }, message = "آیا ادامه می‌دهید؟", dismissLabel = "انصراف")
}
