---
name: test-automation
description: Write and run JUnit 5 + Mockito unit tests for the internet-banking Spring Boot microservices. Use when asked to add, generate, fix, or run tests for any service in this repo.
---

# Test automation for internet-banking microservices

Follow these steps when asked to write tests for a class or service.

## 1. Pick the target
- Services: `core-banking-service`, `internet-banking-{api-gateway,config-server,service-registry,user-service,fund-transfer-service,utility-payment-service}`. Each is a separate Gradle project with its own `./gradlew`.
- Prefer `@Service` classes under `src/main/java/com/javatodev/finance/service/`; they hold the business logic.
- Read the class, its request/response models, entities, repositories, and Feign clients (`service/rest/`) before writing anything.

## 2. Write unit tests, not context tests
- Location: `<service>/src/test/java/<same package as class>/<ClassName>Test.java`.
- Use `@ExtendWith(MockitoExtension.class)`, `@Mock` for repositories and Feign clients, and `@InjectMocks` for the class under test.
- Do NOT use `@SpringBootTest`: it needs the config server, Eureka, Keycloak, and MySQL running, so it fails in isolation.
- Use AssertJ (`assertThat`), available through `spring-boot-starter-test`.
- Cover at least: the happy path, every exception branch, and the values passed to repositories or clients.
- When a method saves the same entity more than once, record its state in a `thenAnswer` stub. An `ArgumentCaptor` only holds a reference to the final state.
- Name tests `method_condition_expectedResult`.

## 3. Run and iterate
```shell
cd <service> && ./gradlew test --tests '<fully.qualified.ClassNameTest>'
```
- Fix the test until it passes. If the code under test is wrong, report the bug and don't weaken the assertion.
- Reports: `<service>/build/reports/tests/test/index.html`.

## 4. Known issues
- `core-banking-service` test compilation fails on main: `TransactionServiceTest` calls a 3-arg `FundTransferRequest` constructor that doesn't exist. Fix or exclude it before running tests in that service.
- Existing `*ApplicationTests.contextLoads` tests (except service-registry and utility-payment-service) need the full stack (`docker-compose/`) and fail standalone.

## Reference example
`internet-banking-utility-payment-service/src/test/java/com/javatodev/finance/service/UtilityPaymentServiceTest.java`
