package com.revolut.interview.data.remote

import com.revolut.interview.data.RateDto
import io.reactivex.Single

interface RatesService {
    suspend fun getRates(baseCurrencyCode: String): List<RateDto>
    fun getRatesSingle(baseCurrencyCode: String): Single<List<RateDto>>
}
