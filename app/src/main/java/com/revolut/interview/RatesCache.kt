package com.revolut.interview

import com.revolut.interview.domain.Rate
import java.util.concurrent.ConcurrentHashMap

interface RatesCache {
    fun put(baseCurrencyCode: String, rates: List<Rate>)
    fun get(baseCurrencyCode: String): List<Rate>?
}

/**
 * Task 2 implementation.
 *
 * The base project stored cache entries in a mutable list, which was not safe for
 * concurrent reads and writes. This implementation uses a ConcurrentHashMap and
 * defensive list copies so it can be reused safely by repositories or background
 * workers in a multithreaded app.
 */
class RatesCacheImpl : RatesCache {

    private val cache = ConcurrentHashMap<String, List<Rate>>()

    override fun put(baseCurrencyCode: String, rates: List<Rate>) {
        cache[baseCurrencyCode] = rates.toList()
    }

    override fun get(baseCurrencyCode: String): List<Rate>? {
        return cache[baseCurrencyCode]?.toList()
    }
}
