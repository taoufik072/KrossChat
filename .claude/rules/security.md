---
paths:
  - "core/data/**"
  - "**/data/**"
  - "**/network/**"
  - "**/auth/**"
---

# Security rules

- Never hardcode a secret, key or token. The API key comes from `BuildKonfig.API_KEY` (generated from `local.properties`); never print it, log it or commit it.
- Never read or modify `local.properties`, `google-services.json`, `GoogleService-Info.plist` or keystores.
- Ktor `Logging`: full logging (`LogLevel.ALL`) only in debug builds, and always sanitize the `Authorization` and `x-api-key` headers (`sanitizeHeader { ... }`).
- Tokens (access and refresh) are stored only through `SessionStorage` (`DataStoreSessionStorage`). Never put them anywhere else: other preferences, logs, analytics or crash reports.
- Known gap: `DataStoreSessionStorage` is not encrypted yet. Any new storage of sensitive data must be encrypted (Android Keystore / iOS Keychain).
- HTTPS only; no cleartext traffic.
- Treat every external input as untrusted: deep-link arguments, push payloads, WebSocket messages. Validate before use.
- Do not disable TLS checks, even temporarily, even in debug.
