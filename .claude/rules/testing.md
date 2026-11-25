---
paths:
  - "**/commonTest/**"
  - "**/androidHostTest/**"
  - "**/iosTest/**"
---

# Testing rules

- Shared logic is tested in `commonTest` with multiplatform libraries only: `kotlin.test`, `kotlinx-coroutines-test` (`runTest`), Turbine, AssertK. Do not use MockK or Mockito in `commonTest` (JVM-only).
- Use hand-written fakes (`FakeChatRepository`) rather than mocks. Shared fakes and helpers live in a `TestSupport.kt` file of the module.
- Network: test data sources with Ktor `MockEngine` and the module's `testHttpClient(engine)` helper. Assert both the parsed `Result` and the request (method, path, body).
- ViewModels: assert `state` and `events` with Turbine (`test { awaitItem() ... }`); replace `Dispatchers.Main` with a test dispatcher.
- Every mapper, every `DataError` mapping and every ViewModel action gets a test. Always cover the failure path, not only success.
- Name tests with backticks describing behaviour: `` `getChats maps server error to SERVER_ERROR` ``.
- Structure: arrange / act / assert, one behaviour per test.
- Run only the tests of the touched module: `./gradlew :<module>:testAndroidHostTest`.
- Do not add `ExampleUnitTest`-style placeholder tests.
