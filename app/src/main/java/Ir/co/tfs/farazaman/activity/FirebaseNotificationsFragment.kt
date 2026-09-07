package Ir.co.tfs.farazaman.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import Ir.co.tfs.farazaman.R
import Ir.co.tfs.farazaman.data.model.AppNotificationDto
import Ir.co.tfs.farazaman.data.repository.AppNotificationRepository
import Ir.co.tfs.farazaman.databinding.FragmentFirebaseNotificationsBinding
import Ir.co.tfs.farazaman.notification.FirebaseNotificationInboxStore
import Ir.co.tfs.farazaman.notification.NotificationUnreadCountStore
import Ir.co.tfs.farazaman.presentation.viewmodel.NotificationsViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FirebaseNotificationsFragment : Fragment() {
    private var _binding: FragmentFirebaseNotificationsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NotificationsViewModel by lazy {
        ViewModelProvider(this)[NotificationsViewModel::class.java]
    }
    private lateinit var adapter: FirebaseNotificationAdapter
    private var receiverRegistered = false

    @Inject lateinit var repository: AppNotificationRepository
    @Inject lateinit var unreadStore: NotificationUnreadCountStore

    private val inboxChangedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) = viewModel.refresh()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentFirebaseNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = FirebaseNotificationAdapter(::openNotification, ::confirmDelete)
        binding.notificationsRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.notificationsRecycler.adapter = adapter
        binding.notificationsRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0 && !recyclerView.canScrollVertically(1)) viewModel.loadNextPage()
            }
        })
        binding.retryButton.setOnClickListener { viewModel.refresh() }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state.items, state.deletingId)
            binding.loadingProgress.visibility =
                if (state.loading && state.items.isEmpty()) View.VISIBLE else View.GONE
            binding.emptyState.visibility =
                if (!state.loading && state.items.isEmpty()) View.VISIBLE else View.GONE
            binding.notificationsRecycler.visibility =
                if (state.items.isEmpty()) View.GONE else View.VISIBLE
            binding.retryButton.visibility =
                if (state.error != null && state.items.isEmpty()) View.VISIBLE else View.GONE
            if (state.error != null && state.items.isNotEmpty()) {
                Toast.makeText(requireContext(), R.string.notification_load_error, Toast.LENGTH_SHORT).show()
            }
        }
        viewModel.refresh()
    }

    override fun onStart() {
        super.onStart()
        ContextCompat.registerReceiver(
            requireContext(),
            inboxChangedReceiver,
            IntentFilter(FirebaseNotificationInboxStore.ACTION_INBOX_CHANGED),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        receiverRegistered = true
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }

    override fun onStop() {
        if (receiverRegistered) {
            requireContext().unregisterReceiver(inboxChangedReceiver)
            receiverRegistered = false
        }
        super.onStop()
    }

    override fun onDestroyView() {
        binding.notificationsRecycler.adapter = null
        _binding = null
        super.onDestroyView()
    }

    private fun openNotification(item: AppNotificationDto) {
        viewLifecycleOwner.lifecycleScope.launch {
            val details = runCatching { repository.details(item.id) }.getOrElse {
                Toast.makeText(requireContext(), R.string.notification_load_error, Toast.LENGTH_SHORT).show()
                return@launch
            }
            val displayed = if (details.isRead) details else {
                runCatching { repository.visit(details.id) }.getOrDefault(details)
            }
            if (!details.isRead && displayed.isRead) {
                unreadStore.update((unreadStore.current() - 1).coerceAtLeast(0))
                viewModel.refresh()
            }
            val extra = listOfNotNull(
                displayed.type?.takeIf(String::isNotBlank),
                displayed.createDate.takeIf(String::isNotBlank),
                displayed.link?.takeIf(String::isNotBlank),
            ).joinToString("\n")
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(displayed.title)
                .setMessage(extra)
                .setPositiveButton(R.string.notification_close, null)
                .show()
        }
    }

    private fun confirmDelete(item: AppNotificationDto) {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(R.string.notification_delete_confirm)
            .setPositiveButton(R.string.notification_delete) { _, _ -> viewModel.delete(item) }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
