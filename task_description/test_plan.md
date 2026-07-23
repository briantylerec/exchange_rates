# Test Plan

## Repository
- Verify that `RatesRepositoryImpl` requests rates using `USD` as the base currency.
- Verify that remote `RateDto` objects are mapped to domain `Rate` objects.
- Verify behavior when the remote service returns an empty list.
- Verify error propagation or fallback behavior if cache support is integrated later.

## ViewModel
- Verify that `getRates()` updates `rateFlow` with repository data.
- Verify that an initial state emits an empty list before data is loaded.
- Verify failure behavior when the repository throws.

## Activity / Lifecycle
- Verify that periodic refresh starts when the screen reaches `STARTED`.
- Verify that periodic refresh stops when the screen goes to background.
- Verify that the adapter receives new lists from `rateFlow`.

## UI
- Verify that currency codes are left aligned and values are right aligned.
- Verify that row dividers are drawn by `RatesDividerItemDecoration`.
- Verify that the last row does not draw an extra divider.
- Verify that frequent rate updates do not recreate the full list unnecessarily.

## Cache
- Verify concurrent `put` and `get` calls do not throw or corrupt state.
- Verify that cached lists are defensive copies and cannot be mutated externally.
