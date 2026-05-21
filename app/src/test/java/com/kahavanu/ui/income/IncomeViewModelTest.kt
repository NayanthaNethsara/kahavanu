package com.kahavanu.ui.income

import app.cash.turbine.test
import com.kahavanu.domain.model.CurrencyOption
import com.kahavanu.domain.model.IncomeLogEntry
import com.kahavanu.domain.model.IncomeLogResult
import com.kahavanu.domain.model.IncomeSource
import com.kahavanu.domain.model.IncomeSourceType
import com.kahavanu.domain.model.ScheduledIncome
import com.kahavanu.domain.repository.IncomeRepository
import com.kahavanu.domain.repository.SettingsRepository
import com.kahavanu.testing.MainDispatcherRule
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
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
class IncomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var incomeRepository: IncomeRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var sourcesFlow: MutableStateFlow<List<IncomeSource>>
    private lateinit var currencyFlow: MutableStateFlow<Pair<CurrencyOption, CurrencyOption>>

    private val defaultSources = listOf(
        IncomeSource(id = 1L, name = "Salary", types = setOf(IncomeSourceType.RECURRENT, IncomeSourceType.ONE_TIME)),
        IncomeSource(id = 2L, name = "Freelance", types = setOf(IncomeSourceType.ONE_TIME, IncomeSourceType.PENDING)),
        IncomeSource(id = 3L, name = "Bonus", types = setOf(IncomeSourceType.ONE_TIME)),
    )

    @Before
    fun setUp() {
        incomeRepository = mockk(relaxed = true)
        settingsRepository = mockk(relaxed = true)
        sourcesFlow = MutableStateFlow(defaultSources)
        currencyFlow = MutableStateFlow(CurrencyOption.LKR to CurrencyOption.USD)

        coEvery { incomeRepository.ensureDefaultSources() } just Runs
        coEvery { incomeRepository.observeIncomeSources() } returns sourcesFlow
        coEvery { settingsRepository.observeCurrencySettings() } returns currencyFlow
    }

    private fun TestScopeVm() = IncomeViewModel(incomeRepository, settingsRepository)

    @Test
    fun `init ensures default sources and populates state from flows`() = runTest {
        val vm = TestScopeVm()
        advanceUntilIdle()

        coVerify { incomeRepository.ensureDefaultSources() }
        val state = vm.uiState.value
        assertEquals(defaultSources, state.sources)
        assertEquals(1L, state.selectedSourceId)
        assertEquals(
            listOf(CurrencyOption.LKR, CurrencyOption.USD),
            state.availableCurrencies,
        )
        assertEquals(CurrencyOption.LKR, state.currency)
    }

    @Test
    fun `onAmountChange strips non-numeric and collapses multiple decimal separators`() = runTest {
        val vm = TestScopeVm()
        advanceUntilIdle()

        vm.onAmountChange("1a2b3.45.6")
        assertEquals("123.45", vm.uiState.value.amount)

        vm.onAmountChange("abc")
        assertEquals("", vm.uiState.value.amount)

        vm.onAmountChange("12.34")
        assertEquals("12.34", vm.uiState.value.amount)
    }

    @Test
    fun `changing income type re-resolves source if current source does not support new type`() = runTest {
        val vm = TestScopeVm()
        advanceUntilIdle()

        vm.onSourceChange(3L)
        assertEquals(3L, vm.uiState.value.selectedSourceId)

        vm.onIncomeTypeChange(IncomeSourceType.PENDING)
        advanceUntilIdle()

        assertEquals(IncomeSourceType.PENDING, vm.uiState.value.incomeType)
        assertEquals(2L, vm.uiState.value.selectedSourceId)
    }

    @Test
    fun `changing income type keeps source if it still supports the new type`() = runTest {
        val vm = TestScopeVm()
        advanceUntilIdle()

        vm.onSourceChange(1L)
        vm.onIncomeTypeChange(IncomeSourceType.RECURRENT)
        advanceUntilIdle()

        assertEquals(1L, vm.uiState.value.selectedSourceId)
    }

    @Test
    fun `onCurrencyChange updates currency`() = runTest {
        val vm = TestScopeVm()
        advanceUntilIdle()

        vm.onCurrencyChange(CurrencyOption.USD)
        assertEquals(CurrencyOption.USD, vm.uiState.value.currency)
    }

    @Test
    fun `onDateChange updates date and closes the picker`() = runTest {
        val vm = TestScopeVm()
        advanceUntilIdle()

        vm.onDatePickerOpenChange(true)
        assertTrue(vm.uiState.value.isDatePickerOpen)

        val date = LocalDate.of(2025, 1, 15)
        vm.onDateChange(date)

        val state = vm.uiState.value
        assertEquals(date, state.receivedDate)
        assertFalse(state.isDatePickerOpen)
    }

    @Test
    fun `onContactSaved and onClearContact mutate contact fields`() = runTest {
        val vm = TestScopeVm()
        advanceUntilIdle()

        vm.onContactSaved("Alice", "+94123")
        var state = vm.uiState.value
        assertEquals("Alice", state.contactName)
        assertEquals("+94123", state.contactNumber)

        vm.onClearContact()
        state = vm.uiState.value
        assertNull(state.contactName)
        assertNull(state.contactNumber)
    }

    @Test
    fun `currency settings flow resets currency to primary when current is no longer available`() = runTest {
        val vm = TestScopeVm()
        advanceUntilIdle()
        vm.onCurrencyChange(CurrencyOption.USD)

        currencyFlow.value = CurrencyOption.LKR to CurrencyOption.EUR
        advanceUntilIdle()

        assertEquals(CurrencyOption.LKR, vm.uiState.value.currency)
        assertEquals(
            listOf(CurrencyOption.LKR, CurrencyOption.EUR),
            vm.uiState.value.availableCurrencies,
        )
    }

    @Test
    fun `logIncome shows validation error for blank description`() = runTest {
        val vm = TestScopeVm()
        advanceUntilIdle()

        vm.onAmountChange("100")
        vm.logIncome()
        advanceUntilIdle()

        assertEquals(
            "Enter a client/description and valid amount",
            vm.uiState.value.errorMessage,
        )
        coVerify(exactly = 0) { incomeRepository.logIncome(any()) }
    }

    @Test
    fun `logIncome shows validation error for non-positive amount`() = runTest {
        val vm = TestScopeVm()
        advanceUntilIdle()

        vm.onClientDescriptionChange("Project X")
        vm.onAmountChange("0")
        vm.logIncome()
        advanceUntilIdle()

        assertEquals(
            "Enter a client/description and valid amount",
            vm.uiState.value.errorMessage,
        )
    }

    @Test
    fun `logIncome shows error when no source is selected`() = runTest {
        sourcesFlow.value = emptyList()
        val vm = TestScopeVm()
        advanceUntilIdle()

        vm.onClientDescriptionChange("Client")
        vm.onAmountChange("100")
        vm.logIncome()
        advanceUntilIdle()

        assertEquals("Select an income source", vm.uiState.value.errorMessage)
    }

    @Test
    fun `logIncome with ONE_TIME persists IncomeLogEntry and posts synced message`() = runTest {
        val captured = slot<IncomeLogEntry>()
        coEvery { incomeRepository.logIncome(capture(captured)) } returns
            Result.success(IncomeLogResult.SYNCED)

        val vm = TestScopeVm()
        advanceUntilIdle()

        vm.onClientDescriptionChange("ACME")
        vm.onAmountChange("250.50")
        vm.onCurrencyChange(CurrencyOption.USD)
        vm.onContactSaved("Bob", "+1000")
        vm.onSourceChange(1L)
        vm.logIncome()
        advanceUntilIdle()

        coVerify(exactly = 1) { incomeRepository.logIncome(any()) }
        val entry = captured.captured
        assertEquals("ACME", entry.title)
        assertEquals(250.50, entry.amount, 0.0)
        assertEquals("USD", entry.currency)
        assertEquals(1L, entry.sourceId)
        assertEquals("Salary", entry.sourceName)
        assertEquals(IncomeSourceType.ONE_TIME.id, entry.sourceType)
        assertEquals("Bob", entry.contactName)
        assertEquals("+1000", entry.contactNumber)

        val state = vm.uiState.value
        assertEquals("Income logged", state.successMessage)
        assertEquals("", state.clientDescription)
        assertEquals("", state.amount)
        assertFalse(state.isSaving)
    }

    @Test
    fun `logIncome falls back to contact name when description is blank`() = runTest {
        val captured = slot<IncomeLogEntry>()
        coEvery { incomeRepository.logIncome(capture(captured)) } returns
            Result.success(IncomeLogResult.LOCAL_ONLY)

        val vm = TestScopeVm()
        advanceUntilIdle()

        vm.onContactSaved("Charlie", null)
        vm.onAmountChange("10")
        vm.logIncome()
        advanceUntilIdle()

        assertEquals("Charlie", captured.captured.title)
        assertEquals(
            "Saved offline. Will sync when online.",
            vm.uiState.value.successMessage,
        )
    }

    @Test
    fun `logIncome for RECURRENT persists ScheduledIncome with frequency`() = runTest {
        val captured = slot<ScheduledIncome>()
        coEvery { incomeRepository.upsertScheduledIncome(capture(captured)) } returns
            Result.success(Unit)

        val vm = TestScopeVm()
        advanceUntilIdle()

        vm.onIncomeTypeChange(IncomeSourceType.RECURRENT)
        vm.onClientDescriptionChange("Salary - Acme")
        vm.onAmountChange("5000")
        vm.onFrequencyChange(RecurrenceFrequency.WEEKLY)
        vm.logIncome()
        advanceUntilIdle()

        coVerify(exactly = 1) { incomeRepository.upsertScheduledIncome(any()) }
        coVerify(exactly = 0) { incomeRepository.logIncome(any()) }

        val scheduled = captured.captured
        assertEquals("Salary - Acme", scheduled.title)
        assertEquals(IncomeSourceType.RECURRENT, scheduled.type)
        assertEquals(RecurrenceFrequency.WEEKLY.label, scheduled.frequency)
        assertEquals("Scheduled income saved", vm.uiState.value.successMessage)
    }

    @Test
    fun `logIncome for PENDING persists ScheduledIncome without frequency`() = runTest {
        val captured = slot<ScheduledIncome>()
        coEvery { incomeRepository.upsertScheduledIncome(capture(captured)) } returns
            Result.success(Unit)

        val vm = TestScopeVm()
        advanceUntilIdle()

        vm.onIncomeTypeChange(IncomeSourceType.PENDING)
        vm.onClientDescriptionChange("Invoice 42")
        vm.onAmountChange("750")
        vm.logIncome()
        advanceUntilIdle()

        val scheduled = captured.captured
        assertEquals(IncomeSourceType.PENDING, scheduled.type)
        assertNull(scheduled.frequency)
    }

    @Test
    fun `logIncome surfaces repository error message on failure`() = runTest {
        coEvery { incomeRepository.logIncome(any()) } returns
            Result.failure(IllegalStateException("disk full"))

        val vm = TestScopeVm()
        advanceUntilIdle()

        vm.onClientDescriptionChange("Client")
        vm.onAmountChange("100")
        vm.logIncome()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals("disk full", state.errorMessage)
        assertFalse(state.isSaving)
    }

    @Test
    fun `logIncome uses generic error when failure has no message`() = runTest {
        coEvery { incomeRepository.logIncome(any()) } returns
            Result.failure(RuntimeException())

        val vm = TestScopeVm()
        advanceUntilIdle()

        vm.onClientDescriptionChange("Client")
        vm.onAmountChange("100")
        vm.logIncome()
        advanceUntilIdle()

        assertEquals("Could not save income", vm.uiState.value.errorMessage)
    }

    @Test
    fun `mutating state clears prior error and success messages`() = runTest {
        coEvery { incomeRepository.logIncome(any()) } returns
            Result.success(IncomeLogResult.SYNCED)

        val vm = TestScopeVm()
        advanceUntilIdle()

        vm.onClientDescriptionChange("Client")
        vm.onAmountChange("100")
        vm.logIncome()
        advanceUntilIdle()
        assertNotNull(vm.uiState.value.successMessage)

        vm.onAmountChange("200")
        assertNull(vm.uiState.value.successMessage)
        assertNull(vm.uiState.value.errorMessage)
    }

    @Test
    fun `uiState emits sources update when repository flow emits new sources`() = runTest {
        val vm = TestScopeVm()
        advanceUntilIdle()

        vm.uiState.test {
            assertEquals(defaultSources, awaitItem().sources)

            val newSources = defaultSources + IncomeSource(
                id = 4L,
                name = "Investments",
                types = setOf(IncomeSourceType.RECURRENT),
            )
            sourcesFlow.value = newSources
            assertEquals(newSources, awaitItem().sources)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
