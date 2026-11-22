package Ir.co.tfs.farazaman.activity

import dagger.hilt.android.AndroidEntryPoint
import Ir.co.tfs.farazaman.R

@AndroidEntryPoint
class CommitmentsSubmitViolationFormActivity : NewSubmitViolationFormActivity() {
    override val formTitle: String get() = getString(R.string.commitments_violation_form_title)
    override val keepOpenAfterSuccess = true
    override val commitmentsMode = true
}
