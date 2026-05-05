---
description: "Use when: working on Kotlin/Compose Android code to follow architecture best practices."
applyTo: "app/src/main/java/**"
---

# Android Architecture Guidance

## Layering and data flow
- Use a clear data layer with repositories as the single source of truth.
- UI and ViewModels must not call data sources directly (Firebase, DataStore, network, etc.).
- Follow unidirectional data flow: UI -> ViewModel actions -> state updates -> UI render.
- Prefer coroutines and Flows between layers.
- Add a domain layer with use cases if logic is reused across multiple ViewModels or gets complex.

## Firebase/Auth pattern
- Define an AuthRepository interface in the data layer.
- Keep Firebase SDK calls inside a DefaultAuthRepository.
- ViewModels depend on AuthRepository, never on FirebaseAuth directly.
- Expose auth state as Flow<Session?> or StateFlow<Session?> from repository.

## UI layer (Compose)
- Screens collect UI state via collectAsStateWithLifecycle.
- ViewModels expose a single uiState StateFlow when practical.
- Avoid sending one-off events from ViewModels; update state instead.
- Keep ViewModels at screen level, not inside reusable composables.
- Use plain state holders for reusable UI components and hoist state.

## ViewModel rules
- Do not depend on Activity, Context, Resources, or AndroidViewModel.
- Use viewModelScope for actions and Flow/StateFlow for streams.
- Use WhileSubscribed(5_000) when converting flows to StateFlow.

## ViewModel template (preferred)
```kotlin
data class ${Screen}UiState(
	val isLoading: Boolean = false,
	val errorMessage: String? = null,
)

class ${Screen}ViewModel(
	private val repository: ${Feature}Repository,
) : ViewModel() {
	private val _uiState = MutableStateFlow(${Screen}UiState())
	val uiState: StateFlow<${Screen}UiState> = _uiState

	fun onAction(/* ... */) {
		viewModelScope.launch {
			// update state and call repository
		}
	}
}
```

## Lifecycle
- Prefer lifecycle-aware effects and repeatOnLifecycle over Activity callbacks.

## Dependencies
- Prefer constructor injection; scope only when needed.
- Use Hilt if the project grows beyond simple manual DI.

## Testing
- Unit test ViewModels and repositories; favor fakes over mocks.
- Test StateFlow values directly when possible.

## Naming
- Methods: verb phrases (e.g., createUser()).
- Properties: noun phrases (e.g., isLoading).
- Streams: get{Model}Stream() / get{Models}Stream().
- Implementations: Default*, Fake* where appropriate.
