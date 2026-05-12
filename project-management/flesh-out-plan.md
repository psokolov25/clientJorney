# План «наращивания мясом» скелетного кода

## Контекст
Проект уже содержит стабильный каркас: доменная модель, runtime/admin API, storage/visit SPI, frontend-заготовки и skeleton-адаптеры каналов. Цель плана — поэтапно довести skeleton-компоненты до production-ready состояния, сохраняя приоритет работ: **сначала редактор клиентского пути, затем Telegram → Web-чат → остальные каналы**.

---

## Фаза 0 (срочный приоритет): Редактор клиентского пути (graph authoring UX)

### 0.1. Переход с canvas-демо на graph editor framework
- [x] Выбрать библиотеку для production-редактора: **React Flow** (решение принято 2026-05-12, см. `project-management/graph-editor-libraries-review.md`).
- [ ] Полностью перевести admin-редактор на React Flow с drag/drop, zoom/pan, selection, edge handles (без legacy ветки).



### 0.1.a. React Flow implementation track (принято в работу)
- [x] Переключить основной `admin/index.html` на React Flow как целевой экран релиза (без feature-flag и без legacy-дублирования).
- [ ] Реализовать `QuestionNode` и `ResultNode` как кастомные React Flow nodes с inline-preview текста и счётчиком вариантов.
- [ ] Реализовать `AnswerEdge` с меткой `answerCode:answerLabel` и кликабельным переходом к редактированию.
- [ ] Подключить side panel к выбранному node/edge (single source of truth: editor store).
- [ ] Добавить auto-layout для первичной читабельности (dagre/elk как отдельный adapter-слой).
- [ ] Подготовить migration utility: convert `nodes+answers[]` <-> React Flow elements.

### 0.2. UX «собрать цепочку без боли»
- [x] Быстрый сценарий: `QUESTION` → набор `answerCode` → переход в следующий `QUESTION`/`RESULT` (реализовано в Admin UI, май 2026).
- [ ] Side panel редактирования узла/ребра (текст вопроса, варианты ответов, условия перехода).
- [ ] Горячие действия: «добавить следующий вопрос», «добавить финальный результат», «добавить шаг выбора услуг».

### 0.3. Семантическая валидация графа
- [ ] Проверка недостижимых узлов, дубликатов `answerCode` в пределах узла, отсутствия путей до `RESULT` (частично: проверка дубликатов и ссылок на несуществующие узлы есть, reachability/to-RESULT pending).
- [ ] Предпросмотр клиентского пути (dry-run прямо из редактора).
- [ ] Блокировка публикации при критических ошибках графа.

### 0.4. Интеграция с backend
- [ ] Прозрачный import/export в `ScenarioGraph` API без ручного редактирования JSON (частично: UI import/export JSON реализован, прямой API-пайплайн — следующий шаг).
- [ ] Автосохранение черновика и контроль конкурентного редактирования.



### 0.5. Следующие шаги (в работу немедленно)
- [ ] Подключить сохранение/загрузку графа через `ScenarioGraph` backend API (persist + reload).
- [ ] Добавить валидацию reachability: стартовый узел, недостижимые узлы, обязательный путь до `RESULT`.
- [ ] Ввести явный тип узла «выбор услуг» (service-selection) и соответствующие правила маршрутизации.
- [ ] Добавить e2e smoke для Admin UI: построение ветки `QUESTION -> QUESTION -> RESULT` + import/export roundtrip.
- [ ] Подготовить migration-note: как нормализовать ранние JSON-черновики в актуальную модель `answers[]`.

### Definition of Done (Graph Editor)
- [ ] Можно собрать полный путь: вопрос → варианты → ветвления → выбор услуг/результат без ручного JSON (частично: UI-цепочка работает, требуется сохранение в backend API).
- [ ] Ошибки маршрута показываются в UI до сохранения.
- [ ] Есть e2e-тест «ветвящийся сценарий до финального RESULT».

---

## Фаза A (приоритет №1 после редактора): Telegram-канал — от polling skeleton к production adapter

### A1. Контракты и конфигурация
- [ ] Уточнить и зафиксировать обязательные `requiredConfigKeys` для Telegram (token, webhook/polling mode, timeout, retry policy, allowed updates).
- [ ] Ввести валидацию конфигурации при старте адаптера и fail-fast ошибки с понятными кодами.
- [ ] Добавить профиль запуска Telegram в `application*.yml` и отдельный runbook с примерами env-переменных.

### A2. Runtime-поток сообщений
- [ ] Реализовать inbound mapping Telegram update → `ClientInputMessage` (text, command, callback, attachments metadata).
- [ ] Реализовать outbound mapping `ClientOutputMessage` → Telegram API (message, keyboard, media placeholders).
- [ ] Добавить delivery status lifecycle (queued/sent/failed) с нормализацией ошибок Telegram API.

### A3. Надежность и эксплуатация
- [ ] Идемпотентность inbound updates (deduplication по update id).
- [ ] Retry/backoff для временных ошибок Telegram API.
- [ ] Корреляция логов (sessionId/channelMessageId/updateId).
- [ ] Метрики: throughput, error-rate, retry-count, p95 latency.

### A4. Тестирование
- [ ] Unit-тесты маппинга inbound/outbound + negative cases.
- [ ] Интеграционные тесты с mock Telegram API (429, 5xx, malformed payload).
- [ ] Smoke-профиль через docker-compose с имитацией Telegram provider.

