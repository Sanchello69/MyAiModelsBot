# AI Models Chat Bot

Android-приложение для общения с различными AI-моделями через OpenRouter API.

## Функциональность

- Чат-интерфейс для общения с AI
- Поддержка трех моделей:
  - DeepSeek Chimera (`tngtech/deepseek-r1t2-chimera:free`)
  - Amazon Nova Lite (`amazon/nova-2-lite-v1:free`)
  - Google Gemma (`google/gemma-3n-e4b-it:free`)
- История сообщений
- Индикаторы загрузки
- Обработка ошибок

## Технологический стек

- **Язык**: Kotlin
- **UI**: Jetpack Compose
- **Архитектура**: Clean Architecture (Data, Domain, Presentation layers)
- **Асинхронность**: Coroutines + Flow
- **Networking**: Retrofit2 + OkHttp
- **DI**: Koin
- **Паттерн**: UDF (Unidirectional Data Flow)

## Структура проекта

```
app/
├── data/
│   ├── mapper/          # DTO to Domain mappers
│   ├── remote/
│   │   ├── dto/         # Data Transfer Objects
│   │   └── OpenRouterApiService.kt
│   └── repository/      # Repository implementations
├── domain/
│   ├── model/           # Domain models
│   ├── repository/      # Repository interfaces
│   └── usecase/         # Use cases
├── presentation/
│   └── chat/            # Chat screen, ViewModel, UI State
└── di/                  # Koin DI modules
```

## Установка и запуск

### 1. Клонирование проекта

Проект уже создан в директории `MyAiModelsBot`.

### 2. API ключ уже настроен! ✅

**API ключ уже добавлен и защищен:**
- Хранится в `local.properties` (файл игнорируется Git)
- Автоматически загружается через `BuildConfig`
- Не попадет в Git при коммитах

**Если вы клонируете проект на другой машине:**
1. Создайте файл `local.properties` в корне проекта
2. Добавьте строку: `OPENROUTER_API_KEY=ваш-ключ`
3. Подробнее: см. [API_KEY_SETUP.md](API_KEY_SETUP.md)

### 3. Сборка проекта

1. Откройте проект в Android Studio
2. Синхронизируйте Gradle: `File > Sync Project with Gradle Files`
3. Запустите приложение на эмуляторе или реальном устройстве

## API Integration

Приложение использует OpenRouter API:
- **Base URL**: `https://openrouter.ai/api/v1/`
- **Endpoint**: `POST /chat/completions`
- **Документация**: [OpenRouter API Docs](https://openrouter.ai/docs/api/reference/overview)

### Формат запроса

```json
{
  "model": "tngtech/deepseek-r1t2-chimera:free",
  "messages": [
    {
      "role": "user",
      "content": "Hello, how are you?"
    }
  ]
}
```

## Использование

1. Запустите приложение
2. Выберите AI модель из выпадающего списка
3. Введите сообщение в текстовое поле
4. Нажмите кнопку отправки
5. Дождитесь ответа от AI

## Обработка ошибок

Приложение обрабатывает следующие ошибки:
- Отсутствие интернет-соединения
- Ошибки API (неверный ключ, лимиты запросов)
- Таймауты запросов
- Непредвиденные ошибки сервера

Все ошибки отображаются в виде Snackbar с понятным описанием.

## Архитектурные решения

### Clean Architecture

**Data Layer**:
- `OpenRouterApiService` - Retrofit интерфейс для API
- `ChatRepositoryImpl` - Реализация репозитория
- DTOs и mappers для преобразования данных

**Domain Layer**:
- `Message`, `AiModel` - Domain модели
- `ChatRepository` - Интерфейс репозитория
- `SendMessageUseCase` - Use case для отправки сообщений

**Presentation Layer**:
- `ChatViewModel` - Управление состоянием UI
- `ChatUiState` - State класс
- `ChatEvent` - События от UI
- `ChatScreen` - Compose UI

### UDF (Unidirectional Data Flow)

1. UI отправляет события (`ChatEvent`) в ViewModel
2. ViewModel обрабатывает события и обновляет состояние
3. UI подписывается на StateFlow и реагирует на изменения
4. Данные текут в одном направлении: UI → ViewModel → State → UI

## Требования

- Android Studio Hedgehog | 2023.1.1 или новее
- minSdk: 24 (Android 7.0)
- targetSdk: 35 (Android 15)
- Kotlin 2.0.0
- Gradle 8.8.0

## Лицензия

Проект создан для образовательных целей.
