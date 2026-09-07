package Ir.co.tfs.farazaman.activity

import android.graphics.Typeface
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import Ir.co.tfs.farazaman.R
import Ir.co.tfs.farazaman.data.model.AppNotificationDto
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class FirebaseNotificationAdapter(
    private val onOpen: (AppNotificationDto) -> Unit,
    private val onDelete: (AppNotificationDto) -> Unit,
) : RecyclerView.Adapter<FirebaseNotificationAdapter.ViewHolder>() {
    private var items: List<AppNotificationDto> = emptyList()
    private var deletingId: Int? = null

    fun submitList(newItems: List<AppNotificationDto>, deletingId: Int?) {
        items = newItems
        this.deletingId = deletingId
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_firebase_notification, parent, false),
        onOpen,
        onDelete,
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(items[position], deletingId == items[position].id)

    override fun getItemCount() = items.size

    class ViewHolder(
        view: View,
        private val onOpen: (AppNotificationDto) -> Unit,
        private val onDelete: (AppNotificationDto) -> Unit,
    ) : RecyclerView.ViewHolder(view) {
        private val title: TextView = view.findViewById(R.id.notificationTitle)
        private val body: TextView = view.findViewById(R.id.notificationBody)
        private val time: TextView = view.findViewById(R.id.notificationTime)
        private val unread: View = view.findViewById(R.id.notificationUnread)
        private val delete: ImageButton = view.findViewById(R.id.notificationDelete)
        private val deleting: ProgressBar = view.findViewById(R.id.notificationDeleting)

        fun bind(item: AppNotificationDto, isDeleting: Boolean) {
            title.text = item.title
            title.setTypeface(title.typeface, if (item.isRead) Typeface.NORMAL else Typeface.BOLD)
            body.text = item.type.orEmpty()
            body.visibility = if (item.type.isNullOrBlank()) View.GONE else View.VISIBLE
            time.text = formatDate(item.createDate)
            unread.visibility = if (item.isRead) View.INVISIBLE else View.VISIBLE
            delete.visibility = if (isDeleting) View.INVISIBLE else View.VISIBLE
            deleting.visibility = if (isDeleting) View.VISIBLE else View.GONE
            itemView.alpha = if (item.isRead) 0.78f else 1f
            itemView.setOnClickListener { if (!isDeleting) onOpen(item) }
            delete.setOnClickListener { if (!isDeleting) onDelete(item) }
        }

        private fun formatDate(value: String): String {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                runCatching {
                    return OffsetDateTime.parse(value).format(
                        DateTimeFormatter.ofPattern("yyyy/MM/dd - HH:mm", Locale("fa")),
                    )
                }
            }
            return value.replace('T', ' ').take(16).replace('-', '/')
        }
    }
}
