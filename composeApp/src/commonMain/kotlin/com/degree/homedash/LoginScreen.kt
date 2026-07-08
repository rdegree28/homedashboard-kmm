package com.degree.homedash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.degree.homedash.shared.data.AuthUser
import com.degree.homedash.shared.data.Users
import com.degree.homedash.ui.AppColors

/**
 * Local app login gate: pick a user and enter their PIN. [onLogin] validates the pair and returns
 * true on success (the caller then navigates away); false shows an inline error.
 */
@Composable
fun LoginScreen(
    users: List<AuthUser>,
    onLogin: (AuthUser, String) -> Boolean,
) {
    var selectedUser by remember { mutableStateOf<AuthUser?>(null) }
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Who's using this?",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            users.forEach { user ->
                val active = user == selectedUser
                Button(
                    onClick = {
                        selectedUser = user
                        error = false
                    },
                    modifier = Modifier.weight(1f).height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (active) AppColors.Accent else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (active) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                ) {
                    Text(text = user.name, maxLines = 1, style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        OutlinedTextField(
            value = pin,
            onValueChange = {
                pin = it
                error = false
            },
            label = { Text("PIN") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            isError = error,
            modifier = Modifier.fillMaxWidth(),
        )

        if (error) {
            Text(
                text = "Incorrect PIN. Try again.",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.StatusRed,
            )
        }

        Button(
            onClick = {
                val user = selectedUser ?: return@Button
                if (!onLogin(user, pin)) {
                    error = true
                    pin = ""
                }
            },
            enabled = selectedUser != null && pin.isNotBlank(),
        ) {
            Text("Log in")
        }
    }
}

@Preview(widthDp = 380, heightDp = 500)
@Composable
private fun LoginScreenPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            LoginScreen(users = Users.all, onLogin = { _, _ -> false })
        }
    }
}
