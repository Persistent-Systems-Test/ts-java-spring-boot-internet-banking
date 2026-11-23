---
name: sdlc-pipeline
description: Deliver a requirement end to end in the internet-banking Spring Boot microservices - requirement validation, design and code generation, unit test generation, PR creation and review - with a human approval gate after each stage. Use when given a Jira ticket or requirement to implement in this repo.
---

# SDLC pipeline (human-in-the-loop) - internet-banking

Run the stages in order. Each stage ends at a **GATE**: post the stage output to the user, then
STOP and block on the user. Continue only on explicit approval ("approved", "go"). On feedback,
redo the stage and present the gate again. Never skip a gate. Never merge a PR.

## Input
- Requirement text + acceptance criteria (from the Jira ticket payload or the user's message).
- If acceptance criteria are missing, ask for them before Stage 1.

## Repo facts
- Package root `com.javatodev.finance`; Java 21, Spring Boot 3.2.4, Spring Cloud 2023.0.0, Lombok.
- Each service is its own Gradle project: `cd <module> && ./gradlew assemble` / `./gradlew test`.
- Modules:
  - `core-banking-service` - accounts, balances, transactions (ledger), Flyway + MySQL
  - `internet-banking-fund-transfer-service` - transfers (`/api/v1/transfer`) via `BankingCoreFeignClient`
  - `internet-banking-utility-payment-service` - bill payments via `BankingCoreRestClient`
  - `internet-banking-user-service` - registration, Keycloak
  - `internet-banking-api-gateway` - routing and security
  - `internet-banking-config-server`, `internet-banking-service-registry` - infrastructure
- `@SpringBootTest` / `*ApplicationTests.contextLoads` need config server, Eureka and DBs
  (`docker-compose/`); they fail standalone except in service-registry and utility-payment-service.

## Stage 1 - Requirement validation (read-only)
1. Decompose the requirement into testable rules R1, R2, ...: trigger, inputs, thresholds,
   exceptions, observable outcome (HTTP status, error code, payload, DB write).
2. Flag rules that are ambiguous, untestable, contradictory, or missing edge cases
   (null, zero, negative, boundaries, auth, concurrency).
3. Find current behavior with `rg -n`, using at least two search approaches (literal terms, then
   synonyms/field names/endpoints/class names). Read controller -> service -> repository ->
   Flyway migrations (`src/main/resources/db/migration/`) -> `exception/GlobalErrorCode.java` /
   `GlobalExceptionHandler`. Check cross-service callers (Feign/REST clients, gateway).
4. Classify each rule EXISTS / PARTIAL / NEW / CONFLICT, citing `module/path:line` of code you opened.

Output table: `| Rule | Expected behavior | Current code (file:line) | Status | Change needed |`,
then impacted modules, DB changes and open questions.

**GATE 1:** wait for approval of the rules and answers to open questions.

## Stage 2 - Design and code generation
1. Post a short design: files to add/change per module, API contract (method, path,
   request/response), DB migration, new error codes, cross-service contract changes.
2. **GATE 2a:** wait for design approval. Do not write code before it.
3. Create a branch `devin/$(date +%s)-<ticket-or-slug>` and implement only the approved design,
   matching surrounding code:
   - Controller in `controller/`: `@RestController`, `@RequestMapping("/api/v1/...")`,
     `@RequiredArgsConstructor`, `@Slf4j`, Swagger `@Tag`/`@Operation`; delegate to the service.
   - Business rules in `service/` (e.g. `TransactionService.validateBalance()`).
   - Entities `model/entity/`, DTOs `model/dto/` (`request/`, `response/`), mappers extend
     `model/mapper/BaseMapper`, Spring Data repositories in `repository/` (or `model/repository/`).
   - Errors: add a constant to `exception/GlobalErrorCode.java`, throw a
     `SimpleBankingGlobalException` subclass, map it in `GlobalExceptionHandler`.
   - DB: new Flyway file `V1.0.<yyyyMMddHHmmss>__<description>.sql`; never edit an existing one.
   - Cross-service calls: Feign clients in `service/rest/`; update both sides of a contract.
4. Run `./gradlew assemble` in every touched module.

**GATE 2b:** post files changed per module and the build result; wait for approval.

## Stage 3 - Unit test generation
1. Test every changed `@Service` class. File: `<module>/src/test/java/<same package>/<Class>Test.java`.
2. JUnit 5 + Mockito: `@ExtendWith(MockitoExtension.class)`, `@Mock` repositories/clients,
   `@InjectMocks` the class. No `@SpringBootTest`. AssertJ `assertThat`.
   Names `method_condition_expectedResult`.
3. Per rule: happy path, each exception branch, boundaries (equal / just above / just below),
   null/empty inputs, and what is passed to repositories/clients (e.g. nothing saved on rejection).
   If an entity is saved more than once, capture state with `thenAnswer`, not `ArgumentCaptor`.
4. Run `./gradlew test --tests '<fqcn>'`, then the module's full `./gradlew test`. If the code is
   wrong, report the bug; never weaken an assertion.
5. Known issues on `main`:
   - `core-banking-service` test compilation fails: `TransactionServiceTest.validateBalance_throwsException`
     uses a non-existent 3-arg `FundTransferRequest` constructor. If you touch this module, fix it
     (no-arg constructor + setters) in the same PR and say so.
   - After that, 3 existing tests still fail (`validateBalance_throwsException` does not stub
     `accountService.readBankAccount`; `UserServiceTest.readUser_found`/`readUsers_success` use a
     `UserEntity` with null accounts). Report them as pre-existing; fix only if approved at GATE 3.

**GATE 3:** post a `| Rule | Test method | Result |` table and the test command output; wait for approval.

## Stage 4 - PR creation
1. Commit as `<type>(<module>): <summary>` (`feat`, `fix`, `test`, `refactor`) and push.
2. Open a PR against `main`. Title `[<JIRA-KEY>] <summary>` (or a plain summary without a ticket). Body:
   - **Requirement**: ticket link and one-line summary
   - **Rule traceability**: rule ID -> code change (file) -> test method
   - **API / DB changes**: endpoints, Flyway migrations, error codes
   - **Testing**: commands and results; what was not tested and why
   - **Open questions** from Stage 1
3. Do not merge.

## Stage 5 - Review
- Devin Review reviews the PR when an automatic review rule is enabled for the repo; otherwise
  comment `/devin review` on the PR. Fix or reply to each finding with new commits.
- Address human review comments the same way.

**GATE 5:** tell the user the PR is ready for human review and merge; post the PR link
(and comment it on the Jira ticket when started from Jira).
