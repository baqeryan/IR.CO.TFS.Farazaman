package Ir.co.tfs.farazaman.presentation.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview(name = "پیمانکار - انتخاب محدوده", showBackground = true, locale = "fa", widthDp = 360, heightDp = 800)
@Composable
private fun ContractorWorkAreaPreview() = ContractorPreviewFrame("انتخاب محدوده کاری") {
    PreviewField("کارفرما", "شهرداری منطقه یک")
    PreviewField("قرارداد", "جمع‌آوری و حمل پسماند")
    Button(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("تأیید و ادامه") }
}

@Preview(name = "پیمانکار - عملیات", showBackground = true, locale = "fa", widthDp = 360, heightDp = 800)
@Composable
private fun ContractorOperationsPreview() = ContractorPreviewFrame("عملیات پیمانکار") {
    PreviewCard("محدوده کاری", "شهرداری منطقه یک — جمع‌آوری پسماند")
    Button(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("شروع عملیات") }
    Button(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("ثبت اقدام") }
    OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("اصلاحیه‌ها") }
}

@Preview(name = "پیمانکار - ثبت اقدام", showBackground = true, locale = "fa", widthDp = 360, heightDp = 800)
@Composable
private fun ContractorActionPreview() = ContractorPreviewFrame("ثبت اقدام") {
    OutlinedTextField(value = "", onValueChange = {}, label = { Text("توضیحات اقدام") }, modifier = Modifier.fillMaxWidth().height(130.dp))
    Text("تصاویر اقدام", style = MaterialTheme.typography.titleMedium)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(3) { Box(Modifier.size(82.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))) }
    }
    OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("گرفتن تصویر با دوربین") }
    Button(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("ثبت و ارسال اقدام") }
}

@Preview(name = "پیمانکار - اصلاحیه‌ها", showBackground = true, locale = "fa", widthDp = 360, heightDp = 800)
@Composable
private fun ContractorCorrectionsPreview() = ContractorPreviewFrame("اصلاحیه‌ها") {
    PreviewCard("عدم جمع‌آوری پسماند", "فصل ششم • ثبت‌شده توسط ناظر")
    PreviewCard("شست‌وشوی ناقص مخزن", "فصل چهارم • نیازمند اصلاح")
}

@Preview(name = "پیمانکار - ثبت اصلاحیه", showBackground = true, locale = "fa", widthDp = 360, heightDp = 800)
@Composable
private fun ContractorCorrectionPreview() = ContractorPreviewFrame("ثبت اصلاحیه") {
    PreviewCard("تخلف مرتبط", "عدم جمع‌آوری پسماند در زمان مقرر")
    OutlinedTextField(value = "", onValueChange = {}, label = { Text("شرح اقدامات اصلاحی") }, modifier = Modifier.fillMaxWidth().height(120.dp))
    OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("گرفتن تصویر با دوربین") }
    Button(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("ثبت و ارسال اصلاحیه") }
}

@Preview(name = "پیمانکار - محل تخلف", showBackground = true, locale = "fa", widthDp = 360, heightDp = 800)
@Composable
private fun ContractorViolationMapPreview() = ContractorPreviewFrame("محل تخلف") {
    Box(
        modifier = Modifier.fillMaxWidth().height(520.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center,
    ) { Text("پیش‌نمایش نقشه و نشانگر تخلف") }
}

@Composable
private fun ContractorPreviewFrame(title: String, content: @Composable ColumnScope.() -> Unit) {
    FaraZamanTheme {
        Column(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(title, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(4.dp))
            content()
        }
    }
}

@Composable
private fun PreviewField(label: String, value: String) {
    OutlinedTextField(value = value, onValueChange = {}, readOnly = true, label = { Text(label) }, modifier = Modifier.fillMaxWidth())
}

@Composable
private fun PreviewCard(title: String, subtitle: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
