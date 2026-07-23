package com.revolut.interview.domain.repository

import com.revolut.interview.domain.Rate

/**
 * Task 1 domain contract.
 *
 * The base project had an empty repository placeholder. The app uses this contract
 * to keep presentation code independent of the remote data source implementation.
 */
interface RatesRepository {

    suspend fun getRates() : List<Rate>
}
