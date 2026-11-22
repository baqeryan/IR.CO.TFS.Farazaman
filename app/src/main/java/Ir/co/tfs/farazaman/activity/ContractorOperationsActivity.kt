package Ir.co.tfs.farazaman.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import Ir.co.tfs.farazaman.contractor.ContractorWorkContextStore
import Ir.co.tfs.farazaman.contractor.UnconfiguredContractorGateway
import Ir.co.tfs.farazaman.presentation.compose.*
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanTheme

class ContractorOperationsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val workContext = ContractorWorkContextStore.read(this)
        if (workContext == null) {
            startActivity(Intent(this, ContractorWorkAreaSelectionActivity::class.java))
            finish()
            return
        }
        setContent {
            FaraZamanTheme {
                ContractorOperationsScreen(
                    state = ContractorOperationsUiState(workContext, UnconfiguredContractorGateway.unavailableMessage()),
                    onEvent = { event -> when (event) {
                    ContractorOperationsEvent.Back -> finish()
                    ContractorOperationsEvent.StartOperation -> showUnavailable()
                    ContractorOperationsEvent.RegisterAction -> startActivity(Intent(this, ContractorActionActivity::class.java))
                    ContractorOperationsEvent.Corrections -> startActivity(Intent(this, ContractorViolationsActivity::class.java))
                    ContractorOperationsEvent.ChangeWorkArea -> { ContractorWorkContextStore.clear(this); startActivity(Intent(this, ContractorWorkAreaSelectionActivity::class.java)); finish() }
                    } },
                )
            }
        }
    }

    private fun showUnavailable() = Toast.makeText(
        this,
        UnconfiguredContractorGateway.unavailableMessage(),
        Toast.LENGTH_LONG,
    ).show()
}
