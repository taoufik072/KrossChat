---
paths:
  - "**/navigation/**"
  - "shared/**"
---

# Navigation rules

Type-safe Compose Navigation (JetBrains multiplatform). Reference: `feature/chat/presentation/.../navigation/ChatGraphRoutes.kt`.

- Each feature declares its routes in a `sealed interface <Feature>GraphRoutes`, every route annotated `@Serializable`: `data object` without arguments, `data class` with arguments.
- Route arguments are primitives or `String` IDs only. Never pass a domain model or a large object through a route: pass the ID and load the data in the destination ViewModel.
- Each feature exposes one `NavGraphBuilder.<feature>Graph(navController, onXxx: () -> Unit)` extension, with a nested `navigation<Graph>(startDestination = ...)`.
- Leaving a feature is always a callback parameter (`onLogout`, `onOpenChat`) wired in the root graph in `:shared`. A feature never references another feature's routes.
- Read arguments with `backStackEntry.toRoute<Route>()` in the graph, or `SavedStateHandle.toRoute<Route>()` in the ViewModel.
- Deep links use the `chirp://` scheme and are declared on the `composable<>` entry (`navDeepLink { uriPattern = ... }`). Treat every deep-link argument as untrusted input: validate it before use, and fall back to a safe screen when it is invalid.
- Adaptive list-detail layouts keep the selected ID in the ViewModel state, not in the back stack.
