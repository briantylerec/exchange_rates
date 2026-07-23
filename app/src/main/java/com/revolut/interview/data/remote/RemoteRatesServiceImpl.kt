package com.revolut.interview.data.remote

import android.os.Looper
import android.os.NetworkOnMainThreadException
import android.util.Log
import com.revolut.interview.data.RateDto
import io.reactivex.Single
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class RemoteRatesServiceImpl : RatesService {

    private val baseResponse = listOf(
        RateDto("USD", 1.0),
        RateDto("GBP", 0.77279),
        RateDto("EUR", 0.86033),
        RateDto("AUD", 1.3906),
        RateDto("BGN", 1.6826),
        RateDto("BRL", 4.1225),
        RateDto("CAD", 1.3196),
        RateDto("CHF", 0.97003),
        RateDto("CNY", 6.8354),
        RateDto("CZK", 22.124),
        RateDto("DKK", 6.4152),
        RateDto("HKD", 7.8569),
        RateDto("HRK", 6.3958),
        RateDto("HUF", 280.89),
        RateDto("IDR", 14904.0),
        RateDto("ILS", 3.5881),
        RateDto("INR", 72.025),
        RateDto("ISK", 109.95),
        RateDto("JPY", 111.45),
        RateDto("KRW", 1122.5),
        RateDto("MXN", 19.242),
        RateDto("MYR", 4.1404),
        RateDto("NOK", 8.4107),
        RateDto("NZD", 1.5171),
        RateDto("PHP", 53.85),
        RateDto("PLN", 3.7152),
        RateDto("RON", 3.9906),
        RateDto("RUB", 68.461),
        RateDto("SEK", 9.1115),
        RateDto("SGD", 1.3765),
        RateDto("THB", 32.805),
        RateDto("TRY", 6.5628),
        RateDto("ZAR", 15.334)
    )

    private val random = Random(System.currentTimeMillis())

    override suspend fun getRates(baseCurrencyCode: String): List<RateDto> {
        simulateNetworkDelayCoroutines()
        return performNetworkCall()
    }

    override fun getRatesSingle(baseCurrencyCode: String): Single<List<RateDto>> =
        Single.fromCallable(::performNetworkCall)
            .simulateNetworkDelayRx()

    private fun performNetworkCall(): List<RateDto> {
        ensureNotMainThread()
        Log.d("RemoteRatesServiceImpl", "performNetworkCall")
        return baseResponse.mapIndexed { index, rates ->
            randomiseResponseItem(index, rates)
        }
    }

    private fun randomiseResponseItem(
        index: Int,
        rates: RateDto
    ) = if (index != 0) {
        val randomRatio = 1.0 + random.nextDouble() / 5.0 - 0.1
        RateDto(rates.currency, rates.value * randomRatio)
    } else rates

    private fun Single<List<RateDto>>.simulateNetworkDelayRx(): Single<List<RateDto>> =
        delay(random.nextInt(0, 1500).toLong(), TimeUnit.MILLISECONDS)

    private suspend fun simulateNetworkDelayCoroutines() {
        delay(random.nextInt(0, 1500).toLong())
    }

    private fun ensureNotMainThread() {
        if (Thread.currentThread() == Looper.getMainLooper().thread) {
            throw NetworkOnMainThreadException()
        }
    }
}