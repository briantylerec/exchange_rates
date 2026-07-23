package com.revolut.interview.data.repository

import com.revolut.interview.data.RateDto
import com.revolut.interview.data.remote.RatesService
import io.reactivex.Single
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Task 3 repository unit test.
 *
 * Uses a fake RatesService so the test verifies repository behavior without relying
 * on the random remote implementation.
 */
class RatesRepositoryImplTest {

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
}

private class FakeRatesService(
    private val response: List<RateDto>
) : RatesService {

    var requestedBaseCurrencyCode: String? = null
        private set

    override suspend fun getRates(baseCurrencyCode: String): List<RateDto> {
        requestedBaseCurrencyCode = baseCurrencyCode
        return response
    }

    override fun getRatesSingle(baseCurrencyCode: String): Single<List<RateDto>> {
        requestedBaseCurrencyCode = baseCurrencyCode
        return Single.just(response)
    }
}
