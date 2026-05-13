# Graph node interaction rules (Phase 0)

## Scope
Документ фиксирует допустимые взаимодействия между узлами в текущем Phase 0 редакторе.

## Node type matrix

| Node type | Allowed outgoing transition codes | Required data | Notes |
|---|---|---|---|
| `QUESTION` | Любые уникальные `answerCode` в пределах узла | `text` | Основной branching-узел |
| `SERVICE_SELECTION` | Любые уникальные `answerCode` | `serviceRefs[]` (не пустой) | Отдельный шаг выбора услуг |
| `API_CAPTURE` | `next` или `__default__` | `captureCode` | Capture-chain шаг, ветвление по answerCode не допускается |
| `GROOVY_CAPTURE` | `next` или `__default__` | `captureCode` | Capture-chain шаг, ветвление по answerCode не допускается |
| `RESULT` | Нет исходящих обязательных правил | `text` | Финальный узел |

## Validation alignment (Phase 0)
- Обязателен явный стартовый узел `id=start` (и он не может быть `RESULT`).
- `QUESTION` и `SERVICE_SELECTION` должны иметь минимум один исходящий переход.
- Capture-узлы без `captureCode` считаются ошибкой.
- Capture-узлы без исходящих переходов считаются ошибкой.
- Для capture-узлов коды переходов, отличные от `next`/`__default__`, считаются ошибкой.
- Для `SERVICE_SELECTION` пустой `serviceRefs[]` считается ошибкой.
- Глобальные проверки: broken edges, duplicate answer codes, unreachable nodes, no-path-to-RESULT.

## Pre-release policy
- Legacy migration-note для старых JSON-черновиков не является блокером релиза на текущем pre-release этапе.
