# План реализации

## Этап 1. Архитектурный каркас
- [ ] Зафиксировать границы модулей и SPI
- [ ] Уточнить контракт Runtime сообщений
- [ ] Уточнить контракт VisitCreation

## Этап 2. Backend MVP
- [ ] Доработать доменную модель RESULT/service-selection
- [ ] Реализовать Runtime endpoint `POST /api/runtime/sessions/{sessionId}/selected-services`
- [ ] Реализовать ServiceSelectionProcessor
- [ ] Расширить RouteValidationService

## Этап 3. Хранилища
- [ ] Довести file-storage до структурированной директории `data/client-journey`
- [ ] Добавить H2 storage skeleton + migrations
- [ ] Добавить PostgreSQL storage skeleton + migrations

## Этап 4. Visit creation
- [ ] VisitCreationOrchestrator
- [ ] VisitManagerVisitCreationClient
- [ ] CustomRestVisitCreationClient skeleton
- [ ] DryRun/Mock implementations

## Этап 5. Frontend embedded
- [ ] Модуль frontend build
- [ ] Копирование assets в `client-journey-app/src/main/resources/public`
- [ ] i18n (ru/en) resource-based

## Этап 6. Канальные адаптеры
- [ ] REST/WebSocket
- [ ] Kafka skeleton
- [ ] Telegram polling skeleton
- [ ] WhatsApp/MAX/Facebook stub adapters
