package com.revolut.interview.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.revolut.interview.data.repository.RatesRepositoryImpl
import com.revolut.interview.domain.Rate
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Task 1 presentation state holder.
 *
 * Exposes the latest list of rates as a StateFlow. RatesActivity collects this flow
 * and submits new lists to the adapter whenever polling returns fresh data.
 */
class RatesViewModel(val ratesRepository: RatesRepositoryImpl) : ViewModel() {
    val rateFlow = MutableStateFlow<List<Rate>>(emptyList())

    suspend fun getRates() {
        viewModelScope.runCatching {
            val response = ratesRepository.getRates()
            rateFlow.value = response
        }
    }
}

/**
 * Manual dependency wiring for the assignment.
 *
 * No DI framework is required by the README, so the factory builds the repository
 * and passes it into RatesViewModel.
 */
object ViewModelFactory : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return RatesViewModel(ratesRepository = RatesRepositoryImpl()) as T
    }
}
