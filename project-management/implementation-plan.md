# План реализации

## Этап 1. Архитектурный каркас
- [x] Зафиксировать границы модулей и SPI
- [x] Уточнить контракт Runtime сообщений
- [x] Уточнить контракт VisitCreation

## Этап 2. Backend MVP
- [x] Доработать доменную модель RESULT/service-selection
- [x] Реализовать Runtime endpoint `POST /api/runtime/sessions/{sessionId}/selected-services`
- [x] Реализовать ServiceSelectionProcessor
- [x] Расширить RouteValidationService

## Этап 3. Хранилища
- [x] Довести file-storage до структурированной директории `data/client-journey`
- [x] Добавить H2 storage skeleton + migrations
- [x] Добавить PostgreSQL storage skeleton + migrations

## Этап 4. Visit creation
- [x] VisitCreationOrchestrator
- [x] VisitManagerVisitCreationClient
- [x] CustomRestVisitCreationClient skeleton
- [x] DryRun/Mock implementations

## Этап 5. Frontend embedded
- [x] Модуль frontend build
- [x] Копирование assets в `client-journey-app/src/main/resources/public`
- [x] i18n (ru/en) resource-based
- [x] Playwright smoke-тесты + CI
- [x] UI-панель оперативной проверки backend (`/api/system/dashboard`) в Admin frontend

## Этап 6. Канальные адаптеры
- [x] REST/WebSocket
- [x] Kafka skeleton
- [x] Telegram polling skeleton
- [x] WhatsApp/MAX/Facebook stub adapters

## Этап 7. Баланс frontend/backend (текущий спринт)
- [x] Backend: сводный operational endpoint `GET /api/system/dashboard`
- [x] Frontend: кнопка загрузки operational-статуса в Admin UI
- [x] Backend: единый контракт ошибки runtime/admin API
- [x] Frontend: обработка структурированных API-ошибок в embedded chat/admin
- [x] Frontend: API-интеграционные e2e-тесты чата (Playwright, mocked runtime API)

- [x] Frontend: Admin operational panel e2e (mocked /api/system/dashboard)

- [x] Frontend: выбор отделения в начале клиентского пути до старта runtime-сессии
- [x] Frontend: регрессионный e2e-тест структурированных API-ошибок в Chat UI (Playwright, mocked runtime API)


## Этап 8. Следующий релиз (Sprint 8)

### Цель релиза
- Закрыть базовые production-риски по безопасности и наблюдаемости.
- Довести импорт/экспорт сценариев до операционно пригодного состояния.

### Scope (обязательный)
- [x] Security: API key + OIDC/Keycloak profile, унифицированные правила доступа к admin/runtime API.
- [x] Security: расширенный rate-limit (конфигурируемые лимиты по endpoint-группам).
- [x] Security: централизованное masking PII в логах runtime/admin.
- [x] Observability: health/readiness/liveness + расширенный dashboard payload.
- [x] Observability: базовые технические метрики (request count/error rate/latency buckets).
- [x] Scenario transfer: расширенный import/export (валидация совместимости версии, режим dry-run с отчетом issues).

### Scope (stretch)
- [ ] Channel adapters: integration-ready skeleton completion (WhatsApp/MAX/Facebook). *(вынесено в следующий спринт по решению PO/PM)*
- [x] Channel adapters: подготовлен integration-ready metadata contract (requiredConfigKeys + webhook signature capability) для WhatsApp/MAX/Facebook skeletons.

### Definition of Done
- [x] Для каждого пункта есть unit/integration tests.
- [x] Добавлены/обновлены API-контракты и документация (`FRONTEND-TESTING.md`/backend docs при необходимости).
- [x] Проверки CI проходят стабильно в двух последовательных запусках.

## Этап 9. Sprint 9 (текущий) — channel readiness + ops hardening

### Цель этапа
- Перевести оставшиеся канальные адаптеры из «заготовки» в полноценный integration-ready baseline и закрыть ключевые эксплуатационные риски.

### Scope (обязательный)
- [x] WhatsApp/MAX/Facebook: финализировать конфигурационные контракты и webhook signature verification flow.
- [x] Добавить адаптерные тесты на корректную обработку delivery статусов и ошибок провайдера.
- [x] Security: покрыть rate-limit edge-cases (burst + path-specific quotas).
- [x] Observability: дополнить operational dashboard индикаторами деградации внешних зависимостей.

### Scope (stretch)
- [x] Добавить smoke-интеграцию channel adapters через docker-compose профили с мок-провайдерами.
- [x] Добавить шаблоны production-конфигурации для multi-branch deployment.

### Definition of Done
- [x] Для каждого нового/измененного контракта есть unit-тесты.
- [x] Изменения отражены в README и release-notes.
- [x] Спринтовые задачи обновлены в backlog и roadmap.
