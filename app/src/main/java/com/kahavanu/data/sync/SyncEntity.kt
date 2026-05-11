package com.kahavanu.data.sync

/**
 * Marker interface for entities that can be synced with Firebase.
 * Implementations should track sync metadata: remote ID, sync status, timestamps.
 */
interface SyncEntity {
    val localId: Long
    val remoteId: String?
    val isSynced: Boolean
    val isDeleted: Boolean
    val updatedAtEpochMillis: Long
    
    /**
     * Convert entity to a map for Firestore storage.
     */
    fun toFirestoreMap(): Map<String, Any?>
}
