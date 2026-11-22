package Ir.co.tfs.farazaman.activity

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.core.content.ContextCompat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import Ir.co.tfs.farazaman.R
import Ir.co.tfs.farazaman.contractor.ContractorDraftValidator
import Ir.co.tfs.farazaman.contractor.UnconfiguredContractorGateway
import Ir.co.tfs.farazaman.presentation.compose.ContractorActionEvent
import Ir.co.tfs.farazaman.presentation.compose.ContractorActionScreen
import Ir.co.tfs.farazaman.presentation.compose.ContractorActionUiState
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanTheme

class ContractorActionActivity : AppCompatActivity() {
    private lateinit var imageCaptureCoordinator: ViolationImageCaptureCoordinator
    private val images = mutableListOf<Uri>()
    private var description by mutableStateOf("")
    private var formError by mutableStateOf<String?>(null)
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        if (result.isNotEmpty() && result.values.all { it }) imageCaptureCoordinator.captureNew()
        else Toast.makeText(this, R.string.contractor_capture_permission_required, Toast.LENGTH_LONG).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { FaraZamanTheme { ContractorActionScreen(ContractorActionUiState(description, images.size, formError, UnconfiguredContractorGateway.unavailableMessage()), ::handleEvent) } }
        imageCaptureCoordinator = ViolationImageCaptureCoordinator(
            caller = this,
            contentResolver = { contentResolver },
            onCaptured = { uri, _ ->
                images.add(uri)
            },
            onCaptureFailed = {
                Toast.makeText(this, R.string.contractor_capture_failed, Toast.LENGTH_LONG).show()
            },
        )
    }

    private fun handleEvent(event: ContractorActionEvent) {
        when (event) {
            is ContractorActionEvent.DescriptionChanged -> { description = event.value; formError = null }
            ContractorActionEvent.CaptureImage -> requestCameraCapture()
            ContractorActionEvent.Submit -> validateAndSubmit()
            ContractorActionEvent.Back -> finish()
        }
    }

    private fun requestCameraCapture() {
        val needed = buildList {
            if (ContextCompat.checkSelfPermission(this@ContractorActionActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                add(Manifest.permission.CAMERA)
            }
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
                ContextCompat.checkSelfPermission(this@ContractorActionActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            ) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
        if (needed.isEmpty()) imageCaptureCoordinator.captureNew() else permissionLauncher.launch(needed.toTypedArray())
    }

    private fun validateAndSubmit() {
        formError = when (ContractorDraftValidator.validate(description, images.size)) {
            ContractorDraftValidator.Error.DESCRIPTION_REQUIRED ->
                getString(R.string.contractor_action_description_required)
            ContractorDraftValidator.Error.IMAGE_REQUIRED ->
                getString(R.string.contractor_action_image_required)
            null -> UnconfiguredContractorGateway.unavailableMessage()
        }
    }

}
