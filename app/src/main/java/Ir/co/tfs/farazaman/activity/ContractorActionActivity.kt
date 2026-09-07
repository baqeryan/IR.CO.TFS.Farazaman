package Ir.co.tfs.farazaman.activity

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import Ir.co.tfs.farazaman.R
import Ir.co.tfs.farazaman.contractor.ContractorDraftValidator
import Ir.co.tfs.farazaman.contractor.UnconfiguredContractorGateway
import Ir.co.tfs.farazaman.databinding.ActivityContractorActionBinding

class ContractorActionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityContractorActionBinding
    private lateinit var imageCaptureCoordinator: ViolationImageCaptureCoordinator
    private val images = mutableListOf<Uri>()
    private lateinit var imagesAdapter: SelectedImagesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContractorActionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_arrow_back)
            title = ""
        }
        binding.toolbar.setNavigationOnClickListener { finish() }

        imagesAdapter = SelectedImagesAdapter(
            images = images,
            onImageClick = ::showImagePreview,
            onImageRemove = { index ->
                if (index in images.indices) {
                    images.removeAt(index)
                    imagesAdapter.notifyItemRemoved(index)
                    imagesAdapter.notifyItemRangeChanged(index, images.size - index)
                }
            },
        )
        binding.imagesList.apply {
            layoutManager = LinearLayoutManager(
                this@ContractorActionActivity,
                LinearLayoutManager.HORIZONTAL,
                false,
            )
            adapter = imagesAdapter
        }
        imageCaptureCoordinator = ViolationImageCaptureCoordinator(
            caller = this,
            contentResolver = { contentResolver },
            onCaptured = { uri, _ ->
                images.add(uri)
                imagesAdapter.notifyItemInserted(images.lastIndex)
                binding.imagesList.scrollToPosition(images.lastIndex)
            },
            onCaptureFailed = {
                Toast.makeText(this, R.string.contractor_capture_failed, Toast.LENGTH_LONG).show()
            },
        )
        binding.captureImageButton.setOnClickListener { imageCaptureCoordinator.captureNew() }
        binding.submitButton.setOnClickListener { validateAndSubmit() }
    }

    private fun validateAndSubmit() {
        binding.descriptionLayout.error = null
        binding.formError.text = null
        when (ContractorDraftValidator.validate(binding.descriptionInput.text?.toString().orEmpty(), images.size)) {
            ContractorDraftValidator.Error.DESCRIPTION_REQUIRED ->
                binding.descriptionLayout.error = getString(R.string.contractor_action_description_required)
            ContractorDraftValidator.Error.IMAGE_REQUIRED ->
                binding.formError.text = getString(R.string.contractor_action_image_required)
            null -> binding.formError.text = UnconfiguredContractorGateway.unavailableMessage()
        }
    }

    private fun showImagePreview(uri: Uri) {
        val image = ImageView(this).apply {
            setImageURI(uri)
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER
            setPadding(24, 24, 24, 24)
        }
        Dialog(this).apply {
            setContentView(image)
            setOnShowListener {
                window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            }
            image.setOnClickListener { dismiss() }
            show()
        }
    }
}
