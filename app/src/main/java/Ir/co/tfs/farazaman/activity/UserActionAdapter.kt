package Ir.co.tfs.farazaman.activity

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import Ir.co.tfs.farazaman.R
import Ir.co.tfs.farazaman.databinding.ItemUserActionBinding
import Ir.co.tfs.farazaman.supervisor.actions.UserActionCategories
import Ir.co.tfs.farazaman.supervisor.actions.UserActionMetadataKeys
import Ir.co.tfs.farazaman.supervisor.actions.UserActionOperations
import Ir.co.tfs.farazaman.supervisor.actions.UserActionRecord
import saman.zamani.persiandate.PersianDate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserActionAdapter(
    private val onOperationClick: (UserActionRecord, String) -> Unit,
) : ListAdapter<UserActionRecord, UserActionAdapter.ActionViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActionViewHolder =
        ActionViewHolder(
            ItemUserActionBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            onOperationClick,
        )

    override fun onBindViewHolder(holder: ActionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ActionViewHolder(
        private val binding: ItemUserActionBinding,
        private val onOperationClick: (UserActionRecord, String) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: UserActionRecord) = with(binding) {
            actionTitle.text = item.title
            actionTimestamp.text = formatDateTime(item.timestampMs)
            actionDescription.text = item.description
            actionDescription.visibility = if (item.description.isBlank()) View.GONE else View.VISIBLE

            val metadataText = metadataLines(item).joinToString("\n")
            actionMetadata.text = metadataText
            actionMetadata.visibility = if (metadataText.isBlank()) View.GONE else View.VISIBLE

            actionStatus.text = item.status.orEmpty()
            actionStatus.visibility = if (item.status.isNullOrBlank()) View.GONE else View.VISIBLE

            actionIcon.setImageResource(iconFor(item.iconKey))
            val (containerColor, contentColor) = colorsFor(item.category)
            val containerTint = ColorStateList.valueOf(ContextCompat.getColor(root.context, containerColor))
            val contentTint = ColorStateList.valueOf(ContextCompat.getColor(root.context, contentColor))
            categoryAccent.backgroundTintList = contentTint
            (actionIcon.parent as View).backgroundTintList = containerTint
            actionIcon.imageTintList = contentTint
            actionStatus.backgroundTintList = containerTint
            actionStatus.setTextColor(contentTint)

            bindOperations(actionOperations, item)
        }

        private fun bindOperations(container: LinearLayout, item: UserActionRecord) {
            container.removeAllViews()
            item.operationKeys.forEach { operationKey ->
                val button = LayoutInflater.from(container.context).inflate(
                    R.layout.item_user_action_operation,
                    container,
                    false,
                ) as MaterialButton
                button.text = when (operationKey) {
                    UserActionOperations.RESUME_MISSION -> container.context.getString(R.string.action_resume_mission)
                    UserActionOperations.OPEN_GPX -> container.context.getString(R.string.action_download_gpx)
                    else -> operationKey
                }
                button.setOnClickListener { onOperationClick(item, operationKey) }
                container.addView(button)
            }
            container.visibility = if (container.childCount == 0) View.GONE else View.VISIBLE
        }

        private fun metadataLines(item: UserActionRecord): List<String> {
            val context = binding.root.context
            val entries = listOf(
                UserActionMetadataKeys.ORGANIZATION to R.string.action_metadata_organization,
                UserActionMetadataKeys.CONTRACT to R.string.action_metadata_contract,
                UserActionMetadataKeys.LOCATION to R.string.action_metadata_location,
                UserActionMetadataKeys.RECORD_ID to R.string.action_metadata_record_id,
                UserActionMetadataKeys.MISSION_ID to R.string.action_metadata_mission_id,
                UserActionMetadataKeys.MISSION_START_AT to R.string.action_metadata_start,
                UserActionMetadataKeys.PAUSED_AT to R.string.action_metadata_pause,
                UserActionMetadataKeys.FINISHED_AT to R.string.action_metadata_finish,
                UserActionMetadataKeys.UPLOADED_AT to R.string.action_metadata_upload,
                UserActionMetadataKeys.DURATION_MS to R.string.action_metadata_duration,
            )
            return entries.mapNotNull { (key, labelRes) ->
                val rawValue = item.metadata[key]?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                val value = when (key) {
                    UserActionMetadataKeys.MISSION_START_AT,
                    UserActionMetadataKeys.PAUSED_AT,
                    UserActionMetadataKeys.FINISHED_AT,
                    UserActionMetadataKeys.UPLOADED_AT -> rawValue.toLongOrNull()?.let(::formatDateTime) ?: rawValue
                    UserActionMetadataKeys.DURATION_MS -> rawValue.toLongOrNull()?.let(::formatDuration) ?: rawValue
                    else -> rawValue
                }
                context.getString(R.string.action_metadata_line, context.getString(labelRes), value)
            }
        }

        private fun formatDuration(durationMs: Long): String {
            val context = binding.root.context
            val totalMinutes = durationMs / 60_000L
            val hours = totalMinutes / 60L
            val minutes = totalMinutes % 60L
            return when {
                hours > 0L -> context.getString(R.string.action_duration_hours_minutes, hours, minutes)
                totalMinutes > 0L -> context.getString(R.string.action_duration_minutes, totalMinutes)
                else -> context.getString(R.string.action_duration_less_than_minute)
            }
        }

        @DrawableRes
        private fun iconFor(key: String?): Int = when (key) {
            "pause" -> R.drawable.ic_action_pause
            "upload" -> R.drawable.ic_action_upload
            "penalty" -> R.drawable.ic_note
            "mission" -> R.drawable.ic_nav_schedule
            else -> R.drawable.ic_notifications
        }

        private fun colorsFor(category: String): Pair<Int, Int> = when (category) {
            UserActionCategories.PAUSES -> R.color.app_warning_container to R.color.app_on_warning_container
            UserActionCategories.UPLOADS -> R.color.app_success_container to R.color.app_on_success_container
            UserActionCategories.PENALTIES -> R.color.app_info_container to R.color.app_on_info_container
            else -> R.color.app_primary_container to R.color.app_on_primary_container
        }

        companion object {
            private val clockFormatter = SimpleDateFormat("HH:mm", Locale("fa", "IR"))

            private fun formatDateTime(timestampMs: Long): String {
                if (timestampMs <= 0L) return "-"
                val date = PersianDate(timestampMs)
                val raw = "%04d/%02d/%02d • %s".format(
                    Locale.US,
                    date.shYear,
                    date.shMonth,
                    date.shDay,
                    clockFormatter.format(Date(timestampMs)),
                )
                return raw.toPersianDigits()
            }

            private fun String.toPersianDigits(): String = map { character ->
                when (character) {
                    in '0'..'9' -> "۰۱۲۳۴۵۶۷۸۹"[character - '0']
                    else -> character
                }
            }.joinToString("")
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<UserActionRecord>() {
        override fun areItemsTheSame(oldItem: UserActionRecord, newItem: UserActionRecord): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: UserActionRecord, newItem: UserActionRecord): Boolean =
            oldItem == newItem
    }
}
