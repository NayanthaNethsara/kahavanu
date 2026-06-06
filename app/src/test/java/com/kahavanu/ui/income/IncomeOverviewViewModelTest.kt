package com.kahavanu.ui.income

import app.cash.turbine.test
import com.kahavanu.domain.model.CurrencyOption
import com.kahavanu.domain.model.IncomeLogEntry
import com.kahavanu.domain.model.IncomeLogResult
import com.kahavanu.domain.model.ScheduledIncome
import com.kahavanu.domain.model.SmsSuggestion
import com.kahavanu.domain.model.SuggestionKind
import com.kahavanu.domain.model.SuggestionStatus
import com.kahavanu.domain.model.ExpenseLogEntry
import com.kahavanu.domain.repository.ExpensesRepository
import com.kahavanu.domain.repository.IncomeRepository
import com.kahavanu.domain.repository.SettingsRepository
import com.kahavanu.domain.repository.SmsSuggestionRepository
import com.kahavanu.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.Rule
import java.time.YearMonth
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class IncomeOverviewViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var incomeRepository: IncomeRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var smsSuggestionRepository: SmsSuggestionRepository
    private lateinit var expensesRepository: ExpensesRepository

    private lateinit var incomeLogsFlow: MutableStateFlow<List<IncomeLogEntry>>
    private lateinit var scheduledFlow: MutableStateFlow<List<ScheduledIncome>>
    private lateinit var currencyFlow: MutableStateFlow<Pair<CurrencyOption, CurrencyOption>>
    private lateinit var expenseLogsFlow: MutableStateFlow<List<ExpenseLogEntry>>

    @Before
    fun setUp() {
        incomeRepository = mockk(relaxed = true)
        settingsRepository = mockk(relaxed = true)
        smsSuggestionRepository = mockk(relaxed = true)
        expensesRepository = mockk(relaxed = true)

        incomeLogsFlow = MutableStateFlow(emptyList())
        scheduledFlow = MutableStateFlow(emptyList())
        currencyFlow = MutableStateFlow(CurrencyOption.LKR to CurrencyOption.USD)
        expenseLogsFlow = MutableStateFlow(emptyList())

        coEvery { incomeRepository.observeIncomeLogs() } returns incomeLogsFlow
        coEvery { incomeRepository.observeScheduledIncomes() } returns scheduledFlow
        coEvery { settingsRepository.observeCurrencySettings() } returns currencyFlow
        coEvery { smsSuggestionRepository.observePendingByKinds(any()) } returns emptyFlow()
        coEvery { expensesRepository.observeExpenseLogs() } returns expenseLogsFlow
    }

    private fun newVm() =
        IncomeOverviewViewModel(
            incomeRepository,
            settingsRepository,
            smsSuggestionRepository,
            expensesRepository,
        )

    private fun startOfThisMonthEpoch(): Long =
        YearMonth.now().atDay(1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

    private fun lastMonthEpoch(): Long =
        YearMonth.now().minusMonths(1).atDay(15)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

    @Test
    fun `currencySettings exposes primary and secondary as codes`() = runTest {
        val vm = newVm()

        vm.currencySettings.test {
            assertEquals("LKR" to "USD", awaitItem())

            currencyFlow.value = CurrencyOption.EUR to CurrencyOption.GBP
            assertEquals("EUR" to "GBP", awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `primaryCurrency tracks the primary currency code`() = runTest {
        val vm = newVm()

        vm.primaryCurrency.test {
            assertEquals("LKR", awaitItem())
            currencyFlow.value = CurrencyOption.EUR to CurrencyOption.USD
            assertEquals("EUR", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `totalIncomeByCurrency aggregates current-month logs per currency`() = runTest {
        val now = startOfThisMonthEpoch()
        incomeLogsFlow.value = listOf(
            IncomeLogEntry(title = "a", amount = 100.0, currency = "LKR", receivedAtEpochMillis = now),
            IncomeLogEntry(title = "b", amount = 50.0, currency = "LKR", receivedAtEpochMillis = now),
            IncomeLogEntry(title = "c", amount = 25.0, currency = "USD", receivedAtEpochMillis = now),
            // last month should be filtered out
            IncomeLogEntry(title = "d", amount = 999.0, currency = "LKR", receivedAtEpochMillis = lastMonthEpoch()),
            // blank currency should be filtered out
            IncomeLogEntry(title = "e", amount = 10.0, currency = "", receivedAtEpochMillis = now),
        )

        val vm = newVm()
        val totals = vm.totalIncomeByCurrency.first { (it["LKR"] ?: 0.0) > 0.0 }
        assertEquals(150.0, totals["LKR"]!!, 0.0)
        assertEquals(25.0, totals["USD"]!!, 0.0)
    }

    @Test
    fun `totalIncomeByCurrency seeds primary and secondary even when there are no logs`() = runTest {
        val vm = newVm()
        val seeded = vm.totalIncomeByCurrency.firstNonEmpty()
        assertTrue(seeded.containsKey("LKR"))
        assertTrue(seeded.containsKey("USD"))
        assertEquals(0.0, seeded["LKR"]!!, 0.0)
        assertEquals(0.0, seeded["USD"]!!, 0.0)
    }

    @Test
    fun `totalReceivedByCurrency excludes pending entries`() = runTest {
        val now = startOfThisMonthEpoch()
        incomeLogsFlow.value = listOf(
            IncomeLogEntry(title = "received", amount = 200.0, currency = "LKR",
                receivedAtEpochMillis = now, sourceType = "one_time"),
            IncomeLogEntry(title = "pending", amount = 500.0, currency = "LKR",
                receivedAtEpochMillis = now, sourceType = "pending"),
            IncomeLogEntry(title = "recurrent received", amount = 100.0, currency = "USD",
                receivedAtEpochMillis = now, sourceType = "recurrent"),
        )

        val vm = newVm()
        val totals = vm.totalReceivedByCurrency.first { (it["LKR"] ?: 0.0) > 0.0 }
        assertEquals(200.0, totals["LKR"]!!, 0.0)
        assertEquals(100.0, totals["USD"]!!, 0.0)
    }

    @Test
    fun `topIncomeSource picks the highest-earning source and excludes pending`() = runTest {
        incomeLogsFlow.value = listOf(
            IncomeLogEntry(title = "Gig", amount = 100.0, currency = "LKR",
                receivedAtEpochMillis = 1L, sourceName = "Freelance"),
            IncomeLogEntry(title = "Pay", amount = 300.0, currency = "LKR",
                receivedAtEpochMillis = 2L, sourceName = "Salary"),
            IncomeLogEntry(title = "Pay", amount = 100.0, currency = "LKR",
                receivedAtEpochMillis = 3L, sourceName = "Salary"),
            // pending should not count towards the top source
            IncomeLogEntry(title = "Bonus", amount = 999.0, currency = "LKR",
                receivedAtEpochMillis = 4L, sourceType = "pending", sourceName = "Bonus"),
        )

        val vm = newVm()
        val top = vm.topIncomeSource.first { it != null }!!
        assertEquals("Salary", top.name)
        assertEquals(400.0, top.amount, 0.0)
        // 400 of 500 received = 80%
        assertEquals(80, top.percent)
    }

    @Test
    fun `currentSavings is all-time received income minus logged expenses`() = runTest {
        incomeLogsFlow.value = listOf(
            IncomeLogEntry(title = "Pay", amount = 1000.0, currency = "LKR", receivedAtEpochMillis = 1L),
            // pending income is excluded
            IncomeLogEntry(title = "Pending", amount = 500.0, currency = "LKR",
                receivedAtEpochMillis = 2L, sourceType = "pending"),
        )
        expenseLogsFlow.value = listOf(
            ExpenseLogEntry(title = "Food", amount = 300.0, currency = "LKR",
                spentAtEpochMillis = 1L, category = "Food"),
            ExpenseLogEntry(title = "Rent", amount = 200.0, currency = "LKR",
                spentAtEpochMillis = 2L, category = "Housing"),
        )

        val vm = newVm()
        // 1000 received - 500 expenses = 500
        val savings = vm.currentSavings.first { it != 0.0 }
        assertEquals(500.0, savings, 0.0)
    }

    @Test
    fun `currentSavings folds secondary-currency income into the primary currency`() = runTest {
        // primary is LKR (see currencyFlow); USD converts at the static 300 rate.
        incomeLogsFlow.value = listOf(
            IncomeLogEntry(title = "Local", amount = 1000.0, currency = "LKR", receivedAtEpochMillis = 1L),
            IncomeLogEntry(title = "Foreign", amount = 10.0, currency = "USD", receivedAtEpochMillis = 2L),
        )

        val vm = newVm()
        // 1000 LKR + (10 USD × 300) = 4000 LKR, no expenses
        val savings = vm.currentSavings.first { it != 0.0 }
        assertEquals(4000.0, savings, 0.0)
    }

    @Test
    fun `topIncomeSource ranks sources after converting to the primary currency`() = runTest {
        incomeLogsFlow.value = listOf(
            IncomeLogEntry(title = "Local", amount = 5000.0, currency = "LKR",
                receivedAtEpochMillis = 1L, sourceName = "Salary"),
            IncomeLogEntry(title = "Foreign", amount = 50.0, currency = "USD",
                receivedAtEpochMillis = 2L, sourceName = "Freelance"),
        )

        val vm = newVm()
        // Freelance 50 USD × 300 = 15,000 LKR outranks Salary's 5,000 LKR
        val top = vm.topIncomeSource.first { it != null }!!
        assertEquals("Freelance", top.name)
        assertEquals(15000.0, top.amount, 0.0)
    }

    @Test
    fun `pendingLogs only includes persistent entries`() = runTest {
        val now = startOfThisMonthEpoch()
        val pending = IncomeLogEntry(title = "p", amount = 1.0, currency = "LKR",
            receivedAtEpochMillis = now, sourceType = "pending")
        val recurrent = IncomeLogEntry(title = "r", amount = 2.0, currency = "LKR",
            receivedAtEpochMillis = now, sourceType = "recurrent")
        val oneTime = IncomeLogEntry(title = "o", amount = 3.0, currency = "LKR",
            receivedAtEpochMillis = now, sourceType = "one_time")
        incomeLogsFlow.value = listOf(pending, recurrent, oneTime)

        val vm = newVm()
        val filtered = vm.pendingLogs.first { it.isNotEmpty() }
        assertEquals(listOf(pending, recurrent), filtered)
    }

    private suspend fun Flow<Map<String, Double>>.firstNonEmpty(): Map<String, Double> =
        first { it.isNotEmpty() }

    @Test
    fun `markAsReceived delegates to repository`() = runTest {
        coEvery { incomeRepository.markScheduledAsReceived(any()) } returns Result.success(Unit)

        val vm = newVm()
        vm.markAsReceived(42L)
        advanceUntilIdle()

        coVerify { incomeRepository.markScheduledAsReceived(42L) }
    }

    @Test
    fun `disableScheduled delegates to repository`() = runTest {
        coEvery { incomeRepository.deleteScheduledIncome(any()) } returns Result.success(Unit)

        val vm = newVm()
        vm.disableScheduled(7L)
        advanceUntilIdle()

        coVerify { incomeRepository.deleteScheduledIncome(7L) }
    }

    @Test
    fun `dismissIncomeSuggestion converts the id and forwards it`() = runTest {
        val vm = newVm()

        vm.dismissIncomeSuggestion("99")
        advanceUntilIdle()
        coVerify { smsSuggestionRepository.dismiss(99L) }

        vm.dismissIncomeSuggestion("not-a-number")
        advanceUntilIdle()
        coVerify(exactly = 0) { smsSuggestionRepository.dismiss(match { it != 99L }) }
    }

    @Test
    fun `confirmIncomeSuggestion logs as new income for INCOME kind`() = runTest {
        val suggestion = sampleSuggestion(kind = SuggestionKind.INCOME, matched = null)
        coEvery { smsSuggestionRepository.getById(11L) } returns suggestion
        coEvery { incomeRepository.logIncome(any()) } returns
            Result.success(IncomeLogResult.SYNCED)

        val vm = newVm()
        vm.confirmIncomeSuggestion("11")
        advanceUntilIdle()

        coVerify {
            incomeRepository.logIncome(match {
                it.title == suggestion.title &&
                    it.amount == suggestion.amount &&
                    it.currency == suggestion.currency &&
                    it.sourceType == "sms"
            })
        }
        coVerify { smsSuggestionRepository.confirm(11L) }
        coVerify(exactly = 0) { incomeRepository.markScheduledAsReceived(any()) }
    }

    @Test
    fun `confirmIncomeSuggestion settles matched scheduled income for SETTLE_PENDING with a match`() = runTest {
        val suggestion = sampleSuggestion(
            kind = SuggestionKind.SETTLE_PENDING,
            matched = 555L,
        )
        coEvery { smsSuggestionRepository.getById(11L) } returns suggestion
        coEvery { incomeRepository.markScheduledAsReceived(555L) } returns Result.success(Unit)

        val vm = newVm()
        vm.confirmIncomeSuggestion("11")
        advanceUntilIdle()

        coVerify { incomeRepository.markScheduledAsReceived(555L) }
        coVerify(exactly = 0) { incomeRepository.logIncome(any()) }
        coVerify { smsSuggestionRepository.confirm(11L) }
    }

    @Test
    fun `confirmIncomeSuggestion falls back to logIncome when SETTLE_PENDING has no match`() = runTest {
        val suggestion = sampleSuggestion(
            kind = SuggestionKind.SETTLE_PENDING,
            matched = null,
        )
        coEvery { smsSuggestionRepository.getById(11L) } returns suggestion
        coEvery { incomeRepository.logIncome(any()) } returns
            Result.success(IncomeLogResult.LOCAL_ONLY)

        val vm = newVm()
        vm.confirmIncomeSuggestion("11")
        advanceUntilIdle()

        coVerify { incomeRepository.logIncome(any()) }
        coVerify(exactly = 0) { incomeRepository.markScheduledAsReceived(any()) }
        coVerify { smsSuggestionRepository.confirm(11L) }
    }

    @Test
    fun `confirmIncomeSuggestion is a no-op for invalid id`() = runTest {
        val vm = newVm()
        vm.confirmIncomeSuggestion("oops")
        advanceUntilIdle()

        coVerify(exactly = 0) { smsSuggestionRepository.getById(any()) }
        coVerify(exactly = 0) { smsSuggestionRepository.confirm(any()) }
    }

    @Test
    fun `confirmIncomeSuggestion is a no-op when suggestion not found`() = runTest {
        coEvery { smsSuggestionRepository.getById(42L) } returns null

        val vm = newVm()
        vm.confirmIncomeSuggestion("42")
        advanceUntilIdle()

        coVerify(exactly = 0) { incomeRepository.logIncome(any()) }
        coVerify(exactly = 0) { incomeRepository.markScheduledAsReceived(any()) }
        coVerify(exactly = 0) { smsSuggestionRepository.confirm(any()) }
    }

    private fun sampleSuggestion(
        id: Long = 11L,
        kind: SuggestionKind,
        matched: Long?,
    ) = SmsSuggestion(
        localId = id,
        userId = "user",
        smsSenderName = "BOC",
        smsBodyHash = "hash",
        smsReceivedAtEpochMillis = 1_000L,
        kind = kind,
        amount = 1234.0,
        currency = "LKR",
        title = "Salary payment",
        merchant = "Employer",
        txnAtEpochMillis = 2_000L,
        matchedScheduledIncomeId = matched,
        status = SuggestionStatus.PENDING,
        confidence = 0.92f,
        createdAtEpochMillis = 0L,
        updatedAtEpochMillis = 0L,
    )
}
