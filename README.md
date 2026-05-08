# Client Journey Platform

Промышленный стартовый проект для визуального конструирования клиентских путей/опросников, интегрируемых с СУО.

## Архитектурное решение

Границы системы: единое Micronaut-приложение предоставляет Admin UI, Web Chat Widget, REST runtime/admin API, WebSocket endpoint, channel adapters, storage implementations и VisitCreation clients. Внешние системы: VisitManager REST API, другие СУО через `VisitCreationClient`, Kafka, Telegram Bot API polling, официальные WhatsApp/MAX/Facebook-провайдеры.

Главный принцип: маршрутизация не реализуется в каналах. Все каналы преобразуют вход в `ClientInputMessage`, вызывают `ScenarioEngine` и возвращают `ClientOutputMessage`.

## Модули

```text
client-journey-domain
client-journey-core
client-journey-storage-spi
client-journey-storage-file
client-journey-storage-h2
client-journey-storage-postgres
client-journey-channel-*
client-journey-visit-spi
client-journey-visit-visitmanager
client-journey-visit-custom-rest
client-journey-visit-mock
client-journey-frontend
client-journey-app
```

## Embedded frontend

React + TypeScript используется как build-module. В production нет отдельного frontend-сервера. Vite собирает:
- `target/dist/admin`;
- `target/dist/widget`.

`client-journey-app` копирует assets в `classpath:public/admin` и `classpath:public/widget`.

Production endpoints:

```text
/admin/**
/widget/client-journey-widget.js
/widget/client-journey-widget.css
/api/**
/ws/runtime
/swagger-ui/**
```

## Storage architecture

`ScenarioRepository`, `ScenarioGraphRepository`, `ConversationSessionRepository`, `ConversationAnswerRepository`, `VisitCreationAttemptRepository`, `VisitCreationSettingsRepository` находятся в SPI.

Реализации:
- file storage: рабочая MVP-реализация, JSON, atomic write, backup-on-write, corrupted directory, single-node;
- H2: JDBC/JSON repository-реализации, Flyway migration, PostgreSQL compatibility mode, local/demo/test;
- PostgreSQL: JDBC/JSONB repository-реализации, Flyway migration, production-профиль.

## Visit creation SPI

`VisitCreationOrchestrator` выполняет:
1. загрузку `VisitCreationSettings`;
2. сбор `VisitCreationRequest`;
3. выбор `VisitCreationClient`;
4. retry loop;
5. сохранение `VisitCreationAttempt`;
6. возврат `VisitCreationResult`.

Клиенты:
- `VisitManagerVisitCreationClient`;
- `CustomRestVisitCreationClient`;
- `MockVisitCreationClient`;
- `DryRunVisitCreationClient`.

## VisitManager integration

MVP реализует основной каноничный режим:

```http
POST /entrypoint/branches/{branchId}/entry-points/{entryPointId}/visits/parameters?printTicket={printTicket}&segmentationRuleId={segmentationRuleId}
```

Body:

```json
{
  "serviceIds": ["c3916e7f-7bea-4490-b9d1-0d4064adbe8b"],
  "parameters": {
    "clientJourney.sessionId": "uuid",
    "clientJourney.scenarioCode": "medical-registration",
    "clientJourney.channel": "WEB_CHAT"
  }
}
```

Остальные каноничные режимы заложены в enum/config и должны быть добавлены без изменения ядра:
- `ENTRYPOINT_SERVICE_IDS_ONLY`;
- `RECEPTION_PRINTER_WITH_PARAMETERS`;
- `VIRTUAL_VISIT`;
- `CREATE_THEN_UPDATE_PARAMETERS`.

## Runtime REST API

```http
POST /api/runtime/scenarios/{scenarioCode}/sessions
POST /api/runtime/sessions/{sessionId}/answers
```

## Admin REST API

