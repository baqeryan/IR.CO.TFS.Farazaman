package Ir.co.tfs.farazaman.presentation.design.components

import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun FzLoading(message: String, modifier: Modifier = Modifier) = FeedbackContainer(modifier) {
    CircularProgressIndicator()
    Text(message, style = MaterialTheme.typography.bodyMedium)
}

@Composable
fun FzEmptyState(title: String, message: String? = null, modifier: Modifier = Modifier) = FeedbackContainer(modifier) {
    Text(title, style = MaterialTheme.typography.titleMedium)
    message?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
}

@Composable
fun FzErrorState(title: String, message: String? = null, retryLabel: String? = null, onRetry: (() -> Unit)? = null, modifier: Modifier = Modifier) = FeedbackContainer(modifier) {
    Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
    message?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
    if (retryLabel != null && onRetry != null) FzButton(retryLabel, onRetry, variant = FzButtonVariant.Secondary)
}

@Composable
private fun FeedbackContainer(modifier: Modifier, content: @Composable () -> Unit) {
    Column(
        modifier = modifier.fillMaxWidth().padding(FaraZamanThemeTokens.spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(FaraZamanThemeTokens.spacing.md),
    ) { content() }
}
