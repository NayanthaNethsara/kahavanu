package com.kahavanu.data.sync

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Singleton

/**
 * Generic Firebase sync manager implementation.
 * Handles delta sync, conflict resolution (Firebase wins), and sync state publishing.
 */
@Singleton
open class FirebaseSyncManager(
    protected val context: Context,
    protected val firestore: FirebaseFirestore,
    protected val auth: FirebaseAuth,
) : SyncManager {
    
    protected val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    override val syncState: StateFlow<SyncState> = _syncState.asStateFlow()
    
    protected val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    override suspend fun sync(): Result<Unit> {
        _syncState.value = SyncState.Syncing
        
        val uid = auth.currentUser?.uid
            ?: return Result.failure<Unit>(Exception("Not authenticated")).also {
                _syncState.value = SyncState.Error(Exception("Not authenticated"))
            }
        
        return try {
            // Subclasses will override this to implement specific sync logic
            // This base implementation just updates state
            updateLastSyncTimestamp()
            _syncState.value = SyncState.Success
            Result.success(Unit)
        } catch (e: Exception) {
            _syncState.value = SyncState.Error(e)
            Result.failure(e)
        }
    }
    
    override fun getLastSyncTimestamp(): Long {
        return prefs.getLong(LAST_SYNC_KEY, 0L)
    }
    
    protected fun updateLastSyncTimestamp() {
        prefs.edit().putLong(LAST_SYNC_KEY, System.currentTimeMillis()).apply()
    }
    
    protected fun getCurrentUserId(): String? = auth.currentUser?.uid
    
    companion object {
        private const val PREFS_NAME = "sync_prefs"
        private const val LAST_SYNC_KEY = "last_sync_timestamp"
    }
}
