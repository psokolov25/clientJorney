# Telegram adapter roadmap (from skeleton to production)

## Phase 1 — transport reliability (POLLING first)
- Introduce stateful `offset` persistence for `getUpdates` to avoid duplicate processing after restart.
- Add scheduler + backoff policy for long-poll loop (`timeout`, retry jitter, circuit-open on repeated failures).
- Add delivery-mode switcher service (`POLLING` default, `WEBHOOK` optional) wired by configuration.

## Phase 2 — webhook hardening
- Verify Telegram secret header (`X-Telegram-Bot-Api-Secret-Token`) in addition to HMAC checks.
- Add webhook idempotency guard (`update_id` cache with TTL).
- Add audit logging with masked payload fields.

## Phase 3 — conversation semantics
- Map Telegram callback buttons (`callback_query`) to structured answer DTOs.
- Add command routing (`/start`, `/help`, `/reset`) and scenario bootstrap mapping.
- Support rich outbound messages: keyboard, inline keyboard, markdown formatting.

## Phase 4 — delivery guarantees
- Split outbound send into provider client + retry policy + DLQ/failure store.
- Add error taxonomy (`RATE_LIMIT`, `CHAT_NOT_FOUND`, `BOT_BLOCKED`, `TEMPORARY_UNAVAILABLE`).
- Add metrics: delivery attempts, success ratio, p95 response time, webhook validation failures.

## Phase 5 — operational readiness
- Add integration tests with mocked Telegram API + contract tests for update payloads.
- Add observability endpoints for channel-specific health and lag.
- Add feature flags for gradual rollout of WEBHOOK mode per environment.
