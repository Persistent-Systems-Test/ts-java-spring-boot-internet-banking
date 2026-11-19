# Playbook: Validate a requirement across SAS and Spring Boot banking

> **Facilitator / presenter:** This file is the portable source for a Devin Playbook.
> In an organization that supports Playbooks, copy its contents into
> **Settings → Playbooks → Create a new Playbook** and configure the invocation
> as `!requirement-validation`. Committing this file does not register it.
> In a Skills-only organization, use the shared Skill `@skills:cross-repo-requirement-validation`
> instead, once it is installed and approved.

## Overview

Validate a proposed requirement against legacy batch behavior and migrated online
behavior. Produce an evidence-backed report, not code changes. Treat the
requirement as the expected behavior, SAS as evidence of legacy behavior, and
Java as evidence of migrated behavior; do not treat them as interchangeable.

## Required from user

- **Requirement text** — paste the requirement and any acceptance criteria; ask
  for the text if it is missing. Ask about ambiguities only when they affect a verdict.
- The session needs read access to `Persistent-Systems-Test/ts-sas-legacy-analytics`
  (legacy) and `Persistent-Systems-Test/ts-java-spring-boot-internet-banking`
  (migrated). Use their checked-out revisions unless the user provides refs.

## Procedure

1. **Decompose the requirement.** Give each testable rule an ID. Identify trigger,
   scope, inputs, thresholds, exceptions and observable outcome. Keep unknowns
   separate from stated acceptance criteria.
2. **Consult the migration map.** Read `docs/LEGACY_TO_JAVA_MIGRATION_MAP.md`
   in the Java repo and explicitly open `.agents/skills/requirement-validation/SKILL.md`
   in each repo so you use both sets of local mechanics. Treat map rows as leads,
   including rows marked `No counterpart found`. If a guide is absent at a
   selected ref, say so and continue from source.
3. **Grep both repos.** Search with `rg -in` or `git grep -in`. Use at least
   two distinct search approaches before declaring a gap: search literal terms,
   codes and thresholds, then search synonyms, domain fields, datasets, endpoints,
   class names or callers (e.g. `NEG_BAL`, negative balance, overdraft,
   `CURRENT_BALANCE`, `actualBalance`, insufficient funds).
4. **Read candidate code.** Use numbered views such as `nl -ba` to inspect the
   whole rule and its call sites. Follow SAS jobs, DATA/PROC steps, macros and
   formats; follow Java controllers, DTOs, services, exceptions, repositories,
   schemas and relevant tests. Separate a declaration from an executed check.
5. **Compare batch vs. real-time semantics.** For the same boundary inputs,
   compare population, trigger, timing, amount sign/units, inclusive limits,
   null/missing behavior, status and side effects. Distinguish a batch flag,
   retained or rejected row, and later notification from an API rejection or
   committed write. Check each side against the requirement, not just each other.
6. **Check downstream consumers.** Trace SAS outputs to persistent tables,
   later jobs, reports and alerts. Trace Java outcomes through exception handlers,
   Feign clients, gateway, persisted statuses and other consumers as applicable.
   Record the files inspected and search terms for any negative finding.
7. **Verify and deliver.** Check every citation against the opened source and
   every verdict against the acceptance criteria. State the two reviewed refs.
   If access or evidence is insufficient, mark the affected checks unassessed
   and explain why instead of declaring a gap.

## Output format

| Req | Legacy ref file:line | Migrated ref file:line | Verdict |
| --- | --- | --- | --- |
| Rule ID, expected behavior and material observed difference (if any) | SAS repo-relative `path:start-end` | Java module-qualified `path:start-end` | One verdict below |

Use `— (not found in reviewed scope)` for an absent reference and record the
searched terms, paths and nearest candidates below the table. Use
`— (not assessed)` in lieu of a verdict only when you cannot complete the check.

- **COVERED** — Java satisfies the rule with no material difference from the applicable legacy behavior. A genuinely new rule can be covered without SAS evidence; say that no legacy baseline applies.
- **COVERED-WITH-DIFF** — Java meets the stated requirement, but a verified legacy-to-Java difference matters to users or downstream consumers; describe it.
- **GAP** — Required migrated behavior was not found after checking multiple search paths and ruling out plausible candidates.
- **CONFLICT** — Observed behavior contradicts an explicit acceptance criterion, including required legacy parity. Cite the condition and observable outcome.

After the table, give **recommended change points** with the module, cited code
location, suggested change and boundary test. Give **open questions** separately,
including unclear policy, scope, or unverified consumers. If none, say so.

## Specifications

Every testable rule has one verdict or a stated evidence blocker. Every
implementation claim and recommended change point has an opened-source citation.
Distinguish static code evidence from any behavior verified at runtime.

## Forbidden actions

- Never cite unopened code, search hits alone, or the migration map as proof.
- Never assume parity from matching words, field names, codes, or math.
- Never report a rule as missing after a single search.
- Do not edit source code, datasets, or configuration during validation.
