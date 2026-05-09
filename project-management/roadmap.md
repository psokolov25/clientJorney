# Дорожная карта

## Фаза 1 — MVP (Q2–Q3 2026)
- Единое Micronaut-приложение (embedded frontend)
- Admin REST API + Runtime REST API
- Storage SPI + file/h2/postgres модули (базовый уровень)
- ServiceSelectionProcessor и поддержка SERVICE_SELECTION
- VisitCreation SPI + dry-run/mock + VisitManager ENTRYPOINT_WITH_PARAMETERS
- Базовый React Admin UI и Widget skeleton

## Фаза 2 — Интеграции и эксплуатация
- Kafka adapter
- Telegram polling adapter
- WebSocket runtime
- Наблюдаемость (метрики, health, correlation-id)
- Security baseline (roles, CORS, rate limiting)

## Фаза 3 — Production hardening
- Полноценные миграции и индексы
- Продвинутые тесты (integration/e2e)
- Документация по деплою и операционным процедурам
