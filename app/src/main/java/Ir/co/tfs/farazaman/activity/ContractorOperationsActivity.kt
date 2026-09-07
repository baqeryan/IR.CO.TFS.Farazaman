package Ir.co.tfs.farazaman.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import Ir.co.tfs.farazaman.R
import Ir.co.tfs.farazaman.contractor.ContractorWorkContextStore
import Ir.co.tfs.farazaman.contractor.UnconfiguredContractorGateway
import Ir.co.tfs.farazaman.databinding.ActivityContractorOperationsBinding

class ContractorOperationsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityContractorOperationsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val workContext = ContractorWorkContextStore.read(this)
        if (workContext == null) {
            startActivity(Intent(this, ContractorWorkAreaSelectionActivity::class.java))
            finish()
            return
        }
        binding = ActivityContractorOperationsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.workAreaValue.text = getString(
            R.string.contractor_work_area_value,
            workContext.organTitle,
            workContext.contractTitle,
        )
        binding.startOperationButton.setOnClickListener { showUnavailable() }
        binding.registerActionButton.setOnClickListener {
            startActivity(Intent(this, ContractorActionActivity::class.java))
        }
        binding.correctionsButton.setOnClickListener {
            startActivity(Intent(this, ContractorViolationsActivity::class.java))
        }
        binding.changeWorkAreaButton.setOnClickListener {
            ContractorWorkContextStore.clear(this)
            startActivity(Intent(this, ContractorWorkAreaSelectionActivity::class.java))
            finish()
        }
    }

    private fun showUnavailable() = Toast.makeText(
        this,
        UnconfiguredContractorGateway.unavailableMessage(),
        Toast.LENGTH_LONG,
    ).show()
}
