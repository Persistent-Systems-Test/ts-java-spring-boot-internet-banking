---
name: requirement-validation
description: Trace banking requirements through Spring Boot services, persistence, and API consumers when comparing migrated Java behavior with legacy SAS.
---

# Requirement validation — Spring Boot banking

## When to use this

Use this repo-specific companion to `.workshop/playbooks/requirement-validation.devin.md` when validating a business requirement against Java and legacy SAS. Start with `docs/LEGACY_TO_JAVA_MIGRATION_MAP.md` for leads, then verify each lead in code. Do not infer migration parity from the map.

## Layout

| Module | Where to look |
| --- | --- |
| `core-banking-service/` | `AccountService` and `TransactionService` for account lookup, transfer and utility-payment balance checks, ledger writes, and account/transaction persistence. |
| `internet-banking-fund-transfer-service/` | `FundTransferController` → `FundTransferService` → `BankingCoreFeignClient`; transfer status, reference and history are stored locally. |
| `internet-banking-user-service/` | `UserController`, `UserService`, and `KeycloakUserService` for registration, core-bank identity checks, approval and Keycloak integration. Distinguish this `UserService` from the core-banking class with the same name. |
| `internet-banking-utility-payment-service/` | `UtilityPaymentController` → `UtilityPaymentService` → `BankingCoreRestClient`; payment status and history are stored locally. |
| `internet-banking-api-gateway/` | API gateway: security and request filters in `configuration/`; check the gateway's `bootstrap*.yml` and config-server source if routes are external. Do not infer deployed routing from a local controller alone. |

## Trace a rule

1. Start in `<module>/src/main/java/com/javatodev/finance/`. Search `*Service.java` for private `validate*()` helpers and their callers, e.g. `TransactionService.validateBalance()`. Also read inline checks in public methods (e.g. user registration), from the controller/DTO to the write or response.
2. Follow exceptions to `exception/GlobalErrorCode.java` where present (core and user services) and to the relevant `GlobalExceptionHandler` or Feign error path. Verify the actual HTTP status, error payload and any caller-side status already written; a code constant alone is not behavior.
3. Follow `*Repository.java` Spring Data interfaces under `repository/` or `model/repository/` to derived queries, entity mappings and schema constraints. Read transaction boundaries, callers and tests in `src/test/` where relevant. Check downstream API and DB consumers before deciding what would change.
4. Always cite the module-qualified, repository-relative `file:line` or `file:start-end` for every rule, material difference and proposed change point. Open the implementation and relevant call sites before citing; use `rg -n` to locate them and `nl -ba` to verify line numbers.
