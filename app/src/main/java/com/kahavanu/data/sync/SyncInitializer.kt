package com.kahavanu.data.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Automatically triggers sync when network becomes available.
 * 
 * Designed to work with any SyncManager implementation.
 */
@Singleton
class SyncInitializer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val syncManager: SyncManager,
) {
    private val syncScope = CoroutineScope(Dispatchers.Default)
    
    fun init() {
        initNetworkCallback()
    }
    
    private fun initNetworkCallback() {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) 
            as? ConnectivityManager ?: return
        
        connectivityManager.registerDefaultNetworkCallback(
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    syncScope.launch {
                        syncManager.sync()
                    }
                }
            }
        )
    }
}
