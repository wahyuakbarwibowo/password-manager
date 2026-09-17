package com.aminmart.passwordmanager.ui.screens.passwordlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aminmart.passwordmanager.data.repository.PasswordRepository
import com.aminmart.passwordmanager.domain.model.PasswordEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PasswordListUiState(
    val passwords: List<PasswordEntry> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val errorMessage: String? = null
)

@HiltViewModel
class PasswordListViewModel @Inject constructor(
    private val passwordRepository: PasswordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PasswordListUiState())
    val uiState: StateFlow<PasswordListUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    init {
        viewModelScope.launch {
            // The repository flow re-emits after every write, so the list stays current
            // across add/edit/delete/import without manual refreshes.
            combine(passwordRepository.getAllPasswords(), _searchQuery) { passwords, query ->
                if (query.isBlank()) passwords else passwords.filter {
                    it.title.contains(query, ignoreCase = true) ||
                        it.username.contains(query, ignoreCase = true) ||
                        it.website.contains(query, ignoreCase = true)
                }
            }.catch { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
            }.collect { passwords ->
                _uiState.value = _uiState.value.copy(passwords = passwords, isLoading = false)
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    /**
     * List rows carry no secrets; decrypt this one entry on demand for quick copy.
     */
    fun copyPassword(id: Long, onReady: (String) -> Unit) {
        viewModelScope.launch {
            runCatching { passwordRepository.getPasswordById(id) }
                .onSuccess { entry ->
                    if (entry == null) {
                        _uiState.value = _uiState.value.copy(errorMessage = "Password not found")
                    } else {
                        onReady(entry.password)
                    }
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(errorMessage = e.message ?: "Could not read password")
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
