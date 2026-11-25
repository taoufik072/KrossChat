---
paths:
  - "core/**"
  - "feature/**"
  - "shared/**"
  - "androidApp/**"
---

# Architecture rules

Dependencies always point inward, towards the domain. Respect the flow described in `CLAUDE.md`.

## Module boundaries

- `core:domain` and `feature:*:domain` are pure Kotlin: no Ktor, no Room, no Compose, no Koin, no Android/iOS API.
- `feature:*:data` depends on `core:domain`, `core:data` and its own `feature:*:domain`. It implements the repository interfaces declared in the domain.
- `feature:*:presentation` depends on its own `feature:*:domain`, `core:presentation` and `core:designsystem`. It never imports a `data` or `database` module.
- **A feature never depends on another feature.** Cross-feature needs go through `core:*` contracts or navigation callbacks.
- `core:designsystem` has no domain dependency: components take plain parameters, not domain models.
- `:shared` is the only place that wires everything together (root nav graph, `startKoin { modules(...) }`). `:androidApp` only hosts entry points.

## New code

- New module: use the matching convention plugin (`convention.kmp.library`, `convention.cmp.library`, `convention.cmp.feature`, `convention.room`) and register it in `settings.gradle.kts`. Never configure targets or compile options by hand in a module.
- New dependency: add it to `gradle/libs.versions.toml` first, then reference it as `libs.*`.
- Put code in `commonMain` by default. Use `androidMain` / `iosMain` only for platform APIs, behind an interface or `expect`/`actual`.
- Before adding a class, check whether an equivalent already exists in `core:*` (`Result`, `DataError`, `ObserveAsEvents`, `Paginator`, design-system components).
