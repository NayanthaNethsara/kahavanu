package com.kahavanu.data.settings

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.kahavanu.data.common.awaitResult
import com.kahavanu.data.settings.local.UserSettingsDao
import com.kahavanu.data.settings.local.UserSettingsEntity
import com.kahavanu.domain.model.CurrencyOption
import com.kahavanu.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultSettingsRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val userSettingsDao: UserSettingsDao,
) : SettingsRepository {
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    init {
        observeRemoteSettings()
    }

    override fun observeCurrencySettings(): Flow<Pair<CurrencyOption, CurrencyOption>> {
        val uid = auth.currentUser?.uid ?: return flowOf(CurrencyOption.LKR to CurrencyOption.USD)
        return userSettingsDao.observeSettings(uid).map { entity ->
            if (entity != null) {
                val primary = try {
                    CurrencyOption.valueOf(entity.primaryCurrency)
                } catch (e: Exception) {
                    CurrencyOption.LKR
                }
                val secondary = try {
                    CurrencyOption.valueOf(entity.secondaryCurrency)
                } catch (e: Exception) {
                    CurrencyOption.USD
                }
                primary to secondary
            } else {
                CurrencyOption.LKR to CurrencyOption.USD
            }
        }
    }

    override suspend fun updateCurrencySettings(
        primary: CurrencyOption,
        secondary: CurrencyOption,
    ): Result<Unit> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("User not authenticated"))

        val now = System.currentTimeMillis()
        userSettingsDao.upsert(
            UserSettingsEntity(
                userId = uid,
                primaryCurrency = primary.name,
                secondaryCurrency = secondary.name,
                updatedAtEpochMillis = now,
            )
        )

        val data = mapOf(
            "primaryCurrency" to primary.name,
            "secondaryCurrency" to secondary.name,
            "updatedAt" to now,
        )

        return firestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(SETTINGS_COLLECTION)
            .document(CONFIG_DOCUMENT)
            .set(data, SetOptions.merge())
            .awaitResult()
            .map { }
    }

    private fun observeRemoteSettings() {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(SETTINGS_COLLECTION)
            .document(CONFIG_DOCUMENT)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener

                val primaryCode = snapshot.getString("primaryCurrency") ?: return@addSnapshotListener
                val secondaryCode = snapshot.getString("secondaryCurrency") ?: return@addSnapshotListener
                val updatedAt = snapshot.getLong("updatedAt") ?: 0L

                repositoryScope.launch {
                    val local = userSettingsDao.getSettings(uid)
                    if (local == null || updatedAt > local.updatedAtEpochMillis) {
                        userSettingsDao.upsert(
                            UserSettingsEntity(
                                userId = uid,
                                primaryCurrency = primaryCode,
                                secondaryCurrency = secondaryCode,
                                updatedAtEpochMillis = updatedAt,
                            )
                        )
                    }
                }
            }
    }
}

private const val USERS_COLLECTION = "users"
private const val SETTINGS_COLLECTION = "settings"
private const val CONFIG_DOCUMENT = "config"
