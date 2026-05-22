package com.kahavanu.ui.subscriptions

import com.kahavanu.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

@OptIn(ExperimentalCoroutinesApi::class)
class ManageSubscriptionsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: ManageSubscriptionsViewModel

    @Before
    fun setUp() {
        viewModel = ManageSubscriptionsViewModel()
    }

    @Test
    fun `initial state contains default subscriptions and correctly calculates total spend`() {
        val state = viewModel.uiState.value
        assertEquals(5, state.subscriptions.size)
        // Netflix (15.49), Spotify (10.99), ChatGPT (20.00), GoogleOne (1.99) are active
        // GitHub Copilot (10.00) is paused.
        // Total monthly spend should be: 15.49 + 10.99 + 20.00 + 1.99 = 48.47
        assertEquals(48.47, state.totalMonthlySpend, 0.001)
    }

    @Test
    fun `input update actions modify corresponding state properties`() {
        viewModel.onNameChange("Adobe Creative Cloud")
        viewModel.onCostChange("29.99")
        viewModel.onCurrencyChange("EUR")
        viewModel.onFrequencyChange("yearly")
        viewModel.onNextBillingChange("June 30, 2026")

        val state = viewModel.uiState.value
        assertEquals("Adobe Creative Cloud", state.nameInput)
        assertEquals("29.99", state.costInput)
        assertEquals("EUR", state.currencyInput)
        assertEquals("yearly", state.frequencyInput)
        assertEquals("June 30, 2026", state.nextBillingInput)
    }

    @Test
    fun `openSheet resets input drafts and opens sheet`() {
        viewModel.onNameChange("Adobe")
        viewModel.onCostChange("10")
        
        viewModel.openSheet()
        
        val state = viewModel.uiState.value
        assertTrue(state.isSheetOpen)
        assertEquals("", state.nameInput)
        assertEquals("", state.costInput)
        assertEquals("USD", state.currencyInput)
        assertEquals("monthly", state.frequencyInput)
        assertEquals("", state.nextBillingInput)
        assertNull(state.errorMessage)
        assertNull(state.successMessage)
    }

    @Test
    fun `closeSheet hides the sheet`() {
        viewModel.openSheet()
        assertTrue(viewModel.uiState.value.isSheetOpen)

        viewModel.closeSheet()
        assertFalse(viewModel.uiState.value.isSheetOpen)
    }

    @Test
    fun `toggleSubscriptionPause updates pause state and total spend calculation`() {
        // Initially Netflix is active (15.49), GitHub Copilot is paused (10.00)
        // Let's pause Netflix
        viewModel.toggleSubscriptionPause("netflix")
        var state = viewModel.uiState.value
        val netflix = state.subscriptions.first { it.id == "netflix" }
        assertTrue(netflix.isPaused)
        
        // Total monthly spend: 48.47 - 15.49 = 32.98
        assertEquals(32.98, state.totalMonthlySpend, 0.001)

        // Now resume GitHub Copilot
        viewModel.toggleSubscriptionPause("github")
        state = viewModel.uiState.value
        val github = state.subscriptions.first { it.id == "github" }
        assertFalse(github.isPaused)
        
        // Total monthly spend: 32.98 + 10.00 = 42.98
        assertEquals(42.98, state.totalMonthlySpend, 0.001)
    }

    @Test
    fun `deleteSubscription removes subscription from the list`() {
        val initialSize = viewModel.uiState.value.subscriptions.size
        
        viewModel.deleteSubscription("netflix")
        
        val state = viewModel.uiState.value
        assertEquals(initialSize - 1, state.subscriptions.size)
        assertFalse(state.subscriptions.any { it.id == "netflix" })
        assertEquals("Subscription removed", state.successMessage)
    }

    @Test
    fun `addSubscription reports error for empty name`() {
        viewModel.openSheet()
        viewModel.onNameChange(" ")
        viewModel.onCostChange("10.0")
        viewModel.onNextBillingChange("June 1")
        
        viewModel.addSubscription()
        
        val state = viewModel.uiState.value
        assertEquals("Enter subscription name", state.errorMessage)
        assertTrue(state.isSheetOpen)
    }

    @Test
    fun `addSubscription reports error for invalid cost`() {
        viewModel.openSheet()
        viewModel.onNameChange("Adobe")
        viewModel.onCostChange("-5.0")
        viewModel.onNextBillingChange("June 1")
        
        viewModel.addSubscription()
        
        var state = viewModel.uiState.value
        assertEquals("Enter a valid positive price", state.errorMessage)

        viewModel.onCostChange("abc")
        viewModel.addSubscription()
        state = viewModel.uiState.value
        assertEquals("Enter a valid positive price", state.errorMessage)
    }

    @Test
    fun `addSubscription reports error for empty next billing date`() {
        viewModel.openSheet()
        viewModel.onNameChange("Adobe")
        viewModel.onCostChange("20.0")
        viewModel.onNextBillingChange(" ")
        
        viewModel.addSubscription()
        
        val state = viewModel.uiState.value
        assertEquals("Enter next billing date", state.errorMessage)
    }

    @Test
    fun `addSubscription successfully adds new subscription and calculates spend correctly`() {
        val initialSize = viewModel.uiState.value.subscriptions.size

        viewModel.openSheet()
        viewModel.onNameChange("Adobe Creative Cloud")
        viewModel.onCostChange("120.0")
        viewModel.onFrequencyChange("yearly")
        viewModel.onNextBillingChange("July 1, 2026")
        
        viewModel.addSubscription()
        
        val state = viewModel.uiState.value
        assertEquals(initialSize + 1, state.subscriptions.size)
        assertFalse(state.isSheetOpen)
        assertEquals("Subscription added successfully!", state.successMessage)
        
        val added = state.subscriptions.last()
        assertEquals("Adobe Creative Cloud", added.name)
        assertEquals(120.0, added.cost, 0.001)
        assertEquals("yearly", added.frequency)
        assertEquals("July 1, 2026", added.nextBillingDate)
        assertFalse(added.isPaused)

        // 120.0 yearly is 10.0 monthly.
        // Total monthly spend: 48.47 + 10.0 = 58.47
        assertEquals(58.47, state.totalMonthlySpend, 0.001)
    }
}
