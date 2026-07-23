package com.revolut.interview.data.mapper

import com.revolut.interview.data.RateDto
import com.revolut.interview.domain.Rate

/**
 * Task 1 mapper.
 *
 * Converts remote DTOs into domain models so the UI does not depend on the remote
 * data representation returned by RatesService.
 */
object RateMapper {

    fun map (rateDto: RateDto) : Rate{
        return Rate(
            currency = rateDto.currency,
            value = rateDto.value
        )
    }
}
