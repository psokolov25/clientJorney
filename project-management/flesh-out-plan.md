# План «наращивания мясом» скелетного кода

## Контекст
Проект уже содержит стабильный каркас: доменная модель, runtime/admin API, storage/visit SPI, frontend-заготовки и skeleton-адаптеры каналов. Цель плана — поэтапно довести skeleton-компоненты до production-ready состояния, сохраняя приоритет работ: **сначала редактор клиентского пути, затем Telegram → Web-чат → остальные каналы**.

## Что означает «заполнить скелетный код плотью» (пофазная реализация)

Ниже — операционное определение «фаз»: для каждой фазы фиксируются **конкретные артефакты кода**, **контракт готовности** и **минимальный набор тестов**, без которых фазу нельзя считать реализованной.

### Phase Gate формула (для всех фаз)
- **Code gate:** реализованы целевые модули/классы фазы (не TODO/заглушки, а рабочий поток).
- **Contract gate:** зафиксированы контракты (API/SPI/events/config) и совместимость версий.
- **Test gate:** unit + integration/smoke тесты проходят локально и в CI.
- **Ops gate:** есть runbook, метрики, health и понятная диагностика отказов.

### Фаза 0 → «Плоть для editor skeleton»
- **Где код:** `client-journey-app` (`admin/index.html`, admin controllers/services, graph validation/capture preview).
- **Что доводим до production-уровня:**
  - полноценный React Flow UX вместо демо-канваса;
  - optimistic locking (`ETag`/`If-Match` + version precondition);
  - publish gate + семантическая валидация маршрутов;
  - capture-node authoring/preview с детерминированными ошибками.
- **Минимум тестов для закрытия фазы:**
  - unit: controller/service precondition conflicts (`409`);
  - unit: capture negative paths (invalid JSON, groovy runtime error);
  - e2e smoke: authoring happy path + import/export roundtrip.

### Фаза A → «Плоть для Telegram skeleton adapter»
- **Где код:** `client-journey-channel-telegram`, `client-journey-channel-spi`, runtime integration points.
- **Что доводим до production-уровня:**
  - inbound/outbound mapping до полного набора поддерживаемых апдейтов;
  - idempotency + retry/backoff + delivery state machine;
  - fail-fast валидация конфига и observability (logs/metrics/health).
- **Минимум тестов для закрытия фазы:**
  - unit: mapping + error normalization;
  - integration: mock provider (429/5xx/malformed payload);
  - adapter smoke profile в docker-compose.

#### Статус-корректив по Фазе A (обновлено: 2026-05-13)
- [x] A1 (частично): в `TelegramPollingAdapterSkeleton` добавлен fail-fast `validateConfig(...)`:
  - проверка обязательных ключей (`botToken`, `deliveryMode`);
  - проверка допустимых mode (`POLLING`/`WEBHOOK`);
  - обязательный `webhookUrl` для `WEBHOOK`;
  - детерминированные коды ошибок (`CONFIG_MISSING*`, `CONFIG_INVALID_DELIVERY_MODE`).
- [x] Добавлены unit-тесты позитивных/негативных кейсов в `TelegramPollingAdapterSkeletonTest`.
- [~] A1 остаётся частично открытой до добавления:
  - profile/runbook конфигурации в `application*.yml`;
  - формализации timeout/retry/allowed-updates в `requiredConfigKeys` policy.

### Фаза B → «Плоть для Web Chat skeleton»
- **Где код:** `client-journey-channel-websocket` + frontend runtime/widget.
- **Что доводим до production-уровня:**
  - lifecycle websocket session (reconnect/heartbeat/acks);
  - white-label theming с runtime-конфигом и безопасным fallback;
  - governance UX: accessibility, localization, secure content policy.
- **Минимум тестов для закрытия фазы:**
  - contract tests websocket events;
  - Playwright e2e (reconnect/errors/theme switch);
  - visual regression по темам.

### Фаза C → «Плоть для остальных channel skeleton»
- **Где код:** `client-journey-channel-whatsapp`, `...-facebook`, `...-max`, `...-kafka`.
- **Что доводим до production-уровня:**
  - единый integration-ready adapter baseline;
  - capability matrix + provider-specific error policies;
  - channel runbooks, smoke profiles, алерты.
- **Минимум тестов для закрытия фазы:**
  - per-channel integration tests;
  - per-channel smoke;
  - сквозной regression для общих adapter contracts.

### Definition of Done для «фазы заполнения скелета»
- [ ] Нет критичных TODO/заглушек в целевых модулях фазы.
- [ ] Все phase gates (Code/Contract/Test/Ops) закрыты и подтверждены в CI.
- [ ] Документация фазы обновлена: runbook + checklist релиза.

