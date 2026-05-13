# Telegram adapter runbook (Phase A1/A2 baseline)

## Purpose
Operational guide for enabling `client-journey-channel-telegram` in a new environment with fail-fast config validation.

## Required config

Minimum required keys (validated by `TelegramPollingAdapterSkeleton.validateConfig(...)`):
- `client-journey.channel.telegram.botToken`
- `client-journey.channel.telegram.deliveryMode` (`POLLING` or `WEBHOOK`)
- `client-journey.channel.telegram.webhookUrl` (required only when `deliveryMode=WEBHOOK`)

Reference profile: `client-journey-app/src/main/resources/application-telegram.yml`.

## Env variables

- `CJ_TELEGRAM_BOT_TOKEN` — BotFather token.
- `CJ_TELEGRAM_DELIVERY_MODE` — `POLLING` or `WEBHOOK`.
- `CJ_TELEGRAM_WEBHOOK_URL` — public HTTPS webhook URL for webhook mode.
- `CJ_TELEGRAM_POLLING_TIMEOUT` — long-poll timeout (seconds).
- `CJ_TELEGRAM_ALLOWED_UPDATES` — comma-separated updates (e.g. `message,callback_query`).
- `CJ_TELEGRAM_RETRY_MAX_ATTEMPTS` — outbound retry budget.
- `CJ_TELEGRAM_RETRY_BASE_DELAY_MS` — base delay for retry policy.

## Startup checklist

1. Set required env vars (`botToken`, `deliveryMode`, optional `webhookUrl`).
2. Run app with telegram profile config loaded.
3. Verify adapter bean is enabled:
   - property `client-journey.channel.telegram.enabled=true`.
4. Validate inbound mapping with sample updates:
   - command (`/start`) -> `COMMAND`;
   - callback (`callback_query.data`) -> `CALLBACK`;
   - media message -> `ATTACHMENT`.
5. Validate outbound behavior:
   - `buildOutboundPayload(...)` with optional `parseMode`/`replyMarkup`;
   - retry behavior and `deliveryState` in debug payload.

## Smoke profile (docker-compose)

Use adapter smoke stack with Telegram env + webhook mock:

```bash
docker compose -f docker-compose.adapters-smoke.yml up --build
```

Smoke profile includes:
- `telegram-mock` stub endpoint on `:8081`;
- app env defaults for Telegram (`CJ_TELEGRAM_*`) including webhook mode;
- baseline provider stub (`mock-provider`) for generic adapter checks.

## Troubleshooting

- `CONFIG_MISSING*` or `CONFIG_INVALID_DELIVERY_MODE`:
  check env var names and mode value.
- `RETRY_EXHAUSTED` in outbound result:
  increase retry budget / inspect channel transport and provider availability.
- Duplicate inbound updates:
  ensure `update_id` is present and dedup pipeline uses `shouldProcessUpdate(...)`.
