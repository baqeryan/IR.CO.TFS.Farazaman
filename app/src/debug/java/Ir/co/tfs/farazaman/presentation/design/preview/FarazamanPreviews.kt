package Ir.co.tfs.farazaman.presentation.design.preview

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.BINARY)
@Preview(name = "FA RTL Light", locale = "fa", widthDp = 360, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "FA RTL Dark", locale = "fa", widthDp = 360, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "FA Large Font", locale = "fa", widthDp = 360, heightDp = 800, fontScale = 1.5f)
@Preview(name = "FA Small Phone", locale = "fa", widthDp = 320, heightDp = 640)
annotation class FzLightPreview

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.BINARY)
@Preview(name = "FA RTL Dark", locale = "fa", widthDp = 360, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_YES)
annotation class FzDarkPreview

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.BINARY)
@Preview(name = "FA Large Font", locale = "fa", widthDp = 360, heightDp = 800, fontScale = 1.5f)
annotation class FzLargeFontPreview

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.BINARY)
@Preview(name = "FA Small Phone", locale = "fa", widthDp = 320, heightDp = 640)
annotation class FzSmallPhonePreview
