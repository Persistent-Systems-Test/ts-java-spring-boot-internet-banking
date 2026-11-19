# SAS → Java migration map

Use this inventory to locate candidates when validating a requirement. It records
observed functional overlap, not historical migration lineage, full parity, or
what is deployed. SAS paths are relative to
[`ts-sas-legacy-analytics`](https://github.com/Persistent-Systems-Test/ts-sas-legacy-analytics);
Java links are relative to this repo. Line numbers were reviewed at SAS
`e3378dfea4e4a8ba274b8b4fa34f2ff39e78d3d5` and Java
`fa1c44541a241b416d02d08faa677f731f0c84c4`; reopen current code before citing.

**Partial overlap** identifies a Java class serving a related function.
**No counterpart found** means no implementation of that SAS function was
identified in the checked-in Java services, schemas, or application configuration;
it is not evidence of absence outside this repo. Framework primitives and
similarly named fields do not count by themselves. Coverage is the tracked
`.sas` sources, not packaged Enterprise Guide projects or spreadsheet assets.

## Programs, jobs, formats, and configuration

| SAS reference | Legacy behavior | Java counterpart and limits |
| --- | --- | --- |
| `Programs/Banking/load_customer_accounts.sas:82-100`, `:123-179` | Builds an account snapshot. `NEG_BAL` flags negative current balances on selected deposit types but retains the snapshot row and publishes exceptions. | **Partial overlap** — core banking [`TransactionService.java:35-55`](../core-banking-service/src/main/java/com/javatodev/finance/service/TransactionService.java#L35-L55) calls [`TransactionService.java:77-81`](../core-banking-service/src/main/java/com/javatodev/finance/service/TransactionService.java#L77-L81) to reject online transfers or utility payments when the source actual balance is negative or below the amount, without a deposit-type filter. [`AccountService.java:27-30`](../core-banking-service/src/main/java/com/javatodev/finance/service/AccountService.java#L27-L30) reads accounts; neither path reproduces the snapshot, `NEG_BAL` output, or notification. |
| `Programs/Banking/daily_transaction_processing.sas:45-96`, `:137-154`, `:172-225` | Validates a feed, computes ordered running balances, flags `OVERDRAFT` among other anomalies, and publishes curated rows. | **Partial overlap** — core banking [`TransactionService.java:48-73`](../core-banking-service/src/main/java/com/javatodev/finance/service/TransactionService.java#L48-L73) posts a utility payment and [`TransactionService.java:83-106`](../core-banking-service/src/main/java/com/javatodev/finance/service/TransactionService.java#L83-L106) posts both sides of a transfer. [`TransactionService.java:77-81`](../core-banking-service/src/main/java/com/javatodev/finance/service/TransactionService.java#L77-L81) rejects insufficient funds before posting, not via the batch anomaly table. No feed validation, ordered batch output, or anomaly job found. |
| `Programs/Banking/credit_risk_scoring.sas:92-110`, `:231-238` | Executes credit scoring and publishes risk scores and migration data. | **No counterpart found.** |
| `Programs/Banking/monthly_regulatory_reporting.sas:41-73`, `:115-123`, `:170-183` | Produces Basel III risk-weighted assets, delinquency, loan-loss coverage, and capital reporting. | **No counterpart found.** |
| `Programs/Insurance/claims_processing.sas:38-85`, `:194-207` | Validates claims against policies and writes claims, manual-review, and fraud outputs. | **No counterpart found.** |
| `Programs/Insurance/policy_valuation.sas:123-140`, `:184-200` | Calculates policy valuation and produces a loss-ratio summary. | **No counterpart found.** |
| `Programs/Parent-Child-Index.sas:5-26`, `:233-246` | Demonstrates account hierarchy indexing and fact/dimension joins. | **No counterpart found.** An account lookup is not a hierarchy traversal. |
| `Programs/Reports/customer_profitability.sas:86-110`, `:159-166` | Rolls up income, fees, and expected credit loss into customer P&L and exports it. | **No counterpart found.** |
| `BatchJobs/run_daily_banking.sas:119-142` | Runs banking jobs in order, applies date gates, and archives batch telemetry. | **No counterpart found.** |
| `BatchJobs/run_daily_insurance.sas:108-121` | Orchestrates insurance jobs and archives batch telemetry. | **No counterpart found.** |
| `Config/autoexec.sas:34-76` | Sets up SAS libraries for raw, staging, curated, report, and warehouse data. | **No counterpart found.** Service datasource/config bootstrap is not an implementation of SAS librefs or batch setup. |
| `Formats/banking_formats.sas:12-68` | Maps account types, statuses, transaction categories, and risk ratings to display values. | **Partial overlap** — core banking [`AccountType.java:3-5`](../core-banking-service/src/main/java/com/javatodev/finance/model/AccountType.java#L3-L5), [`AccountStatus.java:3-5`](../core-banking-service/src/main/java/com/javatodev/finance/model/AccountStatus.java#L3-L5), and [`TransactionType.java:3-7`](../core-banking-service/src/main/java/com/javatodev/finance/model/TransactionType.java#L3-L7) represent related concepts, with different value sets and no equivalent SAS format catalog. |
| `Formats/insurance_formats.sas:11-49` | Maps policy, claims, and insurance risk codes for reporting. | **No counterpart found.** |

## Utility macros

Macro purpose references point to the source header or definition. No Java
counterpart was found for the utility contracts below. For example, a Spring
transaction is not a SAS dataset lock, repository lookup is not a SAS DATA-step
hash lookup, and request validation is not macro parameter validation.

| SAS reference | Legacy utility | Java counterpart |
| --- | --- | --- |
| `Macro/@TEMPLATE.sas:4` | Macro authoring template | No counterpart found |
| `Macro/CreateTableOrView.sas:3-4` | Create a SAS dataset or view | No counterpart found |
| `Macro/IsNum.sas:3-4` | Check whether character data represents a number | No counterpart found |
| `Macro/IsNumD.sas:3-4` | Check numeric input inside a DATA step | No counterpart found |
| `Macro/IsNumM.sas:3-7` | Check numeric macro-variable input | No counterpart found |
| `Macro/RunAll.sas:3-5` | Run SAS programs asynchronously with dependencies | No counterpart found |
| `Macro/RunAll_ControlTable.sas:3-5` | Run dependency-aware SAS jobs with control data | No counterpart found |
| `Macro/age.sas:3-4` | Compute age as of a reference date | No counterpart found |
| `Macro/align_decimals.sas:3-6` | Align decimal points for display | No counterpart found |
| `Macro/attrib.sas:3` | Generate ATTRIB statements from a template | No counterpart found |
| `Macro/batch_submit.sas:3-5` | Submit a SAS editor session in batch | No counterpart found |
| `Macro/bench.sas:3` | Measure elapsed time between calls | No counterpart found |
| `Macro/check_if_empty.sas:3` | Check whether a dataset is empty | No counterpart found |
| `Macro/compare.sas:3` | Compare SAS datasets or libraries | No counterpart found |
| `Macro/count_words.sas:3` | Count delimited words | No counterpart found |
| `Macro/create_datetime_range.sas:3` | Build datetime bounds for a period | No counterpart found |
| `Macro/create_directory.sas:3-4` | Create a directory via SAS DLCREATEDIR | No counterpart found |
| `Macro/create_format.sas:3` | Create a format from a dataset | No counterpart found |
| `Macro/date_impute.sas:3` | Impute partial dates | No counterpart found |
| `Macro/dedup_mstring.sas:3-4` | Deduplicate words in a macro variable | No counterpart found |
| `Macro/dedup_string.sas:3` | Deduplicate items in a string | No counterpart found |
| `Macro/delete_file.sas:3` | Delete an external file by path or fileref | No counterpart found |
| `Macro/dirlist.sas:3` | List directory entries in a SAS dataset | No counterpart found |
| `Macro/dump_mvars.sas:3` | Log macro variables | No counterpart found |
| `Macro/empty.sas:3` | Check whether a dataset is empty | No counterpart found |
| `Macro/excel2sas.sas:3` | Import an Excel workbook into SAS | No counterpart found |
| `Macro/execpath.sas:3-4` | Get the current SAS program path or filename | No counterpart found |
| `Macro/execute_macro.sas:3` | Execute a macro if it exists | No counterpart found |
| `Macro/export.sas:3` | Replace PROC EXPORT | No counterpart found |
| `Macro/export_csv.sas:3` | Export a SAS dataset to CSV | No counterpart found |
| `Macro/export_dbms.sas:3` | Wrap PROC EXPORT for external output | No counterpart found |
| `Macro/export_dlm.sas:3` | Export a SAS dataset to a delimited file | No counterpart found |
| `Macro/export_rldx.sas:3-4` | Dispatch SAS or SQL Server exports to child macros | No counterpart found |
| `Macro/export_saphari.sas:3` | Export SAS or SQL Server data to Saphari | No counterpart found |
| `Macro/export_sas.sas:3-4` | Copy a dataset to another SAS library | No counterpart found |
| `Macro/export_spss.sas:3` | Export a SAS dataset to SPSS | No counterpart found |
| `Macro/export_stata.sas:3` | Export a SAS dataset to Stata | No counterpart found |
| `Macro/export_tab.sas:3` | Export a SAS dataset to a tab-delimited file | No counterpart found |
| `Macro/export_xlsx.sas:3` | Export a SAS dataset to XLSX | No counterpart found |
| `Macro/fmtexist.sas:3` | Check the format search path | No counterpart found |
| `Macro/fmtlist.sas:3` | Print format or informat entries | No counterpart found |
| `Macro/format_text.sas:1-10` | Lay out left, center, and right text in a buffer | No counterpart found |
| `Macro/get_data_attr.sas:3` | Get a dataset attribute | No counterpart found |
| `Macro/get_dups.sas:3` | Identify duplicate records by variables | No counterpart found |
| `Macro/get_lib_attr.sas:3` | Get a library attribute | No counterpart found |
| `Macro/get_parameters.sas:3-4` | Read global and job-specific parameters | No counterpart found |
| `Macro/get_permutations.sas:3` | Generate combinations from an input list | No counterpart found |
| `Macro/getpassword.sas:3` | Read a password from an external file | No counterpart found |
| `Macro/guess_pk.sas:3-4` | Infer a dataset primary key | No counterpart found |
| `Macro/handle.sas:3-4` | List open file handles | No counterpart found |
| `Macro/hash_define.sas:3` | Define a DATA-step hash object | No counterpart found |
| `Macro/hash_lookup.sas:3` | Look up attributes in a DATA-step hash object | No counterpart found |
| `Macro/hash_split_dataset.sas:3` | Split a dataset via a hash object | No counterpart found |
| `Macro/justify.sas:3-4` | Justify text in a fixed-width field | No counterpart found |
| `Macro/kill.sas:3` | Delete specified library contents | No counterpart found |
| `Macro/libname_attr_sqlsvr.sas:3` | Read SQL Server libname attributes | No counterpart found |
| `Macro/libname_sqlsvr.sas:3` | Allocate a SQL Server library via ODBC | No counterpart found |
| `Macro/lock.sas:3` | Acquire or release a SAS dataset lock | No counterpart found |
| `Macro/log2pdf.sas:8` | Render a SAS log as a PDF | No counterpart found |
| `Macro/logparse.sas:3-4` | Extract performance statistics from a SAS log | No counterpart found |
| `Macro/loop.sas:3` | Loop over a list and execute SAS code | No counterpart found |
| `Macro/loop_control.sas:3` | Loop over control-table items | No counterpart found |
| `Macro/marker.sas:3` | Process marker files for job scheduling | No counterpart found |
| `Macro/max_decimals.sas:3-4` | Find maximum decimal places in variables | No counterpart found |
| `Macro/nobs.sas:3` | Count dataset observations | No counterpart found |
| `Macro/optload.sas:3` | Load SAS options from a dataset | No counterpart found |
| `Macro/optsave.sas:3` | Save SAS options to a dataset | No counterpart found |
| `Macro/optval.sas:3` | Read the value of a SAS option | No counterpart found |
| `Macro/pagexofy.sas:3` | Add Page X of Y numbering | No counterpart found |
| `Macro/parmv.sas:3` | Validate macro parameters | No counterpart found |
| `Macro/queryActiveDirectory.sas:3` | Query Active Directory over LDAP | No counterpart found |
| `Macro/randlist.sas:3` | Create a random data list | No counterpart found |
| `Macro/realloc_concat_libs.sas:3` | Reallocate concatenated SAS libraries with read-only members | No counterpart found |
| `Macro/reduce_pixel.sas:3-4` | Simplify map data based on output pixel size | No counterpart found |
| `Macro/sendmail.sas:3` | Send email using a metadata dataset | No counterpart found |
| `Macro/seplist.sas:3` | Emit a delimiter-separated list | No counterpart found |
| `Macro/splitvar.sas:3` | Insert split characters in DATA-step text | No counterpart found |
| `Macro/sql_datetime.sas:3-4` | Convert SAS datetime literals to SQL Server literals | No counterpart found |
| `Macro/squote.sas:3` | Wrap an input in single quotes | No counterpart found |
| `Macro/stp_batch_submit.sas:3` | Submit a batch program from a Stored Process | No counterpart found |
| `Macro/stp_seplist.sas:3` | Flatten Stored Process multi-select inputs | No counterpart found |
| `Macro/stp_session.sas:8-9` | Wrap Stored Process session functions | No counterpart found |
| `Macro/subset_data.sas:3` | Filter dataset rows/columns or rename variables | No counterpart found |
| `Macro/symget.sas:3` | Read a global macro variable shadowed by a local one | No counterpart found |
| `Macro/time_interval.sas:3` | Build date/time interval metadata | No counterpart found |
| `Macro/transpose.sas:3` | Transpose a SAS dataset | No counterpart found |
| `Macro/txt2pdf.sas:4` | Convert text to PDF | No counterpart found |
| `Macro/txt2rtf.sas:28` | Convert ASCII text to RTF | No counterpart found |
| `Macro/useridToEmail.sas:3` | Map a user ID to an email address | No counterpart found |
| `Macro/varexist.sas:3` | Check whether a SAS variable exists | No counterpart found |
| `Macro/varlist.sas:3` | List variables in a dataset | No counterpart found |
| `Macro/varlist2.sas:3` | List dataset variables with additional options | No counterpart found |
