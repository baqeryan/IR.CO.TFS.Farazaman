package Ir.co.tfs.farazaman.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import Ir.co.tfs.farazaman.data.model.AppNotificationDto
import Ir.co.tfs.farazaman.data.repository.AppNotificationRepository
import Ir.co.tfs.farazaman.notification.NotificationUnreadCountStore
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val repository: AppNotificationRepository,
    private val unreadStore: NotificationUnreadCountStore,
) : ViewModel() {
    private val _state = MutableLiveData(NotificationsUiState())
    val state: LiveData<NotificationsUiState> = _state
    private var loadJob: Job? = null
    private var generation = 0L

    fun refresh() = load(reset = true)

    fun loadNextPage() {
        val current = _state.value ?: return
        if (!current.loading && current.hasMore) load(reset = false)
    }

    fun delete(item: AppNotificationDto) {
        val current = _state.value ?: NotificationsUiState()
        _state.value = current.copy(deletingId = item.id, error = null)
        viewModelScope.launch {
            runCatching { repository.delete(item.id) }
                .onSuccess {
                    val latest = _state.value ?: current
                    _state.value = latest.copy(
                        items = latest.items.filterNot { it.id == item.id },
                        deletingId = null,
                    )
                    refreshUnreadCount()
                }
                .onFailure {
                    _state.value = (_state.value ?: current).copy(deletingId = null, error = it)
                    refresh()
                }
        }
    }

    private fun load(reset: Boolean) {
        val current = _state.value ?: NotificationsUiState()
        if (!reset && (current.loading || !current.hasMore)) return
        if (reset) loadJob?.cancel()
        val requestGeneration = ++generation
        val page = if (reset) 1 else current.currentPage + 1
        _state.value = current.copy(
            items = if (reset) emptyList() else current.items,
            loading = true,
            error = null,
        )
        loadJob = viewModelScope.launch {
            runCatching { repository.loadPage(page) }
                .onSuccess { result ->
                    if (requestGeneration != generation) return@onSuccess
                    val previous = _state.value ?: current
                    val merged = if (reset) result.items else {
                        (previous.items + result.items).distinctBy(AppNotificationDto::id)
                    }
                    result.unreadCount?.let(unreadStore::update)
                    _state.value = previous.copy(
                        items = merged,
                        currentPage = result.currentPage,
                        hasMore = result.hasMore && result.items.isNotEmpty(),
                        loading = false,
                        error = null,
                    )
                }
                .onFailure { error ->
                    if (requestGeneration != generation) return@onFailure
                    _state.value = (_state.value ?: current).copy(loading = false, error = error)
                }
        }
    }

    private fun refreshUnreadCount() {
        viewModelScope.launch {
            runCatching { repository.unreadCount() }.onSuccess(unreadStore::update)
        }
    }

    data class NotificationsUiState(
        val items: List<AppNotificationDto> = emptyList(),
        val currentPage: Int = 0,
        val hasMore: Boolean = true,
        val loading: Boolean = false,
        val deletingId: Int? = null,
        val error: Throwable? = null,
    )
}
