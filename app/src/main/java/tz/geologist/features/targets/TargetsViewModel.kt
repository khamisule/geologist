package tz.geologist.features.targets

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class TargetsUiState(
    val region: TargetRegion = TargetRegion.MANYARA,
    val loading: Boolean = true,
    val candidates: List<TargetCandidate> = emptyList(),
    val fromNetwork: Boolean = false,
    val error: String? = null
) {
    val highPriorityCount: Int get() = candidates.count { it.score >= 0.85 }
    val sourceLabel: String get() = if (fromNetwork) "Backend (live)" else "Offline (seed)"
}

/**
 * ViewModel ya M6 Targets. Offline-first: inajaribu backend halisi kisha assets.
 */
class TargetsViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = OfflineFirstTargetRepository(app.applicationContext)
    private val _state = MutableStateFlow(TargetsUiState())
    val state: StateFlow<TargetsUiState> = _state.asStateFlow()

    init { select(TargetRegion.MANYARA) }

    fun select(region: TargetRegion) {
        _state.value = _state.value.copy(region = region, loading = true, error = null)
        viewModelScope.launch {
            try {
                val res = withContext(Dispatchers.IO) { repo.load(region) }
                _state.value = _state.value.copy(
                    loading = false, candidates = res.candidates, fromNetwork = res.fromNetwork
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(loading = false, error = e.message ?: "Load failed")
            }
        }
    }
}
