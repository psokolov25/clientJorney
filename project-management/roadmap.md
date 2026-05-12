# Дорожная карта

## Фаза 0 — Редактор клиентского пути (немедленный фокус)
- Замена текущего canvas-демо на полноценный graph editor UX
- Ветвящиеся цепочки QUESTION → answer options → QUESTION/RESULT без ручного JSON
- Валидации графа и предпросмотр маршрута до публикации
- Технологический baseline: React Flow (рекомендация после исследования)

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

## Фаза 4 — Integration-ready каналы и операционная устойчивость (Q4 2026)
- Завершение официальных adapter-baseline для WhatsApp/MAX/Facebook
- Подписи webhook и политика ретраев/идемпотентности
- Расширенная диагностика деградаций внешних зависимостей
- E2E-проверки branch-aware запусков и visit-creation dry-run

- Добавлены шаблоны `docker-compose.adapters-smoke.yml` и `docs/MULTI-BRANCH-CONFIG-TEMPLATE.yml` для smoke и multi-branch baseline

- Детализированный план развития skeleton-компонентов: `project-management/flesh-out-plan.md` (приоритет: Telegram → Web Chat (white-label) → остальные каналы)

- Сравнение библиотек для graph editor: `project-management/graph-editor-libraries-review.md`
