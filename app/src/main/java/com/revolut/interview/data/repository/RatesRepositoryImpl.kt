package com.revolut.interview.data.repository

import com.revolut.interview.data.mapper.RateMapper
import com.revolut.interview.data.remote.RatesService
import com.revolut.interview.data.remote.RemoteRatesServiceImpl
import com.revolut.interview.domain.Rate
import com.revolut.interview.domain.repository.RatesRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Task 1 repository implementation.
 *
 * Fetches USD rates from the remote source off the main thread and maps the response
 * into domain models. The constructor accepts dependencies so Task 3 can unit test
 * this class with a fake service and a test dispatcher.
 */
class RatesRepositoryImpl(
    private val rateService: RatesService = RemoteRatesServiceImpl(),
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : RatesRepository {

     /*
      * If cache behavior is required later, this repository is the correct integration
      * point because it owns the data policy: read cached rates before the remote
      * request, update cache after success, or return cached values as fallback.
      */
     override suspend fun getRates() : List<Rate> {
        return withContext(dispatcher) {
            rateService.getRates("USD").map(RateMapper::map)
        }
    }
}
