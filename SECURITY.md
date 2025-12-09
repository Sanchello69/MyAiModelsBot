# Безопасность API ключа

## ✅ Что уже сделано

### 1. API ключ защищен от коммита в Git

**Механизм защиты:**
- Ключ хранится в `local.properties`
- Файл `local.properties` добавлен в `.gitignore`
- Ключ НЕ будет закоммичен при `git add .` или `git commit`

### 2. BuildConfig для безопасного доступа

**Как работает:**
```
local.properties (игнорируется Git)
        ↓
build.gradle.kts (читает ключ)
        ↓
BuildConfig.OPENROUTER_API_KEY
        ↓
NetworkModule (использует ключ)
```

### 3. Проверка защиты

Убедитесь, что `local.properties` в `.gitignore`:
```bash
cat .gitignore | grep local.properties
```

Должно вывести:
```
/local.properties
local.properties
```

## ⚠️ Важные правила безопасности

### ❌ НЕ делайте:
1. Не коммитьте `local.properties` в Git
2. Не храните ключ в коде напрямую
3. Не делитесь скриншотами с ключом
4. Не публикуйте ключ в issues/pull requests

### ✅ Делайте:
1. Храните ключ в `local.properties`
2. Добавьте `local.properties` в `.gitignore`
3. Используйте переменные окружения для CI/CD
4. Ротируйте ключи периодически

## Для CI/CD

### GitHub Actions пример:

```yaml
name: Build

on: [push]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2

      - name: Create local.properties
        run: |
          echo "OPENROUTER_API_KEY=${{ secrets.OPENROUTER_API_KEY }}" >> local.properties

      - name: Build
        run: ./gradlew assembleRelease
```

Добавьте секрет в GitHub:
Settings → Secrets → New repository secret
- Name: `OPENROUTER_API_KEY`
- Value: ваш ключ

## Для production приложений

### Дополнительные меры безопасности:

1. **Backend Proxy**
   - API ключ на сервере, не в приложении
   - Мобильное приложение → Ваш сервер → OpenRouter

2. **Android Keystore** (для очень чувствительных данных)
   - Зашифрованное хранилище Android
   - Защита от извлечения ключа

3. **ProGuard/R8**
   - Обфускация кода
   - Усложняет reverse engineering

4. **Certificate Pinning**
   - Защита от MITM атак
   - Проверка SSL сертификата

## Текущее состояние

**Ваш API ключ:**
- ✅ Хранится в `local.properties`
- ✅ Защищен `.gitignore`
- ✅ Используется через `BuildConfig`
- ✅ Не попадет в Git при коммите

**Проверить защиту:**
```bash
# Проверить, что local.properties в gitignore
git check-ignore local.properties
# Должно вывести: local.properties

# Проверить статус файлов
git status
# local.properties НЕ должен быть в списке изменений
```

## Что делать при утечке ключа

Если ключ случайно попал в публичный репозиторий:

1. **Немедленно** удалите ключ на OpenRouter
2. Создайте новый ключ
3. Обновите `local.properties`
4. Удалите ключ из истории Git:
   ```bash
   git filter-branch --force --index-filter \
     "git rm --cached --ignore-unmatch local.properties" \
     --prune-empty --tag-name-filter cat -- --all
   ```
5. Force push (если уже запушили)

## Дополнительные ресурсы

- [OWASP Mobile Security](https://owasp.org/www-project-mobile-security/)
- [Android Security Best Practices](https://developer.android.com/privacy-and-security/security-best-practices)
- [OpenRouter Security](https://openrouter.ai/docs/security)
