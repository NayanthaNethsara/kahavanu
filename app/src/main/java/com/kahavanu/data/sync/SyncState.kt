package com.kahavanu.data.sync

/**
 * Represents the state of a sync operation.
 */
sealed class SyncState {
    object Idle : SyncState()
    object Syncing : SyncState()
    object Success : SyncState()
    data class Error(val exception: Exception) : SyncState()
}
