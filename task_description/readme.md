### General notes
The overall focus points are: architecture, quality and code clarity,<br />
It should be possible to launch the app and verify the result,<br />
You can use UI pattern of your choice: MVVM, MVP, MVI,<br />
Coroutines / RxJava usage is up to you, code template supports both approaches,<br />
There aren't any DI libraries added to the project, manual DI is sufficient for this assignment,<br />
Project already contains some code to build on top of it / change if needed, just please keep RatesService and its implementation intact as it mimics fetching data from network and
should be treated as a remote data source.

# Task 1
### Rates app
- Provide basic implementation for rates exchange single screen app
- App should display a list of exchange rates for USD currency
    - Final design for the screen in the same folder: [rates.png](rates.png)
- Update should happen every 1 second in a reactive manner
- Periodic requests shouldn’t happen when app goes to background

# Task 2
### Cache
- Review RatesCacheImpl
- Make sure it could be used in multithreading application

# Task 3
### Testing
- Implement one unit test for RatesRepository
- Provide a test plan for other cases

# Task 4
### UI
- Align UI to one presented on the design with the most optimal updating mechanism