package com.kahavanu.data.sync

import kotlinx.coroutines.flow.StateFlow

/**
 * Manages synchronization between local Room database and Firebase Firestore.
 * Firebase is the source of truth.
 * 
 * Responsibilities:
 * - Push pending local changes to Firebase
 * - Pull latest state from Firebase (delta sync)
 * - Resolve conflicts: Firebase version wins on timestamp basis
 * - Publish sync state for UI
 */
interface SyncManager {
    /**
     * Observable sync state for UI binding.
     */
    val syncState: StateFlow<SyncState>
    
    /**
     * Perform a full sync cycle:
     * 1. Push any pending local changes
     * 2. Pull latest from Firebase (only changes since last sync)
     * 3. Resolve conflicts (Firebase wins)
     */
    suspend fun sync(): Result<Unit>
    
    /**
     * Get the timestamp of the last successful sync.
     */
    fun getLastSyncTimestamp(): Long
}