```http
GET    /api/admin/scenarios
POST   /api/admin/scenarios
GET    /api/admin/scenarios/{id}
PUT    /api/admin/scenarios/{id}
DELETE /api/admin/scenarios/{id}
GET    /api/admin/scenarios/{id}/graph
PUT    /api/admin/scenarios/{id}/graph
GET    /api/admin/scenarios/{id}/visit-creation-settings
PUT    /api/admin/scenarios/{id}/visit-creation-settings
POST   /api/admin/scenarios/{id}/visit-creation-settings/test
POST   /api/admin/scenarios/{id}/validate
POST   /api/admin/scenarios/{id}/publish
POST   /api/admin/scenarios/{id}/archive
POST   /api/admin/scenarios/{id}/clone-version
POST   /api/admin/scenarios/import
GET    /api/admin/scenarios/{id}/export
```

## Быстрый старт

```bash
./mvnw clean package
java -Dmicronaut.environments=file -jar client-journey-app/target/client-journey-app.jar
```

Windows:

```bat
mvnw.cmd clean package
java -Dmicronaut.environments=file -jar client-journey-app\target\client-journey-app.jar
```

## REST пример

```bash
curl -X POST http://localhost:8080/api/admin/scenarios \
  -H "Content-Type: application/json" \
  -d "{\"code\":\"medical-registration\",\"name\":\"Регистрация в клинике\"}"
```

```bash
curl -X POST http://localhost:8080/api/runtime/scenarios/medical-registration/sessions \
  -H "Content-Type: application/json" \
  -d "{\"channel\":\"REST\",\"externalUserId\":\"client-123\",\"metadata\":{\"source\":\"test\"}}"
```

```bash
curl -X POST http://localhost:8080/api/runtime/sessions/{sessionId}/answers \
  -H "Content-Type: application/json" \
  -d "{\"answerCode\":\"doctor\",\"answerValue\":\"doctor\"}"
```

## Web widget

```html
<script src="https://host/widget/client-journey-widget.js"></script>
<script>
  ClientJourneyWidget.init({
    scenarioCode: "medical-registration",
    apiBaseUrl: "https://host/api",
    theme: {
      primaryColor: "#0057B8",
      accentColor: "#00AEEF",
      title: "Электронная очередь"
    }
  });
</script>
```

## Риски и ограничения MVP

1. File storage — рабочий single-node режим.
2. H2/PostgreSQL реализованы через JDBC CRUD; для большой нагрузки нужно добавить миграционные тесты на реальном PostgreSQL и pool tuning.
3. Kafka/Telegram/WebSocket/WhatsApp/MAX/Facebook — adapter skeletons; Telegram polling и Kafka consumer/producer нужно довести до промышленной обработки update/message offsets.
4. VisitManager реализует каноничные режимы `ENTRYPOINT_WITH_PARAMETERS`, `ENTRYPOINT_SERVICE_IDS_ONLY`, `CREATE_THEN_UPDATE_PARAMETERS`, `RECEPTION_PRINTER_WITH_PARAMETERS`, `VIRTUAL_VISIT`. Основной режим по умолчанию — `ENTRYPOINT_WITH_PARAMETERS`.
5. WhatsApp/MAX/Facebook не используют неофициальные API.
6. Maven wrapper в архиве — shim на установленный Maven; перед production нужно заменить на официальный wrapper.


## Второй этап текущего архива

В архив добавлена практическая доработка MVP:

- реальные JDBC repository для H2 и PostgreSQL;
- отдельные Flyway migration locations для H2/common и PostgreSQL/JSONB;
- расширенная валидация графа: duplicate ids, недостижимые узлы, тупики, отсутствие выхода в RESULT;
- `QuestionResolver`, `AnswerProcessor`, `ConversationSessionService`;
- расширенный `VisitParameterMapper`: `FLAT_PARAMETERS`, `COMPACT_JSON_PARAMETER`, mask email/phone, truncate/reject/store-reference-only;
- `VisitManagerVisitCreationClient` с поддержкой всех каноничных режимов из конфигурации;
- `VisitManagerServiceCatalogClient` и endpoint `GET /api/admin/visit-manager/services`;
- базовый `CustomRestVisitCreationClient` с HTTP method, headers, query parameters, bearer/basic/staticHeader auth, payload template и простым `$.field` response mapping;
- Maven dependencyManagement для внутренних модулей;
- shade packaging единого runnable jar;
- закреплены frontend-зависимости вместо `latest`;
- добавлены unit-test заготовки для graph validation и VisitManager parameter mapper.

