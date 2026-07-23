# Implementation Guide

This document explains the changes required to implement the assignment starting from the base project. The base project used for comparison was `/Users/brianmoraa/Downloads/exchange_rates 2`.

## Task 1 - Rates App

### Files to add or update
- `app/src/main/java/com/revolut/interview/domain/repository/RatesRepository.kt`
- `app/src/main/java/com/revolut/interview/data/repository/RatesRepositoryImpl.kt`
- `app/src/main/java/com/revolut/interview/data/mapper/RateMapper.kt`
- `app/src/main/java/com/revolut/interview/presentation/RatesViewModel.kt`
- `app/src/main/java/com/revolut/interview/presentation/RatesActivity.kt`

### Repository contract

Replace the empty root-level repository contract from the base project with a real domain contract:

```kotlin
package com.revolut.interview.domain.repository

import com.revolut.interview.domain.Rate

interface RatesRepository {
    suspend fun getRates(): List<Rate>
}
```

### DTO to domain mapper

Create a mapper so the remote layer can keep returning `RateDto` while the app UI works with domain `Rate` objects:

```kotlin
object RateMapper {
    fun map(rateDto: RateDto): Rate {
        return Rate(
            currency = rateDto.currency,
            value = rateDto.value
        )
    }
}
```

### Repository implementation

Create `RatesRepositoryImpl` in the data layer. It should depend on `RatesService`, request USD rates, run the remote call on a dispatcher, and map DTOs to domain models.

```kotlin
class RatesRepositoryImpl(
    private val rateService: RatesService = RemoteRatesServiceImpl(),
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : RatesRepository {

    override suspend fun getRates(): List<Rate> {
        return withContext(dispatcher) {
            rateService.getRates("USD").map(RateMapper::map)
        }
    }
}
```

The constructor defaults keep manual dependency injection simple for the app while still allowing unit tests to pass a fake service and test dispatcher.

### ViewModel

The ViewModel owns a `MutableStateFlow<List<Rate>>` and exposes a `getRates()` function that updates that flow:

```kotlin
class RatesViewModel(
    val ratesRepository: RatesRepositoryImpl
) : ViewModel() {

    val rateFlow = MutableStateFlow<List<Rate>>(emptyList())

    suspend fun getRates() {
        viewModelScope.runCatching {
            rateFlow.value = ratesRepository.getRates()
        }
    }
}
```

The factory must pass the repository into the ViewModel:

```kotlin
object ViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return RatesViewModel(ratesRepository = RatesRepositoryImpl()) as T
    }
}
```

### Activity lifecycle and polling

In `RatesActivity`, configure the `RecyclerView`, collect `rateFlow`, and request new rates every second only while the Activity is at least `STARTED`:

```kotlin
private fun observeRates() {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            launch {
                viewModel.rateFlow.collect { rates ->
                    adapter.submitList(rates)
                }
            }

            launch {
                while (true) {
                    viewModel.getRates()
                    delay(1_000.milliseconds)
                }
            }
        }
    }
}
```

`repeatOnLifecycle(Lifecycle.State.STARTED)` is important because it automatically cancels the polling block when the app goes to background.

## Task 2 - Cache

### File to update
- `app/src/main/java/com/revolut/interview/RatesCache.kt`

The base implementation used a mutable list and a `removeAll` plus `add` sequence. That is not safe for concurrent access because another thread can read or write the list between those two operations.

Use `ConcurrentHashMap` and defensive copies:

```kotlin
class RatesCacheImpl : RatesCache {

    private val cache = ConcurrentHashMap<String, List<Rate>>()

    override fun put(baseCurrencyCode: String, rates: List<Rate>) {
        cache[baseCurrencyCode] = rates.toList()
    }

    override fun get(baseCurrencyCode: String): List<Rate>? {
        return cache[baseCurrencyCode]?.toList()
    }
}
```

This makes `put` and `get` safe to call from multiple threads and avoids exposing the stored list instance to external mutation.

The cache is not currently wired into the app flow. If product behavior later requires cached data, `RatesRepositoryImpl` is the correct integration point because it owns the data policy. It can read cache before remote calls, update cache after successful responses, or return cached values as fallback after remote failures.

## Task 3 - Testing

