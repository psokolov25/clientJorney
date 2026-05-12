# Аудит покрытия бэклога (на 2026-05-11)

Цель документа: зафиксировать, какие фичи уже покрыты текущей кодовой базой, а какие остаются неохваченными и требуют реализации.

## 1) MVP: текущий статус покрытия

### Покрыто в коде
- Runtime REST API (`/api/runtime/...`) — реализован.
- Runtime WebSocket API — реализован базовый endpoint.
- Admin REST API (`/api/admin/scenarios`, `graph`) — реализован.
- Admin visit-creation settings API (`GET/PUT/POST test /api/admin/scenarios/{id}/visit-creation-settings`) — реализован baseline.
- VisitManager service catalog API (`GET /api/admin/visit-manager/services`) — реализован baseline skeleton.
- System observability API baseline (`/api/system/metrics`, `/api/system/trace`) — реализован.
- Scenario transfer API baseline (`GET /api/admin/scenarios/{id}/export`, `POST /api/admin/scenarios/import`) — реализован.
- Channel adapters baseline skeleton: WhatsApp/MAX/Facebook — реализованы.
- Storage SPI + file/h2/postgres + выбор через конфиг — реализовано.
- Выбор услуг пользователем и подтверждение выбранных услуг — реализовано в runtime API.
- VisitCreation SPI + DryRun + VisitManager/CustomRest skeleton — реализовано.
- Channel adapters skeleton (Telegram/Kafka/WebSocket) — реализованы.
- Dockerfile + docker-compose — присутствуют.

### Неохваченные (подтвержденные гэпы)
1. **Embedded React Admin UI (production-grade)**
   - Сейчас есть только baseline static embedded page, без React build pipeline.
2. **Embedded Web Chat Widget (production-grade)**
   - Сейчас есть только baseline static embedded page, без интеграции с полноценным widget runtime.
3. **WEB GUI на русском и английском языках (полный охват)**
   - Добавлен baseline i18n для embedded страниц; не покрыты все будущие UI-экраны.

## 2) После MVP: статус

### Неохваченные
- Полный набор channel adapters (WhatsApp/MAX/Facebook integration-ready skeleton).
- Расширенный import/export сценариев.
- Метрики, трассировка (readiness/liveness probes добавлены в baseline, метрики и трассировка остаются неохваченными).
- Security policies (OIDC/Keycloak, rate-limit, masking).
- Полный набор архитектурных тестов на границы всех модулей и отсутствие циклов между модулями.

## 3) Приоритет следующей итерации

Предлагаемый порядок реализации:
1. Embedded frontend delivery (Admin UI + Chat Widget в `public/`).
2. Frontend i18n (ru/en) resource-based.
3. Observability (health/readiness/liveness + базовые метрики).
4. Security baseline (authn/authz + rate limiting).

## 4) Definition of Done для пункта "frontend i18n"

- Есть ru/en resource-файлы.
- В UI отсутствуют hardcoded строки.
- Есть runtime переключение локали.
- Добавлены smoke/e2e тесты на обе локали.


## 5) Что ещё не охвачено (приоритетный остаток)

- Production-ready embedded React Admin UI (build pipeline, сборка, интеграция в public).
- Production-ready Web Chat Widget (stateful runtime UI + интеграция с runtime websocket/rest).
- Полный i18n для всех будущих экранов и сценариев UI (не только baseline-страницы).
- Observability beyond probes: метрики и distributed tracing.
- Security policies: OIDC/Keycloak, rate limit, masking PII.
- Полноценные integration-ready adapters: WhatsApp/MAX/Facebook.
- Расширенный import/export сценариев.