---

## Фаза 0 (срочный приоритет): Редактор клиентского пути (graph authoring UX)

### 0.1. Переход с canvas-демо на graph editor framework
- [x] Выбрать библиотеку для production-редактора: **React Flow** (решение принято 2026-05-12, см. `project-management/graph-editor-libraries-review.md`).
- [ ] Полностью перевести admin-редактор на React Flow с drag/drop, zoom/pan, selection, edge handles (без legacy ветки).



### 0.1.a. React Flow implementation track (принято в работу)
- [x] Переключить основной `admin/index.html` на React Flow как целевой экран релиза (без feature-flag и без legacy-дублирования).
- [x] Реализовать `QuestionNode` и `ResultNode` как кастомные React Flow nodes с inline-preview текста и счётчиком вариантов.
- [x] Реализовать `AnswerEdge` с меткой `answerCode:answerLabel` и кликабельным переходом к редактированию.
- [x] Подключить side panel к выбранному node/edge (редактирование node/edge + выделение с canvas).
- [x] Добавить auto-layout для первичной читабельности (dagre adapter в Admin UI).
- [x] Подготовить migration utility: convert `nodes+answers[]` <-> React Flow elements (вынесены явные converter-функции `reactFlowElementsToScenarioGraph` / `scenarioGraphToReactFlowElements`).

### 0.1.b. Модель узлов и взаимодействий (актуализация scope)
- [x] Поддерживаемые типы узлов в Phase 0: `QUESTION`, `RESULT`, `SERVICE_SELECTION`, `API_CAPTURE`, `GROOVY_CAPTURE`.
- [x] Поддерживаемые типы переходов:
  - answer-driven (`answerCode:answerLabel`) для ветвлений QUESTION/SERVICE_SELECTION;
  - sequence-driven (`next:*`) для capture-step цепочек;
  - retarget/edit перехода через side panel + кликабельную метку ребра на canvas.
- [x] Добавить формальную таблицу «node type -> allowed outgoing/incoming interactions» в runbook/ADR (см. `project-management/graph-node-interaction-rules.md`).

### 0.2. UX «собрать цепочку без боли»
- [x] Быстрый сценарий: `QUESTION` → набор `answerCode` → переход в следующий `QUESTION`/`RESULT` (реализовано в Admin UI, май 2026).
- [x] Side panel редактирования узла/ребра (текст вопроса, варианты ответов, условия перехода).
- [x] Горячие действия: «добавить следующий вопрос», «добавить финальный результат», «добавить шаг выбора услуг».

### 0.3. Семантическая валидация графа
- [x] Проверка недостижимых узлов, дубликатов `answerCode` в пределах узла, отсутствия путей до `RESULT` (добавлена валидация + список ошибок в UI).
- [x] Предпросмотр клиентского пути (dry-run прямо из редактора).
- [x] Блокировка публикации при критических ошибках графа (добавлен явный publish-gate check в Admin UI).

### 0.4. Интеграция с backend
- [x] Прозрачный import/export в `ScenarioGraph` API без ручного редактирования JSON (load/save graph + import/export JSON доступны в Admin UI).
- [x] Автосохранение черновика и контроль конкурентного редактирования (local autosave/load + `version`/`ETag` preconditions + server-side enforcement precondition policy: `428` when missing headers + `409` on version conflict).



### 0.5. Следующие шаги (в работу немедленно)
- [x] Подключить сохранение/загрузку графа через `ScenarioGraph` backend API (persist + reload).
- [x] Добавить валидацию reachability: стартовый узел, недостижимые узлы, обязательный путь до `RESULT`.
- [x] Ввести явный тип узла «выбор услуг» (service-selection) и соответствующие правила маршрутизации (quick-add + валидация serviceRefs).
- [x] Зафиксировать правила маршрутизации для `API_CAPTURE`/`GROOVY_CAPTURE` (в валидаторе разрешены `next` / `__default__`, остальное помечается как ошибка).
- [x] Добавить e2e smoke для Admin UI: построение ветки `QUESTION -> QUESTION -> RESULT` + import/export roundtrip.
- [x] Подготовить migration-note/policy: для pre-release legacy-нормализация не блокирует релиз (см. `project-management/graph-node-interaction-rules.md`).

### 0.6. Детализация задач по `API_CAPTURE` / `GROOVY_CAPTURE`
- [x] UI-редактор конфигурации `API_CAPTURE`:
  - поля `method`, `url`, `payloadField`, `nextInputField`;
  - генерация/применение `captureCode` в JSON-формате.
