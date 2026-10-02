package com.ledger.app.ui.screen.pin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ledger.app.LedgerApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class PinMode { VERIFY, SET, CONFIRM }

data class PinState(
    val mode: PinMode = PinMode.VERIFY,
    val digits: String = "",
    val error: String = "",
    val success: Boolean = false,
    val pendingPin: String = ""
)

class PinViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as LedgerApplication
    private val security = app.securityManager
    private val _state = MutableStateFlow(PinState())
    val state: StateFlow<PinState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val hasPinSet = security.isPinEnabled.first()
            val mode = if (hasPinSet) PinMode.VERIFY else PinMode.SET
            _state.value = _state.value.copy(mode = mode)
        }
    }

    fun forceSetMode() {
        _state.value = _state.value.copy(mode = PinMode.SET, digits = "", error = "", pendingPin = "")
    }

    fun onDigit(d: String) {
        val current = _state.value
        if (current.digits.length >= 4) return
        val updated = current.digits + d
        _state.value = current.copy(digits = updated, error = "")
        if (updated.length == 4) {
            processPin(updated)
        }
    }

    fun onBackspace() {
        val current = _state.value
        if (current.digits.isEmpty()) return
        _state.value = current.copy(digits = current.digits.dropLast(1), error = "")
    }

    private fun processPin(pin: String) {
        viewModelScope.launch {
            when (_state.value.mode) {
                PinMode.VERIFY -> {
                    val ok = security.verifyPin(pin)
                    if (ok) {
                        _state.value = _state.value.copy(success = true)
                    } else {
                        _state.value = _state.value.copy(digits = "", error = "Wrong PIN")
                    }
                }
                PinMode.SET -> {
                    _state.value = _state.value.copy(mode = PinMode.CONFIRM, pendingPin = pin, digits = "", error = "")
                }
                PinMode.CONFIRM -> {
                    if (pin == _state.value.pendingPin) {
                        security.setPin(pin)
                        _state.value = _state.value.copy(success = true)
                    } else {
                        _state.value = _state.value.copy(mode = PinMode.SET, digits = "", pendingPin = "", error = "PINs don't match")
                    }
                }
            }
        }
    }

    class Factory(private val app: LedgerApplication) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
            PinViewModel(app) as T
    }
}
