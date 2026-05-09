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
- [ ] H2 storage
- [ ] PostgreSQL storage
- [ ] Переключение storage через `client-journey.storage.type`
- [ ] Валидация графа маршрута (production-правила)
- [ ] Runtime прохождение маршрута через единое ядро
- [ ] Выбор пользователем от 1 до N услуг в RESULT-узле
- [ ] Runtime endpoint подтверждения выбранных услуг
- [ ] ServiceSelectionProcessor
- [ ] VisitCreation SPI
- [ ] VisitManager client (`ENTRYPOINT_WITH_PARAMETERS`)
- [ ] Dry-run visit creation
- [ ] Custom REST client skeleton
- [ ] Telegram polling adapter skeleton
- [ ] Kafka adapter skeleton
- [ ] WebSocket adapter skeleton
- [ ] Swagger/OpenAPI
- [ ] Dockerfile единого приложения
- [ ] docker-compose (app + postgres + kafka, без отдельного frontend-контейнера)
- [ ] WEB GUI на русском и английском языках
- [ ] Resource files локализации frontend (ru/en)

## Следующий релиз (после MVP)

- [ ] Полный набор channel adapters (WhatsApp/MAX/Facebook как официальные integration-ready skeleton)
- [ ] Расширенный импорт/экспорт сценариев
- [ ] Метрики, трассировка, health/readiness/liveness
- [ ] Политики безопасности (OIDC/Keycloak, rate-limit, masking)
- [ ] Архитектурные тесты на границы слоёв и отсутствие циклических зависимостей
