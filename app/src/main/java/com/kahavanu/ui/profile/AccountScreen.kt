package com.kahavanu.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kahavanu.ui.common.textFieldColors
import com.kahavanu.ui.theme.Spacing

@Composable
fun AccountScreen(
    onBack: () -> Unit,
    viewModel: AccountViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = Spacing.extraLarge),
            verticalArrangement = Arrangement.spacedBy(Spacing.large),
        ) {
            ProfileSubScreenHeader(
                title = "Account",
                onBack = onBack,
            )

            OutlinedTextField(
                value = state.displayNameInput,
                onValueChange = viewModel::onDisplayNameChange,
                label = { Text(text = "Name") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = textFieldColors(),
                enabled = !state.isSavingName,
            )

            if (state.isDisplayNameDirty) {
                Button(
                    onClick = viewModel::saveDisplayName,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.displayNameInput.trim().isNotBlank() && !state.isSavingName,
                ) {
                    if (state.isSavingName) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(text = "Save name")
                    }
                }
            }

            OutlinedTextField(
                value = state.user?.email.orEmpty(),
                onValueChange = {},
                label = { Text(text = "Email") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                enabled = false,
                colors = textFieldColors(),
            )

            Spacer(modifier = Modifier.weight(1f))

            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            state.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            ProfileActionButton(
                text = "Logout",
                icon = Icons.AutoMirrored.Filled.Logout,
                onClick = viewModel::signOut,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                ),
            )

            ProfileActionButton(
                text = "Delete Account",
                icon = Icons.Default.DeleteForever,
                onClick = { viewModel.showDeleteConfirmation(true) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                ),
            )

            Spacer(modifier = Modifier.height(Spacing.large))
        }
    }

    if (state.showDeleteConfirmation) {
        DeleteAccountConfirmationDialog(
            onConfirm = viewModel::deleteAccount,
            onDismiss = { viewModel.showDeleteConfirmation(false) },
        )
    }
}

@Composable
private fun ProfileActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    colors: androidx.compose.material3.ButtonColors = ButtonDefaults.buttonColors(),
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = colors,
        shape = MaterialTheme.shapes.medium,
    ) {
        Icon(imageVector = icon, contentDescription = null)
        Spacer(modifier = Modifier.size(Spacing.small))
        Text(text = text)
    }
}

@Composable
private fun DeleteAccountConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Delete Account") },
        text = {
            Text(
                text = "Are you sure you want to delete your account? This action is permanent and cannot be undone.",
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text(text = "Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        },
    )
}
