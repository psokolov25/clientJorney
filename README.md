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
