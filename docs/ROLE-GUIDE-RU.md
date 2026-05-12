# Руководство по ролям (RU)

## 1. Разработчик
- Запуск backend: `mvn -pl client-journey-app -am test`
- Запуск frontend e2e: `npm ci && npm run frontend:test`
- Канальные адаптеры: см. `client-journey-channel-*` и контракт `IntegrationReadyChannelAdapter`.

## 2. Пользователь (оператор/администратор)
- Admin UI: `/admin/`
- Chat UI: `/chat/`
- Проверка operational-статуса: `/api/system/dashboard`

## 3. Внедренец
- Security policy: `/api/system/security/policy`
- Branch-aware visit creation: `AdminVisitCreationSettingsController` и endpoint resolve.
- Channel adapter onboarding: required config keys в соответствующих skeleton-адаптерах.

## 4. Техподдержка
- Health/readiness/liveness: `/api/system/health`, `/api/system/readiness`, `/api/system/liveness`
- Метрики/trace: `/api/system/metrics`, `/api/system/trace`
- Ошибки API: унифицированный формат `ApiErrorResponse`.

## 5. Продажник / пресейл
- Демо-сценарий: Admin + Chat + branch selection + dashboard.
- Артефакты e2e: `playwright-report/`, `test-results/`
- CI-пайплайн frontend smoke: `.github/workflows/frontend-playwright.yml`
