---
paths:
  - "**/presentation/**"
  - "core/designsystem/**"
---

# Presentation rules (MVI)

Reference: `feature/chat/presentation/.../chat_list/` (`ChatListViewModel`, `ChatListState`, `ChatListAction`, `ChatListEvent`).

## Per screen

- `XxxState`: one immutable `data class` with default values.
- `XxxAction`: `sealed interface` of user intents, prefixed `On` (`OnSelectChat`).
- `XxxEvent`: `sealed interface` of one-shot events (navigation, snackbar).
- `XxxViewModel`: exposes `val state: StateFlow<XxxState>`, `val events: Flow<XxxEvent>` and a single `fun onAction(action: XxxAction)`.
- `XxxRoot` composable gets the ViewModel with `koinViewModel()`, collects state with `collectAsStateWithLifecycle()` and events with `ObserveAsEvents`. `XxxScreen` is stateless: it takes `state` and `onAction` only, so it can be previewed and tested.

## ViewModel

- Build state with `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), initialValue)`; load initial data in `onStart` guarded by a `hasLoadedInitialData` flag.
- Update state with `_state.update { it.copy(...) }`, never by reassigning `value` from several places.
- One-shot events go through `Channel<Event>()` + `receiveAsFlow()`.
- Map domain models to UI models (`toUi()`) in the ViewModel; composables never see domain or DTO types.
- No Android or iOS API in ViewModels.

## Compose

- Use `core:designsystem` components and tokens; no hardcoded colors, dimensions or text styles.
- Pass stable, immutable parameters; use `key` in lazy lists.
- Every new screen and component gets previews (light and dark).
- User-visible strings go in Compose resources, never hardcoded.
