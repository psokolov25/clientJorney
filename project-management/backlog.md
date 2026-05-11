# Бэклог проекта

## MVP (единое Micronaut-приложение)

- [ ] Единое Micronaut-приложение (backend + embedded frontend)
- [ ] Embedded React Admin UI
- [ ] Embedded Web Chat Widget
- [ ] Runtime REST API
- [ ] Runtime WebSocket API
- [ ] Admin REST API
- [ ] Storage SPI
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
- [ ] WEB GUI на русском и английском языках
- [ ] Resource files локализации frontend (ru/en)

## Следующий релиз (после MVP)

- [ ] Полный набор channel adapters (WhatsApp/MAX/Facebook как официальные integration-ready skeleton)
- [ ] Расширенный импорт/экспорт сценариев
- [ ] Метрики, трассировка, health/readiness/liveness
- [ ] Политики безопасности (OIDC/Keycloak, rate-limit, masking)
- [ ] Архитектурные тесты на границы слоёв и отсутствие циклических зависимостей
