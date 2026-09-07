package Ir.co.tfs.farazaman.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import Ir.co.tfs.farazaman.R
import Ir.co.tfs.farazaman.contractor.ContractorWorkContextStore
import Ir.co.tfs.farazaman.contractor.UnconfiguredContractorGateway
import Ir.co.tfs.farazaman.databinding.ActivityContractorWorkAreaSelectionBinding

class ContractorWorkAreaSelectionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityContractorWorkAreaSelectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ContractorWorkContextStore.read(this)?.let {
            startActivity(Intent(this, ContractorOperationsActivity::class.java))
            finish()
            return
        }
        binding = ActivityContractorWorkAreaSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnBack.setOnClickListener { finish() }
        binding.btnProfile.setOnClickListener { SupervisorProfileActivity.open(this) }
        binding.serviceMessage.text = UnconfiguredContractorGateway.unavailableMessage()
    }
}
