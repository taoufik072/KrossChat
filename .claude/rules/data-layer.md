---
paths:
  - "**/data/**"
  - "**/database/**"
---

# Data layer rules

## Errors

- Every repository and data-source function that can fail returns `Result<T, DataError>` or `EmptyResult<DataError>` from `core:domain` (`Result.Success` / `Result.Failure`). Never throw to the caller, never return `null` to signal an error.
- Network calls go through the `HttpClient` extensions in `core/data/network/HttpClientExt.kt` (`get`, `post`, `put`, `delete`, `safeCall`). Never call Ktor directly without `safeCall`: it maps exceptions and HTTP codes to `DataError.Remote`.
- Use the existing `DataError` values (`Remote`, `Local`, `Connection`). Add a new value only when no existing one fits, and map it in `core:presentation` (`DataErrorToUi`).
- Never swallow `CancellationException`.

## Models

- DTOs (`@Serializable`, in `dto/`) never leave the data module. Map them with extension functions in `mappers/` (`fun ChatDto.toDomain()`), with one test per mapper.
- Room entities stay in `feature:*:database`; map them to domain models in the data module.

## Offline-first

- The local Room database is the single source of truth. Repositories expose `Flow` from DAOs; the network only updates the database.
- Outgoing messages are written locally first with a delivery status, then sent; failures update the status so the user can retry.
- Group related writes in a Room `@Transaction`.

## Database

- Any schema change bumps the database version and ships with a migration (prefer `AutoMigration`). Never use a destructive migration on user data.
- DAO reads return `Flow`; writes are `suspend`.

## WebSocket

- The WebSocket session lives in the data layer and is exposed as `Flow`. Reconnect with backoff, only when connectivity is available (`ConnectivityObserver`) and the user is logged in.
- Expose the connection state (`ConnectionState`) so the UI can show it.
