# Release Notes

## 0.1.0-SNAPSHOT

### Добавлено
- Единое Micronaut-приложение с runtime/admin API, embedded Admin/Chat UI и WebSocket runtime.
- Frontend smoke/e2e набор на Playwright и CI workflow с публикацией `playwright-report/` и `test-results/` артефактов.
- Расширенные operational endpoints: `health`, `readiness`, `liveness`, `dashboard`, `metrics`, `trace`.
- Унифицированный API-формат ошибок через `ApiExceptionHandler` и `ApiErrorResponse`.
- Security hardening: rate-limit правила по endpoint-группам, policy endpoint, OIDC-ready конфигурация.
- Branch-aware настройки создания визитов и endpoint для branch selection config.
- PlantUML архитектурные диаграммы с хранением исходников (`.puml`) и визуализаций (`.svg`).

### Изменено
- Улучшены импорт/экспорт сценариев: строгая валидация и dry-run отчет.
- Обновлены channel SPI контракты для integration-ready адаптеров (`DeliveryResult`, `IntegrationReadyChannelAdapter`, `WebhookSignatureSupport`).
- README реструктурирован и дополнен разделами best-practice и архитектурными диаграммами.

### Известные ограничения
- WhatsApp/MAX/Facebook адаптеры остаются integration-ready skeleton и требуют завершения provider-specific runtime.
- Нужны дополнительные integration/e2e прогоны с реальными внешними провайдерами.

### Sprint 9 (текущий фокус)
- Завершение channel adapter readiness (webhook signatures, outbound mapping, provider runbook).
- Security negative-tests для rate-limit edge-cases.
- Расширение observability dashboard сигналами деградации зависимостей.


### Sprint 9 update
- Финализированы конфигурационные контракты для WhatsApp/MAX/Facebook skeleton adapters.
- Добавлены проверки webhook signature (HMAC-SHA256) и unit-тесты для WhatsApp/Facebook/MAX.
- Расширен dashboard сигналами деградации зависимостей и alert-полем.
- Добавлены `docker-compose.adapters-smoke.yml` и `docs/MULTI-BRANCH-CONFIG-TEMPLATE.yml`.