- [x] UI-редактор `GROOVY_CAPTURE`:
  - CodeMirror блок для редактирования Groovy-кода;
  - сохранение кода в `captureCode`.
- [x] Валидация capture-узлов:
  - обязательный `captureCode`;
  - обязательный исходящий переход;
  - допустимые коды переходов: `next` / `__default__`.
- [x] Preview-пайплайн:
  - вызов `/graph/capture-preview` с `nodeId`, `answerValue`, `metadata`;
  - отображение результата preview в side panel.
- [x] Операционные тест-кейсы:
  - негативные кейсы для невалидного JSON-конфига API_CAPTURE;
  - негативные кейсы для runtime ошибок Groovy-скриптов в preview;
  - сценарий цепочки `API_CAPTURE -> GROOVY_CAPTURE -> RESULT` (через preview/unit pipeline).

### 0.7. Production-hardening редактора (сверка с `graph-editor-libraries-review.md`)
- [~] Undo/redo для graph-операций (базовый snapshot-based undo/redo в UI реализован, требуется расширенная шлифовка UX/границ истории).
- [x] Autosave (local) + optimistic locking (server-side precondition enforcement включен).
- [x] Версионирование сценария + diff preview перед публикацией (path-based diff preview в UI реализован).

### Definition of Done (Graph Editor)
- [x] Можно собрать полный путь: вопрос → варианты → ветвления → выбор услуг/результат без ручного JSON.
- [x] Ошибки маршрута показываются в UI до сохранения.
- [x] Есть e2e-тест «ветвящийся сценарий до финального RESULT».

### Остаток до полного закрытия Фазы 0 (операционный)
- [x] Явный publish-гейт: отдельная кнопка проверки публикации и жёсткая блокировка при `validationIssues`.
- [x] Autosave черновика (debounce) + optimistic locking/version check при сохранении (обязательные precondition headers enforced на backend).
- [x] Убрать/пересмотреть migration-note пункт под текущую стратегию «без backward compatibility для pre-release».

---

## Фаза A (приоритет №1 после редактора): Telegram-канал — от polling skeleton к production adapter

### A1. Контракты и конфигурация
- [x] Уточнить и зафиксировать обязательные `requiredConfigKeys` для Telegram (token, webhook/polling mode, timeout, retry policy, allowed updates).
- [x] Ввести валидацию конфигурации при старте адаптера и fail-fast ошибки с понятными кодами.
- [x] Добавить профиль запуска Telegram в `application*.yml` и отдельный runbook с примерами env-переменных.

### A2. Runtime-поток сообщений
- [x] Реализовать inbound mapping Telegram update → `ClientInputMessage` (text, command, callback, attachments metadata).
- [x] Реализовать outbound mapping `ClientOutputMessage` → Telegram API (message, keyboard, media placeholders).
- [x] Добавить delivery status lifecycle (queued/sent/failed) с нормализацией ошибок Telegram API.

### A3. Надежность и эксплуатация
- [x] Идемпотентность inbound updates (deduplication по update id).
- [x] Retry/backoff для временных ошибок Telegram API (exponential schedule + cap + optional jitter в adapter debug path).
- [x] Корреляция логов (sessionId/channelMessageId/updateId) в delivery debug payload.
- [x] Метрики: throughput, error-rate, retry-count, p95 latency (в `metricsSnapshot`: throughputPerSecond, errorRate, retryAttempts, latencyP95Ms).

### A4. Тестирование
- [x] Unit-тесты маппинга inbound/outbound + negative cases.
- [x] Интеграционные тесты с mock Telegram API (429, 5xx, malformed payload).
- [x] Smoke-профиль через docker-compose с имитацией Telegram provider.

### Definition of Done (Telegram)
- [ ] Адаптер проходит интеграционные и smoke-тесты.
- [ ] Включены метрики и health-индикаторы.
- [ ] Подготовлен runbook «как включить Telegram в новом окружении за <30 мин».

---

## Фаза B (приоритет №2): Web-ориентированный чат-бот с кастомизацией под бренд заказчика

### B1. Канальный backend для web chat
- [ ] Довести `client-journey-channel-websocket` до integration-ready (session lifecycle, reconnect, heartbeat, delivery acks).
- [~] Ввести стабильный контракт событий `RuntimeWsRequest/RuntimeWsResponse` с versioning policy. (черновой baseline: `contractVersion=ws.v1`, supported inbound/outbound event types и delivery-ack payload в websocket adapter skeleton)
- [ ] Добавить защиту от race conditions при множественных вкладках/переподключениях.

