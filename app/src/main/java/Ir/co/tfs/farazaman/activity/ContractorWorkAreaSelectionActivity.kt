package Ir.co.tfs.farazaman.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import Ir.co.tfs.farazaman.contractor.ContractorWorkContextStore
import Ir.co.tfs.farazaman.contractor.UnconfiguredContractorGateway
import Ir.co.tfs.farazaman.presentation.compose.ContractorWorkAreaEvent
import Ir.co.tfs.farazaman.presentation.compose.ContractorWorkAreaScreen
import Ir.co.tfs.farazaman.presentation.compose.ContractorWorkAreaUiState
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanTheme

class ContractorWorkAreaSelectionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ContractorWorkContextStore.read(this)?.let {
            startActivity(Intent(this, ContractorOperationsActivity::class.java))
            finish()
            return
        }
        setContent {
            FaraZamanTheme {
                ContractorWorkAreaScreen(
                    ContractorWorkAreaUiState(UnconfiguredContractorGateway.unavailableMessage()),
                    onEvent = { event -> when (event) {
                        ContractorWorkAreaEvent.Back -> finish()
                        ContractorWorkAreaEvent.Profile -> SupervisorProfileActivity.open(this)
                        ContractorWorkAreaEvent.Continue -> Unit
                    } },
                )
            }
        }
    }
}
