# Client Journey Platform

![Java](https://img.shields.io/badge/Java-21-blue)
![Micronaut](https://img.shields.io/badge/Micronaut-4.x-1f6feb)
![Build](https://img.shields.io/badge/build-maven-success)
![License](https://img.shields.io/badge/license-Internal-lightgrey)

Промышленный стартовый проект для визуального конструирования клиентских путей/опросников с embedded Admin UI и Chat UI, единым runtime/admin API и интеграцией с внешними системами СУО/очередей.

## Оглавление
- [1. Обзор](#1-обзор)
- [2. Ключевые возможности](#2-ключевые-возможности)
- [3. Архитектура](#3-архитектура)
- [3.1 Диаграммы (PlantUML + SVG)](#31-диаграммы-plantuml--svg)
- [4. Структура репозитория](#4-структура-репозитория)
- [5. Требования к окружению](#5-требования-к-окружению)
- [6. Быстрый старт](#6-быстрый-старт)
- [7. Конфигурация и профили](#7-конфигурация-и-профили)
- [8. API-обзор](#8-api-обзор)
- [9. Embedded frontend](#9-embedded-frontend)
- [10. Тестирование и качество](#10-тестирование-и-качество)
- [11. Эксплуатация (security/observability)](#11-эксплуатация-securityobservability)
- [12. Лучшие практики для контрибьюторов](#12-лучшие-практики-для-контрибьюторов)
- [13. Ограничения и roadmap](#13-ограничения-и-roadmap)

## 1. Обзор

Система построена как модульный monorepo с одним исполняемым Micronaut-приложением (`client-journey-app`) и набором подключаемых модулей: хранилища, канальные адаптеры и клиенты создания визита.

**Главный принцип:** каналы не реализуют логику маршрутизации — они только маппят сообщения в SPI-контракты (`ClientInputMessage`/`ClientOutputMessage`), а бизнес-логика исполнения маршрутов находится в ядре (`ScenarioEngine`).

## 2. Ключевые возможности

- Единый runtime/admin API и WebSocket runtime endpoint.
- Embedded Admin UI и Chat UI (без отдельного frontend-сервера в production).
- SPI-архитектура для storage/channel/visit-creation.
- Поддержка file/H2/PostgreSQL хранилищ.
- Интеграция с VisitManager и кастомными REST-провайдерами создания визитов.
- Расширенная наблюдаемость (`health/readiness/liveness/dashboard`) и API ошибок.
- Security baseline: API key, rate limiting, PII masking, OIDC-ready конфигурация.

## 3. Архитектура

Границы системы:
- `client-journey-app` — HTTP API, websocket, embedded static frontend.
- `client-journey-core` — исполнение сценариев и route-logic.
- `client-journey-domain` — доменные модели.
- `client-journey-storage-*` — реализации репозиториев.
- `client-journey-channel-*` — адаптеры каналов.
- `client-journey-visit-*` — клиенты создания визитов.

Внешние зависимости: VisitManager REST API, Kafka, Telegram Bot API polling, и официальные провайдеры мессенджеров.



### 3.1 Диаграммы (PlantUML + SVG)

> Исходники диаграмм хранятся в `docs/diagrams/src/*.puml`,
> а текстовые SVG-рендеры — в `docs/diagrams/svg/*.svg`.

#### System context

- Исходник: `docs/diagrams/src/system-context.puml`
- SVG-рендер (`docs/diagrams/svg/system-context.svg`) хранится в репозитории и обновляется при изменениях диаграммы.

#### Module containers

- Исходник: `docs/diagrams/src/module-containers.puml`
- SVG-рендер (`docs/diagrams/svg/module-containers.svg`) хранится в репозитории и обновляется при изменениях диаграммы.

## 4. Структура репозитория

```text
client-journey-domain
client-journey-core
client-journey-storage-spi
client-journey-storage-file
client-journey-storage-h2
client-journey-storage-postgres
client-journey-channel-*
client-journey-visit-spi
client-journey-visit-visitmanager
client-journey-visit-custom-rest
client-journey-visit-mock
client-journey-app
project-management/
docs/
tests/frontend/
```

## 5. Требования к окружению

- JDK 21+
- Maven 3.9+
- Node.js 20+ (для frontend e2e)
- npm 10+
- (опционально) Docker / Docker Compose

## 6. Быстрый старт

### Сборка

```bash
./mvnw clean package
```

### Запуск (file profile)

```bash
java -Dmicronaut.environments=file -jar client-journey-app/target/client-journey-app-0.1.0-SNAPSHOT.jar
```

Для Windows PowerShell используйте тот же аргумент без пробела после `-D`:

```powershell
java "-Dmicronaut.environments=file" -jar client-journey-app/target/client-journey-app-0.1.0-SNAPSHOT.jar
```

### Запуск (postgres profile)

```bash
java -Dmicronaut.environments=postgres -jar client-journey-app/target/client-journey-app-0.1.0-SNAPSHOT.jar
```

Если jar ещё не собран, сначала выполните:

```bash
./mvnw clean package
```


Проверить фактическое имя артефакта можно так:

```bash
ls client-journey-app/target/*.jar
```

PowerShell:

```powershell
Get-ChildItem client-journey-app/target/*.jar
```

## 7. Конфигурация и профили

Основные файлы:
- `client-journey-app/src/main/resources/application.yml`
- `application-file.yml`
- `application-h2.yml`
- `application-postgres.yml`

Рекомендуемые практики:
- Хранить секреты только через переменные окружения/secret manager.
- Разделять конфигурации dev/stage/prod через профили и overlays.
- Для rate-limit и security policy использовать явные значения per-endpoint.

## 8. API-обзор

### Runtime API

```http
POST /api/runtime/scenarios/{scenarioCode}/sessions
POST /api/runtime/sessions/{sessionId}/answers
```

### Admin API

```http
GET    /api/admin/scenarios
POST   /api/admin/scenarios
GET    /api/admin/scenarios/{id}
PUT    /api/admin/scenarios/{id}
DELETE /api/admin/scenarios/{id}
GET    /api/admin/scenarios/{id}/graph
PUT    /api/admin/scenarios/{id}/graph
GET    /api/admin/scenarios/{id}/visit-creation-settings
PUT    /api/admin/scenarios/{id}/visit-creation-settings
POST   /api/admin/scenarios/{id}/visit-creation-settings/test
POST   /api/admin/scenarios/{id}/validate
POST   /api/admin/scenarios/{id}/publish
POST   /api/admin/scenarios/{id}/archive
POST   /api/admin/scenarios/{id}/clone-version
POST   /api/admin/scenarios/import
GET    /api/admin/scenarios/{id}/export
```

## 9. Embedded frontend

Production endpoints:

```text
/
/admin/**
/chat/**
/api/**
/ws/runtime
/swagger-ui/**
```

Frontend тесты и детали описаны в `FRONTEND-TESTING.md`.

WebGUI отдается основным backend-сервером Micronaut (без отдельного frontend-сервера в production).

## 10. Тестирование и качество

### Быстрый запуск unit-тестов

```bash
./mvnw -DskipITs test
```

### Backend

```bash
./mvnw -pl client-journey-app -am test
```

### Frontend e2e

```bash
npm ci
npm run frontend:test
```

### Рекомендации

- Для новых API добавлять unit + controller tests.
- Для cross-layer изменений добавлять архитектурные тесты.
- Для пользовательских сценариев UI — Playwright smoke/integration тесты.

## 11. Эксплуатация (security/observability)

### Логирование

В приложении используется `logback.xml` с гибкой настройкой:
- отдельные аппендеры: `CONSOLE`, `APP_FILE`, `SECURITY_FILE`, `AUDIT_FILE`;
- ротация по времени и размеру (`SizeAndTimeBasedRollingPolicy`);
- лимиты объема архива (`totalSizeCap`) и срока хранения (`maxHistory`) по каждому потоку логов;
- независимые уровни логирования по пакетам (`com.clientjourney`, `com.clientjourney.app.security`, `com.clientjourney.app.admin`).

Основные переменные окружения:
- `LOG_DIR`, `ROOT_LOG_LEVEL`, `APP_LOG_LEVEL`, `SECURITY_LOG_LEVEL`, `AUDIT_LOG_LEVEL`;
- `APP_MAX_FILE_SIZE`, `APP_MAX_HISTORY_DAYS`, `APP_TOTAL_SIZE_CAP`;
- `SEC_MAX_FILE_SIZE`, `SEC_MAX_HISTORY_DAYS`, `SEC_TOTAL_SIZE_CAP`;
- `AUDIT_MAX_FILE_SIZE`, `AUDIT_MAX_HISTORY_DAYS`, `AUDIT_TOTAL_SIZE_CAP`.


- Security: API key фильтр, rate limiting, policy endpoint, masking PII.
- Observability: `health/readiness/liveness/dashboard`, базовые метрики и диагностические payload.
- Runtime ошибки унифицированы через `ApiExceptionHandler`/`ApiErrorResponse`.

## 12. Лучшие практики для контрибьюторов

- Делать маленькие PR с чёткой целью (1 тема = 1 PR).
- Обновлять `project-management/backlog.md` и `project-management/implementation-plan.md` вместе с функциональными изменениями.
- Не смешивать refactor и feature без необходимости.
- Для публичных контрактов (DTO/API/SPI) обязательно добавлять тесты на обратную совместимость.
- Документировать операционные изменения (конфиги, лимиты, флаги).

## 13. Ограничения и roadmap

- Часть channel adapters пока в skeleton-режиме (integration-ready, но без полного production-runtime).
- Требуются дополнительные end-to-end интеграции с реальными внешними API и устойчивые нагрузочные прогоны.
- Roadmap и спринты ведутся в `project-management/`.
- Runbook по подключению channel providers: `docs/CHANNEL-PROVIDER-RUNBOOK.md`.