### Каталог услуг VisitManager

```bash
curl "http://localhost:8080/api/admin/visit-manager/services?baseUrl=http://visitmanager:8080&branchId=37493d1c-8282-4417-a729-dceac1f3e2b4&scope=all"
```

`scope=all` используется для настройки сценариев, `scope=available` — для выбора только доступных услуг.

### PostgreSQL profile

```bash
java -Dmicronaut.environments=postgres -jar client-journey-app/target/client-journey-app.jar
```

PostgreSQL migration использует JSONB для graph, settings, attempts, channel messages и widget themes.

## План работ (roadmap реализации)

### Sprint 0 — Baseline и контроль архитектурных ограничений (1–2 дня)
1. Зафиксировать архитектурные decision records (ADR):
   - embedded frontend only;
   - единое ядро ScenarioEngine;
   - VisitCreationClient SPI;
   - storage switch via Micronaut `@Requires`.
2. Подтвердить boundaries и anti-corruption layer для внешних систем (VisitManager/другие СУО).
3. Настроить quality gates:
   - Checkstyle/SpotBugs/PMD;
   - unit/integration test profile;
   - архитектурные тесты на отсутствие циклических зависимостей.

### Sprint 1 — Каркас multi-module + единый runnable app (3–5 дней)
1. Создать/доработать Maven multi-module структуру.
2. Настроить `dependencyManagement`, версии плагинов, BOM.
3. Подключить frontend build-module (React + TypeScript + Vite) и копирование статики в `client-journey-app`.
4. Настроить упаковку single runnable jar (shade или аналог).
5. Подготовить `application.yml`, `application-file.yml`, `application-h2.yml`, `application-postgres.yml`.

**Definition of Done:** `./mvnw clean package` собирает backend + frontend и формирует единый артефакт.

### Sprint 2 — Domain/Core + Runtime MVP (5–7 дней)
1. Доменная модель: `Scenario`, `ScenarioGraph`, `Node`, `QuestionNode`, `ResultNode`, `ServiceRef`, `ConversationSession`, `ConversationAnswer`.
2. Ядро:
   - `ScenarioEngine`;
   - `QuestionResolver`;
   - `AnswerProcessor`;
   - `ConversationSessionService`.
3. Runtime API:
   - `POST /api/runtime/scenarios/{scenarioCode}/sessions`;
   - `POST /api/runtime/sessions/{sessionId}/answers`.
4. Единые DTO `ClientInputMessage`/`ClientOutputMessage`.

**Definition of Done:** REST-клиент проходит сценарий до `RESULT`, формируется корректный финальный ответ.

### Sprint 3 — RouteValidation + Admin API (5–7 дней)
1. Реализовать `RouteValidationService` с полным списком проверок графа.
2. Реализовать CRUD сценариев и графа.
3. Реализовать publish/archive/clone-version + запрет публикации невалидного графа.
4. Реализовать import/export JSON.
5. Подключить OpenAPI/Swagger.

**Definition of Done:** админ может создать/провалидировать/опубликовать сценарий через API.

### Sprint 4 — Storage SPI + File storage (4–6 дней)
1. Выделить SPI-репозитории.
2. Реализовать file storage:
   - atomic write;
   - backup-on-write;
   - обработка corrupted JSON;
   - single-node safety constraints.
3. Покрыть тестами file repositories.

**Definition of Done:** приложение стабильно работает в профиле `file` на полном runtime/admin флоу.