### Files to add or update
- `app/src/test/java/com/revolut/interview/data/repository/RatesRepositoryImplTest.kt`
- `task_description/test_plan.md`

Remove the empty `ExampleUnitTest` and add a repository unit test with a fake `RatesService`.

The test should verify that:
- The repository requests rates with `"USD"` as the base currency.
- The repository maps remote `RateDto` values into domain `Rate` values.

Core test shape:

```kotlin
@Test
fun getRates_fetchesUsdRatesAndMapsDtosToDomainRates() = runTest {
    val service = FakeRatesService(
        response = listOf(
            RateDto(currency = "USD", value = 1.0),
            RateDto(currency = "EUR", value = 0.86)
        )
    )
    val repository = RatesRepositoryImpl(
        rateService = service,
        dispatcher = StandardTestDispatcher(testScheduler)
    )

    val result = repository.getRates()

    assertEquals("USD", service.requestedBaseCurrencyCode)
    assertEquals(2, result.size)
    assertEquals("USD", result[0].currency)
    assertEquals(1.0, result[0].value, 0.0)
    assertEquals("EUR", result[1].currency)
    assertEquals(0.86, result[1].value, 0.0)
}
```

Add `task_description/test_plan.md` with planned coverage for repository, ViewModel, Activity lifecycle, UI, and cache concurrency cases.

## Task 4 - UI And Efficient Updates

### Files to add or update
- `app/src/main/java/com/revolut/interview/presentation/RatesAdapter.kt`
- `app/src/main/java/com/revolut/interview/presentation/RatesDividerItemDecoration.kt`
- `app/src/main/java/com/revolut/interview/presentation/RatesActivity.kt`
- `app/src/main/res/layout/item_rates.xml`
- `app/src/main/res/values/colors.xml`
- `app/src/main/res/values/dimens.xml`

### Adapter

Replace the manual `RecyclerView.Adapter` implementation and `notifyDataSetChanged()` with `ListAdapter` and `DiffUtil`.

```kotlin
class RatesAdapter : ListAdapter<Rate, RatesAdapter.ViewHolder>(RateDiffCallback) {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val currency: TextView = itemView.findViewById(R.id.currency)
        val value: TextView = itemView.findViewById(R.id.value)

        fun bind(rate: Rate) {
            currency.text = rate.currency
            bindValue(rate)
        }

        fun bindValue(rate: Rate) {
            value.text = rate.value.toString()
        }
    }
}
```

Use currency as the stable item identity:

```kotlin
override fun areItemsTheSame(oldItem: Rate, newItem: Rate): Boolean {
    return oldItem.currency == newItem.currency
}
```

Use a payload so repeated rate updates can refresh only the value text when the currency row is the same:

```kotlin
override fun getChangePayload(oldItem: Rate, newItem: Rate): Any? {
    return if (oldItem.value != newItem.value) {
        RatePayload.ValueChanged
    } else {
        null
    }
}
```

### Item layout

Make `item_rates.xml` a single horizontal row. The currency stays left aligned, and the value column uses width `0dp`, `layout_weight="1"`, and end alignment:

```xml
<TextView
    android:id="@+id/value"
    android:layout_width="0dp"
    android:layout_height="wrap_content"
    android:layout_marginStart="@dimen/rate_column_spacing"
    android:layout_weight="1"
    android:gravity="end"
    android:singleLine="true"
    android:textAlignment="viewEnd" />
```

### Dividers

Do not place a divider `View` inside each item. Add a `RecyclerView.ItemDecoration` instead:

```kotlin
class RatesDividerItemDecoration(
    dividerColor: Int,
    private val dividerHeight: Int
) : RecyclerView.ItemDecoration()
```

Attach it in `RatesActivity`:

```kotlin
recyclerView.addItemDecoration(
    RatesDividerItemDecoration(
        dividerColor = ContextCompat.getColor(this, R.color.grey),
        dividerHeight = resources.getDimensionPixelSize(R.dimen.rate_divider_height)
    )
)
```

This keeps item layouts focused on content and lets the `RecyclerView` draw separators efficiently. The implementation should skip the last item so there is no trailing divider.

## Validation

Run the following commands after implementation:

```bash
sh gradlew :app:compileDebugKotlin
sh gradlew :app:testDebugUnitTest
```

Both commands should finish with `BUILD SUCCESSFUL`.
