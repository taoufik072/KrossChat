---
paths:
  - "**/iosMain/**"
  - "**/iosTest/**"
  - "iosApp/**"
---

# iOS / Kotlin Native rules

- Platform code sits behind an interface declared in `commonMain` (provided by Koin in a platform module) or an `expect`/`actual` pair. Prefer the interface for anything with state or dependencies.
- Use Apple frameworks through Kotlin/Native interop (`platform.Foundation`, `platform.Network`, ...). Release native resources (observers, monitors) when the flow is cancelled (`awaitClose { ... }`).
- Never block the main thread; bridge callbacks to coroutines with `callbackFlow` / `suspendCancellableCoroutine`.
- Public APIs consumed from Swift: avoid exposing `Flow` and `suspend` functions with complex generics without a Swift-friendly wrapper; never let an exception cross into Swift (return `Result`, or annotate with `@Throws`).
- Check iOS only when needed: `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64`.
- Do not modify `iosApp/` Xcode project settings unless explicitly asked.
