# Дорожная карта

## Фаза 0 — Редактор клиентского пути (немедленный фокус)
- Замена текущего canvas-демо на полноценный graph editor UX
- Ветвящиеся цепочки QUESTION → answer options → QUESTION/RESULT без ручного JSON
- Валидации графа и предпросмотр маршрута до публикации
- Технологический baseline: React Flow (рекомендация после исследования)

### Статус Фазы 0 (обновлено: 2026-05-13)
- React Flow editor выбран и включен как основной admin-экран.
- Реализованы кастомные node/edge представления (`QuestionNode`, `ResultNode`, `AnswerEdge`) и расширенная модель узлов (`SERVICE_SELECTION`, `API_CAPTURE`, `GROOVY_CAPTURE`).
- Реализованы quick-actions построения ветвлений, dagre auto-layout и side panel редактирования.
- Реализованы save/load через ScenarioGraph API + import/export JSON.
- Добавлена семантическая валидация графа (reachability, duplicates, path-to-RESULT) с выводом проблем в UI.
- Добавлен frontend smoke для сборки ветки и import/export roundtrip.

### Что осталось, чтобы формально закрыть Фазу 0
- Завершить autosave до production-уровня: локальный debounce есть, остаётся optimistic locking / версия при сохранении.
- Довести undo/redo до production-уровня (базовый snapshot undo/redo уже есть, нужна UX-шлифовка).

## Фаза 1 — MVP (Q2–Q3 2026)
- Единое Micronaut-приложение (embedded frontend)
- Admin REST API + Runtime REST API
- Storage SPI + file/h2/postgres модули (базовый уровень)
- ServiceSelectionProcessor и поддержка SERVICE_SELECTION
- VisitCreation SPI + dry-run/mock + VisitManager ENTRYPOINT_WITH_PARAMETERS
- Базовый React Admin UI и Widget skeleton

### Статус Фазы 1 (обновлено: 2026-05-13)
- Запущен Phase A1-track для Telegram skeleton:
  - добавлен fail-fast validateConfig policy;
  - покрыты unit-негативные кейсы конфигурации;
  - следующий шаг: закрытие config profile/runbook + переход к A2 runtime mapping.
- Фаза A2/A3 частично реализована в skeleton:
  - inbound mapping типов `COMMAND`/`CALLBACK`/`ATTACHMENT`;
  - outbound options (`parseMode`, `replyMarkup`);
  - deduplication по `update_id` и базовая retry-семантика с `RETRY_EXHAUSTED`.

## Фаза 2 — Интеграции и эксплуатация
- Kafka adapter
- Telegram polling adapter
- WebSocket runtime
- Наблюдаемость (метрики, health, correlation-id)
- Security baseline (roles, CORS, rate limiting)

### Статус Фазы 2 (план-коррекция)
- До старта активной реализации зафиксировать единый шаблон phase-gates (Code/Contract/Test/Ops) для каждого интеграционного адаптера.

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
