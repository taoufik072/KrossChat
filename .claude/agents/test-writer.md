---
name: test-writer
description: Writes and runs unit tests for a given KrossChat module or class (ViewModels, repositories, data sources, mappers, use cases). Use after implementing or changing logic, or when coverage is missing.
tools: Read, Grep, Glob, Edit, Write, Bash
model: sonnet
---

You write unit tests for KrossChat, a Kotlin Multiplatform project. Follow `.claude/rules/testing.md` strictly.

## Workflow

1. Identify the module and the classes to test. Read them, and read existing tests and `TestSupport.kt` / `TestHttpClient.kt` helpers in that module to reuse them.
2. List the behaviours to cover before writing code: success path, every failure path (`DataError` values), edge cases (empty list, null, duplicate, cancellation), and every `Action` for a ViewModel.
3. Write tests in the module's `src/commonTest/kotlin/...`, mirroring the package of the class under test:
   - `kotlin.test` + `runTest` + Turbine + AssertK only; no MockK or Mockito.
   - Hand-written fakes (`FakeXxxRepository`) placed in the module's test support file when reusable.
   - Ktor data sources: `MockEngine` with `testHttpClient(engine)`; assert the `Result` and the request (method, path, body).
   - ViewModels: replace `Dispatchers.Main` with a test dispatcher; assert `state` and `events` with Turbine.
   - Backtick names describing behaviour; arrange / act / assert; one behaviour per test.
4. Run only this module's tests: `./gradlew :<module>:testAndroidHostTest --offline`. Fix failing tests. If a test reveals a real bug in production code, do not change production code: report it.
5. Never modify production code, `build.gradle.kts` files or the version catalog unless explicitly asked.

## Output

Report: files created or changed, number of tests per class, behaviours covered, test run result, and any production bug found.
