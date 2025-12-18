# Bitcoin Monitor Integration

## Что было реализовано

### 1. MCP сервер
- Создан HTTP wrapper для локального MCP сервера (`/Users/aleksandrvasilev/McpServer/http-server.js`)
- Сервер запускается на `http://localhost:3000`
- Предоставляет API для получения курса биткоина через CoinCap API

### 2. Android приложение
- Удален выбор модели, зафиксирована модель: `nex-agi/deepseek-v3.1-nex-n1:free`
- Добавлен `SimpleMcpClient` для работы с локальным HTTP MCP сервером
- Создана база данных для хранения истории курса биткоина
- Реализован `BitcoinMonitorService` - фоновый сервис для мониторинга курса каждые 30 секунд
- Добавлен `BitcoinMonitorScreen` - UI для отображения текущего курса, анализа AI и истории
- Интегрирована навигация между Chat и Bitcoin Monitor через вкладки

### 3. Функционал
- **Мониторинг**: Каждые 30 секунд приложение запрашивает текущий курс биткоина через MCP
- **Анализ**: AI модель анализирует изменение курса и делает выводы
- **Хранение**: История курса и анализов сохраняется в локальную базу данных
- **Уведомления**: Foreground service показывает уведомление с текущим курсом и кратким анализом
- **UI**: Отображение текущего курса, последнего анализа и истории последних 20 проверок

## Как запустить

### 1. Запуск MCP сервера
```bash
cd /Users/aleksandrvasilev/McpServer
npm run http
```

Сервер запустится на `http://localhost:3000`

Проверить работу можно командой:
```bash
curl http://localhost:3000/bitcoin/price
```

### 2. Настройка API ключа
Убедитесь, что в `/Users/aleksandrvasilev/McpServer/.env` указан ваш `COINCAP_API_KEY`:
```
COINCAP_API_KEY=your_api_key_here
```

### 3. Запуск Android приложения
1. Откройте проект в Android Studio
2. Соберите и запустите приложение
3. Перейдите на вкладку "Bitcoin Monitor"
4. Нажмите кнопку "Запустить" для начала мониторинга

## Важные замечания

### Для эмулятора Android
- MCP сервер должен быть доступен по адресу `10.0.2.2:3000` (это localhost для эмулятора)
- В коде `SimpleMcpClient` используется именно этот адрес

### Для реального устройства
- Нужно изменить адрес в `SimpleMcpClient.kt`:
```kotlin
private val baseUrl: String = "http://YOUR_COMPUTER_IP:3000"
```
- Замените `YOUR_COMPUTER_IP` на IP адрес вашего компьютера в локальной сети

### Permissions
Приложение требует следующие разрешения:
- `INTERNET` - для сетевых запросов
- `FOREGROUND_SERVICE` - для фонового мониторинга
- `POST_NOTIFICATIONS` - для отображения уведомлений (Android 13+)

## Архитектура

### Компоненты:
- **SimpleMcpClient** - HTTP клиент для работы с MCP сервером
- **BitcoinRepository** - репозиторий для работы с данными биткоина
- **BitcoinMonitorService** - foreground service для фонового мониторинга
- **AnalyzeBitcoinChangeUseCase** - use case для анализа изменений через AI
- **BitcoinMonitorViewModel** - ViewModel для управления состоянием UI
- **BitcoinMonitorScreen** - Compose UI для отображения данных

### База данных:
- **BitcoinPriceEntity** - entity для хранения цены, времени и анализа
- **BitcoinPriceDao** - DAO для работы с таблицей цен
- **AppDatabase** - Room database (версия 2)

## Как это работает

1. Пользователь нажимает "Запустить" на экране Bitcoin Monitor
2. Запускается `BitcoinMonitorService` как foreground service
3. Каждые 30 секунд:
   - Сервис запрашивает текущий курс BTC через MCP → CoinCap API
   - Если есть предыдущее значение, отправляет оба значения AI модели для анализа
   - Сохраняет курс и анализ в базу данных
   - Отправляет broadcast для обновления UI
   - Обновляет уведомление
4. UI отображает текущий курс, последний анализ и историю

## Проблемы и решения

### Если MCP сервер не запускается
```bash
cd /Users/aleksandrvasilev/McpServer
npm install
npm run http
```

### Если приложение не может подключиться к серверу
- Проверьте, что MCP сервер запущен: `curl http://localhost:3000/health`
- Для эмулятора используется адрес `10.0.2.2:3000`
- Для реального устройства - нужен IP компьютера в локальной сети

### Если анализ не работает
- Проверьте, что в `local.properties` указан корректный `OPENROUTER_API_KEY`
- Убедитесь, что модель `nex-agi/deepseek-v3.1-nex-n1:free` доступна в OpenRouter
