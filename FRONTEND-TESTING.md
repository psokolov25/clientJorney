# Frontend testing and browser debugging

## Install tooling

```bash
npm install
npx playwright install --with-deps chromium firefox
```

## Run smoke tests

```bash
npm run frontend:test
```

## Run per-browser

```bash
npm run frontend:test:chromium
npm run frontend:test:firefox
```

## Debug with Chrome/Firefox emulation

```bash
npm run frontend:debug:chromium
npm run frontend:debug:firefox
```

These commands enable Playwright Inspector (`PWDEBUG=1`) for step-by-step frontend debugging.

## Artifacts

- screenshots: `test-results/admin-frontend.png`, `test-results/chat-frontend.png`
- root entrypoint smoke: `tests/frontend/root-entrypoint.spec.ts`
- html report: `playwright-report/index.html`


## CI

GitHub Actions workflow: `.github/workflows/frontend-playwright.yml`

- запускает Playwright smoke-тесты в Chromium и Firefox;
- публикует `playwright-report/` и `test-results/` как artifacts.


## Screenshots policy

- PNG screenshots are generated locally for QA/demo and **must not be committed**.
- Recommended output dirs: `test-results/` or local temp folders.

## Доступ к frontend через backend

Основной способ доступа к WebGUI — через backend-сервер Micronaut:
- `http://localhost:8080/`
- `http://localhost:8080/admin/`
- `http://localhost:8080/chat/`

В production frontend отдается backend-сервером Micronaut.
В e2e-тестах используется встроенный статический сервер (`python3 -m http.server`) для быстрых и стабильных smoke/mock прогонов.
