Create a database:
```shell
./gradlew postgresStart
```
---
#### Manually Test
Example request:
```shell
curl -X POST -H "Content-Type: application/json" \
    -d '{"inactiveAccountIds": [1, 2, 3, 4, 5]}' \
    localhost:9090/inactive-accounts/create-entries
```
---
#### Automation Test
Run `BatchProcessingApplicationTests` for a series of checks, like:
* Testing the http endpoint
* Testing the database integration
* Testing the batch job execution (happy path)
* Testing the batch job retry policy with a recover of failure
* Testing the batch job retry policy when retry limit is exhausted without recovery.

Uses Docker with Testcontainers for the database instance.
