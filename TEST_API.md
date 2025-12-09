# Тест API ключа

Ваш API ключ в Base64: `MDE5YWRiMzEtN2I3OS03NGQ4LWEyM2YtM2RmY2U3OTk4ZDk2Ojc3ZjUyMzlmLTFmYjMtNGM3NS04MTM4LTljOWQwMzkyMzFjYw==`

## Проверка через curl:

```bash
curl https://openrouter.ai/api/v1/chat/completions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer MDE5YWRiMzEtN2I3OS03NGQ4LWEyM2YtM2RmY2U3OTk4ZDk2Ojc3ZjUyMzlmLTFmYjMtNGM3NS04MTM4LTljOWQwMzkyMzFjYw==" \
  -H "HTTP-Referer: https://myaimodelsbot.app" \
  -H "X-Title: AI Models Bot" \
  -d '{
    "model": "tngtech/deepseek-r1t2-chimera:free",
    "messages": [
      {"role": "user", "content": "Say hello"}
    ]
  }'
```

## Возможные проблемы:

1. **Ключ в неправильном формате** - OpenRouter может ожидать декодированный ключ
2. **Отсутствуют обязательные headers** - HTTP-Referer и X-Title
3. **Неправильный endpoint** - проверить URL

## Решение:

Декодируйте Base64 ключ если нужно:
```bash
echo "MDE5YWRiMzEtN2I3OS03NGQ4LWEyM2YtM2RmY2U3OTk4ZDk2Ojc3ZjUyMzlmLTFmYjMtNGM3NS04MTM4LTljOWQwMzkyMzFjYw==" | base64 -d
```

Результат: `019adb31-7b79-74d8-a23f-3dfce7998d96:77f5239f-1fb3-4c75-8138-9c9d039231cc`

Используйте этот декодированный ключ в Authorization header.
