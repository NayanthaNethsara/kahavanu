package com.kahavanu.ui.income

import com.kahavanu.domain.model.CurrencyOption
import com.kahavanu.domain.model.IncomeSource
import com.kahavanu.domain.model.IncomeSourceType
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

@OptIn(ExperimentalCoroutinesApi::class)
class IncomeSourcesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var incomeRepository: IncomeRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var sourcesFlow: MutableStateFlow<List<IncomeSource>>
    private lateinit var currencyFlow: MutableStateFlow<Pair<CurrencyOption, CurrencyOption>>

    private val existingSource = IncomeSource(
        id = 7L,
        name = "Freelance",
        types = setOf(IncomeSourceType.ONE_TIME, IncomeSourceType.PENDING),
    )

    @Before
    fun setUp() {
        incomeRepository = mockk(relaxed = true)
        settingsRepository = mockk(relaxed = true)
        sourcesFlow = MutableStateFlow(listOf(existingSource))
        currencyFlow = MutableStateFlow(CurrencyOption.LKR to CurrencyOption.USD)

        coEvery { incomeRepository.ensureDefaultSources() } just Runs
        coEvery { incomeRepository.observeIncomeSources() } returns sourcesFlow
        coEvery { settingsRepository.observeCurrencySettings() } returns currencyFlow
    }

    private fun newVm() = IncomeSourcesViewModel(incomeRepository, settingsRepository)

    @Test
    fun `init pulls sources and currency settings into state`() = runTest {
        val vm = newVm()
        advanceUntilIdle()

        coVerify { incomeRepository.ensureDefaultSources() }
        val state = vm.uiState.value
        assertEquals(listOf(existingSource), state.sources)
        assertEquals(CurrencyOption.LKR, state.primaryCurrency)
        assertEquals(CurrencyOption.USD, state.secondaryCurrency)
        assertEquals(CurrencyOption.LKR, state.primaryCurrencyDraft)
        assertEquals(CurrencyOption.USD, state.secondaryCurrencyDraft)
    }

    @Test
    fun `startCurrencyEdit toggles editing flag and seeds drafts from current values`() = runTest {
        val vm = newVm()
        advanceUntilIdle()

        vm.onPrimaryCurrencyDraftChange(CurrencyOption.EUR)
        vm.startCurrencyEdit()

        val state = vm.uiState.value
        assertTrue(state.isCurrencyEditing)
        assertEquals(CurrencyOption.LKR, state.primaryCurrencyDraft)
    }

    @Test
    fun `cancelCurrencyEdit clears the editing flag`() = runTest {
        val vm = newVm()
        advanceUntilIdle()
        vm.startCurrencyEdit()

        vm.cancelCurrencyEdit()
        assertFalse(vm.uiState.value.isCurrencyEditing)
    }

    @Test
    fun `saveCurrencySettings forwards drafts to the repository`() = runTest {
        coEvery {
            settingsRepository.updateCurrencySettings(any(), any())
        } returns Result.success(Unit)

        val vm = newVm()
        advanceUntilIdle()

        vm.startCurrencyEdit()
        vm.onPrimaryCurrencyDraftChange(CurrencyOption.EUR)
        vm.onSecondaryCurrencyDraftChange(CurrencyOption.GBP)
        vm.saveCurrencySettings()
        advanceUntilIdle()

        coVerify {
            settingsRepository.updateCurrencySettings(CurrencyOption.EUR, CurrencyOption.GBP)
        }
        assertFalse(vm.uiState.value.isCurrencyEditing)
    }

    @Test
    fun `onTypeToggle adds a type when missing and removes when present`() = runTest {
        val vm = newVm()
        advanceUntilIdle()

        val initial = vm.uiState.value.selectedTypes
        assertTrue(IncomeSourceType.RECURRENT in initial)

        vm.onTypeToggle(IncomeSourceType.RECURRENT)
        assertFalse(IncomeSourceType.RECURRENT in vm.uiState.value.selectedTypes)

        vm.onTypeToggle(IncomeSourceType.RECURRENT)
        assertTrue(IncomeSourceType.RECURRENT in vm.uiState.value.selectedTypes)
    }

    @Test
    fun `openSheet and closeSheet manage sheet visibility, closing also resets edit state`() = runTest {
        val vm = newVm()
        advanceUntilIdle()

        vm.openSheet()
        assertTrue(vm.uiState.value.isSheetOpen)

        vm.startEdit(existingSource)
        assertEquals(existingSource.id, vm.uiState.value.editingSourceId)
        assertEquals(existingSource.name, vm.uiState.value.nameInput)

        vm.closeSheet()
        val state = vm.uiState.value
        assertFalse(state.isSheetOpen)
        assertNull(state.editingSourceId)
        assertEquals("", state.nameInput)
        assertEquals(IncomeSourceType.values().toSet(), state.selectedTypes)
    }

    @Test
    fun `showDeleteConfirmation stores the candidate, dismissDeleteConfirmation clears it`() = runTest {
        val vm = newVm()
        advanceUntilIdle()

        vm.showDeleteConfirmation(existingSource)
        assertEquals(existingSource, vm.uiState.value.sourceToDelete)

        vm.dismissDeleteConfirmation()
        assertNull(vm.uiState.value.sourceToDelete)
    }

    @Test
    fun `saveSource refuses blank names`() = runTest {
        val vm = newVm()
        advanceUntilIdle()

        vm.onNameChange("   ")
        vm.saveSource()
        advanceUntilIdle()

        assertEquals("Enter a source name", vm.uiState.value.errorMessage)
        coVerify(exactly = 0) { incomeRepository.upsertIncomeSource(any()) }
    }

    @Test
    fun `saveSource refuses empty type selection`() = runTest {
        val vm = newVm()
        advanceUntilIdle()

        vm.onNameChange("Side gig")
        IncomeSourceType.values().forEach { vm.onTypeToggle(it) }
        vm.saveSource()
        advanceUntilIdle()

        assertEquals("Select at least one type", vm.uiState.value.errorMessage)
    }

    @Test
    fun `saveSource inserts a new source when editingSourceId is null`() = runTest {
        val captured = slot<IncomeSource>()
        coEvery { incomeRepository.upsertIncomeSource(capture(captured)) } returns
            Result.success(Unit)

        val vm = newVm()
        advanceUntilIdle()

        vm.onNameChange("  Tutoring  ")
        vm.onTypeToggle(IncomeSourceType.PENDING)
        vm.onTypeToggle(IncomeSourceType.RECURRENT)
        vm.saveSource()
        advanceUntilIdle()

        val source = captured.captured
        assertEquals(0L, source.id)
        assertEquals("Tutoring", source.name)
        assertEquals(setOf(IncomeSourceType.ONE_TIME), source.types)

        val state = vm.uiState.value
        assertEquals("Source added", state.successMessage)
        assertFalse(state.isSheetOpen)
        assertEquals("", state.nameInput)
        assertEquals(IncomeSourceType.values().toSet(), state.selectedTypes)
    }

    @Test
    fun `saveSource updates an existing source when editing`() = runTest {
        val captured = slot<IncomeSource>()
        coEvery { incomeRepository.upsertIncomeSource(capture(captured)) } returns
            Result.success(Unit)

        val vm = newVm()
        advanceUntilIdle()

        vm.startEdit(existingSource)
        vm.onNameChange("Freelance Updated")
        vm.saveSource()
        advanceUntilIdle()

        val source = captured.captured
        assertEquals(existingSource.id, source.id)
        assertEquals("Freelance Updated", source.name)
        assertEquals(existingSource.types, source.types)
        assertEquals("Source updated", vm.uiState.value.successMessage)
    }

    @Test
    fun `saveSource surfaces repository error message`() = runTest {
        coEvery { incomeRepository.upsertIncomeSource(any()) } returns
            Result.failure(IllegalStateException("nope"))

        val vm = newVm()
        advanceUntilIdle()

        vm.onNameChange("Anything")
        vm.saveSource()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals("nope", state.errorMessage)
        assertFalse(state.isSaving)
    }

    @Test
    fun `deleteSource clears candidate on success`() = runTest {
        coEvery { incomeRepository.deleteIncomeSource(existingSource.id) } returns
            Result.success(Unit)

        val vm = newVm()
        advanceUntilIdle()
        vm.showDeleteConfirmation(existingSource)

        vm.deleteSource(existingSource)
        advanceUntilIdle()

        assertNull(vm.uiState.value.sourceToDelete)
    }

    @Test
    fun `deleteSource surfaces an error and still clears the candidate on failure`() = runTest {
        coEvery { incomeRepository.deleteIncomeSource(any()) } returns
            Result.failure(RuntimeException("constraint violation"))

        val vm = newVm()
        advanceUntilIdle()
        vm.showDeleteConfirmation(existingSource)

        vm.deleteSource(existingSource)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals("constraint violation", state.errorMessage)
        assertNull(state.sourceToDelete)
    }

    @Test
    fun `mutating state via updateState clears prior messages`() = runTest {
        coEvery { incomeRepository.deleteIncomeSource(any()) } returns
            Result.failure(RuntimeException("boom"))

        val vm = newVm()
        advanceUntilIdle()

        vm.deleteSource(existingSource)
        advanceUntilIdle()
        assertNotNull(vm.uiState.value.errorMessage)

        vm.onNameChange("Anything")
        assertNull(vm.uiState.value.errorMessage)
    }
}