### B2. Frontend chat runtime
- [~] Выделить конфигурируемую тему (цвета, радиусы, типографика, spacing, logo, avatar, tone). (реализован query-driven theming baseline: colors/radius/logo/title/launcher icon)
- [~] Ввести runtime-конфиг темы (JSON/admin endpoint) с валидацией и fallback к дефолтной теме. (пока реализован URL query-config с безопасными ограничениями для radius и дефолтами через CSS vars)
- [~] Поддержать white-label режим: скрытие vendor-элементов, кастомные приветствия, кастомные CTA. (baseline: `whiteLabel=true` скрывает hint/locale, доступны `title/hint/startCta/sendCta`)

### B3. «Корпоративные стандарты» и UX governance
- [~] Пресеты бренд-гайдов (light/dark/high-contrast). (baseline: `themePreset=dark|high-contrast` в web chat)
- [~] Проверки контрастности (WCAG AA) и базовая accessibility-навигация клавиатурой. (baseline: focus-visible стили, Enter-to-send, Esc-to-close widget, aria-label для ключевых полей)
- [~] Локализация UI-текстов (ru/en + расширяемость). (baseline: runtime выбор `locale` через config + сохранение пользовательского выбора в localStorage)
- [~] Политика безопасного рендера контента (XSS-safe markdown/HTML policy). (baseline: safe markdown subset renderer без `innerHTML`, только text nodes + whitelist для ссылок `https?://`)

### B4. Встраивание и поставка
- [~] Embed-режим: script-snippet для внешних сайтов + конфигурация через data-атрибуты. (реализован widget mode + приоритетная конфигурация из URL/query, `#cj-chat-embed[data-*]` и `window.CJ_CHAT_CONFIG`)
- [~] Standalone-режим: отдельная страница чата. (baseline: `mode=standalone-fullscreen` переключает чат в полноэкранный page-layout)
- [~] Версионирование frontend-ассетов и обратная совместимость конфигов тем. (baseline: `CHAT_UI_VERSION` + `configVersion` маркер в UI для явной диагностики совместимости embed-конфига)

### B5. Тестирование
- [~] Playwright e2e: happy path, reconnect, API errors, theme switching. (baseline: smoke покрывает admin phase-0 flow + chat locale + widget/white-label/themePreset/configVersion сценарий)
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

## Ревизия планов фаз (процедура контроля)
- Периодичность: **каждый merge в `work/main` + еженедельный review**.
- Для каждой фазы фиксируем:
  1) что реально реализовано в коде (module/class/test);
  2) какие пункты плана устарели или слишком оптимистичны;
  3) какие проверки отсутствуют до production gate.
- Корректировки в плане обязательны, если:
  - изменился scope фазы;
  - появились новые техриски (например, multi-writer conflicts, provider throttling);
  - тестовые критерии не покрывают новые ветки поведения.

### Ближайшие корректирующие шаги (next actions)
1. **Фаза 0:** закрыть операционный долг по autosave/optimistic-lock policy-hardening (multi-writer сценарии, conflict UX).
2. **Фаза A:** завершить A1 policy-слой и перейти к A2 inbound/outbound mapping с негативными integration-кейсами.
3. **Фаза B:** до начала кодинга зафиксировать versioning policy websocket events и contract-test шаблон.

#### Пометки завершения элементов фаз (журнал)
- [x] **Фаза 0 / Optimistic-lock policy-hardening:** контроллер сохраняет и возвращает `ETag`, а `PUT /graph` требует precondition-заголовок (`If-Match` или `X-Scenario-Graph-Version`) с `428` при отсутствии и `409` при конфликте версии.
- [x] **Фаза 0 / Multi-writer edge-case:** добавлен тест для `If-Match` с несколькими ETag-значениями (включая `W/` weak ETag), чтобы зафиксировать поведение при составных precondition-заголовках.
- [x] **Фаза 0 / HTTP precondition compatibility:** добавлена поддержка wildcard `If-Match: *` и тест-кейс, подтверждающий корректный optimistic-lock flow для существующего ресурса.
- [x] **Фаза 0 / Conflict UX:** в UI реализован сценарий разрешения конфликта версии: локальный draft сохраняется при `409`, показывается конфликт `local vs server`, автоматически строится conflict diff (`server -> local`) и предлагается немедленно загрузить актуальный граф с API.
- [x] **Фаза 0 / Conflict diagnostics hardening:** при `409` версия сервера извлекается как из структурированного поля, так и из текста backend-сообщения (fallback parsing), чтобы подсказки в UI оставались корректными при разных форматах error-body.
- [x] **Фаза 0 / Phase capability enforcement:** `validateFlow` теперь валидирует соответствие графа активной фазе (`PHASE_0/1/2`) и блокирует сохранение при наличии запрещённых в фазе типов узлов.

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
