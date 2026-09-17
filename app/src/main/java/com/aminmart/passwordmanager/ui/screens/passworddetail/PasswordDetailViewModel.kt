package com.aminmart.passwordmanager.ui.screens.passworddetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aminmart.passwordmanager.data.repository.PasswordRepository
import com.aminmart.passwordmanager.domain.model.PasswordEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PasswordDetailUiState(
    val password: PasswordEntry? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val showDeleteDialog: Boolean = false,
    val deleted: Boolean = false
)

@HiltViewModel
class PasswordDetailViewModel @Inject constructor(
    private val passwordRepository: PasswordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PasswordDetailUiState())
    val uiState: StateFlow<PasswordDetailUiState> = _uiState.asStateFlow()

    fun loadPassword(passwordId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val password = passwordRepository.getPasswordById(passwordId)
                _uiState.value = _uiState.value.copy(
                    password = password,
                    isLoading = false,
                    errorMessage = if (password == null) "Password not found" else null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    fun requestDelete() {
        _uiState.value = _uiState.value.copy(showDeleteDialog = true)
    }

    fun dismissDelete() {
        _uiState.value = _uiState.value.copy(showDeleteDialog = false)
    }

    fun deletePassword() {
        val id = _uiState.value.password?.id ?: return
        viewModelScope.launch {
            try {
                passwordRepository.deletePassword(id)
                _uiState.value = _uiState.value.copy(showDeleteDialog = false, deleted = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(showDeleteDialog = false, errorMessage = e.message)
            }
        }
    }
}
