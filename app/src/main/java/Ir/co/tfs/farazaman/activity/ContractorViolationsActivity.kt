package Ir.co.tfs.farazaman.activity

import android.os.Bundle
import Ir.co.tfs.farazaman.R
import Ir.co.tfs.farazaman.databinding.ActivityContractorViolationsBinding
import Ir.co.tfs.farazaman.presentation.base.BaseActivity
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

    private lateinit var binding: ActivityContractorViolationsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContractorViolationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_arrow_back)
            title = ""
        }
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.loadingIndicator.visibility = android.view.View.GONE
        binding.violationsList.visibility = android.view.View.GONE
        binding.errorContainer.visibility = android.view.View.GONE
        binding.emptyContainer.visibility = android.view.View.VISIBLE
        binding.emptyMessage.setText(R.string.contractor_violations_not_connected)
    }
}
