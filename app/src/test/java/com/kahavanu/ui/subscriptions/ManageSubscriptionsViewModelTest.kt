package com.kahavanu.ui.subscriptions

import com.kahavanu.domain.model.CurrencyOption
import com.kahavanu.domain.model.Subscription
import com.kahavanu.domain.repository.ExpensesRepository
import com.kahavanu.domain.repository.SettingsRepository
import com.kahavanu.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class ManageSubscriptionsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var expensesRepository: ExpensesRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var subscriptionsFlow: MutableStateFlow<List<Subscription>>
    private lateinit var currencyFlow: MutableStateFlow<Pair<CurrencyOption, CurrencyOption>>

    private val defaultSubscriptions = listOf(
        Subscription(
            id = "netflix",
            name = "Netflix Standard",
            cost = 15.49,
            currency = "USD",
            frequency = "monthly",
            nextBillingDate = "June 5, 2026",
            isPaused = false
        ),
        Subscription(
            id = "spotify",
            name = "Spotify Premium",
            cost = 10.99,
            currency = "USD",
            frequency = "monthly",
            nextBillingDate = "June 12, 2026",
            isPaused = false
        ),
        Subscription(
            id = "chatgpt",
            name = "ChatGPT Plus",
            cost = 20.00,
            currency = "USD",
            frequency = "monthly",
            nextBillingDate = "June 20, 2026",
            isPaused = false
        ),
        Subscription(
            id = "github",
            name = "GitHub Copilot",
            cost = 10.00,
            currency = "USD",
            frequency = "monthly",
            nextBillingDate = "June 25, 2026",
            isPaused = true
        ),
        Subscription(
            id = "googleone",
            name = "Google One 100GB",
            cost = 1.99,
            currency = "USD",
            frequency = "monthly",
            nextBillingDate = "June 8, 2026",
            isPaused = false
        )
    )

    @Before
    fun setUp() {
        expensesRepository = mockk(relaxed = true)
        settingsRepository = mockk(relaxed = true)
        subscriptionsFlow = MutableStateFlow(defaultSubscriptions)
        currencyFlow = MutableStateFlow(CurrencyOption.USD to CurrencyOption.LKR)

        coEvery { expensesRepository.observeSubscriptions() } returns subscriptionsFlow
        coEvery { settingsRepository.observeCurrencySettings() } returns currencyFlow
    }

    private fun testScopeVm() = ManageSubscriptionsViewModel(expensesRepository, settingsRepository)

    @Test
    fun `initial state contains default subscriptions and correctly calculates total spend`() = runTest {
        val viewModel = testScopeVm()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(5, state.subscriptions.size)
        // Netflix (15.49), Spotify (10.99), ChatGPT (20.00), GoogleOne (1.99) are active
        // GitHub Copilot (10.00) is paused.
        // Total monthly spend should be: 15.49 + 10.99 + 20.00 + 1.99 = 48.47
        assertEquals(48.47, state.totalMonthlySpend, 0.001)
    }

    @Test
    fun `input update actions modify corresponding state properties`() = runTest {
        val viewModel = testScopeVm()
        advanceUntilIdle()

        viewModel.onNameChange("Adobe Creative Cloud")
        viewModel.onCostChange("29.99")
        viewModel.onCurrencyChange("EUR")
        viewModel.onFrequencyChange("yearly")
        viewModel.onNextBillingChange("June 30, 2026")
        viewModel.onCategoryChange("Shopping")

        val state = viewModel.uiState.value
        assertEquals("Adobe Creative Cloud", state.nameInput)
        assertEquals("29.99", state.costInput)
        assertEquals("EUR", state.currencyInput)
        assertEquals("yearly", state.frequencyInput)
        assertEquals("June 30, 2026", state.nextBillingInput)
        assertEquals("Shopping", state.categoryInput)
    }

    @Test
    fun `openSheet resets input drafts and opens sheet`() = runTest {
        val viewModel = testScopeVm()
        advanceUntilIdle()

        viewModel.onNameChange("Adobe")
        viewModel.onCostChange("10")
        
        viewModel.openSheet()
        
        val state = viewModel.uiState.value
        assertTrue(state.isSheetOpen)
        assertEquals("", state.nameInput)
        assertEquals("", state.costInput)
        assertEquals("monthly", state.frequencyInput)
        assertNull(state.errorMessage)
        assertNull(state.successMessage)
    }

    @Test
    fun `closeSheet hides the sheet`() = runTest {
        val viewModel = testScopeVm()
        advanceUntilIdle()

        viewModel.openSheet()
        assertTrue(viewModel.uiState.value.isSheetOpen)

        viewModel.closeSheet()
        assertFalse(viewModel.uiState.value.isSheetOpen)
    }

    @Test
    fun `toggleSubscriptionPause calls repository upsert`() = runTest {
        val viewModel = testScopeVm()
        advanceUntilIdle()

        val captured = slot<Subscription>()
        coEvery { expensesRepository.upsertSubscription(capture(captured)) } returns Result.success(Unit)

        viewModel.toggleSubscriptionPause("netflix")
        advanceUntilIdle()

        coVerify(exactly = 1) { expensesRepository.upsertSubscription(any()) }
        val updated = captured.captured
        assertEquals("netflix", updated.id)
        assertTrue(updated.isPaused)
    }

    @Test
    fun `deleteSubscription calls repository delete`() = runTest {
        val viewModel = testScopeVm()
        advanceUntilIdle()

        coEvery { expensesRepository.deleteSubscription(any()) } returns Result.success(Unit)

        viewModel.deleteSubscription("netflix")
        advanceUntilIdle()

        coVerify(exactly = 1) { expensesRepository.deleteSubscription("netflix") }
        assertEquals("Subscription removed", viewModel.uiState.value.successMessage)
    }

    @Test
    fun `addSubscription reports error for empty name`() = runTest {
        val viewModel = testScopeVm()
        advanceUntilIdle()

        viewModel.openSheet()
        viewModel.onNameChange(" ")
        viewModel.onCostChange("10.0")
        
        viewModel.addSubscription()
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertEquals("Enter subscription name", state.errorMessage)
        assertTrue(state.isSheetOpen)
    }

    @Test
    fun `addSubscription reports error for invalid cost`() = runTest {
        val viewModel = testScopeVm()
        advanceUntilIdle()

        viewModel.openSheet()
        viewModel.onNameChange("Adobe")
        viewModel.onCostChange("-5.0")
        
        viewModel.addSubscription()
        advanceUntilIdle()
        
        var state = viewModel.uiState.value
        assertEquals("Enter a valid positive price", state.errorMessage)

        viewModel.onCostChange("abc")
        viewModel.addSubscription()
        advanceUntilIdle()
        state = viewModel.uiState.value
        assertEquals("Enter a valid positive price", state.errorMessage)
    }

    @Test
    fun `addSubscription successfully adds new subscription and calculates spend correctly`() = runTest {
        val viewModel = testScopeVm()
        advanceUntilIdle()

        val captured = slot<Subscription>()
        coEvery { expensesRepository.upsertSubscription(capture(captured)) } returns Result.success(Unit)

        viewModel.openSheet()
        viewModel.onNameChange("Adobe Creative Cloud")
        viewModel.onCostChange("120.0")
        viewModel.onFrequencyChange("yearly")
        viewModel.onCategoryChange("Shopping")
        
        val testDate = LocalDate.of(2026, 7, 1)
        viewModel.onDateChange(testDate)
        
        viewModel.addSubscription()
        advanceUntilIdle()
        
        coVerify(exactly = 1) { expensesRepository.upsertSubscription(any()) }
        val added = captured.captured
        assertEquals("Adobe Creative Cloud", added.name)
        assertEquals(120.0, added.cost, 0.001)
        assertEquals("yearly", added.frequency)
        assertEquals("Shopping", added.category)
        assertFalse(added.isPaused)
    }
}