### Sprint 5 — H2/PostgreSQL + Flyway (5–8 дней)
1. Реализовать JDBC repositories для H2/PostgreSQL.
2. Подготовить миграции:
   - `db/migration/common` (H2);
   - `db/migration/postgres` (JSONB).
3. Добавить индексы и проверить планы выполнения для ключевых запросов.
4. Интеграционные тесты с H2 и PostgreSQL.

**Definition of Done:** профили `h2` и `postgres` проходят тестовый набор и запускаются без ручных правок.

### Sprint 6 — Visit Creation Subdomain (6–9 дней)
1. Реализовать `VisitCreationOrchestrator`.
2. Реализовать `VisitManagerVisitCreationClient` с каноничными режимами:
   - `ENTRYPOINT_WITH_PARAMETERS` (default);
   - `ENTRYPOINT_SERVICE_IDS_ONLY`;
   - `CREATE_THEN_UPDATE_PARAMETERS`;
   - `RECEPTION_PRINTER_WITH_PARAMETERS`;
   - `VIRTUAL_VISIT`.
3. Реализовать `VisitParameterMapper`:
   - `FLAT_PARAMETERS`;
   - `COMPACT_JSON_PARAMETER`;
   - PII masking;
   - long-value strategies.
4. Реализовать `CustomRestVisitCreationClient` (generic, конфигурируемый).
5. Реализовать `Mock/DryRun` клиенты.

**Definition of Done:** финал сценария создаёт визит или возвращает контролируемый результат dry-run/error.

### Sprint 7 — Channel adapters (skeleton → production-ready path) (5–8 дней)
1. WebSocket runtime skeleton `/ws/runtime`.
2. Kafka adapter skeleton (input/output/events/DLQ + idempotency contract).
3. Telegram polling skeleton (/start, /cancel, /restart).
4. WhatsApp/MAX/Facebook — только официально-совместимые адаптерные точки + mock.

**Definition of Done:** каналы подключаются к одному ядру без дублирования бизнес-логики.

### Sprint 8 — Frontend Admin + Widget MVP (7–12 дней)
1. Admin UI:
   - список сценариев;
   - карточка сценария;
   - graph editor skeleton (React Flow);
   - validation panel;
   - visit-creation settings.
2. Widget MVP:
   - bootstrap script;
   - чат-окно;
   - вопросы/ответы;
   - отображение результата/ошибок.
3. Встроенная отдача статики через Micronaut `/admin/**` и `/widget/**`.

**Definition of Done:** админ через UI создаёт маршрут, клиент проходит его через widget.

### Sprint 9 — Observability, Security, Docs, Delivery (5–8 дней)
1. Structured logging + correlationId.
2. Micrometer метрики, health/readiness/liveness.
3. Security baseline:
   - роли ADMIN/DESIGNER/VIEWER/RUNTIME_CLIENT;
   - CORS для widget;
   - rate limiting runtime API;
   - masking токенов/sid/PII.
4. Dockerfile и docker-compose (без frontend-контейнера).
5. Полный пакет документации + PlantUML диаграммы.

**Definition of Done:** воспроизводимый запуск в Docker, документация покрывает разработку и эксплуатацию.

### Критический путь и зависимости
1. Сначала: multi-module build + embedded frontend pipeline.
2. Затем: domain/core/runtime/admin validation.
3. Затем: storage + visit creation.
4. После: channels + UI/widget hardening.
5. Финал: observability/security/perf + docs.

### Риски и меры
- **Риск:** расползание логики по адаптерам.  
  **Мера:** контракты `ClientInputMessage`/`ClientOutputMessage`, code owners для core.
- **Риск:** несовместимости H2/PostgreSQL JSON/JSONB.  
  **Мера:** двойной набор integration tests и отдельные migration locations.
- **Риск:** vendor lock-in на VisitManager.  
  **Мера:** обязательный путь через `VisitCreationClient` SPI, contract tests для custom clients.
- **Риск:** деградация UX редактора графа.  
  **Мера:** frontend e2e smoke + snapshot tests на ключевые сценарии.
