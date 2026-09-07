package Ir.co.tfs.farazaman.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import Ir.co.tfs.farazaman.supervisor.actions.SupervisorActionRepository
import Ir.co.tfs.farazaman.supervisor.actions.UserActionCategories
import Ir.co.tfs.farazaman.supervisor.actions.UserActionCategory
import Ir.co.tfs.farazaman.supervisor.actions.UserActionRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ActionsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SupervisorActionRepository(application)
    private val _state = MutableLiveData(ActionsUiState(categories = repository.categories()))
    val state: LiveData<ActionsUiState> = _state

    private var selectedFilter = UserActionCategories.ALL
    private var loading = false
    private var loadJob: Job? = null
    private var loadGeneration = 0L

    fun refresh() = load(reset = true)

    fun selectFilter(filter: String) {
        if (selectedFilter == filter) return
        selectedFilter = filter
        load(reset = true)
    }

    fun loadNextPage() {
        if (_state.value?.hasMore == true) load(reset = false)
    }

    private fun load(reset: Boolean) {
        if (!reset && loading) return
        if (reset) loadJob?.cancel()
        loading = true
        val generation = ++loadGeneration
        val filter = selectedFilter
        val current = _state.value ?: ActionsUiState(categories = repository.categories())
        val offset = if (reset) 0 else current.items.size
        _state.value = current.copy(
            items = if (reset) emptyList() else current.items,
            selectedFilter = filter,
            loading = true,
            initialLoad = reset,
        )
        loadJob = viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    repository.loadPage(filter, offset, PAGE_SIZE)
                }
            }.onSuccess { page ->
                if (generation != loadGeneration || filter != selectedFilter) return@onSuccess
                val latest = _state.value ?: current
                _state.value = latest.copy(
                    items = if (reset) page.items else latest.items + page.items,
                    selectedFilter = filter,
                    loading = false,
                    initialLoad = false,
                    hasMore = page.hasMore,
                )
            }.onFailure {
                if (generation != loadGeneration) return@onFailure
                val latest = _state.value ?: current
                _state.value = latest.copy(
                    selectedFilter = filter,
                    loading = false,
                    initialLoad = false,
                )
            }
            if (generation == loadGeneration) loading = false
        }
    }

    data class ActionsUiState(
        val categories: List<UserActionCategory>,
        val items: List<UserActionRecord> = emptyList(),
        val selectedFilter: String = UserActionCategories.ALL,
        val loading: Boolean = false,
        val initialLoad: Boolean = true,
        val hasMore: Boolean = false,
    )

    companion object {
        private const val PAGE_SIZE = 30
    }
}
