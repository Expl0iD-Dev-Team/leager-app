package com.ledger.app.ui.screen.pin

import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ledger.app.LedgerApplication
import com.ledger.app.ui.components.IconBubble
import com.ledger.app.ui.components.LedgerIcons
import com.ledger.app.ui.theme.AppFont
import com.ledger.app.ui.theme.ledger

@Composable
fun PinScreen(
    app: LedgerApplication,
    onSuccess: () -> Unit,
    forceSetup: Boolean = false
) {
    val vm: PinViewModel = viewModel(factory = PinViewModel.Factory(app))
    val state by vm.state.collectAsState()
    val c = MaterialTheme.ledger
    val context = LocalContext.current
    val biometricEnabled by app.securityManager.isBiometricEnabled.collectAsState(initial = false)

    LaunchedEffect(forceSetup) {
        if (forceSetup) vm.forceSetMode()
    }

    LaunchedEffect(state.success) {
        if (state.success) onSuccess()
    }

    LaunchedEffect(biometricEnabled) {
        if (biometricEnabled && state.mode == PinMode.VERIFY) {
            val activity = context as? FragmentActivity ?: return@LaunchedEffect
            val executor = ContextCompat.getMainExecutor(activity)
            val callback = object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }
            }
            val prompt = BiometricPrompt(activity, executor, callback)
            val info = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock Leager")
                .setSubtitle("Confirm it's you")
                .setNegativeButtonText("Use PIN")
                .build()
            prompt.authenticate(info)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        IconBubble(LedgerIcons.Lock, c.onAccent, size = 64.dp, corner = 22.dp, background = c.accent)
        Spacer(Modifier.height(20.dp))
        Text(
            when (state.mode) {
                PinMode.VERIFY  -> "Enter your PIN"
                PinMode.SET     -> "Create a PIN"
                PinMode.CONFIRM -> "Confirm your PIN"
            },
            style = MaterialTheme.typography.titleLarge,
            color = c.text
        )
        Spacer(Modifier.height(6.dp))
        Text(
            if (state.error.isNotEmpty()) state.error else "4 digits",
            fontFamily = AppFont,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            color = if (state.error.isNotEmpty()) c.danger else c.muted
        )

        Spacer(Modifier.height(28.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(4) { idx ->
                val filled = idx < state.digits.length
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(if (filled) c.text else Color.Transparent)
                        .border(1.5.dp, if (filled) c.text else c.borderStrong, CircleShape)
                )
            }
        }

        Spacer(Modifier.height(44.dp))

        val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "", "0", "⌫")
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            keys.chunked(3).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    row.forEach { key ->
                        if (key.isEmpty()) {
                            Spacer(Modifier.size(72.dp))
                        } else {
                            val isBackspace = key == "⌫"
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(if (isBackspace) Color.Transparent else c.surface)
                                    .border(1.dp, if (isBackspace) Color.Transparent else c.border, CircleShape)
                                    .clickable { if (isBackspace) vm.onBackspace() else vm.onDigit(key) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isBackspace) {
                                    Icon(LedgerIcons.Backspace, contentDescription = "Delete", tint = c.muted, modifier = Modifier.size(24.dp))
                                } else {
                                    Text(key, fontFamily = AppFont, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, color = c.text)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
