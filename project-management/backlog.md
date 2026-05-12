# Бэклог проекта

## MVP (единое Micronaut-приложение)

- [x] Единое Micronaut-приложение (backend + embedded frontend)
- [x] Embedded React Admin UI
- [x] Embedded Web Chat Widget
- [x] Runtime REST API
- [x] Runtime WebSocket API
- [x] Admin REST API
- [x] Storage SPI
- [x] File storage (single-node)
- [x] H2 storage
- [x] PostgreSQL storage
- [x] Переключение storage через `client-journey.storage.type`
- [x] Валидация графа маршрута (production-правила)
- [x] Runtime прохождение маршрута через единое ядро
- [x] Выбор пользователем от 1 до N услуг в RESULT-узле
- [x] Runtime endpoint подтверждения выбранных услуг
- [x] ServiceSelectionProcessor
- [x] VisitCreation SPI
- [x] VisitManager client (`ENTRYPOINT_WITH_PARAMETERS`)
- [x] Dry-run visit creation
- [x] Custom REST client skeleton
- [x] Telegram polling adapter skeleton
- [x] Kafka adapter skeleton
- [x] WebSocket adapter skeleton
- [x] Swagger/OpenAPI
- [x] Dockerfile единого приложения
- [x] docker-compose (app + postgres + kafka, без отдельного frontend-контейнера)
- [x] WEB GUI на русском и английском языках
- [x] Resource files локализации frontend (ru/en)
- [x] Frontend smoke/e2e (Playwright, Chromium + Firefox)
- [x] CI workflow для frontend smoke с публикацией артефактов

## Следующий релиз (после MVP)

### План next release (Sprint 8)
- [x] Security & access control hardening (OIDC/Keycloak + API boundaries)
- [x] Observability expansion (health/readiness/liveness + metrics)
- [x] Scenario transfer v2 (compat validation + dry-run report)

- [x] Полный набор channel adapters (WhatsApp/MAX/Facebook как официальные integration-ready skeleton)
- [x] Режим создания визитов: единый сервер или разные серверы по отделениям/регионам
- [x] Расширенный импорт/экспорт сценариев
- [x] Метрики, трассировка, health/readiness/liveness (расширенная operational dashboard)
- [x] Политики безопасности (OIDC/Keycloak, rate-limit, masking)
- [x] Архитектурные тесты на границы слоёв и отсутствие циклических зависимостей
- [x] Интеграционные тесты frontend↔runtime (с моками backend API)

## Текущий релизный контур (Sprint 9 — in progress)

### Цель
- Завершить integration-ready контур каналов и продвинуть production-hardening по эксплуатационным сценариям.

### Коммитнутый прогресс
- [x] Repo hygiene: удалены бинарные SVG-артефакты из git, оставлены только PlantUML-исходники.
- [x] Admin import: добавлен multipart endpoint `/api/admin/scenarios/import/file` с валидацией бинарных файлов и понятной ошибкой.
- [x] Observability/Security: добавлены `/api/system/dependencies`, dashboard alerts и batch-маскирование `/api/system/security/mask-batch`.
- [x] Channel adapters: Facebook/MAX skeleton расширены до проверки webhook-подписи (HMAC-SHA256) + unit-tests.
- [x] Channel adapters: WhatsApp skeleton расширен до проверки webhook-подписи (HMAC-SHA256) + unit-test.
- [x] Добавлен runbook подключения провайдеров: `docs/CHANNEL-PROVIDER-RUNBOOK.md`.
- [x] Observability: добавлена гибкая конфигурация логирования (package-level logger, отдельные appenders, ротация, лимиты объема и хранения).
- [x] Frontend e2e конфиг переведен на запуск через основной backend-сервер (Micronaut), static-режим оставлен как fallback.
- [x] Документация API и тестовые сценарии локализованы на русский (Swagger/OpenAPI summaries + Playwright test descriptions).
- [x] Утвержден контракт channel SPI (`DeliveryResult`, `IntegrationReadyChannelAdapter`, `WebhookSignatureSupport`).
- [x] Добавлены/обновлены unit-тесты по security, observability и runtime/admin контроллерам.
- [x] Добавлена общая точка входа WebGUI через `/` с навигацией в `/admin/` и `/chat/`.

### План Sprint 9
- [x] Channel adapters: завершить официальные integration-ready skeleton для WhatsApp/MAX/Facebook (конфиги, webhook verification, outbound payload mapping).
- [x] Channel adapters: добавить runbook по подключению провайдеров (env vars, signatures, retry semantics).
- [x] Security: добавлены negative-tests для rate-limit по endpoint-группам (правила/лимиты/default window).
- [x] Observability: расширить dashboard сценариями деградации зависимостей (visit provider unavailable, storage latency high).
- [x] Frontend: добавить e2e-сценарий branch-selection + visit-creation dry-run в Admin UI.
- [x] Frontend: Admin dry-run e2e (mocked `/visit-creation-settings/test`) добавлен.
- [x] Frontend: Root WebGUI smoke e2e для точки входа `/` и переходов в `/admin/`/`/chat/`.
- [x] Docs: README расширен и дополнен PlantUML-диаграммами (source + SVG).
- [x] Docs: синхронизировать FRONTEND-TESTING и release-notes по итогам sprint.


### Sprint 9 — execution checklist (next 5 working days)
- [x] Day 1: зафиксировать provider-specific config keys для WhatsApp/MAX/Facebook + примеры env.
- [x] Day 2: реализовать webhook signature validation flow и unit-tests в adapter-модулях.
- [x] Day 3: добавить negative-tests для rate-limit burst/path rules и обновить security docs.
- [x] Day 4: расширить dashboard метриками деградации (storage latency/provider health).
- [x] Day 5: провести smoke regression + обновить release-notes и sprint demo checklist.
