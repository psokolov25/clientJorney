# Runbook: подключение channel providers (Sprint 9)

## Цель

Описать минимальный operational baseline для подключения провайдеров каналов (WhatsApp/MAX/Facebook) на базе integration-ready skeleton адаптеров.

## 1) Конфигурация окружения

Рекомендуемый шаблон переменных (пример):

```bash
CHANNEL_PROVIDER=whatsapp
CHANNEL_WEBHOOK_SECRET=change-me
CHANNEL_API_BASE_URL=https://provider.example/api
CHANNEL_ACCESS_TOKEN=change-me
CHANNEL_TIMEOUT_MS=5000
CHANNEL_RETRY_MAX_ATTEMPTS=3
CHANNEL_RETRY_BACKOFF_MS=500
```

### Обязательные параметры
- `CHANNEL_PROVIDER`
- `CHANNEL_WEBHOOK_SECRET`
- `CHANNEL_API_BASE_URL`
- `CHANNEL_ACCESS_TOKEN`

### Рекомендуемые параметры
- `CHANNEL_TIMEOUT_MS`
- `CHANNEL_RETRY_MAX_ATTEMPTS`
- `CHANNEL_RETRY_BACKOFF_MS`

## 2) Проверка webhook подписи

1. Проверить, что входящий webhook содержит подпись в заголовке провайдера.
2. Нормализовать тело запроса (raw body, без модификации).
3. Сверить HMAC-подпись с `CHANNEL_WEBHOOK_SECRET`.
4. При несовпадении вернуть `401/403` и записать security-событие в `security.log`.

## 3) Outbound delivery

1. Сформировать provider payload из `ClientOutputMessage`.
2. Выполнить POST в `CHANNEL_API_BASE_URL` с `CHANNEL_ACCESS_TOKEN`.
3. Преобразовать ответ провайдера в `DeliveryResult`.
4. При ошибке применить retry policy (max attempts + backoff).

## 4) Retry semantics

- Повторять только retryable ошибки (5xx, timeout, connection reset).
- Не повторять 4xx (кроме 429, если есть `Retry-After`).
- Ограничить общее число попыток `CHANNEL_RETRY_MAX_ATTEMPTS`.

## 5) Наблюдаемость

Минимальные метрики/логи:
- количество отправок и ошибок по provider;
- latency outbound call;
- webhook signature failures;
- retry count и final delivery status.

## 6) Чеклист перед релизом

- [ ] Включена проверка webhook подписи
- [ ] Настроены timeout/retry
- [ ] Логи и метрики доступны
- [ ] Прогнан smoke test для inbound/outbound
- [ ] Проверена ротация логов (`security.log`, `audit.log`)
