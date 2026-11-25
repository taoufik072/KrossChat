# AGENTS.md

This file provides guidance to Codex (Codex.ai/code) when working with code in this repository.

## Skills

Always use the available skills that match the task. The skills prefixed with `android-` cover the most common patterns in this codebase:

- **`android-module-structure`** — adding modules, convention plugins, version catalog, project layout
- **`android-presentation-mvi`** — ViewModel, State/Action/Event, Screen composables, SavedStateHandle
- **`android-compose-ui`** — Compose patterns, recomposition, modifiers, previews, design system
- **`android-navigation`** — type-safe routes, feature nav graphs, cross-feature callbacks
- **`android-data-layer`** — repositories, data sources, DTOs, mappers, Room entities, Ktor client
- **`android-di-koin`** — Koin modules per layer, `koinViewModel()`, assembling in `:shared`
- **`android-error-handling`** — `Result<T, E>`, `DataError`, `EmptyResult`, typed error flows
- **`android-testing`** — ViewModel unit tests with JUnit5, Turbine, fake repositories

## Build Commands

```bash
# Build entire project
./gradlew build

# Run unit tests for all KMP modules (JVM host; runs commonTest sources)
./gradlew testAndroidHostTest

# Run a single module's tests
./gradlew :feature:chat:domain:testAndroidHostTest

# Run Android instrumented tests
./gradlew connectedAndroidTest

# Run a single test class
./gradlew :feature:chat:domain:testAndroidHostTest --tests "com.taoufikcode.chat.domain.SomeTest"

# Assemble Android debug APK
./gradlew :androidApp:assembleDebug

# Lint
./gradlew lint

# Coverage (Kover, aggregated across modules -> build/reports/kover/report.xml)
./gradlew testAndroidHostTest koverXmlReport
./gradlew koverHtmlReport
./gradlew :androidApp:lintDebug
```

Detekt and SonarCloud were removed for now (their Gradle plugins are not yet
compatible with AGP 9); they will be re-added once compatible. The old config
(root build.gradle.kts blocks, config/detekt/, catalog aliases, CI step) is in
git history before 2026-07-19.

## Architecture

KrossChat is a **Kotlin Multiplatform** messaging app (Android + iOS + Desktop/JVM) built with Compose Multiplatform. It uses WebSocket (via Ktor) for real-time chat.

### Dependency flow

```
core:domain          ← pure business logic, no framework deps
     ↓
core:data            ← Ktor HTTP + WebSocket client, DataStore, BuildKonfig (API_KEY)
     ↓
core:presentation    ← shared ViewModels, UI models
core:designsystem    ← design tokens, shared Compose components (no domain dep)
     ↓
feature:*:domain     ← feature use cases + entities (depends on core:domain only)
feature:*:data       ← feature data sources + mappers (depends on core:domain)
feature:*:database   ← Room DAOs + entities (convention.room plugin)
     ↓
feature:*:presentation  ← screens + ViewModels (convention.cmp.feature plugin)
     ↓
shared               ← integration: nav graph wiring, Koin module assembly (KMP library, no app entry points)
     ↓
androidApp           ← Android application module: MainActivity, Application class, manifest
```

### Module naming rule

| Layer | Convention plugin | Included automatically |
|---|---|---|
| KMP library (data/domain) | `convention.kmp.library` | kotlinx-serialization, kotlin-test |
| KMP library with Compose | `convention.cmp.library` | + Material3, icons, Lifecycle, ViewModel |
| Feature module | `convention.cmp.feature` | + Koin, Navigation, SavedStateHandle |
| Room module | `convention.room` | + KSP Room processors for Android & iOS |
| BuildKonfig module | `convention.buildKonfig` | generates `BuildKonfig.API_KEY` from `local.properties` |

### WebSocket pattern

Real-time chat uses `ktor-client-core` WebSocket plugin in `feature:chat:data`. The WebSocket session is managed as a flow in the data source and exposed as a `Flow<ChatMessage>` through the repository. Platform-specific Ktor engines: OkHttp (Android), Darwin (iOS).

### Koin DI wiring

- Each module layer (`data`, `domain`, `presentation`) defines its own Koin module.
- All modules are assembled in `:shared` via `startKoin { modules(...) }`.
- Feature presentation modules use `koinViewModel()` in composables.

### Navigation

Type-safe Compose Navigation (Jetbrains). Each feature exposes a `NavGraphBuilder` extension. The root nav graph in `:shared` includes all feature graphs. Cross-feature navigation is done via callbacks passed down from the root.

### Key library versions

| Library | Version |
|---|---|
| Kotlin / KMP | 2.2.0 |
| Compose Multiplatform | 1.9.0-beta01 |
| Koin | 4.1.0 |
| Ktor | 3.2.3 |
| Room | 2.7.2 |
| Kotlinx Coroutines | 1.10.2 |

### Configuration cache

`gradle.properties` enables Gradle configuration cache and build cache. Avoid using `project.afterEvaluate` or task-graph mutations in convention plugins as they break configuration cache.
