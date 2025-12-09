# Архитектура приложения

## Обзор

Приложение следует принципам **Clean Architecture** с разделением на три основных слоя:

```
┌─────────────────────────────────────┐
│      Presentation Layer             │
│  (UI, ViewModel, State, Events)     │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│       Domain Layer                  │
│  (Use Cases, Models, Interfaces)    │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│        Data Layer                   │
│  (Repository, API, DTOs, Mappers)   │
└─────────────────────────────────────┘
```

## Слои приложения

### 1. Data Layer (Данные)

**Назначение**: Управление источниками данных и их преобразование

**Компоненты**:

- **API Service** (`OpenRouterApiService.kt`)
  - Retrofit интерфейс для взаимодействия с OpenRouter API
  - Определяет HTTP endpoints

- **DTOs** (Data Transfer Objects)
  - `ChatRequest.kt` - модель запроса к API
  - `ChatResponse.kt` - модель ответа от API
  - `MessageDto.kt` - DTO для сообщения

- **Repository Implementation** (`ChatRepositoryImpl.kt`)
  - Реализует интерфейс из Domain layer
  - Обрабатывает вызовы API
  - Преобразует DTO в Domain модели через mappers

- **Mappers** (`MessageMapper.kt`)
  - Функции расширения для преобразования DTO ↔ Domain

```kotlin
// Пример маппинга
fun MessageDto.toDomain(): Message
fun Message.toDto(): MessageDto
```

### 2. Domain Layer (Бизнес-логика)

**Назначение**: Независимая от фреймворков бизнес-логика

**Компоненты**:

- **Models** (Доменные модели)
  - `Message.kt` - модель сообщения с ролью и контентом
  - `MessageRole.kt` - enum для ролей (USER, ASSISTANT, SYSTEM)
  - `AiModel.kt` - enum с доступными AI моделями

- **Repository Interface** (`ChatRepository.kt`)
  - Определяет контракт для работы с данными
  - Не зависит от конкретной реализации

- **Use Cases** (`SendMessageUseCase.kt`)
  - Инкапсулирует бизнес-логику
  - Один use case = одно действие
  - Вызывает repository

```kotlin
class SendMessageUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(
        model: AiModel,
        messages: List<Message>
    ): Result<Message>
}
```

### 3. Presentation Layer (Представление)

**Назначение**: UI и управление состоянием экранов

**Компоненты**:

- **UI State** (`ChatUiState.kt`)
  - Immutable data class с состоянием экрана
  - Содержит: messages, selectedModel, isLoading, error, inputText

- **Events** (`ChatEvent.kt`)
  - Sealed class с событиями от UI
  - OnMessageChange, OnSendMessage, OnModelSelect, OnErrorDismiss

- **ViewModel** (`ChatViewModel.kt`)
  - Управляет состоянием UI через StateFlow
  - Обрабатывает события от UI
  - Вызывает use cases
  - Обновляет UI state

- **Composable UI** (`ChatScreen.kt`)
  - Jetpack Compose UI компоненты
  - Отображает state
  - Генерирует events

## Паттерн UDF (Unidirectional Data Flow)

```
┌──────────┐         ┌────────────┐         ┌──────────┐
│    UI    │ Events  │  ViewModel │  State  │    UI    │
│  Screen  ├────────►│            ├────────►│  Screen  │
│          │         │ StateFlow  │         │          │
└──────────┘         └─────┬──────┘         └──────────┘
                           │
                           ▼
                    ┌──────────────┐
                    │   Use Case   │
                    └──────┬───────┘
                           │
                           ▼
                    ┌──────────────┐
                    │  Repository  │
                    └──────────────┘
```

**Поток данных**:
1. UI отправляет событие (ChatEvent) в ViewModel
2. ViewModel обрабатывает событие
3. ViewModel вызывает Use Case
4. Use Case работает с Repository
5. Repository запрашивает данные через API
6. Результат возвращается обратно по цепочке
7. ViewModel обновляет StateFlow
8. UI автоматически реагирует на изменения State

## Dependency Injection (Koin)

**Модули DI**:

- **NetworkModule** - Retrofit, OkHttp, API Service
- **RepositoryModule** - Repository implementations
- **DomainModule** - Use Cases
- **PresentationModule** - ViewModels

```kotlin
// Инициализация в Application классе
startKoin {
    androidContext(this@AiChatApplication)
    modules(
        networkModule,
        repositoryModule,
        domainModule,
        presentationModule
    )
}
```

**Внедрение зависимостей**:

```kotlin
// ViewModel
val presentationModule = module {
    viewModel { ChatViewModel(get()) }
}

// Use Case
val domainModule = module {
    factory { SendMessageUseCase(get()) }
}

// Repository
val repositoryModule = module {
    single<ChatRepository> { ChatRepositoryImpl(get()) }
}
```

## Обработка состояний

**Loading State**:
```kotlin
_uiState.update { it.copy(isLoading = true) }
```

**Success State**:
```kotlin
_uiState.update {
    it.copy(
        messages = it.messages + assistantMessage,
        isLoading = false
    )
}
```

**Error State**:
```kotlin
_uiState.update {
    it.copy(
        isLoading = false,
        error = error.message
    )
}
```

## Асинхронность

**Coroutines**:
- `viewModelScope.launch` - для запуска корутин в ViewModel
- `suspend fun` - для асинхронных операций
- `StateFlow` - для реактивного обновления UI

**Flow операторы**:
```kotlin
val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

// В Composable
val uiState by viewModel.uiState.collectAsState()
```

## Особенности реализации

### 1. Реактивность
- UI подписывается на StateFlow и автоматически перерисовывается
- Нет необходимости вручную обновлять UI

### 2. Immutability
- State классы immutable (data class)
- Обновление через `copy()`

### 3. Single Source of Truth
- Один источник истины - StateFlow в ViewModel
- UI только читает state, не изменяет его

### 4. Тестируемость
- Каждый слой может быть протестирован независимо
- Use Cases инкапсулируют логику
- Repository - интерфейс, легко мокировать

## Расширяемость

**Добавление новой модели**:
```kotlin
enum class AiModel(val modelId: String, val displayName: String) {
    NEW_MODEL("model-id", "Display Name")
}
```

**Добавление нового use case**:
```kotlin
class NewUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke() { ... }
}
```

**Добавление нового экрана**:
1. Создать State, Event, ViewModel
2. Создать Composable UI
3. Добавить ViewModel в Koin модуль

## Лучшие практики

✅ Следуй принципу единственной ответственности
✅ Избегай логики в UI слое
✅ Используй sealed classes для событий и результатов
✅ Обрабатывай все состояния (loading, success, error)
✅ Используй Kotlin Flow для реактивности
✅ Пиши тесты для каждого слоя

## Диаграмма зависимостей

```
MainActivity
    └── ChatScreen (Composable)
            └── ChatViewModel
                    └── SendMessageUseCase
                            └── ChatRepository (Interface)
                                    └── ChatRepositoryImpl
                                            └── OpenRouterApiService (Retrofit)
```

## Заключение

Данная архитектура обеспечивает:
- **Разделение ответственности** между слоями
- **Тестируемость** каждого компонента
- **Масштабируемость** приложения
- **Maintainability** кода
- **Независимость** от фреймворков
