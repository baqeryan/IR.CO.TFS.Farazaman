package Ir.co.tfs.farazaman.activity

import android.os.Bundle
import Ir.co.tfs.farazaman.R
import Ir.co.tfs.farazaman.presentation.base.BaseActivity
import androidx.activity.compose.setContent
import Ir.co.tfs.farazaman.presentation.compose.ContractorCorrectionsScreen
import Ir.co.tfs.farazaman.presentation.compose.ContractorCorrectionsUiState
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Entry shell for the Contractor workflow.
 *
 * The actionable-violations request is intentionally not started until its backend contract is
 * confirmed. This Activity still provides a stable navigation destination for the confirmed
 * Contractor actor role without introducing a speculative endpoint or DTO.
 */
@AndroidEntryPoint
class ContractorViolationsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FaraZamanTheme {
                ContractorCorrectionsScreen(
                    state = ContractorCorrectionsUiState(getString(R.string.contractor_violations_not_connected)),
                    onEvent = { event -> if (event is Ir.co.tfs.farazaman.presentation.compose.ContractorCorrectionsEvent.Back) finish() },
                )
            }
        }
    }
}