### Definition of Done (Telegram)
- [ ] Адаптер проходит интеграционные и smoke-тесты.
- [ ] Включены метрики и health-индикаторы.
- [ ] Подготовлен runbook «как включить Telegram в новом окружении за <30 мин».

---

## Фаза B (приоритет №2): Web-ориентированный чат-бот с кастомизацией под бренд заказчика

### B1. Канальный backend для web chat
- [ ] Довести `client-journey-channel-websocket` до integration-ready (session lifecycle, reconnect, heartbeat, delivery acks).
- [ ] Ввести стабильный контракт событий `RuntimeWsRequest/RuntimeWsResponse` с versioning policy.
- [ ] Добавить защиту от race conditions при множественных вкладках/переподключениях.

### B2. Frontend chat runtime
- [ ] Выделить конфигурируемую тему (цвета, радиусы, типографика, spacing, logo, avatar, tone).
- [ ] Ввести runtime-конфиг темы (JSON/admin endpoint) с валидацией и fallback к дефолтной теме.
- [ ] Поддержать white-label режим: скрытие vendor-элементов, кастомные приветствия, кастомные CTA.

### B3. «Корпоративные стандарты» и UX governance
- [ ] Пресеты бренд-гайдов (light/dark/high-contrast).
- [ ] Проверки контрастности (WCAG AA) и базовая accessibility-навигация клавиатурой.
- [ ] Локализация UI-текстов (ru/en + расширяемость).
- [ ] Политика безопасного рендера контента (XSS-safe markdown/HTML policy).

### B4. Встраивание и поставка
- [ ] Embed-режим: script-snippet для внешних сайтов + конфигурация через data-атрибуты.
- [ ] Standalone-режим: отдельная страница чата.
- [ ] Версионирование frontend-ассетов и обратная совместимость конфигов тем.

### B5. Тестирование
- [ ] Playwright e2e: happy path, reconnect, API errors, theme switching.
- [ ] Визуальные regression-тесты для тем.
- [ ] Контрактные тесты websocket-событий.

### Definition of Done (Web Chat)
- [ ] Заказчик может сменить тему/бренд без релиза backend-кода.
- [ ] Есть документированный onboarding для интеграции на корпоративный сайт.
- [ ] Покрыты ключевые UX и эксплуатационные сценарии (ошибки/переподключения/деградации).

---

## Фаза C (приоритет №3): Остальные каналы (WhatsApp, MAX, Facebook, Kafka)

### C1. Унификация adapter baseline
- [ ] Привести контракты всех каналов к единому integration-ready стандарту (`IntegrationReadyChannelAdapter`).
- [ ] Унифицировать webhook signature verification и обработку provider-specific ошибок.
- [ ] Согласовать таблицу capability matrix (text/media/buttons/template/status callbacks).

### C2. Enterprise-функции
- [ ] Rate-limit и per-channel quotas.
- [ ] Idempotency keys на inbound события.
- [ ] Политики retry/DLQ (особенно для Kafka и webhook-driven каналов).

### C3. Операционка
- [ ] Channel-specific метрики и алерты.
- [ ] Runbooks инцидентов (деградация провайдера, invalid signature, quota exceeded).
- [ ] Автоматизированные smoke-профили для каждого канала.

### Definition of Done (Other Channels)
- [ ] Каждый канал можно включить через конфиг без доработки core.
- [ ] Для каждого канала есть тест-контур и runbook.
- [ ] Общая observability-панель показывает статус всех каналов единообразно.

---

## Сквозные workstreams (параллельно с фазами A/B/C)
- [ ] **Security:** hardening секретов, rotation policy, минимизация PII в логах.
- [ ] **Observability:** единая трассировка request → session → provider message.
- [ ] **QA automation:** матрица регрессии по каналам + nightly smoke.
- [ ] **Documentation:** архитектурные ADR по channel abstraction и theming.

---

## Предлагаемый порядок исполнения (итерации)
1. **Iteration 1:** Graph Editor 0.1 (spike + выбор стека).
2. **Iteration 2:** Graph Editor 0.2–0.3 (MVP UX + валидации).
3. **Iteration 3:** Graph Editor 0.4 + e2e и rollout в Admin UI.
4. **Iteration 4–5:** Telegram A1–A2.
5. **Iteration 6:** Telegram A3–A4 + go-live readiness.
6. **Iteration 7–8:** Web chat B1–B2.
7. **Iteration 9:** Web chat B3–B5 + branded pilot.
8. **Iteration 10+:** Остальные каналы C1–C3 (по бизнес-приоритету).



### Спринт-план (после выбора React Flow)
1. **Sprint RF-1 (1–2 дня):** `admin-v2` scaffold + React Flow canvas + базовые узлы/ребра.
2. **Sprint RF-2 (2–3 дня):** side panel, CRUD вариантов ответа, синхронизация selection/state.
3. **Sprint RF-3 (1–2 дня):** семантическая валидация reachability/to-RESULT + ошибки в UI.
4. **Sprint RF-4 (1–2 дня):** import/export + backend `ScenarioGraph` API roundtrip + smoke e2e.

## Критерии приоритезации задач в бэклоге
- Влияние на time-to-market ближайшего канала.
- Снижение эксплуатационных рисков (P1/P2 incidents).
- Переиспользуемость решения в других каналах.
- Сложность внедрения vs бизнес-эффект.
