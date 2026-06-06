package com.kahavanu.data.settings

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
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

    private var settingsListener: ListenerRegistration? = null

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val uid = firebaseAuth.currentUser?.uid
        if (uid == null) {
            stopSettingsListener()
            return@AuthStateListener
        }
        repositoryScope.launch {
            ensureDefaultSettings(uid)
        }
        startSettingsListener(uid)
    }

    init {
        auth.addAuthStateListener(authStateListener)
        auth.currentUser?.uid?.let { uid ->
            repositoryScope.launch {
                ensureDefaultSettings(uid)
            }
            startSettingsListener(uid)
        }
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
        // Preserve any existing settings (budget, toggles, last scan) instead of
        // replacing the whole row, which would reset them to defaults.
        val existing = userSettingsDao.getSettings(uid)
        userSettingsDao.upsert(
            (existing ?: UserSettingsEntity(
                userId = uid,
                primaryCurrency = primary.name,
                secondaryCurrency = secondary.name,
                updatedAtEpochMillis = now,
            )).copy(
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

    override fun observeAutoMatchDeposits(): Flow<Boolean> {
        val uid = auth.currentUser?.uid ?: return flowOf(true)
        return userSettingsDao.observeSettings(uid).map { it?.isAutoMatchDepositsEnabled ?: true }
    }

    override suspend fun updateAutoMatchDeposits(enabled: Boolean): Result<Unit> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("User not authenticated"))
        userSettingsDao.updateAutoMatchDeposits(uid, enabled)
        val data = mapOf("isAutoMatchDepositsEnabled" to enabled, "updatedAt" to System.currentTimeMillis())
        return firestore.collection(USERS_COLLECTION).document(uid)
            .collection(SETTINGS_COLLECTION).document(CONFIG_DOCUMENT)
            .set(data, SetOptions.merge()).awaitResult().map { }
    }

    override fun observePushAlerts(): Flow<Boolean> {
        val uid = auth.currentUser?.uid ?: return flowOf(true)
        return userSettingsDao.observeSettings(uid).map { it?.isPushAlertsEnabled ?: true }
    }

    override suspend fun updatePushAlerts(enabled: Boolean): Result<Unit> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("User not authenticated"))
        userSettingsDao.updatePushAlerts(uid, enabled)
        val data = mapOf("isPushAlertsEnabled" to enabled, "updatedAt" to System.currentTimeMillis())
        return firestore.collection(USERS_COLLECTION).document(uid)
            .collection(SETTINGS_COLLECTION).document(CONFIG_DOCUMENT)
            .set(data, SetOptions.merge()).awaitResult().map { }
    }

    override fun observeMonthlyBudget(): Flow<Double> {
        val uid = auth.currentUser?.uid ?: return flowOf(0.0)
        return userSettingsDao.observeSettings(uid).map { it?.monthlyBudget ?: 0.0 }
    }

    override suspend fun updateMonthlyBudget(amount: Double): Result<Unit> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("User not authenticated"))
        val sanitized = amount.coerceAtLeast(0.0)
        val now = System.currentTimeMillis()
        userSettingsDao.updateMonthlyBudget(uid, sanitized, now)
        val data = mapOf("monthlyBudget" to sanitized, "updatedAt" to now)
        return firestore.collection(USERS_COLLECTION).document(uid)
            .collection(SETTINGS_COLLECTION).document(CONFIG_DOCUMENT)
            .set(data, SetOptions.merge()).awaitResult().map { }
    }

    override suspend fun getLastSmsScanEpochMillis(): Long {
        val uid = auth.currentUser?.uid ?: return 0L
        return userSettingsDao.getLastSmsScan(uid) ?: 0L
    }

    override suspend fun updateLastSmsScanEpochMillis(epochMillis: Long) {
        val uid = auth.currentUser?.uid ?: return
        userSettingsDao.updateLastSmsScan(uid, epochMillis)

        val now = System.currentTimeMillis()
        val data = mapOf(
            "lastSmsScanEpochMillis" to epochMillis,
            "updatedAt" to now,
        )

        firestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(SETTINGS_COLLECTION)
            .document(CONFIG_DOCUMENT)
            .set(data, SetOptions.merge())
            .awaitResult()
    }

    private fun startSettingsListener(uid: String) {
        if (settingsListener != null) return
        settingsListener = firestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(SETTINGS_COLLECTION)
            .document(CONFIG_DOCUMENT)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener

                val primaryCode = snapshot.getString("primaryCurrency") ?: return@addSnapshotListener
                val secondaryCode = snapshot.getString("secondaryCurrency") ?: return@addSnapshotListener
                val updatedAt = snapshot.getLong("updatedAt") ?: 0L
                val lastSmsScan = snapshot.getLong("lastSmsScanEpochMillis") ?: 0L
                val autoMatch = snapshot.getBoolean("isAutoMatchDepositsEnabled") ?: true
                val pushAlerts = snapshot.getBoolean("isPushAlertsEnabled") ?: true

                repositoryScope.launch {
                    val local = userSettingsDao.getSettings(uid)
                    val monthlyBudget = snapshot.getDouble("monthlyBudget") ?: local?.monthlyBudget ?: 0.0
                    if (local == null || updatedAt > local.updatedAtEpochMillis) {
                        userSettingsDao.upsert(
                            UserSettingsEntity(
                                userId = uid,
                                primaryCurrency = primaryCode,
                                secondaryCurrency = secondaryCode,
                                updatedAtEpochMillis = updatedAt,
                                lastSmsScanEpochMillis = maxOf(local?.lastSmsScanEpochMillis ?: 0L, lastSmsScan),
                                isAutoMatchDepositsEnabled = autoMatch,
                                isPushAlertsEnabled = pushAlerts,
                                monthlyBudget = monthlyBudget,
                            )
                        )
                    }
                }
            }
    }

    private fun stopSettingsListener() {
        settingsListener?.remove()
        settingsListener = null
    }

    private suspend fun ensureDefaultSettings(uid: String) {
        val existing = userSettingsDao.getSettings(uid)
        if (existing != null) return

        val now = System.currentTimeMillis()
        val primary = CurrencyOption.LKR
        val secondary = CurrencyOption.USD

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
            "isAutoMatchDepositsEnabled" to true,
            "isPushAlertsEnabled" to true,
            "updatedAt" to now,
        )

        firestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(SETTINGS_COLLECTION)
            .document(CONFIG_DOCUMENT)
            .set(data, SetOptions.merge())
            .awaitResult()
    }
}

private const val USERS_COLLECTION = "users"
private const val SETTINGS_COLLECTION = "settings"
private const val CONFIG_DOCUMENT = "config"
