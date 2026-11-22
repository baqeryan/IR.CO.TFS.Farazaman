package Ir.co.tfs.farazaman.presentation.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import Ir.co.tfs.farazaman.R
import Ir.co.tfs.farazaman.presentation.design.theme.FzPersianFontFamily

data class SplashUiState(
    val slogan: String,
    val animate: Boolean = true,
)

@Composable
fun SplashScreen(state: SplashUiState) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(state.animate) { visible = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        AnimatedVisibility(visible = visible, enter = fadeIn(tween(700))) {
            Image(
                painter = painterResource(R.drawable.farazaman_logo),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier.size(256.dp),
            )
        }
        Spacer(Modifier.height(32.dp))
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(700, delayMillis = 250)) +
                slideInVertically(tween(700, delayMillis = 250)) { it / 2 },
        ) {
            Text(
                text = state.slogan,
                color = MaterialTheme.colorScheme.secondary,
                fontFamily = FzPersianFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
            )
        }
    }
}
